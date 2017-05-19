package com.google.googlespeeh;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class zGoogleActivity extends Activity
{

	TextToSpeech mTTS;
	Button btn1 ;
	EditText edt1 ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google);
		btn1 = (Button)findViewById(R.id.btn_fanyi);
		edt1 = (EditText)findViewById(R.id.gv_editText1);
		edt1.setText("");
		btn1.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
			 Intent intent1;
			 
			 if (mTTS != null)	
			 {	switch (edt1.getText().toString())
				{
				case "":
					Toast.makeText(zGoogleActivity.this, "To type someting.", Toast.LENGTH_SHORT).show();
					break;
                case "¼¤»î":
                	 intent1 = new Intent(zGoogleActivity.this, SetActivity.class);
    				startActivity(intent1);
					break;
                case "·þÎñ":
                    intent1 = new Intent(zGoogleActivity.this, MainActivity.class);
    				startActivity(intent1);
					break;	
				default:
					mTTS.speak(edt1.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
					break;
				}
			 }	
			}
		});
		
		  mTTS = new TextToSpeech(this, new OnInitListener()
			{
				
				@Override
				public void onInit(int status)
				{
					Log.e("loghere", "TTS init");
				}
			});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.google, menu);
		return true;
	}

}
