package com.cydroid.softmanager.powersaver.activities;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.interfaces.IPowerService;
import com.cydroid.softmanager.powersaver.service.PowerManagerService;
import com.cydroid.softmanager.powersaver.utils.DebouncedClickAction;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeSelectDialog;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.powersaver.utils.PowerTimer;
import com.cydroid.softmanager.utils.Log;
import com.chenyee.featureoption.ServiceUtil;

import cyee.changecolors.ChameleonColorManager;

public class PowerManagerModeAdapter extends BaseAdapter {
    private static final String TAG = "PowerManagerModeAdapter";

    private final Context mContext;
    private final PowerTimer mPowerTimer;
    private int mModeButtonLocked = -1;
    private IPowerService mIPowerService;
    private PowerModeSelectDialog mModeDescriptionDialog;
    // Gionee <yangxinruo> <2015-08-21> delete for CR01541741 begin
    // private boolean mRepetiveResponed = true;
    // Gionee <yangxinruo> <2015-08-21> delete for CR01541741 end
    private static final boolean DEBUG = true;

    public PowerManagerModeAdapter(Context context, PowerTimer timer) {
        mContext = context;
        mPowerTimer = timer;
    }

    @Override
    public int getCount() {
        int count;
        // Chenyee <CY_Oversea_Req> xionghg 20171012 add for 234035 begin
        if (Consts.gnBQFlag) {
            count = PowerConsts.NORMAL_MODE + 1;
        } else {
            count = PowerConsts.SUPER_MODE + 1;
        }
        // Chenyee <CY_Oversea_Req> xionghg 20171012 add for 234035 end
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position2, View convertView, ViewGroup parent) {
        final int position = position2 + 1;
        ModeHolder holder;
        if (convertView == null) {
            holder = new ModeHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.powermanager_mode_adapter_layout,
                    parent, false);
            holder.partOneView = convertView.findViewById(R.id.primary_action_view);
            holder.partTwoView = convertView.findViewById(R.id.secondary_action_icon);
            holder.part3View = convertView.findViewById(R.id.secondary_action_icon_switch);
            holder.radioButton = (RadioButton) convertView.findViewById(R.id.powersaver_mode_radio_btn);
            holder.headIcon = (ImageView) convertView.findViewById(R.id.powersaver_mode_item_icon);
            holder.modeTitle = (TextView) convertView.findViewById(R.id.powersaver_mode_title);
            holder.modeSummary = (TextView) convertView.findViewById(R.id.powersaver_mode_summary);
            holder.modeTime = (TextView) convertView.findViewById(R.id.powersaver_mode_time);
            holder.secIcon = (ImageView) convertView.findViewById(R.id.secondary_action_icon_img);
            holder.aSwitch = (Switch) convertView.findViewById(R.id.switch2);
            holder.dashview = (View) convertView.findViewById(R.id.dash_view_wheatek);
            convertView.setTag(holder);
        } else {
            holder = (ModeHolder) convertView.getTag();
        }
        // Gionee <yangxinruo> <2015-09-21> modify for CR01555851 start
//        holder.partOneView.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d(TAG, "MODE TEST onTouch change button at " + position + " event:" + event.getAction());
//                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    Log.d(TAG, "MODE TEST onTouch get ACTION_CANCEL");
//                    // Gionee <yangxinruo> <2016-8-18> delete for CR01748027 begin
//                    // v.callOnClick();
//                    // return true;
//                    // Gionee <yangxinruo> <2016-8-18> delete for CR01748027 end
//                }
//                return false;
//            }
//        });
        // Gionee <yangxinruo> <2015-09-21> modify for CR01555851 end

        if (position == PowerConsts.SUPER_MODE+1) {
            holder.partOneView.setOnClickListener(new OnClickListener() {
                // Gionee <yangxinruo> <2015-08-15> modify for CR01538376 begin
                final DebouncedClickAction triggerModeAction = new DebouncedClickAction() {

                    @Override
                    public void debouncedAction() {
                        triggerModeAction(position);
                    }
                };

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "MODE TEST onClick change button at " + position);
                    /*
                     * if (mRepetiveResponed) { Debug.log(DEBUG, TAG,
                     * "exec onClick event"); setRepetiveResponed(false); new
                     * Handler().postDelayed(new Runnable() {
                     *
                     * @Override public void run() { Debug.log(DEBUG, TAG,
                     * "setRepetiveResponed in thread"); setRepetiveResponed(true);
                     * } }, 800); triggerModeAction(position); }
                     */
                    triggerModeAction.onClick();
                }
                // Gionee <yangxinruo> <2015-08-15> modify for CR01538376 end
            });
        } else {
            holder.partOneView.setOnClickListener(null);
        }

        holder.partTwoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "MODE TEST onClick detail button at " + position);
                checkModeDetails(position);
            }
        });

        if (ChameleonColorManager.isNeedChangeColor()) {
            holder.secIcon.setImageTintList(
                    ColorStateList.valueOf(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()));
        }
        holder.aSwitch.setOnCheckedChangeListener(null);
        holder.aSwitch.setChecked(getCheckedState(position));
        holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            final DebouncedClickAction triggerMode = new DebouncedClickAction() {
                @Override
                public void debouncedAction() {
                    triggerModeAction(position);
                }
            };

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    triggerMode.onClick();
                } else {
                    DebouncedClickAction triggerMode = new DebouncedClickAction() {
                        @Override
                        public void debouncedAction() {
                            triggerModeAction(PowerConsts.NONE_MODE);
                        }
                    };

                    triggerMode.onClick();
                }
            }
        });
        initListItem(position, holder);
        return convertView;
    }

    // 设置标志位，防止响应多次快速点击事件
    // Gionee <yangxinruo> <2015-08-21> delete for CR01541741 begin
    /*
    private void setRepetiveResponed(boolean canResponed) {
        mRepetiveResponed = canResponed;
    }
    */
    // Gionee <yangxinruo> <2015-08-21> delete for CR01541741 end

    private void triggerModeAction(final int position) {
        if (position == PowerConsts.NONE_MODE) {
            triggerNoneMode();
        } else if (position == PowerConsts.CHECK_CONSUMPTION_DETAILS) {
            Intent intent = new Intent(mContext, BatteryUseRankActivity.class);
            mContext.startActivity(intent);
        } else {
            // Gionee <yangxinruo> <2015-11-18> add for CR01593050 begin
            if (position == PowerConsts.SUPER_MODE) {
                ActivityManager activityManager = (ActivityManager) mContext
                        .getSystemService(Context.ACTIVITY_SERVICE);
                if (activityManager.isInLockTaskMode()) {
                    Toast.makeText(mContext.getApplicationContext(), R.string.in_lock_task_toast,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // Gionee <yangxinruo> <2015-11-18> add for CR01593050 end
            popDialogDescription(position);
        }
    }

    private void popDialogDescription(int position) {
        if (mModeDescriptionDialog != null) {
            Log.d(TAG, "close old dialog before new one");
            mModeDescriptionDialog.onCloseDialog();
        }
        int mode = PowerModeUtils.getCurrentMode(mContext);
        mModeDescriptionDialog = new PowerModeSelectDialog(mPowerTimer);
        mModeDescriptionDialog.setCurrMode(position);
        mModeDescriptionDialog.setLastMode(mode);
        mModeDescriptionDialog.setModeAdapter(this);
        mModeDescriptionDialog.onShowDialog(mContext);
    }

    private void triggerNoneMode() {
        Log.d(DEBUG, TAG, "enter triggerNoneMode");
        // 单纯点击“无”，不会触发任何动作
        int from = PowerModeUtils.getCurrentMode(mContext);
        if (from == PowerConsts.NONE_MODE) {
            Log.d(DEBUG, TAG, "current mode is None mode, should do nothing");
            return;
        }

        // 弹出模式切换dialog,此时保持屏幕长亮，状态栏不可点击
        // PowerManagerMainActivity.createDialogAndDisableStatusbar(mContext);

        // 执行切换动作
        Log.d(TAG, "request start service to NONE_MODE");
        if (!setLockModeButton(true, PowerConsts.NONE_MODE)) {
            return;
        }
        Intent intent = new Intent(mContext, PowerManagerService.class);
        intent.setAction(PowerConsts.MODE_CHANGE_INTENT);
        Bundle bundle = new Bundle();
        int to = PowerConsts.NONE_MODE;
        bundle.putInt("from", from);
        bundle.putInt("to", to);
        intent.putExtras(bundle);
        ServiceUtil.startForegroundService(mContext, intent);
    }

    public synchronized boolean setLockModeButton(boolean enable, int mode) {
        if (!enable) {
            Log.d(TAG, "mode button unlocked from mode " + mModeButtonLocked);
            mModeButtonLocked = -1;
            return true;
        } else {
            if (isModeButtonLocked(mode)) {
                Log.d(TAG, "mode button already locked by mode " + mModeButtonLocked
                        + ",setLockModeButton cancel!");
                return false;
            } else {
                mModeButtonLocked = mode;
                Log.d(TAG, "mode button locked by mode " + mode);
                return true;
            }
        }
    }

    public synchronized boolean isModeButtonLocked(int mode) {
        return mModeButtonLocked != -1 && mModeButtonLocked != mode;
    }

    private void checkModeDetails(final int position) {
        Intent intent = new Intent();
        Class<?> cls = null;
        switch (position) {
            case PowerConsts.NONE_MODE:
                return;
            case PowerConsts.NORMAL_MODE:
                cls = NormalModeDetailsActivity.class;
                break;
            case PowerConsts.SUPER_MODE:
                cls = SuperModeDetailsActivity.class;
                break;
            case PowerConsts.CHECK_CONSUMPTION_DETAILS:
                cls = BatteryUseRankActivity.class;
                break;
            default:
                break;
        }
        if (cls != null) {
            intent.setClass(mContext, cls);
        }
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(DEBUG, TAG, "mode details activity not found, " + e.toString());
        }
    }

    private void initListItem(int position, ModeHolder holder) {
        holder.radioButton.setChecked(getCheckedState(position));
        switch (position) {
            case PowerConsts.NONE_MODE:
                initNoneModeItem(holder);
                break;
            case PowerConsts.NORMAL_MODE:
                initNormalModeItem(holder);
                break;
            case PowerConsts.SUPER_MODE:
                initSuperModeItem(holder);
                break;
            case PowerConsts.CHECK_CONSUMPTION_DETAILS:
                initCheckComsuptionDetailsItem(holder);
                break;
            default:
                break;
        }
    }

    private boolean getCheckedState(int position) {
        int mode = PowerModeUtils.getCurrentMode(mContext);
        Log.e(TAG, "getCheckedState current:" + mode + " check=" + position);
        return mode == position;
    }

    private void initNoneModeItem(ModeHolder holder) {
        holder.headIcon.setVisibility(View.GONE);
        holder.radioButton.setVisibility(View.VISIBLE);
        holder.modeSummary.setVisibility(View.VISIBLE);
        holder.modeSummary.setSingleLine(false);
        holder.modeTime.setVisibility(View.GONE);
        holder.partTwoView.setVisibility(View.INVISIBLE);
        holder.modeTitle.setText(R.string.none_power_mode);
        holder.modeSummary.setText(R.string.none_power_mode_summary);
    }

    private void initNormalModeItem(ModeHolder holder) {
        holder.headIcon.setVisibility(View.GONE);
        holder.radioButton.setVisibility(View.VISIBLE);
        holder.modeSummary.setVisibility(View.VISIBLE);
        //guoxt modify for CSW1703AC-71 begin
        //holder.modeSummary.setSingleLine(true);
        //guoxt modify for CSW1703AC-71 end
        holder.modeTime.setVisibility(View.VISIBLE);
        holder.partTwoView.setVisibility(View.GONE);
        holder.part3View.setVisibility(View.VISIBLE);
        holder.secIcon.setImageResource(R.drawable.savepower_setting_icon_indi);
        holder.modeTitle.setText(R.string.normal_power_mode);
        holder.modeSummary.setText(R.string.normal_power_mode_summary);
        holder.modeTime.setText(getTimeString(PowerConsts.NORMAL_MODE));
        holder.dashview.setVisibility(View.GONE);
    }

    private void initSuperModeItem(ModeHolder holder) {
        holder.headIcon.setVisibility(View.GONE);
        holder.radioButton.setVisibility(View.VISIBLE);
        holder.modeSummary.setVisibility(View.VISIBLE);
        //guoxt modify for CSW1703AC-71 begin
        //holder.modeSummary.setSingleLine(true);
        //guoxt modify for CSW1703AC-71 end
        holder.modeTime.setVisibility(View.VISIBLE);
        holder.partTwoView.setVisibility(View.GONE);
        holder.part3View.setVisibility(View.VISIBLE);
        holder.secIcon.setImageResource(R.drawable.savepower_summary_icon_indi);
        holder.modeTitle.setText(R.string.super_power_mode);
        // Modify by zhiheng.huang on 2019/12/30 for  start
        if (Consts.SUPPORT_NEW_LAUNCHER) {
            holder.modeSummary.setText(R.string.super_power_mode_summary_new);
        } else {
            boolean showColck = mContext.getResources().getBoolean(R.bool.superlauncher_clock_show);
            holder.modeSummary.setText(!showColck ? R.string.super_power_mode_summary_no_clock
                    : R.string.super_power_mode_summary);
        }
        // Modify by zhiheng.huang on 2019/12/30 for  end
        holder.modeTime.setText(getTimeString(PowerConsts.SUPER_MODE));
        holder.dashview.setVisibility(View.VISIBLE);
        if (false)
            disableclick(holder);
    }


    private void disableclick(ModeHolder holder) {
        holder.radioButton.setAlpha(0.3f);
        holder.modeSummary.setAlpha(0.3f);
        holder.modeTime.setAlpha(0.3f);
        holder.part3View.setAlpha(0.3f);
        holder.modeTitle.setAlpha(0.3f);
        /*holder.aSwitch.setAlpha(0.3f);*/
        holder.aSwitch.setOnCheckedChangeListener(null);
        holder.aSwitch.setFocusable(false);
        holder.aSwitch.setEnabled(false);
        holder.aSwitch.setChecked(false);
    }

    private void initCheckComsuptionDetailsItem(ModeHolder holder) {
        holder.headIcon.setVisibility(View.VISIBLE);
        holder.headIcon.setImageResource(R.drawable.power_consumption_icon);
        holder.radioButton.setVisibility(View.GONE);
        holder.modeSummary.setVisibility(View.VISIBLE);
        holder.modeTime.setVisibility(View.GONE);
        holder.partTwoView.setVisibility(View.VISIBLE);
        holder.part3View.setVisibility(View.GONE);
        holder.secIcon.setImageResource(R.drawable.right_arrow);
        holder.modeTitle.setText(R.string.back_consumption_details_title);
        holder.modeSummary.setText(R.string.back_consumption_details_summary);
        holder.dashview.setVisibility(View.GONE);
    }

    private String getTimeString(int mode) {
        if (mode == PowerConsts.NORMAL_MODE) {
            return mPowerTimer.getTimeStrInNormalMode(
                    mContext.getResources().getString(R.string.time_use_in_configurable_mode_default),
                    mIPowerService);
        } else if (mode == PowerConsts.SUPER_MODE) {
            return getTimeStrInSuperMode();
        } else {
            return "";
        }
    }

    private String getTimeStrInSuperMode() {
        String str = null;
        int time = 0;
        str = mContext.getResources().getString(R.string.time_use_in_supermode);
        time = mPowerTimer.getTimeInSuperMode();
        str = String.format(str, mPowerTimer.formatTime(time));
        Log.d(TAG, "getTimeStrInSuperMode(), str = " + str);
        return str;
    }

    private static class ModeHolder {
        View partOneView;
        View partTwoView;
        View part3View;
        RadioButton radioButton;
        ImageView headIcon;
        TextView modeTitle;
        TextView modeSummary;
        TextView modeTime;
        ImageView secIcon;
        Switch aSwitch;
        View dashview;
    }

    public void setPowerService(IPowerService mService) {
        mIPowerService = mService;
    }

    public void removePopupDialogs() {
        if (mModeDescriptionDialog == null) {
            return;
        }
        mModeDescriptionDialog.onCloseDialog();
    }

}
