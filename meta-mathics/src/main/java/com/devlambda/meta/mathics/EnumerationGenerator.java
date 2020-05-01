package com.devlambda.meta.mathics;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;


/**
 * @author Dewald Pretorius
 */
public class EnumerationGenerator<T> extends AMetaRadixGenerator<T> implements RandomAccess {

   private final long total;

   public EnumerationGenerator(Collection<T> collection, int size) {
      super(collection, size);
      
      long multiplier = 1;
      
      for (int x = 0; x < size; x++) {
         multiplier *= radix;
      }
      
      this.total = multiplier;
   }

   @Override
   public Iterator<List<T>> iterator() {
      return new EnumerationGenerator<>(domain, indexes.length);
   }

   @Override
   public void advanceIndex() {
      adjustIndex(1);
   }
   
   void adjustIndex(int offset) {
      index += offset;
      
      for (int x = indexes.length - 1; x >= 0; x--) {
         indexes[x] = (indexes[x] + offset + radix) % radix;
         if (indexes[x] != 0) break;
      }
   }

   @Override
   public void reduceIndex() {
      adjustIndex(-1);
   }

   @Override
   public void setIndex(int index) {
      if (index < 0 || index >= total()) throw new IndexOutOfBoundsException("index: " + index);
      this.index = index;
      
      for (int x = indexes.length - 1; x >= 0; x--) {
         indexes[x] = index % radix;
         index /= radix;
      }
   }

   @Override
   public long total() {
      return total;
   }
}
