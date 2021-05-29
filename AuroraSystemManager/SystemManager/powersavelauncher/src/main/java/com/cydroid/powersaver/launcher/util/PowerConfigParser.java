package com.cydroid.powersaver.launcher.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Xml;

import java.math.BigDecimal;

import android.os.SystemProperties;

public class PowerConfigParser {
    private static final String CONFIG_FILE_PATH = "/system/etc/Cyee_SystemManager/config.xml";
    private static final boolean DEBUG = true;
    private static final String TAG = "PowerConfigParser";
    //Gionee guoxt 2015-03-04 modified for CR01455466 begin
    private static String projectName = SystemProperties.get("ro.gn.common.mainboard.prop");

    public static PowerConfig getProjectConfig(Context context) {
        PowerConfig config = new PowerConfig();
        boolean projectEndFlag = false;
        boolean projectBeginFlag = false;
        InputStream is = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            is = getConfigStream(context);
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT && !projectEndFlag) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
//                        Debug.log(DEBUG, TAG, "PowerConfig-->getProjectConfig(), START_TAG");
                        if (projectName.equals(tagName)) {
                            projectBeginFlag = true;
                        }
                        if (projectBeginFlag) {
                            initConfigByTagName(parser, config);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (projectName.equals(tagName)) {
                            projectEndFlag = true;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "PowerConfig-->getProjectConfig() throw exception, new PowerConfig default", e);
            return new PowerConfig();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return config;
    }

    private static void initConfigByTagName(XmlPullParser parser, PowerConfig config) throws Exception {
        String tagName = parser.getName();
//        Debug.log(DEBUG, TAG, "PowerConfig-->initConfigByTagName(), tagName = " + tagName);
        if (tagName.equals("project")) {

        } else if (tagName.equals("battery_capacity")) {
            config.battery_capacity = parseInt(parser.nextText());
        } else if (tagName.equals("ac_charge_current")) {
            config.ac_current = parseInt(parser.nextText());
        } else if (tagName.equals("usb_charge_current")) {
            config.usb_current = parseInt(parser.nextText());
        } else if (tagName.equals("original_current")) {
            config.original_current = parseInt(parser.nextText());
        } else if (tagName.equals("original_brightness")) {
            config.original_brightness = parseInt(parser.nextText());
        } else if (tagName.equals("current_in_supermode")) {
            config.current_in_supermode = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("current_per_brightness")) {
            config.current_per_brightness = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("green_background_weight")) {
            config.green_background_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("system_animator_weight")) {
            config.system_animator_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("darktheme_weight")) {
            config.darktheme_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("wifi_weight")) {
            config.wifi_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("bt_weight")) {
            config.bt_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("data_weight")) {
            config.data_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("gps_weight")) {
            config.gps_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("sync_weight")) {
            config.sync_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("push_weight")) {
            config.push_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("cpufreq_weight")) {
            config.cpufreq_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("screen_save_weight")) {
            config.screen_save_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("gestures_weight")) {
            config.gestures_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("timeout_zero_weight")) {
            config.timeout_zero_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("timeout_one_weight")) {
            config.timeout_one_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("timeout_two_weight")) {
            config.timeout_two_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("timeout_three_weight")) {
            config.timeout_three_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("timeout_four_weight")) {
            config.timeout_four_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("timeout_five_weight")) {
            config.timeout_five_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("timeout_six_weight")) {
            config.timeout_six_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("timeout_seven_weight")) {
            config.timeout_seven_weight = Float.parseFloat(parser.nextText());
        } else if (tagName.equals("zero_ten_time")) {
            config.zero_ten_time = parseInt(parser.nextText());
        } else if (tagName.equals("ten_twenty_time")) {
            config.ten_twenty_time = parseInt(parser.nextText());
        } else if (tagName.equals("twenty_thirty_time")) {
            config.twenty_thirty_time = parseInt(parser.nextText());
        } else if (tagName.equals("thirty_forty_time")) {
            config.thirty_forty_time = parseInt(parser.nextText());
        } else if (tagName.equals("forty_fifty_time")) {
            config.forty_fifty_time = parseInt(parser.nextText());
        } else if (tagName.equals("fifty_sixty_time")) {
            config.fifty_sixty_time = parseInt(parser.nextText());
        } else if (tagName.equals("sixty_seventy_time")) {
            config.sixty_seventy_time = parseInt(parser.nextText());
        } else if (tagName.equals("seventy_eight_time")) {
            config.seventy_eight_time = parseInt(parser.nextText());
        } else if (tagName.equals("eight_ninety_time")) {
            config.eight_ninety_time = parseInt(parser.nextText());
        } else if (tagName.equals("ninety_hundred_time")) {
            config.ninety_hundred_time = parseInt(parser.nextText());
        }

    }

    private static InputStream getConfigStream(Context context) {
        try {
            File file = new File(CONFIG_FILE_PATH);
            if (file.exists()) {
                return new FileInputStream(file);
            }
        } catch (Exception e) {
            Log.e(TAG, "PowerConfig-->getConfigStream(), read CONFIG_FILE wrong or some problems");
        }
        InputStream is = null;
        try {
            Log.e(TAG, "PowerConfig-->getConfigStream(), open config.xml");
            is = context.getAssets().open("config.xml");
        } catch (Exception e) {
            Log.e(TAG, "PowerConfig-->getConfigStream(),open asset/config.xml error");
        }

        return is;
    }

    private static int parseInt(String str) {
        return new BigDecimal(str).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

}
