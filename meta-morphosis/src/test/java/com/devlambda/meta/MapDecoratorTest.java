package com.devlambda.meta;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * Unit Test {@link MapDecorator}.
 * 
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class MapDecoratorTest {
   protected @Mock Map<Long, String> map;
   protected Random random = new Random();
   
   private MapDecorator<Long, String> decorator;
   
   @Before
   public void setUp() throws Exception {
      decorator = spy(new MapDecorator<>(map));
      assertEquals(map, decorator.decorated);
   }

   @Test
   public void testSize() {
      int size = random.nextInt();
      
      when(map.size()).thenReturn(size);
      
      assertEquals(size, decorator.size()); //test
      
      verify(map).size();
   }

   @Test
   public void testIsEmpty() {
      boolean empty = random.nextBoolean();
      
      when(map.isEmpty()).thenReturn(empty);
      
      assertEquals(empty, decorator.isEmpty()); //test
      
      verify(map).isEmpty();
   }

   @Test
   public void testContainsKey() {
      boolean contains = random.nextBoolean();
      Long key = random.nextLong();
      
      when(map.containsKey(key)).thenReturn(contains);
      
      assertEquals(contains, decorator.containsKey(key)); //test
      
      verify(map).containsKey(key);
   }

   @Test
   public void testContainsValue() {
      boolean contains = random.nextBoolean();
      String value = Long.toHexString(random.nextLong());
      
      when(map.containsValue(value)).thenReturn(contains);
      
      assertEquals(contains, decorator.containsValue(value)); //test
      
      verify(map).containsValue(value);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testCompute() {
      Long key = random.nextLong();
      BiFunction<Long, String, String> remappingFunction = mock(BiFunction.class);
      String value = Long.toHexString(random.nextLong());
      
      when(map.compute(key, remappingFunction)).thenReturn(value);
      
      assertEquals(value, decorator.compute(key, remappingFunction)); //test
      
      verify(map).compute(key, remappingFunction);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testComputeIfAbsent() {
      Long key = random.nextLong();
      Function<Long, String> mappingFunction = mock(Function.class);
      String value = Long.toHexString(random.nextLong());
      
      when(map.computeIfAbsent(key, mappingFunction)).thenReturn(value);
      
      assertEquals(value, decorator.computeIfAbsent(key, mappingFunction)); //test
      
      verify(map).computeIfAbsent(key, mappingFunction);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testComputeIfPresent() {
      Long key = random.nextLong();
      BiFunction<Long, String, String> remappingFunction = mock(BiFunction.class);
      String value = Long.toHexString(random.nextLong());
      
      when(map.computeIfPresent(key, remappingFunction)).thenReturn(value);
      
      assertEquals(value, decorator.computeIfPresent(key, remappingFunction)); //test
      
      verify(map).computeIfPresent(key, remappingFunction);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testForEach() {
      BiConsumer<Long, String> action = mock(BiConsumer.class);
      
      decorator.forEach(action);
      
      verify(map).forEach(action);
   }

   @Test
   public void testGet() {
      Long key = random.nextLong();
      String value = Long.toHexString(random.nextLong());
      
      when(map.get(key)).thenReturn(value);
      
      assertEquals(value, decorator.get(key)); //test
      
      verify(map).get(key);
   }

   @Test
   public void testGetOrDefault() {
      Long key = random.nextLong();
      String value = Long.toHexString(random.nextLong());
      String defaultValue = Long.toHexString(random.nextLong());
      
      when(map.getOrDefault(key, defaultValue)).thenReturn(value);
      
      assertEquals(value, decorator.getOrDefault(key, defaultValue)); //test
      
      verify(map).getOrDefault(key, defaultValue);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testMerge() {
      Long key = random.nextLong();
      String value = Long.toHexString(random.nextLong());
      String expected = Long.toHexString(random.nextLong());
      BiFunction<String, String, String> remappingFunction = mock(BiFunction.class);
      
      when(map.merge(key, value, remappingFunction)).thenReturn(expected);
      
      assertEquals(expected, decorator.merge(key, value, remappingFunction)); //test
      
      verify(map).merge(key, value, remappingFunction);
   }

   @Test
   public void testPut() {
      Long key = random.nextLong();
      String value = Long.toHexString(random.nextLong());
      String expected = Long.toHexString(random.nextLong());
      
      when(map.put(key, value)).thenReturn(expected);
      
      assertEquals(expected, decorator.put(key, value)); //test
      
      verify(map).put(key, value);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testPutAll() {
      Map<Long, String> map = mock(Map.class);

      decorator.putAll(map); //test
      
      verify(this.map).putAll(map);
   }

   @Test
   public void testPutIfAbsent() {
      Long key = random.nextLong();
      String value = Long.toHexString(random.nextLong());
      String expected = Long.toHexString(random.nextLong());
      
      when(map.putIfAbsent(key, value)).thenReturn(expected);
      
      assertEquals(expected, decorator.putIfAbsent(key, value)); //test
      
      verify(map).putIfAbsent(key, value);
   }

   @Test
   public void testClear() {
      decorator.clear(); //test
      
      verify(map).clear();
   }

   @Test
   public void testRemoveKey() {
      Long key = random.nextLong();
      String value = Long.toHexString(random.nextLong());
      
      when(map.remove(key)).thenReturn(value);
      
      assertEquals(value, decorator.remove(key)); //test
      
      verify(map).remove(key);
   }

   @Test
   public void testRemoveKeyValue() {
      boolean removed = random.nextBoolean();
      Long key = random.nextLong();
      String value = Long.toHexString(random.nextLong());
      
      when(map.remove(key, value)).thenReturn(removed);
      
      assertEquals(removed, decorator.remove(key, value)); //test
      
      verify(map).remove(key, value);
   }

   @Test
   public void testReplaceKeyValue() {
      Long key = random.nextLong();
      String value = Long.toHexString(random.nextLong());
      String previous = Long.toHexString(random.nextLong());
      
      when(map.replace(key, value)).thenReturn(previous);
      
      assertEquals(previous, decorator.replace(key, value)); //test
      
      verify(map).replace(key, value);
   }

   @Test
   public void testReplaceKeyNewOldValue() {
      boolean replaced = random.nextBoolean();
      Long key = random.nextLong();
      String oldValue = Long.toHexString(random.nextLong());
      String newValue = Long.toHexString(random.nextLong());
      
      when(map.replace(key, oldValue, newValue)).thenReturn(replaced);
      
      assertEquals(replaced, decorator.replace(key, oldValue, newValue)); //test
      
      verify(map).replace(key, oldValue, newValue);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testReplaceAll() {
      BiFunction<Long, String, String> function = mock(BiFunction.class);
      
      decorator.replaceAll(function); //test
      
      verify(map).replaceAll(function);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testKeySet() {
      Set<Long> keySet = mock(Set.class);
      
      when(map.keySet()).thenReturn(keySet);
      
      assertEquals(keySet, decorator.keySet()); //test
      
      verify(map).keySet();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testValues() {
      List<String> values = mock(List.class);
      
      when(map.values()).thenReturn(values);
      
      assertEquals(values, decorator.values()); //test
      
      verify(map).values();
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testEntrySet() {
      Set<Entry<Long, String>> entrySet = mock(Set.class);

      when(map.entrySet()).thenReturn(entrySet);
      
      assertEquals(entrySet, decorator.entrySet()); //test
      
      verify(map).entrySet();
   }
}
