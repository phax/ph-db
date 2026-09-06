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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.helger.db.api.telemetry.CDBTelemetry;
import com.helger.telemetry.ETelemetrySpanKind;
import com.helger.telemetry.mock.CapturingTelemetry;
import com.helger.telemetry.mock.CapturingTelemetry.CapturedMeasurement;
import com.helger.telemetry.mock.CapturingTelemetry.CapturedSpan;

/**
 * Test class for the ph-telemetry integration of {@link JPAEnabledManager}. Only the select
 * operations are covered here, because they are the ones that need no {@code EntityManager}.
 *
 * @author Philip Helger
 */
public final class JPAEnabledManagerTelemetryTest
{

  private static final CapturingTelemetry CT = new CapturingTelemetry ();

  @BeforeClass
  public static void installTelemetry ()
  {
    // Must happen before JPAMetrics is class-loaded, because the instruments are resolved once in
    // its static initializer
    CT.install ();
  }

  @AfterClass
  public static void uninstallTelemetry ()
  {
    CapturingTelemetry.uninstall ();
  }

  @Before
  public void clearRecordings ()
  {
    CT.reset ();
  }

  @Nullable
  private static CapturedMeasurement _findMeasurement (@NonNull final String sInstrument)
  {
    return CT.getFirstMeasurement (sInstrument);
  }

  @Test
  public void testSelectSuccess ()
  {
    assertEquals ("Hello", JPAEnabledManager.doSelectStatic (() -> "Hello").get ());

    assertEquals (1, CT.getSpanCount ());
    final CapturedSpan aSpan = CT.getSpans ().getFirstOrNull ();
    assertNotNull (aSpan);
    assertEquals (CDBTelemetry.SPAN_JPA_SELECT, aSpan.getName ());
    assertEquals (ETelemetrySpanKind.CLIENT, aSpan.getKind ());
    assertEquals (CDBTelemetry.COMPONENT_JPA, aSpan.getAttribute (CDBTelemetry.ATTR_COMPONENT));
    assertEquals (CDBTelemetry.JPA_OPERATION_SELECT, aSpan.getAttribute (CDBTelemetry.ATTR_JPA_OPERATION));
    assertEquals (Boolean.TRUE, aSpan.getAttribute (CDBTelemetry.ATTR_SUCCESS));
    assertNull (aSpan.getRecordedException ());
    assertTrue (aSpan.isClosed ());

    final CapturedMeasurement aOperations = _findMeasurement (CDBTelemetry.METRIC_JPA_OPERATIONS);
    assertNotNull (aOperations);
    assertEquals (1, aOperations.getValueAsLong ());
    assertEquals (CDBTelemetry.JPA_OPERATION_SELECT, aOperations.getAttribute (CDBTelemetry.ATTR_JPA_OPERATION));
    assertEquals (Boolean.TRUE, aOperations.getAttribute (CDBTelemetry.ATTR_SUCCESS));
    assertNotNull (_findMeasurement (CDBTelemetry.METRIC_CLIENT_OPERATION_DURATION));
  }

  @Test
  public void testSelectFailure ()
  {
    final JPAExecutionResult <String> aResult = JPAEnabledManager.doSelectStatic (() -> {
      throw new IllegalStateException ("Test exception");
    });
    assertTrue (aResult.isFailure ());

    final CapturedSpan aSpan = CT.getSpans ().getFirstOrNull ();
    assertNotNull (aSpan);
    assertEquals (Boolean.FALSE, aSpan.getAttribute (CDBTelemetry.ATTR_SUCCESS));
    assertEquals (IllegalStateException.class.getName (), aSpan.getAttribute (CDBTelemetry.ATTR_ERROR_TYPE));
    // The manager never rethrows, so the exception must be recorded explicitly
    assertNotNull (aSpan.getRecordedException ());

    final CapturedMeasurement aOperations = _findMeasurement (CDBTelemetry.METRIC_JPA_OPERATIONS);
    assertNotNull (aOperations);
    assertEquals (Boolean.FALSE, aOperations.getAttribute (CDBTelemetry.ATTR_SUCCESS));
    assertEquals (IllegalStateException.class.getName (), aOperations.getAttribute (CDBTelemetry.ATTR_ERROR_TYPE));
  }

  @Test
  public void testTelemetryDisabled ()
  {
    JPAEnabledManager.setTelemetryEnabled (false);
    try
    {
      assertEquals ("Hello", JPAEnabledManager.doSelectStatic (() -> "Hello").get ());

      assertEquals (0, CT.getSpanCount ());
      assertTrue (CT.getMeasurements ().isEmpty ());
    }
    finally
    {
      JPAEnabledManager.setTelemetryEnabled (JPAEnabledManager.DEFAULT_TELEMETRY);
    }
  }
}
