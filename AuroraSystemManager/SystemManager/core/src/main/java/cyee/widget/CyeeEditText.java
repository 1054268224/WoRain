package cyee.widget;

import java.text.BreakIterator;
import java.util.Locale;

import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;
import cyee.text.method.CyeeWordIterator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.MetaKeyKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.EditText;
import android.widget.Editor;
import android.widget.PopupWindow;

import com.cyee.internal.util.ReflectionUtils;

public class CyeeEditText extends EditText {
	
	private static final String TAG = "CyeeEditText";
	
    private static final boolean LOG_DBG = false;
    protected boolean mDoubleTaped = false;
    /**
     * Drawable used in different state.
     */
    // private Drawable mDeleteNormal;
    // private Drawable mDeletePressed;
    private final String LOG_TAG = "GnEditText";
    private float mPreviousTapPositionX;
    private float mPreviousTapPositionY;
    private long mPreviousTapUpTime = 0;
    private final int mSquaredTouchSlopDistance;
    private boolean mDiscardNextActionUp = false;
    private boolean mToolbarEnabled = true;
    private final boolean mSelectionToolEnabled = true;
    private int mPreStart;
    private int mPreEnd;
    private boolean mIsFirstTap = true;
    private boolean mAfterLongClicked = false;
    private final boolean mImSwitcherEnabled = true;
    private boolean mIsInTextSelectionMode = false;
    private Drawable mSelectHandleStart;
    private Drawable mSelectHandleEnd;
    protected Context mContext;
    private GnPositionListener mGnPositionListener;
    final int[] mGnTempCoords = new int[2];
    private int mCurOffset = -1;
    private static final float[] sTmpPosition = new float[2];
    private boolean mMagnifierEnabled = true;
    // private GnTextViewMagnifier mMagnifier;
    
    private CyeeTextViewEditToolbar mEditToolbar;
    private GnSelectionModifierCursorController mSelectionController;
    private MotionEvent mDownMotionEvent;

    private ActionMode mSelectionActionMode;
    private boolean mSelectionControllerEnabled;
    protected boolean isEditToolbarReadMode = false;
    private CyeeWordIterator mWordIterator;
    private boolean mMagnifierAndTextSelectionEnabled = false;
    
    private int mCurX;
    private int mCurY;

    public int mEnd;
    public int mStart;
    
    /**
     * Whether this edit text is in delete mode.
     */
    private boolean mDeletable = false;

    /**
     * Identify the EditText has the quick delete function.
     */
    private boolean mQuickDelete = false;

    /**
     * Whether we should Handle Delete event now.
     */
    boolean mShouldHandleDelete = false;

    /**
     * The mArea the identify the click bounds.
     */
    private int mArea;

    /**
     * Delete listener.
     */
    private OnTextDeletedListener mTextDeleteListener = null;
    private OnPasswordDeletedListener mPasswordDeleteListener = null;
    private int mDrawableSizeRight; //size of the drawable right

	private boolean mShowQuickDeleteDrawable = true;
		
    private GnTextWatcher mGnTextWatcher = null;
    
    private boolean mIsSupportFloatingActionMode = false;
    
    public CyeeEditText(Context context) {
        this(context, null);
    }

    public CyeeEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public CyeeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CyeeEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        final int touchSlop = viewConfiguration.getScaledTouchSlop();
        mSquaredTouchSlopDistance = touchSlop * touchSlop;
        
        mIsSupportFloatingActionMode = isSupportFloatingActionMode();
		if (!mIsSupportFloatingActionMode) {
			this.setCustomSelectionActionModeCallback(mModeCallback);
			setMagnifierAndTextSelectionEnabled(true);
			
			setFastDeletable(false);
			
			setMagnifierAndTextSelectionEnabled(true);
		}
        
        if(getText() != null || !getText().equals("")) {
             mDeletable = true;
        }
        
        changeColor(context, attrs, defStyleAttr, defStyleRes);
        
    }

    private void changeColor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Drawable curDrawable = getBackground();
            int colorV = 0, defaultV = 0;
            if (curDrawable instanceof ColorDrawable) {
                colorV = ((ColorDrawable) curDrawable).getColor();
                defaultV = getResources().getColor(com.cyee.internal.R.color.cyee_edit_text_background_color);
            }
            if (colorV != 0 && colorV == defaultV) {
                Drawable bgDrawable = getResources().getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_input_background);
                setBackground(bgDrawable);
                int paddinglr = getResources().getDimensionPixelOffset(com.cyee.internal.R.dimen.cyee_edittext_padding_lr);
                int paddingtb = getResources().getDimensionPixelOffset(com.cyee.internal.R.dimen.cyee_edittext_padding_tb);
                setPadding(paddinglr, paddingtb, paddinglr, paddingtb);
            }
            if(ChameleonColorManager.isNeedChangeColor(mContext)) {                
                changeSelectionColor(context, attrs, defStyleAttr, defStyleRes);
                
                int defaultColor = getTextColors().getDefaultColor();  
                Resources res = getResources();
                if(defaultColor == res.getColor(R.color.cyee_content_color_primary_on_backgroud_c1)){
                    setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
                    setHintTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
                    return;
                }
                if(defaultColor == res.getColor(R.color.cyee_content_color_primary_on_appbar_t1)){
                    setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
                    setHintTextColor(ChameleonColorManager.getContentColorSecondaryOnAppbar_T2());
                    return;
                }
            }
        } else if (ChameleonColorManager.isNeedChangeColor(context)) {
            changeSelectionColor(context, attrs, defStyleAttr, defStyleRes);
            
			int defaultColor = getTextColors().getDefaultColor();
			Resources res = getResources();
			if(defaultColor == res.getColor(R.color.cyee_content_color_primary_on_backgroud_c1)){
				setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
				setHintTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
                Drawable background = getBackground();
                if (background != null) {
                    setBackgroundColor(ChameleonColorManager.getEditTextBackgroudColor_B3());
                }
				return;
			}
			if(defaultColor == res.getColor(R.color.cyee_content_color_primary_on_appbar_t1)){
				setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
				setHintTextColor(ChameleonColorManager.getContentColorSecondaryOnAppbar_T2());
				Drawable background = getBackground();
                if (background != null) {
                    setBackgroundColor(ChameleonColorManager.getEditTextBackgroudColor_B3());
                }
				return;
			}
        }
	}

    private void changeSelectionColor(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.TextView, defStyleAttr, defStyleRes);
        Drawable textSelectHandle = a.getDrawable(com.android.internal.R.styleable.TextView_textSelectHandle);
        Drawable textCursorDrawable = a.getDrawable(com.android.internal.R.styleable.TextView_textCursorDrawable);
        Drawable textSelectHandleLeft = a.getDrawable(com.android.internal.R.styleable.TextView_textSelectHandleLeft);
        Drawable textSelectHandleRight = a.getDrawable(com.android.internal.R.styleable.TextView_textSelectHandleRight);
        
        // gionee maxw add begin
        Drawable selectHandleCenter = getSelectHandleCenter(a);
        
        if(selectHandleCenter != null) {
            selectHandleCenter.setColorFilter(ChameleonColorManager.getAccentColor_G1(),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }
        
        if(null != textSelectHandleLeft) {
            textSelectHandleLeft.setColorFilter(ChameleonColorManager.getAccentColor_G1(),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }
        
        if(null != textSelectHandleRight) {
            textSelectHandleRight.setColorFilter(ChameleonColorManager.getAccentColor_G1(),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }
        // gionee maxw add end
        
        if (textSelectHandle != null) {
            textSelectHandle.setColorFilter(ChameleonColorManager.getAccentColor_G1(),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (textCursorDrawable != null) {
            textCursorDrawable.setTintList(ColorStateList.valueOf(ChameleonColorManager
                    .getAccentColor_G1()));
        }
        setHighlightColor(ChameleonColorManager.getAccentColor_G1());
        a.recycle();
    }
    
	// gionee maxw add begin
	private Drawable getSelectHandleCenter(TypedArray a) {
		Editor editor = getEditor();
		if(editor == null) {
			return null;
		}
		Drawable selectHandleCenter = (Drawable) ReflectionUtils.getFieldValue(editor, "mSelectHandleCenter");
		if(selectHandleCenter == null) {
			selectHandleCenter = initSelectHandleCenter(a);
		}
		ReflectionUtils.setFieldValue(editor, "mSelectHandleCenter", selectHandleCenter);
		return selectHandleCenter;
	}
	
	private Drawable initSelectHandleCenter(TypedArray a) {
		int mTextSelectHandleRes = a.getResourceId(com.android.internal.R.styleable.TextView_textSelectHandle, 0);
		Drawable selectHandleCenter = getContext().getDrawable(mTextSelectHandleRes);
		return selectHandleCenter;
	}
	
	private Editor getEditor() {
		Editor editor = (Editor) ReflectionUtils.getFieldValue(this, "mEditor");
		return editor;
	}
	// gionee maxw add end

	public boolean isSupportFloatingActionMode() {
		int osVersion = getAndroidOSVersion();
        return osVersion == 23 || osVersion > 23;
    }

	private int getAndroidOSVersion() {
		int osVersion;
		try {
			osVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			osVersion = 0;
		}

		return osVersion;
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(mIsSupportFloatingActionMode) {
    		return super.onTouchEvent(event);
    	}
        // when user not need quick delete, do not show it.
        if (mQuickDelete && !isEmpty(getText().toString())) {
            // else draw the "x" for deletion.
            int deltX = getRight() - getLeft() - getPaddingRight() - mDrawableSizeRight;

            if (deltX < 0) {
                return false;
            }

            int cur_x = (int) event.getX();
            int cur_y = (int) event.getY();

            // event locate in the expected area
            int action = event.getAction();

            switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if ((cur_x > deltX) && mDeletable) {
                    //if clicking locate in the desired area, we handle it.
                    mShouldHandleDelete = true;

                    return true;
                }

                break;
            }

            case MotionEvent.ACTION_UP: {
                if ((cur_x > deltX) && mDeletable && mShouldHandleDelete) {
                    /*
                     * if user care about this event, throw it to them.
                     * else do the default action, clear of the text
                     */
                    if ((null != mTextDeleteListener) &&
                            mTextDeleteListener.onTextDeleted()) {
                        if (LOG_DBG) {
                            Log.d(LOG_TAG, "user care about the delete event!");
                        }

                        break;
                    }
                    onFastDelete();
                    mShouldHandleDelete = false;

                    return true;
                }

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                //user move out of the valid area, we refresh it
                if ((cur_x < deltX) || (cur_y < 0) || (cur_y > getHeight())) {
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:

                break;

            default:
                break;
            }
        }
    	boolean handled = super.onTouchEvent(event);
    	
        if (isMagnifierAndTextSelectionEnabled() && getDefaultEditable()) {
        	switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPreStart = getSelectionStart();
                mPreEnd = getSelectionEnd();
                final float x = event.getX();
                final float y = event.getY();

                if (mDownMotionEvent != null) {
                    mDownMotionEvent.recycle();
                }

                mDownMotionEvent = MotionEvent.obtain(event);
    			
                // Double tap detection
                long duration = SystemClock.uptimeMillis() -
                                mPreviousTapUpTime;

                if ((duration <= ViewConfiguration.getDoubleTapTimeout()) &&
                        isPositionOnText(x, y)) {
                    final float deltaX = x - mPreviousTapPositionX;
                    final float deltaY = y - mPreviousTapPositionY;
                    final float distanceSquared = (deltaX * deltaX) +
                                                  (deltaY * deltaY);

                    if (distanceSquared < mSquaredTouchSlopDistance) {
                    	startTextSelectionMode();

                        if (isMagnifierAndTextSelectionEnabled()) {
                            mDoubleTaped = true;
                        }

                        mDiscardNextActionUp = true;
                    }
                }

                mPreviousTapPositionX = x;
                mPreviousTapPositionY = y;

                break;
                
    		case MotionEvent.ACTION_UP:
                mCurOffset = -1;
    			mPreviousTapUpTime = SystemClock.uptimeMillis();
        		
    			if (mDoubleTaped) {
    				if (mToolbarEnabled) {
    					if (mSelectionToolEnabled) {
    						startTextSelectionMode();
    					}
    					showEditToolbar();
    				}	

    				mDoubleTaped = false;
    			} else {
    				// gionee maxw modify begin
    				if (isEditToolbarShowing() && !mAfterLongClicked) {
    					if(mIsInTextSelectionMode) {
    						getEditToolbar().move();
    					} else {
    						hideEditToolbar();
    					}
    				} else {
    					int start = getSelectionStart();
    					int end = getSelectionEnd();
    					boolean moved = ((mPreStart == mPreEnd) &&
    							(start == end) && (mPreStart != start));
     					if ((getKeyListener() != null) &&
    							isInputMethodTarget() && !isOutside(event) &&
    							((!mIsFirstTap && !moved))) {
    						if (mToolbarEnabled) {
    							showEditToolbar();
    						}
    					}
    				}
    				// gionee maxw modify end
    				
    				// gionee maxw add begin
        			if(!mOnScrollChanged) {
        				if(mIsInTextSelectionMode && !isPositionOnSelection(event)) {
        					stopTextSelectionMode();
        				}
        			}
        			mOnScrollChanged = false;
        			// gionee maxw add end
    			}
    			
    			
    			mAfterLongClicked = false;
    			mIsFirstTap = false;

    			break;
    			
            case MotionEvent.ACTION_MOVE:

                break;
    		default:
    			break;
    	
    			
        	}
        }
    return handled;
    }
    
    private boolean isPositionOnSelection(MotionEvent event) {
    	float x = event.getX();
    	float y = event.getY();
    	int offset = getOffsetForPosition(x, y);
        return offset > mStart && offset < mEnd;
    }

		/** Returns true if the screen coordinates position (x,y) corresponds to a character displayed
         * in the view. Returns false when the position is in the empty space of left/right of text.
         */
        private boolean isPositionOnText(float x, float y) {
            if (getLayout() == null) {
                return false;
            }

            final int line = getLineAtCoordinate(y);
            x = convertToLocalHorizontalCoordinate(x);

            if (x < getLayout().getLineLeft(line)) {
                return false;
            }

            return !(x > getLayout().getLineRight(line));
        }
    	
        private int getLineAtCoordinate(float y) {
            y -= getTotalPaddingTop();

            // Clamp the position to inside of the view.
            y = Math.max(0.0f, y);
            y = Math.min(getHeight() - getTotalPaddingBottom() - 1, y);
            y += getScrollY();

            return getLayout().getLineForVertical((int) y);
        }
        
        private float convertToLocalHorizontalCoordinate(float x) {
            x -= getTotalPaddingLeft();

            // Clamp the position to inside of the view.
            x = Math.max(0.0f, x);
            x = Math.min(getWidth() - getTotalPaddingRight() - 1, x);
            x += getScrollX();

            return x;
        }
        
        private void showEditToolbar() {
            if (!isMagnifierAndTextSelectionEnabled() || !isToolbarEnabled()) {
                return;
            }

            if (isEditToolbarShowing()) {
//                hideEditToolbar();
            	getEditToolbar().move();
            	return;
            }
            getEditToolbar().show();
        }
        
        /**
         * @return True if the edit toolbar is enabled.
         */
        public boolean isToolbarEnabled() {
            return mToolbarEnabled;
        }
        
        private boolean isEditToolbarShowing() {
            if (!isMagnifierAndTextSelectionEnabled() || !isToolbarEnabled()) {
                return false;
            }

            if (mEditToolbar != null) {
                return mEditToolbar.isShowing();
            }

            return false;
        }
        
        /**
         * @return True if the selection actions are enabled in the toolbar.
         */
        public boolean isSelectionToolEnabled() {
            return mSelectionToolEnabled;
        }
  
        private void hideEditToolbar() {
            if (!isMagnifierAndTextSelectionEnabled() || !isToolbarEnabled()) {
                return;
            }

            if (mEditToolbar != null) {
                mEditToolbar.hide();
            }
        }
        
        /** whether the event outside the edit. */
        private boolean isOutside(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            return (x < 0) || (x > getWidth()) || (y < 0) || (y > getHeight());
        }

        private synchronized CyeeTextViewEditToolbar getEditToolbar() {
            if (mEditToolbar == null) {
                mEditToolbar = new CyeeTextViewEditToolbar(this);
            }

            return mEditToolbar;
        }
        
        
        /**
         * @return True if the inputmethod action is enabled in the toolbar.
         */
        public boolean isImSwitcherEnabled() {
            return mImSwitcherEnabled;
        }


        /**
         * @return True if the magnifier and selection actions are enabled.
         */
        protected boolean isMagnifierAndTextSelectionEnabled() {
            return mMagnifierAndTextSelectionEnabled;
        }
        
        
        public boolean startTextSelectionMode() {
            if (!mIsInTextSelectionMode) {
                if ((length() <= 0) || !requestFocus()) {
                    return false;
                }

                if (!hasSelection()) {
                    if (!selectCurrentWord()) {
                     //    return false;
                    }
                }

                showGnSelectionModifierCursorController();
                mIsInTextSelectionMode = true;

                return true;
            }

            return false;
        }


        
        /**
         * Returns the length, in characters, of the text managed by this TextView
         */
        public int length() {
            return getText().length();
        }
        
        private boolean selectCurrentWord() {
            if (length() <= 0) {
                return false;
            }

            if (getTransformationMethod() instanceof PasswordTransformationMethod) {
                selectAll();

                return true;
            }

            int inputType = getInputType();
            int klass = inputType & InputType.TYPE_MASK_CLASS;
            int variation = inputType & InputType.TYPE_MASK_VARIATION;

            if ((klass == InputType.TYPE_CLASS_NUMBER) ||
                    (klass == InputType.TYPE_CLASS_PHONE) ||
                    (klass == InputType.TYPE_CLASS_DATETIME) ||
                    (variation == InputType.TYPE_TEXT_VARIATION_URI) ||
                    (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) ||
                    (variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) ||
                    (variation == InputType.TYPE_TEXT_VARIATION_FILTER)) {
                selectAll();

                return true;
            }

            final int selStart = getSelectionStart();
            final int selEnd = getSelectionEnd();
            int minOffset = Math.max(0, Math.min(selStart, selEnd));
            int maxOffset = Math.max(0, Math.max(selStart, selEnd));

            if (minOffset >= length()) {
                minOffset = length() - 1;
            }

            int selectionStart;
            int selectionEnd;
            CharSequence text = getText();

            URLSpan[] urlSpans = ((Spanned) text).getSpans(minOffset, maxOffset,
                                 URLSpan.class);
            ImageSpan[] imageSpans = ((Spanned) text).getSpans(minOffset,
                                     maxOffset, ImageSpan.class);

            if (urlSpans.length >= 1) {
                URLSpan urlSpan = urlSpans[0];
                selectionStart = ((Spanned) text).getSpanStart(urlSpan);
                selectionEnd = ((Spanned) text).getSpanEnd(urlSpan);
            } else if (imageSpans.length >= 1) {
                android.text.style.ImageSpan imageSpan = imageSpans[0];
                selectionStart = ((Spanned) text).getSpanStart(imageSpan);
                selectionEnd = ((Spanned) text).getSpanEnd(imageSpan);
            } else {
               final CyeeWordIterator wordIterator = getWordIterator(0);
                wordIterator.setCharSequence(text, minOffset, maxOffset);

                selectionStart = wordIterator.getBeginning(minOffset);
                selectionEnd = wordIterator.getEnd(maxOffset);

                if ((selectionStart == BreakIterator.DONE) ||
                        (selectionEnd == BreakIterator.DONE)) {
                    selectionStart = minOffset;
                    selectionEnd = maxOffset;
                }

                if (selectionStart == selectionEnd) {
                    // Possible when the word iterator does not properly handle the text's language
                    int[] range = getCharRange(selectionStart);
                    selectionStart = range[0];
                    selectionEnd = range[1];
                }
            }
            Selection.setSelection((Spannable) text, selectionStart, selectionEnd);
            return selectionEnd > selectionStart;
			}

        protected void showGnSelectionModifierCursorController() {
            getGnSelectionController().show();
            // gionee maxw add begin
            setCursorVisible(false);
            // gionee maxw add end
        }
        

        protected void hideGnSelectionModifierCursorController() {
            if (mSelectionController != null) {
                mSelectionController.hide();
            }
            // gionee maxw add begin
            setCursorVisible(true);
            // gionee maxw add end
        }


        private GnSelectionModifierCursorController getGnSelectionController() {
            if (mSelectionController == null) {
                mSelectionController = new GnSelectionModifierCursorController();
                getViewTreeObserver()
                .addOnTouchModeChangeListener(mSelectionController);
            }

            return mSelectionController;
        }
        
        private class GnSelectionModifierCursorController
        		implements GnCursorController {
        	// The cursor controller handles, lazily created when shown.
        	private GnSelectionStartHandleView mStartHandle;
        	private GnSelectionEndHandleView mEndHandle;

        GnSelectionModifierCursorController() {
        }

        public void show() {
            initDrawables();
            initHandles();
        }
        
        private void initDrawables() {
            if (mSelectHandleStart == null) {
                mSelectHandleStart = mContext.getResources()
                                     .getDrawable(com.cyee.internal.R.drawable.cyee_text_select_handle_top_left);
            }

            if (mSelectHandleEnd == null) {
                mSelectHandleEnd = mContext.getResources()
                                   .getDrawable(com.cyee.internal.R.drawable.cyee_text_select_handle_top_right);
            }
            
            if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                mSelectHandleStart.setTint(ChameleonColorManager.getAccentColor_G1());
                mSelectHandleEnd.setTint(ChameleonColorManager.getAccentColor_G1());
            }
        }

        private void initHandles() {
            // Lazy object creation has to be done before updatePosition() is called.
            if (mStartHandle == null) {
                mStartHandle = new GnSelectionStartHandleView(mSelectHandleStart,
                        mSelectHandleEnd);
            }

            if (mEndHandle == null) {
                mEndHandle = new GnSelectionEndHandleView(mSelectHandleEnd,
                        mSelectHandleStart);
            }

            mStartHandle.show();
            mEndHandle.show();
        }

        public void hide() {
            if (mStartHandle != null) {
                mStartHandle.hide();
            }

            if (mEndHandle != null) {
                mEndHandle.hide();
            }
        }

        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        @Override
        public void onDetached() {
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.removeOnTouchModeChangeListener(this);

            if (mStartHandle != null) {
                mStartHandle.onDetached();
            }

            if (mEndHandle != null) {
                mEndHandle.onDetached();
            }
        }
        
        // maxw add begin
        public boolean isSelectionStartDragged() {
        	return mStartHandle != null && mStartHandle.isDragging();
        }
        // maxw add end
    }
        
        private interface GnCursorController extends ViewTreeObserver.OnTouchModeChangeListener {
            void show();

            void hide();

            void onDetached();
        }
        

        private class GnSelectionStartHandleView extends GnHandleView {
            public GnSelectionStartHandleView(Drawable drawableLtr,
                                                Drawable drawableRtl) {
                super(drawableLtr, drawableRtl);
            }

            @Override
            protected int getHotspotX(Drawable drawable, boolean isRtlRun) {
                if (isRtlRun) {
                    return (drawable.getIntrinsicWidth() * 9) / 28;
                } else {
                    return (drawable.getIntrinsicWidth() * 19) / 28;
                }
            }

            @Override
            protected int getHotspotY(Drawable drawable, boolean isRtlRun) {
                int textHeight = 0;

                if (getPaint() != null) {
                    android.graphics.Paint.FontMetricsInt fm = getPaint()
                            .getFontMetricsInt();
                    textHeight = fm.bottom - fm.top;
                }

                return ((drawable.getIntrinsicHeight() * 4) / 5) + textHeight;
            }

            @Override
            public int getCurrentCursorOffset() {
                return CyeeEditText.this.getSelectionStart();
            }

            @Override
            public void updateSelection(int offset) {
                Selection.setSelection((Spannable) getText(), offset,
                                       getSelectionEnd());
                updateDrawable();
            }

            // gionee maxw modify begin
            @Override
            public void updatePosition(float x, float y) {
                int offset = getOffsetForPosition(x, y);

                // Handles can not cross and selection is at least one character
                //TODO Alan
                //final int selectionEnd = getSelectionEnd(); 
                final int selectionEnd = mEnd;

                if (offset >= selectionEnd) {
                	float checkedY = checkY(selectionEnd, y);
                	offset = getOffsetForPosition(x, checkedY);
                	if (offset >= selectionEnd) {
                		offset = Math.max(0, selectionEnd - 1);
                	}
                }
                positionAtCursorOffset(offset, false);
                
                mEnd = selectionEnd;
                mStart = offset;
            }
            
            private float checkY(int selectionEnd, float y) {
            	int lineCount = getLayout().getLineCount();
            	int selectionEndline = 0;
            	for(int i=0; i<lineCount; i++) {
            		int start = getLayout().getLineStart(i);
            		int end = getLayout().getLineEnd(i);
            		if(selectionEnd >= start && selectionEnd<=end) {
            			selectionEndline = i;
            			break;
            		}
            	}
            	int line = getLineAtCoordinate(y);
            	while(line != selectionEndline) {
            		if(line < selectionEndline) {
            			y += getLineHeight();
            		} else {
            			y -= getLineHeight();
            		}
            		line = getLineAtCoordinate(y);
            	}
            	return y;	
            }

			@Override
			public float computeHandlePositionY(int line) {
				return getLayout().getLineBottom(line) - mHotspotY - 10;
			}

			@Override
			public float computePointPositionY(float rawY, float touchToWindowOffsetY, float touchOffsetY) {
				return rawY - touchToWindowOffsetY +
                        touchOffsetY + mHotspotY;
			}

			@Override
			public boolean isStartHandle() {
				return true;
			}

			@Override
			public boolean isHandleInParent() {
				return CyeeEditText.this.isPositionVisible(mPositionX + mHotspotX, mPositionY + mHotspotY);
			}
            
            // gionee maxw modify end
        }
        
        private class GnSelectionEndHandleView extends GnHandleView {
            public GnSelectionEndHandleView(Drawable drawableLtr,
                                              Drawable drawableRtl) {
                super(drawableLtr, drawableRtl);
            }

            @Override
            protected int getHotspotX(Drawable drawable, boolean isRtlRun) {
                if (isRtlRun) {
                    return (drawable.getIntrinsicWidth() * 19) / 28;
                } else {
                    return (drawable.getIntrinsicWidth() * 9) / 28;
                }
            }

            @Override
            protected int getHotspotY(Drawable drawable, boolean isRtlRun) {
              //  return drawable.getIntrinsicHeight() / 5;
               int textHeight = 0;

                if (getPaint() != null) {
                    android.graphics.Paint.FontMetricsInt fm = getPaint()
                            .getFontMetricsInt();
                    textHeight = fm.bottom - fm.top;
                }

                return ((drawable.getIntrinsicHeight() * 4) / 5) + textHeight;
            }

            @Override
            public int getCurrentCursorOffset() {
                return CyeeEditText.this.getSelectionEnd();
            }

            @Override
            public void updateSelection(int offset) {
                Selection.setSelection((Spannable) getText(), getSelectionStart(),
                                       offset);
                updateDrawable();
            }

            // gionee maxw modify begin
            @Override
            public void updatePosition(float x, float y) {
                int offset = getOffsetForPosition(x, y);
                int line = getLineAtCoordinate(y);
                int end = getLayout().getLineEnd(line);
                int edittextRight = CyeeEditText.this.getRight();
                int edittextRightPadding = CyeeEditText.this.getPaddingRight();
                int letterWidth = getLetterWidth(line);
                if(offset == end-1 && x > edittextRight-edittextRightPadding-letterWidth) {
                	offset++;
                }
                final int selectionStart = mStart;
                if (offset <= selectionStart) {
                    float checkedY = checkY(selectionStart, y);
                    offset = getOffsetForPosition(x, checkedY);
                    if(offset <= selectionStart) {
                    	offset = Math.min(selectionStart + 1, getText().length());
                    }
                }

                positionAtCursorOffset(offset, false);
                mEnd = offset;
            }
            
            private int getLetterWidth(int line) {
            	int end = getLayout().getLineEnd(line);
            	int lastLineEnd = getLayout().getLineEnd(line-1);
            	int width = CyeeEditText.this.getRight() - CyeeEditText.this.getLeft();
            	if(end==lastLineEnd) {
            		return 0;
            	}
            	return width/(end-lastLineEnd);
            }
            
            private float checkY(int selectionStart, float y) {
            	int lineCount = getLayout().getLineCount();
            	int selectionStartline = 0;
            	for(int i=0; i<lineCount; i++) {
            		int start = getLayout().getLineStart(i);
            		int end = getLayout().getLineEnd(i);
            		if(selectionStart >= start && selectionStart<=end) {
            			selectionStartline = i;
            			break;
            		}
            	}
            	int line = getLineAtCoordinate(y);
            	while(line != selectionStartline) {
            		if(line > selectionStartline) {
            			y -= getLineHeight();
            		} else {
            			y += getLineHeight();
            		}
            		line = getLineAtCoordinate(y);
            	}
            	
            	return y;
            }
         // gionee maxw modify end;

			@Override
			public float computeHandlePositionY(int line) {
				return getLayout().getLineBottom(line)-15;
			}

			@Override
			public float computePointPositionY(float rawY, float touchToWindowOffsetY, float touchOffsetY) {
				return rawY - touchToWindowOffsetY + touchOffsetY;
			}

			@Override
			public boolean isStartHandle() {
				return false;
			}

			@Override
			public boolean isHandleInParent() {
				return CyeeEditText.this.isPositionVisible(mPositionX + mHotspotX,
                        mPositionY);
			}
            
        }
        
    private abstract class GnHandleView extends View
        implements GnEditTextPositionListener {
        // Touch-up filter: number of previous positions remembered
        private static final int HISTORY_SIZE = 5;
        private static final int TOUCH_UP_FILTER_DELAY_AFTER = 150;
        private static final int TOUCH_UP_FILTER_DELAY_BEFORE = 350;
        protected Drawable mDrawable;
        protected Drawable mDrawableLtr;
        protected Drawable mDrawableRtl;
        private final PopupWindow mContainer;

        // Position with respect to the parent TextView
        protected int mPositionX;

        // Position with respect to the parent TextView
        protected int mPositionY;
        private boolean mIsDragging;

        // Offset from touch position to mPosition
        private float mTouchToWindowOffsetX;

        // Offset from touch position to mPosition
        private float mTouchToWindowOffsetY;
        protected int mHotspotX;
        protected int mHotspotY;

        // Offsets the hotspot point up, so that cursor is not hidden by the finger when moving up
        private final float mTouchOffsetY;

        // Where the touch position should be on the handle to ensure a maximum cursor visibility
        private final float mIdealVerticalOffset;

        // Parent's (TextView) previous position in window
        private int mLastParentX;

        // Parent's (TextView) previous position in window
        private int mLastParentY;

        // Previous text character offset
        private int mPreviousOffset = -1;

        // Previous text character offset
        private boolean mPositionHasChanged = true;
        private final long[] mPreviousOffsetsTimes = new long[HISTORY_SIZE];
        private final int[] mPreviousOffsets = new int[HISTORY_SIZE];
        private int mPreviousOffsetIndex = 0;
        private int mNumberPreviousOffsets = 0;

        @SuppressLint("NewApi")        
        public GnHandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(CyeeEditText.this.mContext);
            mContainer = new PopupWindow(CyeeEditText.this.mContext, null,
                                             android.R.attr.textSelectHandleWindowStyle);
            // gionee maxw add begin
            mContainer.setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
            // gionee maxw add end
            mContainer.setSplitTouchEnabled(true);
            mContainer.setClippingEnabled(false);
            //TODO Alan.Xu
            //mContainer.setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
            mContainer.setContentView(this);

            mDrawableLtr = drawableLtr;
            mDrawableRtl = drawableRtl;

            updateDrawable();

            final int handleHeight = mDrawable.getIntrinsicHeight();
            mTouchOffsetY = -0.3f * handleHeight;
            mIdealVerticalOffset = 0.7f * handleHeight;
        }

        protected void updateDrawable() {
            final int offset = getCurrentCursorOffset();
            final boolean isRtlCharAtOffset = getLayout().isRtlCharAt(offset);
            mDrawable = isRtlCharAtOffset ? mDrawableRtl : mDrawableLtr;
            mHotspotX = getHotspotX(mDrawable, isRtlCharAtOffset);
            mHotspotY = getHotspotY(mDrawable, isRtlCharAtOffset);
        }

        protected abstract int getHotspotX(Drawable drawable, boolean isRtlRun);

        protected abstract int getHotspotY(Drawable drawable, boolean isRtlRun);

        private void startTouchUpFilter(int offset) {
            mNumberPreviousOffsets = 0;
            addPositionToTouchUpFilter(offset);
        }

        private void addPositionToTouchUpFilter(int offset) {
            mPreviousOffsetIndex = (mPreviousOffsetIndex + 1) % HISTORY_SIZE;
            mPreviousOffsets[mPreviousOffsetIndex] = offset;
            mPreviousOffsetsTimes[mPreviousOffsetIndex] = SystemClock.uptimeMillis();
            mNumberPreviousOffsets++;
        }

        private void filterOnTouchUp() {
            final long now = SystemClock.uptimeMillis();
            int i = 0;
            int index = mPreviousOffsetIndex;
            final int iMax = Math.min(mNumberPreviousOffsets, HISTORY_SIZE);

            while ((i < iMax) &&
                    ((now - mPreviousOffsetsTimes[index]) < TOUCH_UP_FILTER_DELAY_AFTER)) {
                i++;
                index = (mPreviousOffsetIndex - i + HISTORY_SIZE) % HISTORY_SIZE;
            }

            if ((i > 0) && (i < iMax) &&
                    ((now - mPreviousOffsetsTimes[index]) > TOUCH_UP_FILTER_DELAY_BEFORE)) {
                positionAtCursorOffset(mPreviousOffsets[index], false);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        	// gionee maxw modify add
            setMeasuredDimension(mDrawable.getIntrinsicWidth()+10,
                                 mDrawable.getIntrinsicHeight()+10);
            // gionee maxw modify end
        }

        public void show() {
            if (isShowing()) {
                return;
            }

            CyeeEditText.this.getGnPositionListener()
            .addSubscriber(this,
                           true /* local position may change */);

            // Make sure the offset is always considered new, even when focusing at same position
            mPreviousOffset = -1;
            positionAtCursorOffset(getCurrentCursorOffset(), false);
            CyeeEditText.this.mStart = CyeeEditText.this.getSelectionStart();
            CyeeEditText.this.mEnd = CyeeEditText.this.getSelectionEnd();
        }

        protected void dismiss() {
            mIsDragging = false;
            mContainer.dismiss();
            onDetached();
        }

        public void hide() {
            dismiss();
            CyeeEditText.this.getGnPositionListener().removeSubscriber(this);
        }

        public boolean isShowing() {
            return mContainer.isShowing();
        }

        private boolean isVisible() {
            // Always show a dragging handle.
            if (mIsDragging) {
                return true;
            }
            return isHandleInParent();
        }
        
        public boolean isDragging() {
            return mIsDragging;
        }

        public abstract int getCurrentCursorOffset();

        protected abstract void updateSelection(int offset);

        public abstract void updatePosition(float x, float y);
        
        public abstract float computeHandlePositionY(int line);
        
        public abstract float computePointPositionY(float rawY, float mTouchToWindowOffsetY2, float mTouchOffsetY2);

        public abstract boolean isHandleInParent();
        public abstract boolean isStartHandle();
        protected void positionAtCursorOffset(int offset, boolean parentScrolled) {
            // A HandleView relies on the layout, which may be nulled by external methods
            if (getLayout() == null) {
                return;
            }

            if ((offset != mPreviousOffset) || parentScrolled) {
                updateSelection(offset);
                addPositionToTouchUpFilter(offset);

                final int line = getLayout().getLineForOffset(offset);
                mPositionX = (int)(getLayout().getPrimaryHorizontal(offset) -
                                   0.5f - mHotspotX);
//                mPositionY = getLayout().getLineBottom(line) - mHotspotY;
//                mPositionY = (int) computeHandlePositionY(line)-getScrollY();
                mPositionY = (int) computeHandlePositionY(line);
                // Take TextView's padding and scroll into account.
                
                
                mPositionX += viewportToContentHorizontalOffset();
                mPositionY += viewportToContentVerticalOffset();

                mPreviousOffset = offset;
                mPositionHasChanged = true;
            }

            if (mIsDragging) {
                hideEditToolbar();
            }
        }

        public void updatePosition(int parentPositionX, int parentPositionY,
                                   boolean parentPositionChanged, boolean parentScrolled) {


            positionAtCursorOffset(getCurrentCursorOffset(), parentScrolled);
            if (parentPositionChanged || mPositionHasChanged) {
                if (mIsDragging) {
                    // Update touchToWindow offset in case of parent scrolling while dragging
                    if ((parentPositionX != mLastParentX) ||
                            (parentPositionY != mLastParentY)) {
                        mTouchToWindowOffsetX += (parentPositionX -
                                                  mLastParentX);
                        mTouchToWindowOffsetY += (parentPositionY -
                                                  mLastParentY);
                        mLastParentX = parentPositionX;
                        mLastParentY = parentPositionY;
                    }

                    onHandleMoved();
                }

                if (isVisible()) {

                    final int positionX = parentPositionX + mPositionX;
                    final int positionY = parentPositionY + mPositionY;
                    if (isShowing()) {
                        mContainer.update(positionX, positionY, -1, -1);
                    } else {
                        mContainer.showAtLocation(CyeeEditText.this,
                                                  Gravity.NO_GRAVITY, positionX, positionY);
                    }
                } else {
                    if (isShowing()) {
                        dismiss();
                    }
                }

                mPositionHasChanged = false;
            }
        }

        @Override
        protected void onDraw(Canvas c) {
        	// gionee maxw modify begin
            mDrawable.setBounds(0, 0, getRight() - getLeft()-10, getBottom() - getTop()-10);
            // gionee maxw modify end
            mDrawable.draw(c);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
        	
            switch (ev.getActionMasked()) {
            		
            	case MotionEvent.ACTION_DOWN: {
            		startTouchUpFilter(getCurrentCursorOffset());
            		mTouchToWindowOffsetX = ev.getRawX() - mPositionX;
            		mTouchToWindowOffsetY = ev.getRawY() - mPositionY;

            		final GnPositionListener positionListener = getGnPositionListener();
            		mLastParentX = positionListener.getPositionX();
            		mLastParentY = positionListener.getPositionY();
            		mIsDragging = true;

            		hideEditToolbar();

            		break;
            	}

            	case MotionEvent.ACTION_MOVE: {
            		final float rawX = ev.getRawX();
            		final float rawY = ev.getRawY();

            		// Vertical hysteresis: vertical down movement tends to snap to ideal offset
            		final float previousVerticalOffset = mTouchToWindowOffsetY -
                		mLastParentY;
            		final float currentVerticalOffset = rawY - mPositionY -
            				mLastParentY;
            		float newVerticalOffset;

            		if (previousVerticalOffset < mIdealVerticalOffset) {
            			newVerticalOffset = Math.min(currentVerticalOffset,
            					mIdealVerticalOffset);
            			newVerticalOffset = Math.max(newVerticalOffset,
                                                 previousVerticalOffset);
            		} else {
            			newVerticalOffset = Math.max(currentVerticalOffset,
            					mIdealVerticalOffset);
            			newVerticalOffset = Math.min(newVerticalOffset,
                                                 previousVerticalOffset);
            		}

            		mTouchToWindowOffsetY = newVerticalOffset + mLastParentY;

            		final float newPosX = rawX - mTouchToWindowOffsetX + mHotspotX;
//            		final float newPosY = rawY - mTouchToWindowOffsetY +
//                                      mTouchOffsetY + mHotspotY;
            		final float newPosY = computePointPositionY(rawY, mTouchToWindowOffsetY, mTouchOffsetY);
            		updatePosition(newPosX, newPosY);
            		break;
            	}

            	case MotionEvent.ACTION_UP:
            		filterOnTouchUp();
            		mIsDragging = false;
            		
            		showEditToolbar();
            		mCurOffset = -1;
            		
            		break;

            	case MotionEvent.ACTION_CANCEL:
            		mIsDragging = false;
            		showEditToolbar();
            		mCurOffset = -1;

             	  break;
            }

            return true;
        }

        void onHandleMoved() {
        }

        public void onDetached() {
        }
    }
    
    private interface GnEditTextPositionListener {
        void updatePosition(int parentPositionX, int parentPositionY,
                            boolean parentPositionChanged, boolean parentScrolled);
    }
    

    private GnPositionListener getGnPositionListener() {
        if (mGnPositionListener == null) {
            mGnPositionListener = new GnPositionListener();
        }

        return mGnPositionListener;
    }

    private class GnPositionListener implements ViewTreeObserver.OnDrawListener {
        // 3 handles
        // 3 ActionPopup [replace, suggestion, easyedit] (suggestionsPopup first hides the others)
        private final int MAXIMUM_NUMBER_OF_LISTENERS = 6;
        private final GnEditTextPositionListener[] mPositionListeners = new GnEditTextPositionListener[MAXIMUM_NUMBER_OF_LISTENERS];
        private final boolean[] mCanMove = new boolean[MAXIMUM_NUMBER_OF_LISTENERS];
        private boolean mPositionHasChanged = true;

        // Absolute position of the TextView with respect to its parent window
        private int mPositionX;

        // Absolute position of the TextView with respect to its parent window
        private int mPositionY;
        private int mNumberOfListeners;
        private boolean mScrollHasChanged;

        
        public void addSubscriber(
            GnEditTextPositionListener positionListener, boolean canMove) {
            if (mNumberOfListeners == 0) {
                updatePosition();

                ViewTreeObserver vto = CyeeEditText.this.getViewTreeObserver();
                vto.addOnDrawListener(this);
            }

            int emptySlotIndex = -1;

            for (int i = 0; i < MAXIMUM_NUMBER_OF_LISTENERS; i++) {
                GnEditTextPositionListener listener = mPositionListeners[i];

                if (listener == positionListener) {
                    return;
                } else if ((emptySlotIndex < 0) && (listener == null)) {
                    emptySlotIndex = i;
                }
            }

            mPositionListeners[emptySlotIndex] = positionListener;
            mCanMove[emptySlotIndex] = canMove;
            mNumberOfListeners++;
        }

        public void removeSubscriber(
            GnEditTextPositionListener positionListener) {
            for (int i = 0; i < MAXIMUM_NUMBER_OF_LISTENERS; i++) {
                if (mPositionListeners[i] == positionListener) {
                    mPositionListeners[i] = null;
                    mNumberOfListeners--;

                    break;
                }
            }

            if (mNumberOfListeners == 0) {
                ViewTreeObserver vto = CyeeEditText.this.getViewTreeObserver();
                vto.removeOnDrawListener(this);
            }
        }

        public int getPositionX() {
            return mPositionX;
        }

        public int getPositionY() {
            return mPositionY;
        }

        @Override
        public void onDraw() {
        	CharSequence text = CyeeEditText.this.getText();
        	// Gionee <zhangxx> <2013-07-23> add for CR00833897 begin
            int textLength = text.length();
            if (mStart < 0 || mStart > textLength) {
                mStart = 0;
            }
            if (mEnd > textLength) {
                mEnd = textLength;
                mSelectionController.hide();
            }
            // Gionee <zhangxx> <2013-07-23> add for CR00833897 end
        	Selection.setSelection((Spannable) text, mStart, mEnd);
            updatePosition();
            for (int i = 0; i < MAXIMUM_NUMBER_OF_LISTENERS; i++) {
                if (mPositionHasChanged || mScrollHasChanged || mCanMove[i]) {
                    GnEditTextPositionListener positionListener = mPositionListeners[i];
                    
                    if (positionListener != null) {
                    	positionListener.updatePosition(mPositionX, mPositionY,
                                                        mPositionHasChanged, mScrollHasChanged);
                    }
                }
            }

            mScrollHasChanged = false;

        }

        private void updatePosition() {
            CyeeEditText.this.getLocationInWindow(mGnTempCoords);
            mPositionHasChanged = (mGnTempCoords[0] != mPositionX) ||
                                  (mGnTempCoords[1] != mPositionY);
            mPositionX = mGnTempCoords[0];
            mPositionY = mGnTempCoords[1];
        }

        public void onScrollChanged() {
            mScrollHasChanged = true;
        }
    }
    
    private boolean mOnScrollChanged = false;
    // gionee maxw add begin
    @Override
	protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
    	mOnScrollChanged = true;
		super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
		if(mGnPositionListener != null) {
			mGnPositionListener.onScrollChanged();
		}
	}
    // gionee maxw add end

	protected boolean isPositionVisible(int positionX, int positionY) {
        //#endif /* VENDOR_EDIT */
        synchronized (sTmpPosition) {
            final float[] position = sTmpPosition;
            position[0] = positionX;
            position[1] = positionY;

            View view = this;

            while (view != null) {
                if (view != this) {
                    // Local scroll is already taken into account in positionX/Y
                    position[0] -= view.getScrollX();
                    position[1] -= view.getScrollY();
                }

                if ((position[0] < 0) || (position[1] < 0) ||
                        (position[0] > view.getWidth()) ||
                        (position[1] > view.getHeight())) {
                    return false;
                }

                if (!view.getMatrix().isIdentity()) {
                    view.getMatrix().mapPoints(position);
                }

                position[0] += view.getLeft();
                position[1] += view.getTop();

                final ViewParent parent = view.getParent();

                if (parent instanceof View) {
                    view = (View) parent;
                } else {
                    // We've reached the ViewRoot, stop iterating
                    view = null;
                }
            }
        }
        // We've been able to walk up the view hierarchy and the position was never clipped
        return true;
    }

    protected int viewportToContentHorizontalOffset() {
        return getCompoundPaddingLeft() - getScrollX();
    }
    
	protected int viewportToContentVerticalOffset() {
		int offset = getExtendedPaddingTop() - CyeeEditText.this.getScrollY();
		if ((getGravity() & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
			offset += getVerticalOffset(false);
		}
		return offset;
	}
	
	private int getVerticalOffset(boolean forceNormal) {
		int voffset = 0;
		final int gravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;

		Layout l = getLayout();
		Layout hintLayout = getHintLayout();
		if (!forceNormal && getText().length() == 0 && hintLayout != null) {
			l = hintLayout;
		}

		if (gravity != Gravity.TOP) {
			int boxht = getBoxHeight(l);
			int textht = l.getHeight();

			if (textht < boxht) {
				if (gravity == Gravity.BOTTOM)
					voffset = boxht - textht;
				else
					voffset = (boxht - textht) >> 1;
			}
		}

		return voffset;
	}

	private Layout getHintLayout() {
		Layout hintLayout = (Layout) ReflectionUtils.getFieldValue(this, "mHintLayout");
		return hintLayout;
	}
	
	private int getBoxHeight(Layout l) {
        Insets opticalInsets = isLayoutModeOptical(mParent) ? getOpticalInsets() : Insets.NONE;
        int padding = getExtendedPaddingTop() + getExtendedPaddingBottom();
        return getMeasuredHeight() - padding + opticalInsets.top + opticalInsets.bottom;
    }
        
        @Override
        public void onRestoreInstanceState(Parcelable state) {
            super.onRestoreInstanceState(state);
            
            if (isMagnifierAndTextSelectionEnabled() && hasSelection()) {
                setSelection(getSelectionEnd());
            }
        }
        
        private final ActionMode.Callback mModeCallback = new Callback() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        
        public void stopTextSelectionMode() {
            if (mIsInTextSelectionMode) {
                Selection.setSelection((Spannable) getText(), getSelectionEnd());
                hideGnSelectionModifierCursorController();
                mIsInTextSelectionMode = false;
            }
        }
        
        @Override
        protected void onTextChanged(CharSequence text, int start,
                                     int lengthBefore, int lengthAfter) {
            if (isMagnifierAndTextSelectionEnabled()) {
                if (getDefaultEditable()) {
                    hideEditToolbar();
                }

                stopTextSelectionMode();
            }

            super.onTextChanged(text, start, lengthBefore, lengthAfter);
        }
        
        //add the int param just want to diff from the TextView getWordIterator
        public CyeeWordIterator getWordIterator(int i) {
            if (mWordIterator == null) {
                mWordIterator = new CyeeWordIterator(getTextServicesLocale());
            }

            return mWordIterator;
        }
        
        void onLocaleChanged() {
            // Will be re-created on demand in getWordIterator with the proper new locale
            mWordIterator = null;
        }
        
        public Locale getTextServicesLocale() {
            Locale locale = Locale.getDefault();
            final TextServicesManager textServicesManager = (TextServicesManager) mContext.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
            final SpellCheckerSubtype subtype = null; //textServicesManager.getCurrentSpellCheckerSubtype(true);

            if (subtype != null) {
                locale = new Locale(subtype.getLocale());
            }

            return locale;
        }


        private int[] getCharRange(int offset) {
            CharSequence text = getText();
            final int textLength = length();

            if ((offset + 1) < textLength) {
                final char currentChar = text.charAt(offset);
                final char nextChar = text.charAt(offset + 1);

                if (Character.isSurrogatePair(currentChar, nextChar)) {
                    return new int[] { offset, offset + 2 };
                }
            }

            if (offset < textLength) {
                return new int[] { offset, offset + 1 };
            }

            if ((offset - 2) >= 0) {
                final char previousChar = text.charAt(offset - 1);
                final char previousPreviousChar = text.charAt(offset - 2);

                if (Character.isSurrogatePair(previousPreviousChar, previousChar)) {
                    return new int[] { offset - 2, offset };
                }
            }

            if ((offset - 1) >= 0) {
                return new int[] { offset - 1, offset };
            }

            return new int[] { offset, offset };
        }
        
        
        @Override
        public boolean performLongClick() {
        	if(mIsSupportFloatingActionMode) {
        		//M:bug_id:EJWJ-585  donghao  20190730 start
				try{
					return super.performLongClick();
				}catch (IllegalStateException e){
					return true;
				}
        		//M:bug_id:EJWJ-585  donghao  20190730 end

        	}
        	// maxw modify begin
        	// hideEditToolbar();
        	showEditToolbar();
        	// maxw modify begin

            boolean handled = false;
            // maxw modify begin
            boolean vibrate = true;
            // maxw modify end

            if (isMagnifierAndTextSelectionEnabled()) {
                handled = showContextMenu();

                if (!handled && (mDownMotionEvent != null) && getDefaultEditable()) {
                    positionCursor(mDownMotionEvent);
                    /*vibrate = showMagnifier(mCurX, mCurY,
                                            Math.round(mDownMotionEvent.getX()),
                                            Math.round(mDownMotionEvent.getY()), false);*/
                                            //Zhaoan Xu@dlt SDK, temply disable the animatin to fix the issue can't see popup window(including input method and EditTexttoolbar) when showing showMagnifier
                                            //TODO: should find the root cause to fix the finally
                    mAfterLongClicked = true;
                    handled = true;
                }
            }

            if (vibrate) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            if (handled) {
                return true;
            }

            return super.performLongClick();
        }
        
        /**
         * reposition cursor.
         * calculate mCurX and mCurY according the cursor position, consequently control magnifier movement.
         */
        private void positionCursor(MotionEvent event) {
            if (getLayout() == null) {
                return;
            }

            int line = CyeeTextViewHelper.getLineNumber(this, event.getY());
            int offset = CyeeTextViewHelper.getOffsetByLine(this, line, event.getX());

            if (((getText() != null) && (length() > 0)) ||
                    (MetaKeyKeyListener.getMetaState(getText(), (1 << 16)) == 0)) {
                Selection.setSelection((Spannable) getText(), offset);
                stopTextSelectionMode();
            } else {
                Selection.setSelection((Spannable) getText(), getSelectionStart(),
                                       offset);
            }

            boolean outside = isOutside(event);

            if (outside) {
                mCurX = Math.round(event.getX());
                mCurY = Math.round(event.getY());
            } else {
                Layout layout = getLayout();
                int left = Math.round(layout.getPrimaryHorizontal(offset));
                int top = layout.getLineTop(line);
                int bottom = layout.getLineBottom(line);
                float lineRight = (layout.getLineRight(line) +
                                   getTotalPaddingLeft()) - getScrollX();

                if (event.getX() > lineRight) {
                    mCurX = Math.round(event.getX());
                } else if (offset != mCurOffset) {
                    mCurX = (left + getTotalPaddingLeft()) - getScrollX();
                    mCurOffset = offset;
                }

                mCurY = (Math.round((top + bottom) / 2f) + getTotalPaddingTop()) -
                        getScrollY();
            }
        }
        
        private void reset() {
            if (getDefaultEditable()) {
                hideEditToolbar();
            }
            
            // Gionee <zhangxx> <2013-11-05> add for CR00942860 begin
    		if (mSelectionController != null) {
//    			mSelectionController.hide();
    			stopTextSelectionMode();
    		}
            // Gionee <zhangxx> <2013-11-05> add for CR00942860 end

            mCurX = 0;
            mCurY = 0;
            mCurOffset = -1;
            mIsFirstTap = true;

            if (mDownMotionEvent != null) {
                mDownMotionEvent.recycle();
            }
            
            mDownMotionEvent = null;
        }
        

        @Override
        protected void onFocusChanged(boolean focused, int direction,
                                      Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);

            if (isMagnifierAndTextSelectionEnabled()) {
                reset();
            }
        }
        
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);

            if (isMagnifierAndTextSelectionEnabled()) {
                reset();
            }
        }
        
        @Override
        public boolean dispatchKeyEventPreIme(KeyEvent event) {
            if (isMagnifierAndTextSelectionEnabled()) {
                if (getDefaultEditable()) {
                    hideEditToolbar();
                }

                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    stopTextSelectionMode();
                }
            }

            return super.dispatchKeyEventPreIme(event);
        }
        
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (isMagnifierAndTextSelectionEnabled()) {
                if (getDefaultEditable()) {
                    hideEditToolbar();
                }
            }

            return super.dispatchKeyEvent(event);
        }


        @Override
        protected void onVisibilityChanged(View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);

            if (isMagnifierAndTextSelectionEnabled()) {
                if (visibility != VISIBLE) {
                    if (getDefaultEditable()) {
                        hideEditToolbar();
                    }
                }
            }
        }


        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();

            if (isMagnifierAndTextSelectionEnabled()) {
                if (getDefaultEditable()) {
                    hideEditToolbar();

                    if (hasSelection()) {
                        setSelection(getSelectionEnd());
                    }
                }
            }
        }
        
        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            InputConnection ic = super.onCreateInputConnection(outAttrs);

            if (isMagnifierAndTextSelectionEnabled() && onCheckIsTextEditor() &&
                    isEnabled()) {
                if (null == outAttrs.extras) {
                    outAttrs.extras = new Bundle();
                }

                //outAttrs.imeOptions |= EditorInfo.IME_FLAG_Gn_STYLE;
                outAttrs.extras.putBoolean("IS_IME_STYLE_Gn", true);
            }

            return ic;
        }

        /**
         * Sets whether (default) or not the magnifier and selection actions are enabled.
         */
        public void setMagnifierAndTextSelectionEnabled(boolean enabled) {
            if (isMagnifierAndTextSelectionEnabled() && !enabled) {
                // hide magnifier and toolbar
                stopTextSelectionMode();
            }

            mMagnifierAndTextSelectionEnabled = enabled;
        }
        
        /**
         * Sets whether (default) or not the magnifier is enabled. This method works only
         * if <b>isMagnifierAndTextSelectionEnabled</b> returns true.
         */
        public void setMagnifierEnabled(boolean magnifierEnabled) {
            mMagnifierEnabled = magnifierEnabled;
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isMagnifierAndTextSelectionEnabled()) {
                moveEditToolbar();

                if (hasSelection() && isEditToolbarShowing()) {
                    // update the position of the edit toolbar
                    postInvalidateDelayed(500);
                }
            }
        }

        private void moveEditToolbar() {
            if (!isMagnifierAndTextSelectionEnabled() || !isToolbarEnabled()) {
                return;
            }

            getEditToolbar().move();
        }
        
        /**
         * Sets whether (default) or not the edit toolbar is enabled. This method works
         * only if <b>isMagnifierAndTextSelectionEnabled</b> returns true.
         */
        public void setToolbarEnabled(boolean toolbarEnabled) {
            mToolbarEnabled = toolbarEnabled;
        }
        
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (isMagnifierAndTextSelectionEnabled() && isEnabled()) {
                stopTextSelectionMode();
            }

            return super.onKeyUp(keyCode, event);
        }

        private class GnTextWatcher implements TextWatcher {
            public void afterTextChanged(Editable arg0) {
                String text = getText().toString();

                //if the text is not empty, let the delete button to be seen
                if (TextUtils.isEmpty(text)) {
                    setCompoundDrawables(null, null, null, null);
                    mDeletable = false;
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }
        }


        @Override
        protected boolean getDefaultEditable() {
            return true;
        }
 
        @Override
        public Editable getText() {
            return (Editable) super.getText();
        }

        /**
         * Interface definition for a callback to be invoked
         * when the delete button is clicked.
         */
        public interface OnTextDeletedListener {
            boolean onTextDeleted();
        }

        /**
         * Interface definition for a callback to be invoked
         * when delete use keyboard.
         */
        public interface OnPasswordDeletedListener {
            boolean onPasswordDeleted();
        }

        /**
         * Set whether this EditText has a fast deletable function.
         */
        public void setFastDeletable(boolean quickDelete) {

            if (mQuickDelete != quickDelete) {
                mQuickDelete = quickDelete;
                if (mQuickDelete) {
                    //if false, need not care about text change
                    if (mGnTextWatcher == null) {
                        mGnTextWatcher = new GnTextWatcher();
                        addTextChangedListener(mGnTextWatcher);
                    }
                }
            }
        }
        
        /**
         * Get whether can this EditText have fast deletable.
         */
        public boolean isFastDeletable() {
            return mQuickDelete;
        }

        /**
         * Set a special listener to be called when the delete button is clicked.
         */
        public void setOnTextDeletedListener(
            OnTextDeletedListener textDeleteListener) {
            mTextDeleteListener = textDeleteListener;
        }

        public void setOnPasswordDeletedListener(
            OnPasswordDeletedListener passwordDeletedListener) {
            mPasswordDeleteListener = passwordDeletedListener;
        }
  

        /**
         * Check the current text is empty or null?
         * @param currentText The text current exist int the edit text.
         * @return true if the text is not null of of a length of zero, else return fasle.
         */
        private boolean isEmpty(String currentText) {
            if (null == currentText) {
                return false;
            }

            return TextUtils.isEmpty(currentText);
        }

        /**
         * Called when do the default action of delete clicked.
         */
        private void onFastDelete() {
            if (LOG_DBG) {
                Log.d(LOG_TAG, "onTextDelete()");
            }

            CharSequence mText = getText();
            ((Editable) mText).delete(0, mText.length());
            setText("");
        }
        
        /**
         * Sets the Drawables (if any) to appear to the left of, above,
         * to the right of, and below the text.  Use null if you do not
         * want a Drawable there.  The Drawables must already have had
         * {@link Drawable#setBounds} called.
         *
         */
        public void setCompoundDrawables(Drawable left, Drawable top,
                                         Drawable right, Drawable bottom) {
            if(false == mShowQuickDeleteDrawable){
            		super.setCompoundDrawables(null, null, null, null);
            }
            
            super.setCompoundDrawables(left, top, right, bottom);

            if (null != right) {
                mDrawableSizeRight = right.getBounds().width();
            } else {
                mDrawableSizeRight = 0;
            }
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (mQuickDelete && (keyCode == KeyEvent.KEYCODE_DEL)) {
                super.onKeyDown(keyCode, event);

                if (null != mPasswordDeleteListener) {
                    mPasswordDeleteListener.onPasswordDeleted();
                }

                return true;
            }

            return (super.onKeyDown(keyCode, event));
        }
        
        public void setQuickDeleteDrawableVisible(boolean show){
        		mShowQuickDeleteDrawable = show;
        }
        // Gionee <lihq> <2013-12-13> modify for CR00873172 begin
        // GUI 3.5 CyeeSearchView 字体未置中
        // Gionee <daizhimin> <2013-08-05> add for CR00845105 begin 
        /*
        @Override
        public int getExtendedPaddingBottom() {
            return super.getExtendedPaddingBottom() + mContext.getResources().getDimensionPixelOffset(com.cyee.internal.R.dimen.cyee_temp_pading);
        }
        @Override
        public int getExtendedPaddingTop() {
            return super.getExtendedPaddingTop() - viewportToContentVerticalOffset();
        }*/
        // Gionee <daizhimin> <2013-08-05> add for CR00845105 begin  
        // Gionee <lihq> <2013-12-13> modify for CR00873172 end

        // gionee maxw add begin
        @Override
        public boolean bringPointIntoView(int offset) {
        	if(isStartHandleDraging()) {
        		offset = getSelectionStart();
        	}
        	return super.bringPointIntoView(offset);
        }

        private boolean isStartHandleDraging() {
        	if(mSelectionController == null) {
        		return false;
        	}
        	return mSelectionController.isSelectionStartDragged();
        }

		@Override
		public boolean onPreDraw() {
			return super.onPreDraw();
		}
        
        
        // gionee maxw add end
}