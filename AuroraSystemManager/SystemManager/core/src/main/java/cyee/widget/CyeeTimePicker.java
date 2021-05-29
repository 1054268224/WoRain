package cyee.widget;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;

import com.cyee.internal.R;
import cyee.widget.CyeeNumberPicker.Formatter;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
// Gionee <gaoj> <2013-9-27> add for CR00899138 begin
import android.widget.LinearLayout;
// Gionee <gaoj> <2013-9-27> add for CR00899138 end
import android.widget.TextView;
//Chenyee <CY_FrameWork> hushengsong 2018-04-17 modify for CSW1707A-341 begin
import android.text.TextUtils;
//Chenyee <CY_FrameWork> hushengsong 2018-04-17 modify for CSW1707A-341 end
public class CyeeTimePicker extends FrameLayout {

    private static final boolean DEFAULT_ENABLED_STATE = true;

    private static final int HOURS_IN_HALF_DAY = 12;

    private static final int HEIGHT_PORTRAIT = 180;

    private static final int WHEEL_COUNT_PORTRAIT = 5;

    private static final int HEIGHT_LANDSCAPE = 140;

    private static final int WHEEL_COUNT_LANDSCAPE = 3;

    /**
     * A no-op callback used in the constructor to avoid null checks later in
     * the code.
     */
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        @Override
        public void onTimeChanged(CyeeTimePicker view, int hourOfDay, int minute) {
        }
    };

    // state
    private boolean mIs24HourView;

    private boolean mIsAm;

    // ui components
    private final CyeeNumberPicker mHourSpinner;

    private final CyeeNumberPicker mMinuteSpinner;

    private final CyeeNumberPicker mAmPmSpinner;

    private final EditText mHourSpinnerInput;

    private final EditText mMinuteSpinnerInput;

    private final EditText mAmPmSpinnerInput;

    private final TextView mDivider = null;
    
    // Note that the legacy implementation of the TimePicker is
    // using a button for toggling between AM/PM while the new
    // version uses a NumberPicker spinner. Therefore the code
    // accommodates these two cases to be backwards compatible.
    private final Button mAmPmButton;

    private final String[] mAmPmStrings;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;
    private OnTimeChangedListenerInternal mOnTimeChangedListenerInternal;

    private Calendar mTempCalendar;

    private Locale mCurrentLocale;
    
    private final Context mContext;

    // Gionee <gaoj> <2013-9-27> add for CR00899138 begin
    private final LinearLayout mSpinners;
    private final int mVerticalPadding;
    private final int mHorizontalPadding;
    // Gionee <gaoj> <2013-9-27> add for CR00899138 end

    private boolean mShowUnit = false;
    
    
    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    interface OnTimeChangedListenerInternal {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(CyeeTimePicker view, int oldHour, int oldMinute,
                int newHour, int newMinute);

    }
    
    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(CyeeTimePicker view, int hourOfDay, int minute);

    }

    public CyeeTimePicker(Context context) {
        this(context, null);
    }

    public CyeeTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, com.cyee.internal.R.attr.cyeetimePickerStyle);
    }

    public CyeeTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        mContext = context;
        
        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        // process style attributes
        TypedArray attributesArray = context.obtainStyledAttributes(
                attrs, R.styleable.CyeeTimePicker, defStyle, 0);
        int layoutResourceId = attributesArray.getResourceId(
                R.styleable.CyeeTimePicker_cyeeinternalLayout, R.layout.cyee_time_picker);
        attributesArray.recycle();

        final TypedArray inputTypedArray = context.obtainStyledAttributes(attrs, android.R.styleable.TextAppearance);

        final int inputTextSize = inputTypedArray.getDimensionPixelSize(android.R.styleable.TextAppearance_textSize, -1);
        inputTypedArray.recycle();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResourceId, this, true);

        // Gionee <gaoj> <2013-9-27> add for CR00899138 begin
        mSpinners = (LinearLayout) findViewById(com.cyee.internal.R.id.cyee_time_picker);
        
        mVerticalPadding = (int) getResources().getDimension(com.cyee.internal.R.dimen.cyee_datepicker_vertical);
        mHorizontalPadding = (int) getResources().getDimension(com.cyee.internal.R.dimen.cyee_datepicker_horizontal);
        mSpinners.setPadding(mSpinners.getPaddingStart(), mVerticalPadding, mSpinners.getPaddingEnd(), mVerticalPadding);
        // Gionee <gaoj> <2013-9-27> add for CR00899138 end

        // hour
        mHourSpinner = (CyeeNumberPicker) findViewById(com.cyee.internal.R.id.cyee_hour);
        mHourSpinner.setSelectionSrc(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_numberpicker_selection_left));
        mHourSpinner.setOnValueChangedListener(new CyeeNumberPicker.OnValueChangeListener() {
            public void onValueChange(CyeeNumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                if (!is24HourView()) {
                    if ((oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY)
                            || (oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1)) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                }
                onTimeChanged(resolveHour(oldVal), getCurrentMinute(), resolveHour(newVal), getCurrentMinute());
            }
        });
        String hourUnit = getResources().getString(com.cyee.internal.R.string.unit_hour);
        if (mShowUnit) {
            mHourSpinner.setUnit(hourUnit, 2);
        }
        mHourSpinnerInput = (EditText) mHourSpinner.findViewById(com.cyee.internal.R.id.cyee_numberpicker_input);
        mHourSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // divider (only for the new widget style)
        /*mDivider = (TextView) findViewById(R.id.cyee_divider);
        if (mDivider != null) {
            mDivider.setText(com.cyee.internal.R.string(this,"time_picker_separator"));
        }*/

        // minute
        mMinuteSpinner = (CyeeNumberPicker) findViewById(com.cyee.internal.R.id.cyee_minute);
        mMinuteSpinner.setMinValue(0);
        mMinuteSpinner.setMaxValue(59);
        mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setFormatter(CyeeNumberPicker.TWO_DIGIT_FORMATTER);
        mMinuteSpinner.setOnValueChangedListener(new CyeeNumberPicker.OnValueChangeListener() {
            public void onValueChange(CyeeNumberPicker spinner, int oldVal, int newVal) {
                updateInputState();
                int minValue = mMinuteSpinner.getMinValue();
                int maxValue = mMinuteSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = mHourSpinner.getValue() + 1;
                    if (!is24HourView() && newHour == HOURS_IN_HALF_DAY) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourSpinner.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mHourSpinner.getValue() - 1;
                    if (!is24HourView() && newHour == HOURS_IN_HALF_DAY - 1) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourSpinner.setValue(newHour);
                }
                onTimeChanged(getCurrentHour(), oldVal, getCurrentHour(), newVal);
            }
        });
        String minuteUnit = getResources().getString(com.cyee.internal.R.string.unit_minute);
        if (mShowUnit) {
            mMinuteSpinner.setUnit(minuteUnit, 2);
        }
        mMinuteSpinnerInput = (EditText) mMinuteSpinner.findViewById(com.cyee.internal.R.id.cyee_numberpicker_input);
        mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        /* Get the localized am/pm strings and use them in the spinner */
        mAmPmStrings = new DateFormatSymbols().getAmPmStrings();

        // am/pm
        View amPmView = findViewById(com.cyee.internal.R.id.cyee_amPm);
        if (amPmView instanceof Button) {
            mAmPmSpinner = null;
            mAmPmSpinnerInput = null;
            mAmPmButton = (Button) amPmView;
            mAmPmButton.setOnClickListener(new OnClickListener() {
                public void onClick(View button) {
                    button.requestFocus();
                    int oldHour = getCurrentHour();
                    int oldMinute = getCurrentMinute();
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                    onTimeChanged(oldHour, oldMinute, oldHour, oldMinute);
                }
            });
        } else {
            mAmPmButton = null;
            mAmPmSpinner = (CyeeNumberPicker) amPmView;
            mAmPmSpinner.setMinValue(0);
            mAmPmSpinner.setMaxValue(1);
            mAmPmSpinner.setDisplayedValues(mAmPmStrings);
            mAmPmSpinner.setSelectionSrc(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_numberpicker_selection_right));
            mAmPmSpinner.setOnValueChangedListener(new CyeeNumberPicker.OnValueChangeListener() {
                public void onValueChange(CyeeNumberPicker picker, int oldVal, int newVal) {
                    updateInputState();
                    picker.requestFocus();
                    int tmepHourValue = mHourSpinner.getValue();
                    int oldHour = resolveHour(tmepHourValue);
                    int oldMinute = getCurrentMinute();
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                    onTimeChanged(oldHour, oldMinute, resolveHour(tmepHourValue), oldMinute);
                }
            });
            mAmPmSpinnerInput = (EditText) mAmPmSpinner.findViewById(com.cyee.internal.R.id.cyee_numberpicker_input);
            mAmPmSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        if (inputTextSize != -1) {
            mAmPmSpinner.setTextSize(inputTextSize);
            mHourSpinner.setTextSize(inputTextSize);
            mMinuteSpinner.setTextSize(inputTextSize);
        }
        // update controls to initial state
        updateHourControl();
        updateAmPmControl();
        reorderSpinners();
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);

        // set to current time
        setCurrentHour(mTempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(mTempCalendar.get(Calendar.MINUTE));

        if (!isEnabled()) {
            setEnabled(false);
        }

        // set the content descriptions
        setContentDescriptions();

        // If not explicitly specified this view is important for accessibility.
        /*if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }*/
    }

	@Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mMinuteSpinner.setEnabled(enabled);
        if (mDivider != null) {
            mDivider.setEnabled(enabled);
        }
        mHourSpinner.setEnabled(enabled);
        if (mAmPmSpinner != null) {
            mAmPmSpinner.setEnabled(enabled);
        } else {
            mAmPmButton.setEnabled(enabled);
        }
        mIsEnabled = enabled;
    }
	@Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        drawDivider(canvas);
    }

    private void drawDivider(Canvas canvas) {
        // TODO Auto-generated method stub
        Paint paint = new Paint();
        String text = getResources().getString(com.cyee.internal.R.string.cyee_colon);
        paint.setColor(mHourSpinner.getTextColors().getColorForState(ENABLED_STATE_SET, Color.WHITE));
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(mHourSpinner.getTextSize());
        float y = mHourSpinner.getTop() + mHourSpinner.getMeasuredHeight()/2;
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, 1, rect);
        float alignHeight = rect.bottom - rect.top;
        //Chenyee <CY_FrameWork> hushengsong 2018-03-14 modify for SW17W16SE-63 begin
	if(is24HourView()){
	   canvas.drawText(text, getPaddingLeft() + mSpinners.getWidth()/2, y + alignHeight/2,  paint);
	 
	}else{
	   //Chenyee <CY_FrameWork> hushengsong 2018-04-17 modify for CSW1707A-341 begin
	   if (isChineseLanguage()||(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL)){
	     canvas.drawText(text, getPaddingLeft() + mSpinners.getWidth()*2/3, y + alignHeight/2,  paint);
	   }else{
	     canvas.drawText(text, getPaddingLeft() + mSpinners.getWidth()/3, y + alignHeight/2,  paint);
	   }
	   //Chenyee <CY_FrameWork> hushengsong 2018-04-17 modify for CSW1707A-341 end 
	}
        //canvas.drawText(text, getPaddingLeft() + mHourSpinner.getRight(), y + alignHeight/2,  paint);
	//Chenyee <CY_FrameWork> hushengsong 2018-03-14 modify for SW17W16SE-63 end
    }
    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    public void setTextStable(boolean textSteable) {
        mHourSpinner.setTextStable(textSteable);
        mMinuteSpinner.setTextStable(textSteable);
    }
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        mCurrentLocale = locale;
        mTempCalendar = Calendar.getInstance(locale);
    }

    public Locale getCurrentLocale() {
        return mCurrentLocale;
    }
    
    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;

        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            mHour = hour;
            mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }

        @SuppressWarnings({"unused", "hiding"})
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCurrentHour(), getCurrentMinute());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    void setOnTimeChangedListenerInternal(OnTimeChangedListenerInternal onTimeChangedListener) {
        mOnTimeChangedListenerInternal = onTimeChangedListener;
    }
    
    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
        return resolveHour(mHourSpinner.getValue());
    }
    
    private Integer resolveHour(int hour) {
        if (is24HourView()) {
            return hour;
        } else if (mIsAm) {
            return hour % HOURS_IN_HALF_DAY;
        } else {
            return (hour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY;
        }
    }
    
    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        // why was Integer used in the first place?
        if (currentHour == null || currentHour == getCurrentHour()) {
            return;
        }
        int oldHour = getCurrentHour();
        int oldMinute = getCurrentMinute();
        
        if (!is24HourView()) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour >= HOURS_IN_HALF_DAY) {
                mIsAm = false;
                if (currentHour > HOURS_IN_HALF_DAY) {
                    currentHour = currentHour - HOURS_IN_HALF_DAY;
                }
            } else {
                mIsAm = true;
                if (currentHour == 0) {
                    currentHour = HOURS_IN_HALF_DAY;
                }
            }
            updateAmPmControl();
        }
        mHourSpinner.setValue(currentHour);
        onTimeChanged(oldHour, oldMinute, currentHour, oldMinute);
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourView(Boolean is24HourView) {
        if (mIs24HourView == is24HourView) {
            return;
        }
        mIs24HourView = is24HourView;
        // cache the current hour since spinner range changes
        int currentHour = getCurrentHour();
        updateHourControl();
        // set value after spinner range is updated
        setCurrentHour(currentHour);
        updateAmPmControl();
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mMinuteSpinner.getValue();
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute == getCurrentMinute()) {
            return;
        }
        int oldHour = getCurrentHour();
        int oldMinute = getCurrentMinute();
        mMinuteSpinner.setValue(currentMinute);
        onTimeChanged(oldHour, oldMinute, oldHour, currentMinute);
    }

    @Override
    public int getBaseline() {
        return mHourSpinner.getBaseline();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);

        int flags = DateUtils.FORMAT_SHOW_TIME;
        if (mIs24HourView) {
            flags |= DateUtils.FORMAT_24HOUR;
        } else {
            flags |= DateUtils.FORMAT_12HOUR;
        }
        mTempCalendar.set(Calendar.HOUR_OF_DAY, getCurrentHour());
        mTempCalendar.set(Calendar.MINUTE, getCurrentMinute());
        String selectedDateUtterance = DateUtils.formatDateTime(mContext,
                mTempCalendar.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(CyeeTimePicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(CyeeTimePicker.class.getName());
    }

    private void updateHourControl() {
        if (is24HourView()) {
            mHourSpinner.setMinValue(0);
            mHourSpinner.setMaxValue(23);
            mHourSpinner.setFormatter(CyeeNumberPicker.TWO_DIGIT_FORMATTER);
        } else {
            mHourSpinner.setMinValue(1);
            mHourSpinner.setMaxValue(12);
            mHourSpinner.setFormatter(null);
        }
    }

    void adjustWheelCount(boolean isLandScape) {
        if (isLandScape) {
            mHourSpinner.setDisplayedWheelCount(WHEEL_COUNT_LANDSCAPE);
            mMinuteSpinner.setDisplayedWheelCount(WHEEL_COUNT_LANDSCAPE);
            mAmPmSpinner.setDisplayedWheelCount(WHEEL_COUNT_LANDSCAPE);
            mHourSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_LANDSCAPE);
            mMinuteSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_LANDSCAPE);
            mAmPmSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_LANDSCAPE);
        } else {
            mHourSpinner.setDisplayedWheelCount(WHEEL_COUNT_PORTRAIT);
            mMinuteSpinner.setDisplayedWheelCount(WHEEL_COUNT_PORTRAIT);
            mAmPmSpinner.setDisplayedWheelCount(WHEEL_COUNT_PORTRAIT);
            mHourSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_PORTRAIT);
            mMinuteSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_PORTRAIT);
            mAmPmSpinner.getLayoutParams().height = dip2px(mContext, HEIGHT_PORTRAIT);
        }
        invalidate();
    }
    
    private void updateAmPmControl() {
        if (is24HourView()) {
            if (mAmPmSpinner != null) {
                mAmPmSpinner.setVisibility(View.GONE);
            } else {
                mAmPmButton.setVisibility(View.GONE);
            }
            // Gionee zhangxx 2012-11-03 add for CR00724235 begin
            if (mMinuteSpinner != null) {
                //mMinuteSpinner.setBackgroundDrawable(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_numberpicker_right));
                mMinuteSpinner.setSelectionSrc(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_numberpicker_selection_right));            
            }
            // Gionee zhangxx 2012-11-03 add for CR00724235 end
        } else {
            int index = mIsAm ? Calendar.AM : Calendar.PM;
            if (mAmPmSpinner != null) {
                mAmPmSpinner.setValue(index);
                mAmPmSpinner.setVisibility(View.VISIBLE);
            } else {
                mAmPmButton.setText(mAmPmStrings[index]);
                mAmPmButton.setVisibility(View.VISIBLE);
            }
            // Gionee zhangxx 2012-11-03 add for CR00724235 begin
            if (mMinuteSpinner != null) {
                //mMinuteSpinner.setBackgroundDrawable(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_numberpicker_center));
                mMinuteSpinner.setSelectionSrc(getResources().getDrawable(com.cyee.internal.R.drawable.cyee_numberpicker_selection_center));        
            }
            // Gionee zhangxx 2012-11-03 add for CR00724235 end
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    private void onTimeChanged(int oldHour, int oldMinute, int newHour, int newMinute) {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, newHour, newMinute);
        }
        
        if (mOnTimeChangedListenerInternal != null) {
            mOnTimeChangedListenerInternal.onTimeChanged(this, oldHour, oldMinute, newHour, newMinute);
        }
    }
    
    private void setContentDescriptions() {
        // Minute
        /*trySetContentDescription(mMinuteSpinner, R.id.cyee_increment,
                com.cyee.internal.R.string(this,"time_picker_increment_minute_button"));
        trySetContentDescription(mMinuteSpinner, R.id.cyee_decrement,
                com.cyee.internal.R.string(this,"time_picker_decrement_minute_button"));
        // Hour
        trySetContentDescription(mHourSpinner, R.id.cyee_increment,
                com.cyee.internal.R.string(this,"time_picker_increment_hour_button"));
        trySetContentDescription(mHourSpinner, R.id.cyee_decrement,
                com.cyee.internal.R.string(this,"time_picker_decrement_hour_button"));
        // AM/PM
        if (mAmPmSpinner != null) {
            trySetContentDescription(mAmPmSpinner, R.id.cyee_increment,
                    com.cyee.internal.R.string(this,"time_picker_increment_set_pm_button"));
            trySetContentDescription(mAmPmSpinner, R.id.cyee_decrement,
                    com.cyee.internal.R.string(this,"time_picker_decrement_set_am_button"));
        }*/
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
        InputMethodManager inputMethodManager = (InputMethodManager)(mContext.getSystemService(Context.INPUT_METHOD_SERVICE));/*InputMethodManager.peekInstance();*/
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mHourSpinnerInput)) {
                mHourSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteSpinnerInput)) {
                mMinuteSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mAmPmSpinnerInput)) {
                mAmPmSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }
    
    public boolean isAm() {
    	return mIsAm;
    }

    public String getDisplayedAMPMs(){
        return mAmPmSpinner.getDisplayedValue();
    } 
    
    public void setMinuteDelta(int delta) {
		if (delta < 1) {
			return;
		}
		mMinuteSpinner.mDelta(delta);
//		int currentValue = mMinuteSpinner.getValue();
//		mMinuteSpinner.setValue(currentValue - currentValue % delta);
	}

	public void setHourDelta(int delta) {
		if (delta < 1) {
			return;
		}
		mHourSpinner.mDelta(delta);
//		int currentValue = mHourSpinner.getValue();
//		mHourSpinner.setValue(currentValue - currentValue % delta);
	}

	public void setMinMinute(int minValue) {
		if (minValue >= 0 && minValue <= 59) {
			mMinuteSpinner.setMinValue(minValue);
		}
	}

	public void setMaxMinute(int maxValue) {
		if (maxValue >= 0 && maxValue <= 59) {
			mMinuteSpinner.setMaxValue(maxValue);
		}
	}

	public void setMinHour(int minValue) {
		if (is24HourView()) {
			if (minValue >= 0 && minValue <= 23) {
				mHourSpinner.setMinValue(minValue);
				mHourSpinner.setFormatter(CyeeNumberPicker.TWO_DIGIT_FORMATTER);
			}
		} else {
			if (minValue >= 1 && minValue <= 12) {
				mHourSpinner.setMinValue(minValue);
				mHourSpinner.setFormatter(null);
			}
		}
		
	}
	
	public Formatter getFormatter() {
	    return CyeeNumberPicker.TWO_DIGIT_FORMATTER;
	}
	
	public void setMaxHour(int maxValue) {
		if (is24HourView()) {
			if (maxValue >= 0 && maxValue <= 23) {
				mHourSpinner.setMaxValue(maxValue);
				mHourSpinner.setFormatter(CyeeNumberPicker.TWO_DIGIT_FORMATTER);
			}
		} else {
			if (maxValue >= 1 && maxValue <= 12) {
				mHourSpinner.setMaxValue(maxValue);
				mHourSpinner.setFormatter(null);
			}
		}
	}

	public void setSpinnersWidth(int ampmWidth, int hourWidth, int minuteWidth) {
	    mAmPmSpinner.getLayoutParams().width = ampmWidth;
	    mHourSpinner.getLayoutParams().width = hourWidth;
	    mMinuteSpinner.getLayoutParams().width = minuteWidth;
	}

	private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
	
	/**
     * re-order the number spinners to match the current time format
     */
    private void reorderSpinners() {
        if (mCurrentLocale != null && isChineseLanguage()) {
            mSpinners.removeAllViews();
            mSpinners.addView(mAmPmSpinner);
            mSpinners.addView(mHourSpinner);
            mSpinners.addView(mMinuteSpinner);
        }
    }
    
    /**
     * if the unit should be shown
     * @param show
     */
    public void showUnit(boolean show) {
        mShowUnit = show;
    }
    
    private boolean isChineseLanguage() {
        return getResources().getConfiguration().locale.getCountry().equals("CN")
                || getResources().getConfiguration().locale.getCountry().equals("TW");
    }
    
}
