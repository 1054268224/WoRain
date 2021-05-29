package cyee.widget;


import cyee.changecolors.ChameleonColorManager;
import cyee.changecolors.ChangeColorUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class CyeeRadioButton extends RadioButton {
	
    private static final String TAG = "CyeeRadioButton";

    private Drawable mButtonDrawable;
    private int mState = -1;
    private static final int STATE_DEFAULT = 0;
    private static final int STATE_CHECKED = 1;
    private static final int ALPHA_DISABLED = 66;
    private static final int ALPHA_ENABLED = 255;
	
	
	public CyeeRadioButton(Context context) {
		this(context, null);
	}

	public CyeeRadioButton(Context context, AttributeSet attrs) {
		this(context, attrs, com.android.internal.R.attr.radioButtonStyle);

	}

	public CyeeRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public CyeeRadioButton(Context context, AttributeSet attrs, int defStyleAttr,
	        int defStyleRes) {
	    super(context, attrs, defStyleAttr, defStyleRes);
	    if(ChameleonColorManager.getInstance().getCyeeThemeType(context) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
	        boolean isDefault = isDefaultDrawable();
	        if (!isDefault) {
	            return;
	        }
	        Resources iCyeeRes = context.getResources();
	        Drawable bgDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_radiobutton_bg_selector);
	        setButtonDrawable(bgDrawable);
	        if(ChameleonColorManager.isNeedChangeColor(mContext)) {
	            ChangeColorUtil.changeTextViewTextColor(this);
	        }
	    } else if(ChameleonColorManager.isNeedChangeColor(context)){ 
	        final TypedArray a = context.obtainStyledAttributes(
	                attrs, com.android.internal.R.styleable.CompoundButton, defStyleAttr, defStyleRes);
	        mButtonDrawable = a.getDrawable(com.android.internal.R.styleable.CompoundButton_button);
	        a.recycle();
	        setButtonDrawable(mButtonDrawable);

	        Drawable drawable = getBackground();
	        if(drawable != null && drawable instanceof RippleDrawable){
	            ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
	        }

	        ChangeColorUtil.changeTextViewTextColor(this);	
	    }

	}
	
    @SuppressLint("NewApi")
    private boolean isDefaultDrawable() {
        Drawable defaultD = getButtonDrawable();
        return defaultD instanceof AnimatedStateListDrawable;
    }
	
	
	@Override
	protected void drawableStateChanged() {
		// TODO Auto-generated method stub
        if(ChameleonColorManager.isNeedChangeColor(mContext) && mButtonDrawable != null){ 
             changeButtonDrawable();
        }

		super.drawableStateChanged();
	}
	
	private void changeButtonDrawable() {

        if(stateIsChecked(getDrawableState())){ 
            if (mState != STATE_CHECKED) {
                mButtonDrawable.setColorFilter(ChameleonColorManager.getAccentColor_G1(), PorterDuff.Mode.SRC_IN);
                mState = STATE_CHECKED;
            }
        } else {
            if (mState != STATE_DEFAULT) {
                mButtonDrawable.setColorFilter(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2(), PorterDuff.Mode.SRC_IN);
                mState = STATE_DEFAULT;
            }
        }
        
        if (!isEnabled()) {
            mButtonDrawable.setAlpha(ALPHA_DISABLED);
        }else {
            mButtonDrawable.setAlpha(ALPHA_ENABLED);
        }
    }

	private boolean stateIsChecked(int[] myDrawableState) {
        for(int index = 0; index < myDrawableState.length; index ++){
        	if(myDrawableState[index] == android.R.attr.state_checked){
        		return true;
        	}
        }
        return false;
	}

}
