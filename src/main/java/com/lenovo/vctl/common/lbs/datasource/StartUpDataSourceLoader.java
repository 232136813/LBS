package com.lenovo.vctl.common.lbs.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lenovo.vctl.common.lbs.LbsManager;
import com.lenovo.vctl.common.lbs.core.TargetLocation;
import com.lenovo.vctl.common.lbs.exception.LbsException;

/**
 *  启动数据源获取器  
 *  
 *  主要作用是 再同步调用期间  如果 lbs应用 down机或者重启  发布lbs数据的应用应该吧 这些数据写到 一个memcacheq中 等待 lbs应用重启后  重新load 重启时 缺失的数据
 * @author songkun1
 *
 */
public class StartUpDataSourceLoader extends AbstractDataSourceLoader implements IDataSourceLoader<TargetLocation>{
	private Logger logger = LoggerFactory.getLogger(StartUpDataSourceLoader.class);
	
	public StartUpDataSourceLoader (IDataSource dataSource, LbsManager manager){
		super(dataSource, manager);
	}

	
	public void run(){
		while(doLoad()){
			//continue
		}
	
	}
	public IDataSource<TargetLocation> getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource<TargetLocation> dataSource) {
		this.dataSource = dataSource;
	}


	
	

}
