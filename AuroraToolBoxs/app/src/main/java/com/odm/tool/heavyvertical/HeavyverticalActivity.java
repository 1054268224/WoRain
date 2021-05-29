package com.odm.tool.heavyvertical;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.odm.tool.R;
import com.odm.tool.util.CameraHelper;


public class HeavyverticalActivity extends Activity {

    private static final int REQUEST_CAMERA_PERMISSIONS = 1;
    private static boolean isCameraPermissionGrant = false;

    private CameraHelper mCameraHelper;
    private SensorManager mSensorManager;

    private PlumbPointView mPlumbPoint;
    private TextView mVerticalDegree;
    private SurfaceView mSurfaceView;

    private DecimalFormat mDecimalFormat = new DecimalFormat("0.0");

    private double mOldDegree;
    private static long STABLE_MIN_TIME = 1500;
    private long mStableTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heavyvertical);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSIONS);
            isCameraPermissionGrant = false;
        } else {
            isCameraPermissionGrant = true;
            initUI();
        }
    }
	
    private void initUI() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor_accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mSensorEventListener ,sensor_accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mSurfaceView = (SurfaceView) findViewById(R.id.camera_layout);
        mCameraHelper = new CameraHelper(this, mSurfaceView, null, null);

        mPlumbPoint = findViewById(R.id.plumb_point_img);
        mVerticalDegree = findViewById(R.id.vertical_degree);
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCameraPermissionGrant) {
            mCameraHelper.startBackgroundThread();
            mSurfaceView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        if (isCameraPermissionGrant) {
            mCameraHelper.closeCamera();
            mCameraHelper.stopBackgroundThread();
            mSurfaceView.setVisibility(View.GONE);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //modify by wangjian for EJQQQ-658 20200227 start
        //mSensorManager.unregisterListener(mSensorEventListener);
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorEventListener);
        }
        //modify by wangjian for EJQQQ-658 20200227 end
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isCameraPermissionGrant = true;
                    initUI();
                    mCameraHelper.startBackgroundThread();
                    mCameraHelper.openCamera();
                } else {
                    finish();
                }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // In this example, alpha is calculated as t / (t + dT),
                // where t is the low-pass filter's time-constant and
                // dT is the event delivery rate.
                float[] gravity=new float[3];
                float[] linear_acceleration=new float[3];

                // 1，定义参数
                final float alpha = 0.8f;// 该参数取0.8，具体为什么这么取值，没说，但是就这么用就行了。

                // 2，Isolate the force of gravity with the low-pass filter.，得到每个方向上的重力加速度
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                // 3，Remove the gravity contribution with the high-pass filter.，过滤掉重力加速度的影响，得到过滤后的加速度
                float X = event.values[0] - gravity[0];
                float Y = event.values[1] - gravity[1];
                float Z = event.values[2] - gravity[2];

                double degree = (double) Math.round(Math.atan2(-Y, X) * 180f / Math.PI);

                if(Math.abs(degree - mOldDegree) >= 3){
                    mOldDegree = degree;
                    mStableTime  = System.currentTimeMillis();
                } else if (Math.abs(System.currentTimeMillis() - mStableTime) > STABLE_MIN_TIME){
                    degree = mOldDegree;
                }
                if (degree > 90 ) {
                    degree = 90;
                } else if (degree < -90) {
                    degree = -90;
                }

                mPlumbPoint.setDegree(degree);
                mVerticalDegree.setText(mDecimalFormat.format(degree) + "°");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
