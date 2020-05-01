package com.devlambda.meta.protocol;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.devlambda.eventhorizon.Observer;


/**
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolTest {

   Protocol<Packet> protocol;
   @Mock Connection connection;
   @Mock Stream<Packet> stream;
   @Mock InputStream inputStream;
   @Mock OutputStream outputStream;

   @Before
   public void before() {
      protocol = spy(new Protocol<>(connection, stream));
   }

   @Test
   public void testConstructor() {
      assertEquals(connection, protocol.connection);
      assertEquals(stream, protocol.stream);
      assertNotNull(protocol.readEvent);
      assertNotNull(protocol.sendEvent);
      assertNotNull(protocol.connectionEvent);
      assertNotNull(protocol.errorEvent);
      assertNull(protocol.runner);
      assertFalse(protocol.running);
      assertFalse(protocol.reading);
      assertEquals(-1L, protocol.timeout);
   }

   @Test
   public void testSetEventExecutor() {
      Executor executor = mock(Executor.class);

      protocol.setEventExecutor(executor);

      assertEquals(executor, protocol.readEvent.getExecutor());
      assertEquals(executor, protocol.sendEvent.getExecutor());
      assertEquals(executor, protocol.connectionEvent.getExecutor());
      assertEquals(executor, protocol.errorEvent.getExecutor());
      verify(protocol).setEventExecutor(executor);
      verifyNoMoreInteractions(protocol);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testConnect() throws IOException {
      Observer<Protocol<Packet>, Connection> observer = mock(Observer.class);
      protocol.connectionEvent.add(observer);

      when(connection.isConnected()).thenReturn(false);
      when(connection.getInputStream()).thenReturn(inputStream);
      when(connection.getOutputStream()).thenReturn(outputStream);

      protocol.connect();

      InOrder inOrder = inOrder(connection, observer, stream);
      inOrder.verify(connection).isConnected();
      inOrder.verify(connection).open();
      inOrder.verify(observer).onEvent(protocol, connection);
      inOrder.verify(connection).getInputStream();
      inOrder.verify(connection).getOutputStream();
      inOrder.verify(stream).initialize(inputStream, outputStream);
      verify(protocol).connect();
      verifyNoMoreInteractions(protocol, connection, stream);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testConnected() throws IOException {
      Observer<Protocol<Packet>, Connection> observer = mock(Observer.class);
      protocol.connectionEvent.add(observer);

      when(connection.isConnected()).thenReturn(true);
      when(connection.getInputStream()).thenReturn(inputStream);
      when(connection.getOutputStream()).thenReturn(outputStream);

      protocol.connect();

      verify(protocol).connect();
      verify(connection).isConnected();
      verify(connection).getInputStream();
      verify(connection).getOutputStream();
      verify(stream).initialize(inputStream, outputStream);
      verifyNoMoreInteractions(protocol, connection, stream);
      verifyZeroInteractions(observer);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testConnectError() throws IOException {
      IOException ioe = new IOException("Test Connection Error");
      Observer<Protocol<Packet>, Connection> observer = mock(Observer.class);
      protocol.connectionEvent.add(observer);

      when(connection.isConnected()).thenReturn(false);
      doThrow(ioe).when(connection).open();

      try {
         protocol.connect();
         fail("IOException expected");
      } catch (IOException error) {
         assertEquals(ioe, error);
      }

      verify(connection).open();
      verifyZeroInteractions(observer);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testSend() throws IOException, ProtocolException {
      Packet packet = mock(Packet.class);
      Observer<Protocol<Packet>, Packet> observer = mock(Observer.class);
      protocol.sendEvent.add(observer);

      try {
         protocol.send(packet);
      } catch (ProtocolException pe) {
         fail(pe.getMessage());
      }

      InOrder inOrder = inOrder(stream, observer);

      inOrder.verify(stream).write(packet);
      inOrder.verify(observer).onEvent(protocol, packet);
      verify(protocol).send(packet);
      verifyNoMoreInteractions(protocol);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testSendIOException() throws ProtocolException, IOException {
      Packet packet = mock(Packet.class);
      Observer<Protocol<Packet>, Packet> sendObserver = mock(Observer.class);
      Observer<Protocol<Packet>, ProtocolException> errorObserver = mock(Observer.class);
      IOException ioe = new IOException("Wrap this IO Error in a ProtocolException");
      protocol.reading = true;
      protocol.sendEvent.add(sendObserver);
      protocol.errorEvent.add(errorObserver);

      doThrow(ioe).when(stream).write(packet);

      try {
         protocol.send(packet);

         fail();
      } catch (ProtocolException protocolException) {
         assertEquals(ioe, protocolException.getCause());
         verify(errorObserver).onEvent(protocol, protocolException);
      }

      assertFalse(protocol.reading);
      verifyZeroInteractions(sendObserver);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testSendProtocolException() throws ProtocolException, IOException {
      Packet packet = mock(Packet.class);
      Observer<Protocol<Packet>, Packet> sendObserver = mock(Observer.class);
      Observer<Protocol<Packet>, ProtocolException> errorObserver = mock(Observer.class);
      ProtocolException pe = new ProtocolException("Test ProtocolException Read");
      protocol.reading = true;
      protocol.sendEvent.add(sendObserver);
      protocol.errorEvent.add(errorObserver);

      doThrow(pe).when(stream).write(packet);

      try {
         protocol.send(packet);

         fail();
      } catch (ProtocolException protocolException) {
         verify(errorObserver).onEvent(protocol, protocolException);
      }

      assertTrue(protocol.reading);
      verifyZeroInteractions(sendObserver);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testRead() throws IOException, ProtocolException {
      Packet firstPacket = mock(Packet.class);
      Packet lastPacket = mock(Packet.class);
      Observer<Protocol<Packet>, Packet> readObserver = mock(Observer.class);
      Observer<Protocol<Packet>, ProtocolException> errorObserver = mock(Observer.class);
      ProtocolException pe = new ProtocolException("Test ProtocolException Read");
      IOException ioe = new IOException("Force break reading loop");
      protocol.reading = true;
      protocol.readEvent.add(readObserver);
      protocol.errorEvent.add(errorObserver);

      when(stream.read()).thenReturn(firstPacket).thenThrow(pe) // does not break read loop
            .thenReturn(lastPacket).thenThrow(ioe) // breaks read loop
            .then(invoke -> {
               protocol.reading = false; // break read loop
               return firstPacket;
            });

      try {
         protocol.read();
      } catch (IOException ioError) {
         assertEquals(ioe, ioError);
         assertTrue(protocol.reading);

         protocol.read(); // retry reading unit protocol.reading is false

         assertFalse(protocol.reading);
      }

      InOrder inOrder = inOrder(stream, readObserver, errorObserver);

      inOrder.verify(stream).read();
      inOrder.verify(readObserver).onEvent(protocol, firstPacket);
      inOrder.verify(stream).read();
      inOrder.verify(errorObserver).onEvent(protocol, pe);
      inOrder.verify(stream).read();
      inOrder.verify(readObserver).onEvent(protocol, lastPacket);
      inOrder.verify(stream, times(2)).read(); // retry after ioError
      inOrder.verify(readObserver).onEvent(protocol, firstPacket);
   }

   @Test
   public void testRun() throws IOException {
      Answer<?> isRunningAndReading = 
         invoke -> { assertTrue(protocol.running); assertTrue(protocol.reading); return null; };
      doAnswer(isRunningAndReading).when(protocol).connect();
      doAnswer(isRunningAndReading).when(protocol).read();
      doAnswer(isRunningAndReading).when(protocol).disconnect();

      protocol.run();

      assertFalse(protocol.running);
      assertFalse(protocol.reading);
      assertEquals(Thread.currentThread(), protocol.runner);

      InOrder inOrder = inOrder(protocol);

      inOrder.verify(protocol).connect();
      inOrder.verify(protocol).read();
      inOrder.verify(protocol).disconnect();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testRunConnectionError() throws IOException {
      IOException ioe = new IOException("Test Connection Error");
      ArgumentCaptor<ProtocolException> errorCaptor = ArgumentCaptor.forClass(ProtocolException.class);
      Observer<Protocol<Packet>, ProtocolException> errorObserver = mock(Observer.class);
      protocol.errorEvent.add(errorObserver);

      doThrow(ioe).when(protocol).connect();
      doNothing().when(protocol).disconnect();
      doNothing().when(errorObserver).onEvent(eq(protocol), errorCaptor.capture());

      protocol.run();

      verify(errorObserver).onEvent(eq(protocol), any(ProtocolException.class));
      ProtocolException result = errorCaptor.getValue();
      assertNotNull(result);
      assertEquals(ioe, result.getCause());

      verify(protocol, never()).read();
      verify(protocol).disconnect();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testRunReadingError() throws IOException {
      IOException ioe = new IOException("Test Reading Error");
      ArgumentCaptor<ProtocolException> errorCaptor = ArgumentCaptor.forClass(ProtocolException.class);
      Observer<Protocol<Packet>, ProtocolException> errorObserver = mock(Observer.class);
      protocol.errorEvent.add(errorObserver);

      doNothing().when(protocol).connect();
      doThrow(ioe).when(protocol).read();
      doNothing().when(protocol).disconnect();
      doNothing().when(errorObserver).onEvent(eq(protocol), errorCaptor.capture());

      protocol.run();

      verify(errorObserver).onEvent(eq(protocol), any(ProtocolException.class));
      ProtocolException result = errorCaptor.getValue();
      assertNotNull(result);
      assertEquals(ioe, result.getCause());

      verify(protocol).disconnect();
   }

   @Test
   public void testIsRunning() throws IOException {
      assertFalse(protocol.isRunning());

      Answer<?> isRunning = invoke -> { assertTrue(protocol.isRunning()); return null; };
      doAnswer(isRunning).when(protocol).connect();
      doAnswer(isRunning).when(protocol).read();
      doAnswer(isRunning).when(protocol).disconnect();

      protocol.run();

      assertFalse(protocol.running);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testDisconnected() throws IOException {
      Observer<Protocol<Packet>, Connection> observer = mock(Observer.class);
      protocol.connectionEvent.add(observer);

      when(connection.isConnected()).thenReturn(false);

      protocol.disconnect();

      verify(protocol).disconnect();
      verify(connection).isConnected();
      verifyNoMoreInteractions(protocol, connection);
      verifyZeroInteractions(observer);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testDisconnect() throws IOException {
      Observer<Protocol<Packet>, Connection> observer = mock(Observer.class);
      protocol.connectionEvent.add(observer);

      when(connection.isConnected()).thenReturn(true);
      doNothing().when(connection).close();

      protocol.disconnect();

      InOrder inOrder = inOrder(connection, observer);

      inOrder.verify(connection).isConnected();
      inOrder.verify(connection).close();
      inOrder.verify(observer).onEvent(protocol, connection);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testDisconnectError() throws IOException {
      IOException ioe = new IOException("Test Disconnect Error");
      ArgumentCaptor<ProtocolException> errorCaptor = ArgumentCaptor.forClass(ProtocolException.class);
      Observer<Protocol<Packet>, Connection> connectionObserver = mock(Observer.class);
      Observer<Protocol<Packet>, ProtocolException> errorObserver = mock(Observer.class);
      protocol.errorEvent.add(errorObserver);
      protocol.connectionEvent.add(connectionObserver);

      when(connection.isConnected()).thenReturn(true);
      doThrow(ioe).when(connection).close();
      doNothing().when(errorObserver).onEvent(eq(protocol), errorCaptor.capture());

      protocol.disconnect();

      verify(errorObserver).onEvent(eq(protocol), any(ProtocolException.class));
      ProtocolException result = errorCaptor.getValue();
      assertNotNull(result);
      assertEquals(ioe, result.getCause());
      verifyZeroInteractions(connectionObserver);
   }

   @Test
   public void testStopRunningWaitTimeout() throws InterruptedException {
      protocol.runner = new Thread(new Runnable() {
         @Override
         public synchronized void run() {

            try {
               wait();
            } catch (InterruptedException e) {
               assertFalse(protocol.reading);
            }

         }
      });

      protocol.running = true;
      protocol.reading = true;
      protocol.timeout = 10L;

      protocol.runner.start();
      protocol.stop();

      assertFalse(protocol.running);

      protocol.stop(); // idempotent
   }

   @Test
   public void testStopNeverStarted() throws InterruptedException {
      // do nothing when runner thread is null
      protocol.stop();

      // do nothing when thread is dying
      protocol.running = false;
      protocol.runner = new Thread(new Runnable() {
         @Override
         public synchronized void run() {

            try {
               wait();
            } catch (InterruptedException e) {
            }

         }
      });
      protocol.runner.start();
      protocol.stop();
      protocol.runner.interrupt();

      protocol.stop(); // idempotent
   }

   @Test
   public void testGetTimeout() {
      protocol.timeout = 1000L;

      assertEquals(1000L, protocol.getTimeout());
   }

   @Test
   public void testSetTimeout() {
      protocol.setTimeout(1000L);

      assertEquals(1000L, protocol.timeout);
   }

}
