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
package com.helger.db.jdbc.h2;

import java.sql.SQLException;

import org.h2.api.DatabaseEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.tostring.ToStringGenerator;

/**
 * A logging H2 event listener.
 *
 * @author Philip Helger
 */
public class LoggingH2EventListener implements DatabaseEventListener
{
  private static final Logger LOGGER = LoggerFactory.getLogger (LoggingH2EventListener.class);

  public void init (final String sURL)
  {
    LOGGER.info ("init(" + sURL + ")");
  }

  public void opened ()
  {
    LOGGER.info ("opened()");
  }

  public void diskSpaceIsLow ()
  {
    LOGGER.info ("diskSpaceIsLow()");
  }

  public void exceptionThrown (final SQLException aException, final String sSQLStatement)
  {
    LOGGER.error ("exceptionThrown(" + sSQLStatement + ")", aException);
  }

  private static String _getStateName (final int nState)
  {
    switch (nState)
    {
      case STATE_SCAN_FILE:
        return "scan_file";
      case STATE_CREATE_INDEX:
        return "create_index";
      case STATE_RECOVER:
        return "recover";
      case STATE_BACKUP_FILE:
        return "backup";
      case STATE_RECONNECTED:
        return "reconnected";
      default:
        return Integer.toString (nState);
    }
  }

  public void setProgress (final int nState, final String sObjectName, final int nCurrentStep, final int nTotalSteps)
  {
    LOGGER.info ("setProgress(" + _getStateName (nState) + "," + sObjectName + "," + nCurrentStep + "," + nTotalSteps + ")");
  }

  public void closingDatabase ()
  {
    LOGGER.info ("closingDatabase()");
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).getToString ();
  }
}
