package com.lenovo.vctl.common.lbs.core;

import java.util.Calendar;
import java.util.Date;

public enum TimeFilter {

	ANY{
		public int getValue(){
			return ANY_VALUE;
		}
	},TODAY{
		public int getValue(){
			return TODAY_VALUE;
		}
	},TOMORROW{
		public int getValue(){
			return TOMORROW_VALUE;
		}
	}, ONEWEEK{
		public int getValue(){
			return ONEWEEK_VALUE;
		}
	};
	
	public abstract int getValue();
	public static final int ANY_VALUE = 0;
	public static final int TODAY_VALUE = 1;
	public static final int TOMORROW_VALUE = 2;
	public static final int ONEWEEK_VALUE = 3;

	public static long getToday(){
		Calendar c = Calendar.getInstance();
		c.set(c.get(c.YEAR), c.get(c.MONTH), c.get(c.DATE)+1, 0, 0, 0);
		return c.getTimeInMillis();
	}
	
	public static long getTomorrow(){
		Calendar c = Calendar.getInstance();
		c.set(c.get(c.YEAR), c.get(c.MONTH), c.get(c.DATE)+2, 0, 0, 0);
		return c.getTimeInMillis();
	}
	
	
	public static long getOneWeek(){
		Calendar c = Calendar.getInstance();
		c.set(c.get(c.YEAR), c.get(c.MONTH), c.get(c.DATE)+8, 0, 0, 0);
		return c.getTimeInMillis();
	}
	
	public static void main(String[] args){
		System.out.println(getOneWeek());
		System.out.println(new Date(1418054400862l));
	}
	
}
