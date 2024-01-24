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
package com.helger.db.jpa.proxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.persistence.EntityManager;

public class EntityManagerWithListener extends EntityManagerProxy
{
  private IEntityManagerListener m_aListener;

  public EntityManagerWithListener (@Nonnull final EntityManager aEntityMgr)
  {
    super (aEntityMgr);
  }

  public void setCloseListener (@Nullable final IEntityManagerListener aListener)
  {
    m_aListener = aListener;
  }

  @Nullable
  public IEntityManagerListener getCloseListener ()
  {
    return m_aListener;
  }

  @Override
  public void close ()
  {
    try
    {
      super.close ();
    }
    finally
    {
      // Call even on exception
      if (m_aListener != null)
        m_aListener.onAfterEntityManagerClosed ();
    }
  }
}
