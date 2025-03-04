
package org.quickfixj.ilink3;

import org.agrona.sbe.MessageDecoderFlyweight;

import quickfix.Message;

/**
 * Receives messages encoded as FIX tag-value messages along with the decoder
 * flyweight it was decoded from.
 */
public interface FIXMessageHandler {

    void onFIXMessage(Message message, MessageDecoderFlyweight decoderFlyweight);

    void onFIXMessageSend(Message message);

    void onFixMessageUnkownReject(Message message, Integer probableSeqNum);

}
