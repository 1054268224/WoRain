package com.odm.tool.handingpaint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class HandlePaintActivity extends Activity {

    private static final int REQUEST_CAMERA_PERMISSIONS = 1;
    private static boolean isCameraPermissionGrant = false;

    private CameraHelper mCameraHelper;

    private SensorManager mSensorManager;
    
    private TextView mHorizentalDegree;
    private TextView mVerticalDegree;
    private AlignmentView mAlignmentView;
    private SurfaceView mSurfaceView;

    private int mOldAngle;
    private int mOldAnglex;
    private int mOldAngley;
    private static long STABLE_MIN_TIME = 1500;
    private long mStableTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_paint);
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

    @Override
    public void onResume() {
        super.onResume();
        //M:hongqian EJQQ-593 20190927 start
        //if (isCameraPermissionGrant) {
        if (isCameraPermissionGrant && mCameraHelper != null) {
        //M:hongqian EJQQ-593 20190927 end
            mCameraHelper.startBackgroundThread();
            mSurfaceView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        //M:hongqian EJQQ-593 20190927 start
        //if (isCameraPermissionGrant) {
        if (isCameraPermissionGrant && mCameraHelper != null) {
        //M:hongqian EJQQ-593 20190927 end
            mCameraHelper.closeCamera();
            mCameraHelper.stopBackgroundThread();
            mSurfaceView.setVisibility(View.GONE);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //modify bug:EWSWQ-498 wushanfei 20200327 start
        if(mSensorManager != null){
            mSensorManager.unregisterListener(mSensorEventListener);
        }
        //modify bug:EWSWQ-498 wushanfei 20200327 end
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
            //modify by wangjian for EJQQQ-658 20200227 start
            //if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (grantResults != null && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //modify by wangjian for EJQQQ-658 20200227 end
                isCameraPermissionGrant = true;
                initUI();
                //M:hongqian EJQQ-593 20190927 start
                //mCameraHelper.startBackgroundThread();
                //mCameraHelper.openCamera();
                if (mCameraHelper != null){
                    mCameraHelper.startBackgroundThread();
                    mCameraHelper.openCamera();
                }
                //M:hongqian EJQQ-593 20190927 end
            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initUI() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor_accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mSensorEventListener ,sensor_accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mSurfaceView = (SurfaceView) findViewById(R.id.camera_layout);
        mCameraHelper = new CameraHelper(this, mSurfaceView, null, null);
        
        mHorizentalDegree = findViewById(R.id.horizental_degree);
        mVerticalDegree = findViewById(R.id.vertical_degree);
        mAlignmentView = findViewById(R.id.alignment);
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // In this example, alpha is calculated as t / (t + dT),
                // where t is the low-pass filter's time-constant and
                // dT is the event delivery rate.
                float[] gravity=new float[3];

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
                
                float OneEightyOverPi = 57.29577957855f;
                int angle = (int) Math.round(Math.atan2(-Y, X) * OneEightyOverPi);
                int anglex = (int) Math.round(Math.abs(Math.atan2(Y, X) * 180f / Math.PI));
                int angley = (int) Math.round(Math.abs(Math.atan2(X, Y) * 180f / Math.PI));

                if(Math.abs(angle - mOldAngle) >= 3 ||
                        Math.abs(anglex - mOldAnglex) >= 3 ||
                        Math.abs(angley - mOldAngley) >= 3){
                    mOldAngle = angle;
                    mOldAnglex = anglex;
                    mOldAngley = angley;
                    mStableTime  = System.currentTimeMillis();
                } else if (Math.abs(System.currentTimeMillis() - mStableTime) > STABLE_MIN_TIME){
                    angle = mOldAngle;
                    anglex = mOldAnglex;
                    angley = mOldAngley;
                }
                mHorizentalDegree.setText(String.valueOf(anglex) + getResources().getString(R.string.unit));
                mVerticalDegree.setText(String.valueOf(angley) + getResources().getString(R.string.unit));
                mAlignmentView.setDegree(angle);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
