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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.lang.ICloneable;
import com.helger.commons.string.ToStringGenerator;

/**
 * A simple implementation of the {@link IPreparedStatementDataProvider} that
 * takes a list of objects and returns theses objects as they are.
 *
 * @author Philip Helger
 */
public final class ConstantPreparedStatementDataProvider implements
                                                         IPreparedStatementDataProvider,
                                                         ICloneable <ConstantPreparedStatementDataProvider>
{
  private final ICommonsList <Object> m_aValues;

  public ConstantPreparedStatementDataProvider ()
  {
    m_aValues = new CommonsArrayList <> ();
  }

  public ConstantPreparedStatementDataProvider (@Nonnull final Iterable <?> aValues)
  {
    m_aValues = new CommonsArrayList <> (aValues);
  }

  public ConstantPreparedStatementDataProvider (@Nonnull @Nonempty final Object... aValues)
  {
    m_aValues = new CommonsArrayList <> (aValues);
  }

  @Nonnull
  public ConstantPreparedStatementDataProvider addValue (@Nullable final Object aValue)
  {
    m_aValues.add (aValue);
    return this;
  }

  @Nonnegative
  public int getValueCount ()
  {
    return m_aValues.size ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsList <Object> getObjectValues ()
  {
    return m_aValues.getClone ();
  }

  @Nonnull
  @ReturnsMutableCopy
  public ConstantPreparedStatementDataProvider getClone ()
  {
    return new ConstantPreparedStatementDataProvider (m_aValues);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("values", m_aValues).getToString ();
  }
}
