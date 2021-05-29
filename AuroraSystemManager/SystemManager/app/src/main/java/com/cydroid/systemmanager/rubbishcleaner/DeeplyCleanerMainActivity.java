// Gionee: <houjie> <2015-10-27> add for CR01575153 begin
package com.cydroid.systemmanager.rubbishcleaner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
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

import com.cydroid.softmanager.R;
import com.keniu.security.CleanMasterSDK;

import java.util.ArrayList;

import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import com.cydroid.systemmanager.BaseActivity;
import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.rubbishcleaner.common.RubbishInfo;
import com.cydroid.systemmanager.rubbishcleaner.util.StringFilterUtil;
import com.cydroid.systemmanager.ui.UiAnimator;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.UiUtils;
import com.cydroid.systemmanager.utils.Util;

// Gionee: <changph> <2016-11-16> add for M2017 UI CHECK begin
// Gionee: <changph> <2016-11-16> add for M2017 UI CHECK end
//Gionee yubo 2015-08-10 add for CR01535546 begin
//Gionee yubo 2015-08-10 add for CR01535546 end

public class DeeplyCleanerMainActivity extends BaseActivity
        implements ExpandableListView.OnGroupClickListener, OnSelectedListener, RubbishCleanerScanListener,
        RubbishCleanerDelListener {
    private Context mContext;
    private Resources mResources;
    private UiAnimator mUiAnimator;

    private TextView mTotalSizeView;
    private TextView mSizeUnitView;
    private TextView mTotalTextView;
    private TextView mSelectedSizeView;
    private TextView mCleanSelected;
    private TextView mTotalDesc;
    private ImageView mImageView;
    private TextView mCleanEndText;
    private TextView mCleanEndSizeText;

    private CyeeButton mCleanButton;
    private MainCleanAdapter mAdapter;
    private ExpandableListView mExpListView;
    private RelativeLayout mRootView;
    private LinearLayout mDeeplyStickyHeader;
    private LinearLayout mButtonLayout;

    private CyeeAlertDialog mDialog;

    private static boolean DEBUG = true;
    private static String TAG = "CyeeRubbishCleaner/DeeplyCleanerMainActivity";

    private RubbishCleanerDataManager mRubbishCleanerDataManager;

	//Gionee yubo 2015-08-10 add for CR01535546 begin
	private static final boolean gnMSflag = SystemProperties.get("ro.cy.custom").equals("GERMANY_MOBISTEL");
	//Gionee yubo 2015-08-10 add for CR01535546 end
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setElevation(getCyeeActionBar(), 0);
        CleanMasterSDK.getInstance().Initialize(getApplicationContext());
        setContentView(R.layout.rubbish_deeplyclean_activity_main);
        initActionBar();
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
            updateTotalSizeDisplay(mRubbishCleanerDataManager.getTotalSize());
            avoidDisplayNegative();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            mDeeplyStickyHeader.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            mRootView.setBackgroundColor(ChameleonColorManager.getBackgroudColor_B1());
            mButtonLayout.setBackgroundColor(ChameleonColorManager.getBackgroudColor_B1());

            // Gionee: <changph> <2016-11-16> add for M2017 UI CHECK begin
	    int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
	    ColorStateList csl = ColorStateList
                    .valueOf(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            mImageView.setImageTintList(csl);
	    mTotalSizeView.setTextColor(color_T1);
            mSizeUnitView.setTextColor(color_T1);
            mTotalTextView.setTextColor(color_T1);
            mSelectedSizeView.setTextColor(color_T1);
            mCleanSelected.setTextColor(color_T1);
            mTotalDesc.setTextColor(color_T1);
            mCleanEndText.setTextColor(color_T1);
            mCleanEndSizeText.setTextColor(color_T1);
	    // Gionee: <changph> <2016-11-16> add for M2017 UI CHECK end
        }
    }

    private void initActionBar() {
        setSecondClass(RubbishCleanSettingsActivity.class);
        setFirstLayoutVisibility(View.GONE);
        setActionBarEnabled(false);
    }

    private void initFields() {
        mContext = this;
        mResources = getResources();
        mUiAnimator = new UiAnimator();

        mRubbishCleanerDataManager = RubbishCleanerDataManager.getInstance();
        mRubbishCleanerDataManager.recycle();
        mRubbishCleanerDataManager.init(this, MsgConst.DATA_MANAGER_TYPE_DEEPLY, this);
        mRubbishCleanerDataManager.registerRubbishCleanerDelListener(this);

        mTotalSizeView = (TextView) findViewById(R.id.deepclean_total_size);
        mSizeUnitView = (TextView) findViewById(R.id.deepclean_total_unit);
        mTotalTextView = (TextView) findViewById(R.id.deepclean_total);
        mSelectedSizeView = (TextView) findViewById(R.id.deepclean_selected_size);
        mCleanSelected = (TextView) findViewById(R.id.deepcleaned_display);
        mTotalDesc = (TextView) findViewById(R.id.deepclean_total_desc);
        mImageView = (ImageView) findViewById(R.id.deeply_end_flag_image);
        mCleanEndText = (TextView) findViewById(R.id.deeply_clean_end_text);
        mCleanEndSizeText = (TextView) findViewById(R.id.deeply_clean_end_size_text);

        mRootView = (RelativeLayout) findViewById(R.id.root_layout);
        mButtonLayout = (LinearLayout) findViewById(R.id.button_layout);
        mExpListView = (ExpandableListView) findViewById(R.id.deepclean_expandablelist);
        mAdapter = new MainCleanAdapter(mContext, MsgConst.DATA_MANAGER_TYPE_DEEPLY);
        mExpListView.setAdapter(mAdapter);
        mAdapter.setSelectedListener(this);
        mExpListView.setOnGroupClickListener(this);
        mDeeplyStickyHeader = (LinearLayout) findViewById(R.id.deeply_sticky_header);
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

    private void initDisplay() {
        mSelectedSizeView.setVisibility(View.GONE);
        updateTotalSizeDisplay(Long.valueOf(0L)); // size = 0
        initCleanedSize();
        mAdapter.initGroupInfo();
    }

    public void onScanStart() {
        setListViewEnabled(false);
	 setActionBarEnabled(false);
    }

    private class MyListenerAdapter extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            startDeleteRubbish();
        }
    }

    private void execAnimatorBeforeDel() {
        mRootView.removeView(mButtonLayout);
        try {
            int fromHeight = mResources.getDimensionPixelSize(R.dimen.deeply_final_target_height);
            int toHeight = mResources.getDimensionPixelSize(R.dimen.all_screen_height);
            float fromSize = mResources.getDimensionPixelSize(R.dimen.deeply_final_rubbish_textsize);
            float toSize = mResources.getDimensionPixelSize(R.dimen.modify_total_size);
            float fromUnitSize = mResources.getDimensionPixelSize(R.dimen.deeply_final_unit_textsize);
            float toUnitSize = mResources.getDimensionPixelSize(R.dimen.modify_unit_size);
            mUiAnimator.changeLayoutHeight(mDeeplyStickyHeader, fromHeight, toHeight,
                    new MyListenerAdapter());
            mUiAnimator.changeTextSize(mTotalSizeView, fromSize, toSize);
            mUiAnimator.changeTextSize(mSizeUnitView, fromUnitSize, toUnitSize);
            mUiAnimator.changeTextSize(mTotalTextView, fromUnitSize, toUnitSize);
        } catch (NotFoundException e) {
            Log.d(DEBUG, TAG, e.toString());
            finishSelf();
        }
        mCleanSelected.setVisibility(View.VISIBLE);
        mUiAnimator.changeTextAlpha(mCleanSelected, 0, 1);
        mUiAnimator.changeTextAlpha(mSelectedSizeView, 1, 0);
        //mUiAnimator.changeTextAlpha(mTotalDesc, 1, 0);
    }

    private void startDeleteRubbish() {
        new Thread() {
            public void run() {
                if (null != mRubbishCleanerDataManager) {
                    mRubbishCleanerDataManager.startDeleteRubbish(MsgConst.DATA_MANAGER_TYPE_DEEPLY);
                }
            }
        }.start();
    }

    private void prepareDeleteRubbish() {
        setListViewEnabled(false);
        setButtonEnabled(false);
	 setActionBarEnabled(false);
        if (mRubbishCleanerDataManager.isAllNotSelected()) {
            setButtonEnabled(true);
            setListViewEnabled(true);
	     setActionBarEnabled(true);
            return;
        }
        execAnimatorBeforeDel();
    }

    private void updateScanInfoDisplay(Object desc) {
        String scanDesc = (String) desc;
        mSelectedSizeView.setText(scanDesc);
    }

    private void updateTotalSizeDisplay(Object size) {
        String sizeStrWithUnit = Formatter.formatShortFileSize(mContext,
                mRubbishCleanerDataManager.getTotalSize());
        String numStr = StringFilterUtil.getNumStr(sizeStrWithUnit).trim();
        String unitStr = StringFilterUtil.filterAlphabet(sizeStrWithUnit);
		/*guoxt modify for CR01709694 begin*/
		if(numStr.equals("")&& unitStr.equals("") ){
			numStr =sizeStrWithUnit;
		}
		/*guoxt modify for CR01709694 end*/
        Log.d(DEBUG, TAG, "find items, sizeStrWithUnit = " + sizeStrWithUnit + ", numStr = " + numStr);
        mTotalSizeView.setText(" " + numStr + " ");
        mSizeUnitView.setText(unitStr);
    }

    private void updateTotalSizeWhenDel(Object size) {
        String sizeStrWithUnit = Formatter.formatShortFileSize(mContext,
                mRubbishCleanerDataManager.getTotalSize());
        String numStr = StringFilterUtil.getNumStr(sizeStrWithUnit);
        String unitStr = StringFilterUtil.filterAlphabet(sizeStrWithUnit);
		/*guoxt modify for CR01709694 begin*/
		if(numStr.equals("")&& unitStr.equals("") ){
			numStr =sizeStrWithUnit;
		}
		/*guoxt modify for CR01709694 end*/
        Log.d(DEBUG, TAG, "del items, sizeStrWithUnit = " + sizeStrWithUnit + ", numStr = " + numStr);
        mTotalSizeView.setText(" " + numStr);
        mSizeUnitView.setText(unitStr);
    }

    private void updateDeletedSize(Object size) {
        String sizeStr = getResources().getString(R.string.deleted_rubbish_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getCleannedSize()));
        mCleanSelected.setText(sizeWithUnit);
    }

    private void initCleanedSize() {
        mRubbishCleanerDataManager.resetCleannedSize();
        String sizeStr = getResources().getString(R.string.deleted_rubbish_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getCleannedSize()));
        mCleanSelected.setText(sizeWithUnit);
    }

    private void updateAdapterData(int group) {
        if (mAdapter == null || mRubbishCleanerDataManager.isGroupOutOfBound(group)) {
            return;
        }
        ArrayList<RubbishInfo> list = mRubbishCleanerDataManager.getRubbishInfoList(group);
        mAdapter.updateGroupData(group, list);
    }

    private void updateSelectedSizeView() {
        mRubbishCleanerDataManager.calculateSelectedSize();
        String sizeStr = getResources().getString(R.string.selected_items_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getSelectedSize()));
        mSelectedSizeView.setText(sizeWithUnit);
    }

    private void updateScanEndDisplay() {
        mSelectedSizeView.setVisibility(View.VISIBLE);
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
            int toHeight = mResources.getDimensionPixelSize(R.dimen.deeply_final_target_height);
            float fromSize = mResources.getDimensionPixelSize(R.dimen.deeplyclean_rubbish_textsize);
            float toSize = mResources.getDimensionPixelSize(R.dimen.deeply_final_rubbish_textsize);
            float fromUnitSize = mResources.getDimensionPixelSize(R.dimen.deeplyclean_rubbish_unit_textsize);
            float toUnitSize = mResources.getDimensionPixelSize(R.dimen.deeply_final_unit_textsize);
            mUiAnimator.changeLayoutHeight(mDeeplyStickyHeader, fromHeight, toHeight,
                    new ButtonDisplayListener());
            mUiAnimator.changeTextSize(mTotalSizeView, fromSize, toSize);
            mUiAnimator.changeTextSize(mSizeUnitView, fromUnitSize, toUnitSize);
            mUiAnimator.changeTextSize(mTotalTextView, fromUnitSize, toUnitSize);
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

    private void showScannedResults() {
        if (isAllGroupScanFinished()) {
            updateScanEndDisplay();
            avoidDisplayNegative();
            execAnimatorAfterScanFinish();
            setListViewEnabled(true);
            setActionBarEnabled(true);
	    	//Gionee yubo 2015-08-10 add for CR01535546 begin
			if (gnMSflag) {
			    expandBigFileGroup();
			}
			//Gionee yubo 2015-08-10 add for CR01535546 end
			   }
    }
	//Gionee yubo 2015-08-10 add for CR01535546 begin
    private void expandBigFileGroup() {
       // if (mBigfileList.size() > 0
              //  && !mExpListView.isGroupExpanded(DeeplyCleanTypeConst.BIGFILE)) {
           // mExpListView.expandGroup(DeeplyCleanTypeConst.BIGFILE);
      //  }
    }
    //Gionee yubo 2015-08-10 add for CR01535546 end

    // TODO: 避免显示负数，暂时处理，待复现产生条件后修改
    private void avoidDisplayNegative() {
        CharSequence displaySize = mTotalSizeView.getText();
        String sizeStr = displaySize.toString();
        Log.d(DEBUG, TAG, "avoidDisplayNegative, displaySize = " + displaySize + ", sizeStr = "
                + sizeStr);
        try {
            float fSize = Float.parseFloat(sizeStr);
            if (fSize < 0) {
                Log.d(DEBUG, TAG, "avoidDisplayNegative, total size display negative, fSize = " + fSize);
                long size = mRubbishCleanerDataManager.calAllGroupsSize();
                mTotalSizeView.setText(Formatter.formatShortFileSize(mContext, size));
                mSizeUnitView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.d(DEBUG, TAG, "avoidDisplayNegative, throws exception");
            return;
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
            long id) {
        if (mRubbishCleanerDataManager.getRubbishInfoListSize(groupPosition) > 0) {
            Intent intent = new Intent(this, RubbishCleanerDetailActivity.class);
            intent.putExtra(MsgConst.RUBBISH_DETIAL_EXTRA_CLEAN_TYPE, MsgConst.DATA_MANAGER_TYPE_DEEPLY);
            intent.putExtra(MsgConst.RUBBISH_DETIAL_EXTRA_RUBBISH_TYPE, groupPosition);
            startActivityForResult(intent, 0);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1){
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
        setButtonText(R.string.quickly_clean_rubbish);
        LayoutTransition layoutTrans = new LayoutTransition();
        layoutTrans.setDuration(200);
        ObjectAnimator anim = ObjectAnimator.ofFloat(mCleanButton, "TranslationY", 150, 0);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(100);
        layoutTrans.setAnimator(LayoutTransition.APPEARING, anim);
        mButtonLayout.setLayoutTransition(layoutTrans);
        mButtonLayout.addView(mCleanButton, params);
        mCleanButton.setOnClickListener(new MyClickListener());
    }

    private class MyClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (mRubbishCleanerDataManager.isAllNotSelected()) {
                return;
            }
            ConfirmDialog confirmDialog = new ConfirmDialog();
            mDialog = confirmDialog.createDialog();
        }
    }

    private void setButtonText(int resId) {
        mCleanButton.setText(resId);
    }

    private void execDeleteFinishAnim() {
        try {
            CyeeButton button = new CyeeButton(this);
            button.setBackgroundResource(R.drawable.button_bg_selector_material);
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
            button.setTextColor(Color.WHITE);
	    // Gionee: <changph> <2016-11-16> add for M2017 UI CHECK begin
	    if (ChameleonColorManager.isNeedChangeColor()) {
                int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
		button.setTextColor(color_T1);
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

    private void finishSelf() {
        finish();
    }

    private void setButtonEnabled(boolean enabled) {
        if (mCleanButton != null) {
            mCleanButton.setEnabled(enabled);
        }
    }

    private void reversalTextAnim() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.rubbish_text_layout);
        ObjectAnimator anim = ObjectAnimator.ofFloat(layout, "RotationY", 0, -90);
        anim.setDuration(200);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mRubbishCleanerDataManager != null) {
                    mCleanEndText.setVisibility(View.VISIBLE);
                    mCleanEndSizeText.setVisibility(View.VISIBLE);
                    setCleanedSizeText();
                    mUiAnimator.changeTextAlpha(mCleanEndText, 0, 1);
                    mUiAnimator.changeTextAlpha(mCleanEndSizeText, 0, 1);
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
                execDeleteFinishAnim();
            }
        });
        anim.start();
    }
    
    private void setCleanedSizeText() {
        String sizeStr = mResources.getString(R.string.clean_end_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getSelectedSize()));
        mCleanEndSizeText.setText(sizeWithUnit);
    }

    public void onScanItem(int group,Object obj) {
        updateScanInfoDisplay(obj);
    }

    public void onFindItem(int group,Object obj) {
        updateTotalSizeDisplay(obj);
    }

    public void onScanEnd(int group) {
        updateAdapterData(group);
        showScannedResults();
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

    private class ConfirmDialog {
        public ConfirmDialog() {
        }

        public CyeeAlertDialog createDialog() {
            CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mContext);
            builder.setTitle(R.string.rubbish_clean_detail_confirm_dialog_title);            
            builder.setMessage(R.string.rubbish_clean_detail_confirm_dialog_msg);
            builder.setPositiveButton(R.string.go_clean, listener);
            builder.setNegativeButton(R.string.go_cancel, listener);
            return builder.show();
        }

        private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CyeeAlertDialog.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                    case CyeeAlertDialog.BUTTON_POSITIVE:
                        prepareDeleteRubbish();
                        break;
                }
            }
        };
    }
}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end