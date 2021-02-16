package com.devlambda.meta.protocol.util;


/**
 * Byte array value Getter.
 */
public interface GetValue {

   /**
    * 
    * @param data
    * @param offset
    * @param bytes
    * @return value
    */
   long function(byte[] data, int offset, int bytes);
}
