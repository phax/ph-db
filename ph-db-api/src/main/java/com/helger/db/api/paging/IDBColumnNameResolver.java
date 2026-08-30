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

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsMap;

/**
 * Resolve the logical field name of a
 * {@link com.helger.collection.paging.SortField} onto the SQL column expression to be used in the
 * <code>ORDER BY</code> clause.<br>
 * <b>This interface is the security boundary of the whole sorting.</b> The field names usually
 * originate from a UI and are therefore attacker controlled, whereas the returned column expression
 * ends up in the SQL statement verbatim - it cannot be a JDBC parameter. An implementation must
 * therefore only ever return expressions that are hard coded in the application, and it must return
 * <code>null</code> for everything it does not know. Never implement this as an identity function
 * or by escaping the provided value.
 *
 * @author Philip Helger
 * @since 8.4.2
 * @see DBPagingHelper
 */
@FunctionalInterface
public interface IDBColumnNameResolver
{
  /**
   * A resolver that knows no field at all, so that no sorting is ever applied.
   */
  IDBColumnNameResolver NONE = sFieldName -> null;

  /**
   * Get the SQL column expressions to sort by, for the provided logical field name. More than one
   * column may be returned, because a logical field may be stored in multiple columns - like a
   * participant identifier that consists of a scheme and a value column. The sort order is applied
   * to every returned column.
   *
   * @param sFieldName
   *        The logical field name to resolve. Neither <code>null</code> nor empty. Must be treated
   *        as untrusted input.
   * @return The SQL column expressions to be used in the <code>ORDER BY</code> clause, in the order
   *         of precedence. Return <code>null</code> or an empty list if the field name is unknown,
   *         in which case it is ignored.
   */
  @Nullable
  ICommonsList <String> getAllSQLColumnNames (@NonNull @Nonempty String sFieldName);

  /**
   * Create a resolver based on a fixed map from logical field name to a single SQL column
   * expression. This is the recommended way to implement this interface, because the map is
   * inherently a whitelist.
   *
   * @param aFieldNameToColumnName
   *        The map to use. May not be <code>null</code>. It is copied, so later modifications of
   *        the provided map have no effect.
   * @return Never <code>null</code>.
   */
  @NonNull
  static IDBColumnNameResolver createFromMap (@NonNull final Map <String, String> aFieldNameToColumnName)
  {
    ValueEnforcer.notNullNoNullValue (aFieldNameToColumnName, "FieldNameToColumnName");

    final ICommonsMap <String, ICommonsList <String>> aMap = new CommonsHashMap <> ();
    for (final Map.Entry <String, String> aEntry : aFieldNameToColumnName.entrySet ())
      aMap.put (aEntry.getKey (), new CommonsArrayList <> (aEntry.getValue ()));
    return aMap::get;
  }

  /**
   * Create a resolver based on a fixed map from logical field name to a list of SQL column
   * expressions.
   *
   * @param aFieldNameToColumnNames
   *        The map to use. May not be <code>null</code>. It is copied, so later modifications of
   *        the provided map have no effect.
   * @return Never <code>null</code>.
   */
  @NonNull
  static IDBColumnNameResolver createFromMultiMap (@NonNull final Map <String, ? extends List <String>> aFieldNameToColumnNames)
  {
    ValueEnforcer.notNullNoNullValue (aFieldNameToColumnNames, "FieldNameToColumnNames");

    final ICommonsMap <String, ICommonsList <String>> aMap = new CommonsHashMap <> ();
    for (final Map.Entry <String, ? extends List <String>> aEntry : aFieldNameToColumnNames.entrySet ())
      aMap.put (aEntry.getKey (), new CommonsArrayList <> (aEntry.getValue ()));
    return aMap::get;
  }
}
