package com.wheatek.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cydroid.softmanager.R;

public class AlerDialogUtils {

    public static Dialog createSingleSelectDialog(ViewGroup decorView, Context context, String title, String[] list, int currentIndex, OnSelectChangeListener changeListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.select_dialog_wyh, decorView, false);
        TextView mTitle = view.findViewById(R.id.title);
        mTitle.setText(title);
        ViewGroup mSelectContainerLay = view.findViewById(R.id.select_container_lay);
        int i = 0;
        for (String s : list) {
            View layer = inflater.inflate(R.layout.select_dialog_item_container, mSelectContainerLay, false);
            TextView mName = layer.findViewById(R.id.name);
            mName.setText(s);
            CheckBox mCheckbox = layer.findViewById(R.id.checkbox);
            mCheckbox.setChecked(i == currentIndex);
            i++;
            mSelectContainerLay.addView(layer);
            layer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup parent = ((ViewGroup) v.getParent());
                    boolean ischange = false;
                    int selectedIndex = 0;
                    for (int i1 = 0; i1 < parent.getChildCount(); i1++) {
                        View the = parent.getChildAt(i1);
                        if (v == the) {
                            if (!((CheckBox) the.findViewById(R.id.checkbox)).isChecked()) {
                                ((CheckBox) the.findViewById(R.id.checkbox)).setChecked(true);
                                ischange = true;
                                selectedIndex = i1;
                            }
                        } else {
                            if (((CheckBox) the.findViewById(R.id.checkbox)).isChecked()) {
                                ((CheckBox) the.findViewById(R.id.checkbox)).setChecked(false);
                            }
                        }
                    }
                    if (ischange && changeListener != null) {
                        changeListener.OnSelectChange(selectedIndex);
                    }
                }
            });
        }
        Dialog dialog = new Dialog(context);
        dialog.setContentView(view);
        dialog.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        dialog.getWindow().getDecorView().setBackgroundResource(R.drawable.select_dialog_bg_wyg);
        return dialog;
    }

    /**
     * 仅限该类创建的dialog
     *
     * @param alertDialog
     * @param title
     * @param currentindex
     */
    public static void refreshdialogdata(Dialog alertDialog, String title, int currentindex) {
        ((TextView) alertDialog.getWindow().getDecorView().findViewById(R.id.title)).setText(title);
        View v = ((ViewGroup) alertDialog.getWindow().getDecorView().findViewById(R.id.select_container_lay)).getChildAt(currentindex);
        ViewGroup parent = ((ViewGroup) v.getParent());
        for (int i1 = 0; i1 < parent.getChildCount(); i1++) {
            View the = parent.getChildAt(i1);
            if (v == the) {
                if (!((CheckBox) the.findViewById(R.id.checkbox)).isChecked()) {
                    ((CheckBox) the.findViewById(R.id.checkbox)).setChecked(true);

                }
            } else {
                if (((CheckBox) the.findViewById(R.id.checkbox)).isChecked()) {
                    ((CheckBox) the.findViewById(R.id.checkbox)).setChecked(false);
                }
            }
        }
    }

    public interface OnSelectChangeListener {
        void OnSelectChange(int selectedIndex);
    }
}
