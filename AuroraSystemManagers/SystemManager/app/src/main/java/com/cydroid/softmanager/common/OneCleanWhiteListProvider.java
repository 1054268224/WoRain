
package com.cydroid.softmanager.common;

import java.util.List;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.Log;

public class OneCleanWhiteListProvider extends ContentProvider {

    private static final String TAG = "OneCleanWhiteListProvider";

    private static final int SP_WHITE_LIST = 1;

    public static final String AUTHORITY = "com.cydroid.systemmanager.oneclean";

    private static final UriMatcher URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URL_MATCHER.addURI(AUTHORITY, "whitelist", SP_WHITE_LIST);
    }

    public OneCleanWhiteListProvider() {

    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri url) {
        int match = URL_MATCHER.match(url);
        switch (match) {
            case SP_WHITE_LIST:
                return "vnd.android.cursor.dir/whitelist";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
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

    @SuppressWarnings("rawtypes")
    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] defauleValues,
            String sortOrder) {
        String str = url.getPathSegments().get(0);
        MatrixCursor cursor = null;
        try {
            String[] tableCursor = new String[] {"pkgName"};
            cursor = new MatrixCursor(tableCursor);

            if ("whitelist".equals(str)) {
                Log.d(TAG, "query whitelist begin");
                WhiteListManager whiteListManager = WhiteListManager.getInstance();
                whiteListManager.init(getContext());
                List<String> whiteApps = whiteListManager.getUserWhiteApps();
                for (String pkgName : whiteApps) {
                    cursor.addRow(new Object[] {pkgName});
                }
                Log.d(TAG, "query whitelist end");
                /*
                Map<String, ?> map = mSp.getAll();
                for (Map.Entry me : map.entrySet()) {
                    Object value = map.get(me.getKey());
                    if ((boolean) value) {
                        cursor.addRow(new Object[] {me.getKey()});
                    }
                }
                */
            }
        } catch (Exception e) {
            Log.e(TAG, "query", e);
        }
        return cursor;
    }
}
