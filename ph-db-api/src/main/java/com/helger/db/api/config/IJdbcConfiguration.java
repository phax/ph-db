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
package com.helger.db.api.config;

import com.helger.db.api.EDatabaseSystemType;

import jakarta.annotation.Nullable;

/**
 * Read-only interface for JDBC configuration options.
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public interface IJdbcConfiguration extends IJdbcDataSourceConfiguration
{
  @Nullable
  String getJdbcDatabaseType ();

  @Nullable
  default EDatabaseSystemType getJdbcDatabaseSystemType ()
  {
    final String sID = getJdbcDatabaseType ();
    // Use case insensitive resolution by default
    return EDatabaseSystemType.getFromIDCaseInsensitiveOrNull (sID);
  }

  @Nullable
  String getJdbcSchema ();

  boolean isJdbcExecutionTimeWarningEnabled ();

  long getJdbcExecutionTimeWarningMilliseconds ();

  boolean isJdbcDebugConnections ();

  boolean isJdbcDebugTransactions ();

  boolean isJdbcDebugSQL ();
}
