/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AutoBootAppProvider extends ContentProvider {
    private static final String TAG = "AutoBootAppProvider";

    private static final String AUTHORITY = "com.cydroid.softmanager.autobootapp";
    private static final int AUTO_BOOT_APPS_INDEX = 1;
    private static final UriMatcher URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URL_MATCHER.addURI(AUTHORITY, "autobootapps", AUTO_BOOT_APPS_INDEX);
    }

    IAutoBootAppProvider mAutoBootAppProviderImpl;

    @Override
    public boolean onCreate() {
        mAutoBootAppProviderImpl = createAutoBootAppProviderImpl();
        mAutoBootAppProviderImpl.init(getContext());
        return true;
    }

    @Override
    public String getType(Uri url) {
        return null;
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        try {
            int match = URL_MATCHER.match(uri);
            if (AUTO_BOOT_APPS_INDEX != match || null == selectionArgs
                    || 1 != selectionArgs.length) {
                return 0;
            }
            String packageName = selectionArgs[0];
            if (!TextUtils.isEmpty(packageName)) {
                return mAutoBootAppProviderImpl.delete(packageName);
            }
        } catch (Exception e) {
            Log.e(TAG, "delete", e);
        }
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {
            int match = URL_MATCHER.match(uri);
            Set<String> keySet = values.keySet();
            if (AUTO_BOOT_APPS_INDEX == match && 1 == keySet.size()) {
                Iterator<String> itr = keySet.iterator();
                String packageName = values.getAsString(itr.next());
                if (!TextUtils.isEmpty(packageName)) {
                    mAutoBootAppProviderImpl.insert(packageName);
                }
            } else {
                throw new UnsupportedOperationException("Cannot insert uri: " + uri);
            }
        } catch (Exception e) {
            Log.e(TAG, "insert", e);
        }
        return null;
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] defauleValues,
                        String sortOrder) {
        return null;
    }

    interface IAutoBootAppProvider {
        void init(Context context);

        int delete(String packageName);

        void insert(String packageName);
    }

    class AutoBootAppProviderImplV1 implements IAutoBootAppProvider {
        @Override
        public void init(Context context) {
        }

        @Override
        public int delete(String packageName) {
            List<String> userEnableAutoBootApps = new ArrayList<>();
            SharedPreferences sp = getContext().getSharedPreferences(
                    AutoBootAppManager.ENABLE_AUTO_BOOT_APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
            loadUserEnableAutoBootApps(sp, userEnableAutoBootApps);
            return removeUserEnableAutoBootApp(sp, userEnableAutoBootApps, packageName);
        }

        @Override
        public void insert(String packageName) {
            List<String> userEnableAutoBootApps = new ArrayList<>();
            SharedPreferences sp = getContext().getSharedPreferences(
                    AutoBootAppManager.ENABLE_AUTO_BOOT_APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
            loadUserEnableAutoBootApps(sp, userEnableAutoBootApps);
            addUserEnableAutoBootApp(sp, userEnableAutoBootApps, packageName);
        }

        private void loadUserEnableAutoBootApps(SharedPreferences sp, List<String> userEnableAutoBootApps) {
            try {
                Set<String> userEnableApps = new HashSet<>();
                userEnableApps = sp.getStringSet(
                        AutoBootAppManager.ENABLE_AUTO_BOOT_APP_NAMES_KEY, userEnableApps);
                userEnableAutoBootApps.clear();
                userEnableAutoBootApps.addAll(userEnableApps);
            } catch (Exception e) {
                Log.d(TAG, "loadUserEnableAutoBootApps e:" + e.toString());
            }
        }

        private void addUserEnableAutoBootApp(SharedPreferences sp,
                                              List<String> userEnableAutoBootApps, String enableAutoBootApp) {
            if (userEnableAutoBootApps.contains(enableAutoBootApp)) {
                return;
            }
            userEnableAutoBootApps.add(enableAutoBootApp);
            setUserEnableAutoBootApp(sp, userEnableAutoBootApps);
        }

        private int removeUserEnableAutoBootApp(SharedPreferences sp,
                                                List<String> userEnableAutoBootApps, String enableAutoBootApp) {
            if (!userEnableAutoBootApps.contains(enableAutoBootApp)) {
                return 0;
            }
            userEnableAutoBootApps.remove(enableAutoBootApp);
            setUserEnableAutoBootApp(sp, userEnableAutoBootApps);
            return 1;
        }

        private void setUserEnableAutoBootApp(SharedPreferences sp,
                                              List<String> userEnableAutoBootApps) {
            try {
                Set<String> userEnableApps = new HashSet<String>(userEnableAutoBootApps);
                SharedPreferences.Editor editor = sp.edit();
                editor.putStringSet(AutoBootAppManager.ENABLE_AUTO_BOOT_APP_NAMES_KEY, userEnableApps);
                editor.apply();
            } catch (Exception e) {
                Log.d(TAG, "setUserEnableAutoBootApp e:" + e.toString());
            }
        }
    }

    static class AutoBootAppProviderImplV2 implements IAutoBootAppProvider {
        AutoBootAppManager mAutoBootAppManager;

        @Override
        public void init(Context context) {
            mAutoBootAppManager = AutoBootAppManager.getInstance(context);
            mAutoBootAppManager.init(context);
        }

        @Override
        public int delete(String packageName) {
            mAutoBootAppManager.disableAutoBootApp(packageName);
            return 1;
        }

        @Override
        public void insert(String packageName) {
            mAutoBootAppManager.enableAutoBootApp(packageName);
        }
    }

    private IAutoBootAppProvider createAutoBootAppProviderImpl() {
        int version = AutoBootAppManager.getAutoBootAppManagerVersion(getContext());
        switch (version) {
            case AutoBootAppManager.VERSION_COMPONENT_LEVEL:
                return new AutoBootAppProviderImplV1();
            case AutoBootAppManager.VERSION_PROCESS_LEVEL:
                return new AutoBootAppProviderImplV2();
        }
        throw new RuntimeException("createAutoBootAppProviderImpl version:" + version + " error.");
    }
}