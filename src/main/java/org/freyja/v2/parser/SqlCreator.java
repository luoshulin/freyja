package org.freyja.v2.parser;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.freyja.v2.mapping.ShardingProperty;
import org.freyja.v2.object.DbResult;
import org.freyja.v2.object.Parameter;
import org.freyja.v2.sharding.FreyjaEntity;

import com.alibaba.druid.mapping.Property;

public class SqlCreator {

	public static Parameter save(Object bean) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		FreyjaEntity entity = ShardingUtil.getEntity(bean.getClass());

		List<Object> args = new ArrayList<Object>();
		Object subValue = null;
		for (Property p : entity.getProperties().values()) {
			ShardingProperty s = (ShardingProperty) p;
			if (s.isId() && s.isAuto()) {
				continue;
			}

			Object propertyValue = PropertyUtils.getProperty(bean, p.getName());
			args.add(propertyValue);
			if (s.isSubColumn()) {
				subValue = propertyValue;
			}
		}
		String sql = null;
		Parameter parameter = new Parameter(args.toArray(),
				entity.getSqlTyps("insert"));
		if (entity.isSubTable()) {

			DbResult result = entity.toInsert(subValue);
			sql = result.getSql();
			parameter.setDbNo(result.getDbNo());
		} else {
			sql = entity.toInsert();
		}
		parameter.setSql(sql);
		return parameter;
	}

	public static Parameter update(Object bean) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		FreyjaEntity entity = ShardingUtil.getEntity(bean.getClass());
		List<Object> args = new ArrayList<Object>();
		Object idValue = null;
		for (Property p : entity.getProperties().values()) {
			ShardingProperty s = (ShardingProperty) p;
			Object propertyValue = PropertyUtils.getProperty(bean, p.getName());
			if (s.isId()) {
				idValue = propertyValue;
				continue;
			}
			args.add(propertyValue);
		}
		args.add(idValue);

		Parameter parameter = new Parameter(args.toArray(),
				entity.getSqlTyps("update"));
		String sql = null;
		if (entity.isSubTable()) {
			DbResult result = entity.toUpdate(idValue);

			sql = result.getSql();
			parameter.setDbNo(result.getDbNo());
		} else {
			sql = entity.toUpdate();

		}
		parameter.setSql(sql);
		return parameter;
	}

	public static Parameter delete(Object bean) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		FreyjaEntity entity = ShardingUtil.getEntity(bean.getClass());

		Object idValue = PropertyUtils.getProperty(bean, entity.getId()
				.getName());

		Parameter parameter = new Parameter(new Object[] { idValue },
				entity.getSqlTyps("delete"));
		String sql;
		if (entity.isSubTable()) {
			DbResult result = entity.toDelete(idValue);
			sql = result.getSql();

			parameter.setDbNo(result.getDbNo());
		} else {
			sql = entity.toDelete();
		}
		parameter.setSql(sql);
		return parameter;
	}

	public static Parameter get(FreyjaEntity entity, Object idValue) {

		Parameter parameter = new Parameter(new Object[] { idValue },
				entity.getSqlTyps("select"));
		String sql;
		if (entity.isSubTable()) {
			DbResult result = entity.toSelect(idValue);

			sql = result.getSql();
			parameter.setDbNo(result.getDbNo());
		} else {
			sql = entity.toSelect();
		}
		parameter.setSql(sql);
		return parameter;
	}
}
