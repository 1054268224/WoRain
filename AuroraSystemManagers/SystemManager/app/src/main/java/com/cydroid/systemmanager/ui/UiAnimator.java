package com.cydroid.systemmanager.ui;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

public class UiAnimator {
    private ViewGroup.LayoutParams mLayoutParams;
    private static int ANIMATION_DURATION_MS = 500;

    public UiAnimator() {
        
    }

    public void changeLayoutHeight(final View view, int from, int to, AnimatorListenerAdapter listener) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        ValueAnimator vAnimator = ValueAnimator.ofInt(from, to);
        vAnimator.setDuration(ANIMATION_DURATION_MS);
        vAnimator.setInterpolator(new LinearInterpolator());
        vAnimator.start();
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.height = (int) animation.getAnimatedValue();
                view.setLayoutParams(params);
            }
        });
        vAnimator.addListener(listener);
    }

    public void changeLayoutHeight(final View view, int from, int to, int duration, AnimatorListenerAdapter listener) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        ValueAnimator vAnimator = ValueAnimator.ofInt(from, to);
        vAnimator.setDuration(duration);
        vAnimator.setInterpolator(new LinearInterpolator());
        vAnimator.start();
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.height = (int) animation.getAnimatedValue();
                view.setLayoutParams(params);
            }
        });
        vAnimator.addListener(listener);
    }

    public void changeTextAlpha(Object targetView, float from, float to) {
        ObjectAnimator oAnim = ObjectAnimator.ofFloat(targetView, "Alpha", from, to);
        oAnim.setDuration(ANIMATION_DURATION_MS);
        oAnim.setInterpolator(new LinearInterpolator());
        oAnim.start();
    }

    public void changeTextSize(final TextView targetView, float from, float to) {
        ValueAnimator vAnimator = ValueAnimator.ofFloat(from, to);
        vAnimator.setInterpolator(new LinearInterpolator());
        vAnimator.setDuration(ANIMATION_DURATION_MS);
        vAnimator.start();
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float size = (float) animation.getAnimatedValue();
                targetView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }
        });
    }

}
