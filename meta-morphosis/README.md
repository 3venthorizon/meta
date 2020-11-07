# Meta Types & Properties
This API provides a type safe runtime descriptor for data access to objects.

## Java Class Restrictions
In Java, classes and their methods are used to access encapsulated data fields. Once a class is compiled it cannot be 
changed during runtime. Thus the structure and access methods for data is predefined and will only change once the
source code is updated and recompiled. However, Java does provide class abstraction and interfaces to provide 
polomorphic access during runtime. But still these abstractions and interfaces are predefined in source code.

## Runtime Meta Class and Property Wrappers
While classes are predefined at the time it was compiled, this API has the ability to define data structures and their 
access methods dynamically during runtime.

In order to preserve the good practices of object orientated data encapsulation and strong types, this API provides 
runtime [Type](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Type.java) and 
[Property](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Property.java) 
wrappers for classes and their methods respectively.

### Type
A Type is synonymous with a Java class or interface declaration, except that Types are declared programmatically.The 
Type instance wraps the generic Meta Java type declaration. A Type contains a Property list similar to a class 
containing member fields along with their respective getter and setter methods. 

Types are easy to create as it implements a builder pattern.

```java 
Type<Person> typePerson = Type.meta(Person::new) //wraps a Person class constructor
 //next we add the getter and setter pairs of class Person as named Properties
 .add("name", String.class, Person::getName, Person::setName) //adds a property called "name"
 .add("lastName", String.class, Person::getLastName, Person::setLastName) 
 .add("birthDate", Date.class, Person::getDateOfBirth, Person::setDateOfBirth)
 .add("contact", Contact.class, Contact::getContactDetails, Contact::setContactDetails);
```

Once a Type is instantiated it can be used to dynamically access Meta type instances. This allows the developer 
to selectively expose properties via 
[Type](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Type.java).getProperties()
etc whilst honouring the underlying class's data access pattern. 

The benefit of having dynamic runtime 
[Type](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Type.java)s is that it
allows you to expose a limited model via the Meta 
[Type](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Type.java) as the rules
and logic would demand it. And because a
[Property](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Property.java) is a
named alias to an underlying class's getter and setter pair it makes it easy to convert between these Meta
[Type](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Type.java)s where alias
names match.

### Collections Interface Wrappers
Convenience interface wrappers such as java.util.Map, java.util.List, and java.util.ListIterator allow developers
to access Java object instances when configuring 
[Type](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Type.java)s for Java 
classes. Depending on the access pattern strategy you want to implement the 
[Meta](https://github.com/3venthorizon/meta/blob/master/meta-morphosis/src/main/java/com/devlambda/meta/Morph.java).wrapXXX methods
will wrap plain old Java object instances with the respectively named interface.

It is important to note that these Java Collections interface wrappers do not copy or transform instance data from
the plain old java object instances. Instead Map and Collection method accessors and operations are delegated to tightly 
wrapped getter and setter functional interfaces of the Java class definition.

```java
public void mapPersonExample(Type<Person> typePerson, Person person) {
   person.setName("Leon");
   Map<String, Object> mapPerson = Morph.wrapMap(typePerson, person); 
   String oldName = mapPerson.put("name", "Dewald"); //returns "Leon"
   System.out.println("Person renamed from " + oldName + " to " + mapPerson.get("name"));
}

public void listPersonExample(Type<Person> typePerson, Person person) {
   person.setName("Leon");
   List<Object> listPerson = Morph.wrapList(typePerson, person); 
   String oldName = listPerson.set(0, "Dewald"); //returns "Leon"
   System.out.println("Person renamed from " + oldName + " to " + listPerson.get(0));
}

public void iteratorPersonExample(Type<Person> typePerson, Person person) {
   person.setName("Leon");
   Iterator<Object> iteratorPerson = Morph.wrapIterator(typePerson, person); 
   Object oldValue = iteratorPerson.next(); //returns "Leon"
   iteratorPerson.set("Dewald");
   System.out.println("Person renamed from " + oldValue + " to " + iteratorPerson.previous());
   
   System.out.print("Person field values [");
   while(iteratorPerson.hasNext()) {
      System.out.print(iteratorPerson.next());
      System.out.print(", ");
   }
   System.out.println("]");
}
```