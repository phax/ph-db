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

import com.helger.annotation.concurrent.Immutable;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.telemetry.ITelemetryCounter;
import com.helger.telemetry.ITelemetryHistogram;
import com.helger.telemetry.TelemetryMetrics;

/**
 * Central registry of the named metric instruments emitted by {@link FlywayMigrationRunner}. Each
 * instrument is created once at class-load time via the vendor neutral {@link TelemetryMetrics}
 * facade - if no {@code ITelemetryMeterSPI} is registered, the underlying instruments are cheap
 * no-ops, so referencing this class in a deployment without an observability backend has no cost.
 *
 * @author Philip Helger
 * @since 8.5.0
 */
@Immutable
public final class FlywayMetrics
{
  /** Migrations executed by Flyway, by database system. */
  public static final ITelemetryCounter MIGRATIONS = TelemetryMetrics.counter (CDBTelemetry.METRIC_FLYWAY_MIGRATIONS,
                                                                               "Migrations executed by Flyway",
                                                                               CDBTelemetry.UNIT_MIGRATION);

  /** Duration of a whole Flyway migration run, by database system and success. */
  public static final ITelemetryHistogram MIGRATE_DURATION = TelemetryMetrics.histogram (CDBTelemetry.METRIC_FLYWAY_MIGRATE_DURATION,
                                                                                         "Duration of a whole Flyway migration run",
                                                                                         CDBTelemetry.UNIT_SECONDS);

  private FlywayMetrics ()
  {}
}
