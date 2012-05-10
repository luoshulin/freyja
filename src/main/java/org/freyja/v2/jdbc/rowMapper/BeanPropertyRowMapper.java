package org.freyja.v2.jdbc.rowMapper;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.freyja.v2.mapping.ShardingEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

import com.alibaba.druid.mapping.Property;

public class BeanPropertyRowMapper<T> implements RowMapper {

	private ShardingEntity entity;

	public BeanPropertyRowMapper(ShardingEntity entity) {
		this.entity = entity;
		initialize(entity.getClazz());
	}

	private Class<T> mappedClass;

	private Map<String, PropertyDescriptor> mappedFields;

	public void setMappedClass(Class mappedClass) {
		if (this.mappedClass == null) {
			initialize(mappedClass);
		} else {
			if (!this.mappedClass.equals(mappedClass)) {
				throw new InvalidDataAccessApiUsageException(
						"The mapped class can not be reassigned to map to "
								+ mappedClass
								+ " since it is already providing mapping for "
								+ this.mappedClass);
			}
		}
	}

	protected void initialize(Class mappedClass) {
		this.mappedClass = mappedClass;
		this.mappedFields = new HashMap<String, PropertyDescriptor>();
		PropertyDescriptor[] pds = BeanUtils
				.getPropertyDescriptors(mappedClass);
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() == null) {
				continue;
			}
			Property property = entity.getProperty(pd.getName());
			if (property == null) {
				continue;
			}
			this.mappedFields.put(pd.getName().toLowerCase(), pd);

			String underscoredName = underscoreName(property, pd.getName());
			if (!pd.getName().toLowerCase().equals(underscoredName)) {
				this.mappedFields.put(underscoredName, pd);
			}
		}
	}

	private String underscoreName(Property property, String name) {
		return property.getDbColumnName();
	}

	public final Class getMappedClass() {
		return this.mappedClass;
	}

	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		T mappedObject = BeanUtils.instantiate(this.mappedClass);
		BeanWrapper bw = PropertyAccessorFactory
				.forBeanPropertyAccess(mappedObject);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);
			PropertyDescriptor pd = this.mappedFields.get(column.replaceAll(
					" ", "").toLowerCase());
			if (pd != null) {
				try {
					Object value = getColumnValue(rs, index, pd);
					try {
						bw.setPropertyValue(pd.getName(), value);
					} catch (TypeMismatchException e) {
						if (value == null) {
						} else {
							throw e;
						}
					}
				} catch (NotWritablePropertyException ex) {
					throw new DataRetrievalFailureException(
							"Unable to map column " + column + " to property "
									+ pd.getName(), ex);
				}
			}
		}

		return mappedObject;
	}

	protected Object getColumnValue(ResultSet rs, int index,
			PropertyDescriptor pd) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
	}

}
