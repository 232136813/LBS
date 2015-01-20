package com.lenovo.vctl.common.lbs.util;

import java.util.HashSet;
import java.util.Set;






import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lenovo.vctl.dal.cache.Cache;
import com.lenovo.vctl.dal.cache.exception.CacheException;
import com.lenovo.vctl.dal.cache.memcached.CacheFactoryImpl;

public class MemcacheqUtil {
 
	private static final Logger log = LoggerFactory.getLogger(MemcacheqUtil.class);
	
	public static Object pop(String qname, String key) throws CacheException {
		Cache cache = CacheFactoryImpl.getInstance().getCache(qname, true);
		return cache.get(key);
	}
	
	public static boolean push(String qname, String key, Object value) throws CacheException{
		Cache cache = CacheFactoryImpl.getInstance().getCache(qname, true);
		log.info("== push to queue, qname : "+ qname +", key : "+ key +", value : "+ value +" ==");
		return cache.put(key, value);
    }
}
