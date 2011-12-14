package com.ntu.mpp.slapface;

import java.io.BufferedReader;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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
				break;
			case C.ADD_INPUT:
				mServerAgent.addInput((BufferedReader) msg.obj);
				break;
			case C.ADD_OUTPUT:
				mServerAgent.addOutput((PrintWriter) msg.obj);
				break;
			}
		}
	};

	boolean flagReadThread = true;
	private TextView tvMsg;
	private Button btnStart;
	Thread readThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host);
		
		tvMsg = (TextView) findViewById(R.id.tvHostMsg);
		btnStart = (Button) findViewById(R.id.btnHostStart);
		btnStart.setOnClickListener(mBtnStartOnClick);
		mServerAgent = new ServerAgent();
		openServerConnection();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (mServerAgent != null) {
			mServerAgent.clear();
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
