package com.cydroid.powersaver.launcher.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.android.internal.telephony.TelephonyIntents;
import com.cydroid.powersaver.launcher.util.Log;

import java.util.List;

import cyee.widget.CyeeSwitch;

/**
 * Created by xionghg on 17-4-12.
 */

public class MobileDataEnabler {
    private static final String TAG = "Main_Enabler";

    private final Context mContext;
    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;
    private RelativeLayout mMobileView;
    private CyeeSwitch mSwitch;
    protected boolean mSwitchState;

    public MobileDataEnabler(Context context, RelativeLayout container, CyeeSwitch data) {
        mContext = context;
        mMobileView = container;
        mSwitch = data;
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
    }

    public void resume() {
        setSwitchStatus();
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: new value: " + isChecked);
                mSwitchState = isChecked;
                setMobileData(mSwitchState);
            }
        });
        mMobileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitchState = !mSwitchState;
                Log.d(TAG, "mMobileView onClick, mobile data " + (mSwitchState ? "on" : "off"));
                mSwitch.setChecked(mSwitchState);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
    }

    private void setSwitchStatus() {
        // Chenyee liu_shuang 20180505 modify for CSW1707A-797 begin
        if (hasSimCard() && !isAirplaneModeOn(mContext)) {
            mMobileView.setEnabled(true);
            mSwitch.setEnabled(true);
            mSwitchState = isMobileDataOn();
            mSwitch.setChecked(mSwitchState);
        } else {
            mSwitch.setChecked(false);
            mMobileView.setEnabled(false);
            mSwitch.setEnabled(false);
        }

        //mSwitchState = isMobileDataOn();
        //mSwitch.setChecked(mSwitchState);
        // Chenyee liu_shuang 20180505 modify for CSW1707A-797 end
    }

    // Chenyee xionghg 20180302 add for CSW1705A-811 begin
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive action: " + action + "");
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)){
                setSwitchStatus();
            }
        }
    };

    public boolean hasSimCard() {
        int simState = mTelephonyManager.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:  // 没有SIM卡
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        Log.d(TAG, "hasSimCard: simState=" + simState + ", result=" + result);
        return result;
    }
    // Chenyee xionghg 20180302 add for CSW1705A-811 end

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
        mMobileView.setOnClickListener(null);

        mContext.unregisterReceiver(mReceiver);
    }

    public int getSimCount() {
        List<SubscriptionInfo> subInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
        int count = (subInfoList == null) ? 0 : subInfoList.size();
        return count;
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public boolean isMobileDataOn() {
        return mTelephonyManager.getDataEnabled();
    }

    private void setMobileData(boolean enabling) {
        mTelephonyManager.setDataEnabled(enabling);
        mSwitch.setChecked(enabling);
    }

}
