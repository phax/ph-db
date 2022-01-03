/*
 * Copyright (C) 2014-2022 Philip Helger (www.helger.com)
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
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.stream.NonBlockingBufferedWriter;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.state.ESuccess;
import com.helger.db.api.CJDBC_H2;
import com.helger.db.jdbc.AbstractDBConnector;
import com.helger.db.jdbc.executor.DBExecutor;

/**
 * Base DB connector for H2 databases
 *
 * @author Philip Helger
 */
@ThreadSafe
public abstract class AbstractH2Connector extends AbstractDBConnector
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractH2Connector.class);

  public AbstractH2Connector ()
  {}

  @Override
  @Nonnull
  @Nonempty
  protected final String getJDBCDriverClassName ()
  {
    return CJDBC_H2.DEFAULT_JDBC_DRIVER_CLASS_NAME;
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
      LOGGER.info ("Dumping database to OutputStream");
      try (final PrintWriter aPrintWriter = new PrintWriter (new NonBlockingBufferedWriter (StreamHelper.createWriter (aOS,
                                                                                                                       StandardCharsets.UTF_8))))
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

    LOGGER.info ("Backing up database to " + fDestFile);
    final DBExecutor aExecutor = new DBExecutor (this);
    return aExecutor.executeStatement ("BACKUP TO '" + fDestFile.getAbsolutePath () + "'");
  }
}
