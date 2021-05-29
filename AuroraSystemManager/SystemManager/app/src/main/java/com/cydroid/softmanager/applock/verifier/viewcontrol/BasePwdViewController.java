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

import cyee.widget.CyeeButton;
import cyee.widget.CyeeTextView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateXAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.applock.AppLockActivity;
import com.cydroid.softmanager.applock.AppLockUtils;
import com.cydroid.softmanager.applock.verifier.LifecycleAware;
import com.cydroid.softmanager.utils.Log;

public abstract class BasePwdViewController 
        implements AppLockViewController, LifecycleAware {
    private static final String TAG = "BasePwdViewController";

    protected static final long PROMPT_CLEAR_DELAY = 2000L;
    protected static final long PROMPT_SUCCESS_CLEAR_DELAY = 500L;
    protected static final int DEFAULT_BACKGROUND_COLOR = 0xfffdfdfd;

    protected String mPackageName;
    protected Context mContext;
    protected AppLockActivity mActivity;
    protected AppLockViewVerifier mAppLockViewVerifier;

    protected CyeeButton mCancelButton;
    protected ImageView mFpIcon;
    protected ImageView mFrIcon;
    protected ImageView mPwdIcon;
    protected TextView mHeaderTextView;
    protected TextView mForgetPwd;
    //protected View mRootView;
    
    private Animation mShake = null;
    private final ShowPromptRunnable mShowPromptRunnable = new ShowPromptRunnable();

    private class ShowPromptRunnable implements Runnable {
        private boolean fpValid;
        private boolean frValid;
            
        @Override
        public void run() {
            cancelShake();
            Log.d(TAG, "ShowPromptRunnable run fpValid:" + fpValid + ", frValid:" + frValid);
            String title = buildUsableText(fpValid, frValid);
            mHeaderTextView.setText(title);
        }
    }

    private final Runnable mClearPwdViewRunnable = new Runnable() {
        @Override
        public void run() {
            clearPwdView();
        }
    };

    private final Runnable mSuccessDismissRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != mActivity) {
                mActivity.dismiss(true);
            }
        }
    };

    private void cancelShake() {
        if (mShake != null) {
            mShake.cancel();
            mShake = null;
        }
    }

    protected String buildUsableText(boolean fpValid, boolean frValid) {
        StringBuilder sb = new StringBuilder();
        sb.append(mContext.getString(R.string.applock_verify_use_prompt));
        if (frValid) {
            sb.append(mContext.getString(R.string.applock_verify_fr_prompt));
        }
        if (fpValid) {
            sb.append(mContext.getString(R.string.applock_verify_fp_prompt));
        }
        sb.append(mContext.getString(R.string.applock_verify_pwd_prompt));
        Log.d(TAG, "buildUsableText:" + sb.toString());
        return sb.toString();
    }

    protected abstract void clearPwdView();

    public BasePwdViewController(Context context) {
        mContext = context;
        mActivity = (AppLockActivity)context;
    }

    @Override
    public void setAppLockViewVerifier(AppLockViewVerifier verifier) {
        mAppLockViewVerifier = verifier;
    }

    @Override
    public void unSetAppLockViewVerifier() {
        mAppLockViewVerifier = null;
    }

    @Override
    public View createAppLockView(String packageName) {
        Log.d(TAG, "createAppLockView packageName:" + packageName);
        mPackageName = packageName;
        View root = inflateView();
        initViews(root, mPackageName);
        onViewCreate(root);
        return root;
    }

    private View inflateView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int index = getRootViewIndex();
        return inflater.inflate(index, null);
    }

    protected abstract int getRootViewIndex();

    protected abstract void initViews(View view, final String packageName);

    private void onViewCreate(View view) {
        mCancelButton = (CyeeButton) view.findViewById(R.id.cancelButton);
        mFpIcon = (ImageView) view.findViewById(R.id.gnfingerprintimage);
        mFrIcon = (ImageView) view.findViewById(R.id.gnfacerecogimage);
        mPwdIcon = (ImageView) view.findViewById(R.id.gnpwdimage);
        mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
        mForgetPwd = (CyeeTextView) view.findViewById(R.id.forget_password);
        
        String packageTitle = AppLockUtils.getPackageTitle(mContext, mPackageName);
        TextView appHeaderTextView = (TextView) view.findViewById(R.id.appheaderText);
        String appHeadFormat = mContext.getString(R.string.lockpassword_confirm_app_header);
        String appHeadStr = String.format(appHeadFormat, packageTitle);
        appHeaderTextView.setText(appHeadStr);

        mForgetPwd.setVisibility(View.GONE);
        //mForgetPwd.setOnClickListener(mForgetListener);

        mCancelButton.setVisibility(View.GONE);
        //mRootView = view;
    }

    /*
    private final View.OnClickListener mForgetListener = new View.OnClickListener() {
        public void onClick(View v) {
            String bindInfo = getBindAccountInfo();
            if (bindInfo == null || bindInfo.length() == 0) {
                try {
                    Intent intent = new Intent();
                    Context context = mContext.createPackageContext("com.android.settings", 0);
                    intent.setClassName(context, "com.android.settings.securitypassword.GnForgetActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
//                    callOnConfirmCanceled();
                } catch (Exception e) {
                    com.cydroid.softmanager.utils.Log.e(TAG, "mForgetListener exception:" + e.toString());
                }
            } else {
                String[] contents = bindInfo.split(":");
                verifyCyeeAccount(contents[0], contents[1]);
            }
        }
    };

    private String getBindAccountInfo() {
        return mLockPatternUtils.gnGetString("bind_cyee_account");
    }

    private void verifyCyeeAccount(String userName, String uid) {
        GioneeAccount gioneeAccount = GioneeAccount.getInstance(mContext);
        LoginInfo info = new LoginInfo();
        info.setName(userName);
        info.setUid(uid);

        gioneeAccount.verify(mContext, info, new VerifyListener() {
            @Override
            public void onSucess(Object o) {
                try {
                    Intent intent = new Intent();
                    Context context = mContext.createPackageContext("com.android.settings", 0);
                    intent.setClassName(context, "com.android.settings.securitypassword.GnPasswordTypeActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
//                    callOnConfirmCanceled();
                } catch (Exception e) {
                    com.cydroid.softmanager.utils.Log.e(TAG, "verifyCyeeAccount exception:" + e.toString());
                }
            }

            @Override
            public void onCancel(Object o) {
            }
        });
    }
    */

    @Override
    public void allFrozenShow(long pwdSurplusUnfrozenTick) {
        Log.d(TAG, "allFrozenShow");
        mHeaderTextView.removeCallbacks(mShowPromptRunnable);
        cancelShake();
        setHeaderTextFrozenShow(pwdSurplusUnfrozenTick);
        updateFpIcon(false, R.drawable.applock_fp_normal);
        updateFrIcon(false, R.drawable.applock_fr_normal);
        updatePwdIcon(false, R.drawable.applock_pwd_normal);
        clearPwdViewImmediate();
    }

    private void setHeaderTextFrozenShow(long pwdSurplusUnfrozenTick) {
        String text = "";
        long sec = pwdSurplusUnfrozenTick / 1000;
        long min = sec / 60;
        if (min >= 1) {
            min += 1;
            text = mContext.getString(R.string.gn_securitypassword_retry_note, min);
        } else {
            text = mContext.getString(R.string.gn_securitypassword_retry_note2, sec);
        }
        mHeaderTextView.setText(text);
    }

    private void updateFpIcon(boolean fpValid, int resId) {
        if (fpValid) {
            mFpIcon.setVisibility(View.VISIBLE);
            mFpIcon.setBackgroundResource(resId);
        } else {
            mFpIcon.setVisibility(View.GONE);
        }
    }

    private void updateFrIcon(boolean frValid, int resId) {
        if (frValid) {
            mFrIcon.setVisibility(View.VISIBLE);
            mFrIcon.setBackgroundResource(resId);
        } else {
            mFrIcon.setVisibility(View.GONE);
        }
    }

    private void updatePwdIcon(boolean pwdValid, int resId) {
        if (pwdValid) {
            mPwdIcon.setVisibility(View.VISIBLE);
            mPwdIcon.setBackgroundResource(resId);
        } else {
            mPwdIcon.setVisibility(View.GONE);
        }
    }

    private void clearPwdViewImmediate() {
        mHeaderTextView.removeCallbacks(mClearPwdViewRunnable);
        mHeaderTextView.post(mClearPwdViewRunnable);
    }

    @Override
    public void fpFrozenPwdVerifyFailShow(
            boolean fpValid, boolean frValid, boolean frFrozen, int pwdSurplusTryCount) {
        Log.d(TAG, "fpFrozenPwdVerifyFailShow fpValid:" + fpValid + ", frValid:" + frValid);
        pwdVerifyFailShow(false, frValid, frFrozen, pwdSurplusTryCount);
    }

    @Override
    public void fpFrozenShow(boolean fpValid, boolean frValid, 
            boolean frFrozen, boolean isFirst, long fpSurplusUnfrozenTick) {
        Log.d(TAG, "fpFrozenShow fpValid:" + fpValid + ", frValid:"
            + frValid + ", frFrozen:" + frFrozen);
        cancelShake();
        if (isFirst) {
            String info = getFpFrozenHeaderText(fpSurplusUnfrozenTick);
            transitNotifyUser(info, PROMPT_CLEAR_DELAY, false, false, false, !frFrozen);
        } else {
            mHeaderTextView.removeCallbacks(mShowPromptRunnable);
            mHeaderTextView.setText(buildUsableText(false, !frFrozen));
        }
        updatePwdIcon(true, R.drawable.applock_pwd_normal);
        updateFpIcon(true, R.drawable.applock_fp_disable);
        updateFrIcon(frValid, frFrozen ? 
            R.drawable.applock_fr_disable : R.drawable.applock_fr_normal);
    }

    private String getFpFrozenHeaderText(long fpSurplusUnfrozenTick) {
        //long sec = fpSurplusUnfrozenTick / 1000;
        return mContext.getString(R.string.applock_verify_fp_fail_more, 30L);
    }

    protected void transitNotifyUser(String headerString, 
            long duration, boolean animate, boolean clearPasswordView,
            boolean fpValid, boolean frValid) {
        promptDuration(headerString, duration, fpValid, frValid);
        if (animate) {
            shake();
        }
        if (clearPasswordView) {
            clearPwdViewDelayed();
        }
    }

    private void promptDuration(String info, long duration, 
            boolean fpValid, boolean frValid) {
        mHeaderTextView.setText(info);
        mHeaderTextView.removeCallbacks(mShowPromptRunnable);
        mShowPromptRunnable.fpValid = fpValid;
        mShowPromptRunnable.frValid = frValid;
        Log.d(TAG, "promptDuration fpValid:" + fpValid + ", frValid:" + frValid);
        mHeaderTextView.postDelayed(mShowPromptRunnable, duration);
    }

    private void shake() {
        mShake = new TranslateXAnimation(0, 20);
        mShake.setDuration(300);
        mShake.setInterpolator(new CycleInterpolator(2));
        mHeaderTextView.startAnimation(mShake);
    }

    private void clearPwdViewDelayed() {
        mHeaderTextView.removeCallbacks(mClearPwdViewRunnable);
        mHeaderTextView.postDelayed(mClearPwdViewRunnable, PROMPT_SUCCESS_CLEAR_DELAY);
    }

    @Override
    public void pwdVerifySuccessShow(boolean fpValid, boolean frValid) {
        Log.d(TAG, "pwdVerifySuccessShow fpValid:" + fpValid + ", frValid:" + frValid);
        mHeaderTextView.removeCallbacks(mShowPromptRunnable);
        promptSuccessTxt();
        promptPwdSuccessIcon(fpValid, frValid);
        postSuccessDismiss();
    }

    private void promptSuccessTxt() {
        String info = mContext.getString(R.string.applock_verify_success_prompt);
        mHeaderTextView.setText(info);
    }

    private void promptPwdSuccessIcon(boolean fpValid, boolean frValid) {
        updatePwdIcon(fpValid || frValid, R.drawable.applock_pwd_highlight);
    }

    private void postSuccessDismiss() {
        // add for android o applock begin
        if (mActivity != null) {
            mActivity.mUnlockSuccess = true;
        }
        // add for android o applock end
        mHeaderTextView.removeCallbacks(mSuccessDismissRunnable);
        mHeaderTextView.postDelayed(mSuccessDismissRunnable, PROMPT_SUCCESS_CLEAR_DELAY);
    }

    @Override
    public void fpVerifySuccessShow() {
        Log.d(TAG, "fpVerifySuccessShow");
        mHeaderTextView.removeCallbacks(mShowPromptRunnable);
        promptSuccessTxt();
        promptFpSuccessIcon();
        postSuccessDismiss();
    }

    private void promptFpSuccessIcon() {
        updateFpIcon(true, R.drawable.applock_fp_highlight);
    }

    @Override
    public void pwdVerifyFailShow(
            boolean fpValid, boolean frValid, boolean frFrozen, int pwdSurplusTryCount) {
        Log.d(TAG, "pwdVerifyFailShow fpValid:" + fpValid + ", frValid:" + frValid
                + ", pwdSurplusTryCount:" + pwdSurplusTryCount);
        String info = "";
        if (pwdSurplusTryCount > 3) {
            info = mContext.getString(R.string.gn_securitypassword_retry);
        } else {
            info = mContext.getString(R.string.gn_securitypassword_input_error_note, 
                pwdSurplusTryCount);
        }
        
        transitNotifyUser(info, PROMPT_CLEAR_DELAY, true, true, fpValid, !frFrozen);
    }

    @Override
    public void fpVerifyFailShow(boolean fpValid, boolean frValid, 
            boolean frFrozen, int fpSurplusTryCount) {
        Log.d(TAG, "fpVerifyFailShow fpValid:" + fpValid + ", frValid:" + frValid + ", frFrozen:" + frFrozen
                + ", fpSurplusTryCount:" + fpSurplusTryCount);
        String info = "";
        // Gionee xionghg 2017-08-04 modify for 178296 begin
        if (fpSurplusTryCount > 3) {
            info = mContext.getString(R.string.gn_securitypassword_retry);
        } else {
            // info = mContext.getString(R.string.applock_verify_fr_input_error_prompt,
            //     fpSurplusTryCount);
            info = mContext.getString(R.string.gn_securitypassword_input_error_note,
                    fpSurplusTryCount);
        }
        // Gionee xionghg 2017-08-04 modify for 178296 end
        transitNotifyUser(info, PROMPT_CLEAR_DELAY, true, false, fpValid, !frFrozen);
    }

    @Override
    public void allUsableShow(boolean fpValid, boolean frValid, boolean frFrozen) {
        Log.d(TAG, "allUsableShow fpValid:" + fpValid 
            + ", frValid:" + frValid + ", frFrozen:" + frFrozen);
        cancelShake();
        mHeaderTextView.removeCallbacks(mShowPromptRunnable);
        mHeaderTextView.setText(buildUsableText(fpValid, !frFrozen));
        updatePwdIcon(fpValid || frValid, R.drawable.applock_pwd_normal);
        updateFpIcon(fpValid, R.drawable.applock_fp_normal);
        updateFrIcon(frValid, frFrozen ? 
            R.drawable.applock_fr_disable : R.drawable.applock_fr_normal);
    }

    @Override
    public void frVerifyPromptShow(boolean fpValid) {
        String info = mContext.getString(R.string.applock_verify_fr_wait_prompt);
        transitNotifyUser(info, PROMPT_CLEAR_DELAY, false, false, fpValid, true);
    }

    @Override
    public void frVerifyTimeoutShow(boolean fpValid) {
        String info = mContext.getString(R.string.applock_verify_fr_timeout_prompt);
        updateFrIcon(true, R.drawable.applock_fr_disable);
        transitNotifyUser(info, PROMPT_CLEAR_DELAY, false, false, fpValid, false);
    }

    @Override
    public void frVerifySuccessShow() {
        Log.d(TAG, "frVerifySuccessShow");
        mHeaderTextView.removeCallbacks(mShowPromptRunnable);
        promptSuccessTxt();
        promptFrSuccessIcon();
        postSuccessDismiss();
    }

    private void promptFrSuccessIcon() {
        updateFrIcon(true, R.drawable.applock_fr_highlight);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onWindowFocusChanged(boolean focus) {
    }

    public void transitNotifyUser(String headerString, 
            long duration, boolean animate, boolean clearPasswordView) {
        promptDuration(headerString, duration);
        if (animate) {
            shake();
        }
        if (clearPasswordView) {
            clearPwdViewDelayed();
        }
    }

    private void promptDuration(String info, long duration) {
        mHeaderTextView.setText(info);
        mHeaderTextView.removeCallbacks(mShowPromptRunnable);
        mHeaderTextView.postDelayed(mShowPromptRunnable, duration);
    }
}
