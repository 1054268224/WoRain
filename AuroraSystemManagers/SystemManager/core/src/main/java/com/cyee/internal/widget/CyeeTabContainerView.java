package com.cyee.internal.widget;

import cyee.app.CyeeActionBar;
import cyee.changecolors.ChameleonColorManager;
import cyee.changecolors.IChangeColors;
import cyee.theme.global.GlobalThemeConfigConstants;
import cyee.theme.global.ICyeeResource;
import cyee.widget.CyeeWidgetResource;
import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import com.cyee.utils.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.MeasureSpec;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyee.internal.view.CyeeActionBarPolicy;

public class CyeeTabContainerView extends RelativeLayout implements IChangeColors {
    private static final int INDICATORVIEW_MARGIN_BOTTOM = 3;
    private static final int INDICATORVIEW_MARGIN_LEFT = 6;
    private final Context mContext;
	private TabClickListener mTabClickListener;
	private TabTouchListener mTabTouchListener;
	private final CyeeTabIndicator mTabLayout;
	private int mMaxTabWidth;
	private int mContentHeight;
	private int mStackedTabMaxWidth;
	//shaozj private int mSelectedTabIndex;
	//shaozj Runnable mTabSelector;
	private boolean mAllowCollapse;

//	private boolean mIsGioneeWidget3 = false;// for gionee widget3.0
	private final int ANIM_DURATION = 200;// animation duration time
	// Gionee <lihq> <2013-12-19> modify for CR00873172 begin
	private final int INDICATOR_DEFAULT_HEIGHT = 2;
	// Gionee <lihq> <2013-12-19> modify for CR00873172 end
	private final int INDICATOR_DEFAULT_WIDTH = 10;
	private RelativeLayout mContentLayout;
	//shaozj private View mIndicatorView;
    // Gionee <gaoj> <2013-11-22> modify for CR00956115 begin
    private final boolean mToLeft = false;
    private final int mCurrentIndex = 0;
    private final boolean mToRight = false;
    // Gionee <gaoj> <2013-11-22> modify for CR00956115 end
	private int mIndicatorTop;
	private int mIndicatroWidth;
	private int mAnimatingHeightOffset;// the indicator height changes at animating
	private float oriX;
	private boolean mClickable = true;

	// Gionee <lihq> <2014-4-23> add for CR00873172 begin
	private final int mIndicatorMarginLeft;
    // Gionee <lihq> <2014-4-23> add for CR00873172 end
	
	private boolean mActionBarOverlay = false;
    private int mPrevSelected = -1;
	
	public CyeeTabContainerView(Context context) {
		super(context);
		mContext = context;
		CyeeActionBarPolicy abp = CyeeActionBarPolicy.get(context);
		setContentHeight(abp.getTabContainerHeight());
		mTabLayout = createTabLayout();
		//shaozj mIndicatorView = createIndicatorView();
		addView(mTabLayout);
		//shaozj addView(mIndicatorView);
		//shaozj bringChildToFront(mIndicatorView);
		// Gionee <lihq> <2014-4-23> add for CR00873172 begin
		mIndicatorMarginLeft = (int)(INDICATORVIEW_MARGIN_LEFT * context.getResources().getDisplayMetrics().density);
        // Gionee <lihq> <2014-4-23> add for CR00873172 end
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		//shaozj final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
		final int childCount = mTabLayout.getChildCount();
		if (childCount > 1 && (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST)) {
			if (childCount > 2) {
				mMaxTabWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
			} else {
				mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
			}
			mMaxTabWidth = Math.min(mMaxTabWidth, mStackedTabMaxWidth);
		} else {
			mMaxTabWidth = -1;
		}
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(mContentHeight, MeasureSpec.EXACTLY);
		
		//shaozj final boolean canCollapse = !lockedExpanded && mAllowCollapse;
		mTabLayout.measure(MeasureSpec.UNSPECIFIED, heightMeasureSpec);
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		CyeeActionBarPolicy abp = CyeeActionBarPolicy.get(mContext);
		// Action bar can change size on configuration changes.
		// Reread the desired height from the theme-specified style.
		mStackedTabMaxWidth = abp.getStackedTabMaxWidth();
		setContentHeight(abp.getTabContainerHeight());
	}

    public void setTabSelected(int position) {
        // shaozj modify begin
        // mSelectedTabIndex = position;

        // position = getRtlPosition(position);
        Log.v("CyeetabContainerView", "setTabSelected position=" + position
                + " mPrevSelected=" + mPrevSelected);
        final View selectedChild = mTabLayout.getChildAt(position);
        if(null != selectedChild) {
            View prevChild = mTabLayout.getChildAt(mPrevSelected);
            if (null != prevChild) {
                prevChild.setSelected(false);
            }
            selectedChild.setSelected(true);
            mPrevSelected = position;
        }
        // shaozj modify end
    }

	public void setContentHeight(int contentHeight) {
		mContentHeight = contentHeight;
		requestLayout();
	}

	public void addTab(CyeeActionBar.Tab tab, boolean setSelected) {
		TabView tabView = createTabView(tab, false);
		mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
        // Default to the first child being selected
        if (mPrevSelected < 0) {
            mPrevSelected = 0;
        }
		if (setSelected) {
			tabView.setSelected(true);
		}
	}

	public void addTab(CyeeActionBar.Tab tab, int position, boolean setSelected) {
		final TabView tabView = createTabView(tab, false);
		mTabLayout.addView(tabView, position, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
//        if (mTabSpinner != null) {
//            ((TabAdapter) mTabSpinner.getAdapter()).notifyDataSetChanged();
//        }
		if (setSelected) {
			tabView.setSelected(true);
		}
        if (mAllowCollapse) {
            requestLayout();
        }
	}

	public void updateTab(int position) {
		((TabView) mTabLayout.getChildAt(position)).update();
//        if (mTabSpinner != null) {
//            ((TabAdapter) mTabSpinner.getAdapter()).notifyDataSetChanged();
//        }
        if (mAllowCollapse) {
            requestLayout();
        }
	}

	private TabView createTabView(CyeeActionBar.Tab tab, boolean forAdapter) {
		final TabView tabView = new TabView(getContext(), tab, forAdapter);
		if (forAdapter) {
			tabView.setBackgroundDrawable(null);
			tabView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,
					mContentHeight));
		} else {
			tabView.setFocusable(true);

			if (mTabClickListener == null) {
				mTabClickListener = new TabClickListener();
			}
			tabView.setOnClickListener(mTabClickListener);

			if (mTabTouchListener == null) {
				mTabTouchListener = new TabTouchListener();
			}
			tabView.setOnTouchListener(mTabTouchListener);
		}
		return tabView;
	}

	private CyeeTabIndicator createTabLayout() {
		final CyeeTabIndicator tabLayout = new CyeeTabIndicator(getContext(), null,
				android.R.attr.actionBarTabBarStyle);
		tabLayout.setMeasureWithLargestChildEnabled(true);
		tabLayout.setGravity(Gravity.CENTER);
		tabLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		return tabLayout;
	}

	private class TabView extends LinearLayout implements OnLongClickListener, IChangeColors {
		private CyeeActionBar.Tab mTab;
		private TextView mTextView;
		private ImageView mIconView;
		private View mCustomView;

		public TabView(Context context, CyeeActionBar.Tab tab, boolean forList) {
			super(context, null, android.R.attr.actionBarTabStyle);
			mTab = tab;

			if (forList) {
				setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
			} else {
				setGravity(Gravity.CENTER);
			}

			update();
			
			changeColors();
		}

		public void bindTab(CyeeActionBar.Tab tab) {
			mTab = tab;
			update();
		}

		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// Re-measure if we went beyond our maximum size.
			//shaozj final int width = getMeasuredWidth();
			if (mMaxTabWidth > 0 && getMeasuredWidth() > mMaxTabWidth) {
//				super.onMeasure(MeasureSpec.makeMeasureSpec(mMaxTabWidth, MeasureSpec.EXACTLY),
//						heightMeasureSpec);
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxTabWidth, MeasureSpec.EXACTLY);
			}

			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		public void update() {
			final CyeeActionBar.Tab tab = mTab;
			final View custom = tab.getCustomView();
			if (custom != null) {
				final ViewParent customParent = custom.getParent();
				if (customParent != this) {
					if (customParent != null)
						((ViewGroup) customParent).removeView(custom);
					addView(custom);
				}
				mCustomView = custom;
				if (mTextView != null)
					mTextView.setVisibility(GONE);
				if (mIconView != null) {
					mIconView.setVisibility(GONE);
					mIconView.setImageDrawable(null);
				}
			} else {
				if (mCustomView != null) {
					removeView(mCustomView);
					mCustomView = null;
				}

				final Drawable icon = tab.getIcon();
				final CharSequence text = tab.getText();

				if (icon != null) {
					if (mIconView == null) {
						ImageView iconView = new ImageView(getContext());
						LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT);
						lp.gravity = Gravity.CENTER_VERTICAL;
						iconView.setLayoutParams(lp);
						addView(iconView, 0);
						mIconView = iconView;
					}
					mIconView.setImageDrawable(icon);
					mIconView.setVisibility(VISIBLE);
				} else if (mIconView != null) {
					mIconView.setVisibility(GONE);
					mIconView.setImageDrawable(null);
				}

				final boolean hasText = !TextUtils.isEmpty(text);
				if (hasText) {
					if (mTextView == null) {
						TextView textView = new TextView(getContext(), null,
								android.R.attr.actionBarTabTextStyle);
						textView.setEllipsize(TruncateAt.END);
						LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT);
						lp.gravity = Gravity.CENTER_VERTICAL;
						textView.setLayoutParams(lp);
                        ColorStateList textColors = mTab.getTextColor();
                        if (textColors != null) {
                            textView.setTextColor(textColors);
                        }
						addView(textView);
						mTextView = textView;
					}
					mTextView.setText(text);
					mTextView.setVisibility(VISIBLE);
				} else if (mTextView != null) {
					mTextView.setVisibility(GONE);
					mTextView.setText(null);
				}

				if (mIconView != null) {
					mIconView.setContentDescription(tab.getContentDescription());
				}

				if (!hasText && !TextUtils.isEmpty(tab.getContentDescription())) {
					setOnLongClickListener(this);
				} else {
					setOnLongClickListener(null);
					setLongClickable(false);
				}
			}
		}

		public boolean onLongClick(View v) {
			final int[] screenPos = new int[2];
			getLocationOnScreen(screenPos);

			final Context context = getContext();
			final int width = getWidth();
			final int height = getHeight();
			final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

			Toast cheatSheet = Toast.makeText(context, mTab.getContentDescription(), Toast.LENGTH_SHORT);
			// Show under the tab
			cheatSheet.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, (screenPos[0] + width / 2)
					- screenWidth / 2, height);

			cheatSheet.show();
			return true;
		}

		public CyeeActionBar.Tab getTab() {
			return mTab;
		}

        @Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
            if (!mActionBarOverlay) {
                if (ChameleonColorManager.isNeedChangeColor(mContext)
                        && mTextView != null) {
                    if (selected) {
                        mTextView.setTextColor(ChameleonColorManager
                                .getContentColorPrimaryOnAppbar_T1());
                    } else {
                        mTextView.setTextColor(ChameleonColorManager
                                .getContentColorThirdlyOnAppbar_T3());
                    }
                }
            }
	}

        @Override
        public void changeColors() {
            // mTextView.setTextColor(ChameleonColorManager
            // .getContentColorPrimaryOnAppbar_T1());
            if (ChameleonColorManager.isNeedChangeColor(mContext) && !mActionBarOverlay) {
                Drawable background = getBackground();
                if(null != background){
                    background.setColorFilter(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1(), PorterDuff.Mode.SRC_IN);
                }
               
                if (mTextView != null) {
                    mTextView.setTextColor(ChameleonColorManager.getContentColorThirdlyOnAppbar_T3());
                }
            }
        }
    }

	private class TabClickListener implements OnClickListener {
		public void onClick(View view) {
			TabView tabView = (TabView) view;
			tabView.getTab().select();
			final int tabCount = mTabLayout.getChildCount();
			for (int i = 0; i < tabCount; i++) {
				final View child = mTabLayout.getChildAt(i);
				child.setSelected(child == view);
			}
		}
	}

	private class TabTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
					if (!mClickable) {
						return true;
					}
					break;

				default:
					break;
			}
			if (mClickable) {
			}
			return false;
		}
	}

	@Override
	public void setClickable(boolean clickable) {
		mClickable = clickable;
	}
    // Gionee <gaoj> <2013-11-22> add for CR00956115 end
    
    public void onPageScrolled(int position, final float percent, int offset) {
        // shaozj mIndicatorView.setTop(mIndicatorTop + mAnimatingHeightOffset);
        // shaozj setIndicatorPosition(position, percent);
        int tabLayoutChildCount = mTabLayout.getChildCount();
        if ((tabLayoutChildCount == 0) || (position < 0)
                || (position >= tabLayoutChildCount)) {
            return;
        }
        mTabLayout.onPageScrolled(position, percent, offset);
    }

	/**
	 * @param color
	 *            Color.parseColor("#ff0000")
	 */
	public void setIndicatorBackgroundColor(int color) {
		/*shaozj if (mIndicatorView != null) {
			mIndicatorView.setBackgroundColor(color);
		}*/
	    if(null != mTabLayout) {
	        mTabLayout.setIndicatorBackgroundColor(color);
	    }
	}

	public void removeTabAt(int position) {
		mTabLayout.removeViewAt(position);
//        if (mTabSpinner != null) {
//            ((TabAdapter) mTabSpinner.getAdapter()).notifyDataSetChanged();
//        }
        if (mAllowCollapse) {
            requestLayout();
        }
	}

	public void removeAllTabs() {
		mTabLayout.removeAllViews();
//        if (mTabSpinner != null) {
//            ((TabAdapter) mTabSpinner.getAdapter()).notifyDataSetChanged();
//        }
        if (mAllowCollapse) {
            requestLayout();
        }
	}
	
	protected Animator mVisibilityAnim;
	private static final int FADE_DURATION = 200;
	private static final TimeInterpolator sAlphaInterpolator = new DecelerateInterpolator();
	protected final VisibilityAnimListener mVisAnimListener = new VisibilityAnimListener();
    public void animateToVisibility(int visibility) {
        if (mVisibilityAnim != null) {
            mVisibilityAnim.cancel();
        }
        if (visibility == VISIBLE) {
            if (getVisibility() != VISIBLE) {
                setAlpha(0);
            }
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", 1);
            anim.setDuration(FADE_DURATION);
            anim.setInterpolator(sAlphaInterpolator);

            anim.addListener(mVisAnimListener.withFinalVisibility(visibility));
            anim.start();
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", 0);
            anim.setDuration(FADE_DURATION);
            anim.setInterpolator(sAlphaInterpolator);

            anim.addListener(mVisAnimListener.withFinalVisibility(visibility));
            anim.start();
        }
    }
    protected class VisibilityAnimListener implements Animator.AnimatorListener {
        private boolean mCanceled = false;
        private int mFinalVisibility;

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
            if (mCanceled) return;

            mVisibilityAnim = null;
            setVisibility(mFinalVisibility);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mCanceled = true;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
    
    public void setAllowCollapse(boolean allowCollapse) {
        mAllowCollapse = allowCollapse;
    }

    @Override
    public void changeColors() {
        int primaryBackgroundColor = ChameleonColorManager
                .getAppbarColor_A1();
//        mTabLayout.setBackgroundColor(primaryBackgroundColor);
//        setBackgroundColor(primaryBackgroundColor);
    }
    
    public boolean isActionBarOverlay() {
        return mActionBarOverlay;
    }
    
    public void setActionBarOverlay(boolean overlay) {
    	mActionBarOverlay = overlay;
    }
}
