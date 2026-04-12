/*
 * Copyright (C) 2026 Philip Helger (www.helger.com)
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
package com.helger.db.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.BaseCallback;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.logging.LogLevel;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.array.ArrayHelper;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.db.api.config.IJdbcConfiguration;

/**
 * Shared utility for running Flyway database migrations. This class encapsulates the common Flyway
 * setup that is duplicated across multiple projects.
 *
 * @author Philip Helger
 * @since 8.2.0
 */
public final class FlywayMigrationRunner
{
  /**
   * A reusable Flyway {@link Callback} that logs migration events.
   */
  public static final Callback CALLBACK_LOGGING = new BaseCallback ()
  {
    @SuppressWarnings ("hiding")
    private static final Logger LOGGER = LoggerFactory.getLogger (FlywayMigrationRunner.class.getName () + "$Logging");

    @Override
    public boolean supports (@NonNull final Event aEvent, @Nullable final Context aContext)
    {
      // Deprecated by Flyway itself - just to avoid warnings
      if (aEvent == Event.CREATE_SCHEMA)
        return false;
      return super.supports (aEvent, aContext);
    }

    public void handle (@NonNull final Event aEvent, @Nullable final Context aContext)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Flyway: Event " + aEvent.getId ());

      if (aEvent == Event.AFTER_EACH_MIGRATE && aContext != null)
      {
        final MigrationInfo aMI = aContext.getMigrationInfo ();
        if (aMI instanceof final MigrationInfoImpl aMII)
        {
          final ResolvedMigration aRM = aMII.getResolvedMigration ();
          if (aRM != null)
            LOGGER.info ("  Performed migration: " + aRM);
        }
      }
    }
  };

  private static final Logger LOGGER = LoggerFactory.getLogger (FlywayMigrationRunner.class);

  private FlywayMigrationRunner ()
  {}

  /**
   * Run Flyway database migration.
   *
   * @param aJdbcConfig
   *        The JDBC configuration providing the JDBC driver and the schema name. May not be
   *        <code>null</code>.
   * @param aFlywayConfig
   *        The Flyway configuration. May not be <code>null</code>.
   * @param sLocation
   *        The Flyway migration scripts location (e.g. {@code "db/migrate-postgresql"}). May
   *        neither be <code>null</code> nor empty.
   * @param aJavaMigrations
   *        Optional Java migration instances. May be <code>null</code> or empty.
   * @param aCallbacks
   *        Optional Flyway callbacks. May be <code>null</code> or empty. If <code>null</code> or
   *        empty, {@link #CALLBACK_LOGGING} is used as the default.
   */
  public static void runFlyway (@NonNull final IJdbcConfiguration aJdbcConfig,
                                @NonNull final IFlywayConfiguration aFlywayConfig,
                                @NonNull final String sLocation,
                                @Nullable final JavaMigration @NonNull [] aJavaMigrations,
                                @Nullable final Callback @NonNull [] aCallbacks)
  {
    ValueEnforcer.notNull (aJdbcConfig, "JdbcConfig");
    ValueEnforcer.notNull (aFlywayConfig, "FlywayConfig");
    ValueEnforcer.notEmpty (sLocation, "Location");

    LOGGER.info ("Starting to run Flyway with location '" + sLocation + "'");

    final FluentConfiguration aActualFlywayConfig = Flyway.configure ()
                                                          .dataSource (new DriverDataSource (FlywayMigrationRunner.class.getClassLoader (),
                                                                                             aJdbcConfig.getJdbcDriver (),
                                                                                             aFlywayConfig.getFlywayJdbcUrl (),
                                                                                             aFlywayConfig.getFlywayJdbcUser (),
                                                                                             aFlywayConfig.getFlywayJdbcPassword ()));

    aActualFlywayConfig.loggers ("slf4j");

    // Required for creating DB tables
    aActualFlywayConfig.baselineOnMigrate (true);

    // Disable validation, because DDL comments are also taken into consideration
    aActualFlywayConfig.validateOnMigrate (false);

    // Baseline version
    aActualFlywayConfig.baselineVersion (Integer.toString (aFlywayConfig.getFlywayBaselineVersion ()));
    aActualFlywayConfig.baselineDescription ("Flyway baseline");

    // Migration script location
    aActualFlywayConfig.locations (sLocation);

    // Java migrations
    if (ArrayHelper.isNotEmpty (aJavaMigrations))
      aActualFlywayConfig.javaMigrations (aJavaMigrations);

    // Callbacks
    if (ArrayHelper.isNotEmpty (aCallbacks))
      aActualFlywayConfig.callbacks (aCallbacks);
    else
      aActualFlywayConfig.callbacks (CALLBACK_LOGGING);

    // Flyway to handle the DB schema?
    final String sSchema = aJdbcConfig.getJdbcSchema ();
    if (StringHelper.isNotEmpty (sSchema))
    {
      // Use the schema only, if it is explicitly configured
      // The default schema name is ["$user", public] and as such unusable
      aActualFlywayConfig.schemas (sSchema);
    }
    // If no schema is specified, schema create should also be disabled
    aActualFlywayConfig.createSchemas (aFlywayConfig.isFlywaySchemaCreate ());

    // Custom history table name
    final String sHistoryTable = aFlywayConfig.getFlywayHistoryTable ();
    if (StringHelper.isNotEmpty (sHistoryTable))
      aActualFlywayConfig.table (sHistoryTable);

    // Debug mode
    if (aFlywayConfig.isFlywayDebugMode ())
    {
      LOGGER.info ("Flyway debug mode is enabled");
      LogFactory.setLogLevel (LogLevel.DEBUG);
    }

    final Flyway aFlyway = aActualFlywayConfig.load ();

    // Repair mode - run repair before migrate
    if (aFlywayConfig.isFlywayRepairMode ())
    {
      aFlyway.info ();
      LOGGER.info ("Flyway repair mode is enabled - running repair before migrate");
      aFlyway.repair ();
    }

    aFlyway.migrate ();

    LOGGER.info ("Finished running Flyway");
  }
}
