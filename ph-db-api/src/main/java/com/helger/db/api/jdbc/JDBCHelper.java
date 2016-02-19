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
package com.helger.db.api.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.state.ESuccess;

/**
 * Small class for safe SQL-as-usual methods.
 *
 * @author Philip Helger
 */
@Immutable
public final class JDBCHelper
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (JDBCHelper.class);

  private JDBCHelper ()
  {}

  @Nonnull
  public static ESuccess commit (@Nullable final Connection aConnection)
  {
    if (aConnection != null)
    {
      try
      {
        if (!aConnection.isClosed ())
        {
          if (!aConnection.getAutoCommit ())
            aConnection.commit ();
          return ESuccess.SUCCESS;
        }
      }
      catch (final SQLException ex)
      {
        s_aLogger.warn ("Error committing connection " + aConnection, ex);
      }
    }
    return ESuccess.FAILURE;
  }

  public static void rollback (@Nullable final Connection aConnection)
  {
    if (aConnection != null)
    {
      try
      {
        if (!aConnection.isClosed ())
          if (!aConnection.getAutoCommit ())
            aConnection.rollback ();
      }
      catch (final SQLException ex)
      {
        s_aLogger.warn ("Error rolling back connection " + aConnection, ex);
      }
    }
  }

  public static void close (@Nullable final Connection aConnection)
  {
    if (aConnection != null)
    {
      try
      {
        if (!aConnection.isClosed ())
          aConnection.close ();
      }
      catch (final SQLException ex)
      {
        s_aLogger.warn ("Error closing connection " + aConnection, ex);
      }
    }
  }

  /**
   * Determine the JDBC type from the passed class.
   *
   * @param aClass
   *        The class to check. May not be <code>null</code>.
   * @return {@link Types#JAVA_OBJECT} if the type could not be determined.
   * @see "http://java.sun.com/j2se/1.4.2/docs/guide/jdbc/getstart/mapping.html"
   */
  public static int getJDBCTypeFromClass (@Nonnull final Class <?> aClass)
  {
    ValueEnforcer.notNull (aClass, "Class");

    if (!ClassHelper.isArrayClass (aClass))
    {
      // CHAR, VARCHAR or LONGVARCHAR
      if (aClass.equals (String.class))
        return Types.VARCHAR;

      if (aClass.equals (BigDecimal.class))
        return Types.NUMERIC;

      // BIT or BOOLEAN
      if (aClass.equals (Boolean.class) || aClass.equals (boolean.class))
        return Types.BOOLEAN;

      if (aClass.equals (Byte.class) || aClass.equals (byte.class))
        return Types.TINYINT;

      if (aClass.equals (Character.class) || aClass.equals (char.class))
        return Types.CHAR;

      if (aClass.equals (Double.class) || aClass.equals (double.class))
        return Types.DOUBLE;

      if (aClass.equals (Float.class) || aClass.equals (float.class))
        return Types.REAL;

      if (aClass.equals (Integer.class) || aClass.equals (int.class))
        return Types.INTEGER;

      if (aClass.equals (Long.class) || aClass.equals (long.class))
        return Types.BIGINT;

      if (aClass.equals (Short.class) || aClass.equals (short.class))
        return Types.SMALLINT;

      if (aClass.equals (java.sql.Date.class))
        return Types.DATE;

      if (aClass.equals (java.sql.Time.class))
        return Types.TIME;

      if (aClass.equals (java.sql.Timestamp.class))
        return Types.TIMESTAMP;

      if (aClass.equals (java.time.ZonedDateTime.class))
        return Types.TIMESTAMP;

      if (aClass.equals (java.time.OffsetDateTime.class))
        return Types.TIMESTAMP;

      if (aClass.equals (java.time.LocalDateTime.class))
        return Types.TIMESTAMP;

      if (aClass.equals (java.time.LocalDate.class))
        return Types.DATE;

      if (aClass.equals (java.time.LocalTime.class))
        return Types.TIME;
    }
    else
    {
      final Class <?> aComponentType = aClass.getComponentType ();
      if (aComponentType.equals (byte.class))
        return Types.VARBINARY;
    }

    s_aLogger.warn ("Failed to resolve JDBC type from class " + aClass.getName ());
    return Types.JAVA_OBJECT;
  }

  @Nullable
  public static String getJDBCTypeName (final int nType)
  {
    switch (nType)
    {
      case Types.BIT:
        return "BIT";
      case Types.TINYINT:
        return "TINYINT";
      case Types.SMALLINT:
        return "SMALLINT";
      case Types.INTEGER:
        return "INTEGER";
      case Types.BIGINT:
        return "BIGINT";
      case Types.FLOAT:
        return "FLOAT";
      case Types.REAL:
        return "REAL";
      case Types.DOUBLE:
        return "DOUBLE";
      case Types.NUMERIC:
        return "NUMERIC";
      case Types.DECIMAL:
        return "DECIMAL";
      case Types.CHAR:
        return "CHAR";
      case Types.VARCHAR:
        return "VARCHAR";
      case Types.LONGVARCHAR:
        return "LONGVARCHAR";
      case Types.DATE:
        return "DATE";
      case Types.TIME:
        return "TIME";
      case Types.TIMESTAMP:
        return "TIMESTAMP";
      case Types.BINARY:
        return "BINARY";
      case Types.VARBINARY:
        return "VARBINARY";
      case Types.LONGVARBINARY:
        return "LONGVARBINARY";
      case Types.NULL:
        return "NULL";
      case Types.OTHER:
        return "OTHER";
      case Types.JAVA_OBJECT:
        return "JAVA_OBJECT";
      case Types.DISTINCT:
        return "DISTINCT";
      case Types.STRUCT:
        return "STRUCT";
      case Types.ARRAY:
        return "ARRAY";
      case Types.BLOB:
        return "BLOB";
      case Types.CLOB:
        return "CLOB";
      case Types.REF:
        return "REF";
      case Types.DATALINK:
        return "DATALINK";
      case Types.BOOLEAN:
        return "BOOLEAN";
      // JDBC 4.0
      case Types.ROWID:
        return "ROWID";
      case Types.NCHAR:
        return "NCHAR";
      case Types.NVARCHAR:
        return "NVARCHAR";
      case Types.LONGNVARCHAR:
        return "LONGNVARCHAR";
      case Types.NCLOB:
        return "NCLOB";
      case Types.SQLXML:
        return "SQLXML";
      // JDBC 4.2
      case Types.REF_CURSOR:
        return "REF_CURSOR";
      case Types.TIME_WITH_TIMEZONE:
        return "TIME_WITH_TIMEZONE";
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return "TIMESTAMP_WITH_TIMEZONE";
    }
    s_aLogger.warn ("Unsupported JDBC type " + nType);
    return null;
  }
}
