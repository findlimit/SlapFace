package com.ntu.mpp.slapface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback, SensorEventListener {

	private final String tag = getClass().getName();// For Log usage

	private Thread readThread;
	private Thread gameThread;
	private Thread faceThread;
	private Thread countThread;
	private Boolean host;
	private SocketAgent mAgent;
	private ProgressBar myHpBar;
	private ProgressBar enemyHpBar;
	private Double accelerate;
	private Boolean isAttackState;
	private Boolean isDefendState;
	private Boolean isMissOkTimeState = false;
	private int missChance;
	private Boolean isDetect;
	private Boolean countMissTime = false;
	private int damage;
	private Boolean winner;
	private LinearLayout sl;
	private TextView enemyHP;
	private TextView myHP;
	private Vibrator mVibrator;
	private LinearLayout infoLinearLayout;

	private TextView testTextView;
	private TextView testTextView2;
	private TextView testTextView3;
	private Button atkButton;
	private TextView motionHint;

	// For face detection==========↓
	private Camera myCamera;
	private SurfaceView previewSurfaceView;
	private SurfaceHolder previewSurfaceHolder;
	private Boolean previewing = false;
	private TextView detect;

	private Bitmap bitmapPicture;
	private Boolean doingBoolean = false;
	private static int widthP;
	private static int heightP;

	private int imageWidth, imageHeight;
	private FaceDetector myFaceDetect;
	private FaceDetector.Face[] faceDetected;
	private final int numberOfFace = 1;// Just detect one face
	private int numberOfFaceDetected;
	private byte[] frameData;
	private PlanarYUVLuminanceSource source;
	private Bitmap tmp;
	private Bitmap tmp2;

	// For face detection==========↑

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();

		// For Full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.gameview);
		findViews();

		// TODO checkSyncState();

		gameStart();

	}

	private void init() {
		host = getIntent().getBooleanExtra("host", true);
		if (host) {
			mAgent = HostActivity.mServerAgent;
		} else {
			mAgent = ClientActivity.mClientAgent;
		}

		readThread = new Thread(new ReadThread(host, mAgent, mHandler));
		readThread.start();

		mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
		}
	}

	private void checkSyncState() {
		// TODO Auto-generated method stub

	}

	private void gameStart() {

		faceThread = new Thread(new Runnable() {

			@Override
			public void run() {
				faceDetection();

			}
		});
		faceThread.start();

		// runOnUiThread(new Runnable() {
		//
		// @Override
		// public void run() {
		// if (host) {// Not handle sync problem
		// attackState();
		// testTextView.setText("HOST");
		// } else {
		// defendState();
		// testTextView.setText("CLIENT");
		// }
		// }
		//
		// });

		// gameThread = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		if (host) {// Not handle sync problem
			motionHint.setText("Go attack!");
			infoLinearLayout.setBackgroundResource(R.drawable.sf_slap_attack);
			attackState();
			// testTextView.setText("HOST");
		} else {
			motionHint.setText("Ready to look monitor!");
			infoLinearLayout.setBackgroundResource(R.drawable.sf_look_the_phone_monitor);
			defendState();
			// testTextView.setText("CLIENT");
		}
		// }
		//
		// });
		// gameThread.start();

		// Game start (do sync)

	}

	private void attackState() {
		testTextView3.setBackgroundColor(Color.RED);
		isAttackState = true;
		isDefendState = false;
		atkButton.setClickable(true);

	}

	private void defendState() {
		testTextView3.setBackgroundColor(Color.RED);
		isDefendState = true;
		isAttackState = false;
		atkButton.setClickable(false);
		isMissOkTimeState = false;
		missChance = 1;

	}

	private void checkMissAction() {
		// This line should do exchange animation between attack and defend
		motionHint.setText("Ready to look monitor!");
		mHandler.sendEmptyMessageDelayed(messageCode.MISS_START, 600);
		infoLinearLayout.setBackgroundResource(R.drawable.sf_look_the_phone_monitor);
		// Countdown miss time now is 1.5s
		mHandler.sendEmptyMessageDelayed(messageCode.COUNTDOWN_START, 1600);
		mHandler.sendEmptyMessageDelayed(messageCode.COUNTDOWN_OVER, 2600);
	}

	private void findViews() {

		previewSurfaceView = (SurfaceView) findViewById(R.id.facePreview);
		previewSurfaceHolder = previewSurfaceView.getHolder();
		previewSurfaceHolder.addCallback(GameActivity.this);
		previewSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		infoLinearLayout = (LinearLayout) findViewById(R.id.infoLayout);

		detect = (TextView) findViewById(R.id.detectHint);

		myHpBar = (ProgressBar) findViewById(R.id.myHpBar);
		enemyHpBar = (ProgressBar) findViewById(R.id.enemyHpBar);
		sl = (LinearLayout) findViewById(R.id.previewLayout);
		myHP = (TextView) findViewById(R.id.myHP);
		enemyHP = (TextView) findViewById(R.id.enemyHP);

		motionHint = (TextView) findViewById(R.id.motionHint);
		testTextView = (TextView) findViewById(R.id.textView1);
		testTextView2 = (TextView) findViewById(R.id.textView2);
		testTextView3 = (TextView) findViewById(R.id.textView3);
		atkButton = (Button) findViewById(R.id.atkBtn);
		atkButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isAttackState) {
					mAgent.write("ATK&20");
					defendState();
					mVibrator.vibrate(200);
					Log.e(tag, "Press ATK");
				}

			}
		});
	}

	private void gameOverWinner(Boolean isWinner) {
		Intent intent = new Intent();
		intent.setClass(GameActivity.this, GameOverActivity.class);
		intent.putExtra("WIN", isWinner);
		intent.putExtra("host", host);
		startActivity(intent);
		finish();
	}

	// Face detection part==========↓
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		Size size = myCamera.getParameters().getPreviewSize();

		widthP = size.width;
		heightP = size.height;
		if (previewing) {
			myCamera.stopPreview();
			previewing = false;
		}

		try {
			myCamera.setDisplayOrientation(90);
			myCamera.setPreviewDisplay(holder);
			myCamera.startPreview();
			myCamera.setPreviewCallback(this);
			previewing = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		if (myCamera == null) {
			myCamera = Camera.open(getFrontCameraId());
		}

	}

	private int getFrontCameraId() {
		CameraInfo ci = new CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, ci);
			if (ci.facing == CameraInfo.CAMERA_FACING_FRONT)
				return i;
		}
		return -1; // No front-facing camera found
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		myCamera.setPreviewCallback(null);
		myCamera.stopPreview();
		myCamera.release();
		myCamera = null;
		previewing = false;

	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		// bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);

		// if (!doingBoolean) {
		// doingBoolean = true;
		// PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data,
		// widthP, heightP, 0, 0, widthP, heightP, true);
		// Bitmap tmp = source.renderCroppedGreyscaleBitmap();
		//
		// Matrix m = new Matrix();
		// m.setRotate(90);
		//
		// BitmapFactory.Options BitmapFactoryOptionsbfo = new
		// BitmapFactory.Options();
		// BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
		//
		// // Need to speed up==========
		// Bitmap tmp2 = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(),
		// tmp.getHeight(), m, true);
		// bitmapPicture = Bitmap.createScaledBitmap(tmp2, (int)
		// (tmp2.getWidth() * 0.20), (int) (tmp2.getHeight() * 0.20), true);
		//
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		//
		// bitmapPicture = BitmapFactory.decodeByteArray(baos.toByteArray(), 0,
		// baos.toByteArray().length, BitmapFactoryOptionsbfo);
		// // Need to speed up==========
		//
		// imageWidth = bitmapPicture.getWidth();
		// imageHeight = bitmapPicture.getHeight();
		// faceDetected = new FaceDetector.Face[numberOfFace];
		// myFaceDetect = new FaceDetector(imageWidth, imageHeight,
		// numberOfFace);
		// numberOfFaceDetected = myFaceDetect.findFaces(bitmapPicture,
		// faceDetected);
		//
		// if (numberOfFaceDetected > 0) {
		// detect.setText("true");
		// detect.setBackgroundColor(Color.GREEN);
		// // Log.e(tag, "detected");
		// } else {
		// detect.setText("false");
		// detect.setBackgroundColor(Color.RED);
		// // Log.e(tag, "not detected");
		// }
		// doingBoolean = false;
		// }

		frameData = data;

	}

	private void faceDetection() {
		while (true) {

			if (!doingBoolean && frameData != null) {
				doingBoolean = true;
				source = new PlanarYUVLuminanceSource(frameData, widthP, heightP, 0, 0, widthP, heightP, true);
				tmp = source.renderCroppedGreyscaleBitmap();

				Matrix m = new Matrix();
				m.setRotate(90);

				BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
				BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;

				// TODO Need to speed up and resize by camera resolution==========
				tmp2 = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), m, true);
				bitmapPicture = Bitmap.createScaledBitmap(tmp2, (int) (tmp2.getWidth() * 0.20), (int) (tmp2.getHeight() * 0.20), true);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);

				bitmapPicture = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length, BitmapFactoryOptionsbfo);
				// TODO Need to speed up and resize by camera resolution==========

				imageWidth = bitmapPicture.getWidth();
				imageHeight = bitmapPicture.getHeight();
				faceDetected = new FaceDetector.Face[numberOfFace];
				myFaceDetect = new FaceDetector(imageWidth, imageHeight, numberOfFace);
				numberOfFaceDetected = myFaceDetect.findFaces(bitmapPicture, faceDetected);

				if (numberOfFaceDetected > 0) {
					// detect.setText("true");
					// detect.setBackgroundColor(Color.GREEN);

					mHandler.sendEmptyMessage(messageCode.FACE_DETECT);

					// Log.e(tag, "detected");
				} else {
					// detect.setText("false");
					// detect.setBackgroundColor(Color.RED);

					mHandler.sendEmptyMessage(messageCode.FACE_NOT_DETECT);

					// If want to make more missChance, should modify this part
					// Use to count miss when miss time open
					if (countMissTime && missChance > 0 && isMissOkTimeState) {
						missChance--;
						mHandler.sendEmptyMessage(messageCode.MISS_OK);

					} else if (countMissTime && missChance > 0) {
						missChance--;
						mHandler.sendEmptyMessage(messageCode.MISS_FAIL);

					}
					// Log.e(tag, "not detected");
				}
				doingBoolean = false;
			}
		}
	}

	// Face detection part==========↑

	// Use for handler message code
	public interface messageCode {
		public static final int FACE_DETECT = 0;
		public static final int FACE_NOT_DETECT = 1;
		public static final int COUNTDOWN_OVER = 2;
		public static final int MISS_START = 3;
		public static final int COUNTDOWN_START = 5;
		public static final int MISS_FAIL = 4;
		public static final int MISS_OK = 6;
		public static final int ENEMY_HP_ACK = 7;
		public static final int OVER = 8;
		public static final int OVER_VIEW = 9;
		public static final int ATK = 10;
		public static final int CHANGE_ATK_STATE = 11;
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);

			switch (msg.what) {
			case messageCode.ATK:
				if (isDefendState) {
					damage = Integer.valueOf(msg.obj.toString());
					// Ready to miss state
					mVibrator.vibrate(500);
					checkMissAction();
				}
				break;
			case messageCode.FACE_DETECT:
				sl.setBackgroundColor(Color.GREEN);
				isDetect = true;
				detect.setText("true");
				detect.setBackgroundColor(Color.GREEN);
				// Log.e(tag, "detect");
				break;
			case messageCode.FACE_NOT_DETECT:
				sl.setBackgroundColor(Color.RED);
				isDetect = false;
				detect.setText("false");
				detect.setBackgroundColor(Color.RED);
				// Log.e(tag, "not detect");
				break;
			case messageCode.MISS_START:
				testTextView3.setBackgroundColor(Color.GREEN);
				countMissTime = true;
				break;
			case messageCode.COUNTDOWN_START:
				if (missChance > 0) {
					motionHint.setText("Turn your face now!");
					infoLinearLayout.setBackgroundResource(R.drawable.sf_turn_face_away_defend);
				}
				isMissOkTimeState = true;
				testTextView2.setText("True");
				break;
			case messageCode.COUNTDOWN_OVER:
				isMissOkTimeState = false;
				testTextView2.setText("False");
				if (countMissTime && missChance > 0) {
					// mHandler.sendEmptyMessageDelayed(messageCode.MISS_FAIL, 1000);
					mHandler.sendEmptyMessage(messageCode.MISS_FAIL);
					countMissTime = false;
				}
				mHandler.sendEmptyMessageDelayed(messageCode.CHANGE_ATK_STATE, 500);
				break;
			case messageCode.MISS_OK:
				Log.e(tag, "MISS_OK");
				motionHint.setText("MISS! Good job!");
				infoLinearLayout.setBackgroundResource(R.drawable.sf_escaped);
				countMissTime = false;
				// isMissOkTimeState = false;
				mAgent.write("HP&" + String.valueOf(myHpBar.getProgress()));

				// TODO ======Show wait to ATK move attackState(); to COUNTDOWN_OVER======
				// attackState();// Wait to set delay after COUNTDOWN_OVER
				break;
			case messageCode.MISS_FAIL:
				Log.e(tag, "MISS_FAIL");
				motionHint.setText("Not MISS! Too bad!");
				infoLinearLayout.setBackgroundResource(R.drawable.sf_failure);
				countMissTime = false;
				// isMissOkTimeState = false;
				myHpBar.setProgress(myHpBar.getProgress() - damage);
				if (myHpBar.getProgress() > 0) {
					mAgent.write("HP&" + String.valueOf(myHpBar.getProgress()));
					myHP.setText(String.valueOf(myHpBar.getProgress()));
					// TODO ======Show wait to ATK move attackState(); to COUNTDOWN_OVER======
					// attackState();// Wait to set delay after COUNTDOWN_OVER
				} else {
					myHP.setText(String.valueOf(myHpBar.getProgress()));
					motionHint.setText("You lose!");
					infoLinearLayout.setBackgroundResource(R.drawable.sf_lose_min);
					mAgent.write("OVER");
					winner = false;
					mHandler.sendEmptyMessageDelayed(messageCode.OVER_VIEW, 2000);
				}
				break;
			case messageCode.ENEMY_HP_ACK:
				Log.e(tag, "HP_ACK");
				if (Integer.valueOf(msg.obj.toString()) < enemyHpBar.getProgress()) {
					motionHint.setText("Hit!");
					infoLinearLayout.setBackgroundResource(R.drawable.sf_hit);
				} else {
					motionHint.setText("Not Hit!");
					infoLinearLayout.setBackgroundResource(R.drawable.sf_miss);
				}
				enemyHpBar.setProgress(Integer.valueOf(msg.obj.toString()));
				enemyHP.setText(String.valueOf(enemyHpBar.getProgress()));
				break;
			case messageCode.OVER:
				motionHint.setText("You win!");
				infoLinearLayout.setBackgroundResource(R.drawable.sf_win_min);
				enemyHpBar.setProgress(0);
				winner = true;
				enemyHP.setText("0");
				mHandler.sendEmptyMessageDelayed(messageCode.OVER_VIEW, 2000);
				break;
			case messageCode.OVER_VIEW:
				gameOverWinner(winner);
				break;
			case messageCode.CHANGE_ATK_STATE:
				motionHint.setText("Go attack!");
				infoLinearLayout.setBackgroundResource(R.drawable.sf_slap_attack);
				attackState();
				break;

			default:
				break;
			}

		}
	};

	public double getAccelerate(float x, float y, float z) {
		// Calculate Accelerate
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {// Use to Attack

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && isAttackState) {
			// TODO ===========Wait to train slap motion===========
			accelerate = getAccelerate(event.values[0], event.values[1], event.values[2]);
			testTextView.setText(" " + accelerate);
			if (accelerate >= 33) {
				mVibrator.vibrate(200);
				mAgent.write("ATK&20");// TODO make real damage
				defendState();
				Log.e(tag, "ATK");
			}
			// TODO ===========Wait to train slap motion===========
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onPause() {
		super.onPause();
		mVibrator.cancel();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
