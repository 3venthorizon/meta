package com.devlambda.meta.mathics;


import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.stream.IntStream;


/**
 * Permutation Generator.
 */
public class PermutationGenerator<T> extends AMetaRadixGenerator<T> implements RandomAccess {

   protected final int[] digits;
   protected final int[] radixes;
   protected final long total;

   public PermutationGenerator(Collection<T> collection, int size) {
      super(collection, size);
      
      this.digits = new int[size];
      this.radixes = new int[size];
      long multiplier = 1;
      
      for (int x = 0; x < indexes.length; x++) {
         radixes[x] = radix - x;
         multiplier *= radixes[x];
      }
      
      this.total = multiplier;
      
      setIndex(0);
   }

   @Override
   public Iterator<List<T>> iterator() {
      return new PermutationGenerator<T>(domain, indexes.length);
   }

   @Override
   public void advanceIndex() { 
      adjustIndex(1);
   }
   
   void adjustIndex(int offset) {
      index += offset;
      
      for (int x = digits.length -1; x >= 0; x--) {
         digits[x] = (digits[x] + offset + radixes[x]) % radixes[x]; 
         if (digits[x] != 0) break;
      }
      
      List<Integer> abacus = IntStream.range(0, radix).mapToObj(digit -> digit).collect(toList());
      
      for (int x = 0; x < digits.length; x++) {
         indexes[x] = abacus.remove(digits[x]).intValue();
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
      
      for (int x = digits.length -1; x >= 0; x--) {
         digits[x] = index % radixes[x];
         index /= radixes[x];
      }
      
      List<Integer> abacus = IntStream.range(0, radix).mapToObj(digit -> digit).collect(toList());
      
      for (int x = 0; x < digits.length; x++) {
         indexes[x] = abacus.remove(digits[x]).intValue();
      }
   }

   @Override
   public long total() {
      return total;
   }
}
