package com.lenovo.vctl.common.lbs.core;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;



import com.lenovo.vctl.common.lbs.util.ByteUtil;

/**
 * R tree存储的元素， 整个工程的核心数据结构之一， 比如代表某个在某个坐标的人或者某个活动
 * @author songkun1
 *
 */
public class TargetLocation implements Serializable, ISerializable {
	private static final long serialVersionUID = 1L;
	public static final int TARGET_LOCATION_LENGTH = 25;
	private long targetId;
	private long timeStamp;
	private float x;
	private float y;
	private float distance;// 目标点到此点的距离
	private byte type;//额外属性 比如存储男女
	
	public static final byte ADD = 0;
	public static final byte DEL = 1;
	private byte addOrDel;
	
	public TargetLocation(){
		
	}
	public TargetLocation(long targetId, byte type, float x, float y) {
		super();
		this.x = x;
		this.y = y;
		this.targetId = targetId;
		this.type = type;
		this.timeStamp = System.currentTimeMillis();
	}
	
	public TargetLocation(long targetId,  byte type, long createTime, float x, float y){
		super();
		this.x = x;
		this.y = y;
		this.targetId = targetId;
		this.type = type;
		this.timeStamp = createTime;
	}
	

	@Override
	public String toString() {
		return "TargetLocation [targetId=" + targetId + ", timeStamp="
				+ timeStamp + ", x=" + x + ", y=" + y + ", distance="
				+ distance + ", type=" + type + "]";
	}
	@Override
	public ByteBuffer encode() {
		ByteBuffer bb = ByteBuffer.allocate(TARGET_LOCATION_LENGTH);
		bb.putLong(targetId);
		bb.put(type);
		bb.putLong(timeStamp);
		bb.putFloat(x);
		bb.putFloat(y);
		bb.flip();
		return bb;
		
	}
	

	public void decode(ByteBuffer bb){
		bb.rewind();
		targetId = bb.getLong();
		this.type = bb.get();
		timeStamp = bb.getLong();
		x = bb.getFloat();
		y = bb.getFloat();
	}
	
	public long getTargetId() {
		return targetId;
	}
	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public float getDistance() {
		return distance;
	}
	public void setDistance(float distance) {
		this.distance = distance;
	}
	
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	/**
	 * 只是时间戳不一样的equal
	 * @param obj
	 * @return
	 */
	public boolean miniEquals(Object obj){
		if(obj != null && obj instanceof TargetLocation){
			TargetLocation loc = (TargetLocation)obj;
			if(this.targetId  == loc.targetId
			&& this.x         == loc.x
			&& this.y         == loc.y
			&& this.type      == loc.type)
				return true;
			
		}
		return false;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof TargetLocation){
			TargetLocation loc = (TargetLocation)obj;
			if(this.targetId  == loc.targetId
			&& this.timeStamp == loc.timeStamp
			&& this.x         == loc.x
			&& this.y         == loc.y
			&& this.type      == loc.type)
				return true;
			
		}
		return false;
	}
	public byte getAddOrDel() {
		return addOrDel;
	}
	public void setAddOrDel(byte addOrDel) {
		this.addOrDel = addOrDel;
	}	
	
	
	
	
}
