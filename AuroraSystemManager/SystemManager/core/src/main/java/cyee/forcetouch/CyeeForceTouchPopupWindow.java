package cyee.forcetouch;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.cyee.utils.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

@SuppressLint("NewApi")
public class CyeeForceTouchPopupWindow {

    private final static String LOGTAG = "CyeeForceTouchPopupWindow";
    private final static String MASK_VIEW_TAG = "mask_view";
    private final static int MASK_INIT_COLOR_ALPHA = 0x07;
    private final static int MASK_END_COLOR_ALPHA = 0x20;

    private final Context mCxt;
    private final WindowManager mWinMgr;
    private CyeeForceTouchWindowContainer mContainer;
    private boolean isShowing = false;
    private Drawable mBackground;
    private OnForceTouchPopupWindowCallBack mCallback;
    private ObjectAnimator mDismissAnimator;
    
    
    CyeeForceTouchPopupWindow(Context cxt) {
        mCxt = cxt;
        mWinMgr = (WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE);
    }

    boolean isShowing() {
        return isShowing;
    }

    void setBackground(Drawable drawable) {
        mBackground = drawable;
    }

    void showPopWindow(View v, int width, int height) {
        isShowing = true;
        CyeeForceTouchWindowContainer container = new CyeeForceTouchWindowContainer(
                mCxt);
        container.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        if (null == mBackground) {
            Bitmap bmp = CyeeForceTouchBackgroud.getBlurBitmap(mCxt,
                    CyeeForceTouchUtils.getCurrentScreenShot(mCxt), false);
            mBackground = new BitmapDrawable(bmp);
        }
        container.setBackground(mBackground);
        setBackgroundAlpha(0);
        removeAllViews();
        mContainer = container;

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width,
                height);
        addMaskBlackView();
        addViewToPopWindow(v, param);
        mWinMgr.addView(
                mContainer,
                getShowingParams(0, 0,
                        CyeeForceTouchUtils.getScreenWidth(mCxt),
                        CyeeForceTouchUtils.getScreenHeight(mCxt)));
    }

    void addViewToPopWindow(View v, LayoutParams params) {
        params.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        mContainer.addView(v, params);
    }

    void setBackgroundAlpha(int alpha) {
        if (null == mBackground) {
            return;
        }
        mBackground.setAlpha(alpha);

        float percent = (float) alpha / 255;

        setMaskBackgroundColor(percent);
    }

    void setMaskBlackView() {
        addMaskBlackView();
        setMaskBackgroundColor(1);
    }
    
    void removeContainerViews() {
        if (null != mContainer) {
            mContainer.removeAllViews();
        }
    }

    void dismissWithNoAnimation() {
        isShowing = false;
        resetPopupWindow();
    }
    
    void dismiss() {
        Log.e(LOGTAG, "dismiss start isShowing = " + isShowing + "; this="
                + this);
        if (isShowing()) {
            isShowing = false;
            mDismissAnimator = ObjectAnimator.ofFloat(mContainer, "alpha", 1f,
                    0f);
            mDismissAnimator
                    .setDuration(CyeeForceTouchConstant.DISMISS_WIN_ANIM_TIME);
            mDismissAnimator.addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animator arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animator arg0) {
                    Log.d(LOGTAG, "dismiss start onAnimationEnd");
                    mContainer.setAlpha(0f);
                    resetPopupWindow();
                }

                @Override
                public void onAnimationCancel(Animator arg0) {
                }
            });
            mDismissAnimator.start();
        } else {
            if (null != mDismissAnimator) {
                mDismissAnimator.cancel();
                mDismissAnimator = null;
            }
        }
    }

    void setPopupWindowCallback(OnForceTouchPopupWindowCallBack callback) {
        mCallback = callback;
    }

    private void resetPopupWindow() {
        removeAllViews();
        if(null != mCallback) {
            mCallback.dismissOver();
        }
    }
    
    private void addMaskBlackView() {
        if (null == mContainer
                || null != mContainer.findViewWithTag(MASK_VIEW_TAG)) {
            return;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                CyeeForceTouchUtils.getScreenWidth(mCxt),
                CyeeForceTouchUtils.getScreenHeight(mCxt));
        View view = new View(mCxt);
        view.setX(0);
        view.setY(0);
        view.setZ(-2);
        view.setTag(MASK_VIEW_TAG);
        mContainer.addView(view, params);
    }
    
    private void setMaskBackgroundColor(float percent) {
        if (null == mContainer) {
            return;
        }

        View view = mContainer.findViewWithTag(MASK_VIEW_TAG);

        if (null == view) {
            return;
        }

        percent = (float) percent * 100;

        long val = MASK_INIT_COLOR_ALPHA
                + (MASK_END_COLOR_ALPHA - MASK_INIT_COLOR_ALPHA)
                * (int) percent / 100;
        val <<= 24;

        view.setBackgroundColor((int) val);
    }
    
    private void removeAllViews() {
        try {
            if (null != mContainer) {
                mContainer.removeAllViews();
                mContainer.setVisibility(View.GONE);
                mWinMgr.removeView(mContainer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mContainer = null;
        }
    }
    
    private WindowManager.LayoutParams getShowingParams(int x, int y, int w,
            int h) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        // params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // params.windowAnimations = android.R.style.Animation_InputMethod;
        params.width = w;
        params.height = h;
        params.format = PixelFormat.RGBA_8888;
        params.x = x;
        params.y = y;
        params.gravity = Gravity.TOP | Gravity.START;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        return params;
    }

    private boolean isClickOutside(MotionEvent event) {
        boolean ret = true;
        if (null == mContainer) {
            return ret;
        }
        int cnt = mContainer.getChildCount();
        View view = null;
        Rect rect = new Rect();

        for (int i = 0; i < cnt; i++) {
            view = mContainer.getChildAt(i);
            if (MASK_VIEW_TAG.equals(view.getTag())) {
                continue;
            }
            view.getGlobalVisibleRect(rect);
            ret = rect.contains((int) event.getRawX(), (int) event.getRawY());
            if (ret) {
                return false;
            }
        }

        return true;
    }

    private class CyeeForceTouchWindowContainer extends FrameLayout {

        public CyeeForceTouchWindowContainer(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            Log.e(LOGTAG,
                    "dispatchKeyEvent onKeyDown keyCode=" + event.getKeyCode());
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (null != mCallback) {
                        mCallback.dismiss();
                    }
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();
            Log.e(LOGTAG,
                    "CyeeForceTouchWindowContainer onTouchEvent event.getAction()="
                            + event.getAction());
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isClickOutside(event)) {
                    if (null != mCallback) {
                        mCallback.dismiss();
                    }
                }
                break;
            default:
                break;
            }

            return super.onTouchEvent(event);
        }
    }

    public interface OnForceTouchPopupWindowCallBack {
        void dismiss();

        void dismissOver();
    }
}
