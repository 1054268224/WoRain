package cyee.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import com.cyee.utils.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import cyee.theme.global.CyeeContextThemeWrapper;
import cyee.widget.CyeeDatePicker;
import cyee.widget.CyeeTextView;
import cyee.widget.CyeeWidgetResource;
import cyee.widget.CyeeDatePicker.OnDateChangedListener;
import cyee.widget.CyeeDatePicker.LunarModeChangedListener;
import com.cyee.internal.R;
import cyee.changecolors.ChameleonColorManager;

import java.util.Calendar;

import com.cyee.internal.util.Lunar;

public class CyeeDatePickerDialog extends CyeeAlertDialog implements OnClickListener, OnDateChangedListener {

    private static final int YEAR_SPINNER_WIDTH_DP = 104;
    private static final int MONTH_SPINNER_WIDTH_DP = 104;
    private static final int DAY_SPINNER_WIDTH_DP = 104;
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private final CyeeDatePicker mDatePicker;
    private final OnDateSetListener mCallBack;
    private final Context mContext;

    private boolean mTitleNeedsUpdate = true;

    // gionee maxw add begin
    private final CyeeTextView mTitleTv;

    // gionee maxw add end

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
        void onDateSet(CyeeDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    // Gionee <gaoj> <2013-9-27> add for CR00899138 begin
    /**
     * @param context
     *            The context the dialog is to run in.
     * @param callBack
     *            How the parent is notified that the date is set.
     * @param year
     *            The initial year of the dialog.
     * @param monthOfYear
     *            The initial month of the dialog.
     * @param dayOfMonth
     *            The initial day of the dialog.
     */
    public CyeeDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear,
            int dayOfMonth) {

        this(context, resolvedTheme(context,
                com.cyee.internal.R.attr.cyeedatePickerDialogStyle), callBack,
                year, monthOfYear, dayOfMonth);
    }

    // Gionee <gaoj> <2013-9-27> add for CR00899138 end

    private static int resolvedTheme(Context cxt, int resId) {
        TypedValue outValue = new TypedValue();
        cxt.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.resourceId;
    }

    /**
     * @param context
     *            The context the dialog is to run in.
     * @param theme
     *            the theme to apply to this dialog
     * @param callBack
     *            How the parent is notified that the date is set.
     * @param year
     *            The initial year of the dialog.
     * @param monthOfYear
     *            The initial month of the dialog.
     * @param dayOfMonth
     *            The initial day of the dialog.
     */
    public CyeeDatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year,
            int monthOfYear, int dayOfMonth) {
        super(CyeeAlertDialog.getCyeeContext(context,theme), theme);
        
        mContext = getContext();

        mCallBack = callBack;

        Context themeContext = getContext();
        setButton(BUTTON_POSITIVE,
                themeContext.getText(com.cyee.internal.R.string.cyee_ok),
                this);
        // maxw modify:增加取消按钮
        setButton(
                BUTTON_NEGATIVE,
                themeContext.getText(com.cyee.internal.R.string.cyee_cancel),
                this);
        setIcon(0);

        LayoutInflater inflater = (LayoutInflater) themeContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(
                com.cyee.internal.R.layout.cyee_date_picker_dialog, null);
        setView(view);
        mDatePicker = (CyeeDatePicker) view.findViewById(com.cyee.internal.R.id.cyee_datePicker);
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        mDatePicker.setLunarModeChangedListener(mLunarModeChangedListener);
        mDatePicker.setSpinnersWidth(dip2px(mContext, YEAR_SPINNER_WIDTH_DP),
                dip2px(mContext, MONTH_SPINNER_WIDTH_DP),
                dip2px(mContext, DAY_SPINNER_WIDTH_DP));
        mTitleTv = (CyeeTextView) view.findViewById(com.cyee.internal.R.id.title_tv);
        updateTitle();
    }

    public void onClick(DialogInterface dialog, int which) {
        // gionee maxw modify begin
        if (which == DialogInterface.BUTTON_POSITIVE) {
            tryNotifyDateSet();
        }
        // gionee maxw modify end
    }

    public void onDateChanged(CyeeDatePicker view, int year, int month, int day) {
        updateTitle();
    }

    /**
     * Gets the {@link DatePicker} contained in this dialog.
     *
     * @return The calendar view.
     */
    public CyeeDatePicker getDatePicker() {
        return mDatePicker;
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
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    private void tryNotifyDateSet() {
        if (mCallBack != null) {
            mDatePicker.clearFocus();
            mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(), mDatePicker.getMonth(),
                    mDatePicker.getDayOfMonth());
        }
    }

    @Override
    protected void onStop() {
        // maxw modify:只有用户点击done按钮时才保存设置的时间，这里就不再保存了
//        tryNotifyDateSet();
        super.onStop();
    }

    // gionee maxw modify begin
    private void updateTitle() {
        // Gionee <gaoj> <2013-9-27> delete for CR00899138 begin
        if (!mDatePicker.getCalendarViewShown()) {
            String title = buildTitle();
//            setTitle(title);
            mTitleTv.setText(title);
            mTitleNeedsUpdate = true;
        } else {
            if (mTitleNeedsUpdate) {
                mTitleNeedsUpdate = false;
                mTitleTv.setText(com.cyee.internal.R.string.cyee_date_picker_dialog_title);
                // setTitle(com.cyee.internal.R.string.cyee_date_picker_dialog_title);
            }
        }
        // Gionee <gaoj> <2013-9-27> delete for CR00899138 end
    }

    // gionee maxw modify end

    private String buildTitle() {
        return DateUtils.formatDateTime(mContext, mDatePicker.getCurrentTimeMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, this);
    }

    public void showLunarModeSwitch() {
        mDatePicker.showLunarModeSwitch();
    }

    public void hideLunarModeSwitch() {
        mDatePicker.hideLunarModeSwitch();
    }

    private final LunarModeChangedListener mLunarModeChangedListener = new LunarModeChangedListener() {

        @Override
        public void onModeChanged(boolean isLunar) {
            String title = buildTitle();
            mTitleTv.setText(title);
        }
    };
    
    public void setLunarChecked(boolean isLunarMode) {
        mDatePicker.setLunarChecked(isLunarMode);
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
