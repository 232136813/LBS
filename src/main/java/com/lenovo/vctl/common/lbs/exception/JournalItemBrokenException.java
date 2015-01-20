package com.lenovo.vctl.common.lbs.exception;

import java.io.IOException;

public class JournalItemBrokenException extends IOException{
	long position;
	String info;
	public  JournalItemBrokenException(long position, String info){
		this.position = position;
		this.info = info;
	}
	
	public JournalItemBrokenException(long position, Throwable t){
		super(t);
		this.position = position;
	}
	@Override
	public String toString() {
		return "JournalItemBrokenException [position=" + position + ", info="
				+ info + "]";
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}
	
	
	
}
