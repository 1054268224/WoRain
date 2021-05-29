package cyee.widget;

import android.annotation.Size;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Paint.Align;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;
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
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import cyee.widget.CyeeMagicBar.onMoreItemSelectedListener;
import cyee.widget.CyeeNumberPicker.OnScrollListener;
import cyee.widget.CyeeNumberPicker.OnValueChangeListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.cyee.internal.util.Lunar;

import com.cyee.internal.R;

public class CyeeDatePicker extends FrameLayout {

    private static final int HEIGHT_PORTAIT = 180;

    private static final int WHEEL_COUNT_PORTRAIT = 5;

    private static final int HEIGHT_LANDSCAPE = 140;

    private static final int WHEEL_COUNT_LANDSCAPE = 3;

    private static final String LOG_TAG = CyeeDatePicker.class.getSimpleName();

    private static final String DATE_FORMAT = "MM/dd/yyyy";

    private static final int DEFAULT_START_YEAR = 1900;

    private static final int DEFAULT_END_YEAR = 2560;
    
    private static final int LUNAR_SUPPORT_MAX_YEAR = 2099;

    private static final boolean DEFAULT_CALENDAR_VIEW_SHOWN = true;

    private static final boolean DEFAULT_SPINNERS_SHOWN = true;

    private static final boolean DEFAULT_ENABLED_STATE = true;

    private final LinearLayout mSpinners;

    private final CyeeNumberPicker mDaySpinner;

    private final CyeeNumberPicker mMonthSpinner;

    private final CyeeNumberPicker mYearSpinner;

    private final EditText mDaySpinnerInput;

    private final EditText mMonthSpinnerInput;

    private final EditText mYearSpinnerInput;

    private final CalendarView mCalendarView;

    private final LinearLayout mLunarLayout;

    private final CyeeSwitch mLunarSwitch;

    private Locale mCurrentLocale;

    private OnDateChangedListener mOnDateChangedListener;

    private String[] mShortMonths;

    private final java.text.DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);

    private int mNumberOfMonths;

    private Calendar mTempDate;

    private Calendar mMinDate;

    private Calendar mMaxDate;

    private Calendar mCurrentDate;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    private final Context mContext;

    private int mYearState = OnScrollListener.SCROLL_STATE_IDLE;

    private int mMonthState = OnScrollListener.SCROLL_STATE_IDLE;

    private int mDayState = OnScrollListener.SCROLL_STATE_IDLE;

    private final OnScrollListener mScrollListener;

    // Gionee <gaoj> <2013-9-27> add for CR00899138 begin
    private int mVerticalPadding;
    private int mHorizontalPadding;
    // Gionee <gaoj> <2013-9-27> add for CR00899138 end

    private boolean isLunarMode = false;

    private LunarModeChangedListener mLunarModeChangedListener;
    private final OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setLunarMode(isChecked);
            if (mLunarModeChangedListener != null) {
                mLunarModeChangedListener.onModeChanged(isChecked);
            }
        }
    };

    private final OnValueChangeListener mOnChangeListener = new OnValueChangeListener() {
        public void onValueChange(CyeeNumberPicker picker, int oldVal, int newVal) {
            updateInputState();
            if (isLunarMode) {
                onValueChangeInLunar(picker, oldVal, newVal);
                updateSpinnerInLunarMode();
            } else {
                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
                if (picker == mDaySpinner) {
                    onDayValueChange(oldVal, newVal);
                } else if (picker == mMonthSpinner) {
                    onMonthValuehange(oldVal, newVal);
                } else if (picker == mYearSpinner) {
                    onYearValueChange(oldVal, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                // now set the date to the adjusted one
                setDate(mTempDate.get(Calendar.YEAR), mTempDate.get(Calendar.MONTH),
                        mTempDate.get(Calendar.DAY_OF_MONTH));
                updateSpinners();
                if (picker == mDaySpinner) {
                    notifyDateChanged(getYear(), getMonth(), mCurrentDate.get(Calendar.DAY_OF_MONTH));
                } else if (picker == mMonthSpinner) {
                    notifyDateChanged(getYear(), mCurrentDate.get(Calendar.MONTH), getDayOfMonth());
                } else if (picker == mYearSpinner) {
                    notifyDateChanged(mCurrentDate.get(Calendar.YEAR), getMonth(), getDayOfMonth());
                } else {
                    throw new IllegalArgumentException();
                }
            }
            updateCalendarView();
        }
    };

    private void onValueChangeInLunar(CyeeNumberPicker picker, int oldVal, int newVal) {
        if (picker == mDaySpinner) {
            // nothing todo;
            updateDayBorder();
            updateCalendarWithSpinnerValue(mYearSpinner.getValue(), mMonthSpinner.getValue(), newVal);
        } else if (picker == mMonthSpinner) {
            // update dayspinner if need
            updateMonthBorder(newVal);
            updateDaySpinnerInLunar(oldVal, newVal);
            updateCalendarWithSpinnerValue(mYearSpinner.getValue(), newVal, mDaySpinner.getValue());
        } else if (picker == mYearSpinner) {
            // update day spinner and month spinner if need;
            updateYearBorder(newVal);
            updateMonthSpinnerInLunar(oldVal, newVal);
            updateCalendarWithSpinnerValue(newVal, mMonthSpinner.getValue(), mDaySpinner.getValue());
        } else {
            throw new IllegalArgumentException();
        }
        notifyDateChanged(getYear(), getMonth(), getDayOfMonth());
    }

    private void updateYearBorder(int yearValue) {
        if (isLunarMode) {
            if (yearValue > mMaxLunarDate.mLunarY) {
                yearValue = mMaxLunarDate.mLunarY;
            } else if (yearValue < mMinLunarDate.mLunarY) {
                yearValue = mMinLunarDate.mLunarY;
            }
        } else {

        }
        mYearSpinner.setValue(yearValue);
    }

    private void updateMonthBorder(int month) {
        int yearValue = mYearSpinner.getValue();
        int monthValue = month;
        if (isLunarMode) {
            if (yearValue == mMaxLunarDate.mLunarY) {
                if (monthValue > mMaxLunarDate.mLunarM) {
                    monthValue = mMaxLunarDate.mLunarM;
                }
            } else if (yearValue == mMinLunarDate.mLunarY) {
                if (monthValue < mMinLunarDate.mLunarM) {
                    monthValue = mMinLunarDate.mLunarM;
                }
            }
        } else {

        }
        mMonthSpinner.setValue(monthValue);
    }

    private void updateDayBorder() {
        int yearValue = mYearSpinner.getValue();
        int monthValue = mMonthSpinner.getValue();
        int dayValue = mDaySpinner.getValue();

        if (isLunarMode) {
            if (yearValue == mMaxLunarDate.mLunarY) {
                if (monthValue == mMaxLunarDate.mLunarM) {
                    if (dayValue > mMaxLunarDate.mLunarD) {
                        dayValue = mMaxLunarDate.mLunarD;
                    }
                }
            } else if (yearValue == mMinLunarDate.mLunarY) {
                if (monthValue == mMinLunarDate.mLunarM) {
                    if (dayValue < mMinLunarDate.mLunarD) {
                        dayValue = mMinLunarDate.mLunarD;
                    }
                }
            }
        } else {

        }
        mDaySpinner.setValue(dayValue);
    }
    
    private void updateMonthSpinnerInLunar(int oldLunarYear, int newVal) {
        int yearValue = newVal;
        int monthValue = mMonthSpinner.getValue();

        int lunarYear = yearValue;
        int leapMonth = Lunar.leapMonth(lunarYear);
        int oldLeapMonth = Lunar.leapMonth(oldLunarYear);

        if (oldLeapMonth == leapMonth) {
            // nothing todo

        } else {

            // update Month Spinner
            mMonthSpinner.setDisplayedValues(null);
            mMonthSpinner.setMinValue(1);
            mMonthSpinner.setMaxValue(leapMonth > 0 ? 13 : 12);
            mMonthSpinner.setValue(monthValue);

            List<String> list = Lunar.getMonthsNameInYear(lunarYear);
            String[] displayedValues = new String[list.size()];
            displayedValues = list.toArray(displayedValues);
            mMonthSpinner.setDisplayedValues(displayedValues);
            mMonthSpinner.invalidate();
        }

        updateDaySpinnerInLunar(monthValue, monthValue);

    }

    private void updateDaySpinnerInLunar(int oldVal, int newVal) {
        int yearValue = mYearSpinner.getValue();
        int monthValue = newVal;
        int dayValue = mDaySpinner.getValue();

        int lunarYear = yearValue;
        int lunarMonth = monthValue;

        int leapMonth = Lunar.leapMonth(lunarYear);
        boolean isLeapMonth = false;
        if (leapMonth > 0 && lunarMonth == leapMonth + 1) {
            isLeapMonth = true;
        }

        int daysInMonth = 0;

        if (isLeapMonth) {
            // 闰月
            daysInMonth = Lunar.daysInMonth(lunarYear, leapMonth, true);
        } else {

            if (lunarMonth <= leapMonth) {
                // 小于闰月的月份
                daysInMonth = Lunar.daysInMonth(lunarYear, lunarMonth, false);

            } else {

                // 大于闰月的月份

                if (leapMonth > 0) {

                    lunarMonth = monthValue - 1;
                } else {
                    lunarMonth = monthValue;
                }
                daysInMonth = Lunar.daysInMonth(lunarYear, lunarMonth, false);

            }

        }

        int lunarDay = dayValue;
        if (dayValue > daysInMonth) {
            lunarDay = daysInMonth;
        }

        mDaySpinner.setDisplayedValues(null);
        mDaySpinner.setMaxValue(daysInMonth);
        mDaySpinner.setMinValue(1);
        mDaySpinner.setValue(lunarDay);
        String[] displayedValues = Lunar.getDaysDisplayName(daysInMonth);
        mDaySpinner.setDisplayedValues(displayedValues);

        mDaySpinner.invalidate();

    }

    // 从年、月、日三个spinner上获取到数值,将数据更新到mCurrentDate中
    private void updateCalendarWithSpinnerValue(int year, int month, int day) {

        int yearValue = year;
        int monthValue = month;
        int dayValue = day;

        int lunarYear = yearValue;
        int lunarMonth = monthValue;
        int leapMonth = Lunar.leapMonth(lunarYear);// 闰月,例如闰九月
        boolean isLeapMonth = false;

        if (leapMonth > 0 && lunarMonth == leapMonth + 1) {
            isLeapMonth = true;
        }

        int lunarDay = dayValue;

        int[] solor = null;
        if (isLeapMonth) {
            // 闰月
            solor = Lunar.lunarToSolar(lunarYear, leapMonth, lunarDay, true);
        } else {

            if (leapMonth > 0 && lunarMonth > leapMonth) {
                // 闰月之后的月份
                solor = Lunar.lunarToSolar(lunarYear, lunarMonth - 1, lunarDay, false);
            } else {
                // 闰月之前的月份
                solor = Lunar.lunarToSolar(lunarYear, lunarMonth, lunarDay, false);
            }
        }

        int solorYear = solor[0];
        int solorMonth = solor[1] - 1;
        int solorDay = solor[2];
        mCurrentDate.set(Calendar.YEAR, solorYear);
        mCurrentDate.set(Calendar.MONTH, solorMonth);
        mCurrentDate.set(Calendar.DAY_OF_MONTH, solorDay);

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
        void onDateChanged(CyeeDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * 
     * 显示模式切换，农历、公立
     */
    public interface LunarModeChangedListener {
        void onModeChanged(boolean isLunar);
    }

    public CyeeDatePicker(Context context) {
        this(context, null);
    }

    public CyeeDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, com.cyee.internal.R.attr.cyeedatePickerStyle);
    }

    public CyeeDatePicker(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        mContext = context;

        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.CyeeDatePicker,
                defStyle, 0);
        boolean spinnersShown = attributesArray.getBoolean(R.styleable.CyeeDatePicker_cyeespinnersShown,
                DEFAULT_SPINNERS_SHOWN);
        boolean calendarViewShown = attributesArray.getBoolean(
                R.styleable.CyeeDatePicker_cyeecalendarViewShown, DEFAULT_CALENDAR_VIEW_SHOWN);
        int startYear = attributesArray
                .getInt(R.styleable.CyeeDatePicker_cyeestartYear, DEFAULT_START_YEAR);
        int endYear = attributesArray.getInt(R.styleable.CyeeDatePicker_cyeeendYear, DEFAULT_END_YEAR);
        String minDate = attributesArray.getString(R.styleable.CyeeDatePicker_cyeeminDate);
        String maxDate = attributesArray.getString(R.styleable.CyeeDatePicker_cyeemaxDate);
        int layoutResourceId = attributesArray.getResourceId(R.styleable.CyeeDatePicker_cyeeinternalLayout,
                com.cyee.internal.R.layout.cyee_date_picker);
        attributesArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResourceId, this, true);

        mSpinners = (LinearLayout) findViewById(com.cyee.internal.R.id.cyee_pickers);

        mCalendarView = (CalendarView) findViewById(com.cyee.internal.R.id.cyee_calendar_view);

        mDaySpinner = (CyeeNumberPicker) findViewById(com.cyee.internal.R.id.cyee_day);
        mDaySpinnerInput = (EditText) mDaySpinner.findViewById(com.cyee.internal.R.id.cyee_numberpicker_input);

        mMonthSpinner = (CyeeNumberPicker) findViewById(com.cyee.internal.R.id.cyee_month);
        mMonthSpinnerInput = (EditText) mMonthSpinner.findViewById(com.cyee.internal.R.id.cyee_numberpicker_input);

        mYearSpinner = (CyeeNumberPicker) findViewById(com.cyee.internal.R.id.cyee_year);
        mYearSpinnerInput = (EditText) mYearSpinner.findViewById(com.cyee.internal.R.id.cyee_numberpicker_input);

        mLunarLayout = (LinearLayout) findViewById(com.cyee.internal.R.id.cyee_lunar_mode_rl);
        mLunarLayout.setVisibility(View.GONE);

        mLunarSwitch = (CyeeSwitch) findViewById(com.cyee.internal.R.id.cyee_lunar_mode_cb);
        mLunarSwitch.setBackground(null);
        mLunarSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mLunarLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (isBusy()) {
                    return;
                }
                mLunarSwitch.setChecked(!mLunarSwitch.isChecked());
            }
        });

        mScrollListener = new OnScrollListener() {
            @Override
            public void onScrollStateChange(CyeeNumberPicker picker, int scrollState) {
                // TODO Auto-generated method stub
                if (picker == mYearSpinner) {
                    mYearState = scrollState;
                }else if (picker == mMonthSpinner) {
                    mMonthState = scrollState;
                }else if (picker == mDaySpinner) {
                    mDayState = scrollState;
                }else {
                    return;
                }
            }
        };

        mYearSpinner.setOnScrollListener(mScrollListener);
        mMonthSpinner.setOnScrollListener(mScrollListener);
        mDaySpinner.setOnScrollListener(mScrollListener);
        setCurrentLocale(Locale.getDefault());
        initCurrentDate();
        initMinDate(minDate, startYear);
        initMaxDate(maxDate, endYear);

        initCalendarView();
        initDaySpinner();
        initMonthSpinner();
        initYearSpinner();
        reorderSpinners();
        updateSpinners();

        setSpinnersVisibility(spinnersShown, calendarViewShown);
        //adjustPadding();
    }

    private boolean isBusy() {
        return mYearState != OnScrollListener.SCROLL_STATE_IDLE
                || mMonthState != OnScrollListener.SCROLL_STATE_IDLE
                || mDayState != OnScrollListener.SCROLL_STATE_IDLE;
    }

    /**
     * 
     * show only what the user required but make sure we show something and the spinners have higher priority
     */
    private void setSpinnersVisibility(boolean spinnersShown, boolean calendarViewShown) {
        if (!spinnersShown && !calendarViewShown) {
            setSpinnersShown(true);
        } else {
            setSpinnersShown(spinnersShown);
            setCalendarViewShown(calendarViewShown);
        }

    }

    private void initCurrentDate() {
        mCurrentDate.setTimeInMillis(System.currentTimeMillis());
//        updateSpinners();
        updateCalendarView();
        init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DAY_OF_MONTH), null);
    }

    private void adjustPadding() {
        // Gionee <gaoj> <2013-9-27> add for CR00899138 begin
        mVerticalPadding = (int) getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_datepicker_vertical);
        mHorizontalPadding = (int) getResources().getDimension(
                com.cyee.internal.R.dimen.cyee_datepicker_horizontal);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mSpinners.setPadding(mHorizontalPadding, mVerticalPadding, mHorizontalPadding, mVerticalPadding);
        } else {
            mSpinners.setPadding(mVerticalPadding, mVerticalPadding, mVerticalPadding, mVerticalPadding);
        }
        // Gionee <gaoj> <2013-9-27> add for CR00899138 end
    }

    private void onYearValueChange(int oldVal, int newVal) {
        mTempDate.set(Calendar.YEAR, newVal);
    }

    private void onMonthValuehange(int oldVal, int newVal) {
        if (oldVal == 11 && newVal == 0) {
            mTempDate.add(Calendar.MONTH, 1);
        } else if (oldVal == 0 && newVal == 11) {
            mTempDate.add(Calendar.MONTH, -1);
        } else {
            mTempDate.add(Calendar.MONTH, newVal - oldVal);
        }
    }

    private void onDayValueChange(int oldVal, int newVal) {
        int maxDayOfMonth = mTempDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (oldVal == maxDayOfMonth && newVal == 1) {
            mTempDate.add(Calendar.DAY_OF_MONTH, 1);
        } else if (oldVal == 1 && newVal == maxDayOfMonth) {
            mTempDate.add(Calendar.DAY_OF_MONTH, -1);
        } else {
            mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
        }
    }

    private void initCalendarView() {
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView view, int year, int month, int monthDay) {
                setDate(year, month, monthDay);
                updateSpinners();
                notifyDateChanged(year, month, monthDay);
            }
        });
    }

    private void initYearSpinner() {
        mYearSpinner.setOnLongPressUpdateInterval(100);
        mYearSpinner.setOnValueChangedListener(mOnChangeListener);
        mYearSpinner.setSelectionSrc(getResources().getDrawable(
                com.cyee.internal.R.drawable.cyee_numberpicker_selection_left));
        String yearUnit = getResources().getString(
                com.cyee.internal.R.string.unit_year);
        if (shouldShowUnit()) {
            mYearSpinner.setUnit(yearUnit, 4);
        }
    }

    private void initMonthSpinner() {
        mMonthSpinner.setOnLongPressUpdateInterval(200);
        mMonthSpinner.setOnValueChangedListener(mOnChangeListener);
        mMonthSpinner
                .setSelectionSrc(getResources().getDrawable(
                        com.cyee.internal.R.drawable.cyee_numberpicker_selection_center));
        String monthUnit = getResources().getString(
                com.cyee.internal.R.string.unit_month);
        if (shouldShowUnit()) {
            if (!isLunarMode) {
                mMonthSpinner.setUnit(monthUnit, 2);
            } else {
                mMonthSpinner.setUnit("", 2);
            }
        }
    }

    private void initDaySpinner() {
        mDaySpinner.setFormatter(CyeeNumberPicker.TWO_DIGIT_FORMATTER);
        mDaySpinner.setOnLongPressUpdateInterval(100);
        mDaySpinner.setOnValueChangedListener(mOnChangeListener);
        mDaySpinner.setSelectionSrc(getResources().getDrawable(
                com.cyee.internal.R.drawable.cyee_numberpicker_selection_right));
        String dayUnit = getResources().getString(
                com.cyee.internal.R.string.unit_day);
        if (shouldShowUnit()) {
            if (!isLunarMode) {
                mDaySpinner.setUnit(dayUnit, 2);
            } else {
                mDaySpinner.setUnit("", 2);
            }
        }
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
     * Gets the minimal date supported by this {@link CyeeDatePicker} in milliseconds since January 1, 1970
     * 00:00:00 in {@link TimeZone#getDefault()} time zone.
     * <p>
     * Note: The default minimal date is 01/01/1900.
     * <p>
     *
     * @return The minimal supported date.
     */
    public long getMinDate() {
        return mCalendarView.getMinDate();
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
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate
                        .get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        Calendar min = null;
        min = getCalendarForLocale(min, Locale.getDefault());
        min.set(DEFAULT_START_YEAR, 0, 31);
        truncateCalendars(min);

        if (minDate < min.getTimeInMillis()) {
            minDate = min.getTimeInMillis();
        }
        mMinDate.setTimeInMillis(minDate);
        mCalendarView.setMinDate(minDate);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
            updateCalendarView();
        }
        mMinLunarDate = getLunarDate(mMinDate.getTimeInMillis());
        updateSpinners();
    }

    /**
     * Gets the maximal date supported by this {@link CyeeDatePicker} in milliseconds since January 1, 1970
     * 00:00:00 in {@link TimeZone#getDefault()} time zone.
     * <p>
     * Note: The default maximal date is 12/31/2100.
     * <p>
     *
     * @return The maximal supported date.
     */
    public long getMaxDate() {
        return mCalendarView.getMaxDate();
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
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMaxDate
                        .get(Calendar.DAY_OF_YEAR)) {
            return;
        }

        Calendar max = null;
        max = getCalendarForLocale(max, Locale.getDefault());
        max.set(DEFAULT_END_YEAR, 11, 31);
        truncateCalendars(max);

        if (maxDate > max.getTimeInMillis()) {
            maxDate = max.getTimeInMillis();
        }

        mMaxDate.setTimeInMillis(maxDate);
        mCalendarView.setMaxDate(maxDate);
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
            updateCalendarView();
        }
        mMaxLunarDate = getLunarDate(mMaxDate.getTimeInMillis());
        if (mMaxDate.after(getLunarSupportMaxDay())) {
            mMaxLunarDate = new LunarDate();
            mMaxLunarDate.mLunarY = LUNAR_SUPPORT_MAX_YEAR;
            mMaxLunarDate.mLunarM = 13;
            mMaxLunarDate.mLunarD = 30;
            mMaxLunarDate.isLeap = 1;
        }else {
            mMaxLunarDate = getLunarDate(mMaxDate.getTimeInMillis());
        }
        updateSpinners();
    }

    public Calendar getLunarSupportMaxDay() {
        Calendar max = null;
        max = getCalendarForLocale(max, Locale.getDefault());
        max.clear();
        int[] solar = Lunar.lunarToSolar(LUNAR_SUPPORT_MAX_YEAR, 12, 30, false);
        max.set(solar[0], solar[1] - 1, solar[2]);
        return max;
    }

    private Calendar getCalendar(LunarDate lunarDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        int[] solar = null;
        final int leapMonth = Lunar.leapMonth(lunarDate.mLunarY);
        boolean isLeapMonth = leapMonth > 0 && (lunarDate.mLunarM == leapMonth + 1);
        calendar = getCalendarForLocale(calendar, Locale.getDefault());
        if (isLeapMonth) {
            solar = Lunar.lunarToSolar(lunarDate.mLunarY, leapMonth, lunarDate.mLunarD, true);
        }else {
            if (leapMonth > 0 && lunarDate.mLunarM > leapMonth) {
                solar = Lunar.lunarToSolar(lunarDate.mLunarY, lunarDate.mLunarM - 1, lunarDate.mLunarD, false);
            }else {
                solar = Lunar.lunarToSolar(lunarDate.mLunarY, lunarDate.mLunarM, lunarDate.mLunarD, false);
            }
        }
        calendar.set(solar[0], solar[1] - 1, solar[2]);
        return calendar;
    }

    static class LunarDate {
        public int mLunarY = 1900;
        public int mLunarM = 0;
        public int mTmpLunarM = 0;
        public int mLunarD = 31;
        public int isLeap = 1;
    }

    private LunarDate mMaxLunarDate;
    private LunarDate mMinLunarDate;

    private LunarDate getLunarDate(long millis) {
        LunarDate lunarDate = new LunarDate();
        int[] curLunarDate = Lunar.solarToLunar(millis);
        if(null == curLunarDate) {
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
    
    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mDaySpinner.setEnabled(enabled);
        mMonthSpinner.setEnabled(enabled);
        mYearSpinner.setEnabled(enabled);
        mCalendarView.setEnabled(enabled);
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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Gionee <gaoj> <2013-9-27> add for CR00899138 begin
        if (mSpinners != null) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mSpinners.setPadding(mHorizontalPadding, mVerticalPadding, mHorizontalPadding,
                        mVerticalPadding);
            } else {
                mSpinners.setPadding(mVerticalPadding, mVerticalPadding, mVerticalPadding, mVerticalPadding);
            }
        }
        // Gionee <gaoj> <2013-9-27> add for CR00899138 end
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Gets whether the {@link CalendarView} is shown.
     *
     * @return True if the calendar view is shown.
     * @see #getCalendarView()
     */
    public boolean getCalendarViewShown() {
        return mCalendarView.isShown();
    }

    /**
     * Gets the {@link CalendarView}.
     *
     * @return The calendar view.
     * @see #getCalendarViewShown()
     */
    public CalendarView getCalendarView() {
        return mCalendarView;
    }

    /**
     * Sets whether the {@link CalendarView} is shown.
     *
     * @param shown
     *            True if the calendar view is to be shown.
     */
    public void setCalendarViewShown(boolean shown) {
        mCalendarView.setVisibility(shown ? VISIBLE : GONE);
    }

    /**
     * Gets whether the spinners are shown.
     *
     * @return True if the spinners are shown.
     */
    public boolean getSpinnersShown() {
        return mSpinners.isShown();
    }

    /**
     * Sets whether the spinners are shown.
     *
     * @param shown
     *            True if the spinners are to be shown.
     */
    public void setSpinnersShown(boolean shown) {
        mSpinners.setVisibility(shown ? VISIBLE : GONE);
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
        truncateCalendars(mTempDate);
        truncateCalendars(mMinDate);
        truncateCalendars(mMaxDate);
        truncateCalendars(mCurrentDate);
    }

    private void truncateCalendars(Calendar calendar) {
        if (calendar != null) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
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
     * Reorders the spinners according to the date format that is explicitly set by the user and if no such is
     * set fall back to the current locale's default format.
     */
    private void reorderSpinners1() {
        mSpinners.removeAllViews();
        char[] order = DateFormat.getDateFormatOrder(getContext());
        final int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case DateFormat.DATE:
                    mSpinners.addView(mDaySpinner);
                    setImeOptions(mDaySpinner, spinnerCount, i);
//                    setAlign(mDaySpinner, i);
                    break;
                case DateFormat.MONTH:
                    mSpinners.addView(mMonthSpinner);
                    setImeOptions(mMonthSpinner, spinnerCount, i);
//                    setAlign(mMonthSpinner, i);
                    break;
                case DateFormat.YEAR:
                    mSpinners.addView(mYearSpinner);
                    setImeOptions(mYearSpinner, spinnerCount, i);
//                    setAlign(mYearSpinner, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    /**
     * re-order the number spinners to match the current date format
     */
    private void reorderSpinners() {
        mSpinners.removeAllViews();
        char[] order = DateFormat.getDateFormatOrder(getContext());
        final int spinnerCount = order.length;
        if (spinnerCount == 0) {
            return;
        }
        if (order[0] == DateFormat.YEAR) {
            // Chinese:Y M D
            mSpinners.addView(mYearSpinner);
            mSpinners.addView(mMonthSpinner);
            mSpinners.addView(mDaySpinner);
        } else {
            if (order[0] == DateFormat.MONTH) {
                // M D Y
                mSpinners.addView(mMonthSpinner);
                mSpinners.addView(mDaySpinner);
            } else {
                // D M Y
                mSpinners.addView(mDaySpinner);
                mSpinners.addView(mMonthSpinner);
            }
            mSpinners.addView(mYearSpinner);
        }

        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case DateFormat.DATE:
                    setImeOptions(mDaySpinner, spinnerCount, i);
                    break;
                case DateFormat.MONTH:
                    setImeOptions(mMonthSpinner, spinnerCount, i);
                    break;
                case DateFormat.YEAR:
                    setImeOptions(mYearSpinner, spinnerCount, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    // gionee maxw add begin
    private void setAlign(CyeeNumberPicker picker, int i) {
        if (i == 0) {
            picker.setAlign(Align.RIGHT);
        }

        if (i == 2) {
            picker.setAlign(Align.LEFT);
        }
    }

    // gionee maxw add end

    /**
     * Updates the current date.
     *
     * @param year
     *            The year.
     * @param month
     *            The month which is <strong>starting from zero</strong>.
     * @param dayOfMonth
     *            The day of the month.
     */
    public void updateDate(int year, int month, int dayOfMonth) {
        if (!isNewDate(year, month, dayOfMonth)) {
            return;
        }
        setDate(year, month, dayOfMonth);
        updateSpinners();
        updateCalendarView();
        notifyDateChanged(year, month, dayOfMonth);
    }

    // Override so we are in complete control of save / restore for this widget.
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getYear(), getMonth(), getDayOfMonth());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateSpinners();
        updateCalendarView();
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
        updateSpinners();
        updateCalendarView();
        mOnDateChangedListener = onDateChangedListener;
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        mOnDateChangedListener = onDateChangedListener;
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
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        return (mCurrentDate.get(Calendar.YEAR) != year || mCurrentDate.get(Calendar.MONTH) != dayOfMonth || mCurrentDate
                .get(Calendar.DAY_OF_MONTH) != month);
    }

    private void setDate(int year, int month, int dayOfMonth) {
        mCurrentDate.set(year, month, dayOfMonth);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
    }

    private void updateSpinners() {
        if (isLunarMode) {
            updateSpinnerInLunarMode();
        } else {

            updateYearSpinner();

            updateMonthSpinner();

            updateDaySpinner();

        }
        mYearSpinner.setOnValueChangedListener(mOnChangeListener);
        mMonthSpinner.setOnValueChangedListener(mOnChangeListener);
        mDaySpinner.setOnValueChangedListener(mOnChangeListener);

    }

    public Calendar getCurrentDate() {
        return mCurrentDate;
    }

    private void updateSpinnerInLunarMode() {
        Calendar max = getCalendar(mMaxLunarDate);
        if (mCurrentDate.after(max)) {
            mCurrentDate.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), max.get(Calendar.DAY_OF_MONTH));
            notifyDateChanged(mCurrentDate.get(Calendar.YEAR),
                    mCurrentDate.get(Calendar.MONTH),mCurrentDate.get(Calendar.DAY_OF_MONTH));
        }

        Calendar min = getCalendar(mMinLunarDate);
        if (mCurrentDate.before(min)) {
            mCurrentDate.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), min.get(Calendar.DAY_OF_MONTH));
            notifyDateChanged(mCurrentDate.get(Calendar.YEAR),
                    mCurrentDate.get(Calendar.MONTH),mCurrentDate.get(Calendar.DAY_OF_MONTH));
        }
   
        LunarDate lunarDate = getLunarDate(mCurrentDate.getTimeInMillis());
        // year
        int minLuanrYear;
        int maxLunarYear;
        if (mMinLunarDate.mLunarY < Lunar.MIN_YEAR) {
            minLuanrYear = Lunar.MIN_YEAR;
            mMinLunarDate.mLunarY = Lunar.MIN_YEAR;
        }else {
            minLuanrYear = mMinLunarDate.mLunarY;
        }
        
        if (mMaxLunarDate.mLunarY > Lunar.MAX_YEAR) {
            maxLunarYear = Lunar.MAX_YEAR;
            mMaxLunarDate.mLunarY = Lunar.MAX_YEAR;
        }else {
            maxLunarYear = mMaxLunarDate.mLunarY;
        }
        mYearSpinner.setMinValue(minLuanrYear);
        mYearSpinner.setMaxValue(maxLunarYear);
        mYearSpinner.setValue(lunarDate.mLunarY);
        // month
        List<String>  rmList = new ArrayList<String>();
        int leapMonth = Lunar.leapMonth(lunarDate.mLunarY);
        int minLuanrMonth = 1;
        int maxLunarMonth = leapMonth > 0 ? 13 : 12; // 如果有闰月，一年为13个越，否则为12个月
        boolean isCurrentLeapMonth = lunarDate.isLeap == 1;
        List<String> list = Lunar.getMonthsNameInYear(lunarDate.mLunarY);
        if (lunarDate.mLunarY == minLuanrYear) {
            final int index = mMinLunarDate.mLunarM - 1;
            minLuanrMonth = index + 1;
            for (int i = 0; i < index;i++) {
                rmList.add(list.get(i));
            }
        }
        if (lunarDate.mLunarY == maxLunarYear) {
            final int index = mMaxLunarDate.mLunarM - 1;
            maxLunarMonth = index + 1;
            int totalSize = list.size();
            for (int i = index + 1; i < totalSize; i++) {
                rmList.add(list.get(i));
            }
        }
        list.removeAll(rmList);
        mMonthSpinner.setDisplayedValues(null);
        mMonthSpinner.setMinValue(minLuanrMonth);
        mMonthSpinner.setMaxValue(maxLunarMonth);
        // 闰月，value加1，例如，普通9月值为9，闰9月值为10
        mMonthSpinner.setValue(lunarDate.mLunarM);
        String[] displayedValues = new String[list.size()];
        displayedValues = list.toArray(displayedValues);
        mMonthSpinner.setDisplayedValues(displayedValues);
        mMonthSpinner.invalidate();

        // day
        int minLunarDay = 1;
        int daysInMonth = Lunar.daysInMonth(lunarDate.mLunarY, lunarDate.mTmpLunarM, isCurrentLeapMonth);
        int maxLunarDay = daysInMonth;
        if (lunarDate.mLunarY == mMinLunarDate.mLunarY && lunarDate.mLunarM == mMinLunarDate.mLunarM
                && lunarDate.isLeap == mMinLunarDate.isLeap) {
            minLunarDay = mMinLunarDate.mLunarD;
        }
        if (lunarDate.mLunarY == mMaxLunarDate.mLunarY && lunarDate.mLunarM == mMaxLunarDate.mLunarM
                && lunarDate.isLeap == mMaxLunarDate.isLeap) {
            maxLunarDay = mMaxLunarDate.mLunarD;
        }
        mDaySpinner.setWrapSelectorWheel(false);
        mDaySpinner.setDisplayedValues(null);
        mDaySpinner.setMinValue(minLunarDay);
        mDaySpinner.setMaxValue(maxLunarDay);
        mDaySpinner.setValue(lunarDate.mLunarD);
        String[] dayDisplayedValues = Lunar.getDaysDisplayName(minLunarDay - 1, maxLunarDay -1 );
        mDaySpinner.setDisplayedValues(dayDisplayedValues);
        mDaySpinner.invalidate();
    }

    private void updateYearSpinner() {
        mYearSpinner.setMinValue(mMinDate.get(Calendar.YEAR));
        mYearSpinner.setMaxValue(mMaxDate.get(Calendar.YEAR));
        mYearSpinner.setWrapSelectorWheel(true);
        mYearSpinner.setValue(mCurrentDate.get(Calendar.YEAR));
        mYearSpinner.invalidate();
    }

    private void updateMonthSpinner() {
        int minValue = mCurrentDate.getActualMinimum(Calendar.MONTH);
        int maxValue = mCurrentDate.getActualMaximum(Calendar.MONTH);
        boolean isWrapWheel = true;
        if (mCurrentDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)) {
            minValue = mMinDate.get(Calendar.MONTH);
        }
        if (mCurrentDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR)) {
            maxValue = mMaxDate.get(Calendar.MONTH);
        }
        mMonthSpinner.setDisplayedValues(null);
        mMonthSpinner.setMinValue(minValue);
        mMonthSpinner.setMaxValue(maxValue);
        mMonthSpinner.setWrapSelectorWheel(isWrapWheel);
        mMonthSpinner.setValue(mCurrentDate.get(Calendar.MONTH));
        updateMonthDisplayValue(minValue, maxValue);
    }

    private void updateDaySpinner() {

        int minValue = mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxValue = mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        boolean isWrapWheel = true;

        if (mCurrentDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR) &&
                mCurrentDate.get(Calendar.MONTH) == mMinDate.get(Calendar.MONTH)) {
            minValue = mMinDate.get(Calendar.DAY_OF_MONTH);
        } 
        if (mCurrentDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR) &&
                mCurrentDate.get(Calendar.MONTH) == mMaxDate.get(Calendar.MONTH)) {
            maxValue = mMaxDate.get(Calendar.DAY_OF_MONTH);
        }
        mDaySpinner.setDisplayedValues(null);
        mDaySpinner.setMinValue(minValue);
        mDaySpinner.setMaxValue(maxValue);
        mDaySpinner.setWrapSelectorWheel(isWrapWheel);
        mDaySpinner.setValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
        mDaySpinner.invalidate();
    }

    /**
     * Updates the calendar view with the current date.
     */
    private void updateCalendarView() {
        mCalendarView.setDate(mCurrentDate.getTimeInMillis(), false, false);
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

    public long getCurrentTimeMillis() {
        return mCurrentDate.getTimeInMillis();
    }

    /**
     * Notifies the listener, if such, for a change in the selected date.
     */
    private void notifyDateChanged(int year, int month, int day) {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, year, month, day);
        }
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
        TextView input = (TextView) spinner.findViewById(com.cyee.internal.R.id.cyee_numberpicker_input);
        input.setImeOptions(imeOptions);
    }

    private void setContentDescriptions() {
        // Day
        /*trySetContentDescription(mDaySpinner, R.id.cyee_increment,
                com.cyee.internal.R.string(this,"date_picker_increment_day_button"));
        trySetContentDescription(mDaySpinner, R.id.cyee_decrement,
                com.cyee.internal.R.string(this,"date_picker_decrement_day_button"));
        // Month
        trySetContentDescription(mMonthSpinner, R.id.cyee_increment,
                com.cyee.internal.R.string(this,"date_picker_increment_month_button"));
        trySetContentDescription(mMonthSpinner, R.id.cyee_decrement,
                com.cyee.internal.R.string(this,"date_picker_decrement_month_button"));
        // Year
        trySetContentDescription(mYearSpinner, R.id.cyee_increment,
                com.cyee.internal.R.string(this,"date_picker_increment_year_button"));
        trySetContentDescription(mYearSpinner, R.id.cyee_decrement,
                com.cyee.internal.R.string(this,"date_picker_decrement_year_button"));*/
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(mContext.getString(contDescResId));
        }
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
            if (inputMethodManager.isActive(mYearSpinnerInput)) {
                mYearSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMonthSpinnerInput)) {
                mMonthSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mDaySpinnerInput)) {
                mDaySpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    /**
     * Class for managing state storing/restoring.
     */
    private static class SavedState extends BaseSavedState {

        private final int mYear;

        private final int mMonth;

        private final int mDay;

        /**
         * Constructor called from {@link CyeeDatePicker#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, int year, int month, int day) {
            super(superState);
            mYear = year;
            mMonth = month;
            mDay = day;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mMonth = in.readInt();
            mDay = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mYear);
            dest.writeInt(mMonth);
            dest.writeInt(mDay);
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

    private boolean isChineseLanguage() {
        return getResources().getConfiguration().locale.getCountry().equals("CN")
                || getResources().getConfiguration().locale.getCountry().equals("TW");
    }

    //需要显示单位的语言列表
    private static final String[] sUnitLanguage = { "CN", "TW", "KP" };

    private boolean shouldShowUnit() {
        boolean show = false;
        String curLanguage = getResources().getConfiguration().locale
                .getCountry();
        for (String l : sUnitLanguage) {
            if (l.equals(curLanguage)) {
                show = true;
                break;
            }
        }

        return show;
    }
    
    private void updateMonthDisplayValue(int minValue, int maxValue) {
        mNumberOfMonths = maxValue - minValue + 1;
        mShortMonths = new String[mNumberOfMonths];
        int abbrev = isChineseLanguage() ? DateUtils.LENGTH_SHORTEST : DateUtils.LENGTH_MEDIUM;
        for (int i = minValue; i <= maxValue; i++) {
            mShortMonths[i - minValue] = DateUtils.getMonthString(Calendar.JANUARY + i, abbrev);
        }
        String[] displayedValues = Arrays.copyOfRange(mShortMonths, 0, mNumberOfMonths);
        mMonthSpinner.setDisplayedValues(displayedValues);
        mMonthSpinner.invalidate();
    }

    private void setLunarMode(boolean isLunarMode) {
        this.isLunarMode = isLunarMode;
        initDaySpinner();
        initMonthSpinner();
        initYearSpinner();
        updateSpinners();
        invalidate();
    }

    boolean isLunarSwitchShown() {
        return mLunarLayout.getVisibility() != View.GONE;
    }
    
    public boolean isLunarMode() {
        return isLunarMode;
    }

    public void showLunarModeSwitch() {
        mLunarLayout.setVisibility(View.VISIBLE);
        adjustWheelCount();
        
    }

    private void adjustWheelCount() {
        if (mLunarLayout.getVisibility() == View.VISIBLE && mContext.getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mYearSpinner.setDisplayedWheelCount(WHEEL_COUNT_LANDSCAPE);
            mMonthSpinner.setDisplayedWheelCount(WHEEL_COUNT_LANDSCAPE);
            mDaySpinner.setDisplayedWheelCount(WHEEL_COUNT_LANDSCAPE);
            mYearSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_LANDSCAPE);
            mMonthSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_LANDSCAPE);
            mDaySpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_LANDSCAPE);
        } else {
            mYearSpinner.setDisplayedWheelCount(WHEEL_COUNT_PORTRAIT);
            mMonthSpinner.setDisplayedWheelCount(WHEEL_COUNT_PORTRAIT);
            mDaySpinner.setDisplayedWheelCount(WHEEL_COUNT_PORTRAIT);
            mYearSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_PORTAIT);
            mMonthSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_PORTAIT);
            mDaySpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_PORTAIT);
        }
        invalidate();
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

    public void setSpinnersWidth(int yearSpinnerWidth, int monthSpinnerWidth, int daySpinnerWidth) {
        mYearSpinner.getLayoutParams().width = yearSpinnerWidth;
        mMonthSpinner.getLayoutParams().width = monthSpinnerWidth;
        mDaySpinner.getLayoutParams().width = daySpinnerWidth;
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
