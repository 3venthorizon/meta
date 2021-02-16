package com.devlambda.meta.persistence;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import com.devlambda.meta.Property;
import com.devlambda.meta.Type;


/**
 * GraphBuilder is an utility class which provides methods to extract {@link ResultSet} record data into object
 * instances.
 */
public class GraphBuilder {
   
   @SuppressWarnings("rawtypes")
   protected final LinkedHashMap<String, Type> typeMap = new LinkedHashMap<>();
   protected GraphResult graphResult;

   protected GraphBuilder() { }
   
   public static GraphBuilder builder() {
      return new GraphBuilder();
   }
   
   /**
    * Maps a <code>meta</code> type's {@link Property} by its <code>name</code> to the {@link ResultSet}'s 
    * <code>column</code> index. The meta instances can then be retrieved from the 
    * {@link GraphResult#getResults(String, Class)} using the input <code>graph</code> id.
    * 
    * @param <M> metaType
    * @param column index
    * @param name of the property
    * @param metaType accessor
    * @param graph id to access the metaType instance in the {@link GraphResult#getResults(String, Class)}
    * @return builder
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public <M> GraphBuilder mapTypeProperty(int column, String name, Type<M> metaType, String graph) {
      Type<Object> graphType = typeMap.computeIfAbsent(graph, key -> Type.meta(metaType.getCreator()));
      Function<List<Object>, Object> getter = list -> list.get(column);
      Property<M, Object> metaProperty = metaType.getProperty(name);
      BiConsumer<M, Object> setter = metaProperty.set;
      Property property = new Property(name, metaProperty.type, getter, setter);
      graphType.getProperties().add(property);
      
      return this;
   }
   
   /**
    * Maps an indexed column to the output {@link GraphResult}.
    *  
    * @param column index
    * @param graph id to access the metaType instance in the {@link GraphResult#getResults(String, Class)}
    * @return builder
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public GraphBuilder mapGraphScalar(int column, String graph) {
      Function<List<Object>, Object> getter = list -> list.get(column);
      BiConsumer<GraphResult, Object> setter = (result, value) -> result.addElement(graph, value);
      Type<Object> graphType = Type.meta(() -> graphResult);
      Property property = new Property(null, Object.class, getter, setter);
      graphType.getProperties().add(property);
      
      return this;
   }
   
   /**
    * Maps the {@link GraphResult#getResults(String, Class)} by its <code>property</code> to the <code>metaType</code>s
    * setter. The meta instances can then be retrieved from the 
    * {@link GraphResult#getResults(String, Class)} using the input <code>graph</code> id.
    * 
    * @param <M> metaType
    * @param <T> type
    * @param property name to retrieve from the {@link GraphResult#getResults(String, Class)} 
    * @param setter for the property from the {@link GraphResult#getResults(String, Class)} to the metaType instance
    * @param metaType accessor
    * @param graph id to access the metaType instance in the {@link GraphResult#getResults(String, Class)}
    * @return builder
    */
   @SuppressWarnings("unchecked")
   public <M, T> GraphBuilder mapGraphProperty(String property, BiConsumer<M, T> setter, 
                                               Type<M> metaType, String graph) {
      Type<M> graphType = typeMap.computeIfAbsent(graph, key -> Type.meta(metaType.getCreator()));
      graphType.add(property, Object.class, null, setter);
      
      return this;
   }
   
   /**
    * Extracts the {@link ResultSet} as a {@link GraphResult} using the {@link Repository} instance. 
    * 
    * @param resultSet to be extracted
    * @param repository manager
    * @return graphResult
    * @throws SQLException from the {@link ResultSet}
    */
   public GraphResult build(ResultSet resultSet, Repository repository) throws SQLException {
      Type<List<Object>> resultType = repository.extractType(resultSet.getMetaData());
      graphResult = new GraphResult();
      typeMap.keySet().forEach(graphResult::addProperty);
      
      while (resultSet.next()) {
         List<Object> record = repository.extractDataList(resultSet, resultType);
         accumulate(graphResult, record);
      }
      
      return graphResult;
   }
   
   /**
    * Extracts the {@link ResultSet} as a {@link GraphResult} using the {@link MPA} to manage the transactions and
    * automatic {@link MPA#rollback()}.
    * 
    * @param mpa manager
    * @param resultSet to be extracted
    * @return graphResult
    */
   public GraphResult build(MPA mpa, ResultSet resultSet) {
      try {
         return build(resultSet, mpa);
      } catch (SQLException sqle) {
         mpa.rollback();
         throw new RuntimeException("Error ETL from result set to graph result", sqle);
      }
   }
   
   /**
    * Creates a new Collector on this GraphBuilder with a new {@link GraphResult}.
    * 
    * @return collector
    */
   public Collector<List<Object>, GraphResult, GraphResult> collector() {
      this.graphResult = new GraphResult();
      Supplier<GraphResult> supplier = () -> this.graphResult;
      BiConsumer<GraphResult, List<Object>> accumulator = (graphResult, record) -> accumulate(graphResult, record);
      
      return Collector.of(supplier, accumulator, (left, right) -> left, Characteristics.UNORDERED);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   protected void accumulate(GraphResult graphResult, List<Object> record) {
      Map<String, Object> graphEntry = new LinkedHashMap<>();
      
      //[1] extract just the shell types without their compositional field types
      for (Entry<String, Type> entry : typeMap.entrySet()) {
         String graphId = entry.getKey();
         Type<Object> type = entry.getValue();
         Object meta = null;
         
         for (Property property : type.getProperties()) {
            if (typeMap.keySet().contains(property.name)) continue;
            Function<List<Object>, Object> getter = property.get;
            Object value = getter.apply(record);
            if (value == null) continue;
            if (meta == null) meta = type.create();
            property.set.accept(meta, value);
         }
         
         if (meta != null) graphEntry.put(graphId, graphResult.addElement(graphId, meta));
      }
      
      //[2] set the compositional field types for the shell types
      for (Entry<String, Type> entry : typeMap.entrySet()) {
         String graphId = entry.getKey();
         Object meta = graphEntry.get(graphId);
         if (meta == null) continue;
         Type<Object> type = entry.getValue();
         
         for (Property property : type.getProperties()) {
            if (!typeMap.keySet().contains(property.name)) continue;
            Object value = graphEntry.get(property.name);
            if (value == null) continue;
            property.set.accept(meta, value);
         }
      }
   }
}
