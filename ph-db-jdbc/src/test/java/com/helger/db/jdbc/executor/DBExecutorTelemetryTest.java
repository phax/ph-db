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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongSupplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.db.api.EDatabaseSystemType;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.db.jdbc.IHasConnection;
import com.helger.db.jdbc.callback.ConstantPreparedStatementDataProvider;
import com.helger.telemetry.ETelemetrySpanKind;
import com.helger.telemetry.ITelemetryCounter;
import com.helger.telemetry.ITelemetryGauge;
import com.helger.telemetry.ITelemetryHistogram;
import com.helger.telemetry.ITelemetryMeterSPI;
import com.helger.telemetry.ITelemetrySpan;
import com.helger.telemetry.ITelemetryTracerSPI;
import com.helger.telemetry.ITelemetryUpDownCounter;
import com.helger.telemetry.Telemetry;
import com.helger.telemetry.TelemetryAttributes;
import com.helger.telemetry.TelemetryMetrics;

/**
 * Test class for the ph-telemetry integration of {@link DBExecutor}.
 *
 * @author Philip Helger
 */
public final class DBExecutorTelemetryTest
{
  private static final String JDBC_URL = "jdbc:h2:mem:phdbtelemetry;DB_CLOSE_DELAY=-1";
  private static final String TABLE = "test_telemetry";

  /** A span that only records what was set on it. */
  private static final class CapturingSpan implements ITelemetrySpan
  {
    private final String m_sName;
    private final ETelemetrySpanKind m_eKind;
    private final ICommonsOrderedMap <String, Object> m_aAttrs = new CommonsLinkedHashMap <> ();
    private Throwable m_aException;
    private boolean m_bClosed;

    CapturingSpan (@NonNull final String sName, @NonNull final ETelemetrySpanKind eKind)
    {
      m_sName = sName;
      m_eKind = eKind;
    }

    @NonNull
    public ITelemetrySpan setAttribute (@NonNull final String sKey, @Nullable final String sValue)
    {
      if (sValue != null)
        m_aAttrs.put (sKey, sValue);
      return this;
    }

    @NonNull
    public ITelemetrySpan setAttribute (@NonNull final String sKey, final boolean bValue)
    {
      m_aAttrs.put (sKey, Boolean.valueOf (bValue));
      return this;
    }

    @NonNull
    public ITelemetrySpan setAttribute (@NonNull final String sKey, final long nValue)
    {
      m_aAttrs.put (sKey, Long.valueOf (nValue));
      return this;
    }

    @NonNull
    public ITelemetrySpan setAttribute (@NonNull final String sKey, final double dValue)
    {
      m_aAttrs.put (sKey, Double.valueOf (dValue));
      return this;
    }

    @NonNull
    public ITelemetrySpan recordException (@NonNull final Throwable aException)
    {
      m_aException = aException;
      return this;
    }

    @NonNull
    public ITelemetrySpan addEvent (@NonNull final String sName, @NonNull final TelemetryAttributes aAttributes)
    {
      return this;
    }

    @NonNull
    public ITelemetrySpan setStatusOk ()
    {
      return this;
    }

    @NonNull
    public ITelemetrySpan setStatusError (@Nullable final String sMessage)
    {
      return this;
    }

    public void close ()
    {
      m_bClosed = true;
    }
  }

  private static final class CapturingTracer implements ITelemetryTracerSPI
  {
    private final ICommonsList <CapturingSpan> m_aSpans = new CommonsArrayList <> (new CopyOnWriteArrayList <> ());

    @NonNull
    public ITelemetrySpan startSpan (@NonNull final String sName, @NonNull final ETelemetrySpanKind eKind)
    {
      final CapturingSpan ret = new CapturingSpan (sName, eKind);
      m_aSpans.add (ret);
      return ret;
    }
  }

  private record Measurement (String sInstrument, double dValue, ICommonsOrderedMap <String, Object> aAttrs)
  {}

  private static final class CapturingMeter implements ITelemetryMeterSPI
  {
    private final ICommonsList <Measurement> m_aMeasurements = new CommonsArrayList <> (new CopyOnWriteArrayList <> ());

    @NonNull
    private static ICommonsOrderedMap <String, Object> _toMap (@NonNull final TelemetryAttributes aAttrs)
    {
      final ICommonsOrderedMap <String, Object> ret = new CommonsLinkedHashMap <> ();
      aAttrs.forEach (new TelemetryAttributes.IVisitor ()
      {
        public void onString (@NonNull final String sKey, @NonNull final String sValue)
        {
          ret.put (sKey, sValue);
        }

        public void onLong (@NonNull final String sKey, final long nValue)
        {
          ret.put (sKey, Long.valueOf (nValue));
        }

        public void onDouble (@NonNull final String sKey, final double dValue)
        {
          ret.put (sKey, Double.valueOf (dValue));
        }

        public void onBoolean (@NonNull final String sKey, final boolean bValue)
        {
          ret.put (sKey, Boolean.valueOf (bValue));
        }
      });
      return ret;
    }

    private void _record (@NonNull final String sName, final double dValue, @NonNull final TelemetryAttributes aAttrs)
    {
      m_aMeasurements.add (new Measurement (sName, dValue, _toMap (aAttrs)));
    }

    @NonNull
    public ITelemetryCounter createCounter (@NonNull final String sName,
                                            @Nullable final String sDescription,
                                            @Nullable final String sUnit)
    {
      return (nValue, aAttrs) -> _record (sName, nValue, aAttrs);
    }

    @NonNull
    public ITelemetryUpDownCounter createUpDownCounter (@NonNull final String sName,
                                                        @Nullable final String sDescription,
                                                        @Nullable final String sUnit)
    {
      return (nValue, aAttrs) -> _record (sName, nValue, aAttrs);
    }

    @NonNull
    public ITelemetryHistogram createHistogram (@NonNull final String sName,
                                                @Nullable final String sDescription,
                                                @Nullable final String sUnit)
    {
      return (dValue, aAttrs) -> _record (sName, dValue, aAttrs);
    }

    @NonNull
    public ITelemetryGauge createGauge (@NonNull final String sName,
                                        @Nullable final String sDescription,
                                        @Nullable final String sUnit,
                                        @NonNull final LongSupplier aSupplier)
    {
      return () -> {};
    }
  }

  private static final CapturingTracer TRACER = new CapturingTracer ();
  private static final CapturingMeter METER = new CapturingMeter ();

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
    Telemetry.install (TRACER);
    TelemetryMetrics.install (METER);

    _createExecutor ().executeStatement ("CREATE TABLE IF NOT EXISTS " + TABLE + " (id INTEGER, name VARCHAR(100))");
  }

  @AfterClass
  public static void uninstallTelemetry ()
  {
    _createExecutor ().executeStatement ("DROP TABLE IF EXISTS " + TABLE);

    Telemetry.install (null);
    TelemetryMetrics.install (null);
  }

  @Before
  public void clearRecordings ()
  {
    TRACER.m_aSpans.clear ();
    METER.m_aMeasurements.clear ();
  }

  @Nullable
  private static CapturingSpan _findSpan (@NonNull final String sName)
  {
    return TRACER.m_aSpans.findFirst (x -> x.m_sName.equals (sName));
  }

  @Nullable
  private static Measurement _findMeasurement (@NonNull final String sInstrument)
  {
    return METER.m_aMeasurements.findFirst (x -> x.sInstrument ().equals (sInstrument));
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

    final CapturingSpan aSpan = _findSpan ("DELETE");
    assertNotNull (aSpan);
    assertEquals (ETelemetrySpanKind.CLIENT, aSpan.m_eKind);
    assertEquals (CDBTelemetry.COMPONENT_JDBC, aSpan.m_aAttrs.get (CDBTelemetry.ATTR_COMPONENT));
    assertEquals (EDatabaseSystemType.H2.getID (), aSpan.m_aAttrs.get (CDBTelemetry.ATTR_DB_SYSTEM_NAME));
    assertEquals ("DELETE", aSpan.m_aAttrs.get (CDBTelemetry.ATTR_DB_OPERATION_NAME));
    assertEquals (sSQL, aSpan.m_aAttrs.get (CDBTelemetry.ATTR_DB_QUERY_TEXT));
    assertEquals (Boolean.FALSE, aSpan.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_PREPARED));
    assertNull (aSpan.m_aException);
    assertTrue (aSpan.m_bClosed);

    final Measurement aStatements = _findMeasurement (CDBTelemetry.METRIC_JDBC_STATEMENTS);
    assertNotNull (aStatements);
    assertEquals (1, (long) aStatements.dValue ());
    assertEquals ("DELETE", aStatements.aAttrs ().get (CDBTelemetry.ATTR_DB_OPERATION_NAME));
    assertEquals (Boolean.TRUE, aStatements.aAttrs ().get (CDBTelemetry.ATTR_SUCCESS));
    // The SQL text is unbounded and must never become a metric dimension
    assertNull (aStatements.aAttrs ().get (CDBTelemetry.ATTR_DB_QUERY_TEXT));
    assertNotNull (_findMeasurement (CDBTelemetry.METRIC_CLIENT_OPERATION_DURATION));

    final Measurement aConnections = _findMeasurement (CDBTelemetry.METRIC_JDBC_CONNECTIONS);
    assertNotNull (aConnections);
    assertEquals (CDBTelemetry.CONNECTION_OUTCOME_ACQUIRED,
                  aConnections.aAttrs ().get (CDBTelemetry.ATTR_JDBC_CONNECTION_OUTCOME));
    assertNotNull (_findMeasurement (CDBTelemetry.METRIC_JDBC_CONNECTION_ACQUIRE_DURATION));
    assertNotNull (_findMeasurement (CDBTelemetry.METRIC_JDBC_CONNECTIONS_ACTIVE));
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
    final CapturingSpan aInsert = _findSpan ("INSERT");
    assertNotNull (aInsert);
    assertEquals (Boolean.TRUE, aInsert.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_PREPARED));
    assertEquals (Long.valueOf (2), aInsert.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_PARAMETER_COUNT));
    assertEquals (Long.valueOf (1), aInsert.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_UPDATED_ROWS));

    clearRecordings ();

    // Query without parameters
    assertEquals (1, aExecutor.queryAll ("SELECT id, name FROM " + TABLE).size ());
    final CapturingSpan aSelect = _findSpan ("SELECT");
    assertNotNull (aSelect);
    assertEquals (Long.valueOf (1), aSelect.m_aAttrs.get (CDBTelemetry.ATTR_DB_RESPONSE_RETURNED_ROWS));

    clearRecordings ();

    // Query with parameters
    assertEquals (1,
                  aExecutor.queryAll ("SELECT id, name FROM " + TABLE + " WHERE id = ?",
                                      new ConstantPreparedStatementDataProvider (Integer.valueOf (1))).size ());
    final CapturingSpan aPreparedSelect = _findSpan ("SELECT");
    assertNotNull (aPreparedSelect);
    assertEquals (Boolean.TRUE, aPreparedSelect.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_PREPARED));
    assertEquals (Long.valueOf (1), aPreparedSelect.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_PARAMETER_COUNT));
    assertEquals (Long.valueOf (1), aPreparedSelect.m_aAttrs.get (CDBTelemetry.ATTR_DB_RESPONSE_RETURNED_ROWS));
  }

  @Test
  public void testTransactionCommitted ()
  {
    final DBExecutor aExecutor = _createExecutor ();
    assertTrue (aExecutor.performInTransaction ( () -> aExecutor.executeStatement ("DELETE FROM " + TABLE))
                         .isSuccess ());

    final CapturingSpan aSpan = _findSpan (CDBTelemetry.SPAN_JDBC_TRANSACTION);
    assertNotNull (aSpan);
    assertEquals (Long.valueOf (1), aSpan.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_TRANSACTION_LEVEL));
    assertEquals (Boolean.FALSE, aSpan.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_TRANSACTION_NESTED));
    assertEquals (CDBTelemetry.TRANSACTION_OUTCOME_COMMITTED,
                  aSpan.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME));
    assertTrue (aSpan.m_bClosed);

    final Measurement aTransactions = _findMeasurement (CDBTelemetry.METRIC_JDBC_TRANSACTIONS);
    assertNotNull (aTransactions);
    assertEquals (CDBTelemetry.TRANSACTION_OUTCOME_COMMITTED,
                  aTransactions.aAttrs ().get (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME));
  }

  @Test
  public void testTransactionRolledBack ()
  {
    final DBExecutor aExecutor = _createExecutor ();
    assertTrue (aExecutor.performInTransaction ( () -> {
      aExecutor.executeStatement ("DELETE FROM " + TABLE);
      throw new IllegalStateException ("Test exception");
    }).isFailure ());

    final CapturingSpan aSpan = _findSpan (CDBTelemetry.SPAN_JDBC_TRANSACTION);
    assertNotNull (aSpan);
    assertEquals (CDBTelemetry.TRANSACTION_OUTCOME_ROLLED_BACK,
                  aSpan.m_aAttrs.get (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME));
    assertNotNull (aSpan.m_aException);

    final Measurement aTransactions = _findMeasurement (CDBTelemetry.METRIC_JDBC_TRANSACTIONS);
    assertNotNull (aTransactions);
    assertEquals (CDBTelemetry.TRANSACTION_OUTCOME_ROLLED_BACK,
                  aTransactions.aAttrs ().get (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME));
  }

  @Test
  public void testStatementFailure ()
  {
    // Syntactically valid, but the table does not exist
    assertTrue (_createExecutor ().executeStatement ("DELETE FROM this_table_does_not_exist").isFailure ());

    final CapturingSpan aSpan = _findSpan ("DELETE");
    assertNotNull (aSpan);
    assertNotNull (aSpan.m_aException);

    final Measurement aStatements = _findMeasurement (CDBTelemetry.METRIC_JDBC_STATEMENTS);
    assertNotNull (aStatements);
    assertEquals (Boolean.FALSE, aStatements.aAttrs ().get (CDBTelemetry.ATTR_SUCCESS));
    assertNotNull (aStatements.aAttrs ().get (CDBTelemetry.ATTR_ERROR_TYPE));
  }

  @Test
  public void testTelemetryDisabled ()
  {
    assertTrue (_createExecutor ().setTelemetry (false).executeStatement ("DELETE FROM " + TABLE).isSuccess ());

    assertTrue (TRACER.m_aSpans.isEmpty ());
    assertTrue (METER.m_aMeasurements.isEmpty ());
  }

  @Test
  public void testSQLTextDisabled ()
  {
    assertTrue (_createExecutor ().setTelemetrySQLText (false).executeStatement ("DELETE FROM " + TABLE).isSuccess ());

    final CapturingSpan aSpan = _findSpan ("DELETE");
    assertNotNull (aSpan);
    assertEquals ("DELETE", aSpan.m_aAttrs.get (CDBTelemetry.ATTR_DB_OPERATION_NAME));
    assertNull (aSpan.m_aAttrs.get (CDBTelemetry.ATTR_DB_QUERY_TEXT));
  }
}
