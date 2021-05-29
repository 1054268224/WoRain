/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 卡顿跳帧数据接收服务
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.skipframes;

import com.cydroid.softmanager.utils.Log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chenyee.featureoption.ServiceUtil;
public class SkipFramesService extends Service {
    private static final String ACTION_SOFTMANAGER_SKIPFRAMES = "com.cydroid.softmanager.ACTION_EVENT_SKIPFRAMES";
    public static final String ACTION_LOCAL_SKIPFRAMES_EVENT = "com.cydroid.softmanager.analysis.skipframes";

    private static final String TAG = "SkipFramesService";
    private static final long COOL_DOWN_TIME = 5 * 1000;

    private Context mContext;
    private long mLatestInfoTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SkipFramesService onCreate");
        mContext = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(this);
        parserModeChangeIntent(intent);
        return Service.START_STICKY;
    }

    private void parserModeChangeIntent(Intent intent) {
        long currentElapsedTime = SystemClock.elapsedRealtime();
        if (currentElapsedTime - mLatestInfoTime <= COOL_DOWN_TIME) {
            return;
        }
        mLatestInfoTime = SystemClock.elapsedRealtime();
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(ACTION_SOFTMANAGER_SKIPFRAMES)) {
            Log.d(TAG, "get skipframes info");
            final byte[] buffer = intent.getByteArrayExtra("stats");
            if (buffer == null) {
                Log.d(TAG, "skipframe info error!");
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
                    Intent skipFrameIntent = new Intent(ACTION_LOCAL_SKIPFRAMES_EVENT);
                    skipFrameIntent.putExtra("stats", buffer);
                    Log.d(TAG, "send local broadcast ACTION_LOCAL_SKIPFRAMES_EVENT");
                    localBroadcastManager.sendBroadcast(skipFrameIntent);
                }
            }.start();
        }
    }
}
