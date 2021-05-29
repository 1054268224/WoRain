/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyee.internal.app;

import cyee.app.CyeeActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.util.TypedValue;
import com.cyee.utils.Log;
/**
 * An activity that follows the visual style of an AlertDialog.
 * 
 * @see #mAlert
 * @see #mAlertParams
 * @see #setupAlert()
 */
public abstract class CyeeAlertActivity extends CyeeActivity implements DialogInterface {

    /**
     * The model for the alert.
     * 
     * @see #mAlertParams
     */
    protected CyeeAlertController mAlert;
    
    /**
     * The parameters for the alert.
     */
    protected CyeeAlertController.AlertParams mAlertParams;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("CyeeAlertActivity","onCreate start");
        TypedValue outValue = new TypedValue();
        boolean ret = getTheme().resolveAttribute(com.cyee.internal.R.attr.cyeeDialogOtherBtnTxtColor, outValue, true);
        if (!ret) {
            setTheme(com.cyee.internal.R.style.Theme_Cyee_Light_Dialog_Alert);
        }
        setShowVirtualKeyboard(false);
        super.onCreate(savedInstanceState);
        mAlert = new CyeeAlertController(this, this, getWindow());
        mAlertParams = new CyeeAlertController.AlertParams(this);
        
        getWindow().setGravity(Gravity.BOTTOM);
        Log.e("CyeeAlertActivity","onCreate end");
    }

    @Override
    protected void onResume() {
        Log.e("CyeeAlertActivity","onResume start");
        super.onResume();
    }
    
    //Gionee <weidong> <2015-2-3> add for CR01441318 begin
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
    //Gionee <weidong> <2015-2-3> add for CR01441318 end

    public void cancel() {
        Log.e("CyeeAlertActivity","cancel start");
        finish();
    }

    public void dismiss() {
        // This is called after the click, since we finish when handling the
        // click, don't do that again here.
        Log.e("CyeeAlertActivity","dismiss start isFinishing()="+isFinishing());
        if (!isFinishing()) {
            finish();
        }
    }

    /**
     * Sets up the alert, including applying the parameters to the alert model,
     * and installing the alert's content.
     * 
     * @see #mAlert
     * @see #mAlertParams
     */
    protected void setupAlert() {
        mAlertParams.apply(mAlert);
        mAlert.installContent();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAlert.onKeyDown(keyCode, event)) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mAlert.onKeyUp(keyCode, event)) return true;
        return super.onKeyUp(keyCode, event);
    }
    
    public void onDestroy() {
        Log.e("CyeeAlertActivity","onDestroy start ");
        super.onDestroy();
    }
    
}
