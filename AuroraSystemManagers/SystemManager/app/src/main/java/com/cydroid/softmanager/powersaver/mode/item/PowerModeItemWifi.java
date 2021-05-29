package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.net.wifi.WifiManager;
import android.content.Context;

public class PowerModeItemWifi extends PowerModeItem {

    public PowerModeItemWifi(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    public PowerModeItemWifi(Context context) {
        super(context);
    }

    private WifiManager getWifiManager() {
        return (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    /* wifi controller */
    public int getWifiState() {
        WifiManager wifiManager = getWifiManager();
        if (wifiManager == null) {
            return WifiManager.WIFI_STATE_UNKNOWN;
        }
        return wifiManager.getWifiState();
    }

    public void setWifiState(boolean enabled) {
        boolean isOn = isWifiOn();
        WifiManager wifiManager = getWifiManager();
        if (enabled) {
            if (!isOn) {
                wifiManager.setWifiEnabled(true);
                Log.d(TAG, "mWifiManager.setWifiEnabled ----> true");
            }
        } else {
            if (isOn) {
                wifiManager.setWifiEnabled(false);
                Log.d(TAG, "mWifiManager.setWifiEnabled ----> false");
            }
        }
    }

    public boolean isWifiOn() {
        int status = getWifiState();
        return status == WifiManager.WIFI_STATE_ENABLED || status == WifiManager.WIFI_STATE_ENABLING;
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isWifiOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        setWifiState(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
        return isWifiOn() != configVal;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return -powerConfig.wifi_weight;
        } else {
            return 0f;
        }
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("false");
        return res;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.close_wifi_new);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isWifiOn()));
    }

}
