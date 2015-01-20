package com.lenovo.vctl.common.lbs.persistent;

public interface ICheckPointable {
	/*
	 * 支持 checkpoint 机制 
	 */
	public boolean checkPoint();
	/*
	 * 获取最近一次的 checkpoint
	 */
	public CheckPointWrapper findLastestCheckPoint();

}
