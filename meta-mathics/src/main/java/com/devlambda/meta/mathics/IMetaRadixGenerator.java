package com.devlambda.meta.mathics;


import java.util.List;
import java.util.ListIterator;


/**
 * Interface used to generate mathematical groups for a domain collection.
 */
public interface IMetaRadixGenerator<T> extends Iterable<List<T>>, ListIterator<List<T>> {

   @Override
   default void add(List<T> e) {
      throw new UnsupportedOperationException("add");
   }
   
   @Override
   default void remove() {
      throw new UnsupportedOperationException("remove");
   }
   
   @Override
   default void set(List<T> e) {
      throw new UnsupportedOperationException("set");
   }

   void advanceIndex();
   
   void reduceIndex();
   
   List<T> current();

   List<T> remainder();
   
   /**
    * Returns the finite amount of unique group lists that can be generated.
    * 
    * @return total
    */
   long total();
}
