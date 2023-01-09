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

import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.string.ToStringGenerator;

/**
 * Default implementation of {@link IUpdatedRowCountCallback}.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class UpdatedRowCountCallback implements IUpdatedRowCountCallback
{
  private long m_nUpdatedRowCount = NOT_INITIALIZED;

  public UpdatedRowCountCallback ()
  {}

  public void setUpdatedRowCount (final long nUpdatedRowCount)
  {
    m_nUpdatedRowCount = nUpdatedRowCount;
  }

  public long getUpdatedRowCount ()
  {
    return m_nUpdatedRowCount;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("UpdatedRowCount", m_nUpdatedRowCount).getToString ();
  }
}
