package com.devlambda.meta;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * Unit Test {@link CollectionDecorator}.
 * 
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectionDecoratorTest extends IterableDecoratorTest {
   protected Random random = new Random();
   protected @Mock Collection<Integer> collection;

   private CollectionDecorator<Integer> decorator;

   @Override
   public void setUp() throws Exception { 
      super.iterable = collection;
      decorator = spy(new CollectionDecorator<>(collection));
      assertEquals(collection, decorator.decorated);
      
      super.setUp();
   }
   
   @Test
   public void testSize() {
      int size = random.nextInt();
      
      when(collection.size()).thenReturn(size);
      
      assertEquals(size, decorator.size()); //test
   }

   @Test
   public void testIsEmpty() {
      boolean empty = random.nextBoolean();
      
      when(collection.isEmpty()).thenReturn(empty);
      
      assertEquals(empty, decorator.isEmpty()); //test
      
      verify(collection).isEmpty();
   }

   @Test
   public void testContains() {
      boolean contains = random.nextBoolean();
      Integer object = random.nextInt();
      
      when(collection.contains(object)).thenReturn(contains);
      
      assertEquals(contains, decorator.contains(object)); //test
      
      verify(collection).contains(object);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testContainsAll() {
      boolean containsAll = random.nextBoolean();
      Collection<Integer> collection = mock(Collection.class); 
      
      when(this.collection.containsAll(collection)).thenReturn(containsAll);
      
      assertEquals(containsAll, decorator.containsAll(collection)); //test
      
      verify(this.collection).containsAll(collection);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testParallelStream() {
      Stream<Integer> parallelStream = mock(Stream.class);
      
      when(collection.parallelStream()).thenReturn(parallelStream);
      
      assertEquals(parallelStream, decorator.parallelStream()); //test
      
      verify(collection).parallelStream();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testStream() {
      Stream<Integer> stream = mock(Stream.class);
      
      when(collection.stream()).thenReturn(stream);
      
      assertEquals(stream, decorator.stream()); //test
      
      verify(collection).stream();
   }

   @Test
   public void testAdd() {
      boolean added = random.nextBoolean();
      Integer element = random.nextInt();
      
      when(collection.add(element)).thenReturn(added);
      
      assertEquals(added, decorator.add(element)); //test
      
      verify(collection).add(element);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testAddAll() {
      boolean addAll = random.nextBoolean();
      Collection<Integer> collection = mock(Collection.class);
      
      when(this.collection.addAll(collection)).thenReturn(addAll);
      
      assertEquals(addAll, decorator.addAll(collection)); //test
      
      verify(this.collection).addAll(collection);
   }

   @Test
   public void testClear() {
      decorator.clear();
      
      verify(collection).clear();
   }

   @Test
   public void testRemove() {
      boolean removed = random.nextBoolean();
      Integer element = random.nextInt();
      
      when(collection.remove(element)).thenReturn(removed);
      
      assertEquals(removed, decorator.remove(element)); //test
      
      verify(collection).remove(element);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testRemoveIf() {
      boolean removed = random.nextBoolean();
      Predicate<Integer> filter = mock(Predicate.class);
      
      when(collection.removeIf(filter)).thenReturn(removed);
      
      assertEquals(removed, decorator.removeIf(filter)); //test
      
      verify(collection).removeIf(filter);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testRetainAll() {
      boolean changed = random.nextBoolean();
      Collection<Integer> collection = mock(Collection.class);
      
      when(this.collection.retainAll(collection)).thenReturn(changed);
      
      assertEquals(changed, decorator.retainAll(collection)); //test
      
      verify(this.collection).retainAll(collection);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testRemoveAll() {
      boolean changed = random.nextBoolean();
      Collection<Integer> collection = mock(Collection.class);
      
      when(this.collection.removeAll(collection)).thenReturn(changed);
      
      assertEquals(changed, decorator.removeAll(collection)); //test
      
      verify(this.collection).removeAll(collection);
   }

   @Test
   public void testToArray() {
      Integer[] array = { random.nextInt(), random.nextInt(), random.nextInt() };
      
      when(collection.toArray()).thenReturn(array);
      
      assertArrayEquals(array, decorator.toArray()); //test
      
      verify(collection).toArray();
   }

   @Test
   public void testToArrayTArray() {
      Integer[] inputArray = { random.nextInt(), random.nextInt(), random.nextInt() };
      Integer[] outputArray = { random.nextInt(), random.nextInt(), random.nextInt() };
      
      when(collection.toArray(inputArray)).thenReturn(outputArray);
      
      assertArrayEquals(outputArray, decorator.toArray(inputArray));
      
      verify(collection).toArray(inputArray);
   }
}
