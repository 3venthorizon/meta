package com.devlambda.meta.persistence.domain.school;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Subject in the school domain.
 * 
 * @author Dewald Pretorius
 */
public class Subject {

   private Long id;
   private String code;
   private String name;
   private Set<Student> students;

   public Subject() { }

   public Subject(String code, String name) { 
      this(); 
      setCode(code);
      setName(name);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Subject)) return false;

      Subject other = (Subject) object;

      return Objects.equals(this.id, other.id);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id);
   }

   public Long getId() { return id; }
   public void setId(Long id) { this.id = id; }

   public String getCode() { return code; }
   public void setCode(String code) { this.code = code; }

   public String getName() { return name; }
   public void setName(String name) { this.name = name; }

   public Set<Student> getStudents() { 
      if (students == null) students = new HashSet<>();
      return students; 
   }

   public void setStudents(Set<Student> students) { this.students = students; }
}
