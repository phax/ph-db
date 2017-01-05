/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.db.jdbc.executor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.traits.IConvertibleDirectTrait;
import com.helger.db.api.jdbc.JDBCHelper;

/**
 * Represents a single DB query result value within a result row.
 *
 * @author Philip Helger
 */
@Immutable
public class DBResultField implements IConvertibleDirectTrait
{
  private final String m_sColumnName;
  private final int m_nColumnType;
  private final Object m_aValue;

  public DBResultField (@Nonnull @Nonempty final String sColumnName,
                        final int nColumnType,
                        @Nullable final Object aValue)
  {
    ValueEnforcer.notEmpty (sColumnName, "ColumnName");
    m_sColumnName = sColumnName;
    m_nColumnType = nColumnType;
    m_aValue = aValue;
  }

  @Nonnull
  @Nonempty
  public String getColumnName ()
  {
    return m_sColumnName;
  }

  /**
   * @return The column type as defined in {@link java.sql.Types}.
   */
  public int getColumnType ()
  {
    return m_nColumnType;
  }

  @Nullable
  public String getColumnTypeName ()
  {
    return JDBCHelper.getJDBCTypeName (m_nColumnType);
  }

  /**
   * @return The generic value as retrieved from the DB
   */
  @Nullable
  public Object getValue ()
  {
    return m_aValue;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("columnName", m_sColumnName)
                                       .append ("columnType", m_nColumnType)
                                       .append ("value", m_aValue)
                                       .toString ();
  }
}
