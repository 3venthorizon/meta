package com.devlambda.meta.modbus.serial.rtu;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.devlambda.meta.modbus.PDU;
import com.devlambda.meta.protocol.ProtocolException;
import com.devlambda.meta.protocol.Stream;


/**
 * RTUStream
 *
 * @author Dewald Pretorius
 */
public class RTUStream extends Stream<RTU> {

   protected static final int BUFFER_LENGTH = 256; // maximum RTU packet length

   protected final class Master extends Stream<RTU> {

      private Master() {
      }

      @Override
      public RTU read() throws IOException, ProtocolException {
         byte[] buffer = new byte[BUFFER_LENGTH];
         int offset = read(buffer, 0);
         if (offset == 1) offset = read(buffer, offset);
         int function = buffer[1] & 0xFF;

         if (function > 0x80) return readException(buffer, offset);

         switch (function) {
            case PDU.READ_COILS:
            case PDU.READ_DISCRETE_INPUTS:
            case PDU.READ_HOLDING_REGISTERS:
            case PDU.READ_INPUT_REGISTERS:
            case PDU.READWRITE_MULTIPLE_REGISTERS:
               return readRegisters(buffer, offset);

            case PDU.WRITE_SINGLE_COIL:
               return new RTU(RTUStream.this.readRawData(buffer, offset, 6));

            case PDU.WRITE_SINGLE_REGISTER:
               return new RTU(RTUStream.this.readRawData(buffer, offset, 8));

            case PDU.READ_EXCEPTION_STATUS:
               return new RTU(RTUStream.this.readRawData(buffer, offset, 5));

            default:
               in.reset();
               in.skip(2);
               throw new ProtocolException(String.format("Unknown Modbus Master-PDU: [0x%1$04X]", function));
         }

      }

      RTU readException(byte[] buffer, int offset) throws IOException {
         byte[] rawdata = RTUStream.this.readRawData(buffer, offset, 5);
         return new RTU(rawdata);
      }

      RTU readRegisters(byte[] buffer, int offset) throws IOException {
         while (offset < 3) {
            offset = read(buffer, offset);
         }

         // header + registers + crc
         byte[] rawdata = RTUStream.this.readRawData(buffer, offset, 3 + (buffer[2] & 0xFF) + 2);
         return new RTU(rawdata);
      }
   }

   protected final class Slave extends Stream<RTU> {

      private Slave() { }

      @Override
      public RTU read() throws IOException, ProtocolException {
         byte[] buffer = new byte[BUFFER_LENGTH];
         int offset = read(buffer, 0);
         if (offset == 1) offset = read(buffer, offset);
         int function = buffer[1] & 0xFF;

         switch (function) {
            case PDU.READ_COILS:
            case PDU.READ_DISCRETE_INPUTS:
            case PDU.READ_HOLDING_REGISTERS:
            case PDU.READ_INPUT_REGISTERS:
            case PDU.WRITE_SINGLE_COIL:
            case PDU.WRITE_SINGLE_REGISTER:
            case PDU.DIAGNOSTIC:
               return new RTU(RTUStream.this.readRawData(buffer, offset, 8));

            case PDU.READ_EXCEPTION_STATUS:
            case PDU.GET_COM_EVENT_COUNT:
            case PDU.GET_COM_LOG:
            case PDU.REPORT_SLAVEID:
               return new RTU(RTUStream.this.readRawData(buffer, offset, 4));

            case PDU.WRITE_MULTIPLE_COILS:
            case PDU.WRITE_MULTIPLE_REGISTERS:
               return writeRegisters(buffer, offset, 7);

            case PDU.READWRITE_MULTIPLE_REGISTERS:
               return writeRegisters(buffer, offset, 11);

            case PDU.MASK_WRITE_REGISTER:
               return new RTU(RTUStream.this.readRawData(buffer, offset, 10));

            case PDU.READ_FIFO:
               return new RTU(RTUStream.this.readRawData(buffer, offset, 6));

            default:
               in.reset();
               in.skip(2);
               throw new ProtocolException(String.format("Unknown Modbus Slave-PDU: [0x%1$04X]", function));
         }
      }

      RTU writeRegisters(byte[] buffer, int offset, int header) throws IOException {
         while (offset < header) {
            offset = read(buffer, offset);
         }

         int length = header + (buffer[header - 1] & 0xFF) + 2; // header + registers + crc
         byte[] rawdata = RTUStream.this.readRawData(buffer, offset, length);
         return new RTU(rawdata);
      }
   }

   protected Stream<RTU> decorated;

   /**
    * Prevent direct instantiation
    */
   protected RTUStream() {
   }

   public static RTUStream master() {
      RTUStream master = new RTUStream();
      master.decorated = master.new Master();
      return master;
   }

   public static RTUStream slave() {
      RTUStream slave = new RTUStream();
      slave.decorated = slave.new Slave();
      return slave;
   }

   @Override
   public void initialize(InputStream in, OutputStream out) {
      if (!(in instanceof BufferedInputStream)) in = new BufferedInputStream(in, BUFFER_LENGTH);
      super.initialize(in, out);
      decorated.initialize(in, out);
   }

   @Override
   public RTU read() throws IOException, ProtocolException {
      in.mark(BUFFER_LENGTH);
      return decorated.read();
   }

   @Override
   protected byte[] readRawData(byte[] buffer, int offset, int length) throws IOException {
      if (offset > length) {
         in.reset();
         in.skip(length);
      }

      return super.readRawData(buffer, offset, length);
   }
}
