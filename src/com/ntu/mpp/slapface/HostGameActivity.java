package com.ntu.mpp.slapface;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class HostGameActivity extends Activity {

	private ServerAgent mServerAgent;
	
	boolean flagReadThread = true;
	private Thread readThread = new Thread() {// 從server讀字串的thread
		String tmp = "";

		@Override
		public void run() {
			while (flagReadThread) {
				try {
					tmp = mServerAgent.readFromId(0);
					// TODO Switch case of input string.
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
			mServerAgent.end(0);
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mServerAgent = HostActivity.mServerAgent;
		readThread.start();
	}
}
