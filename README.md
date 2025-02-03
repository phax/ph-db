# ph-db

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.helger/ph-db-parent-pom/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.helger/ph-db-parent-pom) 
[![javadoc](https://javadoc.io/badge2/com.helger/ph-db-parent-pom/javadoc.svg)](https://javadoc.io/doc/com.helger/ph-db-parent-pom)
[![CodeCov](https://codecov.io/gh/phax/ph-db/branch/master/graph/badge.svg)](https://codecov.io/gh/phax/ph-db)

Java library with some common DB API, a special JDBC version and a JPA version based on EclipseLink.

Licensed under the Apache 2.0 license.

# Maven usage

Add the following to your pom.xml to use this artifact, where `x.y.z` is to be replaced with the last released version:

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-db-api</artifactId>
  <version>x.y.z</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-db-jdbc</artifactId>
  <version>x.y.z</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-db-jpa</artifactId>
  <version>x.y.z</version>
</dependency>
```

# News and noteworthy

* v7.0.7 - work in progress
    * Updated to Apache Commons Pool 2.12.1
* v7.0.6 - 2024-09-20
    * Updated to Protobuf 4.28.2 to fix CVE-2024-7254
* v7.0.5 - 2024-08-09
    * Updated to MySQLConnector/J 9.0.0
    * Updated to Protobuf 4.x
* v7.0.4 - 2024-03-27
    * Updated to ph-commons 11.1.5
    * Updated to MySQLConnector/J 8.3.0
    * Updated to Apache Commons DBCP 2.12.0
    * Created Java 21 compatibility
* v7.0.3 - 2023-12-10
    * Updated all dependencies
* v7.0.2 - 2023-07-31
    * Updated to ph-commons 11.1
* v7.0.1 - 2023-01-12
    * Updated to MySQLConnector/J 8.0.31 with new Maven coordinates
    * Updated to Protobuf 3.21.12
* v7.0.0 - 2023-01-09
    * Using Java 11 as the baseline
    * Updated to ph-commons 11
    * Updated to EclipseLink 4.0.0
* v6.7.4 - 2022-02-21
    * Updated to H2 2.0.210
    * Fixed a `NullPointerException` in CLOB handling
* v6.7.3 - 2021-11-24
    * Updated to MySQLConnector/J 8.0.25
    * Added new class `DBValueHelper`
    * Added new class `AbstractJDBCEnabledManager`
* v6.7.2 - 2021-09-19
    * Updated to Apache Commons Pool 2.11.1
    * Extended the `DBExecutor` API slightly
* v6.7.1 - 2021-08-20
    * Updated to ph-commons 10.1
    * Updated to MySQLConnector/J 8.0.25
    * Updated to Apache Commons DBCP 2.9.0
    * Updated to Apache Commons Pool 2.10.0
    * Extended `DBResultRow` with additional methods
* v6.7.0 - 2021-04-06
    * Added option to enabled/disable the connection state handling (and disabled it by default)
* v6.6.0 - 2021-03-21
    * Updated to ph-commons 10
    * Updated to EclipseLink 2.7.8
    * Updated to MySQLConnector/J 8.0.23
    * Added new class `ConnectionFromDataSource` that has increased flexibility
* v6.5.0 - 2020-11-27
    * Added conversion from CLOB to String - thanks to GG
    * Commented out the parameter check in favour of the default JDBC driver - thanks to GG for pointing that out
    * Removed the usage of `Optional` in the `DBExecutor` to more easily differentiate between "Error" and "Not found"
* v6.4.0 - 2020-11-02
    * Updated to MySQLConnector/J 8.0.21
    * Improved debug logging in `DBExecutor`
    * Made `DBExecutor` consistently not thread-safe
    * Made some `DBExectur` methods static
* v6.3.1 - 2020-09-30
    * Updated to Apache Commons Pool 2.9.0
    * Updated to Apache Commons DBCP 2.8.0
* v6.3.0 - 2020-08-24
    * Renamed `AbsractConnector` to `AbstractDBConnector`
    * Removed `AbstractDBConnector.getDatabaseName`
    * Added class `AbstractDBConnector`
    * Dropped some specific connector implementations
* v6.2.1 - 2020-08-20
    * Updated to EclipseLink 2.7.7
    * Updated to Apache Commons Pool 2.8.1
    * Updated DBResultRow API
* v6.2.0 - 2020-04-23
    * Updated to Apache Commons Pool 2.8.0
    * Updated to MySQLConnector/J 8.0.19
    * Updated to EclipseLink 2.7.6
    * Extended JDBCHelper return types
    * Added simple transaction support in `DBExecutor`
    * Updated to ph-commons 9.4.1
* v6.1.5 - 2019-10-25
    * Updated to Apache Commons Pool 2.7.0
    * Updated to Apache Commons DBCP 2.7.0
    * Updated to MySQLConnector/J 8.0.18
    * Updated to H2 1.4.200
    * Updated to EclipseLink 2.7.5
    * The `EclipseLinkLogger` logs all error levels below `WARNING` as `Info`
* v6.1.4 - 2019-03-27
    * Updated to H2 1.4.199
    * Replacing "javax.persistence 2.2.1" with "jakarta.persistence 2.2.2"
* v6.1.3 - 2019-03-12
    * Updated to EclipseLink 2.7.4
    * Updated to MySQLConnector/J 8.0.15
    * Updated to Apache Commons Pool 2.6.1
    * Updated to Apache Commons DBCP 2.6.0
    * Updated to H2 1.4.198
* v6.1.2 - 2018-11-22
    * Updated to EclipseLink 2.7.3
    * Updated to MySQLConnector/J 8.0.13
    * Updated to ph-commons 9.2.0
* v6.1.1 - 2018-07-24
    * Fixed OSGI ServiceProvider configuration
    * Updated to EclipseLink 2.7.2
    * Updated to Apache Commons DBCP 2.5.0
    * Updated to Apache Commons Pool 2.6.0
    * Catching an throwing Exception only (instead of Throwable)
* v6.1.0 - 2018-04-23
    * Updated to Apache Commons DBCP 2.2.0
    * Updated to EclipseLink 2.7.1
    * `JPAEnabledManager` now has the possibility to disable the execution time warning
* v6.0.0 - 2017-12-20
    * Updated to ph-commons 9.0.0
    * Updated to H2 1.4.196
    * Updated to EclipseLink 2.7.0
    * Updated to Apache Commons Pool2 2.5.0
* v5.0.1 - 2016-08-21
    * Updated to ph-commons 8.4.x
* v5.0.0 - 2016-06-11
    * Requires at least JDK8

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
It is appreciated if you star the GitHub project if you like it.