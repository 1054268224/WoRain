/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cyee.widget;

//Gionee <zhaoyulong> <2015-06-02> add for CR01490697 begin
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;

import com.cyee.internal.util.ReflectionUtils;
import com.cyee.internal.view.CyeeActionBarPolicy;
import com.cyee.internal.widget.tabhost.CyeePagerAdapter;
import com.cyee.internal.widget.tabhost.CyeeViewPager;
import com.cyee.internal.widget.tabhost.CyeeViewPager.OnPageChangeListener;

/**
 * Container for a tabbed window view. This object holds two children: a set of
 * tab labels that the user clicks to select a specific tab, and a ViewPager
 * object that displays the contents of that page. The individual elements are
 * typically controlled using this container object, rather than setting values
 * on the child elements themselves.
 *
 */
public class CyeeTabHost extends TabHost {

    private CyeeTabWidget mCyeeTabWidget;
    private FrameLayout mCyeeTabContent;
    private CyeeViewPager mViewPager;

    private CyeePagerAdapter mPagerAdapter;
    private final ArrayList<View> mPageList = new ArrayList<View>();

    private final Context mContext;

    private static final String TAG = "CyeeTabHost";

    public CyeeTabHost(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public CyeeTabHost(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.actionBarTabBarStyle);
        // TODO Auto-generated constructor stub
    }

    public CyeeTabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        // TODO Auto-generated constructor stub
    }

    public CyeeTabHost(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() {
        // TODO Auto-generated method stub
        mCyeeTabWidget = (CyeeTabWidget) findViewById(com.android.internal.R.id.tabs);
        ReflectionUtils.setFieldValue(this, "mTabWidget", mCyeeTabWidget);
        if (mCyeeTabWidget == null) {
            throw new RuntimeException(
                    "Your CyeeTabHost must have a TabWidget whose id attribute is 'android.R.id.tabs'");
        }
        mCyeeTabWidget
                .setTabSelectionListener(new CyeeTabWidget.OnTabSelectionChanged() {
                    public void onTabSelectionChanged(int tabIndex,
                            boolean clicked) {
                        setCurrentTab(tabIndex);
                    }
                });
        CyeeActionBarPolicy abp = CyeeActionBarPolicy.get(mContext);
        mCyeeTabWidget.getLayoutParams().height = abp.getTabContainerHeight();
        ReflectionUtils.setFieldValue(this, "mTabKeyListener",
                new OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                        case KeyEvent.KEYCODE_ENTER:
                            return false;

                        }
                        mCyeeTabContent.requestFocus(View.FOCUS_FORWARD);
                        return mCyeeTabContent.dispatchKeyEvent(event);
                    }

                });

        mCyeeTabContent = (FrameLayout) findViewById(com.android.internal.R.id.tabcontent);
        ReflectionUtils.setFieldValue(this, "mTabContent", mCyeeTabContent);
        if (mCyeeTabContent == null) {
            throw new RuntimeException(
                    "Your CyeeTabHost must have a FrameLayout whose id attribute is "
                            + "'android.R.id.tabcontent'");
        }
        mCyeeTabContent.removeAllViews();
        mViewPager = new CyeeViewPager(mContext);
        mCyeeTabContent.addView(mViewPager, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mPagerAdapter = new PagerAdapterImpl(mPageList);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListenerImpl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTab(TabSpec tabSpec) {
        // TODO Auto-generated method stub
        Object mIndicatorStrategy = ReflectionUtils.getFieldValue(tabSpec,
                "mIndicatorStrategy");
        Object mContentStrategy = ReflectionUtils.getFieldValue(tabSpec,
                "mContentStrategy");

        if (mIndicatorStrategy == null) {
            throw new IllegalArgumentException(
                    "you must specify a way to create the tab indicator.");
        }

        if (mContentStrategy == null) {
            throw new IllegalArgumentException(
                    "you must specify a way to create the tab content");
        }

        if (mContentStrategy.getClass().getName()
                .equals("android.widget.TabHost.ViewIdContentStrategy")) {
            throw new IllegalArgumentException(
                    "ViewIdContentStrategy is not supported by CyeeTabHost");
        }

        View tabIndicator = (View) ReflectionUtils.invokeMethod(
                mIndicatorStrategy, "createIndicatorView", null, null);
        tabIndicator.setOnKeyListener((OnKeyListener) ReflectionUtils
                .getFieldValue(this, "mTabKeyListener"));
        // remove the origin backgroundDrawable
        tabIndicator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        mCyeeTabWidget.addView(tabIndicator);
        @SuppressWarnings("unchecked")
        List<TabSpec> mTabSpecs = (List<TabSpec>) ReflectionUtils
                .getFieldValue(this, "mTabSpecs");
        mTabSpecs.add(tabSpec);

        View contentView = (View) ReflectionUtils.invokeMethod(
                mContentStrategy, "getContentView", null, null);
        if (contentView != null) {
            Log.v(TAG, "contentView type is "
                    + contentView.getClass().toString());
        }
        mPageList.add(contentView);
        mPagerAdapter.notifyDataSetChanged();
        if (mCurrentTab == -1) {
            setCurrentTab(0);
            mCurrentTab = 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentTab(int index) {
        // TODO Auto-generated method stub
        List<TabSpec> mTabSpecs = (List<TabSpec>) ReflectionUtils
                .getFieldValue(this, "mTabSpecs");
        if (index < 0 || index >= mTabSpecs.size()) {
            return;
        }
        if (index == mCurrentTab) {
            return;
        }
        mCurrentTab = index;
        mViewPager.setCurrentItem(index);
        Log.v(TAG, "setCurrentTab: " + index);
        ReflectionUtils.invokeMethod(CyeeTabHost.this,
                "invokeOnTabChangeListener", null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAllTabs() {
        mCyeeTabWidget.removeAllViews();
        mCyeeTabWidget.setIndexForSelection(0);
        ReflectionUtils.invokeMethod(this, "initTabHost", null, null);
        mPageList.clear();
        mPagerAdapter.notifyDataSetChanged();
        List<TabSpec> mTabSpecs = (List<TabSpec>) ReflectionUtils.getFieldValue(this, "mTabSpecs");
        mTabSpecs.clear();
        requestLayout();
        invalidate();
    }

    /**
     * 设置TabIndicator的颜色
     * 
     * @param color
     *            你要设置的颜色
     */
    public void setIndicatorBackgroundColor(int color) {
        mCyeeTabWidget.setIndicatorBackgroundColor(color);
    }

    private class PagerAdapterImpl extends CyeePagerAdapter {
        public ArrayList<View> pages;

        public PagerAdapterImpl(ArrayList<View> mListViews) {
            this.pages = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((CyeeViewPager) arg0).removeView(pages.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((CyeeViewPager) arg0).addView(pages.get(arg1), 0);
            return pages.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    private class OnPageChangeListenerImpl implements OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            //mCurrentTab = position;
            mCyeeTabWidget.focusCurrentTab(position);
            Log.v(TAG, "onPageSelected " + position);

        }

        @Override
        public void onPageScrolled(int position, float percent, int offset) {
            mCyeeTabWidget.onPageScrolled(position, percent, offset);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
// Gionee <zhaoyulong> <2015-06-02> add for CR01490697 end