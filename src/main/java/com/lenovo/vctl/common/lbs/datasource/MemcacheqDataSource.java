package com.lenovo.vctl.common.lbs.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lenovo.vctl.apps.commons.utils.GsonUtil;
import com.lenovo.vctl.common.lbs.LbsManager;
import com.lenovo.vctl.common.lbs.core.TargetLocation;
import com.lenovo.vctl.common.lbs.util.MemcacheqUtil;
import com.lenovo.vctl.dal.cache.exception.CacheException;


public class MemcacheqDataSource extends AbstractDataSource implements IDataSource<TargetLocation>{

	private static final Logger logger = LoggerFactory.getLogger(MemcacheqDataSource.class);
	
	public MemcacheqDataSource(LbsManager lbsManager){
		super(lbsManager);
	}
	@Override
	public TargetLocation load(){
		try {
			String targetString = (String)MemcacheqUtil.pop(lbsManager.getProjectName(), lbsManager.getProjectName());
			if(logger.isDebugEnabled())
				logger.debug("Get TargetLocation String from Memcacheq : " + targetString);
			return toObject(targetString);
		} catch (CacheException e) {
			logger.error("Load TargetLocation error ", e);
		}
		return null;
	
	}
	
	private TargetLocation toObject(String s){
		if(s == null)return null;
		return GsonUtil.fromJson(s, TargetLocation.class);
	}

}
