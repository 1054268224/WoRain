package com.cydroid.softmanager.oneclean.loader;

import android.content.Context;

import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.utils.Log;

/**
 * 
 * File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-4-15 Change List:
 */
public class ApplicationMrgLoader extends BaseLoader<Object> {
    private static final String TAG = "ApplicationMrgLoader";
    public static final int LOADER_WHITE_LIST = 100 + 4;

    public ApplicationMrgLoader(Context context) {
        super(context);
    }

    @Override
    public Object loadInBackground() {
        int id = getId();
        Log.d(TAG, "id: " + id);
        switch (id) {
            case LOADER_WHITE_LIST:
                WhiteListManager whiteListManager = WhiteListManager.getInstance();
                whiteListManager.init(getContext());
                break;
            default:
                break;
        }
        return new Object();
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        super.onStartLoading();
    }
}
