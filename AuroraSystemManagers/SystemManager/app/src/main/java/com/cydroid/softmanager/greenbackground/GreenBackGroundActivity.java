package com.cydroid.softmanager.greenbackground;

import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.greenbackground.adapter.GreenBackgroundAdapter;
import com.cydroid.softmanager.oneclean.WhiteListMrgActivity;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.utils.UiUtils;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeSwitch;

public class GreenBackGroundActivity extends BaseActivity {
    private static final String TAG = "GreenBackGroundActivity";
    private static final int START_ANIM = 1;

    private ImageView mImageView;
    private ImageView mImageOut1;
    private ImageView mImageOut2;
    private Animation mAnimation1;
    private Animation mAnimation2;

    private CyeeListView mListView;
    private CyeeSwitch mGreenSwitch;
    // private TextView mGreenbgTitle, mGreenSwitchTitle, mGreengbgSummary1, mGreengbgSummary2,
    //     mGreengbgSummary3, mGreengbgSummary4;
    private TextView mGreenbgTitle, mGreenSwitchTitle, mGreengbgSummary1;
    private RelativeLayout mTopLayout, mHeadBar;

    private Resources mRes;
    private MyHandler mHandler;
    private GreenBackgroundAdapter mAdapter;
    private WhiteListManager mWhiteListManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setElevation(getmActionBar(), 0);
        ChameleonColorManager.getInstance().onCreate(this);
        setContentView(R.layout.greenbackground_activity_layout);
        initView();
        initData();
        setListViewItemClickListener();
        chameleonColorProcess();
    }

    private void initView() {
        mRes = getResources();

        mTopLayout = (RelativeLayout) findViewById(R.id.green_logo_layout);
        mImageView = (ImageView) findViewById(R.id.green_logo_img);
        mImageOut1 = (ImageView) findViewById(R.id.green_logo_out1);
        mImageOut2 = (ImageView) findViewById(R.id.green_logo_out2);
        mAnimation1 = (Animation) AnimationUtils.loadAnimation(this, R.anim.green_bg_anim);
        mAnimation2 = (Animation) AnimationUtils.loadAnimation(this, R.anim.green_bg_anim);
        mGreenbgTitle = (TextView) findViewById(R.id.green_bg_title);

        mHeadBar = (RelativeLayout) findViewById(R.id.green_background_switch_layout);
        mGreenSwitchTitle = (TextView) findViewById(R.id.green_background_switch_title);
        mGreenSwitch = (CyeeSwitch) findViewById(R.id.green_background_switch_btn);
        mGreenSwitch.setFocusable(false);
        setGreenSwitchStateChangeListener();
        headBarSetClickListener();

        mListView = (CyeeListView) findViewById(R.id.listview);
        mListView.setStretchEnable(false);

        mGreengbgSummary1 = (TextView) findViewById(R.id.green_summary_text1);
//        mGreengbgSummary2 = (TextView) findViewById(R.id.green_summary_text2);
//        mGreengbgSummary3 = (TextView) findViewById(R.id.green_summary_text3);
//        mGreengbgSummary4 = (TextView) findViewById(R.id.green_summary_text4);
    }

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            mTopLayout.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());

            // Gionee <houjie> <2015-11-13> add for CR01565278 begin
            mImageView.getBackground().setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mImageOut1.getBackground().setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mImageOut2.getBackground().setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            // Gionee <houjie> <2015-11-13> add for CR01565278 end
        }
    }

    private void setListViewItemClickListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (arg2 == 0) {
                    Intent intent = new Intent(GreenBackGroundActivity.this, WhiteListMrgActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void headBarSetClickListener() {
        mHeadBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.mSwitchState = !mAdapter.mSwitchState;
                mGreenSwitch.setChecked(mAdapter.mSwitchState);
            }
        });
    }

    private void setGreenSwitchStateChangeListener() {
        mGreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mAdapter.mSwitchState = isChecked;
                refreshBackGroundAndText(mAdapter.mSwitchState);
                if (isChecked) {
                    showNotification();
                    mGreenSwitchTitle.setText(mRes.getString(R.string.green_summary_on));
                    if (isShouldShowAnim()) {
                        startAnimWhenOpen();
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    mGreenSwitchTitle.setText(mRes.getString(R.string.green_summary_off));
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initData() {
        mWhiteListManager = WhiteListManager.getInstance();
        mWhiteListManager.init(this);
        mHandler = new MyHandler();
        mAdapter = new GreenBackgroundAdapter(this);
        mListView.setAdapter(mAdapter);
        mAdapter.mSwitchState = mWhiteListManager.isGreenBackgroundEnable();
        if (mAdapter.mSwitchState) {
            mGreenSwitchTitle.setText(mRes.getString(R.string.green_summary_on));
        } else {
            mGreenSwitchTitle.setText(mRes.getString(R.string.green_summary_off));
        }
        mGreenSwitch.setChecked(mAdapter.mSwitchState);
        refreshBackGroundAndText(mAdapter.mSwitchState);
    }

    private void refreshBackGroundAndText(boolean state) {
        if (state) {
            mGreenbgTitle.setText(mRes.getString(R.string.green_background_on));
            //guoxt modify begin
//            mGreengbgSummary1.setVisibility(View.VISIBLE);
//            mGreengbgSummary4.setVisibility(View.VISIBLE);
            mGreengbgSummary1.setText(mRes.getString(R.string.green_summary_string));
//            mGreengbgSummary2.setText(mRes.getString(R.string.green_summary_on_string2));
//            mGreengbgSummary3.setText(mRes.getString(R.string.green_summary_on_string3));
//            mGreengbgSummary4.setText(mRes.getString(R.string.green_summary_on_string4));
            //guoxt modify end
            android.provider.Settings.System.putInt(getContentResolver(), Consts.RUN_GAME_MEMORY_CLEAN_STATUS,
                    1);
            if (ChameleonColorManager.isNeedChangeColor()) {
                mGreengbgSummary1
                        .setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
//                mGreengbgSummary2
//                        .setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
//                mGreengbgSummary3
//                        .setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
//                mGreengbgSummary4
//                        .setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
            }
        } else {
            setViewVisibility(mImageOut1, View.INVISIBLE);
            setViewVisibility(mImageOut2, View.INVISIBLE);
            mImageView.setAlpha(0.5f);
            mGreenbgTitle.setText(mRes.getString(R.string.green_background_off));
            //guoxt modify for oversea begin
            mGreengbgSummary1.setText(mRes.getString(R.string.green_summary_string));
            //guoxt modify for oversea end
//            mGreengbgSummary2.setText(mRes.getString(R.string.green_summary_string1));
//            mGreengbgSummary3.setText(mRes.getString(R.string.green_summary_string2));
//            mGreengbgSummary1.setVisibility(View.INVISIBLE);
//            mGreengbgSummary4.setVisibility(View.INVISIBLE);
            android.provider.Settings.System.putInt(getContentResolver(), Consts.RUN_GAME_MEMORY_CLEAN_STATUS,
                    0);

            if (ChameleonColorManager.isNeedChangeColor()) {
                mGreengbgSummary1
                        .setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
//                mGreengbgSummary2
//                        .setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
//                mGreengbgSummary3
//                        .setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
//                mGreengbgSummary4
//                        .setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
            }
        }
        mWhiteListManager.setGreenBackgroundEnable(state);
    }

    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_ANIM:
                    setViewVisibility(mImageOut2, View.VISIBLE);
                    mImageOut2.startAnimation(mAnimation2);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //chenyee zhaocaili 20180508 add for CSW1707A-845 begin
        releasRes();
        //chenyee zhaocaili 20180508 add for CSW1707A-845 end
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    //chenyee zhaocaili 20180508 add for CSW1707A-845 begin
    private void releasRes() {
        Util.unbindDrawables(findViewById(R.id.root));
    }
    //chenyee zhaocaili 20180508 add for CSW1707A-845 end

    private void startAnimWhenOpen() {
        setViewVisibility(mImageOut1, View.VISIBLE);
        mImageOut1.startAnimation(mAnimation1);
        alphaAnim();
        new Thread() {
            public void run() {
                try {
                    sleep(150);
                    mHandler.sendMessage(mHandler.obtainMessage(START_ANIM));
                } catch (InterruptedException e) {
                    return;
                }
            }
        }.start();
    }

    private void setViewVisibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    private void alphaAnim() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mImageView, "Alpha", 0.5f, 1.0f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(200);
        anim.start();
    }

    private boolean isShouldShowAnim() {
        return mWhiteListManager.isGreenBackgroundEnable();
    }

    private void showNotification() {
        boolean isfirst = mWhiteListManager.isFirstIn();
        if (isfirst) {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.green_bg_remoteview);
            NotificationManager notifiManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.notify,
                    getResources().getString(R.string.green_bg_notifi_content), System.currentTimeMillis());
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.contentView = remoteViews;
            notifiManager.notify(R.string.green_bg_notifi_content, notification);
            mWhiteListManager.setNoFirstIn();
        }
    }
}
