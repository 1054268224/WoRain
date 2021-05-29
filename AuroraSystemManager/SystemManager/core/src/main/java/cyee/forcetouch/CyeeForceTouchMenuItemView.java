package cyee.forcetouch;

import java.util.ArrayList;

import cyee.changecolors.ChameleonColorManager;
import cyee.forcetouch.CyeeForceTouchMenuView.MenuItemBgType;
import cyee.forcetouch.CyeeForceTouchMenuView.OnForceTouchMenuItemClickListener;
import cyee.widget.CyeeWidgetResource;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CyeeForceTouchMenuItemView extends FrameLayout implements
        OnClickListener {

    private final static String LOGTAG = "CyeeForceTouchMenuItemView";

    private int mMenuType = CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU;
    private boolean isSubMenuunFolded = false;
    private int mCurHeight = 0;
    private OnSubMenuChangedListener mSubMenuChangedListener;
    private final ArrayList<MenuItem> mItems = new ArrayList<MenuItem>();
    private int mSelectIdx = -1;
    private OnForceTouchMenuItemClickListener mMenuItemClickListener;
    private ImageView mSubMenuIcon;

    public CyeeForceTouchMenuItemView(Context context) {
        super(context, null, 0, 0);
    }

    public CyeeForceTouchMenuItemView(Context cxt, MenuItem item,
            int menuType, int menuSort) {
        super(cxt, null, 0, resolveTheme(cxt));

        mMenuType = menuType;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        mCurHeight = CyeeForceTouchUtils.dip2px(mContext,
                CyeeForceTouchConstant.MENU_ITEM_HEIGHT);
        addItem(item);
        setInitItemPressBg();
        setClickable(true);
    }

    public void setViewBackground(Drawable drawable) {
        getChildAt(0).setBackground(drawable);
        
    }

    public void setItemPress(int idx, boolean isPressed) {
        getChildAt(idx).setPressed(isPressed);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            doActionUp(event);
        } else if (action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_DOWN) {
            doActionMove(event);
            // }
        } else {
            // to do others
        }

        return super.onTouchEvent(event);
    }

    public void unfoldSubMenu(boolean unfolded) {
        if (unfolded) {
            unfoldSubMenu();
        } else {
            foldSubMenu();
        }
    }

    public void setOnSubMenuChangedListener(OnSubMenuChangedListener listener) {
        mSubMenuChangedListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(widthSize, mCurHeight);
        }
    }

    public OnForceTouchMenuItemClickListener getOnForceTouchMenuItemClickListener() {
        return mMenuItemClickListener;
    }

    public void setOnForceTouchMenuItemClickListener(
            OnForceTouchMenuItemClickListener onItemClickListener) {
        this.mMenuItemClickListener = onItemClickListener;
    }

    public boolean customPerformClick() {
        boolean ret = false;
        if (!isSubMenuunFolded) {
            return getChildAt(0).performClick();
        }

        return ret;
    }

    @Override
    public void onClick(View v) {
        if (mMenuItemClickListener != null) {
            mMenuItemClickListener.onMenuItemClick(mItems.get(v.getId()));
        }
    }

    private boolean doActionUp(MotionEvent event) {
        boolean ret = false;
        int cnt = getChildCount();
        if (cnt > 1) {
            if (mSelectIdx != -1) {
                View view = getChildAt(mSelectIdx);
                ImageView subMenuIcon = (ImageView) view
                        .findViewById(com.cyee.internal.R.id.cyee_submenuIcon);
                if (null != subMenuIcon
                        && subMenuIcon.getVisibility() == View.VISIBLE) {
                    Rect r = new Rect();
                    View subMenuIconLayout = view
                            .findViewById(com.cyee.internal.R.id.cyee_submenuIcon_layout);
                    subMenuIconLayout.getGlobalVisibleRect(r);
                    ret = r.contains((int) event.getRawX(),
                            (int) event.getRawY());
                    if (ret) {
                        isSubMenuunFolded = !isSubMenuunFolded;
                        unfoldSubMenu(isSubMenuunFolded);
                        if (null != mSubMenuChangedListener) {
                            mSubMenuChangedListener
                                    .onSubMenuChangedListener(isSubMenuunFolded);
                        }
                        return ret;
                    }
                }
                getChildAt(mSelectIdx).performClick();
            }
        } else {

        }

        return ret;
    }

    private void rotationAnimator(View subIcon, float fromx, float tox) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(subIcon, "rotation",
                fromx, tox);
        rotation.setDuration(CyeeForceTouchConstant.UNFOLD_SUBMENU_ANIM_TIME);
        rotation.start();
    }

    private boolean doActionMove(MotionEvent event) {
        boolean ret = false;
        boolean resp = CyeeForceTouchUtils.isClickViewOnGlobalScreen(this, event, false);
        float y = event.getY();
        Rect r = new Rect();
        getGlobalVisibleRect(r);
        if (resp) {
            int size = mItems.size();
            int itemHeight = getHeight() / size;
            int curIdx = -1;
            for (int i = 0; i < size; i++) {
                if (y >= getY() + itemHeight * i
                        && y < getY() + itemHeight * (i + 1)) {
                    curIdx = i;
                    break;
                }
            }
            if (mSelectIdx != -1 && curIdx != mSelectIdx) {
                setItemPress(mSelectIdx, false);
            }
            mSelectIdx = curIdx;
            setItemPress(mSelectIdx, true);
        } else {
            if (mSelectIdx != -1) {
                setItemPress(mSelectIdx, false);
                mSelectIdx = -1;
            }
        }

        return ret;
    }

    private void foldSubMenu() {
        isSubMenuunFolded = false;
        showSubMenuAnimator();
    }

    private void unfoldSubMenu() {
        isSubMenuunFolded = true;
        showSubMenuAnimator();
    }

    private void showSubMenuAnimator() {

        float fromx = 0, tox = 180;

        if (!isSubMenuunFolded) {
            fromx = 180;
            tox = 0;
        }
        rotationAnimator(mSubMenuIcon, fromx, tox);

        final int itemH = CyeeForceTouchUtils.dip2px(mContext,
                CyeeForceTouchConstant.MENU_ITEM_HEIGHT);
        final int size = getChildCount();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator
                .setDuration(CyeeForceTouchConstant.UNFOLD_SUBMENU_ANIM_TIME);
        valueAnimator.setObjectValues(new PointF());
        valueAnimator.setEvaluator(new TypeEvaluator<PointF>() {

            @Override
            public PointF evaluate(float fraction, PointF arg1, PointF arg2) {

                PointF point = new PointF();
                point.y = fraction * itemH;
                return point;
            }
        });
        valueAnimator.start();
        valueAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        PointF f = (PointF) animator.getAnimatedValue();
                        if (isSubMenuunFolded) {
                            for (int i = 1; i < size; i++) {
                                View view = getChildAt(i);
                                view.setVisibility(View.VISIBLE);
                                view.setZ(-i);
                                float y = f.y * i;
                                view.setY(y);
                            }
                            mCurHeight = itemH + (int) f.y * (size - 1);
                        } else {
                            for (int i = 1; i < size; i++) {
                                View view = getChildAt(i);
                                view.setZ(-i);
                                float y = f.y * i;
                                view.setY(i * itemH - y);
                                if (f.y == itemH) {
                                    view.setVisibility(View.INVISIBLE);
                                } else {
                                    view.setVisibility(View.VISIBLE);
                                }
                            }
                            mCurHeight = itemH * size - (int) f.y * (size - 1);
                        }

                        CyeeForceTouchMenuItemView.this.requestLayout();
                    }
                });
    }

    private static int resolveTheme(Context cxt) {
        TypedValue outValue = new TypedValue();
        boolean ret = cxt.getTheme().resolveAttribute(
                com.cyee.internal.R.attr.cyeeForceTouchMenuItemBackgroundColor, outValue,
                true);
        if (!ret) {
            cxt.setTheme(com.cyee.internal.R.style.Theme_Cyee_Light);
        }
        return 0;
    }

    private void addItem(MenuItem item) {
        int resId = com.cyee.internal.R.layout.cyee_forcetouch_menu_item;

        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            resId = com.cyee.internal.R.layout.cyee_forcetouch_sms_menu_item;
        }

        View parent = LayoutInflater.from(getContext()).inflate(resId, null,
                false);

        ImageView iconView = (ImageView) parent
                .findViewById(com.cyee.internal.R.id.cyee_menuIcon);
        TextView titleView = (TextView) parent.findViewById(com.cyee.internal.R.id.cyee_menuTitle);
        parent.setEnabled(item.isEnabled());
        if (!TextUtils.isEmpty(item.getTitle())) {
            titleView.setText(item.getTitle());
        }

        if (mMenuType != CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            Drawable icon = item.getIcon();
            if (icon == null) {
                icon = getResources().getDrawable(
                        com.cyee.internal.R.drawable.cyee_forcetouch_menu_icon);
            }
            if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU) {
                if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                    int iconDefaultColor = ChameleonColorManager
                            .getContentColorPrimaryOnBackgroud_C1();
                    icon.setColorFilter(iconDefaultColor, Mode.SRC_IN);
                } else {
                    int iconDefaultColorId = com.cyee.internal.R.color.cyee_bright_foreground_primary_dark;
                    icon.setColorFilter(
                            getResources().getColor(iconDefaultColorId),
                            Mode.SRC_IN);
                }
            }
            iconView.setImageDrawable(icon);
        }
        SubMenu subMenu = item.getSubMenu();
        boolean hasSubMenu = false;
        View subMenuIconLayout = parent.findViewById(com.cyee.internal.R.id.cyee_submenuIcon_layout);
        mSubMenuIcon = (ImageView) parent.findViewById(com.cyee.internal.R.id.cyee_submenuIcon);
        if (null != subMenu && subMenu.size() > 0 && null != mSubMenuIcon) {
            Drawable icon = mSubMenuIcon.getDrawable();
            if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                int iconDefaultColor = ChameleonColorManager
                        .getContentColorPrimaryOnBackgroud_C1();
                icon.setColorFilter(iconDefaultColor, Mode.SRC_IN);
            } else {
                int iconDefaultColorId = com.cyee.internal.R.color.cyee_bright_foreground_primary_dark;
                icon.setColorFilter(
                        getResources().getColor(iconDefaultColorId),
                        Mode.SRC_IN);
            }
            mSubMenuIcon.setVisibility(View.VISIBLE);
            hasSubMenu = true;
        } else {
            if (null != subMenuIconLayout) {
                subMenuIconLayout.setVisibility(View.GONE);
            }
        }

        addView(parent);
        parent.setFocusable(true);
        parent.setId(0);
        parent.setOnClickListener(this);
        mItems.add(item);

        if (hasSubMenu) {
            int size = subMenu.size();
            for (int i = 0; i < size; i++) {
                MenuItem subitem = subMenu.getItem(i);
                if (subitem.isVisible()) {
                    addSubItem(subitem, i);
                }
            }
            int cnt = getChildCount();
            if (cnt <= 1) {
                if (null != subMenuIconLayout) {
                    subMenuIconLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private void addSubItem(MenuItem item, int id) {
        id += 1;
        int resId = com.cyee.internal.R.layout.cyee_forcetouch_submenu_item;

        View parent = LayoutInflater.from(getContext()).inflate(resId, this,
                false);

        TextView titleView = (TextView) parent.findViewById(com.cyee.internal.R.id.cyee_menuTitle);
        parent.setId(id);
        parent.setEnabled(item.isEnabled());
        if (!TextUtils.isEmpty(item.getTitle())) {
            titleView.setText(item.getTitle());
        }
        parent.setZ(-id);
        parent.setVisibility(View.INVISIBLE);
        parent.setFocusable(true);
        addView(parent);
        parent.setId(mItems.size());
        parent.setOnClickListener(this);
        mItems.add(item);
    }

    private void setInitItemPressBg() {
        int cnt = getChildCount();
        if (cnt <= 1) {
            return;
        }

        for (int i = 1; i < cnt; i++) {
            MenuItemBgType type = CyeeForceTouchMenuView.getItemBgType(i, cnt);
            Drawable drawable = CyeeForceTouchMenuView.getItemBackground(
                    mContext, type);
            getChildAt(i).setBackground(drawable);
        }
    }

    public interface OnSubMenuChangedListener {
        void onSubMenuChangedListener(boolean isExpland);
    }

}
