package com.devlambda.meta.protocol;


/**
 * @author Dewald Pretorius
 */
public class ProtocolException extends RuntimeException {
   static final long serialVersionUID = -6127823216122291686L;

   public final long timestamp;

   public ProtocolException(Throwable cause) {
      super(cause);
      timestamp = System.currentTimeMillis();
   }

   public ProtocolException(String message) {
      super(message);
      timestamp = System.currentTimeMillis();
   }

   public ProtocolException(String message, Throwable cause) {
      super(message, cause);
      timestamp = System.currentTimeMillis();
   }
}
