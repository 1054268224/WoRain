package com.cydroid.softmanager.utils;

import com.cydroid.framework.FrameworkUtility;
import com.cydroid.softmanager.R;

import cyee.app.CyeeActionBar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.SystemProperties;

import java.util.Locale;

public class UiUtils {
    public static void setElevation(CyeeActionBar actionbar, float level) {
        FrameworkUtility.invokeMethod(CyeeActionBar.class, actionbar, "setElevation",
                new Class[] {float.class}, new Object[] {(float) level});
    }

    private static final String[] sSpecialStyleModel = {"M2017"};
    private static final String[] sSpecialStyleProject = {"GBL8918"};

    public static boolean isSpecialStyleModel() {
        String modelName = SystemProperties.get("ro.product.model", "");
        String projectId = SystemProperties.get("ro.gn.gnprojectid", "");
        for (String specialModelName : sSpecialStyleModel) {
            if (modelName.equals(specialModelName)) {
                return true;
            }
        }
        for (String specialProjectId : sSpecialStyleProject) {
            if (projectId.equals(specialProjectId)) {
                return true;
            }
        }
        return false;
    }

    // Chenyee <CY_bug> <chenyu> <20180408> modify for CSW1705AC-50 begin
    public static boolean isRTLSysLanguage(Context context){
        Locale mLocale = context.getResources().getConfiguration().locale;
        String mLanguage = mLocale.getLanguage();
        return mLanguage != null && (mLanguage.endsWith("ar") || mLanguage.endsWith("iw"));
    }
    // Chenyee <CY_bug> <chenyu> <20180408> modify for CSW1705AC-50 end

    public static ColorStateList getColorStateList(Context context) {
        Resources resource=(Resources)context.getResources();
        ColorStateList csl=(ColorStateList)resource.getColorStateList(R.color.gn_action_bar_tab_text_selector);
        return csl;
    }
}
