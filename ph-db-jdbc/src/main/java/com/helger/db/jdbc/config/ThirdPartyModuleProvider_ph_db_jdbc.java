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
package com.helger.db.jdbc.config;

import org.jspecify.annotations.Nullable;

import com.helger.annotation.style.IsSPIImplementation;
import com.helger.base.thirdparty.ELicense;
import com.helger.base.thirdparty.IThirdPartyModule;
import com.helger.base.thirdparty.IThirdPartyModuleProviderSPI;
import com.helger.base.thirdparty.ThirdPartyModule;
import com.helger.base.version.Version;

/**
 * Implement this SPI interface if your JAR file contains external third party modules.
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public final class ThirdPartyModuleProvider_ph_db_jdbc implements IThirdPartyModuleProviderSPI
{
  /** Apache commons-pool2 */
  public static final IThirdPartyModule COMMONS_POOL = new ThirdPartyModule ("Apache Commons Pool",
                                                                             "Apache",
                                                                             ELicense.APACHE2,
                                                                             new Version (2, 12, 1),
                                                                             "http://commons.apache.org/proper/commons-pool/");
  /** Apache commons-dbcp2 */
  public static final IThirdPartyModule COMMONS_DBCP = new ThirdPartyModule ("Apache Commons DBCP",
                                                                             "Apache",
                                                                             ELicense.APACHE2,
                                                                             new Version (2, 13, 0),
                                                                             "http://commons.apache.org/proper/commons-dbcp/");

  @Nullable
  public IThirdPartyModule [] getAllThirdPartyModules ()
  {
    return new IThirdPartyModule [] { COMMONS_POOL, COMMONS_DBCP };
  }
}
