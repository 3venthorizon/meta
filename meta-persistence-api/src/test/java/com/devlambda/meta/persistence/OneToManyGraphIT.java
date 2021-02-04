package com.devlambda.meta.persistence;


import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devlambda.meta.Type;
import com.devlambda.meta.persistence.domain.location.City;
import com.devlambda.meta.persistence.domain.location.Country;


/**
 * One to Many Graph example using the {@link Country} to {@link City} relationship.
 * 
 * @author Dewald Pretorius
 */
public class OneToManyGraphIT {

   static final String JDBC_URL = "jdbc:h2:mem:metarepo;DB_CLOSE_DELAY=-1";
   static final String SQL_CREATE_COUNTRY = 
         "CREATE TABLE IF NOT EXISTS COUNTRY(" + 
            "ID INT NOT NULL AUTO_INCREMENT, " + 
            "NAME VARCHAR(100) NOT NULL, " + 
            "CONSTRAINT PK_COUNTRY PRIMARY KEY(ID))";
   static final String SQL_CREATE_CITY = 
         "CREATE TABLE IF NOT EXISTS CITY(" + 
            "ID BIGINT NOT NULL AUTO_INCREMENT, " + 
            "COUNTRY_ID INT NOT NULL, " + 
            "NAME VARCHAR(100) NOT NULL, " + 
            "CONSTRAINT PK_CITY PRIMARY KEY(ID), " +
            "CONSTRAINT FK_CITY_COUNTRY FOREIGN KEY(COUNTRY_ID) REFERENCES COUNTRY(ID) " + 
               "ON UPDATE CASCADE ON DELETE RESTRICT)";

   static final Type<Country> TYPE_COUNTRY = Type.meta(Country::new);
   static final Type<City> TYPE_CITY = Type.meta(City::new);
   
   static MPA mpa;
   
   @BeforeClass
   public static void bootUp() throws Exception {
      setupMetaTypes();
      setupMPA();
      setupData();
   }
   
   /**
    * 
    */
   private static void setupMetaTypes() {
      TYPE_COUNTRY.add("ID", Integer.class, Country::getId, Country::setId)
                  .add("NAME", String.class, Country::getName, Country::setName);
      TYPE_CITY.add("ID", Long.class, City::getId, City::setId)
               .add("NAME", String.class, City::getName, City::setName)
               .add("COUNTRY_ID", Integer.class, City::getCountryId, City::setCountryId);
   }
   
   private static void setupMPA() throws Exception {
      mpa = new MPA(Connector.connectionProvider(JDBC_URL));
      
      try (Statement countryStatement = mpa.connection().createStatement();
           Statement cityStatement = mpa.connection().createStatement();) {
         countryStatement.execute(SQL_CREATE_COUNTRY);
         cityStatement.execute(SQL_CREATE_CITY);
      }
   }

   private static void setupData() throws Exception {
      mpa.register(TYPE_COUNTRY, Statement.RETURN_GENERATED_KEYS);
      mpa.register(TYPE_CITY, Statement.RETURN_GENERATED_KEYS);
      mpa.begin();
      
      { //South Africa
         Country country = mpa.persist(new Country("South Africa"));
         
         mpa.persist(new City(country, "Cape Town"));
         mpa.persist(new City(country, "Kimberley"));
         mpa.persist(new City(country, "Port Elizabeth"));
         mpa.persist(new City(country, "Durban"));
         mpa.persist(new City(country, "Bloemfontein"));
         mpa.persist(new City(country, "Mahikeng"));
         mpa.persist(new City(country, "Polokwane"));
         mpa.persist(new City(country, "Nelspruit"));
         mpa.persist(new City(country, "Pretoria"));
         mpa.persist(new City(country, "Johannesburg"));
      }
      
      { //Namibia
         Country country = mpa.persist(new Country("Namibia"));
         
         mpa.persist(new City(country, "Windhoek"));
         mpa.persist(new City(country, "Swakopmund"));
         mpa.persist(new City(country, "Luderitz"));
         mpa.persist(new City(country, "Keetmanshoop"));
      }
      
      mpa.commit();
   }
   
   @Test
   public void testGetCountries() {
      List<Country> countries = mpa.findAll(Country.class);
      
      assertFalse(countries.isEmpty());
      assertTrue(countries.stream().allMatch(country -> country.getCities().isEmpty()));
   }
   
   @Test
   public void testGetCities() {
      List<City> cities = mpa.findAll(City.class);
      
      assertFalse(cities.isEmpty());
      assertTrue(cities.stream().allMatch(city -> city.getCountry() == null));
   }
   
   @Test
   public void testGraphQuery() throws Exception {
      //setup graph
      GraphBuilder builder = GraphBuilder.builder();
      builder.mapTypeProperty(0, "ID", TYPE_COUNTRY, "country");
      builder.mapTypeProperty(1, "NAME", TYPE_COUNTRY, "country");
      BiConsumer<Country, City> cityAdder = (country, city) -> country.getCities().add(city);
      builder.mapGraphProperty("city", cityAdder, TYPE_COUNTRY, "country");
      
      builder.mapTypeProperty(2, "ID", TYPE_CITY, "city");
      builder.mapTypeProperty(3, "NAME", TYPE_CITY, "city");
      builder.mapTypeProperty(0, "COUNTRY_ID", TYPE_CITY, "city");
      builder.mapGraphProperty("country", City::setCountry, TYPE_CITY, "city");
      
      //prepare sql query
      final String SQL_QUERY = 
            SQL.select("", "a.id AS country_id", "a.name AS country_name", "b.id AS city_id", "b.name AS city_name")
            + "FROM COUNTRY AS a "
            + "INNER JOIN CITY AS b ON b.COUNTRY_ID = a.ID "
            + "WHERE a.NAME = ?";
      final String NAMED_QUERY = "One[Country] - Many[City]";
      Query query = mpa.setQuery(NAMED_QUERY, SQL_QUERY);
      PreparedStatement statement = query.get();
      
      try (ResultSet resultSet = mpa.executeQuery(statement, "Namibia")) {
         GraphResult graphResult = builder.build(mpa, resultSet);
         
         Set<City> cities = graphResult.getResults("city", City.class);
         Set<Country> countries = graphResult.getResults("country", Country.class);
         
         assertFalse(cities.isEmpty());
         assertFalse(countries.isEmpty());
         
         Country namibia = countries.iterator().next();
         assertEquals(cities.size(), namibia.getCities().size());
         assertTrue(cities.containsAll(namibia.getCities()));
         assertTrue(cities.stream().allMatch(city -> Objects.equals(city.getCountry(), namibia)));
      }
   }
   
}
