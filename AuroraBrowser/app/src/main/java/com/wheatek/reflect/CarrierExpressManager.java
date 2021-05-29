package com.wheatek.reflect;

import java.lang.reflect.Field;

public class CarrierExpressManager {

    public static String getConst(String name){
        try {
            Class cls = Class.forName("com.mediatek.common.carrierexpress.CarrierExpressManager");
            Field field = null;
            field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return (String) field.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }
}
