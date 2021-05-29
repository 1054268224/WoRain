/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.browser;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.widget.Toast;

//import com.mediatek.custom.CustomProperties;
import com.wheatek.reflect.CustomProperties;
import com.wheatek.shy.mylibrary.Callb;
import com.wheatek.shy.mylibrary.MNETUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Browser extends Application {

    private final static String LOGTAG = "browser";

    // M : flag for auto on/off log
    public final static boolean DEBUG =
            (!Build.TYPE.equals("user")) ? true :
                    SystemProperties.getBoolean("ro.mtk_browser_debug_enablelog", false);
    public final static boolean ENGONLY =
            (Build.TYPE.equals("eng")) ? true :
                    SystemProperties.getBoolean("ro.mtk_browser_debug_enablelog", false);
    public final static String UAPROF = CustomProperties
            .getString(CustomProperties.MODULE_BROWSER, CustomProperties.UAPROF_URL);
    public final static String HEADER = "X_WAP_PROFILE";

    // Set to true to enable verbose logging.
    final static boolean LOGV_ENABLED = false;

    // Set to true to enable extra debug logging.
    final static boolean LOGD_ENABLED = true;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences info = this.getSharedPreferences("info", MODE_PRIVATE);
        Boolean isFirst = info.getBoolean("isFirst", true);
        if (isFirst) {
            MNETUtils.Companion.postPhoneInfotest(this, new Callb() {
                @Override
                public void response(@NotNull String response, boolean isSuccess) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "调用结果" + response + isSuccess, Toast.LENGTH_SHORT).show();
                            if (isSuccess) {
                                SharedPreferences.Editor edit = info.edit();
                                edit.putBoolean("isFirst", false);
                                edit.commit();
                            }
                        }
                    });
                }
            });
        }
        BrowserSettings.initialize(getApplicationContext());
        Preloader.initialize(getApplicationContext());

        /// M: Add for Regional Phone support. @{
        try {
            Extensions.getRegionalPhonePlugin(getApplicationContext())
                    .updateBookmarks(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BrowserSettings.getInstance().updateSearchEngineSetting();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ///     / @}
    }
}

