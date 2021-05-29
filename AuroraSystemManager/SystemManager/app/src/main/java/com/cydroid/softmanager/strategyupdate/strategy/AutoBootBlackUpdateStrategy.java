package com.cydroid.softmanager.strategyupdate.strategy;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.utils.Log;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.provider.Settings;
/**
 * Created by mengjk on 17-5-16.
 */
public class AutoBootBlackUpdateStrategy implements IStrategy {
    private static final String TAG = AutoBootBlackUpdateStrategy.class.getSimpleName();
    public static final String TYPE = "autobootsysblack";
    private final static String AUTOBOOT_BLACK_VERSION_TAG = "autoboot_blacklist_version";
    private static final String ACTION_UPDATE_SYSTEM_APP_BLACK = "com.chenyee.intent.action.UPDATE_SYSTEM_APP_BLACKLIST";

    @Override
    public void parseJson(Context context, String json, ParseStrategyCallBack callBack) {
        Log.d(TAG, "pareseJson");
        if (callBack != null) {
            callBack.parseStart(getType());
        }
        try {
            JSONObject jsonObj = new JSONObject(json);
            updateVersion(context, jsonObj);
            JSONArray dataArray = jsonObj.getJSONArray("d");
            if (dataArray == null || !isUpdateAutoBootBlackList()) {
                return;
            }
            context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI, "usertype='" + TYPE + "'", null);
            updateAutoBootBlackList(context, dataArray, callBack);
            sendUpdateAutoBootSysAppBlackListBroadcast(context);
            if (callBack != null) {
                callBack.parseSuccess(getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callBack != null) {
                callBack.parseError(getType(), e.toString());
            }
        }
    }

    private void updateVersion(Context context, JSONObject jsonObj) throws JSONException {
        int version = jsonObj.getInt("ver");
        Settings.System.putInt(context.getContentResolver(), AUTOBOOT_BLACK_VERSION_TAG, version);
    }

    private void updateAutoBootBlackList(Context context, JSONArray dataArray, ParseStrategyCallBack callBack)
            throws JSONException {
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObj = dataArray.getJSONObject(i);
            if (dataObj == null) {
                continue;
            }
            String packageName = dataObj.getString("p");
            int status = dataObj.getInt("t");
            ContentValues values = new ContentValues();
            values.put("usertype", TYPE);
            values.put("packagename", packageName);
            values.put("status", status);
            context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, values);
            if (callBack != null) {
                callBack.parsing(getType(), dataObj);
            }
        }
    }

    private boolean isUpdateAutoBootBlackList() {
        File file = new File(Environment.getExternalStorageDirectory(), "testAutoBootBlackList9876543210");
        if (file.exists()) {
            Log.d(TAG, "isUpdateAutoBootBlackList testAutoBootBlackList9876543210 exists");
            return false;
        }
        return true;
    }

    private void sendUpdateAutoBootSysAppBlackListBroadcast(Context context) {
        Intent intent = new Intent(ACTION_UPDATE_SYSTEM_APP_BLACK);
        context.sendBroadcast(intent);
    }

    @Override
    public void setUpdateSuccess(Context context) {
        Log.d(TAG, "setUpdateSuccess");
    }

    @Override
    public boolean isNotify() {
        return false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public int getVersion(Context context) {
        final int VERSION = Settings.System.getInt(context.getContentResolver(), AUTOBOOT_BLACK_VERSION_TAG,
                0);
        return VERSION;
    }

    @Override
    public JSONObject getStrategyRequestBody(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("t", getType());
            jsonObject.put("ver", getVersion(context));
        } catch (Exception e) {
            Log.e(TAG, "getStrategyRequestBody error - " + e.toString());
        }
        return jsonObject;
    }

}
