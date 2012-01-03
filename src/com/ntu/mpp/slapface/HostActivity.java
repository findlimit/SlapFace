package com.ntu.mpp.slapface;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.jar.Attributes.Name;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.encode.QRCodeEncoder;

public class HostActivity extends Activity {
	
	public static ServerAgent mServerAgent;
	
	private Thread socketListener_t;
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case C.ADD_CLIENT:

				mServerAgent.clientCount++;
//				if (mMedia != null) {
//					mMedia.stop();
//					mMedia.reset();
//				}
//				mMedia = MediaPlayer.create(HostActivity.this, R.raw.bell);

//				try {
//					mMedia.prepare();
//				} catch (IllegalStateException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

//				mMedia.start();
//				Toast.makeText(
//						HostActivity.this,
//						HostActivity.this.getResources().getString(
//								R.string.players)
//								+ (mServerAgent.clientCount + 1),
//						Toast.LENGTH_SHORT).show();
				// HostActivity.this.setTitle(HostActivity.this.getTitle()
				// + " " + (mServerAgent.clientCount + 1));
				Log.d("Peter", "ready");
				tvMsg.setText("ready");
				btnStart.setTextColor(Color.BLACK);
				btnStart.setClickable(true);
				break;
			case C.ADD_INPUT:
				mServerAgent.addInput((BufferedReader) msg.obj);
				break;
			case C.ADD_OUTPUT:
				mServerAgent.addOutput((PrintWriter) msg.obj);
				mServerAgent.write("SlapFace");
				break;
			}
		}
	};

	boolean flagReadThread = true;
	private TextView tvMsg;
	private Button btnStart;
	private ImageView ivQRCode;
	Thread readThread;

//	private Intent intentQR;
//	private QRCodeEncoder qrCodeEncoder;
	private WifiManager mWifiManager;
	private ProgressDialog dia_join;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host);
		
//		ivQRCode = (ImageView) findViewById(R.id.ivQRCode);
		tvMsg = (TextView) findViewById(R.id.tvHostMsg);
		btnStart = (Button) findViewById(R.id.btnHostStart);
		btnStart.setOnClickListener(mBtnStartOnClick);
		btnStart.setClickable(false);
		btnStart.setTextColor(Color.GRAY);
		
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		mServerAgent = new ServerAgent();
		
//		macAddress = mWifiManager.getConnectionInfo().getMacAddress();
		
		
//		DhcpInfo mDhcpInfo = mWifiManager.getDhcpInfo();
//		Log.d(C.TAG, mDhcpInfo.toString());
//		Log.d(C.TAG, mWifiManager.getConnectionInfo().toString());
		
//		intentQR = new Intent("com.google.zxing.client.android.ENCODE");
//	    intentQR.putExtra("ENCODE_TYPE", "TEXT_TYPE");
//	    intentQR.putExtra("ENCODE_DATA", macAddress);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// This assumes the view is full screen, which is a good assumption
//	    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
//	    Display display = manager.getDefaultDisplay();
//	    int width = display.getWidth();
//	    int height = display.getHeight();
//	    int smallerDimension = width < height ? width : height;
//	    smallerDimension = smallerDimension * 7 / 8;
//	    	
//		try {
//			qrCodeEncoder = new QRCodeEncoder(this, intentQR, smallerDimension);
//			setTitle(getString(R.string.app_name) + " - " + qrCodeEncoder.getTitle());
//		    Bitmap bitmap;
//			bitmap = qrCodeEncoder.encodeAsBitmap();
//			ivQRCode.setImageBitmap(bitmap);
////		    TextView contents = (TextView) findViewById(R.id.contents_text_view);
//		    tvMsg.setText(qrCodeEncoder.getDisplayContents());
//		} catch (WriterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		if (!isWifiApEnabled()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Please open Wifi Hotspot manually.")
				   .setTitle("Warning!")
				   .setCancelable(false)
				   .setPositiveButton("Open Now", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							HostActivity.this.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
						}
				   })
				   .setNeutralButton("I did", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						HostActivity.this.onResume();
					}
				})
				   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							HostActivity.this.finish();
						}
				   });
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		} else {
			WifiConfiguration wifiConfig;
			if ((wifiConfig = getWifiApConfig()) == null) {
				tvMsg.setText(R.string.null_wifiapconfig);
			} else if (wifiConfig.SSID != null){
				tvMsg.setText("Your Hotspot Name is: " + wifiConfig.SSID);
			} else {
				tvMsg.setText(R.string.null_wifiapconfig);
			}
			
			C.SOCKET_LISTENER = true;
			if (mServerAgent != null) {
				mServerAgent.clear();
			}
			mServerAgent = new ServerAgent();
			openServerConnection();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		if (mServerAgent != null) {
//			mServerAgent.clear();
//		}
		if (socketListener_t != null) {
			C.SOCKET_LISTENER = false;
			socketListener_t.interrupt();
		}
	}
	
	private void setListeners() {

		dia_join.setButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dia_join.dismiss();

				HostActivity.this.finish();

			}
		});
		dia_join.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				HostActivity.this.finish();
			}

		});
	}
	
	private void tasksAfterWifiApEnabled() {
		
	}
	
	private Boolean isWifiApEnabled() {
		// close ordinary wifi connection
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
			return false;
		}
		
		Method[] wmMethods = mWifiManager.getClass().getDeclaredMethods();
//			    Log.d(C.TAG, "enableMobileAP methods " + wmMethods.length);
	    for(Method method: wmMethods){
	        Log.d(C.TAG, "enableMobileAP method.getName() " + method.getName());
	        if (method.getName().equals("isWifiApEnabled")) {
	        	try {
					return (Boolean) method.invoke(mWifiManager);
				} catch (Exception e) {
					// TODO: handle exception
				}
	        }
	    }
	    return false;
	}
	
	private WifiConfiguration getWifiApConfig() {	
		// close ordinary wifi connection
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
		
		Method[] wmMethods = mWifiManager.getClass().getDeclaredMethods();
//	    Log.d(C.TAG, "enableMobileAP methods " + wmMethods.length);
	    for(Method method: wmMethods){
	        Log.d(C.TAG, "enableMobileAP method.getName() " + method.getName());
	        if (method.getName().equals("getWifiApConfiguration")) {
	        	try {
					WifiConfiguration wifiConfiguration = (WifiConfiguration) method.invoke(mWifiManager);
					return wifiConfiguration;
				} catch (Exception e) {
					// TODO: handle exception
				}
	        }
	    }
	    return null;
	}
	
	private void turnOnWifiAP() {
		
	    Method[] wmMethods = mWifiManager.getClass().getDeclaredMethods();
//	    Log.d(C.TAG, "enableMobileAP methods " + wmMethods.length);
	    for(Method method: wmMethods){
	        Log.d(C.TAG, "enableMobileAP method.getName() " + method.getName());
	        if(method.getName().equals("setWifiApEnabled")) {
//	            WifiConfiguration netConfig = new WifiConfiguration();
//	            netConfig.SSID = "MyWifiAP";
//	            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//	            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//	            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//	            netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	            try {
	                Log.d(C.TAG, "enableMobileAP try: ");
//	                method.invoke(mWifiManager, null, true);
	            } catch (Exception e) {
	                Log.e(C.TAG, "enableMobileAP failed: ", e);
	            }
	        }
	    }
	}
	
	private void openServerConnection() {
		Log.i(C.TAG, "+createServer()");
		socketListener_t = new Thread(new SocketListener(mHandler,
				C.SERVER_PORT));
		socketListener_t.start();
		Log.i(C.TAG, "-createServer()");
	}
	
	private OnClickListener mBtnStartOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mServerAgent.writeAll("start");
			tvMsg.setText("start");
			Intent intent = new Intent(HostActivity.this, GameActivity.class);
			intent.putExtra("host", true);
			startActivity(intent);
			finish();
		}
	};
}
