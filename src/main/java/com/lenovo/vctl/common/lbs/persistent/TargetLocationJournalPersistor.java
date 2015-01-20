package com.lenovo.vctl.common.lbs.persistent;

import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import com.lenovo.vctl.common.lbs.LbsManager;
import com.lenovo.vctl.common.lbs.core.ISerializable;
import com.lenovo.vctl.common.lbs.core.TargetLocation;
import com.lenovo.vctl.common.lbs.exception.JournalItemBrokenException;
import com.lenovo.vctl.common.lbs.persistent.JournalItem.CheckPointEnd;
import com.lenovo.vctl.common.lbs.persistent.JournalItem.CheckPointStart;
import com.lenovo.vctl.common.lbs.persistent.JournalItem.JournalAdd;
import com.lenovo.vctl.common.lbs.persistent.JournalItem.JournalDelete;
import com.lenovo.vctl.common.lbs.rtree.RTree;

public class TargetLocationJournalPersistor extends JournalPersistor<JournalItem>{
	private final ByteBuffer buffer = ByteBuffer.allocate(1);
	
	public TargetLocationJournalPersistor(File dir, String project,
			ScheduledExecutorService scheduler, long syncPeriod, RTree rtree, long maxFileSize) {
		super(dir, project, scheduler, syncPeriod, rtree, maxFileSize);
	}

	@Override
	public void replayFile(String fileName, long position){
		try{
			FileChannel fc = new FileInputStream(new File(persistorDir, fileName)).getChannel();
			fc.position(position);
			try{
				int i = 0;
				boolean done = false;
				do{
					JournalItem item = readJournalEntry(fc);
					if(logger.isDebugEnabled())
						logger.debug("replay journals get item = " + item);
					if(item instanceof JournalItem.JournalEnd)
						done = true;
					else if(item instanceof JournalItem.JournalAdd) {
						rTree.add(((JournalItem.JournalAdd) item).getTargetLocation());	//insert into the tree
					}else if(item  instanceof JournalItem.JournalDelete){
						rTree.delete(((JournalItem.JournalDelete) item).getTargetId());
					}else if(item instanceof JournalItem.CheckPointStart){
						//skip
					}else if(item instanceof JournalItem.CheckPointEnd){
						//skip
					}else{
						throw new RuntimeException("Not Recognized Item Error fileName = "+fileName+" item = " + item);
					}
					i++;
				}while(!done);
				if(logger.isDebugEnabled()){
					logger.debug("replay journal end journal = " + fileName + "item count = " + i);
				}
			}catch(JournalItemBrokenException e){
				logger.error("Replay Journal File Error System Will TruncateJournal " + fileName +", Position = " + e.getPosition(), e);
				truncateJournal(fc, e.getPosition());
			}finally{
				if(fc != null && fc.isOpen()){
					try{
						fc.close();
					}catch(Exception ep){}
				}
			}
		}catch(FileNotFoundException e){
	        logger.error("No journal for '{}'; starting with empty queue.", projectName);
		}catch(IOException e){
	        logger.error("Exception replaying journal for '"+projectName+"': "+ fileName, e);
	        logger.error("DATA MAY HAVE BEEN LOST!");
		}
	}
	
	protected JournalItem readJournalEntry(FileChannel in) throws IOException{
		buffer.rewind();
		buffer.limit(1);
	  	long lastPosition = in.position();
	  	int x = 0;
	    do {
	      x = in.read(buffer);
	    } while (buffer.position() < buffer.limit() && x >= 0);

	    if (x < 0) {
	    	return JournalItem.JOURNAL_END;
	    } else {
	      try {
	    	buffer.flip();
	        byte tmp = buffer.get();
	        ByteBuffer data = null;
	        switch(tmp){
	        	case JournalItem.ADD :
	        		data = readBlock(in, JournalItem.JournalAddLength);// throw IoException
	        		JournalAdd add = new JournalItem.JournalAdd();
	        		add.decode(data);
	        		return add;
	        	case JournalItem.DELETE :
	        		data = readBlock(in, JournalItem.JournalDeleteLength);
	        		JournalDelete delete = new JournalItem.JournalDelete();
	        		delete.decode(data);
	        		return delete;
	        		//
	        	case JournalItem.CHECK_POINT_START :
	        		data = readBlock(in, JournalItem.JournalCheckPointLength);
	        		CheckPointStart checkStart = new CheckPointStart();
	        		checkStart.decode(data);
	        		checkStart.setPosition(lastPosition);
	        		return checkStart;
	           	case JournalItem.CHECK_POINT_END :
	        		data = readBlock(in, JournalItem.JournalCheckPointLength);
	        		CheckPointStart checkEnd = new CheckPointStart();
	        		checkEnd.decode(data);
	        		checkEnd.setPosition(lastPosition);//获取checkPoint的 位置信息
	        		return checkEnd;
	        	default  :
	        		throw new JournalItemBrokenException(lastPosition, new IOException("invalid opcode in journal: " + tmp + " at position " + (lastPosition)));
	        }

	      } catch (IOException e){
	    	  logger.error("ReadJournalEntry BrokenItemException  lastPosition = " + lastPosition, e);
	          throw new JournalItemBrokenException(lastPosition, e);
	      } catch (Exception e){
	    	  logger.error("ReadJournalEntry Exception  lastPosition = " + lastPosition, e);
	          throw new JournalItemBrokenException(lastPosition, e);
	      }
	    }

	}

	
	public void cleanOldEntities(long timeStamp){
		//
		long timeToDelete = timeStamp <= 0 ? (System.currentTimeMillis()- rTree.getItemExpireTime()) : timeStamp;//
		List<String> journals = journals(persistorDir, projectName);
		if(journals == null){
			logger.info("Clean Old Files files files null");
			return;
		}
		logger.info("Clean Old Files files files = " + journals  +"; TimeToDelete " + timeToDelete);
		for(String fileName : journals){
			if(fileName != null && fileName.indexOf(".") > 0){
				String[] tmp = fileName.split("\\.");
				try {
					if(tmp.length == 2 && timeToDelete >= Long.parseLong(tmp[1])){
						File f = new File(persistorDir, fileName);
						boolean flag = false;
						try {
							flag = f.delete();
						} catch (Exception e) {
						}
						logger.info("Clean Old Jounral f = " + f.getName() + " flag = "+Boolean.toString(flag));
						
					}
				} catch (NumberFormatException e) {
					logger.error("Clean Old Journals Error ", e);
				}
			}else{
				logger.info("Clean Old Journals file name is " + fileName);
			}

		}
		
	}
	@Override
	public boolean checkPoint(){
		//check point start
		CheckPointStart checkStart = new CheckPointStart();
		CheckPointEnd checkEnd = new CheckPointEnd(checkStart.getId());
		write(checkStart);
		//Flush All Items in the rTree to disk
		TLongObjectHashMap<TargetLocation> objects = rTree.copyTreeObjects();
		if(objects != null){
			for(Object o : objects.values()){
				if((((TargetLocation)o).getTimeStamp() + rTree.getItemExpireTime()) > System.currentTimeMillis()){//checkpoint 没有过期的对象就可以了
					JournalItem.JournalAdd journalAdd = new JournalItem.JournalAdd((TargetLocation)o);
					write(journalAdd);
				}

			}
		}
		//check point end
		write(checkEnd);
		cleanOldEntities(checkStart.getId());
		return true;
	}

	@Override
	protected void replayFile4CheckPoint(String fileName,
			CheckPointWrapper checkPoint) {
		if(fileName == null || checkPoint == null)throw new IllegalArgumentException("Replay File for Check Point Error fileName = " + fileName + " checkPoint = " + checkPoint);
		
		try{
			FileChannel fc = new FileInputStream(new File(persistorDir, fileName)).getChannel();
			try{
				int i = 0;
				boolean done = false;
				do{
					JournalItem item = readJournalEntry(fc);
					if(item instanceof JournalItem.JournalEnd)
						done = true;
					else if(item instanceof JournalItem.CheckPointStart){
						((CheckPointStart) item).setFileName(fileName);
						checkPoint.setTmpCheckPointStart((CheckPointStart)item);
					}else if(item instanceof JournalItem.CheckPointEnd){
						((CheckPointEnd) item).setFileName(fileName);
						checkPoint.setTmpCheckPointEnd((CheckPointEnd)item);
					}else{
						//skip
						continue;
					}
					if(checkPoint.getTmpCheckPointStart() != null && checkPoint.getTmpCheckPointEnd() != null){
						if(checkPoint.getTmpCheckPointStart().getId() == checkPoint.getTmpCheckPointEnd().getId()){//配对的 checkPoint
							checkPoint.setTargetCheckPoint(checkPoint.getTmpCheckPointStart());
						}
					}
					i++;
				}while(!done);
				if(logger.isDebugEnabled()){
					logger.debug("replay journal end journal = " + fileName + "item count = " + i);
				}
			}catch(JournalItemBrokenException e){
				logger.error("Replay Journal File Error System Will TruncateJournal " + fileName +", Position = " + e.getPosition(), e);
				truncateJournal(fc, e.getPosition());
			}finally{
				if(fc != null && fc.isOpen()){
					try{
						fc.close();
					}catch(Exception ep){}
				}
			}
		}catch(FileNotFoundException e){
	        logger.error("No journal for '{}'; starting with empty queue.", projectName);
		}catch(IOException e){
	        logger.error("Exception replaying journal for '"+projectName+"': "+ fileName, e);
	        logger.error("DATA MAY HAVE BEEN LOST!");
		}
		
		
	}



	
	
}
