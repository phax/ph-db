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
package com.helger.db.jdbc;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

import com.helger.db.api.config.JdbcConfiguration;

/**
 * Test class for class {@link DataSourceProviderFromJdbcConfiguration}.
 *
 * @author Philip Helger
 */
public final class DataSourceProviderFromJdbcConfigurationTest
{
  private static void _assertPooling (final int nConfiguredMin,
                                      final int nConfiguredMax,
                                      final int nExpectedMin) throws Exception
  {
    final JdbcConfiguration aJdbcConfig = new JdbcConfiguration (null,
                                                                 "org.h2.Driver",
                                                                 "jdbc:h2:mem:dsprovider-test",
                                                                 "sa",
                                                                 "",
                                                                 null,
                                                                 JdbcConfiguration.DEFAULT_EXECUTION_TIME_WARNING_ENABLED,
                                                                 JdbcConfiguration.DEFAULT_EXECUTION_TIME_WARNING_DURATION,
                                                                 JdbcConfiguration.DEFAULT_DEBUG_CONNECTIONS,
                                                                 JdbcConfiguration.DEFAULT_DEBUG_TRANSACTIONS,
                                                                 JdbcConfiguration.DEFAULT_DEBUG_SQL_STATEMENTS,
                                                                 nConfiguredMin,
                                                                 nConfiguredMax,
                                                                 JdbcConfiguration.DEFAULT_POOLING_MAX_WAIT_DURATION,
                                                                 JdbcConfiguration.DEFAULT_POOLING_BETWEEN_EVICTION_RUNS_DURATION,
                                                                 JdbcConfiguration.DEFAULT_POOLING_MIN_EVICTABLE_IDLE_DURATION,
                                                                 JdbcConfiguration.DEFAULT_POOLING_REMOVE_ABANDONED_DURATION,
                                                                 JdbcConfiguration.DEFAULT_JDBC_POOLING_TEST_ON_BORROW);
    try (final DataSourceProviderFromJdbcConfiguration aProvider = new DataSourceProviderFromJdbcConfiguration (aJdbcConfig))
    {
      final BasicDataSource aDS = aProvider.getDataSource ();
      assertEquals (nConfiguredMax, aDS.getMaxTotal ());
      assertEquals (nConfiguredMax, aDS.getMaxIdle ());
      assertEquals (nExpectedMin, aDS.getInitialSize ());
      assertEquals (nExpectedMin, aDS.getMinIdle ());
    }
  }

  @Test
  public void testDefaultPooling () throws Exception
  {
    _assertPooling (JdbcConfiguration.DEFAULT_POOLING_MIN_CONNECTIONS,
                    JdbcConfiguration.DEFAULT_POOLING_MAX_CONNECTIONS,
                    JdbcConfiguration.DEFAULT_POOLING_MIN_CONNECTIONS);
  }

  @Test
  public void testMinConnectionsWithinMax () throws Exception
  {
    _assertPooling (10, 20, 10);
  }

  @Test
  public void testMinConnectionsCappedAtMax () throws Exception
  {
    _assertPooling (10, 6, 6);
  }

  @Test
  public void testMinConnectionsNotCappedForUnlimitedMax () throws Exception
  {
    _assertPooling (10, -1, 10);
  }

  @Test
  public void testMinConnectionsNeverBelowOne () throws Exception
  {
    _assertPooling (0, 8, 1);
    _assertPooling (-5, 8, 1);
    _assertPooling (-5, -1, 1);
    _assertPooling (5, 0, 1);
  }
}
