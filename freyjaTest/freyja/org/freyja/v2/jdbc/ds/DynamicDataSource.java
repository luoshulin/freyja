package org.freyja.v2.jdbc.ds;

import org.freyja.v2.parser.ShardingUtil;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		return ShardingUtil.engine.getSubDataName() + "_"
				+ DbContextHolder.getDbNum();
	}

}
