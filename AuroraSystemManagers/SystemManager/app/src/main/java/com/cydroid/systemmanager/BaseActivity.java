package com.cydroid.systemmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cydroid.softmanager.R;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import cyee.changecolors.ChameleonColorManager;

public class BaseActivity extends CyeeActivity {
    private LinearLayout mActionBarLayout;
    private LinearLayout mActionBarFirstLayout;
    private LinearLayout mActionBarSecondLayout;
    private CyeeActionBar mActionBar;

    private Class<?> mFirstClass;
    private Class<?> mSecondClass;

    private int mFirstLayoutVisibility = View.GONE;
    // Gionee <xionghg> <2017-05-09> add for 129667 begin
    public static final boolean gnODMflag = SystemProperties.get("ro.cy.oversea.odm").equals("yes");
    // Gionee <xionghg> <2017-05-09> add for 129667 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SystemManagerTheme);
        initActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isUsingCustomActionBar()) {
            setCustomActionBarView();
        }
    }

    public void finishTopActivity() {
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

    public void setFirstClass(Class<?> cls) {
        mFirstClass = cls;
    }

    public void setSecondClass(Class<?> cls) {
        mSecondClass = cls;
    }

    public void setFirstLayoutVisibility(int visibility) {
        mFirstLayoutVisibility = visibility;
    }

    public void setActionBarEnabled(boolean enabled) {
        mActionBarFirstLayout.setEnabled(enabled);
        mActionBarSecondLayout.setEnabled(enabled);
    }

    private void initActionBar() {
        mActionBar = getCyeeActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(isDisplayingHomeAsUp());

        mActionBarLayout = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.systemmanager_settings_actionbar, null);
        if (null != mActionBarLayout) {
            mActionBarFirstLayout = (LinearLayout) mActionBarLayout.findViewById(R.id.first_click_field);
            mActionBarSecondLayout = (LinearLayout) mActionBarLayout.findViewById(R.id.second_click_field);
        }
    }

    private void setCustomActionBarView() {
        ImageView img_button = (ImageView) mActionBarLayout.findViewById(R.id.img_actionbar_custom);
        ImageView img_another_button = (ImageView) mActionBarLayout.findViewById(R.id.img_another_button);

        mActionBarFirstLayout.setVisibility(mFirstLayoutVisibility);
        mActionBarSecondLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSecondClass != null) {
                    Intent intent = new Intent();
                    intent.setClass(BaseActivity.this, mSecondClass);
                    startActivity(intent);
                }
            }
        });

        mActionBarFirstLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mFirstClass != null) {
                    Intent intent = new Intent(BaseActivity.this, mFirstClass);
                    startActivity(intent);
                    finishTopActivity();
                }
            }

        });
        img_button.setImageResource(R.drawable.main_actionbar_setting);
        img_another_button.setImageResource(R.drawable.deep_rubbish_actionbar);

        // Gionee <houjie> <2015-11-13> add for CR01565278 begin
        if (ChameleonColorManager.isNeedChangeColor()) {
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            if (img_button.getDrawable() != null) {
                img_button.getDrawable().setTint(color_T1);
            }
            if (img_another_button.getDrawable() != null) {
                img_another_button.getDrawable().setTint(color_T1);
            }
        }
        // Gionee <houjie> <2015-11-13> add for CR01565278 end

        CyeeActionBar.LayoutParams params = new CyeeActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        mActionBar.setCustomView(mActionBarLayout, params);
        mActionBar.setDisplayShowCustomEnabled(true);
    }
}