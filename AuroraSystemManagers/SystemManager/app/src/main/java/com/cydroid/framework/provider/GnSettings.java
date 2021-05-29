package com.cydroid.framework.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings;

public class GnSettings {

    public static class System {

        public static final String GPRS_CONNECTION_SETTING = "gprs_connection_setting";
        public static long DEFAULT_SIM_NOT_SET = -5;
        public static final String GPRS_CONNECTION_SIM_SETTING = "gprs_connection_sim_setting";
        public static long GPRS_CONNECTION_SIM_SETTING_NEVER = 0;
        public static int GPRS_CONNECTION_SETTING_DEFAULT = 0;
        public static Uri DEFAULT_RINGTONE_URI = android.provider.Settings.System.DEFAULT_RINGTONE_URI;
        // Gionee guoyx 20130305 modified for CR00778697 begin
        public static final String SMS_SIM_SETTING = "sms_sim_setting";// MTK:"sms_sim_setting";
        // Gionee guoyx 20130305 modified for CR00778697 end
        // Gionee <guoyx><2013-04-15> modified for CR00797011 begin
        public static long DEFAULT_SIM_SETTING_ALWAYS_ASK = -1;
        // Gionee <guoyx><2013-04-15> modified for CR00797011 end
        public static String ENABLE_INTERNET_CALL = "enable_internet_call_value";
        public static String AIRPLANE_MODE_ON = "airplane_mode_on";

        public static long getLong(ContentResolver contentResolver, String gprsConnectionSimSetting,
                long defaultSimNotSet) {
            // TODO Auto-generated method stub
            return 0;
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            return Settings.System.getInt(cr, name, def);
        }

        public static int getInt(ContentResolver contentResolver, String gprsConnectionSetting,
                String gprsConnectionSettingDefault) {
            // TODO Auto-generated method stub
            return 0;
        }

        /**
         * Roaming reminder mode<br/>
         * <b>Values: sim ID</b><br/>
         * 0 - once.<br/>
         * 1 - Always ask.<br/>
         * 2 - Never.<br/>
         * 
         * @hide
         */
        public static String ROAMING_REMINDER_MODE_SETTING = "roaming_reminder_mode_setting";

        /**
         * Dual SIM mode setting.<br/>
         * <b>Values:</b><br/>
         * 1 - SIM1 only mode.<br/>
         * 2 - SIM2 only mode.<br/>
         * 3 - Dual SIM mode.<br/>
         * 4 - Flight mode.<br/>
         * 
         * @hide
         */
        public static String DUAL_SIM_MODE_SETTING = "dual_sim_mode_setting";

        // Gionee <fengjianyi><2013-03-15> modify for CR00773021 start
        /**
         * voice call default sim<br/>
         * <b>Values: sim ID</b><br/>
         * 
         * @hide
         */
        public static String VOICE_CALL_SIM_SETTING = "voice_call_sim_setting";// MTK:"voice_call_sim_setting";
        // Gionee <fengjianyi><2013-03-15> modify for CR00773021 end

        /**
         * Voice call setting as Internet call
         * 
         * @hide
         */
        public static long VOICE_CALL_SIM_SETTING_INTERNET = -2;

        /**
         * video call default sim<br/>
         * <b>Values: sim ID</b><br/>
         * 
         * @hide
         */
        public static String VIDEO_CALL_SIM_SETTING = "video_call_sim_setting";

        // Gionee: 20120918 chenrui add for CR00696600 begin
        public static String ALERT_MISS_MSG = "alert_miss_msg";
        public static String ALERT_MISS_MSG_INTERVAL = "alert_miss_msg_interval";
        // Gionee: 20120918 chenrui add for CR00696600 end

        // Gionee guoyx 20130223 add for Qualcomm solution CR00773050 begin
        /**
         * Subscription to be used for data call on a multi sim device. The supported values are 0 = SUB1, 1 =
         * SUB2.
         * 
         * @hide
         */
        public static String MULTI_SIM_DATA_CALL_SUBSCRIPTION = "multi_sim_data_call";
        // Gionee guoyx 20130223 add for Qualcomm solution CR00773050 end
    }
}
