package com.devlambda.meta.persistence;


import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;


/**
 * @author Dewald Pretorius
 */
public class GraphResult {

   @SuppressWarnings("rawtypes")
   protected final LinkedHashMap<String, LinkedHashSet> propertyMap = new LinkedHashMap<>();
   
   protected GraphResult() { }
   
   public void addProperty(String name) {
      propertyMap.computeIfAbsent(name, key -> new LinkedHashSet<>());
   }
   
   @SuppressWarnings("unchecked")
   public <T> LinkedHashSet<T> getResults(String name, Class<T> type) {
      return (LinkedHashSet<T>) propertyMap.get(name);
   }
   
   /**
    * Adds this element to this graph's named result set only if it doesn't contain an {@link Object#equals(Object)}
    * reference.
    * 
    * @param name of the result set
    * @param value element
    * @return element value or existing element reference
    */
   @SuppressWarnings("unchecked")
   public <T> T addElement(String name, T value) {
      LinkedHashSet<T> results = (LinkedHashSet<T>) getResults(name, value.getClass());
      
      synchronized (results) {
         Optional<T> existing = results.stream().filter(result -> Objects.equals(result, value)).findFirst();
         if (!existing.isPresent()) results.add(value);
         return existing.orElse(value);
      }
   }
}
