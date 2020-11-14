package com.devlambda.meta;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * Property composing a getter and setter pair.
 *
 * @author Dewald Pretorius
 */
public class Property<M, T> {

   public final String name;
   public final Class<T> type;
   public final Function<M, T> get;
   public final BiConsumer<M, T> set;

   /**
    * Constructs a property.
    * 
    * @param name alias of the property
    * @param type of the getter and setter
    * @param get functional getter
    * @param set consumer setter
    */
   @SuppressWarnings("unchecked")
   public Property(String name, Class<T> type, Function<M, T> get, BiConsumer<M, ? extends T> set) {
      this.name = name;
      this.type = type;
      this.get = get;
      this.set = (BiConsumer<M, T>) set;
   }
   
   @SuppressWarnings({ "unchecked", "rawtypes" })
   static Property meta(Field field) throws IllegalAccessException {
      boolean accessible = field.isAccessible();
      
      try {
         field.setAccessible(true);
      
         MethodHandles.Lookup caller = MethodHandles.lookup();
         MethodHandle getFieldMh = caller.unreflectGetter(field);
         MethodHandle setFieldMh = caller.unreflectSetter(field);
         Function getter = instance -> getter(getFieldMh, instance);
         BiConsumer setter = (instance, value) -> setter(setFieldMh, instance, value);
         
         return new Property(field.getName(), field.getType(), getter, setter);
      } finally {
         field.setAccessible(accessible);
      }
   }
   
   static Object getter(MethodHandle methodHandle, Object instance) {
      try {
         return methodHandle.invoke(instance);
      } catch (Throwable e) {
         throw new RuntimeException("Property getter failed", e);
      }
   }
   
   static void setter(MethodHandle methodHandle, Object instance, Object value) {
      try {
         methodHandle.invoke(instance, value);
      } catch (Throwable e) {
         throw new RuntimeException("Property setter failed", e);
      }
   }
   
   public boolean isAssignableFrom(Class<?> type) {
      if (this.type.isPrimitive()) {
         if (!(Number.class.isAssignableFrom(type)) && 
               !(Boolean.class.isAssignableFrom(type)) && 
               !(Character.class.isAssignableFrom(type))) return false;
         
         return true;
      } 
      
      return this.type.isAssignableFrom(type);
   }
   
   /**
    * Safely invoke the setter to apply the value to the meta instance.
    * 
    * @param meta instance
    * @param value to be set
    * @return true if and only if the value was safely set
    */
   public boolean setSafe(M meta, T value) {
      if (type.isPrimitive()) {
         if (value == null) return false;
         if (!(value instanceof Number) && !(value instanceof Boolean) && !(value instanceof Character)) return false;
      } else if (value != null && !type.isAssignableFrom(value.getClass())) return false;
      
      set.accept(meta, value);
      return true;
   }
}
