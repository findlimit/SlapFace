package com.ntu.mpp.slapface;


public class C {
	public static final String TAG = "Peter";
	
	public static String SERVER_IP = "192.168.5.1";
	public static final int SERVER_PORT = 12345;
	public static final String SERVER_SSID = "slapface";
	public static final int MAX_CLIENT_NUM = 1;
	
	// Messages for ClientActivity
	protected static final int SHOW_MSG = 1;
	protected static final int DISMISS_MSG = 2;
	protected static final int CHANGE_MSG = 3;
	protected static final int SOCKET_FAILED = 4;
	final static int START_GAME = 5;
	
	// Messages for HostActivity
	protected static final int ADD_CLIENT = 6;
	protected static final int ADD_OUTPUT = 7;
	protected static final int ADD_INPUT = 8;

}
