package com.devlambda.meta.persistence.domain;


import java.util.Objects;


/**
 * Tuple.
 * 
 * @param <L> left
 * @param <R> right
 * 
 * @author Dewald Pretorius
 */
public class Tuple<L, R> {
   
   private L left;
   private R right;

   public Tuple() { }

   public Tuple(L left, R right) { 
      setLeft(left);
      setRight(right);
   }

   @Override
   @SuppressWarnings("unchecked")
   public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Tuple)) return false;
      
      Tuple<L, R> other = (Tuple<L, R>) object;
      
      return Objects.equals(this.left, other.left) && Objects.equals(this.right, other.right);
   }

   @Override
   public int hashCode() { 
      return Objects.hash(left, right); 
   }

   public L getLeft() { return left; }
   public void setLeft(L left) { this.left = left; }

   public R getRight() { return right; }
   public void setRight(R right) { this.right = right; }
}
