package com.cydroid.softmanager.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import com.cydroid.softmanager.common.SharedPreferencesProvider;
import com.cydroid.softmanager.utils.Log;

public class MainProcessSettingsProviderHelper {
    private final Context mContext;
    private String mPreferenceName = "";
    private static final boolean DEBUG = true;
    private static final String TAG = "MainProcessSettingsProviderHelper";

    public MainProcessSettingsProviderHelper(Context context) {
        mContext = context;
        setPreferenceName(Consts.DEFAULT_PREFERENCES_NAME);
    }

    public void setPreferenceName(String name) {
        Log.d(TAG, "use pref name =" + name);
        mPreferenceName = name;
    }

    public boolean hasKey(String key) {
        boolean retValue = false;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("has"), null, key, new String[] {"false"},
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = "true".equals(value);
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper hasKey(), query dababase throw exception:" + e);
        } finally {
            closeCursor(cursor);
        }
        return retValue;
    }

    private String getPreferenceName() {
        return (mPreferenceName == null || mPreferenceName.isEmpty()) ? "" : (mPreferenceName);
    }

    public int getInt(String key, int defValue) {
        int retValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("int"), null, key,
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = Integer.parseInt(value);
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper getInt() query dababase throw exception, " + e.toString());
        } finally {
            closeCursor(cursor);
        }

        return retValue;
    }

    public void putInt(String key, int value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        try {
            mContext.getContentResolver().update(getUri("int"), contentValues, null, null);
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper->putInt() throw Exception, " + e.toString());
        }
    }

    public float getFloat(String key, float defValue) {
        float retValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("float"), null, key,
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = Float.parseFloat(value);
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper getFloat() query dababase throw exception, " + e.toString());
        } finally {
            closeCursor(cursor);
        }

        return retValue;
    }

    public void putFloat(String key, float value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        try {
            mContext.getContentResolver().update(getUri("float"), contentValues, null, null);
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper->putFloat() throw Exception, " + e.toString());
        }
    }

    public boolean getBoolean(String key, boolean defValue) {

        boolean retValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("boolean"), null, key,
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = "true".equals(value);
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper getBoolean(), query dababase throw exception, " + e.toString());
        } finally {
            closeCursor(cursor);
        }
        return retValue;
    }

    public void putBoolean(String key, boolean value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        try {
            mContext.getContentResolver().update(getUri("boolean"), contentValues, null, null);
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper->putBoolean() throw Exception, " + e.toString());
        }
    }

    public String getString(String key, String defValue) {

        String retValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("string"), null, key, new String[] {defValue},
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                retValue = value;
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper getString(), query dababase throw exception, " + e.toString());
        } finally {
            closeCursor(cursor);
        }
        return retValue;
    }

    public void putString(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key, value);
        try {
            mContext.getContentResolver().update(getUri("string"), contentValues, null, null);
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper->putString() throw Exception, " + e.toString());
        }
    }

    public Map<String, String> getAll() {
        HashMap<String, String> res = new HashMap<String, String>();
        if (getPreferenceName().isEmpty()) {
            return res;
        }
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("map"), null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    res.put(cursor.getColumnName(i), cursor.getString(i));
                }
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper getAll(), query dababase throw exception, " + e.toString());
        } finally {
            closeCursor(cursor);
        }
        return res;
    }

    private Uri getUri(String str) {
        Uri uri = Uri.parse("content://" + MainProcessSettingsProvider.AUTHORITY + "/" + str);
        if (!getPreferenceName().isEmpty()) {
            uri = Uri.withAppendedPath(uri, getPreferenceName());
        }
        return uri;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public int removeAll() {
        int count = 0;
        try {
            count = mContext.getContentResolver().delete(getUri("map"), null, null);
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "ProviderHelper removeAll(), query dababase throw exception, " + e.toString());
        }
        return count;
    }

}
