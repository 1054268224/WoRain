//Gionee: mengdw <2015-12-28> add begin
package com.cydroid.softmanager.trafficassistant.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkInfo;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.SystemProperties;

import com.cydroid.framework.FrameworkUtility;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.SIMInfoWrapper;
import com.cydroid.softmanager.trafficassistant.SIMParame;
import com.cydroid.softmanager.trafficassistant.hotspotremind.TrafficAutoCloseHotSpotRemindDialog;
import com.cydroid.softmanager.trafficassistant.model.TrafficNetworkControlAppInfo;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;
import com.chenyee.featureoption.ServiceUtil;
public class TrafficassistantUtil {
    private static final String TAG = "TrafficassistantUtil";

    private static final int SERVICE_START_WAIT_TIME = 3000;
    private static final String SIM_SETTING = "SIM%d_SETTING";
    private static final String SIM_NOTIFICATION = "SIM%d_NOTIFICATION";
    // Gionee: mengdw <2017-01-05> add for 58932 begin
    private static final String ENCRYPTION_APPS = "encryptspace_apps";
    // Gionee: mengdw <2017-01-05> add for 58932 end

    public static void loadExcludeApps(Context context, List<String> apps) {
        if (null == apps) {
            return;
        }
        String[] excludeApps = context.getResources().getStringArray(
                R.array.network_control_exclude_apps);
        for (String pkgName : excludeApps) {
            Log.d(TAG, "loadExcludeApps pkgName=" + pkgName);
            if (!apps.contains(pkgName)) {
                apps.add(pkgName);
            }
        }
    }

    // Gionee: mengdw <2017-01-21> add for CR01776232 begin
    public static void appNameSort(List<? extends TrafficNetworkControlAppInfo> list) {
        Collections.sort(list, new Comparator<TrafficNetworkControlAppInfo>() {
            @Override
            public int compare(TrafficNetworkControlAppInfo lhs, TrafficNetworkControlAppInfo rhs) {
                return Collator.getInstance(Locale.CHINESE).compare(lhs.getAppName(), rhs.getAppName());
            }
        });
    }
    // Gionee: mengdw <2017-01-21> add for CR01776232 end

    // Gionee: mengdw <2017-01-05> add for 58932 begin
    public static boolean isEncryptionApp(Context context, String pkgName) {
        ArrayList<String> mEncryptionAppList = TrafficassistantUtil.getEncryptionsApps(context);
        for (String encryptionAppPkgName : mEncryptionAppList) {
            Log.d(TAG, "isEncryptionApp encryptionAppPkgName=" + encryptionAppPkgName);
            if (null != encryptionAppPkgName && encryptionAppPkgName.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> getEncryptionsApps(Context context) {
        ArrayList<String> encryptionAppList = new ArrayList<String>();
        if (isEncryptionSupport()) {
            Class<?> encryptionsClass = FrameworkUtility.createClass("android.provider.Encryptions$Secure");
            if (encryptionsClass == null) {
                return encryptionAppList;
            }
            String appsStr = (String) FrameworkUtility.invokeStaticMethod(encryptionsClass, "getString",
                    new Class[]{ContentResolver.class, String.class},
                    new Object[]{context.getContentResolver(), "encryptspace_apps"});
            if (appsStr == null || appsStr.isEmpty()) {
                return encryptionAppList;
            }
            String[] appArray = appsStr.split(";");
            encryptionAppList.addAll((List<String>) Arrays.asList(appArray));
            Log.d(TAG, "show EncryptionsApp list: " + encryptionAppList.toString());
            return encryptionAppList;
        } else {
            return encryptionAppList;
        }
    }

    public static boolean isEncryptionSupport() {
        return SystemProperties.get("ro.encryptionspace.enabled", "false").equals("true");
    }

    public static Uri getEncryptUri() {
        Uri uri = null;
        Class<?> encryptionsClass = FrameworkUtility.createClass("android.provider.Encryptions$Secure");
        if (encryptionsClass != null) {
            uri = (Uri) FrameworkUtility.invokeStaticMethod(encryptionsClass, "getUriFor",
                    new Class[]{String.class}, new Object[]{ENCRYPTION_APPS});
        }
        return uri;
    }
    // Gionee: mengdw <2017-01-05> add for 58932 end

    // Gionee: mengdw <2016-12-13> add for CR01776232 begin
    public static String getAppLabel(Context context, String packageName) {
        String appLable = "";
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            appLable = pm.getApplicationLabel(appInfo).toString();
        } catch (Exception e) {
            Log.e(TAG, "getAppLabel exception e=" + e.toString());
        }
        return appLable;
    }

    public static int getUidByPackageName(Context context, String pkgName) {
        int uid = Constant.INVALID_UID;
        ApplicationInfo appInfo = HelperUtils.getApplicationInfo(context, pkgName);
        if (appInfo != null) {
            uid = appInfo.uid;
        }
        Log.d(TAG, "getUidByPackageName pkgName=" + pkgName + " uid=" + uid);
        return uid;
    }

    public static List<ApplicationInfo> getLauncherActivityApps(Context context) {
        List<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolves = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < resolves.size(); i++) {
            ResolveInfo info = resolves.get(i);
            ApplicationInfo ai = getApplicationInfoByPkgName(context, info.activityInfo.packageName);
            if (isInApplications(applications, ai)) {
                continue;
            }
            applications.add(ai);
        }
        return applications;
    }

    public static boolean isUseInternetPermissionApp(Context context, String pkgName) {
        PackageManager pkgManager = context.getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pkgManager.checkPermission(Manifest.permission.INTERNET,
                pkgName);
    }

    public static boolean isInApplications(List<ApplicationInfo> applications, ApplicationInfo ai) {
        if (null == ai) {
            return false;
        }
        for (ApplicationInfo appInfo : applications) {
            if (null != appInfo && ai.packageName.equals(appInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    public static ApplicationInfo getApplicationInfoByPkgName(Context context, String pkgName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (Exception e) {
            Log.d(TAG, "getApplicationInfoByPkgName e=" + e.toString());
        }
        return result;
    }
    // Gionee: mengdw <2016-12-13> add for CR01776232 end

    //Gionee: mengdw <2016-11-22> add for CR01772354 begin
    public static String getNowDate() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
    // Gionee: mengdw <2016-11-22> add for CR01772354 end

    // Gionee: mengdw <2016-11-21> add for CR01772354 begin
    public static String getActiveNumber(Context context) {
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        String buyNumber = simInfo.getSimNumberCurrentNetworkActivated();
        SIMInfoWrapper.setEmptyObject(context);
        return phoneNumberProcess(buyNumber);
    }

    public static boolean isHasChineseType(String input) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(input);
        Log.d(TAG, "isHasChineseType result=" + m.find());
        return m.find();
    }

    // Gionee: mengdw <2016-06-28> add for CR01723956 begin
    public static String phoneNumberProcess(String buyNumber) {
        if (buyNumber != null) {
            // space process
            buyNumber = buyNumber.replace(" ", "");
            // +86 process
            if (buyNumber.startsWith("+86")) {
                buyNumber = buyNumber.substring(3);
            }
            // 0086 process
            if (buyNumber.startsWith("0086")) {
                buyNumber = buyNumber.substring(4);
            }
        } else {
            buyNumber = "";
        }
        return buyNumber;
    }
    // Gionee: mengdw <2016-06-28> add for CR01723956 end
    // Gionee: mengdw <2016-11-22> add for CR01772354 end

    // Gionee: mengdw <2016-11-02> add for CR01639347 begin
    public static String[] getPkgNameByUid(Context context, int uid) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackagesForUid(uid);
        } catch (Exception e) {
            Log.d(TAG, " getPkgNameByUid uid= " + uid + " Exception e=" + e.toString());
        }
        return null;
    }

    public static int getCurrentNetworkType(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info != null) {
            if (ConnectivityManager.TYPE_WIFI == info.getType()) {
                return Constant.WIFI;
            } else if (ConnectivityManager.TYPE_MOBILE == info.getType()) {
                return Constant.MOBILE;
            }
        }
        return Constant.NO_NET_CONNECTED;
    }
    // Gionee: mengdw <2016-11-02> add for CR01639347 end

    // Gionee: mengdw <2016-10-22> add for CR01750259 begin
    public static void loadRelateControlApps(Context context, Map<String, List<String>> relatedLockApps) {
        String[] relateLockAppPairs = context.getResources().getStringArray(
                R.array.romapp_whiteboxapp_related_items);
        for (String pair : relateLockAppPairs) {
            String[] pairInfo = pair.split("/");
            String[] relatedApps = pairInfo[1].split(",");
            HelperUtils.dumpList(TAG, "loadRelateLockApps " + pairInfo[0], Arrays.asList(relatedApps));
            relatedLockApps.put(pairInfo[0], Arrays.asList(relatedApps));
        }
    }
    // Gionee: mengdw <2016-10-22> add for CR01750259 end

    // mengdw <2016-10-09> add for CR01766193 begin
    public static boolean isSoftApOpen(Context context) {
        boolean isOpen = false;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            isOpen = wifiManager.isWifiApEnabled();
        }
        return isOpen;
    }

    public static void setWifiApEnabled(Context context, boolean enabled) {
        Log.d(TAG, "setWifiApEnabled enabled=" + enabled);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // guoxt modify  for <2018-3-21> add for CSW1705A-2349  begin
        if(wifiManager != null && !enabled){
            ConnectivityManager manger = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            manger.stopTethering(ConnectivityManager.TETHERING_WIFI);
            return;
        }
        // guoxt modify  for <2018-3-21> add for CSW1705A-2349  end

        if (wifiManager != null) {
            //guoxt  20180717 add for Ptest begin
           // wifiManager.setWifiApEnabled(null, enabled);
            ConnectivityManager manger = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            manger.startTethering(ConnectivityManager.TETHERING_WIFI, true,
                    new TrafficAutoCloseHotSpotRemindDialog.OnStartTetheringCallback(), new Handler());
          //guoxt  20180717 add for Ptest end
        } else {
            Log.d(TAG, "setWifiApEnabled context=" + context + " wifiManager is null");
        }
    }
    // mengdw <2016-10-09> add for CR01766193 end

    public static int getSingleCardSlotID(Context context) {
        int slotID = 0;
        SIMInfoWrapper wrapper = SIMInfoWrapper.getDefault(context);
        // Gionee: mengdw <2016-05-11> modify for CR01695224 begin
        int count = wrapper != null ? wrapper.getInsertedSimCount() : -1;
        // Gionee: mengdw <2016-05-11> modify for CR01695224 end
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                SIMParame simInfo = wrapper.getInsertedSimInfo().get(i);
                if (count == 1) {
                    slotID = simInfo.mSlot;
                }
            }
        }
        SIMInfoWrapper.setEmptyObject(context);
        return slotID;
    }

    public static boolean isActivated(Context context, int simIndex) {
        boolean result = false;
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        // Gionee: mengdw <2016-05-11> modify for CR01695224 begin
        if (simInfo != null) {
            int activatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
            result = simInfo.gprsIsOpenMethod("getMobileDataEnabled") && (activatedSimIndex == simIndex);
        }
        // Gionee: mengdw <2016-05-11> modify for CR01695224 end
        SIMInfoWrapper.setEmptyObject(context);
        return result;
    }

    public static String getSimSetting(int simIndex) {
        return String.format(SIM_SETTING, simIndex);
    }

    public static String getSimNotification(int simIndex) {
        return String.format(SIM_NOTIFICATION, simIndex);
    }

    public static int getIndexByName(String name, String[] nameList) {
        int index = -1;
        for (int i = 0; i < nameList.length; i++) {
            if (name.equals(nameList[i])) {
                index = i;
            }
        }
        return index;
    }

    public static int getActivatedSimIndex(Context context) {
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int activatedSimIndex = Constant.HAS_NO_SIMCARD;
        // Gionee: mengdw <2016-05-11> modify for CR01695224 begin
        if (simInfo != null && simInfo.gprsIsOpenMethod("getMobileDataEnabled") &&
                !simInfo.isWiFiActived()) {
            activatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
        }
        // Gionee: mengdw <2016-05-11> modify for CR01695224 end
        SIMInfoWrapper.setEmptyObject(context);
        return activatedSimIndex;
    }

    public static int getSimCount(Context context) {
        int count = 0;
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        // Gionee: mengdw <2016-05-11> modify for CR01695224 begin
        if (simInfo != null) {
            count = simInfo.getInsertedSimCount();
        }
        // Gionee: mengdw <2016-05-11> modify for CR01695224 end
        SIMInfoWrapper.setEmptyObject(context);
        return count;
    }

    public static String getActivatedSimImsi(Context context) {
        int activatedIndex = getActivatedSimCardNo(context);
        return MobileTemplate.getSubscriberId(context, activatedIndex);
    }

    public static int getSimCardNo(Context context) {
        int simIndex;
        simIndex = getActivatedSimCardNo(context);
        Log.d(TAG, "getSimCardNo simIndex=" + simIndex);
        if (simIndex < 0) {
            simIndex = getDefaultNoneActivatedSimCardNo(context);
            Log.d(TAG, "getDefaultNoneActivatedSimCardNo simIndex=" + simIndex);
        }
        return simIndex;
    }

    public static int getActivatedSimCardNo(Context context) {
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int activatedSimIndex = Constant.HAS_NO_SIMCARD;
        // Gionee: mengdw <2016-05-10> modify for CR01694142 begin
        if (simInfo != null && simInfo.gprsIsOpenMethod("getMobileDataEnabled")) {
            activatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
            Log.d(TAG, "getActivatedSimCardNo simInfo !=null activatedSimIndex=" + activatedSimIndex);
        }
        // Gionee: mengdw <2016-05-10> modify for CR01694142 end
        SIMInfoWrapper.setEmptyObject(context);
        return activatedSimIndex;
    }

    public static int getDefaultNoneActivatedSimCardNo(Context context) {
        int noneActivatedSimIndex = -1;
        try {
            SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
            // Gionee: mengdw <2015-09-21> modify for CR01557552 begin
            if (null != simInfo && simInfo.getInsertedSimCount() > 1) {
                // Gionee: mengdw <2015-09-21> modify for CR01557552 end
                noneActivatedSimIndex = 0;
                Log.d(TAG, "getDefaultNoneActivatedSimCardNo getInsertedSimCount>1 ");
            } else {
                Log.d(TAG, "getDefaultNoneActivatedSimCardNo getInsertedSimCount<=1 ");
                // Gionee: mengdw <2015-08-25> modify for CR01543192 begin
                if (null != simInfo && simInfo.getInsertedSimInfo().size() > 0) {
                    noneActivatedSimIndex = simInfo.getInsertedSimInfo().get(0).mSlot;
                    Log.d(TAG, "getDefaultNoneActivatedSimCardNo noneActivatedSimIndex=" + noneActivatedSimIndex);
                }
                // Gionee: mengdw <2015-08-25> modify for CR01543192 end
            }
        } catch (Exception e) {
            Log.d(TAG, "getDefaultNoneActivatedSimCardNo Exception e =" + e.toString());
        } finally {
            SIMInfoWrapper.setEmptyObject(context);
        }
        return noneActivatedSimIndex;
    }

    // Gionee: mengdw <2016-07-25> modify for CR01737617 begin
    public static String replaceUnit(String value) {
        String replace = "0.00K";
        try {
            if (value.contains(Constant.STRING_UNIT_KB) || value.contains(Constant.STRING_UNIT_MB)
                    || value.contains(Constant.STRING_UNIT_GB) || value.contains(Constant.STRING_UNIT_TB)) {
                replace = value.substring(0, value.length() - 1);
            } else if (value.contains(Constant.STRING_UNIT_B)) {
                int index = value.indexOf(Constant.STRING_UNIT_B);
                String r = StringFormat.getStringFormat(Double.parseDouble(value.substring(0,
                        index)) / Constant.KB);
                replace = (r == "0.00" ? "0" : r) + Constant.NOTI_STRING_UNIT_KB;
            }
        } catch (Exception e) {
            Log.d(TAG, "replaceUnit value =" + value + " e=" + e.toString());
        }
        return replace;
    }
    // Gionee: mengdw <2016-07-25> modify for CR01737617 end

    public static float getActualFlow(Context context, int simIndex, int day) {
        int[] dateInterval = initDateInterval(day);
        return trafficStatistic(context, dateInterval, simIndex);
    }

    public static int[] initDateInterval(int cycleDay) {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH);
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int[] dayOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((currentYear % 4 == 0 && currentYear % 100 != 0) || currentYear % 400 == 0) {
            dayOfMonth[1] = 29;
        }
        int minMonth = 1, maxMonth = 12;
        int[] date = new int[6];
        if (currentDay >= cycleDay) {
            int day = (cycleDay < dayOfMonth[currentMonth]) ? cycleDay : dayOfMonth[currentMonth];
            int month = currentMonth;
            int year = currentYear;
            date[0] = year;
            date[1] = month + 1;
            date[2] = day;
            day = (cycleDay - 1 > 0) ? (cycleDay - 1) : (dayOfMonth[currentMonth]);
            if (day == cycleDay - 1) {
                month = (currentMonth + 1) % maxMonth;
                if (month == 0) {
                    year++;
                }
            }
            date[3] = year;
            date[4] = month + 1;
            date[5] = day;

        } else {
            int day = (cycleDay - 1 < dayOfMonth[currentMonth]) ? (cycleDay - 1) : (dayOfMonth[currentMonth]);
            int month = currentMonth;
            int year = currentYear;
            date[3] = year;
            date[4] = month + 1;
            date[5] = day;
            month = (currentMonth - 1) < (minMonth - 1) ? (maxMonth - 1) : (currentMonth - 1);
            if (currentMonth - 1 < minMonth - 1) {
                year--;
            }
            day = cycleDay < dayOfMonth[month] ? cycleDay : dayOfMonth[month];
            date[0] = year;
            date[1] = month + 1;
            date[2] = day;
        }
        return date;
    }

    public static float trafficStatistic(Context context, int[] date, int simIndex) {
        long startTime = TimeFormat.getStartTime(date[0], date[1], date[2], 0, 0, 0);
        long endTime = System.currentTimeMillis();
        String flowValue = getTrafficString(context, simIndex, startTime, endTime, System.currentTimeMillis());
        return getMBTrafficFlow(flowValue);
    }

    // Gionee: mengdw <2016-07-07> add for CR01728603 begin
    public static float getMBTrafficFlow(String flowValue) {
        float trafficFlow = 0;
        try {
            if (flowValue.contains(Constant.STRING_UNIT_GB)) {
                int endIndex = flowValue.indexOf(Constant.STRING_UNIT_GB);
                String value = flowValue.substring(0, endIndex);
                trafficFlow = Float.valueOf(value) * Constant.UNIT;
            } else if (flowValue.contains(Constant.STRING_UNIT_MB)) {
                int endIndex = flowValue.indexOf(Constant.STRING_UNIT_MB);
                String value = flowValue.substring(0, endIndex);
                trafficFlow = Float.valueOf(value);
            } else if (flowValue.contains(Constant.STRING_UNIT_KB)) {
                int endIndex = flowValue.indexOf(Constant.STRING_UNIT_KB);
                String value = flowValue.substring(0, endIndex);
                trafficFlow = Float.valueOf(value) / Constant.KB;
            } else if (flowValue.contains(Constant.STRING_UNIT_B)) {
                int endIndex = flowValue.indexOf(Constant.STRING_UNIT_B);
                String value = flowValue.substring(0, endIndex);
                trafficFlow = Float.valueOf(value) / Constant.MB;
            }
            trafficFlow = Float.valueOf(StringFormat.getStringFormatA(trafficFlow).replace(",", "."));
        } catch (Exception e) {
            Log.d(TAG, "trafficStatistic flowValue=" + flowValue);
            Log.d(TAG, "trafficStatistic Exception e=" + e.toString());
        }
        return trafficFlow;
    }
    // Gionee: mengdw <2016-07-07> add for CR01728603 end

    public static String getTrafficString(Context context, int simIndex, long startTime, long endTime,
                                          long nowTime) {
        long data = getTrafficData(context, simIndex, startTime, endTime, System.currentTimeMillis());
        //String str = StringFormat.format(context, data);
        //Gionee guoxt 2017-01-05 modify for  begin
        String totalPhrase = "";
        if (data > 1024 * 1024 * 1024) {
            totalPhrase = String.valueOf(data / 1024.0 / 1024.0 / 1024.0);
            totalPhrase = totalPhrase.substring(0, totalPhrase.indexOf(".") + 2);
            totalPhrase += Constant.STRING_UNIT_GB;
        } else if (data > 1024 * 1024) {
            totalPhrase = String.valueOf(data / 1024.0 / 1024.0);
            totalPhrase = totalPhrase.substring(0, totalPhrase.indexOf(".") + 2);
            totalPhrase += Constant.STRING_UNIT_MB;
        } else if (data > 1024) {
            totalPhrase = String.valueOf(data / 1024.0);
            totalPhrase = totalPhrase.substring(0, totalPhrase.indexOf(".") + 2);
            totalPhrase += Constant.STRING_UNIT_KB;
        } else if (data >= 0) {
            totalPhrase = String.valueOf(data * 1.0);
            totalPhrase = totalPhrase.substring(0, totalPhrase.indexOf(".") + 2);
            totalPhrase += Constant.STRING_UNIT_B;
        } else {
            totalPhrase = "0.00B";
        }
        totalPhrase = totalPhrase.replace(",", ".");
        //Gionee guoxt 2014-07-15 modify for CR01321885 end
        return totalPhrase;
    }

    public static void startServiceIntentProcess(Context context, String serviceName, Class<?> serviceClass) {
        if (!isServiceRunning(context, serviceName)) {
            Intent i = new Intent(context, serviceClass);
            ServiceUtil.startForegroundService(context,i);
        }
    }

    public static void stopServiceIntentProcess(Context context, String serviceName, Class<?> serviceClass) {
        if (isServiceRunning(context, serviceName)) {
            waitTime(SERVICE_START_WAIT_TIME, serviceName);
            Intent i = new Intent(context, serviceClass);
            context.stopService(i);
        }
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        boolean serviceRunning = false;
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(serviceName)) {
                // Log.d(TAG, serviceList.get(i).service.getClassName());
                serviceRunning = true;
                break;
            }
        }
        Log.d(TAG, "isServiceRunning: " + serviceName + " -- " + serviceRunning);
        return serviceRunning;
    }

    private static void waitTime(long ms, String serviceName) {
        Object obj = new Object();
        synchronized (obj) {
            try {
                obj.wait(ms);
            } catch (Exception e) {
                Log.i(serviceName, " obj.wait ", e);
            }
        }
    }

    public static long getTrafficData(Context context, int simIndex, long startTime, long endTime,
                                       long nowTime) {
        if (0 == endTime) {
            endTime = System.currentTimeMillis();
        }
        if (0 == nowTime) {
            nowTime = System.currentTimeMillis();
        }
        NetworkTemplate template = MobileTemplate.getTemplate(context, simIndex);
        INetworkStatsService statsService = INetworkStatsService.Stub.asInterface(ServiceManager
                .getService(Context.NETWORK_STATS_SERVICE));
        NetworkStatsHistory netWorkStatsHistory;
        INetworkStatsSession statsSession;
        NetworkStatsHistory.Entry entry;
        long totalBytes = 0;
        try {
            statsSession = statsService.openSession();
            statsService.forceUpdate();
            if (statsSession != null && template != null) {
                netWorkStatsHistory = statsSession.getHistoryForNetwork(template, FIELD_RX_BYTES
                        | FIELD_TX_BYTES);
                entry = netWorkStatsHistory.getValues(startTime, endTime, nowTime, null);
                totalBytes = (entry != null ? entry.rxBytes + entry.txBytes : 0);
                TrafficStats.closeQuietly(statsSession);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return totalBytes;
    }
}
//Gionee: mengdw <2015-12-28> add end
