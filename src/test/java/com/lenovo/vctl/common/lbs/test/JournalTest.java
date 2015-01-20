package com.lenovo.vctl.common.lbs.test;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import com.lenovo.vctl.common.lbs.persistent.JournalPersistor;
import com.lenovo.vctl.common.lbs.persistent.TargetLocationJournalPersistor;
import com.lenovo.vctl.common.lbs.rtree.RTree;

public class JournalTest {
	public static void main(String[] args) throws ParseException {
		File dir = new File("d:/LBS");
		List<String> abc = JournalPersistor.journals(dir, "huodong");
		/**
		 * File dir, String project,
			ScheduledExecutorService scheduler, long syncPeriod, RTree rtree, long maxFileSize
		 */
		TargetLocationJournalPersistor per = new TargetLocationJournalPersistor(dir, "huodong", null,0, null,10000);
		per.replay();
		//		FileOutputStream fos = FileOutputStream("");
		
//		float x = 108.100010f;
//		Double d = Double.valueOf(Float.toString(x));
//		BigDecimal bd = new BigDecimal(x);
//		bd.setScale(6, BigDecimal.ROUND_DOWN);
//		System.out.println(d);
//		Date d = new Date();
//		Calendar.getInstance().set(c.g, month, date, hourOfDay, minute, second);
	}
}
