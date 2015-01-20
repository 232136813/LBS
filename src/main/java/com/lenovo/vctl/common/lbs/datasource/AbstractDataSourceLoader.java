package com.lenovo.vctl.common.lbs.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lenovo.vctl.common.lbs.LbsManager;
import com.lenovo.vctl.common.lbs.core.TargetLocation;
import com.lenovo.vctl.common.lbs.exception.LbsException;

public abstract class AbstractDataSourceLoader  implements IDataSourceLoader<TargetLocation>{
	

	private Logger logger = LoggerFactory.getLogger(AbstractDataSourceLoader.class);
	protected IDataSource<TargetLocation> dataSource;
	protected LbsManager manager;
	
	public AbstractDataSourceLoader(IDataSource<TargetLocation> dataSource, LbsManager manager){
		this.dataSource = dataSource;
		this.manager = manager;
	}
	
	@Override
	public void insertIntoRTree(TargetLocation loc) {
		if(loc == null)return;
		if(loc.getAddOrDel() == loc.DEL){//删除
			try {
				manager.clearSelfLocation(loc.getTargetId(), manager.SendBackupMessage);
			} catch (LbsException e) {
				// TODO Auto-generated catch block
				logger.error("StartUp Data Source Loader insert into rtree error  target = " + loc, e);
			}//暂时不开启备份
		
		}else{
			try {
				manager.exposeTarget(loc.getX(), loc.getY(), loc.getTargetId(), loc.getTimeStamp(), loc.getType(), manager.SendBackupMessage);
			} catch (LbsException e) {
				logger.error("StartUp Data Source Loader insert into rtree error  target = " + loc, e);
			}
		}
	
	}
	
	@Override
	public boolean doLoad() {
		TargetLocation loc = dataSource.load();
		if(loc != null){
			insertIntoRTree(loc);
			return true;
		}else{
			return false;
		}
		
	}
	
}
