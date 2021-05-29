package com.cydroid.softmanager.systemcheck;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.android.settings.MiraVisionJni;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.softmanager.UninstallAppActivity;
import com.cydroid.softmanager.softmanager.model.SDCardInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.trafficassistant.SIMInfoWrapper;
import com.cydroid.softmanager.trafficassistant.TrafficLimitActivity;
import com.cydroid.softmanager.trafficassistant.TrafficNetworkControlActivity;
import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.StringFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficPreference;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import android.net.NetworkPolicyManager;
import android.os.SystemProperties;
import com.chenyee.featureoption.ServiceUtil;

import cyee.provider.CyeeSettings;

/**
 * Created by zhaocaili on 18-8-4.
 */

public class SystemCheck {

    /**
     *判断Wlan状态，如果Wlan开关开启并且没有连接到网络，则返回true(此时耗电)，否则返回false
     **/
    public static boolean checkSystemWlanIsDisconnected(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(wifiManager != null){
            int wifiState = wifiManager.getWifiState();
            if (wifiState == WifiManager.WIFI_STATE_ENABLED || wifiState == WifiManager.WIFI_STATE_ENABLING){
                NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return  (networkInfo == null || !networkInfo.isConnected());
            }
        }
        return false;
    }

    /**
     *判断蓝牙状态，如果蓝牙开关开启并且没有连接蓝牙设备，则返回true(此时耗电)，否则返回false
     **/
    public static  boolean checkBluetoothIsOpenedButNotInUse(){
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (blueadapter != null){
            if (blueadapter.isEnabled()){
                Set<BluetoothDevice> devices = blueadapter.getBondedDevices();
                for (BluetoothDevice device : devices){
                    boolean connected = device.isConnected();
                    if (connected){
                        return false;
                    }
                }
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    /**
     *判断GPS状态，如果GPS开关开启，则返回true(此时耗电)，否则返回false
     **/
    public static  boolean checkGPSIsOpened(Context context){
        int locationMode = 0;
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    /**
     *判断动作手势状态，如果动作手势开关开启，则返回true(此时耗电)，否则返回false
     **/
    public static  boolean checkGestureIsOpened(Context context){
        return CyeeSettings.getInt(context.getContentResolver(), CyeeSettings.GN_SSG_SWITCH, 0) == 1;
    }

    /**
     * 获得休眠时间 毫秒
     */
    public static  int getScreenOffTime(Context context) {
        int screenOffTime = 0;
        try {
            screenOffTime = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception localException) {

        }
        return screenOffTime;
    }

    /**
     *判断手机剩余存储空间,info[0]剩余空间是否小于10％,是则返回true(清理加速)，否则返回false;info[1]剩余空间字串
     **/
    public static  Object[] checkRemainingSpace(SoftHelperUtils mStorageHelper){
        Object[] info = new Object[2];
        SDCardInfo romInfo = mStorageHelper.getInternalStorageInfo();
        long free = romInfo.mFree;
        long total = romInfo.mTotal;
        float freePercent = free * 1.0f / total;
        info[0] = freePercent < 0.1;
        info[1] = SoftHelperUtils.convertStorage(free);
        return info;
    }

    /**
     *判断锁屏清理开关是否开启，如果开关开启，则返回true(清理加速)，否则返回false
     **/
    public static  boolean checkCleanOnLockScreenIsOpened(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean defaultValue = true;
        if (Consts.gnIPFlag || Consts.cy1703VF || Consts.cyBAFlag) {
            defaultValue = false;
        }
        return pref.getBoolean("screenoff_clean_key", defaultValue);
    }

    /**
     *判断智能睡眠提醒是否开启，如果开关开启，则返回true(省电功能)，否则返回false
     **/
    public static  boolean checkIntelligentSleepIsOpened(Context context){
        SharedPreferences settingPreference = context.getSharedPreferences("powermanager_setting", Context.MODE_MULTI_PROCESS);
        if (Consts.gnVFflag || Consts.gnSwFlag || Consts.gnGTFlag || Consts.gnIPFlag||Consts.cyCXFlag) {
            return settingPreference.getBoolean(Consts.NIGHT_MODE, false);
        } else {
            return settingPreference.getBoolean(Consts.NIGHT_MODE, true);
        }
    }

    public static boolean isScreenPowerSaveSupport(){
        return SystemProperties.get("ro.mtk.aal.support", "no").equals("yes");
    }

    /**
     *判断屏幕省电功能是否开启，如果开关开启，则返回true(省电功能)，否则返回false
     **/
    public static  boolean checkScreenPowerSavingIsOpened(){
        int aalValue = MiraVisionJni.getAALFunction();
        return aalValue == MiraVisionJni.getDefaultAALFunction() || aalValue == MiraVisionJni.AAL_FUNC_CABC;
    }

    /**
     *判断热点功能状态，如果热点开启并且没有连接设备，则返回true(此时耗电)，否则返回false
     **/
    public static  boolean checkHotpotIsOpened(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int state = wifiManager.getWifiApState();
        if (state == WifiManager.WIFI_AP_STATE_ENABLED || state == WifiManager.WIFI_AP_STATE_ENABLING){
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader("/proc/net/arp"));//读取这个文件
                String line;
                while ((line = br.readLine()) != null) {
                    String[] splitted = line.split(" +");//将文件里面的字段分割开来
                    if (splitted.length >= 4) {
                        String ip = splitted[0];
                        Log.d("", "WLAN　ip = " + ip);
                        if (ip != null) {
                            try {
                                if((0 == Runtime.getRuntime().exec("ping -c 1 -w 1 " + ip).waitFor())){
                                    Log.d("", "ping success");
                                    return false;
                                }
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                                Log.d("", "ping fail");
                                return true;
                            }
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                //Log.e(e.getMessage());
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    //CandyLog.e(e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     *判断是否有插sim卡，如果有插，则判断后续的流量套餐设置、剩余流量
     **/
    public static boolean checkIfSimInserted(Context context){
        SIMInfoWrapper wrapper = SIMInfoWrapper.getDefault(context);
        int count = wrapper.getInsertedSimCount();
        return count > 0;
    }

    /**
     *判断当前数据连接的卡是否设置了流量套餐，若设置返回true，否则返回false,提示用户去设置套餐
     **/
    public static  boolean checkTrafficSetHasSet(Context context){
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int defaultDataSubId = simInfo.getSimIndex_CurrentNetworkActivated();
        if (defaultDataSubId == -1){
            defaultDataSubId = simInfo.getInsertedSimInfo().get(0).mSlot;
        }
        boolean isSetted = TrafficPreference.getSimBooleanPreference(context, defaultDataSubId,
                TrafficPreference.KEY_TRAFFIC_PACKAGE_SETTED_FLAG, false);
        Log.d("", "has slot " + defaultDataSubId + " seted? = " + isSetted);
        return isSetted;
    }

    /**
     *判断当前数据连接的卡剩余流量是否足够，如果大于５M，则返回trur,否则返回false,提示用户设置联网应用
     **/
    public static  Object[] checkFreeTrafficEnough(Context context){
        Object[] objects = new Object[3];
        TrafficCalibrateControler mTrafficCalibrateControler = TrafficCalibrateControler.getInstance(context);
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int defaultDataSubId = simInfo.getSimIndex_CurrentNetworkActivated();
        if (defaultDataSubId == -1){
            defaultDataSubId = simInfo.getInsertedSimInfo().get(0).mSlot;
        }
        float used = mTrafficCalibrateControler.getCommonUsedTaffic(context, defaultDataSubId);
        float commonLeft = mTrafficCalibrateControler.getCommonLeftTraffic(context, defaultDataSubId);
        Log.d("", "slot " + defaultDataSubId + " used = " + used + "     free = " + commonLeft);

        objects[0] = !(commonLeft < 5);

        boolean over = !mTrafficCalibrateControler.isCommonTrafficSurplus(context, defaultDataSubId);
        objects[1] = over;

        objects[2] = StringFormat.getUnitStringByValue(Math.abs(commonLeft) * Constant.MB, 1);
        Log.d("", "left flow " + objects[2]);

        return objects;
    }

    /**
     *判断流量节省程序是否开启，如果开启，则返回true,否则返回false
     **/
    public static  boolean checkTrafficSaveOpened(Context context){
        NetworkPolicyManager mPolicyManager = NetworkPolicyManager.from(context);
        return mPolicyManager.getRestrictBackground();
    }

    /**
     *检测可直接清理的垃圾大小
     * * 接口待完善
     **/
    public static void checkRubbishCleanedDirectly(Context context){
        Intent intentCache = new Intent();
        intentCache.setComponent(new ComponentName(context.getPackageName(),
                "com.cydroid.systemmanager.rubbishcleaner.service.RubbishScanService"));
        intentCache.putExtra("startBySystemCheck", true);
        ServiceUtil.startForegroundService(context,intentCache);
    }

    /**
     *检测是否有超过3个月不使用的应用, 有返回true,没有返回false
     **/
    public static  boolean checkNotFrequentlyUsedApps(UninstallAppManager mUninstallAppManager){
        long DAY_TIME = 24 * 60 * 60 * 1000;
        long ONE_MONTH = DAY_TIME * 30;
        List<UninstallAppInfo> uninstallApps = mUninstallAppManager.getAllUninstallAppsByShowType(2);
        for (UninstallAppInfo info : uninstallApps){
            if (info.getUseFrequency() >= ONE_MONTH){
                return true;
            }
        }
        return false;
    }

    /**
     *检测高耗电是否开启，如果开启，则返回true,否则返回false
     **/
    public static  boolean checkHighPowerConsumption(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("power_consume_key", true);
    }

    /**
     *检测Ram/CPU监控是否开启，如果开启，则返回true,否则返回false
     **/
    public static  boolean checkRamAndCPUMonitor(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("cpu_overload_monitor_key", true);
    }

    /**
     *检测自适应电池是否开启，如果开启，则返回true,否则返回false
     **/
    public static  boolean checkAdaptiveBattery(Context context){
        return false;
    }

    /**
     *检测自动调节屏幕亮度是否开启，如果开启，则返回true,否则返回false
     **/
    public static  boolean checkAutomaticScreenBrightness(Context context){
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    /**
     *关闭WLAN
     **/
    public static void closeWlan(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     *关闭蓝牙
     **/
    public static void closeBlueTooth(Context context){
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (blueadapter != null){
            blueadapter.disable();
        }
    }

    /**
     *关闭GPS
     **/
    public static void closeGPS(Context context){
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, android.provider.Settings.Secure.LOCATION_MODE_OFF);
    }

    /**
     *关闭GPS
     **/
    public static void closeGesture(Context context){
        CyeeSettings.putInt(context.getContentResolver(), CyeeSettings.GN_SSG_SWITCH, 0);
    }

    /**
     * 设置休眠时间30s
     */
    public static void setScreenOffTime(Context context) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30 * 1000);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    /**
     * 打开屏幕省电开关
     */
    public static void setScreenPowerSave(Context context) {
        MiraVisionJni.setAALFunction(MiraVisionJni.getDefaultAALFunction());

    }

    /**
     * 剩余空间不足10%，跳转到深度清理
     */
    public static void startDeeplyCleanForResult(Activity context, int requestCode) {
        Intent intent = new Intent();
        try {
            intent.setComponent(new ComponentName(context.getPackageName(),
                    "com.cydroid.systemmanager.rubbishcleaner.DeeplyCleanerMainActivity"));
            context.startActivityForResult(intent, requestCode);
        } catch (Exception localException) {

        }
    }

    /**
     * 可直接清理的垃圾，跳转到垃圾清理
     */
    public static void startRubbishCleanForResult(Activity context, int requestCode) {
        Intent intent = new Intent();
        try {
            intent.setAction("com.cydroid.rubbishcleaner");
            intent.addCategory("com.cydroid.rubbishcleaner.category");
            context.startActivityForResult(intent, requestCode);
        } catch (Exception localException) {

        }
    }

    /**
     * 打开锁屏清理开关
     */
    public static void setCleanOnLockScreen(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("screenoff_clean_key", true);
        editor.commit();
    }

    /**
     * 打开智能睡眠提醒开关
     */
    public static void setIntelligentSleep(Context context) {
        SharedPreferences pref = context.getSharedPreferences("powermanager_setting", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Consts.NIGHT_MODE, true);
        editor.commit();
    }

    /**
     * 关闭热点
     */
    public static void closeHotpot(Context context) {
        TrafficassistantUtil.setWifiApEnabled(context,false);
    }

    /**
     * 跳转到流量管理界面，设置流量套餐
     */
    public static void startSetTrafficSet(Activity context, int requestCode){
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int defaultDataSubId = simInfo.getSimIndex_CurrentNetworkActivated();
        if (defaultDataSubId == -1){
            defaultDataSubId = simInfo.getInsertedSimInfo().get(0).mSlot;
        }
        Intent intent = new Intent(context, TrafficLimitActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.SIM_VALUE, defaultDataSubId);
        bundle.putBoolean("fromExam", true);
        intent.putExtras(bundle);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 剩余流量不足５M或超出，跳转到控制应用联网界面
     */
    public static void startInternetControlActivity(Activity context){
        Intent intent = new Intent(context, TrafficNetworkControlActivity.class);
        context.startActivity(intent);
    }

    /**
     * 打开流量节省程序的开关
     */
    public static void setTrafficSaveOpened(Context context){
        NetworkPolicyManager mPolicyManager = NetworkPolicyManager.from(context);
        mPolicyManager.setRestrictBackground(true);
    }


    /**
     * 开启亮度自动调节
     * @param context
     */
    public static void setAutoBrightness(Context context) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     *跳转到应用卸载界面，卸载超过3个月不使用的应用
     **/
    public static  void uninstallNotFrequentlyUsedApps(Activity context){
        Intent intent = new Intent();
        intent.putExtra("monitor", 2);
        intent.putExtra("sortByUser", false);
        intent.setClass(context, UninstallAppActivity.class);
        context.startActivity(intent);
    }

    /**
     *开启高耗电提醒
     **/
    public static  void setHighPowerConsumption(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("power_consume_key", true);
        editor.commit();
    }

    /**
     *开启Ram/CPU监控
     **/
    public static  void setRamAndCPUMonitor(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("cpu_overload_monitor_key", true);
        editor.commit();
    }

    /**
     *检测自适应电池是否开启，如果开启，则返回true,否则返回false
     * * 接口待完善
     **/
    public static  void setAdaptiveBattery(Context context){

    }
}
