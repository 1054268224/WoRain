package com.wheatek.proxy.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.oneclean.utils.RamAndMemoryHelper;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.softmanager.model.SDCardInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.systemcheck.Score;
import com.cydroid.softmanager.systemcheck.SystemCheck;
import com.cydroid.softmanager.systemcheck.SystemCheckItem;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.MemoryFormatterUtils;
import com.cydroid.softmanager.utils.NameSorting;
import com.example.systemmanageruidemo.MainActivity;
import com.example.systemmanageruidemo.actionpresent.MainActionPresentInterface;
import com.example.systemmanageruidemo.actionview.MainViewActionInterface;
import com.example.systemmanageruidemo.actionview.ViewAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;

import static com.cydroid.softmanager.MainActivity.UPDATE_RAM_INFO;
import static com.cydroid.softmanager.MainActivity.UPDATE_ROM_INFO;
import static com.cydroid.softmanager.MainActivity.UPDATE_SYSTEM_SCORE;

public class HostMainActicity extends HostProxyActivity<MainViewActionInterface> implements MainActionPresentInterface {
    private static final String TAG = HostMainActicity.class.getSimpleName();
    private static final boolean TESTOLD = false;

    {
        attach(new MainActivity());
    }

    MainViewActionInterface mainview;
    private HandlerThread mUpdateMemoryInfoThread;
    private Handler mUpdateMemoryHandler;
    private long mRubbishSize = -1;
    private boolean isSystemCheckOver = false;
    private Context mContext;
    private RamAndMemoryHelper mRamAndMemoryHelper;
    private SoftHelperUtils mStorageHelper;
    private StorageManager mStorageManager;
    private UninstallAppManager mUninstallAppManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (TESTOLD) {
            startActivity(new Intent(this, com.cydroid.softmanager.MainActivity.class));
            finish();
        }
        initParameters();
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.host_bar_bg_white)));
        getSupportActionBar().setElevation(0.0f);
        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ActivityCompat.requestPermissions(this, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //创建文件夹
//                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                        File file = new File(Environment.getExternalStorageDirectory() + "/aa/bb/");
//                        if (!file.exists()) {
//                            Log.d("jim", "path1 create:" + file.mkdirs());
//                        }
//                    }
                    File file = new File("/data/misc/msdata");
                    if (!file.exists()) {
                        boolean r = file.mkdirs();
                        Log.d("jim", "path1 create:" + r);
                    }
                    break;
                }
        }
    }

    private void initParameters() {
        mContext = getApplicationContext();
        mRamAndMemoryHelper = RamAndMemoryHelper.getInstance(mContext);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        mStorageHelper = new SoftHelperUtils(mContext);
        SoftHelperUtils.initStorageState(mContext, mStorageManager);
        mStorageHelper.init(mContext);
        mStorageHelper.setStorageInfoPath(false, SoftHelperUtils.getSDCardPath());
        mUninstallAppManager = UninstallAppManager.getInstance();
        mUninstallAppManager.init(mContext);
//        mWhiteListManager = WhiteListManager.getInstance();
        registerRubbishCheckReceiver();
        updateRamInfo();
    }

    @Override
    public void onstartScan() {
        sendMessageToHandlerThread(SystemCheckItem.CHECK_CLOSED_BACKGROUND_PROCESS);
        SystemCheck.checkRubbishCleanedDirectly(mContext);

    }

    @Override
    public void oncancelScan() {
//      todo .....
    }

    @Override
    public void StopScan() {
        mainview.onStopScan();
    }

    @Override
    public int ChangeScore(int score) {
        mSystemScore = mSystemScore - score;
        mainview.onChangeScore(score, false);
        return mSystemScore;
    }

    private int getCurrentScore() {
        return mSystemScore;
    }

    @Override
    public void setViewAction(MainViewActionInterface viewAvtion) {
        this.mainview = viewAvtion;
    }

    @Override
    public MainViewActionInterface getViewAction() {
        return mainview;
    }


    private void updateRamInfo() {
        mUpdateMemoryInfoThread = new HandlerThread("SystemManager/queryMemoryInfo");
        mUpdateMemoryInfoThread.start();
        mUpdateMemoryHandler = new Handler(mUpdateMemoryInfoThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE_RAM_INFO:
                        String[] memoryInfo = queryMemoryInfo();
                        sendMessageToHandler(UPDATE_RAM_INFO, 0, 0, memoryInfo);
                        break;
                    case UPDATE_ROM_INFO:
                        String[] storageInfo = queryStorageInfo();
                        sendMessageToHandler(UPDATE_ROM_INFO, 0, 0, storageInfo);
                        break;
                    case SystemCheckItem.CHECK_CLOSED_BACKGROUND_PROCESS:
                        int runnings = getRunningProcessSize();
                        reduceSystemScore(runnings > 0, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, null);
                        break;
                    case SystemCheckItem.CHECK_RELEASE_SYSTEM_MEMORY:
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, null);
                        break;
                    case SystemCheckItem.CHECK_SYSTEM_MEMORY_PERCENT:
                        double usedRatio = mRamAndMemoryHelper.getRatioUsedMem();
                        if (usedRatio >= 0.5 && usedRatio < 0.6) {
                            reduceSystemScore(usedRatio > 0.5, msg.what, Score.SCORE_3);
                        } else if (usedRatio >= 0.6 && usedRatio < 0.7) {
                            reduceSystemScore(usedRatio > 0.6, msg.what, Score.SCORE_5);
                        } else if (usedRatio >= 0.7) {
                            reduceSystemScore(usedRatio > 0.7, msg.what, Score.SCORE_8);
                        }
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, null);
                        break;
                    case SystemCheckItem.CHECK_IF_CLEANNING_ON_LOCKSCREEN_OPENED:
                        boolean clean = SystemCheck.checkCleanOnLockScreenIsOpened(mContext);
                        reduceSystemScore(!clean, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, clean);
                        break;
                    case SystemCheckItem.CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY:
                        int count = 0;
                        while (true) {
                            if (mRubbishSize != -1 || count >= 60) break;
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {

                            }
                            count++;
                        }
                        reduceSystemScore(mRubbishSize > 100000000, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, mRubbishSize);
                        break;
                    case SystemCheckItem.CHECK_REMAINING_SPACE:
                        Object[] space = SystemCheck.checkRemainingSpace(mStorageHelper);
                        boolean spaceNotEnough = (boolean) space[0];
                        reduceSystemScore(spaceNotEnough, msg.what, Score.SCORE_3);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, spaceNotEnough);
                        break;
                    case SystemCheckItem.CHECK_NOT_FREQUENTLY_USED_APPS:
                        boolean has = SystemCheck.checkNotFrequentlyUsedApps(mUninstallAppManager);
                        reduceSystemScore(has, msg.what, Score.SCORE_3);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, has);
                        break;
                    case SystemCheckItem.CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED:
                        boolean highPower = SystemCheck.checkHighPowerConsumption(mContext);
                        reduceSystemScore(!highPower, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, highPower);
                        break;
                    case SystemCheckItem.CHECK_IF_RAM_CPU_MONITOR_OPENED:
                        boolean monitor = SystemCheck.checkRamAndCPUMonitor(mContext);
                        reduceSystemScore(!monitor, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, monitor);
                        break;
                    case SystemCheckItem.CHECK_IF_INTELLIGENT_SLEEP_OPENED:
                        boolean intelligent = SystemCheck.checkIntelligentSleepIsOpened(mContext);
                        reduceSystemScore(!intelligent, msg.what, Score.SCORE_3);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, intelligent);
                        break;
                    case SystemCheckItem.CHECK_IF_SCREEN_POWER_SAVE_OPENED:
                        boolean isSupport = SystemCheck.isScreenPowerSaveSupport();
                        if (isSupport) {
                            boolean save = SystemCheck.checkScreenPowerSavingIsOpened();
                            reduceSystemScore(!save, msg.what, Score.SCORE_2);
                        }
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, null);
                        break;
                    case SystemCheckItem.CHECK_IF_ADAPTIVE_BATTERY_OPENED:
                        boolean battery = SystemCheck.checkAdaptiveBattery(mContext);
                        reduceSystemScore(!battery, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, battery);
                        break;
                    case SystemCheckItem.CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED:
                        boolean brightness = SystemCheck.checkAutomaticScreenBrightness(mContext);
                        reduceSystemScore(!brightness, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, brightness);
                        break;
                    case SystemCheckItem.CHECK_IF_WLAN_ENABLED:
                        boolean disconnected = SystemCheck.checkSystemWlanIsDisconnected(mContext);
                        reduceSystemScore(disconnected, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, disconnected);
                        break;
                    case SystemCheckItem.CHECK_IF_BLUETOOTH_OPENED:
                        boolean notInUse = SystemCheck.checkBluetoothIsOpenedButNotInUse();
                        reduceSystemScore(notInUse, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, notInUse);
                        break;
                    case SystemCheckItem.CHECK_IF_GPS_OPENED:
                        boolean isOpened = SystemCheck.checkGPSIsOpened(mContext);
                        reduceSystemScore(isOpened, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, isOpened);
                        break;
                    case SystemCheckItem.CHECK_IF_GESTURE_OPENED:
                        //boolean gesture = SystemCheck.checkGestureIsOpened(mContext);
                        //reduceSystemScore(gesture, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, null);
                        break;
                    case SystemCheckItem.CHECK_IF_HOT_POT_OPENED:
                        boolean hotpot = SystemCheck.checkHotpotIsOpened(mContext);
                        reduceSystemScore(hotpot, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, hotpot);
                        break;
                    case SystemCheckItem.CHECK_IF_SCREEN_SLEEP_TIME:
                        int time = SystemCheck.getScreenOffTime(mContext) / 1000;
                        reduceSystemScore(time > 30, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, null);
                        break;
                    case SystemCheckItem.CHECK_IF_SIM_CARD_INSERTED:
                        boolean isSimInserted = SystemCheck.checkIfSimInserted(mContext);
                        if (isSimInserted) {
                            sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, isSimInserted);
                        } else {
                            sendMessageToHandler(SystemCheckItem.CHECK_OVER, 0, 0, null);
                        }
                        break;
                    case SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET:
                        boolean set = SystemCheck.checkTrafficSetHasSet(mContext);
                        reduceSystemScore(!set, msg.what, Score.SCORE_5);
                        if (set) {
                            sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, set);
                        } else {
                            sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 2, 0, set);
                        }
                        break;
                    case SystemCheckItem.CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH:
                        Object[] enough = SystemCheck.checkFreeTrafficEnough(mContext);
                        reduceSystemScore(!(boolean) enough[0], msg.what, Score.SCORE_10);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, enough);
                        break;
                    case SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED:
                        boolean display = SystemCheck.checkTrafficSaveOpened(mContext);
                        reduceSystemScore(!display, msg.what, Score.SCORE_5);
                        sendMessageToHandler(SystemCheckItem.CHECK_OVER, 0, 0, display);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private int getRunningProcessSize() {
        final MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(mContext);
        List<ProcessMemoryEntity> runnings = memoryManager.getRunningProcessMemoryEntitys(MemoryManager.CLEAN_TYPE_ROCKET);
        return runnings.size();
    }

    private void reduceSystemScore(boolean reduce, int checkItem, int score) {
        if (reduce) {
            SystemCheckItem.addToSubtractionList(checkItem, score);
            reduceSystemScore(score);
            Log.d(TAG, "reduceSystemScore checkItem = " + checkItem + ",  reduceScore = " + score);
        }
    }

    private int mSystemScore = 100;

    private void reduceSystemScore(int score) {
        ChangeScore(score);
    }


    private String[] queryMemoryInfo() {
        String[] info = new String[3];
        double usedRamRatio = mRamAndMemoryHelper.getRatioUsedMem();
        long totalRam = RamAndMemoryHelper.getTotalMem();
        totalRam = MemoryFormatterUtils.translateCapacity(totalRam);

        info[0] = Formatter.formatFileSize(mContext, (long) (usedRamRatio * totalRam)).replace(" ", "");
        info[1] = Formatter.formatFileSize(mContext, totalRam).replace(" ", "");
        return info;
    }

    private String[] queryStorageInfo() {
        String[] info = new String[3];
        SDCardInfo romInfo = mStorageHelper.getInternalStorageInfo();
        long used = romInfo.mUsed;
        long total = romInfo.mTotal;

        // Modify by HZH on 2019/6/15 for EJSL-1532 start
//        info[0] = SoftHelperUtils.convertStorage(used);
//        info[1] = SoftHelperUtils.convertStorage(total);
        info[0] = SoftHelperUtils.unit1000(used);
        info[1] = SoftHelperUtils.unit1000(total);
        // Modify by HZH on 2019/6/15 for EJSL-1532 end
        return info;
    }

    private synchronized void sendMessageToHandlerThread(int what) {
        if (mUpdateMemoryHandler != null) {
            mUpdateMemoryHandler.sendEmptyMessage(what);
        }
    }

    private synchronized void sendMessageToHandler(int checkItem, int arg1, int arg2, Object obj) {
        if (mHandler != null) {
            long delayMillis = 0;
            Message msg = mHandler.obtainMessage(checkItem, arg1, arg2, obj);
            if (checkItem != UPDATE_RAM_INFO && checkItem != UPDATE_ROM_INFO) {
                delayMillis = 200;
            }
            mHandler.sendMessageDelayed(msg, delayMillis);
        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (HostMainActicity.this.isDestroyed()) {
                return;
            }
            switch (msg.what) {
                case UPDATE_RAM_INFO:
                    String[] memoryInfo = (String[]) msg.obj;
                    String usedRam = memoryInfo[0].substring(0, memoryInfo[0].length() - 1);
                    String totalRam = memoryInfo[1].substring(0, memoryInfo[1].length() - 1);
                    String ramInfo = String.format(getResources().getString(R.string.available_ram_info_text), usedRam, totalRam);
                    /*mRamInfoText.setText(ramInfo);*/
                    break;
                case UPDATE_ROM_INFO:
                    String[] storageInfo = (String[]) msg.obj;
                    String used = storageInfo[0].substring(0, storageInfo[0].length() - 1);
                    String total = storageInfo[1].substring(0, storageInfo[1].length() - 1);
                    String romInfo = String.format(getResources().getString(R.string.available_rom_info_text), used, total);
                    /*mRomInfoText.setText(romInfo);*/
                    break;
                case UPDATE_SYSTEM_SCORE:
                    int checkItem = msg.arg1;
                    sendMessageToHandlerThread(checkItem);
                    break;
                case SystemCheckItem.CHECK_OVER:
                    if (!isSystemCheckOver) {
                        isSystemCheckOver = true;
                        StopScan();
                    }
                    break;
            }
        }

    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeAllHandlersMessage();
        mStorageHelper.cleanup();
        SystemCheckItem.releaseAllList();
        SystemCheckItem.releaseRunningProcessList();
        unregisterRubbishCheckReceiver();
        if (mUpdateMemoryInfoThread != null) {
            mUpdateMemoryInfoThread.quit();
        }
    }

    private void removeAllHandlersMessage() {
        mRamAndMemoryHelper.stopUpdateRam();
        mRamAndMemoryHelper.releaseRes();
        mUpdateMemoryHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void unregisterRubbishCheckReceiver() {
        unregisterReceiver(mRubbishCheckReceiver);
    }

    private void registerRubbishCheckReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.cydroid.softmanager.action.rubbishcheck");
        filter.addAction("com.cydroid.softmanager.action.memoryclean");
        registerReceiver(mRubbishCheckReceiver, filter);
    }

    private final BroadcastReceiver mRubbishCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mRubbishCheckReceiver onReceive action = " + action);
            if (action.equals("com.cydroid.softmanager.action.rubbishcheck")) {
                mRubbishSize = intent.getLongExtra("RubbishSize", 0);
                Log.d(TAG, "mRubbishCheckReceiver onReceive mRubbishSize = " + mRubbishSize);
            }
        }
    };

}