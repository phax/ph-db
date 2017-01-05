/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.db.api.mysql;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.commons.collection.ext.CommonsLinkedHashMap;
import com.helger.commons.collection.ext.ICommonsOrderedMap;

/**
 * Test class for class {@link MySQLHelper}.
 *
 * @author Philip Helger
 */
public final class MySQLHelperTest
{
  @Test
  public void testBasic ()
  {
    assertEquals ("jdbc:mysql:a", MySQLHelper.buildJDBCString ("jdbc:mysql:a", null));
    final ICommonsOrderedMap <EMySQLConnectionProperty, String> aMap = new CommonsLinkedHashMap <> ();
    assertEquals ("jdbc:mysql:a", MySQLHelper.buildJDBCString ("jdbc:mysql:a", aMap));
    aMap.put (EMySQLConnectionProperty.autoDeserialize, "true");
    assertEquals ("jdbc:mysql:a?autoDeserialize=true", MySQLHelper.buildJDBCString ("jdbc:mysql:a", aMap));
    aMap.put (EMySQLConnectionProperty.autoSlowLog, "true");
    assertEquals ("jdbc:mysql:a?autoDeserialize=true&autoSlowLog=true",
                  MySQLHelper.buildJDBCString ("jdbc:mysql:a", aMap));
  }
}
