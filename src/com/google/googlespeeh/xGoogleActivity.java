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

public class xGoogleActivity extends Activity
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
					Toast.makeText(xGoogleActivity.this, "To type someting.", Toast.LENGTH_SHORT).show();
					break;
                case "����":
                	 intent1 = new Intent(xGoogleActivity.this, SetActivity.class);
    				startActivity(intent1);
					break;
                case "����":
                    intent1 = new Intent(xGoogleActivity.this, MainActivity.class);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.google, menu);
		return true;
	}

}
