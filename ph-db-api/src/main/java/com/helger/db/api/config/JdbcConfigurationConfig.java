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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.CheckForSigned;
import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.config.IConfig;

/**
 * JDBC configuration accessor from configuration. It resolves the configuration properties on
 * demand.
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public class JdbcConfigurationConfig implements IJdbcConfiguration
{
  public final String SUFFIX_DATABASE_TYPE = "database-type";
  public final String SUFFIX_DRIVER = "driver";
  public final String SUFFIX_URL = "url";
  public final String SUFFIX_USER = "user";
  public final String SUFFIX_PASSWORD = "password";
  public final String SUFFIX_SCHEMA = "schema";

  public final String SUFFIX_EXECUTION_TIME_WARNING_ENABLED = "execution-time-warning.enabled";
  public final String SUFFIX_EXECUTION_TIME_WARNING_MS = "execution-time-warning.ms";

  public final String SUFFIX_DEBUG_CONNECTIONS = "debug.connections";
  public final String SUFFIX_DEBUG_TRANSACTIONS = "debug.transactions";
  public final String SUFFIX_DEBUG_SQL = "debug.sql";

  public final String SUFFIX_POOLING_MAX_CONNECTIONS = "pooling.max-connections";
  public final String SUFFIX_POOLING_MAX_WAIT_MILLIS = "pooling.max-wait.millis";
  public final String SUFFIX_POOLING_BETWEEN_EVICTION_RUNS_MILLIS = "pooling.between-evictions-runs.millis";
  public final String SUFFIX_POOLING_MIN_EVICTABLE_IDLE_MILLIS = "pooling.min-evictable-idle.millis";
  public final String SUFFIX_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS = "pooling.remove-abandoned-timeout.millis";

  private final IConfig m_aConfig;
  private final String m_sConfigPrefix;

  /**
   * Constructor
   *
   * @param aConfig
   *        The config object to use. May not be <code>null</code>.
   * @param sConfigPrefix
   *        The configuration common prefix to use. May not be <code>null</code> but maybe empty. If
   *        it is non-empty, it must end with a dot (".").
   */
  public JdbcConfigurationConfig (@NonNull final IConfig aConfig, @NonNull final String sConfigPrefix)
  {
    ValueEnforcer.notNull (aConfig, "Config");
    ValueEnforcer.notNull (sConfigPrefix, "ConfigPrefix");
    if (sConfigPrefix.length () > 0)
      ValueEnforcer.isTrue (sConfigPrefix.endsWith ("."),
                            () -> "ConfigPrefix '" + sConfigPrefix + "' should end with a dot");

    m_aConfig = aConfig;
    m_sConfigPrefix = sConfigPrefix;
  }

  @NonNull
  protected final IConfig getConfig ()
  {
    return m_aConfig;
  }

  /**
   * @return The configuration prefix provided in the constructor. Never <code>null</code>.
   */
  @NonNull
  public final String getConfigPrefix ()
  {
    return m_sConfigPrefix;
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyDatabaseType ()
  {
    return m_sConfigPrefix + SUFFIX_DATABASE_TYPE;
  }

  @Nullable
  public String getJdbcDatabaseType ()
  {
    return m_aConfig.getAsString (m_sConfigPrefix + SUFFIX_DATABASE_TYPE);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcDriver ()
  {
    return m_sConfigPrefix + SUFFIX_DRIVER;
  }

  @Nullable
  public String getJdbcDriver ()
  {
    return m_aConfig.getAsString (m_sConfigPrefix + SUFFIX_DRIVER);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcUrl ()
  {
    return m_sConfigPrefix + SUFFIX_URL;
  }

  @Nullable
  public String getJdbcUrl ()
  {
    return m_aConfig.getAsString (m_sConfigPrefix + SUFFIX_URL);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcUser ()
  {
    return m_sConfigPrefix + SUFFIX_USER;
  }

  @Nullable
  public String getJdbcUser ()
  {
    return m_aConfig.getAsString (m_sConfigPrefix + SUFFIX_USER);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPassword ()
  {
    return m_sConfigPrefix + SUFFIX_PASSWORD;
  }

  @Nullable
  public String getJdbcPassword ()
  {
    return m_aConfig.getAsString (m_sConfigPrefix + SUFFIX_PASSWORD);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcSchema ()
  {
    return m_sConfigPrefix + SUFFIX_SCHEMA;
  }

  @Nullable
  public String getJdbcSchema ()
  {
    return m_aConfig.getAsString (m_sConfigPrefix + SUFFIX_SCHEMA);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcExecutionTimeWarningEnabled ()
  {
    return m_sConfigPrefix + SUFFIX_EXECUTION_TIME_WARNING_ENABLED;
  }

  public boolean isJdbcExecutionTimeWarningEnabled ()
  {
    return m_aConfig.getAsBoolean (m_sConfigPrefix + SUFFIX_EXECUTION_TIME_WARNING_ENABLED,
                                   JdbcConfiguration.DEFAULT_EXECUTION_TIME_WARNING_ENABLED);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcExecutionTimeWarningMilliseconds ()
  {
    return m_sConfigPrefix + SUFFIX_EXECUTION_TIME_WARNING_MS;
  }

  @CheckForSigned
  public long getJdbcExecutionTimeWarningMilliseconds ()
  {
    return m_aConfig.getAsLong (m_sConfigPrefix + SUFFIX_EXECUTION_TIME_WARNING_MS,
                                JdbcConfiguration.DEFAULT_EXECUTION_DURATION_WARN_MS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcDebugConnections ()
  {
    return m_sConfigPrefix + SUFFIX_DEBUG_CONNECTIONS;
  }

  public boolean isJdbcDebugConnections ()
  {
    return m_aConfig.getAsBoolean (m_sConfigPrefix + SUFFIX_DEBUG_CONNECTIONS,
                                   JdbcConfiguration.DEFAULT_DEBUG_CONNECTIONS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcDebugTransactions ()
  {
    return m_sConfigPrefix + SUFFIX_DEBUG_TRANSACTIONS;
  }

  public boolean isJdbcDebugTransactions ()
  {
    return m_aConfig.getAsBoolean (m_sConfigPrefix + SUFFIX_DEBUG_TRANSACTIONS,
                                   JdbcConfiguration.DEFAULT_DEBUG_TRANSACTIONS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcDebugSQL ()
  {
    return m_sConfigPrefix + SUFFIX_DEBUG_SQL;
  }

  @CheckForSigned
  public boolean isJdbcDebugSQL ()
  {
    return m_aConfig.getAsBoolean (getConfigKeyJdbcDebugSQL (), JdbcConfiguration.DEFAULT_DEBUG_SQL_STATEMENTS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingMaxConnections ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_MAX_CONNECTIONS;
  }

  @CheckForSigned
  public int getJdbcPoolingMaxConnections ()
  {
    return m_aConfig.getAsInt (m_sConfigPrefix + SUFFIX_POOLING_MAX_CONNECTIONS,
                               JdbcConfiguration.DEFAULT_POOLING_MAX_CONNECTIONS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingMaxWaitMillis ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_MAX_WAIT_MILLIS;
  }

  @CheckForSigned
  public long getJdbcPoolingMaxWaitMillis ()
  {
    return m_aConfig.getAsLong (m_sConfigPrefix + SUFFIX_POOLING_MAX_WAIT_MILLIS,
                                JdbcConfiguration.DEFAULT_POOLING_MAX_WAIT_MILLIS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingBetweenEvictionRunsMillis ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_BETWEEN_EVICTION_RUNS_MILLIS;
  }

  @CheckForSigned
  public long getJdbcPoolingBetweenEvictionRunsMillis ()
  {
    return m_aConfig.getAsLong (m_sConfigPrefix + SUFFIX_POOLING_BETWEEN_EVICTION_RUNS_MILLIS,
                                JdbcConfiguration.DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_MILLIS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingMinEvictableIdleMillis ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_MIN_EVICTABLE_IDLE_MILLIS;
  }

  @CheckForSigned
  public long getJdbcPoolingMinEvictableIdleMillis ()
  {
    return m_aConfig.getAsLong (m_sConfigPrefix + SUFFIX_POOLING_MIN_EVICTABLE_IDLE_MILLIS,
                                JdbcConfiguration.DEFAULT_POOLING_MIN_EVICTABLE_IDLE_MILLIS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingRemoveAbandonedTimeoutMillis ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS;
  }

  @CheckForSigned
  public long getJdbcPoolingRemoveAbandonedTimeoutMillis ()
  {
    return m_aConfig.getAsLong (m_sConfigPrefix + SUFFIX_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS,
                                JdbcConfiguration.DEFAULT_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS);
  }
}
