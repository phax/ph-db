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

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;

/**
 * Implementation of {@link IHasConnection} that with a constant
 * {@link Connection}
 *
 * @author Philip Helger
 * @since 6.2.0
 */
public class ConstantConnection implements IHasConnection
{
  private final Connection m_aConnection;
  private final boolean m_bShouldCloseConnection;

  public ConstantConnection (@Nonnull final Connection aConnection, final boolean bShouldCloseConnection)
  {
    ValueEnforcer.notNull (aConnection, "Connection");
    m_aConnection = aConnection;
    m_bShouldCloseConnection = bShouldCloseConnection;
  }

  @Nonnull
  public Connection getConnection ()
  {
    return m_aConnection;
  }

  public boolean shouldCloseConnection ()
  {
    return m_bShouldCloseConnection;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Connection", m_aConnection)
                                       .append ("ShouldCloseConnection", m_bShouldCloseConnection)
                                       .getToString ();
  }
}
