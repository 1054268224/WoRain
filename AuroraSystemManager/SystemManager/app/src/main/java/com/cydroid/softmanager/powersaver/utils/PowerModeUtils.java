package com.cydroid.softmanager.powersaver.utils;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import com.cydroid.framework.FrameworkUtility;
import com.cydroid.softmanager.utils.Log;

import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

/*
 * 获取 ＆ 设置 手机省电模式
 */
public class PowerModeUtils {

    private static final String TAG = "PowerModeUtils";

    public static int getCurrentMode(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), PowerConsts.POWER_MODE,
                PowerConsts.NONE_MODE);
    }

    public static void setCurrentMode(Context context, int currentMode) {
        Log.d(TAG, "set Power Mode " + currentMode + " flag in system provider");
        Settings.Global.putInt(context.getContentResolver(), PowerConsts.POWER_MODE, currentMode);
    }

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    public static int popModeFromStack(Context context) {
        String stackStr = Settings.Global.getString(context.getContentResolver(),
                PowerConsts.POWER_MODE_STACK);
        if (stackStr == null)
            stackStr = "";
        Log.v(TAG, "popModeFromStack,mode stack = [" + stackStr + "]");
        if (!stackStr.isEmpty()) {
            int index = stackStr.indexOf(",");
            if (index < 0) {
                Settings.Global.putString(context.getContentResolver(), PowerConsts.POWER_MODE_STACK, "");
                Log.v(TAG, "popModeFromStack save,mode stack = []");
                return Integer.parseInt(stackStr);
            } else {
                Settings.Global.putString(context.getContentResolver(), PowerConsts.POWER_MODE_STACK,
                        stackStr.substring(index + 1));
                Log.v(TAG, "popModeFromStack save,mode stack = [" + stackStr.substring(index + 1) + "]");
                return Integer.parseInt(stackStr.substring(0, index));
            }
        }
        return -1;
    }

    public static void pushModeToStack(Context context, int lastMode) {
        String stackStr = Settings.Global.getString(context.getContentResolver(),
                PowerConsts.POWER_MODE_STACK);
        if (stackStr == null)
            stackStr = "";
        Log.v(TAG, "pushModeToStack,mode stack = [" + stackStr + "]");
        if (stackStr.isEmpty()) {
            Settings.Global.putString(context.getContentResolver(), PowerConsts.POWER_MODE_STACK,
                    String.valueOf(lastMode));
            Log.v(TAG, "pushModeToStack save,mode stack = [" + lastMode + "]");
        } else {
            Settings.Global.putString(context.getContentResolver(), PowerConsts.POWER_MODE_STACK,
                    lastMode + "," + stackStr);
            Log.v(TAG, "pushModeToStack save,mode stack = [" + lastMode + "," + stackStr + "]");
        }
    }

    public static Stack<Integer> getModeToStack(Context context) {
        String stackStr = Settings.Global.getString(context.getContentResolver(),
                PowerConsts.POWER_MODE_STACK);
        Stack<Integer> res = new Stack<Integer>();
        if (stackStr == null) {
            return res;
        }
        String[] listStr = stackStr.split(",");
        for (int i = 0; i < listStr.length; i++) {
            res.push(Integer.parseInt(listStr[i]));
        }
        return res;
    }

    public static void resetModeToStack(Context context) {
        Settings.Global.putString(context.getContentResolver(), PowerConsts.POWER_MODE_STACK, "");
    }
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end

    // Gionee <yangxinruo> <2015-10-08> add for CR01563463 begin
    public static boolean getCurrentShouldInDarktheme(Context context, boolean defaultVal) {
        String res = Settings.Global.getString(context.getContentResolver(),
                PowerConsts.CURRENT_SHOULD_IN_DARKTHEME);
        if (res != null) {
            if (res.equals("off"))
                return false;
            else if (res.equals("on"))
                return true;
        }
        return defaultVal;
    }

    public static void setCurrentShouldInDarktheme(Context context, boolean isInDark) {
        String val = "off";
        if (isInDark)
            val = "on";
        Settings.Global.putString(context.getContentResolver(), PowerConsts.CURRENT_SHOULD_IN_DARKTHEME, val);
    }

    // Gionee <yangxinruo> <2015-10-08> add for CR01563463 end

    // Gionee <yangxinruo> <2015-09-11> modify for CR01551611 begin
    // Gionee <yangxinruo> <2015-08-13> add for CR01537130 begin
    // Gionee <yangxinruo> <2015-10-10> modify for CR01565117 begin
    public static int getModeProcessing(Context context) {
        // ProviderHelper providerHelper = new ProviderHelper(context);
        // return providerHelper.getInt(PowerConsts.POWER_MODE_PROCESSING, PowerConsts.DONE);
        // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
        return Settings.Global.getInt(context.getContentResolver(), PowerConsts.POWER_MODE_PROCESSING,
                PowerConsts.SUPER_MODE_DONE);
        // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end
    }

    public static void setModeProcessing(Context context, int currentState) {
        // ProviderHelper providerHelper = new ProviderHelper(context);
        // providerHelper.putInt(PowerConsts.POWER_MODE_PROCESSING, currentState);
        Log.d(TAG, "set POWER_MODE_PROCESSING flag in system provider");
        Settings.Global.putInt(context.getContentResolver(), PowerConsts.POWER_MODE_PROCESSING, currentState);
    }

    // Gionee <yangxinruo> <2015-10-10> modify for CR01565117 end
    // Gionee <yangxinruo> <2015-08-13> add for CR01537130 end
    // Gionee <yangxinruo> <2015-09-11> modify for CR01551611 end
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end

    public static void removeTask(ActivityManager am, int taskId) {
        if (Build.VERSION.SDK_INT >= 22) {
            Log.d(TAG, "remove task task-id: " + taskId);
            FrameworkUtility.invokeMethod(ActivityManager.class, am, "removeTask", new Class[] {int.class},
                    new Object[] {(int) taskId});
        } else {
            Log.d(TAG, "remove task task-id(old version) : " + taskId);
            FrameworkUtility.invokeMethod(ActivityManager.class, am, "removeTask",
                    new Class[] {int.class, int.class}, new Object[] {(int) taskId, 0});
        }
    }

    public static void setSuperModeExceptionRebootFlag(Context context) {
        Log.d(TAG, "setSuperModeExceptionRebootFlag 1");
        Settings.Global.putInt(context.getContentResolver(), PowerConsts.SUPER_MODE_EXCEPTION_REBOOT, 1);
    }

    public static void unsetSuperModeExceptionRebootFlag(Context context) {
        Log.d(TAG, "remove SuperModeExceptionRebootFlag");
        Settings.Global.putInt(context.getContentResolver(), PowerConsts.SUPER_MODE_EXCEPTION_REBOOT, 0);
    }

    public static boolean isSuperModeExceptionRebooted(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), PowerConsts.SUPER_MODE_EXCEPTION_REBOOT,
                0) > 0;
    }
}
