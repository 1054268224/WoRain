package com.cydroid.softmanager.systemcheck;

import android.content.Context;
import android.os.Bundle;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.model.ItemInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhaocaili on 18-7-24.
 */

public class SystemCheckItem {
    private static final String TAG = "SystemCheckItem";
    /***************************清理加速项***************************/
    public static final int CHECK_CLOSING_BACKGROUND_PROGRESS = 0;//0
    public static final int CHECK_CLOSED_BACKGROUND_PROCESS = CHECK_CLOSING_BACKGROUND_PROGRESS + 1;//1
    public static final int CHECK_RELEASE_SYSTEM_MEMORY = CHECK_CLOSED_BACKGROUND_PROCESS + 1;//2
    public static final int CHECK_SYSTEM_MEMORY_PERCENT = CHECK_RELEASE_SYSTEM_MEMORY + 1;//3
    public static final int CHECK_IF_CLEANNING_ON_LOCKSCREEN_OPENED = CHECK_SYSTEM_MEMORY_PERCENT + 1;//4
    public static final int CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY = CHECK_IF_CLEANNING_ON_LOCKSCREEN_OPENED + 1;//5
    public static final int CHECK_REMAINING_SPACE = CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY + 1;//6
    public static final int CHECK_NOT_FREQUENTLY_USED_APPS = CHECK_REMAINING_SPACE + 1;//7

    /***************************省电管理项***************************/
    public static final int CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED = CHECK_NOT_FREQUENTLY_USED_APPS + 1;//8
    public static final int CHECK_IF_RAM_CPU_MONITOR_OPENED = CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED + 1;//9
    public static final int CHECK_IF_INTELLIGENT_SLEEP_OPENED = CHECK_IF_RAM_CPU_MONITOR_OPENED + 1;//10
    public static final int CHECK_IF_SCREEN_POWER_SAVE_OPENED = CHECK_IF_INTELLIGENT_SLEEP_OPENED + 1;//11
    public static final int CHECK_IF_ADAPTIVE_BATTERY_OPENED = CHECK_IF_SCREEN_POWER_SAVE_OPENED + 1;//12
    public static final int CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED = CHECK_IF_ADAPTIVE_BATTERY_OPENED + 1;//13
    public static final int CHECK_IF_WLAN_ENABLED = CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED + 1;//14
    public static final int CHECK_IF_BLUETOOTH_OPENED = CHECK_IF_WLAN_ENABLED + 1;//15
    public static final int CHECK_IF_HOT_POT_OPENED = CHECK_IF_BLUETOOTH_OPENED + 1;//16
    public static final int CHECK_IF_GESTURE_OPENED = CHECK_IF_HOT_POT_OPENED + 1;//17
    public static final int CHECK_IF_GPS_OPENED = CHECK_IF_GESTURE_OPENED + 1;//18
    public static final int CHECK_IF_SCREEN_SLEEP_TIME = CHECK_IF_GPS_OPENED + 1;//19

    /***************************流量监控项***************************/
    public static final int CHECK_IF_SIM_CARD_INSERTED = CHECK_IF_SCREEN_SLEEP_TIME + 1;//20
    public static final int CHECK_IF_SET_TRAFFIC_SET = CHECK_IF_SIM_CARD_INSERTED + 1;//21
    public static final int CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH = CHECK_IF_SET_TRAFFIC_SET + 1;//22
    public static final int CHECK_IF_TRAFFIC_SAVE_OPENED = CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH + 1;//23
    public static final int CHECK_OVER = CHECK_IF_TRAFFIC_SAVE_OPENED + 1;//24

    public static final  int CHECK_ALLCOUNT=CHECK_OVER;

    /**
     * 清理加速列表
     */
    public static ArrayList<Bundle> mSpeedupList = new ArrayList<>();
    /**
     * 省电管理列表
     */
    public static ArrayList<Bundle> mPowerManagerList = new ArrayList<>();
    /**
     * 流量管理列表
     */
    public static ArrayList<Bundle> mTrafficAssistentList = new ArrayList<>();
    /**
     * 待优化列表
     */
    public static ArrayList<Bundle> mToBeOptimizeList = new ArrayList<>();
    /**
     * 系统检测列表
     */
    public static ArrayList<Bundle> mSystemCheckList = new ArrayList<>();
    /**
     * 减分列表
     */
    public static HashMap<Integer, Integer> mSubtractionList = new HashMap<>();
    /**
     * 正在运行的应用列表
     */
    public static List<ItemInfo> mRunningProcessList = new ArrayList<>();







    /*********************************清理加速*开始*******************************************/
    /**
     * 正在关闭后台进程，因为关闭时间较久，加一个正在关闭的项，作为提示
     * @param context
     */
    public static void addClosingBackgroundProcessItem(Context context){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.system_check_speedup_category));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, false);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.closing_back_apps));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_CLOSING_BACKGROUND_PROGRESS);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
        addToSystemCheckList(bundle);
    }

    /**
     * 已关闭后台进程
     * @param context
     * @param totalProcess　关闭后台进程的个数
     */
    public static void addCloseBackgroundProcessItem(Context context, int totalProcess){
        String contentTitle;
        if (totalProcess != 0){
            contentTitle = String.format(getString(context, R.string.closed_back_apps), totalProcess);
        }else {
            contentTitle = getString(context, R.string.closed_0_back_apps);
        }
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.system_check_speedup_category));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, false);
        bundle.putString(BUNDLE_KEY.contentTitle, contentTitle);
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_CLOSED_BACKGROUND_PROCESS);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
        updateFirstCheckItem(bundle);
        addToSpeedupList(bundle);
    }

    /**
     * 已释放系统内存
     * @param context
     * @param totalPss　关闭后台进程所释放的系统内存
     */
    public static void addReleaseSystemMemoryItem(Context context, String totalPss){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.system_check_speedup_category));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, false);
        bundle.putString(BUNDLE_KEY.contentTitle, String.format(getString(context, R.string.released_memory), totalPss));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_RELEASE_SYSTEM_MEMORY);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
        addToSystemCheckList(bundle);
        addToSpeedupList(bundle);
    }

    /**
     * 检查系统内存占比
     * @param context
     * @param hightUsed　内存使用占比较多　true; 内存使用占比较少　false
     */
    public static void addCheckSystemMemoryPercentItem(Context context, boolean hightUsed){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.system_check_speedup_category));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, hightUsed);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, hightUsed? R.string.used_memory_much : R.string.used_memory_less));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_SYSTEM_MEMORY_PERCENT);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !hightUsed);
        bundle.putString(BUNDLE_KEY.btnText, null);
        addToSystemCheckList(bundle);
        addToSpeedupList(bundle);
        if (hightUsed){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 剩余空间
     * @param context
     * @param space　剩余空间
     * @param notEnough　不足10% true; 足够 false
     */
    public static void addCheckRemainingSpace(Context context, boolean notEnough, String space){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.system_check_speedup_category));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, notEnough);
        bundle.putString(BUNDLE_KEY.contentTitle, notEnough ?
                    getString(context, R.string.remaining_space_not_enough):
                    String.format(getString(context, R.string.remaining_space), space));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_REMAINING_SPACE);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !notEnough);
        bundle.putString(BUNDLE_KEY.btnText, notEnough? getString(context, R.string.system_check_btn_clean) : null);
        addToSystemCheckList(bundle);
        addToSpeedupList(bundle);
        if (notEnough){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 锁屏清理
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckCleanOnLockScreen(Context context, boolean enable){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.system_check_speedup_category));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enable);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.screenoff_clean_setting_title)
                + "  " + getString(context, enable? R.string.status_opened : R.string.status_not_opened));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_CLEANNING_ON_LOCKSCREEN_OPENED);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enable);
        bundle.putString(BUNDLE_KEY.btnText, enable? null : getString(context, R.string.mode_item_switch_on));
        addToSystemCheckList(bundle);
        addToSpeedupList(bundle);
        if (!enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 可直接清理的垃圾文件
     * @param context
     * @param large 垃圾大于100M true; 垃圾小于100M false
     */
    public static void addCheckRubbishCleanedDirectly(Context context, boolean large){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.system_check_speedup_category));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, large);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, large? R.string.a_lot_of_rubbish : R.string.a_little_rubbish));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !large);
        bundle.putString(BUNDLE_KEY.btnText, large? getString(context, R.string.system_check_btn_clean) : null);
        addToSystemCheckList(bundle);
        addToSpeedupList(bundle);
        if (large){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 有超过３个月不使用的app
     * @param context
     * @param notFrequentlyUsed 不常用 true; 垃圾小于100M false
     */
    public static void addCheckNotFrequentlyUsedApps(Context context, boolean notFrequentlyUsed){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.system_check_speedup_category));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, notFrequentlyUsed);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, notFrequentlyUsed? R.string.uninstall_not_frequently_used_apps : R.string.app_used_normally));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_NOT_FREQUENTLY_USED_APPS);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !notFrequentlyUsed);
        bundle.putString(BUNDLE_KEY.btnText, getString(context, R.string.uninstall));
        addToSystemCheckList(bundle);
        addToSpeedupList(bundle);
        if (notFrequentlyUsed){
            addToOptimizeList(bundle);
        }
    }
    /*********************************清理加速*结束*******************************************/

    /*********************************省电管理*开始*******************************************/
    /**
     * 智能睡眠提醒
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckIntelligentSleep(Context context, boolean enable){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enable);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.night_saving_title)
                + "  " + getString(context, enable? R.string.status_opened : R.string.status_not_opened));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_INTELLIGENT_SLEEP_OPENED);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enable);
        bundle.putString(BUNDLE_KEY.btnText, enable? null : getString(context, R.string.mode_item_switch_on));
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (!enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 屏幕省电
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckScreenPowerSave(Context context, boolean enable){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enable);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.screen_power_save)
                + "  " + getString(context, enable? R.string.status_opened : R.string.status_not_opened));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_SCREEN_POWER_SAVE_OPENED);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enable);
        bundle.putString(BUNDLE_KEY.btnText, enable? null : getString(context, R.string.mode_item_switch_on));
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (!enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * WLAN开启但未连接网络
     * @param context
     * @param disconnect 开启但未连接设备 true; 关闭或已开启但连接了设备 false
     */
    public static void addCheckIfWlanEnableItem(Context context, boolean disconnect){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, disconnect);
        bundle.putString(BUNDLE_KEY.contentTitle, disconnect?
                        String.format(getString(context, R.string.device_status_abnormal), getString(context, R.string.power_wifi_new)) + getString(context, R.string.close_for_saving_power) :
                        String.format(getString(context, R.string.device_status_normal), getString(context, R.string.power_wifi_new)));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_WLAN_ENABLED);
        bundle.putString(BUNDLE_KEY.btnText, disconnect? getString(context, R.string.button_close) : null);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !disconnect);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (disconnect){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 蓝牙开启，但未连接设备
     * @param context
     * @param notInUse 开启但未连接设备 true; 关闭或已开启但连接了设备 false
     */
    public static void addCheckIfBluetoothOpenedItem(Context context, boolean notInUse){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, notInUse);
        bundle.putString(BUNDLE_KEY.contentTitle, notInUse?
                String.format(getString(context, R.string.device_status_abnormal), getString(context, R.string.power_bluetooth)) + getString(context, R.string.close_for_saving_power) :
                String.format(getString(context, R.string.device_status_normal), getString(context, R.string.power_bluetooth)));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_BLUETOOTH_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, notInUse? getString(context, R.string.button_close) : null);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !notInUse);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (notInUse){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 位置信息
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckIfGPSOpenedItem(Context context, boolean enable){
        String contentTitle;
        if (enable){
            contentTitle = getString(context, R.string.gps_status) + "  " + getString(context, R.string.status_opened) + "," + getString(context, R.string.close_for_saving_power);
        }else {
            contentTitle = getString(context, R.string.gps_status) + getString(context, R.string.green_summary_off);
        }
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, enable);
        bundle.putString(BUNDLE_KEY.contentTitle, contentTitle);
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_GPS_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, enable? getString(context, R.string.button_close) : null);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !enable);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 动作手势
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckIfGestureOpenedItem(Context context, boolean enable){
        String contentTitle;
        if (enable){
            contentTitle = getString(context, R.string.gesture_status) + "  " + getString(context, R.string.status_opened) + "," + getString(context, R.string.close_for_saving_power);
        }else {
            contentTitle = getString(context, R.string.gesture_status) + getString(context, R.string.green_summary_off);
        }
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, enable);
        bundle.putString(BUNDLE_KEY.contentTitle, contentTitle);
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_GESTURE_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, enable? getString(context, R.string.button_close) : null);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !enable);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 热点
     * @param context
     * @param disconnect 开启但未连接设备 true; 关闭或开启但连接了设备 false
     */
    public static void addCheckIfHotpotOpenedItem(Context context, boolean disconnect){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, disconnect);
        bundle.putString(BUNDLE_KEY.contentTitle, disconnect?
                getString(context, R.string.hot_pot_status_abnormal) + getString(context, R.string.close_for_saving_power):
                String.format(getString(context, R.string.device_status_normal), getString(context, R.string.hot_pot)));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_HOT_POT_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, disconnect? getString(context, R.string.button_close) : null);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !disconnect);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (disconnect){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 屏幕休眠
     * @param context
     * @param time 屏幕休眠时间
     * @param timeout 大于10分钟 true; 小于10分钟 false
     */
    public static void addCheckScreenSleepTimeItem(Context context, String time, boolean timeout){
        String contentTitle;
        if (timeout){
            contentTitle = String.format(getString(context, R.string.screen_sleep_time_abnormal), time);
        }else {
            contentTitle = getString(context, R.string.screen_sleep_time_normal);
        }
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, timeout);
        bundle.putString(BUNDLE_KEY.contentTitle, contentTitle);
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_SCREEN_SLEEP_TIME);
        bundle.putString(BUNDLE_KEY.btnText, timeout? getString(context, R.string.status_set) : null);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, !timeout);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (timeout){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 高耗电提醒
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckHighPowerConsumptionItem(Context context, boolean enable){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enable);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.power_consume_setting_title)
                + "  " + getString(context, enable? R.string.status_opened : R.string.status_not_opened));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, enable? null : getString(context, R.string.mode_item_switch_on));
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enable);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (!enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * Ram/CPU监控提醒
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckRamAndCPUItem(Context context, boolean enable){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enable);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.cpu_monitor_setting_title)
                + "  " + getString(context, enable? R.string.status_opened : R.string.status_not_opened));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_RAM_CPU_MONITOR_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, enable? null : getString(context, R.string.mode_item_switch_on));
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enable);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (!enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 自适应电池
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckAdaptiveBatteryItem(Context context, boolean enable){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enable);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.adaptive_battery)
                + "  " + getString(context, enable? R.string.status_opened : R.string.status_not_opened));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_ADAPTIVE_BATTERY_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, enable? null : getString(context, R.string.mode_item_switch_on));
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enable);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (!enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 自动调节屏幕亮度
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckAutomaticScreenBrightnessItem(Context context, boolean enable){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_savepower));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enable);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.automatic_brightness)
                + "  " + getString(context, enable? R.string.status_opened : R.string.status_not_opened));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, enable? null : getString(context, R.string.mode_item_switch_on));
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enable);
        addToSystemCheckList(bundle);
        addToPowerManagerList(bundle);
        if (!enable){
            addToOptimizeList(bundle);
        }
    }
    /*********************************省电管理*结束*******************************************/

    /*********************************流量监控*开始*******************************************/
    /**
     * 设置流量套餐
     * @param context
     * @param set 已设置 true; 未设置 false
     */
    public static void addCheckIfHasSetTrafficSetItem(Context context, boolean set){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_traffic));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !set);
        bundle.putString(BUNDLE_KEY.contentTitle, set? null : getString(context, R.string.noti_no_settings));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_SET_TRAFFIC_SET);
        bundle.putString(BUNDLE_KEY.btnText, set?  null: getString(context, R.string.status_set));
        bundle.putBoolean(BUNDLE_KEY.checkStatus, set);
        addToSystemCheckList(bundle);
        addToTrafficAssistentList(bundle);
        if (!set){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 剩余流量
     * @param context
     * @param enough 大于5M true; 小于5M false
     */
    public static void addCheckFreeTrafficEnoughItem(Context context, boolean enough, boolean over, String freeTraffic){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_traffic));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enough);
        bundle.putString(BUNDLE_KEY.contentTitle, enough ? String.format(getString(context, R.string.free_traffic),freeTraffic) :
                over? String.format(getString(context, R.string.traffic_over_used),freeTraffic) + getString(context, R.string.network_control).toLowerCase() :
                        getString(context, R.string.free_traffic_too_less) + getString(context, R.string.network_control).toLowerCase());
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH);
        bundle.putString(BUNDLE_KEY.btnText, enough? null : getString(context, R.string.action_settings));
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enough);
        addToSystemCheckList(bundle);
        addToTrafficAssistentList(bundle);
        if (!enough){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 流量节省程序
     * @param context
     * @param enable 开启 true; 关闭 false
     */
    public static void addCheckIfTrafficSaveOpened(Context context, boolean enable){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_traffic));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !enable);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.traffic_save)
                + "  " + getString(context, enable? R.string.status_opened : R.string.status_not_opened));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_TRAFFIC_SAVE_OPENED);
        bundle.putString(BUNDLE_KEY.btnText, enable? null : getString(context, R.string.mode_item_switch_on));
        bundle.putBoolean(BUNDLE_KEY.checkStatus, enable);
        addToSystemCheckList(bundle);
        addToTrafficAssistentList(bundle);
        if (!enable){
            addToOptimizeList(bundle);
        }
    }

    /**
     * 未插sim卡
     * @param context
     */
    public static void addCheckIfSimInserted(Context context){
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY.category, getString(context, R.string.text_menu_traffic));
        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, false);
        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.traffic_no_simcard));
        bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_SIM_CARD_INSERTED);
        bundle.putString(BUNDLE_KEY.btnText, null);
        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
        addToSystemCheckList(bundle);
        addToTrafficAssistentList(bundle);
    }
    /*********************************流量监控*结束*******************************************/

    private static String getString(Context context, int id){
        return context.getResources().getString(id);
    }

    public static class BUNDLE_KEY {
        public static String category = "category";
        public static String isSuggestedItem = "isSuggestedItem";
        public static String contentTitle = "contentTitle";
        public static String checkItem = "checkItem";
        public static String btnText = "btnText";
        public static String checkStatus = "checkStatus";
    }

    private static void addToSystemCheckList(Bundle bundle){
        mSystemCheckList.add(bundle);
    }

    private static void addToOptimizeList(Bundle bundle){
        mToBeOptimizeList.add(bundle);
    }

    private static void addToTrafficAssistentList(Bundle bundle){
        mTrafficAssistentList.add(bundle);
    }

    private static void addToPowerManagerList(Bundle bundle){
        mPowerManagerList.add(bundle);
    }

    private static void addToSpeedupList(Bundle bundle){
        mSpeedupList.add(bundle);
    }

    private static void removeFromOptimizeList(int index){
        if (index >= 0 && index < mToBeOptimizeList.size()){
            mToBeOptimizeList.remove(index);
        }
    }

    private static void updateOptimizeList(Bundle bundle, int index){
        mToBeOptimizeList.set(index, bundle);
    }

    private static void updateTrafficAssistentList(Bundle bundle, int checkItem){
        for (int i = 0; i < mTrafficAssistentList.size(); i++){
            Bundle temp = mTrafficAssistentList.get(i);
            if (checkItem == temp.getInt(BUNDLE_KEY.checkItem)){
                mTrafficAssistentList.set(i, bundle);
            }
        }
    }

    private static void updatePowerManagerList(Bundle bundle, int checkItem){
        for (int i = 0; i < mPowerManagerList.size(); i++){
            Bundle temp = mPowerManagerList.get(i);
            if (checkItem == temp.getInt(BUNDLE_KEY.checkItem)){
                mPowerManagerList.set(i, bundle);
            }
        }
    }

    private static void updateSpeedupList(Bundle bundle, int checkItem){
        for (int i = 0; i < mSpeedupList.size(); i++){
            Bundle temp = mSpeedupList.get(i);
            if (checkItem == temp.getInt(BUNDLE_KEY.checkItem)){
                mSpeedupList.set(i, bundle);
            }
        }
    }

    private static void updateFirstCheckItem(Bundle bundle){
        if (mSystemCheckList.size() >= 1){
            mSystemCheckList.set(0, bundle);
        }
    }

    public static void updateAfterClickOptimizeList(Context context, int checkItem, Object[] objects){
        for (int i = 0; i < mToBeOptimizeList.size(); i++){
            Bundle bundle = mToBeOptimizeList.get(i);
            if (checkItem == bundle.getInt(BUNDLE_KEY.checkItem)){
                switch (checkItem){
                    case CHECK_IF_WLAN_ENABLED:
                        bundle.putString(BUNDLE_KEY.contentTitle, String.format(getString(context, R.string.device_status_normal),
                                getString(context, R.string.power_wifi_new)));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_BLUETOOTH_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, String.format(getString(context, R.string.device_status_normal),
                                getString(context, R.string.power_bluetooth)));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_GPS_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(
                                context, R.string.gps_status) + "  " + getString(context, R.string.green_summary_off));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_GESTURE_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(
                                context, R.string.gesture_status) + "  " + getString(context, R.string.green_summary_off));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_SCREEN_SLEEP_TIME:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.screen_sleep_time_normal));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_SCREEN_POWER_SAVE_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(
                                context, R.string.screen_power_save) + "  " + getString(context, R.string.green_summary_on));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_RUBBISH_TO_BE_CLEANED_DIRECTLY:
                        boolean rubbish = (boolean)objects[0];
                        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, !rubbish);
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, !rubbish? R.string.a_lot_of_rubbish : R.string.a_little_rubbish));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, rubbish);
                        if (!rubbish){
                            updateOptimizeList(bundle, i);
                        }else {
                            removeFromOptimizeList(i);
                        }
                        updateSpeedupList(bundle, checkItem);
                        break;
                    case CHECK_REMAINING_SPACE:
                        boolean notEnough = (boolean)objects[0];
                        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, notEnough);
                        bundle.putString(BUNDLE_KEY.contentTitle, notEnough ?
                                getString(context, R.string.remaining_space_not_enough):
                                String.format(getString(context, R.string.remaining_space), (String)objects[1]));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, !notEnough);
                        if (notEnough){
                            updateOptimizeList(bundle, i);
                        }else {
                            removeFromOptimizeList(i);
                        }
                        updateSpeedupList(bundle, checkItem);
                        break;
                    case CHECK_NOT_FREQUENTLY_USED_APPS:
                        boolean notFrequentlyUsed = !(boolean)objects[0];
                        bundle.putBoolean(BUNDLE_KEY.isSuggestedItem, notFrequentlyUsed);
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, notFrequentlyUsed? R.string.uninstall_not_frequently_used_apps : R.string.app_used_normally));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, !notFrequentlyUsed);
                        if (!notFrequentlyUsed){
                            removeFromOptimizeList(i);
                            updateSpeedupList(bundle, checkItem);
                        }
                        break;
                    case CHECK_IF_HIGH_POWER_CONSUMPTION_NOTIFICATION_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.power_consume_setting_title)
                                + "  " + getString(context,  R.string.status_opened));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_RAM_CPU_MONITOR_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.cpu_monitor_setting_title)
                                + "  " + getString(context,  R.string.status_opened));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_ADAPTIVE_BATTERY_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.adaptive_battery)
                                + "  " + getString(context, R.string.status_opened));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_AUTOMATIC_SCREEN_BRIGHTNESS_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.automatic_brightness)
                                + "  " + getString(context,  R.string.status_opened));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_CLEANNING_ON_LOCKSCREEN_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.screenoff_clean_setting_title)
                                + "  " + getString(context, R.string.status_opened));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updateSpeedupList(bundle, checkItem);
                        break;
                    case CHECK_IF_INTELLIGENT_SLEEP_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.night_saving_title)
                                + "  " + getString(context, R.string.status_opened));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_HOT_POT_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, String.format(getString(context, R.string.device_status_normal),
                                getString(context, R.string.hot_pot)));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updatePowerManagerList(bundle, checkItem);
                        break;
                    case CHECK_IF_SET_TRAFFIC_SET:
                    case CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH:
                        boolean set = (boolean)objects[0];
                        if (set){
                            boolean enough = (boolean)objects[1];
                            boolean over = (boolean)objects[2];
                            String freeTraffic = (String)objects[3];
                            bundle.putString(BUNDLE_KEY.contentTitle, enough ? String.format(getString(context, R.string.free_traffic),freeTraffic) :
                                    over? String.format(getString(context, R.string.traffic_over_used),freeTraffic) + getString(context, R.string.network_control).toLowerCase() :
                                            getString(context, R.string.free_traffic_too_less) + getString(context, R.string.network_control).toLowerCase());
                            bundle.putBoolean(BUNDLE_KEY.checkStatus, enough);
                            bundle.putString(BUNDLE_KEY.btnText, enough? null : getString(context, R.string.action_settings));
                            bundle.putInt(BUNDLE_KEY.checkItem, CHECK_IF_FREE_TRAFFIC_NOT_ENOUGH);
                            updateTrafficAssistentList(bundle, checkItem);
                            if (enough){
                                removeFromOptimizeList(i);
                            }else {
                                updateOptimizeList(bundle, i);
                            }
                        }
                        break;
                    case CHECK_IF_TRAFFIC_SAVE_OPENED:
                        bundle.putString(BUNDLE_KEY.contentTitle, getString(context, R.string.traffic_save)
                                + "  " + getString(context, R.string.status_opened));
                        bundle.putBoolean(BUNDLE_KEY.checkStatus, true);
                        removeFromOptimizeList(i);
                        updateTrafficAssistentList(bundle, checkItem);
                        break;
                }
                break;
            }
        }
    }

    public static void releaseAllList(){
        mSystemCheckList.clear();
        mSubtractionList.clear();
        mTrafficAssistentList.clear();
        mSpeedupList.clear();
        mPowerManagerList.clear();
        mToBeOptimizeList.clear();
    }

    public static void addToSubtractionList(int checkItem, int score){
        mSubtractionList.put(checkItem, score);
    }

    public static void setRunningProcessList(List<ItemInfo> runnings){
        mRunningProcessList = runnings;
    }

    public static void removeRunningItemByPackageName(String pkgName){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mRunningProcessList != null){
                    for (ItemInfo item : mRunningProcessList){
                        if (item.getPackageName().equals(pkgName)){
                            mRunningProcessList.remove(item);
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    public static void releaseRunningProcessList(){
        mRunningProcessList.clear();
    }
}
