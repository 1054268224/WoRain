package com.cydroid.softmanager.utils;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.cydroid.softmanager.BaseActivity;

public class ColorUtils {

    private static final int COLOR_GOOD_STATE = Color.parseColor("#00A3E4");
    private static final int COLOR_MIDDLE_STATE = Color.parseColor("#F17C41");
    private static final int COLOR_TERRIBLE_STATE = Color.parseColor("#CA384C");

    public static int getCurrentColorByScore(int score){
        int color = 0;
        if (score >= 80) {
            color = COLOR_GOOD_STATE;
        } else if (score >= 70){
            color = COLOR_MIDDLE_STATE;
        } else {
            color = COLOR_TERRIBLE_STATE;
        }
        return color;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(BaseActivity activity, int statusColor) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(statusColor);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
            View statusBarView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(activity));
            statusBarView.setBackgroundColor(statusColor);
            contentView.addView(statusBarView, lp);
        }

    }

    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String getTextStringByColor(int color, String str){
        return "<font color='" + color + "'>" + str + "</font>";
    }

    public static void colorChangeAnim(View view, BaseActivity activity, final int startColor, final int fromColor) {
        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                int currentR = Color.red(startColor) - (Color.red(startColor) - Color.red(fromColor)) * progress / 100;
                int currentG = Color.green(startColor) - (Color.green(startColor) - Color.green(fromColor)) * progress / 100;
                int currentB = Color.blue(startColor) - (Color.blue(startColor) - Color.blue(fromColor)) * progress / 100;
                changeBackgroundColor(view, activity, Color.rgb(currentR, currentG, currentB));
            }
        });
        animator.setDuration(1500);
        animator.start();
    }

    public static void changeBackgroundColor(View view, BaseActivity activity, int color) {
        view.setBackgroundColor(color);
        activity.setActionBarBackgroundColor(new ColorDrawable(color));
        setStatusBarColor(activity, color);
    }
}
