package com.ntu.mpp.slapface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ServerAgent implements SocketAgent{
	// ServerSocket socket;
	ArrayList<BufferedReader> inputList;
	ArrayList<PrintWriter> outputList;
	int clientCount; // dont include server
	boolean flagEnded = false;

	public ServerAgent() {
		inputList = new ArrayList<BufferedReader>();
		outputList = new ArrayList<PrintWriter>();
		clientCount = 0; // no server itself
	}

	public void writeAll(String str) {
		for (PrintWriter output : outputList) {
			output.println(str.trim());
		}
	}

	public void writeToId(String str, int id) {
		if (id < outputList.size())
			outputList.get(id).println(str.trim());
	}

	public String readFromId(int id) throws IOException {
		if (id < inputList.size()) {
			return inputList.get(id).readLine();
		}
		return "";
	}

	public void addOutput(PrintWriter obj) {
		outputList.add(obj);
	}

	public void addInput(BufferedReader obj) {
		inputList.add(obj);
	}

	public void clear() {
		inputList.clear();
		outputList.clear();
		clientCount = 0;
	}

	public void end(int cId) {
		if (cId < inputList.size()) {
			try {
				inputList.get(cId).close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (cId < outputList.size()) {
			outputList.get(cId).close();
		}
	}

	@Override
	public void write(String str) {
		writeToId(str, 0);
	}

	@Override
	public String read() throws IOException {
		return readFromId(0);
	}

	@Override
	public void end() {
		end(0);
	}
}
