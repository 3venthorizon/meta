package com.devlambda.meta.protocol.util;


/**
 * @author Dewald Pretorius
 */
public final class ProtoUtils {

   static final SetValue setBigEndian = 
      (byte[] data, int offset, long value, int bytes) -> setBigEndian(data, offset, value, bytes);
   static final GetValue getBigEndian = (byte[] data, int offset, int bytes) -> getBigEndian(data, offset, bytes);

   static final SetValue setLittleEndian = 
      (byte[] data, int offset, long value, int bytes) -> setLittleEndian(data, offset, value, bytes);
   static final GetValue getLittleEndian = (byte[] data, int offset, int bytes) -> getLittleEndian(data, offset, bytes);

   private ProtoUtils() {
   }

   public static DataBuilder BE(byte[] data) {
      return new DataBuilder(getBigEndian, setBigEndian, data);
   }

   public static DataBuilder LE(byte[] data) {
      return new DataBuilder(getLittleEndian, setLittleEndian, data);
   }

   public static long getBigEndian(byte[] data, int offset, int bytes) {
      long value = 0L;

      for (int position = 0, bits = (bytes << 3) - 8; position < bytes; position++, offset++, bits -= 8) {
         value += (data[offset] & 0xFFL) << bits;
      }

      return value;
   }

   public static long getLittleEndian(byte[] data, int offset, int bytes) {
      long value = 0L;

      for (int position = 0, bits = 0; position < bytes; position++, offset++, bits += 8) {
         value += (data[offset] & 0xFFL) << bits;
      }

      return value;
   }

   public static int setBigEndian(byte[] data, int offset, long value, int bytes) {
      for (int position = 0, bits = (bytes << 3) - 8; position < bytes; position++, offset++, bits -= 8) {
         data[offset] = (byte) (value >> bits);
      }

      return offset;
   }

   public static int setLittleEndian(byte[] data, int offset, long value, int bytes) {
      for (int position = 0, bits = 0; position < bytes; position++, offset++, bits += 8) {
         data[offset] = (byte) (value >> bits);
      }

      return offset;
   }
}
