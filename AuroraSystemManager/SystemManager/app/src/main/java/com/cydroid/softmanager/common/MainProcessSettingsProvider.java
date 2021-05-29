package com.cydroid.softmanager.common;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.cydroid.softmanager.utils.Log;

import cyee.preference.CyeePreferenceManager;

public class MainProcessSettingsProvider extends ContentProvider {

    private static final String TAG = "MainProcessSettingsProvider";

    private static final int TYPE_DEFAULT_BOOLEAN_KEY_ID = 1;
    private static final int TYPE_DEFAULT_STRING_KEY_ID = 2;
    private static final int TYPE_DEFAULT_INT_KEY_ID = 3;
    private static final int TYPE_DEFAULT_FLOAT_KEY_ID = 4;
    private static final int TYPE_DEFAULT_HAS_KEY_ID = 5;

    private static final int TYPE_SETIINGS_MAP_ID = 10;
    private static final int TYPE_BOOLEAN_KEY_ID = 11;
    private static final int TYPE_STRING_KEY_ID = 12;
    private static final int TYPE_INT_KEY_ID = 13;
    private static final int TYPE_FLOAT_KEY_ID = 14;
    private static final int TYPE_HAS_KEY_ID = 15;

    public static final String AUTHORITY = "com.cydroid.softmanager.main_process_settings";

    private static final UriMatcher URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URL_MATCHER.addURI(AUTHORITY, "boolean", TYPE_DEFAULT_BOOLEAN_KEY_ID);
        URL_MATCHER.addURI(AUTHORITY, "int", TYPE_DEFAULT_INT_KEY_ID);
        URL_MATCHER.addURI(AUTHORITY, "string", TYPE_DEFAULT_STRING_KEY_ID);
        URL_MATCHER.addURI(AUTHORITY, "float", TYPE_DEFAULT_FLOAT_KEY_ID);
        URL_MATCHER.addURI(AUTHORITY, "has", TYPE_DEFAULT_HAS_KEY_ID);

        URL_MATCHER.addURI(AUTHORITY, "map/*", TYPE_SETIINGS_MAP_ID);
        URL_MATCHER.addURI(AUTHORITY, "boolean/*", TYPE_BOOLEAN_KEY_ID);
        URL_MATCHER.addURI(AUTHORITY, "int/*", TYPE_INT_KEY_ID);
        URL_MATCHER.addURI(AUTHORITY, "string/*", TYPE_STRING_KEY_ID);
        URL_MATCHER.addURI(AUTHORITY, "float/*", TYPE_FLOAT_KEY_ID);
        URL_MATCHER.addURI(AUTHORITY, "has/*", TYPE_HAS_KEY_ID);
    }

    public MainProcessSettingsProvider() {

    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri url) {
        int match = URL_MATCHER.match(url);
        switch (match) {
            case TYPE_BOOLEAN_KEY_ID:
            case TYPE_DEFAULT_BOOLEAN_KEY_ID:
                return "vnd.android.cursor.dir/boolean";
            case TYPE_STRING_KEY_ID:
            case TYPE_DEFAULT_STRING_KEY_ID:
                return "vnd.android.cursor.dir/string";
            case TYPE_INT_KEY_ID:
            case TYPE_DEFAULT_INT_KEY_ID:
                return "vnd.android.cursor.dir/int";
            case TYPE_FLOAT_KEY_ID:
            case TYPE_DEFAULT_FLOAT_KEY_ID:
                return "vnd.android.cursor.dir/float";
            case TYPE_HAS_KEY_ID:
            case TYPE_DEFAULT_HAS_KEY_ID:
                return "vnd.android.cursor.dir/boolean";
            case TYPE_SETIINGS_MAP_ID:
                return "vnd.android.cursor.dir/map";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        try {
            SharedPreferences sharePreference = CyeePreferenceManager
                    .getDefaultSharedPreferences(getContext());
            if (url.getPathSegments().size() >= 2) {
                sharePreference = getContext().getSharedPreferences(url.getPathSegments().get(1),
                        Context.MODE_PRIVATE);
            }
            Editor sharePreferenceEditor = sharePreference.edit();
            int match = URL_MATCHER.match(url);
            Set<String> keySet = values.keySet();
            if (keySet.size() > 0) {
                Iterator<String> itr = keySet.iterator();
                String key = itr.next();
                Log.d(TAG, "set " + (url.getPathSegments().size() >= 2 ? url.getPathSegments().get(1)
                        : "defaultSharePrederence") + " key=" + key);
                switch (match) {
                    case TYPE_BOOLEAN_KEY_ID:
                    case TYPE_DEFAULT_BOOLEAN_KEY_ID: {
                        sharePreferenceEditor.putBoolean(key, values.getAsBoolean(key)).commit();
                        break;
                    }
                    case TYPE_STRING_KEY_ID:
                    case TYPE_DEFAULT_STRING_KEY_ID: {
                        sharePreferenceEditor.putString(key, values.getAsString(key)).commit();
                        break;
                    }
                    case TYPE_INT_KEY_ID:
                    case TYPE_DEFAULT_INT_KEY_ID: {
                        sharePreferenceEditor.putInt(key, values.getAsInteger(key).intValue()).commit();
                        break;
                    }
                    case TYPE_FLOAT_KEY_ID:
                    case TYPE_DEFAULT_FLOAT_KEY_ID: {
                        sharePreferenceEditor.putFloat(key, values.getAsFloat(key).floatValue()).commit();
                        break;
                    }
                    default: {
                        throw new UnsupportedOperationException("Cannot update URL: " + url);
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "update", e);
        }

        return 1;
    }

    @Override
    public int delete(Uri url, String selection, String[] selectionArgs) {
        SharedPreferences sharePreference = CyeePreferenceManager.getDefaultSharedPreferences(getContext());
        if (url.getPathSegments().size() >= 2) {
            sharePreference = getContext().getSharedPreferences(url.getPathSegments().get(1),
                    Context.MODE_PRIVATE);
        }
        int match = URL_MATCHER.match(url);
        int count = 0;
        Log.d(TAG, "delete match " + match);
        switch (match) {
            case TYPE_SETIINGS_MAP_ID:
                count = sharePreference.getAll().size();
                sharePreference.edit().clear().commit();
                break;
            default:
                throw new UnsupportedOperationException("Cannot delete URL: " + url);
        }
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] defauleValues,
            String sortOrder) {
        MatrixCursor cursor = null;
        SharedPreferences sharePreference = CyeePreferenceManager.getDefaultSharedPreferences(getContext());
        if (url.getPathSegments().size() >= 2) {
            sharePreference = getContext().getSharedPreferences(url.getPathSegments().get(1),
                    Context.MODE_PRIVATE);
        }
        int match = URL_MATCHER.match(url);
        Log.d(TAG, "query match " + match);
        switch (match) {
            case TYPE_SETIINGS_MAP_ID: {
                Map<String, ?> settingMap = sharePreference.getAll();
                String[] tableMapCursor = new String[settingMap.size()];
                String[] tableMapValues = new String[settingMap.size()];
                int count = 0;
                for (Entry<String, ?> entry : settingMap.entrySet()) {
                    tableMapCursor[count] = entry.getKey();
                    tableMapValues[count] = String.valueOf(entry.getValue());
                    count++;
                }
                cursor = new MatrixCursor(tableMapCursor);
                cursor.addRow(tableMapValues);
                break;
            }
            case TYPE_BOOLEAN_KEY_ID:
            case TYPE_DEFAULT_BOOLEAN_KEY_ID:
            case TYPE_STRING_KEY_ID:
            case TYPE_DEFAULT_STRING_KEY_ID:
            case TYPE_INT_KEY_ID:
            case TYPE_DEFAULT_INT_KEY_ID:
            case TYPE_FLOAT_KEY_ID:
            case TYPE_DEFAULT_FLOAT_KEY_ID:
            case TYPE_HAS_KEY_ID:
            case TYPE_DEFAULT_HAS_KEY_ID: {
                String dataType = url.getPathSegments().get(0);

                try {
                    Log.d(TAG,
                            "get " + (url.getPathSegments().size() >= 2 ? url.getPathSegments().get(1)
                                    : "defaultSharePrederence") + " key=" + selection + " def="
                                    + defauleValues[0]);
                    String[] tableCursor = new String[] {"values"};
                    cursor = new MatrixCursor(tableCursor);
                    if ("string".equals(dataType)) {
                        cursor.addRow(new Object[] {sharePreference.getString(selection, defauleValues[0])});
                    }
                    if ("boolean".equals(dataType)) {
                        cursor.addRow(new Object[] {sharePreference.getBoolean(selection,
                                "true".equals(defauleValues[0]))});
                    }
                    if ("int".equals(dataType)) {
                        cursor.addRow(new Object[] {
                                sharePreference.getInt(selection, Integer.parseInt(defauleValues[0]))});
                    }
                    if ("float".equals(dataType)) {
                        cursor.addRow(new Object[] {
                                sharePreference.getFloat(selection, Float.parseFloat(defauleValues[0]))});
                    }
                    if ("has".equals(dataType)) {
                        cursor.addRow(new Object[] {sharePreference.contains(selection)});
                    }
                } catch (Exception e) {
                    Log.e(TAG, "query", e);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Cannot query URL: " + url);
            }
        }

        return cursor;
    }

}