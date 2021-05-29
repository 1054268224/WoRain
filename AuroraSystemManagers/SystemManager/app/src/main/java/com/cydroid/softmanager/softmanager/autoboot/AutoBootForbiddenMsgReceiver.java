/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cydroid.softmanager.utils.Log;

public class AutoBootForbiddenMsgReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoBootForbiddenMsgReceiver";

    public static final String ACTION_APPBOOT_FORBIDDEN_MSG =
            "com.chenyee.intent.action.APPBOOT_FORBIDDEN_MSG";
    public static final String ACTION_TURN_OFF_NOTIFICATION =
            "com.chenyee.intent.action.APPBOOT_TURN_OFF_NOTIFICATION";

    private static final String ACTION_EXTRA_FORBIDDEN_PKG = "FORBIDDEN_PKG";

    private AutoBootForbiddenMsgManager mAutoBootForbiddenMsgManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive action:" + action);
        mAutoBootForbiddenMsgManager = AutoBootForbiddenMsgManager.getInstance();
        mAutoBootForbiddenMsgManager.init(context);
        if (ACTION_APPBOOT_FORBIDDEN_MSG.equals(action)) {
            processAutoBootForbiddenMsg(intent);
        } else if (ACTION_TURN_OFF_NOTIFICATION.equals(action)) {
            closeAutoBootForbiddenMsg();
            cancelNotification();
        }
    }

    private void processAutoBootForbiddenMsg(Intent intent) {
        String packageName = intent.getStringExtra(ACTION_EXTRA_FORBIDDEN_PKG);
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        mAutoBootForbiddenMsgManager.processAutoBootForbiddenMsg(packageName);
    }

    private void closeAutoBootForbiddenMsg() {
        mAutoBootForbiddenMsgManager.closeAutoBootForbiddenMsg();
    }

    private void cancelNotification() {
        mAutoBootForbiddenMsgManager.cancelNotification();
    }
}
