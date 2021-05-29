package com.cydroid.softmanager.softmanager;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;

import androidx.appcompat.app.AppCompatActivity;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.loader.SoftManagerLoader;

/**
 * File Description: 包含listView的activity基础类, 抽象出了listview数据的加载过程。
 *
 * @author: Gionee-lihq
 * @see: 2013-3-27 Change List:
 */
public abstract class BaseListActivity<T extends CompositeAdapter<? extends ItemInfo>> extends AppCompatActivity
        implements LoaderCallbacks<Object> {
    protected Resources mRes;

    protected T mAdapter;
    protected ListView mListView;

    protected void onCreate(Bundle savedInstanceState, int layoutResID) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResID);
        // getCyeeActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.common_blue_bg));
        mRes = getResources();

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = getAdapter();
        mListView.setAdapter(mAdapter);
    }

    protected abstract T getAdapter();

    @Override
    public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
        return new SoftManagerLoader(this);
    }

    @Override
    public void onLoaderReset(Loader<Object> arg0) {
    }

    // Add by HZH on 2019/6/17 for  start
    protected void layoutSearchBox(RelativeLayout relativeLayout, int verb) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(verb);
        relativeLayout.setLayoutParams(layoutParams);
    }
    // Add by HZH on 2019/6/17 for  end
}
