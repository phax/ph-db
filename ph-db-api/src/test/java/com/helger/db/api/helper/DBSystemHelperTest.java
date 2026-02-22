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
package com.helger.db.api.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.db.api.EDatabaseSystemType;

/**
 * Test class for class {@link DBSystemHelper}.
 *
 * @author Philip Helger
 */
public class DBSystemHelperTest
{
  @Test
  public void testGetTableNamePrefix ()
  {
    for (final EDatabaseSystemType eType : EDatabaseSystemType.values ())
    {
      assertEquals ("", DBSystemHelper.getTableNamePrefix (eType, null));
      assertEquals ("", DBSystemHelper.getTableNamePrefix (eType, ""));
      assertEquals ("", DBSystemHelper.getTableNamePrefix (eType, "           "));
    }

    assertEquals ("\"abc\".", DBSystemHelper.getTableNamePrefix (EDatabaseSystemType.DB2, "abc"));
    assertEquals ("\"abc\".", DBSystemHelper.getTableNamePrefix (EDatabaseSystemType.H2, "abc"));
    assertEquals ("`abc`.", DBSystemHelper.getTableNamePrefix (EDatabaseSystemType.MYSQL, "abc"));
    assertEquals ("\"abc\".", DBSystemHelper.getTableNamePrefix (EDatabaseSystemType.ORACLE, "abc"));
    assertEquals ("\"abc\".", DBSystemHelper.getTableNamePrefix (EDatabaseSystemType.POSTGRESQL, "abc"));
    assertEquals ("\"abc\".", DBSystemHelper.getTableNamePrefix (EDatabaseSystemType.SQLSERVER, "abc"));
  }
}
