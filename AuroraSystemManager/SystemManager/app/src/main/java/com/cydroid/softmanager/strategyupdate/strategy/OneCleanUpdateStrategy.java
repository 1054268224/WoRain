package com.cydroid.softmanager.strategyupdate.strategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;

import android.content.ContentValues;
import android.content.Context;
import android.provider.Settings;
import com.cydroid.softmanager.utils.Log;

/**
 * Created by mengjk on 17-5-16.
 */
public class OneCleanUpdateStrategy implements IStrategy {
    private static final String TAG = OneCleanUpdateStrategy.class.getSimpleName();
    public static final String TYPE = "oneclean";
    private final static String ONECLEAN_VERSION_TAG = "oneclean_data_version";
    private final static String ONECLEAN_UPDATA_SUCCESS_TAG = "oneclean_data_update_success";
    private final static int SCREEN_OFF_WHITE_APPS_TYPE = 10;

    @Override
    public void parseJson(Context context, String json, ParseStrategyCallBack callBack) {
        if (callBack != null) {
            callBack.parseStart(getType());
        }
        try {
            JSONObject jsonObj = new JSONObject(json);
            updateVersion(context, jsonObj);
            JSONArray dataArray = jsonObj.getJSONArray("d");
            if (dataArray == null) {
                return;
            }
            deleteOldSystemWhiteList(context);
            addNewSystemWhiteListByJSONArray(context, dataArray, callBack);

            WhiteListManager whiteListManager = WhiteListManager.getInstance();
            whiteListManager.initReset();
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

    private void deleteOldSystemWhiteList(Context context) {
        context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI, "usertype='oneclean' and status!='2'",
                null);
    }

    private void addNewSystemWhiteListByJSONArray(Context context, JSONArray dataArray,
            ParseStrategyCallBack callBack) throws JSONException {
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObj = dataArray.getJSONObject(i);
            if (dataObj == null) {
                continue;
            }

            String packageName = dataObj.getString("p");
            int status = dataObj.getInt("t");
            Log.d(TAG,
                    "addNewSystemWhiteListByJSONArray 1 packageName:" + packageName + ", status:" + status);
            if (2 == status) {
                continue;
            }
            String[] items = parsePackageName(packageName);
            if (2 == items.length) {
                packageName = items[0];
                int priority = Integer.parseInt(items[1]);
                status = (-1 == priority) ? SCREEN_OFF_WHITE_APPS_TYPE : status;
            }
            Log.d(TAG,
                    "addNewSystemWhiteListByJSONArray 2 packageName:" + packageName + ", status:" + status);
            ContentValues values = new ContentValues();
            values.put("usertype", TYPE);
            values.put("packagename", packageName);
            values.put("status", status);
            if (callBack != null) {
                callBack.parsing(getType(), dataObj);
            }
            context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, values);
        }
    }

    private String[] parsePackageName(String packageName) {
        String[] items = packageName.split(",");
        return items;
    }

    private void updateVersion(Context context, JSONObject jsonObj) throws JSONException {
        int version = jsonObj.getInt("ver");
        Settings.System.putInt(context.getContentResolver(), ONECLEAN_VERSION_TAG, version);
    }

    @Override
    public void setUpdateSuccess(Context context) {
        Settings.System.putString(context.getContentResolver(), ONECLEAN_UPDATA_SUCCESS_TAG, "yes");
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
        final int VERSION = Settings.System.getInt(context.getContentResolver(), ONECLEAN_VERSION_TAG, 0);
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
