/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 异常耗电行为应用检测策略:总耗电量偏高
 *
 * Revised Date: 2017-02-05
 */
package com.cydroid.softmanager.powersaver.notification.strategy;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.BatteryStats;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.Display;

import com.android.internal.os.BatterySipper;
import com.cydroid.softmanager.powersaver.analysis.AppBatteryInfo;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//Gionee <yangxinruo> <2016-02-1> modify for CR01634814 begin
public class HighVoltageConsumeMonitor extends PowerConsumeAppMonitor {
    private static final String TAG = "HighVoltageConsumeMonitor";

    private static final boolean INFO_DEBUG = false;
    public static final String UID_POWER_LIST = "voltagediffstrategy_uid_power_list";

    // Gionee <yangxinruo> <2015-09-24> modify for CR01559304 begin
    // Gionee <yangxinruo> <2015-11-17> modify for CR01592255 begin
    // private static final float ALERT_SCORE_THERSHOLD = 10.0f;// mAh
    private static final float ALERT_SCORE_THERSHOLD = 7.0f; // mAh
    // Gionee <yangxinruo> <2015-11-17> modify for CR01592255 end
    private static final long CHECK_INTERVAL = 10 * 60 * 1000; // 间隔10分钟
    // Gionee <yangxinruo> <2015-09-24> modify for CR01559304 end

    // private File mPath;
    private final Context mContext;

    private final SparseArray<Double> mPrevPowerMap = new SparseArray<Double>();
    private final SparseArray<Long> mPrevRecordDatetimeMap = new SparseArray<Long>();
    private final SparseArray<Double> mOverThresholdScoreMap = new SparseArray<Double>();

    private final SparseArray<Double> mCurrentPowerMap = new SparseArray<Double>();

    private final float mRealThreshold;
    private long mLastDatetime = -1;

    private final DisplayManager mDisplayManager;

    private static final int BATTERY_STATS_TYPE = BatteryStats.STATS_SINCE_CHARGED;

    public HighVoltageConsumeMonitor(Context context) {
        mMonitorInterval = CHECK_INTERVAL;

        mContext = context;
        // mPath = context.getFilesDir();
        mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);

        mRealThreshold = ALERT_SCORE_THERSHOLD * 3600 * 1000 / (float) CHECK_INTERVAL;
    }

    @Override
    public synchronized void init() {
        mPrevRecordDatetimeMap.clear();
        mPrevPowerMap.clear();
        mCurrentPowerMap.clear();
        mOverThresholdScoreMap.clear();
    }

    @Override
    public synchronized void dataUpdate(ArrayList<Integer> excludeList) {
        mCurrentPowerMap.clear();
        mOverThresholdScoreMap.clear();

        // Gionee <yangxinruo> <2016-5-6> modify for CR01692658 begin
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String dateStr = formatter.format(curDate);
        // Gionee <yangxinruo> <2016-5-6> modify for CR01692658 end

        long currentDatetime = SystemClock.elapsedRealtime();
        long timeDiff = -1;
        if (mLastDatetime > 0) {
            timeDiff = currentDatetime - mLastDatetime;
        }
        int currentTopPackageUid = getTopPackageUid();
        List<BatterySipper> sipperList = BatteryStatsUtils.getBatterySippers(mContext);
        Log.d(TAG, "monitor start time:" + dateStr + "s exclude:" + currentTopPackageUid
                + " lastUpdateDuration:" + timeDiff);
        if (sipperList != null) {
//            int seed = getARandomNumber(0, 4);
            for (BatterySipper sipper : sipperList) {
                /*
                if (sipper.drainType != BatterySipper.DrainType.APP || sipper.uidObj == null
                        || sipper.getUid() <= 0)
                    continue;
                */
                AppBatteryInfo appinfo = new AppBatteryInfo(sipper);
                if (appinfo.drainType != BatterySipper.DrainType.APP || appinfo.userId < 10000
                        || appinfo.userId == currentTopPackageUid) {
                    continue;
                }
                int uid = appinfo.userId;
                // Gionee <yangxinruo> <2016-7-8> add for CR01729071 begin
                if (excludeList.contains(uid)) {
                    continue;
                }
                // Gionee <yangxinruo> <2016-7-8> add for CR01729071 end
                double power = appinfo.powerValue;
                mCurrentPowerMap.put(uid, power);
                Log.d(TAG, "record new data :" + uid + " " + power);

                // Gionee <yangxinruo> <2015-09-24> add for CR01559304 begin
                long uidLastDatetime = mPrevRecordDatetimeMap.get(uid, (long) -1);
                mPrevRecordDatetimeMap.put(uid, currentDatetime);
                if (uidLastDatetime > 0) {
                    if (uidLastDatetime != mLastDatetime) {
                        Log.d(TAG, uid + " last time not in background,out of date");
                        continue;
                    }
                } else {
                    Log.d(TAG, uid + " last time not in background");
                    continue;
                }
                // Gionee <yangxinruo> <2015-09-24> add for CR01559304 end
                double voltageDiff = getVoltageConsume(uid, timeDiff);
                double currentScore = computeVoltageConsumeScore(timeDiff, voltageDiff);
                Log.d(TAG, "diff=" + timeDiff + " " + mRealThreshold + " uid " + uid
                        + " get current score=" + currentScore);
                // SmartCleanInfoWriter.writeToDataFile("高耗电检测包UID(" + uid + ")：差值 " + currentScore);
                if (currentScore >= mRealThreshold) {
                    Log.d(TAG, "----->uid=" + uid + " over limit,val=" + voltageDiff);
                    // SmartCleanInfoWriter.writeToDataFile("高耗电检测包UID(" + uid + ")：超过阈值" + mRealThreshold);
                    mOverThresholdScoreMap.put(uid, voltageDiff);
                }
//                int reSeed = getARandomNumber(0, 4);
//                if (reSeed == seed) {
//                    Log.d(TAG, "RANDOM TEST seed=" + seed + "----->uid=" + uid + " over limit,val="
//                            + voltageDiff);
//                    mOverThresholdScoreMap.put(uid, voltageDiff);
//                }
            }
        }
        mLastDatetime = currentDatetime;
        finish();
        Log.d(TAG, "RESULT " + mOverThresholdScoreMap.size() + "apps over limit");
    }

    private int getTopPackageUid() {
        Display curDisplay = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
        boolean isScreenOn = curDisplay.getState() == Display.STATE_ON;
        if (!isScreenOn) {
            return -1;
        }
        return HelperUtils.getUidByPackageName(mContext, HelperUtils.getTopActivityPackageName(mContext));
    }

    private double computeVoltageConsumeScore(long timeDiff, double diff) {
        return diff * 3600 * 1000 / (double) timeDiff;
    }

//    @Override
//    public double getScore(int uid) {
//        return mOverThresholdScoreMap.get(uid, (double) -1);
//    }

    private double getVoltageConsume(int uid, long timeDiff) {
        Double newScore = mCurrentPowerMap.get(uid);
        if (newScore == null) {
            Log.d(TAG, "uid " + uid + " data not ready return 0");
            return 0;
        }
        double oldScore = mPrevPowerMap.get(uid, -1d);
        if (oldScore < 0) {
            Log.d(TAG, "uid " + uid + " not in oldUidStats return 0");
            return 0;
        }
        Log.d(TAG, "power old " + oldScore + " power new " + newScore);
        return newScore - oldScore;
    }

    private void finish() {
        mPrevPowerMap.clear();
        if (mCurrentPowerMap.size() <= 0) {
            return;
        }
        for (int i = 0; i < mCurrentPowerMap.size(); i++) {
            int uid = mCurrentPowerMap.keyAt(i);
            Double value = mCurrentPowerMap.get(uid);
            if (INFO_DEBUG) {
                Log.d(TAG, "save last power uid:" + uid + " val:" + value);
            }
            mPrevPowerMap.put(uid, value);
        }
        Log.d(TAG, "save last power finished commit");
        mCurrentPowerMap.clear();
    }

    @Override
    public synchronized HashMap<String, Double> getOverThresholdData() {
        HashMap<String, Double> result = new HashMap<String, Double>();
        for (int i = 0; i < mOverThresholdScoreMap.size(); i++) {
            int uid = mOverThresholdScoreMap.keyAt(i);
            Double value = mOverThresholdScoreMap.get(uid);
            String pkgName = getPackageNameByUid(uid);
            if (!pkgName.isEmpty()) {
                result.put(pkgName, value);
            }
        }
        return result;
    }

    private String getPackageNameByUid(int uid) {
        String[] packageNames = HelperUtils.getPkgNameByUid(mContext, uid);
        if (packageNames == null) {
            return "";
        }
        return packageNames[0];
    }

//    private int getARandomNumber(int min, int max) {
//        Random random = new Random();
//        int s = random.nextInt(max) % (max - min + 1) + min;
//        return s;
//    }
}
//Gionee <yangxinruo> <2016-02-1> modify for CR01634814 end
