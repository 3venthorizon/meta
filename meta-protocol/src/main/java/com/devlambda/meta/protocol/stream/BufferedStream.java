package com.devlambda.meta.protocol.stream;


import java.io.IOException;

import com.devlambda.meta.protocol.Packet;
import com.devlambda.meta.protocol.ProtocolException;
import com.devlambda.meta.protocol.Stream;


public class BufferedStream extends Stream<Packet> {
   
   protected final byte[] buffer;
   
   public BufferedStream(int bufferSize) {
      buffer = new byte[bufferSize];
   }

   @Override
   public Packet read() throws IOException, ProtocolException {
      int bytes = read(buffer, 0);
      byte[] rawdata = new byte[bytes];
      System.arraycopy(buffer, 0, rawdata, 0, bytes);
      
      return new Packet(rawdata);
   }
}
