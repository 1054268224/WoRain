package cyee.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import cyee.widget.CyeeSwitch;
import cyee.widget.CyeeWidgetResource;
import com.cyee.internal.R;

/**
 * A {@link Preference} that provides a two-state toggleable option.
 * <p>
 * This preference will store a boolean into the SharedPreferences.
 *
 * @attr ref android.R.styleable#SwitchPreference_summaryOff
 * @attr ref android.R.styleable#SwitchPreference_summaryOn
 * @attr ref android.R.styleable#SwitchPreference_switchTextOff
 * @attr ref android.R.styleable#SwitchPreference_switchTextOn
 * @attr ref android.R.styleable#SwitchPreference_disableDependentsState
 */
public class CyeeSwitchPreference extends CyeeTwoStatePreference {
    
    private static final String LOGTAG = "CyeeSwitchPreference";
    // Switch text for on and off states
    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;
    private final Listener mListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            CyeeSwitchPreference.this.setChecked(isChecked);
        }
    }

    public CyeeSwitchPreference(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // Gionee <zhangxx> <2013-08-14> modify for CR00857086 begin
        if (NativePreferenceManager.getAnalyzeNativePreferenceXml()
                && attrs != null) {
            int i;
            for (i = 0; i < attrs.getAttributeCount(); i++) {
                int id = attrs.getAttributeNameResource(i);
                switch (id) {
                case android.R.attr.summaryOn:
                    setSummaryOn(NativePreferenceManager
                            .getAttributeStringValue(context, attrs, i));
                    break;
                case android.R.attr.summaryOff:
                    setSummaryOff(NativePreferenceManager
                            .getAttributeStringValue(context, attrs, i));
                    break;
                case android.R.attr.disableDependentsState:
                    setDisableDependentsState(attrs.getAttributeBooleanValue(i,
                            false));
                    break;
                }
            }
            setSwitchTextOn(com.cyee.internal.R.string.cyee_capital_on);
            setSwitchTextOff(com.cyee.internal.R.string.cyee_capital_off);
            setWidgetLayoutResource(R.layout.cyee_preference_widget_switch_hs);
        } else {
            // Gionee <zhangxx> <2013-08-14> modify for CR00857086 end
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CyeeSwitchPreference, defStyleAttr, defStyleRes);
            setSummaryOn(a
                    .getString(R.styleable.CyeeSwitchPreference_cyeesummaryOn));
            setSummaryOff(a
                    .getString(R.styleable.CyeeSwitchPreference_cyeesummaryOff));
            setSwitchTextOn(a
                    .getString(R.styleable.CyeeSwitchPreference_cyeeswitchTextOn));
            setSwitchTextOff(a
                    .getString(R.styleable.CyeeSwitchPreference_cyeeswitchTextOff));
            setDisableDependentsState(a
                    .getBoolean(
                            R.styleable.CyeeSwitchPreference_cyeedisableDependentsState,
                            false));
            a.recycle();
            // Gionee <zhangxx> <2013-08-14> modify for CR00857086 begin
        }
        // Gionee <zhangxx> <2013-08-14> modify for CR00857086 end
    }
    
    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     * @param defStyle Theme attribute defining the default style options
     */
    public CyeeSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     */
    public CyeeSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.switchPreferenceStyle);
    }

    /**
     * Construct a new SwitchPreference with default style options.
     *
     * @param context The Context that will style this preference
     */
    public CyeeSwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View checkableView = view.findViewById(com.cyee.internal.R.id.cyee_switchWidget);
        // Gionee <weidong><2015-1-15> modify for CR01436846 begin
        if (checkableView != null && checkableView instanceof Checkable) {

            if (checkableView instanceof CyeeSwitch) {
                final CyeeSwitch switchView = (CyeeSwitch) checkableView;
                switchView.setOnCheckedChangeListener(null);
            }

            ((Checkable) checkableView).setChecked(mChecked);
            Log.d(LOGTAG, "onBindView mChecked = " + mChecked);
            if (checkableView instanceof CyeeSwitch) {
                final CyeeSwitch switchView = (CyeeSwitch) checkableView;
                switchView.setTextOn(mSwitchOn);
                switchView.setTextOff(mSwitchOff);
                switchView.setOnCheckedChangeListener(mListener);
            }
//            ((Checkable) checkableView).setChecked(mChecked);
//
//            sendAccessibilityEvent(checkableView);
//            if (checkableView instanceof CyeeSwitch) {
//                final CyeeSwitch switchView = (CyeeSwitch) checkableView;
//                switchView.setTextOn(mSwitchOn);
//                switchView.setTextOff(mSwitchOff);
//                switchView.setOnCheckedChangeListener(mListener);
//            }
            // Gionee <weidong><2015-1-15> modify for CR01436846 end
        }

        syncSummaryView(view);
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param onText Text to display in the on state
     */
    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param offText Text to display in the off state
     */
    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    /**
     * @return The text that will be displayed on the switch widget in the on state
     */
    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }

    /**
     * @return The text that will be displayed on the switch widget in the off state
     */
    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }
}
