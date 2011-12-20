package com.ntu.mpp.slapface;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

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
				readThread = new Thread() {// Read "start" string from server.
					String tmp = "";

					@Override
					public void run() {
						try {
							while (true) {
								tmp = mClientAgent.read();
								if (tmp.equals("start")) {
									Intent intent = new Intent(ClientActivity.this, GameActivity.class);
									intent.putExtra("host", false);
									ClientActivity.this.startActivity(intent);
									ClientActivity.this.finish();
									break;
								}
							}
						} catch (IOException e) {
							mHandler.sendMessage(mHandler.obtainMessage(C.SOCKET_FAILED));
							e.printStackTrace();
						}
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
	Thread readThread;
	private WifiManager mWifiManager;
	private List<ScanResult> mScanResults;
	private ListView lvWifi;
	boolean flagReconnect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		
		Log.d("Peter", "in Client");
		lvWifi = (ListView) findViewById(R.id.lvWifi);
    	lvWifi.setAdapter(mListAdapter);
    	lvWifi.setOnItemClickListener(mItemOnClick);
    	Log.d("Peter", "lv over");
		tvClientMsg = (TextView) findViewById(R.id.tvClientMsg);
		dia_join = new ProgressDialog(ClientActivity.this);
		setListeners();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		registerReceiver(mReceiver, filter);
		mWifiManager.startScan();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				mScanResults = mWifiManager.getScanResults();
				mListAdapter.notifyDataSetChanged();
				
//				mWifiManager.startScan();
			} else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
				SupplicantState newState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
				tvClientMsg.setText(newState.toString());
				if (newState.equals(SupplicantState.DISCONNECTED)) {
					flagReconnect = true;
					Log.d("Peter", "disconnected");
					Message m = mHandler.obtainMessage(C.SHOW_MSG);
					mHandler.sendMessage(m);
				}
				if (flagReconnect && newState.equals(SupplicantState.COMPLETED)) {
					flagReconnect = false;
					Log.d("Peter", "openClient");
					openClientConnection();
				}
			}
		}
	};
	
	private BaseAdapter mListAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null || !(convertView instanceof TwoLineListItem)) {
				convertView = View.inflate(getApplicationContext(), 
						android.R.layout.simple_list_item_2, null);
			}
			
			final ScanResult result = mScanResults.get(position);
			((TwoLineListItem)convertView).getText1().setText(result.SSID);
			((TwoLineListItem)convertView).getText2().setText(
					String.format("%s  %d", result.BSSID, result.level)
					);
			return convertView;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}
		
		@Override
		public int getCount() {
			return mScanResults == null ? 0 : mScanResults.size();
		}
	};
	
	private OnItemClickListener mItemOnClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final ScanResult result = mScanResults.get(position);
			launchWifiConnecter(ClientActivity.this, result);
		}
	};
	
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
	
	protected void openClientConnection() {
		Log.e(C.TAG, "+openClientConnection()");

		WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		DhcpInfo mDhcpInfo = mWifiManager.getDhcpInfo();

		int ipadd = mDhcpInfo.gateway;
		C.SERVER_IP = ((ipadd & 0xFF) + "." + (ipadd >> 8 & 0xFF) + "."
				+ (ipadd >> 16 & 0xFF) + "." + (ipadd >> 24 & 0xFF));
		Log.v(C.TAG, C.SERVER_IP);

		while (true) {
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}

			try {
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
	
	/**
	 * Try to launch Wifi Connecter with {@link #hostspot}. Prompt user to download if Wifi Connecter is not installed.
	 * @param activity
	 * @param hotspot
	 */
	private static void launchWifiConnecter(final Activity activity, final ScanResult hotspot) {
		final Intent intent = new Intent(activity, com.farproc.wifi.connecter.MainActivity.class);
		intent.putExtra("com.farproc.wifi.connecter.extra.HOTSPOT", hotspot);
		activity.startActivity(intent);
	}
}
