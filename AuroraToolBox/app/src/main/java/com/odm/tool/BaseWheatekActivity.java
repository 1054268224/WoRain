package com.odm.tool;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BaseWheatekActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.transparent)));
        getSupportActionBar().setElevation(0.0f);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_action_arrow_back);
//        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        getWindow().setStatusBarColor(getColor(R.color.white));
//        getWindow().setBackgroundDrawable(new ColorDrawable(getColor(R.color.transparent)));
//        getWindow().setNavigationBarColor(ContextCompat.getColor(this,R.color.white));
        setFullscreen(false ,true,true);
    }

    public void setFullscreen(boolean isShowStatusBar, boolean isShowNavigationBar, boolean isShowActionBar) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (!isShowStatusBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (!isShowNavigationBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        if (!isShowActionBar) {
            getSupportActionBar().hide();
        }
        setNavigationStatusColor(Color.TRANSPARENT);
    }

    public void setNavigationStatusColor(int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(color);
            getWindow().setStatusBarColor(color);
        }
    }
}