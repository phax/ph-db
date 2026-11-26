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
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.helger.base.io.stream.StreamHelper;
import com.helger.collection.commons.ICommonsList;
import com.helger.db.jdbc.IHasConnection;
import com.helger.db.jdbc.callback.ConstantPreparedStatementDataProvider;
import com.helger.telemetry.mock.CapturingTelemetry;

/**
 * Test the UTC handling of <code>TIMESTAMP</code> columns with {@link DBTimestampUTC}.
 *
 * @author Philip Helger
 */
public final class DBExecutorTimestampUTCTest
{
  private static final Instant [] INSTANTS = { Instant.parse ("2026-07-01T10:00:00.123Z"),
                                               // 02:30 CEST - first occurrence of 02:30 in Vienna
                                               Instant.parse ("2026-10-25T00:30:00Z"),
                                               // 02:30 CET - second occurrence of 02:30 in Vienna
                                               Instant.parse ("2026-10-25T01:30:00Z"),
                                               // UTC wall clock 02:30 does not exist in Vienna
                                               Instant.parse ("2026-03-29T02:30:00Z") };

  @BeforeClass
  public static void installTelemetry ()
  {
    // DBExecutorMetrics resolves its instruments once in its static initializer. If this class runs
    // first, the same instance as in DBExecutorTelemetryTest must already be installed
    DBExecutorTelemetryTest.CT.install ();
  }

  @AfterClass
  public static void uninstallTelemetry ()
  {
    CapturingTelemetry.uninstall ();
  }

  @NonNull
  private static DBExecutor _createExecutor (@NonNull final Connection aConnection)
  {
    return new DBExecutor (new IHasConnection ()
    {
      public Connection getConnection ()
      {
        return aConnection;
      }

      public boolean shouldCloseConnection ()
      {
        return false;
      }
    });
  }

  private static void _assertAllRead (@NonNull final DBExecutor aExecutor)
  {
    final ICommonsList <DBResultRow> aRows = aExecutor.queryAll ("SELECT id, ts FROM test_ts ORDER BY id");
    assertNotNull (aRows);
    assertEquals (INSTANTS.length + 1, aRows.size ());
    for (int i = 0; i < INSTANTS.length; ++i)
    {
      final DBResultRow aRow = aRows.get (i);
      assertEquals (Types.TIMESTAMP, aRow.get (1).getColumnType ());
      final OffsetDateTime aODT = aRow.getAsOffsetDateTimeUTC (1);
      assertNotNull (aODT);
      assertEquals (ZoneOffset.UTC, aODT.getOffset ());
      assertEquals (INSTANTS[i], aODT.toInstant ());
    }
    // SQL NULL
    assertNull (aRows.getLastOrNull ().getAsOffsetDateTimeUTC (1));
    assertNull (aRows.getLastOrNull ().get (1).getUTCTimestamp ());
  }

  @Test
  public void testDifferentTimeZones () throws SQLException
  {
    final TimeZone aOldTZ = TimeZone.getDefault ();
    final String sURL = "jdbc:h2:mem:ts_" + UUID.randomUUID () + ";DB_CLOSE_DELAY=-1";
    try
    {
      // Write in a time zone with daylight saving time
      TimeZone.setDefault (TimeZone.getTimeZone ("Europe/Vienna"));
      final Connection aConnection1 = DriverManager.getConnection (sURL);
      try
      {
        try (final var aStatement = aConnection1.createStatement ())
        {
          aStatement.execute ("CREATE TABLE test_ts (id INT PRIMARY KEY, ts TIMESTAMP(3))");
        }
        final DBExecutor aExecutor = _createExecutor (aConnection1);
        for (int i = 0; i < INSTANTS.length; ++i)
          assertEquals (1,
                        aExecutor.insertOrUpdateOrDelete ("INSERT INTO test_ts (id, ts) VALUES (?, ?)",
                                                          new ConstantPreparedStatementDataProvider (Integer.valueOf (i),
                                                                                                     DBTimestampUTC.of (INSTANTS[i].atOffset (ZoneOffset.ofHours (2))))));
        assertEquals (1,
                      aExecutor.insertOrUpdateOrDelete ("INSERT INTO test_ts (id, ts) VALUES (?, ?)",
                                                        new ConstantPreparedStatementDataProvider (Integer.valueOf (INSTANTS.length),
                                                                                                   DBTimestampUTC.of ((Instant) null))));

        // The database itself stores the UTC wall clock
        final ICommonsList <DBResultRow> aRows = aExecutor.queryAll ("SELECT CAST(ts AS VARCHAR(30)) FROM test_ts WHERE id=2");
        assertNotNull (aRows);
        assertTrue (aRows.getFirstOrNull ().getAsString (0), aRows.getFirstOrNull ().getAsString (0).startsWith ("2026-10-25 01:30:00"));

        _assertAllRead (aExecutor);
      }
      finally
      {
        StreamHelper.close (aConnection1);
      }

      // Read on "another server" in another time zone
      TimeZone.setDefault (TimeZone.getTimeZone ("UTC"));
      final Connection aConnection2 = DriverManager.getConnection (sURL);
      try
      {
        _assertAllRead (_createExecutor (aConnection2));
        try (final var aStatement = aConnection2.createStatement ())
        {
          aStatement.execute ("DROP TABLE test_ts");
        }
      }
      finally
      {
        StreamHelper.close (aConnection2);
      }
    }
    finally
    {
      TimeZone.setDefault (aOldTZ);
    }
  }

  @Test
  public void testBasic ()
  {
    assertNull (DBTimestampUTC.of ((Instant) null));
    assertNull (DBTimestampUTC.of ((OffsetDateTime) null));
    final Instant aNow = Instant.parse ("2026-11-26T12:00:00.123Z");
    final DBTimestampUTC a = DBTimestampUTC.of (aNow);
    assertEquals (aNow, a.getInstant ());
    assertEquals (aNow, a.getAsTimestamp ().toInstant ());
    // The offset does not matter
    assertEquals (a, DBTimestampUTC.of (aNow.atOffset (ZoneOffset.ofHours (-5))));
    assertEquals ("UTC", DBTimestampUTC.createUTCCalendar ().getTimeZone ().getID ());
  }
}
