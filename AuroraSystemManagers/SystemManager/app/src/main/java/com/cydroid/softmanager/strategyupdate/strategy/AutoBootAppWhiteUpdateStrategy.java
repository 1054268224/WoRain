package com.cydroid.softmanager.strategyupdate.strategy;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.utils.Log;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
/**
 * Created by mengjk on 17-5-16.
 */
public class AutoBootAppWhiteUpdateStrategy implements IStrategy {
    private static final String TAG = AutoBootAppWhiteUpdateStrategy.class.getSimpleName();
    public static final String TYPE = "autobootwhite";
    private final static String AUTOBOOT_WHITE_LIST_VERSION_TAG = "autoboot_whitelist_version";

    private boolean mNeedNotify = false;

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
            if (dataArray == null || !isUpdateAutoBootWhiteList()) {
                return;
            }
            context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI, "usertype='" + TYPE + "'", null);
            updateAutoBootWhiteList(context, dataArray, callBack);
            mNeedNotify = true;
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
        Settings.System.putInt(context.getContentResolver(), AUTOBOOT_WHITE_LIST_VERSION_TAG, version);
    }

    private void updateAutoBootWhiteList(Context context, JSONArray dataArray, ParseStrategyCallBack callBack)
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

    private boolean isUpdateAutoBootWhiteList() {
        File file = new File(Environment.getExternalStorageDirectory(), "testAutoBootWhiteList9876543210");
        if (file.exists()) {
            Log.d(TAG, "isUpdateAutoBootWhiteList testAutoBootWhiteList9876543210 exists");
            return false;
        }
        return true;
    }

    @Override
    public void setUpdateSuccess(Context context) {
        Log.d(TAG, "setUpdateSuccess");
    }

    @Override
    public boolean isNotify() {
        return mNeedNotify;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public int getVersion(Context context) {
        final int VERSION = Settings.System.getInt(context.getContentResolver(),
                AUTOBOOT_WHITE_LIST_VERSION_TAG, 0);
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
