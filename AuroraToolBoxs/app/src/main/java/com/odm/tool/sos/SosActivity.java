package com.odm.tool.sos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.odm.tool.R;

/**
 * @author zhiheng.huang
 */
public class SosActivity extends Activity {

    private final int FLASH_TIME = 300;
    private final int TORCH_TIME = 500;

    private ImageView mRing;
    private ImageView mScreenFlash;
    private View mBg;
    private MediaPlayer mmp;
    private int[] colors = new int[]{Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN};
    private int currColor;
    private Handler mFlashHandler;
    private Runnable screenFlashRunnable;
    private ImageView mTorch;
    private CameraManager mCameraManager;
    private Handler mTorchHandler;
    private Runnable mTorchRunnable;
    private boolean isTorchOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        mRing = findViewById(R.id.ring);
        mScreenFlash = findViewById(R.id.screen_flash);
        mTorch = findViewById(R.id.torch);
        mBg = findViewById(R.id.ll_bg);
        View back = findViewById(R.id.iv_back);

        back.setOnClickListener(v -> finish());
        mRing.setOnClickListener(v -> playOrStopSosRing());
        mScreenFlash.setOnClickListener(v -> playOrStopScreenFlash());
        mTorch.setOnClickListener(v -> playOrStopTorch());

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFlashHandler != null) {
            mFlashHandler.removeCallbacks(screenFlashRunnable);
            mFlashHandler = null;
            mBg.setBackgroundColor(Color.WHITE);
            mScreenFlash.setImageResource(R.mipmap.ic_screen_flash);
        }

        if (mmp != null && mmp.isPlaying()) {
            mmp.stop();
            mmp.release();
            mmp = null;
        }

        if (mTorchHandler != null) {
            mTorchHandler.removeCallbacks(mTorchRunnable);
            mTorchHandler = null;
        }
    }

    private void playOrStopScreenFlash() {
        if (mFlashHandler != null) {
            mFlashHandler.removeCallbacks(screenFlashRunnable);
            mFlashHandler = null;
            mBg.setBackgroundColor(Color.WHITE);
            mScreenFlash.setImageResource(R.mipmap.ic_screen_flash);
        } else {
            mFlashHandler = new Handler();
            screenFlashRunnable = new Runnable() {
                @Override
                public void run() {
                    mBg.setBackgroundColor(colors[currColor % colors.length]);
                    currColor++;
                    mFlashHandler.postDelayed(this, FLASH_TIME);
                    mScreenFlash.setImageResource(R.mipmap.sf_pressed);
                }
            };
            mFlashHandler.post(screenFlashRunnable);
        }
    }

    private void playOrStopSosRing() {
        if (mmp != null && mmp.isPlaying()) {
            mmp.stop();
            mmp.release();
            mmp = null;
            mRing.setImageResource(R.mipmap.warning);
        } else {
            mmp = MediaPlayer.create(this, R.raw.warning_ring);
            mmp.setLooping(true);
            mmp.start();
            mRing.setImageResource(R.mipmap.warning_pressed);
        }
    }

    private void playOrStopTorch() {
        if (mTorchHandler != null) {
            lightTorch(false);
            mTorchHandler.removeCallbacks(mTorchRunnable);
            mTorchHandler = null;
            mTorch.setImageResource(R.mipmap.torch);
        } else {
            if (isLowPower()) {
                Toast.makeText(this, getString(R.string.low_battery), Toast.LENGTH_SHORT).show();
                return;
            }
            mTorchHandler = new Handler();
            mTorchRunnable = () -> {
                lightTorch(!isTorchOn);
                mTorch.setImageResource(isTorchOn ? R.mipmap.torch_pressed : R.mipmap.torch);
                if (mTorchHandler != null) {
                    mTorchHandler.postDelayed(mTorchRunnable, TORCH_TIME);
                }
            };

            mTorchHandler.post(mTorchRunnable);
        }
    }

    private void lightTorch(boolean b) {
        try {
            mCameraManager.setTorchMode("0", b);
            isTorchOn = b;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            if (mTorchHandler != null && mTorchRunnable != null) {
                mTorchHandler.removeCallbacks(mTorchRunnable);
                mTorchHandler = null;
            }
        }
    }

    // Add by HZH on 2019/9/26 for  start
    private BatteryManager mBatteryManager;

    private boolean isLowPower() {
        if (mBatteryManager == null) {
            mBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        }
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) <= 15;
    }
    // Add by HZH on 2019/9/26 for  end
}
