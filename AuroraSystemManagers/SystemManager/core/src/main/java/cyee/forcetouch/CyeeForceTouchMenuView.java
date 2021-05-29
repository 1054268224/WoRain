package cyee.forcetouch;

import java.util.ArrayList;

import cyee.changecolors.ChameleonColorManager;
import cyee.forcetouch.CyeeForceTouchMenuItemView.OnSubMenuChangedListener;
import cyee.widget.CyeeWidgetResource;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CyeeForceTouchMenuView extends FrameLayout implements
        OnClickListener, OnSubMenuChangedListener {
    private final static String LOGTAG = "CyeeForceTouchMenuView";

    private final CyeeForceTouchMenu mMenu;
    private OnForceTouchMenuItemClickListener mMenuItemClickListener;
    private OnMenuViewChangedListener mOnMenuViewChangedListener;
    private int mSelectIdx = -1;
    private int mMenuType = CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU;
    private boolean isAnimaling = false;
    private boolean isShowSubMenu;
    private final ArrayList<MenuItem> mItems = new ArrayList<MenuItem>();
    private LinearLayout mLinearLayout;

    public enum MenuItemBgType {
        NORMAL(0), TOP(1), BOTTOM(2), SINGLE(3), PREVIEW(4);

        private int mValue = 0;

        MenuItemBgType(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public CyeeForceTouchMenuView(Context cxt, CyeeForceTouchMenu menu) {
        this(cxt, menu, CyeeForceTouchConstant.MENU_TYPE_QUICK_MENU); // 1联系人类型
    }

    public CyeeForceTouchMenuView(Context cxt, CyeeForceTouchMenu menu,
            int menuType) {
        super(cxt, null, 0, resolveTheme(cxt));

        mMenuType = menuType;
        mMenu = menu;
        setContentView(cxt);
        addContentItem();
        setItemBackground();
        setClickable(true);
    }

    public boolean isAnimaling() {
        return this.isAnimaling;
    }

    @Override
    public void onClick(View v) {
        if (mMenuItemClickListener != null) {
            mMenuItemClickListener.onMenuItemClick(mItems.get(v.getId()));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_DOWN:
            onTouchMenuItem(event);
            break;
        default:
            break;
        }
        return super.onTouchEvent(event);
    }

    public static MenuItemBgType getItemBgType(int idx, int cnt) {
        MenuItemBgType type = MenuItemBgType.NORMAL;
        if (cnt == 1) {
            type = MenuItemBgType.SINGLE;
        } else {
            if (idx == 0) {
                type = MenuItemBgType.TOP;
            } else if (idx == cnt - 1) {
                type = MenuItemBgType.BOTTOM;
            }
        }

        return type;
    }

    public static Drawable getItemBackground(Context cxt, MenuItemBgType type) {
        Resources res = cxt.getResources();
        Drawable drawable = null;
        switch (type) {
        case TOP:
            drawable = res.getDrawable(com.cyee.internal.R.drawable.cyee_forcetouch_menu_item_top_bg);
            break;
        case BOTTOM:
            drawable = res.getDrawable(com.cyee.internal.R.drawable.cyee_forcetouch_menu_item_bottom_bg);
            break;
        case SINGLE:
            drawable = res.getDrawable(com.cyee.internal.R.drawable.cyee_forcetouch_menu_single_item_bg);
            break;
        case PREVIEW:
            drawable = res.getDrawable(com.cyee.internal.R.drawable.cyee_forcetouch_preview_menu_item_bg);
            break;
        default:
            drawable = res.getDrawable(com.cyee.internal.R.drawable.cyee_forcetouch_menu_item_bg);
            break;
        }

        if (ChameleonColorManager.isNeedChangeColor(cxt)) {
            int pressedColorId = com.cyee.internal.R.color.cyee_forcetouch_menu_press_tint_color;
            int pressedColor = res.getColor(pressedColorId);
            drawable.setTintMode(Mode.SRC_IN);
            drawable.setTintList(new ColorStateList(new int[][] {
                    { android.R.attr.state_pressed }, {} }, new int[] {
                    pressedColor,
                    ChameleonColorManager.getPopupBackgroudColor_B2() }));
        }
        return drawable;
    }

    public boolean onTouchMenuItem(MotionEvent event) {
        boolean ret = false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            if (isShowSubMenu) {
                ((CyeeForceTouchMenuItemView) mLinearLayout
                        .getChildAt(mSelectIdx)).onTouchEvent(event);
            } else {
                doActionUp(event);
            }
        } else if (action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_DOWN) {
            if (isShowSubMenu) {
                ((CyeeForceTouchMenuItemView) mLinearLayout
                        .getChildAt(mSelectIdx)).onTouchEvent(event);
            } else {
                doActionMoveNormal(event);
            }
        } else if (action == MotionEvent.ACTION_OUTSIDE) {
            if (mMenuItemClickListener != null) {
                mMenuItemClickListener.onMenuItemClick(null);
            }
            ret = true;
        } else {
            // to do others
        }

        return ret;
    }

    public OnForceTouchMenuItemClickListener getOnForceTouchMenuItemClickListener() {
        return mMenuItemClickListener;
    }

    public void setOnForceTouchMenuItemClickListener(
            OnForceTouchMenuItemClickListener onItemClickListener) {
        this.mMenuItemClickListener = onItemClickListener;
    }

    public void setOnMenuViewChangedListener(OnMenuViewChangedListener listener) {
        this.mOnMenuViewChangedListener = listener;
    }

    public void showPopMenuAnimations(float startY, float endY) {
        showPopMenuAnimations(0, 0, startY, endY);
    }

    public void showPopMenuAnimations(float pivotX, float pivotY, float startY,
            float endY) {
        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            startShowSMSMenuAnimations(startY, endY);
        } else {
            startShowPopMenuAnimations(pivotX, pivotY, startY, endY);
        }
    }

    public void hidePopMenuAnimations() {
        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            hideSMSMenuAnimations();
        } else {
            hideContactMenuAnimations();
        }
    }

    public void cancelShowPopMenuAnim() {
        if (mSmsPopMenuAnim != null && mSmsPopMenuAnim.isRunning()) {
            Log.d(LOGTAG, "cancelShowPopMenuAnim");
            mSmsPopMenuAnim.cancel();
        }
        if (mPopMenuAnim != null && mPopMenuAnim.isRunning()) {
            mPopMenuAnim.cancel();
        }
    }

    public void setMenuViewAlpha(float alpha) {
        final int childCnt = mLinearLayout.getChildCount();
        for (int i = 0; i < childCnt; i++) {
            View view = mLinearLayout.getChildAt(i);
            view.setAlpha(alpha);
        }
    }
    
    @Override
    public void onSubMenuChangedListener(boolean isunFolded) {
        if (isunFolded) {
            unFoldSubMenu(isunFolded);
        } else {
            foldSubMenu();
        }
    }

    private void addContentItem() {
        MenuItem item = null;
        int size = mMenu.size();
        int id = 0;
        if (mMenu.getMenuSort() == CyeeForceTouchConstant.MENU_SORT_REVERT_ORDER) {
            for (int i = size - 1; i >= 0; i--) {
                item = mMenu.getItem(i);
                if (item.isVisible()) {
                    addItem(item, id++);
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                item = mMenu.getItem(i);
                if (item.isVisible()) {
                    addItem(item, id++);
                }
            }
        }
    }

    private void setContentView(Context cxt) {
        LayoutInflater layoutInflater = LayoutInflater.from(cxt);
        mLinearLayout = (LinearLayout) layoutInflater.inflate(
                com.cyee.internal.R.layout.cyee_forcetouch_menu, null);
        Drawable drawable = null;
        int bgId = 0;

        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            bgId = com.cyee.internal.R.drawable.cyee_forcetouch_sms_menu_bg;
        } else {
            bgId = com.cyee.internal.R.drawable.cyee_forcetouch_menu_bg;
        }

        drawable = getResources().getDrawable(bgId);
        if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            drawable.setTint(ChameleonColorManager.getPopupBackgroudColor_B2());
        }
        // 设置菜单容器线性布局的背景色
        mLinearLayout.setBackgroundColor(Color.TRANSPARENT);
        addView(mLinearLayout);
        setBackground(drawable);
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

    private void addItem(MenuItem item, int id) {
        mItems.add(item);
        CyeeForceTouchMenuItemView explandView = new CyeeForceTouchMenuItemView(
                mContext, item, mMenuType, mMenu.getMenuSort());
        explandView.setOnSubMenuChangedListener(this);
        explandView.setId(id);
        mLinearLayout.addView(explandView);
    }

    private void setItemBackground() {
        if (mMenuType == CyeeForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW) {
            addPreviewMenuItemBg();
        } else {
            addNormalMenuItemBg();
        }
    }

    private void addPreviewMenuItemBg() {
        int cnt = mLinearLayout.getChildCount();
        for (int i = 0; i < cnt; i++) {
            CyeeForceTouchMenuItemView view = (CyeeForceTouchMenuItemView) mLinearLayout
                    .getChildAt(i);
            Drawable drawable = getItemBackground(mContext, MenuItemBgType.PREVIEW);
            view.setViewBackground(drawable);
        }
    }

    private void doActionUp(MotionEvent event) {
        boolean ret = false;
        Log.d(LOGTAG,"doActionUp mSelectIdx="+mSelectIdx);
        if (mSelectIdx != -1) {
            CyeeForceTouchMenuItemView curItemView = (CyeeForceTouchMenuItemView) mLinearLayout
                    .getChildAt(mSelectIdx);
            curItemView
                    .setOnForceTouchMenuItemClickListener(mMenuItemClickListener);
            View subIcon = curItemView.findViewById(com.cyee.internal.R.id.cyee_submenuIcon);

            if (null != subIcon && subIcon.getVisibility() == View.VISIBLE) {
                Rect r = new Rect();
                View subMenuIconLayout = curItemView
                        .findViewById(com.cyee.internal.R.id.cyee_submenuIcon_layout);
                subMenuIconLayout.getGlobalVisibleRect(r);
                ret = r.contains((int) event.getRawX(), (int) event.getRawY());
                Log.d(LOGTAG,"doActionUp ret="+ret);
                if (ret) {
                    isShowSubMenu = !isShowSubMenu;
                    if (isShowSubMenu) {
                        onSubMenuChangedListener(true);
                        curItemView.unfoldSubMenu(true);
                    } else {
                    }
                    curItemView.setPressed(false);

                    return;
                }
            }
            curItemView.customPerformClick();
        }
    }

    private boolean doActionMoveNormal(MotionEvent event) {
        boolean ret = false;
        boolean resp = CyeeForceTouchUtils.isClickViewOnGlobalScreen(this, event, true);
        float y = event.getRawY();

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
                ((CyeeForceTouchMenuItemView) mLinearLayout
                        .getChildAt(mSelectIdx)).setItemPress(0, false);
            }
            mSelectIdx = curIdx;
            ((CyeeForceTouchMenuItemView) mLinearLayout.getChildAt(mSelectIdx))
                    .setItemPress(0, true);
        } else {
            if (mSelectIdx != -1) {
                ((CyeeForceTouchMenuItemView) mLinearLayout
                        .getChildAt(mSelectIdx)).setItemPress(0, false);
                mSelectIdx = -1;
            }
        }
        return ret;
    }

    private void addNormalMenuItemBg() {
        int cnt = mLinearLayout.getChildCount();
        for (int i = 0; i < cnt; i++) {
            MenuItemBgType type = getItemBgType(i, cnt);
            CyeeForceTouchMenuItemView view = (CyeeForceTouchMenuItemView) mLinearLayout
                    .getChildAt(i);
            Drawable drawable = getItemBackground(mContext, type);
            view.setViewBackground(drawable);
        }
    }

    private void hideContactMenuAnimations() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(this, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(this, "scaleY", 1f, 0.8f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.0f);

        float pivotY = 0f;
        float transY = 0f;

        if (null != mOnMenuViewChangedListener) {
            transY = mOnMenuViewChangedListener.getTranslationY();
        }
        float startY = getY(), endY = startY - transY;

        if (mMenu.getMenuSort() == CyeeForceTouchConstant.MENU_SORT_REVERT_ORDER) {
            pivotY = getHeight();
            endY = getY() + transY;
        }

        ObjectAnimator translationY = ObjectAnimator.ofFloat(this,
                "translationY", startY, endY);

        setPivotY(pivotY);
        setPivotX(getWidth() / 2);

        set.playTogether(scaleX, alpha, scaleY, translationY);
        set.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        set.start();
    }

    private void hideSMSMenuAnimations() {
        int screentH = CyeeForceTouchUtils.getScreenHeight(getContext());

        if (screentH <= this.getY()) {
            return;
        }

        isAnimaling = true;
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this,
                "translationY", getY(), screentH);
        translationY.setDuration(CyeeForceTouchConstant.SMS_MENU_ANIM_TIME);
        translationY.start();
        translationY.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                isAnimaling = false;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                isAnimaling = false;
            }
        });
    }

    private ValueAnimator mSmsPopMenuAnim;
    private AnimatorSet mPopMenuAnim;
    
    private void startShowPopMenuAnimations(float pivotX, float pivotY,
            float startY, float endY) {
        isAnimaling = true;
        mPopMenuAnim = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(this, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(this, "scaleY", 0.8f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this,
                "translationY", startY, endY);

        setPivotY(pivotY);
        setPivotX(pivotX);

        mPopMenuAnim.playTogether(scaleX, alpha, scaleY, translationY);
        mPopMenuAnim.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        mPopMenuAnim.start();

        mPopMenuAnim.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                isAnimaling = false;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                isAnimaling = false;
            }
        });
    }

    private float mStartFraction;

    private void startShowSMSMenuAnimations(final float startY, final float endY) {
        isAnimaling = true;
        mSmsPopMenuAnim = new ValueAnimator();
        mSmsPopMenuAnim.setDuration(CyeeForceTouchConstant.MENU_ANIM_TIME);
        mSmsPopMenuAnim.setObjectValues(new PointF(0, 0));
        mSmsPopMenuAnim.setEvaluator(new TypeEvaluator<PointF>() {

            @Override
            public PointF evaluate(float fraction, PointF arg1, PointF arg2) {

                PointF point = new PointF();
                float yPosDistance = mOnMenuViewChangedListener
                        .getNewEndPosDistance(endY);
                point.y = startY - fraction * (startY - endY);
                if (yPosDistance != 0) {
                    if (mStartFraction == 0) {
                        mStartFraction = fraction;
                    }
                    float leftFraction = 1 - mStartFraction;

                    point.y -= (point.y - endY + yPosDistance)
                            * (leftFraction - (1 - fraction)) / leftFraction;
                }

                return point;
            }

        });
        mSmsPopMenuAnim.start();
        mSmsPopMenuAnim
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        PointF f = (PointF) animator.getAnimatedValue();
                        CyeeForceTouchMenuView.this.setY(f.y);
                    }
                });

        mSmsPopMenuAnim.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                isAnimaling = false;
                mStartFraction = 0;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
    }

    public interface OnForceTouchMenuItemClickListener {
        void onMenuItemClick(MenuItem menuItem);
    }

    public interface OnMenuViewChangedListener {
        void onMenuViewChanged();

        float getNewEndPosDistance(float endY);

        float getTranslationY();
    }

    static class MenuAnimationInfo {
        public float mPivotX;
        public float mPivotY;
        public float mStartY;
        public float mEndY;

        public MenuAnimationInfo(float pivotX, float pivotY, float startY,
                float endY) {
            this.mPivotX = pivotX;
            this.mPivotY = pivotY;
            this.mStartY = startY;
            this.mEndY = endY;
        }
    }

    private static class AnimalData {
        public int curYPos;
        public int curHeight;
        public int curParentHeight;
        public int curParentYPos;
    }

    private void foldSubMenu() {
        isShowSubMenu = false;
        showSubMenuAnimator();
    }

    private void unFoldSubMenu(final boolean isunFolded) {
        isShowSubMenu = true;
        showSubMenuAnimator();
    }

    private void showSubMenuAnimator() {
        int itemH = CyeeForceTouchUtils.dip2px(mContext,
                CyeeForceTouchConstant.MENU_ITEM_HEIGHT);
        final int width = getWidth();
        final int height = getHeight();
        CyeeForceTouchMenuItemView curView = (CyeeForceTouchMenuItemView) mLinearLayout
                .getChildAt(mSelectIdx);
        final int subMenuCnt = curView.getChildCount();
        final int moveup = itemH * mSelectIdx;
        final int diffHeight;
        final float curYPos = getY();
        final float toYPos;
        final int startHeight;
        final int endH;

        if (isShowSubMenu) {
            diffHeight = height - (itemH * subMenuCnt + moveup);
            startHeight = 0;
            endH = 0;
        } else {
            startHeight = itemH * mSelectIdx + height;
            endH = mLinearLayout.getChildCount() * itemH;
            diffHeight = startHeight - endH;

            int childCnt = mLinearLayout.getChildCount();
            mLinearLayout.setY(-itemH * mSelectIdx);
            for (int i = 0; i < childCnt; i++) {
                View view = mLinearLayout.getChildAt(i);
                view.setVisibility(View.VISIBLE);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    getWidth(), itemH * mSelectIdx + getHeight());
            mLinearLayout.setLayoutParams(params);
        }

        if (mMenu.getMenuSort() == CyeeForceTouchConstant.MENU_SORT_REVERT_ORDER) {
            toYPos = (subMenuCnt - mLinearLayout.getChildCount()) * itemH;
        } else {
            toYPos = 0;
        }

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator
                .setDuration(CyeeForceTouchConstant.UNFOLD_SUBMENU_ANIM_TIME);
        valueAnimator.setObjectValues(new AnimalData());
        valueAnimator.setEvaluator(new TypeEvaluator<AnimalData>() {

            @Override
            public AnimalData evaluate(float fraction, AnimalData arg1,
                    AnimalData arg2) {
                AnimalData data = new AnimalData();
                if (isShowSubMenu) {
                    data.curYPos = -(int) (fraction * moveup);
                    data.curHeight = height - (int) (fraction * diffHeight);
                    data.curParentHeight = data.curHeight + data.curYPos;
                    data.curParentYPos = (int) curYPos
                            - (int) (fraction * toYPos);
                } else {
                    data.curYPos = (int) ((fraction - 1) * moveup);
                    data.curHeight = startHeight
                            - (int) (fraction * diffHeight);
                    data.curParentHeight = height
                            - (int) (fraction * (height - endH));
                    data.curParentYPos = (int) curYPos
                            + (int) (fraction * toYPos);
                }
                return data;
            }
        });
        valueAnimator.start();
        valueAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                MenuItemBgType type = getItemBgType(0, subMenuCnt);
                if (isShowSubMenu) {
                    int childCnt = mLinearLayout.getChildCount();
                    for (int i = 0; i < childCnt; i++) {
                        if (i != mSelectIdx) {
                            View view = mLinearLayout.getChildAt(i);
                            view.setVisibility(View.GONE);
                        }
                    }
                } else {
                    int childCnt = mLinearLayout.getChildCount();
                    type = getItemBgType(mSelectIdx, childCnt);
                }

                CyeeForceTouchMenuItemView view = (CyeeForceTouchMenuItemView) mLinearLayout
                        .getChildAt(mSelectIdx);
                Drawable drawable = getItemBackground(mContext, type);
                view.setViewBackground(drawable);

                mLinearLayout.setY(0);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        getWidth(), LayoutParams.WRAP_CONTENT);
                mLinearLayout.setLayoutParams(params);
                mOnMenuViewChangedListener.onMenuViewChanged();
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        valueAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        AnimalData data = (AnimalData) animator
                                .getAnimatedValue();
                        mLinearLayout.setY(data.curYPos);
                        FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(
                                width, data.curHeight);
                        mLinearLayout.setLayoutParams(linearLayoutParams);
                        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
                                width, data.curParentHeight);
                        CyeeForceTouchMenuView.this
                                .setLayoutParams(frameLayoutParams);
                        CyeeForceTouchMenuView.this.setY(data.curParentYPos);

                    }
                });
    }

}
