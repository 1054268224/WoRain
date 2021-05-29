package com.wheatek.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CustomProperties {
    public static final String MODULE_BROWSER = getConst("MODULE_BROWSER");
    public static final String UAPROF_URL = getConst("UAPROF_URL");

    public static String getString(String moduleBrowser, String userAgent) {
        try {
            Class<?> cls = Class.forName("com.mediatek.custom.CustomProperties");
            Method m = cls.getMethod("getString", String.class, String.class);
            return (String) m.invoke(null, moduleBrowser, userAgent);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static String getConst(String name) {
        try {
            Class<?> cls = Class.forName("com.mediatek.custom.CustomProperties");
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return (String) field.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }
}
