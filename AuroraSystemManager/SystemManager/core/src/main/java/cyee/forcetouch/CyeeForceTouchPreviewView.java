package cyee.forcetouch;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeWidgetResource;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class CyeeForceTouchPreviewView extends LinearLayout {

    private static final String LOGTAG = "CyeeForceTouchPreviewView";

    private final Scroller mScroller;
    private float mYPos1;

    private final Context mCxt;
    private int mPreviewType;
    private OnTouchPreviewCallback mTouchCallback;
    private OnPreviewAnimationCallback mAnimationCallback;
    private PreviewViewState mPreviewState = PreviewViewState.INIT;
    private final GestureDetector mGestureDetector;

    private FrameLayout mCustomView;
    private View mIndicatorView;

    private Rect mDefaultRect;
    private Rect mLastRect;
    private IPreviewControl mPreviewControl;
    private float mUpPos;
    private int mCurViewH;
    private boolean isHiding = false;
    private static final int PREVIEW_INDICATOR_MSG = 1;
    private Handler mHandler;
    
    enum PreviewViewState {
        INIT(0), UP_SCROLLING(1), MENU_FINISH(2), DOWN_SCROLLING(3), INIT_DOWN(
                4);

        private int mValue = 0;

        PreviewViewState(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public void setPreviewControl(IPreviewControl ctrl) {
        mPreviewControl = ctrl;
    }

    public void setPreviewState(PreviewViewState state) {
        this.mPreviewState = state;
    }

    public PreviewViewState getPreviewState() {
        return this.mPreviewState;
    }

    public CyeeForceTouchPreviewView(Context context, int previewType) {
        super(context, null, 0, resolveTheme(context));
        mCxt = context;
        mPreviewType = previewType;

        initViews();

        mScroller = new Scroller(mContext);
        mGestureDetector = new GestureDetector(context,
                new PreviewGestureListener());

        if (mPreviewType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            mDefaultRect = getContentPreviewDefaultRect();
        }
    }

    public void setPreviewViewFinalPos(Point p) {
        if (mLastRect == null) {
            mLastRect = new Rect();

            int screenHeight = CyeeForceTouchUtils.getScreenHeight(mCxt);

            int width = CyeeForceTouchUtils.getScreenWidth(mCxt);

            mLastRect.bottom = screenHeight - p.y;
            mLastRect.top = mLastRect.bottom - mCurViewH;
            mLastRect.left = 0;
            mLastRect.right = mLastRect.left + width;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public void setOnTouchPreviewCallback(OnTouchPreviewCallback callback) {
        this.mTouchCallback = callback;
    }

    public void setPreviewAnimationCallback(OnPreviewAnimationCallback callback) {
        this.mAnimationCallback = callback;
    }

    public void setPreviewType(int type) {
        mPreviewType = type;
    }

    public void addPreviewView(View view) {
        Drawable able = view.getBackground();
        int radius = (int) getResources().getDimension(com.cyee.internal.R.dimen.cyee_forcetouch_preview_radius);

        if (null != able) {
            Drawable newDrawable = CyeeForceTouchUtils.getRoundDrawable(able, radius);
            if(null != newDrawable) {
                view.setBackground(newDrawable);
            }
        } else {
            if (view instanceof ImageView) {
                able = ((ImageView) view).getDrawable();
                Drawable newDrawable = CyeeForceTouchUtils.getRoundDrawable(able, radius);
                if(null != newDrawable) {
                    ((ImageView) view).setImageDrawable(newDrawable);
                }
            } else {
                Drawable drawable = mCxt.getDrawable(com.cyee.internal.R.drawable.cyee_forcetouch_menu_bg);
                if (ChameleonColorManager.isNeedChangeColor(mCxt)) {
                    drawable.setColorFilter(ChameleonColorManager.getPopupBackgroudColor_B2(), Mode.SRC_IN);
                }
                view.setBackgroundDrawable(drawable);
            }
        }
        if(null != able) {
            able.setCallback(null);
            able = null;
        }
        
        mCustomView.addView(view);
    }
    
    void showPreviewAnimations() {
        if (mPreviewType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            startShowContactPreviewAnimations();
        } else if (mPreviewType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            startShowSMSPreviewAnimations();
        }
    }

    void hidePreviewAnimations() {
        if (mPreviewType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            hideContactPreviewAnimations();
        } else if (mPreviewType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            // 如果不再恰当位置，则平移到恰当位置，再消失
            isHiding = true;
            doHideSMSTranslationAnimals();
        }
    }
    
    /**
     * 滚动到目标位置
     * 
     * @param fx
     * @param fy
     */
    void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    /**
     * 设置滚动的相对偏移
     * 
     * @param dx
     * @param dy
     */
    void smoothScrollBy(int dx, int dy) {
        // Log.e(LOGTAG,"smoothScrollBy mScroller.getFinalY()="+mScroller.getFinalY());
        // 设置mScroller的滚动偏移量
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx,
                dy);
        invalidate();// 这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    @Override
    public void computeScroll() {
        // 判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset()) {
            Log.e(LOGTAG,"computeScroll mScroller.getCurrY()="+mScroller.getCurrY());
            // 这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }

    boolean previewGesture(MotionEvent event) {
        event.setLocation(event.getX(), event.getRawY() - mYPos1);

        return mGestureDetector.onTouchEvent(event);
    }

    boolean isNeedHideIndicator() {
        boolean ret = false;
        float distance = mDefaultRect.top - getY();

        int dy = CyeeForceTouchUtils.dip2px(mCxt,
                CyeeForceTouchConstant.PREVIEW_INDICATOR_GONE_DISTANCE);
        Log.e(LOGTAG, "isNeedGoneIndicator dy=" + dy + ";distance=" + distance);
        if (Math.abs(distance) > dy) {
            ret = true;
        }

        return ret;
    }
    
    void startShowSMSPreviewAnimations(float startY, float endY) {
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this,
                "translationY", startY, endY);
        translationY.setDuration(CyeeForceTouchConstant.SMS_MENU_ANIM_TIME);
        translationY.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int act = event.getAction();

        if (isHiding) {
            return super.onTouchEvent(event);
        }

        if (mPreviewType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            if (act == MotionEvent.ACTION_UP) {
                mTouchCallback.onTouchPreview();
            }

            return true;
        }

        if (null != mTouchCallback) {
            boolean response = mTouchCallback.shouldResponseForceTouch();
            if (response) {
                float pressure = event.getPressure();
                if (pressure >= CyeeForceTouchConfig.getInstance(mContext)
                        .getForceThreshold()) {
                    mTouchCallback.onMaxForceTouch(event);

                    return true;
                }
            }
        }

        event.setLocation(event.getX(), event.getRawY());
        switch (act) {
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            doActionUp();
            break;
        case MotionEvent.ACTION_MOVE:
            doPreviewMoveEvent(event);
            break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    void doOutsideEvent(MotionEvent event) {
        int act = event.getAction();
        switch (act) {
        case MotionEvent.ACTION_DOWN:
            mYPos1 = event.getRawY();
            Log.e(LOGTAG, "doOutsideEvent mYPos1=" + mYPos1);
            break;
        case MotionEvent.ACTION_MOVE:
            doPreviewMoveEvent(event);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            doActionUp();
            break;
        default:
            break;
        }
    }
    
    private static int resolveTheme(Context cxt) {
        TypedValue outValue = new TypedValue();
        boolean ret = cxt.getTheme().resolveAttribute(
                com.cyee.internal.R.attr.cyeeForceTouchMenuItemBackgroundColor, outValue,
                true);
        if (!ret) {
            cxt.setTheme(com.cyee.internal.R.style.Theme_Cyee_Light);
        }
        return 0;
    }

    private void initViews() {

        int resId = com.cyee.internal.R.layout.cyee_forcetouch_preview_layout;

        View parent = LayoutInflater.from(getContext()).inflate(resId, this,
                false);

        addView(parent);
        mCustomView = (FrameLayout) parent.findViewById(com.cyee.internal.R.id.cyee_forcetouch_custom_view);
        if (ChameleonColorManager.isNeedChangeColor(mCxt)) {
            mCustomView.getBackground().setColorFilter(
                    ChameleonColorManager.getPopupBackgroudColor_B2(),
                    Mode.SRC_IN);
        }
        mCustomView.setClickable(true);
        mCustomView.setFocusable(true);

        mIndicatorView = (View) parent.findViewById(com.cyee.internal.R.id.cyee_forcetouch_indicator);
        if (mPreviewType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            mIndicatorView.setVisibility(View.GONE);
            int height = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.QUICK_MENU_PREVIEW_HEIGHT);
            LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mCustomView
                    .getLayoutParams();
            linearParams.height = height;
            mCustomView.setLayoutParams(linearParams);
        }
        Point viewP = CyeeForceTouchUtils.measureView(this);
        mCurViewH = viewP.y;
    }

    private void showPreviewScale(float scaleSize) {
        mCustomView.setScaleX(scaleSize);
        mCustomView.setScaleY(scaleSize);
    }
    
    private void showIndicatorView() {
        if (null == mHandler) {
            mHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                    case PREVIEW_INDICATOR_MSG:
                        mIndicatorView.setVisibility(View.VISIBLE);
                        break;

                    default:
                        break;
                    }
                }

            };
        }
        mHandler.sendEmptyMessageDelayed(PREVIEW_INDICATOR_MSG,
                CyeeForceTouchConstant.PREVIEW_INDICATOR_ANIM_TIME);
    }
    
    private void doHideSMSTranslationAnimals() {
        int originYPos = getInitPreviewYPos();
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this,
                "translationY", getY(), originYPos);
        translationY.setDuration(CyeeForceTouchConstant.SMS_MENU_ANIM_TIME);
        translationY.start();
        translationY.addListener(new AnimatorListener() {

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
                hideSMSPreviewAnimations();
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void startShowContactPreviewAnimations() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(this, "scaleX", 0.8f, 1f);

        ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(this, "scaleY", 0.8f, 1f);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 0.2f, 1f);

        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        set.start();
    }

    private void hideContactPreviewAnimations() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(this, "scaleX", 1f, 0.8f);

        ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(this, "scaleY", 1f, 0.8f);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.2f);

        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        set.start();
        set.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                CyeeForceTouchPreviewView.this.setVisibility(View.GONE);
                mAnimationCallback.onHidePreviewAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
    }

    private void startShowSMSPreviewAnimations() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(this, "scaleX", 0.8f, 1f);

        ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(this, "scaleY", 0.8f, 1f);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 0.2f, 1f);

        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        set.start();
        set.addListener(new AnimatorListener() {

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
                showIndicatorView();
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void hideSMSPreviewAnimations() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(this, "scaleX", 1f, 0.8f);

        ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(this, "scaleY", 1f, 0.8f);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.0f);

        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        set.start();
        set.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                CyeeForceTouchPreviewView.this.setVisibility(View.GONE);
                mAnimationCallback.onHidePreviewAnimationEnd();
                isHiding = false;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
    }

    private void showPreviewScaleAnimation(float pressure) {
        float scale = 1;

        if (pressure >= CyeeForceTouchConfig.getInstance(mCxt)
                .getMidThreshold()) {

            int padding = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.PREVIEW_PADDING);

            double maxPressureRange = CyeeForceTouchConfig.getInstance(mCxt)
                    .getForceThreshold()
                    - CyeeForceTouchConfig.getInstance(mCxt).getMidThreshold();

            if (pressure >= CyeeForceTouchConfig.getInstance(mCxt)
                    .getForceThreshold()) {
                pressure = (float) CyeeForceTouchConfig.getInstance(mCxt)
                        .getForceThreshold();
            }
            double curPressureRange = pressure
                    - CyeeForceTouchConfig.getInstance(mCxt).getMidThreshold();

            scale += (float) (curPressureRange
                    * ((float) padding * 2 / getWidth()) / maxPressureRange);
        }
        showPreviewScale(scale);
    }
    
    private void doActionUp() {
        Log.e(LOGTAG, "doActionUp start mLastRect=" + mLastRect);
        if (null != mLastRect) {
            mUpPos = mLastRect.top;
        }
        smoothScrollTo(0, 0);
        mPreviewControl.doPreviewActionUp();
    }

    private void doPreviewMoveEvent(MotionEvent event) {
        float pressure = event.getPressure();

        if (isNeedHideIndicator()) {
            if (null != mHandler) {
                mHandler.removeCallbacksAndMessages(null);
            }
            mIndicatorView.setVisibility(View.INVISIBLE);
            pressure = (float) CyeeForceTouchConfig.getInstance(mCxt)
                    .getMidThreshold();
        } else {
            if (null != mHandler
                    && !mHandler.hasMessages(PREVIEW_INDICATOR_MSG)) {
                mIndicatorView.setVisibility(View.VISIBLE);
            }
        }
        showPreviewScaleAnimation(pressure);
    }

    private Rect getContentPreviewDefaultRect() {
        int width = CyeeForceTouchUtils.getScreenWidth(mCxt);

        Rect tRect = new Rect();
        int yPos = getInitPreviewYPos();
        tRect.top = yPos;
        tRect.bottom = yPos + mCurViewH;
        tRect.left = 0;
        tRect.right = width;

        return tRect;
    }

    private int getInitPreviewYPos() {
        int screenHeight = CyeeForceTouchUtils.getScreenHeight(mCxt);
        int height = CyeeForceTouchUtils.dip2px(mCxt,
                CyeeForceTouchConstant.CONTENT_PREVIEW_HEIGHT);
        int yPos = (screenHeight - height) / 2;

        yPos = yPos - (yPos - (screenHeight - mCurViewH) / 2) * 2;

        return yPos;
    }

    class PreviewGestureListener implements GestureDetector.OnGestureListener {

        private final String LOGTAG = "BouncyGestureListener";

        @Override
        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            // 处理压感预览点击预览事件
            if (null != mTouchCallback) {
                mTouchCallback.onTouchPreview();
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            int ration = 1;
            float dis = distanceY;
            float y = 0;
            int distance = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.MENU_DISPLAY_MOVE_DISTANCE);
            int hideMenuDistance = CyeeForceTouchUtils.dip2px(mCxt,
                    CyeeForceTouchConstant.MENU_DISPEAR_MOVE_DISTANCE);

            if ((getY() > mDefaultRect.top && distanceY > 0)
                    || (null != mLastRect && getY() < mLastRect.top && distanceY < 0)) {
                setY(getY() - dis);
                setX(getX());

                return true;
            }

            if (distanceY <= 0) {
                if (getY() >= mDefaultRect.top) {
                    ration = 8;
                }
                dis = dis / ration;

                y = getY() - dis;
                if (null != mLastRect && y < mLastRect.top) {
                    y = mLastRect.top;
                }

                setY(y);
                setX(getX());

                if (getY() - mUpPos > hideMenuDistance
                        || mDefaultRect.top - distance <= getY()) {
                    mPreviewControl.displayForceTouchMenu(
                            CyeeForceTouchMenuAndPreviewControl.HIDE_MENU, -1);
                } else {
                    mPreviewControl
                            .displayForceTouchMenu(
                                    CyeeForceTouchMenuAndPreviewControl.FOLLOW_TOUCH_MENU,
                                    -distanceY);
                }
            } else {
                // 如果达到最大menu高度，则产生阻尼
                if (null != mLastRect && getY() <= mLastRect.top) {
                    ration = 5;
                    dis = dis / ration;
                    y = getY() - dis;
                    setY(y);
                    setX(getX());
                    if (mLastRect.top > mDefaultRect.top - distance) {
                        if (mDefaultRect.top - getY() > distance
                                && Math.abs(dis) > 0.5f) {
                            mPreviewControl
                                    .displayForceTouchMenu(
                                            CyeeForceTouchMenuAndPreviewControl.DISPLAY_ALL_MENU,
                                            -distanceY);
                        }
                    }
                } else {
                    y = getY() - dis;
                    if (null != mLastRect && y < mLastRect.top) {
                        y = mLastRect.top;
                    }
                    setY(y);
                    setX(getX());

                    if (mDefaultRect.top - getY() > distance
                            && Math.abs(dis) > 0.5f) {
                        mPreviewControl
                                .displayForceTouchMenu(
                                        CyeeForceTouchMenuAndPreviewControl.DISPLAY_MENU,
                                        -distanceY);
                    }
                }
                if (Math.abs(dis) > 0.5f) {
                    mUpPos = getY();
                }
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            if (Math.abs(velocityY) > CyeeForceTouchConstant.MIN_FLING_VELOCITY) {
                if (null != e1) {
                    float minMoveDy = 0;
                    minMoveDy = Math.abs(e2.getRawY() - e1.getRawY());
                    if (minMoveDy >= CyeeForceTouchConstant.MIN_FLING_DISTANCE) {
                        mPreviewControl.doFlingAction(velocityY);
                    }
                } else {
                    mPreviewControl.doFlingAction(velocityY);
                }
            }

            return true;
        }

    }

    public interface OnTouchPreviewCallback {
        void onTouchPreview();

        void onMaxForceTouch(MotionEvent ev);

        boolean shouldResponseForceTouch();
    }

    public interface OnPreviewAnimationCallback {
        void onHidePreviewAnimationEnd();
    }
    
    public interface IPreviewControl {
        void displayForceTouchMenu(int displayType, float moveY);

        void doFlingAction(float velocityY);

        void doPreviewActionUp();
    }

}
