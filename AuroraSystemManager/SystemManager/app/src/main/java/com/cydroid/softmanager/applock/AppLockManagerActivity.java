/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.applock;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.applock.verifier.viewcontrol.SecurityPwdUtils;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.BaseListActivity;
import com.cydroid.softmanager.softmanager.loader.SoftManagerIconLoader;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.widget.RelativeLayout;

public class AppLockManagerActivity extends BaseListActivity<AppLockAdapter> 
        implements StateChangeCallback, LocalChangedCallback {
    private static final String TAG = "AppLockManagerActivity";

    private final int REQUEST_CODE_APP_LOCK_MANAGER_SETTINGS = 100;

    private boolean mAuthenticated = false;
    private boolean mGoSecurityPassword = false;
    private LinearLayout mLoader;
    private LinearLayout mEmpty;

    private AppLockAdapter mAdapter;
    private List<AppLockAppInfo> mData = new ArrayList<AppLockAppInfo>();
    private final AppLockManager mAppLockManager = AppLockManager.getInstance();

    private final CompoundButton.OnCheckedChangeListener mSwitchChangeListener = new SwitchChangeListener();
    private CyeeAlertDialog mSettingDialog;
    private final Handler mHandler = new Handler();
    private Context mContext;
    //private  CyeeLockPatternUtils  mCyeeLockPatternUtils;
    private SearchBox mSearchBox;
    private int mSelectedItemOffset;
    private SoftManagerIconLoader mSoftManagerIconLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        mContext = this;
        // Chenyee xionghg 20180113 add for CSW1702A-2474 begin
        setTheme(R.style.AppLockManagerTheme);
        // Chenyee xionghg 20180113 add for CSW1702A-2474 end
        super.onCreate(savedInstanceState, R.layout.applock_manager_activity);
        initViewVisibility();
        getLoaderManager().initLoader(AppLockManagerActivityLoader.LOADER_APP_LOCK_MANAGER, null, this);
        mAppLockManager.setAppsChangeCallBack(String.valueOf(this.hashCode()), this);
        mAppLockManager.setLocalChangeCallBack(String.valueOf(this.hashCode()), this);
        chameleonColorProcess();
        mSelectedItemOffset = getResources().getDimensionPixelSize(R.dimen.list_selected_item_offset);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.host_bar_bg_white)));
        getSupportActionBar().setElevation(0.0f);
        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void chameleonColorProcess() {
        boolean isNeedChangeColor = ChameleonColorManager.isNeedChangeColor();
        if (isNeedChangeColor) {
            mSearchBox.changeSearchBoxBgColor();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume mAuthenticated:" + mAuthenticated 
            + ", mGoSecurityPassword:" + mGoSecurityPassword);
        super.onResume();
        if (!mAuthenticated) {
            mGoSecurityPassword = true;
            Log.d(TAG, "onResume startSecurityPassword");
            AppLockSecurityPasswordUtils.startSecurityPassword(this);
        } else {
            //chenyee zhaocaili 20180903 modify for CSW1705A-2849 begin
            mGoSecurityPassword = false;
            mAuthenticated = false;
            //chenyee zhaocaili 20180903 modify for CSW1705A-2849 end
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause mAuthenticated:" + mAuthenticated 
            + ", mGoSecurityPassword:" + mGoSecurityPassword);
        super.onPause();
        //chenyee zhaocaili 20180903 modify for CSW1705A-2849 begin
        /*if (!mGoSecurityPassword) {
            finish();
        }*/
        //chenyee zhaocaili 20180903 modify for CSW1705A-2849 end
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy"); 
        super.onDestroy();
        dismissDialog();
        mAppLockManager.unsetAppsChangeCallBack(String.valueOf(this.hashCode()));
        mAppLockManager.unsetLocalChangeCallBack(String.valueOf(this.hashCode()));
        mData.clear();
    }

    private void initViewVisibility() {
        mLoader = (LinearLayout) findViewById(R.id.loader);
        mEmpty = (LinearLayout) findViewById(R.id.empty_app_lock);
        mSearchBox = findViewById(R.id.searchbox);
        // Add by HZH on 2019/6/17 for EJSL-1506 start
        RelativeLayout relativeLayout = mSearchBox.findViewById(R.id.card_view);
        layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
        // Add by HZH on 2019/6/17 for EJSL-1506 end
        mLoader.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mListView.setOnItemClickListener(null);
        /*setActionBarCustomView();*/
        mSearchBox.setSearchListener(new SearchBox.SearchListener(){

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                // Add by HZH on 2019/6/17 for  start 
                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for  end
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the mSearchBox term changing
                //Called after it has updated results
                // Add by HZH on 2019/6/17 for  start
                int size = mSearchBox.getResults().size();
                layoutSearchBox(relativeLayout,
                        !TextUtils.isEmpty(term) && size > 0 ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for  end
            }

            @Override
            public void onSearch(String searchTerm) {
                //Toast.makeText(MainActivity.this, searchTerm +" Searched", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to a result being clicked
                final int position = result.position;
                if (position != -1 && position < mData.size()){
                    mListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mListView.smoothScrollToPositionFromTop(position, mSelectedItemOffset, 500);
                        }
                    });
                }
            }

            @Override
            public void onSearchCleared() {
                //Called when the clear button is clicked
                // Add by HZH on 2019/6/17 for  start
                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for  end
            }

        });
    }

    private void setActionBarCustomView() {
        LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.systemmanager_settings_actionbar,
                null);
        ImageView img = (ImageView) v.findViewById(R.id.img_actionbar_custom);
        LinearLayout first = (LinearLayout) v.findViewById(R.id.first_click_field);
        first.setVisibility(View.GONE);
        LinearLayout second = (LinearLayout) v.findViewById(R.id.second_click_field);
        second.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismissDialog();
                // createAndShowSettingDialog();
                mAuthenticated = true;
                Intent intent = new Intent(mContext, AppLockSettingsActivity.class);
                //chenyee zhaocaili modify for CSW1705P-249 begin
                startActivityForResult(intent, REQUEST_CODE_APP_LOCK_MANAGER_SETTINGS);
                //chenyee zhaocaili modify for CSW1705P-249 end
            }
        });
        img.setImageResource(R.drawable.main_actionbar_setting);
        CyeeActionBar.LayoutParams lp = new CyeeActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        /*getCyeeActionBar().setCustomView(v, lp);
        getCyeeActionBar().setDisplayShowCustomEnabled(true);*/
    }

    private void dismissDialog() {
        if (mSettingDialog != null) {
            mSettingDialog.dismiss();
            mSettingDialog = null;
        }
    }

    private void createAndShowSettingDialog() {
        int selectIdx = mAppLockManager.getAppLockSetting(this);
        Log.d(TAG, "createAndShowSettingDialog selectIdx:" + selectIdx);
        String[] settingDialogAdapter = getResources().getStringArray(R.array.app_lock_setting_items);
        mSettingDialog = new CyeeAlertDialog.Builder(this).setTitle(R.string.app_lock_setting_dialog_title)
                .setSingleChoiceItems(settingDialogAdapter, selectIdx,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAppLockManager.setAppLockSetting(AppLockManagerActivity.this, which);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.action_cancel,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    @Override
    public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
        return new AppLockManagerActivityLoader(this);
    }

    @Override
    protected AppLockAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new AppLockAdapter(this, mData, mSwitchChangeListener);
        }
        return mAdapter;
    }

    @Override
    public void onLoadFinished(Loader<Object> arg0, Object arg1) {
        refreshList();
    }

    @Override
    public void onStateChange() {
        Log.d(TAG, "onStateChange");
        mHandler.post(new Runnable() {
            public void run() {
               refreshList();
            }
        });
    }

    @Override
    public void onLocalChange() {
        Log.d(TAG, "onLocalChange");
        refreshList();
    }

    /*@Override
    protected boolean getHasBackOption() {
        return true;
    }*/

    private void refreshList() {
        List<AppLockAppInfo> lockedList = mAppLockManager.getLockedApps();
        List<AppLockAppInfo> unlockList = mAppLockManager.getUnLockApps();
        if (lockedList.size() <= 0 && unlockList.size() <= 0) {
            showEmptyView();
            return;
        }
        // mSearchBox.setVisibility(View.VISIBLE);
        mLoader.setVisibility(View.GONE);
        mEmpty.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mData.clear();
        mData.addAll(lockedList);
        mData.addAll(unlockList);
        mAdapter.notifyDataSetChanged();setSearchResultList();
        mSoftManagerIconLoader = new SoftManagerIconLoader(this, mData);
        mSoftManagerIconLoader.loadAppIcon(new SoftManagerIconLoader.ImageLoadCompleteCallback() {
            @Override
            public void loadComplete() {
                mData = (List<AppLockAppInfo>) mSoftManagerIconLoader.getListDatas();
                mSearchBox.clearSearchable();
                setSearchResultList();
            }
        });
    }

    public void setSearchResultList(){
        for (int i = 0; i < mData.size(); i++){
            ItemInfo info = mData.get(i);
            SearchResult option = new SearchResult(info.getTitle(), info.getIcon(), i);
            mSearchBox.addSearchable(option);
        }
    }

    private void showEmptyView() {
        mSearchBox.setVisibility(View.GONE);
        mLoader.setVisibility(View.GONE);
        mEmpty.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    private class SwitchChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            int pos = (Integer) view.getTag();
            if (mData.size() <= pos) {
                return;
            }
            String pkgName = mData.get(pos).getPackageName();

            if (HelperUtils.getApplicationInfo(AppLockManagerActivity.this, pkgName) == null) {
                Toast.makeText(AppLockManagerActivity.this,
                        AppLockManagerActivity.this.getResources().getString(R.string.app_not_exit),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            long type = SecurityPwdUtils.getSecurityPasswordType();
            if (type <= 0L) {
               // return;
            }

            final AppLockAppInfo info = (AppLockAppInfo) mData.get(pos);
            if (isChecked) {
                mAppLockManager.lockApp(info.getPackageName());
            } else {
                mAppLockManager.unLockApp(info.getPackageName());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //chenyee zhaocaili modify for CSW1705P-249 begin
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + ", resultCode:" + resultCode);
        if (requestCode == REQUEST_CODE_APP_LOCK_MANAGER_SETTINGS){
            if (data != null){
                int settingCode = data.getIntExtra("requestCode", -1);
                if (settingCode == AppLockSecurityPasswordUtils.REQUEST_CODE_CLEAR_PASSWORD){
                    if (resultCode == Activity.RESULT_OK){
                        for (AppLockAppInfo info : mAppLockManager.getLockedApps()){
                            mAppLockManager.unLockApp(info.getPackageName());
                            AppLockUtils.deleteLockedApps(mContext, info.getPackageName());
                        }
                        finish();
                    }
                }
            }
        }else {
            mAuthenticated = AppLockSecurityPasswordUtils.onActivityResult(this, requestCode, resultCode, data);
            Log.d(TAG, "onActivityResult  mAuthenticated:" + mAuthenticated);
        }
    }
    //chenyee zhaocaili modify for CSW1705P-249 end
}
