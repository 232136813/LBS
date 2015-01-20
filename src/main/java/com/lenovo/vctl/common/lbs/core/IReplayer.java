package com.lenovo.vctl.common.lbs.core;

public interface IReplayer<T> {
	public void replay();
	//从某个journal文件的 某一位置开始回放
	public void replayStartFrom(String fileName, long position);
	public boolean isInReplay();
}
