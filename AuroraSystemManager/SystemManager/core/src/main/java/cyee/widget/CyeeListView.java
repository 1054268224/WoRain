package cyee.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.TypedValue;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cyee.internal.R;
import com.cyee.internal.view.menu.CyeeContextMenuBuilder;
import com.cyee.internal.widget.MultiChoiceScrollListener;
import com.cyee.utils.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cyee.changecolors.ChameleonColorManager;
import cyee.forcetouch.CyeeForceTouchListView;
import cyee.forcetouch.CyeeForceTouchListView.CyeeListViewForceTouchClickCallback;
import cyee.forcetouch.CyeeForceTouchListView.CyeeListViewForceTouchMenuCallback;
import cyee.forcetouch.CyeeForceTouchListView.CyeeListViewForceTouchPreviewCallback;

// Gionee <gaoj> <2013-9-3> add for CR00882174 end
// Gionee <gaoj> <2013-9-3> add for CR00882174 begin

public class CyeeListView extends ListView implements
        ActivityLifecycleCallbacks, OnScrollListener {
    private final Context mContext;
    private OnCreateContextMenuListener mContextMenuListener;
    private Fragment mFragment;

    // Gionee <gaoj> <2013-9-3> add for CR00882174 begin
    private CyeeContextMenuBuilder mMenuBuilder = null;
    // Gionee <gaoj> <2013-9-3> add for CR00882174 end

    // Gionee <lihq> <2013-11-15> modify for CR00873172 begin
    /**
     * Add for {@link CyeeListView} effect.
     */
    /**
     * Flag that enable or disable ListView item over scroll stretch
     */
    // gionee maxw modify begin
    private final boolean mStretchEnable = false;
    // gionee maxw modify end
    private int mMotionPosition;
    private int mMotionY;
    private CyeeStretchAnimationa mStretchAnimationa;
    private final String TAG = "CyeeListView-->";
    private boolean mModifiedDivider = false;
    private OnScrollListener mOnscrListener;
    private int mLastScrollState = -1;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;

    private final Drawable mDivider;

    private boolean mFastScrollEnabled;
    private boolean mFastScrollAlwaysVisible;
    private final Thread mOwnerThread;
    private CyeeFastScroller mFastScroller;
    private int mFastScrollStyle;
    private Drawable mScrollBarDrawable = null;
    private CyeeForceTouchListView mForceTouchListView;
    private GestureDetector mGesture = null;
    private MultiChoiceScrollListener mScrollListener = null;

    // Gionee <lihq> <2013-11-19> add for CR00873172 begin
    // Add for ViewPager Left-Right Effect enable flag.
    private boolean mVPEffectEnable = false;
    // Gionee <lihq> <2013-11-19> add for CR00873172 end

    private boolean mShowBottomItem = false;
    private boolean mShowBottomDivider = true;
    
    // Gionee <weidong> <2017-7-27> modify for 175148 begin
    private View mAddFooterView;
    private View mFootView;
    // Gionee <weidong> <2017-7-27> modify for 175148 end
    
    private int mDirection = LayoutDirection.LTR;
    
    public void setModifiedDiveder(boolean modify) {
        mModifiedDivider = modify;
    }

    public boolean needModifiedDivider() {
        return mModifiedDivider;
    }

    private void modifyDivider() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null && child.getBackground() == null) {
                child.setBackground(mDivider);
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // dismissForceTouchWindow();
        }
        super.onConfigurationChanged(newConfig);
    }

    private int getScreenWidth(Context cxt) {
        WindowManager wm = (WindowManager) cxt
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        return width;
    }

    // Gionee <lihq> <2013-11-15> modify for CR00873172 end
    public CyeeListView(Context context) {
        this(context, null);
    }

    public CyeeListView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.listViewStyle);
    }

    int mScreenWidth = 0;

    public CyeeListView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
        mScreenWidth = getScreenWidth(context);
    }

    public CyeeListView(Context context, AttributeSet attrs, int defStyle,
            int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        mDirection = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
        setCyeeListDivider(context, attrs);
        
//        mForceTouchListView = new CyeeForceTouchListView(this);
        mContext = context;
        SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
                // TODO Auto-generated method stub
                if (mScrollListener != null) {
                    mScrollListener.onScroll(e1, e2, distanceX, distanceY);
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        };
        mGesture = new GestureDetector(mContext, gestureListener);
        mOwnerThread = Thread.currentThread();
        // Gionee <lihq> <2013-10-31> modify for CR00873172 begin
        if (mStretchEnable) {
            setOverScrollMode(AbsListView.OVER_SCROLL_NEVER);
            ViewConfiguration config = ViewConfiguration.get(getContext());
            mMaximumVelocity = config.getScaledMaximumFlingVelocity();
        }
        super.setOnScrollListener(this);
        // Gionee <lihq> <2013-10-31> modify for CR00873172 end
        mDivider = mContext.getResources().getDrawable(com.cyee.internal.R.drawable.cyee_bg_decorator_adapter);
        if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            changeColor();
        }
    }

    /**
     * 是否需要在listview最下面添加一个Item
     * 此方法需要在setAdapter之前调用
     * @param show
     */
    public void setShowBottomItem(boolean show) {
        setShowBottomItem(show, true);
    }
    
    /**
     * 是否需要显示添加在listview最下面Item的分割线
     * @param show 是否显示item
     * @param showBottomDivider 是否显示分割线
     */
    public void setShowBottomItem(boolean show, boolean showBottomDivider) {
        mShowBottomItem = show;
        mShowBottomDivider = showBottomDivider;
    }
    
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mShowBottomItem && (null == mAddFooterView)) {
            mAddFooterView = LayoutInflater.from(getContext()).inflate(R.layout.cyee_listview_bottom_view, this, false);
            mFootView = mAddFooterView.findViewById(com.cyee.internal.R.id.cyee_ll_footview);
            View divider = mAddFooterView.findViewById(com.cyee.internal.R.id.cyee_listview_last_bottom_divider_id);
            
            if (mShowBottomDivider) {
                divider.setVisibility(View.VISIBLE);
            } else {
                divider.setVisibility(View.GONE);
            }
            
            addFooterView(mAddFooterView, null, false);
        }
        super.setAdapter(adapter);
    }
    
    // Gionee <weidong> <2017-7-27> modify for 175148 begin
    @Override
    public boolean removeFooterView(View view) {
        boolean ret = super.removeFooterView(view);
        if (view == mAddFooterView) {
            mAddFooterView = null;
        }
        return ret;
    }
    // Gionee <weidong> <2017-7-27> modify for 175148 end
    
    /**
     * 动态控制显示底部空白行
     * @param visibility
     */
    public void setCyeeFootViewVisible(int visibility) {
        if (null != mFootView) {
            mFootView.setVisibility(visibility);
        }
    }
    
    
    private void setCyeeListDivider(Context context, AttributeSet attrs) {
        int start = com.cyee.internal.R.attr.cyee_list_divider_padding_start;
        int end = com.cyee.internal.R.attr.cyee_list_divider_padding_end;
        int show = com.cyee.internal.R.attr.cyee_list_show_bottom_item;
        int showDivider = com.cyee.internal.R.attr.cyee_list_show_bottom_item_divider;
        
        int[] styleable = { start, end, show, showDivider };

        TypedArray typedArray = context.obtainStyledAttributes(attrs, styleable, com.cyee.internal.R.attr.cyeeListViewStyle, 0);
        float paddingStart = typedArray.getDimension(R.styleable.CyeeListView_cyee_list_divider_padding_start, -1);
        float paddingEnd = typedArray.getDimension(R.styleable.CyeeListView_cyee_list_divider_padding_end, -1);
        mShowBottomItem = typedArray.getBoolean(R.styleable.CyeeListView_cyee_list_show_bottom_item, false);
        mShowBottomDivider = typedArray.getBoolean(R.styleable.CyeeListView_cyee_list_show_bottom_item_divider, true);
        
        typedArray.recycle();

        Drawable dividerDrawable = getDivider();
        if (null == dividerDrawable && (paddingStart != -1 || paddingEnd != -1)) {
            setDividerPadding((int) paddingStart, (int) paddingEnd);
        }
    }

    /**
     * @param paddingStart
     *            default value -1
     * @param paddingEnd
     *            default value -1
     */
    @SuppressLint("NewApi")
    public void setDividerPadding(int paddingStart, int paddingEnd) {
        LayerDrawable divider = (LayerDrawable) getResources().getDrawable(com.cyee.internal.R.drawable.cyee_list_divider_bg);
        if (mDirection == LayoutDirection.RTL) {
            divider.setLayerInset(0, paddingEnd, 0, paddingStart, 0);
        } else {
            divider.setLayerInset(0, paddingStart, 0, paddingEnd, 0);
        }
        setDivider(divider);
        setDividerHeight((int) getResources().getDimension(com.cyee.internal.R.dimen.cyee_divider_height));
    }

    @SuppressLint("NewApi")
    public void setDividerPaddingEnd(int padding) {
        LayerDrawable divider = (LayerDrawable) getResources().getDrawable(com.cyee.internal.R.drawable.cyee_list_divider_bg);
        if (mDirection == LayoutDirection.RTL) {
            divider.setLayerInset(0, padding, 0, 0, 0);
        } else {
            divider.setLayerInset(0, 0, 0, padding, 0);
        }
        setDivider(divider);
        setDividerHeight((int) getResources().getDimension(com.cyee.internal.R.dimen.cyee_divider_height));
    }

    @SuppressLint("NewApi")
    public void setDividerPaddingStart(int padding) {
        LayerDrawable divider = (LayerDrawable) getResources().getDrawable(com.cyee.internal.R.drawable.cyee_list_divider_bg);
        if (mDirection == LayoutDirection.RTL) {
            divider.setLayerInset(0, 0, 0, padding, 0);
        } else {
            divider.setLayerInset(0, padding, 0, 0, 0);
        }
        setDivider(divider);
        setDividerHeight((int) getResources().getDimension(com.cyee.internal.R.dimen.cyee_divider_height));
    }

    @Override
    public void setFastScrollEnabled(final boolean enabled) {
        if (mFastScrollEnabled != enabled) {
            mFastScrollEnabled = enabled;

            if (isOwnerThread()) {
                setFastScrollerEnabledUiThread(enabled);
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setFastScrollerEnabledUiThread(enabled);
                    }
                });
            }
        }
    }

    @Override
    public void setFastScrollStyle(int styleResId) {
        mFastScrollStyle = styleResId;
        if (mFastScroller == null) {
            mFastScrollStyle = styleResId;
        } else {
            mFastScroller.setStyle(styleResId);
        }
    }

    public void setFastScrollAlwaysVisible(final boolean alwaysShow) {
        if (mFastScrollAlwaysVisible != alwaysShow) {
            if (alwaysShow && !mFastScrollEnabled) {
                setFastScrollEnabled(true);
            }
            mFastScrollAlwaysVisible = alwaysShow;
            if (isOwnerThread()) {
                setFastScrollerAlwaysVisibleUiThread(alwaysShow);
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setFastScrollerAlwaysVisibleUiThread(alwaysShow);
                    }
                });
            }
        }
    }

    private boolean isOwnerThread() {
        return mOwnerThread == Thread.currentThread();
    }

    private void setFastScrollerAlwaysVisibleUiThread(boolean alwaysShow) {
        if (mFastScroller != null) {
            mFastScroller.setAlwaysShow(alwaysShow);
        }
    }

    void reportScrollStateChange(int newState) {
        if (newState != mLastScrollState) {
            if (mOnscrListener != null) {
                mLastScrollState = newState;
                mOnscrListener.onScrollStateChanged(this, newState);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mFastScroller != null) {
            mFastScroller.onSizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    public int getVerticalScrollbarWidth() {
        if (mFastScroller != null && mFastScroller.isEnabled()) {
            return Math.max(super.getVerticalScrollbarWidth(),
                    mFastScroller.getWidth());
        }
        return super.getVerticalScrollbarWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        if (mFastScroller != null) {
            boolean intercepted = mFastScroller.onInterceptTouchEvent(ev);
            if (intercepted) {
                return true;
            }
        }
        boolean onInterceptTouchEvent = super.onInterceptTouchEvent(ev);
        return onInterceptTouchEvent;
    }

    /**
     * Returns true if the fast scroller is set to always show on this view.
     *
     * @return true if the fast scroller will always show
     * @see #setFastScrollAlwaysVisible(boolean)
     */
    @Override
    public boolean isFastScrollAlwaysVisible() {
        if (mFastScroller == null) {
            return mFastScrollEnabled && mFastScrollAlwaysVisible;
        } else {
            return mFastScroller.isEnabled()
                    && mFastScroller.isAlwaysShowEnabled();
        }
    }

    @Override
    @ExportedProperty
    public boolean isFastScrollEnabled() {
        if (mFastScroller == null) {
            return mFastScrollEnabled;
        } else {
            return mFastScroller.isEnabled();
        }
    }

    @Override
    public void setVerticalScrollbarPosition(int position) {
        super.setVerticalScrollbarPosition(position);
        if (mFastScroller != null) {
            mFastScroller.setScrollbarPosition(position);
        }
    }

    private void setFastScrollerEnabledUiThread(boolean enabled) {
        if (mFastScroller != null) {
            mFastScroller.setEnabled(enabled);
        } else if (enabled) {
            mFastScroller = new CyeeFastScroller(CyeeListView.this,
                    mFastScrollStyle);
            mFastScroller.setEnabled(true);
        }
        resolvePadding();
        if (mFastScroller != null) {
            mFastScroller.updateLayout();
        }
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        if (mFastScroller != null && mFastScroller.onInterceptHoverEvent(event)) {
            return true;
        }
        return super.onInterceptHoverEvent(event);
    }

    @Override
    protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar,
            int l, int t, int r, int b) {
        Drawable verticalScrollBar = changeColorScrollBar(scrollBar);
        super.onDrawVerticalScrollBar(canvas, verticalScrollBar, l, t, r, b);
    }

    @Override
    protected void onDrawHorizontalScrollBar(Canvas canvas, Drawable scrollBar,
            int l, int t, int r, int b) {
        Drawable horizontalScrollBar = changeColorScrollBar(scrollBar);
        super.onDrawHorizontalScrollBar(canvas, horizontalScrollBar, l, t, r, b);
    }

    private Drawable changeColorScrollBar(Drawable scrollBar) {

        if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            if (mScrollBarDrawable == null && scrollBar != null) {
                mScrollBarDrawable = scrollBar;
                mScrollBarDrawable.setColorFilter(ChameleonColorManager
                        .getContentColorThirdlyOnBackgroud_C3(),
                        android.graphics.PorterDuff.Mode.SRC_IN);
            }
            return mScrollBarDrawable;
        }
        return scrollBar;
    }

    private void changeColor() {
        if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            Drawable drawable = getSelector();
            if (drawable != null) {
                if (drawable instanceof RippleDrawable) {
                    ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
                }
            }
        }
    }

    @Override
    public void setOnItemLongClickListener(
            final android.widget.AdapterView.OnItemLongClickListener listener) {

        android.widget.AdapterView.OnItemLongClickListener wrapListener = new android.widget.AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (listener == null
                        || !listener
                                .onItemLongClick(parent, view, position, id)) {
                    showContextMenuDialog(view, position, id);
                }
                return true;
            }
        };
        super.setOnItemLongClickListener(wrapListener);
    }

    private void showContextMenuDialog(View view, int position, long id) {
        ContextMenuInfo menuInfo = cyeeCreateContextMenuInfo(view, position,
                id);
        // Gionee <gaoj> <2013-9-3> add for CR00882174 begin
        mMenuBuilder = new CyeeContextMenuBuilder(mContext);
        mMenuBuilder.setCurrentMenuInfo(menuInfo);
        mMenuBuilder.setFragment(mFragment);
        if (mContextMenuListener != null) {
            mContextMenuListener.onCreateContextMenu(mMenuBuilder, this,
                    menuInfo);
        }

        if (mContext instanceof AppCompatActivity) {
            ((AppCompatActivity) mContext).getApplication()
                    .registerActivityLifecycleCallbacks(this);
        }
        int size = mMenuBuilder.size();
        if (size > 0) {
            mMenuBuilder.show(this, null);
        }
        // Gionee <gaoj> <2013-9-3> add for CR00882174 end
    }

    // Gionee <fenglp> <2013-07-24> add for CR00812456 begin
    @Override
    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        mContextMenuListener = l;
        setOnItemLongClickListener(getOnItemLongClickListener());
    }

    ContextMenuInfo cyeeCreateContextMenuInfo(View view, int position, long id) {
        return new AdapterContextMenuInfo(view, position, id);
    }

    // Gionee <fenglp> <2013-07-24> add for CR00812456 end

    // Gionee <fenglp> <2013-07-31> add for CR00812456 begin
    public void registerFragmentForContextMenu(Fragment fragment) {
        mFragment = fragment;
    }

    // Gionee <fenglp> <2013-07-31> add for CR00812456 end

    // Gionee <gaoj> <2013-08-27> add for CR00833450 begin
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // TODO Auto-generated method stub

    }

    // Gionee <gaoj> <2013-08-27> add for CR00833450 end

    // Gionee <gaoj> <2013-9-3> add for CR00882174 begin
    private void hideContextMenuDialog() {
        if (mMenuBuilder != null) {
            mMenuBuilder.close();
            mMenuBuilder = null;

            if (mContext instanceof AppCompatActivity) {
                ((AppCompatActivity) mContext).getApplication()
                        .unregisterActivityLifecycleCallbacks(this);
            }
        }
    }

    @Override
    public void onActivityCreated(Activity arg0, Bundle arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityDestroyed(Activity arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityPaused(Activity arg0) {
        // TODO Auto-generated method stub
        hideContextMenuDialog();
    }

    @Override
    public void onActivityResumed(Activity arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivitySaveInstanceState(Activity arg0, Bundle arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityStarted(Activity arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onActivityStopped(Activity arg0) {
        // TODO Auto-generated method stub

    }

    // Gionee <gaoj> <2013-9-3> add for CR00882174 end

    // Gionee <lihq> <2013-10-31> add for CR00873172 begin
    public void setStretchEnable(boolean enable) {
        // gionee maxw delete begin
        // hide this function
        // mStretchEnable = enable;
        // if (enable) {
        // setOverScrollMode(AbsListView.OVER_SCROLL_NEVER);
        // }
        // gionee maxw delete end
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        mGesture.onTouchEvent(ev);
        if (mFastScroller != null) {
            boolean intercepted = mFastScroller.onTouchEvent(ev);
            if (intercepted) {
                return true;
            }
        }
        final int action = ev.getAction();

        if (mStretchEnable) {
            final int y = (int) ev.getY();
            final int x = (int) ev.getX();
            mMotionPosition = pointToPosition(x, y);
            mMotionY = y;
            initVelocityTrackerIfNotExists();
            mVelocityTracker.addMovement(ev);
            switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mStretchAnimationa != null
                        && mStretchAnimationa.isRunning()) {
                    mStretchAnimationa.revertViewSize();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_OUTSIDE:
                break;
            default:
                if (mStretchAnimationa != null
                        && mStretchAnimationa.isRunning()) {
                    mStretchAnimationa.overAnimation(0, false);
                }
                break;
            }
            return super.onTouchEvent(ev);
        } else {
//            boolean consume = doSlideMultiChoice(ev);
//            if (consume) {
//                return true;
//            }
            // Gionee <weidong> <2016-4-20> modify for CR01679083 begin
            // if (getScrollY() == 0) {
            // boolean ret = doForceTouchEvent(ev);
            // if (ret) {
            // return true;
            // }
            // } else {
            // switch (action & MotionEvent.ACTION_MASK) {
            // case MotionEvent.ACTION_CANCEL:
            // case MotionEvent.ACTION_UP:
            // dismissForceTouchWindow();
            // break;
            // default:
            // break;
            // }
            // }
            // Gionee <weidong> <2016-4-20> modify for CR01679083 end
        }
        return super.onTouchEvent(ev);
    }

    private boolean isInvalidEvent = false;
    private boolean isOutEvent = false;
    private int mTouchPosition;
    private int mOldPos = -1;
    private boolean isChecked = false;
    private CyeeMultiChoiceAdapter mAdapter;
    private final ListInfo mInfo = new ListInfo();
    private final Position mCurTouchPos = new Position(0, 0);
    private final static int SLIDE_DISTANCE = 20; // px
    private final static int SLIDE_TIMER = 5; // px
    private final static int SLIDE_MSG_DOWN = 1; // Msgid
    private final static int SLIDE_MSG_UP = 2; // Msgid
    private final static int CHECKBOX_WIDTH = 52;// dp
    private final static int CHECKBOX_RIGHT_PADDING = 6;// dp

    private static class Position {
        private int xPos;
        private int yPos;

        public Position(int xpos, int ypos) {
            this.xPos = xpos;
            this.yPos = ypos;
        }
    }

    private void getListInfo() {
        mInfo.lastPos = getLastVisiblePosition();
        mInfo.firstPos = getFirstVisiblePosition();
        mInfo.childCnt = getChildCount();
        mInfo.firstItemView = getChildAt(0);
        mInfo.lastItemView = getChildAt(mInfo.childCnt - 1);
        if (null != mInfo.lastItemView) {
            mInfo.itemH = mInfo.lastItemView.getHeight();
        }
    }

    private static class ListInfo {
        private int lastPos;
        private int firstPos;
        private int childCnt;
        private View lastItemView;
        private View firstItemView;
        private int itemH;
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case SLIDE_MSG_DOWN:
                if (isInvalidEvent) {
                    smoothScrollBy(SLIDE_DISTANCE, SLIDE_TIMER);
                    mHandler.sendEmptyMessageDelayed(SLIDE_MSG_DOWN,
                            SLIDE_TIMER);
                }
                break;
            case SLIDE_MSG_UP:
                if (isInvalidEvent) {
                    smoothScrollBy(-SLIDE_DISTANCE, SLIDE_TIMER);
                    mHandler.sendEmptyMessageDelayed(SLIDE_MSG_UP, SLIDE_TIMER);
                }
                break;
            default:
                break;
            }
        }
    };

    private boolean isInCheckBoxRect(Position pos) {
        boolean in = false;
        if (pos.xPos >= mScreenWidth
                - dp2px(CHECKBOX_WIDTH + CHECKBOX_RIGHT_PADDING)/*
                                                                 * && pos.yPos
                                                                 * <=
                                                                 * mScreenWidth
                                                                 * - dp2px(
                                                                 * CHECKBOX_RIGHT_PADDING
                                                                 * )
                                                                 */) {
            in = true;
        }

        return in;
    }

    private boolean isInBottom(Position pos) {
        boolean inBottom = false;

        if (pos.yPos >= getBottom() - mInfo.itemH && pos.yPos < getBottom()) {
            inBottom = true;
        }

        return inBottom;
    }

    private boolean isInTop(Position pos) {
        boolean inTop = false;

        if (pos.yPos <= mInfo.itemH && pos.yPos > getTop()) {
            inTop = true;
        }

        return inTop;
    }

    private boolean doActionDown(MotionEvent ev) {
        getListInfo();
        
        if (null == mAdapter) {
            ListAdapter adapter = getAdapter();
            if (adapter instanceof HeaderViewListAdapter) {
                adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
            }
            
            if (!(adapter instanceof CyeeMultiChoiceAdapter)) {
                return false;
            } else {
                mAdapter = (CyeeMultiChoiceAdapter) adapter;
            }
        }
        
        boolean enter = mAdapter.isEnterMultiChoice();
        if (!enter) {
            return false;
        }

        // 50dp 首次点击时，需要在选择框区域，后续事件才有效
        if (isInCheckBoxRect(new Position((int) ev.getX(), (int) ev.getY()))) {
            isInvalidEvent = true;
            isOutEvent = false;
            mOldPos = mTouchPosition = pointToPosition((int) ev.getX(),
                    (int) ev.getY());
            isChecked = mAdapter.isChecked(mTouchPosition);
            isChecked = !isChecked;
            mAdapter.setItemCheckedWithUpdate(mTouchPosition, isChecked);

            return true;
        }

        return false;
    }
    
    private boolean doActionMove(MotionEvent ev) {
        if (!isInvalidEvent) {
            return false;
        }
        
        if (isOutEvent) {
            return true;
        }
        
        if (!isInCheckBoxRect(new Position((int) ev.getX(), (int) ev.getY()))) {
            // 非选择框区域时此次滑动操作结束
            isOutEvent = true;
            mHandler.removeCallbacksAndMessages(null);
            return true;
        }
        mCurTouchPos.xPos = (int) ev.getX();
        mCurTouchPos.yPos = (int) ev.getY();

        int curPos = pointToPosition((int) ev.getX(), (int) ev.getY());
        if (mOldPos != curPos) {
            boolean tmp = isChecked;

            if (Math.abs(mTouchPosition - curPos) < Math.abs(mTouchPosition
                    - mOldPos)) {
                tmp = !isChecked;
                if (mTouchPosition != mOldPos) {
                    boolean checked = mAdapter.isChecked(mOldPos);
                    if (checked != tmp) {
                        mAdapter.setItemCheckedWithUpdate(mOldPos, tmp);
                    }
                }
            } else {
                if (mTouchPosition != curPos) {
                    boolean checked = mAdapter.isChecked(curPos);
                    if (checked != tmp) {
                        mAdapter.setItemCheckedWithUpdate(curPos, tmp);
                    }
                }
            }
            mOldPos = curPos;
        }

        if (isInBottom(new Position((int) ev.getX(), (int) ev.getY()))) {
            smoothScrollBy(SLIDE_DISTANCE, SLIDE_TIMER);
            if (!mHandler.hasMessages(SLIDE_MSG_DOWN)) {
                mHandler.sendEmptyMessageDelayed(SLIDE_MSG_DOWN,
                        SLIDE_TIMER);
            }
        } else if (isInTop(new Position((int) ev.getX(), (int) ev.getY()))) {
            smoothScrollBy(-SLIDE_DISTANCE, SLIDE_TIMER);
            if (!mHandler.hasMessages(SLIDE_MSG_UP)) {
                mHandler.sendEmptyMessageDelayed(SLIDE_MSG_UP, SLIDE_TIMER);
            }
        } else {
            mHandler.removeCallbacksAndMessages(null);
        }

        return true;
    }
    
    private boolean doActionUp(MotionEvent ev) {
        mHandler.removeCallbacksAndMessages(null);
        
        if (!isInvalidEvent) {
            return false;
        }
        mAdapter.updateActionModeMenu();
        
        if (isOutEvent) {
            isInvalidEvent = false;
            isOutEvent = false;
            return true;
        }
        
        if (!isInCheckBoxRect(new Position((int) ev.getX(), (int) ev.getY()))) {
            // 非选择框区域时不处理最后的事件
            isInvalidEvent = false;
            isOutEvent = false;
            return true;
        }
        int curPos = pointToPosition((int) ev.getX(), (int) ev.getY());

        if (null != mInfo.lastItemView && ev.getY() > mInfo.lastItemView.getY()) {
            curPos = mInfo.lastPos;
        } else if (null != mInfo.firstItemView && ev.getY() < mInfo.firstItemView.getY()) {
            curPos = mInfo.firstPos;
        }

        if (curPos != AbsListView.INVALID_POSITION) {
            if (curPos > mTouchPosition) {
                for (int i = 0; i <= curPos - mTouchPosition; i++) {
                    boolean checked = mAdapter
                            .isChecked(mTouchPosition + i);
                    if (checked != isChecked) {
                        mAdapter.setItemCheckedWithUpdate(mTouchPosition
                                + i, isChecked);
                    }
                }
            } else {
                for (int i = 0; i <= mTouchPosition - curPos; i++) {
                    boolean checked = mAdapter.isChecked(curPos + i);
                    if (checked != isChecked) {
                        mAdapter.setItemCheckedWithUpdate(curPos + i,
                                isChecked);
                    }
                }
            }
        }
        isInvalidEvent = false;
        isOutEvent = false;
        
        return true;
    }
    
    private boolean doSlideMultiChoice(MotionEvent ev) {
        boolean consume = false;
        
        int action = ev.getAction();
        switch(action) {
        case MotionEvent.ACTION_DOWN:
            consume = doActionDown(ev);
            break;
        case MotionEvent.ACTION_MOVE:
            consume = doActionMove(ev);
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            consume = doActionUp(ev);
            break;
        }
        
        return consume;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    public void dismissForceTouchWindow() {
        if (null != mForceTouchListView) {
            mForceTouchListView.dismiss();
        }
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
            int scrollY, int scrollRangeX, int scrollRangeY,
            int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX,
                getMaxScrollAmount(), isTouchEvent);

        /*
         * gionee shaozj modify begin // Gionee <lihq> <2013-12-31> modify for
         * CR00958049 begin // Gionee <lihq> <2013-11-14> modify for CR00873172
         * begin if (isOverBottom() || isFlingState()) { return
         * super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
         * scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent); } //
         * Gionee <lihq> <2013-11-14> modify for CR00873172 end // Gionee <lihq>
         * <2013-12-31> modify for CR00958049 end if (mStretchAnimationa ==
         * null) { mStretchAnimationa = new CyeeStretchAnimationa(); } if
         * (!mStretchAnimationa.isRunning()) { if (deltaY > 0 &&
         * getLastVisiblePosition() == getCount() - 1) { // TOGO scroll down if
         * (mStretchAnimationa.isGoUp()) { mStretchAnimationa.setGoUp(false); }
         * 
         * } else if (deltaY < 0 && getFirstVisiblePosition() == 0) { if
         * (!mStretchAnimationa.isGoUp()) { mStretchAnimationa.setGoUp(true); }
         * } mStretchAnimationa.addChildren(getChildren()); } if
         * (!mStretchAnimationa.isGoUp()) { mMotionPosition = getCount() -
         * mMotionPosition; } mStretchAnimationa.overScroll(mMotionY,
         * mMotionPosition, deltaY); return super.overScrollBy(deltaX, deltaY,
         * scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX,
         * maxOverScrollY, isTouchEvent); gionee shaozj modify end
         */
    }

    private List<View> getChildren() {
        List<View> children = new ArrayList<View>();
        if (mStretchAnimationa.isGoUp()) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != null) {
                    children.add(child);
                }
            }
        } else {
            int count = -1;
            for (int i = getChildCount() - 1; i > count; i--) {
                View child = getChildAt(i);
                if (child != null) {
                    children.add(child);
                }
            }
        }
        return children;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        // printChildInfo();
        // Gionee <lihq> <2013-11-11> modify for CR00873172 begin
        if (needModifiedDivider()) {
            modifyDivider();
        }
        // Gionee <lihq> <2013-11-11> modify for CR00873172 end
        if (mStretchEnable
                && mStretchAnimationa != null
                && !mStretchAnimationa.isGoUp()
                && (mStretchAnimationa.isRunning() || mStretchAnimationa
                        .isLastUpdate())) {
            correctLayout();
        }
        super.dispatchDraw(canvas);
    }

    private void correctLayout() {
        int bottom = getMeasuredHeight() - getPaddingBottom();
        // Gionee <lihq> <2013-11-20> add for CR00953331 begin
        int dividerHeight = getDividerHeight();
        // Gionee <lihq> <2013-11-20> add for CR00953331 end
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            int l = child.getLeft();
            int r = child.getMeasuredWidth() + l;
            int h = child.getMeasuredHeight();
            // Gionee <lihq> <2013-12-31> modify for CR00958049 begin
            int top = child.getTop();
            if (validTop(bottom, h, top)) {
                child.layout(l, bottom - h, r, bottom);
                // Gionee <lihq> <2013-11-20> modify for CR00953331 begin
                // bottom -= h;
                bottom = bottom - h - dividerHeight;
                // Gionee <lihq> <2013-11-20> modify for CR00953331 end
            }
            // Gionee <lihq> <2013-12-31> modify for CR00958049 end
        }
    }

    private void printChildInfo() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null) {
                Log.d(TAG, "child " + i + " : " + child);
                Log.d(TAG, "height " + i + " : " + child.getHeight());
                Log.d(TAG, "\n");
            }
        }
    }

    private boolean isOverBottom() {
        boolean isBottom = true;
        View bottom = getChildAt(getChildCount() - 1);
        if (bottom != null) {
            isBottom = bottom.getBottom() < (getMeasuredHeight() - getPaddingBottom());
        }
        return (getFirstVisiblePosition() == 0
                && getLastVisiblePosition() == getCount() - 1 && isBottom);
    }

    // Gionee <lihq> <2013-12-31> add for CR00958049 begin
    private boolean isFlingState() {
        return mLastScrollState == OnScrollListener.SCROLL_STATE_FLING;
    }

    private boolean validTop(int bottom, int h, int top) {
        return Math.abs(top - (bottom - h)) < (h * 2);
    }

    // Gionee <lihq> <2013-12-31> add for CR00958049 end

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        // TODO Auto-generated method stub
        mOnscrListener = l;
        super.setOnScrollListener(this);
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (isInvalidEvent) {
            int curPos = pointToPosition(mCurTouchPos.xPos, mCurTouchPos.yPos);
            if (curPos < totalItemCount) {
                if (mTouchPosition != curPos) {
                    if (null != mAdapter
                            && (mAdapter instanceof CyeeMultiChoiceAdapter)) {
                        boolean checked = mAdapter.isChecked(curPos);
                        if (checked != isChecked) {
                            mAdapter.setItemCheckedWithUpdate(curPos, isChecked);
                        }
                    }
                }
            }
        }

        if (mOnscrListener != null) {
            mOnscrListener.onScroll(view, firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }
        if (mFastScroller != null) {
            mFastScroller.onScroll(firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mFastScroller != null) {
            mFastScroller.onItemCountChanged(getChildCount(), getCount());
        }
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (mFastScroller != null) {
            mFastScroller.setScrollbarPosition(getVerticalScrollbarPosition());
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // gionee maxw modify begin
        if (scrollState != mLastScrollState) {
            if (mOnscrListener != null) {
                mLastScrollState = scrollState;
                mOnscrListener.onScrollStateChanged(view, scrollState);
            }
        }
        if (mStretchEnable) {
            scrollStateChanged(view, scrollState);
        }
        // gionee maxw modify end
    }

    private void scrollStateChanged(AbsListView view, int scrollState) {
        if (mLastScrollState == 2 && scrollState == 0 && !isOverBottom()) {
            if (mStretchAnimationa == null) {
                mStretchAnimationa = new CyeeStretchAnimationa();
            }
            if (getFirstVisiblePosition() == 0
                    || getLastVisiblePosition() == getCount() - 1) {
                float incrase = computeIncrease();
                if (incrase != 0 && !mStretchAnimationa.isRunning()) {
                    mStretchAnimationa.addChildren(getChildren());
                    mStretchAnimationa.overAnimation(incrase, true);
                }
            }
        }
        mLastScrollState = scrollState;
    }

    private float computeIncrease() {
        // Gionee <lihq> <2013-12-9> add for CR00967433 begin
        if (mVelocityTracker == null) {
            return 0;
        }
        // Gionee <lihq> <2013-12-9> add for CR00967433 end
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1, mMaximumVelocity);
        final int initialVelocity = (int) velocityTracker.getYVelocity(0);
        Log.d(TAG, "initialVelocity--> " + initialVelocity);
        mStretchAnimationa.setGoUp(initialVelocity >= 0);
        if (Math.abs(initialVelocity) > 5 && Math.abs(initialVelocity) < 10) {
            return 1.1f;
        }
        if (Math.abs(initialVelocity) >= 10 && Math.abs(initialVelocity) < 15) {
            return 1.2f;
        }
        if (Math.abs(initialVelocity) >= 15) {
            return 1.3f;
        }
        return 0;
    }

    // Gionee <lihq> <2013-11-19> add for CR00873172 begin
    /**
     * Add for ViewPager Left-Right Effect bug. Control it to remain origin
     * translationX.
     */
    private void revertChildrenTrans() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null && child.getTranslationX() < 0) {
                child.setTranslationX(0);
            }
        }
    }

    public void setViewPagerEffectEnable(boolean enable) {
        mVPEffectEnable = enable;
    }

    public boolean getViewPagerEffectEnable() {
        return mVPEffectEnable;
    }

    @Override
    protected void layoutChildren() {
        // TODO Auto-generated method stub
        if (getViewPagerEffectEnable()) {
            revertChildrenTrans();
        }
        super.layoutChildren();
    }

    // Gionee <lihq> <2013-11-21> add for field begin
    protected void invokeScrollStatedChanged(AbsListView view, int scrollState) {
        if (mStretchEnable) {
            scrollStateChanged(view, scrollState);
        }
    }

    // Gionee <lihq> <2013-11-21> add for field end
    // Gionee <lihq> <2013-11-19> add for CR00873172 end
    // Gionee <lihq> <2013-10-31> add for CR00873172 end

    public static final int RETURN_TOP_DURATION = 208;
    public static final int FRAME_DURATION = 16;

    public void returnTop() {
        int totalCount = getCount();
        int visiableCount = getChildCount();
        int sourcePos = getFirstVisiblePosition();

        if (totalCount == 0 || visiableCount == 0 || FRAME_DURATION == 0
                || sourcePos == 0) {
            return;
        }

        final int frameDuration = RETURN_TOP_DURATION / (sourcePos - 0);
        Log.d(TAG, "sourcePos=" + sourcePos + ", frameDuration="
                + frameDuration);

        if (frameDuration > FRAME_DURATION) {
            smoothScrollToPosition(0);
        } else {
            int frameCount = RETURN_TOP_DURATION / FRAME_DURATION;
            final int distance = getFirstVisiblePosition() / frameCount;
            postOnAnimation(new Runnable() {

                @Override
                public void run() {
                    int currentPos = getFirstVisiblePosition();
                    if (currentPos == 0) {
                        return;
                    }
                    int tagetPos = currentPos - distance;
                    if (tagetPos < 0) {
                        tagetPos = 0;
                    }
                    setSelection(tagetPos);
                    invalidate();
                    postOnAnimation(this);
                }
            });
        }

    }

    private boolean doForceTouchEvent(MotionEvent ev) {
        if (null != mForceTouchListView) {
            return mForceTouchListView.doCyeeForceTouchEvent(ev);
        }
        
        return false;
    }

    public void setForceTouchClickCallback(
            CyeeListViewForceTouchClickCallback callback) {
        if (null != mForceTouchListView) {
            mForceTouchListView.setCyeeListViewForceTouchClickCallback(callback);
        }
    }

    public void setForceTouchPreviewCallback(
            CyeeListViewForceTouchPreviewCallback callback) {
        if (null != mForceTouchListView) {
            mForceTouchListView.setForceTouchPreviewCallback(callback);
        }
    }

    public void setForceTouchMenuCallback(
            CyeeListViewForceTouchMenuCallback callback) {
        if (null != mForceTouchListView) {
            mForceTouchListView.setForceTouchMenuCallback(callback);
        }
    }

    public void onDestroyForceTouch() {
        if (null != mForceTouchListView) {
            mForceTouchListView.onDestroyForceTouch();
        }
    }

    void setMultiChoiceScrollListener(MultiChoiceScrollListener listener) {
        this.mScrollListener = listener;
    }
}
