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

import java.time.Duration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.telemetry.ITelemetrySpan;
import com.helger.telemetry.TelemetryAttributes;

/**
 * Emits the ph-telemetry span attributes and metrics for the operations executed by
 * {@link JPAEnabledManager}. All emission happens through the vendor neutral ph-telemetry facades,
 * so without a registered SPI everything degrades to cheap no-ops.
 * <p>
 * {@link JPAEnabledManager} never rethrows the exception of a failed operation - it is returned
 * inside a {@link JPAExecutionResult} instead. Therefore the exception is explicitly recorded on
 * the span here, because the surrounding {@code Telemetry.withSpan} never sees it.
 *
 * @author Philip Helger
 * @since 8.5.0
 */
@Immutable
final class JPATelemetry
{
  private JPATelemetry ()
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
   * Emit the end-of-operation metrics.
   *
   * @param sOperation
   *        The kind of operation that ended. May not be <code>null</code>.
   * @param sErrorType
   *        The class name of the exception that made the operation fail, or <code>null</code> if it
   *        was successful.
   * @param aDuration
   *        The wall-clock duration of the operation. May not be <code>null</code>.
   */
  private static void _onEnd (@NonNull final String sOperation,
                              @Nullable final String sErrorType,
                              @NonNull final Duration aDuration)
  {
    // The span is a no-op if telemetry is disabled - the metrics must be suppressed explicitly
    if (!JPAEnabledManager.isTelemetryEnabled ())
      return;

    final TelemetryAttributes aAttrs = TelemetryAttributes.builder ()
                                                          .put (CDBTelemetry.ATTR_COMPONENT,
                                                                CDBTelemetry.COMPONENT_JPA)
                                                          .put (CDBTelemetry.ATTR_JPA_OPERATION, sOperation)
                                                          .put (CDBTelemetry.ATTR_SUCCESS, sErrorType == null)
                                                          // Only present in case of an error, as
                                                          // the conventions demand
                                                          .put (CDBTelemetry.ATTR_ERROR_TYPE, sErrorType)
                                                          .build ();
    JPAMetrics.OPERATIONS.add (1, aAttrs);
    JPAMetrics.OPERATION_DURATION.record (_toSeconds (aDuration), aAttrs);
  }

  /**
   * Set the descriptive attributes on the span covering a single JPA operation.
   *
   * @param aSpan
   *        The span to fill. May not be <code>null</code>.
   * @param sOperation
   *        The kind of operation that was started. May not be <code>null</code>.
   */
  static void onStart (@NonNull final ITelemetrySpan aSpan, @NonNull final String sOperation)
  {
    aSpan.setAttribute (CDBTelemetry.ATTR_COMPONENT, CDBTelemetry.COMPONENT_JPA);
    aSpan.setAttribute (CDBTelemetry.ATTR_JPA_OPERATION, sOperation);
  }

  /**
   * Remember whether the operation started a new transaction or joined an already active one.
   *
   * @param aSpan
   *        The span to fill. May not be <code>null</code>.
   * @param bTransactionStarted
   *        <code>true</code> if a new transaction was started.
   */
  static void onTransactionStarted (@NonNull final ITelemetrySpan aSpan, final boolean bTransactionStarted)
  {
    aSpan.setAttribute (CDBTelemetry.ATTR_JPA_TRANSACTION_STARTED, bTransactionStarted);
  }

  /**
   * Mark the span of a successful operation and emit its metrics.
   *
   * @param aSpan
   *        The span to mark. May not be <code>null</code>.
   * @param sOperation
   *        The kind of operation that ended. May not be <code>null</code>.
   * @param aDuration
   *        The wall-clock duration of the operation. May not be <code>null</code>.
   */
  static void onSuccess (@NonNull final ITelemetrySpan aSpan,
                         @NonNull final String sOperation,
                         @NonNull final Duration aDuration)
  {
    aSpan.setAttribute (CDBTelemetry.ATTR_SUCCESS, true);
    aSpan.setStatusOk ();
    _onEnd (sOperation, null, aDuration);
  }

  /**
   * Mark the span of a failed operation and emit its metrics.
   *
   * @param aSpan
   *        The span to mark. May not be <code>null</code>.
   * @param sOperation
   *        The kind of operation that ended. May not be <code>null</code>.
   * @param aException
   *        The exception that occurred. May not be <code>null</code>.
   * @param aDuration
   *        The wall-clock duration of the operation. May not be <code>null</code>.
   */
  static void onError (@NonNull final ITelemetrySpan aSpan,
                       @NonNull final String sOperation,
                       @NonNull final Exception aException,
                       @NonNull final Duration aDuration)
  {
    final String sErrorType = aException.getClass ().getName ();
    aSpan.setAttribute (CDBTelemetry.ATTR_SUCCESS, false);
    aSpan.setAttribute (CDBTelemetry.ATTR_ERROR_TYPE, sErrorType);
    aSpan.recordException (aException);
    aSpan.setStatusError (aException.getMessage ());
    _onEnd (sOperation, sErrorType, aDuration);
  }
}
