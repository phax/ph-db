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
package com.helger.db.jdbc.callback;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.string.ToStringGenerator;

/**
 * Special implementation of the {@link IGeneratedKeysCallback} especially for
 * retrieving a single created ID.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class GetSingleGeneratedKeyCallback implements IGeneratedKeysCallback
{
  private Object m_aGeneratedKey;

  public GetSingleGeneratedKeyCallback ()
  {}

  public void onGeneratedKeys (@Nonnull final ICommonsList <ICommonsList <Object>> aGeneratedValues)
  {
    ValueEnforcer.notNull (aGeneratedValues, "GeneratedValues");

    if (aGeneratedValues.size () != 1)
      throw new IllegalArgumentException ("Found not exactly 1 generated value row but " +
                                          aGeneratedValues.size () +
                                          " rows!");
    final ICommonsList <Object> aRow = aGeneratedValues.getFirstOrNull ();
    if (aRow.size () != 1)
      throw new IllegalArgumentException ("The generated row does not contain exactly 1 item but " +
                                          aRow.size () +
                                          " items!");
    m_aGeneratedKey = aRow.getFirstOrNull ();
  }

  @Nonnull
  public Object getGeneratedKey ()
  {
    if (m_aGeneratedKey == null)
      throw new IllegalStateException ("No generated key was determined!");
    return m_aGeneratedKey;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("GeneratedKey", m_aGeneratedKey).getToString ();
  }
}
