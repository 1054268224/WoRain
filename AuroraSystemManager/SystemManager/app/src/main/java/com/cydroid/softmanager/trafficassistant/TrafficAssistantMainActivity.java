package com.cydroid.softmanager.trafficassistant;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.cydroid.framework.SIMInfoFactory;
import com.cydroid.framework.provider.SIMInfo;
import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.UtilizationAlertDialog;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.trafficassistant.controler.TrafficCalibrateControler;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.service.TrafficMonitorControlService;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.StringFormat;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;
import com.cydroid.softmanager.view.TrafficCircleView;
import com.cydroid.softmanager.view.TrafficaBallView;

import java.util.ArrayList;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;

//Gionee: mengdw <2015-08-06> modify for CR01539821 begin

public class TrafficAssistantMainActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "TrafficAssistantMainActivity";

    private static final boolean[] sTrafficSettings = new boolean[Constant.SIM_COUNT];

    private static final int SWEEP_ANGLE_CONSTANT = 300;
    private static final int PERCENT_CONSTANT = 100;
    private static final int STATE_INTO_SCREEN = 8;
    //Gionee: mengdw <2015-11-11> modify for CR01589343 begin
    private static final int COMMON_PAGE_INDEX = 0;
    private static final int IDLE_PAGE_INDEX = 1;
    private static final int INVAID_DATA_VALUE = -1;

    private TrafficCalibrateControler mTrafficCalibrateControler;

    private final int[] mCurrentPageIndex = {0, 0};
    private CyeeActionBar mActionBar;
    //Gionee: mengdw <2015-11-11> modify for CR01589343 end
    private Context mContext;
    private RelativeLayout mLayout;
    private TextView[] mTextViewUsed;
    private TextView[] mTextViewState;
    private TextView[] mTextViewStateValue;
    private TextView[] mTextViewUnitValue;
    private TextView[] mTextViewStateOtherValue;
    private TextView[] mTextViewUnitOtherValue;
    private TrafficCircleView[] mTrafficView;
    private TrafficaBallView[] mTrafficBallView;
    private ImageView[] mImgPrompt;
    private CyeeButton[] mFlowButton;
    private ViewPager mViewPager;
    private TextView mTodayText;
    private TextView mTextViewRestrictValue;
    private SlideImageAdapter mSlideImageAdapter;

    private final boolean[] mViewInit = {false, false};

    private ArrayList<View> mPageViews;

    private int mSimCount;
    private String[] mSimName;
    private final boolean[] mSimState = {false, false};
    private int mCurrentSimIndex = 0;
    private int mActivatedSimIndex;
    private boolean mClickFlag = false;
    /* animation flag when entering into screen first */
    private boolean isIntoScreenAnima = false;
    // Gionee: mengdw <2016-07-13> add for CR01770072 begin
    private UtilizationAlertDialog mUtilizationAlertDialog;
    private UtilizationDialogListener mUtilizationDialogListener;
    // Gionee: mengdw <2016-07-13> add for CR01770072 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.TrafficTheme);
        //setTheme(R.style.AppTabThemeCustom);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trafficassistant_main_layout);
        mContext = this;
        // Gionee: mengdw <2016-07-13> add for CR01770072 begin
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isFirstFlagSet = preferences.getBoolean("is_first_utilization", true);
        Intent fromIntent = this.getIntent();
        boolean isFromNoti = false;
        if (fromIntent != null) {
            int activityFrom = fromIntent.getIntExtra(SellUtils.KEY_ACTIVITY_FORM, SellUtils.ACTIVITY_FORM_MAIN);
            if (activityFrom == SellUtils.ACTIVITY_FORM_NOTIFICATION) {
                isFromNoti = true;
            }
        }
        Log.d(TAG, "onCreate isFirstFlagSet=" + isFirstFlagSet + " isFromNoti=" + isFromNoti);
        init();
        //guoxt modify for CSW1805A-887 begin
       // if (isFromNoti && isFirstFlagSet) {
          ///  showVerdictDialog();
        //} else {
            //showCalibrateDialog(mContext);
            showTrafficSettingsDialog(mContext);
       // }
        //guoxt modify for CSW1805A-887 end
        // Gionee: mengdw <2016-07-13> add for CR01770072 end

    }

    // Gionee: mengdw <2016-10-31> add for CR01770072 begin
    private void showVerdictDialog() {
        // todo wsj 注掉
        /*mUtilizationAlertDialog = new UtilizationAlertDialog(this);
        mUtilizationDialogListener = new UtilizationDialogListener();
        mUtilizationAlertDialog.addDialogBtnOnclickListener(mUtilizationDialogListener);
        mUtilizationAlertDialog.verdictWhetherShowDialog();*/
    }
    // Gionee: mengdw <2016-10-31> add for CR01770072 end

    private void chameleonColorProcess(int simIndex) {
        if (ChameleonColorManager.isNeedChangeColor()) {
            mLayout.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            ColorStateList csl = ColorStateList
                    .valueOf(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            // Gionee: mengdw <2016-11-15> add for CR01773139 begin
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            int color_T3 = ChameleonColorManager.getContentColorThirdlyOnAppbar_T3();
            int color_B4 = ChameleonColorManager.getButtonBackgroudColor_B4();
            /*mTrafficView[simIndex].setArcColor(color_T1);
            mTrafficView[simIndex].setInnerCircleColor(color_T3);*/
            mTrafficBallView[simIndex].setCircleClor(Color.WHITE);
            mTrafficBallView[simIndex].setRingColor(Color.WHITE);
            mTextViewUsed[simIndex].setTextColor(color_T1);
            mTextViewState[simIndex].setTextColor(color_T1);
            mTextViewUnitValue[simIndex].setTextColor(color_T1);
            mTextViewStateValue[simIndex].setTextColor(color_T1);
            mTextViewStateOtherValue[simIndex].setTextColor(color_T1);
            mTextViewUnitOtherValue[simIndex].setTextColor(color_T1);
            mFlowButton[simIndex].setTextColor(color_T1);
            mFlowButton[simIndex].setBackgroundColorFilter(color_B4);
            if (mSimCount == Constant.SIM_COUNT){
                for (int i = 0; i < mSimCount; i++) {
                    mActionBar.getTabAt(i).setTextColor(csl);
                }
            }
            // Gionee: mengdw <2016-11-15> add for CR01773139 end
        }
    }

    private void init() {
        ChameleonColorManager.getInstance().onCreate(this);
        // Gionee: mengdw <2015-11-11> add for CR01589343 begin
        TrafficMonitorControlService.processMonitorControlServiceIntent(mContext, true);
        mTrafficCalibrateControler = TrafficCalibrateControler.getInstance(mContext);

        initActionBar();
        initSimInfo(mContext);
        initView();
        setLayoutClickListener();
    }

    private void initView() {
        mLayout = (RelativeLayout) findViewById(R.id.gn_dial_layout);

        mViewPager = (ViewPager) findViewById(R.id.image_slide_page);
        mTodayText  = (TextView) findViewById(R.id.sim_card_flow_today_value);
        mTextViewRestrictValue = (TextView) findViewById(R.id.sim_card_flow_total_value);

        int viewCount = mSimCount == Constant.SIM_COUNT ? 2 : 1;
        mTextViewUsed = new TextView[viewCount];
        mTextViewState = new TextView[viewCount];
        mTextViewStateValue = new TextView[viewCount];
        mTextViewUnitValue = new TextView[viewCount];
        mTextViewStateOtherValue = new TextView[viewCount];
        mTextViewUnitOtherValue = new TextView[viewCount];
        /*mTrafficView = new TrafficCircleView[viewCount];*/
        mTrafficBallView = new TrafficaBallView[viewCount];
        mImgPrompt = new ImageView[viewCount];
        mFlowButton = new CyeeButton[viewCount];

        LayoutInflater inflater = getLayoutInflater();
        mPageViews = new ArrayList<View>();
        mPageViews.add(inflater.inflate(R.layout.traffic_circle, null));
        if (mSimCount == Constant.SIM_COUNT){
            mPageViews.add(inflater.inflate(R.layout.traffic_circle, null));
        }

        //Gionee <jiangsj> <20170419> add for 113672 end
        mSlideImageAdapter = new SlideImageAdapter();
        mViewPager.setAdapter(mSlideImageAdapter);
        mViewPager.setOnPageChangeListener(new ImagePageChangeListener());
        mViewPager.setCurrentItem(mCurrentSimIndex);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                mActionBar.onPageScrolled(arg0, arg1, arg2);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("guoxt:","tab.onPageSelected()" + position);
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        if (mSimCount == 0 || mSimCount == 1){
            findTabView(0);
        }else {
            for (int i = 0; i < mSimCount; i++){
                findTabView(i);
            }
        }
    }

    public OnClickListener  mflowOnClickLister  = new OnClickListener() {
        @Override
        public void onClick(View v) {
            /*actionGnNewActivity(mContext, TrafficLimitActivity.class);*/

        }
    };

    public  void addTabs(){
        int[] tabTitle = new int[]{R.string.traffic_simcard_focus1, R.string.traffic_simcard_focus2};
        if(mSimCount < Constant.SIM_COUNT ){
            return;
        }
        SIMInfo simInfo = null;
        for (int i = 0; i < mSimCount; i++) {
            if (SIMInfoFactory.getDefault() != null) {
                simInfo = SIMInfoFactory.getDefault().getSIMInfoBySlot(mContext, i);
            }
            Log.d("guoxt,tab=" ,+ tabTitle[i]+ simInfo.mDisplayName);

            mActionBar.addTab(mActionBar.newTab().setText(getString(tabTitle[i]) + "(" + simInfo.mDisplayName + ")").setTextColor(UiUtils.getColorStateList(this)).setTabListener(tablisten));
        }

    }


    public CyeeActionBar.TabListener tablisten = new CyeeActionBar.TabListener() {
        @Override
        public void onTabSelected(CyeeActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            if (mViewPager != null) {
                Log.d("guoxt:","tab.getPosition()" + tab.getPosition());
                mViewPager.setCurrentItem(tab.getPosition());
            }
        }

        @Override
        public void onTabUnselected(CyeeActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
           // Log.d("guoxt:","tab.getPosition()" + tab.getPosition());
           // mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabReselected(CyeeActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    };

    private void setLayoutClickListener() {
        LinearLayout mLayoutNetworkControl = (LinearLayout) findViewById(R.id.gn_networkControl_layout);
        mLayoutNetworkControl.setOnClickListener(this);

        LinearLayout mLayoutDataSaverControl = (LinearLayout) findViewById(R.id.gn_datasaver_layout);
        mLayoutDataSaverControl.setOnClickListener(this);

        LinearLayout mLayoutNetworkRankControl = (LinearLayout) findViewById(R.id.gn_detail_layout);
        mLayoutNetworkRankControl.setOnClickListener(this);

        LinearLayout mLayoutDataPlanControl = (LinearLayout) findViewById(R.id.gn_restriction_layout);
        mLayoutDataPlanControl.setOnClickListener(this);
    }

    private void initSimInfo(Context context) {
        updateSimStateAndName(context);
        mActivatedSimIndex = TrafficassistantUtil.getActivatedSimIndex(context);
        if (mSimCount == Constant.SIM_COUNT) {
            mActionBar.setNavigationMode(CyeeActionBar.NAVIGATION_MODE_TABS);
            addTabs();
        } else {
            mActionBar.removeAllTabs();
        }
    }

    private void initActionBar() {
        mActionBar = getmActionBar();
        UiUtils.setElevation(mActionBar, 0);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setIndicatorBackgroundColor(Color.WHITE);

        LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.systemmanager_settings_actionbar,
                null);
        ImageView img = (ImageView) v.findViewById(R.id.img_actionbar_custom);
        ImageView otherImg = (ImageView) v.findViewById(R.id.img_another_button);
        LinearLayout first = (LinearLayout) v.findViewById(R.id.first_click_field);
        // Gionee: mengdw <2015-11-11> add for CR01589343 begin
        otherImg.setImageResource(R.drawable.power_summary_icon);
        first.setVisibility(View.INVISIBLE);
        first.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                actionGnNewActivity(mContext, TrafficRankActivity.class);
            }
        });
        // Gionee: mengdw <2015-11-11> add for CR01589343 end
        LinearLayout second = (LinearLayout) v.findViewById(R.id.second_click_field);
        second.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                actionGnNewActivity(mContext, TrafficSettingsActivity.class);
            }
        });
        img.setImageResource(R.drawable.main_actionbar_setting);
        CyeeActionBar.LayoutParams lp = new CyeeActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        mActionBar.setCustomView(v, lp);
        mActionBar.setDisplayShowCustomEnabled(true);
    }

    private void updateSimStateAndName(Context context) {
        SIMInfoWrapper wrapper = SIMInfoWrapper.getDefault(context);
        int count = wrapper.getInsertedSimCount();
        mSimCount = count;
        TypedArray name = context.getResources().obtainTypedArray(R.array.tab_name);
        mSimName = new String[name.length()];
        for (int i = 0; i < name.length(); i++) {
            mSimName[i] = name.getString(i);
        }
        name.recycle();

        if (count > 0) {
            SIMParame simInfo = new SIMParame();
            for (int i = 0; i < count; i++) {
                simInfo = wrapper.getInsertedSimInfo().get(i);
                mSimState[simInfo.mSlot] = true;
                if (count == 1) {
                    mCurrentSimIndex = simInfo.mSlot;
                } else {
                    mCurrentSimIndex = getIntent().getIntExtra(Constant.SIM_VALUE, Constant.SIM1);
                    if (mCurrentSimIndex == Constant.HAS_NO_SIMCARD) {
                        mCurrentSimIndex = Constant.SIM1;
                    }
                }
            }

            for (int i = 0; i < count; i++) {
                simInfo = wrapper.getInsertedSimInfo().get(i);
                mSimName[simInfo.mSlot] = simInfo.mDisplayName;
            }
        }
        SIMInfoWrapper.setEmptyObject(context);
    }

    private void showTrafficSettingsDialog(Context context) {
        boolean isFirst = mTrafficCalibrateControler.isFirstEntry(context);
        // Gionee: mengdw <2015-11-17> add for CR01589343 begin
        boolean hasSimCard = mSimState[0] || mSimState[1];
        // Gionee: mengdw <2015-11-17> add for CR01589343 ebd
        if (isFirst && hasSimCard) {
            /*guoxt modify begin */
            showDialog(context, listener, R.string.popup_setting_title_dialog,
                    R.string.popup_setting_content_dialog, R.string.action_settings);
            mTrafficCalibrateControler.resetFirstEntryFlag(context);
            
            /*guoxt modify end */
        }
    }

    // Gionee: mengdw <2016-01-12> add for CR01617372 begin
    private void showDialog(Context context, DialogInterface.OnClickListener dialogListener, int Title,
                            int msg, int positiveBtnTxt) {
        // Modify by zhiheng.huang on 2020/4/1 for TEWBW-1369 start
        CyeeAlertDialog dialog = new CyeeAlertDialog.Builder(context)
                .setTitle(Title)
                .setMessage(msg)
                .setPositiveButton(positiveBtnTxt, dialogListener)
                .setNegativeButton(R.string.action_cancel, dialogListener)
                .create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.dialog_ripple);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.dialog_ripple);
        // Modify by zhiheng.huang on 2020/4/1 for TEWBW-1369 end
    }
    // Gionee: mengdw <2016-01-12> add for CR01617372 end

    private void resetIntoScreenAnimaFlag(boolean flag) {
        isIntoScreenAnima = flag;
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case CyeeAlertDialog.BUTTON_POSITIVE:
                    actionGnNewActivity(mContext, TrafficLimitActivity.class);
                    break;

                case CyeeAlertDialog.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        mClickFlag = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSimCount >= 1){
            if (!isIntoScreenAnima) {
                onIntoScreenAnimation(mContext, mCurrentSimIndex);
            } else {
                resetUI(mContext, mCurrentSimIndex);
            }
        }

        NotificationController.getDefault(mContext.getApplicationContext()).cancelNotification();
    }

    // Gionee <liuyb> <2014-3-3> add for CR01078882 begin
    @Override
    protected void onDestroy() {
        // TrafficStats.closeQuietly(mStatsSession);
        releasRes();
        SIMInfoWrapper.setEmptyObject(mContext);
        super.onDestroy();
        mViewPager.setAdapter(null);
        mPageViews.clear();
        ChameleonColorManager.getInstance().onDestroy(this);

        // Gionee: mengdw <2016-07-13> add for CR01731010 begin
        if (mUtilizationAlertDialog != null) {
            mUtilizationAlertDialog.dismissDialog();
            if (null != mUtilizationDialogListener) {
                mUtilizationAlertDialog.removeDialogBtnOnclickListener(mUtilizationDialogListener);
            }
        }
        // Gionee: mengdw <2016-07-13> add for CR01731010 end
    }

    private void releasRes() {
        Util.unbindDrawables(findViewById(R.id.root));
    }

    // Gionee <liuyb> <2014-3-3> add for CR01078882 end

    @Override
    public void onClick(View v) {
        if (mClickFlag) {
            return;
        }
        mClickFlag = true;

        switch (v.getId()) {
            case R.id.gn_restriction_layout:
                // Gionee: mengdw <2015-12-30> add for CR01616089 begin
                //YouJuAgent.onEvent(mContext, "Traffic_Limit");
                // Gionee: mengdw <2015-12-30> add for CR01616089 end
                actionGnNewActivity(mContext, TrafficLimitActivity.class);
                break;

          //  case R.id.gn_detail_layout:
                //Gionee: mengdw <2015-11-11> modify for CR01589343 begin
               // actionGnNewActivity(mContext, TrafficBuyActivity.class);
                //Gionee: mengdw <2015-11-11> modify for CR01589343 end
                //Gionee: mengdw <2015-12-30> add for CR01616089 begin
                //YouJuAgent.onEvent(mContext, "Traffic_Sell");
                //Gionee: mengdw <2015-12-30> add for CR01616089 end
               // break;

            case R.id.gn_networkControl_layout:
                // Gionee: mengdw <2015-12-30> add for CR01616089 begin
                // YouJuAgent.onEvent(mContext, "Traffic_Control");
                // Gionee: mengdw <2015-12-30> add for CR01616089 end
                actionGnNewActivity(mContext, TrafficNetworkControlActivity.class);
                break;

            case R.id.gn_datasaver_layout:
                // Gionee: mengdw <2015-12-30> add for CR01616089 begin
                // YouJuAgent.onEvent(mContext, "Traffic_Control");
                // Gionee: mengdw <2015-12-30> add for CR01616089 end
                //actionGnNewActivity(mContext, TrafficNetworkControlActivity.class);

                Intent intent = new Intent();
                intent.setAction("android.settings.DATA_USAGE_SETTINGS");
                intent.putExtra("start_from_softmanager", true);
                startActivity(intent);


                break;
            case R.id.gn_detail_layout:
                // Gionee: mengdw <2015-12-30> add for CR01616089 begin
                // YouJuAgent.onEvent(mContext, "Traffic_Control");
                // Gionee: mengdw <2015-12-30> add for CR01616089 end
                actionGnNewActivity(mContext, TrafficRankActivity.class);
                break;
            default:
                break;
        }
    }

    private void actionGnNewActivity(Context context, Class<?> cla) {
        Intent intent = new Intent(context, cla);
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.SIM_VALUE, mCurrentSimIndex);
        intent.putExtras(bundle);
        //Gionee: mengdw <2015-11-11> modify for CR01589343 begin
        //guoxt removed begin 
        //intent.putExtra(SellUtils.KEY_START_SOURCE, SellUtils.BUY_SOURCE.SOURCE_MENU.ordinal());
        //Gionee: mengdw <2015-11-11> modify for CR01589343 end
        //guoxt removed end 
        startActivity(intent);
    }

    private void resetUI(Context context, int sim) {
        if (mSimCount == 1){
            updateUI(context, sim);
        }else if (mSimCount == Constant.SIM_COUNT){
            for (int simIndex = 0; simIndex < mSimCount; simIndex++){
                updateUI(context, simIndex);
            }
        }
    }

    private void updateUI(Context context, int simIndex){
        setViewsVisibility(simIndex);
        boolean isTrafficPackageSetted = mTrafficCalibrateControler.isTafficPackageSetted(context, simIndex);
        setTxt(simIndex, isTrafficPackageSetted);
        setOtherViewText(isTrafficPackageSetted);
        drawImage(context, simIndex);
    }

    private void setViewsVisibility(int simIndex) {
        if (mSimCount == 0){
            /*mImgPrompt[0].setVisibility(View.VISIBLE);*/
            mFlowButton[0].setVisibility(View.GONE);
        }else if (mSimCount == 1){
            int viewIndex = getViewIndex(simIndex);
            setViewVisible(viewIndex);
        }else {
            if (mSimState[simIndex]) {
                setViewVisible(simIndex);
            } else {
                setViewGone(simIndex);
            }
        }
    }

    private void setViewVisible(int simIndex){
        mTextViewState[simIndex].setVisibility(View.VISIBLE);
        mTextViewStateValue[simIndex].setVisibility(View.VISIBLE);
        mTextViewUnitValue[simIndex].setVisibility(View.VISIBLE);
        // Modify by zhiheng.huang on 2020/4/1 for TEWBW-1368	 start
        mTextViewStateOtherValue[simIndex].setVisibility(View.GONE);
        mTextViewUnitOtherValue[simIndex].setVisibility(View.GONE);
        // Modify by zhiheng.huang on 2020/4/1 for TEWBW-1368	 end
        mFlowButton[simIndex].setVisibility(View.VISIBLE);
    }

    private void setViewGone(int simIndex){
        mTextViewState[simIndex].setVisibility(View.GONE);
        mTextViewStateValue[simIndex].setVisibility(View.GONE);
        mTextViewUnitValue[simIndex].setVisibility(View.GONE);
        mTextViewStateOtherValue[simIndex].setVisibility(View.GONE);
        mTextViewUnitOtherValue[simIndex].setVisibility(View.GONE);
        mFlowButton[simIndex].setVisibility(View.GONE);
    }

    private void setOtherViewText(boolean setPackage) {
        // Gionee: mengdw <2015-10-31> modify for CR01569888 begin
        if (mSimCount == 0){
            mTextViewRestrictValue.setText(R.string.no_settings);
            mTodayText.setText("0.0B");
            return;
        }
        if (!setPackage) {
            mTextViewRestrictValue.setText(R.string.no_settings);
        } else {
            int totalFlow = mTrafficCalibrateControler.getCommonTotalTaffic(mContext, mCurrentSimIndex);
            boolean isOnlyLeft = mTrafficCalibrateControler.isCommonOnlyLeft(mContext, mCurrentSimIndex);
            if (totalFlow != 0 && !isOnlyLeft) {
                mTextViewRestrictValue.setText(totalFlow + getString(R.string.flow_unit));
            } else {
                mTextViewRestrictValue.setText("");
            }
        }

        String todayFlow = getTodayFlow(mCurrentSimIndex);
        if(todayFlow != null && !todayFlow.isEmpty()){
            mTodayText.setText(todayFlow);
        }

        // Gionee: mengdw <2015-10-31> modify for CR01569888 end
    }

    private void processFlowDataView(int simIndex, float flowData) {
        String str;
        if (flowData >= Constant.UNIT) {
            // fengpeipei modify for Bug#42575 start
            String strtmp = StringFormat.getUnitStringByValue(Math.abs(flowData) * Constant.MB, 1);
            char lastChar = strtmp.charAt(strtmp.length() - 1);
            if (lastChar == 'G') {
                str = strtmp.substring(0, strtmp.length() - 1);
            } else {
                str = StringFormat.getStringFormat(flowData / Constant.UNIT, 1);
            }
            // fengpeipei modify for Bug#42575 end
            if (Integer.parseInt(str.substring(str.length() - 1)) == 0) {
                str = StringFormat.getStringFormat(flowData / Constant.UNIT, 0);
            }
            int viewIndex = getViewIndex(simIndex);
            onTrafficVisibility(viewIndex, str.length() > 3);
            mTextViewStateValue[viewIndex].setText(str);
            mTextViewStateOtherValue[viewIndex].setText(str);
            mTextViewUnitValue[viewIndex].setText(Constant.STRING_UNIT_GB);
            mTextViewUnitOtherValue[viewIndex].setText(Constant.STRING_UNIT_GB);
        } else {
            // fengpeipei modify for Bug#42575 start
            String strtmp = StringFormat.getUnitStringByValue(Math.abs(flowData) * Constant.MB, 1);
            char lastChar = strtmp.charAt(strtmp.length() - 1);
            if (lastChar == 'M') {
                str = strtmp.substring(0, strtmp.length() - 1);
            } else {
                str = StringFormat.getStringFormat(flowData, 1);
            }
            // fengpeipei modify for Bug#42575 end
            if (Integer.parseInt(str.substring(str.length() - 1)) == 0) {
                str = StringFormat.getStringFormat(flowData, 0);
            }
            int viewIndex = getViewIndex(simIndex);
            onTrafficVisibility(viewIndex, str.length() > 3);
            mTextViewStateValue[viewIndex].setText(str);
            mTextViewStateOtherValue[viewIndex].setText(str);
            mTextViewUnitValue[viewIndex].setText(Constant.STRING_UNIT_MB);
            mTextViewUnitOtherValue[viewIndex].setText(Constant.STRING_UNIT_MB);
        }
    }

    private void showCommonDataView(int simIndex) {
        float commonUsed = mTrafficCalibrateControler.getCommonUsedTaffic(mContext, simIndex);
        String txtUsed = String.format(getString(R.string.used_flow_string),
                StringFormat.getStringFormat(commonUsed));
        boolean isOnlyLeft = mTrafficCalibrateControler.isCommonOnlyLeft(mContext, simIndex);
        int viewIndex = getViewIndex(simIndex);
        if (isOnlyLeft) {
            mTextViewUsed[viewIndex].setText("");
        } else {
            mTextViewUsed[viewIndex].setText(txtUsed);
        }
        float commonLeft = mTrafficCalibrateControler.getCommonLeftTraffic(mContext, simIndex);
        if (mTrafficCalibrateControler.isCommonTrafficSurplus(mContext, simIndex)) {
            mTextViewState[viewIndex].setText(
                    getString(R.string.traffic_origan_txt) + getString(R.string.remained_flow_string));
        } else {
            mTextViewState[viewIndex].setText(
                    getString(R.string.traffic_origan_txt) + getString(R.string.exceed_flow_string));
        }
        processFlowDataView(simIndex, Math.abs(commonLeft));
    }


    private String getTodayFlow(int simIndex) {
        int[] todayDate = TimeFormat.getNowTimeArray();
        long startTime = TimeFormat.getStartTime(todayDate[0], todayDate[1] + 1, todayDate[2], 0, 0, 0);
        String todayFlow = TrafficassistantUtil.getTrafficString(mContext, simIndex, startTime, 0, 0);
        //fengpeipei modify for 61661 start
        //return TrafficassistantUtil.replaceUnit(todayFlow);
        return todayFlow;
        //fengpeipei modify for 61661 end
    }

    private void showIdleDataView(int simIndex) {
        float idleUsed = mTrafficCalibrateControler.getIdleUsed(mContext, simIndex);
        String txtUsed = String.format(getString(R.string.used_flow_string),
                StringFormat.getStringFormat(idleUsed));
        boolean isOnlyLeft = mTrafficCalibrateControler.isIdleOnlyLeftFlag(mContext, simIndex);
        int viewIndex = getViewIndex(simIndex);
        if (isOnlyLeft) {
            mTextViewUsed[viewIndex].setText("");
        } else {
            mTextViewUsed[viewIndex].setText(txtUsed);
        }
        mTextViewState[viewIndex].setText(
                getString(R.string.traffic_idle_txt) + getString(R.string.remained_flow_string));
        float idleLeft = mTrafficCalibrateControler.getIdleLeft(mContext, simIndex);
        processFlowDataView(simIndex, idleLeft);
    }

    private void setTxt(int simIndex, boolean setTrafficPackages) {
        if (!mSimState[simIndex]) {
            //mTextViewSimCardFlag.setVisibility(View.VISIBLE);
           // mTextViewSimCardFlag.setText(getString(R.string.no_card_warning));
        } else {
            // Gionee: mengdw <2016-01-30> modify for CR01630029 begin
            int startDay = mTrafficCalibrateControler.getStartDate(mContext, simIndex);
            float actualFlow = TrafficassistantUtil.getActualFlow(mContext, simIndex, startDay);
            float calibrateFlow = mTrafficCalibrateControler.getCalibratedActualFlow(mContext,
                    simIndex);
            float saveFlow = mTrafficCalibrateControler.getSaveActualFlow(mContext, simIndex);
            if (!isAirModeOn(mContext)) {
                mTrafficCalibrateControler.setSaveActualFlow(mContext, simIndex, actualFlow);
            }
            Log.d(TAG, "setTxt airMode=" + isAirModeOn(mContext) + " actualFlow=" + actualFlow
                    + " calibrateFlow=" + calibrateFlow + " saveFlow=" + saveFlow);
            // Gionee: mengdw <2016-01-30> modify for CR01630029 end
            int viewIndex = getViewIndex(simIndex);
            mTextViewUsed[viewIndex].setVisibility(View.VISIBLE);

            if (setTrafficPackages) {
                // Chenyee xionghg 20180103 add for CSW1702A-742 begin
                updateNotificationBar();
                // Chenyee xionghg 20180103 add for CSW1702A-742 end
                if (mCurrentPageIndex[viewIndex] == COMMON_PAGE_INDEX) {
                    showCommonDataView(simIndex);
                } else {
                    showIdleDataView(simIndex);
                }
            } else {
                float used = 0;
                if (mCurrentPageIndex[viewIndex] == COMMON_PAGE_INDEX) {
                    used = mTrafficCalibrateControler.getCommonUsedTaffic(mContext, simIndex);
                } else {
                    used = mTrafficCalibrateControler.getIdleUsed(mContext, simIndex);
                }
                mTextViewState[viewIndex].setText(getString(R.string.used));
                processFlowDataView(simIndex, used);
                mTextViewUsed[viewIndex].setText(getString(R.string.prompt_no_settings_traffic));
            }
        }
    }

    // Gionee: mengdw <2016-01-30> modify for CR01630029 begin
    private boolean isAirModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    private void onTrafficVisibility(int simIndex, boolean flag) {
        View[] views = {mTextViewStateValue[simIndex], mTextViewStateOtherValue[simIndex], mTextViewUnitValue[simIndex],
                mTextViewUnitOtherValue[simIndex]};
        for (int i = 0; i < views.length; i++) {
            if (i % 2 == 0) {
                views[i].setVisibility(flag ? View.GONE : View.VISIBLE);
            } else {
                views[i].setVisibility(!flag ? View.GONE : View.VISIBLE);
            }
        }
    }

    private float[] setDegrees(int simIndex) {
        if (!mTrafficCalibrateControler.isTafficPackageSetted(mContext, simIndex)) {
            return new float[]{0, 0, 0};
        }
        float[] degree = new float[3];
        float usedFlow = mTrafficCalibrateControler.getCommonUsedTaffic(mContext, simIndex);
        int totalFlow = mTrafficCalibrateControler.getCommonTotalTaffic(mContext, simIndex);
        float usedPercent = ((usedFlow / totalFlow) > 1) ? 1 : (usedFlow / totalFlow);
        degree[0] = usedPercent * SWEEP_ANGLE_CONSTANT;
        degree[1] = mTrafficCalibrateControler.getWarnPercent(mContext, simIndex);
        degree[2] = (float) (usedPercent >= 1 ? 0.9 : usedPercent) * PERCENT_CONSTANT;
        if (degree[2] < 0) {
            degree[2] = 0;
        }
        return degree;
    }

    private void drawImage(Context context, int simIndex) {
        if (!mSimState[simIndex] || !mTrafficCalibrateControler.isTafficPackageSetted(context, simIndex)) {
            updateWave(0, -1, simIndex);
            /*updateCircle(0, -1, simIndex);*/
        } else {
            float[] degree = setDegrees(simIndex);
            updateWave((int) degree[0], (int) degree[1], simIndex);
            /*updateCircle((int) degree[0], (int) degree[1], simIndex);*/
            int totalFlow = mTrafficCalibrateControler.getCommonTotalTaffic(mContext, simIndex);
            int viewIndex = getViewIndex(simIndex);
            if (totalFlow * degree[1] * 0.01 > Constant.UNIT) {
                mTrafficBallView[viewIndex].setWarningText(
                        StringFormat.getStringFormat(totalFlow * degree[1] * 0.01 / Constant.UNIT, 1)
                                + Constant.STRING_UNIT_GB);
            } else {
                mTrafficBallView[viewIndex].setWarningText(
                        (int) (totalFlow * degree[1] * 0.01) + Constant.STRING_UNIT_MB);
            }

        }
    }

    /*private void updateCircle(int ratio, int warningRatio, int simIndex) {
        int viewIndex = getViewIndex(simIndex);
        mTrafficView[viewIndex].updateRatio(ratio);
        mTrafficView[viewIndex].updateWarningRatio(warningRatio);
        mTrafficView[viewIndex].updateViews();
    }*/

    private void updateWave(int ratio, int warningRatio, int simIndex){
        int viewIndex = getViewIndex(simIndex);
        mTrafficBallView[viewIndex].updateWaterLevel(ratio);
        mTrafficBallView[viewIndex].updateWarningRatio(warningRatio);
        mTrafficBallView[viewIndex].updateViews();
    }

    private void updateTrafficInfoTxt(int simIndex, double percent) {
        int totalFlow = mTrafficCalibrateControler.getCommonTotalTaffic(mContext, simIndex);
        boolean overflow = (1 - percent) * totalFlow > Constant.UNIT;
        String str1, str2, str3;
        str2 = percent < 1 ? getString(R.string.remained_flow_string)
                : getString(R.string.exceed_flow_string);
        if (overflow) {
            str1 = StringFormat.getStringFormat((1 - percent) * totalFlow / Constant.UNIT, 1);
            str3 = Constant.STRING_UNIT_GB;
        } else {
            str1 = StringFormat.getStringFormat((1 - percent) * totalFlow, 1);
            str3 = Constant.STRING_UNIT_MB;
        }

        int viewIndex = getViewIndex(simIndex);
        mTextViewStateValue[viewIndex].setText(str1);
        mTextViewStateOtherValue[viewIndex].setText(str1);
        mTextViewState[viewIndex].setText(str2);
        mTextViewUnitValue[viewIndex].setText(str3);
        mTextViewUnitOtherValue[viewIndex].setText(str3);
    }

    public static void TrafficPackageSettingNoti(Context context) {
        if (TrafficassistantUtil.getSimCount(context) == 0) {
            return;
        }
        int simIndex = TrafficassistantUtil.getSimCardNo(context);
        MainProcessSettingsProviderHelper providerHelper = new MainProcessSettingsProviderHelper(context);
        if (providerHelper.getBoolean(TrafficassistantUtil.getSimSetting(simIndex), false)) { // setting
            return;
        }
        // Gionee: mengdw <2016-06-29> delete for CR01724694 begin
        // popNoti(context, simIndex);
        // Gionee: mengdw <2016-06-29> delete for CR01724694 end
        providerHelper.putBoolean(TrafficassistantUtil.getSimNotification(simIndex), true);
    }

    private static void popNoti(Context context, int index) {
        NotificationController notification1 = NotificationController.getDefault(context);
        notification1.setSimIndex(index);
        notification1.setSoundId(false);
        notification1.setTitle(R.string.popup_notification_title);
        notification1.setContent(R.string.popup_notification_ticker_title);
        notification1.setSmallIcon(R.drawable.notify);
        notification1.setTickerText(R.string.popup_notification_title);
        notification1.setClass(com.cydroid.softmanager.trafficassistant.TrafficLimitActivity.class);
        notification1.show(notification1.getBootedId());
    }

    private class SlideImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(View view, int position, Object arg2) {
            if (view instanceof ViewPager) {
                ((ViewPager) view).removeView(mPageViews.get(position));
            }
        }

        @Override
        public Object instantiateItem(View view, int position) {
            if (view instanceof ViewPager) {
                ((ViewPager) view).addView(mPageViews.get(position));
            }

            if (!isIntoScreenAnima) {
                boolean isTrafficPackageSetted = mTrafficCalibrateControler.isTafficPackageSetted(mContext,
                        position);
                setViewsVisibility(position);
                setTxt(position, isTrafficPackageSetted);
                setOtherViewText(isTrafficPackageSetted);
            } else {
                resetUI(mContext, position);
            }
            return mPageViews.get(position);
        }
    }

    private void findTabView(int position) {
        Log.d(TAG, "findTabViews = " + position);
        if (mViewInit[position]){
            return;
        }
        mViewInit[position] = true;
        mTextViewUsed[position] = (TextView) mPageViews.get(position).findViewById(R.id.gn_used_flow);
        mTextViewState[position] = (TextView) mPageViews.get(position).findViewById(R.id.gn_flow_state);
        mTextViewStateValue[position] = (TextView) mPageViews.get(position).findViewById(R.id.gn_state_value);
        mTextViewUnitValue[position] = (TextView) mPageViews.get(position).findViewById(R.id.gn_unit_value);
        mImgPrompt[position] = mPageViews.get(position).findViewById(R.id.img_prompt_settings);
        mFlowButton[position] = (CyeeButton) mPageViews.get(position).findViewById(R.id.flow_button);
        mFlowButton[position].setVisibility(View.VISIBLE);
        mFlowButton[position].setOnClickListener(mflowOnClickLister);
        mTextViewStateOtherValue[position] = (TextView) mPageViews.get(position)
                .findViewById(R.id.gn_state_value_other);
        mTextViewUnitOtherValue[position] = (TextView) mPageViews.get(position)
                .findViewById(R.id.gn_unit_value_other);mTrafficBallView[position] = (TrafficaBallView) mPageViews.get(position).findViewById(R.id.traffic_wave);
        mTrafficBallView[position].startWave();
        /*mTrafficView[position] = (TrafficCircleView) mPageViews.get(position).findViewById(R.id.traffic_circle);*/
        chameleonColorProcess(position);
    }

    private class ImagePageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            findTabView(arg0);
            mCurrentSimIndex = arg0;
            boolean isTrafficPackageSetted = mTrafficCalibrateControler.isTafficPackageSetted(mContext, mCurrentSimIndex);
            setOtherViewText(isTrafficPackageSetted);
        }
    }

    private void onIntoScreenAnimation(Context context, int simIndex) {
        boolean isTrafficPackageSetted = mTrafficCalibrateControler.isTafficPackageSetted(context, simIndex);
        setOtherViewText(isTrafficPackageSetted);
        if (!(mSimState[simIndex] && isTrafficPackageSetted)) {
            if (mSimCount != 2) {
                resetUI(context, simIndex);
            }
            return;
        }
        float usedFlow = mTrafficCalibrateControler.getCommonUsedTaffic(context, simIndex);
        int totalFlow = mTrafficCalibrateControler.getCommonTotalTaffic(context, simIndex);
        double percent = usedFlow / totalFlow;
        int end = (int) (percent > 1 ? SWEEP_ANGLE_CONSTANT : percent * SWEEP_ANGLE_CONSTANT);
        new Thread(new TrafficRunnable(STATE_INTO_SCREEN, 0, end)).start();
    }

    private class TrafficRunnable implements Runnable {
        private int mState = 0;
        private int mStart = 0;
        private int mEnd = 0;

        public TrafficRunnable(int state, int start, int end) {
            mState = state;
            mStart = start;
            mEnd = end;
        }

        @Override
        public void run() {
            int duration = 1;
            sleep(duration * 600);
            switch (mState) {
                case STATE_INTO_SCREEN:
                    while (mStart++ < mEnd) {
                        sendMessage(mState, 0, mStart);
                        sleep(getSleepTime(mStart));
                    }
                    sendMessage(mState, 1, mEnd);
                    break;

                default:
                    break;
            }
        }

        private void sleep(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (Exception ex) {
            }
        }

        private int getSleepTime(int value) {
            int duration = 4;
            return duration;
        }

        private void sendMessage(int state, int arg1, int arg2) {
            Message msg = mHandler.obtainMessage(state, arg1, arg2);
            mHandler.sendMessage(msg);
        }

    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STATE_INTO_SCREEN:
                    onUpdateInfo((Integer) msg.arg2, msg.arg1 != 0);
                    break;

                default:
                    break;
            }
        }

    };

    private void onUpdateInfo(int value, boolean isEnd) {
        if (mSimCount == 0) {
            return;
        }

        if (!isEnd) {
            resetIntoScreenAnimaFlag(true);
            updateWave(value, -1, mCurrentSimIndex);
            /*updateCircle(value, -1, mCurrentSimIndex);*/
            updateTrafficInfoTxt(mCurrentSimIndex, value * 1.0 / SWEEP_ANGLE_CONSTANT);
        } else {
            int percent = mTrafficCalibrateControler.getWarnPercent(mContext, mCurrentSimIndex);
            resetIntoScreenAnimaFlag(true);
            updateWave(value, percent, mCurrentSimIndex);
           /* updateCircle(value, percent, mCurrentSimIndex);*/
            resetUI(mContext, mCurrentSimIndex);
        }
    }

    private void updateNotificationBar() {
        TrafficSettingControler trafficSettingControler = TrafficSettingControler.getInstance(mContext);
        trafficSettingControler.commitTrafficNotiAction(mContext);
    }




    // Gionee: mengdw <2015-11-11> modify for CR01589343 end
    // Gionee: mengdw <2016-10-31> add for CR01770072 begin
    private class UtilizationDialogListener implements UtilizationAlertDialog.DialogBtnOnclickListener {
        @Override
        public void btnPositiveClick() {
            Log.d(TAG, "btnPositiveClick");
            //showCalibrateDialog(mContext);
            showTrafficSettingsDialog(mContext);

        }

        @Override
        public void btnNegativeClick() {
            Log.d(TAG, "btnNegativeClick");
            TrafficAssistantMainActivity.this.finish();
        }
    }
    // Gionee: mengdw <2016-10-31> add for CR01770072 end

    private int getViewIndex(int simIndex){
        int viewIndex = simIndex;
        if (mSimCount == 1 && simIndex >= mSimCount){
            viewIndex = 0;
        }
        return viewIndex;
    }
}
