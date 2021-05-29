//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant;

import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.trafficassistant.actionBarTab.ActionBarTabs;
import com.cydroid.softmanager.trafficassistant.actionBarTab.TabInfos;
import com.cydroid.softmanager.trafficassistant.adapter.MobileDataUsageAdapter;
import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;
import com.cydroid.softmanager.trafficassistant.net.AppItem;
import com.cydroid.softmanager.trafficassistant.net.ChartData;
import com.cydroid.softmanager.trafficassistant.net.ChartDataLoader;
import com.cydroid.softmanager.trafficassistant.net.SummaryForAllUidLoader;
import com.cydroid.softmanager.trafficassistant.net.UidDetailProvider;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.MobileTemplate;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.google.android.collect.Lists;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cyee.app.CyeeActionBar.Tab;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeListView;

import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.TrafficStats.UID_REMOVED;
import static android.net.TrafficStats.UID_TETHERING;

public class TrafficRankActivity extends ActionBarTabs {
    private static final String TAG = "TrafficRankActivity";

    private final ArrayList<LinearLayout> mLayout = new ArrayList<LinearLayout>();
    private final ArrayList<TextView> mTextView = new ArrayList<TextView>();
    private final ArrayList<TextView> mTextViewEmpty = new ArrayList<TextView>();
    private final ArrayList<CyeeListView> mListView = new ArrayList<CyeeListView>();
    private final ArrayList<MobileDataUsageAdapter> mListAdapter = new ArrayList<MobileDataUsageAdapter>();
    private int[] mTextViewArray;
    private INetworkStatsService mStatsService;
    private INetworkStatsSession mStatsSession;
    private NetworkPolicyManager mPolicyManager;
    private NetworkTemplate mTemplate;
    private UidDetailProvider mUidDetailProvider;
    private ChartData mChartData;

    private final int mInsetSide = 0;
    private int mPosition = 1;
    private int mSimIndex;
    private boolean mEntranceFromNoti = false;
    private ArrayAdapter<String> mSpinnerAdapter;
    private final ArrayList<AppItem> mAppItems = new ArrayList<AppItem>();//Lists.newArrayList();
    private TrafficCalibrateControler mTrafficCalibrateControler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (this instanceof AppCompatActivity) {
           getSupportActionBar().hide();
        }
        getEntranceFlag(this);
        initActionTabInfo(this);
        super.onCreate(savedInstanceState);

        mSimIndex = getIntent().getIntExtra(Constant.SIM_VALUE, Constant.HAS_NO_SIMCARD);
        mTrafficCalibrateControler = TrafficCalibrateControler.getInstance(this);
        initNetworkInfo(mContext, mSimIndex);
        // Gionee: mengdw <2017-05-18> add for 144347 begin
        ChameleonColorManager.getInstance().onCreate(this);
        // Gionee: mengdw <2017-05-18> add for 144347 end
    }

    private void initActionTabInfo(Context context) {
        String[] titlesD = new String[]{context.getString(R.string.traffic_day),
                context.getString(R.string.traffic_week), context.getString(R.string.traffic_month)};
        int[] layoutIdsD = null;

        //guoxt modif for CR01625070 begin
        if (Consts.gnSwFlag) {
            layoutIdsD = new int[]{R.layout.sw_trafficassistant_rank_layout,
                    R.layout.sw_trafficassistant_rank_layout, R.layout.sw_trafficassistant_rank_layout};
        } else {
            layoutIdsD = new int[]{R.layout.trafficassistant_rank_layout,
                    R.layout.trafficassistant_rank_layout, R.layout.trafficassistant_rank_layout};
        }
        //guoxt modif for CR01625070 end
        mTabInfos = new TabInfos();
        mTabInfos.setTabNums(titlesD.length);
        mTabInfos.setTabTexts(titlesD);
        mTabInfos.setLayoutIds(layoutIdsD);
        //Gionee luoguangming 2015.07.10 modify CR01545978 begin
        //mTabInfos.setTabCurrItem(mPosition);
        if (mEntranceFromNoti) {
            mTabInfos.setTabCurrItem(1);
        }
        //Gionee luoguangming 2015.07.10 modify CR01545978  end
    }

    private void initNetworkInfo(Context context, int simIndex) {
        mTemplate = MobileTemplate.getTemplate(context, simIndex);
        Log.d(TAG, "initNetworkInfo simIndex=" + simIndex + " mTemplate=" + mTemplate);
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

    public void initUI(int children) {
        View view = mSections.get(children);
        //Gionee <jiangsj> <20170419> add for 113672 begin
        if (view != null && TextUtils.getLayoutDirectionFromLocale(getResources().getConfiguration().locale) == 1) {
            view.setRotationY(180);
        }
        //Gionee <jiangsj> <20170419> add for 113672 end
        initNetworkInfo(mContext, mSimIndex);
        mLayout.add(children, (LinearLayout) view.findViewById(R.id.layout_content));
        mTextView.add(children, (TextView) view.findViewById(R.id.content_display));
        mTextViewEmpty.add(children, (TextView) view.findViewById(R.id.text_empty));

        mTextViewArray = new int[]{R.string.traffic_day_no, R.string.traffic_week_no, R.string.traffic_month_no};
        mTextViewEmpty.get(children).setText(mTextViewArray[children]);

        mListView.add(children, (CyeeListView) view.findViewById(R.id.traffic_list));
        mListAdapter.add(children, new MobileDataUsageAdapter(mUidDetailProvider, mInsetSide, mAppItems));
        mListView.get(children).setOnItemClickListener(mListListener);
        mListView.get(children).setAdapter(mListAdapter.get(children));
    }

    @Override
    public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        super.onTabSelected(arg0, arg1);
        mPosition = arg0.getPosition();
        //Gionee: mengdw <2015-10-10> add log for CR01538409  begin
        Log.d(TAG, "onTabSelected mPosition=" + mPosition);
        //Gionee: mengdw <2015-10-10> add log for CR01538409  end
        mTextViewEmpty.get(mPosition).setText(mTextViewArray[mPosition]);
        mTextViewEmpty.get(mPosition).setVisibility(View.GONE);
        updateDetailData();
    }

    private String getIntentCycleTime() {
        return getIntent().getStringExtra(Constant.TRAFFIC_CYCLE);
    }

    private String[] analyticalData() {
        String cycle = getIntentCycleTime();
        String[] array = cycle.split("-");
        return array;
    }

    private void getEntranceFlag(Context context) {
        //YouJuAgent.onEvent(context, "Noti_TrafficRank_Click");
        mEntranceFromNoti = getIntentCycleTime() != null;
    }

    private void setVisibilityAndText(boolean flag) {
        if (flag) {
            mTextViewEmpty.get(mPosition).setVisibility(View.GONE);
            mLayout.get(mPosition).setVisibility(View.VISIBLE);
        } else {
            mTextViewEmpty.get(mPosition).setVisibility(View.VISIBLE);
            mLayout.get(mPosition).setVisibility(View.GONE);
        }

        if (flag) {
            /*guoxt modify for #130885 begin */
            if (Consts.gnKRFlag) {
                mTextView.get(mPosition).setText(String.format(mContext.getResources().getString(R.string.traffic_monitor_display_kr),
                        getTextViewText(mPosition)));
            } else {
                mTextView.get(mPosition).setText(String.format(mContext.getResources().getString(R.string.traffic_monitor_display),
                        getTextViewText(mPosition)));
            }
            /*guoxt modify for #130885 end */
        }
    }

    private String getTextViewText(int position) {
        String txt = "";
        String[] timeString = {"/", ""};

        switch (position) {
            case 0:
                int[] todayArray = TimeFormat.getNowTimeArray();
                txt = (todayArray[1] + 1) + timeString[0] + todayArray[2]
                        + timeString[1];
                break;

            case 1:
                if (getIntentCycleTime() != null) {
                    String[] array = analyticalData();
                    txt = array[0];
                } else {
                    String[] weekArray = TimeFormat.getWeekArray();
                    String[] splitStart = weekArray[0].split("-");
                    txt = Integer.parseInt(splitStart[1]) + timeString[0]
                            + Integer.parseInt(splitStart[2]) + timeString[1];

                    String[] splitEnd = weekArray[weekArray.length - 1].split("-");
                    txt += "-" + Integer.parseInt(splitEnd[1]) + timeString[0]
                            + Integer.parseInt(splitEnd[2]) + timeString[1];
                }

                break;

            case 2:
                int startDay = mTrafficCalibrateControler.getStartDate(mContext, mSimIndex);
                int[] date = TrafficassistantUtil.initDateInterval(startDay);
                txt = date[1] + timeString[0] + date[2] + timeString[1] + "-"
                        + date[4] + timeString[0] + date[5] + timeString[1];
                break;
            default:
                break;
        }
        return txt;
    }

    //Gionee: mengdw <2015-11-05> delete for CR01585065  begin
    /*@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            onBackPressed();
        }
        return super.onKeyUp(keyCode, event);
    }*/
    //Gionee: mengdw <2015-11-05> delete for CR01585065  end

    @Override
    protected void onResume() {
        super.onResume();
        //Gionee: mengdw <2016-08-23> add log for CR01747432  begin
        Log.d(TAG, "onResume");
        //Gionee: mengdw <2016-08-23> add log for CR01747432  end
        try {
            mStatsService.forceUpdate();
        } catch (RemoteException e) {

        }

        getLoaderManager().restartLoader(Constant.LOADER_CHART_DATA,
                ChartDataLoader.buildArgs(mTemplate, null),
                mChartDataCallbacks);
        //YouJuAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //YouJuAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        mUidDetailProvider.clearCache();
        mUidDetailProvider = null;
        TrafficStats.closeQuietly(mStatsSession);
        super.onDestroy();
        // Gionee: mengdw <2017-05-18> add for 144347 begin
        ChameleonColorManager.getInstance().onDestroy(this);
        // Gionee: mengdw <2017-05-18> add for 144347 end
    }

    private final OnItemClickListener mListListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            final AppItem app = (AppItem) arg0.getItemAtPosition(arg2);
            if (app != null) {
                if (app.key < 10000) {
                    return;
                }
                Intent intent = new Intent(TrafficRankActivity.this,
                        AppDetailActivity.class);
                intent.putExtra("uid", app.key);
                intent.putExtra("position", mPosition);
                intent.putExtra("simNo", mSimIndex);
                intent.putExtra("flag", mEntranceFromNoti);
                TrafficRankActivity.this.startActivity(intent);
            }
        }
    };

    private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<ChartData>() {

        @Override
        public Loader<ChartData> onCreateLoader(int id, Bundle args) {
            return new ChartDataLoader(mContext, mStatsSession, args);
        }

        @Override
        public void onLoadFinished(Loader<ChartData> arg0, ChartData arg1) {
            updateDetailData();
        }

        @Override
        public void onLoaderReset(Loader<ChartData> arg0) {
        }

    };

    private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<NetworkStats>() {

        @Override
        public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
            return new SummaryForAllUidLoader(mContext, mStatsSession, args);
        }

        @Override
        public void onLoadFinished(Loader<NetworkStats> arg0, NetworkStats arg1) {
            //Gionee: mengdw <2015-10-10> add log for CR01538409  begin
            Log.d(TAG, "onLoadFinished call refreshListView");
            //Gionee: mengdw <2015-10-10> add log for CR01538409  end
            final int[] restrictedUids = mPolicyManager.getUidsWithPolicy(POLICY_REJECT_METERED_BACKGROUND);
            refreshListView(arg1, restrictedUids);
        }

        @Override
        public void onLoaderReset(Loader<NetworkStats> arg0) {
            refreshListView(null, new int[0]);
        }
    };

    private void refreshListView(NetworkStats stats, int[] restrictedUids) {
		/*mAppItems.clear();
		mAppItems = getAppsUsingMobileData(stats);
        long mLargest = (mAppItems.size() > 0) ? mAppItems.get(0).total : 0;
        setVisibilityAndText((mLargest == 0) ? false : true);
        mListAdapter.get(mPosition).notifyDataSetChanged(mAppItems, mLargest);*/
        ArrayList<AppItem> appItems = getAppsUsingMobileData(stats);
        long mLargest = (appItems.size() > 0) ? appItems.get(0).total : 0;
        //Gionee: mengdw <2015-10-10> add log for CR01538409  begin
        Log.d(TAG, "refreshListView mLargest=" + mLargest);
        //Gionee: mengdw <2015-10-10> add log for CR01538409  end
        setVisibilityAndText(mLargest != 0);
        mListAdapter.get(mPosition).notifyDataSetChanged(appItems, mLargest);
    }

    public static ArrayList<AppItem> getAppsUsingMobileData(NetworkStats stats) {
        ArrayList<AppItem> items = new ArrayList<>();
        final int currentUserId = ActivityManager.getCurrentUser();
        final SparseArray<AppItem> knownItems = new SparseArray<AppItem>();
        final int size = (stats != null) ? stats.size() : 0;
        NetworkStats.Entry entry = null;
        for (int i = 0; i < size; i++) {
            entry = stats.getValues(i, entry);

            final int uid = entry.uid;
            final int collapseKey;
            /*if (uid < 10000) {
                continue;
            }*/
            if (UserHandle.isApp(uid)) {
                if (UserHandle.getUserId(uid) == currentUserId) {
                    collapseKey = uid;
                } else {
                    collapseKey = UidDetailProvider.buildKeyForUser(UserHandle
                            .getUserId(uid));
                }
            } else if (uid == UID_REMOVED || uid == UID_TETHERING) {
                collapseKey = uid;
            } else {
                collapseKey = android.os.Process.SYSTEM_UID;
            }

            AppItem item = knownItems.get(collapseKey);
            if (item == null) {
                item = new AppItem(collapseKey);
                items.add(item);
                knownItems.put(item.key, item);
            }

            item.addUid(uid);
            item.total += entry.rxBytes + entry.txBytes;
        }

        Collections.sort(items, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem lhs, AppItem rhs) {
                return Long.compare(rhs.total, lhs.total);
                //  return 0;
            }
        });
        return items;
    }

    private void updateDetailData() {
        long[] timeZone = getTimeZone(mPosition);
        //Gionee: mengdw <2015-10-10> add log for CR01538409  begin
        Log.d(TAG, "updateDetailData strart_time=" + timeZone[0] + " end_time=" + timeZone[1]);
        //Gionee: mengdw <2015-10-10> add log for CR01538409  end
        getLoaderManager().restartLoader(Constant.LOADER_SUMMARY,
                SummaryForAllUidLoader.buildArgs(mTemplate, timeZone[0], timeZone[1]),
                mSummaryCallbacks);

    }

    public  long[] getTimeZone(int position) {
        long[] timeZone = new long[3];
        int[] timeArray = null;

        switch (position) {
            case 0:
                timeArray = TimeFormat.getNowTimeArray();
                timeArray[1] += 1;
                break;

            case 1:
                if (getIntentCycleTime() != null) {
                    String[] array = analyticalData();
                    timeZone[0] = Long.valueOf(array[1]);
                    timeZone[1] = Long.valueOf(array[2]);
                } else {
                    String[] weekArray = TimeFormat.getWeekArray();
                    String[] split = weekArray[0].split("-");
                    timeArray = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), 0, 0, 0};

                }
                break;

            case 2:
                int startDay = mTrafficCalibrateControler.getStartDate(mContext, mSimIndex);
                timeArray = TrafficassistantUtil.initDateInterval(startDay);
                break;
            default:
                break;
        }

        if (timeArray != null) {
            timeZone[0] = TimeFormat.getStartTime(timeArray[0], timeArray[1], timeArray[2], 0, 0, 0);
            timeZone[1] = System.currentTimeMillis();
        }

        timeZone[2] = System.currentTimeMillis();

        return timeZone;
    }
}
//Gionee <jianghuan> <2013-09-29> add for CR00975553 end
