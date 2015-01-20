package com.lenovo.vctl.common.lbs.persistent;

import java.io.Serializable;

import com.lenovo.vctl.common.lbs.persistent.JournalItem.CheckPointEnd;
import com.lenovo.vctl.common.lbs.persistent.JournalItem.CheckPointStart;

/**
 *  获取 最近一次 checkPointStart 的文件名和 文件position  用于回放
 */
public class CheckPointWrapper implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JournalItem.CheckPointStart targetCheckPoint;//可以用于回放的 checkpoint ，确定是一个 checkPoint的 start 类型 ，从这个start开始回放journal 可以保证数据完整性
	
	private JournalItem.CheckPointStart tmpCheckPointStart;
	private JournalItem.CheckPointEnd tmpCheckPointEnd;
	
	public CheckPointWrapper(){
		
	}

	public JournalItem.CheckPointStart getTargetCheckPoint() {
		return targetCheckPoint;
	}

	public void setTargetCheckPoint(JournalItem.CheckPointStart targetCheckPoint) {
		this.targetCheckPoint = targetCheckPoint;
	}

	public JournalItem.CheckPointStart getTmpCheckPointStart() {
		return tmpCheckPointStart;
	}

	public void setTmpCheckPointStart(CheckPointStart item) {
		this.tmpCheckPointStart = item;
	}

	public JournalItem.CheckPointEnd getTmpCheckPointEnd() {
		return tmpCheckPointEnd;
	}

	public void setTmpCheckPointEnd(JournalItem.CheckPointEnd tmpCheckPointEnd) {
		this.tmpCheckPointEnd = tmpCheckPointEnd;
	}

	@Override
	public String toString() {
		return "CheckPointWrapper [targetCheckPoint=" + targetCheckPoint
				+ ", tmpCheckPointStart=" + tmpCheckPointStart
				+ ", tmpCheckPointEnd=" + tmpCheckPointEnd + "]";
	}
	

	
	
	
}
