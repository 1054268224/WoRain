package com.odm.tool.gradienter;

import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.odm.tool.R;

/**
 * Created by Hades on 16/10/8.
 */
public class GradienterActivity extends Activity implements SensorEventListener,View.OnClickListener{
    //定义水平仪的仪表盘
    private SpiritView show;
    //定义水平仪能处理的最大倾斜角度，超过该角度气泡直接位于边界
    private int MAX_ANGLE = 30;
    //定义Sensor管理器
    SensorManager sensorManager;

    private ImageView backImg;
    private ImageView caliImg;
    private TextView tv_vertical;
    private TextView tv_horizontal;
    //最小稳定时间
    private static long STABLE_MIN_TIME = 1500;
    private long mStableTime;

    // Add by zhiheng.huang on 2020/8/13 for  start
    private AlertDialog mHelpDialog;
    private boolean mNeedCalibrate = true;
    // Add by zhiheng.huang on 2020/8/13 for  end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gradienter);
        //获取水平仪的主组件
        show = (SpiritView) findViewById(R.id.show);
        backImg = (ImageView) findViewById(R.id.spirit_back);
        backImg.setOnClickListener(this);
        caliImg = (ImageView) findViewById(R.id.spirit_cali);
        caliImg.setOnClickListener(this);
        tv_vertical = (TextView) findViewById(R.id.vertical_value);
        tv_horizontal = (TextView) findViewById(R.id.horizontal_value);
        //获取传感器
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float values[] = sensorEvent.values;
        //获取传感器的类型
        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ORIENTATION:
                //获取与Y轴的夹角
                float yAngle = values[1];
                //获取与Z轴的夹角
                float zAngle = values[2];
                //气泡位于中间时（水平仪完全水平）
                //add by wuzhi.peng for TQQB-226 start
                float[] mStatbleValues = getStableValue(yAngle,zAngle);
                yAngle = mStatbleValues[0];
                zAngle = -mStatbleValues[1];
                //add by wuzhi.peng for TQQB-226 end
                int centerX = (show.back.getWidth() - show.bubble.getWidth()) / 2;
                int centerY = (show.back.getHeight() - show.bubble.getHeight()) / 2;
                int x = centerX;
                int y = centerY;
                //如果与Z轴的倾斜角还在最大角度之内
                if (Math.abs(zAngle) <= MAX_ANGLE) {
                    //根据与Z轴的倾斜角度计算X坐标轴的变化值
                    int deltaX = (int) ((show.back.getWidth() - show.bubble.getWidth()) / 2
                            * zAngle / MAX_ANGLE);
                    x += deltaX;
                }
                //如果与Z轴的倾斜角已经大于MAX_ANGLE，气泡应到最左边
                else if (zAngle > MAX_ANGLE) {
                   // x = 0;
                    x = show.back.getWidth() - show.bubble.getWidth();
                }
                //如果与Z轴的倾斜角已经小于负的Max_ANGLE,气泡应到最右边
                else {
                 //   x = show.back.getWidth() - show.bubble.getWidth();
                    x = 0;
                }

                //如果与Y轴的倾斜角还在最大角度之内
                if (Math.abs(yAngle) <= MAX_ANGLE) {
                    //根据与Z轴的倾斜角度计算X坐标轴的变化值
                    //modify by wuzhi.peng
                    int deltaY = (int) ((show.back.getWidth() - show.bubble.getHeight()) / 2
                            * yAngle / MAX_ANGLE);
                    y += deltaY;
                }
                //如果与Y轴的倾斜角已经大于MAX_ANGLE，气泡应到最下边
                else if (yAngle > MAX_ANGLE) {
                    y = (show.back.getHeight()+show.back.getWidth())/2 - show.bubble.getHeight();
                }
                //如果与Y轴的倾斜角已经小于负的Max_ANGLE,气泡应到最上边
                else {
                    y = (show.back.getHeight()-show.back.getWidth())/2;
                }
                //如果计算出来的X，Y坐标还位于水平仪的仪表盘之内，则更新水平仪气泡坐标


                if (true) {
                    show.bubbleX = x;
                    show.bubbleY = y;
                    //Toast.makeText(Spirit.this, "在仪表盘内", Toast.LENGTH_SHORT).show();
                }
                updateOffsetValues(zAngle,yAngle);
                //通知组件更新
                    show.postInvalidate();

                break;
        }
    }
    //add by wuzhi.peng for TQQB-226 start
    private float lastX,latsZ;
    private float[] stableValues = new float[2];
    private float[] getStableValue(float xValue,float zValue){
        if(Math.abs(xValue - lastX)>=5 || Math.abs(zValue - latsZ)>=5){
            lastX =xValue;
            latsZ = zValue;
            mStableTime  = System.currentTimeMillis();
            stableValues[0] = xValue;
            stableValues[1] = zValue;
        } else {
            if (Math.abs(System.currentTimeMillis() - mStableTime) > STABLE_MIN_TIME){
                xValue = xValue/2;
                xValue = Math.round(xValue);
                xValue = xValue*2;
                zValue = zValue/2;
                zValue = Math.round(zValue);
                zValue = zValue*2;
                stableValues[0] = xValue;
                stableValues[1] = zValue;
            } else {
                stableValues[0] = xValue;
                stableValues[1] = zValue;
            }
        }

        return stableValues;
    }
//add by wuzhi.peng for TQQB-226 end
    private void updateOffsetValues(float horizontal,float vertical){
        tv_vertical.setText(Math.round(vertical)+"°");
        tv_horizontal.setText(Math.round(horizontal)+"°");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Add by zhiheng.huang on 2020/8/13 for  start
        if (i >= SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            Log.d("HZH", "onAccuracyChanged: high");
            mNeedCalibrate = false;
            if (mHelpDialog != null && mHelpDialog.isShowing()) {
                mHelpDialog.dismiss();
                Toast.makeText(this, R.string.calibrate_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("HZH", "onAccuracyChanged: need calibrate");
            mNeedCalibrate = true;
        }
        // Add by zhiheng.huang on 2020/8/13 for  end
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onStop() {
        //取消注册
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        //取消注册
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.spirit_back:
                finish();
                break;
            case R.id.spirit_cali:
                if (mNeedCalibrate) {
                    if (mHelpDialog == null) {
                        mHelpDialog = new AlertDialog.Builder(this)
                                .setTitle(R.string.calibrate_title)
                                .setMessage(R.string.calibrate_message)
                                .setCancelable(false)
                                .create();
                    }
                    mHelpDialog.show();
                } else {
                    Toast.makeText(this, R.string.no_need_to_calibrate, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}