// Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
package com.cydroid.softmanager.powersaver.mode.item;

import java.util.ArrayList;

import com.cydroid.softmanager.powersaver.mode.ModeItemInfo;
import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import com.cydroid.softmanager.R;

public abstract class PowerModeItem {
    public static final String CLASS_PREFIX = "PowerModeItem";
    private static final String CHECKPOINT = "_CHECKPOINT_";
    private static final String CONFIG = "_CONFIG_";

    protected static final String TAG_PERFIX = "PowerModeItem";

    public static final String SWITCH_PASS = "null";

    public static final String RANGE_SYMBOL = "-";

    protected Context mContext;
    protected PowerServiceProviderHelper mProviderHelper;
    protected String mMode;
    protected String mCheckpointKey;
    protected String mConfigKey;
    protected Runnable mBeforeCallback;
    protected Runnable mAfterCallback;
    protected String TAG;
    private ModeItemInfo mModeItemInfo;

    protected PowerModeItem(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        mContext = context;
        mMode = mode;
        mProviderHelper = providerHelper;
        mCheckpointKey = mMode + CHECKPOINT + getName();
        mConfigKey = mMode + CONFIG + getName();
        TAG = TAG_PERFIX + "/" + getName();
        mModeItemInfo = new ModeItemInfo(getName());
        // Log.d(TAG, "create a " + getName());
    }

    protected PowerModeItem(Context context) {
        mContext = context;
    }

    public void store() {
        Log.d(TAG, "store current setting to key:" + mConfigKey);
        saveCurrentToPreference(mConfigKey);
    }

    public void save() {
        Log.d(TAG, "save current setting to key:" + mCheckpointKey);
        saveCurrentToPreference(mCheckpointKey);
    }

    public boolean restoreWithCallback(boolean isIgnoreExternalChange) {
        if (mBeforeCallback != null) {
            mBeforeCallback.run();
        }
        boolean res = restore(isIgnoreExternalChange);
        if (mAfterCallback != null) {
            mAfterCallback.run();
        }
        return res;
    }

    public boolean restore(boolean isIgnoreExternalChange) {
        if (hasCheckpoint() && (isIgnoreExternalChange || !isCurrentSettingsChangedByExternal(mConfigKey))) {
            Log.d(TAG, "restore setting from key:" + mCheckpointKey);
            return restoreFromSavedPreference(mCheckpointKey);
        } else {
            Log.d(TAG, "current settings changed from external,do nothing");
            return true;
        }
    }

    public void set(String value) {
        if (value == null) {
            value = SWITCH_PASS;
        }
        if (!value.equals(SWITCH_PASS)) {
            Log.d(TAG, "set val=" + value + " setting to key:" + mConfigKey);
            if (PowerModeUtils.isNumeric(value)) {
                mProviderHelper.putInt(mConfigKey, Integer.valueOf(value));
            } else if (value.equals("true")) {
                mProviderHelper.putBoolean(mConfigKey, true);
            } else if (value.equals("false")) {
                mProviderHelper.putBoolean(mConfigKey, false);
            } else {
                mProviderHelper.putString(mConfigKey, value);
            }
        } else {
            Log.d(TAG, "remove setting  key:" + mConfigKey);
            mProviderHelper.removeKey(mConfigKey);
        }
    }

    public String getConfigStr() {
        if (!hasConfig()) {
            Log.d(TAG, "no settings found in " + mConfigKey + " ,return empty");
            return SWITCH_PASS;
        }
        String res = mProviderHelper.getValueStr(mConfigKey);
        if (res == null) {
            return SWITCH_PASS;
        } else {
            return res;
        }
    }

    public ArrayList<String> getCandidateValues() {
        ArrayList<String> res = getAvailCandidateValues();
        if (res == null || res.size() == 0) {
            return new ArrayList<String>();
        }
        res.add(SWITCH_PASS);
        return res;
    }

    private String getConfigValStr(String configVal) {
        if (configVal.equals(PowerModeItem.SWITCH_PASS)) {
            return mContext.getResources().getString(R.string.mode_item_switch_pass);
        } else if (configVal.equals("true")) {
            return mContext.getResources().getString(R.string.mode_item_switch_on);
        } else if (configVal.equals("false")) {
            return mContext.getResources().getString(R.string.mode_item_switch_off);
        }
        return configVal;
    }

    public ArrayList<String> getCandidateValueDescs() {
        ArrayList<String> vals = getCandidateValues();
        if (vals == null || vals.size() == 0) {
            return new ArrayList<String>();
        }
        ArrayList<String> res = new ArrayList<String>();
        for (String val : vals) {
            res.add(getConfigValStr(val));
        }
        return res;
    }

    public boolean applyWithCallback() {
        if (mBeforeCallback != null) {
            mBeforeCallback.run();
        }
        boolean res = apply();
        if (mAfterCallback != null) {
            mAfterCallback.run();
        }
        return res;
    }

    public boolean apply() {
        if (!hasConfig()) {
            Log.d(TAG, "no settings found in " + mConfigKey + " ,do nothing");
            return true;
        }
        Log.d(TAG, "apply setting from key:" + mConfigKey);
        return restoreFromSavedPreference(mConfigKey);
    }

    public float getCalculatedWeight(PowerConfig powerConfig) {
        if (hasConfig()) {
            return getWeightForConfig(powerConfig);
        } else {
            return 0;
        }
    }

    public boolean hasCheckpoint() {
        return mProviderHelper.hasKey(mCheckpointKey);
    }

    public boolean hasConfig() {
        return mProviderHelper.hasKey(mConfigKey);
    }

    public float getWeightForConfig(PowerConfig powerConfig) {
        if (!hasConfig()) {
            Log.d(TAG, "no settings found in " + mConfigKey + " ,return 0");
            return 0;
        }
        String res = mProviderHelper.getValueStr(mConfigKey);
        return getWeightForValue(powerConfig, res);
    }

    public float getWeightForCurrent(PowerConfig powerConfig) {
        return 0f;
    }

    public String getName() {
        String className = getClass().getSimpleName();
        if (className.length() <= "PowerModeItem".length()) {
            return "";
        }
        return className.substring("PowerModeItem".length());
    }

    protected abstract void saveCurrentToPreference(String saveKey);

    protected abstract boolean restoreFromSavedPreference(String saveKey);

    protected abstract boolean isCurrentSettingsChangedByExternal(String compareConfigKey);

    public float getWeightForValue(PowerConfig powerConfig, String val) {
        return 0f;
    }

    protected ArrayList<String> getAvailCandidateValues() {
        return null;
    }

    public String getTitle() {
        return "";
    }

    public String getFormat() {
        return "";
    }

    public String getSummary() {
        return "";
    }

    public void setBeforeCallback(Runnable callback) {
        this.mBeforeCallback = callback;
    }

    public void setAfterCallback(Runnable callback) {
        this.mAfterCallback = callback;
    }

    public ModeItemInfo getModeItemInfo() {
        ModeItemInfo res = new ModeItemInfo(mModeItemInfo.name);
        res.configVal = getConfigStr();
        res.defaultVal = getDefaultValue();
        res.candidateVals = getCandidateValues();
        res.candidateValDecs = getCandidateValueDescs();
        res.title = getTitle();
        res.summary = getSummary();
        res.format = getFormat();
        return res;
    }

    public boolean isFunctionAvailable() {
        return true;
    }

    public void setDefaultValue(String defaultVal) {
        mModeItemInfo.defaultVal = defaultVal;
    }

//    public void setDisplayPriority(int displayPriority) {
//        mModeItemInfo.displayPriority = displayPriority;
//    }

    public void resetToDefaultValue() {
        if (!mModeItemInfo.defaultVal.equals(PowerModeItem.SWITCH_PASS)) {
            set(mModeItemInfo.defaultVal);
        }
    }

    public boolean isConfigEqualToDefaultValue() {
        return getConfigStr().equals(mModeItemInfo.defaultVal);
    }

    public String getDefaultValue() {
        return mModeItemInfo.defaultVal;
    }

    public void setModeItemInfo(ModeItemInfo info) {
        mModeItemInfo.configVal = info.configVal;
        set(mModeItemInfo.configVal);
    }

}
// Gionee <yangxinruo> <2016-3-18> add for CR01654969 end