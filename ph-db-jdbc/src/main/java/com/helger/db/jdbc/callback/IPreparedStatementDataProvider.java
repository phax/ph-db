/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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

import java.io.Serializable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.collection.impl.ICommonsList;

/**
 * Interface for objects that deliver content to pass parameters to a prepared
 * statement.
 *
 * @author Philip Helger
 */
public interface IPreparedStatementDataProvider extends Serializable
{
  /**
   * @return The number of parameters provided by this instance.
   */
  @Nonnegative
  int getValueCount ();

  /**
   * @return A non-<code>null</code>, unmodifiable list of values. The length of
   *         the returned list must match the result of {@link #getValueCount()}
   *         .
   */
  @Nonnull
  ICommonsList <Object> getObjectValues ();
}
