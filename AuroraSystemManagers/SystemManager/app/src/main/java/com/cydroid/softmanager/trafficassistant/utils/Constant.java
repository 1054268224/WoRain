package com.cydroid.softmanager.trafficassistant.utils;

import android.net.Uri;

public class Constant {

    public static final int SIM_COUNT = 2;
    public static final int HAS_NO_SIMCARD = -1;
    public static final int SIM1 = 0;
    public static final int SIM2 = 1;

    public static final int LOADER_CHART_DATA = 2;
    public static final int LOADER_SUMMARY = 3;

    public static final String STRING_UNIT_TB = "T";
    public static final String STRING_UNIT_GB = "G";
    public static final String STRING_UNIT_MB = "M";
    public static final String STRING_UNIT_KB = "K";
    public static final String STRING_UNIT_B = "B";

    public static final String NOTI_STRING_UNIT_TB = "T";
    public static final String NOTI_STRING_UNIT_GB = "G";
    public static final String NOTI_STRING_UNIT_MB = "M";
    public static final String NOTI_STRING_UNIT_KB = "K";

    public static final int UNIT = 1024;
    public static final int KB = UNIT;
    public static final int MB = 1024 * KB;
    public static final long GB = 1024 * MB;
    public static final long TB = 1024 * GB;

    public static final int UNIT_B = 0;
    public static final int UNIT_KB = 1;
    public static final int UNIT_MB = 2;
    public static final int UNIT_GB = 3;
    public static final int UNIT_TB = 4;

    public static final String ONLY_CONCE_SWITCH = "only_once_switch";
    public static final int MOBILE= 0;
    public static final int WIFI = 1;
    public static final int NETWORK_NUM = 2;
    
    public static final String SIM_VALUE = "SIM";
    public static final String TRAFFIC_CYCLE = "weekly";
    // Gionee: mengdw <2016-06-01> modify for CR01711341 begin
    public static final String JINLI_VIDEO = "com.cydroid.video";
    public static final String YOUKU_VIDEO = "com.youku.phone.jinli";
    // Gionee: mengdw <2016-06-01> modify for CR01711341 end
    // Gionee: mengdw <2016-07-06> add for CR01639347 begin
    public static final String PACKAGE_NAME = "PackageName";
    public static final String NETWOEK_TYPE = "NetworkType";
    public static final String UPDATE_DISABLED_LIST = "disabledNetAppList";
    public static final String ACTION_NETWORK_DISABLED_LIST_UPDATE = "com.cydroid.softmanager.networkDisabled.Update";
    public static final String PERMISSION_RECEIVE_DISABLED_LIST_UPDATE = "com.cydroid.permission.revice.network.disabledlist.update";
    // Gionee: mengdw <2016-07-06> add for CR01639347 end
    // Gionee: mengdw <2016-08-22> modify for CR01750259 begin
    public static final String ANDROID_FM = "com.android.fmradio";
    // Gionee: mengdw <2016-08-22> modify for CR01750259 end
    // mengdw <2016-10-09> add for CR01766193 begin
    public static final String HOTSPOT_START_TYPE = "HotSpotStartType";
    public static final String  ACTION_HOTSPOT_SERVICE_START= "com.cydroid.softmanager.hotspotremind.serviceStart";
    public static final String  ACTION_HOTSPOT_SERVICE_STOP= "com.cydroid.softmanager.hotspotremind.serviceStop";
    public static final int TYPE_HOTSPOT_START_NORMAL = 0;
    public static final int TYPE_HOTSPOT_START_SYSTEM_MANAGER = 1;
    // mengdw <2016-10-09> add for CR01766193 end
  // Gionee: mengdw <2016-11-02> add for CR01639347 begin
    public static final int NO_NET_CONNECTED = -1;
    public static final String SOFT_MANGER_PACKAGE_NAME = "com.cydroid.softmanager";
    // Gionee: mengdw <2016-11-02> add for CR01639347 end
    // Gionee: mengdw <2016-12-13> add for CR01776232 begin
    public static final int NETWORK_CONTROL_ENABLE_STATUS = 0;
    public static final int NETWORK_CONTROL_DISABLE_STATUS = 1;
    public static final int SYSTEMID_CONSTANT = 10000;
    public static final int INVALID_UID = -1;
    // Gionee: mengdw <2016-12-13> add for CR01776232 end
    // Gionee: mengdw <2016-11-24> add for CR01772354 begin
    // Gionee: mengdw <2016-03-21> modify for CR01649623 begin
    public static final int REQUEST_CODE_CONTACT = 1;
    public static final String GN_CATEGORY = "android.intent.category.GIONEE";
    public static final String AUTHORITY = "com.android.contacts";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "data");
    public static final String CONTACTS_ID = "Data._ID";
    public static final String PHONE_NUMBER = "data1";
    public static final int BIND_DISPLAY_NAME_INDEX = 1;
    public static final int BIND_NUMBER_INDEX = 2;
    public static final String[] ADD_PROJECTION = {"_id", "display_name", "data1"};
    // Gionee: mengdw <2016-03-21> modify for CR01649623 end
    // Gionee: mengdw <2016-11-24> add for CR01772354 begin
    public static final int TRAFFIC_BUY_OPERA_ALIPAY_TYPE = 0;
    public static final int TRAFFIC_BUY_OPERA_WEIXIN_PAY_TYPE = 1;
    public static final String KEY_PAY_INFO = "payInfo";
    public static final String KEY_SUPPLLER_ID = "supplierID";
    public static final String OPERA_DEFAULT_TEL = "400-670-7917";
    public static final int SUPPLIER_TEDDY = 1;
    public static final int SUPPLIER_OPERA = 2;
    public static final String KEY_SERVICE_TEL = "serviceTel";
    public static final String KEY_UPLOAD_URL = "uploadUrl";
    // Gionee: mengdw <2016-11-24> add for CR01772354 end
    public static final String KEY_LOCKSCREEN_TRAFFIC = "lockScreenTraffic";
}