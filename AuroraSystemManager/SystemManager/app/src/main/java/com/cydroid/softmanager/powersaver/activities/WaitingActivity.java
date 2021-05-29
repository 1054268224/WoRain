package com.cydroid.softmanager.powersaver.activities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.service.PowerManagerService;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.powersaver.utils.StatusbarController;
import com.cydroid.softmanager.utils.Log;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.app.ActivityManager.RunningTaskInfo;
import cyee.changecolors.ChameleonColorManager;

import com.cydroid.softmanager.R;

//Gionee <yangxinruo> <2015-07-28> add for CR01522918 begin
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import com.chenyee.featureoption.ServiceUtil;
//Gionee <yangxinruo> <2015-07-28> add for CR01522918 end

public class WaitingActivity extends BaseActivity {
    private static final String TAG = "WaitingActivity";

    public static final String ACTION_FINISH_SELF = "com.cydroid.softmanager.powersaver.WaitingActivity.FINISH_SELF";
    private static final int SHOWING_TOTAL_TIMEOUT = 2 * 60 * 1000 + 30 * 1000;
    private SuperModeProcessReceiver mSuperModeProcessReceiver;

    private View mView;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "enter onCreate----->");
        super.onCreate(savedInstanceState);
        if (PowerModeUtils.getModeProcessing(getApplicationContext()) != PowerConsts.SUPER_MODE_PROCESSING) {
            Log.d(TAG, "show at wrong scence");
            finish();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // xionghg 2017-08-17 add for 188862 begin
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().setAttributes(params);
        // xionghg 2017-08-17 add for 188862 end
        getmActionBar().hide();
        setContentView(R.layout.powersaver_waiting_view);
        initView();
        mHandler = new Handler();
        mSuperModeProcessReceiver = new SuperModeProcessReceiver(this, mHandler, SHOWING_TOTAL_TIMEOUT);
        Log.i(TAG, "leave onCreate----->");
    }

    private void initView() {
        mView = (View) findViewById(R.id.waiting);
        if (ChameleonColorManager.isNeedChangeColor()) {
            mView.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
        }
        mView.setSystemUiVisibility(
                View.STATUS_BAR_DISABLE_BACK | View.STATUS_BAR_DISABLE_HOME | View.STATUS_BAR_DISABLE_RECENT);

        dispatchAllKey();
    }

    private void dispatchAllKey() {
        try {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            Field field = lp.getClass().getDeclaredField("isHomekeyDispatched");
            field.setAccessible(true);
            field.set(lp, 1);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
            Log.i(TAG, "dispatchAllKey----->", e);
        }
    }

    private void processSuperModeFinish() {
        Log.d(TAG, "processSuperModeFinish ,remove self task");
        finishAndRemoveTask();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart----->");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause----->");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "enter onResume----->");
        super.onResume();
        ColorDrawable bdDraw = (ColorDrawable) mView.getBackground();
        if (bdDraw != null)
            getWindow().setStatusBarColor(bdDraw.getColor());
        Log.d(TAG, "leave onResume----->");
    }

    private class SuperModeProcessReceiver extends BroadcastReceiver implements Runnable {
        private final Context mContext;
        private boolean mIsRegistered = false;

        public SuperModeProcessReceiver(Context context, Handler handler, int timeout) {
            mContext = context;
            mHandler = handler;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_FINISH_SELF);
            mContext.registerReceiver(this, filter);
            Log.d(TAG, "set activity timeout " + timeout);
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, timeout);
            mIsRegistered = true;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr.equals(ACTION_FINISH_SELF)) {
                remove();
                // delay 1s to prevent to conflict with startLauncher intent
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        processSuperModeFinish();
                    }
                }, 1000);
            }
        }

        public void remove() {
            if (!mIsRegistered) {
                Log.d(TAG, "SuperModeProcessReceiver not registered or already removed!");
                return;
            }
            mIsRegistered = false;
            mHandler.removeCallbacks(this);
            mContext.unregisterReceiver(this);
            Log.d(TAG, "SuperModeProcessReceiver(" + this + ") in mContext( " + mContext + ") removed");
        }

        @Override
        public void run() {
            Log.d(TAG, "activity timeout  ");
            remove();
            startPowerManagerService();
            WaitingActivity.this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy----->");
        if (mSuperModeProcessReceiver != null) {
            mSuperModeProcessReceiver.remove();
        }
        super.onDestroy();
    }

    private void startPowerManagerService() {
        Log.d(TAG, " startPowerModeService activity tiomeout!");
        Intent startIntent = new Intent(this, PowerManagerService.class);
        startIntent.setAction(PowerManagerService.ACTION_CHECK_SUPER_MODE_BOOT_STATE);
        startIntent.putExtra("reason", "activity-timeout");
        ServiceUtil.startForegroundService(WaitingActivity.this,startIntent);
    }

    @Override
    public void onBackPressed() {
        // do nothing here to disable BACK_KEY
    }
}
