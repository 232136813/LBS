package com.lenovo.vctl.common.lbs.core;

import gnu.trove.procedure.TLongProcedure;

import java.util.List;

import com.lenovo.vctl.common.lbs.exception.LbsException;

public interface ILbsService<T extends ISerializable> {
	public static final byte IGNORE_TYPE = 0;
	public static final byte MALE = 1;
	public static final byte FEMALE = 2;

	/**
	 * 此方法 在查询的
	 * @param x x坐标
	 * @param y  y坐标
	 * @param id  targetId
	 * @param n  查询个数
	 * @param exposeSelf  是否暴露自己的location让别人可以查询
	 * @param 
	 * @return
	 */
	public List<T> getNearestN(float x, float y, long id, long timeStamp, byte type, float distance, int n, boolean exposeSelf, boolean sendBackupMessage) throws LbsException;
	
	
	public List<T> getNearestN(float x, float y, long id, long timeStamp, byte type, TLongProcedure timeFilter, float distance, int n, boolean exposeSelf, boolean sendBackupMessage) throws LbsException;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param id
	 * @param type
	 * @param timeStamp 指定时间戳
	 * @param sendBackupMessag
	 * @return
	 * @throws LbsException
	 */
	
	public boolean exposeTarget(float x, float y, long id, long timeStamp, byte type,  boolean sendBackupMessag) throws LbsException;
	
	public boolean exposeTarget(float x, float y, long id, byte type, boolean sendBackupMessage) throws LbsException;
	
	/**
	 * 清除自己的位置信息
	 * @param id
	 * @return
	 */
	public boolean clearSelfLocation(long id, boolean sendBackupMessag) throws LbsException;
}