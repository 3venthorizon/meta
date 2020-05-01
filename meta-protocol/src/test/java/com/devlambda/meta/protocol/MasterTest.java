package com.devlambda.meta.protocol;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class MasterTest {

   Master<Packet> master;
   @Mock Semaphore semaphore;
   @Mock Connection connection;
   @Mock Stream<Packet> stream;
   @Mock InputStream inputStream;
   @Mock OutputStream outputStream;

   @Before
   public void setUp() throws Exception {
      master = spy(new Master<>(connection, stream));
      master.semaphore = semaphore;
   }

   @Test
   public void semaphoreTryAcquireRelease() throws InterruptedException {
      long timeout = 3000;
      Packet request = mock(Packet.class);
      Packet response = mock(Packet.class);

      when(semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)).thenReturn(true);
      doReturn(response).when(master).requestResponse(request, timeout);

      Packet result = master.request(request, timeout);

      assertEquals(response, result);

      InOrder inOrder = inOrder(master, semaphore);
      inOrder.verify(master).request(request, timeout);
      inOrder.verify(semaphore).tryAcquire(timeout, TimeUnit.MILLISECONDS);
      inOrder.verify(master).requestResponse(request, timeout);
      inOrder.verify(semaphore).release();
      verifyNoMoreInteractions(master, semaphore);
   }
}
