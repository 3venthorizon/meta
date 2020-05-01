package com.devlambda.meta;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * Unit Test {@link MapWrapper}.
 * 
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class MapWrapperTest {
   
   static Supplier<Pojo> creator;
   static Type<Pojo> identityType;
   static Type<Pojo> aliasType;
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
   @SuppressWarnings({ "unchecked" })
   public void testPropertyEntry() {
      Type<Pojo> type = Type.meta(creator);
      Function<Pojo, Object> getter = mock(Function.class);
      BiConsumer<Pojo, Object> setter = (pojo, value) -> {};
      Property<Pojo, Object> property = spy(new Property<Pojo, Object>("MOCK", Object.class, getter, setter));
      type.getProperties().add(property);
      Map<String, Object> wrapper = Meta.wrapMap(type, pojo);
      Entry<String, Object> entry = wrapper.entrySet().iterator().next();
      String value = Long.toHexString(random.nextLong());
      String update = Long.toHexString(random.nextLong());
      
      when(getter.apply(pojo)).thenReturn(value);
      
      assertEquals("MOCK", entry.getKey()); //test
      assertEquals(value, entry.getValue()); //test
      assertEquals(value, entry.setValue(update)); //test
      verify(getter, times(2)).apply(pojo);
      verify(property).setSafe(pojo, update);
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testSize() {
      Map<String, Object> wrapper = Meta.wrapMap(identityType, pojo);
      assertEquals(identityType.getProperties().size(), wrapper.size()); //test

      wrapper = Meta.wrapMap(Type.meta(Pojo::new), pojo);
      assertEquals(0, wrapper.size()); //test
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testIsEmpty() {
      Map<String, Object> wrapper = Meta.wrapMap(identityType, pojo);
      assertFalse(wrapper.isEmpty()); //test
      
      wrapper = Meta.wrapMap(Type.meta(Pojo::new), pojo);
      assertTrue(wrapper.isEmpty()); //test
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testContainsKey() {
      Map<String, Object> wrapper = Meta.wrapMap(identityType, pojo);

      assertTrue(identityType.getPropertyNames().stream().allMatch(wrapper::containsKey)); //test
      assertFalse(wrapper.containsKey("BAD KEY")); //test
      
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testContainsValue() {
      Map<String, Object> wrapper = Meta.wrapMap(identityType, pojo);
      Iterator<Object> iterator = Meta.wrapIterator(identityType, pojo, 0);
      
      while (iterator.hasNext()) {
         assertTrue(wrapper.containsValue(iterator.next())); //test
      }
      
      assertFalse(wrapper.containsValue(Double.NaN)); //test
      
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
   public void testGet() {
      Map<String, Object> wrapper = Meta.wrapMap(identityType, pojo);
      
      assertEquals(pojo.text, wrapper.get("TEXT"));
      assertEquals(pojo.money, wrapper.get("MONEY"));
      assertEquals(pojo.count, wrapper.get("COUNT"));
      assertEquals(pojo.flag, wrapper.get("FLAG"));
      assertEquals(pojo.number, wrapper.get("NUMBER"));
      assertEquals(pojo.parent, wrapper.get("PARENT"));
      assertEquals(pojo.list, wrapper.get("LIST"));
      assertEquals(pojo.primitive, wrapper.get("PRIMITIVE"));
      assertNull(wrapper.get("NO KEY"));
      
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
   public void testPut() {
      Type<Pojo> overloadType = Type.meta(creator);
      overloadType.getProperties().addAll(identityType.getProperties());
      //over loads the property TEXT, but this one points to Pojo#number
      overloadType.add("TEXT", Double.class, Pojo::getNumber, Pojo::setNumber);
      Map<String, Object> wrapper = Meta.wrapMap(overloadType, pojo);
      BigDecimal previousMoney = pojo.money;
      Double previousNumber = pojo.number;
      
      assertEquals(previousMoney, wrapper.put("MONEY", BigDecimal.TEN.pow(6))); //test
      assertEquals(pojo, wrapper.put("PARENT", null)); //test null value
      assertNull(wrapper.put("NO KEY", random.nextDouble())); //test property not found
      assertEquals(previousNumber, wrapper.put("TEXT", Math.E)); //test set overloaded property
      
      verify(pojo).getMoney();
      verify(pojo).setMoney(BigDecimal.TEN.pow(6));
      verify(pojo).getNumber();
      verify(pojo).setNumber(Math.E);
      verify(pojo).getParent();
      verify(pojo).setParent(null);
      verifyNoMoreInteractions(pojo);
   }

   @Test
   public void testRemove() {
      Map<String, Object> wrapper = spy(Meta.wrapMap(identityType, pojo));
      String key = Long.toHexString(random.nextLong());
      Object previous = mock(Object.class);
      
      doReturn(previous).when(wrapper).put(key, null);
      
      assertEquals(previous, wrapper.remove(key)); //test
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testPutAll() {
      Map<String, Object> wrapper = spy(Meta.wrapMap(identityType, pojo));
      Map<String, Object> map = new HashMap<>();
      map.put("TEXT", Long.toHexString(random.nextLong()));
      map.put("COUNT", Integer.MIN_VALUE);
      map.put("NUMBER", null);
      map.put("NO KEY", random.nextDouble());
      map.put("PRIMITIVE", Math.E);
      
      Object previous = mock(Object.class);
      
      doReturn(previous).when(wrapper).put(anyString(), any());
      
      wrapper.putAll(map); //test
      
      verify(wrapper).put("TEXT", map.get("TEXT"));
      verify(wrapper).put("COUNT", map.get("COUNT"));
      verify(wrapper).put("NUMBER", map.get("NUMBER"));
      verify(wrapper).put("NO KEY", map.get("NO KEY"));
      verify(wrapper).put("PRIMITIVE", map.get("PRIMITIVE"));
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testClear() {
      Map<String, Object> wrapper = Meta.wrapMap(identityType, pojo);

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
   @SuppressWarnings("unchecked")
   public void testKeySet() {
      Map<String, Object> wrapper = spy(Meta.wrapMap(identityType, pojo));
      
      Set<String> result = wrapper.keySet(); //test
      
      assertTrue(result.containsAll(identityType.getPropertyNames()));
      assertTrue(identityType.getPropertyNames().containsAll(result));
      
      {
         assertThrows(UnsupportedOperationException.class, () -> result.add("Any String"));
      }
      
      {
         doNothing().when(wrapper).clear();
         
         result.clear(); //test clear map write through
         
         verify(wrapper).clear();
         reset(wrapper);
      }
      
      {
         assertFalse(result.remove("Unknow Element")); //test if set contains entry
         
         String text = Long.toHexString(random.nextLong());
         
         doReturn(text).doReturn(null).when(wrapper).remove("TEXT");
         assertTrue(result.remove("TEXT")); // test remove map write through
         assertFalse(result.remove("TEXT")); // test remove map write through
         reset(wrapper);
      }
      
      verifyZeroInteractions(pojo);
   }

   @Test
   public void testValues() {
      Map<String, Object> wrapper = Meta.wrapMap(identityType, pojo);

      Collection<Object> result = wrapper.values(); //test
      
      assertTrue(result instanceof ListWrapper);
      ListWrapper<Pojo> listWrapper = (ListWrapper<Pojo>) result;
      assertEquals(identityType, listWrapper.getType());
      assertEquals(pojo, listWrapper.getInstance());
      verifyZeroInteractions(pojo);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testEntrySet() {
      Map<String, Object> wrapper = spy(Meta.wrapMap(identityType, pojo));
      Iterator<Object> iterator = Meta.wrapIterator(identityType, pojo, 0);

      Set<Entry<String, Object>> result = wrapper.entrySet(); //test
      
      verifyZeroInteractions(pojo);
      assertEquals(identityType.getProperties().size(), result.size());
      
      for (Entry<String, Object> entry : result) {
         assertTrue(entry instanceof MapWrapper.PropertyEntry);
         MapWrapper<Pojo>.PropertyEntry propertyEntry = (MapWrapper<Pojo>.PropertyEntry) entry;
         assertEquals(propertyEntry.property.name, entry.getKey());
         assertEquals(iterator.next(), entry.getValue());
      }
      
      {
         String text = Long.toHexString(random.nextLong());
         Entry<String, Object> entry = new SimpleEntry<>("TEXT", text);
         
         doReturn(text, "TEXT").when(wrapper).put("TEXT", text);
      
         assertFalse(result.add(entry)); // test add map write through
         assertTrue(result.add(entry)); // test add map write through
      
         verify(wrapper, times(2)).put("TEXT", text);
         reset(wrapper);
      }
      
      {
         doNothing().when(wrapper).clear();
         
         result.clear(); //test clear map write through
         
         verify(wrapper).clear();
         reset(wrapper);
      }
      
      {
         Entry<String, Object> entry = new SimpleEntry<>("TEXT", "TEXT");
         
         assertFalse(result.remove(entry)); //test if set contains entry
         
         String text = Long.toHexString(random.nextLong());
         entry = result.iterator().next();
         
         doReturn(text).doReturn(null).when(wrapper).remove(entry.getKey());
         assertTrue(result.remove(entry)); // test remove map write through
         assertFalse(result.remove(entry)); // test remove map write through
         reset(wrapper);
      }
      
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
   public void testGetType() {
      MapWrapper<Pojo> wrapper = new MapWrapper<>(identityType, pojo);
      
      assertEquals(identityType, wrapper.getType()); //test
   }

   @Test
   public void testGetInstance() {
      MapWrapper<Pojo> wrapper = new MapWrapper<>(identityType, pojo);
      
      assertEquals(pojo, wrapper.getInstance()); //test
   }
   
   @Test
   public void testSetInstance() {
      MapWrapper<Pojo> wrapper = new MapWrapper<>(identityType, pojo);
      Pojo mock = mock(Pojo.class);
      wrapper.setInstance(mock);
      
      assertEquals(mock, wrapper.getInstance()); //test
   }

   @AfterClass
   public static void shutDown() {
      verifyZeroInteractions(creator);
   }
}
