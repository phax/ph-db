/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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
import java.time.OffsetDateTime;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.typeconvert.TypeConverter;

/**
 * Test class for class {@link JPAOffsetDateTimeConverter}.
 *
 * @author Philip Helger
 */
public final class JPAOffsetDateTimeConverterTest
{

  private static final Logger LOGGER = LoggerFactory.getLogger (JPAOffsetDateTimeConverterTest.class);

  @Test
  public void testAll ()
  {
    final OffsetDateTime aNow = PDTFactory.getCurrentOffsetDateTimeMillisOnly ();

    LOGGER.info ("OffsetDateTime: " + aNow);
    final java.util.Date aDate = TypeConverter.convert (aNow, java.util.Date.class);
    LOGGER.info ("Date: " + aDate);
    final OffsetDateTime aNow2 = TypeConverter.convert (aDate, OffsetDateTime.class);
    LOGGER.info ("OffsetDateTime 2: " + aNow2);

    final JPAOffsetDateTimeConverter aConverter = new JPAOffsetDateTimeConverter ();
    final Timestamp aDataValue = aConverter.convertObjectValueToDataValue (aNow, null);
    assertNotNull (aDataValue);
    assertEquals (aNow, aConverter.convertDataValueToObjectValue (aDataValue, null));
  }
}
