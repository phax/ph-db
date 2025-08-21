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
package com.helger.db.jpa;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.hashcode.HashCodeGenerator;
import com.helger.base.lang.clazz.ClassHelper;
import com.helger.base.state.ESuccess;
import com.helger.base.state.ISuccessIndicator;
import com.helger.base.state.SuccessWithValue;
import com.helger.base.tostring.ToStringGenerator;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Represents the result of a single transaction/select within this module. It
 * consists of 3 total fields:
 * <ul>
 * <li>Success/Failure</li>
 * <li>Return object - mostly in case of success</li>
 * <li>Exception - mostly in case of error</li>
 * </ul>
 *
 * @author Philip Helger
 * @param <DATATYPE>
 *        Main object value type.
 */
@Immutable
public class JPAExecutionResult <DATATYPE> extends SuccessWithValue <DATATYPE>
{
  private final Exception m_aException;

  public JPAExecutionResult (@Nonnull final ISuccessIndicator aSuccessIndicator,
                             @Nullable final DATATYPE aObj,
                             @Nullable final Exception aException)
  {
    super (aSuccessIndicator, aObj);
    m_aException = aException;
  }

  /**
   * @return The exception passed in the constructor. May be <code>null</code>.
   * @see #hasException()
   */
  @Nullable
  public Exception getException ()
  {
    return m_aException;
  }

  /**
   * @return <code>true</code> if an exception is present, <code>false</code> if
   *         not.
   * @see #getException()
   */
  public boolean hasException ()
  {
    return m_aException != null;
  }

  /**
   * @return The supplied value.
   * @throws Exception
   *         if an Exception is present
   * @see #get()
   * @see #hasException()
   * @see #getException()
   */
  @Nullable
  public DATATYPE getOrThrow () throws Exception
  {
    if (hasException ())
      throw getException ();
    return get ();
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (!super.equals (o))
      return false;
    final JPAExecutionResult <?> rhs = (JPAExecutionResult <?>) o;
    return EqualsHelper.equals (ClassHelper.getSafeClassName (m_aException), ClassHelper.getSafeClassName (rhs.m_aException));
  }

  @Override
  public int hashCode ()
  {
    return HashCodeGenerator.getDerived (super.hashCode ()).append (ClassHelper.getSafeClassName (m_aException)).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ()).append ("throwable", m_aException).getToString ();
  }

  /**
   * Create a new success object.
   *
   * @param aObj
   *        The returned value from the DB
   * @return Never <code>null</code>.
   * @param <T>
   *        Data type of provided parameter
   */
  @Nonnull
  public static <T> JPAExecutionResult <T> createSuccess (@Nullable final T aObj)
  {
    return new JPAExecutionResult <> (ESuccess.SUCCESS, aObj, null);
  }

  /**
   * Create a new failure object.
   *
   * @param ex
   *        The exception that occurred.
   * @return Never <code>null</code>.
   * @param <T>
   *        Data type - depends on callers requirements.
   */
  @Nonnull
  public static <T> JPAExecutionResult <T> createFailure (@Nullable final Exception ex)
  {
    return new JPAExecutionResult <> (ESuccess.FAILURE, null, ex);
  }
}
