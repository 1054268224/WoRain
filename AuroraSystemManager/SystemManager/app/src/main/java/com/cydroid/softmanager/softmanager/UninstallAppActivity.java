/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.softmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.interfaces.ThemeChangedCallback;
import com.cydroid.softmanager.interfaces.TimeChangedCallback;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.adapter.UninstallAppAdapter;
import com.cydroid.softmanager.softmanager.loader.SoftManagerIconLoader;
import com.cydroid.softmanager.softmanager.loader.SoftManagerLoader;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

public class UninstallAppActivity extends BaseListActivity<UninstallAppAdapter>
        implements TimeChangedCallback, StateChangeCallback, LocalChangedCallback, ThemeChangedCallback {
    private static final String TAG = "UninstallAppActivity";

    private TextView mHeaderTextView;
    private TextView mHeaderSummary;
    private LinearLayout mLoaderView;
    private View mEmptyView;

    private final UninstallAppManager mUninstallAppManager = UninstallAppManager.getInstance();
    private List<UninstallAppInfo> mUninstallApps = new ArrayList<>();
    private final View.OnClickListener mOnClickListener = new OnClickListener();
    private final Handler mHandler = new Handler();

    private boolean sortByUser = true;
    private SearchBox mSearchBox;
    private int mSelectedItemOffset;
    private SoftManagerIconLoader mSoftManagerIconLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        parseIntent();
        // Gionee xionghg 2017-06-14 modify for 156425 begin
        if (Consts.gnSwFlag) {
            super.onCreate(savedInstanceState, R.layout.sw_softmanager_activity_remove_package);
        } else {
            super.onCreate(savedInstanceState, R.layout.softmanager_activity_remove_package);
        }
        // Gionee xionghg 2017-06-14 modify for 156425 end
        /*initCustomActionBar();*/
        initViewVisibility();
        mUninstallAppManager.setAppsChangeCallBack(String.valueOf(this.hashCode()), this);
        mUninstallAppManager.setTimeChangeCallBack(String.valueOf(this.hashCode()), this);
        mUninstallAppManager.setLocalChangeCallBack(String.valueOf(this.hashCode()), this);
        mUninstallAppManager.setThemeChangedCallback(String.valueOf(this.hashCode()), this);
        // Gionee xionghg 2017-05-23 add for 146895 begin
        ChameleonColorManager.getInstance().onCreate(this);
        // Gionee xionghg 2017-05-23 add for 146895 end  
        getLoaderManager().initLoader(SoftManagerLoader.ID_LOADER_UNINSTALL, null, this);
        chameleonColorProcess();
        mSelectedItemOffset = getResources().getDimensionPixelSize(R.dimen.list_selected_item_offset);
    }

    private void chameleonColorProcess() {
        boolean isNeedChangeColor = ChameleonColorManager.isNeedChangeColor();
        if (isNeedChangeColor) {
            mSearchBox.changeSearchBoxBgColor();
        }
    }

    private void initCustomActionBar() {
        LinearLayout actionBar = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.systemmanager_settings_actionbar, null);

        initCustomActionBarFirstClick(actionBar);
        initCustomActionBarSecondClick(actionBar);

        CyeeActionBar.LayoutParams lp = new CyeeActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        //Gionee <jiangsj> <20170414> modify for 113656 begin
        lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        //Gionee <jiangsj> <20170414> modify for 113656 end
        /*getCyeeActionBar().setCustomView(actionBar, lp);
        getCyeeActionBar().setDisplayShowCustomEnabled(true);*/
        //getCyeeActionBar().setElevation(0);
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
        if (!sortByUser){
            second.setVisibility(View.GONE);
        }
        second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int showType = UninstallAppUtils.getUninstallShowType(UninstallAppActivity.this);
                new CyeeAlertDialog.Builder(UninstallAppActivity.this)
                        .setTitle(R.string.uninstall_sort)
                        .setSingleChoiceItems(R.array.uninstall_sort, showType,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i(TAG, "onClick which:" + which);
                                        UninstallAppUtils.setUninstallShowType(UninstallAppActivity.this, which);
                                        refreshList();
                                        dialog.dismiss();
                                    }
                                }).show();
            }
        });
    }

    private void initCustomActionBarSecondClickImg(ViewGroup actionBar) {
        ImageView img = (ImageView) actionBar.findViewById(R.id.img_actionbar_custom);
        img.setImageResource(R.drawable.softmanage_uninstall_sort);
        if (ChameleonColorManager.isNeedChangeColor()) {
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            if (img.getDrawable() != null) {
                img.getDrawable().setTint(color_T1);
            }
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();
        setIntentShowType(intent);

    }

    private void setIntentShowType(Intent intent) {
        final int INVALID_SHOW_TYPE_INDEX = -1;
        int showType = intent.getIntExtra("monitor", INVALID_SHOW_TYPE_INDEX);
        sortByUser = intent.getBooleanExtra("sortByUser", true);
        if (INVALID_SHOW_TYPE_INDEX != showType && sortByUser) {
            UninstallAppUtils.setUninstallShowType(this, showType);
        }
    }


    private void initViewVisibility() {
        mHeaderTextView = (TextView) findViewById(R.id.header_title);
        mHeaderSummary = (TextView) findViewById(R.id.header_summary);
        mLoaderView = (LinearLayout) findViewById(R.id.loader);
        mEmptyView =  findViewById(R.id.empty_remove_pacakge);
        mSearchBox = findViewById(R.id.searchbox);
        // Add by HZH on 2019/6/17 for EJSL-1506 start
        RelativeLayout relativeLayout = mSearchBox.findViewById(R.id.card_view);
        layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
        // Add by HZH on 2019/6/17 for EJSL-1506 end
        mLoaderView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mHeaderTextView.setVisibility(View.GONE);
        mHeaderSummary.setVisibility(View.GONE);
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
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to a result being clicked
                final int position = result.position;
                if (position != -1 && position < mUninstallApps.size()){
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUninstallAppManager.unsetAppsChangeCallBack(String.valueOf(this.hashCode()));
        mUninstallAppManager.unsetTimeChangeCallBack(String.valueOf(this.hashCode()));
        mUninstallAppManager.unsetLocalChangeCallBack(String.valueOf(this.hashCode()));
        mUninstallAppManager.unsetThemeChangedCallback(String.valueOf(this.hashCode()));
        mUninstallApps.clear();
        // Gionee xionghg 2017-05-23 add for 146895 begin
        ChameleonColorManager.getInstance().onDestroy(this);
        // Gionee xionghg 2017-05-23 add for 146895 end
    }

    @Override
    protected UninstallAppAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new UninstallAppAdapter(this, mUninstallApps, mOnClickListener, sortByUser);
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

    private void refreshList() {
        int showType = sortByUser? UninstallAppUtils.getUninstallShowType(this) : 2;
        Log.d(TAG, "showType = " + showType);
        List<UninstallAppInfo> uninstallApps = mUninstallAppManager.
                getAllUninstallAppsByShowType(showType);
        if (uninstallApps.isEmpty()) {
            showEmptyView();
            return;
        }
        // mSearchBox.setVisibility(View.VISIBLE);
        mUninstallApps.clear();
        mUninstallApps.addAll(uninstallApps);
        updateNormalShowViews(showType);
        mAdapter.notifyDataSetChanged();
        setSearchResultList();
        mSoftManagerIconLoader = new SoftManagerIconLoader(this, mUninstallApps);
        mSoftManagerIconLoader.loadAppIcon(new SoftManagerIconLoader.ImageLoadCompleteCallback() {
            @Override
            public void loadComplete() {
                mUninstallApps = (List<UninstallAppInfo>) mSoftManagerIconLoader.getListDatas();
                mSearchBox.clearSearchable();
                setSearchResultList();
            }
        });
    }

    public void setSearchResultList(){
        for (int i = 0; i < mUninstallApps.size(); i++){
            ItemInfo info = mUninstallApps.get(i);
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
        mHeaderSummary.setVisibility(View.GONE);
    }

    private void updateNormalShowViews(int showType) {
        int[] headerTexts = new int[]{R.string.uninstall_header_name,
                R.string.uninstall_header_time,
                R.string.uninstall_header_frequency,
                R.string.uninstall_header_size};

        if (showType >= 0 && showType < headerTexts.length) {
            mHeaderTextView.setText(headerTexts[showType]);
        }
        mHeaderSummary.setText(getResources().getString(R.string.uninstall_app_size,
                mUninstallApps.size()));

        mLoaderView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mHeaderTextView.setVisibility(View.VISIBLE);
        mHeaderSummary.setVisibility(View.VISIBLE);
    }

    public void onTimeChange() {
        Log.d(TAG, "onTimeChange");
        refreshList();
    }

    @Override
    public void onLocalChange() {
        Log.d(TAG, "onLocalChange");
        refreshList();
    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            UninstallAppInfo uninstallAppInfo = getUninstallAppInfo(v);
            if (uninstallAppInfo != null) {
                try {
                    Uri packageURI = Uri.parse("package:" + uninstallAppInfo.getPackageName());
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                    startActivity(uninstallIntent);
                } catch (Exception e) {
                    Log.e(TAG, "OnClickListener e:" + e);
                }
            }
        }
    }

    private UninstallAppInfo getUninstallAppInfo(View v) {
        int pos = (Integer) v.getTag();
        return mUninstallApps.get(pos);
    }

    @Override
    public synchronized void changeTheme(String category) {
        Log.d(TAG, "onStateChange");
        refreshList();
    }
}
