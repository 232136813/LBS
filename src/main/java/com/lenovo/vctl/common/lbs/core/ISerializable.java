package com.lenovo.vctl.common.lbs.core;

import java.nio.ByteBuffer;


public interface ISerializable {
	ByteBuffer encode();
	void decode(ByteBuffer bb);

}
