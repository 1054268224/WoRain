package com.cyee.internal.widget;

import cyee.app.CyeeActionBarImpl;
import cyee.widget.CyeeWidgetResource;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class CyeeActionBarOverlayLayout extends RelativeLayout {
    private int mActionBarHeight;
    private CyeeActionBarImpl mActionBar;
    private int mWindowVisibility = View.VISIBLE;
    private View mContent;
    private View mActionBarTop;
    private CyeeActionBarContainer mContainerView;
    private CyeeActionBarView mActionView;
    private View mActionBarBottom;
    private int mLastSystemUiVisibility;
    private final Rect mZeroRect = new Rect(0, 0, 0, 0);

    static final int[] mActionBarSizeAttr = new int [] {
    						android.R.attr.actionBarSize
    };

    public CyeeActionBarOverlayLayout(Context context) {
        super(context);
        init(context);
    }

    public CyeeActionBarOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(mActionBarSizeAttr);
        mActionBarHeight = ta.getDimensionPixelSize(0, 0);
        ta.recycle();
    }

    public void setActionBar(CyeeActionBarImpl impl) {
        mActionBar = impl;
        if (getWindowToken() != null) {
            // This is being initialized after being added to a window;
            // make sure to update all state now.
            mActionBar.setWindowVisibility(mWindowVisibility);
            if (mLastSystemUiVisibility != 0) {
                int newVis = mLastSystemUiVisibility;
                onWindowSystemUiVisibilityChanged(newVis);
                requestFitSystemWindows();
            }
        }
    }

    public void setShowingForActionMode(boolean showing) {
        if (showing) {
            // Here's a fun hack: if the status bar is currently being hidden,
            // and the application has asked for stable content insets, then
            // we will end up with the action mode action bar being shown
            // without the status bar, but moved below where the status bar
            // would be.  Not nice.  Trying to have this be positioned
            // correctly is not easy (basically we need yet *another* content
            // inset from the window manager to know where to put it), so
            // instead we will just temporarily force the status bar to be shown.
            if ((getWindowSystemUiVisibility() & (SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | SYSTEM_UI_FLAG_LAYOUT_STABLE))
                    == (SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE)) {
                setDisabledSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
            }
        } else {
            setDisabledSystemUiVisibility(0);
        }
    }

    @Override
    public void onWindowSystemUiVisibilityChanged(int visible) {
        super.onWindowSystemUiVisibilityChanged(visible);
        pullChildren();
        final int diff = mLastSystemUiVisibility ^ visible;
        mLastSystemUiVisibility = visible;
        final boolean barVisible = (visible&SYSTEM_UI_FLAG_FULLSCREEN) == 0;
        final boolean wasVisible = mActionBar == null || mActionBar.isSystemShowing();
        if (mActionBar != null) {
            if (barVisible) mActionBar.showForSystem();
            else mActionBar.hideForSystem();
        }
        if ((diff&SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0) {
            if (mActionBar != null) {
                requestFitSystemWindows();
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mWindowVisibility = visibility;
        if (mActionBar != null) {
            mActionBar.setWindowVisibility(visibility);
        }
    }

    private boolean applyInsets(View view, Rect insets, boolean left, boolean top,
            boolean bottom, boolean right) {
        if(null == view){
            return false;
        }
        boolean changed = false;
        //RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
        LayoutParams lp = (LayoutParams)view.getLayoutParams();
        if (left && lp.leftMargin != insets.left) {
            changed = true;
            lp.leftMargin = insets.left;
        }
        if (top && lp.topMargin != insets.top) {
            changed = true;
            lp.topMargin = insets.top;
        }
        if (right && lp.rightMargin != insets.right) {
            changed = true;
            lp.rightMargin = insets.right;
        }
        if (bottom && lp.bottomMargin != insets.bottom) {
            changed = true;
            lp.bottomMargin = insets.bottom;
        }
        return changed;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        pullChildren();

        final int vis = getWindowSystemUiVisibility();
        final boolean stable = (vis & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0;

        // The top and bottom action bars are always within the content area.
        boolean changed = applyInsets(mActionBarTop, insets, true, true, false, true);
        if (mActionBarBottom != null) {
            changed |= applyInsets(mActionBarBottom, insets, true, false, true, true);
        }

        // If the window has not requested system UI layout flags, we need to
        // make sure its content is not being covered by system UI...  though it
        // will still be covered by the action bar since they have requested it to
        // overlay.
        if ((vis & SYSTEM_UI_LAYOUT_FLAGS) == 0) {
            changed |= applyInsets(mContent, insets, true, true, true, true);
            // The insets are now consumed.
            insets.set(0, 0, 0, 0);
        } else {
            changed |= applyInsets(mContent, mZeroRect, true, true, true, true);
        }


        if (stable || mActionBarTop != null && mActionBarTop.getVisibility() == VISIBLE) {
            // The action bar creates additional insets for its content to use.
            insets.top += mActionBarHeight;
        }

        if (mActionBar != null && mActionBar.hasNonEmbeddedTabs()) {
            View tabs = mContainerView.getTabContainer();
            if (stable || (tabs != null && tabs.getVisibility() == VISIBLE)) {
                // If tabs are not embedded, adjust insets to account for them.
                insets.top += mActionBarHeight;
            }
        }

        if (mActionView != null && mActionView.isSplitActionBar()) {
            if (stable || (mActionBarBottom != null
                    && mActionBarBottom.getVisibility() == VISIBLE)) {
                // If action bar is split, adjust buttom insets for it.
                insets.bottom += mActionBarHeight;
            }
        }

        if (changed) {
            requestLayout();
        }

        return super.fitSystemWindows(insets);
    }

    void pullChildren() {
        if (mContent == null) {
            mContent = findViewById(com.cyee.internal.R.id.cyee_content);
            //shaozj modify begin
            //mActionBarTop = findViewById(R.id.cyee_top_action_bar);
            mActionBarTop = findViewById(com.cyee.internal.R.id.cyee_action_bar_container);
            mContainerView = (CyeeActionBarContainer)findViewById(
                    com.cyee.internal.R.id.cyee_action_bar_container);
            mActionView = (CyeeActionBarView) findViewById(com.cyee.internal.R.id.cyee_action_bar);
            //mActionBarBottom = findViewById(com.android.internal.R.id.split_action_bar);
            //mActionBarBottom = findViewById(com.cyee.internal.R.id.cyee_magic_bar);
            //shaozj modify end
        }
    }
}
