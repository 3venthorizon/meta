package com.devlambda.meta.protocol;


import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * @author Dewald Pretorius
 */
@SuppressWarnings("hiding")
public class Master<Packet extends com.devlambda.meta.protocol.Packet> {

   protected Protocol<Packet> protocol;
   protected Semaphore semaphore;
   protected Packet response;
   protected ProtocolException error;

   public Master(Protocol<Packet> protocol) {
      this.protocol = protocol;
      semaphore = new Semaphore(1, true);
      
      protocol.readEvent.add((master, response) -> slaveResponse(response));
      protocol.errorEvent.add((master, error) -> slaveError(error));
   }

   public Master(Connection connection, Stream<Packet> stream) {
      this(new Protocol<>(connection, stream));
   }

   /**
    * @param request
    * @return response
    */
   public Packet request(Packet request, long timeout) throws ProtocolException {
      try {
         if (!semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
            throw new ProtocolException("Request Timedout " + timeout + "ms");
         }
      } catch (InterruptedException ie) {
         throw new ProtocolException("Request Interrupted", ie);
      }

      try {
         return requestResponse(request, timeout);
      } finally {
         semaphore.release();
      }
   }

   public Protocol<Packet> getProtocol() { return protocol; }
   public void setProtocol(Protocol<Packet> protocol) { this.protocol = protocol; }

   protected synchronized Packet requestResponse(Packet request, long timeout) throws ProtocolException {
      response = null;
      error = null;
      protocol.send(request);

      try {
         wait(timeout);

         if (error != null) throw error;
         if (response == null) throw new ProtocolException("Response Timedout " + timeout + "ms");
         return response;
      } catch (InterruptedException ie) {
         throw new ProtocolException("Awaiting Response interrupted", ie);
      }
   }

   protected synchronized void slaveResponse(Packet response) {
      if (semaphore.availablePermits() > 0) return;

      this.response = response;
      notifyAll();
   }

   protected synchronized void slaveError(ProtocolException error) {
      if (semaphore.availablePermits() > 0) return;

      this.error = error;
      notifyAll();
   }
}
