/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.applock.verifier.AppLockController;
import com.cydroid.softmanager.applock.verifier.LifecycleAwareManager;
import com.cydroid.softmanager.utils.Log;

import cyee.app.CyeeActivity;
import cyee.changecolors.ChameleonColorManager;
import cyee.changecolors.OnChangeColorListener;

public class AppLockActivity extends CyeeActivity implements OnChangeColorListener {
    private static final String TAG = "AppLockActivity";

    private String mTAG;
    private AppLockController mAppLockController;
    private LifecycleAwareManager mLifecycleAwareManager;
    private String mLockedPackageName;

    public boolean mUnlockSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate begin");
        super.onCreate(savedInstanceState);
        // Gionee xionghg 2017-08-01 add for 173837 begin
        setTheme(R.style.AppLockTheme);
        // Gionee xionghg 2017-08-01 add for 173837 end
        setContentView(R.layout.activity_app_lock);
        mLifecycleAwareManager = new LifecycleAwareManager();
        mAppLockController = new AppLockController(this, mLifecycleAwareManager);
        mTAG = TAG + mAppLockController.getKey();
        mAppLockController.init();
        mLifecycleAwareManager.notifyOnCreate();
        ChameleonColorManager.getInstance().addOnChangeColorListener(this);
        Log.d(mTAG, "onCreate end");
    }

    @Override
    public void onStart() {
        Log.d(mTAG, "onStart");
        super.onStart();
        mLifecycleAwareManager.notifyOnStart();
        mUnlockSuccess = false;
    }

    public void onResume() {
        Log.d(mTAG, "onResume");
        super.onResume();
        ViewGroup container = (ViewGroup) findViewById(R.id.activity_app_lock);
        String packageName = getFromAppPackageName();
        mAppLockController.createAppLockViewIfNeed(container, packageName);
        mAppLockController.adjustSoftInputMode(getWindow());
        mLifecycleAwareManager.notifyOnResume();
    }

    private String getFromAppPackageName() {
        mLockedPackageName = getIntent().getStringExtra("lock_package");
        return null == mLockedPackageName ? "" : mLockedPackageName;
    }

    public void onPause() {
        Log.d(mTAG, "onPause");
        super.onPause();
        mLifecycleAwareManager.notifyOnPause();
    }

    @Override
    public void onStop() {
        Log.d(mTAG, "onStop");
        super.onStop();
        mLifecycleAwareManager.notifyOnStop();
        if (!mUnlockSuccess) {
            sendUnlockResult(false);
        }
    }

    private void sendUnlockResult(boolean success) {
        // setResult即可，取消发送广播
        // Intent intent = new Intent("android.intent.action.applock.result");
        // intent.putExtra("applock_result", success ? 1 : 0);
        // Log.d(TAG, "sendUnlockResult: success=" + success);
        // sendBroadcast(intent);
       /*Chenyee guoxt modify for CSW1703CX-693 begin*/
        if(!AppLockActivity.this.isFinishing()) {
            finish();
        }
        /*Chenyee guoxt modify for CSW1703CX-693 begin*/
    }

    @Override
    public void onDestroy() {
        Log.d(mTAG, "onDestroy");
        super.onDestroy();
        mLifecycleAwareManager.notifyOnDestroy();
        mLifecycleAwareManager.clearHandler();
        mLifecycleAwareManager = null;
        mAppLockController.deinit();
        ChameleonColorManager.getInstance().removeOnChangeColorListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(mTAG, "onWindowFocusChanged hasFocus:" + hasFocus);
        mLifecycleAwareManager.notifyOnWindowFocusChanged(hasFocus);
    }

    @Override
    public void onBackPressed() {
        Log.d(mTAG, "onBackPressed");
        finishTask(this);
    }

    private void finishTask(AppCompatActivity activity) {
        setResult(AppCompatActivity.RESULT_OK);
        /*Chenyee guoxt modify for CSW1703CX-693 begin*/
        if(!AppLockActivity.this.isFinishing()) {
            finish();
        }
        /*Chenyee guoxt modify for CSW1703CX-693 end*/
    }

    public void dismiss(boolean unlocked) {
        Log.d(mTAG, "dismiss unlocked:" + unlocked);
        if (unlocked) {
            unlockedByUser(this);
        }
    }

    private void unlockedByUser(AppCompatActivity activity) {
        Intent data = new Intent();
        data.putExtra("confirm_result", 0);
        setResult(AppCompatActivity.RESULT_OK, data);
        sendUnlockResult(true);
        // finish();
    }

    public void cancelTask() {
        Log.d(mTAG, "cancelTask");
        finishTask(this);
    }

    public void onChangeColor() {
        Log.d(mTAG, "onChangeColor");
        finish();
    }

    // Gionee <houjie> <2015-07-29> add for CR01519483 begin 
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
    // Gionee <houjie> <2015-07-29> add for CR01519483 end
}
