package com.ntu.mpp.slapface;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends Activity {

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}
	};
	
	private Thread readThread;
	private boolean host;
	private SocketAgent mAgent;

	private TextView textView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		init();
		Button btn = (Button) findViewById(R.id.btnTestSend);
		textView = (TextView) findViewById(R.id.tvTestMsg);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mAgent.write("lala");
				textView.setText("lala");
			}
		});
	}
	
	private void init() {
		host = getIntent().getBooleanExtra("host", true);
		if (host)
			mAgent = HostActivity.mServerAgent;
		else
			mAgent = ClientActivity.mClientAgent;
		readThread = new Thread(new ReadThread(host, mAgent, mHandler));
		readThread.start();
	}
}
