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

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.string.StringHelper;
import com.helger.collection.paging.IPagingSpec;
import com.helger.collection.paging.SortField;
import com.helger.db.api.EDatabaseSystemType;

/**
 * Create the database system specific SQL clauses to sort a query and to limit it to a single
 * "page" of results.
 *
 * @author Philip Helger
 * @since 8.4.2
 */
@Immutable
public final class DBPagingHelper
{
  /**
   * MySQL has no syntax for "all rows starting at a certain offset", so the documented workaround
   * of using a very large row count is applied. See
   * https://dev.mysql.com/doc/refman/8.4/en/select.html
   */
  public static final String MYSQL_ALL_ROWS = "18446744073709551615";

  private static final Logger LOGGER = LoggerFactory.getLogger (DBPagingHelper.class);

  private DBPagingHelper ()
  {}

  /**
   * Create the <code>ORDER BY</code> clause for the sort fields of the provided paging
   * specification. Sort fields that the resolver does not know are skipped.
   *
   * @param aPagingSpec
   *        The paging specification to use. May not be <code>null</code>.
   * @param aColumnNameResolver
   *        The resolver from logical field name to SQL column expression. May not be
   *        <code>null</code>. See {@link IDBColumnNameResolver} for the security implications.
   * @return The SQL clause to be appended to the query, starting with a blank. Never
   *         <code>null</code> but empty if no sort field could be resolved.
   */
  @NonNull
  public static String getOrderByClause (@NonNull final IPagingSpec aPagingSpec,
                                         @NonNull final IDBColumnNameResolver aColumnNameResolver)
  {
    ValueEnforcer.notNull (aPagingSpec, "PagingSpec");
    ValueEnforcer.notNull (aColumnNameResolver, "ColumnNameResolver");

    final StringBuilder aSB = new StringBuilder ();
    for (final SortField aSortField : aPagingSpec.getAllSortFields ())
    {
      final String sFieldName = aSortField.getFieldName ();
      final String sColumnName = aColumnNameResolver.getSQLColumnName (sFieldName);
      if (StringHelper.isEmpty (sColumnName))
      {
        // This is the expected way to reject an unknown - and possibly forged - field name
        LOGGER.warn ("The sort field name '" + sFieldName + "' cannot be resolved to an SQL column and is ignored");
        continue;
      }

      if (aSB.length () == 0)
        aSB.append (" ORDER BY ");
      else
        aSB.append (", ");
      aSB.append (sColumnName).append (aSortField.isAscending () ? " ASC" : " DESC");
    }
    return aSB.toString ();
  }

  /**
   * Create the database system specific clause to limit a query to a single "page" of results. The
   * query must contain an <code>ORDER BY</code> clause, because only a deterministic order
   * guarantees that consecutive page requests return disjunct results. MS SQL Server additionally
   * rejects the created clause if no <code>ORDER BY</code> is present.
   *
   * @param eDBType
   *        The database system to create the clause for. May not be <code>null</code>.
   * @param aPagingSpec
   *        The paging specification to use. May not be <code>null</code>. It may not be an "empty
   *        page" - see below.
   * @return The SQL clause to be appended to the query, starting with a blank. Never
   *         <code>null</code> but empty if all rows are requested.
   * @throws IllegalArgumentException
   *         If the paging specification requests 0 rows. There is no portable SQL for that, and a
   *         query that can never return a row should not be executed at all - handle
   *         {@link IPagingSpec#isEmptyPage()} in the calling code instead.
   */
  @NonNull
  public static String getPagingClause (@NonNull final EDatabaseSystemType eDBType,
                                        @NonNull final IPagingSpec aPagingSpec)
  {
    ValueEnforcer.notNull (eDBType, "DBType");
    ValueEnforcer.notNull (aPagingSpec, "PagingSpec");
    if (aPagingSpec.isEmptyPage ())
      throw new IllegalArgumentException ("A paging specification with a maximum count of 0 cannot be expressed in SQL. Don't run the query at all.");

    final long nStartIndex = aPagingSpec.getStartIndex ();
    final boolean bUnlimited = aPagingSpec.isUnlimited ();

    // All rows, starting at the very beginning - nothing to add
    if (bUnlimited && nStartIndex == 0)
      return "";

    return switch (eDBType)
    {
      // MySQL does not support the SQL standard "OFFSET .. FETCH" syntax
      case MYSQL -> " LIMIT " +
                    (bUnlimited ? MYSQL_ALL_ROWS : Long.toString (aPagingSpec.getMaxCount ())) +
                    " OFFSET " +
                    nStartIndex;
      // DB2, H2, Oracle, PostgreSQL and SQL Server all support the SQL standard
      default -> " OFFSET " +
                 nStartIndex +
                 " ROWS" +
                 (bUnlimited ? "" : " FETCH NEXT " + aPagingSpec.getMaxCount () + " ROWS ONLY");
    };
  }

  /**
   * Create the combined <code>ORDER BY</code> and paging clause. This is the method to be used in
   * practice, because the two clauses belong together - paging without a deterministic order
   * returns arbitrary rows.
   *
   * @param eDBType
   *        The database system to create the clause for. May not be <code>null</code>.
   * @param aPagingSpec
   *        The paging specification to use. May not be <code>null</code>.
   * @param aColumnNameResolver
   *        The resolver from logical field name to SQL column expression. May not be
   *        <code>null</code>.
   * @return The SQL clause to be appended to the query, starting with a blank. Never
   *         <code>null</code> but maybe empty.
   * @see #getOrderByClause(IPagingSpec, IDBColumnNameResolver)
   * @see #getPagingClause(EDatabaseSystemType, IPagingSpec)
   */
  @NonNull
  public static String getOrderByAndPagingClause (@NonNull final EDatabaseSystemType eDBType,
                                                  @NonNull final IPagingSpec aPagingSpec,
                                                  @NonNull final IDBColumnNameResolver aColumnNameResolver)
  {
    final String sOrderBy = getOrderByClause (aPagingSpec, aColumnNameResolver);
    final String sPaging = getPagingClause (eDBType, aPagingSpec);

    if (sPaging.length () > 0 && sOrderBy.length () == 0)
      LOGGER.warn ("Creating an SQL paging clause for " +
                   eDBType.getDisplayName () +
                   " without an ORDER BY clause. Which rows a page contains is undefined that way, so consecutive pages may overlap or lose rows.");

    return sOrderBy + sPaging;
  }
}
