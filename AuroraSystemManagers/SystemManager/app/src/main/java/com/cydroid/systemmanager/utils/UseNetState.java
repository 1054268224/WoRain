package com.cydroid.systemmanager.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class UseNetState {
    private static boolean DEBUG = true;
    private static String TAG = "SDK/UseNetState";

    public static boolean getState(Context context, boolean isOk) {
        return getStateFromDatabase(context, isOk);
    }

    private static boolean getStateFromDatabase(Context context, boolean defValue) {

        boolean returnValue = defValue;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(getUri("boolean"), null, "is_first_utilization",
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                returnValue = "true".equals(value) ? true : false;
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "query database throw exception");
        } finally {
            closeCursor(cursor);
        }
        return returnValue;
    }

    private static Uri getUri(String str) {
        return Uri.parse("content://" + "com.cydroid.systemmanager.sp" + "/" + str);
    }

    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}
