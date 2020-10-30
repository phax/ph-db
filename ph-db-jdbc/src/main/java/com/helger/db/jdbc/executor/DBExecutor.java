/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.CodingStyleguideUnaware;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.callback.CallbackList;
import com.helger.commons.callback.ICallback;
import com.helger.commons.callback.IThrowingRunnable;
import com.helger.commons.callback.exception.IExceptionCallback;
import com.helger.commons.callback.exception.LoggingExceptionCallback;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.state.ESuccess;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.timing.StopWatch;
import com.helger.db.api.callback.IExecutionTimeExceededCallback;
import com.helger.db.api.callback.LoggingExecutionTimeExceededCallback;
import com.helger.db.api.jdbc.JDBCHelper;
import com.helger.db.jdbc.ConnectionFromDataSourceProvider;
import com.helger.db.jdbc.IHasConnection;
import com.helger.db.jdbc.IHasDataSource;
import com.helger.db.jdbc.callback.GetSingleGeneratedKeyCallback;
import com.helger.db.jdbc.callback.IConnectionStatusChangeCallback;
import com.helger.db.jdbc.callback.IGeneratedKeysCallback;
import com.helger.db.jdbc.callback.IPreparedStatementDataProvider;
import com.helger.db.jdbc.callback.IResultSetRowCallback;
import com.helger.db.jdbc.callback.IUpdatedRowCountCallback;
import com.helger.db.jdbc.callback.UpdatedRowCountCallback;

/**
 * Simple wrapper around common JDBC functionality.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class DBExecutor implements Serializable
{
  @FunctionalInterface
  private interface IWithConnectionCallback extends ICallback
  {
    void run (@Nonnull Connection aConnection) throws SQLException;
  }

  @FunctionalInterface
  private interface IWithStatementCallback extends ICallback
  {
    void run (@Nonnull Statement aStatement) throws SQLException;
  }

  @FunctionalInterface
  private interface IWithPreparedStatementCallback extends ICallback
  {
    void run (@Nonnull PreparedStatement aPreparedStatement) throws SQLException;
  }

  @FunctionalInterface
  private interface IConnectionExecutor
  {
    @Nonnull
    ESuccess execute (@Nonnull IWithConnectionCallback aCB, @Nullable IExceptionCallback <? super Exception> aExtraExCB);
  }

  public static final long DEFAULT_EXECUTION_DURATION_WARN_MS = CGlobal.MILLISECONDS_PER_SECOND;
  public static final boolean DEFAULT_DEBUG_CONNECTIONS = false;
  public static final boolean DEFAULT_DEBUG_TRANSACTIONS = false;
  public static final boolean DEFAULT_DEBUG_SQL_STATEMENTS = false;

  private static final Logger LOGGER = LoggerFactory.getLogger (DBExecutor.class);
  private static final Long MINUS1 = Long.valueOf (CGlobal.ILLEGAL_UINT);

  private final IHasConnection m_aConnectionProvider;
  private ETriState m_eConnectionEstablished = ETriState.UNDEFINED;
  private IConnectionStatusChangeCallback m_aConnectionStatusChangeCallback;
  private final CallbackList <IExceptionCallback <? super Exception>> m_aExceptionCallbacks = new CallbackList <> ();
  private IConnectionExecutor m_aConnectionExecutor;

  private long m_nExecutionDurationWarnMS = DEFAULT_EXECUTION_DURATION_WARN_MS;
  private static final CallbackList <IExecutionTimeExceededCallback> m_aExecutionTimeExceededHandlers = new CallbackList <> ();

  private static final AtomicLong s_aConnectionCounter = new AtomicLong (0);
  private static final AtomicLong s_aSQLStatementCounter = new AtomicLong (0);
  private static final AtomicLong s_aTransactionCounter = new AtomicLong (0);

  private final AtomicInteger m_aTransactionLevel = new AtomicInteger (0);
  private boolean m_bDebugConnections = DEFAULT_DEBUG_CONNECTIONS;
  private boolean m_bDebugTransactions = DEFAULT_DEBUG_TRANSACTIONS;
  private boolean m_bDebugSQLStatements = DEFAULT_DEBUG_SQL_STATEMENTS;

  public DBExecutor (@Nonnull final IHasDataSource aDataSourceProvider)
  {
    this (new ConnectionFromDataSourceProvider (aDataSourceProvider));
  }

  public DBExecutor (@Nonnull final IHasConnection aConnectionProvider)
  {
    ValueEnforcer.notNull (aConnectionProvider, "ConnectionProvider");
    m_aConnectionProvider = aConnectionProvider;
    m_aExceptionCallbacks.add (new LoggingExceptionCallback ());
    m_aConnectionExecutor = this::withNewConnectionDo;
    m_aExecutionTimeExceededHandlers.add (new LoggingExecutionTimeExceededCallback (true));
  }

  /**
   * Debug logging method. Only invoked if the respective "debug log" member is
   * set to true
   *
   * @param sMessage
   *        The message to log. May not be <code>null</code>.
   */
  protected final void debugLog (@Nonnull final String sMessage)
  {
    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("[" + Thread.currentThread ().getName () + "] " + sMessage);
  }

  /**
   * @return The current "connection established" state. Never
   *         <code>null</code>.
   */
  @Nonnull
  public final ETriState getConnectionEstablished ()
  {
    return m_eConnectionEstablished;
  }

  /**
   * Set the "Connection established" state.
   *
   * @param eNewState
   *        The new state. May not be <code>null</code>.
   * @return this for chaining
   */
  @Nonnull
  public final DBExecutor setConnectionEstablished (@Nonnull final ETriState eNewState)
  {
    ValueEnforcer.notNull (eNewState, "NewState");
    if (eNewState != m_eConnectionEstablished)
    {
      final ETriState eOldState = m_eConnectionEstablished;
      if (m_bDebugConnections)
        debugLog ("Setting connection established state from " + eOldState + " to " + eNewState);
      m_eConnectionEstablished = eNewState;

      if (m_aConnectionStatusChangeCallback != null)
        m_aConnectionStatusChangeCallback.onConnectionStatusChanged (eOldState, eNewState);
    }
    return this;
  }

  /**
   * Reset the "Connection established" flag. This is a shortcut for
   * <code>setConnectionEstablished (ETriState.UNDEFINED)</code>.
   *
   * @return this for chaining.
   */
  @Nonnull
  public final DBExecutor resetConnectionEstablished ()
  {
    return setConnectionEstablished (ETriState.UNDEFINED);
  }

  /**
   * @return The callback to be invoked, if the connection status changes. May
   *         be <code>null</code>.
   */
  @Nullable
  public final IConnectionStatusChangeCallback getConnectionStatusChangeCallback ()
  {
    return m_aConnectionStatusChangeCallback;
  }

  /**
   * Set the callback to be invoked, if the connection status changes. Only one
   * callback can be invoked.
   *
   * @param aCB
   *        The callback to be invoked. May be <code>null</code>.
   * @return this for chaining
   */
  @Nonnull
  public final DBExecutor setConnectionStatusChangeCallback (@Nullable final IConnectionStatusChangeCallback aCB)
  {
    m_aConnectionStatusChangeCallback = aCB;
    return this;
  }

  /**
   * @return The mutable list of exception callbacks to be invoked in case of an
   *         Exception.
   */
  @Nonnull
  @ReturnsMutableObject
  public final CallbackList <IExceptionCallback <? super Exception>> exceptionCallbacks ()
  {
    return m_aExceptionCallbacks;
  }

  @CheckForSigned
  public final long getExecutionDurationWarnMS ()
  {
    return m_nExecutionDurationWarnMS;
  }

  public final boolean isExecutionDurationWarnEnabled ()
  {
    return m_nExecutionDurationWarnMS > 0;
  }

  @Nonnull
  public final DBExecutor setExecutionDurationWarnMS (final long nExecutionDurationWarnMS)
  {
    m_nExecutionDurationWarnMS = nExecutionDurationWarnMS;
    return this;
  }

  /**
   * Get the custom exception handler list.
   *
   * @return Never <code>null</code>.
   */
  @Nonnull
  public final CallbackList <IExecutionTimeExceededCallback> executionTimeExceededHandlers ()
  {
    return m_aExecutionTimeExceededHandlers;
  }

  public final void onExecutionTimeExceeded (@Nonnull final String sMsg, @Nonnegative final long nExecutionMillis)
  {
    m_aExecutionTimeExceededHandlers.forEach (x -> x.onExecutionTimeExceeded (sMsg, nExecutionMillis, m_nExecutionDurationWarnMS));
  }

  public final boolean isDebugConnections ()
  {
    return m_bDebugConnections;
  }

  @Nonnull
  public final DBExecutor setDebugConnections (final boolean bDebugConnections)
  {
    m_bDebugConnections = bDebugConnections;
    return this;
  }

  public final boolean isDebugTransactions ()
  {
    return m_bDebugTransactions;
  }

  @Nonnull
  public final DBExecutor setDebugTransactions (final boolean bDebugTransactions)
  {
    m_bDebugTransactions = bDebugTransactions;
    return this;
  }

  public final boolean isDebugSQLStatements ()
  {
    return m_bDebugSQLStatements;
  }

  @Nonnull
  public final DBExecutor setDebugSQLStatements (final boolean bDebugSQLStatements)
  {
    m_bDebugSQLStatements = bDebugSQLStatements;
    return this;
  }

  @CodingStyleguideUnaware ("Needs to be synchronized!")
  @Nonnull
  protected final synchronized ESuccess withNewConnectionDo (@Nonnull final IWithConnectionCallback aCB,
                                                             @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final long nConnectionID = s_aConnectionCounter.incrementAndGet ();

    if (m_eConnectionEstablished.isFalse ())
    {
      // Avoid trying again
      if (m_bDebugConnections)
        debugLog ("Refuse to open SQL Connection [" + nConnectionID + "] because it failed previously");
      return ESuccess.FAILURE;
    }

    Connection aConnection = null;
    try
    {
      if (m_bDebugConnections)
        debugLog ("Opening a new SQL Connection [" + nConnectionID + "]");

      // Get connection
      aConnection = m_aConnectionProvider.getConnection ();
      if (aConnection == null)
        return ESuccess.FAILURE;
      try
      {
        if (aConnection.isClosed ())
          LOGGER.error ("Received a closed connection from provider " + m_aConnectionProvider);
      }
      catch (final SQLException ex)
      {
        // Ignore
      }

      setConnectionEstablished (ETriState.TRUE);

      // Okay, connection was established
      return withExistingConnectionDo (aConnection, aCB, aExtraExCB);
    }
    catch (final DBNoConnectionException ex)
    {
      // Error creating a connection
      setConnectionEstablished (ETriState.FALSE);
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("Connection could not be established. Remembering this status.");

      // Invoke callback
      m_aExceptionCallbacks.forEach (x -> x.onException (ex));
      if (aExtraExCB != null)
        aExtraExCB.onException (ex);
      return ESuccess.FAILURE;
    }
    finally
    {
      // Close connection again (if necessary)
      if (aConnection != null && m_aConnectionProvider.shouldCloseConnection ())
      {
        if (m_bDebugConnections)
          debugLog ("Now closing SQL Connection [" + nConnectionID + "]");
        JDBCHelper.close (aConnection);
      }
    }
  }

  @Nonnull
  protected final ESuccess withExistingConnectionDo (@Nonnull final Connection aConnection,
                                                     @Nonnull final IWithConnectionCallback aCB,
                                                     @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    ValueEnforcer.notNull (aConnection, "Connection");
    ValueEnforcer.notNull (aCB, "CB");

    ESuccess eCommited = ESuccess.FAILURE;
    try
    {
      // Perform action on connection
      aCB.run (aConnection);

      // Commit
      eCommited = JDBCHelper.commit (aConnection);
    }
    catch (final SQLException | RuntimeException ex)
    {
      // Invoke callback
      m_aExceptionCallbacks.forEach (x -> x.onException (ex));
      if (aExtraExCB != null)
        aExtraExCB.onException (ex);
      return ESuccess.FAILURE;
    }
    finally
    {
      // Failure? Roll back!
      if (eCommited.isFailure ())
        JDBCHelper.rollback (aConnection);
    }
    return eCommited;
  }

  protected static void handleGeneratedKeys (@Nonnull final ResultSet aGeneratedKeysRS,
                                             @Nonnull final IGeneratedKeysCallback aGeneratedKeysCB) throws SQLException
  {
    final int nCols = aGeneratedKeysRS.getMetaData ().getColumnCount ();
    final ICommonsList <ICommonsList <Object>> aValues = new CommonsArrayList <> ();
    while (aGeneratedKeysRS.next ())
    {
      final ICommonsList <Object> aRow = new CommonsArrayList <> (nCols);
      for (int i = 1; i <= nCols; ++i)
        aRow.add (aGeneratedKeysRS.getObject (i));
      aValues.add (aRow);
    }
    aGeneratedKeysCB.onGeneratedKeys (aValues);
  }

  @Nonnull
  public final ESuccess performInTransaction (@Nonnull final IThrowingRunnable <Exception> aRunnable)
  {
    return performInTransaction (aRunnable, null);
  }

  @Nonnull
  public final ESuccess performInTransaction (@Nonnull final IThrowingRunnable <Exception> aRunnable,
                                              @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final IWithConnectionCallback aWithConnectionCB = aConnection -> {
      // First level has 1
      final int nTransactionLevel = m_aTransactionLevel.incrementAndGet ();
      try
      {
        final long nTransactionID = s_aTransactionCounter.incrementAndGet ();
        if (m_bDebugTransactions)
          debugLog ("Starting a level " + nTransactionLevel + " transaction [" + nTransactionID + "]");

        // Disable auto commit
        // final boolean bOldAutoCommit = aConnection.getAutoCommit ();
        // aConnection.setAutoCommit (false);

        // Avoid creating a new connection
        final IConnectionExecutor aOldConnectionExecutor = m_aConnectionExecutor;
        m_aConnectionExecutor = (aCB2, aExCB2) -> this.withExistingConnectionDo (aConnection, aCB2, aExCB2);

        try
        {
          aRunnable.run ();

          if (nTransactionLevel == 1)
          {
            if (m_bDebugTransactions)
              debugLog ("Now commiting level " + nTransactionLevel + " transaction [" + nTransactionID + "]");

            // Commit
            aConnection.commit ();
          }
          else
          {
            if (m_bDebugTransactions)
              debugLog ("Not commiting level " + nTransactionLevel + " transaction [" + nTransactionID + "] because it is nested");
          }
        }
        catch (final Exception ex)
        {
          if (nTransactionLevel == 1)
          {
            if (m_bDebugTransactions)
              debugLog ("Now rolling back level " +
                        nTransactionLevel +
                        " transaction [" +
                        nTransactionID +
                        "]: " +
                        ex.getClass ().getName () +
                        " - " +
                        ex.getMessage ());

            // Rollback
            aConnection.rollback ();
          }
          else
          {
            if (m_bDebugTransactions)
              debugLog ("Not rolling back level " + nTransactionLevel + " transaction [" + nTransactionID + "] because it is nested");
          }

          // Exception handler
          if (aExtraExCB != null)
            aExtraExCB.onException (ex);

          // Propagate
          if (ex instanceof RuntimeException)
            throw (RuntimeException) ex;
          if (ex instanceof SQLException)
            throw (SQLException) ex;
          throw new SQLException ("Caught exception while perfoming something in a level " +
                                  nTransactionLevel +
                                  " transaction [" +
                                  nTransactionID +
                                  "]",
                                  ex);
        }
        finally
        {
          // Reset state
          m_aConnectionExecutor = aOldConnectionExecutor;

          // try
          // {
          // aConnection.setAutoCommit (bOldAutoCommit);
          // }
          // catch (final SQLException ex)
          // {
          // LOGGER.warn ("Error in resetting AutoCommit for transaction [" +
          // nTransactionID + "] to " + bOldAutoCommit, ex);
          // }

          if (m_bDebugTransactions)
            debugLog ("Finished level " + nTransactionLevel + " transaction [" + nTransactionID + "]");
        }
      }
      finally
      {
        m_aTransactionLevel.decrementAndGet ();
      }
    };
    return m_aConnectionExecutor.execute (aWithConnectionCB, aExtraExCB);
  }

  @Nonnull
  protected final ESuccess withStatementDo (@Nonnull final IWithStatementCallback aCB,
                                            @Nullable final IGeneratedKeysCallback aGeneratedKeysCB,
                                            @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final IWithConnectionCallback aWithConnectionCB = aConnection -> {
      Statement aStatement = null;
      try
      {
        aStatement = aConnection.createStatement ();
        aCB.run (aStatement);

        if (aGeneratedKeysCB != null)
          handleGeneratedKeys (aStatement.getGeneratedKeys (), aGeneratedKeysCB);
      }
      finally
      {
        StreamHelper.close (aStatement);
      }
    };
    return m_aConnectionExecutor.execute (aWithConnectionCB, aExtraExCB);
  }

  protected final void withTimingDo (@Nonnull final String sDescription,
                                     @Nonnull final IThrowingRunnable <SQLException> aRunnable) throws SQLException
  {
    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      aRunnable.run ();
    }
    finally
    {
      aSW.stop ();
      final long nDurationMillis = aSW.getMillis ();

      if (isExecutionDurationWarnEnabled ())
      {
        if (nDurationMillis > m_nExecutionDurationWarnMS)
          onExecutionTimeExceeded ("DB execution " + sDescription, nDurationMillis);
      }
      else
      {
        if (LOGGER.isTraceEnabled ())
          LOGGER.trace ("DB execution " + sDescription + " took " + nDurationMillis + " ms");
      }
    }
  }

  @Nonnull
  protected final ESuccess withPreparedStatementDo (@Nonnull final String sSQL,
                                                    @Nonnull final IPreparedStatementDataProvider aPSDP,
                                                    @Nonnull final IWithPreparedStatementCallback aPSCallback,
                                                    @Nullable final IUpdatedRowCountCallback aUpdatedRowCountCB,
                                                    @Nullable final IGeneratedKeysCallback aGeneratedKeysCB,
                                                    @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final IWithConnectionCallback aWithConnectionCB = aConnection -> {
      final long nSQLStatementID = s_aSQLStatementCounter.incrementAndGet ();
      final String sWhat = "PreparedStatement [" + nSQLStatementID + "] <" + sSQL + "> with " + aPSDP.getValueCount () + " values";
      if (m_bDebugSQLStatements)
        debugLog ("Will execute " + sWhat);

      withTimingDo (sWhat, () -> {
        try (final PreparedStatement aPS = aConnection.prepareStatement (sSQL, Statement.RETURN_GENERATED_KEYS))
        {
          if (aPS.getParameterMetaData ().getParameterCount () != aPSDP.getValueCount ())
            throw new IllegalArgumentException ("parameter count (" +
                                                aPS.getParameterMetaData ().getParameterCount () +
                                                ") does not match passed column name count (" +
                                                aPSDP.getValueCount () +
                                                ")");

          // assign values
          int nIndex = 1;
          for (final Object aArg : aPSDP.getObjectValues ())
            aPS.setObject (nIndex++, aArg);

          // call callback
          aPSCallback.run (aPS);

          // Updated row count callback present?
          if (aUpdatedRowCountCB != null)
          {
            try
            {
              // throws an Exception if not supported
              aUpdatedRowCountCB.setUpdatedRowCount (aPS.getLargeUpdateCount ());
            }
            catch (final Exception ex)
            {
              aUpdatedRowCountCB.setUpdatedRowCount (aPS.getUpdateCount ());
            }
          }

          // retrieve generated keys?
          if (aGeneratedKeysCB != null)
            handleGeneratedKeys (aPS.getGeneratedKeys (), aGeneratedKeysCB);
        }
      });
    };
    return m_aConnectionExecutor.execute (aWithConnectionCB, aExtraExCB);
  }

  @Nonnull
  public ESuccess executeStatement (@Nonnull final String sSQL)
  {
    return executeStatement (sSQL, null, null);
  }

  @Nonnull
  public ESuccess executeStatement (@Nonnull final String sSQL,
                                    @Nullable final IGeneratedKeysCallback aGeneratedKeysCB,
                                    @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    return withStatementDo (aStatement -> {
      final long nSQLStatementID = s_aSQLStatementCounter.incrementAndGet ();
      final String sWhat = "Statement [" + nSQLStatementID + "] <" + sSQL + ">";
      if (m_bDebugSQLStatements)
        debugLog ("Will execute " + sWhat);

      withTimingDo (sWhat, () -> {
        aStatement.execute (sSQL);
      });
    }, aGeneratedKeysCB, aExtraExCB);
  }

  @Nonnull
  public ESuccess executePreparedStatement (@Nonnull final String sSQL, @Nonnull final IPreparedStatementDataProvider aPSDP)
  {
    return executePreparedStatement (sSQL, aPSDP, null, null, null);
  }

  @Nonnull
  public ESuccess executePreparedStatement (@Nonnull final String sSQL,
                                            @Nonnull final IPreparedStatementDataProvider aPSDP,
                                            @Nullable final IUpdatedRowCountCallback aURWCC,
                                            @Nullable final IGeneratedKeysCallback aGeneratedKeysCB,
                                            @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    return withPreparedStatementDo (sSQL, aPSDP, PreparedStatement::execute, aURWCC, aGeneratedKeysCB, aExtraExCB);
  }

  @Nullable
  public Optional <Object> executePreparedStatementAndGetGeneratedKey (@Nonnull final String sSQL,
                                                                       @Nonnull final IPreparedStatementDataProvider aPSDP,
                                                                       @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final GetSingleGeneratedKeyCallback aCB = new GetSingleGeneratedKeyCallback ();
    if (executePreparedStatement (sSQL, aPSDP, null, aCB, aExtraExCB).isFailure ())
      return Optional.empty ();
    return Optional.of (aCB.getGeneratedKey ());
  }

  /**
   * Perform an INSERT or UPDATE statement.
   *
   * @param sSQL
   *        SQL to execute.
   * @param aPSDP
   *        The prepared statement provider.
   * @return The number of modified/inserted rows.
   */
  public long insertOrUpdateOrDelete (@Nonnull final String sSQL, @Nonnull final IPreparedStatementDataProvider aPSDP)
  {
    return insertOrUpdateOrDelete (sSQL, aPSDP, null, null);
  }

  /**
   * Perform an INSERT or UPDATE statement.
   *
   * @param sSQL
   *        SQL to execute.
   * @param aPSDP
   *        The prepared statement provider.
   * @param aGeneratedKeysCB
   *        An optional callback to retrieve eventually generated values. May be
   *        <code>null</code>.
   * @param aExtraExCB
   *        Per-call Exception callback. May be <code>null</code>.
   * @return The number of modified/inserted rows.
   */
  public long insertOrUpdateOrDelete (@Nonnull final String sSQL,
                                      @Nonnull final IPreparedStatementDataProvider aPSDP,
                                      @Nullable final IGeneratedKeysCallback aGeneratedKeysCB,
                                      @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    // We need this wrapper because the anonymous inner class cannot change
    // variables in outer scope.
    final IUpdatedRowCountCallback aURCCB = new UpdatedRowCountCallback ();
    withPreparedStatementDo (sSQL, aPSDP, PreparedStatement::execute, aURCCB, aGeneratedKeysCB, aExtraExCB);
    return aURCCB.getUpdatedRowCount ();
  }

  public static final class CountAndKey
  {
    private final long m_nUpdateCount;
    private final Object m_aGeneratedKey;

    public CountAndKey (@Nonnegative final long nUpdateCount, @Nullable final Object aGeneratedKey)
    {
      m_nUpdateCount = nUpdateCount;
      m_aGeneratedKey = aGeneratedKey;
    }

    @Nonnegative
    public long getUpdateCount ()
    {
      return m_nUpdateCount;
    }

    public boolean isUpdateCountUsable ()
    {
      return m_nUpdateCount != IUpdatedRowCountCallback.NOT_INITIALIZED;
    }

    @Nullable
    public Object getGeneratedKey ()
    {
      return m_aGeneratedKey;
    }

    public boolean hasGeneratedKey ()
    {
      return m_aGeneratedKey != null;
    }
  }

  @Nonnull
  public CountAndKey insertOrUpdateAndGetGeneratedKey (@Nonnull final String sSQL,
                                                       @Nonnull final IPreparedStatementDataProvider aPSDP,
                                                       @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final GetSingleGeneratedKeyCallback aCB = new GetSingleGeneratedKeyCallback ();
    final long nUpdateCount = insertOrUpdateOrDelete (sSQL, aPSDP, aCB, aExtraExCB);
    return new CountAndKey (nUpdateCount, nUpdateCount != IUpdatedRowCountCallback.NOT_INITIALIZED ? aCB.getGeneratedKey () : null);
  }

  /**
   * Iterate the passed result set, collect all values of a single result row,
   * and call the callback for each row of result objects.
   *
   * @param aRS
   *        The result set to iterate.
   * @param aCallback
   *        The callback to be invoked for each row.
   * @return The number of result rows. Always &ge; 0
   * @throws SQLException
   *         on error
   */
  @Nonnegative
  protected static final long iterateResultSet (@WillClose final ResultSet aRS,
                                                @Nonnull final IResultSetRowCallback aCallback) throws SQLException
  {
    try
    {
      // Get column names
      final ResultSetMetaData aRSMD = aRS.getMetaData ();
      final int nCols = aRSMD.getColumnCount ();
      final String [] aColumnNames = new String [nCols];
      final int [] aColumnTypes = new int [nCols];
      for (int i = 1; i <= nCols; ++i)
      {
        aColumnNames[i - 1] = aRSMD.getColumnName (i).intern ();
        aColumnTypes[i - 1] = aRSMD.getColumnType (i);
      }

      // create object once for all rows
      final DBResultRow aRow = new DBResultRow (nCols);

      // for all result set elements
      long nResultRows = 0;
      while (aRS.next ())
      {
        nResultRows++;

        // fill result row
        aRow.internalClear ();
        for (int i = 1; i <= nCols; ++i)
        {
          final Object aColumnValue = aRS.getObject (i);
          aRow.internalAdd (new DBResultField (aColumnNames[i - 1], aColumnTypes[i - 1], aColumnValue));
        }

        // handle result row
        aCallback.accept (aRow);
      }

      return nResultRows;
    }
    finally
    {
      aRS.close ();
    }
  }

  @Nonnull
  public ESuccess queryAll (@Nonnull @Nonempty final String sSQL, @Nonnull final IResultSetRowCallback aResultItemCallback)
  {
    return withStatementDo (aStatement -> {
      final long nSQLStatementID = s_aSQLStatementCounter.incrementAndGet ();
      final String sWhat = "Query [" + nSQLStatementID + "] <" + sSQL + ">";
      if (m_bDebugSQLStatements)
        debugLog ("Will execute " + sWhat);

      withTimingDo (sWhat, () -> {
        final ResultSet aResultSet = aStatement.executeQuery (sSQL);
        final long nResultRows = iterateResultSet (aResultSet, aResultItemCallback);

        if (m_bDebugSQLStatements)
          debugLog ("  Found " + nResultRows + " result rows [" + nSQLStatementID + "]");
      });
    }, (IGeneratedKeysCallback) null, null);
  }

  @Nonnull
  public ESuccess queryAll (@Nonnull final String sSQL,
                            @Nonnull final IPreparedStatementDataProvider aPSDP,
                            @Nonnull final IResultSetRowCallback aResultItemCallback)
  {
    return withPreparedStatementDo (sSQL, aPSDP, aPreparedStatement -> {
      final ResultSet aResultSet = aPreparedStatement.executeQuery ();
      final long nResultRows = iterateResultSet (aResultSet, aResultItemCallback);
      if (m_bDebugSQLStatements)
        debugLog ("  Found " + nResultRows + " result rows");
    }, (IUpdatedRowCountCallback) null, (IGeneratedKeysCallback) null, null);
  }

  @Nullable
  public Optional <ICommonsList <DBResultRow>> queryAll (@Nonnull @Nonempty final String sSQL)
  {
    final ICommonsList <DBResultRow> aAllResultRows = new CommonsArrayList <> ();
    if (queryAll (sSQL, aCurrentObject -> {
      if (aCurrentObject != null)
      {
        // We need to clone the object!
        aAllResultRows.add (aCurrentObject.getClone ());
      }
    }).isFailure ())
      return Optional.empty ();
    return Optional.of (aAllResultRows);
  }

  @Nullable
  public Optional <ICommonsList <DBResultRow>> queryAll (@Nonnull @Nonempty final String sSQL,
                                                         @Nonnull final IPreparedStatementDataProvider aPSDP)
  {
    final ICommonsList <DBResultRow> aAllResultRows = new CommonsArrayList <> ();
    if (queryAll (sSQL, aPSDP, aCurrentObject -> {
      if (aCurrentObject != null)
      {
        // We need to clone the object!
        aAllResultRows.add (aCurrentObject.getClone ());
      }
    }).isFailure ())
      return Optional.empty ();
    return Optional.of (aAllResultRows);
  }

  @Nullable
  public Optional <DBResultRow> querySingle (@Nonnull @Nonempty final String sSQL)
  {
    return queryAll (sSQL).map (ICommonsList::getFirst);
  }

  @Nullable
  public Optional <DBResultRow> querySingle (@Nonnull @Nonempty final String sSQL, @Nonnull final IPreparedStatementDataProvider aPSDP)
  {
    return queryAll (sSQL, aPSDP).map (ICommonsList::getFirst);
  }

  @CheckForSigned
  public long queryCount (@Nonnull final String sSQL)
  {
    return querySingle (sSQL).map (x -> (Number) x.getValue (0)).orElse (MINUS1).longValue ();
  }

  @CheckForSigned
  public long queryCount (@Nonnull final String sSQL, @Nonnull final IPreparedStatementDataProvider aPSDP)
  {
    return querySingle (sSQL, aPSDP).map (x -> (Number) x.getValue (0)).orElse (MINUS1).longValue ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ConnectionProvider", m_aConnectionProvider)
                                       .append ("ExceptionCalbacks", m_aExceptionCallbacks)
                                       .append ("ConnectionExecutor", m_aConnectionExecutor)
                                       .append ("DebugConnections", m_bDebugConnections)
                                       .append ("DebugTransactions", m_bDebugTransactions)
                                       .append ("DebugSQLStatements", m_bDebugSQLStatements)
                                       .getToString ();
  }
}
