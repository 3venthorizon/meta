package com.devlambda.meta;


import java.util.Collection;
import java.util.List;
import java.util.ListIterator;


/**
 * @author Dewald Pretorius
 */
public class ListDecorator<E> extends CollectionDecorator<E> implements List<E> {
   
   protected final List<E> decorated;

   public ListDecorator(List<E> decorated) { 
      super(decorated);
      
      this.decorated = decorated;
   }

   @Override
   public boolean addAll(int index, Collection<? extends E> collection) {
      return decorated.addAll(index, collection);
   }

   @Override
   public E get(int index) {
      return decorated.get(index);
   }

   @Override
   public E set(int index, E element) {
      return decorated.set(index, element);
   }

   @Override
   public void add(int index, E element) {
      decorated.add(index, element);
   }

   @Override
   public E remove(int index) {
      return decorated.remove(index);
   }

   @Override
   public int indexOf(Object object) {
      return decorated.indexOf(object);
   }

   @Override
   public int lastIndexOf(Object object) {
      return decorated.lastIndexOf(object);
   }

   @Override
   public ListIterator<E> listIterator() {
      return decorated.listIterator();
   }

   @Override
   public ListIterator<E> listIterator(int index) {
      return decorated.listIterator(index);
   }

   @Override
   public List<E> subList(int fromIndex, int toIndex) {
      return decorated.subList(fromIndex, toIndex);
   }
}
