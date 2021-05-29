package com.cydroid.softmanager.strategyupdate.strategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.content.ContentValues;
import android.content.Context;
import android.provider.Settings;
/**
 * Created by mengjk on 17-5-16.
 */
public class BlackKillUpdateStrategy implements IStrategy {
    private static final String TAG = BlackKillUpdateStrategy.class.getSimpleName();
    public static final String TYPE = "blackkill";
    private final static String BLACKKILL_DATA_VERSION = "blackkill_data_version";

    @Override
    public void parseJson(Context context, String json, ParseStrategyCallBack callBack) {
        if (callBack != null) {
            callBack.parseStart(getType());
        }
        try {
            JSONObject jsonObj = new JSONObject(json);
            if (jsonObj != null) {
                updateVersion(context, jsonObj);

                JSONArray dataArray = jsonObj.getJSONArray("d");
                if (dataArray == null) {
                    return;
                }
                context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI, "usertype='" + TYPE + "'",
                        null);
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObj = dataArray.getJSONObject(i);
                    if (dataObj != null) {
                        String packageName = dataObj.getString("p");
                        int type = dataObj.getInt("t");
                        ContentValues values = new ContentValues();
                        values.put("usertype", TYPE);
                        values.put("packagename", packageName);
                        values.put("status", type);
                        context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, values);
                        if (callBack != null) {
                            callBack.parsing(getType(), dataObj);
                        }
                    }
                }
                HelperUtils.cleanBlackKillList();
                if (callBack != null) {
                    callBack.parseSuccess(getType());
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "can not parse blackkill json e=" + e);
            if (callBack != null) {
                callBack.parseError(getType(), e.toString());
            }
        }
    }

    private void updateVersion(Context context, JSONObject jsonObj) throws JSONException {
        int version = jsonObj.getInt("ver");
        Log.d(TAG, "parseBlackkillJson return version = " + version);
        Settings.System.putInt(context.getContentResolver(), BLACKKILL_DATA_VERSION, version);
    }

    @Override
    public void setUpdateSuccess(Context context) {
        Log.d(TAG, "Blackkill list update successful!");
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
        final int VERSION = Settings.System.getInt(context.getContentResolver(), BLACKKILL_DATA_VERSION, 0);
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
