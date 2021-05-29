package com.cydroid.softmanager.systemcheck;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.view.AnimBallView;
import com.cydroid.softmanager.view.ScoreCountView;
import com.cydroid.softmanager.animation.ViewAnimatorUtils;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.oneclean.utils.RamAndMemoryHelper;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.ColorUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.List;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeButton;

/**
 * Created by zhaocaili on 18-7-24.
 */

public class SystemCheckActivity extends AppCompatActivity implements IMemoryCleanNativeCallback, ScoreCountView.ScoreChangeListener {
    private static final String TAG = "SystemCheckActivity";

    private Context mContext;
    private TextView mSystemCheckResultTips;
    private ListView mSystemCheckLists;
    private RelativeLayout mTopBg;
    private CyeeButton mCheckDoneBtn;
    private BaseAdapter mSystemCheckAdapter;
    private ProgressBar mProgressBar;

    private int mIntentScore;
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

    //guoxt modify for main page animation begin
    private ScoreCountView mScoreView;
    //guoxt modify for main page animation end
    private int mCurrentColor;
    private long mRubbishSize;
    private boolean isSystemCheckOver;
    private boolean isNeedChangeColor;
    private boolean isItemClickAniming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_check_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().show();
        getSupportActionBar().setElevation(0.0f);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.host_bar_bg_white)));
        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        initViews();
        initParameters();
        initOptimizeThread();
        initSystemCheckThread();
        setCurrentView(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
//        resetStatusBarColor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStorageHelper.cleanup();
        mSystemCheckLists.clearAnimation();
        removeAllHandlersMessage();
        unregisterRubbishCheckReceiver();
    }

    @Override
    public void onBackPressed() {
        if (isSystemCheckOver) {
            returnScoreResult();
        } else {
            SystemCheckItem.releaseAllList();
            super.onBackPressed();
        }
    }

    @Override
    public void onScoreChange(final int score) {
        Log.d(TAG, "OnScoreChange()" + score);
        if (isNeedChangeColor || mCurrentColor == ColorUtils.getCurrentColorByScore(score)) {
            return;
        }
        int beforeColor = mCurrentColor;
        mCurrentColor = ColorUtils.getCurrentColorByScore(score);
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

    AnimBallView manim_ball_view;

    private void initViews() {
        mSystemCheckResultTips = (TextView) findViewById(R.id.system_check_tip);
        mTopBg = (RelativeLayout) findViewById(R.id.system_check_top);
        mSystemCheckLists = (CyeeListView) findViewById(R.id.system_check_list);
        mProgressBar = (ProgressBar) findViewById(R.id.system_check_progress_bar);
        mCheckDoneBtn = (CyeeButton) findViewById(R.id.system_check_done_btn);
        mScoreView = (ScoreCountView) findViewById(R.id.score_view);
        mScoreView.setScoreChangeListener(this);
        mCheckDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnScoreResult();
            }
        });

        manim_ball_view = findViewById(R.id.anim_ball_view);
    }

    private void setCurrentView(Intent intent) {
        mIntentScore = intent.getIntExtra("SYSTEM_SCORE", 100);
        mSystemScore = mIntentScore;
        setCurrentBackgroundColor();
        setSystemScoreResult(mSystemScore);
        isSystemCheckOver = intent.getBooleanExtra("SYSTEM_CHECK_OVER", false);
        if (!isSystemCheckOver) {
            SystemCheck.checkRubbishCleanedDirectly(mContext);
            mSystemCheckAdapter = new SystemCheckingAdapter(SystemCheckActivity.this);
            mSystemCheckLists.setAdapter(mSystemCheckAdapter);
            sendMessageToSystemCheckHandler(SystemCheckItem.CHECK_CLOSING_BACKGROUND_PROGRESS, 0, 0, null);
        } else {
            setSystemCheckResultTips(SystemCheckItem.CHECK_OVER);
            ViewAnimatorUtils.startShowViewAlphaAnimator(mSystemCheckLists, 1000);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mSystemCheckLists.getLayoutParams();
            params.bottomMargin = mCheckDoneBtn.getHeight();
            mSystemCheckLists.setLayoutParams(params);
            mCheckDoneBtn.setVisibility(View.VISIBLE);
            ViewAnimatorUtils.startShowViewAlphaAnimator(mCheckDoneBtn, 1000);
            mSystemCheckAdapter = new SystemCheckOverAdapter(SystemCheckActivity.this, mItemClickCallback);
            mSystemCheckLists.setAdapter(mSystemCheckAdapter);
            //mSystemCheckLists.setOnItemClickListener(mOnItemClickListener);
        }
        execTopHalfAnimation(false);
    }

    private void initParameters() {
        mContext = getApplicationContext();
        isNeedChangeColor = ChameleonColorManager.isNeedChangeColor();
        mProgressBar.setMax(SystemCheckItem.CHECK_OVER);
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

    /**
     * 上半部分界面的动画
     *
     * @param anim 　显示圆圈隐藏的动画　true; 不显示圆圈隐藏的动画　false
     */
    private void execTopHalfAnimation(boolean anim) {
        int fromHeight;
        int toHeight;
        if (!isSystemCheckOver) {
            fromHeight = mContext.getResources().getDimensionPixelSize(R.dimen.top_half_height);
            toHeight = mContext.getResources().getDimensionPixelSize(R.dimen.checking_top_half_height);
        } else {
            fromHeight = mContext.getResources().getDimensionPixelSize(R.dimen.checking_top_half_height);
            toHeight = mContext.getResources().getDimensionPixelSize(R.dimen.final_target_height);
        }
        ValueAnimator animator = ViewAnimatorUtils.changeViewHeightAnimator(mTopBg, fromHeight, toHeight, 1000, new ViewAnimatorUtils.AnimatorCallback() {
            @Override
            public void onAnimationStart() {
                if (!isSystemCheckOver) {
                    //guoxt modify for main page animation begin
                    mScoreView.isExamLoadingAnim(true);
                    //guoxt modify for main page animation end
                } else {
                    mScoreView.hideCircleView(anim);
                    mProgressBar.setVisibility(View.GONE);
                    mSystemCheckAdapter = new SystemCheckOverAdapter(SystemCheckActivity.this, mItemClickCallback);
                    mSystemCheckLists.setAdapter(mSystemCheckAdapter);
                }
            }

            @Override
            public void onAnimationEnd() {
                if (isSystemCheckOver) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mSystemCheckLists.getLayoutParams();
                    params.bottomMargin = mContext.getResources().getDimensionPixelSize(R.dimen.system_check_over_btn_height);
                    mSystemCheckLists.setLayoutParams(params);
                    mCheckDoneBtn.setVisibility(View.VISIBLE);
                    ViewAnimatorUtils.startShowViewAlphaAnimator(mCheckDoneBtn, 300);
                    stopAnimation();
                    //mSystemCheckLists.setOnItemClickListener(mOnItemClickListener);
                }
            }
        });
        animator.start();
    }

    private void setCurrentBackgroundColor() {
        if (isNeedChangeColor) {
            int color_A1 = ChameleonColorManager.getAppbarColor_A1();
            int color_C2 = ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2();
            mSystemCheckResultTips.setTextColor(color_C2);
        } else {
            mCurrentColor = ColorUtils.getCurrentColorByScore(mSystemScore);
        }
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
        try {
            mScoreView.scoreChange(score);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Score error:" + e);
        }
    }

    private void stopAnimation() {
        mScoreView.isExamLoadingAnim(false);

    }
    //guoxt modify for main page animation end

    private void returnScoreResult() {
        Intent intent = new Intent();
        intent.putExtra("SYSTEM_SCORE", mSystemScore);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setSystemScoreResult(int score) {
        mScoreView.setDefaultScore(score);
    }

    private void setSystemCheckResultTips(int checkItem) {
        int tips = R.string.system_check_progress_checking;
        switch (checkItem) {
            case SystemCheckItem.CHECK_OVER:
                int size = SystemCheckItem.mToBeOptimizeList.size();
                if (size != 0) {
                    tips = R.string.system_check_result_to_be_optimize;
                    String optStr = String.format(mContext.getResources().getString(tips, size));
                    mSystemCheckResultTips.setText(optStr);
                } else {
                    tips = R.string.system_check_result_end_tip;
                    mSystemCheckResultTips.setText(tips);
                }
                break;
            default:
                mSystemCheckResultTips.setText(tips);
        }
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
                mScoreView.setCircleRotateAngle(msg.what, SystemCheckItem.CHECK_ALLCOUNT);
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
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_CLOSED_BACKGROUND_PROCESS:
                    int totalProcess = msg.arg1;
                    SystemCheckItem.addCloseBackgroundProcessItem(mContext, totalProcess);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    break;
                case SystemCheckItem.CHECK_RELEASE_SYSTEM_MEMORY:
                    String total = Formatter.formatFileSize(mContext, (long) msg.obj * 1024).replace(" ", "");
                    SystemCheckItem.addReleaseSystemMemoryItem(mContext, total);
                    mSystemCheckAdapter.notifyDataSetChanged();
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
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY:
                    boolean rubbish = (boolean) msg.obj;
                    SystemCheckItem.addCheckRubbishCleanedDirectly(mContext, rubbish);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_REMAINING_SPACE:
                    Object[] space = (Object[]) msg.obj;
                    SystemCheckItem.addCheckRemainingSpace(mContext, (boolean) space[0], (String) space[1]);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_NOT_FREQUENTLY_USED_APPS:
                    boolean notFrequentlyUse = (boolean) msg.obj;
                    SystemCheckItem.addCheckNotFrequentlyUsedApps(mContext, notFrequentlyUse);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED:
                    boolean highPower = (boolean) msg.obj;
                    SystemCheckItem.addCheckHighPowerConsumptionItem(mContext, highPower);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_RAM_CPU_MONITOR_OPENED:
                    boolean monitor = (boolean) msg.obj;
                    SystemCheckItem.addCheckRamAndCPUItem(mContext, monitor);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_INTELLIGENT_SLEEP_OPENED:
                    boolean intelligent = (boolean) msg.obj;
                    SystemCheckItem.addCheckIntelligentSleep(mContext, intelligent);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_SCREEN_POWER_SAVE_OPENED:
                    boolean save = (boolean) msg.obj;
                    SystemCheckItem.addCheckScreenPowerSave(mContext, save);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_ADAPTIVE_BATTERY_OPENED:
                    boolean adaptive = (boolean) msg.obj;
                    SystemCheckItem.addCheckAdaptiveBatteryItem(mContext, adaptive);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED:
                    boolean auto = (boolean) msg.obj;
                    SystemCheckItem.addCheckAutomaticScreenBrightnessItem(mContext, auto);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_WLAN_ENABLED:
                    boolean disconnect = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfWlanEnableItem(mContext, disconnect);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_BLUETOOTH_OPENED:
                    boolean notInUse = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfBluetoothOpenedItem(mContext, notInUse);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_HOT_POT_OPENED:
                    boolean hotpot = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfHotpotOpenedItem(mContext, hotpot);
                    mSystemCheckAdapter.notifyDataSetChanged();
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
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_SCREEN_SLEEP_TIME:
                    Object[] objects = (Object[]) msg.obj;
                    boolean timeOut = (boolean) objects[0];
                    String time = (String) objects[1];
                    SystemCheckItem.addCheckScreenSleepTimeItem(mContext, time, timeOut);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_SIM_CARD_INSERTED:
                    boolean inserted = (boolean) msg.obj;
                    if (inserted) {
                        sendMessageToHandlerThread(msg.what + 1);
                    } else {
                        SystemCheckItem.addCheckIfSimInserted(mContext);
                        mSystemCheckAdapter.notifyDataSetChanged();
                        sendMessageToHandlerThread(SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED);
                    }
                    break;
                case SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET:
                    boolean set = (boolean) msg.obj;
                    if (!set) {
                        SystemCheckItem.addCheckIfHasSetTrafficSetItem(mContext, set);
                        mSystemCheckAdapter.notifyDataSetChanged();
                        sendMessageToHandlerThread(SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED);
                    } else {
                        sendMessageToHandlerThread(msg.what + 1);
                    }
                    break;
                case SystemCheckItem.CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH:
                    Object[] enough = (Object[]) msg.obj;
                    SystemCheckItem.addCheckFreeTrafficEnoughItem(mContext, (boolean) enough[0], (boolean) enough[1], (String) enough[2]);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToHandlerThread(msg.what + 1);
                    break;
                case SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED:
                    boolean display = (boolean) msg.obj;
                    SystemCheckItem.addCheckIfTrafficSaveOpened(mContext, display);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    sendMessageToSystemCheckHandler(SystemCheckItem.CHECK_OVER, 0, 0, null);
                    break;
                case SystemCheckItem.CHECK_OVER:
                    isSystemCheckOver = true;
                    setSystemCheckResultTips(SystemCheckItem.CHECK_OVER);
                    execTopHalfAnimation(true);
                    break;
                default:
                    break;
            }
            setProgressBarProgress(msg.what);
        }
    }

    private void setProgressBarProgress(int progress) {
        if(progress==1){
            manim_ball_view.onStartAnim();
        }
        if(progress>=SystemCheckItem.CHECK_OVER){
            manim_ball_view.onStopAnim();
        }
        mProgressBar.setProgress(progress);
    }

    private void onCleanMemory(Context context) {
        // use new weapon here
        final MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(context);
        new Thread() {
            @Override
            public void run() {
                memoryManager.memoryClean(MemoryManager.CLEAN_TYPE_ROCKET, SystemCheckActivity.this);
            }
        }.start();
    }

    private final OnItemClickCallback mItemClickCallback = new OnItemClickCallback() {
        @Override
        public void onItemClick(View view, int position) {
            if (isItemClickAniming || position <= 0 || position > SystemCheckItem.mToBeOptimizeList.size()) {
                return;
            }
            handleItemClick(view, position);
        }
    };

    private void handleItemClick(View view, int position) {
        isItemClickAniming = true;
        Bundle bundle = SystemCheckItem.mToBeOptimizeList.get(position - 1);
        int clickItem = bundle.getInt(SystemCheckItem.BUNDLE_KEY.checkItem);
        Log.d(TAG, "clickItem = " + clickItem + ",  title = " + bundle.getString(SystemCheckItem.BUNDLE_KEY.contentTitle));
        boolean needAnim = true;
        switch (clickItem) {
            case SystemCheckItem.CHECK_IF_WLAN_ENABLED:
                SystemCheck.closeWlan(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_BLUETOOTH_OPENED:
                SystemCheck.closeBlueTooth(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_GPS_OPENED:
                SystemCheck.closeGPS(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_GESTURE_OPENED:
                SystemCheck.closeGesture(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_SCREEN_SLEEP_TIME:
                SystemCheck.setScreenOffTime(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_SCREEN_POWER_SAVE_OPENED:
                SystemCheck.setScreenPowerSave(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_REMAINING_SPACE:
                SystemCheck.startDeeplyCleanForResult(SystemCheckActivity.this, clickItem);
                needAnim = false;
                break;
            case SystemCheckItem.CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY:
                mRubbishSize = -1;
                SystemCheck.startRubbishCleanForResult(SystemCheckActivity.this, clickItem);
                needAnim = false;
                break;
            case SystemCheckItem.CHECK_NOT_FREQUENTLY_USED_APPS:
                SystemCheck.uninstallNotFrequentlyUsedApps(SystemCheckActivity.this);
                needAnim = false;
                break;
            case SystemCheckItem.CHECK_IF_CLEANNING_ON_LOCKSCREEN_OPENED:
                SystemCheck.setCleanOnLockScreen(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_INTELLIGENT_SLEEP_OPENED:
                SystemCheck.setIntelligentSleep(mContext);
                updateScoreAndList(Score.SCORE_3, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_HOT_POT_OPENED:
                SystemCheck.closeHotpot(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET:
                SystemCheck.startSetTrafficSet(SystemCheckActivity.this, clickItem);
                needAnim = false;
                break;
            case SystemCheckItem.CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH:
                SystemCheck.startInternetControlActivity(SystemCheckActivity.this);
                needAnim = false;
                break;
            case SystemCheckItem.CHECK_IF_TRAFFIC_SAVE_OPENED:
                SystemCheck.setTrafficSaveOpened(mContext);
                updateScoreAndList(Score.SCORE_5, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED:
                SystemCheck.setHighPowerConsumption(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_RAM_CPU_MONITOR_OPENED:
                SystemCheck.setRamAndCPUMonitor(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_ADAPTIVE_BATTERY_OPENED:
                SystemCheck.setAdaptiveBattery(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
            case SystemCheckItem.CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED:
                SystemCheck.setAutoBrightness(mContext);
                updateScoreAndList(Score.SCORE_2, clickItem, null);
                break;
        }
        if (needAnim) {
            ViewAnimatorUtils.rotateViewAnimator(view, 0, -90, 500, new ViewAnimatorUtils.AnimatorCallback() {
                @Override
                public void onAnimationStart() {
                    isItemClickAniming = true;
                }

                @Override
                public void onAnimationEnd() {
                    ViewAnimatorUtils.rotateViewAnimator(view, -90, 0, 0, null);
                    mSystemCheckAdapter.notifyDataSetChanged();
                    isItemClickAniming = false;
                }
            });
        } else {
            isItemClickAniming = false;
        }
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
        if (mSystemCheckAdapter != null) {
            mSystemCheckAdapter.notifyDataSetChanged();
        }
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
            mSystemCheckAdapter.notifyDataSetChanged();
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
