package com.devlambda.meta.persistence;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.WeakHashMap;
import java.util.function.Supplier;


/**
 * TransactionManager is a {@link Connection} provider with the ability to control database transactions.
 */
public class TransactionManager implements Transaction {

   protected ConnectionPool parallelPool;
   protected ConnectionPool sequencePool;
   protected ThreadLocal<Connection> transaction;
   protected WeakHashMap<Connection, Thread> transactionMap;
   
   /**
    * Creates the Transaction Manager with two {@link ConnectionPool}s:
    * <ul>
    * <li>One Auto Commit {@link ConnectionPool} with a minimum of 2 connections or half the machine's processors</li>
    * <li>One Transactional {@link ConnectionPool} with a minimum of 4 connections or equal to the machine's 
    * processors count</li>
    * </ul>
    * @param connector connection provider
    */
   public TransactionManager(Supplier<Connection> connector) {
      this(connector, TransactionManager.defaultPoolSize(), TransactionManager.defaultTransactionLimit());
   }
   
   /**
    * Creates the Transaction Manager with two {@link ConnectionPool}s:
    * <ul>
    * <li>One Auto Commit {@link ConnectionPool}</li>
    * <li>One Transactional {@link ConnectionPool}</li>
    * </ul>
    * @param connector connection provider
    * @param poolSize of auto commit {@link ConnectionPool}
    * @param transactionsLimit of transactional {@link ConnectionPool}
    */
   public TransactionManager(Supplier<Connection> connector, int poolSize, int transactionsLimit) {
      parallelPool = new ConnectionPool(connector, poolSize);
      sequencePool = new ConnectionPool(() -> enableTransactions(connector), transactionsLimit);
      transaction = new ThreadLocal<>();
      transactionMap = new WeakHashMap<>(transactionsLimit);
   }

   public static final int defaultPoolSize() {
      return defaultTransactionLimit() / 2;
   }

   public static final int defaultTransactionLimit() {
      int processors = Runtime.getRuntime().availableProcessors();
      return processors < 4 ? processors = 4 : processors;
   }

   /**
    * Decorates the connections to enable transactions provided by the connector. 
    * 
    * @param connector connection provider
    * @return connection with auto commit disabled
    */
   protected synchronized Connection enableTransactions(Supplier<Connection> connector) {
      try {
         Connection connection = connector.get();
         connection.setAutoCommit(false);

         return connection;
      } catch (SQLException sqle) {
         throw new RuntimeException("Failed to setup transactional connection", sqle);
      }
   }

   @Override
   public Connection get() {
      Connection connection = transaction.get();
      return connection == null ? parallelPool.get() : connection;
   }

   @Override
   public synchronized Connection begin() {
      synchronized (transactionMap) {
         for (int x = 0, total = sequencePool.getSize(); x < total; x++) {
            Connection connection = sequencePool.get();
            if (transactionMap.containsKey(connection)) continue;

            transactionMap.put(connection, Thread.currentThread());
            transaction.set(connection);

            return connection;
         }

         throw new RuntimeException("Transaction connections exhausted");
      }
   }

   @Override
   public Savepoint setSavePoint() {
      Connection connection = transaction.get();
      if (connection == null) connection = begin();

      try {
         return connection.setSavepoint();
      } catch (SQLException sqle) {
         throw new RuntimeException("Failed to set transaction savepoint", sqle);
      }
   }

   @Override
   public void rollback() {
      rollback(null);
   }

   @Override
   public void rollback(Savepoint savepoint) {
      Connection connection = transaction.get();
      if (connection == null) return;

      try {
         if (savepoint == null) connection.rollback();
         else connection.rollback(savepoint);
      } catch (SQLException sqle) {
         throw new RuntimeException("Failed to rollback transaction", sqle);
      } finally {
         endTransaction(connection);
      }
   }

   @Override
   public void commit() {
      Connection connection = transaction.get();
      if (connection == null) return;

      try {
         connection.commit();
      } catch (SQLException sqle) {
         throw new RuntimeException("Failed to commit transaction", sqle);
      } finally {
         endTransaction(connection);
      }
   }

   @Override
   public void close() {
      parallelPool.close();
      sequencePool.close();
   }

   protected void endTransaction(Connection connection) {
      transaction.set(null);

      synchronized (transactionMap) {
         transactionMap.remove(connection);
      }
   }
}
