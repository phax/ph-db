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

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;
import com.helger.db.jdbc.executor.DBNoConnectionException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Implementation of {@link IHasConnection} that creates a connection from an
 * {@link IHasDataSource}.
 *
 * @author Philip Helger
 */
public class ConnectionFromDataSourceProvider implements IHasConnection
{
  private final DataSource m_aDS;
  private boolean m_bLogException = false;

  @SuppressFBWarnings ("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
  public ConnectionFromDataSourceProvider (@Nonnull final IHasDataSource aDSP)
  {
    ValueEnforcer.notNull (aDSP, "DataSourceProvider");
    m_aDS = aDSP.getDataSource ();
    if (m_aDS == null)
      throw new IllegalArgumentException ("Failed to create dataSource from " + aDSP);
  }

  public final boolean isLogException ()
  {
    return m_bLogException;
  }

  @Nonnull
  public final ConnectionFromDataSourceProvider setLogException (final boolean bLogException)
  {
    m_bLogException = bLogException;
    return this;
  }

  @Nonnull
  public Connection getConnection () throws DBNoConnectionException
  {
    try
    {
      final Connection ret = m_aDS.getConnection ();
      if (ret == null)
        throw new DBNoConnectionException ("No connection retrieved from dataSource " + m_aDS);
      return ret;
    }
    catch (final SQLException ex)
    {
      // ex.getCause is e.g. a
      // com.mysql.cj.jdbc.exceptions.CommunicationsException
      throw new DBNoConnectionException ("No connection retrieved from dataSource " + m_aDS, ex);
    }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("DataSource", m_aDS).getToString ();
  }
}
