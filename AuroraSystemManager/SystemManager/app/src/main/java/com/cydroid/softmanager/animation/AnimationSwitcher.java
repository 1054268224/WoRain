package com.cydroid.softmanager.animation;

import java.util.ArrayList;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.cydroid.softmanager.utils.Log;

public class AnimationSwitcher {

    private static final float SCALE_STEP = 0.005f;
    private static final float ALPHA_STEP = 0.009f;
    private final ArrayList<Float> mFromAlpha1 = new ArrayList<Float>();
    private final ArrayList<Float> mFromScaleX1 = new ArrayList<Float>();
    private final ArrayList<Float> mFromTranslateY1 = new ArrayList<Float>();
    private boolean mAlphaFlag = false;
    private boolean mTransFlag = false;
    private boolean mScaleFlag = false;
    private boolean yAlphaDir = false;
    private boolean yScaleDir = false;
    private boolean yTransDir = false;
    private int yAlphaDiff = 0;
    private int yScaleDiff = 0;
    private int yTransDiff = 0;
    private int mDuration = 1;

    public AnimationSwitcher(int num) {
        for (int i = 0; i < num; i++) {
            mFromAlpha1.add(i, 1.0f);
            mFromScaleX1.add(i, 1.0f);
            mFromTranslateY1.add(i, 0f);
        }
    }

    public void setFromAlpha(int index, float alpha) {
        if (index > mFromAlpha1.size()) {
            return;
        }

        mFromAlpha1.set(index, alpha);
    }

    public void setFromScaleX(int index, float scaleX) {
        if (index > mFromScaleX1.size()) {
            return;
        }

        mFromScaleX1.set(index, scaleX);
    }

    public void setFromTranslateY(int index, float translateY) {
        if (index > mFromTranslateY1.size()) {
            return;
        }

        mFromTranslateY1.set(index, translateY);
    }

    public float getFromAlpha(int index) {
        return mFromAlpha1.get(index);
    }

    public float getFromScaleX(int index) {
        return mFromScaleX1.get(index);
    }

    public float getFromTranslateY(int index) {
        return mFromTranslateY1.get(index);
    }

    public void setAlphaFlag(boolean flag) {
        mAlphaFlag = flag;
    }

    public void setTransFlag(boolean flag) {
        mTransFlag = flag;
    }

    public void setScaleFlag(boolean flag) {
        mScaleFlag = flag;
    }

    public void setDiffAlpha(int diff) {
        yAlphaDiff = diff;
    }

    public void setDiffScale(int diff) {
        yScaleDiff = diff;
    }

    public void setDiffTrans(int diff) {
        yTransDiff = diff;
    }

    public void setDirAlpha(boolean dir) {
        yAlphaDir = dir;
    }

    public void setDirScale(boolean dir) {
        yScaleDir = dir;
    }

    public void setDirTrans(boolean dir) {
        yTransDir = dir;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void startAnimations(View view, int index) {
        AnimationSet set = initAnimationSet(index);
        view.startAnimation(set);
    }

    public void startAnimations(View[] views, int[] array) {
        if (views.length != array.length) {
            return;
        }

        for (int i = 0; i < views.length; i++) {
            startAnimations(views[i], array[i]);
        }
    }

    public void startAnimations(ArrayList<View> views, ArrayList<Integer> array) {
        if (views.size() != array.size()) {
            return;
        }

        for (int i = 0; i < views.size(); i++) {
            startAnimations(views.get(i), array.get(i));
        }
    }

    public AnimationSet initAnimationSet(int index) {
        AlphaAnimation aA = alphaAnimationAction(index, yAlphaDiff, yAlphaDir, mAlphaFlag);
        TranslateAnimation tA = translateAnimationAction(index, yTransDiff, yTransDir, mTransFlag);
        ScaleAnimation sA = scaleAnimationAction(index, yScaleDiff, yScaleDir, mScaleFlag);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(aA);
        set.addAnimation(tA);
        set.addAnimation(sA);
        set.setDuration(mDuration);
        set.setFillAfter(true);
        return set;
    }

    public AlphaAnimation alphaAnimationAction(int index, int yDiff, boolean yDir, boolean isValid) {
        AlphaAnimation alphaA = null;
        if (!isValid) {
            alphaA = new AlphaAnimation(mFromAlpha1.get(index), mFromAlpha1.get(index));
        } else {
            alphaA = alphaAnimationAction(index, yDiff, yDir);
        }

        return alphaA;
    }

    private AlphaAnimation alphaAnimationAction(int index, int yDiff, boolean yDir) {
        float toAlpha = yDiff * ALPHA_STEP;
        if (!yDir) {
            toAlpha = 1 - toAlpha;
        }

        AlphaAnimation alphaA = new AlphaAnimation(mFromAlpha1.get(index), toAlpha);
        mFromAlpha1.set(index, toAlpha);
        return alphaA;
    }

    public TranslateAnimation translateAnimationAction(int index, int yDiff, boolean yDir, boolean isValid) {
        TranslateAnimation translateA = null;
        if (!isValid) {
            translateA = new TranslateAnimation(0, 0, mFromTranslateY1.get(index),
                    mFromTranslateY1.get(index));
        } else {
            translateA = translateAnimationAction(index, yDiff, yDir);
        }

        return translateA;
    }

    private TranslateAnimation translateAnimationAction(int index, int yDiff, boolean yDir) {
        float toTranslateY = -yDiff;
        TranslateAnimation translateA = new TranslateAnimation(0, 0, mFromTranslateY1.get(index),
                toTranslateY);
        mFromTranslateY1.set(index, toTranslateY);
        return translateA;
    }

    public ScaleAnimation scaleAnimationAction(int index, int yDiff, boolean yDir, boolean isValid) {
        ScaleAnimation scaleA = null;
        if (!isValid) {
            scaleA = new ScaleAnimation(mFromScaleX1.get(index), mFromScaleX1.get(index),
                    mFromScaleX1.get(index), mFromScaleX1.get(index), Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0f);
        } else {
            scaleA = scaleAnimationAction(index, yDiff, yDir);
        }

        return scaleA;
    }

    private ScaleAnimation scaleAnimationAction(int index, int yDiff, boolean yDir) {
        float endScale = yDiff * SCALE_STEP;
        if (!yDir) {
            endScale = 1.0f - endScale;
        }

        ScaleAnimation scaleA = new ScaleAnimation(mFromScaleX1.get(index), endScale,
                mFromScaleX1.get(index), endScale, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0f);
        mFromScaleX1.set(index, endScale);
        return scaleA;
    }

}
