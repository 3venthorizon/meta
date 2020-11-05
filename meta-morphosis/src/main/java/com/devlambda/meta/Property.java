package com.devlambda.meta;


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
