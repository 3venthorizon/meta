package com.devlambda.meta;


import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;


/**
 * @param <E> the type of elements in this collection.
 * 
 * @author Dewald Pretorius
 */
public class IterableDecorator<E> implements Iterable<E> {
   
   protected final Iterable<E> decorated;

   public IterableDecorator(Iterable<E> decorated) {
      this.decorated = decorated;
   }

   @Override
   public Iterator<E> iterator() {
      return decorated.iterator();
   }
   
   @Override
   public void forEach(Consumer<? super E> action) {
      decorated.forEach(action);
   }
   
   @Override
   public Spliterator<E> spliterator() {
      return decorated.spliterator();
   }
}
