package com.wheatek;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class SystemInfoUtils {

    public static String getDeviceID(Context context) {
//        WifiManager wifiManager = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//        String macAddress = wifiManager.getConnectionInfo().getMacAddress();
//        //  @RequiresPermission(Manifest.permission.READ_PRIVILEGED_PHONE_STATE)
//        String serial = android.os.Build.getSerial();
//        String GPSID = tm.getDeviceId();
        String IMEI = tm.getImei();
//        //  @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
//        String softw = tm.getDeviceSoftwareVersion();
//        String tel = tm.getLine1Number();//获取本机号码
//        String imei = tm.getSimSerialNumber();//获得SIM卡的序号
//        String imsi = tm.getSubscriberId();//得到用户Id
//        getAppVersionName(context);
//        getSdkAPILevel();
//        getSdkVersion();
//        String finger = android.os.Build.FINGERPRINT;
//        String custinfo = getCustInfomation();
        return IMEI;
    }

    private static final String CUSTOM_BUILD_VERSION_PROPERTY
            = "ro.vendor.mediatek.version.release";
    private static final String CUSTOM_VERSION
            = "ro.build.custom_version";

    public static String getCustInfomation_VERSION() {
        return SystemProperties.get(CUSTOM_BUILD_VERSION_PROPERTY);

    }

    public static String getCustInfomation_NAME() {
        return SystemProperties.get(CUSTOM_VERSION);
    }

    /**
     * 返回当前程序版本名
     */
    public static PackageInfo getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 手机系统版本
     */
    public static String getSdkVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 手机系统API level
     */
    public static int getSdkAPILevel() {
        return android.os.Build.VERSION.SDK_INT;
    }


}
