
package org.quickfixj.ilink3;

import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import org.slf4j.Logger;

import io.aeron.logbuffer.ControlledFragmentHandler.Action;
import quickfix.Message;
import uk.co.real_logic.artio.fixp.FixPConnection;
import uk.co.real_logic.artio.fixp.FixPMessageHeader;
import uk.co.real_logic.artio.library.NotAppliedResponse;
import uk.co.real_logic.artio.messages.DisconnectReason;

public class ILink3ConnectionHandler implements uk.co.real_logic.artio.ilink.ILink3ConnectionHandler {

    private final Logger log;
    private final FIXPMessageHandler fixpMessageHandler;
    private final FIXMessageHandler fixMessageHandler;

    public ILink3ConnectionHandler(Logger log, FIXPMessageHandler fixpMessageHandler,
	    FIXMessageHandler fixMessageHandler) {
	this.log = log;
	this.fixpMessageHandler = fixpMessageHandler;
	this.fixMessageHandler = fixMessageHandler;
    }

    @Override
    public Action onNotApplied(FixPConnection connection, long fromSequenceNumber, long msgCount,
	    NotAppliedResponse response) {
	log.info("ILink3Connector.ConnectionHandler.onNotApplied()");
	response.gapfill();
	return Action.CONTINUE;
    }

    @Override
    public Action onRetransmitReject(FixPConnection connection, String reason, long requestTimestamp, int errorCodes) {
	log.info("ILink3Connector.ConnectionHandler.onRetransmitReject() " + reason + " " + errorCodes);
	return Action.CONTINUE;
    }

    @Override
    public Action onRetransmitTimeout(FixPConnection connection) {
	log.warn("ILink3Connector.ConnectionHandler.onRetransmitTimeout()");
	return Action.CONTINUE;
    }

    @Override
    public Action onSequence(FixPConnection connection, long nextSeqNo) {
	log.info("ILink3Connector.ConnectionHandler.onSequence() " + nextSeqNo);
	return Action.CONTINUE;
    }

    @Override
    public Action onError(FixPConnection connection, Exception ex) {
	log.error("ILink3Connector.ConnectionHandler.onError() exception=" + ex);
	return Action.CONTINUE;
    }

    @Override
    public Action onDisconnect(FixPConnection connection, DisconnectReason reason) {
	log.info("ILink3Connector.ConnectionHandler.onDisconnect() reason=" + reason);
	return Action.CONTINUE;
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

	Message fixMessage = ILink3MessageConverter.convertToFIX(decoderFlyweight);
	fixMessageHandler.onFIXMessage(fixMessage, decoderFlyweight);

	return Action.CONTINUE;
    }
}