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
import com.helger.db.api.config.IJdbcDataSourceConfiguration;

/**
 * Shared utility for running Flyway database migrations. This class encapsulates the common Flyway
 * setup that is duplicated across multiple projects.
 *
 * @author Philip Helger
 * @since 8.1.4
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
   *        The JDBC data source configuration providing the JDBC driver. May not be
   *        <code>null</code>.
   * @param aFlywayConfig
   *        The Flyway configuration. May not be <code>null</code>.
   * @param sLocation
   *        The Flyway migration scripts location (e.g. {@code "db/migrate-postgresql"}). May
   *        neither be <code>null</code> nor empty.
   * @param sBaselineDescription
   *        The baseline description. May be <code>null</code>.
   * @param aJavaMigrations
   *        Optional Java migration instances. May be <code>null</code> or empty.
   * @param aCallbacks
   *        Optional Flyway callbacks. May be <code>null</code> or empty. If <code>null</code> or
   *        empty, {@link #CALLBACK_LOGGING} is used as the default.
   * @param sSchema
   *        The database schema name. May be <code>null</code>.
   * @param bSchemaCreate
   *        <code>true</code> to let Flyway create the schema if it does not exist.
   */
  public static void runFlyway (@NonNull final IJdbcDataSourceConfiguration aJdbcConfig,
                                @NonNull final IFlywayConfiguration aFlywayConfig,
                                @NonNull final String sLocation,
                                @Nullable final String sBaselineDescription,
                                @Nullable final JavaMigration [] aJavaMigrations,
                                @Nullable final Callback [] aCallbacks,
                                @Nullable final String sSchema,
                                final boolean bSchemaCreate)
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

    // Required for creating DB tables
    aActualFlywayConfig.baselineOnMigrate (true);

    // Disable validation, because DDL comments are also taken into consideration
    aActualFlywayConfig.validateOnMigrate (false);

    // Baseline version
    aActualFlywayConfig.baselineVersion (Integer.toString (aFlywayConfig.getFlywayBaselineVersion ()));
    if (StringHelper.isNotEmpty (sBaselineDescription))
      aActualFlywayConfig.baselineDescription (sBaselineDescription);

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

    // Schema
    if (StringHelper.isNotEmpty (sSchema))
      aActualFlywayConfig.schemas (sSchema);

    aActualFlywayConfig.createSchemas (bSchemaCreate);

    // Custom history table name
    final String sHistoryTable = aFlywayConfig.getFlywayHistoryTable ();
    if (StringHelper.isNotEmpty (sHistoryTable))
      aActualFlywayConfig.table (sHistoryTable);

    final Flyway aFlyway = aActualFlywayConfig.load ();
    aFlyway.migrate ();

    LOGGER.info ("Finished running Flyway");
  }
}
