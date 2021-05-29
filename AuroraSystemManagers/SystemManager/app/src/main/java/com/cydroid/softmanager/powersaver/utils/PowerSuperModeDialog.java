package com.cydroid.softmanager.powersaver.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.chenyee.featureoption.ServiceUtil;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.activities.SuperModeDetailsActivity;
import com.cydroid.softmanager.powersaver.service.PowerManagerService;
import com.cydroid.softmanager.trafficassistant.NotificationController;
import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Field;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
public class PowerSuperModeDialog extends CyeeActivity {
    private final static String TAG = "PowerSuperModeDialog";

    private Context mContext;
    private PowerTimer mPowerTimer;
    // Gionee <yangxinruo> <2015-09-24> add for CR01559111 begin
    private CyeeAlertDialog mDialog;

    // Gionee <yangxinruo> <2015-09-24> add for CR01559111 end

    // Gionee <yangxinruo> <2016-6-6> add for CR01714392 begin
    private boolean mIntoSuperModeClicked = false;
    // Gionee <yangxinruo> <2016-6-6> add for CR01714392 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        // Gionee <yangxinruo> <2015-11-18> add for CR01593050 begin
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager.isInLockTaskMode()) {
            Toast.makeText(mContext.getApplicationContext(), R.string.in_lock_task_toast, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        // Gionee <yangxinruo> <2015-11-18> add for CR01593050 end
        mPowerTimer = new PowerTimer(mContext);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (PowerConsts.SUPER_MODE == PowerModeUtils.getCurrentMode(mContext)) {
            Toast.makeText(mContext.getApplicationContext(), R.string.super_mode_toast, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        // Gionee <yangxinruo> <2015-09-24> add for CR01559111 begin
        initDialog(mContext);
        // Gionee <yangxinruo> <2015-09-24> add for CR01559111 end
        popDialog(mContext);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDialog != null) {
            Resources res = mContext.getResources();
            String message = "";
            if (Consts.SUPPORT_NEW_LAUNCHER) {
                message = mPowerTimer.getTimeStrInSuperModeOptimizer(
                        res.getString(R.string.dialog_superpower_message_new),
                        res.getString(R.string.dialog_superpower_message_normal_new));
            } else {
                // Modify by zhiheng.huang on 2019/12/30 for  start
                boolean showClock = res.getBoolean(R.bool.superlauncher_clock_show);
                message = mPowerTimer.getTimeStrInSuperMode(
                        res.getString(showClock ? R.string.dialog_superpower_message : R.string.dialog_superpower_message_no_clock),
                        res.getString(R.string.dialog_superpower_message_normal));
                // Modify by zhiheng.huang on 2019/12/30 for  end
            }
            // Gionee xionghg modify for power saving optimization 145357 end
            Log.d(TAG, "msg:" + message);
            mDialog.setMessage(message);
        }
    }

    // Gionee <yangxinruo> <2016-1-13> add for CR01622514 end

    // Gionee <yangxinruo> <2015-09-24> add for CR01559111 begin
    private void initDialog(final Context context) {
        // Gionee <yangxinruo> <2016-1-13> modify for CR01622514 begin
        /*
        Resources res = context.getResources();
        String title = res.getString(R.string.super_power_mode);
        int time = mPowerTimer.getTimeInSuperMode();
        String message = String.format(res.getString(R.string.dialog_superpower_message),
                mPowerTimer.formatTime(time));
        mDialog = new CyeeAlertDialog.Builder(context).setTitle(title).setMessage(message).create();
        */
        Resources res = context.getResources();
        String title = res.getString(R.string.super_power_mode);
        mDialog = new CyeeAlertDialog.Builder(context).setMessage("").setTitle(title).create();
        // Gionee <yangxinruo> <2016-1-13> modify for CR01622514 end
        mDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                context.getResources().getString(R.string.dialog_neutralbutton_txt), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(context, SuperModeDetailsActivity.class);
                        // Gionee <yangxinruo> <2016-4-12> add for CR01674028 begin
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        // Gionee <yangxinruo> <2016-4-12> add for CR01674028 end
                        context.startActivity(intent);
                        // PowerSuperModeDialog.this.finish();
                    }
                });
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getResources().getString(R.string.dialog_positivebutton_txt), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Gionee <yangxinruo> <2015-10-20> add for CR01571556 begin
                        try {
                            Field field = AlertDialog.class.getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            // 设置mShowing值，欺骗android系统
                            field.set(mDialog, false);
                            // Gionee <yangxinruo> <2016-6-6> add for CR01714392 begin
                            mIntoSuperModeClicked = true;
                            // Gionee <yangxinruo> <2016-6-6> add for CR01714392 end
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Gionee <yangxinruo> <2015-10-20> add for CR01571556 end
                        clearNotification(context);
                        // Gionee <yangxinruo> <2015-10-21> delete for CR01570615 begin
                        // stopFloatTouch(context);
                        // Gionee <yangxinruo> <2015-10-21> delete for CR01570615 end
                        intoSuperSaveMode(context);
                        // PowerSuperModeDialog.this.finish();
                    }
                });
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getResources().getString(R.string.dialog_negativebutton_txt), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Log.d(TAG, "cancel call dismiss");
                        // PowerSuperModeDialog.this.finish();
                    }
                });

        mDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    Log.d(TAG, "push KEYCODE_BACK call dismiss");
                    dialog.dismiss();
                    // PowerSuperModeDialog.this.finish();
                }
                return false;
            }
        });

        mDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                Log.d(TAG, "PowerSuperModeDialog dismiss,finish activity");
                PowerSuperModeDialog.this.finish();
            }
        });
    }

    // Gionee <yangxinruo> <2015-10-28> add for CR01575580 begin
    @Override
    protected void onPause() {
        if (mDialog != null) {
            // Gionee <yangxinruo> <2016-6-6> modify for CR01714392 begin
            if (mIntoSuperModeClicked) {
                try {
                    Field field = AlertDialog.class.getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(mDialog, true); // 需要关闭的时候 将这个参数设置为true 他就会自动关闭了
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "click into supermode ,dissmiss");
                mDialog.dismiss();
            }
            // Gionee <yangxinruo> <2016-6-6> modify for CR01714392 end
        }
        super.onPause();
    }
    // Gionee <yangxinruo> <2015-10-28> add for CR01575580 end
    // Gionee <yangxinruo> <2015-09-24> add for CR01559111 end

    // Gionee <yangxinruo> <2015-09-24> modify for CR01559111 begin
    private void popDialog(final Context context) {
        // dialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    // Gionee <yangxinruo> <2015-09-24> modify for CR01559111 end

    // Gionee <yangxinruo> <2015-10-21> delete for CR01570615 begin
    /*
    private void stopFloatTouch(Context context) {
        try {
            if (CyeeSettings.getInt(context.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 0) == 1) {
                new PreferenceHelper(context).commitBoolean(PowerConsts.FLOAT_TOUCH_SEITCH, true);
                Intent intent = new Intent("com.cydroid.floatingtouch.action.STOP_SERVICE");
                context.sendBroadcast(intent);
                Log.d("PowerSuperModeDialog", "send intent STOP_SERVICE");
                CyeeSettings.putInt(context.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 0);
            }
        } catch (Exception e) {
            Log.d("PowerSuperModeDialog", "send intent STOP_SERVICE" + e);
        }
    }
    */
    // Gionee <yangxinruo> <2015-10-21> delete for CR01570615 end

    private void intoSuperSaveMode(Context context) {
        //Gionee yuchao 20160803 add for CR01740416 begin
        Intent intoSuperMode = new Intent();
        intoSuperMode.setAction("gionee.intent.action.supermode");
        context.sendBroadcastAsUser(intoSuperMode, UserHandle.CURRENT);

        Intent intent = new Intent(context, PowerManagerService.class);
        intent.setAction(PowerConsts.MODE_CHANGE_INTENT);
        Bundle bundle = new Bundle();
        int from = PowerModeUtils.getCurrentMode(context);
        int to = PowerConsts.SUPER_MODE;
        bundle.putInt("from", from);
        bundle.putInt("to", to);
        intent.putExtras(bundle);
        ServiceUtil.startForegroundService(context,intent);
    }

    private void clearNotification(Context context) {
        NotificationController.getDefault(context.getApplicationContext()).cancelAllNotification();
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}
