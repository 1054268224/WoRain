package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.content.ContentResolver;
import android.content.Context;

public class PowerModeItemAutoSync extends PowerModeItem {

    public PowerModeItemAutoSync(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private boolean isMasterSyncOn() {
        return ContentResolver.getMasterSyncAutomatically();
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isMasterSyncOn());
    }

    /* sync controller */
    private void setMasterSyncAutomatically(boolean enabled) {
        Log.d(TAG, "->setMasterSyncAutomatically(), " + enabled);
        ContentResolver.setMasterSyncAutomatically(enabled);
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, true);
        setMasterSyncAutomatically(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
        return isMasterSyncOn() != configVal;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return -powerConfig.sync_weight;
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
        return mContext.getResources().getString(R.string.close_sync);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isMasterSyncOn()));
    }
}
