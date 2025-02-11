
package org.quickfixj.ilink3;

import static java.util.Collections.singletonList;

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
import quickfix.FieldConvertError;
import quickfix.FixVersions;
import quickfix.Initiator;
import quickfix.Message;
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

    private final ExecutorService pollingExecutor = Executors.newSingleThreadExecutor();

    private volatile ILink3Connection connection;
    private final SessionSettings settings;
    private final FIXPMessageHandler fixpMessageHandler;
    private final FIXMessageHandler fixMessageHandler;
    private ArchivingMediaDriver mediaDriver;
    private FixEngine engine;
    private FixLibrary library;
    private Future<?> libraryPollingFuture;

    // TODO use SessionSchedule

    public ILink3Connector(SessionSettings settings, FIXPMessageHandler fixpMessageHandler,
	    FIXMessageHandler fixMessageHandler) throws ConfigError {

	this.settings = Objects.requireNonNull(settings, "Need to specify SessionSettings");
	this.fixpMessageHandler = Objects.requireNonNull(fixpMessageHandler, "Need to specify FIXPMessageHandler");
	this.fixMessageHandler = Objects.requireNonNull(fixMessageHandler, "Need to specify FIXMessageHandler");

    }

    public void start() throws ConfigError, FieldConvertError {
	startILink3Connection();
    }

    public void stop() {
	if (connection != null) {
	    if (connection.isConnected()) {
		LOG.info("Stopping connection...");
		connection.terminate("application shutdown", 0);
	    }
	}
	if (libraryPollingFuture != null) {
	    libraryPollingFuture.cancel(true);
	}
	if (library != null) {
	    library.close();
	}
	if (engine != null) {
	    engine.close();
	}
	if (mediaDriver != null) {
	    mediaDriver.close();
	}
    }

    public State getState() {
	if (connection != null) {
	    return connection.state();
	}
	return State.UNBOUND;
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
    
    public boolean triggerRetransmitRequest(long uuid, long fromSeqNo, int msgCount) {
	if (connection != null) {
	    long status = connection.tryRetransmitRequest(uuid, fromSeqNo, msgCount);
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

	    connection.commit();
	    return true;
	} else {
	    LOG.error("Cannot send message {}, connection is not initialized or not established.", fixMessage);
	    return false;
	}
    }

    void startILink3Connection() throws ConfigError, FieldConvertError {
	LOG.info("Starting iLink3 connection...");

	if (libraryPollingFuture != null) {
	    libraryPollingFuture.cancel(true);
	}

	if (settings.isSetting(SESSION_ID_ILINK3, SETTING_FIXP_DEBUG)) {
	    System.setProperty("fix.core.debug", settings.getString(SESSION_ID_ILINK3, SETTING_FIXP_DEBUG));
	}

	final IdleStrategy idleStrategy = new SleepingMillisIdleStrategy();
	final EngineConfiguration engineConfiguration = engineConfiguration();
	mediaDriver = launchMediaDriver(engineConfiguration);
	engine = FixEngine.launch(engineConfiguration);
	library = FixLibrary.connect(libraryConfiguration());

	final uk.co.real_logic.artio.ilink.ILink3ConnectionHandler connectionHandler = new ILink3ConnectionHandler(LOG,
		fixpMessageHandler, fixMessageHandler);
	final ILink3ConnectionConfiguration connectionConfiguration = ILink3ConnectionConfiguration.builder()
		.host(settings.getString(SESSION_ID_ILINK3, Initiator.SETTING_SOCKET_CONNECT_HOST))
		.port(Integer.valueOf(settings.getString(SESSION_ID_ILINK3, Initiator.SETTING_SOCKET_CONNECT_PORT)))
		.sessionId(settings.getString(SESSION_ID_ILINK3, SETTING_SESSION_ID))
		.firmId(settings.getString(SESSION_ID_ILINK3, SETTING_FIRM_ID))
		.userKey(settings.getString(SESSION_ID_ILINK3, SETTING_USER_KEY))
		.accessKeyId(settings.getString(SESSION_ID_ILINK3, SETTING_ACCESS_KEY_ID))
		.tradingSystemName(settings.getString(SESSION_ID_ILINK3, SETTING_TRADING_SYSTEM_NAME))
		.tradingSystemVendor(settings.getString(SESSION_ID_ILINK3, SETTING_TRADING_SYSTEM_VENDOR))
		.tradingSystemVersion(settings.getString(SESSION_ID_ILINK3, SETTING_TRADING_SYSTEM_VERSION))
		.handler(connectionHandler).requestedKeepAliveIntervalInMs(30000)
//		.reEstablishLastConnection(true)
		.build();

	while (!library.isConnected()) {
	    idleStrategy.idle(library.poll(1));
	}

	final Reply<ILink3Connection> reply = library.initiate(connectionConfiguration);

	LOG.info("Executing connection initiation...");
	while (reply.isExecuting()) {
	    idleStrategy.idle(library.poll(1));
	}

	libraryPollingFuture = pollingExecutor.submit(new LibraryPollTask(library, idleStrategy));

	if (reply.hasCompleted()) {
	    connection = reply.resultIfPresent();
	    LOG.info("Connected: " + connection);
	} else if (reply.hasErrored()) {
	    LOG.error("Error when connecting: " + reply.error());
	} else if (reply.hasTimedOut()) {
	    LOG.error("Timed out when connecting: " + reply);
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

    private static class ErrorHandler implements org.agrona.ErrorHandler {

	@Override
	public void onError(Throwable throwable) {
	    LOG.error("ILink3Connector.ErrorHandler.onError() " + throwable);
	}
    }

    private static class RetransmitHandler implements ILink3RetransmitHandler {

	@Override
	public void onReplayedBusinessMessage(int templateId, DirectBuffer buffer, int offset, int blockLength,
		int version) {
	    LOG.info("ILink3Connector.RetransmitHandler.onReplayedBusinessMessage() " + templateId);
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
}
