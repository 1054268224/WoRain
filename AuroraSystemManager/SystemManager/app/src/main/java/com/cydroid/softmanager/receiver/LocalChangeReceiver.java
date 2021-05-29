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

import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.utils.Log;

import java.util.HashMap;
import java.util.Map;

public class LocalChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "LocalChangeReceiver";

    private static final int LOCAL_CHANGE = 0;

    private static final Map<String, Integer> ACTION_MSG_MAP = new HashMap<>();

    static {
        ACTION_MSG_MAP.put(Intent.ACTION_LOCALE_CHANGED, LOCAL_CHANGE);
    }

    private final Context mContext;
    private final LocalChangedCallback mLocalChangedCallback;
    private final BackgroundHandler mBackgroundHandler;

    public LocalChangeReceiver(Context context, LocalChangedCallback callback) {
        mContext = context;
        mLocalChangedCallback = callback;
        mBackgroundHandler = new BackgroundHandler(Looper.getMainLooper());
    }

    public void registerLocalChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        mContext.registerReceiver(this, filter);
    }

    public void unregisterLocalChangeReceiver() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive action:" + action);
        if (!Intent.ACTION_LOCALE_CHANGED.equals(action) || null == mLocalChangedCallback) {
            return;
        }

        Log.d(TAG, "ACTION_LOCALE_CHANGED");
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
                case LOCAL_CHANGE:
                    mLocalChangedCallback.onLocalChange();
                    break;
                default:
                    break;
            }
        }
    }
}