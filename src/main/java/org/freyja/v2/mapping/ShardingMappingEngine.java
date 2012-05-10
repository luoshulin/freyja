package org.freyja.v2.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freyja.v2.mapping.spi.ShardingMySqlMappingProvider;
import org.freyja.v2.object.DbResult;
import org.freyja.v2.object.ExplainResult;
import org.freyja.v2.parser.FreyjaUtil;
import org.freyja.v2.parser.ShardingUtil;
import org.freyja.v2.sharding.FreyjaEntity;

import com.alibaba.druid.mapping.Entity;
import com.alibaba.druid.mapping.MappingContext;
import com.alibaba.druid.mapping.MappingEngine;
import com.alibaba.druid.mapping.spi.MappingVisitor;
import com.alibaba.druid.mapping.spi.PropertyValue;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;

public class ShardingMappingEngine extends MappingEngine {

	private int tableNum;

	private int dbNum;

	private int idSubNum;

	private String subDataName;

	public String getSubDataName() {
		return subDataName;
	}

	public void setSubDataName(String subDataName) {
		this.subDataName = subDataName;
	}

	public int getTableNum() {
		return tableNum;
	}

	public void setTableNum(int tableNum) {
		this.tableNum = tableNum;
	}

	public int getDbNum() {
		return dbNum;
	}

	public void setDbNum(int dbNum) {
		this.dbNum = dbNum;
	}

	@Override
	public void addEntity(Entity entity) {
		((FreyjaEntity) entity).init();

		super.addEntity(entity);
	}

	public ShardingMappingEngine() {
		super(new ShardingMySqlMappingProvider());
	}

	public DbResult shardingAfterResole(MappingVisitor visitor) {
		DbResult dbResult = new DbResult();
		boolean needSharding = false;
		for (SQLTableSource tableSource : visitor.getTableSources().values()) {
			ShardingEntity entity = (ShardingEntity) tableSource
					.getAttribute("mapping.entity");
			if (entity.isSubTable()) {
				needSharding = true;
				break;
			}
		}
		if (!needSharding) {
			dbResult.setNeedSharding(false);
			return dbResult;
		}

		Map<String, String> sharding = new HashMap<String, String>();

		for (PropertyValue entry : visitor.getPropertyValues()) {
			FreyjaEntity entity = (FreyjaEntity) entry.getEntity();

			if (!entity.isSubTable()) {
				continue;
			}

			ShardingProperty property = (ShardingProperty) entry.getProperty();

			if (!property.isSubColumn()) {
				continue;
			}

			Object value = entry.getValue();
			DbResult result = entity.getShardingTableName(value);

			dbResult.setDbNo(result.getDbNo());
			String shardingTableName = result.getTableName();
			sharding.put(entity.getTableName(), shardingTableName);
			break;
		}

		if (sharding.size() == 0) {
			dbResult.setNeedSharding(true);
			return dbResult;
		}
		for (SQLTableSource tableSource : visitor.getTableSources().values()) {

			ShardingEntity entity = (ShardingEntity) tableSource
					.getAttribute("mapping.entity");
			if (entity == null) {
				continue;
			}

			String shardingTableName = sharding.get(entity.getTableName());
			if (shardingTableName == null) {
				continue;
			}
			if (!entity.isSubTable()) {
				continue;
			}

			SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
			exprTableSource.setExpr(new SQLIdentifierExpr(shardingTableName));
		}
		return dbResult;
	}

	private Map<String, ExplainResult> explainCache = new HashMap<String, ExplainResult>();

	public ExplainResult explainToSelectExplainResult(String sql) {
		ExplainResult result = explainCache.get(sql);
		if (result != null) {
			return result;
		}

		SQLSelectQueryBlock query = super.explainToSelectSQLObject(sql);

		MappingVisitor visitor = super.createMappingVisitor();
		query.accept(visitor);
		String jdbcSql = super.toSQL(query);
		boolean needSharding = false;
		for (SQLTableSource tableSource : visitor.getTableSources().values()) {
			ShardingEntity entity = (ShardingEntity) tableSource
					.getAttribute("mapping.entity");
			if (entity.isSubTable()) {
				needSharding = true;
				break;
			}
		}

		String tableName = FreyjaUtil.isSingle(query);

		result = new ExplainResult(jdbcSql, tableName, needSharding, query);
		explainCache.put(sql, result);
		return result;
	}

	public ExplainResult explainToUpdateExplainResult(String sql) {

		ExplainResult result = explainCache.get(sql);
		if (result != null) {
			return result;
		}

		SQLUpdateStatement stmt = super.explainToUpdateSQLObject(sql, new MappingContext());

		String tableName = stmt.getTableName().getSimleName();

		MappingVisitor visitor = this.createMappingVisitor(Collections
				.emptyList());
		stmt.accept(visitor);

		String jdbcSql = toSQL(stmt);

		ShardingEntity entity = ShardingUtil.getEntity(tableName);

		result = new ExplainResult(jdbcSql, tableName, entity.isSubTable(),
				stmt);
		return result;
	}

	public ExplainResult explainToDeleteExplainResult(String sql) {

		ExplainResult result = explainCache.get(sql);
		if (result != null) {
			return result;
		}

		SQLDeleteStatement stmt = super.explainToDeleteSQLObject(sql, new MappingContext());

		String tableName = stmt.getTableName().getSimleName();

		MappingVisitor visitor = this.createMappingVisitor(Collections
				.emptyList());
		stmt.accept(visitor);
		String jdbcSql = toSQL(stmt);
		ShardingEntity entity = ShardingUtil.getEntity(tableName);

		result = new ExplainResult(jdbcSql, tableName, entity.isSubTable(),
				stmt);
		return result;
	}

	public ExplainResult explainToInsertExplainResult(String sql) {

		ExplainResult result = explainCache.get(sql);
		if (result != null) {
			return result;
		}

		SQLInsertStatement stmt = super.explainToInsertSQLObject(sql, new MappingContext());

		String tableName = stmt.getTableName().getSimleName();
		MappingVisitor visitor = this.createMappingVisitor();
		stmt.accept(visitor);
		String jdbcSql = toSQL(stmt);
		ShardingEntity entity = ShardingUtil.getEntity(tableName);

		result = new ExplainResult(jdbcSql, tableName, entity.isSubTable(),
				stmt);
		return result;
	}

	public DbResult explainToUpdateSQL(SQLUpdateStatement stmt,
			List<Object> parameters) {

		// SQLUpdateStatement stmt = explainToUpdateSQLObject(sql);

		MappingVisitor visitor = this.createMappingVisitor(parameters);
		stmt.accept(visitor);
		DbResult result = shardingAfterResole(visitor);

		result.setSql(toSQL(stmt));
		return result;
	}

	@Override
	public void afterResole(MappingVisitor visitor) {
		this.shardingAfterResole(visitor);
	}

	public List<DbResult> explainToSelectSQLArray(SQLSelectQueryBlock query,
			List<Object> parameters) {
		MappingVisitor visitor = this.createMappingVisitor(parameters);
		query.accept(visitor);
		DbResult f = shardingAfterResole(visitor);
		List<DbResult> results = new ArrayList<DbResult>();
		if (f.isNeedSharding()) {
			for (int j = 0; j < dbNum; j++) {
				for (int i = 0; i < tableNum; i++) {
					DbResult result = new DbResult();
					for (SQLTableSource tableSource : visitor.getTableSources()
							.values()) {
						FreyjaEntity entity = (FreyjaEntity) tableSource
								.getAttribute("mapping.entity");
						if (entity == null) {
							continue;
						}
						String shardingTableName = entity.getShardingTableName(
								i).getTableName();

						SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
						exprTableSource.setExpr(new SQLIdentifierExpr(
								shardingTableName));
					}
					result.setSql(toSQL(query));
					result.setDbNo(j);
					result.setTableNo(i);
					results.add(result);
				}
			}
		} else {
			f.setSql(toSQL(query));
			results.add(f);
		}
		return results;
	}

	public DbResult explainToDeleteSQL(SQLDeleteStatement stmt,
			List<Object> parameters) {

		// SQLDeleteStatement stmt = explainToDeleteSQLObject(sql);

		MappingVisitor visitor = this.createMappingVisitor(parameters);
		stmt.accept(visitor);
		DbResult r = shardingAfterResole(visitor);

		r.setSql(toSQL(stmt));
		return r;
	}

	public DbResult explainToInsertSQL(SQLInsertStatement stmt,
			List<Object> parameters) {

		// SQLInsertStatement stmt = explainToInsertSQLObject(sql);

		MappingVisitor visitor = this.createMappingVisitor(parameters);
		stmt.accept(visitor);
		DbResult r = shardingAfterResole(visitor);

		r.setSql(toSQL(stmt));
		return r;
	}

	public int getIdSubNum() {
		return idSubNum;
	}

	public void setIdSubNum(int idSubNum) {
		this.idSubNum = idSubNum;
	}
}