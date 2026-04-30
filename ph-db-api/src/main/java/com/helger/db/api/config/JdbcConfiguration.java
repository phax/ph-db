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
package com.helger.db.api.config;

import java.time.Duration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.CheckForSigned;
import com.helger.base.CGlobal;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.tostring.ToStringGenerator;

/**
 * Default implementation of {@link IJdbcConfiguration}.
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public class JdbcConfiguration implements IJdbcConfiguration
{
  public static final boolean DEFAULT_EXECUTION_TIME_WARNING_ENABLED = true;
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final long DEFAULT_EXECUTION_DURATION_WARN_MS = CGlobal.MILLISECONDS_PER_SECOND;
  public static final Duration DEFAULT_EXECUTION_TIME_WARNING_DURATION = Duration.ofMillis (DEFAULT_EXECUTION_DURATION_WARN_MS);

  public static final boolean DEFAULT_DEBUG_CONNECTIONS = false;
  public static final boolean DEFAULT_DEBUG_TRANSACTIONS = false;
  public static final boolean DEFAULT_DEBUG_SQL_STATEMENTS = false;

  public static final int DEFAULT_POOLING_MAX_CONNECTIONS = 8;
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final long DEFAULT_POOLING_MAX_WAIT_MILLIS = 10_000L;
  public static final Duration DEFAULT_POOLING_MAX_WAIT_DURATION = Duration.ofMillis (DEFAULT_POOLING_MAX_WAIT_MILLIS);
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final long DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_MILLIS = 300_000L;
  public static final Duration DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_DURATION = Duration.ofMillis (DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_MILLIS);
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final long DEFAULT_POOLING_MIN_EVICTABLE_IDLE_MILLIS = 1_800_000L;
  public static final Duration DEFAULT_POOLING_MIN_EVICTABLE_IDLE_DURATION = Duration.ofMillis (DEFAULT_POOLING_MIN_EVICTABLE_IDLE_MILLIS);
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final long DEFAULT_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS = 300_000L;
  public static final Duration DEFAULT_POOLING_REMOVE_ABANDONED_DURATION = Duration.ofMillis (DEFAULT_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS);
  public static final boolean DEFAULT_JDBC_POOLING_TEST_ON_BORROW = false;

  private final String m_sDatabaseType;
  private final String m_sJdbcDriver;
  private final String m_sJdbcUrl;
  private final String m_sJdbcUser;
  private final String m_sJdbcPassword;
  private final String m_sJdbcSchema;

  private final boolean m_bExecutionTimeWarningEnabled;
  private final Duration m_aExecutionTimeWarning;

  private final boolean m_bDebugConnections;
  private final boolean m_bDebugTransactions;
  private final boolean m_bDebugSQL;

  private final int m_nPoolingMaxConnections;
  private final Duration m_aPoolingMaxWait;
  private final Duration m_aPoolingBetweenEvictionRuns;
  private final Duration m_aPoolingMinEvictableIdle;
  private final Duration m_aPoolingRemoveAbandonedTimeout;
  private final boolean m_bPoolingTestOnBorrow;

  /*
   * Constructor.
   * @since 8.3.0
   */
  public JdbcConfiguration (@Nullable final String sDatabaseType,
                            @Nullable final String sJdbcDriver,
                            @Nullable final String sJdbcUrl,
                            @Nullable final String sJdbcUser,
                            @Nullable final String sJdbcPassword,
                            @Nullable final String sJdbcSchema,
                            final boolean bExecutionTimeWarningEnabled,
                            @NonNull final Duration aExecutionTimeWarning,
                            final boolean bDebugConnections,
                            final boolean bDebugTransactions,
                            final boolean bDebugSQL,
                            final int nPoolingMaxConnections,
                            @NonNull final Duration aPoolingMaxWait,
                            @NonNull final Duration aPoolingBetweenEvictionRuns,
                            @NonNull final Duration aPoolingMinEvictableIdle,
                            @NonNull final Duration aPoolingRemoveAbandonedTimeout,
                            final boolean bPoolingTestOnBorrow)
  {
    ValueEnforcer.notNull (aExecutionTimeWarning, "ExecutionTimeWarning");
    ValueEnforcer.notNull (aPoolingMaxWait, "PoolingMaxWait");
    ValueEnforcer.notNull (aPoolingBetweenEvictionRuns, "PoolingBetweenEvictionRuns");
    ValueEnforcer.notNull (aPoolingMinEvictableIdle, "PoolingMinEvictableIdle");
    ValueEnforcer.notNull (aPoolingRemoveAbandonedTimeout, "PoolingRemoveAbandonedTimeout");

    m_sDatabaseType = sDatabaseType;
    m_sJdbcDriver = sJdbcDriver;
    m_sJdbcUrl = sJdbcUrl;
    m_sJdbcUser = sJdbcUser;
    m_sJdbcPassword = sJdbcPassword;
    m_sJdbcSchema = sJdbcSchema;

    m_bExecutionTimeWarningEnabled = bExecutionTimeWarningEnabled;
    m_aExecutionTimeWarning = aExecutionTimeWarning;

    m_bDebugConnections = bDebugConnections;
    m_bDebugTransactions = bDebugTransactions;
    m_bDebugSQL = bDebugSQL;

    m_nPoolingMaxConnections = nPoolingMaxConnections;
    m_aPoolingMaxWait = aPoolingMaxWait;
    m_aPoolingBetweenEvictionRuns = aPoolingBetweenEvictionRuns;
    m_aPoolingMinEvictableIdle = aPoolingMinEvictableIdle;
    m_aPoolingRemoveAbandonedTimeout = aPoolingRemoveAbandonedTimeout;
    m_bPoolingTestOnBorrow = bPoolingTestOnBorrow;
  }

  /*
   * Constructor taking durations as raw milliseconds.
   * @deprecated Since 8.3.0; use the {@link Duration}-based constructor instead.
   */
  @Deprecated (forRemoval = true, since = "8.3.0")
  public JdbcConfiguration (@Nullable final String sDatabaseType,
                            @Nullable final String sJdbcDriver,
                            @Nullable final String sJdbcUrl,
                            @Nullable final String sJdbcUser,
                            @Nullable final String sJdbcPassword,
                            @Nullable final String sJdbcSchema,
                            final boolean bExecutionTimeWarningEnabled,
                            final long nExecutionTimeWarningMilliseconds,
                            final boolean bDebugConnections,
                            final boolean bDebugTransactions,
                            final boolean bDebugSQL,
                            final int nPoolingMaxConnections,
                            final long nPoolingMaxWaitMillis,
                            final long nPoolingBetweenEvictionRunsMillis,
                            final long nPoolingMinEvictableIdleMillis,
                            final long nPoolingRemoveAbandonedTimeoutMillis,
                            final boolean bPoolingTestOnBorrow)
  {
    this (sDatabaseType,
          sJdbcDriver,
          sJdbcUrl,
          sJdbcUser,
          sJdbcPassword,
          sJdbcSchema,
          bExecutionTimeWarningEnabled,
          Duration.ofMillis (nExecutionTimeWarningMilliseconds),
          bDebugConnections,
          bDebugTransactions,
          bDebugSQL,
          nPoolingMaxConnections,
          Duration.ofMillis (nPoolingMaxWaitMillis),
          Duration.ofMillis (nPoolingBetweenEvictionRunsMillis),
          Duration.ofMillis (nPoolingMinEvictableIdleMillis),
          Duration.ofMillis (nPoolingRemoveAbandonedTimeoutMillis),
          bPoolingTestOnBorrow);
  }

  @Nullable
  public String getJdbcDatabaseType ()
  {
    return m_sDatabaseType;
  }

  @Nullable
  public String getJdbcDriver ()
  {
    return m_sJdbcDriver;
  }

  @Nullable
  public String getJdbcUrl ()
  {
    return m_sJdbcUrl;
  }

  @Nullable
  public String getJdbcUser ()
  {
    return m_sJdbcUser;
  }

  @Nullable
  public String getJdbcPassword ()
  {
    return m_sJdbcPassword;
  }

  @Nullable
  public String getJdbcSchema ()
  {
    return m_sJdbcSchema;
  }

  public boolean isJdbcExecutionTimeWarningEnabled ()
  {
    return m_bExecutionTimeWarningEnabled;
  }

  @Override
  @NonNull
  public Duration getJdbcExecutionTimeWarning ()
  {
    return m_aExecutionTimeWarning;
  }

  @CheckForSigned
  @Deprecated (forRemoval = false, since = "8.3.0")
  public long getJdbcExecutionTimeWarningMilliseconds ()
  {
    return m_aExecutionTimeWarning.toMillis ();
  }

  public boolean isJdbcDebugConnections ()
  {
    return m_bDebugConnections;
  }

  public boolean isJdbcDebugTransactions ()
  {
    return m_bDebugTransactions;
  }

  public boolean isJdbcDebugSQL ()
  {
    return m_bDebugSQL;
  }

  @CheckForSigned
  public int getJdbcPoolingMaxConnections ()
  {
    return m_nPoolingMaxConnections;
  }

  @Override
  @NonNull
  public Duration getJdbcPoolingMaxWait ()
  {
    return m_aPoolingMaxWait;
  }

  @CheckForSigned
  @Deprecated (forRemoval = false, since = "8.3.0")
  public long getJdbcPoolingMaxWaitMillis ()
  {
    return m_aPoolingMaxWait.toMillis ();
  }

  @Override
  @NonNull
  public Duration getJdbcPoolingBetweenEvictionRuns ()
  {
    return m_aPoolingBetweenEvictionRuns;
  }

  @CheckForSigned
  @Deprecated (forRemoval = false, since = "8.3.0")
  public long getJdbcPoolingBetweenEvictionRunsMillis ()
  {
    return m_aPoolingBetweenEvictionRuns.toMillis ();
  }

  @Override
  @NonNull
  public Duration getJdbcPoolingMinEvictableIdle ()
  {
    return m_aPoolingMinEvictableIdle;
  }

  @CheckForSigned
  @Deprecated (forRemoval = false, since = "8.3.0")
  public long getJdbcPoolingMinEvictableIdleMillis ()
  {
    return m_aPoolingMinEvictableIdle.toMillis ();
  }

  @Override
  @NonNull
  public Duration getJdbcPoolingRemoveAbandonedTimeout ()
  {
    return m_aPoolingRemoveAbandonedTimeout;
  }

  @CheckForSigned
  @Deprecated (forRemoval = false, since = "8.3.0")
  public long getJdbcPoolingRemoveAbandonedTimeoutMillis ()
  {
    return m_aPoolingRemoveAbandonedTimeout.toMillis ();
  }

  public boolean isJdbcPoolingTestOnBorrow ()
  {
    return m_bPoolingTestOnBorrow;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("DatabaseType", m_sDatabaseType)
                                       .append ("JdbcDriver", m_sJdbcDriver)
                                       .append ("JdbcUrl", m_sJdbcUrl)
                                       .append ("JdbcUser", m_sJdbcUser)
                                       .appendPassword ("JdbcPassword")
                                       .append ("JdbcSchema", m_sJdbcSchema)
                                       .append ("ExecutionTimeWarningEnabled", m_bExecutionTimeWarningEnabled)
                                       .append ("ExecutionTimeWarning", m_aExecutionTimeWarning)
                                       .append ("DebugConnections", m_bDebugConnections)
                                       .append ("DebugTransactions", m_bDebugTransactions)
                                       .append ("DebugSQL", m_bDebugSQL)
                                       .append ("PoolingMaxConnections", m_nPoolingMaxConnections)
                                       .append ("PoolingMaxWait", m_aPoolingMaxWait)
                                       .append ("PoolingBetweenEvictionRuns", m_aPoolingBetweenEvictionRuns)
                                       .append ("PoolingMinEvictableIdle", m_aPoolingMinEvictableIdle)
                                       .append ("PoolingRemoveAbandonedTimeout", m_aPoolingRemoveAbandonedTimeout)
                                       .append ("PoolingTestOnBorrow", m_bPoolingTestOnBorrow)
                                       .getToString ();
  }
}
