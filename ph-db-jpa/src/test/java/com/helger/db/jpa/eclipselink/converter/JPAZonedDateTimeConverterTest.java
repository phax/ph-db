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
package com.helger.db.jpa.eclipselink.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.helger.commons.datetime.PDTFactory;

/**
 * Test class for class {@link JPAZonedDateTimeConverter}.
 *
 * @author Philip Helger
 */
public final class JPAZonedDateTimeConverterTest
{
  @Test
  public void testAll ()
  {
    // Don't use named timezone - it will be lost
    // Fixed date, to avoid timezone change (e.g. CET - CEST)
    final ZonedDateTime aNow = PDTFactory.createZonedDateTime (2021, Month.JANUARY, 1)
                                         .withZoneSameInstant (ZoneOffset.ofHours (1));
    final JPAZonedDateTimeConverter aConverter = new JPAZonedDateTimeConverter ();
    final Timestamp aDataValue = aConverter.convertObjectValueToDataValue (aNow, null);
    assertNotNull (aDataValue);
    assertEquals (PDTFactory.createLocalDate (2021, Month.JANUARY, 1).atStartOfDay (), aDataValue.toLocalDateTime ());

    // Must be the same Instant - TZ info might differ
    assertEquals (aNow.toInstant (), aConverter.convertDataValueToObjectValue (aDataValue, null).toInstant ());
  }
}
