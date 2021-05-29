package cyee.widget;

import java.util.Calendar;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeTimeDayPicker.LunarModeChangedListener;
import cyee.widget.CyeeTimeDayPicker.OnDateChangedListener;
import cyee.widget.CyeeTimeDayPicker.OnTimeChangedListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

public class CyeeTimeDayPickerDialog extends CyeeAlertDialog
        implements OnClickListener, OnDateChangedListener, OnTimeChangedListener {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";

    private static final int DAY_SPINNER_WIDTH_DP = 144;
    private static final int AMPM_SPINNER_WIDTH_DP = 54;
    private static final int HOUR_SPINNER_WIDTH_12_DP = 56;
    private static final int MINUTE_SPINNER_WIDTH__12_DP = 56;
    private static final int HOUR_SPINNER_WIDTH_24_DP = 80;
    private static final int MINUTE_SPINNER_WIDTH_24_DP = 80;
    
    private final Context mContext;

    private int mHour;

    private int mMinute;

    private final Calendar temp = Calendar.getInstance();

    private final CyeeTimeDayPicker mTimeDayPicker;

    private final CyeeTextView mTitleTv;

    private final OnDateSetListener mDateCallBack;

    private final OnTimeSetListener mTimeCallBack;

    private final LunarModeChangedListener mLunarModeChangedListener = new LunarModeChangedListener() {

        @Override
        public void onModeChanged(boolean isLunar) {
            updateTitle();
        }
    };

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /**
         * @param view
         *            The view associated with this listener.
         * @param year
         *            The year that was set.
         * @param monthOfYear
         *            The month that was set (0-11) for compatibility with {@link java.util.Calendar}.
         * @param dayOfMonth
         *            The day of the month that was set.
         */
        void onDateSet(CyeeTimeDayPicker picker, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * The callback interface used to indicate the user is done filling in the time (they clicked on the 'Set'
     * button).
     */
    public interface OnTimeSetListener {

        /**
         * @param view
         *            The view associated with this listener.
         * @param hourOfDay
         *            The hour that was set.
         * @param minute
         *            The minute that was set.
         */
        void onTimeSet(CyeeTimeDayPicker view, int hourOfDay, int minute);
    }

    public CyeeTimeDayPickerDialog(Context context, OnDateSetListener dateCallBack,
            OnTimeSetListener timeCallBack, int year, int monthOfYear, int dayOfMonth, int hourOfDay,
            int minute, boolean is24HourView) {
        this(context,
                resolvedTheme(context,
                        com.cyee.internal.R.attr.cyeedatePickerDialogStyle),
                dateCallBack, timeCallBack, year, monthOfYear, dayOfMonth, hourOfDay, minute, is24HourView);
        // TODO Auto-generated constructor stub
    }

    public CyeeTimeDayPickerDialog(Context context, int theme, OnDateSetListener dateCallBack,
            OnTimeSetListener timeCallBack, int year, int monthOfYear, int dayOfMonth, int hourOfDay,
            int minute, boolean is24HourView) {
        super(CyeeAlertDialog.getCyeeContext(context, theme), theme);
        mContext = context;
        mDateCallBack = dateCallBack;
        mTimeCallBack = timeCallBack;
        Context themeContext = getContext();
        setButton(BUTTON_POSITIVE,
                themeContext.getText(com.cyee.internal.R.string.cyee_ok),
                this);
        setButton(BUTTON_NEGATIVE,
                themeContext.getText(com.cyee.internal.R.string.cyee_cancel),
                this);
        setIcon(0);
        LayoutInflater inflater = (LayoutInflater) themeContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(
                com.cyee.internal.R.layout.cyee_timeday_picker_dialog, null);
        setView(view);
        mTimeDayPicker = (CyeeTimeDayPicker) view
                .findViewById(com.cyee.internal.R.id.cyee_time_day_spinner);
        mTimeDayPicker.setLunarModeChangedListener(mLunarModeChangedListener);
        mTimeDayPicker.setOnTimeChangedLinstener(this);
        mTimeDayPicker.setOnDateChangedListener(this);
        mTimeDayPicker.setIs24HourView(is24HourView);
        if (is24HourView) {
            mTimeDayPicker.setSpinnersWidth(dip2px(mContext, DAY_SPINNER_WIDTH_DP),
                    dip2px(mContext, AMPM_SPINNER_WIDTH_DP), dip2px(mContext, HOUR_SPINNER_WIDTH_24_DP)
                    , dip2px(mContext, MINUTE_SPINNER_WIDTH_24_DP));
        }else {
            mTimeDayPicker.setSpinnersWidth(dip2px(mContext, DAY_SPINNER_WIDTH_DP),
                    dip2px(mContext, AMPM_SPINNER_WIDTH_DP), dip2px(mContext, HOUR_SPINNER_WIDTH_12_DP)
                    , dip2px(mContext, MINUTE_SPINNER_WIDTH__12_DP));
        }
        mTitleTv = (CyeeTextView) view
                .findViewById(com.cyee.internal.R.id.title_tv);
        mTimeDayPicker.setCurrentDate(year, monthOfYear, dayOfMonth);
        mTimeDayPicker.setCurrentTime(hourOfDay, minute);
        mHour = mTimeDayPicker.getCurrentHour();
        mMinute = mTimeDayPicker.getCurrentMinute();
        updateTitle();
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mTimeDayPicker.getYear());
        state.putInt(MONTH, mTimeDayPicker.getMonth());
        state.putInt(DAY, mTimeDayPicker.getDayOfMonth());
        state.putInt(HOUR, mTimeDayPicker.getCurrentHour());
        state.putInt(MINUTE, mTimeDayPicker.getCurrentMinute());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mHour = savedInstanceState.getInt(HOUR);
        mMinute = savedInstanceState.getInt(MINUTE);
        
        mTimeDayPicker.init(year, month, day, this);
        mTimeDayPicker.setCurrentTime(mHour, mMinute);
    }

    @Override
    public void onTimeChanged(CyeeTimeDayPicker picker, int hourOfDay, int minute) {
        // TODO Auto-generated method stub
        mHour = hourOfDay;
        mMinute = minute;
        updateTitle();
    }

    @Override
    public void onDateChanged(CyeeTimeDayPicker picker, int year, int monthOfYear, int dayOfMonth) {
        // TODO Auto-generated method stub
        updateTitle();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        if (which == DialogInterface.BUTTON_POSITIVE) {
            tryNotifyDateSet();
        }
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView
     *            True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourFormat(Boolean is24HourView) {
        mTimeDayPicker.setIs24HourView(is24HourView);
    }

    public boolean is24HourFormat() {
        return mTimeDayPicker.is24HourView();
    }

    public CyeeTimeDayPicker getTimeDayPicker() {
        return mTimeDayPicker;
    }

    /**
     * Sets the current date.
     *
     * @param year
     *            The date year.
     * @param monthOfYear
     *            The date month.
     * @param dayOfMonth
     *            The date day of month.
     */
    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mTimeDayPicker.setCurrentDate(year, monthOfYear, dayOfMonth);
    }

    /**
     * Set the current time
     * 
     * @param hour
     *            The hour of day
     * @param minute
     *            The minute of hour
     */
    public void updateTime(int hour, int minute) {
        mTimeDayPicker.setCurrentTime(hour, minute);
    }

    public void setLunarChecked(boolean isLunarMode) {
        mTimeDayPicker.setLunarChecked(isLunarMode);
    }

    public void showLunarModeSwitch() {
        mTimeDayPicker.showLunarModeSwitch();
    }

    public void hideLunarModeSwitch() {
        mTimeDayPicker.hideLunarModeSwitch();
    }

    private static int resolvedTheme(Context context, int resId) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.resourceId;
    }

    private void updateTitle() {
        mTitleTv.setText(buildTitle());
    }

    private String buildTitle() {
        Calendar current = mTimeDayPicker.getCurrentDate();
        temp.clear();
        temp.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
        temp.set(Calendar.HOUR_OF_DAY, mHour);
        temp.set(Calendar.MINUTE, mMinute);
        return DateUtils.formatDateTime(mContext, temp.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY
                        | DateUtils.FORMAT_SHOW_TIME);
    }

    private void tryNotifyDateSet() {

        if (mDateCallBack != null || mTimeCallBack != null) {
            mTimeDayPicker.clearFocus();
        }
        if (mDateCallBack != null) {
            mDateCallBack.onDateSet(mTimeDayPicker, mTimeDayPicker.getYear(), mTimeDayPicker.getMonth(),
                    mTimeDayPicker.getDayOfMonth());
        }
        if (mTimeCallBack != null) {
            mTimeCallBack.onTimeSet(mTimeDayPicker, mTimeDayPicker.getCurrentHour(),
                    mTimeDayPicker.getCurrentMinute());
        }
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
