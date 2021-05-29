package com.cydroid.systemmanager.antivirus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;

import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.ServiceUtil;

public class MonitorAppInstallReceiver extends BroadcastReceiver {
    private static final String TAG = "MonitorAppInstallReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SystemProperties.get("ro.gn.app.securepay.support", "no").equals("yes")) {
            return;
        }
        mContext = context;
        boolean flag = getBoolean("is_first_utilization", true);
        Log.d(TAG, "onReceive is_first_utilization:" + flag);
        if (flag) {
            return;
        }
        String actionStr = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(actionStr)) {
            Uri data = intent.getData();
            String pkgName = data.getEncodedSchemeSpecificPart();
            Intent startIntent = new Intent(context, MonitorAppInstallService.class);
            startIntent.putExtra("pkgname", pkgName);
            ServiceUtil.startForegroundService(context,startIntent);
        }
    }

    private boolean getBoolean(String key, boolean defValue) {

        boolean returnValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("boolean"), null, key,
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                returnValue = "true".equals(value) ? true : false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception ", e);
        } finally {
            closeCursor(cursor);
        }
        return returnValue;
    }

    private Uri getUri(String str) {
        return Uri.parse("content://" + "com.cydroid.systemmanager.sp" + "/" + str);
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}