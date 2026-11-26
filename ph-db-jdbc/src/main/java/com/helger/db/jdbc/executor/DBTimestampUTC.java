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
package com.helger.db.jdbc.executor;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.tostring.ToStringGenerator;

/**
 * A prepared statement parameter value for a SQL <code>TIMESTAMP</code> column (without time zone),
 * that is stored as UTC. {@link DBExecutor} binds it with
 * {@link java.sql.PreparedStatement#setTimestamp(int, Timestamp, Calendar)} and a UTC calendar, so
 * that the stored value neither depends on the default time zone of the JVM nor on daylight saving
 * time changes. Read the value back with {@link DBResultRow#getAsOffsetDateTimeUTC(int)}.<br>
 * This is needed, because not all databases support a <code>TIMESTAMP WITH TIME ZONE</code> type
 * (e.g. MySQL and DB2 LUW), and because a {@link Timestamp} bound with
 * {@link java.sql.PreparedStatement#setObject(int, Object)} is converted with the default time zone
 * of the JVM.
 *
 * @author Philip Helger
 * @since 8.5.4
 */
@Immutable
public final class DBTimestampUTC
{
  private final Instant m_aInstant;

  private DBTimestampUTC (@NonNull final Instant aInstant)
  {
    ValueEnforcer.notNull (aInstant, "Instant");
    m_aInstant = aInstant;
  }

  /**
   * @return The instant to be stored. Never <code>null</code>.
   */
  @NonNull
  public Instant getInstant ()
  {
    return m_aInstant;
  }

  /**
   * @return The instant as a {@link Timestamp}. Never <code>null</code>.
   */
  @NonNull
  public Timestamp getAsTimestamp ()
  {
    return Timestamp.from (m_aInstant);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final DBTimestampUTC rhs = (DBTimestampUTC) o;
    return m_aInstant.equals (rhs.m_aInstant);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aInstant).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("Instant", m_aInstant).getToString ();
  }

  /**
   * @return A new UTC calendar, as used for binding and reading. Calendar objects are not thread
   *         safe, so a new one is created for every call. Never <code>null</code>.
   */
  @NonNull
  public static Calendar createUTCCalendar ()
  {
    return Calendar.getInstance (TimeZone.getTimeZone (ZoneOffset.UTC), Locale.ROOT);
  }

  /**
   * Create a new value.
   *
   * @param aInstant
   *        The instant to be stored. May be <code>null</code>.
   * @return <code>null</code> if the parameter is <code>null</code>, so that SQL <code>NULL</code>
   *         is stored.
   */
  @Nullable
  public static DBTimestampUTC of (@Nullable final Instant aInstant)
  {
    return aInstant == null ? null : new DBTimestampUTC (aInstant);
  }

  /**
   * Create a new value.
   *
   * @param aODT
   *        The date time to be stored. The offset is not stored - only the instant. May be
   *        <code>null</code>.
   * @return <code>null</code> if the parameter is <code>null</code>, so that SQL <code>NULL</code>
   *         is stored.
   */
  @Nullable
  public static DBTimestampUTC of (@Nullable final OffsetDateTime aODT)
  {
    return aODT == null ? null : new DBTimestampUTC (aODT.toInstant ());
  }
}
