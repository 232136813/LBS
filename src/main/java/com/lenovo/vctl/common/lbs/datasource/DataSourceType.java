package com.lenovo.vctl.common.lbs.datasource;

public enum DataSourceType {
	MEMCACHEQ(0, MemcacheqDataSource.class)/*memcacheq*/, MYSQL(1, MysqlDataSource.class)/*mysql*/;
	private int type;
	private Class<AbstractDataSource> clazz;
	private DataSourceType(int type, Class clazz){
		this.type = type;
		this.clazz = clazz;
	}
	
	public int getType(){
		return this.type;
	}
	
	public Class<AbstractDataSource> getClazz(){
		return this.clazz;
	}
	
	public static Class<AbstractDataSource> getClazzByType(int type){
		for(DataSourceType dataSource : DataSourceType.values()){
			if(dataSource.getType() == type){
				return dataSource.getClazz();
			}
		}
		return null;
	}
	
}
