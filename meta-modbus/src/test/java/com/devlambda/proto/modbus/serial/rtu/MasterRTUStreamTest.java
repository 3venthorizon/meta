package com.devlambda.proto.modbus.serial.rtu;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.devlambda.meta.modbus.PDU;
import com.devlambda.meta.modbus.PDUFactory;
import com.devlambda.meta.modbus.serial.rtu.RTU;
import com.devlambda.meta.modbus.serial.rtu.RTUStream;
import com.devlambda.meta.protocol.ProtocolException;


/**
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class MasterRTUStreamTest {

   RTUStream stream;
   PDUFactory.Slave pduFactory;

   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
      stream = spy(RTUStream.master());
      pduFactory = new PDUFactory.Slave();
   }

   @Test
   public void testReadHoldingRegisters() throws ProtocolException, IOException {
      int slaveAddress = 0xDE;
      int[] registers = { 0x1A22, 0x3B34, 0x5C06, 0x7D58 };
      PDU pdu = pduFactory.readHoldingRegisters(registers);
      RTU rtu = new RTU(slaveAddress, pdu);
      ByteArrayInputStream in = new ByteArrayInputStream(rtu.getRawdata());
      stream.initialize(in, null);

      RTU result = stream.read();

      assertNotNull(result);
      assertEquals(slaveAddress, (int) result.getDeviceAddress());
      PDU pduResult = result.getPDU();
      assertNotNull(pduResult);
      assertEquals(PDU.READ_HOLDING_REGISTERS, (byte) pdu.getFunctionCode());
      PDU.Response response = pduResult.response();
      assertNotNull(response);
      assertArrayEquals(registers, response.values());
   }

   @Test
   public void testReadCoils() throws ProtocolException, IOException {
      int slaveAddress = 0xDE;
      int[] expected = { 0xCD, 0x6B, 0x05 };
      boolean[] states = { true, false, true, true, false, false, true, true, // 0xCD
         true, true, false, true, false, true, true, false, // 0x6B
         true, false, true }; // 0x5

      PDU pdu = pduFactory.readCoils(states);
      RTU rtu = new RTU(slaveAddress, pdu);
      ByteArrayInputStream in = new ByteArrayInputStream(rtu.getRawdata());
      stream.initialize(in, null);

      RTU result = stream.read();

      assertNotNull(result);
      assertEquals(slaveAddress, (int) result.getDeviceAddress());
      PDU pduResult = result.getPDU();
      assertNotNull(pduResult);
      assertEquals(PDU.READ_COILS, (byte) pdu.getFunctionCode());
      PDU.Response response = pduResult.response();
      assertNotNull(response);
      assertArrayEquals(expected, response.values());
   }

   @Test
   public void testReadWriteMultipleRegisters() throws ProtocolException, IOException {
      int slaveAddress = 0x52;
      int[] registers = { 0x0001, 0xFFFF, 0x1234, 1234, 0xABCD, 0xC0DE };
      PDU pdu = pduFactory.readWriteMultipleRegisters(registers);
      RTU rtu = new RTU(slaveAddress, pdu);
      ByteArrayInputStream in = new ByteArrayInputStream(rtu.getRawdata());
      stream.initialize(in, null);

      RTU result = stream.read();

      assertNotNull(result);
      assertEquals(slaveAddress, (int) result.getDeviceAddress());
      PDU pduResult = result.getPDU();
      assertNotNull(pduResult);
      assertEquals(PDU.READWRITE_MULTIPLE_REGISTERS, (byte) pdu.getFunctionCode());
      PDU.Response response = pduResult.response();
      assertNotNull(response);
      assertArrayEquals(registers, response.values());
   }
}
