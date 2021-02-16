package com.devlambda.meta.mathics;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Unit Test {@link PermutationGenerator}.
 */
public class PermutationGeneratorTest {

   @Test
   public void testSubGroup() {
      Set<String> numbers = Arrays.asList("A", "B", "C", "D", "E").stream().collect(Collectors.toSet());
      PermutationGenerator<String> permutations = new PermutationGenerator<>(numbers, 3);

      assertEquals(60, permutations.total());
      List<List<String>> permutationList = new ArrayList<>(60);

      while (permutations.hasNext()) {
         List<String> permutation = permutations.next();
         permutationList.add(permutation);
         System.out.println(permutation.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]")));
      }

      assertEquals(60, permutationList.size());
      
      List<Integer> indexList = IntStream.range(0, 60).mapToObj(digit -> digit).collect(Collectors.toList());
      Collections.shuffle(indexList);
      
      for (Integer index : indexList) {
         permutations.setIndex(index);
         assertEquals(permutationList.get(index), (index % 2 == 0) ? permutations.next() : permutations.current());
      }
   }
   
   @Test
   public void test() {
      Set<String> numbers = Arrays.asList("X", "Y", "Z").stream().collect(Collectors.toSet());
      PermutationGenerator<String> permutations = new PermutationGenerator<>(numbers, 3);
      
      assertEquals(6, permutations.total());
      List<List<String>> permutationList = new ArrayList<>(6);
      
      while (permutations.hasNext()) {
         List<String> permutation = permutations.next();
         permutationList.add(permutation);
         System.out.println(permutation.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]")));
      }

      assertEquals(6, permutationList.size());
      
      List<Integer> indexList = IntStream.range(0, 6).mapToObj(digit -> digit).collect(Collectors.toList());
      Collections.shuffle(indexList);
      
      for (Integer index : indexList) {
         permutations.setIndex(index);
         assertEquals(permutationList.get(index), (index % 2 == 0) ? permutations.next() : permutations.current());
      }
   }
}
