package com.cydroid.softmanager.common;

import android.net.Uri;
import android.os.SystemProperties;

public class Consts {
    /* 新增&替换部分 start */
    public static final String SYSTEMMANAGER = "SystemManager/";
    public static final String MODE = "mode_"; // instead of QUICK_SAVING
    public static final String SAVING_ATNIGHT = "night_";
    /*
     * public static final int EVENT_SCREEN_ON_FOR_POWERSAVATGINNIGHT = 9;
     * public static final int EVENT_SCREEN_OFF_FOR_POWERSAVATGINNIGHT = 10;
     */
    public static final int EVENT_START_POWERSAVING = 5;
    public static final int EVENT_SHUTDOWN_POWERSAVING = 6;
    /* 新增&替换部分 end */

    // SharePreferences key name
    public static final String WIFI_KEY = "wifi";
    public static final String BLUETOOTH_KEY = "bluetooth";
    public static final String DATA_CONNECTION_KEY = "data_connect";
    public static final String GPS_KEY = "gps";
    public static final String SYNC_KEY = "sync";
    public static final String PUSH_NOTIFICATION_KEY = "push";
    public static final String SCREEN_LIGHT_KEY = "light";
    public static final String SCREEN_OFF_TIME_KEY = "timeout";
    public static final String AIRPLANE_MODE = "airplane";
    public static final String ALIGN_ALARM = "key_alarm";
    // for Android5.0
    public static final String DATA_CONNECTION_KEY5 = "data_conn";

    /* 进入灭屏省电的标志位 */
    public static final String FLAG_OF_SCREEN_OFF_POWER = "screen_off_power";

    public static final String ACTION_START_DIALOGACTIVITY = "cydroid.powermanager.intent.action.STARTACTIVITY";

    public static final String POWER_SAVING_WIDGET_ENABLE = "powersaver_widget_enable";
    public static final String ACTION_POWER_OBSERVER_CHANGE = "gionee.intent.action.OBSERVER_CHANGE";
    public static final String ACTION_BATTERY_CHANGE = "gionee.intent.action.BATTERY_CHANGE";

    public static final String ACTION_KILL_PROCESS = "gionee.intent.action.KILL_PROCESS";
    public static final String ACTION_SUPERMODE_OPEN = "cydroid.intent.action.SUPERMODE_OPEN";
    public static final String ACTION_AUTO_CLEAN_MEM = "cydroid.intent.action.AUTO_CLEAN_MEM";
    public static final String ACTION_LOW_MEM_CLEAN = "cydroid.intent.action.LOW_MEM_CLEAN";

    // 绿色后台清理广播sensitize
    public final static String ACTION_GREEN_BACKGROUND_CLEAN = "cydroid.intent.action.GREEN_BACKGROUND_CLEAN";

    public static final Uri ROSTER_CONTENT_URI = Uri.parse("content://com.cyee.settings.RosterProvider/rosters");

    // Gionee <xuhz> <2013-10-18> add for CR00924714 begin
    // 用户使用2小时后（插卡），提示其切换到自动背光以节省功耗。(仅提醒一次)
    // 如果已经提醒过则不再提醒，如果用户修改过屏幕亮度也不再提醒
    // 对应值为 false，不提醒；为true，提醒。
    // public static final String CHANGE_BRIGHTNESS_MODE =
    // "change_brightness_mode";

    // 电量低于或等于30%，提醒用户开启省电模式 (仅提醒一次)
    // 如果已经提醒过则不再提醒，如果正在充电或者电量高于30%不再提醒
    // 对应值为 false，不提醒；为true，提醒。
    public static final String LOW_POWER_NOTIFICATION = "low_power_notification";

    public static final String AUTO_CLEAN = "key_auto_clean";
    public static final String NIGHT_MODE = "key_night_mode";
    // Gionee <xuhz> <2013-10-18> add for CR00924714 end

    public static final String CPU_FREQUENCY = "cpu";
    public static final String POWERSAVER_MODE = "powersaver_mode";
    public static final String PRE_POWERSAVER_MODE = "pre_powersaver_mode";
    public static final int POWERSAVER_DEFAULT_MODE = 0;
    public static final int POWERSAVER_ULTIMATE_MODE = 1;
    public static final int POWERSAVER_CUSTOM_MODE = 2;
    public static final int POWERSAVER_MODE_NUMBER = 3;

    public static final String OFF_SCREEN_CLEAN = "off_screen_clean";
    public static final String LOW_MEMORY_CLEAN = "low_memory_clean";
    public static final String RUN_GAME_MEMORY_CLEAN = "run_game_memory_clean";
    public static final String RUN_GAME_MEMORY_CLEAN_STATUS = "game_memory_clean_status";
    public static final int LOW_MEMORY_MAX_LEVLE = 80;
    public static final String RESULT_NOTE = "clean_result_note";

    public static final String SYSTEMUI_CLEAN_TYPE = "systemui";
    public static final String LOW_MEMORY_CLEAN_TYPE = "low_memory_clean";
    public static final String OFF_SCREEN_CLEAN_TYPE = "off_screen_clean";

    public static final String CLEAN_TYPE = "clean_type";
    public static final String CLEAN_MEM_NUMBER = "clean_mem_number";

    public static final String NIGHT_AUTO_POWER_SAVER = "night_auto_power_saver";

    public final static String ONE_CLEAN_FROM_SYSTEM = "com.cydroid.systemui.action.ONE_CLEAN";

    public static final String DEFAULT_PREFERENCES_NAME = "com.cydroid.softmanager_preferences";
    // ##################Android5.0省电新规划修改 start ###################
    public static final int NONE_MODE = 0;
    public static final int NORMAL_MODE = 1;
    public static final int SUPER_MODE = 2;
    // ##################Android5.0省电新规划修改 end ###################

    public static final String DAILY_POWER_ELEMENTS = "daily_power_elements";
    public static final int Element_GREEN_BG = 1;
    public static final int Element_DARK_THEME = 2;

    public static final boolean IS_TELECOM_PROJECT = "ctcc".equals(SystemProperties.get("ro.gn.custom.operators", ""));

    // #################### Custom flag begin ####################
    public static final String OVERSEA_CUSTOM = SystemProperties.get("ro.cy.custom", "unknown");
    public static final boolean gnSwFlag = OVERSEA_CUSTOM.equals("EUROPE_WEIMEI");
    public static final boolean gnZcFlag = OVERSEA_CUSTOM.equals("SOUTH_AMERICA_AZUMI");
    //Gionee guoxt 2015-03-04 modified for CR01449811 begin
    public static final boolean gnVFflag = OVERSEA_CUSTOM.equals("VISUALFAN");
    //Gionee guoxt 2015-03-04 modified for CR01449811 end
    //Gionee <GN_Oversea_Req> <lucy> <20170424> add for 123274 begin
    public static final boolean gnQMflag = OVERSEA_CUSTOM.equals("PAKISTAN_QMOBILE");
    //Gionee <GN_Oversea_Req> <lucy> <20170424> add for 123274 end
    //Gionee guoxt 2015-03-04 modified for CR01449811 begin
    public static final boolean gnTCflag = OVERSEA_CUSTOM.equals("Casper");
    //Gionee guoxt 2015-03-04 modified for CR01449811 end
    public static final boolean gnKRFlag = OVERSEA_CUSTOM.equals("KOREA_BOE");
    public static final boolean gnIPFlag = OVERSEA_CUSTOM.equals("ISRAEL_PELEPHONE");
    //guoxt modify for 244215 begin
    public static final boolean gnVMFlag = OVERSEA_CUSTOM.equals("VIETNAM_MOBIISTAR");
    //guoxt modify for 244215 end
    public static final boolean gnSyFlag = OVERSEA_CUSTOM.equals("BANGLADESH_SYMPHONY");
    public static final boolean gnGIFlag = OVERSEA_CUSTOM.equals("INDIA_GIONEE");
    /*guoxt modify for 78574  begin*/
    public static final boolean gnGTFlag = OVERSEA_CUSTOM.equals("TAIWAN_GPLUS");
    /*guoxt modify for 78574  end*/
    // Gionee xionghg 2017-06-20 add for 160257 begin
    public static final boolean gnDPFlag = OVERSEA_CUSTOM.equals("ECUADOR_DOPPIO");
    // Gionee xionghg 2017-06-20 add for 160257 end
    // Chenyee <CY_Oversea_Req> xionghg 20171012 add for 234035 begin
    public static final boolean gnBQFlag = OVERSEA_CUSTOM.equals("RUSSIA_BQ");
    // Chenyee <CY_Oversea_Req> xionghg 20171012 add for 234035 end
    // Chenyee <CY_Oversea_Req> xionghg 20180227 add for SW17W16TL-18 begin
    public static final boolean gnTLFlag = OVERSEA_CUSTOM.equals("ZIMBABWE_GTEL");
    // Chenyee <CY_Oversea_Req> xionghg 20180227 add for SW17W16TL-18 end
    //Gionee yubo 2015-07-28 add for CR01527980 begin
    private static final boolean gnMSflag = OVERSEA_CUSTOM.equals("GERMANY_MOBISTEL");
    //Gionee yubo 2015-07-28 add for CR01527980 end

    //Gionee yubo 2015-07-28 add for CR01527980 begin
    public static final boolean cyACflag = OVERSEA_CUSTOM.equals("ALGERIA_CONDOR");
    //Gionee yubo 2015-07-28 add for CR01527980 end

    // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 begin
    public static final boolean cyEIFlag = OVERSEA_CUSTOM.equals("INDONESIA_LUNA");
    // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 end

    // Chenyee <guoxt> <2018-06-26> add for CSW1707ST-71 begin
    public static final boolean cySTFlag = OVERSEA_CUSTOM.equals("COMTRADE_DISTRIBUTION");
    // Chenyee <guoxt> <2018-06-26> add for CSW1707ST-71 end

    public static final boolean cyCXFlag = OVERSEA_CUSTOM.equals("XiaoLaJiao");

    // #################### Custom flag end  ####################

    public static final String gnCustom = SystemProperties.get("ro.cy.common.mainboard.prop");
    /*guoxt modify for CR01758686 begin*/
    public static final boolean gnSimOneFlag = "no".equals(SystemProperties.get("ro.gn.gemini.support", "yes"));
    /*guoxt modify for  CR01758686 end*/

    // Gionee <houjie> <2015-08-19> add for CR01559020 begin
    public static final int CPU_OVERLOAD = 1;
    public static final int MEM_OVERLOAD = 2;
    // Gionee <houjie> <2015-08-19> add for CR01559020 end
    // Gionee xionghg add for power saving optimization begin
    public static final boolean SUPPORT_NEW_LAUNCHER = SystemProperties.get("ro.cy.power.saving.optimize", "no").equals("yes");
    // Gionee xionghg add for power saving optimization end

    // Gionee guoxt 2017-06-20 add for 160257 begin
    public static final boolean gnNoAnti = SystemProperties.get("ro.cy.anti.virus.support", "no").equals("no");
    // Gionee guoxt 2017-06-20 add for 160257 end
    // Gionee guoxt 2017-06-20 add for 160257 begin
    public static final boolean cyBAFlag = SystemProperties.get("ro.cy.custom", "unknown").equals("SOUTH_AMERICA_BLU");
    // Gionee guoxt 2017-06-20 add for 160257 end
    // Chenyee <liu_shuang> <2017-12-25> add for SW17W16TC-139 begin
    public static final boolean cyODMFlag = SystemProperties.get("ro.cy.odm").equals("yes");
    // Chenyee <liu_shuang> <2017-12-25> add for SW17W16TC-139 end
    // Chenyee <liu_shuang> <2017-12-26> add for CSW1702A-931 begin
    public static final boolean cyGoFlag = SystemProperties.get("ro.cy.go.rom").equals("yes");
    // Chenyee <liu_shuang> <2017-12-26> add for CSW1702A-931 end

    // Chenyee <liu_shuang> <2018-03-01> add for CSW1703A-304 begin
    public static final boolean isNotchSupport = true;//SystemProperties.get("ro.cy.notch.support", "no").equals("yes");
    // Chenyee <liu_shuang> <2018-03-01> add for CSW1703A-304 begin
    // Chenyee <guoxt> <2018-05-05> add for CSW1703VF-53 begin
    public static final boolean cy1703VF = gnVFflag && gnCustom.equals("CSW1703");
    // Chenyee <guoxt> <2018-05-05> add for CSW1703VF-53 end
    //add by chenxuanyu start
    public static final boolean isWhistListSupport = false;
    //add by chenxuanyu end

}
