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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.utils.Log;

public class AutoBootAppConditionInitV2 {
    private static final String TAG = "AutoBootAppConditionInitV2";

    private static final String ACTION_UPDATE_BOOT_APP_CONDITION =
            "com.chenyee.intent.action.UPDATE_BOOT_APP_CONDITION";

    private static final String ROSTER_AUTO_BOOT_WHITELIST_TYPE = "autobootwhite";
    private static final String ROSTER_AUTO_BOOT_BROADCAST_ACTION_WHITELIST_TYPE =
            "autobootbroadcastaction";
    private static final String ROSTER_AUTO_BOOT_SERVICE_ACTION_WHITELIST_TYPE =
            "autobootserviceaction";
    private static final String ROSTER_AUTO_BOOT_SERVICE_APP_WHITELIST_TYPE =
            "autobootserviceapp";
    private static final String ROSTER_AUTO_BOOT_CONTENT_PROVIDER_WHITELIST_TYPE =
            "autobootprovider";

    public static synchronized void initAutoBootAppConditionIfNeed(final Context context) {
        if (!isNeedInit(context)) {
            return;
        }
        Log.d(TAG, "initAutoBootAppConditionIfNeed begin");
        loadDefaultBootAppListByType(context, ROSTER_AUTO_BOOT_WHITELIST_TYPE);
        loadDefaultBootAppListByType(context, ROSTER_AUTO_BOOT_BROADCAST_ACTION_WHITELIST_TYPE);
        loadDefaultBootAppListByType(context, ROSTER_AUTO_BOOT_SERVICE_ACTION_WHITELIST_TYPE);
        loadDefaultBootAppListByType(context, ROSTER_AUTO_BOOT_SERVICE_APP_WHITELIST_TYPE);
        loadDefaultBootAppListByType(context, ROSTER_AUTO_BOOT_CONTENT_PROVIDER_WHITELIST_TYPE);

        sendUpdateAutoBootAppConditionBroadcast(context);
        Log.d(TAG, "initAutoBootAppConditionIfNeed end");
    }

    private static boolean isNeedInit(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                    new String[]{"packagename"},
                    "usertype='" + ROSTER_AUTO_BOOT_BROADCAST_ACTION_WHITELIST_TYPE + "'", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "isNeedInit e:", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return true;
    }

    private static void loadDefaultBootAppListByType(final Context context, String type) {
        removeBootAppListByType(context, type);
        String[] defaultBootAppList = context.getResources().
                getStringArray(getDefaultBootAppListIdByType(type));
        insertBootAppListByType(context, type, defaultBootAppList);
    }

    private static int getDefaultBootAppListIdByType(String type) {
        switch (type) {
            case ROSTER_AUTO_BOOT_WHITELIST_TYPE:
                return R.array.autoboot_app_default_whitelist;
            case ROSTER_AUTO_BOOT_BROADCAST_ACTION_WHITELIST_TYPE:
                return R.array.autoboot_app_default_broadcast_action_whitelist;
            case ROSTER_AUTO_BOOT_SERVICE_ACTION_WHITELIST_TYPE:
                return R.array.autoboot_app_default_service_action_whitelist;
            case ROSTER_AUTO_BOOT_SERVICE_APP_WHITELIST_TYPE:
                return R.array.autoboot_app_default_service_app_whitelist;
            case ROSTER_AUTO_BOOT_CONTENT_PROVIDER_WHITELIST_TYPE:
                return R.array.autoboot_app_default_content_provider_whitelist;
            default:
                return R.array.autoboot_app_default_whitelist;
        }
    }

    private static void removeBootAppListByType(final Context context, String type) {
        context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI,
                "usertype='" + type + "'", null);
    }

    private static void insertBootAppListByType(final Context context, String type,
                                                String[] defaultBootAppList) {
        for (int i = 0; i < defaultBootAppList.length; ++i) {
            ContentValues values = new ContentValues();
            values.put("usertype", type);
            values.put("packagename", defaultBootAppList[i]);
            values.put("status", "1");
            context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, values);
        }
    }

    private static void sendUpdateAutoBootAppConditionBroadcast(final Context context) {
        Intent intent = new Intent(ACTION_UPDATE_BOOT_APP_CONDITION);
        context.sendBroadcast(intent);
    }
}