/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.db.jdbc;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.db.api.jdbc.JDBCHelper;

/**
 * Small class for safe SQL-as-usual methods.
 *
 * @author Philip Helger
 */
@Immutable
public final class JDBCExtHelper
{
  private JDBCExtHelper ()
  {}

  /**
   * Determine the JDBC type from the passed class. This extends
   * {@link JDBCHelper} with Joda time classes.
   *
   * @param aClass
   *        The class to check. May not be <code>null</code>.
   * @return {@link Types#JAVA_OBJECT} if the type could not be determined.
   * @see "http://java.sun.com/j2se/1.4.2/docs/guide/jdbc/getstart/mapping.html"
   */
  public static int getJDBCTypeFromClass (@Nonnull final Class <?> aClass)
  {
    ValueEnforcer.notNull (aClass, "Class");

    // Custom converters
    if (aClass.equals (ZonedDateTime.class))
      return Types.TIMESTAMP;

    if (aClass.equals (LocalDateTime.class))
      return Types.TIMESTAMP;

    if (aClass.equals (LocalDate.class))
      return Types.DATE;

    if (aClass.equals (LocalTime.class))
      return Types.TIME;

    return JDBCHelper.getJDBCTypeFromClass (aClass);
  }
}
