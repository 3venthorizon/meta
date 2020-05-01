package com.devlambda.meta.persistence;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;


/**
 * Query is a registry for cached {@link PreparedStatement}s and {@link Connection} associations.
 * 
 * @author Dewald Pretorius
 */
public abstract class Query {

   protected final String sql;
   protected final Supplier<Connection> connector;
   protected final Map<Connection, PreparedStatement> queryMap = new LinkedHashMap<>();

   public Query(Supplier<Connection> connector, String sql) {
      this.connector = connector;
      this.sql = sql;
   }

   public synchronized PreparedStatement get() {
      try {
         Connection connection = connector.get();
         Iterator<Entry<Connection, PreparedStatement>> iterator = queryMap.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<Connection, PreparedStatement> entry = iterator.next();
            if (entry.getKey().isClosed()) iterator.remove();
            else if (Objects.equals(connection, entry.getKey())) return entry.getValue();
         }

         PreparedStatement statement = prepareStatement(connection);
         queryMap.put(connection, statement);

         return statement;
      } catch (SQLException sqle) {
         throw new RuntimeException("Failed to register prepared statement", sqle);
      }
   }

   /**
    * Returns a new prepared statement from the provided <code>connection</code>.
    *  
    * @param connection for creating prepared statement
    * @return preparedStatement
    * @throws SQLException for JDBC api error
    */
   protected abstract PreparedStatement prepareStatement(Connection connection) throws SQLException;
}
