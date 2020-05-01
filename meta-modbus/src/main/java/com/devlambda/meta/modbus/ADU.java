package com.devlambda.meta.modbus;


import com.devlambda.meta.protocol.Packet;
import com.devlambda.meta.protocol.util.DataBuilder;


/**
 * <p>
 * ADU: Application Data Unit
 * </p>
 * 
 * @author Dewald Pretorius
 */
public abstract class ADU extends Packet {

   /**
    * @param rawdata
    */
   public ADU(byte[] rawdata) {
      super(rawdata);
   }

   public ADU(DataBuilder builder) {
      super(builder.data);
   }

   public abstract long getDeviceAddress();

   public abstract PDU getPDU();
}
