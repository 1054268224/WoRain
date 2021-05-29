package com.cyee.internal.widget;

import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeWidgetResource;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.PorterDuff;
import com.android.internal.view.menu.MenuBuilder;

public class CyeeActionBarContextView extends CyeeAbsActionBarView implements AnimatorListener {
	private static final String TAG = "ActionBarContextView";

	private CharSequence mTitle;
	private CharSequence mSubtitle;

	private View mClose;
	private View mCustomView;
	private LinearLayout mTitleLayout;
	private TextView mTitleView;
	private TextView mSubtitleView;
	private final int mTitleStyleRes;
	private final int mSubtitleStyleRes;
	private Drawable mSplitBackground;
	private boolean mTitleOptional;

	private Animator mCurrentAnimation;
	private boolean mAnimateInOnLayout;
	private int mAnimationMode;

	private static final int ANIMATE_IDLE = 0;
	private static final int ANIMATE_IN = 1;
	private static final int ANIMATE_OUT = 2;
	private static final int CUSTOMVIEW_LMARGIN = 16; //dp
	private MenuBuilder mMenu;

	public CyeeActionBarContextView(Context context) {
		this(context, null);
	}

	public CyeeActionBarContextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.actionModeStyle);
	}

	public CyeeActionBarContextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CyeeActionBar, defStyle, 0);
//		setBackgroundDrawable(a.getDrawable(R.styleable.ActionMode_background));
		mTitleStyleRes = a.getResourceId(R.styleable.CyeeActionBar_cyeetitleTextStyle, 0);
		mSubtitleStyleRes = a.getResourceId(R.styleable.CyeeActionBar_cyeesubtitleTextStyle, 0);

		mContentHeight = a.getLayoutDimension(R.styleable.CyeeActionBar_cyeeheight, 0);

//		mSplitBackground = a.getDrawable(R.styleable.ActionMode_backgroundSplit);

		a.recycle();
		
		changeColors();
		
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mActionMenuPresenter != null) {
			mActionMenuPresenter.hideOverflowMenu();
			mActionMenuPresenter.hideSubMenus();
		}
	}

//	@Override
//	public void setSplitActionBar(boolean split) {
//		if (mSplitActionBar != split) {
//			if (mActionMenuPresenter != null) {
//				// Mode is already active; move everything over and adjust the menu itself.
//				final LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
//						LayoutParams.MATCH_PARENT);
//				if (!split) {
//					mMenuView = (ActionMenuView) mActionMenuPresenter.getMenuView(this);
//					mMenuView.setBackgroundDrawable(null);
//					final ViewGroup oldParent = (ViewGroup) mMenuView.getParent();
//					if (oldParent != null)
//						oldParent.removeView(mMenuView);
//					addView(mMenuView, layoutParams);
//				} else {
//					// Allow full screen width in split mode.
//					mActionMenuPresenter.setWidthLimit(
//							getContext().getResources().getDisplayMetrics().widthPixels, true);
//					// No limit to the item count; use whatever will fit.
//					mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
//					// Span the whole width
//					layoutParams.width = LayoutParams.MATCH_PARENT;
//					layoutParams.height = mContentHeight;
//					mMenuView = (ActionMenuView) mActionMenuPresenter.getMenuView(this);
//					mMenuView.setBackgroundDrawable(mSplitBackground);
//					final ViewGroup oldParent = (ViewGroup) mMenuView.getParent();
//					if (oldParent != null)
//						oldParent.removeView(mMenuView);
//					mSplitView.addView(mMenuView, layoutParams);
//				}
//			}
//			super.setSplitActionBar(split);
//		}
//	}

	public void setContentHeight(int height) {
		mContentHeight = height;
	}

	public void setCustomView(View view) {
		if (mCustomView != null) {
			removeView(mCustomView);
		}
		mCustomView = view;
		if (mTitleLayout != null) {
			removeView(mTitleLayout);
			mTitleLayout = null;
		}
		if (view != null) {
			addView(view);
		}
		requestLayout();
	}

	public void setTitle(CharSequence title) {
		mTitle = title;
		initTitle();
	}

	public void setSubtitle(CharSequence subtitle) {
		mSubtitle = subtitle;
		initTitle();
	}

	public CharSequence getTitle() {
		return mTitle;
	}

	public CharSequence getSubtitle() {
		return mSubtitle;
	}

	private void initTitle() {
		if (mTitleLayout == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			inflater.inflate(com.cyee.internal.R.layout.cyee_action_bar_title_item, this);
			mTitleLayout = (LinearLayout) getChildAt(getChildCount() - 1);
			mTitleView = (TextView) mTitleLayout.findViewById(com.cyee.internal.R.id.cyee_action_bar_title);
			mSubtitleView = (TextView) mTitleLayout.findViewById(com.cyee.internal.R.id.cyee_action_bar_subtitle);
			if (mTitleStyleRes != 0) {
				mTitleView.setTextAppearance(mContext, mTitleStyleRes);
			}
			if (mSubtitleStyleRes != 0) {
				mSubtitleView.setTextAppearance(mContext, mSubtitleStyleRes);
			}
		}

		mTitleView.setText(mTitle);
		mSubtitleView.setText(mSubtitle);

		final boolean hasTitle = !TextUtils.isEmpty(mTitle);
		final boolean hasSubtitle = !TextUtils.isEmpty(mSubtitle);
		mSubtitleView.setVisibility(hasSubtitle ? VISIBLE : GONE);
		mTitleLayout.setVisibility(hasTitle || hasSubtitle ? VISIBLE : GONE);
		if (mTitleLayout.getParent() == null) {
			addView(mTitleLayout);
		}
	}

    public void initForMode(final ActionMode mode) {
        if (mClose == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            mClose = inflater.inflate(com.cyee.internal.R.layout.cyee_action_mode_close_item, this, false);
            addView(mClose);
        } else if (mClose.getParent() == null) {
            addView(mClose);
        }
        if(ChameleonColorManager.getInstance().getCyeeThemeType(getContext()) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Drawable bgDrawable = mContext.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_home_as_up);
            ((ImageView)mClose).setImageDrawable(bgDrawable);
        } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            Drawable backgroundDrawable = mClose.getBackground();
            if (backgroundDrawable != null
                    && backgroundDrawable instanceof RippleDrawable) {
                ((RippleDrawable) backgroundDrawable).setColor(ColorStateList
                        .valueOf(ChameleonColorManager
                                .getContentColorThirdlyOnBackgroud_C3()));
            }
        }

        mClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mode.finish();
            }
        });

        mMenu = (MenuBuilder) mode.getMenu();
        // final MenuBuilder menu = (MenuBuilder) mode.getMenu();
        // if (mActionMenuPresenter != null) {
        // mActionMenuPresenter.dismissPopupMenus();
        // }
        // mActionMenuPresenter = new ActionMenuPresenter(mContext);
        // mActionMenuPresenter.setReserveOverflow(true);
        //
        // final LayoutParams layoutParams = new
        // LayoutParams(LayoutParams.WRAP_CONTENT,
        // LayoutParams.MATCH_PARENT);
        // if (!mSplitActionBar) {
        // menu.addMenuPresenter(mActionMenuPresenter);
        // mMenuView = (ActionMenuView) mActionMenuPresenter.getMenuView(this);
        // mMenuView.setBackgroundDrawable(null);
        // addView(mMenuView, layoutParams);
        // } else {
        // // Allow full screen width in split mode.
        // mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels,
        // true);
        // // No limit to the item count; use whatever will fit.
        // mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
        // // Span the whole width
        // layoutParams.width = LayoutParams.MATCH_PARENT;
        // layoutParams.height = mContentHeight;
        // menu.addMenuPresenter(mActionMenuPresenter);
        // mMenuView = (ActionMenuView) mActionMenuPresenter.getMenuView(this);
        // mMenuView.setBackgroundDrawable(mSplitBackground);
        // mSplitView.addView(mMenuView, layoutParams);
        // }

        mAnimateInOnLayout = true;
        // Gionee <fenglp> <2013-07-29> add for CR00812456 end
        mIsActionMode = true;
        // Gionee <fenglp> <2013-07-29> add for CR00812456 end
    }

	public void closeMode() {
		if (mAnimationMode == ANIMATE_OUT) {
			// Called again during close; just finish what we were doing.
			return;
		}
		if (mClose == null) {
			killMode();
			return;
		}

		finishAnimation();
		mAnimationMode = ANIMATE_OUT;
		mCurrentAnimation = makeOutAnimation();
		mCurrentAnimation.start();
	}

	private void finishAnimation() {
		final Animator a = mCurrentAnimation;
		if (a != null) {
			mCurrentAnimation = null;
			a.end();
		}
	}

	public void killMode() {
		finishAnimation();
		removeAllViews();
		if (mSplitView != null) {
			mSplitView.removeView(mMenuView);
		}
		mCustomView = null;
		mMenuView = null;
		mAnimateInOnLayout = false;
	}

	@Override
	public boolean showOverflowMenu() {
		if (mActionMenuPresenter != null) {
			return mActionMenuPresenter.showOverflowMenu();
		}
		return false;
	}

	@Override
	public boolean hideOverflowMenu() {
		if (mActionMenuPresenter != null) {
			return mActionMenuPresenter.hideOverflowMenu();
		}
		return false;
	}

	@Override
	public boolean isOverflowMenuShowing() {
		if (mActionMenuPresenter != null) {
			return mActionMenuPresenter.isOverflowMenuShowing();
		}
		return false;
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		// Used by custom views if they don't supply layout params. Everything else
		// added to an ActionBarContextView should have them already.
		return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(getClass().getSimpleName() + " can only be used "
					+ "with android:layout_width=\"match_parent\" (or fill_parent)");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode == MeasureSpec.UNSPECIFIED) {
			throw new IllegalStateException(getClass().getSimpleName() + " can only be used "
					+ "with android:layout_height=\"wrap_content\"");
		}

		final int contentWidth = MeasureSpec.getSize(widthMeasureSpec);

		int maxHeight = mContentHeight > 0 ? mContentHeight : MeasureSpec.getSize(heightMeasureSpec);

		final int verticalPadding = getPaddingTop() + getPaddingBottom();
		int availableWidth = contentWidth - getPaddingLeft() - getPaddingRight();
		final int height = maxHeight - verticalPadding;
		final int childSpecHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);

		if (mClose != null) {
			availableWidth = measureChildView(mClose, availableWidth, childSpecHeight, 0);
			MarginLayoutParams lp = (MarginLayoutParams) mClose.getLayoutParams();
			availableWidth -= lp.leftMargin + lp.rightMargin;
		}

		if (mMenuView != null && mMenuView.getParent() == this) {
			availableWidth = measureChildView(mMenuView, availableWidth, childSpecHeight, 0);
		}

		if (mTitleLayout != null && mCustomView == null) {
			if (mTitleOptional) {
				final int titleWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
				mTitleLayout.measure(titleWidthSpec, childSpecHeight);
				final int titleWidth = mTitleLayout.getMeasuredWidth();
				final boolean titleFits = titleWidth <= availableWidth;
				if (titleFits) {
					availableWidth -= titleWidth;
				}
				mTitleLayout.setVisibility(titleFits ? VISIBLE : GONE);
			} else {
				availableWidth = measureChildView(mTitleLayout, availableWidth, childSpecHeight, 0);
			}
		}

		if (mCustomView != null) {
			ViewGroup.LayoutParams lp = mCustomView.getLayoutParams();
			final int customWidthMode = lp.width != LayoutParams.WRAP_CONTENT ? MeasureSpec.EXACTLY
					: MeasureSpec.AT_MOST;
			availableWidth -= dip2px(getContext(),CUSTOMVIEW_LMARGIN);
			final int customWidth = lp.width >= 0 ? Math.min(lp.width, availableWidth) : availableWidth;
			final int customHeightMode = lp.height != LayoutParams.WRAP_CONTENT ? MeasureSpec.EXACTLY
					: MeasureSpec.AT_MOST;
			final int customHeight = lp.height >= 0 ? Math.min(lp.height, height) : height;
			mCustomView.measure(MeasureSpec.makeMeasureSpec(customWidth, customWidthMode),
					MeasureSpec.makeMeasureSpec(customHeight, customHeightMode));
		}

		if (mContentHeight <= 0) {
			int measuredHeight = 0;
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				View v = getChildAt(i);
				int paddedViewHeight = v.getMeasuredHeight() + verticalPadding;
				if (paddedViewHeight > measuredHeight) {
					measuredHeight = paddedViewHeight;
				}
			}
			setMeasuredDimension(contentWidth, measuredHeight);
		} else {
			setMeasuredDimension(contentWidth, maxHeight);
		}
	}

	private Animator makeInAnimation() {
		mClose.setTranslationX(-mClose.getWidth()
				- ((MarginLayoutParams) mClose.getLayoutParams()).leftMargin);
		ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(mClose, "translationX", 0);
		buttonAnimator.setDuration(200);
		buttonAnimator.addListener(this);
		buttonAnimator.setInterpolator(new DecelerateInterpolator());

		AnimatorSet set = new AnimatorSet();
		AnimatorSet.Builder b = set.play(buttonAnimator);

		if (mMenuView != null) {
			final int count = mMenuView.getChildCount();
			if (count > 0) {
				for (int i = count - 1, j = 0; i >= 0; i--, j++) {
					View child = mMenuView.getChildAt(i);
					child.setScaleY(0);
					ObjectAnimator a = ObjectAnimator.ofFloat(child, "scaleY", 0, 1);
					a.setDuration(300);
					b.with(a);
				}
			}
		}

		return set;
	}

	private Animator makeOutAnimation() {
		ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(mClose, "translationX", -mClose.getWidth()
				- ((MarginLayoutParams) mClose.getLayoutParams()).leftMargin);
		buttonAnimator.setDuration(200);
		buttonAnimator.addListener(this);
		buttonAnimator.setInterpolator(new DecelerateInterpolator());

		AnimatorSet set = new AnimatorSet();
		AnimatorSet.Builder b = set.play(buttonAnimator);

		if (mMenuView != null) {
			final int count = mMenuView.getChildCount();
			if (count > 0) {
				for (int i = 0; i < 0; i++) {
					View child = mMenuView.getChildAt(i);
					child.setScaleY(0);
					ObjectAnimator a = ObjectAnimator.ofFloat(child, "scaleY", 0);
					a.setDuration(300);
					b.with(a);
				}
			}
		}

		return set;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final boolean isLayoutRtl = isLayoutRtl();
		int x = isLayoutRtl ? r - l - getPaddingRight() : getPaddingLeft();
		final int y = getPaddingTop();
		final int contentHeight = b - t - getPaddingTop() - getPaddingBottom();

		if (mClose != null && mClose.getVisibility() != GONE) {
			MarginLayoutParams lp = (MarginLayoutParams) mClose.getLayoutParams();
			final int startMargin = (isLayoutRtl ? lp.rightMargin : lp.leftMargin);
			final int endMargin = (isLayoutRtl ? lp.leftMargin : lp.rightMargin);
			x = next(x, startMargin, isLayoutRtl);
			x += positionChild(mClose, x, y, contentHeight, isLayoutRtl);
			x = next(x, endMargin, isLayoutRtl);

			if (mAnimateInOnLayout) {
				mAnimationMode = ANIMATE_IN;
				mCurrentAnimation = makeInAnimation();
				mCurrentAnimation.start();
				mAnimateInOnLayout = false;
			}
		}

		if (mTitleLayout != null && mCustomView == null && mTitleLayout.getVisibility() != GONE) {
			x += positionChild(mTitleLayout, x, y, contentHeight, isLayoutRtl);
		}

        if (mCustomView != null) {
            x += dip2px(getContext(), CUSTOMVIEW_LMARGIN);
            x += positionChild(mCustomView, x, y, contentHeight, isLayoutRtl);
        }

		x = isLayoutRtl ? getPaddingLeft() : r - l - getPaddingRight();

		if (mMenuView != null) {
			x += positionChild(mMenuView, x, y, contentHeight, !isLayoutRtl);
		}
	}

	@Override
	public void onAnimationStart(Animator animation) {
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		if (mAnimationMode == ANIMATE_OUT) {
			killMode();
		}
		mAnimationMode = ANIMATE_IDLE;
	}

	@Override
	public void onAnimationCancel(Animator animation) {
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
	}

	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}

	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			// Action mode started
			event.setSource(this);
			event.setClassName(getClass().getName());
			event.setPackageName(getContext().getPackageName());
			event.setContentDescription(mTitle);
		} else {
			super.onInitializeAccessibilityEvent(event);
		}
	}

	public void setTitleOptional(boolean titleOptional) {
		if (titleOptional != mTitleOptional) {
			requestLayout();
		}
		mTitleOptional = titleOptional;
	}

	public boolean isTitleOptional() {
		return mTitleOptional;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		super.dispatchTouchEvent(event);
		// M: consume touch event to prevent from dispatching it to parent or siblings
		return true;
	}
	
	public Menu getMenu(){
	    return mMenu;
	}
	
    // Gionee <fenglp> <2013-07-29> add for CR00812456 begin	
	public boolean isActionModeShowing(){
        return mIsActionModeShowing;
    }
    // Gionee <fenglp> <2013-07-29> add for CR00812456 end

    @Override
    public void changeColors() {
        if(ChameleonColorManager.getInstance().getCyeeThemeType(getContext()) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Drawable bgDrawable = mContext.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_bg);
            setBackground(bgDrawable);
        } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
        }
    }
    
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
