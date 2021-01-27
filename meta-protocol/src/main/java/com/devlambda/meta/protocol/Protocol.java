package com.devlambda.meta.protocol;


import java.io.IOException;
import java.util.concurrent.Executor;

import com.devlambda.eventhorizon.Event;


/**
 * @param <Packet>
 * @author Dewald Pretorius
 */
@SuppressWarnings("hiding")
public class Protocol<Packet extends com.devlambda.meta.protocol.Packet> implements Runnable {

   public final Event<Protocol<Packet>, Packet> readEvent;
   public final Event<Protocol<Packet>, Packet> sendEvent;
   public final Event<Protocol<Packet>, Connection> connectionEvent;
   public final Event<Protocol<Packet>, ProtocolException> errorEvent;

   protected Connection connection;
   protected Stream<Packet> stream;

   Thread runner;
   boolean running;
   boolean reading;
   long timeout;

   public Protocol(Connection connection, Stream<Packet> stream) {
      this.connection = connection;
      this.stream = stream;

      readEvent = new Event<>(Event.ASYNC);
      sendEvent = new Event<>(Event.ASYNC);
      errorEvent = new Event<>(Event.ASYNC);
      connectionEvent = new Event<>(Event.ASYNC);

      timeout = -1L;
      running = false;
      reading = false;
   }

   /**
    * Runs the protocol. This method is thread safe.
    */
   @Override
   public synchronized void run() {
      try {
         runner = Thread.currentThread();
         running = true;
         connect();
         read();
      } catch (IOException ioe) {
         errorEvent.fireEvent(this, new ProtocolException(ioe));
      } finally {
         running = false;
         disconnect();
      }
   }

   public void send(Packet packet) throws ProtocolException {
      try {
         stream.write(packet);
         sendEvent.fireEvent(this, packet);
      } catch (IOException ioe) {
         reading = false;
         ProtocolException pe = new ProtocolException(ioe);
         errorEvent.fireEvent(this, pe);

         throw pe;
      } catch (ProtocolException pe) {
         errorEvent.fireEvent(this, pe);
         throw pe;
      }
   }

   public void stop() throws InterruptedException {
      if (runner == null) return;
      if (!runner.isAlive()) return;
      if (!running) return;

      reading = false;

      if (timeout > 0L) runner.join(timeout);
      running = false;
      if (runner.isAlive()) runner.interrupt();
      if (runner.isAlive()) disconnect();
   }

   protected void connect() throws IOException {
      if (!connection.isConnected()) {
         connection.open();
         stream.initialize(connection.getInputStream(), connection.getOutputStream());
         connectionEvent.fireEvent(this, connection);
      } else {
         stream.initialize(connection.getInputStream(), connection.getOutputStream());
      }
   }

   protected void disconnect() {
      if (!connection.isConnected()) return;

      try {
         connection.close();
      } catch (IOException ioe) {
         errorEvent.fireEvent(this, new ProtocolException(ioe));
      } finally {
         connectionEvent.fireEvent(this, connection);
      }
   }

   protected void read() throws IOException {
      try {
         reading = true;
         
         while (reading) {
            try {
               Packet packet = stream.read();
               readEvent.fireEvent(this, packet);
            } catch (ProtocolException pe) {
               errorEvent.fireEvent(this, pe);
            }
         }
      } finally {
         reading = false;
      }
   }

   public void setEventExecutor(Executor executor) {
      readEvent.setExecutor(executor);
      sendEvent.setExecutor(executor);
      connectionEvent.setExecutor(executor);
      errorEvent.setExecutor(executor);
   }

   public boolean isRunning() { return running; }

   public long getTimeout() { return timeout; }
   public void setTimeout(long timeout) { this.timeout = timeout; }
}
