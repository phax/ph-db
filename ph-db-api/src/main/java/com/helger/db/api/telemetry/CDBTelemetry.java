/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.db.api.telemetry;

import com.helger.annotation.concurrent.Immutable;

/**
 * Constant span, metric and attribute names emitted by all ph-db modules through the vendor neutral
 * ph-telemetry facade. Centralised in <code>ph-db-api</code>, so that the JDBC, the JPA and the
 * Flyway module use literally the same names, and so that applications can reference them when
 * building dashboards, alerting rules or tests.
 * <p>
 * Where the OpenTelemetry database semantic conventions define a name, that name is used verbatim
 * (<code>db.*</code>, <code>error.type</code>) - everything that has no counterpart there is
 * namespaced with <code>phdb.</code>. Note that the <em>value</em> of {@link #ATTR_DB_SYSTEM_NAME}
 * is the ph-db {@link com.helger.db.api.EDatabaseSystemType} ID (e.g. <code>sqlserver</code>) and
 * not necessarily identical to the well-known value of the OpenTelemetry registry.
 * <p>
 * All duration instruments are recorded in <b>seconds</b>, because that is the unit the stable
 * {@link #METRIC_CLIENT_OPERATION_DURATION} convention prescribes.
 *
 * @author Philip Helger
 * @since 8.5.0
 */
@Immutable
public final class CDBTelemetry
{
  // === span names ===
  /**
   * Fallback span name for a single JDBC statement execution. It is only used, if the SQL operation
   * cannot be derived from the statement - otherwise the operation name (e.g. <code>SELECT</code>)
   * is the span name, as the OpenTelemetry conventions demand.
   */
  public static final String SPAN_JDBC_QUERY = "db.query";
  /** Span wrapping a single JDBC transaction - including all nested levels. */
  public static final String SPAN_JDBC_TRANSACTION = "phdb.jdbc.transaction";
  /** Span wrapping a single JPA transaction. */
  public static final String SPAN_JPA_TRANSACTION = "phdb.jpa.transaction";
  /** Span wrapping a single JPA select that is executed without a transaction. */
  public static final String SPAN_JPA_SELECT = "phdb.jpa.select";
  /** Span wrapping a whole Flyway migration run. */
  public static final String SPAN_FLYWAY_MIGRATE = "phdb.flyway.migrate";

  // === metric instrument names ===
  /**
   * Histogram (s): duration of a single database client operation - a JDBC statement or a JPA
   * transaction/select. This is the stable OpenTelemetry instrument, so its unit is seconds and not
   * milliseconds. Use {@link #ATTR_COMPONENT} to separate the JDBC from the JPA operations.
   */
  public static final String METRIC_CLIENT_OPERATION_DURATION = "db.client.operation.duration";
  /**
   * The description of {@link #METRIC_CLIENT_OPERATION_DURATION}. Shared by all modules, so that
   * the instrument is created identically no matter which module touches it first.
   */
  public static final String METRIC_CLIENT_OPERATION_DURATION_DESC = "Duration of a single database client operation";
  /** Counter: executed JDBC statements - successful and failed ones. */
  public static final String METRIC_JDBC_STATEMENTS = "phdb.jdbc.statements";
  /** Counter: finished JDBC transactions - committed, rolled back and nested ones. */
  public static final String METRIC_JDBC_TRANSACTIONS = "phdb.jdbc.transactions";
  /** Counter: JDBC connection acquisitions - successful and failed ones. */
  public static final String METRIC_JDBC_CONNECTIONS = "phdb.jdbc.connections";
  /** Up-down counter: JDBC connections currently in use by a <code>DBExecutor</code>. */
  public static final String METRIC_JDBC_CONNECTIONS_ACTIVE = "phdb.jdbc.connections.active";
  /** Histogram (s): time it took to acquire a JDBC connection from the connection provider. */
  public static final String METRIC_JDBC_CONNECTION_ACQUIRE_DURATION = "phdb.jdbc.connection.acquire.duration";
  /** Counter: executed JPA operations - successful and failed ones. */
  public static final String METRIC_JPA_OPERATIONS = "phdb.jpa.operations";
  /** Counter: migrations executed by Flyway. */
  public static final String METRIC_FLYWAY_MIGRATIONS = "phdb.flyway.migrations";
  /** Histogram (s): duration of a whole Flyway migration run. */
  public static final String METRIC_FLYWAY_MIGRATE_DURATION = "phdb.flyway.migrate.duration";

  // === attribute keys - OpenTelemetry semantic conventions ===
  /**
   * The database system, using the ph-db {@link com.helger.db.api.EDatabaseSystemType} ID as the
   * value. Only present, if the database system is known.
   */
  public static final String ATTR_DB_SYSTEM_NAME = "db.system.name";
  /** The database schema the statements are executed in. Only present, if it is known. */
  public static final String ATTR_DB_NAMESPACE = "db.namespace";
  /**
   * The SQL operation of the executed statement, e.g. <code>SELECT</code>. Only present, if it
   * could be derived from the statement. This is the low cardinality dimension to group metrics by.
   */
  public static final String ATTR_DB_OPERATION_NAME = "db.operation.name";
  /**
   * The SQL text of the executed statement. Only used as a span attribute - never as a metric
   * attribute, because its cardinality is unbounded. Emission can be disabled per
   * <code>DBExecutor</code>.
   */
  public static final String ATTR_DB_QUERY_TEXT = "db.query.text";
  /** The number of rows returned by a query. */
  public static final String ATTR_DB_RESPONSE_RETURNED_ROWS = "db.response.returned_rows";
  /** The fully qualified class name of the exception that made an operation fail. */
  public static final String ATTR_ERROR_TYPE = "error.type";

  // === attribute keys - ph-db specific ===
  /**
   * The ph-db module that emitted the telemetry - {@link #COMPONENT_JDBC}, {@link #COMPONENT_JPA}
   * or {@link #COMPONENT_FLYWAY}.
   */
  public static final String ATTR_COMPONENT = "phdb.component";
  /** Whether the operation was technically successful. */
  public static final String ATTR_SUCCESS = "phdb.success";
  /** Whether the statement was executed as a <code>PreparedStatement</code>. */
  public static final String ATTR_JDBC_PREPARED = "phdb.jdbc.prepared";
  /** The number of parameters passed to a <code>PreparedStatement</code>. */
  public static final String ATTR_JDBC_PARAMETER_COUNT = "phdb.jdbc.parameter.count";
  /** The number of rows inserted, updated or deleted by a statement. */
  public static final String ATTR_JDBC_UPDATED_ROWS = "phdb.jdbc.updated_rows";
  /** The internal ID of the transaction. Only used as a span attribute. */
  public static final String ATTR_JDBC_TRANSACTION_ID = "phdb.jdbc.transaction.id";
  /** The nesting level of the transaction - the outermost transaction has level 1. */
  public static final String ATTR_JDBC_TRANSACTION_LEVEL = "phdb.jdbc.transaction.level";
  /** Whether the transaction is nested inside another one - and therefore does not commit itself. */
  public static final String ATTR_JDBC_TRANSACTION_NESTED = "phdb.jdbc.transaction.nested";
  /**
   * How the transaction ended - {@link #TRANSACTION_OUTCOME_COMMITTED},
   * {@link #TRANSACTION_OUTCOME_ROLLED_BACK} or {@link #TRANSACTION_OUTCOME_NESTED}.
   */
  public static final String ATTR_JDBC_TRANSACTION_OUTCOME = "phdb.jdbc.transaction.outcome";
  /**
   * How the connection acquisition ended - {@link #CONNECTION_OUTCOME_ACQUIRED},
   * {@link #CONNECTION_OUTCOME_FAILED} or {@link #CONNECTION_OUTCOME_REFUSED}.
   */
  public static final String ATTR_JDBC_CONNECTION_OUTCOME = "phdb.jdbc.connection.outcome";
  /**
   * The kind of JPA operation - {@link #JPA_OPERATION_TRANSACTION} or
   * {@link #JPA_OPERATION_SELECT}.
   */
  public static final String ATTR_JPA_OPERATION = "phdb.jpa.operation";
  /**
   * Whether a new JPA transaction was started. It is <code>false</code>, if the operation joined an
   * already active transaction.
   */
  public static final String ATTR_JPA_TRANSACTION_STARTED = "phdb.jpa.transaction.started";
  /** The Flyway migration script location. */
  public static final String ATTR_FLYWAY_LOCATION = "phdb.flyway.location";
  /** Whether Flyway was run in repair mode. */
  public static final String ATTR_FLYWAY_REPAIR_MODE = "phdb.flyway.repair_mode";
  /** The number of migrations Flyway executed in this run. */
  public static final String ATTR_FLYWAY_MIGRATIONS_EXECUTED = "phdb.flyway.migrations.executed";
  /** The schema version before the migration run. */
  public static final String ATTR_FLYWAY_VERSION_INITIAL = "phdb.flyway.version.initial";
  /** The schema version after the migration run. */
  public static final String ATTR_FLYWAY_VERSION_TARGET = "phdb.flyway.version.target";

  // === attribute values ===
  /** {@link #ATTR_COMPONENT} value for the <code>ph-db-jdbc</code> module. */
  public static final String COMPONENT_JDBC = "jdbc";
  /** {@link #ATTR_COMPONENT} value for the <code>ph-db-jpa</code> module. */
  public static final String COMPONENT_JPA = "jpa";
  /** {@link #ATTR_COMPONENT} value for the <code>ph-db-flyway</code> module. */
  public static final String COMPONENT_FLYWAY = "flyway";
  /** {@link #ATTR_JDBC_TRANSACTION_OUTCOME} value for a committed transaction. */
  public static final String TRANSACTION_OUTCOME_COMMITTED = "committed";
  /** {@link #ATTR_JDBC_TRANSACTION_OUTCOME} value for a rolled back transaction. */
  public static final String TRANSACTION_OUTCOME_ROLLED_BACK = "rolled-back";
  /**
   * {@link #ATTR_JDBC_TRANSACTION_OUTCOME} value for a nested transaction, that neither committed
   * nor rolled back, because the outermost transaction does that.
   */
  public static final String TRANSACTION_OUTCOME_NESTED = "nested";
  /** {@link #ATTR_JDBC_CONNECTION_OUTCOME} value for a successfully acquired connection. */
  public static final String CONNECTION_OUTCOME_ACQUIRED = "acquired";
  /** {@link #ATTR_JDBC_CONNECTION_OUTCOME} value for a connection that could not be acquired. */
  public static final String CONNECTION_OUTCOME_FAILED = "failed";
  /**
   * {@link #ATTR_JDBC_CONNECTION_OUTCOME} value for a connection that was not even tried, because a
   * previous attempt already failed.
   */
  public static final String CONNECTION_OUTCOME_REFUSED = "refused";
  /** {@link #ATTR_JPA_OPERATION} value for an operation running in a transaction. */
  public static final String JPA_OPERATION_TRANSACTION = "transaction";
  /** {@link #ATTR_JPA_OPERATION} value for a select running without a transaction. */
  public static final String JPA_OPERATION_SELECT = "select";

  // === metric units ===
  /** Unit of all duration instruments - seconds, as the OpenTelemetry conventions demand. */
  public static final String UNIT_SECONDS = "s";
  /** Unit for counting SQL statements. */
  public static final String UNIT_STATEMENT = "{statement}";
  /** Unit for counting transactions. */
  public static final String UNIT_TRANSACTION = "{transaction}";
  /** Unit for counting connections. */
  public static final String UNIT_CONNECTION = "{connection}";
  /** Unit for counting JPA operations. */
  public static final String UNIT_OPERATION = "{operation}";
  /** Unit for counting Flyway migrations. */
  public static final String UNIT_MIGRATION = "{migration}";

  private CDBTelemetry ()
  {}
}
