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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.helger.base.io.stream.StreamHelper;
import com.helger.collection.commons.ICommonsList;
import com.helger.db.jdbc.IHasConnection;
import com.helger.db.jdbc.callback.ConstantPreparedStatementDataProvider;
import com.helger.telemetry.mock.CapturingTelemetry;

/**
 * Test the CLOB handling of {@link DBExecutor}.
 *
 * @author Philip Helger
 */
public final class DBExecutorClobTest
{
  private Connection m_aConnection;
  private DBExecutor m_aExecutor;

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

  @Before
  public void before () throws SQLException
  {
    m_aConnection = DriverManager.getConnection ("jdbc:h2:mem:clob_" + UUID.randomUUID ());
    try (final var aStatement = m_aConnection.createStatement ())
    {
      aStatement.execute ("CREATE TABLE test_clob (id INT PRIMARY KEY, text CLOB)");
      aStatement.executeUpdate ("INSERT INTO test_clob VALUES (1, NULL)");
      aStatement.executeUpdate ("INSERT INTO test_clob VALUES (2, '')");
      aStatement.executeUpdate ("INSERT INTO test_clob VALUES (3, 'abc')");
    }
    m_aExecutor = new DBExecutor (new IHasConnection ()
    {
      public Connection getConnection ()
      {
        return m_aConnection;
      }

      public boolean shouldCloseConnection ()
      {
        return false;
      }
    });
  }

  @After
  public void after ()
  {
    StreamHelper.close (m_aConnection);
  }

  private DBResultRow _read (final int nID)
  {
    final ICommonsList <DBResultRow> aRows = m_aExecutor.queryAll ("SELECT text FROM test_clob WHERE id=?",
                                                                    new ConstantPreparedStatementDataProvider (Integer.valueOf (nID)));
    assertNotNull (aRows);
    assertEquals (1, aRows.size ());
    final DBResultRow aRow = aRows.getFirstOrNull ();
    assertEquals (Types.CLOB, aRow.get (0).getColumnType ());
    return aRow;
  }

  @Test
  public void testNullStaysNull ()
  {
    assertNull (_read (1).getAsString (0));
  }

  @Test
  public void testValues ()
  {
    assertEquals ("", _read (2).getAsString (0));
    assertEquals ("abc", _read (3).getAsString (0));
  }
}
