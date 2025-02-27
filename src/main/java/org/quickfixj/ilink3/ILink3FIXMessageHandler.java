
package org.quickfixj.ilink3;

import org.agrona.sbe.MessageDecoderFlyweight;

import quickfix.Message;

public class ILink3FIXMessageHandler implements FIXMessageHandler {

    @Override
    public void onFIXMessage(Message message, MessageDecoderFlyweight decoderFlyweight) {
	System.out.println("Got FIX message: " + message);
    }

    @Override
    public void onFIXMessageSend(Message message) {
        System.out.println("Send FIX message: " + message);
    }

}
