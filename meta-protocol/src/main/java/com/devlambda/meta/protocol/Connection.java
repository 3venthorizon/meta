package com.devlambda.meta.protocol;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author Dewald Pretorius
 */
public interface Connection {

   /**
    * Closes the connection object. No need to close the IO Streams.
    * 
    * @throws IOException
    */
   void close() throws IOException;

   /**
    * Creates or recreates a new opened connection.
    * 
    * @throws IOException
    * @throws Exception
    */
   void open() throws IOException;

   /**
    * @return connected state.
    */
   boolean isConnected();

   /**
    * @return input stream
    */
   InputStream getInputStream();

   /**
    * @return output stream
    */
   OutputStream getOutputStream();
}
