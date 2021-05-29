//Gionee: mengdw <2016-03-04> add for CR01640989 begin
package com.cydroid.softmanager.trafficassistant.controler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification.BigTextStyle;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.text.Html;

import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity;
import com.cydroid.softmanager.trafficassistant.hotspotremind.controler.TrafficHotspotUsedRemindControler;
import com.cydroid.softmanager.trafficassistant.model.TrafficSettingUpdateUiData;
import com.cydroid.softmanager.trafficassistant.SellUtils;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.StringFormat;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficPreference;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.trafficassistant.TrafficMonitorBroadcastReceiver;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.common.Consts;
import android.graphics.Color;
import com.cydroid.softmanager.trafficassistant.TrafficSettingsActivity;

public class TrafficSettingControler {
    public static final int NOTIFI_TYPE_NO_SETTED = 0;
    public static final int NOTIFI_TYPE_SURPLUS = 1;
    public static final int NOTIFI_TYPE_EXCEED = 2;

    public static final int HOTSPORT_REMIND_CLOSE = 0;
    
    // mengdw <2016-11-29> add for CR01766193 begin
    private static final int DEFAULT_SELECT_INDEX = 1;
    // mengdw <2016-11-29> add for CR01766193 end
    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    private static final int LOCK_SCREEN_REMIND_DEFAULT_INDEX = 0;
    private static final int LOCK_SCREEN_REMIND_CLOSE = 0;
    // Gionee: mengdw <2016-12-06> add for CR01775579 end
    // Gionee: mengdw <2017-04-21> add for 120343 begin
    private static final int RESIDENT_TRAFFIC_NOTIFICATION_ID = R.string.trafficassistant_app_name + 100;
    // Gionee: mengdw <2017-04-21> add for 120343 end
    
    private final Context mContext;
    private static final String TAG = "TrafficSettingControler";
    // Gionee: mengdw <2016-05-03> modify for CR01684930 begin
    private  static final int MESSAGE_UPDATE__UI = 0;
    // Gionee: mengdw <2016-07-06> add for CR01728114 begin
    private static final int MESSAGE_COMMIT_NOTINICATION = 1;
    private static final int DELAY_TIME = 60000; //1
    // Gionee: mengdw <2016-07-06> add for CR01728114 end
    private final UpdateUiHander mUpdateUiHander;
    // Gionee: mengdw <2016-05-03> modify for CR01684930 end
    private static final int ENTRY_BUY_VALUE = 5;
    private static TrafficSettingControler sInstance;

    private final TrafficHotspotUsedRemindControler mTrafficHotspotUsedRemindControler;
    // Gionee: mengdw <2017-04-21> add for 120343 begin
    private final NotificationManager mNotificationManager;
    // Gionee: mengdw <2017-04-21> add for 120343 end
    
    public static TrafficSettingControler getInstance(Context context) {
        Log.d(TAG," TrafficSettingControler getInstance context=" + context);
        synchronized (TrafficSettingControler.class) {
            if (sInstance == null) {
                sInstance = new TrafficSettingControler(context);
            }
        }
        return sInstance;
    }
    
    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    public int getLockScreenRemindDefaultIndex() {
        return LOCK_SCREEN_REMIND_DEFAULT_INDEX;
    }
    
    public void saveLockScreenRemindSetting(int selectedIndex) {
        TrafficPreference.saveLockScreenSettingPreference(mContext, selectedIndex);
    }
    
    public int getLockScreenRemindSetting() {
        return TrafficPreference.getLockScreenSettingPreference(mContext, LOCK_SCREEN_REMIND_DEFAULT_INDEX);
    }
    
    public boolean isLockScreenRemindClose() {
        return LOCK_SCREEN_REMIND_CLOSE == getLockScreenRemindSetting();
    }
    
    public long getLockScreenRemindLimit() {
        long limit = 0;
        int selectIndex = getLockScreenRemindSetting();
        switch (selectIndex) {
            case 0:
                break;
            case 1:
                limit = 100 * Constant.KB;
                break;
            case 2:
                limit = 1 * Constant.MB;
                break;
            case 3:
                limit = 5 * Constant.MB;
                break;
            default:
                Log.d(TAG, "getLockScreenRemindLimit error selectIndex");
                break;
        }
        return limit;
    }
    // Gionee: mengdw <2016-12-06> add for CR01775579 end
    
    
    public boolean isCalibrateProvinceSetted(int simIndex) {
        int provinceIndex = TrafficPreference.getSimIntPreference(mContext, simIndex, 
                TrafficPreference.KEY_CALIBRATE_PROVINCE_SETTING, TrafficPreference.CALIBRATE_NO_SETTING);
        boolean result = provinceIndex != TrafficPreference.CALIBRATE_NO_SETTING;
        return result;
    }
    
    public boolean isCalibrateBrandSetted(int simIndex) {
        int brandIndex = TrafficPreference.getSimIntPreference(mContext, simIndex, 
                TrafficPreference.KEY_CALIBRATE_BRAND_SETTING, TrafficPreference.CALIBRATE_NO_SETTING);
        boolean result = brandIndex != TrafficPreference.CALIBRATE_NO_SETTING;
        return result;
    }
    
    public void setCalibrateProvince(int simIndex, int province) {
        TrafficPreference.setSimIntPreference(mContext, simIndex, 
                TrafficPreference.KEY_CALIBRATE_PROVINCE_SETTING, province);
    }
    
    public int getSettedCalibrateProvince(int  simIndex) {
        int provinceIndex = TrafficPreference.getSimIntPreference(mContext, simIndex, 
                TrafficPreference.KEY_CALIBRATE_PROVINCE_SETTING, TrafficPreference.CALIBRATE_NO_SETTING);
        return provinceIndex;
    }
    
    public void setCalibrateBrand(int simIndex, int brand) {
        TrafficPreference.setSimIntPreference(mContext, simIndex, 
                TrafficPreference.KEY_CALIBRATE_BRAND_SETTING, brand);
    }
    
    public int getSettedCalibrateBrand(int  simIndex) {
        int brandIndex = TrafficPreference.getSimIntPreference(mContext, simIndex, 
                TrafficPreference.KEY_CALIBRATE_BRAND_SETTING, TrafficPreference.CALIBRATE_NO_SETTING);
        return brandIndex;
    }
    
    public void setHotSportRemidSettedIndex(Context context, int index) {
        TrafficPreference.setIntPreference(context, 
                TrafficPreference.KEY_HOTSPORT_REMIND_SETTED_INDEX, index);
    }
    
    public int getHotSportRemidSettedIndex(Context context) {
        /*guoxt 20170214 modify for 67305 begin*/
         /*guoxt 20170214 modify for  CSW1705AC-5 begin*/
        // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 begin
        if (Consts.gnVFflag || Consts.cyACflag || Consts.cyEIFlag) {
            return TrafficPreference.getIntPreference(context, 
                TrafficPreference.KEY_HOTSPORT_REMIND_SETTED_INDEX, 0);
            }
        // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 end
        /*guoxt 20170214 modify for 67305 end*/
         /*guoxt 20170214 modify for  CSW1705AC-5 end*/
        return TrafficPreference.getIntPreference(context, 
                TrafficPreference.KEY_HOTSPORT_REMIND_SETTED_INDEX, DEFAULT_SELECT_INDEX);
    }
    
    public void setHotSportRemindSettedValue(Context context, int value) {
        TrafficPreference.setIntPreference(context, 
                TrafficPreference.KEY_HOTSPORT_REMIND_SETTED_VALUE, value);
    }
    
    public int getHotSportRemindSettedValue(Context context) {
        /*guoxt 20180326 modify for 67305 begin*/
		 /*guoxt 20170214 modify for  CSW1705AC-5 begin*/
        // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 begin
        if (Consts.gnVFflag || Consts.cyACflag|| Consts.cyEIFlag) {
            return TrafficPreference.getIntPreference(context, 
                TrafficPreference.KEY_HOTSPORT_REMIND_SETTED_VALUE, 0);
            }
        // Chenyee <CY_Oversea_Req> zhaopeng 20180427 add for CSW1703EI-20 end
        /*guoxt 20170214 modify for 67305 end*/
         /*guoxt 20170214 modify for  CSW1705AC-5 end*/
        return TrafficPreference.getIntPreference(context, 
                TrafficPreference.KEY_HOTSPORT_REMIND_SETTED_VALUE, 10);
    }
    
    public void setHotSportLastRemindDate(Context context, int simIndex, String date) {
        TrafficPreference.setSimStringPreference(context, simIndex, 
                TrafficPreference.KEY_HOTSPORT_LAST_REMIND_DATE, date);
    }
    
    public String getHotSportLastRemindDate(Context context, int simIndex) {
        return TrafficPreference.getSimStringPreference(context, simIndex, 
                TrafficPreference.KEY_HOTSPORT_LAST_REMIND_DATE, TimeFormat.getNowDate());
    }
    
    public void setHotSportLastRemindTraffic(Context context, int simIndex, float traffic) {
        TrafficPreference.setSimFloatPreference(context, simIndex, 
                TrafficPreference.KEY_HOTSPORT_LAST_REMIND_TRAFFIC, traffic);
    }
    
    public float getHotSportLastRemindTraffic(Context context, int simIndex) {
        return TrafficPreference.getSimFloatPreference(context, simIndex, 
                TrafficPreference.KEY_HOTSPORT_LAST_REMIND_TRAFFIC, 0);
    }

    public void setNotificationInfoSwtich(Context context, boolean value) {
        TrafficPreference.setBooleanPreference(context, 
                TrafficPreference.KEY_NOTIFICATION_TRAFFIC_INFO, value);
    }
    
    public boolean isNotificationInfoSwtichOpen(Context context) {
        boolean notificationInfoSwitch = true;
        //Gionee <GN_Oversea_Req> <xionghg> <20170620> modify for 160257 begin
        //guoxt modify for CR01775776  57280 begin
        //Gionee <GN_Oversea_Req> <lucy> <20170424> add for 123274 begin
        if (Consts.gnSyFlag || Consts.gnVFflag || Consts.gnTCflag || Consts.gnGIFlag ||
                Consts.gnQMflag || Consts.gnIPFlag || Consts.gnDPFlag || Consts.cyBAFlag) {
            notificationInfoSwitch = TrafficPreference.getBooleanPreference(context,
                    TrafficPreference.KEY_NOTIFICATION_TRAFFIC_INFO, false);
        } else {
            notificationInfoSwitch = TrafficPreference.getBooleanPreference(context,
                    TrafficPreference.KEY_NOTIFICATION_TRAFFIC_INFO, true);
        }
        //Gionee <GN_Oversea_Req> <lucy> <20170424> add for 123274 end
        //guoxt modify for CR01775776 57280  end
        //Gionee <GN_Oversea_Req> <xionghg> <20170620> modify for 160257 end
        return notificationInfoSwitch;
    }
    
    // Gionee: mengdw <2016-07-06> add for CR01728114 begin
    public void startNotiActionMonitor() {
        if (!Consts.cyBAFlag){
            mUpdateUiHander.sendEmptyMessageDelayed(MESSAGE_COMMIT_NOTINICATION, DELAY_TIME);
        }
    }
    // Gionee: mengdw <2016-07-06> add for CR01728114 end
    
    // Gionee: mengdw <2016-05-03> modify for CR01684930 begin
    public void commitTrafficNotiAction(Context context) {
        commitNotificationController(isNotificationInfoSwtichOpen(context));
    }
    
    // mengdw <2016-10-09> add for CR01766193 begin
    public boolean isSettingValueLargeCurrentTraffic(Context context, int settingValue) {
        boolean result = false;
        float diffTraffic = mTrafficHotspotUsedRemindControler.getDiffHotspotTraffic();
        float settingTraffic = settingValue * Constant.UNIT * Constant.UNIT;
        Log.d(TAG, "isSettingValueLargeCurrentTraffic curTraffic=" + diffTraffic + " settingTraffic=" + settingTraffic);
        if(settingTraffic > diffTraffic) {
            result = true;
        }
        return result;
    }
    
    public void restartSettingValue(int settingValue) {
        mTrafficHotspotUsedRemindControler.restartSettingValue(settingValue);
    }
    
    public float getCurrentTraffic() {
        float diffTraffic = mTrafficHotspotUsedRemindControler.getDiffHotspotTraffic();
        float result = diffTraffic /  Constant.UNIT /  Constant.UNIT;
        Log.d(TAG, " getCurrentTraffic curTraffic=" + diffTraffic + " result=" + result);
        return result;
    }
    // mengdw <2016-10-09> add for CR01766193 end
    
    public void commitNotificationController(final boolean checked) {
        int activateSindex =getActivateSim();
        int powerMode = Global.getInt(mContext.getContentResolver(), PowerConsts.POWER_MODE, 0);
        int notificationType = getNotificationType();
		//guoxt modify begin 
        String clickBuyString = mContext.getString(R.string.nogi_entry_buy_text);
		clickBuyString = "";
		//guoxt modify end
        // Gionee: mengdw <2016-05-18> modify for CR01696960 begin
        String notifiFlow = "";
        boolean isStartBuy = false;
        if (notificationType != NOTIFI_TYPE_NO_SETTED) {
            isStartBuy = isStartBuyActivity(activateSindex);
            notifiFlow = isStartBuy ? String.format("%s%s", getNotificationFlow(activateSindex),
                    clickBuyString) : getNotificationFlow(activateSindex);
        }
        // Gionee: mengdw <2016-05-18> modify for CR01696960 end
        Log.d(TAG, "commitNotificationController activateSindex=" + activateSindex +
                " notificationType=" + notificationType + " checked=" + checked);
        // Gionee: mengdw <2017-04-22> add for 121836 begin
        sendUpdateTrafficDataMessage(activateSindex, notifiFlow, notificationType, powerMode, 
                isStartBuy, checked);
        // Gionee: mengdw <2017-04-22> add for 121836 end
    }
    // Gionee: mengdw <2016-05-03> modify for CR01684930 end
    
    // Gionee: mengdw <2017-04-22> add for 121836 begin
    private void sendUpdateTrafficDataMessage(final int activateSindex, final String notifiFlow, final int notificationType,
            final int powerMode, final boolean isStartBuy, final boolean checked) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                TrafficSettingUpdateUiData uiData = new TrafficSettingUpdateUiData();
                uiData.setTodayFlow(getTodayFlow(activateSindex));
                uiData.setTotalFlow(getTotalFlow(activateSindex));
                uiData.setNotificationType(notificationType);
                uiData.setNotifiFlow(notifiFlow);
                uiData.setSimCount(TrafficassistantUtil.getSimCount(mContext));
                uiData.setPowerMode(powerMode);
                uiData.setActivateSindex(activateSindex);
                uiData.setIsChecked(checked);
                uiData.setIsStartBuyActivity(isStartBuy);
                msg.what = MESSAGE_UPDATE__UI;
                msg.obj = uiData;
                Log.d(TAG, "sendUpdateTrafficDataMessage to main thread");
                mUpdateUiHander.sendMessage(msg);
            }
        });
        thread.start();
    }

    // Gionee: mengdw <2017-04-22> add for 121836 end
    
    // Gionee: mengdw <2016-05-18> modiy for CR01696960 begin
    public int getNotificationType() {
        int activateSindex =getActivateSim();
        // Gionee: mengdw <2016-04-25> modify for CR01684752 begin
        boolean isSetted = TrafficPreference.getSimBooleanPreference(mContext, activateSindex,
                TrafficPreference.KEY_TRAFFIC_PACKAGE_SETTED_FLAG, false);
        // /Gionee: mengdw <2016-04-25> modify for CR01684752 end
        Log.d(TAG, "getNotificationType  isSetted=" + isSetted +  " activateSindex=" + activateSindex);
        if (isSetted) {
            TrafficCalibrateControler trafficCalibrateControler = TrafficCalibrateControler.getInstance(mContext);
            float left = trafficCalibrateControler.getCommonLeftTraffic(mContext, activateSindex);
            if (left >= 0) {
                return NOTIFI_TYPE_SURPLUS;
            } else {
                return NOTIFI_TYPE_EXCEED;
            }
        } else {
            return NOTIFI_TYPE_NO_SETTED;
        }
    }
    // Gionee: mengdw <2016-05-18> modiy for CR01696960 end
    
    public String getNotificationFlow(int simIndex) {
        TrafficCalibrateControler trafficCalibrateControler = TrafficCalibrateControler.getInstance(mContext);
        float flow = trafficCalibrateControler.getCommonLeftTraffic(mContext, simIndex);
        return StringFormat.getUnitStringByValue(Math.abs(flow) * Constant.MB, 1);
    }
    
    private TrafficSettingControler(Context context) {
        mContext = context.getApplicationContext();
        // Gionee: mengdw <2017-04-21> add for 120343 begin
        mNotificationManager = (NotificationManager) mContext
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        /*guoxt 2018-03-13 modify for  CSW1705A-2234 begin */
        //ChannelId为"1",ChannelName为"traffic"
        NotificationChannel channel = new NotificationChannel("1",
                "traffic", NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true); //是否在桌面icon右上角展示小红点
        channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
        channel.setLightColor(Color.parseColor("#00a6ce"));
        mNotificationManager.createNotificationChannel(channel);
         /*guoxt 2018-03-13 modify for  CSW1705A-2234 end */

        // Gionee: mengdw <2017-04-21> add for 120343 end
        mUpdateUiHander = new UpdateUiHander();
        mTrafficHotspotUsedRemindControler = TrafficHotspotUsedRemindControler.getInstance(context);
    }
    
    private boolean isStartBuyActivity(int simIndex) {
        TrafficCalibrateControler trafficCalibrateControler = TrafficCalibrateControler.getInstance(mContext);
        float left = trafficCalibrateControler.getCommonLeftTraffic(mContext, simIndex);
        boolean isSetted = trafficCalibrateControler.isTafficPackageSetted(mContext, simIndex);
        return left < ENTRY_BUY_VALUE && isSetted;
    }

    private String getTodayFlow(int simIndex) {
        int[] todayDate = TimeFormat.getNowTimeArray();
        long startTime = TimeFormat.getStartTime(todayDate[0], todayDate[1] + 1, todayDate[2], 0, 0, 0);
        String todayFlow = TrafficassistantUtil.getTrafficString(mContext, simIndex, startTime, 0, 0);
        //fengpeipei modify for 61661 start
        //return TrafficassistantUtil.replaceUnit(todayFlow);
        return todayFlow;
        //fengpeipei modify for 61661 end
    }

    // Gionee: mengdw <2016-05-18> add for CR01696960 begin
    private String getTotalFlow(int simIndex) {
        float total =  TrafficPreference.getSimIntPreference(mContext, simIndex, TrafficPreference.KEY_COMMON_TOTAL, 0);
        return StringFormat.getUnitStringByValue(Math.abs(total) * Constant.MB, 0);
    }
    // Gionee: mengdw <2016-05-18> add for CR01696960 end
    
	// Gionee: mengdw <2016-07-06> add for CR01728114 begin
	private int getActivateSim() {
		int simIndex = TrafficassistantUtil.getActivatedSimCardNo(mContext);
		// Gionee: mengdw <2016-08-27>modify for CR01751383 begin
		int activateSindex = simIndex > 0 ? simIndex : getDefaultActivateSim();
		// Gionee: mengdw <2016-08-27>modify for CR01751383 end
		Log.d(TAG, "getActivateSim simIndex=" + simIndex + " activateSindex="
				+ activateSindex);
		return activateSindex;
	}
    
	// Gionee: mengdw <2016-08-27>add for CR01751383 begin
    private int getDefaultActivateSim() {
    	int simIndex = 0;
		try {
			SubscriptionManager subManager = SubscriptionManager.from(mContext);
			SubscriptionInfo subInfo = subManager.getDefaultDataSubscriptionInfo();
			if(subInfo!=null) {
                simIndex = subInfo.getSimSlotIndex();
            }
			Log.d(TAG, "getDefaultActivateSim subInfo= " + subInfo + " simIndex=" + simIndex);
		} catch (Exception e) {
			Log.d(TAG, "getDefaultActivateSim Exception e=" + e.toString());
		}
    	return simIndex;
    }
	// Gionee: mengdw <2016-08-27>add for CR01751383 end
    
    // private Class<?> getJumpClass(int noifiType, boolean isStartBuyActivity) {
    //     Class<?> cls = com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity.class;
    //     if (noifiType != NOTIFI_TYPE_NO_SETTED) {
    //         /*guoxt modify for 4.1.1  begin */
    //         cls = isStartBuyActivity ? com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity.class
    //                 : com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity.class;
    //         /*guoxt modify for 4.1.1  end */
    //     }
    //     return cls;
    // }
    // Gionee: mengdw <2016-07-06> add for CR01728114 end

    // Gionee: mengdw <2017-04-21> add for 120343 begin
    private void showNotification(TrafficSettingUpdateUiData uiData) {
        Log.d(TAG, "showNotification uiData=" + uiData);
        PendingIntent notificationIntent = getPendingIntent(uiData);
        //Chenyee guoxt modify for CSW1705A-2570 begin
        PendingIntent closeIntent = getClosePendingIntent();
        Resources res = mContext.getResources();
        //Chenyee guoxt modify for CSW1705A-2570 end

        BigTextStyle bigStyle = new BigTextStyle();
        String title = String.format("%s %s", mContext.getString(R.string.noti_today), 
                uiData.gettTodayFlow());
        bigStyle.setBigContentTitle(title);
        String message = getNotiMessage(uiData.getNotificationType(), uiData.getNotifiFlow());
        Log.d(TAG, "showNotification message=" + message);
        bigStyle.bigText(Html.fromHtml(message));
        /*guoxt 2018-03-13 modify for  CSW1705A-2234 begin */
        Notification.Builder builder = new Notification.Builder(mContext,"1").setStyle(bigStyle)
                .setVisibility(Notification.VISIBILITY_PUBLIC).setSmallIcon(R.drawable.notify)
                // Gionee: mengjk  modify for notify TitleIcon And TitleText Color Change
                .setAutoCancel(false).setColor(mContext.getResources().getColor(R.color.notify_icon_text_color))
                .setContentIntent(notificationIntent)
                .setContentTitle(title)
                .setContentText(Html.fromHtml(message));
        /*guoxt 2018-03-13 modify for  CSW1705A-2234 end */
        //Chenyee guoxt modify for CSW1705A-2570 begin
        builder.addAction(0, res.getString(R.string.autoboot_forbidden_notify_turn_off),
                closeIntent);
        //Chenyee guoxt modify for CSW1705A-2570 end
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(RESIDENT_TRAFFIC_NOTIFICATION_ID, notification);
    }
    
    private PendingIntent getPendingIntent(TrafficSettingUpdateUiData uiData) {
        // Chenyee xionghg 20180103 modify for CSW1702A-742 clean code begin
        // Class<?> cls = getJumpClass(uiData.getNotificationType(), uiData.isStartBuyActivity());
        // Log.d(TAG, "getPendingIntent cls=" + cls);
        Class<?> cls = TrafficAssistantMainActivity.class;
        // Chenyee xionghg 20180103 modify for CSW1702A-742 clean code end
        Intent intent = new Intent(mContext, cls);
        intent.putExtra(SellUtils.KEY_ACTIVITY_FORM, SellUtils.ACTIVITY_FORM_NOTIFICATION);
        intent.putExtra(SellUtils.KEY_START_SOURCE, SellUtils.BUY_SOURCE.SOURCE_MONITOR.ordinal());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
    //Chenyee guoxt modify for CSW1705A-2570 begin
    private PendingIntent getClosePendingIntent() {
        Intent turnOffIntent = new Intent();
        turnOffIntent.setClass(mContext, TrafficMonitorBroadcastReceiver.class);
        turnOffIntent.setAction(TrafficMonitorBroadcastReceiver.ACTION_TURN_OFF_DATA_NOTI);
        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(mContext,
                0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT);

        return turnOffPendingIntent;
    }
    //Chenyee guoxt modify for CSW1705A-2570 end
    
    private String getNotiMessage(int notiType, String notiFlow) {
        String message = "";
        switch (notiType) {
            case NOTIFI_TYPE_NO_SETTED:
                message = mContext.getString(R.string.noti_no_settings);
                break;
            case NOTIFI_TYPE_SURPLUS:
                message = String.format("<font color='#789440' >%s %s</font>", 
                        mContext.getString(R.string.noti_surplus), notiFlow);
                break;
            case NOTIFI_TYPE_EXCEED:
                message = String.format("<font color='red' >%s %s</font>",
                        mContext.getString(R.string.noti_exceed), notiFlow);
                break;
            default:
                Log.d(TAG, "error notiType");
                break;
        }
        return message;
    }

    public void cancelNotification() {
        mNotificationManager.cancel(RESIDENT_TRAFFIC_NOTIFICATION_ID);
    }
    // Gionee: mengdw <2017-04-21> add for 120343 end

    class UpdateUiHander extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG,"UpdateUiHander handleMessage msg=" + msg.what);
            // Gionee: mengdw <2016-07-06> modify for CR01728114 begin
            if (MESSAGE_UPDATE__UI == msg.what) {
                TrafficSettingUpdateUiData uiData = (TrafficSettingUpdateUiData) msg.obj;
                Log.d(TAG, "handleMessage uiData=" + uiData);
                if (uiData != null) {
                    // Gionee: mengdw <2017-04-21> add for 120343 begin
                    if (uiData.ischecked() && uiData.getSimCount() > 0 && uiData.getPowerMode() != 2) {
                        showNotification(uiData);
                    } else {
                        cancelNotification();
                    }
                    // Gionee: mengdw <2017-04-21> add for 120343 end
                }
            } else if (MESSAGE_COMMIT_NOTINICATION == msg.what) {
                commitTrafficNotiAction(mContext);
                mUpdateUiHander.sendEmptyMessageDelayed(MESSAGE_COMMIT_NOTINICATION, DELAY_TIME);
            }
            // Gionee: mengdw <2016-07-06> add for CR01728114 end
        }
    }
    // Gionee: mengdw <2016-05-03> modify for CR01684930 end
}
//Gionee: mengdw <2016-03-04> add for CR01640989 end
