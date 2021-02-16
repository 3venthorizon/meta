package com.devlambda.meta;


import static java.util.stream.Collectors.toCollection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * <p>
 * A Type is synonymous with a Java class or interface declaration, except that Types are declared programmatically.
 * The Type instance wraps the generic Meta Java type declaration. A Type contains a {@link Property} list similar to
 * a class containing member fields along with their respective getter and setter methods.
 * </p>
 * <p>
 * Types are easy to create as it implements a builder pattern. 
 * </p>
 * <code>
 * Type&lt;Person&gt; typePerson = Type.meta(Person::new) //wraps a Person class constructor<br>
 * &emsp;//next we add the getter and setter pairs of class Person as named {@link Property}s<br>
 * &emsp;.add("name", String.class, Person::getName, Person::setName) //adds a property called "name" to the Type<br>
 * &emsp;.add("lastName", String.class, Person::getLastName, Person::setLastName) <br>
 * &emsp;.add("birthDate", Date.class, Person::getDateOfBirth, Person::setDateOfBirth)<br>
 * &emsp;.add("contact", Contact.class, Contact::getContactDetails, Contact::setContactDetails);<br>
 * </code>
 * <p>
 * Once a Type is instantiated it can be used to dynamically access Meta type instances. This allows the developer to
 * selectively expose properties via {@link Type#getProperties()} etc whilst honouring the underlying class's data 
 * access pattern.
 * </p>
 * <p>
 * The benefit of having dynamic runtime Types is that it allows you to expose a limited model via the Meta Type as the 
 * rules and logic would demand it. And because a {@link Property} is a named alias to an underlying class's getter and
 * setter pair it makes it easy to convert between these Meta Types where alias names match.
 * </p>
 * 
 * @param <M> Meta
 * @see Morph#convert(Type, Object, Type, Object)
 */
public class Type<M> {

   protected Supplier<M> creator;
   protected List<Property<M, Object>> properties;

   /**
    * Creates a new type.
    * 
    * @param creator of meta instances
    */
   protected Type(Supplier<M> creator) {
      this.creator = creator;
      this.properties = new ArrayList<>();
   }

   /**
    * Instance constructor.
    * 
    * @param <M> metaType
    * @param creator of meta instances
    * @return type
    */
   public static <M> Type<M> meta(Supplier<M> creator) {
      return new Type<>(creator);
   }
   
   /**
    * Creates a Type instance with {@link Properties} which have been reflected from the concrete POJO class's fields.
    * 
    * @param <M> metaType
    * @param creator of meta instances
    * @param concrete class
    * @return type
    */
   @SuppressWarnings("unchecked")
   public static <M> Type<M> meta(Supplier<M> creator, Class<M> concrete) {
      Type<M> type = meta(creator);
      Class<?> inherited = concrete;
      
      while (!Objects.equals(inherited, Object.class)) {
         for (Field field : inherited.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            type.properties.add(Property.meta(field));
         }
         
         inherited = inherited.getSuperclass();
      }
      
      return type;
   }

   /**
    * A builder function to add a new aliased property to this type. 
    * 
    * @param <P> propertyType
    * @param name alias
    * @param type class of property's getter and setter
    * @param get functional getter
    * @param set consumer setter
    * @return this instance
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public <P> Type<M> add(String name, Class<P> type, Function<M, P> get, BiConsumer<M, ? extends P> set) {
      properties.add(new Property(name, type, get, set));
      return this;
   }
   
   /**
    * Adds the collection of properties to the internal list of {@link #properties}.
    *  
    * @param properties collection
    * @return this instance
    */
   public Type<M> addProperties(Collection<Property<M, Object>> properties) {
      properties.addAll(properties);
      return this;
   }

   /**
    * Creates a new meta instance represented by this type.
    *  
    * @return meta
    */
   public M create() {
      return creator.get();
   }

   /**
    * Returns the creator of meta instances.
    * 
    * @return creator of meta instances
    */
   public Supplier<M> getCreator() {
      return creator;
   }

   /**
    * Returns the class which this type wraps.
    * 
    * @return class which this type wraps
    */
   @SuppressWarnings("unchecked")
   public Class<M> getMetaClass() {
      return (Class<M>) creator.get().getClass();
   }

   /**
    * Returns the first property matching the parameter <code>name</code>.
    * 
    * @param name alias of the property
    * @return property
    */
   public Property<M, Object> getProperty(String name) {
      return properties.stream().filter(property -> Objects.equals(name, property.name)).findFirst().orElse(null);
   }
   
   /**
    * Returns the first property matching the parameter <code>name</code>.
    * 
    * @param index zero-based position in the list of properties
    * @return property
    */
   public Property<M, Object> getProperty(int index) {
      return properties.get(index);
   }

   /**
    * Returns the <code>meta</code> class typed property by its overloaded alias <code>name</code>.
    * 
    * @param <T> type
    * @param name of the property
    * @param meta class type
    * @return property
    */
   @SuppressWarnings("unchecked")
   public <T> Property<M, T> getProperty(String name, Class<T> meta) {
      return (Property<M, T>) properties.stream()
            .filter(property -> Objects.equals(name, property.name) && property.isAssignableFrom(meta))
            .findFirst().orElse(null);
   }

   /**
    * Returns the internal list of properties this type contains.
    * 
    * @return properties
    */
   public List<Property<M, Object>> getProperties() {
      return properties;
   }

   /**
    * Returns all the property names.
    * 
    * @return propertyNames
    */
   public LinkedHashSet<String> getPropertyNames() {
      return properties.stream().map(property -> property.name).collect(toCollection(LinkedHashSet::new)); 
   }

   /**
    * Returns the properties grouped by class type.
    * 
    * @return classPropertiesMap
    */
   public Map<Class<?>, List<Property<M, Object>>> mapTypedProperties() {
      return properties.stream().collect(Collectors.groupingBy(Property::getClass));
   }
}
