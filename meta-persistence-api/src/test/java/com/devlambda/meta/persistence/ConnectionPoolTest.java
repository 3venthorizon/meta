package com.devlambda.meta.persistence;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionPoolTest {

   @Mock Supplier<Connection> connector;

   int size = 5;
   ConnectionPool spy;

   @Before
   public void setUp() {
      spy = spy(new ConnectionPool(connector, size));

      assertEquals(connector, spy.connector);
      assertEquals(size, spy.size);
      assertTrue(spy.pool.isEmpty());
   }

   @Test
   public void testGet() throws SQLException {
      testGetFillPool();
      testGetRoundRobinConnection();
      testGetReconnect();
      testGetSQLException();
   }

   void testGetFillPool() {
      for (int x = 0; x < size; x++) {
         Connection connection = mock(Connection.class);

         when(connector.get()).thenReturn(connection);

         Connection result = spy.get(); //test

         assertEquals(connection, result);
         assertEquals(connection, spy.pool.get(x));
         verifyZeroInteractions(connection);
      }

      assertEquals(size, spy.pool.size());
   }

   void testGetRoundRobinConnection() throws SQLException {
      for (Connection connection : spy.pool) {
         when(connection.isClosed()).thenReturn(Boolean.FALSE);
      }

      Set<Connection> connections = new LinkedHashSet<>();

      for (int x = 0; x < size; x++) {
         Connection connection = spy.get(); //test
         connections.add(connection);
      }

      assertEquals(size, connections.size());

      for (Connection connection : connections) {
         assertEquals(connection, spy.get()); //test
      }
   }

   void testGetReconnect() throws SQLException {
      List<Connection> disconnects = new ArrayList<>();
      List<Connection> reconnections = new ArrayList<>();

      for (int index = 1; index < size; index += 2) {
         Connection connection = spy.pool.get(index);
         disconnects.add(connection);
         reset(connection);
         when(connection.isClosed()).thenReturn(Boolean.TRUE);

         Connection reconnnection = mock(Connection.class);
         reconnections.add(reconnnection);
      }

      assertTrue(disconnects.size() > 1);
      when(connector.get()).thenReturn(reconnections.get(0), reconnections.subList(1, reconnections.size())
            .toArray(new Connection[reconnections.size() - 1]));

      Set<Connection> connections = new LinkedHashSet<>();

      for (int x = 0; x < size; x++) {
         Connection connection = spy.get(); //test
         connections.add(connection);
      }

      assertEquals(size, connections.size());
      assertTrue(connections.containsAll(reconnections));
      assertFalse(disconnects.stream().anyMatch(connections::contains));
   }

   void testGetSQLException() {
      SQLException sqle = new SQLException("Test Connection#isClosed()");

      try {
         Connection connection = spy.pool.get(0);
         reset(connection);
         when(connection.isClosed()).thenThrow(sqle);

         for (int x = 0; x < size; x++) {
            spy.get();
         }

         fail("Unchecked Exception expected");
      } catch (SQLException e) {
         fail("SQL Exception is expected to be wrapped in a unchecked exception");
      } catch (RuntimeException e) {
         assertEquals(sqle, e.getCause());
         assertEquals(size - 1, spy.pool.size());
         return;
      }

      fail("No ExceptionSQLException expected");
   }

   @Test
   public void testGetSize() {
      assertEquals(size, spy.getSize());
   }

   @Test
   public void testSetSize() {
      testGetFillPool();
      int resize = size - 2;

      spy.setSize(resize); //test

      assertEquals(resize, spy.getSize());
      assertEquals(resize, spy.pool.size());

      spy.setSize(size);

      assertEquals(size, spy.getSize());
      assertEquals(resize, spy.pool.size());
   }
}
