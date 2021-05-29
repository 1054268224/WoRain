//Gionee: mengdw <2015-12-28> add for CR01589343 begin
package com.cydroid.softmanager.trafficassistant.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.chenyee.featureoption.ServiceUtil;
public class TrafficMonitorControlService extends Service {
    public static final String serviceName = "com.cydroid.softmanager.trafficassistant.service.TrafficMonitorControlService";
    private static final String TAG = "TrafficMonitorControlService";
    private TrafficCalibrateControler mTrafficCalibrateControler;
    
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mTrafficCalibrateControler = TrafficCalibrateControler.getInstance(this);
        super.onCreate();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "TrafficMonitorControlService onDestroy");
        mTrafficCalibrateControler.finalizeTrafficCalibrateControler();
        super.onDestroy();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            ServiceUtil.handleStartForegroundServices(this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static void processMonitorControlServiceIntent(Context context, boolean flag) {
        Log.d(TAG,"processMonitorControlServiceIntent mContext=" + context + " flag=" + flag);
        if (flag) {
            TrafficassistantUtil.startServiceIntentProcess(context, serviceName, TrafficMonitorControlService.class);
        } else {
            TrafficassistantUtil.stopServiceIntentProcess(context, serviceName, TrafficMonitorControlService.class);
        }
    }
}
//Gionee: mengdw <2015-12-28> add  for CR01589343 end