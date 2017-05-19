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
��Ӧ�� 	˵�� 	����
1 	GPS��λ��� 	ͨ���豸GPS��λģ�鷵�صĶ�λ���
2 	ǰ�ζ�λ��� 	���綨λ�������1�롢�����ζ�λ֮���豸λ�ñ仯�ǳ�Сʱ���أ��豸λ��ͨ����������֪��
4 	���涨λ��� 	����һ��ʱ��ǰ�豸��ͬ����λ�û������������綨λ���
5 	Wifi��λ��� 	�������綨λ����λ������Ի�վ��λ�����
6 	��վ��λ��� 	�������綨λ
8 	���߶�λ��� 	-
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
	
	public boolean autobatch = true;  //�Ƿ�Ϊ�Զ�ģʽ
	
	private static SensorManager smManager;
	
	private static Sensor lightsensor ;
	
	private static final int DOACK = 100;
	public static AMapLocation loc,loc_new;
	public static Double lat=0.0,lng=0.0; //lng ����  lat γ��
	public static String lc_street="",lc_province="",lc_city="",
			lc_district="",lc_address="",lc_locationtype="",
			lc_provider="",lc_accuracy="";
	public static String serverip = "0.0.0.0";
	float test = 123.456766982194093763021f;
	public static boolean firstgps = true,LC_CHANGED = false;
	public static int weakup = 0;
	public boolean ackthreadstart = false;
	
	public static boolean networkok = false,connect2server = false;
	

	
	public static boolean[] watertime; //�Ƿ��Ѿ�ִ����ˮ�ƻ�������
	
	public static boolean suning = false;  //�Ƿ�����������
	
	public static String[] water,suntime;
	
	public static int watercount = 0;

	public static int[] getBatter()
	{
		int[] batinfo = {BatteryN,BatteryV,BatteryT};  //�¶���һ������231 ����23.1��
		return batinfo;
	}
	
	public static float getLux()
	{
		float mlux = lux;  //�¶���һ������231 ����23.1��
		return mlux;
	}
	
	public class Tea {
		//����
		public byte[] encrypt(byte[] content, int offset, int[] key, int times){//timesΪ��������
		int[] tempInt = byteToInt(content, offset);
		int y = tempInt[0], z = tempInt[1], sum = 0, i;
		int delta=0x9e3779b9; //�����㷨��׼����ֵ
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
		//����
		public byte[] decrypt(byte[] encryptContent, int offset, int[] key, int times){
		int[] tempInt = byteToInt(encryptContent, offset);
		int y = tempInt[0], z = tempInt[1], sum = 0xC6EF3720, i;
		int delta=0x9e3779b9; //�����㷨��׼����ֵ
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
		//byte[]������ת��int[]������
		private int[] byteToInt(byte[] content, int offset){

		int[] result = new int[content.length >> 2]; //����2��n�η� == ����nλ �� content.length / 4 == content.length ���� 2
		for(int i = 0, j = offset; j < content.length; i++, j += 4){
		result[i] = transform(content[j + 3]) | transform(content[j + 2]) <<  8 |
		transform(content[j + 1]) << 16 | (int)content[j] << 24;
		}
		return result;

		}
		//int[]������ת��byte[]������
		private byte[] intToByte(int[] content, int offset){
		byte[] result = new byte[content.length << 2]; //����2��n�η� == ����nλ �� content.length * 4 == content.length ���� 2
		for(int i = 0, j = offset; j < result.length; i++, j += 4){
		result[j + 3] = (byte)(content[i] & 0xff);
		result[j + 2] = (byte)((content[i] >> 8) & 0xff);
		result[j + 1] = (byte)((content[i] >> 16) & 0xff);
		result[j] = (byte)((content[i] >> 24) & 0xff);
		}
		return result;
		}
		//��ĳ�ֽڱ����ͳɸ������轫��ת���޷�������
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
	 * �Ƚϴ�������ʱ����ʵ��ʱ��Ĵ�С�������ǰʱ��Ȳ����󣬷���true��С�򷵻�false
	 * @param time
	 * @return boolean
	 */
	public boolean bijiaotime(String time) //�Ƚϵ�ǰϵͳʱ���봫������ʱ���С
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
		
			while (isstart) //�����ǰactivity�˳��Ͳ�����ִ��
			{	
				try
				{
					
					Message msg1 = new Message();
					msg1.what = DOGETINFO;
					handle1.sendMessage(msg1);
					
					Thread.sleep(15000);  //ÿ�����������һ�Σ��൱��һ��timerʹ��
					
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
				  //��ʼʱ��
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
		        // ��ȡ�ֻ��������ӹ�����󣨰�����wi-fi,net�����ӵĹ���
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
				        	 
				            // �ֻ��������ӳɹ�  
				        } else if (wifiState != null && mobileState != null  
				                && State.CONNECTED != wifiState  
				                && State.CONNECTED != mobileState) {  
				        	networkok = false;
			        	     isconnect = false;
			        	  
				            // �ֻ�û���κε�����  
				        } else if (wifiState != null && State.CONNECTED == wifiState) {  
				            // �����������ӳɹ�  
				        	     networkok = true;
				        	     isconnect = true;
				        	 
				        }  
		            // ��ȡNetworkInfo����
		       /*     NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
		            
		            if (networkInfo != null && networkInfo.length > 0)
		            {
		                for (int i = 0; i < networkInfo.length; i++)
		                {
		                  //  Log.e("loghere", i + "===״̬===" + networkInfo[i].getState());
		                  //  Log.e("loghere",i + "===����===" + networkInfo[i].getTypeName());
		                    // �жϵ�ǰ����״̬�Ƿ�Ϊ����״̬
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
		
		IntentFilter intentfilter1 = new IntentFilter("speehwakeup");  //ע����մ���㲥��service
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
				
				// ���ö�λ����
				locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
				
				// �����Ƿ���Ҫ��ʾ��ַ��Ϣ
						locationOption.setNeedAddress(true);
						/**
						 * �����Ƿ����ȷ���GPS��λ��������30����GPSû�з��ض�λ�����������綨λ
						 * ע�⣺ֻ���ڸ߾���ģʽ�µĵ��ζ�λ��Ч��������ʽ��Ч
						 */
						locationOption.setGpsFirst(false);
						//��������ʱ�䵥λ����
						locationOption.setHttpTimeOut(30000);
						//����λ����Ϣʱ����,��λ����
						long strInterval = 120*1000;
						
						// ���÷��Ͷ�λ�����ʱ����,��СֵΪ1000�����С��1000������1000��
				     //	locationOption.setInterval(strInterval);
						
						locationClient.setLocationOption(locationOption);
						
						// ������λ
						locationClient.setLocationListener(xSpeeh.this);
						// ��ʼ��λ
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
		  //��ʼʱ��
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
		  //��ʼʱ��
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
		  if(loc_new.getErrorCode()==0) //��λ�ɹ�
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
