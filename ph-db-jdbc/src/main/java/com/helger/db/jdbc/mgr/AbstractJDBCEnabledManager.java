/*
 * Copyright (C) 2014-2023 Philip Helger (www.helger.com)
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
package com.helger.db.jdbc.mgr;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.db.jdbc.executor.DBExecutor;

/**
 * Base class for JDBC enabled managers.
 *
 * @author Philip Helger
 * @since 6.7.3
 */
public abstract class AbstractJDBCEnabledManager
{
  private final Supplier <? extends DBExecutor> m_aDBExecSupplier;

  /**
   * Constructor
   *
   * @param aDBExecSupplier
   *        The supplier for {@link DBExecutor} objects. May not be
   *        <code>null</code>.
   */
  protected AbstractJDBCEnabledManager (@Nonnull final Supplier <? extends DBExecutor> aDBExecSupplier)
  {
    ValueEnforcer.notNull (aDBExecSupplier, "DBExecSupplier");
    m_aDBExecSupplier = aDBExecSupplier;
  }

  /**
   * @return A new DB executor from the Supplier provided in the constructor.
   *         Never <code>null</code>.
   */
  @Nonnull
  protected final DBExecutor newExecutor ()
  {
    final DBExecutor ret = m_aDBExecSupplier.get ();
    if (ret == null)
      throw new IllegalStateException ("The contained DBExecutor Supplier returned null!");
    return ret;
  }
}
