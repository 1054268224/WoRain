package com.cydroid.softmanager.softmanager;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.softmanager.adapter.MoveAdapter;
import com.cydroid.softmanager.softmanager.interfaces.SoftMrgCallback;
import com.cydroid.softmanager.softmanager.loader.SoftManagerLoader;
import com.cydroid.softmanager.softmanager.model.AppInfo;
import com.cydroid.softmanager.softmanager.model.ApplicationsInfo;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.softmanager.utils.SoftMrgUtil;
import com.cydroid.softmanager.utils.AppFilterUtil;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import cyee.widget.CyeeButton;

public class MovePackageTabActivity extends CyeeActivity
        implements CyeeActionBar.TabListener, LoaderCallbacks<Object>, StateChangeCallback, SoftMrgCallback {
    private static final String TAG = "MovePackageTabActivity";
    
    protected Resources mRes;
    private Context mContext;
    private ViewPager mViewPager;
    private MyPagerAdapter mMypagerAdapter;
    private String[] mTabTitles;
    private final List<View> mSections = new ArrayList<View>();

    protected ListView mInternalListView, mOutListView;
    protected LinearLayout mInternalLoadingView, mOutLoadingView;
    protected TextView mInternalLoadText, mOutLoadText, mInternalEmptyText, mOutEmptyText, mInternalAppSize,
            mOutAppSize, mInternalSelectAll, mOutSelectAll;
    protected RelativeLayout mInternalEmpty, mOutEmpty;
    protected CyeeButton mInternalMoveButton, mOutMoveButton;
    protected RelativeLayout mInternalSelectLayout, mOutSelectLayout, mInternalHeadBar, mOutHeaderBar;
    protected String mMovingText;
    protected ImageView mInternalEmptyImage, mOutEmptyImage;

    private static final int SDCARD_APP = 1;
    private static final int INTERNAL_APP = 0;
    
    private final List<AppInfo> mOnSDCard = new ArrayList<AppInfo>();
    private final List<AppInfo> mOnInternel = new ArrayList<AppInfo>();
    private final List<List<AppInfo>> mData = new ArrayList<List<AppInfo>>();
    private ApplicationsInfo mApplicationsInfo;
    private SoftMrgUtil mSoftMrgUtil;
    private boolean mIsInternal = true;
    private int mMoveCount = 0, mInternalTotleCheckCount = 0, mSDcardTotleCheckCount = 0;
    private final List<String> mMovePackageNameList = new ArrayList<String>();
    private boolean mMoveFailed = false, mMoveInBackGround = false, mNeedLoad = true, mOnStop = false;
    private int mInternalCount = 0, mSdcardCount = 0;
    private StorageManager mStorageManager;
    protected MoveAdapter mInternalAdapter, mOutAdapter;
    private boolean mLoadFinished = false;
    private CyeeActionBar mActionBar;

    public void onCreate(Bundle savedInstanceState) {
        if (UiUtils.isSpecialStyleModel()) {
            setTheme(R.style.AppTabTheme);
        } else {
            setTheme(R.style.AppTabThemeCustom);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_main_view);
        initRes();
        initActionBar();
        createDummySection();
        initPageAdapter();
        viewPagerSetTouchListener();
        startLoadingView();
        initData();
        actionBarAddTab();
        getLoaderManager().initLoader(SoftManagerLoader.ID_LOADER_DEFAULT, null, this);
    }

    @Override
    protected void onResume() {
        if (mMoveInBackGround) {
            startLoadingView();
        }

        super.onResume();
    }

    @Override
    protected void onStop() {
        mOnStop = true;
        SoftManagerLoader.setNotLoad(mMoveInBackGround);
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mViewPager.setAdapter(null);
        mOnSDCard.clear();
        mOnInternel.clear();
        mData.clear();
        
        mMovePackageNameList.clear();
    }

    private void initRes() {
        mContext = this;
        mRes = getResources();

        if (SoftHelperUtils.getStorageMountedCount() < 2) {
            mTabTitles = getResources().getStringArray(R.array.move_tab_item);
        } else {
            mTabTitles = getResources().getStringArray(R.array.move_tab_item_sd);
        }

    }

    private void initActionBar() {
        mActionBar = getCyeeActionBar();
        mActionBar.setNavigationMode(CyeeActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setIndicatorBackgroundColor(Color.parseColor("#ffffff"));
    }

    private void createDummySection() {
        for (int i = 0; i < 2; i++) {
            View tab = null;
            //guoxt modif for CR01625070 begin
            if(Consts.gnSwFlag){
                tab = LayoutInflater.from(this).inflate(R.layout.sw_move_package_tab_item, null);
            }else{
                tab = LayoutInflater.from(this).inflate(R.layout.move_package_tab_item, null);
            }
            //guoxt modif for CR01625070 end
			
            mSections.add(tab);
        }
        findTabView();
        setClickListenner();
    }

    private void initPageAdapter() {
        mMypagerAdapter = new MyPagerAdapter();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mMypagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (!mLoadFinished) {
                    return;
                }

                if (mIsInternal) {
                    if (mMoveCount < mInternalTotleCheckCount) {
                        return;
                    }
                } else {
                    if (mMoveCount < mSDcardTotleCheckCount) {
                        return;
                    }
                }
                mActionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                mActionBar.onPageScrolled(arg0, arg1, arg2);
            }
        });
    }

    private void viewPagerSetTouchListener() {
        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!mLoadFinished) {
                    return true;
                }

                if (mIsInternal) {
                    if (mMoveCount < mInternalTotleCheckCount) {
                        return true;
                    }
                } else {
                    if (mMoveCount < mSDcardTotleCheckCount) {
                        return true;
                    }
                }
                mActionBar.onScrollToEnd(v, event);
                return false;
            }
        });
    }

    protected void startLoadingView() {
        if (mIsInternal) {
            mInternalListView.setVisibility(View.GONE);
            mInternalLoadingView.setVisibility(View.VISIBLE);
        } else {
            mOutListView.setVisibility(View.GONE);
            mOutLoadingView.setVisibility(View.VISIBLE);
        }

    }

    private void initData() {
        mApplicationsInfo = ApplicationsInfo.getInstance();
        mSoftMrgUtil = SoftMrgUtil.getInstance(this);
        mSoftMrgUtil.setSoftMrgCallback(this);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
    }

    private void actionBarAddTab() {
        mActionBar.addTab(mActionBar.newTab().setText(mTabTitles[0]).setTabListener(this));
        mActionBar.addTab(mActionBar.newTab().setText(mTabTitles[1]).setTabListener(this));
    }

    private void setInternalData() {
        mInternalAdapter = new MoveAdapter(this, this);
        mData.clear();
        mData.add(INTERNAL_APP, mOnInternel);
        mData.add(SDCARD_APP, mOnSDCard);
        mInternalAdapter.updateChild(INTERNAL_APP, mData);
    }

    private void setOutData() {
        mOutAdapter = new MoveAdapter(this, this);
        mData.clear();
        mData.add(INTERNAL_APP, mOnInternel);
        mData.add(SDCARD_APP, mOnSDCard);
        mOutAdapter.updateChild(SDCARD_APP, mData);
    }

    private void setClickListenner() {
        setSelectAllClick();
        setMoveButtonClick();
    }

    private void setMoveButtonText() {
        String moveButtonText;
        if (SoftHelperUtils.getStorageMountedCount() < 2) {
            // if (mIsInternal) {
            moveButtonText = mRes.getString(R.string.move_to_internal);
            mInternalMoveButton.setText(moveButtonText);
            //mInternalAppSize.setText(mRes.getString(R.string.system_move_app_size, mOnInternel.size()));
           // mInternalHeadBar.setVisibility(View.VISIBLE);
            // }else{
            moveButtonText = mRes.getString(R.string.move_to_systemspace);
            mOutMoveButton.setText(moveButtonText);
            //mOutAppSize.setText(mRes.getString(R.string.internal_move_app_size, mOnSDCard.size()));
            //mOutHeaderBar.setVisibility(View.VISIBLE);
            // }
        } else {
            // if (mIsInternal) {
            moveButtonText = mRes.getString(R.string.move_to_sdcard);
            mInternalMoveButton.setText(moveButtonText);
           // mInternalAppSize.setText(mRes.getString(R.string.system_move_app_size, mOnInternel.size()));
           // mInternalHeadBar.setVisibility(View.VISIBLE);
            // }else{
            moveButtonText = mRes.getString(R.string.move_to_systemspace);
            mOutMoveButton.setText(moveButtonText);
            //mOutAppSize.setText(mRes.getString(R.string.sdcard_move_app_size, mOnSDCard.size()));
           // mOutHeaderBar.setVisibility(View.VISIBLE);
            // }
        }
    }

    private void setSelectAllClick() {
        mInternalHeadBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SoftHelperUtils.setInternalCheckFlag(!SoftHelperUtils.getInternalCheckFlag());
//                if (HelperUtils.getInternalCheckFlag()) {
//                    mInternalSelectAll.setText(R.string.unselect_all);
//                } else {
//                    mInternalSelectAll.setText(R.string.select_all);
//                }
                setCheckStatus(SoftHelperUtils.getInternalCheckFlag(), INTERNAL_APP);
                setEnable(INTERNAL_APP);
                mInternalAdapter.notifyDataSetChanged();
            }
        });

        mOutHeaderBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SoftHelperUtils.setOutCheckFlag(!SoftHelperUtils.getOutCheckFlag());
//                if (HelperUtils.getOutCheckFlag()) {
//                    mOutSelectAll.setText(R.string.unselect_all);
//                } else {
//                    mOutSelectAll.setText(R.string.select_all);
//                }
                setCheckStatus(SoftHelperUtils.getOutCheckFlag(), SDCARD_APP);
                setEnable(SDCARD_APP);
                mOutAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setMoveButtonClick() {
        setInternalMoveButtonClickListener();
        setOutMoveButtonClickListener();

    }

    private void setInternalMoveButtonClickListener() {
        mInternalMoveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SoftHelperUtils.isExSdcardInserted(mContext)) {
                    if (SoftHelperUtils.getStorageMountedCount() < 2) {
                        if (!SoftHelperUtils.isSdcardMounted(mStorageManager)) {
                            Toast.makeText(mContext,
                                    mContext.getResources().getString(R.string.text_no_sd_card_not_surport),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    } else {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.text_no_sd_card),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                getCheckCount();
                mInternalLoadingView.setVisibility(View.VISIBLE);
                mInternalListView.setVisibility(View.GONE);
                setHeaderVisibility(false);
                mActionBar.setDisplayHomeAsUpEnabled(false);
                if (mIsInternal) {
                    moveSelectApp(INTERNAL_APP);
                } else {
                    moveSelectApp(SDCARD_APP);
                }
                mInternalMoveButton.setEnabled(false);
            }
        });
    }

    private void setOutMoveButtonClickListener() {
        mOutMoveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SoftHelperUtils.isExSdcardInserted(mContext)) {
                    if (SoftHelperUtils.getStorageMountedCount() < 2) {
                        if (!SoftHelperUtils.isSdcardMounted(mStorageManager)) {
                            Toast.makeText(mContext,
                                    mContext.getResources().getString(R.string.text_no_sd_card_not_surport),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    } else {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.text_no_sd_card),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                getCheckCount();
                mOutLoadingView.setVisibility(View.VISIBLE);
                mOutListView.setVisibility(View.GONE);
                setHeaderVisibility(false);
                mActionBar.setDisplayHomeAsUpEnabled(false);
                if (mIsInternal) {
                    moveSelectApp(INTERNAL_APP);
                } else {
                    moveSelectApp(SDCARD_APP);
                }
                mOutMoveButton.setEnabled(false);
            }
        });
    }

    private void getCheckCount() {
        if (mIsInternal) {
            for (int i = 0; i < mData.get(INTERNAL_APP).size(); i++) {
                if (mData.get(INTERNAL_APP).get(i).getCheckStaus()) {
                    mInternalTotleCheckCount++;
                }
            }
        } else {
            for (int i = 0; i < mData.get(SDCARD_APP).size(); i++) {
                if (mData.get(SDCARD_APP).get(i).getCheckStaus()) {
                    mSDcardTotleCheckCount++;
                }
            }
        }
    }

    private void moveSelectApp(int position) {
        boolean isFirst = true;
        mMovePackageNameList.clear();
        for (int i = 0; i < mData.get(position).size(); i++) {
            if (mData.get(position).get(i).getCheckStaus()) {
                mData.get(position).get(i).setCheckStatus(false);
                mMovePackageNameList.add(mMovingText + mData.get(position).get(i).getTitle());
                boolean sucecuss = moveApp(mData.get(position).get(i));
                if (!sucecuss) {
                    break;
                }
                if (isFirst) {
                    isFirst = false;
                    String moving = mMovingText + mData.get(position).get(i).getTitle();
                    if (position == INTERNAL_APP) {
                        mInternalLoadText.setText(moving);
                    } else {
                        mOutLoadText.setText(moving);
                    }

                }
            }
        }
    }

    private boolean moveApp(AppInfo info) {
        if (!SoftHelperUtils.isExSdcardInserted(mContext) && mIsInternal) {
            if (SoftHelperUtils.getStorageMountedCount() < 2) {
                if (!SoftHelperUtils.isSdcardMounted(mStorageManager)) {
                    Toast.makeText(mContext,
                            mContext.getResources().getString(R.string.text_no_sd_card_not_surport),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.text_no_sd_card),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        int flag = (info.mApplicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0
                ? PackageManager.MOVE_INTERNAL : PackageManager.MOVE_EXTERNAL_MEDIA;
        if (flag == PackageManager.MOVE_EXTERNAL_MEDIA) {
            Log.d("dzmdzm", "PackageManager.MOVE_EXTERNAL_MEDIA");
        } else {
            Log.d("dzmdzm", "MOVE_INTERNAL");
        }
        mSoftMrgUtil.movePackage(info.mApplicationInfo.packageName, flag);
        return true;

    }

    private void setCheckStatus(boolean isChecked, int position) {
        for (int i = 0; i < mData.get(position).size(); i++) {
            mData.get(position).get(i).setCheckStatus(isChecked);
        }
    }

    private void setEnable(int position) {
        if (mData.size() <= 0) {
            return;
        }
        int count = 0;
        for (int i = 0; i < mData.get(position).size(); i++) {
            if (mData.get(position).get(i).getCheckStaus()) {
                count++;
            }

        }
        if (position == INTERNAL_APP) {
            if (count > 0) {
                mInternalMoveButton.setEnabled(true);
                SoftHelperUtils.setInternalCheckFlag(count == mData.get(position).size());
            } else {
                mInternalMoveButton.setEnabled(false);
                SoftHelperUtils.setInternalCheckFlag(false);
            }
            if (SoftHelperUtils.getInternalCheckFlag()) {
                mInternalSelectAll.setText(R.string.unselect_all);
            } else {
                mInternalSelectAll.setText(R.string.select_all);
            }
        } else {
            if (count > 0) {
                mOutMoveButton.setEnabled(true);
                SoftHelperUtils.setOutCheckFlag(count == mData.get(position).size());
            } else {
                mOutMoveButton.setEnabled(false);
                SoftHelperUtils.setOutCheckFlag(false);
            }
            if (SoftHelperUtils.getOutCheckFlag()) {
                mOutSelectAll.setText(R.string.unselect_all);
            } else {
                mOutSelectAll.setText(R.string.select_all);
            }
        }

    }

    private void findTabView() {
        mInternalListView = (ListView) mSections.get(0).findViewById(R.id.listview);
        mInternalLoadingView = (LinearLayout) mSections.get(0).findViewById(R.id.loader);
        mInternalLoadText = (TextView) mSections.get(0).findViewById(R.id.load_text);
        mInternalEmptyText = (TextView) mSections.get(0).findViewById(R.id.empty_text);
        mInternalEmpty = (RelativeLayout) mSections.get(0).findViewById(R.id.permission_empty_view);
        mInternalMoveButton = (CyeeButton) mSections.get(0).findViewById(R.id.move_button);
        mInternalSelectLayout = (RelativeLayout) mSections.get(0).findViewById(R.id.select_layout);
        mMovingText = mRes.getString(R.string.moving_app);
        mInternalEmptyImage = (ImageView) mSections.get(0).findViewById(R.id.permission_empty_img);
        mInternalAppSize = (TextView) mSections.get(0).findViewById(R.id.move_header_title);
        mInternalSelectAll = (TextView) mSections.get(0).findViewById(R.id.move_header_summary);
        mInternalHeadBar = (RelativeLayout) mSections.get(0).findViewById(R.id.head_bar);
        mInternalHeadBar.setVisibility(View.GONE);
        //mInternalEmpty.setVisibility(View.VISIBLE);
        //mInternalEmptyImage.setBackground(mRes.getDrawable(R.drawable.empty_move_app));
        //mInternalEmptyText.setText(R.string.no_app_system_space);

        mOutListView = (ListView) mSections.get(1).findViewById(R.id.listview);
        mOutLoadingView = (LinearLayout) mSections.get(1).findViewById(R.id.loader);
        mOutLoadText = (TextView) mSections.get(1).findViewById(R.id.load_text);
        mOutEmptyText = (TextView) mSections.get(1).findViewById(R.id.empty_text);
        mOutEmpty = (RelativeLayout) mSections.get(1).findViewById(R.id.permission_empty_view);
        mOutMoveButton = (CyeeButton) mSections.get(1).findViewById(R.id.move_button);
        mOutSelectLayout = (RelativeLayout) mSections.get(1).findViewById(R.id.select_layout);
        mOutEmptyImage = (ImageView) mSections.get(1).findViewById(R.id.permission_empty_img);
        mOutAppSize = (TextView) mSections.get(1).findViewById(R.id.move_header_title);
        mOutSelectAll = (TextView) mSections.get(1).findViewById(R.id.move_header_summary);
        mOutHeaderBar = (RelativeLayout) mSections.get(1).findViewById(R.id.head_bar);
        mOutHeaderBar.setVisibility(View.GONE);
        mOutEmpty.setVisibility(View.VISIBLE);
       /*guoxt modify for CR01512530 beign */
       // mOutEmptyImage.setBackground(mRes.getDrawable(R.drawable.empty_move_app));
      /*guoxt modify for CR01512530 end */
        if (SoftHelperUtils.getStorageMountedCount() < 2) {
            mOutEmptyText.setText(R.string.no_app_internal);
        } else {
            mOutEmptyText.setText(R.string.no_app_sd);
        }
    }

    public class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mSections.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mSections.get(position));
            return mSections.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mSections.get(position));
        }
    }

    @Override
    public void onTabSelected(cyee.app.CyeeActionBar.Tab tab, FragmentTransaction ft) {
        if (!mLoadFinished) {
            return;
        }

        if (mIsInternal) {
            if (mMoveCount < mInternalTotleCheckCount) {
                return;
            }
        } else {
            if (mMoveCount < mSDcardTotleCheckCount) {
                return;
            }
        }
        mViewPager.setCurrentItem(tab.getPosition());
        if (mLoadFinished) {
            mIsInternal = tab.getPosition() == 0;
        }
    }

    @Override
    public void onTabUnselected(cyee.app.CyeeActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabReselected(cyee.app.CyeeActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStateChange() {
        // TODO Auto-generated method stub
        if (mIsInternal) {
            setEnable(INTERNAL_APP);
        } else {
            setEnable(SDCARD_APP);
        }

    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        // TODO Auto-generated method stub
        return new SoftManagerLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {

        if (mMoveInBackGround || mOnStop) {
            return;
        }
        if (mNeedLoad) {
            getAppInfo();
        }
        setMoveButtonText();
        endLoadingView();
        refreshList();
        mLoadFinished = true;

    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
        // TODO Auto-generated method stub

    }

    private void refreshList() {

        if (mApplicationsInfo.mAppEntries.size() == 0) {
            getAppCount();
            setEmptyViewVisible(true, true);
            return;
        }
        getAppCount();
        setVisible();
        setEnable(INTERNAL_APP);
        setEnable(SDCARD_APP);
    }

    private void setEmptyViewVisible(boolean visible, boolean isInternal) {
        if (visible) {
            if (isInternal) {
                mInternalEmpty.setVisibility(View.VISIBLE);
               /*guoxt modify for CR01512530 beign */
               //mInternalEmptyImage.setBackground(mRes.getDrawable(R.drawable.empty_move_app));
               /*guoxt modify for CR01512530 end */
                mInternalListView.setVisibility(View.GONE);
                mInternalSelectLayout.setVisibility(View.GONE);
                mInternalHeadBar.setVisibility(View.GONE);
                mInternalEmptyText.setText(R.string.no_app_system_space);
            } else {
                mOutEmpty.setVisibility(View.VISIBLE);
                /*guoxt modify for CR01512530 beign */
               // mOutEmptyImage.setBackground(mRes.getDrawable(R.drawable.empty_move_app));
               /*guoxt modify for CR01512530 end */
                mOutListView.setVisibility(View.GONE);
                mOutSelectLayout.setVisibility(View.GONE);
                mOutHeaderBar.setVisibility(View.GONE);
                if (SoftHelperUtils.getStorageMountedCount() < 2) {
                    mOutEmptyText.setText(R.string.no_app_internal);
                } else {
                    mOutEmptyText.setText(R.string.no_app_sd);
                }
            }

        } else {
            if (isInternal) {
                mInternalEmpty.setVisibility(View.GONE);
                mInternalLoadingView.setVisibility(View.GONE);
                mInternalListView.setVisibility(View.VISIBLE);
                setInternalData();
                mInternalListView.setAdapter(mInternalAdapter);
                mInternalHeadBar.setVisibility(View.VISIBLE);
                mInternalAppSize.setText(mRes.getString(R.string.system_move_app_size, mOnInternel.size()));
                mInternalSelectLayout.setVisibility(View.VISIBLE);
            } else {
                mOutEmpty.setVisibility(View.GONE);
                mOutLoadingView.setVisibility(View.GONE);
                mOutListView.setVisibility(View.VISIBLE);
                setOutData();
                mOutListView.setAdapter(mOutAdapter);
                mOutHeaderBar.setVisibility(View.VISIBLE);
              /*guoxt modify for CR01515619 beign */
               if (SoftHelperUtils.getStorageMountedCount() < 2) {
                  mOutAppSize.setText(mRes.getString(R.string.internal_move_app_size, mOnSDCard.size()));
               }else{
                  mOutAppSize.setText(mRes.getString(R.string.sdcard_move_app_size, mOnSDCard.size()));
               }
              /*guoxt modify for CR01515619 end */
                mOutSelectLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void getAppCount() {
        mInternalCount = mOnInternel.size();
        mSdcardCount = mOnSDCard.size();
    }

    protected void endLoadingView() {
        if (mIsInternal) {
            mInternalLoadingView.setVisibility(View.GONE);
            mInternalListView.setVisibility(View.VISIBLE);
        } else {
            mOutLoadingView.setVisibility(View.GONE);
            mOutListView.setVisibility(View.VISIBLE);
        }

    }

    private void getAppInfo() {
        mOnSDCard.clear();
        mOnInternel.clear();
        SoftHelperUtils helperUtils = new SoftHelperUtils(mContext);
        List<ApplicationInfo> infos = SoftHelperUtils.getThirdApplicationInfo(mContext);
        for (int i = 0; i < infos.size(); i++) {
            ApplicationInfo info = infos.get(i);
            AppInfo appInfo = new AppInfo(info);
            appInfo.setPackageName(info.packageName);
            appInfo.setTitle(SoftHelperUtils.loadLabel(mContext, info));
            appInfo.setCheckStatus(false);
            String summary = helperUtils.invokePMGetPackageSizeInfo(mContext, info.packageName);
            if (!TextUtils.isEmpty(summary)) {
                appInfo.setSummary(summary);
            }

            if (AppFilterUtil.ON_SD_CARD_FILTER.filterApp(infos.get(i))) {
                if ((infos.get(i).flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                    mOnSDCard.add(appInfo);
                } else {
                    mOnInternel.add(appInfo);
                }
            }
        }
    }

    private void setVisible() {
        // if (mIsInternal) {
        setEmptyViewVisible(mOnInternel.size() == 0, true);
        // } else {
        setEmptyViewVisible(mOnSDCard.size() == 0, false);
        // }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mMoveFailed) {
                mMoveFailed = false;
                return super.onKeyDown(keyCode, event);
            }

            if (mIsInternal) {
                if (mMoveCount < mInternalTotleCheckCount) {
                    Toast.makeText(this, mRes.getString(R.string.moving_wait), Toast.LENGTH_SHORT).show();
                    return true;
                }
            } else {
                if (mMoveCount < mSDcardTotleCheckCount) {
                    Toast.makeText(this, mRes.getString(R.string.moving_wait), Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFinish(String pkgName, int returnCode) {
        Log.d(TAG, "package: " + pkgName + " ,returnCode: " + returnCode);
        String name;
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, pkgName);
        if (info == null) {
            Toast.makeText(this, mRes.getString(R.string.text_move_failed_doesnt_exist_not_surport),
                    Toast.LENGTH_SHORT).show();
            mInternalTotleCheckCount--;
            if (mIsInternal) {
                mInternalCount--;
            } else {
                mSdcardCount--;
            }
            return;
        }
        name = SoftHelperUtils.loadLabel(mContext, info);
        switch (returnCode) {
            case PackageManager.MOVE_SUCCEEDED:
                mMoveCount++;
                if (mMoveCount < mMovePackageNameList.size()) {
                    String moving = mMovePackageNameList.get(mMoveCount);
                    mInternalLoadText.setText(moving);
                }

                Log.d("dzmdzm", "mMoveCount:" + mMoveCount + "mInternalTotleCheckCount:"
                        + mInternalTotleCheckCount + "mSDcardTotleCheckCount:" + mSDcardTotleCheckCount);
                if (mIsInternal) {
                    mInternalCount--;
                    mSdcardCount++;
                } else {
                    mInternalCount++;
                    mSdcardCount--;
                }
                mMoveInBackGround = true;
                mOnStop = true;
                if (mIsInternal) {
                    if (mMoveCount == mInternalTotleCheckCount) {
                        mInternalTotleCheckCount = 0;
                        moveFinished();
                    }
                } else {
                    if (mMoveCount == mSDcardTotleCheckCount) {
                        mSDcardTotleCheckCount = 0;
                        moveFinished();
                    }
                }
                return;
            case PackageManager.MOVE_FAILED_DOESNT_EXIST:
                mMoveFailed = true;
                moveFailed();
                if (SoftHelperUtils.getStorageMountedCount() < 2) {
                    Toast.makeText(this, mRes.getString(R.string.text_no_sd_card_not_surport, name),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, mRes.getString(R.string.text_move_failed_doesnt_exist, name),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case PackageManager.MOVE_FAILED_INSUFFICIENT_STORAGE:
                mMoveFailed = true;
                moveFailed();
                Toast.makeText(this, mRes.getString(R.string.text_move_failed_insufficient_storage, name),
                        Toast.LENGTH_SHORT).show();
                break;
            case PackageManager.MOVE_FAILED_INTERNAL_ERROR:
                mMoveFailed = true;
                moveFailed();
                Toast.makeText(this, mRes.getString(R.string.text_move_failed_internal_error, name),
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                mMoveFailed = true;
                moveFailed();
                Toast.makeText(this, mRes.getString(R.string.text_move_failed_unkown_reason, name),
                        Toast.LENGTH_SHORT).show();
                break;
        }
        setCheckStatus();
        endLoadingView();
        setHeaderVisibility(true);
        if (mIsInternal) {
            mInternalAdapter.notifyDataSetChanged();
        } else {
            mOutAdapter.notifyDataSetChanged();
        }

    }

    private void moveFinished() {
        mInternalCount = 0;
        mSdcardCount = 0;
        mMoveCount = 0;
        mMoveInBackGround = false;
        mNeedLoad = false;
        mOnStop = false;
        endLoadingView();
        getAppInfo();
        refreshList();
        mActionBar.setDisplayHomeAsUpEnabled(true);

    }

    private void moveFailed() {
        mActionBar.setDisplayHomeAsUpEnabled(true);
        setHeaderVisibility(true);
        SoftHelperUtils.setInternalCheckFlag(false);
        SoftHelperUtils.setOutCheckFlag(false);
    }

    private void setCheckStatus() {
        if (mIsInternal) {
            mInternalTotleCheckCount = 0;
            for (int i = 0; i < mData.get(INTERNAL_APP).size(); i++) {
                mData.get(INTERNAL_APP).get(i).setCheckStatus(false);
            }
        } else {
            mSDcardTotleCheckCount = 0;
            for (int i = 0; i < mData.get(SDCARD_APP).size(); i++) {
                mData.get(SDCARD_APP).get(i).setCheckStatus(false);
            }
        }
    }

    private void setHeaderVisibility(boolean visible) {

        if (visible) {
            if (mIsInternal) {
                mInternalHeadBar.setVisibility(View.VISIBLE);
                mInternalSelectLayout.setVisibility(View.VISIBLE);
            } else {
                mOutHeaderBar.setVisibility(View.VISIBLE);
                mOutSelectLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (mIsInternal) {
                mInternalHeadBar.setVisibility(View.GONE);
                mInternalSelectLayout.setVisibility(View.GONE);
            } else {
                mOutHeaderBar.setVisibility(View.GONE);
                mOutSelectLayout.setVisibility(View.GONE);
            }
        }
    }

}
