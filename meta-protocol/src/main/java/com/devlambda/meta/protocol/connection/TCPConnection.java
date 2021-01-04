package com.devlambda.meta.protocol.connection;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.function.Supplier;

import com.devlambda.meta.protocol.Connection;
import com.devlambda.meta.protocol.ProtocolException;


/**
 * @author Dewald Pretorius
 */
public class TCPConnection implements Connection {

   private Socket socket;
   private Supplier<Socket> supplier;
   private SocketAddress endpoint;
   private int timeout;
   
   public TCPConnection(Supplier<Socket> supplier, SocketAddress endpoint, int timeout) {
      this.supplier = supplier;
      this.endpoint = endpoint;
      this.timeout = timeout;
   }

   @Override
   public void close() throws IOException {
      try {
         socket.close();
      } finally {
         socket = null;
      }
   }

   @Override
   public void open() throws IOException {
      if (isConnected()) return;

      try {
         socket = supplier.get();

         if (timeout > 0) {
            socket.connect(endpoint, timeout);
         } else {
            socket.connect(endpoint);
         }
      } catch (IOException ioe) {
         socket = null;
         throw ioe;
      }
   }

   @Override
   public boolean isConnected() { 
      return socket != null && socket.isConnected() && !socket.isClosed(); 
   }

   @Override
   public InputStream getInputStream() {
      try {
         return socket.getInputStream();
      } catch (IOException ioe) {
         throw new ProtocolException(ioe);
      }
   }

   @Override
   public OutputStream getOutputStream() {
      try {
         return socket.getOutputStream();
      } catch (IOException ioe) {
         throw new ProtocolException(ioe);
      }
   }

   public Socket getSocket() { return socket; }
   public void setSocket(Socket socket) { this.socket = socket; }

   public SocketAddress getEndpoint() { return endpoint; }
   public void setEndpoint(SocketAddress endpoint) { this.endpoint = endpoint; }

   public int getTimeout() { return timeout; }
   public void setTimeout(int timeout) { this.timeout = timeout; }
}
