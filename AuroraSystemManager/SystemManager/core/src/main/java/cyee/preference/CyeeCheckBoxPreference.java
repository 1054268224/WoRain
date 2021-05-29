package cyee.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import com.cyee.internal.R;
import cyee.widget.CyeeWidgetResource;

/**
 * A {@link Preference} that provides checkbox widget
 * functionality.
 * <p>
 * This preference will store a boolean into the SharedPreferences.
 * 
 * @attr ref android.R.styleable#CheckBoxPreference_summaryOff
 * @attr ref android.R.styleable#CheckBoxPreference_summaryOn
 * @attr ref android.R.styleable#CheckBoxPreference_disableDependentsState
 */
public class CyeeCheckBoxPreference extends CyeeTwoStatePreference {

    public CyeeCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Gionee <zhangxx> <2013-08-14> add for CR00857086 begin
        if (NativePreferenceManager.getAnalyzeNativePreferenceXml() && attrs != null) {
            int i;
            for (i=0; i<attrs.getAttributeCount(); i++) {
                int id = attrs.getAttributeNameResource(i);
                switch (id) {
                    case android.R.attr.summaryOn:
                        setSummaryOn(NativePreferenceManager.getAttributeStringValue(context, attrs, i));
                        break;
                    case android.R.attr.summaryOff:
                        setSummaryOff(NativePreferenceManager.getAttributeStringValue(context, attrs, i));
                        break;
                    case android.R.attr.disableDependentsState:
                        setDisableDependentsState(attrs.getAttributeBooleanValue(i, false));
                        break;
                }
            }
            setWidgetLayoutResource(com.cyee.internal.R.layout.cyee_preference_widget_checkbox);
        } else {
        // Gionee <zhangxx> <2013-08-14> add for CR00857086 end
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CyeeCheckBoxPreference, defStyle, 0);
            setSummaryOn(a.getString(R.styleable.CyeeCheckBoxPreference_cyeesummaryOn));
            setSummaryOff(a.getString(R.styleable.CyeeCheckBoxPreference_cyeesummaryOff));
            setDisableDependentsState(a.getBoolean(
                    R.styleable.CyeeCheckBoxPreference_cyeedisableDependentsState, false));
            a.recycle();
        // Gionee <zhangxx> <2013-08-14> add for CR00857086 begin
        }
        // Gionee <zhangxx> <2013-08-14> add for CR00857086 end
    }

    public CyeeCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
    }

    public CyeeCheckBoxPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View checkboxView = view.findViewById(android.R.id.checkbox);
        if (checkboxView != null && checkboxView instanceof Checkable) {
            ((Checkable) checkboxView).setChecked(mChecked);
            sendAccessibilityEvent(checkboxView);
        }

        syncSummaryView(view);
    }
}
