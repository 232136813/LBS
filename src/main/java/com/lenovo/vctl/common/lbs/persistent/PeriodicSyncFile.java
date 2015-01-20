package com.lenovo.vctl.common.lbs.persistent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author songkun1
 *
 */
public class PeriodicSyncFile{
	private Long period;
	private FileChannel writer;
	private AtomicBoolean closed = new AtomicBoolean(false);
	private AtomicInteger promises = new AtomicInteger(0);
	private PeriodicSyncTask periodicSyncTask;

	public PeriodicSyncFile(File file, ScheduledExecutorService scheduler, Long period) throws FileNotFoundException{
		this.period = period;
		this.writer = new FileOutputStream(file, true).getChannel();
		this.periodicSyncTask = new PeriodicSyncTask(scheduler, period, period) {
		    public void run() {
		      if (!closed.get() && !(promises.get() > 0)) fsync();
		    }
		};
	}
	
	private void fsync() {
	    synchronized (this){
	     promises.getAndSet(0);
	      try {
	        writer.force(false);
	      } catch (IOException e){
	        return;
	      }
	      periodicSyncTask.stopIf(promises.get() == 0);
	    }
	}
	
	 public void write(ByteBuffer buffer){
		   do {
		      try {
				writer.write(buffer);
		      } catch (IOException e) {
		    	  e.printStackTrace();
			  }
		    } while (buffer.position() < buffer.limit());
		    if (period == null || period.intValue() < 0 || period==Long.MAX_VALUE) {//不设值或者小于0 或者 等于最大值 都不刷盘 
		    	
		    }else if( period.intValue() == 0){//立刻刷盘
		      try {
		        writer.force(false);
		      } catch (Exception e){
		    	  e.printStackTrace();
		      }
		    } else {
		      promises.getAndIncrement();
		      periodicSyncTask.start();
		    }
	 }

	  public void close() throws IOException {
		    closed.set(true);
		    periodicSyncTask.stop();
		    fsync();
		    writer.close();
		    writer = null;
	 }

	  public Long position() throws IOException{
		  if(writer != null)
			  return writer.position();
		  return null;
	  }


	
	
	
}
