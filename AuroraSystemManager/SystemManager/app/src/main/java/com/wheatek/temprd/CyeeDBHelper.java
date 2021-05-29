
package com.wheatek.temprd;

import cyee.provider.CyeeSettings;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemProperties;
import android.util.Log;
//Gionee <wangguojing> <2013-09-04> add for CR00873246 begin
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.Intent;
//Gionee <wangguojing> <2013-09-04> add for CR00873246 end

// Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begin
import android.database.Cursor;
// Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end

//Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;

import com.android.internal.util.XmlUtils;
import com.cydroid.softmanager.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
//Gionee <wangguojing> <2014-05-12> add for CR01237681 end
import java.util.ArrayList;

public class CyeeDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "CyeeSettingsProvider";
    private static final String DATABASE_NAME = "cyeesettings.db";
    private static final int DATABASE_VERSION = 66;
    // private static final String SSG_QUICK_OPERATING = CyeeSettings.SSG_QUICK_OPERATING;
    public  static final String SSG_QUICK_OPERATING = "ssg_quick_operating";
    public  static final String SSG_QUICK_OPERATING_UP_GESTURE_CHOICE = "ssg_quick_operating_up_choice"; 
    public  static final String SSG_QUICK_OPERATING_UP_GESTURE_APP_NAME_OR_PHONE_NUMBER = "ssg_quick_operating_up_app_or_phone_number"; 
    public  static final String SSG_QUICK_OPERATING_RIGHT_GESTURE_CHOICE = "ssg_quick_operating_right_choice"; 
    public  static final String SSG_QUICK_OPERATING_RIGHT_GESTURE_APP_NAME_OR_PHONE_NUMBER = "ssg_quick_operating_right_app_or_phone_number"; 
    public  static final String SSG_QUICK_OPERATING_LEFT_GESTURE_CHOICE = "ssg_quick_operating_left_choice"; 
    public  static final String SSG_QUICK_OPERATING_LEFT_GESTURE_APP_NAME_OR_PHONE_NUMBER = "ssg_quick_operating_left_app_or_phone_number"; 
    public  static final String SSG_QUICK_OPERATING_DOWN_GESTURE_CHOICE = "ssg_quick_operating_down_choice"; 
    public  static final String SSG_QUICK_OPERATING_DOWN_GESTURE_APP_NAME_OR_PHONE_NUMBER = "ssg_quick_operating_down_app_or_phone_number"; 
    // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end

    //Gionee <huangyuncai> <2014-03-04> add for CR01087530 begin
    public static final String NOTIFICATION_DISABLE_REMINDER = "notification_disable_reminder";
    public static final String NETWORK_SPEED_DISPLAY = "network_speed_display";
    //Gionee <huangyuncai> <2014-03-04> add for CR01087530 end
    // Gionee <bug> <wangyaohui> <2013-10-07> modify for CR00911478 end
    private Context mContext;
    //Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
    private static final String CYEESETTING_DB_CONFIG_FILE    = "/system/etc/CyeeSettingConfig.xml";     
    //Gionee <wangguojing> <2014-05-12> add for CR01237681 end

    //gionee yewq 2016-12-21 modify for 52417 begin
    private static boolean gnTorchSupport = SystemProperties.get("ro.cy.torch.gesture.support", "no").equals("yes");
    //gionee yewq 2016-12-21 modify for 52417 end

    //Gionee <GN_Oversea_Bug> <fujiabing> <20170328> add for 77768 begin
    private static final boolean gnIPflag = SystemProperties.get("ro.gn.oversea.custom").equals("ISRAEL_PELEPHONE");
    //Gionee <GN_Oversea_Bug> <fujiabing> <20170328> add for 77768 end
    //Chenyee <CY_Oversea_Req> <zhaopeng> <20180423> add for CSW1705EI-64 begin
    private static final boolean cyEIflag = SystemProperties.get("ro.cy.custom").equals("INDONESIA_LUNA");
    //Chenyee <CY_Oversea_Req> <zhaopeng> <20180423> add for CSW1705EI-64 end
    //Gionee <GN_oversea_Bug> <lipeiming> <20170413> add for 102958 begin
    private static final boolean gnSYflag = SystemProperties.get("ro.gn.oversea.custom").equals("BANGLADESH_SYMPHONY");
    //Gionee <GN_oversea_Bug> <lipeiming> <20170413> add for 102958 end
    //Gionee <GN_Oversea_Req> <linzhicong> <20170517> add for #141269 beign
    private static final boolean sGnBluFlag = SystemProperties.get("ro.cy.custom").equals("SOUTH_AMERICA_BLU");
    //Gionee <GN_Oversea_Req> <linzhicong> <20170517> add for #141269 end
    //Gionee <GN_Oversea_Req> <linzhicong> <20170523> add for #147092 beign
    public static final boolean sGnQMOFlag = SystemProperties.get("ro.gn.oversea.custom") .equals("PAKISTAN_QMOBILE");
    //Gionee <GN_Oversea_Req> <linzhicong> <20170523> add for #147092 end
    //Gionee <GN_Oversea_Req> <fujiabing> <20170818> add for 180104 begin
    public static final boolean isLEDRemindSupport = SystemProperties.get("ro.gn.led.remind.support","no").equals("yes");
    //Gionee <GN_Oversea_Req> <fujiabing> <20170818> add for 180104 end
    //Gionee <GN_Oversea_Req> <zhanglz> <20170825> for 195719 begin
    public static final boolean BLUMusic = SystemProperties.get("ro.gn.cyee.music.support").equals("no");
    //Gionee <GN_Oversea_Req> <zhanglz> <20170825> for 195719 end
    //Chenyee <CY_REQ> <chenyu> <20180127> add for Android GO ROM begin
    public static final boolean cyIsGoRom = SystemProperties.get("ro.cy.go.rom", "no").equals("yes");
    //Chenyee <CY_REQ> <chenyu> <20180127> add for Android GO ROM end
    //Chenyee <CY_REQ> <puyan> <20180309> add for SW17W16RV-236 begin
    private static final boolean cyRVFlag = SystemProperties.get("ro.cy.custom").equals("HighScreen");
    //Chenyee <CY_REQ> <puyan> <20180309> add for SW17W16RV-236 end
    //Chenyee <CY_Oversea_Req> <puyan> <20180421> add for CSW1703CX-28 begin
    private static final boolean cyCXFlag = SystemProperties.get("ro.cy.custom").equals("XiaoLaJiao");
    //Chenyee <CY_Oversea_Req> <puyan> <20180421> add for CSW1703CX-28 begin

    //Chenyee <CY_REQ> <chenyu> <20180510> add for AC/CSW1703A-2504 begin
    public static final boolean cyACFlag = SystemProperties.get("ro.cy.custom").equals("ALGERIA_CONDOR");
    //Chenyee <CY_REQ> <chenyu> <20180510> add for AC/CSW1703A-2504 end

    //Chenyee <CY_REQ> <chenyu> <20180711> add for SW17W13AC1-5 begin
    public static final boolean gmsExpressSupport = SystemProperties.get("ro.cy.gmsexpress.plus.support", "no").equals("yes");
    //Chenyee <CY_REQ> <chenyu> <20180711> add for SW17W13AC1-5 end

    //Chenyee  ningtao 20180625 modify for CSW1803A-620 begin
    public static final boolean mNewLcdEffectSupport = SystemProperties.get("ro.cy.new.lcd.effect.support", "no").equals("yes");
    //Chenyee ningtao  20180625 modify for CSW1803A-620 end

    //Chenyee <CY_REQ> <chenyu> <20180828> add for CSW1803TL-23 begin
    public static final boolean cyTLFlag = SystemProperties.get("ro.cy.custom").equals("ZIMBABWE_GTEL");
    //Chenyee <CY_REQ> <chenyu> <20180828> add for CSW1803TL-23 end

    //Chenyee chenyu  20180927 modify for CSW1703BN-216 begin
    public static final boolean isCyCarrier = SystemProperties.get("ro.cy.carrier").equals("VERIZON");
    //Chenyee chenyu  20180927 modify for CSW1703BN-216 end

    //Chenyee chenyu  20181025 modify for CSW1703A-3941 begin
    public static final boolean is1802Project = SystemProperties.get("ro.cy.vernumber").contains("CSW1802");
    //Chenyee chenyu  20181025 modify for CSW1703A-3941 end

    //Chenyee chenyu  20181025 modify for CSW1703A-3941 begin
    public static final boolean is1803Project = SystemProperties.get("ro.cy.vernumber").contains("CSW1803");
    //Chenyee chenyu  20181025 modify for CSW1703A-3941 end

  //Gionee <lizhipneg> <2016-01-04> add for CR01602143 begin
//    public static final int NODE_TYPE_FORCE_TOUCH_LOW_THRE = 57;                ///sys/bus/platform/devices/tp_wake_switch/force_touch_low_threshold
//    public static final int NODE_TYPE_FORCE_TOUCH_HIGH_THRE = 58;               ///sys/bus/platform/devices/tp_wake_switch/force_touch_high_threshold
//    public static final int NODE_TYPE_FORCE_TOUCH_THRESHOLD = 59;               ///sys/bus/platform/devices/tp_wake_switch/touch_threshold
    public  static String NODE_TYPE_FORCE_TOUCH_LOW_THRE = "sys/bus/platform/devices/tp_wake_switch/force_touch_low_threshold";
    public static  String NODE_TYPE_FORCE_TOUCH_HIGH_THRE ="sys/bus/platform/devices/tp_wake_switch/force_touch_high_threshold";
    public static  String NODE_TYPE_FORCE_TOUCH_THRESHOLD = "sys/bus/platform/devices/tp_wake_switch/touch_threshold";
    
    public static final String NODE_TYPE_FORCE_TOUCH_LOW_THRE_PATH = "force_touch_low_threshold_path";
    public static final String NODE_TYPE_FORCE_TOUCH_HIGH_THRE_PATH  ="force_touch_high_threshold_path";
    public static final String NODE_TYPE_FORCE_TOUCH_THRESHOLD_PATH  = "touch_threshold_path";
  //Gionee <lizhipneg> <2016-01-04> add for CR01602143 end

    public CyeeDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "cyeesettings database created");
        
        db.execSQL("CREATE TABLE config (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE ON CONFLICT REPLACE," +
                    "value TEXT" +
                    ");");
        db.execSQL("CREATE INDEX systemIndex1 ON config (name);");

        // Load inital settings values
        loadConfigSettings(db);
        //Gionee <wangguojing> <2013-10-11> add for CR00932764 begin
        copySettingsDB(db);
        //Gionee <wangguojing> <2013-10-11> add for CR00932764 end
        //Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
        updateDBValue(db);
        //Gionee <wangguojing> <2014-05-12> add for CR01237681 end
      //Gionee <lizhipneg> <2016-01-04> add for CR01602143 begin
        loadForceTouchSettings(db);
        //Gionee <lizhipneg> <2016-01-04> add for CR01602143 end
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        Log.e(TAG, "Upgrading gnsettings database from version " + oldVersion + " to "
                + currentVersion);

        int upgradeVersion = oldVersion;

        if (upgradeVersion == 1) {
			// to do ..
            // Gionee <bug> <wangyaohui> <2013-10-07> add for CR00911478 begin
           	db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_SWITCH,
                    R.bool.def_sound_control_switch);
				loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_CALLING,
                    R.bool.def_sound_control_calling);
				loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_MESSAGE,
                    R.bool.def_sound_control_message);
				loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_LOCKSCREEN,
                    R.bool.def_sound_control_lockscreen);
				loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_ALARMCLOCK,
                    R.bool.def_sound_control_alarmclock);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            // Gionee <bug> <wangyaohui> <2013-10-07> add for CR00911478 end
            upgradeVersion = 2;
        }

        // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begin
         if (upgradeVersion == 2) {
           	db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                //if (SystemProperties.get("ro.gn.gesture.support").equals("yes")) {
                    loadBooleanSetting(stmt,SSG_QUICK_OPERATING,R.bool.def_fast_operation);
                    loadSetting(stmt,SSG_QUICK_OPERATING_UP_GESTURE_CHOICE,"1");
                    loadSetting(stmt,SSG_QUICK_OPERATING_UP_GESTURE_APP_NAME_OR_PHONE_NUMBER,
                                              "com.android.camera/com.android.camera.CameraLauncher");
                    loadSetting(stmt,SSG_QUICK_OPERATING_RIGHT_GESTURE_CHOICE,"1");
                    loadSetting(stmt,SSG_QUICK_OPERATING_RIGHT_GESTURE_APP_NAME_OR_PHONE_NUMBER,
                                              "com.tencent.mm/com.tencent.mm.ui.LauncherUI");				
                    loadSetting(stmt,SSG_QUICK_OPERATING_LEFT_GESTURE_CHOICE,0);
                    loadSetting(stmt,SSG_QUICK_OPERATING_LEFT_GESTURE_APP_NAME_OR_PHONE_NUMBER,"");
                    loadSetting(stmt,SSG_QUICK_OPERATING_DOWN_GESTURE_CHOICE,0);
                    loadSetting(stmt,SSG_QUICK_OPERATING_DOWN_GESTURE_APP_NAME_OR_PHONE_NUMBER,"");
                    
                    
                    //Gionee <wangguojing> <2013-10-31> add for CR00922210 begin
                    loadBooleanSetting(stmt, CyeeSettings.SDG_DEL_PHOTOS,
                        R.bool.def_sdg_del_photos);
                    loadBooleanSetting(stmt, CyeeSettings.SDG_DEL_PHOTOS_SHOW,
                        R.bool.def_sdg_del_photos);
                    
                    loadBooleanSetting(stmt, CyeeSettings.SDG_TASK_CONTROL,
                        R.bool.def_sdg_task_control);
                    loadBooleanSetting(stmt, CyeeSettings.SDG_TASK_CONTROL_SHOW,
                        R.bool.def_sdg_task_control);
                    
                    loadBooleanSetting(stmt, CyeeSettings.SSG_DOUBLECLICK_WAKE,
                        R.bool.def_ssg_doubleclick_wake);
                    loadBooleanSetting(stmt, CyeeSettings.SSG_DOUBLECLICK_WAKE_SHOW,
                        R.bool.def_ssg_doubleclick_wake_show);
                    //Gionee <wangguojing> <2013-10-31> add for CR00922210 end
                    
                //}
                //Gionee <wangguojing> <2013-10-11> add for GPS Optimization begin
                loadBooleanSetting(stmt, CyeeSettings.GPS_PROMPT_REMIND,R.bool.def_gps_prompt_remind);
                //Gionee <wangguojing> <2013-10-11> add for GPS Optimization end
                
                //Gionee <wangguojing> <2013-10-24> add for CR00933390 begin
                //if (SystemProperties.get("ro.gn.floatingwindow.support").equals("yes")) {
                    loadStringSetting(stmt, CyeeSettings.VIRTUAL_DISPLAY_DEVICES,R.string.def_virtual_display_devices);
                //}
                //Gionee <wangguojing> <2013-10-24> add for CR00933390 end
                //Gionee <wangguojing> <2013-10-31> add for CR00942710 begin
                //if (SystemProperties.get("ro.gn.glove_patterns.support").equals("yes")) {
                    loadBooleanSetting(stmt, CyeeSettings.GLOVE_PATTERNS,R.bool.def_glove_patterns);
                //}
                //Gionee <wangguojing> <2013-10-31> add for CR00942710 end
				
                //Gionee <wangguojing> <2013-11-02> add for CR00943428 begin
                //if (SystemProperties.get("ro.gn.voicewake.support").equals("yes")) {
                    loadBooleanSetting(stmt, CyeeSettings.VOICE_WAKE_SWITCH,R.bool.def_voice_wake_switch);
                    loadBooleanSetting(stmt, CyeeSettings.VOICE_WAKE_REMIND,R.bool.def_voice_wake_remind);
                    loadBooleanSetting(stmt, "voice_wake_word"/*CyeeSettings.VOICE_WAKE_WORD*/,R.bool.def_voice_wake_word);
                //}
                //Gionee <wangguojing> <2013-11-02> add for CR00943428 end
                
                //Gionee <wangguojing> <2013-11-04> add for CR00942651 begin
                //if (SystemProperties.get("ro.gn.lcd.effect.support").equals("yes")) {
                    loadBooleanSetting(stmt, CyeeSettings.LCD_EFFECT_MODE,R.bool.def_lcd_effect_adjust);
                //}
                //Gionee <wangguojing> <2013-11-02> add for CR00942651 end
                
                //Gionee <wangguojing> <2013-11-04> add for CR00944538 begin
                loadBooleanSetting(stmt, "show_powersave_dialog"/*CyeeSettings.SHOW_POWERSAVE_DIALOG*/,R.bool.def_show_powersave_dialog);
                //Gionee <wangguojing> <2013-11-04> add for CR00944538 end

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            
            upgradeVersion = 3;
        }
        // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end 
        
        //Gionee <wangguojing> <2013-12-05> add for CR00966365 begin
        if (upgradeVersion == 3) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
              	  loadSetting(stmt, CyeeSettings.FLIP_SOUNDS_ENABLED, 1);
              	  
              	  loadStringSetting(stmt, CyeeSettings.FLIP_ON_SOUND,
              			  R.string.zzzzz_gn_def_flip_on_sound);
              	  loadStringSetting(stmt, CyeeSettings.FLIP_OFF_SOUND,
              			  R.string.zzzzz_gn_def_flip_off_sound);
                //}
              
                loadBooleanSetting(stmt, CyeeSettings.FONT_SIZE, 
              		  R.integer.def_gn_font_size);
              
                loadBooleanSetting(stmt, CyeeSettings.HAPTIC_VIBRATION_ENABLED,
              		  R.bool.def_haptic_vibration);
              
                loadBooleanSetting(stmt, CyeeSettings.SWITCH_VIBRATION_ENABLED,
              		  R.bool.def_switch_vibration);
                loadBooleanSetting(stmt, CyeeSettings.DIALPAD_VIBRATION_ENABLED,
              		  R.bool.def_dialpad_vibration);
                loadBooleanSetting(stmt, CyeeSettings.LOCKSCREEN_VIBRATION_ENABLED,
              		  R.bool.def_lockscreen_vibration);
                loadBooleanSetting(stmt, CyeeSettings.SELECTAPP_VIBRATION_ENABLED,
              		  R.bool.def_selectapp_vibration);
              
                loadBooleanSetting(stmt, CyeeSettings.RING_VIBRATION_ENABLED,
              		  R.bool.def_ring_vibration);
                loadBooleanSetting(stmt, CyeeSettings.MMS_VIBRATION_ENABLED,
              		  R.bool.def_mms_vibration);
                loadBooleanSetting(stmt, CyeeSettings.NOTIFICATION_VIBRATION_ENABLED,
              		  R.bool.def_notification_vibration);
              
                //if (SystemProperties.get("ro.gn.fanfanwidget.support").equals("yes")) {
              	  loadBooleanSetting(stmt, CyeeSettings.FANFAN_WIDGET_AUTO_PUSH,
              			  R.bool.def_fanfan_widget);
                //}
              
                //if (SystemProperties.get("ro.gn.respirationlamp.support").equals("yes")) {
              	  loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_LOW_POWER,
              			  R.bool.def_respirationlamp_low_power);
              	  loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_IN_CHARGE,
              			  R.bool.def_respirationlamp_in_charge);		  
              	  loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_NOTIFICATION,
              			  R.bool.def_respirationlamp_notification);
              	  loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_MUSIC,
              			  R.bool.def_respirationlamp_music);
              	  loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_CALL,
              			  R.bool.def_respirationlamp_call);
                //}
              
                
                //if (SystemProperties.get("ro.gn.soundctrl.support").equals("yes")) {
              	  loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_OPEN_REMIND,
              			  R.bool.def_sound_control_open_remind);
              	  loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_CLOSE_REMIND,
              			  R.bool.def_sound_control_close_remind);
                //}
              
              //if (SystemProperties.get("ro.gn.guestmode.support").equals("yes")) {
              	loadBooleanSetting(stmt, CyeeSettings.GUEST_MODE,
              			R.bool.def_guest_mode);
              	loadBooleanSetting(stmt, CyeeSettings.FIRST_OPEN_GUEST_MODE,
              			R.bool.def_first_open_guest_mode);
              	loadBooleanSetting(stmt, CyeeSettings.GUEST_PASS_ENABLE,
              			R.bool.def_guest_pass_enable);
              	loadStringSetting(stmt, CyeeSettings.GUEST_PASS,
              			R.string.zzzzz_gn_def_guest_pass);
              //}
              
              //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
              	loadIntegerSetting(stmt, CyeeSettings.FLIP_HANGUP_CALL_SWITCH,
              			R.integer.def_flip_hangup_call_default);
              	loadIntegerSetting(stmt, CyeeSettings.FLIP_ANSWER_CALL_SWITCH,
              			R.integer.def_flip_answer_call_default);
              //}
              loadBooleanSetting(stmt, CyeeSettings.Button_Light_State,
              		R.bool.def_button_light_state_default);
              
              //if (SystemProperties.get("ro.gn.networkalert.support", "yes").equals("yes")) {
              	loadBooleanSetting(stmt, CyeeSettings.WIFI_AUTO_NOTIFY,
              			R.bool.def_wifi_auto_notify_default);
              //}
              loadBooleanSetting(stmt, CyeeSettings.ALIGN_WAKE,
              		R.bool.def_align_wake_default);
              
              loadIntegerSetting(stmt, CyeeSettings.SCREEN_OFF_TIMEOUT_BACK_UP,
              		R.integer.def_screen_off_timeout);		
              
              //if (SystemProperties.get("ro.gn.mms.alertMissMsg").equals("yes")) {
              	loadBooleanSetting(stmt, CyeeSettings.ALERT_MISS_MSG, R.bool.gn_def_alert_miss_msg);
              	loadIntegerSetting(stmt, CyeeSettings.ALERT_MISS_MSG_INTERVAL, R.integer.gn_def_alert_miss_msg_interval);
              //}
              
              loadBooleanSetting(stmt, CyeeSettings.AUTO_LCM_ACL,R.bool.def_auto_lcm_acl);
              
              loadStringSetting(stmt, CyeeSettings.ALARM_RING,R.string.def_alarm_ring);
              
              loadStringSetting(stmt, CyeeSettings.RINGTONE2,R.string.def_ringtone2);
              loadStringSetting(stmt, CyeeSettings.MMS,R.string.def_mms);
              loadStringSetting(stmt, CyeeSettings.MMS2,R.string.def_mms2);
              
              loadStringSetting(stmt, CyeeSettings.THEME_PACKEAGE_NAME,R.string.def_theme_package_name); 
              loadIntegerSetting(stmt, CyeeSettings.THEME_CHECK_PACKEAGE,R.integer.def_theme_check_package); 	  
              
              //if (SystemProperties.get("ro.gn.suspendbutton.support").equals("yes")) {
              	loadBooleanSetting(stmt, CyeeSettings.SUSPEND_BUTTON,R.bool.def_suspend_button);
              //}
              
              //if (SystemProperties.get("ro.gn.single.hand.support").equals("yes")) {
              	loadBooleanSetting(stmt, CyeeSettings.PHONE_KEYBOARD,R.bool.def_phone_keyboard);
              	loadBooleanSetting(stmt, CyeeSettings.INPUT_METHOD_KEYBOARD,R.bool.def_input_method_keyboard);
              	loadBooleanSetting(stmt, CyeeSettings.PATTERN_UNLOCKSCREEN,R.bool.def_pattern_unlockscreen);
              	loadBooleanSetting(stmt, CyeeSettings.SMALL_SCREEN_MODE,R.bool.def_small_screen_mode);
              	loadBooleanSetting(stmt, CyeeSettings.SCREEN_SIZE,R.bool.def_screen_size);
              	loadBooleanSetting(stmt, "phone_keyboard_place"/*CyeeSettings.PHONE_KEYBOARD_PLACE*/,R.bool.def_phone_keyboard);
              //}
				
				  
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }

			
            copySettingsDB(db);

            upgradeVersion = 4;
        }
        //Gionee <wangguojing> <2013-12-05> add for CR00966365 end
        
        //Gionee <wangguojing> <2013-12-23> add for CR00989404 begin
        if (upgradeVersion == 4) {
            // to do ..
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadStringSetting(stmt, CyeeSettings.VIRTUAL_DISPLAY_DEVICES,R.string.def_virtual_display_devices);
                //Gionee <wangguojing> <2013-12-23> add for CR00989512 begin
                loadBooleanSetting(stmt, CyeeSettings.INTERNATIONAL_ROAMING_SWITCH,
                        R.bool.def_international_roaming_switch);
                //Gionee <wangguojing> <2013-12-23> add for CR00989512 end
                //Gionee <wangguojing> <2013-12-26> add for CR00992408 begin
				//Chenyee  ningtao 20180625 modify for CSW1803A-620 begin
                loadIntegerSetting(stmt, CyeeSettings.LCD_COLOR_VALUE,
                        mNewLcdEffectSupport ? R.integer.def_lcd_new_color_value : R.integer.def_lcd_color_value);
				//Chenyee ningtao  20180625 modify for CSW1803A-620 end
                //Gionee <wangguojing> <2013-12-26> add for CR00992408 end
                //Gionee <wangguojing> <2013-12-27> add for CR00993259 begin
                loadBooleanSetting(stmt, CyeeSettings.ROTATE_CAMERA_OPEN_APP,
                        R.bool.def_rotate_camera_open_app);
                loadBooleanSetting(stmt, CyeeSettings.USER_EXPERIENCE,
                        R.bool.def_user_experience);
                //Gionee <wangguojing> <2013-12-27> add for CR00993259 end
                //Gionee <wangguojing> <2013-12-30> add for CR00997383 begin
                loadIntegerSetting(stmt, CyeeSettings.DIAl_SOUND_TYPE,
                        R.integer.def_dial_sound_type);
                //Gionee <wangguojing> <2013-12-30> add for CR00997383 end
                
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 5;
        }
        //Gionee <wangguojing> <2013-12-23> add for CR00989404 end

        //Gionee <wangguojing> <2014-01-17> add for CR01025430 begin
        if (upgradeVersion == 5) {
            // to do ..
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
				loadBooleanSetting(stmt, CyeeSettings.LCD_EFFECT_MODE,R.bool.def_lcd_effect_adjust);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 6;
        }
        //Gionee <wangguojing> <2014-01-17> add for CR01025430 end

        //Gionee <wangguojing> <2014-02-11> add for CR01031420 begin
        if (upgradeVersion == 6) {
            // to do ..
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, CyeeSettings.DIAl_SOUND_TYPE, R.integer.def_dial_sound_type);
                loadStringSetting(stmt, CyeeSettings.VIRTUAL_DISPLAY_DEVICES,R.string.def_virtual_display_devices);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 7;
        }
        //Gionee <wangguojing> <2014-02-11> add for CR01031420 end

        if (upgradeVersion == 7) {
        	
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                //Gionee <huangyuncai> <2014-03-04> add for CR01087530 begin
                loadIntegerSetting(stmt, NOTIFICATION_DISABLE_REMINDER, R.integer.def_notification_disable_reminder);
                loadIntegerSetting(stmt, NETWORK_SPEED_DISPLAY, R.integer.def_network_speed_display);
                //Gionee <huangyuncai> <2014-03-04> add for CR01087530 end
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 8;
        }
            
        if (upgradeVersion == 8) {
            
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                //  Gionee <chenml> <2014-05-04> add for CR01215994 begin
                loadBooleanSetting(stmt, CyeeSettings.SILENT_MODE_ENABLED,R.bool.def_silent_mode);
                loadIntegerSetting(stmt, CyeeSettings.LAST_MUSIC_VOLUME,R.integer.def_last_volume);
                loadIntegerSetting(stmt, CyeeSettings.VOLUME_MUSIC,R.integer.def_volume_music);
              //  Gionee <chenml> <2014-05-04> add for CR01215994 end
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 9;
        }
        //Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
        if (upgradeVersion == 9) {
        
            updateDBValue(db);
            upgradeVersion = 10;
        }
        //Gionee <wangguojing> <2014-05-12> add for CR01237681 end
        
        //Gionee <wangguojing> <2014-05-19> add for CR01257437 begin
        if (upgradeVersion == 10) {
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, "adjust_screen_tone"/*CyeeSettings.ADJUST_SCREEN_TONE*/,
                        R.bool.def_adjust_screen_tone);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 11;
        }
        //Gionee <wangguojing> <2014-05-19> add for CR01257437 end
        
        //Gionee <wangguojing> <2014-05-29> add for CR01272670 begin
        if (upgradeVersion == 11) {
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, "user_experience_remind"/*CyeeSettings.USER_EXPERIENCE_REMIND*/,
                        R.bool.def_user_experience_remind);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 12;
        }
        //Gionee <wangguojing> <2014-05-29> add for CR01272670 end

        //Gionee <chenml> <2014-06-04> add for CR01274386 begin
        if (upgradeVersion == 12) {
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_LOW_POWER,
                        R.bool.def_respirationlamp_low_power);
                loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_IN_CHARGE,
                        R.bool.def_respirationlamp_in_charge);          
                loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_NOTIFICATION,
                        R.bool.def_respirationlamp_notification);
                loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_MUSIC,
                        R.bool.def_respirationlamp_music);
                loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_CALL,
                        R.bool.def_respirationlamp_call);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 13;
        }
        //Gionee <chenml> <2014-06-04> add for CR01274386 end
        
        //Gionee <chenml> <2014-06-18> add for CR01272876 begin
        if(upgradeVersion == 13){
            
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                afreshDBvalue(stmt,db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 14;
        }
        //Gionee <chenml> <2014-06-18> add for CR01272876 end
        //Gionee <wangguojing> <2014-09-18> add for CR01386770 begin
        if(upgradeVersion == 14){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, /*CyeeSettings.AAL_CABC_BACKUP*/"aal_cabc_backup",
                        R.bool.def_aal_cabc_backup);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 15;
        }
        //Gionee <wangguojing> <2014-09-18> add for CR01386770 end
        
        //Gionee <chenml> <2014-09-25> add for CR01387135 begin
        if(upgradeVersion == 15){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadStringSetting(stmt, /*CyeeSettings.KEY_RINGTONE_DATA*/"key_ringtone_data",
                        R.string.def_key_ringtone_data);
                loadStringSetting(stmt, /*CyeeSettings.KEY_RINGTONE2_DATA*/"key_ringtone2_data",
                        R.string.def_key_ringtone2_data);
                loadStringSetting(stmt, /*CyeeSettings.KEY_NOTIFICATION_DATA*/"key_notification_data",
                        R.string.def_key_notification_data);
                loadStringSetting(stmt, /*CyeeSettings.KEY_MMS_DATA*/"key_mms_data",
                        R.string.def_key_mms_data);
                loadStringSetting(stmt, /*CyeeSettings.KEY_MMS2_DATA*/"key_mms2_data",
                        R.string.def_key_mms2_data);
                loadStringSetting(stmt, /*CyeeSettings.KEY_VIDEO_DATA*/"key_video_data",
                        R.string.def_key_video_data);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 16;
        }
        //Gionee <chenml> <2014-09-25> add for CR01387135 end
        //Gionee <chenml> <2014-09-27> add for CR01390965 begin
        if(upgradeVersion == 16){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_SWITCH,
                    R.bool.def_sound_control_switch);
                loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_CALLING,
                    R.bool.def_sound_control_calling);
                loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_MESSAGE,
                    R.bool.def_sound_control_message);
                loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_LOCKSCREEN,
                    R.bool.def_sound_control_lockscreen);
                loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_ALARMCLOCK,
                    R.bool.def_sound_control_alarmclock);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 17;
        }
        //Gionee <chenml> <2014-09-27> add for CR01390965 end
        if(upgradeVersion == 17){
            
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, /*CyeeSettings.IS_NEW_SSG*/"is_new_ssg",
                    R.integer.def_is_new_ssg);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 18;
        }
        // Gionee <chenml> <2014-09-29> modify for CR001392602 begin
        if(upgradeVersion == 18){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, /*CyeeSettings.IS_PSENSOR_BROWSE_PICTURE_DG*/
                        "is_psensor_browse_picture_dg", R.integer.def_is_psensor_browse_dg);
                loadIntegerSetting(stmt, /*CyeeSettings.IS_PSENSOR_CTRL_VIDEO_DG*/"is_psensor_ctrl_video_dg",
                        R.integer.def_is_psensor_ctrl_video_dg);
                
                loadIntegerSetting(stmt, /* CyeeSettings.SDG_BROWSE_PHOTOS_PSENSOR*/
                        "sdg_browse_photos_psensor", R.integer.def_sdg_browse_photos_psensor);
                loadIntegerSetting(stmt, /*CyeeSettings.SDG_BROWSE_PHOTOS_SHOW_PSENSOR*/
                        "sdg_browse_photos_show_psensor", R.integer.def_sdg_browse_photos_show_psensor);
                loadIntegerSetting(stmt,/*CyeeSettings.SDG_VIDEO_PAUSE_psensor*/
                        "sdg_video_pause_psensor", R.integer.def_sdg_video_pause_psensor);
                loadIntegerSetting(stmt,  /*CyeeSettings.SDG_VIDEO_PAUSE_SHOW_psensor*/
                        "sdg_video_pause_show_psensor", R.integer.def_sdg_video_pause_show_psensor);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 19;
        }
        // Gionee <chenml> <2014-09-29> modify for CR001392602 end
        //Gionee <wangguojing> <2014-10-17> add for CR01397189 begin
        if(upgradeVersion == 19){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, /*CyeeSettings.IS_VIBRATION_ALERT_ON*/
                        "is_vibration_alert_on", R.integer.def_is_vibration_alert_on);
                
                loadIntegerSetting(stmt, /* CyeeSettings.SSG_VIBRATION_ALERT*/
                        "ssg_vibration_alert", R.integer.def_ssg_vibration_alert);
                loadIntegerSetting(stmt, /*CyeeSettings.SSG_VIBRATION_ALERT_SHOW*/
                        "ssg_vibration_alert_show", R.integer.def_ssg_vibration_alert_show);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 20;
        }
        //Gionee <wangguojing> <2014-10-17> add for CR01397189 end
		
        //Gionee <wangguojing> <2014-10-28> add for CR01403182 begin
        if(upgradeVersion == 20){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                loadIntegerSetting(stmt, /* CyeeSettings.VIRTUAL_KEY_VALUE*/
                        "virtual_key_value", R.integer.def_virtual_key_value);
                
                //Gionee <wangguojing> <2014-11-04> add for CR01406835 begin
                loadStringSetting(stmt, "black_gesture_e"/*CyeeSettings.BLACK_GESTURE_E*/,
                	R.string.def_black_gesture_e);
                loadStringSetting(stmt, "black_gesture_u_right"/*CyeeSettings.BLACK_GESTURE_U_RIGHT*/
                	,R.string.def_black_gesture_u_right);
                loadStringSetting(stmt, "black_gesture_m"/*CyeeSettings.BLACK_GESTURE_M*/,
                	R.string.def_black_gesture_m);
                loadStringSetting(stmt, "black_gesture_triangular"/*CyeeSettings.BLACK_GESTURE_TRIANGULAR*/
                	,R.string.def_black_gesture_triangular);
                loadStringSetting(stmt, "black_gesture_up_slide"/*CyeeSettings.BLACK_GESTURE_UP_SLIDE*/,
                	R.string.def_black_gesture_up_slide);
                loadStringSetting(stmt, "black_gesture_down_slide"/*CyeeSettings.BLACK_GESTURE_DOWN_SLIDE*/
                	,R.string.def_black_gesture_down_slide);
                loadStringSetting(stmt, "black_gesture_w"/*CyeeSettings.BLACK_GESTURE_W*/,
                	R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_z"/*CyeeSettings.BLACK_GESTURE_Z*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_o"/*CyeeSettings.BLACK_GESTURE_O*/,
                	R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_s"/*CyeeSettings.BLACK_GESTURE_S*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_u_left"/*CyeeSettings.BLACK_GESTURE_U_LEFT*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_u_up"/*CyeeSettings.BLACK_GESTURE_U_UP*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_u_down"/*CyeeSettings.BLACK_GESTURE_U_DOWN*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_left_slide"/*CyeeSettings.BLACK_GESTURE_LEFT_SLIDE*/
                	,R.string.def_black_gesture_default);
                loadStringSetting(stmt, "black_gesture_right_slide"/*CyeeSettings.BLACK_GESTURE_RIGHT_SLIDE*/
                	,R.string.def_black_gesture_default);
                loadBooleanSetting(stmt, /* CyeeSettings.IS_NEW_BLACK_GESTURE_ON*/
                        "is_new_black_gesture_on", R.bool.def_is_new_black_gesture_on);
                
                loadBooleanSetting(stmt, "ssg_smart_light_screen" /*CyeeSettings.SSG_SMART_LIGHT_SCREEN*/,
                	R.bool.def_ssg_smart_light_screen);
                loadBooleanSetting(stmt, "ssg_smart_light_screen_show" /*CyeeSettings.SSG_SMART_LIGHT_SCREEN_SHOW*/,
                	R.bool.def_ssg_smart_light_screen_show);
                loadBooleanSetting(stmt, /* CyeeSettings.IS_SMART_LIGHT_SCREEN_ON*/
                        "is_smart_light_screen_on", R.bool.def_is_smart_light_screen_on);
                //Gionee <wangguojing> <2014-11-04> add for CR01406835 end
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 21;
        }
        //Gionee <wangguojing> <2014-10-28> add for CR01403182 end
        //Gionee <wangguojing> <2014-11-17> add for CR01412715 begin
        if(upgradeVersion == 21){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                loadBooleanSetting(stmt, "sdg_light_screen" /*CyeeSettings.SDG_LIGHT_SCREEN*/,
                	R.bool.def_sdg_light_screen);
                loadBooleanSetting(stmt, "sdg_light_screen_show" /*CyeeSettings.SDG_LIGHT_SCREEN_SHOW*/,
                	R.bool.def_sdg_light_screen_show);
                loadBooleanSetting(stmt, /* CyeeSettings.IS_SDG_LIGHT_SCREEN_ON*/
                        "is_sdg_light_screen_on", R.bool.def_is_sdg_light_screen_on);


                loadBooleanSetting(stmt, "sdg_unlock" /*CyeeSettings.SDG_UNLOCK*/,
                	R.bool.def_sdg_unlock);
                loadBooleanSetting(stmt, "sdg_unlock_show" /*CyeeSettings.SDG_UNLOCK_SHOW*/,
                	R.bool.def_sdg_unlock_show);
                loadBooleanSetting(stmt, /* CyeeSettings.IS_SDG_UNLOCK_ON*/
                        "is_sdg_unlock_on", R.bool.def_is_sdg_unlock_on);


                loadBooleanSetting(stmt, "sdg_desktop_slide" /*CyeeSettings.SDG_DESKTOP_SLIDE*/,
                	R.bool.def_sdg_desktop_slide);
                loadBooleanSetting(stmt, "sdg_desktop_slide_show" /*CyeeSettings.SDG_DESKTOP_SLIDE_SHOW*/,
                	R.bool.def_sdg_desktop_slide_show);
                loadBooleanSetting(stmt, /* CyeeSettings.IS_SDG_DESKTOP_SLIDE_ON*/
                        "is_sdg_desktop_slide_on", R.bool.def_is_sdg_desktop_slide_on);


                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 22;
        }
        //Gionee <wangguojing> <2014-11-17> add for CR01412715 end
        //Gionee <wangguojing> <2014-12-01> modify for CR01418765 begin
        if(upgradeVersion == 22){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
				loadBooleanSetting(stmt, "cyee_silent_switch"/*CyeeSettings.CYEE_SILENT_SWITCH*/,
						R.bool.def_cyee_silent_switch);
				loadBooleanSetting(stmt, "cyee_vibration_switch"/*CyeeSettings.CYEE_VIBRATION_SWITCH*/,
						R.bool.def_cyee_vibration_switch);
				
				loadBooleanSetting(stmt, CyeeSettings.LOCKSCREEN_ACCESS,
						R.bool.def_lockscreen_access);
				loadBooleanSetting(stmt, CyeeSettings.APPLICATIONS_ACCESS,
						R.bool.def_applications_access);

				loadIntegerSetting(stmt, /*CyeeSettings.BLACK_GESTURE_CONFIG_VALUE*/
						"black_gesture_config_value", R.integer.def_black_gesture_config_value);

                loadBooleanSetting(stmt, /*CyeeSettings.TIMING_ZEN_MODE*/"timing_zen_mode"
                        ,R.bool.def_timing_zen_mode);
                loadStringSetting(stmt, /*CyeeSettings.ZEN_MODE_DAYS*/"zen_mode_days"
                        ,R.string.def_zen_mode_days);
                loadIntegerSetting(stmt, /*CyeeSettings.ZEN_MODE_CONDITION_INDEX*/"zen_mode_condition_index"
                        ,R.integer.def_zen_mode_condition_index);
				
                loadBooleanSetting(stmt,/*CyeeSettings.CONTROL_CENTER_SWITCH*/"control_center_switch",
                        R.bool.def_control_center_switch);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 23;
        }
        //Gionee <wangguojing> <2014-12-01> modify for CR01418765 end
        if(upgradeVersion == 23){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                loadBooleanSetting(stmt,/*CyeeSettings.SKYLIGHT_SWITCH*/"skylight_switch",
                        R.bool.def_skylight_switch);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 24;
        }
        if(upgradeVersion == 24){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
               loadBooleanSetting(stmt, "sdg_switch_music" /*CyeeSettings.SDG_SWITCH_MUSIC*/,
                       R.bool.def_sdg_switch_music);
               loadBooleanSetting(stmt, "is_sdg_switch_music_on"/* CyeeSettings.IS_SDG_DESKTOP_SLIDE_ON*/, 
                       R.bool.def_is_sdg_switch_music_on);
               
               loadBooleanSetting(stmt, "sdg_reader" /*CyeeSettings.SDG_READER*/,
                       R.bool.def_sdg_reader);
               loadBooleanSetting(stmt, "is_sdg_reader_on"/* CyeeSettings.IS_SDG_READER_ON*/, 
                       R.bool.def_is_sdg_reader_on);

               loadBooleanSetting(stmt, "blackgesture_support_doublefinger"/* CyeeSettings.BLACKGESTURE_SUPPORT_DOUBLEFINGER*/, 
                       R.bool.def_blackgesture_support_doublefinger);

               loadBooleanSetting(stmt, "is_fast_camera_on"/* CyeeSettings.IS_FAST_CAMERA_ON*/, 
                       R.bool.def_is_fast_camera_on);

               loadIntegerSetting(stmt, "low_battery_alert_value"/* CyeeSettings.LOW_BATTERY_ALERT_VALUE*/,
                       R.integer.def_low_battery_alert_value);

               loadBooleanSetting(stmt, "is_glove_patterns_on"/* CyeeSettings.IS_GLOVE_PATTERNS_ON*/, 
                       R.bool.def_is_glove_patterns_on);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 25;
        }
        if(upgradeVersion == 25){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                loadBooleanSetting(stmt, "is_mtk_ipo_on"/* CyeeSettings.IS_MTK_IPO_ON*/, 
                        R.bool.def_is_mtk_ipo_on);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 26;
        }
        //Gionee <wangguojing> <2015-06-15> add for CR01501989 begin
        if(upgradeVersion == 26){
        
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                
                updateIPOValue(db);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 27;
        }
        //Gionee <wangguojing> <2015-06-15> add for CR01501989 end
        //Gionee <wangguojing> <2015-06-15> add for CR01501989 begin
        if(upgradeVersion == 27){
        
            //Gionee <lizhipeng> <2015-07-11> add for CR01518122 begin
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                initCustomGestureData(stmt);
                loadBooleanSetting(stmt, "is_vibration_effect_on"/* CyeeSettings.IS_VIBRATION_EFFECT_ON*/, 
                        R.bool.def_is_vibration_effect_on);
                loadStringSetting(stmt, "vibration_effect_name"/* CyeeSettings.VIBRATION_EFFECT_NAME*/, 
                        R.string.def_vibration_effect_name);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            //Gionee <lizhipeng> <2015-07-11> add for CR01518122 end
            upgradeVersion = 28;
        }
        //Gionee <wangguojing> <2015-06-15> add for CR01501989 end
        
        //Gionee <chenml> <2015-07-24> add for CR01526572 begin
        if(upgradeVersion == 28){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadBooleanSetting(stmt, "is_remove_wifi_display"/* CyeeSettings.IS_REMOVE_WIFI_DISPLAY*/, 
                        R.bool.def_is_remove_wifi_display);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 29;
        }
        //Gionee <chenml> <2015-07-24> add for CR01526572 begin
        
        //Gionee <lizhipeng> <2015-07-29> add for CR01527696 begin
        if(upgradeVersion == 29){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadBooleanSetting(stmt,"8605and8609_gesture_switch",
                        R.bool.def_8605_gesture_switch);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 30;
        }
        //Gionee <lizhipeng> <2015-07-29> add for CR01527696 end

        //Gionee <wangguojing> <2015-08-03> add for CR01511884 begin
        if(upgradeVersion == 30){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadStringSetting(stmt, "wirte_diag_config"/* CyeeSettings.WIRTE_DIAG_CONFIG*/, 
                        R.string.def_wirte_diag_config);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 31;
        }
        //Gionee <wangguojing> <2015-08-03> add for CR01511884 end
        
        //Gionee <chenml> <2015-08-19> add for CR01540669 begin
        if(upgradeVersion == 31){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadStringSetting(stmt, "tranferdata_progress"/* CyeeSettings.TRANFERDATA_PROGRESS*/, 
                        R.string.def_tranferdata_progress);
                loadBooleanSetting(stmt, "is_exsit_tranferdata"/* CyeeSettings.IS_EXSIT_TRANFERDATA*/, 
                        R.bool.def_is_exsit_tranferdata);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 32;
        }
        //Gionee <chenml> <2015-08-19> add for CR01540669 end
        //Gionee <wangguojing> <2015-08-28> add for CR01542795 begin
        if(upgradeVersion == 32){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                updateDiagConfigValue(db);
              //Gionee <lizhipeng> <2015-08-28> add for CR01545605 begin
                updateConfigValue(db);
              //Gionee <lizhipeng> <2015-08-28> add for CR01545605 end
                updateGlovePatternsSwitchValue(db);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 33;
        }
        //Gionee <wangguojing> <2015-08-28> add for CR01542795 end
        
        //Gionee <chenml> <2015-08-31> add for CR01546929 begin
        if(upgradeVersion == 33){

        	SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
                
                loadBooleanSetting(stmt, "is_exist_private_space"/* CyeeSettings.IS_EXIST_PRIVATE_SPACE*/, 
                        R.bool.def_is_exist_private_space);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 34;
        }
        //Gionee <chenml> <2015-08-19> add for CR01546929 end
        
        //Gionee <lizhipeng> <2015-09-06> add for CR01549142 begin
        if(upgradeVersion == 34){

        	SQLiteStatement stmt = null;
            try {
            	 db.beginTransaction();
                 stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");
                 updateConfigValue(db,"black_gesture_u_right");
               //Gionee <lizhipeng> <2015-09-06> add for CR01548979 begin
                 updateConfigValue(db,"black_gesture_e");
                 updateConfigValue(db,"black_gesture_m");
               //Gionee <lizhipeng> <2015-09-06> add for CR01548979 begin
                 db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 35;
        }
        //Gionee <lizhipeng> <2015-09-06> add for CR01549142 end
        
      //Gionee <lizhipeng> <2015-09-11> add for CR01549480 begin
        if(upgradeVersion == 35){

        	SQLiteStatement stmt = null;
            try {
            	 db.beginTransaction();
                 stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");              
                 updateLcmAclValue();              
                 db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 36;
        }
      //Gionee <lizhipeng> <2015-09-11> add for CR01549480 end 
        
        //Gionee <chenml> <2015-09-18> add for 111 begin
        if(upgradeVersion == 36){

        	SQLiteStatement stmt = null;
            try {
            	 db.beginTransaction();
                 stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");          
                 
          loadBooleanSetting(stmt, "is_exist_default_storage",R.bool.def_is_exist_default_storage);       
          loadBooleanSetting(stmt, "is_exist_home_key_settings",R.bool.def_is_exist_home_key_settings);       
          loadIntegerSetting(stmt, "home_key_value", R.integer.def_home_key_value);
          
                 db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 37;
        }
        //Gionee <chenml> <2015-09-18> add for 111 begin home_key_value
        
        if(upgradeVersion == 37){
        
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");          
                
                loadBooleanSetting(stmt, "fingerprint_is_front",R.bool.def_fingerprint_is_front);       
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 38;
        }
        //Gionee <lizhipeng> <2015-09-23> modify for CR01556254 begin
        if(upgradeVersion == 38){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                     + " VALUES(?,?);");          
                
                loadBooleanSetting(stmt, CyeeSettings.GUEST_MODE,
      				  R.bool.def_guest_mode);
                loadBooleanSetting(stmt, "is_screen_saving_on",R.bool.def_is_screen_saving_on); 	  
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 39;
        }
      //Gionee <lizhipeng> <2015-09-23> modify for CR01556254 end

        //Gionee <wangguojing> <2015-10-21> add for CR01572592 begin
        if(upgradeVersion == 39){
        
            upgradeVersion = 40;
        }
        //Gionee <wangguojing> <2015-10-21> add for CR01572592 end
        
        //Gionee <lizhipneg> <2015-10-21> add for CR01556254 begin
        if(upgradeVersion == 40){
        	 SQLiteStatement stmt = null;
             try {
                 db.beginTransaction();
                 stmt = db.compileStatement("REPLACE INTO config(name,value)"
                    + " VALUES(?,?);");
                 loadBooleanSetting(stmt, CyeeSettings.GUEST_MODE,
     				  R.bool.def_guest_mode);
                 db.setTransactionSuccessful();
             } finally {
                 db.endTransaction();
                 if (stmt != null) stmt.close();
             }
            upgradeVersion = 41;
        }
      //Gionee <lizhipneg> <2015-10-21> add for CR01556254 end
        if(upgradeVersion == 41){
        
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                updateConfigValue(db,"is_exsit_tranferdata");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 42;
        }
        if(upgradeVersion == 42){
        
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadBooleanSetting(stmt, "zenmode_allow_important_notice",R.bool.def_zenmode_allow_important_notice);		 
                loadBooleanSetting(stmt, "zenmode_public_holidays_enable",R.bool.def_zenmode_public_holidays_enable);		 
                loadStringSetting(stmt, "zenmode_public_holidays_info",R.string.def_zenmode_public_holidays_info); 	  
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 43;
        }
      //Gionee <lizhipneg> <2015-12-1> add for CR01602143 begin
        if(upgradeVersion == 43){
            loadForceTouchSettings(db);
            upgradeVersion = 44;
        }
      //Gionee <lizhipneg> <2015-12-1> add for CR01602143 end
        
        //Gionee <lizhipneg> <2015-12-1> add for CR01602143 begin
        if(upgradeVersion == 44){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "multitask_display_style", R.integer.def_multitask_display_style);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 45;
        }
      //Gionee <lizhipneg> <2015-12-1> add for CR01602143 end
        
      //Gionee <lizhipeng> <2016-01-15> modify for CR01624293 begin
        if(upgradeVersion == 45){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "display_wechat_avatar", R.integer.def_display_wechat_avatar);
                loadIntegerSetting(stmt, "is_exist_left_right_menu", R.integer.def_is_exist_left_right_menu);
                loadIntegerSetting(stmt, "menu_location_state", R.integer.def_is_exist_left_right_menu);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 46;
        }
      //Gionee <lizhipeng> <2016-01-15> modify for CR01624293 end
        
        //Gionee <lizhipeng> <2016-01-28> modify for CR01632987 begin
        if(upgradeVersion == 46){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "force_touch_side_position", R.integer.def_force_touch_side_position);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 47;
        }
      //Gionee <lizhipeng> <2016-01-28> modify for CR01632987 end
        
      //Gionee <lizhipeng> <2016-01-28> modify for CR01637370 begin
        if(upgradeVersion == 47){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "node_type_left_hand_mode", R.integer.def_node_type_left_hand_mode);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 48;
        }
      //Gionee <lizhipeng> <2016-01-28> modify for CR01637370 end
        if(upgradeVersion == 48){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "is_video_dg_on",  R.integer.def_is_video_dg_on);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 49;
        }
        
        if(upgradeVersion == 49){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "safeos_pay_protect_switch",  R.integer.def_safeos_protect_switch);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 50;
        }
        
      //Gionee <lizhipeng> <2016-04-13> modify for CR01675981 begin
        if(upgradeVersion == 50){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "home_key_display_switch",  R.integer.def_home_key_display_switch);
                loadIntegerSetting(stmt, "support_split_screen",  R.integer.def_support_split_screen);
                loadStringSetting(stmt, "split_screen_switch",  R.integer.def_split_screen_switch);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 51;
        }
        if(upgradeVersion == 51){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "app_lock_setting",  R.integer.def_app_lock_setting);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 52;
        }
        
        if(upgradeVersion == 52){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadIntegerSetting(stmt, "simulation_location_switch",  R.integer.def_simulation_location_switch);
                loadStringSetting(stmt, "simulation_location_address",  R.string.def_simulation_location_address);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 53;
        }
      //Gionee <lizhipeng> <2016-04-13> modify for CR01675981 end

        // Gionee <liuyb> <2016-05-16> add for CR01700399 begin
        if(upgradeVersion == 53){
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, "show_battery_size", R.bool.def_show_battery_size);
                loadStringSetting(stmt, "devices_battery_size", R.string.def_devices_battery_size);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 54;
        }
        // Gionee <liuyb> <2016-05-16> add for CR01700399 end
        
        // Gionee <wangguojing> <2016-06-06> add for CR01714531 begin
        if(upgradeVersion == 54){
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, "is_exist_voice_call_auot_mode", R.bool.def_is_exist_voice_call_auot_mode);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 55;
        }
        // Gionee <wangguojing> <2016-06-06> add for CR01714531 end
        
        if(upgradeVersion == 55){
            
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                loadBooleanSetting(stmt, "is_exist_otg_reverse_charging",  R.bool.def_is_exist_otg_reverse_charging);
                loadIntegerSetting(stmt, "otg_reverse_charging_state",  R.integer.def_otg_reverse_charging_state);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 56;
        }

        if (upgradeVersion == 56) {
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, "support_fingerprint_vibration", R.bool.def_support_fingerprint_vibration);
                //Gionee <GN_oversea_Bug> <lipeiming> <20170413> modify for 102958 begin
                if(gnSYflag) {
                    loadIntegerSetting(stmt, "fingerprint_vibration", R.integer.def_fingerprint_vibration_symphony);
                }else {
                    loadIntegerSetting(stmt, "fingerprint_vibration", R.integer.def_fingerprint_vibration);
                }
                //Gionee <GN_oversea_Bug> <lipeiming> <20170413> modify for 102958 end

                // Gionee <GN_Oversea_Req> <linzhicong> <20170421> add for #117613 beign
                loadIntegerSetting(stmt, "haptic_feedback_enabled", R.integer.def_haptic_feedback_enabled);
                // Gionee <GN_Oversea_Req> <linzhicong> <20170421> add for #117613 end

                loadBooleanSetting(stmt, "support_fast_charge", R.bool.def_support_fast_charge);
                loadIntegerSetting(stmt, "fast_charge", R.integer.def_fast_charge);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 57;
        }

        // Gionee <liuyb> <2016-09-01> add CR01756162 for begin
        if (upgradeVersion == 57) {
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadBooleanSetting(stmt, "smart_protect_eye_support", R.bool.def_smart_protect_eye_support);
                loadIntegerSetting(stmt, "smart_protect_eye_now_switch", R.integer.def_smart_protect_eye_now_switch);
                loadIntegerSetting(stmt, "smart_protect_eye_time_limit_switch", R.integer.def_smart_protect_eye_time_limit_switch);
                loadStringSetting(stmt, "smart_protect_eye_begin_time", R.string.def_smart_protect_eye_begin_time);
                loadStringSetting(stmt, "smart_protect_eye_end_time", R.string.def_smart_protect_eye_end_time);
                loadIntegerSetting(stmt, "smart_protect_eye_level", R.integer.def_smart_protect_eye_level);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 58;
        }
        
     // Gionee <lizhipeng> <2016-08-23> add for CR01675981 begin
        if(upgradeVersion == 58){
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, "is_support_whole_beautiface", R.integer.def_is__support_whole_beautiface);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 59;
        }
        
        if(upgradeVersion == 59){
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");
                loadIntegerSetting(stmt, "is_support_ami_nail", R.integer.def_is_support_ami_nail);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 60;
        }
        // Gionee <lizhipeng> <2016-08-23> add for CR01675981 end

        //Gionee <liuyuankun> <2016-10-27> add for emergency begin
       if (upgradeVersion == 60) {
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");          
                if (SystemProperties.get("ro.gn.switch.emergency.support", "no").equals("yes")) {
                    loadIntegerSetting(stmt, "cyee_emergency_switch", R.integer.cyee_emergency_switch_on);
                } else {
                    loadIntegerSetting(stmt, "cyee_emergency_switch", R.integer.cyee_emergency_switch_off);
                }
                loadStringSetting(stmt, "emergency_sms_content", R.string.def_emergency_sms_content);
                loadIntegerSetting(stmt, "cyee_driving_mode", R.integer.cyee_driving_mode);
                loadIntegerSetting(stmt, "cyee_driving_switch", R.integer.cyee_driving_switch);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 61;
       } 
       //Gionee <liuyuankun> <2016-10-27> add for emergency end
       //Gionee hushengsong 2016-03-10 modify for CR01650054 begin
       if (upgradeVersion == 61) {
           SQLiteStatement stmt = null;
           try {
               db.beginTransaction();
               stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                   + " VALUES(?,?);");
       		  loadIntegerSetting(stmt, "floating_touch_setting_size",
       				R.integer.def_floating_touch_setting_size);
       		  loadIntegerSetting(stmt, "floating_touch_setting_transparency",
       				R.integer.def_floating_touch_setting_transparency);
               db.setTransactionSuccessful();
           } finally {
               db.endTransaction();
               if (stmt != null) stmt.close();
           }
           upgradeVersion = 62;
      } 
  	    //Gionee hushengsong 2016-03-10 modify for CR01650054 end 

      //Chenyee chenyu  20181011 add for CSW1703CX-1299 begin
       if (upgradeVersion == 62) {
           SQLiteStatement stmt = null;
           try {
               db.beginTransaction();
               stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                   + " VALUES(?,?);");
	       loadIntegerSetting(stmt, "boot_sounds", R.integer.def_boot_sounds);
               db.setTransactionSuccessful();
           } finally {
               db.endTransaction();
               if (stmt != null) stmt.close();
           }
           upgradeVersion = 63;
      }
      //Chenyee chenyu  20181011 add for CSW1703CX-1299 end 

      //Chenyee chenyu  20181025 add for CSW1703A-3941 begin
       if (upgradeVersion == 63) {
           SQLiteStatement stmt = null;
           try {
               db.beginTransaction();
               stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");

	       if(mNewLcdEffectSupport){
		    if(!is1802Project && !is1803Project){
		        loadIntegerSetting(stmt, CyeeSettings.LCD_COLOR_VALUE, R.integer.def_lcd_new_color_value);
		    }
		}

               db.setTransactionSuccessful();
           } finally {
               db.endTransaction();
               if (stmt != null) stmt.close();
           }
           upgradeVersion = 64;
      }
      //Chenyee chenyu  20181025 add for CSW1703A-3941 end
        //Chenyee  ningtao 20181126 modify for CSW1703BA-397 begin
        if (upgradeVersion == 64) {
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");

                loadIntegerSetting(stmt, "ssg_doubleclick_lock", R.bool.def_ssg_doubleclick_lock);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 65;
        }
        //Chenyee ningtao  20181126 modify for CSW1703BA-397 end
//Chenyee  ningtao 20181212 modify for CSW1703OTA-763 begin
        if (upgradeVersion == 65) {
            SQLiteStatement stmt = null;
            try {
                db.beginTransaction();
                stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                        + " VALUES(?,?);");

                loadIntegerSetting(stmt, "ssg_doubleclick_cy_lock", R.bool.def_ssg_doubleclick_lock);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 66;
        }
//Chenyee ningtao  20181212 modify for CSW1703OTA-763 end
        // Gionee <liuyb> <2016-09-01> add CR01756162 for end
        if (upgradeVersion != currentVersion) {
            // to do ..
        }
        //Gionee <wangguojing> <2015-10-21> add for CR01572592 begin
        updateDBValueForOTA(db);
        //Gionee <wangguojing> <2015-10-21> add for CR01572592 end
        
    }
    
  //Gionee <lizhipneg> <2015-12-1> add for CR01602143 begin
    private void loadForceTouchSettings(SQLiteDatabase db){
    	updateForceTouchFilePath();
    	SQLiteStatement stmt = null;
        try {
        	int mIntensity_regulating_value[] = new int[] { -100, 0, 100 };
            db.beginTransaction();
            stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                + " VALUES(?,?);"); 
            for(int i=0;i<7;i++){
                loadSetting(stmt, "force_touch_app"+i,"");  
            }
            int mIntensity_level=mContext.getResources().getInteger(R.integer.def_force_touch_mIntensity_level);
            Log.e(TAG,"mIntensity_level="+mIntensity_level);
            int NODE_TYPE_FORCE_TOUCH_LOW_THRE_VALUSE=readGestureNodeValue(mContext, NODE_TYPE_FORCE_TOUCH_LOW_THRE,R.integer.def_force_touch_min_intensity,mIntensity_regulating_value[mIntensity_level]);
            int NODE_TYPE_FORCE_TOUCH_HIGH_THRE_VALUSE=readGestureNodeValue(mContext, NODE_TYPE_FORCE_TOUCH_HIGH_THRE,R.integer.def_force_touch_max_intensity,mIntensity_regulating_value[mIntensity_level]);
            int NODE_TYPE_FORCE_TOUCH_THRESHOLD_VALUSE=readGestureNodeValue(mContext, NODE_TYPE_FORCE_TOUCH_THRESHOLD,R.integer.def_force_touch_thrshold_intensity,0);
           
            loadBooleanSetting(stmt, "force_touch_swich", R.bool.def_force_touch_switch);
            loadBooleanSetting(stmt, "force_touch_screen_switch", R.bool.def_force_touch_screen_switch);
            loadBooleanSetting(stmt, "force_touch_open_app_switch", R.bool.def_force_touch_open_app_switch);
            loadBooleanSetting(stmt, "force_touch_functionswitch", R.bool.def_force_touch_function_switch);	  
            loadSetting(stmt, "force_touch_max_intensity", NODE_TYPE_FORCE_TOUCH_HIGH_THRE_VALUSE);
            loadSetting(stmt, "force_touch_min_intensity", NODE_TYPE_FORCE_TOUCH_LOW_THRE_VALUSE);
            loadSetting(stmt, "force_touch_trigger_intensity", NODE_TYPE_FORCE_TOUCH_THRESHOLD_VALUSE);
            loadIntegerSetting(stmt, "force_touch_side_position", R.integer.def_force_touch_side_position);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (stmt != null) stmt.close();
        }
    }
  //Gionee <lizhipneg> <2015-12-1> add for CR01602143 end

    private void loadConfigSettings(SQLiteDatabase db) {
		Log.e(TAG,"loadConfigSettings");
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO config(name,value)"
                    + " VALUES(?,?);");
            initConfigValues(stmt);
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    private void loadSetting(SQLiteStatement stmt, String key, Object value) {
		Log.e(TAG,"loadConfigSettings:" + key);
        stmt.bindString(1, key);
        stmt.bindString(2, value.toString());
        stmt.execute();
    }

    private void loadStringSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key, mContext.getResources().getString(resid));
    }

    private void loadBooleanSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key,
                mContext.getResources().getBoolean(resid) ? "1" : "0");
    }

    private void loadIntegerSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key,
                Integer.toString(mContext.getResources().getInteger(resid)));
    }

    private void loadFractionSetting(SQLiteStatement stmt, String key, int resid, int base) {
        loadSetting(stmt, key,
                Float.toString(mContext.getResources().getFraction(resid, base, base)));
    }
    
    private void initConfigValues(SQLiteStatement stmt) {
        //Gionee fengjianyi 2012-08-22 modify for CR00673800 start
        //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
            loadSetting(stmt, CyeeSettings.FLIP_SOUNDS_ENABLED, 1);
            
            loadStringSetting(stmt, CyeeSettings.FLIP_ON_SOUND,
                    R.string.zzzzz_gn_def_flip_on_sound);
            loadStringSetting(stmt, CyeeSettings.FLIP_OFF_SOUND,
                    R.string.zzzzz_gn_def_flip_off_sound);
        //}
        //Gionee fengjianyi 2012-08-22 modify for CR00673800 end
        
        //Gionee:zhang_xin 2012-12-09 add for start
        loadBooleanSetting(stmt, CyeeSettings.POWER_SAVER, 
                R.bool.def_gn_power_saver);
        //Gionee:zhang_xin 2012-12-09 add for end
        
        //Gionee <zhang_xin><2013-03-26> add for CR00788411 begin
        loadBooleanSetting(stmt, CyeeSettings.FONT_SIZE, 
                R.integer.def_gn_font_size);
        //Gionee <zhang_xin><2013-03-26> add for CR00788411 end
        
        //gionee zengxuanhui 20121022 add for CR00716758 begin
        loadBooleanSetting(stmt, CyeeSettings.HAPTIC_VIBRATION_ENABLED,
                R.bool.def_haptic_vibration);
        //gionee zengxuanhui 20121022 add for CR00716758 end
        
        //Gionee Jingjc 20121122 modify for CR00722601 begin
        loadBooleanSetting(stmt, CyeeSettings.SWITCH_VIBRATION_ENABLED,
                R.bool.def_switch_vibration);
        loadBooleanSetting(stmt, CyeeSettings.DIALPAD_VIBRATION_ENABLED,
                R.bool.def_dialpad_vibration);
        loadBooleanSetting(stmt, CyeeSettings.LOCKSCREEN_VIBRATION_ENABLED,
                R.bool.def_lockscreen_vibration);
        loadBooleanSetting(stmt, CyeeSettings.SELECTAPP_VIBRATION_ENABLED,
                R.bool.def_selectapp_vibration);
        //Gionee Jingjc 20121122 modify for CR00722601 end
        
        //Gionee <zengxuanhui> <2013-04-26> add for CR00797390 begin
        loadBooleanSetting(stmt, CyeeSettings.RING_VIBRATION_ENABLED,
                R.bool.def_ring_vibration);
        loadBooleanSetting(stmt, CyeeSettings.MMS_VIBRATION_ENABLED,
                R.bool.def_mms_vibration);
        loadBooleanSetting(stmt, CyeeSettings.NOTIFICATION_VIBRATION_ENABLED,
                R.bool.def_notification_vibration);
        //Gionee <zengxuanhui> <2013-04-26> add for CR00797390 end
        
      //  Gionee <chenml> <2014-05-04> add for CR01215994 begin
        loadBooleanSetting(stmt, CyeeSettings.SILENT_MODE_ENABLED,
                R.bool.def_silent_mode);
        loadIntegerSetting(stmt, CyeeSettings.LAST_MUSIC_VOLUME,
                R.integer.def_last_volume);
        loadIntegerSetting(stmt, CyeeSettings.VOLUME_MUSIC,
                R.integer.def_volume_music);
      //  Gionee <chenml> <2014-05-04> add for CR01215994 end
        // Gionee <wangyaohui><2013-05-30> add for CR00820909 begin
        //if (SystemProperties.get("ro.gn.fanfanwidget.support").equals("yes")) {
            loadBooleanSetting(stmt, CyeeSettings.FANFAN_WIDGET_AUTO_PUSH,
                    R.bool.def_fanfan_widget);
        //}
        // Gionee <wangyaohui><2013-05-30> add for CR00820909 end
        
        // Gionee <wangyaohui><2013-06-05> add for CR00823496 begin
        //if (SystemProperties.get("ro.gn.respirationlamp.support").equals("yes")) {
            loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_LOW_POWER,
                    R.bool.def_respirationlamp_low_power);
            loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_IN_CHARGE,
                    R.bool.def_respirationlamp_in_charge);			
            loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_NOTIFICATION,
                    R.bool.def_respirationlamp_notification);
            loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_MUSIC,
                    R.bool.def_respirationlamp_music);
            loadBooleanSetting(stmt, CyeeSettings.RESPIRATION_LAMP_CALL,
                    R.bool.def_respirationlamp_call);
        //}
        // Gionee <wangyaohui><2013-06-05> add for CR00823496 end

        // Gionee <wangguojing> <2013-08-19> add for CR00859362 begin
        //if (SystemProperties.get("ro.gn.gesture.support").equals("yes")) {
            loadBooleanSetting(stmt, "ssg_switch", //CyeeSettings.GN_SSG_SWITCH,
                    R.bool.def_ssg_switch);
            loadBooleanSetting(stmt, "dg_switch", //CyeeSettings.GN_DG_SWITCH,
                    R.bool.def_dg_switch);
            loadBooleanSetting(stmt, CyeeSettings.SSG_AUTO_DIAL,
                    R.bool.def_ssg_auto_dial);
            loadBooleanSetting(stmt, CyeeSettings.SSG_CALL_ACCESS,
                    R.bool.def_ssg_call_access);
            loadBooleanSetting(stmt, CyeeSettings.SSG_DELAY_ALARM,
                    R.bool.def_ssg_delay_alarm);
            loadBooleanSetting(stmt, CyeeSettings.SSG_SWITCH_SCREEN,
                    R.bool.def_ssg_switch_screen);
            loadBooleanSetting(stmt, CyeeSettings.SDG_CALL_ACCESS,
                    R.bool.def_sdg_call_access);
            loadBooleanSetting(stmt, CyeeSettings.SDG_BROWSE_PHOTOS,
                    R.bool.def_sdg_browse_photos);
            loadBooleanSetting(stmt, CyeeSettings.SDG_VIDEO_PROGRESS,
                    R.bool.def_sdg_video_progress);
            loadBooleanSetting(stmt, CyeeSettings.SDG_VIDEO_VOLUME,
                    R.bool.def_sdg_video_volume);
            loadBooleanSetting(stmt, CyeeSettings.SDG_VIDEO_PAUSE,
                    R.bool.def_sdg_video_pause);
            loadBooleanSetting(stmt, CyeeSettings.SSG_AUTO_DIAL_SHOW,
                    R.bool.def_ssg_auto_dial);
            loadBooleanSetting(stmt, CyeeSettings.SSG_CALL_ACCESS_SHOW,
                    R.bool.def_ssg_call_access);
            if (SystemProperties.get("ro.gn.only.disp.alarm.gesture").equals("yes")) {
                loadBooleanSetting(stmt, CyeeSettings.SSG_DELAY_ALARM_SHOW,
                        R.bool.def_ssg_delay_alarm);
            }else{
                loadBooleanSetting(stmt, CyeeSettings.SSG_DELAY_ALARM_SHOW,
                        R.bool.def_ssg_delay_alarm_show);
            }
            loadBooleanSetting(stmt, CyeeSettings.SSG_SWITCH_SCREEN_SHOW,
                    R.bool.def_ssg_switch_screen);
            loadBooleanSetting(stmt, CyeeSettings.SDG_CALL_ACCESS_SHOW,
                    R.bool.def_sdg_call_access);
            loadBooleanSetting(stmt, CyeeSettings.SDG_BROWSE_PHOTOS_SHOW,
                    R.bool.def_sdg_browse_photos_show);
            loadBooleanSetting(stmt, CyeeSettings.SDG_VIDEO_PROGRESS_SHOW,
                    R.bool.def_sdg_video_progress);
            loadBooleanSetting(stmt, CyeeSettings.SDG_VIDEO_VOLUME_SHOW,
                    R.bool.def_sdg_video_volume);
            loadBooleanSetting(stmt, CyeeSettings.SDG_VIDEO_PAUSE_SHOW,
                    R.bool.def_sdg_video_pause);
            //Gionee <wangguojing> <2013-10-11> add for CR00922210 begin
            loadBooleanSetting(stmt, CyeeSettings.SDG_DEL_PHOTOS,
                    R.bool.def_sdg_del_photos);
            loadBooleanSetting(stmt, CyeeSettings.SDG_DEL_PHOTOS_SHOW,
                    R.bool.def_sdg_del_photos);

            loadBooleanSetting(stmt, CyeeSettings.SDG_TASK_CONTROL,
                    R.bool.def_sdg_task_control);
            loadBooleanSetting(stmt, CyeeSettings.SDG_TASK_CONTROL_SHOW,
                    R.bool.def_sdg_task_control);
			
            loadBooleanSetting(stmt, CyeeSettings.SSG_DOUBLECLICK_WAKE,
                    R.bool.def_ssg_doubleclick_wake);
            loadBooleanSetting(stmt, CyeeSettings.SSG_DOUBLECLICK_WAKE_SHOW,
                    R.bool.def_ssg_doubleclick_wake_show);
            //Gionee <wangguojing> <2013-10-11> add for CR00922210 end

            loadBooleanSetting(stmt, "ssg_doubleclick_lock",
                    R.bool.def_ssg_doubleclick_lock);
			//Chenyee  ningtao 20181212 modify for CSW1703OTA-763 begin
            loadBooleanSetting(stmt, "ssg_doubleclick_cy_lock",
                R.bool.def_ssg_doubleclick_lock);
			//Chenyee ningtao  20181212 modify for CSW1703OTA-763 end
      	    // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begin
            loadBooleanSetting(stmt,SSG_QUICK_OPERATING,R.bool.def_fast_operation);
	        loadSetting(stmt,SSG_QUICK_OPERATING_UP_GESTURE_CHOICE,1);
		    loadStringSetting(stmt,SSG_QUICK_OPERATING_UP_GESTURE_APP_NAME_OR_PHONE_NUMBER,R.string.def_fast_operation_up_app);
	        loadSetting(stmt,SSG_QUICK_OPERATING_RIGHT_GESTURE_CHOICE,1);
		    loadStringSetting(stmt,SSG_QUICK_OPERATING_RIGHT_GESTURE_APP_NAME_OR_PHONE_NUMBER,R.string.def_fast_operation_right_app);
			loadSetting(stmt,SSG_QUICK_OPERATING_LEFT_GESTURE_CHOICE,0);
			loadSetting(stmt,SSG_QUICK_OPERATING_LEFT_GESTURE_APP_NAME_OR_PHONE_NUMBER,"");
			loadSetting(stmt,SSG_QUICK_OPERATING_DOWN_GESTURE_CHOICE,0);
			loadSetting(stmt,SSG_QUICK_OPERATING_DOWN_GESTURE_APP_NAME_OR_PHONE_NUMBER,"");
      	    // Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end

        //}
		
//        if (SystemProperties.get("ro.gn.soundctrl.support").equals("yes")) {
            loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_SWITCH,
                    R.bool.def_sound_control_switch);
            loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_CALLING,
                    R.bool.def_sound_control_calling);
            loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_MESSAGE,
                    R.bool.def_sound_control_message);
            loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_LOCKSCREEN,
                    R.bool.def_sound_control_lockscreen);
            loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_ALARMCLOCK,
                    R.bool.def_sound_control_alarmclock);
            //Gionee <chenml> <2013-09-25> add for CR00906622 begin
            loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_OPEN_REMIND,
					R.bool.def_sound_control_open_remind);
            loadBooleanSetting(stmt, CyeeSettings.SOUND_CONTROL_CLOSE_REMIND,
					R.bool.def_sound_control_close_remind);

            //Gionee <chenml> <2013-09-25> add for CR00906622 end
//        }
	  
	  //if (SystemProperties.get("ro.gn.guestmode.support").equals("yes")) {
		  loadBooleanSetting(stmt, CyeeSettings.GUEST_MODE,
				  R.bool.def_guest_mode);
		  loadBooleanSetting(stmt, CyeeSettings.FIRST_OPEN_GUEST_MODE,
				  R.bool.def_first_open_guest_mode);
		  loadBooleanSetting(stmt, CyeeSettings.GUEST_PASS_ENABLE,
				  R.bool.def_guest_pass_enable);
		  loadStringSetting(stmt, CyeeSettings.GUEST_PASS,
				  R.string.zzzzz_gn_def_guest_pass);
	  //}
	  // Gionee <wangguojing> <2013-08-19> add for CR00859362 end

	  //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
		  loadIntegerSetting(stmt, CyeeSettings.FLIP_HANGUP_CALL_SWITCH,
				  R.integer.def_flip_hangup_call_default);
		  loadIntegerSetting(stmt, CyeeSettings.FLIP_ANSWER_CALL_SWITCH,
				  R.integer.def_flip_answer_call_default);
	  //}
	  loadBooleanSetting(stmt, CyeeSettings.Button_Light_State,
			  R.bool.def_button_light_state_default);
	  
	  //if (SystemProperties.get("ro.gn.networkalert.support", "yes").equals("yes")) {
		  loadBooleanSetting(stmt, CyeeSettings.WIFI_AUTO_NOTIFY,
				  R.bool.def_wifi_auto_notify_default);
	  //}
	  loadBooleanSetting(stmt, CyeeSettings.ALIGN_WAKE,
			  R.bool.def_align_wake_default);
	  
	  loadIntegerSetting(stmt, CyeeSettings.SCREEN_OFF_TIMEOUT_BACK_UP,
			  R.integer.def_screen_off_timeout);	  
	  
	  //if (SystemProperties.get("ro.gn.mms.alertMissMsg").equals("yes")) {
		  loadBooleanSetting(stmt, CyeeSettings.ALERT_MISS_MSG, R.bool.gn_def_alert_miss_msg);
		  loadIntegerSetting(stmt, CyeeSettings.ALERT_MISS_MSG_INTERVAL, R.integer.gn_def_alert_miss_msg_interval);
	  //}

	  //Gionee Jingjc 2012-09-16 add for CR00693470 start
	  loadBooleanSetting(stmt, CyeeSettings.AUTO_LCM_ACL,R.bool.def_auto_lcm_acl);
	  //Gionee Jingjc 2012-09-16 add for CR00693470 end
	
	  loadStringSetting(stmt, CyeeSettings.ALARM_RING, R.string.def_alarm_ring);
	
	  loadStringSetting(stmt, CyeeSettings.RINGTONE2,R.string.def_ringtone2);
	  loadStringSetting(stmt, CyeeSettings.MMS,R.string.def_mms);
	  loadStringSetting(stmt, CyeeSettings.MMS2,R.string.def_mms2);

	  loadStringSetting(stmt, CyeeSettings.THEME_PACKEAGE_NAME,R.string.def_theme_package_name); 
	  loadIntegerSetting(stmt, CyeeSettings.THEME_CHECK_PACKEAGE,R.integer.def_theme_check_package);		
	  
	  //if (SystemProperties.get("ro.gn.suspendbutton.support").equals("yes")) {
	      loadBooleanSetting(stmt, CyeeSettings.SUSPEND_BUTTON,R.bool.def_suspend_button);
	  //}

	  //if (SystemProperties.get("ro.gn.single.hand.support").equals("yes")) {
	      loadBooleanSetting(stmt, CyeeSettings.PHONE_KEYBOARD,R.bool.def_phone_keyboard);
	      loadBooleanSetting(stmt, CyeeSettings.INPUT_METHOD_KEYBOARD,R.bool.def_input_method_keyboard);
	      loadBooleanSetting(stmt, CyeeSettings.PATTERN_UNLOCKSCREEN,R.bool.def_pattern_unlockscreen);
	      loadBooleanSetting(stmt, CyeeSettings.SMALL_SCREEN_MODE,R.bool.def_small_screen_mode);
	      loadBooleanSetting(stmt, CyeeSettings.SCREEN_SIZE,R.bool.def_screen_size);
	      loadBooleanSetting(stmt, "phone_keyboard_place"/*CyeeSettings.PHONE_KEYBOARD_PLACE*/,R.bool.def_phone_keyboard);
	  //}
	  
	  //Gionee <wangguojing> <2013-10-11> add for GPS Optimization begin
	  loadBooleanSetting(stmt, CyeeSettings.GPS_PROMPT_REMIND,R.bool.def_gps_prompt_remind);
	  //Gionee <wangguojing> <2013-10-11> add for GPS Optimization end

	  //Gionee <wangguojing> <2013-10-24> add for CR00933390 begin
	  //if (SystemProperties.get("ro.gn.floatingwindow.support").equals("yes")) {
	      loadStringSetting(stmt, CyeeSettings.VIRTUAL_DISPLAY_DEVICES,R.string.def_virtual_display_devices);
	  //}
	  //Gionee <wangguojing> <2013-10-24> add for CR00933390 end

	  //Gionee <wangguojing> <2013-10-31> add for CR00942710 begin
	  //if (SystemProperties.get("ro.gn.glove_patterns.support").equals("yes")) {
	      loadBooleanSetting(stmt, CyeeSettings.GLOVE_PATTERNS,R.bool.def_glove_patterns);
	  //}
	  //Gionee <wangguojing> <2013-10-31> add for CR00942710 end
	  
	  //Gionee <wangguojing> <2013-11-02> add for CR00943428 begin
	  //if (SystemProperties.get("ro.gn.voicewake.support").equals("yes")) {
	      loadBooleanSetting(stmt, CyeeSettings.VOICE_WAKE_SWITCH,R.bool.def_voice_wake_switch);
	      loadBooleanSetting(stmt, CyeeSettings.VOICE_WAKE_REMIND,R.bool.def_voice_wake_remind);
	      loadBooleanSetting(stmt, CyeeSettings.VOICE_WAKE_WORD,R.bool.def_voice_wake_word);
	  //}
	  //Gionee <wangguojing> <2013-11-02> add for CR00943428 end
	  
	  //Gionee <wangguojing> <2013-11-04> add for CR00942651 begin
	  //if (SystemProperties.get("ro.gn.lcd.effect.support").equals("yes")) {
	      loadBooleanSetting(stmt, CyeeSettings.LCD_EFFECT_MODE,R.bool.def_lcd_effect_adjust);
	  //}
	  //Gionee <wangguojing> <2013-11-02> add for CR00942651 end
	  
	  //Gionee <wangguojing> <2013-11-04> add for CR00944538 begin
	  loadBooleanSetting(stmt, "show_powersave_dialog"/*CyeeSettings.SHOW_POWERSAVE_DIALOG*/,R.bool.def_show_powersave_dialog);
	  //Gionee <wangguojing> <2013-11-04> add for CR00944538 end
	  //Gionee <wangguojing> <2013-12-23> add for CR00989512 begin
	  loadBooleanSetting(stmt, CyeeSettings.INTERNATIONAL_ROAMING_SWITCH,
	          R.bool.def_international_roaming_switch);
	  //Gionee <wangguojing> <2013-12-23> add for CR00989512 end
	  //Gionee <wangguojing> <2013-12-26> add for CR00992408 begin
        //Chenyee  ningtao 20180625 modify for CSW1803A-620 begin
	  loadIntegerSetting(stmt, CyeeSettings.LCD_COLOR_VALUE,
              mNewLcdEffectSupport ? R.integer.def_lcd_new_color_value : R.integer.def_lcd_color_value);
        //Chenyee ningtao  20180625 modify for CSW1803A-620 end
	  //Gionee <wangguojing> <2013-12-26> add for CR00992408 end
      //Gionee <wangguojing> <2013-12-27> add for CR00993259 begin
      loadBooleanSetting(stmt, CyeeSettings.ROTATE_CAMERA_OPEN_APP,
              R.bool.def_rotate_camera_open_app);
      loadBooleanSetting(stmt, CyeeSettings.USER_EXPERIENCE,
              R.bool.def_user_experience);
      //Gionee <wangguojing> <2013-12-27> add for CR00993259 end
      //Gionee <wangguojing> <2013-12-30> add for CR00997383 begin
      loadIntegerSetting(stmt, CyeeSettings.DIAl_SOUND_TYPE,
              R.integer.def_dial_sound_type);
      //Gionee <wangguojing> <2013-12-30> add for CR00997383 end
      //Gionee <huangyuncai> <2014-03-04> add for CR01087530 begin
      loadIntegerSetting(stmt, NOTIFICATION_DISABLE_REMINDER, R.integer.def_notification_disable_reminder);
      loadIntegerSetting(stmt, NETWORK_SPEED_DISPLAY, R.integer.def_network_speed_display);
      //Gionee <huangyuncai> <2014-03-04> add for CR01087530 end

      //Gionee <wangguojing> <2014-05-19> add for CR01257437 begin
      loadBooleanSetting(stmt, "adjust_screen_tone"/*CyeeSettings.ADJUST_SCREEN_TONE*/,
              R.bool.def_adjust_screen_tone);
      //Gionee <wangguojing> <2014-05-19> add for CR01257437 end

      //Gionee <wangguojing> <2014-05-29> add for CR01272670 begin
	  loadBooleanSetting(stmt, "user_experience_remind"/*CyeeSettings.USER_EXPERIENCE_REMIND*/,
			  R.bool.def_user_experience_remind);
      //Gionee <wangguojing> <2014-05-29> add for CR01272670 end
      //Gionee <wangguojing> <2014-09-18> add for CR01386770 begin
      loadBooleanSetting(stmt, /*CyeeSettings.AAL_CABC_BACKUP*/"aal_cabc_backup",
			  R.bool.def_aal_cabc_backup);
      //Gionee <wangguojing> <2014-05-29> add for CR01386770 end
	  
      //Gionee <chenml> <2014-09-25> add for CR01387135 begin
      loadStringSetting(stmt, /*CyeeSettings.KEY_RINGTONE_DATA*/"key_ringtone_data",
              R.string.def_key_ringtone_data);
      loadStringSetting(stmt, /*CyeeSettings.KEY_RINGTONE2_DATA*/"key_ringtone2_data",
              R.string.def_key_ringtone2_data);
      loadStringSetting(stmt, /*CyeeSettings.KEY_NOTIFICATION_DATA*/"key_notification_data",
              R.string.def_key_notification_data);
      loadStringSetting(stmt, /*CyeeSettings.KEY_MMS_DATA*/"key_mms_data",
              R.string.def_key_mms_data);
      loadStringSetting(stmt, /*CyeeSettings.KEY_MMS2_DATA*/"key_mms2_data",
              R.string.def_key_mms2_data);
      loadStringSetting(stmt, /*CyeeSettings.KEY_VIDEO_DATA*/"key_video_data",
              R.string.def_key_video_data);
      //Gionee <chenml> <2014-09-25> add for CR01387135 end
      
      // Gionee <chenml> <2014-09-29> modify for CR001392602 begin
        // Gionee <chenml> <2014-09-29> modify for CR001392602 begin
        loadIntegerSetting(stmt, /*CyeeSettings.IS_NEW_SSG*/"is_new_ssg", R.integer.def_is_new_ssg);
        // Gionee <chenml> <2014-09-29> modify for CR001392602 end
        loadIntegerSetting(stmt, /*CyeeSettings.IS_PSENSOR_BROWSE_PICTURE_DG*/
                "is_psensor_browse_picture_dg", R.integer.def_is_psensor_browse_dg);
        loadIntegerSetting(stmt, /*CyeeSettings.IS_PSENSOR_CTRL_VIDEO_DG*/"is_psensor_ctrl_video_dg",
                R.integer.def_is_psensor_ctrl_video_dg);

        loadIntegerSetting(stmt, /* CyeeSettings.SDG_BROWSE_PHOTOS_PSENSOR*/
                "sdg_browse_photos_psensor", R.integer.def_sdg_browse_photos_psensor);
        loadIntegerSetting(stmt, /*CyeeSettings.SDG_BROWSE_PHOTOS_SHOW_PSENSOR*/
                "sdg_browse_photos_show_psensor", R.integer.def_sdg_browse_photos_show_psensor);
        loadIntegerSetting(stmt,/*CyeeSettings.SDG_VIDEO_PAUSE_PSENSOR*/
                "sdg_video_pause_psensor", R.integer.def_sdg_video_pause_psensor);
        loadIntegerSetting(stmt, /*CyeeSettings.SDG_VIDEO_PAUSE_SHOW_PSENSOR*/
                "sdg_video_pause_show_psensor", R.integer.def_sdg_video_pause_show_psensor);
        // Gionee <chenml> <2014-09-29> modify for CR001392602 end
        //Gionee <wangguojing> <2014-10-17> add for CR01397189 begin
        loadIntegerSetting(stmt, /*CyeeSettings.IS_VIBRATION_ALERT_ON*/
                 "is_vibration_alert_on", R.integer.def_is_vibration_alert_on);
        
        loadIntegerSetting(stmt, /* CyeeSettings.SSG_VIBRATION_ALERT*/
                "ssg_vibration_alert", R.integer.def_ssg_vibration_alert);
        loadIntegerSetting(stmt, /*CyeeSettings.SSG_VIBRATION_ALERT_SHOW*/
                "ssg_vibration_alert_show", R.integer.def_ssg_vibration_alert_show);
        //Gionee <wangguojing> <2014-10-17> add for CR01397189 end
        //Gionee <wangguojing> <2014-10-28> add for CR01403182 begin
        loadIntegerSetting(stmt, /* CyeeSettings.VIRTUAL_KEY_VALUE*/
                "virtual_key_value", R.integer.def_virtual_key_value);
        //Gionee <wangguojing> <2014-10-28> add for CR01403182 end
        //Gionee <wangguojing> <2014-11-04> add for CR01406835 begin
        //Gionee <GN_Oversea_Bug> <fujiabing> <20170328> add for 77768 begin
        if(gnIPflag)
	{
        loadStringSetting(stmt, "black_gesture_e"/*CyeeSettings.BLACK_GESTURE_E*/,
        	R.string.def_black_gesture_e_ip);
	//Chenyee <CY_Bug> <zhang_han> <20180109> add for SW17W16WA-61 begin
        //Gionee <GN_Oversea_Req> <linzhicong> <20170517> add for #141269 beign
        }else if(sGnBluFlag || SystemProperties.get("ro.product.model").equals("Primo S6 infinity")){
            loadStringSetting(stmt, "black_gesture_e"/*CyeeSettings.BLACK_GESTURE_E*/,
                    R.string.def_black_gesture_e_blu);
        //Gionee <GN_Oversea_Req> <linzhicong> <20170517> add for #141269 end
	//Chenyee <CY_Bug> <zhang_han> <20180109> add for SW17W16WA-61 end
        //Gionee <GN_Oversea_Req> <linzhicong> <20170523> add for #147092 beign
	//Chenyee <CY_Oversea_Req> <zhaopeng> <20180423> add for CSW1705EI-64 begin
        }else if(cyEIflag){
            loadStringSetting(stmt, "black_gesture_e"/*CyeeSettings.BLACK_GESTURE_E*/,
                    R.string.def_black_gesture_e_ei);
	//Chenyee <CY_Oversea_Req> <zhaopeng> <20180423> add for CSW1705EI-64 end
        }else if(sGnQMOFlag){
            loadStringSetting(stmt, "black_gesture_e"/*CyeeSettings.BLACK_GESTURE_E*/,
                    R.string.def_black_gesture_e_qm);
        //Gionee <GN_Oversea_Req> <linzhicong> <20170523> add for #147092 end

       //Chenyee <CY_REQ> <chenyu> <20180127> add for CSW1702A-2711 begin
        }else if(cyIsGoRom && !cyTLFlag){
            loadStringSetting(stmt, "black_gesture_e"/*CyeeSettings.BLACK_GESTURE_E*/,
                    R.string.def_black_gesture_e_gorom);
       //Chenyee <CY_REQ> <chenyu> <20180127> add for CSW1702A-2711 end

        //Chenyee <CY_REQ> <chenyu> <20180510> add for AC/CSW1703A-2504 begin
        }else if(cyACFlag && gmsExpressSupport){
            loadStringSetting(stmt, "black_gesture_e"/*CyeeSettings.BLACK_GESTURE_E*/,
                    R.string.def_black_gesture_e_ac);
        //Chenyee <CY_REQ> <chenyu> <20180510> add for AC/CSW1703A-2504 end

	}else{
        loadStringSetting(stmt, "black_gesture_e"/*CyeeSettings.BLACK_GESTURE_E*/,
        	R.string.def_black_gesture_e);
	}
    	//Gionee <GN_Oversea_Bug> <fujiabing> <20170328> add for 77768 end
        loadStringSetting(stmt, "black_gesture_u_right"/*CyeeSettings.BLACK_GESTURE_U_RIGHT*/
        	,R.string.def_black_gesture_u_right);
        //Gionee <GN_Oversea_Req> <linzhicong> <20170517> add for #141269 beign
        //Gionee <GN_Oversea_Req> <zhanglz> <20170825> for 195693 begin
		Log.d("Gionee zhanglz", "sGnBluFlag && BLUMusic: sGnBluFlag " + sGnBluFlag + " BLUMusic " + BLUMusic);
        if(sGnBluFlag && BLUMusic){
            loadStringSetting(stmt, "black_gesture_m"/*CyeeSettings.BLACK_GESTURE_M*/,
                    R.string.def_black_gesture_m_blu);
        } else {
            loadStringSetting(stmt, "black_gesture_m"/*CyeeSettings.BLACK_GESTURE_M*/,
                    R.string.def_black_gesture_m);
        }
        //Gionee <GN_Oversea_Req> <zhanglz> <20170825> for 195693 end
        //Gionee <GN_Oversea_Req> <linzhicong> <20170517> add for #141269 end
        loadStringSetting(stmt, "black_gesture_triangular"/*CyeeSettings.BLACK_GESTURE_TRIANGULAR*/
        	,R.string.def_black_gesture_triangular);
        loadStringSetting(stmt, "black_gesture_up_slide"/*CyeeSettings.BLACK_GESTURE_UP_SLIDE*/,
        	R.string.def_black_gesture_up_slide);
        loadStringSetting(stmt, "black_gesture_down_slide"/*CyeeSettings.BLACK_GESTURE_DOWN_SLIDE*/
        	,R.string.def_black_gesture_down_slide);
        loadStringSetting(stmt, "black_gesture_w"/*CyeeSettings.BLACK_GESTURE_W*/,
        	R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_z"/*CyeeSettings.BLACK_GESTURE_Z*/
        	,R.string.def_black_gesture_default);
        //gionee yewq 2016-12-21 modify for 52417 begin
        if(gnTorchSupport){
            loadStringSetting(stmt, "black_gesture_o"/*CyeeSettings.BLACK_GESTURE_O*/,
                    R.string.def_black_gesture_o_torch);
        }else {
            loadStringSetting(stmt, "black_gesture_o"/*CyeeSettings.BLACK_GESTURE_O*/,
                    R.string.def_black_gesture_default);
        }
        //gionee yewq 2016-12-21 modify for 52417 end
        loadStringSetting(stmt, "black_gesture_s"/*CyeeSettings.BLACK_GESTURE_S*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_u_left"/*CyeeSettings.BLACK_GESTURE_U_LEFT*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_u_up"/*CyeeSettings.BLACK_GESTURE_U_UP*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_u_down"/*CyeeSettings.BLACK_GESTURE_U_DOWN*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_left_slide"/*CyeeSettings.BLACK_GESTURE_LEFT_SLIDE*/
        	,R.string.def_black_gesture_default);
        loadStringSetting(stmt, "black_gesture_right_slide"/*CyeeSettings.BLACK_GESTURE_RIGHT_SLIDE*/
        	,R.string.def_black_gesture_default);
        loadBooleanSetting(stmt, /* CyeeSettings.IS_NEW_BLACK_GESTURE_ON*/
        		"is_new_black_gesture_on", R.bool.def_is_new_black_gesture_on);
        
        loadBooleanSetting(stmt, "ssg_smart_light_screen" /*CyeeSettings.SSG_SMART_LIGHT_SCREEN*/,
            R.bool.def_ssg_smart_light_screen);
        loadBooleanSetting(stmt, "ssg_smart_light_screen_show" /*CyeeSettings.SSG_SMART_LIGHT_SCREEN_SHOW*/,
            R.bool.def_ssg_smart_light_screen_show);
        loadBooleanSetting(stmt, /* CyeeSettings.IS_SMART_LIGHT_SCREEN_ON*/
        		"is_smart_light_screen_on", R.bool.def_is_smart_light_screen_on);
        //Gionee <wangguojing> <2014-11-04> add for CR01406835 end
        
        //Gionee <wangguojing> <2014-11-17> add for CR01412715 begin
        loadBooleanSetting(stmt, "sdg_light_screen" /*CyeeSettings.SDG_LIGHT_SCREEN*/,
        	R.bool.def_sdg_light_screen);
        loadBooleanSetting(stmt, "sdg_light_screen_show" /*CyeeSettings.SDG_LIGHT_SCREEN_SHOW*/,
        	R.bool.def_sdg_light_screen_show);
        loadBooleanSetting(stmt, /* CyeeSettings.IS_SDG_LIGHT_SCREEN_ON*/
        		"is_sdg_light_screen_on", R.bool.def_is_sdg_light_screen_on);
        
        
        loadBooleanSetting(stmt, "sdg_unlock" /*CyeeSettings.SDG_UNLOCK*/,
        	R.bool.def_sdg_unlock);
        loadBooleanSetting(stmt, "sdg_unlock_show" /*CyeeSettings.SDG_UNLOCK_SHOW*/,
        	R.bool.def_sdg_unlock_show);
        loadBooleanSetting(stmt, /* CyeeSettings.IS_SDG_UNLOCK_ON*/
        		"is_sdg_unlock_on", R.bool.def_is_sdg_unlock_on);
        
        
        loadBooleanSetting(stmt, "sdg_desktop_slide" /*CyeeSettings.SDG_DESKTOP_SLIDE*/,
        	R.bool.def_sdg_desktop_slide);
        loadBooleanSetting(stmt, "sdg_desktop_slide_show" /*CyeeSettings.SDG_DESKTOP_SLIDE_SHOW*/,
        	R.bool.def_sdg_desktop_slide_show);
        loadBooleanSetting(stmt, /* CyeeSettings.IS_SDG_DESKTOP_SLIDE_ON*/
        		"is_sdg_desktop_slide_on", R.bool.def_is_sdg_desktop_slide_on);
        //Gionee <wangguojing> <2014-11-17> add for CR01412715 end

        loadBooleanSetting(stmt, "cyee_silent_switch"/*CyeeSettings.CYEE_SILENT_SWITCH*/,
                R.bool.def_cyee_silent_switch);
        loadBooleanSetting(stmt, "cyee_vibration_switch"/*CyeeSettings.CYEE_VIBRATION_SWITCH*/,
                R.bool.def_cyee_vibration_switch);

        loadBooleanSetting(stmt, CyeeSettings.LOCKSCREEN_ACCESS,
                R.bool.def_lockscreen_access);
        loadBooleanSetting(stmt, CyeeSettings.APPLICATIONS_ACCESS,
                R.bool.def_applications_access);

        //gionee yewq 2016-12-21 modify for 52417 begin
        if(gnTorchSupport){
            loadIntegerSetting(stmt, /*CyeeSettings.BLACK_GESTURE_CONFIG_VALUE*/
                    "black_gesture_config_value", R.integer.def_torch_support_gesture_config_value);
        }else{
            loadIntegerSetting(stmt, /*CyeeSettings.BLACK_GESTURE_CONFIG_VALUE*/
                    "black_gesture_config_value", R.integer.def_black_gesture_config_value);
        }
       //gionee yewq 2016-12-21 modify for 52417 end

        loadBooleanSetting(stmt, /*CyeeSettings.TIMING_ZEN_MODE*/"timing_zen_mode"
                ,R.bool.def_timing_zen_mode);
        loadStringSetting(stmt, /*CyeeSettings.ZEN_MODE_DAYS*/"zen_mode_days"
                ,R.string.def_zen_mode_days);
        loadIntegerSetting(stmt, /*CyeeSettings.ZEN_MODE_CONDITION_INDEX*/"zen_mode_condition_index"
                ,R.integer.def_zen_mode_condition_index);

        loadBooleanSetting(stmt,/*CyeeSettings.CONTROL_CENTER_SWITCH*/"control_center_switch",
                R.bool.def_control_center_switch);

        loadBooleanSetting(stmt,/*CyeeSettings.SKYLIGHT_SWITCH*/"skylight_switch",
                R.bool.def_skylight_switch);

        loadBooleanSetting(stmt, "sdg_switch_music" /*CyeeSettings.SDG_SWITCH_MUSIC*/,
                R.bool.def_sdg_switch_music);
        loadBooleanSetting(stmt, "is_sdg_switch_music_on"/* CyeeSettings.IS_SDG_DESKTOP_SLIDE_ON*/, 
                R.bool.def_is_sdg_switch_music_on);
        
        loadBooleanSetting(stmt, "sdg_reader" /*CyeeSettings.SDG_READER*/,
                R.bool.def_sdg_reader);
        loadBooleanSetting(stmt, "is_sdg_reader_on"/* CyeeSettings.IS_SDG_READER_ON*/, 
                R.bool.def_is_sdg_reader_on);
        loadBooleanSetting(stmt, "blackgesture_support_doublefinger"/* CyeeSettings.BLACKGESTURE_SUPPORT_DOUBLEFINGER*/, 
                R.bool.def_blackgesture_support_doublefinger);

        loadBooleanSetting(stmt, "is_fast_camera_on"/* CyeeSettings.IS_FAST_CAMERA_ON*/, 
                R.bool.def_is_fast_camera_on);
		
        loadIntegerSetting(stmt, "low_battery_alert_value"/* CyeeSettings.LOW_BATTERY_ALERT_VALUE*/,
                R.integer.def_low_battery_alert_value);

        loadBooleanSetting(stmt, "is_glove_patterns_on"/* CyeeSettings.IS_GLOVE_PATTERNS_ON*/, 
                R.bool.def_is_glove_patterns_on);

        loadBooleanSetting(stmt, "is_mtk_ipo_on"/* CyeeSettings.IS_MTK_IPO_ON*/, 
                R.bool.def_is_mtk_ipo_on);
        //Gionee <lizhipeng> <2015-07-11> add for CR01518122 begin
        initCustomGestureData(stmt);
        //Gionee <lizhipeng> <2015-07-11> add for CR01518122 end
        loadBooleanSetting(stmt, "is_vibration_effect_on"/* CyeeSettings.IS_VIBRATION_EFFECT_ON*/, 
                R.bool.def_is_vibration_effect_on);
        loadStringSetting(stmt, "vibration_effect_name"/* CyeeSettings.VIBRATION_EFFECT_NAME*/, 
                R.string.def_vibration_effect_name);
        
        //Gionee <chenml> <2015-07-24> add for CR01526572 begin      
        loadBooleanSetting(stmt, "is_remove_wifi_display"/* CyeeSettings.IS_REMOVE_WIFI_DISPLAY*/, 
                R.bool.def_is_remove_wifi_display);
        //Gionee <chenml> <2015-07-24> add for CR01526572 end 
        //Gionee <wangguojing> <2015-08-03> add for CR01511884 begin
        loadStringSetting(stmt, "wirte_diag_config"/* CyeeSettings.WIRTE_DIAG_CONFIG*/, 
                R.string.def_wirte_diag_config);
        //Gionee <wangguojing> <2015-08-03> add for CR01511884 end
        
        //Gionee <chenml> <2015-08-19> add for CR01540669 begin
        loadStringSetting(stmt, "tranferdata_progress"/* CyeeSettings.TRANFERDATA_PROGRESS*/, 
                R.string.def_tranferdata_progress);
        loadBooleanSetting(stmt, "is_exsit_tranferdata"/* CyeeSettings.IS_EXSIT_TRANFERDATA*/, 
                R.bool.def_is_exsit_tranferdata);
        //Gionee <chenml> <2015-08-19> add for CR01540669 end
       
        //Gionee <chenml> <2015-08-31> add for CR01546929 begin
        loadBooleanSetting(stmt, "is_exist_private_space"/* CyeeSettings.IS_EXIST_PRIVATE_SPACE*/, 
                R.bool.def_is_exist_private_space);
        //Gionee <chenml> <2015-08-31> add for CR01546929 end
        
        loadBooleanSetting(stmt, "is_exist_default_storage",R.bool.def_is_exist_default_storage);       
        loadBooleanSetting(stmt, "is_exist_home_key_settings",R.bool.def_is_exist_home_key_settings);       
        loadIntegerSetting(stmt, "home_key_value", R.integer.def_home_key_value);
        loadBooleanSetting(stmt, "fingerprint_is_front",R.bool.def_fingerprint_is_front);       
        loadBooleanSetting(stmt, "is_screen_saving_on",R.bool.def_is_screen_saving_on);       
        
        loadBooleanSetting(stmt, "zenmode_allow_important_notice",R.bool.def_zenmode_allow_important_notice);       
        loadBooleanSetting(stmt, "zenmode_public_holidays_enable",R.bool.def_zenmode_public_holidays_enable);       
        loadStringSetting(stmt, "zenmode_public_holidays_info",R.string.def_zenmode_public_holidays_info);       
        
        //Gionee <lizhipneg> <2015-12-1> add for CR01602143 begin
        loadIntegerSetting(stmt, "multitask_display_style", R.integer.def_multitask_display_style);
        //Gionee <lizhipneg> <2015-12-1> add for CR01602143 end      

      //Gionee <lizhipeng> <2016-01-15> modify for CR01624293 begin
        loadIntegerSetting(stmt, "display_wechat_avatar", R.integer.def_display_wechat_avatar);
        loadIntegerSetting(stmt, "is_exist_left_right_menu", R.integer.def_is_exist_left_right_menu);
        loadIntegerSetting(stmt, "menu_location_state", R.integer.def_is_exist_left_right_menu);
        //Gionee <lizhipeng> <2016-01-15> modify for CR01624293 end
        
      //Gionee <lizhipeng> <2016-01-28> modify for CR01637370 begin
        //Chenyee <CY_Settings> <chenyu> <20171113> modify for SW17W16BA-24 begin
        //Chenyee <CY_Settings> <puyan> <20171113> modify for SW17W16RV-236 begin
        if(sGnBluFlag || cyRVFlag){
	    loadIntegerSetting(stmt, "node_type_left_hand_mode", R.integer.def_node_type_right_hand_mode);
	}else{
	    loadIntegerSetting(stmt, "node_type_left_hand_mode", R.integer.def_node_type_left_hand_mode);
	}
        //Chenyee <CY_Settings> <puyan> <20171113> modify for SW17W16RV-236 end
	//Chenyee <CY_Settings> <chenyu> <20171113> modify for SW17W16BA-24 end
      //Gionee <lizhipeng> <2016-01-28> modify for CR01637370 end

        loadIntegerSetting(stmt, "is_video_dg_on",  R.integer.def_is_video_dg_on);
        loadIntegerSetting(stmt, "safeos_pay_protect_switch",  R.integer.def_safeos_protect_switch);
        //Gionee <lizhipeng> <2016-04-13> modify for CR01675981 begin
        loadIntegerSetting(stmt, "home_key_display_switch",  R.integer.def_home_key_display_switch);
        loadIntegerSetting(stmt, "support_split_screen",  R.integer.def_support_split_screen);
        loadIntegerSetting(stmt, "split_screen_switch",  R.integer.def_split_screen_switch);
        loadIntegerSetting(stmt, "app_lock_setting",  R.integer.def_app_lock_setting);
        
        loadIntegerSetting(stmt, "simulation_location_switch",  R.integer.def_simulation_location_switch);
        loadStringSetting(stmt, "simulation_location_address",  R.string.def_simulation_location_address);
        loadIntegerSetting(stmt, "is_support_whole_beautiface", R.integer.def_is__support_whole_beautiface);
        //Gionee <lizhipeng> <2016-04-13> modify for CR01675981 end

        //Gionee <liuyb> <2016-05-16> add for CR01700399 begin
        loadBooleanSetting(stmt, "show_battery_size", R.bool.def_show_battery_size);
        loadStringSetting(stmt, "devices_battery_size", R.string.def_devices_battery_size);
        //Gionee <liuyb> <2016-05-16> add for CR01700399 end

        //Gionee <wangguojing> <2016-06-06> add for CR01714531 begin
        loadBooleanSetting(stmt, "is_exist_voice_call_auot_mode", R.bool.def_is_exist_voice_call_auot_mode);
        //Gionee <wangguojing> <2016-06-06> add for CR01714531 end
	
        loadBooleanSetting(stmt, "is_exist_otg_reverse_charging",  R.bool.def_is_exist_otg_reverse_charging);
        loadIntegerSetting(stmt, "otg_reverse_charging_state",  R.integer.def_otg_reverse_charging_state);

	//Chenyee chenyu  20180927 modify for CSW1703BN-216 begin
	if(isCyCarrier){
	    loadIntegerSetting(stmt, "pco_change_for_hide_datasever",  R.integer.def_pco_change_for_hide_datasever);
	    loadIntegerSetting(stmt, "pco_change_for_switch_datasever",  R.integer.def_pco_change_for_switch_datasever);
        }
	//Chenyee chenyu  20180927 modify for CSW1703BN-216 end	

        loadStringSetting(stmt, "cyee_mms_keywords", R.string.def_cyee_mms_keywords);

        loadBooleanSetting(stmt, "support_fingerprint_vibration", R.bool.def_support_fingerprint_vibration);
        //Gionee <GN_oversea_Bug> <lipeiming> <20170413> modify for 102958 begin
        if(gnSYflag) {
            loadIntegerSetting(stmt, "fingerprint_vibration", R.integer.def_fingerprint_vibration_symphony);
        }else {
            loadIntegerSetting(stmt, "fingerprint_vibration", R.integer.def_fingerprint_vibration);
        }
        //Gionee <GN_oversea_Bug> <lipeiming> <20170413> modify for 102958 end

	//Chenyee chenyu  20181011 add for CSW1703CX-1299 begin
	loadIntegerSetting(stmt, "boot_sounds", R.integer.def_boot_sounds);
	//Chenyee chenyu  20181011 add for CSW1703CX-1299 end	

        // Gionee <GN_Oversea_Req> <linzhicong> <20170421> add for #117613 beign
        loadIntegerSetting(stmt, "haptic_feedback_enabled", R.integer.def_haptic_feedback_enabled);
        // Gionee <GN_Oversea_Req> <linzhicong> <20170421> add for #117613 end

        loadBooleanSetting(stmt, "support_fast_charge", R.bool.def_support_fast_charge);
        loadIntegerSetting(stmt, "fast_charge", R.integer.def_fast_charge);
        loadIntegerSetting(stmt, "is_support_ami_nail", R.integer.def_is_support_ami_nail);

        // Gionee <liuyb> <2016-09-01> add CR01756162 for begin
        loadBooleanSetting(stmt, "smart_protect_eye_support", R.bool.def_smart_protect_eye_support);
        loadIntegerSetting(stmt, "smart_protect_eye_now_switch", R.integer.def_smart_protect_eye_now_switch);
        loadIntegerSetting(stmt, "smart_protect_eye_time_limit_switch", R.integer.def_smart_protect_eye_time_limit_switch);
        loadStringSetting(stmt, "smart_protect_eye_begin_time", R.string.def_smart_protect_eye_begin_time);
        loadStringSetting(stmt, "smart_protect_eye_end_time", R.string.def_smart_protect_eye_end_time);
        loadIntegerSetting(stmt, "smart_protect_eye_level", R.integer.def_smart_protect_eye_level);
        loadBooleanSetting(stmt, "show_hide_button", R.bool.def_show_hide_button);
        //Chenyee  ningtao 20180223 modify for CSW1703A-98 begin
        loadBooleanSetting(stmt, "cy_screen_display_switch", R.bool.def_cy_screen_display_switch);
        loadBooleanSetting(stmt, "cy_virtual_gesture_exist", R.bool.def_cy_virtual_gesture_exist);
        loadBooleanSetting(stmt, "cy_anti_malfunction", R.bool.def_cy_anti_malfunction);
        //Chenyee ningtao  20180223 modify for CSW1703A-98 end
        //Chenyee  ningtao 20180301 modify for CSW1703A-555 begin
        loadBooleanSetting(stmt, "cy_face_lock_unlock", R.bool.def_cy_face_lock_unlock);
        loadBooleanSetting(stmt, "cy_face_lock_stay", R.bool.def_cy_face_lock_stay);
        //Chenyee ningtao  20180301 modify for CSW1703A-555 end
        //Chenyee  ningtao 20180320 modify for CSW1703A-98 begin
        loadBooleanSetting(stmt, "cy_square_screen_enabled_always", R.bool.def_cy_square_screen_enabled_always);
        //Chenyee ningtao  20180320 modify for CSW1703A-98 end
        //Chenyee  ningtao 20180806 modify for CSW1703AC-128 begin
        loadBooleanSetting(stmt, "cy_req_super_vivid_switch", R.bool.def_cy_req_super_vivid_switch);
        //Chenyee ningtao  20180806 modify for CSW1703AC-128 end

        // Gionee <liuyb> <2016-09-01> add CR01756162 for end
	//Gionee hushengsong 2016-12-01 modify for new GnRespirationLampSettings begin
        //Chenyee  ningtao 20180522 modify for CSW1707A-1126 begin
 	if(SystemProperties.get("ro.cy.led.new.support").equals("yes"))
        //Chenyee ningtao  20180522 modify for CSW1707A-1126 end
	{
		loadBooleanSetting(stmt, "respiration_lamp_notification_new",
                    R.bool.def_respirationlamp_notification_new);
 	    	loadBooleanSetting(stmt, "respiration_lamp_call_new",
                    R.bool.def_respirationlamp_call_new);
	}
	//Gionee hushengsong 2016-12-01 modify for new GnRespirationLampSettings end
		//Gionee hushengsong 2016-03-10 modify for CR01650054 begin
		loadIntegerSetting(stmt, "floating_touch_setting_size",
				R.integer.def_floating_touch_setting_size);
		loadIntegerSetting(stmt, "floating_touch_setting_transparency",
				R.integer.def_floating_touch_setting_transparency);
	    //Gionee hushengsong 2016-03-10 modify for CR01650054 end
	    //Gionee <liuyuankun> <2016-10-27> add for emergency begin
        if (SystemProperties.get("ro.gn.switch.emergency.support", "no").equals("yes")) {
            loadIntegerSetting(stmt, "cyee_emergency_switch", R.integer.cyee_emergency_switch_on);
        } else {
            loadIntegerSetting(stmt, "cyee_emergency_switch", R.integer.cyee_emergency_switch_off);
        }
        loadStringSetting(stmt, "emergency_sms_content", R.string.def_emergency_sms_content);
        loadIntegerSetting(stmt, "cyee_driving_mode", R.integer.cyee_driving_mode);
        loadIntegerSetting(stmt, "cyee_driving_switch", R.integer.cyee_driving_switch);
        //Gionee <liuyuankun> <2016-10-27> add for emergency end
	//Gionee <GN_Oversea_Req> <fujiabing> <20170818> add for 180104 begin
	if(isLEDRemindSupport){
	loadBooleanSetting(stmt, "led_lamp_remind_normal",
                    R.bool.def_led_lamp_remind_normal);
	loadBooleanSetting(stmt, "led_lamp_remind_vibrate",
                    R.bool.def_led_lamp_remind_vibrate);
	loadBooleanSetting(stmt, "led_lamp_remind_silent",
                    R.bool.def_led_lamp_remind_silent);
	loadBooleanSetting(stmt, "led_lamp_remind_low_battery",
                    R.bool.def_led_lamp_remind_low_battery);
	loadBooleanSetting(stmt, "led_lamp_remind_blink_frequency",
                    R.bool.def_led_lamp_remind_blink_frequency);
	}
	//Gionee <GN_Oversea_Req> <fujiabing> <20170818> add for 180104 end

	//Chenyee <CY_Oversea_Req> <puyan> <20180421> add for CSW1703CX-28 begin
        //*/ freeme.Greg, 20180410 Default Assistan App's Component Name.
	if (cyCXFlag) {
        loadStringSetting(stmt, Settings.Secure.ASSISTANT, R.string.def_assistant_component_name);

        //Chenyee <CY_Bug> <puyan> <20180425> add for CSW1703CX-10 begin
        //*/freeme,jibowei,20180425,default ime sogou
        loadStringSetting(stmt, Settings.Secure.ENABLED_INPUT_METHODS,
                R.string.config_default_input_method );
        //*/
        //Chenyee <CY_Bug> <puyan> <20180425> add for CSW1703CX-10 end
    }
        //*/
	//Chenyee <CY_Oversea_Req> <puyan> <20180421> add for CSW1703CX-28 end
    }
    
  //Gionee <lizhipeng> <2015-07-11> add for CR01518122 begin
    private void initCustomGestureData(SQLiteStatement stmt){
        loadBooleanSetting(stmt,"custom_gesture_switch",
                R.bool.def_custom_gesture_switch);
        for(int i=1;i<=15;i++){
            loadStringSetting(stmt, "custom_gesture"+i ,R.string.def_custom_gesture_value);
        }    
 
    }
  //Gionee <lizhipeng> <2015-07-11> add for CR01518122 end

    //Gionee <wangguojing> <2013-10-11> add for CR00932764 begin
    private void startCopySettingsValues(SQLiteStatement stmt) {
        int temp;
        String str;
        ContentResolver cr = mContext.getContentResolver();
        temp = Settings.System.getInt(mContext.getContentResolver(),"gn_font_size", 0);
        if(temp != 0){
            loadSetting(stmt, CyeeSettings.FONT_SIZE,temp);
            Settings.System.putInt(mContext.getContentResolver(), "gn_font_size", 0);
        }
        //if(SystemProperties.get("ro.gn.respirationlamp.support").equals("yes")){
            temp = Settings.System.getInt(cr,"gn_respirationlamp_low_power", 1);
            if(temp != 1){
                loadSetting(stmt, CyeeSettings.RESPIRATION_LAMP_LOW_POWER, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_low_power", 1);
            }
            
            temp = Settings.System.getInt(cr,"gn_respirationlamp_in_charge", 1);
            if(temp != 1){
                loadSetting(stmt, CyeeSettings.RESPIRATION_LAMP_IN_CHARGE, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_in_charge", 1);
            }
            
            temp = Settings.System.getInt(cr,"gn_respirationlamp_notification", 1);
            if(temp != 1){
                loadSetting(stmt, CyeeSettings.RESPIRATION_LAMP_NOTIFICATION, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_notification", 1);
            }
            
            temp = Settings.System.getInt(cr,"gn_respirationlamp_music", 1);
            if(temp != 1){
                loadSetting(stmt, CyeeSettings.RESPIRATION_LAMP_MUSIC, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_music", 1);
            }
            
            temp = Settings.System.getInt(cr,"gn_respirationlamp_call", 1);
            if(temp != 1){
                loadSetting(stmt, CyeeSettings.RESPIRATION_LAMP_CALL, temp);
                Settings.System.putInt(cr, "gn_respirationlamp_call", 1);
            }
        //}
		
        //if (SystemProperties.get("ro.gn.gesture.support").equals("yes")) {
        
            temp = Settings.System.getInt(cr,"ssg_auto_dial", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SSG_AUTO_DIAL, temp);
                loadSetting(stmt, CyeeSettings.SSG_AUTO_DIAL_SHOW, temp);
                Settings.System.putInt(cr, "ssg_auto_dial", 0);
            }
            
            temp = Settings.System.getInt(cr,"ssg_call_access", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SSG_CALL_ACCESS, temp);
                loadSetting(stmt, CyeeSettings.SSG_CALL_ACCESS_SHOW, temp)	;			
                Settings.System.putInt(cr, "ssg_call_access", 0);
            }
            
            temp = Settings.System.getInt(cr,"ssg_delay_alarm", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SSG_DELAY_ALARM, temp);
                loadSetting(stmt, CyeeSettings.SSG_DELAY_ALARM_SHOW, temp);
                
                Settings.System.putInt(cr, "ssg_delay_alarm", 0);
            }
            
            temp = Settings.System.getInt(cr,"ssg_switch_screen", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SSG_SWITCH_SCREEN, temp);
                loadSetting(stmt, CyeeSettings.SSG_SWITCH_SCREEN_SHOW, temp);
                Settings.System.putInt(cr, "ssg_switch_screen", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_call_access", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SDG_CALL_ACCESS, temp);
                loadSetting(stmt, CyeeSettings.SDG_CALL_ACCESS_SHOW, temp);
                Settings.System.putInt(cr, "sdg_call_access", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_browse_photos", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SDG_BROWSE_PHOTOS, temp);
                loadSetting(stmt, CyeeSettings.SDG_BROWSE_PHOTOS_SHOW, temp);
                Settings.System.putInt(cr, "sdg_browse_photos", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_video_progress", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SDG_VIDEO_PROGRESS, temp);
                loadSetting(stmt, CyeeSettings.SDG_VIDEO_PROGRESS_SHOW, temp);
                Settings.System.putInt(cr, "sdg_video_progress", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_video_volume", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SDG_VIDEO_VOLUME, temp);
                loadSetting(stmt, CyeeSettings.SDG_VIDEO_VOLUME_SHOW, temp);
                Settings.System.putInt(cr, "sdg_video_volume", 0);
            }
            
            temp = Settings.System.getInt(cr,"sdg_video_pause", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SDG_VIDEO_PAUSE, temp);
                loadSetting(stmt, CyeeSettings.SDG_VIDEO_PAUSE_SHOW, temp);
                
                Settings.System.putInt(cr, "sdg_video_pause", 0);
            }
            
            temp = Settings.System.getInt(cr,"ssg_switch", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.GN_SSG_SWITCH, temp);
                Settings.System.putInt(cr, "ssg_switch", 0);
            }
            
            temp = Settings.System.getInt(cr,"dg_switch", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.GN_DG_SWITCH, temp);
                Settings.System.putInt(cr, "dg_switch", 0);
            }
        //}
		
        //if(SystemProperties.get("ro.gn.soundctrl.support").equals("yes")){
        
            temp = Settings.System.getInt(cr,"gn_sound_control_switch", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SOUND_CONTROL_SWITCH, temp);
                Settings.System.putInt(cr, "gn_sound_control_switch", 0);
            }
            
            temp = Settings.System.getInt(cr,"gn_sound_control_calling", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SOUND_CONTROL_CALLING, temp);
                Settings.System.putInt(cr, "gn_sound_control_calling", 0);
            }
            
            temp = Settings.System.getInt(cr,"gn_sound_control_message", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SOUND_CONTROL_MESSAGE, temp);
                Settings.System.putInt(cr, "gn_sound_control_message", 0);
            }
            
            temp = Settings.System.getInt(cr,"gn_sound_control_lockscreen", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SOUND_CONTROL_LOCKSCREEN, temp);
                Settings.System.putInt(cr, "gn_sound_control_lockscreen", 0);
            }
            
            temp = Settings.System.getInt(cr,"gn_sound_control_alarmclock", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SOUND_CONTROL_ALARMCLOCK, temp);
                Settings.System.putInt(cr, "gn_sound_control_alarmclock", 0);
            }
        
        //}

        //if (SystemProperties.get("ro.gn.flip.support").equals("yes")) {
            temp = Settings.System.getInt(cr,"gn_flip_sounds_enabled", 1);
            if(temp != 1){
                loadSetting(stmt, CyeeSettings.FLIP_SOUNDS_ENABLED, temp);
                Settings.System.putInt(cr, "gn_flip_sounds_enabled", 1);
            }
            temp = Settings.System.getInt(cr,"flip_answer_call_switch", 1);
            if(temp != 1){
                loadSetting(stmt, CyeeSettings.FLIP_ANSWER_CALL_SWITCH, temp);
                Settings.System.putInt(cr, "flip_answer_call_switch", 1);
            }
        //}
		
        temp = Settings.System.getInt(cr,"haptic_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, CyeeSettings.HAPTIC_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "haptic_vibration_enabled", 1);
        }
        
        temp = Settings.System.getInt(cr,"switch_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, CyeeSettings.SWITCH_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "switch_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"dialpad_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, CyeeSettings.DIALPAD_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "dialpad_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"lockscreen_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, CyeeSettings.LOCKSCREEN_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "lockscreen_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"selectapp_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, CyeeSettings.SELECTAPP_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "selectapp_vibration_enabled", 1);
        }
    	
    	
        temp = Settings.System.getInt(cr,"ring_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, CyeeSettings.RING_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "ring_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"mms_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, CyeeSettings.MMS_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "mms_vibration_enabled", 1);
        }
        temp = Settings.System.getInt(cr,"notification_vibration_enabled", 1);
        if(temp != 1){
            loadSetting(stmt, CyeeSettings.NOTIFICATION_VIBRATION_ENABLED, temp);
            Settings.System.putInt(cr, "notification_vibration_enabled", 1);
        }
        //if (SystemProperties.get("ro.gn.fanfanwidget.support").equals("yes")) {
            temp = Settings.System.getInt(cr,"gn_fanfan_widget_auto_push", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.FANFAN_WIDGET_AUTO_PUSH, temp);
                Settings.System.putInt(cr, "gn_fanfan_widget_auto_push", 0);
            }
        //}
        //if (SystemProperties.get("ro.gn.guestmode.support").equals("yes")) {
            temp = Settings.Secure.getInt(cr,"gionee_guest_mode", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.GUEST_MODE, temp);
                Settings.Secure.putInt(cr, "gionee_guest_mode", 0);
            }
            temp = Settings.Secure.getInt(cr,"gionee_first_open_guest", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.FIRST_OPEN_GUEST_MODE, temp);
                Settings.Secure.putInt(cr, "gionee_first_open_guest", 0);
            }
            temp = Settings.Secure.getInt(cr,"gionee_guest_pass_enable", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.GUEST_PASS_ENABLE, temp);
                Settings.Secure.putInt(cr, "gionee_guest_pass_enable", 0);
            }
            str = Settings.Secure.getString(cr,"gionee_guest_pass");
            if(str != null){
                loadSetting(stmt, CyeeSettings.GUEST_PASS, str);
                Settings.Secure.putString(cr, "gionee_guest_pass", null);
            }
        //}
    	
        temp = Settings.System.getInt(cr,"gn_button_light", 0);
        if(temp != 0){
            loadSetting(stmt, CyeeSettings.Button_Light_State, temp);
            Settings.System.putInt(cr, "gn_button_light", 0);
        }
        
        //if (SystemProperties.get("ro.gn.networkalert.support").equals("yes")) {
            temp = Settings.System.getInt(cr,"wifi_auto_notify", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.WIFI_AUTO_NOTIFY, temp);
                Settings.System.putInt(cr, "wifi_auto_notify", 0);
            }
        
        //}
		
         temp = Settings.System.getInt(cr,"align_wake", 1);
         if(temp != 1){
             loadSetting(stmt, CyeeSettings.ALIGN_WAKE, temp);
             Settings.System.putInt(cr, "align_wake", 1);
         }
         
         temp = Settings.System.getInt(cr,"align_wake", 1);
         if(temp != 1){
             loadSetting(stmt, CyeeSettings.ALIGN_WAKE, temp);
             Settings.System.putInt(cr, "align_wake", 1);
         }
         
         temp = Settings.System.getInt(cr,"screen_off_timeout_back_up",30000);
         if(temp != 30000){
             loadSetting(stmt, CyeeSettings.SCREEN_OFF_TIMEOUT_BACK_UP, temp);
             Settings.System.putInt(cr, "screen_off_timeout_back_up", 30000);
         }
    	
        //if (SystemProperties.get("ro.gn.mms.alertMissMsg").equals("yes")) {
            temp = Settings.System.getInt(cr,"alert_miss_msg", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.ALERT_MISS_MSG, temp);
                Settings.System.putInt(cr, "alert_miss_msg", 0);
            }
            temp = Settings.System.getInt(cr,"alert_miss_msg_interval", 5);
            if(temp != 5){
                loadSetting(stmt, CyeeSettings.ALERT_MISS_MSG_INTERVAL, temp);
                Settings.System.putInt(cr, "alert_miss_msg_interval", 5);
            }
        //}
    	
        temp = Settings.System.getInt(cr,"auto_lcm_acl", 0);
        if(temp != 0){
            loadSetting(stmt, CyeeSettings.AUTO_LCM_ACL, temp);
            Settings.System.putInt(cr, "auto_lcm_acl", 0);
        }
        
        str = Settings.System.getString(cr,"alarmring_default");
        if(str != null){
            loadSetting(stmt, CyeeSettings.ALARM_RING, str);
            Settings.System.putString(cr, "alarmring_default", null);
        }
        
        str = Settings.System.getString(cr,"ringtone2");
        if(str != null){
            loadSetting(stmt, CyeeSettings.RINGTONE2, str);
            Settings.System.putString(cr, "ringtone2", null);
        }
        
        str = Settings.System.getString(cr,"mms");
        if(str != null){
            loadSetting(stmt, CyeeSettings.MMS, str);
            Settings.System.putString(cr, "mms", null);
        }
        
        str = Settings.System.getString(cr,"mms2");
        if(str != null){
            loadSetting(stmt, CyeeSettings.MMS2, str);
            Settings.System.putString(cr, "mms2", null);
        }
        
        
        str = Settings.System.getString(cr,"theme_package_name");
        if(str != "theme" && str != null){
            loadSetting(stmt, CyeeSettings.THEME_PACKEAGE_NAME, str);
            Settings.System.putString(cr, "theme_package_name", "theme");
        }
        
        temp = Settings.System.getInt(cr,"theme_check_package", 0);
        if(temp != 0){
            loadSetting(stmt, CyeeSettings.THEME_CHECK_PACKEAGE, temp);
            Settings.System.putInt(cr, "theme_check_package", 0);
        }
        
        //if (SystemProperties.get("ro.gn.suspendbutton.support").equals("yes")) {
            temp = Settings.System.getInt(cr,"gn_suspend_button", 0);
            if(temp != 0){
                loadSetting(stmt, CyeeSettings.SUSPEND_BUTTON, temp);
                Settings.System.putInt(cr, "gn_suspend_button", 0);
            }
        //}
    	
        // if (SystemProperties.get("ro.gn.single.hand.support").equals("yes")) {
             temp = Settings.System.getInt(cr,"gn_phone_keyboard", 0);
             if(temp != 0){
                 loadSetting(stmt, CyeeSettings.PHONE_KEYBOARD, temp);
                 Settings.System.putInt(cr, "gn_phone_keyboard", 0);
             }
             temp = Settings.System.getInt(cr,"gn_input_method_keyboard", 0);
             if(temp != 0){
                 loadSetting(stmt, CyeeSettings.INPUT_METHOD_KEYBOARD, temp);
                 Settings.System.putInt(cr, "gn_input_method_keyboard", 0);
             }
             temp = Settings.System.getInt(cr,"gn_pattern_unlockscreen", 0);
             if(temp != 0){
                 loadSetting(stmt, CyeeSettings.PATTERN_UNLOCKSCREEN, temp);
                 Settings.System.putInt(cr, "gn_pattern_unlockscreen", 0);
             }
             temp = Settings.System.getInt(cr,"gn_small_screen_mode", 0);
             if(temp != 0){
                 loadSetting(stmt, CyeeSettings.SMALL_SCREEN_MODE, temp);
                 Settings.System.putInt(cr, "gn_small_screen_mode", 0);
             }
             temp = Settings.System.getInt(cr,"gn_screen_size", 0);
             if(temp != 0){
                 loadSetting(stmt, CyeeSettings.SCREEN_SIZE, temp);
                 Settings.System.putInt(cr, "gn_screen_size", 0);
             }
         //}

		
    }	
	
    private void copySettingsDB(SQLiteDatabase db) {
		Log.e(TAG,"copySettingsDB");
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                    + " VALUES(?,?);");
            startCopySettingsValues(stmt);
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    //Gionee <wangguojing> <2013-10-11> add for CR00932764 end

	// Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 begi
	  private String getStringValueFromTable(SQLiteDatabase db, String table, String name,
            String defaultValue) {
        Cursor c = null;
        try {
            c = db.query(table, new String[] { "value" }, "name='" + name + "'",
                    null, null, null, null);
            if (c != null && c.moveToFirst()) {
                String val = c.getString(0);
                return val == null ? defaultValue : val;
            }
        } finally {
            if (c != null) c.close();
        }
        return defaultValue;
    }
	// Gionee <bug> <wangyaohui> <2013-10-24> modify for CR00933322 end

    //Gionee <wangguojing> <2014-05-12> add for CR01237681 begin
    private void updateDBValue(SQLiteDatabase db) {
        updateDBValue(db, true);
    }

    public void updateDBValue(SQLiteDatabase db, boolean forceUpdate) {
        FileReader dbReader;
        final File dbConfigFile = new File(CYEESETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateDBValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + CYEESETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "cyeesetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"cyeesetting".equals(name)) {
                        break;
                    }
                    
                    String cyeename = parser.getAttributeValue(null, "cyeename");
                    String cyeevalue = parser.getAttributeValue(null, "cyeevalue");
                    String cyeeProp = parser.getAttributeValue(null, "cyeeprop");
                    Log.w(TAG, "updateDBValue  cyeename=" + cyeename);
                    Log.w(TAG, "updateDBValue  cyeevalue=" + cyeevalue);
                    if(forceUpdate) {
                        loadSetting(stmt, cyeename, cyeevalue);
                    } else if("update".equals(cyeeProp) || "insert".equals(cyeeProp)) {
                        loadSetting(stmt, cyeename, cyeevalue);
                    }
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    //Gionee <wangguojing> <2014-05-12> add for CR01237681 end
	
    //Gionee <chenml> <2014-06-18> add for CR01272876 begin
    private void afreshDBvalue(SQLiteStatement stmt, SQLiteDatabase db) {
        int clickWakeswitch = 0;
        String switchValue = null;
        Cursor cursor = db.query(CyeeSettings.TABLE_CONFIG, null, null, null, null, null, null);
        Log.w(TAG, "afreshDBvalue cursor : " + cursor);
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    switchValue = cursor.getString(cursor.getColumnIndex(CyeeSettings.NAME));
                    if (switchValue.equals(CyeeSettings.GN_SSG_SWITCH)) {
                        clickWakeswitch = cursor.getInt(cursor.getColumnIndex(CyeeSettings.VALUE));
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
            Log.w(TAG, "whether cursor is closed ? cursor : "+cursor);
        }
        int defaulClickValue = mContext.getResources().getBoolean(R.bool.def_ssg_doubleclick_wake_show) ? 1 : 0;
        Log.w(TAG, "afreshDBvalue  defaulClickValue : " + defaulClickValue + " clickWakeswitch : "
                + clickWakeswitch);
        if (clickWakeswitch == 1 && defaulClickValue == 1) {
            loadBooleanSetting(stmt, CyeeSettings.SSG_DOUBLECLICK_WAKE,
                    R.bool.def_ssg_doubleclick_wake_other);
        }
    }
    //Gionee <chenml> <2014-06-18> add for CR01272876 end

    //Gionee <wangguojing> <2015-06-15> add for CR01501989 end
    private void updateIPOValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(CYEESETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateIPOValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + CYEESETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "cyeesetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"cyeesetting".equals(name)) {
                        break;
                    }
                    String cyeename = parser.getAttributeValue(null, "cyeename");
                    if (!"is_mtk_ipo_on".equals(cyeename)) {
                        continue;
                    }
                    
                    String cyeevalue = parser.getAttributeValue(null, "cyeevalue");
                    Log.w(TAG, "updateIPOValue  cyeename=" + cyeename);
                    Log.w(TAG, "updateIPOValue  cyeevalue=" + cyeevalue);
                    loadSetting(stmt, cyeename,cyeevalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <wangguojing> <2015-06-15> add for CR01501989 end
    
    //Gionee <wangguojing> <2015-08-28> add for CR01501989 begin
    private void updateDiagConfigValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(CYEESETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateDiagConfigValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + CYEESETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "cyeesetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"cyeesetting".equals(name)) {
                        break;
                    }
                    String cyeename = parser.getAttributeValue(null, "cyeename");
                    if (!"wirte_diag_config".equals(cyeename)) {
                        continue;
                    }
                    
                    String cyeevalue = parser.getAttributeValue(null, "cyeevalue");
                    Log.w(TAG, "updateDiagConfigValue  cyeename=" + cyeename);
                    Log.w(TAG, "updateDiagConfigValue  cyeevalue=" + cyeevalue);
                    loadSetting(stmt, cyeename,cyeevalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <wangguojing> <2015-08-28> add for CR01501989 end
    
    
    //Gionee <lizhipeng> <2015-08-28> add for CR01545605 begin
    private void updateConfigValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(CYEESETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateConfigValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + CYEESETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "cyeesetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"cyeesetting".equals(name)) {
                        break;
                    }
                    String cyeename = parser.getAttributeValue(null, "cyeename");
                    if (!"8605and8609_gesture_switch".equals(cyeename)) {
                        continue;
                    }
                    
                    String cyeevalue = parser.getAttributeValue(null, "cyeevalue");
                    Log.w(TAG, "updateDiagConfigValue  cyeename=" + cyeename);
                    Log.w(TAG, "updateDiagConfigValue  cyeevalue=" + cyeevalue);
                    loadSetting(stmt, cyeename,cyeevalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <lizhipeng> <2015-08-28> add for CR01545605 end
    //Gionee <wangguojing> <2015-08-29> add for CR01545815 begin
    private void updateGlovePatternsSwitchValue(SQLiteDatabase db) {
        FileReader dbReader;
        final File dbConfigFile = new File(CYEESETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateGlovePatternsSwitchValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + CYEESETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "cyeesetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"cyeesetting".equals(name)) {
                        break;
                    }
                    String cyeename = parser.getAttributeValue(null, "cyeename");
                    if (!"is_glove_patterns_on".equals(cyeename)) {
                        continue;
                    }
                    
                    String cyeevalue = parser.getAttributeValue(null, "cyeevalue");
                    Log.w(TAG, "updateGlovePatternsSwitchValue  cyeename=" + cyeename);
                    Log.w(TAG, "updateGlovePatternsSwitchValue  cyeevalue=" + cyeevalue);
                    loadSetting(stmt, cyeename,cyeevalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    
    
    }
    //Gionee <wangguojing> <2015-08-29> add for CR01545815 end    
    
    
  //Gionee <lizhipeng> <2015-09-06> add for CR01549142 begin
    private void updateConfigValue(SQLiteDatabase db,String CyeeName) {
        FileReader dbReader;
        final File dbConfigFile = new File(CYEESETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateConfigValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + CYEESETTING_DB_CONFIG_FILE);
            return ;
        }
        
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("REPLACE INTO config(name,value)"
                + " VALUES(?,?);");
            
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "cyeesetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"cyeesetting".equals(name)) {
                        break;
                    }
                    String cyeename = parser.getAttributeValue(null, "cyeename");
                    if (!CyeeName.equals(cyeename)) {
                        continue;
                    }
                    String cyeevalue = parser.getAttributeValue(null, "cyeevalue");
                    Log.w(TAG, "updateDiagConfigValue  cyeename=" + cyeename);
                    Log.w(TAG, "updateDiagConfigValue  cyeevalue=" + cyeevalue);
                    loadSetting(stmt, cyeename,cyeevalue);
                    
                }
                
                dbReader.close();
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } catch (IOException e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    //Gionee <lizhipeng> <2015-09-06> add for CR01549142 end
    //Gionee <lizhipeng> <2015-09-11> add for CR01549480 begin
    public void updateLcmAclValue(){
    	ContentResolver cr = mContext.getContentResolver();
    	if(CyeeSettings.getInt(cr,"8605and8609_gesture_switch", 0) == 1){
    		boolean mcheck=(CyeeSettings.getInt(cr,
                    "adjust_screen_tone"/*CyeeSettings.ADJUST_SCREEN_TONE*/, 1) == 1);
       	 	CyeeSettings.putInt(cr,CyeeSettings.AUTO_LCM_ACL,mcheck ? 1: 0);
    	}
    }
    //Gionee <lizhipeng> <2015-09-11> add for CR01549480 end
    //Gionee <wangguojing> <2015-10-21> add for CR01572592 begin
    private void updateDBValueForOTA(SQLiteDatabase db) {
        ArrayList<String> updateOTAList = new ArrayList<String>();
        //can add other need update names
        updateOTAList.add("is_exist_home_key_settings");
        updateOTAList.add("is_video_dg_on");

        //Gionee <liuyb> <2016-05-16> add for CR01700399 begin
        updateOTAList.add("show_battery_size");
        updateOTAList.add("devices_battery_size");
        //Gionee <liuyb> <2016-05-16> add for CR01700399 end
        //Gionee <lizhipeng> <2016-6-24> add for CR01704221 begin  
        updateOTAList.add("home_key_display_switch");
        //Gionee <lizhipeng> <2016-6-24> add for CR01704221 end  

        // Gionee <liuyb> <2016-07-25> add for CR01736852 begin
        updateOTAList.add("support_split_screen");
        // Gionee <liuyb> <2016-07-25> add for CR01736852 end

        updateOTAList.add("support_fingerprint_vibration");
        updateOTAList.add("fingerprint_vibration");
	//Gionee hushengsong 2017-02-27 modify for 75541 begin
	updateOTAList.add("smart_protect_eye_support");
	//Gionee hushengsong 2017-02-27 modify for 75541 end
        updateOTAList.add("support_fast_charge");
        updateOTAList.add("fast_charge");
        updateOTAList.add("is_exist_otg_reverse_charging");
	//Chenyee <CY_REQ> <chenyu> <20181022> add for CSW1703BA-327 begin
        updateOTAList.add("smart_protect_eye_level");
	//Chenyee <CY_REQ> <chenyu> <20181022> add for CSW1703BA-327 end
        //Chenyee  ningtao 20181126 modify for CSW1703BA-397 begin
        updateOTAList.add("ssg_doubleclick_lock");
        //Chenyee ningtao  20181126 modify for CSW1703BA-397 end
        updateOTAList.add("ssg_doubleclick_cy_lock");
        for(int i = 0; i < updateOTAList.size(); i++){
            updateConfigValue(db, updateOTAList.get(i));
        }

    }
    //Gionee <wangguojing> <2015-10-21> add for CR01572592 end
    
    
  //Gionee <lizhipneg> <2016-01-04> add for CR01602143 begin
    private void updateForceTouchFilePath() {
        FileReader dbReader;
        final File dbConfigFile = new File(CYEESETTING_DB_CONFIG_FILE);
        Log.d(TAG, "updateConfigValue");
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Can't open " + CYEESETTING_DB_CONFIG_FILE);
            return ;
        }

            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(dbReader);
                
                XmlUtils.beginDocument(parser, "cyeesetting");
                
                while (true) {
                    XmlUtils.nextElement(parser);
                    
                    String name = parser.getName();
                    if (!"cyeesetting".equals(name)) {
                        break;
                    }
                    String cyeename = parser.getAttributeValue(null, "cyeename");
                    Log.i(TAG,"updateForceTouchFilePath "+cyeename);
                    switch (cyeename) {
					case NODE_TYPE_FORCE_TOUCH_LOW_THRE_PATH:
						NODE_TYPE_FORCE_TOUCH_LOW_THRE = parser.getAttributeValue(null, "cyeevalue");
						break;
					case NODE_TYPE_FORCE_TOUCH_HIGH_THRE_PATH:
						NODE_TYPE_FORCE_TOUCH_HIGH_THRE = parser.getAttributeValue(null, "cyeevalue");
						break;
					case NODE_TYPE_FORCE_TOUCH_THRESHOLD_PATH:
						NODE_TYPE_FORCE_TOUCH_THRESHOLD= parser.getAttributeValue(null, "cyeevalue");
						break;

					default:
						break;
					}
                }
                
                dbReader.close();
            } catch (Exception e) {
                Log.w(TAG, "Exception in font config parser " + e);
            } 
    }
    
    public int readGestureNodeValue(Context context, String path,int defaultValue_resid,int level) {
        File file = new File(path);
        BufferedReader reader = null;
        int tempString =  context.getResources().getInteger(defaultValue_resid)+level;
        try {
            reader = new BufferedReader(new FileReader(file));
            int line = 1;
            tempString =  Integer.parseInt(reader.readLine())+level;
            reader.close();
            Log.i(TAG,"readGestureNodeValue path="+path+"  tempString="+tempString+":level="+level);
            return tempString;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG,e.toString());
        } finally {
            if (reader != null) {
                try {
                	reader.close();
                	return tempString;
                } catch (IOException e1) {
                	 Log.i(TAG,e1.toString());
                }
            }
        }
        Log.i(TAG,"readGestureNodeValue path="+path+"  tempString="+tempString+":level="+level);
        return tempString;
    }
  //Gionee <lizhipneg> <2016-01-04> add for CR01602143 end

}
