package com.wheatek.proxy.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.drawable.ColorDrawable;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.trafficassistant.SIMInfoWrapper;
import com.cydroid.softmanager.trafficassistant.SIMParame;
import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;
import com.cydroid.softmanager.trafficassistant.controler.TrafficNetworkController;
import com.cydroid.softmanager.trafficassistant.net.AppItem;
import com.cydroid.softmanager.trafficassistant.net.SummaryForAllUidLoader;
import com.cydroid.softmanager.trafficassistant.net.UidDetail;
import com.cydroid.softmanager.trafficassistant.net.UidDetailProvider;
import com.cydroid.softmanager.trafficassistant.service.TrafficMonitorControlService;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.MobileTemplate;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.example.systemmanageruidemo.actionpresent.TrafficMonitorPresent2;
import com.example.systemmanageruidemo.actionview.TrafficMonitorView2;
import com.example.systemmanageruidemo.trafficmonitor.TrafficMonitorMainActivity;

import com.example.systemmanageruidemo.actionview.TrafficMonitorView;
import com.example.systemmanageruidemo.trafficmonitor.TrafficMonitorMainActivity2;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraPagerBean;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraRecyBean;

import java.util.ArrayList;
import java.util.List;

import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static com.cydroid.softmanager.trafficassistant.AppDetailActivity.isDisabledApp;
import static com.cydroid.softmanager.trafficassistant.AppDetailActivity.isInvalidNetworkControlApp;
import static com.cydroid.softmanager.trafficassistant.TrafficRankActivity.getAppsUsingMobileData;

public class HostTrafficMonitorMainActivity extends HostProxyActivity<TrafficMonitorView2> implements TrafficMonitorPresent2 {
    {
        attach(new TrafficMonitorMainActivity2());
    }

    public static void TrafficPackageSettingNoti(Context context) {
        if (TrafficassistantUtil.getSimCount(context) == 0) {
            return;
        }
        int simIndex = TrafficassistantUtil.getSimCardNo(context);
        MainProcessSettingsProviderHelper providerHelper = new MainProcessSettingsProviderHelper(context);
        if (providerHelper.getBoolean(TrafficassistantUtil.getSimSetting(simIndex), false)) { // setting
            return;
        }
        // Gionee: mengdw <2016-06-29> delete for CR01724694 begin
        // popNoti(context, simIndex);
        // Gionee: mengdw <2016-06-29> delete for CR01724694 end
        providerHelper.putBoolean(TrafficassistantUtil.getSimNotification(simIndex), true);
    }

    private static final String TAG = HostTrafficMonitorMainActivity.class.getSimpleName();
    private TrafficMonitorView2 viewAvtion;
    private Context mContext;
    private TrafficCalibrateControler mTrafficCalibrateControler;
    private INetworkStatsService mStatsService;
    private INetworkStatsSession mStatsSession;
    private NetworkPolicyManager mPolicyManager;
    private NetworkTemplate mTemplate;
    private UidDetailProvider mUidDetailProvider;
    private TrafficNetworkController mTrafficNetworkController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setTitle(R.string.traffic_control_summary);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.host_bar_bg_white)));
        getSupportActionBar().setElevation(0.0f);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.svg_icon_back_left);
        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(getColor(R.color.cyee_transparent));
        getWindow().setBackgroundDrawable(new ColorDrawable(getColor(R.color.host_bar_bg_white)));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUidDetailProvider.clearCache();
        mUidDetailProvider = null;
        TrafficStats.closeQuietly(mStatsSession);
    }

    private void init() {
        mContext = this;
        TrafficMonitorControlService.processMonitorControlServiceIntent(mContext, true);
        mTrafficCalibrateControler = TrafficCalibrateControler.getInstance(mContext);
        mTrafficNetworkController = TrafficNetworkController.getInstance(mContext);
        initNetworkInfo(mContext, 0);
        mTrafficNetworkController.init();
    }

    private void initNetworkInfo(Context context, int simIndex) {
        mTemplate = MobileTemplate.getTemplate(context, simIndex);
        mUidDetailProvider = new UidDetailProvider(context);
        mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager
                .getService(Context.NETWORK_STATS_SERVICE));
        mPolicyManager = NetworkPolicyManager.from(context);
        try {
            mStatsSession = mStatsService.openSession();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setViewAction(TrafficMonitorView2 viewAvtion) {
        this.viewAvtion = viewAvtion;
    }

    @Override
    public TrafficMonitorView2 getViewAction() {
        return viewAvtion;
    }

    @Override
    public void onRequestSIM(TraPagerBean object) {
        this.data = object;
        startRequesSIM();
    }

    private void startRequesSIM() {
        SIMInfoWrapper wrapper = SIMInfoWrapper.getDefault(mContext);
        int count = wrapper.getInsertedSimCount();
        data.setList(new ArrayList<>());
        for (int i = 0; i < count; i++) {
            SIMParame simInfo = wrapper.getInsertedSimInfo().get(i);
            String name = simInfo.mDisplayName;
            long id = simInfo.mSimId;
            TraPagerBean.SIMBean bean = new TraPagerBean.SIMBean(id, name);
            bean.setIssetted(mTrafficCalibrateControler.isTafficPackageSetted(mContext, i));
            data.getList().add(bean);
            int[] todayDate = TimeFormat.getNowTimeArray();
            long startTime = TimeFormat.getStartTime(todayDate[0], todayDate[1] + 1, todayDate[2], 0, 0, 0);
            bean.setUsedFlow(TrafficassistantUtil.getTrafficData(mContext, i, startTime, 0, 0));
            bean.setTraPack(mTrafficCalibrateControler.getCommonTotalTaffic(mContext, i));
            bean.setSurplusFlow(mTrafficCalibrateControler.getCommonLeftTraffic(mContext, i));
        }
        data.setSimCardScount(data.getList().size());
        responseSIM(data);

    }

    private final LoaderManager.LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderManager.LoaderCallbacks<NetworkStats>() {


        @Override
        public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
            return new SummaryForAllUidLoader(mContext, mStatsSession, args);
        }

        @Override
        public void onLoadFinished(Loader<NetworkStats> arg0, NetworkStats arg1) {
            final int[] restrictedUids = mPolicyManager.getUidsWithPolicy(POLICY_REJECT_METERED_BACKGROUND);
            refreshListView(arg1, restrictedUids);
        }

        @Override
        public void onLoaderReset(Loader<NetworkStats> arg0) {
            refreshListView(null, new int[0]);
        }
    };


    // 查询图标状态的耗时操作，注意不要占用主线程
    private void refreshListView(final NetworkStats stats, final int[] restrictedUids) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            fun(stats, restrictedUids);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fun(stats, restrictedUids);
                }
            }).start();
        }
    }

    private void fun(NetworkStats stats, int[] restrictedUids) {
        mlist.clear();
        ArrayList<AppItem> appItems = getAppsUsingMobileData(stats);
        for (AppItem appItem : appItems) {
            UidDetail detail = mUidDetailProvider.getUidDetail(appItem.key, true);
            String name = detail.label.toString();
            long size = appItem.total;
            String pkgName = mUidDetailProvider.getPackageName();
            boolean isDisabledApp = isDisabledApp(pkgName);
            boolean isInvalidControlApp = isInvalidNetworkControlApp(pkgName, mContext);
            TraRecyBean bean = new TraRecyBean(pkgName, name);
            bean.setIslimit(!isDisabledApp);
            bean.setImageId(detail.icon);
            bean.setUsedTraSize(size);
            bean.setInvalidControlApp(isInvalidControlApp);
            mlist.add(bean);
        }
        refresh(mlist);
    }


    @Override
    public void responseSIM(TraPagerBean object) {
        viewAvtion.onResponseSIM(object);
    }

    private TraPagerBean data;
    private List<TraRecyBean> mlist;

    @Override
    public void onInitData(List<TraRecyBean> list) {
        this.mlist = list;
        long[] timeZone = new long[3];
        int[] timeArray = TimeFormat.getNowTimeArray();
        timeZone[0] = TimeFormat.getStartTime(timeArray[0], timeArray[1], 0, 0, 0, 0);
        timeZone[1] = System.currentTimeMillis();
        timeZone[2] = System.currentTimeMillis();
        getLoaderManager().restartLoader(Constant.LOADER_SUMMARY,
                SummaryForAllUidLoader.buildArgs(mTemplate, timeZone[0], timeZone[1]),
                mSummaryCallbacks);
    }

    @Override
    public void refresh(List<TraRecyBean> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewAvtion.onRefresh(mlist);
            }
        });
    }

    @Override
    public void onAppChangeState(TraRecyBean object, boolean ischecked) {
        if (ischecked) {
            mTrafficNetworkController.enableMobileNetwork(object.getPackageName());
        } else {
            mTrafficNetworkController.disableMobileNetwork(object.getPackageName());
        }

    }

}