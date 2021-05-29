package com.odm.tool.heightmeasure;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.odm.tool.R;

import java.text.DecimalFormat;
import java.util.List;

public class HeightMeasureActivity extends Activity implements View.OnClickListener,
        /*NumberPicker.OnScrollListener,*/ NumberPicker.OnValueChangeListener {
    private final static String TAG = "HeightMeasureActivity";
    private final static int HEIGHT_MEASURE_VIEW = 0;
    private final static int SET_USER_HEIGHT_VIEW = 1;
    private final static int HEIGHT_MEASURE_TARGET_VIEW = 2;
    private final static int HEIGHT_MEASURE_RESULT_VIEW = 3;

    private final static String[] data = {"0", "1", "2"};
    private final static String[] date1 = {"0", "1", "2","3","4","5","6","7","8","9"};

    private int currentView = HEIGHT_MEASURE_VIEW;

    private ImageButton backButton;
    private Button bottomButton;

    private ScrollView heightMeasureScrollview;
    private RelativeLayout setUserHeight;
    private LinearLayout heightMeasureTarget;
    private LinearLayout heightMeasureResult;
    private LinearLayout commonBottom;

    private ImageView targetImageView;

    private NumberPicker firstNumber;
    private NumberPicker secondNumber;
    private NumberPicker larstNumber;

    private TextView userHeightTextView;
    private TextView angleOfElevationTextView;
    private TextView angleOfDepressionTextView;
    private TextView targetDistanceTextView;
    private TextView targetHeightTextView;


    private int[] userHeightArray = new int[]{1,7,5};
    private int userHeight = 175;

    private float angleOfElevation = 0f;
    private float angleOfDepression = 0f;

    private ActionBar mActionBar;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticSensor;
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    float[] orientationValues = new float[3];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_measure);

        mActionBar = getActionBar();//getSupportActionBar();

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(this);

        bottomButton = findViewById(R.id.start);
        bottomButton.setOnClickListener(this);

        heightMeasureScrollview = findViewById(R.id.height_measure_scrollview);
        setUserHeight = findViewById(R.id.set_user_height);
        heightMeasureTarget = findViewById(R.id.height_measure_target);
        heightMeasureResult = findViewById(R.id.height_measure_result);

        commonBottom = findViewById(R.id.common_bottom);

        firstNumber = findViewById(R.id.first_number);
        firstNumber.setDisplayedValues(data);
        firstNumber.setMinValue(0);
        firstNumber.setMaxValue(data.length - 1);
        firstNumber.setValue(userHeightArray[0]);
        //firstNumber.setOnScrollListener(this);
        firstNumber.setOnValueChangedListener(this);


        secondNumber = findViewById(R.id.second_number);
        secondNumber.setDisplayedValues(date1);
        secondNumber.setMinValue(0);
        secondNumber.setMaxValue(date1.length - 1);
        secondNumber.setValue(userHeightArray[1]);
        //secondNumber.setOnScrollListener(this);
        secondNumber.setOnValueChangedListener(this);
        
        larstNumber = findViewById(R.id.last_number);
        larstNumber.setDisplayedValues(date1);
        larstNumber.setMinValue(0);
        larstNumber.setMaxValue(date1.length - 1);
        larstNumber.setValue(userHeightArray[2]);
        //larstNumber.setOnScrollListener(this);
        larstNumber.setOnValueChangedListener(this);

        targetImageView = findViewById(R.id.target_image_view);
        targetImageView.setOnClickListener(this);


        userHeightTextView = findViewById(R.id.height);
        angleOfDepressionTextView = findViewById(R.id.angle_of_elevation);
        angleOfElevationTextView = findViewById(R.id.angle_of_depression);
        targetDistanceTextView = findViewById(R.id.target_distance);
        targetHeightTextView = findViewById(R.id.target_height);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> list = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        for (Sensor sensor:list) {
            Log.e(TAG,"sensor = " + sensor.toString());
        }
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activateSensor();
        calculateOrientation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deactivateSensor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deactivateSensor();
    }

    final SensorEventListener myListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = sensorEvent.values;
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = sensorEvent.values;
            }
            calculateOrientation();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_button:
                finish();
                break;
            case R.id.start:
                if (currentView == HEIGHT_MEASURE_RESULT_VIEW) {
                    bottomButton.setText(R.string.start_measure);
                    angleOfElevation = 0;
                    angleOfDepression = 0;
                    targetImageView.setBackgroundResource(R.drawable.height_measure_second_step_view);
                    heightMeasureScrollview.setVisibility(View.VISIBLE);
                    heightMeasureResult.setVisibility(View.GONE);
                    currentView = HEIGHT_MEASURE_VIEW;
                } else {
                    doNext();
                }
                break;
            case R.id.target_image_view:
                Log.e(TAG,"onclick target_image_view");
                //delete by wangjian for EWSY-525 20181218 start
                //if (Math.abs(Math.abs(orientationValues[2]) - 90) >= 10) {
                //    Toast.makeText(this,R.string.measure_tips,Toast.LENGTH_SHORT).show();
                //    break;
                //}
                ////delete by wangjian for EWSY-525 20181218 end
                if (angleOfDepression == 0) {
                    angleOfDepression = orientationValues[1];
                    if (angleOfDepression >= 0) {
                        Toast.makeText(this,R.string.measure_tips,Toast.LENGTH_SHORT).show();
                        angleOfDepression = 0;
                    } else {
                        angleOfDepression = Math.abs(angleOfDepression);
                        targetImageView.setBackgroundResource(R.drawable.height_measure_last_step_view);
                    }
                } else {
                    angleOfElevation = orientationValues[1];
                    if (angleOfElevation <= 0) {
                        Toast.makeText(this,R.string.measure_tips,Toast.LENGTH_SHORT).show();
                        angleOfElevation = 0;
                    } else {
                        angleOfElevation = Math.abs(angleOfElevation);
                        calculateTargetHeight();
                        doNext();
                    }
                }
                break;
        }
    }

    @Override
    public int getRequestedOrientation() {
        return super.getRequestedOrientation();
    }

    @Override
    public void onBackPressed() {
        doPre();
    }

    private void doPre() {
        switch (currentView) {
            case HEIGHT_MEASURE_VIEW:
                finish();
                break;
            case SET_USER_HEIGHT_VIEW:
                bottomButton.setText(R.string.start_measure);
                heightMeasureScrollview.setVisibility(View.VISIBLE);
                setUserHeight.setVisibility(View.GONE);
                currentView = HEIGHT_MEASURE_VIEW;
                break;

            case HEIGHT_MEASURE_TARGET_VIEW:
                if (angleOfElevation != 0) {
                    targetImageView.setBackgroundResource(R.drawable.height_measure_second_step_view);
                    angleOfElevation = 0;
                } else {
                    showPotraitScreen();
                    angleOfDepression = 0;
                    setUserHeight.setVisibility(View.VISIBLE);
                    heightMeasureTarget.setVisibility(View.GONE);
                    bottomButton.setText(R.string.next);
                    currentView = SET_USER_HEIGHT_VIEW;
                }
                break;
            case HEIGHT_MEASURE_RESULT_VIEW:
                showFullLandScreen();
                heightMeasureResult.setVisibility(View.GONE);
                heightMeasureTarget.setVisibility(View.VISIBLE);
                currentView = HEIGHT_MEASURE_TARGET_VIEW;
                break;
        }
    }

    private void doNext() {
        Log.e(TAG,"doNext currentView = " + currentView);
        commonBottom.setVisibility(View.VISIBLE);
        switch (currentView) {
            case HEIGHT_MEASURE_VIEW:
                bottomButton.setText(R.string.next);
                heightMeasureScrollview.setVisibility(View.GONE);
                setUserHeight.setVisibility(View.VISIBLE);
                currentView = SET_USER_HEIGHT_VIEW;
                break;
            case SET_USER_HEIGHT_VIEW:
                angleOfDepression = 0;
                angleOfElevation = 0;
                showFullLandScreen();
                setUserHeight.setVisibility(View.GONE);
                heightMeasureTarget.setVisibility(View.VISIBLE);
                currentView = HEIGHT_MEASURE_TARGET_VIEW;
                break;
            case HEIGHT_MEASURE_TARGET_VIEW:
                showPotraitScreen();
                heightMeasureTarget.setVisibility(View.GONE);
                heightMeasureResult.setVisibility(View.VISIBLE);
                bottomButton.setText(R.string.remeasure);
                currentView = HEIGHT_MEASURE_RESULT_VIEW;
                break;
            case HEIGHT_MEASURE_RESULT_VIEW:
                break;
        }
    }


    /*@Override
    public void onScrollStateChange(NumberPicker numberPicker, int scrollState) {
        Log.i(TAG,"onScrollStateChange id = " + numberPicker.getId() + " / scrollState = " + scrollState );
        switch (scrollState) {
            case NumberPicker.OnScrollListener.SCROLL_STATE_FLING:
                Log.i(TAG, "SCROLL_STATE_FLING");
                //惯性滑动
                break;
            case NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                //手动滑动
                Log.i(TAG, "SCROLL_STATE_TOUCH_SCROLL");
                break;
            case NumberPicker.OnScrollListener.SCROLL_STATE_IDLE:
                //停止滑动
                Log.i(TAG, "SCROLL_STATE_IDLE");
                break;
        }
    }*/

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        Log.i(TAG,"onValueChange id = " + numberPicker.getId() + " / oldVal = " + oldVal + " / newVal = " + newVal);
        switch (numberPicker.getId()) {
            case R.id.first_number:
                userHeightArray[0] = newVal;
                break;
            case R.id.second_number:
                userHeightArray[1] = newVal;
                break;
            case R.id.last_number:
                userHeightArray[2] = newVal;
                break;
        }
        userHeight = userHeightArray[0] * 100 + userHeightArray[1] * 10 + userHeightArray[2];
        Log.i(TAG,"onValueChange userHeight = " + userHeight);
    }

    private void showFullLandScreen() {
        mActionBar.hide();
        getWindow().getDecorView().
                setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        commonBottom.setVisibility(View.GONE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    private void showPotraitScreen() {
        mActionBar.show();
        getWindow().getDecorView().
                setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        commonBottom.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private  void calculateOrientation() {
        float[] R = new float[16];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, orientationValues);
        // 要经过一次数据格式的转换，转换为度
        orientationValues[0] = (float) Math.toDegrees(orientationValues[0]);
        orientationValues[1] = (float) Math.toDegrees(orientationValues[1]);
        orientationValues[2] = (float) Math.toDegrees(orientationValues[2]);
        Log.e(TAG, orientationValues[0] + "   " + orientationValues[1] + "   " + orientationValues[2]);
    }


    public void activateSensor() {
        // Get events from the accelerometer and magnetic sensor.
        if (mAccelerometerSensor != null) {
            mSensorManager.registerListener(myListener, mAccelerometerSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mMagneticSensor != null) {
            mSensorManager.registerListener(myListener, mMagneticSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void deactivateSensor() {
        // Unregister the sensors.
        if (mAccelerometerSensor != null) {
            mSensorManager.unregisterListener(myListener, mAccelerometerSensor);
        }
        if (mMagneticSensor != null) {
            mSensorManager.unregisterListener(myListener, mMagneticSensor);
        }
    }

    private void calculateTargetHeight() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        userHeightTextView.setText(userHeight + "cm");
        angleOfElevationTextView.setText(decimalFormat.format(angleOfElevation) + "°");
        angleOfDepressionTextView.setText(decimalFormat.format(angleOfDepression) + "°");
        double targetDistance = (userHeight - 12) / Math.tan(angleOfDepression * Math.PI / 180);
        double targetHeight = targetDistance * Math.tan(angleOfElevation * Math.PI / 180) + (userHeight - 12);
        targetDistanceTextView.setText(decimalFormat.format(targetDistance / 100) + "m");
        targetHeightTextView.setText(decimalFormat.format(targetHeight / 100) + "m");
        Log.e("wangjian" , "targetDistance = " + targetDistance + " / targetHeight = " + targetHeight + " / angleOfDepression = " + angleOfDepression + " / angleOfElevation = " + angleOfElevation);
    }

}
