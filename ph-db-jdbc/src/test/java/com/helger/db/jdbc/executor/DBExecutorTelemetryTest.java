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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jspecify.annotations.NonNull;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.helger.db.api.EDatabaseSystemType;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.db.jdbc.IHasConnection;
import com.helger.db.jdbc.callback.ConstantPreparedStatementDataProvider;
import com.helger.telemetry.ETelemetrySpanKind;
import com.helger.telemetry.mock.CapturingTelemetry;
import com.helger.telemetry.mock.CapturingTelemetry.CapturedMeasurement;
import com.helger.telemetry.mock.CapturingTelemetry.CapturedSpan;

/**
 * Test class for the ph-telemetry integration of {@link DBExecutor}.
 *
 * @author Philip Helger
 */
public final class DBExecutorTelemetryTest
{
  private static final String JDBC_URL = "jdbc:h2:mem:phdbtelemetry;DB_CLOSE_DELAY=-1";
  private static final String TABLE = "test_telemetry";
  private static final CapturingTelemetry CT = new CapturingTelemetry ();

  @NonNull
  private static DBExecutor _createExecutor ()
  {
    final IHasConnection aConnectionProvider = () -> {
      try
      {
        final Connection ret = DriverManager.getConnection (JDBC_URL, "sa", "");
        ret.setAutoCommit (false);
        return ret;
      }
      catch (final SQLException ex)
      {
        throw new DBNoConnectionException ("Failed to connect to '" + JDBC_URL + "'", ex);
      }
    };
    return new DBExecutor (aConnectionProvider).setDatabaseSystemType (EDatabaseSystemType.H2);
  }

  @BeforeClass
  public static void installTelemetry ()
  {
    // Must happen before DBExecutorMetrics is class-loaded, because the instruments are resolved
    // once in its static initializer
    CT.install ();

    _createExecutor ().executeStatement ("CREATE TABLE IF NOT EXISTS " + TABLE + " (id INTEGER, name VARCHAR(100))");
  }

  @AfterClass
  public static void uninstallTelemetry ()
  {
    _createExecutor ().executeStatement ("DROP TABLE IF EXISTS " + TABLE);

    CapturingTelemetry.uninstall ();
  }

  @Before
  public void clearRecordings ()
  {
    CT.reset ();
  }

  @Test
  public void testGetOperationName ()
  {
    assertEquals ("SELECT", DBExecutorTelemetry.getOperationName ("select 1"));
    assertEquals ("INSERT", DBExecutorTelemetry.getOperationName ("  \n  INSERT INTO x (a) VALUES (?)"));
    assertEquals ("WITH", DBExecutorTelemetry.getOperationName ("WITH x AS (SELECT 1) SELECT * FROM x"));

    // Not a known operation - the metric dimension must stay bounded
    assertNull (DBExecutorTelemetry.getOperationName ("FROBNICATE x"));
    assertNull (DBExecutorTelemetry.getOperationName ("/* comment */ SELECT 1"));
    assertNull (DBExecutorTelemetry.getOperationName (""));
  }

  @Test
  public void testStatement ()
  {
    final String sSQL = "DELETE FROM " + TABLE;
    assertTrue (_createExecutor ().executeStatement (sSQL).isSuccess ());

    final CapturedSpan aSpan = CT.getFirstSpan ("DELETE");
    assertNotNull (aSpan);
    assertEquals (ETelemetrySpanKind.CLIENT, aSpan.getKind ());
    assertEquals (CDBTelemetry.COMPONENT_JDBC, aSpan.getAttribute (CDBTelemetry.ATTR_COMPONENT));
    assertEquals (EDatabaseSystemType.H2.getID (), aSpan.getAttribute (CDBTelemetry.ATTR_DB_SYSTEM_NAME));
    assertEquals ("DELETE", aSpan.getAttribute (CDBTelemetry.ATTR_DB_OPERATION_NAME));
    assertEquals (sSQL, aSpan.getAttribute (CDBTelemetry.ATTR_DB_QUERY_TEXT));
    assertEquals (Boolean.FALSE, aSpan.getAttribute (CDBTelemetry.ATTR_JDBC_PREPARED));
    assertNull (aSpan.getRecordedException ());
    assertTrue (aSpan.isClosed ());

    final CapturedMeasurement aStatements = CT.getFirstMeasurement (CDBTelemetry.METRIC_JDBC_STATEMENTS);
    assertNotNull (aStatements);
    assertEquals (1, aStatements.getValueAsLong ());
    assertEquals ("DELETE", aStatements.getAttribute (CDBTelemetry.ATTR_DB_OPERATION_NAME));
    assertEquals (Boolean.TRUE, aStatements.getAttribute (CDBTelemetry.ATTR_SUCCESS));
    // The SQL text is unbounded and must never become a metric dimension
    assertNull (aStatements.getAttribute (CDBTelemetry.ATTR_DB_QUERY_TEXT));
    assertNotNull (CT.getFirstMeasurement (CDBTelemetry.METRIC_CLIENT_OPERATION_DURATION));

    final CapturedMeasurement aConnections = CT.getFirstMeasurement (CDBTelemetry.METRIC_JDBC_CONNECTIONS);
    assertNotNull (aConnections);
    assertEquals (CDBTelemetry.CONNECTION_OUTCOME_ACQUIRED,
                  aConnections.getAttribute (CDBTelemetry.ATTR_JDBC_CONNECTION_OUTCOME));
    assertNotNull (CT.getFirstMeasurement (CDBTelemetry.METRIC_JDBC_CONNECTION_ACQUIRE_DURATION));
    assertNotNull (CT.getFirstMeasurement (CDBTelemetry.METRIC_JDBC_CONNECTIONS_ACTIVE));
  }

  @Test
  public void testPreparedStatementAndQuery ()
  {
    final DBExecutor aExecutor = _createExecutor ();
    assertTrue (aExecutor.executeStatement ("DELETE FROM " + TABLE).isSuccess ());
    clearRecordings ();

    assertEquals (1,
                  aExecutor.insertOrUpdateOrDelete ("INSERT INTO " + TABLE + " (id, name) VALUES (?, ?)",
                                                    new ConstantPreparedStatementDataProvider (Integer.valueOf (1),
                                                                                               "foo")));
    final CapturedSpan aInsert = CT.getFirstSpan ("INSERT");
    assertNotNull (aInsert);
    assertEquals (Boolean.TRUE, aInsert.getAttribute (CDBTelemetry.ATTR_JDBC_PREPARED));
    assertEquals (Long.valueOf (2), aInsert.getAttribute (CDBTelemetry.ATTR_JDBC_PARAMETER_COUNT));
    assertEquals (Long.valueOf (1), aInsert.getAttribute (CDBTelemetry.ATTR_JDBC_UPDATED_ROWS));

    clearRecordings ();

    // Query without parameters
    assertEquals (1, aExecutor.queryAll ("SELECT id, name FROM " + TABLE).size ());
    final CapturedSpan aSelect = CT.getFirstSpan ("SELECT");
    assertNotNull (aSelect);
    assertEquals (Long.valueOf (1), aSelect.getAttribute (CDBTelemetry.ATTR_DB_RESPONSE_RETURNED_ROWS));

    clearRecordings ();

    // Query with parameters
    assertEquals (1,
                  aExecutor.queryAll ("SELECT id, name FROM " + TABLE + " WHERE id = ?",
                                      new ConstantPreparedStatementDataProvider (Integer.valueOf (1))).size ());
    final CapturedSpan aPreparedSelect = CT.getFirstSpan ("SELECT");
    assertNotNull (aPreparedSelect);
    assertEquals (Boolean.TRUE, aPreparedSelect.getAttribute (CDBTelemetry.ATTR_JDBC_PREPARED));
    assertEquals (Long.valueOf (1), aPreparedSelect.getAttribute (CDBTelemetry.ATTR_JDBC_PARAMETER_COUNT));
    assertEquals (Long.valueOf (1), aPreparedSelect.getAttribute (CDBTelemetry.ATTR_DB_RESPONSE_RETURNED_ROWS));
  }

  @Test
  public void testTransactionCommitted ()
  {
    final DBExecutor aExecutor = _createExecutor ();
    assertTrue (aExecutor.performInTransaction (() -> aExecutor.executeStatement ("DELETE FROM " + TABLE))
                         .isSuccess ());

    final CapturedSpan aSpan = CT.getFirstSpan (CDBTelemetry.SPAN_JDBC_TRANSACTION);
    assertNotNull (aSpan);
    assertEquals (Long.valueOf (1), aSpan.getAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_LEVEL));
    assertEquals (Boolean.FALSE, aSpan.getAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_NESTED));
    assertEquals (CDBTelemetry.TRANSACTION_OUTCOME_COMMITTED,
                  aSpan.getAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME));
    assertTrue (aSpan.isClosed ());

    final CapturedMeasurement aTransactions = CT.getFirstMeasurement (CDBTelemetry.METRIC_JDBC_TRANSACTIONS);
    assertNotNull (aTransactions);
    assertEquals (CDBTelemetry.TRANSACTION_OUTCOME_COMMITTED,
                  aTransactions.getAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME));
  }

  @Test
  public void testTransactionRolledBack ()
  {
    final DBExecutor aExecutor = _createExecutor ();
    assertTrue (aExecutor.performInTransaction (() -> {
      aExecutor.executeStatement ("DELETE FROM " + TABLE);
      throw new IllegalStateException ("Test exception");
    }).isFailure ());

    final CapturedSpan aSpan = CT.getFirstSpan (CDBTelemetry.SPAN_JDBC_TRANSACTION);
    assertNotNull (aSpan);
    assertEquals (CDBTelemetry.TRANSACTION_OUTCOME_ROLLED_BACK,
                  aSpan.getAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME));
    assertNotNull (aSpan.getRecordedException ());

    final CapturedMeasurement aTransactions = CT.getFirstMeasurement (CDBTelemetry.METRIC_JDBC_TRANSACTIONS);
    assertNotNull (aTransactions);
    assertEquals (CDBTelemetry.TRANSACTION_OUTCOME_ROLLED_BACK,
                  aTransactions.getAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME));
  }

  @Test
  public void testStatementFailure ()
  {
    // Syntactically valid, but the table does not exist
    assertTrue (_createExecutor ().executeStatement ("DELETE FROM this_table_does_not_exist").isFailure ());

    final CapturedSpan aSpan = CT.getFirstSpan ("DELETE");
    assertNotNull (aSpan);
    assertNotNull (aSpan.getRecordedException ());

    final CapturedMeasurement aStatements = CT.getFirstMeasurement (CDBTelemetry.METRIC_JDBC_STATEMENTS);
    assertNotNull (aStatements);
    assertEquals (Boolean.FALSE, aStatements.getAttribute (CDBTelemetry.ATTR_SUCCESS));
    assertNotNull (aStatements.getAttribute (CDBTelemetry.ATTR_ERROR_TYPE));
  }

  @Test
  public void testTelemetryDisabled ()
  {
    assertTrue (_createExecutor ().setTelemetry (false).executeStatement ("DELETE FROM " + TABLE).isSuccess ());

    assertEquals (0, CT.getSpanCount ());
    assertTrue (CT.getMeasurements ().isEmpty ());
  }

  @Test
  public void testSQLTextDisabled ()
  {
    assertTrue (_createExecutor ().setTelemetrySQLText (false).executeStatement ("DELETE FROM " + TABLE).isSuccess ());

    final CapturedSpan aSpan = CT.getFirstSpan ("DELETE");
    assertNotNull (aSpan);
    assertEquals ("DELETE", aSpan.getAttribute (CDBTelemetry.ATTR_DB_OPERATION_NAME));
    assertNull (aSpan.getAttribute (CDBTelemetry.ATTR_DB_QUERY_TEXT));
  }
}
