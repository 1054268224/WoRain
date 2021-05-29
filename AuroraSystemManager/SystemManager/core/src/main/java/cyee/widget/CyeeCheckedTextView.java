package cyee.widget;

import cyee.changecolors.ChameleonColorManager;
import cyee.changecolors.ChangeColorUtil;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

public class CyeeCheckedTextView extends CheckedTextView {

	private Drawable mCheckMarkDrawable;
	
	private int mState = -1;
	private static final int STATE_DEFAULT = 0;
	private static final int STATE_CHECKED = 1;

	public CyeeCheckedTextView(Context context) {
		this(context, null);
	}

	public CyeeCheckedTextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkedTextViewStyle);
	}

	public CyeeCheckedTextView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public CyeeCheckedTextView(Context context, AttributeSet attrs,
	        int defStyleAttr, int defStyleRes) {
	    super(context, attrs, defStyleAttr, defStyleRes);

	    if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
	        if(ChameleonColorManager.isNeedChangeColor(context)) {
	            Drawable drawable = getBackground();
	            if(drawable != null && drawable instanceof RippleDrawable){
	                ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
	            }
	            ChangeColorUtil.changeTextViewTextColor(this);
	        }
	    } else if(ChameleonColorManager.isNeedChangeColor(context)){ 
	        final TypedArray a = context.obtainStyledAttributes(
	                attrs, com.android.internal.R.styleable.CheckedTextView, defStyleAttr, defStyleRes);
	        mCheckMarkDrawable = a.getDrawable(com.android.internal.R.styleable.CheckedTextView_checkMark);
	        a.recycle();
	        if (mCheckMarkDrawable != null) {
	            setCheckMarkDrawable(mCheckMarkDrawable);
	        }

	        Drawable drawable = getBackground();
	        if(drawable != null && drawable instanceof RippleDrawable){
	            ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
	        }
	        ChangeColorUtil.changeTextViewTextColor(this);
	    }
	}
	

	private boolean stateIsChecked(int[] myDrawableState) {
		for (int index = 0; index < myDrawableState.length; index++) {
			if (myDrawableState[index] == android.R.attr.state_checked) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void drawableStateChanged() {
	    if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {

	    } else if (ChameleonColorManager.isNeedChangeColor(mContext) && mCheckMarkDrawable != null) {
	        changeMarkDrawable();
	    }

	    super.drawableStateChanged();
	}
	
	
	private void changeMarkDrawable() {
	    if (stateIsChecked(getDrawableState())) {
	        if (mState != STATE_CHECKED) {
	            mCheckMarkDrawable.setColorFilter(
	                    ChameleonColorManager.getAccentColor_G1(),
	                    PorterDuff.Mode.SRC_IN);
	            mState = STATE_CHECKED;
            }
	        
        } else {
            if (mState != STATE_DEFAULT) {
                
                mCheckMarkDrawable.setColorFilter(ChameleonColorManager
                        .getContentColorSecondaryOnBackgroud_C2(),
                        PorterDuff.Mode.SRC_IN);
                mState = STATE_DEFAULT;
            }
        }
	    
    }
	
}
