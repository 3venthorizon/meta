package com.devlambda.eventhorizon;


import java.util.ArrayList;
import java.util.concurrent.Executor;


/**
 * @author Dewald Pretorius
 */
public class Event<S, D> extends ArrayList<Observer<S, D>> {

   static final long serialVersionUID = -7159508185823401652L;

   protected Executor executor = runnable -> runnable.run();

   public Event() { }

   public void fireEvent(S source, D data) {
      Runnable async = () -> parallelStream().map(observer -> asyncNotifyRunner(observer, source, data))
            .forEach(notifier -> executor.execute(notifier));
      executor.execute(async);
   }

   protected Runnable asyncNotifyRunner(Observer<S, D> observer, S source, D data) {
      return () -> notify(observer, source, data);
   }

   protected void notify(Observer<S, D> observer, S source, D data) {
      try {
         observer.onEvent(source, data);
      } catch (Exception error) {
         burnError(observer, source, data, error);
      }
   }

   protected synchronized void burnError(Observer<S, D> observer, S source, D data, Exception error) {
      System.err.println("FATAL ERROR: The Observer raised an Exception during error event processing.");
      System.err.print("Observer's");
      if (observer != null) System.err.print(" Class : " + observer.getClass().getName());
      System.err.println(" - instance object: {" + observer + "}");
      System.err.print("Event's Source");
      if (source != null) System.err.print(" Class : " + source.getClass().getName());
      System.err.println(" - instance object: {" + source + "}");
      System.err.print("Event's Data");
      if (data != null) System.err.print(" Class : " + data.getClass().getName());
      System.err.println(" - instance object: {" + data + "}");

      error.printStackTrace();
   }

   public Executor getExecutor() { return executor; }
   public void setExecutor(Executor executor) { this.executor = executor; }
}
