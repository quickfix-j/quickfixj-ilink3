
package org.quickfixj.ilink3;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.agrona.DirectBuffer;
import org.agrona.collections.IntHashSet;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aeron.CommonContext;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchivingMediaDriver;
import io.aeron.archive.client.AeronArchive.Context;
import io.aeron.driver.MediaDriver;
import quickfix.ConfigError;
import quickfix.DefaultSessionSchedule;
import quickfix.FieldConvertError;
import quickfix.FixVersions;
import quickfix.Initiator;
import quickfix.Message;
import quickfix.MessageStore;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import uk.co.real_logic.artio.Reply;
import uk.co.real_logic.artio.engine.EngineConfiguration;
import uk.co.real_logic.artio.engine.FixEngine;
import uk.co.real_logic.artio.engine.ILink3RetransmitHandler;
import uk.co.real_logic.artio.fixp.FixPConnection;
import uk.co.real_logic.artio.fixp.FixPConnection.State;
import uk.co.real_logic.artio.ilink.ILink3Connection;
import uk.co.real_logic.artio.ilink.ILink3ConnectionConfiguration;
import uk.co.real_logic.artio.library.AcquiringSessionExistsHandler;
import uk.co.real_logic.artio.library.FixLibrary;
import uk.co.real_logic.artio.library.LibraryConfiguration;
import uk.co.real_logic.artio.library.LibraryConnectHandler;

public class ILink3Connector {

    // Aeron/Artio related settings
    private static final String CLIENT_LOGS = "client-logs";
    private static final String CONTROL_REQUEST_CHANNEL = "aeron:ipc?endpoint=localhost:0";
    private static final String CONTROL_RESPONSE_CHANNEL = "aeron:ipc?endpoint=localhost:0";
    private static final String RECORDING_EVENTS_CHANNEL = "aeron:ipc?control-mode=dynamic|control=localhost:0";
    private static final String REPLICATION_CHANNEL = "aeron:ipc?endpoint=localhost:0";

    private static final String LIBRARY_AERON_CHANNEL = CommonContext.IPC_CHANNEL;
    private static final String ARCHIVE_DIR_NAME = "client-archive";

    // settings used on the Establish message
    public static final String SETTING_SESSION_ID = "SessionID";
    public static final String SETTING_FIRM_ID = "FirmID";
    public static final String SETTING_ACCESS_KEY_ID = "AccessKeyID";
    public static final String SETTING_USER_KEY = "UserKey";
    public static final String SETTING_TRADING_SYSTEM_NAME = "TradingSystemName";
    public static final String SETTING_TRADING_SYSTEM_VERSION = "TradingSystemVersion";
    public static final String SETTING_TRADING_SYSTEM_VENDOR = "TradingSystemVendor";
    public static final String SETTING_FIXP_DEBUG = "FIXPDebug";

    private static final Logger LOG = LoggerFactory.getLogger(ILink3Connector.class);
    /**
     * dummy sessionID that is used for QFJ-ILINK3 converted messages
     */
    public static final SessionID SESSION_ID_ILINK3 = new SessionID(FixVersions.BEGINSTRING_FIXT11, "QFJ", "ILINK3");
    private static final Object WRITE_LOCK = new Object();

    private final ExecutorService pollingExecutor = Executors.newSingleThreadExecutor();

    private final IdleStrategy idleStrategy = new SleepingMillisIdleStrategy();

    private volatile ILink3Connection connection;
    private final SessionSettings settings;
    private final DefaultSessionSchedule sessionSchedule;
    private final ILink3ConnectionHandler connectionHandler;
    private ArchivingMediaDriver mediaDriver;
    private FixEngine engine;
    private FixLibrary library;
    private Future<?> libraryPollingFuture;

    private final MessageStoreFactory messageStoreFactory;
    private MessageStore messageStore = null; // this is null till we have a UUID
    private RetransmitRequest pendingRetransmitRequest = null;

    public ILink3Connector(SessionSettings settings, FIXPMessageHandler fixpMessageHandler,
	    FIXMessageHandler fixMessageHandler, MessageStoreFactory messageStoreFactory) throws ConfigError {

	this.settings = Objects.requireNonNull(settings, "Need to specify SessionSettings");
	this.messageStoreFactory = messageStoreFactory;
	Objects.requireNonNull(fixpMessageHandler, "Need to specify FIXPMessageHandler");
	Objects.requireNonNull(fixMessageHandler, "Need to specify FIXMessageHandler");
	try {
	    sessionSchedule = new DefaultSessionSchedule(settings, SESSION_ID_ILINK3);
	} catch (FieldConvertError e) {
	    throw new ConfigError(e);
	}
	connectionHandler = new ILink3ConnectionHandler(LOG, fixpMessageHandler, fixMessageHandler, this);
    }

    public void start() throws ConfigError, FieldConvertError {
	start(false);
    }

    public void start(boolean useBackUp) throws ConfigError, FieldConvertError {
	try {
	    startILink3Connection(false, useBackUp);
	} catch (Exception e) {
	    LOG.error("Failed to start ILink3Connector", e);
	}
    }

    public void stop() {
	if (connection != null) {
	    if (connection.isConnected()) {
		LOG.info("Stopping connection...");
		// shutdown text needs to be a single word
		connection.terminate("application_shutdown", 0);
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    LOG.warn("Exception when stopping", e);
		}
	    }
	}
	if (libraryPollingFuture != null) {
	    libraryPollingFuture.cancel(true);
	}
	if (library != null) {
	    library.close();
	    library.poll(10);
	}
	if (engine != null) {
	    engine.close();
	}
	if (mediaDriver != null) {
	    mediaDriver.close();
	}
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	    LOG.warn("Exception when stopping", e);
	}
    }

    public State getState() {
	if (connection != null) {
	    return connection.state();
	}
	return State.UNBOUND;
    }

    public long getUUID() {
	if (connection != null) {
	    return connection.uuid();
	}
	return 0;
    }

    public boolean canSend() {
	if (connection != null) {
	    if (connection.state() == FixPConnection.State.ESTABLISHED
		    || connection.state() == FixPConnection.State.AWAITING_KEEPALIVE) {
		return true;
	    }
	}
	return false;
    }

    public boolean isSessionTime() {
	return sessionSchedule.isSessionTime();
    }

    public boolean triggerRetransmitRequest(long uuid, long fromSeqNo, int msgCount) {
	if (connection != null) {
	    long status = -1;
	    if (messageStore != null) {
		try {
		    synchronized (WRITE_LOCK) {
			LOG.info("Start checking senderSeqNums: store: {} connection: {}",
				messageStore.getNextSenderMsgSeqNum(), connection.nextSentSeqNo());
			messageStore.incrNextSenderMsgSeqNum();
			status = connection.tryRetransmitRequest(uuid, fromSeqNo, msgCount);
			LOG.info("End checking senderSeqNums: store: {} connection: {}",
				messageStore.getNextSenderMsgSeqNum(), connection.nextSentSeqNo());
		    }
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    } else {
		LOG.error("MessageStore is null, cannot increment seqNum");
	    }
	    if (status >= 0) {
		return true;
	    }
	}
	return false;
    }

    public boolean triggerRetransmitRequest(long fromSeqNo, int msgCount) {
	if (connection != null) {
	    return triggerRetransmitRequest(connection.uuid(), fromSeqNo, msgCount);
	}
	return false;
    }

    public long nextSenderSeqNum() {
        return connection.nextSentSeqNo();
    }

    public long nextReceiverSeqNum() {
        return connection.nextRecvSeqNo();
    }

    // TODO we should synchronize this method (or use a lock inside the method) to
    // prevent concurrent usage of encoders and sequence numbers
    public boolean sendToTarget(final Message fixMessage) {

	if (fixMessage == null) {
	    LOG.error("sendToTarget() called with NULL message");
	    return false;
	}

	if (canSend()) {
	    try {
		ILink3MessageConverter.convertFromFIXAndSend(fixMessage, connection);
	    } catch (Exception e) {
		LOG.error("Encountered exception when trying to send message {}", fixMessage, e);
		connection.abort();
		return false;
	    }

	    // TODO insert call to callback to enable modification of message before sending

	    if (messageStore != null) {
		try {
		    synchronized (WRITE_LOCK) {
			LOG.info("Start checking senderSeqNums: store: {} connection: {}",
				messageStore.getNextSenderMsgSeqNum(), connection.nextSentSeqNo());
			messageStore.incrNextSenderMsgSeqNum();
			LOG.info("End checking senderSeqNums: store: {} connection: {}",
				messageStore.getNextSenderMsgSeqNum(), connection.nextSentSeqNo());
		    }
		} catch (IOException e) {
		    connection.abort();
		    throw new RuntimeException(e);
		}
	    } else {
		LOG.error("MessageStore is null, cannot increment seqNum");
	    }

	    connection.commit();
	    return true;
	} else {
	    LOG.error("Cannot send message {}, connection is not initialized or not established.", fixMessage);
	    return false;
	}
    }

    public void startILink3Connection(boolean useLastConnection, boolean useBackUp) throws ConfigError {
	startILink3Connection(useLastConnection, useBackUp, true);
    }

    public void reconnectILink3Connection(boolean useLastConnection, boolean useBackUp) throws ConfigError {
        startILink3Connection(useLastConnection, useBackUp, false);
    }

    private void startILink3Connection(boolean useLastConnection, boolean useBackUp, boolean startUp) throws ConfigError {
	LOG.info("Starting iLink3 connection...");

	if (connection != null) {
	    LOG.info("An existing connection object was found with lastUuid: {} currentUuid: {}", connection.lastUuid(),
		    connection.uuid());
	} else {
	    LOG.info("No previous connection found, clean start");
	}

	if (settings.isSetting(SESSION_ID_ILINK3, SETTING_FIXP_DEBUG)) {
	    System.setProperty("fix.core.debug", settings.getString(SESSION_ID_ILINK3, SETTING_FIXP_DEBUG));
	}

	if (libraryPollingFuture != null) {
	    libraryPollingFuture.cancel(true);
	}
	
	if (startUp) {
	    // we only need this first time we startup
	    final EngineConfiguration engineConfiguration = engineConfiguration();
	    mediaDriver = launchMediaDriver(engineConfiguration);
	    engine = FixEngine.launch(engineConfiguration);
	    library = FixLibrary.connect(libraryConfiguration());
	}

	final ILink3ConnectionConfiguration connectionConfiguration = ILink3ConnectionConfiguration.builder()
		.host(useBackUp ? settings.getString(SESSION_ID_ILINK3, Initiator.SETTING_SOCKET_CONNECT_HOST + "1")
			: settings.getString(SESSION_ID_ILINK3, Initiator.SETTING_SOCKET_CONNECT_HOST))
		.port(useBackUp
			? Integer.valueOf(
				settings.getString(SESSION_ID_ILINK3, Initiator.SETTING_SOCKET_CONNECT_PORT + "1"))
			: Integer.valueOf(settings.getString(SESSION_ID_ILINK3, Initiator.SETTING_SOCKET_CONNECT_PORT)))
		.sessionId(settings.getString(SESSION_ID_ILINK3, SETTING_SESSION_ID))
		.firmId(settings.getString(SESSION_ID_ILINK3, SETTING_FIRM_ID))
		.userKey(settings.getString(SESSION_ID_ILINK3, SETTING_USER_KEY))
		.accessKeyId(settings.getString(SESSION_ID_ILINK3, SETTING_ACCESS_KEY_ID))
		.tradingSystemName(settings.getString(SESSION_ID_ILINK3, SETTING_TRADING_SYSTEM_NAME))
		.tradingSystemVendor(settings.getString(SESSION_ID_ILINK3, SETTING_TRADING_SYSTEM_VENDOR))
		.tradingSystemVersion(settings.getString(SESSION_ID_ILINK3, SETTING_TRADING_SYSTEM_VERSION))
		.handler(connectionHandler).requestedKeepAliveIntervalInMs(30000)
		.reEstablishLastConnection(useLastConnection)
		.build();

	while (!library.isConnected()) {
	    idleStrategy.idle(library.poll(1));
	}

	final Reply<ILink3Connection> reply = library.initiate(connectionConfiguration);

	LOG.info("Executing connection initiation...");
	while (reply.isExecuting()) {
	    idleStrategy.idle(library.poll(1));
	}

	if (reply.hasCompleted()) {
	    final ILink3Connection prevConnection = connection;
	    connection = reply.resultIfPresent();

	    if (prevConnection != null) {
                LOG.info("Previous Connection: {}", prevConnection);
            }
            LOG.info("Connection: {}", connection);
            
	    // message store with the uuid of the current session as SessionQualifier
	    messageStore = messageStoreFactory
		    .create(new SessionID(SESSION_ID_ILINK3.getBeginString(), SESSION_ID_ILINK3.getSenderCompID(),
			    SESSION_ID_ILINK3.getTargetCompID(), String.valueOf(connection.uuid())));
	    connectionHandler.setMessageStore(messageStore);

	} else if (reply.hasErrored()) {
	    LOG.error("Error when connecting: " + reply.error());
	} else if (reply.hasTimedOut()) {
	    LOG.error("Timed out when connecting: " + reply);
	}

        libraryPollingFuture = pollingExecutor.submit(new LibraryPollTask(library, idleStrategy));
	if (pendingRetransmitRequest != null) {
	    triggerRetransmitRequest(pendingRetransmitRequest.getUuid(), pendingRetransmitRequest.getSeqNum(), 0);
	    pendingRetransmitRequest = null;
	}
	
//	    todo: act on disconnect from fixlibrary also!
//	    nb: config for engine and library should not be reused over engine.launch() or library.connect() calls
//	    when closing the library, make sure that we do not poll
    }

    private static EngineConfiguration engineConfiguration() {
	final ErrorHandler errorHandler = new ErrorHandler();
	final IntHashSet gapfillOnRetransmitILinkTemplateIds = new IntHashSet();
	final RetransmitHandler retransmitHandler = new RetransmitHandler();
	try (EngineConfiguration configuration = new EngineConfiguration()) {
	    configuration.printAeronStreamIdentifiers(true);
	    Context aeronArchiveContext = configuration.aeronArchiveContext();
	    aeronArchiveContext.controlRequestChannel(CONTROL_REQUEST_CHANNEL)
		    .recordingEventsChannel(RECORDING_EVENTS_CHANNEL).controlResponseChannel(CONTROL_RESPONSE_CHANNEL);

	    return configuration.libraryAeronChannel(LIBRARY_AERON_CHANNEL).deleteLogFileDirOnStart(true)
		    .logFileDir(CLIENT_LOGS).logInboundMessages(true).logOutboundMessages(true)
		    .framerIdleStrategy(new SleepingMillisIdleStrategy()).lookupDefaultAcceptorfixDictionary(false)
		    .errorHandlerFactory(errorBuffer -> errorHandler).fixPRetransmitHandler(retransmitHandler)
		    .gapfillOnRetransmitILinkTemplateIds(gapfillOnRetransmitILinkTemplateIds);
	}
    }

    private static LibraryConfiguration libraryConfiguration() {
	final LibraryConfiguration configuration = new LibraryConfiguration();
	configuration.printAeronStreamIdentifiers(true);

	return configuration.libraryAeronChannels(singletonList(LIBRARY_AERON_CHANNEL))
		.sessionExistsHandler(new AcquiringSessionExistsHandler(true))
		.libraryConnectHandler(new LibraryConnectHandler() {
		    public void onConnect(final FixLibrary library) {
			LOG.info("Connected to FixLibrary");
		    }

		    public void onDisconnect(final FixLibrary library) {
			LOG.info("Disconnected from FixLibrary");
		    }
		});
    }

    private static ArchivingMediaDriver launchMediaDriver(EngineConfiguration engineConfiguration) {
	final MediaDriver.Context context = new MediaDriver.Context().dirDeleteOnStart(true);

	final Archive.Context archiveCtx = new Archive.Context().deleteArchiveOnStart(true)
		.archiveDirectoryName(ARCHIVE_DIR_NAME).controlChannel(CONTROL_REQUEST_CHANNEL)
		.replicationChannel(REPLICATION_CHANNEL).recordingEventsChannel(RECORDING_EVENTS_CHANNEL);

	archiveCtx.segmentFileLength(context.ipcTermBufferLength());
	archiveCtx.controlChannelEnabled(false);
	archiveCtx.archiveClientContext(engineConfiguration.aeronArchiveContext());

	return ArchivingMediaDriver.launch(context, archiveCtx);
    }

    public void handleRemoteDisconnect() {
        //TODO I think this will need to do something with the session scheduling to initiate reconnect attempts?
    }

    public void disconnect() {
        LOG.info("Disconnecting on user request");
        connection.terminate("USER_REQUEST", 0);
    }

    public void connect(boolean uselastConnection, boolean useBackup) {
	LOG.info("Connecting on user request");
	try {
	    reconnectILink3Connection(uselastConnection, useBackup);
	} catch (ConfigError e) {
	    throw new RuntimeException(e);
	}
    }

    public void handleEstablishAck(long previousUuid, long previousSeqNo, long uuid, long lastUuid, long nextSeqNo) {
	if (previousUuid == 0 && previousSeqNo > 0) {
	    // We are connecting at the beginning of the week and cme has sent messages on
	    // the previous uuid. This is indicated by the previousSeqNum>0 with prevUuid==0
	    // Artio will automatically trigger a resend request, but only if its own
	    // administered lastUuid is also 0. This should be the case, but if there is a
	    // failed connection attempt before it may not be so.
	    // The fact that it works with artio, is dependent on the primitive default
	    // values of lastuuid and lastseqnum as such it's not entirely clear if this is
	    // on purpose or happy by product.
	    if (lastUuid == 0) {
		// artio wil trigger a resend request, don't do anything
		// this doesn't happen if we already connected in the same session... which
		// would be weird but may happen if there is a connection problem
	    } else {
		LOG.info("Triggering retransmit request for beginning of week messages");
		pendingRetransmitRequest = new RetransmitRequest(0, 1);
	    }
	}
	if (previousUuid != 0 && previousSeqNo > 0) {
	    if (previousUuid == lastUuid) {
		LOG.info(
			"Artio can detect the discrepancy and will automatically send a resend request, please double check");
	    } else {
		LOG.info(
			"PreviousUuid was: {} with previousSeqNum: {} loading message store from previous session to verify for missing messages",
			previousUuid, previousSeqNo);
		MessageStore prevMessageStore = messageStoreFactory.create(
			new SessionID(FixVersions.BEGINSTRING_FIXT11, "QFJ", "ILINK3", String.valueOf(previousUuid)));
		try {
		    LOG.info("found last processed seqnum for uuid: {} @ {}", previousUuid,
			    prevMessageStore.getNextTargetMsgSeqNum() - 1);
		    if (prevMessageStore.getNextTargetMsgSeqNum() <= previousSeqNo) {
			LOG.info(
				"Last processed business message is different than what we received from cme: {} found: {} triggering a resend request",
				prevMessageStore.getNextTargetMsgSeqNum() - 1, previousSeqNo);
			pendingRetransmitRequest = new RetransmitRequest(previousUuid,
				prevMessageStore.getNextTargetMsgSeqNum());
		    }
		} catch (IOException e) {
		    LOG.error("Could not read messageStore: {}", e.getMessage(), e);
		}
	    }
	}
    }

    public Date getStartTime() throws IOException {
	if (messageStore == null) {
	    return null;
	} else {
	    return messageStore.getCreationTime();
	}
    }

    private static class ErrorHandler implements org.agrona.ErrorHandler {

	@Override
	public void onError(Throwable throwable) {
	    StringWriter sw = new StringWriter();
	    throwable.printStackTrace(new PrintWriter(sw));
	    LOG.error("ILink3Connector.ErrorHandler.onError() {}", sw, throwable);
	}
    }

    private static class RetransmitHandler implements ILink3RetransmitHandler {

	@Override
	public void onReplayedBusinessMessage(int templateId, DirectBuffer buffer, int offset, int blockLength,
		int version) {
	    LOG.info("ILink3Connector.RetransmitHandler.onReplayedBusinessMessage()  {}", templateId);
	}

    }

    private class LibraryPollTask implements Runnable {

	private final FixLibrary fixLibrary;
	private final IdleStrategy idleStrategy;

	public LibraryPollTask(FixLibrary fixLibrary, IdleStrategy idleStrategy) {
	    this.fixLibrary = fixLibrary;
	    this.idleStrategy = idleStrategy;
	}

	@Override
	public void run() {
	    LOG.info(this.getClass().getName() + " STARTED.");
	    while (!Thread.currentThread().isInterrupted()) {
		idleStrategy.idle(fixLibrary.poll(1));
	    }
	    LOG.info(this.getClass().getName() + " STOPPED.");
	}
    }

    private class RetransmitRequest {

	private final long uuid;
	private final long seqNum;

	public RetransmitRequest(long uuid, long seqNum) {
	    this.uuid = uuid;
	    this.seqNum = seqNum;
	}

	public long getUuid() {
	    return uuid;
	}

	public long getSeqNum() {
	    return seqNum;
	}

    }
}
