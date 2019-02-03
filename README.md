# ph-db-api

Java library with some basic DB API, a special JDBC version and a JPA version based on EclipseLink.

# News and noteworthy

* v6.1.3 - work in progress
    * Updated to EclipseLink 2.7.4
    * Updated to MySQLConnector/J 8.0.15
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
  <version>6.1.2</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-db-jdbc</artifactId>
  <version>6.1.2</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-db-jpa</artifactId>
  <version>6.1.2</version>
</dependency>
```

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>
