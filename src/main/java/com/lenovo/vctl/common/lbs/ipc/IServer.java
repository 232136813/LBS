package com.lenovo.vctl.common.lbs.ipc;

public interface IServer {
	public void start(String ip, int port);
	public void shutdown();
}
