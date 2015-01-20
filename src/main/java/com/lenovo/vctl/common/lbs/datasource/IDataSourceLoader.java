package com.lenovo.vctl.common.lbs.datasource;

public interface IDataSourceLoader<T>  extends Runnable{
	public boolean doLoad();
	
	public void insertIntoRTree(T t);
}
