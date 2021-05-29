package com.chenyee.fulltheme.resinfo;

import cyee.changecolors.ChameleonColorManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import com.cyee.utils.Log;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import android.nfc.Tag;

/**
 * Created by ws on 16-8-23.
 */
public class FullThemeResInfoManager {

    private static final String TAG = "FullThemeResInfoManager";

    private static final String URI_QUERY = "content://com.cyee.chameleon.provider/colorConfiguration";
    public static final int ID_COLOR_POWER_SAVING_MODE = 2;

    private final static String CONTACTS_VERGIN_PKG_NAME = "com.android.contacts";
    private final static String MMS_VERGIN_PKG_NAME = "com.android.mms";
    private final static String CONTROLLER_VERGIN_PKG_NAME = "com.android.systemui";

    private final static String CONTACT_FULLTHEME_PACKAGE_NAME = "com.fulltheme.contacts";
    private final static String MMS_FULLTHEME_PACKAGE_NAME = "com.fulltheme.mms";
    private final static String CONTROLLER_FULLTHEME_PACKAGE_NAME = "com.fulltheme.controller";

    private final static String CONTACT_RES_PATH = "data/misc/msdata/theme/fulltheme/FullThemeResForContact.apk";
    private final static String MMS_RES_PATH = "data/misc/msdata/theme/fulltheme/FullThemeResForMMS.apk";
    private final static String CONTROLLER_RES_PATH = "data/misc/msdata/theme/fulltheme/FullThemeResForController.apk";
    //Gionee<LeiYong><2016-11-04> modify for M2017 BEGIN
    private final static String M2017_RES_PATH = "system/app/FullThemeForM2017.apk";
    private final static String M2017_RES_PACKAGENAME = "com.fulltheme.m2017";
    //Gionee<LeiYong><2016-11-04> modify for M2017 END

    private final Map<String, String> mAppToFullThemePackage;
    private final Map<String, String> mAppToApkPath;
    private final Set<String> mPicNameSet = new HashSet<String>();

    private final String mResArrayName = "theme_pic_res_array";
    private boolean mIsM2017 = false;

    private FullThemeResInfoManager() {
        mAppToFullThemePackage = new HashMap<String, String>();
        mAppToApkPath = new HashMap<String, String>();

        mAppToFullThemePackage.put(CONTACTS_VERGIN_PKG_NAME, CONTACT_FULLTHEME_PACKAGE_NAME);
        mAppToFullThemePackage.put(MMS_VERGIN_PKG_NAME, MMS_FULLTHEME_PACKAGE_NAME);
        mAppToFullThemePackage.put(CONTROLLER_VERGIN_PKG_NAME, CONTROLLER_FULLTHEME_PACKAGE_NAME);

        mAppToApkPath.put(CONTACTS_VERGIN_PKG_NAME, CONTACT_RES_PATH);
        mAppToApkPath.put(MMS_VERGIN_PKG_NAME, MMS_RES_PATH);
        mAppToApkPath.put(CONTROLLER_VERGIN_PKG_NAME, CONTROLLER_RES_PATH);

        File m2017File = new File(M2017_RES_PATH);
        mIsM2017 = m2017File.exists();
    }

    public boolean isM2017() {
        return this.mIsM2017;
    }
    
    public static FullThemeResInfoManager getInstance() {
        return FullThemeResInfoManagerHolder.sInsance;
    }


    /***
     * @param context
     * @return
     */
    public String getFullThemeApkPath(Context context) {

        if (mIsM2017) {
            return M2017_RES_PATH;
        }

        if (ChameleonColorManager.isPowerSavingMode()) {
            return null;
        }

        String packageName = context.getPackageName();
        String apkPath = mAppToApkPath.get(packageName);
        Log.d(TAG, "getFullThemeApkPath " + apkPath);
        if (apkPath == null) {
            return null;
        }
        File file = new File(apkPath);

        if (file.exists()) {
            return apkPath;
        }
        return null;
    }


    /**
     * @param context
     * @param picFileName
     * @return if the picture in the packageName should be reload from apk file
     */
    public boolean needReLoadNewRes(Context context, Resources resource, String picFileName) {

        String fullApkPackageName = null;
        if (mIsM2017) {
            fullApkPackageName = M2017_RES_PACKAGENAME;
        } else {
            String packageName = context.getPackageName();
            fullApkPackageName = mAppToFullThemePackage.get(packageName);
        }


        Log.d(TAG, "needReLoadNewRes " + fullApkPackageName);
        if (fullApkPackageName == null) {
            return false;
        }

        if (mPicNameSet.isEmpty()) {
            int arrayId = resource.getIdentifier(mResArrayName, "array", fullApkPackageName);
            if (arrayId != 0) {
                String[] resArray = resource.getStringArray(arrayId);
                for (String resName : resArray) {
                    mPicNameSet.add(resName);
                }
            }
        }
        boolean needReload = false;


        if (mPicNameSet.contains(picFileName)) {
            needReload = true;
        }
        Log.d(TAG, "needReLoadNewRes " + picFileName + "   needReload=" + needReload);
        return needReload;
    }


    /****
     * @param context
     * @return package name of apk file accroding app context,null is returned if there is no relative apk file.
     */
    public String getApkPackageName(Context context) {
        String appPackageName = context.getPackageName();
        return mAppToFullThemePackage.get(appPackageName);
    }

    public static boolean isPowerSavingModeInDB(Context context) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.parse(URI_QUERY);
        Cursor cursor = cr.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int colorId = cursor.getInt(cursor.getColumnIndex("id"));
            return colorId == ID_COLOR_POWER_SAVING_MODE;
        }
        return false;
    }

    private static class FullThemeResInfoManagerHolder {
        public static FullThemeResInfoManager sInsance = new FullThemeResInfoManager();
    }


}
