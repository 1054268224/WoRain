/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-10-27
 */
package com.cydroid.systemmanager.rubbishcleaner;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeListView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

// Gionee: <changph> <2016-11-16> add for M2017 UI CHECK begin
import android.content.res.ColorStateList;
// Gionee: <changph> <2016-11-16> add for M2017 UI CHECK end

import com.cydroid.softmanager.R;
import com.cydroid.systemmanager.BaseActivity;

import com.cydroid.systemmanager.RuntimePermissionsManager;
import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.rubbishcleaner.common.RubbishInfo;
import com.cydroid.systemmanager.rubbishcleaner.util.StringFilterUtil;
import com.cydroid.systemmanager.ui.UiAnimator;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.UiUtils;
import com.cydroid.systemmanager.utils.Util;
import com.keniu.security.CleanMasterSDK;

import java.io.File;
import java.util.ArrayList;

public class RubbishCleanerMainActivity extends BaseActivity
        implements ExpandableListView.OnGroupClickListener, OnSelectedListener, RubbishCleanerScanListener,
        RubbishCleanerDelListener {
    private Context mContext;
    private Resources mResources;
    private UiAnimator mUiAnimator;

    private TextView mTotalSizeView;
    private TextView mUnitTextView;
    private TextView mTotalDescTextView;
    private TextView mAdviceTextView;
    private TextView mSelectedSizeView;
    private RelativeLayout mCmSupportLayout;
    private TextView mCmSupportText;
    private TextView mCleannedDisplay;
    private TextView mCleanEndText;
    private TextView mCleanEndSizeText;
    private TextView mCleanEndAdviceText;
    private CyeeListView mAdviceList;
    private ImageView mImageView;
    private CyeeButton mCleanButton;
    private MainCleanAdapter mAdapter;
    private ExpandableListView mExpListView;
    private RelativeLayout mRootView;
    private RelativeLayout mAdviceContainer;
    private LinearLayout mButtonParentLayout;
    private LinearLayout mTopPartWindowLayout;
    private RelativeLayout mRubbishTextLayout;
    private LinearLayout mStickyLayout;
    private View mDivider;

    private RubbishCleanAdviceAdapter mAdviceAdapter;

    private static boolean DEBUG = true;
    private static String TAG = "CyeeRubbishCleaner/RubbishCleanerMainActivity";

    private RubbishCleanerDataManager mRubbishCleanerDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee: <houjie> <2015-12-26> add for CR01614703 begin
        UiUtils.setElevation(getCyeeActionBar(), 0);
        if (redirectToPermissionCheckIfNeeded()) {
            return;
        }
        // Gionee: <houjie> <2015-12-26> add for CR01614703 end
        CleanMasterSDK.getInstance().Initialize(getApplicationContext());
        setContentView(R.layout.rubbish_activity_main);
        customActionBar();
        initFields();
        initDisplay();

        mRubbishCleanerDataManager.bindKSCleanerService(this);
        chameleonColorProcess();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isAllGroupScanFinished()) {
            for (int i = 0; i < mAdapter.getGroupCount(); ++i) {
                updateAdapterData(i);
            }            
            updateSelectedSizeView();
           /*guoxt modify for CR01747206 begin*/
            if (mRubbishCleanerDataManager != null) {
                updateTotalSizeDisplay(mRubbishCleanerDataManager.getTotalSize());
            }
          /*guoxt modify for CR01747206 begin*/

            avoidDisplayNegative();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRubbishCleanerDataManager != null) {
            mRubbishCleanerDataManager.unregisterRubbishCleanerScanListener(this);
            mRubbishCleanerDataManager.unregisterRubbishCleanerDelListener(this);
            mRubbishCleanerDataManager.unBindKSCleanerService(this);
            mRubbishCleanerDataManager = null;
        }
        //chenyee zhaocaili 20180508 add for CSW1707A-845 begin
        releasRes();
        //chenyee zhaocaili 20180508 add for CSW1707A-845 end
    }

    //chenyee zhaocaili 20180508 add for CSW1707A-845 begin
    private void releasRes() {
        Util.unbindDrawables(findViewById(R.id.root_layout));
    }
    //chenyee zhaocaili 20180508 add for CSW1707A-845 end

    private void customActionBar() {
        setFirstLayoutVisibility(View.VISIBLE);
        setFirstClass(DeeplyCleanerMainActivity.class);
        setSecondClass(RubbishCleanSettingsActivity.class);
        setActionBarEnabled(false);
    }

    private void initFields() {
        mContext = this;
        mResources = getResources();
        mUiAnimator = new UiAnimator();

        mRubbishCleanerDataManager = RubbishCleanerDataManager.getInstance();
        mRubbishCleanerDataManager.recycle();
        mRubbishCleanerDataManager.init(this, MsgConst.DATA_MANAGER_TYPE_NORMAL, this);
        mRubbishCleanerDataManager.registerRubbishCleanerDelListener(this);

        mTotalSizeView = (TextView) findViewById(R.id.total_rubbish_size);
        mUnitTextView = (TextView) findViewById(R.id.total_rubbish_unit);
        mTotalDescTextView = (TextView) findViewById(R.id.total_rubbish_desc);
        mAdviceTextView = (TextView) findViewById(R.id.rubbishinfo_description);
        mSelectedSizeView = (TextView) findViewById(R.id.total_select_size);
        mRubbishTextLayout = (RelativeLayout) findViewById(R.id.rubbish_text_layout);
        mCmSupportLayout = (RelativeLayout) findViewById(R.id.cm_supply_layout);
        mCmSupportText = (TextView) findViewById(R.id.cm_supply);
        mCleannedDisplay = (TextView) findViewById(R.id.cleanned_display);
        mImageView = (ImageView) findViewById(R.id.end_flag_image);
        mCleanEndText = (TextView) findViewById(R.id.clean_end_text);
        mCleanEndSizeText = (TextView) findViewById(R.id.clean_end_size_text);
        mCleanEndAdviceText = (TextView) findViewById(R.id.clean_end_advice_title);
        mAdviceList = (CyeeListView) findViewById(R.id.rubbish_clean_advice_list);
        mRootView = (RelativeLayout) findViewById(R.id.root_layout);
        mAdviceContainer = (RelativeLayout) findViewById(R.id.rubbishinfo_description_container);
        mTopPartWindowLayout = (LinearLayout) findViewById(R.id.top_part_window);
        mButtonParentLayout = (LinearLayout) findViewById(R.id.button_layout);
        mStickyLayout = (LinearLayout) findViewById(R.id.sticky_content);
        mExpListView = (ExpandableListView) findViewById(R.id.expandablelist);
        mDivider = (View) findViewById(R.id.header_divider);
        mAdapter = new MainCleanAdapter(mContext, MsgConst.DATA_MANAGER_TYPE_NORMAL);
        mExpListView.setAdapter(mAdapter);
        mAdapter.setSelectedListener(this);
        mExpListView.setOnGroupClickListener(this);
        mAdviceAdapter = new RubbishCleanAdviceAdapter(mContext);
        mAdviceList.setAdapter(mAdviceAdapter);
    }

    private void chameleonColorProcess() {
        boolean shouldChange = ChameleonColorManager.isNeedChangeColor();
        if (shouldChange) {
            mTopPartWindowLayout.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            mRootView.setBackgroundColor(ChameleonColorManager.getBackgroudColor_B1());
            mButtonParentLayout.setBackgroundColor(ChameleonColorManager.getBackgroudColor_B1());
            // Gionee: <changph> <2016-11-16> add for M2017 UI CHECK begin
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            ColorStateList csl = ColorStateList
                      .valueOf(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            mImageView.setImageTintList(csl);
            mTotalSizeView.setTextColor(color_T1);
            mUnitTextView.setTextColor(color_T1);
            mTotalDescTextView.setTextColor(color_T1);
            mAdviceTextView.setTextColor(color_T1);
            mSelectedSizeView.setTextColor(color_T1);
            mCmSupportText.setTextColor(color_T1);
            mCleannedDisplay.setTextColor(color_T1);
            mCleanEndText.setTextColor(color_T1);
            mCleanEndSizeText.setTextColor(color_T1);
            mCleanEndAdviceText.setTextColor(color_T1);
            // Gionee: <changph> <2016-11-16> add for M2017 UI CHECK end
        }
    }

    private void initDisplay() {
        mAdviceTextView.setText(R.string.scan_state);
        updateTotalSizeDisplay(Long.valueOf(0L));
        initCleannedSize();
        mAdapter.initGroupInfo();
    }

    private class MyListenerAdapter extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            startDeleteRubbish();
        }
    }

    private void execAnimatorBeforeDel() {
        mStickyLayout.setVisibility(View.GONE);
        mRootView.removeView(mButtonParentLayout);
        mCmSupportLayout.setVisibility(View.GONE);
        mCmSupportText.setVisibility(View.GONE);
        mSelectedSizeView.setVisibility(View.GONE);
        mAdviceContainer.setVisibility(View.GONE);
        mRubbishTextLayout.setVisibility(View.GONE);
        try {
            int fromHeight = mResources.getDimensionPixelSize(R.dimen.final_target_height);
            int toHeight = mResources.getDimensionPixelSize(R.dimen.final_target_height);
            float fromSize = mResources.getDimension(R.dimen.final_rubbish_textsize);
            float toSize = mResources.getDimension(R.dimen.modify_total_size);
            float fromUnitSize = mResources.getDimensionPixelSize(R.dimen.final_unit_textsize);
            float toUnitSize = mResources.getDimensionPixelSize(R.dimen.modify_unit_size);
            mUiAnimator.changeLayoutHeight(mTopPartWindowLayout, fromHeight, toHeight, 0,
                    new MyListenerAdapter());
            mUiAnimator.changeTextSize(mTotalSizeView, fromSize, toSize);
            mUiAnimator.changeTextSize(mUnitTextView, fromUnitSize, toUnitSize);
            mUiAnimator.changeTextSize(mTotalDescTextView, fromUnitSize, toUnitSize);
        } catch (NotFoundException e) {
            Log.d(DEBUG, TAG, "execAnimatorBeforeDel throw NotFoundException");
            finishSelf();
        }
        mCleannedDisplay.setVisibility(View.VISIBLE);
        mUiAnimator.changeTextAlpha(mCleannedDisplay, 0, 1);
        mUiAnimator.changeTextAlpha(mSelectedSizeView, 1, 0);
        mUiAnimator.changeTextAlpha(mAdviceTextView, 1, 0);
    }

    private void startDeleteRubbish() {
        new Thread() {
            public void run() {
                if (null != mRubbishCleanerDataManager) {
                    mRubbishCleanerDataManager.startDeleteRubbish(MsgConst.DATA_MANAGER_TYPE_NORMAL);
                }
            }
        }.start();
    }

    private void prepareDeleteRubbish() {
        setListViewEnabled(false);
        setButtonEnabled(false);
        setActionBarEnabled(false);
        /*guoxt modify for CSW1705A-741 begin*/
        if(mRubbishCleanerDataManager == null){
            return;
        }/*guoxt modify for CSW1705A-741 end*/
        if (mRubbishCleanerDataManager.isAllNotSelected()) {
            setButtonEnabled(true);
            setListViewEnabled(true);
            setActionBarEnabled(true);
            return;
        }
        execAnimatorBeforeDel();
    }

    private void showDetailsWhenScanning(Object desc) {
        String scanDesc = (String) desc;
        // Gionee <xionghg> <2017-05-09> add for 129667 begin
        if (gnODMflag && (scanDesc.contains("gionee") || scanDesc.contains("amigo"))) {
            return;
        }
        // Gionee <xionghg> <2017-05-09> add for 129667 end
        mSelectedSizeView.setText(scanDesc);
    }

    private void updateTotalSizeDisplay(Object size) {
        String sizeStrWithUnit = Formatter.formatShortFileSize(mContext,
                mRubbishCleanerDataManager.getTotalSize());
        String numStr = StringFilterUtil.getNumStr(sizeStrWithUnit).trim();
        String unitStr = StringFilterUtil.filterAlphabet(sizeStrWithUnit);
        /*guoxt modify for CR01709694 begin*/
        if(numStr.equals("")&& unitStr.equals("") ){
            unitStr = sizeStrWithUnit;
        }
        /*guoxt modify for CR01709694 end*/
        mTotalSizeView.setText(" " + numStr);
        mUnitTextView.setText(unitStr);	
    }

    private void updateTotalSizeWhenDel(Object size) {
        String sizeStrWithUnit = Formatter.formatShortFileSize(mContext,
                mRubbishCleanerDataManager.getTotalSize());
        String numStr = StringFilterUtil.getNumStr(sizeStrWithUnit);
        String unitStr = StringFilterUtil.filterAlphabet(sizeStrWithUnit);
        /*guoxt modify for CR01709694 begin*/
        if(numStr.equals("")&& unitStr.equals("") ){
            unitStr = sizeStrWithUnit;
        }
        /*guoxt modify for CR01709694 end*/
        mTotalSizeView.setText(" " + numStr);
        mUnitTextView.setText(unitStr);
    }

    private void updateDeletedSize(Object size) {
        String sizeStr = mResources.getString(R.string.deleted_rubbish_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getCleannedSize()));
        mCleannedDisplay.setText(sizeWithUnit);
    }

    private void initCleannedSize() {
        mRubbishCleanerDataManager.resetCleannedSize();
        String sizeStr = mResources.getString(R.string.deleted_rubbish_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getCleannedSize()));
        mCleannedDisplay.setText(sizeWithUnit);
    }

    private void updateAdapterData(int group) {
        /*guoxt modify for CR01747206 begin*/
        if (mAdapter == null || mRubbishCleanerDataManager == null) {
            return;
        }
        if(mRubbishCleanerDataManager.isGroupOutOfBound(group)){
            return;
        }
        /*guoxt modify for CR01747206 end*/
        ArrayList<RubbishInfo> list = mRubbishCleanerDataManager.getRubbishInfoList(group);
        mAdapter.updateGroupData(group, list);
    }

    private void updateSelectedSizeView() {
         /*guoxt modify for CR01747206 begin*/
        if( mRubbishCleanerDataManager == null){
            return;
        }
         /*guoxt modify for CR01747206 begin*/
        mRubbishCleanerDataManager.calculateSelectedSize();
        String sizeStr = mResources.getString(R.string.selected_items_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getSelectedSize()));
        mSelectedSizeView.setText(sizeWithUnit);
    }

    private void updateScanEndDisplay() {
        // mAdviceTextView.setText(R.string.clean_advice);
        mAdviceContainer.setVisibility(View.GONE);
        updateSelectedSizeView();
    }

    private boolean isAllGroupScanFinished() {
        if (mAdapter == null) {
            return false;
        }
        return mAdapter.isAllGroupScanFinish();
    }

    private void execAnimatorAfterScanFinish() {
        try {
            int fromHeight = mResources.getDimensionPixelSize(R.dimen.top_half_height);
            int toHeight = mResources.getDimensionPixelSize(R.dimen.final_target_height);
            float fromSize = mResources.getDimensionPixelSize(R.dimen.total_rubbish_textsize);
            float toSize = mResources.getDimensionPixelSize(R.dimen.final_rubbish_textsize);
            float fromUnitSize = mResources.getDimensionPixelSize(R.dimen.total_rubbish_unit_textsize);
            float toUnitSize = mResources.getDimensionPixelSize(R.dimen.final_unit_textsize);
            mUiAnimator.changeLayoutHeight(mTopPartWindowLayout, fromHeight, toHeight,
                    new ButtonDisplayListener());
            mUiAnimator.changeTextSize(mTotalSizeView, fromSize, toSize);
            mUiAnimator.changeTextSize(mUnitTextView, fromUnitSize, toUnitSize);
            mUiAnimator.changeTextSize(mTotalDescTextView, fromUnitSize, toUnitSize);
        } catch (NotFoundException e) {
            Log.d(DEBUG, TAG, e.toString());
            finishSelf();
        }
    }

    private class ButtonDisplayListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            addCleanButton();
        }
    }

    private void setListViewEnabled(boolean isEnabled) {
        mExpListView.setEnabled(isEnabled);
    }

    private void showScanndResults() {
        if (isAllGroupScanFinished()) {
            updateScanEndDisplay();
            avoidDisplayNegative();
            execAnimatorAfterScanFinish();
            setListViewEnabled(true);
            setActionBarEnabled(true);
        }
    }

    // TODO: 避免显示负数，暂时处理，待复现产生条件后修改
    private void avoidDisplayNegative() {
        CharSequence displaySize = mTotalSizeView.getText();
        String sizeStr = displaySize.toString();
        Log.d(DEBUG, TAG, "avoidDisplayNegative, displaySize = " + displaySize + ", sizeStr = " + sizeStr);
        try {
            float fSize = Float.parseFloat(sizeStr);
            if (fSize < 0) {
                Log.d(DEBUG, TAG, "avoidDisplayNegative, total size display negative, fSize = " + fSize);
                long size = mRubbishCleanerDataManager.calAllGroupsSize();
                mTotalSizeView.setText(Formatter.formatShortFileSize(mContext, size));
                mUnitTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "avoidDisplayNegative, throws exception");
            return;
        }
    }

    /* when open delete apk autolly, database maybe is not sync, so finish self */
    private void finishSelf() {
        finish();
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        /*guoxt modify for SW17W16A-1654 begin*/
        if(null == mRubbishCleanerDataManager || mRubbishCleanerDataManager.isGroupOutOfBound(groupPosition)){
            return true;
        }
        /*guoxt modify for SW17W16A-1654 end*/

        // Gionee: <changph> <2016-08-30> add for CR01752510 begin
        if (mRubbishCleanerDataManager.getRubbishInfoListSize(groupPosition) > 0) {
        // Gionee: <changph> <2016-08-30> add for CR01752510 end
            Intent intent = new Intent(this, RubbishCleanerDetailActivity.class);
            intent.putExtra(MsgConst.RUBBISH_DETIAL_EXTRA_CLEAN_TYPE, MsgConst.DATA_MANAGER_TYPE_NORMAL);
            intent.putExtra(MsgConst.RUBBISH_DETIAL_EXTRA_RUBBISH_TYPE, groupPosition);
            startActivityForResult(intent, 0);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            finish();
        }
    }

    @Override
    public void updateSelectedSize() {
        updateSelectedSizeView();
    }

    @Override
    public boolean isUsingCustomActionBar() {
        return true;
    }

    @Override
    public void finishTopActivity() {
        finish();
        if (mRubbishCleanerDataManager != null) {
            mRubbishCleanerDataManager.unBindKSCleanerService(this);
            mRubbishCleanerDataManager.recycle();
            mRubbishCleanerDataManager = null;
        }
    }

    private void addCleanButton() {
        mCleanButton = new CyeeButton(this);
        int height = mResources.getDimensionPixelSize(R.dimen.cleanbutton_height);
        int bottom = mResources.getDimensionPixelSize(R.dimen.cleanbutton_marginbottom);
        int left = mResources.getDimensionPixelSize(R.dimen.cleanbutton_marginleft);
        int right = mResources.getDimensionPixelSize(R.dimen.cleanbutton_marginright);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height);
        params.bottomMargin = bottom;
        params.leftMargin = left;
        params.rightMargin = right;
        mCleanButton.setText(R.string.quickly_clean_rubbish);
        LayoutTransition layoutTrans = new LayoutTransition();
        layoutTrans.setDuration(200);
        ObjectAnimator addViewAnim = ObjectAnimator.ofFloat(mCleanButton, "TranslationY", 150, 0);
        addViewAnim.setInterpolator(new LinearInterpolator());
        addViewAnim.setDuration(100);
        layoutTrans.setAnimator(LayoutTransition.APPEARING, addViewAnim);
        mButtonParentLayout.setLayoutTransition(layoutTrans);
        mButtonParentLayout.addView(mCleanButton, params);
        mCleanButton.setOnClickListener(new MyClickListener());
    }

    private class MyClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            prepareDeleteRubbish();
        }
    }

    private void execDeleteFinishAnim() {
        try {
            CyeeButton button = new CyeeButton(this);
            //button.setBackgroundResource(R.drawable.button_bg_selector_material);
            int height = mResources.getDimensionPixelSize(R.dimen.cleanbutton_height);
            int bottom = mResources.getDimensionPixelSize(R.dimen.cleanbutton_marginbottom);
            int left = mResources.getDimensionPixelSize(R.dimen.cleanbutton_marginleft);
            int right = mResources.getDimensionPixelSize(R.dimen.cleanbutton_marginright);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, height);
            params.bottomMargin = bottom;
            params.leftMargin = left;
            params.rightMargin = right;
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            button.setText(R.string.clean_end);
            button.setTextColor(Color.BLACK);
            // Gionee: <changph> <2016-11-16> add for M2017 UI CHECK begin
            if (ChameleonColorManager.isNeedChangeColor()) {
                int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
                int color_B4 = ChameleonColorManager.getButtonBackgroudColor_B4();
                button.setTextColor(color_T1);
                button.setBackgroundColorFilter(color_B4);
            }
            // Gionee: <changph> <2016-11-16> add for M2017 UI CHECK end
            float textSize = mResources.getDimensionPixelSize(R.dimen.rubbish_cleaned_button_textsize);
            button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishSelf();
                }
            });
            LayoutTransition layoutTrans = new LayoutTransition();
            ObjectAnimator anim = ObjectAnimator.ofFloat(button, "TranslationY", 200, 0);
            layoutTrans.setAnimator(LayoutTransition.APPEARING, anim);
            mRootView.setLayoutTransition(layoutTrans);
            mRootView.addView(button, params);
        } catch (NotFoundException e) {
            Log.d(DEBUG, TAG, e.toString());
            finishSelf();
        }
    }

    private void setButtonEnabled(boolean enabled) {
        if (mCleanButton != null) {
            mCleanButton.setEnabled(enabled);
        }
    }

    private void reversalTextAnim() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mRubbishTextLayout, "RotationY", 0, -90);
        anim.setDuration(200);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mRubbishCleanerDataManager != null) {
                    reversalImageAnim();
                }
            }
        });
        anim.start();
    }

    private void reversalImageAnim() {
        mImageView.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(mImageView, "RotationY", -90, -180);
        anim.setDuration(200);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCleanEndText.setVisibility(View.INVISIBLE);
                // Modify by HZH on 2019/5/11 for  start ^_^
                mCleanEndSizeText.setVisibility(View.INVISIBLE);
                // Modify by HZH on 2019/5/11 for  end ^_^
                mCleanEndAdviceText.setVisibility(View.VISIBLE);
                mAdviceList.setVisibility(View.VISIBLE);
                mDivider.setVisibility(View.VISIBLE);
                setCleanedSizeText();
                mUiAnimator.changeTextAlpha(mCleanEndText, 0, 1);
                mUiAnimator.changeTextAlpha(mCleanEndSizeText, 0, 1);
                mUiAnimator.changeTextAlpha(mCleanEndAdviceText, 0, 1);
                mUiAnimator.changeTextAlpha(mAdviceList, 0, 1);
                mUiAnimator.changeTextAlpha(mDivider, 0, 1);
                execDeleteFinishAnim();
            }
        });
        anim.start();
    }

    private void setCleanedSizeText() {
        // Gionee <yangxinruo> <2016-1-26> add for CR01631907 begin
        if (mRubbishCleanerDataManager == null) {
            Log.d(DEBUG, TAG, "WARNING! setCleanedSizeText called after activity destoryed?");
            return;
        }
        // Gionee <yangxinruo> <2016-1-26> add for CR01631907 end
        String sizeStr = mResources.getString(R.string.clean_end_size_text);
        float freeSize = getAvailableExternalStorageSize(mContext);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getSelectedSize()), freeSize);
        mCleanEndSizeText.setText(sizeWithUnit);
    }

    public void onScanStart() {
        setListViewEnabled(false);
        setActionBarEnabled(false);
    }

    public void onScanItem(int group,Object obj) {
        showDetailsWhenScanning(obj);
    }

    public void onFindItem(int group,Object obj) {
        updateTotalSizeDisplay(obj);
    }

    public void onScanEnd(int group) {
        updateAdapterData(group);
        showScanndResults();
    }

    public void onDelItem(Object obj) {
        updateTotalSizeWhenDel(obj);
        updateDeletedSize(obj);
    }

    public void onDelItemByDialog(Object obj) {
        updateTotalSizeWhenDel(obj);
        avoidDisplayNegative();
        updateSelectedSizeView();
    }

    public void onDelEnd() {
        setListViewEnabled(true);
        setActionBarEnabled(true);
        reversalTextAnim();
    }

    // Gionee: <houjie> <2015-12-26> add for CR01614703 begin
    private boolean redirectToPermissionCheckIfNeeded() {
        if (RuntimePermissionsManager.isBuildSysNeedRequiredPermissions() 
                && RuntimePermissionsManager.hasNeedRequiredPermissions(this)) {
            RuntimePermissionsManager.redirectToPermissionCheck(this);
            finish();
            return true;
        }
        return false;
    }
    // Gionee: <houjie> <2015-12-26> add for CR01614703 end

    public float getAvailableExternalStorageSize(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        long available = availableBlocksLong * blockSizeLong;
        long all = getExternalStorageSize(context);
        return (available * 1.0f) / all * 100;
    }

    public long getExternalStorageSize(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        return blockCountLong * blockSizeLong;
    }
}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end
