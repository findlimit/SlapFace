package com.ntu.mpp.slapface;

import java.io.IOException;


public interface SocketAgent {
	
	public void write(String str);
	
	public String read() throws IOException;
	
	public void end();
	
}
