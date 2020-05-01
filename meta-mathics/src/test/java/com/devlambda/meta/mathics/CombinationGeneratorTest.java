package com.devlambda.meta.mathics;


import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Ignore;
import org.junit.Test;


public class CombinationGeneratorTest {

   @Test
   public void test() {
      Set<Integer> numbers = IntStream.range(1, 6).mapToObj(number -> number).collect(Collectors.toSet());
      CombinationGenerator<Integer> combinations = new CombinationGenerator<>(numbers, 3);

      int expected = 5 * 4 / 2 * 1; // 5! / (5-3)! * 3!
      int count = 0;

      while (combinations.hasNext()) {
         count++;
         List<Integer> combination = combinations.next();

         System.out.println(combination.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]")));
      }

      assertEquals(expected, count);
   }

   @Ignore
   @Test
   public void testFilterCombinations() {
      Set<Integer> numbers = IntStream.range(1, 20).mapToObj(number -> number).collect(Collectors.toSet());
      CombinationGenerator<Integer> combinations = new CombinationGenerator<>(numbers, 3);
      Stream<List<Integer>> stream = StreamSupport.stream(combinations.spliterator(), false);

      List<List<Integer>> filtered = stream.filter(combination -> {
         int sum = combination.stream().mapToInt(Integer::intValue).sum();
         return sum > 38 && sum <= 38 + 8;
      }).collect(Collectors.toList());

      for (List<Integer> combination : filtered) {
         System.out.print(combination.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]")));
         System.out.println(" = " + combination.stream().mapToInt(Integer::intValue).sum());
      }

      System.out.println("Combinations: " + filtered.size());
   }
}
