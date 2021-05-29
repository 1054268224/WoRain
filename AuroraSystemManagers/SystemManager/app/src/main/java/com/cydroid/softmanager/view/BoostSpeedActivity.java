package com.cydroid.softmanager.view;

import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.MainActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.RunningProcessAdapter;
import com.cydroid.softmanager.RuntimePermissionsManager;
import com.cydroid.softmanager.SystemSettingsActivity;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.oneclean.utils.RamAndMemoryHelper;
import com.cydroid.softmanager.oneclean.utils.RamAndMemoryHelper.RamInfoUpdateCallback;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemDarkTheme;
import com.cydroid.softmanager.systemcheck.SystemCheckItem;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.NameSorting;
import com.cydroid.softmanager.utils.UiUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeButton;

public class BoostSpeedActivity extends BaseActivity
        implements RamInfoUpdateCallback, AnimationListener, IMemoryCleanNativeCallback {
    private final static String TAG = "BoostSpeedActivity";

    public final static int STATE_END = 0xffff;
    public final static int STATE_CHECK_MEMORY = 1;
    public final static int STATE_CLEAN_MEMORY = 2;
    // Gionee <yangxinruo> <2015-12-10> add for CR01606964 begin
    public final static int UPDATE_RAM_UI = 3;
    // Gionee <yangxinruo> <2015-12-10> add for CR01606964 end
    public final static int GET_RUNNING_PROCESS = 4;
    public final static int STATE_INTO_SCREEN = 8;
    public final static int SWEEP_ANGLE_CONSTANT = 290;
    public final static int PERCENT_CONSTANT = 100;

    private LinearLayout mLayout;
    private Context mContext;
    private Resources mRes;
    private RelativeLayout mHeader;
    private MainCircleView mCircle;
    private MainOuterCircleView mOuterCircle;
    private TextView mMemoryScore;
    private TextView mScoreUnit;
    private TextView mMemoryRelease;
    private TextView mMemoryUsed;
    private TextView mMemoryTotal;
    private TextView mScanInfoTxt;
    private CyeeButton mRocketImage;
    // private int mScore;
    private int mUsedMemoryRatio;
    private String mReleaseMemory;
    private Class<?>[] mClss;
    // private BroadcastReceiver mReceiver;
    private RamAndMemoryHelper mOneCleanUtil;
    private TextView mNoRunningText;
    private ProgressBar mLoadingBar;
    private CyeeListView mRunningListview;
    private RunningProcessAdapter mRunningAdapter;
    /* rocket animation when scanning */
    private Animation mAnimRocketRunning;
    /* rocket animation when no scanning */
    private ValueAnimator mAnimRocketNoRunning;
    private int mAnimRocketNoRunningBase;
    /*
     * whether to allow updating memory info automatically. true: allow ; false
     * : prohibit
     */
    private boolean isUpdateMemory = true;
    /* whether the new scanning action may start; true : allow ; false: prohibit */
    private boolean isScanAllow = true;
    /* check whether scan is completed ;true : completed; false : not */
    private boolean isScanEnd = true;
    /* when turn off screen, broadcast of cleaning will not be received */
    private boolean isPause = false;
    // private HelperUtils mHelperUtils;
    /* animation flag when entering into screen first */
    private boolean isIntoScreenAnima = false;

    // Gionee: houjie <2015-11-24> add for CR01597284 begin
    private boolean isRestore = false;
    // Gionee: houjie <2015-11-24> add for CR01597284 end

    // Gionee <yangxinruo> <2015-12-10> add for CR01606964 begin
    private HandlerThread mUpdateMemoryInfoThread;
    private Handler mUpdateMemoryHandler;
    // Gionee <yangxinruo> <2015-12-10> add for CR01606964 end
    // fengpeipei add mFontScale for 49666
    private float mFontScale = 0;

    private WhiteListManager mWhiteListManager;
    private List<String> mUserWhitelistedApps;
    private View.OnClickListener mClickListener;
    private List<ItemInfo> mRunningAppsList = new ArrayList<>();

    // Gionee <yangxinruo> <2015-10-19> add for CR01570066 begin
    private final BroadcastReceiver mColorChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Gionee <yangxinruo> <2015-10-21> modify for CR01571937 begin
            SharedPreferences preferences = getSharedPreferences(MainActivity.MAIN_PROCESS_PREFERENCE,
                    Context.MODE_MULTI_PROCESS);
            // Gionee <yangxinruo> <2015-12-7> modify for CR01605401 begin
            if (isForegroundNow()) {
                // 防止变色后返回主界面重新弹出提示，保证只有从桌面进入才弹出提示
                preferences.edit().putBoolean("color_restart", true).commit();
            }
            // Gionee <yangxinruo> <2015-12-7> modify for CR01605401 end
            // Gionee <yangxinruo> <2015-10-21> modify for CR01571937 end
        }
    };

    // Gionee <yangxinruo> <2015-10-19> add for CR01570066 end
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mColorChangedReceiver,
                new IntentFilter(PowerModeItemDarkTheme.CHAMELEON_CHANGE_COLOR));
        try {
            Configuration config = ActivityManagerNative.getDefault().getConfiguration();
            mFontScale = config.fontScale;
            Log.d(TAG, "main onCreate mFontScale:" + mFontScale);
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to retrieve font size " + e);
        }
        //fengpeipei add for 49666 end
        UiUtils.setElevation(getmActionBar(), 0);
        // Gionee: <houjie> <2015-12-26> add for CR01614703 begin
        if (redirectToPermissionCheckIfNeeded()) {
            return;
        }
        // Gionee: <houjie> <2015-12-26> add for CR01614703 end

        ChameleonColorManager.getInstance().onCreate(this);
        /*
        if (SystemProperties.get("ro.gn.app.securepay.support", "no").equals("yes")) {
            setContentView(R.layout.systemmanager_activity_main_payprotect);
        } else {
            setContentView(R.layout.systemmanager_activity_main_new);
        }
        */
        /*guoxt modify for begin */
        if (Consts.gnVFflag || Consts.gnNoAnti) {
            setContentView(R.layout.systemmanager_activity_main_new_noanti);
        } else {
            setContentView(R.layout.systemmanager_activity_main_new);
        }
        // mHelperUtils = new HelperUtils(this);
        init(this);
        chameleonColorProcess();
    }

    // Gionee <yangxinruo> <2015-10-23> add for CR01573418 begin

    // Gionee <yangxinruo> <2015-12-7> add for CR01605401 begin
    private boolean isForegroundNow() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(getPackageName())) {
                return appProcess.importance <= RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }

    // Gionee <yangxinruo> <2015-12-7> add for CR01605401 end

    // Gionee <yangxinruo> <2015-10-23> add for CR01573418 end

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            int color_A1 = ChameleonColorManager.getAppbarColor_A1();
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            int color_T3 = ChameleonColorManager.getContentColorThirdlyOnAppbar_T3();
            int color_B4 = ChameleonColorManager.getButtonBackgroudColor_B4();

            mHeader.setBackgroundColor(color_A1);

            //mScanInfoTxt.setTextColor(color_T1);
            mMemoryTotal.setTextColor(color_T1);
            mMemoryScore.setTextColor(color_T1);
            mMemoryUsed.setTextColor(color_T1);
            mScoreUnit.setTextColor(color_T1);
            mMemoryRelease.setTextColor(color_T1);
            mCircle.setArcColor(color_T1);
            mCircle.setInnerCircleColor(color_T3);
            mOuterCircle.setOuterArcColor(color_T1);

            // Gionee <houjie> <2015-11-13> add for CR01565278 begin
            mCircle.setPointTint(color_T1);
            // Gionee <houjie> <2015-11-13> add for CR01565278 end
            mRocketImage.setTextColor(color_T1);
            mRocketImage.setBackgroundColorFilter(color_B4);
        }
    }

    private void init(Context context) {
        initParams();
        initUI(context);
        //startRocketAnimationNoRunning();
        //setActivityJump();
        showSettingImg();
        //showVerdictDialog();
        // mRootM = new RootMonitor();
    }

    private void initParams(){
        mContext = getApplicationContext();
        mRes = mContext.getResources();
        mClickListener = new OnClickListener();
        mOneCleanUtil = RamAndMemoryHelper.getInstance(this);
        // Gionee <yangxinruo> <2015-10-19> add for CR01570066 begin

        // Gionee <yangxinruo> <2015-10-19> add for CR01570066 end
        // Gionee <yangxinruo> <2015-12-10> add for CR01606964 begin
        mUpdateMemoryInfoThread = new HandlerThread("SystemManager/queryMemoryInfo");
        mUpdateMemoryInfoThread.start();
        mUpdateMemoryHandler = new Handler(mUpdateMemoryInfoThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case STATE_CHECK_MEMORY:
                        String[] memoryInfo = queryMemoryInfo();
                        Message updateUi = mHandler.obtainMessage(UPDATE_RAM_UI);
                        updateUi.obj = memoryInfo;
                        mHandler.sendMessage(updateUi);
                        break;
                    case GET_RUNNING_PROCESS:
                        List<ItemInfo> runnings = getRunningProcess();
                        Message runningsMsg = mHandler.obtainMessage(GET_RUNNING_PROCESS);
                        runningsMsg.obj = runnings;
                        mHandler.sendMessage(runningsMsg);
                        break;
                    default:
                        break;
                }
            }
        };
        // Gionee <yangxinruo> <2015-12-10> add for CR01606964 end
        sendMessageToHandler(STATE_CHECK_MEMORY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mWhiteListManager = WhiteListManager.getInstance();
                mWhiteListManager.init(mContext);
                mUserWhitelistedApps = mWhiteListManager.getUserWhiteApps();
            }
        }).start();
    }

    private void initUI(Context context) {
        mLayout = (LinearLayout) findViewById(R.id.ram_layout);
        mHeader = (RelativeLayout) findViewById(R.id.header);
        mCircle = (MainCircleView) findViewById(R.id.main_circle);
        mOuterCircle = (MainOuterCircleView) findViewById(R.id.main_outer_circle);
        mMemoryScore = (TextView) findViewById(R.id.scan_score);
        mScoreUnit = (TextView) findViewById(R.id.scan_score_unit);
        mMemoryRelease = (TextView) findViewById(R.id.memory_release_txt);
        mMemoryUsed = (TextView) findViewById(R.id.used_ram_txt);
        mMemoryTotal = (TextView) findViewById(R.id.total_ram_txt);
        //mScanInfoTxt = (TextView) findViewById(R.id.txt_scan_info);
        mRocketImage = (CyeeButton) findViewById(R.id.rocket_img);
        mScoreUnit.setVisibility(View.GONE);
        mLayout.setVisibility(View.GONE);
        mNoRunningText = (TextView) findViewById(R.id.no_running_text);
        mLoadingBar = (ProgressBar) findViewById(R.id.loading_process_bar);
        mRunningListview = (CyeeListView) findViewById(R.id.running_list);
        mRunningAdapter = new RunningProcessAdapter(this, mClickListener);
        mRunningListview.setAdapter(mRunningAdapter);
        mRunningListview.setOnItemClickListener(mOnItemClickListener);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mRocketImage
                .getLayoutParams();
        mAnimRocketNoRunningBase = marginLayoutParams.topMargin;

        mRocketImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                resetIsRestore(false);
                resetCheckingFlag(false);
                resetUpdateMemoryInfoFlag(false);
                resetScanEndFlag(false);
                // startRocketAnimationRunning();

                disableSpeedupBtn();
                onCleanMemory(mContext);
                startCheckCircleAnimation();
            }
        });
    }

    private void enableSpeedupBtn(){
        mRocketImage.setEnabled(true);
        mRocketImage.setAlpha(1.0f);
    }

    private void disableSpeedupBtn(){
        mRocketImage.setEnabled(false);
        mRocketImage.setAlpha(0.8f);
    }

    private void showSettingImg() {
        //setSecondClass(SystemSettingsActivity.class);

        Intent intent = new Intent();
        intent.setClass(this, SystemSettingsActivity.class);
        intent.putExtra("set_from",1);
        setSecondIntent(intent);
        setFirstLayoutVisibility(View.GONE);
    }

    @Override
    public boolean isUsingCustomActionBar() {
        return true;
    }

    @Override
    public boolean isDisplayingHomeAsUp() {
        return true;
    }

    private void onCleanMemory(Context context) {
        // use new weapon here
        final MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(context);
        new Thread() {
            @Override
            public void run() {
                memoryManager.memoryClean(MemoryManager.CLEAN_TYPE_ROCKET, BoostSpeedActivity.this);
            }
        }.start();
    }

    private void updateReleaseMem(Intent intent) {
        updateReleaseMem(intent.getLongExtra("cleanrsult", 0));
    }

    private void updateReleaseMem(long size) {
        mReleaseMemory = Formatter.formatFileSize(mContext, size).replace(" ", "");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume begin");
        // TODO Auto-generated method stub
        super.onResume();
        // Add by zhiheng.huang on 2019/11/30 for  start 
        if (mImg != null) {
            mImg.setVisibility(View.GONE);
        }
        // Add by zhiheng.huang on 2019/11/30 for  end
//        registerIntentFilter();
        startUpdateMemory();
        resetPauseFlag();
        updateDailyDisplay(0);
        // Gionee <houjie> <2015-07-27> add for CR01522222 begin
        updateOuterCircle(false);
        // Gionee <houjie> <2015-07-27> add for CR01522222 end
        // Gionee: houjie <2015-11-24> add for CR01597284 begin
        resetUpdateMemoryInfoFlag(true);
        // Gionee: houjie <2015-11-24> add for CR01597284 end
        //rootCheck();
        /*guoxt modify for SW17W16A-3380 begin*/
        if(mAnimRocketNoRunning!= null){
            mAnimRocketNoRunning.resume();
        }
        /*guoxt modify for SW17W16A-3380 end*/
        updateRunningListView(true, SystemCheckItem.mRunningProcessList);
        sendMessageToHandler(GET_RUNNING_PROCESS);
        Log.d(TAG, "onResume end");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "--------->enter onPause");
        super.onPause();
        stopUpdateMemory();
        /*guoxt modify for SW17W16A-3380 begin*/
        if (mAnimRocketNoRunning != null) {
            mAnimRocketNoRunning.pause();
        }
        /*guoxt modify for SW17W16A-3380 end*/
        // startCheckCleanResult();
        Log.d(TAG, "--------->leave onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setPauseFlag(boolean flag) {
        isPause = flag;
    }

    private void resetUpdateMemoryInfoFlag(boolean flag) {
        isUpdateMemory = flag;
    }

    private void resetCheckingFlag(boolean flag) {
        isScanAllow = flag;
    }

    private void resetScanEndFlag(boolean flag) {
        isScanEnd = flag;
    }

    private void resetIntoScreenAnimaFlag(boolean flag) {
        isIntoScreenAnima = flag;
    }

    // Gionee: houjie <2015-11-24> add for CR01597284 begin
    private void resetIsRestore(boolean flag) {
        isRestore = flag;
    }
    // Gionee: houjie <2015-11-24> add for CR01597284 end

    private void startUpdateMemory() {
        mOneCleanUtil.setCallback(this);
        mOneCleanUtil.startUpdateRam();
    }

    private void stopUpdateMemory() {
        mOneCleanUtil.stopUpdateRam();
    }

    private void resetPauseFlag() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // Thread.sleep(2000);
                    setPauseFlag(false);
                } catch (Exception ex) {

                }
            }
        }).start();
    }

    private void updateDailyDisplay(int flag) {
        if (flag < 0) {
            return;
        }

        String str = "";
        switch (flag) {
            case 0:
                //str = initScanDaily(mContext);// 上次体检 ： 2014-11-19 11：00：08
                break;

            case 1:
                str = mRes.getString(R.string.text_phone_state_better);// 手机状态已经达到最佳
                break;

            case 2:
                str = mRes.getString(R.string.text_phone_state_problem);// 手机状态很差，马上加速
                break;

            case 3:
                str = mRes.getString(R.string.text_memory_release_running);// 正在释放内存
                break;

            case 4:
                str = String.format(mRes.getString(R.string.text_memory_release_display), mReleaseMemory); // 手机加速完成，已释放800MB内存
                break;
            default:
                break;
        }

        //mScanInfoTxt.setText(str);
    }

    private String initScanDaily(Context context) {
        String daily = getLastDaily(context);
        String result;
        if (daily.isEmpty()) {
            result = mRes.getString(R.string.text_scan_daily_none);
        } else {
            String[] split = daily.split(",");
            if (Integer.parseInt(split[0]) == 1) {
                result = mRes.getString(R.string.text_scan_daily_cancel);
            } else {
                result = String.format(mRes.getString(R.string.text_scan_daily_last), split[1]);
            }
        }
        return result;
    }

    //fengpeipei modify for 42565 start
    private String getLastDaily(Context context) {
        //SharedPreferences sharedPreferences = CyeePreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("config_preferences", Context.MODE_PRIVATE);
        String date = sharedPreferences.getString("last_scan_daily", "");
        return date;
    }

    private void updateNewDaily(Context context, int flag) {
        String daily = flag + ",";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        daily += format.format(System.currentTimeMillis());
        //SharedPreferences sharedPreferences = CyeePreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("config_preferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("last_scan_daily", daily).commit();
    }

    @Override
    public void onUpdateRam() {
        Log.d(TAG, "onUpdateRam isIntoScreenAnima:" + isIntoScreenAnima +
                ", isUpdateMemory:" + isUpdateMemory);
        if (!isUpdateMemory) {
            return;
        }
        sendMessageToHandler(STATE_CHECK_MEMORY);
    }

    private synchronized void sendMessageToHandler(int what) {
        if (mUpdateMemoryHandler != null) {
            mUpdateMemoryHandler.sendEmptyMessage(what);
        }
    }

    private void updateMemoryUsedTxt(String txt) {
        mMemoryUsed.setText(String.format(mRes.getString(R.string.text_memory_used), txt));
    }

    private void updateMemoryScoreTxt(String txt) {
        mMemoryScore.setText(txt);
    }

    private void updateMemoryReleaseTxt(Context context, String txt) {
        if (txt == null) {
            mMemoryRelease.setText(mRes.getString(R.string.text_accelerating));
        } else {
            mMemoryRelease.setText(mRes.getString(R.string.text_memory_release, txt));
        }

    }

    private void setMemoryReleaseVisibility(int visibility) {
        // Modify by HZH on 2019/6/6 for EJSL-1498 end
//        mMemoryRelease.setVisibility(visibility);
        mMemoryRelease.setVisibility(View.GONE);
        // Modify by HZH on 2019/6/6 for EJSL-1498 start
    }

    private String[] queryMemoryInfo() {
        String[] info = new String[3];
        double usedRamRatio = mOneCleanUtil.getRatioUsedMem();
        long totalRam = RamAndMemoryHelper.getTotalMem();
        totalRam = translateCapacity(totalRam);

        mUsedMemoryRatio = (int) (usedRamRatio * PERCENT_CONSTANT);
        info[0] = String.valueOf(mUsedMemoryRatio);
        info[1] = Formatter.formatFileSize(mContext, (long) (usedRamRatio * totalRam)).replace(" ", "");
        info[2] = Formatter.formatFileSize(mContext, totalRam).replace(" ", "");
        return info;
    }

    // Chenyee xionghg 20171211 modify for storage conversion begin
    // add = by HZH
    public static long translateCapacity(long capacity) {
        final long M = 1000 * 1000;
        long result = capacity;
        if (capacity <= 67108864L) {
            result = 64 * M;          // 64MB
        } else if (capacity <= 134217728L) {
            result = 128 * M;         // 128MB
        } else if (capacity <= 268435456L) {
            result = 256 * M;         // 256MB
        } else if (capacity <= 536870912L) {
            result = 512 * M;         // 512MB
        } else if (capacity <= 1073741824L) {
            result = 1000 * M;        // 1GB
        } else if (capacity <= 1610612736L) {
            result = 1500 * M;        // 1.5GB
        } else if (capacity <= 2147483648L) {
            result = 2 * 1000 * M;    // 2GB
        } else if (capacity <= 3221225472L) {
            result = 3 * 1000 * M;    // 3GB
        } else if (capacity <= 4294967296L) {
            result = 4 * 1000 * M;    // 4GB
        } else if (capacity <= 6442450944L) {
            result = 6 * 1000 * M;    // 6GB
        } else if (capacity <= 8589934592L) {
            result = 8 * 1000 * M;    // 8GB
        } else if (capacity <= 17179869184L) {
            result = 16 * 1000 * M;   // 16GB
        } else if (capacity <= 32000000000L) {
            result = 32 * 1000 * M;   // 32GB
        }
        return result;
    }
    // Chenyee xionghg 20171211 modify for storage conversion end

    private void updateCircle(int ratio) {
        mCircle.updateRatio(ratio);
        mCircle.updateViews();
    }

    private void updateOuterCircle(boolean flag) {
        mOuterCircle.setRotateRocketFlag(flag);
        mOuterCircle.updateViews();
    }

    private void resetOuterCircleStartAngle() {
        mOuterCircle.resetRotateStartAngle();
    }

    private synchronized void startRocketAnimationRunning() {
        if (mAnimRocketNoRunning != null) {
            mAnimRocketNoRunning.cancel();
        }
        mAnimRocketRunning = AnimationUtils.loadAnimation(mContext, R.anim.gn_anim_rocket_running);
        mAnimRocketRunning.setAnimationListener(this);
        mRocketImage.clearAnimation();
        mRocketImage.startAnimation(mAnimRocketRunning);
    }

    private synchronized void startRocketAnimationNoRunning() {
        mAnimRocketNoRunning = ValueAnimator.ofInt(-16, 16);
        mAnimRocketNoRunning.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatorValue = (int) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mRocketImage
                        .getLayoutParams();
                marginLayoutParams.topMargin = mAnimRocketNoRunningBase + animatorValue;
                mRocketImage.setLayoutParams(marginLayoutParams);
            }
        });
        mAnimRocketNoRunning.setDuration(1500);
        mAnimRocketNoRunning.setRepeatCount(900000);
        mAnimRocketNoRunning.setRepeatMode(ValueAnimator.REVERSE);
        mAnimRocketNoRunning.setTarget(mRocketImage);
        mAnimRocketNoRunning.start();
    }

    private void startCheckCircleAnimation() {
        new Thread(new MemoryRunnable(STATE_CHECK_MEMORY, mUsedMemoryRatio, 0)).start();
        startRotateAnimation();
    }

    private void startCleanCircleAnimation() {
        new Thread(new MemoryRunnable(STATE_CLEAN_MEMORY, 0, mUsedMemoryRatio)).start();
    }

    private void startIntoScreenAnimation() {
        new Thread(new MemoryRunnable(STATE_INTO_SCREEN, 0, mUsedMemoryRatio)).start();
    }

    private void startRotateAnimation() {
        updateOuterCircle(true);
        final Animation animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        mOuterCircle.startAnimation(animation);
    }

    private void startCheckCleanResult() {
        setPauseFlag(true);
        startCleanCircleAnimation();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (mAnimRocketRunning == animation) {
            disableSpeedupBtn();
            onCleanMemory(mContext);
            startCheckCircleAnimation();
        }
    }

    private void onRestoreDefault(int ratio) {
        // Gionee: houjie <2015-11-24> add for CR01597284 begin
        if (!isScanAllow) {
            resetIsRestore(true);
            resetRocketAnim();
        }
        // Gionee: houjie <2015-11-24> add for CR01597284 end
        resetCheckingFlag(true);
        updateDailyDisplay(1);
        // startRocketAnimationNoRunning();
        updateOuterCircle(false);
        // Gionee: houjie <2015-11-24> modify for CR01597284 begin
        // if (isPause) {
        updateCircle(ratio);
        updateMemoryScoreTxt(String.valueOf((int) (ratio * PERCENT_CONSTANT / SWEEP_ANGLE_CONSTANT)));
        // }
        // Gionee: houjie <2015-11-24> modify for CR01597284 end
        restoreDisplay();
    }

    private synchronized void resetRocketAnim() {
        if (mAnimRocketNoRunning != null) {
            mAnimRocketNoRunning.cancel();
        }
        //mRocketImage.clearAnimation();
        enableSpeedupBtn();
        if (null != mAnimRocketRunning) {
            mAnimRocketRunning.setAnimationListener(null);
        }
    }

    private void restoreDisplay() {
        setMemoryReleaseVisibility(View.GONE);
        //mRocketImage.setVisibility(View.VISIBLE);
        enableSpeedupBtn();
    }

    private class MemoryRunnable implements Runnable {
        private int mState = 0;
        private int mStart = 0;
        private int mEnd = 0;

        public MemoryRunnable(int state, int start, int end) {
            mState = state;
            mStart = (int) (start * SWEEP_ANGLE_CONSTANT / PERCENT_CONSTANT);
            mEnd = (int) (end * SWEEP_ANGLE_CONSTANT / PERCENT_CONSTANT);
        }

        @Override
        public void run() {
            // Chenyee xionghg 20171220 add for SW17W16A-2243 begin
            if (BoostSpeedActivity.this.isDestroyed()) {
                Log.w(TAG, "MemoryRunnable run after activity is destroyed, state=" + mState);
                return;
            }
            // Chenyee xionghg 20171220 add for SW17W16A-2243 end
            Log.d(TAG, "--------->MemoryRunnable start state=" + mState + ", mEnd = " + mEnd);
            int duration = 1;
            switch (mState) {
                case STATE_CHECK_MEMORY:
                    int mValue = mStart;
                    while (mStart-- > mEnd) {
                        // Gionee: houjie <2015-11-24> modify for CR01597284 begin
                        if (isPause || isRestore) {
                            // Gionee: houjie <2015-11-24> modify for CR01597284 end
                            break;
                        }
                        sendMessage(mState, mStart);
                        sleep(getSleepTime(mValue - mStart));
                    }
                    break;

                case STATE_CLEAN_MEMORY:
                    if (isScanAllow) {
                        return;
                    }

                    while (!isScanEnd) {
                        if (isPause) {
                            sendMessage(mState, mUsedMemoryRatio);
                            break;
                        }
                        sleep(duration);
                    }

                    while (mStart <= mEnd) {
                        if (isPause) {
                            sendMessage(mState, mUsedMemoryRatio);
                            break;
                        }
                        sendMessage(mState, mStart);
                        sleep(getSleepTime(mStart));
                        mStart++;
                    }
                    sleep(50);
                    break;

                case STATE_INTO_SCREEN:
                    while (mStart++ < mEnd) {
                        sendMessage(mState, mStart);
                        sleep(getSleepTime(mStart));
                        if (isPause) {
                            break;
                        }
                    }
                    break;
            }
            sendMessage(mState, STATE_END);
            Log.d(TAG, "--------->MemoryRunnable end state=" + mState);
        }

        private void sleep(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (Exception ex) {
            }
        }

        private int getSleepTime(int value) {
            int duration = 4;
            return duration;
        }

        private void sendMessage(int state, int value) {
            Message msg = mHandler.obtainMessage(state, value);
            msg.arg1 = mStart;
            mHandler.sendMessage(msg);
        }

    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // Chenyee xionghg 20171220 add for SW17W16A-2243 begin
            if (BoostSpeedActivity.this.isDestroyed()) {
                Log.w(TAG, "handleMessage after activity is destroyed, msg=" + msg.what
                        + ", obj=" + msg.obj);
                return;
            }
            // Chenyee xionghg 20171220 add for SW17W16A-2243 end
            int flag = -1;
            String cleanResult = null;

            switch (msg.what) {
                case STATE_CHECK_MEMORY:
                    if ((Integer) msg.obj == STATE_END) {
                        resetScanEndFlag(true);
                        return;
                    } else {
                        flag = 3;
                        cleanResult = null;
                    }
                    break;

                case STATE_CLEAN_MEMORY:
                    if ((Integer) msg.obj == STATE_END) {
                        onRestoreDefault(msg.arg1);
                        return;
                    } else if ((Integer) msg.obj == (int) mUsedMemoryRatio * SWEEP_ANGLE_CONSTANT
                            / PERCENT_CONSTANT) {
                        flag = 4;
                        cleanResult = mReleaseMemory;
                        resetUpdateMemoryInfoFlag(true);
                        updateNewDaily(mContext, 2);
                        onUpdateRam();
                        updateOuterCircle(false);
                    }
                    // Gionee <houjie> <2015-09-07> add for CR01536201 begin
                    else if ((Integer) msg.obj == (int) mUsedMemoryRatio) {
                        updateNewDaily(mContext, 2);
                    }
                    // Gionee <houjie> <2015-09-07> add for CR01536201 begin
                    break;
                // Gionee <yangxinruo> <2015-12-10> add for CR01606964 begin
                case UPDATE_RAM_UI:
                    String[] memoryInfo = (String[]) msg.obj;
                    updateMemoryUsedTxt(memoryInfo[1].substring(0, memoryInfo[1].length() - 1));
                    mMemoryTotal.setText(memoryInfo[2].substring(0, memoryInfo[2].length() - 1));

                    if (!isIntoScreenAnima) {
                        resetCheckingFlag(false);
                        resetUpdateMemoryInfoFlag(false);
                        startIntoScreenAnimation();
                    } else {
                        mMemoryScore.setText(memoryInfo[0]);
                        updateCircle((int) (mUsedMemoryRatio * SWEEP_ANGLE_CONSTANT / PERCENT_CONSTANT));
                    }
                    return;
                // Gionee <yangxinruo> <2015-12-10> add for CR01606964 end
                case STATE_INTO_SCREEN:
                    if ((Integer) msg.obj == STATE_END) {
                        resetCheckingFlag(true);
                        resetUpdateMemoryInfoFlag(true);
                        resetIntoScreenAnimaFlag(true);
                    } else {
                        mScoreUnit.setVisibility(View.VISIBLE);
                        mLayout.setVisibility(View.VISIBLE);
                        updateMemoryScoreTxt(String.valueOf(
                                (int) ((Integer) msg.obj * PERCENT_CONSTANT / SWEEP_ANGLE_CONSTANT)));
                        updateCircle((Integer) msg.obj);
                    }
                    return;
                case GET_RUNNING_PROCESS:
                    updateRunningListView(false, (List<ItemInfo>)msg.obj);
                    return;
            }
            // Gionee: houjie <2015-11-24> modify for CR01597284 begin
            if (!isRestore) {
                updateDailyDisplay(flag);
                setMemoryReleaseVisibility(View.VISIBLE);
                updateMemoryScoreTxt(
                        String.valueOf((int) ((Integer) msg.obj * PERCENT_CONSTANT / SWEEP_ANGLE_CONSTANT)));
                updateMemoryReleaseTxt(mContext, cleanResult);
                updateCircle((Integer) msg.obj);
            }
            // Gionee: houjie <2015-11-24> modify for CR01597284 end
        }

    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "--------->enter onDestroy");
        ChameleonColorManager.getInstance().onDestroy(this);
        // Gionee <yangxinruo> <2015-10-19> add for CR01570066 begin
        unregisterReceiver(mColorChangedReceiver);
        // Gionee <yangxinruo> <2015-10-19> add for CR01570066 end
        if (mUpdateMemoryInfoThread != null) {
            mUpdateMemoryInfoThread.quit();
        }
        recycleAnimations();
        recycleHandler();
        //chenyee zhaocaili 20180508 add for CSW1707A-845 begin
        releasRes();
        //chenyee zhaocaili 20180508 add for CSW1707A-845 end
        super.onDestroy();
        Log.d(TAG, "--------->leave onDestroy");
    }

    private synchronized void recycleHandler() {
        if (mUpdateMemoryHandler != null) {
            mUpdateMemoryHandler.removeCallbacksAndMessages(null);
            mUpdateMemoryHandler = null;
        }
    }

    //chenyee zhaocaili 20180508 add for CSW1707A-845 begin
    private void releasRes() {
        Util.unbindDrawables(findViewById(R.id.root));
    }
    //chenyee zhaocaili 20180508 add for CSW1707A-845 end

    private synchronized void recycleAnimations() {
        if (mAnimRocketRunning != null) {
            mAnimRocketRunning.setAnimationListener(null);
            mAnimRocketRunning = null;
        }
        if (mAnimRocketNoRunning != null) {
            /*guoxt modify for systemtrace begin */
            mAnimRocketNoRunning.cancel();
            /*guoxt modify for systemtrace end */
            mAnimRocketNoRunning.removeAllUpdateListeners();
            mAnimRocketNoRunning = null;
        }
    }

    // Gionee <houjie> <2015-07-29> add for CR01519483 begin
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        //fengpeipei add for 49666 begin
        if (mFontScale != 0) {
            config.fontScale = mFontScale;
        }
        //fengpeipei add for 49666 end
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
    // Gionee <houjie> <2015-07-29> add for CR01519483 end

    // Gionee: <houjie> <2015-12-26> add for CR01614703 begin
    private boolean redirectToPermissionCheckIfNeeded() {
        if (RuntimePermissionsManager.isBuildSysNeedRequiredPermissions()
                && RuntimePermissionsManager.hasNeedRequiredPermissions(this)) {
            RuntimePermissionsManager.redirectToPermissionCheck(this);
            finish();
            return true;
        }
        return false;
    }
    // Gionee: <houjie> <2015-12-26> add for CR01614703 end

    // new clean callback
    @Override
    public void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys) {
    }

    @Override
    public void onMemoryCleanFinished(int totalProcesses, final long totalPss) {
        Log.d(TAG, "onMemoryCleanFinished --->isPause = " + isPause + ", isScanAllow = " + isScanAllow);
        // Chenyee xionghg 20171206 add for SW17W16A-2243 begin
        if (isDestroyed()) {
            Log.e(TAG, "onMemoryCleanFinished, activity is destroyed, do nothing");
            return;
        }
        // Chenyee xionghg 20171206 add for SW17W16A-2243 end
        /* it will not receiver this broadcast, when turn off screen */
        // Gionee: houjie <2015-11-24> modify for CR01597284 begin
        if (!isPause) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isScanAllow) {
                        updateReleaseMem(totalPss * 1024);
                        queryMemoryInfo();
                        startCleanCircleAnimation();
                        sendMessageToHandler(GET_RUNNING_PROCESS);
                    }
                    resetIsRestore(false);
                }
            });
        }
        // Gionee: houjie <2015-11-24> modify for CR01597284 end
    }

    private List<ItemInfo> getRunningProcess(){
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
        return all;
    }

    private void updateRunningAppsStatus(String packageName){
        for (ItemInfo info : mRunningAppsList){
            if (packageName.equals(info.getPackageName())){
                info.setGreenWhiteListItemState(mUserWhitelistedApps.contains(packageName));
                return;
            }
        }
    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String packageName = (String)v.getTag();
            if (mUserWhitelistedApps.contains(packageName)){
                mWhiteListManager.removeUserWhiteApp(packageName);
                mUserWhitelistedApps.remove(packageName);
            }else {
                mWhiteListManager.addUserWhiteApp(packageName);
                mUserWhitelistedApps.add(packageName);
            }
            updateRunningAppsStatus(packageName);
            mRunningAdapter.setRunningProcess(mRunningAppsList);
        }
    }

    private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position > mRunningAppsList.size()){
                return;
            }
            try{
                String packageName = mRunningAppsList.get(position - 1).getPackageName();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts( "package", packageName, null);
                intent.setData(uri);
                startActivity(intent);
            }catch (Exception e){
                Log.e(TAG, "may be app is not exist " + e.getMessage());
            }
        }
    };

    private void updateRunningListView(boolean fromOnCreate, List<ItemInfo> runnings){
        mRunningAppsList = runnings;
        if (mRunningAppsList == null || mRunningAppsList.size() == 0){
            if (fromOnCreate){
                return;
            }
            mRunningListview.setVisibility(View.GONE);
            mNoRunningText.setVisibility(View.VISIBLE);
            mLoadingBar.setVisibility(View.GONE);
        }else {
            mRunningListview.setVisibility(View.VISIBLE);
            mNoRunningText.setVisibility(View.GONE);
            mLoadingBar.setVisibility(View.GONE);
            mRunningAdapter.setRunningProcess(mRunningAppsList);
        }
    }
}
