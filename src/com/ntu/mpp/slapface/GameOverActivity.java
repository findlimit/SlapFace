package com.ntu.mpp.slapface;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class GameOverActivity extends Activity {

	private RelativeLayout r;
	private Button restart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// For Full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.overview);
		findViews();

		r = (RelativeLayout) findViewById(R.id.relativeLayout1);

		if (this.getIntent().getBooleanExtra("WIN", false)) {
			// Winner
			Drawable d = this.getResources().getDrawable(R.drawable.sf_win);
			r.setBackgroundDrawable(d);
		} else {
			// Loser
			Drawable d = this.getResources().getDrawable(R.drawable.sf_lose);
			r.setBackgroundDrawable(d);
		}

	}

	private void findViews() {
		restart = (Button) findViewById(R.id.btnOverAgain);
		restart.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
//				 Intent intent = new Intent();
//				 intent.putExtra("host", GameOverActivity.this.getIntent().getBooleanExtra("host", false));
//				 intent.setClass(GameOverActivity.this, GameActivity.class);
//				 startActivity(intent);
//				 finish();
			}
		});

	}
}
