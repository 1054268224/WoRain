package com.cydroid.softmanager.oneclean;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;

import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class RunningAppListLoader extends AsyncTaskLoader<List<ProcessMemoryEntity>> {

    private static final String TAG = "RunningAppListLoader";
    private final Context mContext;
    private List<ProcessMemoryEntity> mData;

    public RunningAppListLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public List<ProcessMemoryEntity> loadInBackground() {
        Log.i(TAG, "begin load");
        MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(mContext);
        Bundle params = new Bundle();
        params.putBoolean("is_third_only", false);
        params.putBoolean("is_launcher_show", true);
        List<ProcessMemoryEntity> runningProcessList = memoryManager
                .getRunningProcessMemoryEntitys(MemoryManager.CLEAN_TYPE_PISTOL, params);
        Log.d(TAG, "getRunning runningProcessList size=" + runningProcessList.size());
        attachWhitelistInfo(runningProcessList);
        attachEncryptionsAppsInfo(runningProcessList);
        return runningProcessList;
    }

    private void attachWhitelistInfo(List<ProcessMemoryEntity> runningProcessList) {
        WhiteListManager whiteListManager = WhiteListManager.getInstance();
        whiteListManager.init(mContext);
        for (ProcessMemoryEntity runningPowerConsumeApp : runningProcessList) {
            if (whiteListManager.isInUserWhiteApps(runningPowerConsumeApp.mPackageName)) {
                Log.d(TAG, "pkg:" + runningPowerConsumeApp.mPackageName + " in user whitelist");
                runningPowerConsumeApp.mIsInUserWhiteAppList = true;
            }
        }
    }

    private void attachEncryptionsAppsInfo(List<ProcessMemoryEntity> runningProcessList) {
        ArrayList<String> encryptionsApps = HelperUtils.getEncryptionsApps(mContext.getContentResolver());
        for (ProcessMemoryEntity runningPowerConsumeApp : runningProcessList) {
            if (encryptionsApps.contains(runningPowerConsumeApp.mPackageName)) {
                runningPowerConsumeApp.mIsPrivateApp = true;
            }
        }
    }

    @Override
    public void deliverResult(List<ProcessMemoryEntity> data) {
        if (isReset()) {
            if (data != null) {
                onReleaseResources(data);
            }
        }
        List<ProcessMemoryEntity> oldApps = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    private void onReleaseResources(List<ProcessMemoryEntity> data) {
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<ProcessMemoryEntity> data) {
        super.onCanceled(data);
        onReleaseResources(data);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }
    }
}
