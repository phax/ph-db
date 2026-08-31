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
package com.helger.db.api.paging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.collection.paging.IPagingSpec;
import com.helger.collection.paging.PagingSpec;
import com.helger.collection.paging.SortField;
import com.helger.db.api.EDatabaseSystemType;

/**
 * Test class for class {@link DBPagingHelper}.
 *
 * @author Philip Helger
 */
public final class DBPagingHelperTest
{
  private static final IDBColumnNameResolver RESOLVER;
  static
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("id", "sg.id");
    aMap.put ("name", "sg.name");
    RESOLVER = IDBColumnNameResolver.createFromMap (aMap);
  }

  @Test
  public void testPagingClauseMySQL ()
  {
    assertEquals (" LIMIT 25 OFFSET 50",
                  DBPagingHelper.getPagingClause (EDatabaseSystemType.MYSQL, new PagingSpec (50, 25)));
    assertEquals (" LIMIT 25 OFFSET 0",
                  DBPagingHelper.getPagingClause (EDatabaseSystemType.MYSQL, new PagingSpec (0, 25)));

    // All rows from the very beginning - no clause at all
    assertEquals ("", DBPagingHelper.getPagingClause (EDatabaseSystemType.MYSQL, PagingSpec.UNLIMITED));

    // All rows from an offset - MySQL needs the documented workaround
    assertEquals (" LIMIT " + DBPagingHelper.MYSQL_ALL_ROWS + " OFFSET 50",
                  DBPagingHelper.getPagingClause (EDatabaseSystemType.MYSQL, new PagingSpec (50, -1)));
  }

  @Test
  public void testPagingClauseStandard ()
  {
    for (final EDatabaseSystemType eDBType : new EDatabaseSystemType [] { EDatabaseSystemType.DB2,
                                                                          EDatabaseSystemType.H2,
                                                                          EDatabaseSystemType.ORACLE,
                                                                          EDatabaseSystemType.POSTGRESQL,
                                                                          EDatabaseSystemType.SQLSERVER })
    {
      assertEquals (" OFFSET 50 ROWS FETCH NEXT 25 ROWS ONLY",
                    DBPagingHelper.getPagingClause (eDBType, new PagingSpec (50, 25)));
      assertEquals (" OFFSET 0 ROWS FETCH NEXT 25 ROWS ONLY",
                    DBPagingHelper.getPagingClause (eDBType, new PagingSpec (0, 25)));
      assertEquals ("", DBPagingHelper.getPagingClause (eDBType, PagingSpec.UNLIMITED));
      assertEquals (" OFFSET 50 ROWS", DBPagingHelper.getPagingClause (eDBType, new PagingSpec (50, -1)));
    }
  }

  @Test
  public void testPagingClauseEmptyPage ()
  {
    // 0 rows cannot be expressed in SQL
    try
    {
      DBPagingHelper.getPagingClause (EDatabaseSystemType.MYSQL, new PagingSpec (0, 0));
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {
      // expected
    }
  }

  @Test
  public void testOrderByClause ()
  {
    assertEquals (" ORDER BY sg.id ASC",
                  DBPagingHelper.getOrderByClause (new PagingSpec (0, 25, SortField.ascending ("id")), RESOLVER));
    assertEquals (" ORDER BY sg.name DESC, sg.id ASC",
                  DBPagingHelper.getOrderByClause (new PagingSpec (0,
                                                                   25,
                                                                   SortField.descending ("name"),
                                                                   SortField.ascending ("id")),
                                                   RESOLVER));

    // No sort field at all
    assertEquals ("", DBPagingHelper.getOrderByClause (new PagingSpec (0, 25), RESOLVER));
  }

  @Test
  public void testOrderByClauseIgnoresUnknownFields ()
  {
    // A forged field name must never reach the SQL
    final IPagingSpec aSpec = new PagingSpec (0,
                                              25,
                                              SortField.ascending ("id; DROP TABLE smp_service_group"),
                                              SortField.ascending ("name"));
    assertEquals (" ORDER BY sg.name ASC", DBPagingHelper.getOrderByClause (aSpec, RESOLVER));

    // Everything is unknown
    assertEquals ("", DBPagingHelper.getOrderByClause (aSpec, IDBColumnNameResolver.NONE));
  }

  @Test
  public void testOrderByClauseWithCompositeKey ()
  {
    // One logical field that is stored in two columns - the sort order applies to both
    final ICommonsMap <String, ICommonsList <String>> aMap = new CommonsHashMap <> ();
    aMap.put ("participantid", new CommonsArrayList <> ("sg.scheme", "sg.value"));
    final IDBColumnNameResolver aResolver = IDBColumnNameResolver.createFromMultiMap (aMap);

    assertEquals (" ORDER BY sg.scheme DESC, sg.value DESC",
                  DBPagingHelper.getOrderByClause (new PagingSpec (0, 25, SortField.descending ("participantid")),
                                                   aResolver));
  }

  @Test
  public void testOrderByClauseWithDefaults ()
  {
    final ICommonsList <SortField> aDefault = new CommonsArrayList <> (SortField.ascending ("name"),
                                                                       SortField.descending ("id"));

    // Nothing requested - the default is used
    assertEquals (" ORDER BY sg.name ASC, sg.id DESC",
                  DBPagingHelper.getOrderByClause (new PagingSpec (0, 25), RESOLVER, aDefault));

    // Nothing resolvable - the default is used as well
    assertEquals (" ORDER BY sg.name ASC, sg.id DESC",
                  DBPagingHelper.getOrderByClause (new PagingSpec (0, 25, SortField.ascending ("unknown")),
                                                   RESOLVER,
                                                   aDefault));

    // A requested order always wins over the default
    assertEquals (" ORDER BY sg.id ASC",
                  DBPagingHelper.getOrderByClause (new PagingSpec (0, 25, SortField.ascending ("id")),
                                                   RESOLVER,
                                                   aDefault));

    // Without a default the clause stays empty
    assertEquals ("", DBPagingHelper.getOrderByClause (new PagingSpec (0, 25), RESOLVER, null));
    assertEquals ("", DBPagingHelper.getOrderByClause (new PagingSpec (0, 25), RESOLVER));

    // An unresolvable default is ignored - it cannot create an invalid statement
    assertEquals ("",
                  DBPagingHelper.getOrderByClause (new PagingSpec (0, 25),
                                                   RESOLVER,
                                                   new CommonsArrayList <> (SortField.ascending ("unknown"))));
  }

  @Test
  public void testOrderByAndPagingClauseWithDefaults ()
  {
    // The default order makes the paging deterministic
    assertEquals (" ORDER BY sg.id ASC LIMIT 25 OFFSET 50",
                  DBPagingHelper.getOrderByAndPagingClause (EDatabaseSystemType.MYSQL,
                                                            new PagingSpec (50, 25),
                                                            RESOLVER,
                                                            new CommonsArrayList <> (SortField.ascending ("id"))));
  }

  @Test
  public void testOrderByAndPagingClause ()
  {
    assertEquals (" ORDER BY sg.id ASC LIMIT 25 OFFSET 50",
                  DBPagingHelper.getOrderByAndPagingClause (EDatabaseSystemType.MYSQL,
                                                            new PagingSpec (50, 25, SortField.ascending ("id")),
                                                            RESOLVER));
    assertEquals (" ORDER BY sg.id ASC OFFSET 50 ROWS FETCH NEXT 25 ROWS ONLY",
                  DBPagingHelper.getOrderByAndPagingClause (EDatabaseSystemType.POSTGRESQL,
                                                            new PagingSpec (50, 25, SortField.ascending ("id")),
                                                            RESOLVER));

    // Paging without ordering is possible, but only logs a warning
    assertEquals (" LIMIT 25 OFFSET 50",
                  DBPagingHelper.getOrderByAndPagingClause (EDatabaseSystemType.MYSQL,
                                                            new PagingSpec (50, 25),
                                                            RESOLVER));

    // Neither ordering nor paging
    assertEquals ("",
                  DBPagingHelper.getOrderByAndPagingClause (EDatabaseSystemType.MYSQL,
                                                            PagingSpec.UNLIMITED,
                                                            RESOLVER));
  }

  @Test
  public void testResolverFromMap ()
  {
    final ICommonsMap <String, String> aMap = new CommonsHashMap <> ();
    aMap.put ("id", "sg.id");
    final IDBColumnNameResolver aResolver = IDBColumnNameResolver.createFromMap (aMap);
    assertEquals (new CommonsArrayList <> ("sg.id"), aResolver.getAllSQLColumnNames ("id"));

    // Later modifications of the source map must have no effect
    aMap.put ("name", "sg.name");
    assertNull (aResolver.getAllSQLColumnNames ("name"));
  }
}
