package com.ntu.mpp.slapface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import android.os.Handler;
import android.util.Log;

class LinkHandler implements Runnable {
	
	Socket clientSocket;
	Handler mainHandler;

	public LinkHandler(Socket incoming) {
		// TODO Auto-generated constructor stub
		clientSocket  = incoming;
	}

	public LinkHandler(Handler mHandler, Socket incomingSocket) {
		// TODO Auto-generated constructor stub
		clientSocket  = incomingSocket;
		mainHandler = mHandler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.i(C.TAG, "+LinkHandler.run()");
		Log.i(C.TAG, clientSocket.toString());
		
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true); // Autoflush
			
			String userId = input.readLine();
			Log.e(C.TAG, "From client: " + userId);
			if (userId.equals("")) {
				userId = "noname";
			}
			
			String ipAddress = clientSocket.getInetAddress().toString();
			ipAddress = ipAddress.substring(0, ipAddress.indexOf("/"));
			
			mainHandler.sendMessage(mainHandler.obtainMessage(C.ADD_CLIENT, userId));
			mainHandler.sendMessage(mainHandler.obtainMessage(C.ADD_INPUT, input));
			mainHandler.sendMessage(mainHandler.obtainMessage(C.ADD_OUTPUT, output));
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.i(C.TAG, "-LinkHandler.run()");
	}
}
