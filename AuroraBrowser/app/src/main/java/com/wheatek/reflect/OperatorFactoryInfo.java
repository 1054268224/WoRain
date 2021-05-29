package com.wheatek.reflect;

import java.lang.reflect.Constructor;

public class OperatorFactoryInfo {

    public static Object getOperatorFactoryInfo(String s, String s1, String s2, String s3, String s4, String s5) {
        try {
            Class cls = Class.forName("com.mediatek.common.util.OperatorCustomizationFactoryLoader$OperatorFactoryInfo");
            Constructor<?> con = cls.getConstructor(String.class, String.class, String.class, String.class, String.class, String.class);
            return con.newInstance(s, s1, s2, s3, s4, s5);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }
}
