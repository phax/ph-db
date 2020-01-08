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
package com.helger.db.jpa;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.callback.CallbackList;
import com.helger.commons.callback.IThrowingRunnable;
import com.helger.commons.callback.exception.IExceptionCallback;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.statistics.IMutableStatisticsHandlerCounter;
import com.helger.commons.statistics.IMutableStatisticsHandlerTimer;
import com.helger.commons.statistics.StatisticsManager;
import com.helger.commons.timing.StopWatch;
import com.helger.db.jpa.callback.IExecutionTimeExceededCallback;
import com.helger.db.jpa.callback.LoggingExecutionTimeExceededCallback;

/**
 * JPA enabled manager with transaction handling etc. The
 * {@link IHasEntityManager} required in the constructor should be a request
 * singleton that ensures one {@link EntityManager} per thread. The main
 * {@link EntityManager} objects are usually create from a subclass of
 * {@link AbstractGlobalEntityManagerFactory}.
 *
 * @author Philip Helger
 */
@ThreadSafe
public class JPAEnabledManager
{
  /** By default the entity manager is not locked (changed in 3.0.0) */
  public static final boolean DEFAULT_SYNC_ENTITY_MGR = false;
  /** By default nested transactions are not allowed */
  public static final boolean DEFAULT_ALLOW_NESTED_TRANSACTIONS = false;
  /** By default no transaction is used for select statements */
  public static final boolean DEFAULT_USE_TRANSACTIONS_FOR_SELECT = false;
  /** By default execution time warnings are enabled */
  public static final boolean DEFAULT_EXECUTION_WARN_ENABLED = true;
  /** The default execution time after which a warning is emitted */
  public static final int DEFAULT_EXECUTION_WARN_TIME_MS = 1000;

  private static final Logger LOGGER = LoggerFactory.getLogger (JPAEnabledManager.class);
  private static final IMutableStatisticsHandlerCounter s_aStatsCounterTransactions = StatisticsManager.getCounterHandler (JPAEnabledManager.class.getName () +
                                                                                                                           "$transactions");
  private static final IMutableStatisticsHandlerCounter s_aStatsCounterRollback = StatisticsManager.getCounterHandler (JPAEnabledManager.class.getName () +
                                                                                                                       "$rollback");
  private static final IMutableStatisticsHandlerCounter s_aStatsCounterSuccess = StatisticsManager.getCounterHandler (JPAEnabledManager.class.getName () +
                                                                                                                      "$success");
  private static final IMutableStatisticsHandlerCounter s_aStatsCounterError = StatisticsManager.getCounterHandler (JPAEnabledManager.class.getName () +
                                                                                                                    "$error");
  private static final IMutableStatisticsHandlerTimer s_aStatsTimerExecutionSuccess = StatisticsManager.getTimerHandler (JPAEnabledManager.class.getName () +
                                                                                                                         "$execSuccess");
  private static final IMutableStatisticsHandlerTimer s_aStatsTimerExecutionError = StatisticsManager.getTimerHandler (JPAEnabledManager.class.getName () +
                                                                                                                       "$execError");

  protected static final SimpleReadWriteLock s_aRWLock = new SimpleReadWriteLock ();
  private static final CallbackList <IExceptionCallback <Throwable>> s_aExceptionCallbacks = new CallbackList <> ();
  private static final AtomicBoolean s_aExecutionWarnEnabled = new AtomicBoolean (DEFAULT_EXECUTION_WARN_ENABLED);
  private static final AtomicInteger s_aExecutionWarnTime = new AtomicInteger (DEFAULT_EXECUTION_WARN_TIME_MS);
  private static final CallbackList <IExecutionTimeExceededCallback> s_aExecutionTimeExceededHandlers = new CallbackList <> ();

  static
  {
    // Add default handler
    s_aExceptionCallbacks.add (x -> LOGGER.error ("Failed to perform something in a JPAEnabledManager!", x));
    s_aExecutionTimeExceededHandlers.add (new LoggingExecutionTimeExceededCallback (true));
  }

  private final IHasEntityManager m_aEntityManagerProvider;
  private final AtomicBoolean m_aSyncEntityMgr = new AtomicBoolean (DEFAULT_SYNC_ENTITY_MGR);
  private final AtomicBoolean m_aAllowNestedTransactions = new AtomicBoolean (DEFAULT_ALLOW_NESTED_TRANSACTIONS);
  private final AtomicBoolean m_aUseTransactionsForSelect = new AtomicBoolean (DEFAULT_USE_TRANSACTIONS_FOR_SELECT);

  public JPAEnabledManager (@Nonnull final IHasEntityManager aEntityManagerProvider)
  {
    ValueEnforcer.notNull (aEntityManagerProvider, "EntityManagerProvider");
    m_aEntityManagerProvider = aEntityManagerProvider;
  }

  public final boolean isSyncEntityMgr ()
  {
    return m_aSyncEntityMgr.get ();
  }

  /**
   * Set whether the entity manager should be synchronized upon each access
   *
   * @param bSyncEntityMgr
   *        <code>true</code> to enable sync, <code>false</code> to disable sync
   * @return this for chaining
   */
  @Nonnull
  public final JPAEnabledManager setSyncEntityMgr (final boolean bSyncEntityMgr)
  {
    m_aSyncEntityMgr.set (bSyncEntityMgr);
    return this;
  }

  public final boolean isAllowNestedTransactions ()
  {
    return m_aAllowNestedTransactions.get ();
  }

  /**
   * Allow nested transaction
   *
   * @param bAllowNestedTransactions
   *        <code>true</code> to enable nested transaction
   * @return this for chaining
   */
  @Nonnull
  public final JPAEnabledManager setAllowNestedTransactions (final boolean bAllowNestedTransactions)
  {
    m_aAllowNestedTransactions.set (bAllowNestedTransactions);
    return this;
  }

  /**
   * @return <code>true</code> if transactions should be used for selecting,
   *         <code>false</code> if this can be done without transactions
   */
  public final boolean isUseTransactionsForSelect ()
  {
    return m_aUseTransactionsForSelect.get ();
  }

  /**
   * Use transactions for select statements?
   *
   * @param bUseTransactionsForSelect
   *        <code>true</code> to enable the usage of transactions for select
   *        statements.
   * @return this for chaining
   */
  @Nonnull
  public final JPAEnabledManager setUseTransactionsForSelect (final boolean bUseTransactionsForSelect)
  {
    m_aAllowNestedTransactions.set (bUseTransactionsForSelect);
    return this;
  }

  /**
   * @return Get the entity manager to be used. Must not be <code>null</code>.
   */
  @Nonnull
  protected final EntityManager getEntityManager ()
  {
    return m_aEntityManagerProvider.getEntityManager ();
  }

  /**
   * Get the custom exception handler list for modification.
   *
   * @return <code>null</code> if non is set
   */
  @Nonnull
  @ReturnsMutableObject
  public static final CallbackList <IExceptionCallback <Throwable>> exceptionCallbacks ()
  {
    return s_aExceptionCallbacks;
  }

  /**
   * Invoke the custom exception handler (if present)
   *
   * @param ex
   *        The exception that occurred.
   */
  private static void _invokeCustomExceptionCallback (@Nonnull final Throwable ex)
  {
    s_aExceptionCallbacks.forEach (x -> x.onException (ex));
  }

  /**
   * @return <code>true</code> if long running execution warnings via callback
   *         are enabled, <code>false</code> if not. Default is
   *         <code>true</code>.
   * @see #getDefaultExecutionWarnTime()
   * @see #executionTimeExceededHandlers()
   * @since 6.1.0
   */
  public static final boolean isDefaultExecutionWarnTimeEnabled ()
  {
    return s_aExecutionWarnEnabled.get ();
  }

  /**
   * Enable or disable the warning if execution time exceeds a certain limit.
   * Default is <code>true</code>.
   *
   * @param bEnabled
   *        <code>true</code> to enable it, <code>false</code> to disable it.
   * @see #setDefaultExecutionWarnTime(int)
   * @see #executionTimeExceededHandlers()
   * @since 6.1.0
   */
  public static final void setDefaultExecutionWarnTimeEnabled (final boolean bEnabled)
  {
    s_aExecutionWarnEnabled.set (bEnabled);
  }

  /**
   * @return The milliseconds after which a warning is emitted, if an SQL
   *         statement takes longer to execute.
   * @see #isDefaultExecutionWarnTimeEnabled()
   */
  @Nonnegative
  public static final int getDefaultExecutionWarnTime ()
  {
    return s_aExecutionWarnTime.get ();
  }

  /**
   * Set the milliseconds duration on which a warning should be emitted, if a
   * single SQL execution too at least that long.
   *
   * @param nMillis
   *        The number of milliseconds. Must be &ge; 0.
   * @see #setDefaultExecutionWarnTimeEnabled(boolean)
   */
  public static final void setDefaultExecutionWarnTime (final int nMillis)
  {
    ValueEnforcer.isGE0 (nMillis, "Milliseconds");
    s_aExecutionWarnTime.set (nMillis);
  }

  /**
   * Get the custom exception handler list.
   *
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static final CallbackList <IExecutionTimeExceededCallback> executionTimeExceededHandlers ()
  {
    return s_aExecutionTimeExceededHandlers;
  }

  public static final void onExecutionTimeExceeded (@Nonnull final String sMsg,
                                                    @Nonnegative final long nExecutionMillis)
  {
    s_aExecutionTimeExceededHandlers.forEach (x -> x.onExecutionTimeExceeded (sMsg, nExecutionMillis));
  }

  @Nonnull
  public static final JPAExecutionResult <?> doInTransaction (@Nonnull @WillNotClose final EntityManager aEntityMgr,
                                                              final boolean bAllowNestedTransactions,
                                                              @Nonnull final Runnable aRunnable)
  {
    return doInTransaction (aEntityMgr, bAllowNestedTransactions, () -> {
      aRunnable.run ();
      return null;
    });
  }

  @Nonnull
  public static final JPAExecutionResult <?> doInTransaction (@Nonnull @WillNotClose final EntityManager aEntityMgr,
                                                              final boolean bAllowNestedTransactions,
                                                              @Nonnull final IThrowingRunnable <Exception> aRunnable)
  {
    return doInTransaction (aEntityMgr, bAllowNestedTransactions, () -> {
      aRunnable.run ();
      return null;
    });
  }

  @Nonnull
  public final JPAExecutionResult <?> doInTransaction (@Nonnull final IThrowingRunnable <Exception> aRunnable)
  {
    // Create entity manager
    final EntityManager aEntityMgr = getEntityManager ();
    if (!isSyncEntityMgr ())
    {
      // No synchronization required
      return doInTransaction (aEntityMgr, isAllowNestedTransactions (), aRunnable);
    }

    // Sync on the whole entity manager, to have a cross-manager
    // synchronization!
    synchronized (aEntityMgr)
    {
      return doInTransaction (aEntityMgr, isAllowNestedTransactions (), aRunnable);
    }
  }

  @Nonnull
  public static final <T> JPAExecutionResult <T> doInTransaction (@Nonnull @WillNotClose final EntityManager aEntityMgr,
                                                                  final boolean bAllowNestedTransactions,
                                                                  @Nonnull final Callable <T> aCallable)
  {
    final StopWatch aSW = StopWatch.createdStarted ();
    final EntityTransaction aTransaction = aEntityMgr.getTransaction ();
    final boolean bTransactionRequired = !bAllowNestedTransactions || !aTransaction.isActive ();
    if (bTransactionRequired)
    {
      s_aStatsCounterTransactions.increment ();
      aTransaction.begin ();
    }
    try
    {
      // Execute whatever you want to do
      final T ret = aCallable.call ();
      // And if no exception was thrown, commit it
      if (bTransactionRequired)
        aTransaction.commit ();
      s_aStatsCounterSuccess.increment ();
      s_aStatsTimerExecutionSuccess.addTime (aSW.stopAndGetMillis ());
      return JPAExecutionResult.createSuccess (ret);
    }
    catch (final Exception ex)
    {
      s_aStatsCounterError.increment ();
      s_aStatsTimerExecutionError.addTime (aSW.stopAndGetMillis ());
      _invokeCustomExceptionCallback (ex);
      return JPAExecutionResult.createFailure (ex);
    }
    finally
    {
      if (bTransactionRequired)
        if (aTransaction.isActive ())
        {
          // We got an exception -> rollback
          aTransaction.rollback ();
          LOGGER.warn ("Rolled back transaction for callable " + aCallable);
          s_aStatsCounterRollback.increment ();
        }

      if (isDefaultExecutionWarnTimeEnabled ())
        if (aSW.getMillis () > getDefaultExecutionWarnTime ())
          onExecutionTimeExceeded ("Callback: " +
                                   aSW.getMillis () +
                                   " ms; transaction: " +
                                   bTransactionRequired +
                                   "; Execution of callable in transaction took too long: " +
                                   aCallable.toString (),
                                   aSW.getMillis ());
    }
  }

  @Nonnull
  public final <T> JPAExecutionResult <T> doInTransaction (@Nonnull final Callable <T> aCallable)
  {
    // Create entity manager
    final EntityManager aEntityMgr = getEntityManager ();
    if (!isSyncEntityMgr ())
    {
      // No synchronization required
      return doInTransaction (aEntityMgr, isAllowNestedTransactions (), aCallable);
    }

    // Sync on the whole entity manager, to have a cross-manager
    // synchronization!
    synchronized (aEntityMgr)
    {
      return doInTransaction (aEntityMgr, isAllowNestedTransactions (), aCallable);
    }
  }

  /**
   * Perform a select, without a transaction
   *
   * @param aCallable
   *        The callable
   * @return The return of the callable or <code>null</code> upon success
   * @param <T>
   *        The return type of the callable
   */
  @Nonnull
  public static final <T> JPAExecutionResult <T> doSelectStatic (@Nonnull final Callable <T> aCallable)
  {
    ValueEnforcer.notNull (aCallable, "Callable");

    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      // Call callback
      final T ret = aCallable.call ();
      s_aStatsCounterSuccess.increment ();
      s_aStatsTimerExecutionSuccess.addTime (aSW.stopAndGetMillis ());
      return JPAExecutionResult.createSuccess (ret);
    }
    catch (final Exception ex)
    {
      s_aStatsCounterError.increment ();
      s_aStatsTimerExecutionError.addTime (aSW.stopAndGetMillis ());
      _invokeCustomExceptionCallback (ex);
      return JPAExecutionResult.<T> createFailure (ex);
    }
    finally
    {
      if (isDefaultExecutionWarnTimeEnabled ())
        if (aSW.getMillis () > getDefaultExecutionWarnTime ())
          onExecutionTimeExceeded ("Execution of select took too long: " + aCallable.toString (), aSW.getMillis ());
    }
  }

  /**
   * Run a read-only query. By default no transaction is used, and the entity
   * manager is synchronized.
   *
   * @param aCallable
   *        The callable to execute.
   * @return A non-<code>null</code> result of the select.
   * @param <T>
   *        Return type of the callable
   */
  @Nonnull
  public final <T> JPAExecutionResult <T> doSelect (@Nonnull final Callable <T> aCallable)
  {
    if (isUseTransactionsForSelect ())
    {
      // Use transactions for select statement!
      return doInTransaction (aCallable);
    }

    // Ensure that only one transaction is active for all users!
    final EntityManager aEntityMgr = getEntityManager ();
    if (!isSyncEntityMgr ())
    {
      // No synchronization required
      return doSelectStatic (aCallable);
    }

    // Sync on the whole entity manager, to have a cross-manager
    // synchronization!
    synchronized (aEntityMgr)
    {
      return doSelectStatic (aCallable);
    }
  }

  /**
   * Helper method to handle the execution of "SELECT COUNT(...) ..." SQL
   * statements. To be invoked inside a {@link #doSelect(Callable)} or
   * {@link #doSelectStatic(Callable)} method.
   *
   * @param aQuery
   *        The SELECT COUNT query
   * @return a non-negative row count
   */
  @Nonnull
  public static final Number getSelectCountResultObj (@Nonnull final Query aQuery)
  {
    final Number ret = (Number) aQuery.getSingleResult ();
    return ret != null ? ret : Integer.valueOf (0);
  }

  /**
   * Helper method to handle the execution of "SELECT COUNT(...) ..." SQL
   * statements. To be invoked inside a {@link #doSelect(Callable)} or
   * {@link #doSelectStatic(Callable)} method.
   *
   * @param aQuery
   *        The SELECT COUNT query
   * @return a non-negative row count
   */
  @Nonnegative
  public static final long getSelectCountResult (@Nonnull final Query aQuery)
  {
    return getSelectCountResultObj (aQuery).longValue ();
  }
}
