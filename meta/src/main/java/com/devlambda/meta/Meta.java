package com.devlambda.meta;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Meta is an utility class that provides static methods which operates on {@link Type}s.
 *
 * @author Dewald Pretorius
 */
public final class Meta {

   private Meta() {}
   
   /**
    * Converts the <code>map</code> instance and returns a newly created <code>converted</code> instance. 
    * 
    * @param <M> meta type
    * @param map instance
    * @param to type
    * @return converted
    */
   public static <M> M convert(Map<String, Object> map, Type<M> to) {
      return convert(map, to, to.creator);
   }
   
   /**
    * Converts the <code>map</code> instance and returns a modified <code>output</code> instance. 
    * 
    * @param <M> meta type
    * @param map instance
    * @param to type
    * @param output instance
    * @return output modified
    */
   public static <M> M convert(Map<String, Object> map, Type<M> to, M output) {
      Supplier<M> supplier = () -> output;
      return convert(map, to, supplier);
   }
   
   /**
    * Converts the <code>map</code> instance and returns a newly created <code>converted</code> instance. 
    * 
    * @param <M> meta type
    * @param map instance
    * @param to type
    * @param supplier to create the return instance
    * @return converted
    */
   public static <M> M convert(Map<String, Object> map, Type<M> to, Supplier<M> supplier) {
      M convert = supplier.get();
      
      for (Property<M, Object> setter : to.getProperties()) {
         Object value = map.get(setter.name);
         if (value != null && !setter.type.isAssignableFrom(value.getClass())) continue;
         setter.set.accept(convert, value);
      }
      
      return convert;
   }
   
   /**
    * Returns the <code>meta</code> instance as a map based on its <code>type</code>.
    * 
    * @param <M> meta type
    * @param type meta
    * @param meta instance
    * @return map
    */
   public static <M> Map<String, Object> convert(Type<M> type, M meta) {
      Map<String, Object> map = new LinkedHashMap<>();
      
      for (Property<M, Object> property : type.properties) {
         map.put(property.name, property.get.apply(meta));
      }
      
      return map;
   }

   /**
    * Converts the <code>meta</code> instance and returns a newly created <code>converted</code> instance. 
    * 
    * @param <G> getter 
    * @param <S> setter
    * @param from meta type
    * @param meta instance
    * @param to meta type
    * @return converted
    */
   public static <G, S> S convert(Type<G> from, G meta, Type<S> to) {
      return convert(from, meta, to, to.creator);
   }

   /**
    * Converts the <code>meta</code> instance and returns a modified <code>output</code> instance. 
    * 
    * @param <G> getter
    * @param <S> setter
    * @param from meta type
    * @param meta instance
    * @param to meta type
    * @param output instance
    * @return output modified
    */
   public static <G, S> S convert(Type<G> from, G meta, Type<S> to, S output) {
      Supplier<S> supplier = () -> output;
      return convert(from, meta, to, supplier);
   }

   /**
    * Converts the <code>meta</code> instance and returns a newly created <code>converted</code> instance. 
    * 
    * @param <G> getter
    * @param <S> setter
    * @param from meta type
    * @param meta instance
    * @param to meta type
    * @param supplier to create the return instance
    * @return converted
    */
   public static <G, S> S convert(Type<G> from, G meta, Type<S> to, Supplier<S> supplier) {
      S convert = supplier.get();

      for (Property<S, Object> setter : to.getProperties()) {
         Property<G, Object> getter = from.getProperty(setter.name, setter.type);

         if (getter == null) {
            getter = from.getProperty(setter.name);
            if (getter == null) continue;
            Object value = getter.get.apply(meta);
            if (value != null && !setter.type.isAssignableFrom(value.getClass())) continue;
         }

         Object value = getter.get.apply(meta);
         setter.set.accept(convert, value);
      }

      return convert;
   }
   
   /**
    * Wraps the instance with a {@link Map} interface. 
    * <p>
    * To access the instance field values the {@link Map#get(Object)} can be used.
    * Changes made via the {@link Map#put(Object, Object)} will invoke the underlying instance's setters.
    * </p>
    * 
    * @param <M> meta type
    * @param type meta
    * @param instance meta
    * @return map
    */
   public static <M> Map<String, Object> wrapMap(Type<M> type, M instance) {
      return new MapWrapper<>(type, instance);
   }
   
   /**
    * Wraps the instance with a {@link List} interface. 
    * <p>
    * To access the instance field values the {@link List#get(int)} can be used.
    * Changes made via the {@link List#set(int, Object)} will invoke the underlying instance's setters.
    * </p>
    * 
    * @param <L> meta type
    * @param type meta
    * @param instance meta
    * @return list
    */
   public static <L> List<Object> wrapList(Type<L> type, L instance) {
      return new ListWrapper<>(type, instance);
   }
   
   /**
    * Wraps the instance with a {@link ListIterator} interface. 
    * 
    * @param <I> meta type
    * @param type meta
    * @param instance meta
    * @return iterator
    */
   public static <I> ListIterator<Object> wrapIterator(Type<I> type, I instance, int index) {
      return new ListIteratorWrapper<>(type, instance, index);
   }
}
