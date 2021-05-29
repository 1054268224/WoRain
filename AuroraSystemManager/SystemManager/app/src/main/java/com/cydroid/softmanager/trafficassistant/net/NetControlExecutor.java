/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: mengdw
 *
 * Date: 2017-04-19 for 81367
 */
package com.cydroid.softmanager.trafficassistant.net;

import android.content.Context;
import android.os.INetworkManagementService;

import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Method;

public class NetControlExecutor {
    private static final String TAG = "NetControlExecutor";
    private static final String NET_CONTROL_METHOD = "setFirewallUidChainRule";
    
    private static NetControlExecutor mNetControlExecutor;
    
    public static NetControlExecutor getInstance() {
        if (null == mNetControlExecutor) {
            synchronized(NetControlExecutor.class) {
                if (null == mNetControlExecutor) {
                    mNetControlExecutor = new NetControlExecutor();
                }
            }
        }
        return mNetControlExecutor;
    }
    
    public void setNetworkControlRule(int uid, int netType, boolean isBlock) {//xuanyutag
        try {
            INetworkManagementService networkService = INetworkManagementService.Stub
                    .asInterface(android.os.ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
            /*Class netManger = INetworkManagementService.class;
            Method method = netManger.getDeclaredMethod(NET_CONTROL_METHOD,
                    int.class, int.class, boolean.class);
            method.setAccessible(true);*/
            // todo wsj：注掉
//            networkService.setFirewallUidChainRule(uid, netType, isBlock);
            android.util.Log.d(TAG, "setNetworkControlRule " + uid + "," + (netType == Constant.MOBILE ? "mobile" : "wifi") +","+ (isBlock ? "prohibit":"allow"));
            //method.invoke(networkService, uid, netType, isBlock);
        } catch (Exception e) {
            android.util.Log.e(TAG, "setNetworkControlRule Exception e=" + e.toString());
        }
    }
}