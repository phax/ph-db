/**
 * Copyright (C) 2014-2020 Philip Helger (www.helger.com)
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.Time;
import java.sql.Timestamp;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.lang.ICloneable;
import com.helger.commons.string.ToStringGenerator;

/**
 * Represents a single DB query result row.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class DBResultRow implements ICloneable <DBResultRow>, Serializable
{
  private final DBResultField [] m_aCols;
  private int m_nIndex;

  private DBResultRow (@Nonnull final DBResultRow aOther)
  {
    m_aCols = ArrayHelper.getCopy (aOther.m_aCols);
    m_nIndex = aOther.m_nIndex;
  }

  public DBResultRow (@Nonnegative final int nCols)
  {
    m_aCols = new DBResultField [nCols];
    m_nIndex = 0;
  }

  protected void internalClear ()
  {
    for (int i = 0; i < m_aCols.length; ++i)
      m_aCols[i] = null;
    m_nIndex = 0;
  }

  protected void internalAdd (@Nonnull final DBResultField aResultField)
  {
    ValueEnforcer.notNull (aResultField, "ResultField");

    m_aCols[m_nIndex++] = aResultField;
  }

  @Nonnegative
  public int getUsedColumnIndex ()
  {
    return m_nIndex;
  }

  @Nonnegative
  public int getColumnCount ()
  {
    return m_aCols.length;
  }

  @Nullable
  public DBResultField get (@Nonnegative final int nIndex) throws ArrayIndexOutOfBoundsException
  {
    return m_aCols[nIndex];
  }

  public int getColumnType (@Nonnegative final int nIndex)
  {
    return get (nIndex).getColumnType ();
  }

  @Nullable
  public String getColumnTypeName (@Nonnegative final int nIndex)
  {
    return get (nIndex).getColumnTypeName ();
  }

  @Nonnull
  @Nonempty
  public String getColumnName (@Nonnegative final int nIndex)
  {
    return get (nIndex).getColumnName ();
  }

  @Nullable
  public Object getValue (@Nonnegative final int nIndex)
  {
    return get (nIndex).getValue ();
  }

  @Nullable
  public String getAsString (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsString ();
  }

  @Nullable
  public BigDecimal getAsBigDecimal (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsBigDecimal ();
  }

  @Nullable
  public BigInteger getAsBigInteger (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsBigInteger ();
  }

  public boolean getAsBoolean (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsBoolean ();
  }

  public byte getAsByte (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsByte ();
  }

  public byte getAsByte (@Nonnegative final int nIndex, final byte nDefault)
  {
    return get (nIndex).getAsByte (nDefault);
  }

  @Nullable
  public byte [] getAsByteArray (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsByteArray ();
  }

  public char getAsChar (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsChar ();
  }

  public char getAsChar (@Nonnegative final int nIndex, final char cDefault)
  {
    return get (nIndex).getAsChar (cDefault);
  }

  public double getAsDouble (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsDouble ();
  }

  public double getAsDouble (@Nonnegative final int nIndex, final double dDefault)
  {
    return get (nIndex).getAsDouble (dDefault);
  }

  public float getAsFloat (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsFloat ();
  }

  public float getAsFloat (@Nonnegative final int nIndex, final float fDefault)
  {
    return get (nIndex).getAsFloat (fDefault);
  }

  public int getAsInt (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsInt ();
  }

  public int getAsInt (@Nonnegative final int nIndex, final int nDefault)
  {
    return get (nIndex).getAsInt (nDefault);
  }

  public long getAsLong (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsLong ();
  }

  public long getAsLong (@Nonnegative final int nIndex, final long nDefault)
  {
    return get (nIndex).getAsLong (nDefault);
  }

  public short getAsShort (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsShort ();
  }

  public short getAsShort (@Nonnegative final int nIndex, final short nDefault)
  {
    return get (nIndex).getAsShort (nDefault);
  }

  @Nullable
  public Boolean getAsBooleanObj (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsBooleanObj ();
  }

  @Nullable
  public Byte getAsByteObj (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsByteObj ();
  }

  @Nullable
  public Character getAsCharObj (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsCharObj ();
  }

  @Nullable
  public Double getAsDoubleObj (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsDoubleObj ();
  }

  @Nullable
  public Float getAsFloatObj (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsFloatObj ();
  }

  @Nullable
  public Integer getAsIntObj (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsIntObj ();
  }

  @Nullable
  public Long getAsLongObj (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsLongObj ();
  }

  @Nullable
  public Short getAsShortObj (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsShortObj ();
  }

  @Nullable
  public Blob getAsBlob (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsSqlBlob ();
  }

  @Nullable
  public Clob getAsClob (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsSqlClob ();
  }

  @Nullable
  public Date getAsDate (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsSqlDate ();
  }

  @Nullable
  public NClob getAsNClob (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsSqlNClob ();
  }

  @Nullable
  public RowId getAsRowId (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsSqlRowId ();
  }

  @Nullable
  public Time getAsTime (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsSqlTime ();
  }

  @Nullable
  public Timestamp getAsTimestamp (@Nonnegative final int nIndex)
  {
    return get (nIndex).getAsSqlTimestamp ();
  }

  /**
   * @return A map that contains the mapping from column name to the respective
   *         index
   */
  @Nonnull
  @ReturnsMutableCopy
  public ICommonsMap <String, Integer> getColumnNameToIndexMap ()
  {
    final ICommonsMap <String, Integer> ret = new CommonsHashMap <> ();
    for (int i = 0; i < m_aCols.length; ++i)
      ret.put (m_aCols[i].getColumnName (), Integer.valueOf (i));
    return ret;
  }

  @Nonnull
  public DBResultRow getClone ()
  {
    return new DBResultRow (this);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("cols", m_aCols).append ("index", m_nIndex).getToString ();
  }
}
