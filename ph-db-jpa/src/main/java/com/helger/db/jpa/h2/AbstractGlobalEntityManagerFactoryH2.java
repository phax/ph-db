/**
 * Copyright (C) 2014-2018 Philip Helger (www.helger.com)
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
package com.helger.db.jpa.h2;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.platform.database.H2Platform;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.db.api.h2.H2Helper;
import com.helger.db.jpa.AbstractGlobalEntityManagerFactory;

/**
 * JPA Singleton specific for H2 database.
 *
 * @author Philip Helger
 */
public abstract class AbstractGlobalEntityManagerFactoryH2 extends AbstractGlobalEntityManagerFactory
{
  private static final ICommonsMap <String, String> s_aDefaultConnectionProperties = new CommonsHashMap <> ();

  @Nonnull
  @Nonempty
  private static String _buildJDBCString (@Nonnull @Nonempty final String sJdbcURL,
                                          @Nullable final Map <String, String> aConnectionProperties)
  {
    // Build connection properties from default values and the optional ones
    final Map <String, String> aProps = s_aDefaultConnectionProperties.getClone ();
    if (aConnectionProperties != null)
      aProps.putAll (aConnectionProperties);

    return H2Helper.buildJDBCString (sJdbcURL, aProps);
  }

  /*
   * Constructor. Never initialize manually!
   */
  protected AbstractGlobalEntityManagerFactoryH2 (@Nonnull @Nonempty final String sJdbcURL,
                                                  @Nullable final String sUserName,
                                                  @Nullable final String sPassword,
                                                  @Nonnull @Nonempty final String sPersistenceUnitName)
  {
    this (sJdbcURL, null, sUserName, sPassword, sPersistenceUnitName, null);
  }

  /*
   * Constructor. Never initialize manually!
   */
  protected AbstractGlobalEntityManagerFactoryH2 (@Nonnull @Nonempty final String sJdbcURL,
                                                  @Nullable final Map <String, String> aConnectionProperties,
                                                  @Nullable final String sUserName,
                                                  @Nullable final String sPassword,
                                                  @Nonnull @Nonempty final String sPersistenceUnitName)
  {
    this (sJdbcURL, aConnectionProperties, sUserName, sPassword, sPersistenceUnitName, null);
  }

  /**
   * Constructor. Never initialize manually!
   *
   * @param sJdbcURL
   *        JDBC URL
   * @param aConnectionProperties
   *        Connection properties to build the connection string
   * @param sUserName
   *        User name to access the DB. May be <code>null</code>.
   * @param sPassword
   *        Password to access the DB. May be <code>null</code>.
   * @param sPersistenceUnitName
   *        The name of the persistence unit as stated in the persistence.xml
   * @param aAdditionalFactoryProperties
   *        An optional Map with properties for {@link EntityManagerFactory}.
   *        This can even be used to overwrite the settings specified as
   *        explicit parameters, so be careful. This map is applied after the
   *        special properties are set! May be <code>null</code>.
   */
  protected AbstractGlobalEntityManagerFactoryH2 (@Nonnull @Nonempty final String sJdbcURL,
                                                  @Nullable final Map <String, String> aConnectionProperties,
                                                  @Nullable final String sUserName,
                                                  @Nullable final String sPassword,
                                                  @Nonnull @Nonempty final String sPersistenceUnitName,
                                                  @Nullable final Map <String, Object> aAdditionalFactoryProperties)
  {
    super (H2DriverSingleton.getInstance ().getDriverClassName (),
           _buildJDBCString (sJdbcURL, aConnectionProperties),
           sUserName,
           sPassword,
           H2Platform.class.getName (),
           sPersistenceUnitName,
           aAdditionalFactoryProperties);
  }

  public static final void setJMXEnabledByDefault (final boolean bEnabled)
  {
    if (bEnabled)
      s_aDefaultConnectionProperties.put ("JMX", "TRUE");
    else
      s_aDefaultConnectionProperties.remove ("JMX");
  }
}
