package cyee.forcetouch;

import java.util.ArrayList;
import java.util.List;

import cyee.forcetouch.CyeeForceTouchMenuView.OnForceTouchMenuItemClickListener;
import cyee.forcetouch.CyeeForceTouchPopupWindow.OnForceTouchPopupWindowCallBack;
import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import com.cyee.utils.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class CyeeForceTouchQuickMenuController implements
        OnForceTouchMenuItemClickListener, OnForceTouchPopupWindowCallBack {

    private final static String LOGTAG = "CyeeForceTouchQuickMenuController";
    private final static int SHOW_MENU = 0;
    private final static int HIDE_MENU = 1;
    private final static int SCALE_BG_COLOR = 0x99ffffff;
    private final static int DEFAULT_VALUE = -1;

    private final Context mCxt;
    private final ArrayList<Object> mTouchViews = new ArrayList<Object>();

    private View mCurForceTouchView = null;
    private DisplayPos mDisplayPos;
    private final ForceTouchMenuInfo mForceTouchMenuInfo = new ForceTouchMenuInfo();
    private CyeeForceTouchPopupWindow mForceTouchWindow = null;
    private ArrayList<Object> mCancelTouchViews = new ArrayList<Object>();
    private CyeeForceTouchMenuCallback mMenuCallBack;
    private CyeeForceTouchClickCallback mForceTouchCallBack;
    private CyeeForceTouchControllerCallback mControllerCallBack;
    private CyeeForceTouchMenuView mCurMenuView;
    private CyeeForceTouchMenu mCurForceTouchMenu;

    private Object mObjRegister = new Object();
    private Bitmap mCurScreenShotBmp;
    private Bitmap mCurViewShotBmp;
    private View mCurViewShotView;
    private View mCurScaleView;
    private static final int SCALE_SIZE = 8;// dp
    private final static int PADDING = 8;

    private CyeeForceTouchState mTouchState = CyeeForceTouchState.NULL;
    private boolean isMenuAnimaling = false;
    private float mCurScaleX, mCurScaleY;
    private boolean isEnableForceTouch = true;
    private Point mCurViewPos = new Point(DEFAULT_VALUE, DEFAULT_VALUE);
    private Point mCurViewMargin = new Point();
    private float mCurPressure;
    private boolean isFullScreen;
    private ValueAnimator mShowMenuAnimator;
    private ValueAnimator mHideMenuAnimator;
    
    public CyeeForceTouchState getTouchState() {
        return this.mTouchState;
    }

    public enum DisplayPos {
        LB, RB, LT, RT, T, B
    }

    private Handler mHandler;

    private final OnTouchListener mTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            boolean ret = false;
            float pressure = 0;
            int action = event.getAction();

            pressure = event.getPressure();
            
            Log.d(LOGTAG, "onTouch mCurForceTouchView=" + mCurForceTouchView
                    + ";v=" + v + ";pressure=" + pressure + ";action=" + action
                    + ";getTouchState()=" + getTouchState()
                    + ";isMenuAnimaling=" + isMenuAnimaling
                    + ";isEnableForceTouch=" + isEnableForceTouch);
            
            if (!enableForceTouch() && action == MotionEvent.ACTION_DOWN) {
                return false;
            }

            if (isMenuAnimaling) {
                return true;
            }
            
            if (action == MotionEvent.ACTION_DOWN) {
                doActionDown(v, event);
                return false;
            }

            if (getTouchState() == CyeeForceTouchState.NULL) {
                return ret;
            }
            // Gionee <weidong> <2016-03-04> modify for CR01645308 begin
            if (mCurForceTouchView != v) {
                return false;
            }
            // Gionee <weidong> <2016-03-04> modify for CR01645308 end
            if (getTouchState().getValue() < CyeeForceTouchState.MID
                    .getValue() && action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
                if (pressure > CyeeForceTouchConfig.getInstance(mCxt).getLightThreshold()
                        && pressure < CyeeForceTouchConfig.getInstance(mCxt).getMidThreshold()) {
                    ret = doLightTouch(v, event);
                    if (!ret) {
                        return false;
                    }
                } else if (pressure >= CyeeForceTouchConfig.getInstance(mCxt)
                        .getMidThreshold()) {
                    ret = doForceTouch(v, event);
                    if (ret) {
                        return true;
                    }
                } else {

                }
            }

            switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ret = doActionUp(v, event);
                break;
            case MotionEvent.ACTION_MOVE:
                ret = doActionMove(v, event);
                break;
            default:
                break;
            }
            return ret;
        }
    };

    public CyeeForceTouchQuickMenuController(Context context) {
        mCxt = context;
    }

    public void setEnableForceTouch(boolean enable) {
        isEnableForceTouch = enable;
    }

    /*
     * 创建压感界面点击icon的副本控件，以及点击icon周围的放大控件
     */
    void doActionDown(View v, MotionEvent event) {
        Log.d(LOGTAG,"doActionDown mCurForceTouchView="+mCurForceTouchView+";v="+v);
        if (null != mCurForceTouchView) {
            return;
        }
        setTouchState(CyeeForceTouchState.PRESS);
        mCurForceTouchView = v;
        isFullScreen = CyeeForceTouchUtils.isFullScreen(mCxt);
    }

    public void cancelForceTouch(View view) {
        if (mCancelTouchViews.contains(view)) {
            return;
        }
        mCancelTouchViews.add(view);
    }

    public void registerForceTouchViews(final List<View> views) {
        synchronized (mObjRegister) {
            if (mTouchViews.containsAll(views)) {
                return;
            }
            for (View v : views) {
                if (!mTouchViews.contains(v)) {
                    v.setOnTouchListener(mTouchListener);
                    mTouchViews.add(v);
                }
            }
        }
    }

    public void registerForceTouchView(final View view) {
        synchronized (mObjRegister) {
            view.setOnTouchListener(mTouchListener);
            if (mTouchViews.contains(view)) {
                return;
            }
            mTouchViews.add(view);
        }
    }

    public void unregisterForceTouchView(View view) {
        synchronized (mObjRegister) {
            if (view == null) {
                mTouchViews.clear();
            } else {
                try {
                    view.setOnTouchListener(null);
                    mTouchViews.remove(view);
                    mCancelTouchViews.remove(view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void dismissOver() {
        isMenuAnimaling = false;
        resetValues();
    }

    public void onDestroy() {
        Log.d(LOGTAG, "onDestroy start");
        unregisterForceTouchView(null);
        clearCancelForceTouch();
    }

    public void setCyeeForceTouchMenuCallback(
            CyeeForceTouchMenuCallback listener) {
        this.mMenuCallBack = listener;
    }

    public CyeeForceTouchMenuCallback getCyeeForceTouchMenuCallback() {
        return this.mMenuCallBack;
    }

    public void setCyeeForceTouchClickCallback(
            CyeeForceTouchClickCallback listener) {
        this.mForceTouchCallBack = listener;
    }

    public CyeeForceTouchClickCallback getCyeeForceTouchClickCallback() {
        return this.mForceTouchCallBack;
    }

    public void setCyeeForceTouchControllerCallback(
            CyeeForceTouchControllerCallback callback) {
        this.mControllerCallBack = callback;
    }

    void setSynObj(Object object) {
        this.mObjRegister = object;
    }
    
    @Override
    public void onMenuItemClick(MenuItem menuItem) {
        if (mMenuCallBack != null) {
            if (null != menuItem) {
                mMenuCallBack.onForceTouchMenuItemClick(mCurForceTouchView,
                        menuItem);
            }
        }
        Log.d(LOGTAG,"onMenuItemClick menuItem ="+menuItem);
        dismiss();
    }

    public void dismiss() {
        Log.d(LOGTAG,"dismiss isMenuAnimaling="+isMenuAnimaling+";getTouchState().getValue()="+getTouchState().getValue());
        if (isMenuAnimaling) {
            return;
        }
        if (getTouchState().getValue() >= CyeeForceTouchState.MID.getValue()) {
            if (!isFullScreen) {
                CyeeForceTouchUtils.hideStatusBar(mCxt, false);
            }
        }
        boolean ret = hideMenuAnimation();
        if (!ret) {
            dismissWindow();
        }
    }

    public void dismissWithNoAnimation() {
        if (getTouchState().getValue() >= CyeeForceTouchState.MID.getValue()) {
            if (!isFullScreen) {
                CyeeForceTouchUtils.hideStatusBar(mCxt, false);
            }
        }
        
        if(null != mShowMenuAnimator) {
            mShowMenuAnimator.cancel();
            mShowMenuAnimator = null;
        }
        
        if(null != mHideMenuAnimator) {
            mHideMenuAnimator.cancel();
            mHideMenuAnimator = null;
        }
        
        if (null != mForceTouchWindow) {
            mForceTouchWindow.dismissWithNoAnimation();
            mForceTouchWindow = null;
        }
    }

    private boolean enableForceTouch() {
        boolean isSupport = CyeeForceTouchConfig.getInstance(mCxt).isSupportForceTouch();

        return isEnableForceTouch && isSupport;
    }
    
    private void mockLongClick(final View v) {
        if (mHandler == null) {
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    dismiss();
                    v.performLongClick();
                }
            }, CyeeForceTouchConstant.MOCK_LONG_PRESS_TIME);
        }
    }

    private void cancelMockLongClick() {
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    private boolean doForceTouch(View v, MotionEvent event) {
        boolean ret = false;
        setTouchState(CyeeForceTouchState.MID);
        CyeeForceTouchUtils.invokeVibrate(mCxt, CyeeForceTouchConstant.BUTTON_ON,
                new long[]{CyeeForceTouchConstant.VIBRATE_DELAY, CyeeForceTouchConstant.VIBRATE_TIME_SHORT}, 1);
        if (null != mForceTouchCallBack) {
            ret = mForceTouchCallBack.onForceTouchClick(v);
        }
        Log.d(LOGTAG,"doForceTouch start event="+event.getAction()+";ret="+ret);
        if (!ret) {
            cancelMockLongClick();
            if (null == mForceTouchWindow) {
                showPopWindow(v, event);
            }

            ret = isHasForceTouchMenu(v);

            if (!ret
                    || (null != mCurForceTouchMenu && mCurForceTouchMenu.size() <= 0)) {
                Log.d(LOGTAG, "doForceTouch mCurForceTouchMenu.size() is zero");
                CyeeForceTouchUtils.invokeVibrate(mCxt, CyeeForceTouchConstant.LOCKSCREEN_UNLOCK_CODE_ERROR,
                        new long[]{CyeeForceTouchConstant.VIBRATE_DELAY, CyeeForceTouchConstant.VIBRATE_TIME_SHORT}, 1);
                dismiss();
                return true;
            }

            if (!isFullScreen) {
                CyeeForceTouchUtils.hideStatusBar(mCxt, true);
            }
            
            showPopMenu();

            return true;
        }

        return ret;
    }

    private boolean doLightTouch(View v, MotionEvent event) {
        float pressure = event.getPressure();
        boolean ret = false;

        setTouchState(CyeeForceTouchState.LIGHT);
        if (null != mForceTouchCallBack) {
            ret = mForceTouchCallBack.onLightTouchClick(v, pressure);
        }
        if (ret) {
            return false;
        } else {
            if (mCancelTouchViews.contains(v)) {
                // 用户虽然注册了按压，但是在某种情况下用户又不想处理按压事件
                return false;
            }

            // 取消长按事件
            v.cancelLongPress();            

            // 显示高斯模糊背景
            showPopWindow(v, event);
        }
        
        if (pressure >= mCurPressure) {
            mockLongClick(v);
            mCurPressure = pressure;
        } else {
            // 0.15 为压力变化值
            if (pressure <= mCurPressure - 0.15) {
                cancelMockLongClick();
                mCurPressure = pressure;
            }
        }
        
        return true;
    }
    
    private void getForceTouchWindowViewInfo(View v) {
        Bitmap tmpCurFocusViewBitmap = null;

        if (null != mControllerCallBack) {
            mCurViewPos = new Point(DEFAULT_VALUE, DEFAULT_VALUE);
            mCurViewMargin = new Point();            
            tmpCurFocusViewBitmap = mControllerCallBack.getForceTouchView(v,
                    mCurViewPos, mCurViewMargin);
        }

        if (null == tmpCurFocusViewBitmap) {
            mCurViewShotBmp = CyeeForceTouchUtils.getViewBitmap(v);
        } else {
            mCurViewShotBmp = tmpCurFocusViewBitmap;
        }

        mCurViewShotView = createTouchViewShot();
        mCurScaleView = createTouchScaleView();
    }

    private boolean doActionMove(View v, MotionEvent event) {
        boolean ret = true;
        if (getTouchState().getValue() < CyeeForceTouchState.LIGHT.getValue()) {
            return false;
        }

        if (getTouchState().getValue() < CyeeForceTouchState.MID.getValue()) {
            return true;
        }

        // 获取真实的屏幕物理坐标
        Rect rect = CyeeForceTouchUtils.getViewRect(v);
        float x = 0, y = 0;
        x = event.getX();
        y = event.getY();
        x += rect.left;
        y += rect.top;
        event.setLocation(x, y);
        if (responseMenu()) {
            mCurMenuView.onTouchMenuItem(event);
        }

        return ret;
    }

    private boolean responseMenu() {
        return !isMenuAnimaling && null != mCurMenuView;
    }

    private boolean doActionUp(View v, MotionEvent event) {
        boolean ret = false;
        Log.d(LOGTAG, "doActionUp getTouchState().getValue()="
                + getTouchState().getValue());
        if (getTouchState().getValue() < CyeeForceTouchState.LIGHT.getValue()) {
            dismiss();
        } else if (getTouchState().getValue() < CyeeForceTouchState.MID
                .getValue()) {
            dismiss();
            ret = true;
        } else {
            if (responseMenu()) {
                Log.d(LOGTAG, "doActionUp end action=" + event.getAction());
                mCurMenuView.onTouchMenuItem(event);
            } else {
                dismiss();
            }
            ret = true;
        }

        return ret;
    }

    private void resetValues() {
        Log.d(LOGTAG, "resetValues start");
        
        if (null != mCurScaleView) {
            mCurScaleView = null;
        }
        if (null != mCurScreenShotBmp) {
            mCurScreenShotBmp.recycle();
            mCurScreenShotBmp = null;
        }
        mCurPressure = 0.0f;
        mCurMenuView = null;
        mCurForceTouchView = null;
        mCurForceTouchMenu = null;
        mCurViewPos.x = DEFAULT_VALUE;
        mCurViewPos.y = DEFAULT_VALUE;
        setTouchState(CyeeForceTouchState.NULL);
        cancelMockLongClick();
        isFullScreen = false;
    }

    private boolean isHasForceTouchMenu(View v) {
        if (null == mMenuCallBack) {
            return false;
        }

        if (null == mCurForceTouchMenu) {
            CyeeForceTouchMenu createMenu = new CyeeForceTouchMenu(mCxt);
            mMenuCallBack.onCreateForceTouchMenu(v, createMenu);
            mMenuCallBack.onPrepareForceTouchMenu(v, createMenu);
            mCurForceTouchMenu = createMenu;
        }

        return mCurForceTouchMenu.size() > 0;
    }
    
    private void clearCancelForceTouch() {
        if (null != mCancelTouchViews) {
            mCancelTouchViews.clear();
            mCancelTouchViews = null;
        }
    }
    
    private void showPopWindow(View dropView, MotionEvent event) {
        if (null != mForceTouchWindow && mForceTouchWindow.isShowing()) {
            if (getTouchState() == CyeeForceTouchState.LIGHT) {
                float pressure = 0;
                pressure = event.getPressure()
                        - (float) CyeeForceTouchConfig.getInstance(mCxt).getLightThreshold();
                setScaleViewScale(pressure);
                setPopWindowBackgroudAlpha(pressure);
            }
            return;
        }
        Log.d(LOGTAG,"showPopWindow start event="+event.getAction());
        getCurrentScreenShot();
        
        mForceTouchWindow = new CyeeForceTouchPopupWindow(mCxt);
        mForceTouchWindow.setPopupWindowCallback(this);

        Rect viewRect = null;

        if (mCurViewPos.x == DEFAULT_VALUE) {
            viewRect = CyeeForceTouchUtils.getViewRect(dropView);
        } else {
            viewRect = new Rect();
            viewRect.left = mCurViewPos.x;
            viewRect.top = mCurViewPos.y;
            viewRect.bottom = viewRect.top + mCurViewShotBmp.getHeight();
            viewRect.right = viewRect.left + mCurViewShotBmp.getWidth();
        }

        mForceTouchWindow
                .setBackground(new BitmapDrawable(getBackgroudBitmap()));

        mCurViewShotView.setX(viewRect.left);
        mCurViewShotView.setY(viewRect.top);
        mForceTouchWindow.showPopWindow(mCurViewShotView, viewRect.width(),
                viewRect.height());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                viewRect.width(), viewRect.height());
        mCurScaleView.setX(viewRect.left);
        mCurScaleView.setY(viewRect.top);
        mCurScaleView.setZ(-1f);
        mForceTouchWindow.addViewToPopWindow(mCurScaleView, params);
    }
    
    private void getCurrentScreenShot() {
        Bitmap tmpCurScreenShotBitmap = null;

        if (null != mControllerCallBack) {
            tmpCurScreenShotBitmap = mControllerCallBack.getBlurBitmap();
        }
        if (null == tmpCurScreenShotBitmap) {
            mCurScreenShotBmp = CyeeForceTouchUtils.getCurrentScreenShot(mCxt);
        } else {
            mCurScreenShotBmp = tmpCurScreenShotBitmap;
        }
        getForceTouchWindowViewInfo(mCurForceTouchView);
    }

    /*
     * 创建一个View，以当前按压view的背景图片为背景
     */
    private View createTouchViewShot() {
        ImageView view = new ImageView(mCxt);
        view.setClickable(true);
        LayoutParams params = new LayoutParams();
        params.width = mCurViewShotBmp.getWidth();
        params.height = mCurViewShotBmp.getHeight();
        view.setLayoutParams(params);
        view.setBackground(new BitmapDrawable(mCurViewShotBmp));

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mForceTouchCallBack) {
                    mForceTouchCallBack
                            .onForceTouchClickView(mCurForceTouchView);
                }
                Log.d(LOGTAG,"createTouchViewShot OnClickListener onClick");
                dismiss();
            }
        });

        return view;
    }

    private View createTouchScaleView() {
        ImageView view = new ImageView(mCxt);
        view.setClickable(true);
        LayoutParams params = new LayoutParams();
        params.width = mCurViewShotBmp.getWidth();
        params.height = mCurViewShotBmp.getHeight();
        view.setLayoutParams(params);
        view.setBackground(new BitmapDrawable(mCurViewShotBmp));
        view.setBackgroundTintList(ColorStateList.valueOf(SCALE_BG_COLOR));
        view.setBackgroundTintMode(Mode.SRC_IN);

        return view;
    }

    private void setScaleViewScale(float pressure) {
        float percent = (float) ((pressure * 100) / ((CyeeForceTouchConfig.getInstance(mCxt)
                .getMidThreshold() - CyeeForceTouchConfig.getInstance(mCxt)
                .getLightThreshold()) * 100));
        if (percent >= 1) {
            percent = 1;
        }

        int size = CyeeForceTouchUtils.dip2px(mCxt, SCALE_SIZE);
        float wScale = (float) ((float) size * 2 / mCurViewShotBmp.getWidth())
                * percent + 1;
        float hScale = (float) ((float) size * 2 / mCurViewShotBmp.getHeight())
                * percent + 1;
        mCurScaleView.setScaleX(wScale);
        mCurScaleView.setScaleY(hScale);
    }

    private Bitmap getBackgroudBitmap() {
        Bitmap bmp = CyeeForceTouchBackgroud.getBlurBitmap(mCxt,
                mCurScreenShotBmp, true);
        return bmp;
    }

    private void showPopMenu() {
        setMenuSort();
        CyeeForceTouchMenuView menuView = new CyeeForceTouchMenuView(
                mCurForceTouchView.getContext(), mCurForceTouchMenu,
                CyeeForceTouchConstant.MENU_TYPE_DESKTOP_QUICK_MENU);
        mCurMenuView = menuView;
        mCurMenuView.setOnForceTouchMenuItemClickListener(this);

        getForceTouchWindowPos(SHOW_MENU);

        mCurMenuView.setX(mForceTouchMenuInfo.mMenuXPos);
        mCurMenuView.setY(mForceTouchMenuInfo.mMenuYPos);
        mCurMenuView.setZ(-1f);

        Point p = CyeeForceTouchUtils.measureView(mCurMenuView);
        int width = CyeeForceTouchUtils.dip2px(mCxt,
                CyeeForceTouchConstant.DESK_TOP_MENU_WIDTH);

        int size = CyeeForceTouchUtils.dip2px(mCxt, SCALE_SIZE);

        Point iconP = new Point();
        iconP.x = mCurViewShotBmp.getWidth() - mCurViewMargin.x * 2;
        iconP.y = mCurViewShotBmp.getHeight() - mCurViewMargin.y * 2;
        
        float scaleX = (float) (iconP.x + size * 2) / width;
        float scaleY = (float) (iconP.y + size * 2) / p.y;

        mCurMenuView.setScaleX(scaleX);
        mCurMenuView.setScaleY(scaleY);
        mCurScaleX = scaleX;
        mCurScaleY = scaleY;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                p.y);

        mForceTouchWindow.setBackgroundAlpha(255);
        mForceTouchWindow.addViewToPopWindow(mCurMenuView, params);
        setScaleViewScale(0);
        showMenuAnimation();
    }
    
    class MenuAnimatorParams {
        public float mScaleX;
        public float mScaleY;
        public float mTranslationX;
        public float mTranslationY;
        public float mAlpha;
    }
    
    private MenuAnimatorParams createShowStartParams() {
        MenuAnimatorParams params = new MenuAnimatorParams();
        params.mScaleX = mCurScaleX;
        params.mScaleY = mCurScaleY;
        params.mTranslationX = mForceTouchMenuInfo.mStartX;
        params.mTranslationY = mForceTouchMenuInfo.mStartY;
        params.mAlpha = 0;

        return params;
    }
    
    private MenuAnimatorParams createShowEndParams() {
        MenuAnimatorParams params = new MenuAnimatorParams();
        params.mScaleX = 1;
        params.mScaleY = 1;
        params.mTranslationX = mForceTouchMenuInfo.mEndX;
        params.mTranslationY = mForceTouchMenuInfo.mEndY;
        params.mAlpha = 1;
        
        return params;
    }
    
    private MenuAnimatorParams createHideStartParams() {
        MenuAnimatorParams params = new MenuAnimatorParams();
        params.mScaleX = 1;
        params.mScaleY = 1;
        params.mTranslationX = mForceTouchMenuInfo.mStartX;
        params.mTranslationY = mForceTouchMenuInfo.mStartY;

        return params;
    }
    
    private MenuAnimatorParams createHideEndParams() {
        MenuAnimatorParams params = new MenuAnimatorParams();
        params.mScaleX = 0;
        params.mScaleY = 0;
        params.mTranslationX = mForceTouchMenuInfo.mEndX;
        params.mTranslationY = mForceTouchMenuInfo.mEndY;
        
        return params;
    }
    
    private float computeScaleX(MenuAnimatorParams start,
            MenuAnimatorParams end, float fraction) {
        float diff = end.mScaleX - start.mScaleX;

        return diff * fraction + start.mScaleX;
    }

    private float computeScaleY(MenuAnimatorParams start,
            MenuAnimatorParams end, float fraction) {
        float diff = end.mScaleY - start.mScaleY;

        return diff * fraction + start.mScaleY;
    }

    private float computeTranslationX(MenuAnimatorParams start,
            MenuAnimatorParams end, float fraction) {
        float diff = end.mTranslationX - start.mTranslationX;

        return diff * fraction + start.mTranslationX;
    }

    private float computeTranslationY(MenuAnimatorParams start,
            MenuAnimatorParams end, float fraction) {
        float diff = end.mTranslationY - start.mTranslationY;

        return diff * fraction + start.mTranslationY;
    }

    private float computeAlpha(MenuAnimatorParams start,
            MenuAnimatorParams end, float fraction) {
        float diff = end.mAlpha - start.mAlpha;

        return diff * fraction + start.mAlpha;
    }
    
    private void showMenuAnimation() {
        mShowMenuAnimator = new ValueAnimator();
        mShowMenuAnimator.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        MenuAnimatorParams startParams = createShowStartParams();
        MenuAnimatorParams endParams = createShowEndParams();
        mShowMenuAnimator.setObjectValues(startParams, endParams);
        mShowMenuAnimator.setEvaluator(new TypeEvaluator<MenuAnimatorParams>() {

            @Override
            public MenuAnimatorParams evaluate(float fraction,
                    MenuAnimatorParams startParams, MenuAnimatorParams endParams) {

                MenuAnimatorParams params = new MenuAnimatorParams();
                params.mScaleX = computeScaleX(startParams, endParams, fraction);
                params.mScaleY = computeScaleY(startParams, endParams, fraction);
                params.mTranslationX = computeTranslationX(startParams,
                        endParams, fraction);
                params.mTranslationY = computeTranslationY(startParams,
                        endParams, fraction);
                params.mAlpha = computeAlpha(startParams, endParams, fraction);

                return params;
            }
        });

        mShowMenuAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        MenuAnimatorParams params = (MenuAnimatorParams) animator
                                .getAnimatedValue();

                        mCurMenuView.setScaleX(params.mScaleX);
                        mCurMenuView.setScaleY(params.mScaleY);
                        mCurMenuView.setTranslationX(params.mTranslationX);
                        mCurMenuView.setTranslationY(params.mTranslationY);
                        mCurMenuView.setMenuViewAlpha(params.mAlpha);
                    }
                });

        mShowMenuAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                isMenuAnimaling = true;
                Log.d(LOGTAG, "showMenuAnimation onAnimationStart");
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                isMenuAnimaling = false;
                if(null != mCurMenuView) {
                    mCurMenuView.setMenuViewAlpha(1);
                }
                Log.d(LOGTAG, "showMenuAnimation onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                isMenuAnimaling = false;
            }
        });
        mShowMenuAnimator.start();
    }
    
    private boolean hideMenuAnimation() {
        boolean ret = true;
        Log.d(LOGTAG, "hideMenuAnimation start");
        if (null == mCurMenuView
                || mCurMenuView.getVisibility() != View.VISIBLE) {
            return false;
        }
        Log.d(LOGTAG, "hideMenuAnimation start 1");
        getForceTouchWindowPos(HIDE_MENU);
        mHideMenuAnimator = new ValueAnimator();
        mHideMenuAnimator.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        MenuAnimatorParams startParams = createHideStartParams();
        MenuAnimatorParams endParams = createHideEndParams();
        mHideMenuAnimator.setObjectValues(startParams, endParams);
        mHideMenuAnimator.setEvaluator(new TypeEvaluator<MenuAnimatorParams>() {

            @Override
            public MenuAnimatorParams evaluate(float fraction,
                    MenuAnimatorParams startParams, MenuAnimatorParams endParams) {

                MenuAnimatorParams params = new MenuAnimatorParams();
                params.mScaleX = computeScaleX(startParams, endParams, fraction);
                params.mScaleY = computeScaleY(startParams, endParams, fraction);
                params.mTranslationX = computeTranslationX(startParams,
                        endParams, fraction);
                params.mTranslationY = computeTranslationY(startParams,
                        endParams, fraction);

                return params;
            }
        });

        mHideMenuAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        MenuAnimatorParams params = (MenuAnimatorParams) animator
                                .getAnimatedValue();

                        mCurMenuView.setScaleX(params.mScaleX);
                        mCurMenuView.setScaleY(params.mScaleY);
                        mCurMenuView.setTranslationX(params.mTranslationX);
                        mCurMenuView.setTranslationY(params.mTranslationY);
                    }
                });

        mHideMenuAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                isMenuAnimaling = true;
                Log.d(LOGTAG, "hideMenuAnimation onAnimationStart");
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                isMenuAnimaling = false;
                dismissWindow();
                Log.d(LOGTAG, "hideMenuAnimation onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                isMenuAnimaling = false;
            }
        });
        mHideMenuAnimator.start();

        return ret;
    }

    private void setPopWindowBackgroudAlpha(float pressure) {
        float percent = (float) ((pressure * 100) / ((CyeeForceTouchConfig.getInstance(mCxt)
                .getMidThreshold() - CyeeForceTouchConfig.getInstance(mCxt)
                .getLightThreshold()) * 100));
        if (percent >= 1) {
            percent = 1;
        }
        mForceTouchWindow.setBackgroundAlpha((int) (255 * percent));
    }

    private void measureDisplayPos(Rect vRect) {
        int w, h;
        w = CyeeForceTouchUtils.getScreenWidth(mCxt);
        h = CyeeForceTouchUtils.getScreenHeight(mCxt);

        Point centerPos = new Point();

        centerPos.x = vRect.left + (vRect.width() + 1) / 2;
        centerPos.y = vRect.top + (vRect.height() + 1) / 2;

        if (centerPos.x < w / 2 && centerPos.y < h / 2) {
            mDisplayPos = DisplayPos.LB;
        } else if (centerPos.x > w / 2 && centerPos.y < h / 2) {
            mDisplayPos = DisplayPos.RB;
        } else if (centerPos.x > w / 2 && centerPos.y > h / 2) {
            mDisplayPos = DisplayPos.RT;
        } else if (centerPos.x < w / 2 && centerPos.y > h / 2) {
            mDisplayPos = DisplayPos.LT;
        } else/* if (vRect.left < w / 2 && vRect.right > w / 2) */{
            if (centerPos.y < h / 2) {
                mDisplayPos = DisplayPos.B;
            } else {
                mDisplayPos = DisplayPos.T;
            }
        }
    }

    private void setMenuSort() {
        if (null == mCurForceTouchMenu) {
            return;
        }

        Rect rect = getTouchViewRect();
        measureDisplayPos(rect);

        switch (mDisplayPos) {
        case LB:
        case RB:
        case B:
        default:
            mCurForceTouchMenu
                    .setMenuSort(CyeeForceTouchConstant.MENU_SORT_ORDER);
            break;
        case LT:
        case RT:
        case T:
            mCurForceTouchMenu
                    .setMenuSort(CyeeForceTouchConstant.MENU_SORT_REVERT_ORDER);
            break;
        }
    }
    
    private Rect getTouchViewRect() {
        Rect rect = null;
        if (mCurViewPos.x == DEFAULT_VALUE) {
            rect = CyeeForceTouchUtils.getViewRect(mCurForceTouchView);
        } else {
            rect = new Rect();
            rect.left = mCurViewPos.x;
            rect.top = mCurViewPos.y;
            rect.bottom = rect.top + mCurViewShotBmp.getHeight();
            rect.right = rect.left + mCurViewShotBmp.getWidth();
        }
        
        return rect;
    }
    
    private void getForceTouchWindowPos(int type) {

        Rect rect = getTouchViewRect();
        measureDisplayPos(rect);
        Point iconP = new Point();
        iconP.x = rect.width() - mCurViewMargin.x * 2;
        iconP.y = rect.height() - mCurViewMargin.y * 2;

        Point p = CyeeForceTouchUtils.measureView(mCurMenuView);
        int width = CyeeForceTouchUtils.dip2px(mCxt,
                CyeeForceTouchConstant.DESK_TOP_MENU_WIDTH);

        int padding = CyeeForceTouchUtils.dip2px(mCxt, PADDING);

        Log.d(LOGTAG, "getForceTouchWindowPos mDisplayPos=" + mDisplayPos
                + ";mCurViewMargin.y=" + mCurViewMargin.y
                + ";mCurViewMargin.x=" + mCurViewMargin.x + ";iconP.y="
                + iconP.y + ";iconP.x=" + iconP.x + ";rect.w=" + rect.width()
                + ";rect.h=" + rect.height());
        switch (mDisplayPos) {
        case LB:
            mCurMenuView.setPivotY(0);
            mCurMenuView.setPivotX(0);
            if (type == SHOW_MENU) {
                mForceTouchMenuInfo.mMenuXPos = rect.left - padding
                        + mCurViewMargin.x;
                mForceTouchMenuInfo.mMenuYPos = rect.top - padding
                        + mCurViewMargin.y;
                mForceTouchMenuInfo.mStartY = mForceTouchMenuInfo.mMenuYPos;
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mMenuYPos
                        + iconP.y + padding * 2;
                mForceTouchMenuInfo.mStartX = mForceTouchMenuInfo.mMenuXPos;
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mMenuXPos
                        + padding;
            } else {
                mForceTouchMenuInfo.mStartY = (int) mCurMenuView.getY();
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mStartY
                        - iconP.y / 2 - padding;
                mForceTouchMenuInfo.mStartX = (int) mCurMenuView.getX();
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mStartX
                        + iconP.x / 2;
            }
            break;
        case RB:
            mCurMenuView.setPivotY(0);
            mCurMenuView.setPivotX(width);
            if (type == SHOW_MENU) {
                mForceTouchMenuInfo.mMenuXPos = rect.right - width + padding
                        - mCurViewMargin.x;
                mForceTouchMenuInfo.mMenuYPos = rect.top - padding
                        + mCurViewMargin.y;
                mForceTouchMenuInfo.mStartY = mForceTouchMenuInfo.mMenuYPos;
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mMenuYPos
                        + iconP.y + padding * 2;
                mForceTouchMenuInfo.mStartX = mForceTouchMenuInfo.mMenuXPos;
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mMenuXPos
                        - padding;
            } else {
                mForceTouchMenuInfo.mStartY = (int) mCurMenuView.getY();
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mStartY
                        - iconP.y / 2 - padding;
                mForceTouchMenuInfo.mStartX = (int) mCurMenuView.getX();
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mStartX
                        - iconP.x / 2;
            }
            break;
        case LT:
            mCurMenuView.setPivotY(p.y);
            mCurMenuView.setPivotX(0);
            if (type == SHOW_MENU) {
                mForceTouchMenuInfo.mMenuXPos = rect.left - padding
                        + mCurViewMargin.x;
                mForceTouchMenuInfo.mMenuYPos = rect.bottom - p.y + padding
                        - mCurViewMargin.y;
                mForceTouchMenuInfo.mStartY = mForceTouchMenuInfo.mMenuYPos;
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mMenuYPos
                        - (iconP.y + padding * 2);
                mForceTouchMenuInfo.mStartX = mForceTouchMenuInfo.mMenuXPos;
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mMenuXPos
                        + padding;
            } else {
                mForceTouchMenuInfo.mStartY = (int) mCurMenuView.getY();
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mStartY
                        + iconP.y / 2 + padding;
                mForceTouchMenuInfo.mStartX = (int) mCurMenuView.getX();
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mStartX
                        + iconP.x / 2;
            }
            break;
        case RT:
            mCurMenuView.setPivotY(p.y);
            mCurMenuView.setPivotX(width);
            if (type == SHOW_MENU) {
                mForceTouchMenuInfo.mMenuXPos = rect.right - width + padding
                        - mCurViewMargin.x;
                mForceTouchMenuInfo.mMenuYPos = rect.bottom - p.y + padding
                        - mCurViewMargin.y;
                mForceTouchMenuInfo.mStartY = mForceTouchMenuInfo.mMenuYPos;
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mMenuYPos
                        - (iconP.y + padding * 2);
                mForceTouchMenuInfo.mStartX = mForceTouchMenuInfo.mMenuXPos;
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mMenuXPos
                        - padding;
            } else {
                mForceTouchMenuInfo.mStartY = (int) mCurMenuView.getY();
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mStartY
                        + iconP.y / 2 + padding;
                mForceTouchMenuInfo.mStartX = (int) mCurMenuView.getX();
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mStartX
                        - iconP.x / 2;
            }
            break;
        case T:
            mCurMenuView.setPivotY(p.y);
            mCurMenuView.setPivotX(width / 2);
            if (type == SHOW_MENU) {
                mForceTouchMenuInfo.mMenuXPos = rect.left + rect.width() / 2
                        - width / 2;
                mForceTouchMenuInfo.mMenuYPos = rect.bottom - p.y + padding
                        - mCurViewMargin.y;
                mForceTouchMenuInfo.mStartY = mForceTouchMenuInfo.mMenuYPos;
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mMenuYPos
                        - (iconP.y + padding * 2);
                mForceTouchMenuInfo.mStartX = mForceTouchMenuInfo.mMenuXPos;
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mMenuXPos;
            } else {
                mForceTouchMenuInfo.mStartY = (int) mCurMenuView.getY();
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mStartY
                        + iconP.y / 2 + padding;
                mForceTouchMenuInfo.mStartX = (int) mCurMenuView.getX();
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mStartX;
            }
            break;
        case B:
            mCurMenuView.setPivotY(0);
            mCurMenuView.setPivotX(width / 2);
            if (type == SHOW_MENU) {
                mForceTouchMenuInfo.mMenuXPos = rect.left + rect.width() / 2
                        - width / 2;
                mForceTouchMenuInfo.mMenuYPos = rect.top - padding
                        + mCurViewMargin.y;
                mForceTouchMenuInfo.mStartY = mForceTouchMenuInfo.mMenuYPos;
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mMenuYPos
                        + iconP.y + padding * 2;
                mForceTouchMenuInfo.mStartX = mForceTouchMenuInfo.mMenuXPos;
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mMenuXPos;
            } else {
                mForceTouchMenuInfo.mStartY = (int) mCurMenuView.getY();
                mForceTouchMenuInfo.mEndY = mForceTouchMenuInfo.mStartY
                        - iconP.y / 2 - padding;
                mForceTouchMenuInfo.mStartX = (int) mCurMenuView.getX();
                mForceTouchMenuInfo.mEndX = mForceTouchMenuInfo.mStartX;
            }
            break;
        default:
            break;
        }
        Log.d(LOGTAG, "getForceTouchWindowPos type=" + type + ";mDisplayPos="
                + mDisplayPos + ";mForceTouchMenuInfo.mMenuXPos="
                + mForceTouchMenuInfo.mMenuXPos
                + ";mForceTouchMenuInfo.mMenuYPos="
                + mForceTouchMenuInfo.mMenuYPos
                + ";mForceTouchMenuInfo.mStartY=" + mForceTouchMenuInfo.mStartY
                + ";mForceTouchMenuInfo.mEndY=" + mForceTouchMenuInfo.mEndY
                + ";mForceTouchMenuInfo.mStartX=" + mForceTouchMenuInfo.mStartX
                + ";mForceTouchMenuInfo.mEndX=" + mForceTouchMenuInfo.mEndX);
    }

    private void dismissWindow() {
        resetValues();
        if (null != mForceTouchWindow) {
            isMenuAnimaling = true;
            mForceTouchWindow.dismiss();
            mForceTouchWindow = null;
        }
    }

    private void setTouchState(CyeeForceTouchState state) {
        mTouchState = state;
    }
    
    private static class ForceTouchMenuInfo {
        public int mMenuXPos;
        public int mMenuYPos;
        public int mStartY;
        public int mEndY;
        public int mStartX;
        public int mEndX;
    }
}
