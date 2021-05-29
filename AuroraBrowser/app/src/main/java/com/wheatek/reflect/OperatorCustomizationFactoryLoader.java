package com.wheatek.reflect;

import android.content.Context;

import java.lang.reflect.Method;
import java.util.List;

public class OperatorCustomizationFactoryLoader {
    private static Class cls;
    private static Object object;
    private Context context;

    public OperatorCustomizationFactoryLoader(Context context) {
        this.context = context;
        object = null;
    }

    static {
        try {
            cls = Class.forName("com.mediatek.common.util.OperatorCustomizationFactoryLoader");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Object loadFactory(Context context, List<?> sOperatorFactoryInfoList) throws Exception {
        Method m = cls.getMethod("loadFactory",Context.class,List.class);
        return m.invoke(object,context,sOperatorFactoryInfoList);
    }
}
