package com.google.googlespeeh;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import cn.jpush.android.api.InstrumentedActivity;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class MainActivity extends InstrumentedActivity
{
	private static final int MSG_SET_ALIAS = 1001;
	private static final String TAG = "JPush";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button bt1 = (Button)findViewById(R.id.button1);
		bt1.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent intent2 = new Intent();
		    	 intent2.setClass(MainActivity.this, Speeh.class);
					
					startService(intent2);
			}
		});
		
		Button bt2 = (Button)findViewById(R.id.button2);
		bt2.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				setAlias();
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
	
	private void setAlias() {
	   
	    String alias = Pushapplication.IMEI;
	    if (TextUtils.isEmpty(alias)) {
	        Toast.makeText(MainActivity.this,"别名不能为空", Toast.LENGTH_SHORT).show();
	        return;
	    }
	    if (!ExampleUtil.isValidTagAndAlias(alias)) {
	        Toast.makeText(MainActivity.this,"别名非法", Toast.LENGTH_SHORT).show();
	        return;
	    }

	    // 调用 Handler 来异步设置别名
	    mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
	}
	
	private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
	   
		@Override
	    public void gotResult(int code, String alias, Set<String> tags) {
	        String logs ;
	        switch (code) {
	        case 0:
	            logs = "Set tag and alias success";
	            if( Pushapplication.isdebug )  Log.i(TAG, logs);
	            // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
	            break;
	        case 6002:
	            logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
	            if( Pushapplication.isdebug )  Log.i(TAG, logs);
	            // 延迟 60 秒来调用 Handler 设置别名
	            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
	            break;
	        default:
	            logs = "Failed with errorCode = " + code;
	            if( Pushapplication.isdebug ) Log.e(TAG, logs);
	        }
	    //    ExampleUtil.showToast(logs, getApplicationContext());
	    }
	};

private final Handler mHandler = new Handler() {
	@Override
    public void handleMessage(android.os.Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_SET_ALIAS:
            	if( Pushapplication.isdebug )  Log.d(TAG, "Set alias in handler.");
                // 调用 JPush 接口来设置别名。
                JPushInterface.setAliasAndTags(getApplicationContext(),
                                                (String) msg.obj,
                                                 null,
                                                 mAliasCallback);
            break;
        default:
        	if( Pushapplication.isdebug ) Log.i(TAG, "Unhandled msg - " + msg.what);
        }
    }                                       
};

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
