package com.wheatek.utils;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 来自mtk setting， 最终控制来自 com.mediatek.cta
 */
public interface AutoRunManager {
    boolean isAutoRunPackage(String packageName);

    void setAutoRunPackage(String packageName, boolean autoRun);

    public static AutoRunManager getInstance(Context context) {
        return MtkReflectCtaManagerAutoRunManagerFactory.getInstance(context);
    }

    public static class MtkReflectCtaManagerAutoRunManagerFactory {
        public static AutoRunManager getInstance(Context context) {
            try {
                Class<?> threadClazz = Class.forName("com.mediatek.cta.CtaManagerFactory");
                Method method = threadClazz.getMethod("getInstance");
                Object obj = method.invoke(null);
                method = obj.getClass().getMethod("makeCtaManager");
                Object catmanager = method.invoke(obj);
                if (catmanager == null) throw new NullPointerException("ReflectCtaManager,error");
                return new MyAutoRunManager(catmanager, context);
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }

        private static class MyAutoRunManager implements AutoRunManager {
            private final Context context;
            private final Object catmanager;
            Map<String, Boolean> records;

            public MyAutoRunManager(Object catmanager, Context context) {
                this.catmanager = catmanager;
                this.context = context.getApplicationContext();
                init();
            }

            public void init() {
                try {
                    Method method = catmanager.getClass().getMethod("queryAutoBootRecords", Context.class, int.class);
                    try {
                        records = (Map<String, Boolean>) method.invoke(catmanager, context, android.os.UserHandle.myUserId());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean isAutoRunPackage(String packageName) {
                if (records == null) {
                    return true;
                }
                return records.get(packageName);
            }

            @Override
            public void setAutoRunPackage(String packageName, boolean autoRun) {
                try {
                    Method method = catmanager.getClass().getMethod("changeAppAutoBootStatus", Context.class, String.class, boolean.class, int.class);
                    try {
                        method.invoke(catmanager, context, packageName, autoRun, android.os.UserHandle.myUserId());
                        records.put(packageName, autoRun);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}