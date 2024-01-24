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
package com.helger.db.jpa.eclipselink;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.Session;

import com.helger.commons.CGlobal;

/**
 * Class for customizing JPA sessions.<br>
 * Set the class name in the property
 * <code>eclipselink.session.customizer</code><br>
 * Should have a public no-argument ctor
 *
 * @author Philip Helger
 */
public class EclipseLinkSessionCustomizer implements SessionCustomizer
{
  private static final AtomicInteger LOG_LEVEL = new AtomicInteger (CGlobal.ILLEGAL_UINT);

  public EclipseLinkSessionCustomizer ()
  {}

  /**
   * See {@link SessionLog} for the available log levels
   *
   * @param nLogLevel
   *        Log level to use.
   */
  public static void setGlobalLogLevel (final int nLogLevel)
  {
    if (nLogLevel >= SessionLog.ALL && nLogLevel <= SessionLog.OFF)
      LOG_LEVEL.set (nLogLevel);
  }

  public static int getGlobalLogLevel ()
  {
    return LOG_LEVEL.get ();
  }

  public void customize (final Session aSession) throws Exception
  {
    final int nLogLevel = getGlobalLogLevel ();
    if (nLogLevel != CGlobal.ILLEGAL_UINT)
    {
      // create a custom logger and assign it to the session
      final SessionLog aCustomLogger = new EclipseLinkLogger ();
      aCustomLogger.setLevel (nLogLevel);
      aSession.setSessionLog (aCustomLogger);
    }
  }
}
