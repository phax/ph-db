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
package com.helger.db.api.flyway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;

/**
 * Test class for class {@link FlywayConfiguration}.
 *
 * @author Philip Helger
 */
public class FlywayConfigurationTest
{
  @Test
  public void testBasic ()
  {
    final FlywayConfiguration a = new FlywayConfiguration (false, "url", "usr", "pw", true, 0);
    assertFalse (a.isFlywayEnabled ());
    assertEquals ("url", a.getFlywayJdbcUrl ());
    assertEquals ("usr", a.getFlywayJdbcUser ());
    assertEquals ("pw", a.getFlywayJdbcPassword ());
    assertTrue (a.isFlywaySchemaCreate ());
    assertEquals (0, a.getFlywayBaselineVersion ());

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (a,
                                                                       new FlywayConfiguration (false,
                                                                                                "url",
                                                                                                "usr",
                                                                                                "pw",
                                                                                                true,
                                                                                                0));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (a,
                                                                           new FlywayConfiguration (true,
                                                                                                    "url",
                                                                                                    "usr",
                                                                                                    "pw",
                                                                                                    true,
                                                                                                    0));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (a,
                                                                           new FlywayConfiguration (false,
                                                                                                    "url2",
                                                                                                    "usr",
                                                                                                    "pw",
                                                                                                    true,
                                                                                                    0));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (a,
                                                                           new FlywayConfiguration (false,
                                                                                                    "url",
                                                                                                    "usr2",
                                                                                                    "pw",
                                                                                                    true,
                                                                                                    0));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (a,
                                                                           new FlywayConfiguration (false,
                                                                                                    "url",
                                                                                                    "usr",
                                                                                                    "pw2",
                                                                                                    true,
                                                                                                    0));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (a,
                                                                           new FlywayConfiguration (false,
                                                                                                    "url",
                                                                                                    "usr",
                                                                                                    "pw",
                                                                                                    false,
                                                                                                    0));
    CommonsTestHelper.testDefaultImplementationWithDifferentContentObject (a,
                                                                           new FlywayConfiguration (false,
                                                                                                    "url",
                                                                                                    "usr",
                                                                                                    "pw",
                                                                                                    true,
                                                                                                    1));
  }

  @Test
  public void testBuilder ()
  {
    // Use an empty builder
    final FlywayConfiguration a = FlywayConfiguration.builder ()
                                                     .enabled (true)
                                                     .jdbcUrl ("url")
                                                     .jdbcUser ("user")
                                                     .jdbcPassword ("pw")
                                                     .schemaCreate (false)
                                                     .baselineVersion (5)
                                                     .build ();
    assertTrue (a.isFlywayEnabled ());
    assertEquals ("url", a.getFlywayJdbcUrl ());
    assertEquals ("user", a.getFlywayJdbcUser ());
    assertEquals ("pw", a.getFlywayJdbcPassword ());
    assertFalse (a.isFlywaySchemaCreate ());
    assertEquals (5, a.getFlywayBaselineVersion ());

    // Create a copy via the builder
    final FlywayConfiguration a2 = FlywayConfiguration.builder (a).build ();
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (a, a2);
  }
}
