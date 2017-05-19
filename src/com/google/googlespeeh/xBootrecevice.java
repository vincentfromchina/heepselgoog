package com.google.googlespeeh;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

public class xBootrecevice extends BroadcastReceiver
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
}
