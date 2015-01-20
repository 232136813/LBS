package com.lenovo.vctl.common.lbs.persistent;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lenovo.vctl.common.lbs.rtree.RTree;

/**
 * 
 * @author songkun1
 *
 */

public abstract class JournalPersistor<T extends JournalItem> implements IPersistentable<JournalItem>{

	public static final long DEFAULT_MAX_FILE_SIZE = 5*1024*1024;//5M
	public long maxFileSize = DEFAULT_MAX_FILE_SIZE;
	protected static final Logger logger = LoggerFactory.getLogger(JournalPersistor.class);
	protected volatile File persistorDir;//持久化目录
	protected String projectName;
	protected volatile File currentFile = null;
	protected final AtomicBoolean isInReplay = new AtomicBoolean(true);//是否是在replay过程之中
	protected volatile PeriodicSyncFile writer;
	 /*  8byte userId + 8byte timestamp + 4byte x坐标 + 4byte y坐标 */
	private long syncPeriod;
	private ScheduledExecutorService syncScheduler;
	private AtomicLong currentFileSize = new AtomicLong(0l);
	protected final RTree rTree;
	
	/**
	 * 
	 * @param dir
	 * @param project
	 * @param scheduler
	 * @param syncPeriod
	 * @param rTree
	 * @param maxFileSize
	 */
	public JournalPersistor(File dir, String project, ScheduledExecutorService scheduler, long syncPeriod, final RTree rTree, long maxFileSize){
		this.persistorDir = dir;
		this.projectName = project;
		this.syncScheduler = scheduler;
		this.syncPeriod = syncPeriod;
		this.currentFile = new File(dir, projectName);
		if(currentFile != null){
			this.currentFileSize.getAndSet(currentFile.length());
		}
		this.rTree = rTree;
		if(maxFileSize > 0){
			this.maxFileSize = maxFileSize * 1024;// k
		}
	}
	
	
	public void open()throws FileNotFoundException{
		open(currentFile);
		if(currentFile != null){
			this.currentFileSize.getAndSet(currentFile.length());
		}
	}
	public void open(File file) throws FileNotFoundException{
		writer = new PeriodicSyncFile(file, syncScheduler, syncPeriod);
	}
	
	
	protected File uniqueFile(String infix) {
	    File file = new File(persistorDir, projectName + infix + System.currentTimeMillis());
		try {
			boolean flag = file.createNewFile();
			if(flag){
				return file;
			}
		} catch (IOException e1) {
			logger.error("Create Unique　File Error ", e1);
		}
		return null;
	}
	

	
	protected static Boolean cleanUpPackedFiles(File path,  TreeMap<Long, String> files){  
	    String packFileName = null;
	    Long packTimestamp = null;
	    for(Entry<Long, String> e : files.entrySet()){
	    	if(e.getValue() != null && e.getValue().endsWith(".pack")){
	    		packFileName = e.getValue();
	    		packTimestamp = e.getKey();
	    		break;
	    	}
	    }
	    if(packFileName != null){
	    	SortedMap<Long, String> doomed = files.headMap(packTimestamp, true);
	    	for(Entry<Long, String> e: doomed.entrySet()){
	    		if(!e.getValue().contains(".pack")){
		    		File f = new File(path, e.getValue());
		    		f.delete();
	    		}
	    	}
	    	String newFileName = packFileName.substring(0, packFileName.length() - 5);
	    	new File(path, packFileName).renameTo(new File(path, newFileName));
	    	return true;
	    }else
	    	return false;
	}
	
	/**
	 *  
	 * @param path
	 * @param projectName
	 * @return
	 */
	public static List<String> archivedFiles(File path, String projectName){
		String[] totalFiles = path.list();	
	    if (totalFiles == null) {
	    	return null;
	    } else {
	    	TreeMap<Long, String> timedFiles = new TreeMap<Long, String>();
	    	for(String name : totalFiles){
	    		if(name.startsWith(projectName + ".")){
	    			String[] subNames = name.split("\\.");
	    			if(subNames.length >= 2){
	    				timedFiles.put(Long.parseLong(subNames[1]), name);
	    			}
	    		}
	    	}
	    	
	    	if(cleanUpPackedFiles(path, timedFiles)){
	    		return archivedFiles(path, projectName);
	    	}else{
	    		return Collections.list(Collections.enumeration(timedFiles.values()));
	    	}
	    }
	}
	
	
	@Override
	public List<JournalItem> read(float locationX, float locationY, long targetId,
			int count, boolean isActiveSelf)  {
		throw new UnsupportedOperationException();
	}


	@Override
	public void write(JournalItem o) {
		if(isInReplay()){//如果在replay的时候不能进行写操作
			return;
		}
		if(writer == null)throw new RuntimeException("Writer is Not Opened");
		if(logger.isDebugEnabled())
			logger.debug("Current File Size FileName = " + projectName + "; size = " + currentFileSize.get());
	
			if(currentFileSize.get() >= maxFileSize){//double check
				synchronized(this){//需要同步
					try {
						if(currentFileSize.get() >= maxFileSize){
							rotate();
						}
					} catch (IOException e) {
						logger.error("Write Target to Journal IOException item = " + o, e);
					} catch (InterruptedException e) {
						logger.error("Write Target to Journal InterruptedException item = " + o, e);
					}
					
			}
		}

		ByteBuffer bb = o.encode();
		if(bb != null){
			currentFileSize.getAndAdd(bb.limit());//)
			writer.write(bb);
		}

	}
	
	protected void rotate() throws IOException, InterruptedException{
		File f = uniqueFile(".");
		if(f == null){
			logger.error("Rotate File Error!");
			return;
		}
		boolean flag = this.currentFile.renameTo(f);
		if(!flag)logger.error("Rotate to New File Error f = " + f.getName());
		if(logger.isDebugEnabled())
			logger.debug("Rotate to New File PackFile Name  = " + f.getName() );
		this.currentFile = new File(persistorDir, projectName);
		open();
	}
	
	public void replay(){
		String[] fileNames = persistorDir.list();
		for(String fn : fileNames){
			if(fn.startsWith(projectName + "~~")){
				new File(projectName, fn).delete();
			}
		}
		List<String> journals = journals(persistorDir, projectName);
		logger.info("replaying journals journals list " + journals);
		for(String  fileName : journals){
			replayFile(fileName, 0l);
		}
		isInReplay.getAndSet(false);//replay完成
	
	}
	
	

	@Override
	public void replayStartFrom(String fileName, long position) {
		String[] fileNames = persistorDir.list();
		for(String fn : fileNames){
			if(fn.startsWith(projectName + "~~")){
				new File(projectName, fn).delete();
			}
		}
		List<String> journals = journals(persistorDir, projectName);
		if(journals == null){
			isInReplay.getAndSet(false);//replay完成
			return;
		}
		int index = journals.indexOf(fileName);
		List<String> subJournals = journals.subList(index, journals.size());
		logger.info("replaying journals journals list " + journals);
		for(String  journal : subJournals){
			if(journal.equals(fileName)){
				replayFile(fileName, position);//从指定位置开始
			}else{
				replayFile(fileName, 0l);//从头开始
			}
		
		}
		isInReplay.getAndSet(false);//replay完成

	}


	protected abstract void replayFile(String fileName, long position);
	protected abstract void replayFile4CheckPoint(String fileName, CheckPointWrapper checkPoint);
	
	
	public static List<String> journals(File path, String projectName){
		List<String> fns = archivedFiles(path, projectName);
		if(fns != null)fns.add(projectName);
		return fns;
	}
	public static List<String> journalsReverseOrder(File path, String projectName){
		List<String> files = journals(path, projectName);
		Collections.reverse(files);
		return files;
		
	}
	
	public static List<String> journalsBefore(File path, String projectName, String fileName){
		 List<String> journals = journals(path, projectName);
		 int index = journals.indexOf(fileName);
		 return journals.subList(0, index);
	}
	
	
	public static String journalsAfter(File path, String projectName, String fileName){
		 List<String> journals = journals(path, projectName);
		 if(journals == null)return null;
		 int index = journals.indexOf(fileName);
		 return journals.subList(index, journals.size()).get(0);
	}
	
	public static Set<String> getProjectNamesFromFolder(File path){
		String[] fs = path.list();
		Set<String> fileNames = new HashSet<String>();
		for(String fn : fs){
			if(!fn.contains("~~")){
				fileNames.add(fn.split("\\.")[0]);
			}
		}
		return fileNames;
	}
	public boolean isInReplay(){
		return isInReplay.get();
	}
	
	public static void truncateJournal(final FileChannel fc, Long position) throws IOException{
		FileChannel trancateWriter = fc;
	    try {  
	      trancateWriter.truncate(position);
	    } finally {
	    	if(trancateWriter != null)
				try {
					trancateWriter.close();
				} catch (Exception e) {
				}
	    }
	}
	
	@Override
	public CheckPointWrapper findLastestCheckPoint() {
		String[] fileNames = persistorDir.list();
		for(String fn : fileNames){
			if(fn.startsWith(projectName + "~~")){
				new File(projectName, fn).delete();
			}
		}
		//从最新的文件开始
		List<String> journals = journals(persistorDir, projectName);
		logger.info("Find Lastest Check Point in journals list " + journals);
		CheckPointWrapper checkPoint = new CheckPointWrapper();
		for(String fileName : journals){
			replayFile4CheckPoint(fileName, checkPoint);
		}
		return checkPoint;
	}
	
	protected ByteBuffer readBlock(FileChannel in, int size) throws IOException{
	    ByteBuffer dataBuffer = ByteBuffer.allocate(size);
	    int x= 0;
	    do {
	      x = in.read(dataBuffer);
	    } while (dataBuffer.position() < dataBuffer.limit() && x >= 0);
	    if (x < 0) {
	      // we never expect EOF when reading a block.
	      throw new IOException("Unexpected EOF");
	    }
	    dataBuffer.flip();
	    return dataBuffer;
	}

}
