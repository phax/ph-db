/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.GuardedBy;
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
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.io.stream.NonBlockingBufferedReader;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.state.EChange;
import com.helger.commons.state.ESuccess;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.wrapper.Wrapper;
import com.helger.db.api.callback.IExecutionTimeExceededCallback;
import com.helger.db.api.callback.LoggingExecutionTimeExceededCallback;
import com.helger.db.api.jdbc.JDBCHelper;
import com.helger.db.jdbc.ConnectionFromDataSource;
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

  private static final AtomicLong COUNTER_CONNECTION = new AtomicLong (0);
  private static final AtomicLong COUNTER_CONNECTION_OPEN = new AtomicLong (0);
  private static final AtomicLong COUNTER_CONNECTION_CLOSE = new AtomicLong (0);
  private static final AtomicLong COUNTER_SQL_STATEMENT = new AtomicLong (0);
  private static final AtomicLong COUNTER_TRANSACTION = new AtomicLong (0);

  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  private static final AtomicBoolean CARE_ABOUT_CONNECTION_STATUS = new AtomicBoolean (false);
  @GuardedBy ("RW_LOCK")
  private static ETriState s_eConnectionEstablished = ETriState.UNDEFINED;
  @GuardedBy ("RW_LOCK")
  private static IConnectionStatusChangeCallback s_aConnectionStatusChangeCallback;

  private final IHasConnection m_aConnectionProvider;
  private final CallbackList <IExceptionCallback <? super Exception>> m_aExceptionCallbacks = new CallbackList <> ();
  private IConnectionExecutor m_aConnectionExecutor;

  private long m_nExecutionDurationWarnMS = DEFAULT_EXECUTION_DURATION_WARN_MS;
  private static final CallbackList <IExecutionTimeExceededCallback> EXECUTION_TIME_EXCEEDED_HANDLERS = new CallbackList <> ();

  static
  {
    EXECUTION_TIME_EXCEEDED_HANDLERS.add (new LoggingExecutionTimeExceededCallback (true));
  }

  private final AtomicInteger m_aTransactionLevel = new AtomicInteger (0);
  private boolean m_bDebugConnections = DEFAULT_DEBUG_CONNECTIONS;
  private boolean m_bDebugTransactions = DEFAULT_DEBUG_TRANSACTIONS;
  private boolean m_bDebugSQLStatements = DEFAULT_DEBUG_SQL_STATEMENTS;

  public DBExecutor (@Nonnull final IHasDataSource aDataSourceProvider)
  {
    this (ConnectionFromDataSource.create (aDataSourceProvider));
  }

  public DBExecutor (@Nonnull final IHasConnection aConnectionProvider)
  {
    ValueEnforcer.notNull (aConnectionProvider, "ConnectionProvider");
    m_aConnectionProvider = aConnectionProvider;
    m_aExceptionCallbacks.add (new LoggingExceptionCallback ());
    m_aConnectionExecutor = this::withNewConnectionDo;
  }

  /**
   * @return The internal connection provider. Never <code>null</code>. Don't
   *         use that except you know what you are doing.
   * @since 6.7.2
   */
  @Nonnull
  public final IHasConnection getConnectionProvider ()
  {
    return m_aConnectionProvider;
  }

  /**
   * Debug logging method. Only invoked if the respective "debug log" member is
   * set to true
   *
   * @param sMessage
   *        The message to log. May not be <code>null</code>.
   */
  protected static final void debugLog (@Nonnull final String sMessage)
  {
    if (LOGGER.isInfoEnabled ())
      LOGGER.info (sMessage);
  }

  /**
   * @return <code>true</code> if we care about the connection status,
   *         <code>false</code> if not. Default is <code>false</code>. Only if
   *         this method returns true,
   *         {@link #setConnectionEstablished(ETriState)} is called.
   * @since v6.6.1
   */
  public static final boolean isCareAboutConnectionStatus ()
  {
    return CARE_ABOUT_CONNECTION_STATUS.get ();
  }

  /**
   * @param bCare
   *        <code>true</code> if connection status should be cared about,
   *        <code>false</code> if not.
   * @since v6.6.1
   */
  public static final void setCareAboutConnectionStatus (final boolean bCare)
  {
    CARE_ABOUT_CONNECTION_STATUS.set (bCare);
  }

  /**
   * @return The current "connection established" state. Never
   *         <code>null</code>.
   */
  @Nonnull
  public static final ETriState getConnectionEstablished ()
  {
    return RW_LOCK.readLockedGet ( () -> s_eConnectionEstablished);
  }

  /**
   * Set the "Connection established" state. This method is only called, if
   * {@link #isCareAboutConnectionStatus()} returns <code>true</code>.
   *
   * @param eNewState
   *        The new state. May not be <code>null</code>.
   */
  public static final void setConnectionEstablished (@Nonnull final ETriState eNewState)
  {
    ValueEnforcer.notNull (eNewState, "NewState");
    if (eNewState != getConnectionEstablished ())
    {
      final Wrapper <ETriState> aOldState = new Wrapper <> ();

      // Change value
      final EChange eChange = RW_LOCK.writeLockedGet ( () -> {
        aOldState.set (s_eConnectionEstablished);
        // Check again in write lock
        if (eNewState == aOldState.get ())
          return EChange.UNCHANGED;

        s_eConnectionEstablished = eNewState;
        return EChange.CHANGED;
      });

      if (eChange.isChanged ())
      {
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("Setting connection established state from " + aOldState.get () + " to " + eNewState);

        // Callback only if something changed
        RW_LOCK.readLocked ( () -> {
          if (s_aConnectionStatusChangeCallback != null)
            s_aConnectionStatusChangeCallback.onConnectionStatusChanged (aOldState.get (), eNewState);
        });
      }
    }
  }

  /**
   * Reset the "Connection established" flag. This is a shortcut for
   * <code>setConnectionEstablished (ETriState.UNDEFINED)</code>.
   */
  public static final void resetConnectionEstablished ()
  {
    if (CARE_ABOUT_CONNECTION_STATUS.get ())
      setConnectionEstablished (ETriState.UNDEFINED);
  }

  /**
   * @return The callback to be invoked, if the connection status changes. May
   *         be <code>null</code>.
   */
  @Nullable
  public static final IConnectionStatusChangeCallback getConnectionStatusChangeCallback ()
  {
    return RW_LOCK.readLockedGet ( () -> s_aConnectionStatusChangeCallback);
  }

  /**
   * Set the callback to be invoked, if the connection status changes. Only one
   * callback can be invoked.
   *
   * @param aCB
   *        The callback to be invoked. May be <code>null</code>.
   */
  public static final void setConnectionStatusChangeCallback (@Nullable final IConnectionStatusChangeCallback aCB)
  {
    RW_LOCK.writeLocked ( () -> s_aConnectionStatusChangeCallback = aCB);
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

  /**
   * @return The number of milliseconds after which a warning is emitted or a
   *         value &le; 0 to indicate that the value is not relevant.
   */
  @CheckForSigned
  public final long getExecutionDurationWarnMS ()
  {
    return m_nExecutionDurationWarnMS;
  }

  /**
   * Check if the execution duration warning is enabled or not. This uses the
   * defined execution duration milliseconds.
   *
   * @return <code>true</code> if the execution duration warning is enabled,
   *         <code>false</code> if not.
   * @see #getExecutionDurationWarnMS()
   * @see #setExecutionDurationWarnMS(long)
   */
  public final boolean isExecutionDurationWarnEnabled ()
  {
    return m_nExecutionDurationWarnMS > 0;
  }

  /**
   * Set the execution duration warning milliseconds.
   *
   * @param nExecutionDurationWarnMS
   *        All values &gt; 0 enable the warning, all other values disable the
   *        warning.
   * @return this for chaining
   */
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
  @ReturnsMutableObject
  public static final CallbackList <IExecutionTimeExceededCallback> executionTimeExceededHandlers ()
  {
    return EXECUTION_TIME_EXCEEDED_HANDLERS;
  }

  public final void onExecutionTimeExceeded (@Nonnull final String sMsg, @Nonnegative final long nExecutionMillis)
  {
    EXECUTION_TIME_EXCEEDED_HANDLERS.forEach (x -> x.onExecutionTimeExceeded (sMsg, nExecutionMillis, m_nExecutionDurationWarnMS));
  }

  /**
   * @return <code>true</code> if DB connection creation and destruction should
   *         be debug logged, <code>false</code> otherwise.
   * @see #setDebugConnections(boolean)
   */
  public final boolean isDebugConnections ()
  {
    return m_bDebugConnections;
  }

  /**
   * Enable or disable the debug logging of DB connection actions.
   *
   * @param bDebugConnections
   *        <code>true</code> to enable debug logging, <code>false</code> to not
   *        do it
   * @return this for chaining
   * @see #isDebugConnections()
   */
  @Nonnull
  public final DBExecutor setDebugConnections (final boolean bDebugConnections)
  {
    m_bDebugConnections = bDebugConnections;
    return this;
  }

  /**
   * @return <code>true</code> if DB transaction handling should be debug
   *         logged, <code>false</code> otherwise.
   * @see #setDebugTransactions(boolean)
   */
  public final boolean isDebugTransactions ()
  {
    return m_bDebugTransactions;
  }

  /**
   * Enable or disable the debug logging of DB transaction actions.
   *
   * @param bDebugTransactions
   *        <code>true</code> to enable debug logging, <code>false</code> to not
   *        do it
   * @return this for chaining
   * @see #isDebugTransactions()
   */
  @Nonnull
  public final DBExecutor setDebugTransactions (final boolean bDebugTransactions)
  {
    m_bDebugTransactions = bDebugTransactions;
    return this;
  }

  /**
   * @return <code>true</code> if SQL statements should be debug logged,
   *         <code>false</code> otherwise.
   * @see #setDebugSQLStatements(boolean)
   */
  public final boolean isDebugSQLStatements ()
  {
    return m_bDebugSQLStatements;
  }

  /**
   * Enable or disable the debug logging of SQL statements.
   *
   * @param bDebugSQLStatements
   *        <code>true</code> to enable debug logging, <code>false</code> to not
   *        do it
   * @return this for chaining
   * @see #isDebugSQLStatements()
   */
  @Nonnull
  public final DBExecutor setDebugSQLStatements (final boolean bDebugSQLStatements)
  {
    m_bDebugSQLStatements = bDebugSQLStatements;
    return this;
  }

  @CodingStyleguideUnaware ("Needs to be synchronized!")
  @Nonnull
  protected final ESuccess withNewConnectionDo (@Nonnull final IWithConnectionCallback aCB,
                                                @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final long nConnectionID = COUNTER_CONNECTION.incrementAndGet ();

    if (getConnectionEstablished ().isFalse ())
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
      COUNTER_CONNECTION_OPEN.incrementAndGet ();
      aConnection = m_aConnectionProvider.getConnection ();
      if (aConnection == null)
      {
        if (LOGGER.isWarnEnabled ())
          LOGGER.warn ("  Failed to open SQL Connection [" + nConnectionID + "]");
        return ESuccess.FAILURE;
      }

      if (m_bDebugConnections)
        debugLog ("  Opened SQL Connection [" + nConnectionID + "] is " + aConnection);

      try
      {
        if (aConnection.isClosed ())
          throw new DBNoConnectionException ("Received a closed connection from provider " + m_aConnectionProvider);
      }
      catch (final SQLException ex)
      {
        // Ignore
      }

      if (CARE_ABOUT_CONNECTION_STATUS.get ())
        setConnectionEstablished (ETriState.TRUE);

      // Okay, connection was established

      // Now do the main work
      return withExistingConnectionDo (aConnection, aCB, aExtraExCB);
    }
    catch (final DBNoConnectionException ex)
    {
      // Error creating a connection
      if (CARE_ABOUT_CONNECTION_STATUS.get ())
      {
        setConnectionEstablished (ETriState.FALSE);
        if (LOGGER.isWarnEnabled ())
          LOGGER.warn ("Connection could not be established. Remembering this status.");
      }

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
          debugLog ("Now closing SQL Connection [" + nConnectionID + "] " + aConnection);
        if (JDBCHelper.close (aConnection).isSuccess ())
        {
          if (m_bDebugConnections)
            debugLog ("  Closed SQL Connection [" + nConnectionID + "] " + aConnection);
        }
        else
        {
          if (m_bDebugConnections)
            debugLog ("  Failed to close SQL Connection [" + nConnectionID + "] " + aConnection);
        }
        COUNTER_CONNECTION_CLOSE.incrementAndGet ();
      }

      if (m_bDebugConnections)
        debugLog ("Opened " + COUNTER_CONNECTION_OPEN.intValue () + " and closed " + COUNTER_CONNECTION_CLOSE.intValue () + " connections");
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
    final int nColCount = aGeneratedKeysRS.getMetaData ().getColumnCount ();
    final ICommonsList <ICommonsList <Object>> aValues = new CommonsArrayList <> ();
    while (aGeneratedKeysRS.next ())
    {
      final ICommonsList <Object> aRow = new CommonsArrayList <> (nColCount);
      for (int nCol = 1; nCol <= nColCount; ++nCol)
        aRow.add (aGeneratedKeysRS.getObject (nCol));
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
        final long nTransactionID = COUNTER_TRANSACTION.incrementAndGet ();
        if (m_bDebugTransactions)
          debugLog ("Starting a level " + nTransactionLevel + " transaction [" + nTransactionID + "]");

        // Avoid creating a new connection
        final IConnectionExecutor aOldConnectionExecutor = m_aConnectionExecutor;
        m_aConnectionExecutor = (aCB2, aExCB2) -> this.withExistingConnectionDo (aConnection, aCB2, aExCB2);

        try
        {
          // Run the callback
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
      final long nSQLStatementID = COUNTER_SQL_STATEMENT.incrementAndGet ();
      final String sWhat = "PreparedStatement [" + nSQLStatementID + "] <" + sSQL + "> with " + aPSDP.getValueCount () + " values";
      if (m_bDebugSQLStatements)
        debugLog ("Will execute " + sWhat);

      withTimingDo (sWhat, () -> {
        try (final PreparedStatement aPS = aConnection.prepareStatement (sSQL, Statement.RETURN_GENERATED_KEYS))
        {
          // Handle by JDBC driver
          // Oracle counts incorrectly
          if (false)
            if (aPS.getParameterMetaData ().getParameterCount () != aPSDP.getValueCount ())
            {
              throw new IllegalArgumentException ("parameter count (" +
                                                  aPS.getParameterMetaData ().getParameterCount () +
                                                  ") does not match passed column name count (" +
                                                  aPSDP.getValueCount () +
                                                  ")");
            }

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
      final long nSQLStatementID = COUNTER_SQL_STATEMENT.incrementAndGet ();
      final String sWhat = "Statement [" + nSQLStatementID + "] <" + sSQL + ">";
      if (m_bDebugSQLStatements)
        debugLog ("Will execute " + sWhat);

      withTimingDo (sWhat, () -> aStatement.execute (sSQL));
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

  /**
   * Execute a prepared statement and return the generated key.
   *
   * @param sSQL
   *        The SQL to execute. May not be <code>null</code>.
   * @param aPSDP
   *        The data provider for the prepared statement. May not be
   *        <code>null</code>.
   * @param aExtraExCB
   *        An additional exception callback for this execution only.
   * @return <code>null</code> if the execution failed (see exception handler)
   *         and no key was created, or a non-<code>null</code> key.
   */
  @Nonnull
  public Object executePreparedStatementAndGetGeneratedKey (@Nonnull final String sSQL,
                                                            @Nonnull final IPreparedStatementDataProvider aPSDP,
                                                            @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final GetSingleGeneratedKeyCallback aCB = new GetSingleGeneratedKeyCallback ();
    if (executePreparedStatement (sSQL, aPSDP, null, aCB, aExtraExCB).isFailure ())
      return null;
    return aCB.getGeneratedKey ();
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

  @Nonnull
  private static String _clobToString (@Nullable final java.sql.Clob aClob) throws SQLException
  {
    if (aClob == null)
      return "";

    final StringBuilder aSB = new StringBuilder ();
    try (final Reader aReader = aClob.getCharacterStream ();
         final NonBlockingBufferedReader aBufferedReader = new NonBlockingBufferedReader (aReader))
    {
      int ch;
      while ((ch = aBufferedReader.read ()) > -1)
        aSB.append ((char) ch);
    }
    catch (final IOException ex)
    {
      throw new SQLException ("Could not convert CLOB to String", ex);
    }

    return aSB.toString ();
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
          Object aColumnValue = aRS.getObject (i);
          if (aColumnTypes[i - 1] == Types.CLOB)
          {
            // Special CLOB handling
            final java.sql.Clob aClob = (java.sql.Clob) aColumnValue;
            final long nClobLength = aClob == null ? 0 : aClob.length ();
            if (nClobLength <= Integer.MAX_VALUE)
              aColumnValue = _clobToString (aClob);
            else
              LOGGER.warn ("The contained CLOB is larger than 2GB (" + nClobLength + " chars) and therefore not converted to a String");
          }

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

  /**
   * Perform an SQL query that does not contain any parameters.
   *
   * @param sSQL
   *        The SQL to query. May neither be <code>null</code> nor empty.
   * @param aResultItemCallback
   *        The result item callback to be invoked. May not be
   *        <code>null</code>.
   * @return {@link ESuccess} and never <code>null</code>.
   */
  @Nonnull
  public ESuccess queryAll (@Nonnull @Nonempty final String sSQL, @Nonnull final IResultSetRowCallback aResultItemCallback)
  {
    ValueEnforcer.notEmpty (sSQL, "SQL");
    ValueEnforcer.notNull (aResultItemCallback, "aResultItemCallbackSQL");

    return withStatementDo (aStatement -> {
      final long nSQLStatementID = COUNTER_SQL_STATEMENT.incrementAndGet ();
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

  /**
   * Perform an SQL query that does contains parameters to be filled with the
   * provided {@link IPreparedStatementDataProvider}.
   *
   * @param sSQL
   *        The SQL to query. May neither be <code>null</code> nor empty.
   * @param aPSDP
   *        The data provider for the SQL statement. May not be
   *        <code>null</code>.
   * @param aResultItemCallback
   *        The result item callback to be invoked. May not be
   *        <code>null</code>.
   * @return {@link ESuccess} and never <code>null</code>.
   */
  @Nonnull
  public ESuccess queryAll (@Nonnull @Nonempty final String sSQL,
                            @Nonnull final IPreparedStatementDataProvider aPSDP,
                            @Nonnull final IResultSetRowCallback aResultItemCallback)
  {
    ValueEnforcer.notEmpty (sSQL, "SQL");
    ValueEnforcer.notNull (aPSDP, "PreparedStatementDataProvider");
    ValueEnforcer.notNull (aResultItemCallback, "aResultItemCallbackSQL");

    return withPreparedStatementDo (sSQL, aPSDP, aPreparedStatement -> {
      final ResultSet aResultSet = aPreparedStatement.executeQuery ();
      final long nResultRows = iterateResultSet (aResultSet, aResultItemCallback);
      if (m_bDebugSQLStatements)
        debugLog ("  Found " + nResultRows + " result rows");
    }, (IUpdatedRowCountCallback) null, (IGeneratedKeysCallback) null, null);
  }

  /**
   * Query a list of 0-n rows with an SQL script without parameters.
   *
   * @param sSQL
   *        The SQL to query. May neither be <code>null</code> nor empty.
   * @return <code>null</code> in case of error (see the provided exception
   *         handler) or a non-<code>null</code> but maybe empty list if
   *         querying was successful.
   */
  @Nullable
  public ICommonsList <DBResultRow> queryAll (@Nonnull @Nonempty final String sSQL)
  {
    final ICommonsList <DBResultRow> aAllResultRows = new CommonsArrayList <> ();
    if (queryAll (sSQL, aCurrentObject -> {
      if (aCurrentObject != null)
      {
        // We need to clone the object!
        aAllResultRows.add (aCurrentObject.getClone ());
      }
    }).isFailure ())
      return null;
    return aAllResultRows;
  }

  /**
   * Query a list of 0-n rows with an SQL script with parameters.
   *
   * @param sSQL
   *        The SQL to query. May neither be <code>null</code> nor empty.
   * @param aPSDP
   *        The data provider for the SQL statement. May not be
   *        <code>null</code>.
   * @return <code>null</code> in case of error (see the provided exception
   *         handler) or a non-<code>null</code> but maybe empty list if
   *         querying was successful.
   */
  @Nullable
  public ICommonsList <DBResultRow> queryAll (@Nonnull @Nonempty final String sSQL, @Nonnull final IPreparedStatementDataProvider aPSDP)
  {
    final ICommonsList <DBResultRow> aAllResultRows = new CommonsArrayList <> ();
    if (queryAll (sSQL, aPSDP, aCurrentObject -> {
      if (aCurrentObject != null)
      {
        // We need to clone the object!
        aAllResultRows.add (aCurrentObject.getClone ());
      }
    }).isFailure ())
      return null;
    return aAllResultRows;
  }

  /**
   * Query a a single result row with an SQL script without parameters.
   *
   * @param sSQL
   *        The SQL to query. May neither be <code>null</code> nor empty.
   * @param aConsumer
   *        The consumer to be invoked with the result row. May not be
   *        <code>null</code>. This is necessary to differentiate a
   *        <code>null</code>-result was a "not found" or "a DB error". The
   *        consumer is only invoked if no exception occurred.
   * @return {@link ESuccess} and never <code>null</code>.
   */
  @Nonnull
  public ESuccess querySingle (@Nonnull @Nonempty final String sSQL, @Nonnull final Consumer <? super DBResultRow> aConsumer)
  {
    final ICommonsList <DBResultRow> aList = queryAll (sSQL);
    if (aList == null)
      return ESuccess.FAILURE;

    if (aList.size () > 1)
      LOGGER.warn ("The query '" + sSQL + "' returned " + aList.size () + " results but only the first one is used.");
    // No need to clone again - already cloned
    aConsumer.accept (aList.getFirst ());
    return ESuccess.SUCCESS;
  }

  /**
   * Query a a single result row with an SQL script with parameters.
   *
   * @param sSQL
   *        The SQL to query. May neither be <code>null</code> nor empty.
   * @param aPSDP
   *        The data provider for the SQL statement. May not be
   *        <code>null</code>.
   * @param aConsumer
   *        The consumer to be invoked with the result row. May not be
   *        <code>null</code>. This is necessary to differentiate a
   *        <code>null</code>-result was a "not found" or "a DB error". The
   *        consumer is only invoked if no exception occurred.
   * @return {@link ESuccess} and never <code>null</code>.
   */
  @Nonnull
  public ESuccess querySingle (@Nonnull @Nonempty final String sSQL,
                               @Nonnull final IPreparedStatementDataProvider aPSDP,
                               @Nonnull final Consumer <? super DBResultRow> aConsumer)
  {
    final ICommonsList <DBResultRow> aList = queryAll (sSQL, aPSDP);
    if (aList == null)
      return ESuccess.FAILURE;

    if (aList.size () > 1)
      LOGGER.warn ("The query '" + sSQL + "' returned " + aList.size () + " results but only the first one is used.");
    // No need to clone again - already cloned
    aConsumer.accept (aList.getFirst ());
    return ESuccess.SUCCESS;
  }

  @CheckForSigned
  public long queryCount (@Nonnull final String sSQL)
  {
    final Wrapper <DBResultRow> ret = new Wrapper <> ();
    querySingle (sSQL, ret::set);
    return ret.isNotSet () ? CGlobal.ILLEGAL_UINT : ((Number) ret.get ().getValue (0)).longValue ();
  }

  @CheckForSigned
  public long queryCount (@Nonnull final String sSQL, @Nonnull final IPreparedStatementDataProvider aPSDP)
  {
    final Wrapper <DBResultRow> ret = new Wrapper <> ();
    querySingle (sSQL, aPSDP, ret::set);
    return ret.isNotSet () ? CGlobal.ILLEGAL_UINT : ((Number) ret.get ().getValue (0)).longValue ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("ConnectionProvider", m_aConnectionProvider)
                                       .append ("ExceptionCalbacks", m_aExceptionCallbacks)
                                       .append ("ConnectionExecutor", m_aConnectionExecutor)
                                       .append ("ExecutionDurationWarnMS", m_nExecutionDurationWarnMS)
                                       .append ("TransactionLevel", m_aTransactionLevel)
                                       .append ("DebugConnections", m_bDebugConnections)
                                       .append ("DebugTransactions", m_bDebugTransactions)
                                       .append ("DebugSQLStatements", m_bDebugSQLStatements)
                                       .getToString ();
  }
}
