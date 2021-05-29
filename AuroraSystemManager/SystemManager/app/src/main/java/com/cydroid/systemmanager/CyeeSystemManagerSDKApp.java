package com.cydroid.systemmanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.cydroid.softmanager.R;
import com.intel.security.SecurityContext;
import com.intel.security.vsm.RealTimeScan;
import com.intel.security.vsm.ScanTask;
import com.intel.security.vsm.UpdateTask;
import com.intel.security.vsm.VirusScan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cyee.changecolors.ChameleonColorManager;

import com.cydroid.systemmanager.utils.FileUtil;
import com.cydroid.systemmanager.utils.Log;

//guoxt modify for CR01775652 begin
//guoxt modify for CR01775652 end
//guoxt modify for add youju  begin

//guoxt modify for add youju  end	

public class CyeeSystemManagerSDKApp extends Application {
    //	private static final String FILES_PATH = "/data/data/com.cydroid.systemmanager/files";
    private static final String[] SO_PATH = new String[]{"libkcmutil.so"};
    //guoxt modify for CR01775652 begin
    private static final String TAG = "CyeeSystemManagerSDKApp";
    private VirusScan mVirusScan = null;
    private static final int REQ_CODE_PERMISSION = 1;
    private long mThreatCount = 0;
    private RealTimeScan mRealTimeScan = null;
    private UpdateTask mUpdateTask = null;
    private ScanTask mScanTask;
    private SharedPreferences mRealTimePreferences;
    //guoxt modify for CR01775652 end

    @Override
    public void onCreate() {
        super.onCreate();
        ChameleonColorManager.getInstance().register(this);
        //guoxt modify for CR01775652 begin
        mRealTimePreferences = getApplicationContext().getSharedPreferences("auto_update_key",
                Context.MODE_PRIVATE);
        //guoxt modify for CR01775652 end
        // Gionee <houjie> <2015-12-31> add for CR01616363 begin
        // warning: don't check file exist.
        /*
        if (FileUtil.isExistsFile(LIB_PATH + "/" + SO_PATH[0])) {
            return;
        }
        */
        // Gionee <houjie> <2015-12-31> add for CR01616363 end


        //guoxt modify for CR01775652 begin
        //guoxt modify for CSW1702A-843 begin
		/*	new Thread(new Runnable()
			 {            
			   @Override            
			   public void run()             
			   {    initialize();           
			    }        
			   }).start();
		*/
        //guoxt modify for CSW1702A-843 end
        //guoxt modify for CR01775652 end


        //Intent startIntent = new Intent(getApplicationContext(), ForegroundService.class);
        //getApplicationContext().startService(startIntent);
    }

    public static void copySoFile(Context context) {
        String filepath = context.getFilesDir().getPath().toString();
        File dir = new File(filepath);
        if (!dir.exists())
            dir.mkdir();
        String LIB_PATH = filepath + "/lib";
        dir = new File(LIB_PATH);
        if (!dir.exists())
            dir.mkdir();
        for (int i = 0; i < SO_PATH.length; i++) {
            FileUtil.CopyFileFromAssets(context, SO_PATH[i], LIB_PATH,
                    SO_PATH[i]);
        }

        File soFile = new File(LIB_PATH + "/" + SO_PATH[0]);
        String soVersion = "0";
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            soVersion = packInfo.versionCode + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        setSoValue("libkcmutil.so_2", soFile.length(), "SoVersion_new",
                soVersion, context);
    }


    //guoxt modify for CR01775652 begin
    private void initialize() {
        InputStream license = null;
        try {
            license = getResources().openRawResource(R.raw.license_isec_gionee);
        } catch (Exception e) {
            Log.d(TAG, "license Exception" + e);
        }

        try {
            SecurityContext.initialize(getApplicationContext(), license,
                    new SecurityContext.InitializationCallback() {
                        @Override
                        public void onInitialized() {
                            Log.d(TAG, "Intel mobile security SDK initialized");
                        }
                    });

        } catch (Exception e) {
            Log.d(TAG, "Exception  Exception!", e);
        } finally {
            try {
                license.close();
            } catch (IOException e) {
                Log.d(TAG, "Exception  Exception!", e);
            }
        }
    }

//guoxt modify for CR01775652 end	

    public static void setSoValue(String soLengthStr, long lengthValue,
                                  String verSting, String verValue, Context context) {
        String strSharedPreferenceName = new String(context.getPackageName()
                + "_preferences");
        SharedPreferences shardPreferences = context.getSharedPreferences(
                strSharedPreferenceName, 0);
        SharedPreferences.Editor editor = shardPreferences.edit();
        editor.putLong(soLengthStr, lengthValue);
        editor.putString(verSting, verValue);
        editor.commit();
    }
}
