/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2019. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.wheatek.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class NetworkControllerUtils {
    private static final String TAG = "NetworkControllerUtils";

    public static final String NETWORK_DATA_UPDATE = "com.mediatek.security.action.NETWORK_DATA_UPDATE";
    public static int GET_ALL_PACKAGE = 1;
    public static int GET_3TH_PACKAGE = 2;

    /**
     * Get application name by passing the package name
     *
     * @param context
     *            Context
     * @param pkgName
     *            package name
     * @return the application name
     */
    public static String getApplicationName(Context context, String pkgName) {
        if (pkgName == null) {
            return null;
        }
        String appName = null;
        try {
            PackageManager pkgManager = context.getPackageManager();
            ApplicationInfo info = pkgManager.getApplicationInfo(pkgName, 0);
            appName = String.valueOf(pkgManager.getApplicationLabel(info));
            Log.d(TAG, "getApplicationName:appName: " + appName + "pkgName: " + pkgName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    /**
     * Get application icon by passing the package name
     *
     * @param context
     *            Context
     * @param pkgName
     *            package name
     * @return the application icon
     */
    public static Drawable getApplicationIcon(Context context, String pkgName) {
        Log.d(TAG, "getApplicationName:pkgName: " + pkgName);
        PackageManager pm = context.getPackageManager();
        Drawable icon = null;
        try {
            icon = pm.getApplicationIcon(pkgName);
            //need test if pass pkgName can get icon
        } catch (NameNotFoundException ex) {
            icon = pm.getDefaultActivityIcon();
            Log.e(TAG, pkgName + " app icon not found, using generic app icon");
        }
        return icon;
    }
}
