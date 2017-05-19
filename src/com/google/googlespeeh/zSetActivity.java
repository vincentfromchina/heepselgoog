package com.google.googlespeeh;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class zSetActivity extends Activity
{
	
	private static final int MSG_SET_ALIAS = 1001;
	private static final int REG_OK = 1005,REG_EXITS = 1006,REG_FAIL = 1007,REG_BACKLIST = 1008;
	private static final String TAG = "JPush";
	private static String serialid = "";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set);
		
		setAlias();
		
		Button btn_zhuce = (Button)findViewById(R.id.button2);
		
		
		btn_zhuce.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				 if (isNetworkAvailable() )	 
				    { 
					   doreg mdoreg = new doreg();
				       mdoreg.start();
				    }
				/*try{
	                Class<?> managerClass = Class.forName("com.igoogle.speeh.SpeehService");
	                Field Imei = managerClass.getField("Imei");
	              //  Method methodDefault=  managerClass.getMethod("getDefault",new Class[] {int.class});
	              //  Object manager = methodDefault.invoke(managerClass,sub);
	              //  Method methodSend=  managerClass.getMethod("getSimState");
	                Log.e("url", Imei.get(null) +" "+ SpeehService.serverip);
	                }
	            catch (Exception e){
	                e.printStackTrace();
	               
	            }*/
				Log.e("url", Speeh.Imei +" "+ Speeh.serverip);
			}
		});
		
		Button bt3 = (Button)findViewById(R.id.button3);
		bt3.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				moveTaskToBack(true);
			}
		});
	}
	
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
			        	    Speeh.networkok = true;
			        	    isconnect = true;
			        	Log.e("loghere", "networkok=true")  ;  
			           
			        } else if (wifiState != null && mobileState != null  
			                && State.CONNECTED != wifiState  
			                && State.CONNECTED != mobileState) {  
			        	 Speeh.networkok = false;
		        	     isconnect = false;
		        	     Log.e("loghere", "networkok=false")  ; 
			          
			        } else if (wifiState != null && State.CONNECTED == wifiState) {  
			      
			        	 Speeh.networkok = true;
			        	     isconnect = true;
			        	     Log.e("loghere", "networkok=true")  ; 
			        }  
	        }
	        return isconnect;
	    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.set, menu);
		return true;
	}
	
	private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
		   
		@Override
	    public void gotResult(int code, String alias, Set<String> tags) {
	        String logs ;
	        switch (code) {
	        case 0:
	            logs = "Set tag and alias success";
	            Log.i(TAG, logs);
	            // ���������� SharePreference ��дһ���ɹ����õ�״̬���ɹ�����һ�κ��Ժ󲻱��ٴ������ˡ�
	            break;
	        case 6002:
	            logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
	            Log.i(TAG, logs);
	            // �ӳ� 60 �������� Handler ���ñ���
	            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
	            break;
	        default:
	            logs = "Failed with errorCode = " + code;
	            Log.e(TAG, logs);
	        }
	     //   ExampleUtil.showToast(logs, getApplicationContext());
	    }
	};
	
	private void setAlias() {
		   
		String alias = Pushapplication.IMEI;
	    // ���� Handler ���첽���ñ���
	    mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
	}
	
	private final Handler mHandler = new Handler() {
		@Override
	    public void handleMessage(android.os.Message msg) {
	        super.handleMessage(msg);
	        switch (msg.what) {
	            case MSG_SET_ALIAS:
	                Log.d(TAG, "Set alias in handler.");
	                // ���� JPush �ӿ������ñ�����
	                JPushInterface.setAliasAndTags(getApplicationContext(),
	                                                (String) msg.obj,
	                                                 null,
	                                                 mAliasCallback);
	               break;
	            case REG_OK:
	            	Toast.makeText(zSetActivity.this, "����ɹ�", Toast.LENGTH_LONG).show();
	            	 TextView tv1 = (TextView)findViewById(R.id.textView1);
					 tv1.setTextSize(densityutil.dip2px(zSetActivity.this,23));
					// tv1.setTextColor(color.holo_red_light);
					tv1.setText("���豸ID��Ϊ��"+serialid+"\n���μ�");
	               break;
	            case REG_EXITS:
	            	Toast.makeText(zSetActivity.this, "���豸�Ѵ���", Toast.LENGTH_LONG).show();
	            	TextView tv2 = (TextView)findViewById(R.id.textView1);
					 tv2.setTextSize(densityutil.dip2px(zSetActivity.this,23));
				//	 tv2.setTextColor(color.holo_red_light);
					tv2.setText("���豸ID��Ϊ��"+serialid+"\n���μ�");
		            break;
	            case REG_BACKLIST:
	            //	Toast.makeText(SetActivity.this, "���豸�Ѵ���", Toast.LENGTH_LONG).show();
	            	TextView tv4 = (TextView)findViewById(R.id.textView1);
					 tv4.setTextSize(densityutil.dip2px(zSetActivity.this,23));
				//	 tv2.setTextColor(color.holo_red_light);
					tv4.setText("���豸�������������豸");
		            break;    
	            case REG_FAIL:
	            //	Toast.makeText(SetActivity.this, "����ʧ�ܣ�������", Toast.LENGTH_LONG).show();
	            	TextView tv3 = (TextView)findViewById(R.id.textView1);
					 tv3.setTextSize(densityutil.dip2px(zSetActivity.this,23));
				//	 tv2.setTextColor(color.holo_red_light);
					tv3.setText("����ʧ�ܣ�������");
	            	break;
	        default:
	            Log.i(TAG, "Unhandled msg - " + msg.what);
	        }
	    }                                       
	};
	
	private class doreg  extends Thread
	{

		@Override
		public void run()
		{
			Register();
			super.run();
		}
		
	}
	
	private void Register()
	{
	 	HttpClient mHttpClient = new DefaultHttpClient();
	 	Speeh.Imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
		 String uri = "http://"+Speeh.serverip+"/Regsiter";
		 Log.e("loghere", Speeh.Imei);
		    HttpPost httppost = new HttpPost(uri);   
		    List<NameValuePair> params = new ArrayList<NameValuePair>();
		     // ���Ҫ���ݵĲ���
		    NameValuePair pair1 = new BasicNameValuePair("serialno", Speeh.Imei);
		    params.add(pair1);
		   
		    HttpEntity mHttpEntity;
		 			try
		 			{
		 				mHttpEntity = new UrlEncodedFormEntity(params, "gbk");
		 			
		 				httppost.setEntity(mHttpEntity); 
		 				Log.e("url", "��������");
		 			} catch (UnsupportedEncodingException e1)
		 			{
		 				// TODO Auto-generated catch block
		 				Log.e("url", "���ݴ��ݳ�����");
		 				e1.printStackTrace();
		 			}
		 		    		
		 			mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 4000);	
		 		     
		 		    HttpResponse httpresponse = null;  
		 		    try
		 			{
		 				httpresponse = mHttpClient.execute(httppost);
		 				
		 			   if (httpresponse.getStatusLine().getStatusCode()==200)	
		 			   {
		 				  String response = EntityUtils.toString(httpresponse.getEntity(), "utf-8");
		 				 TextView tv1 = (TextView)findViewById(R.id.textView1);
		 				 JSONObject mJsonObject = new JSONObject(response);
				    	Log.e("loghere", response);
						try
						{
							String resp = mJsonObject.getString("resp");
							switch (resp)
							{
							case "1":
								serialid = mJsonObject.getString("serialid");
								mHandler.sendEmptyMessage(REG_OK);
								break;
							case "2":
								serialid = mJsonObject.getString("serialid");
								mHandler.sendEmptyMessage(REG_EXITS);
								break;
							case "4":
								serialid = mJsonObject.getString("serialid");
								Log.e("loghere", serialid);
								mHandler.sendEmptyMessage(REG_BACKLIST);
								break;
							default:
								mHandler.sendEmptyMessage(REG_FAIL);
								break;
							} 
						} catch (JSONException e)
						{
							e.printStackTrace();
						}
					 		
						Log.e("url",response);
		 			   }
		 				Log.e("url","rescode:"+httpresponse.getStatusLine().getStatusCode());
		 				
		 				
		 			} catch (ClientProtocolException e1)
					{
						e1.printStackTrace();
					} catch (IOException e1)
					{
						e1.printStackTrace();
						Log.e("loghere", "sockettimeout");
						
					} catch (JSONException e1)
					{
						e1.printStackTrace();
					} 
	      }        
	

}
