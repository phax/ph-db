package com.helger.db.jdbc.executor;

import java.sql.SQLException;

import javax.annotation.Nonnull;

public class DBNoConnectionException extends SQLException
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
