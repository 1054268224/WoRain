package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.mode.SuperModeUtils;
import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;

public class PowerModeItemDisableApps extends PowerModeItem {

    private static final String FROZEN_APPS_LIST_PTAH = "/data/misc/gionee/softmanager_supermode_freezed_apps.obj";
    private static final String FROZEN_APPS_LIST_PTAH_MSDATA = "/data/misc/msdata/softmanager_supermode_freezed_apps.obj";
    private final ArrayList<String> mToBeDisabledApps = new ArrayList<String>();

    public PowerModeItemDisableApps(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
        initAppInfo();
    }

    /*guoxt modify for CSW1702A-2175 begin*/
    public static String getAppListPath() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            return FROZEN_APPS_LIST_PTAH;
        }else{
            return FROZEN_APPS_LIST_PTAH_MSDATA;
        }
    }
    /*guoxt modify for CSW1702A-2175 end*/

    private void initAppInfo() {
        List<ApplicationInfo> applicationInfos;
        if (SystemProperties.get("ro.encryptionspace.enabled", "false").equals("true")) {
            // 确保极致省电可以冻结私密应用
            applicationInfos = HelperUtils.getApplicationInfo2(mContext, HelperUtils.getEncryptAppFlag());
        } else {
            applicationInfos = HelperUtils.getApplicationInfo2(mContext, 0);
        }
        List<ApplicationInfo> launcherShowApps = HelperUtils.getApplicationInfos(mContext);
        applicationInfos.addAll(launcherShowApps);
        for (ApplicationInfo appInfo : applicationInfos) {
            if (!mToBeDisabledApps.contains(appInfo.packageName)) {
                mToBeDisabledApps.add(appInfo.packageName);
            }
        }
    }

    public void setExcludeApps(ArrayList<String> appsList) {
        for (String pkgName : appsList) {
            if (mToBeDisabledApps.contains(pkgName)) {
                Log.d(TAG, "exclude pkg ----->" + pkgName);
                mToBeDisabledApps.remove(pkgName);
            }
        }
    }

    @Override
    public void save() {
        saveListToFile(getAppListPath(), mToBeDisabledApps);
    }

    private void saveListToFile(String filePath, ArrayList<String> appList) {
        ObjectOutputStream out = null;
        try {
            File objFile = new File(filePath);
            if (objFile.exists()) {
                boolean res = objFile.delete();
                Log.d(TAG, "remove old file res = " + res);
            }
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
        ArrayList<String> disabledPackages = getListFromFile(getAppListPath());
        unfreezeApps(disabledPackages);
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

    private void unfreezeApps(ArrayList<String> disabledPackages) {
        for (String appPkg : disabledPackages) {
            // <!-- guoxt 2018-03-31 add for CSW1702A-3063 begin -->
            //if(!appPkg.contains("com.google")) {
                 if(appPkg.contains("com.google.android.marvin.talkback")){
                    continue;
                }
                SuperModeUtils.unFreezeApp(mContext, appPkg, false);
           // }
            // <!-- guoxt 2018-03-31 add for CSW1702A-3063 begin -->
        }
    }


    @Override
    public boolean apply() {
        if (!hasConfig()) {
            Log.d(TAG, "no settings " + mConfigKey + " ,do nothing");
            return true;
        }
        Log.d(TAG, "apply setting from key:" + mConfigKey);
        ArrayList<String> disabledPackages = getListFromFile(getAppListPath());
        List<String> disabledComponents = HelperUtils.getPowerSaveNeedDisable(mContext);
        disableDisruptingComponents(disabledComponents, disabledPackages);
        freezeApps(disabledPackages);
        return true;
    }

    private void disableDisruptingComponents(List<String> disabledComponents,
            ArrayList<String> disabledPackages) {
        Log.d(TAG, "disable processDisableSpecialComponent");
        try {
            for (String component : disabledComponents) {
                String[] com = component.split("/");
                Log.d(TAG, "scan processDisableComponent --->pkg:" + com[0] + " cmp:" + com[1]);
                if (!disabledPackages.contains(com[0])) {
                    continue;
                }
                List<ComponentName> listCom = getComponentDatas(new Intent(com[1]), com[0]);
                for (ComponentName cn : listCom) {
                    disableComponent(cn);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "processDisableComponent ------->", e);
        }
    }

    private List<ComponentName> getComponentDatas(Intent intent, String pkgName) {
        List<ComponentName> comList = new ArrayList<ComponentName>();
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> connectivityresolves = pm.queryBroadcastReceivers(intent,
                PackageManager.GET_INTENT_FILTERS | PackageManager.GET_DISABLED_COMPONENTS);
        for (int i = 0; i < connectivityresolves.size(); i++) {
            ResolveInfo ri = connectivityresolves.get(i);
            ActivityInfo ai = ri.activityInfo;
            if (ai != null) {
                if (!pkgName.equals(ai.applicationInfo.packageName)) {
                    continue;
                }
                ComponentName component = new ComponentName(ai.applicationInfo.packageName, ai.name);
                comList.add(component);
                Log.i(TAG, "component add -------> " + component.toShortString());
            }
        }
        return comList;
    }

    private void disableComponent(ComponentName componet) {
        try {
            PackageManager pm = mContext.getPackageManager();
            pm.setComponentEnabledSetting(componet, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            Log.i(TAG, "disableComponent ------->" + componet.toShortString());
        } catch (Exception e) {
            Log.i(TAG, "disableComponent ------->", e);
        }
    }
//xuanyutag
    private void freezeApps(ArrayList<String> disabledPackages) {
       // Log.i(TAG,"cxy trace");
       // new Throwable().printStackTrace();
        for (String appPkg : disabledPackages) {
            // <!-- guoxt 2018-03-31 add for CSW1702A-3063 begin -->
           // if(!appPkg.contains("com.google")) {
                Log.i(TAG,"cxy freezeApps="+appPkg);
                if(appPkg.contains("com.google.android.marvin.talkback")){
                    continue;
                }
                SuperModeUtils.freezeApp(mContext, appPkg, false);//xuanyutag
            //}
            // <!-- guoxt 2018-03-31 add for CSW1702A-3063 end -->
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
