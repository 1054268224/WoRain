//Gionee <jianghuan> <2013-09-29> add for CR00975553 end
package com.cydroid.softmanager.trafficassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;
import com.cydroid.softmanager.trafficassistant.controler.TrafficNetworkController;
import com.cydroid.softmanager.trafficassistant.net.UidDetail;
import com.cydroid.softmanager.trafficassistant.net.UidDetailProvider;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.MobileTemplate;
import com.cydroid.softmanager.trafficassistant.utils.NetworkControlXmlFileUtil;
import com.cydroid.softmanager.trafficassistant.utils.StringFormat;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.trafficassistant.viewToolbar.TitleViewToolbar;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeSwitch;
import cyee.widget.CyeeTextView;

public class AppDetailActivity extends CyeeActivity implements OnClickListener {
    private static final String TAG = "AppDetailActivity";

    // Gionee: mengdw <2017-05-18> add for 141974 begin
    private static final int MSG_LOAD_COMPLETED = 0;

    private static final String KEY_DISABLED_APP = "disabledApp";
    private static final String KEY_CONTROL_APP = "controlApp";
    // Gionee: mengdw <2017-05-18> add for 141974 end

    private CyeeSwitch mSwitch;
    private CyeeTextView mHeadTitle;
    private TextView mTitle;
    private TextView mSummary;
    private TextView mTextDataConneceAllow;
    private ImageView mIcon;
    private ImageView mArrowIcon;

    private LinearLayout mLayoutChart;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mLinearLayout;
    // Gionee: mengdw <2017-05-18> add for 141974 begin
    private LinearLayout mLoaderLayout;
    private LinearLayout mAppDetailLayout;
    // Gionee: mengdw <2017-05-18> add for 141974 end

    private GraphicalView mChartView;
    private NetworkTemplate mTemplate;
    private INetworkStatsService mStatsService;
    private INetworkStatsSession mStatsSession;
    private UidDetailProvider mUidDetailProvider;
    private UidDetail mUidDetail;

    private Context mContext;
    // Gionee: mengdw <2016-09-24> add for CR01764183 begin
    TrafficNetworkController mTrafficNetworkController;
    // Gionee: mengdw <2016-09-24> add for CR01764183 end

    private int mUid;
    private int mPosition;
    private int mSimIndex;
    private int mUnit;
    private final int mTop = 60;
    private int mLeft = 70;// 100;
    private final int mDown = 26;
    private final int mRight = 50;// 20;

    private String[] mTxtTimes;
    private String[] mStr;
    private final String[] chartTitle = {""};
    private String[] xAxisValues;

    private double[] yAxisValues;
    private double[] mYValues;
    private double mMaxYValues = 0.0;
    private double mCurrentDayValues = 0.0;
    private double mCurrentWeekValues = 0.0;
    private double mCurrentMonthValues = 0.0;

    private boolean mNotifiFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        // Gionee: mengdw <2017-06-10> add for 154607 begin
        setTheme(R.style.TrafficTheme);
        // Gionee: mengdw <2017-06-10> add for 154607 end
        setContentView(R.layout.trafficassistant_app_detail_layout);
        // Gionee: mengdw <2017-05-18> add for 141974 begin
        initView();
        initData();
        // Gionee: mengdw <2017-05-18> add for 141974 end
        ChameleonColorManager.getInstance().onCreate(this);
        chameleonColorProcess();
    }

    // Gionee: mengdw <2017-05-18> add for 141974 begin
    private void initView() {
        mLayoutChart = (LinearLayout) findViewById(R.id.chart_show);
        mLinearLayout = (LinearLayout) findViewById(R.id.network_layout);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.shared_layout);
        mRelativeLayout.setOnClickListener(this);

        mHeadTitle = (CyeeTextView) findViewById(R.id.head_title);
        mHeadTitle.setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.title);
        mSummary = (TextView) findViewById(R.id.summary);
        mTextDataConneceAllow = (TextView) findViewById(R.id.text_data_connect_allow);

        mIcon = (ImageView) findViewById(R.id.icon);
        mArrowIcon = (ImageView) findViewById(R.id.arrow);
        mSwitch = (CyeeSwitch) findViewById(R.id.switch_menu);
        mSwitch.setOnCheckedChangeListener(mSwitchChangeListener);
        mLoaderLayout = (LinearLayout) findViewById(R.id.app_detail_loader);
        mAppDetailLayout = (LinearLayout) findViewById(R.id.app_detail_layout);
    }

    private void initData() {
        mContext = this;
        mTrafficNetworkController = TrafficNetworkController.getInstance(mContext);
        initIntentInfo();
        initNetwork();
        initActionBar();
    }
    // Gionee: mengdw <2017-05-18> add for 141974 end

    private void initIntentInfo() {
        mPosition = getIntent().getIntExtra("position", 0);
        mSimIndex = getIntent().getIntExtra("simNo", Constant.HAS_NO_SIMCARD);
        mNotifiFlag = getIntent().getBooleanExtra("flag", false);
        mUid = getIntent().getIntExtra("uid", 0);
    }

    private void initNetwork() {
        mTemplate = MobileTemplate.getTemplate(mContext, mSimIndex);
        mUidDetailProvider = new UidDetailProvider(mContext);
        mUidDetail = mUidDetailProvider.getUidDetail(mUid, true);
        Log.d("uids",
                "===> detail :" + mUid + " , " + mUidDetail.label + " ,"
                        + mUidDetailProvider.getPackageName());
        mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager
                .getService(Context.NETWORK_STATS_SERVICE));
        try {
            mStatsSession = mStatsService.openSession();
        } catch (Exception e) {
            Log.d(TAG, "initNetwork e=" + e.toString());
        }
    }

    private void initActionBar() {
        CyeeActionBar mActionBar = getCyeeActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            mTextDataConneceAllow.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        setLoaderVisibility(true);
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                showAppInfo();
            }
        });
        uiThread.start();
    }

    // Gionee: mengdw <2017-06-09> add for 154065 begin
    private synchronized void showAppInfo() {
        Log.d(TAG, "showAppInfo");
        initNetworkControlApp();

        String pkgName = null != mUidDetailProvider ? mUidDetailProvider.getPackageName() : "";
        boolean isDisabledApp = isDisabledApp(pkgName);
        boolean isInvalidControlApp = isInvalidNetworkControlApp(pkgName,mContext);
        sendUiMsg(isDisabledApp, isInvalidControlApp);
    }

    private void initNetworkControlApp() {
        if (null != mTrafficNetworkController) {
            mTrafficNetworkController.init();
        }
    }

    private void sendUiMsg(boolean isDisabledApp, boolean isInvalidControlApp) {
        Message msg = new Message();
        msg.what = MSG_LOAD_COMPLETED;
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_DISABLED_APP, isDisabledApp);
        bundle.putBoolean(KEY_CONTROL_APP, isInvalidControlApp);
        msg.obj = bundle;
        if (null != mHandler) {
            mHandler.sendMessage(msg);
        }
    }
    // Gionee: mengdw <2017-06-09> add for 154065 end

    // Gionee: mengdw <2017-05-18> add for 141974 begin
    private void setLoaderVisibility(boolean isShow) {
        if (isShow) {
            mLoaderLayout.setVisibility(View.VISIBLE);
            mAppDetailLayout.setVisibility(View.GONE);
        } else {
            mLoaderLayout.setVisibility(View.GONE);
            mAppDetailLayout.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isDisabledApp(String packageName) {
        NetworkControlXmlFileUtil xmlFileUtil = NetworkControlXmlFileUtil.getInstance();
        List<String> disableApps = xmlFileUtil.getDisabledApps(Constant.MOBILE);
        return disableApps.contains(packageName);
    }

    public static boolean isInvalidNetworkControlApp(String packageName,Context mContext) {
        if (null == packageName) {
            return true;
        }
        // Encryption App
        ApplicationInfo appInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == appInfo) {
            return true;
        }
        if (appInfo.uid < Constant.SYSTEMID_CONSTANT) {
            return true;
        }
        List<ApplicationInfo> launcherApps = TrafficassistantUtil.getLauncherActivityApps(mContext);
        return !(TrafficassistantUtil.isInApplications(launcherApps, appInfo) && TrafficassistantUtil
                .isUseInternetPermissionApp(mContext, packageName));
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage msg=" + msg.what);
            if (MSG_LOAD_COMPLETED == msg.what) {
                Bundle bundle = (Bundle) msg.obj;
                boolean isDisabledApp = bundle.getBoolean(KEY_DISABLED_APP);
                boolean isInvalidControlApp = bundle.getBoolean(KEY_CONTROL_APP);
                Log.d(TAG, "handleMessage isDisabledApp=" + isDisabledApp
                        + " isInvalidControlApp=" + isInvalidControlApp);
                updateUi(isInvalidControlApp, isDisabledApp);
            }
        }
    };

    private void updateUi(boolean isInvalidControlApp, boolean isDisabledApp) {
        setLoaderVisibility(false);
        setArrowIcon();
        updateAppInfo();
        updateTrafficData();
        setNetworkControlLayoutVisibility(isInvalidControlApp, isDisabledApp);
        setChartViewDisplay();
    }

    private void setArrowIcon() {
        // Gionee: mengdw <2017-06-09> modify for 154065 begin
        if (null != mUidDetailProvider && null != mUidDetailProvider.getPackageName()) {
            mArrowIcon.setVisibility(View.GONE);
        }
        // Gionee: mengdw <2017-06-09> modify for 154065 end
    }

    private void updateAppInfo() {
        mTxtTimes = mContext.getResources().getStringArray(R.array.traffic_time);
        mTxtTimes = mContext.getResources().getStringArray(R.array.traffic_time);
        mStr = new String[3];
        for (int i = 0; i < mTxtTimes.length; i++) {
            mStr[i] = String.format(mTxtTimes[i], "0.0MB");
        }
        mTitle.setText(mUidDetail.label);
        mIcon.setImageDrawable(mUidDetail.icon);
        if (mUid >= Process.FIRST_APPLICATION_UID) {
            mSummary.setVisibility(View.VISIBLE);
        }

        // Gionee: mengdw <2017-06-09> add for 154065 begin
        if (null != mUidDetailProvider) {
            mSummary.setText(mContext.getResources().getString(R.string.version_name)
                    + mUidDetailProvider.getVersionName());
        }
        // Gionee: mengdw <2017-06-09> add for 154065 end

    }

    private void updateTrafficData() {
        try {
            mStatsService.forceUpdate();
        } catch (Exception e) {
            Log.d(TAG, "updateTrafficData e=" + e.toString());
        }
    }

    private void setNetworkControlLayoutVisibility(boolean isInvalidControlApp, boolean isDisabled) {
        if (!isInvalidControlApp) {
            mLinearLayout.setVisibility(View.VISIBLE);
            mSwitch.setChecked(!isDisabled);
        } else {
            mLinearLayout.setVisibility(View.GONE);
        }
    }
    // Gionee: mengdw <2017-05-18> add for 141974 end

    private void setChartViewDisplay() {
        getUsageDetails(0);
        getUsageDetails(1);
        getUsageDetails(2);
        getUsageDetails(mPosition);
        resetSpinnerValue();
        initChartView(mPosition, xAxisValues, yAxisValues, mMaxYValues);
    }

    private final CompoundButton.OnCheckedChangeListener mSwitchChangeListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String pkgName = mUidDetailProvider.getPackageName();
            Log.d(TAG, "onCheckedChanged isChecked=" + isChecked + " pkgName=" + pkgName);
            // Gionee: mengdw <2017-01-21> add for CR01776232 begin
            if (isChecked) {
                mTrafficNetworkController.enableMobileNetwork(pkgName);
            } else {
                mTrafficNetworkController.disableMobileNetwork(pkgName);
            }
            // Gionee: mengdw <2017-01-21> add for CR01776232 end
        }
    }

    private void resetSpinnerValue() {
        mStr[0] = String.format(mTxtTimes[0], StringFormat.getUnitStringByValue(mCurrentDayValues));
        mStr[1] = String.format(mTxtTimes[1], StringFormat.getUnitStringByValue(mCurrentWeekValues));
        mStr[2] = String.format(mTxtTimes[2], StringFormat.getUnitStringByValue(mCurrentMonthValues));
        mHeadTitle.setText(mStr[mPosition]);
    }

    private void getUsageDetails(int flag) {

        initXAxis(flag);
        mLayoutChart.removeAllViews();

        int xAxisSize = xAxisValues.length;
        yAxisValues = new double[xAxisSize];
        mYValues = new double[xAxisSize];

        if (flag == 0) {
            getDataForHour();
        } else if (flag == 1) {
            if (mNotifiFlag) {
                getDataForNotiWeek();
            } else {
                getDataForWeek();
            }

        } else if (flag == 2) {
            getDataForMonth();
        }

        for (int i = 0; i < mYValues.length; i++) {
            yAxisValues[i] = mYValues[i];
        }

        Arrays.sort(mYValues);
        mMaxYValues = mYValues[xAxisSize - 1];

        mUnit = StringFormat.getUnitByMaxValue(mMaxYValues);

        for (int i = 0; i < yAxisValues.length; i++) {
            yAxisValues[i] = StringFormat.formatDataSize(yAxisValues[i], mUnit);
        }

        mMaxYValues = StringFormat.formatDataSize(mMaxYValues, mUnit);
    }

    private void initXAxis(int flag) {
        if (flag == 0) {
            xAxisValues = TimeFormat.getHoursOfDay();
        } else if (flag == 1) {
            if (mNotifiFlag) {
                xAxisValues = TimeFormat.getNotificationWeekArray();
            } else {
                xAxisValues = TimeFormat.getWeekArray();
            }

        } else if (flag == 2) {
            TrafficCalibrateControler trafficCalibrateControler = TrafficCalibrateControler.getInstance(mContext);
            int cycleDay = trafficCalibrateControler.getStartDate(mContext, mSimIndex);
            xAxisValues = TimeFormat.getDaysOfMonth(TrafficassistantUtil.initDateInterval(cycleDay));
        }
    }

    private void getDataForHour() {

        long startTime = 0, endTime = 0;
        Calendar cal = Calendar.getInstance();
        int mCurrentHour = cal.get(Calendar.HOUR_OF_DAY);
        int mCurrentMinute = cal.get(Calendar.MINUTE);
        int mCurrentSecond = cal.get(Calendar.SECOND);
        int hour;
        for (hour = 0; hour <= mCurrentHour; hour++) {
            startTime = TimeFormat.getStartTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH), hour, 0, 0);
            if (hour != mCurrentHour) {
                endTime = TimeFormat.getStartTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH), hour, 59, 59);
            } else {
                endTime = TimeFormat.getStartTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH), mCurrentHour, mCurrentMinute, mCurrentSecond);
            }
            getUsageValues(hour, mYValues, startTime, endTime);
        }

        for (; hour < xAxisValues.length; hour++) {
            mYValues[hour] = 0;
        }
    }

    private void getDataForNotiWeek() {
        long startTime = 0, endTime = 0;
        String[] weekArray = TimeFormat.getNotificationWeekArray();
        int day;
        for (day = 0; day < weekArray.length; day++) {
            String[] startSplit = weekArray[day].split("-");
            if (day == 0) {
                startTime = TimeFormat.getStartTime(Integer.valueOf(startSplit[0]),
                        Integer.valueOf(startSplit[1]), Integer.valueOf(startSplit[2]), 13, 0, 0);
            } else {
                startTime = TimeFormat.getStartTime(Integer.valueOf(startSplit[0]),
                        Integer.valueOf(startSplit[1]), Integer.valueOf(startSplit[2]), 0, 0, 0);
            }

            if (day == weekArray.length - 1) {
                endTime = TimeFormat.getStartTime(Integer.valueOf(startSplit[0]),
                        Integer.valueOf(startSplit[1]), Integer.valueOf(startSplit[2]), 12, 59, 59);
            } else {
                endTime = TimeFormat.getStartTime(Integer.valueOf(startSplit[0]),
                        Integer.valueOf(startSplit[1]), Integer.valueOf(startSplit[2]), 23, 59, 59);
            }
            getUsageValues(day, mYValues, startTime, endTime);
        }

        mCurrentWeekValues = 0.0;
        for (day = 0; day < xAxisValues.length; day++) {
            mCurrentWeekValues += mYValues[day];
        }
    }

    private void getDataForWeek() {

        long startTime = 0, endTime = 0;
        String[] weekArray = TimeFormat.getWeekArray();
        int day;
        for (day = 0; day < weekArray.length; day++) {

            String[] startSplit = weekArray[day].split("-");
            startTime = TimeFormat.getStartTime(Integer.valueOf(startSplit[0]),
                    Integer.valueOf(startSplit[1]), Integer.valueOf(startSplit[2]), 0, 0, 0);
            endTime = TimeFormat.getStartTime(Integer.valueOf(startSplit[0]), Integer.valueOf(startSplit[1]),
                    Integer.valueOf(startSplit[2]), 23, 59, 59);

            if (endTime > System.currentTimeMillis()) {
                endTime = System.currentTimeMillis();
                getUsageValues(day, mYValues, startTime, endTime);
                break;
            }
            getUsageValues(day, mYValues, startTime, endTime);
        }

        for (day++; day < weekArray.length; day++) {
            mYValues[day] = 0;
        }

        mCurrentWeekValues = 0.0;
        for (day = 0; day < xAxisValues.length; day++) {
            mCurrentWeekValues += mYValues[day];
        }
    }

    private void getDataForMonth() {

        long startTime = 0, endTime = 0;
        Calendar cal = Calendar.getInstance();
        String currentDate = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-"
                + cal.get(Calendar.DAY_OF_MONTH);
        int day = 0;
        for (day = 0; day < xAxisValues.length; day++) {
            //guoxt ,modify for Ptest begin
            //if (Objects.equal(xAxisValues[day], currentDate)) {
            if (xAxisValues[day].equals(currentDate)) {
                startTime = TimeFormat.getStartTime(xAxisValues[day], 0, 0, 0);
                endTime = TimeFormat.getStartTime(currentDate, cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                getUsageValues(day, mYValues, startTime, endTime);
                break;
            }
            //guoxt ,modify for Ptest end

            startTime = TimeFormat.getStartTime(xAxisValues[day], 0, 0, 0);
            endTime = TimeFormat.getStartTime(xAxisValues[day + 1], 0, 0, 0);
            getUsageValues(day, mYValues, startTime, endTime);
        }

        mCurrentDayValues = mYValues[day];
        for (day = day + 1; day < xAxisValues.length; day++) {
            mYValues[day] = 0;
        }

        mCurrentMonthValues = 0.0;
        for (day = 0; day < xAxisValues.length; day++) {
            mCurrentMonthValues += mYValues[day];
        }
    }

    private void getUsageValues(int sort, double[] array, long start, long end) {
        NetworkStats stats = null;
        try {
            stats = mStatsSession.getSummaryForAllUid(mTemplate, start, end, false);
        } catch (RemoteException e) {

        }
        NetworkStats.Entry entry = null;
        final int size = stats != null ? stats.size() : 0;
        for (int j = 0; j < size; j++) {
            entry = stats.getValues(j, entry);
            if (mUid == entry.uid) {
                double value = entry.rxBytes + entry.txBytes;
                array[sort] += value;
            }
        }
    }

    private void initChartView(int flag, String[] xAxisValues, double[] yAxisValues, double mMaxYValues) {
        List<String[]> xSeries = new ArrayList<String[]>();
        List<double[]> ySeries = new ArrayList<double[]>();
        xSeries.add(xAxisValues);
        ySeries.add(yAxisValues);

        int[] colors = new int[]{Color.parseColor("#996baedd") /*
                                                                * Color.parseColor
                                                                * ("#d2d2d2")
                                                                */};
        XYMultipleSeriesRenderer mRenderer = buildRenderer(colors);

        XYMultipleSeriesDataset dataset = buildDataset(chartTitle, xSeries, ySeries);

        int mCurrentTimeIndex = getMonthIndex(xAxisValues, flag);
        setChartSettings(flag, xAxisValues, mRenderer, "", "", "", mCurrentTimeIndex, xAxisValues.length,
                0.00, Math.ceil(mMaxYValues), Color.parseColor("#888888"), Color.parseColor("#888888"));

        ((XYSeriesRenderer) mRenderer.getSeriesRendererAt(0)).setDisplayChartValues(false);

        mChartView = ChartFactory.getBarChartView(AppDetailActivity.this, dataset, mRenderer, Type.DEFAULT);

        mChartView.setOnTouchListener(onTouch);

        mLayoutChart.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT, 40));
    }

    // Gionee <jianghuan> <2013-11-25> add for CR00975553 begin
    OnTouchListener onTouch = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean action = true;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    action = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    action = true;
                    break;
                case MotionEvent.ACTION_UP:
                    action = false;
                    break;

                default:
                    break;
            }

            setTouchViewToolbar((int) event.getX(), (int) event.getY(), action);
            return false;
        }
    };

    private TitleViewToolbar mTitleViewToolbar;

    private void setTouchViewToolbar(int x, int y, boolean action) {
        try {
            Log.d(TAG, "setTouchViewToolbar action=" + action + " x=" + x + " y=" + y);
            int width, heigh;
            int[] location = new int[2];
            String popUPText = "";
            if (!action) {
                if (mTitleViewToolbar != null && mTitleViewToolbar.isShowing()) {
                    mTitleViewToolbar.hide();
                }
                // mChartView.setClickPointColor(-1);
                return;
            }

            SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
            if (seriesSelection == null) {
                if (mTitleViewToolbar != null && mTitleViewToolbar.isShowing()) {
                    mTitleViewToolbar.hide();
                }
                //mChartView.setClickPointColor(-1);
            } else {
                mLayoutChart.getLocationInWindow(location);
                // width = mLayoutChart.getWidth();
                heigh = mLayoutChart.getHeight();
                double left = x - 10;// x;
                double top = location[1] + heigh - 65 - (heigh - 70 - 65) * seriesSelection.getValue()
                        / (Math.ceil(mMaxYValues));

                if (null == mTitleViewToolbar) {
                    mTitleViewToolbar = new TitleViewToolbar(mContext, mChartView.getScrollX(),
                            mChartView.getScrollY());
                }
                mTitleViewToolbar.setPosition((int) left, (int) top);
                if (mPosition == 0) {
                    popUPText = " " + ((int) seriesSelection.getXValue() - 1) + ":00 - "
                            + (int) seriesSelection.getXValue() + ":00 \n" + seriesSelection.getValue()
                            + StringFormat.getUnit(mUnit) + "\n";
                } else /* if(mPosition ==1) */ {
                    String[] timeSplit = xAxisValues[(int) seriesSelection.getXValue() - 1].split("-");
                    String timeZone = " " + Integer.valueOf(timeSplit[1])
                            + mContext.getString(R.string.month) + Integer.valueOf(timeSplit[2])
                            + mContext.getString(R.string.day) + " " + "\n";
                    popUPText = timeZone + seriesSelection.getValue() + StringFormat.getUnit(mUnit) + "\n";
                }

                mTitleViewToolbar.setText(popUPText);
                mTitleViewToolbar.show();
            }
        } catch (Exception e) {
            Log.d(TAG, "setTouchViewToolbar e=" + e.toString());
        }
    }

    // Gionee <jianghuan> <2013-11-25> add for CR00975553 end
    private int getMonthIndex(String[] xAxisValues, int flag) {
        int mCurrentTimeIndex = 0;
        Calendar cal = Calendar.getInstance();
        if (flag == 0) {
            mCurrentTimeIndex = cal.get(Calendar.HOUR_OF_DAY);
        } else if (flag == 1) {
            String mCurrentDate = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-"
                    + cal.get(Calendar.DAY_OF_MONTH);
            for (String date : xAxisValues) {
                if (date.equals(mCurrentDate)) {
                    break;
                } else {
                    mCurrentTimeIndex++;
                }
            }
        }

        return mCurrentTimeIndex;
    }

    private XYMultipleSeriesDataset buildDataset(String[] title, List<String[]> x, List<double[]> y) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int length = title.length;
        for (int i = 0; i < length; i++) {
            CategorySeries series = new CategorySeries(title[i]);
            double[] yV = (double[]) y.get(i);
            for (int k = 0; k < yV.length; k++) {
                series.add(yV[k]);
            }
            dataset.addSeries(series.toXYSeries());
        }
        return dataset;
    }

    private XYMultipleSeriesRenderer buildRenderer(int[] colors) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        float mScale = mContext.getResources().getDisplayMetrics().density;
        if (mScale == 4.0) {
            mLeft = 100;
        } else if (mScale == 3.0) {
            mLeft = 95;
        } else {
            mLeft = 70;
        }
        renderer.setMargins(new int[]{mTop, mLeft, mDown, mRight});
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            renderer.addSeriesRenderer(r);
        }
        return renderer;
    }

    @SuppressWarnings("deprecation")
    private void setChartSettings(int flag, String[] xAxisValues, XYMultipleSeriesRenderer renderer,
                                  String title, String xTitle, String yTitle, double xMin, double xMax,
                                  double yMin, double yMax, int axesColor, int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);

        double min = 0;
        double max = xMax;
        renderer.setXAxisMin(min);
        renderer.setXAxisMax(max);

        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax((yMax == 0.0) ? 5 : yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);

        renderer.setXLabels(0);
        /*guoxt modify for CR01493047 begin */
        renderer.setYLabels(0);
        /*guoxt modify for CR01493047 end */
        renderer.setXLabelsColor(Color.parseColor("#888888"));
        renderer.setYLabelsColor(0, Color.parseColor("#888888"));
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setYLabelsPadding(5.0f);

        renderer.setPanEnabled(true, false);
        renderer.setPanLimits(new double[]{0, xMax + 1, 0, yMax + 1});
        renderer.setMarginsColor(Color.parseColor("#EFEFEF"));
        renderer.setBackgroundColor(Color.parseColor("#EFEFEF"));
        renderer.setApplyBackgroundColor(true);

        renderer.setZoomButtonsVisible(false);
        renderer.setZoomEnabled(false, false);
        renderer.setInScroll(true);
        renderer.setShowGridX(true);
        // renderer.setBarWidth(16f);
        float mScale = mContext.getResources().getDisplayMetrics().density;
        if (mScale == 4.0) {
            renderer.setBarWidth(30f);
            renderer.setLabelsTextSize(35);
        } else if (mScale == 3.0) {
            renderer.setBarWidth(20f);
            renderer.setLabelsTextSize(30);
        } else if (mScale == 2.0) {
            renderer.setBarWidth(14f);
            renderer.setLabelsTextSize(23);
        } else {
            renderer.setBarWidth(9f);
            renderer.setLabelsTextSize(19);
        }
        // renderer.setLabelsTextSize(25);
        renderer.setShowLegend(false);
        setXTextLabel(renderer, xAxisValues);
        /*guoxt modify for CR01493047 begin */
        if (yMax < 3) {
            renderer.addYTextLabel((double) 0, "");
            renderer.addYTextLabel((double) yMax, yMax + "");
        } else {
            renderer.addYTextLabel((double) 0, "");
            renderer.addYTextLabel((double) yMax / 3, StringFormat.getStringFormatA(yMax / 3));
            renderer.addYTextLabel((double) 2 * yMax / 3, StringFormat.getStringFormatA(2 * yMax / 3));
            renderer.addYTextLabel((double) yMax, yMax + "");
        }
        /*guoxt modify for CR01493047 end */
    }

    private void setXTextLabel(XYMultipleSeriesRenderer renderer, String[] xAxisValues) {
        // String xStartLabel, String xEndLabel) {
        String xStartLabel = xAxisValues[0];
        String xEndLabel = xAxisValues[xAxisValues.length - 1];
        if (xStartLabel.length() > 3) {
            xStartLabel = xStartLabel.substring(5);
        } else {
            xStartLabel = xStartLabel.concat(":00");
        }

        if (xEndLabel.length() > 3) {
            xEndLabel = xEndLabel.substring(5);
        } else {
            xEndLabel = "24:00";
        }
        renderer.addXTextLabel((double) 0, xStartLabel);
        renderer.addXTextLabel(xAxisValues.length / 2 + 0l, "......");
        renderer.addXTextLabel((double) xAxisValues.length, xEndLabel);
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.shared_layout:
                String packageName = mUidDetailProvider.getPackageName();
                if (packageName == null) {

                } else {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", packageName, null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                break;

            case R.id.head_title:
                new CyeeAlertDialog.Builder(mContext)
                        .setTitle(mContext.getResources().getString(R.string.usage_detail))
                        .setSingleChoiceItems(mStr, mPosition, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPosition = which;
                                setChartViewDisplay();
                                dialog.dismiss();
                            }
                        }).show();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
        // Gionee: mengdw <2017-06-09> add for 154065 begin
        clearResources();
        // Gionee: mengdw <2017-06-09> add for 154065 end
    }

    // Gionee: mengdw <2017-06-09> add for 154065 begin
    private synchronized void clearResources() {
        mUidDetailProvider.clearCache();
        mUidDetailProvider = null;
        TrafficStats.closeQuietly(mStatsSession);
        // Gionee: mengdw <2017-05-18> add for 141974 begin
        if (null != mHandler && mHandler.hasMessages(MSG_LOAD_COMPLETED)) {
            mHandler.removeMessages(MSG_LOAD_COMPLETED);
            mHandler = null;
        }
        // Gionee: mengdw <2017-05-18> add for 141974 end
    }
    // Gionee: mengdw <2017-06-09> add for 154065 end
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end