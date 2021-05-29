package com.cydroid.softmanager.trafficassistant.actionBarTab;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.cydroid.softmanager.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActionBar.Tab;
import cyee.app.CyeeActivity;

//Gionee <jiangsj> <20170419> add for 113672 begin
//Gionee <jiangsj> <20170419> add for 113672 end

public abstract class ActionBarTabs extends CyeeActivity implements CyeeActionBar.TabListener {

    public List<View> mSections = new ArrayList<View>();
    public Context mContext;
    public ViewPager mViewPager;
    public MyPagerAdapter mMypagerAdapter;
    public CyeeActionBar mActionBar;
    public TabInfos mTabInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isSpecialStyleModel()) {
            setTheme(R.style.AppTabTheme);
        } else {
            setTheme(R.style.AppTabThemeCustom);
        }
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.tab_main_view);

        init();
    }

    public boolean isSpecialStyleModel() {
        if (SystemProperties.get("ro.product.model", "").equals("M2017")) {
            return true;
        }
        return SystemProperties.get("ro.gn.gnprojectid", "").equals("GBL8918");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.setAdapter(null);
    }

    private void init() {
        initActionBarInfo();
        createDummySection();
        actionBarAddTab();
        initPageAdapter();
    }

    private void initActionBarInfo() {
        mActionBar = getCyeeActionBar();
        mActionBar.setNavigationMode(CyeeActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setIndicatorBackgroundColor(Color.WHITE);
    }

    private void initPageAdapter() {
        mMypagerAdapter = new MyPagerAdapter();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mMypagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
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

        mViewPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mActionBar.onScrollToEnd(v, event);
                return false;
            }
        });

        mViewPager.setCurrentItem(mTabInfos.getTabCurrItem());
        //Gionee <jiangsj> <20170419> add for 113672 begin
        if (isLayoutRtl()) {
            mViewPager.setRotationY(180);
        }
        //Gionee <jiangsj> <20170419> add for 113672 end
    }

    // Chenyee xionghg 20170907 add for 203229 begin
    protected boolean isLayoutRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
    }
    // Chenyee xionghg 20170907 add for 203229 end

    private void createDummySection() {
        // mTabInfos = new TabInfos();
        mSections.clear();
        for (int i = 0; i < mTabInfos.getTabNums(); i++) {
            mSections.add(LayoutInflater.from(mContext).inflate(mTabInfos.getLayoutIds()[i], null));
        }

        for (int i = 0; i < mTabInfos.getTabNums(); i++) {
            initUI(i);
        }
    }

    private void actionBarAddTab() {
        for (int i = 0; i < mTabInfos.getTabNums(); i++) {
            mActionBar.addTab(mActionBar.newTab().setText(mTabInfos.getTabTexts()[i]).setTabListener(this));
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
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub
        if (mViewPager != null) {
            mViewPager.setCurrentItem(arg0.getPosition());
        }
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    public abstract void initUI(int children);

}