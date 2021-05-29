package com.cydroid.softmanager.powersaver.utils;

/*
 * 初始化值为默认值
 * 项目未提供config.xml时采用默认值
 * */
//Gionee guoxt 2015-03-04 modified for CR01455466 begin
public class PowerConfig {
    public int battery_capacity = 4010;
    public int ac_current = 3600;
    public int usb_current = 500;
    public int original_current = 377;
    public int original_brightness = 255;
    public float current_in_supermode = 8;

    public float current_per_brightness = 0.7f;
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    public float intelligent_scene_weight_open = 0.03f;
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
    public float green_background_weight = 0.02f;
    public float system_animator_weight = 0.005f;
    public float darktheme_weight = 0.07f;
    public float wifi_weight = 0.07f;
    public float bt_weight = 0.015f;
    public float data_weight = 0.02f;
    public float gps_weight = 0.012f;
    public float sync_weight = 0.017f;
    public float push_weight = 0f;
    public float cpufreq_weight = 0.05f;
    public float screen_save_weight = 0.03f;
    public float gestures_weight = 0.01f;

    public float timeout_zero_weight = 0.035f;
    public float timeout_one_weight = 0.032f;
    public float timeout_two_weight = 0.03f;
    public float timeout_three_weight = 0.025f;
    public float timeout_four_weight = 0.015f;
    public float timeout_five_weight = 0.01f;
    public float timeout_six_weight = 0.005f;
    public float timeout_seven_weight = 0f;

    public int zero_ten_time = 93;
    public int ten_twenty_time = 83;
    public int twenty_thirty_time = 52;
    public int thirty_forty_time = 29;
    public int forty_fifty_time = 38;
    public int fifty_sixty_time = 63;
    public int sixty_seventy_time = 65;
    public int seventy_eight_time = 62;
    public int eight_ninety_time = 65;
    public int ninety_hundred_time = 78;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("print PowerConfig :\n");
        sb.append("battery_capacity:" + battery_capacity + "\n");
        sb.append("ac_current:" + ac_current + "\n");
        sb.append("usb_current:" + usb_current + "\n");
        sb.append("original_current:" + original_current + "\n");
        sb.append("original_brightness:" + original_brightness + "\n");
        sb.append("current_in_supermode:" + current_in_supermode + "\n");
        sb.append("current_per_brightness:" + current_per_brightness + "\n");
        sb.append("intelligent_scene_weight_open:" + intelligent_scene_weight_open + "\n");
        sb.append("green_background_weight:" + green_background_weight + "\n");
        sb.append("system_animator_weight:" + system_animator_weight + "\n");
        sb.append("darktheme_weight:" + darktheme_weight + "\n");
        sb.append("wifi_weight:" + wifi_weight + "\n");
        sb.append("bt_weight:" + bt_weight + "\n");
        sb.append("data_weight:" + data_weight + "\n");
        sb.append("gps_weight:" + gps_weight + "\n");
        sb.append("sync_weight:" + sync_weight + "\n");
        sb.append("push_weight:" + push_weight + "\n");
        sb.append("cpufreq_weight:" + cpufreq_weight + "\n");
        sb.append("screen_save_weight:" + screen_save_weight + "\n");
        sb.append("gestures_weight:" + gestures_weight + "\n");
        sb.append("timeout_zero_weight:" + timeout_zero_weight + "\n");
        sb.append("timeout_one_weight:" + timeout_one_weight + "\n");
        sb.append("timeout_two_weight:" + timeout_two_weight + "\n");
        sb.append("timeout_three_weight:" + timeout_three_weight + "\n");
        sb.append("timeout_four_weight:" + timeout_four_weight + "\n");
        sb.append("timeout_five_weight:" + timeout_five_weight + "\n");
        sb.append("timeout_six_weight:" + timeout_six_weight + "\n");
        sb.append("timeout_seven_weight:" + timeout_seven_weight + "\n");
        sb.append("zero_ten_time:" + zero_ten_time + "\n");
        sb.append("ten_twenty_time:" + ten_twenty_time + "\n");
        sb.append("twenty_thirty_time:" + twenty_thirty_time + "\n");
        sb.append("thirty_forty_time:" + thirty_forty_time + "\n");
        sb.append("forty_fifty_time:" + forty_fifty_time + "\n");
        sb.append("fifty_sixty_time:" + fifty_sixty_time + "\n");
        sb.append("sixty_seventy_time:" + sixty_seventy_time + "\n");
        sb.append("seventy_eight_time:" + seventy_eight_time + "\n");
        sb.append("eight_ninety_time:" + eight_ninety_time + "\n");
        sb.append("ninety_hundred_time:" + ninety_hundred_time + "\n");
        return sb.toString();
    }

}
