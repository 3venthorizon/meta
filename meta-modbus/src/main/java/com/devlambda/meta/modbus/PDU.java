package com.devlambda.meta.modbus;


import static com.devlambda.meta.protocol.util.ProtoUtils.*;

import com.devlambda.meta.protocol.Packet;
import com.devlambda.meta.protocol.util.DataBuilder;


/**
 * <p>
 * PDU: Protocol Data Unit
 * </p>
 * 
 * @author Dewald Pretorius
 */
public class PDU extends Packet {

   public static final byte READ_COILS = 0x01;
   public static final byte READ_DISCRETE_INPUTS = 0x02;
   public static final byte READ_HOLDING_REGISTERS = 0x03;
   public static final byte READ_INPUT_REGISTERS = 0x04;
   public static final byte WRITE_SINGLE_COIL = 0x05;
   public static final byte WRITE_SINGLE_REGISTER = 0x06;
   public static final byte READ_EXCEPTION_STATUS = 0x07;
   public static final byte DIAGNOSTIC = 0x08;
   public static final byte GET_COM_EVENT_COUNT = 0x0B;
   public static final byte GET_COM_LOG = 0x0C;
   public static final byte WRITE_MULTIPLE_COILS = 0x0F;
   public static final byte WRITE_MULTIPLE_REGISTERS = 0x10;
   public static final byte REPORT_SLAVEID = 0x11;
   public static final byte READ_FILE = 0x14;
   public static final byte WRITE_FILE = 0x15;
   public static final byte MASK_WRITE_REGISTER = 0x16;
   public static final byte READWRITE_MULTIPLE_REGISTERS = 0x17;
   public static final byte READ_FIFO = 0x18;
   public static final byte READ_DEVICEID = 0x2B;

   public class Request {

      Request() { }

      public int functionCode() {
         return getFunctionCode();
      }

      public Integer readAddress() {

         switch (rawdata[0]) {
            case READ_COILS:
            case READ_DISCRETE_INPUTS:
            case READ_HOLDING_REGISTERS:
            case READ_INPUT_REGISTERS:
            case READWRITE_MULTIPLE_REGISTERS:
            case READ_FIFO:
               return BE(rawdata).offset(1).getInt(Short.BYTES);

            default:
               return null;
         }

      }

      public Integer writeAddress() {

         switch (rawdata[0]) {
            case WRITE_SINGLE_COIL:
            case WRITE_SINGLE_REGISTER:
            case WRITE_MULTIPLE_COILS:
            case WRITE_MULTIPLE_REGISTERS:
            case MASK_WRITE_REGISTER:
               return BE(rawdata).offset(1).getInt(Short.BYTES);

            case READWRITE_MULTIPLE_REGISTERS:
               return BE(rawdata).offset(5).getInt(Short.BYTES);

            default:
               return null;
         }

      }

      public int[] values() {
         DataBuilder builder = null;
         int bytes = Short.BYTES;

         switch (rawdata[0]) {
            case WRITE_SINGLE_COIL:
            case WRITE_SINGLE_REGISTER:
               return new int[] { BE(rawdata).offset(3).getInt(Short.BYTES) };

            case WRITE_MULTIPLE_COILS:
               bytes = Byte.BYTES;
            case WRITE_MULTIPLE_REGISTERS:
               builder = BE(rawdata).offset(5);
               break;

            case READWRITE_MULTIPLE_REGISTERS:
               builder = BE(rawdata).offset(9);
               break;

            default:
               return null;
         }

         int count = builder.getInt(Byte.BYTES) / bytes / Byte.BYTES;
         int[] registers = new int[count];

         for (int position = 0; position < count; position++) {
            registers[position] = builder.getInt(bytes);
         }

         return registers;
      }

      public Integer readQuantity() {
         switch (rawdata[0]) {
            case PDU.READ_COILS:
            case PDU.READ_DISCRETE_INPUTS:
            case PDU.READ_HOLDING_REGISTERS:
            case PDU.READ_INPUT_REGISTERS:
            case PDU.READWRITE_MULTIPLE_REGISTERS:
               return BE(rawdata).offset(3).getInt(Short.BYTES);

            default:
               return null;
         }
      }

      public Integer writeQuantity() {
         switch (rawdata[0]) {
            case PDU.WRITE_MULTIPLE_COILS:
            case PDU.WRITE_MULTIPLE_REGISTERS:
               return BE(rawdata).offset(3).getInt(Short.BYTES);

            case PDU.READWRITE_MULTIPLE_REGISTERS:
               return BE(rawdata).offset(7).getInt(Short.BYTES);

            default:
               return null;
         }
      }

      public Integer andMask() {
         if (rawdata[0] != MASK_WRITE_REGISTER) return null;
         return BE(rawdata).offset(3).getInt(Short.BYTES);
      }

      public Integer orMask() {
         if (rawdata[0] != MASK_WRITE_REGISTER) return null;
         return BE(rawdata).offset(5).getInt(Short.BYTES);
      }
   }

   public class Response {

      Response() {

      }

      public int functionCode() {
         return getFunctionCode();
      }

      public Integer exceptionCode() {
         if (rawdata[0] < 1) return null;
         return rawdata[1] & 0xFF;
      }

      public Integer address() {

         switch (rawdata[0]) {
            case WRITE_SINGLE_COIL:
            case WRITE_SINGLE_REGISTER:
            case WRITE_MULTIPLE_COILS:
            case WRITE_MULTIPLE_REGISTERS:
            case MASK_WRITE_REGISTER:
               return BE(rawdata).offset(1).getInt(Short.BYTES);

            default:
               return null;
         }

      }

      public int[] values() {
         DataBuilder builder = null;
         int bytes = Short.BYTES;

         switch (rawdata[0]) {
            case READ_COILS:
            case READ_DISCRETE_INPUTS:
               bytes = Byte.BYTES;
            case READ_HOLDING_REGISTERS:
            case READ_INPUT_REGISTERS:
            case READWRITE_MULTIPLE_REGISTERS:
               builder = BE(rawdata).offset(1);
               break;

            case WRITE_MULTIPLE_REGISTERS:
               builder = BE(rawdata).offset(3);
               break;

            case WRITE_SINGLE_REGISTER:
               return new int[] { BE(rawdata).offset(3).getInt(Short.BYTES) };

            default:
               return null;
         }

         int count = builder.getInt(Byte.BYTES) / bytes / Byte.BYTES;
         int[] registers = new int[count];

         for (int position = 0; position < count; position++) {
            registers[position] = builder.getInt(bytes);
         }

         return registers;
      }

      public Integer quantity() {
         switch (rawdata[0]) {
            case PDU.WRITE_MULTIPLE_COILS:
            case PDU.WRITE_MULTIPLE_REGISTERS:
               return BE(rawdata).offset(3).getInt(Short.BYTES);

            default:
               return null;
         }
      }

      public Integer andMask() {
         if (rawdata[0] != MASK_WRITE_REGISTER) return null;
         return BE(rawdata).offset(3).getInt(Short.BYTES);
      }

      public Integer orMask() {
         if (rawdata[0] != MASK_WRITE_REGISTER) return null;
         return BE(rawdata).offset(5).getInt(Short.BYTES);
      }
   }

   public PDU(byte[] rawdata) {
      super(rawdata);
   }

   public PDU(DataBuilder builder) {
      super(builder.data);
   }

   public int getFunctionCode() { return rawdata[0] & 0xFF; }
   public void setFunctionCode(int functionCode) { rawdata[0] = (byte) (functionCode & 0xFF); }

   /**
    * @return request pdu
    */
   public Request request() {
      return new Request();
   }

   /**
    * @return response pdu
    */
   public Response response() {
      return new Response();
   }
}
