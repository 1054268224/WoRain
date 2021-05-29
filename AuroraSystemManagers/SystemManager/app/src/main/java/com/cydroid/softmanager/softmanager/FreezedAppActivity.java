/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.softmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.adapter.FreezedAppAdapter;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppInfo;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppManager;
import com.cydroid.softmanager.softmanager.loader.SoftManagerIconLoader;
import com.cydroid.softmanager.softmanager.loader.SoftManagerLoader;
import com.cydroid.softmanager.utils.GNToast;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import android.text.TextUtils;

public class FreezedAppActivity extends BaseListActivity<FreezedAppAdapter>
        implements StateChangeCallback, LocalChangedCallback, View.OnClickListener {
    private static final String TAG = "FreezeAppActivity";

    private LinearLayout mLoaderView;
    private TextView mEmptyView;
    private TextView mHeaderTextView;
    private CyeeButton mAddFreezeAppBtn;
    private Button mBtnAddApp;

    private final FreezeAppManager mFreezeAppManager = FreezeAppManager.getInstance();
    private List<FreezeAppInfo> mFreezedApps = new ArrayList<>();
    private final OnClickListener mOnClickListener = new OnClickListener();
    private final Handler mHandler = new Handler();

    private boolean mIsFirstLoad = true;
    private SearchBox mSearchBox;
    private int mSelectedItemOffset;
    private SoftManagerIconLoader mSoftManagerIconLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Gionee xionghg 2017-06-14 modify for 156425 begin
        if (Consts.gnSwFlag) {
            super.onCreate(savedInstanceState, R.layout.sw_softmanager_activity_app_freezed);
        } else {
            super.onCreate(savedInstanceState, R.layout.softmanager_activity_app_freezed);
        }
        // Gionee xionghg 2017-06-14 modify for 156425 end
        ChameleonColorManager.getInstance().onCreate(this);
        initViews();
        /*initCustomActionBar();*/
        getLoaderManager().initLoader(SoftManagerLoader.ID_LOADER_FREEZE, null, this);
        mFreezeAppManager.setFreezeAppsChangeCallBack(String.valueOf(this.hashCode()), this);
        mFreezeAppManager.setLocalChangeCallBack(String.valueOf(this.hashCode()), this);
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

    private void initViews() {
        mLoaderView = (LinearLayout) findViewById(R.id.loader);
        mEmptyView =  findViewById(R.id.freezed_app_empty_view);
        // Add by HZH on 2019/5/11 for  start ^_^
        String emptyStr = getString(R.string.text_no_freezed_app_first) + "\n" + getString(R.string.text_no_freezed_app_second);
        mEmptyView.setText(emptyStr);
        // Add by HZH on 2019/5/11 for  end ^_^
        mAddFreezeAppBtn = (CyeeButton) findViewById(R.id.add_freeze_app_btn);
        mBtnAddApp = (Button) findViewById(R.id.btn_add_app);
        mHeaderTextView = (TextView) findViewById(R.id.header_title);
        mSearchBox = findViewById(R.id.searchbox);
        // Add by HZH on 2019/6/17 for EJSL-1506 start
        RelativeLayout relativeLayout = mSearchBox.findViewById(R.id.card_view);
        layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
        // Add by HZH on 2019/6/17 for EJSL-1506 end

        mListView.setVisibility(View.GONE);
        mListView.setOnItemClickListener(null);
        mLoaderView.setVisibility(View.VISIBLE);
        mHeaderTextView.setVisibility(View.GONE);

        mAddFreezeAppBtn.setOnClickListener(this);
        mBtnAddApp.setOnClickListener(this);
        mOnClickListener.setActivity(this);
        mSearchBox.setLogoTextColor(getColor(R.color.item_black));
        mSearchBox.setSearchListener(new SearchBox.SearchListener(){

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
                mBtnAddApp.setVisibility(View.GONE);
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                // Add by HZH on 2019/6/17 for EJSL-1506 start
                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                mBtnAddApp.setVisibility(View.VISIBLE);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the mSearchBox term changing
                //Called after it has updated results
                // Add by HZH on 2019/6/17 for EJSL-1506 start
                int size = mSearchBox.getResults().size();
                layoutSearchBox(relativeLayout,
                        !TextUtils.isEmpty(term) && size > 0 ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

            @Override
            public void onSearch(String searchTerm) {
                //Toast.makeText(MainActivity.this, searchTerm +" Searched", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to a result being clicked
                final int position = result.position;
                if (position != -1 && position < mFreezedApps.size()){
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
                // Add by HZH on 2019/6/17 for EJSL-1506 start
                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

        });
    }

    private void initCustomActionBar() {
        LinearLayout actionBar = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.systemmanager_settings_actionbar, null);

        initCustomActionBarFirstClick(actionBar);
        initCustomActionBarSecondClick(actionBar);

        CyeeActionBar.LayoutParams lp = new CyeeActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        //Gionee <jiangsj> <20170414> modify for 113662 begin
        lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        //Gionee <jiangsj> <20170414> modify for 113662 end
        /*getCyeeActionBar().setCustomView(actionBar, lp);
        getCyeeActionBar().setDisplayShowCustomEnabled(true);*/
    }

    private void initCustomActionBarFirstClick(ViewGroup actionBar) {
        LinearLayout first = (LinearLayout) actionBar.findViewById(R.id.first_click_field);
        first.setVisibility(View.GONE);
    }

    private void initCustomActionBarSecondClick(ViewGroup actionBar) {
        initCustomActionBarSecondClickAction(actionBar);
        initCustomActionBarSecondClickImg(actionBar);
    }

    private void initCustomActionBarSecondClickAction(ViewGroup actionBar) {
        LinearLayout second = (LinearLayout) actionBar.findViewById(R.id.second_click_field);
        second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CyeeAlertDialog.Builder(FreezedAppActivity.this)
                        .setTitle(R.string.title_about)
                        .setMessage(R.string.app_freeze_prompt_info)
                        .setPositiveButton(
                                R.string.app_freeze_prompt_btn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .show();
            }
        });
    }

    private void initCustomActionBarSecondClickImg(ViewGroup actionBar) {
        ImageView img = (ImageView) actionBar.findViewById(R.id.img_actionbar_custom);
        img.setImageResource(R.drawable.app_freeze_info);

        if (ChameleonColorManager.isNeedChangeColor()) {
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            if (img.getDrawable() != null) {
                img.getDrawable().setTint(color_T1);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsFirstLoad) {
            Log.d(TAG, "----->startLoader restart ");
            getLoaderManager().restartLoader(SoftManagerLoader.ID_LOADER_FREEZE, null, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFreezeAppManager.unsetFreezeAppsChangeCallBack(String.valueOf(this.hashCode()));
        mFreezeAppManager.unsetLocalChangeCallBack(String.valueOf(this.hashCode()));

        mAdapter.setOnClickListener(null);
        mOnClickListener.setActivity(null);
        mFreezedApps.clear();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    @Override
    protected FreezedAppAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new FreezedAppAdapter(this, mFreezedApps, mOnClickListener);
        }
        return mAdapter;
    }

    @Override
    public void onStateChange() {
        mHandler.post(new Runnable() {
            public void run() {
                refreshList();
            }
        });
    }

    @Override
    public void onLoadFinished(Loader<Object> arg0, Object arg1) {
        mIsFirstLoad = false;
        refreshList();
    }

    private void refreshList() {
        List<FreezeAppInfo> freezedApps = mFreezeAppManager.getFreezedApps();
        if (freezedApps.isEmpty()) {
            showEmptyView();
            return;
        }
         mSearchBox.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mFreezedApps.clear();
        mFreezedApps.addAll(freezedApps);
        mHeaderTextView.setVisibility(View.GONE);
        updateHeader(freezedApps.size());
        mAdapter.notifyDataSetChanged();
//        setSearchResultList();
        mSoftManagerIconLoader = new SoftManagerIconLoader(this, mFreezedApps);
        mSoftManagerIconLoader.loadAppIcon(new SoftManagerIconLoader.ImageLoadCompleteCallback() {
            @Override
            public void loadComplete() {
                mFreezedApps = (List<FreezeAppInfo>) mSoftManagerIconLoader.getListDatas();
                mSearchBox.clearSearchable();
                setSearchResultList();
            }
        });
    }

    public void setSearchResultList(){
        for (int i = 0; i < mFreezedApps.size(); i++){
            ItemInfo info = mFreezedApps.get(i);
            // Modify by zhiheng.huang on 2020/1/2 for EJQQ-2203 start
            Drawable drawable = info.getIcon();
            /*String [] customIcons = getResources().getStringArray(com.android.internal.R.array.icons);
            if (customIcons != null && customIcons.length > 0) {
                for (String s : customIcons) {
                    if (s.split("@")[1].contains(info.getPackageName())) {
                        int id = getResources().getIdentifier(s.split("@")[0], "drawable", "android");
                        drawable = getResources().getDrawable(id);
                    }
                }
            }*/
            SearchResult option = new SearchResult(info.getTitle(), drawable, i);
            // Modify by zhiheng.huang on 2020/1/2 for EJQQ-2203 end
            mSearchBox.addSearchable(option);
        }
    }

    private void showEmptyView() {
        mSearchBox.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mHeaderTextView.setVisibility(View.GONE);
    }

    private void updateHeader(int size) {
        String headFormat = mRes.getString(R.string.freezed_app_head_text);
        String headStr = String.format(headFormat, size);
        mHeaderTextView.setText(headStr);
        mSearchBox.setLogoText(headStr);
    }

    @Override
    public void onLocalChange() {
        refreshList();
    }

    private static class OnClickListener implements View.OnClickListener {
        FreezedAppActivity mFreezedAppActivity;

        void setActivity(FreezedAppActivity freezedAppActivity) {
            mFreezedAppActivity = freezedAppActivity;
        }

        @Override
        public void onClick(View v) {
            if (null == mFreezedAppActivity) {
                return;
            }

            String packageName = mFreezedAppActivity.getPackageName(v);
            String title = mFreezedAppActivity.getPackageTitle(v);
            String msgFormatStr = mFreezedAppActivity.mRes.getString(R.string.unfreeze_app_toast_format_str);
            String msg = String.format(msgFormatStr, title);
            mFreezedAppActivity.mFreezeAppManager.unFreezeApp(packageName);
            mFreezedAppActivity.refreshList();
            GNToast.showToast(mFreezedAppActivity, msg);
        }
    }

    private String getPackageName(View v) {
        int pos = (Integer) v.getTag();
        if (mFreezedApps.size() <= pos) {
            return "";
        }
        FreezeAppInfo info = mFreezedApps.get(pos);
        return info.getPackageName();
    }

    private String getPackageTitle(View v) {
        int pos = (Integer) v.getTag();
        if (mFreezedApps.size() <= pos) {
            return "";
        }
        FreezeAppInfo info = mFreezedApps.get(pos);
        return info.getTitle();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, FreezeAppActivity.class);
        startActivity(intent);
    }
}
