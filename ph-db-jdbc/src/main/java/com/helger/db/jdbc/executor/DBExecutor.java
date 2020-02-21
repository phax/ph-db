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
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.state.ESuccess;
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

  private static final Logger LOGGER = LoggerFactory.getLogger (DBExecutor.class);
  private static final Long MINUS1 = Long.valueOf (CGlobal.ILLEGAL_UINT);

  private final IHasConnection m_aConnectionProvider;
  private final CallbackList <IExceptionCallback <? super Exception>> m_aExceptionCallbacks = new CallbackList <> ();
  private IConnectionExecutor m_aConnectionExecutor;

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
  @ReturnsMutableObject
  public CallbackList <IExceptionCallback <? super Exception>> exceptionCallbacks ()
  {
    return m_aExceptionCallbacks;
  }

  @CodingStyleguideUnaware ("Needs to be synchronized!")
  @Nonnull
  protected final synchronized ESuccess withNewConnectionDo (@Nonnull final IWithConnectionCallback aCB,
                                                             @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    Connection aConnection = null;
    try
    {
      // Get connection
      aConnection = m_aConnectionProvider.getConnection ();
      if (aConnection == null)
        throw new SQLException ("Failed to get a connection from connection provider");

      // Okay, connection was established
      return withExistingConnectionDo (aConnection, aCB, aExtraExCB);
    }
    catch (final SQLException | RuntimeException ex)
    {
      // Error creating a connection
      // Invoke callback
      m_aExceptionCallbacks.forEach (x -> x.onException (ex));
      if (aExtraExCB != null)
        aExtraExCB.onException (ex);
      return ESuccess.FAILURE;
    }
    finally
    {
      // Close connection again (if necessary)
      if (m_aConnectionProvider.shouldCloseConnection ())
        JDBCHelper.close (aConnection);
    }
  }

  @Nonnull
  protected final ESuccess withExistingConnectionDo (@Nonnull final Connection aConnection,
                                                     @Nonnull final IWithConnectionCallback aCB,
                                                     @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    // Okay, connection was established
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
    return ESuccess.SUCCESS;
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
  public final ESuccess performInTransaction (@Nonnull final IThrowingRunnable <SQLException> aRunnable)
  {
    return performInTransaction (aRunnable, null);
  }

  @Nonnull
  public final ESuccess performInTransaction (@Nonnull final IThrowingRunnable <SQLException> aRunnable,
                                              @Nullable final IExceptionCallback <? super Exception> aExtraExCB)
  {
    final IWithConnectionCallback aWithConnectionCB = aConnection -> {
      // Disable auto commit
      final boolean bOldAutoCommit = aConnection.getAutoCommit ();
      if (!bOldAutoCommit)
        LOGGER.info ("Found a nested transaction");
      aConnection.setAutoCommit (false);

      // Avoid creating a new connection
      final IConnectionExecutor aOldConnectionExecutor = m_aConnectionExecutor;
      m_aConnectionExecutor = (aCB2, aExCB2) -> this.withExistingConnectionDo (aConnection, aCB2, aExCB2);

      try
      {
        aRunnable.run ();
        aConnection.commit ();
      }
      catch (final SQLException | RuntimeException ex)
      {
        aConnection.rollback ();
        throw ex;
      }
      finally
      {
        // Reset state
        m_aConnectionExecutor = aOldConnectionExecutor;
        aConnection.setAutoCommit (bOldAutoCommit);
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

        if (GlobalDebug.isDebugMode ())
          LOGGER.info ("Executing prepared statement: " + sSQL);

        // call callback
        aPSCallback.run (aPS);

        // Updated row count callback present?
        if (aUpdatedRowCountCB != null)
          aUpdatedRowCountCB.setUpdatedRowCount (aPS.getUpdateCount ());

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
      if (GlobalDebug.isDebugMode ())
        LOGGER.info ("Executing statement: " + sSQL);
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
  public int insertOrUpdateOrDelete (@Nonnull final String sSQL, @Nonnull final IPreparedStatementDataProvider aPSDP)
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
  public int insertOrUpdateOrDelete (@Nonnull final String sSQL,
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
    private final int m_nUpdateCount;
    private final Object m_aGeneratedKey;

    public CountAndKey (@Nonnegative final int nUpdateCount, @Nullable final Object aGeneratedKey)
    {
      m_nUpdateCount = nUpdateCount;
      m_aGeneratedKey = aGeneratedKey;
    }

    @Nonnegative
    public int getUpdateCount ()
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
    final int nUpdateCount = insertOrUpdateOrDelete (sSQL, aPSDP, aCB, aExtraExCB);
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
   * @throws SQLException
   *         on error
   */
  protected static final void iterateResultSet (@WillClose final ResultSet aRS,
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
      while (aRS.next ())
      {
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
      final ResultSet aResultSet = aStatement.executeQuery (sSQL);
      iterateResultSet (aResultSet, aResultItemCallback);
    }, (IGeneratedKeysCallback) null, null);
  }

  @Nonnull
  public ESuccess queryAll (@Nonnull final String sSQL,
                            @Nonnull final IPreparedStatementDataProvider aPSDP,
                            @Nonnull final IResultSetRowCallback aResultItemCallback)
  {
    return withPreparedStatementDo (sSQL, aPSDP, aPreparedStatement -> {
      final ResultSet aResultSet = aPreparedStatement.executeQuery ();
      iterateResultSet (aResultSet, aResultItemCallback);
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
                                       .getToString ();
  }
}
