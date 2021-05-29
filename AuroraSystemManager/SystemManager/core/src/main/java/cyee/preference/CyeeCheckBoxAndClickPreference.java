package cyee.preference;

import cyee.widget.CyeeWidgetResource;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class CyeeCheckBoxAndClickPreference extends CyeeCheckBoxPreference {

	private View mImageView;
	private CyeeOnPreferenceClick mPreferenceClickListener;
	private OnClickListener mRBtnClickListener;
	
    public CyeeCheckBoxAndClickPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(com.cyee.internal.R.layout.cyee_preference_checkbox_and_click);
        setWidgetLayoutResource(com.cyee.internal.R.layout.cyee_preference_checkbox_and_click_right_btn);
    }

    public CyeeCheckBoxAndClickPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
    }

    public CyeeCheckBoxAndClickPreference(Context context) {
        this(context, null);
    }
	
	@Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mImageView = view.findViewById(com.cyee.internal.R.id.cyee_right_button);
        if (null == mImageView) {
            return;
        }
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mRBtnClickListener) {
                    mRBtnClickListener.onClick(v);
                    return;
                }
                if (null != mPreferenceClickListener) {
                    mPreferenceClickListener
                            .onPreferenceClick(CyeeCheckBoxAndClickPreference.this);
                }
            }
        });
    }

    public void setRBtnOnClickListener(OnClickListener listener) {
		this.mRBtnClickListener = listener;
	}
	
    public void setRBtnOnClickListener(CyeeOnPreferenceClick listener) {
		this.mPreferenceClickListener = listener;
	}
    
    public interface CyeeOnPreferenceClick {
        void onPreferenceClick(CyeePreference preference);
    }
}