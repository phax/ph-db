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

/**
 * Read-only interface for JDBC data source configuration options.
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public interface IJdbcDataSourceConfiguration
{
  @Nullable
  String getJdbcDriver ();

  @Nullable
  String getJdbcUrl ();

  @Nullable
  String getJdbcUser ();

  @Nullable
  String getJdbcPassword ();

  /**
   * @return The maximum number of active connections in the pool.
   * @since 8.1.3
   */
  @CheckForSigned
  int getJdbcPoolingMaxConnections ();

  /**
   * @return The maximum time to wait for a connection from the pool before throwing an exception.
   *         Never <code>null</code>.
   * @since 8.3.0
   */
  @NonNull
  Duration getJdbcPoolingMaxWait ();

  /**
   * @return The maximum time in milliseconds to wait for a connection from the pool before throwing
   *         an exception.
   * @since 8.1.3
   * @deprecated Since 8.3.0; use {@link #getJdbcPoolingMaxWait()} instead.
   */
  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  default long getJdbcPoolingMaxWaitMillis ()
  {
    return getJdbcPoolingMaxWait ().toMillis ();
  }

  /**
   * @return The time between runs of the idle connection evictor. Never <code>null</code>.
   * @since 8.3.0
   */
  @NonNull
  Duration getJdbcPoolingBetweenEvictionRuns ();

  /**
   * @return The time in milliseconds between runs of the idle connection evictor.
   * @since 8.1.3
   * @deprecated Since 8.3.0; use {@link #getJdbcPoolingBetweenEvictionRuns()} instead.
   */
  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  default long getJdbcPoolingBetweenEvictionRunsMillis ()
  {
    return getJdbcPoolingBetweenEvictionRuns ().toMillis ();
  }

  /**
   * @return The minimum idle time before a connection is eligible for eviction. Never
   *         <code>null</code>.
   * @since 8.3.0
   */
  @NonNull
  Duration getJdbcPoolingMinEvictableIdle ();

  /**
   * @return The minimum idle time in milliseconds before a connection is eligible for eviction.
   * @since 8.1.3
   * @deprecated Since 8.3.0; use {@link #getJdbcPoolingMinEvictableIdle()} instead.
   */
  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  default long getJdbcPoolingMinEvictableIdleMillis ()
  {
    return getJdbcPoolingMinEvictableIdle ().toMillis ();
  }

  /**
   * @return The timeout before an abandoned connection can be removed. Never <code>null</code>.
   * @since 8.3.0
   */
  @NonNull
  Duration getJdbcPoolingRemoveAbandonedTimeout ();

  /**
   * @return The timeout in milliseconds before an abandoned connection can be removed.
   * @since 8.1.3
   * @deprecated Since 8.3.0; use {@link #getJdbcPoolingRemoveAbandonedTimeout()} instead.
   */
  @CheckForSigned
  @Deprecated (forRemoval = true, since = "8.3.0")
  default long getJdbcPoolingRemoveAbandonedTimeoutMillis ()
  {
    return getJdbcPoolingRemoveAbandonedTimeout ().toMillis ();
  }

  /**
   * @return Whether or not the pool will validate objects before they are borrowed from the pool.
   * @since 8.2.1
   */
  boolean isJdbcPoolingTestOnBorrow ();
}
