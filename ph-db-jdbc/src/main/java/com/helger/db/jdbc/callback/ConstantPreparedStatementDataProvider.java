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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.Nonnegative;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.clone.ICloneable;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

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

  public ConstantPreparedStatementDataProvider (@NonNull final Iterable <?> aValues)
  {
    m_aValues = new CommonsArrayList <> (aValues);
  }

  public ConstantPreparedStatementDataProvider (@NonNull @Nonempty final Object... aValues)
  {
    m_aValues = new CommonsArrayList <> (aValues);
  }

  @NonNull
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

  @NonNull
  @ReturnsMutableCopy
  public ICommonsList <Object> getObjectValues ()
  {
    return m_aValues.getClone ();
  }

  @NonNull
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
