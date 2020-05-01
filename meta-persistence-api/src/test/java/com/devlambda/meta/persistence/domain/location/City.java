package com.devlambda.meta.persistence.domain.location;


import java.util.Objects;


/**
 * City in the location domain.
 * 
 * @author Dewald Pretorius
 */
public class City {

   private Long id;
   private Integer countryId;
   private String name;
   private Country country;
   
   public City() { }

   public City(Integer countryId, String name) {
      setCountryId(countryId);
      setName(name);
   }
   
   public City(Country country, String name) { 
      setCountry(country);
      setName(name);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof City)) return false;

      City other = (City) object;

      return Objects.equals(this.id, other.id);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id);
   }

   public Long getId() { return id; }
   public void setId(Long id) { this.id = id; }

   public Integer getCountryId() { return countryId; }
   public void setCountryId(Integer countryId) { this.countryId = countryId; }

   public String getName() { return name; }
   public void setName(String name) { this.name = name; }

   public Country getCountry() { return country; }
   
   public void setCountry(Country country) { 
      this.country = country; 
      setCountryId(country.getId());
   }
}
