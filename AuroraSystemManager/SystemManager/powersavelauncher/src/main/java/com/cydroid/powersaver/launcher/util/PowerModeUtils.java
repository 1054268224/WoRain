package com.cydroid.powersaver.launcher.util;

import android.provider.Settings;
import android.content.Context;

/*
 * 获取 ＆ 设置 手机省电模式
 */
public class PowerModeUtils {

    private static final String TAG = "PowerModeUtils";

    public static final String POWER_MODE = "cyee_powermode";
    public static final int NONE_MODE = 0;
    public static final int NORMAL_MODE = 1;
    public static final int SUPER_MODE = 2;

    public static int getCurrentMode(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), POWER_MODE, NONE_MODE);
    }

    public static void setCurrentMode(Context context, int currentMode) {
        Log.d(TAG, "set Power Mode " + currentMode + " flag in system provider");
        Settings.Global.putInt(context.getContentResolver(), POWER_MODE, currentMode);
    }
}
