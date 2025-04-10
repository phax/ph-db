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
package com.helger.db.jdbc.executor;

import java.sql.SQLException;

import javax.annotation.Nonnull;

/**
 * A special exception that is thrown to indicate that no Connection could be
 * established.
 *
 * @author Philip Helger
 * @since 6.2.0
 */
public class DBNoConnectionException extends Exception
{
  public DBNoConnectionException (@Nonnull final String sMessage)
  {
    super (sMessage);
  }

  public DBNoConnectionException (@Nonnull final String sMessage, @Nonnull final SQLException aCause)
  {
    super (sMessage, aCause);
  }
}
