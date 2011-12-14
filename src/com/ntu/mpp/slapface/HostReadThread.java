package com.ntu.mpp.slapface;

import java.io.IOException;

import android.os.Handler;
import android.util.Log;

public class HostReadThread implements Runnable{

	private String tmp;
	private ServerAgent mServerAgent = HostActivity.mServerAgent;
	boolean flagReadThread = true;
	private Handler mHandler;

	public HostReadThread(Handler handler) {
		mHandler = handler;
	}

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

}
