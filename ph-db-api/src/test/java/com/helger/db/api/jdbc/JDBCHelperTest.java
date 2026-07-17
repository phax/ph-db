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
package com.helger.db.api.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Test class for class {@link JDBCHelper}.
 *
 * @author Philip Helger
 */
public final class JDBCHelperTest
{
  @Test
  public void testGetMaskedConnectionString ()
  {
    // null and passwordless URLs are returned unchanged
    assertNull (JDBCHelper.getMaskedConnectionString (null));
    assertEquals ("", JDBCHelper.getMaskedConnectionString (""));
    assertEquals ("jdbc:h2:mem:test", JDBCHelper.getMaskedConnectionString ("jdbc:h2:mem:test"));
    assertEquals ("jdbc:mysql://localhost:3306/db?user=root",
                  JDBCHelper.getMaskedConnectionString ("jdbc:mysql://localhost:3306/db?user=root"));

    // Query style ('&' separated) - only the password value is masked, user name is kept
    assertEquals ("jdbc:mysql://localhost:3306/db?user=root&password=***",
                  JDBCHelper.getMaskedConnectionString ("jdbc:mysql://localhost:3306/db?user=root&password=secret"));
    assertEquals ("jdbc:mysql://localhost:3306/db?password=***&useSSL=true",
                  JDBCHelper.getMaskedConnectionString ("jdbc:mysql://localhost:3306/db?password=s3cr3t&useSSL=true"));

    // Property style (';' separated), as used by H2 and SQL Server
    assertEquals ("jdbc:h2:mem:test;USER=sa;PASSWORD=***",
                  JDBCHelper.getMaskedConnectionString ("jdbc:h2:mem:test;USER=sa;PASSWORD=secret"));
    assertEquals ("jdbc:h2:mem:test;PASSWORD=***;DB_CLOSE_DELAY=-1",
                  JDBCHelper.getMaskedConnectionString ("jdbc:h2:mem:test;PASSWORD=secret;DB_CLOSE_DELAY=-1"));

    // "pwd" alias is masked as well
    assertEquals ("jdbc:sqlserver://host;pwd=***",
                  JDBCHelper.getMaskedConnectionString ("jdbc:sqlserver://host;pwd=secret"));
  }
}
