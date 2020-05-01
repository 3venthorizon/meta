package com.devlambda.meta.protocol;


import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.devlambda.eventhorizon.Observer;


/**
 * @author Dewald Pretorius
 */
@SuppressWarnings("hiding")
public class Master<Packet extends com.devlambda.meta.protocol.Packet> extends Protocol<Packet> {

   protected Semaphore semaphore;
   protected Packet response;
   protected ProtocolException error;

   public Master(Protocol<Packet> protocol) {
      this(protocol.connection, protocol.stream);
   }

   public Master(Connection connection, Stream<Packet> stream) {
      super(connection, stream);

      semaphore = new Semaphore(1, true);
      Observer<Protocol<Packet>, Packet> slaveResponse = (master, response) -> slaveResponse(response);
      Observer<Protocol<Packet>, ProtocolException> slaveError = (master, error) -> slaveError(error);

      readEvent.add(slaveResponse);
      errorEvent.add(slaveError);
   }

   /**
    * 
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

   protected synchronized Packet requestResponse(Packet request, long timeout) throws ProtocolException {
      response = null;
      error = null;
      send(request);

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
