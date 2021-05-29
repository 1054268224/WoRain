package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.CpuInfoUtils;
import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import com.cydroid.softmanager.R;
import android.content.Context;

public class PowerModeItemCpuLimit extends PowerModeItem {
    public PowerModeItemCpuLimit(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    /* cpu core controller */
    private void recoverCpuCore() {
        Log.d(TAG, "----->recoverCpuCore(), recover cpu core");
        CpuInfoUtils.limitMutliCore(false);
    }

    private void decreaseCpuCore() {
        Log.d(TAG, "----->decreaseCpuCore(), decrease cpu core");
        CpuInfoUtils.limitMutliCore(true);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(mConfigKey, false);
        mProviderHelper.putBoolean(saveKey, configVal);
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        if (configVal) {
            decreaseCpuCore();
        } else {
            recoverCpuCore();
        }
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return false;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return powerConfig.cpufreq_weight;
        } else {
            return 0f;
        }
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.set_cpu_state);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        boolean configVal = mProviderHelper.getBoolean(mConfigKey, false);
        return getWeightForValue(powerConfig, String.valueOf(configVal));
    }

}
