package com.cydroid.powersaver.launcher.util;

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

}
//Gionee guoxt 2015-03-04 modified for CR01455466 end

