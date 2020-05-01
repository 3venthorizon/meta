package com.devlambda.meta.persistence;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


/**
 * ConnectionPool is a round robin {@link Connection} provider to scale database access over a number of 
 * {@link Connection}s.
 * 
 * @author Dewald Pretorius
 */
public class ConnectionPool implements Supplier<Connection> {

   protected final Supplier<Connection> connector;
   protected final List<Connection> pool = new ArrayList<>(); 
   protected final AtomicInteger roundRobin = new AtomicInteger();

   protected int size;

   public ConnectionPool(Supplier<Connection> connector, int size) {
      this.connector = connector;
      this.size = size;
   }

   @Override
   public synchronized Connection get() {
      if (size > pool.size()) {
         Connection connection = connector.get();
         pool.add(connection);

         return connection;
      }

      int index = roundRobin.incrementAndGet() % size;
      Connection connection = pool.get(index);

      try {
         if (!connection.isClosed()) return connection;

         connection = connector.get();
         pool.set(index, connection);

         return connection;
      } catch (SQLException sqle) {
         pool.remove(index);
         throw new RuntimeException("Error getting connection", sqle);
      }
   }

   public int getSize() { 
      return size; 
   }

   public void setSize(int size) {
      if (size < pool.size()) close(pool.subList(size, pool.size()));
      else this.size = size;
   }
   
   public  void close() {
      close(pool);
   }
   
   protected synchronized void close(List<Connection> connections) {
      Iterator<Connection> iterator = connections.iterator();
      
      while (iterator.hasNext()) {
         try {
            Connection connection = iterator.next();
            if (!connection.isClosed()) connection.close();
         } catch (Exception e) {
            e.printStackTrace(System.err);
         } finally {
            iterator.remove();
            this.size--;
         }
      }
   }
}
