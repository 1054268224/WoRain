package cyee.app;

// Gionee <daizhimin> <2013-07-04> add for CR00833379 begin 

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.Window.Callback;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.internal.view.menu.MenuBuilder;
import com.cyee.internal.R;
import com.cyee.utils.Log;
import com.cyee.utils.LogUtil;

import java.lang.reflect.Field;

import cyee.changecolors.ChameleonColorManager;
import cyee.theme.global.CyeeResources;
import cyee.theme.global.CyeeThemeManager;
import cyee.theme.global.ICyeeResource;
import cyee.widget.CyeeMagicBar;
import cyee.widget.CyeeMagicBar.OnMagicBarVisibleChangedListener;
import cyee.widget.CyeeMagicBar.OnTransparentTouchListener;
import cyee.widget.CyeeMagicBar.onMoreItemSelectedListener;
import cyee.widget.CyeeMagicBar.onOptionsItemLongClickListener;
import cyee.widget.CyeeMagicBar.onOptionsItemSelectedListener;

//chenyee 2017-11-11 hushengsong modify navigation color begin


//chenyee 2017-11-11 hushengsong modify navigation color end
public class CyeeActivity extends AppCompatActivity implements onOptionsItemSelectedListener,
        onMoreItemSelectedListener, OnTransparentTouchListener, onOptionsItemLongClickListener, ICyeeResource {
    private static final String TAG = "CyeeActivity";
    private CyeeMagicBar mCyeeMagicBar;
    // shaozj private boolean mClickFlag = false ;
    private boolean mHideMode = false, mShowAgain = true;
    // shaozj

    private boolean mDelay = true;
    // Gionee <gaoj> <2013-9-6> modify for CR00889318 begin
    private LinearLayout mEmptyLayout;
    // Gionee <gaoj> <2013-9-6> modify for CR00889318 end
    private CyeeActionBarImpl mCyeeActionBar = null;
    private LayoutInflater mLayoutInflater;
    private ViewGroup mScreenActionBarLayout;
    private FrameLayout mContentLayout;
    private Menu mOptionMenu;
    // private boolean mNoMenuItem = false ;

    // Gionee <gaoj> <2013-9-6> modify for CR00889318 begin
    private ActionBar mActionBar = null;
    // Gionee <gaoj> <2013-9-6> modify for CR00889318 end

    // Gionee <gaoj> <2013-10-10> add for CR00916580 begin
    private TranslateAnimation mTranslateAnimation;
    // Gionee <gaoj> <2013-10-10> add for CR00916580 end

    // Gionee <gaoj> <2013-12-12> add for CR00974643 begin
    private LinearLayout mMagicbarBg;

    // Gionee <gaoj> <2013-12-12> add for CR00974643 end

    private CyeeResources mCyeeResources;
    private boolean isOverlay = false;

    private boolean showVirtualKeyboard = true;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void init() {
        LogUtil.LOGE("CyeeActivity init()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.LOGE("CyeeActivity onCreate()");
        getSupportActionBar().hide();
        if (!ChameleonColorManager.isNeedChangeColor(this)) {
            ChameleonColorManager.getInstance().clearDrawableCaches(this);
        }
        if (ChameleonColorManager.getInstance().getCyeeThemeType(this) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
//            mCyeeResources = (CyeeResources) CyeeThemeManager.getInstance(this)
//                    .loadCyeeThemeResources(this);
        } else if (ChameleonColorManager.isNeedChangeColor(this)) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.windowIsTranslucent, outValue, true);
            if (outValue.data == 0) { // windowIsTranslucent == false
                getWindow().setBackgroundDrawable(new ColorDrawable(ChameleonColorManager.getBackgroudColor_B1()));
            }
            //chenyee 2017-11-11 hushengsong modify navigation color begin
            int value = Settings.Global.getInt(getContentResolver(),
                    "navigation_bar_color", 0);
            if (value == 0) {
                setNavigationBackground(ChameleonColorManager.getBackgroudColor_B1());
            }
            //chenyee 2017-11-11 hushengsong modify navigation color end
        } else {
            //chenyee 2017-11-11 hushengsong modify navigation color begin
            //setNavigationBackground(getResources().getColor(com.cyee.internal.R.color.cyee_window_background_light));
            //chenyee 2017-11-11 hushengsong modify navigation color end
        }

    }

    private void setGlobalThemeBg() {
        if (ChameleonColorManager.getInstance().getCyeeThemeType(this) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            if (!isOverlay && CyeeThemeManager.getInstance(this).isM2017()) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            Drawable defaultDrawable = getWindow().getDecorView().getBackground();
            if (null != defaultDrawable && defaultDrawable instanceof StateListDrawable) {
                int id = com.cyee.internal.R.drawable.cyee_global_theme_main_bg_small;
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
                BitmapDrawable bgDrawable = new BitmapDrawable(bitmap);
                bgDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
                bgDrawable.setDither(true);
                getWindow().setBackgroundDrawable(bgDrawable);
            }
        } else if (ChameleonColorManager.isNeedChangeColor(this)) {
            //Chenyee <Cyee_Widget> hushengsong 2018-10-12 modify for CSW1805A-191 begin
            if (ChameleonColorManager.isLightTheme(ChameleonColorManager.getAppbarColor_A1())) {
                getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            /*if (!ChameleonColorManager.isLightTheme(ChameleonColorManager.getBackgroudColor_B1())) {
                setNavigationIconWhite();
            }*/
            //Chenyee <Cyee_Widget> hushengsong 2018-10-12 modify for CSW1805A-191 end
        } else {
            int color = getCyeeActionBarColor();
            if (color != -1 && ChameleonColorManager.isLightTheme(color)) {
                //Chenyee <Cyee_Widget> hushengsong 2018-10-12 modify for CSW1805A-160 begin
                getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                //Chenyee <Cyee_Widget> hushengsong 2018-10-12 modify for CSW1805A-160 end
            }
        }
    }

    /**
     * 设置虚拟按键的背景色
     */
    public void setNavigationBackground(int color) {
        if (!showVirtualKeyboard) {
            return;
        }
        /* virtual keybord begin */
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        /* virtual keybord end */
        getWindow().setNavigationBarColor(color);
    }

    /**
     * 设置虚拟按键图标为白色，默认状态为灰色
     */
    public void setNavigationIconWhite() {
        /* virtual keybord begin */
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions |= 0x00000010;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        /* virtual keybord end */
    }

    @Override
    protected void onResume() {
        // Gionee <weidong> <2017-4-28> modify for 99559 begin
        if (!ChameleonColorManager.isNeedChangeColor(this)) {
            ChameleonColorManager.getInstance().clearDrawableCaches(this);
        }
        // Gionee <weidong> <2017-4-28> modify for 99559 end
        super.onResume();
        if (mDelay) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    // Gionee <lihq> <2014-6-24> modify for CR00873172 begin
                    /*
                     * if (mCyeeActionBar != null) {
                     * mCyeeActionBar.startOptionsMenu(); }
                     */
                    startOptionsMenu();
                    // Gionee <lihq> <2014-6-24> modify for CR00873172 end
                }

            }, 50);
            mDelay = false;
        }
        // shaozj add
        setStatusBarColor();
    }

    @Override
    protected void onStop() {
        if (mCyeeMagicBar != null) {
            // shaozj mClickFlag = true;
            // Gionee <gaoj> <2013-10-10> add for CR00916580 begin
            // handItemClick();
            // gionee maxw modify begin
//			mCyeeMagicBar.setCyeeMagicBarVisibility(false);
            mCyeeMagicBar.setListViewVisibilityWithoutAnim(View.GONE);
            // gionee maxw modify end
            // Gionee <gaoj> <2013-10-10> add for CR00916580 end
        }
        super.onStop();
    }

    @Override
    public Resources getResources() {
        if (mCyeeResources != null) {
            return mCyeeResources;
        }

        return super.getResources();
    }

    @Override
    public Theme getTheme() {
        return super.getTheme();
    }

    // Gionee <weidong> <2015-06-04> add for CR01496371 begin
    private boolean isDialogTheme() {
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowAnimationStyle, outValue, true);
        int id = com.cyee.internal.R.style.Animation_Cyee_Dialog;
        return outValue.data == id;
    }

    @Override
    public void finish() {
        super.finish();
        if (isDialogTheme()) {
            overridePendingTransition(0, 0);
        }
    }

    public void finishFromActivity() {
        super.finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // shaozj add 2014-11-27 begin
        if (isUseOriginalActionBar()) {
            mCyeeActionBar.setCustomMenu(menu);
            return super.onCreateOptionsMenu(menu);
        }
        // shaozj add 2014-11-27 end
        if (mCyeeMagicBar == null) {
            return super.onCreateOptionsMenu(menu);
        }
        if (menu == null || menu.size() <= 0) {
            return true;
        }
        // Gionee <lihq> <2014-1-7> add for CR00958000 begin
        // shaozj mClickFlag = false;
        // Gionee <lihq> <2014-1-7> add for CR00958000 end
        // Gionee <lihq> <2014-6-23> modify for CR00873172 begin
		/*
		if (mCyeeActionBar.getActionMode() == null) {
		    parserMenuIfo(menu);
		}
		*/
        if (mCyeeActionBar != null && mCyeeActionBar.getActionMode() != null) {

        } else {
            // gionee maxw add begin
            // parserMenuIfo(menu);
            mCyeeMagicBar.setMenus(menu);
            // gionee maxw add end
        }
        // Gionee <lihq> <2014-6-23> modify for CR00873172 end
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // shaozj add 2014-11-27 begin
        // Gionee <fenglp> <2013-07-25> modify for CR00812456 begin
        mOptionMenu = menu;
        // Gionee <fenglp> <2013-07-25> modify for CR00812456 end
        if (isUseOriginalActionBar()) {
            mCyeeActionBar.setCustomMenu(menu);
            return super.onPrepareOptionsMenu(menu);
        }
        // shaozj add 2014-11-27 end
        if (mCyeeMagicBar == null) {
            return super.onPrepareOptionsMenu(menu);
        }
        if (menu == null || menu.size() <= 0) {
            // mNoMenuItem = true ;
            return true;
        }
        // Gionee <lihq> <2014-6-23> modify for CR00873172 begin
		/*
		if (mCyeeActionBar.getActionMode() == null) {
		    parserMenuIfo(menu);
		}
		*/
        if (mCyeeActionBar != null && mCyeeActionBar.getActionMode() != null) {

        } else {
            // gionee maxw modify begin
//            parserMenuIfo(menu);
            mCyeeMagicBar.setMenus(menu);
            // gionee maxw modify end
        }
        // Gionee <lihq> <2014-6-23> modify for CR00873172 end
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem != null) {
            Log.v(TAG,
                    "start call--> MenuItem: " + menuItem.getTitle()
                            + " has been Selected" + " And it has subMenu? "
                            + menuItem.hasSubMenu());
        }
        // shaozj add 2014-11-27 begin
        if (isUseOriginalActionBar()) {
            if (mOptionMenu != null) {
                mCyeeActionBar.setCustomMenu(mOptionMenu);
            }
            return super.onOptionsItemSelected(menuItem);
        }
        // shaozj add 2014-11-27 begin
        if (mCyeeMagicBar == null) {
            return super.onOptionsItemSelected(menuItem);
        }
        if (menuItem.hasSubMenu()) {
            final SubMenu subMenu = menuItem.getSubMenu();
            CharSequence[] subMenuTitles = new CharSequence[subMenu.size()];
            final int[] subMenuIds = new int[subMenu.size()];
            for (int i = 0; i < subMenu.size(); i++) {
                subMenuTitles[i] = subMenu.getItem(i).getTitle();
                subMenuIds[i] = subMenu.getItem(i).getItemId();
            }
            new CyeeAlertDialog.Builder(this).setTitle(menuItem.getTitle())
                    .setItems(subMenuTitles, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onOptionsItemSelected(subMenu.findItem(subMenuIds[which]));
                        }
                    }).show();
        }

        // Gionee <fenglp> <2013-07-29> add for CR00812456 begin
        if (mCyeeActionBar != null && mCyeeActionBar.isActionModeShowing()) {
            ActionMode actionMode = mCyeeActionBar.getActionMode();
            if (actionMode != null) {
                android.view.ActionMode.Callback callback = ((cyee.app.CyeeActionBarImpl.ActionModeImpl) actionMode)
                        .getCallback();
                callback.onActionItemClicked(actionMode, menuItem);
            }
        }
        // Gionee <fenglp> <2013-07-29> add for CR00812456 end
        // shaozj add 2014-11-15 begin
        // if(mMenuHeight>mCyeeMagicBar.getItemHeight()) {
        if (isMagicbarExpand()) {
            // shaozj end
            // shaozj mClickFlag = true ;
            // Gionee <gaoj> <2013-11-27> modify for CR00916580 begin
            if (mCyeeMagicBar != null) {
                mCyeeMagicBar.changeListViewVisiable(false);
            }
            // sethandItemClickAnimationEnd();
            // Gionee <gaoj> <2013-11-27> modify for CR00916580 end
        }
        // shaozj add 2014-11-15 end
        if (menuItem != null) {
            Log.v(TAG,
                    "end call--> MenuItem: " + menuItem.getTitle()
                            + " has been Selected" + " And it has subMenu? "
                            + menuItem.hasSubMenu());
        }
        return true;
    }

    public void updateOptionsMenu(Menu menu) {
        if (mCyeeMagicBar == null) {
            return;
        }
        if (menu == null || menu.size() <= 0) {
            return;
        }
        // gionee maxw modify begin
//		parserMenuIfo(menu);
        mCyeeMagicBar.setMenus(menu);
        // gionee maxw modify end
    }

    @Override
    public boolean onMoreItemSelected(View view) {
        Log.v(TAG, "onMoreItemSelected, isMagicbarExpand:" + isMagicbarExpand());
        if (mCyeeActionBar != null) {
            Log.v(TAG, "onMoreItemSelected, isActionModeShowing():" + mCyeeActionBar.isActionModeShowing());
        } else {
            Log.v(TAG, "onMoreItemSelected, mCyeeActionBar is null:");
        }

        if (mCyeeActionBar != null && !isMagicbarExpand()) {
            if (!mCyeeActionBar.isActionModeShowing()) {
                startOptionsMenu();
            }
        }
        handItemClick();
        return true;
    }

    @Override
    public boolean OnTransparentTouch(View v, MotionEvent event) {
        // shaozj mClickFlag = true;
        handItemClick();
        return true;
    }

    @Override
    public boolean onOptionsItemLongClick(MenuItem menuItem) {
        Log.v(TAG, "onOptionsItemLongClicked");
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isUseOriginalActionBar()) {
            Log.d(TAG, "onConfigurationChanged isUseOriginalActionBar");
            if (mCyeeActionBar != null) {
                setOptionsMenuHideMode(true);
            }
            return;
        }

        // Gionee <fenglp> <2013-07-04> add for CR00812456 begin
        if (mCyeeActionBar != null) {
            mCyeeActionBar.onConfigurationChanged(newConfig);
        }
        // Gionee <fenglp> <2013-07-04> add for CR00812456 end
        // gionee maxw add begin
        if (mCyeeMagicBar != null) {
            mCyeeMagicBar.onConfigurationChanged2(newConfig);
            if (mCyeeActionBar != null && mCyeeActionBar.isActionModeShowing()
                    && mCyeeActionBar.isActionModeHasMenu()) {

                mCyeeMagicBar.setMenus(mCyeeActionBar.getActionMode().getMenu());

            } else {
                mCyeeMagicBar.setMenus(mOptionMenu);
            }
        }
        // gionee maxw add end
    }

    public void SetCyeeMagicBarNull() { // add for phone
        if (mCyeeMagicBar != null) {
            mCyeeMagicBar = null;
        }
    }

    public void setOptionsMenuHideMode(boolean is_hide) {
        if (mHideMode != is_hide) {
            mHideMode = is_hide;
            // gionee maxw add begin
            if (mCyeeMagicBar == null) {
                return;
            }
            // gionee maxw add end
            mCyeeMagicBar.setHideMode(mHideMode);
            if (mOptionMenu == null || mOptionMenu.size() < 1) {
                return;
            }
            if (mHideMode) {
                // Gionee <lihq> <2013-12-27> add for CR00978315 begin
                cancelAnimationListener();
                // Gionee <lihq> <2013-12-27> add for CR00978315 end
                // Gionee <gaoj> <2013-11-08> modify for CR00946524 begin
                // setLayoutVisibility(View.GONE);
                // gionee maxw modify begin
                mCyeeMagicBar.setMagicBarVisibilityWithoutAnim(View.GONE);
                // gionee maxw modify end
                // Gionee <gaoj> <2013-11-08> modify for CR00946524 end
            } else {
                // Gionee <gaoj> <2013-10-24> delete for CR00921398 begin
                // mClickFlag = true ;
                // handItemClick();
                // Gionee <gaoj> <2013-10-24> delete for CR00921398 end
                mCyeeMagicBar.setMenus(mOptionMenu);
            }
        }
    }

    // Gionee <lihq> <2013-12-27> add for CR00978315 begin
    private void cancelAnimationListener() {
        if (mTranslateAnimation != null) {
            mTranslateAnimation.setAnimationListener(null);
        }
    }

    // Gionee <lihq> <2013-12-27> add for CR00978315 end

    public void setOptionsMenuHideMode(boolean is_hide, boolean show_again) {
        setOptionsMenuHideMode(is_hide);
        mShowAgain = show_again;

    }

    // gionee maxw modify begin
    private void setLayoutVisibility(int visibility) {
        // Gionee <gaoj> <2013-10-10> modify for CR00916580 begin
        if (mCyeeMagicBar != null) {
            mCyeeMagicBar.setMagicBarVisibilityWithAnim(visibility);
        }
        // Gionee <gaoj> <2013-10-10> modify for CR00916580 end
    }

    // gionee maxw modify end

    public boolean getOptionsMenuHideMode() {
        return mHideMode;
    }

	/* shaozj
	public boolean isOptionsMenuExpand() {
		return mMenuHeight > mCyeeMagicBar.getItemHeight() ? true : false ;
	}*/

    public void setOptionsMenuUnExpand() {
        // shaozj mClickFlag = true ;
        // Gionee <gaoj> <2013-10-10> modify for CR00916580 begin
        // handItemClick();
        // gionee maxw modify begin
        if (mCyeeMagicBar != null) {
            mCyeeMagicBar.setOptionsMenuUnExpand();
        }
        // gionee maxw modify end
        // Gionee <gaoj> <2013-10-10> modify for CR00916580 end
    }

    private void handItemClick() {
        // Gionee <gaoj> <2013-12-12> add for CR00974643 begin
        // shaozj commented begin
        // addMagicBarBgAlphaAnimation();
        // shaozj commented end
        // Gionee <gaoj> <2013-12-12> add for CR00974643 end

        // Gionee <gaoj> <2013-10-10> modify for CR00916580 begin
        // gionee maxw modify begin
        if (mCyeeMagicBar != null) {
            mCyeeMagicBar.changeListViewVisiable(true);
        }
        // gionee maxw modify end
        // Gionee <gaoj> <2013-10-10> modify for CR00916580 end
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mCyeeMagicBar == null) {
            return super.onKeyUp(keyCode, event);
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {

            if (mCyeeActionBar != null && mCyeeActionBar.isActionModeShowing()
                    && !mCyeeActionBar.isActionModeHasMenu()) {
                return true;
            }
            // shaozj begin
            // if (mMenuHeight == mCyeeMagicBar.getItemHeight()) {
            if (!isMagicbarExpand()) {
                // shaozj end
                if (mCyeeActionBar != null && !mCyeeActionBar.isActionModeShowing()) {
                    // Gionee <lihq> <2014-6-24> modify for CR00873172 begin
                    // mCyeeActionBar.startOptionsMenu();
                    startOptionsMenu();
                    // Gionee <lihq> <2014-6-24> modify for CR00873172 end
                }
            }

            if (!mShowAgain) {
                // gionee maxw modify begin
                mCyeeMagicBar.setMagicBarVisibilityWithoutAnim(View.GONE);
                // gionee maxw modify end
                return super.onKeyUp(keyCode, event);
            }

            if (mHideMode) {
                mHideMode = false;
                // Gionee <gaoj> <2013-11-08> delete for CR00946524 begin
                // mClickFlag = true;
                // Gionee <gaoj> <2013-11-08> delete for CR00946524 end
                if (haveOptionsMenu()) {
                    setLayoutVisibility(View.VISIBLE);
                } else {
                    // gionee maxw modify begin
                    mCyeeMagicBar.setMagicBarVisibilityWithoutAnim(View.GONE);
                    // gionee maxw modify end
                }
                // Gionee <gaoj> <2013-11-08> add for CR00946524 begin
                return true;
                // Gionee <gaoj> <2013-11-08> add for CR00946524 end
            } else {
                if (mEmptyLayout.getVisibility() == View.GONE) {
                    return true;
                }
            }

            handItemClick();
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            // shaozj begin
            // if (mMenuHeight > mCyeeMagicBar.getItemHeight()) {
            if (isMagicbarExpand()) {
                // shaozj end
                // shaozj mClickFlag = true;
                // Gionee <gaoj> <2013-10-10> modify for CR00916580 begin
                // handItemClick();
                // gionee maxw modify begin
                if (mCyeeMagicBar != null) {
                    mCyeeMagicBar.changeListViewVisiable(true);
                }
                // gionee maxw modify begin
                // Gionee <gaoj> <2013-10-10> modify for CR00916580 end
                return true;
                // Gionee <fenglp> <2013-07-30> add for CR00812456 begin
            } else if (mCyeeActionBar != null && mCyeeActionBar.isActionModeShowing()) {
                ActionMode actionMode = mCyeeActionBar.getActionMode();
                if (actionMode != null) {
                    actionMode.finish();
                }
                return true;
            } else {
                // Gionee <fenglp> <2013-07-30> add for CR00812456 end
                return super.onKeyUp(keyCode, event);
            }
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    public void parserMenuInfo(Menu menu) {
        if (mCyeeMagicBar == null) {
            return;
        }

        if (menu == null || menu.size() == 0) {
            mCyeeMagicBar.clearMagicBarData();
            return;
        }

        mCyeeMagicBar.setMenus(menu);
    }

    // Gionee <fenglp> <2013-07-04> add for CR00812456 begin
    public CyeeActionBar getCyeeActionBar() {
        initCyeeActionBar();
        return mCyeeActionBar;
    }

    public CyeeActionBar getCyeeActionBar(View viewGroup) {
        if (viewGroup instanceof ViewGroup) {
            mScreenActionBarLayout = (ViewGroup) viewGroup;
        }
        initCyeeActionBar();
        return mCyeeActionBar;
    }


    /**
     * Creates a new ActionBar, locates the inflated ActionBarView, initializes the ActionBar with the view,
     * and sets mActionBar.
     */
    private void initCyeeActionBar() {
        initCyeeActionBar(null);
    }

    private void initCyeeActionBar(View actionbarViewGroup) {
        generalScreenLayout();
        if (mCyeeActionBar == null) {
            mCyeeActionBar = new CyeeActionBarImpl(this, actionbarViewGroup);
            mCyeeActionBar.setActivityContent(mContentLayout);
        }

        if (mFeatureActionBarHide || mThemeActionBarHide) {
            return;
        }

        if (isChild() || (mFeatureActionBarHide || mThemeActionBarHide)) {
            mCyeeActionBar.hide();
        }

        // Gionee <gaoj> <2013-9-6> modify for CR00889318 begin
        hideOriginalActionBar();
        /*
         * ActionBar actionBar = getActionBar(); if(actionBar!=null &&
         * actionBar.isShowing()){ actionBar.hide(); }
         */
        // Gionee <gaoj> <2013-9-6> modify for CR00889318 end
    }


    private void hideOriginalActionBar() {
        if (mActionBar == null) {
            mActionBar = getSupportActionBar();
        }
        if (mActionBar != null && mActionBar.isShowing()) {
            mActionBar.hide();
        }
    }

    // Gionee <fenglp> <2013-07-16> modify for CR00812456 begin
    @Override
    public void setContentView(int layoutResID) {
        // Gionee <lihq> <2014-2-21> delete for CR00873172 begin
        // mIsActionBarHide = isCyeeActionBarHide();
        mThemeActionBarHide = isThemeActionBarHide();
        // Gionee <lihq> <2014-2-21> delete for CR00873172 end
        setContentViewWithCyeeActionBar(layoutResID);
        initCyeeActionBar();
        setGlobalThemeBg();
    }

    @Override
    public void setContentView(View view) {
        // Gionee <lihq> <2014-2-21> delete for CR00873172 begin
        // mIsActionBarHide = isCyeeActionBarHide();
        mThemeActionBarHide = isThemeActionBarHide();
        // Gionee <lihq> <2014-2-21> delete for CR00873172 end
        setContentViewWithCyeeActionBar(view);
        initCyeeActionBar();
        setGlobalThemeBg();
    }

    /**
     * 用于在application里面提前初始化控件布局
     *
     * @param viewGroup     cyeeActivity布局view
     * @param customView    应用布局view
     * @param actionbarView actionbar的容器view
     */
    public void setContentView(View viewGroup, View customView, View actionbarView) {
        if (viewGroup instanceof ViewGroup) {
            mScreenActionBarLayout = (ViewGroup) viewGroup;
        }
        // Gionee <lihq> <2014-2-21> delete for CR00873172 begin
        // mIsActionBarHide = isCyeeActionBarHide();
        mThemeActionBarHide = isThemeActionBarHide();
        // Gionee <lihq> <2014-2-21> delete for CR00873172 end

        // Gionee <lihq> <2014-2-21> add for CR00873172 begin
        mFeatureActionBarHide = isFeatureAcitonBarHide();
        setWindowFeatureNoTitle();
        // Gionee <lihq> <2014-2-21> add for CR00873172 end

        setContentViewWithCyeeActionBar(customView);
        initCyeeActionBar(actionbarView);
        setGlobalThemeBg();
    }


    @Override
    public void setContentView(View view, android.view.ViewGroup.LayoutParams params) {
        // Gionee <lihq> <2014-2-21> delete for CR00873172 begin
        // mIsActionBarHide = isCyeeActionBarHide();
        mThemeActionBarHide = isThemeActionBarHide();
        // Gionee <lihq> <2014-2-21> delete for CR00873172 end
        setContentViewWithCyeeActionBar(view, params);
        initCyeeActionBar();
        setGlobalThemeBg();
    }

    // Gionee <fenglp> <2013-07-16> modify for CR00812456 end

    // Gionee <fenglp> <2013-07-17> add for CR00812456 end
    @Override
    public void addContentView(View view, android.view.ViewGroup.LayoutParams params) {
        addContentViewWithCyeeActionBar(view, params);
        initCyeeActionBar();
    }

    private void addContentViewWithCyeeActionBar(View view, android.view.ViewGroup.LayoutParams params) {
        if (mContentLayout == null) {
            setContentViewWithCyeeActionBar(view, params);
        } else {
            mContentLayout.addView(view, params);
        }
        final Callback cb = getWindow().getCallback();
        // isDestroyed() is supported by 4.2
        // if (cb != null && !isDestroyed()) {
        if (cb != null) {
            cb.onContentChanged();
        }
    }

    // Gionee <fenglp> <2013-07-17> add for CR00812456 end

    private void setContentViewWithCyeeActionBar(int layoutResID) {
        generalScreenLayout();

        mContentLayout = (FrameLayout) mScreenActionBarLayout.findViewById(com.cyee.internal.R.id.cyee_content);
        // Gionee <daizhimin> <2013-07-13> add for CR00833379 begin
        // Gionee <gaoj> <2013-9-6> modify for CR00889318 begin
        mEmptyLayout = (LinearLayout) mScreenActionBarLayout.findViewById(com.cyee.internal.R.id.cyee_overlap);
        // Gionee <gaoj> <2013-9-6> modify for CR00889318 end
        mCyeeMagicBar = (CyeeMagicBar) mScreenActionBarLayout.findViewById(com.cyee.internal.R.id.cyee_magic_bar);
        // gionee maxw add begin
        mCyeeMagicBar.setHideMode(mHideMode);
        // gionee maxw add end
        mCyeeMagicBar.setonOptionsItemSelectedListener(this);
        mCyeeMagicBar.setonMoreItemSelectedListener(this);
        mCyeeMagicBar.setonTransparentTouchListener(this);
        mCyeeMagicBar.setonOptionsItemLongClickListener(this);
        // gionee maxw modify begin
        mCyeeMagicBar.setOnMagicBarVisibleChangedListener(new OnMagicBarVisibleChangedListener() {
            @Override
            public void onMagicBarVisibleChanged(int visibility) {
                mEmptyLayout.setVisibility(visibility);
            }
        });
        // gionee maxw modify end
        // Gionee <daizhimin> <2013-07-13> add for CR00833379 end

        // Gionee <gaoj> <2013-12-12> add for CR00974643 begin
        initMagicBarBgLayout();
        // Gionee <gaoj> <2013-12-12> add for CR00974643 end

        mContentLayout.removeAllViews();
        mLayoutInflater.inflate(layoutResID, mContentLayout);
        getWindow().setContentView(mScreenActionBarLayout);

    }

    // Gionee <fenglp> <2013-07-04> add for CR00812456 end

    // Gionee <fenglp> <2013-07-12> add for CR00812456 begin
    private void setContentViewWithCyeeActionBar(View view) {
        setContentViewWithCyeeActionBar(view, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
    }

    private void setContentViewWithCyeeActionBar(View view, ViewGroup.LayoutParams params) {
        generalScreenLayout();

        mContentLayout = (FrameLayout) mScreenActionBarLayout.findViewById(com.cyee.internal.R.id.cyee_content);
        // Gionee <daizhimin> <2013-07-13> add for CR00833379 begin
        // Gionee <gaoj> <2013-9-6> modify for CR00889318 begin
        mEmptyLayout = (LinearLayout) mScreenActionBarLayout.findViewById(com.cyee.internal.R.id.cyee_overlap);
        // Gionee <gaoj> <2013-9-6> modify for CR00889318 end
        mCyeeMagicBar = (CyeeMagicBar) mScreenActionBarLayout.findViewById(com.cyee.internal.R.id.cyee_magic_bar);
        mCyeeMagicBar.setonOptionsItemSelectedListener(this);
        mCyeeMagicBar.setonMoreItemSelectedListener(this);
        mCyeeMagicBar.setonTransparentTouchListener(this);
        mCyeeMagicBar.setonOptionsItemLongClickListener(this);
        // Gionee <daizhimin> <2013-07-13> add for CR00833379 end

        // gionee maxw modify begin
        mCyeeMagicBar.setOnMagicBarVisibleChangedListener(new OnMagicBarVisibleChangedListener() {
            @Override
            public void onMagicBarVisibleChanged(int visibility) {
                mEmptyLayout.setVisibility(visibility);
            }
        });
        // gionee maxw modify end

        // Gionee <gaoj> <2013-12-12> add for CR00974643 begin
        initMagicBarBgLayout();
        // Gionee <gaoj> <2013-12-12> add for CR00974643 end

        mContentLayout.removeAllViews();
        mContentLayout.addView(view, params);

        getWindow().setContentView(mScreenActionBarLayout);

    }

    // Gionee <fenglp> <2013-07-12> add for CR00812456 end

    // Gionee <fenglp> <2013-07-13> add for CR00812456 begin

    /**
     * Retrieve a reference to this activity's ActionBar.
     *
     * @return The Activity's ActionBar, or null if it does not have one.
     * @deprecated use new getCyeeActionBar()
     */

    // Gionee <fenglp> <2013-07-13> add for CR00812456 end

    // Gionee <lihq> <2014-2-21> add for CR00873172 begin
    private boolean isFeatureAcitonBarHide() {
        // Log.e("", "FEATURE_NO_TITLE");
        if (getWindow().hasFeature(Window.FEATURE_ACTION_BAR)) {
            // Log.e("", "FEATURE_ACTION_BAR");
            return false;
        } else return getWindow().hasFeature(Window.FEATURE_NO_TITLE);
    }

    private boolean isThemeActionBarHide() {
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowNoTitle, outValue, true);
        if (outValue.data != 0) {
            return true;
        }

        getTheme().resolveAttribute(android.R.attr.windowActionBar, outValue, true);
        if (outValue.data != 0) {
        } else {
            return true;
        }

        return false;
    }

    private void setWindowFeatureNoTitle() {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {
        }
    }

    // Gionee <lihq> <2014-2-21> add for CR00873172 end

    // Gionee <fenglp> <2013-07-16> add for CR00812456 begin
    private boolean isCyeeActionBarHide() {
        // Gionee <lihq> <2014-2-21> delete for CR00873172 begin
		/*
		if (getWindow().hasFeature(Window.FEATURE_ACTION_BAR)) {
			Log.e("", "FEATURE_ACTION_BAR");
			return false;
		} else if (getWindow().hasFeature(Window.FEATURE_NO_TITLE)) {
			Log.e("", "FEATURE_NO_TITLE");
			return true;
		}
		*/
        // Gionee <lihq> <2014-2-21> delete for CR00873172 end

        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowNoTitle, outValue, true);
        if (outValue.data != 0) {
            return true;
        }

        getTheme().resolveAttribute(android.R.attr.windowActionBar, outValue, true);
        if (outValue.data != 0) {
        } else {
            return true;
        }

        return false;
    }

    // Gionee <fenglp> <2013-07-16> add for CR00812456 end

    // Gionee <fenglp> <2013-07-17> add for CR00812456 begin

    public View getViewWithCyeeActionBar() {
        return mScreenActionBarLayout;
    }

    // Gionee <fenglp> <2013-07-17> add for CR00812456 end

    // Gionee <fenglp> <2013-07-22> add for CR00812456 begin
    @Override
    public void setTitle(CharSequence title) {
        CyeeActionBar actionBar = getCyeeActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }


    private static final int ACTION_MODE_TYPE_PRIMARY = 0;
    private static final int ACTION_MODE_TYPE_FLOATING = 1;

    @Override
    public ActionMode onWindowStartingActionMode(android.view.ActionMode.Callback callback) {
        if (getActionModeType() == ACTION_MODE_TYPE_FLOATING) {
            return super.onWindowStartingActionMode(callback);
        } else {
            initCyeeActionBar();
            if (mCyeeActionBar != null) {
                return mCyeeActionBar.startActionMode(callback);
            }
            return null;

        }
    }

    private int getActionModeType() {
        Field field = getDeclaredField(this, "mActionModeTypeStarting");
        int type = ACTION_MODE_TYPE_PRIMARY;
        if (field == null) {
            return type;
        }
        try {
            field.setAccessible(true);
            type = field.getInt(this);
        } catch (IllegalAccessException e) {
            Log.d(TAG, "IllegalAccessException");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "IllegalArgumentException");
            e.printStackTrace();
        }

        return type;
    }

    private Field getDeclaredField(Object object, String fieldName) {
        Field field = null;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                return field;
            } catch (Exception e) {
                Log.d(TAG, "getDeclaredField e=" + e.toString());
            }
        }

        return field;
    }

    @Override
    public ActionMode startActionMode(android.view.ActionMode.Callback callback) {
        return onWindowStartingActionMode(callback);
    }

    // Gionee <fenglp> <2013-07-22> add for CR00812456 end

    private boolean mFeatureActionBarHide;
    private boolean mThemeActionBarHide;

    private void generalScreenLayout() {

        if (mScreenActionBarLayout == null) {
            // Gionee <lihq> <2014-2-21> add for CR00873172 begin
            mFeatureActionBarHide = isFeatureAcitonBarHide();
            setWindowFeatureNoTitle();
            // Gionee <lihq> <2014-2-21> add for CR00873172 end
            if (mLayoutInflater == null) {
                mLayoutInflater = LayoutInflater.from(this);
            }
            int layoutResource;
            // Gionee <fenglp> <2013-07-30> modify for CR00812456 begin
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.windowIsFloating, outValue, true);
            if (outValue.data != 0) {
                layoutResource = com.cyee.internal.R.layout.cyee_screen_dialog;
            } else {
                getTheme().resolveAttribute(android.R.attr.windowActionBarOverlay, outValue, true);
                if (outValue.data != 0 || getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY)) {
                    layoutResource = com.cyee.internal.R.layout.cyee_screen_action_bar_overlay;
                    isOverlay = true;
                } else {
                    layoutResource = com.cyee.internal.R.layout.cyee_screen_action_bar;
                }
            }
            // Gionee <fenglp> <2013-07-30> modify for CR00812456 end
            mScreenActionBarLayout = (ViewGroup) mLayoutInflater.inflate(layoutResource, null);
        }
    }

    public Menu getOptionMenu() {
        return mOptionMenu;
    }

    private void initMagicBarBgLayout() {
        mMagicbarBg = (LinearLayout) mScreenActionBarLayout.findViewById(com.cyee.internal.R.id.cyee_magic_bar_bg);
        if (mMagicbarBg != null) {
            mMagicbarBg.setOnTouchListener(magicbarTouchListener);
        }
    }

    View.OnTouchListener magicbarTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // shaozj begin
                    // if (mMenuHeight == mListModeHeight || (mCyeeMagicBar != null
                    // && mMenuHeight == mCyeeMagicBar.getTitleModeHeight())) {
                    if (mCyeeMagicBar != null && isMagicbarExpand()) {
                        // shaozj end
                        OnTransparentTouch(v, event);
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    @Override
    public void invalidateOptionsMenu() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                invalOptionsMenu();
            }

        }, 50);
    }

    private boolean mControlCreate = true;
    private MenuBuilder mMenu;

    public void startOptionsMenu() {
        initMenu();
        if (mControlCreate) {
            mMenu.clear();
            onCreateOptionsMenu(mMenu);
            mControlCreate = false;
        }
        onPrepareOptionsMenu(mMenu);
    }

    public void invalOptionsMenu() {
        initMenu();
        mMenu.clear();
        onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, mMenu);
        onPrepareOptionsMenu(mMenu);
    }

    private void initMenu() {
        if (mMenu == null) {
            mMenu = new MenuBuilder(this).setDefaultShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    private int getCyeeActionBarColor() {
        int color = -1;
        try {
            TypedArray a = this.obtainStyledAttributes(null,
                    R.styleable.CyeeActionBar, com.cyee.internal.R.attr.cyeeactionBarStyle, 0);
            color = a.getColor(R.styleable.CyeeActionBar_cyeebackground, -1);
            a.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return color;
    }

    // shaozj add begin
    public void setStatusBarColor() {
        int color = getCyeeActionBarColor();
        if (ChameleonColorManager.isNeedChangeColor(this)
                && !getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY)) {
            color = ChameleonColorManager.getAppbarColor_A1();
        }
        if (-1 != color) {
            getWindow().setStatusBarColor(color);
        }
    }

    public boolean isUseOriginalActionBar() {
        try {
            TypedArray a = this.obtainStyledAttributes(null, R.styleable.CyeeActionBar, com.cyee.internal.R.attr.cyeeactionBarStyle, 0);
            boolean ret = a.getBoolean(R.styleable.CyeeActionBar_cyeeOptionsMenuAsUp, false);
            a.recycle();

            return ret;
        } catch (Exception e) {
            Log.w("CyeeActivity", "get cyeeOptionMenuAsUp error");
        }
        return false;
    }

    public CyeeMagicBar getCyeeMagicBar() {
        return mCyeeMagicBar;
    }

    private boolean haveOptionsMenu() {
        return mOptionMenu != null && mOptionMenu.size() > 0;
    }

    private boolean isMagicbarExpand() {
        return null != mCyeeMagicBar && mCyeeMagicBar.isExpand();
    }
    // shaozj add end

    @Override
    public Resources getSuperResources() {
        return super.getResources();
    }

    public void setShowVirtualKeyboard(boolean show) {
        this.showVirtualKeyboard = show;
    }
}
