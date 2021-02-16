package com.devlambda.meta.persistence;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class QueryTest {

   String sql = "SQL";
   @Mock Supplier<Connection> connector;
   @Mock Connection cachedConnection;
   @Mock Connection closedConnection;
   @Mock PreparedStatement cachedStatement;

   Query spy;

   @Before
   public void setUp() throws Exception {
      spy = spy(new Query(connector, sql) {
         @Override
         protected PreparedStatement prepareStatement(Connection connection) throws SQLException {
            return null;
         }
      });

      spy.queryMap.put(closedConnection, cachedStatement);
      spy.queryMap.put(cachedConnection, cachedStatement);

      assertEquals(connector, spy.connector);
      assertEquals(sql, spy.sql);

      when(closedConnection.isClosed()).thenReturn(Boolean.TRUE);
      when(cachedConnection.isClosed()).thenReturn(Boolean.FALSE);
   }

   @Test
   public void testGet() throws SQLException {
      Connection connection = mock(Connection.class);
      PreparedStatement preparedStatement = mock(PreparedStatement.class);
      SQLException sqle = new SQLException("Test Query#prepareStatement(Connection connection)");

      when(connector.get()).thenReturn(cachedConnection, connection);

      PreparedStatement result = spy.get(); //test

      assertEquals(cachedStatement, result);
      assertTrue(spy.queryMap.size() == 1);
      assertEquals(cachedStatement, spy.queryMap.get(cachedConnection));
      verify(spy, never()).prepareStatement(any(Connection.class));

      reset(spy);
      doReturn(preparedStatement).when(spy).prepareStatement(connection);

      result = spy.get(); //test

      assertEquals(preparedStatement, result);
      assertTrue(spy.queryMap.size() == 2);
      assertEquals(cachedStatement, spy.queryMap.get(cachedConnection));
      assertEquals(preparedStatement, spy.queryMap.get(connection));
      verify(spy).prepareStatement(connection);

      try {
         spy.queryMap.remove(connection);
         reset(spy);
         doThrow(sqle).when(spy).prepareStatement(connection);

         result = spy.get(); //test
         fail("No ExceptionSQLException expected");
      } catch (RuntimeException e) {
         assertEquals(sqle, e.getCause());
         assertTrue(spy.queryMap.size() == 1);
         assertEquals(cachedStatement, spy.queryMap.get(cachedConnection));
         verify(spy).prepareStatement(connection);
         return;
      }

      fail("No ExceptionSQLException expected");
   }

}
