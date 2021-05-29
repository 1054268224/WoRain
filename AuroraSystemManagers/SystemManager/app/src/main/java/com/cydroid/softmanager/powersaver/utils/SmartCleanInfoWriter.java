package com.cydroid.softmanager.powersaver.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cydroid.softmanager.utils.Log;

import android.os.Environment;
import android.os.Build;

/**
 * 工具类，供SmartPowerSaverService写数据用 记录清理数据,可在"拨号*#837504#>系统信息统计>智能内存清理"查看
 */
public class SmartCleanInfoWriter {
    private final static String TAG = "SaveSmartCleanLog";
    /*guoxt modify for CSW1702A-2175 begin*/
    private static final String FILE_PATH_MSDATA = "/data/misc/msdata";
    /*guoxt modify for CSW1702A-2175 end*/
    private final static String FILE_NAME = "clean_log";

    public static void log(String tag, String str) {
        writeToDataFile(getStringDate(System.currentTimeMillis()) + " " + tag + ":" + str);
    }

    public static String getStringDate(long currentTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");
        String dateString = formatter.format(new Date(currentTime));
        return dateString;
    }

    public static void writeToDataFile(String str) {
        String strFile = getDataFilePath();

        try {
            File dirFile;
             dirFile= new File(FILE_PATH_MSDATA);

            if (!dirFile.exists() && dirFile.mkdir()) {
            }

            File file = new File(strFile);
            if (isFileSizeGreater(file) && file.delete()) {
            }

            if (!file.exists() && file.createNewFile()) {
            }

            FileOutputStream out = new FileOutputStream(file, true); // true表示在文件末尾添加
            String msg = str + "\n";
            out.write(msg.getBytes(StandardCharsets.UTF_8));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(true, TAG, "write data to file throw exception error = " + e.getMessage());
        }
    }

    public static String getDataFilePath() {
            return FILE_PATH_MSDATA + "//" + FILE_NAME;
    }

    private static boolean isFileSizeGreater(File file) {
        float f = 0;
        if (file.exists()) {
            try {
                long fileSize = file.length();

                f = ((float) fileSize / (float) (1024 * 1024));

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // 文件大小小于 0.5M
        return !(f < 0.5);
    }
}
