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
package com.helger.db.api.flyway;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.config.IConfig;
import com.helger.db.api.flyway.FlywayConfiguration.FlywayConfigurationBuilder;

/**
 * A specific {@link FlywayConfiguration} builder that takes values from configuration
 * properties.<br>
 * Note: this must be in a separate file, because it relies on {@link IConfig} which is an optional
 * compile time dependency to this module.
 *
 * @author Philip Helger
 * @since 7.1.0
 */
public class FlywayConfigurationBuilderConfig extends FlywayConfigurationBuilder
{
  public static final String SUFFIX_ENABLED = "enabled";
  public static final String SUFFIX_JDBC_URL = "jdbc.url";
  public static final String SUFFIX_JDBC_USER = "jdbc.user";
  public static final String SUFFIX_JDBC_PASSWORD = "jdbc.password";
  public static final String SUFFIX_JDBC_SCHEMA_CREATE = "jdbc.schema-create";
  public static final String SUFFIX_BASELINE_VERSION = "baseline.version";

  private final String m_sConfigPrefix;

  /**
   * Create a new builder, filled with values from the configuration. This eagerly reads all
   * configuration properties.
   *
   * @param aConfig
   *        The config object to use. May not be <code>null</code>.
   * @param sConfigPrefix
   *        The configuration common prefix to use. May not be <code>null</code> but maybe empty. If
   *        it is non-empty, it must end with a dot (".").
   */
  public FlywayConfigurationBuilderConfig (@NonNull final IConfig aConfig, @NonNull final String sConfigPrefix)
  {
    ValueEnforcer.notNull (aConfig, "Config");
    ValueEnforcer.notNull (sConfigPrefix, "ConfigPrefix");
    if (sConfigPrefix.length () > 0)
      ValueEnforcer.isTrue (sConfigPrefix.endsWith ("."),
                            () -> "ConfigPrefix '" + sConfigPrefix + "' should end with a dot");

    m_sConfigPrefix = sConfigPrefix;

    enabled (aConfig.getAsBoolean (sConfigPrefix + SUFFIX_ENABLED, FlywayConfiguration.DEFAULT_FLYWAY_ENABLED));
    jdbcUrl (aConfig.getAsString (sConfigPrefix + SUFFIX_JDBC_URL));
    jdbcUser (aConfig.getAsString (sConfigPrefix + SUFFIX_JDBC_USER));
    jdbcPassword (aConfig.getAsString (sConfigPrefix + SUFFIX_JDBC_PASSWORD));
    schemaCreate (aConfig.getAsBoolean (sConfigPrefix + SUFFIX_JDBC_SCHEMA_CREATE,
                                        FlywayConfiguration.DEFAULT_FLYWAY_JDBC_SCHEMA_CREATE));
    baselineVersion (aConfig.getAsInt (sConfigPrefix + SUFFIX_BASELINE_VERSION,
                                       FlywayConfiguration.DEFAULT_FLYWAY_BASELINE_VERSION));
  }

  /**
   * @return The configuration prefix provided in the constructor. Never <code>null</code>.
   */
  @NonNull
  public final String getConfigPrefix ()
  {
    return m_sConfigPrefix;
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyEnabled ()
  {
    return m_sConfigPrefix + SUFFIX_ENABLED;
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcUrl ()
  {
    return m_sConfigPrefix + SUFFIX_JDBC_URL;
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcUser ()
  {
    return m_sConfigPrefix + SUFFIX_JDBC_USER;
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyJdbcPassword ()
  {
    return m_sConfigPrefix + SUFFIX_JDBC_PASSWORD;
  }

  @NonNull
  @Nonempty
  public final String getConfigKeySchemaCreate ()
  {
    return m_sConfigPrefix + SUFFIX_JDBC_SCHEMA_CREATE;
  }

  @NonNull
  @Nonempty
  public final String getConfigKeyBaselineVersion ()
  {
    return m_sConfigPrefix + SUFFIX_BASELINE_VERSION;
  }
}
