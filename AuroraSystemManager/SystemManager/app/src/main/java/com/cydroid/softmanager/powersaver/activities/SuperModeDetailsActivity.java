package com.cydroid.softmanager.powersaver.activities;

import cyee.changecolors.ChameleonColorManager;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.utils.BatteryStateInfo;
import com.cydroid.softmanager.powersaver.utils.PowerTimer;
import com.cydroid.softmanager.utils.Log;
import android.content.res.Resources;

public class SuperModeDetailsActivity extends BaseActivity {
    private Context mContext;
    private TextView mTimeTextView, mIntroduction_text1, mIntroduction_text2;
    private PowerTimer mPowerTimer;
    private ImageView mImageView;
    private static final String TAG = "SuperModeDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
        setContentView(R.layout.powersaver_super_power_save_view);
        initData();
        findViews();
        chameleonColorProcess();
        updateTimeAlert();
    }

    private void findViews() {
        mTimeTextView = (TextView) findViewById(R.id.time_text);
        mIntroduction_text1 = (TextView) findViewById(R.id.introduction_text1);
        mIntroduction_text2 = (TextView) findViewById(R.id.introduction_text2);
        mImageView = (ImageView) findViewById(R.id.introduction_image);
    }

    private void initData() {
        mContext = this;
        mPowerTimer = new PowerTimer(mContext);
    }

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            int color_C1 = ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1();
            mIntroduction_text1.setTextColor(color_C1);
            mIntroduction_text2.setTextColor(color_C1);
        }
    }

    private void updateTimeAlert() {
        String str = null;
        int time = 0;
        Resources res = mContext.getResources();
        try {
            mTimeTextView.setText(mPowerTimer.getTimeStrInSuperMode(
                    getResources().getString(R.string.super_power_mode_introduction_string3),
                    getResources().getString(R.string.dialog_superpower_message_normal)));
        } catch (NotFoundException e) {
            Log.e(TAG, "updateTimeAlert in super detail, getString() throw exception, " + e.toString());
            return;
        }
        // Add by zhiheng.huang on 2019/12/25 for TEWBW-339 start
        if (!res.getBoolean(R.bool.superlauncher_clock_show)) {
            mIntroduction_text1.setText(R.string.super_power_mode_introduction_string1_no_clock);
            mImageView.setBackgroundResource(R.drawable.super_power_save_mode_no_clock);
        }
        // Add by zhiheng.huang on 2019/12/25 for TEWBW-339 end

        // Gionee xionghg add for power saving optimization 145357 begin
        if (Consts.SUPPORT_NEW_LAUNCHER) {
            mIntroduction_text1.setText(R.string.super_power_mode_introduction_new1);
            mIntroduction_text2.setText(R.string.super_power_mode_introduction_new2);
            mTimeTextView.setText(mPowerTimer.getTimeStrInSuperModeOptimizer(null,
                    getResources().getString(R.string.super_power_mode_introduction_new3)));
            final TextView mPluginText = (TextView) findViewById(R.id.plugin_text);
            //Chenyee bianrong <2018-2-6> add for SW17W16KR-102 begin
            if(Consts.gnKRFlag){
                mPluginText.setText((R.string.super_power_mode_introduction_new4_KR));
            }else {
                //mPluginText.setText(R.string.super_power_mode_introduction_new4);
                //Chenyee guoxt modify for SW17W08A-195 begin
                mPluginText.setText(res.getString(R.string.super_power_mode_introduction_new4) + "\n\n"
                        + res.getString(R.string.super_power_mode_introduction_string4));
                //Chenyee guoxt modify for SW17W08A-195 end
            }
            //Chenyee bianrong <2018-2-6> add for SW17W16KR-102 end
            /*guoxt modify for CSW1703A-403 begin */
            if (Consts.isNotchSupport){
                mImageView.setBackgroundResource(R.drawable.super_power_save_mode_notch);
            }else {
                mImageView.setBackgroundResource(R.drawable.super_power_save_mode_new);
            }
            /*guoxt modify for CSW1703A-403 end */
        }
        // Gionee xionghg add for power saving optimization 145357 end        
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}