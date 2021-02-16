package com.devlambda.meta.persistence;


import java.sql.Connection;
import java.sql.Savepoint;
import java.util.function.Supplier;


/**
 * Transaction denotes the integration definition for controlling database transactions.
 */
public interface Transaction extends Supplier<Connection> {

   Connection begin();

   Savepoint setSavePoint();

   void rollback();

   void rollback(Savepoint savepoint);

   void commit();
   
   void close();
}
