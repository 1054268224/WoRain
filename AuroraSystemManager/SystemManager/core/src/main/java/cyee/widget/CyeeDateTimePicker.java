package cyee.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeDatePicker.LunarModeChangedListener;
import cyee.widget.CyeeDatePicker.OnDateChangedListener;
import cyee.widget.CyeeTimePicker.OnTimeChangedListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.soundtrigger.SoundTrigger.ConfidenceLevel;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import com.cyee.internal.util.Lunar;

public class CyeeDateTimePicker extends LinearLayout implements OnTimeChangedListener, OnDateChangedListener {
    private static final String TAG = "CyeeDateTimePicker";
    private static final int ACTIONBAR_TAB_INDICATOR_BOTTOM_PADDING = 1;
    private final Context mContext;
    private Calendar mCalendar;
    private CyeeTabHost mTabHost;
    private CyeeTabWidget mTabWidget;
    private CyeeDatePicker mDatePicker;
    private CyeeTimePicker mTimePicker;
    private CyeeTextView mDateTabLabel;
    private CyeeTextView mTimeTabLabel;
    private int mAccentColor = 0xff00a3e4;
    private ColorStateList mNomalColor;

    public CyeeDateTimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;
        setOrientation(LinearLayout.VERTICAL);
        setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        initCalendar();

        initTimePicker();
        initDatePicker();

        initTabs();

        updateDatePickerTitle();
        updateTimePickerTitle();

        adjusetHeight();

        changeColor();

    }

    private void initCalendar() {
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(mCalendar.getTimeInMillis() + 10 * 60 * 1000);
    }

    private void initTimePicker() {
        mTimePicker = new CyeeTimePicker(mContext);
        updateTimePicker();
    }

    private void updateTimePicker() {
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        mTimePicker.setPadding(dip2px(mContext, 24), mTimePicker.getPaddingTop(), dip2px(mContext, 24), mTimePicker.getPaddingBottom());
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
        mTimePicker.setOnTimeChangedListener(this);
    }

    private void initDatePicker() {
        mDatePicker = new CyeeDatePicker(mContext);
        updateDatePicker();
        mDatePicker.setLunarModeChangedListener(mLunarModeChangedListener);
    }

    private void updateDatePicker() {
        int year = mCalendar.get(Calendar.YEAR);
        int monthOfYear = mCalendar.get(Calendar.MONTH);
        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
    }

    private void changeColor() {
        if (ChameleonColorManager.isNeedChangeColor(mContext)) {
            mTabHost.setIndicatorBackgroundColor(ChameleonColorManager.getAccentColor_G1());
            mAccentColor = ChameleonColorManager.getAccentColor_G1();
        }
        mDateTabLabel.setTextColor(mAccentColor);
    }

    private void initTabs() {
        int layoutResourceId = com.cyee.internal.R.layout.cyee_datetime_picker;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResourceId, this, true);

        mTabHost = (CyeeTabHost) findViewById(com.cyee.internal.R.id.tabHost);
        mTabHost.setup();

        mTabWidget = (CyeeTabWidget) mTabHost.getTabWidget();
        mTabWidget.setBackgroundColor(0xffffff);

        LinearLayout.LayoutParams tabLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        tabLayoutParams.weight = 1;
        tabLayoutParams.gravity = Gravity.CENTER;
        TabSpec dateTabSpec = mTabHost.newTabSpec("Tab1");
        mDateTabLabel = new CyeeTextView(mContext);
        mDateTabLabel.setEllipsize(TruncateAt.END);
        mDateTabLabel.setLines(1);
        mDateTabLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(
                        com.cyee.internal.R.dimen.cyee_actionbar_tabtext_text_size));
        mDateTabLabel.setGravity(Gravity.CENTER);
        mDateTabLabel.setLayoutParams(tabLayoutParams);
        dateTabSpec.setIndicator(mDateTabLabel);
        dateTabSpec.setContent(new TabContentFactory() {

            @Override
            public View createTabContent(String tag) {
                return mDatePicker;
            }
        });

        TabSpec timeTabSpec = mTabHost.newTabSpec("Tab2");

        mTimeTabLabel = new CyeeTextView(mContext);
        mTimeTabLabel.setLayoutParams(tabLayoutParams);
        mTimeTabLabel.setEllipsize(TruncateAt.END);
        mTimeTabLabel.setLines(1);
        mTimeTabLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(
                        com.cyee.internal.R.dimen.cyee_actionbar_tabtext_text_size));
        mTimeTabLabel.setGravity(Gravity.CENTER);
        timeTabSpec.setIndicator(mTimeTabLabel);
        timeTabSpec.setContent(new TabContentFactory() {

            @Override
            public View createTabContent(String tag) {
                return mTimePicker;
            }
        });
        mNomalColor = mTimeTabLabel.getTextColors();

        mTabHost.setup();
        mTabHost.addTab(dateTabSpec);
        mTabHost.addTab(timeTabSpec);
        mTabHost.setIndicatorBackgroundColor(mAccentColor);
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                if ("Tab1".equalsIgnoreCase(tabId)) {
                    mDateTabLabel.setTextColor(mAccentColor);
                    mTimeTabLabel.setTextColor(mNomalColor);
                } else {
                    mTimeTabLabel.setTextColor(mAccentColor);
                    mDateTabLabel.setTextColor(mNomalColor);
                }
            }
        });
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    private void adjusetHeight() {
        View content = findViewById(android.R.id.tabcontent);
        int height = 0;
        if (mDatePicker.isLunarSwitchShown()) {
            if (mContext.getResources()
                    .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                height = dip2px(mContext, 192);
            } else {
                height = dip2px(mContext, 232);
            }
        } else {
            height = dip2px(mContext, 180);
        }
        content.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

    public CyeeDatePicker getDatePicker() {
        return mDatePicker;
    }

    public CyeeTimePicker getTimePicker() {
        return mTimePicker;
    }

    @Override
    public void onDateChanged(CyeeDatePicker view, int year, int monthOfYear, int dayOfMonth) {
//		mCalendar.set(monthOfYear, monthOfYear, dayOfMonth);
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateDatePickerTitle();
    }

    @Override
    public void onTimeChanged(CyeeTimePicker view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        updateTimePickerTitle();
    }

    private void updateDatePickerTitle() {
//		Date date = mCalendar.getTime();
//		mDateTabLabel.setText(DateFormat.getDateFormat(mContext).format(date));
        String title = buildTitle();
        mDateTabLabel.setText(title);
    }

    private String buildTitle() {
        Date date = mCalendar.getTime();
        SimpleDateFormat formater = new SimpleDateFormat("MM/dd/yyyy");
    	//chenyee <Cyee_Framework> hushengsong 2018-05-23 modify for SW17W16KR-159 begin
	if(isKRLanguage()){
	    formater = new SimpleDateFormat("yyyy/MM/dd");
	}
	//chenyee <Cyee_Framework> hushengsong 2018-05-23 modify for SW17W16KR-159 end
        formater.setTimeZone(mCalendar.getTimeZone());

        return (String) formater.format(date);
    }

    private void updateTimePickerTitle() {
        Date date = mCalendar.getTime();
        if (mTimePicker == null) {
            return;
        }
	//Chenyee <CY_FrameWork> hushengsong 2018-03-14 modify for 12hour-format begin
        SimpleDateFormat formater = new SimpleDateFormat("hh:mm aa");
        if (mTimePicker.is24HourView()) {
            formater = new SimpleDateFormat("HH:mm");
        }
	//Chenyee <CY_FrameWork> hushengsong 2018-03-14 modify for 12hour-format end
        formater.setTimeZone(mCalendar.getTimeZone());
        mTimeTabLabel.setText(formater.format(date));
    }

    public void updateCalendar(Calendar calendar) {
        if (calendar == null) {
            Log.e(TAG, "updateDate calendar == null");
            return;
        }
        mCalendar = calendar;
        updateDatePicker();
        updateTimePicker();

        updateDatePickerTitle();
        updateTimePickerTitle();
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public void set24HourFormat(boolean is24HourFormat) {
        mTimePicker.setIs24HourView(is24HourFormat);
        updateTimePicker();
	//Chenyee <CY_FrameWork> hushengsong 2018-03-14 modify for 12hour-format begin
        updateTimePickerTitle();
	//Chenyee <CY_FrameWork> hushengsong 2018-03-14 modify for 12hour-format end
    }

    public void showLunarModeSwitch() {
        mDatePicker.showLunarModeSwitch();
        adjusetHeight();
    }

    public void hideLunarModeSwitch() {
        mDatePicker.hideLunarModeSwitch();
        adjusetHeight();
    }

    private final LunarModeChangedListener mLunarModeChangedListener = new LunarModeChangedListener() {

        @Override
        public void onModeChanged(boolean isLunar) {
            updateDatePickerTitle();
        }
    };

    public void setLunarChecked(boolean isLunarMode) {
        mDatePicker.setLunarChecked(isLunarMode);
    }

    public void setMinDate(long minDate) {
        mDatePicker.setMinDate(minDate);
    }

    public void setMaxDate(long maxDate) {
        mDatePicker.setMaxDate(maxDate);
    }

    public void setMinHour(int minHour) {
        mTimePicker.setMinHour(minHour);
    }

    public void setMinMinute(int minMinute) {
        mTimePicker.setMinMinute(minMinute);
    }

    public void setMaxMinute(int maxMinute) {
        mTimePicker.setMaxMinute(maxMinute);
    }

    public void setMaxHour(int maxHour) {
        mTimePicker.setMaxHour(maxHour);
    }

    public void setCurrentPage(int index) {
        if (mTabHost == null) {
            return;
        }

        if (index != 0 && index != 1) {
            return;
        }

        mTabHost.setCurrentTab(index);
    }
    
    public static class CyeeDateTimePickerTabWidget extends CyeeTabWidget {
        
        private static final int DIVIDER_HEIGHT_IN_PX = 2;
        private final Paint mPaint;
        private final Context mContext;

        public CyeeDateTimePickerTabWidget(Context context, AttributeSet attrs) {
            super(context, attrs);
            // TODO Auto-generated constructor stub
            mContext = context;
            mPaint = initPaint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
                return;
            }
            int y = getBottom();
            int padding = getChildAt(0).getLeft();
            Rect r = new Rect(padding, y - DIVIDER_HEIGHT_IN_PX, getWidth() - padding, y);
            canvas.drawRect(r, mPaint);
        }

        private Paint initPaint() {
            Paint paint = new Paint();
            paint.setColor(mContext.getResources().getColor(
                    com.cyee.internal.R.color.cyee_button_stroke_color));
            paint.setStrokeWidth(1);
            return paint;
        }
    }
	//chenyee <Cyee_Framework> hushengsong 2018-05-23 modify for SW17W16KR-159 begin
	private boolean isKRLanguage() {
            return getResources().getConfiguration().locale.getCountry().equals("KR")||getResources().getConfiguration().locale.getLanguage().equals("ko");
    }
	//chenyee <Cyee_Framework> hushengsong 2018-05-23 modify for SW17W16KR-159 end
    
}
