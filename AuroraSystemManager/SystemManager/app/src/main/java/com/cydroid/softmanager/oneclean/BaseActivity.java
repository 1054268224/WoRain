package com.cydroid.softmanager.oneclean;

import android.os.Bundle;
import android.view.MenuItem;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;

/**
 * File Description: 基础Activity抽象类，继承系统Activity,封装主题风格！
 *
 * @author: Gionee-lihq
 * @see: 2013-1-10 Change List:
 */
public abstract class BaseActivity extends CyeeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gionee <xuhz> <2013-12-11> delete for CR00967999 begin
        // setTheme(R.style.SystemManagerTheme);
        // Gionee <xuhz> <2013-12-11> delete for CR00967999 end
        super.onCreate(savedInstanceState);
        setActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void setActionBar() {
        CyeeActionBar bar = getCyeeActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    protected boolean getHasBackOption() {
        return true;
    }

}
