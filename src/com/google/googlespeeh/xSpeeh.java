package com.google.googlespeeh;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;

import android.R.bool;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.content.SharedPreferences.Editor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;

import android.util.Log;

/**
响应码 	说明 	介绍
1 	GPS定位结果 	通过设备GPS定位模块返回的定位结果
2 	前次定位结果 	网络定位请求低于1秒、或两次定位之间设备位置变化非常小时返回，设备位移通过传感器感知。
4 	缓存定位结果 	返回一段时间前设备在同样的位置缓存下来的网络定位结果
5 	Wifi定位结果 	属于网络定位，定位精度相对基站定位会更好
6 	基站定位结果 	属于网络定位
8 	离线定位结果 	-
*/

public class xSpeeh extends Service implements AMapLocationListener
{
	
	
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	
	public static String Imei = "";
	
	public static int BatteryN=0,BatteryV=0,BatteryT=0;
	
	private static float lux=0;

	private static byte hi=0x00,low=0x00 ;
	
	static final int DOGETINFO = 1;
	public static boolean isstart = false;
	
	public boolean autobatch = true;  //是否为自动模式
	
	private static SensorManager smManager;
	
	private static Sensor lightsensor ;
	
	private static final int DOACK = 100;
	public static AMapLocation loc,loc_new;
	public static Double lat=0.0,lng=0.0; //lng 经度  lat 纬度
	public static String lc_street="",lc_province="",lc_city="",
			lc_district="",lc_address="",lc_locationtype="",
			lc_provider="",lc_accuracy="";
	public static String serverip = "0.0.0.0";
	float test = 123.456766982194093763021f;
	public static boolean firstgps = true,LC_CHANGED = false;
	public static int weakup = 0;
	public boolean ackthreadstart = false;
	
	public static boolean networkok = false,connect2server = false;
	

	
	public static boolean[] watertime; //是否已经执行淋水计划的数组
	
	public static boolean suning = false;  //是否正在增光中
	
	public static String[] water,suntime;
	
	public static int watercount = 0;

	public static int[] getBatter()
	{
		int[] batinfo = {BatteryN,BatteryV,BatteryT};  //温度是一个整数231 代表23.1°
		return batinfo;
	}
	
	public static float getLux()
	{
		float mlux = lux;  //温度是一个整数231 代表23.1°
		return mlux;
	}
	
	public class Tea {
		//加密
		public byte[] encrypt(byte[] content, int offset, int[] key, int times){//times为加密轮数
		int[] tempInt = byteToInt(content, offset);
		int y = tempInt[0], z = tempInt[1], sum = 0, i;
		int delta=0x9e3779b9; //这是算法标准给的值
		int a = key[0], b = key[1], c = key[2], d = key[3];

		for (i = 0; i < times; i++) {
		sum += delta;
		y += ((z << 4) + a) ^ (z + sum) ^ ((z >> 5) + b);
		z += ((y << 4) + c) ^ (y + sum) ^ ((y >> 5) + d);
		}
		tempInt[0]=y;
		tempInt[1]=z;
		return intToByte(tempInt, 0);
		}
		//解密
		public byte[] decrypt(byte[] encryptContent, int offset, int[] key, int times){
		int[] tempInt = byteToInt(encryptContent, offset);
		int y = tempInt[0], z = tempInt[1], sum = 0xC6EF3720, i;
		int delta=0x9e3779b9; //这是算法标准给的值
		int a = key[0], b = key[1], c = key[2], d = key[3];

		for(i = 0; i < times; i++) {
		z -= ((y << 4) + c) ^ (y + sum) ^ ((y >> 5) + d);
		y -= ((z << 4) + a) ^ (z + sum) ^ ((z >> 5) + b);
		sum -= delta;
		}
		tempInt[0] = y;
		tempInt[1] = z;

		return intToByte(tempInt, 0);
		}
		//byte[]型数据转成int[]型数据
		private int[] byteToInt(byte[] content, int offset){

		int[] result = new int[content.length >> 2]; //除以2的n次方 == 右移n位 即 content.length / 4 == content.length ＞＞ 2
		for(int i = 0, j = offset; j < content.length; i++, j += 4){
		result[i] = transform(content[j + 3]) | transform(content[j + 2]) <<  8 |
		transform(content[j + 1]) << 16 | (int)content[j] << 24;
		}
		return result;

		}
		//int[]型数据转成byte[]型数据
		private byte[] intToByte(int[] content, int offset){
		byte[] result = new byte[content.length << 2]; //乘以2的n次方 == 左移n位 即 content.length * 4 == content.length ＜＜ 2
		for(int i = 0, j = offset; j < result.length; i++, j += 4){
		result[j + 3] = (byte)(content[i] & 0xff);
		result[j + 2] = (byte)((content[i] >> 8) & 0xff);
		result[j + 1] = (byte)((content[i] >> 16) & 0xff);
		result[j] = (byte)((content[i] >> 24) & 0xff);
		}
		return result;
		}
		//若某字节被解释成负的则需将其转成无符号正数
		private  int transform(byte temp){
		int tempInt = (int)temp;
		if(tempInt < 0){
		tempInt += 256;
		}
		return tempInt;
		}

		}

	
	Handler handle1 = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what)
			{
			case DOGETINFO:
				
				break;

			default:
				break;
			}
		}
        
	};
	
	
	
	
	/**
	 * 比较传进来的时间与实际时间的大小，如果当前时间比参数大，返回true，小则返回false
	 * @param time
	 * @return boolean
	 */
	public boolean bijiaotime(String time) //比较当前系统时间与传进来的时间大小
	{
		  String a = "00:00:00";
		   
		   SimpleDateFormat df = new SimpleDateFormat("HHmmss");
		   a = time.replace(":", "");
		   String b = df.format( new java.util.Date(System.currentTimeMillis()));
		   
		   
		   if (Integer.valueOf(a)-Integer.valueOf(b)>0)
		   {
			   return false;
		   }
		   else {
			  return true;
		   }
	}
	
	
	
	
	
	 Thread thread1_loopgetinfo = new Thread()
	 {

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			super.run();
		
			while (isstart) //如果当前activity退出就不继续执行
			{	
				try
				{
					
					Message msg1 = new Message();
					msg1.what = DOGETINFO;
					handle1.sendMessage(msg1);
					
					Thread.sleep(15000);  //每隔多少秒调用一次，相当于一个timer使用
					
					Log.e("loghere", "GoogleSpeeh-->looptimeup"+Thread.currentThread().getId());
					
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		
        
	 };
	
	
	 BroadcastReceiver br1 = new BroadcastReceiver()
		{	
			@Override
			public void onReceive(Context context, Intent intent)
			{
				// TODO Auto-generated method stub
			    Log.e("loghere", "recever--->wakeup"+weakup);
			    weakup += 1;
			    if (serverip.toString().equals("0.0.0.0") || !connect2server)
			    { 
			    	Intent intent2 = new Intent();
			    	 intent2.setClass(xSpeeh.this, SocketService.class);
						
						intent2.putExtra("command", "getipaddr");
						startService(intent2);
			    }
			    
			    if (isNetworkAvailable() && connect2server)	 
			    {
			      Intent intent2 = new Intent();
		    	   intent2.setClass(xSpeeh.this, SocketService.class);
					
					intent2.putExtra("command", "uploaddata");
					startService(intent2);
			    }
			}
			
		};
		
		BroadcastReceiver br2 = new BroadcastReceiver()
		{	
			@Override
			public void onReceive(Context context, Intent intent)
			{
				// TODO Auto-generated method stub
			    Log.e("loghere", "recever-->sendack");
			    
			    if (isNetworkAvailable() && connect2server)	 
			    {   
				   Intent intent2 = new Intent();
				   intent2.setClass(xSpeeh.this, SocketService.class);
					
					intent2.putExtra("command", "doack");
					startService(intent2);
			    }else if (isNetworkAvailable() && !connect2server)
			    {
			    	Intent intent2 = new Intent();
			    	 intent2.setClass(xSpeeh.this, SocketService.class);
						
						intent2.putExtra("command", "getipaddr");
						startService(intent2);
			    }
			   /* Thread_ack mThread_ack = new Thread_ack();
			    mThread_ack.setPriority(Thread.MAX_PRIORITY);
			    mThread_ack.start();*/
			    
				Intent mIntent = new Intent();
				 AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
				  //开始时间
			    long firstime=SystemClock.elapsedRealtime();
				
				 mIntent.setAction("speeh_restart");
				 PendingIntent sender=PendingIntent
				        .getBroadcast(xSpeeh.this, 0, mIntent, 0);
				     
				   am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP
					            , firstime+ 5*1000, sender);
			}
			
		};
		
		BroadcastReceiver br3 = new BroadcastReceiver()
		{	
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if(intent.getAction().equals("speeh_init"))
				 {
					Log.e("loghere", "recever--->serviceinit");
			    	
				   /* mtThread1.setPriority(Thread.MAX_PRIORITY);
					   mtThread1.start();*/
					
					initampservice();
					
				/*	Intent intent2 = new Intent();
			    	 intent2.setClass(Speeh.this, SocketService.class);
						intent2.putExtra("command", "speeh_getipaddr");
						startService(intent2);*/
				 } 
				
				
				 if(intent.getAction().equals("speeh_getlocation"))
				 {
					 
					initampservice();
					Log.e("loghere", "recever--->getlocation");
				 } 
				   
				 if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
				 {
					 
					 BatteryN = intent.getIntExtra("level", 0); 
					// Log.e("loghere", "getbatterinfo"+BatteryN);
				 }   
			}
			
		};
		
	 public  boolean isNetworkAvailable()
		    {
		        Context context = this.getApplicationContext();
		        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		        
		        Boolean isconnect = false;
		        if (connectivityManager == null)
		        {
		        	isconnect = false;
		        }
		        else
		        {
		        	  State wifiState = null;  
				        State mobileState = null;  
				        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
				        wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();  
				        mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();  
				        if (wifiState != null && mobileState != null  
				                && State.CONNECTED != wifiState  
				                && State.CONNECTED == mobileState) {  
				        	    networkok = true;
				        	    isconnect = true;
				        	 
				            // 手机网络连接成功  
				        } else if (wifiState != null && mobileState != null  
				                && State.CONNECTED != wifiState  
				                && State.CONNECTED != mobileState) {  
				        	networkok = false;
			        	     isconnect = false;
			        	  
				            // 手机没有任何的网络  
				        } else if (wifiState != null && State.CONNECTED == wifiState) {  
				            // 无线网络连接成功  
				        	     networkok = true;
				        	     isconnect = true;
				        	 
				        }  
		            // 获取NetworkInfo对象
		       /*     NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
		            
		            if (networkInfo != null && networkInfo.length > 0)
		            {
		                for (int i = 0; i < networkInfo.length; i++)
		                {
		                  //  Log.e("loghere", i + "===状态===" + networkInfo[i].getState());
		                  //  Log.e("loghere",i + "===类型===" + networkInfo[i].getTypeName());
		                    // 判断当前网络状态是否为连接状态
		                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
		                    {
		                    	isconnect = true;
		                    	break;
		                    }
		                }
		            }*/
		        	
		        }
		        Log.e("loghere", "networkok="+String.valueOf(networkok)+" connect2server="+String.valueOf(connect2server)+" firstgps="+String.valueOf(firstgps))  ; 
		        return isconnect;
		    }
	
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		 Log.e("loghere", "GoogleSpeeh-->creatservice");
	
		
		SharedPreferences sarp1 = getSharedPreferences("locinfo", Context.MODE_MULTI_PROCESS);
		Editor sarpedt = sarp1.edit();
		sarpedt.putString("lat", String.valueOf(lat));
		sarpedt.putString("lng", String.valueOf(lng));
		
		
		
		smManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		lightsensor = smManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		
		smManager.registerListener(new SensorEventListener()
		{
			
			@Override
			public void onSensorChanged(SensorEvent event)
			{
				// TODO Auto-generated method stub
			    lux = event.values[0]; 
				
			 //	Log.e("lux", "lux:" +lux);
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy)
			{
				// TODO Auto-generated method stub
				  
			}
		}, lightsensor, SensorManager.SENSOR_DELAY_UI);
		
		IntentFilter intentfilter1 = new IntentFilter("speehwakeup");  //注册接收处理广播的service
		intentfilter1.setPriority(900);
		registerReceiver(br1, intentfilter1);
		
		IntentFilter intentfilter2 = new IntentFilter("speeh_sendack");
		intentfilter2.setPriority(900);
		registerReceiver(br2, intentfilter2);
		
		
		IntentFilter intentfilter3 = new IntentFilter("speeh_init");
		intentfilter3.setPriority(900);
		registerReceiver(br3, intentfilter3);
		
		 intentfilter3 = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		 registerReceiver(br3, intentfilter3);
		 
		 intentfilter3 = new IntentFilter("speeh_getlocation");
		 registerReceiver(br3, intentfilter3);
		 
		 isstart = true;
		
	}
	
	  private void initampservice()
	   {
			
				if (locationClient==null )
			   	{
					locationClient = new AMapLocationClient(xSpeeh.this.getApplicationContext());
				}
						
				if(locationOption==null)
				{	
					locationOption = new AMapLocationClientOption();
				}
				
				// 设置定位参数
				locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
				
				// 设置是否需要显示地址信息
						locationOption.setNeedAddress(true);
						/**
						 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
						 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
						 */
						locationOption.setGpsFirst(false);
						//网络请求时间单位毫秒
						locationOption.setHttpTimeOut(30000);
						//更新位置信息时间间隔,单位毫秒
						long strInterval = 120*1000;
						
						// 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
				     //	locationOption.setInterval(strInterval);
						
						locationClient.setLocationOption(locationOption);
						
						// 启动定位
						locationClient.setLocationListener(xSpeeh.this);
						// 开始定位
						locationClient.startLocation();
				
				if (serverip.toString().equals("0.0.0.0"))
					  {
						 Intent intent2 = new Intent();
				    	 intent2.setClass(xSpeeh.this, SocketService.class);
							
						 intent2.putExtra("command", "getipaddr");
						 startService(intent2);
					  }
	   }
	  
	  private Boolean Doublecompare(Double a,Double b )
		{
			BigDecimal mDecimal= new BigDecimal(b);
			BigDecimal local = new BigDecimal(a);
			int dif = 0;
			dif = mDecimal.compareTo(local);
			
			switch (dif)
			{
			case 1:
				return false;
			case -1:
				return true;
			case 0:
				return false;	
			default:
				break;
			}
			
			return false;
		}
	  
	  private void writedb()
	  {
		  SharedPreferences sarp1 = getSharedPreferences("locinfo", Context.MODE_MULTI_PROCESS);
			Editor sarpedt = sarp1.edit();
			sarpedt.putString("lat", String.valueOf(lat));
			sarpedt.putString("lng", String.valueOf(lng));
			sarpedt.commit();
			LC_CHANGED = false;
	  }
	  
	  private void readdb()
	  {
		  SharedPreferences sarp1 = getSharedPreferences("locinfo", Context.MODE_MULTI_PROCESS);
		    lat = Double.valueOf(sarp1.getString("lat", "0.0"));
		    lng = Double.valueOf(sarp1.getString("lng", "0.0"));
			
	  }
	  
	  private void lc_change()
	  {
		  
			if ( LC_CHANGED )
			  {
			  	 String chazhi = String.valueOf(lng) + " " +String.valueOf(lat) +" " + 
			  			String.valueOf(Math.abs(lng - loc_new.getLongitude())) +" "+ 
			  			String.valueOf(Math.abs(lat - loc_new.getLatitude()) + "\n");
			  			
		   		 loc = loc_new;
		   		 lc_street = Utils.getLocationStr(loc_new)+chazhi;
		   		 
		   		 Log.e("loghere", chazhi);
		   		 lat = loc_new.getLatitude();
				 lng = loc_new.getLongitude();
				 lc_accuracy = String.valueOf(loc_new.getAccuracy());
				 lc_provider = loc_new.getProvider();
				 lc_province= loc_new.getProvince();
				 lc_city = loc_new.getCity();
				 lc_district = loc_new.getDistrict();
				 lc_locationtype = String.valueOf(loc_new.getLocationType());
				 lc_address = loc_new.getAddress();
				 writedb();
			
				  Intent intent2 = new Intent();
			    	 intent2.setClass(xSpeeh.this, SocketService.class);
						
						intent2.putExtra("command", "uploaddata");
						startService(intent2);
			  }			
			    
	  }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		Intent mIntent = new Intent();
		 AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		  //开始时间
	    long firstime=SystemClock.elapsedRealtime();
		
		 mIntent.setAction("speeh_init");
		 PendingIntent sender=PendingIntent
		        .getBroadcast(xSpeeh.this, 0, mIntent, 0);
		     
		   am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP
			            , firstime+ 5*1000, sender);
		
		
	/*	mIntent.setAction("speehwakeup");
	    sender=PendingIntent
	        .getBroadcast(Speeh.this, 0, mIntent, 0);
	  
	   
	    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
	            , firstime+ 10*1000, 600*1000, sender);*/
	    
		
		  mIntent.setAction("speeh_sendack");
		  sender=PendingIntent
		        .getBroadcast(xSpeeh.this, 0, mIntent, 0);
		     
		   am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
			            , firstime+ 20*1000, 900*1000, sender);
		   
		   mIntent.setAction("speeh_getlocation");
			  sender=PendingIntent
			        .getBroadcast(xSpeeh.this, 0, mIntent, 0);
			     
			   am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
				            , firstime+ 30*1000, 300*1000, sender);
		
		 
	//	initampservice();
		
	//	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		Log.e("loghere", "speeh--->onStartCommand,threadID:"+Thread.currentThread().getId());
		
		
	 //	 initampservice(); 
		
		return START_STICKY;
	}

	

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		isstart = false;
	 	unregisterReceiver(br1);
	 	unregisterReceiver(br2);
	 	unregisterReceiver(br3);
	 	
	 	Intent mIntent = new Intent();
		 AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		  //开始时间
	    long firstime=SystemClock.elapsedRealtime();
		
		 mIntent.setAction("speeh_restart");
		 PendingIntent sender=PendingIntent
		        .getBroadcast(xSpeeh.this, 0, mIntent, 0);
		     
		   am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP
			            , firstime+ 30*1000, sender);
	}

	@Override
	public void onLocationChanged(AMapLocation loc_new)
	{
		// TODO Auto-generated method stub
		Log.e("loghere","GoogleSpeeh-->onLocationChanged");
		if (null != loc_new) 
		{
			 readdb();
			Log.e("loghere","LocationErrorCode-->"+String.valueOf(loc_new.getErrorCode()));
		  if(loc_new.getErrorCode()==0) //定位成功
			{ 
			  
			   LC_CHANGED =  Doublecompare(Math.abs(lat - loc_new.getLatitude()),0.0025000) || Doublecompare(Math.abs(lng - loc_new.getLongitude()),0.0025000);
			    Log.e("loghere", LC_CHANGED ? "LC_CHANGED=true" : "LC_CHANGED=false" );
			  
			   Log.e("loghere", (Math.abs(lat - loc_new.getLatitude())) +" "+ (Math.abs(lng - loc_new.getLongitude())));	 
			   this.loc_new = loc_new;
			   lc_change();
			}
		}
		locationClient.stopLocation();
	}
	

}
