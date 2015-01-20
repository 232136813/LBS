package com.lenovo.vctl.common.lbs.exception;

public class LbsException extends Exception{
	String info ;
	
	public LbsException(String info){
		this.info = info;
	}
	public LbsException(Throwable t, String s){
		super(t);
		this.info = s;
	}
	@Override
	public String toString() {
		return "LbsException [info=" + info + "]";
	}
	
}
