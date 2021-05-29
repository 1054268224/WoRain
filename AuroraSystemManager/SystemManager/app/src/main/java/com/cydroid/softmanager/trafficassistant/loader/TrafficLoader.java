/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.trafficassistant.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.cydroid.softmanager.trafficassistant.controler.TrafficNetworkController;

public class TrafficLoader extends AsyncTaskLoader<Object> {
    public static final int ID_LOADER_DEFAULT = 100;    
    public static final int ID_LOADER_TRAFFIC_SAVE_WHITE_LIST = ID_LOADER_DEFAULT + 1;
    // Gionee: mengdw <2017-01-19> add for CR01776232 begin
    public static final int ID_LOADER_TRAFFIC_NETWORK_CONTROL_LIST = ID_LOADER_DEFAULT + 2;
    // Gionee: mengdw <2017-01-19> add for CR01776232 end
    private static boolean mLoadGreenBg = false;
    
    public TrafficLoader(Context context) {
        super(context);
    }

    @Override
    public Object loadInBackground() {
        switch (getId()) {
            case ID_LOADER_TRAFFIC_SAVE_WHITE_LIST:
                if (mLoadGreenBg) {
                    setLoadGreenBg(false);
                    break;
                }

                break;
            // Gionee: mengdw <2017-01-19> add for CR01776232 begin
            case ID_LOADER_TRAFFIC_NETWORK_CONTROL_LIST:
                TrafficNetworkController networkControlContoler = 
                        TrafficNetworkController.getInstance(getContext());
                networkControlContoler.init();
                break;
            // Gionee: mengdw <2017-01-19> add for CR01776232 end
            default:
                break;
        }
        return new Object();
    }

    public static boolean getLoadGreenBg() {
        return mLoadGreenBg;
    }

    public static void setLoadGreenBg(boolean loadGreenBg) {
        mLoadGreenBg = loadGreenBg;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        super.onStartLoading();
    }
}
