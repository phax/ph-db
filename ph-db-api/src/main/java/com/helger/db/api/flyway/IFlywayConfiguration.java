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
package com.helger.db.api.flyway;

import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonnegative;

/**
 * Flyway configuration interface
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public interface IFlywayConfiguration
{
  /**
   * @return <code>true</code> if Flyway is enabled, <code>false</code> if it is disabled.
   */
  boolean isFlywayEnabled ();

  /**
   * @return The JDBC URL that Flyway should use. May be <code>null</code>.
   */
  @Nullable
  String getFlywayJdbcUrl ();

  /**
   * @return The JDBC username that Flyway should use. May be <code>null</code>.
   */
  @Nullable
  String getFlywayJdbcUser ();

  /**
   * @return The JDB password that Flyway should use. May be <code>null</code>.
   */
  @Nullable
  String getFlywayJdbcPassword ();

  /**
   * @return <code>true</code> if Flyway should create the Schema, if it is not existing,
   *         <code>false</code> if the schema must exist already.
   */
  boolean isFlywaySchemaCreate ();

  /**
   * @return The baseline version that Flyway expects. 0 means no previous version and should be the
   *         default. Values &lt; 0 are not allowed.
   */
  @Nonnegative
  int getFlywayBaselineVersion ();
}
