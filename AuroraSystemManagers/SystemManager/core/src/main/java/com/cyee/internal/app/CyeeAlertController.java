package com.cyee.internal.app;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import java.lang.ref.WeakReference;

import cyee.app.CyeeAlertDialog;
import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeCheckedTextView;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeWidgetResource;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CyeeAlertController {

    private static final String TAG = "CyeeAlertController";

    private final Context mContext;
    private final DialogInterface mDialogInterface;
    private final Window mWindow;

    private CharSequence mTitle;

    private CharSequence mMessage;

    private ListView mListView;

    private View mView;

    private int mViewLayoutResId;
    
    private int mViewSpacingLeft;
    
    private int mViewSpacingTop;
    
    private int mViewSpacingRight;
    
    private int mViewSpacingBottom;
    
    private boolean mViewSpacingSpecified = false;
    
    private CyeeButton mButtonPositive;

    private CharSequence mButtonPositiveText;

    private Message mButtonPositiveMessage;

    private CyeeButton mButtonNegative;

    private CharSequence mButtonNegativeText;

    private Message mButtonNegativeMessage;

    private CyeeButton mButtonNeutral;

    private CharSequence mButtonNeutralText;

    private Message mButtonNeutralMessage;

    private ScrollView mScrollView;
    
    private int mIconId = 0;
    
    private Drawable mIcon;
    
    private ImageView mIconView;
    
    private TextView mTitleView;

    private TextView mMessageView;

    private View mCustomTitleView;
    
    private boolean mForceInverseBackground;
    
    private ListAdapter mAdapter;
    
    private int mCheckedItem = -1;

    private final int mAlertDialogLayout;
    private final int mListLayout;
    private final int mMultiChoiceItemLayout;
    private final int mSingleChoiceItemLayout;
    private final int mListItemLayout;
    private final int mStrongHintLayout;
    private boolean mIsStrongHint;
    private final Handler mHandler;

    /*weidong begin*/
    private int mPositiveColor = 0;
    /*weidong end*/
    // Gionee zhangxx 2012-11-01 add for CR00715173 begin
    private boolean mHasCancelIconButton = true;
    private ImageButton mButtonCancel = null;
    // Gionee zhangxx 2012-11-01 add for CR00715173 end
    
    // Gionee <zhangxx><2013-05-15> add for CR00811583 begin
    // gionee widget 3.0 support
    private static boolean mIsGnWidget3Style = false;
    private int mButtonPositiveStyle;
    private int mButtonNeutralStyle;
    // Gionee <zhangxx><2013-05-15> add for CR00811583 end

    // Gionee <gaoj> <2013-9-16> add for CR00899138 begin
    private final int mAlertDialogMaxHeight;
    // Gionee <gaoj> <2013-9-16> add for CR00899138 end
    private final int mContextMenuDialogMaxHeight;
    
    private final int mWidth;
    
    private View mWindowView;
    private final int mSingleDialogMaxHeight;
    
    private final int mMostDisplayCount;
    private final int mCornerRadius;
    
    private final int mFullDark;
    private final int mTopDark;
    private final int mCenterDark;
    private final int mBottomDark;
    private final int mFullBright;
    private final int mTopBright;
    private final int mCenterBright;
    private final int mBottomBright;
    private final int mBottomMedium;
    
    View.OnClickListener mButtonHandler = new View.OnClickListener() {
        public void onClick(View v) {
            Message m = null;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else if (v == mButtonNeutral && mButtonNeutralMessage != null) {
                m = Message.obtain(mButtonNeutralMessage);
            }
            // Gionee zhangxx 2012-11-01 add for CR00715173 begin
            else if (v == mButtonCancel && mDialogInterface != null) {
                mDialogInterface.cancel();
            }
            // Gionee zhangxx 2012-11-01 add for CR00715173 end
            if (m != null) {
                m.sendToTarget();
            }

            // Post a message so we dismiss after the above handlers are executed
            // weidong begin
            mHandler.sendMessage(mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialogInterface));
            // weidong end
            
        }
    };    

    private static final class ButtonHandler extends Handler {
        // CyeeButton clicks have Message.what as the BUTTON{1,2,3} constant
        private static final int MSG_DISMISS_DIALOG = 1;

        private final WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<DialogInterface>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.v(TAG, "click performed : " + msg.what);
            switch (msg.what) {
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;
                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        /*TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(com.android.internal.R.attr.alertDialogCenterButtons,
                outValue, true);
        return outValue.data != 0;*/
        /*
         * 
         * Theme-alertDialogCenterButtons : true
         * Theme.Holo-alertDialogCenterButtons : false
         * Theme.Holo.Light-alertDialogCenterButtons : false
         * so return false*/
        return false;
    }

    public CyeeAlertController(Context context, DialogInterface di, Window window) {
        if(context instanceof ContextThemeWrapper) {
            mContext = ((ContextThemeWrapper)context).getBaseContext();
        } else {
            mContext = context;
        }
        
        mDialogInterface = di;
        mWindow = window;
        mHandler = new ButtonHandler(di);

        TypedArray a = context.obtainStyledAttributes(null,
                R.styleable.CyeeAlertDialog,
                com.cyee.internal.R.attr.cyeealertDialogStyle, 0);
        TypedArray array = context.obtainStyledAttributes(null,
                new int [] {com.android.internal.R.attr.layout_width},
                com.cyee.internal.R.attr.cyeealertDialogStyle, 0);

        mWidth = array.getLayoutDimension(0,
                WindowManager.LayoutParams.MATCH_PARENT);
        array.recycle();
        mAlertDialogLayout = a.getResourceId(R.styleable.CyeeAlertDialog_cyeelayout,
                com.cyee.internal.R.layout.cyee_alert_dialog_light);
        mListLayout = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeelistLayout,
                com.cyee.internal.R.layout.cyee_select_dialog);
        
        int multiResId = com.cyee.internal.R.layout.cyee_select_dialog_multichoice;
        int singleResId = com.cyee.internal.R.layout.cyee_select_dialog_singlechoice;
        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            multiResId = com.cyee.internal.R.layout.cyee_global_theme_select_dialog_multichoice;
            singleResId = com.cyee.internal.R.layout.cyee_global_theme_select_dialog_singlechoice;
        }
        mMultiChoiceItemLayout = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeemultiChoiceItemLayout,
                multiResId);
        mSingleChoiceItemLayout = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeesingleChoiceItemLayout,
                singleResId);
        
        mListItemLayout = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeelistItemLayout,
                com.cyee.internal.R.layout.cyee_select_dialog_item);

        mStrongHintLayout = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeeStrongHintDialogLayout,
                com.cyee.internal.R.layout.cyee_strong_hint_dialog);
        
        // Gionee <gaoj> <2013-9-16> add for CR00899138 begin
        mAlertDialogMaxHeight = (int) mContext.getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_alert_dialog_list_maxheight);
        // Gionee <gaoj> <2013-9-16> add for CR00899138 end
        /*weidong begin*/
        mContextMenuDialogMaxHeight = (int) mContext.getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_context_menu_list_maxheight);
        mPositiveColor = a.getResourceId(R.styleable.CyeeAlertDialog_cyeeDialogPostiveBtnColor,
                com.android.internal.R.color.black);
        mSingleDialogMaxHeight = (int) mContext.getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_single_alert_dialog_list_maxheight);
        /*weidong end*/
        mMostDisplayCount = mContext.getResources().getInteger(
                com.cyee.internal.R.integer.cyeemostDisplayItemCount);
        
        mFullDark = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeefullDark, android.R.color.white);
        mTopDark = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeetopDark, android.R.color.white);
        mCenterDark = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeecenterDark, android.R.color.white);
        mBottomDark = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeebottomDark, android.R.color.white);
        mFullBright = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeefullBright, android.R.color.white);
        mTopBright = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeetopBright, android.R.color.white);
        mCenterBright = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeecenterBright, android.R.color.white);
        mBottomBright = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeebottomBright, android.R.color.white);
        mBottomMedium = a.getResourceId(
                R.styleable.CyeeAlertDialog_cyeebottomMedium, android.R.color.white);     
        
        a.recycle();
        
        mCornerRadius = mContext.getResources().getDimensionPixelSize(com.cyee.internal.R.dimen.cyee_magicbar_bg_corner);
    }
    
    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        
        ViewGroup vg = (ViewGroup)v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void setStrongHint(boolean strongHint) {
        mIsStrongHint = strongHint;
    }
    
    public void installContent() {
        /* We use a custom title so never request a window title */
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        
        if (mView == null || !canTextInput(mView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }

        if (mIsStrongHint) {
            mWindow.setContentView(mStrongHintLayout);
        } else {
            mWindow.setContentView(mAlertDialogLayout);
        }
        
        // Gionee <gaoj> <2013-9-12> modify for CR00899138 begin
        if (mContext.getThemeResId() != com.cyee.internal.R.style.Theme_Cyee_Dialog_Alert_FullScreen) {
            // Gionee <lihq> <2013-9-2> modify for CR00873172 begin
            WindowManager.LayoutParams l = mWindow.getAttributes();
            l.width = mWidth;
            // Gionee <lihq> <2013-9-2> modify for CR00873172 end
        }
        // Gionee <gaoj> <2013-9-12> modify for CR00899138 begin
        setupView();
        if (mIsStrongHint) {
            strongHintDialogWindow();
        }
    }

    private void strongHintDialogWindow() {
        WindowManager.LayoutParams params = mWindow.getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        params.alpha = 0.8f;
        mWindow.setAttributes(params);
    }
    
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    /**
     * @see CyeeAlertDialog.Builder#setCustomTitle(View)
     */
    public void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
    }
    
    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }

    public void setView(int layoutResId) {
        mView = null;
        mViewLayoutResId = layoutResId;
        mViewSpacingSpecified = false;
    }
    
    /**
     * Set the view to display in the dialog.
     */
    public void setView(View view) {
        mView = view;
        mViewLayoutResId = 0;
        mViewSpacingSpecified = false;
    }
    
    /**
     * Set the view to display in the dialog along with the spacing around that view
     */
    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight,
            int viewSpacingBottom) {
        mView = view;
        mViewLayoutResId = 0;
        mViewSpacingSpecified = true;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
    }

    /**
     * Sets a click listener or a message to be sent when the button is clicked.
     * You only need to pass one of {@code listener} or {@code msg}.
     * 
     * @param whichButton Which button, can be one of
     *            {@link DialogInterface#BUTTON_POSITIVE},
     *            {@link DialogInterface#BUTTON_NEGATIVE}, or
     *            {@link DialogInterface#BUTTON_NEUTRAL}
     * @param text The text to display in positive button.
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     * @param msg The {@link Message} to be sent when clicked.
     */
    public void setButton(int whichButton, CharSequence text,
            DialogInterface.OnClickListener listener, Message msg) {
        
        // Gionee <zhangxx><2013-05-15> add for CR00811583 begin
        if (mIsGnWidget3Style) {
            setHasCancelIcon(false);
        }
        // Gionee <zhangxx><2013-05-15> add for CR00811583 end

        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }
        
        switch (whichButton) {

            case DialogInterface.BUTTON_POSITIVE:
                mButtonPositiveText = text;
                mButtonPositiveMessage = msg;
                break;
                
            case DialogInterface.BUTTON_NEGATIVE:
                mButtonNegativeText = text;
                mButtonNegativeMessage = msg;
                break;
                
            case DialogInterface.BUTTON_NEUTRAL:
                mButtonNeutralText = text;
                mButtonNeutralMessage = msg;
                break;
                
            default:
                throw new IllegalArgumentException("CyeeButton does not exist");
        }
    }

    /**
     * Set resId to 0 if you don't want an icon.
     * @param resId the resourceId of the drawable to use as the icon or 0
     * if you don't want an icon.
     */
    public void setIcon(int resId) {
        mIcon = null;
        mIconId = resId;
        if (mIconView != null) {
            if (resId != 0) {
                mIconView.setImageResource(mIconId);
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
    }
    
    public void setIcon(Drawable icon) {
        mIcon = icon;
        mIconId = 0;
        if (mIconView != null) {
            if (mIcon != null) {
                mIconView.setImageDrawable(icon);
            } else {
                mIconView.setVisibility(View.GONE);                
            }
        }
    }

    /**
     * @param attrId the attributeId of the theme-specific drawable
     * to resolve the resourceId for.
     *
     * @return resId the resourceId of the theme-specific drawable
     */
    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        mForceInverseBackground = forceInverseBackground;
    }
    
    public ListView getListView() {
        return mListView;
    }
    
    public CyeeButton getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonNeutral;
            default:
                return null;
        }
    }
    
    private boolean holdByStrongHint(int keyCode) {
        return mIsStrongHint;
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event) || holdByStrongHint(keyCode);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event) || holdByStrongHint(keyCode);
    }
    
    private void setupView() {
        mWindowView = mWindow.findViewById(com.cyee.internal.R.id.cyee_parentPanel);
        FrameLayout customPanel = (FrameLayout) mWindow.findViewById(com.cyee.internal.R.id.cyee_customPanel);
        final View customView;
        if (mView != null) {
            customView = mView;
        } else if (mViewLayoutResId != 0) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            customView = inflater.inflate(mViewLayoutResId, customPanel, false);
        }else {
            customView = null;
        }
        final boolean hasCustomView = customView != null;


        LinearLayout contentPanel = (LinearLayout) mWindow.findViewById(com.cyee.internal.R.id.cyee_contentPanel);
        setupContent(contentPanel);
        boolean hasButtons = setupButtons();
        
        LinearLayout topPanel = (LinearLayout) mWindow.findViewById(com.cyee.internal.R.id.cyee_topPanel);
        boolean hasTitle = setupTitle(topPanel);
            
        View buttonPanel = mWindow.findViewById(com.cyee.internal.R.id.cyee_buttonPanel);
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
            // mWindow.setCloseOnTouchOutsideIfNotSet(true);
        }

        if (hasCustomView) {
            FrameLayout custom = (FrameLayout) mWindow.findViewById(com.cyee.internal.R.id.cyee_custom);
            custom.addView(customView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            if (mViewSpacingSpecified) {
                custom.setPadding(mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                        mViewSpacingBottom);
            }
            if (mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0;
            }
        } else {
            mWindow.findViewById(com.cyee.internal.R.id.cyee_customPanel).setVisibility(View.GONE);
        }
        
        /* Only display the divider if we have a title and a 
         * custom view or a message.
         */
        if (hasTitle) {
            View divider = null;
            if (mMessage != null || customView != null || mListView != null) {
                divider = mWindow.findViewById(com.cyee.internal.R.id.cyee_titleDivider);
            } else {
                divider = mWindow.findViewById(com.cyee.internal.R.id.cyee_titleDividerTop);
            }
            ///M: for theme manager feature @{
            /*if (FeatureOption.MTK_THEMEMANAGER_APP) {
                Resources res = mContext.getResources();
                int textColor = res.getThemeMainColor();
                if (textColor != 0) {
                    if (divider != null) {
                        divider.setBackgroundColor(textColor);
                    }
                }
            }*/
            ///M @}
            /*weidong begin*/
            /*if (divider != null) {
                divider.setVisibility(View.VISIBLE);
            }*/
            /*weidong end*/
            // Gionee zhangxx 2012-11-01 add for CR00715173 begin
            mButtonCancel = (ImageButton) mWindow.findViewById(com.cyee.internal.R.id.cyee_cancel);
            /*weidong begin*/
            /*if (mHasCancelIconButton && mButtonCancel != null) {
                mButtonCancel.setOnClickListener(mButtonHandler);
                mButtonCancel.setVisibility(View.VISIBLE);
            } else if (mButtonCancel != null) {
                mButtonCancel.setVisibility(View.GONE);
            }*/
            /*weidong end*/
            // Gionee zhangxx 2012-11-01 add for CR00715173 end
        }
        if (!mIsStrongHint) {
            setBackground(topPanel, contentPanel, customPanel, hasButtons, null, hasTitle, buttonPanel);
        }
        
        changeColor(topPanel, contentPanel, customPanel,buttonPanel);
    }

    // feihm-begin
    public void changeColor(LinearLayout topPanel, LinearLayout contentPanel,
            FrameLayout customPanel, View buttonPanel) {

        boolean needChangeColor = ChameleonColorManager
                .isNeedChangeColor(mContext);

        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Context cxt = mContext;
            if (mContext instanceof ContextThemeWrapper) {
                cxt = ((ContextThemeWrapper) mContext).getBaseContext();
            }
            Resources iCyeeRes = cxt.getResources();
            mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            
            Drawable bgDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_dialog_bg);
            if (null != mWindowView) {
                mWindowView.setBackground(bgDrawable);
            }
            if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                if (mCustomTitleView == null && !TextUtils.isEmpty(mTitle)) {
                    mTitleView.setTextColor(ChameleonColorManager
                            .getAccentColor_G1());
                }
                if (mScrollView != null && mMessageView != null) {
                    mMessageView.setTextColor(ChameleonColorManager
                            .getContentColorPrimaryOnBackgroud_C1());
                }
                if (!TextUtils.isEmpty(mButtonPositiveText)) {
                    mButtonPositive.setTextColor(ChameleonColorManager
                            .getAccentColor_G1());
                }
            }
            topPanel.setBackgroundColor(Color.TRANSPARENT);
            contentPanel.setBackgroundColor(Color.TRANSPARENT);
            buttonPanel.setBackgroundColor(Color.TRANSPARENT);

            if (customPanel != null) {
                customPanel.setBackgroundColor(Color.TRANSPARENT);
            }
        } else if (needChangeColor) {
            if (mCustomTitleView == null && !TextUtils.isEmpty(mTitle)) {
                mTitleView.setTextColor(ChameleonColorManager
                        .getAccentColor_G1());
            }
            if (mScrollView != null && mMessageView != null) {
                mMessageView.setTextColor(ChameleonColorManager
                        .getContentColorPrimaryOnBackgroud_C1());
            }

            Drawable winBackground = mWindow.getDecorView().getBackground();
            if (null != winBackground) {
                winBackground.setColorFilter(ChameleonColorManager.getPopupBackgroudColor_B2(), Mode.SRC_IN);
            }
            
            if (null != topPanel && null != topPanel.getBackground()) {
                topPanel.getBackground().setColorFilter(ChameleonColorManager
                        .getPopupBackgroudColor_B2(), Mode.SRC_IN);
            }
            if (null != contentPanel && null != contentPanel.getBackground()) {
                contentPanel.getBackground().setColorFilter(ChameleonColorManager
                        .getPopupBackgroudColor_B2(), Mode.SRC_IN);
            }
            if (null != buttonPanel && null != buttonPanel.getBackground()) {
                buttonPanel.getBackground().setColorFilter(ChameleonColorManager
                        .getPopupBackgroudColor_B2(), Mode.SRC_IN);
            }
            if (customPanel != null && null != customPanel.getBackground()) {
                customPanel.getBackground().setColorFilter(ChameleonColorManager
                        .getPopupBackgroudColor_B2(), Mode.SRC_IN);
            }

            // feihm mButtonPositive
            if (!TextUtils.isEmpty(mButtonPositiveText)) {
                mButtonPositive.setTextColor(ChameleonColorManager
                        .getAccentColor_G1());
            }
        }
    }
    // feihm-end

    private boolean setupTitle(LinearLayout topPanel) {
        boolean hasTitle = true;
        
        if (mCustomTitleView != null) {
            // Add the custom title view directly to the topPanel layout
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            topPanel.addView(mCustomTitleView, 0, lp);
            
            // Hide the title template
            View titleTemplate = mWindow.findViewById(com.cyee.internal.R.id.cyee_title_template);
            titleTemplate.setVisibility(View.GONE);
        } else {
            final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
            
            mIconView = (ImageView) mWindow.findViewById(com.cyee.internal.R.id.cyee_icon);
            if (hasTextTitle) {
                /* Display the title if a title is supplied, else hide it */
                mTitleView = (TextView) mWindow.findViewById(com.cyee.internal.R.id.cyee_alertTitle);

                mTitleView.setText(mTitle);
                ///M: for theme manager feature @{
                /*if (FeatureOption.MTK_THEMEMANAGER_APP) {
                    Resources res = mContext.getResources();
                    int textColor = res.getThemeMainColor();
                    if (textColor != 0) {
                        mTitleView.setTextColor(textColor);
                    }
                }*/
               ///M: @}
                
                /* Do this last so that if the user has supplied any
                 * icons we use them instead of the default ones. If the
                 * user has specified 0 then make it disappear.
                 */
                if (mIconId != 0) {
                    mIconView.setImageResource(mIconId);
                } else if (mIcon != null) {
                    mIconView.setImageDrawable(mIcon);
                } else {
                    
                    /* Apply the padding from the icon to ensure the
                     * title is aligned correctly.
                     */
                    // Gionee <gaoj> <2013-9-18> delete for CR00899138 begin
                    /*mTitleView.setPadding(mIconView.getPaddingLeft(),
                            mIconView.getPaddingTop(),
                            mIconView.getPaddingRight(),
                            mIconView.getPaddingBottom());*/
                    // Gionee <gaoj> <2013-9-18> delete for CR00899138 end
                    mIconView.setVisibility(View.GONE);
                }
            } else {
                
                // Hide the title template
                View titleTemplate = mWindow.findViewById(com.cyee.internal.R.id.cyee_title_template);
                titleTemplate.setVisibility(View.GONE);
                mIconView.setVisibility(View.GONE);
                topPanel.setVisibility(View.GONE);
                hasTitle = false;
            }
        }
        return hasTitle;
    }

    private void setupContent(LinearLayout contentPanel) {
        mScrollView = (ScrollView) mWindow.findViewById(com.cyee.internal.R.id.cyee_scrollView);
        // Gionee <zenggz><2013-05-31> add for CR00819632 begin
        if (mScrollView == null){
            return;
        }
        // Gionee <zenggz><2013-05-31> add for CR00819632 end
        mScrollView.setFocusable(false);
        
        // Special case for users that only want to display a String
        mMessageView = (TextView) mWindow.findViewById(com.cyee.internal.R.id.cyee_message);
        if (mMessageView == null) {
            return;
        }
        
        if (mMessage != null) {
            contentPanel.setMinimumHeight((int)mContext.getResources().getDimension(com.cyee.internal.R.dimen.cyee_alert_dialog_msg_min_height));
            mMessageView.setText(mMessage);
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);
            
            if (mListView != null) {
                contentPanel.removeView(mWindow.findViewById(com.cyee.internal.R.id.cyee_scrollView));
                contentPanel.addView(mListView,
                        new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

                // Gionee <gaoj> <2013-9-16> add for CR00899138 begin
                if (mAdapter.getCount() > mMostDisplayCount) {
                    // Gionee <weidong> <2015-4-2> modify for CR01461208 begin
                    int listHeight = 0;
                    if (mListView.getChoiceMode() == AbsListView.CHOICE_MODE_NONE) {
                        listHeight = mContextMenuDialogMaxHeight;
                    } else if (mListView.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) {
                        listHeight = mSingleDialogMaxHeight;
                    } else {
                        listHeight = mAlertDialogMaxHeight;
                    }
                    contentPanel
                            .setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, listHeight, 1.0f));
                    // Gionee <weidong> <2015-4-2> modify for CR01461208 end
                } else {
                    contentPanel.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
                }
                // Gionee <gaoj> <2013-9-16> add for CR00899138 end
            } else {
                contentPanel.setVisibility(View.GONE);
            }
        }
    }

    private final static int BIT_BUTTON_POSITIVE = 1;
    private final static int BIT_BUTTON_NEGATIVE = 4;
    private final static int BIT_BUTTON_NEUTRAL = 2;
    
    private boolean setupButtons() {
        int whichButtons = 0;
        mButtonPositive = (CyeeButton) mWindow.findViewById(com.cyee.internal.R.id.cyee_button1);
        mButtonPositive.setOnClickListener(mButtonHandler);
        LinearLayout layout_btn1 = (LinearLayout) mWindow.findViewById(com.cyee.internal.R.id.cyee_dialog_button1);
        
        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setVisibility(View.GONE);
            layout_btn1.setVisibility(View.GONE);
        } else {
            /*weidong begin*/
            mButtonPositive.setTextColor(mContext.getResources().getColor(mPositiveColor));
            /*weidong end*/
            mButtonPositive.setText(mButtonPositiveText);
            mButtonPositive.setVisibility(View.VISIBLE);
            layout_btn1.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        mButtonNegative = (CyeeButton) mWindow.findViewById(com.cyee.internal.R.id.cyee_button2);
        mButtonNegative.setOnClickListener(mButtonHandler);
        LinearLayout layout_btn2 = (LinearLayout) mWindow.findViewById(com.cyee.internal.R.id.cyee_dialog_button2);
        
        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setVisibility(View.GONE);
            layout_btn2.setVisibility(View.GONE);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            mButtonNegative.setVisibility(View.VISIBLE);
            layout_btn2.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }

        mButtonNeutral = (CyeeButton) mWindow.findViewById(com.cyee.internal.R.id.cyee_button3);
        mButtonNeutral.setOnClickListener(mButtonHandler);
        LinearLayout layout_btn3 = (LinearLayout) mWindow.findViewById(com.cyee.internal.R.id.cyee_dialog_button3);

        if (TextUtils.isEmpty(mButtonNeutralText)) {
            mButtonNeutral.setVisibility(View.GONE);
            layout_btn3.setVisibility(View.GONE);
        } else {
            mButtonNeutral.setText(mButtonNeutralText);
            mButtonNeutral.setVisibility(View.VISIBLE);
            layout_btn3.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }
        
        View btn3_divider = mWindow.findViewById(com.cyee.internal.R.id.cyee_btn3_divider);
        View btn2_divider = mWindow.findViewById(com.cyee.internal.R.id.cyee_btn2_divider);

        btn3_divider.setVisibility(View.VISIBLE);
        if (whichButtons == 0b101) {
           // setButtonCorner(mButtonPositive,BIT_BUTTON_POSITIVE);
          //  setButtonCorner(mButtonNegative,BIT_BUTTON_NEGATIVE);
        } else if (whichButtons == 0b011) {
           // setButtonCorner(mButtonPositive,BIT_BUTTON_POSITIVE);
          //  setButtonCorner(mButtonNeutral,BIT_BUTTON_NEGATIVE);
        } else if (whichButtons == 0b110) {
           // setButtonCorner(mButtonNegative,BIT_BUTTON_NEGATIVE);
           // setButtonCorner(mButtonNeutral,BIT_BUTTON_POSITIVE);
        } else if (whichButtons == 0b111) {
            btn2_divider.setVisibility(View.VISIBLE);
           // setButtonCorner(mButtonPositive,BIT_BUTTON_POSITIVE);
            //setButtonCorner(mButtonNegative,BIT_BUTTON_NEGATIVE);
        } else {
            //setButtonCorner(mButtonPositive,BIT_BUTTON_NEUTRAL);
           // setButtonCorner(mButtonNegative,BIT_BUTTON_NEUTRAL);
            //setButtonCorner(mButtonNeutral,BIT_BUTTON_NEUTRAL);
            btn3_divider.setVisibility(View.GONE);
        }
        
        // Gionee <zhangxx> <2013-05-31> add for CR00811583 begin
        if (mIsGnWidget3Style) {
            setupButtonStyle();
        }
        // Gionee <zhangxx> <2013-05-31> add for CR00811583 end

        if (shouldCenterSingleButton(mContext)) {
            /*
             * If we only have 1 button it should be centered on the layout and
             * expand to fill 50% of the available space.
             */
            if (whichButtons == BIT_BUTTON_POSITIVE) {
                centerButton(mButtonPositive);
            } else if (whichButtons == BIT_BUTTON_NEGATIVE) {
                centerButton(mButtonNeutral);
            } else if (whichButtons == BIT_BUTTON_NEUTRAL) {
                centerButton(mButtonNeutral);
            }
        }
        
        return whichButtons != 0;
    }

    private void setButtonCorner(CyeeButton button, int which) {
        if (null == button) {
            return ;
        }
        Drawable drawable = button.getBackground();
        GradientDrawable gdrawable = null;
        if (drawable instanceof RippleDrawable) {
            RippleDrawable rp = (RippleDrawable) button.getBackground();
            drawable = rp.getDrawable(0).getCurrent();
            if (drawable instanceof InsetDrawable) {
                InsetDrawable inset = (InsetDrawable) drawable;
                drawable = inset.getDrawable();
                if (drawable instanceof GradientDrawable) {
                    gdrawable = (GradientDrawable) drawable;
                }
            }
        }
        if (null == gdrawable) {
            return ;
        }
        
        if (which == BIT_BUTTON_POSITIVE) {
            float[] radii = { 0, 0, 0, 0, mCornerRadius, mCornerRadius, 0, 0 };
            gdrawable.setCornerRadii(radii);
        } else if (which == BIT_BUTTON_NEGATIVE) {
            float[] radii = { 0, 0, 0, 0, 0, 0, mCornerRadius, mCornerRadius };
            gdrawable.setCornerRadii(radii);
        } else if (which == BIT_BUTTON_NEUTRAL) {
            float[] radii = { 0, 0, 0, 0, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius };
            gdrawable.setCornerRadii(radii);
        }
    }
    
    private void centerButton(CyeeButton button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.weight = 0.5f;
        button.setLayoutParams(params);
        /*alert_dialog_holo.xml have not leftSpacer & rightSpacer */
        /*View leftSpacer = mWindow.findViewById(R.id.cyee_leftSpacer);
        if (leftSpacer != null) {
            leftSpacer.setVisibility(View.VISIBLE);
        }
        View rightSpacer = mWindow.findViewById(R.id.cyee_rightSpacer);
        if (rightSpacer != null) {
            rightSpacer.setVisibility(View.VISIBLE);
        }*/
    }

    private void setBackground(LinearLayout topPanel, LinearLayout contentPanel,
            View customPanel, boolean hasButtons, TypedArray a, boolean hasTitle, 
            View buttonPanel) {
        /* Get all the different background required */
        /*
         * We now set the background of all of the sections of the alert.
         * First collect together each section that is being displayed along
         * with whether it is on a light or dark background, then run through
         * them setting their backgrounds.  This is complicated because we need
         * to correctly use the full, top, middle, and bottom graphics depending
         * on how many views they are and where they appear.
         */
        
        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;
        
        int pos = 0;
        if (hasTitle) {
            views[pos] = topPanel;
            light[pos] = false;
            pos++;
        }
        
        /* The contentPanel displays either a custom text message or
         * a ListView. If it's text we should use the dark background
         * for ListView we should use the light background. If neither
         * are there the contentPanel will be hidden so set it as null.
         */
        views[pos] = (contentPanel.getVisibility() == View.GONE) 
                ? null : contentPanel;
        light[pos] = mListView != null;
        pos++;
        if (customPanel != null) {
            views[pos] = customPanel;
            light[pos] = mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
            light[pos] = true;
        }
        
        boolean setView = false;
        for (pos=0; pos<views.length; pos++) {
            View v = views[pos];
            if (v == null) {
                continue;
            }
            if (lastView != null) {
                if (!setView) {
                    lastView.setBackgroundResource(lastLight ? mTopBright : mTopDark);
                } else {
                    lastView.setBackgroundResource(lastLight ? mCenterBright : mCenterDark);
                }
                setView = true;
            }
            lastView = v;
            lastLight = light[pos];
        }
        
        if (lastView != null) {
            if (setView) {
                
                /* ListViews will use the Bright background but buttons use
                 * the Medium background.
                 */ 
                lastView.setBackgroundResource(
                        lastLight ? (hasButtons ? mBottomMedium : mBottomBright) : mBottomDark);
            } else {
                lastView.setBackgroundResource(lastLight ? mFullBright : mFullDark);
            }
        }
        
        /* TODO: uncomment section below. The logic for this should be if 
         * it's a Contextual menu being displayed AND only a Cancel button 
         * is shown then do this.
         */
//        if (hasButtons && (mListView != null)) {
            
            /* Yet another *special* case. If there is a ListView with buttons
             * don't put the buttons on the bottom but instead put them in the
             * footer of the ListView this will allow more items to be
             * displayed.
             */
            
            /*
            contentPanel.setBackgroundResource(bottomBright);
            buttonPanel.setBackgroundResource(centerMedium);
            ViewGroup parent = (ViewGroup) mWindow.findViewById(R.id.cyee_parentPanel);
            parent.removeView(buttonPanel);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT, 
                    AbsListView.LayoutParams.MATCH_PARENT);
            buttonPanel.setLayoutParams(params);
            mListView.addFooterView(buttonPanel);
            */
//        }
        
        if ((mListView != null) && (mAdapter != null)) {
            mListView.setAdapter(mAdapter);
            if (mCheckedItem > -1) {
                mListView.setItemChecked(mCheckedItem, true);
                mListView.setSelection(mCheckedItem);
            }
        }
    }

    public static class RecycleListView extends CyeeListView {
        boolean mRecycleOnMeasure = true;

        public RecycleListView(Context context) {
            super(context);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

       /* @Override
        protected boolean recycleOnMeasure() {
            return mRecycleOnMeasure;
        }*/
    }

    public static class AlertParams {
        public final Context mContext;
        public final LayoutInflater mInflater;
        
        public int mIconId = 0;
        public Drawable mIcon;
        public int mIconAttrId = 0;
        public CharSequence mTitle;
        public View mCustomTitleView;
        public CharSequence mMessage;
        public CharSequence mPositiveButtonText;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNeutralButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public boolean mCancelable;
        public DialogInterface.OnCancelListener mOnCancelListener;
        public DialogInterface.OnDismissListener mOnDismissListener;
        public DialogInterface.OnKeyListener mOnKeyListener;
        public CharSequence[] mItems;
        public ListAdapter mAdapter;
        public DialogInterface.OnClickListener mOnClickListener;
        public View mView;
        public int mViewLayoutResId;
        public int mViewSpacingLeft;
        public int mViewSpacingTop;
        public int mViewSpacingRight;
        public int mViewSpacingBottom;
        public boolean mViewSpacingSpecified = false;
        public boolean[] mCheckedItems;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public int mCheckedItem = -1;
        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        public Cursor mCursor;
        public String mLabelColumn;
        public String mIsCheckedColumn;
        public boolean mForceInverseBackground;
        public AdapterView.OnItemSelectedListener mOnItemSelectedListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public boolean mRecycleOnMeasure = true;
        // Gionee zhangxx 2012-11-01 add for CR00715173 begin 
        public boolean mHasCancelIcon = true;
        public Drawable mCancelIcon = null;
        // Gionee zhangxx 2012-11-01 add for CR00715173 end
        // Gionee <zhangxx> <2013-05-31> add for CR00811583 begin
        public int mPositiveButtonStyle;
        public int mNeutralButtonStyle;
        // Gionee <zhangxx> <2013-05-31> add for CR00811583 end

        /**
         * Interface definition for a callback to be invoked before the ListView
         * will be bound to an adapter.
         */
        public interface OnPrepareListViewListener {
            
            /**
             * Called before the ListView is bound to an adapter.
             * @param listView The ListView that will be shown in the dialog.
             */
            void onPrepareListView(ListView listView);
        }
        
        public AlertParams(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    
        public void apply(CyeeAlertController dialog) {
            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    dialog.setTitle(mTitle);
                }
                if (mIcon != null) {
                    dialog.setIcon(mIcon);
                }
                if (mIconId >= 0) {
                    dialog.setIcon(mIconId);
                }
                if (mIconAttrId > 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(mIconAttrId));
                }
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mPositiveButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                        mPositiveButtonListener, null);
            }
            if (mNegativeButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                        mNegativeButtonListener, null);
            }
            if (mNeutralButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                        mNeutralButtonListener, null);
            }
            if (mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            // For a list, the client can either supply an array of items or an
            // adapter or a cursor
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                            mViewSpacingBottom);
                } else {
                    dialog.setView(mView);
                }
            } else if (mViewLayoutResId != 0) {
                dialog.setView(mViewLayoutResId);
            }
            
            /*
            dialog.setCancelable(mCancelable);
            dialog.setOnCancelListener(mOnCancelListener);
            if (mOnKeyListener != null) {
                dialog.setOnKeyListener(mOnKeyListener);
            }
            */
            // Gionee zhangxx 2012-11-01 add for CR00715173 begin
            if (mHasCancelIcon) {
                // Gionee <zhangxx><2013-05-15> add for CR00811583 begin
                dialog.setHasCancelIcon(!mIsGnWidget3Style || (mPositiveButtonText == null &&
                        mNegativeButtonText == null && mNeutralButtonText == null));
                // Gionee <zhangxx><2013-05-15> add for CR00811583 end
                if (mCancelIcon != null) {
                    dialog.setCancelIcon(mCancelIcon);
                }
            }
            // Gionee zhangxx 2012-11-01 add for CR00715173 end
            // Gionee <zhangxx><2013-05-15> add for CR00811583 begin
            if (mIsGnWidget3Style) {
                dialog.setButtonStyle(DialogInterface.BUTTON_POSITIVE, mPositiveButtonStyle);
                dialog.setButtonStyle(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonStyle);
            }
            // Gionee <zhangxx><2013-05-15> add for CR00811583 end
        }

        private void createListView(final CyeeAlertController dialog) {
            final RecycleListView listView = (RecycleListView) mInflater.inflate(dialog.mListLayout, null);
            // Gionee <weidong> <2015-4-2> modify for CR01461208 begin
            listView.setVerticalFadingEdgeEnabled(true);
            // Gionee <weidong> <2015-4-2> modify for CR01461208 end
            ListAdapter adapter;

            final int textViewId = com.cyee.internal.R.id.cyee_text1;

            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = new ArrayAdapter<CharSequence>(mContext, dialog.mMultiChoiceItemLayout,
                            textViewId, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);

                            TextView textView = (TextView) view.findViewById(textViewId);
                            View dividerView = view.findViewById(com.cyee.internal.R.id.cyee_dialog_multichoice_divider);
                            boolean isEndItem = false;
                            if (null != mItems) {
                                isEndItem = (position == mItems.length - 1);
                            }
                            if (null != dividerView) {
                                if (isEndItem) {
                                    dividerView.setVisibility(View.GONE);
                                } else {
                                    dividerView.setVisibility(View.VISIBLE);
                                }
                            }
                            boolean isItemChecked = false;
                            if (mCheckedItems != null) {
                                isItemChecked = mCheckedItems[position];
                                if (isItemChecked) {
                                    listView.setItemChecked(position, true);
                                }
                            }
                            if (textView instanceof CheckedTextView) {
                                ((CyeeCheckedTextView) textView).setChecked(isItemChecked);
                            }
                            
                            return view;
                        }
                    };
                } else {
                    adapter = new CursorAdapter(mContext, mCursor, false) {
                        private final int mLabelIndex;
                        private final int mIsCheckedIndex;

                        {
                            final Cursor cursor = getCursor();
                            mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            CheckedTextView text = (CheckedTextView) view.findViewById(com.cyee.internal.R.id.cyee_text1);
                            text.setText(cursor.getString(mLabelIndex));

                            listView.setItemChecked(cursor.getPosition(), cursor.getInt(mIsCheckedIndex) == 1);
                        }

                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return mInflater.inflate(dialog.mMultiChoiceItemLayout, parent, false);
                        }

                    };
                }
            } else {
                int layout = mIsSingleChoice ? dialog.mSingleChoiceItemLayout : dialog.mListItemLayout;
                if (mCursor == null) {
                    adapter = (mAdapter != null) ? mAdapter : new SingleSelectAdapter<CharSequence>(mContext,
                            layout, textViewId, mItems, mCheckedItem);
                } else {
                    adapter = new SimpleCursorAdapter(mContext, layout, mCursor, new String[] {mLabelColumn},
                            new int[] {textViewId});
                }
            }

            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }

            /* Don't directly set the adapter on the ListView as we might
             * want to add a footer to the ListView later.
             */
            dialog.mAdapter = adapter;
            dialog.mCheckedItem = mCheckedItem;

            listView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    TextView textView = (TextView) v.findViewById(textViewId);
                    boolean isItemChecked = false;
                    
                    if (mIsMultiChoice) {
                        if (mCheckedItems != null) {
                            isItemChecked = mCheckedItems[position] = listView.isItemChecked(position);
                        }

                        if (textView instanceof CheckedTextView) {
                            ((CyeeCheckedTextView) textView).setChecked(isItemChecked);
                        }
                    } else {
                        mCheckedItem = position;
                        if (null != dialog.mAdapter) {
                            if (dialog.mAdapter instanceof SingleSelectAdapter) {
                                SingleSelectAdapter adapter = (SingleSelectAdapter)dialog.mAdapter;
                                adapter.setCheckedItem(mCheckedItem);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(dialog.mDialogInterface, position);
                        if (!mIsSingleChoice) {
                            dialog.mDialogInterface.dismiss();
                        }
                    }
                    
                    if (mOnCheckboxClickListener != null) {
                        mOnCheckboxClickListener.onClick(dialog.mDialogInterface, position,isItemChecked);
                    }
                }
            });
            
            // Attach a given OnItemSelectedListener to the ListView
            if (mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(mOnItemSelectedListener);
            }

            if (mIsSingleChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            listView.mRecycleOnMeasure = mRecycleOnMeasure;
            dialog.mListView = listView;
        }

        private static class SingleSelectAdapter<T> extends ArrayAdapter<CharSequence> {

            private final int mFieldId;
            private int mCheckedItem;
            private final int mResId;
            private final LayoutInflater mInflater;
            
            public SingleSelectAdapter(Context context, int resource, int textViewResourceId,
                    CharSequence[] objects, int mCheckedItem) {
                super(context, resource, textViewResourceId, objects);
                mResId = resource;
                mInflater = LayoutInflater.from(context);
                mFieldId = textViewResourceId;
                this.mCheckedItem = mCheckedItem;
            }

            public void setCheckedItem(int position) {
                this.mCheckedItem = position;
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                TextView textView = null;
                if (convertView == null) {
                    view = mInflater.inflate(mResId, parent, false);
                } else {
                    view = convertView;
                }
                
                View dividerView = view.findViewById(com.cyee.internal.R.id.cyee_dialog_singlechoice_divider);
                boolean isEndItem = false;
                int itemCnt = getCount();
                isEndItem = (position == itemCnt - 1);
                if (null != dividerView) {
                    if (isEndItem) {
                        dividerView.setVisibility(View.GONE);
                    } else {
                        dividerView.setVisibility(View.VISIBLE);
                    }
                }
                
                try {
                    if (mFieldId == 0) {
                        // If no custom field is assigned, assume the whole
                        // resource is a TextView
                    } else {
                        // Otherwise, find the TextView field within the layout
                        textView = (TextView) view.findViewById(mFieldId);
                    }
                } catch (ClassCastException e) {
                    Log.e("ArrayAdapter",
                            "You must supply a resource ID for a TextView");
                    throw new IllegalStateException(
                            "ArrayAdapter requires the resource ID to be a TextView",
                            e);
                }

                CharSequence item = (CharSequence) getItem(position);
                if (null != textView) {
                    if (item instanceof CharSequence) {
                        textView.setText((CharSequence) item);
                    } else {
                        textView.setText(item.toString());
                    }
                }

                if (textView instanceof CheckedTextView) {
                    ((CyeeCheckedTextView) textView)
                            .setChecked(position == mCheckedItem);
                }

                return view;
            }
        }
    }

    // Gionee zhangxx 2012-11-01 add for CR00715173 begin
    public void setHasCancelIcon(boolean hasCancelIcon) {
        mHasCancelIconButton = hasCancelIcon;
    }
    
    public void setCancelIcon(Drawable cancelIcon) {
        if (mButtonCancel != null) {
            mButtonCancel.setImageDrawable(cancelIcon);
        }
    }
    // Gionee zhangxx 2012-11-01 add for CR00715173 end    
    
    // Gionee <zhangxx> <2013-05-31> add for CR00811583 begin
    private void setupButtonStyle() {
        //mButtonPositive.setGnButtonStyle(mButtonPositiveStyle);
        //mButtonNeutral.setGnButtonStyle(mButtonNeutralStyle);
        // Gionee <lihq> <2013-12-5> add for CR00873172 begin
        //GUI 3.5
        if (mButtonPositiveStyle == CyeeButton.BUTTON_RECOM_STYLE) {
            mButtonPositive.setTextColor(mContext.getResources().getColorStateList(com.cyee.internal.R.color.cyee_alert_dialog_text_light));
        }
        if (mButtonNeutralStyle == CyeeButton.BUTTON_RECOM_STYLE) {
            mButtonNeutral.setTextColor(mContext.getResources().getColorStateList(com.cyee.internal.R.color.cyee_alert_dialog_text_light));
        }
        // Gionee <lihq> <2013-12-5> add for CR00873172 end
    }
    // Gionee <zhangxx> <2013-05-31> add for CR00811583 end
    
    // Gionee <zhangxx><2013-05-15> add for CR00811583 begin
    public void setButtonStyle(int whichButton, int buttonStyle) {
        if (whichButton == DialogInterface.BUTTON_POSITIVE) {
            mButtonPositiveStyle = buttonStyle;
        } else if (whichButton == DialogInterface.BUTTON_NEUTRAL) {
            mButtonNeutralStyle = buttonStyle;
        }
    }
    
    public void setGnWidget3Style(boolean isGnWidget3Style) {
        mIsGnWidget3Style = isGnWidget3Style;
    }
    // Gionee <zhangxx><2013-05-15> add for CR00811583 end  
}
