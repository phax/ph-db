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
package com.helger.db.jpa.mysql;

import java.util.Map;

import org.eclipse.persistence.platform.database.MySQLPlatform;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.collection.commons.CommonsEnumMap;
import com.helger.collection.commons.ICommonsMap;
import com.helger.db.api.mysql.EMySQLConnectionProperty;
import com.helger.db.api.mysql.MySQLHelper;
import com.helger.db.jpa.AbstractGlobalEntityManagerFactory;

/**
 * JPA Singleton specific for MySQL database.
 *
 * @author Philip Helger
 */
public abstract class AbstractGlobalEntityManagerFactoryMySQL extends AbstractGlobalEntityManagerFactory
{
  private static final ICommonsMap <EMySQLConnectionProperty, String> DEFAULT_CONNECTION_PROPS = new CommonsEnumMap <> (EMySQLConnectionProperty.class);

  @NonNull
  @Nonempty
  private static String _buildJDBCString (@NonNull @Nonempty final String sJdbcURL,
                                          @Nullable final Map <EMySQLConnectionProperty, String> aConnectionProperties)
  {
    // Build connection properties from default values and the optional ones
    final ICommonsMap <EMySQLConnectionProperty, String> aProps = DEFAULT_CONNECTION_PROPS.getClone ();
    aProps.putAllIfNotNull (aConnectionProperties);

    return MySQLHelper.buildJDBCString (sJdbcURL, aProps);
  }

  /*
   * Constructor. Never initialize manually!
   */
  protected AbstractGlobalEntityManagerFactoryMySQL (@NonNull @Nonempty final String sJdbcURL,
                                                     @Nullable final String sUserName,
                                                     @Nullable final String sPassword,
                                                     @NonNull @Nonempty final String sPersistenceUnitName)
  {
    this (sJdbcURL, null, sUserName, sPassword, sPersistenceUnitName, null);
  }

  /*
   * Constructor. Never initialize manually!
   */
  protected AbstractGlobalEntityManagerFactoryMySQL (@NonNull @Nonempty final String sJdbcURL,
                                                     @Nullable final Map <EMySQLConnectionProperty, String> aConnectionProperties,
                                                     @Nullable final String sUserName,
                                                     @Nullable final String sPassword,
                                                     @NonNull @Nonempty final String sPersistenceUnitName)
  {
    this (sJdbcURL, aConnectionProperties, sUserName, sPassword, sPersistenceUnitName, null);
  }

  /*
   * Constructor. Never initialize manually!
   */
  protected AbstractGlobalEntityManagerFactoryMySQL (@NonNull @Nonempty final String sJdbcURL,
                                                     @Nullable final Map <EMySQLConnectionProperty, String> aConnectionProperties,
                                                     @Nullable final String sUserName,
                                                     @Nullable final String sPassword,
                                                     @NonNull @Nonempty final String sPersistenceUnitName,
                                                     @Nullable final Map <String, Object> aAdditionalFactoryProperties)
  {
    super (com.mysql.jdbc.Driver.class.getName (),
           _buildJDBCString (sJdbcURL, aConnectionProperties),
           sUserName,
           sPassword,
           MySQLPlatform.class.getName (),
           sPersistenceUnitName,
           aAdditionalFactoryProperties);
  }
}
