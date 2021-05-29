/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: yangxinruo
 *
 * Date: 2016-10-19
 */
package com.cydroid.softmanager.trafficassistant.hotspotremind;

import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.utils.Log;



public class HotSpotMonitorDataHelper {
    private static final String HOTSPOT_MONITOR_PREF_NAME = "com.cydroid.softmanager_preferences";

    private static final String KEY_HOTSPORT_REMIND_SETTED_INDEX = "HotsportRemindSettedIndex";
    private static final int DEFAULT_HOTSPORT_REMIND_SETTED_INDEX = 1;
	/*guoxt 20170214 modify for 67305 begin*/
	private static final int DEFAULT_HOTSPORT_REMIND_SETTED_INDEX_VF = 0;
	/*guoxt 20170214 modify for 67305 end*/

    private static final String KEY_HOTSPORT_REMIND_SETTED_VALUE = "HotsportRemindSettedValue";
    private static final int DEFAULT_HOTSPORT_REMIND_SETTED_VALUE = 10;
	/*guoxt 20170214 modify for 67305 begin*/
	private static final int DEFAULT_HOTSPORT_REMIND_SETTED_VALUE_VF = 0;
	/*guoxt 20170214 modify for 67305 end*/

    private static final String KEY_HOTSPORT_LAST_REMIND_DATE = "HotsportLastRemindDate";

    private static final String KEY_HOTSPORT_LAST_REMIND_TRAFFIC = "HotsportLastRemindTraffic";
    private static final float DEFAULT_HOTSPORT_LAST_REMIND_TRAFFIC = 0f;

    private SharedPreferences getHotSpotPreference(Context context) {
        return context.getSharedPreferences(HOTSPOT_MONITOR_PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isClosedRemind(Context context) {
        return getHotSpotMonitorSwitch(context) == 0;
    }

    private int getHotSpotMonitorSwitch(Context context) {
        /*guoxt 20170214 modify for 67305 begin*/
         /*guoxt 20170214 modify for  CSW1705AC-5 begin*/
        // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 begin
        if (Consts.gnVFflag || Consts.cyACflag|| Consts.cyEIFlag || Consts.cyBAFlag) {
            return getHotSpotPreference(context).getInt(KEY_HOTSPORT_REMIND_SETTED_INDEX,
                DEFAULT_HOTSPORT_REMIND_SETTED_INDEX_VF);
        }
        // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 end
        /*guoxt 20170214 modify for 67305 end*/
         /*guoxt 20170214 modify for  CSW1705AC-5 end*/
		
        return getHotSpotPreference(context).getInt(KEY_HOTSPORT_REMIND_SETTED_INDEX,
                DEFAULT_HOTSPORT_REMIND_SETTED_INDEX);
    }

//    public void setHotSpotMonitorSwitch(Context context, int value) {
//        // TODO need refactor
//
//    }

    public int getHotspotRemindSettingValue(Context context) {
        /*guoxt 20170214 modify for 67305 begin*/
         /*guoxt 20170214 modify for  CSW1705AC-5 begin*/
        // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 begin
         if (Consts.gnVFflag || Consts.cyACflag|| Consts.cyEIFlag || Consts.cyBAFlag) {
            return getHotSpotPreference(context).getInt(KEY_HOTSPORT_REMIND_SETTED_VALUE,
                DEFAULT_HOTSPORT_REMIND_SETTED_VALUE_VF);
        }
        // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 end
        /*guoxt 20170214 modify for 67305 end*/
         /*guoxt 20170214 modify for  CSW1705AC-5 end*/
        return getHotSpotPreference(context).getInt(KEY_HOTSPORT_REMIND_SETTED_VALUE,
                DEFAULT_HOTSPORT_REMIND_SETTED_VALUE);
    }

    public void setHotspotRemindSetted(Context context, int remindValue) {
        // TODO need refactor

    }

    public float getHotSpotStartTraffic(Context context, int simIndex) {
        return getHotSpotPreference(context).getFloat(KEY_HOTSPORT_LAST_REMIND_TRAFFIC + "_" + simIndex,
                DEFAULT_HOTSPORT_LAST_REMIND_TRAFFIC);
    }

    public void setHotSpotStartTraffic(Context context, float lastTraffic, int simIndex) {
        Editor prefEditor = getHotSpotPreference(context).edit();
        prefEditor.putFloat(KEY_HOTSPORT_LAST_REMIND_TRAFFIC + "_" + simIndex, lastTraffic);
        prefEditor.commit();
    }
}
