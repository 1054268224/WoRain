/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.applock;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.cydroid.softmanager.utils.Log;

public class AppLockManagerActivityLoader extends AsyncTaskLoader<Object> {
    private static final String TAG = "AppLockManagerActivityLoader";

    public static final int LOADER_APP_LOCK_MANAGER = 10;

    public AppLockManagerActivityLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        Log.v(TAG, "onStartLoading");
        forceLoad();
        super.onStartLoading();
    }

    @Override
    public Object loadInBackground() {
        int id = getId();
        Log.d(TAG, "id: " + id);
        switch (id) {
            case LOADER_APP_LOCK_MANAGER:
                Log.d(TAG, "start handle LOADER_APP_LOCK_MANAGER");
                AppLockManager appLockManager = AppLockManager.getInstance();
                appLockManager.init(getContext());
                Log.d(TAG, "end handle LOADER_APP_LOCK_MANAGER");
                break;
            default:
                break;
        }
        return new Object();
    }
}