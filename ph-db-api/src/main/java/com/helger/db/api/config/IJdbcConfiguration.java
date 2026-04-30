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
package com.helger.db.api.config;

import java.time.Duration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.CheckForSigned;
import com.helger.db.api.EDatabaseSystemType;

/**
 * Read-only interface for JDBC configuration options.
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public interface IJdbcConfiguration extends IJdbcDataSourceConfiguration
{
  /**
   * @return The database type identifier (e.g. "PostgreSQL", "MySQL"). May be <code>null</code>.
   */
  @Nullable
  String getJdbcDatabaseType ();

  /**
   * @return The resolved {@link EDatabaseSystemType} from the database type string, or
   *         <code>null</code> if the database type is not set or not recognized.
   */
  @Nullable
  default EDatabaseSystemType getJdbcDatabaseSystemType ()
  {
    final String sID = getJdbcDatabaseType ();
    // Use case insensitive resolution by default
    return EDatabaseSystemType.getFromIDCaseInsensitiveOrNull (sID);
  }

  /**
   * @return The database schema to use. May be <code>null</code>.
   */
  @Nullable
  String getJdbcSchema ();

  /**
   * @return <code>true</code> if logging of long-running JDBC executions is enabled.
   */
  boolean isJdbcExecutionTimeWarningEnabled ();

  /**
   * @return The threshold above which a JDBC execution time warning is logged. Never
   *         <code>null</code>.
   * @since 8.3.0
   */
  @NonNull
  default Duration getJdbcExecutionTimeWarning ()
  {
    return Duration.ofMillis (getJdbcExecutionTimeWarningMilliseconds ());
  }

  /**
   * @return The threshold in milliseconds above which a JDBC execution time warning is logged.
   * @deprecated Since 8.3.0; use {@link #getJdbcExecutionTimeWarning()} instead.
   */
  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  long getJdbcExecutionTimeWarningMilliseconds ();

  /**
   * @return <code>true</code> if debug logging of JDBC connection lifecycle (open/close) is
   *         enabled.
   */
  boolean isJdbcDebugConnections ();

  /**
   * @return <code>true</code> if debug logging of JDBC transactions (begin/commit/rollback) is
   *         enabled.
   */
  boolean isJdbcDebugTransactions ();

  /**
   * @return <code>true</code> if debug logging of executed SQL statements is enabled.
   */
  boolean isJdbcDebugSQL ();
}
