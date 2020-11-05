package com.devlambda.meta;


import java.util.Set;


/**
 * @param <E> the type of elements in this collection.
 *
 * @author Dewald Pretorius
 */
public class SetDecorator<E> extends CollectionDecorator<E> implements Set<E> {
   
   protected final Set<E> decorated;
   
   public SetDecorator(Set<E> decorated) {
      super(decorated);
      
      this.decorated = decorated;
   }
}
