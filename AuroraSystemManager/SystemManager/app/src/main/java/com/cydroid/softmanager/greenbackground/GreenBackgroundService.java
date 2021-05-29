package com.cydroid.softmanager.greenbackground;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.ExecuteAsRoot;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.utils.Log;
import com.chenyee.featureoption.ServiceUtil;
public class GreenBackgroundService extends Service {
    private static final String TAG = "GreenBackgroundService";

    private static final boolean DEBUG = true;

    private static final long LOW_MEM_CLEAN_TRIGGER_TIME = 5 * 60 * 1000;   // 5 minutes
    private static final long AUTO_CLEAN_TRIGGER_TIME = 2 * 60 * 1000;      // 2 minutes

    private static final int EVENT_KILL_PROCESS = 1;
    private static final int KILL_PROCESS_DELAY_TIME = 500;

    private Context mContext;
    private MyIntentReceiver mIntentMonitorReceiver;

    private PendingIntent mLowMemCleanIntent = null;
    private PendingIntent mAutoCleanIntent = null;
    private AlarmManager mAlarmManager = null;

    private WhiteListManager mWhiteListManager;

    @Override
    public void onCreate() {
        // Add by zhiheng.huang on 2020/3/5 for TEWBW-986 start
        // main thread may be stalled/sleeping/doing too much work and cannot process the start of the service
        // There is no guarantee that it will not crash
        ServiceUtil.handleStartForegroundServices(this);
        // Add by zhiheng.huang on 2020/3/5 for TEWBW-986 end

        mContext = GreenBackgroundService.this;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mWhiteListManager = WhiteListManager.getInstance();
        mWhiteListManager.init(this);

        registerIntentFilterReceiver();
        setCleanAlarmByGreenBgState();
    }

    private void setCleanAlarmByGreenBgState() {
        // Gionee <yangxinruo> <2015-12-9> modify for CR01606577 begin
        if (mWhiteListManager.isGreenBackgroundEnable()) {
            setLowMemCleanAlarmManager();
        }
        // Gionee <yangxinruo> <2015-12-9> modify for CR01606577 end
    }

    /*
     * 内存低于20%清理后台应用
     */
    public void setLowMemCleanAlarmManager() {
        Intent timeoutIntent = new Intent();
        timeoutIntent.setAction(Consts.ACTION_LOW_MEM_CLEAN);
        mLowMemCleanIntent = PendingIntent.getBroadcast(this, 0, timeoutIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (mLowMemCleanIntent != null) {
            mAlarmManager.cancel(mLowMemCleanIntent);
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    LOW_MEM_CLEAN_TRIGGER_TIME, mLowMemCleanIntent);
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(this);
        // Modify by zhiheng.huang on 2020/3/5 for TEWBW-986 start
        return START_STICKY;
        // Modify by zhiheng.huang on 2020/3/5 for TEWBW-986 end
    }

    private void registerIntentFilterReceiver() {
        mIntentMonitorReceiver = new MyIntentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Consts.ACTION_KILL_PROCESS);
        filter.addAction(Consts.ACTION_AUTO_CLEAN_MEM);
        filter.addAction(Consts.ACTION_LOW_MEM_CLEAN);
        mContext.registerReceiver(mIntentMonitorReceiver, filter);
    }

    private class MyIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "SystemBDReceiver onReceive action:" + action);
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                triggerByScreenOffIntent();
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                triggerByScreenOnIntent();
            } else if (action.equals(Consts.ACTION_KILL_PROCESS)) {
                triggerByKillProcessIntent(intent);
            } else if (action.equals(Consts.ACTION_AUTO_CLEAN_MEM)) {
                triggerByAutoCleanMemIntent();
            } else if (action.equals(Consts.ACTION_LOW_MEM_CLEAN)) {
                triggerByLowMemCleanIntent(context);
            }
        }
    }

    private void triggerByScreenOffIntent() {
        if (mWhiteListManager.isGreenBackgroundEnable()) {
            setScreenOffAutoCleanAlarmManager();
        }
    }

    private void triggerByScreenOnIntent() {
        cancelScreenOffAutoCleanAlarmManager();
    }

    public void cancelScreenOffAutoCleanAlarmManager() {
        if (mAlarmManager != null && mAutoCleanIntent != null) {
            mAlarmManager.cancel(mAutoCleanIntent);
        }
    }

    /*
     * 灭屏2分钟后清理后台应用
     */
    public void setScreenOffAutoCleanAlarmManager() {
        Intent timeoutIntent = new Intent(Consts.ACTION_AUTO_CLEAN_MEM);
        mAutoCleanIntent = PendingIntent.getBroadcast(this, 0, timeoutIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (mAutoCleanIntent != null) {
            mAlarmManager.cancel(mAutoCleanIntent);
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + AUTO_CLEAN_TRIGGER_TIME, mAutoCleanIntent);
            Log.d(TAG, "GreenBackground clean start after " + AUTO_CLEAN_TRIGGER_TIME);
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.d(TAG, "FOR DEBUG:" + e);
        }
    }

    private void triggerByKillProcessIntent(Intent intent) {
        // System.gc();
        int processId = intent.getIntExtra("processid", -1);
        // killBackgroundProcesses(processId);
        Message msg = Message.obtain();
        msg.what = EVENT_KILL_PROCESS;
        msg.obj = Integer.valueOf(processId);
        mHandler.sendMessageDelayed(msg, KILL_PROCESS_DELAY_TIME);
    }

    private void triggerByAutoCleanMemIntent() {
        // 灭屏2分钟清理内存
        // Gionee <houjie> <2016-01-22> remove for CR01622097 begin
        // if (mProviderHelper.getBoolean(Consts.OFF_SCREEN_CLEAN, false)) {
        // Intent i = new Intent(Consts.ONE_CLEAN_FROM_SYSTEM);
        // i.putExtra(Consts.CLEAN_TYPE, Consts.OFF_SCREEN_CLEAN_TYPE);
        // mContext.sendBroadcast(i);
        // }
        // Gionee <houjie> <2016-01-22> remove for CR01622097 end
        sendBroadcastToCleanGreenBg();
    }

    private void sendBroadcastToCleanGreenBg() {
        if (mWhiteListManager.isGreenBackgroundEnable()) {
            Log.d(DEBUG, TAG, "send Broadcast To Process GreenBackground");
            Intent greenbackintent = new Intent(Consts.ACTION_GREEN_BACKGROUND_CLEAN);
            mContext.sendBroadcast(greenbackintent);
        }
    }

    private void triggerByLowMemCleanIntent(Context context) {
        sendBroadcastToCleanGreenBg();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_KILL_PROCESS:
                    int processId = ((Integer) msg.obj).intValue();
                    killBackgroundProcesses(processId);
                    break;
                default:
                    break;
            }
        }
    };

    private void killBackgroundProcesses(int processId) {
        try {
            ExecuteAsRoot.execute("kill -9 " + processId);
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "killBackgroundProcesses throw exception");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
