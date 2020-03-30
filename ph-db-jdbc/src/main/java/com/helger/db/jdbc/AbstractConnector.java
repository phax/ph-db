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
public abstract class AbstractConnector implements IHasDataSource, Closeable
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractConnector.class);

  @Nonnull
  private final SimpleLock m_aLock = new SimpleLock ();
  protected BasicDataSource m_aDataSource;

  public AbstractConnector ()
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
   * @return Connection user name
   */
  @Nullable
  protected abstract String getUserName ();

  /**
   * @return Connection password
   */
  @Nullable
  protected abstract String getPassword ();

  /**
   * @return Name of the database to connect to
   */
  @Nonnull
  protected abstract String getDatabaseName ();

  /**
   * @return The final connection URL to be used for connecting. May not be
   *         <code>null</code>.
   */
  @Nonnull
  public abstract String getConnectionUrl ();

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
      if (m_aDataSource == null)
      {
        // build data source
        m_aDataSource = new BasicDataSource ();
        m_aDataSource.setDriverClassName (getJDBCDriverClassName ());
        if (getUserName () != null)
          m_aDataSource.setUsername (getUserName ());
        if (getPassword () != null)
          m_aDataSource.setPassword (getPassword ());
        m_aDataSource.setUrl (getConnectionUrl ());

        // settings
        m_aDataSource.setDefaultAutoCommit (Boolean.valueOf (isUseDefaultAutoCommit ()));
        m_aDataSource.setPoolPreparedStatements (isPoolPreparedStatements ());
      }
      return m_aDataSource;
    });
  }

  public final void close ()
  {
    try
    {
      m_aLock.lockedThrowing ( () -> {
        if (m_aDataSource != null)
        {
          m_aDataSource.close ();
          m_aDataSource = null;

          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Closed database connection to '" + getDatabaseName () + "'");
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
