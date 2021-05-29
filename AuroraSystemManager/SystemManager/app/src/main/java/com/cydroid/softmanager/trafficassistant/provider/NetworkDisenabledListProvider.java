/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: mengdw
 *
 * Date: 2016-11-02
 * 
 * for CR01639347
 */

package com.cydroid.softmanager.trafficassistant.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.cydroid.softmanager.utils.Log;

public class NetworkDisenabledListProvider extends ContentProvider {
    public static final String AUTHORITY = "com.cydroid.softmanager.networkcontrol";
    
    private static final String TAG ="NetworkDisenabledListProvider";
    private static final int NETWORK_DISENABLED_LIST = 1;
    private static final UriMatcher URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    
    static {
        URL_MATCHER.addURI(AUTHORITY, "disenablelist", NETWORK_DISENABLED_LIST);
    }
    
    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        return true;
    }
    
    @Override
    public String getType(Uri url) {
       return null;
    }
    
    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        return 1;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }
    
    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] defauleValues,
            String sortOrder) {
        try {
            Log.d(TAG, " query selection=" + selection);
        } catch (Exception e) {
            Log.e(TAG, "provider query", e);
        }
        return null;
    }
}