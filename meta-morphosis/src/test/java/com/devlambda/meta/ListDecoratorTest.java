package com.devlambda.meta;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit Test {@link ListDecorator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListDecoratorTest extends CollectionDecoratorTest {
   protected @Mock List<Integer> list;
   
   private ListDecorator<Integer> decorator;
   
   @Before
   public void setUp() throws Exception {
      super.iterable = list;
      super.collection = list;
      decorator = spy(new ListDecorator<>(list));
      assertEquals(list, decorator.decorated);
      
      super.setUp();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testAddAllIndexCollectionElements() {
      boolean changed = random.nextBoolean();
      int index = random.nextInt();
      Collection<Integer> collection = mock(Collection.class);
      
      when(list.addAll(index, collection)).thenReturn(changed);
      
      assertEquals(changed, decorator.addAll(index, collection)); //test
      
      verify(list).addAll(index, collection);
   }

   @Test
   public void testGet() {
      int index = random.nextInt();
      Integer element = random.nextInt();
      
      when(list.get(index)).thenReturn(element);
      
      assertEquals(element, decorator.get(index)); //test
      
      verify(list).get(index);
   }

   @Test
   public void testSet() {
      int index = random.nextInt();
      Integer element = random.nextInt();
      Integer previous = random.nextInt();
      
      when(list.set(index, element)).thenReturn(previous);
      
      assertEquals(previous, decorator.set(index, element)); //test
      
      verify(list).set(index, element);
   }

   @Test
   public void testAddIndexElement() {
      int index = random.nextInt();
      Integer element = random.nextInt();
      
      decorator.add(index, element);
      
      verify(list).add(index, element);
   }

   @Test
   public void testRemoveInt() {
      int index = random.nextInt();
      Integer element = random.nextInt();
      
      when(list.remove(index)).thenReturn(element);
      
      assertEquals(element, decorator.remove(index)); //test
      
      verify(list).remove(index);
   }

   @Test
   public void testIndexOf() {
      Integer object = random.nextInt();
      int index = random.nextInt();
      
      when(list.indexOf(object)).thenReturn(index);
      
      assertEquals(index, decorator.indexOf(object)); //test
      
      verify(list).indexOf(object);
   }

   @Test
   public void testLastIndexOf() {
      Integer object = random.nextInt();
      int index = random.nextInt();
      
      when(list.lastIndexOf(object)).thenReturn(index);
      
      assertEquals(index, decorator.lastIndexOf(object)); //test
      
      verify(list).lastIndexOf(object);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testListIterator() {
      ListIterator<Integer> listIterator = mock(ListIterator.class);
      
      when(list.listIterator()).thenReturn(listIterator);
      
      assertEquals(listIterator, decorator.listIterator()); //test
      
      verify(list).listIterator();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testListIteratorInt() {
      int index = random.nextInt();
      ListIterator<Integer> listIterator = mock(ListIterator.class);
      
      when(list.listIterator(index)).thenReturn(listIterator);
      
      assertEquals(listIterator, decorator.listIterator(index)); //test
      
      verify(list).listIterator(index);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testSubList() {
      int fromIndex = random.nextInt();
      int toIndex = random.nextInt();
      List<Integer> subList = mock(List.class);
      
      when(list.subList(fromIndex, toIndex)).thenReturn(subList);
      
      assertEquals(subList, decorator.subList(fromIndex, toIndex)); //test
      
      verify(list).subList(fromIndex, toIndex);
   }
}
