/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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

import java.sql.Driver;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.Nonempty;
import com.helger.annotation.style.UsedViaReflection;
import com.helger.scope.IScope;
import com.helger.scope.singleton.AbstractGlobalSingleton;

/**
 * A wrapper around the H2 driver, that gets automatically deregistered, when the global scope is
 * closed.
 *
 * @author Philip Helger
 */
public final class H2DriverSingleton extends AbstractGlobalSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (H2DriverSingleton.class);

  /**
   * Invoked via reflection.
   *
   * @deprecated to call it directly
   */
  @Deprecated (forRemoval = false)
  @UsedViaReflection
  public H2DriverSingleton ()
  {
    LOGGER.info ("Loading org.h2.Driver");
    org.h2.Driver.load ();
  }

  @NonNull
  public static H2DriverSingleton getInstance ()
  {
    return getGlobalSingleton (H2DriverSingleton.class);
  }

  @NonNull
  public Class <? extends Driver> getDriverClass ()
  {
    return org.h2.Driver.class;
  }

  @NonNull
  @Nonempty
  public String getDriverClassName ()
  {
    return getDriverClass ().getName ();
  }

  @Override
  protected void onDestroy (@NonNull final IScope aScopeInDestruction) throws Exception
  {
    org.h2.Driver.unload ();
    LOGGER.info ("Unloaded org.h2.Driver");
  }
}
