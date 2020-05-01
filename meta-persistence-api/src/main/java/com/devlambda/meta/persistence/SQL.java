package com.devlambda.meta.persistence;


import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.List;


/**
 * SQL is an utility class to create basic SQL queries.
 *
 * @author Dewald Pretorius
 */
public final class SQL {

   private SQL() {}

   public static String select(String delimiter, String... columns) {
      return select(delimiter, Arrays.asList(columns));
   }

   public static String select(String delimiter, List<String> columns) {
      return columns.stream().collect(joining(delimiter + ", " + delimiter, "SELECT " + delimiter, delimiter + " "));
   }

   public static String where(String delimiter, String... columns) {
      return where(delimiter, Arrays.asList(columns));
   }

   public static String where(String delimiter, List<String> columns) {
      return columns.stream()
                    .collect(joining(delimiter + " = ? AND " + delimiter, " WHERE " + delimiter, delimiter +  " = ? "));
   }

   public static String insert(String table, String delimiter, String... columns) {
      return insert(table, delimiter, Arrays.asList(columns));
   }

   public static String insert(String table, String delimiter, List<String> columns) {
      StringBuilder insertBuilder = new StringBuilder("INSERT INTO ");
      insertBuilder.append(table);
      String parameters = 
            columns.stream().collect(joining(delimiter + ", " + delimiter, "(" + delimiter, delimiter + ") "));
      insertBuilder.append(parameters);
      String valuePlaceholders = columns.stream().map(column -> "?").collect(joining(", ", " VALUES(", ") "));
      insertBuilder.append(valuePlaceholders);

      return insertBuilder.toString();
   }
}
