//mengdw <2015-10-20> add for CR01571760 begin
package com.cydroid.softmanager.trafficassistant.hotspotremind;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeButton;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import com.cydroid.softmanager.utils.Log;
import android.content.Context;
import android.net.wifi.WifiManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.hotspotremind.controler.TrafficHotspotUsedRemindControler;
import com.cydroid.softmanager.trafficassistant.TrafficSettingsActivity;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;

public class TrafficHotSpotWarningDialog extends CyeeActivity {
    private static final String TAG = "TrafficHotSpotWarningDialog";
    // mengdw <2016-10-09> add for CR01766193 begin
    private static final int MESSAGE_UPDATE_CLOSE_BTN = 0;
    private static final int DELAY_TIME = 1 * 1000; //1s
    
    private Context mContext;
    private TrafficHotspotUsedRemindControler mTrafficHotspotUsedRemindControler;
    private UpdateHander mUpdateHander;
    private CyeeAlertDialog mDialog;
    private int mCloseTime = 60;
    // mengdw <2016-10-09> add for CR01766193 end
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_activity_dialog);
        mContext = this;
        mTrafficHotspotUsedRemindControler = TrafficHotspotUsedRemindControler.getInstance(mContext);
        mUpdateHander = new UpdateHander();
        createDialog();
        mUpdateHander.sendEmptyMessageDelayed(MESSAGE_UPDATE_CLOSE_BTN, DELAY_TIME);
    }
    
    // mengdw <2017-03-02> add for 76655 begin
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDialog != null && mDialog.isShowing()) {
            Log.d(TAG, "onDestroy dismiss dialog");
            mDialog.dismiss();
        }
        mDialog = null;
    }
    // mengdw <2017-03-02> add for 76655 end
    
    // mengdw <2016-10-09> modify for CR01766193 begin
    private void createDialog() {
        int useAgin = mTrafficHotspotUsedRemindControler.getUseAgainValue();
        String message = this.getResources().getString(R.string.noti_softap_warning_message);
        String useTxt = String.format(this.getResources().getString(R.string.noti_softap_use_agiain_txt), useAgin);
        int limitValue = mTrafficHotspotUsedRemindControler.getRemindLimit();
        String closeTxt = String.format(this.getResources().getString(R.string.noti_softap_close_text), mCloseTime);
        CyeeAlertDialog.Builder dialogBuilder = new CyeeAlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.noti_softap_warning_title);
        dialogBuilder.setMessage(String.format(message, limitValue));
        dialogBuilder.setNegativeButton(R.string.action_settings, new CyeeAlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //start setting 
                startOtherActivity(TrafficSettingsActivity.class);
                mTrafficHotspotUsedRemindControler.restartTrafficQuery();
                exit(arg0);
            }
        });
        
        dialogBuilder.setNeutralButton(useTxt, new CyeeAlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                mTrafficHotspotUsedRemindControler.selectUseAgain();
                exit(arg0);
            }
        });
        
        dialogBuilder.setPositiveButton(closeTxt, new CyeeAlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Log.d(TAG, "createDialog  PositiveButton click call setWifiApEnabled--false ");
                TrafficassistantUtil.setWifiApEnabled(mContext, false);
                exit(arg0);
            }
        });
        mDialog = dialogBuilder.create();
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mDialog.setCancelable(false);
        mDialog.show();
    }
    
    private void exit(DialogInterface arg0) {
        removeCloseMessage();
        arg0.dismiss();
        finish();
    }
    
    private void setCloseBtnTxt() {
        if(null == mDialog) {
            Log.d(TAG, "setCloseBtnTxt dialog is null error");
            return;
        }
        CyeeButton closeBtn = mDialog.getButton(CyeeAlertDialog.BUTTON_POSITIVE);
        if(closeBtn != null) {
            String closeTxt = String.format(this.getResources().getString(R.string.noti_softap_close_text), mCloseTime);
            closeBtn.setText(closeTxt);
        }
    }
    
    private void closeDialogByTimeout() {
        Log.d(TAG,"closeDialogByTimeout setWifiApEnabled--- -false");
        TrafficassistantUtil.setWifiApEnabled(mContext, false);
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        //start auto close dialog remimd
        startOtherActivity(TrafficAutoCloseHotSpotRemindDialog.class);
        finish();
    }
    
    private void removeCloseMessage() {
        if(mUpdateHander != null && mUpdateHander.hasMessages(MESSAGE_UPDATE_CLOSE_BTN)) {
            mUpdateHander.removeMessages(MESSAGE_UPDATE_CLOSE_BTN);
        }
    }
    
    private  void startOtherActivity(Class<?> cla) {
        Log.d(TAG, "startOtherActivity cla=" + cla);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(mContext, cla);
        mContext.startActivity(intent);
    }
    
    private class UpdateHander extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage msg=" + msg.what);
            if(MESSAGE_UPDATE_CLOSE_BTN == msg.what) {
                if(mCloseTime > 0) {
                    mCloseTime= mCloseTime - 1;
                    setCloseBtnTxt();
                    mUpdateHander.sendEmptyMessageDelayed(MESSAGE_UPDATE_CLOSE_BTN, DELAY_TIME);
                } else {
                    closeDialogByTimeout();
                }
            }
        }
    }
    // mengdw <2016-10-09> modify for CR01766193 end
}
//mengdw <2015-10-20> add for CR01571760 end

