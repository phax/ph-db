package com.helger.db.jdbc.executor;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import oracle.sql.TIMESTAMP;

/**
 * This class is only loaded when Oracle JDBC types are used. It provides custom type converters for
 * Oracle.
 *
 * @author Philip Helger
 * @since 8.1.1
 */
final class DBOracleHelper
{
  private static final class Singleton
  {
    static final DBOracleHelper INSTANCE = new DBOracleHelper ();
  }

  private DBOracleHelper ()
  {}

  @NonNull
  public static DBOracleHelper getInstance ()
  {
    return Singleton.INSTANCE;
  }

  @Nullable
  public LocalDateTime getAsLocalDateTime (@Nullable final Object o)
  {
    try
    {
      if (o instanceof final TIMESTAMP aTS)
        return aTS.toLocalDateTime ();
    }
    catch (final SQLException ex)
    {
      // fall through
    }
    return null;
  }

  @Nullable
  public Timestamp getAsTimestamp (@Nullable final Object o)
  {
    try
    {
      if (o instanceof final TIMESTAMP aTS)
        return aTS.timestampValue ();
    }
    catch (final SQLException ex)
    {
      // fall through
    }
    return null;
  }
}
