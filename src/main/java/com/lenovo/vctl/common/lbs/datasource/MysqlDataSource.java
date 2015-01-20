package com.lenovo.vctl.common.lbs.datasource;

import com.lenovo.vctl.common.lbs.LbsManager;
import com.lenovo.vctl.common.lbs.core.TargetLocation;

public class MysqlDataSource  extends AbstractDataSource implements IDataSource<TargetLocation>{
	
	public MysqlDataSource(LbsManager m) {
		super(m);
	}

	@Override
	public TargetLocation load() {
		return null;
	}

}
