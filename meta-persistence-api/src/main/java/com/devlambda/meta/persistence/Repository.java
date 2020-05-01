package com.devlambda.meta.persistence;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.devlambda.meta.Property;
import com.devlambda.meta.Type;


/**
 * Repository is a JDBC Wrapper which allows data mapping between {@link PreparedStatement}s and {@link ResultSet}s to 
 * Meta{@link Type}s. The wrapped functions will throw the underlying {@link SQLException}s from the JDBC API.
 * 
 * @author Dewald Pretorius
 */
public class Repository {

   /**
    * Extracts for each record in the {@code resultSet} a {@code meta} typed object to be returned in a
    * {@code dataList}.
    * 
    * @param <M> metaType
    * @param resultSet to extract
    * @param meta type
    * @return dataList of updated metaType objects
    * @throws SQLException for JDBC api error
    */
   public <M> List<M> extract(ResultSet resultSet, Type<M> meta) throws SQLException {
      return extractDataRecords(resultSet, meta, 0, Integer.MAX_VALUE);
   }
   
   /**
    * Extracts for each record in the {@code resultSet} a {@code meta} typed object to be returned in a
    * {@code dataList}.
    * 
    * @param <M> metaType
    * @param resultSet to extract
    * @param meta type
    * @param skip the number of records in the {@code resultSet}
    * @param limit the number of record extractions from the {@code resultSet}
    * @return dataList of updated metaType objects
    * @throws SQLException for JDBC api error
    */
   public <M> List<M> extractDataRecords(ResultSet resultSet, Type<M> meta, int skip, int limit) throws SQLException {
      List<M> dataList = new ArrayList<>();

      while (resultSet.next()) {
         if (skip-- > 0) continue;
         M data = extractDataRecord(resultSet, meta);
         dataList.add(data);
         if (dataList.size() >= limit) break;
      }

      return dataList;
   }
   
   /**
    * Extracts for each record in the {@code resultSet} a {@link List} object of values.
    * 
    * @param resultSet to extract
    * @return dataMatrix list of lists
    * @throws SQLException for JDBC api error
    */
   public List<List<Object>> extractDataMatrix(ResultSet resultSet) throws SQLException {
      return extractDataMatrix(resultSet, 0, Integer.MAX_VALUE);
   }
   
   /**
    * Extracts for each record in the {@code resultSet} a {@link List} object of values.
    * 
    * @param resultSet to extract
    * @param skip the number of records in the {@code resultSet}
    * @param limit the number of record extractions from the {@code resultSet}
    * @return dataMatrix list of lists
    * @throws SQLException
    */
   public List<List<Object>> extractDataMatrix(ResultSet resultSet, int skip, int limit) throws SQLException {
      Type<List<Object>> type = extractType(resultSet.getMetaData());
      List<List<Object>> dataList = new ArrayList<>();

      while (resultSet.next()) {
         if (skip-- > 0) continue;
         List<Object> data = extractDataList(resultSet, type);
         dataList.add(data);
         if (dataList.size() >= limit) break;
      }

      return dataList;
   }
   

   /**
    * Clears and loads the {@code statement} in order of the defined {@code properties} names with the {@code data}. 
    * This method is thread safe: The {@code statement} is locked when clearing and setting all the query parameters.
    * 
    * @param <M> metaType
    * @param statement to load
    * @param properties name list to be loaded from the {@code data} into the {@code statement} at the corresponding 
    * index.
    * @param meta {@code data} type
    * @param data prototype
    * @throws SQLException for JDBC api error
    */
   public <M> void load(PreparedStatement statement, List<String> properties, Type<M> meta, M data) 
         throws SQLException {
      List<Object> parameters = properties.stream().map(meta::getProperty)
                                                   .filter(Objects::nonNull)
                                                   .map(property -> property.get.apply(data))
                                                   .collect(Collectors.toList());
      load(statement, parameters);
   }

   /**
    * Clears and loads the {@code statement} in order of the {@code parameter} list's values.
    * 
    * @param statement to load
    * @param parameters values
    * @throws SQLException for JDBC api error
    */
   public void load(PreparedStatement statement, List<? super Object> parameters) throws SQLException {
      load(statement, parameters.toArray());
   }

   /**
    * Clears and loads the {@code statement} in order of the {@code parameter} values.
    * 
    * @param statement to load
    * @param parameters values
    * @throws SQLException for JDBC api error
    */
   public void load(PreparedStatement statement, Object... parameters) throws SQLException {
      synchronized (statement) {
         statement.clearParameters();

         for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
         }
      }
   }

   /**
    * Lock 'n Load the {@code statement} with {@code data} and returns the {@code resultSet} from the executed query. 
    * This method is thread safe: The {@code statement} is locked when setting the query parameters until the 
    * {@code resultSet} is returned. 
    * 
    * @param <M> metaType
    * @param statement to load and execute query
    * @param properties name list to be loaded from the {@code data} into the {@code statement} at the corresponding 
    * index.
    * @param meta {@code data} type
    * @param data instance
    * @return resultSet
    * @throws SQLException for JDBC api error
    */
   public <M> ResultSet executeQuery(PreparedStatement statement, List<String> properties, Type<M> meta, M data) 
         throws SQLException {
      synchronized (statement) {
         load(statement, properties, meta, data);
         return statement.executeQuery();
      }
   }

   /**
    * Lock 'n Load the {@code statement} with {@code parameters} and returns the {@code resultSet} from the executed 
    * query. This method is thread safe: The {@code statement} is locked when setting the query parameters until the 
    * {@code resultSet} is returned. 
    * 
    * @param statement to load and execute query
    * @param parameters data list
    * @return resultSet
    * @throws SQLException for JDBC api error
    */
   public ResultSet executeQuery(PreparedStatement statement, List<? super Object> parameters) throws SQLException {
      synchronized (statement) {
         load(statement, parameters);
         return statement.executeQuery();
      }
   }

   /**
    * Lock 'n Load the {@code statement} with {@code parameters} and returns the {@code resultSet} from the executed 
    * query. This method is thread safe: The {@code statement} is locked when setting the query parameters until the 
    * {@code resultSet} is returned. 
    * 
    * @param statement to load and execute query
    * @param parameters data list
    * @return resultSet
    * @throws SQLException for JDBC api error
    */
   public ResultSet executeQuery(PreparedStatement statement, Object... parameters) throws SQLException {
      synchronized (statement) {
         load(statement, parameters);
         return statement.executeQuery();
      }
   }

   /**
    * Lock 'n Load the {@code statement} with {@code parameters} and returns the {@code rowCount} from the executed 
    * update. This method is thread safe: The {@code statement} is locked when setting the query parameters until 
    * {@code rowCount} is returned.
    * 
    * @param statement to load and execute update
    * @param parameters data list
    * @return rowCount affected
    * @throws SQLException for JDBC api error
    */
   public int executeUpdate(PreparedStatement statement, List<? super Object> parameters) throws SQLException {
      synchronized (statement) {
         load(statement, parameters);
         return statement.executeUpdate();
      }
   }

   /**
    * Lock 'n Load the {@code statement} with {@code parameters} and returns the {@code rowCount} from the executed 
    * update. This method is thread safe: The {@code statement} is locked when setting the query parameters until 
    * {@code rowCount} is returned.
    * 
    * @param statement to load and execute update
    * @param parameters data list
    * @return rowCount affected
    * @throws SQLException for JDBC api error
    */
   public int executeUpdate(PreparedStatement statement, Object... parameters) throws SQLException {
      synchronized (statement) {
         load(statement, parameters);
         return statement.executeUpdate();
      }
   }

   /**
    * Updates each of the {@code resultSet}'s records with the {@code update} data using the {@code columnPropertyMap} 
    * for field binding.
    * 
    * @param <M> metaType
    * @param resultSet to update
    * @param meta {@code update} type
    * @param update prototype
    * @param columnPropertyMap maps {@code update} property names to {@code resultSet} column names.
    * @return dataList of updated metaType objects
    * @throws SQLException for JDBC api error
    */
   public <M> List<M> update(ResultSet resultSet, Type<M> meta, M update, Map<String, String> columnPropertyMap) 
         throws SQLException {
      List<M> dataList = new ArrayList<>();

      while (resultSet.next()) {
         for (Entry<String, String> entry : columnPropertyMap.entrySet()) {
            String column = entry.getKey();
            String name = entry.getValue();
            Property<M, Object> property = meta.getProperty(name);
            Object value = property.get.apply(update);
            resultSet.updateObject(column, value);
         }

         resultSet.updateRow();
         M data = extractDataRecord(resultSet, meta);
         dataList.add(data);
      }

      return dataList;
   }

   /**
    * Deletes each of the records in the {@code resultSet}.
    * 
    * @param <M> metaType
    * @param resultSet to delete
    * @param meta type
    * @return dataList of updated metaType objects
    * @throws SQLException for JDBC api error
    */
   public <M> List<M> delete(ResultSet resultSet, Type<M> meta) throws SQLException {
      List<M> dataList = new ArrayList<>();

      while (resultSet.next()) {
         M data = extractDataRecord(resultSet, meta);
         dataList.add(data);
         resultSet.deleteRow();
      }

      return dataList;
   }

   /**
    * Converts the {@link ResultSetMetaData} descriptor into a {@link Type} definition.
    * 
    * @param rsmd to convert to a meta type.
    * @return metaType
    * @throws SQLException
    */
   @SuppressWarnings("unchecked")
   public Type<List<Object>> extractType(ResultSetMetaData rsmd) throws SQLException {
      int columnCount = rsmd.getColumnCount();
      
      Type<List<Object>> type = Type.meta(() -> new ArrayList<>(Arrays.asList(new Object[columnCount])));
      
      for (int column = 0; column < columnCount; column++) {
         try {
            String className = rsmd.getColumnClassName(column + 1);
            Class<Object> propertyClass = (Class<Object>) Class.forName(className);
            String label = rsmd.getColumnLabel(column + 1);
            final Integer index = Integer.valueOf(column);
            type.add(label, propertyClass, list -> list.get(index), (list, value) -> list.set(index, value));
         } catch (ClassNotFoundException cnfe) {
         }
      }
      
      return type;
   }
   
   /**
    * Extracts a single record from the {@code resulSet}'s current row into the provided {@code meta} type. The values
    * are fetched by their relative column index.
    * 
    * @param resultSet
    * @param type
    * @return
    * @throws SQLException
    */
   public List<Object> extractDataList(ResultSet resultSet, Type<List<Object>> type) 
         throws SQLException {
      List<Object> data = type.create();
      int columnIndex = 1;

      for (Property<List<Object>, Object> property : type.getProperties()) {
         Object value = resultSet.getObject(columnIndex++, property.type);
         property.set.accept(data, value);
      }

      return data;
   }
   
   /**
    * Extracts a single record from the {@code resulSet}'s current row into the provided {@code meta} type. The values
    * are fetched by column label name matching the {@link Type#getProperty(String, Class)} name and class.
    * 
    * @param <M> metaType
    * @param resultSet to extract
    * @param meta type
    * @return metaData 
    * @throws SQLException for JDBC api error
    */
   public <M> M extractDataRecord(ResultSet resultSet, Type<M> meta) throws SQLException {
      M data = meta.create();

      for (Property<M, Object> property : meta.getProperties()) {
         Object value = resultSet.getObject(property.name, property.type);
         property.set.accept(data, value);
      }

      return data;
   }
}
