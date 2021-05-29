package cyee.changecolors;

import android.content.res.ColorStateList;
import android.widget.TextView;

public class ChangeColorUtil {

    private static final int COLOR_PRIMARY_ON_BACKGROUD = 0xCC000000; // C1
    private static final int COLOR_SECONDARY_ON_BACKGROUD = 0x66000000; // C2
    private static final int COLOR_THIRDLY_ON_BACKGROUD = 0x33000000; // C3
    private static final int COLOR_PRIMARY_ON_APPBAR = 0x99000000; // T1
    private static final int COLOR_SECONDARY_ON_APPBAR = 0xccffffff; // T2
    private static final int COLOR_THIRDLY_ON_APPBAR = 0x50ffffff; // T3
    private static final int COLOR_FORTHLY_ON_APPBAR = 0x10ffffff; // T4
    private static final int ACCENT_COLOR = 0xff00a3e4; // G1

    static public void changeTextViewTextColor(TextView textView) {
        if (ChameleonColorManager.isNeedChangeColor(textView.getContext())) {
            ColorStateList colorStateList = textView.getTextColors();
            int[] colors = colorStateList.getColors();
            int[] textColors = new int[colors.length];
            for (int index = 0; index < colors.length; index++) {
                textColors[index] = changeTextColor(colors[index]);
            }

            textView.setTextColor(new ColorStateList(
                    colorStateList.getStates(), textColors));
        }
    }

    static private int changeTextColor(int color) {

        if (color == COLOR_PRIMARY_ON_BACKGROUD) {
            return ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1();
        }

        if (color == COLOR_SECONDARY_ON_BACKGROUD) {
            return ChameleonColorManager
                    .getContentColorSecondaryOnBackgroud_C2();
        }

        if (color == COLOR_THIRDLY_ON_BACKGROUD) {
            return ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3();
        }

        if (color == COLOR_PRIMARY_ON_APPBAR) {
            return ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
        }

        if (color == COLOR_SECONDARY_ON_APPBAR) {
            return ChameleonColorManager.getContentColorSecondaryOnAppbar_T2();
        }

        if (color == COLOR_THIRDLY_ON_APPBAR) {
            return ChameleonColorManager.getContentColorThirdlyOnAppbar_T3();
        }

        if (color == COLOR_FORTHLY_ON_APPBAR) {
            return ChameleonColorManager.getContentColorForthlyOnAppbar_T4();
        }

        if (color == ACCENT_COLOR) {
            return ChameleonColorManager.getAccentColor_G1();
        }

        return color;
    }

}
