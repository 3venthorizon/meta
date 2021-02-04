package com.devlambda.meta.persistence;


import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * This builder constructs a transactional execution plan with exception rollback strategies.
 * 
 * @author Dewald Pretorius
 */
public class TransactionPlanner {
   
   public class Begin {
      String savepoint;
      
      public Call call(Runnable runnable) {
         return call.setCall(runnable, savepoint);
      }
      
      Begin setSavepoint(String savepoint) {
         this.savepoint = savepoint;
         if (savepoint != null) plan.add(() -> mapSavepoint(savepoint));
         return this;
      }
   }
   
   public class Call {
      Runnable call;
      
      public Begin savePoint(String savepoint) {
         return TransactionPlanner.this.savepoint(savepoint);
      }
      
      public Call onRollback(String savepoint) {
         plan.set(plan.size() - 1, () -> execute(call, savepoint));
         return this;
      }
      
      public Plan commit() {
         plan.add(mpa::commit);
         return new Plan();
      }
      
      Call setCall(Runnable call, String savepoint) {
         this.call = call;
         plan.add(() -> execute(call, savepoint));
         return this;
      }
   }
   
   public class Plan {
      public void execute() {
         plan.forEach(Runnable::run);
      }
      
      public Thread executeNew() {
         Thread thread = new Thread(this::execute, "TransactionPlanner.Plan");
         thread.start();
         return thread;
      }
   }

   Begin begin = new Begin();
   Call call = new Call();
   
   final MPA mpa;
   final Map<String, Savepoint> savepointMap = new LinkedHashMap<>();
   final List<Runnable> plan = new ArrayList<>();
   
   TransactionPlanner(MPA mpa) {
      this.mpa = mpa;
   }
   
   public Begin begin() {
      return savepoint("BEGIN");
   }
   
   public Begin savepoint(String savepoint) {
      return begin.setSavepoint(savepoint);
   }
   
   void mapSavepoint(String savepoint) {
      savepointMap.put(savepoint, mpa.setSavepoint());
   }
   
   void execute(Runnable call, String rollback) {
      try {
         call.run();
      } catch (RuntimeException re) {
         mpa.rollback(savepointMap.get(rollback));
         throw re;
      }
   }
}
