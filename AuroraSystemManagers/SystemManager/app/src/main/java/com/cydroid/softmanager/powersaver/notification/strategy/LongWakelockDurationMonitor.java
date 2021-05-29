/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 异常耗电行为应用检测策略:长时间持有Partial Wakelock
 *
 * Revised Date: 2017-02-05
 */
//Gionee <yangxinruo> <2016-02-1> add for CR01634814 add
package com.cydroid.softmanager.powersaver.notification.strategy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class LongWakelockDurationMonitor extends PowerConsumeAppMonitor {
    private static final String TAG = "LongWakelockDurationMonitor";

    private static final boolean INFO_DEBUG = false;
    public static final String UID_POWER_LIST = "wakelockdiffstrategy_uid_power_list";

    private static final long ALERT_SCORE_THERSHOLD = 10 * 60 * 1000; // ms
    private static final long CHECK_INTERVAL = 30 * 60 * 1000; // 间隔30分钟

    private final Context mContext;

    private final SparseArray<Long> mPrevWakelockMap = new SparseArray<Long>();
    private final SparseArray<Long> mPrevRecordDatetimeMap = new SparseArray<Long>();
    private final SparseArray<Long> mOverThresholdScoreMap = new SparseArray<Long>();

    private final SparseArray<Long> mCurrentWakelockMap = new SparseArray<Long>();

    private final float mUnitThreshold;
    private long mLastDatetime = -1;

    private int mTopUid = -1;
//    private int mJustScreenOff = 0;

    // private DisplayManager mDisplayManager;

    private static final int BATTERY_STATS_TYPE = BatteryStats.STATS_SINCE_CHARGED;

    public LongWakelockDurationMonitor(Context context) {
        mMonitorInterval = CHECK_INTERVAL;

        mContext = context;
        // mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);

        mUnitThreshold = (float) ALERT_SCORE_THERSHOLD / (float) CHECK_INTERVAL;

        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    return;
                }
                String actionStr = intent.getAction();
                //Log.d(TAG, "ScreenStateReceiver onReceive action:" + actionStr);
                if (actionStr.equals(Intent.ACTION_SCREEN_OFF)) {
                    Log.d(TAG, "screen off ,monitor activate");
                    mPrevRecordDatetimeMap.clear();
//                    mJustScreenOff = 0;
                    activate();
                } else if (actionStr.equals(Intent.ACTION_SCREEN_ON)) {
                    Log.d(TAG, "screen on ,monitor deactivate");
                    deactivate();
                }
                //Log.d(TAG, "ScreenStateReceiver onReceive action:" + actionStr + " process finished");
            }
        }, screenFilter);
    }

    @Override
    public boolean activate() {
        if (isScreenOn()) {
            Log.d(TAG, "screen on now ,can not be active");
            return false;
        }
        return super.activate();
    }

    private boolean isScreenOn() {
        DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display curDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
        return curDisplay.getState() == Display.STATE_ON;
    }

    @Override
    public synchronized void init() {
        mPrevRecordDatetimeMap.clear();
        mPrevWakelockMap.clear();
        mCurrentWakelockMap.clear();
        mOverThresholdScoreMap.clear();
    }

    @Override
    public synchronized void dataUpdate(ArrayList<Integer> excludeList) {
        mCurrentWakelockMap.clear();
//        if (mJustScreenOff > 0) {
//            Log.d(TAG, "2 times after screenoff clean ,clean old result data");
//            mOverThresholdScoreMap.clear();
//        }
        mTopUid = -1;

        // Gionee <yangxinruo> <2016-5-6> modify for CR01692658 begin
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String dateStr = formatter.format(curDate);
        List<BatterySipper> sipperList = BatteryStatsUtils.getBatterySippers(mContext);
        Log.d(TAG, "StrategieThread start time:" + dateStr);
        // Gionee <yangxinruo> <2016-5-6> modify for CR01692658 end

        long currentDatetime = SystemClock.elapsedRealtime();
        long timeDiff = -1;
        if (mLastDatetime > 0) {
            timeDiff = currentDatetime - mLastDatetime;
        }
//        HashSet<Integer> excludeSet = getExcludeList();

        if (sipperList != null) {
//            int seed = getARandomNumber(0, sipperList.size() - 1);
            for (BatterySipper sipper : sipperList) {
                AppBatteryInfo appinfo = new AppBatteryInfo(sipper);
                if (appinfo.drainType != BatterySipper.DrainType.APP || appinfo.userId < 10000) {
                    continue;
                }
                int uid = appinfo.userId;
                // Gionee <yangxinruo> <2016-7-8> add for CR01729071 begin
                if (excludeList.contains(uid)) {
                    continue;
                }
                // Gionee <yangxinruo> <2016-7-8> add for CR01729071 end
//                if (excludeSet.contains(uid)) {
//                    Log.d(TAG, "uid=" + uid + "in exclude list ,pass");
//                    continue;
//                }
                long wakelockTime = appinfo.wakeLockTime;
                mCurrentWakelockMap.put(uid, wakelockTime);
                /*guoxt modify to reduce log output begin*/
                if(INFO_DEBUG) {
                    Log.d(TAG, "record new data :uid=" + uid + " value=" + wakelockTime);
                }
                 /*guoxt modify to reduce log output end*/

                long uidLastDatetime = mPrevRecordDatetimeMap.get(uid, (long) -1);
                mPrevRecordDatetimeMap.put(uid, currentDatetime);
                if (uidLastDatetime > 0) {
                    if (uidLastDatetime != mLastDatetime) {
                        /*guoxt modify to reduce log output begin*/
                        if(INFO_DEBUG) {
                            Log.d(TAG, "uid=" + uid + " last time not in background,out of date");
                        }
                        /*guoxt modify to reduce log output end*/
                        continue;
                    }
                } else {
                    /*guoxt modify to reduce log output begin*/
                    if(INFO_DEBUG) {
                        Log.d(TAG, "uid=" + uid + " last time not in background,new data");
                    }
                    /*guoxt modify to reduce log output end*/
                    continue;
                }
                // Gionee <yangxinruo> <2015-09-24> add for CR01559304 end
                long diff = getWakelockDurationDiff(uid, timeDiff);
                float unitTimeScore = computeLongWakelockScore(timeDiff, diff);
                Log.d(TAG, "monitor time diff=" + timeDiff + " unit threshold=" + mUnitThreshold + " uid="
                        + uid + " get current score=" + unitTimeScore);
                // SmartCleanInfoWriter.writeToDataFile("组织系统休眠检测包UID(" + uid + ")：差值 " + unitTimeScore);
                if (unitTimeScore >= mUnitThreshold) {
                    Log.d(TAG, "----->uid=" + uid + " over limit val=" + diff);
                    // SmartCleanInfoWriter.writeToDataFile("组织系统休眠检测包UID(" + uid + ")：超过阈值" +
                    // mUnitThreshold);
                    mOverThresholdScoreMap.put(uid, diff);
                }
//                int reSeed = getARandomNumber(0, 4);
//                if (reSeed == seed) {
//                    Log.d(TAG, "RANDOM TEST seed=" + seed + "----->uid=" + uid + " over limit,val=" + diff);
//                    mOverThresholdScoreMap.put(uid, diff);
//                }
            }
        }
        mLastDatetime = currentDatetime;
        finish();
        Log.d(TAG, "RESULT " + mOverThresholdScoreMap.size() + "apps over limit");

//        if (mJustScreenOff < 2) {
//            mJustScreenOff++;
//        }
    }

    private float computeLongWakelockScore(long timeDiff, long diff) {
        // cast to float for more precisely
        return (float) diff / (float) timeDiff;
    }

//    @Override
//    public double getScore(int uid) {
//        if (mJustScreenOff < 2) {
//            Log.d(TAG, "just screen do not report");
//            return -1;
//        }
//        return mOverThresholdScoreMap.get(uid, (long) -1);
//    }

    private long getWakelockDurationDiff(int uid, long timeDiff) {
        Long newScore = mCurrentWakelockMap.get(uid);
        if (newScore == null) {
            Log.d(TAG, "uid=" + uid + " data not ready return 0");
            return 0;
        }
        long oldScore = mPrevWakelockMap.get(uid, (long) -1);
        if (oldScore < 0) {
            Log.d(TAG, "uid=" + uid + " not in oldUidStats return 0");
            return 0;
        }
        Log.d(TAG, "uid=" + uid + " old value=" + oldScore + " new value=" + newScore);
        long diff = newScore - oldScore;
        return diff;
    }

    private void finish() {
        mPrevWakelockMap.clear();
        if (this.mCurrentWakelockMap.size() > 0) {
            for (int i = 0; i < mCurrentWakelockMap.size(); i++) {
                int key = mCurrentWakelockMap.keyAt(i);
                Long value = mCurrentWakelockMap.get(key);
                if (INFO_DEBUG) {
                    Log.d(TAG, "save last wakelocktime uid:" + key + " val:" + value);
                }
                mPrevWakelockMap.put(key, value);
            }
        }
        Log.d(TAG, "save last power finished commit");
        mCurrentWakelockMap.clear();
    }

    @Override
    public long getUpdateInterval() {
        return mMonitorInterval;
    }

    @Override
    public synchronized HashMap<String, Double> getOverThresholdData() {
        HashMap<String, Double> result = new HashMap<String, Double>();
//        if (mJustScreenOff < 2) {
//            Log.d(TAG, "just screen do not report");
//            return result;
//        }
        for (int i = 0; i < mOverThresholdScoreMap.size(); i++) {
            int uid = mOverThresholdScoreMap.keyAt(i);
            Long value = mOverThresholdScoreMap.get(uid);
            String pkgName = getPackageNameByUid(uid);
            if (!pkgName.isEmpty()) {
                result.put(pkgName, Double.valueOf(value));
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
//Gionee <yangxinruo> <2016-02-1> add for CR01634814 end
