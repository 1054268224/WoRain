package com.cydroid.softmanager.oneclean.loader;

import android.app.ActivityManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.PackageManager;

import com.cydroid.softmanager.utils.Log;

/**
 * File Description:
 *
 * @author: Gionee-lihq
 * @see: 2013-1-13 Change List:
 */
public abstract class BaseLoader<D> extends AsyncTaskLoader<D> {

    protected PackageManager mPm;
    protected ActivityManager mAm;
    private static final String TAG = "BaseLoader-->";

    public BaseLoader(Context context) {
        super(context);
        this.mPm = context.getPackageManager();
        this.mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    protected void onStartLoading() {
        Log.v(TAG, "onStartLoading");
        forceLoad();
        super.onStartLoading();
    }

    @Override
    protected boolean onCancelLoad() {
        Log.v(TAG, "onCancelLoad");
        return super.onCancelLoad();
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    protected void onReset() {
        Log.v(TAG, "onReset");
        super.onReset();
    }
}
