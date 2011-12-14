package com.ntu.mpp.slapface;

import java.io.IOException;

import android.os.Handler;

public class ClientReadThread implements Runnable{

	private String tmp;
	private ClientAgent mClientAgent = ClientActivity.mClientAgent;
	boolean flagReadThread = true;
	private Handler mHandler;

	public ClientReadThread(Handler handler) {
		mHandler = handler;
	}
	
	@Override
	public void run() {
		while (flagReadThread) {
			try {
				tmp = mClientAgent.read();
				// TODO Switch case for input string.
//				if (tmp == null) {
//					tmp = "";
//					Log.e("null", "null");
//				} else {
//					
//				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		mClientAgent.end();
	}

}
