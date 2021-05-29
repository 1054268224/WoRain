package cyee.forcetouch;

import java.util.ArrayList;
import java.util.List;

import cyee.forcetouch.CyeeForceTouchMenuAndPreviewControl.IMenuAndPreviewControl;
import cyee.forcetouch.CyeeForceTouchPopupWindow.OnForceTouchPopupWindowCallBack;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import com.cyee.utils.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CyeeForceTouchPreviewController implements
        OnForceTouchPopupWindowCallBack, IMenuAndPreviewControl {

    private final static String TAG = "CyeeForceTouchPreviewController";
    private final Context mContext;
    private CyeeForceTouchClickCallback mForceTouchCallBack;
    private CyeeForceTouchMenuCallback mForceTouchMenuCallBack;
    private CyeeForceTouchPreviewCallback mPreviewCallBack;

    private Object mObjRegister = new Object();
    private final ArrayList<View> mTouchViews = new ArrayList<View>();
    private ArrayList<Object> mCancelTouchViews = new ArrayList<Object>();

    private Menu mCurForceTouchMenu;
    private int mMenuType = CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU; // 1联系人类型，2短信类型
    private final Handler mHandler = new Handler();
    private CyeeForceTouchPopupWindow mForceTouchWindow = null;

    private Bitmap mCurScreenShotBmp;
    private View mCurViewShotView;
    private View mCurSelectedView;
    private boolean isDismissing = false;

    private CyeeForceTouchState mTouchState = CyeeForceTouchState.NULL;
    private boolean isHasForceTouchMenu = true;
    private boolean isEnableForceTouch = true;
//    private boolean isFullScreen;
    private final CyeeForceTouchMenuAndPreviewControl mViewControl;

    public CyeeForceTouchState getTouchState() {
        return this.mTouchState;
    }

    public CyeeForceTouchPreviewController(Context context) {
        mContext = context;
        mViewControl = new CyeeForceTouchMenuAndPreviewControl(mContext);
        mViewControl.setMenuAndPreviewControl(this);
    }

    public void registerForceTouchViews(final List<View> views) {
        synchronized (mObjRegister) {
            if (mTouchViews.containsAll(views)) {
                return;
            }
            for (View v : views) {
                if (!mTouchViews.contains(v)) {
                    mTouchViews.add(v);
                    v.setOnTouchListener(mTouchListener);
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

    public void cancelForceTouch(View view) {
        if (mCancelTouchViews.contains(view)) {
            return;
        }
        mCancelTouchViews.add(view);
    }

    public void onDestroy() {
        unregisterForceTouchView(null);
        clearCancelForceTouch();
    }

    public void setEnableForceTouch(boolean enable) {
        isEnableForceTouch = enable;
    }

    public void dismiss() {
        Log.d(TAG, "dismiss start 123 getTouchState().getValue()="+getTouchState().getValue()+";isDismissing="+isDismissing);
        if (isDismissing) {
            return;
        }
        isDismissing = true;
//        if (!isFullScreen) {
//            CyeeForceTouchUtils.hideStatusBar(mContext, false);
//        }
        if(getTouchState().getValue() <= CyeeForceTouchState.LIGHT.getValue()) {
            hideForceTouchWindoAnimal();
            return ;
        }
        mViewControl.dismissControlView();
    }

    @Override
    public void onMenuItemClick(MenuItem item) {
        if (mForceTouchMenuCallBack != null) {
            dismiss();
            if (null != item) {
                mForceTouchMenuCallBack.onForceTouchMenuItemClick(
                        mCurSelectedView, item);
            }
        }
    }

    public void setCyeeForceTouchClickCallback(
            CyeeForceTouchClickCallback callback) {
        this.mForceTouchCallBack = callback;
    }

    public CyeeForceTouchClickCallback getCyeeForceTouchClickCallback() {
        return this.mForceTouchCallBack;
    }

    public void setCyeeForceTouchMenuCallback(
            CyeeForceTouchMenuCallback callback) {
        this.mForceTouchMenuCallBack = callback;
    }

    public void setForceTouchPreviewCallback(
            CyeeForceTouchPreviewCallback callback) {
        this.mPreviewCallBack = callback;
    }

    public void setForceTouchMenuType(int menuType) {
        mMenuType = menuType;
    }

    void setSynObj(Object object) {
        this.mObjRegister = object;
    }

    @Override
    public void onTouchPreviewClick() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                dismiss();
                if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
                    mPreviewCallBack.onClickPreviewView(mCurSelectedView);
                }
            }
        }, CyeeForceTouchConstant.TOUCH_PREVIEW_DELAY_TIME);
    }

    @Override
    public void dismissOver() {
        // TODO Auto-generated method stub
    }

    @Override
    public void addViewToWindow(View v, LinearLayout.LayoutParams params) {
        mForceTouchWindow.setMaskBlackView();
        mForceTouchWindow.addViewToPopWindow(v, params);
    }

    @Override
    public void dismissWindow() {
        dismiss();
    }

    @Override
    public void dismissOthers() {
        hideForceTouchWindoAnimal();
    }

    @Override
    public void onMaxForceTouch(MotionEvent ev) {
        doMaxForceTouch(ev);
    }

    private boolean doCyeeForceTouchEvent(View view, MotionEvent ev) {
        boolean ret = true;
        float pressure = ev.getPressure();
        int action = ev.getAction();

        Log.d(TAG, "doCyeeForceTouchEvent action=" + action + ";pressure="
                + pressure + ";view=" + mCurSelectedView
                + ";getTouchState().getValue()=" + getTouchState().getValue());

        if (action == MotionEvent.ACTION_DOWN) {
            doActionDown(view, ev);
            return false;
        }

        if (getTouchState() == CyeeForceTouchState.NULL) {
            return false;
        }

        if (mCurSelectedView != view) {
            return false;
        }

        mViewControl.onTouchEvent(ev);

        if (getTouchState().getValue() < CyeeForceTouchState.MID.getValue()) {
            if (pressure > CyeeForceTouchConfig.getInstance(mContext)
                    .getLightThreshold()
                    && pressure < CyeeForceTouchConfig.getInstance(mContext)
                            .getMidThreshold()) {
                ret = doLightTouch(ev);
                if (!ret) {
                    return false;
                }
            } else if (pressure >= CyeeForceTouchConfig.getInstance(mContext)
                    .getMidThreshold()) {
                ret = doForceTouch(ev);
            } else {

            }
        }

        if (getTouchState() == CyeeForceTouchState.MID) {
            boolean response = mViewControl.shouldResponseForceTouch();

            if (response) {
                if (pressure >= CyeeForceTouchConfig.getInstance(mContext)
                        .getForceThreshold()) {
                    return doMaxForceTouch(ev);
                } else {
                    // do nothing
                }
            }
        }

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
            ret = doActionMove(ev);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            ret = doActionUp(ev);
            break;
        default:
            break;
        }
        return ret;
    }

    private void doActionDown(View view, MotionEvent event) {
        // isFullScreen = CyeeForceTouchUtils.isFullScreen(mContext);
        Log.d(TAG,"doActionDown mCurSelectedView="+mCurSelectedView+";mCurScreenShotBmp="+mCurScreenShotBmp);
        if(null != mCurSelectedView) {
            return ;
        }
        mCurSelectedView = view;
        mCurScreenShotBmp = CyeeForceTouchUtils.getCurrentScreenShot(mContext);
        mCurViewShotView = createTouchViewShot();
        if (null == mCurViewShotView) {
            return;
        }
        if (null != mForceTouchMenuCallBack) {
            mViewControl.setMenuType(mMenuType);
        }
        setTouchState(CyeeForceTouchState.PRESS);
    }

    private final OnTouchListener mTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (!enableForceTouch()) {
                return false;
            }
            return doCyeeForceTouchEvent(view, event);
        }
    };

    private boolean enableForceTouch() {
        boolean isSupport = CyeeForceTouchConfig.getInstance(mContext)
                .isSupportForceTouch();

        return isEnableForceTouch && isSupport;
    }

    private void setTouchState(CyeeForceTouchState state) {
        mTouchState = state;
    }

    private View createTouchViewShot() {
        if (null == mCurSelectedView || mCurSelectedView.getWidth() <= 0
                || mCurSelectedView.getHeight() <= 0) {
            return null;
        }

        ImageView view = new ImageView(mContext);
        view.setClickable(true);
        LayoutParams params = new LayoutParams();
        params.width = mCurSelectedView.getWidth();
        params.height = mCurSelectedView.getHeight();
        view.setLayoutParams(params);

        Rect viewRect = CyeeForceTouchUtils.getViewRect(mCurSelectedView);

        int height = viewRect.height();
        int screenH = CyeeForceTouchUtils.getScreenHeight(mContext);
        int top = viewRect.top;

        if (viewRect.bottom > screenH) {
            height = screenH - viewRect.top;
        }
        if (top < 0) {
            height += top;
            top = 0;
        }
        if (height < 0) {
            height = 1;
        }

        Bitmap bmp = Bitmap.createBitmap(mCurScreenShotBmp, viewRect.left, top,
                viewRect.width(), height);

        view.setBackground(new BitmapDrawable(bmp));

        return view;
    }

    private boolean doActionMove(MotionEvent event) {
        if (getTouchState().getValue() < CyeeForceTouchState.LIGHT.getValue()
                || null == mPreviewCallBack || null == mForceTouchMenuCallBack) {
            return false;
        }
        mViewControl.doActionMove(event);

        return true;
    }

    private boolean doActionUp(MotionEvent event) {
        Log.d(TAG, "doActionUp getTouchState()=" + getTouchState());
        boolean ret = false;
        if (getTouchState() == CyeeForceTouchState.MID) {
            ret = mViewControl.doActionUp(event);
            if (!ret) {
                dismiss();
            }
        } else {
            if (getTouchState() == CyeeForceTouchState.LIGHT) {
                ret = true;
            }
            dismiss();
        }
        return ret;
    }

    private boolean doLightTouch(MotionEvent ev) {
        boolean ret = false;
        float pressure = ev.getPressure();

        setTouchState(CyeeForceTouchState.LIGHT);
        if (null != mForceTouchCallBack) {
            ret = mForceTouchCallBack.onLightTouchClick(mCurSelectedView,
                    pressure);
        }
        if (ret) {
            return false;
        } else {
            // 取消长按事件
            // mockLongClick(mListView);

            // 显示高斯模糊背景
            showPopWindow(ev);
        }

        return true;
    }

    private boolean doForceTouch(MotionEvent ev) {
        boolean ret = false;

        if (null == mPreviewCallBack) {
            dismiss();

            return true;
        }

        setTouchState(CyeeForceTouchState.MID);
        CyeeForceTouchUtils.invokeVibrate(mContext,
                CyeeForceTouchConstant.BUTTON_ON, new long[] {
                        CyeeForceTouchConstant.VIBRATE_DELAY,
                        CyeeForceTouchConstant.VIBRATE_TIME_SHORT }, 1);
        if (null != mForceTouchCallBack) {
            ret = mForceTouchCallBack.onForceTouchClick(mCurSelectedView);
        }

        Log.d(TAG, "doForceTouch start ret=" + ret + ";mForceTouchWindow="
                + mForceTouchWindow);

        if (ret) {
            return false;
        } else {
            // 显示菜单
            if (null == mForceTouchWindow) {
                showPopWindow(ev);

            }
            Log.d(TAG, "doForceTouch end");
            
//            if (!isFullScreen) {
//                CyeeForceTouchUtils.hideStatusBar(mContext, true);
//            }
            showPreviewView();
            isHasForceTouchMenu = isHasForceTouchMenu(mCurSelectedView);
            if (!isHasForceTouchMenu) {
                // 用户注册了，但是没有设置创建菜单回调
                return false;
            }
            if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
                showPopMenu();
            }

            return true;
        }
    }

    private boolean doMaxForceTouch(MotionEvent ev) {
        if(getTouchState() == CyeeForceTouchState.MID) {
            mHandler.postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    CyeeForceTouchUtils.invokeVibrate(mContext, CyeeForceTouchConstant.BUTTON_OFF, new long[]{CyeeForceTouchConstant.VIBRATE_DELAY, CyeeForceTouchConstant.VIBRATE_TIME_SHORT}, 1);
                    dismiss();
                    mPreviewCallBack.onClickPreviewView(mCurSelectedView);
                }
            }, CyeeForceTouchConstant.TOUCH_PREVIEW_DELAY_TIME);
//            if (!isFullScreen) {
//                CyeeForceTouchUtils.hideStatusBar(mContext, false);
//            }
            if (null != mForceTouchCallBack) {
                mForceTouchCallBack.onForceTouchClickView(mCurSelectedView);
            }
            setTouchState(CyeeForceTouchState.NULL);
        }
        return true;
    }

    private void showPreviewView() {
        mForceTouchWindow.setBackgroundAlpha(255);
        mForceTouchWindow.removeContainerViews();
        createPreviewView(mMenuType);
    }

    private void showPopMenu() {
        mViewControl.createMenuView((CyeeForceTouchMenu) mCurForceTouchMenu,
                mMenuType);
    }

    private void createPreviewView(int type) {
        if (null == mPreviewCallBack) {
            return;
        }
        View view = mPreviewCallBack.onCreatePreviewView(mCurSelectedView);
        Rect rect = CyeeForceTouchUtils.getViewRect(mCurSelectedView);
        mViewControl.createPreviewView(view, type, rect);
    }

    private boolean isHasForceTouchMenu(View currentView) {
        if (null == mForceTouchMenuCallBack) {
            return false;
        }

        if (null != mForceTouchMenuCallBack) {
            Menu createMenu = new CyeeForceTouchMenu(mContext);
            mForceTouchMenuCallBack.onCreateForceTouchMenu(mCurSelectedView, createMenu);
            mForceTouchMenuCallBack.onPrepareForceTouchMenu(mCurSelectedView, createMenu);
            mCurForceTouchMenu = createMenu;
            mViewControl
                    .setForceTouchMenu((CyeeForceTouchMenu) mCurForceTouchMenu);
        }

        return mCurForceTouchMenu.size() > 0;
    }

    private void cancelMockLongClick() {
        // if (null != mHandler) {
        // mHandler.removeCallbacksAndMessages(null);
        // mHandler = null;
        // }
    }

    private void hideForceTouchWindoAnimal() {
        resetValues();
        if (null != mForceTouchWindow) {
            mForceTouchWindow.dismiss();
            mForceTouchWindow = null;
        }
    }

    private void resetValues() {
        if (null != mCurScreenShotBmp) {
            mCurScreenShotBmp.recycle();
            mCurScreenShotBmp = null;
        }
        mCurSelectedView = null;
        mCurForceTouchMenu = null;
        setTouchState(CyeeForceTouchState.NULL);
        isDismissing = false;
        if (null != mViewControl) {
            mViewControl.onDestroyControl();
        }
        cancelMockLongClick();
//        isFullScreen = false;
    }

    private void showPopWindow(MotionEvent event) {
        if (null == mCurSelectedView) {
            return;
        }

        if (null != mForceTouchWindow && mForceTouchWindow.isShowing()) {
            if (getTouchState() == CyeeForceTouchState.LIGHT) {
                float pressure = 0;
                pressure = event.getPressure()
                        - (float) CyeeForceTouchConfig.getInstance(mContext)
                                .getLightThreshold();
                setPopWindowBackgroudAlpha(pressure);
            }
            return;
        }

        if (null == mForceTouchWindow) {
            mCurSelectedView.cancelLongPress();
        }

        mForceTouchWindow = new CyeeForceTouchPopupWindow(mContext);
        mForceTouchWindow.setPopupWindowCallback(this);
        Rect viewRect = CyeeForceTouchUtils.getViewRect(mCurSelectedView);
        Bitmap bmp = CyeeForceTouchBackgroud.getBlurBitmap(mContext,
                mCurScreenShotBmp, false);
        mForceTouchWindow.setBackground(new BitmapDrawable(bmp));
        mCurViewShotView.setX(viewRect.left);
        mCurViewShotView.setY(viewRect.top);
        mForceTouchWindow.showPopWindow(mCurViewShotView, viewRect.width(),
                viewRect.height());
    }

    private void setPopWindowBackgroudAlpha(float pressure) {
        float percent = (float) ((pressure * 100) / ((CyeeForceTouchConfig
                .getInstance(mContext).getMidThreshold() - CyeeForceTouchConfig
                .getInstance(mContext).getLightThreshold()) * 100));
        if (percent >= 1) {
            percent = 1;
        }
        mForceTouchWindow.setBackgroundAlpha((int) (255 * percent));
    }

    private void clearCancelForceTouch() {
        if (null != mCancelTouchViews) {
            mCancelTouchViews.clear();
            mCancelTouchViews = null;
        }
    }

}
