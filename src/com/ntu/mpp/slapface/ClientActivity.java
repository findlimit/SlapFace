package com.ntu.mpp.slapface;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ClientActivity extends Activity {
	
	public static ClientAgent mClientAgent;
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case C.SHOW_MSG:
				dia_join.show();
				break;
			case C.DISMISS_MSG:
				dia_join.dismiss();
				break;
			case C.CHANGE_MSG:
//				dia_join.setTitle(R.string.connectok);
//				dia_join.setMessage(ClientActivity.this.getResources()
//						.getString(R.string.waitforhost));
				dia_join.dismiss();
				readThread = new Thread() {// 從server讀字串的thread
					String tmp = "";

					@Override
					public void run() {
						while (flagReadThread) {
							try {
								tmp = mClientAgent.read();
								if (tmp == null) {
									tmp = "";
									Log.e("null", "null");
								} else {
									runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											tvClientMsg.setText(tmp);
											Log.d("Peter", tmp);
										}
									});
								}
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						mClientAgent.end();
					}
				};
				readThread.start();
				break;
			case C.SOCKET_FAILED:
				dia_join.setTitle(R.string.connectfail);
				dia_join.setMessage(ClientActivity.this.getResources()
						.getString(R.string.pleasereconnect));
				break;
			}
		}
	};
	
	boolean flagReadThread = true;
	private ProgressDialog dia_join;
	private TextView tvClientMsg;
	private Button btnClientSend;
	Thread readThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		Log.d("Peter", "Client");
		tvClientMsg = (TextView) findViewById(R.id.tvClientMsg);
		btnClientSend = (Button) findViewById(R.id.btnClientSend);
		btnClientSend.setOnClickListener(mBtnClientSendOnClick);
		dia_join = new ProgressDialog(ClientActivity.this);
		
		Log.d("Peter", "findview over");
		Message m = mHandler.obtainMessage(C.SHOW_MSG);
		if (m == null)
			Log.d("Peter", "m null");
		mHandler.sendMessage(m);
		setListeners();
		openClientConnection();
	}
	
	private void setListeners() {

		dia_join.setButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dia_join.dismiss();

				ClientActivity.this.finish();

			}
		});
		dia_join.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				ClientActivity.this.finish();
			}

		});
	}
	
	private OnClickListener mBtnClientSendOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mClientAgent.write("yoyo");
			tvClientMsg.setText("yoyo");
		}
	};
	
	protected void openClientConnection() {
		Log.e(C.TAG, "+openClientConnection()");

		WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		DhcpInfo mDhcpInfo = mWifiManager.getDhcpInfo();

		int ipadd = mDhcpInfo.gateway;
		C.SERVER_IP = ((ipadd & 0xFF) + "." + (ipadd >> 8 & 0xFF) + "."
				+ (ipadd >> 16 & 0xFF) + "." + (ipadd >> 24 & 0xFF));
		Log.v(C.TAG, C.SERVER_IP);

		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
				mDhcpInfo = mWifiManager.getDhcpInfo();

				ipadd = mDhcpInfo.gateway;
				C.SERVER_IP = ((ipadd & 0xFF) + "." + (ipadd >> 8 & 0xFF)
						+ "." + (ipadd >> 16 & 0xFF) + "." + (ipadd >> 24 & 0xFF));
				Log.v(C.TAG, C.SERVER_IP);
				mClientAgent = new ClientAgent(
						C.SERVER_IP, C.SERVER_PORT, mHandler);
				break;
			} catch (UnknownHostException e) {
				Toast.makeText(this, "Please connect in Setting",
						Toast.LENGTH_LONG).show();

				finish();
			} catch (IOException e) {
				continue;
			}
		}

		mClientAgent.write("");
		Log.e(C.TAG, "-openClientConnection()");
	}
}
