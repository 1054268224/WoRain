package com.cyee.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.cyee.internal.R;
import com.cyee.internal.view.CyeeActionBarPolicy;

import cyee.app.CyeeActionBar;

public class CyeeActionBarContainer extends FrameLayout {
    private CyeeExtraViewContainer mExtraViewContainer;
    private View mExtraView;
    private CyeeTabContainerView mTabContainer;
    private CyeeActionBarView mActionBarView;
    private Drawable mBackground;
    private Drawable mStackedBackground;
    private Drawable mSplitBackground;
    private int mContainerHeight;
    private int mExtraViewContainerHeight;
    private View mActivityContent;
    private CyeeActionBar.OnExtraViewDragListener mDragListener;
    private boolean mIsDragOpenStart = false;
    private boolean mIsDragOpenEnd = false;
    private boolean mIsDragCloseEnd = false;
    private boolean mIsDragOpened = false;
    private boolean mIsDragClosed = false;
    private ValueAnimator mAnimator;
    private final Context mContext;
    private boolean mHasEmbeddedTabs = false;

    private boolean mIsTransitioning;
    private final boolean mIsSplit = false;
    private boolean mIsStacked;

    public CyeeActionBarContainer(Context context) {
        this(context, null);
    }

    public CyeeActionBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundDrawable(null);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CyeeActionBar);
        mBackground = a.getDrawable(R.styleable.CyeeActionBar_cyeebackground);
        mStackedBackground = a
                .getDrawable(R.styleable.CyeeActionBar_cyeebackgroundStacked);
        // Gionee <weidong> <2016-04-22> modify for CR01683201 begin
        int color = getResources().getColor(com.cyee.internal.R.color.cyee_actionbar_background_color_light_normal);
        try {
            color = a.getColor(R.styleable.CyeeActionBar_cyeebackground, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setBackgroundColor(color);
        // Gionee <weidong> <2016-04-22> modify for CR01683201 end
        
//		mContainerHeight = a.getLayoutDimension(R.styleable.CyeeActionBar_cyeeheight, 0);
//		if (getId() == R.id.split_action_bar) {
//			mIsSplit = true;
//			mSplitBackground = a.getDrawable(R.styleable.GioneeActionBar_backgroundSplit);
//		}
        a.recycle();
//		setWillNotDraw(mIsSplit ? mSplitBackground == null : mBackground == null
//				&& mStackedBackground == null);
//		setWillNotDraw(mBackground == null);
//		mHasEmbeddedTabs = CyeeActionBarPolicy.get(mContext).hasEmbeddedTabs();
//		setBackgroundDrawable(mBackground);

        setWillNotDraw(mIsSplit ? mSplitBackground == null : mBackground == null
                && mStackedBackground == null);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mActionBarView = (CyeeActionBarView) findViewById(com.cyee.internal.R.id.cyee_action_bar);
        mExtraViewContainer = (CyeeExtraViewContainer) findViewById(com.cyee.internal.R.id.cyee_action_bar_intellgent_container);
    }

    public void setPrimaryBackground(Drawable bg) {
        if (mBackground != null) {
            mBackground.setCallback(null);
            unscheduleDrawable(mBackground);
        }
        mBackground = bg;
        if (bg != null) {
            bg.setCallback(this);
        }
        setWillNotDraw(mIsSplit ? mSplitBackground == null : mBackground == null
                && mStackedBackground == null);
        invalidate();
    }

    public void setStackedBackground(Drawable bg) {
        if (mStackedBackground != null) {
            mStackedBackground.setCallback(null);
            unscheduleDrawable(mStackedBackground);
        }
        mStackedBackground = bg;
        if (bg != null) {
            bg.setCallback(this);
        }
        setWillNotDraw(mIsSplit ? mSplitBackground == null : mBackground == null
                && mStackedBackground == null);
        invalidate();
    }

    public void setSplitBackground(Drawable bg) {
//        if (mSplitBackground != null) {
//            mSplitBackground.setCallback(null);
//            unscheduleDrawable(mSplitBackground);
//        }
//        mSplitBackground = bg;
//        if (bg != null) {
//            bg.setCallback(this);
//        }
//        setWillNotDraw(mIsSplit ? mSplitBackground == null :
//                mBackground == null && mStackedBackground == null);
//        invalidate();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        final boolean isVisible = visibility == VISIBLE;
        if (mBackground != null)
            mBackground.setVisible(isVisible, false);
        if (mStackedBackground != null)
            mStackedBackground.setVisible(isVisible, false);
//        if (mSplitBackground != null) mSplitBackground.setVisible(isVisible, false);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        /* modify for activity onStop delay 10s after change color */
        // Gionee <weidong> <2017-09-21> modify for 85157 begin
        return /*
                * (who == mBackground && !mIsSplit) || (who ==
                * mStackedBackground && mIsStacked) || (who == mSplitBackground
                * && mIsSplit) ||
                */super.verifyDrawable(who);
        // Gionee <weidong> <2017-09-21> modify for 85157 end
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mBackground != null && mBackground.isStateful()) {
            mBackground.setState(getDrawableState());
        }
        if (mStackedBackground != null && mStackedBackground.isStateful()) {
            mStackedBackground.setState(getDrawableState());
        }
        if (mSplitBackground != null && mSplitBackground.isStateful()) {
            mSplitBackground.setState(getDrawableState());
        }
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mBackground != null) {
            mBackground.jumpToCurrentState();
        }
        if (mStackedBackground != null) {
            mStackedBackground.jumpToCurrentState();
        }
        if (mSplitBackground != null) {
            mSplitBackground.jumpToCurrentState();
        }
    }

//    @Override
//    public void onResolveDrawables(int layoutDirection) {
//        super.onResolveDrawables(layoutDirection);
//        if (mBackground != null) {
//            mBackground.setLayoutDirection(layoutDirection);
//        }
//        if (mStackedBackground != null) {
//            mStackedBackground.setLayoutDirection(layoutDirection);
//        }
//        if (mSplitBackground != null) {
//            mSplitBackground.setLayoutDirection(layoutDirection);
//        }
//    }

    public void setTransitioning(boolean isTransitioning) {
        mIsTransitioning = isTransitioning;
        setDescendantFocusability(isTransitioning ? FOCUS_BLOCK_DESCENDANTS : FOCUS_AFTER_DESCENDANTS);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsTransitioning || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onHoverEvent(MotionEvent ev) {
        super.onHoverEvent(ev);

        // An action bar always eats hover events.
        return true;
    }

    public void setTabContainer(CyeeTabContainerView tabContainer) {
        if (mTabContainer != null) {
            removeView(mTabContainer);
        }
        mTabContainer = tabContainer;
//		mTabContainer = new GnTabContainerView(mContext);
        if (tabContainer != null) {
//			tabContainer.setLayoutParams(lp);
            addView(tabContainer);
            final ViewGroup.LayoutParams lp = tabContainer.getLayoutParams();
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = LayoutParams.WRAP_CONTENT;
            tabContainer.setAllowCollapse(false);
        }

//		mActionBarView.setTop(0);
//		mActionBarView.setBottom(mContainerHeight);
    }

    public View getTabContainer() {
        return mTabContainer;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (mIsSplit) {
            if (mSplitBackground != null)
                mSplitBackground.draw(canvas);
        } else {
            if (mBackground != null) {
                //shaozj modify begin
                //mBackground.setBounds(getLeft(), getTop(), getRight(), getBottom());
                if (mActionBarView != null) {
                    mBackground.setBounds(mActionBarView.getLeft(), mActionBarView.getTop(),
                            mActionBarView.getRight(), mActionBarView.getBottom());
                }
                //shaozj modify end
                mBackground.draw(canvas);
            }
            if (mStackedBackground != null && mIsStacked) {
                //shaozj modify begin
                //mStackedBackground.setBounds(getLeft(), getTop(), getRight(), getBottom());
                if (mTabContainer != null) {
                    mStackedBackground.setBounds(mTabContainer.getLeft(), mTabContainer.getTop(),
                            mTabContainer.getRight(), mTabContainer.getBottom());
                }
                //shaozj modify end
                mStackedBackground.draw(canvas);
            }
        }
    }

    @Override
    public ActionMode startActionModeForChild(View child, ActionMode.Callback callback) {
        // No starting an action mode for an action bar child! (Where would it go?)
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		int maxHeight = mContainerHeight >= 0 ? mContainerHeight : MeasureSpec.getSize(heightMeasureSpec);
////		if (mTabContainer != null) {
////			setMeasuredDimension(getMeasuredWidth(), Math.min(mTabContainer.getMeasuredHeight(), maxHeight));
////		}

//		setMeasuredDimension(getMeasuredWidth(), mContainerHeight);

        if (mActionBarView == null)
            return;

        final LayoutParams lp = (LayoutParams) mActionBarView.getLayoutParams();
//		final int actionBarViewHeight = mActionBarView.isCollapsed() ? 0 : mActionBarView.getMeasuredHeight()
//				+ lp.topMargin + lp.bottomMargin;
        final int actionBarViewHeight = mActionBarView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

        setMeasuredDimension(getMeasuredWidth(),
                MeasureSpec.makeMeasureSpec(actionBarViewHeight, MeasureSpec.AT_MOST));

        if (mTabContainer != null && mTabContainer.getVisibility() != GONE) {
            final int mode = MeasureSpec.getMode(heightMeasureSpec);
            if (mode == MeasureSpec.AT_MOST) {
                final int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(getMeasuredWidth(),
                        Math.min(actionBarViewHeight + mTabContainer.getMeasuredHeight(), maxHeight));
            }
        }

    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        mInitActionBarBottom = getBottom();
////		mInitTabContainerTop = mTabContainer.getTop();
        mInitActivityTop = mInitActionBarBottom;
//
        mExtraViewContainer.setBottom(0);

        if (mIsDragEnable) {
            mExtraView = mExtraViewContainer.getExtraView();
            if (mExtraView != null) {
                mExtraViewContainerHeight = mExtraView.getHeight();
            }
        }

        final boolean hasTabs = mTabContainer != null && mTabContainer.getVisibility() != GONE;

        if (mTabContainer != null && mTabContainer.getVisibility() != GONE) {
            final int containerHeight = getMeasuredHeight();
            final int tabHeight = mTabContainer.getMeasuredHeight();

            if ((mActionBarView.getDisplayOptions() & ActionBar.DISPLAY_SHOW_HOME) == 0) {
                // Not showing home, put tabs on top.
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);

                    if (child == mTabContainer)
                        continue;

                    if (!mActionBarView.isCollapsed()) {
                        child.offsetTopAndBottom(tabHeight);
                    }
                }
                mTabContainer.layout(l, 0, r, tabHeight);
            } else {
                mTabContainer.layout(l, containerHeight - tabHeight, r, containerHeight);
            }
        }

        boolean needsInvalidate = false;
        if (mIsSplit) {
            if (mSplitBackground != null) {
                mSplitBackground.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                needsInvalidate = true;
            }
        } else {
            if (mBackground != null) {
                mBackground.setBounds(mActionBarView.getLeft(), mActionBarView.getTop(),
                        mActionBarView.getRight(), mActionBarView.getBottom());
                needsInvalidate = true;
            }
            if ((mIsStacked = hasTabs && mStackedBackground != null)) {
                mStackedBackground.setBounds(mTabContainer.getLeft(), mTabContainer.getTop(),
                        mTabContainer.getRight(), mTabContainer.getBottom());
                needsInvalidate = true;
            }
        }

        if (needsInvalidate) {
            invalidate();
        }

    }

//	@Override
//	protected void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//
//		if (mTabContainer != null ) {
//			ViewGroup.LayoutParams lp = mTabContainer.getLayoutParams();
//			if (lp != null) {
//				lp.width = LayoutParams.WRAP_CONTENT;
//				lp.height = LayoutParams.MATCH_PARENT;
//			}
//			mTabContainer.setLayoutParams(lp);
////			mTabScrollView.setAllowCollapse(true);
//		}
//	}

    private int mInitActionBarBottom;
    private int mInitActivityTop;
    private float mOriY;
    private int mOriBottom;
    private int mOriTabTop;
    private int mOriTabBottom;
    private int mOriActionBarViewTop;
    private int mOriActionBarVieBottom;
    private int mOriActivityContentTop;
    private int mOriIntellgentContainerBottom;
    private int mDistance;
    private static final int ANIM_DURATION = 300;
    private boolean mIsDragEnable = false;

    // Gionee <gaoj> <2013-9-9> add for CR00890470 begin
    private int mFisActivityContentTop = -1;
    // Gionee <gaoj> <2013-9-9> add for CR00890470 end
    
    public void setDragEnable(boolean enable) {
        mIsDragEnable = enable;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mIsDragEnable || mExtraView == null) {
            return super.dispatchTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mAnimator != null) {
                    mAnimator.cancel();
                }
                mOriY = event.getRawY();
                mOriBottom = getBottom();
                mOriIntellgentContainerBottom = mExtraViewContainer.getBottom();
                
                mOriTabTop = mActionBarView.getTop();
                mOriTabBottom = mActionBarView.getBottom();
                if (!mHasEmbeddedTabs && mTabContainer != null) {
                    mOriTabTop = mTabContainer.getTop();
                    mOriTabBottom = mTabContainer.getBottom();
                }
                
                mOriActivityContentTop = getActivityContent().getTop();
                if (mFisActivityContentTop == -1) {
                    mFisActivityContentTop = getActivityContent().getTop();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mDistance = (int) (event.getRawY() - mOriY);
                if (mDistance != 0) {
                    int bottom = mOriBottom + mDistance;
                    int tabContainerTop = mOriTabTop + mDistance;
                    int tabContainerBottom = mOriTabBottom + mDistance;
//					int actionbarViewTop = mOriActionBarViewTop + mDistance;
//					int actionbarViewBottom = mOriActionBarVieBottom + mDistance;
                    int activityTop = mOriActivityContentTop + mDistance;
                    int extraViewBottom = mOriIntellgentContainerBottom + mDistance;
                    if (bottom < mInitActionBarBottom) {
                        bottom = mInitActionBarBottom;
                        tabContainerTop = 0;
                        tabContainerBottom = mInitActionBarBottom;
//						activityTop = mInitActivityTop;
                        //Gionee <gaoj> <2013-9-9> modify for CR00890470 begin
                        activityTop = mFisActivityContentTop;
                        //Gionee <gaoj> <2013-9-9> modify for CR00890470 end
                        extraViewBottom = 0;
                        mIsDragCloseEnd = true;
                    } else if (mExtraViewContainerHeight > 0
                            && bottom > (mExtraViewContainerHeight + mInitActionBarBottom)) {
                        bottom = mExtraViewContainerHeight + mInitActionBarBottom;
                        tabContainerTop = mExtraViewContainerHeight;
                        tabContainerBottom = bottom;
                        //Gionee <gaoj> <2013-9-9> modify for CR00890470 begin
                        activityTop = mExtraViewContainerHeight + mFisActivityContentTop;
                        //Gionee <gaoj> <2013-9-9> modify for CR00890470 end
                        extraViewBottom = mExtraViewContainerHeight;
                        mIsDragOpenEnd = true;
                        mDistance = mExtraViewContainerHeight;
                    } else {
                        mIsDragOpenEnd = false;
                        mIsDragOpened = false;
                        mIsDragCloseEnd = false;
                        mIsDragClosed = false;
                    }
                    setBottom(bottom);
                    getActivityContent().setTop(activityTop);
//                    getActivityContent().setTop(mDistance);
                    mExtraViewContainer.setBottom(extraViewBottom);
                    layoutExtraView(mExtraViewContainer.getHeight());
                    if (mTabContainer != null) {
                        mTabContainer.setTop(tabContainerTop);
                        mTabContainer.setBottom(tabContainerBottom);
                        mTabContainer.setClickable(false);
                    }

                    mActionBarView.setTop(tabContainerTop);
//					mActionBarView.setBottom(tabContainerBottom);
                    mActionBarView.setBottom(bottom);
                    mActionBarView.setClickable(false);

                    if (mDragListener != null) {
                        if (mIsDragOpenEnd) {
                            if (!mIsDragOpened) {
                                mIsDragOpened = true;
                                mIsDragOpenStart = false;
                                mDragListener.onDragUpdate(1f, extraViewBottom);
                                mDragListener.onDragOpenEnd();
                            }
                        } else if (mIsDragCloseEnd) {
                            if (!mIsDragClosed) {
                                mIsDragClosed = true;
                                mIsDragOpenStart = false;
                                mDragListener.onDragUpdate(0f, 0);
                                mDragListener.onDragCloseEnd();
                            }

                        } else {
                            if (mIsDragOpenStart) {
                                mDragListener.onDragUpdate((float) extraViewBottom
                                        / mExtraViewContainerHeight, extraViewBottom);
                            } else {
                                mIsDragOpenStart = true;
                                mDragListener.onDragOpenStart();
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getBottom() == mInitActionBarBottom) {
                    if (mTabContainer != null) {
                        mTabContainer.setClickable(true);
                    }
                    mActionBarView.setClickable(true);
                } else {
                    if (!mIsDragOpenEnd) {
                        actionbarCloseAnimation(mExtraViewContainer.getBottom());
                    }
                }
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        CyeeActionBarPolicy policy = CyeeActionBarPolicy.get(mContext);
        mHasEmbeddedTabs = policy.hasEmbeddedTabs();
//		layoutExtraView(0);
//
//		mActionBarView.setTop(0);
//		mActionBarView.setBottom(mContainerHeight);
//
        if (mDragListener != null) {
            mDragListener.onDragUpdate(0f, 0);
            mDragListener.onDragCloseEnd();
        }
        mContainerHeight = policy.getTabContainerHeight();
    }

    private void layoutExtraView(int containerHeight) {
        int viewHeight = mExtraView.getHeight();
        int top = (containerHeight - viewHeight) / 2;
        int bottom = top + viewHeight;
        mExtraView.setTop(top);
        mExtraView.setBottom(bottom);
    }

    private void actionbarCloseAnimation(final int y) {
        mAnimator = ValueAnimator.ofInt(y, 0);
        mAnimator.setDuration(ANIM_DURATION);
        mAnimator.start();
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int y = (Integer) animation.getAnimatedValue();
                setBottom(mInitActionBarBottom + y);
                // Gionee <gaoj> <2013-9-9> modify for CR00890470 begin
                getActivityContent().setTop(y + mFisActivityContentTop);
                // Gionee <gaoj> <2013-9-9> modify for CR00890470 end
                mExtraViewContainer.setBottom(y);
                layoutExtraView(mExtraViewContainer.getHeight());
                if (mTabContainer != null) {
                    mTabContainer.setTop(y);
                    mTabContainer.setBottom(mInitActionBarBottom + y);
                }
                mActionBarView.setTop(y);
                mActionBarView.setBottom(mInitActionBarBottom + y);

                if (mDragListener != null) {
                    mDragListener.onDragUpdate((float) y / mExtraViewContainerHeight, y);
                }
            }
        });

        mAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mTabContainer != null) {
                    mTabContainer.setClickable(true);
                }
                mActionBarView.setClickable(true);
                if (mDragListener != null) {
                    mDragListener.onDragUpdate(0f, 0);
                    mDragListener.onDragCloseEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (mTabContainer != null) {
                    mTabContainer.setClickable(true);
                }
                mActionBarView.setClickable(true);
                if (mDragListener != null) {
                    mDragListener.onDragUpdate(0f, 0);
                    mDragListener.onDragCloseEnd();
                }
            }
        });
    }

    public void setActivityContent(View view) {
        mActivityContent = view;
    }

    public View getActivityContent() {
        if (mActivityContent == null) {
            mActivityContent = ((AppCompatActivity) mContext).findViewById(com.cyee.internal.R.id.cyee_content);
        }
        return mActivityContent;
    }

    public void setExtraView(View view) {
        mExtraViewContainer.setExtraView(view);
    }

    public void setOnExtraViewDragListener(CyeeActionBar.OnExtraViewDragListener listener) {
        this.mDragListener = listener;
    }
}