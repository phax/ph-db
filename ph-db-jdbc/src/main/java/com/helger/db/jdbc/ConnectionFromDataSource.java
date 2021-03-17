/**
 * Copyright (C) 2014-2021 Philip Helger (www.helger.com)
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

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;
import com.helger.db.jdbc.executor.DBNoConnectionException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Implementation of {@link IHasConnection} that creates a connection from an
 * {@link DataSource}.
 *
 * @author Philip Helger
 * @since 6.5.1
 */
public class ConnectionFromDataSource implements IHasConnection
{
  public static final boolean DEFAULT_VALIDITY_CHECK_ENABLED = true;
  public static final int DEFAULT_VALIDITY_CHECK_TIMEOUT_SECONDS = 3;
  private static final Logger LOGGER = LoggerFactory.getLogger (ConnectionFromDataSource.class);

  private final DataSource m_aDS;
  private boolean m_bValidityCheckEnabled = DEFAULT_VALIDITY_CHECK_ENABLED;
  private int m_nValidityCheckTimeoutSeconds = DEFAULT_VALIDITY_CHECK_TIMEOUT_SECONDS;

  public ConnectionFromDataSource (@Nonnull final DataSource aDS)
  {
    ValueEnforcer.notNull (aDS, "DataSource");
    m_aDS = aDS;
  }

  @Nonnull
  protected final DataSource getDataSource ()
  {
    return m_aDS;
  }

  public final boolean isValidityCheckEnabled ()
  {
    return m_bValidityCheckEnabled;
  }

  @Nonnull
  public final ConnectionFromDataSource setValidityCheckEnabled (final boolean bValidityCheckEnabled)
  {
    m_bValidityCheckEnabled = bValidityCheckEnabled;
    return this;
  }

  public final int getValidityCheckTimeoutSeconds ()
  {
    return m_nValidityCheckTimeoutSeconds;
  }

  @Nonnull
  public final ConnectionFromDataSource setValidityCheckTimeoutSeconds (final int nValidityCheckTimeoutSeconds)
  {
    // 0 means infinite waiting
    m_nValidityCheckTimeoutSeconds = Math.min (nValidityCheckTimeoutSeconds, 0);
    return this;
  }

  @Nonnull
  public Connection getConnection () throws DBNoConnectionException
  {
    try
    {
      final Connection ret = m_aDS.getConnection ();
      if (ret == null)
        throw new DBNoConnectionException ("No connection retrieved from DataSource " + m_aDS);

      if (m_bValidityCheckEnabled)
      {
        // Value is in seconds
        if (ret.isValid (m_nValidityCheckTimeoutSeconds))
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("SQL Connection is valid (" + m_nValidityCheckTimeoutSeconds + ")");
        }
        else
        {
          throw new DBNoConnectionException ("SQL Connection from DataSource " +
                                             m_aDS +
                                             " is not valid anymore (" +
                                             m_nValidityCheckTimeoutSeconds +
                                             " seconds timeout)");
        }
      }

      return ret;
    }
    catch (final SQLException ex)
    {
      // ex.getCause is e.g. a
      // com.mysql.cj.jdbc.exceptions.CommunicationsException
      throw new DBNoConnectionException ("No Connection retrieved from DataSource " + m_aDS, ex);
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("DataSource", m_aDS).getToString ();
  }

  @SuppressFBWarnings ("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
  public static ConnectionFromDataSource create (@Nonnull final IHasDataSource aDSP)
  {
    ValueEnforcer.notNull (aDSP, "DataSourceProvider");
    return new ConnectionFromDataSource (aDSP.getDataSource ());
  }
}
