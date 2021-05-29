package com.odm.tool.flashlight;

import java.util.Arrays;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.hardware.camera2.CameraManager;
import android.widget.Toast;
import com.odm.tool.R;

public class FlashlightActivity extends Activity implements OnClickListener {

    private static final String TAG = "FlashlightActivity";

    private static final String KEY_FLASH_LIGHT_STATE = "flash_light_state";
    private static final String KEY_SOS_STATE = "sos_state";
    private static final int FLASH_MODE_ON = 1;
    private static final int FLASH_MODE_OFF = 0;
    private String mCameraId = "0";

    private RelativeLayout mRelativeLayout;
    private ImageView mTorch_off;
    private ImageView mTorch_on;
    private ImageView mTorch_disable;
    private ImageView mSOS_on;
    private ImageView mSOS_off;
    private ImageView mSOS_disable;
    private ImageView mIvLight;

    private static boolean mSOS_status;
    private boolean mBatterySaveChange;
    private static final int REQUEST_CAMERA = 1;

    private static CameraManager mCameraManager;
    private static Context mContext;

    private boolean mPermissionCamera = false;

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (getFlashlightState()) {
                mCameraManager.registerTorchCallback(mTorchCallback, mHandler);
            } else {
                mCameraManager.unregisterTorchCallback(mTorchCallback);
            }
            refreshViewState(getFlashlightState(), getSOSState());
        }
    };

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (mCameraId == null) {
                return;
            }
            try {
                mCameraManager.setTorchMode(mCameraId, msg.what == FLASH_MODE_ON ? true : false);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Couldn't set torch mode", e);
            }
        };
    };
    private int mLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateContinue();
    }

    private void onCreateContinue() {
        setContentView(R.layout.activity_flashlight);
        init();
    }

    private void init() {
        mContext = this;
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout_for_background);
        mTorch_off = (ImageView) findViewById(R.id.torch_off);
        mTorch_on = (ImageView) findViewById(R.id.torch_on);
        mTorch_disable = (ImageView) findViewById(R.id.torch_disable);

        mSOS_on = (ImageView) findViewById(R.id.sos_on);
        mSOS_off = (ImageView) findViewById(R.id.sos_off);
        mSOS_disable = (ImageView) findViewById(R.id.sos_disable);

        mIvLight = findViewById(R.id.iv_light);

        if (!getResources().getBoolean(R.bool.is_SOS_support)) {
            mSOS_off.setVisibility(View.GONE);
        }

        mTorch_off.setOnClickListener(this);
        mTorch_on.setOnClickListener(this);

        mSOS_off.setOnClickListener(this);
        mSOS_on.setOnClickListener(this);
        // A: Bug_id:XLYLY-1218 chenchunyong 20171227 {
        Settings.Global.putInt(getContentResolver(), KEY_SOS_STATE, mSOS_status ? 1 : 0);
        // A: }
        Uri uri = Settings.Global.getUriFor(KEY_FLASH_LIGHT_STATE);
        getContentResolver().registerContentObserver(uri, true, mObserver);

        mLimit = mContext.getResources().getInteger(R.integer.disallow_open_light_limit);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
                if (getSOSState() && mSOS_status) {
                    mBatterySaveChange = true;
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            mPermissionCamera = false;
        } else {
            mPermissionCamera = true;
            try {
                mCameraId = getCameraId();
            } catch (Throwable e) {
                Log.e(TAG, "Couldn't initialize.", e);
            }
            onResumeContinue();
        }
    }

    private void onResumeContinue() {
        refreshViewState(getFlashlightState(), getSOSState());
    }

    private void refreshViewState(boolean isFlashlightState, boolean isSOSState) {

        mTorch_on.setVisibility(isFlashlightState || isSOSState ? View.VISIBLE : View.GONE);
        mTorch_off.setVisibility((!isFlashlightState && !isSOSState) ? View.VISIBLE : View.GONE);

        if (getResources().getBoolean(R.bool.is_SOS_support)) {
            mSOS_on.setVisibility(isSOSState ? View.VISIBLE : View.GONE);
            mSOS_off.setVisibility(!isSOSState ? View.VISIBLE : View.GONE);
            mSOS_disable.setVisibility(isFlashlightState ? View.VISIBLE : View.GONE);
        }

        mRelativeLayout.setBackgroundColor(Color.parseColor("#24262C"));

        // Add by HZH on 2019/5/25 for EJSL-1422 start ^_^
        mIvLight.setBackgroundResource(isFlashlightState || isSOSState ? R.drawable.view_light_on_new : R.drawable.view_light_off_new);
        // Add by HZH on 2019/5/25 for EJSL-1422 end ^_^
    }

    private boolean getFlashlightState() {
        return Settings.Global.getInt(getContentResolver(), KEY_FLASH_LIGHT_STATE, 0) == 1;
    }

    private boolean getSOSState() {
        return Settings.Global.getInt(getContentResolver(), KEY_SOS_STATE, 0) == 1;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getSOSState() && !mBatterySaveChange) {
            setSOStatus(false);
            Settings.Global.putInt(getContentResolver(), KEY_FLASH_LIGHT_STATE, 0);
        }
        getContentResolver().unregisterContentObserver(mObserver);
        // Add by HZH on 2019/5/25 for EJSL-1424 start ^_^
        unregisterReceiver(mReceiver);
        if (mSp != null) {
            mSp.unload(mSoundID);
            mSp.release();
        }
        // Add by HZH on 2019/5/25 for EJSL-1424 end ^_^
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.torch_on:
                Settings.Global.putInt(getContentResolver(), KEY_FLASH_LIGHT_STATE, 0);
                mIvLight.setBackgroundResource(R.drawable.view_light_off_new);
                // Add by HZH on 2019/5/25 for EJSL-1425 EJSL-1423 start ^_^
                if (mSOS_status) {
                    setSOStatus(false);
                }
                // Add by HZH on 2019/5/25 for EJSL-1425 EJSL-1423  end ^_^
                setFlashStatus(false);
                break;
            case R.id.torch_off:
                if (isLowPower()) {
                    Toast.makeText(mContext,mContext.getString(R.string.low_battery),Toast.LENGTH_SHORT).show();
                    return;
                }
                Settings.Global.putInt(getContentResolver(), KEY_FLASH_LIGHT_STATE, 1);
                mIvLight.setBackgroundResource(R.drawable.view_light_on_new);
                setFlashStatus(true);
                break;
            case R.id.sos_on:
                setSOStatus(false);
                // Add by HZH on 2019/7/2 for EJWJ-240 start
                Settings.Global.putInt(getContentResolver(), KEY_FLASH_LIGHT_STATE, 0);
                // Add by HZH on 2019/7/2 for EJWJ-240 end
                break;
            case R.id.sos_off:
                if (isLowPower()) {
                    Toast.makeText(mContext,mContext.getString(R.string.low_battery),Toast.LENGTH_SHORT).show();
                    return;
                }
                setSOStatus(true);
                break;
            default:
                break;
        }
        // Add by HZH on 2019/5/25 for EJSL-1424 start ^_^
        playSoundEffect();
        // Add by HZH on 2019/5/25 for EJSL-1424 end ^_^ 
    }

    private void setSOStatus(boolean isOpen) {
        mSOS_status = isOpen;
        Settings.Global.putInt(getContentResolver(), KEY_SOS_STATE, isOpen ? 1 : 0);
        refreshViewState(getFlashlightState(), getSOSState());

        if (isOpen) {
            new SOSThread().start();
        }
    }

    private class SOSThread extends Thread {
        @Override
        public void run() {
            int count;

            if (mCameraId != null) {
                count = 3;
            } else {
                return;
            }

            while (mSOS_status) {
                if (count == -3) {
                    count = 3;
                }
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(FLASH_MODE_ON);
                try {
                    for (int i = 0; i < (count > 0 ? (count == 1 ? 8 : 3) : 8); i++) {
                        Thread.sleep(100);
                        if (!mSOS_status) {
                            break;
                        }
                    }
                    // 111000111000...
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(FLASH_MODE_OFF);
                count--;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (grantResults == null || grantResults.length <= 0) {
            return;
        }

        if (requestCode == REQUEST_CAMERA) {
            Log.d(TAG, "onRequestPermissionsResult " + Arrays.toString(permissions));

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionCamera = true;
                onCreateContinue();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
//                Toast.makeText(this, getString(R.string.denied_required_permission), Toast.LENGTH_SHORT).show();
            } else {
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        }
    }

    private final static CameraManager.TorchCallback mTorchCallback =
            new CameraManager.TorchCallback() {

                @Override
                public void onTorchModeUnavailable(String cameraId) {
                    Settings.Global.putInt(mContext.getContentResolver(), KEY_FLASH_LIGHT_STATE, 0);
                }

                @Override
                public void onTorchModeChanged(String cameraId, boolean enabled) {
                }
            };

    private String getCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    // Add by HZH on 2019/5/25 for EJSL-1424 start ^_^
    private SoundPool mSp;
    private int mSoundID;

    public void playSoundEffect() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float ringVolume = am.getStreamVolume(AudioManager.STREAM_RING);
        float volumeRatio = ringVolume == 0 ? 0 : audioCurrentVolume / audioMaxVolume;
        if (mSp != null) {
            mSp.play(mSoundID, volumeRatio, volumeRatio, 0, 0, 1);
        } else {
            mSp = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build())
                    .build();
            mSoundID = mSp.load(this, R.raw.sound, 0);
            mSp.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                mSp.play(mSoundID, volumeRatio, volumeRatio, 0, 0, 1);
            });
        }
    }
    // Add by HZH on 2019/5/25 for EJSL-1424 end ^_^

    // Add by HZH on 2019/9/26 for  start
    private BatteryManager mBatteryManager;

    private boolean isLowPower() {
        if (mBatteryManager == null) {
            mBatteryManager = (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);
        }
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) <= mLimit;
    }
    // Add by HZH on 2019/9/26 for  end

    // Add by zhiheng.huang on 2019/11/4 for  start
    private void setFlashStatus(boolean status) {
        mHandler.sendEmptyMessage(status ? FLASH_MODE_ON : FLASH_MODE_OFF);
    }
    // Add by zhiheng.huang on 2019/11/4 for  end
}