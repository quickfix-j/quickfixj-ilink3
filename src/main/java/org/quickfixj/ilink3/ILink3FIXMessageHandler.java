
package org.quickfixj.ilink3;

import org.agrona.sbe.MessageDecoderFlyweight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Message;

public class ILink3FIXMessageHandler implements FIXMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ILink3FIXMessageHandler.class);

    @Override
    public void onFIXMessage(Message message, MessageDecoderFlyweight decoderFlyweight) {
	LOG.info("Got FIX message: {}", message);
    }

    @Override
    public void onFIXMessageSent(Message message) {
	LOG.info("Sent FIX message: {}", message);
    }

    @Override
    public void onFixMessageUnknownReject(Message message, int probableSeqNum) {
	LOG.info("BusinessReject: CME can not process FIX message: {}", message);
    }

}
