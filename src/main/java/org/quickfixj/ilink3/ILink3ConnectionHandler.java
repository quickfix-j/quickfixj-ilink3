
package org.quickfixj.ilink3;

import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import org.slf4j.Logger;

import io.aeron.logbuffer.ControlledFragmentHandler.Action;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.MessageStore;
import quickfix.field.MsgType;
import uk.co.real_logic.artio.fixp.FixPConnection;
import uk.co.real_logic.artio.fixp.FixPMessageHeader;
import uk.co.real_logic.artio.ilink.ILink3Connection;
import uk.co.real_logic.artio.library.NotAppliedResponse;
import uk.co.real_logic.artio.messages.DisconnectReason;

import java.io.IOException;

public class ILink3ConnectionHandler implements uk.co.real_logic.artio.ilink.ILink3ConnectionHandler {

    private final Logger log;
    private final FIXPMessageHandler fixpMessageHandler;
    private final FIXMessageHandler fixMessageHandler;
	private final ILink3Connector iLink3Connector;
	private MessageStore messageStore;

    public ILink3ConnectionHandler(Logger log, FIXPMessageHandler fixpMessageHandler,
								   FIXMessageHandler fixMessageHandler, ILink3Connector link3Connector) {
	this.log = log;
	this.fixpMessageHandler = fixpMessageHandler;
	this.fixMessageHandler = fixMessageHandler;
	this.iLink3Connector = link3Connector;
	this.messageStore = null;  //this needs to be configured after connecting, we need the uuid

    }

	//todo wouldn't it be nice if this was simply complete and you acces to all messages... yes it would

	@Override
	public Action onEstablishAck( final FixPConnection connection, long previousUuid, long previousSeqNo, long uuid, long lastUuid, long nextSeqNo){
		log.info("we got an establishAck with a an previous uuid " + previousUuid + " and a previous seqnum " + previousSeqNo + " on uuid: " + uuid + " with nextSeqNum: " + nextSeqNo);
		Message fixMessage = ILink3MessageConverter.createFixMessage("EstablishmentAck");
		ILink3MessageConverter.setString(fixMessage,ILink3MessageConverter.UUID,String.valueOf(((ILink3Connection) connection).uuid()));
		ILink3MessageConverter.setString(fixMessage,39013,Long.toString(nextSeqNo));
		ILink3MessageConverter.setString(fixMessage,39021,Long.toString(previousSeqNo));
		ILink3MessageConverter.setString(fixMessage,39015,Long.toString(previousUuid));
		ILink3MessageConverter.setString(fixMessage,39021,Long.toString(previousSeqNo));
		fixMessageHandler.onFIXMessage(fixMessage, null);
		iLink3Connector.handleEstablishAck( previousUuid,  previousSeqNo,  uuid, lastUuid, nextSeqNo);
		return Action.CONTINUE;
	}

    @Override
    public Action onNotApplied(FixPConnection connection, long fromSequenceNumber, long msgCount,
	    NotAppliedResponse response) {
	log.info("ILink3Connector.ConnectionHandler.onNotApplied()");
	Message fixMessage = ILink3MessageConverter.createFixMessage("NotApplied");
	ILink3MessageConverter.setString(fixMessage,ILink3MessageConverter.UUID,String.valueOf(((ILink3Connection) connection).uuid()));
	ILink3MessageConverter.setString(fixMessage,39018,Long.toString(fromSequenceNumber));
	ILink3MessageConverter.setString(fixMessage,39019,Long.toString(msgCount));
	fixMessageHandler.onFIXMessage(fixMessage, null);
	return fixpMessageHandler.onNotApplied(connection, fromSequenceNumber, msgCount, response);
    }

    @Override
    public Action onEstablishmentAck(FixPConnection connection, long previousUuid, long previousSeqNo, long uuid,
	    long lastuuid, long nextSeqNo) {
	return fixpMessageHandler.onEstablishmentAck(connection, previousUuid, previousSeqNo, uuid, lastuuid,
		nextSeqNo);
    }

    @Override
    public Action onRetransmitReject(FixPConnection connection, String reason, long requestTimestamp, int errorCodes) {
		Message fixMessage = ILink3MessageConverter.createFixMessage("RetransmitReject");
		ILink3MessageConverter.setString(fixMessage,ILink3MessageConverter.UUID,String.valueOf(((ILink3Connection) connection).uuid()));
		//lastuuid is missing because artio is a annoying about passing everything usefull on
		ILink3MessageConverter.setString(fixMessage,39002,Long.toString(requestTimestamp));
		ILink3MessageConverter.setString(fixMessage,39012,Long.toString(errorCodes));
		fixMessageHandler.onFIXMessage(fixMessage, null);
	log.info("ILink3Connector.ConnectionHandler.onRetransmitReject() " + reason + " " + errorCodes);
	return  fixpMessageHandler.onRetransmitReject(connection, reason, requestTimestamp, errorCodes);
    }

    @Override
    public Action onRetransmitTimeout(FixPConnection connection) {
	log.warn("ILink3Connector.ConnectionHandler.onRetransmitTimeout()");
	return fixpMessageHandler.onRetransmitTimeout(connection);
    }

    @Override
    public Action onSequence(FixPConnection connection, long nextSeqNo) {
	log.info("ILink3Connector.ConnectionHandler.onSequence() " + nextSeqNo + " " +connection.nextRecvSeqNo()+ " "  +  connection.nextSentSeqNo() );

	//check the seqnum and the message store
        try {
            log.info("Message store: " + messageStore.getNextTargetMsgSeqNum() + " " + messageStore.getNextSenderMsgSeqNum());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		Message fixMessage = ILink3MessageConverter.createFixMessage("Sequence");
		ILink3MessageConverter.setString(fixMessage,ILink3MessageConverter.UUID,String.valueOf(((ILink3Connection) connection).uuid()));
		//FaultToleranceIndicator or KeepAliveIntervalLapsed is missing because artio is a annoying about passing everything usefull on
		ILink3MessageConverter.setString(fixMessage,39013,Long.toString(nextSeqNo));
		fixMessageHandler.onFIXMessage(fixMessage, null);

        return fixpMessageHandler.onSequence(connection, nextSeqNo);
    }

    @Override
    public Action onError(FixPConnection connection, Exception ex) {
	log.error("ILink3Connector.ConnectionHandler.onError() exception=" + ex);
	return fixpMessageHandler.onError(connection,ex);
    }

    @Override
    public Action onDisconnect(FixPConnection connection, DisconnectReason reason) {
	log.info("ILink3Connector.ConnectionHandler.onDisconnect() reason=" + reason);
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
	    log.info("Received message: " + wrap.appendTo(new StringBuilder()).toString());
	} catch (Exception e) {
	    log.error("Error when logging message", e);
	}
	ILink3MessageConverter.logHex(decoderFlyweight);


	try {
		Message fixMessage = ILink3MessageConverter.convertToFIX(decoderFlyweight);
		try {
			if (messageStore != null) {
				if (fixMessage.isSetField(39001)) {
					try {
						String uuid = fixMessage.getString(39001);
						if (uuid.equals(String.valueOf(((ILink3Connection) connection).uuid()))) {
							log.info("Start increment targetseqnum: store:{} connection:{}", messageStore.getNextTargetMsgSeqNum(), connection.nextRecvSeqNo());
							messageStore.incrNextTargetMsgSeqNum();
							log.info("End increment targetseqnum: store:{} connection:{}", messageStore.getNextTargetMsgSeqNum(), connection.nextRecvSeqNo());
						} else {
							log.info("Recieved Message for old UUID: Not incrementing counters");
						}
					} catch (FieldNotFound ignored) {
					}
				}
			} else {
				log.error("No message store found, can't track message seqnums");
			}
		} catch (IOException e) {
			log.info("IOException while accessing messageStore", e);
		}
		log.info(fixMessage.toString());
		if (fixMessage.getHeader().getString(MsgType.FIELD).equals("j")){
			try {
				int rejectReason = fixMessage.getInt(380);
				if (rejectReason == 3 || rejectReason == 109) {
					log.info("Rejection for unprocessed message from cme decrement senderSeqnum and reject");
					//in the exceptional cases of a recject with reason 3:UnsupportedMSgType or 109:Incoming message could not be decoded
					//we should not increment the sender (in otherwords we have to decrement the senderseqnum after recieving these rejections)
					messageStore.setNextSenderMsgSeqNum(messageStore.getNextSenderMsgSeqNum() - 1);
					fixMessageHandler.onFixMessageUnkownReject(fixMessage,messageStore.getNextTargetMsgSeqNum()-1); //previous message was rejected
					return Action.CONTINUE;
				}
			}catch (FieldNotFound ex){
				log.error("BuisnessReject with no reject reason!? {}", fixMessage);
			}
		}
		fixMessageHandler.onFIXMessage(fixMessage, decoderFlyweight);
	}
	catch (Exception e) {
		log.error("Error when creating message" + e.getMessage(), e);
	}
		return Action.CONTINUE;
    }

	public void setMessageStore(MessageStore messageStore) {
		this.messageStore = messageStore;
	}
}
