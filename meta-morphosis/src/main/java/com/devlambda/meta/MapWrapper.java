package com.devlambda.meta;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Wraps the instance with a {@link Map} interface. 
 * <p>
 * To access the instance field values the {@link Map#get(Object)} can be used.
 * Changes made via the {@link Map#put(Object, Object)} will invoke the underlying instance's setters.
 * </p>
 * 
 * @param <M> meta type
 * 
 * @author Dewald Pretorius
 */
public class MapWrapper<M> implements Map<String, Object> {
   
   protected class PropertyEntry implements Entry<String, Object> {
      protected final Property<M, Object> property;
      
      public PropertyEntry(Property<M, Object> property) {
         this.property = property;
      }

      @Override
      public String getKey() {
         return property.name;
      }

      @Override
      public Object getValue() {
         return property.get.apply(instance);
      }

      @Override
      public Object setValue(Object value) { 
         Object previous = property.get.apply(instance);
         property.setSafe(instance, value);
         
         return previous;
      }
   }
   
   protected class IdentitySet extends SetDecorator<String> {
      public IdentitySet() { 
         super(type.getPropertyNames());
      }
      
      @Override
      public boolean add(String element) { throw new UnsupportedOperationException(); }
      
      @Override
      public void clear() { 
         MapWrapper.this.clear();
      }
      
      @Override
      public boolean remove(Object object) {
         if (!contains(object)) return false;
         return MapWrapper.this.remove(object) != null;
      }
   }
   
   protected class PropertyEntrySet extends SetDecorator<Entry<String, Object>> {
      public PropertyEntrySet() {
         super(new LinkedHashSet<>());
         type.properties.stream().map(PropertyEntry::new).forEach(decorated::add);
      }
      
      @Override
      public boolean add(Entry<String, Object> element) {
         return !Objects.equals(MapWrapper.this.put(element.getKey(), element.getValue()), element.getValue());
      }
      
      @Override
      public void clear() { 
         MapWrapper.this.clear();
      }
      
      @Override
      @SuppressWarnings("unchecked")
      public boolean remove(Object object) {
         if (!contains(object)) return false;
         Entry<String, Object> entry = (Entry<String, Object>) object;
         return MapWrapper.this.remove(entry.getKey()) != null;
      }
   }
   
   protected final Type<M> type;
   protected M instance;

   public MapWrapper(Type<M> type, M instance) {
      this.type = type;
      this.instance = instance;
   }

   @Override
   public int size() { 
      return type.getPropertyNames().size();
   }

   @Override
   public boolean isEmpty() { 
      return type.properties.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) { 
      return type.properties.stream().anyMatch(property -> Objects.equals(property.name, key));
   }

   @Override
   public boolean containsValue(Object value) {
      return type.properties.stream().map(property -> property.get.apply(instance))
                                     .anyMatch(field -> Objects.equals(field, value));
   }

   @Override
   public Object get(Object key) { 
      Property<M, Object> property = type.getProperty(key.toString());
      if (property == null) return null;
      return property.get.apply(instance);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Object put(String key, Object value) { 
      Property<M, Object> property;
      if (value == null) property = (Property<M, Object>) type.getProperty(key.toString());
      else property = (Property<M, Object>) type.getProperty(key.toString(), value.getClass());
      if (property == null) return null;
      
      Object previous = property.get.apply(instance);
      property.setSafe(instance, value);
      return previous;
   }

   @Override
   public void putAll(Map<? extends String, ? extends Object> map) { 
      for (Entry<? extends String, ? extends Object> entry : map.entrySet()) {
         put(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public void clear() { 
      type.properties.stream().forEach(property -> property.setSafe(instance, null));
   }

   /**
    * Delegates to {@link #put(Object, Object)} with null as the value.
    */
   @Override
   public Object remove(Object key) { 
      return put(key.toString(), null);
   }

   @Override
   public Set<String> keySet() { 
      return new IdentitySet();
   }

   @Override
   public Collection<Object> values() { 
      return new ListWrapper<>(type, instance);
   }

   @Override
   public Set<Entry<String, Object>> entrySet() { 
      return new PropertyEntrySet();
   }

   public Type<M> getType() { return type; }

   public M getInstance() { return instance; }
   public void setInstance(M instance) { this.instance = instance; }
}
