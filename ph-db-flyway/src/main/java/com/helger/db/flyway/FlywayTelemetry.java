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
package com.helger.db.flyway;

import java.time.Duration;
import java.util.function.Consumer;

import org.flywaydb.core.api.output.MigrateResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.timing.StopWatch;
import com.helger.db.api.EDatabaseSystemType;
import com.helger.db.api.config.IJdbcConfiguration;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.telemetry.ETelemetrySpanKind;
import com.helger.telemetry.ITelemetrySpan;
import com.helger.telemetry.Telemetry;
import com.helger.telemetry.TelemetryAttributes;

/**
 * Emits the ph-telemetry span and metrics for the migration runs of {@link FlywayMigrationRunner}.
 * All emission happens through the vendor neutral ph-telemetry facades, so without a registered SPI
 * everything degrades to cheap no-ops. Since a migration run happens once per application startup,
 * there is no switch to disable the emission.
 *
 * @author Philip Helger
 * @since 8.5.0
 */
@Immutable
final class FlywayTelemetry
{
  private FlywayTelemetry ()
  {}

  /**
   * @param aDuration
   *        The duration to convert. May not be <code>null</code>.
   * @return The provided duration in seconds - the unit all ph-db duration instruments use.
   */
  private static double _toSeconds (@NonNull final Duration aDuration)
  {
    return aDuration.toNanos () / 1_000_000_000.0;
  }

  /**
   * Remember the outcome of a finished migration run on the span and count the executed
   * migrations.
   *
   * @param aSpan
   *        The span of the migration run. May not be <code>null</code>.
   * @param eDBSystemType
   *        The database system in use. May be <code>null</code> if unknown.
   * @param aResult
   *        The result of the migration run. May not be <code>null</code>.
   */
  static void onMigrated (@NonNull final ITelemetrySpan aSpan,
                          @Nullable final EDatabaseSystemType eDBSystemType,
                          @NonNull final MigrateResult aResult)
  {
    aSpan.setAttribute (CDBTelemetry.ATTR_FLYWAY_MIGRATIONS_EXECUTED, aResult.migrationsExecuted);
    aSpan.setAttribute (CDBTelemetry.ATTR_FLYWAY_VERSION_INITIAL, aResult.initialSchemaVersion);
    aSpan.setAttribute (CDBTelemetry.ATTR_FLYWAY_VERSION_TARGET, aResult.targetSchemaVersion);
    aSpan.setAttribute (CDBTelemetry.ATTR_SUCCESS, aResult.success);

    if (aResult.migrationsExecuted > 0)
    {
      final TelemetryAttributes.Builder aBuilder = TelemetryAttributes.builder ();
      aBuilder.put (CDBTelemetry.ATTR_COMPONENT, CDBTelemetry.COMPONENT_FLYWAY);
      if (eDBSystemType != null)
        aBuilder.put (CDBTelemetry.ATTR_DB_SYSTEM_NAME, eDBSystemType.getID ());
      FlywayMetrics.MIGRATIONS.add (aResult.migrationsExecuted, aBuilder.build ());
    }
  }

  /**
   * Run a whole Flyway migration inside a span and record the migration run metrics.
   *
   * @param aJdbcConfig
   *        The JDBC configuration in use. May not be <code>null</code>.
   * @param aFlywayConfig
   *        The Flyway configuration in use. May not be <code>null</code>.
   * @param sLocation
   *        The Flyway migration scripts location. May not be <code>null</code>.
   * @param aBody
   *        The migration run. May not be <code>null</code>.
   */
  static void withMigrateDo (@NonNull final IJdbcConfiguration aJdbcConfig,
                             @NonNull final IFlywayConfiguration aFlywayConfig,
                             @NonNull final String sLocation,
                             @NonNull final Consumer <ITelemetrySpan> aBody)
  {
    final EDatabaseSystemType eDBSystemType = aJdbcConfig.getJdbcDatabaseSystemType ();
    final StopWatch aSW = StopWatch.createdStarted ();
    String sErrorType = null;
    try
    {
      Telemetry.withSpanVoid (CDBTelemetry.SPAN_FLYWAY_MIGRATE, ETelemetrySpanKind.CLIENT, aSpan -> {
        aSpan.setAttribute (CDBTelemetry.ATTR_COMPONENT, CDBTelemetry.COMPONENT_FLYWAY);
        if (eDBSystemType != null)
          aSpan.setAttribute (CDBTelemetry.ATTR_DB_SYSTEM_NAME, eDBSystemType.getID ());
        aSpan.setAttribute (CDBTelemetry.ATTR_DB_NAMESPACE, aJdbcConfig.getJdbcSchema ());
        aSpan.setAttribute (CDBTelemetry.ATTR_FLYWAY_LOCATION, sLocation);
        aSpan.setAttribute (CDBTelemetry.ATTR_FLYWAY_REPAIR_MODE, aFlywayConfig.isFlywayRepairMode ());

        aBody.accept (aSpan);
        aSpan.setStatusOk ();
      });
    }
    catch (final RuntimeException ex)
    {
      sErrorType = ex.getClass ().getName ();
      throw ex;
    }
    finally
    {
      final TelemetryAttributes.Builder aBuilder = TelemetryAttributes.builder ();
      aBuilder.put (CDBTelemetry.ATTR_COMPONENT, CDBTelemetry.COMPONENT_FLYWAY);
      if (eDBSystemType != null)
        aBuilder.put (CDBTelemetry.ATTR_DB_SYSTEM_NAME, eDBSystemType.getID ());
      aBuilder.put (CDBTelemetry.ATTR_SUCCESS, sErrorType == null);
      // Only present in case of an error, as the conventions demand
      aBuilder.put (CDBTelemetry.ATTR_ERROR_TYPE, sErrorType);

      FlywayMetrics.MIGRATE_DURATION.record (_toSeconds (aSW.stopAndGetDuration ()), aBuilder.build ());
    }
  }
}
