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

import com.cydroid.softmanager.interfaces.ThemeChangedCallback;
import com.cydroid.softmanager.utils.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThemeChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "ThemeChangeReceiver";

    private static final int THEME_CHANGE = 0;
    private static final String THEME_CHANGE_ACTION = "com.cyee.intent.action.theme.change";
    private static final String THEME_CHANGE_CATEGORY_V2 = "com.cyee.intent.category.theme.V2";
    private static final String THEME_CHANGE_CATEGORY_V3 = "com.cyee.intent.category.theme.V3";

    private static final Map<String, Integer> ACTION_MSG_MAP = new HashMap<>();

    static {
        ACTION_MSG_MAP.put(THEME_CHANGE_ACTION, THEME_CHANGE);
    }

    private final Context mContext;
    private final ThemeChangedCallback mThemeChangedCallback;
    private final BackgroundHandler mBackgroundHandler;

    public ThemeChangeReceiver(Context context, ThemeChangedCallback callback) {
        mContext = context;
        mThemeChangedCallback = callback;
        mBackgroundHandler = new BackgroundHandler(Looper.getMainLooper());
    }

    public void registerThemeChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(THEME_CHANGE_ACTION);
        filter.addCategory(THEME_CHANGE_CATEGORY_V2);
        filter.addCategory(THEME_CHANGE_CATEGORY_V3);
        mContext.registerReceiver(this, filter);
    }

    public void unregisterThemeChangeReceiver() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive action:" + action);
        if (!ACTION_MSG_MAP.containsKey(action) || null == mThemeChangedCallback) {
            return;
        }

        String category = getCategory(intent);
        Log.d(TAG, "category:" + category);
        Message message = mBackgroundHandler.obtainMessage(ACTION_MSG_MAP.get(action), category);
        mBackgroundHandler.sendMessage(message);
    }

    private String getCategory(Intent intent) {
        Set<String> categories = intent.getCategories();

        if (null != categories) {
            String[] categoriesArray = categories.toArray(new String[categories.size()]);
            return categoriesArray[0];
        }
        return "";
    }

    class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            String category = message.obj.toString();
            Log.d(TAG, "BackgroundHandler msg.what:" + message.what + ", category:" + category);
            switch (message.what) {
                case THEME_CHANGE:
                    mThemeChangedCallback.changeTheme(category);
                    break;
                default:
                    break;
            }
        }
    }
}