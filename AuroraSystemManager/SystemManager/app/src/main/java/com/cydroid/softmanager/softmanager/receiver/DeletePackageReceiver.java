package com.cydroid.softmanager.softmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.IPackageDeleteObserver;
import android.os.Handler;
import android.os.Message;

import cyee.app.CyeeActivity;

import com.cydroid.softmanager.utils.Log;

public class DeletePackageReceiver extends BroadcastReceiver {
    private static final String DELETE_PACKAGE_ACTION = "com.chenyee.action.DELETE_PACKAGE";
    private static final int UNINSTALL_COMPLETE = 1;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String actionStr = intent.getAction();
        if (DELETE_PACKAGE_ACTION.equals(actionStr)) {
            final String pkgName = intent.getStringExtra("DELETE_PACKAGE_NAME");
            Log.e("dzmdzm", "pkgname:" + pkgName);
            new Thread() {
                public void run() {
                    context.getPackageManager().deletePackage(pkgName, null, 0);
                }
            }.start();
        }
    }
}

