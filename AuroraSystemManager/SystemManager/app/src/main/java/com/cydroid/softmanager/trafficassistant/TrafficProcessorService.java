//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficPreference;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.chenyee.featureoption.ServiceUtil;
public class TrafficProcessorService extends Service {
    private static final String TAG = "TrafficProcessorService";

    private static final String serviceName = "com.cydroid.softmanager.trafficassistant.TrafficProcessorService";
    private static final int MSG_STOP = 0;
    private static final int MSG_WARNING = 1;
    private static final int MSG_SETTING = 2;
    private static final int MSG_START_RUNNABLE = 3;
    private static final int MSG_START_RUNNABLE1 = 4;
    private static final int MSG_START_RUNNABLE2 = 5;
    private static final boolean isRunning = true;
    private int mActivatedSimIndex;
    private Context mContext;
    private MainHandler mHandler;
    private SharedPreferences mShare;
    private String mTag;

    private static final int DELAY_TIME = 60000;
    private boolean mSettingFlag = true;
    private int mTimeZone = 0;
    private boolean mWarningStopFlag = false;

    // Gionee: mengdw <2015-11-11> add for CR01589343 begin
    private final String mWarnPercent = "80";
    private final String mWarnValue = "";
    // Gionee: mengdw <2015-11-11> add for CR01589343 end

    private TrafficCalibrateControler mCalibrateController;
    private TrafficSettingControler mSettingController;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // 清除handler的引用，即可停止循环
        if (mHandler != null) {
            mHandler.setService(null);
        }
        // terminateThread();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTag = Util.toString(this);
        // startThread();

        mContext = TrafficProcessorService.this;

        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(mContext);
        mActivatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
        // Gionee: mengdw <2016-05-18> add for CR01696960 begin
        boolean isSetted = TrafficPreference.getSimBooleanPreference(mContext, mActivatedSimIndex,
                TrafficPreference.KEY_TRAFFIC_PACKAGE_SETTED_FLAG, false);
        if (!isSetted) {
            Log.d(TAG, "onCreate package not setted");
            return;
        }
        // Gionee: mengdw <2016-05-18> add for CR01696960 end
        if (!isValid(simInfo, mActivatedSimIndex)) {
            // Gionee: mengdw <2015-11-05> add log for CR01580861 begin
            Log.d(TAG, "onCreate isValid return");
            // Gionee: mengdw <2015-11-05> add log for CR01580861 end
            return;
        }
        mCalibrateController = TrafficCalibrateControler.getInstance(mContext);
        mSettingController = TrafficSettingControler.getInstance(mContext);

        mShare = mContext.getSharedPreferences("com.cydroid.softmanager_preferences",
                Context.MODE_MULTI_PROCESS);

        mHandler = new MainHandler(this);

        if (!mCalibrateController.isTafficPackageSetted(mContext, mActivatedSimIndex)) {
            mSettingFlag = true;
            mHandler.sendEmptyMessage(MSG_START_RUNNABLE1);
        } else if (getMonitorFlag(mShare, mActivatedSimIndex)) {
            int mTimeZone = 0;
            mHandler.sendEmptyMessage(MSG_START_RUNNABLE2);
        } else {
            mWarningStopFlag = false;
            mHandler.sendEmptyMessage(MSG_START_RUNNABLE);
        }
    }

    private static class MainHandler extends Handler {
        private TrafficProcessorService mService;

        MainHandler(TrafficProcessorService service) {
            setService(service);
        }

        void setService(TrafficProcessorService service) {
            mService = service;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mService == null) {
                Log.e(TAG, "handleMessage: mService=null! what=" + msg.what);
                return;
            }
            switch (msg.what) {
                case MSG_STOP:
                    mService.startPopWindow();
                    break;
                case MSG_WARNING:
                    mService.showWarningNotification();
                    break;
                case MSG_SETTING:
                    mService.showSettingNotification();
                    break;
                case MSG_START_RUNNABLE:
                    mService.startRunnable();
                    break;
                case MSG_START_RUNNABLE1:
                    mService.startRunnable1();
                    break;
                case MSG_START_RUNNABLE2:
                    mService.startRunnable2();
                    break;
                default:
                    break;
            }
        }
    }

    private void startPopWindow() {
        Intent intent = new Intent(mContext, TrafficPopWindows.class);
        intent.putExtra("sim_activatedindex", mActivatedSimIndex);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void showWarningNotification() {
        // Gionee: mengdw <2015-11-05> add log for CR01580861 begin
        Log.d(TAG, "show warning notification");
        // Gionee: mengdw <2015-11-05> add log for CR01580861 end
        //guoxt moidyf for oversea begin
        /*
        NotificationController notification = NotificationController.getDefault(mContext);
        notification.setSimIndex(mActivatedSimIndex);
        notification.setSoundId(false);
        // Gionee: mengdw <2015-11-11> add for CR01589343 begin
        String[] data = TrafficPreference.getPreference(mContext, mActivatedSimIndex);
        try {
            mWarnPercent = data[1];
            mWarnValue = (Integer.valueOf(data[0]) * Integer.valueOf(data[1])) / 100 + "";
        } catch (Exception ex) {
            Log.d(TAG,"show MSG_WARNING Exception e=" + ex.toString());
        }
        String title = String.format(mContext.getString(R.string.notification_warn_exceed_warning_title),
            mWarnPercent, mWarnValue);
        notification.setTitle(title);
        notification.setClass(com.cydroid.softmanager.trafficassistant.sell.TrafficBuyActivity.class);
        // Gionee: mengdw <2015-11-11> add for CR01589343 end
        notification.setContent(R.string.notification_warn_exceed_warning_content);
        notification.setWarnTickerText(title);
        notification.setSmallIcon(R.drawable.notify);
        notification.show(notification.getWarningId());
        */

        NotificationController notification = NotificationController.getDefault(mContext);
        notification.setSimIndex(mActivatedSimIndex);
        notification.setSoundId(false);
        notification.setTitle(R.string.notification_warn_exceed_warning_title);
        notification.setContent(R.string.notification_warn_exceed_warning_content);
        notification.setTickerText(R.string.notification_warn_exceed_warning_title);
        notification.setSmallIcon(R.drawable.notify);
        notification
                .setClass(com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity.class);
        notification.show(notification.getWarningId());
        //guoxt moidyf for oversea end
    }

    private void showSettingNotification() {
        // Gionee: mengdw <2015-11-05> add log for CR01580861 begin
        Log.d(TAG, "receive MSG_SETTING show  notification");
        // Gionee: mengdw <2015-11-05> add log for CR01580861 end
        NotificationController notification1 = NotificationController.getDefault(mContext);
        notification1.setSimIndex(mActivatedSimIndex);
        notification1.setSoundId(false);
        notification1.setTitle(R.string.popup_notification_title);
        notification1.setContent(R.string.popup_notification_ticker_title);
        notification1.setSmallIcon(R.drawable.notify);
        notification1.setTickerText(R.string.popup_notification_title);
        notification1
                .setClass(com.cydroid.softmanager.trafficassistant.TrafficLimitActivity.class);
        notification1.show(notification1.getBootedId());
    }

    private void startRunnable1() {
        Log.d(TAG, "startRunnable1:");
        // Gionee: mengdw <2016-05-11> modify for CR01690197 begin
        new Thread(new Runnable() {
            @Override
            public void run() {
                int[] date = TrafficassistantUtil.initDateInterval(1);
                String str = TrafficassistantUtil.getSimNotification(mActivatedSimIndex);
                MainProcessSettingsProviderHelper providerHelper = new MainProcessSettingsProviderHelper(
                        mContext);
                float actualFlow = TrafficassistantUtil.trafficStatistic(mContext, date, mActivatedSimIndex);
                if (mCalibrateController.isTafficPackageSetted(mContext, mActivatedSimIndex)) {
                    return;
                }
                if (actualFlow >= 1 && mSettingFlag && !providerHelper.getBoolean(str, false)) {
                    providerHelper.putBoolean(str, true);
                    // Gionee: mengdw <2016-03-04> delete for CR01640989 begin
                    Log.d(TAG, " startRunnable1 sendMessage(MSG_SETTING);");
                    // sendMessage(MSG_SETTING);
                    // Gionee: mengdw <2016-03-04> delete for CR01640989 end
                    mSettingFlag = false;
                }
            }
        }).start();
        // Gionee: mengdw <2016-05-11> modify for CR01690197 end
        // Gionee: mengdw <2016-06-06> modify for CR01714537 begin
        mSettingController.commitTrafficNotiAction(mContext);
        mHandler.sendEmptyMessageDelayed(MSG_START_RUNNABLE1, DELAY_TIME);
        // Gionee: mengdw <2016-06-06> modify for CR01714537 end
    }

    private void startRunnable2() {
        Log.d(TAG, "startRunnable2:");
        mTimeZone++;
        if (mTimeZone >= 6) {
            mSettingController.commitTrafficNotiAction(mContext);
            mTimeZone = 0;
        }
        mHandler.sendEmptyMessageDelayed(MSG_START_RUNNABLE2, DELAY_TIME / 20);
    }

    private void startRunnable() {
        // Gionee: mengdw <2016-05-11> modify for CR01690197 begin
        new Thread(new Runnable() {
            @Override
            public void run() {
                int totalFlow = mCalibrateController.getCommonTotalTaffic(mContext, mActivatedSimIndex);
                float used = mCalibrateController.getCommonUsedTaffic(mContext, mActivatedSimIndex);
                int percent = mCalibrateController.getWarnPercent(mContext, mActivatedSimIndex);
                boolean stopFlag = mCalibrateController.isStopExhaustFlag(mContext, mActivatedSimIndex);
                boolean isOnlyLeft = mCalibrateController.isCommonOnlyLeft(mContext, mActivatedSimIndex);
                boolean isStop = used >= totalFlow;
                Log.d(mTag, " startRunnable isStop: " + isStop + " stopFlag: " + stopFlag
                        + " isOnlyLeft=" + isOnlyLeft);
                if (isStop && !stopFlag && !isOnlyLeft) {
                    // Gionee: mengdw <2015-11-05> add log for CR01580861 begin
                    Log.d(TAG, "startRunnable send MSG_STOP ");
                    // Gionee: mengdw <2015-11-05> add log for CR01580861 end
                    mHandler.sendEmptyMessage(MSG_STOP);
                    mCalibrateController.setStopExhaustFlag(mContext, mActivatedSimIndex, true);
                }
                float warnValue = (float) ((totalFlow * percent) / 100.0);
                boolean isWarning = used >= warnValue;
                // Gionee: mengdw <2015-11-05> add log for CR01580861 begin
                boolean saveFlag = mCalibrateController.isStopWarningFlag(mContext, mActivatedSimIndex);
                Log.d(mTag, "startRunnable  mWarningStopFlag=" + mWarningStopFlag + " isWarning="
                        + isWarning + " isStop=" + isStop + " saveFlag=" + saveFlag + " totalFlow="
                        + totalFlow + " used=" + used + " warnValue=" + warnValue);
                // Gionee: mengdw <2015-11-05> add log for CR01580861 end
                if (!mWarningStopFlag && isWarning && !isStop && !isOnlyLeft) {
                    if (!saveFlag) {
                        mHandler.sendEmptyMessage(MSG_WARNING);
                        mCalibrateController.setStopWarningFlag(mContext, mActivatedSimIndex, true);
                        mWarningStopFlag = true;
                    }
                }
            }
        }).start();
        // Gionee: mengdw <2016-05-11> modify for CR01690197 end
        // Gionee: mengdw <2016-06-06> modify for CR01714537 begin
        mSettingController.commitTrafficNotiAction(mContext);
        mHandler.sendEmptyMessageDelayed(MSG_START_RUNNABLE, DELAY_TIME);
    }

    private boolean getMonitorFlag(SharedPreferences share, int simIndex) {
        int value = mCalibrateController.getFlowlinkFlag(mContext, simIndex);
        return value != 0;
    }

    private void print(String symbol, String value) {
        int[] times = TimeFormat.getNowTimeArray();
        String time = times[3] + ":" + times[4] + ":" + times[5];
        Log.d(TAG, symbol + "-->" + time + " " + isRunning + " , " + value);
    }

//    private static void startThread() {
//        isRunning = true;
//    }

//    private static void terminateThread() {
//        isRunning = false;
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(TrafficProcessorService.this);
        return super.onStartCommand(intent, flags, startId);
    }

    public static void processIntent(Context context, boolean flag) {
        if (flag) {
            startIntent(context);
        } else {
            stopIntent(context);
        }
    }

    private static void startIntent(Context context) {
        if (!TrafficassistantUtil.isServiceRunning(context, serviceName)) {
            Log.d(TAG, " start service");
            waitTime(3000);

            Intent i = new Intent(context, TrafficProcessorService.class);
            ServiceUtil.startForegroundService(context,i);
        } else {
            Log.d(TAG, " service is already start");
        }
    }

    private static void stopIntent(Context context) {
        if (TrafficassistantUtil.isServiceRunning(context, serviceName)) {
            Log.d(TAG, " stop service");
            waitTime(3000);

            Intent i = new Intent(context, TrafficProcessorService.class);
            context.stopService(i);
        } else {
            Log.d(TAG, " service is already stop ");
        }
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
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end