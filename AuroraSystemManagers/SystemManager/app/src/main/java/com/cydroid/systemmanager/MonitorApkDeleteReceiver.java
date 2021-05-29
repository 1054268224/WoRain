package com.cydroid.systemmanager;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Files;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.cydroid.systemmanager.utils.ApkInfoHelper;
import com.cydroid.systemmanager.utils.FileUtil;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.UnitUtil;

public class MonitorApkDeleteReceiver extends BroadcastReceiver {
    private static final String TAG = "MonitorApkDeleteReceiver";
    private boolean DEBUG = true;
    private Context mContext;
    private BackgroundQueryHandler mQueryHandler;
    private PackageManager mPkgManager;
    private int mQueryHandlerIndex;
    private Map<Integer, String> mDelPackageName = new HashMap<Integer, String>();

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String actionStr = intent.getAction();
        if (UnitUtil.APK_DELETE_ACTION.equals(actionStr)) {
            final String pkgPath = intent.getStringExtra(UnitUtil.APK_DELETE_PACKAGE_PATH_KEY);
            final String pkgName = intent.getStringExtra(UnitUtil.APK_DELETE_PACKAGE_NAME_KEY);

            init();            
            if (null != pkgPath && !pkgPath.isEmpty()) {
                deleteFile(pkgPath);
            } else if (null != pkgName && !pkgName.isEmpty()) {
                doDeleteApkByName(pkgName);
            }
        }
    }

    private void init() {
        if (mQueryHandler == null) {
            mQueryHandler = new BackgroundQueryHandler(mContext.getContentResolver());
            mQueryHandlerIndex = 0;
        }
        
        if (null == mPkgManager) {
            mPkgManager = mContext.getPackageManager();
        }
    }

    private void deleteFile(final String path) {
        new Thread() {
            public void run() {
                deleteFileReally(path);
            }
        }.start();
    }

    private void deleteFileReally(String path) {
        if (path == null) {
            return;
        }
        Log.d(DEBUG, TAG, "deleteFileReally path:" + path);
        FileUtil.recurDelete(new File(path));
        UnitUtil.updateMediaVolume(mContext, mQueryHandler, path);
    }

    private class BackgroundQueryHandler extends AsyncQueryHandler {
        public BackgroundQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            Log.d(DEBUG, TAG, "apk is delete from database result:" + result);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.d(DEBUG, TAG,
                "query apk info complete, check apk packageName to delete apk file token:" + token);
            if (cursor == null || mQueryHandler == null) {
                return;
            }
            String delPkgName = mDelPackageName.get(Integer.valueOf(token));
            Log.d(DEBUG, TAG,
                "onQueryComplete delPkgName:" + delPkgName);
            while (cursor.moveToNext()) {
                String path = cursor.getString(0);
                String pkgName = ApkInfoHelper.getUninstalledApkPkgName(
                        mPkgManager, path);    
                Log.d(DEBUG, TAG,
                    "onQueryComplete path:" + path + ", pkgName:" + pkgName);
                if (null != delPkgName && delPkgName.equals(pkgName)) {
                    deleteFile(path);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private synchronized void doDeleteApkByName(final String pkgName) {
        Log.d(TAG, "doDeleteApkByName pkgName:" + pkgName + ", mQueryHandlerIndex:" + mQueryHandlerIndex);
        mDelPackageName.put(Integer.valueOf(mQueryHandlerIndex), pkgName);
        getApkInfoFromDatabase(mQueryHandlerIndex);
        ++mQueryHandlerIndex;
    }

    private void getApkInfoFromDatabase(final int indexQueryHandler) {
        String volumeName = "external";
        final Uri uri = Files.getContentUri(volumeName);
        final String[] columns = new String[] { "_data, _id" };
        final String selection = "_data like '%.apk'";
        mQueryHandler.post(new Runnable() {
            @Override
            public void run() {
                mQueryHandler.startQuery(indexQueryHandler, null, uri, columns, selection,
                    null, null);
            }
        });
    }
}