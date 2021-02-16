package com.devlambda.meta;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * Unit Test {@link ListIteratorWrapper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListIteratorWrapperTest {
   
   static Supplier<Pojo> creator;
   static Type<Pojo> identityType;
   static Random random;
   
   @Spy Pojo pojo;

   @BeforeClass
   @SuppressWarnings("unchecked")
   public static void bootUp() throws Exception {
      random = new Random();
      creator = mock(Supplier.class);
      identityType = new Type<>(creator);
      identityType.add("TEXT", String.class, Pojo::getText, Pojo::setText);
      identityType.add("MONEY", BigDecimal.class, Pojo::getMoney, Pojo::setMoney);
      identityType.add("NUMBER", Double.class, Pojo::getNumber, Pojo::setNumber);
      identityType.add("COUNT", Integer.class, Pojo::getCount, Pojo::setCount);
      identityType.add("FLAG", Boolean.class, Pojo::getFlag, Pojo::setFlag);
      identityType.add("PARENT", Pojo.class, Pojo::getParent, Pojo::setParent);
      identityType.add("LIST", List.class, Pojo::getList, Pojo::setList);
      identityType.add("PRIMITIVE", double.class, Pojo::getPrimitive, Pojo::setPrimitive);
   }

   @Before
   public void setUp() throws Exception {
      pojo.setCount(random.nextInt());
      pojo.setFlag(random.nextBoolean());
      pojo.setMoney(BigDecimal.valueOf(random.nextLong()));
      pojo.setNumber(random.nextDouble());
      pojo.setText(Long.toHexString(random.nextLong()));
      pojo.setParent(pojo);
      List<String> list = new ArrayList<>(identityType.getPropertyNames());
      Collections.shuffle(list, random);
      pojo.setList(list);
      pojo.setPrimitive(random.nextDouble());

      reset(pojo);
   }

   @Test
   public void testNextXxx() {
      int expectNextIndex = 0;
      ListIteratorWrapper<Pojo> iterator = (ListIteratorWrapper<Pojo>) Morph.wrapIterator(identityType, pojo, 0);
      
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertTrue(iterator.hasNext());
      assertEquals(pojo.text, iterator.next());
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertTrue(iterator.hasNext());
      assertEquals(pojo.money, iterator.next());
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertTrue(iterator.hasNext());
      assertEquals(pojo.number, iterator.next());
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertTrue(iterator.hasNext());
      assertEquals(pojo.count, iterator.next());
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertTrue(iterator.hasNext());
      assertEquals(pojo.flag, iterator.next());
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertTrue(iterator.hasNext());
      assertEquals(pojo.parent, iterator.next());
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertTrue(iterator.hasNext());
      assertEquals(pojo.list, iterator.next());
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertTrue(iterator.hasNext());
      assertEquals(pojo.primitive, iterator.next());
      assertEquals(expectNextIndex++, iterator.nextIndex());
      assertFalse(iterator.hasNext());
      
      InOrder inOrder = inOrder(pojo);
      inOrder.verify(pojo).getText();
      inOrder.verify(pojo).getMoney();
      inOrder.verify(pojo).getNumber();
      inOrder.verify(pojo).getCount();
      inOrder.verify(pojo).getFlag();
      inOrder.verify(pojo).getParent();
      inOrder.verify(pojo).getList();
      inOrder.verify(pojo).getPrimitive();
      inOrder.verifyNoMoreInteractions();
   }

   @Test
   public void testPreviousXxx() {
      int expectPreviousIndex = identityType.getProperties().size();
      ListIteratorWrapper<Pojo> iterator = new ListIteratorWrapper<>(identityType, pojo, expectPreviousIndex--);
      
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      assertEquals(pojo.primitive, iterator.previous());
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      assertEquals(pojo.list, iterator.previous());
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      assertEquals(pojo.parent, iterator.previous());
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      assertEquals(pojo.flag, iterator.previous());
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      assertEquals(pojo.count, iterator.previous());
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      assertEquals(pojo.number, iterator.previous());
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      assertEquals(pojo.money, iterator.previous());
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertTrue(iterator.hasPrevious());
      assertEquals(pojo.text, iterator.previous());
      assertEquals(expectPreviousIndex--, iterator.previousIndex());
      assertFalse(iterator.hasPrevious());
      
      InOrder inOrder = inOrder(pojo);
      inOrder.verify(pojo).getPrimitive();
      inOrder.verify(pojo).getList();
      inOrder.verify(pojo).getParent();
      inOrder.verify(pojo).getFlag();
      inOrder.verify(pojo).getCount();
      inOrder.verify(pojo).getNumber();
      inOrder.verify(pojo).getMoney();
      inOrder.verify(pojo).getText();
      inOrder.verifyNoMoreInteractions();
   }

   @Test
   public void testRemove() {
      final int MONEY_INDEX = 1;
      ListIteratorWrapper<Pojo> iterator = new ListIteratorWrapper<>(identityType, pojo, MONEY_INDEX);
      Object next = iterator.next();
      
      assertNotNull(next);
      assertEquals(pojo.money, next); //prove non-null money field value
      verify(pojo).getMoney();
      
      iterator.remove(); //test
      
      Object previous = iterator.previous();
      assertNull(previous);
      assertEquals(pojo.money, previous); //prove removed(set null) money field value
      verify(pojo, times(2)).getMoney();
      verify(pojo).setMoney(null);
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testSet() {
      ListIteratorWrapper<Pojo> iterator = (ListIteratorWrapper<Pojo>) Morph.wrapIterator(identityType, pojo, 0);
      Object next = iterator.next();
      
      assertNotNull(next);
      assertEquals(pojo.text, next); 
      verify(pojo).getText();
      
      String update = Long.toOctalString(random.nextLong()); 
      iterator.set(update); //test
      
      Object previous = iterator.previous();
      assertNotEquals(next, previous);
      assertEquals(update, previous);
      assertEquals(update, pojo.text);
      verify(pojo, times(2)).getText();
      verify(pojo).setText(update);
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testAdd() {
      ListIteratorWrapper<Pojo> iterator = spy(new ListIteratorWrapper<>(identityType, pojo));
      Long value = Long.valueOf(random.nextLong());
      
      doNothing().when(iterator).set(any());
      
      iterator.add(value);
      
      verify(iterator).set(value);
   }

   @Test
   public void testGetType() {
      ListIteratorWrapper<Pojo> iterator = new ListIteratorWrapper<>(identityType, pojo);
      
      assertEquals(identityType, iterator.getType()); //test
   }

   @Test
   public void testGetInstance() {
      ListIteratorWrapper<Pojo> iterator = new ListIteratorWrapper<>(identityType, pojo);
      
      assertEquals(pojo, iterator.getInstance()); //test
   }
   
   @Test
   public void testSetInstance() {
      ListIteratorWrapper<Pojo> iterator = new ListIteratorWrapper<>(identityType, pojo);
      Pojo mock = mock(Pojo.class);
      iterator.setInstance(mock);
      
      assertEquals(mock, iterator.getInstance()); //test
   }
   
   @AfterClass
   public static void shutDown() {
      verifyZeroInteractions(creator);
   }
}
