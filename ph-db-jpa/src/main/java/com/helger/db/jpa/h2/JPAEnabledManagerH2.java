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
package com.helger.db.jpa.h2;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.db.api.h2.EH2LockMode;
import com.helger.db.api.h2.EH2Log;
import com.helger.db.jpa.IHasEntityManager;
import com.helger.db.jpa.JPAEnabledManager;

/**
 * Special H2 version of {@link JPAEnabledManager}
 *
 * @author Philip Helger
 */
@ThreadSafe
public class JPAEnabledManagerH2 extends JPAEnabledManager
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (JPAEnabledManager.class);

  public JPAEnabledManagerH2 (@Nonnull final IHasEntityManager aEntityManagerProvider)
  {
    super (aEntityManagerProvider);
  }

  public final boolean isTableExisting (@Nonnull final String sTableName)
  {
    return getSelectCountResultObj (getEntityManager ().createQuery ("SELECT count(ID) FROM TABLES t WHERE t.TABLE_TYPE = 'TABLE' AND TABLE_NAME = :tablename",
                                                                     Integer.class)
                                                       .setParameter ("tablename", sTableName)).intValue () > 0;
  }

  private void _executeH2Native (@Nonnull @Nonempty final String sNativeSQL)
  {
    doInTransaction ((Runnable) () -> {
      s_aLogger.info ("Running H2 native command: " + sNativeSQL);
      getEntityManager ().createNativeQuery (sNativeSQL).executeUpdate ();
    });
  }

  public final void doH2Analyze ()
  {
    _executeH2Native ("ANALYZE");
  }

  public final void setH2LockMode (@Nonnull final EH2LockMode eLockMode)
  {
    _executeH2Native ("SET LOCK_MODE=" + eLockMode.getID ());
  }

  public final void setH2Log (@Nonnull final EH2Log eLog)
  {
    _executeH2Native ("SET LOG=" + eLog.getID ());
  }

  public final void setH2UndoLog (final boolean bEnabled)
  {
    _executeH2Native ("SET UNDO_LOG=" + (bEnabled ? "1" : "0"));
  }
}
