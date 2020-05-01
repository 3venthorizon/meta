package com.devlambda.proto.modbus.serial.rtu;


import static org.junit.Assert.*;

import org.junit.Test;

import com.devlambda.meta.modbus.PDU;
import com.devlambda.meta.modbus.PDUFactory;
import com.devlambda.meta.modbus.serial.rtu.RTU;
import com.devlambda.meta.protocol.ProtocolException;
import com.devlambda.meta.protocol.util.DataBuilder;
import com.devlambda.meta.protocol.util.ProtoUtils;


/**
 * @author Dewald Pretorius
 */
public class RTUTest {

   @Test
   public void testRTU() {
      long slaveAddress = 0x01;
      PDUFactory.Master requestFactory = new PDUFactory.Master();
      PDU pdu = requestFactory.readHoldingRegisters(0x0000, 0x0002);

      RTU result = new RTU(slaveAddress, pdu);

      assertEquals(pdu.getRawdata().length + 3, result.getRawdata().length);
      assertEquals(slaveAddress, result.getDeviceAddress());
      DataBuilder data = ProtoUtils.BE(result.getRawdata());
      assertEquals(slaveAddress, data.getLong(Byte.BYTES));
      assertEquals(pdu.getFunctionCode(), data.getInt(Byte.BYTES));
      assertEquals(0, data.getInt(Short.BYTES));
      assertEquals(2, data.getInt(Short.BYTES));
      assertEquals(0xC40B, data.getInt(Short.BYTES));
   }

   @Test
   public void testRTUcrcFailure() {
      long slaveAddress = 0x01;
      PDUFactory.Master requestFactory = new PDUFactory.Master();
      PDU pdu = requestFactory.readHoldingRegisters(0x0000, 0x0002);

      RTU rtu = new RTU(slaveAddress, pdu);
      DataBuilder data = ProtoUtils.BE(rtu.getRawdata());
      data.offset(data.data.length - 2).setShort(0); // clear crc

      try {
         @SuppressWarnings("unused") RTU result = new RTU(data.data);
      } catch (ProtocolException pe) {
         pe.printStackTrace();
         return;
      }

      fail("Expected Protocol Exception");
   }
}
