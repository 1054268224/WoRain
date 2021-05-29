//Gionee: mengdw <2016-09-18> add for CR01760277 begin
package com.cydroid.softmanager.trafficassistant.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings.Global;

import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.lockscreenused.controler.LockScreenTrafficUsedController;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.trafficassistant.TrafficMonitorBroadcastReceiver;
import android.content.IntentFilter;
import com.chenyee.featureoption.ServiceUtil;
public class TrafficResidentService extends Service {
    public static final String serviceName = "com.cydroid.softmanager.trafficassistant.service.TrafficResidentService";
    private static final String TAG = "TrafficResidentService";

    private TrafficSettingControler mTrafficSettingControler;
    // Gionee: mengdw <2016-12-13> add for CR01776232 begin
    // Gionee: mengdw <2016-12-13> add for CR01776232 end
    // Gionee: mengdw <2016-12-27> add for 55657 begin
    private PowerSaveChangeContentObserver mPowerSaveObserver;
    private IntentFilter netFilter;
    private Context mContext;
    private final TrafficMonitorBroadcastReceiver trafficReceiver = new TrafficMonitorBroadcastReceiver();
    // Gionee: mengdw <2016-12-27> add for 55657 end

    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    private LockScreenTrafficUsedController mLockScreenTrafficUsedController;
    // Gionee: mengdw <2016-12-06> add for CR01775579 end
    
    @Override
    public void onCreate() {
        Log.d(TAG, "TrafficResidentService onCreate");
        // Gionee: mengdw <2016-12-27> add for 55657 begin
        mContext = this;
        mTrafficSettingControler = TrafficSettingControler.getInstance(this);
        if (mTrafficSettingControler != null) {
            mTrafficSettingControler.startNotiActionMonitor();
        }
        mPowerSaveObserver = new PowerSaveChangeContentObserver();
        registerPowerSaveObserver();
        // Gionee: mengdw <2016-12-27> add for 55657 end
        // Gionee: mengdw <2016-12-06> add for CR01775579 begin
        mLockScreenTrafficUsedController = LockScreenTrafficUsedController.getInstance(this);
        // Gionee: mengdw <2016-12-06> add for CR01775579 end

        /*guoxt modify for CSW1702A-3133  2018-03-15 begin */
        netFilter = new IntentFilter();
        netFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        netFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(trafficReceiver, netFilter);
        /*guoxt modify for CSW1702A-3133  2018-03-15 end */
        super.onCreate();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "TrafficResidentService onDestroy");
        // Gionee: mengdw <2016-12-27> add for 55657 begin
        unRegisterPowerSaveObserver();
        /*guoxt modify for CSW1702A-3133  2018-03-15 begin */
        mContext.unregisterReceiver(trafficReceiver);
        /*guoxt modify for CSW1702A-3133  2018-03-15 end */
        // Gionee: mengdw <2016-12-27> add for 55657 end
        // Gionee: mengdw <2016-12-06> add for CR01775579 begin
        if (null != mLockScreenTrafficUsedController) {
            mLockScreenTrafficUsedController.finalizeLockScreenTrafficUsedController();
        }
        // Gionee: mengdw <2016-12-06> add for CR01775579 end
        super.onDestroy();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public static void processTrafficServiceIntent(Context context, boolean flag) {
        Log.d(TAG,"AppCreate processTrafficServiceIntent mContext=" + context + " flag=" + flag);
        if (flag) {
            TrafficassistantUtil.startServiceIntentProcess(context, serviceName, TrafficResidentService.class);
        } else {
            TrafficassistantUtil.stopServiceIntentProcess(context, serviceName, TrafficResidentService.class);
        }
    }
    
    // Gionee: mengdw <2016-12-27> add for 55657 begin
    private void registerPowerSaveObserver() {
        mContext.getContentResolver().registerContentObserver(Global.getUriFor(PowerConsts.POWER_MODE),
                true, mPowerSaveObserver);
    }

    private void unRegisterPowerSaveObserver() {
        mContext.getContentResolver().unregisterContentObserver(mPowerSaveObserver);
    }

    class PowerSaveChangeContentObserver extends ContentObserver {
        public PowerSaveChangeContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mTrafficSettingControler.commitTrafficNotiAction(mContext);
        }
    }
    // Gionee: mengdw <2016-12-27> add for 55657 end
}
//Gionee: mengdw <2016-09-18> add for CR01760277 end
