/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.db.jpa.config;

import javax.annotation.Nullable;

import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.thirdparty.ELicense;
import com.helger.commons.thirdparty.IThirdPartyModule;
import com.helger.commons.thirdparty.IThirdPartyModuleProviderSPI;
import com.helger.commons.thirdparty.ThirdPartyModule;
import com.helger.commons.version.Version;

/**
 * Implement this SPI interface if your JAR file contains external third party
 * modules.
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public final class ThirdPartyModuleProvider_ph_db_jpa implements IThirdPartyModuleProviderSPI
{
  /** EclipseLink */
  private static final IThirdPartyModule ECLIPSE_LINK = new ThirdPartyModule ("EclipseLink",
                                                                              "Eclipse Foundation",
                                                                              ELicense.EPL10,
                                                                              new Version (2, 6, 3),
                                                                              "http://www.eclipse.org/eclipselink/");

  /** H2 database */
  public static final IThirdPartyModule H2 = new ThirdPartyModule ("H2 Database Engine",
                                                                   "Eclipse Foundation",
                                                                   ELicense.EPL10,
                                                                   new Version (1, 4, 192),
                                                                   "http://www.h2database.com/",
                                                                   true);
  /** MySQL connector */
  public static final IThirdPartyModule MYSQL = new ThirdPartyModule ("MySQL Connector/J",
                                                                      "Oracle",
                                                                      ELicense.GPL20,
                                                                      new Version (6, 0, 4),
                                                                      "http://www.mysql.com/",
                                                                      true);

  @Nullable
  public IThirdPartyModule [] getAllThirdPartyModules ()
  {
    return new IThirdPartyModule [] { ECLIPSE_LINK, H2, MYSQL };
  }
}
