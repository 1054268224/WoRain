package com.odm.tool.compass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import com.odm.tool.BaseWheatekActivity;
import com.odm.tool.R;
import com.odm.tool.compass.CompassView;

public class CompassActivity extends Activity implements View.OnClickListener, SensorEventListener/*add by zhiheng.huang*/{

    private final float MAX_ROATE_DEGREE = 1.0f;
    private SensorManager mSensorManager;
    private Sensor mOrientationSensor;
    private float mDirection;
    private float mTargetDirection;
    private AccelerateInterpolator mInterpolator;
    protected final Handler mHandler = new Handler();
    private boolean mStopDrawing;
    private boolean mChinease;

    private View mCompassView;
    private CompassView mPointer;
    private LinearLayout mDirectionLayout;
    private LinearLayout mAngleLayout;

    private ImageView im_back;
    private ImageView im_help;
    // Add by zhiheng.huang on 2020/12/21 for  start
    private RelativeLayout mHelpLayout;
    private ImageView mGuideAnimation;
    private AnimationDrawable anim;
    private Sensor mMagneticSensor;
    private Sensor mAccelerometerSensor;
    private Vibrator mVibrator;
    private long mEnterTime;
    private boolean needCalibration = false;
    private final int minPassTimes = 70;
    private int xPassTimes = 0;
    private int yPassTimes = 0;
    private int zPassTimes = 0;
    private int zNegativePassTimes = 0;
    private float[] mGData;
    private int mAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_LOW;
    private int oAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_LOW;
    // Add by zhiheng.huang on 2020/12/21 for  end

    private static long STABLE_MIN_TIME = 1500;
    private long mStableTime;

    protected Runnable mCompassViewUpdater = new Runnable() {
        @Override
        public void run() {
            if (mPointer != null && !mStopDrawing) {
                if (mDirection != mTargetDirection) {

                    // calculate the short routine
                    float to = mTargetDirection;
                    if (to - mDirection > 180) {
                        to -= 360;
                    } else if (to - mDirection < -180) {
                        to += 360;
                    }

                    // limit the max speed to MAX_ROTATE_DEGREE
                    float distance = to - mDirection;
                    if (Math.abs(distance) > MAX_ROATE_DEGREE) {
                        distance = distance > 0 ? MAX_ROATE_DEGREE : (-1.0f * MAX_ROATE_DEGREE);
                    }

                    // need to slow down if the distance is short
                    mDirection = normalizeDegree(mDirection
                            + ((to - mDirection) * mInterpolator.getInterpolation(Math
                            .abs(distance) > MAX_ROATE_DEGREE ? 0.4f : 0.3f)));
                    mPointer.updateDirection(mDirection);
                }

                updateDirection();

                mHandler.postDelayed(mCompassViewUpdater, 20);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        initResources();
        initServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        mStopDrawing = false;
        mHandler.postDelayed(mCompassViewUpdater, 20);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStopDrawing = true;
        // Add by zhiheng.huang on 2020/12/21 for  start
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
        // Add by zhiheng.huang on 2020/12/21 for  end
    }

    private void initResources() {
        mDirection = 0.0f;
        mTargetDirection = 0.0f;
        mInterpolator = new AccelerateInterpolator();
        mStopDrawing = true;
        mChinease = TextUtils.equals(Locale.getDefault().getLanguage(), "zh");

        mCompassView = findViewById(R.id.view_compass);
        mPointer = (CompassView) findViewById(R.id.compass_pointer);
        mDirectionLayout = (LinearLayout) findViewById(R.id.layout_direction);
        mAngleLayout = (LinearLayout) findViewById(R.id.layout_angle);

        im_back = findViewById(R.id.compass_back);
        im_help = findViewById(R.id.compass_help);
        im_back.setOnClickListener(this);
        im_help.setOnClickListener(this);
        mPointer.setImageResource(mChinease ? R.drawable.compass_cn : R.drawable.compass);

        // Add by zhiheng.huang on 2020/12/21 for  start
        mHelpLayout = findViewById(R.id.help_layout);
        mGuideAnimation = findViewById(R.id.guide_animation);
        anim = (AnimationDrawable) mGuideAnimation.getDrawable();
        anim.start();
        // Add by zhiheng.huang on 2020/12/21 for  end
    }

    private void initServices() {
        // sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        // location manager
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        // Add by zhiheng.huang on 2020/12/21 for  start
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mVibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
        mEnterTime = System.currentTimeMillis();
        // Add by zhiheng.huang on 2020/12/21 for  end
    }

    private void updateDirection() {
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        mDirectionLayout.removeAllViews();
        mAngleLayout.removeAllViews();

        ImageView east = null;
        ImageView west = null;
        ImageView south = null;
        ImageView north = null;
        float direction = normalizeDegree(mTargetDirection * -1.0f);
        if (direction > 22.5f && direction < 157.5f) {
            // east
            east = new ImageView(this);
            east.setImageResource(mChinease ? R.drawable.e_cn : R.drawable.e);
            east.setLayoutParams(lp);
        } else if (direction > 202.5f && direction < 337.5f) {
            // west
            west = new ImageView(this);
            west.setImageResource(mChinease ? R.drawable.w_cn : R.drawable.w);
            west.setLayoutParams(lp);
        }

        if (direction > 112.5f && direction < 247.5f) {
            // south
            south = new ImageView(this);
            south.setImageResource(mChinease ? R.drawable.s_cn : R.drawable.s);
            south.setLayoutParams(lp);
        } else if (direction < 67.5 || direction > 292.5f) {
            // north
            north = new ImageView(this);
            north.setImageResource(mChinease ? R.drawable.n_cn : R.drawable.n);
            north.setLayoutParams(lp);
        }

        if (mChinease) {
            // east/west should be before north/south
            if (east != null) {
                mDirectionLayout.addView(east);
            }
            if (west != null) {
                mDirectionLayout.addView(west);
            }
            if (south != null) {
                mDirectionLayout.addView(south);
            }
            if (north != null) {
                mDirectionLayout.addView(north);
            }
        } else {
            if (south != null) {
                mDirectionLayout.addView(south);
            }
            if (north != null) {
                mDirectionLayout.addView(north);
            }
            if (east != null) {
                mDirectionLayout.addView(east);
            }
            if (west != null) {
                mDirectionLayout.addView(west);
            }
        }

        int direction2 = (int) direction;
        boolean show = false;
        if (direction2 >= 100) {
            mAngleLayout.addView(getNumberImage(direction2 / 100));
            direction2 %= 100;
            show = true;
        }
        if (direction2 >= 10 || show) {
            mAngleLayout.addView(getNumberImage(direction2 / 10));
            direction2 %= 10;
        }
        mAngleLayout.addView(getNumberImage(direction2));

        ImageView degreeImageView = new ImageView(this);
        degreeImageView.setImageResource(R.drawable.degree);
        degreeImageView.setLayoutParams(lp);
        mAngleLayout.addView(degreeImageView);
    }

    private ImageView getNumberImage(int number) {
        ImageView image = new ImageView(this);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        switch (number) {
            case 0:
                image.setImageResource(R.drawable.number_0);
                break;
            case 1:
                image.setImageResource(R.drawable.number_1);
                break;
            case 2:
                image.setImageResource(R.drawable.number_2);
                break;
            case 3:
                image.setImageResource(R.drawable.number_3);
                break;
            case 4:
                image.setImageResource(R.drawable.number_4);
                break;
            case 5:
                image.setImageResource(R.drawable.number_5);
                break;
            case 6:
                image.setImageResource(R.drawable.number_6);
                break;
            case 7:
                image.setImageResource(R.drawable.number_7);
                break;
            case 8:
                image.setImageResource(R.drawable.number_8);
                break;
            case 9:
                image.setImageResource(R.drawable.number_9);
                break;
        }
        image.setLayoutParams(lp);
        return image;
    }

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.compass_back:
                finish();
                break;
            case  R.id.compass_help:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClass(CompassActivity.this,CompassHelpActivity.class);
                startActivity(intent);
                break;
        }
    }
    //add by wuzhi.peng for TQQB-226 start
    private float lastX;
    private float getStableValue(float xValue){
        if(Math.abs(xValue - lastX)>=3){
            lastX =xValue;
            mStableTime  = System.currentTimeMillis();
        } else {
            if (Math.abs(System.currentTimeMillis() - mStableTime) > STABLE_MIN_TIME){
                xValue = lastX;
            }
        }
        return xValue;
    }
    //add by wuzhi.peng for TQQB-226 end

    // Add by zhiheng.huang on 2020/12/21 for  start
    private void refreshLayout(boolean showHelp) {
        mHelpLayout.setVisibility(showHelp ? View.VISIBLE : View.GONE);
        mCompassView.setVisibility(!showHelp ? View.VISIBLE : View.GONE);
    }

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

            refreshLayout(false);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int type = event.sensor.getType();
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                mGData = event.values;
                if (needCalibration) {
                    judgeCalibration();
                }
                break;
            case Sensor.TYPE_ORIENTATION:
                float direction = getStableValue(event.values[0]) * -1.0f;
                mTargetDirection = normalizeDegree(direction);

                if (System.currentTimeMillis() - mEnterTime > 2000) {
                    oAccuracy = event.accuracy;
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mAccuracy = event.accuracy;
                if ((mAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
                        || (oAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)) {
                    needCalibration = true;
                    refreshLayout(true);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            if (mHelpLayout.getVisibility() == View.VISIBLE) {
//                Toast.makeText(this,getString(R.string.compass_calibrate_successful_text),Toast.LENGTH_SHORT).show();
                mVibrator.vibrate(500);
            }
            refreshLayout(false);
        } else {
            refreshLayout(true);
            Log.d("HZH", "onAccuracyChanged: need calibration");
        }
    }
    // Add by zhiheng.huang on 2020/12/21 for  end
}
