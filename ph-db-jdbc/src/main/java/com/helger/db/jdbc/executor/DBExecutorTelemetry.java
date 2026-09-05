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

import java.sql.SQLException;
import java.time.Duration;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.CheckForSigned;
import com.helger.annotation.concurrent.Immutable;
import com.helger.base.timing.StopWatch;
import com.helger.collection.commons.CommonsHashSet;
import com.helger.collection.commons.ICommonsSet;
import com.helger.db.api.EDatabaseSystemType;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.telemetry.ETelemetrySpanKind;
import com.helger.telemetry.IThrowingSpanConsumer;
import com.helger.telemetry.ITelemetrySpan;
import com.helger.telemetry.Telemetry;
import com.helger.telemetry.TelemetryAttributes;

/**
 * Emits the ph-telemetry spans and metrics of {@link DBExecutor}. All emission happens through the
 * vendor neutral ph-telemetry facades, so without a registered SPI everything degrades to cheap
 * no-ops.
 * <p>
 * Every method takes the "telemetry enabled" flag of the calling {@link DBExecutor} as its first
 * parameter and does nothing at all if it is <code>false</code> - the body of the
 * <code>with...Do</code> methods is then invoked with the shared no-op span.
 *
 * @author Philip Helger
 * @since 8.5.0
 */
@Immutable
final class DBExecutorTelemetry
{
  /**
   * The SQL operations that may become the {@link CDBTelemetry#ATTR_DB_OPERATION_NAME} value. The
   * whitelist keeps the cardinality of that metric dimension bounded, even if something that is not
   * SQL at all is passed in.
   */
  private static final ICommonsSet <String> OPERATION_NAMES = new CommonsHashSet <> ("ALTER",
                                                                                     "CALL",
                                                                                     "COMMIT",
                                                                                     "CREATE",
                                                                                     "DELETE",
                                                                                     "DROP",
                                                                                     "GRANT",
                                                                                     "INSERT",
                                                                                     "MERGE",
                                                                                     "REPLACE",
                                                                                     "REVOKE",
                                                                                     "ROLLBACK",
                                                                                     "SELECT",
                                                                                     "SET",
                                                                                     "TRUNCATE",
                                                                                     "UPDATE",
                                                                                     "UPSERT",
                                                                                     "WITH");

  private DBExecutorTelemetry ()
  {}

  /**
   * @param aDuration
   *        The duration to convert. May not be <code>null</code>.
   * @return The provided duration in seconds - the unit all ph-db duration instruments use.
   */
  private static double _toSeconds (@NonNull final Duration aDuration)
  {
    return aDuration.toNanos () / 1_000_000_000.0;
  }

  /**
   * Create an attribute builder that is pre-filled with the attributes every ph-db JDBC metric
   * carries.
   *
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   * @return The new builder to be filled further. Never <code>null</code>.
   */
  private static TelemetryAttributes.Builder _createCommon (@Nullable final EDatabaseSystemType eDBSystemType)
  {
    final TelemetryAttributes.Builder ret = TelemetryAttributes.builder ();
    ret.put (CDBTelemetry.ATTR_COMPONENT, CDBTelemetry.COMPONENT_JDBC);
    if (eDBSystemType != null)
      ret.put (CDBTelemetry.ATTR_DB_SYSTEM_NAME, eDBSystemType.getID ());
    return ret;
  }

  /**
   * Add the attributes that every ph-db JDBC span carries.
   *
   * @param aSpan
   *        The span to fill. May not be <code>null</code>.
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   */
  private static void _setCommon (@NonNull final ITelemetrySpan aSpan,
                                  @Nullable final EDatabaseSystemType eDBSystemType)
  {
    aSpan.setAttribute (CDBTelemetry.ATTR_COMPONENT, CDBTelemetry.COMPONENT_JDBC);
    if (eDBSystemType != null)
      aSpan.setAttribute (CDBTelemetry.ATTR_DB_SYSTEM_NAME, eDBSystemType.getID ());
  }

  /**
   * Derive the SQL operation of a statement - the low cardinality dimension all statement metrics
   * are grouped by.
   *
   * @param sSQL
   *        The SQL statement to analyse. May not be <code>null</code>.
   * @return <code>null</code> if the statement does not start with one of the known SQL operations,
   *         the upper cased operation name otherwise.
   */
  @Nullable
  static String getOperationName (@NonNull final String sSQL)
  {
    final int nMax = sSQL.length ();
    int nStart = 0;
    while (nStart < nMax && Character.isWhitespace (sSQL.charAt (nStart)))
      nStart++;

    int nEnd = nStart;
    while (nEnd < nMax && Character.isLetter (sSQL.charAt (nEnd)))
      nEnd++;
    if (nEnd == nStart)
      return null;

    final String sOperation = sSQL.substring (nStart, nEnd).toUpperCase (Locale.ROOT);
    return OPERATION_NAMES.contains (sOperation) ? sOperation : null;
  }

  /**
   * Run the execution of a single SQL statement inside a span and record the statement metrics.
   *
   * @param bTelemetry
   *        <code>true</code> if telemetry is enabled. If <code>false</code>, the body is invoked
   *        with the no-op span and nothing is emitted.
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   * @param sSQL
   *        The SQL statement to be executed. May not be <code>null</code>.
   * @param bWithSQLText
   *        <code>true</code> if the SQL text may be attached to the span.
   * @param bPrepared
   *        <code>true</code> if a <code>PreparedStatement</code> is used.
   * @param nParameterCount
   *        The number of prepared statement parameters, or a negative value if not applicable.
   * @param aBody
   *        The statement execution. May not be <code>null</code>.
   * @throws SQLException
   *         if the body throws it.
   */
  static void withStatementDo (final boolean bTelemetry,
                               @Nullable final EDatabaseSystemType eDBSystemType,
                               @NonNull final String sSQL,
                               final boolean bWithSQLText,
                               final boolean bPrepared,
                               @CheckForSigned final int nParameterCount,
                               @NonNull final IThrowingSpanConsumer <SQLException> aBody) throws SQLException
  {
    if (!bTelemetry)
    {
      aBody.accept (Telemetry.NoOpTelemetrySpan.INSTANCE);
      return;
    }

    final String sOperation = getOperationName (sSQL);
    final StopWatch aSW = StopWatch.createdStarted ();
    String sErrorType = null;
    try
    {
      Telemetry.<SQLException> withSpanVoidThrowing (sOperation != null ? sOperation : CDBTelemetry.SPAN_JDBC_QUERY,
                                                     ETelemetrySpanKind.CLIENT,
                                                     aSpan -> {
                                                       _setCommon (aSpan, eDBSystemType);
                                                       if (sOperation != null)
                                                         aSpan.setAttribute (CDBTelemetry.ATTR_DB_OPERATION_NAME,
                                                                             sOperation);
                                                       if (bWithSQLText)
                                                         aSpan.setAttribute (CDBTelemetry.ATTR_DB_QUERY_TEXT, sSQL);
                                                       aSpan.setAttribute (CDBTelemetry.ATTR_JDBC_PREPARED, bPrepared);
                                                       if (nParameterCount >= 0)
                                                         aSpan.setAttribute (CDBTelemetry.ATTR_JDBC_PARAMETER_COUNT,
                                                                             nParameterCount);

                                                       aBody.accept (aSpan);
                                                       aSpan.setStatusOk ();
                                                     });
    }
    catch (final SQLException | RuntimeException ex)
    {
      sErrorType = ex.getClass ().getName ();
      throw ex;
    }
    finally
    {
      final TelemetryAttributes.Builder aBuilder = _createCommon (eDBSystemType);
      if (sOperation != null)
        aBuilder.put (CDBTelemetry.ATTR_DB_OPERATION_NAME, sOperation);
      aBuilder.put (CDBTelemetry.ATTR_SUCCESS, sErrorType == null);
      // Only present in case of an error, as the conventions demand
      aBuilder.put (CDBTelemetry.ATTR_ERROR_TYPE, sErrorType);
      final TelemetryAttributes aAttrs = aBuilder.build ();

      DBExecutorMetrics.STATEMENTS.add (1, aAttrs);
      DBExecutorMetrics.OPERATION_DURATION.record (_toSeconds (aSW.stopAndGetDuration ()), aAttrs);
    }
  }

  /**
   * Run a transaction inside a span. The metrics are emitted by
   * {@link #onTransactionEnd(boolean, ITelemetrySpan, EDatabaseSystemType, String, int)}, because
   * only the body knows how the transaction ended.
   *
   * @param bTelemetry
   *        <code>true</code> if telemetry is enabled. If <code>false</code>, the body is invoked
   *        with the no-op span and nothing is emitted.
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   * @param nTransactionID
   *        The internal ID of the transaction.
   * @param nTransactionLevel
   *        The nesting level of the transaction - the outermost transaction has level 1.
   * @param aBody
   *        The transaction body. May not be <code>null</code>.
   * @throws SQLException
   *         if the body throws it.
   */
  static void withTransactionDo (final boolean bTelemetry,
                                 @Nullable final EDatabaseSystemType eDBSystemType,
                                 final long nTransactionID,
                                 final int nTransactionLevel,
                                 @NonNull final IThrowingSpanConsumer <SQLException> aBody) throws SQLException
  {
    if (!bTelemetry)
    {
      aBody.accept (Telemetry.NoOpTelemetrySpan.INSTANCE);
      return;
    }

    Telemetry.<SQLException> withSpanVoidThrowing (CDBTelemetry.SPAN_JDBC_TRANSACTION,
                                                   ETelemetrySpanKind.CLIENT,
                                                   aSpan -> {
                                                     _setCommon (aSpan, eDBSystemType);
                                                     aSpan.setAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_ID,
                                                                         nTransactionID);
                                                     aSpan.setAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_LEVEL,
                                                                         nTransactionLevel);
                                                     aSpan.setAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_NESTED,
                                                                         nTransactionLevel > 1);
                                                     aBody.accept (aSpan);
                                                   });
  }

  /**
   * Mark the end of a transaction on the span and count it.
   *
   * @param bTelemetry
   *        <code>true</code> if telemetry is enabled. If <code>false</code>, nothing is emitted.
   * @param aSpan
   *        The span of the transaction. May not be <code>null</code>.
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   * @param sOutcome
   *        How the transaction ended. May not be <code>null</code>.
   * @param nTransactionLevel
   *        The nesting level of the transaction.
   */
  static void onTransactionEnd (final boolean bTelemetry,
                                @NonNull final ITelemetrySpan aSpan,
                                @Nullable final EDatabaseSystemType eDBSystemType,
                                @NonNull final String sOutcome,
                                final int nTransactionLevel)
  {
    if (!bTelemetry)
      return;

    aSpan.setAttribute (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME, sOutcome);
    if (!CDBTelemetry.TRANSACTION_OUTCOME_ROLLED_BACK.equals (sOutcome))
    {
      // A rolled back transaction rethrows, so that the surrounding span sets the error status
      aSpan.setStatusOk ();
    }

    DBExecutorMetrics.TRANSACTIONS.add (1,
                                        _createCommon (eDBSystemType).put (CDBTelemetry.ATTR_JDBC_TRANSACTION_OUTCOME,
                                                                           sOutcome)
                                                                     .put (CDBTelemetry.ATTR_JDBC_TRANSACTION_NESTED,
                                                                           nTransactionLevel > 1)
                                                                     .build ());
  }

  /**
   * Record the result of a connection acquisition.
   *
   * @param bTelemetry
   *        <code>true</code> if telemetry is enabled. If <code>false</code>, nothing is emitted.
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   * @param aDuration
   *        The time it took to acquire the connection. May not be <code>null</code>.
   * @param bSuccess
   *        <code>true</code> if a connection was acquired.
   */
  static void onConnectionAcquired (final boolean bTelemetry,
                                    @Nullable final EDatabaseSystemType eDBSystemType,
                                    @NonNull final Duration aDuration,
                                    final boolean bSuccess)
  {
    if (!bTelemetry)
      return;

    final TelemetryAttributes aAttrs = _createCommon (eDBSystemType).put (CDBTelemetry.ATTR_JDBC_CONNECTION_OUTCOME,
                                                                         bSuccess ? CDBTelemetry.CONNECTION_OUTCOME_ACQUIRED
                                                                                  : CDBTelemetry.CONNECTION_OUTCOME_FAILED)
                                                                    .build ();
    DBExecutorMetrics.CONNECTIONS.add (1, aAttrs);
    DBExecutorMetrics.CONNECTION_ACQUIRE_DURATION.record (_toSeconds (aDuration), aAttrs);
  }

  /**
   * Record a connection that was not even tried, because a previous attempt already failed.
   *
   * @param bTelemetry
   *        <code>true</code> if telemetry is enabled. If <code>false</code>, nothing is emitted.
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   */
  static void onConnectionRefused (final boolean bTelemetry, @Nullable final EDatabaseSystemType eDBSystemType)
  {
    if (!bTelemetry)
      return;

    DBExecutorMetrics.CONNECTIONS.add (1,
                                       _createCommon (eDBSystemType).put (CDBTelemetry.ATTR_JDBC_CONNECTION_OUTCOME,
                                                                          CDBTelemetry.CONNECTION_OUTCOME_REFUSED)
                                                                    .build ());
  }

  /**
   * Change the number of connections that are currently in use.
   *
   * @param bTelemetry
   *        <code>true</code> if telemetry is enabled. If <code>false</code>, nothing is emitted.
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   * @param nDelta
   *        <code>+1</code> when a connection was taken into use, <code>-1</code> when it was
   *        released.
   */
  static void addActiveConnections (final boolean bTelemetry,
                                    @Nullable final EDatabaseSystemType eDBSystemType,
                                    final long nDelta)
  {
    if (!bTelemetry)
      return;

    DBExecutorMetrics.CONNECTIONS_ACTIVE.add (nDelta, _createCommon (eDBSystemType).build ());
  }
}
