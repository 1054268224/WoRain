package com.cydroid.softmanager.powersaver.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.powersaver.interfaces.IPowerService;
import com.cydroid.softmanager.powersaver.interfaces.ISelectedDataChangedListener;
import com.cydroid.softmanager.powersaver.mode.ModeItemInfo;
import com.cydroid.softmanager.powersaver.service.PowerManagerService;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerTimer;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeEditModeView;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeTextView;

public class NormalModeDetailsActivity extends CyeeActivity implements ISelectedDataChangedListener {
    private static final String TAG = "NormalModeDetailsActivity";

    private Context mContext;
    // Gionee <yangxinruo> <2016-3-18> delete for CR01654969 begin
    // private DailyPowerInfo mDPInfo;
    // Gionee <yangxinruo> <2016-3-18> delete for CR01654969 end
    // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
    private PowerModeItemAdapter mModeItemAdapter;
    // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end
    private CyeeEditModeView mEditModeView;
    private CyeeTextView mHeaderText;
    private CyeeListView mListView;
    private PowerTimer mPowerTimer;
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    private List<ModeItemInfo> mConfigList;
    private CyeeButton mResetBtn;
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    private boolean mIsbound = false;
    private IPowerService mService = null;
    private HashMap<String, Integer> mDisplayOrderMap = new HashMap<String, Integer>();

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IPowerService.Stub.asInterface((IBinder) service);
            Log.d(TAG, "PowerManagerService connected,init time");
            mIsbound = true;
            try {
                List<ModeItemInfo> allConfigList = mService.getConfigList(PowerConsts.NORMAL_MODE);
                for (ModeItemInfo config : allConfigList) {
                    Log.d(TAG, "CONFIG:" + config.candidateVals.toString() + " "
                            + config.candidateValDecs.toString() + " " + config.configVal + " "
                            + config.defaultVal + " " + config.name);
                    /*guoxt modify for CSW1705A-2869  begin */
                    if(config.name.equals("PowerSaver")){
                        continue;
                    }
                    /*guoxt modify for  CSW1705A-2869 end */

                    if (getDisplayOrder(config.name) >= 0 && config.candidateVals.size() > 1) {
                        mConfigList.add(config);
                        Collections.sort(mConfigList, new Comparator<ModeItemInfo>() {

                            @Override
                            public int compare(ModeItemInfo lhs, ModeItemInfo rhs) {
                                if (getDisplayOrder(rhs.name) == getDisplayOrder(lhs.name)) {
                                    return 0;
                                }
                                if (getDisplayOrder(rhs.name) > getDisplayOrder(lhs.name)) {
                                    return -1;
                                } else {
                                    return 1;
                                }
                            }

                        });
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "call remote getConfigList error " + e);
            }
            initAdapter();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.d(TAG, "PowerManagerService disconnected");
        }
    };

    private int getDisplayOrder(String itemName) {
        Integer res = mDisplayOrderMap.get(itemName);
        if (res == null) {
            return -1;
        }
        return res;
    }

    @Override
    protected void onDestroy() {
        ChameleonColorManager.getInstance().onDestroy(this);
        if (mIsbound) {
            try {
                mContext.unbindService(mConnection);
                mIsbound = false;
            } catch (Exception e) {
                Log.d(TAG, "unbindService service error " + e);
            }
        }
        super.onDestroy();
    }

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
    // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UiUtils.isSpecialStyleModel()) {
            setTheme(R.style.SystemManagerTheme);
        } else {
            setTheme(R.style.SystemManagerThemeCustom);
        }
        super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
        setContentView(R.layout.powermanager_daily_activity);
        mContext = this;
        mConfigList = new ArrayList<ModeItemInfo>();
        init();
        mDisplayOrderMap = getDisplayOrderMapFromRes();
        mContext.bindService(new Intent(mContext, PowerManagerService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }
    // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end

    private HashMap<String, Integer> getDisplayOrderMapFromRes() {
        HashMap<String, Integer> res = new HashMap<String, Integer>();
        String[] orderArray = mContext.getResources().getStringArray(R.array.powermode_normal_display_order);

        for (int i = 0; i < orderArray.length; i++) {
            res.put(orderArray[i], i);
        }
        return res;
    }

    private void init() {
        initUI();
        initActionBar();
        initPowerTimer();
    }

    private void initUI() {
        mEditModeView = (CyeeEditModeView) findViewById(R.id.dailypower_editmodeview);
        mEditModeView.setEditModeBtnTxt(mContext.getResources().getString(R.string.action_cancel),
                mContext.getResources().getString(R.string.action_save));
        setEditModeBtnClickListener();

        mHeaderText = (CyeeTextView) findViewById(R.id.dailypower_header_text);
        mListView = (CyeeListView) findViewById(R.id.dailypower_list);
        // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
        mResetBtn = (CyeeButton) findViewById(R.id.btn_reset);
        // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
        // Gionee <yangxinruo> <2016-3-18> delete for CR01654969 begin
        // mDPInfo = new DailyPowerInfo(mContext);
        // Gionee <yangxinruo> <2016-3-18> delete for CR01654969 end
    }

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    private void initAdapter() {
        mModeItemAdapter = new PowerModeItemAdapter(mContext, this, mConfigList);
        mListView.setAdapter(mModeItemAdapter);

        mResetBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mModeItemAdapter.resetConfig();
            }

        });
        getTimeInNormalMode();
    }
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end

    private void initActionBar() {
        getCyeeActionBar().hide();
    }

    private void initPowerTimer() {
        mPowerTimer = new PowerTimer(mContext);
    }

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    private void getTimeInNormalMode() {
        mHeaderText.setText(mPowerTimer.getTimeStrInNormalMode(
                mContext.getResources().getString(R.string.time_use_in_configurable_mode), mService));
    }
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end

    private void setEditModeBtnClickListener() {
        mEditModeView.setEditModeBtnClickListener(new CyeeEditModeView.EditModeClickListener() {
            @Override
            public void rightBtnClick() {
                // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
                List<ModeItemInfo> changedConfigList = mModeItemAdapter.getChangedConfiged();
                if (isDisplayedConfigChanged()) {
                    resetNormalAction(changedConfigList);
                }
                finish();
                // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end
            }

            @Override
            public void leftBtnClick() {
                finish();
            }
        });
    }

    protected boolean isDisplayedConfigChanged() {
        return mModeItemAdapter.getChangedConfiged().size() > 0;
    }

    // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
    private void resetNormalAction(List<ModeItemInfo> changedConfigList) {
        Log.d(TAG, "config changed by user,save it");
        try {
            mService.setConfigList(PowerConsts.NORMAL_MODE, changedConfigList);
        } catch (Exception e) {
            Log.d(TAG, "call remote service error,can not save config:" + e);
        }
        /*
        SharedPreferences mHelper = context.getSharedPreferences(PowerManagerSettingsFragment.PREFERENCE_NAME,
                Context.MODE_MULTI_PROCESS);
        mHelper.edit().putInt(Consts.DAILY_POWER_ELEMENTS, mDailyAdapter.getElementValues()).commit();
        
        Intent intent = new Intent(context, PowerManagerService.class);
        intent.setAction(PowerConsts.MODE_UPDATE_WHEN_SAVECONFIG);
        context.startService(intent);
        */
    }

    public void onDataChanged(boolean[] item) {
        getTimeInNormalMode();
    }
    // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end

    @Override
    public void onBackPressed() {
        if (!isDisplayedConfigChanged()) {
            super.onBackPressed();
            return;
        }
        showChangedConfirmDialog();
    }

    private void showChangedConfirmDialog() {
        CyeeAlertDialog confirmDialog = new CyeeAlertDialog.Builder(mContext)

                .setMessage(R.string.normal_detail_changed_alert_message)
                .setTitle(R.string.normal_detail_changed_alert_title)
                .setPositiveButton(R.string.normal_detail_changed_alert_save,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<ModeItemInfo> changedConfigList = mModeItemAdapter.getChangedConfiged();
                                resetNormalAction(changedConfigList);
                                NormalModeDetailsActivity.this.finish();
                            }
                        })
                .setNegativeButton(R.string.normal_detail_changed_alert_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NormalModeDetailsActivity.this.finish();
                            }
                        })
                .create();
        confirmDialog.show();
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
