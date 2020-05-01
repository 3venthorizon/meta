package com.devlambda.eventhorizon;


/**
 * @author Dewald Pretorius
 */
@FunctionalInterface
public interface Observer<S, D> {

   void onEvent(S source, D data);
}
