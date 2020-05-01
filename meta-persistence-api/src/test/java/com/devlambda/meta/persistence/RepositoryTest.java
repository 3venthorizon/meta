package com.devlambda.meta.persistence;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.devlambda.meta.Type;


/**
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class RepositoryTest {

   static class Data {
      private String text;
      private Double decimal;
      private Integer number;

      public String getText() { return text; }
      public void setText(String text) { this.text = text; }

      public Double getDecimal() { return decimal; }
      public void setDecimal(Double decimal) { this.decimal = decimal; }

      public Integer getNumber() { return number; }
      public void setNumber(Integer number) { this.number = number; }
   }

   static Type<Data> TYPE_DATA = Type.meta(Data::new);

   @Spy Repository repository;


   @BeforeClass
   public static void bootUp() {
      TYPE_DATA.add("TEXT", String.class, Data::getText, Data::setText)
               .add("DECIMAL", Double.class, Data::getDecimal, Data::setDecimal)
               .add("NUMBER", Integer.class, Data::getNumber, Data::setNumber);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testExtractDataRecord() throws SQLException {
      Type<Data> TYPE_DATA = Type.meta(Data::new);
      TYPE_DATA.add("TEXT", String.class, Data::getText, Data::setText)
               .add("DECIMAL", Double.class, Data::getDecimal, Data::setDecimal)
               .add("NUMBER", Integer.class, Data::getNumber, Data::setNumber);
      ResultSet resultSet = mock(ResultSet.class);

      when(resultSet.getObject("TEXT", String.class)).thenReturn("VARCHAR");
      when(resultSet.getObject("DECIMAL", Double.class)).thenReturn(901234.5678);
      when(resultSet.getObject("NUMBER", Integer.class)).thenReturn(888);

      Data result = repository.extractDataRecord(resultSet, TYPE_DATA); //test

      assertEquals("VARCHAR", result.getText());
      assertEquals(Double.valueOf(901234.5678), result.getDecimal());
      assertEquals(Integer.valueOf(888), result.getNumber());

      verify(resultSet, times(TYPE_DATA.getProperties().size())).getObject(anyString(), any(Class.class));
      verifyNoMoreInteractions(resultSet);
   }

   @Test
   public void testDelete() throws SQLException {
      ResultSet resultSet = mock(ResultSet.class);
      Data one = mock(Data.class);
      Data two = mock(Data.class);
      Data six = mock(Data.class);

      when(resultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
      doReturn(one, two, six).when(repository).extractDataRecord(resultSet, TYPE_DATA);

      List<Data> result = repository.delete(resultSet, TYPE_DATA); //test

      assertEquals(3, result.size());
      assertEquals(one, result.get(0));
      assertEquals(two, result.get(1));
      assertEquals(six, result.get(2));

      InOrder inOrder = inOrder(repository, resultSet);

      for (int x = 0; x < 3; x++) {
         inOrder.verify(resultSet).next();
         inOrder.verify(repository).extractDataRecord(resultSet, TYPE_DATA);
         inOrder.verify(resultSet).deleteRow();
      }

      inOrder.verify(resultSet).next();
      verifyNoMoreInteractions(resultSet);
   }

   @Test
   public void testExtract() throws SQLException {
      ResultSet resultSet = mock(ResultSet.class);
      Data one = mock(Data.class);
      Data two = mock(Data.class);
      Data six = mock(Data.class);

      when(resultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
      doReturn(one, two, six).when(repository).extractDataRecord(resultSet, TYPE_DATA);

      List<Data> result = repository.extract(resultSet, TYPE_DATA); //test

      assertEquals(3, result.size());
      assertEquals(one, result.get(0));
      assertEquals(two, result.get(1));
      assertEquals(six, result.get(2));

      InOrder inOrder = inOrder(repository, resultSet);

      for (int x = 0; x < 3; x++) {
         inOrder.verify(resultSet).next();
         inOrder.verify(repository).extractDataRecord(resultSet, TYPE_DATA);
      }

      inOrder.verify(resultSet).next();
      verifyNoMoreInteractions(resultSet);
      verify(repository).extractDataRecords(resultSet, TYPE_DATA, 0, Integer.MAX_VALUE);
   }

   @Test
   public void testExtractSkip() throws SQLException {
      ResultSet resultSet = mock(ResultSet.class);
      Data one = mock(Data.class);
      Data two = mock(Data.class);
      Data six = mock(Data.class);
      int skip = 2;

      when(resultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, 
                                        Boolean.FALSE);
      doReturn(one, two, six).when(repository).extractDataRecord(resultSet, TYPE_DATA);

      List<Data> result = repository.extractDataRecords(resultSet, TYPE_DATA, skip, Integer.MAX_VALUE); //test

      assertEquals(3, result.size());
      assertEquals(one, result.get(0));
      assertEquals(two, result.get(1));
      assertEquals(six, result.get(2));

      verify(resultSet, times(6)).next();
      verify(repository, times(3)).extractDataRecord(resultSet, TYPE_DATA);
      verifyNoMoreInteractions(resultSet);
   }

   @Test
   public void testExtractLimit() throws SQLException {
      ResultSet resultSet = mock(ResultSet.class);
      Data one = mock(Data.class);
      Data two = mock(Data.class);
      int limit = 2;

      when(resultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
      doReturn(one, two).when(repository).extractDataRecord(resultSet, TYPE_DATA);

      List<Data> result = repository.extractDataRecords(resultSet, TYPE_DATA, 0, limit); //test

      assertEquals(limit, result.size());
      assertEquals(one, result.get(0));
      assertEquals(two, result.get(1));

      InOrder inOrder = inOrder(repository, resultSet);

      for (int x = 0; x < limit; x++) {
         inOrder.verify(resultSet).next();
         inOrder.verify(repository).extractDataRecord(resultSet, TYPE_DATA);
      }

      verifyNoMoreInteractions(resultSet);
   }

   @Test
   public void testLoad() throws SQLException {
      List<String> properties = Arrays.asList("TEXT", "NUMBER", "unknown-property", "TEXT");
      Data data = mock(Data.class);
      PreparedStatement statement = mock(PreparedStatement.class);

      when(data.getText()).thenReturn("Text");
      when(data.getNumber()).thenReturn(456);

      repository.load(statement, properties, TYPE_DATA, data); //test

      InOrder inOrder = inOrder(repository, statement);
      inOrder.verify(repository).load(statement, properties, TYPE_DATA, data); 
      inOrder.verify(repository).load(eq(statement), anyList()); //delegated test
      inOrder.verify(repository).load(statement, "Text", 456, "Text"); //delegated test
      inOrder.verify(statement).clearParameters();
      inOrder.verify(statement).setObject(1, "Text");
      inOrder.verify(statement).setObject(2, 456);
      inOrder.verify(statement).setObject(3, "Text");
      inOrder.verifyNoMoreInteractions();

      verify(data, times(2)).getText();
      verify(data).getNumber();
      verifyNoMoreInteractions(repository, statement, data);
   }

   @Test
   public void testUpdate() throws SQLException {
      ResultSet resultSet = mock(ResultSet.class);
      Data update = mock(Data.class);
      Data one = mock(Data.class);
      Data two = mock(Data.class);
      Data six = mock(Data.class);
      List<String> columns = Arrays.asList("ColumnB", "ColumnA");
      List<String> properties = TYPE_DATA.getPropertyNames().stream().limit(2).collect(Collectors.toList());
      Collections.reverse(properties);
      assertEquals(columns.size(), properties.size());
      Map<String, String> columnPropertyMap = new HashMap<>();

      for (int x = 0; x < columns.size(); x++) {
         columnPropertyMap.put(columns.get(x), properties.get(x));
      }

      when(resultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
      when(update.getText()).thenReturn("New Line\n");
      when(update.getDecimal()).thenReturn(null);
      doReturn(one, two, six).when(repository).extractDataRecord(resultSet, TYPE_DATA);

      List<Data> result = repository.update(resultSet, TYPE_DATA, update, columnPropertyMap); //test

      assertEquals(3, result.size());
      assertEquals(one, result.get(0));
      assertEquals(two, result.get(1));
      assertEquals(six, result.get(2));

      InOrder inOrder = inOrder(repository, resultSet);

      for (int x = 0; x < 3; x++ ) {
         inOrder.verify(resultSet).next();
         inOrder.verify(resultSet).updateObject("ColumnB", null); //DECIMAL = null
         inOrder.verify(resultSet).updateObject("ColumnA", "New Line\n");
         inOrder.verify(repository).extractDataRecord(resultSet, TYPE_DATA);
      }

      inOrder.verify(resultSet).next();
   }

   @Test
   public void testExecuteQuery() throws SQLException {
      List<String> properties = Arrays.asList("TEXT", "NUMBER", "unknown-property", "TEXT");
      Data data = mock(Data.class);
      PreparedStatement statement = mock(PreparedStatement.class);
      ResultSet resultSet = mock(ResultSet.class);

      doNothing().when(repository).load(statement, properties, TYPE_DATA, data);
      when(statement.executeQuery()).thenReturn(resultSet);

      ResultSet result = repository.executeQuery(statement, properties, TYPE_DATA, data); //test

      assertEquals(resultSet, result);
      InOrder inOrder = inOrder(repository, statement);
      inOrder.verify(repository).executeQuery(statement, properties, TYPE_DATA, data);
      inOrder.verify(repository).load(statement, properties, TYPE_DATA, data);
      inOrder.verify(statement).executeQuery();
      inOrder.verifyNoMoreInteractions();

      verifyNoMoreInteractions(repository, statement);
      verifyZeroInteractions(data);
   }

   @Test
   public void testExecuteQueryParameters() throws SQLException {
      PreparedStatement statement = mock(PreparedStatement.class);
      ResultSet resultSet = mock(ResultSet.class);
      Date date = new Date();

      doNothing().when(repository).load(statement, "TEXT", Math.PI, Boolean.FALSE, date);
      when(statement.executeQuery()).thenReturn(resultSet);

      ResultSet result = repository.executeQuery(statement, "TEXT", Math.PI, Boolean.FALSE, date); //test

      assertEquals(resultSet, result);
      InOrder inOrder = inOrder(repository, statement);
      inOrder.verify(repository).executeQuery(statement, "TEXT", Math.PI, Boolean.FALSE, date);
      inOrder.verify(repository).load(statement, "TEXT", Math.PI, Boolean.FALSE, date);
      inOrder.verify(statement).executeQuery();
      inOrder.verifyNoMoreInteractions();

      verifyNoMoreInteractions(repository, statement);
   }

   @Test
   public void testExecuteQueryParameterList() throws SQLException {
      PreparedStatement statement = mock(PreparedStatement.class);
      ResultSet resultSet = mock(ResultSet.class);
      Date date = new Date();
      List<Object> list  = Arrays.asList("TEXT", Math.PI, Boolean.FALSE, date);

      doNothing().when(repository).load(statement, list);
      when(statement.executeQuery()).thenReturn(resultSet);

      ResultSet result = repository.executeQuery(statement, list); //test

      assertEquals(resultSet, result);
      InOrder inOrder = inOrder(repository, statement);
      inOrder.verify(repository).executeQuery(statement, list);
      inOrder.verify(repository).load(statement, list);
      inOrder.verify(statement).executeQuery();
      inOrder.verifyNoMoreInteractions();

      verifyNoMoreInteractions(repository, statement);
   }

   @Test
   public void testExecuteUpdateParameters() throws SQLException {
      PreparedStatement statement = mock(PreparedStatement.class);
      Date date = new Date();
      int rows = (int) (Math.random() * 100);

      doNothing().when(repository).load(statement, "TEXT", Math.PI, Boolean.FALSE, date);
      when(statement.executeUpdate()).thenReturn(rows);

      int result = repository.executeUpdate(statement, "TEXT", Math.PI, Boolean.FALSE, date); //test

      assertEquals(rows, result);
      InOrder inOrder = inOrder(repository, statement);
      inOrder.verify(repository).executeUpdate(statement, "TEXT", Math.PI, Boolean.FALSE, date);
      inOrder.verify(repository).load(statement, "TEXT", Math.PI, Boolean.FALSE, date);
      inOrder.verify(statement).executeUpdate();
      inOrder.verifyNoMoreInteractions();

      verifyNoMoreInteractions(repository, statement);
   }

   @Test
   public void testExecuteUpdateParameterList() throws SQLException {
      PreparedStatement statement = mock(PreparedStatement.class);
      Date date = new Date();
      List<Object> list  = Arrays.asList("TEXT", Math.PI, Boolean.FALSE, date);
      int rows = (int) (Math.random() * 100);

      doNothing().when(repository).load(statement, list);
      when(statement.executeUpdate()).thenReturn(rows);

      int result = repository.executeUpdate(statement, list); //test

      assertEquals(rows, result);
      InOrder inOrder = inOrder(repository, statement);
      inOrder.verify(repository).executeUpdate(statement, list);
      inOrder.verify(repository).load(statement, list);
      inOrder.verify(statement).executeUpdate();
      inOrder.verifyNoMoreInteractions();

      verifyNoMoreInteractions(repository, statement);
   }
}
