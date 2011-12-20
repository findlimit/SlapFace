package com.ntu.mpp.slapface;

import java.io.IOException;

import android.os.Handler;
import android.util.Log;

public class ReadThread implements Runnable {

	private String tmp;
	private SocketAgent mAgent;
	private Boolean flagReadThread = true;
	private Handler mHandler;
	private Boolean mBoolHost;

	public ReadThread(Boolean host, SocketAgent agent, Handler handler) {
		mHandler = handler;
		mBoolHost = host;
		mAgent = agent;
	}

	@Override
	public void run() {
		while (flagReadThread) {
			try {

				tmp = mAgent.read();

				if (tmp != null) {// To avoid close(kill) app error

					if (tmp.equals(null) || tmp.equals("")) {
						tmp = "";
						// Log.e("null", "null");
					} else {
						mHandler.sendMessage(mHandler.obtainMessage(0, tmp));
						Log.d("Peter", String.valueOf(mBoolHost) + "/" + tmp);
					}

				} else {
					flagReadThread = false;
				}

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		mAgent.end();
	}

}
