package com.lenovo.vctl.common.lbs.datasource;

/**
 * 数据获取策略 
 * @author songkun1
 *
 */
public enum DataSourceLoadStrategy {
	START_UP(0, StartUpDataSourceLoader.class)/*服务启动 开始获取 到取完为止*/, TIMER(1, TimerDataSourceLoader.class)/*定时获取方式*/;
	private int type;
	private Class clazz;
	private DataSourceLoadStrategy(int type, Class clazz){
		this.type = type;
		this.clazz = clazz;
	}
	
	public int getType(){
		return this.type;
	}
	
	public Class getClazz(){
		return this.clazz;
	}
	
	public static Class getClazzByType(int type){
		for(DataSourceLoadStrategy strategy : DataSourceLoadStrategy.values()){
			if(strategy.getType() == type){
				return strategy.getClazz();
			}
		}
		return null;
	}
}
