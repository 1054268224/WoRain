package com.cydroid.systemmanager.utils;

import cyee.app.CyeeActionBar;

public class UiUtils {
    public static void setElevation(CyeeActionBar actionbar, float level) {
        FrameworkUtility.invokeMethod(CyeeActionBar.class, actionbar, "setElevation",
                new Class[] {float.class}, new Object[] {(float) level});
    }
}
