package com.devlambda.meta.protocol;


import com.devlambda.meta.protocol.util.DataBuilder;


/**
 * @author Dewald Pretorius
 */
public abstract class Packet {

   public final long timestamp;

   protected byte[] rawdata;

   public Packet(byte[] rawdata) {
      timestamp = System.currentTimeMillis();
      this.rawdata = rawdata;
   }

   public Packet(DataBuilder builder) {
      this(builder.data);
   }

   public byte[] getRawdata() { return rawdata; }
}
