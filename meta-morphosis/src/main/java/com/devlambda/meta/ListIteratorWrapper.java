package com.devlambda.meta;


import java.util.ListIterator;


/**
 * A value wrapper for meta {@link Type} instances to iterate over their values.
 * 
 * @param <M> meta type
 * 
 * @author Dewald Pretorius
 */
public class ListIteratorWrapper<M> implements ListIterator<Object> {
   
   protected final Type<M> type;
   protected final ListIterator<Property<M, Object>> iterator;
   protected M instance;
   
   protected Property<M, Object> property;

   public ListIteratorWrapper(Type<M> type, M instance) {
      this(type, instance, 0);
   }
   
   public ListIteratorWrapper(Type<M> type, M instance, int index) {
      this.instance = instance;
      this.type = type;

      iterator = type.properties.listIterator(index);
   }

   @Override
   public boolean hasNext() { 
      return iterator.hasNext();
   }

   @Override
   public Object next() {
      property = iterator.next();
      return property.get.apply(instance);
   }

   @Override
   public boolean hasPrevious() {
      return iterator.hasPrevious();
   }

   @Override
   public Object previous() {
      property = iterator.previous();
      return property.get.apply(instance);
   }

   @Override
   public int nextIndex() {
      return iterator.nextIndex();
   }

   @Override
   public int previousIndex() {
      return iterator.previousIndex();
   }

   /**
    * Sets the internal meta {@link #instance} property value to null. This has a no operation effect if the property's
    * type is primitive.
    */
   @Override
   public void remove() {
      set(null);
   }

   /**
    * Sets the internal meta {@link #instance} property value to the element.
    */
   @Override
   public void set(Object element) { 
      property.setSafe(instance, element);
   }

   /**
    * Delegates to {@link #set(Object)}.
    */
   @Override
   public void add(Object element) { 
      set(element);
   }
   
   public Type<M> getType() { return type; }

   public M getInstance() { return instance; }
   public void setInstance(M instance) { this.instance = instance; }
}
