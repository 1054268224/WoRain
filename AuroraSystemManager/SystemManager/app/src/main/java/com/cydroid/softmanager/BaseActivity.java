package com.cydroid.softmanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cydroid.softmanager.utils.Log;
import com.cyee.utils.LogUtil;
import com.cydroid.softmanager.utils.UiUtils;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import cyee.changecolors.ChameleonColorManager;

public class BaseActivity extends CyeeActivity {
    private CyeeActionBar mActionBar;

    private Class<?> mFirstClass;
    private Class<?> mSecondClass;

    private int mFirstLayoutVisibility = View.GONE;
    private int mSecondLayoutVisibility = View.VISIBLE;

    private Intent mSecondIntent;
    private Intent mFirstIntent;

    protected ImageView mImg;

    private ImageView mOtherImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.SystemManagerThemeCustom);
        //guoxt 20180412 modify for CSW1705AC-49 begin
        if (UiUtils.isRTLSysLanguage(this)) {
            setTheme(R.style.SystemManagerThemeCustomRtL);
        } else {
            setTheme(R.style.SystemManagerThemeCustom);
        }
        // guoxt 20180412 modify for CSW1705AC-49 begin
        mActionBar = getCyeeActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(isDisplayingHomeAsUp());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isShowActionBar()) {
            if (isUsingCustomActionBar()) {
                mActionBar.setElevation(0);
                setCustomActionBarView();
            }
        } else {
            mActionBar.hide();
        }
    }

    public boolean isShowActionBar() {
        return true;
    }

    public boolean isDisplayingHomeAsUp() {
        return true;
    }

    public boolean isUsingCustomActionBar() {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem arg0) {
        onBackPressed();
        return super.onOptionsItemSelected(arg0);
    }

    // Gionee <yangxinruo> <2016-5-10> add for CR01658695 begin
    public void setSecondIntent(Intent intent) {
        mSecondIntent = intent;
    }

    public void setFirstIntent(Intent intent) {
        mFirstIntent = intent;
    }

    public void setSecondIcon(Drawable img) {
        if (mImg != null)
            mImg.setImageDrawable(img);
    }

    public void setFirstIcon(Drawable img) {
        if (mOtherImg != null)
            mOtherImg.setImageDrawable(img);
    }

    // Gionee <yangxinruo> <2016-5-10> add for CR01658695 end
    public void setSecondClass(Class<?> cls) {
        mSecondClass = cls;
    }

    public void setFirstClass(Class<?> cls) {
        mFirstClass = cls;
    }

    public void setFirstLayoutVisibility(int visibility) {
        mFirstLayoutVisibility = visibility;
    }

    public void setSecondLayoutVisibility(int visibility) {
        mSecondLayoutVisibility = visibility;
    }

    private void setCustomActionBarView() {
        // Gionee <yangxinruo> <2016-5-10> modify for CR01658695 begin
        LinearLayout group = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.systemmanager_settings_actionbar, null);
        LinearLayout first = (LinearLayout) group.findViewById(R.id.first_click_field);
        LinearLayout second = (LinearLayout) group.findViewById(R.id.second_click_field);
        mImg = (ImageView) group.findViewById(R.id.img_actionbar_custom);
        mImg.setImageResource(R.drawable.main_actionbar_setting);
        mOtherImg = (ImageView) group.findViewById(R.id.img_another_button);
        first.setVisibility(mFirstLayoutVisibility);
        second.setVisibility(mSecondLayoutVisibility);
        mOtherImg.setImageResource(R.drawable.power_summary_icon);
        // Gionee <houjie> <2015-11-13> add for CR01565278 begin
        if (ChameleonColorManager.isNeedChangeColor()) {
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            if (mImg.getDrawable() != null) {
                mImg.getDrawable().setTint(color_T1);
            }
            if (mOtherImg.getDrawable() != null) {
                mOtherImg.getDrawable().setTint(color_T1);
            }
        }
        // Gionee <houjie> <2015-11-13> add for CR01565278 end
        second.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSecondIntent != null) {
                    try {
                        startActivity(mSecondIntent);
                    } catch (Exception e) {
                        Log.d(this.getClass().getSimpleName(), "Can not startActivity:" + e.getMessage());
                    }
                } else if (mSecondClass != null) {
                    Intent intent = new Intent();
                    intent.setClass(BaseActivity.this, mSecondClass);
                    startActivity(intent);
                }
            }
        });
        // Gionee <yangxinruo> <2016-5-10> modify for CR01658695 end
        first.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Gionee <yangxinruo> <2016-5-10> modify for CR01658695 begin
                if (mFirstIntent != null) {
                    try {
                        startActivity(mFirstIntent);
                    } catch (Exception e) {
                        Log.d(this.getClass().getSimpleName(), "Can not startActivity:" + e.getMessage());
                    }
                } else if (mFirstClass != null) {
                    Intent intent = new Intent();
                    intent.setClass(BaseActivity.this, mFirstClass);
                    startActivity(intent);
                }
                // Gionee <yangxinruo> <2016-5-10> modify for CR01658695 end
            }
        });
        CyeeActionBar.LayoutParams params = new CyeeActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        //Gionee <jiangsj> <20170410> modify for 105924 begin
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        //Gionee <jiangsj> <20170410> modify for 105924 end
//        mActionBar.setCustomView(group, params);
        mActionBar.setDisplayShowCustomEnabled(true);
    }

    public CyeeActionBar getmActionBar() {
        return mActionBar;
    }

    public void setActionBarBackgroundColor(ColorDrawable drawable) {
        mActionBar.setBackgroundDrawable(drawable);
    }
}
