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
package com.helger.db.api.h2;

import com.helger.base.id.IHasIntID;

/**
 * H2 lock mode
 *
 * @author Philip Helger
 */
public enum EH2LockMode implements IHasIntID
{
  READ_COMMITTED (3),
  SERIALIZABLE (1),
  READ_UNCOMMITED (0);

  /** Default lock mode: read committed */
  public static final EH2LockMode DEFAULT = READ_COMMITTED;

  private final int m_nValue;

  EH2LockMode (final int i)
  {
    m_nValue = i;
  }

  public int getID ()
  {
    return m_nValue;
  }
}
