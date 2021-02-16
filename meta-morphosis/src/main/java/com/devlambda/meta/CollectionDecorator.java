package com.devlambda.meta;


import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * @param <E> the type of elements in this collection.
 */
public class CollectionDecorator<E> extends IterableDecorator<E> implements Collection<E> {
   
   protected final Collection<E> decorated;
   
   public CollectionDecorator(Collection<E> decorated) {
      super(decorated);
      
      this.decorated = decorated;
   }

   @Override
   public int size() {
      return decorated.size();
   }

   @Override
   public boolean isEmpty() {
      return decorated.isEmpty();
   }

   @Override
   public boolean contains(Object object) {
      return decorated.contains(object);
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      return decorated.containsAll(collection);
   }
   
   @Override
   public Stream<E> parallelStream() { 
      return decorated.parallelStream();
   }
   
   @Override
   public Stream<E> stream() { 
      return decorated.stream();
   }

   @Override
   public boolean add(E element) {
      return decorated.add(element);
   }

   @Override
   public boolean addAll(Collection<? extends E> collection) {
      return decorated.addAll(collection);
   }

   @Override
   public void clear() { 
      decorated.clear();
   }

   @Override
   public boolean remove(Object object) {
      return decorated.remove(object);
   }
   
   @Override
   public boolean removeIf(Predicate<? super E> filter) {
      return decorated.removeIf(filter);
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      return decorated.retainAll(collection);
   }

   @Override
   public boolean removeAll(Collection<?> collection) {
      return decorated.removeAll(collection);
   }

   @Override
   public Object[] toArray() {
      return decorated.toArray();
   }

   @Override
   public <T> T[] toArray(T[] array) {
      return decorated.toArray(array);
   }
}
