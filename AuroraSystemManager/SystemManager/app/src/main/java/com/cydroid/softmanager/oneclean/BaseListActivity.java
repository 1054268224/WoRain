package com.cydroid.softmanager.oneclean;

import cyee.widget.CyeeListView;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.oneclean.loader.ApplicationMrgLoader;
import com.cydroid.softmanager.model.ItemInfo;

public abstract class BaseListActivity<T extends CompositeAdapter<? extends ItemInfo>> extends BaseActivity
        implements LoaderCallbacks<Object> {
    protected CyeeListView mListView;
    protected T mAdapter;
    protected Resources mRes;

    protected void onCreate(Bundle savedInstanceState, int layoutResID) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResID);
        mRes = getResources();
        mListView = (CyeeListView) findViewById(R.id.listview);
        mAdapter = getAdapter();
        mListView.setAdapter(mAdapter);
    }

    protected abstract T getAdapter();

    @Override
    public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
        return new ApplicationMrgLoader(this);
    }

    @Override
    public void onLoaderReset(Loader<Object> arg0) {
    }

    // Add by HZH on 2019/6/17 for  start
    protected void layoutSearchBox(RelativeLayout relativeLayout, int verb) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(verb);
        relativeLayout.setLayoutParams(layoutParams);
    }
    // Add by HZH on 2019/6/17 for  end
}
