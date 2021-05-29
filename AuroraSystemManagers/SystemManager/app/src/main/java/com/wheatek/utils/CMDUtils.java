package com.wheatek.utils;

import android.util.Log;

import java.io.DataOutputStream;

@Deprecated
public class CMDUtils {


    private static final String UNINSTALL = "pm uninstall ";
    private static final String INSTALL = "pm install ";
    private static final String TAG = CMDUtils.class.getSimpleName();

    /***
     * @param command
     * @return
     */
    public static boolean exusecmd(String pachakge, String command, CallBack callBack) {
        Process process = null;
        DataOutputStream os = null;
        String message = "";
        boolean re = false;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.flush();
            process.waitFor();
            Log.e(TAG, command + "---success" + process.exitValue());
            re = true;
        } catch (Exception e) {
            Log.e(TAG, command + "---failed:" + e.toString());
            message = e.toString();
            re = false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (callBack != null) {
                    callBack.onResult(re, message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return re;
    }

    /**
     * pm 命令实现静默安装卸载 不需要权限,
     *
     * @param pageName
     */
    public static boolean unInstallApk(String pageName) {
        return exusecmd(pageName, UNINSTALL + pageName, null);
    }

    /**
     * pm 命令实现静默安装卸载 不需要权限,
     *
     * @param pageName
     */
    public static boolean unInstallApk(String pageName, CallBack callBack) {
        return exusecmd(pageName, UNINSTALL + pageName, callBack);
    }


    /**
     * pm 命令实现静默安装卸载 不需要权限,
     *
     * @param pageName
     */
    public static boolean installApk(String pageName) {
        return exusecmd(pageName, INSTALL + pageName, null);
    }

    /**
     * pm 命令实现静默安装卸载 不需要权限,
     *
     * @param pageName
     */
    public static boolean installApk(String pageName, CallBack callBack) {
        return exusecmd(pageName, INSTALL + pageName, callBack);
    }

    public interface CallBack {
        void onResult(boolean success, String message);
    }
}
