package com.cydroid.softmanager.view;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.cydroid.systemmanager.utils.Log;

public class UnitUtil {
    //Gionee <xuwen><2015-07-28> add for CR01527111 begin
    public static final String APK_DELETE_ACTION = "com.cydroid.antivirus.package_delete";
    public static final String APK_DELETE_PACKAGE_PATH_KEY = "pckPath";
    public static final String APK_DELETE_PACKAGE_NAME_KEY = "pckName";
    //Gionee <xuwen><2015-07-28> add for CR01527111 end

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

    // Gionee <houjie><2015-10-08> add for CR01562723 begin
    /*
    public static void updateMediaVolume(Context context, 
            AsyncQueryHandler asyncQuery, String fileName) {
        Log.d("UnitUtil", "updateMediaVolume fileName:" + fileName);
        fileName = sqliteEscape(fileName);
        Uri baseUri = MediaStore.Files.getContentUri("external");
        StringBuilder selection = new StringBuilder("_data='" + fileName + "'"
                + " or _data like '" + fileName + "/%'");
        Log.d("UnitUtil", "updateMediaVolume baseUri:" + baseUri.toString());
        if (null != context && null != context.getContentResolver().acquireProvider(baseUri)) {
            asyncQuery.startDelete(0, null, baseUri, selection.toString(), null);
        }
    }
    */
    public static void updateMediaVolume(Context context, 
            AsyncQueryHandler asyncQuery, String fileName) {
        Log.d("UnitUtil", "updateMediaVolume fileName:" + fileName);
        Uri data = Uri.parse("file://" + fileName);     
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
    }

    public static String sqliteEscape(String keyWord){
        //keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("'", "''");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&","/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        Log.d("UnitUtil", "sqliteEscape after keyWord:" + keyWord);
        return keyWord;
    }
    // Gionee <houjie><2015-10-08> add for CR01562723 end
}
