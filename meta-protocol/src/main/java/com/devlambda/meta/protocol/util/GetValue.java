package com.devlambda.meta.protocol.util;


/**
 * @author Dewald Pretorius
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
