package com.cydroid.systemmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import cyee.preference.CyeePreferenceManager;

public class PreferenceHelper {

	public static boolean getBoolean(Context context, String key,
			boolean defValue) {
		SharedPreferences sharedPref = CyeePreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(key, defValue);
	}

	public static String getString(Context context, String key, String defValue) {
		SharedPreferences sharedPref = CyeePreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(key, defValue);
	}
}
