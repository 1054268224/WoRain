/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.freeze;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Environment;
import android.util.AtomicFile;
import android.util.Xml;

import com.android.internal.util.FastXmlSerializer;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class FreezeAppUtils {
    private static final String TAG = "FreezeAppManager";

    private static final String FREEZE_TAG = "freeze";
    private static final String FREEZED_TAG = "freezed";
    private static final String FREEZE_CAUTIOUS_TAG = "freezecautious";
    private static final String FREEZEABLE_TAG = "freezeable";

    static final AtomicFile getsFreezeAppsInfoFile(Context context) {
        return new AtomicFile(new File(getDataFilePath(context), "freezeappinfo.xml"));
    }

    public static String getDataFilePath(Context context) {
        return context.getFilesDir().getAbsolutePath() + "/data/misc/msdata/";
    }

    static boolean loadFreezeAppsInfoFromXml(List<String> freezeNormalPackages,
                                             List<String> freezeCautiousPackages, List<String> freezedPackages,
                                             List<String> freezableNormalPackages, AtomicFile freezeAppsInfoFile) {
        boolean ret = false;
        FileInputStream fis = null;
        try {
            fis = freezeAppsInfoFile.openRead();
            final XmlPullParser in = Xml.newPullParser();
            in.setInput(fis, null);
            int type;
            while ((type = in.next()) != END_DOCUMENT) {
                final String tag = in.getName();
                if (type == START_TAG) {
                    if (FREEZE_TAG.equals(tag)) {
                        addToNoContains(freezeNormalPackages, in.nextText());
                    } else if (FREEZE_CAUTIOUS_TAG.equals(tag)) {
                        addToNoContains(freezeCautiousPackages, in.nextText());
                    } else if (FREEZED_TAG.equals(tag)) {
                        addToNoContains(freezedPackages, in.nextText());
                    } else if (FREEZEABLE_TAG.equals(tag)) {
                        addToNoContains(freezableNormalPackages, in.nextText());
                    }
                }
            }

            if (freezeNormalPackages.size() > 0 || freezeCautiousPackages.size() > 0
                    || freezedPackages.size() > 0) {
                ret = true;
            }
        } catch (Exception e) {
            ret = false;
            Log.e(TAG, "loadFreezeAppsInfoFromXml", e);
        } finally {
//            IoUtils.closeQuietly(fis);
        }
        Log.d(TAG, "loadFreezeAppsInfoFromXml ret:" + ret);
        HelperUtils.dumpList(TAG, "loadFreezeAppsInfoFromXml freezeNormalPackages", freezeNormalPackages);
        HelperUtils.dumpList(TAG, "loadFreezeAppsInfoFromXml freezeCautiousPackages", freezeCautiousPackages);
        HelperUtils.dumpList(TAG, "loadFreezeAppsInfoFromXml freezedPackages", freezedPackages);
        HelperUtils.dumpList(TAG, "loadFreezeAppsInfoFromXml freezableNormalPackages", freezableNormalPackages);
        return ret;
    }

    static void loadFreezeAppsInfoByDefault(Context context, List<String> freezeNormalPackages,
                                            List<String> freezeCautiousPackages, List<String> freezableNormalPackages,
                                            int normalFreezeAppArrayResIndex, int cautiousFreezeAppArrayResIndex) {
        String[] freezeNormalPackagesDefault = context.getResources().
                getStringArray(normalFreezeAppArrayResIndex);
        String[] freezeCautiousPackagesDefault = context.getResources().
                getStringArray(cautiousFreezeAppArrayResIndex);

        for (String packageName : freezeNormalPackagesDefault) {
            freezeNormalPackages.add(packageName);
            freezableNormalPackages.add(packageName);
        }

        for (String packageName : freezeCautiousPackagesDefault) {
            freezeCautiousPackages.add(packageName);
        }
        HelperUtils.dumpList(TAG, "loadFreezeAppsInfoByDefault freezeNormalPackages", freezeNormalPackages);
        HelperUtils.dumpList(TAG, "loadFreezeAppsInfoByDefault freezeCautiousPackages", freezeCautiousPackages);
        HelperUtils.dumpList(TAG, "loadFreezeAppsInfoByDefault freezableNormalPackages", freezableNormalPackages);
    }

    static List<ApplicationInfo> filterNoFreezeThirdApps(Context context, List<ApplicationInfo> applications,
                                                         int noFreezeAppResIndex) {
        String[] noFreezePackages = context.getResources().
                getStringArray(noFreezeAppResIndex);
        List<String> noFreezePackagesList = Arrays.asList(noFreezePackages);
        for (int i = 0; i < applications.size(); ++i) {
            ApplicationInfo info = applications.get(i);
            if (null == info || noFreezePackagesList.contains(info.packageName)) {
                applications.remove(i);
                --i;
            }
        }
        Log.d(TAG, "filterNoFreezeThirdApps applications.size():" + applications.size());
        return applications;
    }

    static boolean isAppInNoFreezeApps(Context context, String packageName,
                                       int noFreezeAppResIndex) {
        String[] noFreezePackages = context.getResources().
                getStringArray(noFreezeAppResIndex);
        List<String> noFreezePackagesList = Arrays.asList(noFreezePackages);

        return null != packageName && noFreezePackagesList.contains(packageName);
    }

    static List<String> filterInNoFreezeApps(Context context, List<String> packages,
                                             int noFreezeAppResIndex) {
        ArrayList<String> packagesFiltered = new ArrayList<>();
        String[] noFreezePackages = context.getResources().
                getStringArray(noFreezeAppResIndex);
        List<String> noFreezePackagesList = Arrays.asList(noFreezePackages);

        int count = packages.size();
        for (int i = 0; i < count; ++i) {
            String packageName = packages.get(i);
            if (null != packageName && noFreezePackagesList.contains(packageName)) {
                packagesFiltered.add(packageName);
                packages.remove(i);
                --i;
                --count;
            }
        }
        return packagesFiltered;
    }

    static List<String> filterUninstallFreezeApps(Context context, List<String> freezeApps) {
        for (int i = 0; i < freezeApps.size(); ++i) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(context, freezeApps.get(i));
            if (null == info) {
                freezeApps.remove(i);
                --i;
            }
        }
        Log.d(TAG, "filterUninstallFreezeApps freezeApps.size():" + freezeApps.size());
        return freezeApps;
    }

    static void writeFreezeAppsInfoToXml(List<String> freezeNormalPackages,
                                         List<String> freezeCautiousPackages, List<String> freezedPackages,
                                         List<String> freezableNormalPackages, AtomicFile freezeAppsInfoFile) {
        FileOutputStream fos = null;
        try {
            fos = freezeAppsInfoFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, "utf-8");
            out.startDocument(null, true);
            for (int i = 0; i < freezeNormalPackages.size(); i++) {
                out.startTag(null, FREEZE_TAG);
                out.text("" + freezeNormalPackages.get(i));
                out.endTag(null, FREEZE_TAG);
            }

            for (int i = 0; i < freezeCautiousPackages.size(); i++) {
                out.startTag(null, FREEZE_CAUTIOUS_TAG);
                out.text("" + freezeCautiousPackages.get(i));
                out.endTag(null, FREEZE_CAUTIOUS_TAG);
            }

            for (int i = 0; i < freezedPackages.size(); i++) {
                out.startTag(null, FREEZED_TAG);
                out.text("" + freezedPackages.get(i));
                out.endTag(null, FREEZED_TAG);
            }

            for (int i = 0; i < freezableNormalPackages.size(); i++) {
                out.startTag(null, FREEZEABLE_TAG);
                out.text("" + freezableNormalPackages.get(i));
                out.endTag(null, FREEZEABLE_TAG);
            }

            out.endDocument();
            freezeAppsInfoFile.finishWrite(fos);
        } catch (Exception e) {
            if (fos != null) {
                freezeAppsInfoFile.failWrite(fos);
            }
            Log.e(TAG, "writeFreezeAppsInfoToXml", e);
        } finally {
//            IoUtils.closeQuietly(fos);
        }
    }

    public static void addToNoContains(List<String> l, String s) {
        if (!l.contains(s)) {
            l.add(s);
        }
    }

    public static void loadRelateLockApps(Context context, Map<String,
            List<String>> relatedLockApps, int relateAppArrayResIndex) {
        String[] relateLockAppPairs = context.getResources()
                .getStringArray(relateAppArrayResIndex);
        for (String pair : relateLockAppPairs) {
            String[] pairInfo = pair.split("/");
            String[] relatedApps = pairInfo[1].split(",");
            HelperUtils.dumpList(TAG, "loadRelateLockApps " + pairInfo[0], Arrays.asList(relatedApps));
            relatedLockApps.put(pairInfo[0], Arrays.asList(relatedApps));
        }
    }
}