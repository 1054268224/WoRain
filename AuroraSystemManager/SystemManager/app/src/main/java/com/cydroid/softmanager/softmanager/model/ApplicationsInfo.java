package com.cydroid.softmanager.softmanager.model;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.PrivateCredentialPermission;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.NameSorting;

import android.text.TextUtils;
// Gionee bianrong 2016-7-29 add for CR01739606 begin
import com.cydroid.softmanager.common.Consts;
// Gionee bianrong 2016-7-29 add for CR01739606 end
/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-3-8 Change List:
 */
public class ApplicationsInfo {
    public final List<AppInfo> mAppEntries = new ArrayList<AppInfo>();
    public final Map<String, AppInfo> mMapEntries = new HashMap<String, AppInfo>();

    public final List<AppInfo> mPermissionsAppEntries = new ArrayList<AppInfo>();
    public final Map<String, AppInfo> mPermissionsMapEntries = new HashMap<String, AppInfo>();

    private static final Object LOCK = new Object();
    private static ApplicationsInfo sInstance;
    private static final String TAG = "ApplicationsInfo-->";
    // Gionee <xuhz> <2014-04-04> delete for CR01129543 begin
    // private PackageSizeObserver mPackageSizeObserver;
    // Gionee <xuhz> <2014-04-04> delete for CR01129543 end
    private Context mContext;
    // Gionee <lihq> <2013-6-19> modify for CR00826735 begin
    private final boolean mReady = false;
    // Gionee <lihq> <2013-6-19> modify for CR00826735 end
    
    public final List<AppInfo> mNameSortedList = new ArrayList<AppInfo>();
    
    private final HashMap<String, Long> mUninstallSortSize = new HashMap<String, Long>();
    private List<Map.Entry<String, Long>> mUninstallSortSizeList;
    public final List<AppInfo> mSortSizeAppEntries = new ArrayList<AppInfo>();
    public final Map<String, AppInfo> mSortSizeMapEntries = new HashMap<String, AppInfo>();

    private final HashMap<String, Long> mUninstallSortTime = new HashMap<String, Long>();
    private List<Map.Entry<String, Long>> mUninstallSortTimeList;
    public final List<AppInfo> mSortTimeAppEntries = new ArrayList<AppInfo>();
    public final Map<String, AppInfo> mSortTimeMapEntries = new HashMap<String, AppInfo>();

    private final HashMap<String, Long> mUninstallSortFrequency = new HashMap<String, Long>();
    private List<Map.Entry<String, Long>> mUninstallSortFrequencyList;
    public final List<AppInfo> mSortFrequencyAppEntries = new ArrayList<AppInfo>();
    public final Map<String, AppInfo> mSortFrequencyMapEntries = new HashMap<String, AppInfo>();
    // Gionee <liuyb> <2014-07-23> add for CR01316210 begin
    public static final Map<String, String> mPackageSizeInfoMap = new HashMap<String, String>();
    public static final Map<String, Long> mPackageSizeMap = new HashMap<String, Long>();
    // Gionee <liuyb> <2014-07-23> add for CR01316210 end
    private long mAppSize;
    private static final long DAY_TIME = 24 * 60 * 60 * 1000;
    private static final long MONTH_TIME = DAY_TIME * 30;

    public static ApplicationsInfo getInstance() {
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new ApplicationsInfo();
            }
            return sInstance;
        }
    }

    // Gionee <lihq> <2013-6-19> modify for CR00826735 begin
    public boolean isReady() {
        return mReady;
    }

    // Gionee <yangxinruo> <2016-4-8> add for CR01670643 begin
    public synchronized void loadAppEntries(Context context) {
        loadAppEntries(context, true);
    }
    // Gionee <yangxinruo> <2016-4-8> add for CR01670643 end

    // Gionee <lihq> <2013-6-19> modify for CR00826735 end
    public synchronized void loadAppEntries(Context context, boolean includeUninstalled) {
        // synchronized (mMapEntries) {
        Log.d(TAG, "loadAppEntries includeUninstalled=" + includeUninstalled);
        hashMapClear();
        mContext = context.getApplicationContext();
        // Gionee <yangxinruo> <2016-4-8> modify for CR01670643 begin
        List<ApplicationInfo> applications = SoftHelperUtils.getThirdApplicationInfo(context,
                includeUninstalled);
        // Gionee <yangxinruo> <2016-4-8> modify for CR01670643 end
        // Gionee <xuhz> <2014-04-04> delete for CR01129543 begin
        // PackageManager pm = context.getPackageManager();
        // Gionee <xuhz> <2014-04-04> delete for CR01129543 end
        for (int i = 0; i < applications.size(); i++) {
            ApplicationInfo info = applications.get(i);
            AppInfo appInfo = mMapEntries.get(info.packageName);
            if (appInfo != null) {
                appInfo.mApplicationInfo = info;
            } else {
                appInfo = new AppInfo(info);
                // Gionee <jianghuan> <2014-04-17> modify for CR01200613 begin
                appInfo.setPackageName(info.packageName);
                // appInfo.setIcon(HelperUtils.loadIcon(context, info));
                // Gionee <jianghuan> <2014-04-17> modify for CR01200613 end
                appInfo.setTitle(SoftHelperUtils.loadLabel(context, info));
                appInfo.setCheckStatus(false);

			// Gionee bianrong 2016-7-29 add for CR01739606 begin
				if (Consts.gnKRFlag) {
					Log.d("kptc", "com.cydroid.softmanager.softmanager.model.ApplicationsInfo->loadAppEntries(): info.packageName" + info.packageName);
                	String str = "kuts.ktr.RFService";
					if (!info.packageName.equals(str)) {
		                mUninstallSortSize.put(info.packageName, mAppSize);
		                 mUninstallSortTime.put(info.packageName, getLastUpdateTime(context, info.packageName));
		                mAppEntries.add(appInfo);
		                mMapEntries.put(info.packageName, appInfo);              
		                mNameSortedList.add(appInfo);
					}
				} else {
					mUninstallSortSize.put(info.packageName, mAppSize);
	                mUninstallSortTime.put(info.packageName, getLastUpdateTime(context, info.packageName));

                mAppEntries.add(appInfo);
                mMapEntries.put(info.packageName, appInfo);
                
	                mNameSortedList.add(appInfo);
				}
			// Gionee bianrong 2016-7-29 add for CR01739606 end
            }
        }
        sortBySize();
        loadSortSizeAppEntries(mContext, mUninstallSortSizeList);

        sortBytime();
        loadSortTimeAppEntries(mContext, mUninstallSortTimeList);
        // Gionee <yangxinruo> <2016-6-14> modify for CR01717874 begin
        getFrequentAppList(mContext, includeUninstalled);
        // Gionee <yangxinruo> <2016-6-14> modify for CR01717874 end

        sortByName();

        // Gionee <lihq> <2013-6-19> modify for CR00826735 begin
        // mReady = true;
        // Gionee <lihq> <2013-6-19> modify for CR00826735 end
        // }
    }

    public void setAppsSummary(Context context) {
        Map<String, AppInfo> appEntries = new HashMap<String, AppInfo>();
        Context c = null;
        synchronized (this) {
            if (null == mContext) {
                mContext = context.getApplicationContext();
            }
            appEntries.putAll(mMapEntries);
            c = mContext;
        }
        for (Map.Entry<String, AppInfo> appEntry : appEntries.entrySet()) {
            String packageName = appEntry.getKey();
            AppInfo item = appEntry.getValue();

            String summary = invokePMGetPackageSizeInfo(c, packageName);
            if (!TextUtils.isEmpty(summary)) {
                item.setSummary(summary);
            }
        }
    }

    private void hashMapClear() {
        mMapEntries.clear();
        mAppEntries.clear();
        mUninstallSortSize.clear();
        mUninstallSortTime.clear();
        mUninstallSortFrequency.clear();
        mNameSortedList.clear();
    }
    
    private void sortByName() {
        // Gionee: mengdw <2015-10-12> modify for CR01566282 begin
        synchronized (mNameSortedList) {
            NameSorting.sort(mNameSortedList);
        }
        // Gionee: mengdw <2015-10-12> modify for CR01566282 end
    }

    private void sortBytime() {
        mUninstallSortTimeList = new ArrayList<Map.Entry<String, Long>>(mUninstallSortTime.entrySet());
        Collections.sort(mUninstallSortTimeList, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> firstMapEntry,
                    Map.Entry<String, Long> secondMapEntry) {
                return firstMapEntry.getValue().compareTo(secondMapEntry.getValue());
            }

        });

    }

    private long getFirstInstallTime(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            Log.v(TAG, "Package-->" + packageName + " firstInstallTime = " + info.firstInstallTime);
            return info.firstInstallTime;

        } catch (Exception ex) {
            Log.d(TAG, "isRecentInstallPackage throw exption " + ex.getMessage());
        }
        return 0;

    }

    private long getLastUpdateTime(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            Log.v(TAG, "Package-->" + packageName + " lastUpdateTime = " + info.lastUpdateTime);
            return info.lastUpdateTime;
        } catch (Exception ex) {
            Log.d(TAG, "isRecentInstallPackage throw exption " + ex.getMessage());
        }
        return 0;
    }

    private void sortBySize() {
        mUninstallSortSizeList = new ArrayList<Map.Entry<String, Long>>(mUninstallSortSize.entrySet());
        Collections.sort(mUninstallSortSizeList, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> firstMapEntry,
                    Map.Entry<String, Long> secondMapEntry) {
                return secondMapEntry.getValue().compareTo(firstMapEntry.getValue());
            }

        });

    }

    public synchronized void addPackage(Context context, String pkgName) {
        AppInfo appInfo = mMapEntries.get(pkgName);
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkgName,
                    PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);
            if (appInfo == null) {
                appInfo = new AppInfo(info);
                // Gionee <jianghuan> <2014-04-17> modify for CR01200613 begin
                // appInfo.setIcon(HelperUtils.loadIcon(context, info));
                appInfo.setPackageName(info.packageName);
                // Gionee <jianghuan> <2014-04-17> modify for CR01200613 end
                appInfo.setTitle(SoftHelperUtils.loadLabel(context, info));

                mUninstallSortSize.put(info.packageName, mAppSize);
                mUninstallSortTime.put(info.packageName, getLastUpdateTime(context, info.packageName));

                mAppEntries.add(appInfo);
                mMapEntries.put(pkgName, appInfo);
                mNameSortedList.add(appInfo);
            } else {
                appInfo.mApplicationInfo = info;
                appInfo.setMoving(false);
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sortBySize();
        loadSortSizeAppEntries(context, mUninstallSortSizeList);

        sortBytime();
        loadSortTimeAppEntries(context, mUninstallSortTimeList);

        getFrequentAppList(context);
        NameSorting.sort(mNameSortedList);
    }

    public synchronized void removePackage(Context context, String pkgName) {
        AppInfo appInfo;
        appInfo = mMapEntries.get(pkgName);
        if (appInfo != null) {
            mAppEntries.remove(appInfo);
            mMapEntries.remove(pkgName);
            mNameSortedList.remove(appInfo);
        }

        appInfo = mSortSizeMapEntries.get(pkgName);
        if (appInfo != null) {
            mSortSizeAppEntries.remove(appInfo);
            mSortSizeMapEntries.remove(pkgName);
        }

        appInfo = mSortTimeMapEntries.get(pkgName);
        if (appInfo != null) {
            mSortTimeAppEntries.remove(appInfo);
            mSortTimeMapEntries.remove(pkgName);
        }

        appInfo = mSortFrequencyMapEntries.get(pkgName);
        if (appInfo != null) {
            mSortFrequencyAppEntries.remove(appInfo);
            mSortFrequencyMapEntries.remove(pkgName);
        }

        // Gionee <houjie> <2015-10-22> modify for CR01570083 begin
        mUninstallSortSize.remove(pkgName);
        mUninstallSortTime.remove(pkgName);
        mUninstallSortFrequency.remove(pkgName);
        // Gionee <houjie> <2015-10-22> modify for CR01570083 end
            
        sortBySize();
        loadSortSizeAppEntries(context, mUninstallSortSizeList);

        sortBytime();
        loadSortTimeAppEntries(context, mUninstallSortTimeList);

        getFrequentAppList(context);
        NameSorting.sort(mNameSortedList);
    }

    // Gionee <liuyb> <2014-3-3> add for CR01078882 begin
    public synchronized void releaseRes() {
        if (mMapEntries != null) {
            mMapEntries.clear();
        }
        if (mAppEntries != null) {
            mAppEntries.clear();
        }
        mPermissionsMapEntries.clear();
        mPermissionsAppEntries.clear();
    }

    // Gionee <liuyb> <2014-3-3> add for CR01078882 end

    // Gionee <xuhz> <2014-04-04> add for CR01129543 begin
    class PackageStatsObserver extends IPackageStatsObserver.Stub {
        public boolean retValue = false;
        public PackageStats stats;
        private boolean doneFlag = false;

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            synchronized (this) {
                retValue = succeeded;
                stats = pStats;
                doneFlag = true;
                notifyAll();
            }
        }

        public boolean isDone() {
            return doneFlag;
        }
    }

    // Gionee <liuyb> <2014-7-17> modify for CR01322205 begin
    private static final long MAX_WAIT_TIME = 4 * 1000;
    // Gionee <liuyb> <2014-7-17> modify for CR01322205 end
    // Gionee <liuyb> <2014-7-9> modify for CR01317723 begin
    private static final long WAIT_TIME_INCR = 10;

    // Gionee <liuyb> <2014-7-9> modify for CR01317723 end

    public String invokePMGetPackageSizeInfo(Context context, String packageName) {
        // Gionee <liuyb> <2014-07-23> modify for CR01316210 begin
        if (mPackageSizeInfoMap.containsKey(packageName)) {
            mAppSize = mPackageSizeMap.get(packageName);
            return mPackageSizeInfoMap.get(packageName);
        }
        String result = null;
        try {
            PackageStatsObserver observer = new PackageStatsObserver();
            PackageManager pm = context.getPackageManager();
            // wait on observer
            synchronized (observer) {
                pm.getPackageSizeInfo(packageName, observer);
                long waitTime = 0;
                while ((!observer.isDone()) && (waitTime < MAX_WAIT_TIME)) {
                    observer.wait(WAIT_TIME_INCR);
                    waitTime += WAIT_TIME_INCR;
                }
                Log.i(TAG, "invokePMGetPackageSizeInfo waitTime= " + waitTime);

                if (!observer.isDone()) {
                    Log.e(TAG, "Timed out waiting for PackageStatsObserver.onGetStatsCompleted");
                    // Gionee <liuyb> <2014-7-17> modify for CR01322205 begin
                    mAppSize = 0;
                    observer.notifyAll();
                    mPackageSizeInfoMap.put(packageName, "");
                    mPackageSizeMap.put(packageName, mAppSize);
                    return null;
                    // Gionee <liuyb> <2014-7-17> modify for CR01322205 end
                }
            }
            mAppSize = observer.stats.cacheSize + observer.stats.codeSize + observer.stats.dataSize
                    + observer.stats.externalCodeSize + observer.stats.externalDataSize;

            Log.i(TAG, "invokePMGetPackageSizeInfo packageName= " + packageName + ", size = " + mAppSize);

            result = SoftHelperUtils.getSizeStr(context, mAppSize);
            mPackageSizeInfoMap.put(packageName, result);
            mPackageSizeMap.put(packageName, mAppSize);
            return result;
            // Gionee <liuyb> <2014-07-23> add for CR01316210 end
        } catch (Exception e) {
            Log.w(TAG, "Exception :" + e);
            return null;
        }
    }

    // Gionee <xuhz> <2014-04-04> add for CR01129543 end

    // Gionee <yangxinruo> <2016-6-14> add for CR01717874 begin
    public void getFrequentAppList(Context context) {
        getFrequentAppList(context, true);
    }
    // Gionee <yangxinruo> <2016-6-14> add for CR01717874 end

    public void getFrequentAppList(Context context, boolean includeUninstalled) {
        Log.d(TAG, "-->getFrequentAppList includeUninstalled= " + includeUninstalled);
        PackageManager manager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        List<ResolveInfo> thirdinfos = filterPackage(infos);
        // Gionee <yangxinruo> <2016-6-14> add for CR01717874 begin
        mSortFrequencyAppEntries.clear();
        mSortFrequencyMapEntries.clear();
        // Gionee <yangxinruo> <2016-6-14> add for CR01717874 end
        if (thirdinfos.isEmpty()) {
            /*guoxt modify for CR01640692 begin*/
            //return;
            /*guoxt modify for CR01640692 end*/
        }
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
        final List<UsageStats> statsList = mUsageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
        if (statsList == null) {
            return;
        }
        HashMap<String, UsageStats> usageMap = new HashMap<String, UsageStats>();
        final int statCount = statsList.size();
        for (int i = 0; i < statCount; i++) {
            final android.app.usage.UsageStats pkgStats = statsList.get(i);
            usageMap.put(pkgStats.getPackageName(), pkgStats);
        }

        for (ResolveInfo info : thirdinfos) {
            try {
                UsageStats aStats = usageMap.get(info.activityInfo.packageName);
                if (aStats != null) {
                    long lastUse = System.currentTimeMillis() - aStats.getLastTimeUsed();
                    mUninstallSortFrequency.put(info.activityInfo.packageName, lastUse);
                }
            } catch (Exception e) {
                Log.d(TAG, "getFrequentAppList Exception " + e.getMessage());
            }
        }
        // Gionee <yangxinruo> <2016-6-14> modify for CR01717874 begin
        List<ApplicationInfo> applications = SoftHelperUtils.getThirdApplicationInfo(context,
                includeUninstalled);
        // Gionee <yangxinruo> <2016-6-14> modify for CR01717874 end

        for (int i = 0; i < applications.size(); i++) {
            ApplicationInfo info = applications.get(i);
            if (!mUninstallSortFrequency.containsKey(info.packageName)) {
                mUninstallSortFrequency.put(info.packageName, MONTH_TIME + 1);
            }
        }

        sortByFrequency();
        loadSortFrequencyAppEntries(context, mUninstallSortFrequencyList);

    }

    private void sortByFrequency() {
        mUninstallSortFrequencyList = new ArrayList<Map.Entry<String, Long>>(
                mUninstallSortFrequency.entrySet());
        Collections.sort(mUninstallSortFrequencyList, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> firstMapEntry,
                    Map.Entry<String, Long> secondMapEntry) {
                return secondMapEntry.getValue().compareTo(firstMapEntry.getValue());
            }

        });

    }

    private void loadSortSizeAppEntries(Context context, List<Map.Entry<String, Long>> list) {
        String size;
        mSortSizeAppEntries.clear();
        mSortSizeMapEntries.clear();
        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo info;
            info = HelperUtils.getApplicationInfo(context, list.get(i).getKey());
            if (info == null || "".equalsIgnoreCase(info.packageName)
                    || !mUninstallSortSize.containsKey(info.packageName)) {
                continue;
            }
            AppInfo appInfo = new AppInfo(info);
            appInfo.setPackageName(info.packageName);
            appInfo.setTitle(SoftHelperUtils.loadLabel(context, info));

            appInfo.setSummary(SoftHelperUtils.getStringDate(getLastUpdateTime(context, info.packageName)));
            try {
                size = SoftHelperUtils.getSizeStr(context, mUninstallSortSize.get(info.packageName));
            } catch (Exception e) {
                Log.w(TAG, "Exception :" + e);
                size = " ";
            }
			// Gionee bianrong 2016-7-29 add for CR01739606 begin
			if (Consts.gnKRFlag) {
				Log.d("kptc", "com.cydroid.softmanager.softmanager.model.ApplicationsInfo->loadSortSizeAppEntries(): info.packageName" + info.packageName);
				String str = "kuts.ktr.RFService";
				if (!info.packageName.equals(str)) {
		            appInfo.setSize(size);
		            mSortSizeAppEntries.add(appInfo);
		            mSortSizeMapEntries.put(info.packageName, appInfo);
				}
			} else {
				appInfo.setSize(size);
	            mSortSizeAppEntries.add(appInfo);
	            mSortSizeMapEntries.put(info.packageName, appInfo);
			}
			// Gionee bianrong 2016-7-29 add for CR01739606 begin
        }

    }

    private void loadSortFrequencyAppEntries(Context context, List<Map.Entry<String, Long>> list) {
        String size;
        mSortFrequencyAppEntries.clear();
        mSortFrequencyMapEntries.clear();
        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo info;
            info = HelperUtils.getApplicationInfo(context, list.get(i).getKey());
            if (info == null || !mUninstallSortSize.containsKey(info.packageName)
                    || mUninstallSortSize.get(info.packageName) == null) {
                continue;
            }
            AppInfo appInfo = new AppInfo(info);
            appInfo.setPackageName(info.packageName);
            appInfo.setTitle(SoftHelperUtils.loadLabel(context, info));
            appInfo.setAppFrequency(list.get(i).getValue());
            try {
                size = SoftHelperUtils.getSizeStr(context, mUninstallSortSize.get(info.packageName));
            } catch (Exception e) {
                Log.w(TAG, "Exception :" + e);
                size = " ";
            }
				// Gionee bianrong 2016-7-29 add for CR01739606 begin
			if (Consts.gnKRFlag) {
				Log.d("kptc", "com.cydroid.softmanager.softmanager.model.ApplicationsInfo->loadSortFrequencyAppEntries(): info.packageName" + info.packageName);
				String str = "kuts.ktr.RFService";
				if (!info.packageName.equals(str)) {
		            appInfo.setSize(size);
		            mSortFrequencyAppEntries.add(appInfo);
		            mSortFrequencyMapEntries.put(info.packageName, appInfo);
				}
			} else {
				appInfo.setSize(size);
	            mSortFrequencyAppEntries.add(appInfo);
	            mSortFrequencyMapEntries.put(info.packageName, appInfo);
			}
				// Gionee bianrong 2016-7-29 add for CR01739606 end
        }

    }

    private void loadSortTimeAppEntries(Context context, List<Map.Entry<String, Long>> list) {
        String size;
        mSortTimeAppEntries.clear();
        mSortTimeMapEntries.clear();
        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo info;
            info = HelperUtils.getApplicationInfo(context, list.get(i).getKey());
            if (info == null || !mUninstallSortSize.containsKey(info.packageName)
                    || mUninstallSortSize.get(info.packageName) == null) {
                continue;
            }
            AppInfo appInfo = new AppInfo(info);
            appInfo.setPackageName(info.packageName);
            appInfo.setTitle(SoftHelperUtils.loadLabel(context, info));
            appInfo.setSummary(SoftHelperUtils.getStringDate(list.get(i).getValue()));
            try {
                size = SoftHelperUtils.getSizeStr(context, mUninstallSortSize.get(info.packageName));
            } catch (Exception e) {
                Log.w(TAG, "Exception :" + e);
                size = " ";
            }
			// Gionee bianrong 2016-7-29 add for CR01739606 begin
			if (Consts.gnKRFlag) {
				Log.d("kptc", "com.cydroid.softmanager.softmanager.model.ApplicationsInfo->loadSortTimeAppEntries(): info.packageName" + info.packageName);
				String str = "kuts.ktr.RFService";
				if (!info.packageName.equals(str)) {
		            appInfo.setSize(size);
		            mSortTimeAppEntries.add(appInfo);
		            mSortTimeMapEntries.put(info.packageName, appInfo);
				}
			} else {
				appInfo.setSize(size);
	            mSortTimeAppEntries.add(appInfo);
	            mSortTimeMapEntries.put(info.packageName, appInfo);
			}
			// Gionee bianrong 2016-7-29 add for CR01739606 end
        }

    }

    private List<ResolveInfo> filterPackage(List<ResolveInfo> allPackageList) {
        List<ResolveInfo> list = new ArrayList<ResolveInfo>();
        if (!allPackageList.isEmpty()) {
            list.clear();
            int size = allPackageList.size();
            ResolveInfo resolveInfo = null;
            for (int i = 0; i < size; i++) {
                resolveInfo = allPackageList.get(i);
                // 第三方应用
                if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                    list.add(resolveInfo);
                }
            }
        }
        return list;
    }

}
