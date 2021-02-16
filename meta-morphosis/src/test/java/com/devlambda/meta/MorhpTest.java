package com.devlambda.meta;


import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;


/**
 * MorhpTest 
 */
public class MorhpTest {

   static class A {
      private String field;
      private int number;
      private Double trouble;

      public String getField() { return field; }
      public void setField(String field) { this.field = field; }

      public int getNumber() { return number; }
      public void setNumber(int number) {  this.number = number; }

      public Double getTrouble() { return trouble; }
      public void setTrouble(Double trouble) { this.trouble = trouble; }
   }
   
   static class Ac extends A {
      private String inherited;

      public String getInherited() { return inherited; }
      public void setInherited(String inherited) { this.inherited = inherited; }
   }

   static class B {
      private String a;
      private Integer b;
      private double d;

      public String getA() { return a; }
      public void setA(String a) { this.a = a; }

      public int getB() { return b; }
      public void setB(int b) { this.b = b; }

      public Double getD() { return d; }
      public void setD(Double d) { this.d = d; }
   }

   @Test
   public void testMorphLambda() {
      String field = "Field";
      int pyth = 345;
      double pi = Math.PI;

      //maps the fields in class A to X, Y, Z 
      Type<A> typeA = Type.meta(A::new);
      typeA.add("X", String.class, A::getField, A::setField);
      typeA.add("Y", int.class, A::getNumber, A::setNumber);
      typeA.add("Z", Double.class, A::getTrouble, A::setTrouble);

      //maps the fields in class B to X, Y, Z 
      Type<B> typeB = Type.meta(B::new);
      typeB.add("X", String.class, B::getA, B::setA);
      typeB.add("Y", int.class, B::getB, B::setB);
      typeB.add("Z", Double.class, B::getD, B::setD);

      A metA = new A();
      metA.setField(field);
      metA.setNumber(pyth);
      metA.setTrouble(pi);

      B metB = Morph.convert(typeA, metA, typeB);

      assertEquals(field, metB.getA());
      assertEquals(pyth, metB.getB());
      assertEquals(pi, metB.getD().doubleValue(), 0.0d);

      A reverse = Morph.convert(typeB, metB, typeA);

      assertEquals(field, reverse.getField());
      assertEquals(pyth, reverse.getNumber());
      assertEquals(pi, reverse.getTrouble().doubleValue(), 0.0d);
   }

   @Test
   public void testMorphReflection() throws IllegalAccessException {
      String field = "Field";
      int pyth = 345;
      double pi = Math.PI;
      String inherited = "Inherited";
      
      Type<Ac> typeA = Type.meta(Ac::new, Ac.class);
      Ac metAc = new Ac();

      Map<String, Object> map = Morph.wrapMap(typeA, metAc);
      
      map.put("field", field);
      map.put("number", pyth);
      map.put("trouble", pi);
      map.put("inherited", inherited);
      
      assertEquals(field, metAc.getField());
      assertEquals(pyth, metAc.getNumber());
      assertEquals(Double.valueOf(pi), metAc.getTrouble());
      assertEquals(inherited, metAc.getInherited());
      
      assertEquals(metAc.getField(), map.get("field"));
      assertEquals(Integer.valueOf(metAc.getNumber()), map.get("number"));
      assertEquals(metAc.getTrouble(), map.get("trouble"));
      assertEquals(metAc.getInherited(), map.get("inherited"));
   }
}
