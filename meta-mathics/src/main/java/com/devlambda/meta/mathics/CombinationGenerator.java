package com.devlambda.meta.mathics;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author Dewald Pretorius
 */
public class CombinationGenerator<T> implements Iterable<List<T>>, Iterator<List<T>> {

   protected final List<T> domain;
   protected final int[] indexes;
   protected final int radix;
   protected final int lastIx;
   protected final int sizeDiff;

   public CombinationGenerator(Collection<T> collection, int size) {
      this.domain = new ArrayList<>(collection);
      this.indexes = new int[size];
      this.radix = this.domain.size();
      this.lastIx = size > radix ? radix - 1 : size - 1;
      this.sizeDiff = size > radix ? 0 : radix - size;

      // setup start indexes
      for (int x = 0; x < indexes.length; x++) {
         indexes[x] = x;
      }
   }

   @Override
   public Iterator<List<T>> iterator() {
      return new CombinationGenerator<>(domain, indexes.length);
   }

   @Override
   public boolean hasNext() {
      return indexes[0] <= sizeDiff;
   }

   @Override
   public List<T> next() {
      List<T> combination = IntStream.of(indexes).mapToObj(domain::get).collect(Collectors.toList());
      advanceIndexes();

      return combination;
   }

   protected void advanceIndexes() {
      int depth = lastIx;

      for ( ; depth > 0; depth--) {
         if (indexes[depth] < sizeDiff + depth) break;
      }

      int index = indexes[depth];

      for (int x = depth; x < indexes.length; x++) {
         indexes[x] = ++index;
      }
   }
}
