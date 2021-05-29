package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.powersaver.utils.HotKnotUtils;
import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

import com.cydroid.softmanager.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.SystemProperties;
import android.widget.RemoteViews;

public class PowerModeItemGreenBackground extends PowerModeItem {

    private boolean mIsInited = false;

    public PowerModeItemGreenBackground(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private WhiteListManager getAndInitWhiteListManager() {
        WhiteListManager whiteListManager = WhiteListManager.getInstance();
        if (!mIsInited) {
            whiteListManager.init(mContext);
            mIsInited = true;
        }
        return whiteListManager;
    }

    private boolean isGreenBackgroundOn() {
        boolean res = getAndInitWhiteListManager().isGreenBackgroundEnable();
        Log.d(TAG, "get green bg state=" + res);
        return res;
    }

    private void setGreenBackgroundState(boolean enabled) {
        getAndInitWhiteListManager().setGreenBackgroundEnable(enabled);
        Log.d(TAG, "set green bg state=" + enabled);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isGreenBackgroundOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, true);
        setGreenBackgroundState(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, true);
        return isGreenBackgroundOn() != configVal;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return powerConfig.green_background_weight;
        } else {
            return 0f;
        }
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("true");
        return res;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.set_greenbackground);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isGreenBackgroundOn()));
    }

    @Override
    public boolean isFunctionAvailable() {
        // just for payprotected
        return SystemProperties.get("ro.gn.app.securepay.support", "no").equals("no");
    }
}
