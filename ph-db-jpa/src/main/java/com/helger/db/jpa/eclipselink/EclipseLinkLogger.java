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
package com.helger.db.jpa.eclipselink;

import javax.annotation.Nonnull;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.string.StringHelper;
import com.helger.commons.system.ENewLineMode;

/**
 * A logging adapter that can be hooked into JPA and forwards all logging
 * requests to SLF4J.
 *
 * @author Philip Helger
 */
public class EclipseLinkLogger extends AbstractSessionLog
{
  private static final Logger LOGGER = LoggerFactory.getLogger (EclipseLinkLogger.class);

  @Override
  public void log (@Nonnull final SessionLogEntry aSessionLogEntry)
  {
    final int nLogLevel = aSessionLogEntry.getLevel ();
    if (!shouldLog (nLogLevel))
      return;

    // JPA uses the System property for adding line breaks
    final ICommonsList <String> aMsgLines = StringHelper.getExploded (ENewLineMode.DEFAULT.getText (),
                                                                      formatMessage (aSessionLogEntry));
    final int nMaxIndex = aMsgLines.size ();
    for (int i = 0; i < nMaxIndex; ++i)
    {
      final String sMsg = aMsgLines.get (i);
      final Throwable t = i == nMaxIndex - 1 ? aSessionLogEntry.getException () : null;
      if (nLogLevel >= SessionLog.SEVERE)
        LOGGER.error (sMsg, t);
      else
        if (nLogLevel >= SessionLog.WARNING)
          LOGGER.warn (sMsg, t);
        else
          if (nLogLevel >= SessionLog.CONFIG || GlobalDebug.isDebugMode ())
          {
            if (LOGGER.isInfoEnabled ())
              LOGGER.info (sMsg, t);
          }
          else
          {
            if (LOGGER.isDebugEnabled ())
              LOGGER.debug (sMsg, t);
          }
    }
  }
}
