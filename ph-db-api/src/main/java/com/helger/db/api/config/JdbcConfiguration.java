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

import org.jspecify.annotations.Nullable;

import com.helger.annotation.CheckForSigned;
import com.helger.base.CGlobal;

/**
 * Default implementation of {@link IJdbcConfiguration}.
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public class JdbcConfiguration implements IJdbcConfiguration
{
  public static final boolean DEFAULT_EXECUTION_TIME_WARNING_ENABLED = true;
  public static final long DEFAULT_EXECUTION_DURATION_WARN_MS = CGlobal.MILLISECONDS_PER_SECOND;

  public static final boolean DEFAULT_DEBUG_CONNECTIONS = false;
  public static final boolean DEFAULT_DEBUG_TRANSACTIONS = false;
  public static final boolean DEFAULT_DEBUG_SQL_STATEMENTS = false;

  public static final int DEFAULT_POOLING_MAX_CONNECTIONS = 8;
  public static final long DEFAULT_POOLING_MAX_WAIT_MILLIS = 10_000L;
  public static final long DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_MILLIS = 300_000L;
  public static final long DEFAULT_POOLING_MIN_EVICTABLE_IDLE_MILLIS = 1_800_000L;
  public static final long DEFAULT_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS = 300_000L;
  public static final boolean DEFAULT_JDBC_POOLING_TEST_ON_BORROW = false;

  private final String m_sDatabaseType;
  private final String m_sJdbcDriver;
  private final String m_sJdbcUrl;
  private final String m_sJdbcUser;
  private final String m_sJdbcPassword;
  private final String m_sJdbcSchema;

  private final boolean m_bExecutionTimeWarningEnabled;
  private final long m_nExecutionTimeWarningMilliseconds;

  private final boolean m_bDebugConnections;
  private final boolean m_bDebugTransactions;
  private final boolean m_bDebugSQL;

  private final int m_nPoolingMaxConnections;
  private final long m_nPoolingMaxWaitMillis;
  private final long m_nPoolingBetweenEvictionRunsMillis;
  private final long m_nPoolingMinEvictableIdleMillis;
  private final long m_nPoolingRemoveAbandonedTimeoutMillis;
  private final boolean m_bPoolingTestOnBorrow;

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
    m_sDatabaseType = sDatabaseType;
    m_sJdbcDriver = sJdbcDriver;
    m_sJdbcUrl = sJdbcUrl;
    m_sJdbcUser = sJdbcUser;
    m_sJdbcPassword = sJdbcPassword;
    m_sJdbcSchema = sJdbcSchema;

    m_bExecutionTimeWarningEnabled = bExecutionTimeWarningEnabled;
    m_nExecutionTimeWarningMilliseconds = nExecutionTimeWarningMilliseconds;

    m_bDebugConnections = bDebugConnections;
    m_bDebugTransactions = bDebugTransactions;
    m_bDebugSQL = bDebugSQL;

    m_nPoolingMaxConnections = nPoolingMaxConnections;
    m_nPoolingMaxWaitMillis = nPoolingMaxWaitMillis;
    m_nPoolingBetweenEvictionRunsMillis = nPoolingBetweenEvictionRunsMillis;
    m_nPoolingMinEvictableIdleMillis = nPoolingMinEvictableIdleMillis;
    m_nPoolingRemoveAbandonedTimeoutMillis = nPoolingRemoveAbandonedTimeoutMillis;
    m_bPoolingTestOnBorrow = bPoolingTestOnBorrow;
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

  @CheckForSigned
  public long getJdbcExecutionTimeWarningMilliseconds ()
  {
    return m_nExecutionTimeWarningMilliseconds;
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

  @CheckForSigned
  public long getJdbcPoolingMaxWaitMillis ()
  {
    return m_nPoolingMaxWaitMillis;
  }

  @CheckForSigned
  public long getJdbcPoolingBetweenEvictionRunsMillis ()
  {
    return m_nPoolingBetweenEvictionRunsMillis;
  }

  @CheckForSigned
  public long getJdbcPoolingMinEvictableIdleMillis ()
  {
    return m_nPoolingMinEvictableIdleMillis;
  }

  @CheckForSigned
  public long getJdbcPoolingRemoveAbandonedTimeoutMillis ()
  {
    return m_nPoolingRemoveAbandonedTimeoutMillis;
  }

  public boolean isJdbcPoolingTestOnBorrow ()
  {
    return m_bPoolingTestOnBorrow;
  }
}
