package com.cyee.internal.widget;

//shaozj modify for Android L begin
//import com.android.internal.view.menu.ActionMenuPresenter;
//import com.android.internal.view.menu.ActionMenuView;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
//shaozj modify for Android L end

import com.cyee.internal.R;
import cyee.changecolors.IChangeColors;
import cyee.widget.CyeeWidgetResource;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

//public abstract class CyeeAbsActionBarView extends ViewGroup {
public abstract class CyeeAbsActionBarView extends ViewGroup implements IChangeColors {
	//protected ActionMenuView mMenuView;
	protected ViewGroup mMenuView;
	protected ActionMenuPresenter mActionMenuPresenter;
	protected CyeeActionBarContainer mSplitView;
    protected boolean mSplitActionBar = false;
	protected boolean mSplitWhenNarrow;
	protected int mContentHeight;

	protected Animator mVisibilityAnim;
	protected final VisibilityAnimListener mVisAnimListener = new VisibilityAnimListener();

	private static final TimeInterpolator sAlphaInterpolator = new DecelerateInterpolator();

	private static final int FADE_DURATION = 200;
    // Gionee <fenglp> <2013-07-29> add for CR00812456 begin
	protected boolean mIsActionMode = false;
	protected boolean mIsActionModeShowing = false;
    // Gionee <fenglp> <2013-07-29> add for CR00812456 end

	public CyeeAbsActionBarView(Context context) {
		super(context);
	}

	public CyeeAbsActionBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CyeeAbsActionBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

//		// Action bar can change size on configuration changes.
//		// Reread the desired height from the theme-specified style.
		TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.CyeeActionBar,
		        com.cyee.internal.R.attr.cyeeactionBarStyle, 0);
		int height = a.getLayoutDimension(R.styleable.CyeeActionBar_cyeeheight, 0);
		setContentHeight(height);
		a.recycle();
		if (mSplitWhenNarrow) {
			// Gionee zhangxx 2012-12-10 add for CR00715173 begin
//			if (isGioneeStyle()) {
				setSplitActionBar(true);
//			} else {
//				// Gionee zhangxx 2012-12-10 add for CR00715173 end
//				setSplitActionBar(getContext().getResources().getBoolean(
//						com.android.internal.R.bool.split_action_bar_is_narrow));
//				// Gionee zhangxx 2012-12-10 add for CR00715173 begin
//			}
			// Gionee zhangxx 2012-12-10 add for CR00715173 end
		}
		if (mActionMenuPresenter != null) {
			mActionMenuPresenter.onConfigurationChanged(newConfig);
		}
	}

//	// Gionee zhangxx 2012-12-10 add for CR00715173 begin
//	private boolean isGioneeStyle() {
//		TypedValue outValue = new TypedValue();
//
//		boolean rtn = getContext().getTheme().resolveAttribute(com.android.internal.R.attr.gioneeViewStyle,
//				outValue, true);
//		return outValue.data != 0;
//	}
//
//	// Gionee zhangxx 2012-12-10 add for CR00715173 end

	/**
	 * Sets whether the bar should be split right now, no questions asked.
	 * 
	 * @param split
	 *            true if the bar should split
	 */
	public void setSplitActionBar(boolean split) {
//		mSplitActionBar = split;
	}

	/**
	 * Sets whether the bar should split if we enter a narrow screen configuration.
	 * 
	 * @param splitWhenNarrow
	 *            true if the bar should check to split after a config change
	 */
	public void setSplitWhenNarrow(boolean splitWhenNarrow) {
		mSplitWhenNarrow = splitWhenNarrow;
	}

	public void setContentHeight(int height) {
		mContentHeight = height;
//		if (mMenuView != null) {
//			mMenuView.setMaxItemHeight(mContentHeight);
//		}
		requestLayout();
	}

	public int getContentHeight() {
		return mContentHeight;
	}

	public void setSplitView(CyeeActionBarContainer splitView) {
		mSplitView = splitView;
	}

	/**
	 * @return Current visibility or if animating, the visibility being animated to.
	 */
	public int getAnimatedVisibility() {
		if (mVisibilityAnim != null) {
			return mVisAnimListener.mFinalVisibility;
		}
		return getVisibility();
	}

	public void animateToVisibility(int visibility) {
		if (mVisibilityAnim != null) {
		    //shaozj begin
			//mVisibilityAnim.cancel();
		    mVisibilityAnim.end();
		    //shaozj end
		}
		if (visibility == VISIBLE) {
			/*shaozj if (getVisibility() != VISIBLE) {
				setAlpha(0);
				if (mSplitView != null && mMenuView != null) {
					mMenuView.setAlpha(0);
				}
			}*/
		    //shaozj begin
			//ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", 0, 1);
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "TranslationY",
                    -getContentHeight(), 0);
		    //shaozj end
			anim.setDuration(FADE_DURATION);
			anim.setInterpolator(sAlphaInterpolator);
			if (mSplitView != null && mMenuView != null) {
				AnimatorSet set = new AnimatorSet();
	            //shaozj begin
				//ObjectAnimator splitAnim = ObjectAnimator.ofFloat(mMenuView, "alpha", 0, 1);
                ObjectAnimator splitAnim = ObjectAnimator.ofFloat(mMenuView, "TranslationY",
                        -getContentHeight(), 0);
	            //shaozj end
				splitAnim.setDuration(FADE_DURATION);
				set.addListener(mVisAnimListener.withFinalVisibility(visibility));
				set.play(anim).with(splitAnim);
				set.start();
			} else {
				anim.addListener(mVisAnimListener.withFinalVisibility(visibility));
				anim.start();
			}
		} else {
            //shaozj begin
		    //ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", 1, 0);
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "TranslationY",
                    0, -getContentHeight());
            //shaozj end
			anim.setDuration(FADE_DURATION);
			anim.setInterpolator(sAlphaInterpolator);
			if (mSplitView != null && mMenuView != null) {
				AnimatorSet set = new AnimatorSet();
				//shaozj begin
				//ObjectAnimator splitAnim = ObjectAnimator.ofFloat(mMenuView, "alpha", 1, 0);
	            ObjectAnimator splitAnim = ObjectAnimator.ofFloat(mMenuView, "TranslationY",
	                    0, -getContentHeight());
				//shaozj end
				splitAnim.setDuration(FADE_DURATION);
				set.addListener(mVisAnimListener.withFinalVisibility(visibility));
				set.play(anim).with(splitAnim);
				set.start();
			} else {
				anim.addListener(mVisAnimListener.withFinalVisibility(visibility));
				anim.start();
			}
		}
	}

	@Override
	public void setVisibility(int visibility) {
		if (visibility != getVisibility()) {
			if (mVisibilityAnim != null) {
				mVisibilityAnim.end();
			}
			super.setVisibility(visibility);
		}
	}

	public boolean showOverflowMenu() {
		if (mActionMenuPresenter != null) {
			return mActionMenuPresenter.showOverflowMenu();
		}
		return false;
	}

	public void postShowOverflowMenu() {
		post(new Runnable() {
			public void run() {
				showOverflowMenu();
			}
		});
	}

	public boolean hideOverflowMenu() {
		if (mActionMenuPresenter != null) {
			return mActionMenuPresenter.hideOverflowMenu();
		}
		return false;
	}

	public boolean isOverflowMenuShowing() {
		if (mActionMenuPresenter != null) {
			return mActionMenuPresenter.isOverflowMenuShowing();
		}
		return false;
	}

	public boolean isOverflowReserved() {
		return mActionMenuPresenter != null && mActionMenuPresenter.isOverflowReserved();
	}

	public void dismissPopupMenus() {
		if (mActionMenuPresenter != null) {
			mActionMenuPresenter.dismissPopupMenus();
		}
	}

	protected int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
		child.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST), childSpecHeight);

		availableWidth -= child.getMeasuredWidth();
		availableWidth -= spacing;

		return Math.max(0, availableWidth);
	}

	static protected int next(int x, int val, boolean isRtl) {
		return isRtl ? x - val : x + val;
	}

	protected int positionChild(View child, int x, int y, int contentHeight, boolean reverse) {
		int childWidth = child.getMeasuredWidth();
		int childHeight = child.getMeasuredHeight();
		int childTop = y + (contentHeight - childHeight) / 2;

		if (reverse) {
			child.layout(x - childWidth, childTop, x, childTop + childHeight);
		} else {
			child.layout(x, childTop, x + childWidth, childTop + childHeight);
		}

		return (reverse ? -childWidth : childWidth);
	}

	protected class VisibilityAnimListener implements Animator.AnimatorListener {
		private boolean mCanceled = false;
		int mFinalVisibility;

		public VisibilityAnimListener withFinalVisibility(int visibility) {
			mFinalVisibility = visibility;
			return this;
		}

		@Override
		public void onAnimationStart(Animator animation) {
			setVisibility(VISIBLE);
			mVisibilityAnim = animation;
			mCanceled = false;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (mCanceled)
				return;

			mVisibilityAnim = null;
			setVisibility(mFinalVisibility);
			if (mSplitView != null && mMenuView != null) {
				mMenuView.setVisibility(mFinalVisibility);
			}
			
		    // Gionee <fenglp> <2013-07-29> modify for CR00812456 begin
			if (mIsActionMode) {
                mIsActionModeShowing = mFinalVisibility == VISIBLE;
            }
		    // Gionee <fenglp> <2013-07-29> modify for CR00812456 end
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			mCanceled = true;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}
	}
}
