package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.content.Context;
import android.provider.Settings;

public class PowerModeItemScreenBrightness extends PowerModeItem {

    private static final int CONFIGTYPE_MODE = 1;
    private static final int CONFIGTYPE_BRIGHTNESS = 2;

    public PowerModeItemScreenBrightness(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private int getBrightnessValue() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 128);
    }

    @Override
    public void save() {
        int bMode = getBrightnessMode();
        Log.d(TAG, "save current BrightnessMode " + bMode + " setting to key:" + mCheckpointKey
                + CONFIGTYPE_MODE);
        mProviderHelper.putInt(mCheckpointKey + CONFIGTYPE_MODE, bMode);
        int bValue = getBrightnessValue();
        Log.d(TAG, "save current BrightnessValue " + bValue + " setting to key:" + mCheckpointKey
                + CONFIGTYPE_BRIGHTNESS);
        mProviderHelper.putInt(mCheckpointKey + CONFIGTYPE_BRIGHTNESS, getBrightnessValue());
    }

    @Override
    public boolean restore(boolean isForceRestore) {

        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_BRIGHTNESS)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_BRIGHTNESS))) {
            Log.d(TAG, "restore setting from key:" + mCheckpointKey + CONFIGTYPE_BRIGHTNESS);
            setBrightness(mProviderHelper.getInt(mCheckpointKey + CONFIGTYPE_BRIGHTNESS, 128));
        }
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_MODE)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_MODE))) {
            Log.d(TAG, "restore setting from key:" + mCheckpointKey + CONFIGTYPE_MODE);
            setBrightnessMode(mProviderHelper.getInt(mCheckpointKey + CONFIGTYPE_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL));
        }
        return true;
    }

    private boolean isCurrentSettingsChangedByExternal(int configtype) {
        if (!hasConfig()) {
            return true;
        }
        int conVal = mProviderHelper.getInt(mConfigKey, 70);
        switch (configtype) {
            case CONFIGTYPE_BRIGHTNESS:
                return getBrightnessValue() != conVal;
            case CONFIGTYPE_MODE:
                return isBrightnessAutomaticMode();
            default:
                return true;
        }
    }

    private void setBrightness(int configVal) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, configVal);
    }

    @Override
    public boolean apply() {
        if (!hasConfig()) {
            Log.d(TAG, "no settings " + mConfigKey + " ,do nothing");
            return true;
        }
        Log.d(TAG, "apply setting from key:" + mConfigKey);
        int conVal = mProviderHelper.getInt(mConfigKey, 70);
        setBrightnessMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        setBrightness(conVal);
        return true;
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        // mProviderHelper.putInt(saveKey, getBrightnessValue());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        // int configVal = mProviderHelper.getInt(saveKey, 70);
        // Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
        // configVal);
        // return false;
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return true;
    }

//    private boolean isCurrentSettingsChangedByExternal(int configtype) {
//        if (!hasConfig())
//            return true;
//        int conVal = mProviderHelper.getInt(mConfigKey, 70);
//        switch (configtype) {
//            case CONFIGTYPE_MODE:
//                return getBrightnessMode() != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
//            case CONFIGTYPE_BRIGHTNESS:
//                return getBrightnessValue() != conVal;
//            default:
//                return true;
//        }
//
//    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        int configVal = 0;
        try {
            configVal = Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
        float weight = 0.0f;
        int maxBrightnessValue = 255;
        weight = (configVal - powerConfig.original_brightness) * powerConfig.current_per_brightness * 100
                / powerConfig.original_current / maxBrightnessValue;
        return -weight;
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("0");
        res.add(RANGE_SYMBOL);
        res.add("255");
        return res;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.set_custom_screen_light);
    }

    @Override
    public String getFormat() {
        return mContext.getResources().getString(R.string.mode_item_brightness_perfix);
    }

    private int getBrightnessMode() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    private void setBrightnessMode(int mode) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    private boolean isBrightnessAutomaticMode() {
        return getBrightnessMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        String value = "120";
        if (!isBrightnessAutomaticMode()) {
            value = String.valueOf(getBrightnessValue());
        }
        return getWeightForValue(powerConfig, value);
    }
}
