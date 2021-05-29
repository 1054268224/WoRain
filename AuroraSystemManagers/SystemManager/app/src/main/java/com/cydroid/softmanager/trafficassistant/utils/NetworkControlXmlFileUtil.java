/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: mengdw
 *
 * Date: 2017-04-19 for 81367
 */
package com.cydroid.softmanager.trafficassistant.utils;

import android.os.Build;
import android.os.Environment;
import android.util.AtomicFile;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.Xml;

import com.android.internal.util.FastXmlSerializer;
import com.cydroid.softmanager.utils.Log;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkControlXmlFileUtil {
    private static final String TAG = "NetworkControlXmlFileUtil";
    private static final String LIST_TAG = "list";
    private static final String DISABLED_APP_TAG = "app";
    private static final String MOBILE_DISABLE_FILENAME = "mobile_disable_files.xml";
    private static final String WIFI_DISABLE_FILENAME = "wifi_disable_files.xml";

    private static NetworkControlXmlFileUtil mNetworkControlXmlFileUtil;
    
    private final AtomicFile mMobileDisableFile;
    private final AtomicFile mWifiDisableFile;
    
    public static NetworkControlXmlFileUtil getInstance() {
        if (null == mNetworkControlXmlFileUtil) {
            synchronized (NetworkControlXmlFileUtil.class) {
                if (null == mNetworkControlXmlFileUtil) {
                    mNetworkControlXmlFileUtil = new NetworkControlXmlFileUtil();
                }
            }
        }
        return mNetworkControlXmlFileUtil;
    }
    
    private NetworkControlXmlFileUtil() {
        mMobileDisableFile = new AtomicFile(new File(getDisabledFileDir(), MOBILE_DISABLE_FILENAME));
        mWifiDisableFile = new AtomicFile(new File(getDisabledFileDir(), WIFI_DISABLE_FILENAME));
    }
    
    private File getDisabledFileDir() {
            return new File(Environment.getDataDirectory(), "misc/msdata");
    }
    
    public synchronized List<String> getDisabledApps(int netType) {
        if (Constant.MOBILE == netType) {
            return readXmlFile(mMobileDisableFile);
        } else {
            return readXmlFile(mWifiDisableFile);
        }
    }
    
    private List<String> readXmlFile(AtomicFile file) {
        List<String> disableApps = new ArrayList<String>();
        FileInputStream in = null;
        try {
            in = file.openRead();
            final XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(in, null);
            int type;
            while ((type = xmlParser.next()) != END_DOCUMENT) {
                final String tag = xmlParser.getName();
                if (type == START_TAG && DISABLED_APP_TAG.equals(tag)) {
                    String pkgName = xmlParser.nextText();
                    if (!disableApps.contains(pkgName)) {
                        disableApps.add(pkgName);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "readXmlFile FileNotFoundException e=" + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "readXmlFile IOException e=" + e.toString());
        } catch (XmlPullParserException e) {
            Log.e(TAG, "readXmlFile XmlPullParserException e=" + e.toString());
        } finally {
//            IoUtils.closeQuietly(in);
        }
        return disableApps;
    }
    
    public synchronized void saveDisabledApps(List<String> apps, int netType) {
        if (Constant.MOBILE == netType) {
            writeXmlFile(mMobileDisableFile, apps);
        } else {
            writeXmlFile(mWifiDisableFile, apps);
        }
    }
    
    private void writeXmlFile(AtomicFile file, List<String> disableApps) {
        FileOutputStream fileOut = null;
        try {
            fileOut = file.startWrite();
            XmlSerializer xmlOut = new FastXmlSerializer();
            xmlOut.setOutput(fileOut, "utf-8");
            xmlOut.startDocument(null, true);
            xmlOut.startTag(null, LIST_TAG);
            for (int i = 0; i < disableApps.size(); i++) {
                final String pkgName = disableApps.get(i);
                xmlOut.startTag(null, DISABLED_APP_TAG);
                xmlOut.text(pkgName);
                xmlOut.endTag(null, DISABLED_APP_TAG);
            }
            xmlOut.endTag(null, LIST_TAG);
            xmlOut.endDocument();
            file.finishWrite(fileOut);
        } catch (IOException e) {
            Log.d(TAG, "writeDisableUidsRules e=" + e.toString());
            if (fileOut != null) {
                file.failWrite(fileOut);
            }
        } finally {
//            IoUtils.closeQuietly(fileOut);
        }
    }
}

