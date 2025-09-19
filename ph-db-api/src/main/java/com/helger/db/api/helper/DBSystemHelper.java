/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.db.api.helper;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.db.api.EDatabaseSystemType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A common helper class that deals with database system specific functionality
 *
 * @author Philip Helger
 * @since 8.0.1
 */
@Immutable
public final class DBSystemHelper
{
  private DBSystemHelper ()
  {}

  /**
   * Get the proper SQL "table name prefix" using the correct syntax depending on the DB system used
   *
   * @param eDBType
   *        Database type. May not be <code>null</code>.
   * @param sJdbcSchema
   *        The JDBC schema name that should be used as a prefix. May be <code>null</code>.
   * @return A non-<code>null</code> prefix.
   */
  @Nonnull
  public static String getTableNamePrefix (@Nonnull final EDatabaseSystemType eDBType,
                                           @Nullable final String sJdbcSchema)
  {
    ValueEnforcer.notNull (eDBType, "DBType");

    final String sSchemaName = StringHelper.trim (sJdbcSchema);
    if (StringHelper.isNotEmpty (sSchemaName))
    {
      // Quotes around are required for special chars
      return switch (eDBType)
      {
        // MySQL rules: https://dev.mysql.com/doc/refman/8.4/en/identifiers.html
        // Use backtick as char of choice
        case MYSQL -> '`' + sSchemaName + "`.";
        // Tested and works at least for Postgres
        default -> '"' + sSchemaName + "\".";
      };
    }
    // May not be null
    return "";
  }
}
