package org.freyja.v2.jdbc;

import java.util.List;
import java.util.Map;

public interface FreyjaJdbcOperations {

	public <T> T get(Class<T> clazz, Object id);

	public Number save(Object bean);

	public void update(Object bean);

	public void saveOrUpdate(Object bean);

	public void delete(Object bean);

	public void execute(String sql, Object... args);

	public List query(String sql, Object... args);

	public List<Map<String,Object>> queryForMap(String sql, Object... args);

}
