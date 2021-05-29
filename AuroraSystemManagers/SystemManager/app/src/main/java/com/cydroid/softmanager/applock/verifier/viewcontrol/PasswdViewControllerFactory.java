/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.viewcontrol;

import android.content.Context;

import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public final class PasswdViewControllerFactory {
    private static final String TAG = "PasswdViewControllerFactory";

    private static final long PWD_PATTERN_TYPE = 1L;
    private static final long PWD_FOUR_NUMBER_TYPE = 2L;
    private static final long PWD_MISC_TYPE = 3L;

    private static PasswdViewControllerFactory sInstance;
    private static final HashMap<Long, Class<?>> PASSWD_VIEW_CONTROLLER_LIST =
            new HashMap<>();

    static {
        PASSWD_VIEW_CONTROLLER_LIST.put(PWD_PATTERN_TYPE, PatternPwdViewController.class);
        //guoxt 2017-07-24 modify for 172874  begin
        //PASSWD_VIEW_CONTROLLER_LIST.put(PWD_FOUR_NUMBER_TYPE, FourPwdViewController.class);
        PASSWD_VIEW_CONTROLLER_LIST.put(PWD_FOUR_NUMBER_TYPE, MiscPwdViewController.class);
        //guoxt 2017-07-24 modify for 172874  end
        PASSWD_VIEW_CONTROLLER_LIST.put(PWD_MISC_TYPE, MiscPwdViewController.class);
    }

    public synchronized static PasswdViewControllerFactory getInstance() {
        if (null == sInstance) {
            sInstance = new PasswdViewControllerFactory();
        }
        return sInstance;
    }

    private PasswdViewControllerFactory() {
    }

    public BasePwdViewController createPasswdViewController(
            long type, Context context) {
        Class<?> passwdViewControllerClass = PASSWD_VIEW_CONTROLLER_LIST.get(type);
        if (passwdViewControllerClass != null) {
            return createPasswdViewController(passwdViewControllerClass, context);
        }
        return null;
    }

    private BasePwdViewController createPasswdViewController(
            Class<?> passwdViewControllerClass, Context context) {
        BasePwdViewController passwdViewController = null;
        try {
            Constructor con = passwdViewControllerClass.getConstructor(Context.class);
            passwdViewController = (BasePwdViewController) con.newInstance(context);
        } catch (Exception e) {
            Log.e(TAG, "createPasswdViewController exception:" + e.toString());
        }
        return passwdViewController;
    }
}
