package com.ntu.mpp.slapface;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.util.Log;

class SocketListener implements Runnable {
	
	private int serverPort;
	final Handler mainHandler;

	public SocketListener(Handler mHandler, int port) {
		// TODO Auto-generated constructor stub
		mainHandler = mHandler;
		serverPort = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.i(C.TAG, "+SocketListener()");
		try {
			// establish server socket
			int connIndex = 0;
			ServerSocket serverSocket = new ServerSocket(serverPort);
			Log.i(C.TAG, "port:" + serverSocket.getLocalPort());

			while (connIndex < C.MAX_CLIENT_NUM) {
				Log.e(C.TAG, "WAIT FOR CONNECTION");
				Socket incoming = serverSocket.accept();
				Log.e(C.TAG, "Connected a client!connIndex:"
								+ connIndex
								+ " RemoteSocketAddress:"
								+ String.valueOf(incoming.getRemoteSocketAddress()));
				Thread connHandle = new Thread(new LinkHandler(mainHandler, incoming));
				connHandle.start();
				connIndex++;
				Log.e("cln",connIndex+"");
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(C.TAG, "-SocketListener()");
		
	}
}



