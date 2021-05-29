/**
 * Created by guoxt on 18-8-13.
 */
package com.cydroid.softmanager.applock;

//import com.gionee.youju.statistics.sdk.YouJuAgent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import com.cydroid.softmanager.R;

import cyee.app.CyeeAlertDialog;
import cyee.preference.CyeePreference;
import cyee.preference.CyeePreferenceFragment;
import cyee.preference.CyeePreferenceScreen;
import cyee.preference.CyeeSwitchPreference;

import android.view.WindowManager;
import android.widget.Toast;

import com.cydroid.softmanager.applock.RadioButtonPreference;
public class AppLockSettingsFragment extends CyeePreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private static final String NIGHT_POWER = "key_night_mode";
    private static final String CREATE_SHORT_CUT_KEY = "key_create_shortcut";
    // Gionee <yangxinruo> <2015-10-10> add for CR01565255 begin
    public static final String PREFERENCE_NAME = "powermanager_setting";
    // Gionee <yangxinruo> <2015-10-10> add for CR01565255 end
    // private CyeeSwitchPreference mNightPower, mShortCut;
    private CyeeSwitchPreference mShortCut;
    private Context mContext;
    private RadioButtonPreference mLockScreenoff;
    private RadioButtonPreference mLockRightNow;


    private CyeePreferenceScreen mClearPassWord;
    private CyeePreferenceScreen mChangePassWord;
    private final AppLockManager mAppLockManager = AppLockManager.getInstance();




    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // Gionee <yangxinruo> <2015-10-10> add for CR01565255 begin
        this.getPreferenceManager().setSharedPreferencesName(PREFERENCE_NAME);

        addPreferencesFromResource(R.xml.applock_preference);


        mContext = getActivity();
        mLockScreenoff =  (RadioButtonPreference) findPreference("key_lock_screenoff_mode");
        mLockRightNow =   (RadioButtonPreference) findPreference("key_lock_rightnow_mode");
        mClearPassWord =  (CyeePreferenceScreen) findPreference("clear_password_key");
        mChangePassWord =  (CyeePreferenceScreen) findPreference("change_password_key");

        mLockScreenoff.setOnPreferenceClickListener(monPreferenceClickListener);
        mLockRightNow.setOnPreferenceClickListener(monPreferenceClickListener);
        mClearPassWord.setOnPreferenceClickListener(monPreferenceClickListener);
        mChangePassWord.setOnPreferenceClickListener(monPreferenceClickListener);

    }


    private void refreshRadioButton(){
        mLockScreenoff.setChecked(true);
        mLockRightNow.setChecked(false);
    }

    private final CyeePreference.OnPreferenceClickListener monPreferenceClickListener = new CyeePreference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(CyeePreference arg0) {
            if (arg0 == mLockScreenoff) {
                mLockScreenoff.setChecked(true);
                mLockRightNow.setChecked(false);
                mAppLockManager.setAppLockSetting(getActivity(), 0);
            } else if (arg0 == mLockRightNow) {
                mLockScreenoff.setChecked(false);
                mLockRightNow.setChecked(true);
                mAppLockManager.setAppLockSetting(getActivity(), 1);
            } else if (arg0 == mChangePassWord) {
                AppLockSecurityPasswordUtils.startChangeSecurityPassword(getActivity());
                Toast.makeText(mContext, R.string.change_password, Toast.LENGTH_SHORT).show();
            } else if (arg0 == mClearPassWord) {

                //AppLockSecurityPasswordUtils.startClearSecurityPassword(getActivity());
                //getActivity().finish();
                //Toast.makeText(mContext, R.string.clear_password, Toast.LENGTH_SHORT).show();
                createDialog();
            }
            return false;
        }
    };

    // guoxt modify for CSW1802LT-51 begin
    private CyeeAlertDialog mCheckDialog;

    private void createDialog() {
        String message = null;
        mCheckDialog = new CyeeAlertDialog.Builder(mContext, CyeeAlertDialog.THEME_CYEE_FULLSCREEN)
                .create();
        mCheckDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mCheckDialog.setTitle(mContext.getResources().getString(R.string.remove_app_lock));
            message = mContext.getResources().getString(R.string.remove_app_lock_msg);
        mCheckDialog.setMessage(message);

        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CyeeAlertDialog.BUTTON_POSITIVE:
                        AppLockSecurityPasswordUtils.startClearSecurityPassword(getActivity());
                        //chenyee zhaocaili modify for CSW1705P-249 begin
                        //getActivity().finish();
                        //chenyee zhaocaili modify for CSW1705P-249 end
                        break;
                    case CyeeAlertDialog.BUTTON_NEGATIVE:
                        //chenyee zhaocaili modify for CSW1705P-249 begin
                        //getActivity().finish();
                        //chenyee zhaocaili modify for CSW1705P-249 end
                        break;
                    default:
                        break;
                }
            }
        };

        mCheckDialog.setButton(CyeeAlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok_btn),
                dialogClickLsn);
        mCheckDialog.setButton(CyeeAlertDialog.BUTTON_NEGATIVE, mContext.getResources().getString(R.string.cancel_btn),
                dialogClickLsn);
        mCheckDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        mCheckDialog.show();
    }
    // guoxt modify for CSW1802LT-51 end



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

//        android.util.Log.d("dzmdzm", "onSharedPreferenceChanged~~~");
//            if (mLockScreenoff.isChecked()) {
//                mLockRightNow.setChecked(false);
//                mLockScreenoff.setChecked(true);
//            }else if(mLockRightNow.isChecked()){
//                mLockScreenoff.setChecked(false);
//                mLockRightNow.setChecked(true);
//
//            }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

}
