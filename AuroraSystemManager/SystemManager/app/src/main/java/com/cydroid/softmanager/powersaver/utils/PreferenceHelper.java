package com.cydroid.softmanager.powersaver.utils;

import cyee.preference.CyeePreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;

/*
 * 工具类，读写本应用的SharedPreference项目值
 */
public class PreferenceHelper {
    private final SharedPreferences mPreferences;
    private final SharedPreferences.Editor mEditor;

    public PreferenceHelper(Context context) {
        mPreferences = CyeePreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPreferences.edit();
    }

    public void commitBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key, defaultValue);
    }

    public void commitInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public int getInt(String key, int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }

    public void commitString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public String getString(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

}
