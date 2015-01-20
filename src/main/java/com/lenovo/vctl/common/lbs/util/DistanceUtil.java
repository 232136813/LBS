package com.lenovo.vctl.common.lbs.util;
/**
 * 计算两点之间的距离  
 * @author songkun1
 *
 */



public class DistanceUtil {
		private static final float EARTH_RADIUS = 6378137;  
	    private static float rad(float d)  
	   {  
	        return (float)(d * Math.PI / 180.0);  
	   }  
	    /** 
	     * 基于googleMap中的算法得到两经纬度之间的距离,计算精度与谷歌地图的距离精度差不多，相差范围在0.2米以下 
	     * @param lon1 第一点的经度 
	     * @param lon2 第二点的经度 
	     * @param lat3 第二点的纬度 
	     * @return 返回的距离，单位km 
	     * */  
	    public static float GetDistance(Float lat1, Float long1, Float lat2, Float long2)  
	    
	    {
	    	if(lat1 == null || long1 == null || lat2 == null || long2 == null)return -1;
	    	float radLat1 = rad(lat1);  
	    	float radLat2 = rad(lat2);  
	    	float a = radLat1 - radLat2;  
	    	float b = rad(long1) - rad(long2);  
	    	float s = (float)(2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)))); 
	    	s = s * EARTH_RADIUS;  
	        return (float)s;  
	    }  
	    
	    
	    public static void main(String[] args) {
	    	double x = 39.94784000 ,y= 116.32540000 ;
	    	
	    	System.out.println(DistanceUtil.GetDistance((float)x, (float)y, 40.05309f,116.30143f));
	    }


}
