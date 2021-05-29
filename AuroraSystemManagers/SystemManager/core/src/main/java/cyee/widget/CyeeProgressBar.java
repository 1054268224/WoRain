package cyee.widget;

import cyee.changecolors.ChameleonColorManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class CyeeProgressBar extends ProgressBar{

	
  
    public CyeeProgressBar(Context context) {
        this(context, null);
    }

    public CyeeProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.progressBarStyle);
    }

    public CyeeProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CyeeProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        changeColers();
    }
    
	private void changeColers() {
        if (ChameleonColorManager.isNeedChangeColor(mContext)) {	
			setIndeterminateTintList(ColorStateList.valueOf(ChameleonColorManager.getAccentColor_G1()));
			setSecondaryProgressTintList(ColorStateList.valueOf(ChameleonColorManager.getAccentColor_G1()));
			setProgressTintList(ColorStateList.valueOf(ChameleonColorManager.getAccentColor_G1()));
			setProgressBackgroundTintList(ColorStateList.valueOf(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()));
        }

	}
}
