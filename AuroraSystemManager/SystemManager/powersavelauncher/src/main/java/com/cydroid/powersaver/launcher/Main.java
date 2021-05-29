package com.cydroid.powersaver.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.app.IBatteryStats;
import com.cydroid.powersaver.launcher.util.BatteryStateHelper;
import com.cydroid.powersaver.launcher.util.DebouncedClickAction;
import com.cydroid.powersaver.launcher.util.Log;
import com.cydroid.powersaver.launcher.util.PowerTimer;
import com.cydroid.powersaver.launcher.util.StatusbarController;
import com.cydroid.powersaver.launcher.util.SuperModeUtils;

import java.util.List;
import java.util.Locale;

import cyee.app.CyeeAlertDialog;

import static com.cydroid.powersaver.launcher.ConfigUtil.SUPPORT_NEW_LAUNCHER;
import static com.cydroid.powersaver.launcher.ConfigUtil.cyBAFlag;

import android.os.SystemProperties;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class Main extends AppCompatActivity implements OnLongClickListener, View.OnClickListener {
    private static final String TAG = "Main";

    private static int mStatusBarFlag = StatusBarManager.DISABLE_NONE;
    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 begin
    // private static final String BATTERY_PERCENTAGE = "battery_percentage";
    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 end
    private static final String SETTING_VALUE = "setting_value";
    private static final String IS_FIRST = "is_first";
    private static final String CYEE_SETTING_CC_SWITCH = "control_center_switch";
    //    private static final String PHONE = "com.android.contacts/.activities.DialtactsActivity";
    private static final String PHONE = "com.android.dialer/.main.impl.MainActivity";
    private static final String PHONE_GOOGLE = "com.google.android.dialer/.extensions.GoogleDialtactsActivity";
    private static final String SMS = "com.android.mms/com.android.mms.ui.BootActivity";
    /*guoxt modify for 20170606 153207 begin */
    private static final String SMS_GOOGLE = "com.google.android.apps.messaging/.ui.ConversationListActivity";
    /*guoxt modify for 20170606 153207 end */
    private static final String CONTACTS = "com.android.contacts/.activities.PeopleActivity";
    private static final String CONTACTS_GOOGLE = "com.google.android.contacts/com.android.contacts.activities.PeopleActivity";
    //    private static final String TIME = "com.android.deskclock/.AlarmClock";
    private static final String TIME = "com.android.deskclock/.uichange.DeskClockSub";
    private static final String GOOGLE_TIME = "com.google.android.deskclock/com.android.deskclock.DeskClock";
    private static final String EXIT_ACTION = "com.action.exit.super.power.save.mode";
    private static final String EXIT_ACTION_CATEGORY = "com.action.exit.super.power.save.mode.category";
    // Chenyee <guoxt> <2018-01-09> add for SW17W16WA-62 begin
    public static final boolean cyExpressPlus = SystemProperties.get("ro.cy.gmsexpress.plus.support", "no").equals("yes");
    // Chenyee <guoxt> <2018-01-09> add for SW17W16WA-62 end
    // Chenyee <guoxt> <2018-01-09> add for CSW1707MV-84 begin
    public static final String cyProject = SystemProperties.get("ro.cy.common.mainboard.prop", "no");
    public static final boolean gnVMFlag = SystemProperties.get("ro.cy.custom", "unknown").equals("VIETNAM_MOBIISTAR");
    // Chenyee <guoxt> <2018-01-09> add for CSW1707MV-84 begin
    // Chenyee <CY_ST_Req> <fujiabing> <20180713> add for CSW1707ST-472 begin
    public static final boolean gnSTFlag = SystemProperties.get("ro.cy.custom", "unknown").equals("COMTRADE_DISTRIBUTION");
    // Chenyee <CY_ST_Req> <fujiabing> <20180713> add for CSW1707ST-472 end

    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 begin
    // private static final String ACTION_BATTERY_PERCENTAGE_SWITCH =
    // "mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH";
    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 end
    private static final int TASK_MAX = 50;
    private static final int WATITINGACTIVITY_TOTAL_SHOWING_TIME_OUT = 2 * 60 * 1000;
    private static final int MSG_TIMEOUT_RESET_EXIT_FLAG = 0;

    // Gionee xionghg modify for power saving optimization 145357 begin
    protected TextView mPhoneView;
    protected TextView mSmsView;
    protected TextView mContactsView;
    protected TextView mTimeView;
    private NewLauncherHelper mHelper;
    // Gionee xionghg modify for power saving optimization 145357 end
    private Context mContext;
    private Resources mRes;
    private int[] mDrawableIds;
    private int mIndex = 0;
    private LinearLayout mExitView;
    private TextView mTimeTextView, mHoursNumTextView, mHoursTextView, mMinutesNumTextView, mMinutesTextView;
    private PowerTimer mCommonUtil;
    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 begin
    // private int mSettingValue;
    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 end
    private SharedPreferences mSettingValuePreferences;
    private IBatteryStats mBatteryInfo;
    private boolean mIsExitSuperSaveModeSent = false;

    // Gionee <yangxinruo> <2015-08-28> add for CR01545094 begin
    private TelephonyManager mTelManager;
    private WifiManager mWifiManager;
    // Gionee <yangxinruo> <2016-4-7> add for CR01670121 begin
    private boolean mIsCorrentStart = false;
    // Gionee <yangxinruo> <2016-4-7> add for CR01670121 end

    private BroadcastReceiver mNetworkReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(TAG, "receive CONNECTIVITY_ACTION wifi:" + mWifiManager.getWifiState()
                        + " DATAConnection:" + mTelManager.getDataEnabled());
            }
        }
    };

    // Gionee <yangxinruo> <2015-08-28> add for CR01545094 end

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "get Action " + msg.what);
            switch (msg.what) {
                case MSG_TIMEOUT_RESET_EXIT_FLAG:
                    resetExitSuperModeFlag();
                default:
                    Log.d(TAG, "unknow msg what = " + msg.what);
                    break;
            }
        }
    };

    private void resetExitSuperModeFlag() {
        Log.d(TAG, "unset supermode exit trigger flag");
        mIsExitSuperSaveModeSent = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sethomemenukey(false);


        getSupportActionBar().hide();
        Log.d(TAG, "onCreate----->");
        // Chenyee xionghg add for black NavigationBar begin
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.black));
        // Chenyee xionghg add for black NavigationBar end
        // Gionee xionghg modify for power saving optimization 145357 begin
        if (SUPPORT_NEW_LAUNCHER) {
            setContentView(R.layout.main_new);
            mHelper = new NewLauncherHelper(this);
        } else {
            setContentView(R.layout.main);
        }
        // Gionee xionghg modify for power saving optimization 145357 end
        initData();
        initViews();
        registerIntentReceiver();
        // Gionee <yangxinruo> <2015-09-14> add for CR01538644 begin
        // Gionee <yangxinruo> <2016-3-3> modify for CR01643976 begin
//        if (PowerModeUtils.getCurrentMode(this) == PowerModeUtils.SUPER_MODE
//                && !ChameleonColorManager.isPowerSavingMode()) {
//            Log.d(TAG, "in super mode ,not in darktheme,send broadcast");
//            sendBroadcastToChameleon();
//        }
        // Gionee <yangxinruo> <2016-3-3> modify for CR01643976 end
        // Gionee <yangxinruo> <2015-09-14> add for CR01538644 end
        // Gionee xionghg add for power saving optimization 145357 begin
        if (SUPPORT_NEW_LAUNCHER && mHelper != null) {
            // 需在initViews()之后进行mHelper.onCreate()
            mHelper.onCreate();
        }
        // Gionee xionghg add for power saving optimization 145357 end
    }

    private void sethomemenukey(boolean b) {
        StatusBarManager statusBarManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            statusBarManager = (StatusBarManager) getSystemService(STATUS_BAR_SERVICE);
        }
        if (statusBarManager != null) {
            statusBarManager.disable(b ? StatusBarManager.DISABLE_NONE : StatusBarManager.DISABLE_RECENT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Gionee <yangxinruo> <2016-4-7> modify for CR01670121 begin
        mSettingValuePreferences = mContext.getSharedPreferences(SETTING_VALUE, Context.MODE_PRIVATE);
        boolean isFirst = mSettingValuePreferences.getBoolean(IS_FIRST, true);
        Log.d(TAG, "onResume----> init PowerLauncher flag:isFirst:" + isFirst + " isCorrentStart:"
                + mIsCorrentStart);
        // Gionee <yangxinruo> <2016-4-7> modify for CR01670121 end
        if (mIsCorrentStart && isFirst) {
            // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 begin
            // mSettingValue = Settings.Secure.getInt(mContext.getContentResolver(), BATTERY_PERCENTAGE, 0);
            // mSettingValuePreferences.edit().putInt(SETTING_VALUE, mSettingValue).commit();
            // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 end
            mSettingValuePreferences.edit().putBoolean(IS_FIRST, false).commit();
            new Thread() {
                @Override
                public void run() {
                    List<String> taskWhiteList = SuperModeUtils.getSuperModeTaskWhitelist(mContext);
                    SuperModeUtils.killRunningAndRecentTask(mContext, Main.this.getTaskId(), taskWhiteList);
                }
            }.start();
        }
        // Gionee <yangxinruo> <2015-09-06> modify for CR01548328 begin
        StatusbarController.disableControlCenter(mContext);
        // Gionee <yangxinruo> <2015-09-06> modify for CR01548328 end
        // Settings.Secure.putInt(getContentResolver(),
        // Settings.Secure.ADB_ENABLED, 0);
        // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 begin
        // setBatteryDisPlayMode(2);
        // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 end
        resetExitSuperModeFlag();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Gionee xionghg add for power saving optimization 145357 begin
        if (SUPPORT_NEW_LAUNCHER && mHelper != null) {
            mHelper.onPause();
        }
        // Gionee xionghg add for power saving optimization 145357 end
    }

    private void initData() {
        mContext = this;

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mTelManager = TelephonyManager.from(mContext);

        mRes = mContext.getResources();
        mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
        mCommonUtil = new PowerTimer(mContext);
        // Gionee <yangxinruo> <2016-4-7> modify for CR01670121 begin
        Intent startIntent = getIntent();
        // Gionee <yangxinruo> <2016-4-7> modify for CR01670121 begin
        mIsCorrentStart = startIntent.getBooleanExtra("start_from_softmanager", false);
        // Gionee <yangxinruo> <2016-4-7> modify for CR01670121 end
    }

    private void registerIntentReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mContext.registerReceiver(mBatteryReveiver, filter);
        // Gionee <yangxinruo> <2015-08-28> add for CR01545094 begin
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netFilter.setPriority(1000);
        mContext.registerReceiver(mNetworkReveiver, netFilter);
        // Gionee <yangxinruo> <2015-08-28> add for CR01545094 end
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBatteryReveiver);
        // Gionee <yangxinruo> <2015-08-28> add for CR01545094 begin
        unregisterReceiver(mNetworkReveiver);
        // Gionee <yangxinruo> <2015-08-28> add for CR01545094 end
        mHandler.removeMessages(MSG_TIMEOUT_RESET_EXIT_FLAG);
        sethomemenukey(true);
        super.onDestroy();

    }

    @SuppressLint("NewApi")
    private void initViews() {
        getWindow().setStatusBarColor(Color.BLACK);

        mIndex = 0;
        mDrawableIds = new int[]{R.drawable.dial_bg, R.drawable.mms_bg, R.drawable.contact_bg,
                R.drawable.clock_bg};

        mPhoneView = (TextView) findViewById(R.id.phone);
        // Chenyee <guoxt> <2018-01-09> add for CSW1707MV-84 begin
        // Chenyee <CY_ST_Req> <fujiabing> <20180713> add for CSW1707ST-472 begin
        if (cyProject.equals("CSW1707") && (gnVMFlag || gnSTFlag)) {
            initView(mPhoneView, PHONE_GOOGLE);
        } else {
            initView(mPhoneView, PHONE);
        }
        // Chenyee <CY_ST_Req> <fujiabing> <20180713> add for CSW1707ST-472 end
        // Chenyee <guoxt> <2018-01-09> add for CSW1707MV-84 end

        mSmsView = (TextView) findViewById(R.id.sms);
        /*guoxt modify for 20170606 153207 begin */
        int flag = initViewSMS(mSmsView, SMS);
        if (flag < 0) {
            initView(mSmsView, SMS_GOOGLE);
        }
        /*guoxt modify for 20170606 153207 begin */

        mContactsView = (TextView) findViewById(R.id.contacts);
        // Chenyee <guoxt> <2018-01-09> add for CSW1707MV-84 begin
        // Chenyee <CY_ST_Req> <fujiabing> <20180713> add for CSW1707ST-472 begin
        if (cyProject.equals("CSW1707") && (gnVMFlag || gnSTFlag)) {
            initView(mContactsView, CONTACTS_GOOGLE);
        } else {
            initView(mContactsView, CONTACTS);
        }
        // Chenyee <CY_ST_Req> <fujiabing> <20180713> add for CSW1707ST-472 end
        // Chenyee <guoxt> <2018-01-09> add for CSW1707MV-84 end

        // Gionee xionghg modify for power saving optimization 145357 begin
        // mTimeView = (TextView) findViewById(R.id.time);
        // initView(mTimeView, TIME);
        if (!SUPPORT_NEW_LAUNCHER) {
            mTimeView = (TextView) findViewById(R.id.time);
            if (ConfigUtil.cyBAFlag) {
                initView(mTimeView, GOOGLE_TIME);
            } else {
                initView(mTimeView, TIME);

            }
        }
        // Gionee xionghg modify for power saving optimization 145357 end

        mExitView = (LinearLayout) findViewById(R.id.exit_view);
        exitSuperSaveModeClickListener();

        mTimeTextView = (TextView) findViewById(R.id.time_textview);
        /*guoxt modify for oversea begin */
        //mHoursNumTextView = (TextView) findViewById(R.id.hours_num);
        //mHoursTextView = (TextView) findViewById(R.id.hours);
        // mMinutesNumTextView = (TextView) findViewById(R.id.mins_num);
        //mMinutesTextView = (TextView) findViewById(R.id.mins);
        /*guoxt modify for oversea end */
        updateTimeAlert();
    }

    private void updateTimeAlert() {
        String str = null;
        int time = 0;
        long timeFromSystem = 0;
        boolean isCharging = false;
        try {
            if (BatteryStateHelper.isChargingNow(mContext)) {
                timeFromSystem = mBatteryInfo.computeChargeTimeRemaining();
                if (timeFromSystem > 0) {
                    str = getResources().getString(R.string.need_charging_time);
                    String formattedTime = Formatter.formatShortElapsedTime(mContext, timeFromSystem);
                    str = str.format(str, formattedTime);
                    // guoxt modify begin
                    // mTimeTextView.setText(str);
                    // setVisibility(false);
                } else {
                    if (BatteryStateHelper.getBatteryLevel(mContext) != 100) {
                        str = getResources().getString(R.string.is_charging_now);
                    } else {
                        str = getResources().getString(R.string.charged_completely);
                    }
                    // guoxt modify begin
                    // mTimeTextView.setText(str);
                    // setVisibility(false);
                }
                isCharging = true;
            } else {
                str = getResources().getString(R.string.can_use_time);
                time = mCommonUtil.getTimeInSuperMode();
                // guoxt modify begin
                /*
                if (time < 0) {
                    String timeStr = str + mRes.getString(R.string.power_cannotget);
                    mTimeTextView.setText(timeStr);
                    setVisibility(false);
                } else {
                    setVisibility(true);
                    int hours = time / 60;
                    int minutes = time % 60;
                    if (BatteryStateHelper.getBatteryLevel(mContext) <= 1)
                        str += mRes.getString(R.string.power_low);
                    else
                        str += mRes.getString(R.string.power_about);
                    mTimeTextView.setText(str);
                    mHoursNumTextView.setText(String.valueOf(hours));
                    mMinutesNumTextView.setText(String.valueOf(minutes));
                }
                */
                //guoxt modify end
            }
            // Gionee xionghg add for power saving optimization 145357 begin
            if (SUPPORT_NEW_LAUNCHER && mHelper != null) {
                int canUseTime = mCommonUtil.getCanUseTimeInSuperMode();
                mHelper.setRemainingTime(str, isCharging, canUseTime);
            }
            // Gionee xionghg add for power saving optimization 145357 end
            // str = str.format(str, mCommonUtil.formatTime(time));

        } catch (RemoteException e) {
            Log.e(TAG, "call computeChargeTimeRemaining throw remote exception");
        } catch (NotFoundException e) {
            // Log.e("dzmdzm", "updateTimeAlert, getString() throw exception, " + e.toString());
            Log.e(TAG, "updateTimeAlert, getString() throw exception, " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "updateTimeAlert: ", e);
        }
    }

    private void setVisibility(boolean visible) {
        //guoxt modfiy begin
        if (visible) {
            //mHoursNumTextView.setVisibility(View.VISIBLE);
            //mHoursTextView.setVisibility(View.VISIBLE);
            //mMinutesNumTextView.setVisibility(View.VISIBLE);
            // mMinutesTextView.setVisibility(View.VISIBLE);
        } else {
            //mHoursNumTextView.setVisibility(View.GONE);
            //mHoursTextView.setVisibility(View.GONE);
            //mMinutesNumTextView.setVisibility(View.GONE);
            //mMinutesTextView.setVisibility(View.GONE);
        }
        //guoxt modfiy end
    }

    private void exitSuperSaveModeClickListener() {
        mExitView.setOnClickListener(new OnClickListener() {
            // Gionee <yangxinruo> <2015-08-15> modify for CR01538376 begin
            DebouncedClickAction createDialogAction = new DebouncedClickAction() {
                @Override
                public void debouncedAction() {
                    createDialog(true);
                }
            };

            @Override
            public void onClick(View v) {
                // createDialog(true);
                createDialogAction.onClick();
            }
            // Gionee <yangxinruo> <2015-08-15> modify for CR01538376 end
        });
    }

    private void initView(TextView view, String componentName) {
        ComponentName component = ComponentName.unflattenFromString(componentName);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager manager = this.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);
        // Gionee <yangxinruo> <2015-11-19> add for CR01594040 begin
        if (list == null || list.size() <= 0) {
            Log.d(TAG, "can not find package " + componentName);
            mIndex++;
            return;
        }
        // Gionee <yangxinruo> <2015-11-19> add for CR01594040 end
        ResolveInfo info = list.get(0);
        String title = (String) info.loadLabel(manager);
        // Drawable top = info.loadIcon(manager);
        Drawable top = mContext.getResources().getDrawable(mDrawableIds[mIndex++]);
        top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
        view.setCompoundDrawables(null, top, null, null);
        view.setText(title);
        view.setTag(intent);
        view.setOnClickListener(this);
    }

    /*guoxt modify for 20170606 153207 begin */
    private int initViewSMS(TextView view, String componentName) {
        ComponentName component = ComponentName.unflattenFromString(componentName);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager manager = this.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);
        // Gionee <yangxinruo> <2015-11-19> add for CR01594040 begin
        if (list == null || list.size() <= 0) {
            Log.d(TAG, "can not find package " + componentName);
            //mIndex++;
            return -1;
        }
        // Gionee <yangxinruo> <2015-11-19> add for CR01594040 end
        ResolveInfo info = list.get(0);
        String title = (String) info.loadLabel(manager);
        // Drawable top = info.loadIcon(manager);
        Drawable top = mContext.getResources().getDrawable(mDrawableIds[mIndex++]);
        top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
        view.setCompoundDrawables(null, top, null, null);
        view.setText(title);
        view.setTag(intent);
        view.setOnClickListener(this);
        return 0;
    }
    /*guoxt modify for 20170606 153207 end */

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick v: " + v);
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = (Intent) v.getTag();
        // Gionee <yangxinruo> <2015-09-22> modify for CR01558180 begin
        // Gionee <yangxinruo> <2015-09-18> delete for CR01551498 begin
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // Gionee <yangxinruo> <2015-09-18> delete for CR01551498 end
        // Gionee <yangxinruo> <2015-09-22> modify for CR01558180 end
        startActivity(intent);
    }

    // Gionee xionghg add for power saving optimization 145357 begin
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (SUPPORT_NEW_LAUNCHER && mHelper != null) {
            Log.d(TAG, "onNewIntent: ");
            mHelper.exitClearMode();
        }
    }
    // Gionee xionghg add for power saving optimization 145357 end

    public void onBackPressed() {
        // Gionee xionghg add for power saving optimization 145357 begin
        if (SUPPORT_NEW_LAUNCHER && mHelper != null) {
            Log.d(TAG, "onBackPressed: ");
            mHelper.exitClearMode();
        }
        // Gionee xionghg add for power saving optimization 145357 end
    }

    private void exitSuperSaveMode() {
        // Intent intent = new Intent();
        // Bundle bundle = new Bundle();
        // intent.setAction(EXIT_ACTION);
        // intent.addCategory(EXIT_ACTION_CATEGORY);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // bundle.putInt("power_flag", 1);
        // intent.putExtras(bundle);
        // mContext.startActivity(intent);
        Intent intent = new Intent();
        intent.setAction(EXIT_ACTION);
        intent.setPackage("com.cydroid.softmanager");
        startService(intent);
        // Gionee xionghg add for power saving optimization 145357 begin
        if (SUPPORT_NEW_LAUNCHER && mHelper != null) {
            mHelper.onExit();
        }
        // Gionee xionghg add for power saving optimization 145357 end
        Log.d(TAG, "EXIT_SuperMode_ACTION sended---->");
    }

//    private void sendBroadcastToChameleon() {
//        // 省电模式的广播action: "cyee.intent.action.chameleon.POWER_SAVING_MODE"
//        Intent mPowerSavingModeIntent = new Intent("cyee.intent.action.chameleon.POWER_SAVING_MODE");
//        mPowerSavingModeIntent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
//        // 省电模式开关：isChecked为true,通知变色龙进入省电模式；isChecked为false,关闭省电模式
//        mPowerSavingModeIntent.putExtra("is_power_saving_mode", true);
//        Log.i(TAG, "PowerSaveLauncher sendBroadcastToChameleon set is_power_saving_mode--> true");
//        // 发送打开/关闭省电模式广播
//        sendBroadcast(mPowerSavingModeIntent);
//    }
    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 begin
    /*
    private void setBatteryDisPlayMode(int value) {
        try {
            Settings.Secure.putInt(mContext.getContentResolver(), BATTERY_PERCENTAGE, value);
            // Post the intent
            Intent intent = new Intent(ACTION_BATTERY_PERCENTAGE_SWITCH);
            intent.putExtra("state", value);
            Log.d(TAG, "sendBroadcast battery percentage switch");
            mContext.sendBroadcast(intent);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist battery display style setting", e);
        }
    }
    */
    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 end

    private BroadcastReceiver mBatteryReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(actionStr)) {
                dissmissDialog();
                createDialog(false);
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(actionStr)) {
                dissmissDialog();
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(actionStr)) {
                updateTimeAlert();
                // Gionee xionghg add for power saving optimization 145357 begin
                if (SUPPORT_NEW_LAUNCHER && mHelper != null) {
                    mHelper.onBatteryChanged();
                }
                // Gionee xionghg add for power saving optimization 145357 end
            }
        }

    };

    private void dissmissDialog() {
        if (mPermDialog != null && mPermDialog.isShowing()) {
            mPermDialog.dismiss();
        }
    }

    private CyeeAlertDialog mPermDialog;

    private void createDialog(final boolean fromClick) {
        String message = null;
        mPermDialog = new CyeeAlertDialog.Builder(mContext, CyeeAlertDialog.THEME_CYEE_FULLSCREEN)
                .create();
        // Gionee <yangxinruo> <2015-09-29> delete for CR01559268 begin
        // mPermDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        // Gionee <yangxinruo> <2015-09-29> delete for CR01559268 end
        mPermDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        /*guoxt modify for CR begin*/
        if (Locale.getDefault().getLanguage().toLowerCase().equals("ar")) {
            mPermDialog.setTitle(mRes.getString(R.string.exit_string_title));
        } else {
            mPermDialog.setTitle(mRes.getString(R.string.exit_string));
        }
        /*guoxt modify for CR end*/
        if (fromClick) {
            message = mRes.getString(R.string.exit_msg);
        } else {
            message = mRes.getString(R.string.exit_msg1);
        }
        mPermDialog.setMessage(message);

        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {
            // Gionee <yangxinruo> <2015-08-15> modify for CR01538376 begin
            DebouncedClickAction postiveAction = new DebouncedClickAction() {
                @Override
                public void debouncedAction() {
                    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 begin
                    // setBatteryDisPlayMode(mSettingValuePreferences.getInt(SETTING_VALUE, 0));
                    // Gionee <yangxinruo> <2015-11-17> delete for CR01592358 end
                    if (!mIsExitSuperSaveModeSent) {
                        exitSuperSaveMode();
                        setExitSuperModeFlag();
                        mHandler.removeMessages(MSG_TIMEOUT_RESET_EXIT_FLAG);
                        mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT_RESET_EXIT_FLAG,
                                WATITINGACTIVITY_TOTAL_SHOWING_TIME_OUT);
                    } else {
                        Log.d(TAG, "exit supermode already sent ,cancel");
                    }
                    mSettingValuePreferences.edit().putBoolean(IS_FIRST, true).commit();
                    addYouJuAgent(mContext, true, fromClick);
                    // Main.this.finish();
                }
            };

            private void setExitSuperModeFlag() {
                Log.d(TAG, "set supermode exit trigger flag");
                mIsExitSuperSaveModeSent = true;
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CyeeAlertDialog.BUTTON_POSITIVE:
                        // Settings.Secure.putInt(mContext.getContentResolver(),
                        // "battery_percentage",0);
                        /*
                         * mSettingValuePreferences.edit().putBoolean(IS_FIRST,
                         * true).commit();
                         * setBatteryDisPlayMode(mSettingValuePreferences
                         * .getInt(SETTING_VALUE, 0)); exitSuperSaveMode();
                         * addYouJuAgent(mContext, true, fromClick);
                         * Main.this.finish();
                         */
                        postiveAction.onClick();
                        break;

                    case CyeeAlertDialog.BUTTON_NEGATIVE:
                        //addYouJuAgent(mContext, false, fromClick);
                        break;
                    default:
                        break;
                }
            }
            // Gionee <yangxinruo> <2015-08-15> modify for CR01538376 end
        };

        mPermDialog.setButton(CyeeAlertDialog.BUTTON_POSITIVE, mRes.getString(R.string.ok_string),
                dialogClickLsn);
        mPermDialog.setButton(CyeeAlertDialog.BUTTON_NEGATIVE, mRes.getString(R.string.cancel_string),
                dialogClickLsn);
        mPermDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        mPermDialog.show();
    }

    private void addYouJuAgent(Context context, boolean flag, boolean isCharging) {
        if (isCharging) {
            return;
        }
        /*
         * if(flag){ YouJuAgent.onEvent(context, "PM_ExitModel_RadioButton");
         * }else{ YouJuAgent.onEvent(context, "PM_CancelModel_RadioButton"); }
         */

        Intent intent = new Intent("gionee.intent.action.YouJuAgent");
        Bundle bundle = new Bundle();
        if (flag) {
            bundle.putInt("PMYouJu", 2);
        } else {
            bundle.putInt("PMYouJu", 3);
        }
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    // Gionee xionghg add for power saving optimization 145357 begin
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SUPPORT_NEW_LAUNCHER && mHelper != null) {
            mHelper.onActivityResult(requestCode, resultCode, data);
        }
    }
    // Gionee xionghg add for power saving optimization 145357 end

}
