package com.devlambda.meta.persistence;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.devlambda.meta.Type;


/**
 * MetaRepo is a resource collection in association with a database table or {@link ResultSet}.
 *
 * @author Dewald Pretorius
 */
public class MetaRepo<M> {

   public static final String INSERT = "insert";
   public static final String FIND_BY_ID = "find-by-id";
   public static final String FIND_ALL = "find-all";

   final Supplier<Connection> connector;
   final Type<M> type;
   final Class<M> metaClass;
   final List<String> pkColumns;
   final String tableName;
   final String selectSQL;
   final String findByIdSQL;
   final boolean setAutoGenKeys;

   final Map<String, Query> queryMap = new HashMap<>();

   public MetaRepo(Supplier<Connection> connector, Type<M> type, String tableName, List<String> pkColumns, 
                   boolean setAutoGenKeys, String selectSQL, String findByIdSQL, String insertSQL) {
      this.connector = connector;
      this.tableName = tableName;
      this.type = type;
      this.metaClass = type.getMetaClass();
      this.pkColumns = pkColumns;
      this.selectSQL = selectSQL;
      this.findByIdSQL = findByIdSQL;
      this.setAutoGenKeys = setAutoGenKeys;

      setQuery(INSERT, QueryFunction.persistQuery(connector, insertSQL, setAutoGenKeys));
      setQuery(FIND_BY_ID, QueryFunction.mergeQuery(connector, findByIdSQL));
      setQuery(FIND_ALL, selectSQL);
   }

   /**
    * Creates an ad hoc named cached query.
    * 
    * @param name alias of the query
    * @param sql to create a cahced {@link PreparedStatement}
    * @return query
    */
   public Query setQuery(String name, String sql) {
      return queryMap.computeIfAbsent(name, key -> QueryFunction.simpleQuery(connector, sql));
   }

   /**
    * Caches the named query.
    * 
    * @param name alias of the query
    * @param query to cache
    */
   public void setQuery(String name, Query query) {
      queryMap.put(name, query);
   }

   /**
    * Retrieves a named cached query by its alias.
    * 
    * @param name alias of the query
    * @return query
    */
   public Query getQuery(String name) {
      return queryMap.get(name);
   }
}
