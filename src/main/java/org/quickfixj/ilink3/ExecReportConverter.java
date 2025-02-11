
package org.quickfixj.ilink3;

import static org.quickfixj.ilink3.ILink3MessageConverter.CANCEL_TEXT;
import static org.quickfixj.ilink3.ILink3MessageConverter.DELAY_DURATION;
import static org.quickfixj.ilink3.ILink3MessageConverter.DELAY_TO_TIME;
import static org.quickfixj.ilink3.ILink3MessageConverter.EXECUTION_MODE;
import static org.quickfixj.ilink3.ILink3MessageConverter.LIQUIDITY_FLAG;
import static org.quickfixj.ilink3.ILink3MessageConverter.LOCATION;
import static org.quickfixj.ilink3.ILink3MessageConverter.MANAGED_ORDER;
import static org.quickfixj.ilink3.ILink3MessageConverter.ORDER_REQUEST_ID;
import static org.quickfixj.ilink3.ILink3MessageConverter.ORIG_ORDER_USER;
import static org.quickfixj.ilink3.ILink3MessageConverter.PARTY_DETAILS_LIST_REQ_ID;
import static org.quickfixj.ilink3.ILink3MessageConverter.POSS_RETRANS_FLAG;
import static org.quickfixj.ilink3.ILink3MessageConverter.RESERVATION_PRICE;
import static org.quickfixj.ilink3.ILink3MessageConverter.SENDER_ID;
import static org.quickfixj.ilink3.ILink3MessageConverter.SENDING_TIME_EPOCH;
import static org.quickfixj.ilink3.ILink3MessageConverter.SEQ_NUM;
import static org.quickfixj.ilink3.ILink3MessageConverter.SHORT_SALE_TYPE;
import static org.quickfixj.ilink3.ILink3MessageConverter.SPLIT_MSG;
import static org.quickfixj.ilink3.ILink3MessageConverter.UUID;
import static org.quickfixj.ilink3.ILink3MessageConverter.byteToString;
import static org.quickfixj.ilink3.ILink3MessageConverter.setMsgSeqNum;
import static org.quickfixj.ilink3.ILink3MessageConverter.setString;
import static org.quickfixj.ilink3.ILink3MessageConverter.shortToFIXBooleanValue;

import org.agrona.sbe.MessageDecoderFlyweight;

import iLinkBinary.BooleanNULL;
import iLinkBinary.Decimal32NULLDecoder;
import iLinkBinary.Decimal64NULLDecoder;
import iLinkBinary.ExecMode;
import iLinkBinary.ExecReason;
import iLinkBinary.ExecutionReportCancel534Decoder;
import iLinkBinary.ExecutionReportElimination524Decoder;
import iLinkBinary.ExecutionReportModify531Decoder;
import iLinkBinary.ExecutionReportNew522Decoder;
import iLinkBinary.ExecutionReportPendingCancel564Decoder;
import iLinkBinary.ExecutionReportPendingReplace565Decoder;
import iLinkBinary.ExecutionReportReject523Decoder;
import iLinkBinary.ExecutionReportStatus532Decoder;
import iLinkBinary.ExecutionReportTradeAddendumOutright548Decoder;
import iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder;
import iLinkBinary.ExecutionReportTradeAddendumSpreadLeg550Decoder;
import iLinkBinary.ExecutionReportTradeOutright525Decoder;
import iLinkBinary.ExecutionReportTradeOutright525Decoder.NoFillsDecoder;
import iLinkBinary.ExecutionReportTradeOutright525Decoder.NoOrderEventsDecoder;
import iLinkBinary.ExecutionReportTradeSpread526Decoder;
import iLinkBinary.ExecutionReportTradeSpread526Decoder.NoLegsDecoder;
import iLinkBinary.ExecutionReportTradeSpreadLeg527Decoder;
import iLinkBinary.OrderType;
import iLinkBinary.PRICENULL9Decoder;
import iLinkBinary.ShortSaleType;
import iLinkBinary.SplitMsg;
import quickfix.Group;
import quickfix.Message;
import quickfix.field.AggressorIndicator;
import quickfix.field.CalculatedCcyLastQty;
import quickfix.field.ClOrdID;
import quickfix.field.CrossID;
import quickfix.field.CrossType;
import quickfix.field.CumQty;
import quickfix.field.DiscretionPrice;
import quickfix.field.DisplayQty;
import quickfix.field.ExecID;
import quickfix.field.ExecInst;
import quickfix.field.ExecRestatementReason;
import quickfix.field.ExecType;
import quickfix.field.ExpireDate;
import quickfix.field.FillExecID;
import quickfix.field.FillPx;
import quickfix.field.FillQty;
import quickfix.field.GrossTradeAmt;
import quickfix.field.HostCrossID;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LastRptRequested;
import quickfix.field.LeavesQty;
import quickfix.field.LegLastPx;
import quickfix.field.LegLastQty;
import quickfix.field.LegSecurityID;
import quickfix.field.LegSide;
import quickfix.field.ManualOrderIndicator;
import quickfix.field.MassStatusReqID;
import quickfix.field.MaturityDate;
import quickfix.field.MinQty;
import quickfix.field.NoFills;
import quickfix.field.NoLegs;
import quickfix.field.OrdRejReason;
import quickfix.field.OrdStatus;
import quickfix.field.OrdStatusReqID;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.PriorityIndicator;
import quickfix.field.RiskFreeRate;
import quickfix.field.SecondaryExecID;
import quickfix.field.SecurityID;
import quickfix.field.SettlDate;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TimeToExpiration;
import quickfix.field.TotalNumSecurities;
import quickfix.field.TradeDate;
import quickfix.field.TradeLinkID;
import quickfix.field.TransactTime;
import quickfix.field.UnderlyingPx;
import quickfix.field.Volatility;

public class ExecReportConverter {

    private static final int OPTION_DELTA = 811;
    private static final int LEG_TRADE_ID = 1894;
    private static final int LEG_TRADE_REF_ID = 39023;
    private static final int LEG_EXEC_ID = 1893;
    private static final int LEG_EXEC_REF_ID = 1901;
    private static final int ORIGINAL_ORDER_EVENT_EXEC_ID = 6555;
    private static final int ORIG_SIDE_TRADE_ID = 1507;
    private static final int ORIG_SECONDARY_EXEC_ID = 9703;
    private static final int CONTRA_CALCULATED_CCY_LAST_QTY = 5971;
    private static final int CONTRA_GROSS_TRADE_AMOUNT = 5542;
    private static final int ORDER_EVENT_REASON = 1798;
    private static final int ORDER_EVENT_TYPE = 1796;
    private static final int ORDER_EVENT_QTY = 1800;
    private static final int ORDER_EVENT_EXEC_ID = 1797;
    private static final int ORDER_EVENT_TEXT = 1802;
    private static final int ORDER_EVENT_PRICE = 1799;
    private static final int NO_ORDER_EVENTS = 1795;
    private static final int FILL_YIELD_TYPE = 1622;
    private static final int BENCHMARK_PRICE = 6262;
    private static final int TRADE_TYPE = 828;
    private static final int OWNERSHIP = 7191;
    private static final int SIDE_TRADE_ID = 1506;
    private static final int MD_TRADE_ENTRY_ID = 37711;

    static void mapToFIXExecReport(MessageDecoderFlyweight decoder, Message fixMessage) {

	switch (decoder.sbeTemplateId()) {
	case ExecutionReportNew522Decoder.TEMPLATE_ID:
	    mapExecReportNew((ExecutionReportNew522Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportCancel534Decoder.TEMPLATE_ID:
	    mapExecReportCancel((ExecutionReportCancel534Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportElimination524Decoder.TEMPLATE_ID:
	    mapExecReportElimination((ExecutionReportElimination524Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportModify531Decoder.TEMPLATE_ID:
	    mapExecReportModify((ExecutionReportModify531Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportPendingCancel564Decoder.TEMPLATE_ID:
	    mapExecReportPendingCancel((ExecutionReportPendingCancel564Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportPendingReplace565Decoder.TEMPLATE_ID:
	    mapExecReportPendingReplace((ExecutionReportPendingReplace565Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportStatus532Decoder.TEMPLATE_ID:
	    mapExecReportStatus((ExecutionReportStatus532Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportReject523Decoder.TEMPLATE_ID:
	    mapExecReportReject((ExecutionReportReject523Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportTradeOutright525Decoder.TEMPLATE_ID:
	    mapExecReportTradeOutright((ExecutionReportTradeOutright525Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportTradeSpread526Decoder.TEMPLATE_ID:
	    mapExecReportTradeSpread((ExecutionReportTradeSpread526Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportTradeSpreadLeg527Decoder.TEMPLATE_ID:
	    mapExecReportTradeSpreadLeg((ExecutionReportTradeSpreadLeg527Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportTradeAddendumOutright548Decoder.TEMPLATE_ID:
	    mapExecReportTradeAddendumOutright((ExecutionReportTradeAddendumOutright548Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportTradeAddendumSpread549Decoder.TEMPLATE_ID:
	    mapExecReportTradeAddendumSpread((ExecutionReportTradeAddendumSpread549Decoder) decoder, fixMessage);
	    break;
	case ExecutionReportTradeAddendumSpreadLeg550Decoder.TEMPLATE_ID:
	    mapExecReportTradeAddendumSpreadLeg((ExecutionReportTradeAddendumSpreadLeg550Decoder) decoder, fixMessage);
	    break;
	}
    }

    // TODO there is much duplication with the execreport decoders.
    // unfortunately decoders are final, so we cannot extend them and create some
    // sort of base class which has a common set of fields.
    // TODO replace tagnumbers by decoder.someFieldId()

    private static void mapExecReportReject(ExecutionReportReject523Decoder decoder, Message fixMessage) {

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
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.price().mantissa());
	setString(fixMessage, StopPx.FIELD, Long.toString(decoder.stopPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.stopPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, CrossID.FIELD, Long.toString(decoder.crossID()),
		ExecutionReportReject523Decoder.crossIDNullValue() != decoder.crossID());
	setString(fixMessage, HostCrossID.FIELD, Long.toString(decoder.hostCrossID()),
		ExecutionReportReject523Decoder.hostCrossIDNullValue() != decoder.hostCrossID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, MinQty.FIELD, Long.toString(decoder.minQty()),
		ExecutionReportReject523Decoder.minQtyNullValue() != decoder.minQty());
	setString(fixMessage, DisplayQty.FIELD, Long.toString(decoder.displayQty()),
		ExecutionReportReject523Decoder.displayQtyNullValue() != decoder.displayQty());
	setString(fixMessage, OrdRejReason.FIELD, Integer.toString(decoder.ordRejReason()));
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, DELAY_DURATION, Integer.toString(decoder.delayDuration()),
		ExecutionReportReject523Decoder.delayDurationNullValue() != decoder.delayDuration());
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, CrossType.FIELD, Short.toString(decoder.crossType()),
		ExecutionReportReject523Decoder.crossTypeNullValue() != decoder.crossType());
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		ExecutionReportReject523Decoder.delayToTimeNullValue() != decoder.delayToTime());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, RESERVATION_PRICE, Long.toString(decoder.reservationPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.reservationPrice().mantissa());
    }

    private static void mapExecReportTradeAddendumOutright(ExecutionReportTradeAddendumOutright548Decoder decoder,
	    Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, LastPx.FIELD, Long.toString(decoder.lastPx().mantissa()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, SecondaryExecID.FIELD, Long.toString(decoder.secExecID()));
	setString(fixMessage, ORIG_SECONDARY_EXEC_ID, Long.toString(decoder.origSecondaryExecutionID()),
		ExecutionReportTradeAddendumOutright548Decoder.origSecondaryExecutionIDNullValue() != decoder
			.origSecondaryExecutionID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, LastQty.FIELD, Long.toString(decoder.lastQty()));
	setString(fixMessage, SIDE_TRADE_ID, Long.toString(decoder.sideTradeID()));
	setString(fixMessage, ORIG_SIDE_TRADE_ID, Long.toString(decoder.origSideTradeID()),
		ExecutionReportTradeAddendumOutright548Decoder.origSideTradeIDNullValue() != decoder.origSideTradeID());
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus().value()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType().value()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, TRADE_TYPE, Integer.toString(decoder.trdType()),
		ExecutionReportTradeAddendumOutright548Decoder.trdTypeNullValue() != decoder.trdType());
	setString(fixMessage, ExecRestatementReason.FIELD, Short.toString(decoder.execRestatementReason().value()),
		ExecReason.NULL_VAL != decoder.execRestatementReason());
	setString(fixMessage, SettlDate.FIELD, Integer.toString(decoder.settlDate()),
		ExecutionReportTradeAddendumOutright548Decoder.settlDateNullValue() != decoder.settlDate());
	setString(fixMessage, MaturityDate.FIELD, Integer.toString(decoder.maturityDate()),
		ExecutionReportTradeAddendumOutright548Decoder.maturityDateNullValue() != decoder.maturityDate());
	setString(fixMessage, CalculatedCcyLastQty.FIELD, Long.toString(decoder.calculatedCcyLastQty().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.calculatedCcyLastQty().mantissa());
	setString(fixMessage, GrossTradeAmt.FIELD, Long.toString(decoder.grossTradeAmt().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.grossTradeAmt().mantissa());
	setString(fixMessage, BENCHMARK_PRICE, Long.toString(decoder.benchmarkPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.benchmarkPrice().mantissa());

	iLinkBinary.ExecutionReportTradeAddendumOutright548Decoder.NoFillsDecoder noFills = decoder.noFills();
	if (noFills.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeAddendumOutright548Decoder.NoFillsDecoder noFillsDecoder : noFills) {
		Group noFillsGroup = new Group(NoFills.FIELD, FillPx.FIELD);
		setString(noFillsGroup, FillPx.FIELD, Long.toString(noFillsDecoder.fillPx().mantissa()));
		setString(noFillsGroup, FillQty.FIELD, Long.toString(noFillsDecoder.fillQty()));
		setString(noFillsGroup, FillExecID.FIELD, noFillsDecoder.fillExecID());
		setString(noFillsGroup, FILL_YIELD_TYPE, Short.toString(noFillsDecoder.fillYieldType()));
		fixMessage.addGroup(noFillsGroup);
	    }
	}

	iLinkBinary.ExecutionReportTradeAddendumOutright548Decoder.NoOrderEventsDecoder noOrderEvents = decoder
		.noOrderEvents();
	if (noOrderEvents.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeAddendumOutright548Decoder.NoOrderEventsDecoder noOrderEventsDecoder : noOrderEvents) {
		Group noOrderEventsGroup = new Group(NO_ORDER_EVENTS, ORDER_EVENT_PRICE);
		setString(noOrderEventsGroup, ORDER_EVENT_PRICE,
			Long.toString(noOrderEventsDecoder.orderEventPx().mantissa()));
		setString(noOrderEventsGroup, ORDER_EVENT_TEXT, noOrderEventsDecoder.orderEventText(),
			!noOrderEventsDecoder.orderEventText().isEmpty());
		setString(noOrderEventsGroup, ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.orderEventExecID()));
		setString(noOrderEventsGroup, ORDER_EVENT_QTY, Long.toString(noOrderEventsDecoder.orderEventQty()));
		setString(noOrderEventsGroup, ORDER_EVENT_TYPE,
			Short.toString(noOrderEventsDecoder.orderEventTypeRaw()));
		setString(noOrderEventsGroup, ORDER_EVENT_REASON,
			Short.toString(noOrderEventsDecoder.orderEventReason()));
		setString(noOrderEventsGroup, ORIGINAL_ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.originalOrderEventExecID()),
			iLinkBinary.ExecutionReportTradeAddendumOutright548Decoder.NoOrderEventsDecoder
				.originalOrderEventExecIDNullValue() != noOrderEventsDecoder
					.originalOrderEventExecID());
		setString(noOrderEventsGroup, CONTRA_GROSS_TRADE_AMOUNT,
			Long.toString(noOrderEventsDecoder.contraGrossTradeAmt().mantissa()), Decimal64NULLDecoder
				.mantissaNullValue() != noOrderEventsDecoder.contraGrossTradeAmt().mantissa());
		setString(noOrderEventsGroup, CONTRA_CALCULATED_CCY_LAST_QTY,
			Long.toString(noOrderEventsDecoder.contraCalculatedCcyLastQty().mantissa()),
			Decimal64NULLDecoder.mantissaNullValue() != noOrderEventsDecoder.contraCalculatedCcyLastQty()
				.mantissa());
		fixMessage.addGroup(noOrderEventsGroup);
	    }
	}
    }

    private static void mapExecReportTradeAddendumSpreadLeg(ExecutionReportTradeAddendumSpreadLeg550Decoder decoder,
	    Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, LastPx.FIELD, Long.toString(decoder.lastPx().mantissa()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, SecondaryExecID.FIELD, Long.toString(decoder.secExecID()));
	setString(fixMessage, ORIG_SECONDARY_EXEC_ID, Long.toString(decoder.origSecondaryExecutionID()),
		ExecutionReportTradeAddendumSpreadLeg550Decoder.origSecondaryExecutionIDNullValue() != decoder
			.origSecondaryExecutionID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, LastQty.FIELD, Long.toString(decoder.lastQty()));
	setString(fixMessage, SIDE_TRADE_ID, Long.toString(decoder.sideTradeID()));
	setString(fixMessage, ORIG_SIDE_TRADE_ID, Long.toString(decoder.origSideTradeID()),
		ExecutionReportTradeAddendumSpreadLeg550Decoder.origSideTradeIDNullValue() != decoder
			.origSideTradeID());
	setString(fixMessage, TradeDate.FIELD, Integer.toString(decoder.tradeDate()));
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus().value()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execTypeRaw()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, SettlDate.FIELD, Integer.toString(decoder.settlDate()),
		ExecutionReportTradeAddendumSpreadLeg550Decoder.settlDateNullValue() != decoder.settlDate());
	setString(fixMessage, CalculatedCcyLastQty.FIELD, Long.toString(decoder.calculatedCcyLastQty().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.calculatedCcyLastQty().mantissa());
	setString(fixMessage, GrossTradeAmt.FIELD, Long.toString(decoder.grossTradeAmt().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.grossTradeAmt().mantissa());

	iLinkBinary.ExecutionReportTradeAddendumSpreadLeg550Decoder.NoFillsDecoder noFills = decoder.noFills();
	if (noFills.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeAddendumSpreadLeg550Decoder.NoFillsDecoder noFillsDecoder : noFills) {
		Group noFillsGroup = new Group(NoFills.FIELD, FillPx.FIELD);
		setString(noFillsGroup, FillPx.FIELD, Long.toString(noFillsDecoder.fillPx().mantissa()));
		setString(noFillsGroup, FillQty.FIELD, Long.toString(noFillsDecoder.fillQty()));
		setString(noFillsGroup, FillExecID.FIELD, noFillsDecoder.fillExecID());
		setString(noFillsGroup, FILL_YIELD_TYPE, Short.toString(noFillsDecoder.fillYieldType()));
		fixMessage.addGroup(noFillsGroup);
	    }
	}

	iLinkBinary.ExecutionReportTradeAddendumSpreadLeg550Decoder.NoOrderEventsDecoder noOrderEvents = decoder
		.noOrderEvents();
	if (noOrderEvents.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeAddendumSpreadLeg550Decoder.NoOrderEventsDecoder noOrderEventsDecoder : noOrderEvents) {
		Group noOrderEventsGroup = new Group(NO_ORDER_EVENTS, ORDER_EVENT_PRICE);
		setString(noOrderEventsGroup, ORDER_EVENT_PRICE,
			Long.toString(noOrderEventsDecoder.orderEventPx().mantissa()));
		setString(noOrderEventsGroup, ORDER_EVENT_TEXT, noOrderEventsDecoder.orderEventText(),
			!noOrderEventsDecoder.orderEventText().isEmpty());
		setString(noOrderEventsGroup, ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.orderEventExecID()));
		setString(noOrderEventsGroup, ORDER_EVENT_QTY, Long.toString(noOrderEventsDecoder.orderEventQty()));
		setString(noOrderEventsGroup, ORDER_EVENT_TYPE,
			Short.toString(noOrderEventsDecoder.orderEventTypeRaw()));
		setString(noOrderEventsGroup, ORDER_EVENT_REASON,
			Short.toString(noOrderEventsDecoder.orderEventReason()));
		setString(noOrderEventsGroup, ORIGINAL_ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.originalOrderEventExecID()),
			iLinkBinary.ExecutionReportTradeAddendumSpreadLeg550Decoder.NoOrderEventsDecoder
				.originalOrderEventExecIDNullValue() != noOrderEventsDecoder
					.originalOrderEventExecID());
		fixMessage.addGroup(noOrderEventsGroup);
	    }
	}

//	 tbc

    }

    private static void mapExecReportTradeAddendumSpread(ExecutionReportTradeAddendumSpread549Decoder decoder,
	    Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, LastPx.FIELD, Long.toString(decoder.lastPx().mantissa()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, SecondaryExecID.FIELD, Long.toString(decoder.secExecID()));
	setString(fixMessage, ORIG_SECONDARY_EXEC_ID, Long.toString(decoder.origSecondaryExecutionID()),
		ExecutionReportTradeAddendumSpread549Decoder.origSecondaryExecutionIDNullValue() != decoder
			.origSecondaryExecutionID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, MD_TRADE_ENTRY_ID, Long.toString(decoder.mDTradeEntryID()));
	setString(fixMessage, LastQty.FIELD, Long.toString(decoder.lastQty()));
	setString(fixMessage, SIDE_TRADE_ID, Long.toString(decoder.sideTradeID()));
	setString(fixMessage, ORIG_SIDE_TRADE_ID, Long.toString(decoder.origSideTradeID()),
		ExecutionReportTradeAddendumSpread549Decoder.origSideTradeIDNullValue() != decoder.origSideTradeID());
	setString(fixMessage, TradeDate.FIELD, Integer.toString(decoder.tradeDate()));
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus().value()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execTypeRaw()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()),
		OrderType.NULL_VAL != decoder.ordType());
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, TotalNumSecurities.FIELD, Short.toString(decoder.totalNumSecurities()));
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());

	iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoFillsDecoder noFills = decoder.noFills();
	if (noFills.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoFillsDecoder noFillsDecoder : noFills) {
		Group noFillsGroup = new Group(NoFills.FIELD, FillPx.FIELD);
		setString(noFillsGroup, FillPx.FIELD, Long.toString(noFillsDecoder.fillPx().mantissa()));
		setString(noFillsGroup, FillQty.FIELD, Long.toString(noFillsDecoder.fillQty()));
		setString(noFillsGroup, FillExecID.FIELD, noFillsDecoder.fillExecID());
		setString(noFillsGroup, FILL_YIELD_TYPE, Short.toString(noFillsDecoder.fillYieldType()));
		fixMessage.addGroup(noFillsGroup);
	    }
	}

	iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoLegsDecoder noLegs = decoder.noLegs();
	if (noLegs.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoLegsDecoder noLegsDecoder : noLegs) {
		Group noLegsGroup = new Group(NoLegs.FIELD, LEG_EXEC_ID);
		setString(noLegsGroup, LEG_EXEC_ID, Long.toString(noLegsDecoder.legExecID()));
		setString(noLegsGroup, LegLastPx.FIELD, Long.toString(noLegsDecoder.legLastPx().mantissa()));
		setString(noLegsGroup, LEG_EXEC_REF_ID, Long.toString(noLegsDecoder.legExecRefID()),
			iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoLegsDecoder
				.legExecRefIDNullValue() != noLegsDecoder.legExecRefID());
		setString(noLegsGroup, LEG_TRADE_ID, Long.toString(noLegsDecoder.legTradeID()));
		setString(noLegsGroup, LEG_TRADE_REF_ID, Long.toString(noLegsDecoder.legTradeRefID()),
			iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoLegsDecoder
				.legTradeRefIDNullValue() != noLegsDecoder.legTradeRefID());
		setString(noLegsGroup, LegSecurityID.FIELD, Integer.toString(noLegsDecoder.legSecurityID()));
		setString(noLegsGroup, LegLastQty.FIELD, Long.toString(noLegsDecoder.legLastQty()));
		setString(noLegsGroup, LegSide.FIELD, Short.toString(noLegsDecoder.legSideRaw()));
		fixMessage.addGroup(noLegsGroup);
	    }
	}

	iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoOrderEventsDecoder noOrderEvents = decoder
		.noOrderEvents();
	if (noOrderEvents.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoOrderEventsDecoder noOrderEventsDecoder : noOrderEvents) {
		Group noOrderEventsGroup = new Group(NO_ORDER_EVENTS, ORDER_EVENT_PRICE);
		setString(noOrderEventsGroup, ORDER_EVENT_PRICE,
			Long.toString(noOrderEventsDecoder.orderEventPx().mantissa()));
		setString(noOrderEventsGroup, ORDER_EVENT_TEXT, noOrderEventsDecoder.orderEventText(),
			!noOrderEventsDecoder.orderEventText().isEmpty());
		setString(noOrderEventsGroup, ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.orderEventExecID()));
		setString(noOrderEventsGroup, ORDER_EVENT_QTY, Long.toString(noOrderEventsDecoder.orderEventQty()));
		setString(noOrderEventsGroup, ORDER_EVENT_TYPE,
			Short.toString(noOrderEventsDecoder.orderEventTypeRaw()));
		setString(noOrderEventsGroup, ORDER_EVENT_REASON,
			Short.toString(noOrderEventsDecoder.orderEventReason()));
		setString(noOrderEventsGroup, ORIGINAL_ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.originalOrderEventExecID()),
			iLinkBinary.ExecutionReportTradeAddendumSpread549Decoder.NoOrderEventsDecoder
				.originalOrderEventExecIDNullValue() != noOrderEventsDecoder
					.originalOrderEventExecID());
		fixMessage.addGroup(noOrderEventsGroup);
	    }
	}

    }

    private static void mapExecReportTradeSpreadLeg(ExecutionReportTradeSpreadLeg527Decoder decoder,
	    Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, Volatility.FIELD, Long.toString(decoder.volatility().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.volatility().mantissa());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, LastPx.FIELD, Long.toString(decoder.lastPx().mantissa()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, UnderlyingPx.FIELD, Long.toString(decoder.underlyingPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.underlyingPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, SecondaryExecID.FIELD, Long.toString(decoder.secExecID()));
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, OPTION_DELTA, Long.toString(decoder.optionDelta().mantissa()),
		Decimal32NULLDecoder.mantissaNullValue() != decoder.optionDelta().mantissa());
	setString(fixMessage, TimeToExpiration.FIELD, Long.toString(decoder.timeToExpiration().mantissa()),
		Decimal32NULLDecoder.mantissaNullValue() != decoder.timeToExpiration().mantissa());
	setString(fixMessage, RiskFreeRate.FIELD, Long.toString(decoder.riskFreeRate().mantissa()),
		Decimal32NULLDecoder.mantissaNullValue() != decoder.riskFreeRate().mantissa());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, LastQty.FIELD, Long.toString(decoder.lastQty()));
	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));
	setString(fixMessage, SIDE_TRADE_ID, Long.toString(decoder.sideTradeID()));
	setString(fixMessage, TradeDate.FIELD, Integer.toString(decoder.tradeDate()));
	setString(fixMessage, OrdStatus.FIELD, Short.toString(decoder.ordStatus().value()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SettlDate.FIELD, Integer.toString(decoder.settlDate()),
		ExecutionReportTradeSpreadLeg527Decoder.settlDateNullValue() != decoder.settlDate());
	setString(fixMessage, CalculatedCcyLastQty.FIELD, Long.toString(decoder.calculatedCcyLastQty().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.calculatedCcyLastQty().mantissa());
	setString(fixMessage, GrossTradeAmt.FIELD, Long.toString(decoder.grossTradeAmt().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.grossTradeAmt().mantissa());

	iLinkBinary.ExecutionReportTradeSpreadLeg527Decoder.NoFillsDecoder noFills = decoder.noFills();
	if (noFills.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeSpreadLeg527Decoder.NoFillsDecoder noFillsDecoder : noFills) {
		Group noFillsGroup = new Group(NoFills.FIELD, FillPx.FIELD);
		setString(noFillsGroup, FillPx.FIELD, Long.toString(noFillsDecoder.fillPx().mantissa()));
		setString(noFillsGroup, FillQty.FIELD, Long.toString(noFillsDecoder.fillQty()));
		setString(noFillsGroup, FillExecID.FIELD, noFillsDecoder.fillExecID());
		setString(noFillsGroup, FILL_YIELD_TYPE, Short.toString(noFillsDecoder.fillYieldType()));
		fixMessage.addGroup(noFillsGroup);
	    }
	}

	iLinkBinary.ExecutionReportTradeSpreadLeg527Decoder.NoOrderEventsDecoder noOrderEvents = decoder
		.noOrderEvents();
	if (noOrderEvents.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeSpreadLeg527Decoder.NoOrderEventsDecoder noOrderEventsDecoder : noOrderEvents) {
		Group noOrderEventsGroup = new Group(NO_ORDER_EVENTS, ORDER_EVENT_PRICE);
		setString(noOrderEventsGroup, ORDER_EVENT_PRICE,
			Long.toString(noOrderEventsDecoder.orderEventPx().mantissa()));
		setString(noOrderEventsGroup, ORDER_EVENT_TEXT, noOrderEventsDecoder.orderEventText(),
			!noOrderEventsDecoder.orderEventText().isEmpty());
		setString(noOrderEventsGroup, ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.orderEventExecID()));
		setString(noOrderEventsGroup, ORDER_EVENT_QTY, Long.toString(noOrderEventsDecoder.orderEventQty()));
		setString(noOrderEventsGroup, ORDER_EVENT_TYPE,
			Short.toString(noOrderEventsDecoder.orderEventTypeRaw()));
		setString(noOrderEventsGroup, ORDER_EVENT_REASON,
			Short.toString(noOrderEventsDecoder.orderEventReason()));
		fixMessage.addGroup(noOrderEventsGroup);
	    }
	}
    }

    private static void mapExecReportTradeSpread(ExecutionReportTradeSpread526Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, LastPx.FIELD, Long.toString(decoder.lastPx().mantissa()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()));
	setString(fixMessage, StopPx.FIELD, Long.toString(decoder.stopPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.stopPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, SecondaryExecID.FIELD, Long.toString(decoder.secExecID()));
	setString(fixMessage, CrossID.FIELD, Long.toString(decoder.crossID()),
		ExecutionReportTradeSpread526Decoder.crossIDNullValue() != decoder.crossID());
	setString(fixMessage, HostCrossID.FIELD, Long.toString(decoder.hostCrossID()),
		ExecutionReportTradeSpread526Decoder.hostCrossIDNullValue() != decoder.hostCrossID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, LastQty.FIELD, Long.toString(decoder.lastQty()));
	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));
	setString(fixMessage, MD_TRADE_ENTRY_ID, Long.toString(decoder.mDTradeEntryID()));
	setString(fixMessage, SIDE_TRADE_ID, Long.toString(decoder.sideTradeID()));
	setString(fixMessage, LeavesQty.FIELD, Long.toString(decoder.leavesQty()));
	setString(fixMessage, TradeDate.FIELD, Integer.toString(decoder.tradeDate()));
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, OrdStatus.FIELD, Short.toString(decoder.ordStatus().value()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, AggressorIndicator.FIELD, shortToFIXBooleanValue(decoder.aggressorIndicator().value()));
	setString(fixMessage, CrossType.FIELD, Short.toString(decoder.crossType()),
		ExecutionReportTradeSpread526Decoder.crossTypeNullValue() != decoder.crossType());
	setString(fixMessage, TotalNumSecurities.FIELD, Short.toString(decoder.totalNumSecurities()));
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());

	ExecutionReportTradeSpread526Decoder.NoFillsDecoder noFills = decoder.noFills();
	if (noFills.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeSpread526Decoder.NoFillsDecoder noFillsDecoder : noFills) {
		Group noFillsGroup = new Group(NoFills.FIELD, FillPx.FIELD);
		setString(noFillsGroup, FillPx.FIELD, Long.toString(noFillsDecoder.fillPx().mantissa()));
		setString(noFillsGroup, FillQty.FIELD, Long.toString(noFillsDecoder.fillQty()));
		setString(noFillsGroup, FillExecID.FIELD, noFillsDecoder.fillExecID());
		setString(noFillsGroup, FILL_YIELD_TYPE, Short.toString(noFillsDecoder.fillYieldType()));
		fixMessage.addGroup(noFillsGroup);
	    }
	}

	iLinkBinary.ExecutionReportTradeSpread526Decoder.NoLegsDecoder noLegs = decoder.noLegs();
	if (noLegs.count() > 0) {
	    for (NoLegsDecoder noLegsDecoder : noLegs) {
		Group noLegsGroup = new Group(NoLegs.FIELD, LEG_EXEC_ID);
		setString(noLegsGroup, LEG_EXEC_ID, Long.toString(noLegsDecoder.legExecID()));
		setString(noLegsGroup, LegLastPx.FIELD, Long.toString(noLegsDecoder.legLastPx().mantissa()));
		setString(noLegsGroup, LegSecurityID.FIELD, Integer.toString(noLegsDecoder.legSecurityID()));
		setString(noLegsGroup, LEG_TRADE_ID, Long.toString(noLegsDecoder.legTradeID()));
		setString(noLegsGroup, LegLastQty.FIELD, Long.toString(noLegsDecoder.legLastQty()));
		setString(noLegsGroup, LegSide.FIELD, Short.toString(noLegsDecoder.legSideRaw()));
		fixMessage.addGroup(noLegsGroup);
	    }
	}

	iLinkBinary.ExecutionReportTradeSpread526Decoder.NoOrderEventsDecoder noOrderEvents = decoder.noOrderEvents();
	if (noOrderEvents.count() > 0) {
	    for (iLinkBinary.ExecutionReportTradeSpread526Decoder.NoOrderEventsDecoder noOrderEventsDecoder : noOrderEvents) {
		Group noOrderEventsGroup = new Group(NO_ORDER_EVENTS, ORDER_EVENT_PRICE);
		setString(noOrderEventsGroup, ORDER_EVENT_PRICE,
			Long.toString(noOrderEventsDecoder.orderEventPx().mantissa()));
		setString(noOrderEventsGroup, ORDER_EVENT_TEXT, noOrderEventsDecoder.orderEventText(),
			!noOrderEventsDecoder.orderEventText().isEmpty());
		setString(noOrderEventsGroup, ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.orderEventExecID()));
		setString(noOrderEventsGroup, ORDER_EVENT_QTY, Long.toString(noOrderEventsDecoder.orderEventQty()));
		setString(noOrderEventsGroup, ORDER_EVENT_TYPE,
			Short.toString(noOrderEventsDecoder.orderEventTypeRaw()));
		setString(noOrderEventsGroup, ORDER_EVENT_REASON,
			Short.toString(noOrderEventsDecoder.orderEventReason()));
		fixMessage.addGroup(noOrderEventsGroup);
	    }
	}

    }

    private static void mapExecReportTradeOutright(ExecutionReportTradeOutright525Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, LastPx.FIELD, Long.toString(decoder.lastPx().mantissa()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()));
	setString(fixMessage, StopPx.FIELD, Long.toString(decoder.stopPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.stopPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, SecondaryExecID.FIELD, Long.toString(decoder.secExecID()));
	setString(fixMessage, CrossID.FIELD, Long.toString(decoder.crossID()),
		ExecutionReportTradeOutright525Decoder.crossIDNullValue() != decoder.crossID());
	setString(fixMessage, HostCrossID.FIELD, Long.toString(decoder.hostCrossID()),
		ExecutionReportTradeOutright525Decoder.hostCrossIDNullValue() != decoder.hostCrossID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, LastQty.FIELD, Long.toString(decoder.lastQty()));
	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));
	setString(fixMessage, MD_TRADE_ENTRY_ID, Long.toString(decoder.mDTradeEntryID()));
	setString(fixMessage, SIDE_TRADE_ID, Long.toString(decoder.sideTradeID()));
	setString(fixMessage, TradeLinkID.FIELD, Long.toString(decoder.tradeLinkID()),
		ExecutionReportTradeOutright525Decoder.tradeLinkIDNullValue() != decoder.tradeLinkID());
	setString(fixMessage, LeavesQty.FIELD, Long.toString(decoder.leavesQty()));
	setString(fixMessage, TradeDate.FIELD, Integer.toString(decoder.tradeDate()));
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, OrdStatus.FIELD, Short.toString(decoder.ordStatus().value()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, AggressorIndicator.FIELD, shortToFIXBooleanValue(decoder.aggressorIndicator().value()));
	setString(fixMessage, CrossType.FIELD, Short.toString(decoder.crossType()),
		ExecutionReportTradeOutright525Decoder.crossTypeNullValue() != decoder.crossType());
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, OWNERSHIP, Short.toString(decoder.ownership()),
		ExecutionReportTradeOutright525Decoder.ownershipNullValue() != decoder.ownership());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, TRADE_TYPE, Integer.toString(decoder.trdType()),
		ExecutionReportTradeOutright525Decoder.trdTypeNullValue() != decoder.trdType());
	setString(fixMessage, ExecRestatementReason.FIELD, Short.toString(decoder.execRestatementReason().value()),
		ExecReason.NULL_VAL != decoder.execRestatementReason());
	setString(fixMessage, SettlDate.FIELD, Integer.toString(decoder.settlDate()),
		ExecutionReportTradeOutright525Decoder.settlDateNullValue() != decoder.settlDate());
	setString(fixMessage, MaturityDate.FIELD, Integer.toString(decoder.maturityDate()),
		ExecutionReportTradeOutright525Decoder.maturityDateNullValue() != decoder.maturityDate());
	setString(fixMessage, CalculatedCcyLastQty.FIELD, Long.toString(decoder.calculatedCcyLastQty().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.calculatedCcyLastQty().mantissa());
	setString(fixMessage, GrossTradeAmt.FIELD, Long.toString(decoder.grossTradeAmt().mantissa()),
		Decimal64NULLDecoder.mantissaNullValue() != decoder.grossTradeAmt().mantissa());
	setString(fixMessage, BENCHMARK_PRICE, Long.toString(decoder.benchmarkPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.benchmarkPrice().mantissa());

	NoFillsDecoder noFills = decoder.noFills();
	if (noFills.count() > 0) {
	    for (NoFillsDecoder noFillsDecoder : noFills) {
		Group noFillsGroup = new Group(NoFills.FIELD, FillPx.FIELD);
		setString(noFillsGroup, FillPx.FIELD, Long.toString(noFillsDecoder.fillPx().mantissa()));
		setString(noFillsGroup, FillQty.FIELD, Long.toString(noFillsDecoder.fillQty()));
		setString(noFillsGroup, FillExecID.FIELD, noFillsDecoder.fillExecID());
		setString(noFillsGroup, FILL_YIELD_TYPE, Short.toString(noFillsDecoder.fillYieldType()));
		fixMessage.addGroup(noFillsGroup);
	    }
	}

	NoOrderEventsDecoder noOrderEvents = decoder.noOrderEvents();
	if (noOrderEvents.count() > 0) {
	    for (NoOrderEventsDecoder noOrderEventsDecoder : noOrderEvents) {
		Group noOrderEventsGroup = new Group(NO_ORDER_EVENTS, ORDER_EVENT_PRICE);
		setString(noOrderEventsGroup, ORDER_EVENT_PRICE,
			Long.toString(noOrderEventsDecoder.orderEventPx().mantissa()));
		setString(noOrderEventsGroup, ORDER_EVENT_TEXT, noOrderEventsDecoder.orderEventText(),
			!noOrderEventsDecoder.orderEventText().isEmpty());
		setString(noOrderEventsGroup, ORDER_EVENT_EXEC_ID,
			Long.toString(noOrderEventsDecoder.orderEventExecID()));
		setString(noOrderEventsGroup, ORDER_EVENT_QTY, Long.toString(noOrderEventsDecoder.orderEventQty()));
		setString(noOrderEventsGroup, ORDER_EVENT_TYPE,
			Short.toString(noOrderEventsDecoder.orderEventTypeRaw()));
		setString(noOrderEventsGroup, ORDER_EVENT_REASON,
			Short.toString(noOrderEventsDecoder.orderEventReason()));
		setString(noOrderEventsGroup, CONTRA_GROSS_TRADE_AMOUNT,
			Long.toString(noOrderEventsDecoder.contraGrossTradeAmt().mantissa()), Decimal64NULLDecoder
				.mantissaNullValue() != noOrderEventsDecoder.contraGrossTradeAmt().mantissa());
		setString(noOrderEventsGroup, CONTRA_CALCULATED_CCY_LAST_QTY,
			Long.toString(noOrderEventsDecoder.contraCalculatedCcyLastQty().mantissa()),
			Decimal64NULLDecoder.mantissaNullValue() != noOrderEventsDecoder.contraCalculatedCcyLastQty()
				.mantissa());
		fixMessage.addGroup(noOrderEventsGroup);
	    }
	}

	setString(fixMessage, RESERVATION_PRICE, Long.toString(decoder.reservationPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.reservationPrice().mantissa());
	setString(fixMessage, PriorityIndicator.FIELD, Short.toString(decoder.priorityIndicator()),
		ExecutionReportTradeOutright525Decoder.priorityIndicatorNullValue() != decoder.priorityIndicator());

    }

    private static void mapExecReportStatus(ExecutionReportStatus532Decoder decoder, Message fixMessage) {

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
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.price().mantissa());
	setString(fixMessage, StopPx.FIELD, Long.toString(decoder.stopPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.stopPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));

	setString(fixMessage, OrdStatusReqID.FIELD, Long.toString(decoder.ordStatusReqID()),
		ExecutionReportStatus532Decoder.ordStatusReqIDNullValue() != decoder.ordStatusReqID());
	setString(fixMessage, MassStatusReqID.FIELD, Long.toString(decoder.massStatusReqID()),
		ExecutionReportStatus532Decoder.massStatusReqIDNullValue() != decoder.massStatusReqID());

	setString(fixMessage, CrossID.FIELD, Long.toString(decoder.crossID()),
		ExecutionReportStatus532Decoder.crossIDNullValue() != decoder.crossID());
	setString(fixMessage, HostCrossID.FIELD, Long.toString(decoder.hostCrossID()),
		ExecutionReportStatus532Decoder.hostCrossIDNullValue() != decoder.hostCrossID());

	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));

	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));

	setString(fixMessage, LeavesQty.FIELD, Long.toString(decoder.leavesQty()));

	setString(fixMessage, MinQty.FIELD, Long.toString(decoder.minQty()),
		ExecutionReportStatus532Decoder.minQtyNullValue() != decoder.minQty());
	setString(fixMessage, DisplayQty.FIELD, Long.toString(decoder.displayQty()),
		ExecutionReportStatus532Decoder.displayQtyNullValue() != decoder.displayQty());

	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()),
		ExecutionReportStatus532Decoder.expireDateNullValue() != decoder.expireDate());

	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus().value()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));

	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));

	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));

	setString(fixMessage, LastRptRequested.FIELD, shortToFIXBooleanValue(decoder.lastRptRequested().value()),
		BooleanNULL.NULL_VAL != decoder.lastRptRequested());

	setString(fixMessage, CrossType.FIELD, Short.toString(decoder.crossType()),
		ExecutionReportStatus532Decoder.crossTypeNullValue() != decoder.crossType());
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, RESERVATION_PRICE, Long.toString(decoder.reservationPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.reservationPrice().mantissa());
	setString(fixMessage, PriorityIndicator.FIELD, Short.toString(decoder.priorityIndicator()),
		ExecutionReportStatus532Decoder.priorityIndicatorNullValue() != decoder.priorityIndicator());
	setString(fixMessage, ORIG_ORDER_USER, decoder.origOrderUser(), !decoder.origOrderUser().isEmpty());
	setString(fixMessage, CANCEL_TEXT, decoder.cancelText(), !decoder.cancelText().isEmpty());
    }

    private static void mapExecReportPendingReplace(ExecutionReportPendingReplace565Decoder decoder,
	    Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()));
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));
	setString(fixMessage, LeavesQty.FIELD, Long.toString(decoder.leavesQty()));
	setString(fixMessage, MinQty.FIELD, Long.toString(decoder.minQty()),
		ExecutionReportPendingReplace565Decoder.minQtyNullValue() != decoder.minQty());
	setString(fixMessage, DisplayQty.FIELD, Long.toString(decoder.displayQty()),
		ExecutionReportPendingReplace565Decoder.displayQtyNullValue() != decoder.displayQty());
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		ExecutionReportPendingReplace565Decoder.delayToTimeNullValue() != decoder.delayToTime());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, PriorityIndicator.FIELD, Short.toString(decoder.priorityIndicator()),
		ExecutionReportPendingReplace565Decoder.priorityIndicatorNullValue() != decoder.priorityIndicator());

    }

    private static void mapExecReportModify(ExecutionReportModify531Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()));
	setString(fixMessage, StopPx.FIELD, Long.toString(decoder.stopPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.stopPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, CrossID.FIELD, Long.toString(decoder.crossID()),
		ExecutionReportModify531Decoder.crossIDNullValue() != decoder.crossID());
	setString(fixMessage, HostCrossID.FIELD, Long.toString(decoder.hostCrossID()),
		ExecutionReportModify531Decoder.hostCrossIDNullValue() != decoder.hostCrossID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));
	setString(fixMessage, LeavesQty.FIELD, Long.toString(decoder.leavesQty()));
	setString(fixMessage, MinQty.FIELD, Long.toString(decoder.minQty()),
		ExecutionReportModify531Decoder.minQtyNullValue() != decoder.minQty());
	setString(fixMessage, DisplayQty.FIELD, Long.toString(decoder.displayQty()),
		ExecutionReportModify531Decoder.displayQtyNullValue() != decoder.displayQty());
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, DELAY_DURATION, Integer.toString(decoder.delayDuration()),
		ExecutionReportModify531Decoder.delayDurationNullValue() != decoder.delayDuration());
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, CrossType.FIELD, Short.toString(decoder.crossType()),
		ExecutionReportModify531Decoder.crossTypeNullValue() != decoder.crossType());
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		ExecutionReportModify531Decoder.delayToTimeNullValue() != decoder.delayToTime());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, PriorityIndicator.FIELD, Short.toString(decoder.priorityIndicator()),
		ExecutionReportModify531Decoder.priorityIndicatorNullValue() != decoder.priorityIndicator());

    }

    private static void mapExecReportElimination(ExecutionReportElimination524Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()));
	setString(fixMessage, StopPx.FIELD, Long.toString(decoder.stopPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.stopPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, CrossID.FIELD, Long.toString(decoder.crossID()),
		ExecutionReportElimination524Decoder.crossIDNullValue() != decoder.crossID());
	setString(fixMessage, HostCrossID.FIELD, Long.toString(decoder.hostCrossID()),
		ExecutionReportElimination524Decoder.hostCrossIDNullValue() != decoder.hostCrossID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, MinQty.FIELD, Long.toString(decoder.minQty()),
		ExecutionReportElimination524Decoder.minQtyNullValue() != decoder.minQty());
	setString(fixMessage, DisplayQty.FIELD, Long.toString(decoder.displayQty()),
		ExecutionReportElimination524Decoder.displayQtyNullValue() != decoder.displayQty());
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, CrossType.FIELD, Short.toString(decoder.crossType()),
		ExecutionReportElimination524Decoder.crossTypeNullValue() != decoder.crossType());
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, RESERVATION_PRICE, Long.toString(decoder.reservationPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.reservationPrice().mantissa());
	setString(fixMessage, PriorityIndicator.FIELD, Short.toString(decoder.priorityIndicator()),
		ExecutionReportElimination524Decoder.priorityIndicatorNullValue() != decoder.priorityIndicator());
    }

    private static void mapExecReportPendingCancel(ExecutionReportPendingCancel564Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()));
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));
	setString(fixMessage, LeavesQty.FIELD, Long.toString(decoder.leavesQty()));
	setString(fixMessage, MinQty.FIELD, Long.toString(decoder.minQty()),
		ExecutionReportPendingCancel564Decoder.minQtyNullValue() != decoder.minQty());
	setString(fixMessage, DisplayQty.FIELD, Long.toString(decoder.displayQty()),
		ExecutionReportPendingCancel564Decoder.displayQtyNullValue() != decoder.displayQty());
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		ExecutionReportPendingCancel564Decoder.delayToTimeNullValue() != decoder.delayToTime());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, RESERVATION_PRICE, Long.toString(decoder.reservationPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.reservationPrice().mantissa());
	setString(fixMessage, PriorityIndicator.FIELD, Short.toString(decoder.priorityIndicator()),
		ExecutionReportPendingCancel564Decoder.priorityIndicatorNullValue() != decoder.priorityIndicator());
	setString(fixMessage, ORIG_ORDER_USER, decoder.origOrderUser(), !decoder.origOrderUser().isEmpty());
	setString(fixMessage, CANCEL_TEXT, decoder.cancelText(), !decoder.cancelText().isEmpty());

    }

    private static void mapExecReportCancel(ExecutionReportCancel534Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()));
	setString(fixMessage, StopPx.FIELD, Long.toString(decoder.stopPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.stopPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, CrossID.FIELD, Long.toString(decoder.crossID()),
		ExecutionReportCancel534Decoder.crossIDNullValue() != decoder.crossID());
	setString(fixMessage, HostCrossID.FIELD, Long.toString(decoder.hostCrossID()),
		ExecutionReportCancel534Decoder.hostCrossIDNullValue() != decoder.hostCrossID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, CumQty.FIELD, Long.toString(decoder.cumQty()));
	setString(fixMessage, MinQty.FIELD, Long.toString(decoder.minQty()),
		ExecutionReportCancel534Decoder.minQtyNullValue() != decoder.minQty());
	setString(fixMessage, DisplayQty.FIELD, Long.toString(decoder.displayQty()),
		ExecutionReportCancel534Decoder.displayQtyNullValue() != decoder.displayQty());
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, DELAY_DURATION, Integer.toString(decoder.delayDuration()),
		ExecutionReportCancel534Decoder.delayDurationNullValue() != decoder.delayDuration());
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, OrdType.FIELD, byteToString(decoder.ordTypeRaw()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, ExecRestatementReason.FIELD, Short.toString(decoder.execRestatementReason().value()),
		ExecReason.NULL_VAL != decoder.execRestatementReason());
	setString(fixMessage, CrossType.FIELD, Short.toString(decoder.crossType()),
		ExecutionReportCancel534Decoder.crossTypeNullValue() != decoder.crossType());
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		ExecutionReportCancel534Decoder.delayToTimeNullValue() != decoder.delayToTime());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, RESERVATION_PRICE, Long.toString(decoder.reservationPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.reservationPrice().mantissa());
	setString(fixMessage, PriorityIndicator.FIELD, Short.toString(decoder.priorityIndicator()),
		ExecutionReportCancel534Decoder.priorityIndicatorNullValue() != decoder.priorityIndicator());
	setString(fixMessage, ORIG_ORDER_USER, decoder.origOrderUser(), !decoder.origOrderUser().isEmpty());
	setString(fixMessage, CANCEL_TEXT, decoder.cancelText(), !decoder.cancelText().isEmpty());
    }

    private static void mapExecReportNew(ExecutionReportNew522Decoder decoder, Message fixMessage) {

	String seqNum = Long.toString(decoder.seqNum());
	setMsgSeqNum(seqNum, fixMessage);
	setString(fixMessage, SEQ_NUM, seqNum);
	setString(fixMessage, UUID, Long.toString(decoder.uUID()));
	setString(fixMessage, ExecID.FIELD, decoder.execID());
	setString(fixMessage, SENDER_ID, decoder.senderID());
	setString(fixMessage, ClOrdID.FIELD, decoder.clOrdID());
	setString(fixMessage, PARTY_DETAILS_LIST_REQ_ID, Long.toString(decoder.partyDetailsListReqID()));
	setString(fixMessage, OrderID.FIELD, Long.toString(decoder.orderID()));
	setString(fixMessage, Price.FIELD, Long.toString(decoder.price().mantissa()));
	setString(fixMessage, StopPx.FIELD, Long.toString(decoder.stopPx().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.stopPx().mantissa());
	setString(fixMessage, TransactTime.FIELD, Long.toString(decoder.transactTime()));
	setString(fixMessage, SENDING_TIME_EPOCH, Long.toString(decoder.sendingTimeEpoch()));
	setString(fixMessage, ORDER_REQUEST_ID, Long.toString(decoder.orderRequestID()));
	setString(fixMessage, CrossID.FIELD, Long.toString(decoder.crossID()),
		ExecutionReportNew522Decoder.crossIDNullValue() != decoder.crossID());
	setString(fixMessage, HostCrossID.FIELD, Long.toString(decoder.hostCrossID()),
		ExecutionReportNew522Decoder.hostCrossIDNullValue() != decoder.hostCrossID());
	setString(fixMessage, LOCATION, decoder.location());
	setString(fixMessage, SecurityID.FIELD, Integer.toString(decoder.securityID()));
	setString(fixMessage, OrderQty.FIELD, Long.toString(decoder.orderQty()));
	setString(fixMessage, MinQty.FIELD, Long.toString(decoder.minQty()),
		ExecutionReportNew522Decoder.minQtyNullValue() != decoder.minQty());
	setString(fixMessage, DisplayQty.FIELD, Long.toString(decoder.displayQty()),
		ExecutionReportNew522Decoder.displayQtyNullValue() != decoder.displayQty());
	setString(fixMessage, ExpireDate.FIELD, Integer.toString(decoder.expireDate()));
	setString(fixMessage, DELAY_DURATION, Integer.toString(decoder.delayDuration()),
		ExecutionReportNew522Decoder.delayDurationNullValue() != decoder.delayDuration());
	setString(fixMessage, OrdStatus.FIELD, byteToString(decoder.ordStatus()));
	setString(fixMessage, ExecType.FIELD, byteToString(decoder.execType()));
	setString(fixMessage, Side.FIELD, Short.toString(decoder.side().value()));
	setString(fixMessage, TimeInForce.FIELD, Short.toString(decoder.timeInForce().value()));
	setString(fixMessage, ManualOrderIndicator.FIELD,
		shortToFIXBooleanValue(decoder.manualOrderIndicator().value()));
	setString(fixMessage, POSS_RETRANS_FLAG, shortToFIXBooleanValue(decoder.possRetransFlag().value()));
	setString(fixMessage, SPLIT_MSG, Short.toString(decoder.splitMsg().value()),
		SplitMsg.NULL_VAL != decoder.splitMsg());
	setString(fixMessage, CrossType.FIELD, Short.toString(decoder.crossType()),
		ExecutionReportNew522Decoder.crossTypeNullValue() != decoder.crossType());
	setExecInstString(decoder.execInst().aON(), decoder.execInst().nH(), decoder.execInst().oB(), fixMessage);
	setString(fixMessage, EXECUTION_MODE, byteToString(decoder.executionModeRaw()),
		ExecMode.NULL_VAL != decoder.executionMode());
	setString(fixMessage, LIQUIDITY_FLAG, shortToFIXBooleanValue(decoder.liquidityFlag().value()),
		BooleanNULL.NULL_VAL != decoder.liquidityFlag());
	setString(fixMessage, MANAGED_ORDER, shortToFIXBooleanValue(decoder.managedOrder().value()),
		BooleanNULL.NULL_VAL != decoder.managedOrder());
	setString(fixMessage, SHORT_SALE_TYPE, Short.toString(decoder.shortSaleType().value()),
		ShortSaleType.NULL_VAL != decoder.shortSaleType());
	setString(fixMessage, DELAY_TO_TIME, Long.toString(decoder.delayToTime()),
		ExecutionReportNew522Decoder.delayToTimeNullValue() != decoder.delayToTime());
	setString(fixMessage, DiscretionPrice.FIELD, Long.toString(decoder.discretionPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.discretionPrice().mantissa());
	setString(fixMessage, RESERVATION_PRICE, Long.toString(decoder.reservationPrice().mantissa()),
		PRICENULL9Decoder.mantissaNullValue() != decoder.reservationPrice().mantissa());
	setString(fixMessage, PriorityIndicator.FIELD, Short.toString(decoder.priorityIndicator()),
		ExecutionReportNew522Decoder.priorityIndicatorNullValue() != decoder.priorityIndicator());
    }

    static void setExecInstString(boolean aon, boolean nh, boolean ob, Message fixMessage) {
	StringBuffer execInstString = new StringBuffer();
	if (aon) {
	    execInstString.append(ExecInst.ALL_OR_NONE_AON).append(" ");
	}
	if (nh) {
	    execInstString.append(ExecInst.NOT_HELD).append(" ");
	}
	if (ob) {
	    execInstString.append(ExecInst.BEST_EXECUTION);
	}
	if (execInstString.length() > 0) {
	    setString(fixMessage, ExecInst.FIELD, execInstString.toString().trim());
	}
    }

}
