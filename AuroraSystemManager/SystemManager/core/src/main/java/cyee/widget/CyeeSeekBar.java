package cyee.widget;

import cyee.changecolors.ChameleonColorManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class CyeeSeekBar extends SeekBar {

	public CyeeSeekBar(Context context) {
		this(context, null);
	}

	public CyeeSeekBar(Context context, AttributeSet attrs) {
		this(context, attrs, com.android.internal.R.attr.seekBarStyle);
	}

	public CyeeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public CyeeSeekBar(Context context, AttributeSet attrs, int defStyleAttr,
	        int defStyleRes) {
	    super(context, attrs, defStyleAttr, defStyleRes);

	    if(ChameleonColorManager.getInstance().getCyeeThemeType(context) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
	        int idThumb = com.cyee.internal.R.drawable.cyee_global_theme_big_progressbar_thumb;
	        Drawable thumbDrawable = getResources().getDrawable(idThumb);
	        setThumb(thumbDrawable);
	        int idProgress = com.cyee.internal.R.drawable.cyee_global_theme_big_progress_horizontal;
	        Drawable progressDrawable = getResources().getDrawable(idProgress);
	        setProgressDrawable(progressDrawable);
	        setSplitTrack(false);
	    } else if (ChameleonColorManager.isNeedChangeColor(context)) {
	        TypedArray a = context.obtainStyledAttributes(
	                attrs, com.android.internal.R.styleable.SeekBar, defStyleAttr, defStyleRes);
	        final Drawable thumb = a.getDrawable(com.android.internal.R.styleable.SeekBar_thumb);
	        if (thumb != null) {
	            thumb.setTintList(ColorStateList.valueOf(ChameleonColorManager.getAccentColor_G1()));
	        }
	        setThumb(thumb);
	        changeColers();
	        a.recycle();
	    }
	}

	private void changeColers() {
		Drawable progressDrawable = getProgressDrawable();
		if(progressDrawable != null){
			if (progressDrawable instanceof StateListDrawable) {
				StateListDrawable stateListDrawable = (StateListDrawable)progressDrawable;
				for(int index = 0; index < stateListDrawable.getStateCount(); index ++){
					int[] state = stateListDrawable.getStateSet(index);
					Drawable stateDrawable = stateListDrawable.getStateDrawable(index);
					if(stateIsDisable(state)){
						stateDrawable.setTintList(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
						continue;
					}
					if(stateDrawable != null && stateDrawable instanceof LayerDrawable){
						LayerDrawable layerDrawable = (LayerDrawable) stateDrawable;
						changeLayerDrawable(layerDrawable);
						continue;
					}
				}
			}else if (progressDrawable instanceof LayerDrawable) {
				LayerDrawable layerDrawable = (LayerDrawable) progressDrawable;
				changeLayerDrawable(layerDrawable);
			}
			
		}
		
		Drawable drawable = getBackground();
	    	if(drawable != null){
	    		if(drawable instanceof RippleDrawable){       		
	    			((RippleDrawable) drawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
	    		}
	    	}
	}
	
	private void changeLayerDrawable(LayerDrawable layerDrawable) {

		Drawable progress = layerDrawable.findDrawableByLayerId(android.R.id.progress);
		if (progress != null) {
			progress.setTintList(ColorStateList.valueOf(ChameleonColorManager.getAccentColor_G1()));
		}
		Drawable background = layerDrawable.findDrawableByLayerId(android.R.id.background);
		if (background != null) {
			background.setTintList(ColorStateList.valueOf(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2()));
		}

	}
	
	private boolean stateIsDisable(int[] state) {
        for(int index = 0; index < state.length; index ++){
        	if(state[index] == - android.R.attr.state_enabled){
        		return true;
        	}
        }
        return false;
	}
	 
}
