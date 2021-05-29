package com.cyee.internal.widget;


import android.animation.LayoutTransition;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.CollapsibleActionView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import com.android.internal.view.menu.ActionMenuItem;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.SubMenuBuilder;
import com.cyee.internal.R;
import com.cyee.utils.Log;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActionBar.OnNavigationListener;
import cyee.app.CyeeActivity;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeTextView;

public class CyeeActionBarView extends CyeeAbsActionBarView {
	private static final String TAG = "ActionBarView";
	public static final int DISPLAY_DEFAULT = 0;
	private static final int DISPLAY_RELAYOUT_MASK = CyeeActionBar.DISPLAY_SHOW_HOME | CyeeActionBar.DISPLAY_USE_LOGO
			| CyeeActionBar.DISPLAY_HOME_AS_UP | CyeeActionBar.DISPLAY_SHOW_CUSTOM | CyeeActionBar.DISPLAY_SHOW_TITLE;
	private int mNavigationMode;
	private CyeeTabContainerView mTabContainer;
	private boolean mIncludeTabs;
	private int mDisplayOptions = -1;
	private View mCustomNavView;
	private static final int DEFAULT_CUSTOM_GRAVITY = Gravity.START | Gravity.CENTER_VERTICAL;
	private boolean mUserTitle;
	private CharSequence mTitle;
	private CharSequence mSubtitle;
	private Drawable mIcon;
    private Drawable mLogo;
    private final HomeView mHomeLayout;
    private final HomeView mExpandedHomeLayout;
	private CyeeTextView mTitleView;
	private CyeeTextView mSubtitleView;
	private LinearLayout mTitleLayout;
	private View mTitleUpView;
	private final int mTitleStyleRes;
	private final int mSubtitleStyleRes;
	private final int mProgressStyle;
	private final int mIndeterminateProgressStyle;
	private final boolean mClickable = true;
	private CyeeActionBarContextView mContextView;
	private boolean mIsCollapsed;
	private Spinner mSpinner;
	private LinearLayout mListNavLayout;
	private OnNavigationListener mCallback;
	private ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
	View mExpandedActionView;
	Window.Callback mWindowCallback;
	private final int mHomeResId;
	private SpinnerAdapter mSpinnerAdapter;
	private final ActionMenuItem mLogoNavItem;
	private final int mProgressBarPadding;
	private MenuBuilder mOptionsMenu;
	private ProgressBar mIndeterminateProgressView;
	private ProgressBar mProgressView;
	private Runnable mTabSelector;
	private final Rect mTempRect = new Rect();
	private final int mItemPadding;
	private final int mMaxHomeSlop;
	private static final int MAX_HOME_SLOP = 32; // dp
	private boolean mIsCollapsable;
	private CyeeActivity mActivity;
    private View mEmptyView;
    private OnClickListener mBackClickListener;
    private static final int CLICK_INTERVAL_TIME = 500;
    private OnClickListener mActionBarDoubleClickListener;
    private long mCurTime = 0;
    
	private final AdapterView.OnItemSelectedListener mNavItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected(AdapterView parent, View view, int position, long id) {
			if (mCallback != null) {
				mCallback.onNavigationItemSelected(position, id);
			}
		}

		public void onNothingSelected(AdapterView parent) {
			// Do nothing
		}
	};

    private final OnClickListener mExpandedActionViewUpListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final MenuItemImpl item = mExpandedMenuPresenter.mCurrentExpandedItem;
            if (item != null) {
                MenuItemCompat.collapseActionView(item);
            }
        }
    };

    private final OnClickListener mUpClickListener = new OnClickListener() {
        public void onClick(View v) {
            if ((mDisplayOptions & CyeeActionBar.DISPLAY_HOME_AS_UP) != 0) {
				// Gionee <fenglp> <2013-07-30> modify for CR00812456 begin
                if (mBackClickListener != null) {
                    mBackClickListener.onClick(v);
                } else {
                    if (mActivity != null) {
                        mActivity.finish();
                    }
                }
				// Gionee <fenglp> <2013-07-30> modify for CR00812456 end				
                return;
            }
			if (mWindowCallback != null) {
				mWindowCallback.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, mLogoNavItem);
			}
        }
    };
    
	public CyeeActionBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
        // Background is always provided by the container.
        setBackgroundResource(0);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CyeeActionBar);

        ApplicationInfo appInfo = context.getApplicationInfo();
        PackageManager pm = context.getPackageManager();
        
        mNavigationMode = a.getInt(R.styleable.CyeeActionBar_cyeenavigationMode,
                CyeeActionBar.NAVIGATION_MODE_STANDARD);
		mTitle = a.getText(R.styleable.CyeeActionBar_cyeetitle);
        mSubtitle = a.getText(R.styleable.CyeeActionBar_cyeesubtitle);
        
        if (mTitle == null) {
			if (context instanceof AppCompatActivity) {
				try {
					mTitle = ((AppCompatActivity) context).getTitle();
				} catch (Exception e) {
					Log.e(TAG, "Activity title name not found!", e);
				}
			}
			if (mLogo == null) {
				mLogo = appInfo.loadLogo(pm);
			}
		}
        
        mLogo = a.getDrawable(R.styleable.CyeeActionBar_cyeelogo);
        if (mLogo == null) {
            if (context instanceof AppCompatActivity) {
                try {
                    mLogo = pm.getActivityLogo(((AppCompatActivity) context).getComponentName());
                } catch (NameNotFoundException e) {
                    Log.e(TAG, "Activity component name not found!", e);
                }
            }
            if (mLogo == null) {
                mLogo = appInfo.loadLogo(pm);
            }
        }

        // Gionee <lihq> <2014-6-10> delete for CR00873172 begin
        /*
        mIcon = a.getDrawable(com.cyee.widgetdemol.R.styleable.CyeeActionBar_cyeeicon);
        if (mIcon == null) {
            if (context instanceof Activity) {
                try {
                    mIcon = pm.getActivityIcon(((Activity) context).getComponentName());
                } catch (NameNotFoundException e) {
                    Log.e(TAG, "Activity component name not found!", e);
                }
            }
            if (mIcon == null) {
                mIcon = appInfo.loadIcon(pm);
            }
        }
        */
        // Gionee <lihq> <2014-6-10> delete for CR00873172 end
        final LayoutInflater inflater = LayoutInflater.from(context);

        mHomeResId = a.getResourceId(
                R.styleable.CyeeActionBar_cyeehomeLayout,
                com.cyee.internal.R.layout.cyee_action_bar_home);

        mHomeLayout = (HomeView) inflater.inflate(mHomeResId, this, false);

        mExpandedHomeLayout = (HomeView) inflater.inflate(mHomeResId, this, false);
        mExpandedHomeLayout.setUp(true);
        mExpandedHomeLayout.setOnClickListener(mExpandedActionViewUpListener);
        mExpandedHomeLayout.setContentDescription(getResources().getText(
                com.cyee.internal.R.string.cyee_action_bar_up_description));
		mTitleStyleRes = a
				.getResourceId(R.styleable.CyeeActionBar_cyeetitleTextStyle, 0);
		mSubtitleStyleRes = a.getResourceId(
				R.styleable.CyeeActionBar_cyeesubtitleTextStyle, 0);

		mProgressStyle = a.getResourceId(R.styleable.CyeeActionBar_cyeeprogressBarStyle, 0);
        mIndeterminateProgressStyle = a.getResourceId(
        		R.styleable.CyeeActionBar_cyeeindeterminateProgressStyle, 0);

        mProgressBarPadding = a.getDimensionPixelOffset(R.styleable.CyeeActionBar_cyeeprogressBarPadding, 0);
        mItemPadding = a.getDimensionPixelOffset(R.styleable.CyeeActionBar_cyeeitemPadding, 0);

		setDisplayOptions(a.getInt(R.styleable.CyeeActionBar_cyeedisplayOptions,
		DISPLAY_DEFAULT));

        final int customNavId = a.getResourceId(R.styleable.CyeeActionBar_cyeecustomNavigationLayout, 0);
        if (customNavId != 0) {
            mCustomNavView = (View) inflater.inflate(customNavId, this, false);
            mNavigationMode = CyeeActionBar.NAVIGATION_MODE_STANDARD;
            setDisplayOptions(mDisplayOptions | CyeeActionBar.DISPLAY_SHOW_CUSTOM);
        }

        mContentHeight = a.getLayoutDimension(R.styleable.CyeeActionBar_cyeeheight, 0);
        
        a.recycle();
        
        mLogoNavItem = new ActionMenuItem(context, 0, com.cyee.internal.R.id.cyee_home, 0, 0, mTitle);
        mHomeLayout.setOnClickListener(mUpClickListener);
        mHomeLayout.setClickable(true);
        mHomeLayout.setFocusable(true);

        if (getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        mMaxHomeSlop =
                (int) (MAX_HOME_SLOP * context.getResources().getDisplayMetrics().density + 0.5f);

	}




	public void setHomeButtonEnabled(boolean enable) {
        mHomeLayout.setEnabled(enable);
        mHomeLayout.setFocusable(enable);
        // Make sure the home button has an accurate content description for accessibility.
        if (!enable) {
            mHomeLayout.setContentDescription(null);
            mHomeLayout.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        } else {
            mHomeLayout.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_AUTO);
            if ((mDisplayOptions & CyeeActionBar.DISPLAY_HOME_AS_UP) != 0) {
                mHomeLayout.setContentDescription(mContext.getResources().getText(
                        com.cyee.internal.R.string.cyee_action_bar_up_description));
            } else {
                mHomeLayout.setContentDescription(mContext.getResources().getText(
                        com.cyee.internal.R.string.cyee_action_bar_home_description));
            }
        }
    }
	




	public void setDisplayOptions(int options) {
//		final int flagsChanged = mDisplayOptions == -1 ? -1 : options ^ mDisplayOptions;
//		mDisplayOptions = options;
//
//
//		if ((flagsChanged & CyeeActionBar.DISPLAY_SHOW_TITLE) != 0) {
//			if ((options & CyeeActionBar.DISPLAY_SHOW_TITLE) != 0) {
//				initTitle();
//			} else {
//				removeView(mTitleLayout);
//			}
//		}
		final int flagsChanged = mDisplayOptions == -1 ? -1 : options ^ mDisplayOptions;
		mDisplayOptions = options;

		if ((flagsChanged & DISPLAY_RELAYOUT_MASK) != 0) {
			final boolean showHome = (options & CyeeActionBar.DISPLAY_SHOW_HOME) != 0;
			final int vis = showHome && mExpandedActionView == null ? VISIBLE : GONE;
			mHomeLayout.setVisibility(vis);

			if ((flagsChanged & CyeeActionBar.DISPLAY_HOME_AS_UP) != 0) {
				final boolean setUp = (options & CyeeActionBar.DISPLAY_HOME_AS_UP) != 0;
				mHomeLayout.setUp(setUp);

				// Showing home as up implicitly enables interaction with it.
				// In honeycomb it was always enabled, so make this transition
				// a bit easier for developers in the common case.
				// (It would be silly to show it as up without responding to it.)
				if (setUp) {
					setHomeButtonEnabled(true);
				}
			}

			if ((flagsChanged & CyeeActionBar.DISPLAY_USE_LOGO) != 0) {
				final boolean logoVis = mLogo != null && (options & CyeeActionBar.DISPLAY_USE_LOGO) != 0;
				mHomeLayout.setIcon(logoVis ? mLogo : mIcon);
			}

			if ((flagsChanged & CyeeActionBar.DISPLAY_SHOW_TITLE) != 0) {
				if ((options & CyeeActionBar.DISPLAY_SHOW_TITLE) != 0) {
					initTitle();
				} else {
					removeView(mTitleLayout);
				}
			}

			if (mTitleLayout != null
					&& (flagsChanged & (CyeeActionBar.DISPLAY_HOME_AS_UP | CyeeActionBar.DISPLAY_SHOW_HOME)) != 0) {
				final boolean homeAsUp = (mDisplayOptions & CyeeActionBar.DISPLAY_HOME_AS_UP) != 0;
				mTitleUpView.setVisibility(!showHome ? (homeAsUp ? VISIBLE : GONE) : GONE);
				if (mEmptyView != null) {
                    mEmptyView.setVisibility((!showHome && !homeAsUp) ? VISIBLE : GONE);
				}
				mTitleLayout.setEnabled(!showHome && homeAsUp);
				mTitleLayout.setClickable(!showHome && homeAsUp);
			}

			if ((flagsChanged & CyeeActionBar.DISPLAY_SHOW_CUSTOM) != 0 && mCustomNavView != null) {
				if ((options & CyeeActionBar.DISPLAY_SHOW_CUSTOM) != 0) {
					addView(mCustomNavView);
				} else {
					removeView(mCustomNavView);
				}
			}

			requestLayout();
		} else {
			invalidate();
		}

		// Make sure the home button has an accurate content description for accessibility.
		if (!mHomeLayout.isEnabled()) {
			mHomeLayout.setContentDescription(null);
			mHomeLayout.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
		} else {
			mHomeLayout.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_AUTO);
			if ((options & CyeeActionBar.DISPLAY_HOME_AS_UP) != 0) {
				mHomeLayout.setContentDescription(mContext.getResources().getText(
						com.cyee.internal.R.string.cyee_action_bar_up_description));
			} else {
				mHomeLayout.setContentDescription(mContext.getResources().getText(
						com.cyee.internal.R.string.cyee_action_bar_home_description));
			}
		}
	}

    public void setIcon(Drawable icon) {
        mIcon = icon;
        if (icon != null &&
                ((mDisplayOptions & ActionBar.DISPLAY_USE_LOGO) == 0 || mLogo == null)) {
            mHomeLayout.setIcon(icon);
        }
        if (mExpandedActionView != null) {
            mExpandedHomeLayout.setIcon(mIcon.getConstantState().newDrawable(getResources()));
        }
    }

    public void setIcon(int resId) {
        setIcon(mContext.getResources().getDrawable(resId));
    }

    public void setLogo(Drawable logo) {
        mLogo = logo;
        if (logo != null && (mDisplayOptions & ActionBar.DISPLAY_USE_LOGO) != 0) {
            mHomeLayout.setIcon(logo);
        }
    }

    public void setLogo(int resId) {
        setLogo(mContext.getResources().getDrawable(resId));
    }
	
    public void setNavigationMode(int mode) {
		final int oldMode = mNavigationMode;
		if (mode != oldMode) {
			switch (oldMode) {
//				case CyeeActionBar.NAVIGATION_MODE_LIST:
//                if (mListNavLayout != null) {
//                    removeView(mListNavLayout);
//                }
//					break;
				case CyeeActionBar.NAVIGATION_MODE_TABS:
					if (mTabContainer != null && mIncludeTabs) {
						removeView(mTabContainer);
					}
			}

			switch (mode) {
				case CyeeActionBar.NAVIGATION_MODE_LIST:
					if (mSpinner == null) {
						mSpinner = new Spinner(mContext, null, android.R.attr.actionDropDownStyle);
						mListNavLayout = new LinearLayout(mContext, null, android.R.attr.actionBarTabBarStyle);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
								LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
						params.gravity = Gravity.CENTER;
						mListNavLayout.addView(mSpinner, params);
					}
					if (mSpinner.getAdapter() != mSpinnerAdapter) {
						mSpinner.setAdapter(mSpinnerAdapter);
					}
					mSpinner.setOnItemSelectedListener(mNavItemSelectedListener);
					addView(mListNavLayout);
					break;
				case CyeeActionBar.NAVIGATION_MODE_TABS:
					if (mTabContainer != null && mIncludeTabs) {
						addView(mTabContainer);
					}
					break;
			}
			mNavigationMode = mode;
			requestLayout();
		}
	}

    public void setDropdownAdapter(SpinnerAdapter adapter) {
        mSpinnerAdapter = adapter;
        if (mSpinner != null) {
            mSpinner.setAdapter(adapter);
        }
    }

    public SpinnerAdapter getDropdownAdapter() {
        return mSpinnerAdapter;
    }

    public void setDropdownSelectedPosition(int position) {
        mSpinner.setSelection(position);
    }

    public int getDropdownSelectedPosition() {
        return mSpinner.getSelectedItemPosition();
    }

    
	public int getNavigationMode() {
		return mNavigationMode;
	}
	
	public View getCustomNavigationView() {
		return mCustomNavView;
	}

	public int getDisplayOptions() {
		return mDisplayOptions;
	}
	
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		// Used by custom nav views if they don't supply layout params. Everything else
		// added to an ActionBarView should have them already.
		return new CyeeActionBar.LayoutParams(DEFAULT_CUSTOM_GRAVITY);
	}
	
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        addView(mHomeLayout);

        if (mCustomNavView != null && (mDisplayOptions & ActionBar.DISPLAY_SHOW_CUSTOM) != 0) {
            final ViewParent parent = mCustomNavView.getParent();
            if (parent != this) {
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(mCustomNavView);
                }
                addView(mCustomNavView);
            }
        }
    }
    
    private void initTitle() {
        if (mTitleLayout == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            mTitleLayout = (LinearLayout) inflater.inflate(com.cyee.internal.R.layout.cyee_action_bar_title_item, this, false);
            mTitleView = (CyeeTextView) mTitleLayout.findViewById(com.cyee.internal.R.id.cyee_action_bar_title);
            mSubtitleView = (CyeeTextView) mTitleLayout.findViewById(com.cyee.internal.R.id.cyee_action_bar_subtitle);
            mTitleUpView = (View) mTitleLayout.findViewById(com.cyee.internal.R.id.cyee_up);
            mEmptyView = (View) mTitleLayout.findViewById(com.cyee.internal.R.id.cyee_empty_view);

            mTitleUpView.setOnClickListener(mUpClickListener);

            if (mTitleStyleRes != 0) {
                mTitleView.setTextAppearance(getContext(), mTitleStyleRes);
            }

            if (mTitle != null) {
                mTitleView.setText(mTitle);
            }

            if (mSubtitleStyleRes != 0) {
                mSubtitleView
                        .setTextAppearance(getContext(), mSubtitleStyleRes);
            }
            if (mSubtitle != null) {
                mSubtitleView.setText(mSubtitle);
                mSubtitleView.setVisibility(VISIBLE);
            }
            if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                changeTitleViewColors();
            }

            final boolean homeAsUp = (mDisplayOptions & CyeeActionBar.DISPLAY_HOME_AS_UP) != 0;
            final boolean showHome = (mDisplayOptions & CyeeActionBar.DISPLAY_SHOW_HOME) != 0;
            final boolean showTitleUp = !showHome;
            mTitleUpView
                    .setVisibility(showTitleUp ? (homeAsUp ? VISIBLE : GONE)
                            : GONE);
            View emptyView = (View) mTitleLayout.findViewById(com.cyee.internal.R.id.cyee_empty_view);
            if (emptyView != null) {
                mEmptyView.setVisibility((!showHome && !homeAsUp) ? VISIBLE
                        : GONE);
            }
            mTitleLayout.setEnabled(homeAsUp && showTitleUp);
            mTitleLayout.setClickable(homeAsUp && showTitleUp);
        }
        addView(mTitleLayout);
        if (TextUtils.isEmpty(mTitle) && TextUtils.isEmpty(mSubtitle)) {
            // Don't show while in expanded mode or with empty text
            mTitleLayout.setVisibility(GONE);
        }
    }
	
	public void setContextView(CyeeActionBarContextView view) {
		mContextView = view;
	}

	
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CyeeActionBar.LayoutParams(getContext(), attrs);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp == null) {
            lp = generateDefaultLayoutParams();
        }
        return lp;
    }

    
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);

        if (mExpandedMenuPresenter != null && mExpandedMenuPresenter.mCurrentExpandedItem != null) {
            state.expandedMenuItemId = mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }

        state.isOverflowOpen = isOverflowMenuShowing();

        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable p) {
        SavedState state = (SavedState) p;

        super.onRestoreInstanceState(state.getSuperState());

        if (state.expandedMenuItemId != 0 &&
                mExpandedMenuPresenter != null && mOptionsMenu != null) {
            final MenuItem item = mOptionsMenu.findItem(state.expandedMenuItemId);
            if (item != null) {
                MenuItemCompat.expandActionView(item);
            }
        }

        if (state.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }
    
    static class SavedState extends BaseSavedState {
        int expandedMenuItemId;
        boolean isOverflowOpen;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            expandedMenuItemId = in.readInt();
            isOverflowOpen = in.readInt() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(expandedMenuItemId);
            out.writeInt(isOverflowOpen ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }



    public void setCollapsable(boolean collapsable) {
        mIsCollapsable = collapsable;
    }

    public boolean isCollapsed() {
    	return mIsCollapsed;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int childCount = getChildCount();
        
        if (mIsCollapsable) {
            int visibleChildren = 0;
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != GONE &&
                        !(child == mMenuView && mMenuView.getChildCount() == 0)) {
                	
                	if(mHomeLayout == child || mTitleLayout == child) {
                		if(child.getVisibility() != View.GONE) {
                			visibleChildren++;
                		}
                	} else {
                		visibleChildren++;
                	}
                }
            }

            if (visibleChildren == 0) {
                // No size for an empty action bar when collapsable.
                setMeasuredDimension(0, 0);
                mIsCollapsed = true;
                return;
            }
        }
        mIsCollapsed = false;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " +
                    "with android:layout_width=\"match_parent\" (or fill_parent)");
        }
        
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.AT_MOST) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " +
                    "with android:layout_height=\"wrap_content\"");
        }

        int contentWidth = MeasureSpec.getSize(widthMeasureSpec);

        int maxHeight = mContentHeight >= 0 ?
                mContentHeight : MeasureSpec.getSize(heightMeasureSpec);
        
        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int height = maxHeight - verticalPadding;
        final int childSpecHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        final int exactHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        int availableWidth = contentWidth - paddingLeft - paddingRight;
        int leftOfCenter = availableWidth / 2;
        int rightOfCenter = leftOfCenter;

        HomeView homeLayout = mExpandedActionView != null ? mExpandedHomeLayout : mHomeLayout;

        if (homeLayout.getVisibility() != GONE) {
            final ViewGroup.LayoutParams lp = homeLayout.getLayoutParams();
            int homeWidthSpec;
            if (lp.width < 0) {
                homeWidthSpec = MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST);
            } else {
                homeWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            }
            homeLayout.measure(homeWidthSpec, exactHeightSpec);
            final int homeWidth = homeLayout.getMeasuredWidth() + homeLayout.getStartOffset();
            availableWidth = Math.max(0, availableWidth - homeWidth);
            leftOfCenter = Math.max(0, availableWidth - homeWidth);
        }
        
        if (mMenuView != null && mMenuView.getParent() == this) {
            availableWidth = measureChildView(mMenuView, availableWidth, exactHeightSpec, 0);
            rightOfCenter = Math.max(0, rightOfCenter - mMenuView.getMeasuredWidth());
        }

        if (mIndeterminateProgressView != null &&
                mIndeterminateProgressView.getVisibility() != GONE) {
            availableWidth = measureChildView(mIndeterminateProgressView, availableWidth,
                    childSpecHeight, 0);
            rightOfCenter = Math.max(0,
                    rightOfCenter - mIndeterminateProgressView.getMeasuredWidth());
        }

        final boolean showTitle = mTitleLayout != null && mTitleLayout.getVisibility() != GONE &&
                (mDisplayOptions & ActionBar.DISPLAY_SHOW_TITLE) != 0;

        if (mExpandedActionView == null) {
            switch (mNavigationMode) {
                case ActionBar.NAVIGATION_MODE_LIST:
                    if (mListNavLayout != null) {
                        final int itemPaddingSize = showTitle ? mItemPadding * 2 : mItemPadding;
                        availableWidth = Math.max(0, availableWidth - itemPaddingSize);
                        leftOfCenter = Math.max(0, leftOfCenter - itemPaddingSize);
                        mListNavLayout.measure(
                                MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
                                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                        final int listNavWidth = mListNavLayout.getMeasuredWidth();
                        availableWidth = Math.max(0, availableWidth - listNavWidth);
                        leftOfCenter = Math.max(0, leftOfCenter - listNavWidth);
                    }
                    break;
                case ActionBar.NAVIGATION_MODE_TABS:
                    if (mTabContainer != null) {
                        final int itemPaddingSize = showTitle ? mItemPadding * 2 : mItemPadding;
                        availableWidth = Math.max(0, availableWidth - itemPaddingSize);
                        leftOfCenter = Math.max(0, leftOfCenter - itemPaddingSize);
                        mTabContainer.measure(
                                MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
                                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                        final int tabWidth = mTabContainer.getMeasuredWidth();
                        availableWidth = Math.max(0, availableWidth - tabWidth);
                        leftOfCenter = Math.max(0, leftOfCenter - tabWidth);
                    }
                    break;
            }
        }

        View customView = null;
        if (mExpandedActionView != null) {
            customView = mExpandedActionView;
        } else if ((mDisplayOptions & ActionBar.DISPLAY_SHOW_CUSTOM) != 0 &&
                mCustomNavView != null) {
            customView = mCustomNavView;
        }

        if (customView != null) {
            final ViewGroup.LayoutParams lp = generateLayoutParams(customView.getLayoutParams());
            final CyeeActionBar.LayoutParams ablp = lp instanceof CyeeActionBar.LayoutParams ?
                    (CyeeActionBar.LayoutParams) lp : null;

            int horizontalMargin = 0;
            int verticalMargin = 0;
            if (ablp != null) {
                horizontalMargin = ablp.leftMargin + ablp.rightMargin;
                verticalMargin = ablp.topMargin + ablp.bottomMargin;
            }

            // If the action bar is wrapping to its content height, don't allow a custom
            // view to MATCH_PARENT.
            int customNavHeightMode;
            if (mContentHeight <= 0) {
                customNavHeightMode = MeasureSpec.AT_MOST;
            } else {
                customNavHeightMode = lp.height != LayoutParams.WRAP_CONTENT ?
                        MeasureSpec.EXACTLY : MeasureSpec.AT_MOST;
            }
            final int customNavHeight = Math.max(0,
                    (lp.height >= 0 ? Math.min(lp.height, height) : height) - verticalMargin);

            final int customNavWidthMode = lp.width != LayoutParams.WRAP_CONTENT ?
                    MeasureSpec.EXACTLY : MeasureSpec.AT_MOST;
            int customNavWidth = Math.max(0,
                    (lp.width >= 0 ? Math.min(lp.width, availableWidth) : availableWidth)
                    - horizontalMargin);
            final int hgrav = (ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY) &
                    Gravity.HORIZONTAL_GRAVITY_MASK;

            // Centering a custom view is treated specially; we try to center within the whole
            // action bar rather than in the available space.
            if (hgrav == Gravity.CENTER_HORIZONTAL && lp.width == LayoutParams.MATCH_PARENT) {
                customNavWidth = Math.min(leftOfCenter, rightOfCenter) * 2;
            }

            customView.measure(
                    MeasureSpec.makeMeasureSpec(customNavWidth, customNavWidthMode),
                    MeasureSpec.makeMeasureSpec(customNavHeight, customNavHeightMode));
            availableWidth -= horizontalMargin + customView.getMeasuredWidth();
        }

        if (mExpandedActionView == null && showTitle) {
            availableWidth = measureChildView(mTitleLayout, availableWidth,
                    MeasureSpec.makeMeasureSpec(mContentHeight, MeasureSpec.EXACTLY), 0);
            leftOfCenter = Math.max(0, leftOfCenter - mTitleLayout.getMeasuredWidth());
        }

        if (mContentHeight <= 0) {
            int measuredHeight = 0;
            for (int i = 0; i < childCount; i++) {
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

        if (mContextView != null) {
            mContextView.setContentHeight(getMeasuredHeight());
        }

        if (mProgressView != null && mProgressView.getVisibility() != GONE) {
            mProgressView.measure(MeasureSpec.makeMeasureSpec(
                    contentWidth - mProgressBarPadding * 2, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.AT_MOST));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int contentHeight = b - t - getPaddingTop() - getPaddingBottom();

        if (contentHeight <= 0) {
            // Nothing to do if we can't see anything.
            return;
        }

        final boolean isLayoutRtl = isLayoutRtl();
        final int direction = isLayoutRtl ? +1 : -1;
        int menuStart = isLayoutRtl ? getPaddingLeft() : r - l - getPaddingRight();
        // In LTR mode, we start from left padding and go to the right; in RTL mode, we start
        // from the padding right and go to the left (in reverse way)
        int x = isLayoutRtl ? r - l - getPaddingRight() : getPaddingLeft();
        final int y = getPaddingTop();

        HomeView homeLayout = mExpandedActionView != null ? mExpandedHomeLayout : mHomeLayout;
        boolean needsTouchDelegate = false;
        int homeSlop = mMaxHomeSlop;
        int homeRight = 0;
        if (homeLayout.getVisibility() != GONE) {
            final int startOffset = homeLayout.getStartOffset();
            x += positionChild(homeLayout,
                            next(x, startOffset, isLayoutRtl), y, contentHeight, isLayoutRtl);
            x = next(x, startOffset, isLayoutRtl);
            needsTouchDelegate = homeLayout == mHomeLayout;
            homeRight = x;
        }

        if (mExpandedActionView == null) {
            final boolean showTitle = mTitleLayout != null && mTitleLayout.getVisibility() != GONE &&
                    (mDisplayOptions & ActionBar.DISPLAY_SHOW_TITLE) != 0;
            if (showTitle) {
                x += positionChild(mTitleLayout, x, y, contentHeight, isLayoutRtl);
            }

            switch (mNavigationMode) {
                case ActionBar.NAVIGATION_MODE_STANDARD:
                    break;
                case ActionBar.NAVIGATION_MODE_LIST:
                    if (mListNavLayout != null) {
                        if (showTitle) {
                            x = next(x, mItemPadding, isLayoutRtl);
                        }
                        homeSlop = Math.min(homeSlop, Math.max(x - homeRight, 0));
                        x += positionChild(mListNavLayout, x, y, contentHeight, isLayoutRtl);
                        x = next(x, mItemPadding, isLayoutRtl);
                    }
                    break;
                case ActionBar.NAVIGATION_MODE_TABS:
                    if (mTabContainer != null) {
                        if (showTitle) x = next(x, mItemPadding, isLayoutRtl);
                        homeSlop = Math.min(homeSlop, Math.max(x - homeRight, 0));
                        x += positionChild(mTabContainer, x, y, contentHeight, isLayoutRtl);
                        x = next(x, mItemPadding, isLayoutRtl);
                    }
                    break;
            }
        }

        if (mMenuView != null && mMenuView.getParent() == this) {
            positionChild(mMenuView, menuStart, y, contentHeight, !isLayoutRtl);
            menuStart += direction * mMenuView.getMeasuredWidth();
        }

        if (mIndeterminateProgressView != null &&
                mIndeterminateProgressView.getVisibility() != GONE) {
            positionChild(mIndeterminateProgressView, menuStart, y, contentHeight, !isLayoutRtl);
            menuStart += direction * mIndeterminateProgressView.getMeasuredWidth();
        }

        View customView = null;
        if (mExpandedActionView != null) {
            customView = mExpandedActionView;
        } else if ((mDisplayOptions & ActionBar.DISPLAY_SHOW_CUSTOM) != 0 &&
                mCustomNavView != null) {
            customView = mCustomNavView;
        }
        if (customView != null) {
            final int layoutDirection = getLayoutDirection();
            ViewGroup.LayoutParams lp = customView.getLayoutParams();
            final CyeeActionBar.LayoutParams ablp = lp instanceof CyeeActionBar.LayoutParams ?
                    (CyeeActionBar.LayoutParams) lp : null;
            final int gravity = ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY;
            final int navWidth = customView.getMeasuredWidth();

            int topMargin = 0;
            int bottomMargin = 0;
            if (ablp != null) {
                x = next(x, ablp.getMarginStart(), isLayoutRtl);
                menuStart += direction * ablp.getMarginEnd();
                topMargin = ablp.topMargin;
                bottomMargin = ablp.bottomMargin;
            }

            int hgravity = gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
            // See if we actually have room to truly center; if not push against left or right.
            if (hgravity == Gravity.CENTER_HORIZONTAL) {
                final int centeredLeft = ((mRight - mLeft) - navWidth) / 2;
                if (isLayoutRtl) {
                    final int centeredStart = centeredLeft + navWidth;
                    final int centeredEnd = centeredLeft;
                    if (centeredStart > x) {
                        hgravity = Gravity.RIGHT;
                    } else if (centeredEnd < menuStart) {
                        hgravity = Gravity.LEFT;
                    }
                } else {
                    final int centeredStart = centeredLeft;
                    final int centeredEnd = centeredLeft + navWidth;
                    if (centeredStart < x) {
                        hgravity = Gravity.LEFT;
                    } else if (centeredEnd > menuStart) {
                        hgravity = Gravity.RIGHT;
                    }
                }
            } else if (gravity == Gravity.NO_GRAVITY) {
                hgravity = Gravity.START;
            }

            int xpos = 0;
            switch (Gravity.getAbsoluteGravity(hgravity, layoutDirection)) {
                case Gravity.CENTER_HORIZONTAL:
                    xpos = ((mRight - mLeft) - navWidth) / 2;
                    break;
                case Gravity.LEFT:
                    xpos = isLayoutRtl ? menuStart : x;
                    break;
                case Gravity.RIGHT:
                    xpos = isLayoutRtl ? x - navWidth : menuStart - navWidth;
                    break;
            }

            int vgravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            if (gravity == Gravity.NO_GRAVITY) {
                vgravity = Gravity.CENTER_VERTICAL;
            }

            int ypos = 0;
            switch (vgravity) {
                case Gravity.CENTER_VERTICAL:
                    final int paddedTop = getPaddingTop();
                    final int paddedBottom = mBottom - mTop - getPaddingBottom();
                    ypos = ((paddedBottom - paddedTop) - customView.getMeasuredHeight()) / 2;
                    break;
                case Gravity.TOP:
                    ypos = getPaddingTop() + topMargin;
                    break;
                case Gravity.BOTTOM:
                    ypos = getHeight() - getPaddingBottom() - customView.getMeasuredHeight()
                            - bottomMargin;
                    break;
            }
            final int customWidth = customView.getMeasuredWidth();
            customView.layout(xpos, ypos, xpos + customWidth,
                    ypos + customView.getMeasuredHeight());
            homeSlop = Math.min(homeSlop, Math.max(xpos - homeRight, 0));
            x = next(x, customWidth, isLayoutRtl);
        }

        if (mProgressView != null) {
            mProgressView.bringToFront();
            final int halfProgressHeight = mProgressView.getMeasuredHeight() / 2;
            mProgressView.layout(mProgressBarPadding, -halfProgressHeight,
                    mProgressBarPadding + mProgressView.getMeasuredWidth(), halfProgressHeight);
        }

        if (needsTouchDelegate) {
            mTempRect.set(homeLayout.getLeft(), homeLayout.getTop(),
                    homeLayout.getRight() + homeSlop, homeLayout.getBottom());
            setTouchDelegate(new TouchDelegate(mTempRect, homeLayout));
        } else {
            setTouchDelegate(null);
        }
    }

	public void setClickable(boolean clickable) {
		if (mTabContainer != null) {
			mTabContainer.setClickable(clickable);
		}
	}
	

//    public void setContentHeight(int height) {
//        mContentHeight = height;
//        requestLayout();
//    }

	@Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("CyeeActionBarView_onTouchEvent", "event.getAction()-->"
                + (event.getAction() & MotionEvent.ACTION_MASK));
        // Gionee <weidong> <2015-05-01> add for CR01473846 begin
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            doActionBarDoubleClick();
        }
        // Gionee <weidong> <2015-05-01> add for CR01473846 end
        return super.onTouchEvent(event);
    }

    // Gionee <weidong> <2015-05-01> add for CR01473846 begin
    public void setOnActionBarDoubleClickListener(OnClickListener listener) {
        mActionBarDoubleClickListener = listener;
    }

    private void doActionBarDoubleClick() {
        if (null == mActionBarDoubleClickListener) {
            return;
        }
        long time = System.currentTimeMillis();
        long gap = time - mCurTime;

        if (gap > 0 && gap <= CLICK_INTERVAL_TIME) {
            mCurTime = 0;
            mActionBarDoubleClickListener.onClick(null);
        } else {
            mCurTime = time;
        }
    }
    // Gionee <weidong> <2015-05-01> add for CR01473846 end
    
    @Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
//		if (mTabContainer != null && mIncludeTabs) {
//			ViewGroup.LayoutParams lp = mTabContainer.getLayoutParams();
//			if (lp != null) {
//				lp.width = LayoutParams.WRAP_CONTENT;
//				lp.height = LayoutParams.MATCH_PARENT;
//				mTabContainer.setLayoutParams(lp);
//			}
//
//		}

        mTitleView = null;
        mSubtitleView = null;
        mTitleUpView = null;
        if (mTitleLayout != null && mTitleLayout.getParent() == this) {
            removeView(mTitleLayout);
        }
        mTitleLayout = null;
        if ((mDisplayOptions & CyeeActionBar.DISPLAY_SHOW_TITLE) != 0) {
            initTitle();
        }

        if (mTabContainer != null && mIncludeTabs) {
            ViewGroup.LayoutParams lp = mTabContainer.getLayoutParams();
            if (lp != null) {
                lp.width = LayoutParams.WRAP_CONTENT;
                lp.height = LayoutParams.MATCH_PARENT;
            }
            mTabContainer.setAllowCollapse(true);
        }
        
        /// M: When configuration changed, we re-inflate home view to update layout params including margins.
        LayoutInflater inflater = LayoutInflater.from(getContext());
        HomeView tempHomeLayout = (HomeView)inflater.inflate(mHomeResId, this, false);
        if (tempHomeLayout != null) {
            ImageView tempIconView = (ImageView)tempHomeLayout.findViewById(com.cyee.internal.R.id.cyee_home);
            if (tempIconView != null) {
                HomeView homeLayout = mExpandedActionView != null ? mExpandedHomeLayout : mHomeLayout;
                homeLayout.setIconViewLayoutParams(tempIconView.getLayoutParams());
            }
        }
	}

    public void setWindowCallback(Window.Callback cb) {
        mWindowCallback = cb;
    }
    
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mTabSelector);
        if (mActionMenuPresenter != null) {
            mActionMenuPresenter.hideOverflowMenu();
            mActionMenuPresenter.hideSubMenus();
        }
    }

	
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }
    
    public void initProgress() {
//        mProgressView = new ProgressBar(mContext, null, 0, mProgressStyle);
//        mProgressView.setId(R.id.progress_horizontal);
//        mProgressView.setMax(10000);
//        mProgressView.setVisibility(GONE);
//        addView(mProgressView);
    }
    
    public void initIndeterminateProgress() {
//        mIndeterminateProgressView = new ProgressBar(mContext, null, 0,
//                mIndeterminateProgressStyle);
//        mIndeterminateProgressView.setId(R.id.progress_circular);
//        mIndeterminateProgressView.setVisibility(GONE);
//        addView(mIndeterminateProgressView);
    }

    @Override
    public void setSplitActionBar(boolean splitActionBar) {
//        if (mSplitActionBar != splitActionBar) {
//            if (mMenuView != null) {
//                final ViewGroup oldParent = (ViewGroup) mMenuView.getParent();
//                if (oldParent != null) {
//                    oldParent.removeView(mMenuView);
//                }
//                if (splitActionBar) {
//                    if (mSplitView != null) {
//                        mSplitView.addView(mMenuView);
//                    }
//                    mMenuView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
//                } else {
//                    addView(mMenuView);
//                    mMenuView.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
//                }
//                mMenuView.requestLayout();
//            }
//            if (mSplitView != null) {
//                mSplitView.setVisibility(splitActionBar ? VISIBLE : GONE);
//            }
//
//            if (mActionMenuPresenter != null) {
//                if (!splitActionBar) {
//                    mActionMenuPresenter.setExpandedActionViewsExclusive(
//                            getResources().getBoolean(
//                                    com.android.internal.R.bool.action_bar_expanded_action_views_exclusive));
//                } else {
//                    mActionMenuPresenter.setExpandedActionViewsExclusive(false);
//                    // Allow full screen width in split mode.
//                    mActionMenuPresenter.setWidthLimit(
//                            getContext().getResources().getDisplayMetrics().widthPixels, true);
//                    // No limit to the item count; use whatever will fit.
//                    mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
//                }
//            }
//            super.setSplitActionBar(splitActionBar);
//        }
    }    

	
	public boolean isSplitActionBar() {
		return false;
	}

	public boolean hasEmbeddedTabs() {
		return mIncludeTabs;
	}
	
	public void setEmbeddedTabView(CyeeTabContainerView tabs) {
		if (mTabContainer != null) {
			removeView(mTabContainer);
		}
		mTabContainer = tabs;
		mIncludeTabs = tabs != null;
		if (mIncludeTabs && mNavigationMode == CyeeActionBar.NAVIGATION_MODE_TABS) {
//			ViewGroup.LayoutParams lp = mTabContainer.getLayoutParams();
//			if (lp != null) {
//				lp.width = LayoutParams.WRAP_CONTENT;
//				lp.height = LayoutParams.MATCH_PARENT;
//				mTabContainer.setLayoutParams(lp);
//			} else {
//				mTabContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//						ViewGroup.LayoutParams.MATCH_PARENT));
//			}
//			addView(mTabContainer);
		    addView(mTabContainer);
            ViewGroup.LayoutParams lp = mTabContainer.getLayoutParams();
            lp.width = LayoutParams.WRAP_CONTENT;
            lp.height = LayoutParams.MATCH_PARENT;
            tabs.setAllowCollapse(true);
		}
	}
	
    public void setCallback(OnNavigationListener callback) {
        mCallback = callback;
    }
    
    private void configPresenters(MenuBuilder builder) {
        if (builder != null) {
            builder.addMenuPresenter(mActionMenuPresenter);
            builder.addMenuPresenter(mExpandedMenuPresenter);
        } else {
            mActionMenuPresenter.initForMenu(mContext, null);
            mExpandedMenuPresenter.initForMenu(mContext, null);
            mActionMenuPresenter.updateMenuView(true);
            mExpandedMenuPresenter.updateMenuView(true);
        }
    }

    public boolean hasExpandedActionView() {
        return mExpandedMenuPresenter != null &&
                mExpandedMenuPresenter.mCurrentExpandedItem != null;
    }
    
    public void collapseActionView() {
        final MenuItemImpl item = mExpandedMenuPresenter == null ? null :
                mExpandedMenuPresenter.mCurrentExpandedItem;
        if (item != null) {
            MenuItemCompat.collapseActionView(item);
        }
    }
    
	public void setCustomNavigationView(View view) {
		final boolean showCustom = (mDisplayOptions & CyeeActionBar.DISPLAY_SHOW_CUSTOM) != 0;
		if (mCustomNavView != null && showCustom) {
			removeView(mCustomNavView);
		}
		mCustomNavView = view;
		if (mCustomNavView != null && showCustom) {
			addView(mCustomNavView);
		}
	}
	
	public CharSequence getTitle() {
		return mTitle;
	}

	public void setTitle(CharSequence title) {
		mUserTitle = true;
		setTitleImpl(title);
	}

    public void setWindowTitle(CharSequence title) {
        if (!mUserTitle) {
            setTitleImpl(title);
        }
    }
    
	private void setTitleImpl(CharSequence title) {
		mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
            final boolean visible = mExpandedActionView == null &&
                    (mDisplayOptions & ActionBar.DISPLAY_SHOW_TITLE) != 0 &&
                    (!TextUtils.isEmpty(mTitle) || !TextUtils.isEmpty(mSubtitle));
            mTitleLayout.setVisibility(visible ? VISIBLE : GONE);
        }
        if (mLogoNavItem != null) {
            mLogoNavItem.setTitle(title);
        }
	}
	
	public CharSequence getSubtitle() {
		return mSubtitle;
	}
	
	public void setSubtitle(CharSequence subtitle) {
        mSubtitle = subtitle;
        if (mSubtitleView != null) {
            mSubtitleView.setText(subtitle);
            mSubtitleView.setVisibility(subtitle != null ? VISIBLE : GONE);
            final boolean visible = mExpandedActionView == null &&
                    (mDisplayOptions & CyeeActionBar.DISPLAY_SHOW_TITLE) != 0 &&
                    (!TextUtils.isEmpty(mTitle) || !TextUtils.isEmpty(mSubtitle));
            mTitleLayout.setVisibility(visible ? VISIBLE : GONE);
        }
    }
		
    private static class HomeView extends FrameLayout {
        private View mUpView;
        private ImageView mIconView;
        private int mUpWidth;
        private OnClickListener mBackButtonlistener;

        private static final long DEFAULT_TRANSITION_DURATION = 150;

        public HomeView(Context context) {
            this(context, null);
        }

        public HomeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutTransition t = getLayoutTransition();
            if (t != null) {
                // Set a lower duration than the default
                t.setDuration(DEFAULT_TRANSITION_DURATION);
            }
        }

        public void setUp(boolean isUp) {
            if (mUpView != null) {
                mUpView.setVisibility(isUp ? VISIBLE : GONE);
            }
            if (mIconView != null) {
                mIconView.setVisibility(isUp ? GONE : VISIBLE);
            }
        }

        public void setIcon(Drawable icon) {
            // Gionee <lihq> <2014-6-10> add for CR00873172 begin
            if (icon == null) {
                return;
            }
            // Gionee <lihq> <2014-6-10> add for CR00873172 end
            mIconView.setImageDrawable(icon);
        }
        
        public int getUpWidth() {
            return mUpWidth;
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            onPopulateAccessibilityEvent(event);
            return true;
        }

        @Override
        public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(event);
            final CharSequence cdesc = getContentDescription();
            if (!TextUtils.isEmpty(cdesc)) {
                event.getText().add(cdesc);
            }
        }

        @Override
        public boolean dispatchHoverEvent(MotionEvent event) {
            // Don't allow children to hover; we want this to be treated as a single component.
            return onHoverEvent(event);
        }

        @Override
        protected void onFinishInflate() {
            mUpView = findViewById(com.cyee.internal.R.id.cyee_up);
            mIconView = (ImageView) findViewById(com.cyee.internal.R.id.cyee_home);
        }
        
		public void changeColor() {
			Drawable backgroundDrawable = getBackground();
			if(backgroundDrawable != null && backgroundDrawable instanceof RippleDrawable){
				((RippleDrawable)backgroundDrawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
			}

			if (mUpView != null) {
				ImageView iv = ((ImageView) mUpView);
				Drawable drawable = iv.getDrawable();
				if(drawable != null){			
					drawable.setColorFilter(
							ChameleonColorManager.getContentColorPrimaryOnAppbar_T1(),
							android.graphics.PorterDuff.Mode.SRC_IN);
					iv.setImageDrawable(drawable);
				}
			}
			
			if(mIconView != null){
				Drawable drawable = mIconView.getDrawable();
				if(drawable != null){
					drawable.setColorFilter(
							ChameleonColorManager.getContentColorPrimaryOnAppbar_T1(),
							android.graphics.PorterDuff.Mode.SRC_IN);
					mIconView.setImageDrawable(drawable);				
				}

			}
		}

		public void changeTheme() {
		    if (mUpView != null) {
		        ImageView iv = ((ImageView) mUpView);
		        Drawable defaultDrawable = iv.getDrawable();

		        if(defaultDrawable instanceof StateListDrawable) {
		            Resources iCyeeRes = mContext.getResources();
                    Drawable bgDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_home_as_up);
                    iv.setImageDrawable(bgDrawable);
		        }
		    }
		}
		
        public int getStartOffset() {
        	return 0;
        	//return mUpView.getVisibility() == GONE ? mUpWidth : 0;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            measureChildWithMargins(mUpView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            final LayoutParams upLp = (LayoutParams) mUpView.getLayoutParams();
            mUpWidth = upLp.leftMargin + mUpView.getMeasuredWidth() + upLp.rightMargin;
            int width = mUpView.getVisibility() == GONE ? 0 : mUpWidth;
            int height = upLp.topMargin + mUpView.getMeasuredHeight() + upLp.bottomMargin;
			if (mIconView.getVisibility() != View.GONE) {
				measureChildWithMargins(mIconView, widthMeasureSpec, width, heightMeasureSpec, 0);
			}
            final LayoutParams iconLp = (LayoutParams) mIconView.getLayoutParams();
            width += iconLp.leftMargin + mIconView.getMeasuredWidth() + iconLp.rightMargin;
            height = Math.max(height,
                    iconLp.topMargin + mIconView.getMeasuredHeight() + iconLp.bottomMargin);

            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            switch (widthMode) {
                case MeasureSpec.AT_MOST:
                    width = Math.min(width, widthSize);
                    break;
                case MeasureSpec.EXACTLY:
                    width = widthSize;
                    break;
                case MeasureSpec.UNSPECIFIED:
                default:
                    break;
            }
            switch (heightMode) {
                case MeasureSpec.AT_MOST:
                    height = Math.min(height, heightSize);
                    break;
                case MeasureSpec.EXACTLY:
                    height = heightSize;
                    break;
                case MeasureSpec.UNSPECIFIED:
                default:
                    break;
            }
            setMeasuredDimension(width, height);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final int vCenter = (b - t) / 2;
            final boolean isLayoutRtl = isLayoutRtl();
            final int width = getWidth();
            int upOffset = 0;
            if (mUpView.getVisibility() != GONE) {
                final LayoutParams upLp = (LayoutParams) mUpView.getLayoutParams();
                final int upHeight = mUpView.getMeasuredHeight();
                final int upWidth = mUpView.getMeasuredWidth();
                upOffset = upLp.leftMargin + upWidth + upLp.rightMargin;
                final int upTop = vCenter - upHeight / 2;
                final int upBottom = upTop + upHeight;
                final int upRight;
                final int upLeft;
                if (isLayoutRtl) {
                    upRight = width;
                    upLeft = upRight - upWidth;
                    r -= upOffset;
                } else {
                    upRight = upWidth;
                    upLeft = 0;
                    l += upOffset;
                }
                mUpView.layout(upLeft, upTop, upRight, upBottom);
            }

            final LayoutParams iconLp = (LayoutParams) mIconView.getLayoutParams();
            final int iconHeight = mIconView.getMeasuredHeight();
            final int iconWidth = mIconView.getMeasuredWidth();
            final int hCenter = (r - l) / 2;
            final int iconTop = Math.max(iconLp.topMargin, vCenter - iconHeight / 2);
            final int iconBottom = iconTop + iconHeight;
            final int iconLeft;
            final int iconRight;
            int marginStart = iconLp.getMarginStart();
            final int delta = Math.max(marginStart, hCenter - iconWidth / 2);
            if (isLayoutRtl) {
                iconRight = width - upOffset - delta;
                iconLeft = iconRight - iconWidth;
            } else {
                iconLeft = upOffset + delta;
                iconRight = iconLeft + iconWidth;
            }

            mIconView.layout(iconLeft, iconTop, iconRight, iconBottom);
        }

        /**
         * Set new LayoutParams for icon view.
         * @param lp new LayoutParams for icon view
         * @hide
         */
        public void setIconViewLayoutParams(ViewGroup.LayoutParams lp) {
            mIconView.setLayoutParams(lp);
        }
    }
	
    private class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuBuilder mMenu;
        MenuItemImpl mCurrentExpandedItem;

        @Override
        public void initForMenu(Context context, MenuBuilder menu) {
            // Clear the expanded action view when menus change.
            if (mMenu != null && mCurrentExpandedItem != null) {
                mMenu.collapseItemActionView(mCurrentExpandedItem);
            }
            mMenu = menu;
        }

        @Override
        public MenuView getMenuView(ViewGroup root) {
            return null;
        }

        @Override
        public void updateMenuView(boolean cleared) {
            // Make sure the expanded item we have is still there.
            if (mCurrentExpandedItem != null) {
                boolean found = false;

                if (mMenu != null) {
                    final int count = mMenu.size();
                    for (int i = 0; i < count; i++) {
                        final MenuItem item = mMenu.getItem(i);
                        if (item == mCurrentExpandedItem) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    // The item we had expanded disappeared. Collapse.
                    collapseItemActionView(mMenu, mCurrentExpandedItem);
                }
            }
        }

        @Override
        public void setCallback(Callback cb) {
        }

        @Override
        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            return false;
        }

        @Override
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        @Override
        public boolean flagActionItems() {
            return false;
        }

        @Override
        public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
            mExpandedActionView = MenuItemCompat.getActionView(item);
            // Gionee <lihq> <2014-6-10> modify for CR00873172 begin
            if (mIcon != null) {
                mExpandedHomeLayout.setIcon(mIcon.getConstantState().newDrawable(getResources()));
            }
            // Gionee <lihq> <2014-6-10> modify for CR00873172 end
            mCurrentExpandedItem = item;
            if (mExpandedActionView.getParent() != CyeeActionBarView.this) {
                addView(mExpandedActionView);
            }
            if (mExpandedHomeLayout.getParent() != CyeeActionBarView.this) {
                addView(mExpandedHomeLayout);
            }
            mHomeLayout.setVisibility(GONE);
            if (mTitleLayout != null) mTitleLayout.setVisibility(GONE);
            if (mTabContainer!= null) mTabContainer.setVisibility(GONE);
            if (mSpinner != null) mSpinner.setVisibility(GONE);
            if (mCustomNavView != null) mCustomNavView.setVisibility(GONE);
            requestLayout();
            item.setActionViewExpanded(true);

            if (mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) mExpandedActionView).onActionViewExpanded();
            }

            return true;
        }

        @Override
        public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
            // Do this before detaching the actionview from the hierarchy, in case
            // it needs to dismiss the soft keyboard, etc.
            if (mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) mExpandedActionView).onActionViewCollapsed();
            }

            removeView(mExpandedActionView);
            removeView(mExpandedHomeLayout);
            mExpandedActionView = null;
            if ((mDisplayOptions & CyeeActionBar.DISPLAY_SHOW_HOME) != 0) {
                mHomeLayout.setVisibility(VISIBLE);
            }
            if ((mDisplayOptions & CyeeActionBar.DISPLAY_SHOW_TITLE) != 0) {
                if (mTitleLayout == null) {
                    initTitle();
                } else {
                    mTitleLayout.setVisibility(VISIBLE);
                }
            }
            if (mTabContainer != null && mNavigationMode == CyeeActionBar.NAVIGATION_MODE_TABS) {
                mTabContainer.setVisibility(VISIBLE);
            }
            if (mSpinner != null && mNavigationMode == CyeeActionBar.NAVIGATION_MODE_LIST) {
                mSpinner.setVisibility(VISIBLE);
            }
            if (mCustomNavView != null && (mDisplayOptions & CyeeActionBar.DISPLAY_SHOW_CUSTOM) != 0) {
                mCustomNavView.setVisibility(VISIBLE);
            }
            mExpandedHomeLayout.setIcon(null);
            mCurrentExpandedItem = null;
            requestLayout();
            item.setActionViewExpanded(false);

            return true;
        }

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public Parcelable onSaveInstanceState() {
            return null;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
        }
    }
    
	public void setActivity(CyeeActivity activity) {
		mActivity = activity;
	}
	

    // Gionee <fenglp> <2013-07-30> add for CR00812456 begin
    public void setOnBackClickListener(OnClickListener listener) {
        mBackClickListener= listener;        
    }
    
 // gionee maxw modify begin
 	public void setMenu(Menu menu) {
 		if (mMenuView != null) {
 			final ViewGroup oldParent = (ViewGroup) mMenuView.getParent();
 			if (oldParent != null) {
 				oldParent.removeView(mMenuView);
 			}
 		}

 		LayoutInflater inflater = LayoutInflater.from(mActivity);
 		LinearLayout menuView = new LinearLayout(mContext);
 		for (int i = 0; i < menu.size(); i++) {
 			final MenuItem menuItem = (MenuItem) (menu.getItem(i));

 			if (!menu.getItem(i).isVisible()
 					|| null == menu.getItem(i).getIcon()) {
 				continue;
 			}

 			int layoutId = com.cyee.internal.R.layout.cyee_actionbar_menu_item;
// 			int layoutId = com.cyee.internal.R.layout.cyee_actionbar_menu_item;
 			ImageView menuIv = (ImageView) inflater.inflate(layoutId, null);
 			Drawable itemIcon = menu.getItem(i).getIcon();
            if (ChameleonColorManager.getInstance().getCyeeThemeType(getContext()) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
                
            } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                Drawable drawable = menuIv.getBackground();
                if (drawable != null && drawable instanceof RippleDrawable) {
                    ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
                }
                itemIcon.setTint(ChameleonColorManager
                        .getContentColorPrimaryOnAppbar_T1());
            }

 			menuIv.setImageDrawable(itemIcon);
 			menuIv.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					mActivity.onOptionsItemSelected(menuItem);
 				}
 			});
 			menuIv.setOnLongClickListener(new OnLongClickListener() {
 				@Override
 				public boolean onLongClick(View view) {
 					mActivity.onOptionsItemLongClick(menuItem);
 					return false;
 				}
 			});
 			menuView.addView(menuIv, new LayoutParams(getContentHeight(),
 					getContentHeight()));
 			if (menuView.getChildCount() > 1) {
 				break;
 			}
 		}

 		addView(menuView, new LayoutParams(getContentHeight(),
 				LayoutParams.WRAP_CONTENT));
 		mMenuView = menuView;

 	}

    public void changeTitleViewColors() {
        if (mTitleView != null) {
            mTitleView.setTextColor(ChameleonColorManager
                    .getContentColorPrimaryOnAppbar_T1());
        }
        if (mSubtitleView != null) {
            mSubtitleView.setTextColor(ChameleonColorManager
                    .getContentColorSecondaryOnAppbar_T2());
        }
    }
 	
    @Override
    public void changeColors() {
        changeTitleViewColors();
        mHomeLayout.changeColor();
    }

    public void changeTheme() {
        mHomeLayout.changeTheme();
    }
    
}
