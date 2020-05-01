package com.devlambda.meta;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * Unit Test {@link IterableDecorator}.
 * 
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class IterableDecoratorTest {
   protected @Mock Iterable<Integer> iterable;
   
   private IterableDecorator<Integer> decorator;
   
   @Before
   public void setUp() throws Exception {
      decorator = spy(new IterableDecorator<>(iterable));
      assertEquals(iterable, decorator.decorated);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testIterator() {
      Iterator<Integer> iterator = mock(Iterator.class);
      
      when(iterable.iterator()).thenReturn(iterator);
      
      assertEquals(iterator, decorator.iterator()); //test
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testForEach() {
      Consumer<Integer> action = mock(Consumer.class);
      doNothing().when(iterable).forEach(action);
      
      decorator.forEach(action); //test
      
      verify(iterable).forEach(action);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testSpliterator() {
      Spliterator<Integer> spliterator = mock(Spliterator.class);
      
      when(iterable.spliterator()).thenReturn(spliterator);
      
      assertEquals(spliterator, decorator.spliterator()); //test
   }
}
