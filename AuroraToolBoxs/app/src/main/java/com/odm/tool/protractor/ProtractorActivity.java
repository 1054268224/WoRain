package com.odm.tool.protractor;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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


public class ProtractorActivity extends Activity {

    private static final int REQUEST_CAMERA_PERMISSIONS = 1;
    private static boolean isCameraPermissionGrant = false;

    private CameraHelper mCameraHelper;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protractor);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
            //modify bug:TELYL-234 wushanfei 20200407 start
            if (grantResults != null && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //modify bug:TELYL-234 wushanfei 20200407 end
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

    private void initUI() {
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_view);
        mCameraHelper = new CameraHelper(this, mSurfaceView, null, null);
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
