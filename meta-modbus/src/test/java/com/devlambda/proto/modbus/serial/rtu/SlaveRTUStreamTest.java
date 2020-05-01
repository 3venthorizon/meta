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
public class SlaveRTUStreamTest {

   RTUStream stream;
   PDUFactory.Master pduFactory;

   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
      stream = spy(RTUStream.slave());
      pduFactory = new PDUFactory.Master();
   }

   @Test
   public void testReadHoldingRegisters() throws ProtocolException, IOException {
      int slaveAddress = 0x5A;
      int address = 0xBCDE;
      int registers = 0x2B;
      PDU pdu = pduFactory.readHoldingRegisters(address, registers);
      RTU rtu = new RTU(slaveAddress, pdu);
      ByteArrayInputStream in = new ByteArrayInputStream(rtu.getRawdata());
      stream.initialize(in, null);

      RTU result = stream.read();

      assertNotNull(result);
      assertEquals(slaveAddress, (int) result.getDeviceAddress());
      PDU pduResult = result.getPDU();
      assertNotNull(pduResult);
      assertEquals(PDU.READ_HOLDING_REGISTERS, (byte) pdu.getFunctionCode());
      PDU.Request request = pduResult.request();
      assertNotNull(request);
      assertEquals(address, request.readAddress().intValue());
      assertEquals(registers, request.readQuantity().intValue());
   }

   @Test
   public void testWriteMultipleRegisters() throws ProtocolException, IOException {
      int slaveAddress = 0xAD;
      int address = 0x3A7E;
      int[] registers = { 0x1A22, 0x3B34, 0x5C06, 0x7D58 };
      PDU pdu = pduFactory.writeMultipleRegisters(0x3A7E, registers);
      RTU rtu = new RTU(slaveAddress, pdu);
      ByteArrayInputStream in = new ByteArrayInputStream(rtu.getRawdata());
      stream.initialize(in, null);

      RTU result = stream.read();

      assertNotNull(result);
      assertEquals(slaveAddress, (int) result.getDeviceAddress());
      PDU pduResult = result.getPDU();
      assertNotNull(pduResult);
      assertEquals(PDU.WRITE_MULTIPLE_REGISTERS, (byte) pdu.getFunctionCode());
      PDU.Request request = pduResult.request();
      assertNotNull(request);
      assertEquals(address, request.writeAddress().intValue());
      assertArrayEquals(registers, request.values());
   }

   @Test
   public void testReadWriteMultipleRegisters() throws ProtocolException, IOException {
      int slaveAddress = 0x52;
      Integer readAddress = 0x4444;
      Integer readAmount = 20;
      Integer writeAddress = 0xAAAA;
      int[] registers = { 0x0001, 0xFFFF, 0x1234, 1234, 0xABCD, 0xC0DE };
      PDU pdu = pduFactory.readWriteMulipleRegisters(readAddress, readAmount, writeAddress, registers);
      RTU rtu = new RTU(slaveAddress, pdu);
      ByteArrayInputStream in = new ByteArrayInputStream(rtu.getRawdata());
      stream.initialize(in, null);

      RTU result = stream.read();

      assertNotNull(result);
      assertEquals(slaveAddress, (int) result.getDeviceAddress());
      PDU pduResult = result.getPDU();
      assertNotNull(pduResult);
      assertEquals(PDU.READWRITE_MULTIPLE_REGISTERS, (byte) pdu.getFunctionCode());
      PDU.Request request = pduResult.request();
      assertNotNull(request);
      assertEquals(readAddress, request.readAddress());
      assertEquals(readAmount, request.readQuantity());
      assertEquals(writeAddress, request.writeAddress());
      assertEquals(registers.length, request.writeQuantity().intValue());
      assertArrayEquals(registers, request.values());
   }
}
