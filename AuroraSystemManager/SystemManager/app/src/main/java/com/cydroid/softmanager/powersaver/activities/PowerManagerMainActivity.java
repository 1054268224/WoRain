package com.cydroid.softmanager.powersaver.activities;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.format.Formatter;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.app.IBatteryStats;
import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.powersaver.interfaces.IPowerService;
import com.cydroid.softmanager.powersaver.notification.BackgroundAppListActivity;
import com.cydroid.softmanager.powersaver.service.PowerManagerService;
import com.cydroid.softmanager.powersaver.utils.BatteryStateInfo;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.powersaver.utils.PowerTimer;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;
import com.cydroid.softmanager.view.MainCircleView;
import com.cydroid.softmanager.view.PowerCircleView;

import java.util.List;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeButton;

public class PowerManagerMainActivity extends BaseActivity {
    private static final String TAG = "PowerManagerMainActivity";

    private final static int SWEEP_ANGLE_CONSTANT = 290;
    private final static int PERCENT_CONSTANT = 100;
    private final static int MSG_STATE_INTO_SCREEN = 1;
    private final static int MSG_UPDATE_TIME_TEXT = 11;

    private Context mContext;
    private IBatteryStats mBatteryInfo;
    private TextView mBatteryLevel;
    private TextView mPercent;
    private TextView mTimeDisplay;
    private TextView mDailyPowerTxt;
    private TextView mBgProcessesEntryTxt;
    private CyeeListView mPowerModeList;
    private MainCircleView mPowerCircle;
    private PowerManagerModeAdapter mAdapter;
    private PowerTimer mPowerTimer;
    private RelativeLayout mTopHalfLayout;
    private CyeeButton mBackAppBtn;
    private static final boolean DEBUG = true;
    /* animation flag when entering into screen first */
    private boolean isIntoScreenAnima = true;

    private boolean mShouldDrawAnim = false;
    private boolean mIsModeChangeRegistered = false;

    private StateHandler mHandler = new StateHandler();

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    private boolean mIsBound = false;
    private IPowerService mService = null;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IPowerService.Stub.asInterface((IBinder) service);
            Log.d(TAG, "PowerManagerService connected,init time");
            mIsBound = true;
            mAdapter.setPowerService(mService);
            updateAllViews();
            // Gionee <yangxinruo> <2016-4-25> delete for CR01684864 begin
            // initAdapter();
            // Gionee <yangxinruo> <2016-4-25> delete for CR01684864 end
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.d(TAG, "PowerManagerService disconnected");
            mAdapter.setPowerService(mService);
        }
    };

    private final BroadcastReceiver mExternalChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "MODE TEST mConfigReceiver receive " + intent.getAction() + " call updateAllViews()");
            updateAllViews();
        }
    };

    private final BroadcastReceiver mModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "MODE TEST mode changed  call updateAllViews()");
            mAdapter.setLockModeButton(false, -1);
            updateAllViews();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "--------->enter onCreate, call ChameleonColorManager.getInstance().onCreate");
        UiUtils.setElevation(getmActionBar(), 0);
        ChameleonColorManager.getInstance().onCreate(this);
        setContentView(R.layout.powermanager_main_activity);
        initClassFields();
        mHandler.setActivity(this);

        initAdapter();

        mContext.bindService(new Intent(mContext, PowerManagerService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        setSecondClass(PowerManagerSettingsActivity.class);
        setFirstLayoutVisibility(View.GONE);
        setFirstClass(BatteryUseRankActivity.class);
        chameleonColorProcess();
        mShouldDrawAnim = true;
        mIsModeChangeRegistered = false;

    }

    private void chameleonColorProcess() {
        Log.d(TAG, "COLOR enter chameleonColorProcess isNeedChangeColor val="
                + ChameleonColorManager.isNeedChangeColor());
        if (ChameleonColorManager.isNeedChangeColor()) {
            Log.d(TAG, "COLOR ChameleonColorManager.isNeedChangeColor()=true set theme color ");
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            int color_A1 = ChameleonColorManager.getAppbarColor_A1();
            int color_T3 = ChameleonColorManager.getContentColorThirdlyOnAppbar_T3();
            int color_B4 = ChameleonColorManager.getButtonBackgroudColor_B4();

            mTopHalfLayout.setBackgroundColor(color_A1);

            mBatteryLevel.setTextColor(color_T1);
            mPercent.setTextColor(color_T1);
            mBgProcessesEntryTxt.setTextColor(color_T1);
            mTimeDisplay.setTextColor(color_T1);
            mDailyPowerTxt.setTextColor(color_T1);

            /*mPowerCircle.setArcColor(color_T1);
            mPowerCircle.setInnerCircleColor(color_T3);
            mPowerCircle.setPointTint(color_T1);*/
            mBackAppBtn.setTextColor(color_T1);
            mBackAppBtn.setBackgroundColorFilter(color_B4);
        }
    }

    // Gionee <yangxinruo> <2015-10-23> add for CR01573418 begin
    private boolean isForegroundNow() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(getPackageName())) {
                return appProcess.importance <= RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }
    // Gionee <yangxinruo> <2015-10-23> add for CR01573418 end

    @Override
    protected void onResume() {
        super.onResume();
        // Gionee <yangxinruo> <2015-10-23> modify for CR01573418 begin
        boolean isOnTop = isForegroundNow();
        // Gionee <yangxinruo> <2015-10-23> modify for CR01573418 end

        Log.d(TAG, "--------->enter onResume, mShouldDrawAnim=" + mShouldDrawAnim + " isOnTop=" + isOnTop);
        if (mShouldDrawAnim && isOnTop) {
            onIntoScreenAnimation();
        } else if (mShouldDrawAnim) {
            // Gionee <yangxinruo> <2016-3-14> modify for CR01573418 begin
            int batteryLevel = getBatteryLevel();
            updateBatteryLevel(batteryLevel);
            // Gionee <yangxinruo> <2016-3-14> modify for CR01573418 end
        }
        // Gionee <yangxinruo> <2015-10-12> add for CR01565576 end
        registerExternalConfigChangedReceiver();
        registerPowerModeChangedReceiver();
    }

    private void registerPowerModeChangedReceiver() {
        if (mIsModeChangeRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        // Gionee <yangxinruo> <2015-09-21> add for CR01557759 begin
        filter.addAction(PowerManagerService.EVENT_MODE_CHANGED);
        // Gionee <yangxinruo> <2015-09-21> add for CR01557759 end
        mContext.registerReceiver(mModeChangedReceiver, filter);
        mIsModeChangeRegistered = true;
    }

    private void unregisterPowerModeChangedReceiver() {
        if (mIsModeChangeRegistered) {
            mIsModeChangeRegistered = false;
            mContext.unregisterReceiver(mModeChangedReceiver);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "--------->enter onPause");
        super.onPause();
        try {
            unregisterExternalConfigChangedReceiver();
        } catch (Exception e) {
            Log.e(DEBUG, TAG, "call unregister throw exception ", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "--------->enter onDestroy");
        removePopupDialogs();
        recycleHandler();
        //chenyee zhaocaili 20180508 add for CSW1707A-845 begin
        releasRes();
        //chenyee zhaocaili 20180508 add for CSW1707A-845 end
        try {
            unregisterPowerModeChangedReceiver();
        } catch (Exception e) {
            Log.e(DEBUG, TAG, "call unregister throw exception ", e);
        }
        // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
        Log.d(TAG, "remove  service in adapter");
        mAdapter.setPowerService(null);
        if (mIsBound) {
            try {
                Log.d(TAG, "unbind service");
                mContext.unbindService(mConnection);
                mIsBound = false;
            } catch (Exception e) {
                Log.d(TAG, "unbindService service error " + e);
            }
        }
        // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
        ChameleonColorManager.getInstance().onDestroy(this);
        super.onDestroy();
    }

    private synchronized void recycleHandler() {
        mHandler.setActivity(null);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    private void removePopupDialogs() {
        if (mAdapter == null) {
            return;
        }
        mAdapter.removePopupDialogs();
    }

    //chenyee zhaocaili 20180508 add for CSW1707A-845 begin
    private void releasRes() {
        Util.unbindDrawables(findViewById(R.id.root));
    }
    //chenyee zhaocaili 20180508 add for CSW1707A-845 end

    private void updateAllViews() {
        if (!isIntoScreenAnima) {
            updateBatteryInfo();
            updateTimeAlert();
            // Gionee <yangxinruo> <2015-11-20> add for CR01594360 begin
            updateDailyPowerTxt();
            // Gionee <yangxinruo> <2015-11-20> add for CR01594360 end
        }

        if (mAdapter != null) {
//            mAdapter.notifyDataSetChanged();
        }
    }

    private void updateDailyPowerTxt() {
        int mode = PowerModeUtils.getCurrentMode(mContext);
        if (mode == PowerConsts.NORMAL_MODE) {
            mDailyPowerTxt.setVisibility(View.VISIBLE);
        } else {
            mDailyPowerTxt.setVisibility(View.GONE);
        }
    }

    private void updateBatteryInfo() {
        int batteryLevel = getBatteryLevel();
        updateBatteryLevel(batteryLevel);
        /*updateCircleDisplay((int) (SWEEP_ANGLE_CONSTANT * batteryLevel * 1.0 / PERCENT_CONSTANT));*/
    }

    private int getBatteryLevel() {
        return BatteryStateInfo.getBatteryLevel(mContext);
    }

    private void updateBatteryLevel(int level) {
        mBatteryLevel.setText(Integer.toString(level));
    }

    /*private void updateCircleDisplay(int level) {
        mPowerCircle.updateRatio(level);
        mPowerCircle.updateViews();
    }*/

    private void resetIntoScreenAnimaFlag(boolean flag) {
        isIntoScreenAnima = flag;
    }

    private void updateTimeAlert() {
        Thread updateTimeThread = new Thread() {
            @Override
            public void run() {
                String str = null;
                long timeFromSystem = 0;
                int time = 0;
                try {
                    if (BatteryStateInfo.isChargingNow(mContext)) {
                        timeFromSystem = mBatteryInfo.computeChargeTimeRemaining();
                        if (timeFromSystem > 0) {
                            str = getResources().getString(R.string.need_charging_time);
                            String formattedTime = Formatter.formatShortElapsedTime(mContext, timeFromSystem);
                            str = String.format(str, formattedTime);
                        } else {
                            if (BatteryStateInfo.getBatteryLevel(mContext) != 100) {
                                str = getResources().getString(R.string.is_charging_now);
                            } else {
                                str = getResources().getString(R.string.charged_completely);
                            }
                        }
                    } else {
                        // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
                        str = getResources().getString(R.string.time_use_in_original_mode);
                        // time = mPowerTimer.getTime();
                        time = (int) mPowerTimer.getTimeInMode(mService, PowerConsts.NONE_MODE);
                        // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end
                        str = String.format(str, mPowerTimer.formatTime(time));
                    }
                    if (str == null)
                        str = "";
                    sendMessage(MSG_UPDATE_TIME_TEXT, 0, 0, str);
                } catch (RemoteException e) {
                    Log.d(DEBUG, TAG, "call computeChargeTimeRemaining throw remote exception");
                } catch (NotFoundException e) {
                    Log.d(DEBUG, TAG, "updateTimeAlert, getString() throw exception, " + e.toString());
                }
            }
        };
        updateTimeThread.start();
    }

    private void registerExternalConfigChangedReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mExternalChangedReceiver, filter);
    }

    private void unregisterExternalConfigChangedReceiver() {
        mContext.unregisterReceiver(mExternalChangedReceiver);
    }

    private void initClassFields() {
        mContext = this;
        mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
        mPowerTimer = new PowerTimer(mContext);
        mBatteryLevel = (TextView) findViewById(R.id.battery_level);
        mPercent = (TextView) findViewById(R.id.battery_percent);
        mTimeDisplay = (TextView) findViewById(R.id.power_usetime_show);
        mDailyPowerTxt = (TextView) findViewById(R.id.power_daily_noti);
//        mPowerCircle = (MainCircleView) findViewById(R.id.power_circle);
        mPowerModeList = (CyeeListView) findViewById(R.id.power_mode_list);
        // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
        mTopHalfLayout = (RelativeLayout) findViewById(R.id.power_main_top);
        // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
        mBgProcessesEntryTxt = (TextView) findViewById(R.id.background_running_processes_entry);
        mBgProcessesEntryTxt.setVisibility(View.GONE);
        //mBackAppBtn = (CyeeButton) findViewById(R.id.check_back_app_btn);
        /*mBackAppBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PowerManagerMainActivity.this, BackgroundAppListActivity.class);
                startActivity(intent);
            }
        });*/
    }

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    private void initAdapter() {
        mAdapter = new PowerManagerModeAdapter(mContext, mPowerTimer);
        mPowerModeList.setAdapter(mAdapter);
    }
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end

    @Override
    public boolean isUsingCustomActionBar() {
        return true;
    }

    private void onIntoScreenAnimation() {
        if (isIntoScreenAnima) {
            updateBatteryLevel(100);
            /*updateCircleDisplay(SWEEP_ANGLE_CONSTANT);*/
            int end = getBatteryLevel();
            new Thread(new PowerRunnable(MSG_STATE_INTO_SCREEN, 100, end)).start();
        }
    }

    private class PowerRunnable implements Runnable {
        private int mState = 0;
        private int mStart = 0;
        private int mEnd = 0;

        public PowerRunnable(int state, int start, int end) {
            mState = state;
            mStart = start;
            mEnd = end;
        }

        @Override
        public void run() {
            int duration = 1;
            sleep(duration * 600);
            if ((mState & MSG_STATE_INTO_SCREEN) != 0) {
                while (mStart-- > mEnd) {
                    sendMessage(mState, mStart, 0, null);
                    sleep(getSleepTime(mStart));
                }
                sendMessage(mState, mStart, 1, null);
            }
            // Gionee <yangxinruo> <2015-10-12> add for CR01565576 begin
            mShouldDrawAnim = false;
            // Gionee <yangxinruo> <2015-10-12> add for CR01565576 end
        }

        private void sleep(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (Exception ex) {
            }
        }

        private int getSleepTime(int value) {
            int duration = (int) 1.21 * (value / 20);
            if (duration > 4) {
                duration = 4;
            }
            return duration;
        }
    }

    private synchronized void sendMessage(int state, int arg1, int arg2, Object obj) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(state, arg1, arg2, obj);
            mHandler.sendMessage(msg);
        }
    }

    static class StateHandler extends Handler {
        PowerManagerMainActivity mPowerManagerMainActivity;

        public void setActivity(PowerManagerMainActivity powerManagerMainActivity) {
            mPowerManagerMainActivity = powerManagerMainActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (null == mPowerManagerMainActivity) {
                return;
            }
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_STATE_INTO_SCREEN:
                    if (msg.arg2 == 0) {
                        mPowerManagerMainActivity.updateBatteryLevel(msg.arg1 * PERCENT_CONSTANT / SWEEP_ANGLE_CONSTANT);
                        /*mPowerManagerMainActivity.updateCircleDisplay(msg.arg1 * SWEEP_ANGLE_CONSTANT / PERCENT_CONSTANT);*/
                    } else if (msg.arg2 == 1) {
                        mPowerManagerMainActivity.resetIntoScreenAnimaFlag(false);
                        mPowerManagerMainActivity.updateBatteryInfo();
                        mPowerManagerMainActivity.updateTimeAlert();
                        mPowerManagerMainActivity.updateDailyPowerTxt();
                    }
                    break;
                case MSG_UPDATE_TIME_TEXT:
                    String timeStr = (String) msg.obj;
                    mPowerManagerMainActivity.mTimeDisplay.setText(timeStr);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}
