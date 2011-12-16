/*
 * Wifi Connecter
 * 
 * Copyright (c) 20101 Kevin Yuan (farproc@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 **/ 

package com.ntu.mpp.slapface;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TwoLineListItem;

public class MainActivity extends Activity {
	
	private WifiManager mWifiManager;
	private List<ScanResult> mScanResults;
	private ListView lvWifi;
	private Button btnHost;
	private Button btnClient;
	private Button faceButton;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	
    	mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
    	
    	btnHost = (Button) findViewById(R.id.btnHost);
    	btnHost.setOnClickListener(mBtnHostOnClick);
    	faceButton=(Button)findViewById(R.id.faceTest);
    	faceButton.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, GameActivity.class);
				startActivity(intent);
			}
		});
    	
    	btnClient = (Button) findViewById(R.id.btnClient);
    	btnClient.setOnClickListener(mBtnClientOnClick);
    	
    	lvWifi = (ListView) findViewById(R.id.lvWifi);
    	lvWifi.setAdapter(mListAdapter);
    	lvWifi.setOnItemClickListener(mItemOnClick);
    	lvWifi.setVisibility(ListView.VISIBLE);
    	
    	Log.d("Peter", "haha");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
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
			launchWifiConnecter(MainActivity.this, result);
			Log.d("Peter", "back");
		}
	};
	
	private OnClickListener mBtnHostOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, HostActivity.class);
			startActivity(intent);
			finish();
		}
	};
	
	private OnClickListener mBtnClientOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, ClientActivity.class);
			startActivity(intent);
			finish();
		}
	};
	
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