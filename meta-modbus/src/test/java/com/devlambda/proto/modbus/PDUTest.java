package com.devlambda.proto.modbus;


import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.devlambda.meta.modbus.PDU;
import com.devlambda.meta.modbus.PDUFactory;


/**
 * PDUTest
 *
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class PDUTest {

   @Spy PDUFactory.Master masterFactory;
   @Spy PDUFactory.Slave slaveFactory;

   @Test
   public void requestReadCoils() {
      Integer address = 0xF8E3;
      Integer coils = 2000;
      PDU pdu = masterFactory.readCoils(address, coils);

      assertNotNull(pdu);
      assertEquals(PDU.READ_COILS, (byte) pdu.getFunctionCode());

      PDU.Request request = pdu.request();

      assertNotNull(request);
      assertEquals(pdu.getFunctionCode(), request.functionCode());
      assertEquals(address, request.readAddress());
      assertEquals(coils, request.readQuantity());
   }

   @Test
   public void responseReadCoils() {
      int[] expected = { 0xCD, 0x6B, 0x05 };
      boolean[] coils = { true, false, true, true, false, false, true, true, // 0xCD
         true, true, false, true, false, true, true, false, // 0x6B
         true, false, true }; // 0x5
      PDU pdu = slaveFactory.readCoils(coils);

      assertNotNull(pdu);
      assertEquals(PDU.READ_COILS, (byte) pdu.getFunctionCode());

      PDU.Response response = pdu.response();

      assertNotNull(response);
      assertEquals(pdu.getFunctionCode(), response.functionCode());
      assertArrayEquals(expected, response.values());
   }

   @Test
   public void requestReadHoldingRegisters() {
      Integer address = 0x9ABC;
      Integer registers = 123;
      PDU pdu = masterFactory.readHoldingRegisters(address, registers);

      assertNotNull(pdu);
      assertEquals(PDU.READ_HOLDING_REGISTERS, (byte) pdu.getFunctionCode());

      PDU.Request request = pdu.request();

      assertNotNull(request);
      assertEquals(pdu.getFunctionCode(), request.functionCode());
      assertEquals(address, request.readAddress());
      assertEquals(registers, request.readQuantity());
   }

   @Test
   public void responseReadHoldingRegisters() {
      int[] registers = { 0x0001, 0xFFFF, 0x1234, 1234, 0xABCD, 0xC0DE };
      PDU pdu = slaveFactory.readHoldingRegisters(registers);

      assertNotNull(pdu);
      assertEquals(PDU.READ_HOLDING_REGISTERS, (byte) pdu.getFunctionCode());

      PDU.Response response = pdu.response();

      assertNotNull(response);
      assertEquals(pdu.getFunctionCode(), response.functionCode());
      assertArrayEquals(registers, response.values());
   }

   @Test
   public void requestWriteMultipleRegisters() {
      Integer address = 0xADD5;
      int[] registers = { 0x0001, 0xFFFF, 0x1234, 1234, 0xABCD, 0xC0DE };
      PDU pdu = masterFactory.writeMultipleRegisters(address, registers);

      assertNotNull(pdu);
      assertEquals(PDU.WRITE_MULTIPLE_REGISTERS, (byte) pdu.getFunctionCode());

      PDU.Request request = pdu.request();

      assertNotNull(request);
      assertEquals(pdu.getFunctionCode(), request.functionCode());
      assertEquals(address, request.writeAddress());
      assertArrayEquals(registers, request.values());
   }

   @Test
   public void responseWriteMultipleRegisters() {
      Integer address = 0xACDC;
      Integer quantity = 007;
      PDU pdu = slaveFactory.writeMultipleRegisters(address, quantity);

      assertNotNull(pdu);
      assertEquals(PDU.WRITE_MULTIPLE_REGISTERS, (byte) pdu.getFunctionCode());

      PDU.Response response = pdu.response();

      assertNotNull(response);
      assertEquals(pdu.getFunctionCode(), response.functionCode());
      assertEquals(address, response.address());
      assertEquals(quantity, response.quantity());
   }

   @Test
   public void requestReportSlaveID() {
      PDU pdu = masterFactory.reportSlaveID();

      assertNotNull(pdu);
      assertEquals(PDU.REPORT_SLAVEID, (byte) pdu.getFunctionCode());

      PDU.Request request = pdu.request();
      assertNotNull(request);
      assertEquals(pdu.getFunctionCode(), request.functionCode());
   }

   @Test
   public void requestMaskWriteRegister() {
      Integer address = 0xADD5;
      Integer andmask = 0xAAAA;
      Integer ormask = 0x5555;
      PDU pdu = masterFactory.maskWriteRegister(address, andmask, ormask);

      assertNotNull(pdu);
      assertEquals(PDU.MASK_WRITE_REGISTER, (byte) pdu.getFunctionCode());

      PDU.Request request = pdu.request();

      assertNotNull(request);
      assertEquals(pdu.getFunctionCode(), request.functionCode());
      assertEquals(address, request.writeAddress());
      assertEquals(andmask, request.andMask());
      assertEquals(ormask, request.orMask());
   }

   @Test
   public void responseMaskWriteRegister() {
      Integer address = 0xADD5;
      Integer andmask = 0xAAAA;
      Integer ormask = 0x5555;
      PDU pdu = slaveFactory.maskWriteRegister(address, andmask, ormask);

      assertNotNull(pdu);
      assertEquals(PDU.MASK_WRITE_REGISTER, (byte) pdu.getFunctionCode());

      PDU.Response response = pdu.response();

      assertNotNull(response);
      assertEquals(pdu.getFunctionCode(), response.functionCode());
      assertEquals(address, response.address());
      assertEquals(andmask, response.andMask());
      assertEquals(ormask, response.orMask());
   }

   @Test
   public void requestWriteMultipleCoils() {
      Integer address = 0xC017;
      int[] expected = { 0xCD, 0x6B, 0x05 };
      boolean[] coils = { true, false, true, true, false, false, true, true, // 0xCD
         true, true, false, true, false, true, true, false, // 0x6B
         true, false, true }; // 0x5
      PDU pdu = masterFactory.writeMultipleCoils(address, coils);

      assertNotNull(pdu);
      assertEquals(PDU.WRITE_MULTIPLE_COILS, (byte) pdu.getFunctionCode());

      PDU.Request request = pdu.request();

      assertNotNull(request);
      assertEquals(pdu.getFunctionCode(), request.functionCode());
      assertEquals(address, request.writeAddress());
      assertArrayEquals(expected, request.values());
   }

   @Test
   public void responseWriteMultipleCoils() {
      Integer address = 0xC017;
      int[] expected = { 0xCD, 0x6B, 0x05 };
      PDU pdu = slaveFactory.writeMultipleCoils(address, expected.length);

      assertNotNull(pdu);
      assertEquals(PDU.WRITE_MULTIPLE_COILS, (byte) pdu.getFunctionCode());

      PDU.Response response = pdu.response();

      assertNotNull(response);
      assertEquals(pdu.getFunctionCode(), response.functionCode());
      assertEquals(address, response.address());
      assertEquals(expected.length, response.quantity().intValue());
   }

   @Test
   public void requestReadWriteMultipleRegisters() {
      Integer readAddress = 0x4444;
      Integer readAmount = 20;
      Integer writeAddress = 0xAAAA;
      int[] registers = { 0x0001, 0xFFFF, 0x1234, 1234, 0xABCD, 0xC0DE };
      PDU pdu = masterFactory.readWriteMulipleRegisters(readAddress, readAmount, writeAddress, registers);

      assertNotNull(pdu);
      assertEquals(PDU.READWRITE_MULTIPLE_REGISTERS, (byte) pdu.getFunctionCode());

      PDU.Request request = pdu.request();

      assertNotNull(request);
      assertEquals(pdu.getFunctionCode(), request.functionCode());
      assertEquals(readAddress, request.readAddress());
      assertEquals(readAmount, request.readQuantity());
      assertEquals(writeAddress, request.writeAddress());
      assertEquals(registers.length, request.writeQuantity().intValue());
      assertArrayEquals(registers, request.values());
   }

   @Test
   public void responseReadWriteMultipleRegisters() {
      int[] registers = { 0x0001, 0xFFFF, 0x1234, 1234, 0xABCD, 0xC0DE };
      PDU pdu = slaveFactory.readWriteMultipleRegisters(registers);

      assertNotNull(pdu);
      assertEquals(PDU.READWRITE_MULTIPLE_REGISTERS, (byte) pdu.getFunctionCode());

      PDU.Response response = pdu.response();

      assertNotNull(response);
      assertEquals(pdu.getFunctionCode(), response.functionCode());
      assertArrayEquals(registers, response.values());
   }
}
