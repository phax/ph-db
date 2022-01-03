/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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

import javax.annotation.CheckForSigned;

import com.helger.commons.CGlobal;
import com.helger.commons.callback.ICallback;

/**
 * This callback is used to retrieve generated keys upon insertion.
 *
 * @author Philip Helger
 */
public interface IUpdatedRowCountCallback extends ICallback
{
  /** Default value for uninitialized row count */
  long NOT_INITIALIZED = CGlobal.ILLEGAL_UINT;

  /**
   * @return The number of updated rows or {@link #NOT_INITIALIZED} if
   *         {@link #setUpdatedRowCount(long)} was never called.
   */
  @CheckForSigned
  long getUpdatedRowCount ();

  /**
   * Notify on the updated row count update.
   *
   * @param nUpdatedRowCount
   *        The number of updated rows (e.g. on update or delete)
   */
  void setUpdatedRowCount (long nUpdatedRowCount);
}
