package com.lenovo.vctl.common.lbs.persistent;

import java.nio.ByteBuffer;

import com.lenovo.vctl.common.lbs.core.ISerializable;
import com.lenovo.vctl.common.lbs.core.TargetLocation;

/**
 * 使用文件记录 <code>JournalPersistor</code>持久化的数据，用于回放记录
 * 
 * @author songkun1
 *
 */
public abstract class JournalItem implements ISerializable{

	public static final int JournalAddLength = TargetLocation.TARGET_LOCATION_LENGTH;
	public static final int JournalDeleteLength = 8;
	public static final int JournalCheckPointLength = 8;
	
	public static final byte ADD = 0;
	public static final byte DELETE = 1;
	public static final byte END = 2;
	public static final byte CHECK_POINT_START = 3;
	public static final byte CHECK_POINT_END = 4;
	
	
	
	private String fileName;
	private long position;
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public void setPosition(long position){
		this.position = position;
	}
	
	public String getFileName(){
		return this.fileName;
	}
	
	public long getPosition(){
		return this.position;
	}
	
	public static final class JournalAdd extends JournalItem implements ISerializable{
		private TargetLocation seriaTagert;
		
		public JournalAdd(){
			
		}
		public JournalAdd(TargetLocation target){
			this.seriaTagert = target;
		}
		
		public TargetLocation getTargetLocation(){
			return seriaTagert;
		}
		
		@Override
		public ByteBuffer encode() {
			ByteBuffer bb = ByteBuffer.allocate(JournalAddLength + 1);
			bb.put(ADD);//第一个字段
			bb.putLong(seriaTagert.getTargetId());
			bb.put(seriaTagert.getType());
			bb.putLong(seriaTagert.getTimeStamp());
			bb.putFloat(seriaTagert.getX());
			bb.putFloat(seriaTagert.getY());
			bb.flip();
			return bb;
		}

		@Override
		public void decode(ByteBuffer bb) {
			bb.rewind();
			if(bb.limit() != (JournalAddLength)){//获取的数据只是TargetLocation的长度
				throw new IllegalArgumentException();
			}
			TargetLocation loc = new TargetLocation();
			loc.setTargetId(bb.getLong());
			loc.setType(bb.get());
			loc.setTimeStamp(bb.getLong());
			loc.setX(bb.getFloat());
			loc.setY(bb.getFloat());
			this.seriaTagert = loc;

		}
		@Override
		public String toString() {
			return "JournalAdd [seriaTagert=" + seriaTagert + "]";
		}
		
		

	}
	
	public static final class JournalDelete extends JournalItem implements ISerializable{
		private long targetId;
		
		public JournalDelete(){
			
		}
		public JournalDelete(long targetId){
			this.targetId = targetId;
		}
		
		public long getTargetId(){
			return targetId;
		}
		
		@Override
		public ByteBuffer encode() {
			ByteBuffer bb = ByteBuffer.allocate(JournalDeleteLength + 1);
			bb.put(DELETE);//第一个字段
			bb.putLong(targetId);
			bb.flip();
			return bb;
		}

		@Override
		public void decode(ByteBuffer bb) {
			bb.rewind();
			if(bb.limit() != JournalDeleteLength){//获取的数据只是TargetLocation的长度
				throw new IllegalArgumentException();
			}
			targetId = bb.getLong();

		}
		@Override
		public String toString() {
			return "JournalDelete [targetId=" + targetId + "]";
		}
		

	}
	
	
	
	public static final class CheckPointStart extends JournalItem implements ISerializable{

		private long id;//可以使用时间戳作为 id
		
		public CheckPointStart(){
			this.id  = System.currentTimeMillis();
		}
		
		
		public long getId() {
			return id;
		}



		public void setId(long id) {
			this.id = id;
		}


		@Override
		public ByteBuffer encode() {
			ByteBuffer bb = ByteBuffer.allocate(JournalCheckPointLength + 1);
			bb.put(CHECK_POINT_START);//第一个字段
			bb.putLong(id);
			bb.flip();
			return bb;
		}

		@Override
		public void decode(ByteBuffer bb) {
			bb.rewind();
			if(bb.limit() != JournalCheckPointLength){
				throw new IllegalArgumentException();
			}
			this.id = bb.getLong();
			
		}


		@Override
		public String toString() {
			return "CheckPointStart [id=" + id + ", getFileName()="
					+ getFileName() + ", getPosition()=" + getPosition()
					+ ", toString()=" + super.toString() + ", getClass()="
					+ getClass() + ", hashCode()=" + hashCode() + "]";
		}

		
		
		
	}
	
	public static final class  CheckPointEnd extends JournalItem implements ISerializable{

		private long id;//与 checkPointStart 一一对应
		
		public CheckPointEnd(long id){
			this.id = id;
		}
		@Override
		public ByteBuffer encode() {
			ByteBuffer bb = ByteBuffer.allocate(JournalCheckPointLength + 1);
			bb.put(CHECK_POINT_END);//第一个字段
			bb.putLong(id);
			bb.flip();
			return bb;
		}

		@Override
		public void decode(ByteBuffer bb) {
			bb.rewind();
			if(bb.limit() != JournalCheckPointLength){
				throw new IllegalArgumentException();
			}
			this.id = bb.getLong();
			
		}
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		@Override
		public String toString() {
			return "CheckPointEnd [id=" + id + ", getFileName()="
					+ getFileName() + ", getPosition()=" + getPosition()
					+ ", toString()=" + super.toString() + ", getClass()="
					+ getClass() + ", hashCode()=" + hashCode() + "]";
		}
	

		
		
		
	}
	
	public static final JournalEnd JOURNAL_END = new JournalEnd();
	
	public static final class JournalEnd extends JournalItem implements ISerializable{

		public JournalEnd(){
		}
		@Override
		public ByteBuffer encode() {
			return null;
		}

		@Override
		public void decode(ByteBuffer bb) {
		}
		
	}

	@Override
	public String toString() {
		return "JournalItem [fileName=" + fileName + ", position=" + position
				+ "]";
	}
	
	
	
}
