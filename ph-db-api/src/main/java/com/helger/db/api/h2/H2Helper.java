/*
 * Copyright (C) 2014-2024 Philip Helger (www.helger.com)
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
package com.helger.db.api.h2;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.db.api.CJDBC_H2;

/**
 * H2 helper methods
 *
 * @author Philip Helger
 */
@Immutable
public final class H2Helper
{
  private H2Helper ()
  {}

  /**
   * Build the final connection string from the base JDBC URL and an optional
   * set of connection properties.
   *
   * @param sJdbcURL
   *        The base JDBC URL. May neither be <code>null</code> nor empty and
   *        must started with {@link CJDBC_H2#CONNECTION_PREFIX}
   * @param aConnectionProperties
   *        An optional map with connection properties. May be <code>null</code>
   *        .
   * @return The final JDBC connection string to be used. Never
   *         <code>null</code> or empty
   */
  @Nonnull
  @Nonempty
  public static String buildJDBCString (@Nonnull @Nonempty final String sJdbcURL,
                                        @Nullable final Map <String, String> aConnectionProperties)
  {
    ValueEnforcer.notEmpty (sJdbcURL, "JDBC URL");
    ValueEnforcer.isTrue (sJdbcURL.startsWith (CJDBC_H2.CONNECTION_PREFIX),
                          "The JDBC URL '" + sJdbcURL + "' does not seem to be a H2 connection string!");

    // Add the connection properties to the JDBC string
    final StringBuilder aSB = new StringBuilder (sJdbcURL);
    if (aConnectionProperties != null)
      for (final Map.Entry <String, String> aEntry : aConnectionProperties.entrySet ())
        aSB.append (';').append (aEntry.getKey ()).append ('=').append (aEntry.getValue ());
    return aSB.toString ();
  }
}
