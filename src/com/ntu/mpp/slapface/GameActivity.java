package com.ntu.mpp.slapface;

import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GameActivity extends Activity implements SensorEventListener {

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch(msg.what){
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
	boolean isAttacking;//True: had attacked, now wait for response
	private int myHp;
	private int enemyHp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		host = getIntent().getBooleanExtra("host", true);
		init(host);
		
		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(sensors.size()>0){
        	sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        }
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
		isAttacking=false;
		myHp=100;
		enemyHp=100;
	}
	
	private void reflash(){//used to reflash view
		isAttacking=false;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {//Use to Attack
		// TODO Auto-generated method stub
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			double accelerate=getAccelerate(event.values[0], event.values[1], event.values[2]);
			if(accelerate>20 && adState==true && isAttacking==false){
				isAttacking=true;
				attack(accelerate);
			}
		}
	}
	
	public double getAccelerate(float x, float y, float z){//Calculate Accelerate
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	private void attack(double pow){
		int attackValue=(int)((pow-15)/5);
		sendMsg(1, attackValue);
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

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub	
	}
}
