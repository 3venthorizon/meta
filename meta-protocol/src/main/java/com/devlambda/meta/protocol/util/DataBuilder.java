package com.devlambda.meta.protocol.util;


/**
 * @author Dewald Pretorius
 */
public class DataBuilder {

   public final byte[] data;

   final GetValue getFunction;
   final SetValue setFunction;
   int offset;

   public DataBuilder(GetValue getFunction, SetValue setFunction, byte[] data) {
      this.getFunction = getFunction;
      this.setFunction = setFunction;
      this.data = data;
      this.offset = 0;
   }

   public DataBuilder offset(int offset) {
      this.offset = offset;
      return this;
   }

   public byte[] getBytes(int length) {
      byte[] bytes = new byte[length];
      System.arraycopy(data, offset, bytes, 0, length);
      offset += length;

      return bytes;
   }

   public DataBuilder set(byte[] bytes) {
      System.arraycopy(bytes, 0, data, offset, bytes.length);
      offset += bytes.length;

      return this;
   }

   public DataBuilder set(byte value) {
      data[offset++] = value;
      return this;
   }

   public DataBuilder set(short value) {
      setFunction.function(data, offset, value, Short.BYTES);
      offset += Short.BYTES;
      return this;
   }

   public DataBuilder set(int value) {
      setFunction.function(data, offset, value, Integer.BYTES);
      offset += Integer.BYTES;
      return this;
   }

   public void set(float value) {
      int bits = Float.floatToRawIntBits(value);
      set(bits);
   }

   public void set(double value) {
      long bits = Double.doubleToLongBits(value);
      set(bits);
   }

   public DataBuilder set(long value) {
      setFunction.function(data, offset, value, Long.BYTES);
      offset += Long.BYTES;
      return this;
   }

   public DataBuilder setByte(long value) {
      setFunction.function(data, offset, value, Byte.BYTES);
      offset += Byte.BYTES;
      return this;
   }

   public DataBuilder setShort(long value) {
      setFunction.function(data, offset, value, Short.BYTES);
      offset += Short.BYTES;
      return this;
   }

   public DataBuilder setInt(long value) {
      setFunction.function(data, offset, value, Integer.BYTES);
      offset += Integer.BYTES;
      return this;
   }

   public DataBuilder setLong(long value) {
      setFunction.function(data, offset, value, Long.BYTES);
      offset += Long.BYTES;
      return this;
   }

   public byte getByte() { return data[offset++]; }

   public short getShort(int bytes) {
      return (short) getLong(bytes);
   }

   public short getShort() {
      long value = getFunction.function(data, offset, Short.BYTES);
      offset += Short.BYTES;
      return (short) value;
   }

   public int getInt(int bytes) {
      return (int) getLong(bytes);
   }

   public int getInt() {
      long value = getFunction.function(data, offset, Integer.BYTES);
      offset += Integer.BYTES;
      return (int) value;
   }

   public long getLong(int bytes) {
      long mask = 0xFFL;

      for (int x = 1; x < bytes; x++) {
         mask = (mask << 8) + 0xFFL;
      }

      long value = getFunction.function(data, offset, bytes) & mask;
      offset += bytes;
      return value;
   }

   public long getLong() {
      long value = getFunction.function(data, offset, Long.BYTES);
      offset += Long.BYTES;
      return value;
   }

   public float getFloat() {
      int bits = getInt();
      return Float.intBitsToFloat(bits);
   }

   public double getDouble() {
      long bits = getLong();
      return Double.longBitsToDouble(bits);
   }
}
