package com.ntu.mpp.slapface;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.farproc.wifi.connecter.NewNetworkContent;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

public class ClientActivity extends Activity {

	public static ClientAgent mClientAgent;
	
	public static void releaseClientAgent() {
		mClientAgent = null;
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case C.SHOW_MSG:
				// dia_join.show();
				break;
			case C.DISMISS_MSG:
				dia_join.dismiss();
				break;
			case C.CHANGE_MSG:
				// dia_join.setTitle(R.string.connectok);
				// dia_join.setMessage(ClientActivity.this.getResources()
				// .getString(R.string.waitforhost));
				readThread = new Thread() {// Read "start" string from server.
					String tmp = "";

					@Override
					public void run() {
						try {
							while (true) {
								tmp = mClientAgent.read();
								if (tmp.equals("start")) {
									dia_join.dismiss();
									Intent intent = new Intent(ClientActivity.this, GameActivity.class);
									intent.putExtra("host", false);
									ClientActivity.this.startActivity(intent);
									finish();
									break;
								} else if (tmp.equals("SlapFace")) {
									flagConnected = true;
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
				dia_join.setMessage(ClientActivity.this.getResources().getString(R.string.pleasereconnect));
				break;
			case C.START_GAME:
				String str = (String) msg.obj;
				Toast.makeText(ClientActivity.this, str, Toast.LENGTH_LONG).show();
				dia_join.dismiss();
				break;
			}
		}
	};

	boolean flagReadThread = true;
	boolean flagReconnect = false;
	boolean flagConnected = false;
	Thread readThread;
	private WifiManager mWifiManager;
	private List<ScanResult> mScanResults;
	private ListView lvWifi;
	private ImageButton btnClientConnect;
	private ProgressDialog dia_join;
	private TextView tvClientMsg;
	private TextView tvClientSSID;

	// private Button btnClientOther;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.client);

		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

		lvWifi = (ListView) findViewById(R.id.lvWifi);
		lvWifi.setAdapter(mListAdapter);
		lvWifi.setOnItemClickListener(mItemOnClick);
		tvClientMsg = (TextView) findViewById(R.id.tvClientMsg);
		tvClientSSID = (TextView) findViewById(R.id.tvClientSSID);
		btnClientConnect = (ImageButton) findViewById(R.id.btnClientConnect);
		// btnClientConnect.setClickable(false);
		// btnClientConnect.setTextColor(Color.GRAY);
		// btnClientOther = (Button) findViewById(R.id.btnClientOther);
		// if (!mWifiManager.getConnectionInfo().getSupplicantState().equals(SupplicantState.COMPLETED))
		// btnClientStart.setVisibility(Button.INVISIBLE);

		dia_join = new ProgressDialog(ClientActivity.this);
		dia_join.setMessage("Connected! Wait for host to start");
		// dia_join.setCancelable(false);
		setListeners();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
		tvClientSSID.setText(mWifiManager.getConnectionInfo().getSSID());

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
		// if (mClientAgent != null) {
		// mClientAgent.end();
		// }
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				mScanResults = mWifiManager.getScanResults();
				Collections.sort(mScanResults, Collections.reverseOrder());
				mListAdapter.notifyDataSetChanged();
				// mWifiManager.startScan();
			} else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
				SupplicantState newState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
				if (newState.equals(SupplicantState.COMPLETED)) {
					String strSSID = mWifiManager.getConnectionInfo().getSSID();
//					Log.d("Peter", "strSSID: "+strSSID);
					tvClientSSID.setText(strSSID);
				}
			}
			// else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
			// SupplicantState newState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
			// tvClientMsg.setText(newState.toString());
			// if (newState.equals(SupplicantState.DISCONNECTED)) {
			// flagReconnect = true;
			// Log.d("Peter", "disconnected");
			// // Message m = mHandler.obtainMessage(C.SHOW_MSG);
			// // mHandler.sendMessage(m);
			// }
			// if (flagReconnect && newState.equals(SupplicantState.COMPLETED)) {
			// flagReconnect = false;
			// tvClientBSSID.setText(mWifiManager.getConnectionInfo().getSSID());
			// // dia_join.dismiss();
			// // btnClientConnect.setVisibility(Button.VISIBLE);
			// }
			// }
		}
	};
	
	private class ViewHolder {
		public TextView text1;
		public TextView text2;
	}
	
	public ViewHolder holder;

	private BaseAdapter mListAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(), R.layout.client_wifi_list, null);
				holder = new ViewHolder();
				holder.text1 = (TextView) convertView.findViewById(R.id.tvWifiText1);
				holder.text2 = (TextView) convertView.findViewById(R.id.tvWifiText2);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final ScanResult result = mScanResults.get(position);
			holder.text1.setText(result.SSID);
			holder.text2.setText(String.format("%s  %d", result.BSSID, result.level));
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
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final ScanResult result = mScanResults.get(position);
			launchWifiConnecter(ClientActivity.this, result);
		}
	};

	private void setListeners() {

		// dia_join.setButton("取消", new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		//
		// dia_join.dismiss();
		// // if (mClientAgent != null) {
		// // mClientAgent.end();
		// // }
		// // ClientActivity.this.finish();
		//
		// }
		// });
		// dia_join.setOnCancelListener(new OnCancelListener() {
		//
		// @Override
		// public void onCancel(DialogInterface dialog) {
		// // TODO Auto-generated method stub
		// ClientActivity.this.finish();
		// }
		//
		// });
		btnClientConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// dia_join.show();
				openClientConnection();
			}
		});
		// btnClientOther.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// ClientActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		// }
		// });
	}

	protected void openClientConnection() {
		Log.e(C.TAG, "+openClientConnection()");

		WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		DhcpInfo mDhcpInfo = mWifiManager.getDhcpInfo();

		int ipadd = mDhcpInfo.gateway;
		C.SERVER_IP = ((ipadd & 0xFF) + "." + (ipadd >> 8 & 0xFF) + "." + (ipadd >> 16 & 0xFF) + "." + (ipadd >> 24 & 0xFF));
		Log.v(C.TAG, C.SERVER_IP);
		int i = 0;
		for (i = 0; i < 10; i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				mDhcpInfo = mWifiManager.getDhcpInfo();

				ipadd = mDhcpInfo.gateway;
				C.SERVER_IP = ((ipadd & 0xFF) + "." + (ipadd >> 8 & 0xFF) + "." + (ipadd >> 16 & 0xFF) + "." + (ipadd >> 24 & 0xFF));
				// Log.v(C.TAG, C.SERVER_IP);
				mClientAgent = new ClientAgent(C.SERVER_IP, C.SERVER_PORT, mHandler);
				break;
			} catch (UnknownHostException e) {
				// Toast.makeText(this, "Please connect in Setting",
				// Toast.LENGTH_LONG).show();

				finish();
			} catch (IOException e) {
				continue;
			}
		}

		if (mClientAgent == null) {
			mHandler.sendMessage(mHandler.obtainMessage(C.START_GAME, "You are connecting to wrong host!"));
			return;
		} else {
			mClientAgent.write("SlapFace");
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (!flagConnected) {
						mHandler.sendMessage(mHandler.obtainMessage(C.START_GAME, "You are connecting to wrong host!"));
						return;
					}
				}
			});
			thread.start();
			dia_join.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if ((keyCode == KeyEvent.KEYCODE_BACK||keyCode == KeyEvent.KEYCODE_SEARCH)) {
						return true;
					}
					return false;
				}
			});
			dia_join.show();
		}
		Log.e(C.TAG, "-openClientConnection()");
	}

	/**
	 * Try to launch Wifi Connecter with {@link #hostspot}. Prompt user to download if Wifi Connecter is not installed.
	 * 
	 * @param activity
	 * @param hotspot
	 */
	private static void launchWifiConnecter(final Activity activity, final ScanResult hotspot) {
		final Intent intent = new Intent(activity, com.farproc.wifi.connecter.MainActivity.class);
		intent.putExtra("com.farproc.wifi.connecter.extra.HOTSPOT", hotspot);
		activity.startActivity(intent);
	}
}
