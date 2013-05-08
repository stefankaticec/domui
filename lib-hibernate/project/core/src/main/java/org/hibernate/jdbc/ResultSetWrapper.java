/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 *
 */
package org.hibernate.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * A ResultSet delegate, responsible for locally caching the columnName-to-columnIndex
 * resolution that has been found to be inefficient in a few vendor's drivers (i.e., Oracle
 * and Postgres).
 *
 * @author Steve Ebersole
 */
public class ResultSetWrapper implements ResultSet {

	private ResultSet rs;
	private ColumnNameCache columnNameCache;

	public ResultSetWrapper(ResultSet resultSet, ColumnNameCache columnNameCache) {
		this.rs = resultSet;
		this.columnNameCache = columnNameCache;
	}

	/*package*/ ResultSet getTarget() {
		return rs;
	}


	// ResultSet impl ("overridden") ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Overridden version to utilize local caching of the column indexes by name
	 * to improve performance for those drivers which are known to not support
	 * such caching by themselves.
	 * <p/>
	 * This implementation performs the caching based on the upper case version
	 * of the given column name.
	 *
	 * @param columnName The column name to resolve into an index.
	 * @return The column index corresponding to the given column name.
	 * @throws SQLException - if the ResultSet object does not contain
	 * columnName or a database access error occurs
	 */
	public int findColumn(String columnName) throws SQLException {
		return columnNameCache.getIndexForColumnName( columnName, this );
	}

	public Array getArray(String colName) throws SQLException {
		return rs.getArray( findColumn(colName) );
	}

	public void updateArray(String columnName, Array x) throws SQLException {
		rs.updateArray( findColumn(columnName), x );
	}

	public InputStream getAsciiStream(String columnName) throws SQLException {
		return rs.getAsciiStream( findColumn(columnName) );
	}

	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
		rs.updateAsciiStream( findColumn(columnName), x, length );
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return rs.getBigDecimal( findColumn(columnName) );
	}

	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		return rs.getBigDecimal( findColumn(columnName), scale );
	}

	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		rs.updateBigDecimal( findColumn(columnName), x );
	}

	public InputStream getBinaryStream(String columnName) throws SQLException {
		return rs.getBinaryStream( findColumn(columnName) );
	}

	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
		rs.updateBinaryStream( findColumn(columnName), x, length );
	}

	public Blob getBlob(String columnName) throws SQLException {
		return rs.getBlob( findColumn(columnName) );
	}

	public void updateBlob(String columnName, Blob x) throws SQLException {
		rs.updateBlob( findColumn(columnName), x );
	}

	public boolean getBoolean(String columnName) throws SQLException {
		return rs.getBoolean( findColumn(columnName) );
	}

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		rs.updateBoolean( findColumn(columnName), x );
	}

	public byte getByte(String columnName) throws SQLException {
		return rs.getByte( findColumn(columnName) );
	}

	public void updateByte(String columnName, byte x) throws SQLException {
		rs.updateByte( findColumn(columnName), x );
	}

	public byte[] getBytes(String columnName) throws SQLException {
		return rs.getBytes( findColumn(columnName) );
	}

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		rs.updateBytes( findColumn(columnName), x );
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		return rs.getCharacterStream( findColumn(columnName) );
	}

	public void updateCharacterStream(String columnName, Reader x, int length) throws SQLException {
		rs.updateCharacterStream( findColumn(columnName), x, length );
	}

	public Clob getClob(String columnName) throws SQLException {
		return rs.getClob( findColumn(columnName) );
	}

	public void updateClob(String columnName, Clob x) throws SQLException {
		rs.updateClob( findColumn(columnName), x );
	}

	public Date getDate(String columnName) throws SQLException {
		return rs.getDate( findColumn(columnName) );
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		return rs.getDate( findColumn(columnName), cal );
	}

	public void updateDate(String columnName, Date x) throws SQLException {
		rs.updateDate( findColumn(columnName), x );
	}

	public double getDouble(String columnName) throws SQLException {
		return rs.getDouble( findColumn(columnName) );
	}

	public void updateDouble(String columnName, double x) throws SQLException {
		rs.updateDouble( findColumn(columnName), x );
	}

	public float getFloat(String columnName) throws SQLException {
		return rs.getFloat( findColumn(columnName) );
	}

	public void updateFloat(String columnName, float x) throws SQLException {
		rs.updateFloat( findColumn(columnName), x );
	}

	public int getInt(String columnName) throws SQLException {
		return rs.getInt( findColumn(columnName) );
	}

	public void updateInt(String columnName, int x) throws SQLException {
		rs.updateInt( findColumn(columnName), x );
	}

	public long getLong(String columnName) throws SQLException {
		return rs.getLong( findColumn(columnName) );
	}

	public void updateLong(String columnName, long x) throws SQLException {
		rs.updateLong( findColumn(columnName), x );
	}

	public Object getObject(String columnName) throws SQLException {
		return rs.getObject( findColumn(columnName) );
	}

	public Object getObject(String columnName, Map map) throws SQLException {
		return rs.getObject( findColumn(columnName), map );
	}

	public void updateObject(String columnName, Object x) throws SQLException {
		rs.updateObject( findColumn(columnName), x );
	}

	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		rs.updateObject( findColumn(columnName), x, scale );
	}

	public Ref getRef(String columnName) throws SQLException {
		return rs.getRef( findColumn(columnName) );
	}

	public void updateRef(String columnName, Ref x) throws SQLException {
		rs.updateRef( findColumn(columnName), x );
	}

	public short getShort(String columnName) throws SQLException {
		return rs.getShort( findColumn(columnName) );
	}

	public void updateShort(String columnName, short x) throws SQLException {
		rs.updateShort( findColumn(columnName), x );
	}

	public String getString(String columnName) throws SQLException {
		return rs.getString( findColumn(columnName) );
	}

	public void updateString(String columnName, String x) throws SQLException {
		rs.updateString( findColumn(columnName), x );
	}

	public Time getTime(String columnName) throws SQLException {
		return rs.getTime( findColumn(columnName) );
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		return rs.getTime( findColumn(columnName), cal );
	}

	public void updateTime(String columnName, Time x) throws SQLException {
		rs.updateTime( findColumn(columnName), x );
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		return rs.getTimestamp( findColumn(columnName) );
	}

	public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		rs.updateTimestamp( findColumn(columnName), x );
	}

	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
		return rs.getTimestamp( findColumn(columnName), cal );
	}

	public InputStream getUnicodeStream(String columnName) throws SQLException {
		return rs.getUnicodeStream( findColumn(columnName) );
	}

	public URL getURL(String columnName) throws SQLException {
		return rs.getURL( findColumn(columnName) );
	}

	public void updateNull(String columnName) throws SQLException {
		rs.updateNull( findColumn(columnName) );
	}


	// ResultSet impl (delegated) ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public int getConcurrency() throws SQLException {
		return rs.getConcurrency();
	}

	public int getFetchDirection() throws SQLException {
		return rs.getFetchDirection();
	}

	public int getFetchSize() throws SQLException {
		return rs.getFetchSize();
	}

	public int getRow() throws SQLException {
		return rs.getRow();
	}

	public int getType() throws SQLException {
		return rs.getType();
	}

	public void afterLast() throws SQLException {
		rs.afterLast();
	}

	public void beforeFirst() throws SQLException {
		rs.beforeFirst();
	}

	public void cancelRowUpdates() throws SQLException {
		rs.cancelRowUpdates();
	}

	public void clearWarnings() throws SQLException {
		rs.clearWarnings();
	}

	public void close() throws SQLException {
		rs.close();
	}

	public void deleteRow() throws SQLException {
		rs.deleteRow();
	}

	public void insertRow() throws SQLException {
		rs.insertRow();
	}

	public void moveToCurrentRow() throws SQLException {
		rs.moveToCurrentRow();
	}

	public void moveToInsertRow() throws SQLException {
		rs.moveToInsertRow();
	}

	public void refreshRow() throws SQLException {
		rs.refreshRow();
	}

	public void updateRow() throws SQLException {
		rs.updateRow();
	}

	public boolean first() throws SQLException {
		return rs.first();
	}

	public boolean isAfterLast() throws SQLException {
		return rs.isAfterLast();
	}

	public boolean isBeforeFirst() throws SQLException {
		return rs.isBeforeFirst();
	}

	public boolean isFirst() throws SQLException {
		return rs.isFirst();
	}

	public boolean isLast() throws SQLException {
		return rs.isLast();
	}

	public boolean last() throws SQLException {
		return rs.last();
	}

	public boolean next() throws SQLException {
		return rs.next();
	}

	public boolean previous() throws SQLException {
		return rs.previous();
	}

	public boolean rowDeleted() throws SQLException {
		return rs.rowDeleted();
	}

	public boolean rowInserted() throws SQLException {
		return rs.rowInserted();
	}

	public boolean rowUpdated() throws SQLException {
		return rs.rowUpdated();
	}

	public boolean wasNull() throws SQLException {
		return rs.wasNull();
	}

	public byte getByte(int columnIndex) throws SQLException {
		return rs.getByte(columnIndex);
	}

	public double getDouble(int columnIndex) throws SQLException {
		return rs.getDouble(columnIndex);
	}

	public float getFloat(int columnIndex) throws SQLException {
		return rs.getFloat(columnIndex);
	}

	public int getInt(int columnIndex) throws SQLException {
		return rs.getInt(columnIndex);
	}

	public long getLong(int columnIndex) throws SQLException {
		return rs.getLong(columnIndex);
	}

	public short getShort(int columnIndex) throws SQLException {
		return rs.getShort(columnIndex);
	}

	public void setFetchDirection(int direction) throws SQLException {
		rs.setFetchDirection(direction);
	}

	public void setFetchSize(int rows) throws SQLException {
		rs.setFetchSize(rows);
	}

	public void updateNull(int columnIndex) throws SQLException {
		rs.updateNull(columnIndex);
	}

	public boolean absolute(int row) throws SQLException {
		return rs.absolute(row);
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		return rs.getBoolean(columnIndex);
	}

	public boolean relative(int rows) throws SQLException {
		return rs.relative(rows);
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return rs.getBytes(columnIndex);
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		rs.updateByte(columnIndex, x);
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		rs.updateDouble(columnIndex, x);
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		rs.updateFloat(columnIndex, x);
	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		rs.updateInt(columnIndex, x);
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		rs.updateLong(columnIndex, x);
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		rs.updateShort(columnIndex, x);
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		rs.updateBoolean(columnIndex, x);
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		rs.updateBytes(columnIndex, x);
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return rs.getAsciiStream(columnIndex);
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return rs.getBinaryStream(columnIndex);
	}

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return rs.getUnicodeStream(columnIndex);
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		rs.updateAsciiStream(columnIndex, x, length);
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		rs.updateBinaryStream(columnIndex, x, length);
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return rs.getCharacterStream(columnIndex);
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		rs.updateCharacterStream(columnIndex, x, length);
	}

	public Object getObject(int columnIndex) throws SQLException {
		return rs.getObject(columnIndex);
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		rs.updateObject(columnIndex, x);
	}

	public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
		rs.updateObject(columnIndex, x, scale);
	}

	public String getCursorName() throws SQLException {
		return rs.getCursorName();
	}

	public String getString(int columnIndex) throws SQLException {
		return rs.getString(columnIndex);
	}

	public void updateString(int columnIndex, String x) throws SQLException {
		rs.updateString(columnIndex, x);
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return rs.getBigDecimal(columnIndex);
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return rs.getBigDecimal(columnIndex, scale);
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		rs.updateBigDecimal(columnIndex, x);
	}

	public URL getURL(int columnIndex) throws SQLException {
		return rs.getURL(columnIndex);
	}

	public Array getArray(int columnIndex) throws SQLException {
		return rs.getArray(columnIndex);
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		rs.updateArray(columnIndex, x);
	}

	public Blob getBlob(int columnIndex) throws SQLException {
		return rs.getBlob(columnIndex);
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		rs.updateBlob(columnIndex, x);
	}

	public Clob getClob(int columnIndex) throws SQLException {
		return rs.getClob(columnIndex);
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		rs.updateClob(columnIndex, x);
	}

	public Date getDate(int columnIndex) throws SQLException {
		return rs.getDate(columnIndex);
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		rs.updateDate(columnIndex, x);
	}

	public Ref getRef(int columnIndex) throws SQLException {
		return rs.getRef(columnIndex);
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		rs.updateRef(columnIndex, x);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return rs.getMetaData();
	}

	public SQLWarning getWarnings() throws SQLException {
		return rs.getWarnings();
	}

	public Statement getStatement() throws SQLException {
		return rs.getStatement();
	}

	public Time getTime(int columnIndex) throws SQLException {
		return rs.getTime(columnIndex);
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		rs.updateTime(columnIndex, x);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return rs.getTimestamp(columnIndex);
	}

	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		rs.updateTimestamp(columnIndex, x);
	}

	public Object getObject(int columnIndex, Map map) throws SQLException {
		return rs.getObject( columnIndex, map );
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return rs.getDate(columnIndex, cal);
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return rs.getTime(columnIndex, cal);
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return rs.getTimestamp(columnIndex, cal);
	}

	/**** new jdk6 ****/

	public int getHoldability() throws SQLException {
		return rs.getHoldability();
	}

	public Reader getNCharacterStream(int arg0) throws SQLException {
		return rs.getNCharacterStream(arg0);
	}

	public Reader getNCharacterStream(String arg0) throws SQLException {
		return rs.getNCharacterStream(findColumn(arg0));
	}

	public NClob getNClob(int arg0) throws SQLException {
		return rs.getNClob(arg0);
	}

	public NClob getNClob(String arg0) throws SQLException {
		return rs.getNClob(findColumn(arg0));
	}

	public String getNString(int arg0) throws SQLException {
		return rs.getNString(arg0);
	}

	public String getNString(String arg0) throws SQLException {
		return rs.getNString(findColumn(arg0));
	}

	public RowId getRowId(int arg0) throws SQLException {
		return rs.getRowId(arg0);
	}

	public RowId getRowId(String arg0) throws SQLException {
		return rs.getRowId(findColumn(arg0));
	}

	public SQLXML getSQLXML(int arg0) throws SQLException {
		return rs.getSQLXML(arg0);
	}

	public SQLXML getSQLXML(String arg0) throws SQLException {
		return getSQLXML(arg0);
	}

	public boolean isClosed() throws SQLException {
		return rs.isClosed();
	}

	public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		rs.updateAsciiStream(arg0, arg1, arg2);
	}

	public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
		updateAsciiStream(arg0, arg1);
	}

	public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
		rs.updateAsciiStream(findColumn(arg0), arg1, arg2);
	}

	public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
		rs.updateAsciiStream(findColumn(arg0), arg1);
	}

	public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		rs.updateBinaryStream(arg0, arg1, arg2);
	}

	public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
		rs.updateBinaryStream(arg0, arg1);
	}

	public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
		rs.updateBinaryStream(findColumn(arg0), arg1);
	}

	public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
		rs.updateBinaryStream(findColumn(arg0), arg1);
	}

	public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
		rs.updateBlob(arg0, arg1, arg2);
	}

	public boolean isWrapperFor(Class< ? > iface) throws SQLException {
		return rs.isWrapperFor(iface);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return rs.unwrap(iface);
	}

	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		rs.updateBlob(columnIndex, inputStream);
	}

	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		rs.updateBlob(findColumn(columnLabel), inputStream, length);
	}

	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		rs.updateBlob(findColumn(columnLabel), inputStream);
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		rs.updateCharacterStream(columnIndex, x, length);
	}

	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		rs.updateCharacterStream(columnIndex, x);
	}

	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		rs.updateCharacterStream(findColumn(columnLabel), reader, length);
	}

	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		rs.updateCharacterStream(findColumn(columnLabel), reader);
	}

	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		rs.updateClob(columnIndex, reader, length);
	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		rs.updateClob(columnIndex, reader);
	}

	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		rs.updateClob(findColumn(columnLabel), reader, length);
	}

	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		rs.updateClob(findColumn(columnLabel), reader);
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		rs.updateNCharacterStream(columnIndex, x, length);
	}

	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		rs.updateNCharacterStream(columnIndex, x);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		rs.updateNCharacterStream(findColumn(columnLabel), reader, length);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		rs.updateNCharacterStream(findColumn(columnLabel), reader);
	}

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		rs.updateNClob(columnIndex, nClob);
	}

	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		rs.updateNClob(columnIndex, reader, length);
	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		rs.updateNClob(columnIndex, reader);
	}

	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		rs.updateNClob(findColumn(columnLabel), nClob);
	}

	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		rs.updateNClob(findColumn(columnLabel), reader, length);
	}

	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		rs.updateNClob(findColumn(columnLabel), reader);
	}

	public void updateNString(int columnIndex, String nString) throws SQLException {
		rs.updateNString(columnIndex, nString);
	}

	public void updateNString(String columnLabel, String nString) throws SQLException {
		rs.updateNString(findColumn(columnLabel), nString);
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		rs.updateRowId(columnIndex, x);
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		rs.updateRowId(findColumn(columnLabel), x);
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		rs.updateSQLXML(columnIndex, xmlObject);
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		rs.updateSQLXML(findColumn(columnLabel), xmlObject);
	}
}
