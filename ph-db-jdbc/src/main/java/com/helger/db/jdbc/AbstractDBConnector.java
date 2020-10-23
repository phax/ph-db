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
package com.helger.db.jdbc;

import java.io.Closeable;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
import com.helger.commons.concurrent.SimpleLock;
import com.helger.commons.string.ToStringGenerator;

/**
 * Abstract implementation of {@link IHasDataSource} based on
 * {@link BasicDataSource} implementation.
 *
 * @author Philip Helger
 */
@ThreadSafe
public abstract class AbstractDBConnector implements IHasDataSource, Closeable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractDBConnector.class);

  @Nonnull
  private final SimpleLock m_aLock = new SimpleLock ();
  protected BasicDataSource m_aDataSource;

  public AbstractDBConnector ()
  {}

  @Nonnull
  protected final SimpleLock getLock ()
  {
    return m_aLock;
  }

  @Nonnull
  @Nonempty
  protected abstract String getJDBCDriverClassName ();

  /**
   * @return The final connection URL to be used for connecting. May not be
   *         <code>null</code>.
   */
  @Nonnull
  public abstract String getConnectionUrl ();

  /**
   * @return Connection user name
   */
  @Nullable
  protected abstract String getUserName ();

  /**
   * @return Connection password
   */
  @Nullable
  protected abstract String getPassword ();

  @OverrideOnDemand
  protected boolean isUseDefaultAutoCommit ()
  {
    return false;
  }

  @OverrideOnDemand
  protected boolean isPoolPreparedStatements ()
  {
    return true;
  }

  @Nonnull
  public final DataSource getDataSource ()
  {
    return m_aLock.lockedGet ( () -> {
      BasicDataSource ret = m_aDataSource;
      if (ret != null && ret.isClosed ())
        ret = null;
      if (ret == null)
      {
        // build data source
        // This is usually only called once on startup and than the same
        // DataSource is reused during the entire lifetime
        ret = new BasicDataSource ();
        ret.setDriverClassName (getJDBCDriverClassName ());
        final String sUserName = getUserName ();
        if (sUserName != null)
          ret.setUsername (sUserName);
        final String sPassword = getPassword ();
        if (sPassword != null)
          ret.setPassword (sPassword);
        ret.setUrl (getConnectionUrl ());

        // settings
        ret.setDefaultAutoCommit (Boolean.valueOf (isUseDefaultAutoCommit ()));
        ret.setPoolPreparedStatements (isPoolPreparedStatements ());

        // Remember when ready
        m_aDataSource = ret;

        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("Created new DataSource " + ret);
      }
      return ret;
    });
  }

  public final void close ()
  {
    try
    {
      m_aLock.lockedThrowing ( () -> {
        if (m_aDataSource != null)
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Now closing DataSource");

          m_aDataSource.close ();
          m_aDataSource = null;

          if (LOGGER.isInfoEnabled ())
            LOGGER.info ("Closed DataSource");
        }
      });
    }
    catch (final SQLException ex)
    {
      throw new IllegalStateException ("Failed to close DataSource " + m_aDataSource, ex);
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("DataSource", m_aDataSource).getToString ();
  }
}
