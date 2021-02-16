package com.devlambda.meta;


import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


/**
 * Plain Old Java Object.
 */
public class Pojo {
   
   String text;
   BigDecimal money;
   Double number;
   Integer count;
   Boolean flag;
   Pojo parent;
   List<String> list;
   double primitive;
   
   @Override
   public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Pojo)) return false;
      
      Pojo other = (Pojo) object;
      
      return Objects.equals(count, other.count) && Objects.equals(flag, other.flag) && 
             Objects.equals(money, other.money) && Objects.equals(number, other.number) && 
             Objects.equals(text, other.text);
   }

   @Override
   public int hashCode() { 
      return Objects.hash(count, flag, money, number, text); 
   }

   public String getText() { return text; }
   public void setText(String text) { this.text = text; }

   public BigDecimal getMoney() { return money; }
   public void setMoney(BigDecimal money) { this.money = money; }

   public Double getNumber() { return number; }
   public void setNumber(Double number) { this.number = number; }

   public Integer getCount() { return count; }
   public void setCount(Integer count) { this.count = count; }

   public Boolean getFlag() { return flag; }
   public void setFlag(Boolean flag) { this.flag = flag; }

   public Pojo getParent() { return parent; }
   public void setParent(Pojo parent) { this.parent = parent; }

   public List<String> getList() { return list; }
   public void setList(List<String> list) { this.list = list; }

   public double getPrimitive() { return primitive; }
   public void setPrimitive(double primitive) { this.primitive = primitive; }
}
