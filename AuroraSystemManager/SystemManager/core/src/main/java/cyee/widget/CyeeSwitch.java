package cyee.widget;

import cyee.changecolors.ChameleonColorManager;
import cyee.changecolors.ChangeColorUtil;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;

public class CyeeSwitch extends CyeeBaseSwitch {
    ColorStateList mThumbColors;

    public CyeeSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CyeeSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.switchStyle);
    }

    public CyeeSwitch(Context context) {
        this(context, null);
    }

    public CyeeSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setThumbDrawable(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_switch_thumb_material_anim));
        setTrackDrawable(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_switch_track_selector));
        changeColor();
    }

    private void changeColor() {
        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
            Resources iCyeeRes = mContext.getResources();
            Drawable bgDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_switch_bg_selector);
            setButtonDrawable(bgDrawable);
            setThumbDrawable(null);
            setTrackDrawable(null);
        } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            int colorId = com.cyee.internal.R.color.cyee_switch_thumb;
            mThumbColors = getContext().getResources().getColorStateList(colorId);
            changeThumbDrawable();
            ChangeTrackDrawableColor();
            Drawable drawable = getBackground();
            if (drawable != null && drawable instanceof RippleDrawable) {
                ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
            }
            // Gionee <weidong> <2017-07-21> add for 165025 begin
            ChangeColorUtil.changeTextViewTextColor(this);
            // Gionee <weidong> <2017-07-21> add for 165025 end
        }
    }

    private void ChangeTrackDrawableColor() {
        Drawable trackDrawable = getTrackDrawable();
        if (null == trackDrawable) {
            return;
        }

        if (trackDrawable instanceof StateListDrawable) {
            StateListDrawable stateListDrawable = (StateListDrawable) trackDrawable;
            for (int index = 0; index < stateListDrawable.getStateCount(); index++) {
                Drawable drawable = stateListDrawable.getStateDrawable(index);
                if (stateIsChecked(stateListDrawable.getStateSet(index))) {
                    drawable.setTintList(ColorStateList.valueOf(ChameleonColorManager.getAccentColor_G1()));
                } else {
                    drawable.setTintList(ColorStateList.valueOf(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()));
                }
            }
        } else if (trackDrawable instanceof LayerDrawable) {
            changeTrackDrawable(trackDrawable);
        } else {
            // Gionee <weidong> <2017-07-31> add for 177269 begin
            int colorChecked = ChameleonColorManager.getAccentColor_G1();
            int colorUnChecked = ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1();
            ColorStateList drawableColors = new ColorStateList(new int[][] { {-android.R.attr.state_checked, android.R.attr.state_enabled},
                    {-android.R.attr.state_checked, -android.R.attr.state_enabled}, {android.R.attr.state_checked, android.R.attr.state_enabled},
                    {android.R.attr.state_checked, -android.R.attr.state_enabled}, {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                    new int[] {colorUnChecked, getDisableColor(colorUnChecked), colorChecked, getDisableColor(colorChecked), colorUnChecked,
                            colorChecked});
            trackDrawable.setTintList(drawableColors);
            // Gionee <weidong> <2017-07-31> add for 177269 begin
        }
    }

    private int getDisableColor(int color) {
        ARGB argb = hexToARGB(color);
        argb.alpha *= 0.3; // add alpha 30%
        int newColor = ARGBToHex(argb);

        return newColor;
    }

    @Override
    protected void drawableStateChanged() {
        if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {

        } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
//            ChangeTrackDrawableColor();
        }
        super.drawableStateChanged();
    }

    private void changeTrackDrawable(Drawable trackDrawable) {
        LayerDrawable layerDrawable = (LayerDrawable) trackDrawable;
        int num = layerDrawable.getNumberOfLayers();
        for (int i = 0; i < num; i++) {
            Drawable drawable = layerDrawable.getDrawable(i);
            int colorChecked = ChameleonColorManager.getAccentColor_G1();
            int colorUnChecked = ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1();
            ColorStateList drawableColors = new ColorStateList(new int[][] { {android.R.attr.state_checked, android.R.attr.state_enabled},{}},
                    new int[] {colorChecked,colorUnChecked});
            drawable.setTintList(drawableColors);
        }
    }

    private void changeThumbDrawable() {
        Drawable drawable = getThumbDrawable();
        if (null == mThumbColors || null == drawable) {
            return;
        }
        drawable.setTintList(mThumbColors);
    }

    private boolean stateIsChecked(int[] myDrawableState) {
        for (int index = 0; index < myDrawableState.length; index++) {
            if (myDrawableState[index] == android.R.attr.state_checked) {
                return true;
            }
        }
        return false;
    }

    private int ARGBToHex(ARGB argb) {
        return argb.alpha << 24 | argb.red << 16 | argb.green << 8 | argb.blue;
    }

    private ARGB hexToARGB(int hexVal) {
        ARGB argb = new ARGB();
        argb.alpha = (hexVal >> 24) & 0xFF;
        argb.red = (hexVal >> 16) & 0xFF;
        argb.green = (hexVal >> 8) & 0xFF;
        argb.blue = hexVal & 0xFF;

        return argb;
    }

    private static class ARGB {
        public int alpha;
        public int red;
        public int green;
        public int blue;
    }
}
