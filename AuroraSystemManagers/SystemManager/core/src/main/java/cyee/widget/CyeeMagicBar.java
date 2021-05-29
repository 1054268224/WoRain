package cyee.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.internal.view.menu.MenuItemImpl;
import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

//import com.android.internal.view.menu.MenuItemImpl;

public class CyeeMagicBar extends RelativeLayout implements OnClickListener, OnItemClickListener, OnTouchListener, OnLongClickListener {

    private static final String TAG = "CyeeMagicBar";
    // constants
    public static final int MAGIC_BAR_MENU_MAX_COUNT = 4;
    public static final int MAX_BAR_ITEM_COUNT = MAGIC_BAR_MENU_MAX_COUNT;
    // private static final int MAX_ICON_SIZE = 60;

    private static final int MAGICBAR_LISTVIEW_LANDSCAPE_MAX_LINE = 2;
    private static final int MAGICBAR_LISTVIEW_PORTRAIT_MAX_LINE = 4;

    // parameters
    private Drawable mCyeeOptionMenuMoreBg;
    private int mItemHeight;
    private int mListItemHeight;
    private int mTitleModeHeight;
    private int mMaxListViewheight;
    private int mListViewHorizontalMargin;
    private int mListViewBottomMargin;
    private int mMaxIconSize;
    private int mTitleBottomPadding;
    private int mListViewTopMargin;

    private onOptionsItemSelectedListener mListener;
    private onMoreItemSelectedListener mMoreListener;
    private OnTransparentTouchListener mTouchListener;
    private onOptionsItemLongClickListener mLongClickListener;
    private OnMagicBarVisibleChangedListener mOnMagicBarVisibleChangedListener;
    private final Map<Button, MenuItem> menusOnTab = new HashMap<Button, MenuItem>();
    private final List<MenuItem> menusOnList = new ArrayList<MenuItem>();
    private TranslateAnimation mTranslateAnimation;
    private float mListModeAnimationHeght;

    // views
    private ScrollView mScrollView;
    private LinearLayout mMenuView;
    private RelativeLayout mCyeeMagicbarLayout;
    private LinearLayout mMagicbarBackgroud;
    private View mShadow;
    private Button[] mTabButtons;
    private boolean isHideMode;
    private LinearLayout mTab;
    private ObjectAnimator mTranslateAnimator = new ObjectAnimator();
    private boolean isExpand = false;
    private View mTabDivider;
    private LayoutAnimationController mLayoutAnimationController;
    
    private ColorStateList mDefaultTextColor;
    
    public CyeeMagicBar(Context context) {
        this(context, null);
    }

    public CyeeMagicBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initData(context, attrs);
        initView();
    }

    private void initData(Context context, AttributeSet attrs) {
        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.cyeeOptionMenu);
        mCyeeOptionMenuMoreBg = a.getDrawable(R.styleable.cyeeOptionMenu_cyeeoptionMenuMoreBg);
        a.recycle();

        Resources resources = context.getResources();
        mItemHeight = (int) resources.getDimension(com.cyee.internal.R.dimen.cyee_magicbar_item_height);
        mListItemHeight = (int) resources.getDimension(com.cyee.internal.R.dimen.cyee_magicbar_list_item_height);
        mTitleModeHeight = (int) resources.getDimension(com.cyee.internal.R.dimen.cyee_magicbar_title_mode_height);
        mMaxListViewheight = (int) resources.getDimension(com.cyee.internal.R.dimen.cyee_magicbar_max_listview_height);
        mListViewHorizontalMargin = (int) resources.getDimension(com.cyee.internal.R.dimen.cyee_magicbar_listview_left_right_margin);
        mListViewBottomMargin = (int) resources.getDimension(com.cyee.internal.R.dimen.cyee_magicbar_listview_bottom_margin);
        // final float density = resources.getDisplayMetrics().density;
        // mMaxIconSize = (int) (MAX_ICON_SIZE * density + 8.5f);
        computeMaxIconSize();
        mTitleBottomPadding = (int) resources.getDimension(com.cyee.internal.R.dimen.cyee_magicbar_title_bottom_padding);
        mListViewTopMargin = (int) resources.getDimension(com.cyee.internal.R.dimen.cyee_magicbar_listview_top_margin);
    }

    private void computeMaxIconSize() {
        float density = mContext.getResources().getDisplayMetrics().density;
        float width = mContext.getResources().getDisplayMetrics().widthPixels;
        //left and right padding is 10dp，inner left and right padding is 10dp，so total is 40dp
        mMaxIconSize = (int) ((width - 40* density) / 4);
    }

    /**
     * 获取底部tab菜单文字颜色
     */
    private void getDefaultTextColor() {
        final int[] textColor = new int[] {android.R.attr.actionMenuTextColor};
        TypedArray a = getContext().getTheme().obtainStyledAttributes(textColor);
        mDefaultTextColor = a.getColorStateList(0);
        if (null == mDefaultTextColor) {
            mDefaultTextColor = getResources().getColorStateList(com.cyee.internal.R.color.cyee_action_menu_text);
        }
        a.recycle();
    }
    
    private void initView() {
        getDefaultTextColor();
        final Context context = getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mCyeeMagicbarLayout = (RelativeLayout) inflater.inflate(com.cyee.internal.R.layout.cyee_magicbar_menu_item,
                this, false);
        mMagicbarBackgroud = (LinearLayout) mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_magicbar_backgroud);
        mTab = (LinearLayout) mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_magicbar_tab);
        initTabButtons();

        mTab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                
            }
        });
        
        mShadow = mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_magicbar_shadow);
        mShadow.setBackgroundColor(Color.TRANSPARENT);
        mShadow.setVisibility(View.GONE);

        mTabDivider = mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_magicbar_tab_divider);
        mTabDivider.setVisibility(View.GONE);

        mMenuView = (LinearLayout) mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_menu_list);
        mScrollView = (ScrollView) mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_menu_scrollview);

        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Resources iCyeeRes = getResources();
            Log.d(TAG, "initView GLOBAL_THEME start");
            setBackgroundColor(Color.TRANSPARENT);
            mMagicbarBackgroud.setBackgroundColor(Color.TRANSPARENT);
            Drawable bgDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_magicbar_bg);
            mTab.setBackground(bgDrawable);
            Drawable listviewBg = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_magicbar_listview_bg);
            mMenuView.setBackground(listviewBg);
        } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
//            Drawable drawable = mMenuView.getSelector();
//            if (drawable != null) {
//                if (drawable instanceof RippleDrawable) {
//                    ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
//                }
//            }
            mMagicbarBackgroud.getBackground().setColorFilter(ChameleonColorManager.getPopupBackgroudColor_B2(), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        removeAllViews();
        addView(mCyeeMagicbarLayout);
    }

    private void initTabButtons() {
        mTabButtons = new Button[4];
        mTabButtons[0] = (Button) mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_icon0);
        mTabButtons[1] = (Button) mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_icon1);
        mTabButtons[2] = (Button) mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_icon2);
        mTabButtons[3] = (Button) mCyeeMagicbarLayout.findViewById(com.cyee.internal.R.id.cyee_icon3);
        for (int i = 0; i < 4; i++) {
            mTabButtons[i].setOnClickListener(this);
            mTabButtons[i].setOnLongClickListener(this);
            mTabButtons[i].setMinimumWidth(mMaxIconSize);
            mTabButtons[i].setMinWidth(mMaxIconSize);
        }
    }

    public void clearMagicBarData() {
        initView();
        setMagicBarVisibilityWithoutAnim(View.GONE);
    }

    public void setMenus(Menu menu) {
        Log.v(TAG, "Refresh Menus start");
        if (isAnimatorRunning()) {
            return;
        }
        Log.v(TAG, "Refresh Menus");
        if (isMenusEmpty(menu)) {
            setMagicBarVisibilityWithoutAnim(View.GONE);
            return;
        }
        menusOnList.clear();
        menusOnTab.clear();
        for (Button btn : mTabButtons) {
            btn.setVisibility(View.GONE);
        }

        List<MenuItem> alwaysMenuItem = new ArrayList<MenuItem>();
        List<MenuItem> ifRoomMenuItem = new ArrayList<MenuItem>();
        List<MenuItem> othersMenuItem = new ArrayList<MenuItem>();
        int menuItemCount = menu.size();
        for (int i = 0; i < menuItemCount; i++) {
            if (!menu.getItem(i).isVisible()) {
                continue;
            }

            MenuItemImpl menuItem = (MenuItemImpl) menu.getItem(i);
            if (menuItem.requiresActionButton()) {
                alwaysMenuItem.add(menuItem);
                continue;
            }

            if (menuItem.requestsActionButton()) {
                ifRoomMenuItem.add(menuItem);
                continue;
            }

            othersMenuItem.add(menuItem);
        }

        List<MenuItem> tabMenuItem = new ArrayList<MenuItem>();
        tabMenuItem.addAll(alwaysMenuItem);
        tabMenuItem.addAll(ifRoomMenuItem);

        if (tabMenuItem.size() > 4) {
            setMoreIconAndText();
            mTabButtons[3].setVisibility(View.VISIBLE);
            mTabButtons[3].setEnabled(true);
            for (int i = 0; i < tabMenuItem.size(); i++) {
                MenuItem menuItem = tabMenuItem.get(i);
                if (i < 3) {
                    menusOnTab.put(mTabButtons[i], menuItem);
                } else {
                    menusOnList.add(menuItem);
                }
            }
        } else {

            for (int i = 0; i < 3; i++) {
                if (i < tabMenuItem.size()) {
                    MenuItem menuItem = tabMenuItem.get(i);
                    menusOnTab.put(mTabButtons[i], menuItem);
                }
            }

            if (othersMenuItem.size() > 0) {
                setMoreIconAndText();
                mTabButtons[3].setVisibility(View.VISIBLE);
                mTabButtons[3].setEnabled(true);
                if (tabMenuItem.size() > 3) {
                    MenuItem menuItem = tabMenuItem.get(3);
                    if (menuItem != null) {
                        menusOnList.add(menuItem);
                    }
                }
            } else {

                if (tabMenuItem.size() > 3) {
                    MenuItem menuItem = tabMenuItem.get(3);
                    if (menuItem != null) {
                        menusOnTab.put(mTabButtons[3], menuItem);
                    }
                }
            }

        }

        menusOnList.addAll(othersMenuItem);
        setTabButtonMenuIconAndText();
        constructMenuView();
        setListViewMaxHeight();

        if (menusOnList.isEmpty()) {
            isExpand = false;
        }
        if (menusOnList.isEmpty() && menusOnTab.isEmpty()) {
            setMagicBarVisibilityWithoutAnim(View.GONE);
        } else {
            if (!isHideMode) {
                setMagicBarVisibilityWithoutAnim(View.VISIBLE);
            } else {
                setMagicBarVisibilityWithoutAnim(View.GONE);
            }
        }
        updateShadow();
    }

    private void constructMenuView() {
        int size = menusOnList.size();
        int menuCnt = mMenuView.getChildCount();
        if (size < menuCnt) {
            for (int i = 0; i < menuCnt - size; i++) {
                mMenuView.removeViewAt(menuCnt - 1 - i);
            }
        }
        boolean useCache = false;
        for (int i = 0; i < size; i++) {
            useCache = menuCnt > 0;
            final MenuItem menuItem = menusOnList.get(i);
            View convertView = null;
            if (useCache) {
                convertView = mMenuView.getChildAt(i);
            } else {
                convertView = LayoutInflater.from(mContext).inflate(
                        com.cyee.internal.R.layout.cyee_magicbar_listview_item, null);
            }
            if (null == convertView) {
                useCache = false;
                convertView = LayoutInflater.from(mContext).inflate(
                        com.cyee.internal.R.layout.cyee_magicbar_listview_item, null);
            }
            setMenuBackground(convertView, false);
            // Gionee <weidong> <2017-7-26> modify for 175425 begin
            if (menuItem.isEnabled()) {
                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (isAnimatorRunning()) {
                            Log.d(TAG, "constructMenuView animation hasStarted, list item can not click");
                            return;
                        }
                        Log.v(TAG, "constructMenuView mListener != null:" + (mListener != null));

                        if (mListener != null) {
                            mListener.onOptionsItemSelected(menuItem);
                        }
                    }
                });
            } else {
                // Gionee <weidong> <2017-8-17> modify for 186350 begin
                convertView.setClickable(false);
                // Gionee <weidong> <2017-8-17> modify for 186350 end
            }
            // Gionee <weidong> <2017-7-26> modify for 175425 end
            TextView textView = (TextView) convertView.findViewById(com.cyee.internal.R.id.cyee_magic_listitem_textview);
            CheckBox checkBox = (CheckBox) convertView.findViewById(com.cyee.internal.R.id.cyee_magic_listitem_checkbox);

            if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
                if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                    Resources iCyeeRes = getResources();
                    ColorStateList textColors = iCyeeRes.getColorStateList(com.cyee.internal.R.color.cyee_global_theme_magicbar_text_color_selector);
                    textView.setTextColor(textColors);
                }
            }

            if (menuItem == null) {
                if (!useCache) {
                    mMenuView.addView(convertView);
                }
                return;
            }
            textView.setText(menuItem.getTitle());
            textView.setAlpha(1.0f);
            checkBox.setVisibility(View.GONE);
            checkBox.setChecked(false);

            if (!menuItem.isEnabled()) {
                textView.setAlpha(0.3f);
            } else {
                textView.setAlpha(1.0f);
            }

            if (menuItem.isCheckable()) {
                checkBox.setVisibility(View.VISIBLE);
            } else {
                checkBox.setVisibility(View.GONE);
            }

            checkBox.setChecked(menuItem.isChecked());
            if (!useCache) {
                mMenuView.addView(convertView);
            }
        }
        int childCnt = mMenuView.getChildCount();
        if (childCnt > 0) {
            View view = mMenuView.getChildAt(childCnt - 1);
            setMenuBackground(view, true);
        }
    }

    private void setMenuBackground(View view, boolean isLastItem) {
        if (null == view) {
            return ;
        }
        if (isLastItem) {
            view.setBackground(getResources().getDrawable(
                    com.cyee.internal.R.drawable.cyee_magicbar_last_item_bg_ripple));
            View divider = view.findViewById(com.cyee.internal.R.id.cyee_divider);
            if (null != divider) {
                divider.setVisibility(View.GONE);
            }
        } else {
            view.setBackground(getResources().getDrawable(
                    com.cyee.internal.R.drawable.cyee_magicbar_item_bg_ripple));
            View divider = view.findViewById(com.cyee.internal.R.id.cyee_divider);
            if (null != divider) {
                divider.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void setTabButtonMenuIconAndText() {
        for (Button tabBtn : mTabButtons) {
            MenuItem menuItem = menusOnTab.get(tabBtn);
            if (menuItem == null) {
                continue;
            }

            tabBtn.setCompoundDrawablesWithIntrinsicBounds(null, zoomIcon(getChameleonDrawable(menuItem.getIcon())), null, null);
            int padding = (int)getResources().getDimension(com.cyee.internal.R.dimen.cyee_magicbar_icon_and_text_padding);
            tabBtn.setCompoundDrawablePadding(padding);
            tabBtn.setEnabled(menuItem.isEnabled());
            tabBtn.setText(menuItem.getTitle());
            tabBtn.setTag(menuItem.getItemId());
            tabBtn.setVisibility(View.VISIBLE);
            // Gionee <weidong> <2017-7-28> modify for 175433 begin
            tabBtn.setTextColor(mDefaultTextColor);
            // Gionee <weidong> <2017-7-28> modify for 175433 end
            if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                ColorStateList textColors = new ColorStateList(new int[][] { {-android.R.attr.state_enabled}, {android.R.attr.state_enabled}},
                        new int[] {ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3(),
                                ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()});
                tabBtn.setTextColor(textColors);

                Drawable backgroundDrawable = tabBtn.getBackground();
                if (backgroundDrawable instanceof RippleDrawable) {
                    ((RippleDrawable) backgroundDrawable).setColor(ColorStateList.valueOf(ChameleonColorManager
                            .getContentColorThirdlyOnBackgroud_C3()));
                }
            }
        }
    }

    private boolean haveMagicListView() {
        return isHaveData();
    }

    private boolean isMenusEmpty(Menu menu) {
        if (menu == null) {
            return true;
        }

        int menuCount = menu.size();
        return menuCount == 0;
    }

    // set "more/close" icon
    public void setMoreIconAndText() {
        Drawable moreIcon = getChameleonDrawable(mCyeeOptionMenuMoreBg);
        Drawable closeIcon = mContext.getResources().getDrawable(
                com.cyee.internal.R.drawable.cyee_magic_menu_close_icon);

        int moreButtonIndex = MAX_BAR_ITEM_COUNT - 1;

        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Resources iCyeeRes = getResources();
            Log.d(TAG, "setMoreIconAndText GLOBAL_THEME start");
            Drawable moreDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_magicbar_more_icon);
            moreIcon = moreDrawable;

            Drawable closeDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_magicbar_close_icon);
            closeIcon = closeDrawable;
        } else {
            if (closeIcon != null && ChameleonColorManager.isNeedChangeColor(mContext)) {
                closeIcon.setColorFilter(ChameleonColorManager.getAccentColor_G1(), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        if (isExpand()) {
            mTabButtons[moreButtonIndex].setCompoundDrawablesWithIntrinsicBounds(null, closeIcon, null, null);
            mTabButtons[moreButtonIndex].setText(com.cyee.internal.R.string.cyee_actionbar_magic_item_close);
            mTabButtons[moreButtonIndex].setTextColor(getResources().getColor(com.cyee.internal.R.color.cyee_alert_dialog_button_text_color));
            if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                mTabButtons[moreButtonIndex].setTextColor(ChameleonColorManager.getAccentColor_G1());
            }
        } else {
            mTabButtons[moreButtonIndex].setCompoundDrawablesWithIntrinsicBounds(null, moreIcon, null, null);
            mTabButtons[moreButtonIndex].setText(com.cyee.internal.R.string.cyee_actionbar_magic_item_more);
            mTabButtons[moreButtonIndex].setTextColor(mDefaultTextColor);
            if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                mTabButtons[moreButtonIndex].setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            }
        }
        int padding = (int)getResources().getDimension(com.cyee.internal.R.dimen.cyee_magicbar_icon_and_text_padding);
        mTabButtons[moreButtonIndex].setCompoundDrawablePadding(padding);
        
        if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            Drawable d = mTabButtons[moreButtonIndex].getBackground();
            if (d instanceof RippleDrawable) {
                ((RippleDrawable) d).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
            }

        }
        if (menusOnList.isEmpty()) {
            mTabButtons[moreButtonIndex].setVisibility(View.GONE);
            mTabButtons[moreButtonIndex].setEnabled(false);
        } else {
            mTabButtons[moreButtonIndex].setVisibility(View.VISIBLE);
            mTabButtons[moreButtonIndex].setEnabled(true);
        }
    }

    private Drawable zoomIcon(Drawable icon) {
        if (icon != null) {
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if ((width > mMaxIconSize || height > mMaxIconSize) && (width > 0 && height > 0 && mMaxIconSize > 0)) {
                Bitmap bm = drawableToBitmap(icon);
                Drawable drawable = new BitmapDrawable(bm);
                return drawable;
            }
            return icon;

        }
        return null;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int w = Math.min(mMaxIconSize, drawable.getIntrinsicWidth());
        int h = Math.min(mMaxIconSize, drawable.getIntrinsicHeight());
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    private Drawable getChameleonDrawable(Drawable drawable) {

        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {

        } else if (drawable != null && ChameleonColorManager.isNeedChangeColor(mContext)) {
            drawable.setColorFilter(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1(), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            // Gionee <weidong> <2017-8-1> modify for 176359 begin
            if (null != drawable) {
                drawable.clearColorFilter();
            }
            // Gionee <weidong> <2017-8-1> modify for 176359 end
        }
        return drawable;
    }

    private void setListViewMaxHeight() {
        if (mMenuView == null) {
            return;
        }
        int maxItemCount;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            maxItemCount = MAGICBAR_LISTVIEW_LANDSCAPE_MAX_LINE;
        } else {
            maxItemCount = MAGICBAR_LISTVIEW_PORTRAIT_MAX_LINE;
        }

        int height = 0;
        if (menusOnList.size() > maxItemCount) {
            height = mMaxListViewheight;
        } else {
            height = menusOnList.size() * getListItemHeight();
        }
        mListModeAnimationHeght = height;
    }

    public int getItemHeight() {
        return mItemHeight;
    }

    public int getListItemHeight() {
        return mListItemHeight;
    }

    public int getTitleModeHeight() {
        return mTitleModeHeight;
    }

    public void setExpand(boolean isExpend) {
        this.isExpand = isExpend;
    }

    public boolean isExpand() {
        return isExpand;
    }

    private void updateView() {
        updateShadow();
        if (menusOnList.size() > 0) {
            setMoreIconAndText();
        }
    }

    private void updateShadow() {
        if (isExpand()) {
            mShadow.setVisibility(View.GONE);
            mShadow.setBackgroundResource(com.cyee.internal.R.drawable.cyee_magicbar_gradient);
            setViewHeight(0);
            mTabDivider.setVisibility(View.VISIBLE);
        } else {
            mTabDivider.setVisibility(View.GONE);
            if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {

            } else {
                setViewHeight(0);
                mShadow.setBackgroundColor(0x50808080);
            }
        }
        if (menusOnList.isEmpty() && menusOnTab.isEmpty()) {
            if (mShadow.getVisibility() != View.GONE) {
                mShadow.setVisibility(View.GONE);
            }
        } else {
            if (mShadow.getVisibility() != View.VISIBLE) {
                mShadow.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setViewHeight(int height) {
        android.view.ViewGroup.LayoutParams lp = mShadow.getLayoutParams();
        lp.height = height;
        mShadow.setLayoutParams(lp);
        mShadow.invalidate();
    }

    public boolean isHaveData() {
        return mMenuView != null && mMenuView.getChildCount() > 0;
    }

    /**
     * Interface definition for a callback to be invoked when click Item
     */
    public interface onOptionsItemSelectedListener {
        boolean onOptionsItemSelected(MenuItem menuItem);
    }

    public interface onOptionsItemLongClickListener {
        boolean onOptionsItemLongClick(MenuItem menuItem);
    }

    public interface OnTransparentTouchListener {
        boolean OnTransparentTouch(View v, MotionEvent event);
    }

    public interface onMoreItemSelectedListener {
        boolean onMoreItemSelected(View view);
    }

    public interface OnMagicBarVisibleChangedListener {
        void onMagicBarVisibleChanged(int visibility);
    }

    /**
     * Register a callback to be invoked when click one Item
     * 
     * @param l
     *            The callback that will run
     */
    public void setonTransparentTouchListener(OnTransparentTouchListener l) {
        mTouchListener = l;
    }

    public void setonOptionsItemSelectedListener(onOptionsItemSelectedListener l) {
        mListener = l;
    }

    public void setonMoreItemSelectedListener(onMoreItemSelectedListener l) {
        mMoreListener = l;
    }

    public void setonOptionsItemLongClickListener(onOptionsItemLongClickListener l) {
        mLongClickListener = l;
    }

    public void setOnMagicBarVisibleChangedListener(OnMagicBarVisibleChangedListener l) {
        mOnMagicBarVisibleChangedListener = l;
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        return false;
    }

    @Override
    public void onClick(View v) {
        if (isAnimatorRunning()) {
            Log.d(TAG, "animation hasStarted, tab item can not click");
            return;
        }

        int moreButtonId = com.cyee.internal.R.id.cyee_icon3;

        Log.v(TAG, "onClick,v.getId() == moreButtonId:" + (v.getId() == moreButtonId) + " menuOnTab == null:" + (menusOnTab.get(v) == null)
                + " mListener == null:" + (mListener == null));

        if (v.getId() == moreButtonId && menusOnTab.get(v) == null) {
            if (mMoreListener != null) {
                mMoreListener.onMoreItemSelected(v);
            }
            return;
        }
        if (mListener == null) {
            return;
        }
        MenuItem menuItem = menusOnTab.get(v);
        if (menuItem != null && menuItem.isEnabled()) {
            mListener.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mLongClickListener == null) {
            Log.v(TAG, "mLongClickListener is null");
            return true;
        }

        int moreButtonId = com.cyee.internal.R.id.cyee_icon3;
        if (v.getId() == moreButtonId && menusOnTab.get(v) == null) {
            Log.v(TAG, "moreButton can't be longClicked");
            return true;
        }

        mLongClickListener.onOptionsItemLongClick(menusOnTab.get(v));
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(isAnimatorRunning()) {
            return ;
        }

        Log.v(TAG, "mListener != null:" + (mListener != null));

        if (mListener != null) {
            mListener.onOptionsItemSelected(menusOnList.get(position));
        }

    }

    public boolean isHideMode() {
        return isHideMode;
    }

    public void setHideMode(boolean isHideMode) {
        Log.d(TAG, "setHideMode isHideMode="+isHideMode);
        this.isHideMode = isHideMode;
    }

    class MenuListAdapter extends BaseAdapter {

        public MenuListAdapter() {
        }

        @Override
        public int getCount() {
            return menusOnList.size();
        }

        @Override
        public Object getItem(int position) {
            return menusOnList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean isEnabled(int position) {
            if (menusOnList.size() > position && !menusOnList.get(position).isEnabled()) {
                return false;
            }

            return super.isEnabled(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (null == convertView) {
                holder = new Holder();
                convertView = LayoutInflater.from(mContext).inflate(
                        com.cyee.internal.R.layout.cyee_magicbar_listview_item, null);
                holder.textView = (TextView) convertView.findViewById(com.cyee.internal.R.id.cyee_magic_listitem_textview);
                holder.checkBox = (CheckBox) convertView.findViewById(com.cyee.internal.R.id.cyee_magic_listitem_checkbox);

                if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
                    if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                        Resources iCyeeRes = getResources();
                        ColorStateList textColors = iCyeeRes.getColorStateList(com.cyee.internal.R.color.cyee_global_theme_magicbar_text_color_selector);
                        holder.textView.setTextColor(textColors);
                    }
                }

                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            MenuItem menuItem = menusOnList.get(position);
            if (menuItem == null) {
                return convertView;
            }
            holder.textView.setText(menuItem.getTitle());
            holder.textView.setAlpha(1.0f);
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);

            if (!menuItem.isEnabled()) {
                holder.textView.setAlpha(0.3f);
            } else {
                holder.textView.setAlpha(1.0f);
            }

            if (menuItem.isCheckable()) {
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }

            holder.checkBox.setChecked(menuItem.isChecked());

            return convertView;
        }

        class Holder {
            public TextView textView;
            public CheckBox checkBox;
        }
    }

    public void onConfigurationChanged2(Configuration newConfig) {
        if (null != newConfig) {
            Log.v(TAG, "CyeeMagicBar onConfigurationChanged is called newConfig=" + newConfig.orientation);
        }
        mMaxListViewheight = (int) mContext.getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_magicbar_max_listview_height);
        setListViewMaxHeight();
        adjustTabButtonMinWidth();
    }

    private void adjustTabButtonMinWidth() {
        computeMaxIconSize();
        for (int i = 0; i < 4; i++) {
            mTabButtons[i].setMinimumWidth(mMaxIconSize);
            mTabButtons[i].setMinWidth(mMaxIconSize);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        adjuestListViewHeightIfNeed(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void adjuestListViewHeightIfNeed(int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mMaxListViewheight + mTitleModeHeight > height) {
            mListModeAnimationHeght = height - mTitleModeHeight;
        } else {
            // Gionee <weidong> <2016-12-28> modify for 54452 begin
            // setListViewMaxHeight();
            // Gionee <weidong> <2016-12-28> modify for 54452 end
        }
    }

	// ======================  set listview and magicbar visibility with Animations ======================

    private static final int TRANSLATION_ANIMATION_DURATION = 320;

    public void changeListViewVisiable(boolean withAnim) {
        if (!haveMagicListView()) {
            return;
        }

        if (isExpand()) {
            if (withAnim) {
                setListViewVisibilityWithAnim(View.GONE);
            } else {
                setListViewVisibilityWithoutAnim(View.GONE);
            }
        } else {
            if (withAnim) {
                setListViewVisibilityWithAnim(View.VISIBLE);
            } else {
                setListViewVisibilityWithoutAnim(View.VISIBLE);
            }
        }
    }

    public void setOptionsMenuUnExpand() {
        setMagicBarVisibilityWithoutAnim(View.GONE);
        this.isExpand = false;
    }

    public void setMagicBarVisibilityWithoutAnim(int visibility) {
        cancelAnimator();
        Log.d(TAG, "setMagicBarVisibilityWithoutAnim visibility="+visibility+";mListModeAnimationHeght="+mListModeAnimationHeght
                +";isExpand="+isExpand);
        if (isExpand) {
            mScrollView.setVisibility(View.VISIBLE);
            setMagicbarListViewHeight((int) mListModeAnimationHeght);
        } else {
            mScrollView.setVisibility(View.GONE);
        }
        this.setVisibility(visibility);
        if (mOnMagicBarVisibleChangedListener != null) {
            mOnMagicBarVisibleChangedListener.onMagicBarVisibleChanged(this.getVisibility());
        }
    }

    public void setMagicBarVisibilityWithAnim(final int visibility) {
        cancelAnimator();
        Log.d(TAG, "setMagicBarVisibilityWithAnim visibility="+visibility);
        if (isExpand) {
            initAnimator(this, "translationY", getTranslationY(), 0);
            mTranslateAnimator.start();
        } else {
            initAnimator(this, "translationY", getTranslationY(), mListModeAnimationHeght);
            mTranslateAnimator.start();
        }
        this.setVisibility(visibility);
        if (mOnMagicBarVisibleChangedListener != null) {
            mOnMagicBarVisibleChangedListener.onMagicBarVisibleChanged(this.getVisibility());
        }
    }

    public void setListViewVisibilityWithoutAnim(int visibility) {
        isExpand = visibility == View.VISIBLE;
        if (isExpand) {
            mScrollView.setVisibility(View.VISIBLE);
            Log.d(TAG, "setListViewVisibilityWithoutAnim mListModeAnimationHeght="+mListModeAnimationHeght);
            setMagicbarListViewHeight((int) mListModeAnimationHeght);
        } else {
            mScrollView.setVisibility(View.GONE);
        }
        updateView();
    }

    public void setListViewVisibilityWithAnim(final int visibility) {
        if (isAnimatorRunning()) {
            return;
        }

        if (visibility == View.VISIBLE) {
            showMenuAnimator(true);
            mMenuView.postDelayed(new Runnable() {

                @Override
                public void run() {
                    initAnim(0, 0, mListModeAnimationHeght, 0, LayoutAnimationController.ORDER_NORMAL);
                    startLayoutTransition();
                }
            }, 30);
        } else {
            initAnim(0, 0, 0, mListModeAnimationHeght, LayoutAnimationController.ORDER_REVERSE);
            startLayoutTransition();
            mMenuView.postDelayed(new Runnable() {

                @Override
                public void run() {
                    showMenuAnimator(false);
                }
            }, 50);
        }
    }

    private void startLayoutTransition() {
        mMenuView.setLayoutAnimation(mLayoutAnimationController);
        mMenuView.startLayoutAnimation();
    }

    private void initAnim(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, int animalType) {
        mTranslateAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        mTranslateAnimation.setFillAfter(true);
        mTranslateAnimation.setDuration(TRANSLATION_ANIMATION_DURATION);
        mTranslateAnimation.setInterpolator(new DecelerateInterpolator());
        mLayoutAnimationController = new LayoutAnimationController(mTranslateAnimation);
        float cnt = mMenuView.getChildCount();
        float delayfactor = TRANSLATION_ANIMATION_DURATION / cnt / 1000f / 2; //此值用于延迟动画，属于经验值
        mLayoutAnimationController.setDelay(delayfactor);
        mLayoutAnimationController.setOrder(animalType);
    }

    private void initAnimator(View view, String attr, float from, float to) {
        mTranslateAnimator = ObjectAnimator.ofFloat(view, attr, from, to);
        mTranslateAnimator.setDuration(TRANSLATION_ANIMATION_DURATION);
        mTranslateAnimator.setInterpolator(new DecelerateInterpolator());
    }

    private ValueAnimator mHeightAnimator;

    private void setMagicbarListViewHeight(int height) {
         ViewGroup.LayoutParams p = mScrollView.getLayoutParams();
         p.height = height;
         mScrollView.setLayoutParams(p);
    }

    private void showMenuAnimator(final boolean showMenu) {
        mScrollView.setVerticalScrollBarEnabled(false);
        if (showMenu) {
            mScrollView.setVisibility(View.VISIBLE);
            setMagicbarListViewHeight(0);
        }

        int startV = 0, endV = 0;
        if (showMenu) {
            endV = (int) mListModeAnimationHeght;
            isExpand = true;
            updateView();
        } else {
            startV = (int) mListModeAnimationHeght;
        }

        mHeightAnimator = ValueAnimator.ofInt(startV, endV);
        mHeightAnimator.setDuration(TRANSLATION_ANIMATION_DURATION);
        mHeightAnimator.setEvaluator(new TypeEvaluator<Integer>() {

            @Override
            public Integer evaluate(float fraction, Integer startV, Integer endV) {
                int height = 0;
                height = startV + (int) (fraction * 100 * (endV - startV)) / 100;
                Log.d(TAG, "showMenuAnimator height=" + height);

                return height;
            }
        });
        mHeightAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                Integer h = (Integer) animator.getAnimatedValue();
                setMagicbarListViewHeight(h);
            }
        });

        mHeightAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                if (showMenu) {
                    mScrollView.scrollTo(0, 0);
                }
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                if (!showMenu) {
                    mScrollView.setVisibility(View.GONE);
                    isExpand = false;
                    updateView();
                }
                mScrollView.setVerticalScrollBarEnabled(true);
                if (null != mTranslateAnimation) {
                    mTranslateAnimation.cancel();
                }
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        mHeightAnimator.start();
    }
    
    private boolean isAnimatorRunning() {
        boolean ret = false;

        if (mTranslateAnimator != null) {
            if (mTranslateAnimator.isStarted() && mTranslateAnimator.isRunning()) {
                return true;
            }
        }
        if (null != mHeightAnimator) {
            if (mHeightAnimator.isStarted() && mHeightAnimator.isRunning()) {
                return true;
            }
        }

        return ret;
    }
    
    private void cancelAnimator() {
        if (mTranslateAnimator != null) {
            if (mTranslateAnimator.isStarted() && mTranslateAnimator.isRunning()) {
                mTranslateAnimator.cancel();
            }
        }
        if (null != mHeightAnimator) {
            if (mHeightAnimator.isStarted() && mHeightAnimator.isRunning()) {
                mHeightAnimator.cancel();
            }
        }
    }
}
