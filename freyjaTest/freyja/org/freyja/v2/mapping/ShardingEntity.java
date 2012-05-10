package org.freyja.v2.mapping;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.mapping.Entity;
import com.alibaba.druid.mapping.Property;

public class ShardingEntity extends Entity {

	private boolean isSubTable;

	private boolean isAsync;

	private Class clazz;

	@Override
	public void addProperty(Property property) {

		ShardingProperty p = (ShardingProperty) property;
		if (p.isId()) {
			propertyCache.put("id", p);
		} else if (p.isSubColumn()) {
			propertyCache.put("subColumn", p);
		}

		super.addProperty(p);
	}

	public boolean isAsync() {
		return isAsync;
	}

	public void setAsync(boolean isAsync) {
		this.isAsync = isAsync;
	}

	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public boolean isSubTable() {
		return isSubTable;
	}

	public void setSubTable(boolean isSubTable) {
		this.isSubTable = isSubTable;
	}

	private Map<String, ShardingProperty> propertyCache = new HashMap<String, ShardingProperty>();

	public ShardingProperty getId() {
		return propertyCache.get("id");
	}

	public ShardingProperty getSubColumn() {
		return propertyCache.get("subColumn");
	}

}