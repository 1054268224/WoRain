package com.cydroid.softmanager;

/**
 * Created by zhaocaili on 18-7-23.
 */

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.oneclean.utils.RamAndMemoryHelper;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.powersaver.activities.PowerManagerMainActivity;
import com.cydroid.softmanager.softmanager.SoftManagerActivity;
import com.cydroid.softmanager.softmanager.model.SDCardInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.systemcheck.Score;
import com.cydroid.softmanager.systemcheck.SystemCheck;
import com.cydroid.softmanager.systemcheck.SystemCheckActivity;
import com.cydroid.softmanager.systemcheck.SystemCheckItem;
import com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity;
import com.cydroid.softmanager.utils.ColorUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.MemoryFormatterUtils;
import com.cydroid.softmanager.utils.NameSorting;
import com.cydroid.softmanager.view.AnimBallView;
import com.cydroid.softmanager.view.BoostSpeedActivity;
import com.cydroid.softmanager.view.ScoreCountView;
import com.cydroid.systemmanager.rubbishcleaner.RubbishCleanerMainActivity;

import java.util.ArrayList;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeButton;
import android.widget.ImageView;

import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

public class MainActivity extends BaseActivity
        implements RamAndMemoryHelper.RamInfoUpdateCallback, SoftHelperUtils.StorageInfoUpdateCallback, View.OnClickListener, ScoreCountView.ScoreChangeListener{
    private static final String TAG = "MainActivity";

    private final static int MSG_UPDATE_POWER_MANAGER_SUMMARY = 2;
    private final static int MSG_UPDATE_APP_MANAGER_SUMMARY = 3;

    private final int REQUEST_SCORE_CODE = 0;

    public final static int UPDATE_RAM_INFO = 1000;
    public final static int UPDATE_ROM_INFO = 1001;
    public final static int UPDATE_SYSTEM_SCORE = 1002;

    private int mSystemScore = 100;
    private int mIntentScore = 0;

    private Context mContext;
    private TextView mRamInfoText;
    private TextView mRamInfoTitle;
    private TextView mRomInfoText;
    private TextView mRomInfoTitle;
    private CyeeButton mOptimizeBtn;
    private CyeeListView mSystemMainList;
    private SystemMainAdapter mAdapter;

    private HandlerThread mUpdateMemoryInfoThread;
    private Handler mUpdateMemoryHandler;
    private RamAndMemoryHelper mRamAndMemoryHelper;
    private SoftHelperUtils mStorageHelper;
    private StorageManager mStorageManager;
    private UninstallAppManager mUninstallAppManager;
    private boolean isSystemCheckOver = false;
    private final StateHandler mStateHandler = new StateHandler();
    private final List<String> mUserWhitelistedApps = new ArrayList<>();
    //guoxt modify for main page animation begin
    private ImageView mScanView;
    private ObjectAnimator mScanAnim;
    public int mScoreLayoutHeightInExam;
    private ScoreCountView mScoreView;
    //guoxt modify for main page animation end
    private int mCurrentColor;


    private long mRubbishSize = -1;
    private boolean isNeedChangeColor;
    private AnimBallView mAnimBallView;
    private TextView mBtnBootSpeed;
    private TextView mBtnCacheCleaner;
    private TextView mBtnPowerManager;
    private TextView mBtnTrafficmon;
    private TextView mBtnAppManager;
    private TextView mBtnSetting;

    // Gionee <yangxinruo> <2015-10-21> add for CR01571937 begin
    public final static String MAIN_PROCESS_PREFERENCE = "softmanager_preferences_main_process";
    // Gionee <yangxinruo> <2015-10-21> add for CR01571937 end


    private WhiteListManager mWhiteListManager;


    public boolean isanimstart;

    static class BUTTON_STATUS{
        public static int OPTIMIZE = 0;
        public static int CONTINUE_OPTIMIZE = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "--------->enter onCreate, call ChameleonColorManager.getInstance().onCreate");
        //Chenyee guoxt modify for CSW1703A-2587 begin
        Log.refreshLogEnable();
        //Chenyee guoxt modify for CSW1703A-2587 end
//        UiUtils.setElevation(getmActionBar(), 0);
        //ChameleonColorManager.getInstance().onCreate(this);
        setContentView(R.layout.systemmanager_main_activity);
        showSettingImg();
        initViews();
        initParameters();
        disableMainBtn();
        updateRamInfo();
        chameleonColorProcess();

        mAnimBallView = findViewById(R.id.anim_ball_view);
        mAnimBallView.onStartAnim();

        getWindow().getDecorView().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // startRotateAnimation();
                //guoxt modify for main page animation begin
                isSystemCheckOver = false;
                mAnimBallView.onStartAnim();
                //scanAnimStart();
                mScoreView.isExamLoadingAnim(true);
                //guoxt modify for main page animation end
                SystemCheck.checkRubbishCleanedDirectly(mContext);
                sendMessageToHandlerThread(SystemCheckItem.CHECK_CLOSED_BACKGROUND_PROCESS);
            }
        }, 2500);
        // Add by zhiheng.huang on 2019/11/28 for EJQQQ-144 start
        sendMessageToHandlerThread(UPDATE_ROM_INFO);
        // Add by zhiheng.huang on 2019/11/28 for EJQQQ-141 end

        transparentStatusBar();

    }

    private void transparentStatusBar() {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

    }



    public void scanAnimStart() {
        mScanView.setVisibility(View.VISIBLE);
        mScanAnim = ObjectAnimator.ofFloat(mScanView, "translationY", mScoreLayoutHeightInExam,
                -mScanView.getDrawable().getMinimumHeight());
        mScanAnim.setDuration(2000);
        mScanAnim.setRepeatCount(-1);
        mScanAnim.setInterpolator(new FastOutLinearInInterpolator());
        mScanAnim.start();
    }

    public void cancelScanAnim() {
        if (null == mScanAnim) {
            return;
        }
        mScanAnim.cancel();
        mScanView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScoreView.showCircleView();
//        setCurrentBackgroundColor();
        startUpdateMemoryAndStorageInfo();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "--------->enter onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
        removeAllHandlersMessage();
        mStorageHelper.cleanup();
        SystemCheckItem.releaseAllList();
        SystemCheckItem.releaseRunningProcessList();
        unregisterRubbishCheckReceiver();
    }

    @Override
    public boolean isUsingCustomActionBar() {
        return true;
    }

    @Override
    public boolean isDisplayingHomeAsUp() {
        return false;
    }

    @Override
    public void onUpdateRam() {
        sendMessageToHandlerThread(UPDATE_RAM_INFO);
    }

    @Override
    public void onUpdateStorageInfo() {
        sendMessageToHandlerThread(UPDATE_ROM_INFO);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scan_system_btn:
                mIntentScore = mSystemScore;
                int btnStatus = (int)v.getTag();
                Intent intent = new Intent(MainActivity.this, SystemCheckActivity.class);
                intent.putExtra("SYSTEM_SCORE", mSystemScore);
                intent.putExtra("SYSTEM_CHECK_OVER", btnStatus != 0);
                startActivityForResult(intent, REQUEST_SCORE_CODE);
                break;
            case R.id.cache_cleaner_btn:
                actionNewActivity(mContext, RubbishCleanerMainActivity.class);
                break;
            case R.id.boot_speed_btn:
                actionNewActivity(mContext,BoostSpeedActivity.class);
                break;
            case R.id.power_manager_btn:
                actionNewActivity(mContext,PowerManagerMainActivity.class);
                break;
            case R.id.traffic_monitor_btn:
                actionNewActivity(mContext,TrafficAssistantMainActivity.class);
                break;
            case R.id.app_manager_btn:
                actionNewActivity(mContext,SoftManagerActivity.class);
                break;
            case R.id.setting_btn:
                actionNewActivity(mContext,SystemSettingsActivity.class);
                break;
            default:
                break;
        }
        int id = v.getId();
        if (id == R.id.scan_system_btn){
            mIntentScore = mSystemScore;
            int btnStatus = (int)v.getTag();
            Intent intent = new Intent(MainActivity.this, SystemCheckActivity.class);
            intent.putExtra("SYSTEM_SCORE", mSystemScore);
            intent.putExtra("SYSTEM_CHECK_OVER", btnStatus != 0);
            startActivityForResult(intent, REQUEST_SCORE_CODE);
        }
    }

    private void actionNewActivity(Context context, Class<?> cla){
        Intent intent = new Intent(context,cla);
        startActivity(intent);
    }

    @Override
    public void onScoreChange(final int score) {
        Log.d(TAG, "OnScoreChange()" + score + ", mSystemScore = " + mSystemScore + ",  isSystemCheckOver = " + isSystemCheckOver);
        if (isSystemCheckOver && (score == mSystemScore) && mScoreView.isScoreScrolling()){
            stopAnimation();
            enableMainBtn(BUTTON_STATUS.OPTIMIZE);
        }
        if (isNeedChangeColor || mCurrentColor == ColorUtils.getCurrentColorByScore(score)) {
            return;
        }
        int beforeColor = mCurrentColor;
        mCurrentColor = ColorUtils.getCurrentColorByScore(score);
//        ColorUtils.colorChangeAnim(mTopHalfLayout, MainActivity.this, beforeColor, mCurrentColor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null){
            int result = data.getIntExtra("SYSTEM_SCORE", 100);
            Log.d(TAG, "onActivityResult result = " + result + "  view score = " + mScoreView.getCurrentScore());
            if (result != mScoreView.getCurrentScore()){
                if (result > 100){
                    result = 100;
                }
                int reduceScore = -(result - mScoreView.getCurrentScore());
                reduceSystemScore(reduceScore);
                Log.d(TAG, "reduceSystemScore onActivityResult = " + reduceScore);
            }
            enableMainBtn(BUTTON_STATUS.CONTINUE_OPTIMIZE);
        }
        isSystemCheckOver = false;
    }

    private void removeAllHandlersMessage(){
        mRamAndMemoryHelper.stopUpdateRam();
        mRamAndMemoryHelper.releaseRes();
        mUpdateMemoryInfoThread.quit();
        mUpdateMemoryHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initViews() {
        mBtnCacheCleaner = (TextView) findViewById(R.id.cache_cleaner_btn);
        mBtnCacheCleaner.setOnClickListener(this);
        mBtnBootSpeed = (TextView) findViewById(R.id.boot_speed_btn);
        mBtnBootSpeed.setOnClickListener(this);
        mBtnPowerManager = (TextView)findViewById(R.id.power_manager_btn);
        mBtnPowerManager.setOnClickListener(this);
        mBtnTrafficmon = (TextView)findViewById(R.id.traffic_monitor_btn);
        mBtnTrafficmon.setOnClickListener(this);
        mBtnAppManager = (TextView)findViewById(R.id.app_manager_btn);
        mBtnAppManager.setOnClickListener(this);
        mBtnSetting = (TextView)findViewById(R.id.setting_btn);
        mBtnSetting.setOnClickListener(this);

        mScoreLayoutHeightInExam = getResources().getDimensionPixelSize(R.dimen.power_top_half_height);
        /*mRamInfoText = (TextView) findViewById(R.id.ram_info);
        mRamInfoTitle = (TextView) findViewById(R.id.ram_info_title);
        mRomInfoText = (TextView) findViewById(R.id.rom_info);
        mRomInfoTitle = (TextView) findViewById(R.id.rom_info_title);*/
        mOptimizeBtn = (CyeeButton) findViewById(R.id.scan_system_btn);
        mOptimizeBtn.setOnClickListener(this);
        /*mSystemMainList = (CyeeListView) findViewById(R.id.system_main_list);*/


        mScanView = (ImageView) findViewById(R.id.scan_view);

        mScanView.setImageDrawable( getResources().getDrawable(R.drawable.scan_bg));
        //guoxt modify for main page animation begin
        mScoreView = (ScoreCountView) findViewById(R.id.score_view);
        mScoreView.setScoreChangeListener(this);
        //guoxt modify for main page animation begin
    }

    private void initParameters() {
        mContext = getApplicationContext();
        mAdapter = new SystemMainAdapter(this);
        /*mSystemMainList.setAdapter(mAdapter);*/
        mRamAndMemoryHelper = RamAndMemoryHelper.getInstance(mContext);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        mStorageHelper = new SoftHelperUtils(mContext);
        SoftHelperUtils.initStorageState(mContext, mStorageManager);
        // Modify by zhiheng.huang on 2019/11/28 for EJQQQ-144 start
        mStorageHelper.init(mContext);
        mStorageHelper.setStorageInfoPath(false, SoftHelperUtils.getSDCardPath());
        // Modify by zhiheng.huang on 2019/11/28 for EJQQQ-141 end
        mWhiteListManager = WhiteListManager.getInstance();
        mUninstallAppManager = UninstallAppManager.getInstance();
        mUninstallAppManager.init(mContext);
        registerRubbishCheckReceiver();
        getRunningProcess();
    }

    private void showSettingImg() {
        // setSecondClass(SystemSettingsActivity.class);
        Intent intent = new Intent();
        intent.setClass(this, SystemSettingsActivity.class);
        intent.putExtra("set_from",0);
        setSecondIntent(intent);
        setFirstLayoutVisibility(View.GONE);
    }

    private void setCurrentBackgroundColor(){
        if (isNeedChangeColor){
            int color_A1 = ChameleonColorManager.getAppbarColor_A1();
//            ColorUtils.changeBackgroundColor(mTopHalfLayout, MainActivity.this, color_A1);
        }else {
            if (mIntentScore != 0){
                mCurrentColor = ColorUtils.getCurrentColorByScore(mIntentScore);
                mIntentScore = 0;
            }else {
                mCurrentColor = ColorUtils.getCurrentColorByScore(mSystemScore);
            }
//            ColorUtils.changeBackgroundColor(mTopHalfLayout, MainActivity.this, mCurrentColor);
        }
    }

    private void chameleonColorProcess() {
//        isNeedChangeColor = ChameleonColorManager.isNeedChangeColor();
        Log.d(TAG, "COLOR enter chameleonColorProcess isNeedChangeColor val=" + isNeedChangeColor);
        if (isNeedChangeColor) {
            Log.d(TAG, "COLOR ChameleonColorManager.isNeedChangeColor()=true set theme color ");
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            int color_A1 = ChameleonColorManager.getAppbarColor_A1();
            int color_B4 = ChameleonColorManager.getButtonBackgroudColor_B4();

//            ColorUtils.changeBackgroundColor(mTopHalfLayout, MainActivity.this, color_A1);
            mOptimizeBtn.setTextColor(color_T1);
            mOptimizeBtn.setBackgroundColorFilter(color_B4);
            /*mRamInfoText.setTextColor(color_T1);
            mRamInfoTitle.setTextColor(color_T1);
            mRomInfoText.setTextColor(color_T1);
            mRomInfoTitle.setTextColor(color_T1);*/
        }
    }

    private void disableMainBtn(){
        mOptimizeBtn.setEnabled(false);
        mOptimizeBtn.setAlpha(0.8f);
        mOptimizeBtn.setText(R.string.system_is_scanning);
    }

    private void enableMainBtn(int status){
        mOptimizeBtn.setEnabled(true);
        mOptimizeBtn.setAlpha(1.0f);
        mOptimizeBtn.setTag(status);
        if (mSystemScore == 100){
            mOptimizeBtn.setText(R.string.system_check_good);
        }else {
            if (status == BUTTON_STATUS.OPTIMIZE){
                mOptimizeBtn.setText(R.string.system_optimization);
            }else {
                mOptimizeBtn.setText(R.string.system_continue_optimization);
            }
        }
    }



    private int getRunningProcessSize(){
        final MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(mContext);
        List<ProcessMemoryEntity> runnings = memoryManager.getRunningProcessMemoryEntitys(MemoryManager.CLEAN_TYPE_ROCKET);
        return runnings.size();
    }

    private void getRunningProcess(){
        new Thread(new Runnable() {
            @Override
            public void run() {
//                mWhiteListManager.init(mContext);
//                mUserWhitelistedApps = mWhiteListManager.getUserWhiteApps();
                final MemoryManager memoryManager = MemoryManager.getInstance();
                memoryManager.init(mContext);
                List<ProcessMemoryEntity> runnings = memoryManager.getRunningProcessMemoryEntitysIncludeWhitelist(MemoryManager.CLEAN_TYPE_ROCKET);
                List<ItemInfo> notInUserWhiteListRunningApps = new ArrayList<>();
                List<ItemInfo> all = new ArrayList<>();
                for (ProcessMemoryEntity entity : runnings){
                    ApplicationInfo appInfo = HelperUtils.getApplicationInfo(mContext, entity.mPackageName);
                    //chenyee zhaocaili 20181025 add for BoostSpeed list just display the apps have icon in launcher begin
                    Intent intent = getPackageManager().getLaunchIntentForPackage(entity.mPackageName);
                    if (appInfo != null && intent != null){
                        //chenyee zhaocaili 20181025 add for BoostSpeed list just display the apps have icon in launcher end
                        ItemInfo info = new ItemInfo();
                        boolean isWhiteApp = mUserWhitelistedApps.contains(entity.mPackageName);
                        info.setGreenWhiteListItemState(isWhiteApp);
                        info.setPackageName(entity.mPackageName);
                        info.setTitle(HelperUtils.loadLabel(mContext, appInfo));
                        info.setIcon(HelperUtils.loadIcon(mContext, appInfo));
                        if (isWhiteApp){
                            all.add(info);
                        }else {
                            notInUserWhiteListRunningApps.add(info);
                        }
                    }

                }
                NameSorting.sort(all);
                NameSorting.sort(notInUserWhiteListRunningApps);
                all.addAll(notInUserWhiteListRunningApps);
                SystemCheckItem.setRunningProcessList(all);
            }
        }).start();
    }

    private void stopAnimation() {
        mAnimBallView.onStopAnim();
        //cancelScanAnim();
        mScoreView.isExamLoadingAnim(false);

    }
    //guoxt modify for main page animation end

    private synchronized void sendMessageToHandlerThread(int what) {
        if (mUpdateMemoryHandler != null) {
            mUpdateMemoryHandler.sendEmptyMessage(what);
        }
    }

    private void startUpdateMemoryAndStorageInfo() {
        mRamAndMemoryHelper.setCallback(this);
        mRamAndMemoryHelper.startUpdateRam();
        mStorageHelper.setCallback(this);
    }

    private void updateRamInfo(){
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
                        if (usedRatio >= 0.5 && usedRatio < 0.6){
                            reduceSystemScore(usedRatio > 0.5, msg.what, Score.SCORE_3);
                        }else if (usedRatio >= 0.6 && usedRatio < 0.7){
                            reduceSystemScore(usedRatio > 0.6, msg.what, Score.SCORE_5);
                        }else if (usedRatio >= 0.7){
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
                        while (true){
                            if (mRubbishSize != -1 || count >= 60) break;
                            try {
                                Thread.sleep(500);
                            }catch (Exception e){

                            }
                            count++;
                        }
                        reduceSystemScore(mRubbishSize > 100000000, msg.what, Score.SCORE_2);
                        sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, mRubbishSize);
                        break;
                    case SystemCheckItem.CHECK_REMAINING_SPACE:
                        Object[] space = SystemCheck.checkRemainingSpace(mStorageHelper);
                        boolean spaceNotEnough = (boolean)space[0];
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
                        if (isSupport){
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
                        if (isSimInserted){
                            sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, isSimInserted);
                        }else {
                            sendMessageToHandler(SystemCheckItem.CHECK_OVER, 0, 0, null);
                        }
                        break;
                    case SystemCheckItem.CHECK_IF_SET_TRAFFIC_SET:
                        boolean set = SystemCheck.checkTrafficSetHasSet(mContext);
                        reduceSystemScore(!set, msg.what, Score.SCORE_5);
                        if (set){
                            sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 1, 0, set);
                        }else {
                            sendMessageToHandler(UPDATE_SYSTEM_SCORE, msg.what + 2, 0, set);
                        }
                        break;
                    case SystemCheckItem.CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH:
                        Object[] enough = SystemCheck.checkFreeTrafficEnough(mContext);
                        reduceSystemScore(!(boolean)enough[0], msg.what, Score.SCORE_10);
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

    private void reduceSystemScore(boolean reduce, int checkItem, int score) {
        if (reduce){
            SystemCheckItem.addToSubtractionList(checkItem, score);
            reduceSystemScore(score);
            Log.d(TAG, "reduceSystemScore checkItem = " + checkItem + ",  reduceScore = " + score);
        }
    }

    //guoxt modify for main page animation begin
    private void reduceSystemScore(int score) {
        mSystemScore = mSystemScore - score;
        try {
            mScoreView.scoreChange(-score);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Score error:" + e);
        }
    }
    //guoxt modify for main page animation end


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

    private synchronized void sendMessageToHandler(int checkItem, int arg1, int arg2, Object obj) {
        if (mHandler != null) {
            long delayMillis = 0;
            Message msg = mHandler.obtainMessage(checkItem, arg1, arg2, obj);
            if (checkItem != UPDATE_RAM_INFO && checkItem != UPDATE_ROM_INFO){
                delayMillis = 200;
            }
            mHandler.sendMessageDelayed(msg, delayMillis);
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (MainActivity.this.isDestroyed()) {
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
                    isSystemCheckOver = true;
                    if (isSystemCheckOver && !mScoreView.isScoreScrolling() && mSystemScore == mScoreView.getCurrentScore()){
                        stopAnimation();
                        enableMainBtn(BUTTON_STATUS.OPTIMIZE);
                    }
                    break;
            }
        }

    };

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    private synchronized void sendMessage(int state, int arg1, int arg2, Object obj) {
        if (mStateHandler != null) {
            Message msg = mStateHandler.obtainMessage(state, arg1, arg2, obj);
            mStateHandler.setActivity(this);
            mStateHandler.sendMessage(msg);
        }
    }

    static class StateHandler extends Handler {
        MainActivity mActivity;

        public void setActivity(MainActivity systemMainActivity) {
            mActivity = systemMainActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (null == mActivity) {
                return;
            }
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_POWER_MANAGER_SUMMARY:
                    String timeStr = (String) msg.obj;
                    int time = msg.arg1;
                    mActivity.mAdapter.updatePowerManagerSummary(time, timeStr);
                    break;
                case MSG_UPDATE_APP_MANAGER_SUMMARY:
                    int size = msg.arg1;
                    mActivity.mAdapter.updateAppManagerSummary(size);
                default:
                    break;
            }
        }
    }

    private final BroadcastReceiver mRubbishCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mRubbishCheckReceiver onReceive action = " + action);
            if (action.equals("com.cydroid.softmanager.action.rubbishcheck")){
                mRubbishSize = intent.getLongExtra("RubbishSize", 0);
                Log.d(TAG, "mRubbishCheckReceiver onReceive mRubbishSize = " + mRubbishSize);
            }else if (action.equals("com.cydroid.softmanager.action.memoryclean")){
                getRunningProcess();
            }
        }
    };

    private void registerRubbishCheckReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.cydroid.softmanager.action.rubbishcheck");
        filter.addAction("com.cydroid.softmanager.action.memoryclean");
        registerReceiver(mRubbishCheckReceiver, filter);
    }

    private void unregisterRubbishCheckReceiver() {
        unregisterReceiver(mRubbishCheckReceiver);
    }
}

