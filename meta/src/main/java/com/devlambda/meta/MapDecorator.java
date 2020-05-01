package com.devlambda.meta;


import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @param <K> key
 * @param <V> value

 * @author Dewald Pretorius
 */
public class MapDecorator<K, V> implements Map<K, V> {
   
   protected final Map<K, V> decorated;

   public MapDecorator(Map<K, V> decorated) {
      this.decorated = decorated;
   }

   @Override
   public int size() {
      return decorated.size();
   }

   @Override
   public boolean isEmpty() {
      return decorated.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return decorated.containsKey(key);
   }

   @Override
   public boolean containsValue(Object value) {
      return decorated.containsValue(value);
   }
   
   @Override
   public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return decorated.compute(key, remappingFunction);
   }
   
   @Override
   public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      return decorated.computeIfAbsent(key, mappingFunction);
   }
   
   @Override
   public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return decorated.computeIfPresent(key, remappingFunction);
   }
   
   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      decorated.forEach(action);
   }

   @Override
   public V get(Object key) { 
      return decorated.get(key);
   }
   
   @Override
   public V getOrDefault(Object key, V defaultValue) { 
      return decorated.getOrDefault(key, defaultValue);
   }
   
   @Override
   public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      return decorated.merge(key, value, remappingFunction);
   }

   @Override
   public V put(K key, V value) {
      return decorated.put(key, value);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> map) {
      decorated.putAll(map);
   }
   
   @Override
   public V putIfAbsent(K key, V value) { 
      return decorated.putIfAbsent(key, value);
   }

   @Override
   public void clear() {
      decorated.clear();
   }

   @Override
   public V remove(Object key) {
      return decorated.remove(key);
   }
   
   @Override
   public boolean remove(Object key, Object value) {
      return decorated.remove(key, value);
   }
   
   @Override
   public V replace(K key, V value) {
      return decorated.replace(key, value);
   }
   
   @Override
   public boolean replace(K key, V oldValue, V newValue) {
      return decorated.replace(key, oldValue, newValue);
   }
   
   @Override
   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      decorated.replaceAll(function);
   }

   @Override
   public Set<K> keySet() {
      return decorated.keySet();
   }

   @Override
   public Collection<V> values() {
      return decorated.values();
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      return decorated.entrySet();
   }
}
