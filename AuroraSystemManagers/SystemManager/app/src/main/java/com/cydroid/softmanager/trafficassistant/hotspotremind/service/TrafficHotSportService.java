//mengdw <2015-10-20> add for CR01571760 begin
package com.cydroid.softmanager.trafficassistant.hotspotremind.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import  com.cydroid.softmanager.utils.Log;

import com.cydroid.softmanager.trafficassistant.hotspotremind.controler.TrafficHotspotUsedRemindControler;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.chenyee.featureoption.ServiceUtil;
public class TrafficHotSportService extends Service {
    private static final String TAG = "TrafficHotSportService";

    private static final String serviceName = "com.cydroid.softmanager.trafficassistant.service.TrafficHotSportService";

    private Context mContext;
    private TrafficHotspotUsedRemindControler mTrafficHotspotUsedRemindControler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, " onCreate");
        mContext = TrafficHotSportService.this;
        mTrafficHotspotUsedRemindControler = TrafficHotspotUsedRemindControler.getInstance(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand flags=" + flags + " startId=" + startId);
        if (null == intent) {
            Log.d(TAG, "onStartCommand intent is null");
            return super.onStartCommand(null, flags, startId);
        }
        ServiceUtil.handleStartForegroundServices(TrafficHotSportService.this);
        if (null == mTrafficHotspotUsedRemindControler) {
            mTrafficHotspotUsedRemindControler = TrafficHotspotUsedRemindControler.getInstance(mContext);
        }
        int startType = intent.getIntExtra(Constant.HOTSPOT_START_TYPE, Constant.TYPE_HOTSPOT_START_NORMAL);
        String action = intent.getAction();
        boolean isMonitorRunning = mTrafficHotspotUsedRemindControler.isMonitorRunning();
        Log.d(TAG, "onStartCommand startType=" + startType + " action=" + action + " isMonitorRunning=" + isMonitorRunning);
        if (Constant.ACTION_HOTSPOT_SERVICE_START.equals(action) && !isMonitorRunning) {
            mTrafficHotspotUsedRemindControler.startHotspotMonitor(startType);
        } else if (Constant.ACTION_HOTSPOT_SERVICE_STOP.equals(action) && isMonitorRunning) {
            mTrafficHotspotUsedRemindControler.stopHotspotMonitor();
            this.stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
//mengdw <2015-10-20> add for CR01571760 end
