package com.devlambda.meta.persistence.domain.school;


import com.devlambda.meta.persistence.domain.Tuple;


/**
 * @author Dewald Pretorius
 */
public class StudentClassMap extends Tuple<Long, Long> {

   public StudentClassMap() {
   }

   public StudentClassMap(Long left, Long right) { 
      super(left, right);
   }
   
   @Override
   public boolean equals(Object object) {
      return super.equals(object); 
   }
   
   @Override
   public int hashCode() { 
      return super.hashCode(); 
   }
}
