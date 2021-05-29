/*
 *
 */
package com.cydroid.softmanager.powersaver.fragment;

import com.cydroid.framework.SIMInfoFactory;
import com.cydroid.framework.provider.SIMInfo;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemAutoLcmAcl;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemBluetooth;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemDataConnect;
import com.cydroid.softmanager.powersaver.mode.item.PowerModeItemWifi;
import com.cydroid.softmanager.utils.Log;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import cyee.app.CyeeAlertDialog;
import cyee.preference.CyeePreference.OnPreferenceChangeListener;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import cyee.preference.CyeePreference;
import cyee.preference.CyeePreferenceCategory;
import cyee.preference.CyeePreferenceFragment;
import cyee.preference.CyeePreferenceScreen;
import cyee.preference.CyeeSwitchPreference;
import cyee.preference.CyeePreferenceGroup;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

public class PowerManagerSettingsFragment extends CyeePreferenceFragment
        implements OnSharedPreferenceChangeListener {
    private static final String TAG = "PowerManagerSettingsFragment";

    // Gionee <yangxinruo> <2015-10-10> add for CR01565255 begin
    public static final String PREFERENCE_NAME = "powermanager_setting";
    // Gionee <yangxinruo> <2015-10-10> add for CR01565255 end
    // private CyeeSwitchPreference mNightPower, mShortCut;
    private CyeeSwitchPreference mShortCut;
    private Context mContext;
    PowerModeItemAutoLcmAcl mPowerModeItemScreenPowerSave;
    PowerModeItemBluetooth mPowerModeItemBluetooth;
    PowerModeItemWifi mPowerModeItemWifi;
    PowerModeItemDataConnect mPowerModeItemDataConnect;
    private static final String SCREEN_POWER_SAVE_KEY = "key_screen_power_save";
    private static final String BLUETOOTH_KEY = "key_bluetooth";
    private static final String WLAN_KEY = "key_wlan";
    private static final String DATAWORK_KEY = "key_mobile_network";
    private static final String Brightness_KEY = "key_brightness";
    private static final String Sleep_KEY = "key_sleep";
    private static final String Battery_KEY = "key_battery";

    public static final String SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private CyeePreferenceCategory mScreenAndNetworkCategory;
    private CyeeSwitchPreference mScreenPowerSavePreference;
    private CyeeSwitchPreference mWifiSwithPerference;
    private CyeeSwitchPreference mDataworkSwithPerference;
    private CyeeSwitchPreference mBluetoothSwithPerference;
    private CyeeSwitchPreference mBrightnessSwithPerference;
    private CyeePreferenceScreen mSleepPreferenceScreen;
    private CyeeSwitchPreference mBatterySwithPerference;

    private SyncBroadcastReceiver mSyncBroadcastReceiver;
    private BrightnessChangeContentObserver  mBrightnessObserver;
    private CyeeAlertDialog mAlertDialog;
    private SimChangeReceiver mSimChangeReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // Gionee <yangxinruo> <2015-10-10> add for CR01565255 begin
        this.getPreferenceManager().setSharedPreferencesName(PREFERENCE_NAME);

        if (Consts.gnVFflag || Consts.gnSwFlag || Consts.gnGTFlag || Consts.gnIPFlag ||
                Consts.cyCXFlag) {
        // Gionee xionghg 2017-05-04 modify for 130202 end
            addPreferencesFromResource(R.xml.powermanager_settings_vf);
        }else if(Consts.gnKRFlag){
            addPreferencesFromResource(R.xml.powermanager_settings_kr);
	    }else{
            addPreferencesFromResource(R.xml.powermanager_settings);
        }

        mContext = getActivity();
        mSyncBroadcastReceiver = new SyncBroadcastReceiver(mContext);
        mSimChangeReceiver = new SimChangeReceiver(mContext);
        mScreenAndNetworkCategory = (CyeePreferenceCategory) findPreference("screen_and_network");
        mScreenPowerSavePreference = (CyeeSwitchPreference) findPreference(SCREEN_POWER_SAVE_KEY);
        mWifiSwithPerference =  (CyeeSwitchPreference) findPreference(WLAN_KEY);
        mDataworkSwithPerference =  (CyeeSwitchPreference) findPreference(DATAWORK_KEY);
        mBluetoothSwithPerference =  (CyeeSwitchPreference) findPreference(BLUETOOTH_KEY);
        mBrightnessSwithPerference = (CyeeSwitchPreference) findPreference(Brightness_KEY);
        mSleepPreferenceScreen     = (CyeePreferenceScreen) findPreference(Sleep_KEY);
        mBatterySwithPerference    =  (CyeeSwitchPreference) findPreference(Battery_KEY);
        mPowerModeItemScreenPowerSave = new PowerModeItemAutoLcmAcl(mContext);
        mPowerModeItemBluetooth = new PowerModeItemBluetooth(mContext);
        mPowerModeItemWifi = new PowerModeItemWifi(mContext);
        mPowerModeItemDataConnect = new PowerModeItemDataConnect(mContext);
        mBrightnessObserver = new BrightnessChangeContentObserver();
        registerBrightnessObserver();
        refreshswithStatus();
        mSleepPreferenceScreen.setOnPreferenceClickListener(new CyeePreference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {

                showSingleChoiceConfigDialog();
                return false;
            }
            });
    }

    public  void refreshswithStatus(){
        mScreenPowerSavePreference.setChecked(mPowerModeItemScreenPowerSave.isScreenSaverOn());
        mWifiSwithPerference.setChecked(mPowerModeItemWifi.isWifiOn());
        mDataworkSwithPerference.setChecked(mPowerModeItemDataConnect.isDataConnectionOn());
        mBluetoothSwithPerference.setChecked(mPowerModeItemBluetooth.isBluetoothOn());
        int brightnessMode = Settings.System.getInt(mContext.getContentResolver(),
                SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
        mBrightnessSwithPerference.setChecked(brightnessMode != SCREEN_BRIGHTNESS_MODE_MANUAL);

        setNewSummary(getTimeoutString());

        int setting = Settings.System.getInt(getActivity().getContentResolver(),
                SHOW_BATTERY_PERCENT, 0);
        mBatterySwithPerference.setChecked(setting == 1);
        updateDataworkSwithPerference();
        updateScreenSavePerference();
    }


    private class SyncBroadcastReceiver extends BroadcastReceiver {
        private final Context mContext;

        SyncBroadcastReceiver(Context context) {
            mContext = context;
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            mContext.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr == null || actionStr.isEmpty()) {
                return;
            }
            switch (actionStr) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    refreshswithStatus();

                default:
                    break;
            }
        }

        public void remove() {
            mContext.unregisterReceiver(this);
        }
    }



    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        refreshswithStatus();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mSyncBroadcastReceiver.remove();
        mSimChangeReceiver.remove();
        unRegisterBrightnessObserver();
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(BLUETOOTH_KEY)) {
            mPowerModeItemBluetooth.setBluetooth(sharedPreferences.getBoolean(BLUETOOTH_KEY, false));
        } else if (key.equals(WLAN_KEY)) {
            mPowerModeItemWifi.setWifiState(sharedPreferences.getBoolean(WLAN_KEY, false));
        }else if(key.equals(DATAWORK_KEY)){
            mPowerModeItemDataConnect.setDataConnectionState(sharedPreferences.getBoolean(DATAWORK_KEY, false));
        } else if(key.equals(Brightness_KEY)){
            if (sharedPreferences.getBoolean(Brightness_KEY, false)) {
                setBrightnessMode(SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            } else {
                setBrightnessMode(SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
       }else if(key.equals(Battery_KEY)){
            boolean showPercentage = sharedPreferences.getBoolean(Battery_KEY, false);
            try {
                Settings.System.putInt(getActivity().getContentResolver(), SHOW_BATTERY_PERCENT,
                        showPercentage ? 1 : 0);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist battery display style setting", e);
            }
        }else if(key.equals(SCREEN_POWER_SAVE_KEY)){
           boolean defaultValue = true;
           if (Consts.cyBAFlag){
               defaultValue = false;
           }
           boolean isOpen = sharedPreferences.getBoolean(SCREEN_POWER_SAVE_KEY, defaultValue);
           mPowerModeItemScreenPowerSave.enableAmoledLcm(isOpen? 0 : 1);
        }
    }

    private void setBrightnessMode(int mode) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("15000");
        res.add("30000");
        res.add("60000");
        res.add("120000");
        res.add("300000");
        res.add("600000");
        res.add("1800000");
        return res;
    }

    private int getScreenTimeout() {
        int defTimeout = 15000;
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                defTimeout);
    }

    /* screen timeout controller */
    private void setScreenTimeout(int timeout) {
        int value = timeout;
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, value);
    }



    public ArrayList<String> getCandidateValueDescs() {
        ArrayList<String> decs = getAvailCandidateValues();
        for (int i = 0; i < decs.size(); i++) {
            String dec = decs.get(i);
            int decVal = 0;
            try {
                decVal = Integer.parseInt(dec);
                String newValStr = "";
                if (decVal > 60000) {
                    newValStr = mContext.getResources().getString(R.string.mode_item_timeout_ms,
                            decVal / 60000);
                    //chenyee zhaocaili modify for CSW1803A-1667 begin
                    newValStr = setArabiaLanguage(newValStr, decVal);
                    //chenyee zhaocaili modify for CSW1803A-1667 end
                } else if (decVal == 60000) {
                    newValStr = mContext.getResources().getString(R.string.mode_item_timeout_m,
                            decVal / 60000);
                } else {
                    newValStr = mContext.getResources().getString(R.string.mode_item_timeout_s,
                            decVal / 1000);
                }
                decs.set(i, newValStr);
            } catch (NumberFormatException nfe) {
                continue;
            } catch (Resources.NotFoundException nofe) {
                continue;
            } catch (Exception ex) {
                continue;
            }
        }
        return decs;
    }


    private String getTimeoutString() {
        final ArrayList<String> vals = getAvailCandidateValues();
        final ArrayList<String> valsStr = getCandidateValueDescs();

        int currentTimeout = getScreenTimeout();
        int selectedIndex = vals.size() - 1;
        for (int i = 0; i < vals.size(); i++) {
            if (currentTimeout == Integer.parseInt(vals.get(i))) {
                selectedIndex = i;
                break;
            }
        }
        return valsStr.get(selectedIndex);

    }

    private void showSingleChoiceConfigDialog() {
        final ArrayList<String> vals = getAvailCandidateValues();
        final ArrayList<String> valsStr = getCandidateValueDescs();
        int currentTimeout = getScreenTimeout();
        int selectedIndex = vals.size() - 1;
        for (int i = 0; i < vals.size(); i++) {
            if (currentTimeout == Integer.parseInt(vals.get(i))) {
                selectedIndex = i;
                break;
            }
        }
        String[] valsArray = new String[valsStr.size()];
        valsArray = valsStr.toArray(valsArray);
        mAlertDialog = new CyeeAlertDialog.Builder(getActivity()).setTitle(mContext.getResources().getString(R.string.set_sleep_time))
                .setSingleChoiceItems(valsArray, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectVal = vals.get(which);
                        String selectValstr = valsStr.get(which);
                        setScreenTimeout(Integer.parseInt(selectVal));
                        mAlertDialog.dismiss();

                        setNewSummary(selectValstr);
                    }

                }).setNegativeButton(R.string.mode_item_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog.dismiss();
                    }
                }).create();
        mAlertDialog.show();
    }

    private void setNewSummary(String selectValstr) {
        String configValStr = String.format(
                getResources().getString(R.string.sleep_summary),selectValstr);
        mSleepPreferenceScreen.setSummary(configValStr);

    }
    private void registerBrightnessObserver() {
        mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SCREEN_BRIGHTNESS_MODE),
                true, mBrightnessObserver);
    }

    private void unRegisterBrightnessObserver() {
        mContext.getContentResolver().unregisterContentObserver(mBrightnessObserver);
    }

    class BrightnessChangeContentObserver extends ContentObserver {
        public BrightnessChangeContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            refreshswithStatus();
        }
    }

    private class SimChangeReceiver extends BroadcastReceiver {
        private final Context mContext;
        private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
        private final static int SIM_VALID = 0;
        private final static int SIM_INVALID = 1;
        private int simState = SIM_INVALID;

        SimChangeReceiver(Context context) {
            mContext = context;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_SIM_STATE_CHANGED);
            mContext.registerReceiver(this, filter);
        }

        public int getSimState() {
            return simState;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
                TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
                simState = tm.getSimState();
                Log.d(TAG, "SimChangeReceiver OnReceive SimState = " + simState);
                updateDataworkSwithPerference();
            }
        }

        public void remove() {
            mContext.unregisterReceiver(this);
        }
    }

    private void updateDataworkSwithPerference(){
        List<SIMInfo> simInfoList = SIMInfoFactory.getDefault().getInsertedSIMList(mContext);
        if (simInfoList != null && simInfoList.size() > 0){
            if (mSimChangeReceiver.getSimState() == TelephonyManager.SIM_STATE_READY){
                mScreenAndNetworkCategory.addPreference(mDataworkSwithPerference);
            }else {
                mScreenAndNetworkCategory.removePreference(mDataworkSwithPerference);
            }
            return;
        }
        mScreenAndNetworkCategory.removePreference(mDataworkSwithPerference);
    }

    private void updateScreenSavePerference(){
        boolean isSupport = mPowerModeItemScreenPowerSave.isUseAALSupport();
        if (isSupport){
            mScreenAndNetworkCategory.addPreference(mScreenPowerSavePreference);
            return;
        }
        mScreenAndNetworkCategory.removePreference(mScreenPowerSavePreference);
    }

    //chenyee zhaocaili modify for CSW1803A-1667 begin
    private String setArabiaLanguage(String timeStr, int time){
        Resources res = mContext.getResources();
        Configuration config = res.getConfiguration();
        String language = config.locale.getLanguage();
        String country = config.locale.getCountry();
        if (country.equals("DZ") && language.equals("ar") && (time == 120000 || time == 1800000)){
            if (time == 120000){
                return "دقيقتين";
            }else {
                return "دقيقة 30";
            }
        }
        return timeStr;
    }
    //chenyee zhaocaili modify for CSW1803A-1667 end
}
