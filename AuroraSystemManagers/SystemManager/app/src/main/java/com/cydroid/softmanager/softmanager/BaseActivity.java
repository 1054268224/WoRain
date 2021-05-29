package com.cydroid.softmanager.softmanager;

import cyee.app.CyeeActivity;
import cyee.app.CyeeActionBar;

import android.os.Bundle;
import android.os.SystemProperties;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.utils.UiUtils;
import java.util.Locale;
import android.content.Context;

public abstract class BaseActivity extends CyeeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UiUtils.isSpecialStyleModel()) {
            setTheme(R.style.SystemManagerTheme);
        } else {
            //setTheme(R.style.SystemManagerThemeCustom);
            //guoxt 20180412 modify for CSW1705AC-49 begin
            if(UiUtils.isRTLSysLanguage(this)) {
                setTheme(R.style.SystemManagerThemeCustomRtL);
            }else{
                setTheme(R.style.SystemManagerThemeCustom);
            }
            //guoxt 20180412 modify for CSW1705AC-49 end

        }
        setActionBar();
    }



    protected void setActionBar() {
        CyeeActionBar bar = getCyeeActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected boolean getHasBackOption() {
        return true;
    }
}
