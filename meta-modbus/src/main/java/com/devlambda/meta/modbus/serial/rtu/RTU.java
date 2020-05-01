package com.devlambda.meta.modbus.serial.rtu;


import com.devlambda.meta.modbus.ADU;
import com.devlambda.meta.modbus.PDU;
import com.devlambda.meta.protocol.ProtocolException;
import com.devlambda.meta.protocol.util.ProtoUtils;


/**
 * <p>
 * RTU: Remote Terminal Unit
 * </p>
 * 
 * @author Dewald Pretorius
 */
public class RTU extends ADU {

   protected static final int POLYNOME = 0xA001;

   /**
    * @param rawdata
    */
   public RTU(byte[] rawdata) {
      super(rawdata);

      int gencrc = generateCRC(this);
      int rawcrc = ProtoUtils.BE(rawdata).offset(rawdata.length - 2).getInt(Short.BYTES);

      if (gencrc != rawcrc) {
         String message = String.format("CRC Failure: expected [0x%1$04X] != [0x%2$04X] received", gencrc, rawcrc);
         throw new ProtocolException(message);
      }
   }

   public RTU(long slaveAddress, PDU pdu) {
      super(new byte[pdu.getRawdata().length + 3]);

      ProtoUtils.BE(rawdata).setByte(slaveAddress).set(pdu.getRawdata()).setShort(generateCRC(this));
   }

   public static int generateCRC(RTU rtu) {
      int crc = 0xFFFF;

      for (int x = 0; x < rtu.rawdata.length - 2; x++) {
         crc = crc ^ (rtu.rawdata[x] & 0x00FF);

         for (int bit = 0; bit < 8; bit++) {
            if ( (crc & 1) == 1) crc = ( (crc & 0xFFFF) >>> 1) ^ POLYNOME;
            else crc = (crc & 0xFFFF) >>> 1;
         } // for

      } // for

      int swap = (crc & 0xFF00) >> 8;
      crc = (crc & 0x00FF) << 8;
      crc += swap;

      return crc;
   }

   @Override
   public long getDeviceAddress() { return rawdata[0] & 0xFFL; }

   @Override
   public PDU getPDU() {
      byte[] pdu = new byte[rawdata.length - 3];
      System.arraycopy(rawdata, 1, pdu, 0, pdu.length);
      return new PDU(pdu);
   }
}
