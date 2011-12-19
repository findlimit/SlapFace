package com.ntu.mpp.slapface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;

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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import android.view.Window;
import android.view.WindowManager;
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
	private Boolean isMissTimeState;
	private int missChance;
	private Boolean isDetect;

	private TextView testTextView;
	private TextView testTextView2;
	private Button atkButton;

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

		gameStart();

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
			attackState();
			// testTextView.setText("HOST");
		} else {
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
		isAttackState = true;
		isDefendState = false;
		atkButton.setClickable(true);

	}

	private void defendState() {
		isDefendState = true;
		isAttackState = false;
		atkButton.setClickable(false);
		isMissTimeState = false;
		missChance = 1;

	}

	private void missTimeStateCountdown() {
		isMissTimeState = true;
		testTextView2.setText("True");

		// Now miss time is 1.5 s
		mHandler.sendEmptyMessageDelayed(messageCode.COUNTDOWN_OVER, 1500);

		// runOnUiThread(new Runnable() {
		//
		// @Override
		// public void run() {
		// countThread = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// try {
		// Thread.sleep(1500);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// isMissTimeState = false;
		// testTextView2.setText("False");
		//
		// }
		// });
		// countThread.start();
		// // isMissTimeState = false;
		// // testTextView2.setText("False");
		// }
		// });

		// TimerTask task = new TimerTask() {
		//
		// @Override
		// public void run() {
		// isMissTimeState = false;
		// testTextView2.setText("False");
		// }
		// };
		// Timer timer = new Timer();
		// timer.schedule(task, 1 * 1000);

		// countThread = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// try {
		// Thread.sleep(1500);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// isMissTimeState = false;
		// //testTextView2.setText("False");
		//
		// }
		// });
		// countThread.start();

		// TimerTask task = new TimerTask() {
		// @Override
		// public void run() {
		// Thread tt = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// Log.e(tag, "10S");
		// while (true) {
		// Log.e(tag, "10S");
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
		// });
		// tt.start();
		//
		// }
		// };
		// Timer timer = new Timer();
		// timer.schedule(task, 5 * 1000);
	}

	private void findViews() {

		previewSurfaceView = (SurfaceView) findViewById(R.id.facePreview);
		previewSurfaceHolder = previewSurfaceView.getHolder();
		previewSurfaceHolder.addCallback(GameActivity.this);
		previewSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		detect = (TextView) findViewById(R.id.detectHint);

		myHpBar = (ProgressBar) findViewById(R.id.myHpBar);
		enemyHpBar = (ProgressBar) findViewById(R.id.enemyHpBar);

		testTextView = (TextView) findViewById(R.id.textView1);
		testTextView2 = (TextView) findViewById(R.id.textView2);
		atkButton = (Button) findViewById(R.id.atkBtn);
		atkButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (isAttackState) {
					mAgent.write("ATK");
					defendState();
					Log.e(tag, "Press ATK");
				}

			}
		});
	}

	private void gameOver(Boolean isWinner) {

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

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
		}
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

				// Need to speed up==========
				tmp2 = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), m, true);
				bitmapPicture = Bitmap.createScaledBitmap(tmp2, (int) (tmp2.getWidth() * 0.20), (int) (tmp2.getHeight() * 0.20), true);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);

				bitmapPicture = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length, BitmapFactoryOptionsbfo);
				// Need to speed up==========

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
		public static final int ATK = 1000;
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			// test.setText(msg.obj.toString());

			switch (msg.what) {
			case messageCode.ATK:
				if (isDefendState) {
					// Countdown miss time
					missTimeStateCountdown();

					myHpBar.setProgress(myHpBar.getProgress() - 10);
					attackState();// For test should do after return self HP
				}

				break;
			case messageCode.FACE_DETECT:
				isDetect = true;
				detect.setText("true");
				detect.setBackgroundColor(Color.GREEN);
				// Log.e(tag, "detect");
				break;

			case messageCode.FACE_NOT_DETECT:
				isDetect = false;
				detect.setText("false");
				detect.setBackgroundColor(Color.RED);
				// Log.e(tag, "not detect");
				break;
			case messageCode.COUNTDOWN_OVER:
				isMissTimeState = false;
				testTextView2.setText("False");
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
			accelerate = getAccelerate(event.values[0], event.values[1], event.values[2]);
			testTextView.setText(" " + accelerate);
			if (accelerate >= 33) {
				mAgent.write("ATK");
				defendState();
				Log.e(tag, "ATK");
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}
}
