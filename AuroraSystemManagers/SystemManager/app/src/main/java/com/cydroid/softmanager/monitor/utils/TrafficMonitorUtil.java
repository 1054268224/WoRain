package com.cydroid.softmanager.monitor.utils;

import cyee.app.CyeeActivity;
import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.NetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.TrafficStats;
import static android.net.TrafficStats.UID_REMOVED;
import static android.net.TrafficStats.UID_TETHERING;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.SparseArray;
import android.view.View;

import com.cydroid.softmanager.trafficassistant.net.AppInfo;
import com.cydroid.softmanager.trafficassistant.net.AppItem;
import com.cydroid.softmanager.trafficassistant.net.SummaryForAllUidLoader;
import com.cydroid.softmanager.trafficassistant.net.UidDetailProvider;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.MobileTemplate;
import com.cydroid.softmanager.trafficassistant.utils.StringFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.google.android.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficMonitorUtil {
    private static final String TAG = "TrafficMonitorUtil";
    private final Context mContext;
    private NetworkTemplate mTemplate;
    // private UidDetailProvider mUidDetailProvider;
    private INetworkStatsService mStatsService;
    private INetworkStatsSession mStatsSession;
    // private NetworkPolicyManager mPolicyManager;
    private final Map<String, String> mMapAppList = new HashMap<String, String>();
    private long start;
    private long end;
    private long[] mTrafficArray;
    private int[] mUidArray;
    private String mThreeMaxApp = "";
    private final int mSimIndex;
    // Gionee: mengdw <2015-10-20> add for CR01571760 begin
    private long mHotSportTraffic = 0;
    // Gionee: mengdw <2015-10-20> add for CR01571760 end
    
    public TrafficMonitorUtil(Context context, long startTime, long endTime, int index) {
        mContext = context;
        start = startTime;
        end = endTime;
        mSimIndex = index;
    }

    // Gionee: mengdw <2015-10-20> add for CR01571760 begin
    public void updateQueryTime(long startTime, long endTime) {
        start = startTime;
        end = endTime;
    }

    public long getHotSportTraffic() {
        try {
            mStatsService.forceUpdate();
        } catch (Exception e) {
            Log.d("TrafficMonitorUtil", "getHotSportTraffic e=" + e.toString());
        }
        getApplicationsTraffic(mContext);
        return mHotSportTraffic;
    }
    // Gionee: mengdw <2015-10-20> add for CR01571760 end

    public void initNetworkParam(Context context, int index) {
        Log.d(TAG, "initNetworkParam index=" + index);
        mTemplate = MobileTemplate.getTemplate(context, index);
        // Gionee: mengdw <2017-03-16> add for 69788 begin
        try {
            mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager
                    .getService(Context.NETWORK_STATS_SERVICE));
            mStatsSession = mStatsService.openSession();
        } catch (Exception e) {
            Log.d(TAG, "initNetworkParam Exception e=" + e.toString());
        }
        // Gionee: mengdw <2017-03-16> add for 69788 end
    }

    public void updateDetailData() {

        initNetworkParam(mContext, mSimIndex);

        getApplicationsTraffic(mContext);
        getInternetAppList(mContext);
        getThreeMaxApp();
    }

    public void closeSession() {
        TrafficStats.closeQuietly(mStatsSession);
    }

    public String getTrafficConsumptionApp() {
        return mThreeMaxApp;
    }

    private void getThreeMaxApp() {
        long max = 0;
        int index = 0;
        mThreeMaxApp = "";
        if (mTrafficArray.length == 0) {
            return;
        }

        if (mTrafficArray.length <= 3) {
            for (int i = 0; i < mTrafficArray.length; i++) {
                mThreeMaxApp += mMapAppList.get(String.valueOf(mUidArray[i]));
                if (i < mTrafficArray.length - 1) {
                    mThreeMaxApp += ",";
                }
            }
            return;
        }

        for (int i = 0; i < 3; i++) {
            max = mTrafficArray[0];
            index = 0;
            for (int j = 1; j < mTrafficArray.length; j++) {
                if (max < mTrafficArray[j]) {
                    max = mTrafficArray[j];
                    index = j;
                }
            }
            mTrafficArray[index] = 0;
            mThreeMaxApp += mMapAppList.get(String.valueOf(mUidArray[index]));
            if (i < 2) {
                mThreeMaxApp += ",";
            }
        }
    }

    private void getInternetAppList(Context context) {
        PackageManager pkgManager = context.getPackageManager();
        List<PackageInfo> packages = pkgManager.getInstalledPackages(0);
        mMapAppList.clear();
        for (PackageInfo packageInfo : packages) {
            mMapAppList.put(String.valueOf(packageInfo.applicationInfo.uid), packageInfo.applicationInfo
                    .loadLabel(pkgManager).toString());
        }
    }

    private void getInternetAppLists(Context context) {
        PackageManager pkgManager = context.getPackageManager();
        List<ApplicationInfo> packages = getApplicationInfo(context);
        mMapAppList.clear();
        for (ApplicationInfo packageInfo : packages) {
            mMapAppList.put(String.valueOf(packageInfo.uid), packageInfo.loadLabel(pkgManager).toString());
        }
    }

    public static List<ApplicationInfo> getApplicationInfo(Context context) {
        List<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
        List<ResolveInfo> resolves = getLauncherShowActivity(context);
        for (int i = 0; i < resolves.size(); i++) {
            ResolveInfo info = resolves.get(i);
            ApplicationInfo ai = getApplicationInfo(context, info.activityInfo.packageName);
            if (containApplications(applications, ai)) {
                continue;
            }
            applications.add(ai);
        }
        return applications;
    }

    public static List<ResolveInfo> getLauncherShowActivity(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return context.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    public static ApplicationInfo getApplicationInfo(Context context, String pkgName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    private static boolean containApplications(List<ApplicationInfo> applications, ApplicationInfo ai) {
        for (ApplicationInfo appInfo : applications) {
            if (ai.packageName.equals(appInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
    
    // Gionee: mengdw <2016-12-06> add for CR01657626 begin
    public void initNetParam() {
        initNetworkParam(mContext, mSimIndex);
        try {
            mStatsService.forceUpdate();
        } catch (RemoteException e) {
            Log.d("TrafficMonitorUtil", "initNetParam RemoteException e=" + e.toString());
        }
    }
    // Gionee: mengdw <2016-12-06> add for CR01657626 end
    
    // Gionee: mengdw <2016-12-06> modify for CR01657626 begin
    public ArrayList<AppItem> getApplicationsTraffic(Context context) {
        // Gionee: mengdw <2015-10-20> add for CR01571760 begin
        Log.d("TrafficMonitorUtil", "getApplicationsTraffic start=" + start + " end=" + end);
        // Gionee: mengdw <2015-10-20> add for CR01571760 end
        SummaryForAllUidLoader loader = new SummaryForAllUidLoader(context, mStatsSession,
                SummaryForAllUidLoader.buildArgs(mTemplate, start, end));
        NetworkStats stats = loader.loadInBackground();

        int unit;
        double total;
        ArrayList<AppItem> mItems = Lists.newArrayList();
        final int currentUserId = ActivityManager.getCurrentUser();
        final SparseArray<AppItem> knownItems = new SparseArray<AppItem>();
        NetworkStats.Entry entry = null;
        final int size = (stats != null) ? stats.size() : 0;
        Log.d(TAG, "getApplicationsTraffic size=" + size + " mTemplate=" + mTemplate);
        for (int i = 0; i < size; i++) {

            entry = stats.getValues(i, entry);

            final int uid = entry.uid;
            // Gionee: mengdw <2015-10-20> add for CR01571760 begin
            if (uid == UID_TETHERING) {
                mHotSportTraffic = entry.rxBytes + entry.txBytes;
            }
            long trafficData = entry.rxBytes + entry.txBytes;
            Log.d("TrafficMonitorUtil", "getApplicationsTraffic uid=" + uid + " trafficData=" +
                    trafficData + " UID_REMOVED=" + UID_REMOVED);
            // Gionee: mengdw <2015-10-20> add for CR01571760 end
            final int collapseKey;
            // Gionee: mengdw <2016-12-06> modify for CR01657626 begin
            if (uid < 10000 && uid != UID_REMOVED && uid != UID_TETHERING) {
                continue;
            }
            // Gionee: mengdw <2016-12-06> modify for CR01657626 end
            if (UserHandle.isApp(uid)) {
                if (UserHandle.getUserId(uid) == currentUserId) {
                    collapseKey = uid;
                } else {
                    collapseKey = UidDetailProvider.buildKeyForUser(UserHandle.getUserId(uid));
                }
            } else if (uid == UID_REMOVED || uid == UID_TETHERING) {
                Log.d("TrafficMonitorUtil", "getApplicationsTraffic tether or remoe set uid=" + uid);
                collapseKey = uid;
            } else {
                collapseKey = android.os.Process.SYSTEM_UID;
            }

            AppItem item = knownItems.get(collapseKey);
            if (item == null) {
                item = new AppItem(collapseKey);
                mItems.add(item);
                knownItems.put(item.key, item);
            } 

            item.addUid(uid);
            item.total += entry.rxBytes + entry.txBytes;
        }

        mUidArray = new int[mItems.size()];
        mTrafficArray = new long[mItems.size()];
        for (int i = 0; i < mItems.size(); i++) {
            mUidArray[i] = mItems.get(i).key;
            mTrafficArray[i] = mItems.get(i).total;
        }
        // Gionee: mengdw <2016-12-06> add for CR01657626 begin
        Collections.sort(mItems, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem lhs, AppItem rhs) {
                return Long.compare(rhs.total, lhs.total);
            }
        });
        return mItems;
        // Gionee: mengdw <2016-12-06> add for CR01657626 end
    }
    // Gionee: mengdw <2016-12-06> modify for CR01657626 end
}
