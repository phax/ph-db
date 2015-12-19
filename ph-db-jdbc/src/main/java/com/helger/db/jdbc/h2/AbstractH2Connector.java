/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.ThreadSafe;

import org.h2.api.DatabaseEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.charset.CCharset;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.stream.NonBlockingBufferedWriter;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.state.ESuccess;
import com.helger.db.api.CJDBC_H2;
import com.helger.db.api.h2.LoggingH2EventListener;
import com.helger.db.jdbc.AbstractConnector;
import com.helger.db.jdbc.executor.DBExecutor;

/**
 * Base DB connector for H2 databases
 *
 * @author Philip Helger
 */
@ThreadSafe
public abstract class AbstractH2Connector extends AbstractConnector
{
  /** Default trace level file: 1 */
  public static final int DEFAULT_TRACE_LEVEL_FILE = 1;
  /** Default trace level system.out: 0 */
  public static final int DEFAULT_TRACE_LEVEL_SYSOUT = 0;
  /** Default close on exit: true */
  public static final boolean DEFAULT_CLOSE_ON_EXIT = true;
  private static final Logger s_aLogger = LoggerFactory.getLogger (AbstractH2Connector.class);

  private int m_nTraceLevelFile = DEFAULT_TRACE_LEVEL_FILE;
  private int m_nTraceLevelSysOut = DEFAULT_TRACE_LEVEL_SYSOUT;
  private Class <? extends DatabaseEventListener> m_aEventListenerClass = LoggingH2EventListener.class;
  private boolean m_bCloseOnExit = DEFAULT_CLOSE_ON_EXIT;

  public AbstractH2Connector ()
  {}

  @Override
  @Nonnull
  @Nonempty
  protected final String getJDBCDriverClassName ()
  {
    return CJDBC_H2.DEFAULT_JDBC_DRIVER_CLASS_NAME;
  }

  public final int getTraceLevelFile ()
  {
    getLock ().lock ();
    try
    {
      return m_nTraceLevelFile;
    }
    finally
    {
      getLock ().unlock ();
    }
  }

  public final void setTraceLevelFile (final int nTraceLevelFile)
  {
    getLock ().lock ();
    try
    {
      m_nTraceLevelFile = nTraceLevelFile;
    }
    finally
    {
      getLock ().unlock ();
    }
  }

  public final int getTraceLevelSysOut ()
  {
    getLock ().lock ();
    try
    {
      return m_nTraceLevelSysOut;
    }
    finally
    {
      getLock ().unlock ();
    }
  }

  public final void setTraceLevelSysOut (final int nTraceLevelSysOut)
  {
    getLock ().lock ();
    try
    {
      m_nTraceLevelSysOut = nTraceLevelSysOut;
    }
    finally
    {
      getLock ().unlock ();
    }
  }

  @Nullable
  public final Class <? extends DatabaseEventListener> getEventListenerClass ()
  {
    getLock ().lock ();
    try
    {
      return m_aEventListenerClass;
    }
    finally
    {
      getLock ().unlock ();
    }
  }

  public final void setEventListenerClass (@Nullable final Class <? extends DatabaseEventListener> aEventListenerClass)
  {
    getLock ().lock ();
    try
    {
      m_aEventListenerClass = aEventListenerClass;
    }
    finally
    {
      getLock ().unlock ();
    }
  }

  public final boolean isCloseOnExit ()
  {
    getLock ().lock ();
    try
    {
      return m_bCloseOnExit;
    }
    finally
    {
      getLock ().unlock ();
    }
  }

  public final void setCloseOnExit (final boolean bCloseOnExit)
  {
    getLock ().lock ();
    try
    {
      m_bCloseOnExit = bCloseOnExit;
    }
    finally
    {
      getLock ().unlock ();
    }
  }

  @Override
  @Nonnull
  public final String getConnectionUrl ()
  {
    final StringBuilder ret = new StringBuilder (CJDBC_H2.CONNECTION_PREFIX);
    ret.append (getDatabaseName ());
    if (m_nTraceLevelFile != DEFAULT_TRACE_LEVEL_FILE)
      ret.append (";TRACE_LEVEL_FILE=").append (m_nTraceLevelFile);
    if (m_nTraceLevelSysOut != DEFAULT_TRACE_LEVEL_SYSOUT)
      ret.append (";TRACE_LEVEL_SYSTEM_OUT=").append (m_nTraceLevelSysOut);
    if (m_aEventListenerClass != null)
      ret.append (";DATABASE_EVENT_LISTENER='").append (m_aEventListenerClass.getName ()).append ("'");
    if (m_bCloseOnExit != DEFAULT_CLOSE_ON_EXIT)
      ret.append (";DB_CLOSE_ON_EXIT=").append (Boolean.toString (m_bCloseOnExit).toUpperCase (Locale.US));
    return ret.toString ();
  }

  @Nonnull
  public final ESuccess dumpDatabase (@Nonnull final File aFile)
  {
    return dumpDatabase (FileHelper.getOutputStream (aFile));
  }

  /**
   * Dump the database to the passed output stream and closed the passed output
   * stream.
   *
   * @param aOS
   *        The output stream to dump the DB content to. May not be
   *        <code>null</code>. Automatically closed when done.
   * @return <code>true</code> upon success, <code>false</code> if an error
   *         occurred.
   */
  @Nonnull
  public final ESuccess dumpDatabase (@Nonnull @WillClose final OutputStream aOS)
  {
    ValueEnforcer.notNull (aOS, "OutputStream");

    // Save the DB data to an SQL file
    try
    {
      s_aLogger.info ("Dumping database '" + getDatabaseName () + "' to OutputStream");
      final PrintWriter aPrintWriter = new PrintWriter (new NonBlockingBufferedWriter (StreamHelper.createWriter (aOS,
                                                                                                                  CCharset.CHARSET_UTF_8_OBJ)));
      try
      {
        final DBExecutor aExecutor = new DBExecutor (this);
        final ESuccess ret = aExecutor.queryAll ("SCRIPT SIMPLE", aCurrentObject -> {
          if (aCurrentObject != null)
          {
            // The value of the first column is the script line
            aPrintWriter.println (aCurrentObject.get (0).getValue ());
          }
        });
        aPrintWriter.flush ();
        return ret;
      }
      finally
      {
        StreamHelper.close (aPrintWriter);
      }
    }
    finally
    {
      StreamHelper.close (aOS);
    }
  }

  /**
   * Create a backup file. The file is a ZIP file.
   *
   * @param fDestFile
   *        Destination filename. May not be <code>null</code>.
   * @return {@link ESuccess}
   */
  @Nonnull
  public final ESuccess createBackup (@Nonnull final File fDestFile)
  {
    ValueEnforcer.notNull (fDestFile, "DestFile");

    s_aLogger.info ("Backing up database '" + getDatabaseName () + "' to " + fDestFile);
    final DBExecutor aExecutor = new DBExecutor (this);
    return aExecutor.executeStatement ("BACKUP TO '" + fDestFile.getAbsolutePath () + "'");
  }
}
