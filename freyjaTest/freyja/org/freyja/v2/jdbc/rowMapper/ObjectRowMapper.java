package org.freyja.v2.jdbc.rowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

public class ObjectRowMapper implements RowMapper<Object> {
	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		Object[] arrOfColValues = new Object[columnCount];

		for (int i = 1; i <= columnCount; ++i) {

			Object obj = getColumnValue(rs, i);

			if (columnCount == 1) {
				return obj;
			}
			arrOfColValues[i] = obj;
		}
		return arrOfColValues;
	}

	protected Object getColumnValue(ResultSet rs, int index)
			throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index);
	}
}