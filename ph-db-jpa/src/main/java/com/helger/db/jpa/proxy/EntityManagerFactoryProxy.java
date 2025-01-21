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

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.CodingStyleguideUnaware;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Proxy implementation of the {@link EntityManagerFactory} interface.
 *
 * @author Philip Helger
 */
public class EntityManagerFactoryProxy implements EntityManagerFactory
{
  private final EntityManagerFactory m_aEntityMgrFactory;

  protected EntityManagerFactoryProxy (@Nonnull final EntityManagerFactory aEntityMgrFactory)
  {
    ValueEnforcer.notNull (aEntityMgrFactory, "EntityMgrFactory");
    m_aEntityMgrFactory = aEntityMgrFactory;
  }

  @Nonnull
  public final EntityManagerFactory getWrappedEntityManagerFactory ()
  {
    return m_aEntityMgrFactory;
  }

  public EntityManager createEntityManager ()
  {
    return m_aEntityMgrFactory.createEntityManager ();
  }

  public EntityManager createEntityManager (final Map map)
  {
    return m_aEntityMgrFactory.createEntityManager (map);
  }

  public EntityManager createEntityManager (final SynchronizationType synchronizationType)
  {
    return m_aEntityMgrFactory.createEntityManager (synchronizationType);
  }

  public EntityManager createEntityManager (final SynchronizationType synchronizationType, final Map map)
  {
    return m_aEntityMgrFactory.createEntityManager (synchronizationType, map);
  }

  public boolean isOpen ()
  {
    return m_aEntityMgrFactory.isOpen ();
  }

  public void close ()
  {
    m_aEntityMgrFactory.close ();
  }

  // public String getName ()
  // {
  // return m_aEntityMgrFactory.getName ();
  // }

  public CriteriaBuilder getCriteriaBuilder ()
  {
    return m_aEntityMgrFactory.getCriteriaBuilder ();
  }

  public Metamodel getMetamodel ()
  {
    return m_aEntityMgrFactory.getMetamodel ();
  }

  @CodingStyleguideUnaware
  public Map <String, Object> getProperties ()
  {
    return m_aEntityMgrFactory.getProperties ();
  }

  public Cache getCache ()
  {
    return m_aEntityMgrFactory.getCache ();
  }

  public PersistenceUnitUtil getPersistenceUnitUtil ()
  {
    return m_aEntityMgrFactory.getPersistenceUnitUtil ();
  }

  // public PersistenceUnitTransactionType getTransactionType ()
  // {
  // return m_aEntityMgrFactory.getTransactionType ();
  // }

  // public SchemaManager getSchemaManager ()
  // {
  // return m_aEntityMgrFactory.getSchemaManager ();
  // }

  public void addNamedQuery (final String name, final Query query)
  {
    m_aEntityMgrFactory.addNamedQuery (name, query);
  }

  public <T> T unwrap (final Class <T> cls)
  {
    return m_aEntityMgrFactory.unwrap (cls);
  }

  public <T> void addNamedEntityGraph (final String graphName, final EntityGraph <T> entityGraph)
  {
    m_aEntityMgrFactory.addNamedEntityGraph (graphName, entityGraph);
  }

  // public <R> Map <String, TypedQueryReference <R>> getNamedQueries (final
  // Class <R> resultType)
  // {
  // return m_aEntityMgrFactory.getNamedQueries (resultType);
  // }
  //
  // public <E> Map <String, EntityGraph <? extends E>> getNamedEntityGraphs
  // (final Class <E> entityType)
  // {
  // return m_aEntityMgrFactory.getNamedEntityGraphs (entityType);
  // }
  //
  // public void runInTransaction (final Consumer <EntityManager> work)
  // {
  // m_aEntityMgrFactory.runInTransaction (work);
  // }
  //
  // public <R> R callInTransaction (final Function <EntityManager, R> work)
  // {
  // return m_aEntityMgrFactory.callInTransaction (work);
  // }
}
