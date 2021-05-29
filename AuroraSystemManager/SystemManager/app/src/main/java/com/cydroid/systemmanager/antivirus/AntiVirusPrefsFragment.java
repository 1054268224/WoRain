package com.cydroid.systemmanager.antivirus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.intel.security.Properties;
import com.intel.security.SecurityContext;
import com.intel.security.vsm.RealTimeScan;
import com.intel.security.vsm.UpdateObserver;
import com.intel.security.vsm.UpdateTask;
import com.intel.security.vsm.VirusScan;

import cyee.preference.CyeePreference;
import cyee.preference.CyeePreference.OnPreferenceClickListener;
import cyee.preference.CyeePreferenceFragment;
import cyee.preference.CyeePreferenceScreen;
import cyee.preference.CyeeSwitchPreference;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.ServiceUtil;

public class AntiVirusPrefsFragment extends CyeePreferenceFragment implements
        OnSharedPreferenceChangeListener {
    private static final String TAG = "AntiVirusPrefsFragment";

    private Context mAppContext;
    // private static final String CLOUD_KEY = "cloud_key";
    public static final String APP_INSTALL_MONITOR_KEY = "app_install_monitor_key";
    public static final String REAL_TIME_MONITOR_KEY = "real_time_monitor_key";
    public static final String AUTO_UPDATE_KEY = "auto_update_key";
    private static final String MANUAL_UPDATE_KEY = "manual_update_key";
    private CyeeSwitchPreference mAppInstallMonitor, mRealTimeMonitor, mAutoUpdate;// mAntiVirusCloud
    private CyeePreferenceScreen mManualUpdateScreen;
    private SharedPreferences mAutoUpdatePreferences;
    private ConfigLoader mConfigLoader = null;

    private Object SYNC_UPDATE = new Object();
    private UpdateTask mUpdateTask = null;
    private VirusScan mVirusScan = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppContext = getActivity().getApplicationContext();
        initView();
        initData();
        mVirusScan = (VirusScan) SecurityContext.getService(getActivity().getApplicationContext(), SecurityContext.VIRUS_SCAN);
    }


    private void doCancelUpdateEngine() {
        if (mVirusScan == null) {
            return;
        }
        Log.d(TAG, "cancel update");
        synchronized (SYNC_UPDATE) {
            if (mUpdateTask != null) {
                mUpdateTask.cancel();
            }
        }
    }

    private void initData() {
        mAutoUpdatePreferences = mAppContext.getSharedPreferences(AUTO_UPDATE_KEY, Context.MODE_PRIVATE);
        mConfigLoader = new ConfigLoader(mAppContext);
        mConfigLoader.load();
        /*
         * ApplicationInfo appInfo = HelperUtils.getApplicationInfo(mAppContext,
         * "com.cydroid.softmanager"); mAppNetworkControl =
         * AppNetworkControl.getInstance(mAppContext); mUid = appInfo.uid;
         * Log.e("dzmdzm", "Uid:"+mUid);
         */
    }

    private void initView() {
        if (SystemProperties.get("ro.gn.app.securepay.support", "no").equals("yes")) {
            addPreferencesFromResource(R.xml.antivirus_settings_payprotect);
        } else {
            addPreferencesFromResource(R.xml.antivirus_settings);
            mAppInstallMonitor = (CyeeSwitchPreference) findPreference(APP_INSTALL_MONITOR_KEY);
            mRealTimeMonitor = (CyeeSwitchPreference) findPreference(REAL_TIME_MONITOR_KEY);
        }

        // mAntiVirusCloud = (CyeeSwitchPreference) findPreference(CLOUD_KEY);
        mAutoUpdate = (CyeeSwitchPreference) findPreference(AUTO_UPDATE_KEY);
        mManualUpdateScreen = (CyeePreferenceScreen) findPreference(MANUAL_UPDATE_KEY);
        setManualClickListener();
    }

    private void setManualClickListener() {
        mManualUpdateScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {
                /*guoxt modify for VSM begin */
                //update();
                doUpdateEngine();
                /*guoxt modify for VSM end */

                return false;
            }
        });
    }

    Handler handler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case UpdateObserver.RESULT_SUCCEEDED:
                    Toast.makeText(mAppContext, R.string.update_finish,
                            Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(mAppContext, R.string.update_failed,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    public void doUpdateEngine() {
        Log.e("guoxt", "mManualUpdateScreen is clicked");
        // Gionee <houjie> <2015-10-22> add for CR01564093 begin
        if (!isNetworkAvailable()) {
            Toast.makeText(mAppContext, R.string.update_failed,
                    Toast.LENGTH_SHORT).show();
            Log.d("guoxt", "startUpdate failed : network does not available");
            return;
        }
        // Gionee <houjie> <2015-10-22> add for CR01564093 end

        if (mVirusScan == null) {
            return;
        }

        if (mUpdateTask != null) {
            Log.d("guoxt:", "update-progress:" + (int) mUpdateTask.getState().getProgress() * 100);
            return;

        }
        Toast.makeText(mAppContext, R.string.update_start_check, Toast.LENGTH_SHORT).show();

        synchronized (SYNC_UPDATE) {
            mUpdateTask = mVirusScan.update(new UpdateObserver() {
                @Override
                public void onStarted() {
                    Log.d("guoxt", "update started");
                }

                @Override
                public void onCompleted(int i) {
                    // UpdateObserver.RESULT_SUCCEEDED  0
                    // UpdateObserver.RESULT_CANCELED   1
                    handler.sendEmptyMessage(i);
                    Log.d("guoxt", "update completed, result is " + i);
                    Log.d("guoxt", "success: " + UpdateObserver.RESULT_SUCCEEDED);
                    Log.d("guoxt", "cancel: " + UpdateObserver.RESULT_CANCELED);
                    synchronized (SYNC_UPDATE) {
                        // Remove reference to update task once update done.
                        mUpdateTask = null;
                    }

                    // Gets signature database version
                    String dat = mVirusScan.getProperties().getString(Properties.KEY_DAT_VERSION);
                    // Gets scan engine version
                    String mcs = mVirusScan.getProperties().getString(Properties.KEY_MCS_VERSION);

                    Log.d(TAG, "update completed, dat is " + dat);
                    Log.d(TAG, "update completed, mcs is " + mcs);

                }
            });
            float progress = mUpdateTask.getState().getProgress();
            Log.d("guoxt:", "update-progress:" + (int) mUpdateTask.getState().getProgress() * 100);
        }
    }

    // Gionee <houjie> <2015-10-22> add for CR01564093 begin
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mAppContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    // Gionee <houjie> <2015-10-22> add for CR01564093 end

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void setRealTimeScan(boolean flag) {
        VirusScan mVirusScan = (VirusScan) SecurityContext
                .getService(mAppContext, SecurityContext.VIRUS_SCAN);
        RealTimeScan realTimeScan = mVirusScan.getRealTimeScan();

        String[] types = new String[]{RealTimeScan.REAL_TIME_SCAN_MESSAGE,
                RealTimeScan.REAL_TIME_SCAN_PACKAGE, RealTimeScan.REAL_TIME_SCAN_FILE};
        if (flag) {
            realTimeScan.enable(RealTimeScan.REAL_TIME_SCAN_MESSAGE);
            realTimeScan.enable(RealTimeScan.REAL_TIME_SCAN_PACKAGE);
            realTimeScan.enable(RealTimeScan.REAL_TIME_SCAN_FILE);

            for (String scanType : types) {
                if (realTimeScan.isEnabled(scanType)) {
                    Log.d(TAG, scanType + " is enabled sucess!");
                    Intent startIntent = new Intent(mAppContext, ForegroundService.class);
                    ServiceUtil.startForegroundService(mAppContext,startIntent);
                }
            }
        } else {
            realTimeScan.disable(RealTimeScan.REAL_TIME_SCAN_MESSAGE);
            realTimeScan.disable(RealTimeScan.REAL_TIME_SCAN_PACKAGE);
            realTimeScan.disable(RealTimeScan.REAL_TIME_SCAN_FILE);
            Log.d(TAG, "stopService");
            Intent intent = new Intent(mAppContext, ForegroundService.class);
            mAppContext.stopService(intent);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*
         * if (key.equals(CLOUD_KEY)) { if (mAntiVirusCloud.isChecked()) {
         * Log.e("dzmdzm", "mAntiVirusCloud.isChecked()") ;
         * mAppNetworkControl.singleNetworkControlAllow(mUid); }else {
         * Log.e("dzmdzm", "mAntiVirusCloud.isChecked() 11") ;
         * mAppNetworkControl.singleNetworkControlProhibit(mUid); }
         * 
         * }
         */

        if (key.equals(APP_INSTALL_MONITOR_KEY) && mAppInstallMonitor != null) {
            if (mAppInstallMonitor.isChecked()) {
                mAutoUpdatePreferences.edit().putBoolean(APP_INSTALL_MONITOR_KEY, true).commit();
                // Log.e("dzmdzm", "mAppInstallMonitor.isChecked()") ;
            } else {
                mAutoUpdatePreferences.edit().putBoolean(APP_INSTALL_MONITOR_KEY, false).commit();
                // Log.e("dzmdzm", "mAppInstallMonitor.isChecked() 11") ;
            }
        } else if (key.equals(AUTO_UPDATE_KEY)) {
            if (mAutoUpdate.isChecked()) {
                mAutoUpdatePreferences.edit().putBoolean(AUTO_UPDATE_KEY, true).commit();
                // Log.e("dzmdzm", "mAutoUpdate.isChecked()") ;

            } else {
                mAutoUpdatePreferences.edit().putBoolean(AUTO_UPDATE_KEY, false).commit();
                // Log.e("dzmdzm", "mAutoUpdate.isChecked() 11") ;
            }
        } else if (key.equals(REAL_TIME_MONITOR_KEY)) {
            if (mRealTimeMonitor.isChecked()) {
                mAutoUpdatePreferences.edit().putBoolean(REAL_TIME_MONITOR_KEY, true).commit();
                setRealTimeScan(true);

                //real-time SCAN
            } else {
                mAutoUpdatePreferences.edit().putBoolean(REAL_TIME_MONITOR_KEY, false).commit();
                setRealTimeScan(false);
            }
        }
    }

}
