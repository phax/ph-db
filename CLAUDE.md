# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules
mvn clean install

# Build single module
mvn clean install -pl ph-db-jdbc

# Run all tests
mvn test

# Run single test class
mvn test -pl ph-db-api -Dtest=EDatabaseSystemTypeTest

# Run single test method
mvn test -pl ph-db-api -Dtest=EDatabaseSystemTypeTest#testBasic
```

## Project Overview

ph-db is a multi-module Java database abstraction library (Java 17+, built with Java 21). Parent POM: `com.helger:parent-pom`. Uses ph-commons as the foundation library.

### Module Dependency Graph

```
ph-db-api          ← core abstractions, config interfaces, DB-specific helpers
  ├── ph-db-jdbc   ← JDBC executor, connection pooling (Commons DBCP2), DB-specific connectors
  ├── ph-db-jpa    ← JPA/EclipseLink manager, entity manager factories, type converters
  └── ph-db-flyway ← Flyway migration runner and configuration
```

### Key Classes

- **`EDatabaseSystemType`** (`ph-db-api`) — enum of supported databases (DB2, H2, MySQL, Oracle, PostgreSQL, SQLServer)
- **`IJdbcConfiguration`** / **`IJdbcDataSourceConfiguration`** (`ph-db-api`) — JDBC config interfaces including pooling params
- **`AbstractDBConnector`** (`ph-db-jdbc`) — template base for DB connectors; subclasses provide driver class, URL, credentials
- **`DBExecutor`** (`ph-db-jdbc`) — main JDBC query execution class with prepared statement support
- **`JPAEnabledManager`** (`ph-db-jpa`) — main JPA transaction executor with statistics tracking
- **`AbstractGlobalEntityManagerFactory`** (`ph-db-jpa`) — singleton base for EntityManagerFactory; DB-specific subclasses for H2 and MySQL
- **`FlywayMigrationRunner`** (`ph-db-flyway`) — utility class to run Flyway migrations

### Patterns

- **Template Method**: `AbstractDBConnector`, `AbstractGlobalEntityManagerFactory` — subclass to provide DB-specific details
- **Callback interfaces**: `IResultSetRowCallback`, `IPreparedStatementDataProvider` for JDBC result/parameter handling
- **Proxy wrappers**: `EntityManagerProxy` / `EntityManagerFactoryProxy` add listener support to JPA
- All modules are OSGi bundles with JPMS Automatic-Module-Name

## Testing

- JUnit 4 throughout
- SPI tests (`SPITest`) in each module verify ServiceLoader registration
- H2 used as in-memory test database
