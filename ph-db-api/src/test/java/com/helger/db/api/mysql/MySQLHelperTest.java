package com.helger.db.api.mysql;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

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
    final Map <EMySQLConnectionProperty, String> aMap = new LinkedHashMap <> ();
    assertEquals ("jdbc:mysql:a", MySQLHelper.buildJDBCString ("jdbc:mysql:a", aMap));
    aMap.put (EMySQLConnectionProperty.autoDeserialize, "true");
    assertEquals ("jdbc:mysql:a?autoDeserialize=true", MySQLHelper.buildJDBCString ("jdbc:mysql:a", aMap));
    aMap.put (EMySQLConnectionProperty.autoSlowLog, "true");
    assertEquals ("jdbc:mysql:a?autoDeserialize=true&autoSlowLog=true",
                  MySQLHelper.buildJDBCString ("jdbc:mysql:a", aMap));
  }
}
