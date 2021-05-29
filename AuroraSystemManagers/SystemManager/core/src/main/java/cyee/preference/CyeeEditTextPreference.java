package cyee.preference;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
// Gionee <gaoj> <2013-11-07> add for CR00943435 begin
import android.widget.LinearLayout;
// Gionee <gaoj> <2013-11-07> add for CR00943435 end
import cyee.widget.CyeeEditText;
import cyee.widget.CyeeWidgetResource;

import com.cyee.internal.R;

/**
 * A {@link Preference} that allows for string
 * input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link CyeeEditText}
 * in a dialog. This {@link CyeeEditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any CyeeEditText
 * attributes on the EditTextPreference.
 * <p>
 * This preference will store a string into the SharedPreferences.
 * <p>
 * See {@link android.R.styleable#CyeeEditText CyeeEditText Attributes}.
 */
public class CyeeEditTextPreference extends CyeeDialogPreference {
    /**
     * The edit text shown in the dialog.
     */
    private final CyeeEditText mEditText;
    
    private String mText;
    
    // Gionee <gaoj> <2013-11-07> add for CR00943435 begin
    private final int mEditTextMarginBootom;
    private final int mEditTextMarginLeft;
    // Gionee <gaoj> <2013-11-07> add for CR00943435 end
    
    public CyeeEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mEditText = new CyeeEditText(context, attrs);

        // Give it an ID so it can be saved/restored
        mEditText.setId(android.R.id.edit);

        /*
         * The preference framework and view framework both have an 'enabled'
         * attribute. Most likely, the 'enabled' specified in this XML is for
         * the preference framework, but it was also given to the view framework.
         * We reset the enabled state.
         */
        mEditText.setEnabled(true);

        // Gionee <gaoj> <2013-11-07> add for CR00943435 begin
        mEditTextMarginBootom = (int) context.getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_edit_text_margin_bottom);
        mEditTextMarginLeft = (int) context.getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_edit_text_margin_left);
        // Gionee <gaoj> <2013-11-07> add for CR00943435 end
    }
    
    
    public CyeeEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public CyeeEditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextPreferenceStyle);
    }

    public CyeeEditTextPreference(Context context) {
        this(context, null);
    }
    
    /**
     * Saves the text to the {@link SharedPreferences}.
     * 
     * @param text The text to save
     */
    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        
        mText = text;
        
        persistString(text);
        
        final boolean isBlocking = shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }
    
    /**
     * Gets the text from the {@link SharedPreferences}.
     * 
     * @return The current preference value.
     */
    public String getText() {
        return mText;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        CyeeEditText editText = mEditText;
        editText.setText(getText());
        
        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    /**
     * Adds the CyeeEditText widget of this preference to the dialog's view.
     * 
     * @param dialogView The dialog view.
     */
    protected void onAddEditTextToDialogView(View dialogView, CyeeEditText editText) {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(com.cyee.internal.R.id.cyee_edittext_container);
        if (container != null) {
            // Gionee <gaoj> <2013-11-07> add for CR00943435 begin
            // container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
            // ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            if (getDialogMessage() != null && !getDialogMessage().equals("")) {
                params.setMargins(mEditTextMarginLeft, 0, mEditTextMarginLeft,
                        mEditTextMarginBootom);
            } else {
                params.setMargins(mEditTextMarginLeft, 0,
                        mEditTextMarginLeft, mEditTextMarginBootom);
            }
            // Gionee <gaoj> <2013-11-07> add for CR00943435 end
            container.addView(editText, params);
        }
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setText(restoreValue ? getPersistedString(mText) : (String) defaultValue);
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
    }

    /**
     * Returns the {@link CyeeEditText} widget that will be shown in the dialog.
     * 
     * @return The {@link CyeeEditText} widget that will be shown in the dialog.
     */
    public CyeeEditText getEditText() {
        return mEditText;
    }

    /** @hide */
    @Override
    protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }
        
        final SavedState myState = new SavedState(superState);
        myState.text = getText();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
         
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setText(myState.text);
    }
    
    private static class SavedState extends BaseSavedState {
        String text;
        
        public SavedState(Parcel source) {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    
}
