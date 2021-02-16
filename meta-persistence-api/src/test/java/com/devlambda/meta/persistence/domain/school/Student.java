package com.devlambda.meta.persistence.domain.school;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Student in the school domain.
 */
public class Student {

   private Long id;
   private String name;
   private Set<Subject> subjects;
   
   public Student() { }

   public Student(String name) {
      setName(name);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Student)) return false;

      Student other = (Student) object;

      return Objects.equals(this.id, other.id);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id);
   }

   public Long getId() { return id; }
   public void setId(Long id) { this.id = id; }

   public String getName() { return name; }
   public void setName(String name) { this.name = name; }

   public Set<Subject> getSubjects() { 
      if (subjects == null) subjects = new HashSet<>();
      return subjects; 
   }

   public void setSubjects(Set<Subject> subjects) { this.subjects = subjects; }
}
