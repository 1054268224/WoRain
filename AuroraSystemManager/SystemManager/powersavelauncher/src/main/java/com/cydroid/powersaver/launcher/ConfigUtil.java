package com.cydroid.powersaver.launcher;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class ConfigUtil {

    public static final String[] GN_FUNC_ARR = new String[]{"0", "1", "2"};
    public static final String SUPPORT_NEW_LAUNCHER_STRING = "ro.cy.power.saving.optimize";
    public static final boolean SUPPORT_NEW_LAUNCHER = SystemProperties.get(SUPPORT_NEW_LAUNCHER_STRING, "no").equals("yes");
    public static final boolean cyBAFlag = SystemProperties.get("ro.cy.custom", "unknown").equals("SOUTH_AMERICA_BLU");
    //Chenyee <bianrong> <2018-1-25> add for SW17W16KR-84 begin
    public static final boolean cyKRFlag = SystemProperties.get("ro.cy.custom", "unknown").equals("KOREA_BOE");
    //Chenyee <bianrong> <2018-1-25> add for SW17W16KR-84 end
    // Chenyee guoxt modify for CSW1703CX-249 begin
    public static final String gnCustom = SystemProperties.get("ro.cy.common.mainboard.prop");
    public static final boolean cyCXFlag = SystemProperties.get("ro.cy.custom", "unknown").equals("XiaoLaJiao");
    public static final boolean cy1703CX = cyCXFlag && gnCustom.equals("CSW1703");
    // Chenyee guoxt modify for CSW1703CX-249 end
    private static SharedPreferences mSharedPreferences;

    private static SharedPreferences getSP(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    /**
     * Save custom functions, open an APP, or launch AppPickerActivity
     *
     * @param context
     * @param position icon index in [0, 1, 2]
     * @param function activityName
     */
    public static void saveCustomization(Context context, int position, String function) {
        getSP(context).edit().putString(Integer.toString(position), function).commit();
    }

    /**
     * Get saved custom functions
     *
     * @param context
     * @return
     */
    public static String[] getFunctions(Context context) {
        String[] functions = new String[GN_FUNC_ARR.length];
        for (int i = 0; i < functions.length; i++) {
            String function = getSP(context).getString(Integer.toString(i), "");
            if (!TextUtils.isEmpty(function)) {
                functions[i] = function;
            } else {
                functions[i] = GN_FUNC_ARR[i];
            }
        }
        return functions;
    }

    /**
     * Determines if the app has been added to the desktop
     *
     * @param context
     * @param activityName
     * @return
     */
    public static boolean activityAdded(Context context, String activityName) {
        String[] functions = getFunctions(context);
        for (String function : functions) {
            if (function.length() > 1 && function.split(":").length > 1) {
                if (function.split(":")[1].contains(activityName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the list of system applications that can be added
     *
     * @param context
     * @return
     */
    public static List<String> getSystemAppList(Context context) {
        String[] systemApps = context.getResources().getStringArray(R.array.super_mode_systemapplist);
        return Arrays.asList(systemApps);
    }

    /**
     * Get the list of social applications that can be added
     *
     * @param context
     * @return
     */
    public static List<String> getSocialAppList(Context context) {
        // Chenyee guoxt modify for CSW1703CX-249 begin
        String[] socialApps;
        if(cyCXFlag){
            socialApps = context.getResources().getStringArray(R.array.super_mode_socialapplist_xiaolajiao);
        }else {
            socialApps = context.getResources().getStringArray(R.array.super_mode_socialapplist);
        }
        // Chenyee guoxt modify for CSW1703CX-249 end
        return Arrays.asList(socialApps);
    }

    public static boolean isFirstTime(Context context) {
        return getSP(context).getBoolean("isFirstTime", true);
    }

    public static void setFirstTime(Context context, boolean value) {
        getSP(context).edit().putBoolean("isFirstTime", value).commit();
    }

    /**
     * Write the value of simulate color space
     *
     * @param newMode 0 set to gray scale mode, -1 back to normal
     */
    public static void writeSimulateColorSpace(Context context, int newMode) {
        final ContentResolver cr = context.getContentResolver();
        if (newMode < 0) {
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 0);
        } else {
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 1);
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER, newMode);
        }
    }
}
