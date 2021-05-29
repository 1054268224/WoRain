//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;

public class TrafficPopWindows extends CyeeActivity {
    /*guoxt modify for oversea pop begin*/
    private Context mContext;
    private int mActivatedSimIndex;
    private TrafficCalibrateControler mTrafficCalibrateControler;

    // Gionee <yangxinruo> <2015-11-02> add for CR01602784 begin
    private CyeeAlertDialog mDialog;
    // Gionee <yangxinruo> <2015-11-02> add for CR01602784 end

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mTrafficCalibrateControler = TrafficCalibrateControler.getInstance(mContext);

        Intent intent = this.getIntent();
        mActivatedSimIndex = intent.getIntExtra("sim_activatedindex", 0);
        popDialog();
    }

    private void popDialog() {
        CyeeAlertDialog alertDialog = new CyeeAlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.mobile_data_stop_title))
                .setMessage(mContext.getString(R.string.mobile_data_stop_contetn))
                .setPositiveButton(mContext.getString(R.string.action_stop),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //guoxt modify begin
                                /*
                                SIMInfoWrapper.getDefault(mContext).setGprsEnable("setMobileDataEnabled",
                                        false);
                                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                        .putInt(TrafficPreference.getSimFlowlinkFlag(mActivatedSimIndex), 1)
                                        .commit();
                                 */
                                SIMInfoWrapper.getDefault(mContext).setGprsEnable("setMobileDataEnabled",
                                        false);
                                mTrafficCalibrateControler.setFlowlinkFlag(mContext, mActivatedSimIndex, 1);
                                //guoxt modify end 
                                TrafficPopWindows.this.finish();
                                // MobileTemplate.setMobileDataEnabled(mContext,
                                // false);
                            }
                        })
                .setNegativeButton(mContext.getString(R.string.action_restart),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TrafficPopWindows.this.finish();
                                //guoxt modify begin
                                mTrafficCalibrateControler.setFlowlinkFlag(mContext, mActivatedSimIndex, 2);
                                //guoxt modify end
/*
                                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                        .putInt(TrafficPreference.getSimFlowlinkFlag(mActivatedSimIndex), 2)
                                        .commit();
*/
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // TODO Auto-generated method stub
                                        // TrafficProcessorService.processIntent(mContext,
                                        // false);
                                    }
                                }).start();

                            }
                        }).create();

        alertDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    dialog.dismiss();
                    TrafficPopWindows.this.finish();
                }
                return false;
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
/*guoxt modify for oversea pop end*/
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end
