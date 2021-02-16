package com.devlambda.meta.persistence;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * QueryFunction is an utility class to provide different types of {@link Query}s.
 */
public class QueryFunction extends Query {

   protected final Function<Connection, PreparedStatement> queryFunction;

   public QueryFunction(Supplier<Connection> connector, Function<Connection, PreparedStatement> queryFunction) {
      super(connector, null);

      this.queryFunction = queryFunction;
   }

   public static Query simpleQuery(Supplier<Connection> connector, String sql) {
      return new QueryFunction(connector, connection -> QueryFunction.queryFunction(connection, sql));
   }

   public static Query mergeQuery(Supplier<Connection> connector, String sql) {
      return new QueryFunction(connector, connection -> QueryFunction.mergeFunction(connection, sql));
   }

   public static Query persistQuery(Supplier<Connection> connector, String sql, boolean setAutoGenKeys) {
      int generateKeys = setAutoGenKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
      return new QueryFunction(connector, connection -> QueryFunction.persistFunction(connection, sql, generateKeys));
   }

   protected static PreparedStatement mergeFunction(Connection connection, String sql) {
      try {
         return connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
      } catch (SQLException sqle) {
         throw new RuntimeException("Failed to register prepared statement for merge query", sqle);
      }
   }

   protected static PreparedStatement persistFunction(Connection connection, String sql, int generateKeys) {
      try {
         return connection.prepareStatement(sql, generateKeys);
      } catch (SQLException sqle) {
         throw new RuntimeException("Failed to register prepared statement for persist query", sqle);
      }
   }

   protected static PreparedStatement queryFunction(Connection connection, String sql) {
      try {
         return connection.prepareStatement(sql);
      } catch (SQLException sqle) {
         throw new RuntimeException("Failed to register prepared statement for simple query", sqle);
      }
   }

   @Override
   protected PreparedStatement prepareStatement(Connection connection) throws SQLException {
      return queryFunction.apply(connection);
   }
}
