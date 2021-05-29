package cyee.app;

import java.util.Locale;

import cyee.theme.global.CyeeResources;
import cyee.widget.CyeeNumberPicker.Formatter;
import cyee.widget.CyeeTextView;
import cyee.widget.CyeeTimePicker;
import cyee.widget.CyeeTimePicker.OnTimeChangedListener;
import cyee.widget.CyeeWidgetResource;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.filterfw.io.GraphIOException;
import android.os.Bundle;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

/**
 * A dialog that prompts the user for the time of day using a {@link TimePicker}.
 *
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-timepicker.html">Time Picker
 * tutorial</a>.</p>
 */
public class CyeeTimePickerDialog extends CyeeAlertDialog
        implements OnClickListener, OnTimeChangedListener {

    private static final int HOUR_OF_HALF_DAY = 12;

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        void onTimeSet(CyeeTimePicker view, int hourOfDay, int minute);
    }

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    private static final int AMPM_SPINNER_WIDTH_DP = 90;
    private static final int HOUR_SPINNER_WIDTH_DP = 92;
    private static final int MINUTE_SPINNER_WIDTH__DP = 92;

    private final CyeeTimePicker mTimePicker;
    private final CyeeTextView mTitleView;
    private final Context mContext;

    private final OnTimeSetListener mCallback;

    int mInitialHourOfDay;
    int mInitialMinute;
    boolean mIs24HourView;

    // Gionee <gaoj> <2013-9-27> add for CR00899138 begin
    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public CyeeTimePickerDialog(Context context,
            OnTimeSetListener callBack,
            int hourOfDay, int minute, boolean is24HourView) {
        this(context, resolvedTheme(context,com.cyee.internal.R.attr.cyeedatePickerDialogStyle), callBack, hourOfDay, minute, is24HourView);
    }
    // Gionee <gaoj> <2013-9-27> add for CR00899138 end

    private static int resolvedTheme(Context cxt,int resId) {
        TypedValue outValue = new TypedValue();
        cxt.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.resourceId;
    }
    
    /**
     * @param context Parent.
     * @param theme the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public CyeeTimePickerDialog(Context context,
            int theme,
            OnTimeSetListener callBack,
            int hourOfDay, int minute, boolean is24HourView) {
        super(CyeeAlertDialog.getCyeeContext(context, theme), theme);
        mCallback = callBack;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourView = is24HourView;
        mContext = context;

        setIcon(0);
        // Gionee <gaoj> <2013-9-27> delete for CR00899138 begin
//        setTitle(com.cyee.internal.R.string.cyee_time_picker_dialog_title);
        // Gionee <gaoj> <2013-9-27> add for CR00899138 end

        Context themeContext = getContext();
        setButton(BUTTON_POSITIVE, themeContext.getText(com.cyee.internal.R.string.cyee_ok), this);
		// maxw modify: 增加取消按钮
        setButton(BUTTON_NEGATIVE, themeContext.getText(com.cyee.internal.R.string.cyee_cancel), this);
        
        LayoutInflater inflater =
                (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(com.cyee.internal.R.layout.cyee_time_picker_dialog, null);
        setView(view);
        mTimePicker = (CyeeTimePicker) view.findViewById(com.cyee.internal.R.id.cyee_timePicker);
        mTitleView = (CyeeTextView) view.findViewById(com.cyee.internal.R.id.title_tv);
        // initialize state
        //Chenyee <Cyee_Widget> hushengsong 2018-07-13 modify for CSW1707LT-40 begin
        //mTimePicker.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        //Chenyee <Cyee_Widget> hushengsong 2018-07-13 modify for CSW1707LT-40 end
        mTimePicker.setOnTimeChangedListener(this);
        mTimePicker.setIs24HourView(mIs24HourView);
        mTimePicker.setCurrentHour(mInitialHourOfDay);
        mTimePicker.setCurrentMinute(mInitialMinute);
        mTimePicker.setSpinnersWidth(dip2px(mContext, AMPM_SPINNER_WIDTH_DP),
                dip2px(mContext, HOUR_SPINNER_WIDTH_DP), dip2px(mContext, MINUTE_SPINNER_WIDTH__DP));
    }
    
    public void onClick(DialogInterface dialog, int which) {
    	// gionee maxw modify begin
    	if(which == DialogInterface.BUTTON_POSITIVE) {
    		tryNotifyTimeSet();
    	}
    	// gionee maxw modify end
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minutOfHour);
    }

    // gionee maxw add begin
    @Override
    public void onTimeChanged(CyeeTimePicker timePicker, int hourOfDay, int minute) {
        StringBuilder builder = new StringBuilder();
        if(!timePicker.is24HourView()){
            builder.append((hourOfDay % HOUR_OF_HALF_DAY)).append(" : ").append(format(minute));
            Locale locale = mTimePicker.getCurrentLocale(); 
            if (locale != null && isChineseLanguage()) {
                builder.insert(0, mTimePicker.getDisplayedAMPMs());
            }else {
                builder.append(mTimePicker.getDisplayedAMPMs());
            }
        }else {
            builder.append((hourOfDay)).append(" : ").append(format(minute));
        }
        mTitleView.setText(builder.toString());
    }
    // gionee maxw add end

    private boolean isChineseLanguage() {
        return mContext.getResources().getConfiguration().locale.getCountry().equals("CN")
                || mContext.getResources().getConfiguration().locale.getCountry().equals("TW");
    }
    
    private void tryNotifyTimeSet() {
        if (mCallback != null) {
            mTimePicker.clearFocus();
            mCallback.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(),
                    mTimePicker.getCurrentMinute());
        }
    }

    @Override
    protected void onStop() {
	// maxw modify:只有用户点击done按钮时修改时间，这里就不再修改时间了
//        tryNotifyTimeSet();
        super.onStop();
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, mTimePicker.getCurrentHour());
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
    }


    public CyeeTimePicker getTimePicker() {
    	    return mTimePicker;
    }

    private String format(int value) {
        Formatter formatter = mTimePicker.getFormatter();
        return (formatter != null) ? formatter.format(value) : String.valueOf(value);
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
