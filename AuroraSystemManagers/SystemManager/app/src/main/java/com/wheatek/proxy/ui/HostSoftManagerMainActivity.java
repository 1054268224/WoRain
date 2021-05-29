package com.wheatek.proxy.ui;

import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.interfaces.ThemeChangedCallback;
import com.cydroid.softmanager.interfaces.TimeChangedCallback;
import com.cydroid.softmanager.softmanager.loader.SoftManagerIconLoader;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppUtils;
import com.example.systemmanageruidemo.UnitUtil;
import com.example.systemmanageruidemo.actionpresent.SoftManagerPresent;
import com.example.systemmanageruidemo.actionview.SoftManagerView;
import com.example.systemmanageruidemo.softmanager.SoftManagerMainActivity;
import com.example.systemmanageruidemo.softmanager.adapter.SoftItem;
import com.wheatek.utils.CMDUtils;
import com.wheatek.utils.UninstallPackageManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HostSoftManagerMainActivity extends HostProxyActivity<SoftManagerView> implements SoftManagerPresent, StateChangeCallback, TimeChangedCallback, LocalChangedCallback, ThemeChangedCallback {
    private SoftManagerIconLoader mSoftManagerIconLoader;
    private IPackageDeleteObserver.Stub mIPackageDeleteObserver;

    {
        attach(new SoftManagerMainActivity());
    }

    SoftManagerView viewAvtion;

    @Override
    public void setViewAction(SoftManagerView viewAvtion) {
        this.viewAvtion = viewAvtion;
    }

    @Override
    public SoftManagerView getViewAction() {
        return viewAvtion;
    }

    List<SoftItem> list;
    private List<UninstallAppInfo> mUninstallApps = new ArrayList<>();

    @Override
    public void onInitData(List<SoftItem> list) {
        this.list = list;
        refreshList(true);
    }

    private final UninstallAppManager mUninstallAppManager = UninstallAppManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUninstallAppManager.setAppsChangeCallBack(String.valueOf(this.hashCode()), this);
        mUninstallAppManager.setTimeChangeCallBack(String.valueOf(this.hashCode()), this);
        mUninstallAppManager.setLocalChangeCallBack(String.valueOf(this.hashCode()), this);
        mUninstallAppManager.setThemeChangedCallback(String.valueOf(this.hashCode()), this);
        mIPackageDeleteObserver = new IPackageDeleteObserver.Stub() {

            @Override
            public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                finishPosition(returnCode == 0, packageName);
                con--;
                if (con == 0) {
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    refreshList(false);
                                }
                            }
                    ).start();
                }
            }
        };
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.host_bar_bg_white)));
        getSupportActionBar().setElevation(0.0f);
        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUninstallAppManager.unsetAppsChangeCallBack(String.valueOf(this.hashCode()));
        mUninstallAppManager.unsetTimeChangeCallBack(String.valueOf(this.hashCode()));
        mUninstallAppManager.unsetLocalChangeCallBack(String.valueOf(this.hashCode()));
        mUninstallAppManager.unsetThemeChangedCallback(String.valueOf(this.hashCode()));
        mUninstallApps.clear();
    }

    private void refreshList(boolean isfirst) {
        List<UninstallAppInfo> uninstallApps = mUninstallAppManager.
                getAllUninstallAppsByShowType(2);
        for (UninstallAppInfo uninstallApp : uninstallApps) {
            boolean iscunzai;
            for (UninstallAppInfo mUninstallApp : mUninstallApps) {
                if (uninstallApp.getPackageName().equals(mUninstallApp.getPackageName())) {
                    iscunzai = true;
                    if (mUninstallApp.getIcon() != null) {
                        uninstallApp.setIcon(mUninstallApp.getIcon());
                    }
                    break;
                }
            }
        }
        mUninstallApps.clear();
        mUninstallApps.addAll(uninstallApps);
        if (isfirst) {
            mSoftManagerIconLoader = new SoftManagerIconLoader(HostSoftManagerMainActivity.this, mUninstallApps);
            mSoftManagerIconLoader.loadAppIcon(new SoftManagerIconLoader.ImageLoadCompleteCallback() {
                @Override
                public void loadComplete() {
                    mUninstallApps = (List<UninstallAppInfo>) mSoftManagerIconLoader.getListDatas();
                    refreshList2(list);
                }
            });
        } else {
            refreshList2(list);
        }
    }


    private void refreshList2(List<SoftItem> list) {
        if (list == null) return;
        Iterator<SoftItem> i = list.iterator();
        while (i.hasNext()) {
            SoftItem item = i.next();
            boolean r = false;
            for (UninstallAppInfo itemInfo : mUninstallApps) {
                if (item.getPackagename().equals(itemInfo.getPackageName())) {
                    r = true;
                    break;
                }
            }
            if (!r) {
                i.remove();
            }
        }
        for (UninstallAppInfo itemInfo : mUninstallApps) {
            boolean r = false;
            for (SoftItem itemInfo2 : list) {
                if (itemInfo2.getPackagename().equals(itemInfo.getPackageName())) {
                    itemInfo2.setImageId(itemInfo.getIcon());
                    r = true;
                    break;
                }
            }
            if (!r) {
                SoftItem bootItem = new SoftItem(itemInfo.getPackageName(), itemInfo.getIcon(), itemInfo.getTitle(), UnitUtil.convertStorage(itemInfo.getPackageSize()), false);
                list.add(bootItem);
            }
        }
        refreshData(list);
    }


    int con = 0;

    @Override
    public void onSelectListdo(List<SoftItem> list) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (SoftItem softItem : list) {
                    if (softItem.getTrue()) {
                        con++;
                    }
                }
                for (SoftItem softItem : list) {
                    if (softItem.getTrue()) {
                        UninstallPackageManager.DeletePackage(softItem.getPackagename(), mIPackageDeleteObserver, HostSoftManagerMainActivity.this);
                    }
                }
            }
        }).start();
    }

    @Override
    public void refreshData(List<SoftItem> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewAvtion.onRefresh(list);
            }
        });
    }

    @Override
    public void finishPosition(boolean success, String packagename) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewAvtion.onFinishPosition(success, packagename);
            }
        });
    }

    @Override
    public void onStateChange() {

    }

    @Override
    public void onTimeChange() {

    }

    @Override
    public void onLocalChange() {

    }

    @Override
    public void changeTheme(String category) {

    }
}