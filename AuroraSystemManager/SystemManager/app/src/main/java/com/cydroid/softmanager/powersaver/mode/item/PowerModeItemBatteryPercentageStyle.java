package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class PowerModeItemBatteryPercentageStyle extends PowerModeItem {

    public PowerModeItemBatteryPercentageStyle(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private int getBatteryStyle() {
        return Settings.Secure.getInt(mContext.getContentResolver(), "battery_percentage", 0);
    }

    private void setBatteryStyle(int value) {
        try {
            Settings.Secure.putInt(mContext.getContentResolver(), "battery_percentage", value);
            // Post the intent
            Intent intent = new Intent("mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH");
            intent.putExtra("state", value);
            Log.d(TAG, "sendBroadcast battery percentage switch");
            mContext.sendBroadcast(intent);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist battery display style setting", e);
        }
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putInt(saveKey, getBatteryStyle());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        setBatteryStyle(mProviderHelper.getInt(saveKey, 0));
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        int configVal = mProviderHelper.getInt(compareConfigKey, 0);
        return getBatteryStyle() != configVal;
    }

}
