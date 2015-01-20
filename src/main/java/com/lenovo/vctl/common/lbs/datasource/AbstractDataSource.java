package com.lenovo.vctl.common.lbs.datasource;

import com.lenovo.vctl.common.lbs.LbsManager;
import com.lenovo.vctl.common.lbs.core.TargetLocation;

public abstract class AbstractDataSource implements IDataSource<TargetLocation>{
	protected LbsManager lbsManager;
	public AbstractDataSource(LbsManager m){
		this.lbsManager = m;
	}
	
}
