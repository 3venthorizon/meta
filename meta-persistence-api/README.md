# Meta Persistence API (MPA)
The Meta Persistence API provides a very light weight database interface that allows you to persist, retrieve, update
and delete table records mapped as Java class instances.

This [MPA](https://github.com/3venthorizon/meta/tree/master/meta-persistence-api/src/main/java/com/devlambda/meta/persistence/MPA.java)
is built on top of the JDBC API. Below is a list of key features supported:

* Maps between Java Class instances and database records or java.sql.ResultSets.
* [Connector](https://github.com/3venthorizon/meta/tree/master/meta-persistence-api/src/main/java/com/devlambda/meta/persistence/Connector.java)
 java.sql.Connection provider.
* [ConnectionPooling](https://github.com/3venthorizon/meta/tree/master/meta-persistence-api/src/main/java/com/devlambda/meta/persistence/ConnectionPool.java)
for Round Robin connection distribution and scaling.
* Named [Queries](https://github.com/3venthorizon/meta/tree/master/meta-persistence-api/src/main/java/com/devlambda/meta/persistence/Query.java)
 or cached java.sql.PreparedStatement per java.sql.Connection.
* Transparent [Transaction Management](https://github.com/3venthorizon/meta/tree/master/meta-persistence-api/src/main/java/com/devlambda/meta/persistence/TransactionManager.java)
with java.lang.Thread process java.sql.Connection affinity.
    * Split purpose auto-commit and transactional connection pools.
    * Transaction Planner 
    *** Operation Chaining
    *** Multi savepoint rollbacks
    *** Transaction forking
* [Repository](https://github.com/3venthorizon/meta/tree/master/meta-persistence-api/src/main/java/com/devlambda/meta/persistence/Repository.java) 
access to java.sql.* resources used without compromising database and session state.
