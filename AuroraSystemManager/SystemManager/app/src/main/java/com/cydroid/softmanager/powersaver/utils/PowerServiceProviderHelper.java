package com.cydroid.softmanager.powersaver.utils;

import java.util.Set;

import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.content.SharedPreferences;

public class PowerServiceProviderHelper {
    private static final String TAG = "PowerServiceProviderHelper";
    private static final String PREFERENCE_NAME = "remote_power_service_config";

    private final Context mContext;

    public PowerServiceProviderHelper(Context context) {
        mContext = context;
    }

    private SharedPreferences getSharedPreference() {
        return mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
    }

    public int getInt(String key, int defValue) {
        SharedPreferences preference = getSharedPreference();
        return preference.getInt(key, defValue);
    }

    public void putInt(String key, int value) {
        SharedPreferences preference = getSharedPreference();
        preference.edit().putInt(key, value).commit();
    }

    public float getFloat(String key, float defValue) {
        SharedPreferences preference = getSharedPreference();
        return preference.getFloat(key, defValue);
    }

    public void putFloat(String key, float value) {
        SharedPreferences preference = getSharedPreference();
        preference.edit().putFloat(key, value).commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        SharedPreferences preference = getSharedPreference();
        return preference.getBoolean(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences preference = getSharedPreference();
        preference.edit().putBoolean(key, value).commit();
    }

    public String getString(String key, String defValue) {
        SharedPreferences preference = getSharedPreference();
        return preference.getString(key, defValue);
    }

    public void putString(String key, String value) {
        SharedPreferences preference = getSharedPreference();
        preference.edit().putString(key, value).commit();
    }

    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 begin
    public Set<String> getStringSet(String key, Set<String> defValue) {
        SharedPreferences preference = getSharedPreference();
        return preference.getStringSet(key, defValue);
    }

    public void putStringSet(String key, Set<String> value) {
        SharedPreferences preference = getSharedPreference();
        preference.edit().putStringSet(key, value).commit();
    }

    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 end

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    public boolean hasKey(String key) {
        SharedPreferences preference = getSharedPreference();
        return preference.contains(key);
    }

    public String getValueStr(String key) {
        SharedPreferences preference = getSharedPreference();
        try {
            boolean booleanVal = preference.getBoolean(key, false);
            return String.valueOf(booleanVal);
        } catch (ClassCastException cce) {
            // Log.d(TAG, key + " is not boolean,try next");
        }
        try {
            int intVal = preference.getInt(key, -1);
            return String.valueOf(intVal);
        } catch (ClassCastException cce) {
            // Log.d(TAG, key + "not int,try next");
        }
        try {
            float floatVal = preference.getFloat(key, -1f);
            return String.valueOf(floatVal);
        } catch (ClassCastException cce) {
            // Log.d(TAG, key + "not float,try next");
        }
        try {
            String strVal = preference.getString(key, "");
            return strVal;
        } catch (ClassCastException cce) {
            // Log.d(TAG, key + "not string,empty");
        }
        return null;
    }

    public boolean removeKey(String key) {
        SharedPreferences preference = getSharedPreference();
        return preference.edit().remove(key).commit();
    }
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
}
