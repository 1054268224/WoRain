//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeEditText;
import cyee.widget.CyeeNumberPicker;
import cyee.widget.CyeeSwitch;

public class TrafficLimitActivity extends CyeeActivity implements OnClickListener {
    private static final String TAG = "TrafficLimitActivity";

    private Context mContext;
    private CyeeEditText mTotalFlow;
    private TextView mDate;
    private TextView mPercentValue;
    private TextView mWarningValue;
    private TextView mUsedFlow;
    private SeekBar mSeekBar;
    private LinearLayout mLinearLayoutSet;
    private CyeeSwitch mSwitchButton;

    private int mSimIndex = 0;
    private int mCycleDay = 1;
    private final int mProgressStart = 80;
    private boolean mFromExam = false;

    private TrafficCalibrateControler mTrafficCalibrateControler;

    private final CompoundButton.OnCheckedChangeListener mSwitchChangeListener = new SwitchChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TrafficTheme);
        super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
        setContentView(R.layout.trafficassistant_limit_flow_settings);

        mContext = this;
        mTrafficCalibrateControler = TrafficCalibrateControler.getInstance(mContext);

        mTotalFlow = (CyeeEditText) this.findViewById(R.id.gn_total_flow);
        mDate = (TextView) this.findViewById(R.id.date_start_edit);
        mPercentValue = (TextView) this.findViewById(R.id.gn_percent_value);
        mWarningValue = (TextView) this.findViewById(R.id.gn_warning_value);
        mUsedFlow = (TextView) this.findViewById(R.id.gn_used_flow);
        mSeekBar = (SeekBar) this.findViewById(R.id.seekBar);
        mLinearLayoutSet = (LinearLayout)this.findViewById(R.id.limit_set);
        mSwitchButton = (CyeeSwitch)this.findViewById(R.id.traffic_switch);

        mTotalFlow.addTextChangedListener(mTextWatcher);
        mUsedFlow.addTextChangedListener(mTextWatcher);
        mSeekBar.setMax(20);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        LinearLayout dateLayout = (LinearLayout) findViewById(R.id.gn_date_layout);
        dateLayout.setOnClickListener(this);
        mSwitchButton.setOnCheckedChangeListener(mSwitchChangeListener);

        initActionBar();
        initData(this.getIntent().getExtras());
        setEditModeBtnClickListener();
        chameleonColorProcess();
    }

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            /*guoxt modify for CR01514844 begin */
            mTotalFlow.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            mTotalFlow.setBackgroundColor(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3());
            mUsedFlow.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            mUsedFlow.setBackgroundColor(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3());
            mSeekBar.setBackgroundColor(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3());
            /*guoxt modify for CR01514844 end */
        }
    }

    private void initActionBar() {
        CyeeActionBar actionBar = getCyeeActionBar();
        actionBar.setDisplayOptions(CyeeActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.trafficassistant_limit_activity_actionbar);
        View view = actionBar.getCustomView();
        View saveMenuItem = view.findViewById(R.id.save_menu_item);
        //((cyee.widget.CyeeButton) saveMenuItem).setTextColor(Color.WHITE);
        View discardMenuItem = view.findViewById(R.id.discard_menu_item);
        //((cyee.widget.CyeeButton) discardMenuItem).setTextColor(Color.WHITE);
        // Gionee: mengdw <2016-11-15> modify for CR01773139 begin
        if (ChameleonColorManager.isNeedChangeColor()) {
            // Gionee: jiangsj 2017.05.20 modify for 143611 begin
            ((cyee.widget.CyeeButton) saveMenuItem).setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            ((cyee.widget.CyeeButton) discardMenuItem).setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            // Gionee: jiangsj 2017.05.20 modify for 143611 end
        }
        // Gionee: mengdw <2016-11-15> modify for CR01773139 end
        saveMenuItem.setOnClickListener(this);
        discardMenuItem.setOnClickListener(this);
    }

    private void initData(Bundle bundle) {
        // Gionee: mengdw <2015-08-25> modify for CR01543192 begin
        if (bundle != null) {
            // Gionee: mengdw <2015-10-15> modify for CR01568633 begin
            mSimIndex = bundle.getInt(Constant.SIM_VALUE, 0);
            mFromExam = bundle.getBoolean("fromExam", false);
        }
        // Gionee: mengdw <2015-08-25> modify for CR01543192 end
        int totalFlow = mTrafficCalibrateControler.getCommonTotalTaffic(mContext, mSimIndex);
        int percent = mTrafficCalibrateControler.getWarnPercent(mContext, mSimIndex);
        float used = mTrafficCalibrateControler.getCommonUsedTaffic(mContext, mSimIndex);
        mSwitchButton.setChecked(mTrafficCalibrateControler.getCommonTafficMonitor(mContext, mSimIndex));
        if (totalFlow > 0) {
            String strFlow = totalFlow + "";
            mTotalFlow.setText(strFlow);
            // Gionee: mengdw <2016-07-01> modify for CR01725200 begin
            try {
                mTotalFlow.setSelection(strFlow.length());
            } catch (Exception e) {
                Log.d(TAG, "initData setSelection Exception e=" + e.toString());
            }
            // Gionee: mengdw <2016-07-01> modify for CR01725200 end
        } else {
            mTotalFlow.setText("");
        }
        mPercentValue.setText(percent + "");
        mUsedFlow.setText(used + "");
        // Gionee: mengdw <2015-11-11> modify for CR01589343 begin
        try {
            mCycleDay = mTrafficCalibrateControler.getStartDate(mContext, mSimIndex);
            updateDateText();
            if (!mTotalFlow.getText().toString().isEmpty()) {
                mWarningValue.setText(String.valueOf(((1.0 * Integer
                        .parseInt(mTotalFlow.getText().toString()) * Integer.parseInt(mPercentValue.getText()
                        .toString())) / 100)));
            } else {
                mWarningValue.setText("0");
            }
            mSeekBar.setProgress(Integer.parseInt(mPercentValue.getText().toString()) - mProgressStart);
        } catch (Exception e) {
            Log.d(TAG, "initData NumberFormatException e=" + e.toString());
        }
        // Gionee: mengdw <2015-11-11> modify for CR01589343 end
    }

    private void onStartFlowMonitor(final Context context) {
        Log.d(TAG, "onStartFlowMonitor isActivated=" + TrafficassistantUtil.isActivated(context, mSimIndex));
        if (TrafficassistantUtil.isActivated(context, mSimIndex)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    TrafficProcessorService.processIntent(context, false);
                    TrafficProcessorService.processIntent(context, true);
                }
            }).start();
        }
        onBackToParentActivity(context);
    }

    //guoxt modify for android P begin 
    private void onStopFlowMonitor(final Context context) {
        Log.d(TAG, "onStartFlowMonitor isActivated=" + TrafficassistantUtil.isActivated(context, mSimIndex));
        if (TrafficassistantUtil.isActivated(context, mSimIndex)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    TrafficProcessorService.processIntent(context, false);
                }
            }).start();
        }
        onBackToParentActivity(context);
    }
    //guoxt modify for android P end 

    private void onBackToParentActivity(Context context) {
        if (mFromExam){
            return;
        }
        Intent intent = new Intent(context, TrafficAssistantMainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.SIM_VALUE, mSimIndex);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void setEditModeBtnClickListener() {
        /*
         * mEditModeView.setEditModeBtnClickListener(new
         * CyeeEditModeView.EditModeClickListener() {
         * 
         * @Override public void rightBtnClick() {
         * saveTrafficSettings(mContext); }
         * 
         * @Override public void leftBtnClick() { finish(); } });
         */
    }

    @Override
    protected void onResume() {
        super.onResume();
        // YouJuAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //YouJuAgent.onPause(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gn_date_layout:
                selectCycleDay();
                break;

            case R.id.discard_menu_item:
                finish();
                break;

            case R.id.save_menu_item:
                saveTrafficSettings(mContext);
                break;
            default:
                break;
        }
    }


    private class SwitchChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if(isChecked){
                mLinearLayoutSet.setVisibility(View.VISIBLE);
            }else{
                mLinearLayoutSet.setVisibility(View.GONE);
            }
        }
    }

    private void saveTrafficSettings(Context context) {
        if (mTotalFlow.getText().toString().isEmpty() && mSwitchButton.isChecked()  ) {
		//guoxt modify for android P begin 
            Toast.makeText(context, getString(R.string.info_save_error_no_flow_input), Toast.LENGTH_SHORT)
                    .show();
           // cleanPreference(context);

            //finish();
		//guoxt modify for android P end 
        } else if (mUsedFlow.getText().toString().isEmpty()  && mSwitchButton.isChecked()) {
            Toast.makeText(context, getString(R.string.info_save_error_no_used_input), Toast.LENGTH_SHORT)
                    .show();
        } else  if(mTotalFlow.getText().toString().isEmpty() && !mSwitchButton.isChecked() ){
             cleanPreference(context);
            finish();
        }else{
            savePreference(context);
            if(mSwitchButton.isChecked()) {
                onStartFlowMonitor(context);
            }
            updateNotificationBar(context);
            finish();
        }
    }

    //guoxt modify for android P begin 
    // Gionee: mengdw <2015-11-11> add for CR01589343 begin
    private void cleanPreference(Context context) {
        mTrafficCalibrateControler.setTafficPackageSetted(context, mSimIndex, false);
        mTrafficCalibrateControler.setCommonTotalTaffic(context, mSimIndex, 0);
        mTrafficCalibrateControler.setCommonTafficMonitor(context, mSimIndex, mSwitchButton.isChecked());
        onStopFlowMonitor(context);
    }
	//guoxt modify for android P end 

    // Gionee: mengdw <2015-11-11> add for CR01589343 begin
    private void savePreference(Context context) {
        int[] times = TimeFormat.getNowTimeArray();
        try {
            int totalFlow = Integer.parseInt(mTotalFlow.getText().toString());
            int percent = Integer.parseInt(mPercentValue.getText().toString());
            float used = Float.parseFloat(mUsedFlow.getText().toString());
            String curDate = times[0] + "-" + times[1] + "-"
                    + times[2] + "-" + times[3] + "-"
                    + times[4] + "-" + times[5];
            float left = totalFlow - used;
            mTrafficCalibrateControler.setCommonTotalTaffic(context, mSimIndex, totalFlow);
            mTrafficCalibrateControler.setCommonTafficMonitor(context, mSimIndex, mSwitchButton.isChecked());
            mTrafficCalibrateControler.setCommonUsedTaffic(context, mSimIndex, used);
            mTrafficCalibrateControler.setCommonLeftTraffic(context, mSimIndex, left);
            mTrafficCalibrateControler.setWarnPercent(context, mSimIndex, percent);
            mTrafficCalibrateControler.setStartDate(context, mSimIndex, mCycleDay);
            mTrafficCalibrateControler.saveSettedCurrentDate(context, mSimIndex, curDate);
            float actualFlow = TrafficassistantUtil.getActualFlow(context, mSimIndex, mCycleDay);
            mTrafficCalibrateControler.setCalibratedActualFlow(context, mSimIndex, actualFlow);
            mTrafficCalibrateControler.setCommonOnlyLeftFlag(context, mSimIndex, false);
            mTrafficCalibrateControler.setTafficPackageSetted(context, mSimIndex, true);
            mTrafficCalibrateControler.setFlowlinkFlag(context, mSimIndex, 0);
            mTrafficCalibrateControler.setStopWarningFlag(context, mSimIndex, false);
            mTrafficCalibrateControler.setStopExhaustFlag(context, mSimIndex, false);
            Log.d(TAG, "savePreference totalFlow=" + totalFlow + " percent=" + percent + " used="
                    + used + " curDate=" + curDate + " left=" + left + " actualFlow=" + actualFlow);
        } catch (Exception e) {
            Log.d(TAG, "savePreference Exception e=" + e.toString());
        }
    }
    // Gionee: mengdw <2015-11-11> add for CR01589343 end

    private void updateNotificationBar(Context context) {
        TrafficSettingControler trafficSettingControler = TrafficSettingControler.getInstance(mContext);
        trafficSettingControler.commitTrafficNotiAction(mContext);
    }

    private void selectCycleDay() {
        int day = 31;
        CyeeNumberPicker picker = new CyeeNumberPicker(this);
        // picker.setBackground(getResources().getDrawable(R.drawable.gn_ic_timepicker_bg));
        picker.setOnValueChangedListener(new CyeeNumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(CyeeNumberPicker arg0, int oldVal, int newVal) {
                mCycleDay = newVal;
            }
        });

        picker.setWrapSelectorWheel(false);
        picker.setFocusable(false);
        ((EditText) picker.getChildAt(1)).setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // do nothing
            }
        });
        ((EditText) picker.getChildAt(1)).setInputType(android.text.InputType.TYPE_NULL);

        picker.setMinValue(1);
        picker.setMaxValue(day);
        picker.setValue(mCycleDay);

        CyeeAlertDialog.Builder alertDialog = new CyeeAlertDialog.Builder(mContext);

        alertDialog.setTitle(getString(R.string.date_start_settings));
        alertDialog.setView(picker, 70, 40, 70, 40);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                updateDateText();
            }
        });
        alertDialog.setPositiveButton(R.string.action_done, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateDateText();
            }
        });

        alertDialog.show();
    }

    OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
                double value = 0;
                mPercentValue.setText(String.valueOf(mSeekBar.getProgress() + mProgressStart));
                if (!mTotalFlow.getText().toString().isEmpty()) {
                    value = Integer.parseInt(mPercentValue.getText().toString())
                            * Integer.parseInt(mTotalFlow.getText().toString());
                }
                mWarningValue.setText(String.valueOf(value / 100));
            } catch (Exception e) {
                Log.d(TAG, "onProgressChanged Exception e=" + e.toString());
            }
        }
    };

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            try {
                if (mTotalFlow.isFocused() && !s.toString().isEmpty()) {
                    if (Float.valueOf(s.toString()) == 0) {
                        s.replace(0, s.length(), "");
                    } else if (s.toString().startsWith("0")) {
                        s.replace(0, s.length(), String.valueOf(Integer.valueOf(s.toString())));
                    }

                    int percent = Integer.valueOf(mPercentValue.getText().toString());
                    double value = 0;
                    if (!mTotalFlow.getText().toString().isEmpty()) {
                        value = (percent * Integer.valueOf(mTotalFlow.getText().toString()));
                    }
                    mWarningValue.setText(String.valueOf(value / 100));
                /*guoxt modify for CR01548662 begin*/
                } else if (mTotalFlow.isFocused() && s.toString().isEmpty()) {
                    mWarningValue.setText("0");
                }
                /*guoxt modify for CR01548662 end*/

                if (mUsedFlow.isFocused() && !s.toString().isEmpty()) {
                    if (s.toString().startsWith(".")) {
                        s.replace(0, 1, "0.");
                    } else if (s.toString().length() > 1 && !s.toString().startsWith("0.")) {
                        if (Float.valueOf(s.toString()) == 0) {
                            s.replace(0, s.length(), "0");
                        } else if (s.toString().startsWith("0")) {
                            if (s.toString().contains(".")) {
                                s.replace(0, s.length(), String.valueOf(Float.valueOf(s.toString())));
                            } else {
                                s.replace(0, s.length(), String.valueOf(Integer.valueOf(s.toString())));
                            }
                            // s.replace(0, s.length(),
                            // String.valueOf(Float.valueOf(s.toString())));
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Exception e=" + e.toString());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    private void updateDateText() {
        String language = getResources().getConfiguration().locale.getCountry();
        if (language.equals("UK") || language.equals("US")) {
            mDate.setText(mCycleDay + "");
        } else {
            mDate.setText(mCycleDay + getString(R.string.day));
        }
    }
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end