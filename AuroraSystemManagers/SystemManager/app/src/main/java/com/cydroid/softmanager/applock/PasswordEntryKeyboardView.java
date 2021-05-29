package com.cydroid.softmanager.applock;

/**
 * @author xionghg
 * @created 17-10-31.
 */

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

// a copy of com.android.internal.widget.PasswordEntryKeyboardView from sdk 24
public class PasswordEntryKeyboardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    static final int KEYCODE_SHIFT_LONGPRESS = -101;
    static final int KEYCODE_VOICE = -102;
    static final int KEYCODE_F1 = -103;
    static final int KEYCODE_NEXT_LANGUAGE = -104;

    public PasswordEntryKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordEntryKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PasswordEntryKeyboardView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean setShifted(boolean shifted) {
        boolean result = super.setShifted(shifted);
        // invalidate both shift keys
        int[] indices = getKeyboard().getShiftKeyIndices();
        for (int index : indices) {
            invalidateKey(index);
        }
        return result;
    }

}
