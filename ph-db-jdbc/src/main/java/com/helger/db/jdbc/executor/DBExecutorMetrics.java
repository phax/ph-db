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
package com.helger.db.jdbc.executor;

import com.helger.annotation.concurrent.Immutable;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.telemetry.ITelemetryCounter;
import com.helger.telemetry.ITelemetryHistogram;
import com.helger.telemetry.ITelemetryUpDownCounter;
import com.helger.telemetry.TelemetryMetrics;

/**
 * Central registry of the named metric instruments emitted by {@link DBExecutor}. Each instrument
 * is created once at class-load time via the vendor neutral {@link TelemetryMetrics} facade - if no
 * {@code ITelemetryMeterSPI} is registered, the underlying instruments are cheap no-ops, so
 * referencing this class in a deployment without an observability backend has no cost.
 *
 * @author Philip Helger
 * @since 8.5.0
 */
@Immutable
public final class DBExecutorMetrics
{
  /** Duration of a single JDBC statement execution, by database system, operation and success. */
  public static final ITelemetryHistogram OPERATION_DURATION = TelemetryMetrics.histogram (CDBTelemetry.METRIC_CLIENT_OPERATION_DURATION,
                                                                                           CDBTelemetry.METRIC_CLIENT_OPERATION_DURATION_DESC,
                                                                                           CDBTelemetry.UNIT_SECONDS);

  /** Executed JDBC statements, by database system, operation and success. */
  public static final ITelemetryCounter STATEMENTS = TelemetryMetrics.counter (CDBTelemetry.METRIC_JDBC_STATEMENTS,
                                                                               "Executed JDBC statements - successful and failed ones",
                                                                               CDBTelemetry.UNIT_STATEMENT);

  /** Finished JDBC transactions, by database system and outcome. */
  public static final ITelemetryCounter TRANSACTIONS = TelemetryMetrics.counter (CDBTelemetry.METRIC_JDBC_TRANSACTIONS,
                                                                                 "Finished JDBC transactions - committed, rolled back and nested ones",
                                                                                 CDBTelemetry.UNIT_TRANSACTION);

  /** JDBC connection acquisitions, by database system and outcome. */
  public static final ITelemetryCounter CONNECTIONS = TelemetryMetrics.counter (CDBTelemetry.METRIC_JDBC_CONNECTIONS,
                                                                                "JDBC connection acquisitions - successful and failed ones",
                                                                                CDBTelemetry.UNIT_CONNECTION);

  /** JDBC connections currently in use by a {@link DBExecutor}, by database system. */
  public static final ITelemetryUpDownCounter CONNECTIONS_ACTIVE = TelemetryMetrics.upDownCounter (CDBTelemetry.METRIC_JDBC_CONNECTIONS_ACTIVE,
                                                                                                   "JDBC connections currently in use",
                                                                                                   CDBTelemetry.UNIT_CONNECTION);

  /** Time it took to acquire a JDBC connection, by database system and outcome. */
  public static final ITelemetryHistogram CONNECTION_ACQUIRE_DURATION = TelemetryMetrics.histogram (CDBTelemetry.METRIC_JDBC_CONNECTION_ACQUIRE_DURATION,
                                                                                                    "Time it took to acquire a JDBC connection from the connection provider",
                                                                                                    CDBTelemetry.UNIT_SECONDS);

  private DBExecutorMetrics ()
  {}
}
