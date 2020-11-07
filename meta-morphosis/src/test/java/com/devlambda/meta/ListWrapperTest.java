package com.devlambda.meta;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
 * Unit Test {@link ListWrapper}.
 * 
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class ListWrapperTest {
   
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
      pojo.setPrimitive(Math.PI);
      List<String> list = new ArrayList<>(identityType.getPropertyNames());
      Collections.shuffle(list, random);
      pojo.setList(list);

      reset(pojo);
   }

   @Test
   public void testSize() {
      List<Object> wrapper = Morph.wrapList(identityType, pojo);
      assertEquals(identityType.getProperties().size(), wrapper.size()); //test
      
      wrapper = Morph.wrapList(Type.meta(Pojo::new), pojo);
      assertEquals(0, wrapper.size()); //test
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testIsEmpty() {
      List<Object> wrapper = Morph.wrapList(identityType, pojo);
      assertFalse(wrapper.isEmpty()); //test
      
      wrapper = Morph.wrapList(Type.meta(Pojo::new), pojo);
      assertTrue(wrapper.isEmpty()); //test
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testContains() {
      List<Object> wrapper = Morph.wrapList(identityType, pojo);
      Iterator<Object> iterator = Morph.wrapIterator(identityType, pojo, 0);
      
      while (iterator.hasNext()) {
         assertTrue(wrapper.contains(iterator.next())); //test
      }
      
      assertFalse(wrapper.contains(Long.toHexString(random.nextLong()))); //test
      verify(pojo, atLeastOnce()).getCount();
      verify(pojo, atLeastOnce()).getFlag();
      verify(pojo, atLeastOnce()).getList();
      verify(pojo, atLeastOnce()).getMoney();
      verify(pojo, atLeastOnce()).getNumber();
      verify(pojo, atLeastOnce()).getParent();
      verify(pojo, atLeastOnce()).getText();
      verify(pojo, atLeastOnce()).getPrimitive();
      verifyNoMoreInteractions(pojo);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testIterator() {
      ListWrapper<Pojo> wrapper = spy(new ListWrapper<>(identityType, pojo));
      ListIterator<Object> iterator = mock(ListIterator.class);
      
      doReturn(iterator).when(wrapper).listIterator();
      
      assertEquals(iterator, wrapper.iterator()); //test
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testToArray() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      Object[] array = 
         { pojo.text, pojo.money, pojo.number, pojo.count, pojo.flag, pojo.parent, pojo.list, pojo.primitive };
      
      assertArrayEquals(array, wrapper.toArray()); //test
      
      verify(pojo, atLeastOnce()).getCount();
      verify(pojo, atLeastOnce()).getFlag();
      verify(pojo, atLeastOnce()).getList();
      verify(pojo, atLeastOnce()).getMoney();
      verify(pojo, atLeastOnce()).getNumber();
      verify(pojo, atLeastOnce()).getParent();
      verify(pojo, atLeastOnce()).getText();
      verify(pojo, atLeastOnce()).getPrimitive();
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testToArrayTArray() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      Object[] array = 
         { pojo.text, pojo.money, pojo.number, pojo.count, pojo.flag, pojo.parent, pojo.list, pojo.primitive };
      
      Object[] result = wrapper.toArray(new Object[4]); //test
      
      assertArrayEquals(array, result);
      verify(pojo, atLeastOnce()).getCount();
      verify(pojo, atLeastOnce()).getFlag();
      verify(pojo, atLeastOnce()).getList();
      verify(pojo, atLeastOnce()).getMoney();
      verify(pojo, atLeastOnce()).getNumber();
      verify(pojo, atLeastOnce()).getParent();
      verify(pojo, atLeastOnce()).getText();
      verify(pojo, atLeastOnce()).getPrimitive();
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testAddObject() {
      ListWrapper<Pojo> wrapper = spy(new ListWrapper<>(identityType, pojo));
      
      assertTrue(wrapper.add(BigDecimal.TEN)); //test
      assertEquals(BigDecimal.TEN, pojo.money);
      verify(pojo).setMoney(BigDecimal.TEN);
      assertFalse(wrapper.add(Collections.emptySet())); //test
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testAddIntObject() {
      ListWrapper<Pojo> wrapper = spy(new ListWrapper<>(identityType, pojo));
      Object previous = mock(Object.class);
      int index = 3;
      String element = Long.toHexString(random.nextLong());
      
      doReturn(previous).when(wrapper).set(anyInt(), any());
      
      wrapper.add(index, element); //test
      
      verify(wrapper).set(index, element);
      verifyZeroInteractions(pojo);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testAddAllCollection() {
      ListWrapper<Pojo> wrapper = spy(new ListWrapper<>(identityType, pojo));
      Collection<Object> collection = mock(Collection.class);
      
      doReturn(Boolean.TRUE).when(wrapper).addAll(0, collection);
      
      assertTrue(wrapper.addAll(collection)); //test
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testAddAllFromIndexCollection() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      Collection<Object> collection = Arrays.asList(BigDecimal.ONE, Double.NaN, Integer.MAX_VALUE);
      final int MONEY_INDEX = 1;
      
      assertTrue(wrapper.addAll(MONEY_INDEX, collection)); //test
      
      verify(pojo).setMoney(BigDecimal.ONE);
      verify(pojo).setNumber(Double.NaN);
      verify(pojo).setCount(Integer.MAX_VALUE);
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testContainsAll() {
      ListWrapper<Pojo> wrapper = spy(new ListWrapper<>(identityType, pojo));
      Collection<Object> collection = Arrays.asList(BigDecimal.ONE, Double.NaN, null, Integer.MAX_VALUE);
      
      doReturn(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE).when(wrapper).contains(any());
      
      assertFalse(wrapper.containsAll(collection)); //test
      assertTrue(wrapper.containsAll(collection)); //test
      
      verify(wrapper, times(2)).contains(BigDecimal.ONE);
      verify(wrapper, times(2)).contains(Double.NaN);
      verify(wrapper, times(2)).contains(null);
      verify(wrapper).contains(Integer.MAX_VALUE);
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testRemoveAll() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      List<Object> remove = Arrays.asList(pojo.text, pojo.primitive, Long.toHexString(random.nextLong()));
      
      assertTrue(wrapper.removeAll(remove)); //test
      
      verify(pojo, atLeastOnce()).getCount();
      verify(pojo, atLeastOnce()).getFlag();
      verify(pojo, atLeastOnce()).getList();
      verify(pojo, atLeastOnce()).getMoney();
      verify(pojo, atLeastOnce()).getNumber();
      verify(pojo, atLeastOnce()).getParent();
      verify(pojo, atLeastOnce()).getText();
      verify(pojo, atLeastOnce()).getPrimitive();
      verify(pojo).setText(null);
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testRetainAll() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      List<Object> remove = Arrays.asList(pojo.text, pojo.primitive, Long.toHexString(random.nextLong()));
      
      assertTrue(wrapper.retainAll(remove)); //test
      
      verify(pojo, atLeastOnce()).getCount();
      verify(pojo, atLeastOnce()).getFlag();
      verify(pojo, atLeastOnce()).getList();
      verify(pojo, atLeastOnce()).getMoney();
      verify(pojo, atLeastOnce()).getNumber();
      verify(pojo, atLeastOnce()).getParent();
      verify(pojo, atLeastOnce()).getText();
      verify(pojo, atLeastOnce()).getPrimitive();
      verify(pojo).setCount(null);
      verify(pojo).setFlag(null);
      verify(pojo).setList(null);
      verify(pojo).setMoney(null);
      verify(pojo).setNumber(null);
      verify(pojo).setParent(null);
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testClear() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      
      wrapper.clear(); //test
      
      verify(pojo).setCount(null);
      verify(pojo).setFlag(null);
      verify(pojo).setList(null);
      verify(pojo).setMoney(null);
      verify(pojo).setNumber(null);
      verify(pojo).setParent(null);
      verify(pojo).setText(null);
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testGet() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      final int MONEY_INDEX = 1;
      
      assertEquals(pojo.money, wrapper.get(MONEY_INDEX)); //test
      
      verify(pojo).getMoney();
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testSetIndex() {
      ListWrapper<Pojo> wrapper = spy(new ListWrapper<>(identityType, pojo));
      final int MONEY_INDEX = 1;
      BigDecimal previous = pojo.money;
      
      assertEquals(previous, wrapper.set(MONEY_INDEX, BigDecimal.TEN)); //test

      InOrder inOrder = inOrder(pojo);
      inOrder.verify(pojo).getMoney();
      inOrder.verify(pojo).setMoney(BigDecimal.TEN);
      verifyNoMoreInteractions(pojo);
   }
   
   @Test
   public void testRemoveObject() {
      ListWrapper<Pojo> wrapper = spy(new ListWrapper<>(identityType, pojo));
      Object previous = mock(Object.class);
      String element = Long.toHexString(random.nextLong());
      int index = Math.abs(random.nextInt());
      
      doReturn(index, -1).when(wrapper).indexOf(element);
      doReturn(previous).when(wrapper).set(index, null);
      
      assertTrue(wrapper.remove(element)); //test
      assertFalse(wrapper.remove(element)); //test
      verify(wrapper).set(index, null);
      verify(wrapper, never()).set(anyInt(), any(Object.class));
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testRemoveInt() {
      ListWrapper<Pojo> wrapper = spy(new ListWrapper<>(identityType, pojo));
      Object previous = mock(Object.class);
      int index = random.nextInt();
      
      doReturn(previous).when(wrapper).set(index, null);
      
      assertEquals(previous, wrapper.remove(index)); //test
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testIndexOf() {
      pojo.setPrimitive(pojo.getNumber());
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      ListIterator<Object> iterator = Morph.wrapIterator(identityType, pojo, 0);
      Property<Pojo, Object> number = identityType.getProperty("NUMBER");
      Property<Pojo, Object> primitive = identityType.getProperty("PRIMITIVE");
      int numberIndex = identityType.getProperties().indexOf(number);
      int primitiveIndex = identityType.getProperties().indexOf(primitive);
      int expectedIndex = Math.min(numberIndex, primitiveIndex);
      
      while (iterator.hasNext()) {
         Object value = iterator.next();
         int index = iterator.previousIndex();
         
         if (index == numberIndex || index == primitiveIndex) {
            assertEquals(expectedIndex, wrapper.indexOf(value)); //test
         } else assertEquals(index, wrapper.indexOf(value)); //test
      }
      
      assertEquals(-1, wrapper.indexOf(Double.NaN));
      
      verify(pojo, atLeastOnce()).getCount();
      verify(pojo, atLeastOnce()).getFlag();
      verify(pojo, atLeastOnce()).getList();
      verify(pojo, atLeastOnce()).getMoney();
      verify(pojo, atLeastOnce()).getNumber();
      verify(pojo, atLeastOnce()).getParent();
      verify(pojo, atLeastOnce()).getText();
      verify(pojo, atLeastOnce()).getPrimitive();
      verify(pojo).setPrimitive(anyDouble());
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testLastIndexOf() {
      pojo.setPrimitive(pojo.getNumber());
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      ListIterator<Object> iterator = Morph.wrapIterator(identityType, pojo, identityType.getProperties().size());
      Property<Pojo, Object> number = identityType.getProperty("NUMBER");
      Property<Pojo, Object> primitive = identityType.getProperty("PRIMITIVE");
      int numberIndex = identityType.getProperties().indexOf(number);
      int primitiveIndex = identityType.getProperties().indexOf(primitive);
      int expectedIndex = Math.max(numberIndex, primitiveIndex);
      
      while (iterator.hasPrevious()) {
         Object value = iterator.previous();
         int index = iterator.nextIndex();
         
         if (index == numberIndex || index == primitiveIndex) {
            assertEquals(expectedIndex, wrapper.lastIndexOf(value)); //test
         } else assertEquals(index, wrapper.lastIndexOf(value)); //test
      }
      
      assertEquals(-1, wrapper.lastIndexOf(Double.NaN));
      
      verify(pojo, atLeastOnce()).getCount();
      verify(pojo, atLeastOnce()).getFlag();
      verify(pojo, atLeastOnce()).getList();
      verify(pojo, atLeastOnce()).getMoney();
      verify(pojo, atLeastOnce()).getNumber();
      verify(pojo, atLeastOnce()).getParent();
      verify(pojo, atLeastOnce()).getText();
      verify(pojo, atLeastOnce()).getPrimitive();
      verify(pojo).setPrimitive(anyDouble());
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testListIterator() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      
      ListIteratorWrapper<Pojo> listIterator = (ListIteratorWrapper<Pojo>) wrapper.listIterator(); //test
      
      assertEquals(identityType, listIterator.getType());
      assertEquals(pojo, listIterator.getInstance());
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testListIteratorInt() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      
      ListIteratorWrapper<Pojo> listIterator = (ListIteratorWrapper<Pojo>) wrapper.listIterator(3); //test
      
      assertEquals(identityType, listIterator.getType());
      assertEquals(pojo, listIterator.getInstance());
      assertEquals(3, listIterator.nextIndex());
      assertEquals(2, listIterator.previousIndex());
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testSubList() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      
      List<Object> subList = wrapper.subList(2, 6); //test
      
      assertTrue(subList instanceof ListWrapper);
      assertEquals(4, subList.size());
      
      for (int index = 2, sub = 0; index < 6; index++, sub++) {
         assertEquals(wrapper.get(index), subList.get(sub));
      }
      
      ListWrapper<Pojo> subWrapper = (ListWrapper<Pojo>) subList;
      
      assertEquals(pojo, subWrapper.getInstance());
      assertNotEquals(identityType, subWrapper.getType());
      assertEquals(creator, subWrapper.getType().getCreator());
      assertTrue(identityType.properties.containsAll(subWrapper.getType().getProperties()));
   }

   @Test
   public void testGetType() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      
      assertEquals(identityType, wrapper.getType()); //test
   }

   @Test
   public void testGetInstance() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      
      assertEquals(pojo, wrapper.getInstance()); //test
   }
   
   @Test
   public void testSetInstance() {
      ListWrapper<Pojo> wrapper = new ListWrapper<>(identityType, pojo);
      Pojo mock = mock(Pojo.class);
      wrapper.setInstance(mock);
      
      assertEquals(mock, wrapper.getInstance()); //test
   }

   @AfterClass
   public static void shutDown() {
      verifyZeroInteractions(creator);
   }
}
