
package com.cydroid.softmanager.applock;

import android.content.Context;
import cyee.preference.CyeeCheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;

import android.view.View.OnClickListener;

import com.cydroid.softmanager.R;

/**
 * Created by guoxt on 18-8-13.
 */
public class RadioButtonPreference extends CyeeCheckBoxPreference {
	private View mImageView;
	private OnClickListener mRBtnClickListener;
	private final boolean displayRightButton = false;
	
	
    public RadioButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.radiobutton_preference);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioButtonPreference(Context context) {
        this(context, null);
    }
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
        mImageView = view.findViewById(R.id.checkbox);
	}

}
