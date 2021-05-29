package com.cydroid.softmanager.powersaver.utils;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.powersaver.activities.NormalModeDetailsActivity;
import com.cydroid.softmanager.powersaver.activities.PowerManagerModeAdapter;
import com.cydroid.softmanager.powersaver.activities.SuperModeDetailsActivity;
import com.cydroid.softmanager.powersaver.fragment.PowerManagerSettingsFragment;
import com.cydroid.softmanager.powersaver.service.PowerManagerService;
import com.cydroid.softmanager.trafficassistant.NotificationController;
import com.cydroid.softmanager.utils.GNToast;
import com.cydroid.softmanager.utils.Log;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeCheckBox;
import cyee.widget.CyeeTextView;

import com.chenyee.featureoption.ServiceUtil;
//import com.gionee.youju.statistics.sdk.YouJuAgent;

public class PowerModeSelectDialog {
    private static final String TAG = "PowerModeSelectDialog";

    public static final String NOT_POP_DIALOG = "not_pop_dialog";
    private PowerManagerModeAdapter mModeAdapter;
    // private String mTitle;
    // private String mMessage;
    private int mCurrMode;
    private int mLastMode;
    private final PowerTimer mPowerTimer;
    /*guoxt modify for CR01597159 begin*/
    IActivityManager activityManager = ActivityManagerNative.getDefault();
    private boolean isScreenpin = false;
    /*guoxt modify for CR01597159 end*/
    private CyeeCheckBox mIsShouldPopDialogCheckbox;
    private CyeeAlertDialog mDialog;

    public PowerModeSelectDialog(PowerTimer timer) {
        mPowerTimer = timer;
        /*guoxt modify for CR01597159 begin*/
        try {
            isScreenpin = activityManager.isInLockTaskMode();
        } catch (Exception e) {
            Log.d("PowerModeSelectDialog", "send intent STOP_SERVICE" + e);
        }
        /*guoxt modify for CR01597159 end*/
    }

    public void setCurrMode(int mode) {
        mCurrMode = mode;
    }

    public void setLastMode(int mode) {
        mLastMode = mode;
    }

    public void setModeAdapter(PowerManagerModeAdapter adapter) {
        mModeAdapter = adapter;
    }

    public void onShowDialog(final Context context) {
        Resources mRes = context.getResources();
        // Gionee <yangxinruo> <2015-10-10> delete for CR01565255 begin
        // final PreferenceHelper mHelper = new PreferenceHelper(context);
        // Gionee <yangxinruo> <2015-10-10> delete for CR01565255 end
        if (isShouldNotShow(context)) {
            onActivateMode(context, mCurrMode);
//            notifyDataSetChange(mCurrMode);
            return;
        }
        // do not pop dialog when locked
        if (mModeAdapter.isModeButtonLocked(mCurrMode)) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        String msgTxt = getMessage(mRes, mCurrMode);
        View view = getCustomDialogView(inflater, msgTxt, mCurrMode);
        mIsShouldPopDialogCheckbox = (CyeeCheckBox) view.findViewById(R.id.dialog_check_box);

        mDialog = new CyeeAlertDialog.Builder(context).setTitle(getTitle(mRes, mCurrMode)).setView(view)
                .setNeutralButton(getNeutralButtonText(mRes, mCurrMode), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(context, getModeDetailClass(mCurrMode));
                        context.startActivity(intent);
                    }
                })

                .setPositiveButton(R.string.dialog_positivebutton_txt, new OnClickListener() {
                    // Gionee <yangxinruo> <2015-08-15> modify for CR01538376 begin
                    final DebouncedClickAction onActivateMode = new DebouncedClickAction() {
                        @Override
                        public void debouncedAction() {
                            if (mCurrMode == Consts.NORMAL_MODE && mIsShouldPopDialogCheckbox != null
                                    && mIsShouldPopDialogCheckbox.isChecked()) {
                                SharedPreferences helper = context.getSharedPreferences(
                                        PowerManagerSettingsFragment.PREFERENCE_NAME, Context.MODE_PRIVATE);
                                helper.edit().putBoolean(NOT_POP_DIALOG, true).commit();
                            }
                            /*guoxt modify for CR01597159 end*/
                            if (isScreenpin) {
                                GNToast.showToast(context, context.getResources().getString(R.string.text_in_screenpin));
                            } else {
                                onActivateMode(context, mCurrMode);
//                                notifyDataSetChange(mCurrMode);
                            }
                            /*guoxt modify for CR01597159 end*/
                        }
                    };

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        /*
                         * onActivateMode(context, mHelper, mCurrMode);
                         * notifyDataSetChange(mCurrMode);
                         */
                        onActivateMode.onClick();
                    }
                    // Gionee <yangxinruo> <2015-08-15> modify for CR01538376 end
                })

                .setNegativeButton(R.string.dialog_negativebutton_txt, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
//                        notifyDataSetChange(mLastMode);
                    }
                }).create();
        mDialog.show();
        // Add by zhiheng.huang on 2020/1/2 for TEWBW-616 start
        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.dialog_ripple);
        mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.dialog_ripple);
        mDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundResource(R.drawable.dialog_ripple);
        // Add by zhiheng.huang on 2020/1/2 for TEWBW-616 end
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (PowerModeSelectDialog.this != null)
                    notifyDataSetChange(mLastMode);
            }
        });
    }

    private String getTitle(Resources res, int mode) {
        switch (mode) {
            case Consts.NORMAL_MODE:
                return String.format(res.getString(R.string.normal_power_mode), "");
            case Consts.SUPER_MODE:
                return String.format(res.getString(R.string.super_power_mode), "");
            default:
                return "";
        }
    }

    private String getMessage(Resources res, int mode) {
        switch (mode) {
            case Consts.NORMAL_MODE:
                return String.format(res.getString(R.string.dialog_dailypower_message_nogesture), "");
            case Consts.SUPER_MODE:
                String timeStrInSuperMode = "";
                if (Consts.SUPPORT_NEW_LAUNCHER) {
                    timeStrInSuperMode = mPowerTimer.getTimeStrInSuperModeOptimizer(
                            res.getString(R.string.dialog_superpower_message_new),
                            res.getString(R.string.dialog_superpower_message_normal_new));
                } else {
                    // Modify by zhiheng.huang on 2019/12/30 for  start
                    boolean showClock = res.getBoolean(R.bool.superlauncher_clock_show);
                    timeStrInSuperMode = mPowerTimer.getTimeStrInSuperMode(
                            res.getString(showClock ? R.string.dialog_superpower_message : R.string.dialog_superpower_message_no_clock),
                            res.getString(R.string.dialog_superpower_message_normal));
                    // Modify by zhiheng.huang on 2019/12/30 for  end
                }
                return timeStrInSuperMode;
            default:
                return "";
        }
    }

    private String getNeutralButtonText(Resources res, int mode) {
        switch (mode) {
            case Consts.NORMAL_MODE:
                return String.format(res.getString(R.string.dialog_neutralbutton_config_txt), "");
            case Consts.SUPER_MODE:
                return String.format(res.getString(R.string.dialog_neutralbutton_txt), "");
            default:
                return "";
        }
    }

    private View getCustomDialogView(LayoutInflater inflater, String msgTxt, int currentMode) {
        View resView = inflater.inflate(R.layout.custom_alertdialog_layout, null);
        resView.findViewById(R.id.scroll_main_content_container).setVisibility(View.GONE);
        resView.findViewById(R.id.no_scroll_main_content).setVisibility(View.VISIBLE);
        if (mCurrMode == Consts.NORMAL_MODE) {
            CyeeCheckBox checkBox = (CyeeCheckBox) resView.findViewById(R.id.dialog_check_box);
            checkBox.setChecked(false);
            resView.findViewById(R.id.dialog_checkbox_container).setVisibility(View.VISIBLE);
        } else {
            resView.findViewById(R.id.dialog_checkbox_container).setVisibility(View.GONE);
        }
        CyeeTextView txtMainContent = (CyeeTextView) resView.findViewById(R.id.no_scroll_main_content);
        txtMainContent.setText(msgTxt);
        return resView;
    }

    private Class<?> getModeDetailClass(int mode) {
        Class<?>[] mClass = {NormalModeDetailsActivity.class, SuperModeDetailsActivity.class};
        return mClass[mode - 1];
    }

    private boolean isShouldNotShow(Context context) {
        if (mCurrMode == Consts.NORMAL_MODE) {
            // Gionee <yangxinruo> <2015-10-10> modify for CR01565255 begin
            // Gionee <yangxinruo> <2015-10-29> modify for CR01571937 begin
            // ProviderHelper mHelper = new ProviderHelper(context);
            SharedPreferences mHelper = context.getSharedPreferences(
                    PowerManagerSettingsFragment.PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
            // Gionee <yangxinruo> <2015-10-29> modify for CR01571937 end
            boolean notShowDialog = mHelper.getBoolean(NOT_POP_DIALOG, false);
            Log.d(TAG, "should show dialog? " + !notShowDialog);
            // Gionee <yangxinruo> <2015-10-10> modify for CR01565255 end
            return notShowDialog;
        } else return mCurrMode != Consts.SUPER_MODE;
    }

    // Gionee <yangxinruo> <2015-10-10> modify for CR01565255 begin
    // private void onActivateMode(Context context, PreferenceHelper helper, int mode) {
    private void onActivateMode(Context context, int mode) {
        // Gionee <yangxinruo> <2015-10-10> modify for CR01565255 end


        switch (mode) {
            case Consts.NONE_MODE:
                break;

            case Consts.NORMAL_MODE:
                intoNormalMode(context, mode);
                break;

            case Consts.SUPER_MODE:
                ActivityManager activityManager = (ActivityManager) context
                        .getSystemService(Context.ACTIVITY_SERVICE);
                if (activityManager.isInLockTaskMode()) {
                    Toast.makeText(context.getApplicationContext(), R.string.in_lock_task_toast,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // Chenyee xionghg 20170929 add for 212610 begin
                if (ActivityManager.isUserAMonkey()) {
                    Log.w(TAG, "return for user is a monkey");
                    return;
                }
                // Chenyee xionghg 20170929 add for 212610 end
                // Gionee xionghg 2017-05-06 add for 126327 begin
                String defLauncher = Util.getDefaultLauncherPkg(context);
                if ("com.cydroid.powersaver.launcher".equalsIgnoreCase(defLauncher)) {
                    Log.e(TAG, "Current launcher is not right, return!");
                    return;
                }
                // Gionee xionghg 2017-05-06 add for 126327 end
                clearNotification(context);
                // Gionee <yangxinruo> <2015-10-21> delete for CR01570615 begin
                // stopFloatTouch(context);
                // Gionee <yangxinruo> <2015-10-21> delete for CR01570615 end
                intoSuperSaveMode(context, mode);
                break;
            default:
                break;
        }

    }

    // Gionee <yangxinruo> <2015-10-21> delete for CR01570615 begin
    /*
    private void stopFloatTouch(Context context) {
        try {
            if (CyeeSettings.getInt(context.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 0) == 1) {
                new PreferenceHelper(context).commitBoolean(PowerConsts.FLOAT_TOUCH_SEITCH, true);
                Intent intent = new Intent("com.cydroid.floatingtouch.action.STOP_SERVICE");
                context.sendBroadcast(intent);
                Log.d(TAG, "send intent STOP_SERVICE");
                CyeeSettings.putInt(context.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 0);
            }
        } catch (Exception e) {
            Log.d(TAG, "send intent STOP_SERVICE" + e);
        }
    }
    */
    // Gionee <yangxinruo> <2015-10-21> delete for CR01570615 end

    private void intoNormalMode(Context context, int mode) {
        int from = PowerModeUtils.getCurrentMode(context);
        if (mode == from) {
            return;
        }
        Log.d(TAG, "request start service to NORMAL_MODE");
        if (!mModeAdapter.setLockModeButton(true, PowerConsts.NORMAL_MODE)) {
            return;
        }
        Intent intent = new Intent(context, PowerManagerService.class);
        intent.setAction(PowerConsts.MODE_CHANGE_INTENT);
        Bundle bundle = new Bundle();
        int to = PowerConsts.NORMAL_MODE;
        bundle.putInt("from", from);
        bundle.putInt("to", to);
        intent.putExtras(bundle);
        ServiceUtil.startForegroundService(context, intent);
    }

    private void intoSuperSaveMode(Context context, int mode) {
        Log.d(TAG, "request start activity to SUPER_MODE");
        if (!mModeAdapter.setLockModeButton(true, PowerConsts.SUPER_MODE)) {
            return;
        }
        Intent intent = new Intent(context, PowerManagerService.class);
        intent.setAction(PowerConsts.MODE_CHANGE_INTENT);
        Bundle bundle = new Bundle();
        int from = PowerModeUtils.getCurrentMode(context);
        int to = PowerConsts.SUPER_MODE;
        bundle.putInt("from", from);
        bundle.putInt("to", to);
        intent.putExtras(bundle);
        ServiceUtil.startForegroundService(context, intent);
    }

    private void notifyDataSetChange(int mode) {
        mModeAdapter.notifyDataSetChanged();
    }


    /*
    private void switchToNoramlModeInBatteryRatio(Context context, int lastMode, int nowMode) {
        if (lastMode == nowMode || nowMode != Consts.NORMAL_MODE) {
            return;
        }

        int percent = BatteryStateInfo.getBatteryLevel(context);
        if (percent <= 100 && percent >= 80) {
            YouJuAgent.onEvent(context, "PM_Power100to80_ExtramOpened");
        } else if (percent >= 60) {
            YouJuAgent.onEvent(context, "PM_Power80to60_ExtramOpened");
        } else if (percent >= 40) {
            YouJuAgent.onEvent(context, "PM_Power60to40_ExtramOpened");
        } else if (percent >= 20) {
            YouJuAgent.onEvent(context, "PM_Power40to20_ExtramOpened");
        } else {
            YouJuAgent.onEvent(context, "PM_Power20Below_ExtramOpened");
        }
    }
     */
    private void clearNotification(Context context) {
        NotificationController.getDefault(context.getApplicationContext()).cancelAllNotification();
    }

    public void onCloseDialog() {
        Log.d(TAG, "force close dialog");
        if (mDialog == null) {
            Log.d(TAG, "dialog is null");
            return;
        }
        mDialog.dismiss();
    }
}
