package cyee.forcetouch;

import cyee.forcetouch.CyeeForceTouchMenuAndPreviewControl.IMenuAndPreviewControl;
import cyee.forcetouch.CyeeForceTouchPopupWindow.OnForceTouchPopupWindowCallBack;
import cyee.widget.CyeeListView;
import android.content.Context;
import android.content.res.Configuration;
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
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CyeeForceTouchListView implements
        OnForceTouchPopupWindowCallBack, OnGestureListener,
        IMenuAndPreviewControl {

    private final static String LOGTAG = "CyeeForceTouchListView";

    private static final int DEFAULT = -1;

    private final Context mCxt;
    private final CyeeListView mListView;
    private CyeeListViewForceTouchClickCallback mForceTouchCallBack;
    private CyeeListViewForceTouchPreviewCallback mPreviewCallBack;
    private CyeeListViewForceTouchMenuCallback mForceTouchMenuCallBack;
    private Menu mCurForceTouchMenu;
    private int mMenuType = CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU; // 1联系人类型，2短信类型
    private final Handler mHandler = new Handler();
    private CyeeForceTouchPopupWindow mForceTouchWindow = null;

    private Bitmap mCurScreenShotBmp;
    private View mCurViewShotView;
    private int mCurSelectedItem;
    private Rect mCurSelViewRect = new Rect();
    
    private final GestureDetector mDetector;
    private boolean isDismissing = false;

    private CyeeForceTouchState mTouchState = CyeeForceTouchState.NULL;
    private boolean isHasForceTouchMenu = true;
//    private boolean isFullScreen;
    
    private int mFeedback = DEFAULT;
    private OnItemLongClickListener mItemLongClickListerer;

    private final CyeeForceTouchMenuAndPreviewControl mViewControl;

    private void setTouchState(CyeeForceTouchState state) {
        mTouchState = state;
    }

    public CyeeForceTouchState getTouchState() {
        return this.mTouchState;
    }

    public CyeeForceTouchListView(CyeeListView listView) {
        mCxt = listView.getContext();
        mDetector = new GestureDetector(this);
        mListView = listView;
        mViewControl = new CyeeForceTouchMenuAndPreviewControl(mCxt);
        mViewControl.setMenuAndPreviewControl(this);
    }

    public boolean doCyeeForceTouchEvent(MotionEvent ev) {

        boolean ret = false;
        float pressure = ev.getPressure();
        int action = ev.getAction();

        if (!enableForceTouch()) {
            // Gionee <weidong> <2016-8-19> add for CR01741964 begin
            if(action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_CANCEL) {
                dismiss();
            }
            // Gionee <weidong> <2016-8-19> add for CR01741964 end
            return false;
        }
        
        mDetector.onTouchEvent(ev);

        if (getTouchState() == CyeeForceTouchState.NULL) {
            return false;
        }

        if (null == mCurScreenShotBmp) {
            return ret;
        }

        mViewControl.onTouchEvent(ev);
        
        Log.d(LOGTAG,"doCyeeForceTouchEvent pressure="+pressure+";action="+action+";getTouchState()="+getTouchState());
        
        if (getTouchState().getValue() < CyeeForceTouchState.MID.getValue()) {
            if (pressure > CyeeForceTouchConfig.getInstance(mCxt).getLightThreshold() && pressure < CyeeForceTouchConfig.getInstance(mCxt).getMidThreshold()) {
                ret = doLightTouch(ev);
                if (!ret) {
                    return false;
                }
            } else if (pressure >= CyeeForceTouchConfig.getInstance(mCxt).getMidThreshold()) {
                ret = doForceTouch(ev);
            } else {

            }
        }

        if (getTouchState() == CyeeForceTouchState.MID) {
            boolean response = mViewControl.shouldResponseForceTouch();

            if (response) {
                if (pressure >= CyeeForceTouchConfig.getInstance(mCxt).getForceThreshold()) {
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
            ret = doActionUp(ev);
            break;
        default:
            break;
        }
        return ret;
    }

    private boolean enableForceTouch() {
        boolean isSupport = CyeeForceTouchConfig.getInstance(mCxt)
                .isSupportForceTouch();

        return null != mPreviewCallBack && isSupport
                && !isScreenLandscape(mCxt);
    }

    private boolean isScreenLandscape(Context cxt) {
        Configuration config = cxt.getResources().getConfiguration();
        int ori = config.orientation;
        return ori == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    protected void doActionDown(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        mCurSelectedItem = mListView.pointToPosition(x, y);
        int firstPos = mListView.getFirstVisiblePosition();
        View curSelectedView = mListView.getChildAt(mCurSelectedItem - firstPos);
        if (null == curSelectedView || curSelectedView.getWidth() <= 0
                || curSelectedView.getHeight() <= 0) {
            return;
        }
        mCurSelViewRect = CyeeForceTouchUtils.getViewRect(curSelectedView);
        mCurScreenShotBmp = CyeeForceTouchUtils.getCurrentScreenShot(mCxt);
        if (null == mCurScreenShotBmp) {
            return;
        }
        mCurViewShotView = createTouchViewShot();
        if (null != mForceTouchMenuCallBack) {
            mMenuType = mForceTouchMenuCallBack
                    .onForceTouchMenuType(mCurSelectedItem);
            mViewControl.setMenuType(mMenuType);
        }
//        isFullScreen = CyeeForceTouchUtils.isFullScreen(mCxt);
        // Gionee <weidong> <2016-1-15> modify for CR01623240 begin
        setTouchState(CyeeForceTouchState.PRESS);
        // Gionee <weidong> <2016-1-15> modify for CR01623240 end
    }

    private View createTouchViewShot() {
        ImageView view = new ImageView(mCxt);
        view.setClickable(true);
        int height = mCurSelViewRect.height();
        LayoutParams params = new LayoutParams();
        params.width = mCurSelViewRect.width();
        params.height = height;
        view.setLayoutParams(params);

        int screenH = CyeeForceTouchUtils.getScreenHeight(mCxt);
        int screenW = CyeeForceTouchUtils.getScreenWidth(mCxt);
        int top = mCurSelViewRect.top;
        int width = mCurSelViewRect.width();
        
        if (mCurSelViewRect.bottom > screenH) {
            height = screenH - mCurSelViewRect.top;
        }
        if (mCurSelViewRect.right > screenW) {
            width = screenW - mCurSelViewRect.left;
        }
        
        if (top < 0) {
            height += top;
            top = 0;
        }
        if (height < 0) {
            height = 1;
        }
        // Gionee <weidong> <2016-9-10> modify for CR01757141 begin
        if (mCurSelViewRect.left < 0) {
            Log.d(LOGTAG, "createTouchViewShot mCurSelViewRect.left = "
                    + mCurSelViewRect.left);
            width += mCurSelViewRect.left;
            mCurSelViewRect.left = 0;
        }
        if (width < 0) {
            Log.d(LOGTAG, "createTouchViewShot width = " + width);
            width = 1;
        }
        // Gionee <weidong> <2016-9-10> modify for CR01757141 end
        
        Bitmap bmp = Bitmap.createBitmap(mCurScreenShotBmp, mCurSelViewRect.left, top,
                width, height);

        view.setBackground(new BitmapDrawable(bmp));

        return view;
    }

    private boolean doActionMove(MotionEvent event) {
        if (getTouchState().getValue() < CyeeForceTouchState.LIGHT.getValue()
                || null == mPreviewCallBack || null == mForceTouchMenuCallBack) {
            return false;
        }
        if (getTouchState() == CyeeForceTouchState.MID) {
            mViewControl.doActionMove(event);
        }

        return true;
    }

    private boolean doActionUp(MotionEvent event) {
        boolean ret = false;
        Log.e(LOGTAG, "doActionUp getTouchState()=" + getTouchState());

        if (getTouchState() == CyeeForceTouchState.MID) {
            ret = mViewControl.doActionUp(event);
            if (!ret) {
                dismiss();
            }
        } else {
            if (getTouchState() == CyeeForceTouchState.LIGHT) {
                ret = true;
            } else if (getTouchState() == CyeeForceTouchState.FORCE) {
                resetValues();
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
            ret = mForceTouchCallBack.onLightTouchClick(mCurSelectedItem,
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

        if (null == mPreviewCallBack || null == mForceTouchMenuCallBack) {
            dismiss();

            return true;
        }

        setTouchState(CyeeForceTouchState.MID);
        CyeeForceTouchUtils.invokeVibrate(mCxt,
                CyeeForceTouchConstant.BUTTON_ON, new long[]{CyeeForceTouchConstant.VIBRATE_DELAY, CyeeForceTouchConstant.VIBRATE_TIME_SHORT}, 1);
        if (null != mForceTouchCallBack) {
            ret = mForceTouchCallBack.onForceTouchClick(mCurSelectedItem);
        }

        Log.e(LOGTAG, "doForceTouch start ret=" + ret + ";mForceTouchWindow="
                + mForceTouchWindow);

        if (ret) {
            return false;
        } else {
            // 显示菜单
            if (null == mForceTouchWindow) {
                showPopWindow(ev);
            }
//                if (!isFullScreen) {
//                    CyeeForceTouchUtils.hideStatusBar(mCxt, true);
//                }
            
            
            ret = showPreviewView();
            if (!ret) {
                setTouchState(CyeeForceTouchState.NULL);
                dismiss();
                return true;
            }
            isHasForceTouchMenu = isHasForceTouchMenu(mCurSelectedItem);
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
        if (getTouchState() == CyeeForceTouchState.MID) {
            setTouchState(CyeeForceTouchState.FORCE);
            CyeeForceTouchUtils.invokeVibrate(mCxt,
                    CyeeForceTouchConstant.BUTTON_OFF, new long[]{CyeeForceTouchConstant.VIBRATE_DELAY, CyeeForceTouchConstant.VIBRATE_TIME_SHORT}, 1);
            dismiss();
            mPreviewCallBack.onClickPreviewView(mCurSelectedItem);
            
            // if (!isFullScreen) {
            // CyeeForceTouchUtils.hideStatusBar(mCxt, false);
            // }
            if (null != mForceTouchCallBack) {
                mForceTouchCallBack.onTouchClick(mCurSelectedItem);
            }
        }

        return true;
    }

    private boolean showPreviewView() {
        mForceTouchWindow.setBackgroundAlpha(255);
        mForceTouchWindow.removeContainerViews();
        return createPreviewView(mMenuType);
    }

    private void showPopMenu() {
        mViewControl.createMenuView((CyeeForceTouchMenu) mCurForceTouchMenu,
                mMenuType);
    }

    private boolean createPreviewView(int type) {
        if (null == mPreviewCallBack) {
            return false;
        }
        View view = mPreviewCallBack.onCreatePreviewView(mCurSelectedItem);

        if (null == view) {
            return false;
        }

        mViewControl.createPreviewView(view, type, mCurSelViewRect);

        return true;
    }

    private boolean isHasForceTouchMenu(int pos) {
        if (null == mForceTouchMenuCallBack) {
            return false;
        }

        if (null != mForceTouchMenuCallBack) {
            Menu createMenu = new CyeeForceTouchMenu(mCxt);
            mForceTouchMenuCallBack.onCreateMenu(pos, createMenu);
            mForceTouchMenuCallBack.onPrepareMenu(pos, createMenu);
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

    public void dismiss() {
        Log.e(LOGTAG, "dismiss start");
        if (isDismissing) {
            return;
        }
        Log.e(LOGTAG, "dismiss start getTouchState().getValue()="+getTouchState().getValue());
        isDismissing = true;
//        if (!isFullScreen) {
//            CyeeForceTouchUtils.hideStatusBar(mCxt, false);
//        }
        if (getTouchState().getValue() <= CyeeForceTouchState.LIGHT.getValue()) {
            hideForceTouchWindoAnimal();
            return;
        }

        mViewControl.dismissControlView();
    }

    private void hideForceTouchWindoAnimal() {
        if(getTouchState() != CyeeForceTouchState.FORCE) {
            resetValues();
        }
        isDismissing = false;
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
        mCurForceTouchMenu = null;
        setTouchState(CyeeForceTouchState.NULL);
        isDismissing = false;
        if (null != mViewControl) {
            mViewControl.onDestroyControl();
        }
        cancelMockLongClick();
        if (null != mItemLongClickListerer) {
            mListView.setOnItemLongClickListener(mItemLongClickListerer);
        }
        if (mFeedback != DEFAULT) {
            mListView.setHapticFeedbackEnabled(mFeedback == 1);
        }
//        mFeedback = DEFAULT;
//        isFullScreen = false;
    }

    private void showPopWindow(MotionEvent event) {
        if (null != mForceTouchWindow && mForceTouchWindow.isShowing()) {
            if (getTouchState() == CyeeForceTouchState.LIGHT) {
                float pressure = 0;
                pressure = event.getPressure() - (float) CyeeForceTouchConfig.getInstance(mCxt).getLightThreshold();
                setPopWindowBackgroudAlpha(pressure);
            }
            return;
        }

        if (null == mForceTouchWindow) {
            if(mItemLongClickListerer == null) {
                mItemLongClickListerer = mListView.getOnItemLongClickListener();
            }
            if(null != mItemLongClickListerer) {
                mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
                    
                    @Override
                    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                        return true;
                    }
                });
            }
            if(mFeedback == DEFAULT) {
                mFeedback = mListView.isHapticFeedbackEnabled() == true ? 1 : 0;
            }
            mListView.setHapticFeedbackEnabled(false);
            mListView.cancelLongPress();
        }

        mForceTouchWindow = new CyeeForceTouchPopupWindow(mCxt);
        mForceTouchWindow.setPopupWindowCallback(this);
        Bitmap bmp = CyeeForceTouchBackgroud.getBlurBitmap(mCxt,
                mCurScreenShotBmp, false);
        mForceTouchWindow.setBackground(new BitmapDrawable(bmp));
        mCurViewShotView.setX(mCurSelViewRect.left);
        mCurViewShotView.setY(mCurSelViewRect.top);
        mForceTouchWindow.showPopWindow(mCurViewShotView, mCurSelViewRect.width(),
                mCurSelViewRect.height());
    }

    private void setPopWindowBackgroudAlpha(float pressure) {
        float percent = (float) ((pressure * 100) / ((CyeeForceTouchConfig.getInstance(mCxt)
                .getMidThreshold() - CyeeForceTouchConfig.getInstance(mCxt).getLightThreshold()) * 100));
        if (percent >= 1) {
            percent = 1;
        }
        mForceTouchWindow.setBackgroundAlpha((int) (255 * percent));
    }

    public interface CyeeListViewForceTouchClickCallback {
        boolean onLightTouchClick(int position, float pressure);

        boolean onForceTouchClick(int position);

        void onTouchClick(int position);
    }

    public interface CyeeListViewForceTouchMenuCallback {
        void onCreateMenu(int position, Menu menu);

        void onPrepareMenu(int position, Menu menu);

        void onForceTouchMenuItemClick(int position, MenuItem menuItem);

        int onForceTouchMenuType(int position);
    }

    public interface CyeeListViewForceTouchPreviewCallback {
        View onCreatePreviewView(int position);

        void onClickPreviewView(int position);
    }

    @Override
    public void onMenuItemClick(MenuItem item) {
        if (mForceTouchMenuCallBack != null) {
            dismiss();
            if (null != item) {
                mForceTouchMenuCallBack.onForceTouchMenuItemClick(
                        mCurSelectedItem, item);
            }
        }
    }

    public void setCyeeListViewForceTouchClickCallback(
            CyeeListViewForceTouchClickCallback callback) {
        this.mForceTouchCallBack = callback;
    }

    public void setForceTouchMenuCallback(
            CyeeListViewForceTouchMenuCallback callback) {
        this.mForceTouchMenuCallBack = callback;
    }

    public void setForceTouchPreviewCallback(
            CyeeListViewForceTouchPreviewCallback callback) {
        this.mPreviewCallBack = callback;
    }

    public void onDestroyForceTouch() {
        CyeeForceTouchConfig.getInstance(mCxt).onDestroyForceTouchConfig();
    }
    
    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
//        Log.e(LOGTAG, "onFling arg0=" + arg0.getAction());
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
//        Log.e(LOGTAG, "onLongPress arg0=" + arg0.getAction());
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
//        Log.e(LOGTAG, "onScroll arg0=" + arg0.getAction());
        
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
//        Log.e(LOGTAG, "onShowPress arg0=" + arg0.getAction());
        doActionDown(arg0);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
//        Log.e(LOGTAG, "onSingleTapUp arg0=" + arg0.getAction());
        return false;
    }

    @Override
    public void onTouchPreviewClick() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                dismiss();
                if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
                    mPreviewCallBack.onClickPreviewView(mCurSelectedItem);
                }
            }
        }, CyeeForceTouchConstant.TOUCH_PREVIEW_DELAY_TIME);
    }

    @Override
    public void dismissOver() {
        // TODO Auto-generated method stub
        resetValues();
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

}
