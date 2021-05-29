package cyee.preference;

import com.cyee.internal.R;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeWidgetResource;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Used to group {@link Preference} objects and provide a disabled title above
 * the group.
 * 
 * <div class="special reference"> <h3>Developer Guides</h3>
 * <p>
 * For information about building a settings UI with Preferences, read the <a
 * href="{@docRoot}guide/topics/ui/settings.html">Settings</a> guide.
 * </p>
 * </div>
 */
public class CyeePreferenceButtonCategory extends CyeePreferenceCategory {
    private static final String TAG = "CyeePreferenceButtonCategory";
    private RelativeLayout mCategoryBtn;
    private ICyeeCategoryBtnClickListener mBtnClickListener;
    private String mButtonText;

    public CyeePreferenceButtonCategory(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseCyeePreferenceAttr(context, attrs, defStyleAttr, defStyleRes);
        setWidgetLayoutResource(com.cyee.internal.R.layout.cyee_preference_widget_button_category);
    }

    private void parseCyeePreferenceAttr(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        if (NativePreferenceManager.getAnalyzeNativePreferenceXml()
                && attrs != null) {
        } else {
            AnalyzeCyeePreferenceAttributeSet(context, attrs, defStyleAttr,
                    defStyleRes);
        }
    }

    public CyeePreferenceButtonCategory(Context context, AttributeSet attrs,
            int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public CyeePreferenceButtonCategory(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceCategoryStyle);
    }

    public CyeePreferenceButtonCategory(Context context) {
        this(context, null);
    }

    public String getButtonText() {
        return mButtonText;
    }

    public void setButtonText(String text) {
        this.mButtonText = text;
        notifyChanged();
    }

    public void setCategoryBtnClickListener(
            ICyeeCategoryBtnClickListener listener) {
        mBtnClickListener = listener;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mCategoryBtn = (RelativeLayout) view
                .findViewById(com.cyee.internal.R.id.cyee_category_btn_container);
        CyeeButton btn = (CyeeButton) view.findViewById(com.cyee.internal.R.id.cyee_category_btn);
        btn.setText(getButtonText());

        if (null != mCategoryBtn) {
            mCategoryBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (null != mBtnClickListener) {
                        mBtnClickListener
                                .onCategoryBtnClickListener(CyeePreferenceButtonCategory.this);
                    }
                }
            });
        }
    }

    @Override
    protected boolean onPrepareAddPreference(CyeePreference preference) {
        if (preference instanceof CyeePreferenceButtonCategory) {
            throw new IllegalArgumentException("Cannot add a " + TAG
                    + " directly to a " + TAG);
        }

        return super.onPrepareAddPreference(preference);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    private void AnalyzeCyeePreferenceAttributeSet(Context context,
            AttributeSet attrs, int defStyle, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CyeePreference, defStyle, defStyleRes);
        for (int i = a.getIndexCount(); i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.CyeePreference_cyeeCategoryBtnText) {
                mButtonText = a.getString(attr);
            }
        }
        a.recycle();
    }

    public interface ICyeeCategoryBtnClickListener {
        void onCategoryBtnClickListener(CyeePreference preference);
    }

}
