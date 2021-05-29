package com.cydroid.softmanager.powersaver.mode;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.powersaver.activities.WaitingActivity;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItem;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemDarkTheme;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemDarkWallpaper;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemDisableApps;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemPause;
import com.cydroid.softmanager.powersaver.utils.CpuInfoUtils;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.powersaver.utils.StatusbarController;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.provider.CyeeSettings;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;

public class SuperModeController {
    private static final String SUPER_LAUNCHER_PACKAGE_NAME = "com.cydroid.powersaver.launcher";
    private static final String TAG = "SuperModeController";
    private final Context mContext;
    private final Handler mSuperModeHandler;
    private final Handler mMainThreadHandler;
    private static final int EVENT_START_SUPERMODE = 1;
    private static final int EVENT_EXIT_SUPERMODE = 2;
    private static final int POWERSAVE_COLOR_CHANGE_MAX_TIME_OUT = 20 * 1000;
    private static final int POWERSAVE_LAUNCHER_CHANGE_MAX_TIME_OUT = 30 * 1000;
    private static final int TOTAL_TIMEOUT = 2 * 60 * 1000;

    public static final String BOARDCAST_EXIT_SUPERMODE_FINISH = "com.cydroid.softmanager.powersaver.utils.EXIT_SUPERMODE_FINISH";
    public static final String BOARDCAST_START_SUPERMODE_FINISH = "com.cydroid.softmanager.powersaver.utils.START_SUPERMODE_FINISH";
    public static final String BOARDCAST_SUPERMODE_ABORT = "com.cydroid.softmanager.powersaver.utils.SUPERMODE_ABORT";

    private ModeItemsController mSuperModeControlItems;
    private ModeItemsController mSuperModeItems;
    private ColorChangeReceiver mColorChangedReceiver;
    private Runnable mCallback;

    private boolean mIsRebootDialogShow = false;

    public enum State {
        START, EXIT, NONE
    }

    private State mState;
    private TotalTimeoutTimer mTotalTimeoutTimer;
    private int mRestoreToMode;

    public SuperModeController(Context context, Looper looper) {
        mContext = context;
        mSuperModeHandler = new SuperModeHandler(looper);
        mState = State.NONE;
        mMainThreadHandler = new Handler(mContext.getMainLooper());
    }

    public void start(Runnable afterCallback) {
        Log.d(TAG, "start supermode ----->");
        String stateStr = checkSuperModeState();
        if (!stateStr.isEmpty()) {
            emergencyRestore("start_" + stateStr);
        }

        PowerModeUtils.setCurrentMode(mContext, PowerConsts.SUPER_MODE);
        PowerModeUtils.setModeProcessing(mContext, PowerConsts.SUPER_MODE_PROCESSING);
        StatusbarController.disableControlCenter(mContext);
        StatusbarController.setStatusbarState(mContext,
                StatusBarManager.DISABLE_NONE | StatusBarManager.DISABLE_EXPAND
                        | StatusBarManager.DISABLE_HOME | StatusBarManager.DISABLE_BACK
                        | StatusBarManager.DISABLE_RECENT);
        startWaitingActivity();
        mTotalTimeoutTimer = new TotalTimeoutTimer(mMainThreadHandler);
        mTotalTimeoutTimer.set(TOTAL_TIMEOUT);

        Log.d(TAG, "remove recent and running task first");
        ArrayList<String> taskWhiteList = new ArrayList<String>();
        taskWhiteList.addAll(SuperModeUtils.getSuperModeTaskWhitelist(mContext));
        if (!taskWhiteList.contains(mContext.getPackageName())) {
            // do not remove self task
            taskWhiteList.add(mContext.getPackageName());
        }
        SuperModeUtils.killRunningAndRecentTask(mContext, -1, taskWhiteList);

        mState = State.START;
        mCallback = afterCallback;
        mSuperModeItems = getSuperModeController(mContext);
//        configDarkTheme(EVENT_START_SUPERMODE);
        configEnableSuperModeLauncher(EVENT_START_SUPERMODE);
        configDisableApps();
        changeStartSuperModeArrangment(mSuperModeItems);
        mSuperModeItems.saveCheckPoint();
        checkStartSuperModeConfigs(mSuperModeItems);
    }

    private void changeStartSuperModeArrangment(ModeItemsController modeItemsController) {
        modeItemsController.moveToLast("CpuLimit");
        modeItemsController.moveToFirst("HomeKeyValue");
        modeItemsController.moveToFirst("VoiceControl");
        modeItemsController.moveToFirst("Gesture");
        modeItemsController.moveToFirst("FloatTouch");
    }

    private void configEnableSuperModeLauncher(final int event) {
        PowerModeItem enableSuperModeLauncherItem = mSuperModeItems.getItemByName("EnableSuperModeLauncher");
        if (enableSuperModeLauncherItem == null) {
            return;
        }
        PowerModeItem pauseItem = PowerModeItemSimpleFactory.getInstanceByName(mContext, "SUPER", "Pause");
        if (pauseItem == null) {
            return;
        }
        if (!(pauseItem instanceof PowerModeItemPause)) {
            return;
        }
        enableSuperModeLauncherItem.setBeforeCallback(new Runnable() {
            @Override
            public void run() {
                SuperLauncherChangeReceiver launcherChangeReceiver = new SuperLauncherChangeReceiver(mContext,
                        mSuperModeHandler, event, POWERSAVE_LAUNCHER_CHANGE_MAX_TIME_OUT);
            }
        });
        ((PowerModeItemPause) pauseItem).setName("pause_after_enableSuperModeLauncher");
        mSuperModeItems.addItemAfterItem(pauseItem, "EnableSuperModeLauncher");
    }

    private void startWaitingActivity() {
        Intent intent = new Intent();
        intent.setClassName(mContext.getPackageName(), WaitingActivity.class.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
    }

    private String checkSuperModeState() {
        String defLauncher = Util.getDefaultLauncherPkg(mContext);
        int currentMode = PowerModeUtils.getCurrentMode(mContext);
        boolean isSuperModeDone = (PowerModeUtils.getModeProcessing(mContext) == PowerConsts.SUPER_MODE_DONE);
        Log.d(TAG, "enter checkSuperModeState  defLauncher=" + defLauncher + " currentMode=" + currentMode
                + " isSuperModeDone=" + isSuperModeDone);
        if (!isSuperModeDone) {
            return "supermode_unfinished";
        }
        // Gionee <yangxinruo> <2016-2-23> modify for CR01639403 begin
        if (!defLauncher.equals(SUPER_LAUNCHER_PACKAGE_NAME)) {
            if (currentMode == PowerConsts.SUPER_MODE) {
                Log.e(TAG, "WRONG! superMode with no powersaver launcher, restore ");
                return "supermode_wrong_launcher";
            }
        } else {
            if (currentMode != PowerConsts.SUPER_MODE) {
                Log.e(TAG, "WRONG! powersaver launcher with no super mode, restore");
                return "nonemode_wrong_launcher";
            }
        }
        return "";
    }

    private static ModeItemsController getSuperModeController(Context context) {
        String[] superItemArray = context.getResources().getStringArray(R.array.powermode_SUPER);
        ModeItemsController superModeItems = new ModeItemsController(context, "SUPER", superItemArray);
        superModeItems.resetConfigToDefault();
        return superModeItems;
    }

    private void configDarkTheme(final int event) {
        PowerModeItem darkItem = mSuperModeItems.getItemByName("DarkTheme");
        if (darkItem == null) {
            return;
        }
        PowerModeItem pauseItem = PowerModeItemSimpleFactory.getInstanceByName(mContext, "SUPER", "Pause");
        if (pauseItem == null) {
            return;
        }
        if (!(pauseItem instanceof PowerModeItemPause)) {
            return;
        }

        darkItem.setBeforeCallback(new Runnable() {
            @Override
            public void run() {
                ColorChangeReceiver colorChangeReceiver = new ColorChangeReceiver(mContext, mSuperModeHandler,
                        event, POWERSAVE_COLOR_CHANGE_MAX_TIME_OUT);
            }
        });
        ((PowerModeItemPause) pauseItem).setName("pause_after_darktheme");
        mSuperModeItems.addItemAfterItem(pauseItem, "DarkTheme");
    }

    private void configDisableApps() {
        PowerModeItem diableAppsItem = mSuperModeItems.getItemByName("DisableApps");
        if (diableAppsItem == null) {
            return;
        }
        if (!(diableAppsItem instanceof PowerModeItemDisableApps)) {
            return;
        }
        ArrayList<String> excludeList = new ArrayList<String>();
        List<String> superWhitelist = SuperModeUtils.getPowerSaveWhiteList(mContext);
        excludeList.addAll(superWhitelist);
        // Gionee xionghg add for power saving optimization begin
        if (Consts.SUPPORT_NEW_LAUNCHER) {
            List<String> moreWhiteList = HelperUtils.getPowerSaveMoreWhiteList(mContext);
            Log.d(TAG, "getExcludeList: add more white list, size is: " + moreWhiteList.size());
            excludeList.addAll(moreWhiteList);
        }
        // Gionee xionghg add for power saving optimization end        
        InputMethodInfo info = HelperUtils.getDefInputMethod(mContext);
        if (info != null) {
            excludeList.add(info.getPackageName());
        }
        String liveWallpaperPkg = HelperUtils.getLivePaperPkgName(mContext);
        if (liveWallpaperPkg != null && !liveWallpaperPkg.isEmpty()) {
            excludeList.add(liveWallpaperPkg);
        }
        ((PowerModeItemDisableApps) diableAppsItem).setExcludeApps(excludeList);
    }

    private void checkStartSuperModeConfigs(ModeItemsController modeItems) {
        Log.d(TAG, "continue start supermode");
        modeItems.applyConfig();
        if (modeItems.getState() != ModeItemsController.State.FINISH) {
            Log.d(TAG, "start supermode config paused!");
        } else {
            Log.d(TAG, "start supermode config finished");
            finishStart();
        }
    }

    private void finishStart() {
        Log.d(TAG, "----->start supermode finished,send broadcast");

        ArrayList<String> shouldKillAppList = new ArrayList<String>();
        shouldKillAppList.addAll(SuperModeUtils.getStartSuperModeForceKillList(mContext));
        killExplicityApps(shouldKillAppList);

        PowerModeUtils.setModeProcessing(mContext, PowerConsts.SUPER_MODE_DONE);
        Intent startIntent = new Intent(BOARDCAST_START_SUPERMODE_FINISH);
        mContext.sendBroadcast(startIntent);
        finishWaitingActivity();

        mTotalTimeoutTimer.unset();
        if (mCallback != null) {
            mCallback.run();
        }

        Log.d(TAG, "----->start supermode finished, enable statusBar");
        StatusbarController.setStatusbarState(mContext, StatusBarManager.DISABLE_NONE);
        startSuperModeLauncher();
        Log.d(TAG, "remove recent and running task when finished");
        ArrayList<String> taskWhiteList = new ArrayList<String>();
        taskWhiteList.addAll(SuperModeUtils.getSuperModeTaskWhitelist(mContext));
        if (!taskWhiteList.contains("com.cydroid.powersaver.launcher")) {
            // do not superlauncher
            taskWhiteList.add("com.cydroid.powersaver.launcher");
        }
        if (!taskWhiteList.contains("com.cydroid.powersaver.launcher")) {
            // do not remove self
            taskWhiteList.add(mContext.getPackageName());
        }
        taskWhiteList.addAll(SuperModeUtils.getSuperModeTaskComponentWhitelist(mContext));
        SuperModeUtils.killRunningAndRecentTask(mContext, -1, taskWhiteList);
    }

    private void startSuperModeLauncher() {
        Intent superHome = new Intent(Intent.ACTION_MAIN);
        superHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        superHome.addCategory(Intent.CATEGORY_HOME);
        if (mContext.getPackageManager().resolveActivity(superHome, 0) == null) {
            Log.d(TAG, "com.cydroid.powersaver.launcher is disabled,do not send intent.");
            return;
        }
        superHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        superHome.putExtra("start_from_softmanager", true);
        mContext.startActivity(superHome);
    }

    private void finishWaitingActivity() {
        Intent finishIntent = new Intent(WaitingActivity.ACTION_FINISH_SELF);
        mContext.sendBroadcast(finishIntent);
    }

    private void killExplicityApps(ArrayList<String> shouldKillAppList) {
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (String pkgName : shouldKillAppList) {
            Log.d(TAG, "into super mode force kill ---->" + pkgName);
            activityManager.forceStopPackage(pkgName);
        }
    }

    public void restore(Runnable afterCallback, ArrayList<Integer> appliedModes, int toMode) {
        Log.d(TAG, "exit supermode -----> to " + toMode);
        String stateStr = checkSuperModeState();
        mRestoreToMode = toMode;
        if (!stateStr.isEmpty()) {
            emergencyRestore("exit_" + stateStr);
        }

        StatusbarController.disableControlCenter(mContext);
        StatusbarController.setStatusbarState(mContext,
                StatusBarManager.DISABLE_NONE | StatusBarManager.DISABLE_EXPAND
                        | StatusBarManager.DISABLE_HOME | StatusBarManager.DISABLE_BACK
                        | StatusBarManager.DISABLE_RECENT);
        PowerModeUtils.setModeProcessing(mContext, PowerConsts.SUPER_MODE_PROCESSING);
        startWaitingActivity();
        mTotalTimeoutTimer = new TotalTimeoutTimer(mMainThreadHandler);
        mTotalTimeoutTimer.set(TOTAL_TIMEOUT);
        mState = State.EXIT;
        mCallback = afterCallback;

        ArrayList<String> exitKillList = new ArrayList<String>();
        exitKillList.addAll(SuperModeUtils.getExitSuperModeForceKillList(mContext));
        killExplicityApps(exitKillList);

        mSuperModeItems = getSuperModeController(mContext);
        for (Integer mode : appliedModes) {
            if (mode == PowerConsts.NORMAL_MODE) {
                ModeItemsController normalMode = NormalModeController.getNormalModeController(mContext);
                mSuperModeItems.replaceAndUnionByMode(normalMode);
            }
        }
//        configDarkTheme(EVENT_EXIT_SUPERMODE);
        configEnableSuperModeLauncher(EVENT_EXIT_SUPERMODE);
        changeExitSuperModeArrangment(mSuperModeItems);
        checkExitSuperModeConfigs(mSuperModeItems);
    }

    private void changeExitSuperModeArrangment(ModeItemsController modeItemsController) {
        modeItemsController.moveToFirst("CpuLimit");
        modeItemsController.moveToLast("HomeKeyValue");
        modeItemsController.moveToLast("VoiceControl");
        modeItemsController.moveToLast("Gesture");
        modeItemsController.moveToLast("FloatTouch");
    }

    private void checkExitSuperModeConfigs(ModeItemsController modeItems) {
        Log.d(TAG, "continue exit supermode");
        modeItems.restoreCheckPoint(true);
        if (modeItems.getState() != ModeItemsController.State.FINISH) {
            Log.d(TAG, "exit supermode config paused!");
        } else {
            Log.d(TAG, "exit supermode config finished");
            finishRestore();
        }
    }

    private void finishRestore() {
        Log.d(TAG, "outSuperPowerSaveMode end");
        PowerModeUtils.setCurrentMode(mContext, mRestoreToMode);
        PowerModeUtils.setModeProcessing(mContext, PowerConsts.SUPER_MODE_DONE);
        Log.d(TAG, "----->exit supermode finished");
        finishWaitingActivity();
        Intent intent = new Intent(BOARDCAST_EXIT_SUPERMODE_FINISH);
        mContext.sendBroadcast(intent);
        mTotalTimeoutTimer.unset();
        if (mCallback != null) {
            mCallback.run();
        }
        Log.d(TAG, "----->exit supermode finished, enable statusbar and controlcenter");
        StatusbarController.enableControlCenter(mContext);
        StatusbarController.setStatusbarState(mContext, StatusBarManager.DISABLE_NONE);
        startDefaultLauncher();
    }

    private void startDefaultLauncher() {
        Intent home = new Intent(Intent.ACTION_MAIN, null);
        home.addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        Log.d(TAG, " waitingActivity finish start default launcher，FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
        mContext.startActivity(home);
    }

    private void emergencyRestore(String reason) {
        Log.d(TAG, "enter emergencyRestore, reason=" + reason);
        ArrayList<Integer> appliedModes = new ArrayList<Integer>();
        int fromMode = PowerModeUtils.getCurrentMode(mContext);
        while (fromMode != -1) {
            fromMode = PowerModeUtils.popModeFromStack(mContext);
            appliedModes.add(fromMode);
        }
        mSuperModeItems = getSuperModeController(mContext);
        for (Integer mode : appliedModes) {
            if (mode == PowerConsts.NORMAL_MODE) {
                ModeItemsController normalMode = NormalModeController.getNormalModeController(mContext);
                mSuperModeItems.replaceAndUnionByMode(normalMode);
            }
        }
        changeExitSuperModeArrangment(mSuperModeItems);
        mSuperModeItems.restoreCheckPoint(true);
        Intent abortBroadcastintent = new Intent(BOARDCAST_SUPERMODE_ABORT);
        abortBroadcastintent.putExtra("reason", reason);
        abortBroadcastintent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        mContext.sendBroadcast(abortBroadcastintent);
        StatusbarController.setStatusbarState(mContext, StatusBarManager.DISABLE_NONE);
        StatusbarController.enableControlCenter(mContext);
        PowerModeUtils.setCurrentMode(mContext, PowerConsts.NONE_MODE);
        PowerModeUtils.setModeProcessing(mContext, PowerConsts.SUPER_MODE_DONE);
        PowerModeUtils.resetModeToStack(mContext);
        showSuperPowerErrorDialog();
    }

    private static class ColorChangeReceiver extends BroadcastReceiver implements Runnable {
        private final Context mContext;
        private final Handler mHandler;
        private final int mEvent;
        private boolean mIsRegistered = false;

        public ColorChangeReceiver(Context context, Handler handler, int event, int timeout) {
            mContext = context;
            mEvent = event;
            mHandler = handler;
            IntentFilter filter = new IntentFilter();
            filter.addAction(PowerModeItemDarkTheme.CHAMELEON_CHANGE_COLOR);
            filter.addAction(PowerModeItemDarkTheme.DARK_THEME_NOT_CHANGE);
            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            mContext.registerReceiver(this, filter);
            Log.d(TAG, "set color change timeout " + timeout);
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, timeout);
            mIsRegistered = true;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // 变色之后再继续
            Log.d(TAG, intent.getAction() + " received event " + mEvent);
            if (mEvent == EVENT_START_SUPERMODE) {
                toDoContinueStart();
            } else if (mEvent == EVENT_EXIT_SUPERMODE) {
                toDoContinueRestore();
            }
            remove();
        }

        @Override
        public void run() {
            Log.d(TAG, "color change timeout event " + mEvent);

            if (mEvent == EVENT_START_SUPERMODE) {
                toDoContinueStart();
            } else if (mEvent == EVENT_EXIT_SUPERMODE) {
                toDoContinueRestore();
            }
            remove();
        }

        private void remove() {
            if (!mIsRegistered) {
                Log.d(TAG, "ColorChangeReceiver not registered or already removed!");
                return;
            }
            mIsRegistered = false;
            mHandler.removeCallbacks(this);
            try {
                mContext.unregisterReceiver(this);
            } catch (Exception e) {
                Log.d(TAG, "unSetColorChangedReceiver Exception " + e);
            }
        }

        private void toDoContinueRestore() {
            Log.d(TAG, " send EVENT_EXIT_SUPERMODE");
            mHandler.removeMessages(EVENT_EXIT_SUPERMODE);
            mHandler.sendEmptyMessage(EVENT_EXIT_SUPERMODE);
        }

        private void toDoContinueStart() {
            Log.d(TAG, " send EVENT_START_SUPERMODE");
            mHandler.removeMessages(EVENT_START_SUPERMODE);
            mHandler.sendEmptyMessage(EVENT_START_SUPERMODE);
        }
    }

    private static class SuperLauncherChangeReceiver extends BroadcastReceiver implements Runnable {
        private final Context mContext;
        private final Handler mHandler;
        private final int mEvent;
        private boolean mIsRegistered = false;

        public SuperLauncherChangeReceiver(Context context, Handler handler, int event, int timeout) {
            mContext = context;
            mEvent = event;
            mHandler = handler;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            mContext.registerReceiver(this, filter);
            Log.d(TAG, "set color change timeout " + timeout);
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, timeout);
            mIsRegistered = true;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // 极致省电桌面冻结/解冻之后再继续
            Log.d(TAG, intent.getAction() + " received event " + mEvent);
            if (mEvent == EVENT_START_SUPERMODE) {
                if (!isPowerLauncherFrozen(mContext)) {
                    toDoContinueStart();
                    remove();
                }
            } else if (mEvent == EVENT_EXIT_SUPERMODE) {
                if (isPowerLauncherFrozen(mContext)) {
                    toDoContinueRestore();
                    remove();
                }
            }
        }

        private boolean isPowerLauncherFrozen(Context context) {
            int launcherStatus = context.getPackageManager()
                    .getApplicationEnabledSetting("com.cydroid.powersaver.launcher");
            Log.d(TAG, "powersaverLauncher status = " + launcherStatus);
            return launcherStatus == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || launcherStatus == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        }

        @Override
        public void run() {
            Log.d(TAG, "color change timeout event " + mEvent);
            if (mEvent == EVENT_START_SUPERMODE) {
                toDoContinueStart();
            } else if (mEvent == EVENT_EXIT_SUPERMODE) {
                toDoContinueRestore();
            }
            remove();
        }

        private void remove() {
            if (!mIsRegistered) {
                Log.d(TAG, "SuperLauncherChangeReceiver not registered or already removed!");
                return;
            }
            mIsRegistered = false;
            mHandler.removeCallbacks(this);
            try {
                mContext.unregisterReceiver(this);
            } catch (Exception e) {
                Log.d(TAG, "unSet SuperLauncherChangeReceiver Exception " + e);
            }
        }

        private void toDoContinueRestore() {
            Log.d(TAG, " send EVENT_EXIT_SUPERMODE");
            mHandler.removeMessages(EVENT_EXIT_SUPERMODE);
            mHandler.sendEmptyMessage(EVENT_EXIT_SUPERMODE);
        }

        private void toDoContinueStart() {
            Log.d(TAG, " send EVENT_START_SUPERMODE");
            mHandler.removeMessages(EVENT_START_SUPERMODE);
            mHandler.sendEmptyMessage(EVENT_START_SUPERMODE);
        }
    }

    private class TotalTimeoutTimer implements Runnable {
        Handler mHandler;
        private boolean mIsSet = false;

        public TotalTimeoutTimer(Handler mainThreadHandler) {
            mHandler = mainThreadHandler;
        }

        @Override
        public void run() {
            unset();
            Log.d(TAG, "MSG_TOTAL_TIMEOUT ----->Waiting timeout check status");
            try {
                if (mSuperModeHandler.getLooper().getThread() != mContext.getMainLooper().getThread()) {
                    mSuperModeHandler.getLooper().quit();
                }
            } catch (Exception e) {
                Log.d(TAG, "can not stop mode change thread!");
            }
            String stateStr = checkSuperModeState();
            if (!stateStr.isEmpty()) {
                emergencyRestore("timeout_" + stateStr);
            } else {
                finishWaitingActivity();
            }
        }

        public void set(int totalTimeout) {
            Log.d(TAG, "set total timeout " + TOTAL_TIMEOUT);
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, totalTimeout);
            mIsSet = true;
        }

        public void unset() {
            mHandler.removeCallbacks(this);
        }

    }

    private class SuperModeHandler extends Handler {

        public SuperModeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_START_SUPERMODE:
                    checkStartSuperModeConfigs(mSuperModeItems);
                    break;
                case EVENT_EXIT_SUPERMODE:
                    checkExitSuperModeConfigs(mSuperModeItems);
                    break;
                default:
                    break;
            }
        }
    }

    private void showSuperPowerErrorDialog() {
        if (mIsRebootDialogShow) {
            Log.d(TAG, "dialog is showing ,skip");
            return;
        }
        Log.d(TAG, "process supermode failed,show showSuperPowerErrorDialog!!");
        mIsRebootDialogShow = true;
        CyeeAlertDialog permDialog = new CyeeAlertDialog.Builder(mContext,
                CyeeAlertDialog.THEME_CYEE_FULLSCREEN).create();

        permDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        permDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        permDialog.setTitle(mContext.getResources().getString(R.string.msg_error_title));

        String message = mContext.getResources().getString(R.string.msg_superpower_error);
        permDialog.setMessage(message);
        permDialog.setCanceledOnTouchOutside(false);
        permDialog.setCancelable(false);
        permDialog.setButton(CyeeAlertDialog.BUTTON_NEGATIVE,
                mContext.getResources().getString(R.string.msg_reboot),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "reset data of chameleon and softmanager--->");
//                        SuperModeUtils.cleanAppDate(mContext, "com.cyee.chameleon");
//                        SuperModeUtils.cleanAppDate(mContext, "com.cydroid.softmanager");
//                        SuperModeUtils.rebootFromSuperModeException(mContext);
                    }
                });
        permDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "reset data of chameleon and softmanager--->");
//                SuperModeUtils.cleanAppDate(mContext, "com.cyee.chameleon");
//                SuperModeUtils.cleanAppDate(mContext, "com.cydroid.softmanager");
//                SuperModeUtils.rebootFromSuperModeException(mContext);
            }
        });
        permDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "should not be here");
                mIsRebootDialogShow = false;
            }
        });
        permDialog.show();
    }

    public void checkAndRestoreWhenBoot(String reason, Runnable callback) {
        String stateStr = checkSuperModeState();
        if (!stateStr.isEmpty()) {
            emergencyRestore("startprocess_" + stateStr);
            return;
        }
        resetDarktheme();
        resetSystemUi();
        resetTouchlessGestrues();
        finishWaitingActivity();
        if (callback != null) {
            callback.run();
        }
    }

    private void resetTouchlessGestrues() {
        boolean isNoSupportTouchlessGestrues = "no"
                .equals(SystemProperties.get("ro.gn.distancegesture.support", "yes"));
        if (isNoSupportTouchlessGestrues) {
            Log.d(TAG, "NO TOUCHLESS GESTURE! set to false");
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.GN_DG_SWITCH, 0);
        }
    }

    private void resetSystemUi() {
        String defLauncher = Util.getDefaultLauncherPkg(mContext);
        if (SUPER_LAUNCHER_PACKAGE_NAME.equalsIgnoreCase(defLauncher)) {
            return;
        }

        if (Settings.Global.getInt(mContext.getContentResolver(), "kidsHomeMode", 0) == 1) {
            Log.d(TAG, "do not reset ControlCenter,because in kidsHomeMode");
            return;
        }
        StatusbarController.setControlCenterSwitch(mContext, 1);
        StatusbarController.setStatusbarState(mContext, StatusBarManager.DISABLE_NONE);
    }

    private void checkPowerSaverLauncherState() {
        String defLauncher = Util.getDefaultLauncherPkg(mContext);
        int currentMode = PowerModeUtils.getCurrentMode(mContext);
        boolean isSuperModeDone = (PowerModeUtils.getModeProcessing(mContext) == PowerConsts.SUPER_MODE_DONE);
        Log.d(TAG, "enter restoreAllConfigToNormal when reboot defLauncher=" + defLauncher + " currentMode="
                + currentMode + " isSuperModeDone=" + isSuperModeDone);
        if (!defLauncher.equals(SUPER_LAUNCHER_PACKAGE_NAME)) {
            if (currentMode == PowerConsts.SUPER_MODE) {
                Log.e(TAG, "WRONG! superMode with no powersaver launcher, restore ");
                emergencyRestore("boot-launcher");
                return;
            }
            if (!isSuperModeDone) {
                Log.e(TAG, "WRONG! not finished super mode,restore");
                emergencyRestore("boot-unfinished-change");
                return;
            }
        } else {
            if (currentMode != PowerConsts.SUPER_MODE) {
                Log.e(TAG, "WRONG! powersaver launcher with no super mode, restore");
                emergencyRestore("boot-launcher");
                return;
            }
        }
        Log.i(TAG, "reset ModeProcessing to DONE when BOOT_COMPLETE");
        PowerModeUtils.setModeProcessing(mContext, PowerConsts.SUPER_MODE_DONE);
    }

    private void resetDarktheme() {
        boolean currentChameleonStatus = ChameleonColorManager.isPowerSavingMode()
                && ChameleonColorManager.isNeedChangeColor();
        Log.d(TAG,
                "current in " + PowerModeUtils.getCurrentMode(mContext)
                        + " mode,ChameleonColorManager dark theme is powerSaving:"
                        + ChameleonColorManager.isPowerSavingMode() + " needcolorchange:"
                        + ChameleonColorManager.isNeedChangeColor());
        boolean isShouldInDarkTheme = PowerModeUtils.getCurrentShouldInDarktheme(mContext,
                currentChameleonStatus);
        if (isShouldInDarkTheme == currentChameleonStatus) {
            return;
        }
        Log.d(TAG, " CurrentShouldInDarktheme=" + isShouldInDarkTheme + ", but ChameleonColorManager return "
                + currentChameleonStatus + " try to reset ChameleonColorManager!");
        PowerModeItemDarkTheme darkItem = (PowerModeItemDarkTheme) PowerModeItemSimpleFactory
                .getInstanceByName(mContext, "SUPER", "DarkTheme");
        if (darkItem != null) {
            darkItem.emergencyRestore(isShouldInDarkTheme);
        }
    }
}
