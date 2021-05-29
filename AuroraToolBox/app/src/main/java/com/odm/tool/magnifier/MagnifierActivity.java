package com.odm.tool.magnifier;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.odm.tool.R;
import com.odm.tool.util.CameraHelper;


public class MagnifierActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_CAMERA_PERMISSIONS = 1;
    private static boolean isCameraPermissionGrant = false;
    private CameraHelper mCameraHelper;

    private float mZoom = 2f;
    private String mCameraId = CameraHelper.CAMERA_BACK; // Default back camera

    private ImageView mCameraSwitchView;
    private ImageView mCameraFlashView;
    private ImageView mZoomOutView;
    private ImageView mZoomView;
    private SeekBar mSeekBarView;
    private LinearLayout mSeekbarlayout;
    private boolean needReopen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        // D Bug:EJQQQ-709 wushanfei 20200319 {
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                // WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        // D: }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_LAYOUT_FLAGS
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnifier);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSIONS);
            isCameraPermissionGrant = false;
        } else {
            isCameraPermissionGrant = true;
            initUI();
        }
        needReopen = false;
        //A:BUG_ID:YWSW-1905 tiankun 20191223 {
        mHandler.sendEmptyMessage(1);
        //A: }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCameraPermissionGrant) {
            if (needReopen) {
                //M: hongqian EJQQ-560 20190927 start
                //mCameraHelper.openCamera();
                mCameraHelper.openCamera(mCameraId);
                //M: hongqian EJQQ-560 20190927 end
            }
            mCameraHelper.startBackgroundThread();
        }
    }

    @Override
    public void onPause() {
        if (isCameraPermissionGrant) {
            mCameraHelper.closeCamera();
            mCameraHelper.stopBackgroundThread();
        }
        needReopen = true;
        super.onPause();
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
                mCameraHelper.startBackgroundThread();
                mCameraHelper.openCamera(mCameraId);
            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ic_camera_switch:
                switchCamera();
                break;
            case R.id.ic_camera_flash:
                //A:BUG_ID:YWSW-1905 tiankun 20191223 {
                if (isLowBattery()) {
                    switchFlashMode();
                }else {
                    showLowBatteryHintInfo();
                }
                //A: }
                break;
            case R.id.ic_seekbar_down:
                if (mZoom > 2) {
                    mZoom = mZoom - 1f;
                    setZoom(mZoom);
                }
                break;
            case R.id.ic_seekbar_up:
                if(mZoom < 7){
                    mZoom = mZoom + 1f;
                    setZoom(mZoom);
                }
                break;
        }
    }

    private void initUI() {
        TextureView surfaceView = (TextureView) findViewById(R.id.camera_view);
        // D Bug:EJQQQ-709 wushanfei 20200319 {
        // A: Bug_id:EWSY-582 chenchunyong 20190102 {
        /*int flag = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        surfaceView.setSystemUiVisibility(flag);*/
        // A: }
        // D: }
        mCameraHelper = new CameraHelper(this, null, surfaceView, mCameraHelperCallback);

        mCameraSwitchView = findViewById(R.id.ic_camera_switch);
        mCameraFlashView = findViewById(R.id.ic_camera_flash);
        mZoomOutView = findViewById(R.id.ic_seekbar_down);
        mZoomView = findViewById(R.id.ic_seekbar_up);
        ImageView back = findViewById(R.id.back);
        mSeekbarlayout = findViewById(R.id.seekbar_layout);

        mCameraSwitchView.setOnClickListener(this);
        mCameraFlashView.setOnClickListener(this);
        mZoomOutView.setOnClickListener(this);
        mZoomView.setOnClickListener(this);
        back.setOnClickListener(this);

        mSeekBarView = findViewById(R.id.ic_seekbar);
        mSeekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mZoom = seekBar.getProgress()/20 + 2;
                setZoom(mZoom);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mZoom = seekBar.getProgress()/20 + 2;
                setZoom(mZoom);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mZoom = seekBar.getProgress()/20 + 2;
                setZoom(mZoom);
            }
        });
    }

    private void refreshUI(){
        if(mSeekbarlayout != null){
            mSeekbarlayout.setVisibility(View.VISIBLE);
        }
        if (mCameraHelper.getCameraCharacteristics(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
            mCameraFlashView.setVisibility(View.VISIBLE);
        } else {
            mCameraFlashView.setVisibility(View.GONE);
        }

        if (mCameraHelper.getPreviewBuilder(CaptureRequest.FLASH_MODE) == CaptureRequest.FLASH_MODE_OFF) {
            mCameraFlashView.setImageResource(R.drawable.ic_flash_img_close_icon);
        } else {
            mCameraFlashView.setImageResource(R.drawable.ic_flash_img_on_icon);
        }

        if (mCameraHelper.getNumberOfCameras() > 1) {
            mCameraSwitchView.setVisibility(View.VISIBLE);
            mCameraSwitchView.setEnabled(true);
        }

        mZoom = 2;
        mSeekBarView.setProgress((int)mZoom);
    }

    private void switchFlashMode() {
        if (mCameraHelper.getPreviewBuilder(CaptureRequest.FLASH_MODE) == CaptureRequest.FLASH_MODE_OFF) {
            mCameraHelper.setPreviewBuilder(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            mCameraFlashView.setImageResource(R.drawable.ic_flash_img_on_icon);
        } else {
            mCameraHelper.setPreviewBuilder(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            mCameraFlashView.setImageResource(R.drawable.ic_flash_img_close_icon);
        }
    }

    private void switchCamera() {
        if (mCameraId.equals(CameraHelper.CAMERA_FRONT)) {
            mCameraId = CameraHelper.CAMERA_BACK;
            mCameraHelper.closeCamera();
            reopenCamera();

        } else if (mCameraId.equals(CameraHelper.CAMERA_BACK)) {
            mCameraId = mCameraHelper.CAMERA_FRONT;
            mCameraHelper.closeCamera();
            reopenCamera();
        }
    }

    private void reopenCamera() {
        mCameraHelper.openCamera(mCameraId);
        mCameraSwitchView.setEnabled(false);
    }

    private void setZoom(float ratio){
        Rect mSensorRect = mCameraHelper.getCameraCharacteristics(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        int radio2 = mCameraHelper.getCameraCharacteristics(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue() / 3;
        int realRadio2 = mCameraHelper.getCameraCharacteristics(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
        int centerX2 = mSensorRect.centerX();
        int centerY2 = mSensorRect.centerY();
        int minMidth2 = (int) (mSensorRect.right - ((ratio * centerX2) / 5 / radio2) - 1);
        int minHeight2 = (int) (mSensorRect.bottom - ((ratio * centerY2) / 5 / radio2) - 1);
        if (minMidth2 < mSensorRect.right / realRadio2 || minHeight2 < mSensorRect.bottom / realRadio2) {
            Log.i("sb_zoom", "sb_zoomsb_zoomsb_zoom");
            return;
        }
        Rect newRect2 = new Rect(0, 0, minMidth2, minHeight2);
        // A: Bug_id:TEWBW-852 chenchunyong 20200114 {
        int xCenter = mSensorRect.width() / 2;
        int yCenter = mSensorRect.height() / 2;
        int xDelta = (int) (mSensorRect.width() / ratio);
        int yDelta = (int) (mSensorRect.height() / ratio);
        newRect2 = new Rect(xCenter - xDelta, yCenter - yDelta, xCenter + xDelta, yCenter + yDelta);
        // A: }
        
        mSeekBarView.setProgress((int)((ratio - 2f) * 20f));

        mCameraHelper.setPreviewBuilder(CaptureRequest.SCALER_CROP_REGION, newRect2);
    }

    private CameraHelper.Callback mCameraHelperCallback = new CameraHelper.Callback() {
        @Override
        public void refreshUI() {
            mHandler.sendEmptyMessage(0000);
        }
    };
    //M:BUG_ID:YWSW-1905 tiankun 20191223 {
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           switch (msg.what){
               case 0:
                   refreshUI();
                   break;
               case 1:
                   //modify bug:TELYL-234 wushanfei 20200407 start
                   //if (! isLowBattery()){
                   if (!isLowBattery() && mCameraHelper != null && mCameraFlashView != null){
                   //modify bug:TELYL-234 wushanfei 20200407 end
                       //if (mCameraHelper.getPreviewBuilder(CaptureRequest.FLASH_MODE) != CaptureRequest.FLASH_MODE_OFF) {
                           mCameraHelper.setPreviewBuilder(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                           mCameraFlashView.setImageResource(R.drawable.ic_flash_img_close_icon);
                       //}
                   }
                   mHandler.sendEmptyMessageDelayed(1,1500);
                   break;
           }
        }
    };
    //M: }

    //A:BUG_ID:YWSW-1905 tiankun 20191223 {
    public boolean isLowBattery(){
        BatteryManager mBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        int battery = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if (battery <= 15){
            return false;
        }
        return true;
    }
    public void showLowBatteryHintInfo(){
        Toast mToast = Toast.makeText(this, R.string.low_battery,Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER,0,0);
        mToast.show();
    }
    //A: }
}
