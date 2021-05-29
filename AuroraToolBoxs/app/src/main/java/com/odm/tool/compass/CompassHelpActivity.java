package com.odm.tool.compass;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.odm.tool.R;

import java.util.Locale;


public class CompassHelpActivity extends Activity implements View.OnClickListener, SensorEventListener {
    private ImageView mGuideAnimation;
    private ImageView im_back;
    AnimationDrawable anim;
    private boolean mChinease;// 系统当前是否使用中文
    SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private Sensor mAccelerometerSensor;
    private Vibrator mVibrator;
    private int MAX_ANGLE = 30;//偏移角度
    private int mCalibrationCount;
    private boolean mYAngle, mY_Angle, mZAngle, mZ_Angle;
    private int mDelayVibraTime = 5000;
    private long mEnterTime;
    private boolean mShowCalibrateTost;
    // Add by HZH on 2019/8/2 for YJSQ-4558 start
    private Sensor oSensor;
    // Add by HZH on 2019/8/2 for YJSQ-4558 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass_help);
        initResources();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Add by HZH on 2019/8/2 for YJSQ-4558 start
        oSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        // Add by HZH on 2019/8/2 for YJSQ-4558 end
        mVibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
        mEnterTime = System.currentTimeMillis();
    }

    private void initResources() {
        mGuideAnimation = findViewById(R.id.guide_animation);
        im_back = findViewById(R.id.compass_help_back);
        im_back.setOnClickListener(this);
        mChinease = TextUtils.equals(Locale.getDefault().getLanguage(), "zh");

        anim = (AnimationDrawable) mGuideAnimation
                .getDrawable();
        anim.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mMagneticSensor,
                    SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mAccelerometerSensor,
                    SensorManager.SENSOR_DELAY_UI);
            // Add by HZH on 2019/8/2 for YJSQ-4558 start
            mSensorManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_UI);
            // Add by HZH on 2019/8/2 for YJSQ-4558 end
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mVibrator!=null) {
            mVibrator.cancel();
        }
    im_back.removeCallbacks(mDelayRunnable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.compass_help_back:
                finish();
                break;
        }
    }

    // Add by HZH on 2019/8/1 for YJSQ-4558 start
    private boolean needCalibration = true;
    private final int minPassTimes = 70;
    private int xPassTimes = 0;
    private int yPassTimes = 0;
    private int zPassTimes = 0;
    private int zNegativePassTimes = 0;
    private float[] mGData;
    private int mAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_LOW;
    private int oAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_LOW;

    private void judgeCalibration() {

        if (Math.abs(mGData[0]) >= 12) {
            xPassTimes++;
        }
        if (Math.abs(mGData[1]) >= 12) {
            yPassTimes++;
        }
        if (mGData[2] >= 12) {
            zPassTimes++;
        }
        if (mGData[2] <= -5) {
            zNegativePassTimes++;
        }

        if ((xPassTimes > minPassTimes)
                && (yPassTimes > minPassTimes)
                && (zPassTimes > minPassTimes)
                && (zNegativePassTimes > minPassTimes)
                && (mAccuracy >= SensorManager.SENSOR_STATUS_ACCURACY_LOW)
                && (oAccuracy >= SensorManager.SENSOR_STATUS_ACCURACY_LOW)) {
            needCalibration = false;

            xPassTimes = 0;
            yPassTimes = 0;
            zPassTimes = 0;
            zNegativePassTimes = 0;

            Toast.makeText(this, getString(R.string.compass_calibrate_successful_text), Toast.LENGTH_SHORT).show();
            mVibrator.vibrate(1000);

            new Handler().postDelayed(() -> finish(), 2500);
        }
    }
    // Add by HZH on 2019/8/1 for YJSQ-4558 end
    @Override
    public void onSensorChanged(SensorEvent event) {

        // Add by HZH on 2019/8/1 for YJSQ-4558 start
        int type = event.sensor.getType();
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                mGData = event.values;
                if (needCalibration) {
                    judgeCalibration();
                }
                break;
            case Sensor.TYPE_ORIENTATION:
                if (System.currentTimeMillis() - mEnterTime > 2000) {
                    oAccuracy = event.accuracy;
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mAccuracy = event.accuracy;
                if ((mAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
                        || (oAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)) {
                    needCalibration = true;
                }
                break;
            default:
                break;
        }
        // Add by HZH on 2019/8/1 for YJSQ-4558 end
    }


    Runnable mDelayRunnable = new Runnable(){
         @Override
            public void run() {
                mVibrator.vibrate(1000);
                finish();
            }
    };
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            if((System.currentTimeMillis() - mEnterTime)>mDelayVibraTime){
                // Modify by HZH on 2019/8/2 for YJSQ-4558 start
//                Toast.makeText(this,getString(R.string.compass_calibrate_successful_text),Toast.LENGTH_SHORT).show();
//                mVibrator.vibrate(1000);
//                finish();
                // Modify by HZH on 2019/8/2 for YJSQ-4558 end
            } else {
                if(!mShowCalibrateTost){
                    mShowCalibrateTost = true;
                    Toast.makeText(this,getString(R.string.compass_calibrate_successed),Toast.LENGTH_SHORT).show();
                    im_back.postDelayed(mDelayRunnable,3000);
                }
            }
        }
    }
}
