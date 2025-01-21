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
package com.helger.db.jpa.proxy;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SynchronizationType;

/**
 * A special {@link EntityManagerFactory} that creates {@link EntityManager}
 * objects that are unique per thread.
 *
 * @author Philip Helger
 */
public class EntityManagerFactoryWithListener extends EntityManagerFactoryProxy implements IEntityManagerListener
{
  private static final ThreadLocal <EntityManagerWithListener> TL = new ThreadLocal <> ();

  public EntityManagerFactoryWithListener (@Nonnull final EntityManagerFactory aEntityMgrFactory)
  {
    super (aEntityMgrFactory);
  }

  @SuppressWarnings ("rawtypes")
  @Override
  public EntityManagerWithListener createEntityManager ()
  {
    return createEntityManager ((Map) null);
  }

  @Override
  public EntityManagerWithListener createEntityManager (@Nullable final Map aProperties)
  {
    EntityManagerWithListener aEntityMgr = TL.get ();
    if (aEntityMgr == null)
    {
      aEntityMgr = new EntityManagerWithListener (super.createEntityManager (aProperties));
      TL.set (aEntityMgr);
      // Set special listener, so that the ThreadLocal is cleared after close
      aEntityMgr.setCloseListener (this);
    }
    return aEntityMgr;
  }

  @SuppressWarnings ("rawtypes")
  @Override
  public EntityManager createEntityManager (final SynchronizationType eSynchronizationType)
  {
    return createEntityManager (eSynchronizationType, (Map) null);
  }

  @Override
  public EntityManager createEntityManager (final SynchronizationType eSynchronizationType, final Map aProperties)
  {
    EntityManagerWithListener aEntityMgr = TL.get ();
    if (aEntityMgr == null)
    {
      aEntityMgr = new EntityManagerWithListener (super.createEntityManager (eSynchronizationType, aProperties));
      TL.set (aEntityMgr);
      // Set special listener, so that the ThreadLocal is cleared after close
      aEntityMgr.setCloseListener (this);
    }
    return aEntityMgr;
  }

  @OverridingMethodsMustInvokeSuper
  public void onAfterEntityManagerClosed ()
  {
    TL.remove ();
  }
}
