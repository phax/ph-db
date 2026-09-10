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
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.helger.base.io.stream.StreamHelper;
import com.helger.base.numeric.mutable.MutableBoolean;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.db.jdbc.IHasConnection;
import com.helger.db.jdbc.callback.ConstantPreparedStatementDataProvider;

/**
 * Verify transaction boundaries using an independent READ_COMMITTED observer.
 *
 * @author vinit-thummar
 */
public final class DBExecutorTransactionTest
{
  private static final Integer ONE = Integer.valueOf (1);

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
  public void after ()
  {
    StreamHelper.close (m_aWriter);
    StreamHelper.close (m_aObserver);
  }

  private static void _assertRowIDs (final Connection aConnection,
                                     final ICommonsList <Integer> aExpected) throws SQLException
  {
    final ICommonsList <Integer> aIDs = new CommonsArrayList <> ();
    try (final var aStatement = aConnection.createStatement ();
         final var aRows = aStatement.executeQuery ("SELECT id FROM test_transaction ORDER BY id"))
    {
      while (aRows.next ())
        aIDs.add (Integer.valueOf (aRows.getInt (1)));
    }
    assertEquals (aExpected, aIDs);
  }

  private void _delete ()
  {
    assertEquals (1,
                  m_aExecutor.insertOrUpdateOrDelete ("DELETE FROM test_transaction WHERE id=?",
                                                      new ConstantPreparedStatementDataProvider (ONE)));
  }

  @Test
  public void testDirectJDBCRollback () throws SQLException
  {
    try (final var aStatement = m_aWriter.createStatement ())
    {
      aStatement.executeUpdate ("DELETE FROM test_transaction");
    }
    _assertRowIDs (m_aWriter, new CommonsArrayList <> ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
    m_aWriter.rollback ();
    _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
  }

  @Test
  public void testPreparedStatementCommittedAtTransactionEnd () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      _delete ();
      _assertRowIDs (m_aWriter, new CommonsArrayList <> ());
      _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
    }).isSuccess ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> ());
    assertFalse (m_aWriter.getAutoCommit ());
    assertFalse (m_aWriter.isClosed ());
  }

  @Test
  public void testStatementCommittedAtTransactionEnd () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      assertTrue (m_aExecutor.executeStatement ("DELETE FROM test_transaction").isSuccess ());
      _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
    }).isSuccess ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> ());
  }

  private void _testRollback (final Exception aException) throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      _delete ();
      throw aException;
    }).isFailure ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
    _assertRowIDs (m_aWriter, new CommonsArrayList <> (ONE));
    // The same executor must be usable after rollback.
    assertTrue (m_aExecutor.performInTransaction (this::_delete).isSuccess ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> ());
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
                                          new ConstantPreparedStatementDataProvider (ONE)).isEmpty ());
      else
        assertTrue (m_aExecutor.queryAll ("SELECT id FROM test_transaction").isEmpty ());
      _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
    }).isSuccess ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> ());
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
      _assertRowIDs (m_aWriter, new CommonsArrayList <> ());
      _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
    }).isSuccess ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> ());
  }

  @Test
  public void testOuterFailureRollsBackNestedSuccess () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      assertTrue (m_aExecutor.performInTransaction (this::_delete).isSuccess ());
      throw new SQLException ("Outer transaction failed");
    }).isFailure ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
  }

  @Test
  public void testNestedFailurePreventsOuterCommit () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      _delete ();
      assertTrue (m_aExecutor.performInTransaction (() -> { throw new SQLException ("Nested transaction failed"); })
                             .isFailure ());
      _assertRowIDs (m_aWriter, new CommonsArrayList <> ());
      _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
      // A failure result must not allow partial work to be committed, even if
      // the caller continues instead of throwing an exception.
      assertTrue (m_aExecutor.executeStatement ("INSERT INTO test_transaction VALUES (2)").isSuccess ());
    }).isFailure ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
    assertTrue (m_aExecutor.performInTransaction (this::_delete).isSuccess ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> ());
  }

  @Test
  public void testSQLFailurePreventsCommit () throws SQLException
  {
    assertTrue (m_aExecutor.performInTransaction (() -> {
      _delete ();

      final MutableBoolean aExceptionReported = new MutableBoolean (false);
      m_aExecutor.insertOrUpdateOrDelete ("INSERT INTO missing_table VALUES (?)",
                                          new ConstantPreparedStatementDataProvider (Integer.valueOf (2)),
                                          null,
                                          ex -> aExceptionReported.set (true));
      assertTrue (aExceptionReported.booleanValue ());
      _assertRowIDs (m_aWriter, new CommonsArrayList <> ());
      _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));

      assertTrue (m_aExecutor.executeStatement ("INSERT INTO test_transaction VALUES (2)").isSuccess ());
    }).isFailure ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));
  }

  @Test
  public void testStandaloneOperationsStillCommit () throws SQLException
  {
    _delete ();
    _assertRowIDs (m_aObserver, new CommonsArrayList <> ());

    assertTrue (m_aExecutor.executeStatement ("INSERT INTO test_transaction VALUES (2)").isSuccess ());
    _assertRowIDs (m_aObserver, new CommonsArrayList <> (Integer.valueOf (2)));
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
    _assertRowIDs (m_aWriter, new CommonsArrayList <> (ONE));
    _assertRowIDs (m_aObserver, new CommonsArrayList <> (ONE));

    _delete ();
    _assertRowIDs (m_aObserver, new CommonsArrayList <> ());
  }
}
