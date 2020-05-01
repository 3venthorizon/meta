package com.devlambda.eventhorizon;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.Executor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EventTest {

   @Spy Event<EventTest, String> event;

   @Test
   @SuppressWarnings("unchecked")
   public void testBurnError() {
      Observer<EventTest, String> observer = mock(Observer.class);
      String data = "Test Data";
      Exception error = spy(new Exception());

      event.burnError(observer, this, data, error);

      verify(error).printStackTrace();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testAsyncNotifyRunner() {
      Observer<EventTest, String> observer = mock(Observer.class);
      String data = "Test Data";

      Runnable result = event.asyncNotifyRunner(observer, this, data);

      verifyZeroInteractions(observer);
      verify(event).asyncNotifyRunner(observer, this, data);
      verify(event, never()).notify(observer, this, data);
      verifyNoMoreInteractions(event);
      reset(event, observer);

      result.run();

      verify(observer).onEvent(this, data);
      verify(event).notify(observer, this, data);
      verifyNoMoreInteractions(event);
   }

   @Test
   public void testFireEventExecutor() {
      String data = "Test Data";
      event.executor = mock(Executor.class);
      ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

      doNothing().when(event.executor).execute(runnableCaptor.capture());

      event.fireEvent(this, data);

      verify(event).fireEvent(this, data);
      verify(event.executor).execute(any(Runnable.class));
      verifyNoMoreInteractions(event, event.executor);

      Runnable result = runnableCaptor.getValue();
      assertNotNull(result);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testFireEventRunnables() {
      String data = "Test Data";
      event.executor = mock(Executor.class);
      Observer<EventTest, String> observer = mock(Observer.class);
      ArgumentCaptor<Runnable> asyncCaptor = ArgumentCaptor.forClass(Runnable.class);
      event.add(observer);
      event.add(observer);
      event.add(observer);

      doNothing().when(event.executor).execute(asyncCaptor.capture());

      event.fireEvent(this, data);

      verify(event).fireEvent(this, data);
      verify(event, times(3)).add(observer);
      verify(event.executor).execute(any(Runnable.class));
      verifyNoMoreInteractions(event, event.executor);
      verifyZeroInteractions(observer);

      Runnable result = asyncCaptor.getValue();
      assertNotNull(result);

      reset(event, event.executor, observer);
      ArgumentCaptor<Runnable> notifierCaptor = ArgumentCaptor.forClass(Runnable.class);

      doNothing().when(event.executor).execute(notifierCaptor.capture());

      result.run();

      verify(event, times(3)).asyncNotifyRunner(observer, this, data);
      verify(event.executor, times(3)).execute(any(Runnable.class));
      verify(event, never()).notify(any(Observer.class), any(EventTest.class), anyString());
      verifyNoMoreInteractions(event.executor);
      verifyZeroInteractions(observer);

      List<Runnable> notifiers = notifierCaptor.getAllValues();

      assertEquals(3, notifiers.size());

      reset(event, event.executor, observer);

      for (Runnable notifier : notifiers) {
         notifier.run();
      }

      verify(event, times(3)).notify(observer, this, data);
      verify(observer, times(3)).onEvent(this, data);
      verifyNoMoreInteractions(event, observer);
      verifyZeroInteractions(event.executor);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testFireEvent() {
      Observer<EventTest, String> observer = mock(Observer.class);
      String data = "Test Data";

      event.add(observer);
      event.add(observer);
      event.add(observer);

      event.fireEvent(this, data);

      verify(event, times(3)).asyncNotifyRunner(observer, this, data);
      verify(event, times(3)).notify(observer, this, data);
      verify(observer, times(3)).onEvent(this, data);
   }

   @Test
   public void testGetExecutor() {
      Executor executor = event.getExecutor();
      assertNotNull(executor);

      event.executor = mock(Executor.class);
      assertNotEquals(executor, event.getExecutor());
      assertEquals(event.executor, event.getExecutor());
   }

   @Test
   public void testSetExecutor() {
      Executor executor = mock(Executor.class);
      assertNotEquals(executor, event.executor);

      event.setExecutor(executor);

      assertEquals(executor, event.executor);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testObserverException() {
      String data = "Test Data";
      Observer<EventTest, String> observer = mock(Observer.class);
      Exception error = new RuntimeException();

      doThrow(error).when(observer).onEvent(this, data);
      doNothing().when(event).burnError(observer, this, data, error);

      event.notify(observer, this, data);

      verify(event).burnError(observer, this, data, error);
   }
}
