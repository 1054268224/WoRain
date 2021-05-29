// mengdw <2016-10-09> modify for CR01766193 begin
package com.cydroid.softmanager.trafficassistant.hotspotremind;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeButton;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import  com.cydroid.softmanager.utils.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.hotspotremind.controler.TrafficHotspotUsedRemindControler;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.TrafficSettingsActivity;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;

public class TrafficAutoCloseHotSpotRemindDialog extends CyeeActivity {
    private static final String TAG = "TrafficAutoCloseHotSpotRemindDialog";
    
    private Context mContext;
    // mengdw <2016-10-09> add for CR01766193 begin
    private TrafficHotspotUsedRemindControler mTrafficHotspotUsedRemindControler;
    // mengdw <2016-10-09> add for CR01766193 end
    private CyeeAlertDialog mDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_activity_dialog);
        mContext = this;
        mTrafficHotspotUsedRemindControler = TrafficHotspotUsedRemindControler.getInstance(mContext);
        createDialog();
    }

    // mengdw <2016-10-09> modify for CR01766193 begin
    private void createDialog() {
        String message = this.getResources().getString(R.string.hotspot_auto_close_message);
        int limitValue = mTrafficHotspotUsedRemindControler.getRemindLimit();
        CyeeAlertDialog.Builder dialogBuilder = new CyeeAlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.noti_softap_warning_title);
        dialogBuilder.setMessage(String.format(message, limitValue));
        dialogBuilder.setOnDismissListener(new DialogDismissListener());
        dialogBuilder.setNegativeButton(R.string.action_settings, new CyeeAlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //start setting 
                startOtherActivity(TrafficSettingsActivity.class);
                arg0.dismiss();
                finish();
            }
        });
        
        String kownTxt = this.getResources().getString(R.string.dialog_know);
        dialogBuilder.setNeutralButton(kownTxt, new CyeeAlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                finish();
            }
        });
        
        String restartTxt = this.getResources().getString(R.string.hotspot_restart);
        dialogBuilder.setPositiveButton(restartTxt, new CyeeAlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Log.d(TAG, "createDialog  PositiveButton click call setWifiApEnabled--true ");
                // mengdw <2017-05-08> add for 132020 begin
                ConnectivityManager manger = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                manger.startTethering(ConnectivityManager.TETHERING_WIFI, true, 
                        new OnStartTetheringCallback(), new Handler());
                // mengdw <2017-05-08> add for 132020 end
                //TrafficassistantUtil.setWifiApEnabled(mContext, true);
                arg0.dismiss();
                finish();
            }
        });
        mDialog = dialogBuilder.create();
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //dialog.setCancelable(false);
        mDialog.show();
    }
    
    private  void startOtherActivity(Class<?> cla) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(mContext, cla);
        mContext.startActivity(intent);
    }
    
    private class DialogDismissListener implements OnDismissListener {
        @Override
        public void onDismiss(DialogInterface dialog) {
            Log.d(TAG, "dialog onDismiss");
            TrafficAutoCloseHotSpotRemindDialog.this.finish();
        }
    }

    // mengdw <2017-05-08> add for 132020 begin
    //guoxt  20180717 add for Ptest begin
    public static final class OnStartTetheringCallback extends ConnectivityManager.OnStartTetheringCallback {
        @Override
        public void onTetheringStarted() {
            Log.d(TAG, "onTetheringStarted ");
        }

        @Override
        public void onTetheringFailed() {
            Log.d(TAG, "onTetheringFailed ");
        }
    }
    //guoxt  20180717 add for Ptest end
    // mengdw <2017-05-08> add for 132020 end
}
// mengdw <2016-10-09> modify for CR01766193 end

