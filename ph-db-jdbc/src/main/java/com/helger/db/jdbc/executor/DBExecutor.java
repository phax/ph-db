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
import com.helger.db.api.jdbc.JDBCHelper;
import com.helger.db.jdbc.ConnectionFromDataSourceProvider;
import com.helger.db.jdbc.IHasConnection;
import com.helger.db.jdbc.IHasDataSource;
import com.helger.db.jdbc.callback.GetSingleGeneratedKeyCallback;
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
  public interface IWithConnectionCallback extends ICallback
  {
    void run (@Nonnull Connection aConnection) throws SQLException;
  }

  @FunctionalInterface
  public interface IWithStatementCallback extends ICallback
  {
    void run (@Nonnull Statement aStatement) throws SQLException;
  }

  @FunctionalInterface
  public interface IWithPreparedStatementCallback extends ICallback
  {
    void run (@Nonnull PreparedStatement aPreparedStatement) throws SQLException;
  }

  @FunctionalInterface
  public interface IConnectionExecutor
  {
    @Nonnull
    ESuccess execute (@Nonnull IWithConnectionCallback aCB,
                      @Nullable IExceptionCallback <? super Exception> aExtraExCB);
  }

  @FunctionalInterface
  public interface IConnectionEstablishedChangeCallback extends ICallback
  {
    void onConnectionEstablishedChange (@Nonnull ETriState eOld, @Nonnull ETriState eNew);
  }

  public static final boolean DEFAULT_DEBUG_CONNECTIONS = false;
  public static final boolean DEFAULT_DEBUG_TRANSACTIONS = false;
  public static final boolean DEFAULT_DEBUG_SQL_STATEMENTS = false;
  private static final Logger LOGGER = LoggerFactory.getLogger (DBExecutor.class);
  private static final Long MINUS1 = Long.valueOf (CGlobal.ILLEGAL_UINT);

  private final IHasConnection m_aConnectionProvider;
  private ETriState m_eConnectionEstablished = ETriState.UNDEFINED;
  private IConnectionEstablishedChangeCallback m_aConnectionEstablishedCallback;
  private final CallbackList <IExceptionCallback <? super Exception>> m_aExceptionCallbacks = new CallbackList <> ();
  private IConnectionExecutor m_aConnectionExecutor;

  private final AtomicLong m_aConnectionCounter = new AtomicLong (0);
  private final AtomicLong m_aSQLStatementCounter = new AtomicLong (0);
  private final AtomicLong m_aTransactionCounter = new AtomicLong (0);
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
  }

  @Nonnull
  public final ETriState getConnectionEstablished ()
  {
    return m_eConnectionEstablished;
  }

  @Nonnull
  public final DBExecutor setConnectionEstablished (@Nonnull final ETriState eNewState)
  {
    ValueEnforcer.notNull (eNewState, "NewState");
    if (eNewState != m_eConnectionEstablished)
    {
      final ETriState eOldState = m_eConnectionEstablished;
      if (m_bDebugConnections && LOGGER.isInfoEnabled ())
        LOGGER.info ("Setting connection established state from " + eOldState + " to " + eNewState);
      m_eConnectionEstablished = eNewState;

      if (m_aConnectionEstablishedCallback != null)
        m_aConnectionEstablishedCallback.onConnectionEstablishedChange (eOldState, eNewState);
    }
    return this;
  }

  @Nonnull
  public final DBExecutor resetConnectionEstablished ()
  {
    return setConnectionEstablished (ETriState.UNDEFINED);
  }

  @Nonnull
  public final IConnectionEstablishedChangeCallback getConnectionEstablishedChangeCallback ()
  {
    return m_aConnectionEstablishedCallback;
  }

  @Nonnull
  public final DBExecutor setConnectionEstablishedChangeCallback (@Nullable final IConnectionEstablishedChangeCallback aCB)
  {
    m_aConnectionEstablishedCallback = aCB;
    return this;
  }

  @Nonnull
  @ReturnsMutableObject
  public final CallbackList <IExceptionCallback <? super Exception>> exceptionCallbacks ()
  {
    return m_aExceptionCallbacks;
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
    final long nConnectionID = m_aConnectionCounter.incrementAndGet ();

    if (m_eConnectionEstablished.isFalse ())
    {
      // Avoid trying again
      if (m_bDebugConnections && LOGGER.isInfoEnabled ())
        LOGGER.info ("Refuse to open SQL Connection [" + nConnectionID + "] because it failed previously");
      return ESuccess.FAILURE;
    }

    Connection aConnection = null;
    try
    {
      if (m_bDebugConnections && LOGGER.isInfoEnabled ())
        LOGGER.info ("Opening a new SQL Connection [" + nConnectionID + "]");

      // Get connection
      aConnection = m_aConnectionProvider.getConnection ();
      if (aConnection == null)
        return ESuccess.FAILURE;

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
        if (m_bDebugConnections && LOGGER.isInfoEnabled ())
          LOGGER.info ("Now closing SQL Connection [" + nConnectionID + "]");
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
      final long nTransactionID = m_aTransactionCounter.incrementAndGet ();

      // Disable auto commit
      final boolean bOldAutoCommit = aConnection.getAutoCommit ();
      if (m_bDebugTransactions && LOGGER.isInfoEnabled ())
      {
        if (bOldAutoCommit)
          LOGGER.info ("Starting a transaction [" + nTransactionID + "]");
        else
          LOGGER.info ("Starting a nested transaction [" + nTransactionID + "]");
      }
      aConnection.setAutoCommit (false);

      // Avoid creating a new connection
      final IConnectionExecutor aOldConnectionExecutor = m_aConnectionExecutor;
      m_aConnectionExecutor = (aCB2, aExCB2) -> this.withExistingConnectionDo (aConnection, aCB2, aExCB2);

      try
      {
        aRunnable.run ();

        // Commit
        aConnection.commit ();
      }
      catch (final Exception ex)
      {
        // Rollback
        aConnection.rollback ();

        // Exception handler
        if (aExtraExCB != null)
          aExtraExCB.onException (ex);

        // Propagate
        if (ex instanceof RuntimeException)
          throw (RuntimeException) ex;
        if (ex instanceof SQLException)
          throw (SQLException) ex;
        throw new SQLException ("Caught exception while perfoming something in a transaction", ex);
      }
      finally
      {
        // Reset state
        m_aConnectionExecutor = aOldConnectionExecutor;
        aConnection.setAutoCommit (bOldAutoCommit);

        if (m_bDebugTransactions && LOGGER.isInfoEnabled ())
        {
          if (bOldAutoCommit)
            LOGGER.info ("Finished transaction [" + nTransactionID + "]");
          else
            LOGGER.info ("Finished nested transaction [" + nTransactionID + "]");
        }
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

  @Nonnull
  protected final ESuccess withPreparedStatementDo (@Nonnull final String sSQL,
                                                    @Nonnull final IPreparedStatementDataProvider aPSDP,
                                                    @Nonnull final IWithPreparedStatementCallback aPSCallback,
                                                    @Nullable final IUpdatedRowCountCallback aUpdatedRowCountCB,
                                                    @Nullable final IGeneratedKeysCallback aGeneratedKeysCB,
                                                    @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final IWithConnectionCallback aWithConnectionCB = aConnection -> {
      final long nSQLStatementID = m_aSQLStatementCounter.incrementAndGet ();

      if (m_bDebugSQLStatements && LOGGER.isInfoEnabled ())
        LOGGER.info ("Will run PreparedStatement [" +
                     nSQLStatementID +
                     "] <" +
                     sSQL +
                     "> with " +
                     aPSDP.getValueCount () +
                     " values");

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
      final long nSQLStatementID = m_aSQLStatementCounter.incrementAndGet ();
      if (m_bDebugSQLStatements && LOGGER.isInfoEnabled ())
        LOGGER.info ("Will execute statement [" + nSQLStatementID + "] <" + sSQL + ">");

      aStatement.execute (sSQL);
    }, aGeneratedKeysCB, aExtraExCB);
  }

  @Nonnull
  public ESuccess executePreparedStatement (@Nonnull final String sSQL,
                                            @Nonnull final IPreparedStatementDataProvider aPSDP)
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
    return withPreparedStatementDo (sSQL, aPSDP, aPS -> aPS.execute (), aURWCC, aGeneratedKeysCB, aExtraExCB);
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
    withPreparedStatementDo (sSQL, aPSDP, aPS -> aPS.execute (), aURCCB, aGeneratedKeysCB, aExtraExCB);
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
    return new CountAndKey (nUpdateCount,
                            nUpdateCount != IUpdatedRowCountCallback.NOT_INITIALIZED ? aCB.getGeneratedKey () : null);
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
  public ESuccess queryAll (@Nonnull @Nonempty final String sSQL,
                            @Nonnull final IResultSetRowCallback aResultItemCallback)
  {
    return withStatementDo (aStatement -> {
      final long nSQLStatementID = m_aSQLStatementCounter.incrementAndGet ();
      if (m_bDebugSQLStatements && LOGGER.isInfoEnabled ())
        LOGGER.info ("Will execute SQL query [" + nSQLStatementID + "] <" + sSQL + ">");

      final ResultSet aResultSet = aStatement.executeQuery (sSQL);
      final long nResultRows = iterateResultSet (aResultSet, aResultItemCallback);

      if (m_bDebugSQLStatements && LOGGER.isInfoEnabled ())
        LOGGER.info ("  Found " + nResultRows + " result rows [" + nSQLStatementID + "]");
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
      if (m_bDebugSQLStatements && LOGGER.isInfoEnabled ())
        LOGGER.info ("  Found " + nResultRows + " result rows");
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
  public Optional <DBResultRow> querySingle (@Nonnull @Nonempty final String sSQL,
                                             @Nonnull final IPreparedStatementDataProvider aPSDP)
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
