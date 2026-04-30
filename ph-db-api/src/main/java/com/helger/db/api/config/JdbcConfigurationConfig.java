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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.CheckForSigned;
import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.tostring.ToStringGenerator;
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
  public static final String SUFFIX_DATABASE_TYPE = "database-type";
  public static final String SUFFIX_DRIVER = "driver";
  public static final String SUFFIX_URL = "url";
  public static final String SUFFIX_USER = "user";
  public static final String SUFFIX_PASSWORD = "password";
  public static final String SUFFIX_SCHEMA = "schema";

  public static final String SUFFIX_EXECUTION_TIME_WARNING_ENABLED = "execution-time-warning.enabled";
  public static final String SUFFIX_EXECUTION_TIME_WARNING = "execution-time-warning";
  /**
   * @deprecated Since 8.3.0; use {@link #SUFFIX_EXECUTION_TIME_WARNING} with the duration grammar
   *             (e.g. <code>5s</code>, <code>1m 30s</code>) instead.
   */
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final String SUFFIX_EXECUTION_TIME_WARNING_MS = "execution-time-warning.ms";

  public static final String SUFFIX_DEBUG_CONNECTIONS = "debug.connections";
  public static final String SUFFIX_DEBUG_TRANSACTIONS = "debug.transactions";
  public static final String SUFFIX_DEBUG_SQL = "debug.sql";

  public static final String SUFFIX_POOLING_MAX_CONNECTIONS = "pooling.max-connections";
  public static final String SUFFIX_POOLING_MAX_WAIT = "pooling.max-wait";
  /**
   * @deprecated Since 8.3.0; use {@link #SUFFIX_POOLING_MAX_WAIT} with the duration grammar (e.g.
   *             <code>5s</code>, <code>1m 30s</code>) instead.
   */
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final String SUFFIX_POOLING_MAX_WAIT_MILLIS = "pooling.max-wait.millis";

  public static final String SUFFIX_POOLING_BETWEEN_EVICTION_RUNS = "pooling.between-evictions-runs";
  /**
   * @deprecated Since 8.3.0; use {@link #SUFFIX_POOLING_BETWEEN_EVICTION_RUNS} with the duration
   *             grammar (e.g. <code>5s</code>, <code>1m 30s</code>) instead.
   */
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final String SUFFIX_POOLING_BETWEEN_EVICTION_RUNS_MILLIS = "pooling.between-evictions-runs.millis";

  public static final String SUFFIX_POOLING_MIN_EVICTABLE_IDLE = "pooling.min-evictable-idle";
  /**
   * @deprecated Since 8.3.0; use {@link #SUFFIX_POOLING_MIN_EVICTABLE_IDLE} with the duration
   *             grammar (e.g. <code>5s</code>, <code>1m 30s</code>) instead.
   */
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final String SUFFIX_POOLING_MIN_EVICTABLE_IDLE_MILLIS = "pooling.min-evictable-idle.millis";

  public static final String SUFFIX_POOLING_REMOVE_ABANDONED_TIMEOUT = "pooling.remove-abandoned-timeout";
  /**
   * @deprecated Since 8.3.0; use {@link #SUFFIX_POOLING_REMOVE_ABANDONED_TIMEOUT} with the duration
   *             grammar (e.g. <code>5s</code>, <code>1m 30s</code>) instead.
   */
  @Deprecated (forRemoval = true, since = "8.3.0")
  public static final String SUFFIX_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS = "pooling.remove-abandoned-timeout.millis";

  public static final String SUFFIX_POOLING_TEST_ON_BORROW = "pooling.test-on-borrow";

  private static final Logger LOGGER = LoggerFactory.getLogger (JdbcConfigurationConfig.class);

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

  /**
   * Resolve a duration-typed configuration value, preferring the duration-grammar key over the
   * legacy millisecond-typed key. The duration-grammar key accepts compound expressions like
   * <code>10s</code>, <code>5m</code>, <code>2d 5h 30m</code> via
   * {@link IConfig#getAsConfigDuration(String, java.util.function.Consumer)}. If the value is
   * missing, blank, or fails to parse, the legacy <code>.millis</code>/<code>.ms</code> key is read
   * as a <code>long</code>; if it too is absent the supplied default is returned.
   *
   * @param sDurationKey
   *        The duration-grammar configuration key (e.g. <code>"pooling.max-wait"</code>).
   * @param sLegacyMillisKey
   *        The legacy millisecond-typed configuration key (e.g.
   *        <code>"pooling.max-wait.millis"</code>).
   * @param aDefault
   *        The default value if neither key is configured.
   * @return The resolved duration. Never <code>null</code>.
   */
  @NonNull
  private Duration _getDurationOrLegacy (@NonNull final String sDurationKey,
                                         @NonNull final String sLegacyMillisKey,
                                         @NonNull final Duration aDefault)
  {
    final Duration aDuration = m_aConfig.getAsConfigDuration (sDurationKey,
                                                              sMsg -> LOGGER.warn ("Failed to parse configuration key '" +
                                                                                   sDurationKey +
                                                                                   "' as duration: " +
                                                                                   sMsg));
    if (aDuration != null)
      return aDuration;

    if (m_aConfig.containsConfiguredValue (sLegacyMillisKey))
    {
      LOGGER.warn ("Configuration key '" +
                   sLegacyMillisKey +
                   "' is deprecated; please use '" +
                   sDurationKey +
                   "' with the duration grammar (e.g. '5s', '1m 30s') instead.");
      return Duration.ofMillis (m_aConfig.getAsLong (sLegacyMillisKey, aDefault.toMillis ()));
    }

    return aDefault;
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
    return m_aConfig.getAsString (getConfigKeyDatabaseType ());
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
    return m_aConfig.getAsString (getConfigKeyJdbcDriver ());
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
    return m_aConfig.getAsString (getConfigKeyJdbcUrl ());
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
    return m_aConfig.getAsString (getConfigKeyJdbcUser ());
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
    return m_aConfig.getAsString (getConfigKeyJdbcPassword ());
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
    return m_aConfig.getAsString (getConfigKeyJdbcSchema ());
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcExecutionTimeWarningEnabled ()
  {
    return m_sConfigPrefix + SUFFIX_EXECUTION_TIME_WARNING_ENABLED;
  }

  public boolean isJdbcExecutionTimeWarningEnabled ()
  {
    return m_aConfig.getAsBoolean (getConfigKeyJdbcExecutionTimeWarningEnabled (),
                                   JdbcConfiguration.DEFAULT_EXECUTION_TIME_WARNING_ENABLED);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcExecutionTimeWarning ()
  {
    return m_sConfigPrefix + SUFFIX_EXECUTION_TIME_WARNING;
  }

  /**
   * @return The legacy millisecond-typed configuration key for the execution time warning
   *         threshold. May not be <code>null</code>.
   * @deprecated Since 8.3.0; use {@link #getConfigKeyJdbcExecutionTimeWarning()} with the duration
   *             grammar instead.
   */
  @NonNull
  @Nonempty
  @Deprecated (forRemoval = true, since = "8.3.0")
  public final String getConfigKeyJdbcExecutionTimeWarningMilliseconds ()
  {
    return m_sConfigPrefix + SUFFIX_EXECUTION_TIME_WARNING_MS;
  }

  @Override
  @NonNull
  public Duration getJdbcExecutionTimeWarning ()
  {
    return _getDurationOrLegacy (getConfigKeyJdbcExecutionTimeWarning (),
                                 getConfigKeyJdbcExecutionTimeWarningMilliseconds (),
                                 JdbcConfiguration.DEFAULT_EXECUTION_TIME_WARNING_DURATION);
  }

  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  public long getJdbcExecutionTimeWarningMilliseconds ()
  {
    return getJdbcExecutionTimeWarning ().toMillis ();
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcDebugConnections ()
  {
    return m_sConfigPrefix + SUFFIX_DEBUG_CONNECTIONS;
  }

  public boolean isJdbcDebugConnections ()
  {
    return m_aConfig.getAsBoolean (getConfigKeyJdbcDebugConnections (), JdbcConfiguration.DEFAULT_DEBUG_CONNECTIONS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcDebugTransactions ()
  {
    return m_sConfigPrefix + SUFFIX_DEBUG_TRANSACTIONS;
  }

  public boolean isJdbcDebugTransactions ()
  {
    return m_aConfig.getAsBoolean (getConfigKeyJdbcDebugTransactions (), JdbcConfiguration.DEFAULT_DEBUG_TRANSACTIONS);
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
    return m_aConfig.getAsInt (getConfigKeyJdbcPoolingMaxConnections (),
                               JdbcConfiguration.DEFAULT_POOLING_MAX_CONNECTIONS);
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingMaxWait ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_MAX_WAIT;
  }

  /**
   * @return The legacy millisecond-typed configuration key for the pooling max wait timeout. May
   *         not be <code>null</code>.
   * @deprecated Since 8.3.0; use {@link #getConfigKeyJdbcPoolingMaxWait()} with the duration
   *             grammar instead.
   */
  @NonNull
  @Nonempty
  @Deprecated (forRemoval = true, since = "8.3.0")
  public final String getConfigKeyJdbcPoolingMaxWaitMillis ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_MAX_WAIT_MILLIS;
  }

  @Override
  @NonNull
  public Duration getJdbcPoolingMaxWait ()
  {
    return _getDurationOrLegacy (getConfigKeyJdbcPoolingMaxWait (),
                                 getConfigKeyJdbcPoolingMaxWaitMillis (),
                                 JdbcConfiguration.DEFAULT_POOLING_MAX_WAIT_DURATION);
  }

  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  public long getJdbcPoolingMaxWaitMillis ()
  {
    return getJdbcPoolingMaxWait ().toMillis ();
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingBetweenEvictionRuns ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_BETWEEN_EVICTION_RUNS;
  }

  /**
   * @return The legacy millisecond-typed configuration key for the pooling between-eviction-runs
   *         interval. May not be <code>null</code>.
   * @deprecated Since 8.3.0; use {@link #getConfigKeyJdbcPoolingBetweenEvictionRuns()} with the
   *             duration grammar instead.
   */
  @NonNull
  @Nonempty
  @Deprecated (forRemoval = true, since = "8.3.0")
  public final String getConfigKeyJdbcPoolingBetweenEvictionRunsMillis ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_BETWEEN_EVICTION_RUNS_MILLIS;
  }

  @Override
  @NonNull
  public Duration getJdbcPoolingBetweenEvictionRuns ()
  {
    return _getDurationOrLegacy (getConfigKeyJdbcPoolingBetweenEvictionRuns (),
                                 getConfigKeyJdbcPoolingBetweenEvictionRunsMillis (),
                                 JdbcConfiguration.DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_DURATION);
  }

  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  public long getJdbcPoolingBetweenEvictionRunsMillis ()
  {
    return getJdbcPoolingBetweenEvictionRuns ().toMillis ();
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingMinEvictableIdle ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_MIN_EVICTABLE_IDLE;
  }

  /**
   * @return The legacy millisecond-typed configuration key for the pooling min-evictable-idle
   *         duration. May not be <code>null</code>.
   * @deprecated Since 8.3.0; use {@link #getConfigKeyJdbcPoolingMinEvictableIdle()} with the
   *             duration grammar instead.
   */
  @NonNull
  @Nonempty
  @Deprecated (forRemoval = true, since = "8.3.0")
  public final String getConfigKeyJdbcPoolingMinEvictableIdleMillis ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_MIN_EVICTABLE_IDLE_MILLIS;
  }

  @Override
  @NonNull
  public Duration getJdbcPoolingMinEvictableIdle ()
  {
    return _getDurationOrLegacy (getConfigKeyJdbcPoolingMinEvictableIdle (),
                                 getConfigKeyJdbcPoolingMinEvictableIdleMillis (),
                                 JdbcConfiguration.DEFAULT_POOLING_MIN_EVICTABLE_IDLE_DURATION);
  }

  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  public long getJdbcPoolingMinEvictableIdleMillis ()
  {
    return getJdbcPoolingMinEvictableIdle ().toMillis ();
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingRemoveAbandonedTimeout ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_REMOVE_ABANDONED_TIMEOUT;
  }

  /**
   * @return The legacy millisecond-typed configuration key for the pooling remove-abandoned
   *         timeout. May not be <code>null</code>.
   * @deprecated Since 8.3.0; use {@link #getConfigKeyJdbcPoolingRemoveAbandonedTimeout()} with the
   *             duration grammar instead.
   */
  @NonNull
  @Nonempty
  @Deprecated (forRemoval = true, since = "8.3.0")
  public final String getConfigKeyJdbcPoolingRemoveAbandonedTimeoutMillis ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS;
  }

  @Override
  @NonNull
  public Duration getJdbcPoolingRemoveAbandonedTimeout ()
  {
    return _getDurationOrLegacy (getConfigKeyJdbcPoolingRemoveAbandonedTimeout (),
                                 getConfigKeyJdbcPoolingRemoveAbandonedTimeoutMillis (),
                                 JdbcConfiguration.DEFAULT_POOLING_REMOVE_ABANDONED_DURATION);
  }

  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  public long getJdbcPoolingRemoveAbandonedTimeoutMillis ()
  {
    return getJdbcPoolingRemoveAbandonedTimeout ().toMillis ();
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPoolingTestOnBorrow ()
  {
    return m_sConfigPrefix + SUFFIX_POOLING_TEST_ON_BORROW;
  }

  public boolean isJdbcPoolingTestOnBorrow ()
  {
    return m_aConfig.getAsBoolean (getConfigKeyJdbcPoolingTestOnBorrow (),
                                   JdbcConfiguration.DEFAULT_JDBC_POOLING_TEST_ON_BORROW);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Config", m_aConfig)
                                       .append ("ConfigPrefix", m_sConfigPrefix)
                                       .getToString ();
  }
}
