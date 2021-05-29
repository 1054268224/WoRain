package com.cydroid.systemmanager.rubbishcleaner.util;

public class FileNameUtils {
    public static String getFileNameFromAbsolutelypath(String filePath) {
        if (null == filePath || filePath.isEmpty()) {
            return "";
        }
        int pos = filePath.lastIndexOf('/');
        return filePath.substring(pos + 1);
    }

    public static String getPathFromAbsolutelypath(String filePath) {
        if (null != filePath && !filePath.isEmpty()) {
            int pos = filePath.lastIndexOf('/');
            if (pos > 0 && pos < filePath.length()) {
                return filePath.substring(0, pos);
            } else {
                return filePath;
            }
        }
        return "";
    }
}
