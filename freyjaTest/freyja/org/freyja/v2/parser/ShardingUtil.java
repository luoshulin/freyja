package org.freyja.v2.parser;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import org.freyja.v2.annotation.Column;
import org.freyja.v2.annotation.Id;
import org.freyja.v2.annotation.SubColumn;
import org.freyja.v2.annotation.Table;
import org.freyja.v2.annotation.Transient;
import org.freyja.v2.mapping.ShardingMappingEngine;
import org.freyja.v2.mapping.ShardingProperty;
import org.freyja.v2.sharding.FreyjaEntity;

public class ShardingUtil {

	public static ShardingMappingEngine engine = new ShardingMappingEngine();

	public static FreyjaEntity getEntity(String tableName) {
		return (FreyjaEntity) engine.getEntities().get(tableName);
	}

	public static FreyjaEntity getEntity(Class clazz) {
		return (FreyjaEntity) engine.getEntities().get(clazz.getSimpleName());
	}

//	private static LinkedHashMap<String, FreyjaEntity> entities = new LinkedHashMap<String, FreyjaEntity>();

	public static void createEntity(Class<?> clazz) {
		FreyjaEntity entity = new FreyjaEntity();

		Table table = clazz.getAnnotation(Table.class);

		if (table.name().equals("t_user")) {
			System.out.println();
		}

		entity.setName(clazz.getSimpleName());
		entity.setTableName(table.name());
		entity.setSubTable(table.isSubTable());
		entity.setClazz(clazz);
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			PropertyDescriptor pd = org.springframework.beans.BeanUtils
					.getPropertyDescriptor(clazz, field.getName());

			if (pd == null || pd.getWriteMethod() == null
					|| pd.getReadMethod() == null) {
				continue;
			}

			if (field.isAnnotationPresent(Transient.class)) {
				continue;
			}

			String columnName = field.getName();
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				columnName = column.name();
			}

			ShardingProperty property = new ShardingProperty(field.getType(),
					field.getName(), columnName);

			if (field.isAnnotationPresent(Id.class)) {
				property.setId(true);

				Id id = field.getAnnotation(Id.class);

				property.setAuto(id.auto());
			}
			if (entity.isSubTable()) {
				if (field.isAnnotationPresent(SubColumn.class)) {
					property.setSubColumn(true);
				}
			}

			entity.addProperty(property);

		}
//		entities.put(table.name(), entity);
//		entities.put(entity.getName(), entity);
		
		engine.getEntities().put(table.name(), entity);
		engine.addEntity(entity);
	}

}
