package com.devlambda.meta.persistence;


import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.devlambda.meta.Type;


public class MpaIT {

   interface MapPerson {
      Map<String, Object> map();
      void setMap(Map<String, Object> map);

      default Long getId() { return (Long) map().get("ID"); }
      default void setId(Long id) { map().put("ID", id); }

      default String getName() { return (String) map().get("NAME"); }
      default void setName(String name) { map().put("NAME", name); }

      default Date getDateOfBirth() { return (Date) map().get("DOB"); }
      default void setDateOfBirth(Date dateOfBirth) { map().put("DOB", dateOfBirth); }

      default String getEmail() { return (String) map().get("EMAIL"); }
      default void setEmail(String email) { map().put("EMAIL", email); }

      default String getMobile() { return (String) map().get("MOBILE"); }
      default void setMobile(String mobile) { map().put("MOBILE", mobile); }

      default BigDecimal getMoney() { return (BigDecimal) map().get("MONEY"); }
      default void setMoney(BigDecimal money) { map().put("MONEY", money); }

      default Float getHeight() { return (Float) map().get("HEIGHT"); }
      default void setHeight(Float height) { map().put("HEIGHT", height); }

      default Double getRatio() { return (Double) map().get("RATIO"); }
      default void setRatio(Double ratio) { map().put("RATIO", ratio); }

      default Boolean hasMatric() { return (Boolean) map().get("MATRIC"); }
      default void setMatric(Boolean matric) { map().put("MATRIC", matric); }
   }

   static class Person implements MapPerson {

      Map<String, Object> map;

      Person(Map<String, Object> map) {
         setMap(map);
      }

      @Override
      public Map<String, Object> map() { return map; }

      @Override
      public void setMap(Map<String, Object> map) { this.map = map; }
   }

   static final Type<Map<String, Object>> TYPE_MAP = Type.meta(HashMap::new);
   static final Type<MapPerson> TYPE_INTERFACE = Type.meta(() -> new Person(new HashMap<>()));

   static final String SQL_CREATE_TABLE = 
         "CREATE TABLE IF NOT EXISTS PERSON(" + 
               "ID BIGINT NOT NULL AUTO_INCREMENT, " + 
               "NAME VARCHAR(200) NOT NULL, " + 
               "DOB DATETIME NOT NULL, " + 
               "EMAIL VARCHAR(200), " + 
               "MOBILE VARCHAR(30), " + 
               "MONEY DECIMAL(30,2), " + 
               "HEIGHT NUMBER(3,2), " + 
               "RATIO DOUBLE, " +
               "MATRIC BOOL, " + 
               "CONSTRAINT PK_PERSON PRIMARY KEY(ID)" +
               ")";

   static Connection connection;
   static MPA mpa;

   Map<String, Object> personMap;
   MapPerson mapPerson;

   @BeforeClass
   public static void bootUp() throws Exception {
      TYPE_MAP.add("ID", Long.class, map -> (Long) map.get("ID"), (map, value) -> map.put("ID", value))
         .add("NAME", String.class, map -> (String) map.get("NAME"), (map, value) -> map.put("NAME", value))
         .add("DOB", Date.class, map -> (Date) map.get("DOB"), (map, value) -> map.put("DOB", value))
         .add("EMAIL", String.class, map -> (String) map.get("EMAIL"), (map, value) -> map.put("EMAIL", value))
         .add("MOBILE", String.class, map -> (String) map.get("MOBILE"), (map, value) -> map.put("MOBILE", value))
         .add("MONEY", BigDecimal.class, map -> (BigDecimal) map.get("MONEY"), (map, value) -> map.put("MONEY", value))
         .add("HEIGHT", Float.class, map -> (Float) map.get("HEIGHT"), (map, value) -> map.put("HEIGHT", value))
         .add("RATIO", Double.class, map -> (Double) map.get("RATIO"), (map, value) -> map.put("RATIO", value))
         .add("MATRIC", Boolean.class, map -> (Boolean) map.get("MATRIC"), (map, value) -> map.put("MATRIC", value));
      TYPE_INTERFACE
         .add("ID", Long.class, MapPerson::getId, MapPerson::setId)
         .add("NAME", String.class, MapPerson::getName, MapPerson::setName)
         .add("DOB", Date.class, MapPerson::getDateOfBirth, MapPerson::setDateOfBirth)
         .add("EMAIL", String.class, MapPerson::getEmail, MapPerson::setEmail)
         .add("MOBILE", String.class, MapPerson::getMobile, MapPerson::setMobile)
         .add("MONEY", BigDecimal.class, MapPerson::getMoney, MapPerson::setMoney)
         .add("HEIGHT", Float.class, MapPerson::getHeight, MapPerson::setHeight)
         .add("RATIO", Double.class, MapPerson::getRatio, MapPerson::setRatio)
         .add("MATRIC", Boolean.class, MapPerson::hasMatric, MapPerson::setMatric);
      connection = DriverManager.getConnection("jdbc:h2:mem:metarepo");

      connection.createStatement().execute(SQL_CREATE_TABLE);
      mpa = new MPA(() -> connection);
      mpa.register(TYPE_MAP, "PERSON", Statement.RETURN_GENERATED_KEYS);
      mpa.register(TYPE_INTERFACE, "PERSON", Statement.RETURN_GENERATED_KEYS);
   }

   @Before
   public void setUp() throws Exception {
      personMap = new HashMap<>();
      personMap.put("NAME", "Dewald Pretorius");
      Calendar calendar = Calendar.getInstance();
      calendar.set(1982, Calendar.AUGUST, 3, 23, 55, 00);
      calendar.set(Calendar.MILLISECOND, 0);
      personMap.put("DOB", calendar.getTime());
      personMap.put("EMAIL", "pretorius.dewald@gmail.com");
      personMap.put("MOBILE", "+27 82 ### ####");
      personMap.put("MONEY", new BigDecimal("-987123.56"));
      personMap.put("HEIGHT", 1.78f);
      personMap.put("RATIO", Math.PI);
      personMap.put("MATRIC", true);
      assertNull(personMap.get("ID"));

      mapPerson = new Person(personMap);
   }

   @Test
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public void testMap() {
      Map<String, Object> persisted = mpa.persist(personMap); //test

      assertNotNull(persisted.get("ID"));

      Optional<Map> foundById = mpa.findById(Map.class, persisted.get("ID")); //test
      List<Map> findAll = mpa.findAll(Map.class); //test

      assertTrue(foundById.isPresent());
      assertEquals(1, findAll.size());

      Map<String, Object> findById = foundById.get();
      Map<String, Object> allButOne = findAll.get(0);

      assertFalse(persisted == findById);
      assertEquals(persisted.size(), findById.size());
      assertEquals(persisted.size(), allButOne.size());

      for (Entry<String, Object> entry : persisted.entrySet()) {
         assertEquals(entry.getValue(), findById.get(entry.getKey()));
         assertEquals(entry.getValue(), allButOne.get(entry.getKey()));
      }

      Map<String, Object> update = new HashMap<>(findById);
      update.put("NAME", "Darth Maul");

      Map<String, Object> merged = mpa.merge(update); //test

      assertNotNull(merged);
      assertFalse(update == merged);
      assertEquals(merged.size(), update.size());
      assertEquals("Darth Maul", merged.get("NAME"));

      for (Entry<String, Object> entry : update.entrySet()) {
         assertEquals(entry.getValue(), merged.get(entry.getKey()));
      }

      Map<String, Object> delete = new HashMap<>();
      delete.put("ID", merged.get("ID"));

      Map<String, Object> removed = mpa.remove(delete); //test

      assertNotNull(removed);
      assertFalse(removed == merged);
      assertEquals(merged.size(), removed.size());

      for (Entry<String, Object> entry : merged.entrySet()) {
         assertEquals(entry.getValue(), removed.get(entry.getKey()));
      }

      foundById = mpa.findById(Map.class, persisted.get("ID")); //test

      assertFalse(foundById.isPresent());
   }

   @Test
   public void testInterface() {
      MapPerson persisted = mpa.persist(mapPerson); //test

      assertNotNull(persisted.getId());

      Optional<MapPerson> foundById = mpa.findById(MapPerson.class, persisted.getId()); //test
      List<MapPerson> findAll = mpa.findAll(MapPerson.class); //test

      assertTrue(foundById.isPresent());
      assertEquals(1, findAll.size());

      MapPerson findById = foundById.get();
      MapPerson allButOne = findAll.get(0);

      assertFalse(persisted == findById);
      assertEquals(persisted.map().size(), findById.map().size());
      assertEquals(persisted.map().size(), allButOne.map().size());

      for (Entry<String, Object> entry : persisted.map().entrySet()) {
         assertEquals(entry.getValue(), findById.map().get(entry.getKey()));
         assertEquals(entry.getValue(), allButOne.map().get(entry.getKey()));
      }

      Person update = new Person(new HashMap<>(findById.map()));
      update.setName("Darth Maul");

      MapPerson merged = mpa.merge(update); //test

      assertNotNull(merged);
      assertFalse(update == merged);
      assertEquals(merged.map().size(), update.map().size());
      assertEquals("Darth Maul", merged.getName());

      for (Entry<String, Object> entry : update.map().entrySet()) {
         assertEquals(entry.getValue(), merged.map().get(entry.getKey()));
      }

      MapPerson delete = new Person(new HashMap<>());
      delete.setId(merged.getId());

      MapPerson removed = mpa.remove(delete); //test

      assertNotNull(removed);
      assertFalse(removed == merged);
      assertEquals(merged.map().size(), removed.map().size());

      for (Entry<String, Object> entry : merged.map().entrySet()) {
         assertEquals(entry.getValue(), removed.map().get(entry.getKey()));
      }

      foundById = mpa.findById(MapPerson.class, persisted.getId()); //test

      assertFalse(foundById.isPresent());
   }

   @AfterClass
   public static void shutDown() throws Exception {
      connection.close();
   }
}
