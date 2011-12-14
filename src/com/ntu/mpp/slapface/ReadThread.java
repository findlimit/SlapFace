package com.ntu.mpp.slapface;

import java.io.IOException;

import android.os.Handler;
import android.util.Log;

public class ReadThread implements Runnable{

	private String tmp;
	private SocketAgent mAgent;
	boolean flagReadThread = true;
	private Handler mHandler;

	public ReadThread(boolean host, Handler handler) {
		mHandler = handler;
		if (host)
			mAgent = HostActivity.mServerAgent;
		else
			mAgent = ClientActivity.mClientAgent;
	}

	@Override
	public void run() {
		while (flagReadThread) {
			try {
				tmp = mAgent.read();
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
		mAgent.end();
	}

}
