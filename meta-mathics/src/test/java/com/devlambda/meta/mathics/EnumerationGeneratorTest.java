package com.devlambda.meta.mathics;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;


/**
 * @author Dewald Pretorius
 */
public class EnumerationGeneratorTest {

   @Test
   public void test() {
      List<String> hexadecimal = 
            Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F");
      EnumerationGenerator<String> enumerations = new EnumerationGenerator<>(hexadecimal, 2);

      assertEquals((int) Math.pow(16, 2), enumerations.total());
      List<List<String>> enumerationList = new ArrayList<>((int) enumerations.total());

      for (List<String> enumaration : enumerations) {
         enumerationList.add(enumaration);
         System.out.println(enumaration.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]")));
      }

      assertEquals((int) Math.pow(16, 2), enumerationList.size());
      assertEquals((int) Math.pow(16, 2), enumerations.total());
      
      List<Integer> indexList = 
            IntStream.range(0, (int) enumerations.total()).mapToObj(digit -> digit).collect(Collectors.toList());
      Collections.shuffle(indexList);
      
      for (Integer index : indexList) {
         enumerations.setIndex(index);
         assertEquals(enumerationList.get(index), (index % 2 == 0) ? enumerations.next() : enumerations.current());
      }
   }
}
