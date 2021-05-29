package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;
import java.io.File;
import java.lang.reflect.Method;

import com.android.settings.MiraVisionJni;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

import cyee.provider.CyeeSettings;

public class PowerModeItemAutoLcmAcl extends PowerModeItem {
    private static final String[] PATHS_OF_LIBMIRAVISION_SO = {"/system/lib/libMiraVision_jni.so",
            "/system/vendor/lib/libMiraVision_jni.so"};
    private static final String MIRAVISION_TEST = "softmanager_miravision_test";

    public PowerModeItemAutoLcmAcl(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    public PowerModeItemAutoLcmAcl(Context context) {
        super(context);
    }

    // Gionee <yangxinruo> <2016-5-17> modify for CR01699612 begin
    public boolean isScreenSaverOn() {
        if (isUseAALSupport()) {
            Log.d(TAG, "isScreenSaverOn use mAALSupport features");
            int func = MiraVisionJni.getAALFunction();
            Log.d(TAG, "mira func=" + func);
            boolean res = false;
            if ((func & MiraVisionJni.AAL_FUNC_CABC) != 0) {
//            if ((~func & MiraVisionJni.getDefaultAALFunction()) == 0) {
                res = true;
            }
            Log.d(TAG, "aal return res=" + res);
            return res;
        } else {
            int value = CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.AUTO_LCM_ACL, 0);
            return value == 1;
        }
    }
    // Gionee <yangxinruo> <2016-5-17> modify for CR01699612 end

    public boolean isUseAALSupport() {
        return SystemProperties.get("ro.mtk.aal.support", "no").equals("yes");
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isScreenSaverOn());
    }

    private void writeDriverNodeValue(Context context, int nodeType, int value) {

        Object pm = (Object) (context.getSystemService("cyeeserver"));
        try {
            Class cls = Class.forName("android.os.cyeeserver.CyeeServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            method.invoke(pm, nodeType, value);
        } catch (RuntimeException re) {
            Log.d(TAG, "->writeDriverNodeValue(), Exception = " + re);
        } catch (Exception ex) {
            Log.d(TAG, "->writeDriverNodeValue(), Exception = " + ex);
        }
    }

    // Gionee <yangxinruo> <2016-5-17> modify for CR01699612 begin
    public void enableAmoledLcm(int value) {
        // driver node id
        int nodeTypeLcmAclBrightness = 0; // "/sys/class/leds/amoled_lcm_acl/brightness"
        if (isUseAALSupport()) {
            Log.d(TAG, "enableAmoledLcm use mAALSupport features val=" + value);
            int func = (value == 1) ? MiraVisionJni.getDefaultAALFunction() : MiraVisionJni.AAL_FUNC_DRE;
            Log.d(TAG, "setAALFunction func=" + func);
            //MiraVisionJni.setAALFunction(func);
        } else {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.AUTO_LCM_ACL, value);
            writeDriverNodeValue(mContext, nodeTypeLcmAclBrightness, value);
        }
    }
    // Gionee <yangxinruo> <2016-5-17> modify for CR01699612 end

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, true);
        if (configVal) {
            enableAmoledLcm(1);
        } else {
            enableAmoledLcm(0);
        }
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, true);
        return isScreenSaverOn() != configVal;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return powerConfig.screen_save_weight;
        } else {
            return 0f;
        }
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isScreenSaverOn()));
    }

    @Override
    public boolean isFunctionAvailable() {
        if (isTestMode(mContext)) {
            Log.d(TAG, "test mode disable this function!");
            return false;
        }
        if (isUseAALSupport()) {
            for (String libPath : getMiraVisionLibPath()) {
                File miravisionLibFile = new File(libPath);
                if (miravisionLibFile.exists()) {
                    Log.d(TAG, "Mira lib " + libPath + " found");
                    return true;
                }
            }
            Log.d(TAG, "no Mira lib found!");
            return false;
        }
        return true;
    }

    private String[] getMiraVisionLibPath() {
        return PATHS_OF_LIBMIRAVISION_SO;
    }

    private boolean isTestMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(), MIRAVISION_TEST, 0) == 1;
    }
}
