package com.google.googlespeeh;

import android.app.Application;
import android.app.Notification;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;


public class zPushapplication extends Application
{
	 private static final String TAG = "JPush";
	 public static String IMEI ;
	// static Boolean speehstart = false; 
	 BasicPushNotificationBuilder pnfb = new BasicPushNotificationBuilder(zPushapplication.this);
	 BasicPushNotificationBuilder pnfb2 = new BasicPushNotificationBuilder(zPushapplication.this);
	 CustomPushNotificationBuilder pnfb3 = new CustomPushNotificationBuilder(zPushapplication.this,R.layout.customer_notitfication_layout,R.id.icon, R.id.title, R.id.text);
	 
	 
	 @Override
	public void onCreate()
	{
		 Log.d(TAG, "[googlespeeh--Pushapplication] onCreate");
         super.onCreate();
         
         IMEI = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
         if ((IMEI==null) || (IMEI.equals("000000000000000")))
         {
        	 Log.d(TAG, "设备异常");
         }
         
         pnfb.statusBarDrawable =  android.R.drawable.alert_dark_frame;
         pnfb.notificationFlags = Notification.FLAG_ONLY_ALERT_ONCE;
         pnfb.notificationDefaults =  Notification.DEFAULT_LIGHTS;
         pnfb.developerArg0 = "developerArg0";
         
         pnfb2.statusBarDrawable = R.drawable.h_548349;
         pnfb2.notificationFlags = Notification.FLAG_AUTO_CANCEL;
         pnfb2.notificationDefaults =  Notification.DEFAULT_SOUND;
         
         pnfb3.statusBarDrawable = R.drawable.j_2629;
         pnfb3.layoutIconDrawable = R.drawable.h_548349;
         pnfb3.notificationDefaults = Notification.DEFAULT_SOUND;
         pnfb3.notificationFlags = Notification.FLAG_AUTO_CANCEL;
     //    pnfb3.developerArg0 = "developerArg0";
         
         
         JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
         JPushInterface.init(this);     		// 初始化 JPush
         JPushInterface.setPushNotificationBuilder(2, pnfb2);
         JPushInterface.setPushNotificationBuilder(3, pnfb3);
         JPushInterface.setDefaultPushNotificationBuilder( pnfb);
         
         if (!Speeh.isstart)
		 {Intent newIntent = new Intent(this,Speeh.class);
		    startService(newIntent);
		 }
         
	}
	 

}
