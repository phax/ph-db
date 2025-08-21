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
package com.helger.db.jpa;

import java.util.Map;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.annotation.OverridingMethodsMustInvokeSuper;
import com.helger.annotation.style.OverrideOnDemand;
import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsMap;
import com.helger.db.jpa.eclipselink.EclipseLinkLogger;
import com.helger.db.jpa.eclipselink.EclipseLinkSessionCustomizer;
import com.helger.db.jpa.utils.PersistenceXmlHelper;
import com.helger.scope.IScope;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

/**
 * Abstract global singleton to handle a single persistence unit.
 *
 * @author Philip Helger
 */
public abstract class AbstractGlobalEntityManagerFactory extends AbstractGlobalSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractGlobalEntityManagerFactory.class);

  static
  {
    // Check if all existing META-INF/persistence.xml files reference existing
    // classes
    PersistenceXmlHelper.checkPersistenceXMLValidity ();
  }

  private final String m_sPersistenceUnitName;
  private final ICommonsMap <String, Object> m_aFactoryProps;
  private EntityManagerFactory m_aFactory;

  /**
   * Constructor
   *
   * @param sJdbcDriverClass
   *        Name of the JDBC driver class. Must be a class implementing
   *        java.sql.Driver.
   * @param sJdbcURL
   *        JDBC URL
   * @param sUserName
   *        User name to access the DB. May be <code>null</code>.
   * @param sPassword
   *        Password to access the DB. May be <code>null</code>.
   * @param sPlatformClass
   *        The EclipseLink platform name. May either be a fully qualified
   *        class-name of a recognized abbreviation.
   * @param sPersistenceUnitName
   *        The name of the persistence unit as stated in the persistence.xml
   * @param aAdditionalFactoryProperties
   *        An optional Map with properties for {@link EntityManagerFactory}.
   *        This can even be used to overwrite the settings specified as
   *        explicit parameters, so be careful. This map is applied after the
   *        special properties are set! May be <code>null</code>.
   */
  protected AbstractGlobalEntityManagerFactory (@Nonnull @Nonempty final String sJdbcDriverClass,
                                                @Nonnull @Nonempty final String sJdbcURL,
                                                @Nullable final String sUserName,
                                                @Nullable final String sPassword,
                                                @Nonnull @Nonempty final String sPlatformClass,
                                                @Nonnull @Nonempty final String sPersistenceUnitName,
                                                @Nullable final Map <String, Object> aAdditionalFactoryProperties)
  {
    ValueEnforcer.notEmpty (sJdbcDriverClass, "JdbcDriverClass");
    ValueEnforcer.notEmpty (sJdbcURL, "JdbcURL");
    ValueEnforcer.notEmpty (sPlatformClass, "PlatformClass");
    ValueEnforcer.notEmpty (sPersistenceUnitName, "PersistenceUnitName");

    LOGGER.info ("Using JDBC URL " + sJdbcURL + " with JDBC driver " + sJdbcDriverClass + " and user '" + sUserName + "'");

    final ICommonsMap <String, Object> aFactoryProps = new CommonsHashMap <> ();
    aFactoryProps.put (PersistenceUnitProperties.JDBC_DRIVER, sJdbcDriverClass);
    aFactoryProps.put (PersistenceUnitProperties.JDBC_URL, sJdbcURL);
    aFactoryProps.put (PersistenceUnitProperties.JDBC_USER, sUserName);
    aFactoryProps.put (PersistenceUnitProperties.JDBC_PASSWORD, sPassword);

    aFactoryProps.put (PersistenceUnitProperties.LOGGING_LOGGER, EclipseLinkLogger.class.getName ());
    aFactoryProps.put (PersistenceUnitProperties.SESSION_CUSTOMIZER, EclipseLinkSessionCustomizer.class.getName ());
    aFactoryProps.put (PersistenceUnitProperties.TARGET_DATABASE, sPlatformClass);

    // Not desired to have default values for
    // PersistenceUnitProperties.DDL_GENERATION,
    // PersistenceUnitProperties.CREATE_JDBC_DDL_FILE and
    // PersistenceUnitProperties.DROP_JDBC_DDL_FILE, when multiple JPA
    // configurations are present

    // Add parameter properties
    aFactoryProps.addAll (aAdditionalFactoryProperties);

    // Consistency check if no explicit DDL generation mode is specified!
    if (aFactoryProps.containsKey (PersistenceUnitProperties.DDL_GENERATION) &&
        !aFactoryProps.containsKey (PersistenceUnitProperties.DDL_GENERATION_MODE))
    {
      final String sDDLGeneration = (String) aFactoryProps.get (PersistenceUnitProperties.DDL_GENERATION);
      if (!PersistenceUnitProperties.NONE.equals (sDDLGeneration))
      {
        LOGGER.warn ("DDL generation is set to '" +
                     sDDLGeneration +
                     "' but no DDL generation mode is defined, which defaults to '" +
                     PersistenceUnitProperties.DDL_DATABASE_GENERATION +
                     "' - defaulting to '" +
                     PersistenceUnitProperties.DDL_SQL_SCRIPT_GENERATION +
                     "'!!!");
        aFactoryProps.put (PersistenceUnitProperties.DDL_GENERATION_MODE, PersistenceUnitProperties.DDL_SQL_SCRIPT_GENERATION);
      }
    }

    m_sPersistenceUnitName = sPersistenceUnitName;
    m_aFactoryProps = aFactoryProps;
  }

  /**
   * This method allows you to customize the created
   * {@link EntityManagerFactory} in any way. By default it is returned as
   * created.
   *
   * @param aEMF
   *        The original {@link EntityManagerFactory}. Never <code>null</code>.
   * @return The final {@link EntityManagerFactory} to use. May not be
   *         <code>null</code>.
   */
  @Nonnull
  @OverrideOnDemand
  protected EntityManagerFactory customizeEntityManagerFactory (@Nonnull final EntityManagerFactory aEMF)
  {
    return aEMF;
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  protected void onAfterInstantiation (@Nonnull final IScope aScope)
  {
    // Create entity manager factory
    final EntityManagerFactory aFactory = Persistence.createEntityManagerFactory (m_sPersistenceUnitName, m_aFactoryProps);
    if (aFactory == null)
      throw new IllegalStateException ("Failed to create entity manager factory for persistence unit '" +
                                       m_sPersistenceUnitName +
                                       "' with properties " +
                                       m_aFactoryProps.toString () +
                                       "!");

    // Customize on demand
    m_aFactory = customizeEntityManagerFactory (aFactory);
    LOGGER.info ("Created EntityManagerFactory for persistence unit '" + m_sPersistenceUnitName + "'");

    // Consistency check after creation!
    final Map <String, Object> aRealProps = m_aFactory.getProperties ();
    if (aRealProps.containsKey (PersistenceUnitProperties.DDL_GENERATION) &&
        !aRealProps.containsKey (PersistenceUnitProperties.DDL_GENERATION_MODE))
    {
      final String sDDLGeneration = (String) aRealProps.get (PersistenceUnitProperties.DDL_GENERATION);
      if (!PersistenceUnitProperties.NONE.equals (sDDLGeneration))
      {
        throw new IllegalStateException ("DDL generation is set to '" +
                                         sDDLGeneration +
                                         "' but no DDL generation mode is defined, which defaults to '" +
                                         PersistenceUnitProperties.DDL_DATABASE_GENERATION +
                                         "' which can erase all your data. Please explicitly state a value for the property '" +
                                         PersistenceUnitProperties.DDL_GENERATION_MODE +
                                         "'!!!\nEffective properties are: " +
                                         aRealProps.toString ());
      }
    }
  }

  /**
   * Called when the global scope is destroyed (e.g. upon servlet context
   * shutdown)
   *
   * @throws Exception
   *         if closing fails
   */
  @Override
  @OverridingMethodsMustInvokeSuper
  protected void onDestroy (@Nonnull final IScope aScopeInDestruction) throws Exception
  {
    // Destroy factory
    if (m_aFactory != null)
    {
      if (m_aFactory.isOpen ())
      {
        // Clear cache
        try
        {
          m_aFactory.getCache ().evictAll ();
        }
        catch (final PersistenceException ex)
        {
          // May happen if now database connection is available
        }
        // Close
        m_aFactory.close ();
      }
      m_aFactory = null;
    }
    LOGGER.info ("Closed EntityManagerFactory for persistence unit '" + m_sPersistenceUnitName + "'");
  }

  /**
   * @return The persistence unit name. Neither <code>null</code> nor empty.
   */
  @Nonnull
  @Nonempty
  public final String getPersistenceUnitName ()
  {
    return m_sPersistenceUnitName;
  }

  /**
   * @return The EntityManagerFactory creation properties. Never
   *         <code>null</code>.
   */
  @Nonnull
  @ReturnsMutableCopy
  public final ICommonsMap <String, Object> getAllFactoryProperties ()
  {
    return m_aFactoryProps.getClone ();
  }

  /**
   * @return The underlying {@link EntityManagerFactory}. Never
   *         <code>null</code>.
   */
  @Nonnull
  public final EntityManagerFactory getEntityManagerFactory ()
  {
    final EntityManagerFactory ret = m_aFactory;
    if (ret == null)
      throw new IllegalStateException ("No EntityManagerFactory present!");
    return ret;
  }

  /**
   * Create a new {@link EntityManager} with the default properties - usually
   * this is suitable!
   *
   * @return The created {@link EntityManager} and never <code>null</code>.
   */
  @Nonnull
  public final EntityManager createEntityManager ()
  {
    return createEntityManager (null);
  }

  /**
   * Create a new {@link EntityManager} with custom properties!
   *
   * @param aMap
   *        The custom properties to use. May be <code>null</code> for no
   *        properties.
   * @return The created {@link EntityManager} and never <code>null</code>.
   */
  @Nonnull
  public EntityManager createEntityManager (@SuppressWarnings ("rawtypes") final Map aMap)
  {
    // Create entity manager (factory may be null - e.g. after close)
    final EntityManager aEntityManager = getEntityManagerFactory ().createEntityManager (aMap);
    if (aEntityManager == null)
      throw new IllegalStateException ("Failed to create EntityManager from factory " + m_aFactory + " with parameters " + aMap + "!");
    return aEntityManager;
  }
}
