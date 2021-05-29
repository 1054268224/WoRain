    package com.cydroid.softmanager.powersaver.mode.item;

    import com.cydroid.softmanager.common.Consts;
    import com.cydroid.softmanager.powersaver.mode.SuperModeUtils;
    import com.cydroid.softmanager.powersaver.utils.PowerConfig;
    import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
    import com.cydroid.softmanager.utils.Log;

    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.ObjectInputStream;
    import java.io.ObjectOutputStream;
    import java.util.ArrayList;
    import java.util.Iterator;
    import java.util.LinkedList;
    import java.util.List;
    import java.util.Set;

    import com.cydroid.softmanager.R;
    import com.cydroid.softmanager.common.Util;

    import android.content.ComponentName;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.pm.ActivityInfo;
    import android.content.pm.PackageManager;
    import android.content.pm.ResolveInfo;
    import android.net.Uri;
    import android.os.Build;
    import android.os.PatternMatcher;
    import android.provider.Settings;

    public class PowerModeItemEnableSuperModeLauncher extends PowerModeItem {
        private static final String SUPER_LAUNCHER_PACKAGE_NAME = "com.cydroid.powersaver.launcher";
        private static final String PREF_DEFAULT_APP_SETTINGS = "default_pkg";
        private static final String KEY_DEFAULT_LAUNCHER = "default_pkg";
        private static final String[] CHENYEE_SYSTEM_LAUNCHERS = {"com.android.launcher3"};
        private static final String FROZEN_LAUNCHER_LIST_MSDATA_PTAH = "/data/misc/msdata/" + "softmanager_supermode_freezed_launchers.obj";
        private static final String GIONEE_DEFAULT_LAUNCHER_PACKAGE_NAME = "com.android.launcher3";

        public PowerModeItemEnableSuperModeLauncher(Context context, String mode,
                                                    PowerServiceProviderHelper providerHelper) {
            super(context, mode, providerHelper);
        }

        public String getFrozenPath() {
            return mContext.getFilesDir().getAbsolutePath() + FROZEN_LAUNCHER_LIST_MSDATA_PTAH;
        }

        @Override
        public void save() {
            ArrayList<String> disabledLauncherPackages = new ArrayList<String>();
            String defaultLauncherPackageName = Util.getDefaultLauncherPkg(mContext);
            if (defaultLauncherPackageName != null && !defaultLauncherPackageName.equals("android")
                    && !defaultLauncherPackageName.equalsIgnoreCase(SUPER_LAUNCHER_PACKAGE_NAME)) {
                Log.d(TAG, "save default launcher pkg = " + defaultLauncherPackageName);
                mContext.getSharedPreferences(PREF_DEFAULT_APP_SETTINGS, Context.MODE_PRIVATE).edit()
                        .putString(KEY_DEFAULT_LAUNCHER, defaultLauncherPackageName);
                disabledLauncherPackages.add(defaultLauncherPackageName);
            }
            List<String> re = Util.getLauncherPkg(mContext);
            for (String o : re) {
                if (!disabledLauncherPackages.contains(o)) {
                    disabledLauncherPackages.add(o);
                }
            }
            if (disabledLauncherPackages.contains(SUPER_LAUNCHER_PACKAGE_NAME)) {
                disabledLauncherPackages.remove(SUPER_LAUNCHER_PACKAGE_NAME);
            }
            saveGioneeSystemLauncherPackages(disabledLauncherPackages);
            saveListToFile(getFrozenPath(), disabledLauncherPackages);

        }

        private void saveGioneeSystemLauncherPackages(ArrayList<String> appList) {
            for (String launcherPkg : CHENYEE_SYSTEM_LAUNCHERS) {
                if (!appList.contains(launcherPkg)) {
                    appList.add(launcherPkg);
                }
            }
        }

        private void saveListToFile(String filePath, ArrayList<String> appList) {
            ObjectOutputStream out = null;
            try {
                File objFile = new File(filePath);
                if (objFile.exists()) {
                    objFile.delete();
                } else {
                    objFile.getParentFile().mkdirs();
                }
                objFile.createNewFile();
                out = new ObjectOutputStream(new FileOutputStream(filePath));
                out.writeObject(appList);
                out.flush();
                out.close();
                Log.i(TAG, "saveListToFile appList.size() --------> " + appList.size());
            } catch (Exception e) {
                Log.e(TAG, "saveListToFile-------->", e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e) {
                        Log.d(TAG, "file close failed! " + e);
                    }
                }
            }
        }

        @Override
        public boolean restore(boolean isForceRestore) {
            /*guoxt modify for CSW1702A-2175 begin*/
            ArrayList<String> disabledLauncherPackages = getListFromFile(getFrozenPath());
            disabledLauncherPackages.add(GIONEE_DEFAULT_LAUNCHER_PACKAGE_NAME);
            /*guoxt modify for CSW1702A-2175 end*/

            String savedDefaultLauncherPkgname = mContext
                    .getSharedPreferences(PREF_DEFAULT_APP_SETTINGS, Context.MODE_PRIVATE)
                    .getString(KEY_DEFAULT_LAUNCHER, GIONEE_DEFAULT_LAUNCHER_PACKAGE_NAME);
            freezeSuperModeLauncher();
            if (!disabledLauncherPackages.isEmpty()) {
                Log.d(TAG, "enable system launcher");
                unfreezeLaunchers(disabledLauncherPackages, savedDefaultLauncherPkgname);
            }
            setDefaultLauncher(savedDefaultLauncherPkgname);
            return true;
        }

        private ArrayList<String> getListFromFile(String filePath) {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new FileInputStream(filePath));
                ArrayList<String> frozenAppList = (ArrayList<String>) in.readObject();
                in.close();
                Log.i(TAG, "getListFromFile list.size() --------> " + frozenAppList.size());
                return frozenAppList;
            } catch (Exception e) {
                Log.e(TAG, "getListFromFile-------->", e);
                return new ArrayList<String>();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        Log.d(TAG, "file close failed! " + e);
                    }
                }
            }
        }

        private void unfreezeLaunchers(ArrayList<String> disabledLauncherPackages, String defaultLauncher) {
            for (String launcherPkg : disabledLauncherPackages) {
                SuperModeUtils.unFreezeApp(mContext, launcherPkg, launcherPkg.equals(defaultLauncher));
            }
        }

        private void freezeSuperModeLauncher() {
            SuperModeUtils.freezeApp(mContext, SUPER_LAUNCHER_PACKAGE_NAME, false);
        }

        private void setDefaultLauncher(String launcherPackageName) {
            if (launcherPackageName.isEmpty()) {
                return;
            }
            ResolveInfo defaultLauncherResolveInfo = null;
            Intent launcherIntent = getLauncherIntent();
            List<ResolveInfo> launcherResolveInfos = mContext.getPackageManager().queryIntentActivities(
                    launcherIntent, PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
            int launcherResolveInfosSize = launcherResolveInfos.size();
            for (int i = 0; i < launcherResolveInfosSize; i++) {
                ResolveInfo launcherResolveInfo = launcherResolveInfos.get(i);
                if (launcherResolveInfo.activityInfo.packageName.equals(launcherPackageName)) {
                    defaultLauncherResolveInfo = launcherResolveInfo;
                    Log.d(TAG, "find default launcherResolveInfo " + launcherResolveInfo.resolvePackageName);
                    break;
                }
            }
            if (defaultLauncherResolveInfo == null) {
                Log.d(TAG, "setDefaultLauncher failed!  can not find default launcher ResolveInfo");
                return;
            }
            IntentFilter filter = new IntentFilter();
            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            ActivityInfo defaultLauncherActivityInfo = defaultLauncherResolveInfo.activityInfo;
            launcherIntent.setComponent(new ComponentName(defaultLauncherActivityInfo.applicationInfo.packageName,
                    defaultLauncherActivityInfo.name));
            if (launcherIntent.getAction() != null) {
                filter.addAction(launcherIntent.getAction());
            }
            Set<String> categories = launcherIntent.getCategories();
            if (categories != null) {
                for (String category : categories) {
                    filter.addCategory(category);
                }
            }
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            int defaultLauncherCategory = defaultLauncherResolveInfo.match & IntentFilter.MATCH_CATEGORY_MASK;
            Uri data = launcherIntent.getData();
            if (defaultLauncherCategory == IntentFilter.MATCH_CATEGORY_TYPE) {
                String mimeType = launcherIntent.resolveType(mContext);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        // Log.v("ResolverActivity: ", "" + e);
                        Log.v(TAG, "ResolverActivity: " + e);
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null) {
                if (defaultLauncherCategory != IntentFilter.MATCH_CATEGORY_TYPE
                        || (!"file".equals(data.getScheme()) && !"content".equals(data.getScheme()))) {
                    filter.addDataScheme(data.getScheme());
                    Iterator<IntentFilter.AuthorityEntry> aIt = defaultLauncherResolveInfo.filter
                            .authoritiesIterator();
                    if (aIt != null) {
                        while (aIt.hasNext()) {
                            IntentFilter.AuthorityEntry a = aIt.next();
                            if (a.match(data) >= 0) {
                                int port = a.getPort();
                                filter.addDataAuthority(a.getHost(), port >= 0 ? Integer.toString(port) : null);
                                break;
                            }
                        }
                    }
                    Iterator<PatternMatcher> pIt = defaultLauncherResolveInfo.filter.pathsIterator();
                    if (pIt != null) {
                        String path = data.getPath();
                        while (path != null && pIt.hasNext()) {
                            PatternMatcher p = pIt.next();
                            if (p.match(path)) {
                                filter.addDataPath(p.getPath(), p.getType());
                                break;
                            }
                        }
                    }
                }
            }
            if (filter != null) {
                ComponentName[] set = new ComponentName[launcherResolveInfosSize];
                int bestMatch = 0;
                for (int k = 0; k < launcherResolveInfosSize; k++) {
                    set[k] = new ComponentName(launcherResolveInfos.get(k).activityInfo.packageName,
                            launcherResolveInfos.get(k).activityInfo.name);
                    if (launcherResolveInfos.get(k).match > bestMatch) {
                        bestMatch = launcherResolveInfos.get(k).match;
                    }

                }
                mContext.getPackageManager().addPreferredActivity(filter, bestMatch, set,
                        launcherIntent.getComponent());
            }
        }

        private Intent getLauncherIntent() {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            return intent;
        }

        @Override
        public boolean apply() {
            if (!hasConfig()) {
                Log.d(TAG, "no settings " + mConfigKey + " ,do nothing");
                return true;
            }
            Log.d(TAG, "apply setting from key:" + mConfigKey);
            unfreezeSuperModeLauncher();
            /*guoxt modify for CSW1702A-2175 begin*/
            ArrayList<String> disabledLauncherPackages = getListFromFile(getFrozenPath());
            /*guoxt modify for CSW1702A-2175 end*/
            disabledLauncherPackages.add(GIONEE_DEFAULT_LAUNCHER_PACKAGE_NAME);
            freezeLaunchers(disabledLauncherPackages);
            return true;
        }

        private void unfreezeSuperModeLauncher() {
            Log.e(TAG, "----->unfreezeSuperModeLauncher ");
            if (!Consts.SUPPORT_NEW_LAUNCHER) {
                SuperModeUtils.cleanAppDate(mContext, SUPER_LAUNCHER_PACKAGE_NAME);
            }
            SuperModeUtils.unFreezeApp(mContext, SUPER_LAUNCHER_PACKAGE_NAME, true);
        }

        private void freezeLaunchers(ArrayList<String> disabledLauncherPackages) {
            for (String launcherPkg : disabledLauncherPackages) {
                SuperModeUtils.freezeApp(mContext, launcherPkg, false);
            }
        }

        @Override
        protected void saveCurrentToPreference(String saveKey) {
        }

        @Override
        protected boolean restoreFromSavedPreference(String saveKey) {
            return true;
        }

        @Override
        protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
            return true;
        }
    }
