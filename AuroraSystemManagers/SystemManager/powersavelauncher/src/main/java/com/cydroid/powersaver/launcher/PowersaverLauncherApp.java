// Gionee <daizm> <2013-10-11> add for CR00919205 begin 
package com.cydroid.powersaver.launcher;

import cyee.changecolors.ChameleonColorManager;
import android.app.Application;

public class PowersaverLauncherApp extends Application {
    private static final String TAG = "PowersaverLauncherApp";

    @Override
    public void onCreate() {
        super.onCreate();
        ChameleonColorManager.getInstance().register(this, false);
    }
}
