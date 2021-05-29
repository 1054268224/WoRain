package com.cydroid.softmanager.softmanager.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppManager;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftSettingsManager;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppManager;
import com.cydroid.softmanager.softmanager.interfaces.LoaderManagerCallback;
import com.cydroid.softmanager.softmanager.model.ApplicationsInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.utils.Log;

public class SoftManagerLoader extends AsyncTaskLoader<Object> {
    public static final int ID_LOADER_DEFAULT = 100;

    public static final int ID_LOADER_AUTO_BOOT = ID_LOADER_DEFAULT + 1;
    public static final int ID_LOADER_UNINSTALL = ID_LOADER_DEFAULT + 2;
    public static final int ID_LOADER_FREEZE = ID_LOADER_DEFAULT + 3;
    public static final int ID_LOADER_DEFAULTSOFT = ID_LOADER_DEFAULT + 4;
    
    private static boolean mNotLoad = false;
    
    public SoftManagerLoader(Context context) {
        super(context);
    }

    @Override
    public Object loadInBackground() {
        switch (getId()) {
            case ID_LOADER_DEFAULT:
                if (mNotLoad) {
                    setNotLoad(false);
                    break;
                }
                ApplicationsInfo.getInstance().loadAppEntries(getContext());
                ApplicationsInfo.getInstance().setAppsSummary(getContext());
                break;
            case ID_LOADER_DEFAULTSOFT:
                DefaultSoftSettingsManager defaultSoftSettingsManager = 
                    DefaultSoftSettingsManager.getInstance();
                defaultSoftSettingsManager.init(getContext());
                break;
            case ID_LOADER_FREEZE:
                FreezeAppManager freezeAppManager = FreezeAppManager.getInstance();
                freezeAppManager.init(getContext());
                break;
            case ID_LOADER_UNINSTALL:
                UninstallAppManager uninstallAppManager = UninstallAppManager.getInstance();
                uninstallAppManager.init(getContext());
                break;
            case ID_LOADER_AUTO_BOOT:
                AutoBootAppManager autoBootAppManager = AutoBootAppManager.getInstance(getContext());
                autoBootAppManager.init(getContext());
                break;
            default:
                break;
        }
        return new Object();
    }

    public static void setNotLoad(boolean notLoad) {
        mNotLoad = notLoad;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        super.onStartLoading();
    }
}
