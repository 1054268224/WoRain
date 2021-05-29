package com.cydroid.softmanager.softmanager;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeListView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.applock.AppLockManagerActivity;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.adapter.FrontPageAdapter;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppManager;
import com.cydroid.softmanager.softmanager.model.SDCardInfo;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.view.SoftManagerCircleView;
import com.cydroid.systemmanager.BaseWheatekActivity;
import com.wheatek.proxy.ui.HostSoftManagerMainActivity;
//import com.cydroid.softmanager.youju.YouJuManager;
import android.os.SystemProperties;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * File Description:
 *
 * @author: Gionee-lihq
 * @see: 2013-5-3 Change List:
 * @modify:Gionee-Daizm 2013-08-20
 */
public class SoftManagerActivity extends BaseWheatekActivity implements OnItemClickListener, SoftHelperUtils.StorageInfoUpdateCallback {
    private static final String TAG = "SoftManagerActivity";
    private static final String OTG_PATH = "/storage/usbotg";
    private final static int PHOHE_USAGE = 7;
    private final static int SDCARD_USAGE = 8;
    private final static int STORAGE_INFO = 9;
    private SoftManagerCircleView mCircleView;
    private RelativeLayout mStorageLayout, mStorageinfoView, mNoneSdcardView;
    private LinearLayout mHaveSdcardView;
    private RelativeLayout.LayoutParams mLayoutParams;
    private TextView mInternalStorageTitle, mSdcardStorageTitle, mInternalStorageInfo, mSdcardStorageinfo,
            mSingleInternalStorageTitle, mSingleInternalStorageInfo, mNo_storage_text, mSingleProgress,
            mSingleProgressTitle, mSingleProgressSign;
    private ImageView mPhoneImg, mSDcardImg, mDoublePhomeImgIndicator, mDoubleSDcardImgIndicator,
            mSinglePhomeImgIndicator;
    private View mVerticalLine;
    private StorageManager mStorageManager;
    private final int FREE_STORAGE = 0;
    private final int USED_STORAGE = 1;
    private Resources mRes;
    private CyeeListView mListView;
    private FrontPageAdapter mAdapter;
    private final List<ItemInfo> mData = new ArrayList<ItemInfo>();
    private final List<Intent> mIntents = new ArrayList<Intent>();
    private String mInternalstorage, mSdcardstorage, mEnableString, mUsedString;
    private SoftHelperUtils mHelperUtils;
    private boolean isFirstIn = true;
    //Gionee <GN_Oversea_Bug> <fengpeipei> <20161123> for applock begin
    private final boolean mIsAppLockSupport = SystemProperties.get("ro.cy.applock.support", "no").equals("yes");
    //Gionee <GN_Oversea_Bug> <fengpeipei> <20161123> for applock end
    private final ScannerReceiver mBroadcastReceiver = new ScannerReceiver();

    private static final String[] sClickCount_Youju = new String[]{"APP_Boot", "APP_Uninstall", "APP_Freeze",
            "APP_Lock"};

    public static final int sSDK = Build.VERSION.SDK_INT;
    public static final int ANDROIDM_API_LEVEL = 23;
    // Add by HZH on 2019/8/2 for  start
    private TextView mDoubleProgress;
    private String mInternalProgress;
    private String mSdProgress;
    // Add by HZH on 2019/8/2 for  end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*UiUtils.setElevation(getCyeeActionBar(), 0);*/
        ChameleonColorManager.getInstance().onCreate(this);
        setContentView(R.layout.softmanager_main_activity_layout);
        /*setActionBarCustomView();*/
        initView();
        initData();
        initIntents();
        registerUSBReceiver();
        chameleonColorProcess();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void chameleonColorProcess() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            mStorageLayout.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            mInternalStorageTitle.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mSdcardStorageTitle.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mInternalStorageInfo.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mSdcardStorageinfo.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());

            mSingleInternalStorageTitle
                    .setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mSingleInternalStorageInfo
                    .setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mSingleProgress.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mSingleProgressTitle.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mSingleProgressSign.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());

            mCircleView.setOuterCircleColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());

            // Gionee <houjie> <2015-11-13> add for CR01565278 begin
            mPhoneImg.getDrawable().setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mSDcardImg.getDrawable().setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mDoublePhomeImgIndicator.getBackground()
                    .setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mDoubleSDcardImgIndicator.getBackground()
                    .setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            mSinglePhomeImgIndicator.getBackground()
                    .setTint(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
            // Gionee <houjie> <2015-11-13> add for CR01565278 end
        }
    }

    @Override
    protected void onResume() {
        mListView.setOnItemClickListener(this);
        mHelperUtils = new SoftHelperUtils(this);
        mHelperUtils.init(this);
        SoftHelperUtils.initStorageState(SoftManagerActivity.this, mStorageManager);
        mHelperUtils.setCallback(this);
        updateUSBCapacity();
        updateListView();
        if (mData != null) {
            updatatext0(mData.get(0));
        }
        super.onResume();
        //YouJuAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        //Gionee <GN_Oversea_Bug> <guoxt> <20161206> for 37554 begin
        mHelperUtils.cleanup();
        //Gionee <GN_Oversea_Bug> <guoxt> <20161206> for 37554 begin
        super.onPause();
        //YouJuAgent.onPause(this);
    }

    // Gionee <liuyb> <2014-3-3> add for CR01078882 begin
    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        releasRes();
        if (mData != null) {
            mData.clear();
        }
        if (mIntents != null) {
            mIntents.clear();
        }
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    private void releasRes() {
        Util.unbindDrawables(findViewById(R.id.root));
    }
    // Gionee <liuyb> <2014-3-3> add for CR01078882 end

    private void updateUSBCapacity() {
        boolean sdCardReady = SoftHelperUtils.isSdcardMounted(mStorageManager);
        if (sdCardReady) {
            Log.v(TAG, "sd card is ready");
            initVisibility();
            setVisibled(true);
            mNo_storage_text.setVisibility(View.GONE);
            refreshCategoryInfo();
        } else {
            Log.v(TAG, "sd card is not ready");
            setVisibled(false);
            mCircleView.setVisibility(View.GONE);
            mNo_storage_text.setVisibility(View.VISIBLE);
        }
    }

    /**
     *
     * @param visibled
     */
    private void setVisibled(boolean visibled) {
        mStorageinfoView.removeAllViews();
        if (visibled) {
            if (SoftHelperUtils.getStorageMountedCount() >= 2) {
                initHaveSdcardView();
            } else {
                initNoneSdcardView();
            }
        } else {
            initVisibility();
        }
    }

    private void updateListView() {
        mAdapter.notifyDataSetChanged();
    }

    private void refreshCategoryInfo() {
        Log.v(TAG, "refreshCategoryInfo");
        if (SoftHelperUtils.getStorageCount() >= 2 || sSDK >= ANDROIDM_API_LEVEL) {
            SDCardInfo sdCardInfo = null;
            if (SoftHelperUtils.getStorageMountedCount() >= 2) {
                if (mHelperUtils.getExternalStorageInfo().mTotal > 0) {
                    setVisibled(true);
                    setStorageInfo();
                } else {
                    if (isOtgMode()) {
                        setOtgStorageInfoPath();
                    } else {
                        setStorageInfoPath();
                    }
                }
            } else if (SoftHelperUtils.getStorageMountedCount() == 1) {
                // Modify by HZH on 2019/8/5 for EJWJ-398 start
                if (mHelperUtils.getInternalStorageInfo().mTotal > 0) {
                    setUSBInfo(mHelperUtils.getInternalStorageInfo(),
                            SoftHelperUtils.getStorageMountedCount());
                } else {
                    mHelperUtils.setStorageInfoPath(false, SoftHelperUtils.getSDCardPath());
                }
                setVisibled(true);
                controlNoSdcardVisibility();
                mCircleView.updateInnerCircleBgAlpha(0);
                mCircleView.updateViews();
                // Modify by HZH on 2019/8/5 for EJWJ-398 end
            }
        }
    }

    private void setStorageInfoPath() {
        List<StorageVolume> storageVolumes = new ArrayList<StorageVolume>();
        int externalStorageId = -1;

        String[] pathList = mStorageManager.getVolumePaths();
        StorageVolume[] storageVolume = mStorageManager.getVolumeList();
        int len = storageVolume.length;

        for (int i = 0; i < len; i++) {
            if (!mStorageManager.getVolumeState(pathList[i]).equals("not_present")) {
                storageVolumes.add(storageVolume[i]);
            }
        }

        for (int i = 0; i < storageVolumes.size(); i++) {
            if (SoftHelperUtils.isExternalStorage(this, storageVolumes.get(i))) {
                externalStorageId = i;
                break;
            }
        }

        /*guoxt modify for CR01522753 begin*/
        mHelperUtils.setStorageInfoPath(false, SoftHelperUtils.getSDCardPath());
        mHelperUtils.setStorageInfoPath(true, SoftHelperUtils.getSDCard2Path());
        /*
        if (externalStorageId != -1) {
		    mHelperUtils.setStorageInfoPath(false, HelperUtils.getSDCardPath());
		    mHelperUtils.setStorageInfoPath(true, HelperUtils.getSDCard2Path());
        } else {
		    mHelperUtils.setStorageInfoPath(true, HelperUtils.getSDCardPath());
		    mHelperUtils.setStorageInfoPath(false, HelperUtils.getSDCard2Path());
        }
        */
        /*guoxt modify for CR01522753  end*/

    }

    private void setOtgStorageInfoPath() {
        mHelperUtils.setStorageInfoPath(false, SoftHelperUtils.getSDCardPath());
        mHelperUtils.setStorageInfoPath(true, SoftHelperUtils.getSDCard2Path());
    }

    private void setStorageInfo() {
        setSdcardInfo(mHelperUtils.getExternalStorageInfo());
        setUSBInfo(mHelperUtils.getInternalStorageInfo(), SoftHelperUtils.getStorageMountedCount());
        controlHaveSdcardVisibility();
        mCircleView.updateInnerCircleBgAlpha(51);
        mCircleView.updateViews();
    }

    private void setOtgStorageInfo() {
        setSdcardInfo(mHelperUtils.getInternalStorageInfo());
        setUSBInfo(mHelperUtils.getExternalStorageInfo(), SoftHelperUtils.getStorageMountedCount());
        mCircleView.updateViews();
    }

    private boolean isOtgMode() {
        return OTG_PATH.equals(SoftHelperUtils.getSDCard2Path());
    }

    //Chenyee guoxt modify for CSW1703AC-75 begin
    private void setUSBInfo(SDCardInfo sdCardInfo, int count) {
        if (sdCardInfo != null) {
            int progress = (int) ((float) (sdCardInfo.mTotal - sdCardInfo.mFree) / sdCardInfo.mTotal * 100);
            // Add by HZH on 2019/8/2 for EJWJ-398 start
            mInternalProgress = String.valueOf(progress);
            // Add by HZH on 2019/8/2 for EJWJ-398 end
            if (count == 1) {
                // Modify by HZH on 2019/7/2 for  start
//                setInternalTextView(convertStorage(sdCardInfo.mTotal - sdCardInfo.mFree), convertStorage(sdCardInfo.mTotal));
                setInternalTextView(SoftHelperUtils.unit1000(sdCardInfo.mUsed), SoftHelperUtils.unit1000(sdCardInfo.mTotal));
                // Modify by HZH on 2019/7/2 for  end
                mSingleProgress.setText(String.valueOf(progress));
                changeVisibility(false);
            } else {
                // Modify by HZH on 2019/7/2 for  start
//                setDoubleInternalTextView(convertStorage(sdCardInfo.mTotal - sdCardInfo.mFree), convertStorage(sdCardInfo.mTotal));
                setDoubleInternalTextView(SoftHelperUtils.unit1000(sdCardInfo.mUsed), SoftHelperUtils.unit1000(sdCardInfo.mTotal));
                // Modify by HZH on 2019/7/2 for  end
                changeVisibility(true);
            }

            if (isFirstIn) {
                mSingleProgress.setText("0");
                new Thread(new StorageRunnable(PHOHE_USAGE, 0, progress)).start();
                isFirstIn = false;
            } else {
                mCircleView.updateViewByPhoneUsage((int) (progress * 360 * 0.01));
            }
        }
    }
    //Chenyee guoxt modify for CSW1703AC-75 end

    //Chenyee guoxt modify for CSW1703AC-75 begin
    public String convertStorage(long size) {
        /*guoxt modify for SW17W16A-2741 begin*/
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        /*guoxt modify for SW17W16A-2741 end*/

        if (size >= gb) {
            return String.format("%.1f" +
                    getResources().getString(R.string.GB), (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f" + getResources().getString(R.string.MB) : "%.1f" + getResources().getString(R.string.MB), f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f" + getResources().getString(R.string.KB) : "%.1f" + getResources().getString(R.string.KB), f);
        } else {
            return String.format("%d" + getResources().getString(R.string.B), size);
        }
    }
    //Chenyee guoxt modify for CSW1703AC-75 end

    private void setSdcardInfo(SDCardInfo sdCardInfo) {
        if (sdCardInfo != null) {
            changeVisibility(true);
            // Modify by HZH on 2019/7/2 for  start
//            setSdcardTextView(convertStorage(sdCardInfo.mTotal - sdCardInfo.mFree), convertStorage(sdCardInfo.mTotal));
//            int progress = (int) ((float) (sdCardInfo.mTotal - sdCardInfo.mFree) / sdCardInfo.mTotal * 100);
            setSdcardTextView(SoftHelperUtils.unit1000(sdCardInfo.mUsed), SoftHelperUtils.unit1000(sdCardInfo.mTotal));
            int progress = (int) ((float) sdCardInfo.mUsed / sdCardInfo.mTotal * 100);
            mSdProgress = String.valueOf(progress);
            // Modify by HZH on 2019/7/2 for  end

            if (isFirstIn) {
                new Thread(new StorageRunnable(SDCARD_USAGE, 0, progress)).start();
            } else {
                mCircleView.updateViewBySDUsage((int) (progress * 360 * 0.01));
            }
        }
    }

    private void changeVisibility(boolean visible) {
        if (visible) {
            mCircleView.updateInnerCircleAlpha(255);
        } else {
            mCircleView.updateInnerCircleAlpha(0);
        }
    }

    private void setSdcardTextView(String used, String total) {
        String tmpString = used + "/" + total;
        // mSdcardStorageTitle.setText(mRes.getString(R.string.phone_outer_sd_storage));
        mSdcardStorageinfo.setText(tmpString);
    }

    private void setInternalTextView(String used, String total) {
        String tmpString = used + "/" + total;
        // mSingleInternalStorageTitle.setText(mRes.getString(R.string.single_phone_inner_storage));
        mSingleInternalStorageInfo.setText(tmpString);
    }

    private void setDoubleInternalTextView(String used, String total) {
        String tmpString = used + "/" + total;
        // mInternalStorageTitle.setText(mRes.getString(R.string.phone_inner_storage));
        mInternalStorageInfo.setText(tmpString);
    }

    private void initView() {
        mRes = getResources();
        // CyeeActionBar actionbar = getCyeeActionBar();
        // actionbar.setBackgroundDrawable(mRes.getDrawable(R.drawable.common_blue_bg));

        mStorageLayout = (RelativeLayout) findViewById(R.id.storage_info);
        mCircleView = (SoftManagerCircleView) findViewById(R.id.soft_circle);
        mStorageinfoView = (RelativeLayout) findViewById(R.id.storage_usage_info);
        mNo_storage_text = (TextView) findViewById(R.id.no_storage_text);
        mPhoneImg = (ImageView) findViewById(R.id.phone_img);
        mSDcardImg = (ImageView) findViewById(R.id.sd_img);
        mSingleProgressTitle = (TextView) findViewById(R.id.single_progress_title);
        mSingleProgress = (TextView) findViewById(R.id.single_progress);
        mSingleProgressSign = (TextView) findViewById(R.id.single_progress_sign);
        // Add by HZH on 2019/8/2 for EJWJ-398 start
        mDoubleProgress = (TextView) findViewById(R.id.double_progress);
        // Add by HZH on 2019/8/2 for EJWJ-398 end

        mHaveSdcardView = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.softmanager_have_sdcard_layout, null);
        mInternalStorageTitle = (TextView) mHaveSdcardView.findViewById(R.id.phone_storage_title);
        mInternalStorageInfo = (TextView) mHaveSdcardView.findViewById(R.id.phone_usage_info);
        mSdcardStorageTitle = (TextView) mHaveSdcardView.findViewById(R.id.sd_storage_title);
        mSdcardStorageinfo = (TextView) mHaveSdcardView.findViewById(R.id.sd_usage_info);
        mVerticalLine = (View) mHaveSdcardView.findViewById(R.id.vertical_line);
        mDoublePhomeImgIndicator = (ImageView) mHaveSdcardView.findViewById(R.id.phone_img_indicator);
        mDoubleSDcardImgIndicator = (ImageView) mHaveSdcardView.findViewById(R.id.sd_img_indicator);

        mNoneSdcardView = (RelativeLayout) LayoutInflater.from(this)
                .inflate(R.layout.softmanager_none_sdcard_layout, null);
        mSingleInternalStorageTitle = (TextView) mNoneSdcardView
                .findViewById(R.id.single_internal_storage_title);
        mSingleInternalStorageInfo = (TextView) mNoneSdcardView.findViewById(R.id.single_internal_usage_info);
        mSinglePhomeImgIndicator = (ImageView) mNoneSdcardView
                .findViewById(R.id.single_internal_img_indicator);

        mLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        initVisibility();

        mListView = (CyeeListView) findViewById(R.id.listview);
        mListView.setStretchEnable(false);
        mListView.setOnItemClickListener(this);
        mAdapter = new FrontPageAdapter(this, mData);
        mListView.setAdapter(mAdapter);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
    }

    private void initData() {
        String[] titles = mRes.getStringArray(R.array.listitem_soft_manager_activity_p);

        int[] resIds = new int[]{R.drawable.svg_icon_auto_start, R.drawable.svg_icon_authority_management,
                R.drawable.svg_icon_soft_freeze, R.drawable.icon_soft_applock, R.drawable.svg_icon_soft_uninstall};
        //Gionee <GN_Oversea_Bug> <fengpeipei> <20161123> for applock begin
        if (!mIsAppLockSupport) {
            String[] titlestmp = new String[titles.length - 1];
            System.arraycopy(titles, 0, titlestmp, 0, titles.length - 2);
            titlestmp[titlestmp.length - 1] = titles[titles.length - 1];
            titles = titlestmp;
            resIds = new int[]{R.drawable.svg_icon_auto_start, R.drawable.svg_icon_authority_management,
                    R.drawable.svg_icon_soft_freeze, R.drawable.svg_icon_soft_uninstall};
        }
        //Chenyee guoxt modify for CSW1703CX-919 begin
        if (Consts.cyCXFlag && mIsAppLockSupport) {
            String[] titlestmp = new String[titles.length - 2];
            titlestmp[0] = titles[1];
            titlestmp[1] = titles[3];
            titlestmp[2] = titles[4];
            titles = titlestmp;
            resIds = new int[]{R.drawable.svg_icon_authority_management,
                    R.drawable.icon_soft_applock, R.drawable.icon_soft_applock};

        }
        //Chenyee guoxt modify for CSW1703CX-919 end
        //Gionee <GN_Oversea_Bug> <fengpeipei> <20161123> for applock end
        for (int i = 0; i < titles.length; i++) {
            ItemInfo info = new ItemInfo();
            info.setTitle(titles[i]);
            info.setIcon(mRes.getDrawable(resIds[i]));
            if (ChameleonColorManager.isNeedChangeColor()) {
                //guoxt modify for  37563 begin
                //info.getIcon().setTint(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
                //guoxt modify for  37563 end
            }
            if (i == 0) {

                updatatext0(info);
            }
            mData.add(info);
        }
        mInternalstorage = mRes.getString(R.string.text_total_internal);
        mSdcardstorage = mRes.getString(R.string.text_total_sd);
        mEnableString = mRes.getString(R.string.text_available_space);
        mUsedString = mRes.getString(R.string.text_used_space);
    }

    private void updatatext0(ItemInfo info) {
        int size = AutoBootAppManager.getInstance(this).getEnableAutoBootApps().size();
        String headFormat = mRes.getString(R.string.auto_start_head_text);
        String headStr = String.format(headFormat, size);
        info.setSummary(headStr);
    }

    private void initIntents() {
        Intent[] names = new Intent[]{new Intent(this, AutoStartMrgActivity.class),
                new Intent(this, DefaultSoftMrgActivity.class), new Intent(this, FreezedAppActivity.class),
                new Intent(this, AppLockManagerActivity.class), new Intent(this, HostSoftManagerMainActivity.class)};
        //Gionee <GN_Oversea_Bug> <fengpeipei> <20161123> for applock begin
        if (!mIsAppLockSupport) {
            names = new Intent[]{new Intent(this, AutoStartMrgActivity.class),
                    new Intent(this, DefaultSoftMrgActivity.class), new Intent(this, FreezedAppActivity.class), new Intent(this, HostSoftManagerMainActivity.class)};
        }
        //Gionee <GN_Oversea_Bug> <fengpeipei> <20161123> for applock end

        //Chenyee guoxt modify for CSW1703CX-919 begin
        if (Consts.cyCXFlag && mIsAppLockSupport) {
            names = new Intent[]{
                    new Intent(this, DefaultSoftMrgActivity.class), new Intent(this, AppLockManagerActivity.class), new Intent(this, HostSoftManagerMainActivity.class)};
        }
        //Chenyee guoxt modify for CSW1703CX-919 end

        for (int i = 0; i < names.length; i++) {
            mIntents.add(names[i]);
        }
    }

    /*@Override
    protected boolean getHasBackOption() {
        return true;
    }*/

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Intent intent = mIntents.get(position);
        if (intent != null) {
            startActivity(intent);
            mListView.setOnItemClickListener(null);
        }
        //YouJuAgent.onEvent(SoftManagerActivity.this, sClickCount_Youju[position]);
    }

    private void registerUSBReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private class ScannerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                // Modify by HZH on 2019/8/5 for EJWJ-398 start
                if (/*action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)
                        || */action.equals(Intent.ACTION_MEDIA_MOUNTED)
                        || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    isFirstIn = true;
                    SoftHelperUtils.initStorageState(context, mStorageManager);
                    mHelperUtils.cleanSdcardInfo();
                    mHelperUtils.init(context);
                    updateUSBCapacity();
                }
                // Modify by HZH on 2019/8/5 for EJWJ-398 end
            } catch (Exception e) {
                Log.e(TAG, "ScannerReceiver e:" + e);
            }
        }
    }

    @Override
    public void onUpdateStorageInfo() {
        sendMessage(STORAGE_INFO, 0);
    }

    private void updateStorageInfo() {
        if (SoftHelperUtils.getStorageCount() >= 2 || sSDK >= ANDROIDM_API_LEVEL) {
            setVisibled(true);
            if (SoftHelperUtils.getStorageMountedCount() >= 2) {
                setStorageInfo();
            } else if (SoftHelperUtils.getStorageMountedCount() == 1) {
                SDCardInfo sdCardInfo = mHelperUtils.getInternalStorageInfo();
                setUSBInfo(sdCardInfo, SoftHelperUtils.getStorageMountedCount());
                mCircleView.updateInnerCircleBgAlpha(0);
                controlNoSdcardVisibility();
                mCircleView.updateViews();
            } else if (SoftHelperUtils.getStorageMountedCount() == 0) {
                setVisibled(false);
                mCircleView.setVisibility(View.GONE);
                mNo_storage_text.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initVisibility() {
        mCircleView.setVisibility(View.GONE);
        mPhoneImg.setVisibility(View.GONE);
        mSDcardImg.setVisibility(View.GONE);
        mVerticalLine.setVisibility(View.GONE);
        mSingleProgressTitle.setVisibility(View.GONE);
        mSingleProgress.setVisibility(View.GONE);
        mSingleProgressSign.setVisibility(View.GONE);
        mDoublePhomeImgIndicator.setVisibility(View.GONE);
        mDoubleSDcardImgIndicator.setVisibility(View.GONE);
        mSinglePhomeImgIndicator.setVisibility(View.GONE);
    }

    private void controlNoSdcardVisibility() {
        mSingleProgressTitle.setVisibility(View.VISIBLE);
        mSingleProgress.setVisibility(View.VISIBLE);
        mSingleProgressSign.setVisibility(View.VISIBLE);
        // Add by HZH on 2019/8/2 for EJWJ-398 start
        mDoubleProgress.setVisibility(View.GONE);
        // Add by HZH on 2019/8/2 for EJWJ-398 end
    }

    private void controlHaveSdcardVisibility() {
        mSingleProgressTitle.setVisibility(View.GONE);
        mSingleProgress.setVisibility(View.GONE);
        mSingleProgressSign.setVisibility(View.GONE);
        // Add by HZH on 2019/8/2 for EJWJ-398 start
        mDoubleProgress.setVisibility(View.VISIBLE);
        mDoubleProgress.setText(mInternalProgress + "% / " + mSdProgress + "%");
        // Add by HZH on 2019/8/2 for EJWJ-398 end
    }

    private void initNoneSdcardView() {
        mStorageinfoView.addView(mNoneSdcardView, mLayoutParams);
        mCircleView.updateViewByPhoneUsage(0);
        mCircleView.updateViewBySDUsage(0);
        mCircleView.updateInnerCircleBgAlpha(0);
        mCircleView.setVisibility(View.VISIBLE);
        mCircleView.updateViews();
        mSinglePhomeImgIndicator.setVisibility(View.VISIBLE);
        mSingleInternalStorageTitle.setText(mRes.getString(R.string.single_phone_inner_storage));
        mPhoneImg.setVisibility(View.VISIBLE);
    }

    private void initHaveSdcardView() {
        mStorageinfoView.addView(mHaveSdcardView, mLayoutParams);
        mCircleView.updateViewByPhoneUsage(0);
        mCircleView.updateViewBySDUsage(0);
        mCircleView.updateInnerCircleBgAlpha(51);
        mCircleView.setVisibility(View.VISIBLE);
        mCircleView.updateViews();
        mPhoneImg.setVisibility(View.VISIBLE);
        mSDcardImg.setVisibility(View.VISIBLE);
        mVerticalLine.setVisibility(View.VISIBLE);
        mDoublePhomeImgIndicator.setVisibility(View.VISIBLE);
        mDoubleSDcardImgIndicator.setVisibility(View.VISIBLE);
        mInternalStorageTitle.setText(mRes.getString(R.string.phone_inner_storage));
        mSdcardStorageTitle.setText(mRes.getString(R.string.phone_outer_sd_storage));
    }

    private void setActionBarCustomView() {
        /*
        if (Environment.isExternalStorageEmulated()) {
            return;
        }
        */

        LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.systemmanager_settings_actionbar,
                null);
        ImageView otherImg = (ImageView) v.findViewById(R.id.img_another_button);
        ImageView img = (ImageView) v.findViewById(R.id.img_actionbar_custom);
        LinearLayout first = (LinearLayout) v.findViewById(R.id.first_click_field);
        //first.setVisibility(View.GONE);
        first.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        });
        otherImg.setImageResource(R.drawable.main_actionbar_setting);

        LinearLayout second = (LinearLayout) v.findViewById(R.id.second_click_field);
        second.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SoftManagerActivity.this, MovePackageTabActivity.class);
                startActivity(intent);
            }
        });
        img.setImageResource(R.drawable.svg_icon_back_left);

        if (Environment.isExternalStorageEmulated()) {
            second.setVisibility(View.GONE);
        }

        // Gionee <houjie> <2015-11-13> add for CR01565278 begin
        if (ChameleonColorManager.isNeedChangeColor()) {
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            if (img.getDrawable() != null) {
                img.getDrawable().setTint(color_T1);
            }
        }
        // Gionee <houjie> <2015-11-13> add for CR01565278 end

        CyeeActionBar.LayoutParams lp = new CyeeActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;/*
        getCyeeActionBar().setCustomView(v, lp);
        getCyeeActionBar().setDisplayShowCustomEnabled(true);*/
    }

    private class StorageRunnable implements Runnable {
        private int mState = 0;
        private int mStart = 0;
        private int mEnd = 0;

        public StorageRunnable(int state, int start, int end) {
            mState = state;
            mStart = start;
            mEnd = (int) (end * 360 * 0.01);
        }

        @Override
        public void run() {
            int duration = 1;
            sleep(300);
            while (mStart++ <= mEnd) {
                sendMessage(mState, mStart);
                sleep(getSleepTime(mStart));
            }
        }

        private void sleep(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (Exception ex) {
            }
        }

        private int getSleepTime(int value) {
            int duration = 3;
            return duration;
        }
    }

    private void sendMessage(int state, int arg1) {
        Message msg = mHandler.obtainMessage(state, arg1);
        mHandler.sendMessage(msg);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PHOHE_USAGE:
                    mCircleView.updateViewByPhoneUsage((Integer) msg.obj);
                    mCircleView.updateViews();
                    mSingleProgress.setText(String.valueOf((Integer) msg.obj * 100 / 360));
                    break;
                case SDCARD_USAGE:
                    mCircleView.updateViewBySDUsage((Integer) msg.obj);
                    mCircleView.updateViews();
                    break;
                case STORAGE_INFO:
                    updateStorageInfo();
                    break;
                default:
                    break;
            }
        }
    };

    // Gionee <houjie> <2015-07-29> add for CR01519483 begin
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
    // Gionee <houjie> <2015-07-29> add for CR01519483 end
}