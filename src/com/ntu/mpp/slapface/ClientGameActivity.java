package com.ntu.mpp.slapface;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ClientGameActivity extends Activity {
	
	private ClientAgent mClientAgent;
	
	boolean flagReadThread = true;
	private Thread readThread = new Thread() {// 從server讀字串的thread
		String tmp = "";

		@Override
		public void run() {
			while (flagReadThread) {
				try {
					tmp = mClientAgent.read();
					// TODO Switch case for input string.
					if (tmp == null) {
						tmp = "";
						Log.e("null", "null");
					} else {
						
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			mClientAgent.end();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mClientAgent = ClientActivity.mClientAgent;
		readThread.start();
	}
}
