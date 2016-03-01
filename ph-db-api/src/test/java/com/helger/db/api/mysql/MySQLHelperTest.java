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
