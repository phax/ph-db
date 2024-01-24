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
package com.helger.db.api;

import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.thirdparty.ELicense;
import com.helger.commons.thirdparty.IThirdPartyModule;
import com.helger.commons.thirdparty.ThirdPartyModule;
import com.helger.commons.version.Version;

/**
 * JDBC constants for PostgreSQL
 *
 * @author Philip Helger
 * @since 6.3.0
 */
@Immutable
public final class CJDBC_PostgreSQL
{
  /** MySQL connector */
  public static final IThirdPartyModule POSTGRESQL = new ThirdPartyModule ("PostgreSQL JDBC Driver",
                                                                           "PostgreSQL Global Development Group",
                                                                           ELicense.BSD,
                                                                           new Version (42, 2, 15),
                                                                           "https://jdbc.postgresql.org");

  public static final String CONNECTION_PREFIX = "jjdbc:postgresql:";

  public static final String DEFAULT_JDBC_DRIVER_CLASS_NAME = "org.postgresql.Driver";

  @PresentForCodeCoverage
  private static final CJDBC_PostgreSQL INSTANCE = new CJDBC_PostgreSQL ();

  private CJDBC_PostgreSQL ()
  {}
}
