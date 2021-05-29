/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cydroid.softmanager.powersaver.fuelgauge;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.util.FastPrintWriter;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.powersaver.activities.NewPowerUsageDetailActivity;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeTextView;

import static com.cydroid.softmanager.common.Consts.cyODMFlag;

public class NewPowerUsageDetailFragment extends Fragment implements CyeeButton.OnClickListener {

    private static final String GN_WIFI_SETTINGS_ACTIVITY = "com.android.settings/com.android.settings.wifi.WifiSettingsActivity";
    //guoxt modify for CSW1805A-361 begin
    // Note: Must match the sequence of the DrainType
    private static int[] sDrainTypeDesciptions = new int[] {R.string.battery_desc_standby,
            R.string.battery_desc_radio, R.string.battery_desc_voice, R.string.battery_desc_wifi,
            R.string.battery_desc_bluetooth, R.string.battery_desc_flashlight, R.string.battery_desc_display,
            R.string.battery_desc_apps, R.string.battery_desc_users, R.string.battery_desc_unaccounted,
            R.string.battery_desc_overcounted,R.string.battery_desc_overcounted,R.string.battery_desc_overcounted};

    public enum DrainType {
        AMBIENT_DISPLAY,
        APP,
        BLUETOOTH,
        CAMERA,
        CELL,
        FLASHLIGHT,
        IDLE,
        MEMORY,
        OVERCOUNTED,
        PHONE,
        SCREEN,
        UNACCOUNTED,
        USER,
        WIFI,
    }
    private static final int[] sDrainTypeDesciptions_P = new int[] {
        R.string.battery_desc_standby,
        R.string.battery_desc_apps,
        R.string.battery_desc_bluetooth,
        R.string.battery_desc_standby,//camera
        R.string.battery_desc_radio,
        R.string.battery_desc_flashlight,
        R.string.battery_desc_standby,
        R.string.battery_desc_standby,//memory
        R.string.battery_desc_overcounted,
        R.string.battery_desc_voice,
        R.string.battery_desc_display,
        R.string.battery_desc_unaccounted,
        R.string.battery_desc_users,
        R.string.battery_desc_wifi_new};
    //guoxt modify for CSW1805A-361 end

    public static void startBatteryDetailPage(Context caller, BatteryStatsHelper helper, int statsType,
            ExtendedBatterySipper entry, boolean showLocationButton) {
        // Initialize mStats if necessary.
        helper.getStats();

        // final int dischargeAmount = helper.getStats().getDischargeAmount(statsType);
        Bundle args = new Bundle();
        String pkgLable = entry.getName();
        try {
            pkgLable = HelperUtils.loadLabel(caller,
                    HelperUtils.getApplicationInfo(caller, entry.getDefaultPackageName()));
        } catch (Exception e) {
            pkgLable = entry.getName();
        }
        args.putString(NewPowerUsageDetailFragment.EXTRA_TITLE, pkgLable);
        args.putInt(NewPowerUsageDetailFragment.EXTRA_PERCENT, entry.getPercent());
        // args.putInt(NewPowerUsageDetailFragment.EXTRA_GAUGE,
        // (int) Math.ceil(entry.sipper.value * 100 / helper.getMaxPower()));
        args.putLong(NewPowerUsageDetailFragment.EXTRA_USAGE_DURATION, helper.getStatsPeriod());
        args.putString(NewPowerUsageDetailFragment.EXTRA_ICON_PACKAGE, entry.getDefaultPackageName());
        args.putInt(NewPowerUsageDetailFragment.EXTRA_ICON_ID, entry.getIconId());
        args.putDouble(NewPowerUsageDetailFragment.EXTRA_NO_COVERAGE, entry.batterSipper.noCoveragePercent);
        if (entry.batterSipper.uidObj != null) {
            args.putInt(NewPowerUsageDetailFragment.EXTRA_UID, entry.batterSipper.uidObj.getUid());
        }
        args.putSerializable(NewPowerUsageDetailFragment.EXTRA_DRAIN_TYPE, entry.batterSipper.drainType);
        args.putBoolean(NewPowerUsageDetailFragment.EXTRA_SHOW_LOCATION_BUTTON, showLocationButton);

        // int userId = UserHandle.myUserId();
        int[] types;
        double[] values;
        switch (entry.batterSipper.drainType) {
            case APP:
            case USER:
                BatteryStats.Uid uid = entry.batterSipper.uidObj;
                types = new int[] {/*R.string.usage_type_computed_power, */R.string.usage_type_cpu,
                        R.string.usage_type_cpu_foreground, R.string.usage_type_wake_lock,
                        R.string.usage_type_gps, R.string.usage_type_wifi_running_new,
                        R.string.usage_type_data_recv, R.string.usage_type_data_send,
                        R.string.usage_type_radio_active, R.string.usage_type_data_wifi_recv_new,
                        R.string.usage_type_data_wifi_send_new, R.string.usage_type_audio,
                        R.string.usage_type_video};
                values = new double[] {/*entry.getSortValue(),*/ entry.getCpuTime(), entry.getCpuFgTime(),
                        entry.getWakeLockTime(), entry.getGpsTime(), entry.getWifiRunningTime(),
                        entry.batterSipper.mobileRxPackets, entry.batterSipper.mobileTxPackets,
                        entry.batterSipper.mobileActive, entry.batterSipper.wifiRxPackets,
                        entry.batterSipper.wifiTxPackets, 0, 0};
                if (entry.batterSipper.drainType == BatterySipper.DrainType.APP) {
                    Writer result = new StringWriter();
                    PrintWriter printWriter = new FastPrintWriter(result, false, 1024);
					if(helper.getStats()!=null){
						helper.getStats().dumpLocked(caller, printWriter, "", helper.getStatsType(),
								uid.getUid());
					}
                    printWriter.flush();
                    args.putString(NewPowerUsageDetailFragment.EXTRA_REPORT_DETAILS, result.toString());

                    result = new StringWriter();
                    printWriter = new FastPrintWriter(result, false, 1024);
                    helper.getStats().dumpCheckinLocked(caller, printWriter, helper.getStatsType(),
                            uid.getUid());
                    printWriter.flush();
                    args.putString(NewPowerUsageDetailFragment.EXTRA_REPORT_CHECKIN_DETAILS,
                            result.toString());
                    // userId = UserHandle.getUserId(uid.getUid());
                }
                break;
            case CELL:
                types = new int[] {/*R.string.usage_type_computed_power, */R.string.usage_type_on_time,
                        R.string.usage_type_no_coverage, R.string.usage_type_radio_active};
                values = new double[] {/*entry.getSortValue(), */entry.getUsageTime(),
                        entry.batterSipper.noCoveragePercent, entry.batterSipper.mobileActive};
                break;
            case WIFI:
                types = new int[] {/*R.string.usage_type_computed_power, */R.string.usage_type_wifi_running_new,
                        R.string.usage_type_cpu, R.string.usage_type_cpu_foreground,
                        R.string.usage_type_wake_lock, R.string.usage_type_data_recv,
                        R.string.usage_type_data_send, R.string.usage_type_data_wifi_recv_new,
                        R.string.usage_type_data_wifi_send_new};
                values = new double[] {/*entry.getSortValue(), */entry.getUsageTime(), entry.getCpuTime(),
                        entry.getCpuFgTime(), entry.getWakeLockTime(), entry.batterSipper.mobileRxPackets,
                        entry.batterSipper.mobileTxPackets, entry.batterSipper.wifiRxPackets,
                        entry.batterSipper.wifiTxPackets,};
                break;
            case BLUETOOTH:
                types = new int[] {/*R.string.usage_type_computed_power, */R.string.usage_type_on_time,
                        R.string.usage_type_cpu, R.string.usage_type_cpu_foreground,
                        R.string.usage_type_wake_lock, R.string.usage_type_data_recv,
                        R.string.usage_type_data_send, R.string.usage_type_data_wifi_recv_new,
                        R.string.usage_type_data_wifi_send_new};
                values = new double[] {/*entry.getSortValue(),*/ entry.getUsageTime(), entry.getCpuTime(),
                        entry.getCpuFgTime(), entry.getWakeLockTime(), entry.batterSipper.mobileRxPackets,
                        entry.batterSipper.mobileTxPackets, entry.batterSipper.wifiRxPackets,
                        entry.batterSipper.wifiTxPackets,};
                break;
            case UNACCOUNTED:
                types = new int[] {R.string.usage_type_total_battery_capacity,
                        R.string.usage_type_computed_power, R.string.usage_type_actual_power};
                values = new double[] {helper.getPowerProfile().getBatteryCapacity(),
                        helper.getComputedPower(), helper.getMinDrainedPower(),};
                break;
            case OVERCOUNTED:
                types = new int[] {R.string.usage_type_total_battery_capacity,
                        R.string.usage_type_computed_power, R.string.usage_type_actual_power};
                values = new double[] {helper.getPowerProfile().getBatteryCapacity(),
                        helper.getComputedPower(), helper.getMaxDrainedPower(),};
                break;
            default:
                types = new int[] {/*R.string.usage_type_computed_power, */R.string.usage_type_on_time};
                values = new double[] {/*entry.getSortValue(), */entry.getUsageTime()};
        }
        args.putIntArray(NewPowerUsageDetailFragment.EXTRA_DETAIL_TYPES, types);
        args.putDoubleArray(NewPowerUsageDetailFragment.EXTRA_DETAIL_VALUES, values);

        // This is a workaround, see b/17523189
        /*
        if (userId == UserHandle.myUserId()) {
            caller.startPreferencePanel(NewPowerUsageDetailFragment.class.getName(), args,
                    R.string.details_title, null, null, 0);
        } else {
        caller.startPreferencePanelAsUser(NewPowerUsageDetailFragment.class.getName(), args,
                R.string.details_title, null, UserHandle.CURRENT);
        }*/
        Intent intent = new Intent();
        intent.putExtras(args);
        intent.setAction(NewPowerUsageDetailActivity.ACTION_USAGE_DETAIL);
        caller.startActivity(intent);
    }

    public static final int ACTION_DISPLAY_SETTINGS = 1;
    public static final int ACTION_WIFI_SETTINGS = 2;
    public static final int ACTION_BLUETOOTH_SETTINGS = 3;
    public static final int ACTION_WIRELESS_SETTINGS = 4;
    public static final int ACTION_APP_DETAILS = 5;
    public static final int ACTION_LOCATION_SETTINGS = 6;
    public static final int ACTION_FORCE_STOP = 7;
    public static final int ACTION_REPORT = 8;

    public static final int USAGE_SINCE_UNPLUGGED = 1;
    public static final int USAGE_SINCE_RESET = 2;

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_PERCENT = "percent";
    public static final String EXTRA_GAUGE = "gauge";
    public static final String EXTRA_UID = "uid";
    public static final String EXTRA_USAGE_SINCE = "since";
    public static final String EXTRA_USAGE_DURATION = "duration";
    public static final String EXTRA_REPORT_DETAILS = "report_details";
    public static final String EXTRA_REPORT_CHECKIN_DETAILS = "report_checkin_details";
    public static final String EXTRA_DETAIL_TYPES = "types"; // Array of usage types (cpu, gps, etc)
    public static final String EXTRA_DETAIL_VALUES = "values"; // Array of doubles
    public static final String EXTRA_DRAIN_TYPE = "drainType"; // DrainType
    public static final String EXTRA_ICON_PACKAGE = "iconPackage"; // String
    public static final String EXTRA_NO_COVERAGE = "noCoverage";
    public static final String EXTRA_ICON_ID = "iconId"; // Int
    public static final String EXTRA_SHOW_LOCATION_BUTTON = "showLocationButton"; // Boolean

    private PackageManager mPm;
    private DevicePolicyManager mDpm;
    private String mTitle;
    // private int mUsageSince;
    private int[] mTypes;
    private int mUid;
    private double[] mValues;
    private View mRootView;
    private CyeeTextView mTitleView;
    private ViewGroup mTwoButtonsPanel;
    private CyeeButton mForceStopButton;
    private CyeeButton mReportButton;
    private ViewGroup mDetailsParent;
    private ViewGroup mControlsParent;
    private ViewGroup mMessagesParent;
    // private long mStartTime;
    private BatterySipper.DrainType mDrainType;
    private Drawable mAppIcon;
    private double mNoCoverage; // Percentage of time that there was no coverage

    private boolean mUsesGps;
    private boolean mShowLocationButton;

    private static final String TAG = "PowerUsageDetail";
    private String[] mPackages;

    ApplicationInfo mApp;
    ComponentName mInstaller;
    private int mProgress;
    private String mProgressText;
    private CyeeTextView mDetailsTitle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPm = getActivity().getPackageManager();
        mDpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.power_usage_details, container, false);
        Utils.prepareCustomPreferencesList(container, view, view, false);

        mRootView = view;
        createDetails();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // mStartTime = android.os.Process.getElapsedCpuTime();
        checkForceStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setPercent(int percentOfMax, int percentOfTotal) {
        mProgress = percentOfMax / 10;
        mProgressText = getActivity().getString(R.string.percentage, (double) percentOfTotal / 10d);
    }

    private void createDetails() {
        final Bundle args = getArguments();
        mTitle = args.getString(EXTRA_TITLE);
        final int percentage = args.getInt(EXTRA_PERCENT, 1);
        // final int gaugeValue = args.getInt(EXTRA_GAUGE, 1);
        // mUsageSince = args.getInt(EXTRA_USAGE_SINCE, USAGE_SINCE_UNPLUGGED);
        mUid = args.getInt(EXTRA_UID, 0);
        mDrainType = (BatterySipper.DrainType) args.getSerializable(EXTRA_DRAIN_TYPE);
        mNoCoverage = args.getDouble(EXTRA_NO_COVERAGE, 0);
        String iconPackage = args.getString(EXTRA_ICON_PACKAGE);
        int iconId = args.getInt(EXTRA_ICON_ID, 0);
        mShowLocationButton = args.getBoolean(EXTRA_SHOW_LOCATION_BUTTON);
        if (!TextUtils.isEmpty(iconPackage)) {
            try {
                final PackageManager pm = getActivity().getPackageManager();
                ApplicationInfo ai = pm.getPackageInfo(iconPackage, 0).applicationInfo;
                if (ai != null) {
                    mAppIcon = ai.loadIcon(pm);
                }
            } catch (NameNotFoundException nnfe) {
                // Use default icon
            }
        } else if (iconId != 0) {
            try {
                mAppIcon = getActivity().getDrawable(iconId);
            } catch (Exception e) {
                Log.d(TAG, "can not get icondrawable for id " + iconId);
                mAppIcon = null;
            }
        }
        if (mAppIcon == null) {
            mAppIcon = getActivity().getPackageManager().getDefaultActivityIcon();
        }

        getActivity().setTitle(getDescriptionForDrainType());
        // Set the description
        /*
        final CyeeTextView summary = (CyeeTextView) mRootView.findViewById(R.id.sipper_summary);
        summary.setText(getDescriptionForDrainType());
        summary.setVisibility(View.VISIBLE);
        */

        mTypes = args.getIntArray(EXTRA_DETAIL_TYPES);
        mValues = args.getDoubleArray(EXTRA_DETAIL_VALUES);

        mTitleView = (CyeeTextView) mRootView.findViewById(R.id.title);
        mTitleView.setText(mTitle);

        setPercent(percentage, percentage);

        final CyeeTextView text1 = (CyeeTextView) mRootView.findViewById(R.id.summary);
        text1.setText(mProgressText);

        mTwoButtonsPanel = (ViewGroup) mRootView.findViewById(R.id.two_buttons_panel);
        mForceStopButton = (CyeeButton) mRootView.findViewById(R.id.left_button);
        mReportButton = (CyeeButton) mRootView.findViewById(R.id.right_button);
        mForceStopButton.setEnabled(false);

        final ProgressBar progress = (ProgressBar) mRootView.findViewById(R.id.progress);
        // progress.setProgress(gaugeValue);
        progress.setProgress(mProgress);

        final ImageView icon = (ImageView) mRootView.findViewById(android.R.id.icon);
        icon.setImageDrawable(mAppIcon);
        if (mDrainType != BatterySipper.DrainType.APP && ChameleonColorManager.isNeedChangeColor()
                && UiUtils.isSpecialStyleModel()) {
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            icon.getDrawable().setTint(color_T1);
        }

        mDetailsTitle = (CyeeTextView) mRootView.findViewById(R.id.details_title);
        mDetailsTitle.setVisibility(View.GONE);
        mDetailsParent = (ViewGroup) mRootView.findViewById(R.id.details);
        mControlsParent = (ViewGroup) mRootView.findViewById(R.id.controls);
        mMessagesParent = (ViewGroup) mRootView.findViewById(R.id.messages);

        fillDetailsSection();
        fillPackagesSection(mUid);
        fillControlsSection(mUid);
        fillMessagesSection(mUid);

        if (mUid >= Process.FIRST_APPLICATION_UID) {
            mForceStopButton.setText(R.string.force_stop);
            mForceStopButton.setTag(ACTION_FORCE_STOP);
            mForceStopButton.setOnClickListener(this);
            mReportButton.setText(com.android.internal.R.string.report);
            mReportButton.setTag(ACTION_REPORT);
            mReportButton.setOnClickListener(this);

            // check if error reporting is enabled in secure settings
            int enabled = android.provider.Settings.Global.getInt(getActivity().getContentResolver(),
                    android.provider.Settings.Global.SEND_ACTION_APP_ERROR, 0);
            if (enabled != 0) {
                if (mPackages != null && mPackages.length > 0) {
                    try {
                        mApp = getActivity().getPackageManager().getApplicationInfo(mPackages[0], 0);
                        mInstaller = ApplicationErrorReport.getErrorReportReceiver(getActivity(),
                                mPackages[0], mApp.flags);
                    } catch (NameNotFoundException e) {
                    }
                }
                mReportButton.setEnabled(mInstaller != null);
            } else {
                mTwoButtonsPanel.setVisibility(View.GONE);
            }
        } else {
            mTwoButtonsPanel.setVisibility(View.GONE);
        }
    }

    public void onClick(View v) {
        doAction((Integer) v.getTag());
    }

    // utility method used to start sub activity
    private void startApplicationDetailsActivity() {
        final Bundle args = getArguments();
        String packageName = args.getString(EXTRA_ICON_PACKAGE);
        if (packageName != null && packageName.length() > 0) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private void startSettingActivity(String action) {
        if (action != null && action.length() > 0) {
            Intent intent = new Intent();
            intent.setAction(action);
            startActivity(intent);
        }
    }

    private void doAction(int action) {
        // SettingsActivity sa = (SettingsActivity) getActivity();
        switch (action) {
            case ACTION_DISPLAY_SETTINGS:
                startSettingActivity(Settings.ACTION_DISPLAY_SETTINGS);
                break;
            case ACTION_WIFI_SETTINGS:
                // startSettingActivity(Settings.ACTION_WIFI_SETTINGS);
                startGNWifiSetting();
                break;
            case ACTION_BLUETOOTH_SETTINGS:
                startSettingActivity(Settings.ACTION_BLUETOOTH_SETTINGS);
                break;
            case ACTION_WIRELESS_SETTINGS:
                startSettingActivity(Settings.ACTION_WIRELESS_SETTINGS);
                break;
            case ACTION_LOCATION_SETTINGS:
                startSettingActivity(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                break;
            case ACTION_APP_DETAILS:
                startApplicationDetailsActivity();
                break;
            case ACTION_FORCE_STOP:
                killProcesses();
                break;
            case ACTION_REPORT:
                reportBatteryUse();
                break;
            default:
                Log.d(TAG, "Unknow action!");
                break;
        }
    }

    private void startGNWifiSetting() {
        //guoxt modify for
        ComponentName component = ComponentName.unflattenFromString(GN_WIFI_SETTINGS_ACTIVITY);
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
       // intent.setComponent(component);
        startActivity(intent);


    }

    private void fillDetailsSection() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if (mTypes == null || mValues == null) {
            return;
        }
        if (mTypes.length == 0 || mValues.length == 0) {
            return;
        }
        int itemCount = 0;
        for (int i = 0; i < mTypes.length; i++) {
            // Only add an item if the time is greater than zero
            if (mValues[i] <= 0) {
                continue;
            }
            final String label = getString(mTypes[i]);
            String value = null;
            switch (mTypes[i]) {
                case R.string.usage_type_data_recv:
                case R.string.usage_type_data_send:
                case R.string.usage_type_data_wifi_recv_new:
                case R.string.usage_type_data_wifi_send_new:
                    final long packets = (long) (mValues[i]);
                    value = Long.toString(packets);
                    break;
                case R.string.usage_type_no_coverage:
                    final int percentage = (int) Math.floor(mValues[i]);
                    value = Utils.formatPercentage(percentage);
                    break;
                case R.string.usage_type_total_battery_capacity:
                case R.string.usage_type_computed_power:
                case R.string.usage_type_actual_power:
                    long powerVal = (long) (mValues[i]);
                    if (powerVal > 0) {
                        value = getActivity().getString(R.string.mah, (long) (powerVal));
                    } else {
                        powerVal = 1;
                        value = "<" + getActivity().getString(R.string.mah, (long) (powerVal));
                    }
                    break;
                case R.string.usage_type_gps:
                    mUsesGps = true;
                    // Fall through
                default:
                    value = Utils.formatElapsedTime(getActivity(), mValues[i], true);
            }
            ViewGroup item = (ViewGroup) inflater.inflate(R.layout.power_usage_detail_item_text, null);
            mDetailsParent.addView(item);
            Log.d(TAG, "ccc: " + value);
            itemCount++;
            CyeeTextView labelView = (CyeeTextView) item.findViewById(R.id.label);
            CyeeTextView valueView = (CyeeTextView) item.findViewById(R.id.value);
            labelView.setText(label);
            valueView.setText(value);
        }
        if (itemCount > 0) {
            mDetailsTitle.setVisibility(View.VISIBLE);
        }
    }

    private void fillControlsSection(int uid) {
        PackageManager pm = getActivity().getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);
        PackageInfo pi = null;
        try {
            pi = packages != null ? pm.getPackageInfo(packages[0], 0) : null;
        } catch (NameNotFoundException nnfe) {
            Log.d(TAG, "pkg not found " + nnfe);
        }
        ApplicationInfo ai = pi != null ? pi.applicationInfo : null;

        boolean removeHeader = true;
        switch (mDrainType) {
            case APP:
                // If it is a Java application and only one package is associated with the Uid
                if (packages != null && packages.length == 1) {
                    addControl(R.string.battery_action_app_details, R.string.battery_sugg_apps_info,
                            ACTION_APP_DETAILS);
                    removeHeader = false;
                    // If the application has a settings screen, jump to that
                    // TODO:
                }
                // If power usage detail page is launched from location page, suppress "Location"
                // CyeeButton to prevent circular loops.
                if (mUsesGps && mShowLocationButton) {
                    addControl(R.string.location_settings_title, R.string.battery_sugg_apps_gps,
                            ACTION_LOCATION_SETTINGS);
                    removeHeader = false;
                }
                break;
            case SCREEN:
                addControl(R.string.display_settings, R.string.battery_sugg_display, ACTION_DISPLAY_SETTINGS);
                removeHeader = false;
                break;
            case WIFI:
                addControl(R.string.wifi_settings_new, R.string.battery_sugg_wifi_new, ACTION_WIFI_SETTINGS);
                removeHeader = false;
                break;
            case BLUETOOTH:
                addControl(R.string.bluetooth_settings, R.string.battery_sugg_bluetooth_basic,
                        ACTION_BLUETOOTH_SETTINGS);
                removeHeader = false;
                break;
            case CELL:
                if (mNoCoverage > 10) {
                    addControl(R.string.radio_controls_title, R.string.battery_sugg_radio,
                            ACTION_WIRELESS_SETTINGS);
                    removeHeader = false;
                }
                break;
            default:
                Log.d(TAG, "Unknow DrainType!");
                break;
        }
        if (removeHeader) {
            mControlsParent.setVisibility(View.GONE);
        }
    }

    private void addControl(int title, int summary, int action) {
        final Resources res = getResources();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup item = (ViewGroup) inflater.inflate(R.layout.power_usage_action_item, null);
        mControlsParent.addView(item);
        CyeeButton actionButton = (CyeeButton) item.findViewById(R.id.action_button);
        CyeeTextView summaryView = (CyeeTextView) item.findViewById(R.id.summary);
        actionButton.setText(res.getString(title));
        summaryView.setText(res.getString(summary));
        actionButton.setOnClickListener(this);
        actionButton.setTag(Integer.valueOf(action));
        if (PowerModeUtils.getCurrentMode(getActivity()) == PowerConsts.SUPER_MODE) {
            actionButton.setVisibility(View.GONE);
        }
    }

    private void fillMessagesSection(int uid) {
        boolean removeHeader = true;
        switch (mDrainType) {
            case UNACCOUNTED:
                addMessage(R.string.battery_msg_unaccounted);
                removeHeader = false;
                break;
            default:
                Log.d(TAG, "Unknow DrainType!");
                break;
        }
        if (removeHeader) {
            mMessagesParent.setVisibility(View.GONE);
        }
    }

    private void addMessage(int message) {
        final Resources res = getResources();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View item = inflater.inflate(R.layout.power_usage_message_item, null);
        mMessagesParent.addView(item);
        CyeeTextView messageView = (CyeeTextView) item.findViewById(R.id.message);
        messageView.setText(res.getText(message));
    }

    private void removePackagesSection() {
        View view;
        if ((view = mRootView.findViewById(R.id.packages_section_title)) != null) {
            view.setVisibility(View.GONE);
        }
        if ((view = mRootView.findViewById(R.id.packages_section)) != null) {
            view.setVisibility(View.GONE);
        }
    }

    private void killProcesses() {
        if (mPackages == null) {
            return;
        }
        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        final int userId = UserHandle.getUserId(mUid);
        for (int i = 0; i < mPackages.length; i++) {
            am.forceStopPackageAsUser(mPackages[i], userId);
        }
        checkForceStop();
    }

    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mForceStopButton.setEnabled(getResultCode() != Activity.RESULT_CANCELED);
        }
    };

    private void checkForceStop() {
        if (mPackages == null || mUid < Process.FIRST_APPLICATION_UID) {
            mForceStopButton.setEnabled(false);
            return;
        }
        for (int i = 0; i < mPackages.length; i++) {
            if (mDpm.packageHasActiveAdmins(mPackages[i])) {
                mForceStopButton.setEnabled(false);
                return;
            }
        }
        for (int i = 0; i < mPackages.length; i++) {
            try {
                ApplicationInfo info = mPm.getApplicationInfo(mPackages[i], 0);
                if ((info.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
                    mForceStopButton.setEnabled(true);
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        Intent intent = new Intent(Intent.ACTION_QUERY_PACKAGE_RESTART,
                Uri.fromParts("package", mPackages[0], null));
        intent.putExtra(Intent.EXTRA_PACKAGES, mPackages);
        intent.putExtra(Intent.EXTRA_UID, mUid);
        intent.putExtra(Intent.EXTRA_USER_HANDLE, UserHandle.getUserId(mUid));
        getActivity().sendOrderedBroadcast(intent, null, mCheckKillProcessesReceiver, null,
                AppCompatActivity.RESULT_CANCELED, null, null);
    }

    private void reportBatteryUse() {
        if (mPackages == null) {
            return;
        }

        ApplicationErrorReport report = new ApplicationErrorReport();
        report.type = ApplicationErrorReport.TYPE_BATTERY;
        report.packageName = mPackages[0];
        report.installerPackageName = mInstaller.getPackageName();
        report.processName = mPackages[0];
        report.time = System.currentTimeMillis();
        report.systemApp = (mApp.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

        final Bundle args = getArguments();
        ApplicationErrorReport.BatteryInfo batteryInfo = new ApplicationErrorReport.BatteryInfo();
        batteryInfo.usagePercent = args.getInt(EXTRA_PERCENT, 1);
        batteryInfo.durationMicros = args.getLong(EXTRA_USAGE_DURATION, 0);
        batteryInfo.usageDetails = args.getString(EXTRA_REPORT_DETAILS);
        batteryInfo.checkinDetails = args.getString(EXTRA_REPORT_CHECKIN_DETAILS);
        report.batteryInfo = batteryInfo;

        Intent result = new Intent(Intent.ACTION_APP_ERROR);
        result.setComponent(mInstaller);
        result.putExtra(Intent.EXTRA_BUG_REPORT, report);
        result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(result);
    }

    private void fillPackagesSection(int uid) {
        if (uid < 1) {
            removePackagesSection();
            return;
        }
        ViewGroup packagesParent = (ViewGroup) mRootView.findViewById(R.id.packages_section);
        if (packagesParent == null) {
            return;
        }
        LayoutInflater inflater = getActivity().getLayoutInflater();

        PackageManager pm = getActivity().getPackageManager();
        // final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
        mPackages = pm.getPackagesForUid(uid);
        if (mPackages == null || mPackages.length < 2) {
            removePackagesSection();
            return;
        }

        // Convert package names to user-facing labels where possible
        for (int i = 0; i < mPackages.length; i++) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(mPackages[i], 0);
                CharSequence label = ai.loadLabel(pm);
                // Drawable icon = defaultActivityIcon;
                if (label != null) {
                    mPackages[i] = label.toString();
                }
                //fengpeipei add for 58624 start 
                if(ai.packageName.equals("com.cydroid.factorytests")){
                	mPackages[i] = ai.packageName;
                }
                //fengpeipei add for 58624 end

                //Chenyee <liu_shuang> <2017-12-23> add for SW17W16TC-139 begin
                //ODM项目不能有chenyee或cyee字样
                if (cyODMFlag && (mPackages[i].contains("chenyee") || mPackages[i].contains("cyee"))) {
                    continue;
                }
                //Chenyee <liu_shuang> <2017-12-23> add for SW17W16TC-139 end

                // if (ai.icon != 0) {
                // icon = ai.loadIcon(pm);
                // }
                View item = inflater.inflate(R.layout.power_usage_package_item, null);
                packagesParent.addView(item);
                CyeeTextView labelView = (CyeeTextView) item.findViewById(R.id.label);
                labelView.setText(mPackages[i]);
            } catch (NameNotFoundException e) {
            }
        }
    }

    private String getDescriptionForDrainType() {
        return getResources().getString(sDrainTypeDesciptions_P[mDrainType.ordinal()]);
    }
}
