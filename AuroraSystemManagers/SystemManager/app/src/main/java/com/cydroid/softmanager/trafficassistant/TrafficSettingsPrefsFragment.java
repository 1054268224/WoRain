package com.cydroid.softmanager.trafficassistant;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;

import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.preference.CyeePreference;
import cyee.preference.CyeePreference.OnPreferenceChangeListener;
import cyee.preference.CyeePreference.OnPreferenceClickListener;
import cyee.preference.CyeePreferenceCategory;
import cyee.preference.CyeePreferenceFragment;
import cyee.preference.CyeePreferenceGroup;
import cyee.preference.CyeeSwitchPreference;
import cyee.provider.CyeeSettings;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeEditText;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

public class TrafficSettingsPrefsFragment extends CyeePreferenceFragment {
    private static final String TAG = "TrafficSettingsPrefsFragment";

    public static final String NOTIFICATION_INFO = "noti_always";
    private static final String NETWORK_SPEED = "network_speed_display";
    // Gionee: mengdw <2016-01-12> add for CR01617372 begin
    private static final String KEY_CALIBRATE_SETTING_SIM1 = "key_calibrate_setting_SIM1";
    private static final String KEY_CALIBRATE_SETTING_SIM2 = "key_calibrate_setting_SIM2";
    //Gionee jiangsj 2017.05.16 add for 141074  begin
    private static final String KEY_NOTI_SOFTAP_CATEGORY = "key_noti_softap_category";
    //Gionee jiangsj 2017.05.16 add for 141074  end
    private static final String KEY_CALIBRATE_SETTING_PROVINCE_SIM1 = "key_calibrate_setting_province_SIM1";
    private static final String KEY_CALIBRATE_SETTING_PROVINCE_SIM2 = "key_calibrate_setting_province_SIM2";
    private static final String KEY_CALIBRATE_SETTING_BRAND_SIM1 = "key_calibrate_setting_brand_SIM1";
    private static final String KEY_CALIBRATE_SETTING_BRAND_SIM2 = "key_calibrate_setting_brand_SIM2";
    private static final String NOTIFI_SOFTAP_DATA_TRAFFIC_KEY = "noti_softap_setting";
    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    private static final String KEY_TRAFFIC_LOCK_SCREEN_REMIND_SETTING = "key_lockscreen_traffic_remind";
    // Gionee: mengdw <2016-12-06> add for CR01775579 end
    // Gionee: mengdw <2017-01-19> add for 65945 begin
    private static final String KEY_NOTIFICATION_CATEGORY = "key_notification_category";
    // Gionee: mengdw <2017-01-19> add for 65945 end

    // mengdw <2016-10-09> add for CR01766193 begin
    private static final int SETTING_CLOSE_INDEX = 0;
    private static final int SETTING_CLOSE_VALUE = 0;
    private static final int USER_DEFINE_SETTING_INDEX = 4;

    // mengdw <2016-10-09> add for CR01766193 end
    CyeePreferenceCategory mCalibrateSettingCategorySim1;
    CyeePreferenceCategory mCalibrateSettingCategorySim2;
    //Gionee jiangsj 2017.05.16 add for 141074  begin
    CyeePreferenceCategory mNotiSoftapCategory;
    //Gionee jiangsj 2017.05.16 add for 141074  end
    CyeePreference mCalibrateSettingProvinceSim1;
    CyeePreference mCalibrateSettingProvinceSim2;
    CyeePreference mCalibrateSettingBrandSim1;
    CyeePreference mCalibrateSettingBrandSim2;
    // Gionee: mengdw <2016-04-11> add for CR01672645 begin
    private CyeePreference mSoftapNotification;
    private CyeeEditText mUserInput;
    private AlertDialog.Builder mHotSpotSetDialogBuiler;
    private Button mHotspotPositeBtn;
    private CyeeSwitchPreference mNetworkSpeed;
    // Gionee: mengdw <2016-04-11> add for CR01672645 end
    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    private CyeePreference mTrafficLockScreenRemindPreference;
    // Gionee: mengdw <2016-12-06> add for CR01775579 end

    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    private String[] mLockScreenRemindSettingAdapter;
    // Gionee: mengdw <2016-12-06> add for CR01775579 end
    private String[] mArrayProvinceAdapter;
    private String[] mArrayBrandAdapter;
    private int mOpSimIndex = 0;
    // Gionee: mengdw <2016-01-12> add for CR01617372 end
    // Gionee: mengdw <2015-10-20> add for CR01571760 begin
    private int mSettingSelectIndex = 1;
    private String[] mArrayAdapter;
    // Gionee: mengdw <2015-10-20> add for CR01571760 end
    private Context mContext;
    private TrafficSettingControler mTrafficSettingControler;
    //Gionee yubo 2015-07-15 modify for CR01521516 begin
    private static final boolean sGnDisableCyeeSystemUI = SystemProperties.get("ro.gn.cyee_systemui_support").equals("no");
    //Gionee yubo 2015-07-15 modify for CR01521516 end

    //Chenyee guoxt modify for CSW1705A-2570 begin
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"refreshDataNoti");
                refreshDataNoti();
        }
    };
    //Chenyee guoxt modify for CSW1705A-2570 end


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.trafficassistant_settings);
        // Gionee: mengdw <2015-10-20> add for CR01571760 begin
        mArrayAdapter = getResources().getStringArray(R.array.notification_softap_setting_array);
        mArrayProvinceAdapter = getResources().getStringArray(R.array.calibrate_province_setting_array);
        mArrayBrandAdapter = getResources().getStringArray(R.array.calibrate_setting_brand_array);
        mTrafficSettingControler = TrafficSettingControler.getInstance(mContext);
        loadSettings(mContext);
        /*guoxt modify begin*/
        //calibrateSettingShow();

        // Gionee: mengdw <2015-10-20> add for CR01571760 end
        // Gionee: mengdw <2016-12-06> add for CR01775579 begin
        // mLockScreenRemindSettingAdapter = getResources().getStringArray(R.array.lockscreen_traffic_remind_limit_show_array);
        //lockScreenTrafficReindSettingInit();
        /*guoxt modify end*/
        // Gionee: mengdw <2016-12-06> add for CR01775579 end
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    //Chenyee guoxt modify for CSW1705A-2570 begin
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"unregisterReceiver");
        mContext.unregisterReceiver(mIntentReceiver);
    }
    //Chenyee guoxt modify for CSW1705A-2570 end


    @Override
    public void onResume() {
        super.onResume();
        //Chenyee guoxt modify for CSW1705A-2570 begin
        IntentFilter filter = new IntentFilter();
        filter.addAction("chenyee.intent.action.turnoff.radiobutton");
         mContext.registerReceiver(mIntentReceiver, filter);
        //Chenyee guoxt modify for CSW1705A-2570 end
        updateHotSpotNotificationSummary();
        updateCalibrateSettingSummay();
        // Gionee: mengdw <2016-01-12> add for CR01617372 end
        // Gionee: mengdw <2016-04-11> add for CR01672645 begin
        /*refreshNetwrokSpeed();*/
        // Gionee: mengdw <2016-04-11> add for CR01672645 end
        //Chenyee guoxt modify for CSW1705A-2570 begin
        refreshDataNoti();
        //Chenyee guoxt modify for CSW1705A-2570 end
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SIMInfoWrapper.setEmptyObject(mContext);
    }

    // Gionee: mengdw <2016-04-11> add for CR01672645 begin
    private void refreshNetwrokSpeed() {
        final ContentResolver contentResolver = mContext.getContentResolver();
        int speedSwitch = CyeeSettings.getInt(contentResolver, NETWORK_SPEED, 0);
        mNetworkSpeed.setChecked(speedSwitch != 0);
    }

    //Chenyee guoxt modify for CSW1705A-2570 begin
    private void refreshDataNoti() {
//        CyeeSwitchPreference notificationInfo = (CyeeSwitchPreference) findPreference(NOTIFICATION_INFO);
//        notificationInfo.setChecked(mTrafficSettingControler.isNotificationInfoSwtichOpen(mContext));
    }
    //Chenyee guoxt modify for CSW1705A-2570 end

    // Gionee: mengdw <2016-04-11> add for CR01672645 end
    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    private void lockScreenTrafficReindSettingInit() {
        mTrafficLockScreenRemindPreference = (CyeePreference) findPreference(KEY_TRAFFIC_LOCK_SCREEN_REMIND_SETTING);
        updateLockScreenRemindSummary();
        mTrafficLockScreenRemindPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {
                CyeeAlertDialog.Builder singleChoiceDialog = new CyeeAlertDialog.Builder(mContext);
                singleChoiceDialog.setTitle(R.string.lockscreen_traffic_remid_title);
                int selectIndex = mTrafficSettingControler.getLockScreenRemindSetting();
                singleChoiceDialog.setSingleChoiceItems(mLockScreenRemindSettingAdapter, selectIndex,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTrafficSettingControler.saveLockScreenRemindSetting(which);
                                Log.d(TAG, "mengdw-onClick which=" + which);
                                updateLockScreenRemindSummary();
                                dialog.dismiss();
                            }
                        });
                singleChoiceDialog.setNegativeButton(R.string.noti_softap_setting_cancel,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        });
                singleChoiceDialog.show();
                return true;
            }
        });
    }

    private void updateLockScreenRemindSummary() {
        int selectIndex = mTrafficSettingControler.getLockScreenRemindSetting();
        String selectLimitStr = mLockScreenRemindSettingAdapter[selectIndex];
        String summryMessage = "";
        if (mTrafficSettingControler.isLockScreenRemindClose()) {
            summryMessage = selectLimitStr;
        } else {
            summryMessage = String.format(
                    mContext.getResources().getString(R.string.lockscreen_traffic_remid_txt), selectLimitStr);
        }
        Log.d(TAG, "mengdw-updateLockScreenRemindSummary selectIndex=" + selectIndex +
                " selectLimitStr=" + selectLimitStr + " summryMessage=" + summryMessage);
        mTrafficLockScreenRemindPreference.setSummary(summryMessage);
    }
    // Gionee: mengdw <2016-12-06> add for CR01775579 end

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            mUserInput.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
        }
    }

    private void updateHotSpotNotificationSummary() {
        int hotSportRemindIndex = mTrafficSettingControler.getHotSportRemidSettedIndex(mContext);
        int hotSportRemindValue = mTrafficSettingControler.getHotSportRemindSettedValue(mContext);
        if (mSoftapNotification != null) {
            String summryMessage = "";
            if (hotSportRemindIndex != TrafficSettingControler.HOTSPORT_REMIND_CLOSE) {
                //fengpeipei modify for 62338 start
                summryMessage = String.format(
                        mContext.getResources().getString(R.string.noti_softap_setting_text),
                        hotSportRemindValue + "M");
                //fengpeipei modify for 62338 end
            } else {
                summryMessage = mContext.getResources().getString(R.string.noti_softap_no_warning_text);
            }
            mSoftapNotification.setSummary(summryMessage);
        }
    }

    private void createUserDefineDialog() {
        LayoutInflater inputLayoutInflater = LayoutInflater.from(mContext);
        final View inputView = inputLayoutInflater.inflate(R.layout.hotsport_user_define_dialog, null);
        mUserInput = (CyeeEditText) inputView.findViewById(R.id.user_define_input);
        mUserInput.requestFocus();
        chameleonColorProcess();
        mHotSpotSetDialogBuiler = new AlertDialog.Builder(mContext);
        mHotSpotSetDialogBuiler.setTitle(R.string.noti_softap_setting_userDefine);
        mHotSpotSetDialogBuiler.setView(inputView);
        mHotSpotSetDialogBuiler.setNegativeButton(R.string.noti_softap_setting_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        mHotSpotSetDialogBuiler.setPositiveButton(R.string.noti_softap_setting_commit, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                String userDefine = mUserInput.getText().toString();
                Log.d(TAG, "UserDefineDialog Positive click userDefine=" + userDefine);
                try {
                    int hotSportRemindValue = Integer.parseInt(userDefine);
                    processUserDefineHotspotSetting(hotSportRemindValue);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "HotSportTraffic Set Exception");
                }
                dialog.dismiss();
            }
        });
        AlertDialog hotSpotSetDialog = mHotSpotSetDialogBuiler.create();
        hotSpotSetDialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                // Chenyee <liu_shuang> <2018-04-23> modify for CSW1703A-1288 begin
                // Chenyee <liu_shuang> <2017-12-26> modify for CSW1702A-931 begin
                /*if (!Consts.cyGoFlag) {
                    InputMethodManager imm = (InputMethodManager) mContext
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mUserInput, InputMethodManager.SHOW_IMPLICIT);
                } else {*/
                    mUserInput.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) mContext
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(mUserInput, InputMethodManager.SHOW_IMPLICIT);
                        }
                    },20);
                /*}*/
                // Chenyee <liu_shuang> <2017-12-26> modify for CSW1702A-931 end
                // Chenyee <liu_shuang> <2018-04-23> modify for CSW1703A-1288 end
            }
        });
        hotSpotSetDialog.show();
        mHotspotPositeBtn = hotSpotSetDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mHotspotPositeBtn.setEnabled(false);
        mUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String changeText = s.toString();
                mHotspotPositeBtn.setEnabled(changeText.trim().length() != 0 && !changeText.equals(""));
            }
        });
    }
    // Gionee: mengdw <2015-10-20> add for CR01571760 end

    // mengdw <2016-10-09> add for CR01766193 begin
    private void processUserDefineHotspotSetting(int settingValue) {
        if (SETTING_CLOSE_VALUE == settingValue) {
            SellUtils.showToast(mContext, mContext.getResources().getString(R.string.hotspot_userdefine_settingvalue_invalid));
            return;
        }
        processHotspotSettingChange(USER_DEFINE_SETTING_INDEX, settingValue);
    }
    // mengdw <2016-10-09> add for CR01766193 end

    // Gionee: mengdw <2016-01-12> add for CR01617372 begin
    private void setSim1Summary(int simIndex) {
        if (mTrafficSettingControler.isCalibrateProvinceSetted(simIndex)) {
            int province = mTrafficSettingControler.getSettedCalibrateProvince(simIndex);
            mCalibrateSettingProvinceSim1.setSummary(mArrayProvinceAdapter[province]);
        }
        if (mTrafficSettingControler.isCalibrateBrandSetted(simIndex)) {
            int brand = mTrafficSettingControler.getSettedCalibrateBrand(simIndex);
            mCalibrateSettingBrandSim1.setSummary(mArrayBrandAdapter[brand]);
        }
    }

    private void setSim2Summary(int simIndex) {
        if (mTrafficSettingControler.isCalibrateProvinceSetted(simIndex)) {
            int province = mTrafficSettingControler.getSettedCalibrateProvince(simIndex);
            mCalibrateSettingProvinceSim2.setSummary(mArrayProvinceAdapter[province]);
        }
        if (mTrafficSettingControler.isCalibrateBrandSetted(simIndex)) {
            int brand = mTrafficSettingControler.getSettedCalibrateBrand(simIndex);
            mCalibrateSettingBrandSim2.setSummary(mArrayBrandAdapter[brand]);
        }
    }

    private void updateCalibrateSettingSummay() {
        // Gionee: mengdw <2016-01-29> modify for CR01631609 begin
        int count = TrafficassistantUtil.getSimCount(mContext);
        switch (count) {
            case 0:
                break;
            case 1:
                int slotID = TrafficassistantUtil.getSingleCardSlotID(mContext);
                setSim1Summary(slotID);
                break;
            case 2:
                setSim1Summary(0);
                setSim2Summary(1);
                break;
            default:
                break;
        }
        //Gionee: mengdw <2016-01-29> modify for CR01631609 end
    }

    private void calibrateSettingShow() {
        int count = TrafficassistantUtil.getSimCount(mContext);
        Log.d(TAG, "calibrateSettingShow count=" + count);
        switch (count) {
            case 0:
                getPreferenceScreen().removePreference(mCalibrateSettingCategorySim1);
                getPreferenceScreen().removePreference(mCalibrateSettingCategorySim2);
                break;
            case 1:
                getPreferenceScreen().addPreference(mCalibrateSettingCategorySim1);
                getPreferenceScreen().removePreference(mCalibrateSettingCategorySim2);
                break;
            case 2:
                getPreferenceScreen().addPreference(mCalibrateSettingCategorySim1);
                getPreferenceScreen().addPreference(mCalibrateSettingCategorySim2);
                String sim1Title = String.format("%s %s",
                        mContext.getResources().getString(R.string.traffic_simcard_focus1), mContext
                                .getResources().getString(R.string.calibrate_setting_txt));
                String sim2Title = String.format("%s %s",
                        mContext.getResources().getString(R.string.traffic_simcard_focus2), mContext
                                .getResources().getString(R.string.calibrate_setting_txt));
                mCalibrateSettingCategorySim1.setTitle(sim1Title);
                mCalibrateSettingCategorySim2.setTitle(sim2Title);
                break;
            default:
                break;
        }
    }

    // Gionee: mengdw <2016-01-29> modify for CR01631609 begin
    private void resetOpSimIndex() {
        if (TrafficassistantUtil.getSimCount(mContext) == 1) {
            mOpSimIndex = TrafficassistantUtil.getSingleCardSlotID(mContext);
        }
    }
    //Gionee: mengdw <2016-01-29> modify for CR01631609 end

    private void createProvinceSettingDialag() {
        CyeeAlertDialog.Builder provinceChoiceDialog = new CyeeAlertDialog.Builder(mContext);
        int settingSelectIndex = 0;
        // Gionee: mengdw <2016-01-29> modify for CR01631609 begin
        resetOpSimIndex();
        // Gionee: mengdw <2016-01-29> modify for CR01631609 end
        if (mTrafficSettingControler.isCalibrateProvinceSetted(mOpSimIndex)) {
            settingSelectIndex = mTrafficSettingControler.getSettedCalibrateProvince(mOpSimIndex);
        }
        provinceChoiceDialog.setTitle(R.string.calibrate_setting_province_choice);
        provinceChoiceDialog.setSingleChoiceItems(mArrayProvinceAdapter, settingSelectIndex,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "provinceChoiceDialog onClick which=" + which);
                        mTrafficSettingControler.setCalibrateProvince(mOpSimIndex, which);
                        updateCalibrateSettingSummay();
                        dialog.dismiss();
                    }
                });
        provinceChoiceDialog.setNegativeButton(R.string.noti_softap_setting_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        provinceChoiceDialog.show();
    }

    private void createBrandSettingDialag() {
        Log.d(TAG, "createBrandSettingDialag simIndex = " + mOpSimIndex);
        CyeeAlertDialog.Builder brandChoiceDialog = new CyeeAlertDialog.Builder(mContext);
        int settingSelectIndex = 0;
        // Gionee: mengdw <2016-01-29> modify for CR01631609 begin
        resetOpSimIndex();
        // Gionee: mengdw <2016-01-29> modify for CR01631609 end
        if (mTrafficSettingControler.isCalibrateBrandSetted(mOpSimIndex)) {
            settingSelectIndex = mTrafficSettingControler.getSettedCalibrateBrand(mOpSimIndex);
        }
        brandChoiceDialog.setTitle(R.string.calibrate_setting_brand_choice);
        brandChoiceDialog.setSingleChoiceItems(mArrayBrandAdapter, settingSelectIndex, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "brandChoiceDialog onClick which=" + which);
                mTrafficSettingControler.setCalibrateBrand(mOpSimIndex, which);
                updateCalibrateSettingSummay();
                dialog.dismiss();
            }
        });
        brandChoiceDialog.setNegativeButton(R.string.noti_softap_setting_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        brandChoiceDialog.show();
    }

    private void calibrateSettingsInit() {
        mCalibrateSettingCategorySim1 = (CyeePreferenceCategory) findPreference(KEY_CALIBRATE_SETTING_SIM1);
        mCalibrateSettingCategorySim2 = (CyeePreferenceCategory) findPreference(KEY_CALIBRATE_SETTING_SIM2);
        mCalibrateSettingProvinceSim1 = (CyeePreference) findPreference(KEY_CALIBRATE_SETTING_PROVINCE_SIM1);
        mCalibrateSettingProvinceSim1.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {
                Log.d(TAG, "ProvinceSim1 onPreferenceClick");
                mOpSimIndex = 0;
                createProvinceSettingDialag();
                return true;
            }
        });
        mCalibrateSettingProvinceSim2 = (CyeePreference) findPreference(KEY_CALIBRATE_SETTING_PROVINCE_SIM2);
        mCalibrateSettingProvinceSim2.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {
                Log.d(TAG, "ProvinceSim2 onPreferenceClick");
                mOpSimIndex = 1;
                createProvinceSettingDialag();
                return true;
            }
        });
        mCalibrateSettingBrandSim1 = (CyeePreference) findPreference(KEY_CALIBRATE_SETTING_BRAND_SIM1);
        mCalibrateSettingBrandSim1.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {
                Log.d(TAG, "mCalibrateSettingBrandSim1 onPreferenceClick");
                mOpSimIndex = 0;
                createBrandSettingDialag();
                return true;
            }
        });
        mCalibrateSettingBrandSim2 = (CyeePreference) findPreference(KEY_CALIBRATE_SETTING_BRAND_SIM2);
        mCalibrateSettingBrandSim2.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {
                Log.d(TAG, "mCalibrateSettingBrandSim2 onPreferenceClick");
                mOpSimIndex = 1;
                createBrandSettingDialag();
                return true;
            }
        });
    }

    // Gionee: mengdw <2016-01-12> add for CR01617372 end
    private void loadSettings(final Context context) {
        // Gionee: mengdw <2016-01-12> add for CR01617372 begin
        //guoxt modify begin
        //calibrateSettingsInit();
        //guoxt modify end
        // Gionee: mengdw <2016-01-12> add for CR01617372 end
        /*mNetworkSpeed = (CyeeSwitchPreference) findPreference(NETWORK_SPEED);
        final ContentResolver contentResolver = context.getContentResolver();
        int speedSwitch = CyeeSettings.getInt(contentResolver, NETWORK_SPEED, 0);

        //Gionee guoxt 2015-07-15 modify for CR01591008 begin
        if (!sGnDisableCyeeSystemUI) {
            mNetworkSpeed.setChecked(speedSwitch != 0);

            mNetworkSpeed.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(CyeePreference arg0, Object arg1) {
                    boolean checked = ((Boolean) arg1).booleanValue();
                    CyeeSettings.putInt(contentResolver, NETWORK_SPEED, checked ? 1 : 0);
                    return true;
                }
            });
        }*/
        // Gionee xionghg <2017-07-27> modify for 174677 begin
        // Gionee: mengdw <2017-01-19> add for 65945 begin
        // CyeePreferenceCategory notificatonCategory = (CyeePreferenceCategory) findPreference(KEY_NOTIFICATION_CATEGORY);
        // notificatonCategory.removePreference(mNetworkSpeed)

        // Chenyee <liu_shuang> <2018-03-01> modify for CSW1703A-304 begin
        //Gionee yubo 2015-07-15 modify for CR01521516 begin
        /*if (sGnDisableCyeeSystemUI && mNetworkSpeed != null || Consts.isNotchSupport) {
            ((CyeePreferenceGroup) findPreference(KEY_NOTIFICATION_CATEGORY)).removePreference(mNetworkSpeed);
        }*/
        //Gionee yubo 2015-07-15 modify for CR01521516 end
        // Chenyee <liu_shuang> <2018-03-01> modify for CSW1703A-304 end
        // Gionee: mengdw <2017-01-19> add for 65945 end
        // Gionee xionghg <2017-07-27> modify for 174677 end

//        CyeeSwitchPreference notificationInfo = (CyeeSwitchPreference) findPreference(NOTIFICATION_INFO);
//        notificationInfo.setChecked(mTrafficSettingControler.isNotificationInfoSwtichOpen(mContext));
        mTrafficSettingControler.commitTrafficNotiAction(mContext);
//        notificationInfo.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//
//            @Override
//            public boolean onPreferenceChange(CyeePreference arg0, Object arg1) {
//                boolean checked = ((Boolean) arg1).booleanValue();
//                mTrafficSettingControler.setNotificationInfoSwtich(mContext, checked);
//                mTrafficSettingControler.commitNotificationController(checked);
//                // Chenyee xionghg 20180103 delete for CSW1702A-742 duplicate code begin
//                // mTrafficSettingControler.commitTrafficNotiAction(mContext);
//                // Chenyee xionghg 20180103 delete for CSW1702A-742 duplicate code end
//                dataSync(mContext);
//                return true;
//            }
//        });
        // Gionee: mengdw <2015-10-20> modify for CR01571760 begin
        // Gionee: jiangsj <2017-05-16> modify for 141074 begin
        if (Consts.gnKRFlag) {
            removeHotSpotPreference();
        } else {
            hotSpotSettingInit();
        }
        // Gionee: jiangsj <2017-05-16> modify for 141074 end
        // Gionee: mengdw <2015-10-20> modify for CR01571760 end
    }

    // Gionee: jiangsj <2017-05-16> add for 141074 begin
    private void removeHotSpotPreference() {
        mNotiSoftapCategory = (CyeePreferenceCategory) findPreference(KEY_NOTI_SOFTAP_CATEGORY);
        //Chenyee bianrong <2018-2-6> modify for SW17W16KR-104 begin
        if(mNotiSoftapCategory != null) {
            getPreferenceScreen().removePreference(mNotiSoftapCategory);
        }
        //Chenyee bianrong <2018-2-6> modify for SW17W16KR-104 end
    }
    // Gionee: jiangsj <2017-05-16> add for 141074 end

    private void hotSpotSettingInit() {
        // Gionee: mengdw <2015-10-20> add for CR01571760 begin
        mSoftapNotification = (CyeePreference) findPreference(NOTIFI_SOFTAP_DATA_TRAFFIC_KEY);
        mSettingSelectIndex = mTrafficSettingControler.getHotSportRemidSettedIndex(mContext);
        mSoftapNotification.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {
                AlertDialog.Builder singleChoiceDialog = new AlertDialog.Builder(mContext);
                singleChoiceDialog.setTitle(R.string.noti_softap_setting_title);
                singleChoiceDialog.setSingleChoiceItems(mArrayAdapter, mSettingSelectIndex,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Chenyee liu_shuang 2017-12-04 modify for SW17W16A-2064 begin
                                // mSettingSelectIndex = which;
                                //Chenyee liu_shuang 2017-12-04 modify for SW17W16A-2064 end
                                processHotspotSettingIntemSelected(which);
                                dialog.dismiss();
                            }
                        });
                singleChoiceDialog.setNegativeButton(R.string.noti_softap_setting_cancel,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        });
                singleChoiceDialog.show();
                return true;
            }
        });
        // Gionee: mengdw <2015-10-20> add for CR01571760 end
    }

    // mengdw <2016-10-09> add for CR01766193 begin
    private void processHotspotSettingIntemSelected(int settingIndex) {
        switch (settingIndex) {
            case 0:
                saveSettingChange(SETTING_CLOSE_INDEX, SETTING_CLOSE_VALUE);
                break;
            case 1:
                processHotspotSettingChange(1, 10);
                break;
            case 2:
                processHotspotSettingChange(2, 50);
                break;
            case 3:
                processHotspotSettingChange(3, 100);
                break;
            case 4:
                createUserDefineDialog();
                break;
            default:
                Log.d(TAG, "processHotspotSettingIntemSelected error choice");
                break;
        }
    }

    private void saveSettingChange(int settingIndex, int settingValue) {
        //Chenyee liu_shuang 2017-12-04 add for SW17W16A-2064 begin
        Log.d(TAG, "saveSettingChange: index = " + settingIndex + ", value = " + settingValue);
        mSettingSelectIndex = settingIndex;
        //Chenyee liu_shuang 2017-12-04 add for SW17W16A-2064 end
        mTrafficSettingControler.setHotSportRemidSettedIndex(mContext, settingIndex);
        mTrafficSettingControler.setHotSportRemindSettedValue(mContext, settingValue);
        updateHotSpotNotificationSummary();
        mTrafficSettingControler.restartSettingValue(settingValue);
    }

    private void processHotspotSettingChange(int settingIndex, int settingValue) {
        boolean isHotspotOpen = TrafficassistantUtil.isSoftApOpen(mContext);
        if (isHotspotOpen) {
            if (mTrafficSettingControler.isSettingValueLargeCurrentTraffic(mContext, settingValue)) {
                saveSettingChange(settingIndex, settingValue);
                String validMessage = String.format(
                        mContext.getResources().getString(R.string.hotspot_settingvalue_valid), settingValue);
                SellUtils.showToast(mContext, validMessage);
            } else {
                float curTraffic = mTrafficSettingControler.getCurrentTraffic();
                String invalidMessage = String.format(
                        mContext.getResources().getString(R.string.hotspot_settingvalue_invalid), curTraffic);
                SellUtils.showToast(mContext, invalidMessage);
            }
        } else {
            saveSettingChange(settingIndex, settingValue);
        }
    }
    // mengdw <2016-10-09> add for CR01766193 end

    private void dataSync(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                TrafficProcessorService.processIntent(context, false);
                TrafficProcessorService.processIntent(context, true);
            }
        }).start();
    }
}
