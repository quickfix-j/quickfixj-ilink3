
package org.quickfixj.ilink3;

import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;

import io.aeron.logbuffer.ControlledFragmentHandler.Action;
import uk.co.real_logic.artio.fixp.FixPConnection;
import uk.co.real_logic.artio.fixp.FixPMessageHeader;
import uk.co.real_logic.artio.library.NotAppliedResponse;
import uk.co.real_logic.artio.messages.DisconnectReason;

/**
 * Receives FIXP business message and events from
 * uk.co.real_logic.artio.ilink.ILink3ConnectionHandler.
 */
public interface FIXPMessageHandler {

    MessageDecoderFlyweight onFIXPMessage(FixPConnection connection, int templateId, DirectBuffer buffer, int offset,
	    int blockLength, int version, boolean possRetrans, FixPMessageHeader messageHeader);

    default Action onNotApplied(FixPConnection connection, long fromSequenceNumber, long msgCount,
	    NotAppliedResponse response) {
	response.gapfill();
	return Action.CONTINUE;
    }

    default Action onRetransmitReject(FixPConnection connection, String reason, long requestTimestamp, int errorCodes) {
	return Action.CONTINUE;
    }

    default Action onRetransmitTimeout(FixPConnection connection) {
	return Action.CONTINUE;
    }

    default Action onSequence(FixPConnection connection, long nextSeqNo) {
	return Action.CONTINUE;
    }

    default Action onError(FixPConnection connection, Exception ex) {
	return Action.CONTINUE;
    }

    default Action onDisconnect(FixPConnection connection, DisconnectReason reason) {
	return Action.CONTINUE;
    }

}
