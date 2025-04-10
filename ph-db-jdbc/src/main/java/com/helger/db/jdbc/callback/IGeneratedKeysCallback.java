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

import com.helger.commons.callback.ICallback;
import com.helger.commons.collection.impl.ICommonsList;

/**
 * This callback is used to retrieve generated keys upon insertion.
 *
 * @author Philip Helger
 */
@FunctionalInterface
public interface IGeneratedKeysCallback extends ICallback
{
  /**
   * Set all generated keys.
   *
   * @param aGeneratedKeys
   *        The outer list represents all rows, whereas the nested inner lists
   *        represents all columns containing the different values.
   */
  void onGeneratedKeys (@Nonnull ICommonsList <ICommonsList <Object>> aGeneratedKeys);
}
