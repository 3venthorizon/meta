package com.devlambda.meta.protocol;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @param <Packet>
 * @author Dewald Pretorius
 */
@SuppressWarnings("hiding")
public abstract class Stream<Packet extends com.devlambda.meta.protocol.Packet> {

   protected InputStream in;
   protected OutputStream out;

   public void initialize(InputStream in, OutputStream out) {
      this.in = in;
      this.out = out;
   }

   /**
    * Writes a single packet to the output stream.
    * 
    * @param out
    * @param packet
    * @throws IOException
    * @throws ProtocolException
    */
   public void write(Packet packet) throws IOException, ProtocolException {
      if (packet == null) throw new ProtocolException(new NullPointerException());

      synchronized (out) {
         out.write(packet.getRawdata());
         out.flush();
      } // object lock

   }

   /**
    * Reads a single packet from the input stream.
    * 
    * @param in
    * @return packet
    * @throws IOException
    * @throws ProtocolException
    */
   public abstract Packet read() throws IOException, ProtocolException;

   /**
    * Reads the input stream into the supplied buffer from the offset.
    * 
    * @param in
    * @param buffer
    * @param offset
    * @return offset adjusted
    * @throws IOException
    */
   protected int read(byte[] buffer, int offset) throws IOException {
      int read = in.read(buffer, offset, buffer.length - offset);
      if (read < 0) throw new IOException("End of Stream");
      return offset + read;
   }

   /**
    * <p>
    * Reads the input stream from the offset until the offset is greater or equal to the length, or the end of the
    * stream is reached.
    * </p>
    * <p>
    * The buffer is copied into the rawdata byte-array return value up to the offset. Further data read from the input
    * stream will be populated from the offset to the length of the rawdata.
    * </p>
    * 
    * @param in
    * @param buffer
    * @param offset
    * @param length
    * @return rawdata of length bytes
    * @throws IOException
    */
   protected byte[] readRawData(byte[] buffer, int offset, int length) throws IOException {
      byte[] rawdata = new byte[length];
      System.arraycopy(buffer, 0, rawdata, 0, offset < length ? offset : length);

      while (offset < length) {
         offset = read(rawdata, offset);
      }

      return rawdata;
   }
}
