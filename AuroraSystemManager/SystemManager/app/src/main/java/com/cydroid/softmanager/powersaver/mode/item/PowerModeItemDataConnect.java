package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.telephony.TelephonyManager;
import android.content.Context;

public class PowerModeItemDataConnect extends PowerModeItem {

    public PowerModeItemDataConnect(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    public PowerModeItemDataConnect(Context context) {
        super(context);
    }

    /* data connection controller */
    public void setDataConnectionState(boolean enabled) {
        Log.d(TAG, "telManager.setDataEnabled ----> " + enabled);
        TelephonyManager telManager = TelephonyManager.from(mContext);
        telManager.setDataEnabled(enabled);
    }

    public boolean isDataConnectionOn() {
        TelephonyManager telManager = TelephonyManager.from(mContext);
        return telManager.getDataEnabled();
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isDataConnectionOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        setDataConnectionState(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
        return isDataConnectionOn() != configVal;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return -powerConfig.data_weight;
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
        return mContext.getResources().getString(R.string.close_data_connection);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isDataConnectionOn()));
    }

}
