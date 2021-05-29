package com.cydroid.softmanager.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;

import com.cydroid.softmanager.R;

import cyee.widget.CyeeListView;



public class ViewAnimatorUtils {
    private static final String TAG = "ViewAnimatorUtils";

    public static ValueAnimator changeViewHeightAnimator(
            final View view, int fromHeight, int toHeight, long duration, AnimatorCallback callback){
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        ValueAnimator viewHeightChangeAnim = ValueAnimator.ofInt(fromHeight, toHeight);
        viewHeightChangeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.height = (int) animation.getAnimatedValue();
                view.setLayoutParams(params);
            }
        });
        viewHeightChangeAnim.setDuration(duration);
        viewHeightChangeAnim.setInterpolator(new LinearInterpolator());
        viewHeightChangeAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                callback.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                callback.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return viewHeightChangeAnim;
    }

    public static ObjectAnimator animation(View view, String anim, float from, float to, int duration){
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, anim, from, to);
        animator.setDuration(duration);
        animator.start();
        return animator;
    }

    public static void listViewFlyInFromBottom(Context context, CyeeListView listView, AnimatorCallback callback){
        Animation animation = (Animation) AnimationUtils.loadAnimation(context, R.anim.list_check_over_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                callback.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                callback.onAnimationEnd();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        LayoutAnimationController lac = new LayoutAnimationController(animation);
        //lac.setDelay(0.1f);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
        listView.setLayoutAnimation(lac);
    }

    public interface AnimatorCallback {
        void onAnimationStart();
        void onAnimationEnd();
    }

    public static void cancelAnimator(Animator anim){
        if (anim == null) return;
        if (anim.isRunning()) {
            anim.cancel();
        }
    }

    public static void releaseAnimator(Animator anim){
        cancelAnimator(anim);
        anim = null;
    }

    public static void viewAlphaAnimator(final View view, float from, float to, long time,
                                         Animator.AnimatorListener listener){
        ValueAnimator showAnim = ValueAnimator.ofFloat(from, to);
        showAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                view.setAlpha(alpha);
            }
        });
        if (null != listener){
            showAnim.addListener(listener);
        }
        showAnim.setDuration(time);
        showAnim.start();
    }

    public static void startShowViewAlphaAnimator(final View view, long time) {
        viewAlphaAnimator(view, 0, 1, time, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }
        });
    }

    public static void startHideViewAlphaAnimator(final View view, long time){
        viewAlphaAnimator(view, 1, 0, time, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
    }

    public static void rotateViewAnimator(View view, float from, float to, int duration, AnimatorCallback callback){
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "RotationX", from, to);
        anim.setDuration(duration);
        if (callback != null){
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    callback.onAnimationStart();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    callback.onAnimationEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        anim.start();
    }
}
