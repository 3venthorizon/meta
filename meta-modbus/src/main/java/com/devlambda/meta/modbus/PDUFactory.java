package com.devlambda.meta.modbus;


import static com.devlambda.meta.protocol.util.ProtoUtils.*;

import java.util.Arrays;

import com.devlambda.meta.protocol.util.DataBuilder;


/**
 * @author Dewald Pretorius
 */
public abstract class PDUFactory {

   public static class Master {
      public PDU readCoils(int address, int coils) {
         if (coils < 1 || coils > 2000) throw new IllegalArgumentException("Expected number of coils [1..2000]");
         return new PDU(BE(new byte[5]).setByte(PDU.READ_COILS).setShort(address).setShort(coils));
      }

      public PDU readDiscreteInputs(int address, int inputs) {
         if (inputs < 1 || inputs > 2000) throw new IllegalArgumentException("Expected number of inputs [1..2000]");
         return new PDU(BE(new byte[5]).setByte(PDU.READ_DISCRETE_INPUTS).setShort(address).setShort(inputs));
      }

      public PDU readHoldingRegisters(int address, int registers) {

         if (registers < 1 || registers > 125) {
            throw new IllegalArgumentException("Expected number of registers [1..125]");
         }

         return new PDU(BE(new byte[5]).setByte(PDU.READ_HOLDING_REGISTERS).setShort(address).setShort(registers));
      }

      public PDU readInputRegisters(int address, int registers) {

         if (registers < 1 || registers > 125) {
            throw new IllegalArgumentException("Expected number of registers [1..125]");
         }

         return new PDU(BE(new byte[5]).setByte(PDU.READ_INPUT_REGISTERS).setShort(address).setShort(registers));
      }

      public PDU writeSingleCoil(int address, boolean status) {
         int coil = status ? 0xFF00 : 0;
         return new PDU(BE(new byte[5]).setByte(PDU.WRITE_SINGLE_COIL).setShort(address).setShort(coil));
      }

      public PDU writeSingleRegister(int address, int value) {
         return new PDU(BE(new byte[5]).setByte(PDU.WRITE_SINGLE_REGISTER).setShort(address).setShort(value));
      }

      public PDU readExceptionStatus() {
         byte[] rawdata = { PDU.READ_EXCEPTION_STATUS };
         return new PDU(rawdata);
      }

      public PDU diagnostics(int subfunction, int data) {
         return new PDU(BE(new byte[5]).setByte(PDU.DIAGNOSTIC).setShort(subfunction).setShort(data));
      }

      public PDU getCommEventCounter() {
         byte[] rawdata = { PDU.GET_COM_EVENT_COUNT };
         return new PDU(rawdata);
      }

      public PDU getCommEventLog() {
         byte[] rawdata = { PDU.GET_COM_LOG };
         return new PDU(rawdata);
      }

      public PDU writeMultipleCoils(int address, boolean[] coils) {
         int outBytes = coils.length / 8;
         if (coils.length % 8 > 0) outBytes++;
         byte[] outputs = convert(coils);
         DataBuilder data = BE(new byte[outputs.length + 6]).setByte(PDU.WRITE_MULTIPLE_COILS).setShort(address)
               .setShort(coils.length).setByte(outBytes).set(outputs);
         return new PDU(data);
      }

      public PDU writeMultipleRegisters(int address, int[] registers) {
         int count = registers.length * 2;
         DataBuilder data = BE(new byte[6 + count]).setByte(PDU.WRITE_MULTIPLE_REGISTERS).setShort(address)
               .setShort(registers.length).setByte(count);

         for (int register : registers) {
            data.setShort(register);
         } // for

         return new PDU(data);
      }

      public PDU reportSlaveID() {
         byte[] rawdata = { PDU.REPORT_SLAVEID };
         return new PDU(rawdata);
      }

      public PDU maskWriteRegister(int address, int andmask, int ormask) {
         DataBuilder data = BE(new byte[7]).setByte(PDU.MASK_WRITE_REGISTER).setShort(address).setShort(andmask)
               .setShort(ormask);
         return new PDU(data);
      }

      public PDU readWriteMulipleRegisters(int readAddress, int readAmount, int writeAddress, int[] registers) {
         int writeTotal = registers.length * 2;
         byte[] rawdata = new byte[10 + registers.length * 2];
         DataBuilder data = BE(rawdata).setByte(PDU.READWRITE_MULTIPLE_REGISTERS).setShort(readAddress)
               .setShort(readAmount).setShort(writeAddress).setShort(registers.length).setByte(writeTotal);

         for (int register : registers) {
            data.setShort(register);
         } // for

         return new PDU(data);
      }

      public PDU readFIFOQueue(int pointer) {
         return new PDU(BE(new byte[3]).setByte(PDU.READ_FIFO).setShort(pointer));
      }
   }

   public static class Slave {
      public PDU error(int functionCode, int exceptionCode) {
         byte[] rawdata = { (byte) (0x80 + functionCode), (byte) exceptionCode };
         return new PDU(rawdata);
      }

      public PDU readCoils(boolean[] states) {
         byte[] stateBytes = convert(states);
         DataBuilder data = BE(new byte[stateBytes.length + 2]).setByte(PDU.READ_COILS).setByte(stateBytes.length)
               .set(stateBytes);
         return new PDU(data);
      }

      public PDU readDiscreteInputs(boolean[] states) {
         byte[] stateBytes = convert(states);
         byte[] rawdata = new byte[stateBytes.length + 2];
         DataBuilder data = BE(rawdata).setByte(PDU.READ_DISCRETE_INPUTS).setByte(stateBytes.length).set(stateBytes);
         return new PDU(data);
      }

      public PDU readHoldingRegisters(int[] registers) {
         byte[] registerBytes = convert(registers);
         DataBuilder data = BE(new byte[registerBytes.length + 2]).setByte(PDU.READ_HOLDING_REGISTERS)
               .setByte(registerBytes.length).set(registerBytes);
         return new PDU(data);
      }

      public PDU readInputRegisters(int[] registers) {
         byte[] registerBytes = convert(registers);
         DataBuilder data = BE(new byte[registerBytes.length + 2]).setByte(PDU.READ_INPUT_REGISTERS)
               .setByte(registerBytes.length).set(registerBytes);
         return new PDU(data);
      }

      public PDU writeSingleCoil(int address) {
         return new PDU(BE(new byte[3]).setByte(PDU.WRITE_SINGLE_COIL).setShort(address));
      }

      public PDU writeSingleRegister(int address, int value) {
         return new PDU(BE(new byte[5]).setByte(PDU.WRITE_SINGLE_REGISTER).setShort(address).setShort(value));
      }

      public PDU writeMultipleCoils(int address, int quantity) {
         return new PDU(BE(new byte[5]).setByte(PDU.WRITE_MULTIPLE_COILS).setShort(address).setShort(quantity));
      }

      public PDU writeMultipleRegisters(int address, int quantity) {
         return new PDU(BE(new byte[5]).setByte(PDU.WRITE_MULTIPLE_REGISTERS).setShort(address).setShort(quantity));
      }

      public PDU maskWriteRegister(int address, int andmask, int ormask) {
         DataBuilder data = BE(new byte[7]).setByte(PDU.MASK_WRITE_REGISTER).setShort(address).setShort(andmask)
               .setShort(ormask);
         return new PDU(data);
      }

      public PDU readWriteMultipleRegisters(int[] registers) {
         byte[] registerBytes = convert(registers);
         DataBuilder data = BE(new byte[registerBytes.length + 2]).setByte(PDU.READWRITE_MULTIPLE_REGISTERS)
               .setByte(registerBytes.length).set(registerBytes);
         return new PDU(data);
      }

      public PDU readExceptionStatus(int status) {
         byte[] rawdata = { PDU.READ_EXCEPTION_STATUS, (byte) status };
         return new PDU(rawdata);
      }
   }

   static byte[] convert(boolean[] states) {
      int stateBytes = states.length / 8;
      if (states.length % 8 > 0) stateBytes++;
      byte[] rawdata = new byte[stateBytes];
      DataBuilder builder = BE(rawdata);
      int stateIndex = 0;

      for (int offset = 0; offset < stateBytes; offset++) {
         int stateByte = 0;

         for (int bit = 0; bit < 8 && stateIndex < states.length; bit++, stateIndex++) {
            if (!states[stateIndex]) continue;
            stateByte |= 1 << bit;
         }

         builder.setByte(stateByte);
      }

      return rawdata;
   }

   static byte[] convert(int[] registers) {
      byte[] rawdata = new byte[registers.length * 2];
      DataBuilder builder = BE(rawdata);
      Arrays.stream(registers).forEach(register -> builder.setShort(register));

      return rawdata;
   }
}
