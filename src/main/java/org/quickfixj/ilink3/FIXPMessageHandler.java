
package org.quickfixj.ilink3;

import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;

import uk.co.real_logic.artio.fixp.FixPConnection;
import uk.co.real_logic.artio.fixp.FixPMessageHeader;

/**
 * Receives FIXP business messages.
 */
public interface FIXPMessageHandler {

    MessageDecoderFlyweight onFIXPMessage(FixPConnection connection, int templateId, DirectBuffer buffer, int offset,
	    int blockLength, int version, boolean possRetrans, FixPMessageHeader messageHeader);

}
