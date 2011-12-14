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
		}
	};
	
	private Thread readThread;
	private boolean host;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	private void init() {
		host = getIntent().getBooleanExtra("host", true);
		readThread = new Thread(new ReadThread(host, mHandler));
		readThread.start();
	}
}
