package com.google.googlespeeh;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

public class zBootrecevice extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		 if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			   Intent newIntent = new Intent(context,Speeh.class);
			   context.startService(newIntent);
		 }
		 
		 if(intent.getAction().equals("speeh_restart")){
			if (!Speeh.isstart)
			 {Intent newIntent = new Intent(context,Speeh.class);
			   context.startService(newIntent);
			 }
		 }
		 
		 if(intent.getAction().equals("android.intent.action.ACTION_POWER_CONNECTED")){
			 if (!Speeh.isstart)
			 {Intent newIntent = new Intent(context,Speeh.class);
			   context.startService(newIntent);
			 }
		 }
		 
		 if(intent.getAction().equals("android.intent.action.PACKAGE_CHANGED")){
			 if (!Speeh.isstart)
			 {Intent newIntent = new Intent(context,Speeh.class);
			   context.startService(newIntent);
			 }
		 }
		 
		 if(intent.getAction().equals("android.intent.action.PACKAGE_RESTARTED")){
			 if (!Speeh.isstart)
			 {Intent newIntent = new Intent(context,Speeh.class);
			   context.startService(newIntent);
			 }
		 }
		 
		 if(intent.getAction().equals("android.intent.action.ACTION_POWER_DISCONNECTED")){
			 if (!Speeh.isstart)
			 {Intent newIntent = new Intent(context,Speeh.class);
			   context.startService(newIntent);
			 }
		 }
		 
		 if(intent.getAction().equals("com.tencent.mm.plugin.openapi.Intent.ACTION_REFRESH_WXAPP")){
			 if (!Speeh.isstart)
			 {Intent newIntent = new Intent(context,Speeh.class);
			   context.startService(newIntent);
			 }
		 }
		 
		 if(intent.getAction().equals("miui.intent.action.UPDATE_CURRENT_WIFI_CONFIGURATION")){
			 if (!Speeh.isstart)
			 {Intent newIntent = new Intent(context,Speeh.class);
			   context.startService(newIntent);
			 }
		 }
		 
	/*	 if(intent.getAction().equals("speehservice_check")){
			 Log.e("loghere", "Broadcast_check");
			   Intent newIntent = new Intent(context,DaemonService.class);
			   DaemonService.command = "check";
			   context.startService(newIntent);
		 }*/
		 
		 
		 if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
			 Log.e("loghere", "CONNECTIVITY_CHANGE");
			   State wifiState = null;  
		        State mobileState = null;  
		        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		        wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();  
		        mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();  
		        if (wifiState != null && mobileState != null  
		                && State.CONNECTED != wifiState  
		                && State.CONNECTED == mobileState) {  
		        	Speeh.networkok = true;
		            //  
		        } else if (wifiState != null && mobileState != null  
		                && State.CONNECTED != wifiState  
		                && State.CONNECTED != mobileState) {  
		            //  
		        } else if (wifiState != null && State.CONNECTED == wifiState) {  
		            // 
		        	Speeh.networkok = true;
		        }  
			 
		 }
	}
	
	
}
