package com.cydroid.softmanager.softmanager;

import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.interfaces.ThemeChangedCallback;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.adapter.AutoStartAdapter;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppInfo;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppManager;
import com.cydroid.softmanager.softmanager.loader.SoftManagerIconLoader;
import com.cydroid.softmanager.softmanager.loader.SoftManagerLoader;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import android.text.TextUtils;
import android.widget.RelativeLayout;

public class AutoStartMrgActivity extends BaseListActivity<AutoStartAdapter>
        implements StateChangeCallback, LocalChangedCallback, ThemeChangedCallback {
    private static final String TAG = "AutoStartMrgActivity";

    private LinearLayout mLoaderView;
    private LinearLayout mEmptyView;
    private TextView mHeaderTextView;

    private List<AutoBootAppInfo> mData = new ArrayList<>();
    private AutoBootAppManager mAutoBootAppManager;

    private final SwitchChangeListener mSwitchChangeListener = new SwitchChangeListener();
    private final Handler mHandler = new Handler();
    private SearchBox mSearchBox;
    private int mSelectedItemOffset;
    private SoftManagerIconLoader mSoftManagerIconLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gionee xionghg 2017-06-14 modify for 156425 begin
        if (Consts.gnSwFlag) {
            super.onCreate(savedInstanceState, R.layout.sw_softmanager_activity_auto_start);
        } else {
            super.onCreate(savedInstanceState, R.layout.softmanager_activity_auto_start);
        }
        // Gionee xionghg 2017-06-14 modify for 156425 end
        mAutoBootAppManager = AutoBootAppManager.getInstance(this);
        initViewVisibility();
        getLoaderManager().initLoader(SoftManagerLoader.ID_LOADER_AUTO_BOOT, null, this);
        mAutoBootAppManager.setAutoBootAppsChangeCallBack(
                String.valueOf(this.hashCode()), this);
        mAutoBootAppManager.setLocalChangeCallBack(
                String.valueOf(this.hashCode()), this);
        mAutoBootAppManager.setThemeChangedCallback(
                String.valueOf(this.hashCode()), this);
        ChameleonColorManager.getInstance().onCreate(this);
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
        super.onResume();
        mAutoBootAppManager.init(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.setSwitchChangeListener(null);
        mListView.setAdapter(null);
        mSwitchChangeListener.setActivity(null);
        mAutoBootAppManager.unsetAutoBootAppsChangeCallBack(String.valueOf(this.hashCode()));
        mAutoBootAppManager.unsetLocalChangeCallBack(String.valueOf(this.hashCode()));
        mAutoBootAppManager.unsetThemeChangedCallback(String.valueOf(this.hashCode()));
        mData.clear();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    private void initViewVisibility() {
        mSwitchChangeListener.setActivity(this);
        mHeaderTextView = (TextView) findViewById(R.id.header_title);
        mLoaderView = (LinearLayout) findViewById(R.id.loader);
        mEmptyView = (LinearLayout) findViewById(R.id.empty_auto_start);
        mSearchBox = findViewById(R.id.searchbox);
        // Add by HZH on 2019/6/17 for EJSL-1506 start
        RelativeLayout relativeLayout = mSearchBox.findViewById(R.id.card_view);
        layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
        // Add by HZH on 2019/6/17 for EJSL-1506 end
        mLoaderView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mListView.setOnItemClickListener(null);
        mHeaderTextView.setVisibility(View.GONE);
        mSearchBox.setSearchListener(new SearchBox.SearchListener(){

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                // Add by HZH on 2019/6/17 for EJSL-1506 start
                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
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
                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

        });
    }

    @Override
    protected AutoStartAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new AutoStartAdapter(this, mData, mSwitchChangeListener);
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

    private void refreshList() {
        List<AutoBootAppInfo> enableList = mAutoBootAppManager.getEnableAutoBootApps();
        List<AutoBootAppInfo> disableList = mAutoBootAppManager.getDisableAutoBootApps();
        if (enableList.size() <= 0 && disableList.size() <= 0) {
            showEmptyView();
            return;
        }
        // mSearchBox.setVisibility(View.VISIBLE);
        mLoaderView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mData.clear();
        mData.addAll(enableList);
        mData.addAll(disableList);
        mHeaderTextView.setVisibility(View.GONE);
        updateHeader(enableList.size());
        mAdapter.notifyDataSetChanged();
        setSearchResultList();
        mSoftManagerIconLoader = new SoftManagerIconLoader(this, mData);
        mSoftManagerIconLoader.loadAppIcon(new SoftManagerIconLoader.ImageLoadCompleteCallback() {
            @Override
            public void loadComplete() {
                mData = (List<AutoBootAppInfo>) mSoftManagerIconLoader.getListDatas();
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
        mLoaderView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mHeaderTextView.setVisibility(View.GONE);
    }

    private void updateHeader(int size) {
        String headFormat = mRes.getString(R.string.auto_start_head_text);
        String headStr = String.format(headFormat, size);
        mHeaderTextView.setText(headStr);
    }

    private static class SwitchChangeListener implements CompoundButton.OnCheckedChangeListener {
        AutoStartMrgActivity mAutoStartMrgActivity;

        void setActivity(AutoStartMrgActivity autoStartMrgActivity) {
            mAutoStartMrgActivity = autoStartMrgActivity;
        }

        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if (null == mAutoStartMrgActivity) {
                return;
            }

            int pos = (Integer) view.getTag();
            if (mAutoStartMrgActivity.mData.size() <= pos) {
                return;
            }
            String pkgName = mAutoStartMrgActivity.mData.get(pos).getPackageName();

            if (HelperUtils
                    .getApplicationInfo(mAutoStartMrgActivity, pkgName) == null) {
                Toast.makeText(mAutoStartMrgActivity,
                        mAutoStartMrgActivity.mRes.getString(R.string.app_not_exit),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            final AutoBootAppInfo info = (AutoBootAppInfo) mAutoStartMrgActivity.mData.get(pos);
            if (isChecked) {
                mAutoStartMrgActivity.mAutoBootAppManager.enableAutoBootApp(info.getPackageName());
            } else {
                mAutoStartMrgActivity.mAutoBootAppManager.disableAutoBootApp(info.getPackageName());
            }
            info.setAutoBootState(isChecked);
            List<AutoBootAppInfo> enableList = mAutoStartMrgActivity.
                    mAutoBootAppManager.getEnableAutoBootApps();
            mAutoStartMrgActivity.updateHeader(enableList.size());
        }
    }

    @Override
    public synchronized void changeTheme(String category) {
        Log.d(TAG, "onStateChange");
        refreshList();
    }
}
