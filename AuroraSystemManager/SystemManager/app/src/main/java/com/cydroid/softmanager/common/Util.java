package com.cydroid.softmanager.common;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.cydroid.softmanager.utils.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Util {
    private final static String TAG = "Util";

    private final static Uri CONTENT_URI = Uri.parse("content://com.cyee.settings.RosterProvider/rosters");
    private final static String[] ROOT_LIST = new String[]{"com.cydroid.softmanager", "system_server",
            "com.cydroid.softmanager:remote", "cy.com.android.synchronizer", "com.cydroid.dataclone",
            "com.cydroid.filemanager", "com.android.settings", "com.android.providers.privatemedia",
            "com.android.camera", "com.cydroid.gallery"};

    public static void unbindDrawables(View view) {
        try {
            if (view == null) {
                Log.w("Util", "unbindDrawables view == null");
                return;
            }

            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                Drawable drawable = imageView.getDrawable();
                if (drawable != null && drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null) {
                        // bitmap.recycle();
                        bitmap = null;
                    }
                    drawable = null;
                }
                drawable = imageView.getBackground();
                if (drawable != null && drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null) {
                        // bitmap.recycle();
                        bitmap = null;
                    }
                    drawable = null;
                }
                imageView.setImageDrawable(null);
                imageView.setBackgroundDrawable(null);
            }

            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
                view.setBackgroundResource(0);
            }

            if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        } catch (Exception e) {
            Log.e("Util", "unbindDrawables exception --->", e);
        }
    }

    public static final HashSet<String> mCheckPermissions = new HashSet<String>();
    public static final HashSet<String> mWhiteList = new HashSet<String>();

    static {
        mCheckPermissions.add("android.permission.CALL_PHONE");
        mCheckPermissions.add("android.permission.SEND_SMS");
        mCheckPermissions.add("android.permission.SEND_SMS_MMS");
        mCheckPermissions.add("android.permission.READ_SMS");
        mCheckPermissions.add("android.permission.READ_SMS_MMS");
        mCheckPermissions.add("android.permission.READ_CONTACTS");
        mCheckPermissions.add("android.permission.READ_CONTACTS_CALLS");
        mCheckPermissions.add("android.permission.READ_CALL_LOG");
        mCheckPermissions.add("android.permission.WRITE_SMS");
        mCheckPermissions.add("android.permission.WRITE_SMS_MMS");
        mCheckPermissions.add("android.permission.WRITE_CONTACTS");
        mCheckPermissions.add("android.permission.WRITE_CONTACTS_CALLS");
        mCheckPermissions.add("android.permission.WRITE_CALL_LOG");
        mCheckPermissions.add("android.permission.ACCESS_FINE_LOCATION");
        mCheckPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
        mCheckPermissions.add("android.permission.RECORD_AUDIO");
        mCheckPermissions.add("android.permission.READ_PHONE_STATE");
        mCheckPermissions.add("android.permission.CAMERA");
        mCheckPermissions.add("android.permission.BLUETOOTH");
        mCheckPermissions.add("android.permission.BLUETOOTH_ADMIN");
        mCheckPermissions.add("android.permission.CHANGE_WIFI_STATE");
        mCheckPermissions.add("android.permission.CHANGE_NETWORK_STATE");
        mCheckPermissions.add("android.permission.NFC");
        mWhiteList.add("com.android.commands.monkey");
    }

    // Gionee <liuyb> <2014-6-28> add for CR01311273 begin
    public static final List<ComponentName> mMmsComponent = new ArrayList<ComponentName>();

    static {
        mMmsComponent.add(
                new ComponentName("com.android.mms", "com.android.mms.transaction.AlertMissMsgReceiver"));
        mMmsComponent.add(new ComponentName("com.android.mms", "com.android.mms.transaction.SmsReceiver"));
        mMmsComponent
                .add(new ComponentName("com.android.mms", "com.android.mms.transaction.WapPushReceiver"));
        mMmsComponent.add(
                new ComponentName("com.android.mms", "com.android.mms.transaction.MmsSystemEventReceiver"));
        mMmsComponent.add(
                new ComponentName("com.android.contacts", "com.gionee.android.contacts.SimStateReceiver"));
        mMmsComponent
                .add(new ComponentName("com.android.deskclock", "com.android.deskclock.AlarmInitReceiver"));
        mMmsComponent.add(new ComponentName("com.cydroid.note", "com.cydroid.note.AlarmInitReceiver"));
    }

    public static void enableMmsComponent(Context context) {
        PackageManager pm = context.getPackageManager();
        for (ComponentName name : mMmsComponent) {
            try {
                pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void initRootNameList(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean flag = preferences.getBoolean("is_need_init_rootlist", true);
        if (flag) {
            Log.e("Util", "initRootListPackages");
            try {
                context.getContentResolver().delete(CONTENT_URI, "usertype='root'", null);
                for (String pkgName : ROOT_LIST) {
                    ContentValues values = new ContentValues();
                    values.put("usertype", "root");
                    values.put("packagename", pkgName);
                    values.put("status", 1);
                    context.getContentResolver().insert(CONTENT_URI, values);
                }
                preferences.edit().putBoolean("is_need_init_rootlist", false).commit();

                android.provider.Settings.System.putInt(context.getContentResolver(), "oneclean_data_version",
                        28);
            } catch (Exception e) {
                Log.e("Util", "initRootNameList", e);
            }
        }
    }
    // Gionee <liuyb> <2014-6-28> add for CR01311273 end

    // Gionee <liuyb> <2014-10-13> add for CR01393511 begin
    public static final List<ComponentName> mSettingPermissionComponent = new ArrayList<ComponentName>();

    static {
        mSettingPermissionComponent.add(new ComponentName("com.android.settings",
                "com.android.settings.permission.PermissionAppDetail"));
        mSettingPermissionComponent.add(new ComponentName("com.android.settings",
                "com.android.settings.permission.PemissionDeniedReceiver"));
    }

    public static void modifySettingPermissionComponent(Context context) {
        PackageManager pm = context.getPackageManager();
        int settingsVersion = getVersionCode(context, "com.android.settings");
        int softmanagerVersion = getVersionCode(context, "com.cydroid.softmanager");
        for (ComponentName name : mSettingPermissionComponent) {
            try {
                if (settingsVersion >= 50103001 && softmanagerVersion < 10404001) {
                    pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                }
                if (settingsVersion >= 50103001 && softmanagerVersion >= 10404001) {
                    pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getVersionCode(Context context, String packagerName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo mPackageInfo = null;
        try {
            mPackageInfo = pm.getPackageInfo(packagerName, PackageManager.GET_DISABLED_COMPONENTS
                    | PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_SIGNATURES);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        return mPackageInfo.versionCode;
    }
    // Gionee <liuyb> <2014-10-13> add for CR01393511 end

    public static List<String> getLauncherPkg(Context context) {
        // String pkg = "android";
        List<String> re = new ArrayList<>();
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> lst = pm.queryIntentActivities(intent, 0);
            for (ResolveInfo resolveInfo : lst) {
                re.add(resolveInfo.activityInfo.packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }

    // Gionee <liuyb> <2014-6-28> add for CR01403879 begin
    public static String getDefaultLauncherPkg(Context context) {
        // String pkg = "android";
        String pkg = "com.android.launcher3";
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo match = context.getPackageManager().resolveActivity(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (match != null && match.activityInfo != null) {
                pkg = match.activityInfo.packageName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == pkg) {
            pkg = "";
        }
        Log.i(TAG, "DefaultLauncherPkg-->" + pkg);
        return pkg;
    }

    public static List<String> getMusicApps(Context context) {
        List<String> ls = new ArrayList<String>();
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.parse("file:///android_asset/gionee"), "audio/mpeg");
            List<ResolveInfo> riLists = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
            for (ResolveInfo ri : riLists) {
                if (ri != null && ri.activityInfo != null) {
                    ls.add(ri.activityInfo.packageName);
                }
            }
            Log.i(TAG, "music app -----> " + ls.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ls;
    }

    public static boolean isPlayMusic(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return am.isMusicActive();
    }

    public static boolean isFmOn(Context context) {
        boolean flag = false;
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            Class<?> cpClass = am.getClass();
            Method method = cpClass.getDeclaredMethod("isFmActive");
            flag = (Boolean) method.invoke(am);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static List<String> getFMApps(Context context) {
        List<String> ls = new ArrayList<String>();
        ls.add("com.caf.fmradio");
        ls.add("com.mediatek.FMRadio");
        ls.add("com.android.fmradio");
        return ls;
    }

    public static List<String> getInstalledLauncherList(Context context) {
        List<String> result = new ArrayList<String>();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> riLists = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
        try {
            for (ResolveInfo rInfo : riLists) {
                if (rInfo != null && rInfo.activityInfo != null
                        && !"android".endsWith(rInfo.activityInfo.packageName)) {
                    result.add(rInfo.activityInfo.packageName);
                    Log.i(TAG, "InstalledLauncher --> " + rInfo.activityInfo.packageName);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "InstalledLauncher---->", e);
        }
        return result;
    }
    // Gionee <liuyb> <2014-6-28> add for CR01403879 end

    // Gionee <liuyb> <2014-10-04> add for CR01406883 begin
    public static void killSpecialProcess(Context context) {
        killAndroidHiddenProcess(context);
    }

    public static void killAndroidHiddenProcess(Context context) {
        // read pid
        try {
            String returnStr = do_exec(
                    new String[]{"sh", "-c", " ps | grep -e 'com\\.' -e 'net\\.' -e 'cn\\.' -e 'org\\.' "});
            Map<String, Integer> psProcHm = new HashMap<>();
            Scanner scanner = new Scanner(returnStr);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String pidStr = line.substring(10, 16).replaceAll(" ", "");
                String procStr = line.substring(line.lastIndexOf(" ") + 1);
                psProcHm.put(procStr, Integer.parseInt(pidStr));
            }
            scanner.close();

            Map<String, Integer> amsProcHm = getCurrentProcesses(context);
            for (Map.Entry<String, Integer> entry : psProcHm.entrySet()) {
                if (amsProcHm.get(entry.getKey()) == null && !mWhiteList.contains(entry.getKey())) {
                    Log.i(TAG, entry.getKey() + " --------> " + entry.getValue());
                    Intent intent = new Intent(Consts.ACTION_KILL_PROCESS);
                    intent.putExtra("processid", entry.getValue());
                    context.sendBroadcast(intent);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "killAndroidHiddenProcess--->", e);
        }
    }

    private static Map<String, Integer> getCurrentProcesses(Context context) {
        Map<String, Integer> hm = new HashMap<String, Integer>();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        int count = processes.size();
        for (int i = 0; i < count; i++) {
            RunningAppProcessInfo process = processes.get(i);
            hm.put(process.processName, process.pid);
        }
        return hm;
    }

    public static String do_exec(String[] args) {
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        java.lang.Process process = null;
        InputStream inIs = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
    // Gionee <liuyb> <2014-10-04> add for CR01406883 end

    public static String getTopActivityPackageName(Context context) {
        String pkgName = "";
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

    // Gionee <yangxinruo> <2016-5-23> add for CR01695246 begin
    public static String getCurrentTopActivityPackageName(Context context) {
        String pkgName = "";
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
    // Gionee <yangxinruo> <2016-5-23> add for CR01695246 end

    // Gionee <houjie> <2015-08-05> add for CR01532150 begin
    public static long getRandomDelayTime(int max) {
        Random random = new Random(System.currentTimeMillis());
        long time = random.nextInt(max);
        return time;
    }

    public static long getRandomDelayTime(long max) {
        Random random = new Random(System.currentTimeMillis());
        int scale = random.nextInt(1000);
        return (long) scale * max / 1000L;
    }

    public static boolean isRecentAppsOrLauncherForeground(Context context) {
        try {
            List<String> launcherList = getInstalledLauncherList(context);
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).baseActivity != null) {
                String pkgName = runningTaskInfos.get(0).baseActivity.getPackageName();
                String clsName = runningTaskInfos.get(0).baseActivity.getClassName();

                if (pkgName.equals("com.android.systemui")
                        && clsName.equals("com.android.systemui.recents.RecentsActivity")) {
                    return true;
                }

                for (String launcher : launcherList) {
                    if (pkgName.equals(launcher)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // Gionee <houjie> <2015-08-05> add for CR01532150 end

    // Gionee fengpeipei<2016-05-04> add for CR01683757 begin
    public static long translateCapacity(long capacity) {
        long result = capacity;
        if (capacity < 67108864L) {
            result = 67108864L;
        } else if (capacity < 134217728L) {
            result = 134217728L;
        } else if (capacity < 268435456L) {
            result = 268435456L;
        } else if (capacity < 536870912L) {
            result = 536870912L;
        } else if (capacity < 1073741824L) {
            result = 1073741824L;
            //Gionee <wangguojing> <2015-04-14> add for CR01464547 begin
        } else if (capacity < 1610612736L) {
            result = 1610612736L;
            //Gionee <wangguojing> <2015-04-14> add for CR01464547 end
        } else if (capacity < 2147483648L) {
            result = 2147483648L;
            //Gionee <wangguojing> <2013-11-28> add for CR00958487 begin
        } else if (capacity < 3221225472L) {
            result = 3221225472L;
            //Gionee <wangguojing> <2013-11-28> add for CR00958487 end
        } else if (capacity < 4294967296L) {
            result = 4294967296L;
        } else if (capacity < 8589934592L) {
            result = 8589934592L;
        } else if (capacity < 17179869184L) {
            result = 17179869184L;
        } else if (capacity < 32000000000L) {
            result = 34359738368L;
            // Gionee <chenml> <2015-05-27> add for CR01484375 begin
        } else if (capacity < 64000000000L) {
            result = 68719476736L;
        }
        // Gionee <chenml> <2015-05-27> add for CR01484375 end
        return result;
    }
    //Gionee fengpeipei <2016-05-04> add for CR01683757 end

    /**
     * 返回包含对象类名及其16进制哈希值的字串
     *   tips: Object默认的toString()方法还包含了包名, 但当包名太长时不利于调试
     *
     * @param o 任意非空对象
     * @return  包含对象类名及其16进制哈希值的字串
     */
    public static String toString(Object o) {
        return o.getClass().getSimpleName() + "@" + Integer.toHexString(o.hashCode());
    }
}
