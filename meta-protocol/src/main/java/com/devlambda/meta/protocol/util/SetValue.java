package com.devlambda.meta.protocol.util;


/**
 * Byte array value Setter.
 */
public interface SetValue {

   /**
    * Set the value upon the data from the byte offset position.
    * 
    * @param data   buffer
    * @param offset within data
    * @param value  to be set upon data
    * @param bytes  length of value
    * @return offset adjusted
    */
   void function(byte[] data, int offset, long value, int bytes);
}
