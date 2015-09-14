/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.callback.IThrowingRunnable;
import com.helger.commons.callback.adapter.AdapterRunnableToCallable;
import com.helger.commons.callback.adapter.AdapterRunnableToThrowingRunnable;
import com.helger.commons.callback.adapter.AdapterThrowingRunnableToCallable;
import com.helger.commons.callback.exception.IExceptionCallback;
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
  /** The default execution time after which a warning is emitted */
  public static final int DEFAULT_EXECUTION_WARN_TIME_MS = 1000;

  private static final Logger s_aLogger = LoggerFactory.getLogger (JPAEnabledManager.class);
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

  protected static final ReadWriteLock s_aRWLock = new ReentrantReadWriteLock ();
  private static IExceptionCallback <Throwable> s_aExceptionCallback;
  private static final AtomicInteger s_aExecutionWarnTime = new AtomicInteger (DEFAULT_EXECUTION_WARN_TIME_MS);
  private static IExecutionTimeExceededCallback s_aExecutionTimeExceededHandler = new LoggingExecutionTimeExceededCallback (true);

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
   */
  public final void setSyncEntityMgr (final boolean bSyncEntityMgr)
  {
    m_aSyncEntityMgr.set (bSyncEntityMgr);
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
   */
  public final void setAllowNestedTransactions (final boolean bAllowNestedTransactions)
  {
    m_aAllowNestedTransactions.set (bAllowNestedTransactions);
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
   */
  public final void setUseTransactionsForSelect (final boolean bUseTransactionsForSelect)
  {
    m_aAllowNestedTransactions.set (bUseTransactionsForSelect);
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
   * Set a custom exception handler that is called in case performing some
   * operation fails.
   *
   * @param aExceptionCallback
   *        The exception handler to be set. May be <code>null</code> to
   *        indicate no custom exception handler.
   */
  public static final void setCustomExceptionCallback (@Nullable final IExceptionCallback <Throwable> aExceptionCallback)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      s_aExceptionCallback = aExceptionCallback;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * Get the custom exception handler.
   *
   * @return <code>null</code> if non is set
   */
  @Nullable
  public static final IExceptionCallback <Throwable> getCustomExceptionCallback ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_aExceptionCallback;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  /**
   * Invoke the custom exception handler (if present)
   *
   * @param t
   *        The exception that occurred.
   */
  private static void _invokeCustomExceptionCallback (@Nonnull final Throwable t)
  {
    final IExceptionCallback <Throwable> aExceptionCallback = getCustomExceptionCallback ();
    if (aExceptionCallback == null)
    {
      // By default: log something :)
      s_aLogger.error ("Failed to perform something in a JPAEnabledManager!", t);
    }
    else
      try
      {
        aExceptionCallback.onException (t);
      }
      catch (final Throwable t2)
      {
        s_aLogger.error ("Error in JPAEnabledManager custom exception handler " + aExceptionCallback, t2);
      }
  }

  /**
   * @return The milliseconds after which a warning is emitted, if an SQL
   *         statement takes longer to execute.
   */
  @Nonnegative
  public static final int getDefaultExecutionWarnTime ()
  {
    return s_aExecutionWarnTime.get ();
  }

  /**
   * Set the milli seconds duration on which a warning should be emitted, if a
   * single SQL execution too at least that long.
   *
   * @param nMillis
   *        The number of milli seconds. Must be &ge; 0.
   */
  public static final void setDefaultExecutionWarnTime (final int nMillis)
  {
    ValueEnforcer.isGE0 (nMillis, "Milliseconds");
    s_aExecutionWarnTime.set (nMillis);
  }

  public static final void setExecutionTimeExceededHandler (@Nullable final IExecutionTimeExceededCallback aExecutionTimeExceededHandler)
  {
    s_aRWLock.writeLock ().lock ();
    try
    {
      s_aExecutionTimeExceededHandler = aExecutionTimeExceededHandler;
    }
    finally
    {
      s_aRWLock.writeLock ().unlock ();
    }
  }

  /**
   * Get the custom exception handler.
   *
   * @return <code>null</code> if non is set
   */
  @Nullable
  public static final IExecutionTimeExceededCallback getExecutionTimeExceededHandler ()
  {
    s_aRWLock.readLock ().lock ();
    try
    {
      return s_aExecutionTimeExceededHandler;
    }
    finally
    {
      s_aRWLock.readLock ().unlock ();
    }
  }

  public static final void onExecutionTimeExceeded (@Nonnull final String sMsg,
                                                    @Nonnegative final long nExecutionMillis)
  {
    final IExecutionTimeExceededCallback aHdl = getExecutionTimeExceededHandler ();
    if (aHdl != null)
      try
      {
        // Invoke the handler
        aHdl.onExecutionTimeExceeded (sMsg, nExecutionMillis);
      }
      catch (final Throwable t)
      {
        s_aLogger.error ("Failed to invoke exceution time exceeded handler " + aHdl, t);
      }
  }

  @Nonnull
  public static final JPAExecutionResult <?> doInTransaction (@Nonnull @WillNotClose final EntityManager aEntityMgr,
                                                              final boolean bAllowNestedTransactions,
                                                              @Nonnull final Runnable aRunnable)
  {
    return doInTransaction (aEntityMgr, bAllowNestedTransactions, AdapterRunnableToCallable.createAdapter (aRunnable));
  }

  @Nonnull
  public static final JPAExecutionResult <?> doInTransaction (@Nonnull @WillNotClose final EntityManager aEntityMgr,
                                                              final boolean bAllowNestedTransactions,
                                                              @Nonnull final IThrowingRunnable <Exception> aRunnable)
  {
    return doInTransaction (aEntityMgr,
                            bAllowNestedTransactions,
                            AdapterThrowingRunnableToCallable.createAdapter (aRunnable));
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
  public final JPAExecutionResult <?> doInTransaction (@Nonnull final Runnable aRunnable)
  {
    return doInTransaction (new AdapterRunnableToThrowingRunnable <Exception> (aRunnable));
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
    catch (final Throwable t)
    {
      s_aStatsCounterError.increment ();
      s_aStatsTimerExecutionError.addTime (aSW.stopAndGetMillis ());
      _invokeCustomExceptionCallback (t);
      return JPAExecutionResult.<T> createFailure (t);
    }
    finally
    {
      if (bTransactionRequired)
        if (aTransaction.isActive ())
        {
          // We got an exception -> rollback
          aTransaction.rollback ();
          s_aLogger.warn ("Rolled back transaction for callable " + aCallable);
          s_aStatsCounterRollback.increment ();
        }

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
    catch (final Throwable t)
    {
      s_aStatsCounterError.increment ();
      s_aStatsTimerExecutionError.addTime (aSW.stopAndGetMillis ());
      _invokeCustomExceptionCallback (t);
      return JPAExecutionResult.<T> createFailure (t);
    }
    finally
    {
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
