//Gionee: mengdw <2016-06-20> add for CR01720823 begin
package com.cydroid.softmanager.softmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

import com.cydroid.softmanager.utils.Log;

public class SecurityDetectionProcessWifiReceiver extends BroadcastReceiver {
    private static final String TAG = "SecurityDetectionProcessWifiReceiver";
    private static final String ACTION_SECURITY_DETECTION_FORGET_WIFI = "com.softmanager.securitydetection.forgetwifi.action";
    private WifiManager mWifiManager;
    private WifiManager.ActionListener mForgetListener;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        createWifiListener();
        if(intent != null && ACTION_SECURITY_DETECTION_FORGET_WIFI.equals(intent.getAction())) {
            Log.d(TAG, "onReceive ACTION_SECURITY_DETECTION_FORGET_WIFI");
            forgetConnectedWifi();
        }
    }
    
    private void forgetConnectedWifi() {
        // Gionee: mengdw <2017-04-14> modify for 113747 begin
        try {
            if (null == mWifiManager) {
                Log.d(TAG, "forgetConnectedWifi mWifiManager is null");
                return;
            }
            WifiInfo connectInfo = mWifiManager.getConnectionInfo();
            if (null == connectInfo) {
                Log.d(TAG, "forgetConnectedWifi error connectInfo is null");
                return;
            }
            int networkID = connectInfo.getNetworkId();
            String ssid = connectInfo.getSSID();
            Log.d(TAG, "forgetWifi networkID=" + networkID + " ssid=" + ssid);
            if (networkID >= 0) {
                mWifiManager.forget(networkID, mForgetListener);
            }
        } catch (Exception e) {
            Log.d(TAG, "forgetConnectedWifi e=" + e.toString());
        }
        // Gionee: mengdw <2017-04-14> modify for 113747 end
    }
    
    private void createWifiListener() {
        mForgetListener = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "forgetWifi onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "forgetWifi onFailure reason=" + reason);
            }
        };
    }
}
//Gionee: mengdw <2016-06-20> add for CR01720823 end