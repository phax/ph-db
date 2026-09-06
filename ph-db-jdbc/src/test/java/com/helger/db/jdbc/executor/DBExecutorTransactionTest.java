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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.helger.db.jdbc.IHasConnection;
import com.helger.db.jdbc.callback.ConstantPreparedStatementDataProvider;

/**
 * Verify transaction boundaries using an independent READ_COMMITTED observer.
 *
 * @author vinit-thummar
 */
public final class DBExecutorTransactionTest
{
  private Connection m_aWriter;
  private Connection m_aObserver;
  private DBExecutor m_aExecutor;

  @Before
  public void before () throws SQLException
  {
    final String sURL = "jdbc:h2:mem:transaction_" + UUID.randomUUID ();
    m_aWriter = DriverManager.getConnection (sURL);
    m_aObserver = DriverManager.getConnection (sURL);
    m_aObserver.setTransactionIsolation (Connection.TRANSACTION_READ_COMMITTED);
    try (final var aStatement = m_aWriter.createStatement ())
    {
      aStatement.execute ("CREATE TABLE test_transaction (id INT PRIMARY KEY)");
      aStatement.executeUpdate ("INSERT INTO test_transaction VALUES (1)");
    }
    m_aWriter.setAutoCommit (false);
    m_aExecutor = new DBExecutor (new IHasConnection ()
    {
      public Connection getConnection ()
      {
        return m_aWriter;
      }

      public boolean shouldCloseConnection ()
      {
        return false;
      }
    });
    m_aExecutor.exceptionCallbacks ().removeAll ();
  }

  @After
  public void after () throws SQLException
  {
    try
    {
      if (m_aWriter != null)
        m_aWriter.close ();
    }
    finally
    {
      if (m_aObserver != null)
        m_aObserver.close ();
    }
  }

  private static void _assertRows (final Connection aConnection, final Integer... aExpected) throws SQLException
  {
    final List <Integer> aIDs = new ArrayList <> ();
    try (final var aStatement = aConnection.createStatement ();
         final var aRows = aStatement.executeQuery ("SELECT id FROM test_transaction ORDER BY id"))
    {
      while (aRows.next ())
        aIDs.add (Integer.valueOf (aRows.getInt (1)));
    }
    assertEquals (Arrays.asList (aExpected), aIDs);
  }

  private void _delete ()
  {
    assertEquals (1,
                  m_aExecutor.insertOrUpdateOrDelete ("DELETE FROM test_transaction WHERE id=?",
                                                      new ConstantPreparedStatementDataProvider (Integer.valueOf (1))));
  }

  @Test
  public void testDirectJDBCRollback () throws SQLException
  {
    try (final var aStatement = m_aWriter.createStatement ())
    {
      aStatement.executeUpdate ("DELETE FROM test_transaction");
    }
    _assertRows (m_aWriter);
    _assertRows (m_aObserver, 1);
    m_aWriter.rollback ();
    _assertRows (m_aObserver, 1);
  }

  @Test
  public void testPreparedStatementCommittedAtTransactionEnd () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      _delete ();
      _assertRows (m_aWriter);
      _assertRows (m_aObserver, 1);
    }).isSuccess ());
    _assertRows (m_aObserver);
    assertFalse (m_aWriter.getAutoCommit ());
    assertFalse (m_aWriter.isClosed ());
  }

  @Test
  public void testStatementCommittedAtTransactionEnd () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      assertTrue (m_aExecutor.executeStatement ("DELETE FROM test_transaction").isSuccess ());
      _assertRows (m_aObserver, 1);
    }).isSuccess ());
    _assertRows (m_aObserver);
  }

  private void _testRollback (final Exception aException) throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      _delete ();
      throw aException;
    }).isFailure ());
    _assertRows (m_aObserver, 1);
    _assertRows (m_aWriter, 1);
    // The same executor must be usable after rollback.
    assertTrue (m_aExecutor.performInTransaction (this::_delete).isSuccess ());
    _assertRows (m_aObserver);
  }

  @Test
  public void testRollbackOnSQLException () throws SQLException
  {
    _testRollback (new SQLException ("Deliberate rollback"));
  }

  @Test
  public void testRollbackOnRuntimeException () throws SQLException
  {
    _testRollback (new IllegalStateException ("Deliberate rollback"));
  }

  private void _testQueryDoesNotCommit (final boolean bPrepared) throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      // Use JDBC directly to isolate whether the following query commits.
      try (final var aStatement = m_aWriter.createStatement ())
      {
        aStatement.executeUpdate ("DELETE FROM test_transaction");
      }
      if (bPrepared)
        assertTrue (m_aExecutor.queryAll ("SELECT id FROM test_transaction WHERE id=?",
                                         new ConstantPreparedStatementDataProvider (Integer.valueOf (1))).isEmpty ());
      else
        assertTrue (m_aExecutor.queryAll ("SELECT id FROM test_transaction").isEmpty ());
      _assertRows (m_aObserver, 1);
    }).isSuccess ());
    _assertRows (m_aObserver);
  }

  @Test
  public void testQueryDoesNotCommit () throws SQLException
  {
    _testQueryDoesNotCommit (false);
  }

  @Test
  public void testPreparedQueryDoesNotCommit () throws SQLException
  {
    _testQueryDoesNotCommit (true);
  }

  @Test
  public void testNestedSuccessDoesNotCommit () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      assertTrue (m_aExecutor.performInTransaction (this::_delete).isSuccess ());
      _assertRows (m_aWriter);
      _assertRows (m_aObserver, 1);
    }).isSuccess ());
    _assertRows (m_aObserver);
  }

  @Test
  public void testOuterFailureRollsBackNestedSuccess () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      assertTrue (m_aExecutor.performInTransaction (this::_delete).isSuccess ());
      throw new SQLException ("Outer transaction failed");
    }).isFailure ());
    _assertRows (m_aObserver, 1);
  }

  @Test
  public void testNestedFailurePreventsOuterCommit () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      _delete ();
      assertTrue (m_aExecutor.performInTransaction (() -> {
        throw new SQLException ("Nested transaction failed");
      }).isFailure ());
      _assertRows (m_aWriter);
      _assertRows (m_aObserver, 1);
      // A failure result must not allow partial work to be committed, even if
      // the caller continues instead of throwing an exception.
      assertTrue (m_aExecutor.executeStatement ("INSERT INTO test_transaction VALUES (2)").isSuccess ());
    }).isFailure ());
    _assertRows (m_aObserver, 1);
    assertTrue (m_aExecutor.performInTransaction (this::_delete).isSuccess ());
    _assertRows (m_aObserver);
  }

  @Test
  public void testSQLFailurePreventsCommit () throws SQLException
  {
    final AtomicBoolean aExceptionReported = new AtomicBoolean ();
    assertTrue (m_aExecutor.performInTransaction (() -> {
      _delete ();
      m_aExecutor.insertOrUpdateOrDelete ("INSERT INTO missing_table VALUES (?)",
                                          new ConstantPreparedStatementDataProvider (Integer.valueOf (2)),
                                          null,
                                          ex -> aExceptionReported.set (true));
      assertTrue (aExceptionReported.get ());
      _assertRows (m_aWriter);
      _assertRows (m_aObserver, 1);
      assertTrue (m_aExecutor.executeStatement ("INSERT INTO test_transaction VALUES (2)").isSuccess ());
    }).isFailure ());
    _assertRows (m_aObserver, 1);
  }

  @Test
  public void testStandaloneOperationsStillCommit () throws SQLException
  {
    _delete ();
    _assertRows (m_aObserver);
    assertTrue (m_aExecutor.executeStatement ("INSERT INTO test_transaction VALUES (2)").isSuccess ());
    _assertRows (m_aObserver, 2);
  }

  @Test
  public void testStandaloneFailureStillRollsBack () throws SQLException
  {
    // Existing standalone failure behaviour also rolls back pending JDBC work.
    try (final var aStatement = m_aWriter.createStatement ())
    {
      aStatement.executeUpdate ("DELETE FROM test_transaction");
    }
    assertTrue (m_aExecutor.executeStatement ("INSERT INTO missing_table VALUES (2)").isFailure ());
    _assertRows (m_aWriter, 1);
    _assertRows (m_aObserver, 1);
    _delete ();
    _assertRows (m_aObserver);
  }
}
