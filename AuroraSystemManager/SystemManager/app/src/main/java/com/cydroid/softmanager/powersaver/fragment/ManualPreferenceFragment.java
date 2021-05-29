package com.cydroid.softmanager.powersaver.fragment;

import android.os.Bundle;
import cyee.preference.CyeePreferenceFragment;
import cyee.widget.CyeeListView;

import com.cydroid.softmanager.R;

public class ManualPreferenceFragment extends CyeePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.manual_preference);
    }


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        setListViewStretch();
    }

    private void setListViewStretch() {
        CyeeListView listView = null;
        //guoxt  20180717 add for Ptest begin
        //listView = getListView();
        //guoxt  20180717 add for Ptest end
        if (listView != null) {
            listView.setStretchEnable(false);
        }
    }

}