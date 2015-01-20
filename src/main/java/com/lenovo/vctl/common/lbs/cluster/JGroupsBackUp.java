package com.lenovo.vctl.common.lbs.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lenovo.vctl.common.lbs.LbsManager;
import com.lenovo.vctl.common.lbs.core.TargetLocation;
import com.lenovo.vctl.common.lbs.exception.LbsException;
import com.lenovo.vctl.message.IMessage;
import com.lenovo.vctl.message.LbsBackupMessage;
import com.lenovo.vctl.message.handler.ThreadPoolHandler;

/**
 * jgroups 主备同步
 * @author songkun1
 *
 */
public class JGroupsBackUp extends ThreadPoolHandler{
	private static final Logger logger = LoggerFactory.getLogger(JGroupsBackUp.class);
	private LbsManager manager;
	
	public JGroupsBackUp(LbsManager manager){
		super(1);//单线程
		this.manager = manager;
	}
	
	@Override
	protected void handle0(IMessage message) {
		if(message == null || !(message instanceof LbsBackupMessage) ) return;
		LbsBackupMessage msg = (LbsBackupMessage)(message);
		if(msg.getLbsProject() == manager.getProjectId()){
			TargetLocation target = new TargetLocation();
			target.setTargetId(msg.getTargetId());
			target.setTimeStamp(target.getTimeStamp());
			target.setType(msg.getLbsType());
			target.setX(msg.getX());
			target.setY(msg.getY());
			target.setAddOrDel(msg.getAddOrDel());
			if(target.getTargetId() <= 0) return;// ignore;
			if(msg.getAddOrDel() == TargetLocation.ADD ){
				try {
					manager.exposeTarget(target.getX(), target.getX(), target.getTargetId(), target.getTimeStamp() <= 0? System.currentTimeMillis() : target.getTimeStamp(), target.getType(), false);
				} catch (LbsException e) {
					logger.error("BackUp TargetLocation Add Error target = " + target);
				}
			}else{//删除的消息
				try {
					manager.clearSelfLocation(target.getTargetId(), false);
				} catch (LbsException e) {
					logger.error("BackUp TargetLocation Delete Error target = " + target);
				}
			}
		}
	}

	
	public static void main(String[] args) {
		TargetLocation target = new TargetLocation();
		System.out.println(target.getX() == 0f);
	}
}
