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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback, SensorEventListener {
	private final String tag = getClass().getName();

	private Thread readThread;
	private boolean host;
	boolean isHost;// True: Host, False: client.
	boolean adState;// True: Attacker, False: Defender.
	boolean isAttacking;// True: had attacked, now wait for response
	private int myHp;
	private int enemyHp;
	private SocketAgent mAgent;
	private ProgressBar myHpBar;
	private ProgressBar enemyHpBar;

	// For face detection==========
	private Camera myCamera;
	private SurfaceView previewSurfaceView;
	private SurfaceHolder previewSurfaceHolder;
	private boolean previewing = false;
	private TextView detect;

	private Bitmap bitmapPicture;
	private boolean doingBoolean = false;
	private static int widthP;
	private static int heightP;

	private int imageWidth, imageHeight;
	private FaceDetector myFaceDetect;
	private FaceDetector.Face[] faceDetected;
	private final int numberOfFace = 1;
	private int numberOfFaceDetected;

	// For face detection==========

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		// For Full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.gameview);
		findViews();

		// host = getIntent().getBooleanExtra("host", true);
		// init(host);
	}

	private void findViews() {

		previewSurfaceView = (SurfaceView) findViewById(R.id.facePreview);
		previewSurfaceHolder = previewSurfaceView.getHolder();
		previewSurfaceHolder.addCallback(GameActivity.this);
		previewSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		detect = (TextView) findViewById(R.id.detectHint);
		
		myHpBar=(ProgressBar)findViewById(R.id.myHpBar);
		myHpBar=(ProgressBar)findViewById(R.id.enemyHpBar);
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
		isAttacking = false;
		myHp = 100;
		enemyHp = 100;

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		myCamera.setPreviewCallback(null);
		myCamera.stopPreview();
		myCamera.release();
		myCamera = null;
		previewing = false;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		// bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);

		if (!doingBoolean) {
			doingBoolean = true;
			PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, widthP, heightP, 0, 0, widthP, heightP, true);
			Bitmap tmp = source.renderCroppedGreyscaleBitmap();

			Matrix m = new Matrix();
			m.setRotate(90);

			BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
			BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;

			// Need to speed up==========
			Bitmap tmp2 = Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), m, true);
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
				detect.setText("true");
				detect.setBackgroundColor(Color.GREEN);
				Log.e(tag, "detected");
			} else {
				detect.setText("false");
				detect.setBackgroundColor(Color.RED);
				Log.e(tag, "not detected");
			}
			doingBoolean = false;
		}

	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// test.setText(msg.obj.toString());

		}
	};

	private void reflash() {// used to reflash view
		isAttacking = false;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {// Use to Attack
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			double accelerate = getAccelerate(event.values[0], event.values[1], event.values[2]);
			if (accelerate > 20 && adState == true && isAttacking == false) {
				isAttacking = true;
				attack(accelerate);
			}
		}
	}

	public double getAccelerate(float x, float y, float z) {// Calculate
															// Accelerate
		return Math.sqrt(x * x + y * y + z * z);
	}

	private void attack(double pow) {
		int attackValue = (int) ((pow - 15) / 5);
		sendMsg(1, attackValue);
	}

	private void getRespond(int eHP) {
		enemyHp = eHP;
		if (enemyHp <= 0) {
			isGameOver(true);
		} else {
			reflash();
			adState = true;
			waitThreeSecond();
		}
	}

	private void waitThreeSecond() {// used to wait three second
	}

	private void isGameOver(boolean isYouWin) {// isYouWin==true: attacker win
	}

	// ////////////////////////////////////////////////////////
	private void sendMsg(int type, Object pow) {// TODO: Peter's work
		// TODO Auto-generated method stub
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}
}
