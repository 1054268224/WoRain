// Gionee <liuyb> <2014-2-25> add for CR01083582 begin
package com.cydroid.softmanager.monitor;

import java.lang.reflect.Method;

import cyee.app.CyeePrt;
//import com.cydroid.softmanager.monitor.utils.CyeePrt;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.cydroid.softmanager.R;

import com.cydroid.softmanager.monitor.interfaces.IMonitorJob;
import com.cydroid.softmanager.trafficassistant.NotificationController;
import com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity;
import com.cydroid.softmanager.trafficassistant.TrafficPopWindows;
import com.cydroid.softmanager.trafficassistant.utils.TrafficPreference;
import com.cydroid.softmanager.utils.Log;

//Gionee <jianghuan> <2014-04-01> modify for CR01097010 begin
public class RootMonitor implements IMonitorJob {

    private static final String TAG = "RootMonitor";
    public static final String ROOT_LEVEL = "root_level";
    public static final String ROOT = "root";
    private Context mContext;

    @Override
    public void setExecTime(int hour, int minutes) {
        // TODO Auto-generated method stub
    }

    @Override
    public void execute(Context context) {
        // TODO Auto-generated method stub

        mContext = context;
        SharedPreferences share = mContext.getSharedPreferences(ROOT, Context.MODE_MULTI_PROCESS);
        boolean rootFlag = share.getBoolean(ROOT, false);
        int rootLevel = share.getInt(ROOT_LEVEL, 0);
        int currRootState = getCurrRootState();
        Log.d(TAG, "currRootState :" + currRootState + " rootFlag :" + rootFlag + " rootLevel :" + rootLevel);

        if (0 == currRootState) {
            commitSharePreBooleanValue(share, ROOT, false);
            commitSharePreIntValue(share, ROOT_LEVEL, 0);
            if (2 == rootLevel) {
                sendNotificationForOtaRepairSuccess(mContext);
            }
            return;
        }

        if (!rootFlag) {
            commitSharePreBooleanValue(share, ROOT, true);
        }

        if (!rootFlag || currRootState != rootLevel) {

            if (1 == currRootState) {
                commitSharePreIntValue(share, ROOT_LEVEL, 1);
                startActivity(mContext, 1);
                // YouJuAgent.onEvent(context, "Root");
            } else if (2 == currRootState) {
                commitSharePreIntValue(share, ROOT_LEVEL, 2);
                startActivity(mContext, 2);
                // YouJuAgent.onEvent(context, "RootFile");
            }
        }
    }


    public int getCurrRootState() {
        //guoxt modify for overseaO begin
        int result = 0;
        //return CyeePrt.nativeCheckIfRoot();
        try{
            result = CyeePrt.nativeCheckIfRoot();

        }catch (Exception e){
            Log.d(TAG, e.toString()); 
  
        }
      return result;
     //guoxt modify for overseaO end
        /*
         * int result = 0; try{ CyeePrt prt = new CyeePrt(); Class cls =
         * Class.forName("cyee.app.CyeePrt"); Method method =
         * cls.getMethod("nativeCheckIfRoot"); Object object =
         * method.invoke(prt); result = Integer.valueOf(object.toString());
         * Log.d(TAG, "success result :" + result); } catch (Exception e) {
         * Log.d(TAG, e.toString()); } Log.d(TAG, "fail result :" + result);
         * return result;
         */
    }

    public boolean isRooted() {
        return getCurrRootState() != 0;
    }

    public void startActivity(Context context, int flag) {
        Intent intent = new Intent(context, RootWarningWindow.class);
        intent.putExtra("rootflag", flag);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void commitSharePreBooleanValue(SharedPreferences share, String key, boolean value) {
        share.edit().putBoolean(key, value).commit();
    }

    private void commitSharePreIntValue(SharedPreferences share, String key, int value) {
        share.edit().putInt(key, value).commit();
    }

    private void sendNotificationForOtaRepairSuccess(Context context) {

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(R.string.root_repair_title);

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.ota_risk_relieve_title))
                .setContentText(context.getString(R.string.ota_risk_relieve_content))
                // Gionee: mengjk  modify for notify TitleIcon And TitleText Color Change
                .setContentIntent(contentIntent).setSmallIcon(R.drawable.notify).setColor(context.getResources().getColor(R.color.notify_icon_text_color))
                .setWhen(System.currentTimeMillis())
                .setTicker(context.getString(R.string.ota_risk_relieve_title)).setAutoCancel(true);

        // builder.setDefaults(Notification.DEFAULT_SOUND);

        Notification notification = builder.getNotification();

        notificationManager.notify(R.string.root_repair_title, notification);

        // YouJuAgent.onEvent(context, "RootFile_Recover");
    }
    // Gionee <jianghuan> <2014-04-01> modify for CR01097010 end
}
// Gionee <liuyb> <2014-2-25> add for CR01083582 end
