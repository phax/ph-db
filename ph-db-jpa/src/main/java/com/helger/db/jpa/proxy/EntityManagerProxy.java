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

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.CodingStyleguideUnaware;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Proxy implementation of the {@link EntityManager} interface.
 *
 * @author Philip Helger
 */
public class EntityManagerProxy implements EntityManager
{
  private final EntityManager m_aEntityMgr;

  public EntityManagerProxy (@Nonnull final EntityManager aEntityMgr)
  {
    ValueEnforcer.notNull (aEntityMgr, "EntityMgr");
    m_aEntityMgr = aEntityMgr;
  }

  @Nonnull
  public final EntityManager getWrappedEntityManager ()
  {
    return m_aEntityMgr;
  }

  public void persist (final Object object)
  {
    m_aEntityMgr.persist (object);
  }

  public <T> T merge (final T entity)
  {
    return m_aEntityMgr.merge (entity);
  }

  public void remove (final Object object)
  {
    m_aEntityMgr.remove (object);
  }

  public <T> T find (final Class <T> entityClass, final Object primaryKey)
  {
    return m_aEntityMgr.find (entityClass, primaryKey);
  }

  public <T> T getReference (final Class <T> entityClass, final Object primaryKey)
  {
    return m_aEntityMgr.getReference (entityClass, primaryKey);
  }

  public void flush ()
  {
    m_aEntityMgr.flush ();
  }

  public void setFlushMode (final FlushModeType flushModeType)
  {
    m_aEntityMgr.setFlushMode (flushModeType);
  }

  public FlushModeType getFlushMode ()
  {
    return m_aEntityMgr.getFlushMode ();
  }

  public void lock (final Object object, final LockModeType lockModeType)
  {
    m_aEntityMgr.lock (object, lockModeType);
  }

  public void refresh (final Object object)
  {
    m_aEntityMgr.refresh (object);
  }

  public void clear ()
  {
    m_aEntityMgr.clear ();
  }

  public boolean contains (final Object object)
  {
    return m_aEntityMgr.contains (object);
  }

  public Query createQuery (final String string)
  {
    return m_aEntityMgr.createQuery (string);
  }

  public Query createNamedQuery (final String string)
  {
    return m_aEntityMgr.createNamedQuery (string);
  }

  public Query createNativeQuery (final String string)
  {
    return m_aEntityMgr.createNativeQuery (string);
  }

  public Query createNativeQuery (final String string, final Class aClass)
  {
    return m_aEntityMgr.createNativeQuery (string, aClass);
  }

  public Query createNativeQuery (final String string, final String string0)
  {
    return m_aEntityMgr.createNativeQuery (string, string0);
  }

  public void joinTransaction ()
  {
    m_aEntityMgr.joinTransaction ();
  }

  public Object getDelegate ()
  {
    return m_aEntityMgr.getDelegate ();
  }

  public void close ()
  {
    m_aEntityMgr.close ();
  }

  public boolean isOpen ()
  {
    return m_aEntityMgr.isOpen ();
  }

  public EntityTransaction getTransaction ()
  {
    return m_aEntityMgr.getTransaction ();
  }

  public <T> T find (final Class <T> entityClass, final Object primaryKey, final Map <String, Object> properties)
  {
    return m_aEntityMgr.find (entityClass, primaryKey, properties);
  }

  public <T> T find (final Class <T> entityClass, final Object primaryKey, final LockModeType lockMode)
  {
    return m_aEntityMgr.find (entityClass, primaryKey, lockMode);
  }

  public <T> T find (final Class <T> entityClass,
                     final Object primaryKey,
                     final LockModeType lockMode,
                     final Map <String, Object> properties)
  {
    return m_aEntityMgr.find (entityClass, primaryKey, lockMode, properties);
  }

  public void lock (final Object entity, final LockModeType lockMode, final Map <String, Object> properties)
  {
    m_aEntityMgr.lock (entity, lockMode, properties);
  }

  public void refresh (final Object entity, final Map <String, Object> properties)
  {
    m_aEntityMgr.refresh (entity, properties);
  }

  public void refresh (final Object entity, final LockModeType lockMode)
  {
    m_aEntityMgr.refresh (entity, lockMode);
  }

  public void refresh (final Object entity, final LockModeType lockMode, final Map <String, Object> properties)
  {
    m_aEntityMgr.refresh (entity, lockMode, properties);
  }

  public void detach (final Object entity)
  {
    m_aEntityMgr.detach (entity);
  }

  public LockModeType getLockMode (final Object entity)
  {
    return m_aEntityMgr.getLockMode (entity);
  }

  public void setProperty (final String propertyName, final Object value)
  {
    m_aEntityMgr.setProperty (propertyName, value);
  }

  @CodingStyleguideUnaware
  public Map <String, Object> getProperties ()
  {
    return m_aEntityMgr.getProperties ();
  }

  public <T> TypedQuery <T> createQuery (final CriteriaQuery <T> criteriaQuery)
  {
    return m_aEntityMgr.createQuery (criteriaQuery);
  }

  public Query createQuery (final CriteriaUpdate updateQuery)
  {
    return m_aEntityMgr.createQuery (updateQuery);
  }

  public Query createQuery (final CriteriaDelete deleteQuery)
  {
    return m_aEntityMgr.createQuery (deleteQuery);
  }

  public <T> TypedQuery <T> createQuery (final String qlString, final Class <T> resultClass)
  {
    return m_aEntityMgr.createQuery (qlString, resultClass);
  }

  public <T> TypedQuery <T> createNamedQuery (final String name, final Class <T> resultClass)
  {
    return m_aEntityMgr.createNamedQuery (name, resultClass);
  }

  public StoredProcedureQuery createNamedStoredProcedureQuery (final String name)
  {
    return m_aEntityMgr.createNamedStoredProcedureQuery (name);
  }

  public StoredProcedureQuery createStoredProcedureQuery (final String procedureName)
  {
    return m_aEntityMgr.createStoredProcedureQuery (procedureName);
  }

  public StoredProcedureQuery createStoredProcedureQuery (final String procedureName, final Class... resultClasses)
  {
    return m_aEntityMgr.createStoredProcedureQuery (procedureName, resultClasses);
  }

  public StoredProcedureQuery createStoredProcedureQuery (final String procedureName, final String... resultSetMappings)
  {
    return m_aEntityMgr.createStoredProcedureQuery (procedureName, resultSetMappings);
  }

  public boolean isJoinedToTransaction ()
  {
    return m_aEntityMgr.isJoinedToTransaction ();
  }

  public <T> T unwrap (final Class <T> cls)
  {
    return m_aEntityMgr.unwrap (cls);
  }

  public EntityManagerFactory getEntityManagerFactory ()
  {
    return m_aEntityMgr.getEntityManagerFactory ();
  }

  public CriteriaBuilder getCriteriaBuilder ()
  {
    return m_aEntityMgr.getCriteriaBuilder ();
  }

  public Metamodel getMetamodel ()
  {
    return m_aEntityMgr.getMetamodel ();
  }

  public <T> EntityGraph <T> createEntityGraph (final Class <T> rootType)
  {
    return m_aEntityMgr.createEntityGraph (rootType);
  }

  public EntityGraph <?> createEntityGraph (final String graphName)
  {
    return m_aEntityMgr.createEntityGraph (graphName);
  }

  public EntityGraph <?> getEntityGraph (final String graphName)
  {
    return m_aEntityMgr.getEntityGraph (graphName);
  }

  @CodingStyleguideUnaware
  public <T> List <EntityGraph <? super T>> getEntityGraphs (final Class <T> entityClass)
  {
    return m_aEntityMgr.getEntityGraphs (entityClass);
  }
}
