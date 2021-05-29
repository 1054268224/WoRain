/*
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
import android.content.Intent;

import com.cydroid.softmanager.powersaver.utils.SmartCleanInfoWriter;
import com.cydroid.softmanager.utils.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;

public class VirusLibraryUpdateStrategy implements UpdateStrategy {
    private final static String TAG = "VirusLibraryUpdateStrategy";

    @Override
    public HttpRequestBase getHttpRequest(Context context) {
        startUpdateVirusLibraryService(context);
       // updateAVLVirusDatabase();
        return null;
    }

    private void startUpdateVirusLibraryService(Context context) {
        Log.d(TAG, "send broadcast to systemmanagerSDK");
        Intent updateBroadcastIntent = new Intent("com.cydroid.systemmanager.UPDATE_LIBRARY");
        context.sendBroadcast(updateBroadcastIntent);
    }

    private void updateAVLVirusDatabase() {

    }

    private void recordUpdateRulesData(int res, String newVirusVer) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String dateStr = formatter.format(curDate);
        StringBuilder sb = new StringBuilder();
        sb.append("\n<---AVL Update " + dateStr + "--->").append("\nResult:" + res)
                .append("\nFinVirusDatabaseVersion:" + newVirusVer);
        SmartCleanInfoWriter.writeToDataFile(sb.toString());
    }

    @Override
    public void setUpdateSuccess(Context context) {
    }

    @Override
    public void pareseJson(Context context, String json) {
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