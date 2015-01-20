package com.lenovo.vctl.common.lbs.datasource;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lenovo.vctl.common.lbs.LbsManager;
import com.lenovo.vctl.common.lbs.core.TargetLocation;

/**
 * 定时获取器  
 * 定时获取器的主要作用是 异步获取lbs数据
 * lbs数据发布者 并不是同步调用lbs服务的 借口来插入lbs数据到lbs服务中
 * 而是发布到 第三方的queue中 然后 lbs服务 主动 定时去获取数据
 * @author songkun1
 *
 */
public class TimerDataSourceLoader extends AbstractDataSourceLoader implements IDataSourceLoader<TargetLocation>{
	private Logger logger = LoggerFactory.getLogger(TimerDataSourceLoader.class);
	
	private final Timer timer = new Timer();
	private final TimerTask task = new TimerTask(){

		@Override
		public void run() {
			if(logger.isDebugEnabled())
				logger.debug("Timer Data Source Loader Do Load");
			while(doLoad())
				;//	continue
		}
		
	};
	
	public TimerDataSourceLoader(IDataSource dataSource, LbsManager manager){
		super(dataSource, manager);
	}
	
	@Override
	public void run() {
		logger.info("Timer Data Source Loader start up");
		timer.schedule(task, 5000, 1000);
	}



}
