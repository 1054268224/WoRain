package com.cydroid.softmanager.powersaver.utils;

import com.cydroid.softmanager.utils.Log;
import android.content.Context;
import android.app.StatusBarManager;

import cyee.provider.CyeeSettings;

public class StatusbarController {
    private static int mStatusBarFlag = StatusBarManager.DISABLE_NONE;
    private static final String CYEE_SETTING_CC_SWITCH = "control_center_switch";
    private static final boolean DEBUG = true;
    private static final String TAG = "StatusbarController";

    public static void enableStatusbar(Context context) {
        int enable_value = 1;
        setControlCenterSwitch(context, enable_value);
        // setStatusbarState(context, StatusBarManager.DISABLE_NONE);
        setSatusBarExpand(context, true);
    }

    public static void disableStatusbar(Context context) {
        int disable_value = 0;
        setControlCenterSwitch(context, disable_value);
        // setStatusbarState(context, StatusBarManager.DISABLE_EXPAND);
        setSatusBarExpand(context, false);
    }

    // Gionee <yangxinruo> <2015-09-06> modify for CR01548328 begin
    public static void setControlCenterSwitch(Context context, int value) {
//        Log.d(DEBUG, TAG, "updateDatabase in StatusbarController--->" + value + " ,ControlCenter "
//                + (value == 0 ? "disable" : "enable"));
//        CyeeSettings.putInt(context.getContentResolver(), CYEE_SETTING_CC_SWITCH, value);
    }

    // Gionee <yangxinruo> <2015-09-06> modify for CR01548328 end

    private static void setSatusBarExpand(Context context, boolean isEnable) {
//        if (isEnable) {
//            mStatusBarFlag &= ~StatusBarManager.DISABLE_EXPAND;
//        } else {
//            mStatusBarFlag |= StatusBarManager.DISABLE_EXPAND;
//        }
//        setStatusbarState(context, mStatusBarFlag);
    }

    public static void setStatusbarState(Context context, int state) {
//        Log.d(DEBUG, TAG, "setStatusbarState in StatusbarController ------->" + state + " ,Statusbar "
//                + (state == 0 ? "enable expand" : "disable expand"));
//        StatusBarManager barManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
//        barManager.disable(state);
    }

    public static void enableOnlyStatusbar(Context context) {
        Log.d(DEBUG, TAG,
                "enableOnlyStatusbar in StatusbarController ------->" + StatusBarManager.DISABLE_NONE);
        setStatusbarState(context, StatusBarManager.DISABLE_NONE);
    }

    public static void disableOnlyStatusbar(Context context) {
        Log.d(DEBUG, TAG,
                "disableOnlyStatusbar in StatusbarController ------->" + StatusBarManager.DISABLE_EXPAND);
        setStatusbarState(context, StatusBarManager.DISABLE_EXPAND);
    }

    public static void enableControlCenter(Context context) {
//        int enable_value = 1;
//        setControlCenterSwitch(context, enable_value);
    }

    public static void disableControlCenter(Context context) {
//        int disable_value = 0;
//        setControlCenterSwitch(context, disable_value);
    }
}
