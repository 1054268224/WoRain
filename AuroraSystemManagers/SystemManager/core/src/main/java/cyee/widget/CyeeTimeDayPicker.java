package cyee.widget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import com.cyee.internal.util.Lunar;

import com.cyee.internal.R;
import com.cyee.internal.R.integer;
import cyee.widget.CyeeDatePicker.LunarDate;
import cyee.widget.CyeeNumberPicker.OnValueChangeListener;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

public class CyeeTimeDayPicker extends LinearLayout {

    private static final String TAG = "CyeeTimeDayPicker";

    private static final int HEIGHT_PORTRAIT = 180;

    private static final int WHEEL_COUNT_PORTRAIT = 5;

    private static final int HEIGHT_LANDSCAPE = 140;

    private static final int WHEEL_COUNT_LANDSCAPE = 3;

    private static final int DEFAULT_START_YEAR = 1900;
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final int DEFAULT_END_YEAR = 2560;

    private static final int LUNAR_SUPPORT_MIN_YEAR = 1900;
    private static final int LUNAR_SUPPORT_MAX_YEAR = 2099;
    private static final boolean DEFAULT_ENABLED_STATE = true;

    private static final int DEFAULT_INIT_DISPLAY_VALUE_COUNT = 11;

    private final CyeeNumberPicker mDaySpinner;
    private final CyeeTimePicker mTimeSpinner;
    private final CyeeSwitch mLunarSwitch;
    private final LinearLayout mLunarLayout;

    private final int mVerticalPadding;
    private final int mHorizontalPadding;

    private boolean isLunarMode = false;

    private OnDateChangedListener mOnDateChangedListener;
    private LunarModeChangedListener mLunarModeChangedListener;
    private OnTimeChangedListener mOnTimeChangedListener;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;
    private final java.text.DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);

    private Locale mCurrentLocale;

    private Calendar mTempDate;

    private Calendar mMinDate;

    private Calendar mMaxDate;

    private Calendar mCurrentDate;

    private LunarDate mMaxLunarDate;

    private LunarDate mMinLunarDate;

    private final int mStartYear;

    private final int mEndYear;

    private String[] mDayDisplayValues;

    private boolean mIs24HourView;

    private static final String[] sUnitLanguage = {"CN", "TW", "KP"};

    private final OnValueChangeListener mValueChangeListener = new OnValueChangeListener() {

        @Override
        public void onValueChange(CyeeNumberPicker picker, int oldValue, int newValue) {
            // TODO Auto-generated method stub
            CyeeTimeDayPicker.this.changeDays(picker, newValue, oldValue);
        }
    };

    private final cyee.widget.CyeeTimePicker.OnTimeChangedListenerInternal mTimeChangedListener = new CyeeTimePicker.OnTimeChangedListenerInternal() {

        @Override
        public void onTimeChanged(CyeeTimePicker view, int oldHour, int oldMinute, int newHour,
                int newMinute) {
            // TODO Auto-generated method stub
            notifyTimeChanged(CyeeTimeDayPicker.this, newHour, newMinute);
        }
    };

    private final OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setLunarMode(isChecked);
            if (mLunarModeChangedListener != null) {
                mLunarModeChangedListener.onModeChanged(isChecked);
            }
        }
    };

    public CyeeTimeDayPicker(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public CyeeTimeDayPicker(Context context, AttributeSet attrs) {
        this(context, attrs, com.cyee.internal.R.attr.cyeeTimeDayPickerStyle);
        // TODO Auto-generated constructor stub
    }

    public CyeeTimeDayPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        this.setOrientation(LinearLayout.VERTICAL);

        // obtain the layout data
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.CyeeTimeDayPicker,
                defStyleAttr, 0);
        int layoutId = attributesArray.getResourceId(R.styleable.CyeeTimeDayPicker_cyeeTimeDayPickerLayout,
                com.cyee.internal.R.layout.cyee_timeday_picker);
        mVerticalPadding = (int) context.getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_timeday_picker_vertical);
        mHorizontalPadding = (int) context.getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_timeday_picker_horizontal);
        mStartYear = attributesArray.getInt(R.styleable.CyeeTimeDayPicker_cyeeTimeDayPickerStartYear,
                DEFAULT_START_YEAR);
        mEndYear = attributesArray.getInt(R.styleable.CyeeTimeDayPicker_cyeeTimeDayPickerEndYear,
                DEFAULT_END_YEAR);
        final String minDate = attributesArray.getString(R.styleable.CyeeTimeDayPicker_cyeeMinDate);
        final String maxDate = attributesArray.getString(R.styleable.CyeeTimeDayPicker_cyeeMaxDate);
        attributesArray.recycle();
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutId, this, true);

        mDaySpinner = (CyeeNumberPicker) findViewById(
                com.cyee.internal.R.id.cyee_day_spinner);
        mTimeSpinner = (CyeeTimePicker) findViewById(
                com.cyee.internal.R.id.cyee_time_spinner);
        mLunarSwitch = (CyeeSwitch) findViewById(
                com.cyee.internal.R.id.cyee_lunar_switch);
        LinearLayout spinners = (LinearLayout) findViewById(
                com.cyee.internal.R.id.cyee_timeday_frame);
        // clear paddings in xml file of CyeeTimePicker
        LinearLayout timeLayout = (LinearLayout) findViewById(
                com.cyee.internal.R.id.cyee_time_picker);
        mLunarLayout = (LinearLayout) findViewById(
                com.cyee.internal.R.id.cyee_lunar_mode_rl);
        // mLunarLayout.setVisibility(View.GONE);
        timeLayout.setPadding(0, 0, 0, 0);
        mTimeSpinner.setTextStable(true);
        mLunarLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mLunarSwitch.setChecked(!mLunarSwitch.isChecked());
            }
        });

        mLunarSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
        // set our layout parameters
        spinners.setPadding(mHorizontalPadding, 0, mHorizontalPadding, 0);
        // this.setPadding(0, mVerticalPadding, 0, mVerticalPadding);

        setCurrentLocale(Locale.getDefault());
        mTempDate = getCalendarForLocale(mCurrentDate, mCurrentLocale);

        initMinDate(minDate, mStartYear);
        initMaxDate(maxDate, mEndYear);
        initCurrentDate();
        initDaySpinner();
        updateTimeSpinner();
    }

    public void setCurrentDate(int year, int month, int dayofMonth) {
        // demo
        mTempDate.clear();
        mTempDate.set(year, month, dayofMonth);
        if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
            throw new IllegalStateException("date out of index");
        }
        mCurrentDate.set(year, month, dayofMonth);
        notifyDateChanged();
        initDaySpinner();
        invalidate();
    }

    public void setCurrentTime(int hour, int minute) {
        // demo
        if (hour == mTimeSpinner.getCurrentHour() && minute == mTimeSpinner.getCurrentMinute()) {
            return;
        }
        mTimeSpinner.setCurrentHour(hour);
        mTimeSpinner.setCurrentMinute(minute);
        notifyTimeChanged(this, hour, minute);
    }

    public Calendar getCurrentDate() {
        return mCurrentDate;
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView
     *            True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourView(Boolean is24HourView) {
        if (mIs24HourView == is24HourView) {
            return;
        }
        mIs24HourView = is24HourView;
        mTimeSpinner.setIs24HourView(is24HourView);
    }

    /**
     * @return The selected year.
     */
    public int getYear() {
        return mCurrentDate.get(Calendar.YEAR);
    }

    /**
     * @return The selected month.
     */
    public int getMonth() {
        return mCurrentDate.get(Calendar.MONTH);
    }

    /**
     * @return The selected day of month.
     */
    public int getDayOfMonth() {
        return mCurrentDate.get(Calendar.DAY_OF_MONTH);
    }

    public Long getCurrentTimeMillis() {
        mTempDate.clear();
        mTempDate.set(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DAY_OF_MONTH), mTimeSpinner.getCurrentHour(),
                mTimeSpinner.getCurrentMinute());
        return mTempDate.getTimeInMillis();
    }

    public int getCurrentHour() {
        return mTimeSpinner.getCurrentHour();
    }

    public int getCurrentMinute() {
        return mTimeSpinner.getCurrentMinute();
    }

    private void setLunarMode(boolean isLunarMode) {
        if (isLunarMode == this.isLunarMode)
            return;

        this.isLunarMode = isLunarMode;
        initDaySpinner();
        mDaySpinner.invalidate();
    }

    /**
     * Sets the minimal date supported by this {@link NumberPicker} in milliseconds since January 1, 1970
     * 00:00:00 in {@link TimeZone#getDefault()} time zone.
     *
     * @param minDate
     *            The minimal supported date.
     */
    public void setMinDate(long minDate) {
        mTempDate.setTimeInMillis(minDate);
        if (mTempDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        /**
         * I think there is something wrong with these code, it will override the min date specified by custom
         * So delete it!
         */
        Calendar min = null;
        min = getCalendarForLocale(min, Locale.getDefault());
        min.set(mStartYear, 0, 1);

        if (minDate < min.getTimeInMillis()) {
            minDate = min.getTimeInMillis();
        }
        /**
         * I think there is something wrong with these code, it will override the min date specified by custom
         * So delete it!
         */
        mMinDate.setTimeInMillis(minDate);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        }

        final Calendar minSupport = getLunarSupportMinDay();
        final Calendar calendar = mMinDate.before(minSupport) ? minSupport : mMinDate;
        mMinLunarDate = getLunarDate(calendar.getTimeInMillis());
        /**
         * TO DO update views
         */
    }

    /**
     * Sets the maximal date supported by this {@link CyeeDatePicker} in milliseconds since January 1, 1970
     * 00:00:00 in {@link TimeZone#getDefault()} time zone.
     *
     * @param maxDate
     *            The maximal supported date.
     */
    public void setMaxDate(long maxDate) {
        mTempDate.setTimeInMillis(maxDate);
        if (mTempDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMaxDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }

        /**
         * I think there is something wrong with these code, it will override the max date specified by custom
         * So delete it!
         */
        Calendar max = null;
        max = getCalendarForLocale(max, Locale.getDefault());
        max.set(mEndYear, 11, 31);

        if (maxDate > max.getTimeInMillis()) {
            maxDate = max.getTimeInMillis();
        }
        /**
         * I think there is something wrong with these code, it will override the max date specified by custom
         * So delete it!
         */
        mMaxDate.setTimeInMillis(maxDate);
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }

        final Calendar maxSupport = getLunarSupportMaxDay();
        final Calendar calendar = mMaxDate.after(maxSupport) ? maxSupport : mMaxDate;
        mMaxLunarDate = getLunarDate(calendar.getTimeInMillis());
        /**
         * TO DO update views
         */
    }

    public Calendar getLunarSupportMinDay() {
        Calendar min = null;
        min = getCalendarForLocale(min, mCurrentLocale);
        min.set(LUNAR_SUPPORT_MIN_YEAR, 0, 1);
        return min;
    }

    public Calendar getLunarSupportMaxDay() {
        Calendar max = null;
        max = getCalendarForLocale(max, mCurrentLocale);
        max.set(LUNAR_SUPPORT_MAX_YEAR, 11, 31);
        return max;
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        mOnDateChangedListener = onDateChangedListener;
    }

    // Override so we are in complete control of save / restore for this widget.
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getYear(), getMonth(), getDayOfMonth(), getCurrentHour(),
                getCurrentMinute());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mTimeSpinner.setCurrentHour(ss.mHour);
        mTimeSpinner.setCurrentMinute(ss.mMinute);
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        initDaySpinner();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mDaySpinner.setEnabled(enabled);
        mTimeSpinner.setEnabled(enabled);
        mLunarSwitch.setEnabled(enabled);
        mIsEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);

        final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
        String selectedDateUtterance = DateUtils.formatDateTime(mContext, mCurrentDate.getTimeInMillis(),
                flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(CyeeDatePicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(CyeeDatePicker.class.getName());
    }

    public void setOnTimeChangedLinstener(OnTimeChangedListener listener) {
        this.mOnTimeChangedListener = listener;
    }

    private void updateTimeSpinner() {
        mTimeSpinner.setIs24HourView(mIs24HourView);
        mTimeSpinner.setCurrentHour(mCurrentDate.get(Calendar.HOUR_OF_DAY));
        mTimeSpinner.setCurrentMinute(mCurrentDate.get(Calendar.MINUTE));
        mTimeSpinner.setOnTimeChangedListenerInternal(mTimeChangedListener);
    }

    /**
     * set the min date giving priority of the minDate over startYear
     */
    private void initMinDate(String minDate, int startYear) {
        mTempDate.clear();
        if (!TextUtils.isEmpty(minDate)) {
            if (!parseDate(minDate, mTempDate)) {
                mTempDate.set(startYear, 0, 1);
            }
        } else {
            mTempDate.set(startYear, 0, 1);
        }
        setMinDate(mTempDate.getTimeInMillis());
    }

    /**
     * set the max date giving priority of the maxDate over endYear
     */
    private void initMaxDate(String maxDate, int endYear) {
        mTempDate.clear();
        if (!TextUtils.isEmpty(maxDate)) {
            if (!parseDate(maxDate, mTempDate)) {
                mTempDate.set(endYear, 11, 31);
            }
        } else {
            mTempDate.set(endYear, 11, 31);
        }
        setMaxDate(mTempDate.getTimeInMillis());
    }

    /**
     * Parses the given <code>date</code> and in case of success sets the result to the <code>outDate</code>.
     *
     * @return True if the date was parsed.
     */
    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    /**
     * Gets a calendar for locale bootstrapped with the value of a given calendar.
     *
     * @param oldCalendar
     *            The old calendar.
     * @param locale
     *            The locale.
     */
    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }

    /**
     * Sets the current locale.
     *
     * @param locale
     *            The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }

        mCurrentLocale = locale;

        mTempDate = getCalendarForLocale(mTempDate, locale);
        mMinDate = getCalendarForLocale(mMinDate, locale);
        mMaxDate = getCalendarForLocale(mMaxDate, locale);
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale);
        mMinLunarDate = getLunarDate(mMinDate.getTimeInMillis());
        mMaxLunarDate = getLunarDate(mMaxDate.getTimeInMillis());
    }

    private void initCurrentDate() {
        mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DAY_OF_MONTH), null);
    }

    /**
     * Initialize the state. If the provided values designate an inconsistent date the values are normalized
     * before updating the spinners.
     *
     * @param year
     *            The initial year.
     * @param monthOfYear
     *            The initial month <strong>starting from zero</strong>.
     * @param dayOfMonth
     *            The initial day of the month.
     * @param onDateChangedListener
     *            How user is notified date is changed by user, can be null.
     */
    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        setDate(year, monthOfYear, dayOfMonth);
        initDaySpinner();
        mOnDateChangedListener = onDateChangedListener;
    }

    private void setDate(int year, int month, int dayOfMonth) {
        mCurrentDate.set(year, month, dayOfMonth);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
    }

    private boolean shouldShowUnit() {
        boolean show = false;
        String curLanguage = getResources().getConfiguration().locale.getCountry();
        for (String l : sUnitLanguage) {
            if (l.equals(curLanguage)) {
                show = true;
                break;
            }
        }

        return show;
    }

    public boolean isLunarMode() {
        return isLunarMode;
    }

    public void showLunarModeSwitch() {
        mLunarLayout.setVisibility(View.VISIBLE);
        adjustWheelCount();
    }

    public void hideLunarModeSwitch() {
        mLunarLayout.setVisibility(View.GONE);
        adjustWheelCount();
    }

    public void setLunarModeChangedListener(LunarModeChangedListener listener) {
        this.mLunarModeChangedListener = listener;
    }

    public void setLunarChecked(boolean isLunarMode) {
        mLunarSwitch.setChecked(isLunarMode);
    }

    public void setSpinnersWidth(int dayWidth, int ampmWidth, int hourWidth, int minuteWidth) {
        mDaySpinner.getLayoutParams().width = dayWidth;
        mTimeSpinner.setSpinnersWidth(ampmWidth, hourWidth, minuteWidth);
    }

    /**
     * Sets the IME options for a spinner based on its ordering.
     *
     * @param spinner
     *            The spinner.
     * @param spinnerCount
     *            The total spinner count.
     * @param spinnerIndex
     *            The index of the given spinner.
     */
    private void setImeOptions(CyeeNumberPicker spinner, int spinnerCount, int spinnerIndex) {
        final int imeOptions;
        if (spinnerIndex < spinnerCount - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT;
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE;
        }
        TextView input = (TextView) spinner
                .findViewById(com.cyee.internal.R.id.cyee_numberpicker_input);
        input.setImeOptions(imeOptions);
    }

    private void updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        InputMethodManager inputMethodManager = (InputMethodManager) (mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE));/*InputMethodManager.peekInstance();*/
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mDaySpinner)) {
                mDaySpinner.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    private LunarDate getLunarDate(long millis) {
        LunarDate lunarDate = new LunarDate();
        int[] curLunarDate = Lunar.solarToLunar(millis);
        if (null == curLunarDate) {
            return lunarDate;
        }
        int maxLunarYear = curLunarDate[0];

        lunarDate.mLunarY = maxLunarYear;

        // month
        int leapMonth = Lunar.leapMonth(maxLunarYear);
        int currentLunarMonth = curLunarDate[1];
        lunarDate.mTmpLunarM = currentLunarMonth;
        boolean isCurrentLeapMonth = curLunarDate[3] == 1;

        if (leapMonth > 0) {
            // 闰月之后的所有月份，＋1
            if (currentLunarMonth > leapMonth) {
                currentLunarMonth++;
            }
            // 闰月，＋1
            if (currentLunarMonth == leapMonth && isCurrentLeapMonth) {
                currentLunarMonth++;
            }
        }
        lunarDate.mLunarM = currentLunarMonth;

        // day
        int currentLunarDay = curLunarDate[2];
        lunarDate.mLunarD = currentLunarDay;
        lunarDate.isLeap = curLunarDate[3];

        return lunarDate;
    }

    private void initDaySpinner() {
        mDaySpinner.setTextStable(true);
        mDaySpinner.resetMinValue(Integer.MAX_VALUE / 2 - DEFAULT_INIT_DISPLAY_VALUE_COUNT / 2);
        mDaySpinner.resetMaxValue(Integer.MAX_VALUE / 2 + DEFAULT_INIT_DISPLAY_VALUE_COUNT / 2);
        mDaySpinner.setValue(Integer.MAX_VALUE / 2);
        mTempDate.set(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DAY_OF_MONTH));
        mDayDisplayValues = generateDayDisplayValues(mTempDate, false);
        mDaySpinner.setDisplayedValues(mDayDisplayValues);
        mTempDate.clear();
        mDaySpinner.setOnValueChangedListener(mValueChangeListener);
    }

    private void changeDays(CyeeNumberPicker picker, int newValue, int oldValue) {
        int delt = newValue - oldValue;
        if (delt > 0) {
            delt = DEFAULT_INIT_DISPLAY_VALUE_COUNT / 2;
        } else {
            delt = -(DEFAULT_INIT_DISPLAY_VALUE_COUNT / 2);
        }
        mCurrentDate.add(Calendar.DATE, newValue - oldValue);
        // restore mCurrentDate and do nothing
        if (mCurrentDate.before(mMinDate) || mCurrentDate.after(mMaxDate)) {
            mCurrentDate.add(Calendar.DATE, oldValue - newValue);
            Log.d(TAG, "out of index");
            return;
        }

        updateInputState();
        mTempDate.clear();
        mTempDate.set(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DAY_OF_MONTH));
        mTempDate.add(Calendar.DATE, delt);
        if (newValue > oldValue) {
            picker.changeOneSilently(generateDayDisplayValues(mTempDate, true)[0], true);
        } else if (picker.getMinValue() > 1) {
            picker.changeOneSilently(generateDayDisplayValues(mTempDate, true)[0], false);
        }
        notifyDateChanged();
    }

    private void notifyDateChanged() {
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, mCurrentDate.get(Calendar.YEAR),
                    mCurrentDate.get(Calendar.MONTH), mCurrentDate.get(Calendar.DAY_OF_MONTH));
        }
    }

    private void notifyTimeChanged(CyeeTimeDayPicker picker, int hourOfDay, int minute) {
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(picker, hourOfDay, minute);
        }
    }

    private String[] generateDayDisplayValues(Calendar calendar, boolean onlyOne) {
        if (isLunarMode) {
            return generateDayLunarDisplayValues(calendar, onlyOne);
        } else {
            return generateSonarDayDisplayValues(calendar, onlyOne);
        }
    }

    private String[] generateSonarDayDisplayValues(Calendar calendar, boolean onlyOne) {
        if (calendar == null) {
            return null;
        }

        int dayCount = 1;
        if (!onlyOne) {
            dayCount = DEFAULT_INIT_DISPLAY_VALUE_COUNT;
        }
        calendar.add(Calendar.DATE, -(dayCount / 2));
        String dayUnit = "";
        if (isChineseLanguage()) {
            dayUnit = " " + getResources()
                    .getString(com.cyee.internal.R.string.unit_day);
        }
        String[] displayValues = new String[dayCount];
        for (int i = 0; i < dayCount; i++) {
            if (DateUtils.isToday(calendar.getTimeInMillis())) {
                displayValues[i] = mContext.getResources()
                        .getString(com.cyee.internal.R.string.cyee_today);
            } else {
                displayValues[i] = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                        Locale.getDefault()) + calendar.get(Calendar.DATE) + dayUnit
                        + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            }
            calendar.add(Calendar.DATE, 1);
        }
        return displayValues;
    }

    private String[] generateDayLunarDisplayValues(Calendar calendar, boolean onlyOne) {
        if (calendar == null) {
            return null;
        }

        int dayCount = 1;
        if (!onlyOne) {
            dayCount = DEFAULT_INIT_DISPLAY_VALUE_COUNT;
        }
        calendar.add(Calendar.DATE, -(dayCount / 2));

        String[] displayValues = new String[dayCount];
        for (int i = 0; i < dayCount; i++) {
            if (DateUtils.isToday(calendar.getTimeInMillis())) {
                displayValues[i] = mContext.getResources()
                        .getString(com.cyee.internal.R.string.cyee_today);
            } else {
                if (calendar.after(getLunarSupportMaxDay())) {
                    calendar = getLunarSupportMaxDay();
                    notifyDateChanged();
                }
                LunarDate lunarDate = getLunarDate(calendar.getTimeInMillis());
                displayValues[i] = Lunar.getMonthsNameInYear(lunarDate.mLunarY).get(lunarDate.mLunarM - 1)
                        + getLunarDayNameInMonth(lunarDate)
                        + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            }
            calendar.add(Calendar.DATE, 1);
        }
        return displayValues;
    }

    private String getLunarDayNameInMonth(LunarDate lunarDate) {
        if (lunarDate == null)
            return null;

        boolean isCurrentLeapMonth = lunarDate.isLeap == 1;
        int daysInMonth = Lunar.daysInMonth(lunarDate.mLunarY, lunarDate.mTmpLunarM, isCurrentLeapMonth);
        return Lunar.getDaysDisplayName(daysInMonth)[lunarDate.mLunarD - 1];
    }

    private void adjustWheelCount() {
        if (mLunarLayout.getVisibility() == View.VISIBLE && mContext.getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mDaySpinner.setDisplayedWheelCount(WHEEL_COUNT_LANDSCAPE);
            mDaySpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_LANDSCAPE);
            mTimeSpinner.adjustWheelCount(true);
        } else {
            mDaySpinner.setDisplayedWheelCount(WHEEL_COUNT_PORTRAIT);
            mDaySpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_PORTRAIT);
            mTimeSpinner.adjustWheelCount(false);
        }
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private boolean isChineseLanguage() {
        return getResources().getConfiguration().locale.getCountry().equals("CN")
                || getResources().getConfiguration().locale.getCountry().equals("TW");
    }

    /**
     * Class for managing state storing/restoring.
     */
    private static class SavedState extends BaseSavedState {

        private final int mYear;

        private final int mMonth;

        private final int mDay;

        private final int mHour;

        private final int mMinute;

        /**
         * Constructor called from {@link CyeeDatePicker#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, int year, int month, int day, int hour, int minute) {
            super(superState);
            mYear = year;
            mMonth = month;
            mDay = day;
            mHour = hour;
            mMinute = minute;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mMonth = in.readInt();
            mDay = in.readInt();
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mYear);
            dest.writeInt(mMonth);
            dest.writeInt(mDay);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }

        @SuppressWarnings("all")
        // suppress unused and hiding
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * The callback used to indicate the user changes\d the date.
     */
    public interface OnDateChangedListener {

        /**
         * Called upon a date change.
         *
         * @param view
         *            The view associated with this listener.
         * @param year
         *            The year that was set.
         * @param monthOfYear
         *            The month that was set (0-11) for compatibility with {@link java.util.Calendar}.
         * @param dayOfMonth
         *            The day of the month that was set.
         */
        void onDateChanged(CyeeTimeDayPicker view, int year, int monthOfYear,
                           int dayOfMonth);
    }

    /**
     * 
     * 显示模式切换，农历、公立
     */
    public interface LunarModeChangedListener {
        void onModeChanged(boolean isLunar);
    }

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view
         *            The view associated with this listener.
         * @param hourOfDay
         *            The current hour.
         * @param minute
         *            The current minute.
         */
        void onTimeChanged(CyeeTimeDayPicker view, int hourOfDay, int minute);

    }
}
