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
package com.helger.db.api.flyway;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.builder.IBuilder;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * Default implementation of {@link IFlywayConfiguration}.
 *
 * @author Philip Helger
 * @since 7.0.7
 */
@Immutable
public class FlywayConfiguration implements IFlywayConfiguration
{
  public static final boolean DEFAULT_FLYWAY_ENABLED = true;
  public static final boolean DEFAULT_FLYWAY_JDBC_SCHEMA_CREATE = false;
  public static final int DEFAULT_FLYWAY_BASELINE_VERSION = 0;

  private final boolean m_bEnabled;
  private final String m_sJdbcUrl;
  private final String m_sJdbcUser;
  private final String m_sJdbcPassword;
  private final boolean m_bSchemaCreate;
  private final int m_nBaselineVersion;

  public FlywayConfiguration (final boolean bEnabled,
                              @Nullable final String sJdbcUrl,
                              @Nullable final String sJdbcUser,
                              @Nullable final String sJdbcPassword,
                              final boolean bSchemaCreate,
                              @Nonnegative final int nBaselineVersion)
  {
    ValueEnforcer.isGE0 (nBaselineVersion, "BaselineVersion");

    m_bEnabled = bEnabled;
    m_sJdbcUrl = sJdbcUrl;
    m_sJdbcUser = sJdbcUser;
    m_sJdbcPassword = sJdbcPassword;
    m_bSchemaCreate = bSchemaCreate;
    m_nBaselineVersion = nBaselineVersion;
  }

  public boolean isFlywayEnabled ()
  {
    return m_bEnabled;
  }

  @Nullable
  public String getFlywayJdbcUrl ()
  {
    return m_sJdbcUrl;
  }

  @Nullable
  public String getFlywayJdbcUser ()
  {
    return m_sJdbcUser;
  }

  @Nullable
  public String getFlywayJdbcPassword ()
  {
    return m_sJdbcPassword;
  }

  public boolean isFlywaySchemaCreate ()
  {
    return m_bSchemaCreate;
  }

  @Nonnegative
  public int getFlywayBaselineVersion ()
  {
    return m_nBaselineVersion;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !o.getClass ().equals (getClass ()))
      return false;
    final FlywayConfiguration rhs = (FlywayConfiguration) o;
    return m_bEnabled == rhs.m_bEnabled &&
           EqualsHelper.equals (m_sJdbcUrl, rhs.m_sJdbcUrl) &&
           EqualsHelper.equals (m_sJdbcUser, rhs.m_sJdbcUser) &&
           EqualsHelper.equals (m_sJdbcPassword, rhs.m_sJdbcPassword) &&
           m_bSchemaCreate == rhs.m_bSchemaCreate &&
           m_nBaselineVersion == rhs.m_nBaselineVersion;
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_bEnabled)
                                       .append (m_sJdbcUrl)
                                       .append (m_sJdbcUser)
                                       .append (m_sJdbcPassword)
                                       .append (m_bSchemaCreate)
                                       .append (m_nBaselineVersion)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Enabled", m_bEnabled)
                                       .append ("JdbcUrl", m_sJdbcUrl)
                                       .append ("JdbcUser", m_sJdbcUser)
                                       .appendPassword ("JdbcPassword")
                                       .append ("SchemaCreate", m_bSchemaCreate)
                                       .append ("BaselineVersion", m_nBaselineVersion)
                                       .getToString ();
  }

  /**
   * @return A new empty {@link Builder}. Never <code>null</code>.
   */
  @Nonnull
  public static Builder builder ()
  {
    return new Builder ();
  }

  /**
   * Create a new builder
   *
   * @param aBase
   *        The base object to take over the value. May not be <code>null</code>.
   * @return A new {@link Builder} that contains the values of the existing object.
   */
  @Nonnull
  public static Builder builder (@Nonnull final IFlywayConfiguration aBase)
  {
    return new Builder (aBase);
  }

  /**
   * Builder class for {@link FlywayConfiguration} objects.
   *
   * @author Philip Helger
   */
  @NotThreadSafe
  public static class Builder implements IBuilder <FlywayConfiguration>
  {
    private boolean m_bEnabled = DEFAULT_FLYWAY_ENABLED;
    private String m_sJdbcUrl;
    private String m_sJdbcUser;
    private String m_sJdbcPassword;
    private boolean m_bSchemaCreate = DEFAULT_FLYWAY_JDBC_SCHEMA_CREATE;
    private int m_nBaselineVersion = DEFAULT_FLYWAY_BASELINE_VERSION;

    public Builder ()
    {}

    public Builder (@Nonnull final IFlywayConfiguration aBase)
    {
      ValueEnforcer.notNull (aBase, "Base");
      enabled (aBase.isFlywayEnabled ()).jdbcUrl (aBase.getFlywayJdbcUrl ())
                                        .jdbcUser (aBase.getFlywayJdbcUser ())
                                        .jdbcPassword (aBase.getFlywayJdbcPassword ())
                                        .schemaCreate (aBase.isFlywaySchemaCreate ())
                                        .baselineVersion (aBase.getFlywayBaselineVersion ());
    }

    @Nonnull
    public Builder enabled ()
    {
      return enabled (true);
    }

    @Nonnull
    public Builder disabled ()
    {
      return enabled (false);
    }

    @Nonnull
    public Builder enabled (final boolean b)
    {
      m_bEnabled = b;
      return this;
    }

    @Nonnull
    public Builder jdbcUrl (@Nullable final String s)
    {
      m_sJdbcUrl = s;
      return this;
    }

    @Nonnull
    public Builder jdbcUser (@Nullable final String s)
    {
      m_sJdbcUser = s;
      return this;
    }

    @Nonnull
    public Builder jdbcPassword (@Nullable final String s)
    {
      m_sJdbcPassword = s;
      return this;
    }

    @Nonnull
    public Builder schemaCreate (final boolean b)
    {
      m_bSchemaCreate = b;
      return this;
    }

    @Nonnull
    public Builder baselineVersion (@Nonnegative final int n)
    {
      ValueEnforcer.isGE0 (n, "BaselineVersion");

      m_nBaselineVersion = n;
      return this;
    }

    @Nonnull
    public FlywayConfiguration build ()
    {
      // Everything is optional - nothing to check
      return new FlywayConfiguration (m_bEnabled,
                                      m_sJdbcUrl,
                                      m_sJdbcUser,
                                      m_sJdbcPassword,
                                      m_bSchemaCreate,
                                      m_nBaselineVersion);
    }
  }
}
