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
package com.helger.db.jpa;

import com.helger.annotation.concurrent.Immutable;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.telemetry.ITelemetryCounter;
import com.helger.telemetry.ITelemetryHistogram;
import com.helger.telemetry.TelemetryMetrics;

/**
 * Central registry of the named metric instruments emitted by {@link JPAEnabledManager}. Each
 * instrument is created once at class-load time via the vendor neutral {@link TelemetryMetrics}
 * facade - if no {@code ITelemetryMeterSPI} is registered, the underlying instruments are cheap
 * no-ops, so referencing this class in a deployment without an observability backend has no cost.
 *
 * @author Philip Helger
 * @since 8.5.0
 */
@Immutable
public final class JPAMetrics
{
  /**
   * Duration of a single JPA operation, by kind of operation and success. This is the same
   * instrument {@code DBExecutorMetrics.OPERATION_DURATION} uses - the
   * {@link CDBTelemetry#ATTR_COMPONENT} attribute separates the JDBC from the JPA operations.
   */
  public static final ITelemetryHistogram OPERATION_DURATION = TelemetryMetrics.histogram (CDBTelemetry.METRIC_CLIENT_OPERATION_DURATION,
                                                                                           CDBTelemetry.METRIC_CLIENT_OPERATION_DURATION_DESC,
                                                                                           CDBTelemetry.UNIT_SECONDS);

  /** Executed JPA operations, by kind of operation and success. */
  public static final ITelemetryCounter OPERATIONS = TelemetryMetrics.counter (CDBTelemetry.METRIC_JPA_OPERATIONS,
                                                                               "Executed JPA operations - successful and failed ones",
                                                                               CDBTelemetry.UNIT_OPERATION);

  private JPAMetrics ()
  {}
}
