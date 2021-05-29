//mengdw <2015-10-20> add for CR01571760 begin
package com.cydroid.softmanager.trafficassistant;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import  com.cydroid.softmanager.utils.Log;

import com.cydroid.softmanager.monitor.utils.TrafficMonitorUtil;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.chenyee.featureoption.ServiceUtil;
public class TrafficHotSportService extends Service {
    private static final String TAG = "TrafficHotSportService";

    private static final String serviceName = "com.cydroid.softmanager.trafficassistant.TrafficHotSportService";
    private static final int QUERY_HOTSPORT_TRAFFIC_MESSAGE = 0;
    private static final int DELAY_TIME = 60 * 1000;// 60s
    private static final int TRAFFIC_UNIT = 1024;
    private static final int SERVICE_START_WAIT_TIME = 3000;
    private static long mStartTime = 0;

    private static boolean isRunning = true;
    private int mActivatedSimIndex;
    private Context mContext;
    private SharedPreferences mShare;
    private HotSportTrafficHandler mHotSportTrafficHandler;
    private TrafficMonitorUtil mTrafficMonitorUtil;
    private TrafficSettingControler mTrafficSettingControler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        terminateThread();
        if (mHotSportTrafficHandler != null) {
            mHotSportTrafficHandler.removeMessages(QUERY_HOTSPORT_TRAFFIC_MESSAGE);
        }
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startThread();
        mContext = TrafficHotSportService.this;
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(mContext);
        mActivatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
        Log.d(TAG, "onCreate mActivatedSimIndex=" + mActivatedSimIndex);
        if (!isValid(simInfo, mActivatedSimIndex)) {
            Log.d(TAG, "mActivatedSimIndex isValid not start HotSportTraffic");
            return;
        }
        mTrafficSettingControler = TrafficSettingControler.getInstance(mContext);
        mTrafficMonitorUtil = new TrafficMonitorUtil(mContext, mStartTime, System.currentTimeMillis(), mActivatedSimIndex);
        mTrafficMonitorUtil.initNetworkParam(mContext, mActivatedSimIndex);
        mHotSportTrafficHandler = new HotSportTrafficHandler();
        mHotSportTrafficHandler.sendEmptyMessageDelayed(QUERY_HOTSPORT_TRAFFIC_MESSAGE, DELAY_TIME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(TrafficHotSportService.this);
        return super.onStartCommand(intent, flags, startId);
    }

    public static void processIntent(Context context, boolean flag) {
        if (flag) {
            startIntent(context);
        } else {
            stopIntent(context);
        }
    }

    public class HotSportTrafficHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == QUERY_HOTSPORT_TRAFFIC_MESSAGE) {
                if (mTrafficMonitorUtil != null) {
                    mTrafficMonitorUtil.updateQueryTime(mStartTime, System.currentTimeMillis());
                    float hotSportTraffic = getHotSpotTraffic();
                    int hotSportRemindValue = mTrafficSettingControler.getHotSportRemindSettedValue(mContext);
                    int hotSportRemindIndex = mTrafficSettingControler.getHotSportRemidSettedIndex(mContext);
                    long hotSportTrafficLimit = (long) hotSportRemindValue * TRAFFIC_UNIT * TRAFFIC_UNIT;
                    // mengdw <2016-04-11> modify for CR01672259 begin
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    boolean isSoftApOpen = wifiManager == null || wifiManager.isWifiApEnabled();
                    Log.d(TAG, "isSoftApOpen=" + isSoftApOpen + " hotSportTraffic=" + hotSportTraffic +
                            " hotSportTrafficSetting=" + hotSportRemindValue);
                    if (hotSportRemindIndex != TrafficSettingControler.HOTSPORT_REMIND_CLOSE) {
                        if (hotSportTraffic >= hotSportTrafficLimit && isSoftApOpen) {
                            Log.d(TAG, "hotspot warning dialog");
                            saveLastRemindData();
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClass(getApplicationContext(), TrafficHotSpotWarningDialog.class);
                            startActivity(intent);
                        } else {
                            mHotSportTrafficHandler.sendEmptyMessageDelayed(QUERY_HOTSPORT_TRAFFIC_MESSAGE,
                                    DELAY_TIME);
                        }
                    } else {
                        Log.d(TAG, "HotSportTraffic close not show dialog");
                        mHotSportTrafficHandler.sendEmptyMessageDelayed(QUERY_HOTSPORT_TRAFFIC_MESSAGE,
                                DELAY_TIME);
                    }
                    // mengdw <2016-04-11> modify for CR01672259 end
                } else {
                    Log.d(TAG, "mTrafficMonitorUtil is null error");
                }
            }
        }
    }

    private float getHotSpotTraffic() {
        long monitorTraffic = mTrafficMonitorUtil.getHotSportTraffic();
        float result = monitorTraffic;
        String lastRemindDate = mTrafficSettingControler.getHotSportLastRemindDate(mContext, mActivatedSimIndex);
        float lastRemindTraffic = mTrafficSettingControler.getHotSportLastRemindTraffic(mContext, mActivatedSimIndex);
        String currentDate = TimeFormat.getNowDate();
        if (isTheSameDay(lastRemindDate, currentDate)) {
            if (monitorTraffic >= lastRemindTraffic) {
                result = monitorTraffic - lastRemindTraffic;
            }
        }
        Log.d(TAG, "getHotSpotTraffic lastRemindTime=" + lastRemindDate + " lastRemindTraffic=" + lastRemindTraffic +
                " currentDate=" + currentDate + " result=" + result + " monitorTraffic=" + monitorTraffic);
        return result;
    }

    private boolean isTheSameDay(String lastDate, String currentDate) {
        String[] lastDateSplit = lastDate.split("-");
        String[] curDateSplit = currentDate.split("-");
        return lastDateSplit[2].equals(curDateSplit[2]);
    }

    private void saveLastRemindData() {
        String date = TimeFormat.getNowDate();
        long traffic = mTrafficMonitorUtil.getHotSportTraffic();
        mTrafficSettingControler.setHotSportLastRemindDate(mContext, mActivatedSimIndex, date);
        mTrafficSettingControler.setHotSportLastRemindTraffic(mContext, mActivatedSimIndex, traffic);
    }

    private static long getStartTime() {
        int[] timeArray = null;
        long strartTime = System.currentTimeMillis();
        timeArray = TimeFormat.getNowTimeArray();
        timeArray[1] += 1;

        //if (timeArray != null) {
        strartTime = TimeFormat.getStartTime(timeArray[0], timeArray[1], timeArray[2], timeArray[3],
                timeArray[4], timeArray[5]);
        //}
        return strartTime;
    }

    private static void startIntent(Context context) {
        if (!TrafficassistantUtil.isServiceRunning(context, serviceName)) {
            mStartTime = getStartTime();
            Log.d(TAG, "startIntent mStartTime=" + mStartTime + " currtime=" + System.currentTimeMillis());
            waitTime(SERVICE_START_WAIT_TIME);
            Intent i = new Intent(context, TrafficHotSportService.class);
            ServiceUtil.startForegroundService(context,i);
        } else {
            Log.d(TAG, "service is already start");
        }
    }

    private static void stopIntent(Context context) {
        if (TrafficassistantUtil.isServiceRunning(context, serviceName)) {
            Log.d(TAG, "stop service");
            waitTime(SERVICE_START_WAIT_TIME);
            Intent i = new Intent(context, TrafficHotSportService.class);
            context.stopService(i);
        } else {
            Log.d(TAG, "service is already stop ");
        }
    }

    private static void startThread() {
        isRunning = true;
    }

    private static void terminateThread() {
        isRunning = false;
    }

    private boolean isValid(SIMInfoWrapper simInfo, int simIndex) {
        return (simIndex > -1) && !simInfo.isWiFiActived()
                && simInfo.gprsIsOpenMethod("getMobileDataEnabled");
    }

    private static void waitTime(long ms) {
        Object obj = new Object();
        synchronized (obj) {
            try {
                obj.wait(ms);
            } catch (Exception e) {
                Log.i("TrafficProcessorService", " obj.wait ", e);
            }
        }
    }
}
//mengdw <2015-10-20> add for CR01571760 end
