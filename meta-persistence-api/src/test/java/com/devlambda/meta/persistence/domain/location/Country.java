package com.devlambda.meta.persistence.domain.location;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Country in the location domain.
 */
public class Country {

   private Integer id;
   private String name;
   private Set<City> cities;
   
   public Country() { }
   
   public Country(String name) {
      setName(name);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Country)) return false;
      
      Country other = (Country) object;
      
      return Objects.equals(this.id, other.id);
   }

   @Override
   public int hashCode() { 
      return Objects.hash(id); 
   }

   public Integer getId() { return id; }
   public void setId(Integer id) { this.id = id; }

   public String getName() { return name; }
   public void setName(String name) { this.name = name; }

   public Set<City> getCities() {
      if (cities == null) cities = new HashSet<>();
      return cities;
   }

   public void setCities(Set<City> cities) { this.cities = cities; }
}
