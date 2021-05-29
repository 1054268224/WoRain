package com.cydroid.softmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;

public class UtilizationAlertDialog {
    private static final String TAG = "UtilizationAlertDialog";

    // Gionee: mengdw <2016-10-31> add for CR01770072 begin
    public interface DialogBtnOnclickListener {
        void btnPositiveClick();
        void btnNegativeClick();
    }

    // Gionee: mengdw <2016-10-31> add for CR01770072 end
    private final CyeeActivity mContext;
    private CheckBox mCheckBox;
    // Gionee: mengdw <2015-08-14> add for CR01535940 begin
    public CyeeAlertDialog mCyeeAlertDialog;

    // Gionee: mengdw <2016-10-31> add for CR01770072 begin
    private final List<DialogBtnOnclickListener> mDialogBtnOnclickListenerList = new ArrayList<DialogBtnOnclickListener>();

    public void addDialogBtnOnclickListener(DialogBtnOnclickListener listener) {
        if (!mDialogBtnOnclickListenerList.contains(listener)) {
            mDialogBtnOnclickListenerList.add(listener);
        }
    }

    public void removeDialogBtnOnclickListener(DialogBtnOnclickListener listener) {
        mDialogBtnOnclickListenerList.remove(listener);
    }
    // Gionee: mengdw <2016-10-31> add for CR01770072 end
    // Gionee: mengdw <2015-08-14> add for CR01535940 end

    public UtilizationAlertDialog(CyeeActivity context) {
        mContext = context;
    }

    public void verdictWhetherShowDialog() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean flag = preferences.getBoolean("is_first_utilization", true);
        if (flag) {
            // Gionee <yangxinruo> <2015-10-19> add for CR01570066 begin
            // Gionee <yangxinruo> <2015-10-21> modify for CR01571937 begin
            SharedPreferences mainPreferences = mContext
                    .getSharedPreferences(MainActivity.MAIN_PROCESS_PREFERENCE, Context.MODE_MULTI_PROCESS);
            boolean isColor = mainPreferences.getBoolean("color_restart", false);
            // Gionee <yangxinruo> <2015-10-21> modify for CR01571937 end
            if (!isColor) {
                // Gionee <yangxinruo> <2015-10-19> add for CR01570066 end
                createAndShowDialog();
            } else {
                // Gionee <yangxinruo> <2015-12-7> modify for CR01605401 begin
                mainPreferences.edit().putBoolean("color_restart", false).commit();
                // Gionee <yangxinruo> <2015-12-7> modify for CR01605401 end
            }
        }
    }

    public void createAndShowDialog() {
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mContext);
        builder.setTitle(R.string.utilization_title);
        // builder.setMessage(R.string.utilization_content);
        builder.setPositiveButton(R.string.dialog_continue_btn, listener);
        builder.setNegativeButton(R.string.dialog_exit_btn, listener);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.custom_alertdialog_layout, null);
        mCheckBox = (CheckBox) view.findViewById(R.id.dialog_check_box);
        //Gionee <guoxt> <2014-07-25> add for CR01325489 begin
        mCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });
        //Gionee <guoxt> <2014-07-25> add for CR01325489 end
        builder.setView(view);
        CyeeAlertDialog dialog = builder.show();
        mCyeeAlertDialog = dialog;
        dialog.setCancelable(false);
    }

    // Gionee: mengdw <2015-08-14> add for CR01535940 begin
    public void dismissDialog() {
        if (mCyeeAlertDialog != null) {
            mCyeeAlertDialog.dismiss();
            mCyeeAlertDialog = null;
        }
    }
    // Gionee: mengdw <2015-08-14> add for CR01535940 end

    private final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case CyeeAlertDialog.BUTTON_POSITIVE:
                    setAlertDialogFlag();
                    // Gionee: mengdw <2016-10-31> add for CR01770072 begin
                    for (DialogBtnOnclickListener listener : mDialogBtnOnclickListenerList) {
                        listener.btnPositiveClick();
                    }
                    // Gionee: mengdw <2016-10-31> add for CR01770072 end
                    dialog.dismiss();
                    break;

                case CyeeAlertDialog.BUTTON_NEGATIVE:
                    removeAlertDialogFlag();
                    // Gionee: mengdw <2016-10-31> add for CR01770072 begin
                    for (DialogBtnOnclickListener listener : mDialogBtnOnclickListenerList) {
                        listener.btnNegativeClick();
                    }
                    // Gionee: mengdw <2016-10-31> add for CR01770072 end
                    mContext.finish();
                    break;
                default:
                    break;
            }
        }

    };

    private void setAlertDialogFlag() {
        //fengpeipei modify for 58464 start
        if (mCheckBox != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            Log.d(TAG, "setAlertDialogFlag is_first_utilization:" + (!mCheckBox.isChecked()));
            preferences.edit().putBoolean("is_first_utilization", !mCheckBox.isChecked()).commit();
        }
        //fengpeipei modify for 58464 end
    }

    private void removeAlertDialogFlag() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        preferences.edit().remove("is_first_utilization");
    }

}