package com.lenovo.vctl.common.lbs.core;

import java.util.List;

public interface IReader<T>{
	List<T> read(float locationX, float locationY, long targetId, int count, boolean isActiveSelf);
}
