/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot.v1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoBootOptionFactory {
    private static final String TAG = "AutoBootOptionFactory";
    private static final Map<Intent, Class<?>> SYS_BOOT_AUTO_BOOT_OPTION_STRATEGIES = new HashMap<Intent, Class<?>>();

    static {
        SYS_BOOT_AUTO_BOOT_OPTION_STRATEGIES.put(new Intent(Intent.ACTION_BOOT_COMPLETED),
                BaseAutoBootOptionStrategy.class);
    }

    private static final Map<Intent, Class<?>> BACKGROUND_AUTO_BOOT_OPTION_STRATEGIES = new HashMap<Intent, Class<?>>();

    static {
        BACKGROUND_AUTO_BOOT_OPTION_STRATEGIES.put(new Intent(ConnectivityManager.CONNECTIVITY_ACTION),
                BaseAutoBootOptionStrategy.class);
        BACKGROUND_AUTO_BOOT_OPTION_STRATEGIES.put(new Intent(WifiManager.WIFI_STATE_CHANGED_ACTION),
                BaseAutoBootOptionStrategy.class);
        BACKGROUND_AUTO_BOOT_OPTION_STRATEGIES.put(new Intent(WifiManager.NETWORK_STATE_CHANGED_ACTION),
                BaseAutoBootOptionStrategy.class);
    }

    private static AutoBootOptionFactory sInstance;

    public synchronized static AutoBootOptionFactory getInstance() {
        if (null == sInstance) {
            sInstance = new AutoBootOptionFactory();
        }
        return sInstance;
    }

    private AutoBootOptionFactory() {
    }

    public List<AutoBootOptionStrategy> createAutoBootOptionStrategies(Context context) {
        List<AutoBootOptionStrategy> autoBootOptionStrategies =
                createSysBootAutoBootOptionStrategies(context);
        autoBootOptionStrategies.addAll(createBackgroundAutoBootOptionStrategies(context));
        return autoBootOptionStrategies;
    }

    public List<AutoBootOptionStrategy> createSysBootAutoBootOptionStrategies(Context context) {
        return createAutoBootOptionStrategies(context, SYS_BOOT_AUTO_BOOT_OPTION_STRATEGIES,
                AutoBootOptionItem.SYS_BOOT_AUTO_BOOT_TYPE);
    }

    public List<AutoBootOptionStrategy> createBackgroundAutoBootOptionStrategies(Context context) {
        return createAutoBootOptionStrategies(context, BACKGROUND_AUTO_BOOT_OPTION_STRATEGIES,
                AutoBootOptionItem.BACKGROUND_AUTO_BOOT_TYPE);
    }

    private List<AutoBootOptionStrategy> createAutoBootOptionStrategies(Context context,
            Map<Intent, Class<?>> autoBootOptionStrategies, int type) {
        List<AutoBootOptionStrategy> list = new ArrayList<AutoBootOptionStrategy>();
        for (Map.Entry<Intent, Class<?>> entry : autoBootOptionStrategies.entrySet()) {
            Intent intent = entry.getKey();
            Class<?> strategyClass = entry.getValue();
            AutoBootOptionStrategy strategy = createStrategy(strategyClass, context, intent, type);
            if (null != strategy) {
                list.add(strategy);
            }
        }
        return list;
    }

    private AutoBootOptionStrategy createStrategy(Class<?> strategyClass, Context context,
                                                  Intent intent, int type) {
        AutoBootOptionStrategy strategy = null;
        try {
            Constructor con = strategyClass.getConstructor(Context.class, Intent.class, int.class);
            strategy = (AutoBootOptionStrategy) con.newInstance(context, intent, type);
        } catch (Exception e) {
            Log.e(TAG, "createStrategy exception:" + e.toString());
        }
        return strategy;
    }
}