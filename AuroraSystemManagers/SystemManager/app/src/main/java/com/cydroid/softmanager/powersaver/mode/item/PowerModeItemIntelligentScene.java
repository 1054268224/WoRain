package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.io.File;
import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.content.Context;
import android.os.SystemProperties;

public class PowerModeItemIntelligentScene extends PowerModeItem {

    private static final String PATH_LIBTSPOWER_SO = "/system/lib/libtspower.so";
    public static final int INTELLIGENT_SCENE_CLOSE = 0;
    public static final int INTELLIGENT_SCENE_OPEN = 1;

    public PowerModeItemIntelligentScene(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        // tspower alwayls save off ,because no other access could control it
        mProviderHelper.putBoolean(saveKey, false);
    }

    private boolean isIntelligentSceneOn() {
        if (!isFunctionAvailable()) {
            return false;
        }
        String val = SystemProperties.get("persist.sys.dimoutswitch");
        if (val == null || val.isEmpty()) {
            val = SystemProperties.get("persist.sys.dimoutnews_ue");
        }
        if (val == null || val.isEmpty()) {
            val = SystemProperties.get("persist.sys.dimoutgpustatus");
        }
        if (val == null || val.isEmpty()) {
            return false;
        }

        return !val.equals("0");
    }

    @Override
    public boolean isFunctionAvailable() {
        File tspowerLibFile = new File(getTsPowerLibPath());
        return tspowerLibFile.exists();
    }

    private String getTsPowerLibPath() {
        return PATH_LIBTSPOWER_SO;
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
        setIntelligentScene(configVal);
        return true;
    }

    private void setIntelligentScene(boolean enable) {
        if (!isFunctionAvailable()) {
            return;
        }
        Log.d(TAG, "---->setIntelligentScene enable = " + enable);
        if (enable) {
            openIntelligentScene();
        } else {
            closeIntelligentScene(INTELLIGENT_SCENE_CLOSE);
        }
    }

    private void openIntelligentScene() {
        // Gionee <yangxinruo> <2015-12-25> add for CR01614312 begin
        // main switch on
        SystemProperties.set("persist.sys.dimoutswitch", String.valueOf(INTELLIGENT_SCENE_OPEN));
        // Gionee <yangxinruo> <2015-12-25> add for CR01614312 end

        SystemProperties.set("persist.sys.dimoutgpustatus", String.valueOf(INTELLIGENT_SCENE_OPEN));

        SystemProperties.set("persist.sys.dimoutgame", "" + 2);
        SystemProperties.set("persist.sys.dimoutmap", "" + 2);
        SystemProperties.set("persist.sys.dimoutbrowser", "" + 2);
        SystemProperties.set("persist.sys.dimoutsocial", "" + 2);
        SystemProperties.set("persist.sys.dimoutreader", "" + 2);
        SystemProperties.set("persist.sys.dimoutnews", "" + 2);

        SystemProperties.set("persist.sys.dimoutbuyer", "" + 2);
        SystemProperties.set("persist.sys.dimoutvideo", "" + 2);

        SystemProperties.set("persist.sys.dimoutbrowser_ue", "" + 1);
        SystemProperties.set("persist.sys.dimoutsocial_ue", "" + 1);
        SystemProperties.set("persist.sys.dimoutnews_ue", "" + 1);

        SystemProperties.set("persist.sys.dimoutbuyer_ue", "" + 1);
    }

    private void closeIntelligentScene(int level) {
        SystemProperties.set("persist.sys.dimoutgame", "" + level);
        SystemProperties.set("persist.sys.dimoutmap", "" + level);
        SystemProperties.set("persist.sys.dimoutbrowser", "" + level);
        SystemProperties.set("persist.sys.dimoutsocial", "" + level);
        SystemProperties.set("persist.sys.dimoutreader", "" + level);
        SystemProperties.set("persist.sys.dimoutnews", "" + level);

        SystemProperties.set("persist.sys.dimoutbuyer", "" + level);
        SystemProperties.set("persist.sys.dimoutvideo", "" + level);

        SystemProperties.set("persist.sys.dimoutbrowser_ue", "" + 0);
        SystemProperties.set("persist.sys.dimoutsocial_ue", "" + 0);
        SystemProperties.set("persist.sys.dimoutnews_ue", "" + 0);

        SystemProperties.set("persist.sys.dimoutbuyer_ue", "" + 0);

        SystemProperties.set("persist.sys.dimoutgpustatus", String.valueOf(INTELLIGENT_SCENE_CLOSE));

        // Gionee <yangxinruo> <2015-12-25> add for CR01614312 begin
        // main switch off
        SystemProperties.set("persist.sys.dimoutswitch", String.valueOf(INTELLIGENT_SCENE_CLOSE));
        // Gionee <yangxinruo> <2015-12-25> add for CR01614312 end
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return false;
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isIntelligentSceneOn()));
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return powerConfig.intelligent_scene_weight_open;
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
        return mContext.getResources().getString(R.string.daily_power_Intelligent_scene_title);
    }

}
