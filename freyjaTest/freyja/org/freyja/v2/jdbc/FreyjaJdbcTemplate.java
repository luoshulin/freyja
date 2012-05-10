package org.freyja.v2.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.freyja.v2.jdbc.ds.DbContextHolder;
import org.freyja.v2.jdbc.rowMapper.BeanPropertyRowMapper;
import org.freyja.v2.jdbc.rowMapper.MapRowMapper;
import org.freyja.v2.jdbc.rowMapper.ObjectRowMapper;
import org.freyja.v2.mapping.ShardingEntity;
import org.freyja.v2.object.DbResult;
import org.freyja.v2.object.ExplainResult;
import org.freyja.v2.object.Parameter;
import org.freyja.v2.parser.ShardingUtil;
import org.freyja.v2.parser.SqlCreator;
import org.freyja.v2.sharding.FreyjaEntity;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;

import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;

public class FreyjaJdbcTemplate extends FreyjaJdbcAccessor implements
		FreyjaJdbcOperations {

	@Override
	public <T> T get(Class<T> clazz, Object id) {
		Assert.notNull(id);
		FreyjaEntity entity = ShardingUtil.getEntity(clazz);

		Parameter parameter = SqlCreator.get(entity, id);
		DbContextHolder.setDbNum(parameter.getDbNo());
		List<T> list = jdbcTemplate.query(parameter.getSql(),
				parameter.getArgs(), parameter.getSqlTypes(),
				new BeanPropertyRowMapper<T>(entity));

		int size = list.size();
		if (size == 0) {
			return null;
		} else if (size == 1) {
			return list.get(0);
		} else {
			throw new RuntimeException("error result");
		}

	}

	@Override
	public Number save(Object bean) {
		try {
			Assert.notNull(bean);
			ShardingEntity entity = ShardingUtil.getEntity(bean.getClass());
			Assert.notNull(entity);
			Parameter parameter = SqlCreator.save(bean);
			DbContextHolder.setDbNum(parameter.getDbNo());
			if (entity.getId() == null || !entity.getId().isAuto()) {

				jdbcTemplate.update(parameter.getSql(), parameter.getArgs(),
						parameter.getSqlTypes());

				return null;
			}

			KeyHolder holder = new GeneratedKeyHolder();
			PreparedStatementCreatorFactory ps = new PreparedStatementCreatorFactory(
					parameter.getSql(), parameter.getSqlTypes());
			ps.setReturnGeneratedKeys(true);
			PreparedStatementCreator psc = ps
					.newPreparedStatementCreator(parameter.getArgs());
			jdbcTemplate.update(psc, holder);

			return holder.getKey();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void update(Object bean) {

		try {
			Assert.notNull(bean);
			Parameter parameter = SqlCreator.update(bean);

			DbContextHolder.setDbNum(parameter.getDbNo());
			jdbcTemplate.update(parameter.getSql(), parameter.getArgs(),
					parameter.getSqlTypes());

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveOrUpdate(Object object) {

	}

	@Override
	public void delete(Object bean) {
		Assert.notNull(bean);
		try {
			Parameter parameter = SqlCreator.delete(bean);

			DbContextHolder.setDbNum(parameter.getDbNo());
			jdbcTemplate.update(parameter.getSql(), parameter.getArgs(),
					parameter.getSqlTypes());

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void execute(String sql, Object... args) {
		Assert.notNull(sql);
		if (sql.startsWith("update")) {
			this.executeUpdate(sql, args);
		} else if (sql.startsWith("insert")) {
			this.executeInsert(sql, args);
		} else if (sql.startsWith("delete")) {
			this.executeDelete(sql, args);
		} else {
			throw new RuntimeException("error execute type");
		}
	}

	private void executeUpdate(String sql, Object... args) {

		ExplainResult result = ShardingUtil.engine
				.explainToUpdateExplainResult(sql);
		String shardingSql = result.getJdbcSql();
		if (result.isNeedSharding()) {

			DbResult r = ShardingUtil.engine.explainToUpdateSQL(
					(SQLUpdateStatement) result.getStmt(), Arrays.asList(args));
			DbContextHolder.setDbNum(r.getDbNo());
			shardingSql = r.getSql();
		}
		jdbcTemplate.update(shardingSql, args);
	}

	private void executeDelete(String sql, Object... args) {

		ExplainResult result = ShardingUtil.engine
				.explainToDeleteExplainResult(sql);
		String shardingSql = result.getJdbcSql();
		if (result.isNeedSharding()) {
			DbResult r = ShardingUtil.engine.explainToDeleteSQL(
					(SQLDeleteStatement) result.getStmt(), Arrays.asList(args));

			shardingSql = r.getSql();
			DbContextHolder.setDbNum(r.getDbNo());

		}
		jdbcTemplate.update(shardingSql, args);

	}

	private void executeInsert(String sql, Object... args) {

		ExplainResult result = ShardingUtil.engine
				.explainToInsertExplainResult(sql);
		String shardingSql = result.getJdbcSql();
		if (result.isNeedSharding()) {
			DbResult r = ShardingUtil.engine.explainToInsertSQL(
					result.getInsert(), Arrays.asList(args));
			shardingSql = r.getSql();
		}
		jdbcTemplate.update(shardingSql, args);

	}

	@Override
	public List query(String sql, Object... values) {
		if (sql.toLowerCase().startsWith("from")) {
			sql = "select * " + sql;
		}

		ExplainResult explain = ShardingUtil.engine
				.explainToSelectExplainResult(sql);

		List<DbResult> results = new ArrayList<DbResult>();
		if (explain.isNeedSharding()) {
			List<DbResult> dbrs = ShardingUtil.engine.explainToSelectSQLArray(
					explain.getQuery(), Arrays.asList(values));
			results.addAll(dbrs);
		} else {
			DbResult result = new DbResult();
			result.setSql(explain.getJdbcSql());
			// result.setDbNo(explain.getDbNo());
			results.add(result);
		}
		List list = new ArrayList();
		String tableName = explain.getTableName();

		for (DbResult result : results) {
			DbContextHolder.setDbNum(result.getDbNo());

			List l = null;
			if (tableName != null) {
				ShardingEntity entity = ShardingUtil.getEntity(tableName);
				l = jdbcTemplate.query(result.getSql(), values,
						new BeanPropertyRowMapper(entity));
			} else {
				l = jdbcTemplate.query(result.getSql(), values,
						new ObjectRowMapper());
			}
			list.addAll(l);
		}
		return list;
	}

	@Override
	public List<Map<String, Object>> queryForMap(String sql, Object... values) {

		ExplainResult explain = ShardingUtil.engine
				.explainToSelectExplainResult(sql);

		List<DbResult> results = new ArrayList<DbResult>();
		if (explain.isNeedSharding()) {
			List<DbResult> dbrs = ShardingUtil.engine.explainToSelectSQLArray(
					explain.getQuery(), Arrays.asList(values));
			results.addAll(dbrs);
		} else {
			DbResult result = new DbResult();
			result.setSql(explain.getJdbcSql());
			results.add(result);
		}
		List list = new ArrayList();
		String tableName = explain.getTableName();
		for (DbResult result : results) {
			DbContextHolder.setDbNum(result.getDbNo());

			List l = jdbcTemplate.query(result.getSql(), values,
					new MapRowMapper());
			list.addAll(l);
		}
		return list;
	}

}
