
package org.quickfixj.ilink3;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.SessionSettings;
import uk.co.real_logic.artio.fixp.FixPConnection;
import uk.co.real_logic.artio.fixp.FixPMessageHeader;
import uk.co.real_logic.artio.ilink.Ilink3Protocol;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Token;

/**
 * Override this class to implement custom behaviour of callbacks in
 * {@link FIXPMessageHandler}, e.g.
 * {@link FIXPMessageHandler#onSequence(FixPConnection, long)}
 */
public class ILink3FIXPMessageHandler implements FIXPMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ILink3FIXPMessageHandler.class);
    private final Map<Integer, MessageDecoderFlyweight> templateToDecoderMap = new HashMap<>();

    public ILink3FIXPMessageHandler(SessionSettings settings) {

	if (settings.isSetting(ILink3Connector.SESSION_ID_ILINK3, ILink3Connector.SETTING_FIXP_DEBUG)) {
	    try {
		System.setProperty("fix.core.debug",
			settings.getString(ILink3Connector.SESSION_ID_ILINK3, ILink3Connector.SETTING_FIXP_DEBUG));
	    } catch (ConfigError e) {
		// ignore, we check for the setting first
	    }
	}

	Ir sbeIr = Ilink3Protocol.loadSbeIr();
	Collection<List<Token>> messages = sbeIr.messages();
	List<MessageDecoderFlyweight> collect = messages.stream().map(tokens -> {
	    final Token beginMessage = tokens.get(0);
	    final String decoderName = "iLinkBinary" + "." + beginMessage.name() + "Decoder";
	    return (MessageDecoderFlyweight) newInstance(decoderName);
	}).collect(Collectors.toList());

	for (MessageDecoderFlyweight messageDecoderFlyweight : collect) {
	    templateToDecoderMap.put(messageDecoderFlyweight.sbeTemplateId(), messageDecoderFlyweight);
	    LOG.info("Added decoder {} for templateId {}", messageDecoderFlyweight.getClass().getSimpleName(),
		    messageDecoderFlyweight.sbeTemplateId());
	}
    }

    private Object newInstance(final String decoderName) {
	try {
	    return Class.forName(decoderName).getConstructor().newInstance();
	} catch (final InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException
		| InvocationTargetException e) {
	    LOG.warn(e.getMessage());
	    return null;
	}
    }

    @Override
    public MessageDecoderFlyweight onFIXPMessage(FixPConnection connection, int templateId, DirectBuffer buffer,
	    int offset, int blockLength, int version, boolean possRetrans, FixPMessageHeader messageHeader) {

	return getDecoder(templateId);
    }
    
    private MessageDecoderFlyweight getDecoder(int templateId) {
	if (templateToDecoderMap.containsKey(templateId)) {
	    return templateToDecoderMap.get(templateId);
	}
	MessageDecoderFlyweight defaultMessageDecoderFlyweight = new DefaultMessageDecoderFlyweightImplementation();
	// TODO set fields from onFIXPMessage
	return defaultMessageDecoderFlyweight;
    }

    // TODO
    private class DefaultMessageDecoderFlyweightImplementation implements MessageDecoderFlyweight {

	@Override
	public int sbeSchemaVersion() {
	    return 0;
	}

	@Override
	public int sbeSchemaId() {
	    return 0;
	}

	@Override
	public int offset() {
	    return 0;
	}

	@Override
	public int encodedLength() {
	    return 0;
	}

	@Override
	public DirectBuffer buffer() {
	    return null;
	}

	@Override
	public int sbeTemplateId() {
	    return 0;
	}

	@Override
	public String sbeSemanticType() {
	    return "N/A";
	}

	@Override
	public int sbeBlockLength() {
	    return 0;
	}

	@Override
	public void limit(int limit) {
	}

	@Override
	public int limit() {
	    return 0;
	}

	@Override
	public MessageDecoderFlyweight wrap(DirectBuffer buffer, int offset, int actingBlockLength, int actingVersion) {
	    return null;
	}

	@Override
	public StringBuilder appendTo(StringBuilder builder) {
	    return null;
	}
    }

}
