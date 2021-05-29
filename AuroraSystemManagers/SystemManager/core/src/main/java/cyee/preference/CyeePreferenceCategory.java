package cyee.preference;

import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Used to group {@link Preference} objects
 * and provide a disabled title above the group.
 * 
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For information about building a settings UI with Preferences,
 * read the <a href="{@docRoot}guide/topics/ui/settings.html">Settings</a>
 * guide.</p>
 * </div>
 */
public class CyeePreferenceCategory extends CyeePreferenceGroup {
    private static final String TAG = "PreferenceCategory";
    
    public CyeePreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CyeePreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public CyeePreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceCategoryStyle);
    }

    public CyeePreferenceCategory(Context context) {
        this(context, null);
    }
    
    @Override
    protected boolean onPrepareAddPreference(CyeePreference preference) {
        if (preference instanceof CyeePreferenceCategory) {
            throw new IllegalArgumentException(
                    "Cannot add a " + TAG + " directly to a " + TAG);
        }
        
        return super.onPrepareAddPreference(preference);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
    
}
