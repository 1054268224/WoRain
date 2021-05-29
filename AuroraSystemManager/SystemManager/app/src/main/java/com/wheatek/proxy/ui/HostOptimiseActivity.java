package com.wheatek.proxy.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.oneclean.utils.RamAndMemoryHelper;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.systemcheck.Score;
import com.cydroid.softmanager.systemcheck.SystemCheck;
import com.cydroid.softmanager.systemcheck.SystemCheckItem;
import com.example.systemmanageruidemo.optimise.OptimiseActivity;
import com.example.systemmanageruidemo.actionpresent.OptimisePresent;
import com.example.systemmanageruidemo.actionview.OptimiseView;
import com.example.systemmanageruidemo.bean.PBean;

import java.util.List;

public class HostOptimiseActivity extends HostProxyActivity<OptimiseView> implements OptimisePresent, IMemoryCleanNativeCallback {
    private static final String TAG = HostOptimiseActivity.class.getSimpleName();

     {
        attach(new OptimiseActivity());
    }

    OptimiseView viewAvtion;

    @Override
    public void setViewAction(OptimiseView viewAvtion) {
        this.viewAvtion = viewAvtion;
    }

    @Override
    public OptimiseView getViewAction() {
        return viewAvtion;
    }

    @Override
    public void onStarclean(int currentscore) {
        mSystemScore = currentscore;
        sendMessageToSystemCheckHandler(SystemCheckItem.CHECK_CLOSING_BACKGROUND_PROGRESS, 0, 0, null);
    }

    @Override
    public void changeScore(int a) {
        getViewAction().onchangescore(a);
    }

    @Override
    public void finishClean(int a) {
        getViewAction().onfinishclean(a);
    }

    List<PBean> mlist;

    @Override
    public void onRequestlist(List<PBean> list) {
        this.mlist = list;
    }

    @Override
    public void responseList(List<PBean> list) {
        clearandsetlist();
        getViewAction().onresposelist(list);
    }

    private void clearandsetlist() {
    }

    @Override
    public void itemStatesChange(int groupindex, boolean isresult) {
        getViewAction().onitemstateschange(groupindex, isresult);
    }


    private Context mContext;
    private int mSystemScore;
    private SoftHelperUtils mStorageHelper;
    private StorageManager mStorageManager;

    private HandlerThread mSystemCheckThread;
    private Handler mMessageDispatchHandler;
    private final SystemCheckHandler mSystemCheckHandler = new SystemCheckHandler();
    private HandlerThread mOptimizeThread;
    private Handler mOptimaizeHandler;
    private final ItemOptimizedHandler mItemOptimizedHandler = new ItemOptimizedHandler();

    private RamAndMemoryHelper mRamAndMemoryHelper;
    private UninstallAppManager mUninstallAppManager;

    private long mRubbishSize;
    private boolean isSystemCheckOver;
    private boolean isItemClickAniming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParameters();
        initOptimizeThread();
        initSystemCheckThread();
    }

    private  void AddShortcut(){
        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,getString(R.string.app_name));
        shortcutIntent.putExtra("duplicate", false);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(getApplicationContext(), HostOptimiseActivity.class);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(HostOptimiseActivity.this,
                        R.mipmap.ic_launcher));
        sendBroadcast(shortcutIntent);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStorageHelper.cleanup();
        removeAllHandlersMessage();
        unregisterRubbishCheckReceiver();
    }

    @Override
    public void onBackPressed() {
        if (isSystemCheckOver) {
            returnScoreResult();
        } else {
            SystemCheckItem.releaseAllList();
        }
        super.onBackPressed();
    }


    @Override
    public void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys) {
        int size = processMemoryEntitys.size();
        addSystemScore(size > 0, SystemCheckItem.CHECK_CLOSED_BACKGROUND_PROCESS);
        sendMessageToSystemCheckHandler(SystemCheckItem.CHECK_CLOSED_BACKGROUND_PROCESS, size, 0, null);
    }

    @Override
    public void onMemoryCleanFinished(int totalProcesses, long totalPss) {
        sendMessageToSystemCheckHandler(SystemCheckItem.CHECK_RELEASE_SYSTEM_MEMORY, 0, 0, totalPss);
        sendBroadcast(new Intent("com.cydroid.softmanager.action.memoryclean"));
    }

    private void initParameters() {
        mContext = getApplicationContext();
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        mStorageHelper = new SoftHelperUtils(mContext);
        SoftHelperUtils.initStorageState(mContext, mStorageManager);
        mStorageHelper.setStorageInfoPath(false, SoftHelperUtils.getSDCardPath());
        mStorageHelper.init(mContext);
        mRamAndMemoryHelper = RamAndMemoryHelper.getInstance(mContext);
        mRamAndMemoryHelper.startUpdateRam();
        mUninstallAppManager = UninstallAppManager.getInstance();
        mUninstallAppManager.init(mContext);
        registerRubbishCheckReceiver();
    }


    private void addSystemScore(boolean add, int checkItem) {
        if (add && SystemCheckItem.mSubtractionList.containsKey(checkItem)) {
            int score = SystemCheckItem.mSubtractionList.get(checkItem);
            addSystemScore(score);
            Log.d(TAG, "addSystemScore checkItem = " + checkItem + ",  addScore = " + score);
        }
    }

    //guoxt modify for main page animation begin
    private void addSystemScore(int score) {
        mSystemScore = mSystemScore + score;
        if (mSystemScore > 100) {
            mSystemScore = 100;
        }
        changeScore(score);
    }

    private void returnScoreResult() {
        Intent intent = new Intent();
        intent.putExtra("SYSTEM_SCORE", mSystemScore);
        setResult(RESULT_OK, intent);
    }

    private void setSystemCheckResultTips(int checkItem) {
        Log.d(TAG, "shiyu:setSystemCheckResultTips" + checkItem);
    }

    private void removeAllHandlersMessage() {
        mRamAndMemoryHelper.stopUpdateRam();
        mRamAndMemoryHelper.releaseRes();
        mSystemCheckThread.quit();
        mMessageDispatchHandler.removeCallbacksAndMessages(null);
        mSystemCheckHandler.removeCallbacksAndMessages(null);
    }

    private synchronized void sendMessageToHandlerThread(int what) {
        if (mMessageDispatchHandler != null) {
            setSystemCheckResultTips(what);
            mMessageDispatchHandler.sendEmptyMessageDelayed(what, 300);
        }
    }

    private void initSystemCheckThread() {
        mSystemCheckThread = new HandlerThread("SystemManager/SystemCheck");
        mSystemCheckThread.start();
        mMessageDispatchHandler = new Handler(mSystemCheckThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case SystemCheckItem.CHECK_CLOSED_BACKGROUND_PROCESS:
                        onCleanMemory(mContext);
                        break;
                    case SystemCheckItem.CHECK_RELEASE_SYSTEM_MEMORY:

                        break;
                    case SystemCheckItem.CHECK_SYSTEM_MEMORY_PERCENT:
                        double usedRatio = mRamAndMemoryHelper.getRatioUsedMem();
                        //chenyee zhaocaili 20181107 modify for CSW1805A-1515 begin
                        addSystemScore(true, msg.what);
                        //chenyee zhaocaili 20181107 modify for CSW1805A-1515 end
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, usedRatio);
                        break;
                    case SystemCheckItem.CHECK_IF_CLEANNING_ON_LOCKSCREEN_OPENED:
                        boolean clean = SystemCheck.checkCleanOnLockScreenIsOpened(mContext);
                        addSystemScore(clean, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, clean);
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
                        addSystemScore(mRubbishSize < 100000000, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, mRubbishSize > 100000000);
                        break;
                    case SystemCheckItem.CHECK_REMAINING_SPACE:
                        Object[] space = SystemCheck.checkRemainingSpace(mStorageHelper);
                        boolean enough = !((boolean) space[0]);
                        addSystemScore(enough, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, space);
                        break;
                    case SystemCheckItem.CHECK_NOT_FREQUENTLY_USED_APPS:
                        boolean has = SystemCheck.checkNotFrequentlyUsedApps(mUninstallAppManager);
                        addSystemScore(!has, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, has);
                        break;
                    case SystemCheckItem.CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED:
                        boolean highPower = SystemCheck.checkHighPowerConsumption(mContext);
                        addSystemScore(highPower, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, highPower);
                        break;
                    case SystemCheckItem.CHECK_IF_RAM_CPU_MONITOR_OPENED:
                        boolean monitor = SystemCheck.checkRamAndCPUMonitor(mContext);
                        addSystemScore(monitor, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, monitor);
                        break;
                    case SystemCheckItem.CHECK_IF_INTELLIGENT_SLEEP_OPENED:
                        boolean intelligent = SystemCheck.checkIntelligentSleepIsOpened(mContext);
                        addSystemScore(intelligent, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, intelligent);
                        break;
                    case SystemCheckItem.CHECK_IF_SCREEN_POWER_SAVE_OPENED:
                        boolean support = SystemCheck.isScreenPowerSaveSupport();
                        if (support) {
                            boolean save = SystemCheck.checkScreenPowerSavingIsOpened();
                            addSystemScore(save, msg.what);
                            sendMessageToSystemCheckHandler(msg.what, 0, 0, save);
                        } else {
                            sendMessageToHandlerThread(msg.what + 1);
                        }
                        break;
                    case SystemCheckItem.CHECK_IF_ADAPTIVE_BATTERY_OPENED:
                        boolean battery = SystemCheck.checkAdaptiveBattery(mContext);
                        addSystemScore(battery, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, battery);
                        break;
                    case SystemCheckItem.CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED:
                        boolean brightness = SystemCheck.checkAutomaticScreenBrightness(mContext);
                        addSystemScore(brightness, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, brightness);
                        break;
                    case SystemCheckItem.CHECK_IF_WLAN_ENABLED:
                        boolean disconnected = SystemCheck.checkSystemWlanIsDisconnected(mContext);
                        addSystemScore(!disconnected, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, disconnected);
                        break;
                    case SystemCheckItem.CHECK_IF_BLUETOOTH_OPENED:
                        boolean notInUse = SystemCheck.checkBluetoothIsOpenedButNotInUse();
                        addSystemScore(!notInUse, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, notInUse);
                        break;
                    case SystemCheckItem.CHECK_IF_HOT_POT_OPENED:
                        boolean hotpot = SystemCheck.checkHotpotIsOpened(mContext);
                        addSystemScore(!hotpot, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, hotpot);
                        break;
                    case SystemCheckItem.CHECK_IF_GESTURE_OPENED:
                        //boolean gesture = SystemCheck.checkGestureIsOpened(mContext);
                        //addSystemScore(!gesture, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, null);
                        break;
                    case SystemCheckItem.CHECK_IF_GPS_OPENED:
                        boolean isOpened = SystemCheck.checkGPSIsOpened(mContext);
                        addSystemScore(!isOpened, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, isOpened);
                        break;
                    case SystemCheckItem.CHECK_IF_SCREEN_SLEEP_TIME:
                        int time = SystemCheck.getScreenOffTime(mContext) / 1000;
                        String timeStr = "";
                        boolean timeOut = false;
                        if (time <= 30) {
                            timeStr = String.format(mContext.getResources().getString(R.string.mode_item_timeout_s), time);
                        } else {
                            timeOut = true;
                            time = time / 60;
                            timeStr = String.format(mContext.getResources().getString(R.string.mode_item_timeout_m), time);
                        }
                        addSystemScore(!timeOut, msg.what);
                        Object[] objects = new Object[2];
                        objects[0] = timeOut;
                        objects[1] = timeStr;
                        sendMessageToSystemCheckHandler(msg.what, time, 0, objects);
                        break;
                    case SystemCheckItem.CHECK_IF_SIM_CARD_INSERTED:
                        boolean isSimInserted = SystemCheck.checkIfSimInserted(mContext);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, isSimInserted);
                        break;
                    case SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET:
                        boolean set = SystemCheck.checkTrafficSetHasSet(mContext);
                        addSystemScore(set, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, set);
                        break;
                    case SystemCheckItem.CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH:
                        Object[] freeEnough = SystemCheck.checkFreeTrafficEnough(mContext);
                        addSystemScore((boolean) freeEnough[0], msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, freeEnough);
                        break;
                    case SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED:
                        boolean display = SystemCheck.checkTrafficSaveOpened(mContext);
                        addSystemScore(display, msg.what);
                        sendMessageToSystemCheckHandler(msg.what, 0, 0, display);
                        break;
                }
            }
        };
    }

    private synchronized void sendMessageToSystemCheckHandler(int checkItem, int arg1, int arg2, Object obj) {
        if (mSystemCheckHandler != null) {
            Message msg = mSystemCheckHandler.obtainMessage(checkItem, arg1, arg2, obj);
            long delay = 0;
            if (checkItem == SystemCheckItem.CHECK_RELEASE_SYSTEM_MEMORY) {
                delay = 1000;
            }
            mSystemCheckHandler.sendMessageDelayed(msg, delay);
        }
    }

    private class SystemCheckHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SystemCheckItem.CHECK_CLOSING_BACKGROUND_PROGRESS:
                    SystemCheckItem.addClosingBackgroundProcessItem(mContext);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_CLOSED_BACKGROUND_PROCESS:
                    int totalProcess = msg.arg1;
                    SystemCheckItem.addCloseBackgroundProcessItem(mContext, totalProcess);

                    break;
                case SystemCheckItem.CHECK_RELEASE_SYSTEM_MEMORY:
                    String total = Formatter.formatFileSize(mContext, (long) msg.obj * 1024).replace(" ", "");
                    SystemCheckItem.addReleaseSystemMemoryItem(mContext, total);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_SYSTEM_MEMORY_PERCENT:
                    //chenyee zhaocaili 20181107 modify for CSW1805A-1515 begin
                    //double usedRatio = (double)msg.obj;
                    //SystemCheckItem.addCheckSystemMemoryPercentItem(mContext, usedRatio > 0.5);
                    //mSystemCheckAdapter.notifyDataSetChanged();
                    //chenyee zhaocaili 20181107 modify for CSW1805A-1515 end
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_CLEANNING_ON_LOCKSCREEN_OPENED:
                    boolean clean = (boolean) msg.obj;
                    SystemCheckItem.addCheckCleanOnLockScreen(mContext, clean);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY:
                    boolean rubbish = (boolean) msg.obj;
                    SystemCheckItem.addCheckRubbishCleanedDirectly(mContext, rubbish);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_REMAINING_SPACE:
                    Object[] space = (Object[]) msg.obj;
                    SystemCheckItem.addCheckRemainingSpace(mContext, (boolean) space[0], (String) space[1]);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_NOT_FREQUENTLY_USED_APPS:
                    boolean notFrequentlyUse = (boolean) msg.obj;
                    SystemCheckItem.addCheckNotFrequentlyUsedApps(mContext, notFrequentlyUse);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED:
                    boolean highPower = (boolean) msg.obj;
                    SystemCheckItem.addCheckHighPowerConsumptionItem(mContext, highPower);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_RAM_CPU_MONITOR_OPENED:
                    boolean monitor = (boolean) msg.obj;
                    SystemCheckItem.addCheckRamAndCPUItem(mContext, monitor);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_INTELLIGENT_SLEEP_OPENED:
                    boolean intelligent = (boolean) msg.obj;
                    SystemCheckItem.addCheckIntelligentSleep(mContext, intelligent);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_SCREEN_POWER_SAVE_OPENED:
                    boolean save = (boolean) msg.obj;
                    SystemCheckItem.addCheckScreenPowerSave(mContext, save);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_ADAPTIVE_BATTERY_OPENED:
                    boolean adaptive = (boolean) msg.obj;
                    SystemCheckItem.addCheckAdaptiveBatteryItem(mContext, adaptive);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED:
                    boolean auto = (boolean) msg.obj;
                    SystemCheckItem.addCheckAutomaticScreenBrightnessItem(mContext, auto);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_WLAN_ENABLED:
                    boolean disconnect = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfWlanEnableItem(mContext, disconnect);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_BLUETOOTH_OPENED:
                    boolean notInUse = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfBluetoothOpenedItem(mContext, notInUse);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_HOT_POT_OPENED:
                    boolean hotpot = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfHotpotOpenedItem(mContext, hotpot);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_GESTURE_OPENED:
                    //boolean gesture = (boolean)msg.obj;
                    //SystemCheckItem.addCheckIfGestureOpenedItem(mContext, gesture);
                    //mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_GPS_OPENED:
                    boolean isOpened = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfGPSOpenedItem(mContext, isOpened);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_SCREEN_SLEEP_TIME:
                    Object[] objects = (Object[]) msg.obj;
                    boolean timeOut = (boolean) objects[0];
                    String time = (String) objects[1];
                    SystemCheckItem.addCheckScreenSleepTimeItem(mContext, time, timeOut);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_SIM_CARD_INSERTED:
                    boolean inserted = (boolean) msg.obj;
                    if (inserted) {
                        sendMessageToHandlerThread(msg.what + 1);
                    } else {
                        SystemCheckItem.addCheckIfSimInserted(mContext);

                        sendMessageToHandlerThread(SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED);
                    }
                    break;
                case SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET:
                    boolean set = (boolean) msg.obj;
                    if (!set) {
                        SystemCheckItem.addCheckIfHasSetTrafficSetItem(mContext, set);

                        sendMessageToHandlerThread(SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED);
                    } else {
                        sendMessageToHandlerThread(msg.what + 1);
                    }
                    break;
                case SystemCheckItem.CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH:
                    Object[] enough = (Object[]) msg.obj;
                    SystemCheckItem.addCheckFreeTrafficEnoughItem(mContext, (boolean) enough[0], (boolean) enough[1], (String) enough[2]);

                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED:
                    boolean display = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfTrafficSaveOpened(mContext, display);

                    sendMessageToSystemCheckHandler(SystemCheckItem.CHECK_OVER, 0, 0, null);
                    break;
                case SystemCheckItem.CHECK_OVER:
                    isSystemCheckOver = true;
                    setSystemCheckResultTips(SystemCheckItem.CHECK_OVER);
                    break;
                default:
                    break;
            }
            setProgressBarProgress(msg.what);
        }
    }

    private void setProgressBarProgress(int what) {
        Log.d(TAG, "shiyu:setProgressBarProgress" + what);
    }


    private void onCleanMemory(Context context) {
        // use new weapon here
        final MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(context);
        new Thread() {
            @Override
            public void run() {
                memoryManager.memoryClean(MemoryManager.CLEAN_TYPE_ROCKET, HostOptimiseActivity.this);
            }
        }.start();
    }

    private void updateScoreAndList(int addScore, int checkItem, Object[] objects) {
        Log.d(TAG, " updateScoreAndList checkItem = " + checkItem + ",  addScore = " + addScore + ",  objects = " + objects);
        if (objects == null || (boolean) objects[0]) {
            if (checkItem == SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET) {
                if ((boolean) objects[1]) {
                    addSystemScore(addScore);
                } else {
                    addSystemScore(-addScore);
                }
            } else {
                addSystemScore(true, checkItem);
            }
        }

        SystemCheckItem.updateAfterClickOptimizeList(mContext, checkItem, objects);
        setSystemCheckResultTips(SystemCheckItem.CHECK_OVER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult checkItem = " + requestCode);
        if (requestCode == SystemCheckItem.CHECK_REMAINING_SPACE) {
            mStorageHelper.cleanup();
            mStorageHelper.init(mContext);

        }
        checkClickedItemAgain(requestCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkClickedItemAgain(int checkItem) {
        mOptimaizeHandler.sendEmptyMessage(checkItem);
    }

    private void initOptimizeThread() {
        mOptimizeThread = new HandlerThread("SystemManager/SystemCheckOptimize");
        mOptimizeThread.start();
        mOptimaizeHandler = new Handler(mOptimizeThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case SystemCheckItem.CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY:
                        SystemCheck.checkRubbishCleanedDirectly(mContext);
                        int time = 0;
                        while (true) {
                            if (mRubbishSize != -1 || time >= 10) break;
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {

                            }
                            time++;
                        }
                        Object[] rubbish = new Object[1];
                        rubbish[0] = mRubbishSize < 100000000;
                        sendMessageToOptimizeHandler(msg.what, rubbish);
                        break;
                    case SystemCheckItem.CHECK_REMAINING_SPACE:
                        Object[] space = SystemCheck.checkRemainingSpace(mStorageHelper);
                        sendMessageToOptimizeHandler(msg.what, space);
                        break;
                    case SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET:
                        boolean set = SystemCheck.checkTrafficSetHasSet(mContext);
                        Object[] objects = set ? new Object[4] : new Object[1];
                        objects[0] = set;
                        if (set) {
                            Object[] freeEnough = SystemCheck.checkFreeTrafficEnough(mContext);
                            objects[1] = freeEnough[0];
                            objects[2] = freeEnough[1];
                            objects[3] = freeEnough[2];
                        }
                        sendMessageToOptimizeHandler(msg.what, objects);
                        break;
                    case SystemCheckItem.CHECK_NOT_FREQUENTLY_USED_APPS:
                        boolean notFrequentlyUse = SystemCheck.checkNotFrequentlyUsedApps(mUninstallAppManager);
                        Object[] uninstall = new Object[1];
                        uninstall[0] = !notFrequentlyUse;
                        sendMessageToOptimizeHandler(msg.what, uninstall);
                        break;
                }
            }
        };
    }

    private void sendMessageToOptimizeHandler(int checkItem, Object[] objects) {
        Message m = mItemOptimizedHandler.obtainMessage(checkItem, objects);
        mItemOptimizedHandler.sendMessage(m);
    }

    private class ItemOptimizedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SystemCheckItem.CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY:
                    updateScoreAndList(Score.SCORE_2, msg.what, (Object[]) msg.obj);
                    break;
                case SystemCheckItem.CHECK_REMAINING_SPACE:
                    updateScoreAndList(Score.SCORE_2, msg.what, (Object[]) msg.obj);
                    break;
                case SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET:
                    updateScoreAndList(Score.SCORE_5, msg.what, (Object[]) msg.obj);
                    break;
                case SystemCheckItem.CHECK_NOT_FREQUENTLY_USED_APPS:
                    updateScoreAndList(Score.SCORE_3, msg.what, (Object[]) msg.obj);
                    break;
            }

        }
    }

    private final BroadcastReceiver mRubbishCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRubbishSize = intent.getLongExtra("RubbishSize", 0);
            Log.d(TAG, "mRubbishCheckReceiver onReceive mRubbishSize = " + mRubbishSize);
        }
    };

    private void registerRubbishCheckReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.cydroid.softmanager.action.rubbishcheck");
        registerReceiver(mRubbishCheckReceiver, filter);
    }

    private void unregisterRubbishCheckReceiver() {
        unregisterReceiver(mRubbishCheckReceiver);
    }

}
