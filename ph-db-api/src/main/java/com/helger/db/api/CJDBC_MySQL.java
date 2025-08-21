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
package com.helger.db.api;

import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.PresentForCodeCoverage;
import com.helger.base.thirdparty.ELicense;
import com.helger.base.thirdparty.IThirdPartyModule;
import com.helger.base.thirdparty.ThirdPartyModule;
import com.helger.base.version.Version;

/**
 * JDBC constants for MySQL
 *
 * @author Philip Helger
 */
@Immutable
public final class CJDBC_MySQL
{
  /** MySQL connector */
  public static final IThirdPartyModule MYSQL = new ThirdPartyModule ("MySQL Connector/J",
                                                                      "Oracle",
                                                                      ELicense.GPL20,
                                                                      new Version (8, 0, 21),
                                                                      "http://www.mysql.com/");

  /** Default JDBC URL prefix */
  public static final String CONNECTION_PREFIX = "jdbc:mysql:";
  public static final String DEFAULT_JDBC_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
  public static final String DEFAULT_JDBC_DRIVER_CLASS_NAME_V8 = "com.mysql.cj.jdbc.Driver";

  @PresentForCodeCoverage
  private static final CJDBC_MySQL INSTANCE = new CJDBC_MySQL ();

  private CJDBC_MySQL ()
  {}
}
