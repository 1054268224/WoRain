package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;

import cyee.changecolors.ChameleonColorManager;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;

public class PowerModeItemDarkTheme extends PowerModeItem {
    public static final String DARK_THEME_NOT_CHANGE = "com.cydroid.softmanager.powersaver.mode.item.DARK_THEME_NOT_USE";
    public static final String CHAMELEON_CHANGE_COLOR = "cyee.intent.action.chameleon.CHANGE_COLOR";

    public PowerModeItemDarkTheme(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private boolean isShouldUseDarkTheme(boolean defaultVal) {
        return PowerModeUtils.getCurrentShouldInDarktheme(mContext, defaultVal);
    }

    private boolean isChameleonCurrentUseDarkTheme() {
        Log.d(TAG,
                "ChameleonColorManager.isPowerSavingMode() --->" + ChameleonColorManager.isPowerSavingMode());
        Log.d(TAG,
                "ChameleonColorManager.isNeedChangeColor() --->" + ChameleonColorManager.isNeedChangeColor());
        return ChameleonColorManager.isPowerSavingMode() && ChameleonColorManager.isNeedChangeColor();
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
//        mProviderHelper.putBoolean(saveKey, isUseDarkTheme());
// dark theme alwayls off when status saved
        mProviderHelper.putBoolean(saveKey, false);
    }

    private void setDarkTheme(boolean enabled) {
        setDarkTheme(enabled, false);
    }

    private void setDarkTheme(boolean enabled, boolean isForce) {
        PowerModeUtils.setCurrentShouldInDarktheme(mContext, enabled);
        if (!isForce && isChameleonCurrentUseDarkTheme() == enabled) {
            Log.d(TAG, "ChameleonColorManager already in status " + isChameleonCurrentUseDarkTheme());
            Intent intent = new Intent(DARK_THEME_NOT_CHANGE);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            Log.d(TAG, "setDarkTheme()  notify status not change immedatelly");
            mContext.sendBroadcast(intent);
        }
        Log.d(TAG, "setDarkTheme() sendBroadcast set is_power_saving_mode--> " + enabled);
        Intent intent = new Intent("cyee.intent.action.chameleon.POWER_SAVING_MODE");
		//Chenyee guoxt modify for target 26 begin
        intent.setComponent(new ComponentName("com.cydroid.change",
                "com.cyee.chameleon.ThemeChangedReceiver"));
		//Chenyee guoxt modify for target end
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("is_power_saving_mode", enabled);
        mContext.sendBroadcast(intent);
    }

    @Override
    public boolean restore(boolean isForceRestore) {
        if (!hasCheckpoint()) {
            Log.d(TAG, "checkpoint info lost,force to set false");
            saveCurrentToPreference(mCheckpointKey);
        }
        Log.d(TAG, "restore setting from key:" + mCheckpointKey);
        return restoreFromSavedPreference(mCheckpointKey);
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        setDarkTheme(configVal);
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
            return powerConfig.darktheme_weight;
        } else {
            return 0f;
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
    public String getTitle() {
        return mContext.getResources().getString(R.string.set_darktheme);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig,
                String.valueOf(isShouldUseDarkTheme(false) && isChameleonCurrentUseDarkTheme()));
    }

    public void emergencyRestore(boolean isShouldInDarkTheme) {
        Log.d(TAG, "emergencyRestore reset to " + isShouldInDarkTheme);
        setDarkTheme(isShouldInDarkTheme, true);
    }

    @Override
    public boolean isFunctionAvailable() {
        return !UiUtils.isSpecialStyleModel();
    }

}
