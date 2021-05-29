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
import cyee.widget.CyeeTextView;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import com.cydroid.softmanager.applock.PasswordEntryKeyboardView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.applock.verifier.PasswordInputView;
import com.cydroid.softmanager.utils.Log;

public final class FourPwdViewController extends BasePwdViewController {
    private static final String TAG = "FourPwdViewController";

    private PasswordInputView mPasswordEntry;
    private PasswordEntryKeyboardView mKeyboardView;

    private final InputMethodManager mInputMethodManager;
    private final Runnable mShowSoftInputRunnable = new Runnable() {
        public void run() {
            mPasswordEntry.requestFocus();
            mInputMethodManager.showSoftInput(mPasswordEntry, 0);
        }
    };

    private final Runnable mHideSoftInputRunnable = new Runnable() {
        public void run() {
            mInputMethodManager.hideSoftInputFromWindow(
                mPasswordEntry.getWindowToken(), 0);
        }
    };

    private final PasswordInputView.onTextChangedLisenter mTextChangedLisenter =
        new PasswordInputView.onTextChangedLisenter() {
            @Override
            public void onTextChanged(){
                handleNext();
            }
    };
    
    private void handleNext() {
        final String pin = mPasswordEntry.getText().toString();
        if (null != pin && !pin.isEmpty()) {
            mAppLockViewVerifier.verifyPin(pin);
        }
    }
    
    public FourPwdViewController(Context context) {
        super(context);
        mInputMethodManager = (InputMethodManager)
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    protected int getRootViewIndex() {
        return R.layout.gn_confirm_lock_password_four;
    }

    protected void initViews(View view, final String packageName) {
        ViewGroup container = (ViewGroup) view.findViewById(R.id.password_entry_container);
        mPasswordEntry = new PasswordInputView(mContext);
        mPasswordEntry.setOnTextChangedLisenter(mTextChangedLisenter);
        mPasswordEntry.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        if (SecurityPwdUtils.useSecureKeyboardSupport(mContext)) {
            mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER 
                | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        }

        mPasswordEntry.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI 
            | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 
            LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        container.addView(mPasswordEntry, lp);

        mKeyboardView = (PasswordEntryKeyboardView) view.findViewById(R.id.keyboard);

        int backgroudColor = ChameleonColorManager.isNeedChangeColor() ? 
            ChameleonColorManager.getBackgroudColor_B1()
                : DEFAULT_BACKGROUND_COLOR;
        view.setBackgroundColor(backgroudColor);
    }

    @Override
    public void destroyAppLockView() {
    }

    @Override
    public void allFrozenShow(long pwdSurplusUnfrozenTick) {
        Log.d(TAG, "allFrozenShow");
        mPasswordEntry.setEnabled(false);
        mPasswordEntry.setText(null);
        mPasswordEntry.setVisibility(View.INVISIBLE);
        super.allFrozenShow(pwdSurplusUnfrozenTick);
    }

    @Override
    public void pwdVerifySuccessShow(boolean fpValid, boolean frValid) {
        Log.d(TAG, "pwdVerifySuccessShow");
        mPasswordEntry.setVisibility(View.VISIBLE);
        mPasswordEntry.post(mHideSoftInputRunnable);
        super.pwdVerifySuccessShow(fpValid, frValid);
    }

    @Override
    public void fpVerifySuccessShow() {
        Log.d(TAG, "fpVerifySuccessShow");
        mPasswordEntry.post(mHideSoftInputRunnable);
        super.fpVerifySuccessShow();
    }

    @Override
    public void frVerifySuccessShow() {
        Log.d(TAG, "frVerifySuccessShow");
        mPasswordEntry.post(mHideSoftInputRunnable);
        super.frVerifySuccessShow();
    }

    @Override
    public void allUsableShow(boolean fpValid, boolean frValid, boolean frFrozen) {
        Log.d(TAG, "allUsableShow");
        mPasswordEntry.setVisibility(View.VISIBLE);
        mPasswordEntry.setEnabled(true);
        mPasswordEntry.requestFocus();
        mPasswordEntry.post(mShowSoftInputRunnable);
        super.allUsableShow(fpValid, frValid, frFrozen);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }

        if (shouldAutoShowSoftKeyboard()) {
            mPasswordEntry.post(mShowSoftInputRunnable);
        } else {
            mPasswordEntry.post(mHideSoftInputRunnable);
        }
    }

    private boolean shouldAutoShowSoftKeyboard() {
        return mPasswordEntry.isEnabled();
    }

    public void pauseAppLockView() {
        mInputMethodManager.hideSoftInputFromWindow(
            mPasswordEntry.getWindowToken(), 0);
    }

    public void resumeAppLockView() {
        mPasswordEntry.requestFocus();
        mInputMethodManager.showSoftInput(mPasswordEntry, 0);
    }

    protected void clearPwdView() {
        mPasswordEntry.setText(null);
    }
}
