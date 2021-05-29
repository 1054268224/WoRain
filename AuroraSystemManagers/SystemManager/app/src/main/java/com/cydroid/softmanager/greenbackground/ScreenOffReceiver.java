package com.cydroid.softmanager.greenbackground;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.view.inputmethod.InputMethodInfo;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ScreenOffReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenOffReceiver";

    private static final HashSet<String> mAlarmApps = new HashSet<>();

    static {
        mAlarmApps.add("com.android.calendar");
    }

    private static final HashSet<String> mWallpaperApps = new HashSet<>();

    static {
        mWallpaperApps.add("com.hzxwkj.myshare.sdk");
        mWallpaperApps.add("com.cydroid.change.engine.vlife");
        mWallpaperApps.add("com.dwallpaper.Jellyfish");
        mWallpaperApps.add("com.dwallpaper.Nebula");
        mWallpaperApps.add("com.dwallpaper.Crystal");
        mWallpaperApps.add("com.dwallpaper.Hellocity");
        mWallpaperApps.add("com.dwallpaper.Polygon");
    }

    private WhiteListManager mWhiteListManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        mWhiteListManager = WhiteListManager.getInstance();
        mWhiteListManager.init(context);
        String actionStr = intent.getAction();
        if (!mWhiteListManager.isGreenBackgroundEnable()
                || !Consts.ACTION_GREEN_BACKGROUND_CLEAN.equals(actionStr)) {
            return;
        }

        // Gionee <liuyb> <2014-10-04> add for CR01406883 begin
        Util.killSpecialProcess(context);
        // Gionee <liuyb> <2014-10-04> add for CR01406883 end
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

        List<String> thirdApps = getThirdApps(context);
        String launcher = Util.getDefaultLauncherPkg(context);
        String inputMethod = getInputMethod(context);
        List<String> musicApps = getMusicApps(context);
        List<String> fmApps = getFmApps(context);
        String liveWallPaper = getWallPaper(context);
        String topAppName = Util.getTopActivityPackageName(context);
        String currentTopAppName = Util.getCurrentTopActivityPackageName(context);
        Log.d(TAG,
                "except apps :" + "launcher:" + launcher + " input:" + inputMethod + " livewallpaper:"
                        + liveWallPaper + " top:" + topAppName + " currentTop:" + currentTopAppName);

        for (RunningAppProcessInfo runinfo : runningAppProcesses) {
            for (String pkgName : runinfo.pkgList) {
                if (thirdApps.contains(pkgName)) {
                    if ("com.cydroid.softmanager".equalsIgnoreCase(pkgName)
                            || launcher.equalsIgnoreCase(pkgName)
                            || inputMethod.equalsIgnoreCase(pkgName)
                            || musicApps.contains(pkgName)
                            || fmApps.contains(pkgName)
                            || topAppName.equals(pkgName)
                            || currentTopAppName.equals(pkgName)
                            || liveWallPaper.equals(pkgName)
                            || mWallpaperApps.contains(pkgName)) {
                        continue;
                    }

                    if (!mWhiteListManager.isInUserWhiteApps(pkgName)) {
                        forceStopPackage(am, pkgName);
                    }
                }
            }
        }
    }

    private List<String> getThirdApps(Context context) {
        List<String> thirdApps = new ArrayList<>();
        List<ApplicationInfo> applications = SoftHelperUtils.getThirdApplicationInfo(context);
        for (ApplicationInfo info : applications) {
            thirdApps.add(info.packageName);
        }
        return thirdApps;
    }

    private String getInputMethod(Context context) {
        String inputMethod = "";
        InputMethodInfo input = HelperUtils.getDefInputMethod(context);
        if (input != null) {
            inputMethod = input.getPackageName();
        }
        return inputMethod;
    }

    private List<String> getMusicApps(Context context) {
        List<String> musicApps = new ArrayList<>();
        if (Util.isPlayMusic(context)) {
            musicApps = Util.getMusicApps(context);
        }
        return musicApps;
    }

    private List<String> getFmApps(Context context) {
        List<String> fmApps = new ArrayList<>();
        if (Util.isFmOn(context)) {
            fmApps = Util.getFMApps(context);
        }
        return fmApps;
    }

    private String getWallPaper(Context context) {
        String liveWallPaper = HelperUtils.getLivePaperPkgName(context);
        if (liveWallPaper == null)
            liveWallPaper = "";
        return liveWallPaper;
    }

    private void forceStopPackage(ActivityManager am, String packageName) {
        Log.d(TAG, "forceStopPackage packageName:" + packageName);
        if (!mAlarmApps.contains(packageName)) {
            am.forceStopPackage(packageName);
        }
    }
}
