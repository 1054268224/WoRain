package cyee.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.appcompat.widget.ListPopupWindow;

import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;

public class CyeeSpinner extends Spinner {

	private ListAdapter mListAdapter;
    private int mMode ;
    /**
     * Use a dialog window for selecting spinner options.
     */
    public static final int MODE_DIALOG = 0;
    /**
     * Use a dropdown anchored to the Spinner for selecting spinner options.
     */
    public static final int MODE_DROPDOWN = 1;
    /**
     * Use the theme-supplied value to select the dropdown mode.
     */
    private static final int MODE_THEME = -1;
    
    
    private CyeeForwardingListener mForwardingListener;
    
    
    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnItemClickListener mOnItemClickListener;
    
    private DropdownPopup mPopup;
    
    private int mDropDownWidth;
    
    private CharSequence mPromptText;
    
    /**
     * Construct a new spinner with the given context's theme.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     */
    public CyeeSpinner(Context context) {
        this(context, null);
    }

    /**
     * Construct a new spinner with the given context's theme and the supplied
     * mode of displaying choices. <code>mode</code> may be one of
     * {@link #MODE_DIALOG} or {@link #MODE_DROPDOWN}.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param mode Constant describing how the user will select choices from the spinner.
     * 
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public CyeeSpinner(Context context, int mode) {
        this(context, null,  android.R.attr.spinnerStyle, mode);
    }

    /**
     * Construct a new spinner with the given context's theme and the supplied attribute set.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public CyeeSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.spinnerStyle);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style attribute.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     */
    public CyeeSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0, MODE_THEME);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style. <code>mode</code> may be one of {@link #MODE_DIALOG} or
     * {@link #MODE_DROPDOWN} and determines how the user will select choices from the spinner.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     * @param mode Constant describing how the user will select choices from the spinner.
     *
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public CyeeSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, 0, mode);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style. <code>mode</code> may be one of {@link #MODE_DIALOG} or
     * {@link #MODE_DROPDOWN} and determines how the user will select choices from the spinner.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     * @param defStyleRes A resource identifier of a style resource that
     *        supplies default values for the view, used only if
     *        defStyleAttr is 0 or can not be found in the theme. Can be 0
     *        to not look for defaults.
     * @param mode Constant describing how the user will select choices from the spinner.
     *
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public CyeeSpinner(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, MODE_DIALOG);

        TypedArray a = context.obtainStyledAttributes(attrs,
                com.cyee.internal.R.styleable.CyeeSpinner, defStyleAttr, 0);
        mMode = mode ;
        if (mMode == MODE_THEME) {
            mMode = a.getInt(com.cyee.internal.R.styleable.CyeeSpinner_cyeespinnerMode, MODE_DROPDOWN);
        }
        
        if (isModeDropDown()) {
            final DropdownPopup popup = new DropdownPopup(context, attrs, defStyleAttr, 0);
            mDropDownWidth = a.getLayoutDimension(
                    com.android.internal.R.styleable.Spinner_dropDownWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopup = popup;
//            mForwardingListener = new ForwardingListener(this) {
//                @Override
//                public ListPopupWindow getPopup() {
//                    return mPopup;
//                }
//
//                @Override
//                public boolean onForwardingStarted() {
//                    if (!mPopup.isShowing()) {
//                        mPopup.show(getTextDirection(), getTextAlignment());
//                    }
//                    return true;
//                }
//            };
        }
        
        CharSequence[] entries = a.getTextArray(com.cyee.internal.R.styleable.CyeeSpinner_cyeeentries);
        if (entries != null) {
            int layoutItem  = com.cyee.internal.R.layout.cyee_simple_spinner_item;
            int layoutDropdownItem  = com.cyee.internal.R.layout.cyee_simple_spinner_dropdown_item;
            ArrayAdapter<CharSequence> adapter =
                    new ArrayAdapter<CharSequence>(context,layoutItem, entries);
            adapter.setDropDownViewResource(layoutDropdownItem);
            setAdapter(adapter);
        }
        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {

        } else if (ChameleonColorManager.isNeedChangeColor(context)) {
            changeStateDrawable();
            if (isModeDropDown()) {
                setPopupBackgroundDrawable(new ColorDrawable(
                        ChameleonColorManager.getBackgroudColor_B1()));
            }
        }
        setPrompt(a.getString(com.cyee.internal.R.styleable.CyeeSpinner_cyeeprompt));
        a.recycle();
    }

    
    private boolean isModeDropDown() {
        return mMode != MODE_DIALOG;
    }
    
	
    private void changeStateDrawable() {
        Drawable drawable = getBackground().mutate();
        if(drawable != null && drawable instanceof StateListDrawable){
            StateListDrawable stateListDrawable = (StateListDrawable)drawable;
            for(int index = 0; index < stateListDrawable.getStateCount(); index ++){
                int[] state = stateListDrawable.getStateSet(index);
                Drawable stateDrawable = stateListDrawable.getStateDrawable(index);
                if(stateIsSelected(state)){
                    stateDrawable.setTintList(ColorStateList.valueOf(ChameleonColorManager.getAccentColor_G1()));
                } else {
                    stateDrawable.setTintList(ColorStateList.valueOf(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2()));
                }
            }
        } else if (drawable != null
                && drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            int num = layerDrawable.getNumberOfLayers();
            if (num >= 2) {
                Drawable drawableIndicator = layerDrawable.getDrawable(1);
                drawableIndicator.setTintList(ColorStateList.valueOf(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2()));
            }
        }
        setBackground(drawable);
    }
	
    
	
    @Override
    public void setPopupBackgroundDrawable(Drawable background) {
        if (isModeDropDown() && mPopup != null) {
            mPopup.setBackgroundDrawable(new ColorDrawable(ChameleonColorManager
                    .getPopupBackgroudColor_B2()));
        }  
    }
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isModeDropDown() && mForwardingListener != null
                && mForwardingListener.onTouch(this, event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }
	
	@Override
    public void setOnItemClickListener(OnItemClickListener l) {
        super.setOnItemClickListener(l);
        mOnItemClickListener = l;
    }
	
	private boolean stateIsSelected(int[] state) {
        for(int index = 0; index < state.length; index ++){
        	if(state[index] == android.R.attr.state_pressed){
        		return true;
        	}
        }
        return false;
	}

	@Override
    public void setAdapter(SpinnerAdapter adapter) {
        super.setAdapter(adapter);
        mListAdapter = new DropDownAdapter(adapter);
       // if (adapter instanceof ListAdapter) {
       //         mListAdapter = (ListAdapter) adapter;
       // }

        if (mPopup != null && isModeDropDown()) {
            mPopup.setAdapter(mListAdapter);
        }
    }
    
	public CharSequence getPrompt() {
        return mPromptText;
    }
    
    @Override
    public void setPrompt(CharSequence prompt) {
        if (isModeDropDown() && mPopup != null) {
            mPopup.setPromptText(prompt);
        }
        mPromptText = prompt;
    }

	@Override
	public boolean performClick() {
        if (mMode == MODE_DIALOG) {
            new CyeeAlertDialog.Builder(getContext())
                .setTitle(getPrompt())
                .setSingleChoiceItems(mListAdapter,getSelectedItemPosition(),this)
                .show();
        }else {
            if (!mPopup.isShowing()) {
                mPopup.show(getTextDirection(), getTextAlignment());
            }
        }
        return true;

	}

    /**
     * <p>Wrapper class for an Adapter. Transforms the embedded Adapter instance
     * into a ListAdapter.</p>
     */
    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private final SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        /**
         * <p>Creates a new ListAdapter wrapper for the specified adapter.</p>
         *
         * @param adapter the Adapter to transform into a ListAdapter
         */
        public DropDownAdapter(SpinnerAdapter adapter) {
            this.mAdapter = adapter;
            if (adapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter) adapter;
            }
        }

        public int getCount() {
            return mAdapter == null ? 0 : mAdapter.getCount();
        }

        public Object getItem(int position) {
            return mAdapter == null ? null : mAdapter.getItem(position);
        }

        public long getItemId(int position) {
            return mAdapter == null ? -1 : mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return mAdapter == null ? null :
                    mAdapter.getDropDownView(position, convertView, parent);
        }

        public boolean hasStableIds() {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true. 
         */
        public boolean areAllItemsEnabled() {
            final ListAdapter adapter = mListAdapter;
            if (adapter != null) {
                return adapter.areAllItemsEnabled();
            } else {
                return true;
            }
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        public boolean isEnabled(int position) {
            final ListAdapter adapter = mListAdapter;
            if (adapter != null) {
                return adapter.isEnabled(position);
            } else {
                return true;
            }
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 1;
        }
        
        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

   
    
    @Override
    public void setDropDownHorizontalOffset(int pixels) {
        if (isModeDropDown()) {
            mPopup.setHorizontalOffset(pixels);
        } 
    }
    
    

    @Override
    public void setDropDownVerticalOffset(int pixels) {
        if (isModeDropDown()) {
            mPopup.setVerticalOffset(pixels);
        } 
    }
    
    @Override
    public void setDropDownWidth(int pixels) {
        mDropDownWidth = pixels;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        if (isModeDropDown()) {
            if (mPopup != null && mPopup.isShowing()) {
                mPopup.dismiss();
            }
        }
    }
    
   
    

    private class DropdownPopup extends ListPopupWindow {
        
        private static final int MAX_ITEMS_MEASURED = 15;
        private final Rect mTempRect = new Rect();
        private CharSequence mHintText;
        private ListAdapter mAdapter;
       
       public DropdownPopup(
               Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
           super(context, attrs, defStyleAttr, defStyleRes);

           setAnchorView(CyeeSpinner.this);
           setModal(true);
           setPromptPosition(POSITION_PROMPT_ABOVE);
           setOnItemClickListener(new OnItemClickListener() {
               public void onItemClick(AdapterView parent, View v, int position, long id) {
                   CyeeSpinner.this.setSelection(position);
                   if (mOnItemClickListener != null) {
                       CyeeSpinner.this.performItemClick(v, position, mAdapter.getItemId(position));
                   }
                  dismiss();
               }
           });
       }
       
        private void changeDropDownListSelector(Drawable selector) {
            if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {

            } else if (selector != null
                    && ChameleonColorManager.isNeedChangeColor(mContext)) {
                if (selector instanceof RippleDrawable) {
                    ((RippleDrawable) selector).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
                }
            }
        }
        
        @Override
        public void setAdapter(ListAdapter adapter) {
            super.setAdapter(adapter);
            mAdapter = adapter;
        }

        public CharSequence getHintText() {
            return mHintText;
        }
        
        public void setPromptText(CharSequence hintText) {
            // Hint text is ignored for dropdowns, but maintain it here.
            mHintText = hintText;
        }
        
        void computeContentWidth() {
            final Drawable background = getBackground();
            int hOffset = 0;
            if (background != null) {
                background.getPadding(mTempRect);
                hOffset = isLayoutRtl() ? mTempRect.right : -mTempRect.left;
            } else {
                mTempRect.left = mTempRect.right = 0;
            }

            final int spinnerPaddingLeft = CyeeSpinner.this.getPaddingLeft();
            final int spinnerPaddingRight = CyeeSpinner.this.getPaddingRight();
            final int spinnerWidth = CyeeSpinner.this.getWidth();

            if (mDropDownWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
                int contentWidth =  measureContentWidth(
                        (SpinnerAdapter) mAdapter, getBackground());
                final int contentWidthLimit = mContext.getResources()
                        .getDisplayMetrics().widthPixels - mTempRect.left - mTempRect.right;
                if (contentWidth > contentWidthLimit) {
                    contentWidth = contentWidthLimit;
                }
                setContentWidth(Math.max(
                       contentWidth, spinnerWidth - spinnerPaddingLeft - spinnerPaddingRight));
            } else if (mDropDownWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
                setContentWidth(spinnerWidth - spinnerPaddingLeft - spinnerPaddingRight);
            } else {
                setContentWidth(mDropDownWidth);
            }

            if (isLayoutRtl()) {
                hOffset += spinnerWidth - spinnerPaddingRight - getWidth();
            } else {
                hOffset += spinnerPaddingLeft;
            }
            setHorizontalOffset(hOffset);
        }
        
        public void show(int textDirection, int textAlignment) {
            final boolean wasShowing = isShowing();
            computeContentWidth();
            setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
            super.show();
            final ListView listView = getListView();
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setTextDirection(textDirection);
            listView.setTextAlignment(textAlignment);
            setSelection(CyeeSpinner.this.getSelectedItemPosition());
            changeDropDownListSelector(listView.getSelector());
            if (wasShowing) {
                // Skip setting up the layout/dismiss listener below. If we were previously
                // showing it will still stick around.
                return;
            }

            // Make sure we hide if our anchor goes away.
            // TODO: This might be appropriate to push all the way down to PopupWindow,
            // but it may have other side effects to investigate first. (Text editing handles, etc.)
            final ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                final OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!CyeeSpinner.this.isVisibleToUser()) {
                            dismiss();
                        } else {
                            computeContentWidth();

                            // Use super.show here to update; we don't want to move the selected
                            // position or adjust other things that would be reset otherwise.
                            show();
                        }
                    }
                };
                vto.addOnGlobalLayoutListener(layoutListener);
               setOnDismissListener(new OnDismissListener() {
                   @Override
                   public void onDismiss() {
                       final ViewTreeObserver vto = getViewTreeObserver();
                       if (vto != null) {
                           vto.removeOnGlobalLayoutListener(layoutListener);
                       }
                   }
               });
            }
        }

       @Override
       public void dismiss() {
            super.dismiss();
       }
       
       int measureContentWidth(SpinnerAdapter adapter, Drawable background) {
           if (adapter == null) {
               return 0;
           }

           int width = 0;
           View itemView = null;
           int itemType = 0;
           final int widthMeasureSpec =
               MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
           final int heightMeasureSpec =
               MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

           // Make sure the number of items we'll measure is capped. If it's a huge data set
           // with wildly varying sizes, oh well.
           int start = Math.max(0, getSelectedItemPosition());
           final int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
           final int count = end - start;
           start = Math.max(0, start - (MAX_ITEMS_MEASURED - count));
           for (int i = start; i < end; i++) {
               final int positionType = adapter.getItemViewType(i);
               if (positionType != itemType) {
                   itemType = positionType;
                   itemView = null;
               }
               itemView = adapter.getView(i, itemView, CyeeSpinner.this);
               if (itemView.getLayoutParams() == null) {
                   itemView.setLayoutParams(new ViewGroup.LayoutParams(
                           ViewGroup.LayoutParams.WRAP_CONTENT,
                           ViewGroup.LayoutParams.WRAP_CONTENT));
               }
               itemView.measure(widthMeasureSpec, heightMeasureSpec);
               width = Math.max(width, itemView.getMeasuredWidth());
           }

           // Add background padding to measured width
           if (background != null) {
               background.getPadding(mTempRect);
               width += mTempRect.left + mTempRect.right;
           }

           return width;
       }
   }
       

}