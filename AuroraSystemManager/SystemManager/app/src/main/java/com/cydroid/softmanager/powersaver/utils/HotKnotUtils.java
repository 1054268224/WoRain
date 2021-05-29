package com.cydroid.softmanager.powersaver.utils;

/**
 * @author chenml
 * @date 2015-03-24
 * */
import java.lang.reflect.Method;

import android.content.Context;
import android.os.SystemProperties;

public class HotKnotUtils {
    private static final String PACKAGE_NAME = "com.mediatek.hotknot.HotKnotAdapter";

    public static final String EXTRA_ADAPTER_STATE = "com.mediatek.hotknot.extra.ADAPTER_STATE";// HotKnotAdapter.EXTRA_ADAPTER_STATE
    public static final String ACTION_HOTKNOT_SETTINGS = "mediatek.settings.HOTKNOT_SETTINGS";// HotKnotAdapter.ACTION_HOTKNOT_SETTINGS
    public static final String ACTION_ADAPTER_STATE_CHANGED = "com.mediatek.hotknot.action.ADAPTER_STATE_CHANGED";// HotKnotAdapter.ACTION_ADAPTER_STATE_CHANGED
    public static final int STATE_DISABLED = 1; // HotKnotAdapter.STATE_DISABLED
    public static final int STATE_ENABLED = 2; // HotKnotAdapter.STATE_ENABLED

    public static Object getDefaultAdapter(Context context) {
        try {
            Class<?> c = Class.forName(PACKAGE_NAME);
            Method method = c.getMethod("getDefaultAdapter", Context.class);
            Object adapter = method.invoke(null, context);
            return adapter;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }

    }

    public static boolean enable(Context context) {
        boolean result;
        try {
            Class<?> aoclass = Class.forName(PACKAGE_NAME);
            Method method = aoclass.getDeclaredMethod("enable");
            result = (Boolean) method.invoke(getDefaultAdapter(context));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    public static boolean disable(Context context) {
        boolean result;
        try {
            Class<?> aoclass = Class.forName(PACKAGE_NAME);
            Method method = aoclass.getDeclaredMethod("disable");
            result = (Boolean) method.invoke(getDefaultAdapter(context));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    public static boolean isEnabled(Context context) {
        boolean result;
        try {
            Class<?> aoclass = Class.forName(PACKAGE_NAME);
            Method method = aoclass.getDeclaredMethod("isEnabled");
            result = (Boolean) method.invoke(getDefaultAdapter(context));
        } catch (Exception ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    public static boolean isHotKnotSupport() {
        boolean mtksupport = SystemProperties.get("ro.mtk_hotknot_support", "0").equals("1");
        boolean gnsupport = SystemProperties.get("ro.gn.hotknot.support", "no").equals("yes");
        return mtksupport && gnsupport;
    }

}
