package com.cydroid.softmanager.powersaver.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.Display;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.powersaver.interfaces.IPowerService;
import com.cydroid.softmanager.powersaver.mode.ModeItemInfo;
import com.cydroid.softmanager.powersaver.mode.NightModeController;
import com.cydroid.softmanager.powersaver.mode.NoneModeController;
import com.cydroid.softmanager.powersaver.mode.NormalModeController;
import com.cydroid.softmanager.powersaver.mode.SuperModeController;
import com.cydroid.softmanager.powersaver.utils.BatteryStateInfo;
import com.cydroid.softmanager.powersaver.utils.GNPushServiceHelper;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.chenyee.featureoption.ServiceUtil;

public class PowerManagerService extends Service {
    private static final String TAG = "PowerManagerService";
    private static final boolean DEBUG = true;

    public static final String ACTION_CHECK_SUPER_MODE_BOOT_STATE = "com.cydroid.softmanager.powersaver.service.ACTION_CHECK_SUPER_MODE_BOOT_STATE";
    public static final String EVENT_MODE_CHANGED = "com.cydroid.softmanager.powersaver.utils.MODE_CHANGED";
    private static final String ACTION_ENTER_NIGHT_MODE = "com.cydroid.softmanager.powersaver.service.ACTION_AUTO_ENTER_POWERSAVER";
    private static final String ACTION_EXIT_SUPER_POWER_SAVE_MODE = "com.action.exit.super.power.save.mode";

    private Context mContext;
    private AlarmManager mAlarmManager = null;
    private DisplayManager mDisplayManager;
    private HandlerThread mChangeThread;
    private PendingIntent mEnterNightModeIntent = null;
    private ChangeThreadHandler mPowerModeChangeHandler;

    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
        }

        @Override
        public void onDisplayRemoved(int displayId) {
        }

        @Override
        public void onDisplayChanged(int displayId) {
            if (mDisplayManager != null && displayId == Display.DEFAULT_DISPLAY) {
                Display curDisplay = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
                boolean screenOn = curDisplay.getState() == Display.STATE_ON;
                Log.d(TAG, "DisplayListener onReceive screenOn:" + screenOn);
                if (screenOn) {
                    Log.d(TAG, "screen on exit night mode");
                    exitNightMode();
                    // Gionee <GN_Oversea_Req> <xionghg> <2017-05-16> add for 138763 begin
                    if (Consts.gnIPFlag) {
                        if (batteryReceiverRegistered) {
                            Log.d(TAG, "screen on, unregister battery receiver");
                            unregisterBatteryReceiver();
                        }
                    }
                    // Gionee <GN_Oversea_Req> <xionghg> <2017-05-16> add for 138763 end
                } else {
                    Log.d(TAG, "screen off set night mode alarm");
                    setEnterNightModeAlarm();
                    // Gionee <GN_Oversea_Req> <xionghg> <2017-05-16> add for 138763 begin
                    if (Consts.gnIPFlag) {
                        if (needRegisterBatteryReceiver()) {
                            Log.d(TAG, "screen off, register battery receiver");
                            registerBatteryReceiver();
                        }
                    }
                    // Gionee <GN_Oversea_Req> <xionghg> <2017-05-16> add for 138763 end
                }
            }
        }
    };

    // Gionee <GN_Oversea_Req> <xionghg> <2017-05-16> add for 138763 begin
    private boolean needRegisterBatteryReceiver() {
        // gnIP and charging and in NORMAL_MODE and not registered
        boolean condition1 = Consts.gnIPFlag;
        boolean condition2 = BatteryStateInfo.isChargingNow(mContext);
        boolean condition3 = PowerModeUtils.getCurrentMode(mContext) == PowerConsts.NORMAL_MODE;
        boolean condition4 = !batteryReceiverRegistered;
        return condition1 && condition2 && condition3 && condition4;
    }

    private boolean batteryReceiverRegistered = false;

    private BatteryReceiver mBatteryReceiver;

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // return if battery level not reach 80
            int level = BatteryStateInfo.getBatteryLevel(mContext);
            if (level < 80) {
                Log.d(TAG, "level=" + level + ", return");
                return;
            }

            Log.d(TAG, "change power mode to NONE_MODE when charging to " + level + "%");
            // check and process supermode boot check at mode change thread,if not checked when boot
            postTaskToChangeThread(new SuperModeCheckTask("firstChange"));
            // mode change at mode change thread
            postTaskToChangeThread(new ModeChangeTask(PowerConsts.NORMAL_MODE, PowerConsts.NONE_MODE));
            Log.d(TAG, "charge enough, unregister battery receiver");
            unregisterBatteryReceiver();
        }
    }

    private void registerBatteryReceiver() {
        mBatteryReceiver = new BatteryReceiver();
        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBatteryReceiver, batteryFilter);
        batteryReceiverRegistered = true;
    }

    private void unregisterBatteryReceiver() {
        mContext.unregisterReceiver(mBatteryReceiver);
        mBatteryReceiver = null;
        batteryReceiverRegistered = false;
    }
    // Gionee <GN_Oversea_Req> <xionghg> <2017-05-16> add for 138763 end

    @Override
    public void onCreate() {
        try {
//            android.os.Debug.waitForDebugger();
            mContext = PowerManagerService.this;
            mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
            checkNightMode();
            mDisplayManager.registerDisplayListener(mDisplayListener, null);
            // initGNPushService();
            super.onCreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkNightMode() {
        Display curDisplay = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
        boolean screenOn = curDisplay.getState() == Display.STATE_ON;
        if (screenOn) {
            Log.d(TAG, "service start check night mode,exit");
            exitNightMode();
        }
    }

    private void initGNPushService() {
        // Gionee <yangxinruo> <2016-6-14> add for CR01707701 begin
        GNPushServiceHelper gNPushHelper = GNPushServiceHelper.getInstance();
        gNPushHelper.initPushSettings(mContext);
        // Gionee <yangxinruo> <2016-6-14> add for CR01707701 end
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
        // Binder functions for remote calling from softmanager main process
        return new IPowerService.Stub() {

            @Override
            public List<ModeItemInfo> getConfigList(int mode) throws RemoteException {
                List<ModeItemInfo> res = null;
                if (mode == PowerConsts.NORMAL_MODE) {
                    res = (new NormalModeController(mContext)).getPowerModeConfigList();
                }
                return res;
            }

            @Override
            public void setConfigList(int mode, List<ModeItemInfo> configList) throws RemoteException {
                if (mode == PowerConsts.NORMAL_MODE) {
                    (new NormalModeController(mContext)).setPowerModeConfigList(configList);
                    if (PowerModeUtils.getCurrentMode(mContext) == PowerConsts.NORMAL_MODE) {
                        postTaskToChangeThread(new ModeUpdateTask(mode));
                    }
                }
            }

            @Override
            public boolean isConfigDiffFromDefault(int mode) throws RemoteException {
                if (mode == PowerConsts.NORMAL_MODE) {
                    return (new NormalModeController(mContext)).isPowerModeConfigDiffFromDefault();
                }
                return false;
            }

            @Override
            public float getModeConfigWeight(int mode) throws RemoteException {
                float res = 1f;
                if (mode == PowerConsts.NONE_MODE) {
                    res = (new NoneModeController(mContext)).getPowerModeConfigWeight();
                } else if (mode == PowerConsts.NORMAL_MODE) {
                    res = (new NormalModeController(mContext)).getPowerModeConfigWeight();
                }
                return res;
            }

            @Override
            public float getModeDefaultWeight(int mode) throws RemoteException {
                float res = 1f;
                if (mode == PowerConsts.NONE_MODE) {
                    res = (new NoneModeController(mContext)).getPowerModeDefaultWeight();
                } else if (mode == PowerConsts.NORMAL_MODE) {
                    res = (new NormalModeController(mContext)).getPowerModeDefaultWeight();
                }
                Log.d(TAG, "get mode = " + mode + " getDefaultWeight=" + res);
                return res;
            }

        };
        // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
    }

    private void postTaskToChangeThread(Runnable task) {
        if (mChangeThread == null) {
            mChangeThread = new HandlerThread("PowerModeChangeThread");
            mChangeThread.start();
            mPowerModeChangeHandler = new ChangeThreadHandler(mChangeThread.getLooper());
        }
        mPowerModeChangeHandler.addTask(task);
    }

    private class ChangeThreadHandler extends Handler {

        private final Queue<Runnable> mTaskList;
        private boolean mIsAvailable = true;

        public ChangeThreadHandler(Looper looper) {
            super(looper);
            mTaskList = new LinkedList<Runnable>();
        }

        public void addTask(Runnable task) {
            mTaskList.add(task);
            checkAndExecTask();
        }

        private void checkAndExecTask() {
            if (!mIsAvailable) {
                Log.d(TAG, "last task not finished ");
                return;
            }
            Runnable toBeExecTask = mTaskList.poll();
            if (toBeExecTask != null) {
                mIsAvailable = false;
                Log.d(TAG, "exec task =  " + toBeExecTask);
                post(toBeExecTask);
            } else {
                Log.d(TAG, "no task to exec ");
            }
        }

        public void onTaskFinish() {
            mIsAvailable = true;
            Log.d(TAG, "call next task");
            checkAndExecTask();
        }
    }

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    class ModeUpdateTask implements Runnable {
        private final int mode;

        ModeUpdateTask(int mode) {
            this.mode = mode;
        }

        @Override
        public void run() {
            if (mode == PowerConsts.NORMAL_MODE) {
                NormalModeController normalModeController = new NormalModeController(mContext);
                normalModeController.applyPowerModeConfig(new Runnable() {
                    @Override
                    public void run() {
                        notifyPowerModeUpdated(mode);
                    }
                });
            } else {
                Log.d(TAG, "wrong mode for ModeUpdateTask!!!");
                mPowerModeChangeHandler.onTaskFinish();
            }
        }
    }
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            super.onStartCommand(intent, flags, startId);
            ServiceUtil.handleStartForegroundServices(this);
            parserModeChangeIntent(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Service.START_STICKY;
    }

    private void parserModeChangeIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        Log.d(TAG, "PowerManagerService process intent action = " + action);
        if (action.equals(PowerConsts.MODE_CHANGE_INTENT)) {
            Bundle bundle = intent.getExtras();
            // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
            // int from = bundle.getInt("from");
            int from = PowerModeUtils.getCurrentMode(mContext);
            int to = bundle.getInt("to");
            Log.d(TAG, "request switch from " + from + " mode(current) to " + to);
            postTaskToChangeThread(new ModeChangeTask(from, to));
        } else if (action.equals(ACTION_CHECK_SUPER_MODE_BOOT_STATE)) {
            String reason = intent.getStringExtra("reason");
            // check and process supermode boot check at mode change thread
            postTaskToChangeThread(new SuperModeCheckTask(reason));
            // modeChangeBootCheck(reason);
        } else if (action.equals(ACTION_ENTER_NIGHT_MODE)) {
            startNightMode();
        } else if (action.equals(ACTION_EXIT_SUPER_POWER_SAVE_MODE)) {
            int from = PowerModeUtils.getCurrentMode(mContext);
            int to = PowerConsts.NONE_MODE;
            Log.d(TAG, "request switch from " + from + " mode(current) to " + to);
            postTaskToChangeThread(new ModeChangeTask(from, to));
        }
        Log.d(TAG, "leave parserModeChangeIntent");
    }

    // Gionee <yangxinruo> <2015-10-13> add for CR01565955 begin
    class ModeChangeTask implements Runnable {
        private final int from;
        private final int to;

        ModeChangeTask(int from, int to) {
            this.from = from;
            this.to = to;
        }

        // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
        @Override
        public void run() {
            try {


                Log.d(TAG, "in modechange thread for parser mode-change Intent, change mode from = " + from
                        + " --> to = " + to);
                // Gionee <yangxinruo> <2015-08-13> add for CR01546824 begin
                if (PowerModeUtils.getModeProcessing(mContext) != PowerConsts.SUPER_MODE_DONE && false) {
                    Log.d(TAG, "mode-change Intent, supermode is proccessing,cancel this intent");
                    mPowerModeChangeHandler.onTaskFinish();
                    return;
                }
                int currentMode = PowerModeUtils.getCurrentMode(mContext);
                // Gionee <yangxinruo> <2015-08-13> add for CR01546824 end
                if (from != PowerModeUtils.getCurrentMode(mContext)) {
                    Log.d(DEBUG, TAG, "mode-change Intent, fromMode != currentMode,cancel this intent");
                    mPowerModeChangeHandler.onTaskFinish();
                    return;
                }
                // Gionee <yangxinruo> <2015-09-09> modify for CR01547438 begin
                if (from == to) {
                    Log.d(DEBUG, TAG, "mode-change Intent, fromMode == toMode,cancel this intent");
                    mPowerModeChangeHandler.onTaskFinish();
                    return;
                }
                // Gionee <yangxinruo> <2015-09-09> modify for CR01547438 end
                if (to > from) {
                    PowerModeUtils.pushModeToStack(mContext, to);
                    if (to == PowerConsts.NORMAL_MODE) {
                        (new NormalModeController(mContext)).start(new Runnable() {
                            @Override
                            public void run() {
                                notifyPowerModeUpdated(to);
                            }
                        });
                    } else if (to == PowerConsts.SUPER_MODE) {
                        (new SuperModeController(mContext, mChangeThread.getLooper())).start(new Runnable() {

                            @Override
                            public void run() {
                                notifyPowerModeUpdated(to);
                            }
                        });
                    } else {
                        Log.d(TAG, "invalid to mode!!");
                        mPowerModeChangeHandler.onTaskFinish();
                    }
                } else {
                    ArrayList<Integer> appliedModes = new ArrayList<Integer>();
                    int toMode = from;
                    while (toMode != to) {
                        appliedModes.add(toMode);
                        toMode = PowerModeUtils.popModeFromStack(mContext);
                        if (toMode < PowerConsts.NONE_MODE) {
                            toMode = PowerConsts.NONE_MODE;
                            break;
                        }
                    }
                    if (from == PowerConsts.NORMAL_MODE) {
                        (new NormalModeController(mContext)).restore(new Runnable() {
                            @Override
                            public void run() {
                                notifyPowerModeUpdated(to);
                            }
                        });
                    } else if (from == PowerConsts.SUPER_MODE) {
                        (new SuperModeController(mContext, mChangeThread.getLooper())).restore(new Runnable() {
                            @Override
                            public void run() {
                                notifyPowerModeUpdated(to);
                            }
                        }, appliedModes, toMode);
                    } else {
                        Log.d(TAG, "invalid from mode!!");
                        mPowerModeChangeHandler.onTaskFinish();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
// Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end
        }
    }
// Gionee <yangxinruo> <2015-10-13> add for CR01565955 end

    class SuperModeCheckTask implements Runnable {
        private final String mReason;

        public SuperModeCheckTask(String reason) {
            mReason = reason;
        }

        @Override
        public void run() {
            Log.d(TAG, "in modechange thread for checking supermode status reason= " + mReason);
            (new SuperModeController(mContext, mChangeThread.getLooper())).checkAndRestoreWhenBoot(mReason,
                    new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "mode change boot check finished,reset exception flag");
                            PowerModeUtils.unsetSuperModeExceptionRebootFlag(mContext);
                            mPowerModeChangeHandler.onTaskFinish();
                        }
                    });
        }

    }

    private void notifyPowerModeUpdated(int mode) {
        mPowerModeChangeHandler.onTaskFinish();
        Intent updateIntent = new Intent(EVENT_MODE_CHANGED);
        updateIntent.putExtra("mode", mode);
        mContext.sendBroadcast(updateIntent);
    }

    private void exitNightMode() {
        (new NightModeController(mContext)).restore();
        if (mEnterNightModeIntent == null) {
            return;
        }
        mAlarmManager.cancel(mEnterNightModeIntent);
    }

    private void startNightMode() {
        (new NightModeController(mContext)).start();
    }

    public void setEnterNightModeAlarm() {
        NightModeController nightController = new NightModeController(mContext);
        MainProcessSettingsProviderHelper mainProcessSettingsProviderHelper = new MainProcessSettingsProviderHelper(
                mContext);
        if (!mainProcessSettingsProviderHelper.hasKey("is_first_utilization")) {
            Log.d(TAG, "network not enable ,can not set night mode alarm!");
            return;
        }
        if (!nightController.isNightModeSwitchEnable()) {
            return;
        }
        long enterTime = nightController.getStartTime();
        Log.d(TAG, "set enter nightmode alarm after " + enterTime / 3600000 + "H"
                + enterTime % 3600000 / 60000 + "m");
        Intent serviceIntent = new Intent(mContext, PowerManagerService.class);
        serviceIntent.setAction(ACTION_ENTER_NIGHT_MODE);
        mEnterNightModeIntent = PendingIntent.getService(this, 0, serviceIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (mEnterNightModeIntent == null) {
            Log.d(TAG, "enter night mode intent is null!!!");
            return;
        }
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + enterTime,
                mEnterNightModeIntent);
    }

    @Override
    public void onDestroy() {
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        stopForeground(true);
        super.onDestroy();
    }
}
