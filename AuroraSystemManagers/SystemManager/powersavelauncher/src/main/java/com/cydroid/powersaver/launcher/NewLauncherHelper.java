package com.cydroid.powersaver.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.powersaver.launcher.settings.SettingActivity;
import com.cydroid.powersaver.launcher.util.BatteryStateHelper;
import com.cydroid.powersaver.launcher.util.Log;

import java.util.ArrayList;
import java.util.List;

//Chenyee zhangjianwei 2018-3-29 add for SW17W16IP-127 begin
import java.util.Locale;
//Chenyee zhangjianwei 2018-3-29 add for SW17W16IP-127 end

/**
 * Created by xionghg on 17-5-2.
 */

public class NewLauncherHelper {
    private static final String TAG = "Main_Helper";

    private Main mActivity;
    /**
     * Current desktop mode
     */
    private Status mStatus = Status.Normal;
    /**
     * Display standby time
     */
    private TextView mStandByTimeView;
    /**
     * Display available time
     */
    private TextView mUseTimeView;

    private List<Function> mFunctionList = new ArrayList<>();
    /**
     * Hold views of the three extra apps
     */
    private ExtraAppViewHolder[] mExtraAppViewHolders;
    private FrameLayout mSettingView;
    private PackageManager mPackageManager;

    public NewLauncherHelper(Main activity) {
        mActivity = activity;
        mPackageManager = mActivity.getApplicationContext().getPackageManager();

        // initialize TimeView first
        mStandByTimeView = (TextView) mActivity.findViewById(R.id.time_textview);
        mUseTimeView = (TextView) mActivity.findViewById(R.id.time_textview2);
    }

    public void onCreate() {
        Log.d(TAG, "onCreate");
        initExtraAppViews();
        restoreFunction();
        initButtons();
        setOnLongClickListenerForFixedApps();

        mSettingView = (FrameLayout) mActivity.findViewById(R.id.setting_view);
        mSettingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                mActivity.startActivity(intent);
            }
        });

        showGuideInterface();
    }

    private void showGuideInterface() {
        if (ConfigUtil.isFirstTime(mActivity)) {
            Log.d(TAG, "isFirstTime=true, enter guide mode");
            mStatus = Status.Guide;
//            RelativeLayout root = mActivity.findViewById(R.id.root);
//            ViewParent parent = root.getParent();
//            if (parent instanceof FrameLayout) {
//                ((FrameLayout) parent).addView(...);
//            }
            final RelativeLayout guideView1 = (RelativeLayout) mActivity.findViewById(R.id.guide_view1);
            final RelativeLayout guideView2 = (RelativeLayout) mActivity.findViewById(R.id.guide_view2);
            TextView nextTextView1 = (TextView) mActivity.findViewById(R.id.guide1_next_text);
            TextView nextTextView2 = (TextView) mActivity.findViewById(R.id.guide2_next_text);

            Log.d(TAG, "guide mode show first guide interface");
            guideView1.setVisibility(View.VISIBLE);
            nextTextView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guideView1.setVisibility(View.GONE);
                    Log.d(TAG, "guide mode show second guide interface");
                    guideView2.setVisibility(View.VISIBLE);
                }
            });
            nextTextView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guideView2.setVisibility(View.GONE);
                    Log.d(TAG, "exit guide mode success");
                    mStatus = Status.Normal;
                    ConfigUtil.setFirstTime(mActivity, false);
                }
            });
        }
    }

    private void initExtraAppViews() {
        mExtraAppViewHolders = new ExtraAppViewHolder[]{new ExtraAppViewHolder(), new ExtraAppViewHolder(), new ExtraAppViewHolder()};
        mExtraAppViewHolders[0].containerView = (RelativeLayout) mActivity.findViewById(R.id.extra_app0);
        mExtraAppViewHolders[0].iconImage = (ImageView) mActivity.findViewById(R.id.extra_app0_image);
        mExtraAppViewHolders[0].labelText = (TextView) mActivity.findViewById(R.id.extra_app0_text);
        mExtraAppViewHolders[0].clearImage = (ImageView) mActivity.findViewById(R.id.extra_app0_clear_image);

        mExtraAppViewHolders[1].containerView = (RelativeLayout) mActivity.findViewById(R.id.extra_app1);
        mExtraAppViewHolders[1].iconImage = (ImageView) mActivity.findViewById(R.id.extra_app1_image);
        mExtraAppViewHolders[1].labelText = (TextView) mActivity.findViewById(R.id.extra_app1_text);
        mExtraAppViewHolders[1].clearImage = (ImageView) mActivity.findViewById(R.id.extra_app1_clear_image);

        mExtraAppViewHolders[2].containerView = (RelativeLayout) mActivity.findViewById(R.id.extra_app2);
        mExtraAppViewHolders[2].iconImage = (ImageView) mActivity.findViewById(R.id.extra_app2_image);
        mExtraAppViewHolders[2].labelText = (TextView) mActivity.findViewById(R.id.extra_app2_text);
        mExtraAppViewHolders[2].clearImage = (ImageView) mActivity.findViewById(R.id.extra_app2_clear_image);
    }

    public void onExit() {
        Log.d(TAG, "onExit");
        mActivity = null;
    }

    public void setRemainingTime(String chargingStr, boolean isCharging, int canUseTime) {
        if (isCharging) {
            // mStandByTimeView.setText(chargingStr);
            // mUseTimeView.setText("");
            mUseTimeView.setText(chargingStr);
        } else {
            final Resources mRes = mActivity.getResources();
            // String str = mRes.getString(R.string.remaining_time);
            // str += getTimeString(mRes, time);
            // mStandByTimeView.setText(str);
            String str2 = mRes.getString(R.string.remaining_time_2);
            str2 += getTimeString(mRes, canUseTime);
            mUseTimeView.setText(str2);
        }
    }

    //Chenyee zhangjianwei 2018-3-29 add for SW17W16IP-127 begin
    protected static String getTimeString(Resources mRes, int time) {

        //Chenyee zhangjianwei 2018-3-29 add for SW17W16IP-127 begin
        Locale locale =  Locale.getDefault(); 
        String country = locale.getCountry();
        String lanuage = locale.getLanguage(); 
        //Chenyee zhangjianwei 2018-3-29 add for SW17W16IP-127 end

        String result = "";
        if (time < 0) {
            result = mRes.getString(R.string.power_cannotget);
        } else {
            int hour = time / 60;
            int minute = time % 60;

            if(country.equals("IL") && lanuage.equals("iw")){
               String minuteStr = mRes.getQuantityString(R.plurals.remaining_time_minute, minute, minute);
               result += minuteStr;
              if (hour > 0) {
                String hourStr = mRes.getQuantityString(R.plurals.remaining_time_hour, hour, hour);
                result += hourStr;
              }
            }else {
                if (hour > 0) {
                   String hourStr = mRes.getQuantityString(R.plurals.remaining_time_hour, hour, hour);
                   result += hourStr;
                }
                String minuteStr = mRes.getQuantityString(R.plurals.remaining_time_minute, minute, minute);
                result += minuteStr;
            }
        }

        return result;
    }
    //Chenyee zhangjianwei 2018-3-29 add for SW17W16IP-127 end

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode = " + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            // TODO: make Function implements Parcelable
            Function function = (Function) data.getSerializableExtra(AppPickerActivity.PICK_DATA);
            if (function.functionNull) {
                ConfigUtil.saveCustomization(mActivity, requestCode, null);
            } else {
                ConfigUtil.saveCustomization(mActivity, requestCode, function.packageName + ":" + function.activityName);
            }
            restoreFunction();
            initButtons();
        }
    }

    /**
     * Restore saved custom functions, the job of finding activities is in initButton()
     */
    private void restoreFunction() {
        String[] functionStrings = ConfigUtil.getFunctions(mActivity);
        mFunctionList.clear();
        for (int i = 0; i < functionStrings.length; i++) {
            Function function = new Function();
            String funcString = functionStrings[i];
            Log.i(TAG, "restoreFunction, function" + i + " is: " + funcString);

            if (funcString.length() == 1) {
                // current index has no function
                function.functionNull = true;
            } else if (funcString.length() > 1) {
                function.functionNull = false;
                int sum = funcString.split(":").length;
                function.packageName = funcString.split(":")[0];
                if (sum <= 1) {
                    function.activityName = "";
                } else {
                    function.activityName = funcString.split(":")[1];
                }
            }
            mFunctionList.add(function);
        }
    }

    /**
     * init all Buttons
     */
    private void initButtons() {
        for (int i = 0; i < mFunctionList.size(); i++) {
            initButton(i, true);
        }
    }

    /**
     * init single Buttons
     *
     * @param index
     * @param setListener whether set OnClickListener
     */
    private void initButton(final int index, boolean setListener) {
        if (index < 0 || index >= 3) {
            Log.e(TAG, "initButton: index error");
            return;
        }
        Function function = mFunctionList.get(index);
        Log.d(TAG, "init Button" + index + ", pkg=" + function.packageName);

        String title = "";
        Drawable top;

        if (function.functionNull) {
            title = mActivity.getString(R.string.add_app);
            top = mActivity.getResources().getDrawable(R.drawable.add_bg);
            if (setListener) {
                mExtraAppViewHolders[index].containerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent pickerIntent = new Intent(mActivity, AppPickerActivity.class);
                        mActivity.startActivityForResult(pickerIntent, index);
                    }
                });
            }

            mExtraAppViewHolders[index].containerView.setOnLongClickListener(null);
            mExtraAppViewHolders[index].clearImage.setOnClickListener(null);
            mExtraAppViewHolders[index].clearImage.setVisibility(View.GONE);
        } else {
            Intent intent = new Intent();
            if (TextUtils.isEmpty(function.activityName)) {
                intent = mPackageManager.getLaunchIntentForPackage(function.packageName);
            } else {
                ComponentName cn = new ComponentName(function.packageName, function.activityName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setComponent(cn);
            }
            List<ResolveInfo> list = mPackageManager.queryIntentActivities(intent, 0);
            if (list.size() == 0) {
                Log.e(TAG, "can not find activity for package: " + function.packageName
                        + ", which could be frozen or uninstalled");
                // 未找到应用，该位置置空，重新加载
                function.functionNull = true;
                function.packageName = null;
                ConfigUtil.saveCustomization(mActivity, index, null);
                initButton(index, true);
                return;
            }

            ResolveInfo info = list.get(0);
            title = (String) info.loadLabel(mPackageManager);
            top = info.loadIcon(mPackageManager);
            // Set extra icons to gray scale
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0.0f);
            top.setColorFilter(new ColorMatrixColorFilter(cm));

            mExtraAppViewHolders[index].containerView.setTag(intent);
            mExtraAppViewHolders[index].containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent2 = (Intent) v.getTag();
                    intent2.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    mActivity.startActivity(intent2);
                }
            });
            mExtraAppViewHolders[index].containerView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    enterClearMode();
                    return true;
                }
            });
        }
        //chenyee zhaocaili 20180526 add for CSW1703CX-97 begin
        resetIconViewSize(mExtraAppViewHolders[index].iconImage, function.packageName);
        //chenyee zhaocaili 20180526 add for CSW1703CX-97 end
        mExtraAppViewHolders[index].iconImage.setImageDrawable(top);
        mExtraAppViewHolders[index].labelText.setText(title);
    }

    /**
     * Enter clear mode and cancel containerView's OnClickListener
     */
    protected void enterClearMode() {
        if (mStatus == Status.Clearing) {
            Log.d(TAG, "enterClearMode: now in clear mode, return");
            return;
        }
        Log.e(TAG, "enterClearMode:");
        mStatus = Status.Clearing;
        setOnClickListenerForFixedApps(null);
        for (int i = 0; i < mFunctionList.size(); i++) {
            Function function = mFunctionList.get(i);
            Log.d(TAG, "clear ClickListener on position" + i + ", pkg=" + function.packageName);
            mExtraAppViewHolders[i].containerView.setOnClickListener(null);

            final int funcPosition = i;
            if (!function.functionNull) {
                mExtraAppViewHolders[funcPosition].clearImage.setVisibility(View.VISIBLE);
                mExtraAppViewHolders[funcPosition].clearImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "clear function" + funcPosition);
                        ConfigUtil.saveCustomization(mActivity, funcPosition, null);
                        restoreFunction();
                        initButton(funcPosition, false);
                        checkModeAfterClearApp();
                    }
                });
            }
        }
    }

    /**
     * Set the OnClickListener for the fixed apps
     *
     * @param onClickListener
     */
    protected void setOnClickListenerForFixedApps(View.OnClickListener onClickListener) {
        mActivity.mPhoneView.setOnClickListener(onClickListener);
        mActivity.mSmsView.setOnClickListener(onClickListener);
        mActivity.mContactsView.setOnClickListener(onClickListener);
    }

    /**
     * Set the OnLongClickListener for the fixed application
     */
    protected void setOnLongClickListenerForFixedApps() {
        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Try entering clear mode
                for (Function function : mFunctionList) {
                    if (!function.functionNull) {
                        Log.d(TAG, "try entering clear mode success");
                        enterClearMode();
                        return true;
                    }
                }
                Log.d(TAG, "try entering clear mode fail, cause no extra apps added");
                return true;
            }
        };
        mActivity.mPhoneView.setOnLongClickListener(onLongClickListener);
        mActivity.mSmsView.setOnLongClickListener(onLongClickListener);
        mActivity.mContactsView.setOnLongClickListener(onLongClickListener);
    }

    /**
     * Check if all custom functions are cleared, if so, resume normal mode
     */
    private void checkModeAfterClearApp() {
        if (mStatus == Status.Clearing) {
            int count = 0;
            for (Function function : mFunctionList) {
                if (function.functionNull) {
                    count++;
                }
            }
            if (count == 3) {
                Log.d(TAG, "exit clear mode cause all functions are null");
                exitClearMode();
            }
        }
    }

    /**
     * Exit clear mode and hide clearImage
     */
    protected void exitClearMode() {
        if (mStatus != Status.Clearing) {
            return;
        }
        Log.e(TAG, "exitClearMode: ");
        mStatus = Status.Normal;
        setOnClickListenerForFixedApps(mActivity);
        for (int i = 0; i < mExtraAppViewHolders.length; i++) {
            mExtraAppViewHolders[i].clearImage.setOnClickListener(null);
            mExtraAppViewHolders[i].clearImage.setVisibility(View.GONE);
        }
        initButtons();
    }

    public void onPause() {
        exitClearMode();
    }

    private SparseArray<Long> batteryArray = new SparseArray<>();
    private static final boolean TEST_BATTERY = false;
    /**
     * Use the percentage of power to calculate the average current for a while,
     * Just use in test period
     */
    public void onBatteryChanged() {
        if (TEST_BATTERY) {
            long currentTime = SystemClock.elapsedRealtime();
            int currentLevel = BatteryStateHelper.getBatteryLevel(mActivity);
            if (batteryArray.get(currentLevel) != null) {
                Log.d(TAG, "onBatteryChanged: level exists, return");
                return;
            }
            batteryArray.put(currentLevel, currentTime);
            int size = batteryArray.size();
            if (size <= 1) {
                Log.d(TAG, "onBatteryChanged: return for size=" + size);
                return;
            }

            int beginLevel = batteryArray.keyAt(1);
            long beginTime = batteryArray.get(beginLevel);
            if (beginLevel <= currentLevel) {
                String text1 = "电量错误,初始值: " + beginLevel + ",当前值: " + currentLevel;
                Log.d(TAG, "onBatteryChanged: " + text1);
                return;
            }

            float timePassed = (currentTime - beginTime) / ((float) (60 * 60 * 1000)); // hour
            float capacity = (beginLevel - currentLevel) * 45.0F;  // mAh
            float electric = capacity / timePassed; // mA
            String text2 = String.format("间隔: %.4f h,消耗: %.2f mAh,电流值:%.2f mA", timePassed, capacity, electric);
            Log.e(TAG, "onBatteryChanged: " + text2);
            if (batteryArray.size() == 100) {
                batteryArray.removeAtRange(10, 100);
            }
        }
    }

    public enum Status {
        Normal, Clearing, Guide
    }

    static class ExtraAppViewHolder {
        RelativeLayout containerView;
        ImageView iconImage;
        TextView labelText;
        ImageView clearImage;
    }

    //chenyee zhaocaili 20180526 add for CSW1703CX-97 begin
    private void resetIconViewSize(ImageView view, String pkg){
        if (view != null && pkg != null && ConfigUtil.cy1703CX && (pkg.contains("tencent") || pkg.equals("com.sina.weibo")
                || pkg.contains("facebook") || pkg.equals("com.immomo.momo") || pkg.equals("com.android.calculator2"))){
            ViewGroup.LayoutParams para = view.getLayoutParams();
            int size = mActivity.getResources().getDimensionPixelSize(R.dimen.extra_app_image_size);
            Log.d(TAG, "image size = " + size);
            para.height = size;
            para.width = size;
            view.setLayoutParams(para);
        }
    }
    //chenyee zhaocaili 20180526 add for CSW1703CX-97 end
}
