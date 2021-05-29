// Gionee: <houjie> <2015-10-27> add for CR01575153 begin
package com.cydroid.systemmanager.rubbishcleaner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.cydroid.softmanager.R;
import com.cydroid.systemmanager.rubbishcleaner.RubbishCleanerDataManager.GroupKeyInfo;

import java.io.File;
import java.util.ArrayList;

import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeExpandableListView;
import com.cydroid.systemmanager.BaseActivity;
import com.cydroid.systemmanager.rubbishcleaner.common.CleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.DeeplyCleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.rubbishcleaner.common.RubbishInfo;
import com.cydroid.systemmanager.rubbishcleaner.util.StringFilterUtil;
import com.cydroid.systemmanager.ui.UiAnimator;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.UiUtils;

// Gionee: <changph> <2016-11-16> add for M2017 UI CHECK begin
// Gionee: <changph> <2016-11-16> add for M2017 UI CHECK end
//Gionee <GN_Oveasea_Bug> <guoxt> <20161213> for #41948 begin
//Gionee <GN_Oveasea_Bug> <guoxt> <20161213> for #41948 end

public class RubbishCleanerDetailActivity extends BaseActivity
        implements CyeeExpandableListView.OnChildClickListener, CyeeExpandableListView.OnGroupClickListener,
        OnSelectedListener, RubbishCleanerDelListener {
    private static final String TAG = "RubbishCleanerDetailActivity";

    private static final boolean[] NORMAL_CLEAN_IS_EXPANDABLE_LIST = {true, false, false, false};
    private static final boolean[] DEEPLY_CLEAN_IS_EXPANDABLE_LIST = {true, false, true};
    private static final boolean[][] IS_EXPANDABLE_LIST = {NORMAL_CLEAN_IS_EXPANDABLE_LIST,
            DEEPLY_CLEAN_IS_EXPANDABLE_LIST};
    private static final int[] TITLE_INDEXS = {R.array.group_name_arrays, R.array.deeplyclean_group_arrays};
    private static final int[] HEADER_FIRST_INDEXS = {R.string.rubbish_clean_detail_head_first_normal,
            R.string.rubbish_clean_detail_head_first_deeply};
    private static final int[] HEADER_SECOND_INDEXS = {R.string.rubbish_clean_detail_head_second_normal,
            R.string.rubbish_clean_detail_head_second_deeply};

    private static final int[] MSGS_INDEXS = {R.array.rubbish_clean_normal_dialog_str,
            R.array.rubbish_clean_deeply_dialog_str};

    private Context mContext;
    private Resources mResources;
    private UiAnimator mUiAnimator;
    private int mCleanResult = 0;

    // Chenyee xionghg 20180312 modify for CSW1705A-741 begin
    private int mCleanType;
    private int mRubbishType;
    // Chenyee xionghg 20180312 modify for CSW1705A-741 end

    private TextView mTotalSizeView;
    private TextView mUnitTextView;
    private TextView mCleannedDisplay;
    private TextView mHeaderTextView;
    private TextView mHeaderSumary;
    private TextView mCleanEndText;
    private TextView mCleanEndSizeText;
    private ImageView mImageView;
    private RelativeLayout mRootView;
    private LinearLayout mTopPartWindowLayout;
    private LinearLayout mButtonParentLayout;
    private CyeeButton mCleanButton;
    private CyeeExpandableListView mExpListView;
    private CyeeAlertDialog mDialog;
    private DetailCleanExpandableListAdapter mExpandableAdapter;
    private DetailCleanListAdapter mAdapter;

    private RubbishCleanerDataManager mRubbishCleanerDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        // CleanMasterSDK.getInstance().Initialize(this);
        setContentView(R.layout.rubbish_activity_detail);

        parseIntent();
        initRubbishCleanerDataManager();
        if (dataInvalideFinish()) {
            Log.d(TAG, "onCreate Data invalide");
            return ;
        }

        initFields();
        initDisplay();
        chameleonColorProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (dataInvalideFinish()) {
            Log.d(TAG, "onResume Data invalide");
            return;
        }

        updateHeader();
        if (IS_EXPANDABLE_LIST[mCleanType][mRubbishType]) {
            ArrayList<GroupKeyInfo> groupKeys = mRubbishCleanerDataManager
                    .getExpandableGroupKeyList(mCleanType, mRubbishType);
            mExpandableAdapter.initGroupInfo(groupKeys);

            for (int i = 0; i < groupKeys.size(); ++i) {
                mExpandableAdapter.updateGroupData(i, mRubbishCleanerDataManager
                        .getExpandableGroupData(mCleanType, mRubbishType, groupKeys.get(i).key));
            }
        } else {
            ArrayList<RubbishInfo> groupData = mRubbishCleanerDataManager.getGroupData(mCleanType,
                    mRubbishType);
            mAdapter.initGroupInfo(groupData);
        }
    }

    private boolean dataInvalideFinish() {
        if (mRubbishCleanerDataManager.isGroupOutOfBound(mRubbishType)) {
            Log.d(TAG, "dataInvalideFinish Data invalide: " + mRubbishType);
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        collapseGroup();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if (mRubbishCleanerDataManager != null) {
            mRubbishCleanerDataManager.unregisterRubbishCleanerDelListener(this);
            mRubbishCleanerDataManager = null;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(mCleanResult);
        finish();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        mCleanType = intent.getIntExtra(MsgConst.RUBBISH_DETIAL_EXTRA_CLEAN_TYPE, -1);
        mRubbishType = intent.getIntExtra(MsgConst.RUBBISH_DETIAL_EXTRA_RUBBISH_TYPE, -1);
        Log.d(TAG, "parseIntent mCleanType:" + mCleanType + ", mRubbishType:" + mRubbishType);

        if (mCleanType == -1 || mRubbishType == -1) {
            throw new RuntimeException("don't have mCleanType or mRubbishType.");
        }
    }

    private void initRubbishCleanerDataManager() {
        mRubbishCleanerDataManager = RubbishCleanerDataManager.getInstance();
        mRubbishCleanerDataManager.registerRubbishCleanerDelListener(this);
    }

    private void initFields() {
        mContext = this;
        mResources = getResources();
        mUiAnimator = new UiAnimator();

        mTotalSizeView = (TextView) findViewById(R.id.total_rubbish_size);
        mUnitTextView = (TextView) findViewById(R.id.total_rubbish_unit);
        mCleannedDisplay = (TextView) findViewById(R.id.cleanned_display);
        mImageView = (ImageView) findViewById(R.id.end_flag_image);
        mCleanEndText = (TextView) findViewById(R.id.clean_end_text);
        mCleanEndSizeText = (TextView) findViewById(R.id.clean_end_size_text);

        mRootView = (RelativeLayout) findViewById(R.id.root_layout);
        mTopPartWindowLayout = (LinearLayout) findViewById(R.id.top_part_window);
        mHeaderTextView = (TextView) findViewById(R.id.header_title);
        mHeaderSumary = (TextView) findViewById(R.id.header_summary);
        mButtonParentLayout = (LinearLayout) findViewById(R.id.button_layout);
        mExpListView = (CyeeExpandableListView) findViewById(R.id.expandablelist);

        if (IS_EXPANDABLE_LIST[mCleanType][mRubbishType]) {
            mExpandableAdapter = new DetailCleanExpandableListAdapter(mContext, mCleanType, mRubbishType);
            mExpListView.setAdapter(mExpandableAdapter);
            mExpandableAdapter.setSelectedListener(this);
            mExpListView.setOnChildClickListener(this);
        } else {
            mAdapter = new DetailCleanListAdapter(mContext, mCleanType, mRubbishType);
            mExpListView.setIndicatorBounds(0, 0);
            mExpListView.setAdapter(mAdapter);
            mAdapter.setSelectedListener(this);
            mExpListView.setOnGroupClickListener(this);
        }
    }

    private void initDisplay() {
        setCurTitle();
        addCleanButton();
        setActionBarBackClickListener();
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
	    mTotalSizeView.setTextColor(color_T1);
            mUnitTextView.setTextColor(color_T1);
            mCleannedDisplay.setTextColor(color_T1);
            //fengpeipei modify for 57416 start
            //mHeaderTextView.setTextColor(color_T1);
            //mHeaderSumary.setTextColor(color_T1);
            //fengpeipei modify for 57416 end
            mCleanEndText.setTextColor(color_T1);
            mCleanEndSizeText.setTextColor(color_T1);
	    // Gionee: <changph> <2016-11-16> add for M2017 UI CHECK end
        }
    }

    private void setCurTitle() {
        String[] titles = mResources.getStringArray(TITLE_INDEXS[mCleanType]);
        setTitle(titles[mRubbishType]);
    }

    private void updateHeader() {
        String firstHeadFormat = mResources.getString(HEADER_FIRST_INDEXS[mCleanType]);
        String firstHeadStr = String.format(firstHeadFormat,
                mRubbishCleanerDataManager.getRubbishInfoListSize(mRubbishType));
        mHeaderTextView.setText(firstHeadStr);

        String secondHeadFormat = mResources.getString(HEADER_SECOND_INDEXS[mCleanType]);
        String secondHeadStr = String.format(secondHeadFormat, Formatter.formatShortFileSize(mContext,
                mRubbishCleanerDataManager.calGroupSelectedSize(mRubbishType)));
        mHeaderSumary.setText(secondHeadStr);
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
        mButtonParentLayout.addView(mCleanButton, params);
        mCleanButton.setOnClickListener(new MyClickListener());
    }

    private void setActionBarBackClickListener() {
        getCyeeActionBar().setOnBackClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setButtonEnabled(boolean enabled) {
        if (mCleanButton != null) {
            mCleanButton.setEnabled(enabled);
        }
    }

    private void collapseGroup() {
        if (mExpandableAdapter == null || mExpListView == null) {
            return;
        }
        int groupCnt = mExpandableAdapter.getGroupCount();
        for (int i = 0; i < groupCnt; i++) {
            mExpListView.collapseGroup(i);
        }
    }

    private void setListViewEnabled(boolean isEnabled) {
        mExpListView.setEnabled(isEnabled);
    }

    private void prepareDeleteRubbish() {
        setListViewEnabled(false);
        setButtonEnabled(false);
	 setActionBarEnabled(false);
        if (mRubbishCleanerDataManager.isAllNotSelectedInGroup(mRubbishType)) {
            setButtonEnabled(true);
            setListViewEnabled(true);
	     setActionBarEnabled(true);
            return;
        }
        mCleanResult = 1;
        execAnimatorBeforeDel();
    }

    private void execAnimatorBeforeDel() {
        mRootView.removeView(mButtonParentLayout);
        mExpListView.setVisibility(View.GONE);
        mHeaderTextView.setVisibility(View.GONE);
        mHeaderSumary.setVisibility(View.GONE);
        updateDeletedSize(0);
        try {
            int toHeight = mResources.getDimensionPixelSize(R.dimen.all_screen_height);
            float fromSize = mResources.getDimension(R.dimen.final_rubbish_textsize);
            float toSize = mResources.getDimension(R.dimen.modify_total_size);
            float fromUnitSize = mResources.getDimensionPixelSize(R.dimen.final_unit_textsize);
            float toUnitSize = mResources.getDimensionPixelSize(R.dimen.modify_unit_size);
            mUiAnimator.changeLayoutHeight(mTopPartWindowLayout, 0, toHeight, new MyListenerAdapter());
            mUiAnimator.changeTextSize(mTotalSizeView, fromSize, toSize);
            mUiAnimator.changeTextSize(mUnitTextView, fromUnitSize, toUnitSize);
        } catch (Exception e) {
            Log.d(TAG, "execAnimatorBeforeDel throw NotFoundException");
            setResult(1);
            finish();
        }
        mTopPartWindowLayout.setVisibility(View.VISIBLE);
        mCleannedDisplay.setVisibility(View.VISIBLE);
        mUiAnimator.changeTextAlpha(mCleannedDisplay, 0, 1);
    }

    private void startDeleteRubbish() {
        new Thread() {
            public void run() {
                mRubbishCleanerDataManager.startDeleteRubbishByType(mCleanType, mRubbishType);
            }
        }.start();
    }

    private void updateTotalSizeWhenDel(Object size) {
        String sizeStrWithUnit = Formatter.formatShortFileSize(mContext,
                mRubbishCleanerDataManager.getTotalSize());
        String numStr = StringFilterUtil.getNumStr(sizeStrWithUnit);
        String unitStr = StringFilterUtil.filterAlphabet(sizeStrWithUnit);
        mTotalSizeView.setText(numStr);
        mUnitTextView.setText(unitStr);
    }

    private void updateDeletedSize(Object size) {
        String sizeStr = mResources.getString(R.string.deleted_rubbish_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getCleannedSize()));
        mCleannedDisplay.setText(sizeWithUnit);
    }

    private void reversalTextAnim() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.rubbish_text_layout);
        ObjectAnimator anim = ObjectAnimator.ofFloat(layout, "RotationY", 0, -90);
        anim.setDuration(200);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reversalImageAnim();
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
                mCleanEndText.setVisibility(View.VISIBLE);
                mCleanEndSizeText.setVisibility(View.VISIBLE);
                setCleanedSizeText();
                mUiAnimator.changeTextAlpha(mCleanEndText, 0, 1);
                mUiAnimator.changeTextAlpha(mCleanEndSizeText, 0, 1);
                execDeleteFinishAnim();
            }
        });
        anim.start();
    }

    private void setCleanedSizeText() {
        // Gionee <yangxinruo> <2016-1-26> add for CR01631907 begin
        if (mRubbishCleanerDataManager == null) {
            Log.d(TAG, "WARNING! setCleanedSizeText called after activity destoryed?");
            return;
        }
        // Gionee <yangxinruo> <2016-1-26> add for CR01631907 end
        String sizeStr = mResources.getString(R.string.clean_end_size);
        String sizeWithUnit = String.format(sizeStr,
                Formatter.formatShortFileSize(mContext, mRubbishCleanerDataManager.getCleannedSize()));
        mCleanEndSizeText.setText(sizeWithUnit);
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
                    setResult(1);
                    finish();
                }
            });
            LayoutTransition layoutTrans = new LayoutTransition();
            ObjectAnimator anim = ObjectAnimator.ofFloat(button, "TranslationY", 200, 0);
            layoutTrans.setAnimator(LayoutTransition.APPEARING, anim);
            mRootView.setLayoutTransition(layoutTrans);
            mRootView.addView(button, params);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            setResult(1);
            finish();
        }
    }

    @Override
    public boolean onGroupClick(CyeeExpandableListView parent, View v, int groupPosition, long id) {
        if (mDialog != null && mDialog.isShowing()) {
            return true;
        }
        RubbishInfo rInfo = (RubbishInfo) mAdapter.getGroup(groupPosition);
        IntentDialog dialog = new IntentDialog(groupPosition, -1, rInfo);
        mDialog = dialog.createDialog();
        return true;
    }

    @Override
    public boolean onChildClick(CyeeExpandableListView parent, View v, int groupPosition, int childPosition,
            long id) {
        if (mDialog != null && mDialog.isShowing()) {
            return true;
        }
        RubbishInfo rInfo = (RubbishInfo) mExpandableAdapter.getChild(groupPosition, childPosition);
        IntentDialog dialog = new IntentDialog(groupPosition, childPosition, rInfo);
        mDialog = dialog.createDialog();
        return true;
    }

    @Override
    public void updateSelectedSize() {
        updateHeader();
    }

    public void onDelItem(Object obj) {
        updateTotalSizeWhenDel(obj);
        updateDeletedSize(obj);
    }

    public void onDelItemByDialog(Object obj) {
        updateHeader();
    }

    public void onDelEnd() {
        mCleanResult = 1;
        setListViewEnabled(true);
	 setActionBarEnabled(true);
        reversalTextAnim();
    }

    private class IntentDialog {
        private int groupPos;
        private int childPos;
        private RubbishInfo rInfo;

        public IntentDialog(int groupPosition, int childPositon, RubbishInfo info) {
            groupPos = groupPosition;
            childPos = childPositon;
            rInfo = info;
        }

        public CyeeAlertDialog createDialog() {
            CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mContext);
            builder.setTitle(rInfo.desc);
            builder.setMessage(createMsg());
            builder.setPositiveButton(R.string.go_clean, listener);
            builder.setNegativeButton(R.string.go_cancel, listener);
            //add by zhaopeng for TEWBW-1904 20200715 start
            /*if (mRubbishType == CleanTypeConst.APK && rInfo.isInstalled == false) {
                builder.setNeutralButton(R.string.go_install_apk, listener);
            } */
            //add by zhaopeng for TEWBW-1904 20200715 end
            //delete bug:EJQQQ-1301 zhaopeng 20200527 start
            /*else {
                builder.setNeutralButton(R.string.rubbish_clean_detail_view_file, listener);
            }*/
            //delete bug:EJQQQ-1301 zhaopeng 20200527 end

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
                        if (mCleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL) {
                            mRubbishCleanerDataManager.deleteFileReally(rInfo.path);
                            updateAdapterList();
                        } else {
                            ConfirmDialog confirmDialog = new ConfirmDialog(groupPos, childPos, rInfo);
                            mDialog = confirmDialog.createDialog();
                            dialog.dismiss();
                        }
                        break;
                    case CyeeAlertDialog.BUTTON_NEUTRAL:
                        if (mCleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL
                                && mRubbishType == CleanTypeConst.APK && rInfo.isInstalled == false) {
                            gotoInstallApk(rInfo.path);
                        } else {
                            viewFile();
                            dialog.dismiss();
                        }
                        break;
                }
            }
        };

        private void updateAdapterList() {
            mRubbishCleanerDataManager.removeRubbishInfo(mCleanType, mRubbishType, groupPos, rInfo);
            if (mRubbishType == CleanTypeConst.APK) {
                mRubbishCleanerDataManager.syncLocalDatabase(mRubbishCleanerDataManager.APK_QUERY_TOKEN,
                        rInfo.db_id);
            }

            if (IS_EXPANDABLE_LIST[mCleanType][mRubbishType]) {
                ArrayList<GroupKeyInfo> groupKeys = mRubbishCleanerDataManager
                        .getExpandableGroupKeyList(mCleanType, mRubbishType);
                boolean isGroupChanged = (groupKeys.size() != mExpandableAdapter.getGroupCount());

                if (isGroupChanged) {
                    mExpandableAdapter.initGroupInfo(groupKeys);
                    if (groupKeys.isEmpty()) {
                        mExpandableAdapter.updateEmptyGroupData();
                    } else {
                        for (int i = 0; i < groupKeys.size(); ++i) {
                            mExpandableAdapter.updateGroupData(i, mRubbishCleanerDataManager
                                    .getExpandableGroupData(mCleanType, mRubbishType, groupKeys.get(i).key));
                        }
                    }
                } else {
                    mExpandableAdapter.updateGroupData(groupPos, mRubbishCleanerDataManager
                            .getExpandableGroupData(mCleanType, mRubbishType, groupKeys.get(groupPos).key));
                }
            } else {
                ArrayList<RubbishInfo> groupData = mRubbishCleanerDataManager.getGroupData(mCleanType,
                        mRubbishType);
                mAdapter.initGroupInfo(groupData);
            }
            mRubbishCleanerDataManager.delItemByDialog(Long.valueOf(rInfo.size));
        }

        private void gotoInstallApk(String path) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File newFile = new File(path);
            Uri contentUri = FileProvider.getUriForFile(mContext, "com.cydroid.systemmanager.rubbishcleaner.fileprovider", newFile);

            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            try {
                mContext.startActivity(intent);
                setResult(1);
                finish();
            } catch (ActivityNotFoundException e) {
                return;
            }
        }

        private void viewFile() {
            Intent intent = getGoToFileManagerIntent(rInfo.path);
            boolean isAllowed = intent.resolveActivity(mContext.getPackageManager()) != null;
            if (!isAllowed) {
                Toast.makeText(mContext, R.string.rubbish_clean_detail_cannot_find_fileexplorer,
                        Toast.LENGTH_LONG).show();
            } else {
                mContext.startActivity(intent);
            }
        }

        private String createMsg() {
            String[] msgs = null;
            try {
                msgs = mContext.getResources().getStringArray(MSGS_INDEXS[mCleanType]);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            String msg = "";

            if (mCleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY
                    && mRubbishType == DeeplyCleanTypeConst.CACHE) {
                msg = String.format(msgs[mRubbishType], rInfo.descx);
            } else if ((mCleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY
                    && mRubbishType == DeeplyCleanTypeConst.BIGFILE)
                    || (mCleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL
                            && mRubbishType == CleanTypeConst.APK)) {
                msg = String.format(msgs[mRubbishType], Formatter.formatShortFileSize(mContext, rInfo.size));
            } else {
                msg = String.format(msgs[mRubbishType], rInfo.name, rInfo.desc);
            }
            return msg;
        }

        private Intent getGoToFileManagerIntent(String path) {
            Intent intent = new Intent();
            intent.setPackage("com.cydroid.filemanager");
            File file = new File(path);
				
            if (file.isFile()) {
                int index = path.lastIndexOf("/");
                String realpath = path.subSequence(0, index).toString();
                file = new File(realpath);
            }
            intent.setData(Uri.fromFile(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.d(TAG, "getGoToFileManagerIntent intent:" + intent);
            return intent;
        }

    }

    private class ConfirmDialog {
        private int groupPos;
        private int childPos;
        private RubbishInfo rInfo;
        private boolean isCleanButton = false;

        public ConfirmDialog(int groupPosition, int childPositon, RubbishInfo info) {
            groupPos = groupPosition;
            childPos = childPositon;
            rInfo = info;
        }

        public ConfirmDialog() {
            isCleanButton = true;
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
					    // Gionee: <changph> <2016-10-21> add begin
                        UiUtils.setElevation(getCyeeActionBar(), 0);
						// Gionee: <changph> <2016-10-21> add end
                        if (isCleanButton) {
                            prepareDeleteRubbish();
                        } else {
                            mRubbishCleanerDataManager.deleteFileReally(rInfo.path);
                            updateAdapterList();
                        }
                        break;
                }
            }
        };

        private void updateAdapterList() {
            mRubbishCleanerDataManager.removeRubbishInfo(mCleanType, mRubbishType, groupPos, rInfo);
            if (mRubbishType == DeeplyCleanTypeConst.BIGFILE) {
                mRubbishCleanerDataManager.syncLocalDatabase(mRubbishCleanerDataManager.BIGFILE_QUERY_TOKEN,
                        rInfo.db_id);
            }

            if (IS_EXPANDABLE_LIST[mCleanType][mRubbishType]) {
                ArrayList<GroupKeyInfo> groupKeys = mRubbishCleanerDataManager
                        .getExpandableGroupKeyList(mCleanType, mRubbishType);
                boolean isGroupChanged = (groupKeys.size() != mExpandableAdapter.getGroupCount());

                if (isGroupChanged) {
                    mExpandableAdapter.initGroupInfo(groupKeys);
                    if (groupKeys.isEmpty()) {
                        mExpandableAdapter.updateEmptyGroupData();
                    } else {
                        for (int i = 0; i < groupKeys.size(); ++i) {
                            mExpandableAdapter.updateGroupData(i, mRubbishCleanerDataManager
                                    .getExpandableGroupData(mCleanType, mRubbishType, groupKeys.get(i).key));
                        }
                    }
                } else {
                    mExpandableAdapter.updateGroupData(groupPos, mRubbishCleanerDataManager
                            .getExpandableGroupData(mCleanType, mRubbishType, groupKeys.get(groupPos).key));
                }
            } else {
                ArrayList<RubbishInfo> groupData = mRubbishCleanerDataManager.getGroupData(mCleanType,
                        mRubbishType);
                mAdapter.initGroupInfo(groupData);
            }
            mRubbishCleanerDataManager.delItemByDialog(Long.valueOf(rInfo.size));
        }
    }

    private class MyClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if ((mDialog != null && mDialog.isShowing()) || 
                    mRubbishCleanerDataManager.isAllNotSelectedInGroup(mRubbishType)) {
                return;
            }

            ConfirmDialog confirmDialog = new ConfirmDialog();
            mDialog = confirmDialog.createDialog();
            // prepareDeleteRubbish();
        }
    }

    private class MyListenerAdapter extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            startDeleteRubbish();
        }
    }
}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end
