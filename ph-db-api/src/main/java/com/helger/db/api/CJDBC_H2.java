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
package com.helger.db.api;

import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;

/**
 * JDBC constants for H2
 *
 * @author Philip Helger
 */
@Immutable
public final class CJDBC_H2
{
  /** Default JDBC URL prefix */
  public static final String CONNECTION_PREFIX = "jdbc:h2:";
  public static final String DEFAULT_JDBC_DRIVER_CLASS_NAME = "org.h2.Driver";

  @PresentForCodeCoverage
  private static final CJDBC_H2 s_aInstance = new CJDBC_H2 ();

  private CJDBC_H2 ()
  {}
}
