package com.lenovo.vctl.common.lbs.persistent;

import com.lenovo.vctl.common.lbs.core.IReader;
import com.lenovo.vctl.common.lbs.core.IReplayer;
import com.lenovo.vctl.common.lbs.core.ISerializable;
import com.lenovo.vctl.common.lbs.core.IWriter;

public interface IPersistentable<T> extends IReader<T>, IWriter<T>, IReplayer<T>, ICheckPointable{
	public void open()throws Exception;
	public void cleanOldEntities(long timeStamp);
	
}
