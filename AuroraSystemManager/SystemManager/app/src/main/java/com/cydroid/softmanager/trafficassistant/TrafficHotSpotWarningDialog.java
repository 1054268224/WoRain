//mengdw <2015-10-20> add for CR01571760 begin
package com.cydroid.softmanager.trafficassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import  com.cydroid.softmanager.utils.Log;
import android.view.WindowManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;

public class TrafficHotSpotWarningDialog extends CyeeActivity {
    private static final String TAG = "TrafficHotSpotWarningDialog";

    private CyeeAlertDialog dialog;
    private static final int HOTSPORT_TRAFFIC_SET_SELECT_VALUE = 0;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_activity_dialog);
        mContext = this;
        createDialog();
    }

    private void createDialog() {
        TrafficSettingControler trafficSettingControler = TrafficSettingControler.getInstance(mContext);
        int hotSportRemindValue = trafficSettingControler.getHotSportRemindSettedValue(mContext);
        int hotSportRemindIndex = trafficSettingControler.getHotSportRemidSettedIndex(mContext);
        String message = this.getResources().getString(R.string.noti_softap_warning_message);
        Log.d(TAG, "createDialog hotSportTrafficSetting value=" + hotSportRemindValue + " index=" + hotSportRemindIndex);
        CyeeAlertDialog.Builder b = new CyeeAlertDialog.Builder(this);
        b.setTitle(R.string.noti_softap_warning_title);
        b.setMessage(String.format(message, hotSportRemindValue + "M"));
        b.setNegativeButton(R.string.noti_softap_setting_cancel, new CyeeAlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                finish();
            }
        });
        b.setPositiveButton(R.string.noti_softap_close_text, new CyeeAlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Log.d(TAG, "createDialog  PositiveButton click call setWifiApEnabled--false ");
                //guoxt  20180717 add for Ptest begin
                // WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                // guoxt modify for  <2018-07-18> add for CSW1705A-2751 begin
                TrafficassistantUtil.setWifiApEnabled(mContext,false);
                // guoxt modify for  <2018-07-18> add for CSW1705A-2751end
              //  manager.setWifiApEnabled(null, false);
                //guoxt  20180717 add for Ptest end
                arg0.dismiss();
                finish();
            }
        });
        dialog = b.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setCancelable(false);
        dialog.show();
    }
}
//mengdw <2015-10-20> add for CR01571760 end

