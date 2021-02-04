package com.devlambda.meta.persistence;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionManagerTest {

   @Mock ConnectionPool parallelPool;
   @Mock ConnectionPool sequencePool;
   @Mock ThreadLocal<Connection> transaction;
   @Mock WeakHashMap<Connection, Thread> transactionMap;

   TransactionManager spy;

   @Before
   @SuppressWarnings("unchecked")
   public void setUp() {
      spy = spy(new TransactionManager(mock(Supplier.class), 2, 4));
      spy.parallelPool = parallelPool;
      spy.sequencePool = sequencePool;
      spy.transaction = transaction;
      spy.transactionMap = transactionMap;
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testConstructor() throws SQLException {
      int poolSize = 2;
      int transactionsLimit = 4;
      Supplier<Connection> connector = mock(Supplier.class);
      Connection connection = mock(Connection.class);

      when(connector.get()).thenReturn(connection);

      TransactionManager result = new TransactionManager(connector, 2, 4); //test

      assertEquals(poolSize, result.parallelPool.getSize());
      assertEquals(transactionsLimit, result.sequencePool.getSize());
      assertEquals(connector, result.parallelPool.connector);
      assertNotEquals(connector, result.sequencePool.connector);

      TransactionManager spy = spy(result);

      Connection trxConnection = spy.sequencePool.get(); //test transaction decoration of the connection

      assertEquals(connection, trxConnection);
      verify(connection).setAutoCommit(false);
      verifyNoMoreInteractions(connection);

      reset(connection, spy);

      Connection con = spy.parallelPool.get(); //test no transaction connection
      assertEquals(connection, con);
      verifyZeroInteractions(connection, spy);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testEnableTransactions() {
      Supplier<Connection> connector = mock(Supplier.class);
      Connection connection = mock(Connection.class);
      SQLException sqle = new SQLException("Test Connection#isClosed()");

      try {
         when(connector.get()).thenReturn(connection);

         spy.enableTransactions(connector); //test 

         verify(connection).setAutoCommit(false);

         reset(connection);
         doThrow(sqle).when(connection).setAutoCommit(false);

         spy.enableTransactions(connector); //test exception disabling auto commit

         fail("Unchecked Exception expected");
      } catch (SQLException e) {
         fail("SQL Exception is expected to be wrapped in a unchecked exception");
      } catch (RuntimeException e) {
         assertEquals(sqle, e.getCause());
         return;
      }

      fail("No ExceptionSQLException expected");
   }

   @Test
   public void testGet() {
      Connection trxConnection = mock(Connection.class);
      Connection connection = mock(Connection.class);

      when(transaction.get()).thenReturn(null, trxConnection);
      when(parallelPool.get()).thenReturn(connection);

      assertEquals(connection, spy.get()); //test non-transactional connection
      assertEquals(trxConnection, spy.get()); //test transactional connection
   }

   @Test
   public void testBegin() {
      Connection connection = mock(Connection.class);

      when(sequencePool.getSize()).thenReturn(5);
      when(sequencePool.get()).thenReturn(connection);
      when(transactionMap.containsKey(connection)).thenReturn(Boolean.TRUE, Boolean.FALSE);

      Connection result = spy.begin();

      assertEquals(connection, result);

      InOrder inOrder = inOrder(transaction, transactionMap);
      inOrder.verify(transactionMap, times(2)).containsKey(connection);
      inOrder.verify(transactionMap).put(connection, Thread.currentThread());
      inOrder.verify(transaction).set(connection);

      verifyNoMoreInteractions(transaction, transactionMap);
      verifyZeroInteractions(connection, parallelPool);
   }

   @Test
   public void testBeginTransactionsExhausted() {
      Connection connection = mock(Connection.class);

      when(sequencePool.getSize()).thenReturn(5);
      when(sequencePool.get()).thenReturn(connection);
      when(transactionMap.containsKey(connection)).thenReturn(Boolean.TRUE);

      try {
         spy.begin(); //test

         fail("Unchecked Exception expected");
      } catch (RuntimeException e) {
         assertEquals("Transaction connections exhausted", e.getMessage());

         verify(transactionMap, times(5)).containsKey(connection);
         verifyNoMoreInteractions(transactionMap);
         verifyZeroInteractions(connection, transaction, parallelPool);

         return;
      }

      fail("Unchecked Exception expected");
   }

   @Test
   public void testSetSavePoint() throws SQLException {
      Connection connection = mock(Connection.class);
      Connection trxConnection = mock(Connection.class);
      Savepoint conSavePoint = mock(Savepoint.class);
      Savepoint trxSavePoint = mock(Savepoint.class);
      SQLException sqle = new SQLException("Test Connection#setSavepoint()");

      when(transaction.get()).thenReturn(null, trxConnection);
      doReturn(connection).when(spy).begin();
      when(connection.setSavepoint()).thenReturn(conSavePoint);
      when(trxConnection.setSavepoint()).thenReturn(trxSavePoint).thenThrow(sqle);

      Savepoint result = spy.setSavePoint(); //test non-existant transaction savepoint

      assertEquals(conSavePoint, result);
      verify(connection).setSavepoint();
      verifyZeroInteractions(conSavePoint);

      result = spy.setSavePoint(); //test existing transaction savepoint

      assertEquals(trxSavePoint, result);
      verify(trxConnection).setSavepoint();
      verifyZeroInteractions(trxSavePoint);

      try {
         spy.setSavePoint(); //test setSavepoint SQLException
         fail("Unchecked Exception expected");
      } catch (RuntimeException e) {
         assertEquals(sqle, e.getCause());
         return;
      }

      fail("Unchecked Exception expected");
   }

   @Test
   public void testRollback() {
      doNothing().when(spy).rollback(null);

      spy.rollback(); //test

      verify(spy).rollback(null);
   }

   @Test
   public void testRollbackSavepoint() throws SQLException {
      Savepoint savepoint = mock(Savepoint.class);
      Savepoint errorpoint = mock(Savepoint.class);
      Connection connection = mock(Connection.class);
      SQLException sqle = new SQLException("Test Connection#rollback(Savepoint savepoint)");

      when(transaction.get()).thenReturn(null, null, connection);

      spy.rollback(null); //test no operation for no transaction
      spy.rollback(savepoint); //test no operation for no transaction

      doNothing().when(spy).endTransaction(connection);
      doNothing().when(connection).rollback(savepoint);
      doNothing().when(connection).rollback();
      doThrow(sqle).when(connection).rollback(errorpoint);

      spy.rollback(savepoint); //test rollback with savepoint
      spy.rollback(null); //test no savepoint rollback

      try {
         spy.rollback(errorpoint);
         fail("Unchecked Exception expected");
      } catch (RuntimeException e) {
         assertEquals(sqle, e.getCause());

         InOrder inOrder = inOrder(connection);
         inOrder.verify(connection).rollback(savepoint);
         inOrder.verify(connection).rollback();
         inOrder.verify(connection).rollback(errorpoint);

         verify(transaction, times(5)).get();
         verify(spy, times(3)).endTransaction(connection);
         verifyNoMoreInteractions(connection);
         verifyZeroInteractions(savepoint, errorpoint);

         return;
      }

      fail("Unchecked Exception expected");
   }

   @Test
   public void testCommit() throws SQLException {
      Connection connection = mock(Connection.class);
      Connection errorConnection = mock(Connection.class);
      SQLException sqle = new SQLException("Test Connection#commit()");

      when(transaction.get()).thenReturn(null, connection, errorConnection);

      spy.commit(); //test no operation for no transaction

      doNothing().when(connection).commit();
      doThrow(sqle).when(errorConnection).commit();

      try {
         spy.commit(); //test commit
         spy.commit(); //test SQLException during commit
         fail("Unchecked Exception expected");
      } catch (RuntimeException e) {
         assertEquals(sqle, e.getCause());

         verify(connection).commit();
         verify(errorConnection).commit();
         verifyNoMoreInteractions(connection, errorConnection);

         return;
      }

      fail("Unchecked Exception expected");
   }

   @Test
   public void testEndTransaction() {
      Connection connection = mock(Connection.class);

      spy.endTransaction(connection); //test

      verify(transaction).set(null);
      verify(transactionMap).remove(connection);
      verifyNoMoreInteractions(transaction, transactionMap);
      verifyZeroInteractions(connection);
   }
}
