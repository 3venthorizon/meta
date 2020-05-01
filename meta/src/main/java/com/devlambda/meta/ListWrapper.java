package com.devlambda.meta;


import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;


/**
 * Wraps the instance with a {@link List} interface. 
 * <p>
 * To access the instance field values the {@link List#get(int)} can be used.
 * Changes made via the {@link List#set(int, Object)} will invoke the underlying instance's setters.
 * </p>
 * 
 * @author Dewald Pretorius
 */
public class ListWrapper<M> implements List<Object> {
   
   protected final Type<M> type;
   protected M instance;

   public ListWrapper(Type<M> type, M instance) {
      this.type = type;
      this.instance = instance;
   }

   @Override
   public int size() { 
      return type.properties.size();
   }

   @Override
   public boolean isEmpty() { 
      return type.properties.isEmpty();
   }

   /**
    * Sets the first {@link ListWrapper#instance} field which is assignable from the parameter element's class if and
    * only if it will not cause a {@link ClassCastException}s.
    * 
    * @return false if the property could not be set else returns true
    */
   @Override
   public boolean add(Object element) {
      for (Property<M, Object> property : type.properties) {
         if (property.setSafe(instance, element)) return true;
      }
      
      return false;
   }

   /**
    * Delegates to {@link #set(int, Object)}.
    */
   @Override
   public void add(int index, Object element) { 
      set(index, element);
   }

   /**
    * Delegates to {@link #addAll(int, Collection)} with a start index of 0.
    */
   @Override
   public boolean addAll(Collection<? extends Object> collection) { 
      return addAll(0, collection);
   }

   @Override
   public boolean addAll(int index, Collection<? extends Object> collection) { 
      boolean changed = false;
      Iterator<? extends Object> citerator = collection.iterator();
      Iterator<Property<M, Object>> piterator = type.properties.listIterator(index);
      
      while (citerator.hasNext() && piterator.hasNext()) {
         Property<M, Object> property = piterator.next();
         Object element = citerator.next();
         changed |= property.setSafe(instance, element);
      }
      
      return changed;
   }

   @Override
   public void clear() { 
      type.properties.stream().forEach(property -> property.setSafe(instance, null));
   }

   @Override
   public boolean contains(Object object) {
      return type.properties.stream().map(property -> property.get.apply(instance))
                                     .anyMatch(element -> Objects.equals(element, object));
   }

   @Override
   public boolean containsAll(Collection<?> collection) { 
      return collection.stream().allMatch(this::contains);
   }

   @Override
   public Object get(int index) { 
      Property<M, Object> property = type.properties.get(index);
      return property.get.apply(instance);
   }

   @Override
   public Object set(int index, Object element) { 
      Property<M, Object> property = type.properties.get(index);
      Object previous = property.get.apply(instance);
      property.setSafe(instance, element);
      return previous;
   }

   @Override
   public int indexOf(Object object) { 
      int index = -1;
      
      for (Property<M, Object> property : type.properties) {
         index++;
         if (Objects.equals(property.get.apply(instance), object)) return index;
      }
      
      return -1;
   }

   @Override
   public int lastIndexOf(Object object) { 
      int index = type.properties.size();
      ListIterator<Property<M, Object>> iterator = type.properties.listIterator(index);
      
      while (iterator.hasPrevious()) {
         index--;
         Property<M, Object> property = iterator.previous();
         if (Objects.equals(property.get.apply(instance), object)) return index;
      }
      
      return -1;
   }

   /**
    * Delegates to {@link #listIterator()}.
    */
   @Override
   public Iterator<Object> iterator() { 
      return listIterator();
   }

   @Override
   public ListIterator<Object> listIterator() {
      return new ListIteratorWrapper<>(type, instance);
   }

   @Override
   public ListIterator<Object> listIterator(int index) {
      return new ListIteratorWrapper<>(type, instance, index);
   }

   @Override
   public boolean remove(Object object) { 
      int index = indexOf(object);
      if (index < 0) return false;
      set(index, null);
      return true;
   }

   /**
    * Delegates to {@link #set(int, Object)} with null as the value.
    */
   @Override
   public Object remove(int index) { 
      return set(index, null);
   }

   @Override
   public boolean removeAll(Collection<?> collection) {
      boolean changed = false;
      
      for (Object element : collection) {
         for (Property<M, Object> property : type.properties) {
            if (!Objects.equals(element, property.get.apply(instance))) continue;
            changed |= property.setSafe(instance, null);
         }
      }
      
      return changed;
   }

   @Override
   public boolean retainAll(Collection<?> collection) { 
      boolean changed = false;
      
      for (Property<M, Object> property : type.properties) {
         if (collection.contains(property.get.apply(instance))) continue;
         changed |= property.setSafe(instance, null);
      }
      
      return changed;
   }

   @Override
   public List<Object> subList(int fromIndex, int toIndex) { 
      Type<M> subtype = new Type<>(type.creator);
      subtype.properties = type.properties.subList(fromIndex, toIndex);
      
      return new ListWrapper<M>(subtype, instance);
   }
   
   @Override
   public Object[] toArray() { 
      return type.properties.stream().map(property -> property.get.apply(instance)).collect(toList()).toArray();
   }

   @Override
   public <T> T[] toArray(T[] array) {
      return type.properties.stream().map(property -> property.get.apply(instance)).collect(toList()).toArray(array);
   }

   public Type<M> getType() { return type; }

   public M getInstance() { return instance; }
   public void setInstance(M instance) { this.instance = instance; }
}
