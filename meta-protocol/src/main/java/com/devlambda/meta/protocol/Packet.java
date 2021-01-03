package com.devlambda.meta.protocol;

/**
 * @author Dewald Pretorius
 */
public class Packet {

   protected final long timestamp;

   protected byte[] rawdata;

   public Packet(byte[] rawdata) {
      timestamp = System.currentTimeMillis();
      this.rawdata = rawdata;
   }
   
   public byte[] getRawdata() { return rawdata; }

   public long getTimestamp() { return timestamp; }
}
