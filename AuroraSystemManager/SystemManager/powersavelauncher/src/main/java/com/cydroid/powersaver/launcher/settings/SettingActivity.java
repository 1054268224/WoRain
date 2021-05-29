package com.cydroid.powersaver.launcher.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
//Chenyee <bianrong> <2018-1-25> add for SW17W16KR-84 begin
import com.cydroid.powersaver.launcher.ConfigUtil;
//Chenyee <bianrong> <2018-1-25> add for SW17W16KR-84 end
import com.cydroid.powersaver.launcher.R;
import com.cydroid.powersaver.launcher.util.DebouncedClickAction;
import com.cydroid.powersaver.launcher.util.Log;

import java.util.Locale;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeSwitch;

/**
 * Created by xionghg on 17-4-12.
 */

public class SettingActivity extends CyeeActivity {
    private static final String TAG = "SettingActivity";

    private RelativeLayout mMobileView;
    private CyeeSwitch mMobileSwitch;
    private RelativeLayout mWifiView;
    private RelativeLayout mExitView;

    private MobileDataEnabler mEnabler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Chenyee xionghg add for black NavigationBar begin
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.black));
        // Chenyee xionghg add for black NavigationBar end
        setContentView(R.layout.activity_setting);

        CyeeActionBar actionBar = getCyeeActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initViews();
        initListeners();
        mEnabler = new MobileDataEnabler(this, mMobileView, mMobileSwitch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEnabler.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEnabler.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initViews() {
        mMobileView = (RelativeLayout) findViewById(R.id.mobile_view);
        mMobileSwitch = (CyeeSwitch) findViewById(R.id.mobile_switch);
        mWifiView = (RelativeLayout) findViewById(R.id.wifi_view);
        //Chenyee <bianrong> <2018-1-25> add for SW17W16KR-84 begin
        if(ConfigUtil.cyKRFlag){
            mWifiView.setVisibility(View.GONE);
        }
        //Chenyee <bianrong> <2018-1-25> add for SW17W16KR-84 end
        mExitView = (RelativeLayout) findViewById(R.id.exit_view);
    }

    private void initListeners() {
        mWifiView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName cn = new ComponentName("com.cydroid.setting.adapter.wifi", "com.cydroid.setting.adapter.wifi.wifiSettingsActivity");
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(cn);
                startActivity(intent);
            }
        });
        mExitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mExitView onClick: ");
                createDialog(true);
            }
        });
    }

    private CyeeAlertDialog mPermDialog;
    private boolean mIsExitSuperSaveModeSent = false;

    private void createDialog(final boolean fromClick) {
        String message = "";
        mPermDialog = new CyeeAlertDialog.Builder(this, CyeeAlertDialog.THEME_CYEE_FULLSCREEN)
                .create();
        mPermDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        if (Locale.getDefault().getLanguage().toLowerCase().equals("ar")) {
            mPermDialog.setTitle(getString(R.string.exit_string_title));
        } else {
            mPermDialog.setTitle(getString(R.string.exit_string));
        }
        if (fromClick) {
            message = getString(R.string.exit_msg);
        } else {
            message = getString(R.string.exit_msg1);
        }
        mPermDialog.setMessage(message);

        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {
            DebouncedClickAction postiveAction = new DebouncedClickAction() {
                @Override
                public void debouncedAction() {
                    if (!mIsExitSuperSaveModeSent) {
                        exitSuperSaveMode();
                        Log.d(TAG, "set supermode exit trigger flag");
                        mIsExitSuperSaveModeSent = true;
                    } else {
                        Log.d(TAG, "exit supermode already sent ,cancel");
                    }
                    getSharedPreferences("setting_value", Context.MODE_PRIVATE).edit().putBoolean("is_first", true).commit();
//                    addYouJuAgent(mContext, true, fromClick);
                }
            };

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CyeeAlertDialog.BUTTON_POSITIVE:
                        postiveAction.onClick();
                        break;
                    case CyeeAlertDialog.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };

        mPermDialog.setButton(CyeeAlertDialog.BUTTON_POSITIVE, getString(R.string.ok_string),
                dialogClickLsn);
        mPermDialog.setButton(CyeeAlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel_string),
                dialogClickLsn);
//        mPermDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//
//            }
//        });

        mPermDialog.show();
    }

    private void exitSuperSaveMode() {
        Intent intent = new Intent();
        intent.setAction("com.action.exit.super.power.save.mode");
        intent.setPackage("com.cydroid.softmanager");
        startService(intent);
        Log.d(TAG, "EXIT_SuperMode_ACTION sended---->");
        // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
