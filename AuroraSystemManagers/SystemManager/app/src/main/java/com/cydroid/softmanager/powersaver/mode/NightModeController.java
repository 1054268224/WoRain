package com.cydroid.softmanager.powersaver.mode;

import java.util.Calendar;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.fragment.PowerManagerSettingsFragment;
import com.cydroid.softmanager.utils.Log;

import com.cydroid.softmanager.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

public class NightModeController {
    private static final String TAG = "NightModeController";

    private static final int NIGHT_TIME = 1;
    private static final int MIDNIGHT_TIME = 2;
    private static final int WEE_HOURS_TIME = 3;

    private static final long SET_ALARM_AT_NIGHT_WAIT_TIME = 3 * 60 * 60 * 1000;
    private static final long SET_ALARM_AT_MIDNIGHT_WAIT_TIME = 2 * 60 * 60 * 1000;
    private static final long SET_ALARM_AT_WEE_HOURS_WAIT_TIME = 30 * 60 * 1000;

    public static final String PREF_NIGHT_MODE_STATE = "night_mode_state";

    private final Context mContext;

    public NightModeController(Context context) {
        mContext = context;
    }

    public void start() {
        if (isInNightMode()) {
            Log.d(TAG, "already in nightmode");
            return;
        }
        Log.d(TAG, "----->startEnterPowerSaver()");
        ModeItemsController nightModeItems = getNightModeController(mContext);
        nightModeItems.saveCheckPoint();
        nightModeItems.applyConfig();
        setInNightMode(true);
    }

    private static ModeItemsController getNightModeController(Context context) {
        String[] nightItemArray = context.getResources().getStringArray(R.array.powermode_NIGHT);
        ModeItemsController nightModeItems = new ModeItemsController(context, "NIGHT", nightItemArray);
        nightModeItems.resetConfigToDefault();
        return nightModeItems;
    }

    public void restore() {
        if (isInNightMode()) {
            ModeItemsController nightModeItems = getNightModeController(mContext);
            nightModeItems.restoreCheckPoint();
            setInNightMode(false);
        }
    }

    public boolean isInNightMode() {
        SharedPreferences nightModeStatePref = mContext.getSharedPreferences(PREF_NIGHT_MODE_STATE,
                Context.MODE_PRIVATE);
        boolean res = nightModeStatePref.getBoolean(Consts.NIGHT_AUTO_POWER_SAVER, false);
        Log.d(TAG, "is in night mode now ?" + res);
        return res;
    }

    private void setInNightMode(boolean enable) {
        Log.d(TAG, "set night mode state = " + enable);
        SharedPreferences nightModeStatePref = mContext.getSharedPreferences(PREF_NIGHT_MODE_STATE,
                Context.MODE_PRIVATE);
        nightModeStatePref.edit().putBoolean(Consts.NIGHT_AUTO_POWER_SAVER, enable).commit();
    }

    public boolean isNightModeSwitchEnable() {
        SharedPreferences settingPreference = mContext.getSharedPreferences(
                PowerManagerSettingsFragment.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);

		//Gionee guoxt 2017-03-07 modify for 78574 begin
        boolean isNightModeOn = false;
        // Gionee xionghg 2017-05-04 modify for 130202 begin
        if (Consts.gnVFflag || Consts.gnSwFlag || Consts.gnGTFlag || Consts.gnIPFlag||Consts.cyCXFlag || Consts.cyBAFlag) {
        // Gionee xionghg 2017-05-04 modify for 130202 end
            isNightModeOn = settingPreference.getBoolean(Consts.NIGHT_MODE, false);
        } else {
            isNightModeOn = settingPreference.getBoolean(Consts.NIGHT_MODE, true);
        }
        //Gionee guoxt 2017-03-07 modify for 78574 end    
        Log.d(TAG, "powersetting val  isNightModeSwitchOn=" + isNightModeOn);
        return isNightModeOn;
    }

    public long getStartTime() {
        int nightMode = getNightTime();
        long enterTime = 0;
        if (nightMode == NIGHT_TIME) { // 当前时间为 20点 至 22点之间，则三小时之后关闭耗电项，亮屏后恢复。
            // isAutoEnter = true;
            enterTime = SET_ALARM_AT_NIGHT_WAIT_TIME;
        } else if (nightMode == MIDNIGHT_TIME) { // 当前时间为 22点 至
                                                 // 24点之间，则两小时之后关闭耗电项，亮屏后恢复。
            // isAutoEnter = true;
            enterTime = SET_ALARM_AT_MIDNIGHT_WAIT_TIME;
        } else if (nightMode == WEE_HOURS_TIME) { // 当前时间为 0点 至
                                                  // 3点之间，则半小时之后关闭耗电项，亮屏后恢复。
            // isAutoEnter = true;
            enterTime = SET_ALARM_AT_WEE_HOURS_WAIT_TIME;
        } else {
            Calendar c23 = Calendar.getInstance();
            c23.set(Calendar.HOUR_OF_DAY, 23);
            c23.set(Calendar.SECOND, 0);
            c23.set(Calendar.MINUTE, 0);
            c23.set(Calendar.MILLISECOND, 0);
            enterTime = c23.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
            // 其他时间,指定到23:00进入夜间省电
        }
        return enterTime;
    }

    private int getNightTime() {
        long currentTime = System.currentTimeMillis();
        String sHour = DateFormat.format("kk", currentTime).toString();

        Long lHour = Long.valueOf(sHour);
        if (lHour >= 20 && lHour < 22) {
            return NIGHT_TIME;
        } else if (lHour >= 22 && lHour < 24) {
            return MIDNIGHT_TIME;
        } else if (lHour >= 0 && lHour <= 3) {
            return WEE_HOURS_TIME;
        }
        return 0;
    }

}
