package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

import com.cydroid.softmanager.R;

import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.content.Context;

public class PowerModeItemWifiAp extends PowerModeItem {

    private final Handler mHandler;
    private final OnStartTetheringCallback mEtherCallback;

    public PowerModeItemWifiAp(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
        mHandler = new Handler();
        mEtherCallback = new OnStartTetheringCallback();
    }

    private WifiManager getWifiManager() {
        return (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    private void setWifiApState(boolean enabled) {
        boolean isOn = isWifiApOn();
        WifiManager wifiManager = getWifiManager();
        ConnectivityManager manger = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (enabled) {
            //guoxt  20180717 add for Ptest begin
           // wifiManager.setWifiApEnabled(null, true);
            Log.d(TAG, "mWifiManager.setWifiApEnabled ----> true");
            Log.d(TAG, "ConnectivityManager.startTethering ----> true");
            manger.startTethering(ConnectivityManager.TETHERING_WIFI, true, mEtherCallback, mHandler);
        } else {
            manger.stopTethering(ConnectivityManager.TETHERING_WIFI);
           // wifiManager.setWifiApEnabled(null, false);
            //guoxt  20180717 add for Ptest end
            Log.d(TAG, "mWifiManager.setWifiApEnabled ----> false");
        }
    }

    private boolean isWifiApOn() {
        WifiManager wifiManager = getWifiManager();
        int apState = wifiManager.getWifiApState();
        return apState == WifiManager.WIFI_AP_STATE_ENABLING || apState == WifiManager.WIFI_AP_STATE_ENABLED;
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isWifiApOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        setWifiApState(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
        return isWifiApOn() != configVal;
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("false");
        return res;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.close_wifi_ap_new);
    }

    private final class OnStartTetheringCallback extends ConnectivityManager.OnStartTetheringCallback {
        @Override
        public void onTetheringStarted() {
            Log.d(TAG, "onTetheringStarted ");
        }

        @Override
        public void onTetheringFailed() {
            Log.d(TAG, "onTetheringFailed ");
        }
    }

}
