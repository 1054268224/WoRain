package com.cydroid.softmanager.powersaver.mode;

import java.lang.reflect.Constructor;

import com.cydroid.framework.FrameworkUtility;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItem;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;
import android.content.Context;

public class PowerModeItemSimpleFactory {
    private static final String TAG = "PowerModeItemSimpleFactory";

    // private static final String CLASS_PACKAGE_PATH = "com.cydroid.softmanager.powersaver.mode.item";

    public static PowerModeItem getInstanceByName(Context context, String mode, String name) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null !!");
        }
        if (mode == null || mode.isEmpty() || name == null || name.isEmpty()) {
            return null;
        }
        String findName = new StringBuffer().append(PowerModeItem.class.getPackage().getName()).append(".")
                .append(PowerModeItem.CLASS_PREFIX).append(name).toString();

        // Log.d(TAG, "load configItem " + findName + " in " + mMode + " mode");
        Class<?> itemClass = FrameworkUtility.createClass(findName);
        if (itemClass == null) {
            Log.d(TAG, "skip:can not find item " + findName + "  for mode:" + mode);
            return null;
        }
        if (!PowerModeItem.class.isAssignableFrom(itemClass)) {
            Log.d(TAG, "skip:item class " + findName + "  is not a valid mode item");
            return null;
        }
        try {
            Constructor<?> conFunc = itemClass.getConstructor(Context.class, String.class,
                    PowerServiceProviderHelper.class);
            PowerModeItem item = (PowerModeItem) conFunc.newInstance(context, mode,
                    new PowerServiceProviderHelper(context));
            if (!item.isFunctionAvailable()) {
                Log.d(TAG, "skip:item class " + findName + "  system do not support this function");
                return null;
            }
            return item;
        } catch (Exception e) {
            Log.d(TAG, "skip : get mode:" + mode + " name:" + name + " instance failed:" + e);
            for (StackTraceElement ele : e.getStackTrace()) {
                Log.d(TAG, ele.toString());
            }
            return null;
        }
    }
}
