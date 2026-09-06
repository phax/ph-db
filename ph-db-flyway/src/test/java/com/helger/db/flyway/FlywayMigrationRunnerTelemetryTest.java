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

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.jspecify.annotations.NonNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.helger.db.api.EDatabaseSystemType;
import com.helger.db.api.config.IJdbcConfiguration;
import com.helger.db.api.config.JdbcConfiguration;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.telemetry.ETelemetrySpanKind;
import com.helger.telemetry.mock.CapturingTelemetry;
import com.helger.telemetry.mock.CapturingTelemetry.CapturedMeasurement;
import com.helger.telemetry.mock.CapturingTelemetry.CapturedSpan;

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

  private static final CapturingTelemetry CT = new CapturingTelemetry ();

  @BeforeClass
  public static void installTelemetry ()
  {
    // Must happen before FlywayMetrics is class-loaded, because the instruments are resolved once
    // in its static initializer
    CT.install ();
  }

  @AfterClass
  public static void uninstallTelemetry ()
  {
    CapturingTelemetry.uninstall ();
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

    assertEquals (1, CT.getSpanCount ());
    final CapturedSpan aSpan = CT.getSpans ().getFirstOrNull ();
    assertNotNull (aSpan);
    assertEquals (CDBTelemetry.SPAN_FLYWAY_MIGRATE, aSpan.getName ());
    assertEquals (ETelemetrySpanKind.CLIENT, aSpan.getKind ());
    assertEquals (CDBTelemetry.COMPONENT_FLYWAY, aSpan.getAttribute (CDBTelemetry.ATTR_COMPONENT));
    assertEquals (EDatabaseSystemType.H2.getID (), aSpan.getAttribute (CDBTelemetry.ATTR_DB_SYSTEM_NAME));
    assertEquals (LOCATION, aSpan.getAttribute (CDBTelemetry.ATTR_FLYWAY_LOCATION));
    assertEquals (Boolean.FALSE, aSpan.getAttribute (CDBTelemetry.ATTR_FLYWAY_REPAIR_MODE));
    assertEquals (Long.valueOf (1), aSpan.getAttribute (CDBTelemetry.ATTR_FLYWAY_MIGRATIONS_EXECUTED));
    assertEquals (Boolean.TRUE, aSpan.getAttribute (CDBTelemetry.ATTR_SUCCESS));
    assertNull (aSpan.getRecordedException ());
    assertTrue (aSpan.isClosed ());

    final CapturedMeasurement aMigrations = CT.getFirstMeasurement (CDBTelemetry.METRIC_FLYWAY_MIGRATIONS);
    assertNotNull (aMigrations);
    assertEquals (1, aMigrations.getValueAsLong ());
    assertEquals (EDatabaseSystemType.H2.getID (), aMigrations.getAttribute (CDBTelemetry.ATTR_DB_SYSTEM_NAME));

    final CapturedMeasurement aDuration = CT.getFirstMeasurement (CDBTelemetry.METRIC_FLYWAY_MIGRATE_DURATION);
    assertNotNull (aDuration);
    assertEquals (Boolean.TRUE, aDuration.getAttribute (CDBTelemetry.ATTR_SUCCESS));
    assertNull (aDuration.getAttribute (CDBTelemetry.ATTR_ERROR_TYPE));
  }
}
