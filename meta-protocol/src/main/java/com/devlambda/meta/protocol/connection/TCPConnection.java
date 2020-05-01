package com.devlambda.meta.protocol.connection;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import com.devlambda.meta.protocol.Connection;
import com.devlambda.meta.protocol.ProtocolException;


/**
 * @author Dewald Pretorius
 */
public class TCPConnection implements Connection {

   private Socket socket;
   private SocketAddress endpoint;
   private int timeout;

   public TCPConnection(Socket socket, SocketAddress endpoint, int timeout) {
      this.socket = socket;
      this.endpoint = endpoint;
      this.timeout = timeout;
   }

   public TCPConnection(Socket socket) {
      this(socket, null, -1);
   }

   @Override
   public void close() throws IOException {
      socket.close();
   }

   @Override
   public void open() throws IOException {
      if (isConnected()) return;

      if (timeout > 0) {
         socket.connect(endpoint, timeout);
      } else {
         socket.connect(endpoint);
      }

   }

   @Override
   public boolean isConnected() { return socket.isConnected() && socket.isClosed(); }

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
