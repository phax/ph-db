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
package com.helger.db.flyway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongSupplier;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsLinkedHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.db.api.EDatabaseSystemType;
import com.helger.db.api.config.IJdbcConfiguration;
import com.helger.db.api.config.JdbcConfiguration;
import com.helger.db.api.telemetry.CDBTelemetry;
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
 * Test class for the ph-telemetry integration of {@link FlywayMigrationRunner}. It runs a real
 * migration against an in-memory H2 database.
 *
 * @author Philip Helger
 */
public final class FlywayMigrationRunnerTelemetryTest
{
  private static final String JDBC_URL = "jdbc:h2:mem:phdbflywaytelemetry;DB_CLOSE_DELAY=-1";
  private static final String LOCATION = "db/migrate-test";

  // TODO ph-telemetry 1.0.2: replace the local test doubles below with com.helger.telemetry.mock.CapturingTelemetry
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

  @BeforeClass
  public static void installTelemetry ()
  {
    // Must happen before FlywayMetrics is class-loaded, because the instruments are resolved once
    // in its static initializer
    Telemetry.install (TRACER);
    TelemetryMetrics.install (METER);
  }

  @AfterClass
  public static void uninstallTelemetry ()
  {
    Telemetry.install (null);
    TelemetryMetrics.install (null);
  }

  @NonNull
  private static IJdbcConfiguration _createJdbcConfig ()
  {
    return new JdbcConfiguration (EDatabaseSystemType.H2.getID (),
                                  "org.h2.Driver",
                                  JDBC_URL,
                                  "sa",
                                  "",
                                  null,
                                  JdbcConfiguration.DEFAULT_EXECUTION_TIME_WARNING_ENABLED,
                                  JdbcConfiguration.DEFAULT_EXECUTION_TIME_WARNING_DURATION,
                                  JdbcConfiguration.DEFAULT_DEBUG_CONNECTIONS,
                                  JdbcConfiguration.DEFAULT_DEBUG_TRANSACTIONS,
                                  JdbcConfiguration.DEFAULT_DEBUG_SQL_STATEMENTS,
                                  JdbcConfiguration.DEFAULT_POOLING_MAX_CONNECTIONS,
                                  JdbcConfiguration.DEFAULT_POOLING_MAX_WAIT_DURATION,
                                  JdbcConfiguration.DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_DURATION,
                                  JdbcConfiguration.DEFAULT_POOLING_MIN_EVICTABLE_IDLE_DURATION,
                                  JdbcConfiguration.DEFAULT_POOLING_REMOVE_ABANDONED_DURATION,
                                  JdbcConfiguration.DEFAULT_JDBC_POOLING_TEST_ON_BORROW);
  }

  @Test
  public void testMigrate ()
  {
    final IFlywayConfiguration aFlywayConfig = FlywayConfiguration.builder ()
                                                                  .jdbcUrl (JDBC_URL)
                                                                  .jdbcUser ("sa")
                                                                  .jdbcPassword ("")
                                                                  .build ();
    FlywayMigrationRunner.runFlyway (_createJdbcConfig (),
                                     aFlywayConfig,
                                     LOCATION,
                                     new JavaMigration [0],
                                     new Callback [0]);

    assertEquals (1, TRACER.m_aSpans.size ());
    final CapturingSpan aSpan = TRACER.m_aSpans.getFirstOrNull ();
    assertNotNull (aSpan);
    assertEquals (CDBTelemetry.SPAN_FLYWAY_MIGRATE, aSpan.m_sName);
    assertEquals (ETelemetrySpanKind.CLIENT, aSpan.m_eKind);
    assertEquals (CDBTelemetry.COMPONENT_FLYWAY, aSpan.m_aAttrs.get (CDBTelemetry.ATTR_COMPONENT));
    assertEquals (EDatabaseSystemType.H2.getID (), aSpan.m_aAttrs.get (CDBTelemetry.ATTR_DB_SYSTEM_NAME));
    assertEquals (LOCATION, aSpan.m_aAttrs.get (CDBTelemetry.ATTR_FLYWAY_LOCATION));
    assertEquals (Boolean.FALSE, aSpan.m_aAttrs.get (CDBTelemetry.ATTR_FLYWAY_REPAIR_MODE));
    assertEquals (Long.valueOf (1), aSpan.m_aAttrs.get (CDBTelemetry.ATTR_FLYWAY_MIGRATIONS_EXECUTED));
    assertEquals (Boolean.TRUE, aSpan.m_aAttrs.get (CDBTelemetry.ATTR_SUCCESS));
    assertNull (aSpan.m_aException);
    assertTrue (aSpan.m_bClosed);

    final Measurement aMigrations = METER.m_aMeasurements.findFirst (x -> x.sInstrument ()
                                                                           .equals (CDBTelemetry.METRIC_FLYWAY_MIGRATIONS));
    assertNotNull (aMigrations);
    assertEquals (1, (long) aMigrations.dValue ());
    assertEquals (EDatabaseSystemType.H2.getID (), aMigrations.aAttrs ().get (CDBTelemetry.ATTR_DB_SYSTEM_NAME));

    final Measurement aDuration = METER.m_aMeasurements.findFirst (x -> x.sInstrument ()
                                                                         .equals (CDBTelemetry.METRIC_FLYWAY_MIGRATE_DURATION));
    assertNotNull (aDuration);
    assertEquals (Boolean.TRUE, aDuration.aAttrs ().get (CDBTelemetry.ATTR_SUCCESS));
    assertNull (aDuration.aAttrs ().get (CDBTelemetry.ATTR_ERROR_TYPE));
  }
}
