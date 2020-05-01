package com.devlambda.proto.modbus;


import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.devlambda.meta.modbus.PDU;
import com.devlambda.meta.modbus.PDUFactory;


/**
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class PDUFactoryTest {

   @Spy PDUFactory.Master masterFactory;

   @Test
   public void requestReadHoldingRegisters() {
      PDU result = masterFactory.readHoldingRegisters(0xABCD, 12);

      byte[] rawdata = result.getRawdata();

      assertEquals(PDU.READ_HOLDING_REGISTERS, rawdata[0]);
      assertEquals((byte) 0xAB, rawdata[1]);
      assertEquals((byte) 0xCD, rawdata[2]);
      assertEquals((byte) 0x00, rawdata[3]);
      assertEquals((byte) 12, rawdata[4]);
   }
}
