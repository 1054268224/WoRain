package cyee.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AnimationUtils;
import android.widget.SpinnerAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.android.internal.view.ActionBarPolicy;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.SubMenuBuilder;
import com.cyee.internal.view.CyeeActionBarPolicy;
import com.cyee.internal.widget.CyeeActionBarContainer;
import com.cyee.internal.widget.CyeeActionBarContextView;
import com.cyee.internal.widget.CyeeActionBarOverlayLayout;
import com.cyee.internal.widget.CyeeActionBarView;
import com.cyee.internal.widget.CyeeTabContainerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeMagicBar;

// Gionee <gaoj> <2013-9-6> modify for CR00889318 begin
// Gionee <gaoj> <2013-9-6> modify for CR00889318 end

public class CyeeActionBarImpl extends CyeeActionBar {
	public class TabImpl extends CyeeActionBar.Tab {
		private CyeeActionBar.TabListener mCallback;
		private Object mTag;
		private Drawable mIcon;
		private CharSequence mText;
		private CharSequence mContentDesc;
		private int mPosition = -1;
		private View mCustomView;
		private ColorStateList mTextColors = null;

		public CyeeActionBar.TabListener getCallback() {
			return mCallback;
		}

		@Override
		public CharSequence getContentDescription() {
			return mContentDesc;
		}

		@Override
		public View getCustomView() {
			return mCustomView;
		}

		@Override
		public Drawable getIcon() {
			return mIcon;
		}

		@Override
		public int getPosition() {
			return mPosition;
		}

		@Override
		public Object getTag() {
			return mTag;
		}

		@Override
		public CharSequence getText() {
			return mText;
		}

		@Override
		public void select() {
			selectTab(this);
		}

		@Override
		public Tab setContentDescription(CharSequence contentDesc) {
			mContentDesc = contentDesc;
			if (mPosition >= 0) {
				mTabContainerView.updateTab(mPosition);
			}
			return this;
		}

		@Override
		public Tab setContentDescription(int resId) {
			return setContentDescription(mContext.getResources().getText(resId));
		}

		@Override
		public Tab setCustomView(int layoutResId) {
			return setCustomView(LayoutInflater.from(getThemedContext()).inflate(layoutResId, null));
		}

		@Override
		public Tab setCustomView(View view) {
			mCustomView = view;
			if (mPosition >= 0) {
				mTabContainerView.updateTab(mPosition);
			}
			return this;
		}

		@Override
		public Tab setIcon(Drawable icon) {
			mIcon = icon;
			if (mPosition >= 0) {
				mTabContainerView.updateTab(mPosition);
			}
			return this;
		}

		@Override
		public Tab setIcon(int resId) {
			return setIcon(mContext.getResources().getDrawable(resId));
		}

		public void setPosition(int position) {
			mPosition = position;
		}

		@Override
		public Tab setTabListener(CyeeActionBar.TabListener callback) {
			mCallback = callback;
			return this;
		}

		@Override
		public Tab setTag(Object tag) {
			mTag = tag;
			return this;
		}

		@Override
		public Tab setText(CharSequence text) {
			mText = text;
			if (mPosition >= 0) {
				mTabContainerView.updateTab(mPosition);
			}
			return this;
		}

		@Override
		public Tab setText(int resId) {
			return setText(mContext.getResources().getText(resId));
		}

        @Override
        public Tab setTextColor(ColorStateList textColors) {
            // TODO Auto-generated method stub
            mTextColors = textColors;
            return this;
        }

        @Override
        public ColorStateList getTextColor() {
            // TODO Auto-generated method stub
            return mTextColors;
        }
	}

	private static final int CONTEXT_DISPLAY_NORMAL = 0;
	private static final int CONTEXT_DISPLAY_SPLIT = 1;
	private static final int INVALID_POSITION = -1;
	private int mSavedTabPosition = INVALID_POSITION;
	private AppCompatActivity mActivity;
	private Context mContext;
	private View mContentView;
	private CyeeActionBarView mActionView;
	private CyeeActionBarContainer mContainerView;
	private CyeeActionBarContainer mSpliteView;
	private CyeeTabContainerView mTabContainerView;
	private final ArrayList<TabImpl> mTabs = new ArrayList<TabImpl>();
	private boolean mHasEmbeddedTabs;
	private TabImpl mSelectedTab;
	private boolean mHiddenByApp = false;
	private Context mThemedContext;
	private ViewGroup mTopVisibilityView;
	private Animator mCurrentShowAnim;
    private static final String TAG = "Cyee_WidgetDemoL.CyeeActionBarImpl";

	ActionModeImpl mActionMode;
	ActionMode mDeferredDestroyActionMode;
	ActionMode.Callback mDeferredModeDestroyCallback;

	private int mCurWindowVisibility = View.VISIBLE;

	private boolean mHiddenBySystem;

	private CyeeActionBarOverlayLayout mOverlayLayout;

	private CyeeActionBarContextView mContextView;

	private int mContextDisplayMode;
	private boolean mDisplayHomeAsUpSet;
	private boolean mShowingForMode;
	private boolean mNowShowing = true;
	private boolean mShowHideAnimationEnabled;
	private boolean mLastMenuVisibility;
	private final ArrayList<OnMenuVisibilityListener> mMenuVisibilityListeners = new ArrayList<OnMenuVisibilityListener>();
	private Dialog mDialog;
	
	private boolean mActionBarOverlay;
	
	// Gionee <lihq> <2014-6-25> modify for CR00873172 begin
	//private MenuBuilder mMenu;
	// Gionee <lihq> <2014-6-25> modify for CR00873172 end
	private final boolean mControlCreate = true ;
	private ViewGroup mActionbarContainerView = null;
	
	
	private static boolean checkShowingFlags(boolean hiddenByApp, boolean hiddenBySystem,
			boolean showingForMode) {
		if (showingForMode) {
			return true;
		} else return !hiddenByApp && !hiddenBySystem;
	}
	
	final AnimatorListener mHideListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			if (mContentView != null) {
				mContentView.setTranslationY(0);
				mTopVisibilityView.setTranslationY(0);
			}
//            if (mSplitView != null && mContextDisplayMode == CONTEXT_DISPLAY_SPLIT) {
//                mSplitView.setVisibility(View.GONE);
//            }
			mTopVisibilityView.setVisibility(View.GONE);
			mContainerView.setTransitioning(false);
			mCurrentShowAnim = null;
			completeDeferredDestroyActionMode();
			if (mOverlayLayout != null) {
				mOverlayLayout.requestFitSystemWindows();
			}
		}
	};

	final AnimatorListener mShowListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			mCurrentShowAnim = null;
			mTopVisibilityView.requestLayout();
		}
	};

    public CyeeActionBarImpl(AppCompatActivity activity) {
        this(activity, null);
    }

    public CyeeActionBarImpl(AppCompatActivity activity, View actionbarContainerView) {
        mActivity = activity;
        // Window window = activity.getWindow();
        // View decor = window.getDecorView();

        View decor = ((CyeeActivity) activity).getViewWithCyeeActionBar();
        mActionBarOverlay = mActivity.getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        mActionbarContainerView = (ViewGroup) actionbarContainerView;

        init(decor);
        if (!mActionBarOverlay) {
            mContentView = decor.findViewById(com.cyee.internal.R.id.cyee_content);
        }
        // init(activity);
        // Gionee <lihq> <2014-6-25> modify for CR00873172 begin
        // mMenu = new
        // MenuBuilder(activity).setDefaultShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        // Gionee <lihq> <2014-6-25> modify for CR00873172 end
    }

	public CyeeActionBarImpl(Dialog dialog) {
		mDialog = dialog;
		init(dialog.getWindow().getDecorView());
	}

	// Gionee <lihq> <2014-6-25> modify for CR01316537 begin
	
	public void startOptionsMenu() {
	    /*
		if(mControlCreate) {
			mMenu.clear();
			mActivity.onCreateOptionsMenu(mMenu);
			mControlCreate = false ;	
		}
		mActivity.onPrepareOptionsMenu(mMenu);
		*/
	    ((CyeeActivity)mActivity).startOptionsMenu();
	}

	// Gionee <lihq> <2014-3-3> add for CR01087505 begin
	public void invalidateOptionsMenu() {
	    /*
        mMenu.clear();
        mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, mMenu);
        mActivity.onPrepareOptionsMenu(mMenu);
        */
	    ((CyeeActivity)mActivity).invalOptionsMenu();
    }
	// Gionee <lihq> <2014-3-3> add for CR01087505 end
	// Gionee <lihq> <2014-6-25> modify for CR01316537 end
	@Override
	public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
		mMenuVisibilityListeners.add(listener);
	}

	@Override
	public void addTab(Tab tab) {
		addTab(tab, mTabs.isEmpty());
	}

	@Override
	public void addTab(Tab tab, boolean setSelected) {
		ensureTabsExist();
		mTabContainerView.addTab(tab, setSelected);
		configureTab(tab, mTabs.size());
		if (setSelected) {
			selectTab(tab);
		}
	}

	@Override
	public void addTab(Tab tab, int position) {
		addTab(tab, position, mTabs.isEmpty());
	}

	@Override
	public void addTab(Tab tab, int position, boolean setSelected) {
		ensureTabsExist();
		mTabContainerView.addTab(tab, position, setSelected);
		configureTab(tab, position);
		if (setSelected) {
			selectTab(tab);
		}
	}

	void animateToMode(boolean toActionMode) {
		if (toActionMode) {
			showForActionMode();
		} else {
			hideForActionMode();
		}

//		mActionView.animateToVisibility(toActionMode ? View.GONE : View.VISIBLE);
		mActionView.setVisibility(toActionMode ? View.INVISIBLE : View.VISIBLE);
		mContextView.animateToVisibility(toActionMode ? View.VISIBLE : View.GONE);
		// Gionee <weidong> <2017-08-25> modify for 188539 begin
		if (mTabContainerView != null) {
		 // Gionee <weidong> <2017-08-25> modify for 188539 end
//		if (mTabContainerView != null && !mActionView.hasEmbeddedTabs() && mActionView.isCollapsed()) {
//			mTabContainerView.animateToVisibility(toActionMode ? View.GONE : View.VISIBLE);
		    mTabContainerView.animateToVisibility(toActionMode ? View.INVISIBLE : View.VISIBLE);
		}
	}

	private void cleanupTabs() {
		if (mSelectedTab != null) {
			selectTab(null);
		}
		mTabs.clear();
		if (mTabContainerView != null) {
			mTabContainerView.removeAllTabs();
		}
		mSavedTabPosition = INVALID_POSITION;
	}

	void completeDeferredDestroyActionMode() {
		if (mDeferredModeDestroyCallback != null) {
			mDeferredModeDestroyCallback.onDestroyActionMode(mDeferredDestroyActionMode);
			mDeferredDestroyActionMode = null;
			mDeferredModeDestroyCallback = null;
		}
	}

	private void configureTab(Tab tab, int position) {
		final TabImpl tabi = (TabImpl) tab;
		final CyeeActionBar.TabListener callback = tabi.getCallback();

		if (callback == null) {
			throw new IllegalStateException("Action Bar Tab must have a Callback");
		}

		tabi.setPosition(position);
		mTabs.add(position, tabi);

		final int count = mTabs.size();
		for (int i = position + 1; i < count; i++) {
			mTabs.get(i).setPosition(i);
		}
	}

	public void dispatchMenuVisibilityChanged(boolean isVisible) {
		if (isVisible == mLastMenuVisibility) {
			return;
		}
		mLastMenuVisibility = isVisible;

		final int count = mMenuVisibilityListeners.size();
		for (int i = 0; i < count; i++) {
			mMenuVisibilityListeners.get(i).onMenuVisibilityChanged(isVisible);
		}
	}

	public void doHide(boolean fromSystem) {
		if (mCurrentShowAnim != null) {
			mCurrentShowAnim.end();
		}

		if (mCurWindowVisibility == View.VISIBLE && (mShowHideAnimationEnabled || fromSystem)) {
			mTopVisibilityView.setAlpha(1);
			mContainerView.setTransitioning(true);
			AnimatorSet anim = new AnimatorSet();
			float endingY = -mTopVisibilityView.getHeight();
			if (fromSystem) {
				int[] topLeft = {0, 0};
				mTopVisibilityView.getLocationInWindow(topLeft);
				endingY -= topLeft[1];
			}
			AnimatorSet.Builder b = anim.play(ObjectAnimator.ofFloat(mTopVisibilityView, "translationY",
					endingY));
			if (mContentView != null) {
				b.with(ObjectAnimator.ofFloat(mContentView, "translationY", 0, endingY));
			}
//			if (mSplitView != null && mSplitView.getVisibility() == View.VISIBLE) {
//				mSplitView.setAlpha(1);
//				b.with(ObjectAnimator.ofFloat(mSplitView, "translationY", mSplitView.getHeight()));
//			}
			anim.setInterpolator(AnimationUtils.loadInterpolator(mContext,
			        android.R.interpolator.accelerate_cubic));
			
			anim.setDuration(250);
			anim.addListener(mHideListener);
			mCurrentShowAnim = anim;
			anim.start();
		} else {
			mHideListener.onAnimationEnd(null);
		}
	}

	public void doShow(boolean fromSystem) {
		if (mCurrentShowAnim != null) {
			mCurrentShowAnim.end();
		}
		mTopVisibilityView.setVisibility(View.VISIBLE);

		if (mCurWindowVisibility == View.VISIBLE && (mShowHideAnimationEnabled || fromSystem)) {
			mTopVisibilityView.setTranslationY(0); // because we're about to ask its window loc
			float startingY = -mTopVisibilityView.getHeight();
			if (fromSystem) {
				int[] topLeft = {0, 0};
				mTopVisibilityView.getLocationInWindow(topLeft);
				startingY -= topLeft[1];
			}
			mTopVisibilityView.setTranslationY(startingY);
			AnimatorSet anim = new AnimatorSet();
			AnimatorSet.Builder b = anim.play(ObjectAnimator.ofFloat(mTopVisibilityView, "translationY", 0));
			if (mContentView != null) {
				b.with(ObjectAnimator.ofFloat(mContentView, "translationY", startingY, 0));
			}
//			if (mSplitView != null && mContextDisplayMode == CONTEXT_DISPLAY_SPLIT) {
//				mSplitView.setTranslationY(mSplitView.getHeight());
//				mSplitView.setVisibility(View.VISIBLE);
//				b.with(ObjectAnimator.ofFloat(mSplitView, "translationY", 0));
//			}
			anim.setInterpolator(AnimationUtils.loadInterpolator(mContext,
			        android.R.interpolator.decelerate_cubic));
			anim.setDuration(250);
			// If this is being shown from the system, add a small delay.
			// This is because we will also be animating in the status bar,
			// and these two elements can't be done in lock-step. So we give
			// a little time for the status bar to start its animation before
			// the action bar animates. (This corresponds to the corresponding
			// case when hiding, where the status bar has a small delay before
			// starting.)
			anim.addListener(mShowListener);
			mCurrentShowAnim = anim;
			anim.start();
		} else {
			mTopVisibilityView.setAlpha(1);
			mTopVisibilityView.setTranslationY(0);
			if (mContentView != null) {
				mContentView.setTranslationY(0);
			}
//			if (mSplitView != null && mContextDisplayMode == CONTEXT_DISPLAY_SPLIT) {
//				mSplitView.setAlpha(1);
//				mSplitView.setTranslationY(0);
//				mSplitView.setVisibility(View.VISIBLE);
//			}
			mShowListener.onAnimationEnd(null);
		}
		if (mOverlayLayout != null) {
			mOverlayLayout.requestFitSystemWindows();
		}
	}

	private void ensureTabsExist() {
		if (mTabContainerView != null) {
			return;
		}

		CyeeTabContainerView tabContainerView = new CyeeTabContainerView(mContext);

		if (mHasEmbeddedTabs) {
			tabContainerView.setVisibility(View.VISIBLE);
			mActionView.setEmbeddedTabView(tabContainerView);
		} else {
			if (getNavigationMode() == NAVIGATION_MODE_TABS) {
				tabContainerView.setVisibility(View.VISIBLE);
				if (mOverlayLayout != null) {
					mOverlayLayout.requestFitSystemWindows();
				}
			} else {
				tabContainerView.setVisibility(View.GONE);
			}
			mContainerView.setTabContainer(tabContainerView);
		}
		mTabContainerView = tabContainerView;
		mTabContainerView.setActionBarOverlay(mActionBarOverlay);
		if (ChameleonColorManager.isNeedChangeColor(mContext) && !mActionBarOverlay) {
			mTabContainerView.changeColors();
		}
	}

	@Override
	public View getCustomView() {
		return mActionView.getCustomNavigationView();
	}

	@Override
	public int getDisplayOptions() {
		return mActionView.getDisplayOptions();
	}

	@Override
	public int getHeight() {
		return mContainerView.getHeight();
	}

	@Override
	public int getNavigationItemCount() {
		switch (mActionView.getNavigationMode()) {
			case NAVIGATION_MODE_TABS:
				return mTabs.size();
			case NAVIGATION_MODE_LIST:
				SpinnerAdapter adapter = mActionView.getDropdownAdapter();
				return adapter != null ? adapter.getCount() : 0;
			default:
				return 0;
		}
	}

	@Override
	public int getNavigationMode() {
		return mActionView.getNavigationMode();
	}

	@Override
	public int getSelectedNavigationIndex() {
		switch (mActionView.getNavigationMode()) {
			case NAVIGATION_MODE_TABS:
				return mSelectedTab != null ? mSelectedTab.getPosition() : -1;
			case NAVIGATION_MODE_LIST:
				return mActionView.getDropdownSelectedPosition();
			default:
				return -1;
		}
	}

	@Override
	public Tab getSelectedTab() {
		return mSelectedTab;
	}

	@Override
	public CharSequence getSubtitle() {
		return mActionView.getSubtitle();
	}

	@Override
	public Tab getTabAt(int index) {
		return mTabs.get(index);
	}

	@Override
	public int getTabCount() {
		return mTabs.size();
	}

	@Override
	public Context getThemedContext() {
		if (mThemedContext == null) {
			TypedValue outValue = new TypedValue();
			Resources.Theme currentTheme = mContext.getTheme();
			currentTheme.resolveAttribute(android.R.attr.actionBarWidgetTheme, outValue, true);
			final int targetThemeRes = outValue.resourceId;

			if (targetThemeRes != 0 && mContext.getThemeResId() != targetThemeRes) {
				mThemedContext = new ContextThemeWrapper(mContext, targetThemeRes);
			} else {
				mThemedContext = mContext;
			}
		}
		return mThemedContext;
	}

	@Override
	public CharSequence getTitle() {
		return mActionView.getTitle();
	}

	public boolean hasNonEmbeddedTabs() {
		return !mHasEmbeddedTabs && getNavigationMode() == NAVIGATION_MODE_TABS;
	}

	@Override
	public void hide() {
		if (!mHiddenByApp) {
			mHiddenByApp = true;
			updateVisibility(false);
		}
//		if (mContainerView != null) {
//			mContainerView.setVisibility(View.GONE);
//		}
	}

	private void hideForActionMode() {
		if (mShowingForMode) {
			mShowingForMode = false;
			if (mOverlayLayout != null) {
				mOverlayLayout.setShowingForActionMode(false);
			}
			updateVisibility(false);
		}
	}

	public void hideForSystem() {
		if (!mHiddenBySystem) {
			mHiddenBySystem = true;
			updateVisibility(true);
		}
	}

//	private void init(Activity activity) {
//		mActivity = activity;
//		mContext = mActivity.getWindow().getDecorView().getContext();
//		mContainerView = (CyeeActionBarContainer) mActivity
//				.findViewById(com.cyee.internal.R.id.cyee_action_bar_container);
//		mActionView = (CyeeActionBarView) mActivity.findViewById(com.cyee.internal.R.id.cyee_action_bar);
////		mSpliteView = (GnActionBarContainer) mActivity.findViewById(com.cyee.internal.R.id(activity, "split_action_bar"));
//
//		if (mContainerView == null || mActionView == null) {
//			throw new RuntimeException(
//					"can not init CyeeActionBar, getCyeeActionBar() should be after setContentView()");
//		}
//
//		
//		// This was initially read from the action bar style
//        final int current = mActionView.getDisplayOptions();
//        final boolean homeAsUp = (current & DISPLAY_HOME_AS_UP) != 0;
//        if (homeAsUp) {
//            mDisplayHomeAsUpSet = true;
//        }
//
//        ActionBarPolicy abp = ActionBarPolicy.get(mContext);
//        setHomeButtonEnabled(abp.enableHomeButtonByDefault() || homeAsUp);
//        
//		mHasEmbeddedTabs = abp.hasEmbeddedTabs();
////		ensureTabsExist();
//		setHasEmbeddedTabs(mHasEmbeddedTabs);
//	}

    private void init(View decor) {
        mContext = decor.getContext();
        mOverlayLayout = (CyeeActionBarOverlayLayout) decor
                .findViewById(com.cyee.internal.R.id.cyee_action_bar_overlay_layout);
        if (mOverlayLayout != null) {
            mOverlayLayout.setActionBar(this);
        }
        // Gionee <gaoj> <2013-9-6> modify for CR00889318 begin
        ViewStub viewStub = (ViewStub) decor
                .findViewById(com.cyee.internal.R.id.cyee_actionbar_container_stub);
        
        if (viewStub != null) {
            mActionbarContainerView = (ViewGroup) viewStub.inflate();
            mActionView = (CyeeActionBarView) mActionbarContainerView
                    .findViewById(com.cyee.internal.R.id.cyee_action_bar);
            mContextView = (CyeeActionBarContextView) mActionbarContainerView
                    .findViewById(com.cyee.internal.R.id.cyee_action_context_bar);
            mContainerView = (CyeeActionBarContainer) mActionbarContainerView
                    .findViewById(com.cyee.internal.R.id.cyee_action_bar_container);
        } else if (null != mActionbarContainerView) {
            if (null == mActionView) {
                mActionView = (CyeeActionBarView) mActionbarContainerView
                        .findViewById(com.cyee.internal.R.id.cyee_action_bar);
            }
            if (null == mContextView) {
                mContextView = (CyeeActionBarContextView) mActionbarContainerView
                        .findViewById(com.cyee.internal.R.id.cyee_action_context_bar);
            }
            if (null == mContainerView) {
                mContainerView = (CyeeActionBarContainer) mActionbarContainerView
                        .findViewById(com.cyee.internal.R.id.cyee_action_bar_container);
            }
        }
        // Gionee <gaoj> <2013-9-6> modify for CR00889318 end
        // mTopVisibilityView = (ViewGroup)
        // decor.findViewById(com.cyee.internal.R.id(mContext,
        // "cyee_top_action_bar"));
        if (mTopVisibilityView == null) {
            mTopVisibilityView = mContainerView;
        }
        // mSplitView = (ActionBarContainer)
        // decor.findViewById(com.cyee.internal.R.id(mContext,
        // "cyee_split_action_bar"));

        if (mActionView == null || mContextView == null
                || mContainerView == null) {
            throw new IllegalStateException(getClass().getSimpleName()
                    + " can only be used "
                    + "with a compatible window decor layout");
        }
        mActionView.setActivity((CyeeActivity) mActivity);
        mActionView.setContextView(mContextView);
        mContextDisplayMode = mActionView.isSplitActionBar() ? CONTEXT_DISPLAY_SPLIT
                : CONTEXT_DISPLAY_NORMAL;

        // This was initially read from the action bar style
        final int current = mActionView.getDisplayOptions();
        final boolean homeAsUp = (current & DISPLAY_HOME_AS_UP) != 0;
        if (homeAsUp) {
            mDisplayHomeAsUpSet = true;
        }

        ActionBarPolicy abp = ActionBarPolicy.get(mContext);
        setHomeButtonEnabled(abp.enableHomeButtonByDefault() || homeAsUp);
        setHasEmbeddedTabs(abp.hasEmbeddedTabs());
		//Chenyee <Cyee_Widget> hushengsong 2018-08-11 modify for p style begin
		setElevation(0);
		//Chenyee <Cyee_Widget> hushengsong 2018-08-11 modify for p style end
        if (!mActionBarOverlay) {
            if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
                Resources iCyeeRes = mContext.getResources();
                Drawable bgDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_bg);
                mActionbarContainerView.setElevation(iCyeeRes.getDimensionPixelOffset(com.cyee.internal.R.dimen.cyee_global_theme_actionbar_elevation));
                mActionbarContainerView.setBackground(bgDrawable);
                setBackgroundDrawable(null);
                setStackedBackgroundDrawable(null);
                setSplitBackgroundDrawable(null);
                mActionView.changeTheme();
                if(ChameleonColorManager.isNeedChangeColor(mContext)) {
                    mActionView.changeTitleViewColors();
                }                
            } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                int primaryBackgroundColor = ChameleonColorManager
                        .getAppbarColor_A1();
                ColorDrawable backgroundDrawable = new ColorDrawable(
                        primaryBackgroundColor);
                setBackgroundDrawable(backgroundDrawable);
                setStackedBackgroundDrawable(backgroundDrawable);
                setSplitBackgroundDrawable(backgroundDrawable);
                mActivity.getWindow().setStatusBarColor(primaryBackgroundColor);
                mActionView.changeColors();
            }
        }
    }

	@Override
	public boolean isShowing() {
		return mNowShowing;
	}

	public boolean isSystemShowing() {
		return !mHiddenBySystem;
	}

	@Override
	public Tab newTab() {
		return new TabImpl();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		setHasEmbeddedTabs(CyeeActionBarPolicy.get(mContext).hasEmbeddedTabs());
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		if (mTabContainerView != null) {
			mTabContainerView.onPageScrolled(arg0, arg1, arg2);
		}
	}

	@Override
	public void onScrollToEnd(View v, MotionEvent event) {
		//shaozj comented begin
		//if (mTabContainerView != null) {
		//	mTabContainerView.onScrollToEnd(v, event);
		//}
		//shaozj comented end
	}

	@Override
	public void removeAllTabs() {
		cleanupTabs();
	}

	@Override
	public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
		mMenuVisibilityListeners.remove(listener);
	}

	@Override
	public void removeTab(Tab tab) {
		removeTabAt(tab.getPosition());
	}

	@Override
	public void removeTabAt(int position) {
		if (mTabContainerView == null) {
			return;
		}

		int selectedTabPosition = mSelectedTab != null ? mSelectedTab.getPosition() : mSavedTabPosition;
		mTabContainerView.removeTabAt(position);
		TabImpl removedTab = mTabs.remove(position);
		if (removedTab != null) {
			removedTab.setPosition(-1);
		}

		final int newTabCount = mTabs.size();
		for (int i = position; i < newTabCount; i++) {
			mTabs.get(i).setPosition(i);
		}

		if (selectedTabPosition == position) {
			selectTab(mTabs.isEmpty() ? null : mTabs.get(Math.max(0, position - 1)));
		}
	}

	@Override
	public void selectTab(Tab tab) {
		if (getNavigationMode() != NAVIGATION_MODE_TABS) {
			mSavedTabPosition = tab != null ? tab.getPosition() : INVALID_POSITION;
			return;
		}

		final FragmentTransaction trans = mActivity.getSupportFragmentManager().beginTransaction()
				.disallowAddToBackStack();

		if (mSelectedTab == tab) {
			if (mSelectedTab != null) {
				mSelectedTab.getCallback().onTabReselected(mSelectedTab, trans);
				//shaozj mTabContainerView.animateToTab(tab.getPosition());
				mTabContainerView.setTabSelected(tab.getPosition());
			}
		} else {
			mTabContainerView.setTabSelected(tab != null ? tab.getPosition() : Tab.INVALID_POSITION);
			if (mSelectedTab != null) {
				mSelectedTab.getCallback().onTabUnselected(mSelectedTab, trans);
			}
			mSelectedTab = (TabImpl) tab;
			if (mSelectedTab != null) {
				mSelectedTab.getCallback().onTabSelected(mSelectedTab, trans);
			}
		}

		if (!trans.isEmpty()) {
			trans.commit();
		}
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
	    mContainerView.setPrimaryBackground(d);
	}

	@Override
	public void setCustomView(int resId) {
		setCustomView(LayoutInflater.from(getThemedContext()).inflate(resId, mActionView, false));
	}

	@Override
	public void setCustomView(View view) {
		mActionView.setCustomNavigationView(view);
	}

	@Override
	public void setCustomView(View view, LayoutParams layoutParams) {
		view.setLayoutParams(layoutParams);
		mActionView.setCustomNavigationView(view);
	}

	public void setDefaultDisplayHomeAsUpEnabled(boolean enable) {
		if (!mDisplayHomeAsUpSet) {
			setDisplayHomeAsUpEnabled(enable);
		}
	}

	@Override
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		setDisplayOptions(showHomeAsUp ? DISPLAY_HOME_AS_UP : 0, DISPLAY_HOME_AS_UP);
	}

	@Override
	public void setDisplayOptions(int options) {
		if ((options & DISPLAY_HOME_AS_UP) != 0) {
			mDisplayHomeAsUpSet = true;
		}
		mActionView.setDisplayOptions(options);
	    
		setDisplayShowExtraViewEnabled((options & DISPLAY_SHOW_EXTRA_VIEW) != 0);
	}

	@Override
	public void setDisplayOptions(int options, int mask) {
		final int current = mActionView.getDisplayOptions();
		if ((mask & DISPLAY_HOME_AS_UP) != 0) {
			mDisplayHomeAsUpSet = true;
		}
		mActionView.setDisplayOptions((options & mask) | (current & ~mask));
		
		setDisplayShowExtraViewEnabled((options & DISPLAY_SHOW_EXTRA_VIEW) != 0);
	}

	@Override
	public void setDisplayShowCustomEnabled(boolean showCustom) {
		setDisplayOptions(showCustom ? DISPLAY_SHOW_CUSTOM : 0, DISPLAY_SHOW_CUSTOM);
	}

	@Override
	public void setDisplayShowHomeEnabled(boolean showHome) {
		setDisplayOptions(showHome ? DISPLAY_SHOW_HOME : 0, DISPLAY_SHOW_HOME);
	}

	@Override
	public void setDisplayShowTitleEnabled(boolean showTitle) {
		setDisplayOptions(showTitle ? DISPLAY_SHOW_TITLE : 0, DISPLAY_SHOW_TITLE);
	}

	@Override
	public void setDisplayUseLogoEnabled(boolean useLogo) {
		setDisplayOptions(useLogo ? DISPLAY_USE_LOGO : 0, DISPLAY_USE_LOGO);
	}

    private void setHasEmbeddedTabs(boolean hasEmbeddedTabs) {
        mHasEmbeddedTabs = hasEmbeddedTabs;
        // Switch tab layout configuration if needed
        // ensureTabsExist();
        if (!mHasEmbeddedTabs) {
            mActionView.setEmbeddedTabView(null);
            mContainerView.setTabContainer(mTabContainerView);
        } else {
            mContainerView.setTabContainer(null);
            mActionView.setEmbeddedTabView(mTabContainerView);
        }
        final boolean isInTabMode = getNavigationMode() == NAVIGATION_MODE_TABS;
        if (mTabContainerView != null) {
            if (isInTabMode) {
                if (null == mActionMode) {
                    mTabContainerView.setVisibility(View.VISIBLE);
                } else {
                    mTabContainerView.setVisibility(View.INVISIBLE);
                }
                if (mOverlayLayout != null) {
                    mOverlayLayout.requestFitSystemWindows();
                }
            } else {
                mTabContainerView.setVisibility(View.GONE);
            }
        }
        mActionView.setCollapsable(!mHasEmbeddedTabs && isInTabMode);
    }
	
	@Override
	public void setHomeButtonEnabled(boolean enable) {
		mActionView.setHomeButtonEnabled(enable);
	}

	@Override
	public void setIcon(Drawable icon) {
		mActionView.setIcon(icon);
	}

	@Override
	public void setIcon(int resId) {
		mActionView.setIcon(resId);
	}

	@Override
    public void setIndicatorBackgroundColor(int color) {
        ensureTabsExist();
        mTabContainerView.setIndicatorBackgroundColor(color);
    }

	@Override
	public void setListNavigationCallbacks(SpinnerAdapter adapter, OnNavigationListener callback) {
		mActionView.setDropdownAdapter(adapter);
		mActionView.setCallback(callback);
	}

	@Override
	public void setLogo(Drawable logo) {
		mActionView.setLogo(logo);
	}

	@Override
	public void setLogo(int resId) {
		mActionView.setLogo(resId);
	}

	@Override
	public void setNavigationMode(int mode) {
		final int oldMode = mActionView.getNavigationMode();
		switch (oldMode) {
			case NAVIGATION_MODE_TABS:
				mSavedTabPosition = getSelectedNavigationIndex();
				selectTab(null);
				mTabContainerView.setVisibility(View.GONE);
				break;
		}
		if (oldMode != mode && !mHasEmbeddedTabs) {
			if (mOverlayLayout != null) {
				mOverlayLayout.requestFitSystemWindows();
			}
		}
//		mActionView.setVisibility(View.VISIBLE);
		mActionView.setNavigationMode(mode);
		switch (mode) {
			case NAVIGATION_MODE_TABS:
				ensureTabsExist();
				mTabContainerView.setVisibility(View.VISIBLE);
				if (mSavedTabPosition != INVALID_POSITION) {
					setSelectedNavigationItem(mSavedTabPosition);
					mSavedTabPosition = INVALID_POSITION;
				}
				break;
		}
		mActionView.setCollapsable(mode == NAVIGATION_MODE_TABS && !mHasEmbeddedTabs);
	}

	@Override
	public void setSelectedNavigationItem(int position) {
		switch (mActionView.getNavigationMode()) {
			case NAVIGATION_MODE_TABS:
				selectTab(mTabs.get(position));
				break;
			case NAVIGATION_MODE_LIST:
				mActionView.setDropdownSelectedPosition(position);
				break;
			default:
				throw new IllegalStateException(
						"setSelectedNavigationIndex not valid for current navigation mode");
		}
	}

	public void setShowHideAnimationEnabled(boolean enabled) {
		mShowHideAnimationEnabled = enabled;
		if (!enabled && mCurrentShowAnim != null) {
			mCurrentShowAnim.end();
		}
	}

	public void setSplitBackgroundDrawable(Drawable d) {
//		if (mSplitView != null) {
//			mSplitView.setSplitBackground(d);
//		}
	}

	public void setStackedBackgroundDrawable(Drawable d) {
		mContainerView.setStackedBackground(d);
	}

	@Override
	public void setSubtitle(CharSequence subtitle) {
	    mActionView.setSubtitle(subtitle);
	}

	@Override
	public void setSubtitle(int resId) {
		setSubtitle(mContext.getString(resId));
	}

	public void setTitle(CharSequence title) {
		mActionView.setTitle(title);
	}

	@Override
	public void setTitle(int resId) {
		setTitle(mContext.getString(resId));
	}

	public void setWindowVisibility(int visibility) {
		mCurWindowVisibility = visibility;
	}

	@Override
	public void show() {
		if (mHiddenByApp) {
			mHiddenByApp = false;
			updateVisibility(false);
		}
//		if (mContainerView != null) {
//			mContainerView.setVisibility(View.VISIBLE);
//		}
	}

	private void showForActionMode() {
		if (!mShowingForMode) {
			mShowingForMode = true;
			if (mOverlayLayout != null) {
				mOverlayLayout.setShowingForActionMode(true);
			}
			updateVisibility(false);
		}
	}

	public void showForSystem() {
		if (mHiddenBySystem) {
			mHiddenBySystem = false;
			updateVisibility(true);
		}
	}

	public ActionMode startActionMode(ActionMode.Callback callback) {
		if (mActionMode != null) {
			mActionMode.finish();
		}

		mContextView.killMode();
		ActionModeImpl mode = new ActionModeImpl(callback);
		if (mode.dispatchOnCreate()) {
			mode.invalidate();
			mContextView.initForMode(mode);
			animateToMode(true);
//			if (mSplitView != null && mContextDisplayMode == CONTEXT_DISPLAY_SPLIT) {
//				// TODO animate this
//				if (mSplitView.getVisibility() != View.VISIBLE) {
//					mSplitView.setVisibility(View.VISIBLE);
//					if (mOverlayLayout != null) {
//						mOverlayLayout.requestFitSystemWindows();
//					}
//				}
//			}
			mContextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
			mActionMode = mode;
			// Gionee <fenglp> <2013-07-25> modify for CR00812456 begin
			Menu actionModeMenu = mActionMode.getMenu();
            if (actionModeMenu != null && actionModeMenu.size() > 0) {
            	((CyeeActivity) mActivity).setOptionsMenuUnExpand();
            	// gionee maxw modify begin
                ((CyeeActivity) mActivity).parserMenuInfo(actionModeMenu);
            	// gionee maxw modify end
                ((CyeeActivity) mActivity).setOptionsMenuHideMode(false);
            }
			// Gionee <fenglp> <2013-07-25> modify for CR00812456 end
            //shaozj add begin
            CyeeMagicBar cyeeMagicBar = ((CyeeActivity) mActivity).getCyeeMagicBar();
            if(null != cyeeMagicBar){
            	cyeeMagicBar.setMagicBarVisibilityWithoutAnim(View.VISIBLE);
            }
            //shaozj add end

			return mode;
		}
		return null;
	}

	private void updateVisibility(boolean fromSystem) {
		// Based on the current state, should we be hidden or shown?
		final boolean shown = checkShowingFlags(mHiddenByApp, mHiddenBySystem, mShowingForMode);

		if (shown) {
			if (!mNowShowing) {
				mNowShowing = true;
				doShow(fromSystem);
			}
		} else {
			if (mNowShowing) {
				mNowShowing = false;
				doHide(fromSystem);
			}
		}
	}

	public void setActivityContent(View view) {
		mContainerView.setActivityContent(view);
	}

	@Override
	public void setExtraView(View view) {
		mContainerView.setExtraView(view);
	}

	@Override
	public void setOnExtraViewDragListener(OnExtraViewDragListener listener) {
		mContainerView.setOnExtraViewDragListener(listener);
	}

	public class ActionModeImpl extends ActionMode implements MenuBuilder.Callback {
		private ActionMode.Callback mCallback;
		private final MenuBuilder mMenu;
		private WeakReference<View> mCustomView;

		public ActionModeImpl(ActionMode.Callback callback) {
			mCallback = callback;
			mMenu = new MenuBuilder(getThemedContext())
					.setDefaultShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			mMenu.setCallback(this);
		}

		@Override
		public MenuInflater getMenuInflater() {
			return new MenuInflater(getThemedContext());
		}

		@Override
		public Menu getMenu() {
			return mMenu;
		}

		@Override
		public void finish() {
			if (mActionMode != this) {
				// Not the active action mode - no-op
				return;
			}

			// If this change in state is going to cause the action bar
			// to be hidden, defer the onDestroy callback until the animation
			// is finished and associated relayout is about to happen. This lets
			// apps better anticipate visibility and layout behavior.
			if (!checkShowingFlags(mHiddenByApp, mHiddenBySystem, false)) {
				// With the current state but the action bar hidden, our
				// overall showing state is going to be false.
				mDeferredDestroyActionMode = this;
				mDeferredModeDestroyCallback = mCallback;
			} else {
				mCallback.onDestroyActionMode(this);
			}
            CyeeMagicBar cyeeMagicBar = ((CyeeActivity) mActivity)
                    .getCyeeMagicBar();
            if (null != cyeeMagicBar) {
                cyeeMagicBar.setExpand(false);
            }
			// Gionee <fenglp> <2013-07-25> modify for CR00812456 begin
			Menu optionMenu = ((CyeeActivity) mActivity).getOptionMenu();
			if (optionMenu != null) {
			    ((CyeeActivity) mActivity).setOptionsMenuHideMode(false);
			    if(((CyeeActivity) mActivity).isUseOriginalActionBar()) {
			    	setCustomMenu(optionMenu);
			    	((CyeeActivity) mActivity).parserMenuInfo(null);
			    } else {
			    	((CyeeActivity) mActivity).parserMenuInfo(optionMenu);
			    }
			} else {
			    ((CyeeActivity) mActivity).setOptionsMenuHideMode(true);
			    cyeeMagicBar = ((CyeeActivity) mActivity).getCyeeMagicBar();
	            if(null != cyeeMagicBar && !((CyeeActivity) mActivity).isUseOriginalActionBar()){
	                cyeeMagicBar.setMagicBarVisibilityWithoutAnim(View.GONE);
	            }
			}
			// Gionee <fenglp> <2013-07-25> modify for CR00812456 end
			mCallback = null;
	         //shaozj add begin
            
            //shaozj add end
			animateToMode(false);

			// Clear out the context mode views after the animation finishes
			mContextView.closeMode();
			mActionView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

			mActionMode = null;
		}

        @Override
        public void invalidate() {
            mMenu.stopDispatchingItemsChanged();
            try {
                if (null != mCallback) {
                    mCallback.onPrepareActionMode(this, mMenu);
                }
            } finally {
                mMenu.startDispatchingItemsChanged();
            }
        }

		public boolean dispatchOnCreate() {
			mMenu.stopDispatchingItemsChanged();
			try {
				return mCallback.onCreateActionMode(this, mMenu);
			} finally {
				mMenu.startDispatchingItemsChanged();
			}
		}

		@Override
		public void setCustomView(View view) {
			mContextView.setCustomView(view);
			mCustomView = new WeakReference<View>(view);
		}

		@Override
		public void setSubtitle(CharSequence subtitle) {
			mContextView.setSubtitle(subtitle);
		}

		@Override
		public void setTitle(CharSequence title) {
			mContextView.setTitle(title);
		}

		@Override
		public void setTitle(int resId) {
			setTitle(mContext.getResources().getString(resId));
		}

		@Override
		public void setSubtitle(int resId) {
			setSubtitle(mContext.getResources().getString(resId));
		}

		@Override
		public CharSequence getTitle() {
			return mContextView.getTitle();
		}

		@Override
		public CharSequence getSubtitle() {
			return mContextView.getSubtitle();
		}

		@Override
		public void setTitleOptionalHint(boolean titleOptional) {
			super.setTitleOptionalHint(titleOptional);
			mContextView.setTitleOptional(titleOptional);
		}

		@Override
		public boolean isTitleOptional() {
			return mContextView.isTitleOptional();
		}

		@Override
		public View getCustomView() {
			return mCustomView != null ? mCustomView.get() : null;
		}

		public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
			if (mCallback != null) {
				return mCallback.onActionItemClicked(this, item);
			} else {
				return false;
			}
		}

		public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
		}

		public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
			if (mCallback == null) {
				return false;
			}

			if (!subMenu.hasVisibleItems()) {
				return true;
			}

			new MenuPopupHelper(getThemedContext(), subMenu).show();
			return true;
		}

		public void onCloseSubMenu(SubMenuBuilder menu) {
		}

		public void onMenuModeChange(MenuBuilder menu) {
			if (mCallback == null) {
				return;
			}
			invalidate();
			mContextView.showOverflowMenu();
		}
		
		// Gionee <fenglp> <2013-07-29> add for CR00812456 begin
        public Callback getCallback() {
            return mCallback;
        }
        // Gionee <fenglp> <2013-07-29> add for CR00812456 end        
	}

    // Gionee <fenglp> <2013-07-26> add for CR00812456 begin	
	/**
	 * CyeeActionMode is shown  or hidden
	 * @return true shown, false hidden
	 */
    public boolean isActionModeShowing() {
        if (mContextView != null) {
            return mContextView.isActionModeShowing();
        }
        return false;
    }
	
	public boolean isActionModeHasMenu(){
	    if(null == mActionMode){
	        return false;
	    }
	    Menu actionModeMenu = mActionMode.getMenu();
        return actionModeMenu != null && actionModeMenu.size() > 0;
	}
    // Gionee <fenglp> <2013-07-26> add for CR00812456 end
	// Gionee <fenglp> <2013-07-29> add for CR00812456 begin
	public ActionMode getActionMode(){
	    return mActionMode;
	}
	// Gionee <fenglp> <2013-07-29> add for CR00812456 end

	// Gionee <fenglp> <2013-07-30> add for CR00812456 begin
	public void setOnBackClickListener(OnClickListener listener) {
	    mActionView.setOnBackClickListener(listener);
	}
    // Gionee <weidong> <2015-05-01> add for CR01473846 begin
    public void setOnActionBarDoubleClickListener(OnClickListener listener) {
        mActionView.setOnActionBarDoubleClickListener(listener);
    }
    // Gionee <weidong> <2015-05-01> add for CR01473846 end
    public void updateActionMode() {
        if (mActionMode == null) {
            return;
        }
        Menu actionModeMenu = mActionMode.getMenu();
        if (actionModeMenu != null && actionModeMenu.size() > 0) {
            ((CyeeActivity) mActivity).setOptionsMenuUnExpand();
            ((CyeeActivity) mActivity).parserMenuInfo(actionModeMenu);
            ((CyeeActivity) mActivity).setOptionsMenuHideMode(false);
        }        
    }
	// Gionee <fenglp> <2013-07-30> add for CR00812456 end
    @Override
    public void setDisplayShowExtraViewEnabled(boolean showExtraView) {
        mContainerView.setDragEnable(showExtraView);
    }
    //shaozj add 2014-11-27 begin
    public void setCustomMenu(Menu menu){
        if (menu.size() > 0 && mActionView!=null) {
            mActionView.setMenu(menu);
        }
    }

    @Override
    public void changeColors() {
        
    }
    //shaozj add 2014-11-27 end
    // Gionee <weidong> <2016-05-04> add for CR01683201 begin
    @Override
    public void setElevation(float elevation) {
        mContainerView.setElevation(elevation);
    }
    // Gionee <weidong> <2016-05-04> add for CR01683201 end
}
