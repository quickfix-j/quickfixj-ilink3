
package org.quickfixj.ilink3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import iLinkBinary.ExecMode;
import iLinkBinary.ListUpdAct;
import iLinkBinary.MassActionOrdTyp;
import quickfix.Message;
import quickfix.field.MsgType;

public class ILink3MessageConverterTest {

    @Test
    public void testConvert() {
	Message fixMessage = ILink3MessageConverter.createFixMessage(MsgType.EXECUTION_REPORT);
	assertTrue(fixMessage instanceof quickfix.fix50.ExecutionReport);

	assertEquals("A", ILink3MessageConverter.byteToString(ListUpdAct.Add.value()));
	assertEquals("P", ILink3MessageConverter.byteToString(ExecMode.Passive.value()));
	assertEquals("\0", ILink3MessageConverter.byteToString(ExecMode.NULL_VAL.value()));

	int i = 52;
	assertEquals(MassActionOrdTyp.StopLimit, MassActionOrdTyp.get((byte) i));
    }
}
