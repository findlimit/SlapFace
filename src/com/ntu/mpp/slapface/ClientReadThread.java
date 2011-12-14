package com.ntu.mpp.slapface;

import java.io.IOException;

import android.util.Log;

public class ClientReadThread implements Runnable{

	private String tmp;
	private ClientAgent mClientAgent = ClientActivity.mClientAgent;
	boolean flagReadThread = true;

	@Override
	public void run() {
		while (flagReadThread) {
			try {
				tmp = mClientAgent .read();
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

}
