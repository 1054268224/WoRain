// Gionee <yangxinruo> <2015-12-11> modify for CR01608017 begin
// Gionee <houjie> <2015-08-19> add for CR01559020 begin
package com.cydroid.softmanager.monitor.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.inputmethod.InputMethodInfo;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.oneclean.RunningAppListActivity;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.monitor.utils.ProcessCpuTracker;
import com.cydroid.softmanager.monitor.utils.RamAndMemoryUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cydroid.softmanager.R;
import com.chenyee.featureoption.ServiceUtil;
public class CpuRamMonitorService extends Service {
    private final static String TAG = "CpuRamMonitorService";
    /*guoxt modify for SW17W16A-2534 begin */
    private final static boolean DEBUG_POLL = false;
    /*guoxt modify for SW17W16A-2534 end */

    // Gionee <yangxinruo><2016-2-26> modify for CR01640160 begin
    private final static float MAX_RUNNING_PROCESS_ONECLEAN_TRIGGER = 4.0f;// per cpu
    // Gionee <yangxinruo><2016-2-26> modify for CR01640160 end
    private final static double MAX_USED_RAM_RATIO_ONECLEAN_TRIGGER = 0.90;
    // Gionee <yangxinruo><2015-12-21> modify for CR01610272 begin
    private final static long DEFAULT_AVAIL_RAM_ONECLEAN_TRIGGER = 200;// Mb
    // Gionee <yangxinruo><2015-12-21> modify for CR01610272 end

    /*guoxt modify for SW17W16A-2534 begin */
    private final static long POLL_INTERVAL = 10000;
    /*guoxt modify for SW17W16A-2534 end */

    private final static int NOTIFICATION_CPU_COUNTER_THRESHOLD = 3;
    private final static int NOTIFICATION_MEM_COUNTER_THRESHOLD = 3;
    private final static int NOTIFICATION_CANCEL_COUNTER_THRESHOLD = 2;

    // private final static long COOL_DOWN_TIME = 5 * 60 * 1000;
    private final static long NOTI_COOL_DOWN_TIME = 0;
    private final static long BOOT_DELAY_TIME = 6 * 60 * 1000;

    private final static int MSG_CPU_MEM_POLL = 1;
    private final static int MSG_NOTI_COOL_DOWN = 2;

    private Context mContext;
    private NotificationManager mNotificationManager;

    private CpuTracker mStats;

    private ScreenStateReceiver mScreenStateReceiver;
    private int mStopService = -1;

    private String mTopPackage = "";

    // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
    private int mCanShowHeadup = 0;
    private boolean mIsNotiRemoved = false;
    private boolean mNotiShowed = false;
    // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end

    private int mCheckUnderThresholdCounter = 0;

    private int mCpuOverloadCount = 0;
    private int mMemOverloadCount = 0;

    private final boolean mCanRunning = false;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CPU_MEM_POLL) {
//                Log.d(TAG, "handleMessage ---msg=MSG_CPU_MEM_POLL");
//                mStats.update();
//                cpuMemOverloadProcess();
//                Message m = obtainMessage(MSG_CPU_MEM_POLL);
//                sendMessageDelayed(m, POLL_INTERVAL);
            } else if (msg.what == MSG_NOTI_COOL_DOWN) {
                // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
                mCanShowHeadup++;
                // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
            }
        }
    };
    // Gionee <mengjk><2017-6-30> add for CR159259 begin
    private boolean isCpuMemOverLoadProcessRunning = true;
    private boolean isExecuteCpuMemOverLoadProcess = true;
    private CpuMemOverLoadThread mCpuMemOverLoadThread;

    private class CpuMemOverLoadThread extends Thread {
        public void run() {
            Log.d(TAG, "BEGIN CpuMemOverLoadThread:");
            setName("CpuMemOverLoadThread");
            while (isCpuMemOverLoadProcessRunning) {
                if (isExecuteCpuMemOverLoadProcess) {
                    mHandler.sendEmptyMessage(MSG_CPU_MEM_POLL);
                    mStats.update();
                    cpuMemOverloadProcess();
                }
                try {
                    Thread.sleep(POLL_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (CpuRamMonitorService.this) {
                mCpuMemOverLoadThread = null;
            }
        }

        public void cancel() {
            isCpuMemOverLoadProcessRunning = false;
        }
    }
    // Gionee <mengjk><2017-6-30> add for CR159259 end

    // Gionee <yangxinruo><2015-12-21> add for CR01610272 begin
    private long mMinAvailMemTrigger = 0;
    // Gionee <yangxinruo><2016-2-26> add for CR01640160 begin
    private int mCpuNum = 1;
    // Gionee <yangxinruo><2016-2-26> add for CR01640160 end

    // Gionee <yangxinruo><2015-12-21> add for CR01610272 end
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mContext = getApplicationContext();

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mScreenStateReceiver = new ScreenStateReceiver(mContext);
        mStats = new CpuTracker();
        mStats.init();
        // Gionee <yangxinruo><2016-2-26> add for CR01640160 begin
        mCpuNum = HelperUtils.getCpuCoreNums();
        // Gionee <yangxinruo><2016-2-26> add for CR01640160 end
        // cpuMemOverloadProcess();
        // Gionee <yangxinruo><2015-12-21> add for CR01610272 begin
        // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
        long tmpMinAvailMemTrigger = mMinAvailMemTrigger = getMaxOomLevel() / 1024;
        // Gionee <yangxinruo><2016-5-18> modify for CR01702617 begin
        if (isLmkThresholdInvlid(tmpMinAvailMemTrigger))
            mMinAvailMemTrigger = DEFAULT_AVAIL_RAM_ONECLEAN_TRIGGER;
        Log.d(TAG, "cpunum=" + mCpuNum + " use minTrigger=" + mMinAvailMemTrigger + " oom="
                + tmpMinAvailMemTrigger);
        // Gionee <yangxinruo><2016-5-18> modify for CR01702617 end
        // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
        // Gionee <yangxinruo><2015-12-21> add for CR01610272 end
        final PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (pm.isInteractive()) {
            startPolling();
        }
    }

    private boolean isLmkThresholdInvlid(long tmpMinAvailMemTrigger) {
        return tmpMinAvailMemTrigger <= 0 || tmpMinAvailMemTrigger > DEFAULT_AVAIL_RAM_ONECLEAN_TRIGGER;
    }

    // Gionee <yangxinruo><2015-12-21> add for CR01610272 begin
    private long getMaxOomLevel() {
        int read;
        StringBuffer output = new StringBuffer();
        String resultStr = "";
        java.lang.Process process = null;
        try {
            // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
            process = Runtime.getRuntime().exec(new String[] {"sh", "-c",
                    "dumpsys activity o | grep -E 'CACHED_APP_MAX_ADJ|CACHED_APP_MIN_ADJ'"});
            // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
            reader.close();

            int suProcessRetval = process.waitFor();
            if (255 != suProcessRetval) {
                resultStr = output.toString();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error executing dumpsys sensorservice", ex);
            return 0;
        } finally {
            if (process != null)
                process.destroy();
        }

        if (!"".equals(resultStr)) {
            Log.e(TAG, "ams dump Result" + resultStr);
            String rex = "\\((\\d+)\\skB\\)";
            Pattern patterndiff = Pattern.compile(rex);
            Matcher matcher = patterndiff.matcher(resultStr);
            // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
            long[] oomThreshold = {0, 0};
            int count = 0;
            while (matcher.find() && count < 2) {
                try {
                    Log.d(TAG, " get match count=" + matcher.groupCount() + " str=" + matcher.group(1));
                    oomThreshold[count] = Long.parseLong(matcher.group(1));
                    Log.d(TAG, "get oom = " + oomThreshold[count]);
                    count++;
                    // return oomMax / 1024;
                } catch (Exception e) {
                    Log.e(TAG, "Error format number", e);
                    // return 0;
                }
            }
            if (count < 2)
                return oomThreshold[0] + 30 * 1024;
            else {
                return Math.min(oomThreshold[0], oomThreshold[1])
                        + Math.abs(oomThreshold[1] - oomThreshold[0]) / 10 * 3;
            }
            // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
        } else
            return 0;
    }

    // Gionee <yangxinruo><2015-12-21> add for CR01610272 end
    private void startPolling() {
        long bootDiffTime = BOOT_DELAY_TIME - SystemClock.elapsedRealtime();
        // Gionee <mengjk><2017-6-30> add for CR159259 begin
        if (mCpuMemOverLoadThread == null) {
            mCpuMemOverLoadThread = new CpuMemOverLoadThread();
            isCpuMemOverLoadProcessRunning = true;
            mCpuMemOverLoadThread.start();
        }
        // Gionee <mengjk><2017-6-30> modify for CR159259 end
        if (bootDiffTime > 0) {
            Log.d(TAG, "enter startPolling delay " + bootDiffTime);
            // Gionee <mengjk><2017-6-30> add for CR159259 begin
            isExecuteCpuMemOverLoadProcess = false;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExecuteCpuMemOverLoadProcess = true;
                }
            }, bootDiffTime);
//            mHandler.removeMessages(MSG_CPU_MEM_POLL);
//            mHandler.sendEmptyMessageDelayed(MSG_CPU_MEM_POLL, bootDiffTime);
        } else {
            Log.d(TAG, "enter startPolling now ");
            isExecuteCpuMemOverLoadProcess = true;
//            mHandler.sendEmptyMessage(MSG_CPU_MEM_POLL);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        // Gionee <mengjk><2017-6-30> modify for CR159259 begin
        if (mCpuMemOverLoadThread != null) {
            mCpuMemOverLoadThread.cancel();
        }
//        mHandler.removeMessages(MSG_CPU_MEM_POLL);
//        mHandler.removeMessages(MSG_NOTI_COOL_DOWN);
        // Gionee <mengjk><2017-6-30> modify for CR159259 end
        mContext.unregisterReceiver(mScreenStateReceiver);
        super.onDestroy();
    }

    private void cpuMemOverloadProcess() {
        if (mStopService < 0) {
            mStopService = initStopFlag();
            Log.d(TAG, "init stop state = " + mStopService);
        }
        if (mStopService == 1) {
            Log.d(TAG, "switch off ,stop service");
            stopSelf();
            return;
        }

        final CpuTracker stats = mStats;
        final String loadAverageStringData = stats.mLoadAverageStringData;

        String[] data = loadAverageStringData.split("/");
        int runningProcess = Integer.parseInt(data[0]);
        // Gionee <yangxinruo><2016-2-26> add for CR01640160 begin
        float perCoreRunningProcess = (float) runningProcess / (float) mCpuNum;
        // Gionee <yangxinruo><2016-2-26> add for CR01640160 end
        //long availMem = RamAndMemoryUtils.getAvailMem();
        long availMem = RamAndMemoryUtils.getAvailMemRead();
        if (DEBUG_POLL) {
            double usedRamRatio = RamAndMemoryUtils.getRatioUsedMem();
            Log.i(TAG,
                    "cpuMemOverloadProcess loadAverageStringData:" + loadAverageStringData + ", usedRamRatio:"
                            + usedRamRatio + ", availMem:" + availMem + "Mb, perCoreRunningProcess:"
                            + perCoreRunningProcess);
        }
        // Gionee xionghg 2017-06-07 modify for 151489 begin
        float realThreshold = MAX_RUNNING_PROCESS_ONECLEAN_TRIGGER;
        if (mCpuNum <= 4) {
            realThreshold += 2.0;
        }
        if (perCoreRunningProcess > realThreshold) {
        // Gionee xionghg 2017-06-07 modify for 151489 end
            double usedRamRatio = RamAndMemoryUtils.getRatioUsedMem();
            Log.i(TAG,
                    "cpuMemOverloadProcess CPU loadAverageStringData:" + loadAverageStringData
                            + ", usedRamRatio:" + usedRamRatio + ", availMem:" + availMem
                            + "Mb, perCoreRunningProcess:" + perCoreRunningProcess);
            if (mCpuOverloadCount < NOTIFICATION_CPU_COUNTER_THRESHOLD)
                mCpuOverloadCount++;
            showOverloadNotification(Consts.CPU_OVERLOAD);
        } else {
            mCpuOverloadCount = 0;
        }
        // Gionee <yangxinruo><2015-12-21> modify for CR01610272 begin
        if (availMem <= mMinAvailMemTrigger) {
            // Log.d(TAG, "MEM_OVERLOAD");
            // mOneCleanUtil.oneCleanByCpuMemOverload(Consts.MEM_OVERLOAD, null);
            double usedRamRatio = RamAndMemoryUtils.getRatioUsedMem();
            Log.d(TAG,
                    "cpuMemOverloadProcess MEM loadAverageStringData:" + loadAverageStringData
                            + ", usedRamRatio:" + usedRamRatio + ", availMem:" + availMem
                            + "Mb, perCoreRunningProcess:" + perCoreRunningProcess);
            if (mMemOverloadCount < NOTIFICATION_MEM_COUNTER_THRESHOLD)
                mMemOverloadCount++;
            showOverloadNotification(Consts.MEM_OVERLOAD);
        } else {
            mMemOverloadCount = 0;
        }
        // Gionee <yangxinruo><2015-12-21> modify for CR01610272 end
        // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
        if ((mCpuOverloadCount + mMemOverloadCount) == 0 && mNotiShowed) {
            Log.d(TAG, "Mem&CPU stats check OK.can cancel noti? counter=" + mCheckUnderThresholdCounter);
            if (mCheckUnderThresholdCounter >= NOTIFICATION_CANCEL_COUNTER_THRESHOLD - 1) {
                mNotificationManager.cancel(R.string.cpu_monitor_headup_title);
                afterRemoveNotification();
            } else {
                mCheckUnderThresholdCounter++;
            }
        }
        // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
    }

    private int initStopFlag() {
        // Gionee <yangxinruo><2016-4-5> add for CR01691255 begin
        // for CpuRamMonitorService killed by exception or start by notification when switch off
        MainProcessSettingsProviderHelper settingsHelper = new MainProcessSettingsProviderHelper(mContext);
        if (settingsHelper.getBoolean("cpu_overload_monitor_key", true)) {
            return 0;
        } else {
            return 1;
        }
        // Gionee <yangxinruo><2016-4-5> add for CR01691255 end
    }

    private String getDefaultInputMethodInfo() {
        String defaultInputMethodPkgName = "";
        InputMethodInfo info = HelperUtils.getDefInputMethod(mContext);
        if (info != null) {
            defaultInputMethodPkgName = info.getPackageName();
        }
        return defaultInputMethodPkgName;
    }

    private void showOverloadNotification(int cpuOverload) {
        if (cpuOverload == Consts.CPU_OVERLOAD) {
            Log.d(TAG, "enter showOverloadNotification,because of CPU_OVERLOAD " + mCpuOverloadCount);
        } else if (cpuOverload == Consts.MEM_OVERLOAD) {
            Log.d(TAG, "enter showOverloadNotification,because of MEM_OVERLOAD " + mMemOverloadCount);
        }
        String currentTopPkg = "";
        String currentTopActivity = "";
        List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
        try {
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).baseActivity != null) {
                currentTopPkg = runningTaskInfos.get(0).baseActivity.getPackageName();
                currentTopActivity = runningTaskInfos.get(0).topActivity.flattenToString();
            }
        } catch (Exception e) {
            Log.d(TAG, "get top app error " + e);
        }
        if (!currentTopActivity
                .equals("com.cydroid.softmanager/com.cydroid.softmanager.oneclean.RunningAppListActivity")
                && !currentTopPkg.equals(mTopPackage)) {
            mTopPackage = currentTopPkg;
            if (mIsNotiRemoved) {
                // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
                Log.d(TAG, "scene changed ,reset counter");
                mIsNotiRemoved = false;
                mCanShowHeadup++;
                // Gionee <yangxinruo><2015-12-16> add for CR01610272 begin
                mCpuOverloadCount = 0;
                mMemOverloadCount = 0;
                if (cpuOverload == Consts.CPU_OVERLOAD) {
                    mCpuOverloadCount++;
                } else if (cpuOverload == Consts.MEM_OVERLOAD) {
                    mMemOverloadCount++;
                }
                // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
                // Gionee <yangxinruo><2015-12-16> add for CR01610272 end
            }
            /*
            if (!(cpuOverload == Consts.CPU_OVERLOAD && NOTIFICATION_CPU_DELAY_PERIOD == 0)
                    && !(cpuOverload == Consts.MEM_OVERLOAD && NOTIFICATION_MEM_DELAY_PERIOD == 0)) {
            Log.d(TAG, "threshold not 0 skip,cancel ");
            return;
            */
            // }
        }
        if (mNotiShowed) {
            Log.d(TAG, "notification showed,cancel");
            return;
        }
        if (mCpuOverloadCount < NOTIFICATION_CPU_COUNTER_THRESHOLD
                && mMemOverloadCount < NOTIFICATION_MEM_COUNTER_THRESHOLD) {
            Log.d(TAG, "not reach to the threshold,cancel cpu_counter:" + mCpuOverloadCount + " mem_counter:"
                    + mMemOverloadCount);
            return;
        }
        if (mCanShowHeadup < 0) {
            Log.d(TAG, "old notification showed at same activity or cooling down,cancel");
            return;
        }

        Intent processIntent = new Intent(mContext, RunningAppListActivity.class);
        processIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        ArrayList<String> excludePkgs = new ArrayList<String>();
        String launcherPkg = Util.getDefaultLauncherPkg(mContext);
        String inputPackage = getDefaultInputMethodInfo();
        excludePkgs.add(launcherPkg);
        excludePkgs.add(inputPackage);
        Log.d(TAG, "exclude packages are " + mTopPackage + " " + launcherPkg + " " + inputPackage);
        processIntent.putStringArrayListExtra("packages", excludePkgs);
        processIntent.putExtra("noti", R.string.cpu_monitor_headup_title);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, processIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent cpuMonitorIntent = new Intent(mContext, CpuRamMonitorService.class);
        cpuMonitorIntent.putExtra("event", "headupremoved");
        PendingIntent deleteIntent = PendingIntent.getService(mContext, 2, cpuMonitorIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent fakeIntent = new Intent(mContext, CpuRamMonitorService.class);
        PendingIntent fakePendingIntent = PendingIntent.getService(mContext, 0, fakeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Resources resManager = mContext.getResources();

        Notification.Builder notification = new Notification.Builder(mContext).setOngoing(true)
                .setDeleteIntent(deleteIntent).setVisibility(Notification.VISIBILITY_PUBLIC)
                // Gionee: mengjk modify for notify TitleIcon And TitleText Color Change
                .setSmallIcon(R.drawable.notify).setColor(resManager.getColor(R.color.notify_icon_text_color))
                .setFullScreenIntent(fakePendingIntent, true)
                .setContentTitle(resManager.getString(R.string.cpu_monitor_headup_title))
                .setContentText(resManager.getString(R.string.cpu_monitor_headup_summary))
                .addAction(0, resManager.getString(R.string.cpu_monitor_headup_to_clean), pendingIntent);
        Notification noti = notification.build();
        Log.d(TAG, "finally show notification because " + (cpuOverload == Consts.CPU_OVERLOAD ? "cpu" : "mem")
                + " count=" + (cpuOverload == Consts.CPU_OVERLOAD ? mCpuOverloadCount : mMemOverloadCount));
        // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
        mNotificationManager.notify(R.string.cpu_monitor_headup_title, noti);
        mCanShowHeadup = -2;
        mNotiShowed = true;
        mCheckUnderThresholdCounter = 0;
        // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
    }

    class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SHUTDOWN);
            context.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            Log.d(TAG, "ScreenStateReceiver onReceive action:" + actionStr);
            if (Intent.ACTION_SCREEN_ON.equals(actionStr)) {
                startPolling();
            } else if (Intent.ACTION_SCREEN_OFF.equals(actionStr)) {
                // Gionee <mengjk><2017-6-30> add for CR159259 begin
                if (mCpuMemOverLoadThread != null) {
                    mCpuMemOverLoadThread.cancel();
                }
//                mHandler.removeMessages(MSG_CPU_MEM_POLL);
//                mHandler.removeMessages(MSG_NOTI_COOL_DOWN);
                // Gionee <mengjk><2017-6-30> modify for CR159259 end
            } else if (Intent.ACTION_SHUTDOWN.equals(actionStr)) {
                Log.d(TAG, "system shutdown service stop");
                stopSelf();
            }
            // Log.d(TAG, "ScreenStateReceiver onReceive action:" + actionStr + " process finished");
        }
    }

    private void afterRemoveNotification() {
        // mCanShowHeadup = true;
        Log.d(TAG, "afterRemoveNotification noti removed,reset flag, cooldown");
        // Gionee <yangxinruo><2016-1-7> modify for CR01619670 begin
        mIsNotiRemoved = true;
        mNotiShowed = false;
        mHandler.sendEmptyMessageDelayed(MSG_NOTI_COOL_DOWN, NOTI_COOL_DOWN_TIME);
        // Gionee <yangxinruo><2016-1-7> modify for CR01619670 end
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            ServiceUtil.handleStartForegroundServices(this);
            String eventStr = intent.getStringExtra("event");
            if (eventStr != null) {
                String[] events = eventStr.split("-");
                for (String event : events) {
                    Log.d(TAG, "onStartCommand event--->" + event);
                    if (event.equals("stop"))
                        mStopService = 1;
                    else if (event.equals("start"))
                        mStopService = 0;
                    else if (event.equals("headupremoved")) {
                        afterRemoveNotification();
                    }
                }
            }
        }
        return START_STICKY;
    }

    private static final class CpuTracker extends ProcessCpuTracker {
        // float mLoad1;
        // float mLoad5;
        // float mLoad15;
        String mLoadAverageStringData = "0";

        // Gionee <houjie> <2016-1-6> modify for CR01618420 begin
        CpuTracker() {
            super();
        }

        // Gionee <houjie> <2016-1-6> modify for CR01618420 end

        @Override
        public void onLoadChanged(float load1, float load5, float load15, String loadAverageStringData) {
            // mLoad1 = load1;
            // mLoad5 = load5;
            // mLoad15 = load15;
            mLoadAverageStringData = loadAverageStringData;
        }
    }
}
// Gionee <houjie> <2015-08-19> add for CR01559020 end
// Gionee <yangxinruo> <2015-12-11> modify for CR01608017 begin
