/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.ELockType;
import com.helger.commons.annotation.IsLocked;
import com.helger.scope.IScope;
import com.helger.scope.singleton.AbstractRequestSingleton;

/**
 * Abstract request singleton to handle a single {@link EntityManager}.<br>
 * Note: this class does NOT implement {@link IHasEntityManager} by purpose, as
 * this class should not be used as a direct callback parameter, because than
 * only the object of this particular request is used.
 *
 * @author Philip Helger
 */
@ThreadSafe
public abstract class AbstractPerRequestEntityManager extends AbstractRequestSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractPerRequestEntityManager.class);

  private volatile EntityManager m_aEntityManager;
  private boolean m_bDestroyed = false;

  public AbstractPerRequestEntityManager ()
  {}

  /**
   * Create a new {@link EntityManager} when required.
   *
   * @return The created {@link EntityManager}. Never <code>null</code>.
   */
  @Nonnull
  @IsLocked (ELockType.WRITE)
  protected abstract EntityManager createEntityManager ();

  /**
   * @return The {@link EntityManager} to be used in this request. If it is the
   *         first request to an {@link EntityManager} in this request is
   *         created via createEntityManager(). Never <code>null</code>.
   */
  @Nonnull
  public EntityManager getEntityManager ()
  {
    final EntityManager ret = m_aRWLock.readLockedGet ( () -> {
      if (m_bDestroyed)
        throw new IllegalStateException ("This object was already destroyed and should not be re-used!");
      return m_aEntityManager;
    });
    if (ret != null)
      return ret;

    // No EntityManager present for this request
    m_aRWLock.writeLock ().lock ();
    try
    {
      // Try again in write lock
      EntityManager ret2 = m_aEntityManager;
      if (ret2 == null)
      {
        ret2 = createEntityManager ();
        if (ret2 == null)
          throw new IllegalStateException ("Failed to create EntityManager!");
        m_aEntityManager = ret2;

        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("EntityManager created");
      }
      return ret2;
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  protected void onDestroy (@Nonnull final IScope aScopeInDestruction)
  {
    m_aRWLock.writeLock ().lock ();
    try
    {
      // Close EntityManager, if present
      final EntityManager aEM = m_aEntityManager;
      if (aEM != null)
      {
        aEM.close ();
        m_aEntityManager = null;

        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("EntityManager destroyed");
      }
      // Independent if it was closed or not
      m_bDestroyed = true;
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }
}
