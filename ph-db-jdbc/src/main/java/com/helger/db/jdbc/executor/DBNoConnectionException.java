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
