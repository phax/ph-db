/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.db.jdbc.db2;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.db.api.CJDBC_DB2;
import com.helger.db.jdbc.AbstractConnector;

/**
 * Abstract DB connector for DB2
 *
 * @author Philip Helger
 */
public abstract class AbstractDB2Connector extends AbstractConnector
{
  public AbstractDB2Connector ()
  {}

  @Override
  @Nonnull
  @Nonempty
  protected String getJDBCDriverClassName ()
  {
    return CJDBC_DB2.DEFAULT_JDBC_DRIVER_CLASS_NAME;
  }

  @Override
  @Nonnull
  public final String getConnectionUrl ()
  {
    final StringBuilder ret = new StringBuilder (CJDBC_DB2.CONNECTION_PREFIX);
    ret.append (getDatabaseName ());
    return ret.toString ();
  }
}
