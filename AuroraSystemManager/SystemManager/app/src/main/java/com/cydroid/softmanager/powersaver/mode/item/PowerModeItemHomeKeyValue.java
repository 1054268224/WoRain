package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;

import cyee.provider.CyeeSettings;

public class PowerModeItemHomeKeyValue extends PowerModeItem {

    public PowerModeItemHomeKeyValue(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private int getHomeKeyValue() {
        return CyeeSettings.getInt(mContext.getContentResolver(), "home_key_value", 0);
    }

    private void setHomeKeyValue(int homeValue) {
        Log.d(TAG, "----> setHomeKeyValue val=" + homeValue);
        if (getHomeKeyValue() == homeValue) {
            return;
        }
        try {
            CyeeSettings.putInt(mContext.getContentResolver(), "home_key_value", homeValue);
        } catch (Exception e) {
            Log.e(TAG, "could not setHomeKeyValue", e);
        }
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putInt(saveKey, getHomeKeyValue());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        setHomeKeyValue(mProviderHelper.getInt(saveKey, 0));
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        int configVal = mProviderHelper.getInt(compareConfigKey, 0);
        return getHomeKeyValue() != configVal;
    }

}
