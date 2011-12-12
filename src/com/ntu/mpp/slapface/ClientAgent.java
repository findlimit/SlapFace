package com.ntu.mpp.slapface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ClientAgent {

	Socket socket;
	BufferedReader input;
	PrintWriter output;

	public ClientAgent(String serverIp, int serverPort, Handler mHandler)
			throws UnknownHostException, IOException {
		// Log.e(Global.TAG, "+socket()");
		socket = new Socket(C.SERVER_IP, C.SERVER_PORT);
		input = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);
		Log.e(C.TAG, "-socket()");

		Message m = mHandler.obtainMessage(C.CHANGE_MSG);
		mHandler.sendMessage(m);
	}

	public void write(String string) {
		output.println(string.trim());
	}

	public String read() throws IOException {
		return input.readLine();
	}

	public void end() {
		try {

			input.close();
			output.close();
			socket.close();

		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
