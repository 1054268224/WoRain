package com.cydroid.softmanager.powersaver.utils;

public class PowerConsts {
    public static final String POWER_MODE = "cyee_powermode";
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    public static final String POWER_MODE_STACK = "cyee_powermode_stack";
    public static final String SUPER_MODE_EXCEPTION_REBOOT = "cyee_supermode_exception";
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
    public static final int NONE_MODE = 0;
    public static final int NORMAL_MODE = 1;
    public static final int SUPER_MODE = 2;
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    public static final int NIGHT_MODE = 3;
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
    public static final int CHECK_CONSUMPTION_DETAILS = SUPER_MODE + 1;

    public static final String GREEN_BACKGROUND_KEY = "state_green_b";
    public static final String SYSTEM_ANIM_KEY = "state_sys_anim";
    public static final String DARK_THEME_KEY = "state_dark_theme";
    public static final String DARK_WALLPAPER_KEY = "state_dark_wallpaper";
    public static final String WIFI_KEY = "state_wifi";
    public static final String WIFI_AP_KEY = "state_wifi_ap";

    public static final String MTK_VIBRATION_PROFILE = "state_mtk_vibration_profile";

    public static final String BLUETOOTH_KEY = "state_bluetooth";
    public static final String DATA_KEY = "state_dataconnection";
    public static final String GPS_KEY = "state_gps";
    public static final String SYNC_KEY = "state_sync";
    public static final String PUSH_KEY = "state_push";
    public static final String BRIGHTNESS_KEY = "state_brightness";
    public static final String BRIGHTNESS_MODE_KEY = "state_brightness_mode";
    public static final String TIMEOUT_KEY = "state_timeout";
    public static final String CPUCORE_KEY = "state_cpu";
    public static final String SCREEN_SAVER_KEY = "state_screen_saver";
    public static final String GESTRUES_KEY = "state_gestrues";
    public static final String SMART_GESTRUES_KEY = "state_smart_gestrues";
    public static final String HOTKNOT_KEY = "state_hotknot";
    public static final String SCREENOFF_GESTRUES_KEY = "screenoff_gestures";
    public static final String DOUBLECLICK_GESTRUES_KEY = "doubleclick_gestures";
    public static final String TOUCHLESS_GESTRUES_KEY = "state_touchless_gestrues";
    public static final String WINDOW_ANIM_KEY = "state_window_anim";
    public static final String TRANS_ANIM_KEY = "state_trans_anim";
    public static final String DURATION_ANIM_KEY = "state_duration_anim";
    // Gionee <yangxinruo> <2015-11-17> add for CR01592358 begin
    public static final String BATTERY_STYLE = "state_battery_style";
    // Gionee <yangxinruo> <2015-11-17> add for CR01592358 end
    // Gionee <yangxinruo> <2016-3-28> add for CR01661325 begin
    public static final String HOME_KEY_VALUE = "home_key_value";
    // Gionee <yangxinruo> <2016-3-28> add for CR01661325 end
    public static final String SYSTEM_NOTI_RECEIVERS = "system_noti_receivers";
    // Gionee <yangxinruo> <2016-2-26> add for CR01634882 begin
    public static final String WIFI_SLEEP_POLICY = "wifi_sleep_policy";
    // Gionee <yangxinruo> <2016-2-26> add for CR01634882 end
    // Gionee <yangxinruo> <2015-08-11> delete for CR01535702 begin
    // 为保证"铃音"正常使用,恢复STREAM_MUSIC,CR01529091由故事锁屏处理
    // Gionee <yangxinruo> <2015-07-30> add for CR01529091 begin
    // public static final String MUSIC_MUTE_KEY = "state_music_mute";
    // Gionee <yangxinruo> <2015-07-30> add for CR01529091 end
    // Gionee <yangxinruo> <2015-08-11> delete for CR01535702 end
    public static final String NONE_POWER = "none_";
    public static final String NORMAL_POWER = "normal_";
    public static final String SUPER_POWER = "super_";

    // 日常省电三个配置项的key，切换模式时保存，恢复状态时使用
    public static final String CHECKBOX_GREENBACKGROUND = "select_green_bg";
    public static final String CHECKBOX_SYSTEMANIM = "select_sys_anim";
    public static final String CHECKBOX_DARK_THEME = "select_dark_theme";
    // 发送此广播，触发省电模式切换
    public static final String MODE_CHANGE_INTENT = "cydroid.action.powermanager.changemode";
    // 在日常省电模式下，重新配置，保存后执行新的日常省电模式

    public static final String IS_USING_LIVEPAPER = "is_use_livepaper";
    public static final String LIVE_WALLPAPER_CLASSNAME = "live_paper_classname";
    public static final String LIVE_WALLPAPER_PKGNAME = "live_paper_pkgname";

    // 语音控制
    public static final String SOUND_CONTROL_SWITCH = "sound_control_switch";
    public static final String SOUND_CALLING_CONTROL = "sound_calling_control";
    public static final String SOUND_MESSAGE_CONTROL = "sound_msg_control";

    // NFC
    public static final String IS_NFC_CLOSE = "is_nfc_close";

    // FLOAT_TOUCH
    public static final String FLOAT_TOUCH_SEITCH = "float_touch_switch";
    // Gionee <yangxinruo> <2015-08-13> add for CR01537130 begin
    public static final String POWER_MODE_PROCESSING = "cyee_powermode_processing";
    // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
    public static final int SUPER_MODE_PROCESSING = 0;
    public static final int SUPER_MODE_DONE = 1;
    // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end
    // Gionee <yangxinruo> <2015-08-13> add for CR01537130 end
    // Gionee <yangxinruo> <2015-09-17> add for CR01549816 begin
    public static final String CURRENT_SHOULD_IN_DARKTHEME = "current_in_darktheme";
    // Gionee <yangxinruo> <2015-09-17> add for CR01549816 end

    // Gionee <yangxinruo> <2015-11-5> add for CR01581102 begin
    public static final String CURRENT_IN_GREENBACKGROUND = "current_in_greenbackground";
    // Gionee <yangxinruo> <2015-11-5> add for CR01581102 end
}
