
package org.quickfixj.ilink3;

import java.io.IOException;
import java.util.Optional;

import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import org.slf4j.Logger;

import io.aeron.logbuffer.ControlledFragmentHandler.Action;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.MessageStore;
import quickfix.field.BusinessRejectReason;
import quickfix.field.MsgType;
import uk.co.real_logic.artio.fixp.FixPConnection;
import uk.co.real_logic.artio.fixp.FixPMessageHeader;
import uk.co.real_logic.artio.ilink.ILink3Connection;
import uk.co.real_logic.artio.library.NotAppliedResponse;
import uk.co.real_logic.artio.messages.DisconnectReason;

public class ILink3ConnectionHandler implements uk.co.real_logic.artio.ilink.ILink3ConnectionHandler {

    private final Logger log;
    private final FIXPMessageHandler fixpMessageHandler;
    private final FIXMessageHandler fixMessageHandler;
    private final ILink3Connector iLink3Connector;
    private MessageStore messageStore;

    public ILink3ConnectionHandler(Logger log, FIXPMessageHandler fixpMessageHandler,
	    FIXMessageHandler fixMessageHandler, ILink3Connector iLink3Connector) {
	this.log = log;
	this.fixpMessageHandler = fixpMessageHandler;
	this.fixMessageHandler = fixMessageHandler;
	this.iLink3Connector = iLink3Connector;
	this.messageStore = null; // this needs to be configured after connecting, we need the uuid
    }

    @Override
    public Action onEstablishmentAck(FixPConnection connection, long previousUuid, long previousSeqNo, long uuid,
	    long lastUuid, long nextSeqNo) {

	log.info("Got EstablishmentAck with PreviousUUID {} and PreviousSeqNo {} on UUID {} with NextSeqNo {}",
		previousUuid, previousSeqNo, uuid, nextSeqNo);

	Message fixMessage = ILink3MessageConverter.createFixMessage("EstablishmentAck");
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.UUID,
		Long.toString(((ILink3Connection) connection).uuid()));
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.NEXT_SEQ_NO, Long.toString(nextSeqNo));
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.PREVIOUS_SEQ_NO,
		Long.toString(previousSeqNo));
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.PREVIOUS_UUID, Long.toString(previousUuid));
	
	fixMessageHandler.onFIXMessage(fixMessage, null);
	iLink3Connector.handleEstablishAck(previousUuid, previousSeqNo, uuid, lastUuid, nextSeqNo);
	
	return Action.CONTINUE;
    }

    public Action onNotApplied(FixPConnection connection, long fromSequenceNumber, long msgCount,
	    NotAppliedResponse response) {
	
	log.info("ILink3Connector.ConnectionHandler.onNotApplied()");
	Message fixMessage = ILink3MessageConverter.createFixMessage("NotApplied");
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.UUID,
		Long.toString(((ILink3Connection) connection).uuid()));
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.FROM_SEQ_NO,
		Long.toString(fromSequenceNumber));
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.MSG_COUNT, Long.toString(msgCount));

	fixMessageHandler.onFIXMessage(fixMessage, null);

	return fixpMessageHandler.onNotApplied(connection, fromSequenceNumber, msgCount, response);
    }
    
    @Override
    public Action onRetransmitReject(FixPConnection connection, String reason, long requestTimestamp, int errorCodes) {
	
	Message fixMessage = ILink3MessageConverter.createFixMessage("RetransmitReject");
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.UUID,
		Long.toString(((ILink3Connection) connection).uuid()));
	// lastuuid is missing because artio is does not pass it on
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.REQUEST_TIMESTAMP,
		Long.toString(requestTimestamp));
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.ERROR_CODES, Long.toString(errorCodes));

	fixMessageHandler.onFIXMessage(fixMessage, null);
	log.info("ILink3Connector.ConnectionHandler.onRetransmitReject() {} {}", reason, errorCodes);

	return fixpMessageHandler.onRetransmitReject(connection, reason, requestTimestamp, errorCodes);
    }

    @Override
    public Action onRetransmitTimeout(FixPConnection connection) {
	log.warn("ILink3Connector.ConnectionHandler.onRetransmitTimeout()");
	return fixpMessageHandler.onRetransmitTimeout(connection);
    }

    @Override
    public Action onSequence(FixPConnection connection, long nextSeqNo) {
	
	log.info("ILink3Connector.ConnectionHandler.onSequence() {} {} {}", nextSeqNo, connection.nextRecvSeqNo(),
		connection.nextSentSeqNo());

	// check the seqnum and the message store
	try {
	    log.info("Message store: {} {}", messageStore.getNextTargetMsgSeqNum(),
		    messageStore.getNextSenderMsgSeqNum());
	} catch (IOException e) {
	    // TODO is it sensible to throw an exception here?
	    throw new RuntimeException(e);
	}
	Message fixMessage = ILink3MessageConverter.createFixMessage("Sequence");
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.UUID,
		Long.toString(((ILink3Connection) connection).uuid()));
	// FaultToleranceIndicator and KeepAliveIntervalLapsed are missing because artio
	// does not pass it on
	ILink3MessageConverter.setString(fixMessage, ILink3MessageConverter.NEXT_SEQ_NO, Long.toString(nextSeqNo));

	fixMessageHandler.onFIXMessage(fixMessage, null);

	return fixpMessageHandler.onSequence(connection, nextSeqNo);
    }

    @Override
    public Action onError(FixPConnection connection, Exception ex) {
	log.error("ILink3Connector.ConnectionHandler.onError() exception={}", ex);
	return fixpMessageHandler.onError(connection, ex);
    }

    @Override
    public Action onDisconnect(FixPConnection connection, DisconnectReason reason) {
	log.info("ILink3Connector.ConnectionHandler.onDisconnect() reason={}", reason);
	iLink3Connector.handleRemoteDisconnect();
	return fixpMessageHandler.onDisconnect(connection, reason);
    }

    @Override
    public Action onBusinessMessage(FixPConnection connection, int templateId, DirectBuffer buffer, int offset,
	    int blockLength, int version, boolean possRetrans, FixPMessageHeader messageHeader) {

	MessageDecoderFlyweight decoderFlyweight = fixpMessageHandler.onFIXPMessage(connection, templateId, buffer,
		offset, blockLength, version, possRetrans, messageHeader);
	MessageDecoderFlyweight wrap = decoderFlyweight.wrap(buffer, offset, blockLength, version);
	try {
	    // TODO check creating a thread-local string buffer for toString()/appendTo()
	    // output
	    log.info("Received message: {}", wrap.appendTo(new StringBuilder()).toString());
	} catch (Exception e) {
	    log.error("Error when logging message", e);
	}

	ILink3MessageConverter.logHex(decoderFlyweight);
	
	Message fixMessage = ILink3MessageConverter.convertToFIX(decoderFlyweight);

	try {
	    if (messageStore != null) {
		Optional<String> optionalUUID = fixMessage.getOptionalString(ILink3MessageConverter.UUID);
		if (optionalUUID.isPresent()) {
		    String uuid = optionalUUID.get();
		    if (uuid.equals(String.valueOf(((ILink3Connection) connection).uuid()))) {
			log.info("Start incrementing targetSeqNum: store: {} connection: {}",
				messageStore.getNextTargetMsgSeqNum(), connection.nextRecvSeqNo());
			messageStore.incrNextTargetMsgSeqNum();
			log.info("End incrementing targetSeqNum: store: {} connection: {}",
				messageStore.getNextTargetMsgSeqNum(), connection.nextRecvSeqNo());
		    } else {
			log.info("Received message for old UUID: not incrementing counters");
		    }
		}
	    } else {
		log.error("No message store found, can't track message seqNums");
	    }
	} catch (IOException e) {
	    log.error("IOException while accessing messageStore", e);
	}

	log.info("Converted FIX message: {}", fixMessage); // TODO probably could be removed since we log in
							   // fixMessageHandler.onFIXMessage
	
	try {
	    if (fixMessage.getHeader().getString(MsgType.FIELD).equals(MsgType.BUSINESS_MESSAGE_REJECT)) {
		int rejectReason = fixMessage.getInt(BusinessRejectReason.FIELD);
		if (rejectReason == BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE || rejectReason == 109) {
		    log.info("Rejection for unprocessed message from cme decrement senderSeqnum and reject");
		    // in the exceptional cases of a reject with reason 3:UnsupportedMSgType or
		    // 109:Incoming message could not be decoded
		    // we should not increment the senderseqnum (in other words we have to
		    // decrement the senderseqnum after receiving these rejections)
		    
		    // TODO doesn't this also need to be protected by a LOCK? See
		    // ILink3Connector.sendToTarget() and triggerRetransmitRequest()
		    messageStore.setNextSenderMsgSeqNum(messageStore.getNextSenderMsgSeqNum() - 1);
		    // previous message was rejected
		    fixMessageHandler.onFixMessageUnknownReject(fixMessage, messageStore.getNextTargetMsgSeqNum() - 1);
		    return Action.CONTINUE;
		}
	    }
	} catch (FieldNotFound | IOException ex) {
	    log.error("Problem processing BusinessMessageReject {}", fixMessage, ex);
	}

	fixMessageHandler.onFIXMessage(fixMessage, decoderFlyweight);
	
	return Action.CONTINUE;
    }

    public void setMessageStore(MessageStore messageStore) {
	this.messageStore = messageStore;
    }
}
