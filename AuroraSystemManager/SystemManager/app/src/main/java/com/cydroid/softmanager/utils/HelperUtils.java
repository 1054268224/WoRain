package com.cydroid.softmanager.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.SparseArray;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import cyee.widget.CyeeButton;

import android.widget.TextView;

import com.cydroid.framework.FrameworkUtility;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
// import com.cydroid.softmanager.strategyupdate.strategy.BlackKillUpdateStrategy;
import com.cydroid.softmanager.utils.Log;

import android.appwidget.AppWidgetManager;
//import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

/**
 * File Description:
 *
 * @author: Gionee-lihq
 * @see: 2013-1-17 Change List:
 */
public class HelperUtils {

    public static final String TAG = "HelperUtils";

    public static final String BLACK_KILL_TYPE = "blackkill";

    /**
     * To get default input method information.
     *
     * @return InputMethodInfo
     */
    public static InputMethodInfo getDefInputMethod(Context context) {
        String defInput = android.provider.Settings.Secure.getString(context.getContentResolver(),
                "default_input_method");
        InputMethodManager mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> mImis = mImm.getEnabledInputMethodList();
        for (int i = 0; i < mImis.size(); i++) {
            InputMethodInfo info = mImis.get(i);
            if (info.getId().equals(defInput)) {
                return info;
            }
        }
        return null;
    }

    // Gionee <yangxinruo> <2016-1-19> add for CR01625558 begin

    /**
     * To get current live wallpaper package name,return null if using static wallpaper
     *
     * @return String
     */
    public static String getLivePaperPkgName(Context context) {
        WallpaperManager wpm = WallpaperManager.getInstance(context);
        WallpaperInfo info = wpm.getWallpaperInfo();
        if (info != null) {
            String pkgName = info.getPackageName();
            return pkgName;
        } else {
            return null;
        }
    }
    // Gionee <yangxinruo> <2016-1-19> add for CR01625558 end

    /**
     * To get numbers of current system installed third part applications.
     *
     * @param context
     * @return the number of third app
     */
    public static int getSumThirdApp(Context context) {
        int result = 0;
        List<ApplicationInfo> appInfos = context.getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < appInfos.size(); i++) {
            ApplicationInfo info = appInfos.get(i);
            if (AppFilterUtil.THIRD_PARTY_FILTER.filterApp(info)) {
                result++;
            }
        }
        return result;
    }

    /**
     * To get application of current installed application
     *
     * @param context
     * @return
     */
    public static List<ApplicationInfo> getApplicationInfo2(Context context) {
        return getApplicationInfo2(context, 0);
    }

    public static List<ApplicationInfo> getApplicationInfo2(Context context, int flags) {
        int queryFlags = (flags | PackageManager.GET_UNINSTALLED_PACKAGES
                | PackageManager.GET_DISABLED_COMPONENTS);
        List<ApplicationInfo> mApplications = context.getPackageManager()
                .getInstalledApplications(queryFlags);
        for (int i = 0; i < mApplications.size(); i++) {
            final ApplicationInfo info = mApplications.get(i);

            if (!info.enabled && info.enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || info.packageName.equals("android")) {
                mApplications.remove(i);
                i--;
                continue;
            }
        }
        return mApplications;
    }

    public static List<ApplicationInfo> getApplicationInfos(Context context) {
        List<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
        List<ResolveInfo> resolves = getLauncherShowActivity(context);
        for (int i = 0; i < resolves.size(); i++) {
            ResolveInfo info = resolves.get(i);
            ApplicationInfo ai = getApplicationInfo(context, info.activityInfo.packageName);
            // Gionee <xuwen> <2015-08-25> modify for CR01547240 begin
            if (null == ai || containApplications(applications, ai)) {
                continue;
            }
            // Gionee <xuwen> <2015-08-25> modify for CR01547240 end
            applications.add(ai);
        }
        return applications;
    }

    private static boolean containApplications(List<ApplicationInfo> applications, ApplicationInfo ai) {
        for (ApplicationInfo appInfo : applications) {
            // Gionee <xuwen><2015-08-05> add for CR01533145 begin
            if (null == ai || null == ai.packageName) {
                return false;
            }
            // Gionee <xuwen><2015-08-05> add for CR01533145 end

            if (ai.packageName.equals(appInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    public static List<ResolveInfo> getLauncherShowActivity(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return context.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    /**
     * To convert String {@value src} to "src(num)"
     *
     * @param src
     * @param num
     * @return
     */
    public static String joinStr(String src, int num) {
        StringBuffer strBuf = new StringBuffer();
        int index = src.indexOf("(");
        if (index > 0) {
            src = src.substring(0, index);
        }
        if (num >= 0) {
            strBuf.append(src);
            strBuf.append("(");
            strBuf.append(num);
            strBuf.append(")");
            return strBuf.toString();
        } else {
            return src;
        }
    }

    public static String getSizeStr(Context context, long size) {
        if (size >= 0) {
            return Formatter.formatFileSize(context, size);
        }
        return null;
    }

    public static String loadLabel(Context context, ApplicationInfo info) {
        CharSequence cs = info.loadLabel(context.getPackageManager());
        // String result =
        // info.loadLabel(context.getPackageManager()).toString();
        if (cs == null) {
            return info.packageName;
        }
        return cs.toString();
    }

    public static Drawable loadIcon(Context context, ApplicationInfo info) {
        Drawable result = null;
        try {
            if (info != null) {
                result = info.loadIcon(context.getPackageManager());
            }
            if (result == null) {
                result = context.getResources().getDrawable(R.drawable.sym_app_on_sd_unavailable_icon);
            }
        } catch (Exception ex) {
        }

        return result;
    }

    /**
     * 设置指定button内容显示样式为(如 "一键清理(20)")
     *
     * @param context Context
     * @param buttion 需要设置的button
     * @param text    显示内容
     * @param num     显示的数字
     */
    public static void setButtonText(Context context, CyeeButton buttion, String text, int num) {
        if (num > 0) {
            buttion.setText(HelperUtils.joinStr(text, num));
            buttion.setEnabled(true);
        } else {
            buttion.setText(text);
            buttion.setEnabled(false);
        }
    }

    public static String getHtmlString(String str, String sec) {
        StringBuffer tempStr = new StringBuffer();
        tempStr.append("<font color=red>");
        tempStr.append(sec);
        tempStr.append("</font>");
        tempStr.append(str);
        return tempStr.toString();
    }

    public static void setViewText(View view, String text) {
        TextView textView = (TextView) view.findViewById(R.id.load_text);
        textView.setText(text);
    }

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    private static final Set<String> mWhiteList = new HashSet<String>();
    private static final Set<String> mDefaultWhiteList = new HashSet<String>();
    // Gionee <yangxinruo><2016-1-5> add for CR01618272 begin
    private static Set<String> mBlackKillList = new HashSet<String>();

    public static void cleanBlackKillList() {
        mBlackKillList.clear();
    }

    public static Set<String> getBlackKillList(Context context) {
        if (mBlackKillList.size() == 0) {
            Set<String> blackKillList = new HashSet<String>();
            Cursor c = null;
            try {
                c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                        new String[]{"packagename"},
                        "usertype='" + BLACK_KILL_TYPE + "' and status='1' ", null, null);
                if (c != null && c.moveToFirst()) {
                    do {
                        blackKillList.add(c.getString(0));
                    } while (c.moveToNext());
                    mBlackKillList = blackKillList;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            }
        }
        return mBlackKillList;
    }

    // Gionee <yangxinruo><2016-1-5> add for CR01618272 end
    // Gionee <yangxinruo> <2016-5-10> add for CR01658695 begin
    private static Set<String> mSafedAppList = null;
    public static final String SAFED_APP_TYPE = "safedapp";

    public static void cleanSafedAppList() {
        if (mSafedAppList != null) {
            mSafedAppList.clear();
            mSafedAppList = null;
        }
    }

    public static Set<String> getSafedAppList(Context context) {
        if (mSafedAppList == null) {
            Set<String> safedAppList = new HashSet<String>();
            Cursor c = null;
            try {
                c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                        new String[]{"packagename"}, "usertype='" + SAFED_APP_TYPE + "' ", null, null);
                if (c != null && c.moveToFirst()) {
                    do {
                        safedAppList.add(c.getString(0));
                    } while (c.moveToNext());
                    mSafedAppList = safedAppList;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            }
        }
        return mSafedAppList;
    }

    public static final String SAFED_LIST_TYPE = "safelist";
    public static final int SAFED_LIST_FLAG_ALL_APPS = 0x01;
    public static final int SAFED_LIST_FLAG_SYSTEMMANAGER = 0x02;
    public static final int SAFED_LIST_FLAG_CYEELAUNCHER = 0x04;
    public static final int SAFED_LIST_FLAG_GET_ALL = SAFED_LIST_FLAG_ALL_APPS | SAFED_LIST_FLAG_SYSTEMMANAGER
            | SAFED_LIST_FLAG_CYEELAUNCHER;

    public static Set<String> getSafedList(Context context, int flags) {
        Set<String> safedList = new HashSet<String>();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                    new String[]{"packagename", "status"}, "usertype='" + SAFED_LIST_TYPE + "' ", null,
                    null);
            if (c != null && c.moveToFirst()) {
                do {
                    if ((flags & c.getInt(1)) != 0) {
                        safedList.add(c.getString(0));
                    }
                } while (c.moveToNext());
            }
            Log.d(TAG, "getSafedList show res = " + safedList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return safedList;
    }

    public static HashMap<String, Integer> getSafedListMap(Context context, int flags) {
        HashMap<String, Integer> safedListMap = new HashMap<String, Integer>();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                    new String[]{"packagename", "status"}, "usertype='" + SAFED_LIST_TYPE + "' ", null,
                    null);
            if (c != null && c.moveToFirst()) {
                do {
                    if ((flags & c.getInt(1)) != 0) {
                        safedListMap.put(c.getString(0), c.getInt(1));
                    }
                } while (c.moveToNext());
            }
            Log.d(TAG, "getSafedListMap show res = " + safedListMap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return safedListMap;
    }

    public static boolean isSafedListApp(String pkgname, Context context, int flags) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI, new String[]{"status"},
                    "packagename=? AND usertype=?", new String[]{pkgname, SAFED_LIST_TYPE}, null);
            if (c != null && c.moveToFirst()) {
                do {
                    Log.d(TAG, "isSafedListApp get pkg " + pkgname + " status=" + c.getInt(0));
                    if ((flags & c.getInt(0)) != 0) {
                        return true;
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return false;
    }
    // Gionee <yangxinruo> <2016-5-10> add for CR01658695 end

    public static void writeAppProcessLimitOptions(SharedPreferences mSharedPreferences) {
        try {
            int limit = mSharedPreferences.getInt("app_process_limit", -1);
            if (limit != -1) {
                ActivityManagerNative.getDefault().setProcessLimit(limit);
            }
        } catch (RemoteException e) {
        }
    }

    // Gionee <xuwen><2015-08-31> add for CR01536073 begin
    public static List<String> getPowerSaveWhiteList(Context context) {
        String[] whitearray = context.getResources().getStringArray(R.array.super_power_save_whitelist);
        return Arrays.asList(whitearray);
    }

    // Gionee <xuwen><2015-08-31> add for CR01536073 end

    // Gionee xionghg add for power saving optimization begin
    public static List<String> getPowerSaveMoreWhiteList(Context context) {
        //Chenyee guoxt modify for CSW1703CX-249 begin
        String[] whiteArray;
        if (Consts.cyCXFlag) {
            whiteArray = context.getResources().getStringArray(R.array.super_power_save_whitelist_xiaolajiao_more);
        } else {
            whiteArray = context.getResources().getStringArray(R.array.super_power_save_whitelist_more);
        }
        //Chenyee guoxt modify for CSW1703CX-249 end
        return Arrays.asList(whiteArray);
    }
    // Gionee xionghg add for power saving optimization end

    // Gionee <yangxinruo> <2015-12-7> add for CR01604665 begin
    public static List<String> getPowerSaveNeedDisable(Context context) {
        String[] whitearray = context.getResources().getStringArray(R.array.super_power_need_disable);
        return Arrays.asList(whitearray);
    }

    // Gionee <yangxinruo> <2015-12-7> add for CR01604665 end
    // Gionee <yangxinruo> <2016-1-5> add for CR01618272 begin
    public static ArrayList<String> getPackagesNameByPid(PackageManager packageManager,
                                                         List<RunningAppProcessInfo> runningAppList, int pID) {
        ArrayList<String> processName = new ArrayList<String>();
        if (runningAppList == null || runningAppList.isEmpty()) {
            Log.d(TAG, "no running app info!");
            return processName;
        }
        Iterator<RunningAppProcessInfo> i = runningAppList.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid != pID) {
                    continue;
                }
                if (info.pkgList == null || info.pkgList.length <= 0) {
                    Log.d(TAG, "can not find pkgnames for pid :" + pID);
                    continue;
                }
                for (String pkgName : info.pkgList) {
                    if (!processName.contains(pkgName)) {
                        processName.add(pkgName);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Error with pid " + pID + ">> :" + e.toString());
            }
        }
        return processName;
    }

    public static ArrayList<String> getActiveCameraApps(PackageManager packageManager,
                                                        List<RunningAppProcessInfo> runningAppList) {
        String resultStr = executeShellCmd("dumpsys media.camera | grep -A 1 'Device is open'");

        ArrayList<String> resArray = new ArrayList<String>();
        if (resultStr.isEmpty()) {
            return resArray;
        }
        Log.e(TAG, "dump media.camera Result:\n" + resultStr);
        String rex = "PID:\\s+(\\d+)";
        Pattern patterndiff = Pattern.compile(rex);
        Matcher matcher = patterndiff.matcher(resultStr);
        while (matcher.find()) {
            for (String pkgname : getPackagesNameByPid(packageManager, runningAppList,
                    Integer.parseInt(matcher.group(1)))) {
                if (!pkgname.isEmpty() && !resArray.contains(pkgname)) {
                    resArray.add(pkgname);
                    Log.d(TAG, "pkg using camera " + pkgname);
                }
            }
        }
        return resArray;
    }

    public static ArrayList<String> getActiveSensorApps(PackageManager packageManager) {
        String resultStr = executeShellCmd("dumpsys sensorservice | grep -A 3 'Connection Number:'");

        ArrayList<String> resArray = new ArrayList<String>();
        if (resultStr.isEmpty()) {
            return resArray;
        }
        Log.e(TAG, "dump sensorservice Result:\n" + resultStr);
        String rex = "\\|\\suid\\s(\\d+)";
        Pattern patterndiff = Pattern.compile(rex);
        Matcher matcher = patterndiff.matcher(resultStr);
        while (matcher.find()) {
            String[] pkgnames = packageManager.getPackagesForUid(Integer.parseInt(matcher.group(1)));
            if (pkgnames == null || pkgnames.length <= 0) {
                continue;
            }
            for (String pkgname : pkgnames) {
                if (!pkgname.isEmpty() && !resArray.contains(pkgname)) {
                    resArray.add(pkgname);
                    Log.d(TAG, "pkg using sensor " + pkgname);
                }
            }
        }
        return resArray;
    }

    public static ArrayList<String> getActiveAudioTrackApps(PackageManager packageManager,
                                                            List<RunningAppProcessInfo> runningAppList) {
        String resultStr = executeShellCmd("dumpsys media.audio_flinger");

        ArrayList<String> resArray = new ArrayList<String>();
        if (resultStr.isEmpty()) {
            return resArray;
        }
        // Gionee <yangxinruo> <2016-1-19> modify for CR01625692 begin
        SparseArray<ArrayList<Integer>> sessionMap = new SparseArray<ArrayList<Integer>>();
        boolean globalSessionOn = false;
        boolean outputTrackOn = false;
        boolean inputTrackOn = false;
        String rexGlobalSessionStr = "(\\d+)\\s+?(\\d+)\\s+?\\d+";
        //guoxt 20180417 modify for CSW1703A-1781 begin
        // String rexOutputStr = ".+yes\\s+?\\d+\\s+?\\d+\\s+?\\d+\\s+?\\d+\\s+?(\\d+).+";
        String rexOutputStr = ".+yes\\s+?(\\d+)\\s+?(\\d+)\\s+?[a-zA-Z].+";
        // String rexInputStr =  ".+yes\\s+?\\d+\\s+?\\d+\\s+?\\d+\\s+?(\\d+).+";
        String rexInputStr = "\\s+?(\\d+)\\s+?(\\d+).+";
        //guoxt 20180417 modify for CSW1703A-1781 end
        Pattern patternGlobalSession = Pattern.compile(rexGlobalSessionStr);
        Pattern patternOutput = Pattern.compile(rexOutputStr);
        Pattern patternInput = Pattern.compile(rexInputStr);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(resultStr.getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                // Log.d(TAG, "dump audio +++++++++" + line);
                if (globalSessionOn) {
                    Matcher matcher = patternGlobalSession.matcher(line);
                    if (matcher.find()) {
                        ArrayList<Integer> pidArray = sessionMap.get(Integer.parseInt(matcher.group(1)));
                        if (pidArray == null) {
                            pidArray = new ArrayList<Integer>();
                            sessionMap.append(Integer.parseInt(matcher.group(1)), pidArray);
                        }
                        pidArray.add(Integer.parseInt(matcher.group(2)));
                        // sessionMap.append(Integer.parseInt(matcher.group(1)),
                        // Integer.parseInt(matcher.group(2)));
                        Log.d(TAG, "+++++++++add v->" + matcher.group(1) + "->" + matcher.group(2));
                    } else if (line.isEmpty()) {
                        globalSessionOn = false;
                        // Log.d(TAG,"+++++++++globalSessionOff");
                    }
                }
                if (outputTrackOn) {
                    Matcher matcher = patternOutput.matcher(line);
                    if (matcher.find()) {
                        //guoxt 20180417 modify for CSW1703A-1781 begin
                        Log.d(TAG, "find output pids for session " + Integer.parseInt(matcher.group(2)));
                        // int pid = sessionMap.get(Integer.parseInt(matcher.group(1)));
                        ArrayList<Integer> pids = sessionMap.get(Integer.parseInt(matcher.group(2)));
                        //guoxt 20180417 modify for CSW1703A-1781 end
                        if (pids != null) {
                            for (int pid : pids) {
                                Log.d(TAG, "+++++++++v->" + pid);
                                fillPackageNameByProcess(packageManager, runningAppList, resArray, pid);
                            }
                        }
                    } else if (line.isEmpty()) {
                        outputTrackOn = false;
                        // Log.d(TAG,"+++++++++outputTrackOff");
                    }
                }
                if (inputTrackOn) {
                    Matcher matcher = patternInput.matcher(line);
                    if (matcher.find()) {
                        Log.d(TAG, "find input pids for session " + Integer.parseInt(matcher.group(1)));
                        // int pid = sessionMap.get(Integer.parseInt(matcher.group(1)));
                        ArrayList<Integer> pids = sessionMap.get(Integer.parseInt(matcher.group(1)));
                        if (pids != null) {
                            for (int pid : pids) {
                                Log.d(TAG, "+++++++++v->" + pid);
                                fillPackageNameByProcess(packageManager, runningAppList, resArray, pid);
                            }
                        }
                    } else if (line.isEmpty()) {
                        inputTrackOn = false;
                        // Log.d(TAG,"+++++++++inputTrackOff");
                    }
                }
                if ("Global session refs:".equals(line)) {
                    globalSessionOn = true;
                    // Log.d(TAG,"+++++++++globalSessionOn");
                } else if (line.startsWith("Output thread ")) {
                    outputTrackOn = true;
                    // Log.d(TAG,"+++++++++outputTrackOn");
                } else if (line.startsWith("Input thread ")) {
                    inputTrackOn = true;
                    // Log.d(TAG,"+++++++++inputTrackOn");
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "IOexception " + e);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    Log.d(TAG, "close BufferedReader error!" + e);
                }
        }
        // Gionee <yangxinruo> <2016-1-19> modify for CR01625692 end
        return resArray;
    }

    private static void fillPackageNameByProcess(PackageManager packageManager,
                                                 List<RunningAppProcessInfo> runningAppList, ArrayList<String> resArray, int pid) {
        ArrayList<String> pkgNamesFromPid = getPackagesNameByPid(packageManager, runningAppList, pid);
        for (String pkgname : pkgNamesFromPid) {
            if (!pkgname.isEmpty() && !resArray.contains(pkgname)) {
                resArray.add(pkgname);
            }
        }
        String pkgNameFromProcessName = pkgNameFromProcessName(packageManager, runningAppList, pid);
        if (!pkgNameFromProcessName.isEmpty() && !resArray.contains(pkgNameFromProcessName)) {
            resArray.add(pkgNameFromProcessName);
        }
        Log.d(TAG, "pid :" + pid + " pkgNames = " + resArray);
    }

    private static String pkgNameFromProcessName(PackageManager pm,
                                                 List<RunningAppProcessInfo> runningAppList, int pID) {
        String processName = "";
        if (runningAppList == null || runningAppList.isEmpty()) {
            Log.d(TAG, "no running app info!");
            return processName;
        }
        Iterator<RunningAppProcessInfo> i = runningAppList.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                Log.d(TAG, "show me pid=" + info.pid + " processName=" + info.processName);
                if (info.pid != pID) {
                    continue;
                }
                processName = info.processName.split(":")[0];
            } catch (Exception e) {
                Log.d(TAG, "pkgNameFromProcessName failed E:" + e);
                processName = "";
            }
        }
        return processName;
    }

    public static String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res == null || res.activityInfo == null) {
            return null;
        }
        return res.activityInfo.packageName;
    }

    //Chenyee guoxt modify for CSW1703A-1870 begin
    /*
     获取当前桌面上正在使用的widget 应用
     */
    public static ArrayList<String> getEnableAppWidgetPackages(Context context, List<RunningAppProcessInfo> runningAppList) {
        ArrayList<String> packages = new ArrayList<>();
        String launcherPkg = getLauncherPackageName(context);
        if (launcherPkg == null) {
            return packages;
        }
        try {
            final AppWidgetManager manager = AppWidgetManager.getInstance(context);
            Field f = AppWidgetManager.class.getDeclaredField("mService");
            f.setAccessible(true);
            final Object service = f.get(manager);
            Class c = Class.forName("com.android.internal.appwidget.IAppWidgetService");
            Method m = c.getDeclaredMethod("getAppWidgetOfHost", String.class, int.class);
            m.setAccessible(true);

            packages.addAll(((List<ComponentName>) m.invoke(service, launcherPkg, 0)).
                    stream().map(new Function<ComponentName, String>() {
                @Override
                public String apply(ComponentName component) {
                    return component.getPackageName();
                }
            }).distinct().collect(Collectors.toList()));
        } catch (Exception e) {
            Log.i("TAG_A", "error", e);
        }

        return packages;
    }
//
//    public static ArrayList<String> getActiveUseWidgetApps( Context context,
//                                                           List<RunningAppProcessInfo> runningAppList) {
//        ArrayList<String> resArray = new ArrayList<String>();
//        AppWidgetManager mAppWidgetManager;
//        mAppWidgetManager = AppWidgetManager.getInstance(context);
//        List<AppWidgetProviderInfo> providers = mAppWidgetManager
//                .getInstalledProviders();
//        final int providerCount = providers.size();
//        for (int i = 0; i < providerCount; i++) {
//            ComponentName provider = providers.get(i).provider;
//            String pkgname = provider.getPackageName();
//            Log.d("guoxt:", ">>>>> package name :  " + provider.getPackageName()
//                    + ">>>>>> class name : " + provider.getClassName());
//
//                if (!pkgname.isEmpty() && !resArray.contains(pkgname)) {
//                    resArray.add(pkgname);
//                    Log.d(TAG, "pkg using gps " + pkgname);
//                }
//        }
//        return  resArray;
//
//    }

    //Chenyee guoxt modify for CSW1703A-1870 end
    public static ArrayList<String> getActiveGpsApps(PackageManager packageManager) {
        String resultStr = executeShellCmd("dumpsys location|grep 'Reciever\\['");

        ArrayList<String> resArray = new ArrayList<String>();
        if (resultStr.isEmpty()) {
            return resArray;
        }
        Log.e(TAG, "dump location Result:\n" + resultStr);
        String rex = "\\((\\d+?)\\)";
        Pattern patterndiff = Pattern.compile(rex);
        Matcher matcher = patterndiff.matcher(resultStr);
        while (matcher.find()) {
            String[] pkgnames = packageManager.getPackagesForUid(Integer.parseInt(matcher.group(1)));
            if (pkgnames == null || pkgnames.length <= 0) {
                continue;
            }
            for (String pkgname : pkgnames) {
                if (!pkgname.isEmpty() && !resArray.contains(pkgname)) {
                    resArray.add(pkgname);
                    Log.d(TAG, "pkg using gps " + pkgname);
                }
            }
        }
        return resArray;
    }

//    public static ArrayList<String> showProcessAdjList(Context context) {
//        String resultStr = executeShellCmd("dumpsys activity|grep 'trm:'");
//
//        ArrayList<String> resArray = new ArrayList<String>();
//        if (!"".equals(resultStr)) {
//            Log.e(TAG, "dump activity Result:\n" + resultStr);
//        }
//        return resArray;
//    }
    // Gionee <yangxinruo> <2016-1-5> add for CR01618272 begin

    // Gionee <yangxinruo><2016-2-26> add for CR01640160 begin
    public static int getCpuCoreNums() {
        // Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
        }

        try {
            // Get directory containing CPU info
//            File dir = new File("/sys/devices/system/cpu/");
            File dir = new File(File.separator + "sys" + File.separator + "devices" + File.separator
                    + "system" + File.separator + "cpu" + File.separator);
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            Log.d(TAG, "CPU Count: " + files.length);
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            // Print exception
            Log.d(TAG, "CPU Count: Failed.");
            e.printStackTrace();
            // Default to return 1 core
            return 1;
        }
    }
    // Gionee <yangxinruo><2016-2-26> add for CR01640160 end

    // Gionee <houjie> <2016-03-19> add for CR01645506 begin
    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
    // Gionee <houjie> <2016-03-19> add for CR01645506 end

    public static ArrayList<WakelockInfo> getWakelocks(Context context) {
        // Log.d(TAG, "exe cmd :" + "dumpsys power|grep 'PARTIAL_WAKE_LOCK'");
        String resultStr = executeShellCmd("dumpsys power|grep 'PARTIAL_WAKE_LOCK'");
        // Log.d(TAG, "execmd res=" + resultStr);

        String rexPartialWakelockStr = "\\s+PARTIAL_WAKE_LOCK\\s+'(.+?)'\\s\\(uid=(\\d+),\\spid=(\\d+)";
        Pattern patternPartialWakelock = Pattern.compile(rexPartialWakelockStr);

        ArrayList<WakelockInfo> resArray = new ArrayList<WakelockInfo>();
        BufferedReader br = null;
        PackageManager pm = context.getPackageManager();
        try {
            br = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(resultStr.getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                // Log.d(TAG, "getWakelocks parse line=" + line);
                Matcher matcher = patternPartialWakelock.matcher(line);
                if (matcher.find()) {
                    // Log.d(TAG, "+++++++++add v->");
                    String wakelockName = "";
                    String wakelockPkgName = "";
                    int wakelockUid = -1;
                    int wakelockPid = -1;
                    if (matcher.groupCount() >= 1) {
                        wakelockName = matcher.group(1);
                    }
                    if (matcher.groupCount() >= 2) {
                        wakelockUid = Integer.parseInt(matcher.group(2));
                        String[] packages = pm.getPackagesForUid(wakelockUid);
                        if (packages != null && packages.length != 0) {
                            wakelockPkgName = packages[0];
                        }
                    }
                    if (matcher.groupCount() >= 3) {
                        wakelockPid = Integer.parseInt(matcher.group(3));
                    }
                    String pkgVserion = HelperUtils.getPackageVersion(pm, wakelockPkgName);
                    resArray.add(new WakelockInfo(wakelockName, wakelockUid, wakelockPid, wakelockPkgName,
                            pkgVserion));
                }
            }
        } catch (IOException ioE) {
            Log.d(TAG, "IOexception " + ioE);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    Log.d(TAG, "close BufferedReader error!" + e);
                }
        }

        return resArray;
    }

    public static class WakelockInfo {
        public String name;
        public int uid;
        public int pid;
        public String pkgName;
        public String pkgVersion;

        public WakelockInfo(String name, int uid, int pid, String pkgName, String pkgVserion) {
            this.name = name;
            this.uid = uid;
            this.pid = pid;
            this.pkgName = pkgName;
            this.pkgVersion = pkgVserion;
        }

        @Override
        public String toString() {
            String str = "waklock info: name=" + name + " uid=" + uid + " pid=" + pid + " pkgName=" + pkgName
                    + " pkgVersion=" + pkgVersion;
            return str;
        }

    }

    public static String executeShellCmd(String cmd) {
        int read;
        char[] buffer = new char[4096];
        StringBuffer output = new StringBuffer();
        String resultStr = "";
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }

            int suProcessRetval = process.waitFor();
            if (255 != suProcessRetval) {
                resultStr = output.toString();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error executing:" + cmd, ex);
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, "close BufferedReader error!" + e);
                }
            }
        }
        return resultStr;
    }

    public static String getRomVersion() {
        String version = "";
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);
            version = (String) method.invoke(null, "ro.gn.gnznvernumber", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getModel() {
        String model = "phone";
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);
            model = (String) method.invoke(null, "ro.product.model", "Phone");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public static String getPlatform() {
        String model = "platform";
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);
            model = (String) method.invoke(null, "ro.board.platform", "platform");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public static String getImei(Context context) {
        return getImei(context, false);
    }

    public static String getImei(Context context, boolean isEncrypt) {
        String imei = getRawImei(context);
        if (imei == null || imei.length() < 10) {
            Log.e(TAG, "imei is error. imei =" + imei);
        }
        if (isEncrypt) {
            return GNDecodeUtils.get(imei);
        } else {
            return imei;
        }
    }

    private static String getRawImei(Context context) {
        // get imei for BEIJING projects
        String pro_imei = SystemProperties.get("persist.sys.imei_for_y3");
        if (pro_imei != null && !pro_imei.isEmpty()) {
            return pro_imei;
        }
        // get imei for SHENZHEN projects
        pro_imei = SystemProperties.get("persist.radio.imei");
        if (pro_imei != null && !pro_imei.isEmpty()) {
            return pro_imei;
        }
        // get imei for leagcy projects
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        if (imei == null) {
            imei = "";
        }
        return imei;
    }

    public static String getPackageVersion(PackageManager packageManager, String pkgname) {
        try {
            PackageInfo pi = packageManager.getPackageInfo(pkgname, 0);
            String pkgVersion = pi.versionName;
            if (pkgVersion == null) {
                pkgVersion = "";
            }
            return pkgVersion;
        } catch (Exception e) {
            Log.d(TAG, "pkg not found " + pkgname + " " + e);
            return "";
        }
    }

    public static ApplicationInfo getApplicationInfo(Context context, String pkgName, int flags) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, flags);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ApplicationInfo getApplicationInfo(Context context, String pkgName) {
        final boolean GN_ENCRYPTIONSPACE_PROP = SystemProperties.get("ro.encryptionspace.enabled", "false")
                .equals("true");
        if (GN_ENCRYPTIONSPACE_PROP) {
            return getApplicationInfo(context, pkgName, 0x00200000);
        } else {
            return getApplicationInfo(context, pkgName, 0);
        }
    }

    public static void dumpList(String tag, String key, List<?> list) {
        Log.d(tag, key + " dumpList begin...");
        for (Object object : list) {
            Log.v(tag, key + ": " + object.toString());
        }
        Log.d(tag, key + " dumpList end...");
    }

    public static void dumpArray(String tag, String key, Object[] array) {
        Log.d(tag, key + " dumpArray begin...");
        for (Object object : array) {
            Log.d(tag, key + ": " + object.toString());
        }
        Log.d(tag, key + " dumpArray end...");
    }

    public static void dumpMap(String tag, String key, Map<?, ?> map) {
        Log.d(tag, key + " dumpMap begin...");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Log.d(tag, key + ": key=" + entry.getKey() + ", value=" + entry.getValue());
        }
        Log.d(tag, key + " dumpMap end...");
    }

    public static ArrayList<String> getEncryptionsApps(ContentResolver contentResolver) {
        ArrayList<String> res = new ArrayList<String>();
        if (SystemProperties.get("ro.encryptionspace.enabled", "false").equals("true")) {
            Class<?> encryptionsClass = FrameworkUtility.createClass("android.provider.Encryptions$Secure");
            if (encryptionsClass == null) {
                return res;
            }
            String appsStr = (String) FrameworkUtility.invokeStaticMethod(encryptionsClass, "getString",
                    new Class[]{ContentResolver.class, String.class},
                    new Object[]{contentResolver, "encryptspace_apps"});
            if (appsStr == null || appsStr.isEmpty()) {
                return res;
            }
            String[] appArray = appsStr.split(";");
            res.addAll((List<String>) Arrays.asList(appArray));
            Log.d(TAG, "show EncryptionsApp list: " + res.toString());
            return res;
        } else {
            return res;
        }
    }

    public static List<ApplicationInfo> getDisabledApps(Context context) {
        ArrayList<ApplicationInfo> res = new ArrayList<ApplicationInfo>();
        int queryFlags = PackageManager.GET_DISABLED_COMPONENTS;
        if (SystemProperties.get("ro.encryptionspace.enabled", "false").equals("true")) {
            queryFlags |= getEncryptAppFlag();
        }
        List<ApplicationInfo> applications = context.getPackageManager().getInstalledApplications(queryFlags);
        for (ApplicationInfo info : applications) {
            if (!info.enabled) {
                res.add(info);
            }
        }
        return res;
    }

    public static boolean isPackageExist(Context context, String packagename) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getInstallerPackageName(packagename);
            Log.d(TAG, "packagename " + packagename + " exist");
            return true;
        } catch (Exception e) {
            Log.d(TAG, "packagename " + packagename + " not exist");
            return false;
        }
    }

    public static String[] getPkgNameByUid(Context context, int uid) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackagesForUid(uid);
        } catch (Exception e) {
            Log.d(TAG, " getPkgNameByUid uid= " + uid + " Exception e=" + e.toString());
        }
        return null;
    }

    public static String getTopActivityPackageName(Context context) {
        String pkgName = null;
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).topActivity != null) {
                pkgName = runningTaskInfos.get(0).topActivity.getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgName;
    }

    public static String getBaseActivityPackageName(Context context) {
        String pkgName = null;
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).baseActivity != null) {
                pkgName = runningTaskInfos.get(0).baseActivity.getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgName;
    }

    public static int getUidByPackageName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            return info.applicationInfo.uid;
        } catch (Exception e) {
            Log.d(TAG, "pkg not found " + packageName + " " + e);
            return -1;
        }
    }

    public static int getEncryptAppFlag() {
        int res = 0x00400000; // default
        Object systemFlag = FrameworkUtility.getStaticFieldValue(PackageManager.class, "GET_ENCRYPTAPPS");
        if (systemFlag == null) {
            return res;
        }
        return (int) systemFlag;
    }

    /**
     * If there is 'test0123456789softmanager' in the internal storage root directory, true is returned,
     * otherwise the false is returned. So if you want to return true, you should create the
     * 'test0123456789softmanager' directory in the root directory
     */
    public static boolean isUseTestUrl() {
        try {
            final String testDirectoryName = "test0123456789softmanager";
            String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String testDirectory = sdcardPath + "/" + testDirectoryName;
            File f = new File(testDirectory);
            Log.d(TAG, "test path=" + testDirectory);
            return f.exists() && f.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }
}
