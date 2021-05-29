/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cydroid.softmanager.interfaces.TimeChangedCallback;
import com.cydroid.softmanager.utils.Log;

import java.util.HashMap;
import java.util.Map;

public class TimeChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "TimeChangeReceiver";

    private static final int TIME_CHANGE = 0;
    private static final Map<String, Integer> ACTION_MSG_MAP = new HashMap<>();

    static {
        ACTION_MSG_MAP.put(Intent.ACTION_TIME_CHANGED, TIME_CHANGE);
    }

    private final Context mContext;
    private final TimeChangedCallback mTimeChangedCallback;
    private final BackgroundHandler mBackgroundHandler;

    public TimeChangeReceiver(Context context, TimeChangedCallback callback) {
        mContext = context;
        mTimeChangedCallback = callback;
        mBackgroundHandler = new BackgroundHandler(Looper.getMainLooper());
    }

    public void registerTimeChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        mContext.registerReceiver(this, filter);
    }

    public void unregisterTimeChangeReceiver() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive action:" + action);
        if (!Intent.ACTION_TIME_CHANGED.equals(action)
                || null == mTimeChangedCallback) {
            return;
        }

        Log.d(TAG, "ACTION_TIME_CHANGED");
        Message message = mBackgroundHandler.obtainMessage(ACTION_MSG_MAP.get(action));
        mBackgroundHandler.sendMessage(message);
    }

    class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            Log.d(TAG, "BackgroundHandler msg.what:" + message.what);
            switch (message.what) {
                case TIME_CHANGE:
                    mTimeChangedCallback.onTimeChange();
                    break;
                default:
                    break;
            }
        }
    }
}