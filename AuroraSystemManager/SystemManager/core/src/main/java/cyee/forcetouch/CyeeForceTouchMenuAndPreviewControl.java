package cyee.forcetouch;

import cyee.forcetouch.CyeeForceTouchMenuView.OnForceTouchMenuItemClickListener;
import cyee.forcetouch.CyeeForceTouchMenuView.OnMenuViewChangedListener;
import cyee.forcetouch.CyeeForceTouchPreviewView.IPreviewControl;
import cyee.forcetouch.CyeeForceTouchPreviewView.OnPreviewAnimationCallback;
import cyee.forcetouch.CyeeForceTouchPreviewView.OnTouchPreviewCallback;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import com.cyee.utils.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;

public class CyeeForceTouchMenuAndPreviewControl implements
        OnPreviewAnimationCallback, OnTouchPreviewCallback,
        OnForceTouchMenuItemClickListener, IPreviewControl,
        OnMenuViewChangedListener {

    private final static String LOGTAG = "CyeeForceTouchMenuAndPreviewControl";

    public final static int DISPLAY_MENU = 0;
    public final static int HIDE_MENU = DISPLAY_MENU + 1;
    public final static int FOLLOW_TOUCH_MENU = HIDE_MENU + 1;
    public final static int DISPLAY_ALL_MENU = FOLLOW_TOUCH_MENU + 1;
    
    private CyeeForceTouchMenuView mMenuView;
    private CyeeForceTouchPreviewView mPreviewView;
    private final Context mCxt;
    private MotionEvent mMotionEvent;
    private IMenuAndPreviewControl mCallback;
    private CyeeForceTouchMenu mMenu;
    private PreviewViewInfo mPreviewInfo;
    private int mMenuType;
    private boolean isOpenMenu;
    private int mCurPreviewH;

    public CyeeForceTouchMenuAndPreviewControl(Context cxt) {
        mCxt = cxt;
    }

    public void setMenuType(int type) {
        mMenuType = type;
    }

    public void onTouchEvent(MotionEvent ev) {
        mMotionEvent = ev;

        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            return;
        }

        if (null != mPreviewView
                && mPreviewView.getVisibility() == View.VISIBLE) {
            mPreviewView.previewGesture(ev);
            mPreviewView.doOutsideEvent(ev);
        }
    }

    @Override
    public boolean shouldResponseForceTouch() {
        boolean ret = false;
        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW
                && !mPreviewView.isNeedHideIndicator()) {
            ret = true;
        }

        return ret;
    }

    public void doActionMove(MotionEvent ev) {
        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            if (null == mMenuView) {
                return;
            }
            mMenuView.onTouchMenuItem(ev);
        } else {
            if (null != mMenu && mMenu.size() > 0) {
                if (null == mPreviewView) {
                    return;
                }
                mPreviewView.doOutsideEvent(mMotionEvent);
            }
        }
    }

    public boolean doActionUp(MotionEvent ev) {
        boolean ret = false;

        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            if (null == mMenuView) {
                return ret;
            }
            ret = true;
            mMenuView.onTouchMenuItem(ev);
        } else {
            if (null == mPreviewView) {
                return ret;
            }
            ret = true;
            mPreviewView.doOutsideEvent(mMotionEvent);
        }

        return ret;
    }

    public void dismissControlView() {
        Log.e(LOGTAG, "dismissControlView mMenuView=" + mMenuView
                + ";mPreviewView=" + mPreviewView);
        if (null != mMenuView) {
            mMenuView.hidePopMenuAnimations();
        }
        if (null != mPreviewView) {
            mPreviewView.hidePreviewAnimations();
        }
    }

    public void setMenuAndPreviewControl(IMenuAndPreviewControl callback) {
        this.mCallback = callback;
    }

    public void createPreviewView(View customView, int type, Rect clickRect) {
        CyeeForceTouchPreviewView previewView = new CyeeForceTouchPreviewView(
                mCxt, type);
        mPreviewView = previewView;
        mPreviewView.setPreviewControl(this);
        mPreviewView.setPreviewAnimationCallback(this);
        mPreviewView.addPreviewView(customView);
        mPreviewView.setOnTouchPreviewCallback(this);

        Rect rect = getPreviewRect(type, clickRect);
        mCurPreviewH = rect.height();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                rect.width(), mCurPreviewH);

        mPreviewView.setX(rect.left);
        mPreviewView.setY(rect.top);
        Log.e(LOGTAG,
                "createPreviewView rect.left=" + rect.left + ";rect.top="
                        + rect.top + ";width=" + rect.width() + ";height="
                        + rect.height() + ";type=" + type + ";mPreviewView.y"
                        + mPreviewView.getY());
        mCallback.addViewToWindow(mPreviewView, params);
        mMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mPreviewView.doOutsideEvent(mMotionEvent);

        mPreviewView.showPreviewAnimations();
    }

    public void setForceTouchMenu(CyeeForceTouchMenu menu) {
        mMenu = menu;
    }

    public void createMenuView(CyeeForceTouchMenu menu, int type) {
        int width = 0, height = 0, xPos = 0, yPos = 0;

        if (null == menu || menu.size() <= 0) {
            Point p = new Point();
            p.y = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.PREVIEW_INDICATOR_GONE_DISTANCE);
            mPreviewView.setPreviewViewFinalPos(p);
            return;
        }

        mMenu = menu;

        if (type == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            Log.e(LOGTAG, "createMenuView mPreviewInfo.mMenuSort="
                    + mPreviewInfo.mMenuSort);
            mMenu.setMenuSort(mPreviewInfo.mMenuSort);
        }
        mMenuView = new CyeeForceTouchMenuView(mCxt, mMenu, type);
        mMenuView.setOnForceTouchMenuItemClickListener(this);
        mMenuView.setOnMenuViewChangedListener(this);

        float animalY = 0f;
        float pivotY = 0f;
        int screentH = CyeeForceTouchUtils.getScreenHeight(mCxt);
        Point p = CyeeForceTouchUtils.measureView(mMenuView);

        height = p.y;
        if (type == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            int padding = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.PREVIEW_PADDING);
            xPos = mPreviewInfo.mViewRect.left + padding;
            if (mPreviewInfo.mMenuSort == CyeeForceTouchConstant.MENU_SORT_ORDER) {
                yPos = mPreviewInfo.mViewRect.bottom + padding;
                animalY = mPreviewInfo.mViewRect.bottom
                        - mPreviewInfo.mViewRect.height() / 2;
                pivotY = 0f;
            } else {
                yPos = mPreviewInfo.mViewRect.top - padding - height;
                animalY = yPos + padding + mPreviewInfo.mViewRect.height() / 2;
                pivotY = height;
            }
            mMenuView.setX(xPos);
            mMenuView.setY(animalY);
            mMenuView.setZ(-1);
            width = CyeeForceTouchUtils.getScreenWidth(mCxt) - padding * 2;
        } else {
            // 短信菜单
            yPos = (int) mPreviewView.getY() + mPreviewView.getHeight();
            mMenuView.setX(0f);
            if (!mMenuView.isAnimaling()) {
                mMenuView.setY(screentH);
            }
            width = CyeeForceTouchUtils.getScreenWidth(mCxt);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                LayoutParams.WRAP_CONTENT);

        mCallback.addViewToWindow(mMenuView, params);

        Log.e(LOGTAG, "createMenuView yPos=" + yPos + ";height=" + height
                + ";screentH=" + screentH);

        mMenuView.setVisibility(View.VISIBLE);
        if (type == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            mMenuView.showPopMenuAnimations(width / 2, pivotY, animalY, yPos);
        } else {
            if (yPos + height > screentH) {
                // 展开一半菜单
                mMenuView.showPopMenuAnimations(screentH, yPos);
            } else {
                // 全部展开菜单此情况菜单项比较少
                mMenuView.showPopMenuAnimations(screentH, screentH - height);
            }
            isOpenMenu = true;
            mPreviewView.setPreviewViewFinalPos(p);
        }
    }

    public void onDestroyControl() {
        if (mMenuView != null) {
            mMenuView.cancelShowPopMenuAnim();
            mMenuView = null;
        }
        mPreviewView = null;
        isOpenMenu = false;
        mMenuType = 0;
    }

    private Rect getPreviewRect(int type, Rect rect) {
        int width = 0, height = 0, yPos = 0, screenHeight = 0;

        screenHeight = CyeeForceTouchUtils.getScreenHeight(mCxt);
        width = CyeeForceTouchUtils.getScreenWidth(mCxt);

        if (null == mPreviewInfo) {
            mPreviewInfo = new PreviewViewInfo();
        }

        Log.e(LOGTAG, "getPreviewRect type=" + type + ";rect.top=" + rect.top
                + ";screenHeight=" + screenHeight);

        if (type == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            height = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.QUICK_MENU_PREVIEW_HEIGHT);
            if (rect.top + height / 2 < screenHeight / 2) {
                mPreviewInfo.mMenuSort = CyeeForceTouchConstant.MENU_SORT_ORDER;
                yPos = rect.top;
            } else {
                mPreviewInfo.mMenuSort = CyeeForceTouchConstant.MENU_SORT_REVERT_ORDER;
                if (rect.bottom > screenHeight) {
                    yPos = screenHeight - height;
                } else {
                    yPos = rect.bottom - height;
                }
            }
        } else {
            mPreviewInfo.mMenuSort = CyeeForceTouchConstant.MENU_SORT_ORDER;
            Point viewP = CyeeForceTouchUtils.measureView(mPreviewView);
            height = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.CONTENT_PREVIEW_HEIGHT);
            yPos = (screenHeight - height) / 2;
            yPos = yPos - (yPos - (screenHeight - viewP.y) / 2) * 2;
            height = viewP.y;
        }

        Rect tRect = new Rect();
        tRect.top = yPos;
        tRect.bottom = yPos + height;
        tRect.left = 0;
        tRect.right = width;

        mPreviewInfo.mViewRect = tRect;

        return tRect;
    }

    private void followTouchMenu() {
        Log.e(LOGTAG, "followTouchMenu isOpenMenu=" + isOpenMenu
                + ";mMenuView=" + mMenuView);
        if (null == mMenuView || !isOpenMenu) {
            return;
        }
        if (!mMenuView.isAnimaling()) {
            mMenuView.setY(mPreviewView.getY() + mCurPreviewH);
        }
    }

    private void displayMenu(float distanceY) {

        if (!isOpenMenu) {
            // 弹出菜单
            displayMenuView();
        } else {
            if (null == mMenuView) {
                return;
            }

            if (!mMenuView.isAnimaling()) {
                mMenuView.setY(mPreviewView.getY() + mCurPreviewH);
            }
        }
    }

    private void displayAllMenu() {
        if (!isOpenMenu) {
            displayMenuView();
        }
    }
    
    private void displayMenuView() {
        if (null != mMenuView && mMenuView.isAnimaling()) {
            return;
        }
        createMenuView(mMenu, mMenuType);
    }

    private void hideMenu() {
        if (null == mMenuView) {
            return;
        }

        if (mMenuView.isAnimaling() || !isOpenMenu) {
            return;
        }
        isOpenMenu = false;
        Log.e(LOGTAG, "movePreviewView hide");
        mMenuView.hidePopMenuAnimations();
    }

    @Override
    public void onHidePreviewAnimationEnd() {
        Log.e(LOGTAG, "onHidePreviewAnimationEnd mCallback =" + mCallback);
        if (null != mCallback) {
            mCallback.dismissOthers();
        }
    }

    @Override
    public void onTouchPreview() {
        if (null != mCallback) {
            mCallback.onTouchPreviewClick();
        }
    }

    @Override
    public void onMaxForceTouch(MotionEvent ev) {
        if (null != mCallback) {
            mCallback.onMaxForceTouch(ev);
        }
    }
    
    @Override
    public void onMenuItemClick(MenuItem menuItem) {
        if (null != menuItem && null != mCallback) {
            mCallback.onMenuItemClick(menuItem);
        }
    }

    @Override
    public void displayForceTouchMenu(int displayType, float moveY) {

        Log.e(LOGTAG, "displayForceTouchMenu displayType=" + displayType
                + ";moveY=" + moveY);
        if (displayType == DISPLAY_MENU) {
            displayMenu(moveY);
        } else if (displayType == HIDE_MENU) {
            hideMenu();
        } else if (displayType == FOLLOW_TOUCH_MENU) {
            followTouchMenu();
        } else if (displayType == DISPLAY_ALL_MENU) {
            displayAllMenu();
        }
    }

    @Override
    public void doFlingAction(float velocityY) {
        if (velocityY > 0) {
            mCallback.dismissWindow();
        } else {
            int screentH = CyeeForceTouchUtils.getScreenHeight(mCxt);

            Point p = new Point(0, 0);

            if (null != mPreviewView) {
                p = CyeeForceTouchUtils.measureView(mPreviewView);
            }

            if (null != mMenuView) {
                p = CyeeForceTouchUtils.measureView(mMenuView);
                float tmp = screentH - p.y;
                Log.e(LOGTAG, "doFlingAction mMenuView = " + mMenuView
                        + ";mMenuView.getY()=" + mMenuView.getY()
                        + ";screentH - p.y=" + tmp + ";p.y=" + p.y
                        + ";screetH=" + screentH);
                if (mMenuView.getY() > screentH - p.y) {
                    mMenuView.showPopMenuAnimations(mMenuView.getY(), screentH
                            - p.y);
                }
            }

            float endY = 0;
            if (p.y == 0) {
                endY = (screentH - mCurPreviewH) / 2;
            } else {
                endY = screentH - p.y - mCurPreviewH;
            }
            if (null == mPreviewView) {
                return;
            }
            mPreviewView.startShowSMSPreviewAnimations(mPreviewView.getY(),
                    endY);
        }
    }

    @Override
    public void doPreviewActionUp() {
        float mockY = 0;
        Log.e(LOGTAG, "doPreviewActionUp start isOpenMenu=" + isOpenMenu);
        if (isOpenMenu) {
            mockY = -1;
        } else {
            mockY = 1;
        }
        doFlingAction(mockY);
    }

    @Override
    public void onMenuViewChanged() {
        float yPosMenuView = mMenuView.getY();
        float yPosPreviewView = mPreviewView.getY();
        int height = CyeeForceTouchUtils.getScreenHeight(mCxt);
        int menuH = mMenuView.getHeight();

        if (yPosMenuView >= yPosPreviewView) {
            if (yPosMenuView + menuH > height) {
                float distance = yPosMenuView - yPosPreviewView;
                yPosMenuView = height - menuH;
                yPosPreviewView = yPosMenuView - distance;
                if (yPosPreviewView < 0) {
                    yPosPreviewView = 0;
                    yPosMenuView = yPosPreviewView + distance;
                }
                mPreviewView.setX(mPreviewView.getX());
                mPreviewView.setY(yPosPreviewView);
                mMenuView.setX(mMenuView.getX());
                mMenuView.setY(yPosMenuView);
            }
        } else {
            int padding = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.PREVIEW_PADDING);

            int previewH = mPreviewView.getHeight();

            if (yPosPreviewView - padding - menuH < 0) {
                yPosMenuView = 0;
                yPosPreviewView = menuH + padding;

                if (yPosPreviewView + previewH > height) {
                    yPosPreviewView = height - previewH;
                    yPosMenuView = yPosPreviewView - padding - menuH;
                }
                mMenuView.setX(mMenuView.getX());
                mMenuView.setY(yPosMenuView);
                mPreviewView.setX(mPreviewView.getX());
                mPreviewView.setY(yPosPreviewView);
            }
        }

        return;
    }

    @Override
    public float getNewEndPosDistance(float endY) {
        int screentH = CyeeForceTouchUtils.getScreenHeight(mCxt);
        if(mMenuView == null || mPreviewView == null) {
            return 0;
        }
        Point p = CyeeForceTouchUtils.measureView(mMenuView);
        float yPos = mPreviewView.getY() + mCurPreviewH;
        if (yPos < screentH - p.y) {
            yPos = screentH - p.y;
        }

        if (endY <= yPos) {
            return 0;
        }

        return endY - yPos;
    }
    
    @Override
    public float getTranslationY() {
        float transY = 0;

        if (null == mPreviewInfo) {
            return transY;
        }

        transY = mPreviewInfo.mViewRect.height() / 2;

        return transY;
    }
    
    public interface IMenuAndPreviewControl {
        void addViewToWindow(View v, LinearLayout.LayoutParams params);

        void dismissWindow();

        void dismissOthers();

        void onMenuItemClick(MenuItem item);

        void onTouchPreviewClick();
        
        void onMaxForceTouch(MotionEvent ev);
    }

    static class PreviewViewInfo {
        public Rect mViewRect;
        public int mMenuSort = CyeeForceTouchConstant.MENU_SORT_ORDER;// 0正序
                                                                       // 1倒序
    }

}
