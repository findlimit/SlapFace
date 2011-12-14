package com.ntu.mpp.slapface;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class GameActivity extends Activity {

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch(msg.what){
			case 0:
				// TODO: Call attack method 
				attack(Double.parseDouble(msg.obj.toString()));
				break;
			case 1:
				// TODO: Call defend method
				break;
			case 2:
				// TODO: Run when attacker waiting for response from defender
			default:
				getRespond(Integer.parseInt(msg.obj.toString()));
				break;
			}
		}
	};
	
	private Thread readThread;
	private boolean host;
	
	boolean isHost;//True: Host, False: client.
	boolean adState;//True: Attacker, False: Defender.
	private int myHp;
	private int enemyHp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		host = getIntent().getBooleanExtra("host", true);
		init(host);
	}
	
	private void init(boolean host) {
		if (host){
			readThread = new Thread(new HostReadThread(mHandler));
			isHost=true;
			adState=true;
		}else{
			readThread = new Thread(new ClientReadThread(mHandler));
			isHost=false;
			adState=false;
		}
		readThread.start();
		myHp=100;
		enemyHp=100;
	}
	
	private void reflash(){//used to reflash view
	}
	
	
	private void attack(double pow){
		//mHandler.sendMessage();
		sendMsg(1, pow);
	}

	private void getRespond(int eHP){
		enemyHp=eHP;
		if(enemyHp<=0){
			isGameOver(true);
		}else{
			reflash();
			adState=true;
			waitThreeSecond();
		}
	}
	
	private void waitThreeSecond(){//used to wait three second
	}
	private void isGameOver(boolean isYouWin){//isYouWin==true: attacker win
	}
	//////////////////////////////////////////////////////////
	private void sendMsg(int type, Object pow) {//TODO: Peter's work
		// TODO Auto-generated method stub
	}
}
