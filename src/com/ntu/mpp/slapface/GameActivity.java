package com.ntu.mpp.slapface;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class GameActivity extends Activity {

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch(msg.what){
			case 0:
				// TODO: Call attack method 
				break;
			case 1:
				// TODO: Call defend method
				break;
			default:
				break;
			}
		}
	};
	
	private Thread readThread;
	private boolean host;
	
	boolean isHost;
	private int myHp;
	private int enemyHp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		host = getIntent().getBooleanExtra("host", true);
		init(host);
	}
	
	private void init(boolean host) {
		if (host){
			readThread = new Thread(new HostReadThread(mHandler));
			isHost=true;
		}else{
			readThread = new Thread(new ClientReadThread(mHandler));
			isHost=false;
		}
		readThread.start();
		myHp=100;
		enemyHp=100;
	}
	
	
}
