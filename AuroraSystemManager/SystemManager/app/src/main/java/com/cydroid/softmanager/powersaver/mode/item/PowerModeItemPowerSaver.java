package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;

public class PowerModeItemPowerSaver extends PowerModeItem {
    public PowerModeItemPowerSaver(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private boolean isPowerSaverOn() {
//        int mode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE,
//                Settings.Secure.LOCATION_MODE_OFF);

        int mode = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Global.LOW_POWER_MODE_TRIGGER_LEVEL, 0);

        Log.d(TAG, "isPowerSaverOn Get power state val=" + mode);
        return mode != 0;
    }

    private void setPowerSaverState(boolean enabled) {
        boolean isOn = isPowerSaverOn();
        Log.d("guoxt1:", "setPowerSaverState state val=" + enabled);
        if (enabled) {
            Log.d("guoxt2:", "setPowerSaverState enabled=" + enabled);
            Log.d("guoxt3:", "isOn=" + isOn);
            //if (!isOn) {
                Log.d("guoxt4:", "isOn=" + isOn);
                mContext.getSystemService(PowerManager.class).setPowerSaveModeEnabled(true);
                Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.LOW_POWER_WARNING_ACKNOWLEDGED, 1);
           // }
        } else {
            //if (isOn) {
                mContext.getSystemService(PowerManager.class).setPowerSaveModeEnabled(false);
                Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.LOW_POWER_WARNING_ACKNOWLEDGED,
                        0);
           // }
        }
    }


    @Override
    public ArrayList<String> getCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("true");
        res.add("false");
        return res;
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isPowerSaverOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        setPowerSaverState(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
//        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
//        return  configVal;
        return  false;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return -powerConfig.gps_weight;
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
        return mContext.getResources().getString(R.string.close_power);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isPowerSaverOn()));
    }
}
