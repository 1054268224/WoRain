package cyee.widget;

import java.util.Calendar;

import cyee.app.CyeeAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.cyee.utils.Log;
import android.view.ViewGroup;

public class CyeeDateTimePickerDialog extends CyeeAlertDialog implements OnClickListener {

    private static final String TAG = "CyeeDateTimePickerDialog";
    private static final String CALENDAR = "calendar";
    private final CyeeDateTimePicker mDateTimePicker;
    private final OnDateTimeSetListener mOnDateTimeSetListener;

    public interface OnDateTimeSetListener {

        void onDateTimeSet(Calendar calendar);
    }

    public CyeeDateTimePickerDialog(Context context, OnDateTimeSetListener onDateTimeSetListener,
            Calendar calendar) {
        this(context, CyeeAlertDialog.resolveDialogTheme(context, 0), onDateTimeSetListener, calendar);
    }

    private CyeeDateTimePickerDialog(Context context, int theme, OnDateTimeSetListener onDateTimeSetListener,
            Calendar calendar) {
        super(CyeeAlertDialog.getCyeeContext(context, theme), theme);

        mDateTimePicker = new CyeeDateTimePicker(context, null, 0, 0);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mDateTimePicker.setLayoutParams(lp);
        mDateTimePicker.updateCalendar(calendar);

        setButton(BUTTON_POSITIVE,
                context.getText(com.cyee.internal.R.string.cyee_ok), this);
        setButton(BUTTON_NEGATIVE,
                context.getText(com.cyee.internal.R.string.cyee_cancel), this);
        setIcon(0);
        setView(mDateTimePicker);
        mOnDateTimeSetListener = onDateTimeSetListener;

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            tryNotifyDateTimeSet();
        }
    }

    private void tryNotifyDateTimeSet() {
        if (mOnDateTimeSetListener == null) {
            Log.e(TAG, "tryNotifyDateTimeSet() mOnDateTimeSetListener == null");
            return;
        }

        Calendar calendar = mDateTimePicker.getCalendar();
        if (calendar == null) {
            Log.e(TAG, "tryNotifyDateTimeSet() calendar == null");
            return;
        }
        // Gionee <weidong> <2017-7-18> modify for 164970 begin
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // Gionee <weidong> <2017-7-18> modify for 164970 end
        mOnDateTimeSetListener.onDateTimeSet(calendar);
    }

    public void updateDate(Calendar calendar) {
        if (calendar == null) {
            Log.e(TAG, "updateDate calendar == null");
            return;
        }
        mDateTimePicker.updateCalendar(calendar);
    }

    public void set24HourFormat(boolean is24HourFormat) {
        mDateTimePicker.set24HourFormat(is24HourFormat);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle savedInstanceState = super.onSaveInstanceState();
        Calendar calendar = mDateTimePicker.getCalendar();
        if (calendar != null) {
            savedInstanceState.putSerializable(CALENDAR, calendar);
        }
        return savedInstanceState;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Calendar calendar = (Calendar) savedInstanceState.get(CALENDAR);
        mDateTimePicker.updateCalendar(calendar);
    }

    public void showLunarModeSwitch() {
        mDateTimePicker.showLunarModeSwitch();
    }

    public void hideLunarModeSwitch() {
        mDateTimePicker.hideLunarModeSwitch();
    }

    public void setLunarChecked(boolean isLunarMode) {
        mDateTimePicker.setLunarChecked(isLunarMode);
    }

    public void setMinDate(long minDate) {
        mDateTimePicker.setMinDate(minDate);
    }

    public void setMaxDate(long maxDate) {
        mDateTimePicker.setMaxDate(maxDate);
    }

    public void setMinHour(int minHour) {
        mDateTimePicker.setMinHour(minHour);
    }

    public void setMinMinute(int minMinute) {
        mDateTimePicker.setMinMinute(minMinute);
    }

    public void setMaxMinute(int maxMinute) {
        mDateTimePicker.setMaxMinute(maxMinute);
    }

    public void setMaxHour(int maxHour) {
        mDateTimePicker.setMaxHour(maxHour);
    }

    public void setCurrentPage(int index) {
        mDateTimePicker.setCurrentPage(index);
    }
}
