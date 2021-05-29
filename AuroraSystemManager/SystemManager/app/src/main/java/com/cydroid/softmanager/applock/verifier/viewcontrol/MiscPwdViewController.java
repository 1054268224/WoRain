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
import cyee.widget.CyeeButton;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.internal.widget.TextViewInputDisabler;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.utils.Log;

public final class MiscPwdViewController extends BasePwdViewController
        implements OnEditorActionListener, TextWatcher {
    private static final String TAG = "MiscPwdViewController";

    private CyeeButton mContinueButton;
    private TextView mPasswordEntry;
    private TextViewInputDisabler mPasswordEntryInputDisabler;
    
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
    
    public MiscPwdViewController(Context context) {
        super(context);
        mInputMethodManager = (InputMethodManager)
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    protected int getRootViewIndex() {
        return R.layout.gn_confirm_lock_password;
    }

    protected void initViews(View view, final String packageName) {
        mCancelButton = (CyeeButton) view.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.cancelTask();
            }
        });
        mContinueButton = (CyeeButton) view.findViewById(R.id.next_button);
        mContinueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNext();
            }
        });
        mContinueButton.setEnabled(false);

        mPasswordEntry = (TextView) view.findViewById(R.id.password_entry);
        mPasswordEntry.setOnEditorActionListener(this);
        mPasswordEntry.addTextChangedListener(this);
        if (SecurityPwdUtils.useSecureKeyboardSupport(mContext)) {
            mPasswordEntry.setInputType(
                InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
        }
        mPasswordEntry.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI 
            | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        
        mPasswordEntryInputDisabler = new TextViewInputDisabler(mPasswordEntry);

        int currentType = mPasswordEntry.getInputType();
        mPasswordEntry.setInputType(currentType);

		//guoxt 2017-07-24 modify for 172874  begin 
		long type = SecurityPwdUtils.getSecurityPasswordType();
		if(type == 2){
		   mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		   }
		//guoxt 2017-07-24 modify for 172874  end 
		
        int backgroudColor = ChameleonColorManager.isNeedChangeColor() ?
                ChameleonColorManager.getBackgroudColor_B1()
                : DEFAULT_BACKGROUND_COLOR;
        view.setBackgroundColor(backgroudColor);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mContinueButton.setEnabled(mPasswordEntry.getText().length() > 0);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Check if this was the result of hitting the enter or "done" key
        if (actionId == EditorInfo.IME_NULL
                || actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_NEXT) {
            handleNext();
            return true;
        }
        return false;
    }

    private void handleNext() {
        mPasswordEntryInputDisabler.setInputEnabled(false);

        final String pin = mPasswordEntry.getText().toString();
        if (null != pin && !pin.isEmpty()) {
            mAppLockViewVerifier.verifyPin(pin);
        } else {
            mPasswordEntryInputDisabler.setInputEnabled(true);
        }
    }

    @Override
    public void destroyAppLockView() {
    }

    @Override
    public void allFrozenShow(long pwdSurplusUnfrozenTick) {
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
        mPasswordEntryInputDisabler.setInputEnabled(true);
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
        super.onWindowFocusChanged(hasFocus);
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
        mPasswordEntryInputDisabler.setInputEnabled(true);
        mPasswordEntry.setText(null);
    }
}
