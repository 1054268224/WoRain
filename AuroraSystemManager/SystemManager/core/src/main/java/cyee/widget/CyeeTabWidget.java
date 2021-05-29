package cyee.widget;
//Gionee <zhaoyulong> <2015-06-02> add for CR01490697 begin
import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import cyee.changecolors.IChangeColors;
import cyee.theme.global.GlobalThemeConfigConstants;
import cyee.theme.global.ICyeeResource;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.cyee.internal.util.ReflectionUtils;

/**
*
* Displays a list of tab labels representing each page in the parent's tab
* collection. The container object for this widget is
* {@link android.widget.TabHost TabHost}. When the user selects a tab, this
* object sends a message to the parent container, TabHost, to tell it to switch
* the displayed page. You typically won't use many methods directly on this
* object. The container TabHost is used to add labels, add the callback
* handler, and manage callbacks. You might call this object to iterate the list
* of tabs, or to tweak the layout of the tab list, but most methods should be
* called on the containing TabHost object.
* 
* @author zhaoyulong
* */

public class CyeeTabWidget extends TabWidget implements IChangeColors {

	private static final int ACTIONBAR_TAB_INDICATOR_BOTTOM_PADDING = 0;
	private static final int ACTIONBAR_TAB_INDICATOR_HEIGHT = 2;
	private final int mSelectedUnderlineThickness;
	private final Paint mSelectedUnderlinePaint;

	private int mIndexForSelection;
	private float mSelectionOffset;
	private final int mSelectedUnderlineBottomPadding;
	private ColorStateList mColorStateList;
	private Drawable mIndicatorDrawable;
	
	private OnTabSelectionChanged mSelectionChangedListener;

	private static final String TAG = "CyeeTabWidget";

	public CyeeTabWidget(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public CyeeTabWidget(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.actionBarTabBarStyle);
		// TODO Auto-generated constructor stub
	}

	public CyeeTabWidget(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs, defStyle, 0);
		// TODO Auto-generated constructor stub
	}

    public CyeeTabWidget(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // TODO Auto-generated constructor stub
        setStripEnabled(false);
        setDividerDrawable(null);
        final Resources res = context.getResources();

        mSelectedUnderlineBottomPadding = (int) (ACTIONBAR_TAB_INDICATOR_BOTTOM_PADDING * res
                .getDisplayMetrics().density);
        mSelectedUnderlinePaint = new Paint();
        int underlineColor = context.getResources().getColor(com.cyee.internal.R.color.cyee_actionbar_tabtext_color_light);
        setIndicatorBackgroundColor(underlineColor);

        if (ChameleonColorManager.getInstance().getCyeeThemeType(
                mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Resources iCyeeRes = mContext.getResources();
            mIndicatorDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_tab_indicator);
            if (ChameleonColorManager.isNeedChangeColor(context)) {
                Drawable background = new ColorDrawable(
                        ChameleonColorManager.getStatusbarBackgroudColor_S1());
                setBackground(background);
            }
        } else if (ChameleonColorManager.isNeedChangeColor(context)) {
            Drawable background = new ColorDrawable(
                    ChameleonColorManager.getAppbarColor_A1());
            setBackground(background);
        } else {
            if (attrs != null) {
                TypedArray array = context.obtainStyledAttributes(attrs,
                        R.styleable.CyeeActionBar);
                Drawable mBackground = array
                        .getDrawable(R.styleable.CyeeActionBar_cyeebackground);
                setBackground(mBackground);
                array.recycle();
            }
        }
        
        int height = -1;
        if (null != mIndicatorDrawable) {
            height = mIndicatorDrawable.getIntrinsicHeight();
        }
        if (height == -1) {
            height = (int) (ACTIONBAR_TAB_INDICATOR_HEIGHT * res
                    .getDisplayMetrics().density);
        }
        mSelectedUnderlineThickness = height;
        
        setGravity(Gravity.CENTER_VERTICAL);
        setWillNotDraw(false);
    }

	/*
	 * 设置tab上面文字的颜色
	 * @param color 设置的文字颜色值
	 * */
    public void setCyeeTabWidgetTextColor(ColorStateList color) {
        mColorStateList = color;
    }
	
	/** {@inheritDoc} */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		if (v == this && hasFocus && getTabCount() > 0) {
			getChildTabViewAt(
					(Integer) ReflectionUtils.getFieldValue(this, "mSelectedTab"))
					.requestFocus();
			return;
		}

        if (hasFocus) {
            int i = 0;
            int numTabs = getTabCount();
            while (i < numTabs) {
                if (getChildTabViewAt(i) == v) {
                    setCurrentTab(i);
                    
                    if (mSelectionChangedListener != null) {
                        mSelectionChangedListener.onTabSelectionChanged(i, false);
                    }
                    if (isShown()) {
                        // a tab is focused so send an event to announce the tab widget state
                        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                    }
                    break;
                }
                i++;
            }
        }
	}

	/** {@inheritDoc} */
	@Override
    public void setCurrentTab(int index) {
        // TODO Auto-generated method stub
        int mSelectedTab = (Integer) ReflectionUtils.getFieldValue(this,
                "mSelectedTab");
        if (index < 0 || index >= getTabCount() || index == mSelectedTab) {
            return;
        }
        Log.v(TAG, "setCurrentTab: " + index + " preTab: " + mSelectedTab);
        if (mSelectedTab != -1) {
            TextView pretv = (TextView) getChildTabViewAt(mSelectedTab)
                    .findViewById(com.android.internal.R.id.title);
            setTextColor(pretv, false);
        }

        TextView curtv = (TextView) getChildTabViewAt(index).findViewById(
                com.android.internal.R.id.title);
        setTextColor(curtv, true);
        if (mSelectionChangedListener != null) {
            mSelectionChangedListener.onTabSelectionChanged(index, false);
        }
        super.setCurrentTab(index);
    }
	/** {@inheritDoc} */
	@Override
	public void addView(View child) {
		if (child.getLayoutParams() == null) {
			final LinearLayout.LayoutParams lp = new LayoutParams(0,
					ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
			lp.setMargins(0, 0, 0, 0);
			child.setLayoutParams(lp);
		}

		// Ensure you can navigate to the tab with the keyboard, and you can
		// touch it
		child.setFocusable(true);
		child.setClickable(true);
		TextView tv = (TextView) child.findViewById(com.android.internal.R.id.title);
		if (tv != null) {
			tv.setEllipsize(TruncateAt.END);
			tv.setLines(1);
			tv.setTextSize(
					TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimension(
							com.cyee.internal.R.dimen.cyee_actionbar_tabtext_text_size));
			
            setTextColor(tv, false);
			tv.setGravity(Gravity.CENTER);
		}
       
		super.addView(child);
		int mSelectedTab = (Integer) ReflectionUtils.getFieldValue(this, "mSelectedTab");
		if (mSelectedTab == -1) {
			setCurrentTab(0);
			ReflectionUtils.setFieldValue(this, "mSelectedTab", 0);
		}

		// TODO: detect this via geometry with a tabwidget listener rather
		// than potentially interfere with the view's listener
		child.setOnClickListener(new TabClickListener(getTabCount() - 1));
		child.setOnFocusChangeListener(this);
		
		
	}
	
    private void setTextColor(TextView view, boolean isSelected) {
        if (null == view || TextUtils.isEmpty(view.getText())) {
            return;
        }

        if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            if (isSelected) {
                view.setTextColor(ChameleonColorManager
                        .getContentColorPrimaryOnAppbar_T1());
            } else {
                view.setTextColor(ChameleonColorManager
                        .getContentColorThirdlyOnAppbar_T3());
            }
        } else {
            if (null != mColorStateList) {
                view.setSelected(isSelected);
                view.setTextColor(mColorStateList);
            } else {
                if (isSelected) {
                    view.setTextColor(getResources().getColor(com.cyee.internal.R.color.cyee_actionbar_tabtext_color_dark));
                } else {
                    view.setTextColor(getResources().getColor(com.cyee.internal.R.color.cyee_actionbar_tabtext_color_light));
                }
            }
        }
    }
	
	/**
	 * Notifies this view that view pager has been scrolled. We save the tab
	 * index and selection offset for interpolating the position and width of
	 * selection underline.
	 */
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		mIndexForSelection = position;
		mSelectionOffset = positionOffset;
		invalidate();
    }

    protected void setIndexForSelection(int indexForSelection) {
        mIndexForSelection = indexForSelection;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int childCount = getChildCount();
        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mIndexForSelection);
            int selectedLeft = selectedTitle.getLeft();
            int selectedRight = selectedTitle.getRight();
            final boolean isRtl = isRtl();
            final boolean hasNextTab = isRtl ? mIndexForSelection > 0
                    : (mIndexForSelection < (getChildCount() - 1));
            if ((mSelectionOffset > 0.0f) && hasNextTab) {
                // Draw the selection partway between the tabs
                View nextTitle = getChildAt(mIndexForSelection
                        + (isRtl ? -1 : 1));
                int nextLeft = nextTitle.getLeft();
                int nextRight = nextTitle.getRight();

                selectedLeft = (int) (mSelectionOffset * nextLeft + (1.0f - mSelectionOffset)
                        * selectedLeft);
                selectedRight = (int) (mSelectionOffset * nextRight + (1.0f - mSelectionOffset)
                        * selectedRight);
            }

            int height = getHeight();

            if (null == mIndicatorDrawable) {
                canvas.drawRect(selectedLeft, height
                        - mSelectedUnderlineThickness
                        - mSelectedUnderlineBottomPadding, selectedRight,
                        height - mSelectedUnderlineBottomPadding,
                        mSelectedUnderlinePaint);
            } else {
                mIndicatorDrawable.setBounds(selectedLeft, height
                        - mSelectedUnderlineThickness
                        - mSelectedUnderlineBottomPadding, selectedRight,
                        height - mSelectedUnderlineBottomPadding);
                mIndicatorDrawable.draw(canvas);
            }
        }
    }

	private boolean isRtl() {
		return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
	}
	
    public void setIndicatorBackgroundColor(int color) {
        if (ChameleonColorManager.getInstance().getCyeeThemeType(
                mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Resources iCyeeRes = mContext.getResources();
            mIndicatorDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_tab_indicator);
        } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            color = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
        }
        mSelectedUnderlinePaint.setColor(color);
    }

	 /**
     * Provides a way for {@link TabHost} to be notified that the user clicked on a tab indicator.
     */
    void setTabSelectionListener(OnTabSelectionChanged listener) {
        mSelectionChangedListener = listener;
    }
	/**
	 * 变色
	 */
	@Override
	public void changeColors() {
	    if (ChameleonColorManager.getInstance().getCyeeThemeType(
	            mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
	        if (ChameleonColorManager.isNeedChangeColor(mContext)) {
	            Drawable background = new ColorDrawable(
	                    ChameleonColorManager.getStatusbarBackgroudColor_S1());
	            setBackground(background);
	        }
	    } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
	        Drawable background = new ColorDrawable(
	                ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
	        setBackground(background);
	    }
	}
	
	
	/**
     * Let {@link TabHost} know that the user clicked on a tab indicator.
     */
    interface OnTabSelectionChanged {
        /**
         * Informs the TabHost which tab was selected. It also indicates
         * if the tab was clicked/pressed or just focused into.
         *
         * @param tabIndex index of the tab that was selected
         * @param clicked whether the selection changed due to a touch/click
         * or due to focus entering the tab through navigation. Pass true
         * if it was due to a press/click and false otherwise.
         */
        void onTabSelectionChanged(int tabIndex, boolean clicked);
    }
    
 // registered with each tab indicator so we can notify tab host
    private class TabClickListener implements OnClickListener {

        private final int mTabIndex;

        private TabClickListener(int tabIndex) {
            mTabIndex = tabIndex;
        }

        public void onClick(View v) {
            if (mSelectionChangedListener != null) {
                mSelectionChangedListener.onTabSelectionChanged(mTabIndex, true);
            }
        }
    }

}
//Gionee <zhaoyulong> <2015-06-02> add for CR01490697 end