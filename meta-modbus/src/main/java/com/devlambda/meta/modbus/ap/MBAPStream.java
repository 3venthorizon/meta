package com.devlambda.meta.modbus.ap;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.devlambda.meta.protocol.ProtocolException;
import com.devlambda.meta.protocol.Stream;
import com.devlambda.meta.protocol.util.ProtoUtils;


/**
 * MBAPStream
 *
 * @author Dewald Pretorius
 */
public class MBAPStream extends Stream<MBAP> {

   protected static final int BUFFER_LENGTH = 260; // maximum MBAP packet length

   @Override
   public void initialize(InputStream in, OutputStream out) {
      if (!(in instanceof BufferedInputStream)) in = new BufferedInputStream(in, BUFFER_LENGTH);
      super.initialize(in, out);
   }

   @Override
   public MBAP read() throws IOException, ProtocolException {
      in.mark(BUFFER_LENGTH);
      byte[] buffer = new byte[BUFFER_LENGTH];
      int offset = 0;

      while (offset < 6) {
         offset = read(buffer, offset);
      }

      int remainder = ProtoUtils.BE(buffer).offset(4).getInt(Short.BYTES);
      int length = remainder + 6;

      if (offset > length) {
         in.reset();
         in.skip(length);
      }

      return new MBAP(readRawData(buffer, offset, length));
   }
}
