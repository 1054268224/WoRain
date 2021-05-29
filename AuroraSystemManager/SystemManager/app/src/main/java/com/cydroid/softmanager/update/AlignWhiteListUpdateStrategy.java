/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.update;

import android.content.Context;
import android.provider.Settings;

import com.cydroid.softmanager.powersaver.utils.PreferenceHelper;
import com.cydroid.softmanager.utils.HelperUtils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class AlignWhiteListUpdateStrategy implements UpdateStrategy {
    private final static String TAG = "AlignWhiteListUpdateStrategy";
    private final static String ALIGN_LIST_VERSION_TAG = "align_data_version";

    private final static String TEST_HTTP_URL = "http://t-reactor.gionee.com/reactor-api/bwList.do?t=align&v=";
    private final static String HTTP_URL = "http://reactor.gionee.com/reactor-api/bwList.do?t=align&v=";

    public HttpRequestBase getHttpRequest(Context context) {
        String urlStr = getHttpUrl(context);
        HttpGet httpGet = new HttpGet(urlStr);
        return httpGet;
    }

    private String getHttpUrl(Context context) {
        int version = new PreferenceHelper(context).getInt(ALIGN_LIST_VERSION_TAG, 0);
        return HelperUtils.isUseTestUrl()  ? (TEST_HTTP_URL + version) : (HTTP_URL + version);
    }

    public void setUpdateSuccess(Context context) {
    }

    public void pareseJson(Context context, String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);
            updateVersion(context, jsonObj);

            JSONArray dataArray = jsonObj.getJSONArray("d");
            if (dataArray == null) {
                return;
            }
            updateAlignList(context, dataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateVersion(Context context, JSONObject jsonObj) throws JSONException {
        int version = jsonObj.getInt("v");
        new PreferenceHelper(context).commitInt(ALIGN_LIST_VERSION_TAG, version);
    }

    private void updateAlignList(Context context, JSONArray dataArray) throws JSONException {
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObj = dataArray.getJSONObject(i);
            if (null == dataObj) {
                continue;
            }
            String packageName = dataObj.getString("p");
            int type = dataObj.getInt("t");
            Settings.System.putInt(context.getContentResolver(), "ALIGN_" + packageName, type);
        }
    }

    public boolean ifNeedNotifyUpdateSuccessed() {
        return false;
    }

    @Override
    public Map<String, String> getResultParams() {
        return null;
    }

    @Override
    public boolean isNeedSecurityConnection() {
        return false;
    }
}