/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.db.jdbc.h2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;

public class H2MemConnector extends AbstractH2Connector
{
  protected final String m_sDBName;
  protected final String m_sUser;
  protected final String m_sPassword;

  public H2MemConnector (@Nullable final String sUser, @Nullable final String sPassword)
  {
    this ("h2memdb", sUser, sPassword);
  }

  public H2MemConnector (@Nonnull @Nonempty final String sDBName,
                         @Nullable final String sUser,
                         @Nullable final String sPassword)
  {
    ValueEnforcer.notNull (sDBName, "DBName");
    m_sDBName = sDBName;
    m_sUser = sUser;
    m_sPassword = sPassword;
  }

  @Nonnull
  @Nonempty
  public String getDBName ()
  {
    return m_sDBName;
  }

  @Override
  @Nonnull
  protected String getUserName ()
  {
    return m_sUser;
  }

  @Override
  @Nonnull
  protected String getPassword ()
  {
    return m_sPassword;
  }

  @Override
  @Nonnull
  protected String getDatabaseName ()
  {
    return "mem:" + m_sDBName;
  }
}
