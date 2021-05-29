/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.viewcontrol;

import cyee.changecolors.ChameleonColorManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.applock.AppLockUtils;
import com.cydroid.softmanager.applock.verifier.auth.pwd.GnLockPatternUtils;
import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class PatternPwdViewController extends BasePwdViewController {
    private static final String TAG = "PatternPwdViewController";
    private static final String REFLECT_CLASS_NAME = 
        "com.android.internal.widget.LockPatternView";
    
    private final int mEffectiveUserId;
    private final GnLockPatternUtils mLockPatternUtils;

    private LockPatternView mLockPatternView;

    private final LockPatternView.OnPatternListener mConfirmExistingLockPatternListener
            = new LockPatternView.OnPatternListener() {
        public void onPatternStart() {
            //mControllerUI.cancelClear();         // when use input, cancel clear
        }

        public void onPatternCleared() {
            //mControllerUI.cancelClear();
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            Log.i(TAG, "onPatternDetected");
            mLockPatternView.setEnabled(false);
            startCheckPattern(pattern, null);
            return;
        }

        private void startCheckPattern(final List<LockPatternView.Cell> pattern,
                                       final Intent intent) {
            if (null == pattern 
                    || pattern.size() < LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                String info = mActivity.getString(R.string.lockpattern_need_to_unlock_wrong);
                transitNotifyUser(info, 800, false, true);
                return;
            }
            mAppLockViewVerifier.verifyPattern(pattern);
        }
    };

    public PatternPwdViewController(Context context) {
        super(context);
        mLockPatternUtils = new GnLockPatternUtils(mContext);
        mEffectiveUserId = AppLockUtils.getEffectiveUserId(mContext);
    }

    protected int getRootViewIndex() {
        return R.layout.gn_confirm_lock_pattern;
    }

    protected void initViews(View view, final String packageName) {
        mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern);
        try {
            Class<?> LockPatternViewClass = Class.forName(
                "com.android.internal.widget.LockPatternView", true, 
                view.getContext().getClassLoader());
            Field mRegularColorField = LockPatternViewClass.getDeclaredField("mRegularColor");
            Field mErrorColorField = LockPatternViewClass.getDeclaredField("mErrorColor");
            Field mSuccessColorField = LockPatternViewClass.getDeclaredField("mSuccessColor");
            mRegularColorField.setAccessible(true);
            mErrorColorField.setAccessible(true);
            mSuccessColorField.setAccessible(true);
            int regularColor = Color.parseColor("#444444");
            mRegularColorField.set(mLockPatternView, regularColor);
            // Chenyee xionghg 20170113 modify for CSW1705A-725 begin
            // 8.1上不设置默认为白色，改为cyee蓝和设置模块同步
            int successColor = Color.parseColor("#00a6ce");
            mSuccessColorField.set(mLockPatternView, successColor);
            // Chenyee xionghg 20170113 modify for CSW1705A-725 end
            int errorColor = Color.parseColor("#ff1800");
            mErrorColorField.set(mLockPatternView, errorColor);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        setDynamicCell(mLockPatternView, true);
        mLockPatternView.setTactileFeedbackEnabled(
                mLockPatternUtils.isTactileFeedbackEnabled());
        mLockPatternView.setInStealthMode(!mLockPatternUtils.isVisiblePatternEnabled(
                mEffectiveUserId));
        mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);

        int backgroudColor = ChameleonColorManager.isNeedChangeColor() ?
                ChameleonColorManager.getBackgroudColor_B1()
                : DEFAULT_BACKGROUND_COLOR;
        view.setBackgroundColor(backgroudColor);
    }

    private void setDynamicCell(LockPatternView view, boolean arg) {
        try {
            Class<?> cls = (Class<?>) Class.forName(REFLECT_CLASS_NAME);
            Method methodT = cls.getMethod("setDynamicCell", boolean.class);
            methodT.invoke(view, arg);
        } catch (Exception e) {
            Log.e(TAG, "setDynamicCell exception:" + e);
        }
    }

    @Override
    public void destroyAppLockView() {
    }

    @Override
    public void allFrozenShow(long pwdSurplusUnfrozenTick) {
        Log.d(TAG, "allFrozenShow");
        mLockPatternView.setEnabled(false);
        mLockPatternView.setVisibility(View.INVISIBLE);
        super.allFrozenShow(pwdSurplusUnfrozenTick);
    }

    @Override
    public void pwdVerifyFailShow(
            boolean fpValid, boolean frValid, boolean frFrozen, int pwdSurplusTryCount) {
        mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
        super.pwdVerifyFailShow(fpValid, frValid, frFrozen, pwdSurplusTryCount);
    }

    @Override
    public void pwdVerifySuccessShow(boolean fpValid, boolean frValid) {
        Log.d(TAG, "pwdVerifySuccessShow");
        mLockPatternView.setVisibility(View.VISIBLE);
        super.pwdVerifySuccessShow(fpValid, frValid);
    }

    @Override
    public void allUsableShow(boolean fpValid, boolean frValid, boolean frFrozen) {
        Log.d(TAG, "allUsableShow");
        mLockPatternView.setVisibility(View.VISIBLE);
        mLockPatternView.setEnabled(true);
        mLockPatternView.enableInput();
        super.allUsableShow(fpValid, frValid, frFrozen);
    }

    public void pauseAppLockView() {
    }

    public void resumeAppLockView() {
    }

    protected void clearPwdView() {
        mLockPatternView.setEnabled(true);
        mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
        mLockPatternView.clearPattern();
    }
}
