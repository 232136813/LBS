package com.lenovo.vctl.common.lbs.datasource;

import com.lenovo.vctl.common.lbs.core.ISerializable;



/**
 * 数据来源
 * 
 * lsb服务可以被动接受数据， 也可以主动拉取数据
 * 被动接受数据  应用可以调用 ipc 来插入数据到 lbs服务器 
 * 
 * 主动拉取数据 就是从  mysql  memcacheq redis 或者其他服务 获取数据插入 lbs服务器中
 * 
 * 
 *   
 * @author songkun1
 *
 */
public interface IDataSource<T> {
	public T load();
}
