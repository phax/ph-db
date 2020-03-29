# ph-db

Java library with some basic DB API, a special JDBC version and a JPA version based on EclipseLink.

# News and noteworthy

* v6.2.0 - work in progress
    * Updated to Apache Commons Pool 2.8.0
    * Updated to MySQLConnector/J 8.0.19
    * Updated to EclipseLink 2.7.6
    * Extended JDBCHelper return types
    * Added simple transaction support in `DBExecutor`
    * Updated to ph-commons 9.4.0
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

# Maven usage

Add the following to your pom.xml to use this artifact:

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-db-api</artifactId>
  <version>6.1.5</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-db-jdbc</artifactId>
  <version>6.1.5</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-db-jpa</artifactId>
  <version>6.1.5</version>
</dependency>
```

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a> |
Kindly supported by [YourKit Java Profiler](https://www.yourkit.com)