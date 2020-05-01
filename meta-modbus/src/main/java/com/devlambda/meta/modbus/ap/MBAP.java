package com.devlambda.meta.modbus.ap;


import com.devlambda.meta.modbus.ADU;
import com.devlambda.meta.modbus.PDU;
import com.devlambda.meta.protocol.util.ProtoUtils;


/**
 * MBAP
 *
 * @author Dewald Pretorius
 */
public class MBAP extends ADU {

   public static int PROTOCOL_MODBUS = 0;

   /**
    * @param rawdata
    */
   public MBAP(byte[] rawdata) {
      super(rawdata);
   }

   /**
    * @param builder
    */
   public MBAP(long unitId, int transactionId, int protocolId, PDU pdu) {
      super(new byte[pdu.getRawdata().length + 7]);

      ProtoUtils.BE(rawdata).setShort(transactionId).setShort(protocolId).setShort(rawdata.length + 1).setByte(unitId)
            .set(pdu.getRawdata());
   }

   @Override
   public long getDeviceAddress() { return rawdata[6] & 0xFFL; }

   @Override
   public PDU getPDU() {
      byte[] pdu = new byte[rawdata.length - 7];
      System.arraycopy(rawdata, 1, pdu, 0, pdu.length);
      return new PDU(pdu);
   }

   public int getTransactionId() { return ProtoUtils.BE(rawdata).getInt(Short.BYTES); }

   public int getProtocolId() { return ProtoUtils.BE(rawdata).offset(2).getInt(Short.BYTES); }
}
