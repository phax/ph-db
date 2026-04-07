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

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

import org.apache.commons.dbcp2.BasicDataSource;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.db.api.config.IJdbcConfiguration;
import com.helger.db.api.config.IJdbcDataSourceConfiguration;

/**
 * Data source provider from {@link IJdbcConfiguration}.
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public class DataSourceProviderFromJdbcConfiguration implements IHasDataSource, Closeable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DataSourceProviderFromJdbcConfiguration.class);

  private final BasicDataSource m_aDS;

  /**
   * Constructor creating a pooled data source from the provided JDBC configuration.
   *
   * @param aJdbcConfig
   *        The JDBC configuration to use. May not be <code>null</code>.
   */
  public DataSourceProviderFromJdbcConfiguration (@NonNull final IJdbcDataSourceConfiguration aJdbcConfig)
  {
    // build data source
    // This is usually only called once on startup and than the same
    // DataSource is reused during the entire lifetime
    m_aDS = new BasicDataSource ();
    m_aDS.setDriverClassName (aJdbcConfig.getJdbcDriver ());
    final String sUserName = aJdbcConfig.getJdbcUser ();
    if (sUserName != null)
      m_aDS.setUsername (sUserName);
    final String sPassword = aJdbcConfig.getJdbcPassword ();
    if (sPassword != null)
      m_aDS.setPassword (sPassword);
    m_aDS.setUrl (aJdbcConfig.getJdbcUrl ());

    // settings
    m_aDS.setDefaultAutoCommit (Boolean.FALSE);
    m_aDS.setPoolPreparedStatements (true);

    // Pooling config
    final int nMaxConnections = aJdbcConfig.getJdbcPoolingMaxConnections ();
    m_aDS.setMaxTotal (nMaxConnections);
    m_aDS.setMaxWait (Duration.ofMillis (aJdbcConfig.getJdbcPoolingMaxWaitMillis ()));
    m_aDS.setInitialSize (Math.min (4, nMaxConnections));
    m_aDS.setMinIdle (Math.min (4, nMaxConnections));
    m_aDS.setMaxIdle (nMaxConnections);

    final long nBetweenEvictionRunsMillis = aJdbcConfig.getJdbcPoolingBetweenEvictionRunsMillis ();
    if (nBetweenEvictionRunsMillis > 0)
    {
      m_aDS.setDurationBetweenEvictionRuns (Duration.ofMillis (nBetweenEvictionRunsMillis));
      m_aDS.setTestWhileIdle (true);
    }
    if (aJdbcConfig.getJdbcPoolingMinEvictableIdleMillis () > 0)
      m_aDS.setMinEvictableIdle (Duration.ofMillis (aJdbcConfig.getJdbcPoolingMinEvictableIdleMillis ()));
    if (aJdbcConfig.getJdbcPoolingRemoveAbandonedTimeoutMillis () > 0)
    {
      m_aDS.setRemoveAbandonedOnBorrow (true);
      m_aDS.setRemoveAbandonedTimeout (Duration.ofMillis (aJdbcConfig.getJdbcPoolingRemoveAbandonedTimeoutMillis ()));
    }

    LOGGER.info ("DataSource created with max " +
                 nMaxConnections +
                 " connections to '" +
                 aJdbcConfig.getJdbcUrl () +
                 "'");
  }

  @NonNull
  public BasicDataSource getDataSource ()
  {
    return m_aDS;
  }

  public void close () throws IOException
  {
    if (m_aDS != null && !m_aDS.isClosed ())
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Now closing DataSource");

      try
      {
        m_aDS.close ();
        LOGGER.info ("Successfully closed DataSource");
      }
      catch (final SQLException ex)
      {
        throw new IllegalStateException ("Failed to close DataSource " + m_aDS, ex);
      }
    }
  }
}
