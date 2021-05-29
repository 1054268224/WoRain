package cyee.preference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.cyee.internal.R;
import cyee.widget.CyeeWidgetResource;

/**
 * @hide
 */
public class CyeeSeekBarDialogPreference extends CyeeDialogPreference {
    private static final String TAG = "SeekBarDialogPreference";

    private final Drawable mMyIcon;

    public CyeeSeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(com.cyee.internal.R.layout.cyee_seekbar_dialog);
        createActionButtons();

        // Steal the XML dialogIcon attribute's value
        mMyIcon = getDialogIcon();
        setDialogIcon(null);
    }

    // Allow subclasses to override the action buttons
    public void createActionButtons() {
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
        if (mMyIcon != null) {
            iconView.setImageDrawable(mMyIcon);
        } else {
            iconView.setVisibility(View.GONE);
        }
    }

    protected static SeekBar getSeekBar(View dialogView) {
        return (SeekBar) dialogView.findViewById(com.cyee.internal.R.id.cyee_seekbar);
    }
}
