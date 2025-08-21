/*
 * Copyright (C) 2014-2025 Philip Helger (www.helger.com)
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
package com.helger.db.api.helper;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.datetime.xml.XMLOffsetDateTime;

import jakarta.annotation.Nullable;

/**
 * A common helper class that deals with values in databases. Focussing on types as well as on
 * length restrictions.
 *
 * @author Philip Helger
 * @since 6.7.3
 */
@Immutable
public final class DBValueHelper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DBValueHelper.class);

  private DBValueHelper ()
  {}

  @Nullable
  public static Time toTime (@Nullable final LocalTime aLT)
  {
    return aLT == null ? null : Time.valueOf (aLT);
  }

  @Nullable
  public static Date toDate (@Nullable final LocalDate aLD)
  {
    return aLD == null ? null : Date.valueOf (aLD);
  }

  @Nullable
  public static Timestamp toTimestamp (@Nullable final LocalDateTime aLDT)
  {
    return aLDT == null ? null : Timestamp.valueOf (aLDT);
  }

  @Nullable
  public static Timestamp toTimestamp (@Nullable final XMLOffsetDateTime aODT)
  {
    return aODT == null ? null : Timestamp.from (aODT.toInstant ());
  }

  @Nullable
  public static Timestamp toTimestamp (@Nullable final OffsetDateTime aODT)
  {
    return aODT == null ? null : Timestamp.from (aODT.toInstant ());
  }

  @Nullable
  public static Timestamp toTimestamp (@Nullable final ZonedDateTime aODT)
  {
    return aODT == null ? null : Timestamp.from (aODT.toInstant ());
  }

  @FunctionalInterface
  public interface ITrimmedToLengthCallback
  {
    void onTrim (int nExistingLength, int nTrimmedToLength);
  }

  @Nullable
  public static String getTrimmedToLength (@Nullable final String s, final int nMaxLengthIncl)
  {
    return getTrimmedToLength (s,
                               nMaxLengthIncl,
                               (m, n) -> LOGGER.warn ("Cutting value with length " + m + " to " + n + " for DB"));
  }

  @Nullable
  public static String getTrimmedToLength (@Nullable final String s,
                                           final int nMaxLengthIncl,
                                           @Nullable final ITrimmedToLengthCallback aCallback)
  {
    ValueEnforcer.isGT0 (nMaxLengthIncl, "MaxLengthIncl");
    if (s == null)
      return null;

    final int nLength = s.length ();
    if (nLength <= nMaxLengthIncl)
      return s;

    if (aCallback != null)
      aCallback.onTrim (nLength, nMaxLengthIncl);
    return s.substring (0, nMaxLengthIncl);
  }
}
