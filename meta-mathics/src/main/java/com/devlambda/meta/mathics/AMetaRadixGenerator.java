package com.devlambda.meta.mathics;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Abstract implementation of {@link IMetaRadixGenerator}.
 * 
 * @author Dewald Pretorius
 */
public abstract class AMetaRadixGenerator<T> implements IMetaRadixGenerator<T> {
   
   protected final List<T> domain;
   protected final int[] indexes;
   protected final int radix;
   protected int index;
   
   protected AMetaRadixGenerator(Collection<T> domain, int size) {
      this.domain = new ArrayList<>(domain);
      this.indexes = new int[size];
      this.radix = this.domain.size();
   }

   @Override
   public List<T> current() {
      return IntStream.of(indexes).mapToObj(domain::get).collect(Collectors.toList());
   }
   
   @Override
   public boolean hasNext() { 
      return getIndex() < total();
   }
   
   @Override
   public boolean hasPrevious() { 
      return getIndex() == 0;
   }
   
   @Override
   public List<T> next() {
      List<T> group = current();
      advanceIndex();
      
      return group;
   }
   
   @Override
   public List<T> previous() { 
      reduceIndex();
      return current();
   }
   
   @Override
   public int nextIndex() {
      return index + 1;
   }

   @Override
   public int previousIndex() {
      return index - 1;
   }
   
   @Override
   public List<T> remainder() {
      if (domain.size() == indexes.length) return Collections.emptyList();
      
      List<T> remainder = new ArrayList<>(domain);
      remainder.removeAll(current());
      
      return remainder;
   }

   public int getIndex() { return index; }

   public abstract void setIndex(int index);
}
