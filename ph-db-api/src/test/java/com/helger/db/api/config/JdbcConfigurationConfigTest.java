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

import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.junit.Test;

import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsMap;
import com.helger.config.Config;
import com.helger.config.source.appl.ConfigurationSourceFunction;

/**
 * Test class for class {@link JdbcConfigurationConfig}.
 *
 * @author Philip Helger
 */
public final class JdbcConfigurationConfigTest
{
  private static JdbcConfigurationConfig _build (final ICommonsMap <String, String> aMap)
  {
    final Config aConfig = new Config (new ConfigurationSourceFunction (aMap::get));
    return new JdbcConfigurationConfig (aConfig, "jdbc.");
  }

  @SuppressWarnings ("removal")
  @Test
  public void testDefaultsWhenNothingConfigured ()
  {
    final JdbcConfigurationConfig aCfg = _build (new CommonsHashMap <> ());
    assertEquals (JdbcConfiguration.DEFAULT_EXECUTION_DURATION_WARN_MS,
                  aCfg.getJdbcExecutionTimeWarningMilliseconds ());
    assertEquals (JdbcConfiguration.DEFAULT_POOLING_MAX_WAIT_MILLIS, aCfg.getJdbcPoolingMaxWaitMillis ());
    assertEquals (JdbcConfiguration.DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_MILLIS,
                  aCfg.getJdbcPoolingBetweenEvictionRunsMillis ());
    assertEquals (JdbcConfiguration.DEFAULT_POOLING_MIN_EVICTABLE_IDLE_MILLIS,
                  aCfg.getJdbcPoolingMinEvictableIdleMillis ());
    assertEquals (JdbcConfiguration.DEFAULT_POOLING_REMOVE_ABANDONED_TIMEOUT_MILLIS,
                  aCfg.getJdbcPoolingRemoveAbandonedTimeoutMillis ());
  }

  @SuppressWarnings ("removal")
  @Test
  public void testLegacyMillisKeysStillWork ()
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("jdbc.execution-time-warning.ms", "750");
    aMap.put ("jdbc.pooling.max-wait.millis", "5000");
    aMap.put ("jdbc.pooling.between-evictions-runs.millis", "60000");
    aMap.put ("jdbc.pooling.min-evictable-idle.millis", "120000");
    aMap.put ("jdbc.pooling.remove-abandoned-timeout.millis", "30000");
    final JdbcConfigurationConfig aCfg = _build (aMap);
    assertEquals (750L, aCfg.getJdbcExecutionTimeWarningMilliseconds ());
    assertEquals (5_000L, aCfg.getJdbcPoolingMaxWaitMillis ());
    assertEquals (60_000L, aCfg.getJdbcPoolingBetweenEvictionRunsMillis ());
    assertEquals (120_000L, aCfg.getJdbcPoolingMinEvictableIdleMillis ());
    assertEquals (30_000L, aCfg.getJdbcPoolingRemoveAbandonedTimeoutMillis ());
  }

  @SuppressWarnings ("removal")
  @Test
  public void testNewDurationKeys ()
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("jdbc.execution-time-warning", "750ms");
    aMap.put ("jdbc.pooling.max-wait", "5s");
    aMap.put ("jdbc.pooling.between-evictions-runs", "1m");
    aMap.put ("jdbc.pooling.min-evictable-idle", "2m");
    aMap.put ("jdbc.pooling.remove-abandoned-timeout", "30s");
    final JdbcConfigurationConfig aCfg = _build (aMap);
    assertEquals (750L, aCfg.getJdbcExecutionTimeWarningMilliseconds ());
    assertEquals (5_000L, aCfg.getJdbcPoolingMaxWaitMillis ());
    assertEquals (60_000L, aCfg.getJdbcPoolingBetweenEvictionRunsMillis ());
    assertEquals (120_000L, aCfg.getJdbcPoolingMinEvictableIdleMillis ());
    assertEquals (30_000L, aCfg.getJdbcPoolingRemoveAbandonedTimeoutMillis ());
  }

  @SuppressWarnings ("removal")
  @Test
  public void testNewDurationKeyWinsOverLegacy ()
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("jdbc.pooling.max-wait", "30s");
    aMap.put ("jdbc.pooling.max-wait.millis", "5000");
    final JdbcConfigurationConfig aCfg = _build (aMap);
    assertEquals (30_000L, aCfg.getJdbcPoolingMaxWaitMillis ());
  }

  @SuppressWarnings ("removal")
  @Test
  public void testCompoundDurationExpression ()
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("jdbc.pooling.min-evictable-idle", "1h 30m");
    final JdbcConfigurationConfig aCfg = _build (aMap);
    assertEquals (90L * 60L * 1000L, aCfg.getJdbcPoolingMinEvictableIdleMillis ());
  }

  @SuppressWarnings ("removal")
  @Test
  public void testNewKeyParseFailureFallsBackToLegacy ()
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("jdbc.pooling.max-wait", "not-a-duration");
    aMap.put ("jdbc.pooling.max-wait.millis", "7000");
    final JdbcConfigurationConfig aCfg = _build (aMap);
    assertEquals (7_000L, aCfg.getJdbcPoolingMaxWaitMillis ());
  }

  @Test
  public void testDurationGettersWhenNothingConfigured ()
  {
    final JdbcConfigurationConfig aCfg = _build (new CommonsHashMap <> ());
    assertEquals (JdbcConfiguration.DEFAULT_EXECUTION_TIME_WARNING_DURATION, aCfg.getJdbcExecutionTimeWarning ());
    assertEquals (JdbcConfiguration.DEFAULT_POOLING_MAX_WAIT_DURATION, aCfg.getJdbcPoolingMaxWait ());
    assertEquals (JdbcConfiguration.DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_DURATION,
                  aCfg.getJdbcPoolingBetweenEvictionRuns ());
    assertEquals (JdbcConfiguration.DEFAULT_POOLING_MIN_EVICTABLE_IDLE_DURATION,
                  aCfg.getJdbcPoolingMinEvictableIdle ());
    assertEquals (JdbcConfiguration.DEFAULT_POOLING_REMOVE_ABANDONED_DURATION,
                  aCfg.getJdbcPoolingRemoveAbandonedTimeout ());
  }

  @Test
  public void testDurationGettersWithNewKeys ()
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("jdbc.execution-time-warning", "750ms");
    aMap.put ("jdbc.pooling.max-wait", "5s");
    aMap.put ("jdbc.pooling.between-evictions-runs", "1m");
    aMap.put ("jdbc.pooling.min-evictable-idle", "2m");
    aMap.put ("jdbc.pooling.remove-abandoned-timeout", "30s");
    final JdbcConfigurationConfig aCfg = _build (aMap);
    assertEquals (Duration.ofMillis (750), aCfg.getJdbcExecutionTimeWarning ());
    assertEquals (Duration.ofSeconds (5), aCfg.getJdbcPoolingMaxWait ());
    assertEquals (Duration.ofMinutes (1), aCfg.getJdbcPoolingBetweenEvictionRuns ());
    assertEquals (Duration.ofMinutes (2), aCfg.getJdbcPoolingMinEvictableIdle ());
    assertEquals (Duration.ofSeconds (30), aCfg.getJdbcPoolingRemoveAbandonedTimeout ());
  }

  @SuppressWarnings ("removal")
  @Test
  public void testJdbcConfigurationPojoDurationConstructor ()
  {
    final JdbcConfiguration aCfg = new JdbcConfiguration (null,
                                                          "org.h2.Driver",
                                                          "jdbc:h2:mem:test",
                                                          "sa",
                                                          "",
                                                          null,
                                                          true,
                                                          Duration.ofSeconds (2),
                                                          false,
                                                          false,
                                                          false,
                                                          16,
                                                          Duration.ofSeconds (15),
                                                          Duration.ofMinutes (10),
                                                          Duration.ofHours (1),
                                                          Duration.ofMinutes (5),
                                                          true);
    assertEquals (Duration.ofSeconds (2), aCfg.getJdbcExecutionTimeWarning ());
    assertEquals (Duration.ofSeconds (15), aCfg.getJdbcPoolingMaxWait ());
    assertEquals (Duration.ofMinutes (10), aCfg.getJdbcPoolingBetweenEvictionRuns ());
    assertEquals (Duration.ofHours (1), aCfg.getJdbcPoolingMinEvictableIdle ());
    assertEquals (Duration.ofMinutes (5), aCfg.getJdbcPoolingRemoveAbandonedTimeout ());
    // Backward-compatible millis getters return the same value
    assertEquals (2_000L, aCfg.getJdbcExecutionTimeWarningMilliseconds ());
    assertEquals (15_000L, aCfg.getJdbcPoolingMaxWaitMillis ());
    assertEquals (600_000L, aCfg.getJdbcPoolingBetweenEvictionRunsMillis ());
    assertEquals (3_600_000L, aCfg.getJdbcPoolingMinEvictableIdleMillis ());
    assertEquals (300_000L, aCfg.getJdbcPoolingRemoveAbandonedTimeoutMillis ());
  }

  @Test
  public void testJdbcConfigurationPojoLegacyMillisConstructor ()
  {
    @SuppressWarnings ("removal")
    final JdbcConfiguration aCfg = new JdbcConfiguration (null,
                                                          "org.h2.Driver",
                                                          "jdbc:h2:mem:test",
                                                          "sa",
                                                          "",
                                                          null,
                                                          true,
                                                          2_000L,
                                                          false,
                                                          false,
                                                          false,
                                                          16,
                                                          15_000L,
                                                          600_000L,
                                                          3_600_000L,
                                                          300_000L,
                                                          true);
    assertEquals (Duration.ofSeconds (2), aCfg.getJdbcExecutionTimeWarning ());
    assertEquals (Duration.ofSeconds (15), aCfg.getJdbcPoolingMaxWait ());
    assertEquals (Duration.ofMinutes (10), aCfg.getJdbcPoolingBetweenEvictionRuns ());
    assertEquals (Duration.ofHours (1), aCfg.getJdbcPoolingMinEvictableIdle ());
    assertEquals (Duration.ofMinutes (5), aCfg.getJdbcPoolingRemoveAbandonedTimeout ());
  }
}
