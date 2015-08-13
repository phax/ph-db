/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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

import java.sql.Date;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.helger.datetime.PDTFactory;

/**
 * Test class for class {@link JPAJodaLocalDateConverter}.
 * 
 * @author Philip Helger
 */
public final class JPAJodaLocalDateConverterTest
{
  @Test
  public void testAll ()
  {
    final LocalDate aNow = PDTFactory.getCurrentLocalDate ();
    final JPAJodaLocalDateConverter aConverter = new JPAJodaLocalDateConverter ();
    final Date aDataValue = aConverter.convertObjectValueToDataValue (aNow, null);
    assertNotNull (aDataValue);
    assertEquals (aNow, aConverter.convertDataValueToObjectValue (aDataValue, null));
  }
}
