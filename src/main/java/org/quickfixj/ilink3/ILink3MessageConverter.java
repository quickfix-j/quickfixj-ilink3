
package org.quickfixj.ilink3;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import org.agrona.sbe.MessageEncoderFlyweight;
import org.agrona.sbe.MessageFlyweight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iLinkBinary.AvgPxInd;
import iLinkBinary.BooleanNULL;
import iLinkBinary.BusinessReject521Decoder;
import iLinkBinary.ClearingAcctType;
import iLinkBinary.CmtaGiveUpCD;
import iLinkBinary.CustOrdHandlInst;
import iLinkBinary.ExecMode;
import iLinkBinary.ListUpdAct;
import iLinkBinary.ManualOrdInd;
import iLinkBinary.ManualOrdIndReq;
import iLinkBinary.MassActionOrdTyp;
import iLinkBinary.MassCancelTIF;
import iLinkBinary.MassCxlReqTyp;
import iLinkBinary.MassStatusOrdTyp;
import iLinkBinary.MassStatusReqTyp;
import iLinkBinary.MassStatusTIF;
import iLinkBinary.MessageHeaderEncoder;
import iLinkBinary.NewOrderSingle514Encoder;
import iLinkBinary.OFMOverrideReq;
import iLinkBinary.OrderCancelReject535Decoder;
import iLinkBinary.OrderCancelReplaceReject536Decoder;
import iLinkBinary.OrderCancelReplaceRequest515Encoder;
import iLinkBinary.OrderCancelRequest516Encoder;
import iLinkBinary.OrderMassActionReport562Decoder;
import iLinkBinary.OrderMassActionReport562Decoder.NoAffectedOrdersDecoder;
import iLinkBinary.OrderMassActionRequest529Encoder;
import iLinkBinary.OrderMassStatusRequest530Encoder;
import iLinkBinary.OrderStatusRequest533Encoder;
import iLinkBinary.OrderTypeReq;
import iLinkBinary.PRICENULL9Encoder;
import iLinkBinary.PartyDetailRole;
import iLinkBinary.PartyDetailsDefinitionRequest518Encoder;
import iLinkBinary.PartyDetailsDefinitionRequest518Encoder.NoPartyDetailsEncoder;
import iLinkBinary.PartyDetailsDefinitionRequest518Encoder.NoTrdRegPublicationsEncoder;
import iLinkBinary.PartyDetailsDefinitionRequestAck519Decoder;
import iLinkBinary.PartyDetailsDefinitionRequestAck519Decoder.NoPartyDetailsDecoder;
import iLinkBinary.PartyDetailsDefinitionRequestAck519Decoder.NoTrdRegPublicationsDecoder;
import iLinkBinary.PartyDetailsListReport538Decoder;
import iLinkBinary.PartyDetailsListRequest537Encoder;
import iLinkBinary.PartyDetailsListRequest537Encoder.NoPartyIDsEncoder;
import iLinkBinary.PartyDetailsListRequest537Encoder.NoRequestingPartyIDsEncoder;
import iLinkBinary.QuoteTyp;
import iLinkBinary.RFQSide;
import iLinkBinary.RequestForQuote543Encoder;
import iLinkBinary.RequestForQuote543Encoder.NoRelatedSymEncoder;
import iLinkBinary.RequestForQuoteAck546Decoder;
import iLinkBinary.SLEDS;
import iLinkBinary.SMPI;
import iLinkBinary.ShortSaleType;
import iLinkBinary.SideNULL;
import iLinkBinary.SideReq;
import iLinkBinary.SplitMsg;
import quickfix.FieldMap;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.Group;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.field.AffectedOrderID;
import quickfix.field.AvgPxIndicator;
import quickfix.field.BusinessRejectReason;
import quickfix.field.BusinessRejectRefID;
import quickfix.field.ClOrdID;
import quickfix.field.CustOrderCapacity;
import quickfix.field.CustOrderHandlingInst;
import quickfix.field.CxlQty;
import quickfix.field.CxlRejReason;
import quickfix.field.CxlRejResponseTo;
import quickfix.field.DiscretionPrice;
import quickfix.field.DisplayQty;
import quickfix.field.ExecID;
import quickfix.field.ExecInst;
import quickfix.field.ExpireDate;
import quickfix.field.LastFragment;
import quickfix.field.ListUpdateAction;
import quickfix.field.ManualOrderIndicator;
import quickfix.field.MarketSegmentID;
import quickfix.field.MassActionRejectReason;
import quickfix.field.MassActionReportID;
import quickfix.field.MassActionResponse;
import quickfix.field.MassActionScope;
import quickfix.field.MassActionType;
import quickfix.field.MassStatusReqID;
import quickfix.field.MassStatusReqType;
import quickfix.field.MinQty;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.NoAffectedOrders;
import quickfix.field.NoPartyIDs;
import quickfix.field.NoRelatedSym;
import quickfix.field.OrdStatus;
import quickfix.field.OrdStatusReqID;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.PartyID;
import quickfix.field.PartyIDSource;
import quickfix.field.PartyRole;
import quickfix.field.Price;
import quickfix.field.QuoteRejectReason;
import quickfix.field.QuoteReqID;
import quickfix.field.QuoteStatus;
import quickfix.field.QuoteType;
import quickfix.field.RefMsgType;
import quickfix.field.RefSeqNum;
import quickfix.field.RefTagID;
import quickfix.field.SecurityGroup;
import quickfix.field.SecurityID;
import quickfix.field.SenderCompID;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.TargetCompID;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TotalAffectedOrders;
import quickfix.field.TransactTime;
import uk.co.real_logic.artio.fixp.SimpleOpenFramingHeader;
import uk.co.real_logic.artio.ilink.BusinessRejectReasons;
import uk.co.real_logic.artio.ilink.ILink3Connection;

public class ILink3MessageConverter {
    // field and message constants
    public static final String MSG_TYPE_ORDER_MASS_ACTION_REPORT = "BZ";
    public static final String MSG_TYPE_PARTY_DETAILS_DEFINITION_REQUEST = "CX";
    public static final String MSG_TYPE_PARTY_DETAILS_DEFINITION_REQUEST_ACK = "CY";
    public static final String MSG_TYPE_PARTY_DETAILS_LIST_REPORT = "CG";
    public static final String MSG_TYPE_PARTY_DETAILS_LIST_REQUEST = "CF";
    public static final int REQUEST_RESULT = 1511;
    public static final int TOT_NUM_PARTIES = 1512;
    public static final int REQUEST_TIME = 5979;
    public static final int EXCHANGE_QUOTE_REQ_ID = 9770;
    public static final int REQUESTING_PARTY_ID_SOURCE = 1659;
    public static final int REQUESTING_PARTY_ROLE = 1660;
    public static final int REQUESTING_PARTY_ID = 1658;
    public static final int NO_REQUESTING_PARTY_IDS = 1657;
    public static final int MASS_CANCEL_REQUEST_TYPE = 6115;
    public static final int ORD_STATUS_REQ_TYPE = 5000;
    public static final int PARTY_DETAIL_ID_SOURCE = 1692;
    public static final int PARTY_DETAIL_DEFINITION_STATUS = 1879;
    public static final int PARTY_DETAIL_REQUEST_STATUS = 1878;
    public static final int CANCEL_TEXT = 2807;
    public static final int RESERVATION_PRICE = 9562;
    public static final int DELAY_TO_TIME = 7552;
    public static final int SPLIT_MSG = 9553;
    public static final int POSS_RETRANS_FLAG = 9765;
    public static final int DELAY_DURATION = 5904;
    public static final int UUID = 39001;
    public static final int REQUEST_TIMESTAMP = 39002;
    public static final int ERROR_CODES = 39012;
    public static final int FROM_SEQ_NO = 39018;
    public static final int MSG_COUNT = 39019;
    public static final int PREVIOUS_SEQ_NO = 39021;
    public static final int PREVIOUS_UUID = 39015;
    public static final int NEXT_SEQ_NO = 39013;
    public static final int SEQ_NUM = 9726;
    public static final int TRD_REG_PUBLICATION_REASON = 2670;
    public static final int TRD_REG_PUBLICATION_TYPE = 2669;
    public static final int PARTY_DETAIL_ROLE = 1693;
    public static final int PARTY_DETAIL_ID = 1691;
    public static final int IDM_SHORT_CODE = 36023;
    public static final int EXECUTOR = 5290;
    public static final int CLEARING_TRADE_PRICE_TYPE = 1598;
    public static final int SELF_MATCH_PREVENTION_INSTRUCTION = 8000;
    public static final int NO_PARTY_DETAILS = 1671;
    public static final int NO_PARTY_UPDATES = 1676;
    public static final int NO_TRD_REG_PUBLICATIONS = 2668;
    public static final int CLEARING_ACCOUNT_TYPE = 1816;
    public static final int CMTA_GIVEUP_CD = 9708;
    public static final int SELF_MATCH_PREVENTION_ID = 2362;
    public static final int AVG_PX_GROUP_ID = 1731;
    public static final int MEMO = 5149;
    public static final int OFM_OVERRIDE = 9768;
    public static final int ORIG_ORDER_USER = 9937;
    public static final int SHORT_SALE_TYPE = 5409;
    public static final int MANAGED_ORDER = 6881;
    public static final int LIQUIDITY_FLAG = 9373;
    public static final int EXECUTION_MODE = 5906;
    public static final int LOCATION = 9537;
    public static final int ORDER_REQUEST_ID = 2422;
    public static final int PARTY_DETAILS_LIST_REQ_ID = 1505;
    public static final int PARTY_DETAILS_LIST_REPORT_ID = 1510;
    public static final int SENDER_ID = 5392;
    public static final int SENDING_TIME_EPOCH = 5297;

    private static final MessageFactory DEFAULT_MESSAGE_FACTORY = new quickfix.fix50.MessageFactory();
    private static volatile MessageFactory messageFactory = DEFAULT_MESSAGE_FACTORY;

    private static final Logger LOG = LoggerFactory.getLogger(ILink3MessageConverter.class);
    private static final int HEADER_SIZE = SimpleOpenFramingHeader.SOFH_LENGTH + MessageHeaderEncoder.ENCODED_LENGTH;

    // TODO create interface to enable overloading of convert method
    // TODO and pass converter to connector to enable use of custom converter

    public static void setMessageFactory(MessageFactory messageFactory) {
	ILink3MessageConverter.messageFactory = messageFactory != null ? messageFactory : DEFAULT_MESSAGE_FACTORY;
    }

    public static Message convertToFIX(MessageDecoderFlyweight decoderFlyweight) {
	final Message fixMessage;
	final String sbeSemanticType = decoderFlyweight.sbeSemanticType();
	if ("N/A".equals(sbeSemanticType) || sbeSemanticType == null) {
	    // default implementation of an unknown message
	    LOG.warn("Encountered unknown message type.");
	    fixMessage = new Message();
	} else {

	    fixMessage = createFixMessage(sbeSemanticType);
	    switch (sbeSemanticType) {
	    case MsgType.BUSINESS_MESSAGE_REJECT:
		mapToFIXBusinessMessageReject((BusinessReject521Decoder) decoderFlyweight, fixMessage);
		break;
	    case MsgType.EXECUTION_REPORT:
		ExecReportConverter.mapToFIXExecReport(decoderFlyweight, fixMessage);
		break;
	    case MsgType.MASS_QUOTE_ACKNOWLEDGEMENT:
		mapToFIXMassQuoteAck((RequestForQuoteAck546Decoder) decoderFlyweight, fixMessage);
		break;
	    case MsgType.ORDER_CANCEL_REJECT:
		mapToFIXOrderCancelOrReplaceReject(decoderFlyweight, fixMessage);
		break;
	    case MSG_TYPE_ORDER_MASS_ACTION_REPORT:
		mapToFIXOrderMassActionReport((OrderMassActionReport562Decoder) decoderFlyweight, fixMessage);
		break;
	    case MSG_TYPE_PARTY_DETAILS_DEFINITION_REQUEST_ACK:
		mapToFIXPartyDetailsDefReqAck((PartyDetailsDefinitionRequestAck519Decoder) decoderFlyweight,
			fixMessage);
		break;
	    case MSG_TYPE_PARTY_DETAILS_LIST_REPORT:
		mapToFIXPartyDetailsListReport((PartyDetailsListReport538Decoder) decoderFlyweight, fixMessage);
		break;
	    default:
		LOG.warn("Could not map to FIX message for decoder {}", decoderFlyweight.getClass().getSimpleName());
	    }
	}

	setCompIDs(fixMessage, true);
	return fixMessage;
    }

    public static MessageEncoderFlyweight getEncoder(Message fixMessage) {

	final Optional<String> msgTypeOptional = fixMessage.getHeader().getOptionalString(MsgType.FIELD);
	if (!msgTypeOptional.isPresent()) {
	    LOG.error("Message {} has no MsgType set", fixMessage);
	    return null;
	}

	// TODO create encoders once and reuse
	// TODO probably we should put this to a msgType-to-encoder map, compare
	// ILink3FIXPMessageHandler
	final String msgType = msgTypeOptional.get();
	switch (msgType) {
	case MsgType.ORDER_SINGLE:
	    return new NewOrderSingle514Encoder();
	case MsgType.ORDER_CANCEL_REQUEST:
	    return new OrderCancelRequest516Encoder();
	case MsgType.ORDER_CANCEL_REPLACE_REQUEST:
	    return new OrderCancelReplaceRequest515Encoder();
	case MsgType.ORDER_STATUS_REQUEST:
	    return new OrderStatusRequest533Encoder();
	case MsgType.ORDER_MASS_ACTION_REQUEST:
	    return new OrderMassActionRequest529Encoder();
	case MsgType.ORDER_MASS_STATUS_REQUEST:
	    return new OrderMassStatusRequest530Encoder();
	case MSG_TYPE_PARTY_DETAILS_DEFINITION_REQUEST:
	    return new PartyDetailsDefinitionRequest518Encoder();
	case MSG_TYPE_PARTY_DETAILS_LIST_REQUEST:
	    return new PartyDetailsListRequest537Encoder();
	case MsgType.QUOTE_REQUEST:
	    return new RequestForQuote543Encoder();
	default:
	    LOG.error("Encountered unknown or unsupported MsgType {}", msgType);
	    return null;
	}
    }

    public static Message convertFromFIXAndSend(Message fixMessage, ILink3Connection connection)
	    throws ILink3ConnectorException {

	// TODO refactor so that we handle everything in one method and
	// do not need to duplicate code in ILink3MessageConverter.getEncoder() and
	// convertFromFIX()

	final MessageEncoderFlyweight messageEncoderFlyweight = ILink3MessageConverter.getEncoder(fixMessage);
	if (messageEncoderFlyweight == null) {
	    throw new ILink3ConnectorException("Could not get encoder for message " + fixMessage);
	}

	int variableLength = getVariableLength(fixMessage, messageEncoderFlyweight);
	long position = connection.tryClaim(messageEncoderFlyweight, variableLength);

	if (position < 0) {
	    // TODO handle more gracefully
	    throw new ILink3ConnectorException("Could not claim buffer for sending message " + fixMessage);
	}

	// fill fields from FIX to SBE message
	try {
	    ILink3MessageConverter.convertFromFIX(fixMessage, messageEncoderFlyweight,
		    connection.nextSentSeqNo() - 1 /* tryClaim already incremented by 1 */);
	} catch (NumberFormatException | InvalidMessage | FieldNotFound e) {
	    throw new ILink3ConnectorException(e);
	}
	return fixMessage;
    }

    public static void convertFromFIX(Message fixMessage, MessageEncoderFlyweight messageEncoderFlyweight, long seqNum)
	    throws InvalidMessage, NumberFormatException, FieldNotFound {

	final Optional<String> msgTypeOptional = fixMessage.getHeader().getOptionalString(MsgType.FIELD);
	if (!msgTypeOptional.isPresent()) {
	    throw new InvalidMessage("Message has no MsgType set", fixMessage);
	}

	final String msgType = msgTypeOptional.get();
	switch (msgType) {
	case MsgType.ORDER_SINGLE:
	    mapNewOrderSingle(fixMessage, (NewOrderSingle514Encoder) messageEncoderFlyweight, seqNum);
	    break;
	case MsgType.ORDER_CANCEL_REQUEST:
	    mapOrderCancelRequest(fixMessage, (OrderCancelRequest516Encoder) messageEncoderFlyweight, seqNum);
	    break;
	case MsgType.ORDER_CANCEL_REPLACE_REQUEST:
	    mapOrderCancelReplaceRequest(fixMessage, (OrderCancelReplaceRequest515Encoder) messageEncoderFlyweight,
		    seqNum);
	    break;
	case MsgType.ORDER_STATUS_REQUEST:
	    mapOrderStatusRequest(fixMessage, (OrderStatusRequest533Encoder) messageEncoderFlyweight, seqNum);
	    break;
	case MsgType.ORDER_MASS_ACTION_REQUEST:
	    mapOrderMassActionRequest(fixMessage, (OrderMassActionRequest529Encoder) messageEncoderFlyweight, seqNum);
	    break;
	case MsgType.ORDER_MASS_STATUS_REQUEST:
	    mapOrderMassStatusRequest(fixMessage, (OrderMassStatusRequest530Encoder) messageEncoderFlyweight, seqNum);
	    break;
	case MSG_TYPE_PARTY_DETAILS_DEFINITION_REQUEST:
	    mapPartyDetailsDefinitionRequest(fixMessage,
		    (PartyDetailsDefinitionRequest518Encoder) messageEncoderFlyweight, seqNum);
	    break;
	case MSG_TYPE_PARTY_DETAILS_LIST_REQUEST:
	    mapPartyDetailsListRequest(fixMessage, (PartyDetailsListRequest537Encoder) messageEncoderFlyweight, seqNum);
	    break;
	case MsgType.QUOTE_REQUEST:
	    mapRequestForQuote(fixMessage, (RequestForQuote543Encoder) messageEncoderFlyweight, seqNum);
	    break;
	default:
	    throw new InvalidMessage("Encountered unknown or unsupported MsgType " + msgType);
	}

	fixMessage.getHeader().setString(MsgSeqNum.FIELD, Long.toString(seqNum));
	logHex(messageEncoderFlyweight);
    }

    static void logHex(final MessageFlyweight messageFlyweight) {

	// TODO if (LOG.isDebugEnabled()) {
	DirectBuffer buffer = messageFlyweight.buffer();
	int limit = messageFlyweight.limit();
	if (limit > 0) {
	    try {
		int headerOffset = messageFlyweight.offset() - HEADER_SIZE;
		byte[] byteArray = new byte[limit - headerOffset];
		buffer.getBytes(headerOffset, byteArray);

		// estimated String length including hex representation and spaces
		// TODO re-use stringbuilder
		StringBuilder sbHex = new StringBuilder(messageFlyweight.encodedLength() * 4);
		for (byte b : byteArray) {
		    sbHex.append(String.format("%02X ", b));
		}
		// TODO LOG.debug
		LOG.info("encoder/decoder={}, templateId={}, hex={}", messageFlyweight.getClass().getSimpleName(),
			messageFlyweight.sbeTemplateId(), sbHex.toString());
	    } catch (Throwable e) {
		LOG.warn(e.getMessage());
	    }
	}
    }

    static void setCompIDs(Message fixMessage, boolean incoming) {
	String sender = incoming ? ILink3Connector.SESSION_ID_ILINK3.getTargetCompID()
		: ILink3Connector.SESSION_ID_ILINK3.getSenderCompID();
	String target = incoming ? ILink3Connector.SESSION_ID_ILINK3.getSenderCompID()
		: ILink3Connector.SESSION_ID_ILINK3.getTargetCompID();
	setString(fixMessage.getHeader(), SenderCompID.FIELD, sender);
	setString(fixMessage.getHeader(), TargetCompID.FIELD, target);
    }

    static Message createFixMessage(final String msgType) {
	Message fixMessage = messageFactory.create(FixVersions.FIXT_SESSION_PREFIX, msgType);
	// MessageFactory will not set MsgType for unknown messages
	fixMessage.getHeader().setString(MsgType.FIELD, msgType);
	return fixMessage;
    }

    private static void mapToFIXBusinessMessageReject(BusinessReject521Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	boolean textEmpty = decoder.text().isEmpty();
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, Text.FIELD, decoder.text(), !textEmpty);
	setString(fixMessage, SENDER_ID, decoder.senderID(), !decoder.senderID().isEmpty());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()),
		BusinessReject521Decoder.partyDetailsListReqIDNullValue() != decoder.partyDetailsListReqID());
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, BusinessRejectRefID.FIELD, Long.toString(decoder.businessRejectRefID()),
		BusinessReject521Decoder.businessRejectRefIDNullValue() != decoder.businessRejectRefID());
	setString(fixMessage, LOCATION, decoder.location(), !decoder.location().isEmpty());
	setString(fixMessage, RefSeqNum.FIELD, Long.toString(decoder.refSeqNum()),
		BusinessReject521Decoder.refSeqNumNullValue() != decoder.refSeqNum());
	setString(fixMessage, RefTagID.FIELD, Integer.toString(decoder.refTagID()),
		BusinessReject521Decoder.refTagIDNullValue() != decoder.refTagID());
	setString(fixMessage, BusinessRejectReason.FIELD, Integer.toString(decoder.businessRejectReason()));
	setString(fixMessage, RefMsgType.FIELD, decoder.refMsgType(), !decoder.refMsgType().isEmpty());
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()),
		ManualOrdInd.NULL_VAL != decoder.manualOrderIndicator());
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());

	if (textEmpty) {
	    setString(fixMessage, Text.FIELD, BusinessRejectReasons.readableReason(decoder.businessRejectReason()));
	} else {
	    setString(fixMessage, Text.FIELD,
		    decoder.text() + " / " + BusinessRejectReasons.readableReason(decoder.businessRejectReason()));
	}
    }

    static void mapToFIXOrderMassActionReport(OrderMassActionReport562Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, MassActionReportID.FIELD, Long.toString(decoder.massActionReportID()));
	setString(fixMessage, MassActionType.FIELD, byteToString(decoder.massActionType()));
	setString(fixMessage, SecurityGroup.FIELD, decoder.securityGroup(), !decoder.securityGroup().isEmpty());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()),
		OrderMassActionReport562Decoder.securityIDNullValue() != decoder.securityID());
	setString(fixMessage, DELAY_DURATION, Integer.toString(decoder.delayDuration()),
		OrderMassActionReport562Decoder.delayDurationNullValue() != decoder.delayDuration());
	setString(fixMessage, MassActionResponse.FIELD, Short.toString(decoder.massActionResponseRaw()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, MassActionScope.FIELD, Short.toString(decoder.massActionScopeRaw()));
	setString(fixMessage, TotalAffectedOrders.FIELD, Long.toString(decoder.totalAffectedOrders()));
	setString(fixMessage, LastFragment.FIELD, shortToFIXBooleanValue(decoder.lastFragment().value()));
	setString(fixMessage, MassActionRejectReason.FIELD, Short.toString(decoder.massActionRejectReason()),
		OrderMassActionReport562Decoder.massActionRejectReasonNullValue() != decoder.massActionRejectReason());
	setString(fixMessage, MarketSegmentID.FIELD, Short.toString(decoder.marketSegmentID()),
		OrderMassActionReport562Decoder.marketSegmentIDNullValue() != decoder.marketSegmentID());
	setString(fixMessage, MASS_CANCEL_REQUEST_TYPE, Short.toString(decoder.massCancelRequestTypeRaw()),
		MassCxlReqTyp.NULL_VAL != decoder.massCancelRequestType());
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()), SideNULL.NULL_VAL != decoder.side());
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()),
		MassActionOrdTyp.NULL_VAL != decoder.ordType());
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()),
		MassCancelTIF.NULL_VAL != decoder.timeInForce());
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		OrderMassActionReport562Decoder.delayToTimeNullValue() != decoder.delayToTime());
	setString(fixMessage, CANCEL_TEXT, decoder.cancelText(), !decoder.cancelText().isEmpty());
	setString(fixMessage, ORIG_ORDER_USER, decoder.origOrderUser(), !decoder.origOrderUser().isEmpty());

	NoAffectedOrdersDecoder noAffectedOrders = decoder.noAffectedOrders();
	if (noAffectedOrders.count() > 0) {
	    for (NoAffectedOrdersDecoder noAffectedOrdersDecoder : noAffectedOrders) {
		Group noAffectedOrdersGroup = new Group(NoAffectedOrders.FIELD, OrigClOrdID.FIELD);
		setString(noAffectedOrdersGroup, OrigClOrdID.FIELD, noAffectedOrdersDecoder.origCIOrdID());
		setString(noAffectedOrdersGroup, AffectedOrderID.FIELD,
			Long.toString(noAffectedOrdersDecoder.affectedOrderID()));
		setString(noAffectedOrdersGroup, CxlQty.FIELD, Long.toString(noAffectedOrdersDecoder.cxlQuantity()));
		fixMessage.addGroup(noAffectedOrdersGroup);
	    }
	}

    }

    static void mapToFIXPartyDetailsListReport(PartyDetailsListReport538Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, AVG_PX_GROUP_ID, decoder.avgPxGroupID(), !decoder.avgPxGroupID().isEmpty());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, PARTY_DETAILS_LIST_REPORT_ID, Long.toString(decoder.partyDetailsListReportID()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, SELF_MATCH_PREVENTION_ID, Long.toString(decoder.selfMatchPreventionID()),
		PartyDetailsListReport538Decoder.selfMatchPreventionIDNullValue() != decoder.selfMatchPreventionID());
	setString(fixMessage, TOT_NUM_PARTIES, Integer.toString(decoder.totNumParties()));
	setString(fixMessage, REQUEST_RESULT, Short.toString(decoder.requestResultRaw()));
	setString(fixMessage, LastFragment.FIELD, shortToFIXBooleanValue(decoder.lastFragment().value()));
	setString(fixMessage, CustOrderCapacity.FIELD, Short.toString(decoder.custOrderCapacity().value()),
		iLinkBinary.CustOrderCapacity.NULL_VAL != decoder.custOrderCapacity());
	setString(fixMessage, CLEARING_ACCOUNT_TYPE, Short.toString(decoder.clearingAccountType().value()),
		ClearingAcctType.NULL_VAL != decoder.clearingAccountType());
	setString(fixMessage, SELF_MATCH_PREVENTION_INSTRUCTION,
		byteToString(decoder.selfMatchPreventionInstruction().value()),
		SMPI.NULL_VAL != decoder.selfMatchPreventionInstruction());
	setString(fixMessage, AvgPxIndicator.FIELD, Short.toString(decoder.avgPxIndicator().value()),
		AvgPxInd.NULL_VAL != decoder.avgPxIndicator());
	setString(fixMessage, CLEARING_TRADE_PRICE_TYPE, Short.toString(decoder.clearingTradePriceType().value()),
		SLEDS.NULL_VAL != decoder.clearingTradePriceType());
	setString(fixMessage, CMTA_GIVEUP_CD, byteToString(decoder.cmtaGiveupCDRaw()),
		CmtaGiveUpCD.NULL_VAL != decoder.cmtaGiveupCD());
	setString(fixMessage, CustOrderHandlingInst.FIELD, byteToString(decoder.custOrderHandlingInstRaw()),
		CustOrdHandlInst.NULL_VAL != decoder.custOrderHandlingInst());
	setString(fixMessage, EXECUTOR, Long.toString(decoder.executor()),
		PartyDetailsListReport538Decoder.executorNullValue() != decoder.executor());
	setString(fixMessage, IDM_SHORT_CODE, Long.toString(decoder.iDMShortCode()),
		PartyDetailsListReport538Decoder.iDMShortCodeNullValue() != decoder.iDMShortCode());
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());

	iLinkBinary.PartyDetailsListReport538Decoder.NoPartyDetailsDecoder noPartyDetails = decoder.noPartyDetails();
	if (noPartyDetails.count() > 0) {
	    for (iLinkBinary.PartyDetailsListReport538Decoder.NoPartyDetailsDecoder noPartyDetailsDecoder : noPartyDetails) {
		Group noPartyDetailsGroup = new Group(NO_PARTY_DETAILS, PARTY_DETAIL_ID);
		setString(noPartyDetailsGroup, PARTY_DETAIL_ID, noPartyDetailsDecoder.partyDetailID());
		setString(noPartyDetailsGroup, PARTY_DETAIL_ID_SOURCE,
			byteToString(noPartyDetailsDecoder.partyDetailIDSource()));
		setString(noPartyDetailsGroup, PARTY_DETAIL_ROLE,
			Integer.toString(noPartyDetailsDecoder.partyDetailRole().value()));
		fixMessage.addGroup(noPartyDetailsGroup);
	    }
	}

	iLinkBinary.PartyDetailsListReport538Decoder.NoTrdRegPublicationsDecoder noTrdRegPublications = decoder
		.noTrdRegPublications();
	if (noTrdRegPublications.count() > 0) {
	    for (iLinkBinary.PartyDetailsListReport538Decoder.NoTrdRegPublicationsDecoder noTrdRegPublicationsDecoder : noTrdRegPublications) {
		Group noTrdRegPublicationsGroup = new Group(NO_TRD_REG_PUBLICATIONS, TRD_REG_PUBLICATION_TYPE);
		setString(noTrdRegPublicationsGroup, TRD_REG_PUBLICATION_TYPE,
			Short.toString(noTrdRegPublicationsDecoder.trdRegPublicationType()));
		setString(noTrdRegPublicationsGroup, TRD_REG_PUBLICATION_REASON,
			Short.toString(noTrdRegPublicationsDecoder.trdRegPublicationReason()));
		fixMessage.addGroup(noTrdRegPublicationsGroup);
	    }
	}
    }

    static void mapToFIXPartyDetailsDefReqAck(PartyDetailsDefinitionRequestAck519Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, MEMO, decoder.memo(), !decoder.memo().isEmpty());
	setString(fixMessage, AVG_PX_GROUP_ID, decoder.avgPxGroupID(), !decoder.avgPxGroupID().isEmpty());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, SELF_MATCH_PREVENTION_ID, Long.toString(decoder.selfMatchPreventionID()),
		PartyDetailsDefinitionRequestAck519Decoder.selfMatchPreventionIDNullValue() != decoder
			.selfMatchPreventionID());
	setString(fixMessage, PARTY_DETAIL_REQUEST_STATUS, Short.toString(decoder.partyDetailRequestStatus()));
	setString(fixMessage, CustOrderCapacity.FIELD, Short.toString(decoder.custOrderCapacity().value()),
		iLinkBinary.CustOrderCapacity.NULL_VAL != decoder.custOrderCapacity());
	setString(fixMessage, CLEARING_ACCOUNT_TYPE, Short.toString(decoder.clearingAccountType().value()),
		ClearingAcctType.NULL_VAL != decoder.clearingAccountType());
	setString(fixMessage, SELF_MATCH_PREVENTION_INSTRUCTION,
		byteToString(decoder.selfMatchPreventionInstruction().value()),
		SMPI.NULL_VAL != decoder.selfMatchPreventionInstruction());
	setString(fixMessage, AvgPxIndicator.FIELD, Short.toString(decoder.avgPxIndicator().value()),
		AvgPxInd.NULL_VAL != decoder.avgPxIndicator());
	setString(fixMessage, CLEARING_TRADE_PRICE_TYPE, Short.toString(decoder.clearingTradePriceType().value()),
		SLEDS.NULL_VAL != decoder.clearingTradePriceType());
	setString(fixMessage, CMTA_GIVEUP_CD, byteToString(decoder.cmtaGiveupCDRaw()),
		CmtaGiveUpCD.NULL_VAL != decoder.cmtaGiveupCD());
	setString(fixMessage, CustOrderHandlingInst.FIELD, byteToString(decoder.custOrderHandlingInstRaw()),
		CustOrdHandlInst.NULL_VAL != decoder.custOrderHandlingInst());

	// NoPartyUpdates is always 1 so ListUpdateAction and
	// PartyDetailDefinitionStatus are flat in the message and not in a
	// repeating group
	setString(fixMessage, ListUpdateAction.FIELD, byteToString(decoder.listUpdateAction().value()));
	setString(fixMessage, PARTY_DETAIL_DEFINITION_STATUS, Short.toString(decoder.partyDetailDefinitionStatus()));

	setString(fixMessage, EXECUTOR, Long.toString(decoder.executor()),
		PartyDetailsDefinitionRequestAck519Decoder.executorNullValue() != decoder.executor());
	setString(fixMessage, IDM_SHORT_CODE, Long.toString(decoder.iDMShortCode()),
		PartyDetailsDefinitionRequestAck519Decoder.iDMShortCodeNullValue() != decoder.iDMShortCode());
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());

	NoPartyDetailsDecoder noPartyDetails = decoder.noPartyDetails();
	if (noPartyDetails.count() > 0) {
	    for (NoPartyDetailsDecoder noPartyDetailsDecoder : noPartyDetails) {
		Group noPartyDetailsGroup = new Group(NO_PARTY_DETAILS, PARTY_DETAIL_ID);
		setString(noPartyDetailsGroup, PARTY_DETAIL_ID, noPartyDetailsDecoder.partyDetailID());
		setString(noPartyDetailsGroup, PARTY_DETAIL_ID_SOURCE,
			byteToString(noPartyDetailsDecoder.partyDetailIDSource()));
		setString(noPartyDetailsGroup, PARTY_DETAIL_ROLE,
			Integer.toString(noPartyDetailsDecoder.partyDetailRole().value()));
		fixMessage.addGroup(noPartyDetailsGroup);
	    }
	}

	NoTrdRegPublicationsDecoder noTrdRegPublications = decoder.noTrdRegPublications();
	if (noTrdRegPublications.count() > 0) {
	    for (NoTrdRegPublicationsDecoder noTrdRegPublicationsDecoder : noTrdRegPublications) {
		Group noTrdRegPublicationsGroup = new Group(NO_TRD_REG_PUBLICATIONS, TRD_REG_PUBLICATION_TYPE);
		setString(noTrdRegPublicationsGroup, TRD_REG_PUBLICATION_TYPE,
			Short.toString(noTrdRegPublicationsDecoder.trdRegPublicationType()));
		setString(noTrdRegPublicationsGroup, TRD_REG_PUBLICATION_REASON,
			Short.toString(noTrdRegPublicationsDecoder.trdRegPublicationReason()));
		fixMessage.addGroup(noTrdRegPublicationsGroup);
	    }
	}
    }

    static void mapToFIXMassQuoteAck(RequestForQuoteAck546Decoder decoder, Message fixMessage) {
	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, Text.FIELD, decoder.text(), !decoder.text().isEmpty());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, EXCHANGE_QUOTE_REQ_ID, decoder.exchangeQuoteReqID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, REQUEST_TIME, Long.toString(decoder.requestTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, QuoteReqID.FIELD, Long.toString(decoder.quoteReqID()));
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, QuoteRejectReason.FIELD, Integer.toString(decoder.quoteRejectReason()),
		RequestForQuoteAck546Decoder.quoteRejectReasonNullValue() != decoder.quoteRejectReason());
	setString(fixMessage, DELAY_DURATION, Integer.toString(decoder.delayDuration()),
		RequestForQuoteAck546Decoder.delayDurationNullValue() != decoder.delayDuration());
	setString(fixMessage, QuoteStatus.FIELD, Short.toString(decoder.quoteStatus().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		RequestForQuoteAck546Decoder.delayToTimeNullValue() != decoder.delayToTime());
    }

    static void mapToFIXOrderCancelOrReplaceReject(MessageDecoderFlyweight decoder, Message fixMessage) {

	switch (decoder.sbeTemplateId()) {
	case OrderCancelReject535Decoder.TEMPLATE_ID:
	    mapOrderCancelReject((OrderCancelReject535Decoder) decoder, fixMessage);
	    break;
	case OrderCancelReplaceReject536Decoder.TEMPLATE_ID:
	    mapOrderReplaceCancelReject((OrderCancelReplaceReject536Decoder) decoder, fixMessage);
	    break;
	}
    }

    private static void mapOrderCancelReject(OrderCancelReject535Decoder decoder, Message fixMessage) {
	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, Text.FIELD, decoder.text(), !decoder.text().isEmpty());
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, CxlRejResponseTo.FIELD, byteToString(decoder.cxlRejResponseTo()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, CxlRejReason.FIELD, Integer.toString(decoder.cxlRejReason()));
	setString(fixMessage, DELAY_DURATION, Integer.toString(decoder.delayDuration()),
		OrderCancelReject535Decoder.delayDurationNullValue() != decoder.delayDuration());
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		OrderCancelReject535Decoder.delayToTimeNullValue() != decoder.delayToTime());
    }

    private static void mapOrderReplaceCancelReject(OrderCancelReplaceReject536Decoder decoder, Message fixMessage) {
	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, Text.FIELD, decoder.text(), !decoder.text().isEmpty());
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, CxlRejReason.FIELD, Integer.toString(decoder.cxlRejReason()));
	setString(fixMessage, DELAY_DURATION, Integer.toString(decoder.delayDuration()),
		OrderCancelReplaceReject536Decoder.delayDurationNullValue() != decoder.delayDuration());
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, CxlRejResponseTo.FIELD, byteToString(decoder.cxlRejResponseTo()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		OrderCancelReplaceReject536Decoder.delayToTimeNullValue() != decoder.delayToTime());
    }

    static void setMsgSeqNum(String seqNum, Message fixMessage) {
	// seqNum in QFJ is actually still integer but max
	// value for iLink3 is 999999999 which is 1 short of
	// billion
	fixMessage.getHeader().setString(MsgSeqNum.FIELD, seqNum);
    }

    static String shortToFIXBooleanValue(short value) {
	return value == 0 ? "N" : "Y";
    }

    static String byteToString(byte byteValue) {
	byte[] byteArray = new byte[] { byteValue };
	return new String(byteArray, StandardCharsets.US_ASCII);
    }

    static void setString(FieldMap fieldMap, int field, String value) {
	try {
	    fieldMap.setString(field, value);
	} catch (Exception e) {
	    LOG.warn("Exception when setting field {} with value {}", field, value, e);
	}
    }

    static void setString(FieldMap fieldMap, int field, String value, boolean condition) {
	if (condition) {
	    setString(fieldMap, field, value);
	}
    }

    // TODO sometimes we do not need to explicitly map from QFJ constants to iLink3
    // constants
    // but could convert the QFJ char or int directly to its byte value

    private static void mapPartyDetailsDefinitionRequest(Message fixMessage,
	    PartyDetailsDefinitionRequest518Encoder partyDetailsDefinitionRequest, long seqNum)
	    throws NumberFormatException, FieldNotFound {

	partyDetailsDefinitionRequest
		.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));
	partyDetailsDefinitionRequest.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));

	char listUpdateAction = fixMessage.getChar(ListUpdateAction.FIELD);
	switch (listUpdateAction) {
	case 'A':
	    partyDetailsDefinitionRequest.listUpdateAction(ListUpdAct.Add);
	    break;
	case 'D':
	    partyDetailsDefinitionRequest.listUpdateAction(ListUpdAct.Delete);
	    break;
	}

	partyDetailsDefinitionRequest.seqNum(seqNum);

	if (fixMessage.isSetField(MEMO)) {
	    partyDetailsDefinitionRequest.memo(fixMessage.getString(MEMO));
	} else {
	    partyDetailsDefinitionRequest.memo(0, PartyDetailsDefinitionRequest518Encoder.memoNullValue());
	}

	if (fixMessage.isSetField(AVG_PX_GROUP_ID)) {
	    partyDetailsDefinitionRequest.avgPxGroupID(fixMessage.getString(AVG_PX_GROUP_ID));
	} else {
	    partyDetailsDefinitionRequest.avgPxGroupID(0,
		    PartyDetailsDefinitionRequest518Encoder.avgPxGroupIDNullValue());
	}

	if (fixMessage.isSetField(SELF_MATCH_PREVENTION_ID)) {
	    partyDetailsDefinitionRequest
		    .selfMatchPreventionID(Long.valueOf(fixMessage.getString(SELF_MATCH_PREVENTION_ID)));
	} else {
	    partyDetailsDefinitionRequest
		    .selfMatchPreventionID(PartyDetailsDefinitionRequest518Encoder.selfMatchPreventionIDNullValue());
	}

	if (fixMessage.isSetField(CMTA_GIVEUP_CD)) {
	    char cmtaGiveUpCD = fixMessage.getChar(CMTA_GIVEUP_CD);
	    switch (cmtaGiveUpCD) {
	    case 'G':
		partyDetailsDefinitionRequest.cmtaGiveupCD(CmtaGiveUpCD.GiveUp);
		break;
	    case 'S':
		partyDetailsDefinitionRequest.cmtaGiveupCD(CmtaGiveUpCD.SGXoffset);
		break;
	    }
	} else {
	    putNullByteForCharNull(partyDetailsDefinitionRequest,
		    PartyDetailsDefinitionRequest518Encoder.cmtaGiveupCDEncodingOffset());
//	    partyDetailsDefinitionRequest.cmtaGiveupCD(CmtaGiveUpCD.NULL_VAL);
	}

	if (fixMessage.isSetField(CustOrderCapacity.FIELD)) {
	    int custOrderCapacity = fixMessage.getInt(CustOrderCapacity.FIELD);
	    switch (custOrderCapacity) {
	    case CustOrderCapacity.MEMBER_TRADING_FOR_THEIR_OWN_ACCOUNT:
		partyDetailsDefinitionRequest
			.custOrderCapacity(iLinkBinary.CustOrderCapacity.Membertradingfortheirownaccount);
		break;
	    case CustOrderCapacity.CLEARING_FIRM_TRADING_FOR_ITS_PROPRIETARY_ACCOUNT:
		partyDetailsDefinitionRequest
			.custOrderCapacity(iLinkBinary.CustOrderCapacity.Memberfirmtradingforitsproprietaryaccount);
		break;
	    case CustOrderCapacity.MEMBER_TRADING_FOR_ANOTHER_MEMBER:
		partyDetailsDefinitionRequest
			.custOrderCapacity(iLinkBinary.CustOrderCapacity.Membertradingforanothermemberornonmember);
		break;
	    case CustOrderCapacity.ALL_OTHER:
		partyDetailsDefinitionRequest.custOrderCapacity(iLinkBinary.CustOrderCapacity.Allother);
		break;
	    }
	} else {
	    partyDetailsDefinitionRequest.custOrderCapacity(iLinkBinary.CustOrderCapacity.NULL_VAL);
	}

	if (fixMessage.isSetField(CLEARING_ACCOUNT_TYPE)) {
	    int clearingAccountType = fixMessage.getInt(CLEARING_ACCOUNT_TYPE);
	    switch (clearingAccountType) {
	    case 0:
		partyDetailsDefinitionRequest.clearingAccountType(ClearingAcctType.Customer);
		break;
	    case 1:
		partyDetailsDefinitionRequest.clearingAccountType(ClearingAcctType.Firm);
		break;
	    default:
		partyDetailsDefinitionRequest.clearingAccountType(ClearingAcctType.NULL_VAL);
		break;
	    }
	} else {
	    partyDetailsDefinitionRequest.clearingAccountType(ClearingAcctType.NULL_VAL);
	}

	if (fixMessage.isSetField(SELF_MATCH_PREVENTION_INSTRUCTION)) {
	    char selfMatchPreventionInstruction = fixMessage.getChar(SELF_MATCH_PREVENTION_INSTRUCTION);
	    switch (selfMatchPreventionInstruction) {
	    case 'N':
		partyDetailsDefinitionRequest.selfMatchPreventionInstruction(SMPI.CancelNewest);
		break;
	    case 'O':
		partyDetailsDefinitionRequest.selfMatchPreventionInstruction(SMPI.CancelOldest);
		break;
	    default:
		partyDetailsDefinitionRequest.selfMatchPreventionInstruction(SMPI.NULL_VAL);
		break;
	    }
	} else {
	    putNullByteForCharNull(partyDetailsDefinitionRequest,
		    PartyDetailsDefinitionRequest518Encoder.selfMatchPreventionInstructionEncodingOffset());
	}

	if (fixMessage.isSetField(AvgPxIndicator.FIELD)) {
	    int avgPxIndicator = fixMessage.getInt(AvgPxIndicator.FIELD);
	    switch (avgPxIndicator) {
	    case AvgPxIndicator.NO_AVERAGE_PRICING:
		partyDetailsDefinitionRequest.avgPxIndicator(AvgPxInd.NoAveragePricing);
		break;
	    case AvgPxIndicator.TRADE_IS_PART_OF_AN_AVERAGE_PRICE_GROUP_IDENTIFIED_BY_THE_TRADELINKID:
		partyDetailsDefinitionRequest
			.avgPxIndicator(AvgPxInd.TradeispartofanAveragePriceGroupIdentifiedbytheAvgPxGrpID);
		break;
	    case 3:
		partyDetailsDefinitionRequest.avgPxIndicator(AvgPxInd.TradeispartofaNotionalValueAveragePriceGroup);
		break;
	    default:
		partyDetailsDefinitionRequest.avgPxIndicator(AvgPxInd.NULL_VAL);
		break;
	    }
	} else {
	    partyDetailsDefinitionRequest.avgPxIndicator(AvgPxInd.NULL_VAL);
	}

	if (fixMessage.isSetField(CLEARING_TRADE_PRICE_TYPE)) {
	    int clearingTradePriceType = fixMessage.getInt(CLEARING_TRADE_PRICE_TYPE);
	    switch (clearingTradePriceType) {
	    case 0:
		partyDetailsDefinitionRequest.clearingTradePriceType(SLEDS.TradeClearingatExecutionPrice);
		break;
	    case 1:
		partyDetailsDefinitionRequest.clearingTradePriceType(SLEDS.TradeClearingatAlternateClearingPrice);
		break;
	    default:
		partyDetailsDefinitionRequest.clearingTradePriceType(SLEDS.NULL_VAL);
		break;
	    }
	} else {
	    putNullByteForCharNull(partyDetailsDefinitionRequest,
		    PartyDetailsDefinitionRequest518Encoder.clearingTradePriceTypeEncodingOffset());
//	    partyDetailsDefinitionRequest.clearingTradePriceType(SLEDS.NULL_VAL);
	}

	if (fixMessage.isSetField(CustOrderHandlingInst.FIELD)) {
	    char custOrderHandlingInst = fixMessage.getChar(CustOrderHandlingInst.FIELD);
	    switch (custOrderHandlingInst) {
	    case 'W':
		partyDetailsDefinitionRequest.custOrderHandlingInst(CustOrdHandlInst.DeskElectronic);
		break;
	    case 'Y':
		partyDetailsDefinitionRequest.custOrderHandlingInst(CustOrdHandlInst.ClientElectronic);
		break;
	    case 'C':
		partyDetailsDefinitionRequest.custOrderHandlingInst(CustOrdHandlInst.FCMprovidedscreen);
		break;
	    case 'G':
		partyDetailsDefinitionRequest.custOrderHandlingInst(CustOrdHandlInst.FCMAPIorFIX);
		break;
	    case 'H':
		partyDetailsDefinitionRequest.custOrderHandlingInst(CustOrdHandlInst.AlgoEngine);
		break;
	    case 'D':
		partyDetailsDefinitionRequest.custOrderHandlingInst(CustOrdHandlInst.Otherprovidedscreen);
		break;
	    default:
		partyDetailsDefinitionRequest.custOrderHandlingInst(CustOrdHandlInst.NULL_VAL);
		break;
	    }
	} else {
	    putNullByteForCharNull(partyDetailsDefinitionRequest,
		    PartyDetailsDefinitionRequest518Encoder.custOrderHandlingInstEncodingOffset());
//	    partyDetailsDefinitionRequest.custOrderHandlingInst(CustOrdHandlInst.NULL_VAL);
	}

	if (fixMessage.isSetField(EXECUTOR)) {
	    partyDetailsDefinitionRequest.executor(Long.valueOf(fixMessage.getString(EXECUTOR)));
	} else {
	    partyDetailsDefinitionRequest.executor(PartyDetailsDefinitionRequest518Encoder.executorNullValue());
	}

	if (fixMessage.isSetField(IDM_SHORT_CODE)) {
	    partyDetailsDefinitionRequest.iDMShortCode(Long.valueOf(fixMessage.getString(IDM_SHORT_CODE)));
	} else {
	    partyDetailsDefinitionRequest.iDMShortCode(PartyDetailsDefinitionRequest518Encoder.iDMShortCodeNullValue());
	}

	Group noPartyUpdatesGroup = fixMessage.getGroup(1, NO_PARTY_UPDATES);
	int noPartyDetailsCount = noPartyUpdatesGroup.getGroupCount(NO_PARTY_DETAILS);
	NoPartyDetailsEncoder noPartyDetailsEncoder = partyDetailsDefinitionRequest
		.noPartyDetailsCount(noPartyDetailsCount);
	for (int i = 1; i <= noPartyDetailsCount; i++) {
	    Group noPartyDetailsGroup = noPartyUpdatesGroup.getGroup(i, NO_PARTY_DETAILS);
	    String partyDetailID = noPartyDetailsGroup.getString(PARTY_DETAIL_ID);
	    int partyDetailRoleInt = noPartyDetailsGroup.getInt(PARTY_DETAIL_ROLE);
	    PartyDetailRole partyDetailRole;

	    switch (partyDetailRoleInt) {
	    case 96:
		partyDetailRole = PartyDetailRole.TakeUpFirm;
		break;
	    case 1000:
		partyDetailRole = PartyDetailRole.TakeUpAccount;
		break;
	    case 1:
		partyDetailRole = PartyDetailRole.ExecutingFirm;
		break;
	    case 118:
		partyDetailRole = PartyDetailRole.Operator;
		break;
	    case 24:
		partyDetailRole = PartyDetailRole.CustomerAccount;
		break;
	    default:
		partyDetailRole = PartyDetailRole.NULL_VAL;
		break;
	    }

	    noPartyDetailsEncoder.next().partyDetailID(partyDetailID).partyDetailRole(partyDetailRole)
		    .partyDetailIDSource();
	}

	// there can be only one
	if (fixMessage.getGroupCount(NO_TRD_REG_PUBLICATIONS) == 1) {
	    Group noTrdRegPublicationsGroup = fixMessage.getGroup(1, NO_TRD_REG_PUBLICATIONS);
	    NoTrdRegPublicationsEncoder noTrdRegPublicationsEncoder = partyDetailsDefinitionRequest
		    .noTrdRegPublicationsCount(1);

	    noTrdRegPublicationsEncoder.next()
		    .trdRegPublicationType((short) noTrdRegPublicationsGroup.getInt(TRD_REG_PUBLICATION_TYPE))
		    .trdRegPublicationReason((short) noTrdRegPublicationsGroup.getInt(TRD_REG_PUBLICATION_REASON));
	} else {
	    partyDetailsDefinitionRequest.noTrdRegPublicationsCount(0);
	}
    }

    /**
     * Workaround for CME schema bug using byte 48 instead of byte 0 for NULL chars.
     */
    private static void putNullByteForCharNull(MessageEncoderFlyweight encoder, int fieldOffset) {
	encoder.buffer().putByte(encoder.offset() + fieldOffset, (byte) 0);
    }

    private static void mapOrderStatusRequest(Message fixMessage, OrderStatusRequest533Encoder orderStatusRequest,
	    long seqNum) throws NumberFormatException, FieldNotFound {

	orderStatusRequest.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));
	orderStatusRequest.ordStatusReqID(Long.valueOf(fixMessage.getString(OrdStatusReqID.FIELD)));
	boolean manualOrderIndicator = fixMessage.getBoolean(ManualOrderIndicator.FIELD);
	if (manualOrderIndicator) {
	    orderStatusRequest.manualOrderIndicator(ManualOrdIndReq.Manual);
	} else {
	    orderStatusRequest.manualOrderIndicator(ManualOrdIndReq.Automated);
	}
	orderStatusRequest.seqNum(seqNum);
	orderStatusRequest.senderID(fixMessage.getString(SENDER_ID));
	orderStatusRequest.orderID(Long.valueOf(fixMessage.getString(OrderID.FIELD)));
	orderStatusRequest.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));
	orderStatusRequest.location(fixMessage.getString(LOCATION));
    }

    private static void mapRequestForQuote(Message fixMessage, RequestForQuote543Encoder requestForQuote, long seqNum)
	    throws NumberFormatException, FieldNotFound {

	requestForQuote.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));
	requestForQuote.quoteReqID(Long.valueOf(fixMessage.getString(QuoteReqID.FIELD)));
	boolean manualOrderIndicator = fixMessage.getBoolean(ManualOrderIndicator.FIELD);
	if (manualOrderIndicator) {
	    requestForQuote.manualOrderIndicator(ManualOrdIndReq.Manual);
	} else {
	    requestForQuote.manualOrderIndicator(ManualOrdIndReq.Automated);
	}
	requestForQuote.seqNum(seqNum);
	requestForQuote.senderID(fixMessage.getString(SENDER_ID));
	requestForQuote.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));
	requestForQuote.location(fixMessage.getString(LOCATION));
	if (fixMessage.isSetField(QuoteType.FIELD)) {
	    requestForQuote.quoteType(QuoteTyp.get((short) fixMessage.getInt(QuoteType.FIELD)));
	} else {
	    requestForQuote.quoteType(QuoteTyp.NULL_VAL);
	}

	int groupCount = fixMessage.getGroupCount(NoRelatedSym.FIELD);
	NoRelatedSymEncoder noRelatedSymEncoder = requestForQuote.noRelatedSymCount(groupCount);
	if (groupCount > 0) {
	    for (int i = 1; i <= groupCount; i++) {
		Group noRelatedSymGroup = fixMessage.getGroup(i, NoRelatedSym.FIELD);

		NoRelatedSymEncoder next = noRelatedSymEncoder.next();
		next.securityID(noRelatedSymGroup.getInt(SecurityID.FIELD));

		if (noRelatedSymGroup.isSetField(OrderQty.FIELD)) {
		    next.orderQty(noRelatedSymGroup.getInt(OrderQty.FIELD));
		} else {
		    next.orderQty(NoRelatedSymEncoder.orderQtyNullValue());
		}
		if (noRelatedSymGroup.isSetField(Side.FIELD)) {
		    next.side(RFQSide.get((short) noRelatedSymGroup.getInt(Side.FIELD)));
		} else {
		    next.side(RFQSide.NULL_VAL);
		}
	    }
	}
    }

    private static void mapPartyDetailsListRequest(Message fixMessage,
	    PartyDetailsListRequest537Encoder partyDetailsListRequest, long seqNum)
	    throws NumberFormatException, FieldNotFound {

	partyDetailsListRequest.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));
	partyDetailsListRequest.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));
	partyDetailsListRequest.seqNum(seqNum);

	int groupCount = fixMessage.getGroupCount(NO_REQUESTING_PARTY_IDS);
	NoRequestingPartyIDsEncoder noRequestingPartyIDsEncoder = partyDetailsListRequest
		.noRequestingPartyIDsCount(groupCount);
	if (groupCount > 0) {
	    for (int i = 1; i <= groupCount; i++) {
		Group noRequestingPartyIDsGroup = fixMessage.getGroup(i, NO_REQUESTING_PARTY_IDS);

		NoRequestingPartyIDsEncoder next = noRequestingPartyIDsEncoder.next();
		next.requestingPartyID(noRequestingPartyIDsGroup.getString(REQUESTING_PARTY_ID));
		next.requestingPartyIDSource((byte) noRequestingPartyIDsGroup.getChar(REQUESTING_PARTY_ID_SOURCE));
		next.requestingPartyRole((byte) noRequestingPartyIDsGroup.getChar(REQUESTING_PARTY_ROLE));
	    }
	}

	groupCount = fixMessage.getGroupCount(NoPartyIDs.FIELD);
	NoPartyIDsEncoder noPartyIDsEncoder = partyDetailsListRequest.noPartyIDsCount(groupCount);
	if (groupCount > 0) {
	    for (int i = 1; i <= groupCount; i++) {
		Group noPartyIDsGroup = fixMessage.getGroup(i, NoPartyIDs.FIELD);

		NoPartyIDsEncoder next = noPartyIDsEncoder.next();
		next.partyID(Long.valueOf(noPartyIDsGroup.getString(PartyID.FIELD)));
		next.partyIDSource((byte) noPartyIDsGroup.getChar(PartyIDSource.FIELD));
		next.partyRole(noPartyIDsGroup.getInt(PartyRole.FIELD));
	    }
	}
    }

    private static void mapOrderMassActionRequest(Message fixMessage,
	    OrderMassActionRequest529Encoder orderMassActionRequest, long seqNum)
	    throws NumberFormatException, FieldNotFound {

	orderMassActionRequest.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));
	orderMassActionRequest.orderRequestID(Long.valueOf(fixMessage.getString(ORDER_REQUEST_ID)));
	boolean manualOrderIndicator = fixMessage.getBoolean(ManualOrderIndicator.FIELD);
	if (manualOrderIndicator) {
	    orderMassActionRequest.manualOrderIndicator(ManualOrdIndReq.Manual);
	} else {
	    orderMassActionRequest.manualOrderIndicator(ManualOrdIndReq.Automated);
	}
	orderMassActionRequest.seqNum(seqNum);
	orderMassActionRequest.senderID(fixMessage.getString(SENDER_ID));
	// massactionType is constant 3
	orderMassActionRequest.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));
	if (fixMessage.isSetField(SecurityGroup.FIELD)) {
	    orderMassActionRequest.securityGroup(fixMessage.getString(SecurityGroup.FIELD));
	}
	orderMassActionRequest.location(fixMessage.getString(LOCATION));
	if (fixMessage.isSetField(SecurityID.FIELD)) {
	    orderMassActionRequest.securityID(fixMessage.getInt(SecurityID.FIELD));
	}
	orderMassActionRequest
		.massActionScope(iLinkBinary.MassActionScope.get((short) fixMessage.getInt(MassActionScope.FIELD)));
	if (fixMessage.isSetField(MarketSegmentID.FIELD)) {
	    orderMassActionRequest.marketSegmentID((short) fixMessage.getInt(MarketSegmentID.FIELD));
	}
	if (fixMessage.isSetField(MASS_CANCEL_REQUEST_TYPE)) {
	    orderMassActionRequest
		    .massCancelRequestType(MassCxlReqTyp.get((short) fixMessage.getInt(MASS_CANCEL_REQUEST_TYPE)));
	} else {
	    orderMassActionRequest.massCancelRequestType(MassCxlReqTyp.NULL_VAL);
	}
	if (fixMessage.isSetField(Side.FIELD)) {
	    orderMassActionRequest.side(SideNULL.get((short) fixMessage.getInt(Side.FIELD)));
	} else {
	    orderMassActionRequest.side(SideNULL.NULL_VAL);
	}
	if (fixMessage.isSetField(OrdType.FIELD)) {
	    orderMassActionRequest.ordType(MassActionOrdTyp.get((byte) fixMessage.getInt(OrdType.FIELD)));
	} else {
	    orderMassActionRequest.ordType(MassActionOrdTyp.NULL_VAL);
	}
	if (fixMessage.isSetField(TimeInForce.FIELD)) {
	    orderMassActionRequest.timeInForce(MassCancelTIF.get((short) fixMessage.getInt(TimeInForce.FIELD)));
	} else {
	    orderMassActionRequest.timeInForce(MassCancelTIF.NULL_VAL);
	}
	if (fixMessage.isSetField(LIQUIDITY_FLAG)) {
	    boolean liquidityFlag = fixMessage.getBoolean(LIQUIDITY_FLAG);
	    if (liquidityFlag) {
		orderMassActionRequest.liquidityFlag(BooleanNULL.True);
	    } else {
		orderMassActionRequest.liquidityFlag(BooleanNULL.False);
	    }
	} else {
	    orderMassActionRequest.liquidityFlag(BooleanNULL.NULL_VAL);
	}
	if (fixMessage.isSetField(ORIG_ORDER_USER)) {
	    orderMassActionRequest.origOrderUser(fixMessage.getString(ORIG_ORDER_USER));
	}
    }

    private static void mapOrderMassStatusRequest(Message fixMessage,
	    OrderMassStatusRequest530Encoder orderMassStatusRequest, long seqNum)
	    throws NumberFormatException, FieldNotFound {

	orderMassStatusRequest.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));
	orderMassStatusRequest.massStatusReqID(Long.valueOf(fixMessage.getString(MassStatusReqID.FIELD)));
	boolean manualOrderIndicator = fixMessage.getBoolean(ManualOrderIndicator.FIELD);
	if (manualOrderIndicator) {
	    orderMassStatusRequest.manualOrderIndicator(ManualOrdIndReq.Manual);
	} else {
	    orderMassStatusRequest.manualOrderIndicator(ManualOrdIndReq.Automated);
	}
	orderMassStatusRequest.seqNum(seqNum);
	orderMassStatusRequest.senderID(fixMessage.getString(SENDER_ID));
	orderMassStatusRequest.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));
	if (fixMessage.isSetField(SecurityGroup.FIELD)) {
	    orderMassStatusRequest.securityGroup(fixMessage.getString(SecurityGroup.FIELD));
	}
	orderMassStatusRequest.location(fixMessage.getString(LOCATION));
	if (fixMessage.isSetField(SecurityID.FIELD)) {
	    orderMassStatusRequest.securityID(fixMessage.getInt(SecurityID.FIELD));
	}
	orderMassStatusRequest
		.massStatusReqType(MassStatusReqTyp.get((short) fixMessage.getInt(MassStatusReqType.FIELD)));
	if (fixMessage.isSetField(ORD_STATUS_REQ_TYPE)) {
	    orderMassStatusRequest
		    .ordStatusReqType(MassStatusOrdTyp.get((short) fixMessage.getInt(ORD_STATUS_REQ_TYPE)));
	} else {
	    orderMassStatusRequest.ordStatusReqType(MassStatusOrdTyp.NULL_VAL);
	}
	if (fixMessage.isSetField(TimeInForce.FIELD)) {
	    orderMassStatusRequest.timeInForce(MassStatusTIF.get((short) fixMessage.getInt(TimeInForce.FIELD)));
	} else {
	    orderMassStatusRequest.timeInForce(MassStatusTIF.NULL_VAL);
	}
	if (fixMessage.isSetField(MarketSegmentID.FIELD)) {
	    orderMassStatusRequest.marketSegmentID((short) fixMessage.getInt(MarketSegmentID.FIELD));
	}
    }

    private static void mapOrderCancelReplaceRequest(Message fixMessage,
	    OrderCancelReplaceRequest515Encoder orderCancelReplaceRequest, long seqNum)
	    throws NumberFormatException, FieldNotFound {

	if (fixMessage.isSetField(Price.FIELD)) {
	    orderCancelReplaceRequest.price().mantissa(Long.valueOf(fixMessage.getString(Price.FIELD)));
	} else {
	    orderCancelReplaceRequest.price().mantissa(PRICENULL9Encoder.mantissaNullValue());
	}

	orderCancelReplaceRequest.orderQty(fixMessage.getInt(OrderQty.FIELD));
	orderCancelReplaceRequest.securityID(fixMessage.getInt(SecurityID.FIELD));

	char side = fixMessage.getChar(Side.FIELD);
	orderCancelReplaceRequest.side(side == Side.BUY ? SideReq.Buy : SideReq.Sell);

	orderCancelReplaceRequest.seqNum(seqNum);
	orderCancelReplaceRequest.senderID(fixMessage.getString(SENDER_ID));
	orderCancelReplaceRequest.clOrdID(fixMessage.getString(ClOrdID.FIELD));
	orderCancelReplaceRequest.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));

	if (fixMessage.isSetField(OrderID.FIELD)) {
	    orderCancelReplaceRequest.orderID(Long.valueOf(fixMessage.getString(OrderID.FIELD)));
	} else {
	    orderCancelReplaceRequest.orderID(OrderCancelReplaceRequest515Encoder.orderIDNullValue());
	}

	if (fixMessage.isSetField(StopPx.FIELD)) {
	    orderCancelReplaceRequest.stopPx().mantissa(Long.valueOf(fixMessage.getString(StopPx.FIELD)));
	} else {
	    orderCancelReplaceRequest.stopPx().mantissa(PRICENULL9Encoder.mantissaNullValue());
	}

	orderCancelReplaceRequest.orderRequestID(Long.valueOf(fixMessage.getString(ORDER_REQUEST_ID)));
	orderCancelReplaceRequest.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));
	orderCancelReplaceRequest.location(fixMessage.getString(LOCATION));

	if (fixMessage.isSetField(MinQty.FIELD)) {
	    orderCancelReplaceRequest.minQty(fixMessage.getInt(MinQty.FIELD));
	} else {
	    orderCancelReplaceRequest.minQty(OrderCancelReplaceRequest515Encoder.minQtyNullValue());
	}

	if (fixMessage.isSetField(DisplayQty.FIELD)) {
	    orderCancelReplaceRequest.displayQty(fixMessage.getInt(DisplayQty.FIELD));
	} else {
	    orderCancelReplaceRequest.displayQty(OrderCancelReplaceRequest515Encoder.displayQtyNullValue());
	}

	if (fixMessage.isSetField(ExpireDate.FIELD)) {
	    orderCancelReplaceRequest.expireDate(fixMessage.getInt(ExpireDate.FIELD));
	} else {
	    orderCancelReplaceRequest.expireDate(NewOrderSingle514Encoder.expireDateNullValue());
	}

	char ordType = fixMessage.getChar(OrdType.FIELD);
	switch (ordType) {
	case OrdType.MARKET:
	    orderCancelReplaceRequest.ordType(OrderTypeReq.MarketwithProtection);
	    break;
	case OrdType.LIMIT:
	    orderCancelReplaceRequest.ordType(OrderTypeReq.Limit);
	    break;
	case OrdType.STOP_STOP_LOSS:
	    orderCancelReplaceRequest.ordType(OrderTypeReq.StopwithProtection);
	    break;
	case OrdType.STOP_LIMIT:
	    orderCancelReplaceRequest.ordType(OrderTypeReq.StopLimit);
	    break;
	case OrdType.MARKET_WITH_LEFT_OVER_AS_LIMIT:
	    orderCancelReplaceRequest.ordType(OrderTypeReq.MarketWithLeftoverAsLimit);
	    break;
	}

	// we need to get it as String due to special value 99
	String timeInForce = fixMessage.getString(TimeInForce.FIELD);
	switch (timeInForce) {
	case "" + TimeInForce.DAY:
	    orderCancelReplaceRequest.timeInForce(iLinkBinary.TimeInForce.Day);
	    break;
	case "" + TimeInForce.GOOD_TILL_CANCEL:
	    orderCancelReplaceRequest.timeInForce(iLinkBinary.TimeInForce.GoodTillCancel);
	    break;
	case "" + TimeInForce.IMMEDIATE_OR_CANCEL:
	    orderCancelReplaceRequest.timeInForce(iLinkBinary.TimeInForce.FillAndKill);
	    break;
	case "" + TimeInForce.FILL_OR_KILL:
	    orderCancelReplaceRequest.timeInForce(iLinkBinary.TimeInForce.FillOrKill);
	    break;
	case "" + TimeInForce.GOOD_TILL_DATE:
	    orderCancelReplaceRequest.timeInForce(iLinkBinary.TimeInForce.GoodTillDate);
	    break;
	case "99": // GFS (good for session)
	    orderCancelReplaceRequest.timeInForce(iLinkBinary.TimeInForce.GoodForSession);
	    break;

	}

	boolean manualOrderIndicator = fixMessage.getBoolean(ManualOrderIndicator.FIELD);
	if (manualOrderIndicator) {
	    orderCancelReplaceRequest.manualOrderIndicator(ManualOrdIndReq.Manual);
	} else {
	    orderCancelReplaceRequest.manualOrderIndicator(ManualOrdIndReq.Automated);
	}

	boolean ofmOverride = fixMessage.getBoolean(OFM_OVERRIDE);
	if (ofmOverride) {
	    orderCancelReplaceRequest.oFMOverride(OFMOverrideReq.Enabled);
	} else {
	    orderCancelReplaceRequest.oFMOverride(OFMOverrideReq.Disabled);
	}

	orderCancelReplaceRequest.execInst().oB(false).nH(false).aON(false);
	if (fixMessage.isSetField(ExecInst.FIELD)) {
	    String execInst = fixMessage.getString(ExecInst.FIELD);
	    if (execInst.contains("" + ExecInst.ALL_OR_NONE_AON)) {
		orderCancelReplaceRequest.execInst().aON(true);
	    }
	    if (execInst.contains("" + ExecInst.BEST_EXECUTION)) {
		orderCancelReplaceRequest.execInst().oB(true);
	    }
	    if (execInst.contains("" + ExecInst.NOT_HELD)) {
		orderCancelReplaceRequest.execInst().nH(true);
	    }
	}

	if (fixMessage.isSetField(EXECUTION_MODE)) {
	    char executionMode = fixMessage.getChar(EXECUTION_MODE);
	    switch (executionMode) {
	    case 'A':
		orderCancelReplaceRequest.executionMode(ExecMode.Aggressive);
		break;
	    case 'P':
		orderCancelReplaceRequest.executionMode(ExecMode.Passive);
		break;
	    default:
		orderCancelReplaceRequest.executionMode(ExecMode.NULL_VAL);
		break;
	    }
	} else {
	    putNullByteForCharNull(orderCancelReplaceRequest,
		    OrderCancelReplaceRequest515Encoder.executionModeEncodingOffset());
//	    orderCancelReplaceRequest.executionMode(ExecMode.NULL_VAL);
	}

	if (fixMessage.isSetField(LIQUIDITY_FLAG)) {
	    boolean liquidityFlag = fixMessage.getBoolean(LIQUIDITY_FLAG);
	    if (liquidityFlag) {
		orderCancelReplaceRequest.liquidityFlag(BooleanNULL.True);
	    } else {
		orderCancelReplaceRequest.liquidityFlag(BooleanNULL.False);
	    }
	} else {
	    orderCancelReplaceRequest.liquidityFlag(BooleanNULL.NULL_VAL);
	}

	if (fixMessage.isSetField(MANAGED_ORDER)) {
	    boolean managedOrder = fixMessage.getBoolean(MANAGED_ORDER);
	    if (managedOrder) {
		orderCancelReplaceRequest.managedOrder(BooleanNULL.True);
	    } else {
		orderCancelReplaceRequest.managedOrder(BooleanNULL.False);
	    }
	} else {
	    orderCancelReplaceRequest.managedOrder(BooleanNULL.NULL_VAL);
	}

	if (fixMessage.isSetField(SHORT_SALE_TYPE)) {
	    int shortSaleType = fixMessage.getInt(SHORT_SALE_TYPE);
	    switch (shortSaleType) {
	    case 0:
		orderCancelReplaceRequest.shortSaleType(ShortSaleType.LongSell);
		break;
	    case 1:
		orderCancelReplaceRequest.shortSaleType(ShortSaleType.ShortSaleWithNoExemptionSESH);
		break;
	    case 2:
		orderCancelReplaceRequest.shortSaleType(ShortSaleType.ShortSaleWithExemptionSSEX);
		break;
	    case 3:
		orderCancelReplaceRequest.shortSaleType(ShortSaleType.UndisclosedSellInformationNotAvailableUNDI);
		break;
	    default:
		orderCancelReplaceRequest.shortSaleType(ShortSaleType.NULL_VAL);
		break;
	    }
	} else {
	    orderCancelReplaceRequest.shortSaleType(ShortSaleType.NULL_VAL);
	}

	if (fixMessage.isSetField(DiscretionPrice.FIELD)) {
	    orderCancelReplaceRequest.discretionPrice()
		    .mantissa(Long.valueOf(fixMessage.getString(DiscretionPrice.FIELD)));
	} else {
	    orderCancelReplaceRequest.discretionPrice().mantissa(PRICENULL9Encoder.mantissaNullValue());
	}

    }

    private static void mapOrderCancelRequest(Message fixMessage, OrderCancelRequest516Encoder orderCancelRequest,
	    long seqNum) throws NumberFormatException, FieldNotFound {

	if (fixMessage.isSetField(OrderID.FIELD)) {
	    orderCancelRequest.orderID(Long.valueOf(fixMessage.getString(OrderID.FIELD)));
	} else {
	    orderCancelRequest.orderID(OrderCancelRequest516Encoder.orderIDNullValue());
	}

	orderCancelRequest.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));

	boolean manualOrderIndicator = fixMessage.getBoolean(ManualOrderIndicator.FIELD);
	if (manualOrderIndicator) {
	    orderCancelRequest.manualOrderIndicator(ManualOrdIndReq.Manual);
	} else {
	    orderCancelRequest.manualOrderIndicator(ManualOrdIndReq.Automated);
	}

	orderCancelRequest.seqNum(seqNum);
	orderCancelRequest.senderID(fixMessage.getString(SENDER_ID));
	orderCancelRequest.clOrdID(fixMessage.getString(ClOrdID.FIELD));
	orderCancelRequest.orderRequestID(Long.valueOf(fixMessage.getString(ORDER_REQUEST_ID)));
	orderCancelRequest.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));
	orderCancelRequest.location(fixMessage.getString(LOCATION));
	orderCancelRequest.securityID(fixMessage.getInt(SecurityID.FIELD));

	char side = fixMessage.getChar(Side.FIELD);
	orderCancelRequest.side(side == Side.BUY ? SideReq.Buy : SideReq.Sell);

	if (fixMessage.isSetField(LIQUIDITY_FLAG)) {
	    boolean liquidityFlag = fixMessage.getBoolean(LIQUIDITY_FLAG);
	    if (liquidityFlag) {
		orderCancelRequest.liquidityFlag(BooleanNULL.True);
	    } else {
		orderCancelRequest.liquidityFlag(BooleanNULL.False);
	    }
	} else {
	    orderCancelRequest.liquidityFlag(BooleanNULL.NULL_VAL);
	}

	if (fixMessage.isSetField(ORIG_ORDER_USER)) {
	    orderCancelRequest.origOrderUser(fixMessage.getString(ORIG_ORDER_USER));
	}

    }

    private static void mapNewOrderSingle(Message fixMessage, NewOrderSingle514Encoder newOrderSingle, long seqNum)
	    throws NumberFormatException, FieldNotFound {

	if (fixMessage.isSetField(Price.FIELD)) {
	    newOrderSingle.price().mantissa(Long.valueOf(fixMessage.getString(Price.FIELD)));
	} else {
	    newOrderSingle.price().mantissa(PRICENULL9Encoder.mantissaNullValue());
	}

	newOrderSingle.orderQty(fixMessage.getInt(OrderQty.FIELD));
	newOrderSingle.securityID(fixMessage.getInt(SecurityID.FIELD));

	char side = fixMessage.getChar(Side.FIELD);
	newOrderSingle.side(side == Side.BUY ? SideReq.Buy : SideReq.Sell);

	newOrderSingle.seqNum(seqNum);
	newOrderSingle.senderID(fixMessage.getString(SENDER_ID));
	newOrderSingle.clOrdID(fixMessage.getString(ClOrdID.FIELD));
	newOrderSingle.partyDetailsListReqID(Long.valueOf(fixMessage.getString(PARTY_DETAILS_LIST_REQ_ID)));
	newOrderSingle.orderRequestID(Long.valueOf(fixMessage.getString(ORDER_REQUEST_ID)));
	newOrderSingle.sendingTimeEpoch(Long.valueOf(fixMessage.getString(SENDING_TIME_EPOCH)));

	if (fixMessage.isSetField(StopPx.FIELD)) {
	    newOrderSingle.stopPx().mantissa(Long.valueOf(fixMessage.getString(StopPx.FIELD)));
	} else {
	    newOrderSingle.stopPx().mantissa(PRICENULL9Encoder.mantissaNullValue());
	}

	newOrderSingle.location(fixMessage.getString(LOCATION));

	if (fixMessage.isSetField(MinQty.FIELD)) {
	    newOrderSingle.minQty(fixMessage.getInt(MinQty.FIELD));
	} else {
	    newOrderSingle.minQty(NewOrderSingle514Encoder.minQtyNullValue());
	}

	if (fixMessage.isSetField(DisplayQty.FIELD)) {
	    newOrderSingle.displayQty(fixMessage.getInt(DisplayQty.FIELD));
	} else {
	    newOrderSingle.displayQty(NewOrderSingle514Encoder.displayQtyNullValue());
	}

	if (fixMessage.isSetField(ExpireDate.FIELD)) {
	    newOrderSingle.expireDate(fixMessage.getInt(ExpireDate.FIELD));
	} else {
	    newOrderSingle.expireDate(NewOrderSingle514Encoder.expireDateNullValue());
	}

	char ordType = fixMessage.getChar(OrdType.FIELD);
	switch (ordType) {
	case OrdType.MARKET:
	    newOrderSingle.ordType(OrderTypeReq.MarketwithProtection);
	    break;
	case OrdType.LIMIT:
	    newOrderSingle.ordType(OrderTypeReq.Limit);
	    break;
	case OrdType.STOP_STOP_LOSS:
	    newOrderSingle.ordType(OrderTypeReq.StopwithProtection);
	    break;
	case OrdType.STOP_LIMIT:
	    newOrderSingle.ordType(OrderTypeReq.StopLimit);
	    break;
	case OrdType.MARKET_WITH_LEFT_OVER_AS_LIMIT:
	    newOrderSingle.ordType(OrderTypeReq.MarketWithLeftoverAsLimit);
	    break;
	}

	// we need to get it as String due to special value 99
	String timeInForce = fixMessage.getString(TimeInForce.FIELD);
	switch (timeInForce) {
	case "" + TimeInForce.DAY:
	    newOrderSingle.timeInForce(iLinkBinary.TimeInForce.Day);
	    break;
	case "" + TimeInForce.GOOD_TILL_CANCEL:
	    newOrderSingle.timeInForce(iLinkBinary.TimeInForce.GoodTillCancel);
	    break;
	case "" + TimeInForce.IMMEDIATE_OR_CANCEL:
	    newOrderSingle.timeInForce(iLinkBinary.TimeInForce.FillAndKill);
	    break;
	case "" + TimeInForce.FILL_OR_KILL:
	    newOrderSingle.timeInForce(iLinkBinary.TimeInForce.FillOrKill);
	    break;
	case "" + TimeInForce.GOOD_TILL_DATE:
	    newOrderSingle.timeInForce(iLinkBinary.TimeInForce.GoodTillDate);
	    break;
	case "99": // GFS (good for session)
	    newOrderSingle.timeInForce(iLinkBinary.TimeInForce.GoodForSession);
	    break;

	}

	boolean manualOrderIndicator = fixMessage.getBoolean(ManualOrderIndicator.FIELD);
	if (manualOrderIndicator) {
	    newOrderSingle.manualOrderIndicator(ManualOrdIndReq.Manual);
	} else {
	    newOrderSingle.manualOrderIndicator(ManualOrdIndReq.Automated);
	}

	newOrderSingle.execInst().oB(false).nH(false).aON(false);
	if (fixMessage.isSetField(ExecInst.FIELD)) {
	    String execInst = fixMessage.getString(ExecInst.FIELD);
	    if (execInst.contains("" + ExecInst.ALL_OR_NONE_AON)) {
		newOrderSingle.execInst().aON(true);
	    }
	    if (execInst.contains("" + ExecInst.BEST_EXECUTION)) {
		newOrderSingle.execInst().oB(true);
	    }
	    if (execInst.contains("" + ExecInst.NOT_HELD)) {
		newOrderSingle.execInst().nH(true);
	    }
	}

	if (fixMessage.isSetField(EXECUTION_MODE)) {
	    char executionMode = fixMessage.getChar(EXECUTION_MODE);
	    switch (executionMode) {
	    case 'A':
		newOrderSingle.executionMode(ExecMode.Aggressive);
		break;
	    case 'P':
		newOrderSingle.executionMode(ExecMode.Passive);
		break;
	    default:
		newOrderSingle.executionMode(ExecMode.NULL_VAL);
		break;
	    }
	} else {
	    putNullByteForCharNull(newOrderSingle, NewOrderSingle514Encoder.executionModeEncodingOffset());
//	    newOrderSingle.executionMode(ExecMode.NULL_VAL);
	}

	if (fixMessage.isSetField(LIQUIDITY_FLAG)) {
	    boolean liquidityFlag = fixMessage.getBoolean(LIQUIDITY_FLAG);
	    if (liquidityFlag) {
		newOrderSingle.liquidityFlag(BooleanNULL.True);
	    } else {
		newOrderSingle.liquidityFlag(BooleanNULL.False);
	    }
	} else {
	    newOrderSingle.liquidityFlag(BooleanNULL.NULL_VAL);
	}

	if (fixMessage.isSetField(MANAGED_ORDER)) {
	    boolean managedOrder = fixMessage.getBoolean(MANAGED_ORDER);
	    if (managedOrder) {
		newOrderSingle.managedOrder(BooleanNULL.True);
	    } else {
		newOrderSingle.managedOrder(BooleanNULL.False);
	    }
	} else {
	    newOrderSingle.managedOrder(BooleanNULL.NULL_VAL);
	}

	if (fixMessage.isSetField(SHORT_SALE_TYPE)) {
	    int shortSaleType = fixMessage.getInt(SHORT_SALE_TYPE);
	    switch (shortSaleType) {
	    case 0:
		newOrderSingle.shortSaleType(ShortSaleType.LongSell);
		break;
	    case 1:
		newOrderSingle.shortSaleType(ShortSaleType.ShortSaleWithNoExemptionSESH);
		break;
	    case 2:
		newOrderSingle.shortSaleType(ShortSaleType.ShortSaleWithExemptionSSEX);
		break;
	    case 3:
		newOrderSingle.shortSaleType(ShortSaleType.UndisclosedSellInformationNotAvailableUNDI);
		break;
	    default:
		newOrderSingle.shortSaleType(ShortSaleType.NULL_VAL);
		break;
	    }
	} else {
	    newOrderSingle.shortSaleType(ShortSaleType.NULL_VAL);
	}

	if (fixMessage.isSetField(DiscretionPrice.FIELD)) {
	    newOrderSingle.discretionPrice().mantissa(Long.valueOf(fixMessage.getString(DiscretionPrice.FIELD)));
	} else {
	    newOrderSingle.discretionPrice().mantissa(PRICENULL9Encoder.mantissaNullValue());
	}

	// currently not supported
	newOrderSingle.reservationPrice().mantissa(PRICENULL9Encoder.mantissaNullValue());

    }

    static int getVariableLength(final Message fixMessage, final MessageEncoderFlyweight messageEncoderFlyweight) {

	int variableLength = 0;
	// special cases for some variable length messages with repeating groups
	if (messageEncoderFlyweight instanceof PartyDetailsDefinitionRequest518Encoder) {

	    int groupCount = fixMessage.getGroupCount(NO_TRD_REG_PUBLICATIONS);
	    variableLength = variableLength + NoTrdRegPublicationsEncoder.HEADER_SIZE
		    + groupCount * NoTrdRegPublicationsEncoder.sbeBlockLength();

	    // is always present once and has nested party details repeating group
	    if (fixMessage.hasGroup(NO_PARTY_UPDATES)) {
		try {
		    groupCount = fixMessage.getGroup(1, NO_PARTY_UPDATES).getGroupCount(NO_PARTY_DETAILS);
		    variableLength = variableLength + NoPartyDetailsEncoder.HEADER_SIZE
			    + groupCount * NoPartyDetailsEncoder.sbeBlockLength();
		} catch (FieldNotFound e) {
		    // should not happen since we check for group presence first
		}
	    }

	} else if (messageEncoderFlyweight instanceof RequestForQuote543Encoder) {

	    int groupCount = fixMessage.getGroupCount(NoRelatedSym.FIELD);
	    variableLength = variableLength + NoRelatedSymEncoder.HEADER_SIZE
		    + groupCount * NoRelatedSymEncoder.sbeBlockLength();

	} else if (messageEncoderFlyweight instanceof PartyDetailsListRequest537Encoder) {

	    int groupCount = fixMessage.getGroupCount(NO_REQUESTING_PARTY_IDS);
	    variableLength = variableLength + NoRequestingPartyIDsEncoder.HEADER_SIZE
		    + groupCount * NoRequestingPartyIDsEncoder.sbeBlockLength();

	    groupCount = fixMessage.getGroupCount(NoPartyIDs.FIELD);
	    variableLength = variableLength + NoPartyIDsEncoder.HEADER_SIZE
		    + groupCount * NoPartyIDsEncoder.sbeBlockLength();
	}

	return variableLength;
    }

// to CME
//    	DONE iLink 3 New Order - Single
//    	DONE iLink 3 Order Cancel Replace Request
//    	DONE iLink 3 Order Cancel Request
//    	NOT NEEDED iLink 3 Mass Quote
//    	NOT NEEDED iLink 3 Quote Cancel
//    	DONE iLink 3 Order Status Request
//    	DONE iLink 3 Order Mass Status Request
//    	DONE iLink 3 Order Mass Action Request
//    	NOT NEEDED iLink 3 New Order Cross
//    	DONE iLink 3 Request for Quote
//    	NOT NEEDED iLink 3 Security Definition Request
//    	DONE iLink 3 Party Details Definition Request
//    	DONE iLink 3 Party Details List Request
//    	NOT NEEDED iLink 3 Execution Acknowledgment

// from CME
//    	DONE iLink 3 Business Reject
//    	DONE iLink 3 Execution Report - New Order
//    	DONE iLink 3 Execution Report - Modify
//    	DONE iLink 3 Execution Report - Cancel
//    	DONE iLink 3 Execution Report - Status
//    	DONE iLink 3 Execution Report - Trade Outright
//  	DONE iLink 3 Execution Report - Trade Spread
//    	DONE iLink 3 Execution Report - Trade Spread Leg
//    	DONE iLink 3 Execution Report - Elimination
//    	DONE iLink 3 Execution Report - Reject
//    	DONE iLink 3 Execution Report - Trade Addendum Outright
//   	DONE iLink 3 Execution Report - Trade Addendum Spread
//   	DONE iLink 3 Execution Report - Trade Addendum Spread Leg
//    	DONE iLink 3 Order Cancel Reject
//    	DONE iLink 3 Order Cancel Replace Reject
//    	NOT NEEDED iLink 3 Security Definition Response
//    	NOT NEEDED iLink 3 Mass Quote Acknowledgment
//    	DONE iLink 3 Request for Quote Acknowledgment
//    	NOT NEEDED iLink 3 Quote Cancel Acknowledgment
//   	DONE iLink 3 Order Mass Action Report
//    	DONE iLink 3 Party Details Definition Request Acknowledgment
//    	DONE iLink 3 Party Details List Report
//   	DONE iLink 3 Execution Report Pending Cancel
//  	DONE iLink 3 Execution Report Pending Replace

}
