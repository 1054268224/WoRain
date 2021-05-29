//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.trafficassistant.controler.TrafficNetworkController;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.hotspotremind.service.TrafficHotSportService;
import com.cydroid.softmanager.trafficassistant.net.AppTrafficService;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.MobileTemplate;
import com.cydroid.softmanager.trafficassistant.utils.TrafficPreference;
import com.cydroid.softmanager.utils.Log;
import com.chenyee.featureoption.ServiceUtil;
import java.util.Calendar;

public class TrafficMonitorBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "TrafficMonitorBroadcastReceiver";

    private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private final static String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private final static String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    //Chenyee guoxt modify for CSW1705A-2570 begin
    public final static String ACTION_TURN_OFF_DATA_NOTI = "chenyee.intent.action.turnoff.data.noti";
    //Chenyee guoxt modify for CSW1705A-2570 end
    private int mSimIndex;
    private final int mSimCount = Constant.SIM_COUNT;
    private final boolean[] mTrafficSettings = new boolean[mSimCount];
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.d(TAG, "onReceive action=" + intent.getAction());
        if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
            Log.d(TAG, "sim card change!");
            simCardChange(mContext);
            // Gionee: mengdw <2016-08-08> add for CR01738356 begin
            processSimChange();
            // Gionee: mengdw <2016-08-08> add for CR01738356 end
        } else if (intent.getAction().equals(ACTION_CONNECTIVITY_CHANGE)) {
            // Gionee <yangxinruo> <2016-4-26> modify for CR01685530 begin
            int powerMode = PowerModeUtils.getCurrentMode(mContext);
            Log.d(TAG, "network change! powerMode=" + powerMode);
            if (powerMode != PowerConsts.SUPER_MODE) {
                networkChange(mContext);
            }
            // Gionee <yangxinruo> <2016-4-26> modify for CR01685530 end
            // Gionee: mengdw <2017-03-02> add for 55579 begin
            Log.d(TAG, "network change update totay view");
            TrafficSettingControler controler = TrafficSettingControler.getInstance(mContext);
            controler.commitTrafficNotiAction(mContext);
            // Gionee: mengdw <2017-03-02> add for 55579 end

        } else if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            networkControl(mContext);
            // Gionee: mengdw <2015-10-20> add for CR01571760 begin
            //Chenyee guoxt modify for CSW1705A-2570 begin
        } else if(intent.getAction().equals(ACTION_TURN_OFF_DATA_NOTI)){
            TrafficSettingControler controler = TrafficSettingControler.getInstance(mContext);
            controler.setNotificationInfoSwtich(mContext,false);
            controler.cancelNotification();
            Intent refreshButton = new Intent("chenyee.intent.action.turnoff.radiobutton");
            mContext.sendBroadcast(refreshButton);
            //Chenyee guoxt modify for CSW1705A-2570 end
        } else if (intent.getAction().equals(WifiManager.WIFI_AP_STATE_CHANGED_ACTION)) {
            handleWifiApStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE,
                    WifiManager.WIFI_AP_STATE_FAILED));
            // Gionee: mengdw <2015-10-20> add for CR01571760 end
        }
    }

    // Gionee: mengdw <2016-12-13> add for CR01776232 begin
    private void networkControl(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TrafficNetworkController networkControl = TrafficNetworkController.getInstance(context);
                networkControl.init();
                networkControl.rebootDisableNetwork();
            }
        }).start();
    }



    // Gionee: mengdw <2015-10-20> add for CR01571760 begin
    private void handleWifiApStateChanged(int state) {
        Log.d(TAG, "handleWifiApStateChanged state=" + state);
        if (WifiManager.WIFI_AP_STATE_ENABLED == state) {
            startHotSportTrafficMonitor(mContext);
        } else if (WifiManager.WIFI_AP_STATE_DISABLED == state) {
            stopHotSportTrafficMonitor(mContext);
        }
    }
    // Gionee: mengdw <2015-10-20> add for CR01571760 end

    // Gionee: mengdw <2016-08-08> add for CR01738356 begin
    private void processSimChange() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                resetSimCardChangeInfo(mContext);
            }
        }).start();
    }
    // Gionee: mengdw <2016-08-08> add for CR01738356 end

    private void simCardChange(Context context) {
        Log.d(TAG, "simCardChange call commitTrafficNotiAction");
        TrafficSettingControler trafficSettingControler = TrafficSettingControler.getInstance(mContext);
        trafficSettingControler.commitTrafficNotiAction(mContext);
    }

    private void resetSimCardChangeInfo(Context context) {
        SharedPreferences share = context.getSharedPreferences("com.cydroid.softmanager_preferences",
                Context.MODE_MULTI_PROCESS);
        SIMInfoWrapper.setEmptyObject(context);
        final SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        //Chenyee guoxt modify for SWW1618OTA-450 begin
        if(simInfo == null){
            return;
        }
        //Chenyee guoxt modify for SWW1618OTA-450 end
        int count = simInfo.getInsertedSimCount();
        for (int i = 0; i < count; i++) {
            try {
                String currentSimIMSIId = MobileTemplate.getSubscriberId(context, i);
                int simIndex = simInfo.getInsertedSimInfo().get(i).mSlot;
                String key = "sim" + simIndex;
                String oldSimIMSIId = share.getString(key, "");
                if (oldSimIMSIId.isEmpty()) {
                    share.edit().putString(key, currentSimIMSIId).commit();
                } else if (!currentSimIMSIId.equals(oldSimIMSIId)) {
                    Log.d("sim", "resetSimCardChangeInfo" + currentSimIMSIId + "," + oldSimIMSIId + ",no equal");
                    share.edit().putString(key, currentSimIMSIId).commit();
                } else {
                    Log.d("sim", currentSimIMSIId + "," + oldSimIMSIId + ",equal");
                }
            } catch (Exception ex) {
                Log.d(TAG, "Exception :" + ex.toString());
            }
        }
    }

    private void networkChange(final Context context) {
        SharedPreferences share = context.getSharedPreferences("com.cydroid.softmanager_preferences",
                Context.MODE_MULTI_PROCESS);
        SIMInfoWrapper.setEmptyObject(context);
        onTrafficSettings(context, share);
        final SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        if (networkCheck(context, share, simInfo)) {
            onBindSaveingTraffic(context, simInfo);
            startTrafficMonitor(context);
        } else {
            stopTrafficMonitor(context);
        }
    }

    private void startTrafficMonitor(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                TrafficProcessorService.processIntent(context, false);
                TrafficProcessorService.processIntent(context, true);
            }
        }).start();
    }

    private void stopTrafficMonitor(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                TrafficProcessorService.processIntent(context, false);
            }
        }).start();
    }

    // Gionee: mengdw <2015-10-20> add for CR01571760 begin
    private void startHotSportTrafficMonitor(final Context context) {
        Intent startIntent = new Intent(context, TrafficHotSportService.class);
        startIntent.setAction(Constant.ACTION_HOTSPOT_SERVICE_START);
        startIntent.putExtra(Constant.HOTSPOT_START_TYPE, Constant.TYPE_HOTSPOT_START_NORMAL);
        ServiceUtil.startForegroundService(context,startIntent);
    }

    private void stopHotSportTrafficMonitor(final Context context) {
        Intent stopIntent = new Intent(context, TrafficHotSportService.class);
        stopIntent.setAction(Constant.ACTION_HOTSPOT_SERVICE_STOP);
        stopIntent.putExtra(Constant.HOTSPOT_START_TYPE, Constant.TYPE_HOTSPOT_START_NORMAL);
        ServiceUtil.startForegroundService(context,stopIntent);
    }
    // Gionee: mengdw <2015-10-20> add for CR01571760 end

    private boolean networkCheck(Context context, SharedPreferences share, SIMInfoWrapper simInfo) {
        if (simInfo.getInsertedSimCount() == 0) {
            return false;
        }

        if (simInfo.isWiFiActived() || !simInfo.gprsIsOpenMethod("getMobileDataEnabled")) {
            return false;
        }

        mSimIndex = getSimCardNo(context, simInfo);

        return mSimIndex >= 0;
    }

    private int getSimCardNo(Context context, SIMInfoWrapper simInfo) {
        int simIndex;
        simIndex = getActivatedSimCardNo(context, simInfo);
        if (simIndex < 0) {
            simIndex = getDefaultNoneActivatedSimCardNo(context, simInfo);
        }
        return simIndex;
    }

    private int getActivatedSimCardNo(Context context, SIMInfoWrapper simInfo) {
        int activatedSimIndex = -1;
        if (simInfo.gprsIsOpenMethod("getMobileDataEnabled")) {
            activatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
        }
        return activatedSimIndex;
    }

    private int getDefaultNoneActivatedSimCardNo(Context context, SIMInfoWrapper simInfo) {
        int noneActivatedSimIndex = -1;
        // Gionee: mengdw <2015-09-21> modify for CR01557552 begin
        if (null != simInfo && simInfo.getInsertedSimCount() > 1) {
            // Gionee: mengdw <2015-09-21> modify for CR01557552 end
            noneActivatedSimIndex = 0;
        } else {
            // Gionee: mengdw <2015-08-25> modify for CR01543192 begin
            if (null != simInfo && simInfo.getInsertedSimInfo().size() > 0) {
                noneActivatedSimIndex = simInfo.getInsertedSimInfo().get(0).mSlot;
            }
            // Gionee: mengdw <2015-08-25> modify for CR01543192 end
        }
        return noneActivatedSimIndex;
    }

    private void onTrafficSettings(Context context, SharedPreferences share) {
        String[] strSettings = new String[mSimCount];
        String[] strReset = new String[mSimCount];
        boolean[] isReset = new boolean[mSimCount];

        initMonitorFlag(share, strSettings, strReset, isReset);
        resetMonitorFlag(context, share, strSettings, strReset, isReset);
    }

    private void initMonitorFlag(SharedPreferences share, String[] strSettings, String[] strReset,
                                 boolean[] isReset) {
        // Gionee: mengdw <2016-05-18> modify for CR01696960 begin
        for (int index = 0; index < mSimCount; index++) {
            mTrafficSettings[index] = TrafficPreference.getSimBooleanPreference(mContext, index,
                    TrafficPreference.KEY_TRAFFIC_PACKAGE_SETTED_FLAG, false);
            isReset[index] = TrafficPreference.getSimBooleanPreference(mContext, index,
                    TrafficPreference.KEY_SIM_RESET, false);
        }
        // Gionee: mengdw <2016-05-18> modify for CR01696960 end
    }

    private void resetMonitorFlag(Context context, SharedPreferences share, String[] strSettings,
                                  String[] strReset, boolean[] isReset) {
        for (int index = 0; index < mSimCount; index++) {
            if (mTrafficSettings[index]) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int startDay = TrafficPreference.getSimIntPreference(context, index,
                        TrafficPreference.KEY_START_DATE, 1);
                if (day == startDay) {
                    if (!isReset[index]) {
                        Log.d(TAG, "resetMonitorFlag reset CalibratedActualFlow");
                        TrafficPreference.setSimFloatPreference(context, index,
                                TrafficPreference.KEY_CALIBRATED_ACTUAL_FLOW, 0);
                        TrafficPreference.setSimBooleanPreference(context, index,
                                TrafficPreference.KEY_SIM_RESET, true);
                    }
                } else {
                    TrafficPreference.setSimBooleanPreference(context, index,
                            TrafficPreference.KEY_SIM_RESET, false);
                }
            }
        }
    }

    private void onBindSaveingTraffic(Context context, SIMInfoWrapper simInfo) {
        if (isPermitBindService(context)) {
            int activatedSim = simInfo.getSimIndex_CurrentNetworkActivated();
            String imsi = MobileTemplate.getSubscriberId(context, activatedSim);
            if (imsi == null) {
                return;
            }

            AppTrafficService trafficService = AppTrafficService.getInstance(context);
            trafficService.setIMSI(imsi);
            trafficService.unbindTrafficService();
            trafficService.bindTrafficService();
            updateSaveingTrafficState(context);
        }
    }

    private boolean isPermitBindService(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Consts.DEFAULT_PREFERENCES_NAME,
                Context.MODE_MULTI_PROCESS);
        return preferences.getBoolean("traffica_save_switch", false);
    }

    private void updateSaveingTrafficState(Context context) {
        SharedPreferences share = context.getSharedPreferences(Consts.DEFAULT_PREFERENCES_NAME,
                Context.MODE_MULTI_PROCESS);
        share.edit().putBoolean("traffic_imsi", true).commit();
    }
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end
