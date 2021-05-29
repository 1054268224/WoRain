package com.cydroid.softmanager.softmanager.defaultsoft;

import android.content.Intent;
import android.net.Uri;

import com.cydroid.softmanager.common.Consts;

public final class DefMrgSoftIntent {
    public static final int DEF_INPUT_METHOD = 0;
    public static final int DEF_BROWSER = 1;
    public static final int DEF_PHONE = DEF_BROWSER + 1;
    public static final int DEF_HOME = DEF_BROWSER + 2;
    public static final int DEF_CAMERA = DEF_BROWSER + 3;
    public static final int DEF_GALLAY = DEF_BROWSER + 4;
    public static final int DEF_MUSIC = DEF_BROWSER + 5;
    public static final int DEF_VIDEO = DEF_BROWSER + 6;
    public static final int DEF_READER = DEF_BROWSER + 7;
    // Gionee <lihq> <2013-05-28> modify add for CR00819503 begin
    // Gionee <houjie> <2015-09-21> modify add for CR01556005 begin
    private static final String[] MUSIC_DATA_TYPES = new String[]{"audio/*", "application/ogg",
            "application/x-ogg", "application/itunes", "application/aac", "application/imy",
            "application/amr", "application/mp3"};
    // Gionee <houjie> <2015-09-21> modify add for CR01556005 end
    // Gionee <lihq> <2013-05-28> modify add for CR00819503 end

    /*guoxt modify for CSW1702A-2320 begin */
    public static Intent getDefIntent(int i) {
        Intent intent = null;
        switch (i) {
            case DEF_BROWSER:
                intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse("http://www.baidu.com"));
                break;
            case DEF_PHONE:
                intent = new Intent("com.android.contacts.action.LIST_CONTACTS");
                break;
            case DEF_HOME:
                intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                break;
            case DEF_CAMERA:
                intent = new Intent("android.media.action.IMAGE_CAPTURE");
                break;
            case DEF_GALLAY:
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse("file:///android_asset/cydata"), "image/*");
                break;
            case DEF_MUSIC:
                intent = new Intent("android.intent.action.VIEW");
                // Gionee <lihq> <2013-05-28> modify for CR00819503 begin
                // intent.setDataAndType(Uri.parse("file:///android_asset/gionee"),
                // "audio/*");
                // Gionee <houjie> <2015-08-05> modify for CR01529597 begin
                /*
                intent.setDataAndType(Uri.parse("file:///android_asset/gionee"), "audio/mpeg");
                */
                intent.setDataAndType(Uri.parse("file:///android_asset/cydata"), "audio/*");
                // Gionee <houjie> <2015-08-05> modify for CR01529597 end
                // Gionee <lihq> <2013-05-28> modify for CR00819503 end
                break;
            case DEF_VIDEO:
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse("file:///android_asset/cydata"), "video/*");
                break;
            case DEF_READER:
                //guoxt modify for CSW1703OTA-427 begin
                if(Consts.cyBAFlag){
                    intent = new Intent("android.intent.action.WEB_SEARCH");
                }else {
                    intent = new Intent("android.intent.action.VIEW");
                    intent.setDataAndType(Uri.parse("content:///android_asset/cydata"), "text/plain");
                }
                //guoxt modify for CSW1703OTA-427 end
                break;
            default:
                break;
        }
        return intent;
    }
     /*guoxt modify for CSW1702A-2320 end */

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "MS_EXPOSE_REP", justification = "seems no problem")
    public static String[] getMusicDataTypes() {
        return MUSIC_DATA_TYPES;
    }
}
