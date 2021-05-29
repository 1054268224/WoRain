/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.oneclean;

import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.monitor.service.ScreenOffCleanService;
import com.cydroid.softmanager.oneclean.adapter.WhiteListAdapter;
import com.cydroid.softmanager.oneclean.loader.ApplicationMrgLoader;
import com.cydroid.softmanager.oneclean.whitelist.WhiteAppInfo;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.softmanager.loader.SoftManagerIconLoader;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.changecolors.ChameleonColorManager;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import android.text.TextUtils;

public class WhiteListMrgActivity extends BaseListActivity<WhiteListAdapter>
        implements StateChangeCallback, LocalChangedCallback {
    private static final String TAG = "WhiteListMrgActivity";

    private static final String ACTION_UPDATE_USER_WHITE_LIST =
            "com.cydroid.softmanager.action.ACTION_UPDATE_USER_WHITE_LIST";

    private List<WhiteAppInfo> mData = new ArrayList<WhiteAppInfo>();
    private WhiteListAdapter mAdapter;
    private WhiteListManager mWhiteListManager;
    private final View.OnClickListener mOnClickListener = new OnClickListener();

    protected TextView mHeader;
    protected LinearLayout mLoaderView;
    protected RelativeLayout mContainer;
    protected TextView mLoadText;
    protected TextView mEmpty;
    protected LinearLayout mHeaderList;
    private final Handler mHandler = new Handler();
    private SearchBox mSearchBox;
    private int mSelectedItemOffset;
    private SoftManagerIconLoader mSoftManagerIconLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.OneCleanLightSecond);
        if (UiUtils.isSpecialStyleModel()) {
            setTheme(R.style.SystemManagerTheme);
        } else {
            setTheme(R.style.SystemManagerThemeCustom);
        }
        // Gionee xionghg 2017-06-14 modify for 156425 begin
        if (Consts.gnSwFlag) {
            super.onCreate(savedInstanceState, R.layout.sw_whitelistmrg_activity_layout);
        } else {
            super.onCreate(savedInstanceState, R.layout.whitelistmrg_activity_layout);
        }
        // Gionee xionghg 2017-06-14 modify for 156425 end
        mWhiteListManager = WhiteListManager.getInstance();
        initView();
        initActionBar();
        mHeaderList.setVisibility(View.GONE);
        getLoaderManager().initLoader(ApplicationMrgLoader.LOADER_WHITE_LIST, null, this);
        ChameleonColorManager.getInstance().onCreate(this);
        mWhiteListManager.setWhiteListChangeCallBack(String.valueOf(this.hashCode()), this);
        mWhiteListManager.setLocalChangeCallBack(String.valueOf(this.hashCode()), this);
        mSelectedItemOffset = getResources().getDimensionPixelSize(R.dimen.list_selected_item_offset);

        if (getIntent().getBooleanExtra("is_from_screen_off_clean", false)) {
            Log.d(TAG, "enter WhiteListMrgActivity from screenoff noti");
            MainProcessSettingsProviderHelper settingsHelper = new MainProcessSettingsProviderHelper(this);
            settingsHelper.putBoolean(ScreenOffCleanService.SHOULD_SHOW_WHITELIST_NOTI, false);
        }
        chameleonColorProcess();
    }

    private void chameleonColorProcess() {
        boolean isNeedChangeColor = ChameleonColorManager.isNeedChangeColor();
        if (isNeedChangeColor) {
            mSearchBox.changeSearchBoxBgColor();
        }
    }

    private void initView() {
        mLoaderView = (LinearLayout) findViewById(R.id.loader);
        mHeader = (TextView) findViewById(R.id.header_text);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mLoadText = (TextView) findViewById(R.id.load_text);
        mEmpty = (TextView) findViewById(R.id.text_empty);
        mHeaderList = (LinearLayout) findViewById(R.id.id_header);
        mSearchBox = findViewById(R.id.searchbox);
        // Add by HZH on 2019/6/17 for EJSL-1506 start
//        RelativeLayout relativeLayout = mSearchBox.findViewById(R.id.card_view);
//        layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
        // Add by HZH on 2019/6/17 for EJSL-1506 end
        mSearchBox.setSearchListener(new SearchBox.SearchListener(){

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                // Add by HZH on 2019/6/17 for EJSL-1506 start
//                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the mSearchBox term changing
                //Called after it has updated results
                // Add by HZH on 2019/6/17 for EJSL-1506 start
//                int size = mSearchBox.getResults().size();
//                layoutSearchBox(relativeLayout,
//                        !TextUtils.isEmpty(term) && size > 0 ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.CENTER_VERTICAL);
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
                // Add by HZH on 2019/6/17 for EJSL-1506 start
//                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

        });
    }

    private void initActionBar() {
        CyeeActionBar actionBar = getCyeeActionBar();
        int curMode = PowerModeUtils.getCurrentMode(this);
        actionBar.setDisplayHomeAsUpEnabled(curMode != PowerConsts.SUPER_MODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWhiteListManager.unsetWhiteListChangeCallBack(String.valueOf(this.hashCode()));
        mWhiteListManager.unsetLocalChangeCallBack(String.valueOf(this.hashCode()));
        mData.clear();
        ChameleonColorManager.getInstance().onDestroy(this);
        // Gionee: changph <2017-05-22> add for 146383 begin 
        Intent intent = new Intent(ACTION_UPDATE_USER_WHITE_LIST);
        this.sendBroadcast(intent);
        // Gionee: changph <2017-05-22> add for 146383 end 
    }

    @Override
    protected WhiteListAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new WhiteListAdapter(this, mData, mOnClickListener);
        }
        return mAdapter;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        refreshList();
    }

    private void refreshList() {
        List<WhiteAppInfo> userWhiteApps = mWhiteListManager.getUserWhiteAppsForUI();
        mLoaderView.setVisibility(View.GONE);
        mData.clear();
        mData.addAll(userWhiteApps);
        if (mData.size() > 0) {
            // mSearchBox.setVisibility(View.VISIBLE);
            mContainer.setVisibility(View.VISIBLE);
            mHeaderList.setVisibility(View.VISIBLE);
            mHeader.setText(mRes.getString(R.string.whitelist_remind_text));
            mEmpty.setVisibility(View.GONE);
        } else {
            mSearchBox.setVisibility(View.GONE);
            mContainer.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
            mEmpty.setText(mRes.getString(R.string.text_empty_white_list));
        }
        mAdapter.notifyDataSetChanged();setSearchResultList();
        mSoftManagerIconLoader = new SoftManagerIconLoader(this, mData);
        mSoftManagerIconLoader.loadAppIcon(new SoftManagerIconLoader.ImageLoadCompleteCallback() {
            @Override
            public void loadComplete() {
                mData = (List<WhiteAppInfo>) mSoftManagerIconLoader.getListDatas();
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

    public void oneClean(View view) {
        Intent intent = new Intent(this, WhiteListAddActivity.class);
        startActivity(intent);
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
    public void onLocalChange() {
        refreshList();
    }

    @Override
    public void onBackPressed() {
        int curMode = PowerModeUtils.getCurrentMode(this);
        if (curMode == PowerConsts.SUPER_MODE) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String packageName = getPackageName(v);
            mWhiteListManager.removeUserWhiteApp(packageName);
            refreshList();
        }
    }

    private String getPackageName(View v) {
        int pos = (Integer) v.getTag();
        if (mData.size() <= pos) {
            return "";
        }
        WhiteAppInfo info = mData.get(pos);
        return info.getPackageName();
    }
}
