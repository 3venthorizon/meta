package com.devlambda.meta.persistence;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Supplier;


/**
 * Connector provides a thin wrapper for the {@link DriverManager} to create {@link Connection}s and {@link Connection}
 * providers.
 */
public final class Connector {
   
   protected static final String ERROR = "Failed to create database connection";
   
   private Connector() { }

   public static Supplier<Connection> connectionProvider(String url) {
      return () -> getConnection(url);
   }
   
   public static Supplier<Connection> connectionProvider(String url, Properties properties) {
      return () -> getConnection(url, properties);
   }
   
   public static Supplier<Connection> connectionProvider(String url, String username, String password) {
      return () -> getConnection(url, username, password);
   }
   
   public static Connection getConnection(String url) {
      try {
         return DriverManager.getConnection(url);
      } catch (SQLException sqle) {
         throw new RuntimeException(ERROR, sqle);
      }
   }
   
   public static Connection getConnection(String url, Properties properties) {
      try {
         return DriverManager.getConnection(url, properties);
      } catch (SQLException sqle) {
         throw new RuntimeException(ERROR, sqle);
      }
   }
   
   public static Connection getConnection(String url, String username, String password) {
      try {
         return DriverManager.getConnection(url, username, password);
      } catch (SQLException sqle) {
         throw new RuntimeException(ERROR, sqle);
      }
   }
}
