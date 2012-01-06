package com.ntu.mpp.slapface;

import java.io.IOException;

import com.ntu.mpp.slapface.GameActivity.messageCode;

import android.os.Handler;
import android.util.Log;

public class ReadThread implements Runnable {

	private String tmp;
	private String[] tmpPart;
	private SocketAgent mAgent;
//	private Boolean flagReadThread = true;
	private Handler mHandler;
	private Boolean mBoolHost;

	public ReadThread(Boolean host, SocketAgent agent, Handler handler) {
		mHandler = handler;
		mBoolHost = host;
		mAgent = agent;
	}

	@Override
	public void run() {
		while (C.READ_THREAD) {
			try {

				tmp = mAgent.read();

				if (tmp != null) {// To avoid close(kill) app error

					if (tmp.equals(null) || tmp.equals("")) {
						tmp = "";
						// Log.e("null", "null");
					} else {
						tmpPart = tmp.split("&");

						if (tmpPart[0].equals("ATK")) {
							mHandler.sendMessage(mHandler.obtainMessage(messageCode.ATK, tmpPart[1]));
						} else if (tmpPart[0].equals("HP")) {
							mHandler.sendMessage(mHandler.obtainMessage(messageCode.ENEMY_HP_ACK, tmpPart[1]));
						} else if (tmpPart[0].equals("OVER")) {
							mHandler.sendEmptyMessage(messageCode.OVER);
						}
						Log.e("Peter", String.valueOf(mBoolHost) + "/" + tmp);
					}

				} else {
					C.READ_THREAD = false;
				}

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		mAgent.end();
	}

}
