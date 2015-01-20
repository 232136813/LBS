package com.lenovo.vctl.common.lbs;

import gnu.trove.procedure.TLongFloatProcedure;
import gnu.trove.procedure.TLongProcedure;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lenovo.vctl.common.lbs.cluster.JGroupsBackUp;
import com.lenovo.vctl.common.lbs.core.ILbsService;
import com.lenovo.vctl.common.lbs.core.ISerializable;
import com.lenovo.vctl.common.lbs.core.TargetLocation;
import com.lenovo.vctl.common.lbs.core.TimeFilter;
import com.lenovo.vctl.common.lbs.datasource.AbstractDataSource;
import com.lenovo.vctl.common.lbs.datasource.DataSourceLoadStrategy;
import com.lenovo.vctl.common.lbs.datasource.DataSourceType;
import com.lenovo.vctl.common.lbs.datasource.IDataSource;
import com.lenovo.vctl.common.lbs.datasource.IDataSourceLoader;
import com.lenovo.vctl.common.lbs.datasource.MemcacheqDataSource;
import com.lenovo.vctl.common.lbs.datasource.StartUpDataSourceLoader;
import com.lenovo.vctl.common.lbs.exception.LbsException;
import com.lenovo.vctl.common.lbs.persistent.CheckPointWrapper;
import com.lenovo.vctl.common.lbs.persistent.IPersistentable;
import com.lenovo.vctl.common.lbs.persistent.JournalItem;
import com.lenovo.vctl.common.lbs.persistent.TargetLocationJournalPersistor;
import com.lenovo.vctl.common.lbs.rtree.RTree;
import com.lenovo.vctl.common.lbs.util.PriorityQueue;
import com.lenovo.vctl.message.LbsBackupMessage;
import com.lenovo.vctl.message.channel.Channel;

/**
 * 注意使用模式
 * @author songkun1
 *
 */
public class LbsManager implements ILbsService<TargetLocation>{
	
	private static final Logger logger = LoggerFactory.getLogger(LbsManager.class);
	
	public static final long DefaultItemExpireTimeInMill = 1*1000*60*30;// 对象的存活时间

	public static final long DefaultCleanTimerTaskPeriod= 1*1000*60*20;//20分钟//20分钟清理一次 log
	
	public static final long DefaultCheckPointInterval =  1*1000*60*60*2; //两小时 checkPoint一次
	
	
	
	private long cleanTimerTaskPeriodMs; //毫秒

	private long checkPointTimerTaskPeriodMs;
	
	private Properties prop;
	private RTree rTree;
	private IPersistentable<JournalItem> persistor;
	private static final String DEFAULT_DATADIR = "/data/LBS"; 
	private static final String DEFAULT_PROJECTNAME = "default_lbs"; 
	private String dataDir;
	private File dataDirFile;
	private String projectName;
	private long syncPeriod;
	private ScheduledExecutorService service;
	private boolean delayWrite;
	private ScheduledExecutorService delayWriteScheduler;//延迟写
	private byte projectId = -1;//没有project id 不能启动
	
	private JGroupsBackUp backUp;
	private Channel channel;
	public static final boolean SendBackupMessage = false;//send 主备同步信息
	private IDataSource dataSource;
	private IDataSourceLoader<TargetLocation> dataSourceLoader;
	private Thread dataLoaderThread;//获取data数据的 loader线程
	
	//单个journal文件的大小
	private long maxFileSize;
	private static final class CleanJournalTimerTask extends TimerTask{
		IPersistentable<JournalItem> persistor;
		public CleanJournalTimerTask(IPersistentable p){
			this.persistor = p;
		}
		@Override
		public void run() {
			logger.info("Check Clean Journal Task Running!");
			persistor.cleanOldEntities(0l);
		}
	}
	private static final class CheckPointTimerTask extends TimerTask{
		IPersistentable<JournalItem> persistor;
		public CheckPointTimerTask(IPersistentable p){
			this.persistor = p;
		}
		@Override
		public void run() {
			logger.info("Check Point Task Running!");
			persistor.checkPoint();
		}
	}
	private CleanJournalTimerTask cleanJournalTimerTask;
	private CheckPointTimerTask checkPointTimerTask;
	private final Timer timer = new Timer("Journal-Timer", true);
	
	
		
	private static final class DelayJob implements Runnable{
		public static final boolean Add = true;
		public static final boolean Delete = false;
		private boolean add = Add;
		float x, y; long id; byte type;
		long timeStamp;
		public RTree rtree;
		public DelayJob(RTree rtree, long id){
			this.rtree = rtree;
			this.id = id;
			this.add = Delete;
		
		}
		public DelayJob(RTree rtree, float x, float y, long id, long timeStamp, byte type){
			this.rtree = rtree;
			this.x = x;
			this.y = y;
			this.id = id;
			this.timeStamp = timeStamp;
			this.type = type;
		}
		@Override
		public void run() {
			if(add)
				this.rtree.add(x, y, id, timeStamp, type);
			else
				this.rtree.delete(id);
		}
		
	}
	
//	private final Queue delayWriteQueue;
		
	public LbsManager(){

	}

	public LbsManager(Properties prop){
		this.prop = prop;
	}
	public void setProp(Properties p){
		this.prop = p;
	}
	
	public void init()throws Exception{
		this.rTree = new RTree();
		Long i = null;
		Integer dataSourceType = -1;// DataSourceType
		Integer dataSourceLoadStrategy = -1;//DataSourceLoadStrategy
		logger.info("Init LbsManager use Properties p = " + this.prop );
		if(prop != null){
			if(prop.getProperty("DataDir") != null){
				this.dataDir = prop.getProperty("DataDir");
			}else{
				this.dataDir = DEFAULT_DATADIR;
			}
			if(prop.getProperty("SyncPeriod") != null){
				try{
					i = Long.parseLong(prop.getProperty("SyncPeriod"));
				}catch(Exception e){
					i = null;
				}
			}
			if(prop.getProperty("ProjectName") != null && !prop.getProperty("ProjectName").trim().equals("")){
				this.projectName = prop.getProperty("ProjectName");
			}else{
				this.projectName =DEFAULT_PROJECTNAME;
			}
			if(prop.get("DelayWrite") != null && prop.getProperty("DelayWrite").trim().equals("1")){
				this.delayWrite = true;
			}else{
				this.delayWrite = false;
			}
			
			if(prop.getProperty("MaxJournalSize") != null){
				try{
					this.maxFileSize = Long.parseLong(prop.getProperty("MaxJournalSize", "-1"));
				}catch(Exception e){
					this.maxFileSize = -1;
				}
				
			}else{
				this.maxFileSize = -1;
			}
			
			if(prop.getProperty("CleanJournaIntevelInMinu") != null){
				try{
					this.cleanTimerTaskPeriodMs = TimeUnit.MINUTES.toMillis(Long.parseLong(prop.getProperty("CleanJournaIntevelInMinu")));
				}catch(Exception e){
				}
			}
			if(prop.getProperty("CheckPointIntevelInMinu") != null){
				try{
					this.checkPointTimerTaskPeriodMs = TimeUnit.MINUTES.toMillis(Long.parseLong(prop.getProperty("CheckPointIntevelInMinu")));
				}catch(Exception e){
				}
			}
			if(prop.getProperty("ProjectId") != null){
				try{
					this.projectId = Byte.parseByte(prop.getProperty("ProjectId"));
				}catch(Exception e){
					this.projectId = -1;
				}
			}
			if(prop.getProperty("DataSourceType") != null){
				try{
					dataSourceType = Integer.parseInt(prop.getProperty("DataSourceType"));
				}catch(Exception e){
					dataSourceType = 0;
				}
			}
			if(prop.getProperty("DataSourceLoadStrategy") != null){
				try{
					dataSourceLoadStrategy = Integer.parseInt(prop.getProperty("DataSourceLoadStrategy"));
				}catch(Exception e){
					dataSourceLoadStrategy = 0;
				}
			}
			
		}else{
			this.dataDir = DEFAULT_DATADIR;
			this.projectName =DEFAULT_PROJECTNAME;
			this.delayWrite = true;
			this.maxFileSize = -1;
		}
		if(this.projectId <= 0) throw new Exception("Project Id is not legal ! please check your properties files and you can get some information in vctl-message project");
		this.backUp = new JGroupsBackUp(this);
		//暂时定义位  udp 消息传递  如果考虑到消息安全型 可以使用 tcp传输
		this.channel = new Channel(projectName, "udp.xml");
		this.channel.setHandler(backUp);//消息处理
		this.rTree.init(prop);//rTree初始化
		this.syncPeriod = (i == null)? -1 : i;
		this.dataDirFile = new File(dataDir);
		this.dataDirFile.mkdirs();//第一次启动
		this.service = Executors.newSingleThreadScheduledExecutor();
		this.persistor = new TargetLocationJournalPersistor(this.dataDirFile, this.projectName, this.service, this.syncPeriod, this.rTree, this.maxFileSize);
		this.persistor.open();
		this.delayWriteScheduler = Executors.newSingleThreadScheduledExecutor();
		this.rTree.setLbsManager(this);
		this.cleanJournalTimerTask = new CleanJournalTimerTask(this.persistor);
		this.checkPointTimerTask = new CheckPointTimerTask(this.persistor);
		if(this.cleanTimerTaskPeriodMs <= 0)this.cleanTimerTaskPeriodMs = DefaultCleanTimerTaskPeriod;
		if(this.checkPointTimerTaskPeriodMs <= 0)this.checkPointTimerTaskPeriodMs = DefaultCheckPointInterval;
		this.timer.schedule(this.cleanJournalTimerTask, cleanTimerTaskPeriodMs, cleanTimerTaskPeriodMs);
		this.timer.schedule(this.checkPointTimerTask, checkPointTimerTaskPeriodMs, checkPointTimerTaskPeriodMs);
		Class dataSourceClazz = DataSourceType.getClazzByType(dataSourceType);
		if(dataSourceClazz != null){
			this.dataSource = (IDataSource)dataSourceClazz.getConstructor(LbsManager.class).newInstance(this);
			Class dataSourceLoaderClazz = DataSourceLoadStrategy.getClazzByType(dataSourceLoadStrategy);
			if(dataSourceLoaderClazz != null){
				this.dataSourceLoader = (IDataSourceLoader)dataSourceLoaderClazz.getConstructor(IDataSource.class, LbsManager.class).newInstance(this.dataSource, this);
				this.dataLoaderThread = new Thread(dataSourceLoader);
			}
		}

		replay();
		if(this.dataLoaderThread != null)
			this.dataLoaderThread.start(); //启动数据loader线程
	}

	

	public void start(){
	
	}
	
	public void shutDown(){
		service.shutdown();
		logger.info("service shutdown !");
	}

	private void replay() {
		logger.info("#################################################");
		logger.info("replay journal start !");
		CheckPointWrapper checkPoint = persistor.findLastestCheckPoint();
		if(checkPoint.getTargetCheckPoint() != null){
			logger.info("replay Journal from CheckPoint = " + checkPoint);
			persistor.replayStartFrom(checkPoint.getTargetCheckPoint().getFileName(), checkPoint.getTargetCheckPoint().getPosition());
		}else{
			logger.info("replay Journal All");
			persistor.replay();//
		}
		logger.info("replay journal finsh !");
		logger.info("#################################################");
	}
	


	@Override
	public List<TargetLocation> getNearestN(final float x, final float y, final long id, final long timeStamp, final byte type, float distance, int num,
			final boolean exposeSelf, boolean sendBackupMessage)  throws LbsException{
		if(persistor.isInReplay()){
			throw new LbsException("Lbs Service is in replaying !");
		}
		PriorityQueue pq = new PriorityQueue(PriorityQueue.SORT_ORDER_ASCENDING, num);
		final List<TargetLocation> result = new ArrayList<TargetLocation>();
		TLongFloatProcedure pro = new TLongFloatProcedure(){
			@Override
			public boolean execute(long key, float value) {
				TargetLocation location = rTree.getTargetLocation(key);
				if(location != null && location.getTargetId() != id){//过滤自己
					location.setDistance(value);
					boolean isTarget = true;
					if(type != ILbsService.IGNORE_TYPE && type != location.getType()){
						isTarget = false;
					}
					if(isTarget)
						result.add(location);
				}
				return true;
			}

		};
		rTree.nearestN(x, y, pro, num, distance, pq);
//		long timeStamp = System.currentTimeMillis();
		if(exposeSelf){//如果要暴漏自己的地址
			TargetLocation tl = new TargetLocation();
			tl.setTargetId(id);
			tl.setTimeStamp(timeStamp <= 0? System.currentTimeMillis() : timeStamp);
			tl.setType(type);
			tl.setX(x);
			tl.setY(y);
			JournalItem.JournalAdd add = new JournalItem.JournalAdd(tl);
			persistor.write(add);//持久化
			if(delayWrite){
				DelayJob write = new DelayJob(rTree, x, y, id, timeStamp, type);
				delayWriteScheduler.submit(write);
			}else{
				rTree.add(tl);//直接更新
			}
		}
		if(sendBackupMessage)
			sendAddBackupMessage(x, y, id, timeStamp, type);
		return result;
	}


	@Override
	public boolean exposeTarget(float x, float y, long id, byte type, boolean sendBackupMessage) throws LbsException{
		return exposeTarget(x, y, id, System.currentTimeMillis(), type, sendBackupMessage);
	}


	@Override
	public boolean clearSelfLocation(long id, boolean sendBackupMessage)  throws LbsException{
		if(persistor.isInReplay()){
			throw new LbsException("Lbs Service is in replaying !");
		}
		JournalItem.JournalDelete del = new JournalItem.JournalDelete(id);
		persistor.write(del);//持久化
		if(delayWrite){
			DelayJob write = new DelayJob(rTree, id);
			delayWriteScheduler.submit(write);
		}else{
			return rTree.delete(id);//直接更新
		}	
		if(sendBackupMessage)
			sendDeleteBackupMessage(id);
		return true;
	}
	
	
	private void sendDeleteBackupMessage(long targetId){
		if(channel != null){
			LbsBackupMessage msg = new LbsBackupMessage();
			msg.setTargetId(targetId);
			msg.setLbsProject(this.projectId);
			msg.setAddOrDel(TargetLocation.DEL);
			try {
				channel.sendMessage(msg);
			} catch (Exception e) {
				logger.error("Send Lbs BackUp Message error msg = "  + msg);
			}
		}
	}
	
	private void sendAddBackupMessage(float x, float y, long targetId, long timeStamp, byte type){
		if(channel != null){
			LbsBackupMessage msg = new LbsBackupMessage();
			msg.setTargetId(targetId);
			msg.setLbsProject(this.projectId);
			msg.setLbsType(type);
			msg.setTimeStamp(timeStamp);
			msg.setX(x);
			msg.setY(y);
			msg.setAddOrDel(TargetLocation.ADD);
			try {
				channel.sendMessage(msg);
			} catch (Exception e) {
				logger.error("Send Lbs BackUp Message error msg = "  + msg);
			}
		}
	}
	
	public void delayDeleteExpireLocation(long id){
		DelayJob write = new DelayJob(rTree, id);
		delayWriteScheduler.submit(write);
	}

	
	public byte getProjectId(){
		return this.projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public boolean exposeTarget(float x, float y, long id,
			long timeStamp, byte type, boolean sendBackupMessage) throws LbsException {
			if(persistor.isInReplay()){
				throw new LbsException("Lbs Service is in replaying !");
			}
			if((timeStamp + rTree.getItemExpireTime()) <= System.currentTimeMillis())return true;
			TargetLocation tl = new TargetLocation();
			tl.setTargetId(id);
			tl.setTimeStamp(timeStamp);
			tl.setType(type);
			tl.setX(x);
			tl.setY(y);
			JournalItem.JournalAdd add = new JournalItem.JournalAdd(tl);
			persistor.write(add);//持久化
			if(delayWrite){
				DelayJob write = new DelayJob(rTree, x, y , id, timeStamp, type);
				delayWriteScheduler.submit(write);
			}else{
				return rTree.add(tl);//直接更新
			}	
			if(sendBackupMessage)
				sendAddBackupMessage(x, y, id, timeStamp, type);
			return true;
		}

	@Override
	public List<TargetLocation> getNearestN(final float x, final float y, final long id,
			long timeStamp, final byte type, final TLongProcedure timeFilter, float distance, int num,
			boolean exposeSelf, boolean sendBackupMessage) throws LbsException {
		if(persistor.isInReplay()){
			throw new LbsException("Lbs Service is in replaying !");
		}
		PriorityQueue pq = new PriorityQueue(PriorityQueue.SORT_ORDER_ASCENDING, num);
		final List<TargetLocation> result = new ArrayList<TargetLocation>();
		TLongFloatProcedure pro = new TLongFloatProcedure(){
			@Override
			public boolean execute(long key, float value) {
				TargetLocation location = rTree.getTargetLocation(key);
				if(location != null && location.getTargetId() != id){//过滤自己
					location.setDistance(value);
					boolean isTarget = true;
					if(type != ILbsService.IGNORE_TYPE && type != location.getType()){
						isTarget = false;
					}
					if(timeFilter != null && !timeFilter.execute(location.getTimeStamp())){
						isTarget = false;
					}
					if(logger.isDebugEnabled())
						logger.debug("LBS GetNearestN tagget = " + location + "; type = " + type +"; isTarget = " + isTarget);
					if(isTarget)
						result.add(location);
				}
				return true;
			}

		};
		rTree.nearestN(x, y, pro, num, distance, pq);
//		long timeStamp = System.currentTimeMillis();
		if(exposeSelf){//如果要暴漏自己的地址
			TargetLocation tl = new TargetLocation();
			tl.setTargetId(id);
			tl.setTimeStamp(timeStamp <= 0? System.currentTimeMillis() : timeStamp);
			tl.setType(type);
			tl.setX(x);
			tl.setY(y);
			JournalItem.JournalAdd add = new JournalItem.JournalAdd(tl);
			persistor.write(add);//持久化
			if(delayWrite){
				DelayJob write = new DelayJob(rTree, x, y, id, timeStamp, type);
				delayWriteScheduler.submit(write);
			}else{
				rTree.add(tl);//直接更新
			}
		}
		if(sendBackupMessage)
			sendAddBackupMessage(x, y, id, timeStamp, type);
		return result;
	}


	
}
