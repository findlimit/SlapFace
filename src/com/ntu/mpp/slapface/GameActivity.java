package com.ntu.mpp.slapface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback, SensorEventListener {

	private final String tag = getClass().getName();// For Log usage

	private Thread readThread;
	private Thread gameThread;
	private Thread faceThread;
	private Boolean host;
	private SocketAgent mAgent;
	private ProgressBar myHpBar;
	private ProgressBar enemyHpBar;
	private Double accelerate;

	private TextView testTextView;
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

		gameThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (host) {// Not handle sync problem
					attackState();
					// testTextView.setText("HOST");
				} else {
					defendState();
					// testTextView.setText("CLIENT");
				}
			}

		});
		gameThread.start();

	}

	private void attackState() {
		atkButton.setClickable(true);

	}

	private void defendState() {
		atkButton.setClickable(false);

	}

	private void findViews() {

		previewSurfaceView = (SurfaceView) findViewById(R.id.facePreview);
		previewSurfaceHolder = previewSurfaceView.getHolder();
		previewSurfaceHolder.addCallback(GameActivity.this);
		previewSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		detect = (TextView) findViewById(R.id.detectHint);

		myHpBar = (ProgressBar) findViewById(R.id.myHpBar);
		myHpBar = (ProgressBar) findViewById(R.id.enemyHpBar);

		testTextView = (TextView) findViewById(R.id.textView1);
		atkButton = (Button) findViewById(R.id.atkBtn);
		atkButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mAgent.write("ATK");
				Log.e(tag, "Press ATK");
			}
		});
		// atkButton.setClickable(false);
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
		if (!doingBoolean) {
			frameData = data;
		}

	}

	private void faceDetection() {
		while (true) {

			if (!doingBoolean) {
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
					//detect.setText("true");
					//detect.setBackgroundColor(Color.GREEN);
					// Log.e(tag, "detected");
				} else {
				//	detect.setText("false");
				//	detect.setBackgroundColor(Color.RED);
					// Log.e(tag, "not detected");
				}
				doingBoolean = false;
			}
		}
	}

	// Face detection part==========↑

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			// test.setText(msg.obj.toString());

			switch (msg.what) {
			case 0:

				myHpBar.setProgress(myHpBar.getProgress() - 10);

				break;

			default:
				break;
			}

		}
	};

	public double getAccelerate(float x, float y, float z) {// Calculate
		// Accelerate
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {// Use to Attack

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelerate = getAccelerate(event.values[0], event.values[1], event.values[2]);

		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}
}
